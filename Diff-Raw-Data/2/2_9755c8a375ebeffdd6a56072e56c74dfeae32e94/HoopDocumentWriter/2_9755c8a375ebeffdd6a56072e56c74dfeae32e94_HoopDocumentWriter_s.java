 /** 
  * Author: Martin van Velsen <vvelsen@cs.cmu.edu>
  * 
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU Lesser General Public License as 
  *  published by the Free Software Foundation, either version 3 of the 
  *  License, or (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  * 
  */
 
 package edu.cmu.cs.in.hoop.hoops.save;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import org.apache.lucene.analysis.Analyzer;
 import org.apache.lucene.analysis.standard.StandardAnalyzer;
 import org.apache.lucene.document.Document;
 import org.apache.lucene.document.Field;
 import org.apache.lucene.document.LongField;
 import org.apache.lucene.document.StringField;
 import org.apache.lucene.document.TextField;
 import org.apache.lucene.index.IndexWriter;
 import org.apache.lucene.index.IndexWriterConfig;
 import org.apache.lucene.index.Term;
 import org.apache.lucene.index.IndexWriterConfig.OpenMode;
 import org.apache.lucene.store.Directory;
 import org.apache.lucene.store.FSDirectory;
 import org.apache.lucene.util.Version;
 
 import com.sleepycat.collections.StoredMap;
 
 import edu.cmu.cs.in.base.HoopDataType;
 import edu.cmu.cs.in.base.HoopLink;
 import edu.cmu.cs.in.base.HoopStringTools;
 import edu.cmu.cs.in.base.kv.HoopKV;
 import edu.cmu.cs.in.base.kv.HoopKVDocument;
 import edu.cmu.cs.in.base.kv.HoopKVLong;
 import edu.cmu.cs.in.hoop.hoops.base.HoopBase;
 import edu.cmu.cs.in.hoop.hoops.base.HoopSaveBase;
 import edu.cmu.cs.in.hoop.properties.types.HoopBooleanSerializable;
 import edu.cmu.cs.in.search.HoopDataSet;
 
 /**
 * 
 */
 public class HoopDocumentWriter extends HoopSaveBase
 {    				
 	private static final long serialVersionUID = -1691608095189030052L;
 	
 	private int checkCounter=0;
 	private int errorCounter=0;
 	
 	private StoredMap<Long,HoopKVLong> threadData=null;
 	
	// Lucense variables ...
 	
 	private IndexWriter writer=null;
 	private boolean create = false; // This will setup an append if the index is already available
 	private String searchStore="";
 	
 	private HoopBooleanSerializable indexDocuments=null;
 	
 	/**
 	 *
 	 */
     public HoopDocumentWriter () 
     {
 		setClassName ("HoopDocumentWriter");
 		debug ("HoopDocumentWriter ()");
 												
 		setHoopDescription ("Write Documents to Document DB");
 		
 		indexDocuments=new HoopBooleanSerializable (this,"indexDocuments",false);
     }
 	/**
 	 * 
 	 */
     public void reset ()
     {
     	debug ("reset ()");
     	
     	super.reset();
     	
     	checkCounter=0;
     	errorCounter=0;    	
     	
     	threadData=null;
     }	
 	/**
 	 *
 	 */
 	public Boolean runHoop (HoopBase inHoop)
 	{		
 		debug ("runHoop ()");
 		
 		ArrayList <HoopKV> inData=inHoop.getData();
 		
 		if (inData==null)
 		{
 			this.setErrorString ("Error: no input data to work with");
 			return (false);
 		}		
 		
 		debug ("Mapping project path ("+getProjectPath ()+") to db dir ...");
 				
 		if (HoopLink.dataSet==null)
 		{
 			HoopLink.dataSet=new HoopDataSet ();
 			HoopLink.dataSet.checkDB ();
 			
 			initSearch ();
 		}
 		else
 			HoopLink.dataSet.checkDB ();
 		
 		HoopKVLong threadCache=null;
 		
 		checkCounter=0;
 		
 		for (int t=0;t<inData.size();t++)
 		{			
 			HoopKV aKV=inData.get(t);
 									
 			if (aKV instanceof HoopKVDocument)
 			{			
 				HoopKVDocument newDocument=(HoopKVDocument) aKV;
 				
 				// This call will associate a timestamp with a document, but
 				// through an alternative table also use a unique ID to link
 				// to a document. But only if the documentID field is not blank
 				// and contains a long value.
 			
 				HoopLink.dataSet.writeKV (newDocument.getKey(),newDocument);
 				
 				addToSearchIndex (newDocument);
 				
 				threadCache=processThreadData (newDocument,threadCache);
 				
 				checkCounter++;
 			}			
 		}			
 		
 		debug ("Check: processed " + checkCounter + " documents, with " + errorCounter + " errors");
 		
 		if (getVisualizer ()!=null)
 		{
 			getVisualizer ().setExecutionInfo (" R: " + checkCounter + ", Err: " + errorCounter);
 		}	
 				
 		return (true);
 	}	
 	/**
 	 * 
 	 */
 	private HoopKVLong processThreadData (HoopKVDocument aDocument,HoopKVLong threadCache)
 	{
 		if (threadCache!=null)
 			debug ("processThreadData ("+aDocument.documentID.getValue()+","+aDocument.getKeyString()+","+threadCache.getKey()+")");
 		else
 			debug ("processThreadData ("+aDocument.documentID.getValue()+","+aDocument.getKeyString()+")");
 		
 		if (HoopLink.dataSet==null)
 		{
 			debug ("No dataset, can't process thread data");
 			return (null);
 		}
 		
 		if (threadData==null)
 		{
 			threadData=HoopLink.dataSet.getThreads();
 		}
 		
 		if (threadData==null)
 		{
 			debug ("Error: no thread database available, can't process thread data");
 			return (null);
 		}
 				
 		if (aDocument.threadID.getValue().isEmpty()==false)
 		{						
 			debug ("Procedding thread ID: " + aDocument.threadID.getValue() + " for document: " + aDocument.documentID.getValue());
 			
 			if (HoopStringTools.isLong (aDocument.threadID.getValue())==true)
 			{
 				debug ("Thread ID is of type Long");
 				
 				Long newThreadID=Long.parseLong(aDocument.threadID.getValue());
 								
 				HoopKVLong testThread=null;
 				
 				if (threadCache!=null)
 				{
 					debug ("We've got a potential thread cache hit, checking ("+threadCache.getKey()+","+newThreadID+")...");
 															
 					if (threadCache.getKey().equals(newThreadID)==true)
 					{
 						debug ("Thread cache hit: " + newThreadID);
 																		
 						threadCache.add(aDocument.getKeyString());
 												
 						return (threadCache);
 					}
 					
 					// We're switching over so we have to make sure that we write out all the
 					// updates we've put in the cached object
 					
 					threadData.put(threadCache.getKey(),threadCache);					
 				}
 								
 				testThread=threadData.get(newThreadID);
 				
 				//debug ("No cache hit, proceed as normal with thread id: " + testThread.getValue());
 				
 				if (testThread==null)
 				{
 					debug ("No thread entry found for ID:" + newThreadID + " for document "+aDocument.documentID.getValue()+", creating ...");
 										
 					testThread=new HoopKVLong ();
 					testThread.setKey(newThreadID);
 					testThread.setValue(aDocument.getKeyString());
 					threadData.put(newThreadID,testThread);
 															
 					return (testThread);
 					
 					//showThreadDB ();
 				}
 				else
 				{
 					debug ("Thread ID is already in our database, updating ...");
 					
 					debug ("Check >");
 					showThread (testThread);
 					debug ("Check <");
 					
 					if (aDocument.threadStarter.getValue().isEmpty()==false)
 					{
 						if (HoopDataType.booleanStringValue(aDocument.threadStarter.getValue())==true)
 						{
 							debug ("Potential conflict, thread ID already exists but document indicates it's a thread starter");
 							
 							errorCounter++;
 						}
 						else
 						{
 							debug ("Bumping thread ID ("+newThreadID+") with document: "+aDocument.createDate.getValue()+" ...");
 														
 							testThread.add(aDocument.getKeyString());
 							
 							threadData.put(newThreadID,testThread);
 							
 							//showThreadDB ();
 						}
 					}
 					else
 					{
 						debug ("Bumping thread ID ("+newThreadID+") with document: "+aDocument.createDate.getValue()+" ...");
 						
 						testThread.add(aDocument.getKeyString());
 						
 						threadData.put(newThreadID,testThread);
 						
 						return (testThread);
 					}
 				}								
 			}
 		}		
 		else
 			debug ("This document does not contain thread data");
 		
 		return (null);
 	}
 	/**
 	 * 
 	 */
 	@SuppressWarnings("unused")
 	private void showThreadDB ()
 	{
 		debug ("showThreadDB ()");
 
 		StoredMap<Long,HoopKVLong> threadData=HoopLink.dataSet.getThreads();
 		
 		if (threadData==null)
 		{
 			debug ("Error: no thread data available");
 			return;
 		}		
 		
 		StoredMap<Long,HoopKVDocument> map=HoopLink.dataSet.getData();
 		
 		if (map==null)
 		{
 			debug ("Error: no document data available");
 			return;
 		}		
 		   	
     	Integer totalShown=100;
     	
     	int dSize=threadData.size();
     	
     	if (dSize<totalShown)
     		totalShown=dSize;
     	
     	debug ("Thread data size: " + dSize + " adjusted: " + totalShown);
     	    	
 		Iterator<HoopKVLong> iterator = threadData.values().iterator();
 
 		int index=0;
 		int count=100;
 		
 		if (threadData.size()<100)
 			count=threadData.size ();
 		
 		debug ("Thread data size: " + dSize + " adjusted: " + count);
 												
 		while ((iterator.hasNext()) && (index<count)) 
 		{
 			HoopKVLong aThread=(HoopKVLong) iterator.next();
 								
 			showThread (aThread);
     			
 			index++;
 		}	    			
 	}
 	/**
 	 * 
 	 */
 	private void showThread (HoopKVLong aThread)
 	{
 		debug ("showThread ("+aThread.getKeyString()+","+aThread.getValueSize()+")");
 		
 		ArrayList <Object> docIDs=aThread.getValuesRaw();
 		
 		StringBuffer docList=new StringBuffer ();
 		
 		for (int i=0;i<docIDs.size();i++)
 		{
 			if (i>0)
 				docList.append (", ");
 			
 			docList.append(docIDs.get (i));
 		}
 		
 		debug ("Doc list: " + docList.toString());
 	}
 	/**
 	 * 
 	 */
 	private void initSearch ()
 	{
 		debug ("initSearch ()");
 		
 		if (indexDocuments.getPropValue()==false)
 		{
 			debug ("Search not enabled, skipping Lucene init");
 			return;
 		}
 		
 		if (searchStore.isEmpty()==true)
 		{
 			searchStore=getProjectPath ()+"/system/search";
 			
 			File checker=new File (searchStore);
 			
 			if (checker.exists()==false)
 			{
 				if (HoopLink.fManager.createDirectory (searchStore)==false)
 				{
 					debug ("Error creating search directory: " + searchStore);
 					return;
 				}
 			}
 			else
 				debug ("Document search directory exists, excellent");			
 		}
 		
 		debug ("Indexing to directory '" + searchStore + "'...");
 
 		if (writer==null)
 		{
 			Directory dir=null;
 			
 			try 
 			{
 				dir = FSDirectory.open(new File(searchStore));
 			} 
 			catch (IOException e) 
 			{
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 				return;
 			}
 			
 			Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_40);
 			IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_40, analyzer);
 
 			if (create) 
 			{
 				// Create a new index in the directory, removing any
 				// previously indexed documents:
 				iwc.setOpenMode (OpenMode.CREATE);
 			} 
 			else 
 			{
 				// Add new documents to an existing index:
 				iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);
 			}
 
 			// Optional: for better indexing performance, if you
 			// are indexing many documents, increase the RAM
 			// buffer.  But if you do this, increase the max heap
 			// size to the JVM (eg add -Xmx512m or -Xmx1g):
 			//
 			// iwc.setRAMBufferSizeMB(256.0);
 
 			try 
 			{
 				writer = new IndexWriter(dir, iwc);
 			} 
 			catch (IOException e) 
 			{
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 				writer=null; // Disable
 			}
 		}
 	}
 	/**
 	 * 
 	 */
 	private void addToSearchIndex (HoopKVDocument aDocument)
 	{
 		debug ("addToSearchIndex ()");
 		
 		if (indexDocuments.getPropValue()==false)
 		{
 			debug ("Search not enabled, skipping Lucene indexing");
 			return;
 		}
 		
 		// make a new, empty document
 		Document doc = new Document();
 
 		// Add the path of the file as a field named "path".  Use a
 		// field that is indexed (i.e. searchable), but don't tokenize 
 		// the field into separate words and don't index term frequency
 		// or positional information:
 		  
 		//Field pathField = new StringField("path", file.getPath(), Field.Store.YES);
 		Field pathField = new StringField("path", aDocument.url.getValue(), Field.Store.YES);
 		doc.add(pathField);
 
 		// Add the last modified date of the file a field named "modified".
 		// Use a LongField that is indexed (i.e. efficiently filterable with
 		// NumericRangeFilter).  This indexes to milli-second resolution, which
 		// is often too fine.  You could instead create a number based on
 		// year/month/day/hour/minutes/seconds, down the resolution you require.
 		// For example the long value 2011021714 would mean
 		// February 17, 2011, 2-3 PM.
 		  
 		//doc.add(new LongField("modified", file.lastModified(), Field.Store.NO));
 		doc.add(new LongField("modified", aDocument.modifiedDate.getValueSize(), Field.Store.NO));
 
 		// Add the contents of the file to a field named "contents".  Specify a Reader,
 		// so that the text of the file is tokenized and indexed, but not stored.
 		// Note that FileReader expects the file to be in UTF-8 encoding.
 		// If that's not the case searching for special characters will fail.
 		  
 		doc.add(new TextField("contents",aDocument.text.getValue(),Field.Store.NO));
 
 		if (writer.getConfig().getOpenMode() == OpenMode.CREATE) 
 		{
 			// New index, so we just add the document (no old document can be there):
 			// System.out.println("adding " + file);
 			debug ("Adding " + aDocument.url.getValue() + " to search index");
 			  
 			try 
 			{
 				writer.addDocument(doc);
 			} 
 			catch (IOException e) 
 			{
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		} 
 		else 
 		{
 			// Existing index (an old copy of this document may have been indexed) so 
 			// we use updateDocument instead to replace the old one matching the exact 
 			// path, if present:
 			//System.out.println("updating " + file);
 			debug ("Updating " + aDocument.url.getValue() + " in search index");
 			try 
 			{
 				writer.updateDocument(new Term("path", aDocument.url.getValue()), doc);
 			} 
 			catch (IOException e) 
 			{
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}          		
 	}
 	/**
 	 * 
 	 */
 	public HoopBase copy ()
 	{
 		return (new HoopDocumentWriter ());
 	}		
 }
