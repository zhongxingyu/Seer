 package org.ukiuni.lighthttpserver.util;
 
 import java.io.ByteArrayOutputStream;
 import java.io.Closeable;
 import java.io.IOException;
 import java.io.InputStream;
 
 public class ByteReader extends InputStream implements Closeable {
 	private InputStream in;
 	private byte[] lineSeparatorData;
	private ByteArrayOutputStream bout = new ByteArrayOutputStream();
 	private String charset;
 
 	public ByteReader(InputStream in, String charset, byte[] lineSeparatorData) {
 		this.in = in;
 		this.charset = charset;
 		this.lineSeparatorData = lineSeparatorData;
 	}
 
 	public ByteReader(InputStream in, String charset) {
 		this.in = in;
 		this.charset = charset;
		String lineSeparator = System.getProperty("line.separator");
		if (null != lineSeparator) {
			this.lineSeparatorData = lineSeparator.getBytes();
		}
 	}
 
 	public String readLine() throws IOException {
 		byte[] data = readTo(lineSeparatorData);
 		if (null == data) {
 			return null;
 		}
 		return new String(data, charset);
 	}
 
 	public byte[] readTo(byte[] searchData) throws IOException {
 		byte[] buffer = new byte[searchData.length];
 		int lineDevideMatchIndex = 0;
 		int readed = in.read(buffer);
		while (readed > 0 && (0 == searchData.length || lineDevideMatchIndex != searchData.length)) {
 			for (int i = 0; i < readed; i++) {
 				if (buffer[i] == searchData[lineDevideMatchIndex]) {
 					lineDevideMatchIndex++;
 				} else {
 					lineDevideMatchIndex = 0;
 				}
 			}
 			bout.write(buffer, 0, readed);
 			if (lineDevideMatchIndex != searchData.length) {
 				readed = in.read(buffer, 0, buffer.length - lineDevideMatchIndex);
 			}
 		}
 		if (bout.size() == 0) {
 			return null;
 		}
 		byte[] returnValueWithToData = bout.toByteArray();
		byte[] returnValue = new byte[returnValueWithToData.length - searchData.length];
 		bout.reset();
 		System.arraycopy(returnValueWithToData, 0, returnValue, 0, returnValue.length);
 		return returnValue;
 	}
 
 	public int read(byte[] buffer) throws IOException {
 		return in.read(buffer);
 	}
 
 	@Override
 	public void close() throws IOException {
 		in.close();
 	}
 
 	public int read() throws IOException {
 		return in.read();
 	}
 }
