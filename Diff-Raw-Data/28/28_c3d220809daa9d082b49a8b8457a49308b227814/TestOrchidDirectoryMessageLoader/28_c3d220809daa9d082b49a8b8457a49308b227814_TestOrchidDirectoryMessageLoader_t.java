 /**
  * Copyright (c) 2011, Mikael Svahn, Softhouse Consulting AB
  * 
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so:
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  */
 package se.softhouse.garden.orchid.text;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.Locale;
 
 import junit.framework.Assert;
 
 import org.junit.Test;
 
 import se.softhouse.garden.orchid.commons.text.OrchidDefaultMessageCode;
 import se.softhouse.garden.orchid.commons.text.OrchidMessage;
 import se.softhouse.garden.orchid.commons.text.OrchidMessageFormat;
 import se.softhouse.garden.orchid.commons.text.storage.OrchidMessageFormatStorage;
 import se.softhouse.garden.orchid.commons.text.storage.OrchidStringMessageStorage;
 import se.softhouse.garden.orchid.commons.text.storage.provider.OrchidMessageDirectoryStorageProvider;
 import se.softhouse.garden.orchid.commons.text.storage.provider.OrchidMessageStorageCachedProvider;
 import se.softhouse.garden.orchid.text.TestOrchidMessageFormat.TestMessages;
 
 public class TestOrchidDirectoryMessageLoader {
 
 	@Test
 	public void testGetMessage() throws IOException {
 		OrchidMessageStorageCachedProvider<String> provider = new OrchidMessageDirectoryStorageProvider<String>("file:texttest");
 		OrchidStringMessageStorage dml = new OrchidStringMessageStorage(provider);
 		dml.start();
 		Assert.assertEquals("FileTest message", dml.getMessage("bundle1.msg.0"));
 		Assert.assertEquals("FileTest message", dml.getMessage("bundle1.msg.0"));
 		Assert.assertNull(dml.getMessage("bundle1.msg.0.txt"));
 		Assert.assertEquals("FileTest message\nline 2", dml.getMessage("bundle1.msg.1"));
 		Assert.assertEquals("svse", dml.getMessage("bundle1.msg.2", new Locale("sv", "SE")));
 		Assert.assertEquals("ense", dml.getMessage("bundle1.msg.2", new Locale("en", "SE")));
 		Assert.assertEquals("se", dml.getMessage("bundle1.msg.2", new Locale("de", "SE")));
 		Assert.assertEquals("se", dml.getMessage(new OrchidDefaultMessageCode("bundle1.msg.2"), new Locale("de", "SE")));
 	}
 
 	@Test
 	public void testZipGetMessage() throws IOException {
 		OrchidMessageStorageCachedProvider<String> provider = new OrchidMessageDirectoryStorageProvider<String>(
 		        "zip:https://nodeload.github.com/Softhouse/orchid/zipball/master!~Softhouse-orchid-[^/.]*/se.softhouse.garden.orchid.commons/texttest");
 		provider.setPackageStartLevel(1);
 		OrchidStringMessageStorage dml = new OrchidStringMessageStorage(provider);
 		dml.start();
 		Assert.assertEquals("FileTest message", dml.getMessage("bundle1.msg.0"));
 		Assert.assertEquals("FileTest message", dml.getMessage("bundle1.msg.0"));
 		Assert.assertNull(dml.getMessage("bundle1.msg.0.txt"));
 		Assert.assertEquals("FileTest message\nline 2", dml.getMessage("bundle1.msg.1"));
 		Assert.assertEquals("svse", dml.getMessage("bundle1.msg.2", new Locale("sv", "SE")));
 		Assert.assertEquals("ense", dml.getMessage("bundle1.msg.2", new Locale("en", "SE")));
 		Assert.assertEquals("se", dml.getMessage("bundle1.msg.2", new Locale("de", "SE")));
 		Assert.assertEquals("se", dml.getMessage(new OrchidDefaultMessageCode("bundle1.msg.2"), new Locale("de", "SE")));
 	}
 
 	@Test
 	public void testSubZipGetMessage() throws IOException {
 		OrchidMessageStorageCachedProvider<String> provider = new OrchidMessageDirectoryStorageProvider<String>(
 		        "zip:https://github.com/Softhouse/orchid/blob/master/se.softhouse.garden.orchid.commons/texttest/props/b2/text.zip?raw=true!line");
 		provider.setPackageStartLevel(1);
 		OrchidStringMessageStorage dml = new OrchidStringMessageStorage(provider);
 		dml.start();
 		Assert.assertEquals("This is line 1", dml.getMessage("1"));
 		Assert.assertEquals("This is line 2", dml.getMessage("2"));
 	}
 
 	@Test
 	public void testGetMessageGroup() throws IOException {
 		OrchidMessageStorageCachedProvider<String> provider = new OrchidMessageDirectoryStorageProvider<String>("file:texttest");
 		OrchidStringMessageStorage dml = new OrchidStringMessageStorage(provider);
 		dml.start();
 		Assert.assertEquals("0,1", dml.getMessage("bundle1.msg"));
 		Assert.assertEquals("0,1,2", dml.getMessage("bundle1.msg", new Locale("sv", "SE")));
 		Assert.assertEquals("0,1,2", dml.getMessage("bundle1.msg", new Locale("en", "SE")));
 		Assert.assertEquals("0,1,2", dml.getMessage("bundle1.msg", new Locale("de", "SE")));
 	}
 
 	@Test
 	public void testGetMessageCache() throws IOException {
 		Locale.setDefault(Locale.US);
 		OrchidMessageStorageCachedProvider<String> provider = new OrchidMessageDirectoryStorageProvider<String>("file:texttest");
 		OrchidStringMessageStorage dml = new OrchidStringMessageStorage(provider);
 		dml.start();
 		File file1 = new File("texttest/bundle1/cache/cache");
 		File file2 = new File("texttest/bundle1/cache/cache_en_us");
 		file1.delete();
 		file2.delete();
 		provider.reload(true);
 		Assert.assertNull(dml.getMessage("bundle1.cache.cache"));
 		writeFileContent("This is a test", file1);
 		provider.reload(true);
 		Assert.assertEquals("This is a test", dml.getMessage("bundle1.cache.cache"));
 		writeFileContent("This is a test 2", file1);
 		provider.reload(true);
 		Assert.assertEquals("This is a test 2", dml.getMessage("bundle1.cache.cache"));
 		writeFileContent("This is a test 3", file2);
 		provider.setCacheSeconds(-1);
 		provider.reload(false);
 		Assert.assertEquals("This is a test 2", dml.getMessage("bundle1.cache.cache"));
 		dml.stop();
 		dml.start();
 		Assert.assertEquals("This is a test 3", dml.getMessage("bundle1.cache.cache"));
 		Assert.assertEquals("This is a test 2", dml.getMessage("bundle1.cache.cache", new Locale("se")));
 		Assert.assertEquals("cache", dml.getMessage("bundle1.cache"));
 		dml.stop();
 	}
 
 	@Test
 	public void testReloadThread() throws IOException, InterruptedException {
 		Locale.setDefault(Locale.US);
 		OrchidMessageDirectoryStorageProvider<String> provider = new OrchidMessageDirectoryStorageProvider<String>("file:texttest");
 		provider.setWatchUrl("file:texttest");
 		OrchidStringMessageStorage dml = new OrchidStringMessageStorage(provider);
 		provider.setCacheMillis(500);
 		File root = new File("texttest");
 		File watchfile = new File("texttest/watchfile");
 		File file1 = new File("texttest/bundle1/thread/1.txt");
 		File file2 = new File("texttest/bundle1/thread/2.txt");
 		file1.delete();
 		file2.delete();
 		Assert.assertNull(dml.getMessage("bundle1.cache.cache"));
 		writeFileContent("Test1A", file1);
 		writeFileContent("Test2A", file2);
 		dml.start();
 		Assert.assertEquals("Test1A", dml.getMessage("bundle1.thread.1"));
 		Assert.assertEquals("Test2A", dml.getMessage("bundle1.thread.2"));
 		writeFileContent("Test1B", file1);
 		writeFileContent("Test2B", file2);
 		root.setLastModified(System.currentTimeMillis());
 		Thread.sleep(1000);
 		Assert.assertEquals("Test1B", dml.getMessage("bundle1.thread.1"));
 		Assert.assertEquals("Test2B", dml.getMessage("bundle1.thread.2"));
 		writeFileContent("Test1C", file1);
 		root.setLastModified(System.currentTimeMillis());
 		Thread.sleep(1000);
 		Assert.assertEquals("Test1C", dml.getMessage("bundle1.thread.1"));
 		Assert.assertEquals("Test2B", dml.getMessage("bundle1.thread.2"));
 		writeFileContent("A", watchfile);
 		provider.setWatchUrl("file:texttest/watchfile");
 		provider.setWatchContent(true);
 		writeFileContent("Test1D", file1);
 		Thread.sleep(1000);
 		Assert.assertEquals("Test1D", dml.getMessage("bundle1.thread.1"));
 		Assert.assertEquals("Test2B", dml.getMessage("bundle1.thread.2"));
 		writeFileContent("Test1E", file1);
 		Thread.sleep(1000);
 		Assert.assertEquals("Test1D", dml.getMessage("bundle1.thread.1"));
 		Assert.assertEquals("Test2B", dml.getMessage("bundle1.thread.2"));
 		writeFileContent("B", watchfile);
 		Thread.sleep(1000);
 		Assert.assertEquals("Test1E", dml.getMessage("bundle1.thread.1"));
 		Assert.assertEquals("Test2B", dml.getMessage("bundle1.thread.2"));
 	}
 
 	@Test
 	public void testFunctions() throws IOException {
 		Locale.setDefault(Locale.US);
 		OrchidMessageStorageCachedProvider<OrchidMessageFormat> provider = new OrchidMessageDirectoryStorageProvider<OrchidMessageFormat>("file:texttest");
 		OrchidMessageFormatStorage dml = new OrchidMessageFormatStorage(provider);
 		dml.start();
 		Assert.assertEquals("The base text and the embedded: Default Hi", dml.getMessage("func.base").format(OrchidMessage.arg("msg", "Hi")));
 		Assert.assertEquals("The base text and the embedded: SV Hi", dml.getMessage("func.base", new Locale("sv", "SE")).format(OrchidMessage.arg("msg", "Hi")));
 	}
 
 	@Test
 	public void testChoice() throws IOException {
 		Locale.setDefault(Locale.US);
 		OrchidMessageStorageCachedProvider<OrchidMessageFormat> provider = new OrchidMessageDirectoryStorageProvider<OrchidMessageFormat>("file:texttest");
 		OrchidMessageFormatStorage dml = new OrchidMessageFormatStorage(provider);
 		dml.start();
 		Assert.assertEquals("The base text and the choice: Type 1", dml.getMessage("choice.base").format(OrchidMessage.arg("type", 1)));
 		Assert.assertEquals("The base text and the choice: Type 2", dml.getMessage("choice.base").format(OrchidMessage.arg("type", 2)));
 		Assert.assertEquals("The base text and the choice: TYPE 5", dml.getMessage("choice.base").format(OrchidMessage.arg("type", 5)));
 	}
 
 	@Test
 	public void testPopeties() throws IOException {
 		Locale.setDefault(Locale.US);
 		OrchidMessageStorageCachedProvider<OrchidMessageFormat> provider = new OrchidMessageDirectoryStorageProvider<OrchidMessageFormat>("file:texttest");
 		OrchidMessageFormatStorage dml = new OrchidMessageFormatStorage(provider);
 		dml.start();
 		Assert.assertEquals("This is the title", dml.getMessage("props.b1.text.title").format(OrchidMessage.arg("type", 1)));
 	}
 
 	@Test
 	public void testMessageSourceLocale() throws IOException {
 		Locale.setDefault(Locale.US);
 		OrchidMessageDirectoryStorageProvider<OrchidMessageFormat> provider = new OrchidMessageDirectoryStorageProvider<OrchidMessageFormat>();
 		OrchidMessageFormatStorage dml = new OrchidMessageFormatStorage(provider);
 		provider.setUrl("file:texttest/test");
 		dml.start();
 		Assert.assertEquals("default1", dml.getMessage(TestMessages.LOCAL_1, Locale.getDefault()).format(null));
 		Assert.assertEquals("default2", dml.getMessage(TestMessages.LOCAL_2, Locale.getDefault()).format(null));
 		Assert.assertEquals("default1", dml.getMessage(TestMessages.LOCAL_1, new Locale("se")).format(null));
 		Assert.assertEquals("se2", dml.getMessage(TestMessages.LOCAL_2, new Locale("se")).format(null));
 		Assert.assertEquals("se3", dml.getMessage(TestMessages.LOCAL_3, new Locale("se")).format(null));
 		Assert.assertEquals("se4", dml.getMessage(TestMessages.LOCAL_4, new Locale("se")).format(null));
 		Assert.assertEquals("default1", dml.getMessage(TestMessages.LOCAL_1, new Locale("se", "sv")).format(null));
 		Assert.assertEquals("se2", dml.getMessage(TestMessages.LOCAL_2, new Locale("se", "sv")).format(null));
 		Assert.assertEquals("se_sv3", dml.getMessage(TestMessages.LOCAL_3, new Locale("se", "sv")).format(null));
 		Assert.assertEquals("se_sv4", dml.getMessage(TestMessages.LOCAL_4, new Locale("se", "sv")).format(null));
 		Assert.assertEquals("default1", dml.getMessage(TestMessages.LOCAL_1, new Locale("se", "sv", "sms")).format(null));
 		Assert.assertEquals("se2", dml.getMessage(TestMessages.LOCAL_2, new Locale("se", "sv", "sms")).format(null));
 		Assert.assertEquals("se_sv3", dml.getMessage(TestMessages.LOCAL_3, new Locale("se", "sv", "sms")).format(null));
 		Assert.assertEquals("se_sv_sms4", dml.getMessage(TestMessages.LOCAL_4, new Locale("se", "sv", "sms")).format(null));
 		Assert.assertEquals("email", dml.getMessage(TestMessages.LOCAL_4, new Locale("se", "", "email")).format(null));
 	}
 
 	@Test
 	public void testZip() throws IOException {
 		Locale.setDefault(Locale.US);
 		OrchidMessageStorageCachedProvider<OrchidMessageFormat> provider = new OrchidMessageDirectoryStorageProvider<OrchidMessageFormat>("file:texttest");
 		OrchidMessageFormatStorage dml = new OrchidMessageFormatStorage(provider);
 		dml.start();
 		Assert.assertEquals("This is the title", dml.getMessage("props.b2.text.title").format(OrchidMessage.arg("type", 1)));
 		Assert.assertEquals("This is line 1", dml.getMessage("props.b2.text.line.1").format(OrchidMessage.arg("type", 1)));
 		Assert.assertEquals("This is line 2", dml.getMessage("props.b2.text.line.2").format(OrchidMessage.arg("type", 1)));
 		Assert.assertEquals("This is line 3", dml.getMessage("props.b2.text.line.3").format(OrchidMessage.arg("type", 1)));
 		Assert.assertEquals("This is line 4", dml.getMessage("props.b2.text.line.4").format(OrchidMessage.arg("type", 1)));
 	}
 
 	private void writeFileContent(String text, File file) throws FileNotFoundException, IOException {
 		FileOutputStream fos = new FileOutputStream(file);
 		fos.write(text.getBytes());
 		fos.close();
 	}
 
 	@Test
 	public void testMultiDirs() throws IOException {
 		Locale.setDefault(Locale.US);
 		OrchidMessageDirectoryStorageProvider<OrchidMessageFormat> provider = new OrchidMessageDirectoryStorageProvider<OrchidMessageFormat>();
 		OrchidMessageFormatStorage dml = new OrchidMessageFormatStorage(provider);
 		provider.setUrls(new String[] { "file:texttest/test", "file:texttest/props" });
 		dml.start();
 		Assert.assertEquals("default1", dml.getMessage(TestMessages.LOCAL_1, Locale.getDefault()).format(null));
 		Assert.assertEquals("This is the title", dml.getMessage("b2.text.title").format(OrchidMessage.arg("type", 1)));
 	}
 
 	@Test
 	public void testDirLevel() throws IOException {
 		Locale.setDefault(Locale.US);
 		OrchidMessageDirectoryStorageProvider<OrchidMessageFormat> provider = new OrchidMessageDirectoryStorageProvider<OrchidMessageFormat>();
 		OrchidMessageFormatStorage dml = new OrchidMessageFormatStorage(provider);
 		provider.setUrls(new String[] { "file:texttest" });
 		provider.setPackageStartLevel(2);
 		dml.start();
 		Assert.assertEquals("default1", dml.getMessage(TestMessages.LOCAL_1, Locale.getDefault()).format(null));
 		Assert.assertEquals("This is the title", dml.getMessage("b2.text.title").format(OrchidMessage.arg("type", 1)));
 	}
 
 	@Test
 	public void testDirLevelWithZip() throws IOException {
 		Locale.setDefault(Locale.US);
 		OrchidMessageDirectoryStorageProvider<OrchidMessageFormat> provider = new OrchidMessageDirectoryStorageProvider<OrchidMessageFormat>();
 		OrchidMessageFormatStorage dml = new OrchidMessageFormatStorage(provider);
 		provider.setUrls(new String[] { "file:texttest/props", "file:texttest/test" });
 		provider.setPackageStartLevel(3);
 		dml.start();
 		Assert.assertEquals("This is line 1", dml.getMessage("line.1").format(OrchidMessage.arg("type", 1)));
 		Assert.assertEquals("This is the title", dml.getMessage("title").format(OrchidMessage.arg("type", 1)));
 	}

 }
