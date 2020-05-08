 package org.apache.ibatis.executor;
 
 public class ErrorContext {
 
   private static String NEWLINE; // Can't be final due to a weird Java compiler issue.
   private static final ThreadLocal<ErrorContext> LOCAL = new ThreadLocal<ErrorContext>();
 
   private ErrorContext stored;
   private String resource;
   private String activity;
   private String object;
   private String message;
   private String sql;
   private Throwable cause;
 
   static {
     try {
       NEWLINE = System.getProperty("line.separator");
     } catch (Throwable t) {
       NEWLINE = "\n";
     }
   }
 
   private ErrorContext() {
   }
 
   public static ErrorContext instance() {
     ErrorContext context = LOCAL.get();
     if (context == null) {
       context = new ErrorContext();
       LOCAL.set(context);
     }
     return context;
   }
 
   public ErrorContext store() {
     stored = this;
     LOCAL.set(new ErrorContext());
     return LOCAL.get();
   }
 
   public ErrorContext recall() {
     if (stored != null) {
       LOCAL.set(stored);
       stored = null;
     }
     return LOCAL.get();
   }
 
   public ErrorContext resource(String resource) {
     this.resource = resource;
     return this;
   }
 
   public ErrorContext activity(String activity) {
     this.activity = activity;
     return this;
   }
 
   public ErrorContext object(String object) {
     this.object = object;
     return this;
   }
 
   public ErrorContext message(String message) {
     this.message = message;
     return this;
   }
 
   public ErrorContext sql(String sql) {
     this.sql = sql;
     return this;
   }
 
   public ErrorContext cause(Throwable cause) {
     this.cause = cause;
     return this;
   }
 
   public ErrorContext reset() {
 	LOCAL.remove();
     resource = null;
     activity = null;
     object = null;
     message = null;
     sql = null;
     cause = null;
     return this;
   }
 
   public String toString() {
     StringBuffer description = new StringBuffer();
 
     // message
     if (this.message != null) {
       description.append(NEWLINE);
       description.append("### ");
       description.append(this.message);
     }
 
     // resource
     if (resource != null) {
       description.append(NEWLINE);
       description.append("### The error may exist in ");
       description.append(resource);
     }
 
     // object
     if (object != null) {
       description.append(NEWLINE);
       description.append("### The error may involve ");
       description.append(object);
     }
 
     // activity
     if (activity != null) {
       description.append(NEWLINE);
       description.append("### The error occurred while ");
       description.append(activity);
     }
 
     // activity
     if (sql != null) {
       description.append(NEWLINE);
       description.append("### SQL: ");
       description.append(sql.replace('\n', ' ').replace('\r', ' ').replace('\t', ' ').trim());
     }
 
     // cause
     if (cause != null) {
       description.append(NEWLINE);
       description.append("### Cause: ");
       description.append(cause.toString());
     }
 
     return description.toString();
   }
 
 }
