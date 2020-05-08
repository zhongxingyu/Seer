 package ai.hsm;
 
 import java.util.LinkedList;
 
 import ai.hsm.HSMBase.UpdateResult;
 
 public class HSMTest {
 	static final LinkedList<Action> NO_ACTION = new LinkedList<Action>();
 	
 	static class TestAction extends Action {
 		public String actionText;
 		
 		public TestAction(String actionText) {
 			this.actionText = actionText;
 		}
 		
 		@Override 
 		public void act() {
 			System.out.println(actionText);
 		}
 	}
 	
 	static class TestState extends SubMachineState {
 		public Action action;
 		
 		public TestState(String actionText) {
 			this.action = new TestAction(actionText);
 		}
 		
 		@Override
 		public LinkedList<Action> getAction() {
 			LinkedList<Action> result = new LinkedList<Action>();
 			result.add(this.action);
 			return result;
 		}
 		
 		@Override
 		public LinkedList<Action> getEntryAction() {
 			return NO_ACTION;
 		}
 		
 		@Override
 		public LinkedList<Action> getExitAction() {
 			return NO_ACTION;
 		}
 	}
 	
 	static int TEST_VALUE = 0;
 	
 	public static void main(String[] args) {
 		SubMachineState root = new SubMachineState();
 		
		SubMachineState state1 = new SubMachineState();
 		TestState state2 = new TestState("State 2");
 		
 		Transition transition1 = new Transition(new TestAction("Transition 1"), new Condition() {
 			@Override
 			public boolean test() {
 				return TEST_VALUE == 1;
 			}
 		});
 		
 		Transition transition3 = new Transition(new TestAction("Transition 3"), new Condition() {
 
 			@Override
 			public boolean test() {
 				return TEST_VALUE == 3;
 			}
 			
 		});
 		
 		bindTransition(state1, state2, transition1);
 		bindTransition(state2, state1, transition3);
 		
 		root.addState(state1);
 		root.addState(state2);
 		
 		root.initialState = root.currentState = state1;
 		
 		TestState internal1 = new TestState("Internal 1");
 		TestState internal2 = new TestState("Internal 2");
 		
 		Transition transition2 = new Transition(new TestAction("Transition 2"), new Condition() {
 			@Override
 			public boolean test() {
 				return TEST_VALUE == 2;
 			}
 		});
 		
 		bindTransition(internal1, internal2, transition2);
 		
 		state1.addState(internal1);
 		state2.addState(internal2);
 		
 		state1.initialState = state1.currentState = internal1;
 		
 		
 		UpdateResult result = root.update();
 		
 		for (Action action : result.actions) {
 			action.act();
 		}
 		
 		TEST_VALUE = 2;
 		
 		result = root.update();
 		
 		for (Action action : result.actions) {
 			action.act();
 		}
 		
 		result = root.update();
 		
 		for (Action action : result.actions) {
 			action.act();
 		}
 		
 		TEST_VALUE = 1;
 		
 		result = root.update();
 		
 		for (Action action : result.actions) {
 			action.act();
 		}
 		
 		result = root.update();
 		
 		for (Action action : result.actions) {
 			action.act();
 		}
 		
 		TEST_VALUE = 3;
 		
 		result = root.update();
 		
 		for (Action action : result.actions) {
 			action.act();
 		}
 		
 		result = root.update();
 		
 		for (Action action : result.actions) {
 			action.act();
 		}
 		
 	}
 	
 	static void bindTransition(SubMachineState src, SubMachineState target, Transition transition) {
 		src.addTransition(transition);
 		transition.source = src;
 		transition.target = target;
 	}
 
 }
