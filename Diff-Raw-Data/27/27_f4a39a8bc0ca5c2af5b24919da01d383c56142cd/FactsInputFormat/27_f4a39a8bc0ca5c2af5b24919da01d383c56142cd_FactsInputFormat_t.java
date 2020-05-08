 /*
  * Copyright 2010 Softgress - http://www.softgress.com/
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *      http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package eu.larkc.iris.storage;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.hadoop.io.LongWritable;
 import org.apache.hadoop.mapred.InputFormat;
 import org.apache.hadoop.mapred.InputSplit;
 import org.apache.hadoop.mapred.JobConf;
 import org.deri.iris.api.basics.IAtom;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 
 public abstract class FactsInputFormat<T extends AtomRecord> implements
 		InputFormat<LongWritable, T> {
 
 	private static final Logger logger = LoggerFactory.getLogger(FactsInputFormat.class);
 	
 	public static int MAX_BLOCK_SIZE = 1024;
 	
 	public static class NullAtomWritable extends AtomRecord {
 
 		@Override
 		public void read(IAtom atom) {
 		}
 
 		@Override
 		public void write(FactsStorage storage) {
 		}
 	}
 
 	public abstract long getTotalSize(JobConf jobConf);
 
 	@Override
 	public InputSplit[] getSplits(JobConf job, int numSplits)
 			throws IOException {
 		List<FactsInputSplit> splits = new ArrayList<FactsInputSplit>();
 		
 		long size = getTotalSize(job);
 		splits.addAll(createSplits(job, size));
 		
 		logger.info("returning " + splits.size() + " splits");
 		return splits.toArray(new FactsInputSplit[0]);
 	}
 
 	protected List<FactsInputSplit> createSplits(JobConf jobConf, long size) {
 		List<FactsInputSplit> splits = new ArrayList<FactsInputSplit>();
 		int nbSplits = (int) ((size / MAX_BLOCK_SIZE) + 1);
 		for (int i = 0; i < nbSplits; i++) {
 			long theOffset = getOffset(i, 0);
 			splits.add(new FactsInputSplit(Math.min(size - theOffset, MAX_BLOCK_SIZE), theOffset));
 		}
 		return splits;
 	}
 
 	public long getOffset(int nbDoneSplits, long lastOffset) {
 		return nbDoneSplits * MAX_BLOCK_SIZE;
 	}
 	
 }
