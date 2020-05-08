 package org.myrest.io;
 
 import java.io.UnsupportedEncodingException;
 
 import org.jboss.netty.buffer.ChannelBuffer;
 import org.jboss.netty.buffer.ChannelBuffers;
 import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
 import org.jboss.netty.handler.codec.http.HttpResponseStatus;
 import org.jboss.netty.handler.codec.http.HttpVersion;
 
 import com.google.protobuf.Message;
 
 /**
  * 
  * Write GPB to text
  * 
  */
 public class GPBText extends DefaultHttpResponse {
 
 	byte[] bytes;
 
 	public GPBText(Message msg) throws UnsupportedEncodingException {
 		super(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
 		setHeader("Content-Type", "application/x-protobuf");
 
 		byte[] bytes = msg.toByteArray();
 		ChannelBuffer buffer = ChannelBuffers.wrappedBuffer(bytes);
		buffer.writeBytes(bytes);
 		super.setContent(buffer);
 		this.bytes = bytes;
 	}
 
 	public String asString() throws UnsupportedEncodingException {
 		return new String(bytes, "UTF-8");
 	}
 
 }
