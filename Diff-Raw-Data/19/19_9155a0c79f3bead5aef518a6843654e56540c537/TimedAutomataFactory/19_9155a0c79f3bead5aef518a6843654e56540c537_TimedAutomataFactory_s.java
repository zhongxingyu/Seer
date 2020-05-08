 package fr.labri.tima;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.jdom2.Document;
 import org.jdom2.Element;
 import org.jdom2.JDOMException;
 import org.jdom2.input.SAXBuilder;
 import org.jdom2.input.sax.XMLReaderXSDFactory;
 
 import fr.labri.AutoQualifiedClassLoader;
 import fr.labri.tima.ITimedAutomata.Action;
 import fr.labri.tima.ITimedAutomata.ContextProvider;
 import fr.labri.tima.ITimedAutomata.Executor;
 import fr.labri.tima.ITimedAutomata.NodeFactory;
 import fr.labri.tima.ITimedAutomata.Predicate;
 import fr.labri.tima.ITimedAutomata.Spawner;
 import fr.labri.tima.ITimedAutomata.State;
 
 public class TimedAutomataFactory<C> {
 	public static final String AUTOMATA_TAG = "timedautomata";
 	public static final String AUTOMATA_NAME_TAG = "name";
 
 	public static final String STATE_TAG = "state";
 	public static final String TRANSITION_TAG = "path";
 	public static final String TIMEOUT_TAG = "timeout";
 	
 	public static final String STATE_URGENT_TAG = "urgent";
 	public static final String STATE_NAME_TAG = "name";
 	public static final String STATE_INITIAL_TAG = "initial";
 	public static final String STATE_TERMINAL_TAG = "terminal";
 
 	public static final String ACTION_TAG = "action";
 	public static final String ACTION_NAME_TAG = "type";
 	public static final String ACTION_ATTR_TAG = "attr";
 
 	public static final String SPAWN_TAG = "spawn";
 	public static final String SPAWN_NAME_TAG = "name";
 	public static final String SPAWN_ACTION_TAG = "type";
 	public static final String SPAWN_ATTR_TAG = "attr";
 	private static final String TERMINATE_TAG = "terminate";
 
 	
 	public static final String TRANSITION_TARGET_TAG = "to";
 	public static final String TRANSITION_PREDICATE_TAG = "guard";
 	public static final String TRANSITION_TIMEOUT_TAG = "timeout";
 	public static final String TRANSITION_ATTR_TAG = "attr";
 	
//	public static final String XMLNS="http://www.w3.org/namespace/";
 	public static final String XMLNS_XSI = "http://www.w3.org/2001/XMLSchema-instance";
	public static final String XSI_LOCATION = "http://www.labri.fr/~fmoranda/xsd/ta.xsd";
 	
 	private final NodeFactory<C> _factory;
 	
 	private final Map<String, Action<C>> _actionMap = new HashMap<String, Action<C>>();
 	private final Map<String, Predicate<C>> _predicateMap = new HashMap<String, Predicate<C>>();
 	
 	private final List<ITimedAutomata<C>> _masters = new ArrayList<>();
 
 	public TimedAutomataFactory(NodeFactory<C> factory) {
 		_factory = factory;
 	}
 
 	public Document parseXML(InputStream stream, boolean validate) throws JDOMException, IOException {
		// FIXME if validate == true, it does not work :)
		SAXBuilder sxb = new SAXBuilder(validate ? new XMLReaderXSDFactory(TimedAutomata.class.getResource("ta.xsd")) : null);
 
 		Document document = sxb.build(stream);
 		return document;
 	}
 	
 	public List<TimedAutomata<C>> loadXML(InputStream stream) throws JDOMException, IOException {
 		return loadXML(parseXML(stream, false));
 	}
 	
 	public List<TimedAutomata<C>> loadXML(InputStream stream, boolean validate) throws JDOMException, IOException {
 		return loadXML(parseXML(stream, validate));
 	}
 	
 	public List<TimedAutomata<C>> loadXML(Document root) throws JDOMException, IOException {
 		Map<String, Element> autosMap = new HashMap<>();
 		Map<Element, TimedAutomata<C>> autos = new HashMap<>();
 
 		resolveAutomataName(root.getRootElement(), autosMap, autos);
 		for(Element auto: autosMap.values())
 			loadAutomata(auto, autosMap, autos);
 		
 		List<TimedAutomata<C>> res = new ArrayList<>();
 		res.addAll(autos.values());
 		_masters.add(autos.get(root.getRootElement()));
 		
 		return res;
 	}
 
 	final public Executor<C> getExecutor(ContextProvider<C> provider) {
 		return getExecutor(provider, true);
 	}
 
 	public Executor<C> getExecutor(ContextProvider<C> provider, boolean compiled) {
 		Executor<C> exec = new BasicExecutor<>(provider);
 		for(ITimedAutomata<C> master: _masters) {
 			if(compiled)
 				master = master.compile();
 			exec.start(master, null);
 		}
 		return exec;
 	}
 	
 	protected void resolveAutomataName(Element auto, Map<String, Element> autosMap, Map<Element, TimedAutomata<C>> autos) {
 		String name = auto.getAttributeValue(AUTOMATA_NAME_TAG);
 		
 		if(name == null)
 			throw new RuntimeException("Automata has no name: " + auto.toString());
 		
 		if(autos.containsKey(name))
 			throw new RuntimeException("There is more than one automata with the name: " + name);
 
 		autosMap.put(name, auto);
 		autos.put(auto, new TimedAutomata<C>());
 		
 		for(Element sub: auto.getChildren(AUTOMATA_TAG))
 			resolveAutomataName(sub, autosMap, autos);
 	}
 
 	protected ITimedAutomata<C> loadAutomata(Element auto, Map<String, Element> autosMap, Map<Element, TimedAutomata<C>> autos) throws JDOMException, IOException {
 		
 		Map<String, Element> stateMap = new HashMap<String, Element>();
 		Map<Element, Element> spawnMap = new HashMap<Element, Element>();
 		resolveStates(auto, autosMap, stateMap, spawnMap);
 		Map<Element, Element> transMap = new HashMap<Element, Element>();
 		resolveTransitions(auto, stateMap, transMap);
 		
 		TimedAutomata<C> cAuto = autos.get(auto);
 		Map<Element, State<C>> states = new HashMap<>();
 		buildStates(cAuto, stateMap, states, spawnMap, autos);
 		buildTransitions(cAuto, states, transMap);
 		
 		return cAuto;
 	}
 	
 	protected void buildTransitions(TimedAutomata<C> auto, Map<Element, State<C>> stateMap, Map<Element, Element> transMap) {
 		for(Entry<Element, State<C>> entry: stateMap.entrySet()){
 			Element srcElt = entry.getKey();
 			State<C> src = entry.getValue();
 			
 			for(Element trans: srcElt.getChildren(TRANSITION_TAG)) {
 				State<C> dest = stateMap.get(transMap.get(trans));
 				
 				String pred = trans.getAttributeValue(TRANSITION_PREDICATE_TAG);
 				String timeoutval = trans.getAttributeValue(TRANSITION_TIMEOUT_TAG);
 				String attr = trans.getAttributeValue(TRANSITION_ATTR_TAG);
 				int timeout = (timeoutval == null) ? TimedAutomata.INFINITY : Integer.parseInt(timeoutval);
 				
 				Predicate<C> predicate = getPredicate(pred, attr);
 				if(predicate == null)
 					throw new RuntimeException("Unable to create predicate : " + pred +"(" + attr+")");
 				auto.addTransition(src, timeout, predicate, dest);
 			}
 			
 			Element timeout = srcElt.getChild(TIMEOUT_TAG);
 			if(timeout != null) {
 				State<C> dest = stateMap.get(transMap.get(timeout));
 				auto.addDefaultTransition(src, dest);
 			} 
 		}
 	}
 
 	protected void buildStates(TimedAutomata<C> auto, Map<String, Element> stateMap, Map<Element, State<C>> states, Map<Element, Element> spawnMap, Map<Element, TimedAutomata<C>> autos) {
 		for(Entry<String, Element> entry: stateMap.entrySet()){
 			boolean isTerm = false, isSpawn = false;
 			Element state = entry.getValue();
 			ArrayList<Spawn> spawns = new ArrayList<>();
 			ArrayList<Action<C>> acts = new ArrayList<>();
 			
 			for(Element act: state.getChildren()) {
 				if(isTerm)
 					throw new RuntimeException("Unreachable code, actions after terminate in " + entry.getKey());
 				String aName = act.getName();
 				if(ACTION_TAG.equalsIgnoreCase(aName)) { // TODO remove this switch case
 					Action<C> a = getAction(act.getAttributeValue(ACTION_NAME_TAG), act.getAttributeValue(ACTION_ATTR_TAG));
 					if(a == null)
 						throw new RuntimeException("Unable to create action : " + act.getAttributeValue(ACTION_NAME_TAG) +"(" + act.getAttributeValue(ACTION_ATTR_TAG)+")");
 					acts.add(a);
 				} else if(SPAWN_TAG.equalsIgnoreCase(aName)) {
 					spawns.add(newSpawnAction(act, spawnMap, autos));
 					isSpawn = true;
 				} else if(TERMINATE_TAG.equalsIgnoreCase(aName)) {
 					isTerm = true;
 				} else if(TRANSITION_TAG.equalsIgnoreCase(aName)) {
 				} else if(TIMEOUT_TAG.equalsIgnoreCase(aName)) {
 				} else
 					throw new RuntimeException("Unknwown tag: " + aName);
 			}
 			
 			
 			String dfltAct = state.getAttributeValue(ACTION_TAG);
 			if(dfltAct != null) {
 				Action<C> a = getAction(dfltAct, state.getAttributeValue(ACTION_ATTR_TAG));
				if(a == null)
					throw new RuntimeException("Unable to create default action : " + state.getAttributeValue(ACTION_NAME_TAG) +"(" + state.getAttributeValue(ACTION_ATTR_TAG)+")");
				acts.add(a);
 			} else if(state.getAttributeValue(ACTION_ATTR_TAG) != null)
 				throw new RuntimeException("Attribue without action in state: "+ entry.getKey());
 			
 			if ("true".equalsIgnoreCase(state.getAttributeValue(TimedAutomataFactory.STATE_TERMINAL_TAG)))
 				if(isTerm)
 					throw new RuntimeException("More than one terminate in " + entry.getKey());
 				else {
 					isTerm = true;
 				}
 
 			boolean isInitial = "true".equalsIgnoreCase(state.getAttributeValue(TimedAutomataFactory.STATE_INITIAL_TAG));
 
 			int modifiers = ("true".equalsIgnoreCase(state.getAttributeValue(TimedAutomataFactory.STATE_URGENT_TAG)) ? ITimedAutomata.URGENT : 0)
 					| (isInitial ? ITimedAutomata.INITIAL : 0)
 					| (isSpawn ? ITimedAutomata.SPAWN : 0)
 					| (isTerm ? ITimedAutomata.TERMINATE : 0);
 			
 			State<C> st = newState(entry.getKey(), acts, spawns, modifiers);
 			states.put(state, st);
 			if(isInitial)
 				auto.setInitial(st);
 		}
 	}
 
 	protected State<C> newState(final String name, final ArrayList<Action<C>> actions, final ArrayList<Spawn> automatas, final int modifiers) {
 		return new State<C>() {
 			@Override
 			public String getName() {
 				return name;
 			}
 
 			@Override
 			public List<Action<C>> getActions() {
 				return actions;
 			}
 			
 			@Override
 			public List<ITimedAutomata<C>> getSpawnableAutomatas() {
 				return null;
 			}
 
 			@Override
 			public int getModifier() {
 				return modifiers;
 			}
 
 			@Override
 			public void preAction(C context, ITimedAutomata.Executor<C> executor, String key) {
 				if(hasModifier(ITimedAutomata.SPAWN, modifiers))
 					for (Spawn spawn: automatas)
 						spawn.start(executor, context, key);
 				
 				for (Action<C> action : getActions())
 					action.preAction(context, key);
 			}
 
 			@Override
 			public void eachAction(C context, ITimedAutomata.Executor<C> executor, String key) {
 				for (Action<C> action : getActions())
 					action.eachAction(context, key);
 			}
 
 			@Override
 			public void postAction(C context, ITimedAutomata.Executor<C> executor, String key) {
 				for (Action<C> action : getActions())
 					action.postAction(context, key);
 			}
 		};
 	}
 	
 	protected Spawn newSpawnAction(Element source, Map<Element, Element> spawnMap, Map<Element, TimedAutomata<C>> autos) {
 		String spawnerName = source.getAttributeValue(SPAWN_ACTION_TAG);
 		String spawnerAttr = source.getAttributeValue(SPAWN_ATTR_TAG);
 		// TODO write a getSpan Method
 		final Spawner<C> spawner = spawnerName == null ? null : _factory.newSpawner(spawnerName, spawnerAttr);
 		TimedAutomata<C> target = autos.get(spawnMap.get(source));
 		return new Spawn(target, spawner);
 	}
 	
 	protected void resolveTransitions(Element root, Map<String, Element> stateMap, Map<Element, Element> transMap) {
 		for(Element state: root.getChildren(STATE_TAG)){
 			
 			boolean hasTimeout = false;
 			for(Element trans: state.getChildren(TIMEOUT_TAG)) {
 				Element target = stateMap.get(trans.getAttributeValue(TRANSITION_TARGET_TAG));
 				
 				if(hasTimeout)
 					throw new RuntimeException("State '"+target+"' has more than one timeout");
 				if(target == null) 
 					throw new RuntimeException("Timeout target does not exists in '" + root.getAttributeValue(AUTOMATA_NAME_TAG) + "/"+state+"': " + trans.getAttributeValue(TRANSITION_TARGET_TAG));
 				transMap.put(trans, target);
 				hasTimeout = true;
 			}
 			
 			for(Element trans: state.getChildren(TRANSITION_TAG)) {
 				Element target = stateMap.get(trans.getAttributeValue(TRANSITION_TARGET_TAG));
 				if(hasTimeout) {
 					String timeoutval = trans.getAttributeValue(TRANSITION_TIMEOUT_TAG);
 					if (timeoutval != null && Integer.parseInt(timeoutval) == TimedAutomata.INFINITY)
 						throw new RuntimeException("Cannot mix timeout and infinite guars in: "+ state);
 				}
 				if(target == null)
 					throw new RuntimeException("Target does not exists in '" + root.getAttributeValue(AUTOMATA_NAME_TAG) + "': " + trans.getAttributeValue(TRANSITION_TARGET_TAG));
 				transMap.put(trans, target);
 			}
 		}
 	}
 
 	protected Element resolveStates(Element root, Map<String, Element> autos, Map<String, Element> stateMap, Map<Element, Element> spawnMap) throws JDOMException {
 		Element initial = null;
 		
 		for(Element state: root.getChildren(STATE_TAG)){
 			String name = state.getAttributeValue(STATE_NAME_TAG);
 			if(stateMap.containsKey(name))
 				throw new JDOMException("Node name is not unique: "+ name);
 			stateMap.put(name, state);
 			
 			if("true".equalsIgnoreCase(state.getAttributeValue(TimedAutomataFactory.STATE_INITIAL_TAG))) {
 				if(initial != null)
 					throw new RuntimeException("More than one initial state in " + root.getAttributeValue(AUTOMATA_NAME_TAG) + ": '"+initial+"', '"+state+"'");
 				initial = state;
 			}
 			
 			for(Element spawn: root.getChildren(SPAWN_TAG)){
 				String tName = spawn.getAttributeValue(SPAWN_NAME_TAG);
 				if (tName == null)
 					throw new RuntimeException("Spawning unamed automata in " + root.getAttributeValue(AUTOMATA_NAME_TAG) + "/" + name);
 				
 				Element target = autos.get(tName);
 				if (target == null)
 					throw new RuntimeException("Spawning unknwon automata '"+ tName + "' in " + root.getAttributeValue(AUTOMATA_NAME_TAG) + "/" + name);
 				spawnMap.put(spawn, target);
 			}
 		}
 		if(initial == null)
 			throw new RuntimeException(root.getAttributeValue(AUTOMATA_NAME_TAG) + " automata has no initial state");
 		return initial;
 	}
 	
 	protected Action<C> getAction(final String type, final String attr) {
 		String name = type + ((attr == null ) ? "" : (":"+attr)); 
 		if(_actionMap.containsKey(name))
 			return _actionMap.get(name);
 		Action<C> act = _factory.newAction(type, attr);
 		_actionMap.put(name, act);
 		return act;
 	}
 	
 	public static final boolean hasModifier(int modifiers, int modifier) {
 		return (modifiers & modifier) > 0;
 	}
 	
 	protected Predicate<C> getPredicate(String type, String attr) {
 		String name = type + ((attr == null ) ? "" : (":"+attr));
 		if(_predicateMap.containsKey(name))
 			return _predicateMap.get(name);
 		Predicate<C> t = _factory.newPredicate(name, attr);
 		_predicateMap.put(name, t);
 		return t;
 	}
 	
 	protected int getModifierFromNode(Element state) {
 		String name = state.getAttributeValue(TimedAutomataFactory.STATE_NAME_TAG);
 		return ("true".equalsIgnoreCase(state.getAttributeValue(TimedAutomataFactory.STATE_URGENT_TAG)) ? ITimedAutomata.URGENT : 0)
 				| ("true".equalsIgnoreCase(state.getAttributeValue(TimedAutomataFactory.STATE_INITIAL_TAG)) ? ITimedAutomata.INITIAL : 0)
 				| (("stop".equalsIgnoreCase(name) || "terminate".equalsIgnoreCase(name)) ? ITimedAutomata.TERMINATE : 0)
 				| (state.getChild(TimedAutomataFactory.SPAWN_TAG) != null ? ITimedAutomata.SPAWN : 0) ;
 	}
 	
 	public static <C> NodeFactory<C> getReflectNodeBuilder(final Class<C> dummy) {
 		return getReflectNodeBuilder(TimedAutomata.class.getClassLoader(), dummy);
 	}
 	
 	public static <C> NodeFactory<C> getReflectNodeBuilder(final String searchPrefix, final Class<C> dummy) {
 		return getReflectNodeBuilder(new AutoQualifiedClassLoader(searchPrefix), dummy);
 	}
 	
 	public static <C> NodeFactory<C> getReflectNodeBuilder(final ClassLoader loader, final Class<C> dummy) {
 		return new NodeFactory<C>() {
 			@Override
 			public Action<C> newAction(String type, String attr) {
 				return newInstance(type, attr);
 			}
 			
 			@Override
 			public Predicate<C> newPredicate(String type, String attr) {
 				return newInstance(type, attr);
 			}
 			
 			@Override
 			public Spawner<C> newSpawner(String type, String attr) {
 				return newInstance(type, attr);
 			}
 			
 			@SuppressWarnings("unchecked")
 			public <T> T newInstance(String type, String attr) {
 				try {
 					Class<?> clz = loader.loadClass(type);
 					T state = null;
 					if(type == null)
 						return null;
 					try {
 						state = (T) clz.getConstructor(String.class).newInstance(attr);
 					} catch (NoSuchMethodException e) {
 						state = (T) clz.getConstructor().newInstance();
 					}
 					return state;
 				} catch (NoSuchMethodException | SecurityException
 						| ClassNotFoundException | InstantiationException
 						| IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
 //					e.printStackTrace();
 				}
 				return null;
 			}
 		};
 	}
 	
 	protected class Spawn { // Remove this class
 		private ITimedAutomata<C> _auto;
 		private Spawner<C> _spawner;
 		
 		Spawn(ITimedAutomata<C> auto, Spawner<C> spawner) {
 			_auto = auto;
 		}
 		
 		public void start(Executor<C> executor, C context, String parentKey) {
 			executor.start(_auto, (_spawner == null) ? null : _spawner.getSpawnerKey(context, parentKey));
 		}
 
 		public ITimedAutomata<C> getTargetAutomata() {
 			return _auto;
 		}
 	}
 }
