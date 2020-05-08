 package net.hetimatan.net.torrent.client.message;
 
 import java.io.IOException;
 
 import net.hetimatan.io.file.MarkableReader;
 
 public class HelperLookAheadMessage {
 
 	private MessageNull mNullMessage = new MessageNull(0, -999);
 
 	public HelperLookAheadMessage() {
 	}
 
 	public MessageNull getMessageNull() {
 		return mNullMessage;
 	}
 
 
 	public boolean lookahead(MarkableReader reader) throws IOException {
 		try  {
			reader.pushMark();
 			mNullMessage = MessageNull.decode(reader);
 			return true;
 		} catch(IOException e) {
 		} catch(NegativeArraySizeException e) {			
 		} finally {
			reader.popMark();
 		}
 		return false;
 	}
 	
 }
