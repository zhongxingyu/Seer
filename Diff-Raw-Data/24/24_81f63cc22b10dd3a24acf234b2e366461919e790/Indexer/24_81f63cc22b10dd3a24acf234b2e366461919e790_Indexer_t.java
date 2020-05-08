 package com.indexer.model;
 
 import org.apache.commons.io.output.ByteArrayOutputStream;
 import org.apache.log4j.Logger;
 import org.apache.lucene.index.CorruptIndexException;
 import org.apache.lucene.index.IndexWriter;
 import org.apache.lucene.index.IndexWriterConfig;
 import org.apache.lucene.index.Term;
 import org.apache.lucene.store.Directory;
 import org.apache.lucene.store.FSDirectory;
 import org.apache.lucene.util.Version;
 import org.apache.lucene.analysis.standard.StandardAnalyzer;
 import org.apache.lucene.document.Document;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import com.database.connector.Database;
 import com.mongodb.BasicDBObject;
 import com.mongodb.DBCollection;
 import com.mongodb.DBCursor;
 import com.mongodb.DBObject;
 import com.mongodb.WriteResult;
 import javax.xml.transform.OutputKeys;
 import javax.xml.transform.TransformerConfigurationException;
 import javax.xml.transform.sax.SAXTransformerFactory;
 import javax.xml.transform.sax.TransformerHandler;
 import javax.xml.transform.stream.StreamResult;
 import org.apache.tika.metadata.Metadata;
 import org.apache.tika.parser.AutoDetectParser;
 import org.apache.tika.parser.ParseContext;
 import org.apache.tika.parser.Parser;
 import org.xml.sax.ContentHandler;
 import org.apache.lucene.document.Field;
 import org.apache.lucene.document.Field.Index;
 import org.apache.lucene.document.Field.Store;
 import org.apache.lucene.document.Field.TermVector;
 import org.bson.types.ObjectId;
 
 
 public class Indexer {
 	static Logger logger = Logger.getLogger(Indexer.class);
 	/*
 	public static void main(String[] args) throws Exception {
 		BasicConfigurator.configure();
 		String indexDir = "/home/irmak/hede2/";
 		String dataDir = "/home/irmak/hede/";//"D:\\Philosophy\\Cambridge Companions";
 		logger.info("Entering application.");
 		long start = System.currentTimeMillis();
 		Indexer indexer = new Indexer(indexDir);
 		int numIndexed;
 		try {
 			numIndexed = indexer.index(dataDir);
 		} finally {
 			indexer.close();
 		}
 		long end = System.currentTimeMillis();
 		System.out.println("Indexing " + numIndexed + " files took "
 				+ (end - start) + " milliseconds");
 		logger.info("Exiting application.");
 	}
 	*/
 	private IndexWriter writer;
 	
 	public Indexer(String indexDir) throws IOException {
 		
 		Directory dir = FSDirectory.open(new File(indexDir));
 		writer = new IndexWriter(dir, new IndexWriterConfig(Version.LUCENE_34, new StandardAnalyzer(Version.LUCENE_34)));
 	}
 
 	public void close() throws IOException {
 		writer.close();
 	}
 
 	public int index(File f, String fileId, String uid) throws Exception {
 		//File[] files = new File(dataDir).listFiles();
 		//for (File f : files) {
 			if (!f.isDirectory() && !f.isHidden() && f.exists() && f.canRead()) {
 				indexFile(f, fileId, uid);
 			}
 		//}
 		return writer.numDocs();
 	}
 	
 	public boolean delete(String bookId) throws CorruptIndexException, IOException{
 		writer.deleteDocuments(new Term("bookID", bookId));
 		writer.commit();
 		
 		return writer.hasDeletions();
 	}
 
 	private static TransformerHandler getTransformerHandler(OutputStream output, String method, String encoding, boolean prettyPrint) throws TransformerConfigurationException {
         SAXTransformerFactory factory = (SAXTransformerFactory) SAXTransformerFactory.newInstance();
         TransformerHandler handler = factory.newTransformerHandler();
         handler.getTransformer().setOutputProperty(OutputKeys.METHOD, method);
         handler.getTransformer().setOutputProperty(OutputKeys.INDENT, prettyPrint ? "yes" : "no");
         if (encoding != null) {
             handler.getTransformer().setOutputProperty(
                     OutputKeys.ENCODING, encoding);
         }
         handler.setResult(new StreamResult(output));
         return handler;
     }
 	
 	private Document indexPage(String page, int pageNum, String bookName, String bookID, String authorName) throws Exception {
		//System.out.println("Indexing page "+pageNum);
 		Document doc = new Document();
 		doc.add(new Field("page", page, Store.YES, Index.ANALYZED, TermVector.WITH_POSITIONS_OFFSETS));
 		doc.add(new Field("pageNum", Integer.toString(pageNum), Field.Store.YES, Field.Index.NOT_ANALYZED));
 		doc.add(new Field("bookName", bookName, Field.Store.YES, Field.Index.NOT_ANALYZED));
 		doc.add(new Field("bookID", bookID, Field.Store.YES, Field.Index.NOT_ANALYZED));
 		doc.add(new Field("authorName", authorName, Field.Store.YES, Field.Index.NOT_ANALYZED));
 		return doc;
 	}
 	
 	public void indexFile(File f, String fileId, String uid) throws Exception {
		//System.out.println("Indexing " + f.getCanonicalPath());
 		
 		InputStream in = new FileInputStream(f);
 		
 		OutputStream out = new ByteArrayOutputStream();
 		ContentHandler handler = getTransformerHandler(out, "html", "UTF-8", true);
 		Metadata metadata = new Metadata();
 		Parser parser = new AutoDetectParser();
 		ParseContext context = new ParseContext();
 		parser.parse(in, handler, metadata, context);
 		String hede = out.toString();
 		hede = hede.substring(hede.indexOf("<body>")+("<body>").length(),hede.indexOf("</body>"));
 		
 		String[] pages = hede.split("<div class=\"page\">");
 		
 		String author = metadata.get("Author");
 		if(author == null)
 			author = "Not Available";
 		String title = metadata.get("title");
 		if(title == null)
 			title = f.getName();
 		
 		
 		Database.getInstance().connect();	//get DB connection singleton
 		String bookID = fileId;
 		DBCollection pageColl = Database.getInstance().getCollection("pages");
 
 		int i = 0;
 		for(String rawPageContent:pages){
 			
 			List<String> content = new ArrayList<String>();						//create the list which will contains paragraphs
 			
 			int indexOfDiv = rawPageContent.indexOf("</div>");					//<div class="page"> was cleaned, now closing tag is found if there is.
 			
 			if(indexOfDiv != -1)
 				rawPageContent = rawPageContent.substring(0, indexOfDiv);		//if there is a closing div, clean it
 			
 			String[] paragraphs = rawPageContent.split("<p>");					//split the page by paragraphs
 			
 			for(String parag:paragraphs){
 				int indexOfP = parag.indexOf("</p>");
 				if(indexOfP != -1)
 					parag = parag.substring(0,indexOfP);						//clean the closing </p> tag
 				content.add(parag);												//add the cleaned paragraph content to content list
 			}
 			
 			DBObject pageObj = new BasicDBObject();
 			pageObj.put("author", author);
 			pageObj.put("title", title);
 			pageObj.put("content", content);
 			pageObj.put("bookId", bookID);
 			pageObj.put("number", i++);
 			pageColl.insert(pageObj);											//insert page to db
 		}
 		
 		BasicDBObject query = new BasicDBObject();
 		query.put("bookId", bookID);
 		DBCursor cursor = pageColl.find(query).sort(new BasicDBObject("number",1));			//sort by page number
 		
 		while(cursor.hasNext()) {
 			DBObject p = cursor.next();
 			@SuppressWarnings("unchecked")
 			List<String> content = (List<String>) p.get("content");
 			Iterator<String> it = content.iterator();
 			String page = "";
 			while(it.hasNext()) page += " "+it.next();
 			
 			
 			String bookName = (String)p.get("title");
 			if(bookName == null) {
 				String filename = f.getName();
 				int indexOfPathExtension = filename.lastIndexOf('.');  
 				if (indexOfPathExtension > 0 && indexOfPathExtension <= filename.length() - 2 )
 					bookName = filename.substring(0, indexOfPathExtension);
 				else
 					bookName = "Not applicable";
 			}
 			String authorName = (String)p.get("author");
 			if(authorName == null)
 				authorName = "Not applicable";
 			Document doc = indexPage(page, (int)p.get("number"), bookName, (String)p.get("bookId"), authorName);
 			writer.addDocument(doc);
 			
 		}
 		writer.commit();
 		BasicDBObject userObject = new BasicDBObject();
 		userObject.put("_id", new ObjectId(uid));
 		BasicDBObject booksArray = new BasicDBObject();
 		BasicDBObject singleBook = new BasicDBObject("id",fileId.toString());
 		singleBook.put("author", author);
 		singleBook.put("title", title);
 		booksArray.put("$push", new BasicDBObject("books",singleBook));
 	    DBCollection userColl = Database.getInstance().getCollection("user");
 	    WriteResult result = userColl.update(userObject, booksArray, true, true);
	    System.out.println("Indexer db error: "+result.getLastError());
 		
 		in.close();
 		out.flush();
 		out.close();
 
 	}
 }
