 package ca.todoist.util;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.hamcrest.CoreMatchers.notNullValue;
 import static org.junit.Assert.assertThat;
 
 import org.junit.Test;
 
 public class LoadFileTest {
 
 	private static final String VALD_FILE_NAME = getValidFileName();
 
 	private static String getValidFileName() {
 		final String dir = System.getProperty("user.dir");
 		String folderAndName = "\\src\\test\\java\\test\\test.properties";
 		return dir + folderAndName;
 	}
 
 	@Test(expected = IllegalArgumentException.class)
 	public void testLoadEmpty() throws Exception {
 		LoadFile.load("");
 	}
 
 	@Test
 	public void testLoad() throws Exception {
 		assertThat(LoadFile.load(VALD_FILE_NAME), is(notNullValue()));
 	}
 }
