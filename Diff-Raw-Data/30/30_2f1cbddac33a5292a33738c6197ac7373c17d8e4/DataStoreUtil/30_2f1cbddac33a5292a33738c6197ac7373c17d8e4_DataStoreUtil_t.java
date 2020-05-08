 /*
  * Last modification information:
 * $Revision: 1.3 $
 * $Date: 2004-11-23 17:49:28 $
  * $Author: scytacki $
  *
  * Licence Information
  * Copyright 2004 The Concord Consortium 
 */
 package org.concord.data.stream;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.io.Reader;
 import java.util.StringTokenizer;
 
 import org.concord.framework.data.stream.DataChannelDescription;
 import org.concord.framework.data.stream.DataStore;
 import org.concord.framework.data.stream.WritableDataStore;
 
 
 /**
  * DataStoreUtil
  * Class name and description
  *
  * Date created: Nov 15, 2004
  *
  * @author scott<p>
  *
  */
 public class DataStoreUtil
 {
 	public static void loadData(Reader reader, WritableDataStore ds, boolean hasHeaders)
 		throws IOException
 	{
 		BufferedReader lineReader = new BufferedReader(reader);
 
		ds.clearValues();
 		
 		if(hasHeaders) {
 			// parse the headers and set the channel descriptions
 		}
 
 		int row = 0;
 		int col = 0;
 		
 		String line = lineReader.readLine();
 		while(line != null) {
 			col = 0;
 			StringTokenizer toks = new StringTokenizer(line, "\t");
 			
 			while(toks.hasMoreTokens()) {
 				String strValue = toks.nextToken();
 				
 				try {
 					Float fValue = Float.valueOf(strValue);
 					ds.setValueAt(row, col, fValue);
 				} catch (NumberFormatException e) {
 					ds.setValueAt(row, col, strValue);
 				}
 				col++;
 			}
 			row++;
 
 			line = lineReader.readLine();
		}
		
 	}
 	
 	public static void printData(PrintStream outS, DataStore ds, 
 			int rowsToPrint[], boolean printHeaders)
 	{
 		int numRows, numCols;
 		numRows = ds.getTotalNumSamples();
 		numCols = ds.getTotalNumChannels();
 
 		if (printHeaders){
 			for (int j=0; j<numCols; j++){
 				DataChannelDescription cDesc = ds.getDataChannelDescription(j);
 				outS.print(cDesc.getName()+"\t");
 			}
 			outS.println("");
 		}
 
 		int r = 0;
 		boolean blnPrint;
 		for (int i=0; i<numRows; i++){
 			blnPrint = false;
 			if (rowsToPrint == null){
 				blnPrint = true;
 			}
 			else {
 				if (r < rowsToPrint.length && rowsToPrint[r] == i){
 					blnPrint = true;
 					r++;
 				}
 			}
 			if (blnPrint){
 				Object obj;
 				for (int j=0; j<numCols; j++){
 					obj = ds.getValueAt(i, j);
 					if (obj != null){
 						outS.print(obj.toString());
 					}
 					outS.print("\t");
 				}
 				outS.println("");
 			}
 		}
 	}
 
 }
