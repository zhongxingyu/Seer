 package org.xmodel.log;
 
 import java.util.Calendar;
 
 /**
  * An implementation of Log.ISink performs basic formatting and delegates logging.
  */
 public final class FormatSink implements ILogSink
 {
   public FormatSink( ILogSink delegate)
   {
     this.delegate = delegate;
   }
 
   /* (non-Javadoc)
    * @see org.xmodel.log.Log.ISink#log(org.xmodel.log.Log, int, java.lang.String)
    */
   @Override
   public void log( Log log, int level, Object message)
   {
     StringBuilder sb = new StringBuilder();
     formatDate( Calendar.getInstance(), sb);
     sb.append( ' ');
     
     sb.append( Log.getLevelName( level).toUpperCase());
     sb.append( ' ');
     
     String thread = Thread.currentThread().getName();
     sb.append( "["); sb.append( thread); sb.append( "] ");
     
     //sb.append( "("); sb.append( log.getName()); sb.append( ") - ");
     sb.append( "("); sb.append( getStack()); sb.append( ") - ");
     
     sb.append( message);
     
     delegate.log( log, level, sb.toString());
   }
 
   /* (non-Javadoc)
    * @see org.xmodel.log.Log.ISink#log(org.xmodel.log.Log, int, java.lang.Throwable)
    */
   @Override
   public void log( Log log, int level, Throwable throwable)
   {
     log( log, level, "", throwable);
   }
 
   /* (non-Javadoc)
    * @see org.xmodel.log.Log.ISink#log(org.xmodel.log.Log, int, java.lang.String, java.lang.Throwable)
    */
   @Override
   public void log( Log log, int level, Object object, Throwable throwable)
   {
     String message = object.toString();
     String thread = Thread.currentThread().getName();
     String levelName = Log.getLevelName( level).toUpperCase();
 
     String trace = getStack();
     StringBuilder date = new StringBuilder();
     formatDate( Calendar.getInstance(), date);
     
     StringBuilder sb = new StringBuilder();
     
     if ( message.length() > 0)
     {
       sb.append( date); sb.append( ' ');
       sb.append( levelName);
       sb.append( " ["); sb.append( thread); sb.append( "]");
      //sb.append( " ("); sb.append( log.getName()); sb.append( ") - ");
       sb.append( " ("); sb.append( trace); sb.append( ") - ");
       sb.append( message);
      delegate.log( log, level, sb.toString());
     }
     
    sb.setLength( 0);
     sb.append( date); sb.append( ' ');
     sb.append( levelName);
     sb.append( " ["); sb.append( thread); sb.append( "]");
    //sb.append( " ("); sb.append( log.getName()); sb.append( ") - ");
     sb.append( " ("); sb.append( trace); sb.append( ") - ");
     sb.append( throwable.getClass().getSimpleName());
     sb.append( ": ");
     sb.append( throwable.getMessage());
    delegate.log( log, level, sb.toString());
     
     StackTraceElement[] stack = throwable.getStackTrace();
     for( StackTraceElement element: stack)
     {
      sb.setLength( 0);
//      sb.append( date); sb.append( ' ');
//      sb.append( levelName);
//      sb.append( ' ');
       sb.append( element.toString());
      delegate.log( log, level, sb.toString());
     }
   }
 
   /**
    * Format the specified absolute time into the specified message.
    * @param calendar The calendar instance.
    * @param message The message.
    */
   private void formatDate( Calendar calendar, StringBuilder message)
   {
     int year = calendar.get( Calendar.YEAR) - 2000;
     int month = calendar.get( Calendar.MONTH);
     int day = calendar.get( Calendar.DAY_OF_MONTH);
     
     int hour = calendar.get( Calendar.HOUR);
     int min = calendar.get( Calendar.MINUTE);
     int sec = calendar.get( Calendar.SECOND);
     int msec = calendar.get( Calendar.MILLISECOND);
     
     message.append( String.format( "%02d-%02d-%02d %02d:%02d:%02d.%03d", month, day, year, hour, min, sec, msec));
   }
   
   /**
    * @return Return partial stack trace.
    */
   private String getStack()
   {
     // get stack trace
     StackTraceElement[] stack = Thread.currentThread().getStackTrace();
     
     // find first non-logging package stack frame
     int start = 1;
     for( ; start < stack.length; start++)
     {
       String className = stack[ start].getClassName();
       if ( !className.startsWith( "org.xmodel.log")) break;
     }
 
     String className = stack[ start].getClassName();
     int index = className.lastIndexOf( '.');
     if ( index > 0) index = className.lastIndexOf( '.', index-1);
     if ( index > 0) className = className.substring( index+1);
 
     return className + "." + stack[ start].getMethodName();
   }
   
   private ILogSink delegate;
 }
