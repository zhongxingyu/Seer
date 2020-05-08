 /**
  *
  * @author Machiel Jansen, Edgar Meij
  */
 package indexer;
 
 import java.io.Closeable;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.Reader;
 import java.nio.file.FileVisitResult;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.nio.file.SimpleFileVisitor;
 import java.nio.file.attribute.BasicFileAttributes;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.apache.lucene.analysis.Analyzer;
 import org.apache.lucene.analysis.TokenStream;
 import org.apache.lucene.document.Document;
 import org.apache.lucene.document.Field.Store;
 import org.apache.lucene.document.StringField;
 import org.apache.lucene.document.TextField;
 import org.apache.lucene.index.CorruptIndexException;
 import org.apache.lucene.index.IndexWriter;
 import org.apache.tika.Tika;
 import org.apache.tika.config.TikaConfig;
 import org.apache.tika.metadata.Metadata;
 
 public class BaseIndexing implements AutoCloseable {
 	public enum FixedFields {
 		ID,
 		CONTENT,
 		MEDIA_TYPE("mediaType");
 
 		public final String fieldName;
 
 		private FixedFields() {
 			fieldName = this.name().toLowerCase();
 		}
 
 		private FixedFields(final String fieldName_) {
 			fieldName = fieldName_;
 		}
 	}
   
   public File datadir;
 	private final TikaConfig tc = TikaConfig.getDefaultConfig();
 	private final AnalyzerFactory analyzerFactory;
 
   private IndexWriterUtil indexWriterUtil;
   private IndexWriter indexWriter;
 //  public FSDirectory indexdir;
 //  public final File cachedir;
   private ConfigurationHandler cfg;
   private HandlerFactory hf;
   public int added = 0;
   public int failed = 0;
   
   /** logger for Commons logging. */
   private transient Logger log =
    Logger.getLogger("BaseIndexing.class.getName()");
 
   /** Creates a new instance of BaseIndexing
    * @param       configFile      path to the configurationfile
    * @param       name            Name of the index to use
    * @param       dataPath        path to the files
    */
   public BaseIndexing(String configFile, String name, String dataPath) {
     this(new ConfigurationHandler(configFile), name, dataPath);
   }
   
   public BaseIndexing(ConfigurationHandler cfg, String name, String dataPath) {
     
     this.cfg = cfg;
     
     hf = new HandlerFactory(cfg);
 	analyzerFactory = new AnalyzerFactory(cfg);
 
 	indexWriterUtil = (name == null || name.length() == 0) ?
 			new IndexWriterUtil(cfg) :
 			new IndexWriterUtil(cfg, name);
 	
     datadir = (dataPath == null || dataPath.length() == 0) ?
 			new File(cfg.getDataPath()) :
 			new File(dataPath);
 	
 		if (log.isLoggable(Level.FINE)) {
 		  log.fine("Indexdir: " + indexWriterUtil.getIndexdir());
 		  log.fine("Datadir: " + datadir);
 		}
 		
   }
 
 	@Override
 	public void close() {
 		indexWriterUtil.closeIndexWriter();
 	}
 
   /** Adds Documents to the index */
   public boolean addDocuments() throws IOException{
 	assertIsDirectory(indexWriterUtil.getCacheDir());
 
     indexDocs(datadir);
     indexWriterUtil.getIndexWriter().commit();
     return true;
   }
 
   /**
    * Add the visited file to the Lucene Index of {@link #indexWriter}.
    * 
    * Use {@link Tika} to detect the file type an parse the file into a stream. 
    * Use an {@link Analyzer} as returned by 
    * {@link AnalyzerFactory#getAnalyzer(String)}.
    *
    * @author Kasper van den Berg <kasper@kaspervandenberg.net>
    */
   private class TikaIndexAdder extends SimpleFileVisitor<Path> {
 		@Override
 		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
 			
 			Tika tikaParser = new  Tika(); 
 			VisitedDocument doc = new VisitedDocument(file);
 			doc.storeFilename();
 			Analyzer analyzer = doc.detectAndStoreMediaType(tikaParser);
 			
 			try {
 				doc.storeContent(tikaParser, analyzer);
 				try {
 					indexWriterUtil.getIndexWriter().addDocument(doc.getDocument(), analyzer);
 					indexWriterUtil.getIndexWriter().commit();
 					indexWriterUtil.copyToCache(file);
 					
 				} catch (OutOfMemoryError ex) {
 					indexWriterUtil.handleOutOfMememoryError(ex);
 					
 					failed++;
 					throw ex;
 				} catch (CorruptIndexException ex) {
 					String msg =  String.format(
 							"Lucene index %s corrupt, rebuild index",
 							indexWriterUtil.getIndexdir().getName());
 					log.log(Level.SEVERE, msg, ex);
 					
 					failed++;
 					throw ex;
 				} catch (IOException ex) {
 					// Skip this file continue, with next
 					String msg = String.format(
 							"Error indexing: while writing to index %s file %s",
 							indexWriterUtil.getIndexdir().getName(),
 							file.toString());
 					log.log(Level.SEVERE, msg, ex);
 					
 					failed++;
 					return FileVisitResult.CONTINUE;
 				}
 			} catch (IOException ex) {
 				// Skip this file
 				String msg = String.format(
 						"Error indexing: while parsing file %s",
 						file.toString());
 				log.log(Level.WARNING, msg, ex);
 
 				failed++;
 				return FileVisitResult.CONTINUE;
 			}
 			
 			added++;
 			return FileVisitResult.CONTINUE;
 		}
 
 		@Override
 		public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
 			log.log(Level.WARNING, String.format(
 					"Error indexing file %s into index %s",
 					file,
 					indexWriterUtil.getIndexdir().getName()), exc);
 			return FileVisitResult.CONTINUE;
 		}
 
 		private class VisitedDocument {
 			private final Document doc = new Document();
 			private final Metadata metadata = new Metadata();
 			private final Path file;
 
 			public VisitedDocument(Path file_) {
 				file = file_;
 			}
 			
 			public void storeFilename() {
 				doc.add(new StringField(
 						FixedFields.ID.fieldName, file.toFile().getName(), Store.YES));
 				metadata.add(Metadata.RESOURCE_NAME_KEY, file.toFile().getName());
 			}
 
 			public Analyzer detectAndStoreMediaType(Tika parser) {
 				try {
 					String mediaType = parser.detect(file.toFile());
 					
 					doc.add(new StringField(
 							FixedFields.MEDIA_TYPE.fieldName, mediaType, Store.YES));
 
 					// Todo AnalyzerFactory uses filename extension, change it to use
 					// Mime type
 					return analyzerFactory.getAnalyzer(mediaType);
 				} catch (IOException ex) {
 					// Fall back to global analyzer and omit media type field.
 					String msg = String.format(
 							"Error Indexing: while detecting file type of %s",
 							file.toString());
 					log.log(Level.WARNING, msg, ex);
 					
 					return analyzerFactory.getGlobalAnalyzer();
 				}
 			}
 
 			public void storeContent(Tika parser, Analyzer analyzer)
 					throws IOException {
 				Reader content = parser.parse(
 						new FileInputStream(file.toFile()), metadata);
 
 				TokenStream tokenStream = analyzer.tokenStream(
 						FixedFields.CONTENT.fieldName, content);
 				doc.add(new TextField(FixedFields.CONTENT.fieldName, tokenStream));
 			}
 
 			public Document getDocument() {
 				return doc;
 			}
 		}
 	}
 
 	/** Does the actual indexing
 	 * @param       file            File to index
 	 */
 	private void indexDocs(File file) {
 		// TODO Integrate Tica config with 'indexconfig.xml'
 		try {
 			Files.walkFileTree(file.toPath(), new TikaIndexAdder());
 		} catch (IOException ex) {
 			String msg = String.format("Failed to index %s into index %s",
 					file.getPath(),
 					indexWriterUtil.getIndexdir().getName());
 			throw new Error(msg, ex);
 		}
 	}
 
   public File getIndexdir() {
 	  return indexWriterUtil.getIndexdir();
   }
 
   private void assertIsDirectory(File f) {
 	if(!f.exists()) {
 		f.mkdirs();
 	}
 	
 	if((!f.exists()) || (!f.isDirectory())) {
 		throw new RuntimeException(String.format("Error creating directory %s", f.toString()));
 	}
   }
 }
 
 /* vim: set shiftwidth=4 tabstop=4 noexpandtab fo=ctwan ai : */
 
