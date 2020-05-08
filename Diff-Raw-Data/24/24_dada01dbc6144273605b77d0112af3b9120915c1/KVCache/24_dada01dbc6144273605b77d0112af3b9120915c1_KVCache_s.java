 /**
  * Implementation of a set-associative cache.
  * 
  * @author Mosharaf Chowdhury (http://www.mosharaf.com)
  * @author Prashanth Mohan (http://www.cs.berkeley.edu/~prmohan)
  * 
  * Copyright (c) 2012, University of California at Berkeley
  * All rights reserved.
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *  * Redistributions of source code must retain the above copyright
  *    notice, this list of conditions and the following disclaimer.
  *  * Redistributions in binary form must reproduce the above copyright
  *    notice, this list of conditions and the following disclaimer in the
  *    documentation and/or other materials provided with the distribution.
  *  * Neither the name of University of California, Berkeley nor the
  *    names of its contributors may be used to endorse or promote products
  *    derived from this software without specific prior written permission.
  *    
  *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
  *  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  *  DISCLAIMED. IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY
  *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 //package edu.berkeley.cs162;
 package edu.berkeley.cs162;
 
 import java.io.StringWriter;
 import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.soap.Node;
 import javax.xml.stream.XMLStreamWriter;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Text;
 
 
 /**
  * A set-associate cache which has a fixed maximum number of sets (numSets).
  * Each set has a maximum number of elements (MAX_ELEMS_PER_SET).
  * If a set is full and another entry is added, an entry is dropped based on the eviction policy.
  */
 public class KVCache implements KeyValueInterface {	
 	private int numSets = 100;
 	private int maxElemsPerSet = 10;
 	private KVCacheSet[] sets;
 
 	/**
 	 * Creates a new LRU cache.
 	 * @param cacheSize	the maximum number of entries that will be kept in this cache.
 	 */
 	public KVCache(int numSets, int maxElemsPerSet) {
 		this.numSets = numSets;
 		this.maxElemsPerSet = maxElemsPerSet;     
 		// TODO: Implement Me!
 		this.sets = new KVCacheSet[numSets];
 		for(int i = 0; i<numSets; i++){
 			sets[i] = new KVCacheSet(this.maxElemsPerSet);
 		}
 	}
 
 	/**
 	 * Retrieves an entry from the cache.
 	 * Assumes the corresponding set has already been locked for writing.
 	 * @param key the key whose associated value is to be returned.
 	 * @return the value associated to this key, or null if no value with this key exists in the cache.
 	 */
 	public String get(String key) {
 		// Must be called before anything else
 		AutoGrader.agCacheGetStarted(key);
 		AutoGrader.agCacheGetDelay();
         
 		// TODO: Implement Me!
 		String getResult = sets[getSetId(key)].get(key);
 		// Must be called before returning
 		AutoGrader.agCacheGetFinished(key);
 		return getResult;
 	}
 
 	/**
 	 * Adds an entry to this cache.
 	 * If an entry with the specified key already exists in the cache, it is replaced by the new entry.
 	 * If the cache is full, an entry is removed from the cache based on the eviction policy
 	 * Assumes the corresponding set has already been locked for writing.
 	 * @param key	the key with which the specified value is to be associated.
 	 * @param value	a value to be associated with the specified key.
 	 * @return true is something has been overwritten 
 	 */
 	public void put(String key, String value) {
 		// Must be called before anything else
 		AutoGrader.agCachePutStarted(key, value);
 		AutoGrader.agCachePutDelay();
 
 		// TODO: Implement Me!
 		boolean putResult = sets[getSetId(key)].put(key,value);
 		// Must be called before returning
 		AutoGrader.agCachePutFinished(key, value);
 	}
 
 	/**
 	 * Removes an entry from this cache.
 	 * Assumes the corresponding set has already been locked for writing.
 	 * @param key	the key with which the specified value is to be associated.
 	 */
 	public void del (String key) {
 		// Must be called before anything else
 		AutoGrader.agCacheGetStarted(key);
 		AutoGrader.agCacheDelDelay();
 		
 		// TODO: Implement Me!
 		sets[getSetId(key)].del(key);
 		// Must be called before returning
 		AutoGrader.agCacheDelFinished(key);
 	}
 	
 	/**
 	 * @param key
 	 * @return	the write lock of the set that contains key.
 	 */
 	public WriteLock getWriteLock(String key) {
 		return sets[getSetId(key)].getWriteLock();
 	}
 	
 	/**
 	 * 
 	 * @param key
 	 * @return	set of the key
 	 */
 	private int getSetId(String key) {
 		return Math.abs(key.hashCode()) % numSets;
 	}
 	
 	public String toXML() throws KVException {
 		try { 
 			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
 			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
 			Document doc = docBuilder.newDocument();
 			Element rElem, sElem, pElem, kElem, vElem;
 			Text kValue;
 			Text vValue;
 			rElem = doc.createElement("KVCache");
 			doc.appendChild(rElem);
 			for (int i =0; i < sets.length; i++) {
 				KVCacheSet set = sets[i];
 				sElem = doc.createElement("Set");
 				sElem.setAttribute("Id", String.valueOf(i)); //we need the String of key for set id
 				for (String key: set.entries.keySet()) {
 					pElem = doc.createElement("KVPair");
 					//Key
 		            kValue = doc.createTextNode(key);
 		            kElem = doc.createElement("Key");
 		            kElem.appendChild(kValue);
 		            pElem.appendChild(kElem);
 		            //Value
 		            vValue = doc.createTextNode(set.entries.get(key).getData());
 		            vElem = doc.createElement("Value");
 		            vElem.appendChild(vValue);
 					pElem.appendChild(vElem);
 					sElem.appendChild(pElem);
 				}
 				//Append pair to root
	            rElem.appendChild(sElem);	
 			}
 			DOMSource domSource = new DOMSource(doc);
 	        StringWriter stringWriter = new StringWriter();
 	        StreamResult streamResult = new StreamResult(stringWriter);
 	        TransformerFactory transformerFactory = TransformerFactory.newInstance();
 	        Transformer transformer = transformerFactory.newTransformer();
 	        transformer.transform(domSource, streamResult);
 	        return stringWriter.toString();
 		}
 	    catch (Exception e) {
 	    	throw new KVException(new KVMessage("resp", "Error during KVCache toXML"));
 	    }  
 	}
 }
