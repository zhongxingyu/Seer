 package eu.stratosphere.sopremo.base;
 
 import com.fasterxml.jackson.databind.ObjectMapper;
 import eu.stratosphere.nephele.configuration.Configuration;
 import eu.stratosphere.nephele.template.GenericInputSplit;
 import eu.stratosphere.pact.common.contract.GenericDataSource;
 import eu.stratosphere.pact.common.io.TableInputFormat;
 import eu.stratosphere.pact.common.io.statistics.BaseStatistics;
 import eu.stratosphere.pact.common.plan.PactModule;
 import eu.stratosphere.pact.generic.io.InputFormat;
 import eu.stratosphere.sopremo.EvaluationContext;
 import eu.stratosphere.sopremo.expressions.EvaluationExpression;
 import eu.stratosphere.sopremo.io.JsonGenerator;
 import eu.stratosphere.sopremo.operator.ElementaryOperator;
 import eu.stratosphere.sopremo.operator.InputCardinality;
 import eu.stratosphere.sopremo.operator.Name;
 import eu.stratosphere.sopremo.operator.Property;
 import eu.stratosphere.sopremo.pact.SopremoUtil;
 import eu.stratosphere.sopremo.serialization.SopremoRecord;
 import eu.stratosphere.sopremo.serialization.SopremoRecordLayout;
 import eu.stratosphere.sopremo.type.IJsonNode;
 import eu.stratosphere.sopremo.type.NullNode;
 import eu.stratosphere.sopremo.type.ObjectNode;
 import eu.stratosphere.sopremo.type.TextNode;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.hbase.HBaseConfiguration;
 import org.apache.hadoop.hbase.client.Get;
 import org.apache.hadoop.hbase.client.HTable;
 import org.apache.hadoop.hbase.client.Result;
 import org.apache.hadoop.util.StringUtils;
 import org.okkam.dopa.apis.beans.request.GetUnstructuredDocumentsQuery;
 import org.okkam.dopa.apis.client.OkkamDopaIndexClient;
 import org.okkam.dopa.apis.response.GetUnstructuredDocumentsResponse;
 
 import java.io.IOException;
 import java.io.StringWriter;
 import java.nio.charset.Charset;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 /**
  * Operator that queries the OKKAM index, retrieves the URLs, and fetches the documents from IMR's HBase instance.
  */
 @Name(verb = "fetchIMRDocsWithOKKAMQuery")
 @InputCardinality(0)
 public class OKKAMIndexIMRFetch extends ElementaryOperator<OKKAMIndexIMRFetch> {
 
     private static final Log LOG = LogFactory.getLog(OKKAMIndexIMRFetch.class);
 
     public static final String QUERY_PARAMETER = "OKKAM.index.query.parameter";
 
     private static final Charset UTF8 = Charset.forName("UTF-8");
 
     private String query;
 
     public static class OKKAMIndexInputFormat implements InputFormat<SopremoRecord, GenericInputSplit> {
 
         private OkkamDopaIndexClient client;
         private String queryString;
         private List<String> docIds;
         private ObjectNode node = new ObjectNode();
         private TextNode textNode = new TextNode();
 
         private int resultIndex;
         private Result[] results;
 
         private String tableName;
         private String configLocation;
         private org.apache.hadoop.conf.Configuration hConf;
         private HTable table;
 
 
         @Override
         public void configure(Configuration parameters) {
             queryString = parameters.getString(QUERY_PARAMETER, null);
             tableName = parameters.getString(TableInputFormat.INPUT_TABLE, null);
             configLocation = parameters.getString(TableInputFormat.CONFIG_LOCATION, null);
         }
 
         @Override
         public BaseStatistics getStatistics(BaseStatistics cachedStatistics) throws IOException {
             return null;
         }
 
         @Override
         public GenericInputSplit[] createInputSplits(int minNumSplits) throws IOException {
             GenericInputSplit[] splits = new GenericInputSplit[minNumSplits];
             for (int i = 0; i < minNumSplits; i++) {
                 splits[i] = new GenericInputSplit(i);
             }
             return splits;
         }
 
         @Override
         public Class<? extends GenericInputSplit> getInputSplitType() {
             return GenericInputSplit.class;
         }
 
         @Override
         public void open(GenericInputSplit split) throws IOException {
             if (split.getSplitNumber() == 0) {
                 // FIXME parameterize the host
                 client = new OkkamDopaIndexClient("192.168.0.34:8080", "okkam-index", 10);
 
                 ObjectMapper mapper = new ObjectMapper();
                 GetUnstructuredDocumentsQuery query = mapper.readValue(queryString, GetUnstructuredDocumentsQuery.class);
                 GetUnstructuredDocumentsResponse ret = null;
 
                 try {
                     ret = client.getUnstructuredDocuments(query, false);
                 } catch (IOException e) {
                     // TODO implement exception handling
                 }
                 if (ret.getError() == null) {
                    docIds = ret.getDocIds();
                     table = getHTable();
 
                     List<Get> gets = new ArrayList<Get>();
                     for (String docId : docIds) {
                         byte[] row = docId.getBytes(UTF8);
                         Get getInstance = new Get(row);
                         // TODO restrict the get to the required operations
                         gets.add(getInstance);
                     }
                     results = table.get(gets);
                     resultIndex = 0;
                 }
 
             }
         }
 
         @Override
         public boolean reachedEnd() throws IOException {
             return resultIndex >= results.length;
         }
 
         @Override
         public boolean nextRecord(SopremoRecord record) throws IOException {
             if (resultIndex < results.length) {
                 Result res = results[resultIndex];
                 // TODO extract data from result
                res.get
                 resultIndex++;
                 return true;
             }
             return false;
         }
 
         @Override
         public void close() throws IOException {
             table.close();
         }
 
         private HTable getHTable () {
             LOG.info("Got config location: " + configLocation);
             if (configLocation != null)
             {
                 org.apache.hadoop.conf.Configuration dummyConf = new org.apache.hadoop.conf.Configuration();
                 dummyConf.addResource(new Path("file://" + configLocation));
                 hConf = HBaseConfiguration.create(dummyConf);
                 // hConf.set("hbase.master", "im1a5.internetmemory.org");
                 LOG.info("hbase master: " + hConf.get("hbase.master"));
                 LOG.info("zookeeper quorum: " + hConf.get("hbase.zookeeper.quorum"));
             }
             try {
                 return new HTable(this.hConf, tableName);
             } catch (Exception e) {
                 LOG.error(StringUtils.stringifyException(e));
             }
             return null;
         }
     }
 
     @Override
     public PactModule asPactModule(EvaluationContext context, SopremoRecordLayout layout) {
         GenericDataSource<OKKAMIndexInputFormat> contract = new GenericDataSource<OKKAMIndexInputFormat>(
                 OKKAMIndexInputFormat.class, String.format("OKKAMIndex %s", query));
 
         final PactModule pactModule = new PactModule(0, 1);
         SopremoUtil.setEvaluationContext(contract.getParameters(), context);
         SopremoUtil.setLayout(contract.getParameters(), layout);
         contract.getParameters().setString(QUERY_PARAMETER, query);
         pactModule.getOutput(0).setInput(contract);
         return pactModule;
     }
 
 
     @Property(preferred = true)
     @Name(noun = "for")
     public void setQueryParameter(EvaluationExpression value) {
         if (value == null)
             throw new NullPointerException("value expression must not be null");
         IJsonNode node = value.evaluate(NullNode.getInstance());
         StringWriter writer = new StringWriter();
         JsonGenerator gen = new JsonGenerator(writer);
         try {
             gen.writeTree(node);
             gen.flush();
         } catch (IOException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
         query = writer.getBuffer().toString();
     }
 }
