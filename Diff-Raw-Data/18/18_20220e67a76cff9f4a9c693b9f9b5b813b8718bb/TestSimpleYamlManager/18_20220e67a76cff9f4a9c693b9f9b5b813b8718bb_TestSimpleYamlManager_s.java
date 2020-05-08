 /*
  * This file is part of CraftCommons.
  *
  * Copyright (c) 2011 CraftFire <http://www.craftfire.com/>
  * CraftCommons is licensed under the GNU Lesser General Public License.
  *
  * CraftCommons is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * CraftCommons is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.craftfire.commons.yaml;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertNotSame;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.assertSame;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 import static org.mockito.Matchers.anyObject;
 import static org.mockito.Matchers.anyString;
 import static org.mockito.Matchers.argThat;
 import static org.mockito.Matchers.eq;
 import static org.mockito.Matchers.isA;
 import static org.mockito.Mockito.*;
 
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.Reader;
 import java.io.StringReader;
 import java.io.UnsupportedEncodingException;
 import java.lang.reflect.Field;
 import java.nio.CharBuffer;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 import java.util.Set;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.ArgumentMatcher;
 import org.yaml.snakeyaml.DumperOptions;
 import org.yaml.snakeyaml.Yaml;
 import org.yaml.snakeyaml.constructor.BaseConstructor;
 import org.yaml.snakeyaml.representer.Representer;
 import org.yaml.snakeyaml.resolver.Resolver;
 
 import com.craftfire.commons.util.LoggingManager;
 
 public class TestSimpleYamlManager {
     private static Random rnd = new Random();
     private SimpleYamlManager manager;
 
     @Before
     public void setup() throws IOException {
         this.manager = new SimpleYamlManager(new StringReader(""));
     }
 
     @Test
     public void testRootRedirects() throws YamlException, NoSuchFieldException, IllegalAccessException {
         boolean randomBool = rnd.nextBoolean();
         int randomInt = rnd.nextInt();
         long randomLong = rnd.nextLong();
         byte[] randomBytes = new byte[8];
         rnd.nextBytes(randomBytes);
         String randomString = new String(randomBytes);
 
         YamlNode mock0 = mock(YamlNode.class);
         YamlNode mock1 = mock(YamlNode.class);
         Field f = this.manager.getClass().getDeclaredField("root");
         f.setAccessible(true);
         f.set(this.manager, mock0);
 
         stub(mock0.getFinalNodeCount()).toReturn(randomInt);
         assertEquals(randomInt, this.manager.getFinalNodeCount());
         verify(mock0).getFinalNodeCount();
 
         stub(mock0.hasNode("bob.has.a.dog")).toReturn(randomBool).toReturn(true);
         assertEquals(randomBool, this.manager.exist("bob.has.a.dog"));
         verify(mock0).hasNode("bob.has.a.dog");
 
         stub(mock0.getNode("bob.has.a.dog")).toReturn(mock1);
         assertSame(mock1, this.manager.getNode("bob.has.a.dog"));
         verify(mock0).getNode("bob.has.a.dog");
 
         stub(mock1.getBool(true)).toReturn(!randomBool);
         assertEquals(!randomBool, this.manager.getBoolean("bob.has.a.dog", true));
         verify(mock1).getBool(true);
 
         stub(mock1.getString("blah")).toReturn(randomString);
         assertEquals(randomString, this.manager.getString("bob.has.a.dog", "blah"));
         verify(mock1).getString("blah");
 
         stub(mock1.getInt((int) randomLong)).toReturn(randomInt);
         assertEquals(randomInt, this.manager.getInt("bob.has.a.dog", (int) randomLong));
         verify(mock1).getInt((int) randomLong);
 
         stub(mock1.getLong(randomInt)).toReturn(randomLong);
         assertEquals(randomLong, this.manager.getLong("bob.has.a.dog", randomInt));
         verify(mock1).getLong(randomInt);
 
         stub(mock0.getNode("bob.has.a.dog", true)).toReturn(mock1);
         this.manager.setNode("bob.has.a.dog", "test" + randomInt);
         verify(mock0).getNode("bob.has.a.dog", true);
         verify(mock1).setValue("test" + randomInt);
 
         verify(mock0, times(5)).getNode("bob.has.a.dog");
         verify(mock0, times(5)).hasNode("bob.has.a.dog");
 
         stub(mock0.getNode("bob.has.a.cat")).toThrow(new YamlException());
         stub(mock0.hasNode("bob.has.a.cat")).toReturn(true);
         assertEquals(randomBool, this.manager.getBoolean("bob.has.a.cat", randomBool));
         assertEquals(randomString, this.manager.getString("bob.has.a.cat", randomString));
         assertEquals(randomInt, this.manager.getInt("bob.has.a.cat", randomInt));
         assertEquals(randomLong, this.manager.getLong("bob.has.a.cat", randomLong));
 
         verify(mock0, times(4)).getNode("bob.has.a.cat");
         verify(mock0, times(4)).hasNode("bob.has.a.cat");
 
         stub(mock0.hasNode("bob.has.a.cat")).toReturn(false);
         assertEquals(randomBool, this.manager.getBoolean("bob.has.a.mouse", randomBool));
         assertEquals(randomString, this.manager.getString("bob.has.a.mouse", randomString));
         assertEquals(randomInt, this.manager.getInt("bob.has.a.mouse", randomInt));
         assertEquals(randomLong, this.manager.getLong("bob.has.a.mouse", randomLong));
         verify(mock0, times(4)).hasNode("bob.has.a.mouse");
 
         YamlManager mgrMock = mock(YamlManager.class);
         stub(mgrMock.getRootNode()).toReturn(mock1);
         @SuppressWarnings("unchecked")
         List<YamlNode> list = mock(List.class);
         stub(mock1.getChildrenList()).toReturn(list);
 
         this.manager.addNodes(mgrMock);
         verify(mock0).addChildren(list);
         verify(mgrMock, atLeastOnce()).getRootNode();
         verify(mock1).getChildrenList();
 
         @SuppressWarnings("unchecked")
         Map<String, Object> map = mock(Map.class);
         this.manager.addNodes(map);
         verify(mock0).addChildren(map);
     }
 
     @Test
     public void testDefaults() {
         boolean randomBool = rnd.nextBoolean();
         int randomInt = rnd.nextInt();
         long randomLong = rnd.nextLong();
         byte[] randomBytes = new byte[8];
         rnd.nextBytes(randomBytes);
         String randomString = new String(randomBytes);
 
         SimpleYamlManager spy = spy(this.manager);
 
         doReturn(randomBool).when(spy).getBoolean("bob.has.no.cat", false);
         assertEquals(randomBool, spy.getBoolean("bob.has.no.cat"));
         verify(spy).getBoolean("bob.has.no.cat", false);
 
         doReturn(randomString).when(spy).getString("bob.has.no.cat", null);
         assertEquals(randomString, spy.getString("bob.has.no.cat"));
         verify(spy).getString("bob.has.no.cat", null);
 
         doReturn(randomInt).when(spy).getInt("bob.has.no.cat", 0);
         assertEquals(randomInt, spy.getInt("bob.has.no.cat"));
         verify(spy).getInt("bob.has.no.cat", 0);
 
         doReturn(randomLong).when(spy).getLong("bob.has.no.cat", 0);
         assertEquals(randomLong, spy.getLong("bob.has.no.cat"));
         verify(spy).getLong("bob.has.no.cat", 0);
     }
 
     @Test
     public void testRootNode() {
         Object mockValue = mock(Object.class);
         SimpleYamlManager mockMgr = mock(SimpleYamlManager.class);
         YamlNode node = new YamlNode(mockMgr, "test", mockValue);
 
         this.manager.setRootNode(node);
         YamlNode root = this.manager.getRootNode();
 
         assertNotSame(node, root);
         assertEquals(mockValue, root.getValue());
         assertNull(root.getName());
         assertSame(this.manager, root.getYamlManager());
         assertNull(root.getParent());
     }
 
     @Test
     public void testLogger() throws NoSuchFieldException, IllegalAccessException {
         LoggingManager logger = mock(LoggingManager.class);
         this.manager.setLoggingManager(logger);
         assertSame(logger, this.manager.getLogger());
 
         try {
             this.manager.setLoggingManager(null);
             fail("Expected an IllegalArgumentException");
         } catch (IllegalArgumentException e) {
         }
     }
 
     @Test
     public void testConstructors1() throws IllegalAccessException, NoSuchFieldException {
         Settings defaults = new Settings();
         Settings mocked = prepareSettings();
         File file = mock(File.class);
         Set<File> files = new HashSet<File>();
         files.add(file);
 
         SimpleYamlManager mgr = new SimpleYamlManager(file);
         assertEquals(files, mgr.getFiles());
         assertNull(mgr.getResource());
         assertSame(defaults.getLogger().getLogger(), mgr.getLogger().getLogger());
         assertEquals(defaults.getLogger().getPrefix(), mgr.getLogger().getPrefix());
         assertEquals(defaults.getSeparator(), mgr.getSeparator());
         assertEquals(defaults.isCaseSensitive(), mgr.isCaseSensitive());
         // TODO: Multi-document will be added here
         compareYaml(mgr.yaml, defaults.getResolver(), defaults.getRepresenter(), defaults.getConstructor(), defaults.getDumperOptions());
 
         mgr = new SimpleYamlManager(file, mocked);
         assertEquals(files, mgr.getFiles());
         assertNull(mgr.getResource());
         assertSame(mocked.getLogger(), mgr.getLogger());
         assertEquals(mocked.getSeparator(), mgr.getSeparator());
         assertEquals(mocked.isCaseSensitive(), mgr.isCaseSensitive());
         // TODO: Multi-document will be added here
         assertSame(mocked.createYaml(), mgr.yaml);
     }
 
     @Test
     public void testConstructors2() throws IllegalAccessException, NoSuchFieldException {
         Settings defaults = new Settings();
         Settings mocked = prepareSettings();
         String resource = "path/to/david";
 
         SimpleYamlManager mgr = new SimpleYamlManager(resource);
         assertTrue(mgr.getFiles().isEmpty());
         assertEquals(resource, mgr.getResource());
         assertSame(defaults.getLogger().getLogger(), mgr.getLogger().getLogger());
         assertEquals(defaults.getLogger().getPrefix(), mgr.getLogger().getPrefix());
         assertEquals(defaults.getSeparator(), mgr.getSeparator());
         assertEquals(defaults.isCaseSensitive(), mgr.isCaseSensitive());
         // TODO: Multi-document will be added here
         compareYaml(mgr.yaml, defaults.getResolver(), defaults.getRepresenter(), defaults.getConstructor(), defaults.getDumperOptions());
 
         mgr = new SimpleYamlManager(resource, mocked);
         assertTrue(mgr.getFiles().isEmpty());
         assertEquals(resource, mgr.getResource());
         assertSame(mocked.getLogger(), mgr.getLogger());
         assertEquals(mocked.getSeparator(), mgr.getSeparator());
         assertEquals(mocked.isCaseSensitive(), mgr.isCaseSensitive());
         // TODO: Multi-document will be added here
         assertSame(mocked.createYaml(), mgr.yaml);
     }
 
     @Test
     public void testConstructors3() throws IllegalAccessException, NoSuchFieldException, IOException {
         Settings defaults = new Settings();
         Settings mocked = prepareSettings();
         Reader reader = mock(Reader.class);
 
         Field f = SimpleYamlManager.class.getDeclaredField("reader");
         f.setAccessible(true);
 
         SimpleYamlManager mgr = new SimpleYamlManager(reader);
         assertTrue(mgr.getFiles().isEmpty());
         assertNull(mgr.getResource());
         assertSame(reader, f.get(mgr));
         assertSame(defaults.getLogger().getLogger(), mgr.getLogger().getLogger());
         assertEquals(defaults.getLogger().getPrefix(), mgr.getLogger().getPrefix());
         assertEquals(defaults.getSeparator(), mgr.getSeparator());
         assertEquals(defaults.isCaseSensitive(), mgr.isCaseSensitive());
         // TODO: Multi-document will be added here
         compareYaml(mgr.yaml, defaults.getResolver(), defaults.getRepresenter(), defaults.getConstructor(), defaults.getDumperOptions());
 
         mgr = new SimpleYamlManager(reader, mocked);
         assertTrue(mgr.getFiles().isEmpty());
         assertNull(mgr.getResource());
         assertSame(reader, f.get(mgr));
         assertSame(mocked.getLogger(), mgr.getLogger());
         assertEquals(mocked.getSeparator(), mgr.getSeparator());
         assertEquals(mocked.isCaseSensitive(), mgr.isCaseSensitive());
         // TODO: Multi-document will be added here
         assertSame(mocked.createYaml(), mgr.yaml);
     }
 
     @Test
     public void testConstructors4() throws IllegalAccessException, NoSuchFieldException, IOException {
         Settings defaults = new Settings();
         Settings mocked = prepareSettings();
         String testStr = "testąóĸ";
         InputStream stream = new ByteArrayInputStream(testStr.getBytes("UTF-8"));
 
         Field f = SimpleYamlManager.class.getDeclaredField("reader");
         f.setAccessible(true);
 
         SimpleYamlManager mgr = new SimpleYamlManager(stream);
         assertTrue(mgr.getFiles().isEmpty());
         assertNull(mgr.getResource());
         CharBuffer buf = CharBuffer.allocate(testStr.length());
         assertEquals(testStr.length(), ((Reader) f.get(mgr)).read(buf));
         buf.flip();
         assertEquals(testStr, buf.toString());
         assertSame(defaults.getLogger().getLogger(), mgr.getLogger().getLogger());
         assertEquals(defaults.getLogger().getPrefix(), mgr.getLogger().getPrefix());
         assertEquals(defaults.getSeparator(), mgr.getSeparator());
         assertEquals(defaults.isCaseSensitive(), mgr.isCaseSensitive());
         // TODO: Multi-document will be added here
         compareYaml(mgr.yaml, defaults.getResolver(), defaults.getRepresenter(), defaults.getConstructor(), defaults.getDumperOptions());
 
         stream = new ByteArrayInputStream(testStr.getBytes("UTF-8"));
         mgr = new SimpleYamlManager(stream, mocked);
         assertTrue(mgr.getFiles().isEmpty());
         assertNull(mgr.getResource());
         buf = CharBuffer.allocate(testStr.length());
         assertEquals(testStr.length(), ((Reader) f.get(mgr)).read(buf));
         buf.flip();
         assertEquals(testStr, buf.toString());
         assertSame(mocked.getLogger(), mgr.getLogger());
         assertEquals(mocked.getSeparator(), mgr.getSeparator());
         assertEquals(mocked.isCaseSensitive(), mgr.isCaseSensitive());
         // TODO: Multi-document will be added here
         assertSame(mocked.createYaml(), mgr.yaml);
     }
 
     @Test
     public void testSave() throws NoSuchFieldException, IllegalAccessException {
         assertFalse(this.manager.save());
 
         LoggingManager logger = new LoggingManager("CraftFire.YamlManager", "[YamlManager]");
         Yaml yaml = mock(Yaml.class);
         Settings settings = mock(Settings.class);
         stub(settings.createYaml()).toReturn(yaml);
         stub(settings.getLogger()).toReturn(logger);
         File file = mock(File.class);
        stub(file.getPath()).toReturn("target/test/tmp" + rnd.nextInt());
         YamlNode root = mock(YamlNode.class);
         stub(root.dump()).toReturn("fox: true");
         Field f = SimpleYamlManager.class.getDeclaredField("root");
         f.setAccessible(true);
 
         SimpleYamlManager mgr = new SimpleYamlManager(file, settings);
         f.set(mgr, root);
         assertTrue(mgr.save());
         verify(root).dump();
         verify(yaml).dump(eq("fox: true"), isA(FileWriter.class));
 
         logger = mock(LoggingManager.class);
         mgr.setLoggingManager(logger);
         stub(file.getPath()).toReturn("");
 
         assertFalse(mgr.save());
         verify(root, times(2)).dump();
         verify(yaml, times(1)).dump(eq("fox: true"), isA(FileWriter.class));
         verify(logger).stackTrace(isA(IOException.class));
     }
 
     @Test
     public void testLoad() throws IOException, NoSuchFieldException, IllegalAccessException {
         SimpleYamlManager mgr = spy(this.manager);
         Field readerF = SimpleYamlManager.class.getDeclaredField("reader");
         readerF.setAccessible(true);
 
         LoggingManager logger = mock(LoggingManager.class);
         mgr.setLoggingManager(logger);
 
         doNothing().when(mgr).load(isA(File.class));
         doNothing().when(mgr).load(isA(Reader.class));
         doNothing().when(mgr).load(anyString());
 
         assertNotNull(readerF.get(mgr));
         assertTrue(mgr.load());
         verify(mgr).load(isA(Reader.class));
         assertNull(readerF.get(mgr));
 
         Field resourceF = SimpleYamlManager.class.getDeclaredField("resource");
         resourceF.setAccessible(true);
         resourceF.set(mgr, "path/to/resource");
 
         assertTrue(mgr.load());
         assertNotNull(resourceF.get(mgr));
         verify(mgr).load("path/to/resource");
 
         IOException e = new IOException();
         doThrow(e).when(mgr).load("path/to/resource");
 
         assertFalse(mgr.load());
         verify(mgr, times(2)).load("path/to/resource");
         verify(logger).stackTrace(e);
 
         resourceF.set(mgr, null);
 
         File file = mock(File.class);
         Field fileF = SimpleYamlManager.class.getDeclaredField("file");
         fileF.setAccessible(true);
         fileF.set(mgr, file);
 
         assertTrue(mgr.load());
         assertNotNull(fileF.get(mgr));
         verify(mgr).load(file);
 
         e = new IOException();
         doThrow(e).when(mgr).load(isA(File.class));
 
         assertFalse(mgr.load());
         verify(mgr, times(2)).load(file);
         verify(logger).stackTrace(e);
 
         fileF.set(mgr, null);
         assertFalse(mgr.load());
 
         // Make sure it didn't call more any of those.
         verify(mgr, times(2)).load(file);
         verify(mgr, times(2)).load("path/to/resource");
         verify(mgr).load(isA(Reader.class));
         verify(logger, times(2)).stackTrace(isA(Exception.class));
     }
 
     @Test
     public void testLoadFromReader() {
         int randomInt = rnd.nextInt();
         String testString = "test" + randomInt + "ąęłĸ¶þ→µ";
         Reader reader = new StringReader(testString);
         Object obj = mock(Object.class);
 
         LoggingManager logger = new LoggingManager("CraftFire.YamlManager", "[YamlManager]");
         Yaml yaml = mock(Yaml.class);
         Settings settings = mock(Settings.class);
         stub(settings.createYaml()).toReturn(yaml);
         stub(settings.getLogger()).toReturn(logger);
         stub(yaml.load(argThat(new IsReaderThatContains(testString)))).toReturn(obj);
 
         SimpleYamlManager mgr = new SimpleYamlManager("", settings);
         mgr.load(reader);
         assertSame(obj, mgr.getRootNode().getValue());
         verify(yaml).load(isA(Reader.class));
     }
 
     @Test
     public void testLoadFromStream() throws UnsupportedEncodingException {
         int randomInt = rnd.nextInt();
         String testString = "test" + randomInt + "ąęłĸ¶þ→µ";
         InputStream stream = new ByteArrayInputStream(testString.getBytes("UTF-8"));
         SimpleYamlManager mgr = spy(this.manager);
 
         doNothing().when(mgr).load(isA(Reader.class));
         mgr.load(stream);
         verify(mgr).load(argThat(new IsReaderThatContains(testString)));
     }
 
     @Test
     public void testLoadFromResource() throws IOException {
         String fileContent = "Some UTF-8 ąęłĸóþ¶↓µ";
         String filePath = "testresource.txt";
 
         SimpleYamlManager mgr = spy(this.manager);
 
         doNothing().when(mgr).load(isA(InputStream.class));
         mgr.load(filePath);
         verify(mgr).load(argThat(new IsStreamThatContains(fileContent.getBytes("UTF-8"))));
     }
 
     @Test
    public void testLoadFromFile() throws IOException {
         String fileContent = "Some UTF-8 ąęłĸóþ¶↓µ";
         String filePath = "./target/test-classes/testresource.txt";
 
         LoggingManager logger = mock(LoggingManager.class);
         SimpleYamlManager mgr = spy(this.manager);
 
         mgr.setLoggingManager(logger);
         mgr.load((File) null);
         verify(mgr, never()).load(isA(InputStream.class));
         verify(mgr, never()).load(isA(Reader.class));
 
         doNothing().when(mgr).load(isA(InputStream.class));
 
         File file = mock(File.class);
         stub(file.getPath()).toReturn(filePath);
         stub(file.exists()).toReturn(true);
         mgr.load(file);
         verify(mgr).load(argThat(new IsStreamThatContains(fileContent.getBytes("UTF-8"))));
 
         stub(file.exists()).toReturn(false);
         stub(file.getParentFile()).toReturn(null);
         mgr.load(file);
         verify(file).createNewFile();
         verify(mgr).load(argThat(new IsStreamThatContains(fileContent.getBytes("UTF-8"))));
 
         File file1 = mock(File.class);
         stub(file.getParentFile()).toReturn(file1);
         mgr.load(file);
         verify(file1).mkdirs();
         verify(file, times(2)).createNewFile();
         verify(mgr).load(argThat(new IsStreamThatContains(fileContent.getBytes("UTF-8"))));
 
         stub(file.createNewFile()).toThrow(new IOException());
         stub(file.getParentFile()).toReturn(null);
         mgr.load(file);
         verify(file, times(3)).createNewFile();
         verify(mgr, times(3)).load((InputStream) anyObject());
     }
 
     class IsReaderThatContains extends ArgumentMatcher<Reader> {
         private final String string;
         private List<Reader> readers = new ArrayList<Reader>();
 
         public IsReaderThatContains(String string) {
             this.string = string;
         }
 
         @Override
         public boolean matches(Object argument) {
             if (argument instanceof Reader) {
                 if (this.readers.contains(argument)) {
                     return true;
                 }
                 char[] buf = new char[this.string.length()];
                 try {
                     ((Reader) argument).read(buf);
                     if (new String(buf).equals(this.string)) {
                         this.readers.add((Reader) argument);
                         return true;
                     }
                 } catch (IOException e) {
                     e.printStackTrace();
                 }
             }
             return false;
         }
     }
 
     class IsStreamThatContains extends ArgumentMatcher<InputStream> {
         private final byte[] bytes;
         private List<InputStream> streams = new ArrayList<InputStream>();
 
         public IsStreamThatContains(byte[] bytes) {
             this.bytes = bytes;
         }
 
         @Override
         public boolean matches(Object argument) {
             if (argument instanceof InputStream) {
                 if (this.streams.contains(argument)) {
                     return true;
                 }
                 byte[] buf = new byte[this.bytes.length];
                 try {
                     ((InputStream) argument).read(buf);
                     if (Arrays.equals(buf, this.bytes)) {
                         this.streams.add((InputStream) argument);
                         return true;
                     }
                 } catch (IOException e) {
                     e.printStackTrace();
                 }
             }
             return false;
         }
     }
 
     private Settings prepareSettings() {
         boolean randomBool = rnd.nextBoolean();
         Settings settings = mock(Settings.class);
 
         stub(settings.isCaseSensitive()).toReturn(randomBool);
         stub(settings.isMultiDocument()).toReturn(!randomBool);
         stub(settings.getSeparator()).toReturn("--sep--");
 
         LoggingManager logger = mock(LoggingManager.class);
         stub(settings.getLogger()).toReturn(logger);
 
         Yaml yaml = mock(Yaml.class);
         stub(settings.createYaml()).toReturn(yaml);
 
         return settings;
     }
 
     private void compareYaml(Yaml yaml, Resolver resolver, Representer representer, BaseConstructor constructor, DumperOptions options) throws IllegalAccessException, NoSuchFieldException {
         Field resolverF = Yaml.class.getDeclaredField("resolver");
         Field constructorF = Yaml.class.getDeclaredField("constructor");
         Field representerF = Yaml.class.getDeclaredField("representer");
         Field optionsF = Yaml.class.getDeclaredField("dumperOptions");
 
         resolverF.setAccessible(true);
         constructorF.setAccessible(true);
         representerF.setAccessible(true);
         optionsF.setAccessible(true);
 
         assertEquals(resolver.getClass(), resolverF.get(yaml).getClass());
         assertEquals(constructor.getClass(), constructorF.get(yaml).getClass());
         assertEquals(representer.getClass(), representerF.get(yaml).getClass());
 
         DumperOptions options1 = (DumperOptions) optionsF.get(yaml);
         assertEquals(options.getDefaultFlowStyle(), options1.getDefaultFlowStyle());
         assertEquals(options.getDefaultScalarStyle(), options1.getDefaultScalarStyle());
         assertEquals(options.getIndent(), options1.getIndent());
         assertEquals(options.getLineBreak(), options1.getLineBreak());
         assertEquals(options.getTags(), options1.getTags());
         assertEquals(options.getTimeZone(), options1.getTimeZone());
         assertEquals(options.getVersion(), options1.getVersion());
         assertEquals(options.getWidth(), options1.getWidth());
         assertEquals(options.isAllowReadOnlyProperties(), options1.isAllowReadOnlyProperties());
         assertEquals(options.isAllowUnicode(), options1.isAllowUnicode());
         assertEquals(options.isCanonical(), options1.isCanonical());
         assertEquals(options.isExplicitEnd(), options1.isExplicitEnd());
         assertEquals(options.isExplicitStart(), options1.isExplicitStart());
         assertEquals(options.isPrettyFlow(), options1.isPrettyFlow());
     }
 }
