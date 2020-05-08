 package com.orangeleap.tangerine.util;
 
 import java.util.Stack;
 
 import org.apache.commons.logging.Log;
 import com.orangeleap.tangerine.util.OLLogger;
 
 public class RulesStack {
 	
     protected static final Log logger = OLLogger.getLog(RulesStack.class);
 	
     private static ThreadLocal<Stack<String>> tl_stack = new ThreadLocal<Stack<String>>(){
         protected synchronized Stack<String> initialValue() {
               return new Stack<String>();
           }
     };
   
     public static Stack<String> getStack() {
           return tl_stack.get();
     }
     
     // Returns true if the item was already on the stack (re-entrancy problem?)
     public static boolean push(String operationId) {
     	boolean reentrant = getStack().contains(operationId);
     	getStack().push(operationId);
     	if (reentrant) {
    		logger.error("Re-entrant rules stack state.");
     	}
     	return reentrant;
     }
     
     // Returns false if the expected item wasn't on the top of the stack (missing try/finally?)
     public static boolean pop(String operationId) {
     	boolean expected = operationId.equals(getStack().pop());
     	if (!expected) {
     		logger.error("Unexpected rules stack state: " + operationId);
     	}
     	return expected;
     }
   
 }
