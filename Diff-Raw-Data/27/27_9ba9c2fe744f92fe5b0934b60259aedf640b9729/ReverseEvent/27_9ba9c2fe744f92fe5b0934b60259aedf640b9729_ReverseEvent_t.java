 package net.sayon.dovor.events;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.nio.ByteBuffer;
 
 import net.sayon.dovor.Buddy;
 
 public class ReverseEvent extends Event { // TODO rename this class, do not like :L
 	private String text;
 	private InputStream is;
 	private OutputStream os;
 
 	public ReverseEvent(Buddy buddy, String l, InputStream is, OutputStream os) {
 		this.buddy = buddy;
 		this.text = l;
 		this.is = is;
 		this.os = os;
 	}
 
 	public String getText() {
 		return text;
 	}
 
 	public InputStream getInputStream() {
 		return is;
 	}
 
 	public OutputStream getOutputStream() {
 		return os;
 	}
 
 	public String readStringTillChar(char c) throws IOException {
 		return new String(readBytesTillChar(c));
 	}
 
 	public byte[] readBytesTillChar(char c) throws IOException {
 		ByteBuffer bb = ByteBuffer.allocate(100 * 1024); // 100 KiB
 		int rc;
 		while ((rc = is.read()) >= 0 && rc != c) {
 			bb.put((byte) rc);
 		}
 		return bb.array();
 	}
 
	public byte[] readUnescapedBytesTillChar(char c) throws IOException {
		ByteBuffer bb = ByteBuffer.allocate(100 * 1024); // 100 KiB
		int rc;
		int pc = 0x0;
		while ((rc = is.read()) >= 0 && rc != c) {
			if (pc == '\\' && rc == 'n') {
				bb.position(bb.position()-1);
				bb.put((byte) '\n');
			} else {
				bb.put((byte) rc);
			}
		}
		return bb.array();
	}

 }
