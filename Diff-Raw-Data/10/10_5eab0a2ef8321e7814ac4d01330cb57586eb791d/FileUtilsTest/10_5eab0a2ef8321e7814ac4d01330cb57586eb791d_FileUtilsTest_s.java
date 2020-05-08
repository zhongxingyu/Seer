 package nl.nikhef.jgridstart.util;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.Arrays;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import junit.framework.AssertionFailedError;
 import junit.framework.TestCase;
 
 import org.junit.Test;
 
 public class FileUtilsTest extends TestCase {
     
     /** as {@link #assertEquals} but then with lineendings trimmed */
     public void assertTrimmedEquals(CharSequence expect, CharSequence real) throws AssertionFailedError {
 	String s = System.getProperty("line.separator");
 	String sexpect = expect.toString();
 	if (sexpect.startsWith(s)) sexpect = sexpect.substring(s.length(), sexpect.length());
 	if (sexpect.endsWith(s)) sexpect = sexpect.substring(0, sexpect.length()-s.length());
 	String sreal = real.toString();
 	if (sreal.startsWith(s)) sreal = sreal.substring(s.length(), sreal.length());
 	if (sreal.endsWith(s)) sreal = sreal.substring(0, sreal.length()-s.length());
 	if (!sexpect.equals(sreal))
 	    throw new AssertionFailedError("expected:<"+sexpect+"> but was:<"+sreal+">");
     }
     
     /** Helper method: make sure file permissions are ok.
      * <p>
      * This method only checks it if it is possible to do so on this system
      * using a known method. On Windows, this test is currently partly skipped.
      */
     protected void assertPermissions(File f, boolean read, boolean write, boolean exec, boolean userOnly) throws AssertionFailedError, IOException {
 	try {
 	    // methods only available for Java 1.6 and higher; want to compile on 1.5 too
 	    final Method canRead = File.class.getDeclaredMethod("canRead", new Class[] {});
 	    final Method canWrite = File.class.getDeclaredMethod("canWrite", new Class[] {});
 	    final Method canExecute = File.class.getDeclaredMethod("canExecute", new Class[] {});
 	    
 	    assertEquals(read, ((Boolean)canRead.invoke(f, new Object[]{})).booleanValue());
 	    assertEquals(write, ((Boolean)canWrite.invoke(f, new Object[]{})).booleanValue());
 	    assertEquals(exec, ((Boolean)canExecute.invoke(f, new Object[]{})).booleanValue());
 	} catch (NoSuchMethodException e){
 	    // too bad ...
 	} catch (InvocationTargetException e) {
 	    throw new IOException(e.getLocalizedMessage());
 	} catch (IllegalAccessException e) {
 	    throw new IOException(e.getLocalizedMessage());
 	}
 	
 	if (System.getProperty("os.name").startsWith("Windows")) {
 	    // no check 
 	} else {
 	    String perms = (read?"r":"-") + (write?"w":"-") + (exec?"x":"-");
 	    StringBuffer out = new StringBuffer();
 	    FileUtils.Exec(new String[]{"ls", "-l", "-d", f.getPath()}, null, out);
 	    // http://www.opengroup.org/onlinepubs/000095399/utilities/ls.html
 	    Pattern p = Pattern.compile("\\s*([dbclp-])([r-][w-][SsTtx-])([r-][w-][SsTtx-])([r-][w-][SsTtx-]).*", Pattern.DOTALL);
 	    Matcher m = p.matcher(out.toString());
 	    if (!m.matches())
 		throw new IOException("Could not understand ls output: "+out);
 	    if (!m.group(2).contentEquals(perms))
 		throw new AssertionFailedError("File has wrong user permissions: "+f);
 	    if ((read||write||exec) && m.group(3).contentEquals("---")!=userOnly)
 		throw new AssertionFailedError("File has wrong group permissions: "+f);
 	    if ((read||write||exec) && m.group(4).contentEquals("---")!=userOnly)
 		throw new AssertionFailedError("File has wrong other permissions: "+f);
 	}
     }
     
     /** contents of dummy temp file of {@link #createDummyTempFile} */
     protected String dummyString = "foo bar test"+System.getProperty("line.separator");
     /** create dummy temp file containing {@link #dummyString} */
     protected File createDummyTempFile() throws IOException {
 	File tmp = File.createTempFile("test", ".tmp");
 	tmp.deleteOnExit();
 	FileWriter writer = new FileWriter(tmp);
 	writer.append(dummyString);
 	writer.close();
 	return tmp;
     }
     
     @Test public void testCopyAll1() throws Exception { copyFileTest(true, true, true, true); }
     @Test public void testCopyAll2() throws Exception { copyFileTest(true, false, true, true); }
     @Test public void testCopyAll3() throws Exception { copyFileTest(true, true, false, true); }
     @Test public void testCopyAll4() throws Exception { copyFileTest(true, false, false, true); }
     // skip tests without read permissions since copying can't be done then
     
     @Test public void testCopyUseronly1() throws Exception { copyFileTest(true, true, true, false); }
     @Test public void testCopyUseronly2() throws Exception { copyFileTest(true, false, true, false); }
     @Test public void testCopyUseronly3() throws Exception { copyFileTest(true, true, false, false); }
     @Test public void testCopyUseronly4() throws Exception { copyFileTest(true, false, false, false); }
     // skip tests without read permissions since copying can't be done then
     
     /** Windows robocopy test case where destination file name with same name as
      * source filename is present. */
     @Test
     public void testCopyRobocopyCheck1() throws Exception {
 	File src = createDummyTempFile();
 	File tmpdir = FileUtils.createTempDir("test");
 	File tmpsrc = new File(tmpdir, src.getName());
 	File tmpdst = new File(tmpdir, "foobar.tmp");
 	final String tmpsrcText = "dummy test file with same name as file copied from";
 	try {
 	    FileWriter writer = new FileWriter(tmpsrc);
 	    writer.append(tmpsrcText);
 	    writer.close();
 	    FileUtils.CopyFile(src, tmpdst);
 	    assertTrimmedEquals(dummyString, FileUtils.readFile(tmpdst));
 	    assertTrimmedEquals(tmpsrcText, FileUtils.readFile(tmpsrc));
 	} finally {
 	    src.delete();
 	    tmpsrc.delete();
 	    tmpdst.delete();
 	    tmpdir.delete();
 	}
 	
     }
 
     public void copyFileTest(boolean read, boolean write, boolean exec, boolean userOnly) throws Exception  {
 	File src = createDummyTempFile();
 	File dst = new File(src.getParentFile(), src.getName()+".copy");
 	try {
 	    FileUtils.chmod(src, read, write, exec, userOnly);
 	    FileUtils.CopyFile(src, dst);
 	    // compare permissions
 	    assertPermissions(dst, read, write, exec, userOnly);
 	    // compare contents
 	    if (read) assertEquals(dummyString, FileUtils.readFile(dst));
 	} finally {
 	    src.delete();
 	    dst.delete();
 	}
     }
     
     @Test
     public void testListFilesOnly() throws Exception {
 	File dir = FileUtils.createTempDir("test");
 	File file1 = new File(dir, "test01.tmp");
 	File file2 = new File(dir, "test02.tmp");
 	File file3 = new File(dir, "test03.tmp");
 	File filedir = new File(dir, "testdir.tmp");
 	
 	try {
 	    file1.createNewFile();
 	    file2.createNewFile();
 	    file3.createNewFile();
 	    filedir.mkdir();
 
 	    File[] files = FileUtils.listFilesOnly(dir);
 	    Arrays.sort(files);
 	    File [] wantfiles = new File[] {file1, file2, file3};
 	    Arrays.sort(wantfiles);
 
 	    assertEquals(wantfiles.length, files.length);
 	    for (int i=0; i<wantfiles.length; i++)
 		assertEquals(wantfiles[i].getCanonicalPath(), files[i].getCanonicalPath());
 	} finally {
 	    file1.delete();
 	    file2.delete();
 	    file3.delete();
 	    filedir.delete();
 	    dir.delete();
 	}
     }
 
     @Test
     public void testReadFile() throws Exception  {
 	File tmp = createDummyTempFile();
 	try {
 	    assertEquals(dummyString, FileUtils.readFile(tmp));
 	} finally {
 	    tmp.delete();
 	}
     }
     
     @Test
     public void testChmodAll() throws Exception {
 	doChmodTest(false);
     }
     @Test
     public void testChmodUseronly() throws Exception {
 	doChmodTest(true);
     }
 
     public void doChmodTest(boolean userOnly) throws Exception  {
 	File tmp = File.createTempFile("test", "tmp");
 	try {
 	    assertTrue(tmp.exists());
 	    FileUtils.chmod(tmp, true, false, false, userOnly);
 	    assertPermissions(tmp, true, false, false, userOnly);
 	    FileUtils.chmod(tmp, true, true, false, userOnly);
 	    assertPermissions(tmp, true, true, false,userOnly);
 	    FileUtils.chmod(tmp, true, true, true, userOnly);
 	    assertPermissions(tmp, true, true, true, userOnly);
 	    FileUtils.chmod(tmp, false, true, true, userOnly);
 	    assertPermissions(tmp, false, true, true, userOnly);
 	    FileUtils.chmod(tmp, false, false, true, userOnly);
 	    assertPermissions(tmp, false, false, true, userOnly);
 	    FileUtils.chmod(tmp, false, true, false, userOnly);
 	    assertPermissions(tmp, false, true, false, userOnly);
 	    FileUtils.chmod(tmp, false, false, false, userOnly);
 	    assertPermissions(tmp, false, false, false, userOnly);
 	} finally {
 	    tmp.delete();
 	}
     }
 
     @Test
     public void testCreateTempDir() throws Exception {
 	File tmpDir = FileUtils.createTempDir("foobar");
 	try {
 	    assertTrue(tmpDir.exists());
 	    assertTrue(tmpDir.isDirectory());
 	    assertPermissions(tmpDir, true, true, true, true);
 	} finally {
 	    tmpDir.delete();
 	}
 	assertFalse(tmpDir.exists());
     }
 
     @Test
     public void testExecSimple() throws Exception  {
 	String[] cmd = new String[]{"echo", "hi there"};
 	if (System.getProperty("os.name").startsWith("Windows"))
 	    cmd = new String[]{"cmd", "/C", "echo", "hi there"};
 	assertEquals(0, FileUtils.Exec(cmd));
     }
     @Test
     public void testExecSimpleFail() throws Exception {
 	try {
 	    FileUtils.Exec(new String[]{"some_nonExis.ting2283filefoo.bar.really"});
 	} catch (IOException e) {
 	    return;
 	}
 	throw new AssertionFailedError("Expected supposed nonexisting executable to fail");
     }
 
     @Test
     public void testExecRead() throws Exception {
 	StringBuffer out = new StringBuffer();
 	String[] cmd = new String[]{"echo", "hi there"};
 	if (System.getProperty("os.name").startsWith("Windows"))
	    cmd = new String[]{"cmd", "/C", "echo", "hi there"};
 	FileUtils.Exec(cmd, null, out);
 	assertTrimmedEquals("hi there", out.toString());
     }
     @Test
     public void testExecReadWrite() throws Exception {
 	// On Windows, exec has a problem that stdin isn't closed when the
 	// end of data to supply to the process's stdin is reached and hangs.
 	// So there we run a shell, and exit. On other systems just cat
 	// is enough.
 	if (!System.getProperty("os.name").startsWith("Windows")) {
 	    String[] cmd = new String[] { "cat" };
 	    StringBuffer out = new StringBuffer();
 	    FileUtils.Exec(cmd, "foo bar", out);
 	    assertTrimmedEquals("foo bar", out.toString());
     	} else {
 	    String[] cmd = new String[] { "cmd" };
 	    StringBuffer out = new StringBuffer();
 	    FileUtils.Exec(cmd, "exit"+System.getProperty("line.separator"), out);
 	    assertTrue(out.toString().startsWith("Microsoft Windows"));
     	}
     }
 }
