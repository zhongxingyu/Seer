 package uk.ac.susx.tag.wag;
 
 import static com.google.common.base.Preconditions.*;
 import static java.util.concurrent.TimeUnit.NANOSECONDS;
 
 import com.google.common.base.Stopwatch;
 import com.google.common.io.Closeables;
 import com.google.common.io.Closer;
 import com.google.common.io.CountingInputStream;
 import edu.jhu.nlp.wikipedia.PageCallbackHandler;
 import edu.jhu.nlp.wikipedia.WikiPage;
 import edu.jhu.nlp.wikipedia.WikiXMLParser;
 import edu.jhu.nlp.wikipedia.WikiXMLSAXParser;
 import org.sweble.wikitext.engine.*;
 import org.sweble.wikitext.engine.Compiler;
 import org.sweble.wikitext.engine.utils.SimpleWikiConfiguration;
 import org.sweble.wikitext.lazy.LinkTargetException;
 import uk.ac.susx.tag.util.CompressorStreamFactory2;
 import uk.ac.susx.tag.util.DateTimeUtils;
 import uk.ac.susx.tag.util.MiscUtil;
 
 import javax.xml.bind.JAXBException;
 import java.io.*;
 import java.net.URL;
 import java.util.EnumSet;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * Created with IntelliJ IDEA.
  * User: hiam20
  * Date: 19/02/2013
  * Time: 16:40
  * To change this template use File | Settings | File Templates.
  */
 public class WikiAliasGenerator {
     private static final Logger LOG = Logger.getLogger(WikiAliasGenerator.class.getName());
     // Configuration defaults
     public static final boolean DEFAULT_identityAliasesProduced = false;
 
     //
     private final AliasHandler handler;
 
     private final EnumSet<AliasType> producedTypes;
 
     // Configuration parameters
     private boolean identityAliasesProduced = DEFAULT_identityAliasesProduced;
 
     public WikiAliasGenerator(AliasHandler handler, EnumSet<AliasType> producedTypes) throws FileNotFoundException, JAXBException {
         this.handler = checkNotNull(handler, "handler");
         this.producedTypes = producedTypes;
     }
 
     public void setIdentityAliasesProduced(boolean identityAliasesProduced) {
         this.identityAliasesProduced = identityAliasesProduced;
     }
 
     public void process(URL wikiXmlUrl, int limit) throws Exception {
         final CompressorStreamFactory2 compressorFactory = CompressorStreamFactory2.builder()
                 .setTransparentSignatureDetection(true)
                 .build();
         Closer closer = Closer.create();
 
         try {
 
 
             final InputStream inputStream =
                     closer.register(compressorFactory.createCompressorInputStream(
                             closer.register(new BufferedInputStream(
                                     closer.register(wikiXmlUrl.openStream())))));
 
             process(inputStream, limit, -1L);
 
         } finally {
             Closeables.close(closer, true);
         }
 
 
     }
 
     public void process(File wikiXmlFile, int limit) throws Exception {
 //        final CompressorStreamFactory2 compressorFactory = CompressorStreamFactory2.builder().build();
         Closer closer = Closer.create();
 
         try {
 
             final InputStream inputStream =
 //                    closer.registerEnumSet(compressorFactory.createCompressorInputStream(
                     closer.register(new BufferedInputStream(
                             closer.register(new FileInputStream(wikiXmlFile))));
 
             process(inputStream, limit, wikiXmlFile.length());
 
         } finally {
             Closeables.close(closer, true);
         }
 
 
     }
 
     public void process(final InputStream inputStream, final int limit, final long expectedSizeBytes) throws Exception {
 
         final org.sweble.wikitext.engine.Compiler swebleCompiler;
         SimpleWikiConfiguration config = new SimpleWikiConfiguration(
                 "classpath:/org/sweble/wikitext/engine/SimpleWikiConfiguration.xml");
         swebleCompiler = new Compiler(config);
 
         final CompressorStreamFactory2 compressorFactory = CompressorStreamFactory2.builder()
                 .setTransparentSignatureDetection(true)
                 .build();
 
         final CountingInputStream cis = new CountingInputStream(inputStream);
 
         final InputStream xis = compressorFactory.createCompressorInputStream(
                 new BufferedInputStream(cis));
 
 
         final WikiXMLParser parser = new WikiXMLSAXParser(xis);
 
         final Stopwatch sw = new Stopwatch();
         sw.start();
 
         parser.setPageCallback(new PageCallbackHandler() {
 
             private int count = 0;
 
             @Override
             public void process(WikiPage page) {
 //                System.out.println("Title: " + page.getTitle());
                if (limit >= 0 && count > limit) {
                     throw new WikiXMLParserHaltException();
                 }
 
                 // Display progress intermittently
                 if (count % 1000 == 0 && LOG.isLoggable(Level.INFO)) {
                     if (expectedSizeBytes >= 0) {
                         // We know how much data to expect so we can predict time remaining
                         final long bytesReader = cis.getCount();
                         final double pctComplete = (100.0 * bytesReader) / expectedSizeBytes;
                         LOG.log(Level.INFO, "Processed {0} of {1} ({2}% complete.)",
                                 new Object[]{MiscUtil.humanReadableBytes(cis.getCount()),
                                         MiscUtil.humanReadableBytes(expectedSizeBytes), pctComplete});
 
                         long elapsed = sw.elapsed(NANOSECONDS);
                         long remaining = (long) (elapsed * (100.0 - pctComplete) / pctComplete);
                         LOG.log(Level.INFO, "Elapsed time {0} (Estimated {1} remaining.)",
                                 new Object[]{DateTimeUtils.humanReadableTime(elapsed, NANOSECONDS),
                                         DateTimeUtils.humanReadableTime(remaining, NANOSECONDS)});
                     }
                 }
 
                 try {
 
                     final PageTitle pageTitle = PageTitle.make(swebleCompiler.getWikiConfig(), page.getTitle());
                     final PageId pageId = new PageId(pageTitle, -1);
                     final CompiledPage cp = swebleCompiler.postprocess(pageId, page.getWikiText(), null);
 
                     final AliasAstVisitor visitor = new AliasAstVisitor(
                             page.getTitle(), swebleCompiler.getWikiConfig(), producedTypes);
                     final List<Alias> aliases = (List<Alias>) visitor.go(cp.getPage());
 
                     for (Alias alias : aliases) {
 
                         // Don't produce aliases where the soruce and target are the same (if disabled)
                         if (!identityAliasesProduced && alias.getTarget().equals(alias.getSource()))
                             continue;
 
 
                         handler.handle(alias);
 
                     }
 
 
                 } catch (CompilerException e) {
                     LOG.log(Level.WARNING, "Failed to parse WikiText in page: " + page.getTitle(), e);
                 } catch (LinkTargetException e) {
                     LOG.log(Level.WARNING, "Failed to parse WikiText in page: " + page.getTitle(), e);
                 }
 
                 ++count;
             }
 
         });
 
         try {
             parser.parse();
         } catch (WikiXMLParserHaltException ex) {
             // swallow
         }
 
     }
 
 
     /**
      * WikiXMLParserHaltException is dummy exception used to stop the JWikiXML parser prematurely. This is useful
      * because
      * it sometimes only the first K articles need to be processed.
      */
     private final class WikiXMLParserHaltException extends RuntimeException {
 
     }
 
 
 }
