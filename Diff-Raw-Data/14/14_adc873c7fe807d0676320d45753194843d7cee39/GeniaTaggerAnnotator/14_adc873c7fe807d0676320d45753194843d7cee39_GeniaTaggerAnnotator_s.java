 package txtfnnl.uima.analysis_component;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.apache.uima.UimaContext;
 import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
 import org.apache.uima.cas.CASException;
 import org.apache.uima.cas.FSIterator;
 import org.apache.uima.jcas.JCas;
 import org.apache.uima.jcas.tcas.Annotation;
 import org.apache.uima.resource.ResourceInitializationException;
 import org.apache.uima.util.Level;
 import org.apache.uima.util.Logger;
 
 import org.uimafit.component.JCasAnnotator_ImplBase;
 import org.uimafit.descriptor.ConfigurationParameter;
 
 import txtfnnl.subprocess.ReadlineRuntime;
 import txtfnnl.uima.AnalysisComponentBuilder;
 import txtfnnl.uima.Views;
 import txtfnnl.uima.tcas.SentenceAnnotation;
 import txtfnnl.uima.tcas.TokenAnnotation;
 
 /** A simple data type for the tokens (lines) produced by the GENIA tagger. */
 class Token {
   private final String[] content;
   private static final int WORD = 0;
   private static final int STEM = 1;
   private static final int POS = 2;
   private static final int CHUNK = 3;
   private static final int NER = 4;
   @SuppressWarnings("unused")
   private static final int PROTEIN = 5; // TODO: enable protein tagging
 
   Token(String raw) {
     content = raw.split("\\t");
     assert content.length == 5;
   }
 
   String word() {
     return content[WORD];
   }
 
   String stem() {
     return content[STEM];
   }
 
   String partOfSpeech() {
     return content[POS];
   }
 
   String chunk() {
     return content[CHUNK];
   }
 
   String entityTag() {
     return content[NER];
   }
 
   @Override
   public String toString() {
     final StringBuilder sb = new StringBuilder();
     for (final String s : content) {
       sb.append(s);
       sb.append('\t');
     }
     sb.deleteCharAt(sb.length() - 1);
     return sb.toString();
   }
 }
 
 /**
  * A wrapper for the <code>geniatagger</code> executable. For this wrapper to work, the directory
  * with the the GENIA models and dictionaries must be known and its content readable.
  * 
  * @author Florian Leitner
  */
 class GeniaTagger extends ReadlineRuntime<List<Token>> {
   /**
    * Create a new link-parser process fork.
    * 
    * @param dictPath the path to the parser dictionaries and models
    * @param logger to handle all error messages
    * @throws IOException on failure
    */
   public GeniaTagger(String dictPath, Logger logger) throws IOException {
     super("geniatagger", new File(dictPath), logger);
   }
 
   /**
    * Tag a single sentence.
    * 
    * @return the tagged tokens
    * @throws IOException on IO failures
    */
   @Override
   protected List<Token> parseResponse() throws IOException {
     final List<Token> tokens = new LinkedList<Token>();
     String line = readLine();
     while (line.length() > 0) {
       tokens.add(new Token(line));
       line = readLine();
     }
     return tokens;
   }
 }
 
 /**
  * An Genia Tagger Token AE variant for the txtfnnl pipeline. For this annotator to work, the
  * <code>geniatagger</code> executable has to be on the global <code>PATH</code>.
  * <p>
  * This AE tokenizes a {@link SentenceAnnotation sentence}, and sets the PoS tag and chunk tag
  * feature for each detected {@link TokenAnnotation token}.
  * 
  * @author Florian Leitner
  */
 public class GeniaTaggerAnnotator extends JCasAnnotator_ImplBase {
   /** The annotator's URI (for the annotations) set by this AE. */
   public static final String URI = "http://www.nactem.ac.uk/tsujii/GENIA/tagger";
   /** The namespace used for all annotations. */
   public static final String NAMESPACE = "http://nlp2rdf.lod2.eu/schema/doc/sso/";
   /** The identifier used for all annotations. */
   public static final String IDENTIFIER = "Word";
   /**
    * The directory path used by the GENIA Tagger. If in their default location at
    * <code>/usr/local/share/geniatagger/</code>, it is not necessary to set this parameter.
    */
   public static final String PARAM_DIRECTORY = "DirectoryPath";
   @ConfigurationParameter(name = PARAM_DIRECTORY,
       defaultValue = "/usr/local/share/geniatagger/",
       description = "Path to the directory with the model files.")
   private String dictionariesPath;
   protected Logger logger;
   private GeniaTagger tagger;
 
   public static class Builder extends AnalysisComponentBuilder {
     public Builder() {
       super(GeniaTaggerAnnotator.class);
     }
 
     /**
      * Configure the directory where the tagger and the dictionaries are located. By default, these
      * would be in <code>/usr/local/share/geniatagger/</code>.
      * 
      * @param path of the directory
      */
     public Builder setDirectory(File path) throws IOException {
       if (path != null) {
         String p = path.getAbsolutePath();
         if (!path.exists()) throw new IOException("'" + p + "' does not exist");
         if (!path.isDirectory())
           throw new IllegalArgumentException("'" + p + "' is not a directory");
         if (!path.canRead()) throw new IllegalArgumentException("'" + p + "' cannot be read");
       }
       setOptionalParameter(PARAM_DIRECTORY, path.getCanonicalPath());
       return this;
     }
   }
 
   /**
    * Configure a Genia Tagger AE Builder.
    */
   public static Builder configure() {
     return new Builder();
   }
 
   @Override
   public void initialize(UimaContext ctx) throws ResourceInitializationException {
     super.initialize(ctx);
     logger = ctx.getLogger();
     try {
       tagger = new GeniaTagger(dictionariesPath, logger);
     } catch (final IOException e) {
       logger.log(Level.SEVERE, "geniatagger setup failed (dir: ''{0}'')", dictionariesPath);
       throw new ResourceInitializationException(e);
     }
     logger.log(Level.INFO, "initialized GENIA tagger");
   }
 
   @Override
   public void process(JCas jcas) throws AnalysisEngineProcessException {
     // TODO: use default view
     JCas textCas;
     try {
       textCas = jcas.getView(Views.CONTENT_TEXT.toString());
     } catch (final CASException e) {
       throw new AnalysisEngineProcessException(e);
     }
     final FSIterator<Annotation> sentenceIt = SentenceAnnotation.getIterator(textCas);
     List<Token> tokens;
     int count = 0;
     while (sentenceIt.hasNext()) {
       final Annotation sentenceAnn = sentenceIt.next();
       final String sentence = sentenceAnn.getCoveredText().replace('\n', ' ');
       final int sentenceOffset = sentenceAnn.getBegin();
       if (sentence.length() > 4096) {
         logger.log(Level.WARNING, "skipping too long sentence at offset {0}", sentenceOffset);
         continue;
       }
       int wordOffset = 0;
       int searchOffset = 0;
       int wordLength = 0;
       try {
         tokens = tagger.process(sentence);
       } catch (final IOException e) {
        logger.log(Level.SEVERE, "geniatagger failed on: ''{0}''", sentence);
        throw new AnalysisEngineProcessException(e);
       }
       TokenAnnotation last = null;
       count += tokens.size();
       for (final Token t : tokens) {
         wordLength = t.word().length();
         wordOffset = sentence.indexOf(t.word(), searchOffset);
         if (wordOffset == -1 && "``".equals(t.word()) || "''".equals(t.word())) {
           wordOffset = sentence.indexOf("\"", searchOffset);
           wordLength = 1;
         }
         if (wordOffset == -1) {
           logger.log(Level.SEVERE, "unmatched word ''{0}'' in ''{1}'' at {2}",
               new Object[] { t.word(), sentence, searchOffset });
           throw new AnalysisEngineProcessException(new RuntimeException("unmatched token"));
         }
         /* annotate the token */
         final TokenAnnotation tokenAnn = new TokenAnnotation(textCas, sentenceOffset + wordOffset,
             sentenceOffset + wordOffset + wordLength);
         tokenAnn.setAnnotator(URI);
         tokenAnn.setNamespace(NAMESPACE);
         tokenAnn.setIdentifier(IDENTIFIER);
         tokenAnn.setConfidence(1.0);
         tokenAnn.setPos(t.partOfSpeech());
         tokenAnn.setStem(t.stem().toLowerCase());
         switch (t.chunk().charAt(0)) {
         case 'B':
           if (last != null) last.setChunkEnd(true);
           tokenAnn.setChunk(t.chunk().substring(2));
           tokenAnn.setChunkBegin(true);
           last = tokenAnn;
           break;
         case 'I':
           tokenAnn.setChunk(t.chunk().substring(2));
           last = tokenAnn;
           break;
         case 'O':
           if (last != null) last.setChunkEnd(true);
           last = null;
           break;
         default:
           logger.log(Level.SEVERE, "unexpected chunk tag ''{0}''", t.chunk());
           throw new AnalysisEngineProcessException(new RuntimeException("illeagal chunk tag"));
         }
         tokenAnn.addToIndexes();
         searchOffset = wordOffset + wordLength;
       }
       if (last != null) last.setChunkEnd(true);
     }
     logger.log(Level.FINE, "annotated {0} tokens", count);
   }
 
   @Override
   public void destroy() {
     super.destroy();
     try {
       tagger.stop();
     } catch (final IOException e) {
       logger.log(Level.INFO, "IOException while destorying the tagger: {0}", e.getMessage());
     }
   }
 }
