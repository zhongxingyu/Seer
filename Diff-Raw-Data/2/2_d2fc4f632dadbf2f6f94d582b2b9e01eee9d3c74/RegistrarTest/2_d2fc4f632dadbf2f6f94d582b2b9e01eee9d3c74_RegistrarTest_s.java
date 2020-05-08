 package net.ayld.facade.api;
 
 import java.io.IOException;
 import java.net.URL;
 
 import junit.framework.Assert;
 import net.ayld.facade.event.model.OperationStartEvent;
 import net.ayld.facade.util.Tokenizer;
 
 import org.junit.Test;
 
 import com.google.common.eventbus.Subscribe;
 import com.google.common.io.Resources;
 
 public class RegistrarTest {
 	
 	@Test
 	public void jarExtractionUpdateCallCount() throws IOException {
 		final Listener callCountListener = new Listener();
 		
 		ListenerRegistrar.listeners(callCountListener).register();
 		
 		LibraryMinimizer
 			.forSourcesAt(toPath(Resources.getResource("test-classes/test-src-dir")))
 			.withLibs(toPath(Resources.getResource("test-classes/test-lib-dir")))
 			.getFile();
 		
		Assert.assertTrue(callCountListener.getCallCount() == 6);
 	}
 	
 	private String toPath(URL uri) {
 		return Tokenizer.delimiter(":").tokenize(uri.toString()).lastToken();
 	}
 	
 	public static class Listener {
 		
 		private int callCount = 0;
 
 		@Subscribe
 		public void listen(OperationStartEvent u) {
 			callCount++;
 		}
 		
 		public int getCallCount() {
 			return callCount;
 		}
 	}
 }
