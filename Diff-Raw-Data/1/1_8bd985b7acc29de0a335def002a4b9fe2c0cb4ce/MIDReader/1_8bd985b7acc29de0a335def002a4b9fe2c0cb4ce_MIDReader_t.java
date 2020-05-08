 /*
  * Acacia - GS-FLX & Titanium read error-correction and de-replication software.
  * Copyright (C) <2011>  <Lauren Bragg and Glenn Stone - CSIRO CMIS & University of Queensland>
  * 
  * 	This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *  
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *  
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package pyromaniac.IO;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedList;
 
 import pyromaniac.AcaciaEngine;
 import pyromaniac.DataStructures.MIDPrimerCombo;
 
 // TODO: Auto-generated Javadoc
 /**
  * The Class MIDReader.
  */
 public class MIDReader 
 {
 	
 	/** The input. */
 	BufferedReader input;
 	
 	/** The input location. */
 	String inputLocation;
 	
 	/**
 	 * Instantiates a new mID reader.
 	 *
 	 * @param inputFile the input file
 	 */
 	public MIDReader(String inputFile)
 	{
 		try 
 		{
 			if(inputFile == null || inputFile.length() == 0)
 			{
 				throw new Exception("Input file for MIDS is not valid");
 			}
 			
 			this.inputLocation = inputFile;
 			
 			URL pathToFile = getClass().getResource(this.inputLocation);
 			File f = new File(inputFile);
 
 			
 			if (pathToFile != null)
 				this.input = new BufferedReader(new InputStreamReader(pathToFile.openStream()));
 			else if(f.exists())	
 			{ 
 				this.input = new BufferedReader(new FileReader(inputFile));
 			}
 			else
 			{
 				throw new Exception("MID file not found: " + inputFile);
 			}
 		}
 		catch(Exception e)
 		{
 			System.out.println(e.getMessage());
 			e.printStackTrace();
 			System.exit(1);
 		}
 	}
 	
 	/**
 	 * Load mids.
 	 *
 	 * @return the linked list
 	 * @throws MIDFormatException the mID format exception
 	 */
 	public LinkedList<MIDPrimerCombo> loadMIDS() throws MIDFormatException
 	{
 		LinkedList <MIDPrimerCombo> MIDTags = new LinkedList <MIDPrimerCombo>();
 		int midCount = 0;
 		try
 		{
 			String line = input.readLine();
 			while (line != null) 
 			{
 				if(line.trim().length() == 0)
 				{
					line = input.readLine();
 					continue;
 				}
 				
 				String[] columns = line.split(",");
 				if(columns.length != 3)
 				{
 					columns = line.split("\t");
 					
 					if(columns.length != 3)
 						throw new MIDFormatException("Incorrect number of columns in " + inputLocation);
 				}
 				
 				// assuming that columns[0] is the MIDtag, and columns[1] is the
 				// descriptor.
 				
 				//for now, make the descriptor in the first column.
 				
 				String descriptor = columns[0].trim();				
 				String midTag = columns[1].trim();
 				String primerSeq = columns[2].trim();
 				
 				for(int i = 0; i < midTag.length();i++)
 				{
 					char curr = midTag.charAt(i);
 					if(! AcaciaEngine.getEngine().isIUPAC(curr))
 					{
 						throw new MIDFormatException("Invalid MID sequence: " + midTag);
 					}
 				}
 				
 				midTag = midTag.replace("\"", "");
 				descriptor = descriptor.replace("\"", "");
 				
 				//bugfix, to uppercase.
 				MIDTags.add(new MIDPrimerCombo(midTag.toUpperCase(), primerSeq.toUpperCase(), descriptor));
 				line = input.readLine();
 				midCount++;
 			}
 		} 
 		catch (IOException ie) 
 		{
 			System.out.println("An input exception occurred from " + inputLocation);
 		}
 		return MIDTags;
 	}
 	
 	/**
 	 * The Class MIDFormatException.
 	 */
 	public class MIDFormatException extends Exception
 	{
 		
 		/**
 		 * Instantiates a new mID format exception.
 		 *
 		 * @param message the message
 		 */
 		public MIDFormatException(String message)
 		{
 			super("MID formatting error: " + message);
 		}
 	}
 }
