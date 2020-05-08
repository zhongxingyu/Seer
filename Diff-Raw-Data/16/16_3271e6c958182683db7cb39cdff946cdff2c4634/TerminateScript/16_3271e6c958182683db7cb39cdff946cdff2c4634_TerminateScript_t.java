 package test.com.popodeus.chat;
/*
 import javax.script.ScriptException;
 import javax.script.ScriptEngine;
 import javax.script.ScriptContext;
*/
 import net.sourceforge.htmlunit.corejs.javascript.ContextFactory;
 
 /**
  * photodeus
  * Dec 10, 2009
  * 3:31:00 AM
  */
 public class TerminateScript {
 
 	public static void main(String[] args) {
		/*
 		final String script =
 			"function SayIt() {\n" +
 			"	print('I would say x = ' + x);\n" +
 			"}\n" +
 			"var foo = new SayIt();\n" +
 			"var x = 1;\n" +
 			"for (var i=0; i<1024; i++) x *= 1.00001;" +
 			"\nprintln('Hello world... ' + x);";
 
 		try {
 			ContextFactory factory = new ContextFactory();
 
			final ScriptEngine engine =  new ScriptEngine();
 			ScriptContext ctx = engine.getContext();
 
 			long milli = System.currentTimeMillis();
 			long now = System.nanoTime();
 			engine.eval(script, ctx);
 			now = System.nanoTime() - now;
 			milli = System.currentTimeMillis() - milli;
 			System.out.println("Time taken: " + milli);
 
 			engine.invokeFunction("SayIt", new Object[]{null});
 
 		} catch (NoSuchMethodException nex) {
 			System.err.println(nex);
 		} catch (ScriptException e) {
 			e.printStackTrace();
 		}
		*/
 	}
 }
