 package de.questmaster.fatremote.datastructures;
 
 
 /**
  * @author daniel
  *
  */
 public class FATRemoteEvent {
 	
 	private short[] mCommand = { 0x48, 0x12, 0x00, 0x00 };
 	private short[] mPayload = new short[0];
 	
 	/**
 	 * 
 	 * @param cmd2
 	 * @param cmd3
 	 */
 	public FATRemoteEvent(short cmd2, short cmd3) {
 		this(cmd2, cmd3, null);
 	}
 
 	/**
 	 * 
 	 * @param cmd2
 	 * @param cmd3
 	 * @param payload
 	 */
 	public FATRemoteEvent(short cmd2, short cmd3, short[] payload) {
 		mCommand[2] = cmd2;
 		mCommand[3] = cmd3;
 		
 		if (payload != null) {
			mPayload = payload;
 		}
 	}
 
 	public short[] getRemoteCode() {
 		return mCommand.clone();
 	}
 	
 	public short[] getCodePayload() {
 		return mPayload.clone();
 	}
 	
 }
