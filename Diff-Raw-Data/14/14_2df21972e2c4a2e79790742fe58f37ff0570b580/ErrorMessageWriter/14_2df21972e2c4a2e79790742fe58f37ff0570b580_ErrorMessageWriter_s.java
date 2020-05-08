 package org.isatools.errorreporter.html;
 
 import org.isatools.errorreporter.model.ErrorLevel;
 import org.isatools.errorreporter.model.ErrorMessage;
 import org.isatools.errorreporter.model.ISAFileErrorReport;
 
 import java.util.Scanner;
 
 public class ErrorMessageWriter {
 
     private static final String BASE_HTML_FILE = "/html-resources/error_full_template.html";
     private static final String TABLE_HTML_FILE = "/html-resources/error_list_template.html";
 
     private String injectedTableHTML;
     private String baseHTML;
 
 
    public String createHTMLRepresentationOfErrors(ISAFileErrorReport errors) {
 
         // only load the files if they are not already in memory
         if (baseHTML == null || injectedTableHTML == null) {
             loadFiles();
         }
 
         StringBuilder tables = new StringBuilder();
        for (ErrorMessage error : errors.getMessages()) {
             String tmpTable = injectedTableHTML;
 
             tmpTable = tmpTable.replaceAll("(TYPE)", error.getErrorLevel().toString());
             tmpTable = tmpTable.replaceAll("(ERROR_CLASS)", error.getErrorLevel() == ErrorLevel.ERROR ? "error-tag" : "warning-tag");
             tmpTable = tmpTable.replaceAll("MESSAGE", error.getMessage());
 
             tables.append(tmpTable);
         }
 
         return baseHTML.replace("<INJECTED_CODE/>", tables.toString());
     }
 
     private void loadFiles() {
         baseHTML = loadAndAssignFileContents(BASE_HTML_FILE);
         injectedTableHTML = loadAndAssignFileContents(TABLE_HTML_FILE);
     }
 
     private String loadAndAssignFileContents(String fileLocation) {
         Scanner fileScanner = new Scanner(getClass().getResourceAsStream(fileLocation));
 
         StringBuilder sb = new StringBuilder();
         while (fileScanner.hasNextLine()) {
             sb.append(fileScanner.nextLine());
         }
 
         return sb.toString();
     }
 }
