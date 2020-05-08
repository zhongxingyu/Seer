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
 import java.net.URL;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Scanner;
 import java.util.Vector;
 import java.util.zip.GZIPInputStream;
 import java.util.zip.GZIPOutputStream;
 
 import net.htmlparser.jericho.Element;
 import net.htmlparser.jericho.HTMLElementName;
 import net.htmlparser.jericho.Renderer;
 import net.htmlparser.jericho.Source;
 
 import org.wikipedia.miner.util.SentenceSplitter;
 
 import edu.cmu.lemurproject.WarcHTMLResponseRecord;
 import edu.cmu.lemurproject.WarcRecord;
 
 /**
  *
  * @author Mark D. Smucker
  */
 public class Clueweb2trectext 
 {
     static final org.wikipedia.miner.util.SentenceSplitter sentenceSpliter = new SentenceSplitter();
     //static final TokenizerFactory TOKENIZER_FACTORY = IndoEuropeanTokenizerFactory.INSTANCE;
     //static final SentenceModel SENTENCE_MODEL  = new IndoEuropeanSentenceModel( false, false ) ;
     //static final SentenceChunker SENTENCE_CHUNKER 
     //    = new SentenceChunker(TOKENIZER_FACTORY,SENTENCE_MODEL);
 
     public static void main(String[] args) throws IOException,
             FileNotFoundException, Exception 
     {
         // Okay, the easiest way to do this is to pass in an input directory and output dir
         if (args.length != 5) 
          {
              System.err.println("usage: java Clueweb2trectext inputDir outputDir fusionSpam.ge70.gz docno.mapping.simple AnchorTextFileDir");
              return;
          }
         
         String inputDirName = args[0];
         String outputDirName = args[1];
         String hamDocnosFileName = args[2];
         String mappingFileName = args[3];
         String anchorTextDir = args[4];
         
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
 
         File anchorTextDirectory = new File(anchorTextDir);
         if (!anchorTextDirectory.exists()) 
         {
             System.err.println("Anchor textFile directory  does not exist: "
                     + anchorTextDirectory);
             return;
         }
         ClueWebDocnoMapping docnoMapper = new ClueWebDocnoMapping(mappingFileName);
         ClueWebDocnoSet hamDocnos = new ClueWebDocnoSet(hamDocnosFileName, docnoMapper);
 
         // for each warc.gz file in inputDir, convert it and output new file to outputDir
 
         String[] fileNames = inputDir.list();
         for (int fileNameIdx = 0; fileNameIdx < fileNames.length; ++fileNameIdx) 
         {
             String currFileName = fileNames[fileNameIdx];
 
             if (currFileName.endsWith("warc.gz")) 
             {
                 ConvertWarcToTrec(inputDir, currFileName, outputDir, hamDocnos,
                         anchorTextDirectory);
             }
         }
     }
 
     public static void ConvertWarcToTrec(File inputDir,
             String gzipWarcFileName, File outputDir, ClueWebDocnoSet hamDocnos,
             File anchorTextDir) throws IOException, Exception 
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
         PrintWriter trecFile = new PrintWriter(new BufferedOutputStream(
                 new GZIPOutputStream(new FileOutputStream(outputDir.getAbsolutePath()
                         + File.separator + gzipTrectextFileName))));
 
         String inputWarcFile = inputDir.getAbsolutePath() + File.separator
                 + gzipWarcFileName;
 
         // open our gzip input stream
         GZIPInputStream gzInputStream = new GZIPInputStream(
                 new FileInputStream(inputWarcFile));
 
         /// cast to a data input stream
         DataInputStream inStream = new DataInputStream(gzInputStream);
 
         // iterate through our stream
         WarcRecord thisWarcRecord;
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
                 doc.append("</DOCNO>\n<TEXT>\n");
 
                 ByteArrayInputStream contentStream = new ByteArrayInputStream(
                         contentBytes);
                 BufferedReader inReader = new BufferedReader(
                         new InputStreamReader(contentStream));
 
                 String url = htmlRecord.getTargetURI();
 
                 // forward to the first /n/n
 
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
                 // read them all into a string buffer
                 // to remove all new lines
                 StringBuilder html = new StringBuilder(65536);
                 while ((line = inReader.readLine()) != null) 
                 {
                     html.append(line);
                 }
                 Source source = new Source(html);
                 source.setLogger(null); // turn off logging
                 source.fullSequentialParse(); // error message recommends calling this after creation
                 String title = getTitle(source);
                 Renderer renderer = source.getRenderer();
                 renderer.setIncludeHyperlinkURLs(false);
                 String renderedText = renderer.toString();
                 //TextExtractor textExtractor = source.getTextExtractor() ;
                 //String renderedText = textExtractor.toString() ;
 
                 // identify and mark up sentences
                 String sentenceText = renderedText;
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
                 sentenceText = sbSentenceText.toString();
                 //                Chunking chunking 
                 //                    = SENTENCE_CHUNKER.chunk(renderedText.toCharArray(),0,renderedText.length());
                 //                Set<Chunk> sentences = chunking.chunkSet();
                 //                int minSentenceLength = 80 ; // a line of google snippet is around 90 chars
                 //                if (sentences.size() >= 1) 
                 //                {
                 //                    StringBuilder sbSentenceText = new StringBuilder( (int)(renderedText.length()*1.20) ) ;
                 //                    String slice = chunking.charSequence().toString();
                 //                    int lengthAdded = 0 ;
                 //                    for (Iterator<Chunk> it = sentences.iterator(); it.hasNext(); ) 
                 //                    {
                 //                        Chunk sentence = it.next();
                 //                        int start = sentence.start();
                 //                        int end = sentence.end();
                 //                        String sentToAppend = slice.substring(start,end);
                 //                        if ( lengthAdded == 0 )
                 //                        {
                 //                            sbSentenceText.append( "<sentence>") ;                            
                 //                        }
                 //                        else
                 //                        {
                 //                            // make sure there is a space between appended
                 //                            // sentences.
                 //                            sbSentenceText.append( " ") ; 
                 //                        }
                 //                        sbSentenceText.append( sentToAppend ) ;
                 //                        lengthAdded += sentToAppend.length() ;
                 //                        if ( lengthAdded >= minSentenceLength )
                 //                        {
                 //                            sbSentenceText.append( "</sentence>" ) ;
                 //                            lengthAdded = 0 ;
                 //                        }
                 //                    }
                 //                    if ( lengthAdded > 0)
                 //                    {
                 //                            sbSentenceText.append( "</sentence>" ) ;
                 //                            lengthAdded = 0 ;                        
                 //                    }                
                 //                    sentenceText = sbSentenceText.toString() ;
                 //                }    
 
                 //String tokenizedurl = TokenizeUrl( url ) ;
                 String anchorText = anchorMap.get(docno) ; 
                 if ( anchorText == null ) 
                 {
                     anchorText = "" ;
                 }
                 doc.append("<title>");
                 doc.append(title);
                 doc.append("</title>\n<url>");
                 doc.append(url);
                 doc.append("</url>\n");
                 doc.append(TokenizeUrl(url));
                 doc.append("<body>\n");
                 doc.append(sentenceText);
                 doc.append("\n</body>\n");
                 doc.append("<anchortext>" + anchorText + "</anchortext>\n");
                 doc.append("</text>\n</DOC>\n");
                 System.out.println(docno);
                 trecFile.print(doc.toString());
             }
         }
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
         String tokenUrl = "";
         String protocol = url.substring(0, url.indexOf("//"));
         String words = "";
 
         if (url.indexOf("?") == -1)
             words = url.substring(url.indexOf("/", url.indexOf("//") + 2));
         else
             words = url.substring(url.indexOf("/", url.indexOf("//") + 2), url.indexOf("?"));
         tokenUrl = "<tokenizedurl>url broken into tokens with protocol ("
                 + protocol + "// stripped and then broken into words on . "
                 + words + ""
                 + " and truncated at the prescence of ? </tokenizedurl>\n";
         return tokenUrl;
     }
 
     public static Map<String, String> readAnchorTextFile(String fileName)
             throws IOException 
     {
         Map<String, String> docnoTextMap = new HashMap<String, String>();
         GZIPInputStream gzInputStream = new GZIPInputStream(
                 new FileInputStream(fileName));
         Scanner input = new Scanner(gzInputStream);
 
         while (input.hasNext()) 
         {
             String line = input.nextLine();
            String docno = line.substring(0, line.indexOf("\t"));
            String text = line.substring(line.indexOf("\t") + 1);
            docnoTextMap.put(docno.trim(), text.trim());
         }
         return docnoTextMap;
     }
 }
