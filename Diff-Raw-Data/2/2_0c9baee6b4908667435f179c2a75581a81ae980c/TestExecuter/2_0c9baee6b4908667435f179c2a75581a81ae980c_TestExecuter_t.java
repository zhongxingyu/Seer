 /** -----------------------------------------------------------------
  *    Sammelbox: Collection Manager - A free and open-source collection manager for Windows & Linux
  *    Copyright (C) 2011 Jerome Wagener & Paul Bicheler
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
 
 package org.sammelbox;
 
 import static org.junit.Assert.fail;
 
 import java.io.File;
 
 import org.junit.runner.RunWith;
 import org.junit.runners.Suite;
 import org.sammelbox.album.AlterAlbumTests;
 import org.sammelbox.album.BackupRestoreTests;
 import org.sammelbox.album.CreateAlbumTests;
 import org.sammelbox.album.RemoveAlbumTests;
 import org.sammelbox.albumitems.AddAlbumItemTests;
 import org.sammelbox.albumitems.AlbumItemPictureTests;
 import org.sammelbox.albumitems.RemoveAlbumItemTests;
 import org.sammelbox.albumitems.UpdateAlbumItemTests;
 import org.sammelbox.albumviews.GeneralAlbumViewTests;
 import org.sammelbox.albumviews.ModifyAlbumViewTests;
 import org.sammelbox.albumviews.RunAlbumViewTests;
 import org.sammelbox.controller.filesystem.FileSystemAccessWrapper;
 import org.sammelbox.controller.filesystem.FileSystemLocations;
 import org.sammelbox.controller.managers.ConnectionManager;
 import org.sammelbox.exporting.ExportTests;
 import org.sammelbox.importing.CSVImportTests;
 import org.sammelbox.searching.AdvancedSearchTests;
 import org.sammelbox.searching.QuickSearchTests;
 import org.sammelbox.sidepanes.SidepaneCreationTests;
 
 @RunWith(Suite.class)
 
 @Suite.SuiteClasses({
 	AlterAlbumTests.class, 
 	BackupRestoreTests.class,  
 	CreateAlbumTests.class,
 	AddAlbumItemTests.class,
 	RemoveAlbumTests.class,
 	
 	AddAlbumItemTests.class,
 	RemoveAlbumItemTests.class,
 	UpdateAlbumItemTests.class,
 	AlbumItemPictureTests.class,
 	
 	GeneralAlbumViewTests.class,
 	ModifyAlbumViewTests.class,
 	RunAlbumViewTests.class,
 	
 	AdvancedSearchTests.class,
 	QuickSearchTests.class,
 	
 	ExportTests.class,
 	CSVImportTests.class,
 	
 	SidepaneCreationTests.class
 })
 
 public class TestExecuter {
 	public static final String PATH_TO_TEST_CBK = 
 			System.getProperty("user.dir") + File.separatorChar + "test" +
 					File.separatorChar + "testdata" + 
					File.separatorChar + "test-albums-version-3.2.cbk";
 	
 	public static final String PATH_TO_TEST_PICTURE_1 = 
 			System.getProperty("user.dir") + File.separatorChar + "res" + 
 					File.separator + "graphics"+
 					File.separatorChar + "placeholder.png";
 	
 	public static final String PATH_TO_TEST_PICTURE_2 = 
 			System.getProperty("user.dir") + File.separatorChar + "res" + 
 					File.separator + "graphics"+
 					File.separatorChar + "placeholder2.png";
 	
 	public static final String PATH_TO_TEST_PICTURE_3 = 
 			System.getProperty("user.dir") + File.separatorChar + "res" + 
 					File.separator + "graphics"+
 					File.separatorChar + "placeholder3.png";
 	
 	public static void resetTestHome() {
 		try {
 			FileSystemLocations.setActiveHomeDir(FileSystemLocations.DEFAULT_SAMMELBOX_TEST_HOME);
 			ConnectionManager.closeConnection();
 			FileSystemAccessWrapper.removeHomeDirectory();
 			Class.forName(Sammelbox.ORG_SQLITE_JDBC);
 			FileSystemAccessWrapper.updateSammelboxFileStructure();			
 			ConnectionManager.openConnection();
 			FileSystemAccessWrapper.updateAlbumFileStructure(ConnectionManager.getConnection());
 		} catch (Exception e) {
 			fail("A problem occured while resetting the test home directory");
 		}
 	}
 }
