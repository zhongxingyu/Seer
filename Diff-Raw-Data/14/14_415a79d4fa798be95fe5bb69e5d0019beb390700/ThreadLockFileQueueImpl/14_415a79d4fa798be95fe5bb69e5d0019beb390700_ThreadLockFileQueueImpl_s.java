 package com.macrohuang.fileq.impl;
 
 import java.io.IOException;
 import java.nio.ByteBuffer;
 import java.nio.channels.FileChannel.MapMode;
 import java.util.Arrays;
 import java.util.concurrent.locks.ReentrantLock;
 
 import com.macrohuang.fileq.FileQueue;
 import com.macrohuang.fileq.conf.Config;
 import com.macrohuang.fileq.exception.CheckSumFailException;
 import com.macrohuang.fileq.util.NumberBytesConvertUtil;
 
 /**
  * You should always keep this class Singleton in your application, or there will be something damage! To use multion, use {@link com.macrohuang.fileq.MultionFileQueueImpl} instead.
  * @author macro
  *
  * @param <E>
  */
 public class ThreadLockFileQueueImpl<E> extends AbstractFileQueueImpl<E>
 		implements FileQueue<E> {
 	private final ReentrantLock writeLock = new ReentrantLock();
 	private final ReentrantLock readLock = new ReentrantLock();
 
 	public ThreadLockFileQueueImpl(Config config) {
 		super(config);
 	}
 
 	 @Override
 	public void add(E e) {
 		byte[] objBytes = codec.encode(e);
 		byte[] metaBytes = new byte[META_SIZE];
 		Arrays.fill(metaBytes, (byte) 0xff);
 		System.arraycopy(LEADING_HEAD, 0, metaBytes, 0, 4);
 		System.arraycopy(NumberBytesConvertUtil.int2ByteArr(objBytes.length), 0, metaBytes, 4, 4);
 		byte[] checkSum = new byte[CHECKSUM_SIZE];
 		Arrays.fill(checkSum, (byte) 0xff);
 		System.arraycopy(NumberBytesConvertUtil.int2ByteArr(META_SIZE + objBytes.length), 0, checkSum, 0,
 				NumberBytesConvertUtil.int2ByteArr(META_SIZE + objBytes.length).length);
 		long size = metaBytes.length + objBytes.length + checkSum.length;
 		try {
 			writeLock.lock();
 			// Current object exceed the file size, expand it first.
 			if (writeMappedByteBuffer.position() + size > writeMappedByteBuffer.limit()) {
 				writeMappedByteBuffer = writeChannel.map(MapMode.READ_WRITE, writeMappedByteBuffer.position(), size);
 			}
 			writeMappedByteBuffer.put(metaBytes);
 			writeMappedByteBuffer.put(objBytes);
 			writeMappedByteBuffer.put(checkSum);
 			if (writePosition.addAndGet(size) >= getFileSize()) {
 				increateWriteNumber();
 			}
 			updateWriteMeta();
 		} catch (IOException e1) {
 			e1.printStackTrace();
 		} finally {
 			writeLock.unlock();
 		}
 	}
 
 	@Override
 	protected E peekInner(boolean remove, long timeout) {
         try {
         	if (remove){
         		readLock.lock();
         	}
             long position = readPosition.get();
             ByteBuffer metaBuffer = ByteBuffer.allocate(META_SIZE);
 			readChannel.read(metaBuffer, position);
 			metaBuffer.flip();
             if (metaBuffer.getInt()!= magic) {// data error,try to read from the right position.
             	if (!readLock.isHeldByCurrentThread())
             		readLock.lock();
             	position = readPosition.incrementAndGet();
             	boolean success = false;
             	while(!success){
            		if (position == writePosition.get()){
             			if (timeout>0){
 							Thread.sleep(timeout);
            				if (position == writePosition.get()){
             					return null;
             				}
             			}else{
							Thread.sleep(100);
             			}
					} else if (position >= readChannel.size()) {
 						increateReadNumber();
             		}
             		metaBuffer.clear();
 					readChannel.read(metaBuffer, position);
 					metaBuffer.flip();
                     if (metaBuffer.getInt()==magic){
                     	success=true;
                     }else{
                     	position=readPosition.incrementAndGet();
                     }
             	}
             }
             int objLength = metaBuffer.getInt();
             ByteBuffer objBuffer = ByteBuffer.allocate(objLength);
 			readChannel.read(objBuffer, position + META_SIZE);
             E obj = codec.decode(objBuffer.array());
             ByteBuffer checksumBuffer = ByteBuffer.allocate(CHECKSUM_SIZE);
 			readChannel.read(checksumBuffer, position + META_SIZE + objLength);
             checksumBuffer.flip();
             if (checksumBuffer.getInt() != META_SIZE + objLength) {// checksum
                 throw new CheckSumFailException();
             }
             if (remove){
 				readPosition.addAndGet(META_SIZE + objLength + CHECKSUM_SIZE);
 				if (readPosition.get() >= readChannel.size()) {
 					increateReadNumber();
 				}
 				updateReadMeta();
             }
             return obj;
         } catch (Exception e) {
             e.printStackTrace();
         } finally {
         	if (readLock.isHeldByCurrentThread())
         		readLock.unlock();
         }
         return null;
     }
 }
