 package eu.stratosphere.sopremo.base;
 
 import com.fasterxml.jackson.databind.ObjectMapper;
 import eu.stratosphere.nephele.configuration.Configuration;
 import eu.stratosphere.nephele.template.GenericInputSplit;
 import eu.stratosphere.pact.common.contract.GenericDataSource;
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
 import org.okkam.dopa.apis.beans.request.GetUnstructuredDocumentsQuery;
 import org.okkam.dopa.apis.client.OkkamDopaIndexClient;
 import org.okkam.dopa.apis.response.GetUnstructuredDocumentsResponse;
 
 import java.io.IOException;
 import java.io.StringWriter;
 import java.util.Iterator;
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  * User: mleich
  * Date: 7/11/2013
  * Time: 14:13
  * To change this template use File | Settings | File Templates.
  */
 @Name(verb = "getDocumentRefsFromOKKAM")
 @InputCardinality(0)
 public class OKKAMIndexAccess extends ElementaryOperator<OKKAMIndexAccess> {
 
     public static final String QUERY_PARAMETER = "OKKAM.index.query.parameter";
 
     private String query;
 
     public static class OKKAMIndexInputFormat implements InputFormat<SopremoRecord, GenericInputSplit> {
 
         private OkkamDopaIndexClient client;
         private String queryString;
         private List<String> docIds;
         private ObjectNode node = new ObjectNode();
         private TextNode textNode = new TextNode();
         private Iterator<String> iterator;
 
         @Override
         public void configure(Configuration parameters) {
             queryString = parameters.getString(QUERY_PARAMETER, null);
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
                     //FIXME: handle garbage entities 
                	docIds = ret.getDocIds();
                     iterator = docIds.iterator();
                 }
 
             }
         }
 
         @Override
         public boolean reachedEnd() throws IOException {
             return iterator == null || !iterator.hasNext();
         }
 
         @Override
         public boolean nextRecord(SopremoRecord record) throws IOException {
             if (iterator != null && iterator.hasNext()) {
                 String docId = iterator.next();
                 textNode.setValue(docId);
                 node.put("docID", textNode);
                 record.setNode(node);
                 return true;
             }
             return false;
         }
 
         @Override
         public void close() throws IOException {
             // do nothing
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
