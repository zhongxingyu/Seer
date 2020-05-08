 package org.walkmanz.gardenz.store.linked;
 
 import org.walkmanz.gardenz.store.BufferedDataFile;
 import org.walkmanz.gardenz.store.FileEOFException;
 import org.walkmanz.gardenz.store.FileFormatException;
 import org.walkmanz.gardenz.store.WriteState;
 import org.walkmanz.gardenz.util.HexUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.File;
 import java.io.IOException;
 
 /**
  * 
  * 以链性结构顺序存储数据到文件, 线程不安全
  *
  */
 public class LinkedDataFile extends BufferedDataFile {
 
 	private static final Logger LOG = LoggerFactory.getLogger(LinkedDataFile.class);
 	
 	/**
 	 * 文件限制大小
 	 */
 	private final int fileLimitLength = LinkedIndexFile.DATA_FILE_LIMIT_LENGTH;
 
 	
 	public LinkedDataFile(File file) throws IOException, FileFormatException {
 		
 		this(file.getAbsolutePath());
 	}
 	
 	public LinkedDataFile(String path) throws IOException, FileFormatException {
 		
 		super(path, true);
 		
 		if(super.isCreateFile){
 			//设置头文件长度
 			//ByteBuffer buffer = ByteBuffer.allocate(LinkedIndexFile.DATA_MESSAGE_START_POSITION);
 			
 			//文件头logo
 			//buffer.put(LinkedIndexFile.DATA_FILE_HEAD_LOGO.getBytes()); // 0-7 logo string
 			//文件尾部指针
 			//buffer.putInt(LinkedIndexFile.DATA_MESSAGE_START_POSITION); // 8-11 end position
 			//buffer.putInt(-1);
 			//buffer.putLong(-1);
 			//buffer.putLong(-1);// 12 - 31 占位符, 未分配
 			//buffer.rewind();
 			//super.write(buffer.array());
 			
 			
 			//文件头logo
 			super.writeUTF(LinkedIndexFile.DATA_FILE_HEAD_LOGO); // 0-7 logo string
 			
 			//文件尾部指针
 			super.writeInt(LinkedIndexFile.DATA_MESSAGE_START_POSITION); // 8-11 end position
 			super.writeInt(-1);
 			super.writeLong(-1);
 			super.writeLong(-1);// 12 - 31 占位符, 未分配
 			
 			
 			LOG.info("数据文件 {} 创建完毕", super.getFileName());
 			
 		} else {
 			// 验证文件头大小
 			if (super.size() < LinkedIndexFile.DATA_MESSAGE_START_POSITION) {
 				throw new FileFormatException("数据文件格式错误");
 			}
 			
 			// 读文件头logo
 			String logoString = super.readUTF(LinkedIndexFile.DATA_FILE_HEAD_LOGO.getBytes().length, 0);
 			
 			if (! logoString.equals(LinkedIndexFile.DATA_FILE_HEAD_LOGO)) {
 				throw new FileFormatException("数据文件格式错误");
 			}
 
 			
 			LOG.info("数据文件 {} 已打开", super.getFileName());
 		}
 	}
 	
 	public WriteState put(byte[] record) throws IOException {
 		int increment = record.length + 4;
 		//判断是否会写文件溢出
 		if (isFull(increment)) {
 			return WriteState.WRITE_FULL;
 		}
 		//先写记录长度
 		super.writeInt(record.length);
 		//再写记录内容
 		super.write(record);
 		
 		return WriteState.WRITE_SUCCESS;
 	}
 	
 	public byte[] get() throws IOException, FileEOFException {
 		
 		int position = (int)super.position();
 		
 		int length = super.readInt();
 		
 		byte[] bytes = super.read(length);
 		
 		if(bytes == null || bytes.length == 0){
 			this.position(position);
 		}
 		
 		return bytes;
 	}
 	
 	
 	/**
 	 * 判断文件是满了
 	 * @param increment
 	 * @return
 	 */
 	public boolean isFull(int increment) throws IOException {
 		// confirm if the file is full
 		if (this.fileLimitLength < this.position() + increment) {
 			return true;
 		}
 		return false;
 	}
 	
 	/**
 	 * 关闭文件
 	 */
 	public void close() throws IOException {
 		super.close();
 	}
 	
 	/**
 	 * 获取文件尾部指针位置
 	 * @return
 	 */
 	int getEndPosition() throws IOException {
 		return (int)super.readInt(8);
 	}
 	
 	/**
 	 * 设置文件尾部指针位置
 	 * @return
 	 */
 	void putEndPosition() throws IOException {
 		super.writeInt((int)super.position(), 8);
 	}
 	
 }
