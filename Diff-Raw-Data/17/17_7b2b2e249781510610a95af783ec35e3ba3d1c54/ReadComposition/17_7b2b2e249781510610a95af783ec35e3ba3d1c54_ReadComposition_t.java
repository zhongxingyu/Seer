 package net.malariagen.gatk.coverage;
 
 import java.util.Map;
 import java.util.Set;
 
 import net.sf.samtools.SAMRecord;
 
 import org.broadinstitute.sting.gatk.contexts.ReferenceContext;
 import org.broadinstitute.sting.gatk.refdata.ReadMetaDataTracker;
 import org.broadinstitute.sting.gatk.walkers.ReadWalker;
 import org.broadinstitute.sting.utils.sam.GATKSAMRecord;
 
 public class ReadComposition extends ReadWalker<SAMRecord,net.malariagen.gatk.coverage.ReadComposition.CompositionAccumulator> {
 
 	
 	@Override
 	public SAMRecord map(ReferenceContext ref, GATKSAMRecord read,
 			ReadMetaDataTracker metaDataTracker) {
 		return null;
 	}
 
 	@Override
 	public CompositionAccumulator reduceInit() {
 	
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public CompositionAccumulator reduce(SAMRecord value,
 			CompositionAccumulator sum) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public class CompositionAccumulator {
 		
 		Map<String,Set<Composition>> perGroup;
 		
 		public CompositionAccumulator(Set<String> groupNames) {
 			
 		}
 	}	
 
 	
 	private class Composition {
 		public int gcCount;
 		public int readLength;
 		public long readCount;
 		private int hashCode;
 		
 		public Composition(int readLength, int gcCount) {
 			this.gcCount++;
 			this.readLength++;
 			this.hashCode = _hashCode();
 		}
 		
 		private int _hashCode () {
 			return Integer.valueOf(gcCount * readLength).hashCode();
 		}
 		
 		public boolean equals(Object o) {
 			if (o == null) return false;
 			if (!(o instanceof Composition)) return false;
 			Composition c= (Composition) o;
 			return (c.gcCount == this.gcCount) && (c.readLength == this.readLength);
 		}
 		
 		public int compareTo(Composition c) {
 			if (c == null)
 				throw new IllegalArgumentException("cannot compare to null");
			int result = this.gcCount - c.gcCount;
 			if (result != 0) return result;
			return this.readLength - c.readLength;
 		}
 		
 		public void mergeIn(Composition c) {
 			if (c == null)
 				throw new IllegalArgumentException("cannot merge in a null composition");
 			if (!this.equals(c))
 				throw new IllegalArgumentException("trying to mix different compositions");
 			this.readCount += c.readCount;
 		}
 		
 	}
 }
