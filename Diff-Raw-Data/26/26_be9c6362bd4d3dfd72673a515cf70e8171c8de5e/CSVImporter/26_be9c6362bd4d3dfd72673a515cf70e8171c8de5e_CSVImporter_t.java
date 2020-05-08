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
 
 package org.sammelbox.controller.filesystem.importing;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.sql.Date;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.sammelbox.model.album.AlbumItem;
 import org.sammelbox.model.album.AlbumItemPicture;
 import org.sammelbox.model.album.FieldType;
 import org.sammelbox.model.album.ItemField;
 import org.sammelbox.model.album.MetaItemField;
 import org.sammelbox.model.album.OptionType;
 import org.sammelbox.model.album.StarRating;
 import org.sammelbox.model.database.exceptions.DatabaseWrapperOperationException;
 import org.sammelbox.model.database.operations.DatabaseOperations;
 import org.sammelbox.view.image.ImageManipulator;
 
 public final class CSVImporter {
 	private static final int NO_PICTURE_INDEX = -1;
 	private static final String NO_PICTURE_COLUMN_NAME = "NO_PICTURE_COLUMN_NAME";
 	private static final String NO_PICTURE_SEPARATION_CHARACTER = "NO_PICTURE_SEPARATION_CHARACTER";
 
 	private CSVImporter() {
 	}
 	
 	public static void importCSV(String albumName, String csvFilePath, String separationCharacter, boolean isSimulation) throws ImportException {
 		importCSV(albumName, csvFilePath, separationCharacter, NO_PICTURE_COLUMN_NAME, NO_PICTURE_SEPARATION_CHARACTER, isSimulation);
 	}
 	
 	public static void importCSV(String albumName, String csvFilePath, String separationCharacter, 
 			String pictureColumnName, String pictureSeparationCharacter, boolean isSimulation) throws ImportException {
 		
 		try (BufferedReader br = new BufferedReader(new FileReader(new File(csvFilePath)));) {
 			int pictureColumnIndex = NO_PICTURE_INDEX;
 			
 			List<MetaItemField> metaItemFields = new ArrayList<>();
 			
 			String line = br.readLine();
 			pictureColumnIndex = handleFirstLine(line, albumName, separationCharacter, metaItemFields, pictureColumnName, pictureSeparationCharacter, isSimulation);
 			
 			while ((line = br.readLine()) != null) {
 				handleData(line, albumName, separationCharacter, metaItemFields, pictureSeparationCharacter, pictureColumnIndex, isSimulation);
 			}
 						
 		} catch (FileNotFoundException fnfe) {
 			throw new ImportException("An error occured while handling the file", fnfe);
 		} catch (IOException ioe) {
 			throw new ImportException("An error occured while handling the file", ioe);
 		} catch (NumberFormatException nfe) {
 			throw new ImportException("An error occured while trying to interpret a decimal or integer value", nfe);
 		} catch (DatabaseWrapperOperationException dwoe) {
 			throw new ImportException("An internal error occured", dwoe);
 		}
 	}
 	
 	private static int handleFirstLine(String line, String albumName, String separationCharacter, List<MetaItemField> metaItemFields, 
 			String pictureColumnName, String pictureSeparationCharacter, boolean isSimulation) throws ImportException, DatabaseWrapperOperationException {
 		
 		if (line == null || line.isEmpty()) {
 			throw new ImportException("There seems to be no header data");
 		}
 		
 		int pictureColumnIndex = NO_PICTURE_INDEX;
 		
 		// Using the first line, we should be able to define the fields, and possibly there types
 		String[] fieldHeader = line.split(separationCharacter);
 		
 		for (int i=0; i<fieldHeader.length; i++) {
 			// Check if the type had been added to the field name
 			String[] fieldNameAndPossiblyType = fieldHeader[i].split("~");
 			
 			if (fieldNameAndPossiblyType.length == 1) {
 				if (fieldNameAndPossiblyType[0].equals(pictureColumnName)) {
 					pictureColumnIndex = i;
 				} else {
 					metaItemFields.add(new MetaItemField(fieldNameAndPossiblyType[0], FieldType.TEXT));
 				}
 			} else if (fieldNameAndPossiblyType.length == 2) {
 				metaItemFields.add(new MetaItemField(fieldNameAndPossiblyType[0], FieldType.valueOf(fieldNameAndPossiblyType[1])));
 			} else {
 				throw new ImportException("Could not interpret: " + fieldHeader[i]);
 			}
 		}
 		
 		if (!isSimulation) {
 			if (NO_PICTURE_COLUMN_NAME.equals(pictureColumnName) && NO_PICTURE_SEPARATION_CHARACTER.equals(pictureSeparationCharacter)) {
 				DatabaseOperations.createNewAlbum(albumName, metaItemFields, false);
 			} else {
 				DatabaseOperations.createNewAlbum(albumName, metaItemFields, true);
 			}
 		}
 		
 		return pictureColumnIndex;
 	}
 	
 	private static void handleData(String line, String albumName, String separationCharacter, List<MetaItemField> metaItemFields, 
 			String pictureSeperationCharacter, int pictureColumnIndex, boolean isSimulation) throws DatabaseWrapperOperationException, ImportException {
 		
 		String[] fieldValues = line.split(separationCharacter, NO_PICTURE_INDEX);
 
 		int pictureOffset = pictureColumnIndex == NO_PICTURE_INDEX ? 0 : 1;
 		if ((metaItemFields.size() + pictureOffset) != fieldValues.length) {
 			throw new ImportException("The header line has a different number of columns compared to the current data line (" + line + ")");
 		}
 		
 		List<ItemField> itemFields = new ArrayList<>();
 		List<AlbumItemPicture> pictures = new ArrayList<>();
 		
		// parse item fields
 		for (int index=0; index<metaItemFields.size(); index++) {
 			if (index != pictureColumnIndex) {
 				convertIntoDatabaseValueAndAddToItemFields(metaItemFields, fieldValues, index, itemFields);
 			}	
 		}
 		
		// parse picture field
		for (String filePath : fieldValues[pictureColumnIndex].split(pictureSeperationCharacter)) {
			if (!filePath.isEmpty()) {
				if (isSimulation) {
					// if it is a simulation, just check if the specified files exist!
					new File(filePath).exists();
				} else {
					pictures.add(ImageManipulator.adaptAndStoreImageForCollectorUsingApacheImaging(new File(filePath), albumName));
				}
			}
		}
		
 		AlbumItem albumItem = new AlbumItem(albumName, itemFields);
 		albumItem.setPictures(pictures);
 		
 		if (!isSimulation) {
 			DatabaseOperations.addAlbumItem(albumItem, true);
 		}
 	}
 	
 	private static void convertIntoDatabaseValueAndAddToItemFields(List<MetaItemField> metaItemFields, String[] fieldValues, int index, List<ItemField> itemFields) {
 		switch (metaItemFields.get(index).getType()) {
 		case TEXT:
 			itemFields.add(new ItemField(metaItemFields.get(index).getName(), FieldType.TEXT, fieldValues[index]));
 			break;
 		case DECIMAL:
 			if (!fieldValues[index].isEmpty()) {
 				itemFields.add(new ItemField(metaItemFields.get(index).getName(), FieldType.DECIMAL, Double.valueOf(fieldValues[index])));
 			}
 			break;
 		case INTEGER:
 			if (!fieldValues[index].isEmpty()) {
 				itemFields.add(new ItemField(metaItemFields.get(index).getName(), FieldType.INTEGER, Integer.valueOf(fieldValues[index])));
 			}
 			break;
 		case OPTION:
 			itemFields.add(new ItemField(metaItemFields.get(index).getName(), FieldType.OPTION, OptionType.valueOf(fieldValues[index])));
 			break;
 		case STAR_RATING:
 			itemFields.add(new ItemField(metaItemFields.get(index).getName(), FieldType.STAR_RATING, StarRating.valueOf(fieldValues[index])));
 			break;
 		case URL:
 			itemFields.add(new ItemField(metaItemFields.get(index).getName(), FieldType.URL, fieldValues[index]));
 			break;
 		case DATE:
 			itemFields.add(new ItemField(metaItemFields.get(index).getName(), FieldType.DATE, new Date(System.currentTimeMillis())));
 			break;
 		default:
 			break;
 		}
 	}
 }
