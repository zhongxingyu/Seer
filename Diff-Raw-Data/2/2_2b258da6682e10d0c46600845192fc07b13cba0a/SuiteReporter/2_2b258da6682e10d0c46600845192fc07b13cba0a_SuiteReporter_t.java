 package logreporter;
 import java.io.*;
 /*
 # Copyright (c) 2011, SugarCRM, Inc.
 # All rights reserved.
 #
 # Redistribution and use in source and binary forms, with or without
 # modification, are permitted provided that the following conditions are met:
 #    * Redistributions of source code must retain the above copyright
 #      notice, this list of conditions and the following disclaimer.
 #    * Redistributions in binary form must reproduce the above copyright
 #      notice, this list of conditions and the following disclaimer in the
 #      documentation and/or other materials provided with the distribution.
 #    * Neither the name of SugarCRM, Inc. nor the
 #      names of its contributors may be used to endorse or promote products
 #      derived from this software without specific prior written permission.
 #
 # THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 # AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 # IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
 # ARE DISCLAIMED. IN NO EVENT SHALL SugarCRM, Inc. BE LIABLE FOR ANY
 # DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 # (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 # LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 # ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 # (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 # SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
 /**
  * Takes in a File type input (should be the directory of .log files), reads all the log files and 
  * generate a nice html summary table of the tests. The generated html will be in the same directory 
  * 
  * @param folder - path of the folder containing SODA report logs
  *
  */
 public class SuiteReporter {
 	private File folder;
 	private File[] filesList;
 	private int count;
 	private String suiteName;
 	
 	/**
 	 * file reading stuff
 	 */
 	private FileReader input;
 	private BufferedReader br;
 	private String strLine, tmp;
 	
 	/**
 	 * file output stuff
 	 */
 	private FileOutputStream output;
 	private PrintStream repFile;
 	
 	//////////////////////////////
 	//Constructors
 	//////////////////////////////
 	/**
 	 * default constructor
 	 */
 	public SuiteReporter(){
 		folder = new File("");
 		filesList = new File[0];
 		count = 0;
 	}
 	
 	/**
 	 * Constructor for class SuiteReporter
 	 * @param folder - the File folder in which the suite log files reside.
 	 */
 	public SuiteReporter(File folder){
 		this.folder = folder;
 		filesList = folder.listFiles();
 		count = 0;
 		suiteName = folder.getName();
 		
 		/**
 		 * set up file output
 		 */
 		try{
 			output = new FileOutputStream(folder.getAbsolutePath()+"/"+suiteName+".html");
 			repFile = new PrintStream(output);
 		}catch(Exception e){
 			System.err.println("Error writing to file "+suiteName+".html");
 			e.printStackTrace();
 		}
 		
 	}
 	
 	/////////////////////////////
 	//report-generating methods
 	/////////////////////////////
 	/**
 	 * generates a html report file
 	 */
 	public void generateReport(){
 		generateHTMLHeader();
 		/**
 		 * find files in folder that ends with .log, and process them
 		 */
 		for (int i=0; i < filesList.length; i++){
 			//ignores nonfiles and hidden files
 			if (filesList[i].isFile() && !filesList[i].isHidden()){
 				//read if is .log file
 				if (filesList[i].getName().endsWith("log")){
 					readNextLog(filesList[i]);
 					//remove the .log extention
 					String temp = filesList[i].getName().substring(0, filesList[i].getName().indexOf("."));
 					//get last line
 					try {
 						while ((tmp = br.readLine()) != null){
 							strLine = tmp;
 						}
 					}catch (IOException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 					//find log status, generate table row
 					if (strLine.contains("blocked:1")){
 						generateTableRow(temp, 2);
 					}
 					else if (strLine.contains("result:-1")){
 						generateTableRow(temp, 0);
 					}
 					else{
 						generateTableRow(temp, 1);
 					}
 				}
 			}		
 		}
 		repFile.print("\n</table>\n</body>\n</html>\n");
 		repFile.close();
 	}
 	
 	/**
 	 * generates a html table row based on data from .log report file
 	 * 
 	 * @param fileName - name of the .log report file this table row is representing
 	 * @param failed - status of the test. 0 = failed, 1 = passed, 2 = blocked
 	 */
 	public void generateTableRow(String fileName, int status){
 		count ++;
 		repFile.println("<tr id=\""+count+"\" onMouseOver=\"this.className='highlight'\" onMouseOut=\"this.className='tr_normal'\" class=\"tr_normal\" >");
 		repFile.println("\t<td class=\"td_file_data\">"+count+"</td>");
 		repFile.println("\t<td class=\"td_file_data\">"+fileName+".xml</td>");
 		if (status == 0){
 			repFile.println("\t<td class=\"td_failed_data\">Failed</td>");
 		}
 		else if (status == 1){
 			repFile.println("\t<td class=\"td_passed_data\">Passed</td>");
 		}
 		else {
 			repFile.println("\t<td class=\"_data\">Blocked</td>");
 		}
		repFile.println("\t<td class=\"td_report_data\"><a href='Report-"+fileName+".html'>Report Log</a></td>");
 		repFile.println("</tr>");
 	}
 	
 	/**
 	 * generates the html header for the report file
 	 */
 	private void generateHTMLHeader(){
 		final String title = "suite "+suiteName+".xml test results";
 		repFile.println("<html>");
 		repFile.println("<style type=\"text/css\">");
 		repFile.println("table {");
 		repFile.println("\twidth: 100%;");
 		repFile.println("\tborder: 2px solid black;");
 		repFile.println("\tborder-collapse: collapse;");
 		repFile.println("\tpadding: 0px;");
 		repFile.println("\tbackground: #FFFFFF;");
 		repFile.println("}");
 		repFile.println(".td_header_master {");
 		repFile.println("\twhite-space: nowrap;");
 		repFile.println("\tbackground: #b6dde8;");
 		repFile.println("\ttext-align: center;");
 		repFile.println("\tfont-family: Arial;");
 		repFile.println("\tfont-weight: bold;");
 		repFile.println("\tfont-size: 12px;");
 		repFile.println("\tborder-left: 0px solid black;");
 		repFile.println("\tborder-right: 2px solid black;");
 		repFile.println("\tborder-bottom: 2px solid black;");
 		repFile.println("}");
 		repFile.println(".td_file_data {");
 		repFile.println("\twhite-space: nowrap;");
 		repFile.println("\ttext-align: left;");
 		repFile.println("\tfont-family: Arial;");
 		repFile.println("\tfont-weight: bold;");
 		repFile.println("\tfont-size: 12px;");
 		repFile.println("\tborder-left: 0px solid black;");
 		repFile.println("\tborder-right: 2px solid black;");
 		repFile.println("\tborder-bottom: 2px solid black;");
 		repFile.println("}");
 		repFile.println(".td_passed_data {");
 		repFile.println("\twhite-space: nowrap;");
 		repFile.println("\ttext-align: center;");
 		repFile.println("\tfont-family: Arial;");
 		repFile.println("\tfont-weight: bold;");
 		repFile.println("\tcolor: #00cc00;");
 		repFile.println("\tfont-size: 12px;");
 		repFile.println("\tborder-left: 0px solid black;");
 		repFile.println("\tborder-right: 2px solid black;");
 		repFile.println("\tborder-bottom: 2px solid black;");
 		repFile.println("}");
 		repFile.println("._data {");
 		repFile.println("\twhite-space: nowrap;");
 		repFile.println("\ttext-align: center;");
 		repFile.println("\tfont-family: Arial;");
 		repFile.println("\tfont-weight: bold;");
 		repFile.println("\tcolor: #FFCF10;");
 		repFile.println("\tfont-size: 12px;");
 		repFile.println("\tborder-left: 0px solid black;");
 		repFile.println("\tborder-right: 2px solid black;");
 		repFile.println("\tborder-bottom: 2px solid black;");
 		repFile.println("}");
 		repFile.println(".td_failed_data {");
 		repFile.println("\twhite-space: nowrap;");
 		repFile.println("\ttext-align: center;");
 		repFile.println("\tfont-family: Arial;");
 		repFile.println("\tfont-weight: bold;");
 		repFile.println("\tcolor: #FF0000;");
 		repFile.println("\tfont-size: 12px;");
 		repFile.println("\tborder-left: 0px solid black;");
 		repFile.println("\tborder-right: 2px solid black;");
 		repFile.println("\tborder-bottom: 2px solid black;");
 		repFile.println("}");
 		repFile.println(".td_failed_data_zero {");
 		repFile.println("\twhite-space: nowrap;");
 		repFile.println("\ttext-align: center;");
 		repFile.println("\tfont-family: Arial;");
 		repFile.println("\tfont-weight: normal;");
 		repFile.println("\tcolor: #FFFFFF;");
 		repFile.println("\tfont-size: 12px;");
 		repFile.println("\tborder-left: 0px solid black;");
 		repFile.println("\tborder-right: 2px solid black;");
 		repFile.println("\tborder-bottom: 2px solid black;");
 		repFile.println("}");		
 		repFile.println(".td_report_data {");
 		repFile.println("\twhite-space: nowrap;");
 		repFile.println("\ttext-align: center;");
 		repFile.println("\tfont-family: Arial;");
 		repFile.println("\tfont-weight: normal;");
 		repFile.println("\tfont-size: 12px;");
 		repFile.println("\tborder-left: 0px solid black;");
 		repFile.println("\tborder-right: 2px solid black;");
 		repFile.println("\tborder-bottom: 2px solid black;");
 		repFile.println("}");
 		repFile.println(".highlight {");
 		repFile.println("\tbackground-color: #8888FF;");
 		repFile.println("}");
 		repFile.println(".tr_normal {");
 		repFile.println("\tbackground-color: #e5eef3;");
 		repFile.println("}");
 		repFile.println("</style>");
 		repFile.println("<title>"+title+"</title>");
 		repFile.println("<body>");
 		repFile.println("<table id=\"tests\">");
 		repFile.println("<tr id=\"header\">");
 		repFile.println("\t<td class=\"td_header_master\" colspan=\"4\">");
 		repFile.println("\tSuite:"+suiteName+".xml Test Results");
 		repFile.println("</td>");
 		repFile.println("<tr id=\"header_key\">");
 		repFile.println("\t<td class=\"td_header_master\"></td>");
 		repFile.println("\t<td class=\"td_header_master\">Test File</td>");
 		repFile.println("\t<td class=\"td_header_master\">Status</td>");
 		repFile.println("\t<td class=\"td_header_master\">Report Log</td>");
 		repFile.println("</tr>");
 	}
 	
 	/////////////////////////////
 	//misc. methods
 	/////////////////////////////
 	
 	/**
 	 * sets up FileReader and BufferedReader for the next report file
 	 * @param inputFile - a properly formatted .log SODA report file
 	 */
 	private void readNextLog(File inputFile){
 		try{
 			/*sets up file reader to read input one character at a time*/
 			input = new FileReader(inputFile);
 			/*sets up buffered reader to read input one line at a time*/
 			br = new BufferedReader(input);
 		}catch (FileNotFoundException e){
 			System.err.println("file not found: "+inputFile);
 		}catch (Exception e){
 			System.err.println("error reading file" + inputFile);
 		}
 	}
 
 }
