 package com.cee.news.parser.impl;
 
 import java.io.IOException;
 import java.net.URL;
 import java.util.List;
 
 import org.xml.sax.InputSource;
import org.xml.sax.Parser;
 import org.xml.sax.SAXException;
 import org.xml.sax.XMLReader;
 
 import com.cee.news.model.Article;
import com.cee.news.parser.net.WebClient;
 import com.cee.news.parser.ArticleParser;
 import com.cee.news.parser.ParserException;
 
 import de.l3s.boilerpipe.BoilerpipeInput;
 import de.l3s.boilerpipe.BoilerpipeProcessingException;
 import de.l3s.boilerpipe.document.TextBlock;
 import de.l3s.boilerpipe.document.TextDocument;
 import de.l3s.boilerpipe.sax.BoilerpipeHTMLContentHandler;
import de.l3s.boilerpipe.sax.BoilerpipeSAXInput;
 
 /**
  * Implementation of {@link ArticleParser} using the {@link BoilerpipeInput} of the boilerpipe library.
  */
 public class BoilerpipeArticleParser implements ArticleParser {
     
     private XMLReader reader;
     
     private WebClient webClient;
 
     public BoilerpipeArticleParser() {}
     
     public BoilerpipeArticleParser(XMLReader reader, WebClient webClient) {
         this.reader = reader;
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
 		this.reader = reader;
 	}
 
 	/* (non-Javadoc)
      * @see com.cee.newsdiff.parser.ArticleParser#parse(com.cee.newsdiff.model.Article)
      */
     public Article parse(Article article) throws ParserException, IOException {
         try {
         	BoilerpipeHTMLContentHandler boilerpipeHandler = new BoilerpipeHTMLContentHandler();
         	
         	//TODO register tag action for none text content like media and so on
         	
         	reader.setContentHandler(boilerpipeHandler);
         	reader.parse(new InputSource(webClient.openStream(new URL(article.getLocation()))));
             TextDocument textDoc = boilerpipeHandler.toTextDocument();
             ArticleExtractor.INSTANCE.process(textDoc);
             List<com.cee.news.model.TextBlock> content = article.getContent();
             for (TextBlock block : textDoc.getTextBlocks()) {
                 if (block.isContent()) {
                     content.add(new com.cee.news.model.TextBlock(block.getText(), block.getNumWords()));
                     
                 }
             }
             if (content.size() < 1) {
                // throw new ParserException("No article content found for " + article.getLocation());
             }
             return article;
         } catch (BoilerpipeProcessingException e) {
             throw new ParserException(e);
         } catch (SAXException e) {
             throw new ParserException(e);
         }
     }
 
 }
