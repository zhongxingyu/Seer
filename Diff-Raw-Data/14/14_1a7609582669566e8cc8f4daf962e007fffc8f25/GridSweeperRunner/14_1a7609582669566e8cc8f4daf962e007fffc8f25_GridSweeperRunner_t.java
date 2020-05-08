 package edu.umich.lsa.cscs.gridsweeper;
 
 import java.io.*;
 
 import static edu.umich.lsa.cscs.gridsweeper.StringUtils.*;
 
 import edu.umich.lsa.cscs.gridsweeper.parameters.ParameterMap;
 
 /**
  * The GridSweeperRunner command-line tool to actually run the model
  * on the execution host.
  * @author Ed Baskerville
  *
  */
 public class GridSweeperRunner
 {
 	/**
 	 * Runs the model. First, reads the {@link RunSetup} object from standard input,
 	 * and extracts settings for the run. If file transfer is on, input files
 	 * are then downloaded from the file transfer system. Then an adapter object
 	 * is created as specified in the run setup and used to actually run the model.
 	 * Finally, if necessary, files are staged back to the file transfer system
 	 * to be retrieved at the submission host. 
 	 */
 	public static void main(String[] args)
 	{
 		RunResults results;
 		try
 		{
 			// Load RunSetup object
			RunSetup setup;
			try
			{
				ObjectInputStream stdinStream = new ObjectInputStream(System.in);
				setup = (RunSetup)stdinStream.readObject();
			}
			catch(Exception e)
			{
				ObjectInputStream stdinFileStream =
					new ObjectInputStream(new FileInputStream(".gsweep_in.0"));
				setup = (RunSetup)stdinFileStream.readObject();
			}
 			
 			// Get GridSweeper settings
 			Settings settings = setup.getSettings();
 			
 			// Download input files
 			boolean useFileTransfer = settings.getBooleanProperty("UseFileTransfer");
 			FileTransferSystem fts = null;
 			if(useFileTransfer)
 			{
 				String className = settings.getSetting("FileTransferSystemClassName");
 				Settings ftsSettings = settings.getSettingsForClass(className);
 				fts = FileTransferSystemFactory.getFactory().getFileTransferSystem(className, ftsSettings);
 				fts.connect();
 				
 				StringMap inputFiles = setup.getInputFiles();
 				
 				for(String key : inputFiles.keySet())
 				{
 					String path = inputFiles.get(key);
 					String fileTransferSubpath = appendPathComponent(setup.getFileTransferSubpath(), "input");
 					
 					String remotePath = appendPathComponent(fileTransferSubpath, path);
 					String localPath = path;
 					
 					fts.downloadFile(remotePath, localPath);
 				}
 				
 				fts.disconnect();
 			}
 			
 			String adapterClassName = settings.getSetting("AdapterClass");
 			Settings adapterSettings = settings.getSettingsForClass(adapterClassName);
 			Adapter adapter = AdapterFactory.createAdapter(adapterClassName, adapterSettings);
 			
 			// Run!
 			ParameterMap parameters = setup.getParameters();
 			int runNumber = setup.getRunNumber();
 			int rngSeed = setup.getRngSeed();
 			results = adapter.run(parameters, runNumber, rngSeed);
 			
 			// Stage files listed in run properties back to server (if asked for)
 			if(useFileTransfer)
 			{
 				fts.connect();
 				
 				StringList outputFiles = setup.getOutputFiles();
 				
 				for(String outputFile : outputFiles)
 				{
 					String fileTransferSubpath = setup.getFileTransferSubpath();
 					String remotePath = appendPathComponent(fileTransferSubpath, outputFile);
 					
 					fts.uploadFile(outputFile, remotePath);
 				}
 				
 				fts.disconnect();
 			}
 			
 			// If file transfer is off, write standard output and standard error
 			// to local files.
 			// If file transfer is on, this will happen at the client end of things
 			if(!useFileTransfer)
 			{
 				String stdoutFilename = "stdout." + runNumber;
 				byte[] stdoutData = results.getStdoutData();
 				writeData(stdoutFilename, stdoutData);
 				
 				String stderrFilename = "stderr." + runNumber;
 				byte[] stderrData = results.getStderrData();
 				writeData(stderrFilename, stderrData);
 			}
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			results = new RunResults(e); 
 		}
 		
 		// Write results to stdout
 		// Set up ObjectOutputStream to write RunResults object
 		try
 		{
 			ObjectOutputStream stdoutStream = new ObjectOutputStream(System.out);
 			stdoutStream.writeObject(results);
 		}
 		catch(Exception e) {}
 	}
 
 	private static void writeData(String filename, byte[] data) throws IOException
 	{
 		OutputStream os = new BufferedOutputStream(new FileOutputStream(filename));
 		os.write(data);
 		os.close();
 	}
 }
