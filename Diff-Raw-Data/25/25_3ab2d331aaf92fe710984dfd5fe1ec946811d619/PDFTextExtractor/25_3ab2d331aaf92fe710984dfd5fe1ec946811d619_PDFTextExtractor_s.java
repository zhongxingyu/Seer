 package org.seasr.meandre.components.tools.text.io;
 
 import java.net.URI;
 import java.util.logging.Logger;
 
 import org.meandre.annotations.Component;
 import org.meandre.annotations.ComponentInput;
 import org.meandre.annotations.ComponentOutput;
 import org.meandre.components.abstracts.AbstractExecutableComponent;
 import org.meandre.core.ComponentContext;
 import org.meandre.core.ComponentContextProperties;
 import org.seasr.datatypes.BasicDataTypesTools;
 import org.seasr.meandre.components.tools.Names;
 import org.seasr.meandre.support.parsers.DataTypeParser;
 import org.seasr.meandre.support.text.PDFUtils;
 
 /** This class provides methods related to text extraction from PDF files
  *
  * @author Boris Capitanu
  * @author Xavier Llor&agrave;
  * @author Loretta Auvil
  *
  */
 
 @Component(creator = "Loretta Auvil",
            description = "This component extracts the text from a pdf document. "+
                          "The input is a String or URL specifiying the url of the pdf document. "+
                          "The output is the extracted text.",
            name = "PDF Text Extractor",
            tags = "URL, text, pdf",
            baseURL = "meandre://seasr.org/components/")
 
 public class PDFTextExtractor extends AbstractExecutableComponent {
 
     @ComponentInput(description = "The URL of the PDF file." +
                                   "<br>TYPE: String, URL",
                     name = Names.PORT_LOCATION)
    public final static String IN_PDF_URL = Names.PORT_LOCATION;
 
     @ComponentOutput(description = "The text extracted from the PDF file." +
                                    "<br>TYPE: Text",
                      name = Names.PORT_TEXT)
    public final static String OUT_TEXT = Names.PORT_TEXT;
 
 
     private Logger _console;
 
 
     public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
         _console = getConsoleLogger();
     }
 
     public void executeCallBack(ComponentContext cc) throws Exception {
         Object data = cc.getDataComponentFromInput(IN_PDF_URL);
         _console.fine("Got input of type: " + data.getClass().toString());
 
         URI uri = DataTypeParser.parseAsURI(data);
         _console.fine("Processing PDF document: " + uri);
 
         String text = PDFUtils.extractText(uri.toURL());
 
         cc.pushDataComponentToOutput(OUT_TEXT, BasicDataTypesTools.stringToStrings(text));
     }
 
     public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
     }
 
 }
