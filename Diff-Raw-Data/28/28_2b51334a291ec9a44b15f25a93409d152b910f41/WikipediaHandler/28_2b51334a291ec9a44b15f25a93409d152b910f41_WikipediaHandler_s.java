 package com.thomasdimson.wikipedia.lda.java;
 
 import com.google.common.collect.Queues;
 import com.thomasdimson.wikipedia.Data;
 import com.thomasdimson.wikipedia.Data.DumpPage;
 import com.google.common.base.Function;
 import org.apache.commons.compress.compressors.CompressorException;
 import org.apache.commons.compress.compressors.CompressorStreamFactory;
 import org.apache.logging.log4j.LogManager;
 import org.apache.logging.log4j.Logger;
 import org.xml.sax.Attributes;
 import org.xml.sax.SAXException;
 import org.xml.sax.helpers.DefaultHandler;
 
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.parsers.SAXParserFactory;
 import java.io.*;
 import java.util.Deque;
 import java.util.Iterator;
 import java.util.concurrent.atomic.AtomicInteger;
 import java.util.regex.Pattern;
 
 
 public class WikipediaHandler extends DefaultHandler {
     public static final Logger logger  = LogManager.getLogger(WikipediaHandler.class);
 
     public static final Pattern SPECIAL_PATTERN = Pattern.compile(
        "^(File|Talk|Special|Wikipedia|Wiktionary|User|User Talk|Category|Portal|Template|MediaWiki|Help):"
     );
 
     public static boolean isSpecialTitle(String title) {
         return SPECIAL_PATTERN.matcher(title).matches();
     }
 
     public static void writeStructuredDump(String xmlFileName, String outputFileName) throws IOException, SAXException,
                                                                                              ParserConfigurationException, CompressorException {
         final InputStream inputStream;
 
         if(xmlFileName.endsWith(".bz2")) {
             inputStream = new CompressorStreamFactory().createCompressorInputStream(
                     CompressorStreamFactory.BZIP2,
                     new BufferedInputStream(new FileInputStream(xmlFileName)));
             System.out.println("Creating bz2 input stream");
         } else {
             inputStream = new BufferedInputStream(new FileInputStream(xmlFileName));
             System.out.println("Creating input stream");
         }
 
         final AtomicInteger numSeen = new AtomicInteger(0);
         final OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outputFileName));
         try {
             WikipediaHandler handler = new WikipediaHandler(new Function<DumpPage, Void>() {
                 @Override
                 public Void apply(DumpPage dumpPage) {
                     if(isSpecialTitle(dumpPage.getTitle())) {
                         return null;
                     }
 
                     try {
                         dumpPage.writeDelimitedTo(outputStream);
                     } catch (IOException e) {
                         throw new RuntimeException(e);
                     }
 
                     if(numSeen.incrementAndGet() % 100000 == 0) {
                         logger.info("Reached page " + numSeen.get());
                     }
                     return null;
                 }
             });
 
             handler.parse(inputStream);
         } finally {
             outputStream.close();
             inputStream.close();
         }
     }
 
     public static Iterator<DumpPage> newStructuredDumpIterator(String filename) throws FileNotFoundException {
         final InputStream inputStream = new BufferedInputStream(new FileInputStream(filename));
 
         try {
             return new Iterator<DumpPage>() {
                 DumpPage nextMessage = DumpPage.parseDelimitedFrom(inputStream);
 
                 @Override
                 public boolean hasNext() {
                     return nextMessage != null;
                 }
 
                 @Override
                 public DumpPage next() {
                     DumpPage ret = nextMessage;
                     try {
                         nextMessage = DumpPage.parseDelimitedFrom(inputStream);
                     } catch (IOException e) {
                         throw new RuntimeException(e);
                     }
                     return ret;
                 }
 
                 @Override
                 public void remove() {
                     throw new UnsupportedOperationException();
                 }
             };
         } catch (IOException e) {
             throw new RuntimeException(e);
         }
     }
 
     private final Function<DumpPage, Void> articleCallback;
 
     private StringBuilder pageTitleBuilder;
     private StringBuilder revisionIdBuilder;
     private StringBuilder revisionTextBuilder;
     private StringBuilder pageNamespaceBuilder;
     private DumpPage.Builder pageBuilder;
 
     private Deque<String> nameStack = Queues.newArrayDeque();
 
     public WikipediaHandler(Function<DumpPage, Void> articleCallback) {
         this.articleCallback = articleCallback;
     }
 
     public void parse(InputStream inputStream) throws ParserConfigurationException, SAXException, IOException {
         SAXParserFactory.newInstance().newSAXParser().parse(inputStream, this);
     }
 
     public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
         if(nameStack.size() > 0) {
             if(qName.equalsIgnoreCase("page")) {
                 pageBuilder = DumpPage.newBuilder();
             } else if(nameStack.peek().equalsIgnoreCase("page")) {
                 if(qName.equalsIgnoreCase("redirect")) {
                     pageBuilder.setRedirect(attributes.getValue("title"));
                 } else if(qName.equalsIgnoreCase("title")) {
                     pageTitleBuilder = new StringBuilder();
                 } else if(qName.equalsIgnoreCase("ns")) {
                     pageNamespaceBuilder = new StringBuilder();
                 }
             } else if (nameStack.peek().equalsIgnoreCase("revision")) {
                 if(qName.equalsIgnoreCase("id")) {
                     revisionIdBuilder = new StringBuilder();
                 } else if(qName.equalsIgnoreCase("text")) {
                     revisionTextBuilder = new StringBuilder();
                 }
             }
         }
         nameStack.push(qName);
     }
 
     @Override
     public void characters(char[] ch, int start, int length) throws SAXException {
         if(pageTitleBuilder != null) {
             pageTitleBuilder.append(ch, start, length);
         } else if(revisionTextBuilder != null) {
             revisionTextBuilder.append(ch, start, length);
         } else if(revisionIdBuilder != null)  {
             revisionIdBuilder.append(ch, start, length);
         } else if(pageNamespaceBuilder != null)  {
             pageNamespaceBuilder.append(ch, start, length);
         }
     }
 
     @Override
     public void endElement(String uri, String localName, String qName) throws SAXException {
         nameStack.pop();
         if(nameStack.size() > 0) {
             if(qName.equalsIgnoreCase("page")) {
                 articleCallback.apply(pageBuilder.build());
                 pageBuilder = null;
             } else if (nameStack.peek().equalsIgnoreCase("page")) {
                 if(qName.equalsIgnoreCase("title")) {
                     pageBuilder.setTitle(pageTitleBuilder.toString());
                     pageTitleBuilder = null;
                 } else if(qName.equalsIgnoreCase("ns")) {
                     pageBuilder.setNamespace(Integer.valueOf(pageNamespaceBuilder.toString()));
                     pageNamespaceBuilder = null;
                 }
             } else if(nameStack.peek().equalsIgnoreCase("revision")) {
                 if (qName.equalsIgnoreCase("id")) {
                     pageBuilder.setId(Long.valueOf(revisionIdBuilder.toString()));
                     revisionIdBuilder = null;
                 } else if(qName.equalsIgnoreCase("text")) {
                     pageBuilder.setText(revisionTextBuilder.toString());
                     revisionTextBuilder = null;
                 }
             }
         }
     }
 }
