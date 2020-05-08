 package com.indexisto.tool.tolpen.config;
 
 import static com.indexisto.tool.tolpen.prune.search.SearchType.Prefix3;
 
 import java.nio.file.Path;
 import java.nio.file.Paths;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.List;
 
 import com.indexisto.tool.tolpen.prepare.index.IndexType;
 import com.indexisto.tool.tolpen.prepare.request.Request;
 import com.indexisto.tool.tolpen.prepare.request.SearchRequest;
 import com.indexisto.tool.tolpen.prune.search.SearchType;
 import com.indexisto.tool.tolpen.util.selectable.SelectableProvider;
 
 public class Config {
 
 	public final static Path basePath = Paths.get("src/resources/data");
 
     public static class DocumentStorage {
 
         public static final Path source  = basePath.resolve("enwiki-20130102-pages-articles.xml");
         public static final Path storage = basePath.resolve("docStorage");
         public static final long limit   = 1000000L;
         public static final String extension = ".doc";
     }
 
 
     public static class RequestStorage {
 
         public static final Path storage = basePath.resolve("reqStorage");
         public static final long limit   = 1000000L;
         public static final String extension = ".rec";
     }
 
 
     public static class TaskStorage {
        public static final Path source = basePath.resolve("highlight.tasks");
     }
 
 
     public static class Prepare {
 
         public static final String host = "node04";
         public static final int port = 9200;
 
         public static final String type = "doc";
         public static final Path storage = basePath.resolve("indecies");
 
         private final static Collection<SelectableProvider<SearchType>> searchTypes = new ArrayList<>();
         static {
 //            searchTypes.add(new SelectableProvider<>(10, HighSpanNear));
 //            searchTypes.add(new SelectableProvider<>( 5, MedSpanNear));
 //            searchTypes.add(new SelectableProvider<>( 1, LowSpanNear));
 //            searchTypes.add(new SelectableProvider<>(10, HighPhrase));
 //            searchTypes.add(new SelectableProvider<>( 5, MedPhrase));
 //            searchTypes.add(new SelectableProvider<>( 1, LowPhrase));
 //            searchTypes.add(new SelectableProvider<>(10, HighSloppyPhrase));
 //            searchTypes.add(new SelectableProvider<>( 5, MedSloppyPhrase));
 //            searchTypes.add(new SelectableProvider<>( 1, LowSloppyPhrase));
             searchTypes.add(new SelectableProvider<>(10, Prefix3));
         }
         private final static Collection<Request> queries = Arrays.asList(new Request[] {
             new SearchRequest(9L, Long.MAX_VALUE, searchTypes),
             //new UpdateRequest(9L, Long.MAX_VALUE)
         });
         private final static List<IndexType> indexTypes = Arrays.asList(new IndexType[] {
             // big size
 //            new IndexType(3L, 1L, 1000, queries, 1000000L, 100L*60), // hot
 //            new IndexType(3L, 1L, 1000, queries, 1000000L,  10L*60), // med
 //            new IndexType(4L, 1L, 1000, queries, 1000000L,   1L*60), // cold
             // medium size
             new IndexType(10L, 10L, 1000, queries, 10000L, 100L*60), // hot
             new IndexType(10L, 10L, 1000, queries, 10000L,  10L*60), // med
             new IndexType(10L, 10L, 1000, queries, 10000L,   1L*60), // cold
             // small size
             new IndexType(20L, 30L, 1000, queries, 1000L, 100L*60), // hot
             new IndexType(20L, 30L, 1000, queries, 1000L,  10L*60), // med
             new IndexType(20L, 30L, 1000, queries, 1000L,   1L*60)  // cold
         });
 
 
         public static List<IndexType> getIndexTypes() {
             return indexTypes;
         }
 
 
         public static long getDocChunkLenght(long docCount) {
                  if (docCount <    1000L) return 500L;
             else if (docCount <   10000L) return 1000L;
             else if (docCount <  100000L) return 5000L;
             else if (docCount < 1000000L) return 10000L;
             else                          return 20000L;
         }
     }
 }
