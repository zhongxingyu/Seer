 package ru.chernobrivenko;
 
 import java.io.ByteArrayInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Properties;
 
 import com.ascentialsoftware.jds.Stage;
 
 
 public class ReadExcelStage extends Stage {

 	private Excel ex;
 
 	public void initialize() 
 	{ 
 		//Java Pack API Logging method to log messages to Director Log.
 		info("*****Initializing Excel Application*****"); 
 
 		//Code block to initalize and read user property values provided in the Stage GUI.
 		byte[] userProperties = getUserProperties().getBytes();
 		InputStream propertyStream = new ByteArrayInputStream(userProperties);
 		Properties properties = new Properties();
 
 		try
 		{
 			properties.load(propertyStream);  //User properties loaded.
 		}
 
 		//Log a message if user properties cannot be loaded.
 		catch(IOException e)
 		{
 			info("*****Could not load user properties");
 		} 	
 
 		//Read the value of the user property named ExcelFileName
		String src = properties.getProperty("ExcelFileName" ); 		
		String template = properties.getProperty("TemplateFileName" );
 		ex = new Excel();
 		try
 		{
			if(template.equals(""))
				ex.loadExcel(src);		
			else
				ex.loadExcel(src, template);
 		}
 		catch (FileNotFoundException e) 
 		{
 			//Log a message if the input excel file is not found
 			info("The input Excel file is not found"); 
 		}
 		catch(IOException ioExp)
 		{
 			//Log the IO Exception
 			info("*****IO Exception*****"); 
 		}	
 		catch(org.apache.poi.hssf.OldExcelFormatException UnsupportedExcelFormatException)
 		{
 			//Log a message if the Excel format is not supported.
 			info("*****Cannot read old Excel format*****"); 
 		}	
 	} 
 
 
 	public void terminate()
 	{
 		// Log information message while terminating the Application.	
 		info("*****Terminating Excel Application*****");
 	}
 
 
 	//The core processing logic of the Java application.
 	public int process() 
 	{	 
 		com.ascentialsoftware.jds.Row outputRow = createOutputRow();
 
 		if(ex.writeOutputRow(outputRow))								
 		{			
 			info("*****Writing Excel data to Target*****");			
 			//Write the row to the output link in the job
 			writeRow(outputRow);
 			return OUTPUT_STATUS_READY;				
 		}
 		return OUTPUT_STATUS_END_OF_DATA;
 	} 
 
 
 
 
 }
