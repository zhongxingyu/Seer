 package suite.file;
 
 import java.io.Closeable;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.nio.ByteBuffer;
 
 public class PageFile implements Closeable {
 
 	private static final int pageSize = 4096;
 
 	private AccessibleFile file;
 
 	public PageFile(String filename) throws FileNotFoundException {
 		file = new AccessibleFile(filename);
 	}
 
 	@Override
 	public void close() throws IOException {
 		file.close();
 	}
 
 	public ByteBuffer load(int pageNo) throws IOException {
		int start = pageNo * pageSize, end = start + pageNo;
 		return file.load(start, end);
 	}
 
 	public void save(int pageNo, ByteBuffer buffer) throws IOException {
 		file.save(pageNo * pageSize, buffer);
 	}
 
 }
