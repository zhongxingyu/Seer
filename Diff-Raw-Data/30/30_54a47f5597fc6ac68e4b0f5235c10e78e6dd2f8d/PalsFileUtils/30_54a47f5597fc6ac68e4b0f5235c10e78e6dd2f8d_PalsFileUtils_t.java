 package pals.utils;
 
 import java.io.BufferedReader;
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.regex.Matcher;
 
 import org.apache.commons.exec.CommandLine;
 import org.apache.commons.exec.DefaultExecutor;
 import org.apache.commons.exec.PumpStreamHandler;
 import org.apache.log4j.Logger;
 
 import pals.Configuration;
 import pals.Globals;
 import pals.analysis.AnalysisException;
 import pals.entity.Analysis;
 import pals.entity.DataSet;
 import pals.entity.DataSetVersion;
 import pals.entity.ModelOutput;
 import pals.entity.User;
 
 
 public class PalsFileUtils {
 	
 	private static Logger log = Logger.getLogger(PalsFileUtils.class);
 	
 	public static void copyFileInJava(String srcPath, String targetPath) throws IOException {
 		log.info("Copying file " +srcPath+ " to " + targetPath);
 		FileInputStream in = null;
 		FileOutputStream out = null;  
 		File userFile = new File ( targetPath);
 		File srcFile = new File( srcPath);
 		try {
 	            in = new FileInputStream( srcFile );
 	            out = new FileOutputStream( userFile );
 	            int c;
 
 	            while ((c = in.read()) != -1) {
 	                out.write(c);
 	            }
 		} finally {
 	            if (in != null) {
 	                in.close();
 	            }
 	            if (out != null) {
 	                out.close();
 	            }
 		}
 	}
 	
 	public static void executeCommand(String exStr) throws AnalysisException, IOException, InterruptedException {
 		log.info("Executing command: " + exStr);
 		CommandLine commandLine = CommandLine.parse(exStr);
 		DefaultExecutor executor = new DefaultExecutor();
 		executor.setExitValue(Configuration.getInstance().DEFAULT_EXIT_CODE);
 		ByteArrayOutputStream stdout = new ByteArrayOutputStream();
         PumpStreamHandler psh = new PumpStreamHandler(stdout);
         executor.setStreamHandler(psh);
         try
         {
 		    int exitValue = executor.execute(commandLine);
 		    log.debug("Runtime call exit value was:" + exitValue);
         }
         catch( IOException e )
         {
        	throw new AnalysisException(parseOutputForError(stdout,exStr));
         }
 	}
 	
	public static String parseOutputForError(ByteArrayOutputStream output, String command) throws IOException
 	{	
		String error = "R script failure script: " + command;
 		ByteArrayInputStream in = new ByteArrayInputStream(output.toByteArray());
 		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
 		String line;
 		while( (line = reader.readLine()) != null)
 		{
 			for( String code : Configuration.getInstance().ERROR_CODES )
 			{
 				if( line.contains(code) )
 				{
 					log.debug("Error code picked up: " + code);
 					String[] splitLine = line.split(":");
 					if( splitLine.length >= 2 )
 					{
 						error = splitLine[1].trim();
 						for( int i=2; i < splitLine.length; ++i )
 						{
 							error += ":" + splitLine[i];
 						}
 						// just make sure it is only one sentence
 						String[] sentences = error.split("\\^");
 						if( sentences.length > 0 )
 						{
 							error = sentences[0].trim();
 						}
 					}
 				}
 			}
 		}
 		return error;
 	}
 	
 	public static String getUserDirPath(User user) {
 		return Configuration.getInstance().PATH_TO_APP_DATA + "/" + user.getUsername();
 	}
 	
 	private static String getModelOutputDirPath(Integer modelOutputId, User user) {
 		//return getUserDirectory(userName) + File.separator + Globals.MODEL_OUTPUT_DIR_PREFIX + modelOutputId;
 		return getUserDirPath(user);
 	}
 	
 	public static String getModelOutputDirPath(ModelOutput modelOutput) {
 		return getModelOutputDirPath(modelOutput.getId(), modelOutput.getOwner());
 	}
 	
 	public static String getModelOutputFilePathWithSuffix(Integer modelOutputId, User user, String suffix) {
 		return getModelOutputDirPath(modelOutputId, user) + File.separator + Globals.MODEL_OUTPUT_FILE_PREFIX + modelOutputId + suffix;
 	}
 	
 	private static String getModelOutputFilePath(Integer modelOutputId, User user) {
 		return getModelOutputFilePathWithSuffix(modelOutputId, user, Globals.NETCDF_FILE_SUFFIX);
 	}
 	
 	public static String getModelOutputFilePath(ModelOutput modelOutput) {
 		return getModelOutputFilePath(modelOutput.getId(), modelOutput.getOwner());
 	}
 	
 	public static String getDataSetDirPath(DataSet dataSet) {
 		//return Configuration.getInstance().PATH_TO_APP_DATA + File.separator + dataSet.getUserName() + File.separator + Globals.DATA_SET_DIR_PREFIX + dataSet.getDataSetId(); 
 		return getUserDirPath(dataSet.getOwner());
 	}
 	
 	public static String getDataSetVersionFilePathWidthSuffix(DataSetVersion dataSetVersion, String suffix) {
 		return getDataSetDirPath(dataSetVersion.getDataSet()) + "/" + Globals.DATA_SET_FILE_PREFIX + dataSetVersion.getDataSet().getId() + "." + dataSetVersion.getId() + suffix;
 	}
 	
 	public static String getDataSetVersionFluxFilePath(DataSetVersion dataSetVersion) {
 		return getDataSetVersionFilePathWidthSuffix(dataSetVersion, "_flux" + Globals.NETCDF_FILE_SUFFIX);
 	}
 	
 	public static String getDataSetVersionMetFilePath(DataSetVersion dataSetVersion) {
 		return getDataSetVersionFilePathWidthSuffix(dataSetVersion, "_met" + Globals.NETCDF_FILE_SUFFIX);
 	}
 	
 	public static File getDataSetVersionMetFile(DataSetVersion dataSetVersion) {
 		return new File(getDataSetVersionMetFilePath(dataSetVersion));
 	}
 	
 	public static File getDataSetVersionFluxFile(DataSetVersion dataSetVersion) {
 		return new File(getDataSetVersionFluxFilePath(dataSetVersion));
 	}
 	
 	public static String getDataSetVersionBenchmarkFilePath(DataSetVersion dataSetVersion) {
 		return getDataSetVersionFilePathWidthSuffix(dataSetVersion, "_bench" + Globals.NETCDF_FILE_SUFFIX);
 	}
 	
 	/* OLD METHOD
 	 * 
 	 * public static String getAnalysisRunFileLabel(AnalysisRun analRun) {
 		String fileName = Globals.MODEL_OUTPUT_FILE_PREFIX + analRun.getModelOutputId() + Globals.ANALYSIS_RUN_FILE_PREFIX + analRun.getAnalysisId();
 		return getModelOutputDirPath(analRun.getModelOutputId(), analRun.getModelOutput().getUser()) + File.separator + fileName;
 	}*/
 	
 	public static String getAnalysisRunFileLabel(Analysis analRun) {
 		//String fileName = Globals.MODEL_OUTPUT_FILE_PREFIX + analRun.getAnalysisRunId() + Globals.ANALYSIS_RUN_FILE_PREFIX + analRun.getAnalysisId();
 		String fileName = Globals.ANALYSIS_RUN_FILE_PREFIX + analRun.getId();
 		return getUserDirPath(analRun.getAnalysable().getOwner()) + File.separator + fileName;
 	}
 	
 	public static File getAnalysisRunFilePDF(Analysis analRun) {
 		return new File(getAnalysisRunFileLabel(analRun) + Globals.PDF_FILE_SUFFIX);
 	}
 	
 	public static File getAnalysisRunFileBenchPDF(Analysis analRun) {
 		return new File(getAnalysisRunFileLabelBench(analRun) + Globals.PDF_FILE_SUFFIX);
 	}
 	
 	public static File getAnalysisRunFilePNG(Analysis analRun) {
 		return new File(getAnalysisRunFileLabel(analRun) + Globals.PNG_FILE_SUFFIX);
 	}
 	
 	public static File getAnalysisRunFileBenchPNG(Analysis analRun) {
 		return new File(getAnalysisRunFileLabelBench(analRun) + Globals.PNG_FILE_SUFFIX);
 	}
 	
 	public static File getAnalysisRunFileThumb(Analysis analRun) {
 		return new File(getAnalysisRunFileLabel(analRun) + Globals.THUMB_FILE_SUFFIX);
 	}
 	
 	public static String correctSlashesForR(String input)
 	{
 		if( !System.getProperty("file.separator").equals("/") )
 		{
 		    input = input.replace('/','\\');
 		}
 		String doubleBackslash = input.replace("\\","\\\\");
 		return doubleBackslash;
 	}
 
 	public static String getAnalysisRunFileLabelBench(Analysis analRun) 
 	{
 		String fileName = Globals.ANALYSIS_RUN_FILE_PREFIX_BENCH + analRun.getId();
 		return getUserDirPath(analRun.getAnalysable().getOwner()) + File.separator + fileName;
 	}
 
 	
 
 }
