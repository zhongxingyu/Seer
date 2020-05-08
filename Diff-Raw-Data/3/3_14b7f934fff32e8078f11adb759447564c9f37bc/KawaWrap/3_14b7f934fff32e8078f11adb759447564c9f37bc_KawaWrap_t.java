 package util;
 
 import globals.*;
 
 import gnu.mapping.Environment;
 import gnu.mapping.Named;
 import gui.ErrorFrame;
 import kawa.standard.Scheme;
 
 /**
  * Wrapper around Kawa.
  */
 public class KawaWrap {
 	Scheme kawa;
 	Environment env;
 
 	/**
 	 * Connect to Kawa.
 	 */
 	public KawaWrap() {
 		reset();
 	}
 	
 	/**
 	 * Reset the stored Scheme interpreter and environment.
 	 */
 	public void reset() {
 		Scheme.registerEnvironment();
 		kawa = new Scheme();
 		env = kawa.getEnvironment();
 		
 		// Load globals.
         for (Globals g : new Globals[]{
         		new Randomness(),
         		new Mathiness(),
         		new Treeitude(),
         		new Imageitude(),
         }) {
         	try {
         		g.addMethods(this);
         	} catch(Throwable ex) {
         		ErrorFrame.log("Unable to load globals from " + g.getClass().getName() + ": " + ex.getMessage());
         	}
         }
 	}
 	
 	/**
 	 * Bind a new function from the Java end of things.
 	 * 
 	 * @param proc The function to bind.
 	 */
 	public void bind(Named proc) {
 		kawa.defineFunction(proc);
 	}
 	
 	/**
 	 * Evaluate a command, given as a string.
 	 * @param s The string to evaluate.
 	 * @return The result.
 	 */
 	public Object eval(String cmd) {
 		try {
 			cmd = cmd.replace('[', '(').replace(']', ')');
 			Object result = Scheme.eval(cmd, env);
 
 			// Return the final result.
 			if (result == null || result.toString().length() == 0)
 				return null;
 			else 
 				return result;
 		} catch (StackOverflowError ex) {
 			return "Possible infinite loop detected.";
 		} catch (gnu.mapping.UnboundLocationException ex) {
 			return "Error: " + ex.getMessage();
 		} catch (gnu.mapping.WrongArguments ex) {
 			return "Error: " + ex.getMessage();
 		} catch (IllegalArgumentException ex) {
 			return ex.getMessage();
 		} catch (Throwable ex) {
			ErrorFrame.log("Unknown error handled: " + ex.toString());
			return "Error: " + ex.getMessage();
 		}
 	}
 }
