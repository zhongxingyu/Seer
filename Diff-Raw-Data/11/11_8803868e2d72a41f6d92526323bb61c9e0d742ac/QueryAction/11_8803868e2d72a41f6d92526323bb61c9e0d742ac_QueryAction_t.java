 package com.turbulence.core.actions;
 
 import java.io.IOException;
 import java.io.OutputStream;
 
 import java.lang.String;
 
 import java.util.*;
 
 import javax.ws.rs.core.StreamingOutput;
 
 import javax.ws.rs.WebApplicationException;
 
 import org.apache.commons.lang.UnhandledException;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import com.hp.hpl.jena.query.QueryExecution;
 import com.hp.hpl.jena.query.QueryExecutionFactory;
 import com.hp.hpl.jena.query.QuerySolution;
 import com.hp.hpl.jena.query.ResultSet;
 
 import com.hp.hpl.jena.rdf.model.Model;
 import com.hp.hpl.jena.rdf.model.ModelFactory;
 
 import com.hp.hpl.jena.query.Query;
 import com.hp.hpl.jena.query.QueryFactory;
 
 import com.turbulence.core.ClusterSpaceJenaGraph;
 
 import me.prettyprint.cassandra.model.ConfigurableConsistencyLevel;
 
 import me.prettyprint.cassandra.serializers.StringSerializer;
 import me.prettyprint.cassandra.serializers.UUIDSerializer;
 
 import me.prettyprint.cassandra.service.ColumnSliceIterator;
 
 import me.prettyprint.cassandra.service.template.ColumnFamilyTemplate;
 import me.prettyprint.cassandra.service.template.ThriftColumnFamilyTemplate;
 
 import me.prettyprint.cassandra.service.ThriftKsDef;
 
 import me.prettyprint.hector.api.beans.ColumnSlice;
 import me.prettyprint.hector.api.beans.HColumn;
 import me.prettyprint.hector.api.beans.OrderedRows;
 import me.prettyprint.hector.api.beans.Row;
 import me.prettyprint.hector.api.beans.Rows;
 
 import me.prettyprint.hector.api.Cluster;
 
 import me.prettyprint.hector.api.ddl.ColumnFamilyDefinition;
 import me.prettyprint.hector.api.ddl.ComparatorType;
 import me.prettyprint.hector.api.ddl.KeyspaceDefinition;
 
 import me.prettyprint.hector.api.exceptions.HectorException;
 
 import me.prettyprint.hector.api.factory.HFactory;
 
 import me.prettyprint.hector.api.HConsistencyLevel;
 import me.prettyprint.hector.api.Keyspace;
 
 import me.prettyprint.hector.api.query.MultigetSliceQuery;
 import me.prettyprint.hector.api.query.RangeSlicesQuery;
 import me.prettyprint.hector.api.query.SliceQuery;
 
 public class QueryAction implements Action {
     private static final Log logger = LogFactory.getLog(QueryAction.class);
 
     private final String query;
     /**
      * Constructs a new instance.
      */
     public QueryAction(String query)
     {
         this.query = query;
     }
 
 	public StreamingOutput stream() {
         Query q = QueryFactory.create(query);
 
        if (q.getQueryType() != Query.QueryTypeSelect) {
            final String message = "<result><message>Cannot handle query " + query
                + ". Only SELECT supported.</message><error>"
                + TurbulenceError.QUERY_PARSE_FAILED + "</error></result>";
            return new StreamingOutput() {
                public void write(OutputStream out) throws IOException, WebApplicationException {
                    out.write(message.getBytes());
                }
            };
        }

         Model model = ModelFactory.createModelForGraph(new ClusterSpaceJenaGraph());
         QueryExecution exec = QueryExecutionFactory.create(q, model);
         final ResultSet resultSet = exec.execSelect();
 
         ColumnFamilyTemplate<String, UUID> template;
         Cluster myCluster = HFactory.getOrCreateCluster("turbulence", "localhost:9160");
         ColumnFamilyDefinition cfDef = HFactory.createColumnFamilyDefinition("Turbulence",
                 "Main",
                 ComparatorType.UUIDTYPE);
         cfDef.setKeyValidationClass("UTF8Type");
         KeyspaceDefinition kspd = myCluster.describeKeyspace("Turbulence");
         if (kspd == null) {
             KeyspaceDefinition newKeyspace = HFactory.createKeyspaceDefinition("Turbulence", ThriftKsDef.DEF_STRATEGY_CLASS, 1 /*TODO replication factor*/, Arrays.asList(cfDef));
             myCluster.addKeyspace(newKeyspace, true);
         }
         // FIXME: in a cluster we don't want this
         ConfigurableConsistencyLevel lvl = new ConfigurableConsistencyLevel();
         lvl.setDefaultReadConsistencyLevel(HConsistencyLevel.ONE);
         lvl.setDefaultWriteConsistencyLevel(HConsistencyLevel.ONE);
         final Keyspace ksp = HFactory.createKeyspace("Turbulence", myCluster, lvl);
 
         template = new ThriftColumnFamilyTemplate<String, UUID>(ksp, "Main", StringSerializer.get(), UUIDSerializer.get());
 
         return new StreamingOutput() {
             public void write(OutputStream out) throws IOException, WebApplicationException {
                 while (resultSet.hasNext()) {
                     logger.warn("-----------------------------");
                     QuerySolution row = resultSet.next();
                     List<String> urls = new ArrayList<String>();
 
                     //MultigetSliceQuery<String, UUID, String> query = HFactory.createMultigetSliceQuery(ksp, StringSerializer.get(), UUIDSerializer.get(), StringSerializer.get());
                     SliceQuery<String, UUID, String> query = HFactory.createSliceQuery(ksp, StringSerializer.get(), UUIDSerializer.get(), StringSerializer.get());
                     List<String> keys = new ArrayList<String>(resultSet.getResultVars().size());
                     for (String var : resultSet.getResultVars()) {
                         keys.add(row.getResource(var).getURI());
                         logger.warn(row.getResource(var).getURI());
                     }
                     query.setKey(keys.get(0));
                     try {
                         //ColumnFamilyResult<UUID, String> res = template.queryColumns("a key");
                         query.setColumnFamily("Main");
 
                         //Rows<String, UUID, String> qr = query.execute().get();
                         ColumnSliceIterator<String, UUID, String> it = new ColumnSliceIterator<String, UUID, String>(query, null, (UUID)null, false);
                         /*for (Row<String, UUID, String> cRow : qr) {
                             logger.warn("ROW " + cRow.getKey());
                             ColumnSlice<UUID, String> slice = cRow.getColumnSlice();
                             for (HColumn<UUID, String> col : slice.getColumns()) {
                                 logger.warn(col.getName());
                                 logger.warn(col.getValue());
                             }
                         }*/
                         while (it.hasNext()) {
                             HColumn<UUID, String> col = it.next();
                             out.write(col.getValue().getBytes());
                         }
                     } catch (HectorException e) {
                         throw new UnhandledException(e);
                     }
                         //out.write(row.get(s).toString().getBytes());
                 }
             }
         };
 	}
 
 	public Result perform() {
 	    throw new UnsupportedOperationException();
     }
 }
