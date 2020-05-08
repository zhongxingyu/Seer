 package events;
 
 import java.util.List;
 import java.util.HashMap;
 import java.util.Stack;
 
 import sourceparser.SourceParser;
 
 import log.Logger;
 
 import com.sun.jdi.AbsentInformationException;
 import com.sun.jdi.Field;
 import com.sun.jdi.IncompatibleThreadStateException;
 import com.sun.jdi.LocalVariable;
 import com.sun.jdi.Location;
 import com.sun.jdi.ObjectReference;
 import com.sun.jdi.ReferenceType;
 import com.sun.jdi.StackFrame;
 import com.sun.jdi.ThreadReference;
 import com.sun.jdi.Value;
 import com.sun.jdi.VirtualMachine;
 import com.sun.jdi.event.Event;
 import com.sun.jdi.event.StepEvent;
 import com.sun.jdi.request.EventRequestManager;
 import com.sun.jdi.request.StepRequest;
 
 public class StepEventHandler implements IEventHandler {
 
 	private SourceParser sourceParser;
 	private Logger logger;
 	private VirtualMachine vm;
 	
 	private ThreadsMap threads;
 	
 	public StepEventHandler(SourceParser sourceParser, Logger logger, VirtualMachine vm) {
 
 		this.sourceParser = sourceParser;
 		this.logger = logger;
 		this.vm = vm;
 		
 		this.threads = new ThreadsMap();
 		
 		requestEvents();
 	}
 	
 	private void requestEvents() {
 		EventRequestManager erm = vm.eventRequestManager();
 		
 		for (ThreadReference ref : vm.allThreads()) {
 			StepRequest request = erm.createStepRequest(ref, StepRequest.STEP_LINE, StepRequest.STEP_INTO);
 			request.addClassFilter("client.*");
 			request.enable();
 		}
 	}
 	
 	@Override
 	public int handle(Event event) {
 		StepEvent stepEvent = (StepEvent)event;
 		
 		Location location = stepEvent.location();
 		
 		String sourcePath;
 		int lineNumber;
 		String methodName; 
 		
 		try {
 			sourcePath = location.sourcePath();
 			lineNumber = location.lineNumber();
 			methodName = location.method().name();
 		} catch (AbsentInformationException e) {
 			// No location information available for this step
 			
 			return 0;
 		}
 			
 		for (ThreadReference thread : vm.allThreads()) {	
 			String threadName = thread.name();
 			
 			if (threads.containsKey(threadName) == false) {
 				VariablesStack variablesStack = new VariablesStack();
 				variablesStack.setLineNumber(lineNumber);
 				
 				threads.put(threadName, variablesStack);
 			}
 			
 			int frameCount;
 			
 			try {
 				frameCount = thread.frameCount();
 			} catch (IncompatibleThreadStateException e) {
 				// No stack variables available for this thread
 				
 				continue;
 			}
 			
 			VariablesStack variablesStack = threads.get(threadName);
 			
 			if (variablesStack.size() < frameCount) {
 				// Stack has grown so make a new VariablesMap
 				
 				variablesStack.push(new VariablesMap());
 			} else if (variablesStack.size() > frameCount) {
 				// Stack has shrunk so pop a VariablesMap
 				
 				variablesStack.pop();
 			}
 			
 			if (frameCount > 0) {				
 				VariablesMap variablesMap = variablesStack.peek();
 				
 				try {
 					StackFrame stackFrame = thread.frame(0);
 					
 					for (LocalVariable variable : stackFrame.visibleVariables()) {
 						if (variablesMap.containsKey(variable.name())) {
 							// Variable already existed
 							
 							Value oldValue = variablesMap.get(variable.name());
 							Value newValue = stackFrame.getValue(variable);
 							
 							if (oldValue.equals(newValue) == false) {
 								variablesMap.put(variable.name(), newValue);
 								
 								logger.log(sourcePath, variablesStack.getLineNumber(), methodName, variable.name(), newValue);
 							}
 						} else {
 							// Variable was just declared
 							
 							Value newValue = stackFrame.getValue(variable);
 							
 							variablesMap.put(variable.name(), newValue);
 							
 							logger.log(sourcePath, variablesStack.getLineNumber(), methodName, variable.name(), newValue);
 						}						
 					}					
 
 					System.out.println("Step in " + sourcePath + ":" + lineNumber);
 					
 					List<String> variableNames = sourceParser.getVariables(sourcePath, lineNumber);
 					
 					if (variableNames != null) {
 						for (String variableName : variableNames) {
 							
 							LocalVariable variable = stackFrame.visibleVariableByName(variableName);
 							
 							if (variable != null) {
 								// The variable was on the stack
 								
 								System.out.println("\tUsed variable " + variableName + " value: " + stackFrame.getValue(variable));
 							} else {
 								// The variable was not on the stack
 
 								Value value = null;
 																
 								ObjectReference objectReference = stackFrame.thisObject();
 								if (objectReference != null) {
 									ReferenceType referenceType = objectReference.referenceType();
 									Field field = referenceType.fieldByName(variableName);
 									value = objectReference.getValue(field);
 								}
									
 								if (value != null) {
 									System.out.println("\tUsed variable " + variableName + " value: " + value);
 								} else {
									System.out.println("\tUsed variable " + variableName + " value: UNKNOWN");
 								}
 							}
 						}
 					}
 					
 					variablesStack.setLineNumber(lineNumber);
 				} catch (IncompatibleThreadStateException e) {
 					// No stack variables available for this thread
 					
 					continue;
 				} catch (AbsentInformationException e) {
 					// No stack variables available for this thread
 					
 					continue;
 				} 
 			}
 		}
 		
 		return 0;
 	}
 	
 	private class ThreadsMap extends HashMap<String, VariablesStack> {
 
 		private static final long serialVersionUID = -58718752877349620L;
 	}
 	
 	private class VariablesStack extends Stack<VariablesMap> {
 
 		private static final long serialVersionUID = -7640570711643014510L;
 		
 		private int lineNumber;
 		
 		public int getLineNumber() {
 			return lineNumber;
 		}
 		
 		public void setLineNumber(int lineNumber) {
 			this.lineNumber = lineNumber;
 		}
 	}
 	
 	private class VariablesMap extends HashMap<String, Value> {
 
 		private static final long serialVersionUID = 6201778837494415462L;	
 	}
 }
