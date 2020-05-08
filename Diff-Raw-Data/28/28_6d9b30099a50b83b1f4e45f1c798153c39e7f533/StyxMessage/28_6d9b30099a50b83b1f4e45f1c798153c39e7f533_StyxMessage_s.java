 package com.v2soft.styxlib.library.messages.base;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 
 import com.v2soft.styxlib.library.messages.StyxRAttachMessage;
 import com.v2soft.styxlib.library.messages.StyxRAuthMessage;
 import com.v2soft.styxlib.library.messages.StyxRClunkMessage;
 import com.v2soft.styxlib.library.messages.StyxRCreateMessage;
 import com.v2soft.styxlib.library.messages.StyxRErrorMessage;
 import com.v2soft.styxlib.library.messages.StyxRFlushMessage;
 import com.v2soft.styxlib.library.messages.StyxROpenMessage;
 import com.v2soft.styxlib.library.messages.StyxRReadMessage;
 import com.v2soft.styxlib.library.messages.StyxRRemoveMessage;
 import com.v2soft.styxlib.library.messages.StyxRStatMessage;
 import com.v2soft.styxlib.library.messages.StyxRVersionMessage;
 import com.v2soft.styxlib.library.messages.StyxRWStatMessage;
 import com.v2soft.styxlib.library.messages.StyxRWalkMessage;
 import com.v2soft.styxlib.library.messages.StyxRWriteMessage;
 import com.v2soft.styxlib.library.messages.StyxTAttachMessage;
 import com.v2soft.styxlib.library.messages.StyxTAuthMessage;
 import com.v2soft.styxlib.library.messages.StyxTClunkMessage;
 import com.v2soft.styxlib.library.messages.StyxTCreateMessage;
 import com.v2soft.styxlib.library.messages.StyxTFlushMessage;
 import com.v2soft.styxlib.library.messages.StyxTOpenMessage;
 import com.v2soft.styxlib.library.messages.StyxTReadMessage;
 import com.v2soft.styxlib.library.messages.StyxTRemoveMessage;
 import com.v2soft.styxlib.library.messages.StyxTStatMessage;
 import com.v2soft.styxlib.library.messages.StyxTVersionMessage;
 import com.v2soft.styxlib.library.messages.StyxTWStatMessage;
 import com.v2soft.styxlib.library.messages.StyxTWalkMessage;
 import com.v2soft.styxlib.library.messages.StyxTWriteMessage;
 import com.v2soft.styxlib.library.messages.base.enums.MessageType;
 import com.v2soft.styxlib.library.messages.base.enums.ModeType;
 import com.v2soft.styxlib.library.messages.base.structs.StyxQID;
 import com.v2soft.styxlib.library.server.StyxBufferOperations;
 
 public abstract class StyxMessage {
 	public static final int BASE_BINARY_SIZE = 7;
 	
 	public static final int NOTAG  =      0xFFFF;
 	public static final long NOFID = 0xFFFFFFFFL;
 	
 	private int mTag;
 	private MessageType mType;
 	
     /**
      * Construct message from DoubleStateBuffer
      * @param is intput stream
      * @param io_unit packet size
      * @return constructed Message object
      * @throws IOException
      */
     public static StyxMessage factory(StyxBufferOperations buffer, int io_unit) 
             throws IOException {
         // get common packet data
         long packet_size = buffer.readUInt32();
         if ( packet_size > io_unit ) throw new IOException("Packet size to large");
         MessageType type = MessageType.factory(buffer.readUInt8());
         if ( type == null ) throw new NullPointerException("Type is null");
        assert type != null;
         int tag = buffer.readUInt16();
         // load other data
         StyxMessage result = null;
         switch (type) {
         case Tversion:
             result = new StyxTVersionMessage();
             break;
         case Rversion:
             result = new StyxRVersionMessage(0, null);
             break;
         case Tauth:
             result = new StyxTAuthMessage(NOFID);
             break;
         case Tflush:
             result = new StyxTFlushMessage(NOTAG);
             break;
         case Tattach:
             result = new StyxTAttachMessage(NOFID, NOFID, null, null);
             break;
         case Twalk:
             result = new StyxTWalkMessage(NOFID, NOFID, "");
             break;
         case Rauth:
             result = new StyxRAuthMessage(tag, StyxQID.EMPTY);
             break;
         case Rerror:
             result = new StyxRErrorMessage(tag, null);
             break;
         case Rflush:
             result = new StyxRFlushMessage(tag);
             break;
         case Rattach:
             result = new StyxRAttachMessage(tag, StyxQID.EMPTY);
             break;
         case Rwalk:
             result = new StyxRWalkMessage(tag, null); 
             break;
         case Topen:
             result = new StyxTOpenMessage(NOFID, ModeType.OREAD);
             break;
         case Ropen:
             result = new StyxROpenMessage(tag, null, 0);
             break;
         case Tcreate:
             result = new StyxTCreateMessage(NOFID, null, 0, ModeType.OWRITE);
             break;
         case Rcreate:
             result = new StyxRCreateMessage(tag);
             break;
         case Tread:
             result = new StyxTReadMessage(NOFID, null, 0);
             break;
         case Rread:
             result = new StyxRReadMessage(tag, null, 0);
             break;
         case Twrite:
             result = new StyxTWriteMessage(NOFID, null, null );
             break;
         case Rwrite:
             result = new StyxRWriteMessage(tag, 0);
             break;
         case Tclunk:
             result = new StyxTClunkMessage(tag);
             break;
         case Rclunk:
             result = new StyxRClunkMessage(tag);
             break;
         case Tremove:
             result = new StyxTRemoveMessage(tag);
             break;
         case Rremove:
             result = new StyxRRemoveMessage(tag);
             break;
         case Tstat:
             result = new StyxTStatMessage(tag);
             break;
         case Rstat:
             result = new StyxRStatMessage(tag);
             break;
         case Twstat:
             result = new StyxTWStatMessage(NOFID, null);
             break;
         case Rwstat:
             result = new StyxRWStatMessage(tag);
             break;
         }
         result.setTag((short) tag);
         result.load(buffer);
         return result;
     }	
 	
 	public static String toString(byte[] bytes)
 	{
 		if ( (bytes == null) || (bytes.length==0))
 			return "-";
 		StringBuilder result = new StringBuilder();
 		result.append(Integer.toHexString(((int)bytes[0])&0xFF));
 		result.append(",");
 		int count = bytes.length;
 		for (int i=1; i<count; i++)
 		{
 			result.append(Integer.toHexString(((int)bytes[i])&0xFF));
 			result.append(',');
 		}
 		
 		return String.format("(%s)", result);
 	}
 	
 	public static int getUTFSize(String utf)
 	{
 		if (utf == null)
			return getUTFSize("");
 		return 2 + countUTFBytes(utf);
 	}
 	
	public static int countUTFBytes(String utf)
	{
	    // TODO !!!! Is it working correct??
		int result = 0;
		int length = utf.length();
		for (int i=0; i<length; i++)
		{
			int posChar = utf.charAt(i);
			if (posChar > 0 && posChar <=127)
				result += 1;
			else if (posChar <= 2047)
				result += 2;
			else 
				result += 3;
		}
		
		return result;
 	}
 	
 	protected StyxMessage(MessageType type, int tag) {
 		mType = type;
 		mTag = tag;
 	}
 	
 	public MessageType getType()
 	{
 		return mType;
 	}
 	
 	public int getTag()
 	{
 		return mTag;
 	}
 	
 	public void setTag(int tag)
 	{
 		mTag = (tag & 0xFFFF);
 	}
 	
 	public int getBinarySize()
 	{
 		return BASE_BINARY_SIZE;
 	}
 	
 	public void writeToBuffer(StyxBufferOperations output)  
 	        throws UnsupportedEncodingException, IOException {
 		output.clear();
 		int packetSize = getBinarySize();
 		output.limit(packetSize);
 		output.writeUInt(packetSize);
 		output.writeUByte((short) getType().getByte());
 		output.writeUShort(getTag());
 	}
 
 	protected abstract String internalToString();
     protected abstract void load(StyxBufferOperations buffer) throws IOException;
 	
 	@Override
 	public String toString() {
 		String stmp = String.format("Type: %s\nTag: %d", 
 				getType().toString(), getTag());
 		
 		String internal = internalToString();
 		if (internal != null)
 			stmp = String.format("%s\n%s", stmp, internal);
 		
 		return String.format("(\n%s\n)", stmp);
 	}
 }
