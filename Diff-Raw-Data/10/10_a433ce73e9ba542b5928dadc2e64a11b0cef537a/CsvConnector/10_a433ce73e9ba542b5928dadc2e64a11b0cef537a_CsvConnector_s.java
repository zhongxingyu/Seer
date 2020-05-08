 /*
  * Copyright (c) 2009, GoodData Corporation. All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification, are permitted provided
  * that the following conditions are met:
  *
  *     * Redistributions of source code must retain the above copyright notice, this list of conditions and
  *        the following disclaimer.
  *     * Redistributions in binary form must reproduce the above copyright notice, this list of conditions
  *        and the following disclaimer in the documentation and/or other materials provided with the distribution.
  *     * Neither the name of the GoodData Corporation nor the names of its contributors may be used to endorse
  *        or promote products derived from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS
  * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
  * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
  * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
  * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
  * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 package com.gooddata.connector;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 import java.net.URLDecoder;
 
 import com.gooddata.connector.backend.ConnectorBackend;
 import com.gooddata.connector.driver.Constants;
 import com.gooddata.csv.DataTypeGuess;
 import com.gooddata.exception.ProcessingException;
 import com.gooddata.modeling.model.SourceColumn;
 import com.gooddata.modeling.model.SourceSchema;
 import com.gooddata.processor.CliParams;
 import com.gooddata.processor.Command;
 import com.gooddata.processor.ProcessingContext;
 import com.gooddata.util.FileUtil;
 import com.gooddata.util.NameTransformer;
 import com.gooddata.util.StringUtil;
 import org.apache.log4j.Logger;
 
 /**
  * GoodData CSV Connector
  *
  * @author zd <zd@gooddata.com>
  * @version 1.0
  */
 public class CsvConnector extends AbstractConnector implements Connector {
 
     private static Logger l = Logger.getLogger(CsvConnector.class);
 
     // data file
     private File dataFile;
     
      //hasheader flag
     private boolean hasHeader;
 
     /**
      * Creates GoodData CSV connector
      * @param backend connector backend
      */
     protected CsvConnector(ConnectorBackend backend) {
         super(backend);
     }
 
     /**
      * Create a new GoodData CSV connector. This constructor creates the connector from a config file
      * @param backend connector backend
      * @return new CSV Connector
      */
     public static CsvConnector createConnector(ConnectorBackend backend) {
         return new CsvConnector(backend);    
     }
 
     /**
      * Saves a template of the config file
      * @param configFileName the new config file name
      * @param dataFileName the data file
      * @param defaultLdmType default LDM type
      * @param folder default folder
      * @throws IOException in case of an IO issue
      */
     public static void saveConfigTemplate(String configFileName, String dataFileName, String defaultLdmType, String folder) throws IOException {
     	SourceSchema s = guessSourceSchema(configFileName, dataFileName, defaultLdmType, folder);
         s.writeConfig(new File(configFileName));
     }
     
     /**
      * Generates a source schema from the headers of a CSV file with a help of a partial config file
      * @param configFileName config file name
      * @param dataFileName CSV data file name
      * @param defaultLdmType default LDM type
      * @param folder folder
      * @return new SourceSchema
      * @throws IOException in case of IO issues
      */
     static SourceSchema guessSourceSchema (String configFileName, String dataFileName, String defaultLdmType, String folder) throws IOException {
     	File configFile = new File(configFileName);
     	InputStream configStream = configFile.exists() ? new FileInputStream(configFile) : null;
     	return guessSourceSchema(configStream, new File(dataFileName).toURI().toURL(), defaultLdmType, folder);
     }
     
     static SourceSchema guessSourceSchema (InputStream configStream, URL dataUrl, String defaultLdmType, String folder) throws IOException {
         String name = URLDecoder.decode(FileUtil.getFileName(dataUrl).split("\\.")[0], "utf-8").trim();
         String[] headers = FileUtil.getCsvHeader(dataUrl);
         int i = 0;
         final SourceSchema s;
         if (configStream != null) {
         	s = SourceSchema.createSchema(configStream);
         } else {
         	s = SourceSchema.createSchema(name);
         }
         final int knownColumns = s.getColumns().size();
         NameTransformer idGen = new NameTransformer(new NameTransformer.NameTransformerCallback() {
         	public String transform(String str) {
         		String idorig = StringUtil.csvHeaderToIdentifier(str);
         		int idmax = Constants.MAX_TABLE_NAME_LENGTH - s.getName().length() - 3; // good enough for 999 long names
         		if (idorig.length() <= idmax)
         			return idorig;
         		return idorig.substring(0, idmax);
         		
         	}
         });
         NameTransformer titleGen = new NameTransformer(new NameTransformer.NameTransformerCallback() {
         	public String transform(String str) {
         		return StringUtil.csvHeaderToTitle(str);
         	}
         });
         if (knownColumns < headers.length) {
         	SourceColumn[] guessed = DataTypeGuess.guessCsvSchema(dataUrl, true);
         	if (guessed.length != headers.length) {
         		throw new AssertionError("The size of data file header is different than the number of guessed fields");
         	}
 	        for(int j = knownColumns; j < headers.length; j++) {
 	        	final String header = headers[j];
 	            final SourceColumn sc;
 	            final String identifier = idGen.transform(header);
 	            final String title = titleGen.transform(header);
 	            if (defaultLdmType != null) {
 	            	sc = new SourceColumn(identifier, defaultLdmType, title, folder);
 	            } else {
 		            sc = guessed[j];
 		            sc.setName(identifier);
 		            sc.setTitle(title);
 		            sc.setFolder(folder);
 	            }
 	            s.addColumn(sc);
 	            i++;
 	        }
         } 
         return s;
     }
 
     /**
      * Data CSV file getter
      * @return the data CSV file
      */
     public File getDataFile() {
         return dataFile;
     }
 
     /**
      * Data CSV file setter
      * @param dataFile the data CSV file
      */
     public void setDataFile(File dataFile) {
         this.dataFile = dataFile;
     }
 
     /**
      * Extracts the source data CSV to the internal database where it is going to be transformed
      * @throws IOException in case of IO issues
      */
     public void extract() throws IOException {
         File src = getDataFile();
         getConnectorBackend().extract(src, getHasHeader());
     }
 
     /**
      * hasHeader getter
      * @return hasHeader flag
      */
     public boolean getHasHeader() {
         return hasHeader;
     }
 
     /**
      * hasHeader setter
      * @param hasHeader flag value
      */
     public void setHasHeader(boolean hasHeader) {
         this.hasHeader = hasHeader;
     }
 
     /**
      * {@inheritDoc}
      */
     public boolean processCommand(Command c, CliParams cli, ProcessingContext ctx) throws ProcessingException {
         try {
             if(c.match("GenerateCsvConfig")) {
                 generateCsvConfig(c, cli, ctx);
             }
             else if(c.match("LoadCsv")) {
                 loadCsv(c, cli, ctx);
             }           
             else
                 return super.processCommand(c, cli, ctx);
         }
         catch (IOException e) {
             throw new ProcessingException(e);
         }
         return true;
     }
 
     /**
      * Loads new CSV file command processor
      * @param c command
      * @param p command line arguments
      * @param ctx current processing context
      * @throws IOException in case of IO issues
      */
     private void loadCsv(Command c, CliParams p, ProcessingContext ctx) throws IOException {
         String configFile = c.getParamMandatory("configFile");
         String csvDataFile = c.getParamMandatory("csvDataFile");
         String hdr = c.getParamMandatory("header");
         File conf = FileUtil.getFile(configFile);
         File csvf = FileUtil.getFile(csvDataFile);
         boolean hasHeader = false;
         if(hdr.equalsIgnoreCase("true"))
             hasHeader = true;
         initSchema(conf.getAbsolutePath());
         setDataFile(new File(csvf.getAbsolutePath()));
         setHasHeader(hasHeader);
         // sets the current connector
         ctx.setConnector(this);
         setProjectId(ctx);
         l.info("CSV Connector successfully loaded.");
     }
 
     /**
      * Generate new config file from CSV command processor
      * @param c command
      * @param p command line arguments
      * @param ctx current processing context
      * @throws IOException in case of IO issues
      */
     private void generateCsvConfig(Command c, CliParams p, ProcessingContext ctx) throws IOException {
         String configFile = c.getParamMandatory("configFile");
         String csvHeaderFile = c.getParamMandatory("csvHeaderFile");
         String defaultLdmType = c.getParam( "defaultLdmType");
    	String folder = c.getParam( "defaultFolder");
         
         CsvConnector.saveConfigTemplate(configFile, csvHeaderFile, defaultLdmType, folder);
         l.info("CSV Connector configuration successfully generated. See config file: "+configFile);
     }
}
