 package org.acaro.sketches;
 
 import java.io.BufferedOutputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.RandomAccessFile;
 import java.nio.BufferUnderflowException;
 import java.nio.ByteBuffer;
 import java.nio.MappedByteBuffer;
 import java.nio.channels.FileChannel;
 import java.nio.channels.FileChannel.MapMode;
 import java.util.Comparator;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.TreeMap;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * 
  * @author Claudio Martella
  * 
  * Replay of the log to MindSketches. 
  * If no file is found an empty MindSketch is returned ready to be filled and used.
  *
  */
 
 public class SketchesHelper {
 	final static Logger logger = LoggerFactory.getLogger(SketchesHelper.class);
 	private static final String EXTENSION = ".sb";
 
 	public static MindSketches loadSketchBook(String file) throws IOException {
 		MindSketches memory = new MindSketches();
 		RandomAccessFile book = null;
 		FileChannel ch = null;
 		int loaded=0;
 		
 		try {
 			book = new RandomAccessFile(file, "rw");
 			ch = book.getChannel();
 			MappedByteBuffer buffer = ch.map(MapMode.READ_WRITE, 0, ch.size());
 			buffer.load();
 			
 			while (buffer.hasRemaining()) {
 				byte type = buffer.get();
 				long ts = buffer.getLong();
 				short keySize = buffer.getShort();
 				int valueSize = buffer.getInt();
 				
 				Sketch s;
 				byte[] key, value;
 				
 				switch (type) {
 				
 				case Sketch.THROWUP: 
 
 					key = new byte[keySize];
 					value = new byte[valueSize];
 					buffer.get(key); 
 					buffer.get(value);
 					s = new Throwup(key, value, ts);
 					break;
 
 				case Sketch.BUFF:
 
 					key = new byte[keySize];
 					buffer.get(key);
 					s = new Buff(key, ts); 
 					break;
 
 				default: throw new IOException("Corrupted SketchBook: read unknown type: " + type); 
 				}
 				memory.put(s.getKey(), s);
 				loaded++;
 			}
 			
 			book.close();
 		} catch (BufferUnderflowException e) {
 			logger.info("Truncated file, we probably died without synching correctly");
 			ch.truncate(ch.position());
 			ch.force(true);
 			book.close();
 		}
 
 		logger.debug(loaded + " loaded: " + memory.getSize());
 		
 		return memory;
 	}
 	
 	public static String getFilename(String path, String name) {
 		return path + "/" + name + EXTENSION;
 	}
 	
 	public static void burnMindSketches(MindSketches memory, String filename) throws IOException {
 		long start = System.currentTimeMillis();
 		logger.info("burning started: " + start);
 		
 		TreeMap<byte[], Sketch> sortedMap = new TreeMap<byte[], Sketch>(new Comparator<byte[]>() {
 			// lexicographic comparison
 			public int compare(byte[] left, byte[] right) {
 				for (int i = 0, j = 0; i < left.length && j < right.length; i++, j++) {
 					int a = (left[i] & 0xff);
 					int b = (right[j] & 0xff);
 					if (a != b) {
 						return a - b;
 					}
 				}
 				return left.length - right.length;
 			}
 		});
 		sortedMap.putAll(memory.getMap());
 		logger.debug("MindSketches is sorted");
 		
 		FileOutputStream fos = new FileOutputStream(filename, false);
 		BufferedOutputStream bos = new BufferedOutputStream(fos, 1024*1024);
 		FileChannel fc = fos.getChannel();
 		
 		ByteBuffer header = ByteBuffer.allocate(Wall.HEADER_SIZE);
 		header.put(Wall.DIRTY);
 		header.putLong(sortedMap.size());
 		header.rewind();
 		fc.write(header);
 		
 		for (Entry<byte[], Sketch> entry: sortedMap.entrySet()) {
 			//logger.debug("burning " + entry.getKey());
 			for (ByteBuffer b: entry.getValue().getBytes()) 
 				bos.write(b.array());
 		}
 		
 		bos.flush();
		fc.position(0);
 		header.put(0, Wall.CLEAN);
 		header.rewind();
 		fc.write(header);
 		fos.close();
 		
 		logger.info("burning finished: " + (System.currentTimeMillis()-start));
 	}
 }
