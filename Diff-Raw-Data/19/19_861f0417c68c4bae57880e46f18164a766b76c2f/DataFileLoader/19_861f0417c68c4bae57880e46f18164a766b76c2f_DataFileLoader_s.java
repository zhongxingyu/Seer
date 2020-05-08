 /*
  * Copyright (c) 2004-2007 Rensselaer Polytechnic Institute
  * Copyright (c) 2007 NEES Cyberinfrastructure Center
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
  *
  * For more information: http://nees.rpi.edu/3dviewer/
  */
 
 package org.nees.rpi.vis.loaders;
 
 import com.infomata.data.*;
 
 import java.net.URL;
 import java.io.IOException;
 import java.io.File;
 import java.util.Vector;
 import java.util.ArrayList;
 
 import org.nees.rpi.util.StringTransformer;
 import org.nees.rpi.vis.model.DVDataChannel;
 import org.nees.rpi.vis.model.DVDataFile;
 
 /**
  * Loads delilmited data files. Data files are expected to have
  * two header lines:
  * 	- First line, required: names of the data columns
  *  - Second line, optional: units for the data in the column
  */
 public class DataFileLoader
 {
 	/** the file name of the file parsed for data */
 	String currentFilename;
 
 	/** flag to check if a file had a line for units or not */
 	boolean hasUnits = false;
 
 	DelimiterType delimiterType;
 	String delimiter = null;
 	DataFile reader = null;
 	int skipRows = 0;
 
 	/** all the data channels loaded from the data file */
 	private ArrayList<DVDataChannel> channels = new ArrayList<DVDataChannel>();
 
 	public DataFileLoader(DelimiterType delimType)
 	{
 		this(delimType, null, null);
 	}
 
 	public DataFileLoader(DelimiterType delimType, String delimiter, String escape)
 	{
 		delimiterType = delimType;
 		this.delimiter = delimiter;
 
 		reader = DataFile.createReader("UTF8");
 		switch(delimType) {
 		case TAB_DELIMITED:
 			reader.setDataFormat(new TabFormat());
 			break;
 		case COMMA_DELIMITED:
 			reader.setDataFormat(new CSVFormat());
 			break;
 		case OTHER:
 			reader.setDataFormat(new SimpleDelimiterFormat(delimiter, escape));
 			break;
 		default:
 			reader.setDataFormat(new TabFormat());
 			break;
 		}
 		reader.containsHeader(true);
 	}
 
 	public static DVDataFile loadFile(URL dataFile,
 	                                  int skipRows,
 	                                  DelimiterType delimType,
 	                                  String delimiter,
 	                                  String escape)
 		throws DataFileLoaderException
 	{
 		DataFileLoader dataloader = new DataFileLoader(delimType, delimiter, escape);
 		dataloader.setSkipRows(skipRows);
 		return dataloader.loadFile(dataFile);
 	}
 
 	/**
 	 * return's the file name for the original file parsed to read
 	 * in the data from
 	 *
 	 * @return	the file name containing the data
 	 */
 	public String getFilename()
 	{
 		return currentFilename;
 	}
 
 	public void setSkipRows(int skip)
 	{
 		this.skipRows = skip;
 	}
 
 	public float[] getColumn(String columnHeading)
 	{
 		for (DVDataChannel channel : channels) {
 			if (channel.getName().compareToIgnoreCase(columnHeading) == 0)
 				return channel.getData();
 		}
 
 		return null;
 	}
 
 	/**
 	 * To check if the parsed file had unit information in it.
 	 * @return
 	 * 	return true if it had unit information, false otherwise
 	 */
 	public boolean hasUnits()
 	{
 		return hasUnits;
 	}
 
 	/**
 	 * Lookup for the unit information of a column.
 	 * @param columnHeading
 	 * @return
 	 * 	Returns null if the data file had no unit information
 	 * 	or if the column heading doesn't match any of the ones
 	 *  in the data file.
 	 */
 	public String getUnit(String columnHeading)
 	{
 		if (!hasUnits)
 			return null;
 
 		for (DVDataChannel channel : channels) {
 			if (channel.getName().compareToIgnoreCase(columnHeading) == 0)
 				return channel.getUnit();
 		}
 
 		return null;
 	}
 
 	/**
 	 * Returns an array of all the column headers from the loaded
 	 * datafile
 	 * @return		array of the column headers
 	 */
 	public String[] getChannelNames()
 	{
		String[] outHeaders = new String[channels.size()-1];
 
		for(int i = 0; i < channels.size(); i++)
			outHeaders[i-1] = channels.get(i).getName();
 
 		return outHeaders;
 	}
 
     @SuppressWarnings("unchecked")  // Uses 3rd party library, can't fix this
 	public DVDataFile loadFile(URL dataFile) throws DataFileLoaderException
 	{
 		String columnHeaders[] = null;
 		String units[] = null;
 		float data[][] = null;
 		DVDataFile dvdatafile = new DVDataFile(new File(dataFile.getFile()), delimiterType, delimiter, skipRows);
 
 		try {
 			reader.open(dataFile);
 			currentFilename = dataFile.getFile();
 			columnHeaders = (String[]) reader.getHeaderList().toArray(new String[reader.getHeaderList().size()]);
 
 			//cleanup headers; trim and change to lowercase
 			for (int i = 0; i < columnHeaders.length; i++)
 				columnHeaders[i] = columnHeaders[i].trim().toLowerCase();
 
 			DataRow row = reader.next();
 			int numCols = row.size();
 
 			//check that the number of column headers matches the number of data columns
 			if (columnHeaders.length != numCols)
 				throw new DataFileLoaderException("Data Inconsistancy: The number of " +
 								"headings don't match the number of data columns");
 
 			Vector vData[] = new Vector[numCols];
 			for (int i = 0; i < numCols; i++)
 				vData[i] = new Vector();
 
 			for (int counter = 1; row != null; row = reader.next()) {
 				//check that the number of data columns is consistent
 				//for this data row
 				if (numCols != row.size())
 					throw new DataFileLoaderException("Data Inconsistancy: inconsistent " +
 					                                  "number of data columns in data row " +
 					                                  getRowNumberFromCounter(counter));
 
 				//check if the first row after the headers contains unit
 				//information
 				if (counter == 1)
 				{
 					if (!StringTransformer.isNumber(row.getString(0)))
 					{
 						hasUnits = true;
 						units = new String[numCols];
 						for (int i=0; i<numCols; i++)
 						{
 							units[i] = row.getString(i);
 						}
 						continue;
 					}
 				}
 
 				for (int i=0; i<numCols; i++)
 					vData[i].add((float) row.getDouble(i));
 
 				counter++;
 
 				for (int i=0; i<skipRows; i++)
 				{
 					row = reader.next();
 					counter++;
 				}
 			}
 
 			int numRows = vData[0].size();
 			data = new float[numCols][];
 			for (int i=0; i<numCols; i++)
 			{
 				data[i] = new float[numRows];
 				for (int j=0; j<numRows; j++)
 					data[i][j] = (Float) vData[i].get(j);
 			}
 		} catch (IOException e) {
 			throw new DataFileLoaderException("Error loading data file", e);
 		} finally {
 			reader.close();
 
 			//move all info to channel objects
 			if (columnHeaders != null && data != null)
 			{
 				for (int i=0; i<columnHeaders.length; i++)
 				{
 					DVDataChannel newChannel;
 					if (hasUnits)
 						newChannel = new DVDataChannel(columnHeaders[i], units[i], data[i]);
 					else
 						newChannel = new DVDataChannel(columnHeaders[i], data[i]);
 					newChannel.setParent(dvdatafile);
 					channels.add(newChannel);
 				}
 			}
 		}
 
 		dvdatafile.setChannels(channels);
 		return dvdatafile;
 	}
 
 	private int getRowNumberFromCounter(int counter)
 	{
 		return hasUnits() ? counter + 2 : counter + 1;
 	}
 }
