 package ghm.follow.test;
 
 import ghm.follow.FileFollower;
 import ghm.follow.io.OutputDestination;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.Writer;
 
 public abstract class BaseTestCase {
 	protected FileFollower follower;
 	protected File followedFile;
 	protected Writer followedFileWriter;
 
 	public void setUp() throws Exception {
		System.setProperty("user.home", System.getProperty("java.io.tmpdir"));
		System.out.println("Using ${user.home} = " + System.getProperty("user.home"));

 		followedFile = createTempFile();
 		followedFile.deleteOnExit();
 		followedFileWriter = new BufferedWriter(new FileWriter(followedFile));
 		follower = new FileFollower(followedFile, new OutputDestination[0]);
 	}
 
 	public void tearDown() throws Exception {
 		follower.stopAndWait();
 		followedFileWriter.flush();
 		followedFileWriter.close();
 	}
 
 	protected void writeToFollowedFileAndWait(String string) throws Exception {
 		followedFileWriter.write(string);
 		followedFileWriter.flush();
 		Thread.sleep(follower.getLatency() + 100);
 	}
 
 	protected void clearFollowedFile() throws Exception {
 		followedFileWriter.close();
 		new FileWriter(followedFile, false).write("");
 	}
 
 	protected File createTempFile() throws IOException {
 		File file = File.createTempFile("followedFile", null);
 		file.deleteOnExit();
 		return file;
 	}
 }
