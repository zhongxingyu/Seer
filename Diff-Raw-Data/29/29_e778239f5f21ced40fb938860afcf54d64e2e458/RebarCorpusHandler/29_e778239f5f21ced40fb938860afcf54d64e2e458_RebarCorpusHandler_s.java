 /**
  * 
  */
 package edu.jhu.hlt.rebar.accumulo;
 
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import org.apache.accumulo.core.client.BatchScanner;
 import org.apache.accumulo.core.client.BatchWriter;
 import org.apache.accumulo.core.client.Connector;
 import org.apache.accumulo.core.client.MutationsRejectedException;
 import org.apache.accumulo.core.client.Scanner;
 import org.apache.accumulo.core.client.TableNotFoundException;
 import org.apache.accumulo.core.data.Key;
 import org.apache.accumulo.core.data.Mutation;
 import org.apache.accumulo.core.data.Range;
 import org.apache.accumulo.core.data.Value;
 
 import org.apache.thrift.TDeserializer;
 import org.apache.thrift.TException;
 import org.apache.thrift.protocol.TBinaryProtocol;
 
 import com.maxjthomas.dumpster.CorpusHandler;
 import com.maxjthomas.dumpster.Document;
 import com.maxjthomas.dumpster.RebarThriftException;
 
 import edu.jhu.hlt.rebar.RebarException;
 import edu.jhu.hlt.rebar.config.RebarConfiguration;
 import edu.jhu.hlt.rebar.util.RebarUtil;
 
 /**
  * @author max
  * 
  */
 public class RebarCorpusHandler extends AbstractAccumuloClient implements CorpusHandler.Iface {
 
   private final BatchWriter corporaTableBW;
 
   private final TDeserializer deserializer;
   
   /**
    * @throws RebarException
    */
   public RebarCorpusHandler() throws RebarException {
     this(AbstractAccumuloClient.getConnector());
   }
 
   /**
    * @param conn
    * @throws RebarException
    */
   public RebarCorpusHandler(Connector conn) throws RebarException {
     super(conn);
     this.tableOps.createTableIfNotExists(RebarConfiguration.AVAILABLE_CORPUS_TABLE_NAME);
     this.deserializer = new TDeserializer(new TBinaryProtocol.Factory());
     try {
       this.corporaTableBW = this.conn.createBatchWriter(RebarConfiguration.AVAILABLE_CORPUS_TABLE_NAME, defaultBwOpts.getBatchWriterConfig());
     } catch (TableNotFoundException e) {
       throw new RebarException(e);
     }
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see edu.jhu.hlt.rebar.accumulo.AbstractAccumuloClient#flush()
    */
   @Override
   public void flush() throws RebarException {
     try {
       this.corporaTableBW.flush();
     } catch (MutationsRejectedException e) {
       throw new RebarException(e);
     }
   }
 
   // private void
 
   /*
    * (non-Javadoc)
    * 
    * @see com.maxjthomas.dumpster.CorpusHandler.Iface#createCorpus(java.lang.String, java.util.Set)
    */
   @Override
   public void createCorpus(String corpusName, Set<Document> docList) throws RebarThriftException, TException {
     if (this.corpusExists(corpusName))
       throw new RebarThriftException("This corpus already exists.");
 
     if (!isValidCorpusName(corpusName)) {
       throw new RebarThriftException("Corpus name must begin with 'corpus_'. Corpus name: " + corpusName + " is not valid.");
     }
 
     if (docList.size() == 0) {
       throw new RebarThriftException("Can't create a corpus with zero documents.");
     }
 
     // first, add this to the available corpora table.
     final Mutation m = new Mutation(corpusName);
     m.put("", "", new Value(new byte[0]));
 
     try {
       this.corporaTableBW.addMutation(m);
 
       // now, try to create the new table.
       this.tableOps.createTableIfNotExists(corpusName);
 
       BatchWriter bw = this.conn.createBatchWriter(corpusName, defaultBwOpts.getBatchWriterConfig());
       for (Document d : docList) {
         final Mutation subM = new Mutation(d.id);
         subM.put("", "", new Value(new byte[0]));
         bw.addMutation(subM);
       }
 
       bw.close();
 
     } catch (MutationsRejectedException | RebarException | TableNotFoundException e) {
       try {
         throw RebarUtil.wrapException(e);
       } catch (RebarException e1) {
         throw new RebarThriftException(e1.getMessage());
       }
     }
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see com.maxjthomas.dumpster.CorpusHandler.Iface#getCorpusDocumentSet(java.lang.String)
    */
   @Override
   public Set<Document> getCorpusDocumentSet(String corpusName) throws RebarThriftException, TException {
     Set<Document> docSet = new HashSet<>();
     try {
       // first hit the corpus table itself, to get the ids for the ranges.
       Scanner sc = this.conn.createScanner(corpusName, RebarConfiguration.getAuths());
       Range r = new Range();
       sc.setRange(r);
       
       Set<Range> rangeSet = new HashSet<>();
       Iterator<Entry<Key, Value>> iter = sc.iterator();
       while (iter.hasNext()) {
         Key k = iter.next().getKey();
         String rowId = k.getRow().toString();
         Range range = new Range(rowId);
         rangeSet.add(range);
       }
       
       BatchScanner bsc = this.conn.createBatchScanner(RebarConfiguration.DOCUMENT_TABLE_NAME, RebarConfiguration.getAuths(), 10);
       bsc.setRanges(rangeSet);
 
      //bsc.addScanIterator(WholeRowIterator.class);
       Iterator<Entry<Key, Value>> bscIter = bsc.iterator();
       while (bscIter.hasNext()) {
         Value v = bscIter.next().getValue();
         Document d = new Document(); 
         this.deserializer.deserialize(d, v.get());
         docSet.add(d);
       }
       
       bsc.close();
       
       if (docSet.size() != rangeSet.size()) {
         throw new RebarThriftException("Did not retrieve all documents associated "
             + "with corpus: " + corpusName + "; expected: " + rangeSet.size()
             + " but got: " + docSet.size());
       }
       
       return docSet;
     } catch (TableNotFoundException e) {
       throw new RebarThriftException(e.getMessage());
     }
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see com.maxjthomas.dumpster.CorpusHandler.Iface#listCorpora()
    */
   @Override
   public Set<String> listCorpora() throws RebarThriftException, TException {
     try {
       Scanner sc = this.conn.createScanner(RebarConfiguration.AVAILABLE_CORPUS_TABLE_NAME, RebarConfiguration.getAuths());
       Range r = new Range();
       sc.setRange(r);
       Iterator<Entry<Key, Value>> iter = sc.iterator();
       Set<String> corporaSet = new HashSet<>();
       while (iter.hasNext()) {
         Key k = iter.next().getKey();
         corporaSet.add(k.toString());
       }
       
       return corporaSet;
     } catch (TableNotFoundException e) {
       throw new RebarThriftException(e.getMessage());
     }
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see com.maxjthomas.dumpster.CorpusHandler.Iface#deleteCorpus(java.lang.String)
    */
   @Override
   public void deleteCorpus(String corpusName) throws RebarThriftException, TException {
     if (!this.corpusExists(corpusName))
       throw new RebarThriftException("Can't delete a corpus that doesn't exist.");
 
     try {
       // first, delete corpus table.
       this.tableOps.deleteTable(corpusName);
 
       // next, remove entry from available_corpora.
       final Mutation mDel = new Mutation(corpusName);
       mDel.putDelete("", "");
       this.corporaTableBW.addMutation(mDel);
     } catch (MutationsRejectedException | RebarException e) {
       try {
         throw RebarUtil.wrapException(e);
       } catch (RebarException e1) {
         throw new RebarThriftException(e1.getMessage());
       }
     }
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see java.lang.AutoCloseable#close()
    */
   @Override
   public void close() throws Exception {
     this.corporaTableBW.close();
     this.bw.close();
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see com.maxjthomas.dumpster.CorpusHandler.Iface#corpusExists(java.lang.String)
    */
   @Override
   public boolean corpusExists(String corpusName) throws TException {
     try {
       Scanner sc = this.conn.createScanner(RebarConfiguration.AVAILABLE_CORPUS_TABLE_NAME, RebarConfiguration.getAuths());
       Range r = new Range(corpusName);
       sc.setRange(r);
       Iterator<Entry<Key, Value>> iter = sc.iterator();
       return iter.hasNext();
     } catch (TableNotFoundException e) {
       try {
         throw RebarUtil.wrapException(e);
       } catch (RebarException e1) {
         throw new TException(e1.getMessage());
       }
     }
   }
 
   protected static String generateCorpusName(String corpusName) {
     return RebarConfiguration.CORPUS_PREFIX + corpusName;
   }
 
   public static boolean isValidCorpusName(String corpusName) {
     return corpusName.startsWith(RebarConfiguration.CORPUS_PREFIX);
   }
 }
