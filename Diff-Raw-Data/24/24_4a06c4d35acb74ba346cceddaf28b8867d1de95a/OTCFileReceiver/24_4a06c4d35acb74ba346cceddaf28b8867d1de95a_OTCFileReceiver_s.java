 /*
  * This file is part of OTC.
  * 
  * Copyright (c) 2011, Marius MOULIS <moulis.marius@gmail.com>
  *
  * OTC is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * OTC is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with OTC.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 /**
  * 
  * This class provides features to get a file onto the network with TCP
  * 
  */
 package utils.tcp;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.Socket;
 
 import utils.config.OTCConstants;
 
 import core.core.OTCParameters;
 
 public class OTCFileReceiver {
 	
 	/**
 	 * 
 	 * The socket used to get file content
 	 * 
 	 * */
 	private Socket socket;
 	
 	/**
 	 * 
 	 * {@link OTCParameters} used to get some parameters of file transfer
 	 * 
 	 * */
 	private OTCParameters otcp;
 	
 	public OTCFileReceiver(Socket socket, OTCParameters otcp){
 		this.socket = socket;
 		this.otcp = otcp;
 	}
 	
 	/**
 	 * 
 	 * Get back a file and store it to the appropriate location
 	 * @throws IOException
 	 * 
 	 * */
 	public void getFile() throws IOException {
 		
 		// Making reader for socket
 		BufferedReader socketReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
 		
 		// Getting fileName
 		String fileName = socketReader.readLine();
 		
 		// Build path for this file
		String path = (String)otcp.getParameterValue(OTCConstants.OTC_PARAMETER_HTML_FILE_SERVER_PATH) + fileName;
 		
 		// Make file
 		File outFile = new File(path); 
 		if (outFile.exists())
 			outFile.delete();
 		outFile.createNewFile();
 		
 		// Make writer
 		FileWriter writer = new FileWriter(outFile);
 		
 		// Buffer
 		String buffer = socketReader.readLine();
 		
 		System.out.println("Receiving " + buffer.length() + " bytes of data from " + socket);
 		
 		// Get content
 		while (buffer != null){
 			
 			// Write received content into target file
 			writer.write(buffer);
 			
 			// Read next content
 			buffer = socketReader.readLine();
 
 			// Output
 			if (buffer != null)
 				System.out.println("Receiving " + buffer.length() + " bytes of data from " + socket);
 		}
 		
 		// Close connection & file
 		System.out.print("Closing connexion from " + socket + "...");
 		socketReader.close();
 		writer.close();
 		System.out.println("done!");
 	}
 }
