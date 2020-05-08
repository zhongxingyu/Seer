 package com.codingstory.polaris.search;
 
 import com.codingstory.polaris.indexing.FieldName;
 import com.codingstory.polaris.indexing.FileId;
 import com.codingstory.polaris.indexing.PojoToThriftConverter;
 import com.codingstory.polaris.indexing.analysis.JavaSrcAnalyzer;
 import com.codingstory.polaris.parser.Token;
 import com.google.common.base.Preconditions;
 import com.google.common.base.Predicate;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import org.apache.commons.codec.DecoderException;
 import org.apache.commons.codec.binary.Hex;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.lucene.analysis.Analyzer;
 import org.apache.lucene.analysis.WhitespaceAnalyzer;
 import org.apache.lucene.document.Document;
 import org.apache.lucene.index.IndexReader;
 import org.apache.lucene.index.Term;
 import org.apache.lucene.queryParser.MultiFieldQueryParser;
 import org.apache.lucene.queryParser.ParseException;
 import org.apache.lucene.queryParser.QueryParser;
 import org.apache.lucene.search.*;
 import org.apache.lucene.search.highlight.Highlighter;
 import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
 import org.apache.lucene.search.highlight.QueryScorer;
 import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
 import org.apache.lucene.util.Version;
 
 import java.io.Closeable;
 import java.io.IOException;
 import java.util.List;
 import java.util.Map;
 
 import static com.codingstory.polaris.indexing.FieldName.*;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Zhou Yunqing
  * Date: 12-8-18
  * Time: 下午11:43
  * To change this template use File | Settings | File Templates.
  */
 public class SrcSearcher implements Closeable {
     private static final Log LOGGER = LogFactory.getLog(SrcSearcher.class);
     private final IndexReader reader;
     private final IndexSearcher searcher;
     private final QueryParser parser;
 
     public SrcSearcher(IndexReader reader) throws IOException {
         this.reader = Preconditions.checkNotNull(reader);
         searcher = new IndexSearcher(reader);
         String[] fields = FieldName.ALL_FIELDS.toArray(new String[FieldName.ALL_FIELDS.size()]);
         Map<String, Float> boostMap = Maps.newHashMap();
         boostMap.put(FieldName.TYPE_NAME, 4.0f);
         boostMap.put(FieldName.METHOD_NAME, 3.0f);
         boostMap.put(FieldName.PACKAGE_NAME, 1.0f);
         parser = new MultiFieldQueryParser(Version.LUCENE_36, fields, new JavaSrcAnalyzer(), boostMap);
     }
 
     @Override
     public void close() throws IOException {
         searcher.close();
         reader.close();
     }
 
     public String getContent(byte[] fileId) throws IOException {
         Query query = new TermQuery(new Term(FILE_ID, Hex.encodeHexString(fileId)));
         int docid = searcher.search(query, 1).scoreDocs[0].doc;
         return new String(reader.document(docid).getFieldable(FILE_CONTENT).getBinaryValue());
     }
 
     public List<TSearchResultEntry> search(String queryString, int limit) throws ParseException, IOException, InvalidTokenOffsetsException {
         LOGGER.debug("Query: " + queryString);
         Query query = parser.parse(queryString);
         TopDocs topDocs = searcher.search(query, limit);
         ScoreDoc[] scoreDocs = topDocs.scoreDocs;
         List<TSearchResultEntry> results = Lists.newArrayList();
         Highlighter highlighter = new Highlighter(new SimpleHTMLFormatter(), new QueryScorer(query));
         Analyzer analyzer = new WhitespaceAnalyzer(Version.LUCENE_36);
         for (ScoreDoc doc : scoreDocs) {
             TSearchResultEntry result = new TSearchResultEntry();
             int docid = doc.doc;
             Document document = reader.document(docid);
             result.setProjectName(document.getFieldable(PROJECT_NAME).stringValue());
             result.setFileName(document.getFieldable(FILE_NAME).stringValue());
             byte[] fileId = null;
             try {
                 fileId = Hex.decodeHex(document.getFieldable(FILE_ID).stringValue().toCharArray());
             } catch (DecoderException e) {
                 throw new AssertionError(e);
             }
             result.setFileId(new FileId(fileId).getValueAsString());
             String content = getContent(fileId);
             result.setDocumentId(docid);
            Explanation explanation = searcher.explain(query, docid);
            result.setScore(explanation.getValue());
            result.setExplanation(explanation.toHtml());
             result.setKind(PojoToThriftConverter.convertTokenKind(Token.Kind.valueOf(document.get(KIND))));
             LOGGER.debug(result.getFileName() + "(" + Hex.encodeHexString(fileId) + ")");
             int offset = Integer.parseInt(document.get(OFFSET));
             result.setSummary(getSummary(content, offset));
             results.add(result);
         }
         return results;
     }
 
     public String getSummary(String content, int offset) {
         String[] lines = content.split("\n");
         int i = 0;
         int lengthTillNow = 0;
         for (; i < lines.length; ++i) {
             if (lengthTillNow > offset) {
                 break;
             }
             lengthTillNow += lines[i].length() + 1;
         }
         StringBuilder builder = new StringBuilder();
         for (int j = i - 2; j < i + 3; ++j) {
             if (j >= 0 && j < lines.length) {
                 builder.append(lines[j]);
                 builder.append("\n");
             }
         }
         return builder.toString();
     }
 
     public List<String> completeQuery(final String queryString, int limit) throws IOException {
         LOGGER.debug("Complete query: " + queryString);
         BooleanQuery q = new BooleanQuery();
         q.add(new PrefixQuery(new Term(FieldName.PACKAGE_NAME, queryString)), BooleanClause.Occur.SHOULD);
         q.add(new PrefixQuery(new Term(FieldName.TYPE_NAME, queryString)), BooleanClause.Occur.SHOULD);
         q.add(new PrefixQuery(new Term(FieldName.FILE_NAME, queryString)), BooleanClause.Occur.SHOULD);
         TopDocs topDocs = searcher.search(q, limit);
         ScoreDoc[] scoreDocs = topDocs.scoreDocs;
         List<String> results = Lists.newArrayList();
         for (ScoreDoc scoreDoc : scoreDocs) {
             int docid = scoreDoc.doc;
             Document document = reader.document(docid);
             results.addAll(ImmutableList.copyOf(document.getValues(FieldName.PACKAGE_NAME)));
             results.addAll(ImmutableList.copyOf(document.getValues(FieldName.TYPE_NAME)));
             results.addAll(ImmutableList.copyOf(document.getValues(FieldName.FILE_NAME)));
         }
         results = ImmutableList.copyOf(Iterables.filter(results, new Predicate<String>() {
             @Override
             public boolean apply(String s) {
                 return s.startsWith(queryString);
             }
         }));
         if (results.size() > limit) {
             results = results.subList(0, limit);
         }
         LOGGER.debug("Candidates: " + results);
         return results;
     }
 }
