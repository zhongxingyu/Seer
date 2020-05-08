 package clueweb2trectext;
 
 import java.io.BufferedOutputStream;
 import java.io.BufferedReader;
 import java.io.ByteArrayInputStream;
 import java.io.DataInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.io.OutputStreamWriter;
 import java.net.URL;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Scanner;
 import java.util.Vector;
 import java.util.zip.GZIPInputStream;
 import java.util.zip.GZIPOutputStream;
 import java.nio.charset.CharsetDecoder ;
 import java.nio.charset.Charset ;
 
 import net.htmlparser.jericho.Element;
 import net.htmlparser.jericho.HTMLElementName;
 import net.htmlparser.jericho.Renderer;
 import net.htmlparser.jericho.Source;
 
 import org.wikipedia.miner.util.SentenceSplitter;
 
 import edu.cmu.lemurproject.WarcHTMLResponseRecord;
 import edu.cmu.lemurproject.WarcRecord;
 import java.nio.charset.CodingErrorAction;
 
 /**
  *
  * @author Mark D. Smucker
  */
 public class Clueweb2trectext 
 {
     static final org.wikipedia.miner.util.SentenceSplitter sentenceSpliter = new SentenceSplitter();
 
     public static void main(String[] args) throws IOException,
             FileNotFoundException, Exception 
     {
         // Okay, the easiest way to do this is to pass in an input directory and output dir
         if (args.length != 5) 
          {
              System.err.println("usage: java Clueweb2trectext inputDir outputDir fusionSpam.ge70Dir docno.mapping.simple AnchorTextFileDir");
              return;
          }
         
         String inputDirName = args[0];
         String outputDirName = args[1];
         String hamDocnosDirName = args[2];
         String mappingFileName = args[3];
         String anchorTextDirName = args[4];
         
         File inputDir = new File(inputDirName);
         if (!inputDir.exists()) 
         {
             System.err.println("inputDir does not exist: " + inputDirName);
             return;
         }
         File outputDir = new File(outputDirName);
         if (!outputDir.exists()) 
         {
             System.err.println("outputDir does not exist: " + outputDirName);
             return;
         }
 
         File hamDocnosDir = new File(hamDocnosDirName);
         if (!hamDocnosDir.exists()) 
         {
             System.err.println("hamDocnosDir does not exist: " + hamDocnosDirName);
             return;
         }
 
         File anchorTextDir = new File(anchorTextDirName);
         if (!anchorTextDir.exists()) 
         {
             System.err.println("Anchor text directory  does not exist: "
                     + anchorTextDirName );
             return;
         }
         ClueWebDocnoMapping docnoMapper = new ClueWebDocnoMapping(mappingFileName);
 
         // for each warc.gz file in inputDir, convert it and output new file to outputDir
         String[] fileNames = inputDir.list();
         for (int fileNameIdx = 0; fileNameIdx < fileNames.length; ++fileNameIdx) 
         {
             String currFileName = fileNames[fileNameIdx];
 
             if (currFileName.endsWith("warc.gz")) 
             {
                 ConvertWarcToTrec(inputDir, currFileName, outputDir, hamDocnosDir,
                         anchorTextDir, docnoMapper);
             }
         }
     }
 
     public static void ConvertWarcToTrec(File inputDir,
             String gzipWarcFileName, File outputDir, File hamDocnosDir,
             File anchorTextDir, ClueWebDocnoMapping docnoMapper) throws IOException, Exception 
     {       
         int extIdx = gzipWarcFileName.indexOf(".warc.gz");
         int dotIdx = gzipWarcFileName.indexOf(".");
         if (extIdx != dotIdx) 
         {
             throw new Exception("bad file name:" + gzipWarcFileName);
         }
 
         String baseName = gzipWarcFileName.substring(0, extIdx);
         String anchorTextFileName = baseName + ".gz";
 
         Map<String, String> anchorMap = readAnchorTextFile(anchorTextDir.getAbsolutePath()
                 + File.separator + anchorTextFileName);
         String gzipTrectextFileName = baseName + ".trectext.gz";
         PrintWriter trecFile = new PrintWriter(                              
                 new OutputStreamWriter(
                 new BufferedOutputStream(
                 new GZIPOutputStream(new FileOutputStream(outputDir.getAbsolutePath()
                         + File.separator + gzipTrectextFileName))),
                 "UTF-8" ) ) ;
 
         String hamDocnosFileName = hamDocnosDir.getAbsolutePath() + File.separator + baseName + ".gz" ;
         ClueWebDocnoSet hamDocnos = new ClueWebDocnoSet(hamDocnosFileName, docnoMapper);        
         
         String inputWarcFile = inputDir.getAbsolutePath() + File.separator
                 + gzipWarcFileName;
 
         // open our gzip input stream
         GZIPInputStream gzInputStream = new GZIPInputStream(
                 new FileInputStream(inputWarcFile));
 
         /// cast to a data input stream
         DataInputStream inStream = new DataInputStream(gzInputStream);
 
         // iterate through our stream
         WarcRecord thisWarcRecord;
         char [] buf = new char[1024*1024] ; // buffer for reading html
         while ((thisWarcRecord = WarcRecord.readNextWarcRecord(inStream)) != null) 
         {
             // see if it's a response record
             if (thisWarcRecord.getHeaderRecordType().equals("response")) 
             {
                 // it is - create a WarcHTML record
                 WarcHTMLResponseRecord htmlRecord = new WarcHTMLResponseRecord(
                         thisWarcRecord);
                 String docno = htmlRecord.getTargetTrecID();
                 if (!hamDocnos.DocnoExists(docno)) 
                 {
                     continue;
                 }
 
                 byte[] contentBytes = thisWarcRecord.getContent();
                 StringBuilder doc = new StringBuilder(contentBytes.length);
                 doc.append("<DOC>\n<DOCNO>");
                 doc.append(docno);
                 doc.append("</DOCNO>\n");
 
                 ByteArrayInputStream contentStream = new ByteArrayInputStream(
                         contentBytes);
                 // the english portion of clueweb is suppose to be in utf-8
                 // now, what are we supposed to do with the header though?
                 // maybe the header gets all sort of non-UTF8 junk in it?
                 //
                 // only, let's be in control of this decoding business
                 Charset utf8Charset = Charset.forName("UTF-8");
                 CharsetDecoder decoder = utf8Charset.newDecoder() ;
                 // I want to clean up bad junk in the input.  No bad stuff!
                 decoder.onMalformedInput(CodingErrorAction.REPLACE) ;
                 decoder.replaceWith(" ") ; // replace with blanks
                 BufferedReader inReader = new BufferedReader(
                         new InputStreamReader(contentStream, decoder));
 
                 String url = htmlRecord.getTargetURI();
                 doc.append("<url>");
                 doc.append(stripNonValidXMLCharacters(url));
                 doc.append("</url>\n");
 
                 // forward to the first /n/n , i.e. a blank line separates the
                 // html header from the html doc
                 boolean firstLine = true;
                 boolean inHeader = true;
                 String line = null;
                 while (inHeader && ((line = inReader.readLine()) != null)) 
                 {
                     if (!firstLine && line.trim().length() == 0) 
                     {
                         inHeader = false;
                     } 
                     else 
                     {
                         //outHeader.println( line );
                     }
 
                     if (firstLine) 
                     {
                         firstLine = false;
                     }
                 }
 
                 // now we have the rest of the lines
                 // read them all into a string buffer to get a string.
                 StringBuilder html = new StringBuilder(contentBytes.length);
                 int numRead = 0 ;
                 while ( ( numRead = inReader.read(buf) ) != -1 )
                 {
                     html.append(buf, 0, numRead ) ;
                 }
                 inReader.close();
                 
                 Source source = new Source(html); // works because html implements CharSequence
                 source.setLogger(null); // turn off logging
                 source.fullSequentialParse(); // error message recommends calling this after creation
                 String title = getTitle(source) ;
                 Renderer renderer = source.getRenderer();
                 renderer.setIncludeHyperlinkURLs(false);
                 String renderedText = stripNonValidXMLCharacters(renderer.toString()) ;
                 doc.append("<cached>\n") ; // include a copy of the rendered text as a "cached" version
                 doc.append(renderedText) ; // but do not index this text.  we can later
                doc.append("</cached>\n") ; // extract it easily though from the doc
                 doc.append("<TEXT>\n"); // we'll index everything in text
                 
                 // identify and mark up sentences                
                 Vector<String> sentences = sentenceSpliter.getSentences( renderedText,
                     org.wikipedia.miner.util.SentenceSplitter.MULTIPLE_NEWLINES);
                 StringBuilder sbSentenceText = new StringBuilder(
                         (int) (renderedText.length() * 1.20));
                 for (int sIdx = 0; sIdx < sentences.size(); ++sIdx) 
                 {
                     if (sIdx > 0) 
                     {
                         sbSentenceText.append("\n<sentence>");
                     } 
                     else 
                     {
                         sbSentenceText.append("<sentence>");
                     }
                     String sentToAppend = sentences.elementAt(sIdx);
                     sbSentenceText.append(sentToAppend);
                     sbSentenceText.append("</sentence>");
                 }
                 String sentenceText = sbSentenceText.toString();
                 String tokenizedurl = TokenizeUrl( url ) ;
                 String anchorText = anchorMap.get(docno) ; 
                 if ( anchorText == null ) 
                 {
                     anchorText = "" ;
                 }
                 doc.append("<title>");
                 doc.append( stripNonValidXMLCharacters(title) );
                 doc.append("</title>\n") ;
                 doc.append( "<tokenizedurl>");
                 doc.append( stripNonValidXMLCharacters(tokenizedurl) );
                 doc.append( "</tokenizedurl>\n");
                 doc.append("<body>\n");
                 doc.append(sentenceText); // already clean xml 
                 doc.append("\n</body>\n");
                 doc.append("<anchortext>" + stripNonValidXMLCharacters( anchorText ) + "</anchortext>\n");
                 doc.append("</text>\n</DOC>\n");
                 trecFile.print(doc.toString());
             }
         }
         inStream.close();
         trecFile.flush();
         trecFile.close();
     }
 
     private static String getTitle(Source source) 
     {
         Element titleElement = source.getFirstElement(HTMLElementName.TITLE);
 
         if (titleElement == null) 
         {
             return "";
         }
         return titleElement.getContent().toString();
     }
 
     private static String TokenizeUrl(String url) 
     {
         int slashslashIdx = url.indexOf("//") ;
         int startIdx = slashslashIdx + 2 ;
         if ( slashslashIdx == -1 || startIdx >= url.length() )
             startIdx = 0 ;
         int questionIdx = url.indexOf("?") ;
         int endIdx = questionIdx ;
         if ( questionIdx <= startIdx )
             endIdx = url.length() ;
         String hostAndPath = url.substring(startIdx, endIdx) ;
         String [] tokens = hostAndPath.split("[^a-zA-Z0-9]");
         StringBuilder sbResult = new StringBuilder() ;
         for ( int i = 0 ; i < tokens.length ; ++i )
         {
             if ( i > 0 )
                 sbResult.append(" ");
             sbResult.append(tokens[i]) ;
         }
         return sbResult.toString() ;
     }
 
     public static Map<String, String> readAnchorTextFile(String fileName)
             throws IOException 
     {
         Map<String, String> docnoTextMap = new HashMap<String, String>();
         
         GZIPInputStream gzInputStream = new GZIPInputStream(
                 new FileInputStream(fileName));
         
         BufferedReader inReader = new BufferedReader(
                         new InputStreamReader(gzInputStream));
 
         String line = null ;
         while ((line = inReader.readLine()) != null) 
         {
             int tabIdx = line.indexOf("\t") ;
             if ( tabIdx != -1 )
             {
                 String docno = line.substring(0, tabIdx );
                 String text = line.substring(tabIdx + 1);
                 docnoTextMap.put(docno, text);
             }
         }
         inReader.close();
         return docnoTextMap;
     }
     
  /** From: http://blog.mark-mclaren.info/2007/02/invalid-xml-characters-when-valid-utf8_5873.html
      *  and modified by Mark Smucker to use codepoint and insert blanks rather than delete
      * 
      * This method ensures that the output String has only
      * valid XML unicode characters as specified by the
      * XML 1.0 standard. For reference, please see
      * <a href="http://www.w3.org/TR/2000/REC-xml-20001006#NT-Char">the
      * standard</a>. This method will return an empty
      * String if the input is null or empty.
      *
      * @param in The String whose non-valid characters we want to remove.
      * @return The in String, stripped of non-valid characters.
      */
     public static String stripNonValidXMLCharacters(String in) 
     {
         StringBuilder out = new StringBuilder(); // Used to hold the output.
         int current; // Used to reference the current character.
 
         if (in == null || ("".equals(in))) return ""; // vacancy test.
         for (int i = 0; i < in.length(); i++) 
         {
             // actually check the unicode value
             current = in.codePointAt(i); // NOTE: No IndexOutOfBoundsException caught here; it should not happen.
             if ((current == 0x9) ||
                 (current == 0xA) ||
                 (current == 0xD) ||
                 ((current >= 0x20) && (current <= 0xD7FF)) ||
                 ((current >= 0xE000) && (current <= 0xFFFD)) ||
                 ((current >= 0x10000) && (current <= 0x10FFFF)))
                 out.append(in.charAt(i)); // append the surragote or actual char
             else
                 out.append(' ');
         }
         return out.toString();
     }        
     
     
 }
