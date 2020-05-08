 package com.amadeus.ori.translate.exporters;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.Collection;
 
 import javax.xml.stream.XMLOutputFactory;
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.XMLStreamWriter;
 
 import org.apache.commons.lang.NotImplementedException;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import com.amadeus.ori.translate.domain.Translation;
 import com.amadeus.ori.translate.repository.LanguageRepository;
 
 @ExporterName(name="android", description="Android values XML files")
 public class AndroidExporter implements Exporter {
 	
 	private static final Log LOG = LogFactory.getLog(AndroidExporter.class);
 
 	@Override
 	public String getFilename(String bundle, String language) {
 
 		String filename = "values";
 		
 		if (language != null && !language.isEmpty()) {
 			filename += "-" + language;
 		}		
 
 		filename += "/strings.xml";
 		return filename;
 	}
 
 	@Override
 	public void writeBundle(OutputStream outputStream, String bundleName,
 			String language, Collection<Translation> translations)
 			throws IOException {
 		
 		if ((translations != null) && (translations.size() > 0)) {
 
 			XMLOutputFactory factory = XMLOutputFactory.newInstance();
 			try {
 				XMLStreamWriter writer = factory
 						.createXMLStreamWriter(outputStream);
 
 				writer.writeStartDocument();
 				writeResources(writer, translations, bundleName);
 				writer.writeEndDocument();
 
 				writer.flush();
 				writer.close();
 
 			} catch (XMLStreamException e) {
 				LOG.error("Unable to write to XML stream", e);
 				throw new IOException("Unable to write to XML stream", e);
 			}
 		}
 	}
 
 	private void writeResources(XMLStreamWriter writer,
 			Collection<Translation> translations, String bundleName) throws XMLStreamException {
 
 		writer.writeStartElement("resources");
 
 		for (Translation translation : translations) {
 			String bundleId = translation.getBundle();
 			
 			if (bundleName.equals(bundleId)) {
 				writeStringResource(writer, translation);
 			}	
 		}
 
 		writer.writeEndElement();
 	}
 
 	private void writeStringResource(XMLStreamWriter writer,
 			Translation translation) throws XMLStreamException {
 		
 	     writer.writeStartElement("string");
	     writer.writeAttribute("name", translation.getKeywordId());
 	     writer.writeCharacters(translation.getValue());
 	     writer.writeEndElement();
 	}
 
 	@Override
 	public String getIndexFilename(String bundleName) {
 		// No index
 		return null;
 	}
 
 	@Override
 	public void writeIndex(OutputStream outputStream, String bundleName,
 			String[] languages, LanguageRepository languageRepository)
 			throws IOException {
 		
 		// No index
 		throw new NotImplementedException();
 	}
 
 }
