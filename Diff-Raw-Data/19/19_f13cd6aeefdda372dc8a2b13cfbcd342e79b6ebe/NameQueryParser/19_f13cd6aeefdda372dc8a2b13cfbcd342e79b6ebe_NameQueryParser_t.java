 package brightsolid.solr.plugins;
 
 import java.io.IOException;
 import java.io.StringReader;
 
 import org.apache.lucene.analysis.Analyzer;
 import org.apache.lucene.analysis.TokenStream;
 import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
 import org.apache.lucene.index.Term;
 import org.apache.lucene.search.BooleanClause;
 import org.apache.lucene.search.BooleanQuery;
 import org.apache.lucene.search.DisjunctionMaxQuery;
 import org.apache.lucene.search.FuzzyQuery;
 import org.apache.lucene.search.Query;
 import org.apache.lucene.search.BooleanClause.Occur;
 import org.apache.lucene.search.TermQuery;
 import org.apache.lucene.search.WildcardQuery;
 import org.apache.lucene.search.spans.SpanMultiTermQueryWrapper;
 import org.apache.lucene.search.spans.SpanQuery;
 import org.apache.lucene.search.spans.SpanTermQuery;
 import org.apache.solr.common.params.SolrParams;
 import org.apache.solr.request.SolrQueryRequest;
 import org.apache.solr.search.LuceneQParser;
 import org.apache.solr.search.QueryParsing;
 import org.apache.solr.search.SyntaxError;
 
 public class NameQueryParser extends LuceneQParser {
 
   private float tie = 0.01f;
   private String field;
   private String value;
   private float synboost = 0.8f;
   private float initialboost = 0.2f;
  private float phoneticboost = 0.1f;
   private float nullboost = 0.01f;
   private float fuzzyboost = 0.2f;
 
   private boolean usefuzzy = true;
   private boolean usephonetic = true;
   private boolean usenull = true;
   private boolean useinitial = true;
   private boolean usesyn = true;
   private String gendervalue;
   private String genderfield;
 
 
   public NameQueryParser(String arg0, SolrParams arg1, SolrParams arg2, SolrQueryRequest arg3) {
     super(arg0, arg1, arg2, arg3);
 
     if (getParam("tie") != null) {
       tie = Float.parseFloat(getParam("tie"));
     }
     if (getParam("synboost") != null) {
       synboost = Float.parseFloat(getParam("synboost"));
     }
     if (getParam("initialboost") != null) {
       initialboost = Float.parseFloat(getParam("initialboost"));
     }
    if (getParam("phoneticboost") != null) {
      phoneticboost = Float.parseFloat(getParam("phoneticboost"));
     }
     if (getParam("nullboost") != null) {
       nullboost = Float.parseFloat(getParam("nullboost"));
     }
     if (getParam("fuzzyboost") != null) {
       fuzzyboost = Float.parseFloat(getParam("fuzzyboost"));
     }
     if (getParam("usesyn") != null) {
       usesyn = Boolean.parseBoolean(getParam("usesyn"));
     }
     if (getParam("usefuzzy") != null) {
       usefuzzy = Boolean.parseBoolean(getParam("usefuzzy"));
     }
     if (getParam("usephonetic") != null) {
       usephonetic = Boolean.parseBoolean(getParam("usephonetic"));
     }
     if (getParam("usenull") != null) {
       usenull = Boolean.parseBoolean(getParam("usenull"));
     }
     if (getParam("useinitial") != null) {
       useinitial = Boolean.parseBoolean(getParam("useinitial"));
     }
     gendervalue = localParams.get("gendervalue");
     genderfield = localParams.get("genderfield", "Gender__facet_text");
     field = localParams.get(QueryParsing.F);
     value = localParams.get(QueryParsing.V).trim();
   }
 
   @Override
   public Query parse() throws SyntaxError {
 
     BooleanQuery bq = new BooleanQuery();
     Analyzer analyzer = getReq().getSchema().getFieldType(field + "_an").getQueryAnalyzer();
 
     TokenStream stream;
     try {
       stream = analyzer.tokenStream(null, new StringReader(value));
       CharTermAttribute cattr = stream.addAttribute(CharTermAttribute.class);
       stream.reset();
       int position = 0;
       while (stream.incrementToken()) {
         Query q = CreateDisjunctionOrWildcard(cattr.toString(), position);
         bq.add(new BooleanClause(q, Occur.MUST));
         position++;
       }
       stream.end();
       stream.close();
     } catch (IOException e) {
       throw new SyntaxError("Query term could not be split", e);
     }
 
     return bq;
   }
 
   private Query CreateDisjunctionOrWildcard(String val, int position) {
     if (val.contains("*") || val.contains("?")) {
       SpanQuery sq;
       if (val.endsWith("*") && val.length() == 2) {
         sq = new SpanTermQuery(new Term(field + "_an_initial", val.substring(0, 1)));
       } else {
         WildcardQuery wq = new WildcardQuery(new Term(field + "_an", val));
         sq = new SpanMultiTermQueryWrapper<WildcardQuery>(wq);
       }
       return new SpanTargetPositionQuery(sq, position);
 
     } else {
       return CreateDisjunction(val, position);
     }
   }
 
   private Query CreateDisjunction(String val, int position) {
 
     DisjunctionMaxQuery dq = new DisjunctionMaxQuery(tie);
 
     // Add analysed name
     SpanQuery aq = new SpanTermQuery(new Term(field + "_an", val));
     Query saq = new SpanTargetPositionQuery(aq, position);
     dq.add(saq);
 
     if(usesyn){
       // Add synonyms
       SpanQuery sq = new SpanTermQuery(new Term(field + "_syn", val));
       Query ssq = new SpanTargetPositionQuery(sq, position);
       ssq.setBoost(synboost);
       dq.add(ssq);
     }
  
     if (useinitial) {
       // Add initial
       String firstChar = val.substring(0, 1);
       SpanQuery iq = new SpanTermQuery(new Term(field + "_an", firstChar));
       Query siq = new SpanTargetPositionQuery(iq, position);
       siq = addGenderQuery(siq);
       siq.setBoost(initialboost);
       dq.add(siq);
     }
 
     if (usephonetic) {
       // Add phonetics
       SpanQuery pq = new SpanTermQuery(new Term(field + "_an_rs", val));
       Query spq = new SpanTargetPositionQuery(pq, position);
       spq = addGenderQuery(spq);
      spq.setBoost(phoneticboost);
       dq.add(spq);
     }
 
     if (usefuzzy) {
       // Add fuzzy
       FuzzyQuery fq = new FuzzyQuery(new Term(field + "_an", val));
       SpanQuery fqw = new SpanMultiTermQueryWrapper<FuzzyQuery>(fq);
       Query sfq = new SpanTargetPositionQuery(fqw, position);
       sfq = addGenderQuery(sfq);
       sfq.setBoost(fuzzyboost);
       dq.add(sfq);
     }
 
     if (usenull) {
       // Null names
       SpanQuery nq = new SpanTermQuery(new Term(field, "-"));
       Query snq = new SpanTargetPositionQuery(nq, position);
       snq = addGenderQuery(snq);
       snq.setBoost(nullboost);
       dq.add(snq);
     }
 
     return dq;
   }
 
   private Query addGenderQuery(Query spq) {
 
     if (gendervalue != null && !gendervalue.isEmpty()) {
       BooleanQuery bq = new BooleanQuery();
       bq.add(spq, Occur.MUST);
       bq.add(new TermQuery(new Term(genderfield, gendervalue)), Occur.MUST);
       return bq;
     }
     return spq;
   }
 }
