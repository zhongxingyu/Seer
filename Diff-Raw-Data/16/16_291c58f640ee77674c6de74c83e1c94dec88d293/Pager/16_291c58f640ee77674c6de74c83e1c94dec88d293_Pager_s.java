 package com.shaubert.blogadapter.client;
 
 import java.io.IOException;
 import java.util.Collections;
 import java.util.List;
 
 public class Pager<T> {    
     
     private DataLoader loader;
     private Parser parser;
     private DataLoaderRequest initialRequest;
     
     private PagerDataParserResult<T> result;
     
     public Pager(DataLoader loader, Parser parser, DataLoaderRequest initialRequest) {
         this.loader = loader;
         this.parser = parser;
         this.initialRequest = initialRequest;
     }
     
     public Pager(DataLoader loader, Parser parser, PagerDataParserResult<T> prevResult) {
         this.loader = loader;
         this.parser = parser;
         this.result = prevResult;
     }
 
     public boolean hasNext() {
         return result == null ? true : result.getNextDataRequest() != null;
     }
     
     public DataLoaderRequest getNextLoaderRequest() {
         return result != null ? result.getNextDataRequest() : null;
     }
     
     public List<T> loadNext() throws IOException {
         if (hasNext()) {
             DataLoaderRequest request = result == null ? initialRequest : result.getNextDataRequest();
            result = parser.parse(request, loader.load(request));
            return result.getResult();
         } else {
             return Collections.emptyList();
         }
     }
     
 }
