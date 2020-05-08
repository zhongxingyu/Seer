 package org.culturegraph.mf.search.index;
 
 import org.apache.lucene.document.Field;
 import org.apache.lucene.index.IndexWriter;
 import org.culturegraph.mf.framework.DefaultObjectReceiver;
 import org.culturegraph.mf.framework.DefaultStreamReceiver;
 import org.culturegraph.mf.framework.StreamReceiver;
 import org.culturegraph.mf.morph.Metamorph;
 import org.culturegraph.mf.search.IndexConstants;
 import org.culturegraph.mf.stream.converter.CGEntityEncoder;
 import org.culturegraph.mf.stream.pipe.StreamTee;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 
 /**
  * writes an event stream (see {@link StreamReceiver}) to a Lucene Index. Still
  * prototypical!
  * 
  * @author Markus Michael Geipel
  * 
  */
 public final class StreamIndexer implements StreamReceiver {
 
 	
	
 	private final BatchIndexer indexer;
 	private final StreamTee tee = new StreamTee();
	private static final Logger LOG = LoggerFactory.getLogger(StreamIndexer.class);
 
 	public StreamIndexer(final IndexWriter indexWriter) {
 		indexer = new BatchIndexer(indexWriter);
		tee.addReceiver(new IndexedFieldReceiver(indexer));
		final CGEntityEncoder encoder = new CGEntityEncoder();
		tee.addReceiver(encoder);
		encoder.setReceiver(new SerializedFieldReceiver(indexer));
 	}
 	
 
 	public StreamIndexer(final IndexWriter indexWriter, final Metamorph metamorph) {
 		indexer = new BatchIndexer(indexWriter);
		tee.addReceiver(metamorph);
		metamorph.setReceiver(new IndexedFieldReceiver(indexer));
		final CGEntityEncoder encoder = new CGEntityEncoder();
		tee.addReceiver(encoder);
		encoder.setReceiver(new SerializedFieldReceiver(indexer));
		
 	}
 	
 	public IndexWriter getIndexWriter(){
 		return indexer.getIndexWriter();
 	}
 
 	public int getCount() {
 		return indexer.getCount();
 	}
 
 	@Override
 	public void startRecord(final String identifier) {
 		indexer.startDocument(identifier);
 		tee.startRecord(identifier);
 	}
 
 	@Override
 	public void endRecord() {
 		tee.endRecord();
 		indexer.endDocument();
 	}
 	
 	@Override
 	public void startEntity(final String name) {
 		tee.startEntity(name);
 	}
 
 	@Override
 	public void endEntity() {
 		tee.endEntity();
 	}
 
 	@Override
 	public void literal(final String name, final String value) {
		LOG.info("teeing " + name + " " + value);
 		tee.literal(name, value);
 	}
 
 	@Override
 	public void resetStream() {
 		throw new UnsupportedOperationException("Cannot reset StreamIndexer");
 	}
 	
 	@Override
 	public void closeStream() {
 		indexer.flush();
 		indexer.close();
 	}
 
 
 	public void setBatchSize(final int batchSize) {
 		indexer.setBatchSize(batchSize);
 	}
 
 	public int getBatchSize() {
 		return indexer.getBatchSize();
 	}
 	
 
 	private static final class IndexedFieldReceiver extends DefaultStreamReceiver {
 		private final BatchIndexer indexer;
 
 		public IndexedFieldReceiver(final BatchIndexer indexer) {
 			super();
 			this.indexer = indexer;
 		}
 	
 		@Override
 		public void literal(final String name, final String value) {
			
			LOG.info("indexing " + name + " " + value);
			
 			indexer.add(new Field(name, value, Field.Store.NO, Field.Index.ANALYZED));
 		}
 	}
 	
 	private static final class SerializedFieldReceiver extends DefaultObjectReceiver<String> {
 		private final BatchIndexer indexer;
 
 		public SerializedFieldReceiver(final BatchIndexer indexer) {
 			super();
 			this.indexer = indexer;
 		}
 	
 		@Override
 		public void process(final String value) {
 			indexer.add(new Field(IndexConstants.SERIALIZED, value, Field.Store.YES, Field.Index.NO));
 		}
 	}
 }
