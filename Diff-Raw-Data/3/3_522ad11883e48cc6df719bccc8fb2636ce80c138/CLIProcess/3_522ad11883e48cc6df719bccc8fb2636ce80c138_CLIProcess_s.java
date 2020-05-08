 package org.vamdc.validator.cli;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 
 import org.vamdc.validator.OperationModes;
 import org.vamdc.validator.Setting;
 import org.vamdc.validator.interfaces.XSAMSIOModel;
 import org.vamdc.validator.interfaces.XSAMSValidatorException;
 import org.vamdc.validator.iocontroller.XSAMSDocument;
 import org.vamdc.validator.source.XSAMSSourceException;
 
 /**
  * Command-line interface process.
  * In command-line mode Java TAP-VAMDC plugin testing is not supported(yet)
  * @author doronin
  *
  */
 public class CLIProcess {
 
 	public static final int STATUS_ERROR=1;
 	public static final int STATUS_PROCESSED=0;
 	public static final int STATUS_DONE_NOTHING=-1;
 
 	private int status=STATUS_DONE_NOTHING;
 
 
 	public CLIProcess(OptionsParser parser){
 		//Check if we are asked to print usage?
 		if ( Boolean.TRUE.equals(parser.getOptionValue(parser.printUsage))) {
 			parser.printUsage();
 			System.exit(status);
 		}
 
 		//If we have output path defined, work in CLI mode
 		String outputPath = (String)parser.getOptionValue(parser.outputPath);
 		if (outputPath!=null){
 			File outputFile = new File(outputPath);
 			if (outputFile.isDirectory() && outputFile.canWrite()){
 				//Force network mode
 				Setting.OperationMode.setValue(OperationModes.network.name());
 				//Set status
 				status = STATUS_PROCESSED;
 				
 				XSAMSIOModel doc = new XSAMSDocument();
 				
 				Collection<String> queries = getQueries(parser, doc);
 				
 				int counter = 0;
 				try{
 					//For each query execute it, validate output and save both document and errors in output path.
 					for (String query:queries){
 
 						System.out.println(query);
 						try{
 							doc.doQuery(query);
 						}catch(XSAMSSourceException e){
 							e.printStackTrace();
 						} catch (XSAMSValidatorException e) {
 							e.printStackTrace();
 						}
 
 						saveOutput(doc,outputFile, counter++);
 					}
 				}catch(IOException e){
 					e.printStackTrace();
 					status++;
 				}finally{
 					doc.close();
 				}
 			}else{
 				status = STATUS_ERROR;
 			}
 
 		}
 	}
 
 	private Collection<String> getQueries(OptionsParser parser, XSAMSIOModel doc) {
 		Collection<String> queries = new ArrayList<String>();
 		String query=null;
 		while ((query=(String)parser.getOptionValue(parser.queryString))!=null)
 			queries.add(query);
 		if (queries.size()==0 && doc.getSampleQueries()!=null)
 			queries.addAll(doc.getSampleQueries());
 		return queries;
 	}
 
 	public int getStatus(){
 		return status;
 	}
 
 	/**
 	 * Save output file and report file to specified output folder
 	 * @param doc XSAMS validator model
 	 * @param basePath base path for files
 	 * @param index index of query in a list
 	 */
 	private void saveOutput(XSAMSIOModel doc, File basePath, int index) throws IOException{
 		//Check if base is a directory
 		if (!basePath.isDirectory())
 			throw new IOException("basePath is not a directory");
 
 		//Save XSAMS document
 		File xsamsDocument = new File(basePath.getAbsolutePath()+File.separator+"xsams"+index+".xml");
 		if (!xsamsDocument.exists()){
 			System.out.print("Writing "+xsamsDocument.getAbsolutePath()+" ...");
 			doc.saveFile(xsamsDocument);
 			System.out.println("Done");
 		}
 		else
 			throw new IOException("File"+xsamsDocument.getAbsolutePath()+" already exists!");
 
 		//Save status
 		File statusFile = new File(basePath.getAbsolutePath()+File.separator+"report"+index+".xml");
 		if (!statusFile.exists()){
 			System.out.print("Writing "+statusFile.getAbsolutePath()+" ...");
 			new XMLReport(doc,statusFile,xsamsDocument.getName()).write();
 			System.out.println("Done ");
 		}
 		else
 			throw new IOException("File"+statusFile.getAbsolutePath()+" already exists!");
 
 	}
 
 }
