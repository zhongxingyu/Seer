 package edu.berkeley.icsi.wlgen;
 
 import java.io.DataInput;
 import java.io.DataOutput;
 import java.io.IOException;
 
 import edu.berkeley.icsi.wlgen.datagen.DataGeneratorOutput;
 import eu.stratosphere.pact.common.contract.DataDistribution;
 import eu.stratosphere.pact.common.type.PactRecord;
 import eu.stratosphere.pact.common.type.base.PactString;
 
 public class ReduceDataDistribution implements DataDistribution {
 
 	private PactRecord[] boundaries;
 
 	public ReduceDataDistribution(final double[] dataDistribution) {
 
 		this.boundaries = new PactRecord[dataDistribution.length];
 		for (int i = 0; i < dataDistribution.length; ++i) {
 			this.boundaries[i] = toBucketBoundary(dataDistribution[i]);
 		}
 	}
 
 	public ReduceDataDistribution() {
 	}
 
 	private static PactRecord toBucketBoundary(final double val) {
 
 		long l = (((long) (val * (double) Integer.MAX_VALUE * 2)) & 0xFFFFFFFF);
 		final StringBuilder sb = new StringBuilder(8);
 
 		for (int j = 0; j < 8; ++j) {
 			final int pos = (int) (0x000f & l);
 			sb.append(DataGeneratorOutput.KEY_ALPHABET[pos]);
 			l = l >> 4;
 		}
 
 		final PactRecord record = new PactRecord(1);
 		final PactString str = new PactString(sb.reverse());
 		record.setField(0, str);
 
 		return record;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public PactRecord getBucketBoundary(final int bucketNum, final int totalNumBuckets) {
 
		if (this.boundaries.length != (totalNumBuckets -1)) {
 			throw new IllegalStateException("Number of buckets do not match (" + totalNumBuckets + ", expected "
 				+ this.boundaries.length + ")");
 		}
 
 		return this.boundaries[bucketNum];
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void write(final DataOutput out) throws IOException {
 
 		out.writeInt(this.boundaries.length);
 		for (int i = 0; i < this.boundaries.length; ++i) {
 			this.boundaries[i].write(out);
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void read(final DataInput in) throws IOException {
 
 		final int length = in.readInt();
 		this.boundaries = new PactRecord[length];
 		for (int i = 0; i < length; ++i) {
 			this.boundaries[i] = new PactRecord();
 			this.boundaries[i].read(in);
 		}
 	}
 
 }
