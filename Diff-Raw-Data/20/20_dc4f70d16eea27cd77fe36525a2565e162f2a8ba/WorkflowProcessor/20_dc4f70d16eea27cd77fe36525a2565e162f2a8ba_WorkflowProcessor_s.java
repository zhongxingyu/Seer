 package clear4j.processors;
 
 import clear4j.Clear;
 import clear4j.Runner;
 import clear4j.beans.Workflow;
 import clear4j.processor.Arg;
 import clear4j.processor.Function;
 import clear4j.processor.instruction.Instruction;
 
 import java.io.Serializable;
 import java.lang.reflect.Method;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.CopyOnWriteArrayList;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 
 public class WorkflowProcessor {
 
     private static final Logger LOG = Logger.getLogger(WorkflowProcessor.class.getName());
 
     // LOCKS for waitFor method
     private static final Map<String, Object> LOCKS = new ConcurrentHashMap<String, Object>();
     // Workflows returned that have been waited for
     private static final Map<String, Workflow> RECEIVED = new ConcurrentHashMap<String, Workflow>();
     // Active workflows
     private static final List<Workflow> ACTIVE_WORKFLOWS = new CopyOnWriteArrayList<Workflow>();
 
     @Function
     public void initialProcess(Workflow workflow){
         if (LOG.isLoggable(Level.INFO)){
             LOG.info(String.format("Running new workflow [%s]", workflow.getId()));
         }
         ACTIVE_WORKFLOWS.add(workflow);
     }
 
     @Function
 	public void finalProcess(Workflow workflow){
 
         if (LOG.isLoggable(Level.INFO)){
             LOG.info(String.format("Finishing workflow [%s]", workflow.getId()));
         }
         if (LOCKS.containsKey(workflow.getId())){
             RECEIVED.put(workflow.getId(), workflow);
         }
 		Object lock = LOCKS.get(workflow.getId());
 		if (lock != null){
 			synchronized (lock) {
 				lock.notifyAll();
 			}
 		}
 
         ACTIVE_WORKFLOWS.remove(workflow);
 
 	}
 
     @Function
     public void runWorkflowRemotely(Workflow workflow){
        Clear.run(Workflow.localize(workflow));
     }
 
     @Function    //TODO return monitor object
     public String monitor(){
         return String.valueOf(ACTIVE_WORKFLOWS);
     }
 
     public static void processWorkflow(Class<?> processorClass, Workflow workflow) {
     	Instruction<?> instr = workflow.getCurrentInstruction();
 
         String operation = instr.getFunction().getOperation();
         Arg<?>[] args = instr.getFunction().getArgs();
 
         if (LOG.isLoggable(Level.INFO)){
             LOG.info(String.format("operation [%s]", operation));
         }
 
         try { //TODO all this needs cleaning up!!!!!!!!!!!!!!
 
             //TODO new instance?
             Object processorObject = processorClass.getConstructor().newInstance();
 
             for (final Method method : processorClass.getDeclaredMethods()) {
 
                 clear4j.processor.Function annotation = method.getAnnotation(clear4j.processor.Function.class);
                 if (annotation != null && method.getName().equals(operation)) {
 
                     if (!operation.equals("finalProcess")) { //TODO proper checking
 
                         if (!operation.equals("initialProcess")) {
 
                             if (args.length == 0 && method.getParameterTypes().length > 1) {
                                 continue; // match args with method params
                             }
 
                             if (LOG.isLoggable(Level.INFO)) {
                                 LOG.info(String.format("Invoking method [%s] on [%s] with piped value [%s] and args [%s]", method.getName(), processorObject, instr.getValue(), Arrays.toString(args)));
                             }
                             try {
                                 Serializable returnValue;
                                 if (args.length > 0 && method.getParameterTypes().length == 2) {
                                     returnValue = (Serializable) method.invoke(processorObject, instr.getValue(), instr.getFunction().getArgs());
                                 } else if (method.getParameterTypes().length == 1) {
                                     returnValue = (Serializable) method.invoke(processorObject, instr.getValue());
                                 } else {
                                     returnValue = (Serializable) method.invoke(processorObject);
                                 }
                                 if (LOG.isLoggable(Level.INFO)) {
                                     LOG.info(String.format("Returned value: [%s]", returnValue));
                                 }
 
                                 //run next instruction in the workflow piping in the return value returned from this call
                                 instr.setDone(true);
                                 Runner.run(workflow, returnValue);
 
                             } catch (Exception e) {
                                 instr.setException(e);
                                 Runner.runFinalProcess(workflow);
                                 throw new RuntimeException(String.format("Exception in workflow [%s] at instruction [%s]", workflow, instr), e);
                             }
 
                         } else {   //run inital process
                             method.invoke(processorObject, workflow);
                             Clear.run(workflow);
                         }
 
                     } else { //run final process
                         method.invoke(processorObject, workflow);
                     }
 
                 }
             }
 
         } catch (Exception e) {
             LOG.log(Level.SEVERE, e.getMessage(), e);
         }
 
 	}
 
 	public static Serializable waitFor(String id){
 		//create LOCK object for id
 		Object lock = new Object();
 		LOCKS.put(id, lock);
 		synchronized(lock){
 			try {
 				lock.wait();
 				LOCKS.remove(id);
 			} catch (InterruptedException e) {
 				Thread.currentThread().interrupt();
 			}
 		}
         Workflow workflow = RECEIVED.remove(id);
         return workflow.getCurrentInstruction().getValue();
 	}
 }
