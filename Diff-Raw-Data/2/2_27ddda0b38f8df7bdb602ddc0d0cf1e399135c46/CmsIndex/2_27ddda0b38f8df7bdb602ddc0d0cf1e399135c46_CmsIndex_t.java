 package org.alt60m.cms.servlet;
 
 import org.alt60m.util.ObjectHashUtil;
 
 import java.io.IOException;
 import java.util.*;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.lucene.index.*;
 import org.apache.lucene.analysis.*;
 import org.apache.lucene.document.*;
 import org.apache.lucene.queryParser.*;
 import org.apache.lucene.search.*;
 
 public class CmsIndex {
 
 	static String searchIndexPath;
 	static String fileSpecsPath;
 
 	private static Log log = LogFactory.getLog(CmsIndex.class);
 	
 	public CmsIndex() {}
 	public static void SetIndexPath(String searchIndexPathIn) {
 		searchIndexPath = searchIndexPathIn;
 	}
 	public static void setFileSpecsPath(String fileSpecsPath) {
 		CmsIndex.fileSpecsPath = fileSpecsPath;
 	}
 
 	public void populate() {
 		try {
 			IndexWriter writer = new IndexWriter(searchIndexPath, new SimpleAnalyzer(), true);
 			Hashtable fileSpecs = CmsFileSpecsProcessor.parse(fileSpecsPath);
 
 			// get all the files in the cms
 			org.alt60m.cms.model.File a = new org.alt60m.cms.model.File();
 			Vector fileSet = (Vector)ObjectHashUtil.list(a.selectList("1=1 order by CmsFileID"));
 
 			for (int i=0;i<fileSet.size();i++) {
 				Hashtable f = (Hashtable)fileSet.get(i);
 				log.debug("Indexing file " + f.get("FileId"));
 				add(f,writer,fileSpecs);
 			}
 			log.debug("Optimizing");
 			writer.optimize();
 			writer.close();
 		} catch (Exception e) {
 			log.error(e, e);
 		}
 	}
 
 	public static void add(Hashtable d) {
 		try {
 			IndexWriter writer;
 			writer = new IndexWriter(searchIndexPath, new SimpleAnalyzer(), false);
 			Hashtable fileSpecs = CmsFileSpecsProcessor.parse(fileSpecsPath);
 			add(d,writer,fileSpecs);
 			writer.close();
 		} catch (IOException ioe) {
 			log.error("Error while adding to search index!", ioe);
 		}
 	}
 
 	public static void add(Hashtable d, Hashtable fileSpecs) {
 		try {
 			IndexWriter writer;
 			writer = new IndexWriter(searchIndexPath, new SimpleAnalyzer(), false);
 			add(d,writer,fileSpecs);
 			writer.close();
 		} catch (IOException ioe) {
 			log.error("Error while adding to search index!", ioe);
 		}
 	}
 
 	public static void add(Hashtable d, IndexWriter writer, Hashtable fileSpecs) {
 		try {
 			String id = (String)d.get("FileId");
 			Document doc = new Document();
 			doc.add(Field.Keyword("FileId",id));
 			doc.add(Field.Text("title",(String)d.get("Title")));
 			doc.add(Field.Text("author",(String)d.get("Author")));
 			doc.add(Field.Text("submitter",(String)d.get("Submitter")));
 			doc.add(Field.Text("contact",(String)d.get("Contact")));
 			doc.add(Field.Text("keywords",(String)d.get("Keywords")));
 			doc.add(Field.Text("summary",(String)d.get("Summary")));
 			doc.add(Field.Keyword("language",(String)d.get("Language")));
 			try	{doc.add(Field.Keyword("dateAdded",DateField.dateToString((Date)d.get("DateAdded"))));}
 			catch (NullPointerException ne)	{doc.add(Field.Keyword("dateAdded",""));}
 			doc.add(Field.Keyword("quality",(String)d.get("Quality")));
 			doc.add(Field.Keyword("mime",(String)d.get("Mime")));
 			doc.add(Field.Keyword("url",(String)d.get("Url")));
 			doc.add(Field.Text("all",""+d.get("Title")+" "+d.get("Author")+" "+d.get("Submitter")+" "+d.get("Contact")+" "+d.get("Keywords")+" "+d.get("Summary")));
 
 			// add type of doc based on filespec type
 			String url = (String)d.get("Url");
 			try	{
 				String ext = url.toLowerCase().substring(url.lastIndexOf("."));
 				if ((d.get("submitType")!=null)&&(d.get("submitType").equals("web"))) { ext = ".html"; }
 				if (((String)d.get("Url")).startsWith("http://")) { ext = ".html"; }
 				Hashtable fileSpec = (Hashtable)fileSpecs.get(ext);
 				if (fileSpec != null) {
 					doc.add(Field.Keyword("type",(String)fileSpec.get("Group")));
 				} else {
 					log.warn("File '" + d.get("Title") + "' with extension '" + ext + "' has no group");
 					doc.add(Field.Keyword("type","none"));
 				}
 			}
 			catch (StringIndexOutOfBoundsException sioobe)
 			{
 				doc.add(Field.Keyword("type","none"));
 			}
 
 			writer.addDocument(doc);
 		} catch (IOException ioe) {
 			log.error("Error while adding to search index!", ioe);
 		}
 	}
 
 	public static void update(Hashtable d, Hashtable fileSpecs) {
 		String url = (String)d.get("Url");
 		CmsIndex.remove(url);
 		CmsIndex.add(d, fileSpecs);
 	}
 
 	public static void remove(String id) {
 		try {
 			IndexReader reader = IndexReader.open(searchIndexPath);
 			//this should work but there is a bug in Lucene with using an int as a value for the term
 			//Term term = new Term("cmsFileId", id);
 			Term term = new Term("url", id);
 			reader.delete(term);
 			reader.close();
 		} catch (IOException ioe) {
 			log.error("Error while deleting from search index", ioe);
 		}
 	}
 
 	public static Hashtable search(String queryString) {
 		Hashtable h = new Hashtable();
 		try {
 			Query query = QueryParser.parse(queryString, "all", new StopAnalyzer());
 			h = CmsIndex.search(query);
 		} catch (ParseException pe) {
			log.warn(pe);
 		}
 		return h;
 
 	}
 
 	public static Hashtable search(Query query) {
 		Hashtable results = new Hashtable();
 		try {
 			Searcher searcher = new IndexSearcher(searchIndexPath);
 			new StopAnalyzer();         //Is this being used?
 
 			//Query query1 = QueryParser.parse(queryString+" +quality:1", "all", analyzer);
 
 			log.info("Searching for: " + query.toString("all"));
 
 			Hits hits = searcher.search(query);
 			log.info(hits.length() + " total matching document(s)");
 
 			for (int i=0; i < hits.length(); i++) {
 				Hashtable hit = new Hashtable();
 				hit.put("FileId",hits.doc(i).get("FileId"));
 				hit.put("Title",hits.doc(i).get("title"));
 				hit.put("Author",hits.doc(i).get("author"));
 				hit.put("Submitter",hits.doc(i).get("submitter"));
 				hit.put("Contact",hits.doc(i).get("contact"));
 				hit.put("Summary",hits.doc(i).get("summary"));
 				hit.put("Language",hits.doc(i).get("language"));
 				hit.put("DateAdded",hits.doc(i).get("dateAdded"));
 				hit.put("Quality",hits.doc(i).get("quality"));
 				hit.put("Mime",hits.doc(i).get("mime"));
 				hit.put("Url",hits.doc(i).get("url"));
 				hit.put("Score",Float.toString(hits.score(i)*100));
 				results.put(Integer.toString(i),hit);
 			}
 			searcher.close();
 
 		} catch (Exception e) {
 			log.error(" caught a " + e.getClass() +
 				 "\n with message: " + e.getMessage());
 		}
 		return results;
 	}
 
 //    javac -d G:\ade4\classes G:\ade4\controlled-src\services-src\source\org\alt60m\cms\servlet\CmsIndex.java
 	public static void main(String[] args) {
 		CmsIndex ci = new CmsIndex();
 		log.info("Index creation Started");
 		ci.populate();
 
 		/*
 		Hashtable results = ci.search(args[0]);
 		for (int i=0;i<results.size();i++) {
 			Hashtable result = (Hashtable)results.get(new Integer(i).toString());
 			log.info((i+1) + ". " + result.get("Title") + " (" + result.get("Score") + "%)");
 		}
 		*/
 	}
 
 }
