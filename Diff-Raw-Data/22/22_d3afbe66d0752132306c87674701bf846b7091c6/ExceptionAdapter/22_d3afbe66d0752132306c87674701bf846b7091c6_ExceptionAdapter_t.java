 package melnorme.miscutil;
 
 import java.io.IOException;
 
 
 /**
  * Excepetion adapter to make checked exceptions less annoying. 
  * Based on Bruce Eckel's article:
  * http://www.mindview.net/Etc/Discussions/CheckedExceptions
  */
 @SuppressWarnings("serial")
 public class ExceptionAdapter extends RuntimeException {
 
 	// The original checked exception
 	private Exception originalException;
 	// Number of frames that originalException traveled while checked
 	private int checkedLength; 
 
 	public ExceptionAdapter(Exception e) {
 		super(e.toString());
 		originalException = e;
 		
 		// Determine checkedLength based on the difference to this stack trace
 		StackTraceElement[] est = e.getStackTrace();
 		checkedLength = est.length - getStackTrace().length;
 		
 		StackTraceElement ste = getStackTrace()[0];
 		String firstMethod = ste.getClassName() +"."+ ste.getMethodName();
 		// Adjust checkedLength if EA was created in method unchecked
 		if(firstMethod.endsWith("ExceptionAdapter.unchecked"))
 			checkedLength++;
 		
 	}
 

	protected void printStackTraceAppendable(Appendable pr) {
         synchronized(pr) {
             try {
 				pr.append(this.toString());
 	            StackTraceElement[] trace = originalException.getStackTrace();
 	            for (int i=0; i < trace.length; i++) {
 	                pr.append("\tat " + trace[i]);
 	            	if(i == checkedLength)
 	            		pr.append(" [UNCHECKED]");
 	                pr.append("\n");
 	            }
 			} catch (IOException e) {
 				melnorme.miscutil.Assert.assertFail();
 			}
         }
 	}
 	
 	@Override
 	public void printStackTrace(java.io.PrintStream ps) {		
		printStackTraceAppendable(ps);
 	}
 
 	@Override
 	public void printStackTrace(java.io.PrintWriter pw) {
		printStackTraceAppendable(pw);
 	}
 
 
 	public void rethrow() throws Exception {
 		throw originalException;
 	}
 
 	@Override
 	public String toString() {
         //String name = getClass().getName();
         //return name + "\n>> " + getLocalizedMessage();
         return "[UE] " + getLocalizedMessage();
 	}
 	
 	/** Creates an unchecked Throwable, if not unchecked already. */
 	public static final RuntimeException unchecked(Throwable e) {
 		if(e instanceof RuntimeException)
 			throw (RuntimeException) e;
 		else if(e instanceof Exception)
 			throw new ExceptionAdapter((Exception)e);
 		if(e instanceof Error)
 			throw (Error) e;
 		else {
 			Assert.fail("uncheck: Unsupported Throwable: " + e);
 			return null;
 		}
 	}
 	
	/** Same as unchecked() but stands for TO DO code. 
	 * Uses the Deprecated annotation solely to cause a warning. */
	@Deprecated 
	public static RuntimeException uncheckedTODO(Throwable e) {
 		return unchecked(e);
 	}
 }
