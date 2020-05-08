 package dna.updates.generators.util;
 
 import java.io.File;
 
 import dna.graph.Graph;
 import dna.io.BatchReader;
 import dna.updates.batch.Batch;
 import dna.updates.generators.BatchGenerator;
 
 /**
  * 
  * Batch generator that reads batches from a given dir. the filename depends on
  * the given FilenameIndexType, either the graph's current timestamp or the next
  * (i.e., current + 1).
  * 
  * @author benni
  * 
  */
 public class ReadableFilesBatchGenerator extends BatchGenerator {
 
 	public static enum FilenameIndexType {
 		CURRENT_TIMESTAMP, NEXT_TIMESTAMP
 	}
 
 	private String dir;
 
 	private String prefix;
 
 	private String suffix;
 
 	private FilenameIndexType filenameType;
 
 	public ReadableFilesBatchGenerator(String dir, String prefix,
 			FilenameIndexType filenameType, String suffix) {
 		super("ReadableFilesBatchGenerator");
 		this.dir = dir;
 		this.prefix = prefix;
 		this.filenameType = filenameType;
		this.prefix = prefix;
 	}
 
 	@Override
 	public Batch generate(Graph g) {
 		return BatchReader.read(this.dir, this.getFilename(g), g);
 	}
 
 	protected String getFilename(Graph g) {
 		switch (this.filenameType) {
 		case CURRENT_TIMESTAMP:
 			return this.prefix + g.getTimestamp() + this.suffix;
 		case NEXT_TIMESTAMP:
 			return this.prefix + (g.getTimestamp() + 1) + this.suffix;
 		default:
 			return null;
 		}
 	}
 
 	@Override
 	public void reset() {
 	}
 
 	@Override
 	public boolean isFurtherBatchPossible(Graph g) {
 		return (new File(this.dir + this.getFilename(g))).exists();
 	}
 
 }
