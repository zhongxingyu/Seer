 package net.hetimatan.net.torrent.client.message;
 
 
 import java.io.IOException;
 import java.io.OutputStream;
 
 import net.hetimatan.io.file.MarkableReader;
 import net.hetimatan.util.io.ByteArrayBuilder;
 
 public class MessageNull extends TorrentMessage {
 
 	public static final String TAG = "nullmessage";
 
 	private int mLength = 0;
 	private int mSign = -999;
 
 	public MessageNull(int length, int sign) {
 		super(TorrentMessage.DUMMY_SIGN_NULL);
 		mLength = length;
 		mSign = sign;
 	}
 
 	public int getSign() {
 		return mSign;
 	}
 
 	public int getMessageLength() {
 		return mLength;
 	}
 
 	@Override
 	public String toString() {
 		return TAG+":";
 	}
 
 	@Override
 	public void encode(OutputStream output) throws IOException {
 		output.write(ByteArrayBuilder.parseInt(0, ByteArrayBuilder.BYTEORDER_BIG_ENDIAN));
 	}
 
 	public static MessageNull decode(MarkableReader reader) throws IOException {
 		int len =_length(reader);
 		int sign = -999;
 		if(len>0) {
 			sign = reader.read();
 		}
 		for(int i=1;i<len;i++){
			if(0 > reader.read()){
 				throw new IOException();
 			}
 		}
 		return new MessageNull(len, sign);
 	}
 }
