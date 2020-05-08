 package ch.bazaruto.tests;
 
 import static org.junit.Assert.*;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.zip.ZipFile;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import ch.bazaruto.storage.ZipStorage;
 
 import static org.hamcrest.Matchers.*;
 
 public class ZipStorageTest {
 	
 	ZipStorage zip;
 	
 	@Before
 	public void setUp() throws Exception {
 		zip = new ZipStorage(new ZipFile("static/test.zip"));
 	}
 	
 	@Test
 	public void exists_file1() {
 		assertThat(zip.exists("file1.txt"), is(true));
 	}
 	
 	@Test
 	public void exists_folder1() {
 		assertThat(zip.exists("folder1"), is(true));
 	}
 	
 	@Test
 	public void exists_file2() {
 		assertThat(zip.exists("folder1/file2.txt"), is(true));
 	}
 	
 	@Test
 	public void exists_folder2() {
 		assertThat(zip.exists("folder1/folder2"), is(true));
 	}
 	
 	@Test
	public void exists_index() {
		assertThat(zip.exists(""), is(true));
	}
	
	@Test
	public void exists_dot_index() {
		assertThat(zip.exists("."), is(true));
	}
	
	@Test
 	public void file_does_not_exist() {
 		assertThat(zip.exists("file8.txt"), is(false));
 	}
 	
 	@Test
 	public void folder_does_not_exist() {
 		assertThat(zip.exists("folder1/folder3"), is(false));
 	}
 
 	
 	@Test
 	public void list_has_file1() {
 		assertThat(zip.list(""), hasItemInArray("file1.txt"));
 	}
 	
 	@Test
 	public void list_dot_has_file1() {
 		assertThat(zip.list("."), hasItemInArray("file1.txt"));
 	}
 
 	@Test
 	public void list_folder1() {
 		assertThat(zip.list(""), hasItemInArray("folder1"));
 	}
 	
 	@Test
 	public void list_folder1_has_file2() {
 		assertThat(zip.list("folder1"), hasItemInArray("file2.txt"));
 	}
 	
 	@Test
 	public void list_folder1_has_folder2() {
 		assertThat(zip.list("folder1"), hasItemInArray("folder2"));
 	}
 	
 	@Test
 	public void list_folder2_has_file3() {
 		assertThat(zip.list("folder1/folder2"), hasItemInArray("file3.txt"));
 	}
 	
 	@Test
 	public void isDirectory_folder1() {
 		assertThat(zip.isDirectory("folder1"), is(true));
 	}
 	
 	@Test
 	public void isDirectory_folder2() {
 		assertThat(zip.isDirectory("folder1/folder2/"), is(true));
 	}
 	
 	@Test
 	public void not_isDirectory_file1() {
 		assertThat(zip.isDirectory("file1.txt"), is(false));
 	}
 	
 	@Test
 	public void not_isDirectory_file2() {
 		assertThat(zip.isDirectory("folder1/file2.txt"), is(false));
 	}
 	
 	@Test
	public void isDirectory_index() {
		assertThat(zip.isDirectory(""), is(true));
	}
	
	@Test
 	public void lastModified_file1() {
 		assertThat(zip.lastModified("file1.txt"), greaterThan(0L));
 	}
 	
 	@Test
 	public void length_file1() {
 		assertThat(zip.length("file1.txt"), is(4L));
 	}
 	
 	@Test
 	public void length_folder1() {
 		assertThat(zip.length("folder1"), is(0L));
 	}
 	
 	@Test
 	public void read_file1() {
 		try {
 			InputStreamReader isr = new InputStreamReader(zip.open("file1.txt"));
 			BufferedReader br = new BufferedReader(isr);
 			assertEquals("foo", br.readLine());
 		} catch (IOException e) {
 			e.printStackTrace();
 			fail("Could not read file 1");
 		}
 	}
 	
 	@Test
 	public void read_file2() {
 		try {
 			InputStreamReader isr = new InputStreamReader(zip.open("folder1/file2.txt"));
 			BufferedReader br = new BufferedReader(isr);
 			assertEquals("bar", br.readLine());
 		} catch (IOException e) {
 			e.printStackTrace();
 			fail("Could not read file 1");
 		}
 	}
 }
