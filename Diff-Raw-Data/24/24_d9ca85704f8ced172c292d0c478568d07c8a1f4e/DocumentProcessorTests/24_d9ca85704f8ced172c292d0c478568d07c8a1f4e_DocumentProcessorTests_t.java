 package com.exalead.codesearch.indexing.analysis;
 
 import junit.framework.TestCase;
 
 import com.exalead.pdoc.ProcessableDocument;
 import com.exalead.pdoc.kv.KVPDoc;
 
public abstract class DocumentProcessorTests extends TestCase {
 
     protected ProcessableDocument buildDocument(String url, String text, String ... metas) {
         ProcessableDocument pdoc = new KVPDoc();
         pdoc.addMeta("url", url);
         pdoc.addMeta("text", text);
         if (metas.length % 2 != 0) {
             throw new IllegalArgumentException("Metas must be formatted as follow: m1, v1, m2, v2, ..., mN, vN");
         }
         for (int i = 0; i < metas.length; i += 2) {
             pdoc.addMeta(metas[i], metas[i + 1]);
         }
         return pdoc;
     }
     
 }
