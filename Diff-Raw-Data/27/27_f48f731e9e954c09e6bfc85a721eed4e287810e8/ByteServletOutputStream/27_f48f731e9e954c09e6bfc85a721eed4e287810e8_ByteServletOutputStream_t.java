 package org.tsaikd.java.filter.io;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 
 import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 public class ByteServletOutputStream extends ServletOutputStream {
 
 	static Log log = LogFactory.getLog(ByteServletOutputStream.class);
 
 	protected ByteArrayOutputStream baos;
 
 	public ByteServletOutputStream(ByteArrayOutputStream baos) {
 		this.baos = baos;
 	}
 
 	@Override
 	public void write(int b) throws IOException {
 		baos.write(b);
 	}
 
	@Override
	public boolean isReady() {
		return true;
	}

	@Override
	public void setWriteListener(WriteListener listener) {
	}

 }
