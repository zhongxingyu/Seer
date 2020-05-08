 package org.zend.sdk.test;
 
 import static org.junit.Assert.fail;
 
 import java.io.File;
 import java.util.Random;
 
 import org.junit.After;
 import org.junit.Before;
 import org.zend.sdkcli.internal.commands.CommandLine;
 import org.zend.sdklib.logger.ILogger;
 import org.zend.sdklib.logger.Log;
 
 public abstract class AbstractTest {
 
	protected static final String FOLDER = "test/config/apps/";
 
 	protected File file;
 
 	@Before
 	public void startUp() {
 		final String tempDir = System.getProperty("java.io.tmpdir");
 		file = new File(tempDir + File.separator + new Random().nextInt());
 		file.mkdir();
 		Log.getInstance().registerLogger(new ILogger() {
 
 			@Override
 			public void warning(Object message) {
 				System.out.println(message);
 			}
 
 			@Override
 			public void info(Object message) {
 				System.out.println(message);
 			}
 
 			@Override
 			public ILogger getLogger(String creatorName, boolean verbose) {
 				return this;
 			}
 
 			@Override
 			public void error(Object message) {
 				System.out.println(message);
 			}
 
 			@Override
 			public void debug(Object message) {
 				System.out.println(message);
 			}
 		});
 	}
 
 	@After
 	public void shutdown() {
 		delete(file);
 	}
 
 	protected CommandLine getLine(String command) {
 		String[] parts = command.split(" ");
 		if (parts.length > 0) {
 			System.out.println("Command: " + command);
 			return new CommandLine(parts);
 		}
 		fail("Invalid command line: " + command);
 		return null;
 	}
 
 	protected boolean delete(File file) {
 		if (file == null || !file.exists()) {
 			return true;
 		}
 		if (file.isDirectory()) {
 			String[] children = file.list();
 			for (int i = 0; i < children.length; i++) {
 				boolean result = delete(new File(file, children[i]));
 				if (!result) {
 					return false;
 				}
 			}
 		}
 		return file.delete();
 	}
 
 }
