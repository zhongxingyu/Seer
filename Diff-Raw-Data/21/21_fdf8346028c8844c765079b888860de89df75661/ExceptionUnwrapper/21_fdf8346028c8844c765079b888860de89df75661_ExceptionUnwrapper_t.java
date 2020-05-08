 /*
  * Created on Apr 12, 2005
  */
 package uk.org.ponder.util;
 
 /**
  * If the supplied exception object is wrapping another (distinct, non-null
  * exception), return that exception. Otherwise, return null.
  * @author Antranig Basman (antranig@caret.cam.ac.uk)
  * 
  */
 public interface ExceptionUnwrapper {
  /** Returns <code>false</code> if this unwrapper cannot operate (perhaps
   * because the Class it operates on is not visible in this ClassLoader).
   */
  public boolean isValid();
   public Throwable unwrapException(Throwable tounwrap);
 }
