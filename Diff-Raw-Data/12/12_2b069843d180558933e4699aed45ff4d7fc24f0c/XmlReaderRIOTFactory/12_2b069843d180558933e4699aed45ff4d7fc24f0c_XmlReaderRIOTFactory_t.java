 /*
  * Copyright (c) 2011 Miguel Ceriani
  * miguel.ceriani@gmail.com
 
  * This file is part of Semantic Web Open datatafloW System (SWOWS).
 
  * SWOWS is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as
  * published by the Free Software Foundation, either version 3 of
  * the License, or (at your option) any later version.
 
  * SWOWS is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
 
  * You should have received a copy of the GNU Affero General
  * Public License along with SWOWS.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.swows.reader;
 
 import java.io.InputStream;
 
 import org.apache.jena.atlas.web.ContentType;
 import org.apache.jena.riot.Lang;
 import org.apache.jena.riot.LangBuilder;
 import org.apache.jena.riot.RDFLanguages;
 import org.apache.jena.riot.RDFParserRegistry;
 import org.apache.jena.riot.ReaderRIOT;
 import org.apache.jena.riot.ReaderRIOTFactory;
 import org.apache.jena.riot.system.StreamRDF;
 import org.swows.xmlinrdf.DomEncoder2;
 import org.xml.sax.InputSource;
 
 import com.hp.hpl.jena.sparql.util.Context;
 
 public class XmlReaderRIOTFactory implements ReaderRIOTFactory {
 	
	public static final String XML_SYNTAX_URI = "http://www.swows.org/syntaxes/XML";
 
 	protected static void initialize() {
 //		RDFReaderFImpl.setBaseReaderClassName(XML_SYNTAX_URI, XmlReaderRIOTFactory.class.getCanonicalName());
 
         Lang lang =
         		LangBuilder
         		.create("XML", "text/xml")
         		.addAltContentTypes("application/xml","text/xml","image/svg+xml")
         		.addFileExtensions("xml","svg").build() ;
         // This just registers the name, not the parser.
         RDFLanguages.register(lang) ;
         
         // Register the parser factory.
         ReaderRIOTFactory factory = new XmlReaderRIOTFactory() ;
         RDFParserRegistry.registerLangTriples(lang, factory) ;
         
 //        // Optional extra:
 //        // If needed to set or override the syntax, register the name explicitly ...
 //        System.out.println("## --") ;
 //        IO_Jena.registerForModelRead("SSE", RDFReaderSSE.class) ;
 //        // and use read( , "SSE")
 //        Model model2 = ModelFactory.createDefaultModel().read(filename, "SSE") ;
 //        model2.write(System.out, "TTL") ;
 	}
 
 	@Override
 	public ReaderRIOT create(Lang language) {
 		return new ReaderRIOT() {
 			
 			@Override
 			public void read(
 					InputStream in,
 					String baseURI,
 					ContentType ct,
 					StreamRDF output,
 					Context context) {
 				InputSource xmlInputSource = new InputSource(in);
 				xmlInputSource.setSystemId(baseURI);
 				output.start();
 				output.base(baseURI);
 				DomEncoder2.encode(xmlInputSource, baseURI, output);
 				output.finish();
 			}
 		};
 	}
 
 }
