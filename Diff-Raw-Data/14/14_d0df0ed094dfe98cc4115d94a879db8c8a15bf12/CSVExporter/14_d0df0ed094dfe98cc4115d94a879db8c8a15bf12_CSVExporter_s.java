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
 
 package org.sammelbox.controller.filesystem.exporting;
 
 import java.io.BufferedWriter;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.List;
 
 import org.sammelbox.controller.i18n.DictKeys;
 import org.sammelbox.controller.i18n.Translator;
 import org.sammelbox.model.album.AlbumItem;
 import org.sammelbox.model.album.FieldType;
 import org.sammelbox.model.album.OptionType;
 import org.sammelbox.model.album.StarRating;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public final class CSVExporter {
 	private static final Logger LOGGER = LoggerFactory.getLogger(CSVExporter.class);
 	
 	private CSVExporter() {
 	}
 	
 	public static void exportAlbum(List<AlbumItem> albumItems, String filepath, String separationCharacter) {
 		StringBuilder headerBuilder = new StringBuilder();
 		StringBuilder dataBuilder = new StringBuilder();
 		boolean firstLine = true;
 
 		for (AlbumItem albumItem : albumItems) {			
 			for (int i=0; i<albumItem.getFields().size(); i++) {				
 				if (albumItem.getField(i).getType().equals(FieldType.UUID)) {
 					// schema or content version UUID --> ignore 
 				}
 				else if (albumItem.getField(i).getType().equals(FieldType.ID)) {
 					// do not show ID either
 				} else {
 					if (albumItem.getField(i).getType().equals(FieldType.OPTION)) {
 						addHeaderIfFirstLine(firstLine, albumItem, headerBuilder, separationCharacter, i);
 
 						// TODO this should be in the OptionType
 						if (albumItem.getField(i).getValue() == OptionType.YES) {
 							dataBuilder.append(Translator.get(DictKeys.BROWSER_YES));
 						} else if (albumItem.getField(i).getValue() == OptionType.NO) {
 							dataBuilder.append(Translator.get(DictKeys.BROWSER_NO));
 						} else {
 							dataBuilder.append(Translator.get(DictKeys.BROWSER_UNKNOWN));
 						}
 						
 						addSeparationCharacterIfRequired(albumItem, dataBuilder, separationCharacter, i);
 					} else if (albumItem.getField(i).getType().equals(FieldType.STAR_RATING)) {
 						addHeaderIfFirstLine(firstLine, albumItem, headerBuilder, separationCharacter, i);
 						dataBuilder.append(StarRating.toComboBoxArray()[((StarRating) albumItem.getField(i).getValue()).getIntegerValue()]);
 						addSeparationCharacterIfRequired(albumItem, dataBuilder, separationCharacter, i);
 					} else {
 						addHeaderIfFirstLine(firstLine, albumItem, headerBuilder, separationCharacter, i);
 						dataBuilder.append(albumItem.getField(i).getValue());
 						addSeparationCharacterIfRequired(albumItem, dataBuilder, separationCharacter, i);
 					}
 				}
 			}
 			
 			if (firstLine) {
 				headerBuilder.append(System.lineSeparator());
 			}
 			
 			dataBuilder.append(System.lineSeparator());
 			firstLine = false;
 		}
 
 		try {
 			BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(filepath));
 			bufferedWriter.write(headerBuilder.toString() + dataBuilder.toString());
 			bufferedWriter.close();
 		} catch (IOException e) {
 			LOGGER.error("An error occured while writing the export data to its destinatation (" + filepath + ")", e);
 		}
 	}
 	
 	private static void addHeaderIfFirstLine(boolean firstLine, AlbumItem albumItem, StringBuilder headerBuilder, String separationCharacter, int fieldPosition) {
 		if (firstLine) {
 			headerBuilder.append(albumItem.getField(fieldPosition).getName());
 			addSeparationCharacterIfRequired(albumItem, headerBuilder, separationCharacter, fieldPosition);
 		}
 	}
 	
 	private static void addSeparationCharacterIfRequired(AlbumItem albumItem, StringBuilder builder, String separationCharacter, int fieldPosition) {
 		if (fieldPosition < albumItem.getFields().size() - 1) {
 			builder.append(separationCharacter);
 		}
 	}
 }
