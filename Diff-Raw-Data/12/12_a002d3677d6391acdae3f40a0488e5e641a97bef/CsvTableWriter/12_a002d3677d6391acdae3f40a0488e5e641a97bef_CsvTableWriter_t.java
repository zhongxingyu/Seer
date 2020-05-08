 /*******************************************************************************
  * CONAN: Copy Number Variation Analysis Software for
  * Next Generation Genome-Wide Association Studies
  * 
  * Copyright (C) 2009, 2010 Lukas Forer
  *  
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by 
  * the Free Software Foundation; either version 3 of the License, or
  * (at your option) any later version.
  *  
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *  
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, see <http://www.gnu.org/licenses/>.
  ******************************************************************************/
 
 package genepi.io.table.writer;
 
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 
 import au.com.bytecode.opencsv.CSVWriter;
 
 public class CsvTableWriter extends AbstractTableWriter {
 
 	private CSVWriter writer;
 	public String[] currentLine;
 	private Map<String, Integer> columns2Index = new HashMap<String, Integer>();
 
 	public CsvTableWriter(String filename) {
 		try {
 			writer = new CSVWriter(new FileWriter(filename));
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public CsvTableWriter(String filename, char separator) {
 		try {
 			writer = new CSVWriter(new FileWriter(filename), separator);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public CsvTableWriter(String filename, char separator, boolean quote) {
 		try {
 			writer = new CSVWriter(new FileWriter(filename), separator,
 					quote ? CSVWriter.DEFAULT_QUOTE_CHARACTER
							: CSVWriter.NO_QUOTE_CHARACTER, CSVWriter.NO_ESCAPE_CHARACTER);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public void close() {
 		try {
 			writer.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public int getColumnIndex(String column) {
 		return columns2Index.get(column);
 	}
 
 	@Override
 	public boolean next() {
 		writer.writeNext(currentLine);
 		for (int i = 0; i < currentLine.length; i++) {
 			currentLine[i] = "";
 		}
 		return true;
 	}
 
 	@Override
 	public void setColumns(String[] columns) {
 		currentLine = new String[columns.length];
 		for (int i = 0; i < columns.length; i++) {
 			columns2Index.put(columns[i], i);
 			currentLine[i] = "";
 		}
 		writer.writeNext(columns);
 	}
 
 	@Override
 	public void setDouble(int column, double value) {
 		currentLine[column] = value + "";
 	}
 
 	@Override
 	public void setInteger(int column, int value) {
 		currentLine[column] = value + "";
 	}
 
 	@Override
 	public void setString(int column, String value) {
 		currentLine[column] = value;
 	}
 
 	@Override
 	public void setRow(String[] row) {
 		currentLine = row;
 	}
 
 }
