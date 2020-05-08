 package be.uclouvain.jail.vm;
 
 import java.net.URL;
 
 /** Tests examples. */
 public class JailExampleTest {
 
 	/** Returns a graph URL. */
 	public URL[] getJailURLs() {
 		return new URL[] {
 			ClassLoader.getSystemResource("train/commons.jail"),
 			ClassLoader.getSystemResource("train/decorate.jail"),
 			ClassLoader.getSystemResource("train/kernel.jail"),
 			ClassLoader.getSystemResource("train/ptaconstr.jail"),
 			ClassLoader.getSystemResource("train/standard3.jail"),
 		};
 	}
 	
 	/** Tests JailVM class. */
 	public void testJailVM() throws Exception {
 		//int index = 0;
 		for (URL url: getJailURLs()) {
 			//assertNotNull(index++ + "-th URL is not null.",url);
 			JailVM vm = new JailVM();
 			vm.execute(url);
 		}
 	}
 	
 	
 	
 }
