 package com.cargosmart.b2b.edi.input;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.Reader;
 
 import com.cargosmart.b2b.edi.common.Document;
 
 public abstract class EdiBuilder {
 
 	public EdiBuilder() {
 		super();
 	}
 
 	public abstract Document buildDocument(String content);
 
 	/**
 	 * It will build a EDI document from file.
 	 * 
 	 * @param file File to read from
 	 * @return Document 
 	 * @throws IOException when an I/O error prevents a document from being fully parsed
 	 */
 	public Document buildDocument(File file) throws IOException {
 		return buildDocument(new FileReader(file));
 	}
 	
     /**
      * It will build a EDI document from reader.
      * 
      * @param reader document to read from
      * @return Document 
      * @throws IOException when an I/O error prevents a document from being fully parsed
      */
 	public Document buildDocument(Reader reader) throws IOException {
         BufferedReader buffReader = new BufferedReader(reader);
         char[] buffer = new char[1024];
         int nRead;
         StringBuilder content = new StringBuilder();
         while ((nRead = buffReader.read(buffer, 0, 1024)) != -1) {
             content.append(buffer, 0, nRead);
         }
        buffReader.close();
         return buildDocument(content.toString());
 	}
 
 }
