 package net.primitive.javascript.repl;
 
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.times;
 import static org.mockito.Mockito.verify;
 
 import net.primitive.javascript.core.Scriptable;
 import net.primitive.javascript.core.ScriptableObject;
 import net.primitive.javascript.core.natives.StandardObjects;
 import net.primitive.javascript.interpreter.RuntimeContext;
 
 import org.junit.Test;
 import org.mockito.Mockito;
 
 public class GlobalsCommandTest {
 
 	@Test
 	public void should_print_global_objects() throws Exception {
 
 		Scriptable globalObject = new ScriptableObject();
 		RuntimeContext.enterContext(
 				StandardObjects.createStandardObjects(globalObject),
 				globalObject);
 
 		REPLRuntime runtime = mock(REPLRuntime.class);
 		GlobalsCommand command = new GlobalsCommand();
 		command.execute(runtime, new String[] {});
 
		verify(runtime, times(4)).println(Mockito.anyString());
 	}
 
 }
