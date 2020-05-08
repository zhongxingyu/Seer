 package org.lawrencebower.docgen.web_model.view.view_factory.tsv_factory.parser;
 
 import org.lawrencebower.docgen.core.exception.DocGenException;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.Reader;
 
 /** A file with rows with tab (\t) separated values. Comment lines
  * starting with a hash (#) and empty lines will be ignored.
  */
 public class TSVReader {
 
     @Autowired(required = false)
     private StreamFactory streamFactory;
 
 	public static final String separator = "\t";
 	public static final String comment = "#";
     public static final String EMPTY_COL = "(null)";
 
     public DataSet readDataSetAsFile(String fileName) {
 
         InputStreamReader streamReader = streamFactory.getStreamFromFile(fileName);
 
         return readTSV(streamReader);
     }
 
     private DataSet readTSV(Reader streamReader) {
 
         BufferedReader reader = new BufferedReader(streamReader);
 
         DataSet result = new DataSet();
 
         while (true) {
 
             String line = readLine(reader);
             if (line == null) {
                 break;
             }
 
             String trimmedLine = line.trim();
 
             if (trimmedLine.isEmpty()) {
                 continue; // ignore empty lines
             }
             if (trimmedLine.startsWith(comment)) {
                 continue; // ignore comment lines
             }
 
             String[] tokens = trimmedLine.split(separator);

            processNewlines(tokens);

             result.addRow(new DataRow(tokens));
         }
 
         return result;
     }
 
     private String readLine(BufferedReader reader) {
         try {
             return reader.readLine();
         } catch (IOException e) {
             throw new DocGenException(e);
         }
     }

    private void processNewlines(String[] tokens) {
        for(int i = 0; i<tokens.length;i++){
            String processedToken = replaceNewlineChars(tokens[i]);
            tokens[i] = processedToken;
        }
    }

    private String replaceNewlineChars(String token){
        return token.replace("\\n","\n");
    }
 }
