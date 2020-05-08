 package ar.edu.itba.pdc.duta.net.buffer.internal;
 
 import java.io.IOException;
 import java.nio.ByteBuffer;
 import java.nio.channels.FileChannel;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 
 
 public class DynamicDataBuffer extends AbstractDataBuffer {
 
 	private static final Logger logger = Logger.getLogger(DynamicDataBuffer.class);
 
 	private List<ByteBuffer> buffer;
 
 	private int capacity = 0x1000; // 4 KB �� puny humans
 
 
 	public DynamicDataBuffer() {
 
 		buffer = new ArrayList<ByteBuffer>();
 		buffer.add(ByteBuffer.allocate(capacity));
 	}
 
 	public DynamicDataBuffer(int capacity) {
 
 		this.capacity = capacity;
 		buffer = new ArrayList<ByteBuffer>();
 		buffer.add(ByteBuffer.allocate(capacity));
 	}
 
 	public DynamicDataBuffer(ByteBuffer buffer) {
 
 		capacity = buffer.capacity();
 		writeIndex = capacity;
 		this.buffer = new ArrayList<ByteBuffer>();
 		this.buffer.add(buffer);
 	}
 
 	@Override
 	public void read(int count) throws IOException {
 
 		if (count == 0) {
 			return;
 		}
 
 		int startIndex = writeIndex / capacity;
 		int startPos = writeIndex % capacity;
 		int endIndex = (writeIndex + count - 1) / capacity;
 		int endPos = (capacity + writeIndex + count - 1) % capacity + 1;
 		int readBytes;
 
 		if (startIndex >= buffer.size()) {
 			buffer.add(ByteBuffer.allocate(capacity));
 		}
 
 		ByteBuffer aux = buffer.get(startIndex);
 		aux.limit(capacity);
 		aux.position(startPos);
 
 		if (startIndex == endIndex) {
 
 			aux.limit(endPos);
 			readBytes = inputChannel.read(aux);
 
 			if (readBytes == -1) {
 				throw new IOException("The input channel just gave an invalid read");
 			} else {
 				writeIndex += readBytes;
 			}
 			return;
 		}
 
 		aux.limit(capacity);
 		readBytes = inputChannel.read(aux);
 
 		if (readBytes == -1) {
 			throw new IOException("The input channel just gave an invalid read");
 		} else {
 			writeIndex += readBytes;
 		}
 
 		if (readBytes < capacity - startPos) {
 			return;
 		}
 
 		for (int index = startIndex + 1; index < endIndex; index++) {
 
 			if (index >= buffer.size()) {
 				buffer.add(ByteBuffer.allocate(capacity));
 			}
 
 			aux = buffer.get(index);
 			aux.position(0);
 			aux.limit(capacity);
 			readBytes = inputChannel.read(aux);
 
 			if (readBytes == -1) {
 				throw new IOException("The input channel just gave an invalid read");
 			} else {
 				writeIndex += readBytes;
 			}
 
 			if (readBytes < capacity) {
 				return;
 			}
 		}
 
 		if (endIndex >= buffer.size()) {
 			buffer.add(ByteBuffer.allocate(capacity));
 		}
 
 		aux = buffer.get(endIndex);
 		aux.position(0);
 		aux.limit(endPos);
 		readBytes = inputChannel.read(aux);
 
 		if (readBytes > 0) {
 			writeIndex += readBytes;
 		}
 	}
 
 	@Override
 	public void write() throws IOException {
 
 		if (readIndex >= writeIndex) {
 			return;
 		}
 
 		int startIndex = readIndex / capacity;
 		int endIndex = (writeIndex - 1) / capacity;
 		int startPos = readIndex % capacity;
 		int endPos = (capacity + writeIndex - 1) % capacity + 1;
 		int writtenBytes;
 
 		ByteBuffer aux = buffer.get(startIndex);
 		aux.limit(capacity);
 		aux.position(startPos);
 
 		if (startIndex == endIndex) {
 
 			aux.limit(endPos);
 			readIndex += outputChannel.write(aux);
 			return;
 		}
 
 		aux.limit(capacity);
 		writtenBytes = outputChannel.write(aux);
 		
 		if (writtenBytes == -1) {
 			throw new IOException("The output channel just gave an invalid write");
 		} else {
 			readIndex += writtenBytes;
 		}
 
 		if (writtenBytes < capacity - startPos) {
 			return;
 		}
 
 		for (int index = startIndex + 1; index < endIndex; index++) {
 
 			aux = buffer.get(index);
 			aux.position(0);
 			aux.limit(capacity);
 			writtenBytes = outputChannel.write(aux);
 			
 			if (writtenBytes == -1) {
 				throw new IOException("The output channel just gave an invalid write");
 			} else {
 				readIndex += writtenBytes;
 			}
 
 			if (writtenBytes < capacity) {
 				return;
 			}
 		}
 
 		aux = buffer.get(endIndex);
 		aux.position(0);
 		aux.limit(endPos);
 		readIndex += outputChannel.write(aux);
 	}
 
 	@Override
 	public void get(int pos, byte[] buffer, int offset, int count) throws IOException {
 
 		if (count == 0) {
 			return;
 		}
 
 		int startIndex = pos / capacity;
 		int startPos = pos % capacity;
 		int end = Math.min(pos + count, writeIndex);
 		int endIndex = (end - 1) / capacity;
 		int endPos = (capacity + end - 1) % capacity + 1;
 
 		ByteBuffer aux = this.buffer.get(startIndex);
 		aux.limit(capacity);
 		aux.position(startPos);
 
 		if (startIndex == endIndex) {
 
 			aux.limit(endPos);
			aux.get(buffer, offset, end - pos);
 			return;
 		}
 
 		aux.limit(capacity);
 		aux.get(buffer, offset, capacity - startPos);
 		offset += capacity - startPos;
 
 		for (int index = startIndex + 1; index < endIndex; index++) {
 
 			aux = this.buffer.get(index);
 			aux.position(0);
 			aux.limit(capacity);
 			aux.get(buffer, offset, capacity);
 			offset += capacity;
 		}
 
 		aux = this.buffer.get(endIndex);
 		aux.position(0);
 		aux.limit(endPos);
 		aux.get(buffer, offset, endPos);
 	}
 
 	@Override
 	public void collect() {
 
 		buffer = null;
 	}
 
 	public void writeToFile(FileChannel fileChannel) throws IOException{
 
 		if (writeIndex == 0) {
 			return;
 		}
 
 		fileChannel.position(0);
 
 		int endIndex = (writeIndex - 1) / capacity;
 		int endPos = (capacity + writeIndex - 1) % capacity + 1;
 		ByteBuffer aux;
 
 		for (int index = 0; index < endIndex; index++) {
 
 			aux = buffer.get(index);
 			aux.position(0);
 			aux.limit(capacity);
 			fileChannel.write(aux);
 		}
 
 		aux = buffer.get(endIndex);
 		aux.position(0);
 		aux.limit(endPos);
 		fileChannel.write(aux);
 	}
 }
