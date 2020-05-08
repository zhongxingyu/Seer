 package crimsonwoods.android.apps.KinectServer.protocol;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.Socket;
 import java.nio.ByteBuffer;
 import java.nio.ByteOrder;
 
 import crimsonwoods.android.apps.KinectServer.error.*;
 import crimsonwoods.android.apps.KinectServer.protocol.KinectServerProtocolCommand.KinectServerProtocolHeader;
 
 public class KinectServerProtocolParser {
 	private static final ByteOrder DEFAULT_BYTE_ORDER = ByteOrder.LITTLE_ENDIAN;
 	
 	private static class KinectServerXferContext {
 		private byte[] headerBytes;
 		private byte[] bodyBytes;
 		private int receivedHeaderByteLength;
 		private int receivedBodyByteLength;
 		private KinectServerProtocolHeader header;
 		
 		public KinectServerXferContext() {
 			headerBytes = new byte[KinectServerProtocolHeader.HEADER_SIZE];
 			bodyBytes = null;
 			receivedHeaderByteLength = 0;
 			receivedBodyByteLength = 0;
 			header = null;
 		}
 		
 		public void clear() {
 			//headerBytes = new byte[KinectServerProtocolHeader.HEADER_SIZE];
 			//bodyBytes = null;
 			receivedHeaderByteLength = 0;
 			receivedBodyByteLength = 0;
 			header = null;
 		}
 		
 		public boolean isHeaderReceived() {
 			return headerBytes.length == receivedHeaderByteLength;
 		}
 		
 		public boolean isBodyReceived() {
 			if (!isHeaderReceived()) {
 				return false;
 			}
			if (null == header) {
				return false;
			}
 			return header.getBodyLength() == receivedBodyByteLength;
 		}
 		
 		public byte[] getHeaderBytes() {
 			return headerBytes;
 		}
 		
 		public byte[] getBodyBytes() {
 			return bodyBytes;
 		}
 		
 		public int getReceivedHeaderByteLength() {
 			return receivedHeaderByteLength;
 		}
 		
 		public void addReceivedHeaderByteLength(int length) {
 			receivedHeaderByteLength += length;
 		}
 		
 		public int getReceivedBodyByteLength() {
 			return receivedBodyByteLength;
 		}
 		
 		public void addReceivedBodyByteLength(int length) {
 			receivedBodyByteLength += length;
 		}
 		
 		public KinectServerProtocolHeader getHeader() {
 			return header;
 		}
 		
		public int getBodyByteLength() {
			return (null == header) ? 0 : header.getBodyLength();
		}
		
 		public void setHeader(KinectServerProtocolHeader header) {
 			this.header = header;
 			if (null != header) {
 				if (0 < header.getBodyLength()) {
 					if ((null == bodyBytes) || (bodyBytes.length < header.getBodyLength())) {
 						bodyBytes = new byte[header.getBodyLength()];
 					}
 				}
 			}
 		}
 	}
 	
 	private static KinectServerXferContext xferContext = new KinectServerXferContext();
 	private static KinectServerProtocolNotification[] notification = null;
 	private static int notificationIndex = 0;
 	
 	public static KinectServerProtocolCommand parse(Socket client) throws IOException {
 		KinectServerProtocolCommand command = null;
 		InputStream is = client.getInputStream();
 		try {
 			if (!xferContext.isHeaderReceived()) {
				int n = read(is, xferContext.getHeaderBytes(), KinectServerProtocolHeader.HEADER_SIZE, xferContext.getReceivedHeaderByteLength());
 				if (0 > n) {
 					client.close();
 					return null;
 				}
 				xferContext.addReceivedHeaderByteLength(n);
 				if (xferContext.getReceivedHeaderByteLength() < KinectServerProtocolHeader.HEADER_SIZE) {
 					return null;
 				}
 				xferContext.setHeader(new KinectServerProtocolHeader(wrap(xferContext.getHeaderBytes())));
 			}
 			if (xferContext.isHeaderReceived() && !xferContext.isBodyReceived()) {
				int n = read(is, xferContext.getBodyBytes(), xferContext.getBodyByteLength(), xferContext.getReceivedBodyByteLength());
 				if (0 > n) {
 					client.close();
 					return null;
 				}
 				xferContext.addReceivedBodyByteLength(n);
 				if (xferContext.getReceivedBodyByteLength() < xferContext.header.getBodyLength()) {
 					return null;
 				}
 				
 				KinectServerProtocolHeader header = xferContext.getHeader();
 				ByteBuffer body = wrap(xferContext.getBodyBytes());
 				
 				xferContext.clear();
 				
 				switch(header.getCommandType()) {
 				case Request:
 					// TODO : not supported.
 					break;
 				case Reply:
 					throw new InvalidKinectProtocolCommandTypeException();
 				case Notification:
 					if (null == notification) {
 						notification = new KinectServerProtocolNotification[2];
 					}
 					if (null == notification[notificationIndex]) {
 						notification[notificationIndex] = new KinectServerProtocolNotification(header, body);
 					} else {
 						notification[notificationIndex].reload(header, body);
 					}
 					command = notification[notificationIndex];
 					notificationIndex = (notificationIndex + 1) % notification.length;
 					break;
 				default:
 					throw new UnknownKinectProtocolCommandTypeException();
 				}
 			}
		} catch (Exception ex) {
			if (null != xferContext) {
				xferContext.clear();
			}
			throw new KinectServerRuntimeException(ex);
 		} finally {
 		}
 		return command;
 	}
 	
 	private static ByteBuffer wrap(byte[] bytes) {
 		ByteBuffer buffer = ByteBuffer.wrap(bytes);
 		buffer.order(DEFAULT_BYTE_ORDER);
 		return buffer;
 	}
 	
	private static int read(InputStream is, byte[] buffer, int size, int offset) throws IOException {
		final int length = size - offset;
 		int read;
 		for (read = 0; read < length; ) {
 			int n = is.read(buffer, offset + read, length - read);
 			if (0 > n) {
 				if (0 == read) {
 					return n;
 				}
 				break;
 			}
 			read += n;
 		}
 		return read;
 	}
 }
