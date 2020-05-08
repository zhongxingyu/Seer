 package run;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.net.URL;
 import java.security.ProtectionDomain;
 import java.util.Arrays;
 import java.util.Iterator;
 
 import org.apache.lucene.analysis.Analyzer;
 import org.apache.lucene.analysis.en.EnglishAnalyzer;
 import org.apache.lucene.analysis.standard.StandardAnalyzer;
 import org.apache.lucene.document.Document;
 import org.apache.lucene.document.Field;
 import org.apache.lucene.document.NumericField;
 import org.apache.lucene.index.IndexWriter;
 import org.apache.lucene.index.IndexWriterConfig;
 import org.apache.lucene.index.Term;
 import org.apache.lucene.index.IndexWriterConfig.OpenMode;
 import org.apache.lucene.queryParser.ParseException;
 import org.apache.lucene.queryParser.QueryParser;
 import org.apache.lucene.search.BooleanClause;
 import org.apache.lucene.search.BooleanQuery;
 import org.apache.lucene.search.FuzzyQuery;
 import org.apache.lucene.search.PhraseQuery;
 import org.apache.lucene.search.PrefixQuery;
 import org.apache.lucene.search.Query;
 import org.apache.lucene.search.TermQuery;
 import org.apache.lucene.search.WildcardQuery;
 import org.apache.lucene.store.Directory;
 import org.apache.lucene.store.FSDirectory;
 import org.apache.lucene.util.Version;
 import org.doubleshow.lucene_web.Dpc;
 import org.doubleshow.lucene_web.SearchOptsImpl;
 import org.doubleshow.lucene_web.model.DpcDao;
 import org.doubleshow.lucene_web.model.DpcDaoException;
 import org.doubleshow.lucene_web.model.DpcDoc;
 import org.doubleshow.lucene_web.model.DpcSearchOpts;
 import org.doubleshow.lucene_web.model.DpcSearchResult;
 import org.doubleshow.lucene_web.model.impl.DpcDaoImpl;
 import org.eclipse.jetty.server.Server;
 import org.eclipse.jetty.server.bio.SocketConnector;
 import org.eclipse.jetty.webapp.WebAppContext;
 
 import com.csvreader.CsvReader;
 
 public class Main {
 	static void printHelp() {
 		System.out.println("-h this help");
 		System.out.println("-i <path to the index> [default:'" + index + "']");
 		System.out.println("-d <path to the docs> [default:'" + docs + "']");
 		System.out.println("-s run server [default: " + serve + "]");
 		System.out.println("-c create index [default: " + createIndex + "]");
 		System.out.println("-u update index [default: " + updateIndex + "]");
 		System.out.println("-p <port> server port [default: " + port + "]");
 		System.out.println("-a <address/host> server address [default: " + host + "]");
 		System.out.println("-qo <file path> output file for query results [default: " + outputFile
 				+ " (print to standard output)](query mode)");
 		System.out.println("-qn <results num> max number of results[default: " + resultsNum + "](query mode)");
 		System.out.println("-q <query> (activates query mode, all the arguments after it are treated as part of query)");
 		System.out.println("-st use stemmer [default: " + useStemmer + "]");
 		System.out.println("-pq nicely print parsed query [default: " + printQuery + "]");
 		System.out.println("-et exclude title [default: " + excludeTitle + "]");
 		System.out.println("-ec exclude comment [default: " + excludeComment + "]");
 	}
 
 	static String index = Dpc.DEFAULT_INDEX, docs = Dpc.DEFAULT_DOCS, host = "localhost";
 	static boolean serve = false, createIndex = false, updateIndex = false;
 	static int port = 8080;
 	static String outputFile = "-", query = null;
 	static int resultsNum = 10;
 	static boolean useStemmer = false, printQuery = false;
 	static boolean excludeTitle = false;
 	static boolean excludeComment = false;
 	// for testing purpose, we can specify war/directory location for server mode
 	static final String P_WAR_FILE = "warFile";
 
 	public static void main(String[] args) throws Exception {
 
 		for (Iterator<String> it = Arrays.asList(args).iterator(); it.hasNext();) {
 			String v = it.next();
 			if (v.equals("-h")) {
 				printHelp();
 				return;
 			} else if (v.equals("-i")) {
 				if (!it.hasNext())
 					throw new IllegalArgumentException(v + " needs argument");
 				index = it.next();
 			} else if (v.equals("-d")) {
 				if (!it.hasNext())
 					throw new IllegalArgumentException(v + " needs argument");
 				docs = it.next();
 			} else if (v.equals("-p")) {
 				if (!it.hasNext())
 					throw new IllegalArgumentException(v + " needs argument");
 				port = Integer.parseInt(it.next());
 			} else if (v.equals("-a")) {
 				if (!it.hasNext())
 					throw new IllegalArgumentException(v + " needs argument");
 				host = it.next();
 			} else if (v.equals("-s")) {
 				serve = true;
 			} else if (v.equals("-c")) {
 				createIndex = true;
 			} else if (v.equals("-u")) {
 				updateIndex = true;
 			} else if (v.equals("-qo")) {
 				if (!it.hasNext())
 					throw new IllegalArgumentException(v + " needs argument");
 				outputFile = it.next();
 			} else if (v.equals("-qn")) {
 				if (!it.hasNext())
 					throw new IllegalArgumentException(v + " needs argument");
 				resultsNum = Integer.parseInt(it.next());
 			} else if (v.equals("-q")) {
 				if (!it.hasNext())
 					throw new IllegalArgumentException(v + " needs argument");
 				StringBuilder builder = new StringBuilder();
 				while (it.hasNext()) {
 					builder.append(it.next());
 					if (it.hasNext())
 						builder.append(' ');
 				}
 				query = builder.toString();
 			} else if (v.equals("-st")) {
 				useStemmer = true;
 			} else if (v.equals("-pq")) {
 				printQuery = true;
 			} else if (v.equals("-ec")) {
 				excludeComment = true;
 			} else if (v.equals("-et")) {
 				excludeTitle = true;
 			}
 		}
 		if (createIndex || updateIndex) {
 			System.out.println((createIndex ? "creating" : "updating") + " index directory '" + index + "'");
 			index(index, createIndex, docs, useStemmer);
 			return;
 		}
 		if (serve) {
 			System.setProperty(Dpc.SP_PREFIX + Dpc.P_INDEX, index);
 			System.setProperty(Dpc.SP_PREFIX + Dpc.P_USE_STEMMER, useStemmer ? "true" : "false");
 			System.out.println("running server on http://" + host + ':' + port + '/');
 			serve(host, port);
 			return;
 		}
 		if (query != null) {
 			if (printQuery) {
 				printQueryToStdout(query, useStemmer);
 				return;
 			}
 			System.out.println("making query '" + query + "'");
 			query(index, query, resultsNum, outputFile, excludeComment, excludeTitle);
 			return;
 		}
 		printHelp();
 	}
 
 	static void serve(String host, int port) {
 		Server server = new Server();
 		System.out.println(Arrays.toString(server.getHandlers()));
 		SocketConnector connector = new SocketConnector();
 
 		connector.setMaxIdleTime(1000 * 60 * 60);
 		connector.setSoLingerTime(-1);
 		connector.setPort(port);
 		connector.setHost(host);
 		server.addConnector(connector);
 
 		WebAppContext context = new WebAppContext();
 		context.setContextPath("/");
 		context.setDefaultsDescriptor("webdefault.xml");
 		if (System.getProperty(Dpc.SP_PREFIX + P_WAR_FILE) == null) {
 			ProtectionDomain protectionDomain = Main.class.getProtectionDomain();
 			URL location = protectionDomain.getCodeSource().getLocation();
 			context.setWar(location.toExternalForm());
 		} else {
 			context.setWar(System.getProperty(Dpc.SP_PREFIX + P_WAR_FILE));
 		}
 		server.setHandler(context);
 		try {
 			server.start();
 			System.out.println("server started, press enter to stop");
 			System.in.read();
 			server.stop();
 			server.join();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	public static void index(String indexPath, boolean create, String docsPath, boolean useStemmer) throws IOException {
 		Directory dir = FSDirectory.open(new File(indexPath));
 		Analyzer analyzer = useStemmer ? new EnglishAnalyzer(DpcDaoImpl.LUCENE_VERSION) : new StandardAnalyzer(DpcDaoImpl.LUCENE_VERSION);
 		IndexWriterConfig iwc = new IndexWriterConfig(DpcDaoImpl.LUCENE_VERSION, analyzer);
 		if (create) {
 			iwc.setOpenMode(OpenMode.CREATE);
 		} else {
 			iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);
 		}
 		IndexWriter writer = new IndexWriter(dir, iwc);
 		indexDocs(writer, new File(docsPath));
 		writer.close();
 
 	}
 
 	//{ID}{TITLE}{DESCRIPTION}{CATEGORIES}{COMMENT}
 	public static void indexDocs(IndexWriter writer, File dpcFile) throws IOException {
 		int num = 0;
 		CsvReader dpc = new CsvReader(new FileReader(dpcFile), '\t');
 		String value;
 		String fullText;
 		long time = System.currentTimeMillis();
 		while (dpc.readRecord()) {
 			fullText = "";
 			Document doc = new Document();
 
 			NumericField fieldId = new NumericField(Dpc.F_ID, Field.Store.YES, true);
 			fieldId.setLongValue(Long.parseLong(dpc.get(0)));
 			doc.add(fieldId);
 
 			value = nullIfNA(dpc.get(1));
 			if (!dpc.get(1).equals("N/A")) {
 				doc.add(new Field(Dpc.F_TITLE, value, Field.Store.YES, Field.Index.ANALYZED));
 				fullText += value;
 
 			}
 
 			value = nullIfNA(dpc.get(2));
 			if (value != null) {
 				doc.add(new Field(Dpc.F_DESCRIPTION, value, Field.Store.YES, Field.Index.ANALYZED));
 				fullText += value;
 			}
 
 			value = nullIfNA(dpc.get(3));
 			if (value != null) {
 				doc.add(new Field(Dpc.F_CATEGORIES, value, Field.Store.YES, Field.Index.ANALYZED));
 				fullText += value;
 			}
 			value = nullIfNA(dpc.get(4));
 			if (value != null) {
 				doc.add(new Field(Dpc.F_COMMENT, value, Field.Store.YES, Field.Index.ANALYZED));
 				fullText += value;
 			}
 
 			doc.add(new Field(Dpc.F_TEXT, fullText, Field.Store.NO, Field.Index.ANALYZED));
 			if (writer.getConfig().getOpenMode() == OpenMode.CREATE) {
 				writer.addDocument(doc);
 			} else {
 				writer.updateDocument(new Term(Dpc.F_ID, dpc.get(0)), doc);
 			}
 			if (++num % 100 == 0)
 				System.out.print('*');
 		}
 		System.out.println("");
 		System.out.println(String.format("indexed %d in %.3f seconds", num, ((float) (System.currentTimeMillis() - time)) / 1000));
 	}
 
 	static void query(String indexPath, String query, int resultsNum, String outputFile, boolean excludeComment, boolean excludeTitle) throws DpcDaoException, FileNotFoundException {
		DpcDao dao = new DpcDaoImpl(indexPath, useStemmer);
 		DpcSearchOpts search;
 		if (excludeComment || excludeTitle){
 			search = new SearchOptsImpl(query, false, !excludeTitle, true, true, !excludeComment, 0, resultsNum);
 		}else{
 			search = new SearchOptsImpl(query, true, false, false, false, false, 0, resultsNum);
 		}
 		DpcSearchResult result = dao.getTop(search);
 		PrintStream out = "-".equals(outputFile) ? System.out : new PrintStream(outputFile);
 		for (DpcDoc doc : result.getDocs())
 			out.println(doc.getId() + " " + doc.getScore() + " " + doc.getTitle());
 	}
 
 	static void printQueryToStdout(String queryString, boolean useStemmer) throws ParseException {
 		Analyzer analyzer = useStemmer ? new EnglishAnalyzer(DpcDaoImpl.LUCENE_VERSION) : new StandardAnalyzer(DpcDaoImpl.LUCENE_VERSION);
 		QueryParser parser = new QueryParser(DpcDaoImpl.LUCENE_VERSION, Dpc.F_TEXT, analyzer);
 		Query query = parser.parse(queryString);
 		printQuery(query, System.out, "  ", "");
 	}
 
 	static String nullIfNA(String s) {
 		if (s != null && s.equals("N/A"))
 			return null;
 		return s;
 	}
 
 	static void printQuery(Query q, PrintStream out, String ind, String cind) {
 
 		if (q instanceof BooleanQuery) {
 			out.println(cind + "boolean[");
 			BooleanQuery bq = (BooleanQuery) q;
 			for (BooleanClause bc : bq.getClauses()) {
 				out.println(cind + ind + bc.getOccur().name());
 				printQuery(bc.getQuery(), out, ind, cind + ind + ind);
 			}
 			out.println(cind + "]" + boost(q));
 		} else if (q instanceof TermQuery) {
 			TermQuery tq = (TermQuery) q;
 			out.println(cind + tq.getTerm() + boost(q));
 		} else if (q instanceof PrefixQuery) {
 			PrefixQuery pq = (PrefixQuery) q;
 			out.println(cind + "prefix " + pq.getPrefix() + boost(q));
 		} else if (q instanceof WildcardQuery) {
 			WildcardQuery wq = (WildcardQuery) q;
 			out.println(cind + "wildcard " + wq.getTerm() + boost(q));
 		} else if (q instanceof FuzzyQuery) {
 			FuzzyQuery fq = (FuzzyQuery) q;
 			out.println(String.format("%sfuzzy %s min_similarity=%.2f", cind, fq.getTerm(), fq.getMinSimilarity()) + boost(q));
 		} else if (q instanceof PhraseQuery) {
 			PhraseQuery pq = (PhraseQuery) q;
 			out.println(cind + "phrase " + Arrays.toString(pq.getTerms()) + (pq.getSlop() == 0 ? "" : " slop=" + pq.getSlop()) + boost(q));
 		} else {
 			out.println(cind + q.getClass() + " " + q + boost(q));
 		}
 		//out.println(q.getClass());
 	}
 
 	static String boost(Query q) {
 		return q.getBoost() == 1 ? "" : String.format(" boost=%.2f", q.getBoost());
 	}
 }
