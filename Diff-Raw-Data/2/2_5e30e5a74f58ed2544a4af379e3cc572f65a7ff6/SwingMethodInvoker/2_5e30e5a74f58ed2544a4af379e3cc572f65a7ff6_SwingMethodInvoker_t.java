 package com.test9.irc.display;
 
 import java.io.ByteArrayOutputStream;
 import java.io.PrintStream;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 
 /**
  * This class abuses generic types and reflection to allow any method with less than
  * 6 parameters to be called on any Swing Component asynchronously using the
  * SwingUtilities.invokeLater(..) or synchronously without concern about threading
  * issues using the SwingUtilities.invokeAndWait method. invokeAndWait should be used
  * any time multiple dependent calls are made to handle an update.
  * 
  * If it is desired that a method with more than 5 parameters be called all that
  * is required to add support is the addition of case labels to support higher
  * numbers. This should be trivial to implement.
  * 
  * The specific method prototype to be called is defined using the enclosed Parameter
  * class. Parameter instances should be constructed using the type that is used to
  * define the actual Method being called. Behavior is unspecified if the instance of
  * Class used to construct a Parameter is not the actual type that is used in the
  * Method declaration being invoked. If the Method being invoked has a parameter 
  * specified as being of Interface foo and you construct a Parameter as being of type
  * FooImplementation it may still throw an IllegalArgumentException when the Method
  * is invoked resulting in a RuntimeException being thrown.
  * 
  * @author Jason Stedman
  *
  */
 @SuppressWarnings("rawtypes")
 public class SwingMethodInvoker implements Runnable{
 	private Object subject;
 	private Method verb;
 	private Parameter[] parameters;
 	private boolean hasBeenExecuted;
 
 	/**
 	 * This is the only constructor.
 	 * 
 	 * Use it sparingly, SwingMethodInvoker instances can be reused by resetting the subject, verb and parameters using the reconfigure(..) method.
 	 * 
 	 * Check the hasBeenExecuted method's return value to see if an instance is waiting to be invoked and should not be reused yet.
 	 * Behavior is unspecified if hasBeenExecuted returns false and you modify any members of an instance. Actually, behavior is somewhat unspecified anyway lol.
 	 * 
 	 * @param subject		The Swing Object instance to have its method invoked.
 	 * @param verb			The String name of the method to be invoked. So if you want to <JLabel>.setText(..) you should pass in "setText"
 	 * @param parameters	The parameter list to be passed into the method. It is essential that the correct Class instances be set in the Parameters as they are used to figure out which Method prototype to call.
 	 */
 	public SwingMethodInvoker(Object subject, String verb, Parameter[] parameters ){
 		this.subject = subject;
 		this.parameters = parameters;
 		this.setVerb(verb);
 		this.hasBeenExecuted = false;
 	}
 
 	/**
 	 * This method should be used to reconfigure an instance for reuse.
 	 * 
 	 * @param subject		The JComponent instance to have its method invoked.
 	 * @param verb			The String name of the method to be invoked. So if you want to <JLabel>.setText(..) you should pass in "setText"
 	 * @param parameters	The parameter list to be passed into the method. It is essential that the correct Class instances be set in the Parameters as they are used to figure out which Method prototype to call.
 	 * @return This returns true if the instance has already been invoked, or false if it has not. If it returns false, either wait some time for it to be invoked, or try using a different instance.
 	 */
 	public boolean reconfigure(Object subject, String verb, Parameter[] parameters ){
 		if(this.hasBeenExecuted){
 			this.subject = subject;
 			this.parameters = parameters;
 			this.setVerb(verb);
 			this.hasBeenExecuted = false;
 			return true;
 		}else {
 			return false;
 		}
 	}
 
 	/**
 	 * Do not call this method yourself, pass the SwingMethodInvoker instance to the SwingUtilities.invokeLater(..) to be run for you.
 	 */
 	public void run(){
 		try {
 			switch (parameters.length){
 			case 1:
 				verb.invoke(subject, parameters[0]);
 				break;
 			case 2:
 				verb.invoke(subject, parameters[0], parameters[1]);
 				break;
 			case 3:
 				verb.invoke(subject, parameters[0], parameters[1], parameters[2]);
 				break;
 			case 4:
 				verb.invoke(subject, parameters[0], parameters[1], parameters[2], parameters[3]);
 				break;
 			case 5:
 				verb.invoke(subject, parameters[0], parameters[1], parameters[2], parameters[3], parameters[4]);
 				break;
 			}
 		} catch (IllegalArgumentException e) {
 			throw new RuntimeException("These are not the parameters you are looking for.");
 		} catch (IllegalAccessException e) {
 			throw new RuntimeException("Ah Ah Ah! You didn't say the magic word!");
 		} catch (InvocationTargetException e) {
 			ByteArrayOutputStream out = new ByteArrayOutputStream();
 			PrintStream outStream = new PrintStream(out);
 			e.getTargetException().printStackTrace(outStream);
 			outStream.close();
 			throw new RuntimeException("The method that was invoked("+verb.getName()+") has thrown an exception : "+new String(out.toString()));
 		}
 	}
 
 	private void setVerb(String verb) {
 		Class[] methodPrototype = new Class[parameters.length];
 		for(int x = 0;x<parameters.length;x++){
 			methodPrototype[x]=parameters[x].getType();
 		}
 		try {
 			this.verb = subject.getClass().getMethod(verb, methodPrototype);
 		} catch (SecurityException e) {
 			throw new RuntimeException("Ah Ah Ah! You didn't say the magic word!");
 		} catch (NoSuchMethodException e) {
 			throw new RuntimeException("No such method exists in type : "+subject.getClass().getName()+ " with parameter types : " + listParameterTypes());
 		}
 	}
 
 	private String listParameterTypes() {
 		String listOfParameterTypes = "";
 		for(Parameter p : parameters){
			listOfParameterTypes+=p.getType().getName();
 			listOfParameterTypes+=" ";
 		}
 		return listOfParameterTypes;
 	}
 
 	/**
 	 * You can call this method to see if it is even worth trying to reconfigure a particular instance of this class.
 	 * 
 	 * Checking this method before reconfiguring will be more efficient if you regularly fail reconfigure(..) calls.
 	 * 
 	 * @return hasBeenExecuted
 	 */
 	public boolean hasBeenExecuted() {
 		return hasBeenExecuted;
 	}
 
 	/**
 	 * This class is used to pass a parameter for the Method defined as verb for a SwingMethodInvoker.
 	 * 
 	 * The argument type of type Class must match the fully qualified type of the parameter being set. 
 	 * The argument value is of type P which must extend or implement the class or interface defined by Class type.
 	 * 
 	 * @author Jason
 	 *
 	 * @param <P> P is a type which must extend or implement Class type if it is not of Class type directly.
 	 */
 	public static class Parameter<P>{
 		private Class type;
 		private P value;
 
 		public Parameter(P value, Class type){
 			type = value.getClass();
 			this.setValue(value);
 		}
 
 		public void setType(Class type) {
 			this.type = type;
 		}
 
 		public Class getType() {
 			return type;
 		}
 
 		public void setValue(P value) {
 			this.value = value;
 		}
 
 		public P getValue() {
 			return value;
 		}
 	}
 }
