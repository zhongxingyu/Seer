 package org.virtual.sr;
 
 import com.hp.hpl.jena.query.Query;
 import com.hp.hpl.jena.query.QueryExecutionFactory;
 import com.hp.hpl.jena.query.QueryFactory;
 import org.virtualrepository.Asset;
 import org.virtualrepository.impl.Type;
 import org.virtualrepository.spi.Importer;
 
 import com.hp.hpl.jena.query.ResultSet;
 
 public class RdfImporter<A extends Asset> implements Importer<A, ResultSet> {
 
     private final RepositoryConfiguration configuration;
     private final Type<A> type;
 
     public RdfImporter(Type<A> type, RepositoryConfiguration configuration) {
         this.configuration = configuration;
         this.type = type;
     }
 
     @Override
     public Type<A> type() {
         return type;
     }
 
     @Override
     public Class<ResultSet> api() {
         return ResultSet.class;
     }
 
     @Override
     public ResultSet retrieve(A asset) throws Exception {
 
         Query q = QueryFactory.create(configuration.sparqlQueryForCodelist(asset.id()));
         String endpoint = configuration.discoveryURI().toString();
         ResultSet codes = QueryExecutionFactory.sparqlService(endpoint, q).execSelect();
         System.out.println(configuration);
        return codes;
     }
 }
