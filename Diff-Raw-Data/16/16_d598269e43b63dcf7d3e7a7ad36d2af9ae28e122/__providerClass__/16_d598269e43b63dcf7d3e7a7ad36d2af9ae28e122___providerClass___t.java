package ${package};
 
 import java.rmi.RemoteException; 
 import java.util.logging.Logger;
 
 import sorcer.service.Context;
 import sorcer.util.Log;
 
 public class ${providerClass} implements ${providerInterface} {
 
 	private static Logger logger = Log.getTestLog();
 	
 	public Context sayHelloWorld(Context context) throws RemoteException {
 		try {
 			logger.info("${providerInterface} Provider got a message: " + context);
 			String input = (String) context.getValue("in/value");
 			logger.info("${providerInterface} Input = " + input);
 			String output = "Hello there - " + input;
 			context.putOutValue("out/value", output);
 			logger.info("${providerInterface} Provider sent a message" + context);
 		} catch (Exception e) {
 			logger.severe("${providerInterface} Provider - problem interpreting message: " + context);
 		}
 		return context;		
 	}
 }
