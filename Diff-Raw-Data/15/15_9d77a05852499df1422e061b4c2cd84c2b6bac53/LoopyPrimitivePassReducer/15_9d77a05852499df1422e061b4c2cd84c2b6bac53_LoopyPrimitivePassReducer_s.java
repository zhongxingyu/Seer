 package edu.berkeley.gamesman.loopyhadoop;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.Random;
 
 import edu.berkeley.gamesman.core.Configuration;
 import edu.berkeley.gamesman.core.Record;
 import edu.berkeley.gamesman.core.State;
 import edu.berkeley.gamesman.core.Value;
 import edu.berkeley.gamesman.database.DatabaseHandle;
 import edu.berkeley.gamesman.database.FileDatabase;
 import edu.berkeley.gamesman.game.Game;
 import edu.berkeley.gamesman.parallel.RangeFile;
 import org.apache.hadoop.fs.FileSystem;
 import org.apache.hadoop.fs.LocalFileSystem;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.io.ArrayFile;
 import org.apache.hadoop.io.IntWritable;
 import org.apache.hadoop.io.LongWritable;
 import org.apache.hadoop.io.SequenceFile;
 import org.apache.hadoop.mapreduce.Reducer;
 
 public class LoopyPrimitivePassReducer<S extends State> extends
 		Reducer<RangeFile, LongWritable, LongWritable, IntWritable> {
 	private FileSystem fs;
 	private Configuration conf;
 	private Game<S> game;
 	private final Random rand = new Random();
 	private IntWritable zero = new IntWritable(0);
 	private LongWritable hashLongWritable = new LongWritable(0);
 	private LongWritable recordLongWritable = new LongWritable(0);
 	private Path primitivePath;
 	private S[] childStates;
 	private S position;
 
 	@Override
 	public void setup(Context context) {
 		try {
 			org.apache.hadoop.conf.Configuration hadoopConf = context
 					.getConfiguration();
 			conf = Configuration.deserialize(hadoopConf
 					.get("gamesman.configuration"));
 			fs = FileSystem.get(hadoopConf);
 			game = conf.getCheckedGame();
 			childStates = game.newStateArray(game.maxChildren());
 			position = game.newState();
 			primitivePath = new Path(hadoopConf.get("primitive.output"));
 		} catch (IOException e) {
 			throw new Error(e);
 		} catch (ClassNotFoundException e) {
 			throw new Error(e);
 		}
 	}
 
 	// thank you hadoop
 	@Override
 	public void reduce(RangeFile rangeFile, Iterable<LongWritable> hashes,
 			Context context) {
 		ArrayList<Long> sortedHashes = new ArrayList<Long>();
 
 		for (LongWritable hash : hashes) {
 			sortedHashes.add(hash.get());
 		}
 
 		Collections.sort(sortedHashes);
 
 		writeNumChildren(rangeFile, sortedHashes, context);
 		markDatabase(rangeFile, context, sortedHashes);
 	}
 
 	private void writeNumChildren(RangeFile rangeFile,
 			ArrayList<Long> sortedHashes, Context context) {
 		try {
 			long rangeStart = rangeFile.myRange.firstRecord;
 			long numRecords = rangeFile.myRange.numRecords;
 
 			Path rangeFileDBPath = rangeFile.myFile.getPath();
 
			LocalFileSystem lfs = new LocalFileSystem();
 
 			String stringPath = lfs.pathToFile(rangeFileDBPath).getPath()
 					+ "_numChildren";
 			String localStringPath = stringPath + "_local";
 
 			int[] range = new int[(int) numRecords];
 
 			Path localPath = new Path(localStringPath);
 
 			Path hdfsPath = new Path(stringPath);
 
 			if (fs.exists(hdfsPath)) {
 				fs.copyToLocalFile(hdfsPath, localPath);
 
 				ArrayFile.Reader arrayReader = new ArrayFile.Reader(fs,
 						localStringPath, context.getConfiguration());
 
 				IntWritable temp = new IntWritable();
 				for (int n = 0; n < range.length; n++) {
 					arrayReader.get(n, temp);
 					range[n] = temp.get();
 					if (n < 20) {
 						System.out.println(range[n]);
 					}
 					// get the num children from the file
 				}
 
 				arrayReader.close();
 
 				lfs.delete(localPath, true);
 				// get rid of the old file,we're making a new one
 			}
 
 			ArrayFile.Writer arrayWriter = new ArrayFile.Writer(context
 					.getConfiguration(), fs, localStringPath, IntWritable.class);
 
 			Iterator<Long> hashIter = sortedHashes.iterator();
 			long nextHash = hashIter.next();
 			for (int n = 0; n < range.length; n++) {
 				IntWritable numChildren = new IntWritable();
 
 				if (nextHash == rangeStart + n) {// we're writing to a hash that
 					// we're filling in here
 					game.hashToState(nextHash, position);
 					numChildren.set(game.validMoves(position, childStates));
 
 					if (hashIter.hasNext()) {
 						nextHash = hashIter.next();
 					}
 				} else {
 					numChildren.set(range[n]);
 				}
 
 				arrayWriter.append(numChildren);
 			}
 
 			arrayWriter.close();
 
 			Path tempPath = new Path(stringPath + "_" + rand.nextLong());
 			// use a random long to prevent collisions in the expensive copy
 			// step
 
 			// lfs.pathToFile(lfs.getChecksumFile(localPath)).delete();
 
 			fs.moveFromLocalFile(localPath, tempPath);
			// copy the written array file to hdfs
 			fs.rename(tempPath, hdfsPath);
 			// rename to complete process
 
 		} catch (IOException io) {
 			throw new Error(io);
 		}
 	}
 
 	private void markDatabase(RangeFile rangeFile, Context context,
 			ArrayList<Long> sortedHashes) throws Error {
 		try {
 			Path path = rangeFile.myFile.getPath();
 
 			LocalFileSystem lfs = new LocalFileSystem();
 
 			String stringPath = lfs.pathToFile(path).getPath();
 			String localStringPath = stringPath + "_local";
 
 			Path localPath = new Path(localStringPath);
 			fs.copyToLocalFile(path, localPath);
 
 			FileDatabase database = new FileDatabase(localStringPath);
 
 			DatabaseHandle readHandle = database.getHandle(true);
 			DatabaseHandle writeHandle = database.getHandle(false);
 
 			Record record = game.newRecord();
 			S gameState = game.newState();
 
 			Path primitiveFile = new Path(primitivePath, "range"
 					+ rangeFile.myRange.firstRecord
 					+ "to"
 					+ (rangeFile.myRange.firstRecord
 							+ rangeFile.myRange.numRecords - 1));
 			SequenceFile.Writer primitiveFileWriter = SequenceFile
 					.createWriter(fs, context.getConfiguration(),
 							primitiveFile, LongWritable.class,
 							LongWritable.class);
 
 			/*
 			 * all hashes are going to be in the same file so this goes outside
 			 * the loop
 			 */
 			boolean changesMade = false;
 			for (Long hash : sortedHashes) {
 				game.hashToState(hash, gameState);
 				long recordHash = database.readRecord(readHandle, hash);
 				game.longToRecord(gameState, recordHash, record);
 				/* get the record associated with this hash */
 
 				boolean visited = record.value != Value.IMPOSSIBLE;
 
 				if (!visited) {
 					hashLongWritable.set(hash);
 
 					Value primitiveValue = game.primitiveValue(gameState);
 					if (primitiveValue == Value.UNDECIDED) {
 						// value is not primitive
 						record.value = Value.DRAW;
 					} else // primitive
 					{
 						recordLongWritable.set(recordHash);
 						primitiveFileWriter.append(hashLongWritable,
 								recordLongWritable);
 						record.value = primitiveValue;
 						record.remoteness = 0;
 						// we have to deal with this during the solve for
 						// non-primitives?
 					}
 					recordHash = game.recordToLong(gameState, record);
 					database.writeRecord(writeHandle, hash, recordHash);
 					// write this record to the database
 					changesMade = true;
 					context.write(hashLongWritable, zero);
 				}
 			}
 
 			primitiveFileWriter.close();
 			database.close();
 
 			if (changesMade) {
 				Path tempPath = new Path(stringPath + "_" + rand.nextLong());
 				// use a random long to prevent collisions in the expensive copy
 				// step
 				lfs.pathToFile(lfs.getChecksumFile(localPath)).delete();
 
 				fs.moveFromLocalFile(localPath, tempPath);
 				// copy the written database to hdfs
 				fs.rename(tempPath, path);
 				// rename to complete process
 			}
 		} catch (IOException e) {
 			throw new Error(e);
 		} catch (ClassNotFoundException e) {
 			throw new Error(e);
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 	}
 }
