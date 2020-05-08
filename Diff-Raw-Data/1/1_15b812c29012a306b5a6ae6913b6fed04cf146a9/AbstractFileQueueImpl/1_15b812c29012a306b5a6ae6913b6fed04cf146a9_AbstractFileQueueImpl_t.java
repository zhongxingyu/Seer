 package com.macrohuang.fileq.impl;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.RandomAccessFile;
 import java.nio.MappedByteBuffer;
 import java.nio.channels.FileChannel;
 import java.nio.channels.FileChannel.MapMode;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.atomic.AtomicInteger;
 import java.util.concurrent.atomic.AtomicLong;
 
 import com.macrohuang.fileq.FileQueue;
 import com.macrohuang.fileq.codec.Codec;
 import com.macrohuang.fileq.codec.impl.KryoCodec;
 import com.macrohuang.fileq.conf.Config;
 import com.macrohuang.fileq.util.FileUtil;
 import com.macrohuang.fileq.util.NumberBytesConvertUtil;
 
 public abstract class AbstractFileQueueImpl<E> implements FileQueue<E> {
 	private Config config;
 	private final AtomicInteger objectCount;
 	protected Codec codec;
 	private final AtomicLong writeNumber;
 	protected AtomicLong writePosition;
 	private final AtomicLong readNumber;
 	protected AtomicLong readPosition;
 	protected MappedByteBuffer writeMappedByteBuffer;
 	protected static final int META_SIZE = 16;
 	protected static final int CHECKSUM_SIZE = 16;
 	protected static final int magic = 1314520;
 	protected static final byte[] LEADING_HEAD = NumberBytesConvertUtil.int2ByteArr(magic);
 	private MappedByteBuffer queueMetaBuffer;
 	private RandomAccessFile readFile;
 	private RandomAccessFile writeFile;
 	protected FileChannel readChannel;
 	protected FileChannel writeChannel;
 	private FileChannel metaChannel;
 	private RandomAccessFile metaAccessFile;
 	private static final int SIZE_OF_QUEUE_META = 46;
 	
 	public enum MetaOffset {
 		WriteNumberName(0), WriteNumber(2), WritePositionName(10), WritePosition(12), ReadNumberName(20), ReadNumber(22), ReadPositionName(30), ReadPosition(
 				32), ObjectCountName(40), ObjectCount(42);
 		private MetaOffset(int offset){
 			this.offset = offset;
 		}
 		public int getOffset() {
 			return offset;
 		}
 		public void setOffset(int offset) {
 			this.offset = offset;
 		}
 
 		private int offset;
 	}
 
 	public AbstractFileQueueImpl(Config config) {
 		codec = config.getCodec() == null ? new KryoCodec() : config.getCodec();
 		objectCount = new AtomicInteger(0);
 		writeNumber = new AtomicLong(0);
 		readNumber = new AtomicLong(0);
 		writePosition = new AtomicLong(0);
 		readPosition = new AtomicLong(0);
 		this.config = config;
 		init();
 	}
 
 	private void init() {
 		try {
 			if (config.isInit()) {
 				File basePath = new File(config.getBasePath());
 				delete(basePath);
 			}
 			boolean isNew = FileUtil.isMetaExists(config);
 			metaAccessFile = new RandomAccessFile(FileUtil.getMetaFile(config), "rw");
 			metaChannel = metaAccessFile.getChannel();
 			queueMetaBuffer = metaChannel.map(MapMode.READ_WRITE, 0, SIZE_OF_QUEUE_META);
 			if (!isNew && !config.isInit()) {
 				writeNumber.set(queueMetaBuffer.getLong(MetaOffset.WriteNumber.offset));
 				writePosition.set(queueMetaBuffer.getLong(MetaOffset.WritePosition.offset));
 				readNumber.set(queueMetaBuffer.getLong(MetaOffset.ReadNumber.offset));
 				readPosition.set(queueMetaBuffer.getLong(MetaOffset.ReadPosition.offset));
 				objectCount.set(queueMetaBuffer.getInt(MetaOffset.ObjectCount.offset));
 			} else {
 				queueMetaBuffer.put("WN".getBytes());
 				queueMetaBuffer.put(NumberBytesConvertUtil.long2ByteArr(0L));
 				queueMetaBuffer.put("WP".getBytes());
 				queueMetaBuffer.put(NumberBytesConvertUtil.long2ByteArr(0L));
 				queueMetaBuffer.put("RN".getBytes());
 				queueMetaBuffer.put(NumberBytesConvertUtil.long2ByteArr(0L));
 				queueMetaBuffer.put("RP".getBytes());
 				queueMetaBuffer.put(NumberBytesConvertUtil.long2ByteArr(0L));
 				queueMetaBuffer.put("OC".getBytes());
 				queueMetaBuffer.put(NumberBytesConvertUtil.int2ByteArr(0));
 			}
 			writeFile = new RandomAccessFile(FileUtil.getDataFile(config, writeNumber.get()), "rw");
 			writeChannel = writeFile.getChannel();
 			writeMappedByteBuffer = writeChannel.map(MapMode.READ_WRITE, 0, config.getFileSize());
			writeMappedByteBuffer.position(Long.valueOf(writePosition.get()).intValue());
 
 			readFile = new RandomAccessFile(FileUtil.getDataFile(config, readNumber.get()), "r");
 			readChannel = readFile.getChannel();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public Config getConfig() {
 		return config;
 	}
 
 	public void setConfig(Config config) {
 		this.config = config;
 		init();
 	}
 
 	@Override
 	public boolean remain() {
 		return readPosition == writePosition;
 	}
 
 	@Override
 	public int size() {
 		return objectCount.get();
 	}
 
 	@Override
 	public abstract void add(E e);
 
 	protected abstract E peekInner(boolean remove, long timeout);
 
 	@Override
 	public E remove() {
 		if (objectCount.get() == 0)
 			return null;
 		return peekInner(true, 0L);
 	}
 
 	@Override
 	public E peek() {
 		if (objectCount.get() == 0)
 			return null;
 		return peekInner(false, 0L);
 	}
 
 	@Override
 	public E peek(long timeout, TimeUnit timeUnit) throws InterruptedException {
 		if (objectCount.get() == 0) {
 			if (timeout > 0) {
 				Thread.sleep(TimeUnit.MILLISECONDS.convert(timeout, timeUnit));
 			} else {
 				while (objectCount.get() == 0) {
 					Thread.sleep(100);
 				}
 			}
 		}
 		if (objectCount.get() == 0) {
 			return null;
 		}
 		return peekInner(false, TimeUnit.MILLISECONDS.convert(timeout, timeUnit));
 	}
 
 	@Override
 	public void clear() {
 		objectCount.getAndSet(0);
 		readPosition.getAndSet(writePosition.get());
 	}
 
 	@Override
 	public E take() throws InterruptedException {
 		while (objectCount.get() == 0) {
 			Thread.sleep(100);
 		}
 		return peekInner(true, 0L);
 	}
 
 	@Override
 	public E take(long timeout, TimeUnit unit) throws InterruptedException {
 		if (objectCount.get() == 0) {
 			Thread.sleep(TimeUnit.MILLISECONDS.convert(timeout, unit));
 		}
 		if (objectCount.get() == 0) {
 			return null;
 		}
 		return peekInner(true, TimeUnit.MILLISECONDS.convert(timeout, unit));
 	}
 
 	@Override
 	public void close() {
 		try {
 			metaAccessFile.close();
 			metaChannel.close();
 			readChannel.close();
 			readFile.close();
 			writeChannel.close();
 			writeFile.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public boolean delete(File file) {
 		boolean success = true;
 		if (file.isDirectory()) {
 			for (File file2 : file.listFiles()) {
 				success &= delete(file2);
 			}
 		}
 		success &= file.delete();
 		return success;
 	}
 
 	@Override
 	public boolean delete() {
 		close();
 		return delete(new File(config.getBasePath()));
 	}
 
 	protected void increateWriteNumber() throws IOException {
 		queueMetaBuffer.putLong(MetaOffset.WriteNumber.offset, writeNumber.incrementAndGet());
 		writeChannel.close();
 		writeFile.close();
 		writeFile = new RandomAccessFile(FileUtil.getDataFile(config, writeNumber.get()), "rw");
 		writeChannel = writeFile.getChannel();
 		writePosition.set(0L);
 		queueMetaBuffer.putLong(MetaOffset.WritePosition.offset, writePosition.get());
 		writeMappedByteBuffer = writeChannel.map(MapMode.READ_WRITE, 0, config.getFileSize());
 	}
 
 	private boolean backupDataFile() {
 		if (config.isBackup()) {
 			try {
 				FileOutputStream backupStream = new FileOutputStream(FileUtil.getBakFile(config, readNumber.get()));
 				FileChannel targetChannel = backupStream.getChannel();
 				readChannel.transferTo(0, readChannel.size(), targetChannel);
 				backupStream.close();
 				targetChannel.close();
 			} catch (Exception e) {
 				return false;
 			}
 		}
 		return true;
 	}
 
 	protected void increateReadNumber() throws IOException {
 		boolean backup = backupDataFile();
 		File toDelFile = FileUtil.getDataFile(config, readNumber.get());
 		queueMetaBuffer.putLong(MetaOffset.ReadNumber.offset, readNumber.incrementAndGet());
 		readChannel.close();
 		readFile.close();
 		readFile = new RandomAccessFile(FileUtil.getDataFile(config, readNumber.get()), "r");
 		readChannel = readFile.getChannel();
 		readPosition.set(0L);
 		queueMetaBuffer.putLong(MetaOffset.ReadPosition.offset, readPosition.get());
 		if (backup)
 			toDelFile.delete();
 	}
 
 	protected final void updateWriteMeta() {
 		queueMetaBuffer.putLong(MetaOffset.WritePosition.offset, writePosition.get());
 		queueMetaBuffer.putInt(MetaOffset.ObjectCount.offset, objectCount.incrementAndGet());
 	}
 
 	protected final void updateReadMeta() {
 		queueMetaBuffer.putLong(MetaOffset.ReadPosition.offset, readPosition.get());
 		queueMetaBuffer.putInt(MetaOffset.ObjectCount.offset, objectCount.decrementAndGet());
 	}
 
 	protected final int getFileSize() {
 		return config.getFileSize();
 	}
 }
