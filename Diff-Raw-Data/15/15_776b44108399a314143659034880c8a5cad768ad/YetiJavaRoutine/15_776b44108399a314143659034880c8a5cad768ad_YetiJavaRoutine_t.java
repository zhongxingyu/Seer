 package yeti.environments.java;
 
 import java.lang.reflect.InvocationTargetException;
 import java.util.HashMap;
 
 import yeti.YetiCallException;
 import yeti.YetiCard;
 import yeti.YetiLog;
 import yeti.YetiModule;
 import yeti.YetiName;
 import yeti.YetiRoutine;
 import yeti.YetiType;
 import yeti.YetiVariable;
 import yeti.environments.YetiSecurityException;
 
 
 /**
  * Class that represents a routine in Java. A routine is supposed to be either a constructor or a  method.
  * @author  Manuel Oriol (manuel@cs.york.ac.uk)
  * @date  Jun 22, 2009
  */
 public class YetiJavaRoutine extends YetiRoutine {
 
 	/**
 	 * Result of the last call.
 	 */
 	protected YetiVariable lastCallResult=null;
 
 
 	/**
 	 * 
 	 * Creates a Java routine.
 	 * 
 	 * @param name the name of the routine.
 	 * @param openSlots the open slots for the routine.
 	 * @param returnType the type of the returned value.
 	 * @param originatingModule the module in which it was defined
 	 */
 	public YetiJavaRoutine(YetiName name, YetiType[] openSlots, YetiType returnType, YetiModule originatingModule) {
 		super();
 		this.name = name;
 		this.openSlots = openSlots;
 		this.returnType = returnType;
 		this.originatingModule = originatingModule;
 	}
 
 	/* (non-Javadoc)
 	 * 
 	 * Checks the arguments. In the case of Java, arguments match (TODO: change that for generics).
 	 * 
 	 * @see yeti.YetiRoutine#checkArguments(yeti.YetiCard[])
 	 */
 	@Override
 	public boolean checkArguments(YetiCard[] arg) {
 
 		return true;
 	}
 
 	/* (non-Javadoc)
 	 * Method used to perform the actual call
 	 * 
 	 * @see yeti.environments.java.YetiJavaRoutine#makeCall(yeti.YetiCard[])
 	 */
 	public Object makeCall(YetiCard []arg){
 		String log = null;
 		super.makeCall(arg);
 
 		try {
 
 			try {
 				makeEffectiveCall(arg);
 				this.incnTimesCalledSuccessfully();
 			} catch(YetiCallException e) {
 				log = e.getLog();
 				throw e.getOriginalThrowable();
 			}
 
 		} catch (IllegalArgumentException e) {
 			// should never happen
 			//e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			// should never happen
 			// e.printStackTrace();
 		} catch (InvocationTargetException e) {
 
 			// if we are here, we found a bug.
 			// we first print the log
 			YetiLog.printYetiLog(log+");", this);
 			// then print the exception
 			if ((e.getCause() instanceof RuntimeException  || e.getCause() instanceof Error) && !isAcceptable(e.getCause())) {
 				if (e.getCause() instanceof ThreadDeath) {
 					YetiLog.printYetiLog("/**POSSIBLE BUG FOUND: TIMEOUT**/", this);
 					this.incnTimesCalledUndecidable();
 				} else {
 					if (e.getCause() instanceof YetiSecurityException) {
 						YetiLog.printYetiLog("/**POSSIBLE BUG FOUND: "+e.getCause().getMessage()+" **/", this);
 						this.incnTimesCalledUndecidable();
 					} else {
 						if (YetiLog.isAccountableFailure(e.getCause())) {
 							YetiLog.printYetiLog("/**BUG FOUND: RUNTIME EXCEPTION**/", this);
 							this.incnTimesCalledUnsuccessfully();
							//e.getCause().printStackTrace();
 						}
 						else {
 							YetiLog.printYetiLog("/**NORMAL EXCEPTION:**/", this);
 							this.incnTimesCalledSuccessfully();
 						}
 					}
 				}
 				YetiLog.printYetiThrowable(e.getCause(), this,true);
 			}
 			else {
 				YetiLog.printYetiLog("/**NORMAL EXCEPTION:**/", this);
 				YetiLog.printYetiThrowable(e.getCause(), this,false);
 				this.incnTimesCalledSuccessfully();
 			}
 		} catch (Error e){
 			// if we are here there was a serious error
 			// we print it
 			YetiLog.printYetiLog(log+");", this);
 			if (!YetiLog.isAccountableFailure(e.getCause())) {
 				YetiLog.printYetiLog("/**BUG FOUND: ERROR**/", this);
 				YetiLog.printYetiThrowable(e.getCause(), this,true);
 				this.incnTimesCalledUnsuccessfully();
 			}
 			else {
 				YetiLog.printYetiLog("/**NORMAL EXCEPTION:**/", this);
 				YetiLog.printYetiThrowable(e.getCause(), this,true);
 				this.incnTimesCalledSuccessfully();
 			}
 		}
 		catch (Throwable e){
 			// should never happen
			//e.printStackTrace();
 		}
 		return this.lastCallResult;
 	}
 
 	/**
 	 * A hashmap of acceptable exception types.
 	 */
 	@SuppressWarnings("unchecked")
 	public HashMap <String, Class> acceptableExceptionTypes = new HashMap <String, Class>();
 
 	/**
 	 * @return  the types of exceptions that are considered "acceptable"
 	 */
 	@SuppressWarnings("unchecked")
 	public HashMap<String, Class> getAcceptableExceptionTypes() {
 		return acceptableExceptionTypes;
 	}
 
 	/**
 	 * Add an acceptable exception type of this routine.
 	 * 
 	 * @param s the name of the exception type.
 	 */
 	public void addAcceptableExceptionType(String s) {
 		try {	
 			YetiLog.printDebugLog("Added to "+this.name.getValue() + " the following acceptable exception " +s+" in "+Class.forName(s), this);
 			acceptableExceptionTypes.put(s, Class.forName(s));
 		} catch (ClassNotFoundException e) {
 			// Ignored
 			YetiLog.printDebugLog(e.getMessage(), this);
 		}
 	}
 
 	/**
 	 * This method returns true if and only if the throwable 
 	 * is considered acceptable for the routine.
 	 * 
 	 * @param cause the Throwable to consider.
 	 * @return true if the exception is to be expected, false otherwise.
 	 */
 	@SuppressWarnings("unchecked")
 	public boolean isAcceptable(Throwable cause) {
 		// optimization
 		if (acceptableExceptionTypes.isEmpty()) return false;
 		// for all classes, we check that the Throwable is not an instance 
 		// of a subclass of an acceptable Throwable
 
 		YetiLog.printDebugLog("Class of the cause of the Throwable: "+cause.getClass().getName()+ "for method: "+this.getName().getValue(), this);
 		for (Class c: acceptableExceptionTypes.values()) {
 			YetiLog.printDebugLog("Matching: _"+c.getName()+ "_ with: _"+cause.getClass().getName(), this);
 
 			if (c.getName().equals(cause.getClass().getName())) return true;
 		}
 		return false;
 	}
 
 	/* (non-Javadoc)
 	 * A stub for sublasses.
 	 * 
 	 * @see yeti.YetiRoutine#makeEffectiveCall(yeti.YetiCard[])
 	 */
 	@Override
 	public String makeEffectiveCall(YetiCard[] arg) throws Throwable {
 		// by default this one does not do anything
 		return null;
 	}
 
 }
