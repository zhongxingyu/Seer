 package fr.labri.timedautomata;
 
 import java.awt.Dimension;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.jdom2.Attribute;
 import org.jdom2.Document;
 import org.jdom2.Element;
 import org.jdom2.JDOMException;
 import org.jdom2.Namespace;
 import org.jdom2.input.SAXBuilder;
 import org.jdom2.input.sax.XMLReaderXSDFactory;
 import org.jdom2.output.Format;
 import org.jdom2.output.XMLOutputter;
 
 import edu.uci.ics.jung.algorithms.layout.FRLayout;
 import edu.uci.ics.jung.algorithms.layout.Layout;
 import edu.uci.ics.jung.graph.DirectedGraph;
 import edu.uci.ics.jung.graph.DirectedSparseGraph;
 import edu.uci.ics.jung.visualization.BasicVisualizationServer;
 import fr.labri.AutoQualifiedClassLoader;
 import fr.labri.Utils;
 import fr.labri.timedautomata.CompiledTimedAutomata.DelegatedTimedAutomata;
 
 public abstract class TimedAutomata<C> implements ITimedAutomata<C> {
 	public static final String ROOT_TAG = "timedautomata";
 
 	public static final String STATE_TAG = "state";
 	public static final String TRANSITION_TAG = "path";
 	public static final String TIMEOUT_TAG = "timeout";
 	
 	public static final String STATE_ACTION_TAG = "action";
 	public static final String STATE_URGENT_TAG = "action";
 	public static final String STATE_ATTR_TAG = "attr";
 	public static final String STATE_NAME_TAG = "name";
 	public static final String STATE_INITIAL_TAG = "initial";
 	
 	public static final String TRANSITION_TARGET_TAG = "to";
 	public static final String TRANSITION_PREDICATE_TAG = "guard";
 	public static final String TRANSITION_TIMEOUT_TAG = "timeout";
 	
 //	public static final String XMLNS="http://www.w3.org/namespace/";
 	public static final String XMLNS_XSI="http://www.w3.org/2001/XMLSchema-instance";
 	public static final String XSI_LOCATION="http://www.labri.fr/~fmoranda/xsd/ta.xsd";
 	
 	public static final int INFINITY = -1;
 	public static final int TIMEOUT = 0;
 	
 	Action<C> _initial;
 	final Map<Action<C>, List<Transition>> _transitions = new HashMap<Action<C>, List<Transition>>();
 	final Map<String, Action<C>> _stateMap = new HashMap<String, Action<C>>();
 	final Map<String, Predicate<C>> _transMap = new HashMap<String, Predicate<C>>();
 
 	Action<C> _current;
 	int _currentTime;
 	
 	public void setInitial(Action<C> state) {
 		_initial = state;
 	}
 
 	public void addDefaultTransition(Action<C> from, Action<C> to) {
 		addTransition(from, TIMEOUT, null, to);
 	}
 	
 	public void addTransition(Action<C> from, Predicate<C> trans, Action<C> to) {
 		addTransition(from, INFINITY, trans, to);
 	}
 	
 	public void addTransition(Action<C> from, int timeout, Predicate<C> trans, Action<C> to) {
 		List<Transition> t = _transitions.get(from);
 		if(t == null) {
 			t = new ArrayList<Transition>();
 			_transitions.put(from, t);
 		}
 		t.add(new Transition(timeout, trans, to));
 	}
 	
 	public ITimedAutomata<C> compile(Action<C> init) {
 		_initial = init;
 		return compile();
 	}
 
 	public ITimedAutomata<C> compile() {
 		if(_initial == null)
 			throw new RuntimeException("Initial state not set");
 		
 		return new Compiler().compile();
 	}
 	
 	class Compiler {
 		int[] timeouts;
 		int[] timeoutTargets;
 		int[][] transitionPredicates;
 		int[][] transitionTarget;
 		
 		Map<Action<C>, List<Next>> newNodes;
 		final Map<Action<C>, Integer> nodeIndex = new HashMap<Action<C>, Integer>();
 		final Map<Predicate<C>, Integer> transIndex = new HashMap<Predicate<C>, Integer>();
 		
 		
 		int computeStates() {
 			int nb = _stateMap.size();
 			newNodes = new HashMap<Action<C>, List<Next>>(nb);
 			
 			for(Action<C> node: _transitions.keySet()) {
 				List<Next> n = nextStates(node);
 				nb += Math.max(n.size() - 2, 0); // FIXME make a smarter method in Next
 				newNodes.put(node, n);
 			}
 			
 			return nb;
 		}
 
 		private ITimedAutomata<C> compile() {
 			int nb = computeStates();
 			allocateTables(nb);
 			
 			for(Entry<Action<C>, List<Next>> e: newNodes.entrySet()) {
 				List<Next> lst = e.getValue();
 				int size = lst.size() - 1;
 				int node = getIndex(e.getKey(), nodeIndex);
 				noTimeout(node);
 				Next next = null;
 				
 				for(int i = 0; i < size; i ++) {
 					next = lst.get(i);
 					addTransitions(node, next);
 					if(i < (size-1))
 						node = addTimeout(node, new VirtualState<C>(e.getKey()), next.deadline);
 				}
 				
 				// add what's left
 				Next n = lst.get(size);
 				if(n.deadline == TIMEOUT) {
 					if(n.size() > 0) {
 						if(next == null)
 							System.err.println("States '"+ e.getKey()+"' has only a timeout state !");
 						addTimeout(node, n.states.get(0), next == null ? 0 : next.deadline ); // FIXME does a timeout without alternatives have any sense ?
 					}
 				} else {
 					for(int i = 0; i < n.size(); i++)
 						addTransitions(node, n);
 				}
 			}
 			
 			return newAutomata(mapToActions(nodeIndex), mapToPredicates(transIndex), nodeIndex.get(_initial), transitionPredicates, timeouts, transitionTarget, timeoutTargets);
 		}
 		
 		private void allocateTables(int size) {
 			timeouts = new int[size];
 			timeoutTargets = new int[size];
 			transitionPredicates = new int[size][];
 			transitionTarget = new int[size][];
 		}
 		
 		private List<Next> nextStates (Action<C> state) {
 			int m;
 			List<Transition> nexts =  new ArrayList<Transition>(_transitions.get(state));
 			List<Next> result = new ArrayList<Next>();
 			int offset = 0;
 			while(true) {
 				m = nextDeadLine(nexts, offset);
 				if(m != Integer.MAX_VALUE) {
 					result.add(selectNextState(m - offset, m, nexts));
 					offset = m;
 				} else {
 					switch(nexts.size()) {
 					case 0:
 						throw new RuntimeException("Automata has no default transition for node: "+ state);
 						//		Next timeout = new Next(TIMEOUT);
 						//		result.add(timeout);
 						//	break;
 					case 1:
 						Transition target = nexts.get(0);
 						Next timeout = new Next(target.timeout);
 						timeout.add(target.state, target.predicate);
 						result.add(timeout);
 						break;
 					default:
 						Next infinites = new Next(INFINITY);
 						for(Transition t: nexts) {
 							if(t.timeout != INFINITY)
 								throw new RuntimeException("Cannot mix timeout alternative and infinite guards, neither having more than a single timeout alternative ('"+state+"': " + nexts + ")");
 							infinites.add(t.state, t.predicate);
 						}
 						result.add(infinites);
 					}
 					break;
 				}
 			}
 			
 			return result;
 		}
 		
 		Next selectNextState(int deadline, int elapsed, List<Transition> nexts) {
 			Next next = new Next(deadline);
 			
 			Iterator<Transition> it = nexts.iterator();
 			while(it.hasNext()) {
 				Transition t = it.next();
 				if(t.timeout != TIMEOUT) {
 					next.add(t.state, t.predicate);
 					if(t.timeout == elapsed)
 						it.remove();
 				}
 			}
 			return next;
 		}
 		
 		private int nextDeadLine(List<Transition> targets, int min) {
 			int m = Integer.MAX_VALUE;
 			for(Transition t: targets) {
 				if(t.timeout == Integer.MAX_VALUE)
 					throw new RuntimeException(Integer.MAX_VALUE+" is a reserved timeout value"); // FIXME rewrite without using MAX_VALUE
 					
 				if(t.timeout > min)
 					m = Math.min(m, t.timeout);
 			}
 			return m;
 		}
 		
 		private void noTimeout(int state) {
 			timeouts[state] = INFINITY;
 			timeoutTargets[state] = -1;
 		}
 		
 		private int addTimeout(int state, Action<C> nextState, int deadline) {
 			timeouts[state] = deadline;
 			return timeoutTargets[state] = getIndex(nextState, nodeIndex);
 		}
 		
 		private void addTransitions(int node, Next next) {
 			int ln = next.size();
 			int[] pred = new int[ln];
 			int[] target = new int[ln];
 			transitionPredicates[node] = pred;
 			transitionTarget[node] = target;
 	
 			for(int j = 0; j < ln ; j++) {
 				pred[j] = getIndex(next.trans.get(j), transIndex);
 				target[j] = getIndex(next.states.get(j), nodeIndex);
 			}
 		}
 	}
 	
 	public Action<C> getState(final String name, final String type, final String attr) {
 		if(_stateMap.containsKey(name))
 			return _stateMap.get(name);
 		Action<C> act = newState(name, type, attr);
 		_stateMap.put(name, act);
 		return act;
 	}
 	
 	public Predicate<C> getPredicate(final String name) {
 		if(_stateMap.containsKey(name))
 			return _transMap.get(name);
 		Predicate<C> t = newPredicate(name);
 		_transMap.put(name, t);
 		return t;
 	}
 
 	abstract protected Action<C> newState(String name, String type, String attr);
 	abstract protected Predicate<C> newPredicate(String type);
 	abstract protected ITimedAutomata<C> newAutomata(Action<C>[] states, Predicate<C>[] predicates, int initial, int[][] transitionsPredicates, int[] timeouts, int[][] transitionsTarget, int[] timeoutsTarget);
 	
 	public static <C> TimedAutomata<C> getTimedAutoma(final ContextProvider<C> context, final NodeFactory<C> factory) {
 		return new TimedAutomata<C>(){
 			@Override
 			protected ITimedAutomata<C> newAutomata(Action<C>[] states,
 					Predicate<C>[] predicates, int initial,
 					int[][] transitionsPredicates, int[] timeouts,
 					int[][] transitionsTarget, int[] timeoutsTarget) {
 
 				return new DelegatedTimedAutomata<C>(context, states, predicates, initial, transitionsPredicates, timeouts, transitionsTarget, timeoutsTarget);
 			}
 
 			@Override
 			protected Action<C> newState(String name, String type, String attr) {
 				return factory.newState(name, type, attr);
 			}
 
 			@Override
 			protected Predicate<C> newPredicate(String type) {
 				return factory.newPredicate(type);
 			}
 
 			@Override
 			public C getContext() {
 				return context.getContext();
 			}
 		};
 	}
 	
 	@SuppressWarnings("unchecked")
 	private Action<C>[] mapToActions(Map<Action<C>, Integer> map) {
 		return mapTo(map, new Action[map.size()]);
 	}
 	
 	@SuppressWarnings("unchecked")
 	private Predicate<C>[] mapToPredicates(Map<Predicate<C>, Integer> map) {
 		return mapTo(map, new Predicate[map.size()]);
 	}
 	
 	private <T> T[] mapTo(Map<T, Integer> map, T[] array) {
 		for(Entry<T, Integer> e: map.entrySet())
 			array[e.getValue()] = e.getKey();
 		return array;
 	}
 	
 	private <T> int getIndex(T item, Map<T, Integer> index) {
 		if(index.containsKey(item))
 			return index.get(item);
 		int idx = index.size();
 		index.put(item, idx);
 		return idx;
 	}
 	
 	final public class Transition { // FIXME put this private ASAP
 		final Predicate<C> predicate;
 		final int timeout;
 		final Action<C> state;
 		
 		Transition(int time, Predicate<C> t, Action<C> s) {
 			timeout = time;
 			state = s;
 			predicate = t;
 		}
 		
 		@Override
 		public String toString() {
 			return new StringBuilder("{").append(predicate).append("/").append(timeout).append("->").append(state).append("}").toString();
 		}
 	}
 	
 	class Next implements Iterable<Entry<Action<C>, Predicate<C>>> {
 		final int deadline;
 		ArrayList<Action<C>> states = new ArrayList<Action<C>>();
 		ArrayList<Predicate<C>> trans = new ArrayList<Predicate<C>>();
 		
 		Next(int t) {
 			deadline = t;
 		}
 		
 		void add(Action<C> s, Predicate<C> t) {
 			states.add(s);
 			trans.add(t);
 		}
 		
 		@Override
 		public String toString() {
 			return new StringBuilder("<").append(deadline).append("::").append(states).append(trans).append(">").toString();
 		}
 
 		int size() {
 			return states.size();
 		}
 		
 		@Override
 		public Iterator<Entry<Action<C>, Predicate<C>>> iterator() {
 			return new Iterator<Map.Entry<Action<C>,Predicate<C>>>() {
 				int pos = 0;
 				@Override
 				public boolean hasNext() {
 					return pos < states.size();
 				}
 
 				@Override
 				public Entry<Action<C>, Predicate<C>> next() {
 					return new Entry<Action<C>, Predicate<C>>() {
 						@Override
 						public Action<C> getKey() {
 							return states.get(pos);
 						}
 
 						@Override
 						public Predicate<C> getValue() {
 							return trans.get(pos);
 						}
 
 						@Override
 						public Predicate<C> setValue(Predicate<C> value) {
 							return trans.set(pos, value);
 						}
 					};
 				}
 
 				@Override
 				public void remove() {
 					states.remove(pos);
 					trans.remove(pos);
 				}
 			};
 		}
 	}
 	
 	static private class VirtualState<C> extends NamedAction<C> {
 		public VirtualState(Action<C> state) {
 			super("Virtual"+state.getName(), state);
 		}
 	}
 	
 	static public class NamedAction<C> implements Action<C> {
 		final String _name;
 		final Action<C> _orig;
 
 		public NamedAction(String name, Action<C> state) {
 			_name = name;
 			_orig = state;
 		}
 		@Override
 		public void preAction(C context, ITimedAutomata<C> auto) {
 			_orig.preAction(context, auto);
 		}
 		@Override
 		public void eachAction(C context, ITimedAutomata<C> auto) {
 			_orig.eachAction(context, auto);
 		}
 		@Override
 		public void postAction(C context, ITimedAutomata<C> auto) {
 			_orig.postAction(context, auto);
 		}
 		@Override
 		public String toString() {
 			return _name;
 		}
 
 		@Override
 		public String getType() {
 			return _orig.getType();
 		}
 		@Override
 		public String getName() {
 			return _name;
 		}
 	}
 	
 	public static class StateAdapter<C> implements Action<C> {
 		@Override
 		public void preAction(C context, ITimedAutomata<C> auto) {
 		}
 		@Override
 		public void eachAction(C context, ITimedAutomata<C> auto) {
 		}
 		@Override
 		public void postAction(C context, ITimedAutomata<C> auto) {
 		}
 		@Override
 		public String getName() {
 			return null;
 		}
 		@Override
 		public String getType() {
 			return getClass().getName();
 		}
 	}
 	
 	
 	public static class UrgentState<C> implements Action<C> {
 		final Action<C> _orig;
 
 		public UrgentState(Action<C> state) {
 			_orig = state;
 		}
 		@Override
 		public void preAction(C context, ITimedAutomata<C> auto) {
 			_orig.preAction(context, auto);
 		}
 		@Override
 		public void eachAction(C context, ITimedAutomata<C> auto) {
 			_orig.eachAction(context, auto);
 		}
 		@Override
 		public void postAction(C context, ITimedAutomata<C> auto) {
 			_orig.postAction(context, auto);
 		}
 		@Override
 		public String toString() {
 			return getName();
 		}
 		@Override
 		public String getType() {
 			return _orig.getType();
 		}
 		@Override
 		public String getName() {
 			return _orig.getName();
 		}
 	}
 	
 	public static class TransitionAdapter<C> implements Predicate<C> {
 		public boolean isValid(C context) {
 			return false;
 		}
 
 		@Override
 		public String getType() {
 			return getClass().getName();
 		}
 		
 		public String toString() {
 			return getType();
 		}
 	}
 	
 	final public Document parseXML(InputStream stream, boolean validate) throws JDOMException, IOException {
 		// FIXME if validate == true, it does not work :)
 		SAXBuilder sxb = new SAXBuilder(validate ? new XMLReaderXSDFactory(TimedAutomata.class.getResource("ta.xsd")) : null);
 
 		Document document = sxb.build(stream);
 		return document;
 	}
 	
 	final public void loadXML(InputStream stream) throws JDOMException, IOException {
 		loadXML(parseXML(stream, false));
 	}
 	
 	final public void loadXML(InputStream stream, boolean validate) throws JDOMException, IOException {
 		loadXML(parseXML(stream, validate));
 	}
 	
 	final public void loadXML(Document root) throws JDOMException, IOException {
 		Map<String, Action<C>> names = new HashMap<>();
 	
 		loadXMLStates(root, names);
 		
 		for(Element state: root.getRootElement().getChildren(STATE_TAG)){
 			Action<C> src = names.get(state.getAttributeValue(STATE_NAME_TAG));
 			for(Element trans: state.getChildren(TRANSITION_TAG)) {
 				String pred = trans.getAttributeValue(TRANSITION_PREDICATE_TAG);
 				Action<C> dest = names.get(trans.getAttributeValue(TRANSITION_TARGET_TAG));
 				String timeoutval = trans.getAttributeValue(TRANSITION_TIMEOUT_TAG);
 				int timeout = timeoutval == null ? INFINITY : Integer.parseInt(timeoutval);
 				addTransition(src, timeout, getPredicate(pred), dest);
 			}
 			Element timeout = state.getChild(TIMEOUT_TAG);
 			if(timeout != null) {
 				Action<C> dest = names.get(timeout.getAttributeValue(TRANSITION_TARGET_TAG));
 				addDefaultTransition(src, dest);
 			} 
 		}
 	}
 	
 	private void loadXMLStates(Document root, Map<String, Action<C>> names) throws JDOMException {
 		for(Element state: root.getRootElement().getChildren(STATE_TAG)){
 			String name = state.getAttributeValue(STATE_NAME_TAG);
 			if(names.containsKey(name))
 				throw new JDOMException("Node name is not unique: "+ name);
 			Action<C> st = getState(name, state.getAttributeValue(STATE_ACTION_TAG), state.getAttributeValue(STATE_ATTR_TAG));
 			if("true".equalsIgnoreCase(state.getAttributeValue(STATE_URGENT_TAG))) {
 				st = new UrgentState<C>(st);
 			}
 			if("true".equalsIgnoreCase(state.getAttributeValue(STATE_INITIAL_TAG))) {
 				if(_initial != null)
 					throw new RuntimeException("More than one initial state: '"+_initial+"', '"+st+"'");
 				_initial = st;
 			}
 			names.put(name, st);
 		}
 	}
 	
 	public Document toXML() {
 		Element root = new Element(ROOT_TAG);
 		int slen = _stateMap.size();
 		@SuppressWarnings("unchecked")
 		Action<C>[] states = new Action[slen];
 		_stateMap.values().toArray(states);
 		
 		Namespace ns = Namespace.getNamespace("xsi", XMLNS_XSI);
 		root.addNamespaceDeclaration(ns);
 		root.setAttribute(new Attribute("noNamespaceSchemaLocation", XSI_LOCATION, ns));
 		
 		for(Entry<Action<C>, List<Transition>> e : _transitions.entrySet()) {
 			Element state = xmlState(e.getKey(), states);
 			root.addContent(state);
 			
 			for(Transition t: e.getValue()) {
 				state.addContent(xmlTransition(t, states));
 			}
 		}
 		
 		for(Action<C> src: _stateMap.values()) {
 			if(_transitions.containsKey(src)) continue;
 			root.addContent(xmlState(src, states));
 		}
 		
 		return new Document(root);
 	}
 	
 	private Element xmlState(Action<C> src, Action<C>[] states) {
 		Element state = new Element(STATE_TAG);
 
 		state.setAttribute(new Attribute(STATE_NAME_TAG, getNodeName(src, states)));
 		if(src == _initial)
 			state.setAttribute(new Attribute(STATE_INITIAL_TAG, "true"));
 		
 		if(src instanceof UrgentState) 
 			state.setAttribute(new Attribute(STATE_URGENT_TAG, "true"));
 
 		String type = src.getType();
 		if(type != null)
 			state.setAttribute(new Attribute(STATE_ACTION_TAG, type));
 		return state;
 	}
 	
 	private Element xmlTransition(Transition t, Action<C>[] states) {
 		Element path = new Element(t.predicate == null ? TIMEOUT_TAG : TRANSITION_TAG );
 		path.setAttribute(new Attribute(TRANSITION_TARGET_TAG, getNodeName(t.state, states)));
 		if(t.predicate != null) {
 			if(t.timeout > 0)
 				path.setAttribute(new Attribute(TRANSITION_TIMEOUT_TAG, Integer.toString(t.timeout)));
 			path.setAttribute(new Attribute(TRANSITION_PREDICATE_TAG, t.predicate.getType()));
 		}
 		return path;
 	}
 	
 	public static <C> String getNodeName(Action<C> state, Action<C>[] states) {
 		String name = state.getName();
 		return (name == null) ? "node" + Integer.toString(Utils.indexOf(state, states)) : name;
 	}
 	
 	public final <S extends OutputStream> S xmlToStream(S stream) throws IOException {
 		new XMLOutputter(Format.getPrettyFormat()).output(toXML(), stream);
 		return stream;
 	}
 
 	public final String toString() {
 		try {
 			return xmlToStream(new ByteArrayOutputStream()).toString();
 		} catch (IOException e) {
 			return new StringBuilder("<error>").append(e.getMessage()).append("</error>").toString();
 		}
 	}
 
 	public String toDot(String name) {
 		StringBuilder b = new StringBuilder("digraph ").append(name).append(" {\n");
 		int slen = _stateMap.size();
 		@SuppressWarnings("unchecked")
 		Action<C>[] states = new Action[slen];
 		_stateMap.values().toArray(states);
 		
 		for(int i = 0; i < slen; i++) {
 			b.append(getNodeName(states[i], states)).append(" [label=\"").append(states[i].getType()).append("\"");
 			if(states[i] == _initial)
 				b.append(", shape=\"doubleoctagon\"");
 			b.append("];\n");
 		}
 		
 		for(Entry<Action<C>, List<Transition>> e: _transitions.entrySet()) {
 			Action<C> src = e.getKey();
 			for(Transition t: e.getValue())
 				if(t.timeout == TIMEOUT)
 					b.append(getNodeName(src, states)). append(" -> ").append(getNodeName(t.state, states)). append(" [style=dashed];\n");
 				else {
 					b.append(getNodeName(src, states)).append(" -> ").append(getNodeName(t.state, states)).append(" [label=\"").append(t.predicate.getType());
 					if (t.timeout != INFINITY)
 						b.append("[< ").append(t.timeout).append("]");
 					b.append("\"];\n");
 				}
 		}
 		return b.append("};").toString();
 	}
 	
 	public static <C> NodeFactory<C> getReflectNodeBuilder(final Class<C> dummy) {
 		return getReflectNodeBuilder(TimedAutomata.class.getClassLoader(), dummy);
 	}
 	
 	public static <C> NodeFactory<C> getReflectNodeBuilder(final String searchPrefix, final Class<C> dummy) {
 		return getReflectNodeBuilder(new AutoQualifiedClassLoader(searchPrefix), dummy);
 	}
 	
 	public static <C> NodeFactory<C> getReflectNodeBuilder(final ClassLoader loader, final Class<C> dummy) {
 		return new NodeFactory<C>() {
 			@SuppressWarnings("unchecked")
 			@Override
 			public Action<C> newState(String name, String type, String attr) {
 				try {
 					Class<?> clz = loader.loadClass(type);
 					Action<C> state = null;
 					if(type == null)
 						state = new StateAdapter<C>();
 					try {
 						state = (Action<C>) clz.getConstructor(String.class).newInstance(attr);
 					} catch (NoSuchMethodException e) {
 						state = (Action<C>) clz.getConstructor().newInstance();
 					}
 					return new NamedAction<C>(name,  state);
 				} catch (NoSuchMethodException | SecurityException
 						| ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
 					e.printStackTrace();
 				}
 				return null;
 			}
 			
 			@SuppressWarnings("unchecked")
 			@Override
 			public Predicate<C> newPredicate(String type) {
 				try {
 					return (Predicate<C>) loader.loadClass(type).getConstructor().newInstance();
 				} catch (NoSuchMethodException | SecurityException
 						| ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
 					e.printStackTrace();
 				}
 				return null;
 			}
 		};
 	}
 	
 	@Override
 	final public void nextState() {
 		C context = getContext();
 
 		boolean allexpired = true;
 		Action<C> timeoutTarget = null;
 		for(Transition trans: _transitions.get(_current)) {
 			int timeout = trans.timeout;
 			if(timeout == TIMEOUT)
 				timeoutTarget = trans.state;
 			else if (_currentTime < timeout || timeout == INFINITY) {
 				allexpired = false;
 				if(trans.predicate.isValid(context))
 					setState(trans.state, context);
 			}
 		}
 		if(allexpired && timeoutTarget != null)
 			setState(timeoutTarget, context);
 			
 	}
 
 	@Override
 	final public Action<C> getInitialState() {
 		return _initial;
 	}
 
 	@Override
 	final public void setInitialState(Action<C> initial) {
 		_initial = initial;
 	}
 
 	@Override
 	final public Action<C> getCurrentState() {
 		return _current;
 	}
 
 	@Override
 	final public Action<C>[] getStates() {
 		@SuppressWarnings("unchecked")
 		Action<C>[] a = new Action[_stateMap.size()]; 
 		return _stateMap.values().toArray(a);
 	}
 
 	@Override
 	final public void reset() {
 		_currentTime = 0;
 	}
 
 	@Override
 	final public void start() {
 		assert _current == null;
 		setState(_initial);
 	}
 
 	@Override
 	final public void restart() {
 		setState(_initial);
 	}
 	
 	@Override
 	final public void setState(Action<C> target) {
 		setState(target, getContext());
 	}
 
 	final public void setState(Action<C> target, C context) {
 		if(_current == target) {
 			target.eachAction(context, this);
 		} else {
 			_current.postAction(context, this);
 			_current = target;
 			_currentTime = 0;
 			_current.preAction(context, this);
 		}
 	}
 	
 	@Override
 	public Predicate<C>[] getPredicates() {
 		@SuppressWarnings("unchecked")
 		Predicate<C>[] a = new Predicate[_stateMap.size()]; 
 		return _transMap.values().toArray(a);
 	}
 	
 	BasicVisualizationServer<Action<C>, Predicate<C>> asPanel() {
 		DirectedGraph<Action<C>, Predicate<C>> g = asGraph();
 		Layout<Action<C>, Predicate<C>> layout = new FRLayout<>(g);
 		layout.setSize(new Dimension(300,300));
 		BasicVisualizationServer<Action<C>, Predicate<C>> vv = 
 				new BasicVisualizationServer<Action<C>, Predicate<C>>(layout);
 		vv.setPreferredSize(new Dimension(350,350)); //Sets the viewing area size
 		vv.getRenderContext().getPickedVertexState().pick(_initial, true);
 		for(Predicate<C> e: g.getEdges())
 			if(e instanceof DefaultTransition)
 				vv.getRenderContext().getPickedEdgeState().pick(e, true);
 		return vv;
 	}
 	
 	DirectedGraph<Action<C>, Predicate<C>> asGraph() {
 		DirectedGraph<Action<C>, Predicate<C>> sgv = new DirectedSparseGraph<Action<C>, Predicate<C>>();
 		for(Action<C> state: _stateMap.values())
 			sgv.addVertex(state);
 		for(Entry<Action<C>, List<TimedAutomata<C>.Transition>> edge: _transitions.entrySet()) {
 			Action<C> src = edge.getKey();
 			for(TimedAutomata<C>.Transition dst: edge.getValue())
 				if(dst.predicate != null)
 					sgv.addEdge(dst.predicate, src, dst.state);
 				else
 					sgv.addEdge(new DefaultTransition<C>(), src, dst.state);
 
 		}
 		return sgv;
 	}
 	
 	static class DefaultTransition<C> extends TransitionAdapter<C> {
 	}
 }
