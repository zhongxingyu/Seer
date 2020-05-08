 /** -----------------------------------------------------------------
  *    Sammelbox: Collection Manager - A free and open-source collection manager for Windows & Linux
  *    Copyright (C) 2011 Jérôme Wagener & Paul Bicheler
  *
  *    This program is free software: you can redistribute it and/or modify
  *    it under the terms of the GNU General Public License as published by
  *    the Free Software Foundation, either version 3 of the License, or
  *    (at your option) any later version.
  *
  *    This program is distributed in the hope that it will be useful,
  *    but WITHOUT ANY WARRANTY; without even the implied warranty of
  *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *    GNU General Public License for more details.
  *
  *    You should have received a copy of the GNU General Public License
  *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
  ** ----------------------------------------------------------------- */
 
 package collector.desktop.tests.album;
 
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
import collector.desktop.controller.filesystem.FileSystemAccessWrapper;
 import collector.desktop.controller.managers.ConnectionManager;
 import collector.desktop.controller.managers.DatabaseIntegrityManager;
 import collector.desktop.model.album.AlbumItem;
 import collector.desktop.model.album.AlbumItemPicture;
 import collector.desktop.model.album.FieldType;
 import collector.desktop.model.album.MetaItemField;
 import collector.desktop.model.database.DatabaseStringUtilities;
 import collector.desktop.model.database.exceptions.DatabaseWrapperOperationException;
 import collector.desktop.model.database.operations.DatabaseOperations;
 import collector.desktop.tests.TestExecuter;
 import collector.desktop.tests.utilities.TestQueries;
 
 public class AlterAlbumTests {
 	@BeforeClass
 	public static void setUpBeforeClass() throws Exception {
 	}
 
 	@AfterClass
 	public static void tearDownAfterClass() throws Exception {
 		TestExecuter.resetEverything();
 	}
 
 	@Before
 	public void setUp() {
 		TestExecuter.resetEverything();
 	}
 
 	@After
 	public void tearDown() throws Exception {
 		ConnectionManager.closeConnection();
 	}
 
 	@Test
 	public void testAddPublisherFieldToBookAlbum() {
 		try {
 			DatabaseIntegrityManager.restoreFromFile(TestExecuter.PATH_TO_TEST_CBK);
 
 			MetaItemField metaItemField = new MetaItemField("Publisher", FieldType.Text, false);
 			long originalAlbumItemCount = DatabaseOperations.getNumberOfItemsInAlbum("Books");
 
 			DatabaseOperations.appendNewAlbumField("Books", metaItemField);
 			List<MetaItemField> metaDataItems = DatabaseOperations.getAlbumItemFieldNamesAndTypes("Books");
 
 			assertTrue("New publisher text column should be added at the end", 
 					metaDataItems.get(metaDataItems.size()-1).getName().equals("Publisher"));
 
 			assertTrue("The album item count incorrectly changed", 
 					originalAlbumItemCount == DatabaseOperations.getNumberOfItemsInAlbum("Books"));
 		} catch (DatabaseWrapperOperationException e) {
 			fail(e.getMessage());
 		}
 	}
 
 	@Test
 	public void testAddQuickSearchablePublisherFieldToBookAlbum() {
 		try {
 			DatabaseIntegrityManager.restoreFromFile(TestExecuter.PATH_TO_TEST_CBK);
 
 			MetaItemField metaItemField = new MetaItemField("Publisher", FieldType.Text, true);
 			long originalAlbumItemCount = DatabaseOperations.getNumberOfItemsInAlbum("Books");
 
 			DatabaseOperations.appendNewAlbumField("Books", metaItemField);
 			List<MetaItemField> metaDataItems = DatabaseOperations.getAlbumItemFieldNamesAndTypes("Books");
 
 			assertTrue("New publisher text column should be added at the end", 
 					metaDataItems.get(metaDataItems.size()-1).getName().equals("Publisher"));
 
 			assertTrue("The album item count incorrectly changed", 
 					originalAlbumItemCount == DatabaseOperations.getNumberOfItemsInAlbum("Books"));
 		}catch (DatabaseWrapperOperationException e ) {
 			fail(e.getMessage());
 		}
 	}
 
 	@Test
 	public void testAddPublisherFieldAndMoveFourUp() {
 		try {
 			DatabaseIntegrityManager.restoreFromFile(TestExecuter.PATH_TO_TEST_CBK);
 			long originalAlbumItemCount = DatabaseOperations.getNumberOfItemsInAlbum("Books");
 
 			MetaItemField metaItemField = new MetaItemField("Publisher", FieldType.Text, false);
 
 			DatabaseOperations.appendNewAlbumField("Books", metaItemField);
 			List<MetaItemField> metaDataItems = DatabaseOperations.getAlbumItemFieldNamesAndTypes("Books");
 			
 			assertTrue("New publisher text column should be added at the end", 
 					metaDataItems.get(metaDataItems.size()-1).getName().equals("Publisher"));
 
 			DatabaseOperations.reorderAlbumItemField("Books", metaDataItems.get(metaDataItems.size()-1), metaDataItems.get(metaDataItems.size()-3));
 			metaDataItems = DatabaseOperations.getAlbumItemFieldNamesAndTypes("Books");
 			
 			DatabaseOperations.reorderAlbumItemField("Books", metaDataItems.get(metaDataItems.size()-2), metaDataItems.get(metaDataItems.size()-4));
 			metaDataItems = DatabaseOperations.getAlbumItemFieldNamesAndTypes("Books");
 			
 			DatabaseOperations.reorderAlbumItemField("Books", metaDataItems.get(metaDataItems.size()-3), metaDataItems.get(metaDataItems.size()-5));
 			metaDataItems = DatabaseOperations.getAlbumItemFieldNamesAndTypes("Books");
 			
 			DatabaseOperations.reorderAlbumItemField("Books", metaDataItems.get(metaDataItems.size()-4), metaDataItems.get(metaDataItems.size()-6));
 			metaDataItems = DatabaseOperations.getAlbumItemFieldNamesAndTypes("Books");
 			
 			assertTrue("New publisher text column should be at the fourth position after reordering", 
 					metaDataItems.get(2).getName().equals("Publisher"));
 
 			assertTrue("The album item count incorrectly changed", 
 					originalAlbumItemCount == DatabaseOperations.getNumberOfItemsInAlbum("Books"));
 		} catch (DatabaseWrapperOperationException e ) {
 			fail(e.getMessage());
 		}
 	}
 
 	@Test
 	public void testMoveDvdTitleToEnd() {
 		try {
 			DatabaseIntegrityManager.restoreFromFile(TestExecuter.PATH_TO_TEST_CBK);
 
 			List<MetaItemField> metaDataItems = DatabaseOperations.getAlbumItemFieldNamesAndTypes("DVDs");
 			long originalAlbumItemCount = DatabaseOperations.getNumberOfItemsInAlbum("DVDs");
 
 			assertTrue("Title text column should be at the beginning", 
 					metaDataItems.get(0).getName().equals("Title"));
 
 			DatabaseOperations.reorderAlbumItemField("DVDs", metaDataItems.get(0), metaDataItems.get(1));
 
 			metaDataItems = DatabaseOperations.getAlbumItemFieldNamesAndTypes("DVDs");
 			DatabaseOperations.reorderAlbumItemField("DVDs", metaDataItems.get(1), metaDataItems.get(2));
 
 			metaDataItems = DatabaseOperations.getAlbumItemFieldNamesAndTypes("DVDs");
 			DatabaseOperations.reorderAlbumItemField("DVDs", metaDataItems.get(2), metaDataItems.get(3));
 
 			metaDataItems = DatabaseOperations.getAlbumItemFieldNamesAndTypes("DVDs");
 			DatabaseOperations.reorderAlbumItemField("DVDs", metaDataItems.get(3), metaDataItems.get(4));
 
 			metaDataItems = DatabaseOperations.getAlbumItemFieldNamesAndTypes("DVDs");
 			DatabaseOperations.reorderAlbumItemField("DVDs", metaDataItems.get(4), metaDataItems.get(5));
 
 			metaDataItems = DatabaseOperations.getAlbumItemFieldNamesAndTypes("DVDs");
 
 			assertTrue("Title text column should be at the end", metaDataItems.get(5).getName().equals("Title"));
 			assertTrue("The album item count incorrectly changed", 
 					originalAlbumItemCount == DatabaseOperations.getNumberOfItemsInAlbum("DVDs"));
 		} catch (DatabaseWrapperOperationException e) {
 			fail(e.getMessage());
 		}
 	}
 
 	@Test
 	public void testRenameNonQuicksearchableField() {
 		try {
 			DatabaseIntegrityManager.restoreFromFile(TestExecuter.PATH_TO_TEST_CBK);
 
 			List<MetaItemField> metaDataItems = DatabaseOperations.getAlbumItemFieldNamesAndTypes("Books");
 			long originalAlbumItemCount = DatabaseOperations.getNumberOfItemsInAlbum("Books");
 
 			assertTrue("The first column name should be 'Book Title'", metaDataItems.get(0).getName().equals("Book Title"));
 
 			MetaItemField bookTitleField = metaDataItems.get(0);
 			MetaItemField titleField = new MetaItemField("Title", FieldType.Text, false);
 
 			DatabaseOperations.renameAlbumItemField("Books", bookTitleField, titleField);
 
 			metaDataItems = DatabaseOperations.getAlbumItemFieldNamesAndTypes("Books");
 
 			assertTrue("The first column name should now be 'Title'", metaDataItems.get(0).getName().equals("Title"));
 			assertTrue("The album item count incorrectly changed", 
 					originalAlbumItemCount == DatabaseOperations.getNumberOfItemsInAlbum("Books"));
 		} catch (DatabaseWrapperOperationException e ) {
 			fail(e.getMessage());
 		}
 	}
 
 	@Test
 	public void testRenameQuicksearchableField() {
 		try {
 			DatabaseIntegrityManager.restoreFromFile(TestExecuter.PATH_TO_TEST_CBK);
 
 			List<MetaItemField> metaDataItems = DatabaseOperations.getAlbumItemFieldNamesAndTypes("DVDs");
 			long originalAlbumItemCount = DatabaseOperations.getNumberOfItemsInAlbum("DVDs");
 			
 			if (!metaDataItems.get(0).getName().equals("Title")) {
 				fail("The second column name should be 'Title'" );
 			}
 
 			MetaItemField dvdTitleField = metaDataItems.get(0);
 			MetaItemField titleField = new MetaItemField("DVD Title", FieldType.Text, dvdTitleField.isQuickSearchable());
 
 			DatabaseOperations.renameAlbumItemField("DVDs", dvdTitleField, titleField);
 			metaDataItems = DatabaseOperations.getAlbumItemFieldNamesAndTypes("DVDs");
 
 			assertTrue("The first column name should now be 'DVD Title'", metaDataItems.get(0).getName().equals("DVD Title"));
 			assertTrue("The album item count incorrectly changed", originalAlbumItemCount == DatabaseOperations.getNumberOfItemsInAlbum("DVDs"));
 		} catch (DatabaseWrapperOperationException e ) {
 			fail(e.getMessage());
 		}
 	}
 
 	@Test
 	public void testRenameAlbum() {
 		try {
 			DatabaseIntegrityManager.restoreFromFile(TestExecuter.PATH_TO_TEST_CBK);
 
 			List<String> albumList = DatabaseOperations.getListOfAllAlbums();
 
 			assertTrue("There should be three albums present", albumList.size() == 3);
 			assertTrue("There should be an album with the name 'Books'", albumList.contains("Books"));
 			assertTrue("There should be an album with the name 'DVDs'", albumList.contains("DVDs"));
 			assertTrue("There should be an album with the name 'Music CDs'", albumList.contains("Music CDs"));
			assertTrue("Each album should have its corresponding picture album folder",
					(new File(FileSystemAccessWrapper.getFilePathForAlbum("DVDs"))).exists());
			
 			DatabaseOperations.renameAlbum("DVDs", "Movie DVDs");
 			DatabaseOperations.renameAlbum("Books", "My Books");
 
 			albumList = DatabaseOperations.getListOfAllAlbums();
 
 			assertTrue("There should be an album with the name 'My Books'", albumList.contains("My Books"));
 			assertTrue("There should be an album with the name 'Movie DVDs'", albumList.contains("Movie DVDs"));
 			assertTrue("There should be an album with the name 'Music CDs'", albumList.contains("Music CDs"));
			assertTrue("Each album should have its corresponding picture album folder",
					(new File(FileSystemAccessWrapper.getFilePathForAlbum("Movie DVDs"))).exists());
 		} catch (DatabaseWrapperOperationException e ) {
 			fail(e.getMessage());
 		}
 	}
 
 	@Test
 	public void testDeleteAuthorField() {
 		try {
 			DatabaseIntegrityManager.restoreFromFile(TestExecuter.PATH_TO_TEST_CBK);
 			List<MetaItemField> metaDataItems = DatabaseOperations.getAlbumItemFieldNamesAndTypes("Books");
 			long originalAlbumItemCount = DatabaseOperations.getNumberOfItemsInAlbum("Books");
 
 			assertTrue("The second column name should be 'Author'", metaDataItems.get(1).getName().equals("Author"));
 
 			assertTrue("Picture table for Books should exist", 
 					TestQueries.isDatabaseTableAvailable(DatabaseStringUtilities.generatePictureTableName("Books")));
 			assertTrue("Typeinfo table for Books should exist", 
 					TestQueries.isDatabaseTableAvailable(DatabaseStringUtilities.generateTypeInfoTableName("Books")));
 			
 			MetaItemField authorMetaItemField = new MetaItemField("Author", FieldType.Text, false);
 			DatabaseOperations.removeAlbumItemField("Books", authorMetaItemField);
 			metaDataItems = DatabaseOperations.getAlbumItemFieldNamesAndTypes("Books");
 
 			for (MetaItemField metaItemField : metaDataItems) {
 				assertTrue("The 'Author' field should no longer be present", metaItemField.getName().equals("Books") == false);
 			}
 
 			assertTrue("Picture table for Books should still exist", 
 					TestQueries.isDatabaseTableAvailable(DatabaseStringUtilities.generatePictureTableName("Books")));
 			assertTrue("Typeinfo table for Books should still exist", 
 					TestQueries.isDatabaseTableAvailable(DatabaseStringUtilities.generateTypeInfoTableName("Books")));
 			
 			assertTrue("The album item count incorrectly changed", 
 					originalAlbumItemCount == DatabaseOperations.getNumberOfItemsInAlbum("Books"));
 		} catch (DatabaseWrapperOperationException e) {			
 			fail(e.getMessage());
 		}
 	}
 	
 	@Test
 	public void testReplaceSinglePictureWithTwoOtherPictures() {
 		try {
 			DatabaseIntegrityManager.restoreFromFile(TestExecuter.PATH_TO_TEST_CBK);
 			AlbumItem albumItem = DatabaseOperations.getAlbumItem("DVDs", 1);
 			
 			assertTrue("The first DVD item should contain only one picture", albumItem.getPictures().size() == 1);
 			
 			List<AlbumItemPicture> pictures = new ArrayList<>();
 			pictures.add(new AlbumItemPicture(
 					TestExecuter.PATH_TO_TEST_PICTURE_1, TestExecuter.PATH_TO_TEST_PICTURE_1, "DVDs", 1));
 			pictures.add(new AlbumItemPicture(
 					TestExecuter.PATH_TO_TEST_PICTURE_2, TestExecuter.PATH_TO_TEST_PICTURE_2, "DVDs", 1));
 			
 			albumItem.setPictures(pictures);
 			
 			assertTrue("The *non* persisted DVD item should now contain two pictures", albumItem.getPictures().size() == 2);
 			
 			DatabaseOperations.updateAlbumItem(albumItem);
 			
 			AlbumItem newAlbumItem = DatabaseOperations.getAlbumItem("DVDs", 1);
 			
 			assertTrue("The *persisted* DVD item should now contain two pictures", newAlbumItem.getPictures().size() == 2);
 		} catch (DatabaseWrapperOperationException e) {
 			fail(e.getMessage());
 		}
 	}
 	
 	@Test
 	public void testRenameAlbumShouldRenameInternalTables() {
 		try {
 			DatabaseIntegrityManager.restoreFromFile(TestExecuter.PATH_TO_TEST_CBK);
 			DatabaseOperations.renameAlbum("DVDs", "My DVD Collection");
 			
 			if (!TestQueries.isDatabaseTableAvailable("my_dvd_collection_typeinfo")) {
 				fail("The typeinfo table should always be present");
 			}
 			
 			if (!TestQueries.isDatabaseTableAvailable("my_dvd_collection_pictures")) {
 				fail("The picture table should always be present");
 			}
 		} catch (DatabaseWrapperOperationException e) {
 			fail(e.getMessage());
 		}
 	}
 	
 	@Test
 	public void testPictureTableShouldAlwaysBePresent() {
 		try {
 			List<MetaItemField> fields = new ArrayList<>();
 			
 			fields.add(new MetaItemField("field1", FieldType.Text));
 			fields.add(new MetaItemField("field2", FieldType.Text));
 			
 			DatabaseOperations.createNewAlbum("TestAlbum", fields, false);
 			
 			if (!TestQueries.isDatabaseTableAvailable("TestAlbum_pictures")) {
 				fail("The picture table should always be present");
 			}
 		} catch (DatabaseWrapperOperationException e) {
 			fail(e.getMessage());
 		}
 	}
 	
 	@Test
 	public void testTableNameTranslation() {
 		try {
 			List<MetaItemField> fields = new ArrayList<>();
 			
 			fields.add(new MetaItemField("field1", FieldType.Text));
 			fields.add(new MetaItemField("field2", FieldType.Text));
 			
 			DatabaseOperations.createNewAlbum("Test Album 1", fields, false);
 			
 			if (!TestQueries.isDatabaseTableAvailable("test_album_1_pictures")) {
 				fail("The table name should be lower case and spaces should be replaced by underscores");
 			}
 		} catch (DatabaseWrapperOperationException e) {
 			fail(e.getMessage());
 		}
 	}
 	
 	@Test
 	public void testPictureDisableEnable() {
 		try {
 			DatabaseIntegrityManager.restoreFromFile(TestExecuter.PATH_TO_TEST_CBK);
 			
 			assertTrue("DVDs must have pictures within the test album", DatabaseOperations.isPictureAlbum("DVDs"));
 			assertTrue("There must be picture records in the picture table", 
 					TestQueries.getNumberOfRecordsInTable(DatabaseStringUtilities.generatePictureTableName("DVDs")) != 0);
 			
 			// disable pictures
 			DatabaseOperations.setAlbumPictureFunctionality("DVDs", false);
 			
 			// picture records should be removed while picture table should remain
 			assertTrue("Picture table should still exist", 
 					TestQueries.isDatabaseTableAvailable(DatabaseStringUtilities.generatePictureTableName("DVDs")));
 			assertTrue("DVDs no longer have pictures associated", !DatabaseOperations.isPictureAlbum("DVDs"));
 			assertTrue("There must be NO picture records since the picture functionality has been disabled", 
 					TestQueries.getNumberOfRecordsInTable(DatabaseStringUtilities.generatePictureTableName("DVDs")) == 0);
 			
 			// enable pictures
 			DatabaseOperations.setAlbumPictureFunctionality("DVDs", true);
 			
 			assertTrue("Picture table should still exist", 
 					TestQueries.isDatabaseTableAvailable(DatabaseStringUtilities.generatePictureTableName("DVDs")));
 			assertTrue("DVDs must have pictures again", DatabaseOperations.isPictureAlbum("DVDs"));
 			
 			// add new pictures pictures to existing item
 			AlbumItem albumItem = DatabaseOperations.getAlbumItem("DVDs", 1);
 			
 			List<AlbumItemPicture> albumItemPictures = new ArrayList<AlbumItemPicture>();
 			albumItemPictures.add(new AlbumItemPicture(TestExecuter.PATH_TO_TEST_PICTURE_1, 
 					TestExecuter.PATH_TO_TEST_PICTURE_1, "DVDs", AlbumItemPicture.PICTURE_ID_UNDEFINED));
 			albumItemPictures.add(new AlbumItemPicture(TestExecuter.PATH_TO_TEST_PICTURE_2, 
 					TestExecuter.PATH_TO_TEST_PICTURE_2, "DVDs", AlbumItemPicture.PICTURE_ID_UNDEFINED));
 			albumItemPictures.add(new AlbumItemPicture(TestExecuter.PATH_TO_TEST_PICTURE_3, 
 					TestExecuter.PATH_TO_TEST_PICTURE_3, "DVDs", AlbumItemPicture.PICTURE_ID_UNDEFINED));
 			
 			albumItem.setPictures(albumItemPictures);
 			DatabaseOperations.updateAlbumItem(albumItem);
 			
 			assertTrue("DVDs must have pictures once again", DatabaseOperations.isPictureAlbum("DVDs"));
 			assertTrue("There should now be three pictures in the picture table", 
 					TestQueries.getNumberOfRecordsInTable(DatabaseStringUtilities.generatePictureTableName("DVDs")) == 3);
 		} catch (DatabaseWrapperOperationException e) {
 			fail(e.getMessage());
 		}
 	}
 	
 	@Test
 	public void testAlterationsLeaveNumberOfItemsAndPicturesUnaffected() {
 		try {
 			DatabaseIntegrityManager.restoreFromFile(TestExecuter.PATH_TO_TEST_CBK);
 			
 			final long numberOfItems = TestQueries.getNumberOfRecordsInTable(DatabaseStringUtilities.generateTableName("DVDs"));
 			final long numberOfPictures = TestQueries.getNumberOfRecordsInTable(DatabaseStringUtilities.generatePictureTableName("DVDs"));
 			
 			Map<Integer, MetaItemField> metaItemFields = DatabaseOperations.getAlbumItemMetaMap("DVDs");
 			MetaItemField oldField = metaItemFields.get(3);
 			MetaItemField newField = new MetaItemField("Test A", FieldType.Decimal, false);
 			DatabaseOperations.renameAlbumItemField("DVDs", oldField, newField);
 			
 			assertTrue("The number of items should not have changed after a field rename", 
 					numberOfItems == TestQueries.getNumberOfRecordsInTable(DatabaseStringUtilities.generateTableName("DVDs")));
 			assertTrue("The number of pictures should not have changed after a field rename", 
 					numberOfPictures == TestQueries.getNumberOfRecordsInTable(DatabaseStringUtilities.generatePictureTableName("DVDs")));
 						
 			MetaItemField oscarWinningField = new MetaItemField("Oscar winning movie", FieldType.Option, false);
 			DatabaseOperations.appendNewAlbumField("DVDs", oscarWinningField);
 			
 			assertTrue("The number of items should not have changed after a field has been added", 
 					numberOfItems == TestQueries.getNumberOfRecordsInTable(DatabaseStringUtilities.generateTableName("DVDs")));
 			assertTrue("The number of pictures should not have changed after a field has been added", 
 					numberOfPictures == TestQueries.getNumberOfRecordsInTable(DatabaseStringUtilities.generatePictureTableName("DVDs")));
 			
 			DatabaseOperations.reorderAlbumItemField("DVDs", oscarWinningField, DatabaseOperations.getAlbumItemMetaMap("DVDs").get(1));
 			
 			assertTrue("The number of items should not have changed after reordering album items", 
 					numberOfItems == TestQueries.getNumberOfRecordsInTable(DatabaseStringUtilities.generateTableName("DVDs")));
 			assertTrue("The number of pictures should not have changed after reordering album items", 
 					numberOfPictures == TestQueries.getNumberOfRecordsInTable(DatabaseStringUtilities.generatePictureTableName("DVDs")));
 			
 			metaItemFields = DatabaseOperations.getAlbumItemMetaMap("DVDs");			
 			DatabaseOperations.removeAlbumItemField("DVDs", metaItemFields.get(1));
 			
 			assertTrue("The number of items should still be the same after removing a field", 
 					numberOfItems == TestQueries.getNumberOfRecordsInTable(DatabaseStringUtilities.generateTableName("DVDs")));
 			assertTrue("The number of pictures should still be the same after removing a field", 
 					numberOfPictures == TestQueries.getNumberOfRecordsInTable(DatabaseStringUtilities.generatePictureTableName("DVDs")));
 		} catch (DatabaseWrapperOperationException e) {
 			fail(e.getMessage());
 		}
 	}
 	
 	@Test
 	public void testSetQuicksearableOnAlbumWithSpace() {
 		try {
 			DatabaseIntegrityManager.restoreFromFile(TestExecuter.PATH_TO_TEST_CBK);
 
 			Map<Integer, MetaItemField> metaItemFields = DatabaseOperations.getAlbumItemMetaMap("Music CDs");
 			metaItemFields.get(2).setQuickSearchable(true);
 			DatabaseOperations.updateQuickSearchable("Music CDs", metaItemFields.get(2));
 
 			metaItemFields = DatabaseOperations.getAlbumItemMetaMap("Music CDs");
 			assertTrue("The first field of the 'Music CDs' album should now be quick searchable!", metaItemFields.get(2).isQuickSearchable());
 		} catch (Exception e) {
 			fail(e.getMessage());
 		}
 	}
 	
 	@Test
 	public void testAppendColumnsToAlbum() {
 		try {
 			DatabaseIntegrityManager.restoreFromFile(TestExecuter.PATH_TO_TEST_CBK);
 
 			final int initialColumnCount = DatabaseOperations.getAlbumItemMetaMap("Music CDs").size();
 			
 			MetaItemField price = new MetaItemField("Price", FieldType.Decimal);
 			DatabaseOperations.appendNewAlbumField("Music CDs", price);
 			
 			MetaItemField releaseDate = new MetaItemField("Release Date", FieldType.Date);
 			DatabaseOperations.appendNewAlbumField("Music CDs", releaseDate);
 			
 			assertTrue("Music CDs should now have two additional columns", 
 					DatabaseOperations.getAlbumItemMetaMap("Music CDs").size() == initialColumnCount + 2);
 			
 		} catch (Exception e) {
 			fail(e.getMessage());
 		}
 	}
 }
