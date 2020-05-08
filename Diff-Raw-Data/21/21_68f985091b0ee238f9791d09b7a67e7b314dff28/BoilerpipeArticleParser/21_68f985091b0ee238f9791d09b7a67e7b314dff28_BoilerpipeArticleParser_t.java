 package com.cee.news.parser.impl;
 
 import java.io.IOException;
 import java.io.Reader;
 import java.net.URL;
 import java.util.List;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 import org.xml.sax.XMLReader;
 
 import com.cee.news.model.Article;
 import com.cee.news.parser.ArticleParser;
 import com.cee.news.parser.ParserException;
 import com.cee.news.parser.net.WebClient;
 import com.cee.news.parser.net.WebResponse;
 
 import de.l3s.boilerpipe.BoilerpipeInput;
 import de.l3s.boilerpipe.BoilerpipeProcessingException;
 import de.l3s.boilerpipe.document.TextBlock;
 import de.l3s.boilerpipe.document.TextDocument;
import de.l3s.boilerpipe.extractors.ArticleExtractor;
 import de.l3s.boilerpipe.sax.BoilerpipeHTMLContentHandler;
 
 /**
  * Implementation of {@link ArticleParser} using the {@link BoilerpipeInput} of the boilerpipe library.
  */
 public class BoilerpipeArticleParser implements ArticleParser {
 	
 	private static final Logger LOG = LoggerFactory.getLogger(BoilerpipeArticleParser.class);
     
     private XMLReader xmlReader;
     
     private WebClient webClient;
 
     public BoilerpipeArticleParser() {}
     
     public BoilerpipeArticleParser(XMLReader xmlReader, WebClient webClient) {
         this.xmlReader = xmlReader;
     	this.webClient = webClient;
     }
     
     /**
      * @param webClient Client used to execute web requests
      */
     public void setWebClient(WebClient webClient) {
         this.webClient = webClient;
     }
     
     /**
      * @param reader Reader used to read HTML content from input stream
      */
     public void setReader(XMLReader reader) {
 		this.xmlReader = reader;
 	}
 
 	/* (non-Javadoc)
      * @see com.cee.newsdiff.parser.ArticleParser#parse(com.cee.newsdiff.model.Article)
      */
     public Article parse(Article article) throws ParserException, IOException {
     	Reader textReader = null;
         try {
         	LOG.info("start parsing article content of {}", article.getTitle());
         	BoilerpipeHTMLContentHandler boilerpipeHandler = new BoilerpipeHTMLContentHandler();
         	
         	//TODO register tag action for none text content like media and so on
         	WebResponse response = webClient.openWebResponse(new URL(article.getLocation()));
         	textReader = response.getReader();
         	xmlReader.setContentHandler(boilerpipeHandler);
         	InputSource is = new InputSource(textReader);
         	xmlReader.parse(is);
         	TextDocument textDoc = boilerpipeHandler.toTextDocument();
         	LOG.debug("extracting main content from {}", article.getTitle());
            //ArticleExtractor.INSTANCE.process(textDoc);
        	ArticleExtractor.INSTANCE.process(textDoc);
             List<com.cee.news.model.TextBlock> content = article.getContent();
             for (TextBlock block : textDoc.getTextBlocks()) {
                 if (block.isContent()) {
                     content.add(new com.cee.news.model.TextBlock(block.getText(), block.getNumWords()));
                     
                 }
             }
             if (content.size() < 1) {
             	LOG.warn("no main content found for {}", article.getTitle());
             }
             LOG.info("finished parsing article content of {}, found {} textblocks", article.getTitle(), content.size());
             return article;
         } catch (BoilerpipeProcessingException e) {
             throw new ParserException(e);
         } catch (SAXException e) {
             throw new ParserException(e);
         } finally {
         	if (textReader != null) {
         		textReader.close();
         	}
         }
     }
 
 }
