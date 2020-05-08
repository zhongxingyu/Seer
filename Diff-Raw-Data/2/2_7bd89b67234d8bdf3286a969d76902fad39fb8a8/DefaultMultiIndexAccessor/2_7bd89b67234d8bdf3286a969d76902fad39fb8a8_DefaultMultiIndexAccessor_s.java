 package com.gentics.cr.lucene.indexaccessor;
 
 /**
  * Licensed to the Apache Software Foundation (ASF) under one or more
  * contributor license agreements.  See the NOTICE file distributed with
  * this work for additional information regarding copyright ownership.
  * The ASF licenses this file to You under the Apache License, Version 2.0
  * (the "License"); you may not use this file except in compliance with
  * the License.  You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.apache.lucene.index.IndexReader;
 import org.apache.lucene.index.IndexWriter;
 import org.apache.lucene.index.MultiReader;
 import org.apache.lucene.search.MultiSearcher;
 import org.apache.lucene.search.Searchable;
 import org.apache.lucene.search.Searcher;
 import org.apache.lucene.search.Similarity;
 import org.apache.lucene.store.Directory;
 
 /**
  * Default MultiIndexAccessor implementation.
  * 
  * Last changed: $Date: 2009-09-02 17:57:48 +0200 (Mi, 02 Sep 2009) $
  * @version $Revision: 180 $
  * @author $Author: supnig@constantinopel.at $
  *
  */
 public class DefaultMultiIndexAccessor implements IndexAccessor {
 
   private final Map<Searcher, IndexAccessor> multiSearcherAccessors = new HashMap<Searcher, IndexAccessor>();
   private final Map<IndexReader, IndexAccessor> multiReaderAccessors = new HashMap<IndexReader, IndexAccessor>();
 
   private Similarity similarity;
   
   private Directory[] dirs;
 
   /**
    * Create new Instance
  * @param dirs 
    */
   public DefaultMultiIndexAccessor(Directory[] dirs) {
     this.similarity = Similarity.getDefault();
     this.dirs = dirs;
   }
 
   /**
    * Create new instance
  * @param dirs 
    * @param similarity
    */
   public DefaultMultiIndexAccessor(Directory[] dirs,Similarity similarity) {
     this.similarity = similarity;
     this.dirs = dirs;
   }
 
 
   
   /*
    * (non-Javadoc)
    * 
    * @see com.mhs.indexaccessor.MultiIndexAccessor#release(org.apache.lucene.search.Searcher)
    */
   public synchronized void release(Searcher multiSearcher) {
     Searchable[] searchers = ((MultiSearcher) multiSearcher).getSearchables();
     for (Searchable searchable : searchers) {
       multiSearcherAccessors.remove(searchable).release((Searcher) searchable);
     }
   }
 
   	/**
   	 * Closes all index accessors contained in the multi accessor
   	 */
 	public void close() {
 		for (Entry<Searcher,IndexAccessor> iae : this.multiSearcherAccessors.entrySet()) {
 			IndexAccessor ia = iae.getValue();
 			if(ia.isOpen())
 				ia.close();
 	    }
 		for (Entry<IndexReader,IndexAccessor> iae : this.multiReaderAccessors.entrySet()) {
 			IndexAccessor ia = iae.getValue();
 			if(ia.isOpen())
 				ia.close();
 	    }
 	}
 	
 	public Searcher getPrioritizedSearcher() throws IOException {
 		
 		    Searcher[] searchers = new Searcher[this.dirs.length];
 		    
 		    IndexAccessorFactory factory = IndexAccessorFactory.getInstance();
 		    int i=0;
 		    for (Directory index:this.dirs) {
 		      IndexAccessor indexAccessor = factory.getAccessor(index);
 		      searchers[i] = indexAccessor.getPrioritizedSearcher();
 		      multiSearcherAccessors.put(searchers[i], indexAccessor);
 		      i++;
 		    }
 
 		    MultiSearcher multiSearcher = new MultiSearcher(searchers);
 
 		    return multiSearcher;
 	}
 	
 	public IndexReader getReader(boolean write) throws IOException {
 		if(write)throw new UnsupportedOperationException(); 
 		
 		IndexReader[] readers = new IndexReader[this.dirs.length];
 		
 		IndexAccessorFactory factory = IndexAccessorFactory.getInstance();
 	    int i=0;
 	    for (Directory index:this.dirs) {
 	      IndexAccessor indexAccessor = factory.getAccessor(index);
 	      readers[i] = indexAccessor.getReader(false);
 	      multiReaderAccessors.put(readers[i], indexAccessor);
 	      i++;
 	    }
 		
 		MultiReader multiReader = new MultiReader(readers,true);
 		
 		return multiReader;
 	}
 	
 	public Searcher getSearcher() throws IOException {
 		 	Searcher[] searchers = new Searcher[this.dirs.length];
 		    
 		    IndexAccessorFactory factory = IndexAccessorFactory.getInstance();
 		    int i=0;
 		    for (Directory index:this.dirs) {
 		      IndexAccessor indexAccessor = factory.getAccessor(index);
 		      searchers[i] = indexAccessor.getSearcher(this.similarity,null);
 		      multiSearcherAccessors.put(searchers[i], indexAccessor);
 		      i++;
 		    }
 
 		    MultiSearcher multiSearcher = new MultiSearcher(searchers);
 
 		    return multiSearcher;
 	}
 	
 	public Searcher getSearcher(IndexReader indexReader) throws IOException {
 		Searcher[] searchers = new Searcher[this.dirs.length];
 	    
 	    IndexAccessorFactory factory = IndexAccessorFactory.getInstance();
 	    int i=0;
 	    for (Directory index:this.dirs) {
 	      IndexAccessor indexAccessor = factory.getAccessor(index);
 	      searchers[i] = indexAccessor.getSearcher(this.similarity, indexReader);
 	      multiSearcherAccessors.put(searchers[i], indexAccessor);
 	      i++;
 	    }
 
 	    MultiSearcher multiSearcher = new MultiSearcher(searchers);
 
 	    return multiSearcher;
 	}
 	
 	public Searcher getSearcher(Similarity similarity, IndexReader indexReader)throws IOException {
 		Searcher[] searchers = new Searcher[this.dirs.length];
 	    
 	    IndexAccessorFactory factory = IndexAccessorFactory.getInstance();
 	    int i=0;
 	    for (Directory index:this.dirs) {
 	      IndexAccessor indexAccessor = factory.getAccessor(index);
 	      searchers[i] = indexAccessor.getSearcher(similarity, indexReader);
 	      multiSearcherAccessors.put(searchers[i], indexAccessor);
 	      i++;
 	    }
 
 	    MultiSearcher multiSearcher = new MultiSearcher(searchers);
 
 	    return multiSearcher;
 	}
 	
 	public IndexWriter getWriter() throws IOException {
 		throw new UnsupportedOperationException();
 	}
 	
 	public boolean isOpen() {
 		boolean open = true;
 		IndexAccessorFactory factory = IndexAccessorFactory.getInstance();
 		for (Directory index:this.dirs) {
 	      IndexAccessor indexAccessor = factory.getAccessor(index);
 	      if(!indexAccessor.isOpen())
 	      {
 	    	  open = false;
 	      }
 	    }
 		return open;
 	}
 	
 	public void open() {
 		IndexAccessorFactory factory = IndexAccessorFactory.getInstance();
 		for (Directory index:this.dirs) {
 	      IndexAccessor indexAccessor = factory.getAccessor(index);
 	      indexAccessor.open();
 	    }
 	}
 	
 	public int readingReadersOut() {
 		int usecount = 0;
 		IndexAccessorFactory factory = IndexAccessorFactory.getInstance();
 		for (Directory index:this.dirs) {
 	      IndexAccessor indexAccessor = factory.getAccessor(index);
 	      usecount += indexAccessor.readingReadersOut();
 	    }
 		return usecount;
 	}
 	
 	public void release(IndexReader reader, boolean write) {
 		IndexReader[] readers = ((MultiReader) reader).getSequentialSubReaders();
 	    for (IndexReader r : readers) {
	      multiSearcherAccessors.remove(r).release(r,write);
 	    }
 	}
 	
 	public void release(IndexWriter writer) {
 		throw new UnsupportedOperationException();		
 	}
 	
 	public int searcherUseCount() {
 		int usecount = 0;
 		IndexAccessorFactory factory = IndexAccessorFactory.getInstance();
 		for (Directory index:this.dirs) {
 	      IndexAccessor indexAccessor = factory.getAccessor(index);
 	      usecount += indexAccessor.searcherUseCount();
 	    }
 		return usecount;
 	}
 	
 	public int writerUseCount() {
 		int usecount = 0;
 		IndexAccessorFactory factory = IndexAccessorFactory.getInstance();
 		for (Directory index:this.dirs) {
 	      IndexAccessor indexAccessor = factory.getAccessor(index);
 	      usecount += indexAccessor.writerUseCount();
 	    }
 		return usecount;
 	}
 	
 	public int writingReadersUseCount() {
 		int usecount = 0;
 		IndexAccessorFactory factory = IndexAccessorFactory.getInstance();
 		for (Directory index:this.dirs) {
 	      IndexAccessor indexAccessor = factory.getAccessor(index);
 	      usecount += indexAccessor.writingReadersUseCount();
 	    }
 		return usecount;
 	}
 
 	public void reopen() throws IOException {
 		IndexAccessorFactory factory = IndexAccessorFactory.getInstance();
 		for (Directory index:this.dirs) {
 		      IndexAccessor indexAccessor = factory.getAccessor(index);
 		      indexAccessor.reopen();
 		}
 	}
 
 }
