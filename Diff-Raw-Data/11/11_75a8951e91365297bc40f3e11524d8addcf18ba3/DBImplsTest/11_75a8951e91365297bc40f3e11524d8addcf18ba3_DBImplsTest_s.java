 package ca.cutterslade.edbafakac.db.test;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.assertTrue;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.SQLException;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.ServiceLoader;
 
 import org.junit.AfterClass;
 import org.junit.Assert;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.junit.runners.Parameterized;
 import org.junit.runners.Parameterized.Parameters;
 
 import ca.cutterslade.edbafakac.db.Entry;
 import ca.cutterslade.edbafakac.db.EntryAlreadyExistsException;
 import ca.cutterslade.edbafakac.db.EntryNotFoundException;
 import ca.cutterslade.edbafakac.db.EntryService;
 import ca.cutterslade.edbafakac.db.jdbc.JdbcEntryService;
 
 import com.google.appengine.repackaged.com.google.common.collect.ImmutableList;
 import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
 import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
 import com.google.common.collect.ImmutableMap;
 import com.google.common.collect.ImmutableSet;
 import com.google.common.collect.Iterables;
 
 @SuppressWarnings("PMD")
 @RunWith(Parameterized.class)
 public class DBImplsTest {
 
   private static final String KEY = "e37d1e64-ed18-47dd-8501-baca4fea5b40";
 
   private static final String VALUE = "b4124b63-d57b-40f4-935a-e751bcca07da";
 
   private static final LocalServiceTestHelper HELPER =
       new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
 
   private final EntryService entryService;
 
   @Parameters
   public static Collection<Object[]> getParameters() {
     final ImmutableList.Builder<Object[]> builder = ImmutableList.builder();
     for (final Iterator<EntryService> it = ServiceLoader.load(EntryService.class).iterator(); it.hasNext();) {
       builder.add(new Object[]{ it.next() });
     }
     return builder.build();
   }
 
   public DBImplsTest(final EntryService entryService) {
     this.entryService = entryService;
   }
 
   @BeforeClass
   public static void setUp() {
     HELPER.setUp();
   }
 
   @BeforeClass
   public static void jdbcSetup() throws SQLException {
     final Connection connection = DriverManager.getConnection("jdbc:hsqldb:mem:edbafakac", "sa", "");
     try {
       JdbcEntryService.createTable(connection);
     }
     finally {
       connection.close();
     }
   }
 
   @AfterClass
   public static void tearDown() {
     HELPER.tearDown();
   }
 
   @Test
   public void getEntryTest() {
     final Entry entry = entryService.getNewEntry();
     Assert.assertTrue(entry.isDirty());
     Assert.assertNotNull(entry);
     Assert.assertNotNull(entry.getKey());
     Assert.assertTrue(entry.getPropertyKeys().isEmpty());
     Assert.assertNull(entry.getProperty(KEY));
     entry.setProperty(KEY, VALUE);
     Assert.assertEquals(VALUE, entry.getProperty(KEY));
     Assert.assertTrue(entry.isDirty());
   }
 
   @Test
   public void saveEntryTest() {
     Entry entry = entryService.getNewEntry();
     Assert.assertTrue(entry.isDirty());
     entry.setProperty(KEY, VALUE);
     Assert.assertTrue(entry.isDirty());
     entryService.saveEntry(entry);
     Assert.assertFalse(entry.isDirty());
     entry = entryService.getEntry(entry.getKey());
     Assert.assertEquals(VALUE, entry.getProperty(KEY));
     Assert.assertFalse(entry.isDirty());
   }
 
   @Test(expected = EntryNotFoundException.class)
   public void noSaveEntryTest() {
     final Entry entry = entryService.getNewEntry();
     entryService.getEntry(entry.getKey());
   }
 
   @Test
   public void noSaveModificationTest() {
     Entry entry = entryService.getNewEntry();
     Assert.assertTrue(entry.isDirty());
     entryService.saveEntry(entry);
     Assert.assertFalse(entry.isDirty());
     entry.setProperty(KEY, VALUE);
     Assert.assertTrue(entry.isDirty());
     entry = entryService.getEntry(entry.getKey());
     Assert.assertFalse(entry.isDirty());
     Assert.assertFalse(entry.hasProperty(KEY));
     Assert.assertNull(entry.getProperty(KEY));
   }
 
   @Test(expected = IllegalArgumentException.class)
   public void nullPropertyKeyTest() {
     entryService.getNewEntry().setProperty(null, VALUE);
   }
 
   @Test(expected = IllegalArgumentException.class)
   public void nullPropertyValueTest() {
     entryService.getNewEntry().setProperty(KEY, null);
   }
 
   @Test(expected = EntryAlreadyExistsException.class)
   public void newEntryWithUsedKeyTest() {
     final Entry entry = entryService.getNewEntry();
     Assert.assertTrue(entry.isDirty());
     entryService.saveEntry(entry);
     Assert.assertFalse(entry.isDirty());
     entryService.getNewEntry(entry.getKey());
   }
 
   @Test(expected = EntryNotFoundException.class)
   public void removeTest() {
     final Entry entry = entryService.getNewEntry();
     entryService.saveEntry(entry);
     entryService.removeEntry(entry.getKey());
     entryService.getEntry(entry.getKey());
   }
 
   @Test
   public void presetKeyTest() {
     final Entry entry = entryService.getNewEntry(KEY);
     Assert.assertFalse(entry.isDirty());
     Assert.assertEquals(KEY, entry.getKey());
     Assert.assertNotNull(entryService.getEntry(entry.getKey()));
   }
 
   @Test
   public void presetKeyNotSavedTest() {
     final Entry entry = entryService.getNewEntry();
     // this does not throw an exception because the first entry was never saved
     Assert.assertEquals(entry.getKey(), entryService.getNewEntry(entry.getKey()).getKey());
   }
 
   @Test(expected = EntryAlreadyExistsException.class)
   public void presetKeySavedTest() {
     final Entry entry = entryService.getNewEntry();
     entryService.saveEntry(entry);
     entryService.getNewEntry(entry.getKey());
   }
 
   @Test
   public void presetKeyAfterRemoveTest() {
     final Entry entry = entryService.getNewEntry();
     Assert.assertTrue(entry.isDirty());
     entryService.saveEntry(entry);
     Assert.assertFalse(entry.isDirty());
     entryService.removeEntry(entry.getKey());
     Assert.assertEquals(entry.getKey(), entryService.getNewEntry(entry.getKey()).getKey());
   }
 
   @Test(expected = IllegalArgumentException.class)
   public void nullPresetKeyTest() {
     entryService.getNewEntry(null);
   }
 
   @Test(expected = IllegalArgumentException.class)
   public void nullGetEntryTest() {
     entryService.getEntry(null);
   }
 
   @Test(expected = IllegalArgumentException.class)
   public void nullRemovePropertyTest() {
     entryService.getNewEntry().removeProperty(null);
   }
 
   @Test
   public void hasPropertyTest() {
     final Entry entry = entryService.getNewEntry();
     Assert.assertTrue(entry.isDirty());
     entry.setProperty(KEY, VALUE);
     Assert.assertTrue(entry.isDirty());
     Assert.assertTrue(entry.hasProperty(KEY));
   }
 
   @Test
   public void getPropertiesTest() {
     final Entry entry = entryService.getNewEntry();
     Assert.assertTrue(entry.isDirty());
     entry.setProperty(KEY, VALUE);
     Assert.assertTrue(entry.isDirty());
     final ImmutableMap<String, String> properties = entry.getProperties();
     Assert.assertEquals(1, properties.size());
     Assert.assertEquals(VALUE, properties.get(KEY));
   }
 
   @Test
   public void getPropertyKeysTest() {
     final Entry entry = entryService.getNewEntry();
     Assert.assertTrue(entry.isDirty());
     entry.setProperty(KEY, VALUE);
     Assert.assertTrue(entry.isDirty());
     final ImmutableSet<String> propertyKeys = entry.getPropertyKeys();
     Assert.assertEquals(1, propertyKeys.size());
     Assert.assertEquals(KEY, Iterables.getOnlyElement(propertyKeys));
   }
 
   @Test(expected = IllegalArgumentException.class)
   public void nullSaveEntryTest() {
     entryService.saveEntry(null);
   }
 
   @Test(expected = IllegalArgumentException.class)
   public void nullRemoveEntryTest() {
     entryService.removeEntry(null);
   }
 
   @Test
   public void removePropertyTest() {
     Entry entry = entryService.getNewEntry();
     assertTrue(entry.isDirty());
     entryService.saveEntry(entry);
     assertFalse(entry.isDirty());
     entry = entryService.getEntry(entry.getKey());
     assertFalse(entry.isDirty());
     entry.setProperty(KEY, VALUE);
     assertTrue(entry.isDirty());
     entryService.saveEntry(entry);
     assertFalse(entry.isDirty());
     assertEquals(VALUE, entry.getProperty(KEY));
     entry = entryService.getEntry(entry.getKey());
     assertFalse(entry.isDirty());
     entry.removeProperty(KEY);
     assertTrue(entry.isDirty());
     assertFalse(entry.hasProperty(KEY));
     assertNull(entry.getProperty(KEY));
   }
 }
