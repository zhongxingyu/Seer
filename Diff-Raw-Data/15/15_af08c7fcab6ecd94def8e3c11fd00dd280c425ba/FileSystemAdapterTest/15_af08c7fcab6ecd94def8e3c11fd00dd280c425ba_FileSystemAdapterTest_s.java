 package no.f12.agiledeploy.deployer;
 
 import static org.junit.Assert.assertEquals;
 
 import java.io.File;
 
 import org.junit.Test;
 
 public class FileSystemAdapterTest {
 
 	@Test(expected = IllegalStateException.class)
 	public void shouldThrowExceptionWhenUnableToCreateSymLink() {
 		FileSystemAdapterImpl adapter = new FileSystemAdapterImpl();
 		adapter.setSymLinkCommand("nothing");
 		adapter.createSymbolicLink(new File("."), new File("."));
 	}
 
 	@Test
 	public void shouldSucceedWithSimpleCommand() {
 		FileSystemAdapterImpl adapter = new FileSystemAdapterImpl();
 		adapter.setSymLinkCommand("echo");
 	}
 
 	@Test
 	public void shouldPerformExpectedSubstitution() {
 		assertEquals(String.format(FileSystemAdapterImpl.DEFAULT_SYMLINKCOMMAND, "hello1", "hello2"),
 				"ln -s hello1 hello2");
 	}

 }
