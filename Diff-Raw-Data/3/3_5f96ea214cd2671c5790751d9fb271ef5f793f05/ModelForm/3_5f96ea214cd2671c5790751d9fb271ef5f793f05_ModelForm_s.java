 package forms;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 public abstract class ModelForm<T> {
     protected T instance;
     private ErrorList errors;
 
     public T getInstance() {
         return instance;
     }
 
     public void setInstance(T instance) {
         this.instance = instance;
     }
 
     public abstract boolean isValid();
 
     public T save() throws ValidationException {
         if ( !isValid() ) {
             throw new ValidationException("Form data was not valid");
         }
         return null;
     }
 
     public void clean() {
         Method[] methods = this.getClass().getDeclaredMethods();
         for ( Method method : methods ) {
             if ( method.getName().startsWith("clean") && !method.getName().equals("clean") ) {
                 method.setAccessible(true);
                 try {
                     method.invoke(this);
                 } catch (IllegalAccessException | InvocationTargetException ignored) {
                 }
             }
         }
     }
 
     public ErrorList getErrors() {
         if ( errors == null )
             errors = new ErrorList();
         return errors;
     }
 
     public String getErrorsDisplay() {
         return getErrors().getErrorsDisplay();
     }
 
     class ErrorList {
         protected  Map<String, List<String>> errors;
 
         ErrorList() {
             this.errors = new HashMap<>();
         }
 
         public void appendError(String field, String message) {
             List<String> list = errors.get(field);
             if ( list == null )
                 list = new LinkedList<>();
             list.add(message);
             errors.put(field, list);
         }
 
         public String getErrorsDisplay() {
             StringBuilder sb = new StringBuilder("<html>");
             sb.append("<h3>Input was not valid</h3>");
 
             for ( Map.Entry<String, List<String>> e : errors.entrySet() ) {
 
                 sb.append("<h4>");
                 sb.append(e.getKey()).append(":");
                 sb.append("</h4>");
                 for ( String s : e.getValue() ) {
                     sb.append("<ul>");
                     sb.append("<li>");
                     sb.append(s);
                     sb.append("</li>");
                     sb.append("</ul>");
                 }
                sb.append("</html>");
             }
             System.out.println(sb.toString());
             return sb.toString();
         }
     }
 }
