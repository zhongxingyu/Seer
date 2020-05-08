 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package com.flaptor.util.parser;
 
 import com.flaptor.util.Execute;
 import java.io.ByteArrayInputStream;
 import org.pdfbox.encryption.DocumentEncryption;
 import org.pdfbox.pdfparser.PDFParser;
 import org.pdfbox.pdmodel.PDDocument;
 import org.pdfbox.pdmodel.PDDocumentInformation;
 import org.pdfbox.util.PDFTextStripper;
 
 
 /**
  *
  * @author jorge
  */
 public class PdfParser implements IParser {
 
 
    public ParseOutput parse(String url, byte[] content) throws Exception {
         ParseOutput output = null;
         PDDocument pdf = null;
         try {
             PDFParser parser = new PDFParser(new ByteArrayInputStream(content));
             parser.parse();
             pdf = parser.getPDDocument();
             PDFTextStripper stripper = new PDFTextStripper();
             String text = stripper.getText(pdf);
             PDDocumentInformation info = pdf.getDocumentInformation();
             String title = info.getTitle();
             output = new ParseOutput(url);
             output.addFieldString(ParseOutput.CONTENT, text);
             output.setTitle(title);
 //System.out.println("ENCODING: "+encoding);
 //System.out.println("TITLE: "+title);
 //System.out.println("TEXT: "+text);
 //System.out.println("LEN: "+text.length());
         } finally {
             Execute.close(pdf);
             Execute.close(output);
         }
         return output;
     }
 
 }
