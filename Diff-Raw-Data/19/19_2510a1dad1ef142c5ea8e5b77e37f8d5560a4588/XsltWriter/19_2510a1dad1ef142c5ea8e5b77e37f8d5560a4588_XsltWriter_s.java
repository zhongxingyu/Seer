 package de.unigoettingen.sub.convert.output;
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.IOException;
 import java.io.OutputStream;
 
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Marshaller;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.OutputKeys;
 import javax.xml.transform.Source;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerConfigurationException;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.TransformerFactoryConfigurationError;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 import javax.xml.transform.stream.StreamSource;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import de.unigoettingen.sub.convert.api.WriterWithOptions;
 import de.unigoettingen.sub.convert.model.Char;
 import de.unigoettingen.sub.convert.model.Document;
 import de.unigoettingen.sub.convert.model.Line;
 import de.unigoettingen.sub.convert.model.Metadata;
 import de.unigoettingen.sub.convert.model.Page;
 import de.unigoettingen.sub.convert.model.Paragraph;
 import de.unigoettingen.sub.convert.model.TextBlock;
 import de.unigoettingen.sub.convert.model.Word;
 
 public class XsltWriter extends WriterWithOptions {
 	private final static Logger LOGGER = LoggerFactory.getLogger(XsltWriter.class);
 	private static final String XSLT_DESCRIPTION = "[path] to XSLT script file";
 
 	private boolean firstPage = true;
 	private String beforeMeta = "";
 	private String betweenMetaAndPages = "";
 	private String afterPages = "";
 	
 	public XsltWriter() {
 		supportedOptions.put("xslt", XSLT_DESCRIPTION);
 	}
 	
 	@Override
 	public void writeStart() {
 		checkOutputStream();
 		checkXsltSheet();
 		
 		findOutOutputDocStructure();
 		writeBeginningOfDoc();
 	}
 
 	private void checkXsltSheet() {
 		if (setOptions.get("xslt") == null) {
 			throw new IllegalArgumentException("Path to XSLT file is not set");
 		}
 	}
 
 	private void findOutOutputDocStructure() {
 		Document sampleDoc = new Document();
 		Metadata sampleMeta = new Metadata();
 		sampleMeta.setOcrSoftwareName("sampleSoftwareName");
 		sampleDoc.setMetadata(sampleMeta);
 		Page samplePage = newPageWithText();
 		sampleDoc.getPage().add(samplePage);
 		
 		String completeDocWithMetaAndPage = transformToString(sampleDoc);
 
 		String metaPartOnly = transformToString(sampleMeta);
 		metaPartOnly = makeToRegex(metaPartOnly);
 		String pagePartOnly = transformToString(samplePage);
 		pagePartOnly = makeToRegex(pagePartOnly);
 
 		String patternForSplit = null;
 		if (metaPartOnly.trim().isEmpty()) {
 			patternForSplit = pagePartOnly;
 		} else {
 			patternForSplit = metaPartOnly + "|" + pagePartOnly;
 		}
 
 		String[] docParts = completeDocWithMetaAndPage.split(patternForSplit);
 		if (docParts.length == 2) {
 			beforeMeta = docParts[0];
 			afterPages = docParts[1];
 		} else if (docParts.length == 3) {
 			beforeMeta = docParts[0];
 			betweenMetaAndPages = docParts[1];
 			afterPages = docParts[2];
 		} else {
 			throw new IllegalStateException("Could not determine structure for output document");
 		}
 	}
 
 	private Page newPageWithText() {
 		Page page = new Page();
 		TextBlock block = new TextBlock();
 		Paragraph par = new Paragraph();
 		Line line = new Line();
 		Word w = new Word();
 		Char ch = new Char();
 		ch.setValue("a");
 		w.getCharacters().add(ch);
 		line.getLineItems().add(w);
 		par.getLines().add(line);
 		block.getParagraphs().add(par);
 		page.getPageItems().add(block);
 		return page;
 	}
 	
 	private String transformToString(Object fragment) {
 		ByteArrayOutputStream baos = new ByteArrayOutputStream();
 		transformAndOutput(fragment, baos);
		return baos.toString();
 	}
 		
 	private String makeToRegex(String docFragment) {
 		Character[] regexSymbolsToRemove = {'|', '&', '?', '+', '*', '\\', '['};
 		for (char symbol : regexSymbolsToRemove) {
 			docFragment = docFragment.replace(symbol, '.');
 		}
 		docFragment = docFragment.replaceAll("\\s+", "\\\\s*");
 		docFragment = docFragment.replaceAll("><", ">\\\\s*<");
 
 		return docFragment;
 	}
 
 	private void writeBeginningOfDoc() {
 		try {
			output.write(beforeMeta.getBytes());
 		} catch (IOException e) {
 			LOGGER.error("Could not write to output", e);
 		}
 	}
 
 	@Override
 	public void writeMetadata(Metadata meta) {
 		transformAndOutput(meta, output);
 	}
 
 	private void transformAndOutput(Object fragment, OutputStream target) {
 		try {
 			org.w3c.dom.Document document = newDom();
 			
 			JAXBContext context = JAXBContext.newInstance(fragment.getClass());
 			Marshaller m = context.createMarshaller();
 			m.marshal(fragment, document);
 			
 			Source src = new DOMSource(document);
 			Transformer transformer = newTransformer();
 			transformer.transform(src, new StreamResult(target));
 		} catch (JAXBException e) {
 			throw new IllegalStateException("Could not convert to internal XML format: " + fragment.getClass(), e);
 		} catch (TransformerException e) {
 			throw new IllegalStateException("Could not transform XML fragment to output: " + fragment.getClass(), e);
 		}
 	}
 
 	private org.w3c.dom.Document newDom() {
 		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
 		factory.setNamespaceAware(true);
 		org.w3c.dom.Document document = null;
 		try {
 			DocumentBuilder builder = factory.newDocumentBuilder();
 			document = builder.newDocument();
 		} catch (ParserConfigurationException e) {
 			throw new IllegalStateException("Could not create empty DOM tree", e);
 		}
 		return document;
 	}
 
 	private Transformer newTransformer() {
 		Transformer transformer = null;
 		File xslt = new File(setOptions.get("xslt"));
 		if (!xslt.exists()) {
 			throw new IllegalArgumentException("File does not exist: " + xslt.getAbsolutePath());
 		}
 		try {
 			transformer = TransformerFactory.newInstance().newTransformer(
 					new StreamSource(xslt) );
 		} catch (TransformerConfigurationException e) {
 			throw new IllegalStateException("Could not process xslt file " + xslt.getAbsolutePath(), e);
 		} catch (TransformerFactoryConfigurationError e) {
 			throw new IllegalStateException("Could not process xslt file " + xslt.getAbsolutePath(), e);
 		}
 		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
 		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
 
 		return transformer;
 	}
 	
 	@Override
 	public void writePage(Page page) {
 		if (firstPage) {
 			try {
				output.write(betweenMetaAndPages.getBytes());
 			} catch (IOException e) {
 				LOGGER.error("Could not write to output", e);
 			}
 			firstPage = false;
 		}
 		transformAndOutput(page, output);
 	}
 
 	@Override
 	public void writeEnd() {
 		try {
			output.write(afterPages.getBytes());
 		} catch (IOException e) {
 			LOGGER.error("Could not write to output", e);
 		}
 		
 	}
 
 }
