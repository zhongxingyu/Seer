 package clear4j;
 
 import clear4j.processor.instruction.Instruction;
 import clear4j.msg.Messenger;
 import clear4j.msg.beans.DefaultQueue;
 import clear4j.msg.queue.Message;
 import clear4j.msg.queue.MessageListener;
 import clear4j.processor.CustomLoader;
 import clear4j.processor.Process;
 
 import java.io.Serializable;
 import java.lang.reflect.Method;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * User: alexb
  * Date: 24/05/12
  * Time: 16:44
  */
 public final class Clear {
 
     private Clear() {
     }
 
     private static final Logger LOG = Logger.getLogger(Clear.class.getName());
 
     public static void run(Workflow workflow) {
         run(workflow, workflow.getNextInstruction());
     }
 
     private static void run(Workflow workflow, Serializable returnValue) {
         run(workflow, workflow.getNextInstruction(returnValue)); //TODO
     }
 
     private static void run(Workflow workflow, Instruction<?> instr){
     	if (instr != null) {
             Function function = instr.getFunction();
             Messenger.send(new DefaultQueue(function.getProcessorClass().getName(), function.getHost()), workflow);
         }
     }
 
     static {
     	init();
     }
     
     private static void init() {
     	//load all function definitions
     	try {
 			for(Class<?> loaded : CustomLoader.getClasses("clear4j")){ //TODO default config package
 				if (loaded.getAnnotation(Config.class) != null){
 					for (Method method : loaded.getDeclaredMethods()){
 						if (Function.class == method.getReturnType()){
 							Function function = (Function) method.invoke(null,(Object[]) null);
 							setup(function);
 						}
 					}
 				}
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 			throw new RuntimeException(e);
 		} 
     }
 
 	private static void setup(Function function) {
         //TODO check this - only register local processors
         if (function.getHost().isLocal()) {
 
         	final Class<?> processorClass = function.getProcessorClass();
         	
             Messenger.register(new DefaultQueue(processorClass.getName(), function.getHost()), new MessageListener<Workflow>() {
 
                 @Override
                 public void onMessage(Message<Workflow> message) {
 
                     Workflow workflow = message.getPayload();
 
                     Instruction<?> instr = workflow.getCurrentInstruction();
 
                     String operation = instr.getFunction().getOperation();
 
                     try {
 
                         
                         //TODO new instance?
                         Object processorObject = processorClass.getConstructor().newInstance();
 
                         for (final Method method : processorClass.getDeclaredMethods()) {
 
                             Process annotation = method.getAnnotation(Process.class);
                             if (annotation != null && method.getName().equals(operation)) {
 
                             	if (processorClass != Functions.finalProcess().getProcessorClass()) {
                                     Serializable returnValue = (Serializable) method.invoke(processorObject, instr.getValue());
                                     //this just stores the values for debugging
                                     workflow.setValue(String.format("%s.%s(%s)", processorClass.getName(), operation, instr.getValue()), returnValue);
 
                                     run(workflow, returnValue);
                                     
                             	} else {
                             		method.invoke(processorObject, workflow);
                             	}
 
                             }
                         }
 
                     } catch (Exception e) {
                         LOG.log(Level.SEVERE, e.getMessage());
                         e.printStackTrace();
                     }
                 }
             });
 
         }
         
 	}
     
    
 
 
 }
