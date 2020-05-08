 package yeti.environments.java;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.lang.reflect.Modifier;
 import java.util.HashMap;
 
 import yeti.YetiCard;
 import yeti.YetiIdentifier;
 import yeti.YetiLog;
 import yeti.YetiModule;
 import yeti.YetiName;
 import yeti.YetiType;
 import yeti.YetiVariable;
 
 /**
  * Class that represents a Java method.
  * 
  * @author Manuel Oriol (manuel@cs.york.ac.uk)
  * @date Jun 22, 2009
  *
  */
 public class YetiJavaMethod extends YetiJavaRoutine {
 	
 	/**
 	 * a list of methods not to test. Typically will contain wait, notify, notifyAll
 	 */
 	public static HashMap<String,Object> methodsNotToAdd ;
 
 	/**
 	 * Result of the last call.
 	 */
 	public YetiVariable lastCallResult=null;
 	
 	/**
 	 * The actual method to call.
 	 */
 	protected Method m;
 	
 	/**
 	 * Checks whether this method is a static method or not. 
 	 */
 	public boolean isStatic = false;
 	
 	/**
 	 * Constructor to define a Java method. By default the method is non-static.
 	 * 
 	 * @param name the name of the method.
 	 * @param openSlots the open slots of the method.
 	 * @param returnType the return type of the method.
 	 * @param originatingModule the module in which it was defined.
 	 * @param m the method implementation.
 	 */
 	public YetiJavaMethod(YetiName name, YetiType[] openSlots, YetiType returnType, YetiModule originatingModule, Method m) {
 		super(name, openSlots, returnType, originatingModule);
 		isStatic = Modifier.isStatic((m.getModifiers()));
 		this.m=m;
 	}
 	
 	/**
 	 * Constructor to define a Java method
 	 * 
 	 * @param name the name of the method.
 	 * @param openSlots the open slots of the method.
 	 * @param returnType the return type of the method.
 	 * @param originatingModule the module in which it was defined.
 	 * @param m the method implementation.
 	 * @param isStatic the parameter that says whether the method is static or not.
 	 */
 	public YetiJavaMethod(YetiName name, YetiType[] openSlots, YetiType returnType, YetiModule originatingModule, Method m, boolean isStatic) {
 		super(name, openSlots, returnType, originatingModule);
 		this.m=m;
 		this.isStatic=isStatic;
 	}
 	
 	
 	/**
 	 * A method to initialize the set of methods not to add.
 	 */
 	public static void initMethodsNotToAdd(){
 		methodsNotToAdd = new HashMap<String,Object>();
 		methodsNotToAdd.put("wait", null);
 		methodsNotToAdd.put("notify", null);
 		methodsNotToAdd.put("notifyAll", null);
 	}
 	
 	/**
 	 * Checks whether the method corresponds to a method not to test.
 	 * 
 	 * @param methodName teh name of the method.
 	 * @return <code>true</code> if the method should not be tested. <code>false</code> Otherwise.
 	 */
 	public static boolean isMethodNotToAdd(String methodName){
 		if (methodsNotToAdd==null)
 			initMethodsNotToAdd();
 		return methodsNotToAdd.containsKey(methodName);
 	}
 
 	/* (non-Javadoc)
 	 * Returns the name of the method for pretty-print.
 	 * 
 	 * @see java.lang.Object#toString()
 	 */
 	public String toString() {
 		return m.getName();
 	}
 
 	/* (non-Javadoc)
 	 * Method used to perform the actual call
 	 * 
 	 * @see yeti.environments.java.YetiJavaRoutine#makeCall(yeti.YetiCard[])
 	 */
 	public Object makeCall(YetiCard []arg){
 		lastCallResult=null;
 		int length = 0;
 		String prefix;
 		boolean isValue= false;
 		
 		// if the method is static, we need to adjust the arguments
 		// there is no target in the open slots.
 		if (isStatic){
 			length = arg.length;
 			prefix = this.originatingModule.getModuleName();
 		}else{
 			length = arg.length-1;
 			prefix = arg[0].getIdentity().toString();
 		}
 		
 		Object []initargs=new Object[length];
 		String log = null;
 		
 		// we start generating the log as well as the identifier to use to store the 
 		// result if there is one.
 		YetiIdentifier id=null;
 		if (m.getReturnType()!= void.class) {
 			// if there is a result to be expected
 			YetiLog.printDebugLog("return type is "+m.getReturnType().getName(), this);
 			id=YetiIdentifier.getFreshIdentifier();
 			String s0=m.getName();
 			if (s0.startsWith("__yetiValue_")){
 				isValue=true;
 				log = this.returnType.toString()+" "+ id.getValue() + "=";
 			} else
 				log = this.returnType.toString()+" "+ id.getValue() + "="+ prefix +"."+ m.getName()+"(";			
 		} else {
 			// otherwise
 			log = prefix + "."+m.getName()+"(";
 		}
 		
 		// we adjust the number of arguments according 
 		// to the fact that they are static or not.
 		int offset=1;
 		Object target=null;
 		if (isStatic){
 			offset=0;
 		} else {
 			target= arg[0].getValue();			
 		}
 		for (int i = offset;i<arg.length; i++){
 			initargs[i-offset]=arg[i].getValue();
 			if (!isValue){
 				log=log+arg[i].toString();
 				if (i<arg.length-1){
 					log=log+",";
 				}
 			}
 		}
 		try {
 			
 			// we  make the call
 			if (target!=null)
 				YetiLog.printDebugLog("trying to call "+m.getName()+" on a "+target.getClass().getName(), this);
 			else 
 				YetiLog.printDebugLog("trying to call statically "+m.getName()+" of "+m.getDeclaringClass().getName(), this);				
 			Object o = m.invoke(target,initargs);
 			
 			// if the reurn type is void, we look it up
 			if (returnType==null)
 				returnType=YetiType.allTypes.get(m.getReturnType().getName());
 			// if there is a result, we store it and create the variable
 			if (id!=null&&o!=null){
 				this.lastCallResult=new YetiVariable(id, returnType, o);
 			}
 			// if this is a value, we print it directly
 			if (isValue)
 				// we escape the values
 				// TODO check that this is sufficient
 				if (o instanceof Character){
 					String value;
 					switch (((Character)o).charValue()){
 					case '\t': {
 							value = "\t"; 
 							break;
 						}
 					case '\n': {
 							value = "\n";
 							break;
 						}
 					default : 
 						value = ""+(((Character)o).charValue());
 					
 					}
 					log=log+"'"+value+"'"+";";
 				} else
 					log=log+o.toString()+";";
 			else
 				log=log+");";
 			// finally we print the log.
 			YetiLog.printYetiLog(log, this);
 		} catch (IllegalArgumentException e) {
 			// should never happen
 			//e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			// should never happen
 			//e.printStackTrace();
 		} catch (InvocationTargetException e) {
 			
 			// if we are here, we found a bug.
 			// we first print the log
 			YetiLog.printYetiLog(log+");", this);
 			// then print the exception
			if (e.getCause() instanceof RuntimeException || e.getCause() instanceof Error)			
 				YetiLog.printYetiLog("/**BUG FOUND: RUNTIME EXCEPTION**/", this);
 			else
 				YetiLog.printYetiLog("/**NORMAL EXCEPTION:**/", this);
 			YetiLog.printYetiThrowable(e.getCause(), this);
 		} catch (Error e){
 			// if we are here there was a serious error
 			// we print it
 			YetiLog.printYetiLog(log+");", this);
 			YetiLog.printYetiLog("BUG FOUND: ERROR", this);
 			YetiLog.printYetiThrowable(e.getCause(), this);
 	
 		}
 		return this.lastCallResult;
 	}
 
 	/**
 	 * Getter for the implementation of the method.
 	 * 
 	 * @return the implementation of the method.
 	 */
 	public Method getM() {
 		return m;
 	}
 
 	/**
 	 * Setter for the implementation of the method.
 	 * 
 	 * @param m the method to set.
 	 */
 	public void setM(Method m) {
 		this.m = m;
 	}
 
 }
