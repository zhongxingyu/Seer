 package a2;
 
 import java.io.ByteArrayOutputStream;
 import java.io.Closeable;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.StringWriter;
 import java.io.UnsupportedEncodingException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.Iterator;
 import java.util.zip.GZIPInputStream;
 
 import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
 import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
 import org.apache.commons.io.IOUtils;
 
 public class ArchiveStringIterator implements Iterator<String>,Iterable<String>, Closeable {
 
 	InputStream in = null;
 	GZIPInputStream gz = null;
 	TarArchiveInputStream tar = null;
 	Iterator<String> strings = null;
 	TarArchiveEntry next = null;
 	GithubRequest request = null;
 
 	public ArchiveStringIterator(String url) throws MalformedURLException, IOException{
 		request = new GithubRequest(url);
 		if(request.status==200){
 			this.gz = new GZIPInputStream(request.getInputStream());
 			this.tar = new TarArchiveInputStream(gz);
 			next = tar.getNextTarEntry();
 		}
 	}
 	
 	@Override
 	public boolean hasNext() {
 		return next!=null;
 	}
 
 	@Override
 	public String next() {
		try {
 			next=tar.getNextTarEntry();
 			while(next!=null && (!next.isFile() || !next.getName().endsWith(".java"))){
 
 				next = tar.getNextTarEntry();
 			}
 
 			ByteArrayOutputStream out = new ByteArrayOutputStream();
 			int size = 1024;
 			byte[] bytes = new byte[size];
 			int read=0;
 			while(true){
 				read = tar.read(bytes,0,size);
 				if(read!=-1)out.write(bytes,0,read);
 				else break;
 			}
 			return new String(out.toByteArray(),"UTF-8");
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace(System.err);
 		} catch (IOException e) {
 			e.printStackTrace(System.err);
 		}
 		return null;
 	}
 
 	@Override
 	public void remove() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void close() throws IOException {
 		IOUtils.closeQuietly(tar);
 		IOUtils.closeQuietly(gz);
 		IOUtils.closeQuietly(in);
 		IOUtils.closeQuietly(request);
 	}
 
 	@Override
 	public Iterator<String> iterator() {
 		// TODO Auto-generated method stub
 		return this;
 	}
 
 }
