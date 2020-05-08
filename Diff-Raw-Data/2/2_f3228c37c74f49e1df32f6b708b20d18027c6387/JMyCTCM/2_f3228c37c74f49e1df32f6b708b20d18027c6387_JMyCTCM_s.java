 package jMyCTCM;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 
 import boccaccio.andrea.ctmc.ICTCM;
 import boccaccio.andrea.filesystem.DirectoryFactory;
 import boccaccio.andrea.filesystem.FileFilterFactory;
 import boccaccio.andrea.inputFiles.FileLoaderFactory;
 import boccaccio.andrea.outputFiles.FileWriterFactory;
 
 public class JMyCTCM {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		List<String> ff = new ArrayList<>();
 		List<File> lf;
 		ICTCM tmpCTCM;
 		String inputFilename;
 		String outputFilename;
 		int i = -1;
 		int max = -1;
 		ff.add("ctmc");
 		lf = DirectoryFactory.getInstance().getDirectory(".").getFiles(FileFilterFactory.getInstance().getFileFilter(ff));
 		
 		max = lf.size();
 		
 		for(i=0;i<max;++i) {
 			try {
 				inputFilename = lf.get(i).getAbsolutePath();
				outputFilename = inputFilename.substring(0,inputFilename.lastIndexOf("ctmc")) + "output.ctmc";
 				tmpCTCM = FileLoaderFactory.getInstance().getFileLoader().load(inputFilename);
 				FileWriterFactory.getInstance().getFileWriter().write(tmpCTCM, outputFilename);
 				System.out.println(inputFilename + " correttamente elaborato risultati nel file " + outputFilename);
 				
 			} catch (Exception e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 	}
 
 }
