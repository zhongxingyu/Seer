 package net.fortytwo.sesametools.rdfjson;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.io.StringWriter;
 import java.io.Writer;
 import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
 import java.util.Collection;
import java.util.List;
 
 import org.openrdf.model.Statement;
 import org.openrdf.model.ValueFactory;
 import org.openrdf.rio.ParseErrorListener;
 import org.openrdf.rio.ParseLocationListener;
 import org.openrdf.rio.ParserConfig;
import org.openrdf.rio.ParserSetting;
 import org.openrdf.rio.RDFFormat;
 import org.openrdf.rio.RDFHandler;
 import org.openrdf.rio.RDFHandlerException;
 import org.openrdf.rio.RDFParseException;
 import org.openrdf.rio.RDFParser;
import org.openrdf.rio.helpers.BasicParserSettings;
import org.openrdf.rio.helpers.RDFParserBase;
 
 import se.kmr.scam.rest.util.RDFJSON;
 
 import com.github.ansell.sesamerioextensions.api.RDFFormatExtensions;
 
 /**
  * RDFParser implementation for the proposed RDF/JSON format (see
  * http://n2.talis.com/wiki/RDF_JSON_Specification)
  * 
  * @author Joshua Shinavier (http://fortytwo.net). Builds on code by Hannes Ebner
  */
 @SuppressWarnings("unused")
 public class RDFJSONParser implements RDFParser
 {
     
     private ValueFactory valueFactory;
     private RDFHandler rdfHandler;
     private ParseErrorListener parseErrorListener;
     private ParseLocationListener parseLocationListener;
     private ParserConfig config = new ParserConfig();
     
     @Override
     public ParserConfig getParserConfig()
     {
         return this.config;
     }
     
     @Override
     public RDFFormat getRDFFormat()
     {
         return RDFFormatExtensions.RDFJSONPREFERRED;
     }
     
     @Override
     public void parse(final InputStream in, final String baseURI) throws IOException, RDFParseException,
         RDFHandlerException
     {
         final Reader reader = new BufferedReader(new InputStreamReader(in, Charset.forName("UTF-8")));
         this.parse(reader, baseURI);
     }
     
     @Override
     public void parse(final Reader reader, final String baseURI) throws IOException, RDFParseException,
         RDFHandlerException
     {
         if(null == this.rdfHandler)
         {
             throw new IllegalStateException("RDF handler has not been set");
         }
         
         final String s = this.toString(reader);
         final Collection<Statement> g = RDFJSON.rdfJsonToGraph(s);
         
         if(g == null)
         {
             throw new RDFParseException("Could not parse JSON RDF Graph");
         }
         
         this.rdfHandler.startRDF();
         for(final Statement statement : g)
         {
             this.rdfHandler.handleStatement(statement);
         }
         this.rdfHandler.endRDF();
     }
     
     @Override
     public void setDatatypeHandling(final DatatypeHandling datatypeHandling)
     {
         this.config =
                 new ParserConfig(this.config.verifyData(), this.config.stopAtFirstError(),
                         this.config.isPreserveBNodeIDs(), datatypeHandling);
     }
     
     @Override
     public void setParseErrorListener(final ParseErrorListener listener)
     {
         this.parseErrorListener = listener;
     }
     
     @Override
     public void setParseLocationListener(final ParseLocationListener listener)
     {
         this.parseLocationListener = listener;
     }
     
     @Override
     public void setParserConfig(final ParserConfig config)
     {
         this.config = config;
     }
     
     @Override
     public void setPreserveBNodeIDs(final boolean preserveBNodeIDs)
     {
         this.config =
                 new ParserConfig(this.config.verifyData(), this.config.stopAtFirstError(), preserveBNodeIDs,
                         this.config.datatypeHandling());
     }
     
     @Override
     public void setRDFHandler(final RDFHandler handler)
     {
         this.rdfHandler = handler;
     }
     
     @Override
     public void setStopAtFirstError(final boolean stopAtFirstError)
     {
         this.config =
                 new ParserConfig(this.config.verifyData(), stopAtFirstError, this.config.isPreserveBNodeIDs(),
                         this.config.datatypeHandling());
     }
     
     @Override
     public void setValueFactory(final ValueFactory valueFactory)
     {
         this.valueFactory = valueFactory;
     }
     
     @Override
     public void setVerifyData(final boolean verifyData)
     {
         this.config =
                 new ParserConfig(verifyData, this.config.stopAtFirstError(), this.config.isPreserveBNodeIDs(),
                         this.config.datatypeHandling());
     }
     
     private String toString(final Reader reader) throws IOException
     {
         final Writer writer = new StringWriter();
         
         final char[] buffer = new char[1024];
         int n;
         while((n = reader.read(buffer)) != -1)
         {
             writer.write(buffer, 0, n);
         }
         return writer.toString();
     }
    
    @Override
    public Collection<ParserSetting<?>> getSupportedSettings()
    {
        Collection<ParserSetting<?>> result = new ArrayList<ParserSetting<?>>(4);

        result.add(BasicParserSettings.DATATYPE_HANDLING);
        result.add(BasicParserSettings.PRESERVE_BNODE_IDS);
        result.add(BasicParserSettings.STOP_AT_FIRST_ERROR);
        result.add(BasicParserSettings.VERIFY_DATA);

        return result;
    }
 }
