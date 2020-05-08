 package pt.inevo.encontra;
 
import pt.inevo.encontra.common.ResultSet;
import pt.inevo.encontra.common.ResultSetDefaultImpl;
 import pt.inevo.encontra.index.IndexedObject;
 import pt.inevo.encontra.nbtree.index.NBTreeSearcher;
 import pt.inevo.encontra.query.Query;
 import pt.inevo.encontra.storage.IEntry;
 
 /**
  * A simple Knn image searcher.
  * @author Ricardo
  */
 public class SimpleImageSearcher<O extends IndexedObject> extends NBTreeSearcher<O> {
 
     @Override
    public ResultSet<O> search(Query query) {
        ResultSet<IEntry> results = new ResultSetDefaultImpl<IEntry>();
 
         // TODO implement here this simple image searcher to perform knn
 
 //        if (supportsQueryType(query.getType())) {
 //            if (query.getType().equals(Query.QueryType.KNN)) {
 //                KnnQuery q = (KnnQuery) query;
 //                IndexedObject o = (IndexedObject) q.getQuery();
 //                if (o.getValue() instanceof BufferedImage) {
 //                    Descriptor d = getDescriptorExtractor().extract(o);
 //                    results = performKnnQuery(d, q.getKnn());
 //                }
 //            }
 //        }
 
         return getResultObjects(results);
     }
 }
