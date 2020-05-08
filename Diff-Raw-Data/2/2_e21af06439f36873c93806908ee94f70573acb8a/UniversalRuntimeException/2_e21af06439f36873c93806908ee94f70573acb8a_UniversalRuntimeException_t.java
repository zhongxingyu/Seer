 /*
  * Created on 22-Aug-2003
  */
 package uk.org.ponder.util;
 
 import java.io.PrintStream;
 import java.io.PrintWriter;
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 
 import org.xml.sax.SAXException;
 
 import uk.org.ponder.stringutil.CharWrap;
 
 /**
  * The root of unchecked runtime exceptions thrown by the libraries. There is a
  * general movement to make most exceptions runtime exceptions (by not only
  * me!), since exception specifications often add verbosity without facility.
  * <p>
  * Checked exceptions are most appropriate for signalling problems between
  * libraries with a wide degree of separation. Within a single body of code,
  * unchecked exceptions should be used to propagate error conditions to the next
  * boundary. * http://c2.com/cgi/wiki?CheckedExceptionsConsideredHarmful
  * <p>
  * This class has a useful (and growing) body of schemes for absorbing the
  * target exceptions from other types of wrapping exceptions and rewrapping
  * them.
  * <p>
  * What we wish to preserve is a) the ultimate stack trace from the cause of the
  * problem and b) a set of increasingly detailed messages that can be accreted
  * onto the exception as it winds up the stack.
  * <p>
  * A UniversalRuntimeException also contains a Class representing its
  * "category", a point in the inheritance hierarchy that may be used to classify
  * the nature of exceptions, as being distinct from the wrapped target exception
  * intended to record its cause. An object of the category need not ever be
  * created, the inheritance hierachy may be queried via
  * Class.isAssignableFrom().
  * <p>
  * The exception category defaults to the concrete class of the exception being
  * wrapped.
  * 
  * @author Bosmon
  */
 public class UniversalRuntimeException extends RuntimeException implements
     WrappingException {
   private static String[] unwrapclasses = new String[] { "uk.org.ponder.servletutil.ServletExceptionUnwrapper" };
   private Throwable targetexception;
   private String message;
   private Class category;
   private static ArrayList unwrappers = new ArrayList();
   static {
     for (int i = 0; i < unwrapclasses.length; ++i) {
       try {
         Class unwrapclass = Class.forName(unwrapclasses[i]);
         Object unwrapper = unwrapclass.newInstance();
         addUnwrapper((ExceptionUnwrapper) unwrapper);
       }
       catch (Throwable t) {
         Logger.log.warn("Couldn't load unwrapper " + unwrapclasses[i]);
       }
     }
   }
 
   public static synchronized void addUnwrapper(ExceptionUnwrapper muncher) {
     unwrappers.add(muncher);
   }
 
   public UniversalRuntimeException(String s) {
     message = s;
   }
 
   public String getMessage() {
     // fix for failure in log4j to tolerate null message.
     return message == null? "" : message;
   }
 
   public Class getCategory() {
     return category;
   }
 
   public void setCategory(Class category) {
     this.category = category;
   }
 
   public UniversalRuntimeException(Throwable t) {
     message = t.getMessage();
     targetexception = t;
   }
 
   public Throwable getTargetException() {
     return targetexception;
   }
 
   private static String computeMessage(String extradetail, String orig) {
     CharWrap accumulate = new CharWrap();
     accumulate.append(extradetail);
     
     if (orig != null && orig.length() > 0) {
       accumulate.append("\n--> ").append(orig);
     }
     return accumulate.toString();
   }
   
   /**
    * Accumulates the message supplied message onto the beginning of any existing
    * exception message, and wraps supplied exception as the target exception of
    * the returned UniversalRuntimeException.
    * <p>
    * If the supplied exception is already a UniversalRuntimeException, the same
    * object is returned.
    * <p>
    * If the supplied exception is a wrapping exception of one of the recognised
    * kinds (InvocationTargetException, or if registered, ServletException &c), 
    * it is unwrapped and its target exception becomes the wrapped exception.
    * 
    * @param t
    *          An encountered exception, to be wrapped.
    * @param fullmsg
    *          The message to be added to the exceptions information.
    * @return
    */
   public static UniversalRuntimeException accumulate(Throwable t,
       String extradetail) {
     String message = computeMessage(extradetail, t.getMessage());
     UniversalRuntimeException togo = accumulateMsg(t, t.getClass(), message);
     return togo;
   }
 
   /** Used to "pass-through" an exception leaving its message unchanged */
   public static UniversalRuntimeException accumulate(Throwable t) {
     UniversalRuntimeException togo = accumulateMsg(t, t.getClass(), t
         .getMessage());
     return togo;
   }
   
   public static UniversalRuntimeException accumulate(Throwable t,
       Class category, String extradetail) {
     String message = computeMessage(extradetail, t.getMessage());
     UniversalRuntimeException togo = accumulateMsg(t, category, message);
     return togo;
   }
 
   public static Throwable unwrapException(Throwable tounwrap) {
     if (tounwrap instanceof InvocationTargetException) {
       return ((InvocationTargetException)tounwrap).getTargetException();
     }
     if (tounwrap instanceof SAXException) {
       return ((SAXException)tounwrap).getException();
     }
     for (int i = 0; i < unwrappers.size(); ++ i) {
       Throwable unwrapped = ((ExceptionUnwrapper)unwrappers.get(i)).unwrapException(tounwrap);
       if (unwrapped != null) {
         return unwrapped;
       }
     }
     return null;
   }
   
   public static UniversalRuntimeException accumulateMsg(Throwable t,
       Class category, String fullmsg) {
     Throwable tounwrap;
     do {
       tounwrap = t;
       t = unwrapException(tounwrap);
     } while (t != null);
     t = tounwrap;
 
     UniversalRuntimeException togo = null;
     if (t instanceof UniversalRuntimeException) {
       togo = (UniversalRuntimeException) t;
     }
     else {
       togo = new UniversalRuntimeException(t);
       togo.category = category;
     }
     // InvokationTargetException wrapping randomly trashes the message. Guard
     // against this with this generally well-motivated hack:
     if (fullmsg != null && !fullmsg.equals("")) {
       togo.message = fullmsg;
     }
     return togo;
   }
 
   public String getStackHead() {
     CharWrap togo = new CharWrap();
     if (targetexception != null) {
       togo.append("Target exception of " + targetexception.getClass());
     }
     else if (category != null) {
       togo.append("Exception category " + category);
       
     }
    togo.append("\nSuccessive lines until stack trace show causes progressing to exception site:\n").append(getMessage());
     return togo.toString();
   }
 
 
   // QQQQQ move these three methods to static utility for all WrappingExceptions
   public void printStackTrace() {
     if (targetexception != null) {
       System.err.println(getStackHead());
       targetexception.printStackTrace();
     }
     else
       super.printStackTrace();
   }
 
   public void printStackTrace(PrintWriter pw) {
     if (targetexception != null) {
       pw.print(getStackHead());
       targetexception.printStackTrace(pw);
     }
     else
       super.printStackTrace(pw);
   }
 
   public void printStackTrace(PrintStream ps) {
     if (targetexception != null) {
       ps.print(getStackHead());
       targetexception.printStackTrace(ps);
     }
     else
       super.printStackTrace(ps);
   }
 
   public StackTraceElement[] getStackTrace() {
     return targetexception != null ? targetexception.getStackTrace()
         : super.getStackTrace();
   }
 }
