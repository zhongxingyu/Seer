 package name.kazennikov.annotations.patterns;
 
 import gnu.trove.list.array.TIntArrayList;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import name.kazennikov.annotations.Annotation;
 import name.kazennikov.annotations.AnnotationEngineException;
 import name.kazennikov.annotations.AnnotationList;
 import name.kazennikov.annotations.Annotator;
 import name.kazennikov.annotations.Document;
 import name.kazennikov.annotations.annotators.BasicTokenizer;
 import name.kazennikov.annotations.fsm.JapePlusFSM;
 import name.kazennikov.annotations.fsm.JapePlusFSM.State;
 import name.kazennikov.annotations.fsm.JapePlusFSM.Transition;
 import name.kazennikov.annotations.fsm.JapePlusFSM.TypeMatcher;
 import name.kazennikov.logger.Logger;
 
 import org.apache.log4j.BasicConfigurator;
 
 import com.google.common.base.Predicate;
 
 public class RecursiveJapeAnnotator implements Annotator {
 	private static final Logger logger = Logger.getLogger();
 
 	public static class FSMInstance {
 		//Map<String, AnnotationList> bindings;
 		List<AnnotationList> stack;
 		List<String> keys;
 		List<AnnotationList> values;
 		int position = 0;
 		Rule rule;
 
 		public void init() {
 			//bindings = new HashMap<>();
 			keys = new ArrayList<>();
 			values = new ArrayList<>();
 			stack = new ArrayList<>();
 		}
 
 		public FSMInstance copy() {
 			FSMInstance copy = new FSMInstance();
 			//copy.bindings = new HashMap<String, AnnotationList>();
 			copy.keys = new ArrayList<>(this.keys.size());
 			copy.values = new ArrayList<>(this.values.size());
 			copy.stack = new ArrayList<>();
 			copy.position = position;
 
 			for(AnnotationList l : stack) {
 				copy.stack.add(l.copy());
 			}
 			
 			for(int i = 0; i < keys.size(); i++) {
 				copy.keys.add(keys.get(i));
 				copy.values.add(values.get(i).copy());
 			}
 			
 
 //			for(Map.Entry<String, AnnotationList> e : bindings.entrySet()) {
 //				copy.bindings.put(e.getKey(), e.getValue().copy());
 //			}
 
 
 
 			return copy;
 		}
 
 		public void push() {
 			stack.add(new AnnotationList());
 		}
 
 		public void addMatching(Annotation a) {
 			for(AnnotationList l : stack) {
 				l.add(a);
 			}
 		}
 
 		public void pop(String groupName) {
 			AnnotationList l = stack.get(stack.size() - 1);
 			stack.remove(stack.size() - 1);
 			//bindings.put(groupName, l);
 			keys.add(groupName);
 			values.add(l);
 		}
 
 		public static FSMInstance newInstance() {
 			FSMInstance inst = new FSMInstance();
 			inst.init();
 			return inst;
 		}
 
 		public Map<String, AnnotationList> bindings() {
 			Map<String, AnnotationList> map = new HashMap<String, AnnotationList>();
 			for(int i = 0; i < keys.size(); i++) {
 				map.put(keys.get(i), values.get(i));
 			}
 			return map;
 		}
 
 
 	}
 
 
 	public static class Matcher {
 		AnnotationList input;
 		int[] nextAnnotationIndex;
 		Document doc;
 		Phase phase;
 		
 		
 		List<FSMInstance> instances = new ArrayList<>();
 
 		public Matcher(Document doc, final Phase phase) {
 			this.phase = phase;
 			this.doc = doc;
 			input = doc.get(new Predicate<Annotation>() {
 
 				@Override
 				public boolean apply(Annotation input) {
 					return phase.input.contains(input.getType());
 				}
 
 			}); 
 
 			input.sort();		
 			nextAnnotationIndex = JapeEngineUtils.computeFollowingAnnotationIndex(input);
 		}
 
 
 		public void execute() {
 			int index = 0;
 
 			State state = phase.fsm.getStart();
 
 			while(index < input.size()) {
 				
 				boolean res = tryExecute(index, state, FSMInstance.newInstance());
 				
 				// if something matched
 				if(!instances.isEmpty()) {
 					index = applyRules(index);
 					instances.clear();
 				} else {
 					index = skipToNextIndex(index);
 				}
 				
 				if(index < 0)
 					break;
 				
 			}
 		}
 		
 		public int execOnce() {
 			FSMInstance inst = instances.get(0);
 			for(RHS rhs : inst.rule.rhs()) {
 				rhs.execute(doc, input, inst.bindings());
 			}
 
 			return -1;
 		}
 		
 		public int execFirst() {
 			FSMInstance inst = instances.get(0);
 			for(RHS rhs : inst.rule.rhs()) {
 				rhs.execute(doc, input, inst.bindings());
 			}
 
 			return skipToNextIndex(inst.position);
 		}
 		
 		public int execAll(int startIndex) {
 			for(int i = 0; i < instances.size(); i++) {
 				FSMInstance inst = instances.get(i);
 				for(RHS rhs : inst.rule.rhs()) {
 					rhs.execute(doc, input, inst.bindings());
 				}
 			}
 			
 			return skipToNextIndex(startIndex);
 		}
 		
 		public int execBrill() {
 			int maxPos = Integer.MIN_VALUE;
 			for(int i = 0; i < instances.size(); i++) {
 				FSMInstance inst = instances.get(i);
 				for(RHS rhs : inst.rule.rhs()) {
 					rhs.execute(doc, input, inst.bindings());
 				}
 
 				maxPos = Math.max(maxPos, inst.position);
 			}
 			
 			return skipToNextIndex(maxPos);
 		}
 		
 		public int execAppelt() {
 			Collections.sort(instances, new Comparator<FSMInstance>() {
 
 				@Override
 				public int compare(FSMInstance o1, FSMInstance o2) {
 					int res = o2.position - o1.position;
 					if(res != 0)
 						return res;
 					
 					res = o2.rule.getPriority() - o1.rule.getPriority();
 					if(res != 0)
 						return res;
 					
 					res = o2.rule.number - o1.rule.number;
 								
 					return res;
 				}
 			});
 			
 			for(RHS rhs : instances.get(0).rule.rhs()) {
 				rhs.execute(doc, input, instances.get(0).bindings());
 			}
 
 			return skipToNextIndex(instances.get(0).position);
 		}
 
 		
 		public int applyRules(int startIndex) {
 			switch(phase.mode) {
 			case ONCE:
 				return execOnce();
 			case FIRST:
 				return execFirst();
 			case ALL:
 				return execAll(startIndex);
 			case BRILL:
 				return execBrill();
 			case APPELT:
 				return execAppelt();
 			}
 			
 			return -1;
 		}
 		
 
 
 		/**
 		 * Попробовать отматчить fsm начиная с данной позиции
 		 * @param index
 		 * @return true, if we need to continue
 		 */
 		public boolean tryExecute(int index, State state, FSMInstance instance) {
 			if(state.isFinal()) {
 				for(Rule r : state.getRules()) {
 					FSMInstance inst = instance.copy();
 					inst.rule = r;	
 					instances.add(inst);
 				}
 				
 				if(phase.mode == MatchMode.FIRST || phase.mode == MatchMode.ONCE)
 					return false;
 			}
 			
 			int nextIndex = skipToNextIndex(index);
 
 			for(Transition t : state.getTransitions()) {
 				int type = t.getType();
 				boolean res;
 				
 				if(type == JapePlusFSM.GROUP_START) {
 					FSMInstance inst = instance.copy();
 					inst.push();
 					res = tryExecute(index, t.getDest(), inst);
 				} else if(type < 0) { // group end
 					String groupName = phase.fsm.getGroupName(-type - 1);
 					FSMInstance inst = instance.copy();
 					inst.pop(groupName);
 					res = tryExecute(index, t.getDest(), inst);
 				} else {
 					res = tryMatch(index, nextIndex, t, instance);
 				}
 				
 				if(!res)
 					return res;
 
 			}
 
 			return true;
 		}
 
 		public boolean tryMatch(int startIndex, int endIndex, Transition t, FSMInstance instance) {
 			List<TypeMatcher> matchers = t.getMatchers();
 			int[] matchedAnnotations = new int[matchers.size()];
 			return tryConstraintMatch(startIndex, endIndex, matchers, 0, matchedAnnotations, t.getDest(), instance);
 		}
 
 		public boolean tryConstraintMatch(int startIndex, int endIndex, 
 				List<TypeMatcher> typeMatchers, int tmIndex, int[] matched,
 				State dest, FSMInstance instance) {
 			
 			if(startIndex >= input.size())
 				return true;
 
 			if(tmIndex == typeMatchers.size()) {
 				FSMInstance inst = instance.copy();
 				inst.position = startIndex;
 				int nextIndex = Integer.MIN_VALUE;
 				for(int annotIndex : matched) {
 					inst.addMatching(input.get(annotIndex));
 					nextIndex = Math.max(nextIndex, nextAnnotationIndex[annotIndex]);
 				}
 				
 				return tryExecute(nextIndex, dest, inst);
 			} else {
 				TypeMatcher m = typeMatchers.get(tmIndex);
 				TIntArrayList matchersIndex = m.getMatchers();
 				TIntArrayList flags = m.getFlags();
 
 				for(int annotIndex = startIndex; annotIndex < endIndex; annotIndex++) {
 					Annotation a = input.get(annotIndex);
					boolean res = true;
 					for(int matcherIndex = 0; matcherIndex < matchersIndex.size(); matcherIndex++) {
 						AnnotationMatcher matcher = phase.fsm.getMatcher(matchersIndex.get(matcherIndex));
 						int flag = flags.get(matcherIndex);
						boolean res0 = matcher.match(a);
 
 						if(flag == 1)
							res0 = !res0;
						
						if(!res0) {
							res = false;
							break;
 						}
 					}
					// if all matchers
					if(res) {
						matched[tmIndex] = annotIndex;
						tryConstraintMatch(startIndex, endIndex, typeMatchers, tmIndex + 1, matched, dest, instance);
					}

 				}				
 			}
 			
 			return true;
 		}
 
 
 		public int skipToNextIndex(int index) {
 			if(index >= input.size())
 				return index;
 			return nextAnnotationIndex[index];
 		}
 	}
 
 	public static void main(String[] args) throws Exception {
 		BasicConfigurator.configure();
 		Phase fsm = JapeEngineUtils.compilePhase(new File("jape/parser/4.jape"));
 		BasicTokenizer t = new BasicTokenizer();
 		t.setSeparator(",.!?()[]\"'$%^&*#{}\\|/-");
 		Document d = new Document("doc", "this--is table.");
 		t.annotate(d);
 		Matcher m = new Matcher(d, fsm);
 		m.execute();
 
 		System.out.printf("Done%n");
 	}
 	
 	File japeFile;
 	Phase phase;
 	
 	public File getJapeFile() {
 		return japeFile;
 	}
 
 	public void setJapeFile(File japeFile) {
 		this.japeFile = japeFile;
 	}
 
 	
 	public void init() {
 		try {
 			phase = JapeEngineUtils.compilePhase(japeFile);
 		} catch(Exception e) {
 			logger.warn(e);
 			throw new AnnotationEngineException(e);
 		}
 	}
 	
 	
 	
 
 	@Override
 	public boolean isApplicable(Document doc) {
 		return true;
 	}
 
 	@Override
 	public void annotate(Document doc) {
 		Matcher m = new Matcher(doc, phase);
 		m.execute();
 		
 	}
 
 }
