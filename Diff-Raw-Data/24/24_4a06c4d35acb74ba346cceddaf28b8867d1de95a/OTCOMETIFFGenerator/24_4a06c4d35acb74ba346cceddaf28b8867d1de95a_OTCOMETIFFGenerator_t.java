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
  * This class provides features to convert OME-XML File + TIFF file into OME-TIFF File
  * 
  */
 package core.core;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.net.Socket;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.StringTokenizer;
 
 
 import loci.common.RandomAccessInputStream;
 import loci.formats.tiff.TiffSaver;
 
 import utils.config.OTCConstants;
 import utils.html.OTCHTMLGenerator;
 import utils.log.OTCLogger;
 import utils.xml.OTCOMEXMLGenerator;
 
 import core.metadata.OTCMetadataGroup;
 
 import ihm.OTCGeneratorConsole;
 
 
 public class OTCOMETIFFGenerator {
 	
 	/**
 	 * 
 	 * Singleton
 	 * 
 	 * */
 	private static OTCOMETIFFGenerator generator;
 	
 	/**
 	 * 
 	 * {@link OTCParameters} used to send HTML file
 	 * 
 	 * */
 	private OTCParameters otcp;
 	
 	/**
 	 * 
 	 * A console used to output
 	 * 
 	 * */
 	private OTCGeneratorConsole console;
 	
 	/**
 	 * 
 	 * List of TIFF files to be converted 
 	 * 
 	 * */
 	private ArrayList<File> tiffFiles;
 	
 	/**
 	 * 
 	 * List of OME-XML meta data to include in TIFF files
 	 * 
 	 * */
 	private ArrayList<OTCMetadataGroup> metadatas;
 	
 	/**
 	 * 
 	 * Default constructor
 	 * 
 	 * */
 	private OTCOMETIFFGenerator(OTCParameters otcp){
 		this.otcp = otcp;
 	}
 	
 	/**
 	 * 
 	 * Return the OME-TIFF Generator
 	 * 
 	 * */
 	public static OTCOMETIFFGenerator getGenerator(OTCParameters otcp){
 		if (generator == null)
 			generator = new OTCOMETIFFGenerator(otcp);
 		
 		return generator;
 	}
 	
 
 	/**
 	 * 
 	 * Set TIFF files to be converted
 	 * @param tiffFiles TIFF files to be converted
 	 *
 	 */
 	public void setTiffFiles(ArrayList<File> tiffFiles) {
 		this.tiffFiles = tiffFiles;
 	}
 
 	/**
 	 * 
 	 * Set meta data to be included in TIFF files
 	 * @param metadatas Meta data to be included
 	 *
 	 */
 	public void setMetadatas(ArrayList<OTCMetadataGroup> metadatas) {
 		this.metadatas = metadatas;
 	}
 
 	/**
 	 * 
 	 * Set a console for the generator
 	 * 
 	 * @param console Console to output
 	 *
 	 */
 	public void setConsole(OTCGeneratorConsole console) {
 		this.console = console;
 	}
 	
 	/**
 	 * 
 	 * Generate OME-TIFF files
 	 * @param otcp {@link OTCParameters} used to generate OME-TIFF file
 	 * 
 	 * */
 	public void genOMETIFFFiles(final OTCParameters otcp){
 		Thread t = new Thread(new Runnable() {
 			@Override
 			public void run() {
 				OTCOMEXMLGenerator xmlGenerator = OTCOMEXMLGenerator.getGenerator();
 				OTCHTMLGenerator htmlGenerator = OTCHTMLGenerator.getGenerator();
 				OTCLogger logger = OTCLogger.getLogger();
 				File logFile = logger.getLogFile(new File((String)otcp.getParameterValue(OTCConstants.OTC_PARAMETER_LOG_FILE_KEY)).getAbsolutePath());
 				console.getConsole().setText("## THIS OUTPUT WILL BE DUPLICATE INTO : " + logFile.getAbsolutePath() + "\n");
 				console.addTextInConsole("=== Conversion Start ===\n");
 				for (int i = 0 ; i < tiffFiles.size() ; i++){
 					logger.log(logFile, "Conversion of file " + tiffFiles.get(i).getAbsolutePath() + "\n");
 					String tmpString1;
 					String tmpString2 = "";
 					console.addTextInConsole("\n** Handle file : " + tiffFiles.get(i).getAbsolutePath() +" **\n");
 					
 					// Generating HTML File
 					try {
 						console.addTextInConsole("Build HTML file for \"" + tiffFiles.get(i).getAbsolutePath() + "\"...");
 						logger.log(logFile, "Build HTML file for \"" + tiffFiles.get(i).getAbsolutePath() + "\"...\n");
 						tmpString2 = htmlGenerator.genHTML(metadatas.get(i), otcp);
 						console.addTextInConsole("done!\n\t==>HTML file genetared at : \"" + new File((String)otcp.getParameterValue(OTCConstants.OTC_PARAMETER_HTML_DEFAULT_PATH_IDENTIFIER_KEY)).getAbsolutePath() + "/" + tmpString2 + "\"\n");
 						logger.log(logFile, "\t==> HTML file genetared at : \"" + new File((String)otcp.getParameterValue(OTCConstants.OTC_PARAMETER_HTML_DEFAULT_PATH_IDENTIFIER_KEY)).getAbsolutePath() + "/" + tmpString2 + "\"\n");
 					} catch (Exception e) {
 						console.addTextInConsole("\nError building HTML : " + e.getMessage() + "\n=== Stack Trace ===\n");
 						logger.log(logFile, "\nError building HTML : " + e.getMessage() + "\n=== Stack Trace ===\n");
 						for (StackTraceElement el : e.getStackTrace()){
 							console.addTextInConsole(el.toString()+ "\n");
 							logger.log(logFile, el.toString()+ "\n\n");
 						}
 					}
 					
 					// Generating OME -XML
 					try {
 						console.addTextInConsole("Build OME-XML file for \"" + tiffFiles.get(i).getAbsolutePath() + "\"...");
 						logger.log(logFile, "Build OME-XML file for \"" + tiffFiles.get(i).getAbsolutePath() + "\"...\n");
 						tmpString1 = xmlGenerator.genOMEXMLFile(metadatas.get(i), otcp, tmpString2);
 						console.addTextInConsole("done!\n\t==>OME-XML file genetared at : \"" + new File((String)otcp.getParameterValue(OTCConstants.OTC_PARAMETER_OME_XML_DEFAULT_PATH_IDENTIFIER_KEY)).getAbsolutePath() + "/" + tmpString1 + "\"\n");
 						logger.log(logFile, "\t==> OME-XML file genetared at : \"" + new File((String)otcp.getParameterValue(OTCConstants.OTC_PARAMETER_OME_XML_DEFAULT_PATH_IDENTIFIER_KEY)).getAbsolutePath() + "/" + tmpString1 + "\"\n");						
 					} catch (Exception e) {
 						console.addTextInConsole("\nError building OME-XML : " + e.getMessage() + "\n=== Stack Trace ===\n");
 						logger.log(logFile, "\nError building OME-XML : " + e.getMessage() + "\n=== Stack Trace ===\n");
 						for (StackTraceElement el : e.getStackTrace()){
 							console.addTextInConsole(el.toString()+ "\n");
 							logger.log(logFile, el.toString()+ "\n\n");
 						}
 					}
 					
 					// Generating OME-TIFF File
 					try {
 						
 						console.addTextInConsole("Building OME-TIFF file");
 						logger.log(logFile, "Building OME-TIFF file...\n");
 						// Make new file with correct extension
 						String oMETIFFFilePath = new File((String)otcp.getParameterValue(OTCConstants.OTC_PARAMETER_OME_TIFF_DEFAULT_PATH_IDENTIFIER_KEY)).getAbsolutePath() + "/" + makeIMETIFFFileNameWithExtention(tiffFiles.get(i).getName());
 						File outFile = new File(oMETIFFFilePath);
 						if (outFile.exists())
 							outFile.delete();
 						outFile.createNewFile();
 						
 						// Copying file
 						FileInputStream in = new FileInputStream(tiffFiles.get(i));
 						FileOutputStream out = new FileOutputStream(outFile);
 						byte buffer[] = new byte[512 * 1024];
 						int readCount;
 						while ((readCount = in.read(buffer)) != -1){
 							out.write(buffer, 0, readCount);
 							console.addTextInConsole(".");
 						}
 						in.close();
 						out.close();
 						
 						// Put XML in TIFF file header
 						// TODO : Handle Original meta data
 						TiffSaver saver = new TiffSaver(outFile.getAbsolutePath());
 						RandomAccessInputStream input = new RandomAccessInputStream(outFile.getAbsolutePath());
 						
 						saver.overwriteComment(input, xmlGenerator.genOMEXML(metadatas.get(i)).replaceFirst("###", (String)otcp.getParameterValue(OTCConstants.OTC_PARAMETER_OMERO_HTTP_ADDRESS) + (String)otcp.getParameterValue(OTCConstants.OTC_PARAMETER_OMERO_HTML_DIRECTORY) + tmpString2));
 						input.close();
 						
 						console.addTextInConsole("done!\n\t==>OME-TIFF file genetared at : \"" + oMETIFFFilePath + "\"\n");
 						logger.log(logFile, "\t==> OME-TIFF file genetared at : \"" + oMETIFFFilePath + "\"\n");
 						
 					} catch (Exception e) {
 						console.addTextInConsole("\nError generating OME-TIFF file : " + e.getMessage() + "\n=== Stack Trace ===\n");
 						logger.log(logFile, "\nError generating OME-TIFF file : " + e.getMessage() + "\n=== Stack Trace ===\n");
 						for (StackTraceElement el : e.getStackTrace()){
 							console.addTextInConsole(el.toString()+ "\n");
 							logger.log(logFile, el.toString()+ "\n\n");
 						}
 					}
 					
 					// Send HTML File to OMERO Server
 					File htmlFile = new File((String)otcp.getParameterValue(OTCConstants.OTC_PARAMETER_HTML_DEFAULT_PATH_IDENTIFIER_KEY) + "/" + tmpString2);
 					console.addTextInConsole("Sending HTML File : \"" + htmlFile.getAbsolutePath() + "\" to OMERO server...");
 					logger.log(logFile, "Sending HTML File : \"" + htmlFile.getAbsolutePath() + "\" to OMERO server...\n");
 					sendHTML(htmlFile, logger, logFile);
 					console.addTextInConsole("done!\n\n");
 					logger.log(logFile, "\t==> done!\n\n\n\n");
 					
 				}
 				console.addTextInConsole("=== Conversion Finished ===\n");				
 			}
 		});
 		t.start();
 	}
 	
 	/**
 	 * 
 	 * Change the extension of the TIFF File to OME-TIFF
 	 * @param orginalName Original name of the TIFF file
 	 * @return new file name
 	 * 
 	 * */
 	private String makeIMETIFFFileNameWithExtention(String orginalName){
 		
 		StringTokenizer st = new StringTokenizer(orginalName, ".");
 		String newName = "";
 		for (int i = 0 ; i < st.countTokens() - 1 ; i++){
 			if (i != 0)
 				newName += ("." + st.nextToken());
 			else
 				newName += st.nextToken();
 		}
 		
 		return newName + ".ome.tiff";
 	}
 	
 	/**
 	 * 
 	 * Send generated HTLM file over network
 	 * @param fileToBeSend HTML file we want to send to OMERO server
 	 * @param logger {@link OTCLogger} used to print logs
 	 * @param logFile {@link File} that the log has to be printed into
 	 * 
 	 * */
 	private void sendHTML(File fileToBeSend, OTCLogger logger, File logFile){
 		Socket socket;
 		PrintStream outputStream;
		String host = (String)otcp.getParameterValue(OTCConstants.OTC_PARAMETER_OMERO_HOST);
 		int port = Integer.parseInt((String)otcp.getParameterValue(OTCConstants.OTC_PARAMETER_LISTENING_PORT));
 		
 		try {
 			
 			// Build socket			
 			logger.log(logFile, "Building socket...\n");
 			socket = new Socket(host, port);
 			logger.log(logFile, "\t==> done!\n");
 			
 			// Build output stream
 			logger.log(logFile, "Building outputstream...\n");
 			outputStream = new PrintStream(socket.getOutputStream());
 			logger.log(logFile, "\t==> done!\n");
 			
 			// Sending file name
 			logger.log(logFile, "Sending file name...\n");
 			outputStream.println(fileToBeSend.getName());
 			logger.log(logFile, "\t==> done!\n");
 			
 			// Build file reader
 			BufferedReader reader = new BufferedReader(new FileReader(fileToBeSend));
 			
 			// Sending file
 			logger.log(logFile, "Sending file...\n");
 			String buffer = reader.readLine();
 			while (buffer != null){
 				outputStream.println(buffer);
 				buffer = reader.readLine();
 			}
 			logger.log(logFile, "\t==> done!\n");
 			
 			// Closing file & connection
 			logger.log(logFile, "Closing connection to server...\n");
 			outputStream.close();
 			reader.close();
 			logger.log(logFile, "\t==> done!\n");
 			
 		} catch (UnknownHostException e) {
 			logger.log(logFile, "Unable to send file : " + e.getMessage() + " \n");
			e.printStackTrace();
 		} catch (IOException e) {
			e.printStackTrace();
 			logger.log(logFile, "Unable to send file : " + e.getMessage() + " \n");
 		}
 	}
 }
