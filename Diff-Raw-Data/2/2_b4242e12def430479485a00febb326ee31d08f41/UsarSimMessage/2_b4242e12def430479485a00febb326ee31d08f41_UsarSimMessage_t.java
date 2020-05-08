 /**
  * 
  */
 package AP2DX.usarsim;
 
 import java.lang.annotation.Annotation;
 import java.lang.reflect.Type;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import java.lang.reflect.Field;
 //import .Retention;
 //import .Target;
 import java.lang.annotation.*;
 
 import AP2DX.Message;
 import AP2DX.Module;
 
 /**
  * @author Jasper Timmer
  * 
  */
 public class UsarSimMessage extends Message {
     /**
      * make a new UsarSimMessage
      * @param in
      */
 	public UsarSimMessage(String in) {
 		super(in, Module.UNDEFINED);
 	}
 	
     /**
      * make a new UsarSimMessage
      */
 	public UsarSimMessage(MessageType type) {
 		super(type);
 	}
 	
 	@Override
         public Message.MessageType getMsgType() {
             if(this.type == null)
             {
                 String startPatternStr = "^[A-Z]+";
                 Pattern startPattern = Pattern.compile(startPatternStr);
                 Matcher startMatcher = startPattern.matcher(this.getMessageString());
 
                 if (startMatcher.find()) {
                    this.type = UsarSimMessage.MessageType.getEnumByString(startMatcher.group(0));
                 }
                 else{
                     this.type = null;
                 }
             }
             return this.type;
         }
 
 	@Override
 	protected void parseMessage() throws Exception {
 		throw new Exception("Not possible on this class, try casting to a specialized message type.");
 	}
 	
 	
 	/**
 	 * This method uses annotated fields to build the output of the message.
 	 * field.toString() is the value of the field like so: {name value}.
 	 */
 	@Override
 	protected String compileMessage() {
 		StringBuilder output = new StringBuilder();
 		
 		output.append(this.getMsgType());
 		
         for (Field field : this.getClass().getDeclaredFields()) {
             System.out.println("In for loop");
             if (field.isAnnotationPresent(UsarMessageField.class)) {
                 System.out.println("after if");
                 try{
                     field.setAccessible(true);
                     output.append(String.format(" {%s %s}", field.getAnnotation(UsarMessageField.class).name(), field.get(this)));
                     field.setAccessible(false);
                 }
                 catch(Exception e)
                 {
                     System.err.print(e.getMessage());
                 }
 
             } else if (field.isAnnotationPresent(UsarMessageIteratorField.class)) {
                 System.out.println("After else");
                 Iterator it = List.class.cast(field).iterator();
 
                 while (it.hasNext()) {
                     Object entry = it.next();
                     output.append(entry.toString());
                 }
             }
         }
 		
 		return output.toString();
 	}
 
     public String toString()
     {
         return compileMessage();
     }
 	
 	/**
 	 * override getMessageString to execute compile before getting string.
 	 * Best practice is to store this string in a local variable instead of
 	 * repeatedly calling this function.
 	 */
 	@Override
 	public String getMessageString() {
 		return this.messageString;
 	}
 	
 	/**
 	 * Annotation to use on specialized usar sim message fields, that need to be compiled in the outputstring.
 	 * 'name' is the Name of the field
 	 * @author Jasper Timmer
 	 *
 	 */
      @Retention(RetentionPolicy.RUNTIME)
      @Target(ElementType.FIELD)
 	public @interface UsarMessageField {
 		String name();
 	};
 	
 	/**
 	 * Declare this on a specialized message's field, to let the compiler know, that type is a list of values.
 	 * It should be in the form of List<specialclass> ...; where specialclass.toString() returns the value to
 	 * be appended to the output. Example: "{Link 1} {Value 23.3} {Torque -23.4}".
 	 * 
 	 * @author Jasper Timmer
 	 *
 	 */
      @Retention(RetentionPolicy.RUNTIME)
      @Target(ElementType.FIELD)
 	public @interface UsarMessageIteratorField {};
 }
