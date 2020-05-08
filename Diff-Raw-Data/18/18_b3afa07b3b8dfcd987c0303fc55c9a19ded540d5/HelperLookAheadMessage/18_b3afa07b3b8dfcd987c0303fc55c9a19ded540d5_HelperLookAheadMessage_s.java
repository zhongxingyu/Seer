 package net.hetimatan.net.torrent.client.message;
 
 import java.io.IOException;
 
 import net.hetimatan.io.file.MarkableReader;
 
 public class HelperLookAheadMessage {
 
 	private MessageNull mNullMessage = new MessageNull(0, -999);
	private long mStartFP = 0;
 
 	public HelperLookAheadMessage() {
 	}
 
 	public MessageNull getMessageNull() {
 		return mNullMessage;
 	}
 
	public long getStartFp() {
		return mStartFP;
	}
 
 	public boolean lookahead(MarkableReader reader) throws IOException {
 		try  {
			mStartFP = reader.getFilePointer();
 			mNullMessage = MessageNull.decode(reader);
 			return true;
 		} catch(IOException e) {
 		} catch(NegativeArraySizeException e) {			
 		} finally {
			reader.seek(mStartFP);
 		}
 		return false;
 	}
 	
 }
