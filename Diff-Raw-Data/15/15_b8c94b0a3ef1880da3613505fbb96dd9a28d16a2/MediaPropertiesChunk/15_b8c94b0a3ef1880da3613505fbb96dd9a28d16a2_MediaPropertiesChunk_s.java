 package outag.formats.real.io;
 
 import java.io.ByteArrayInputStream;
 import java.io.DataInputStream;
 
 import outag.formats.exceptions.UnsupportedException;
 import outag.formats.generic.Utils;
 import outag.formats.real.utils.AudioInfo;
 import outag.formats.real.utils.LogicalStreamInfo;
 import outag.formats.real.utils.LosslessAudioInfo;
 
 /** MDPR chunk */
 public class MediaPropertiesChunk {
 	short streamId;
 	public int maxBitrate;
 	public int averageBitrate;
 	int maxDataPacketSize;
 	int averageDataPacketSize;
 	int streamStartInMs;
 	/** Preroll in ms (to be subtracted from timestamps?) */
 	int prerollInMs;
 	/** stream duration */
 	public int duration; // in ms
 	public String streamDescription;
 	public String mimeType; 
 	public AudioInfo audioInfo = null;
 	public LosslessAudioInfo losslessAudioInfo = null;
 	public LogicalStreamInfo logicStreamInfo = null;
 	
 	
 //	dword   Chunk type ('MDPR')
 //	dword   Chunk size
 //	word    Chunk version (always 0, for every known file)
 //	word    Stream number
 //	dword   Maximum bit rate
 //	dword   Average bit rate
 //	dword   Size of largest data packet
 //	dword   Average size of data packet
 //	dword   Stream start offset in ms
 //	dword   Preroll in ms (to be subtracted from timestamps?)
 //	dword   Stream duration in ms
 //	byte    Size of stream description string
 //	byte[]  Stream description string
 //	byte    Size of stream mime type string
 //	byte[]  Mime type string
 //	dword   Size of type specific part of the header
 //	byte[]  Type specific data, meaning and format depends on mime type	
 	
 	public MediaPropertiesChunk(DataInputStream f) throws Exception {
 		streamId = f.readShort();
 		maxBitrate = f.readInt();
 		averageBitrate = f.readInt();
 		maxDataPacketSize = f.readInt();
 		averageDataPacketSize = f.readInt();
 		streamStartInMs = f.readInt();
 		prerollInMs  = f.readInt();
 		duration = f.readInt();
 //		byte    Size of stream description string
 		streamDescription = Utils.readString(f, f.read());
 //		byte    Size of stream mime type string
 		mimeType = Utils.readString(f, f.read());
 		
 		int dataLength = (int)f.readInt();
 		byte [] b = new byte[dataLength];
 		f.readFully(b);
 		
 		switch(mimeType) {
 			case "audio/x-pn-realaudio" :
 				audioInfo = new AudioInfo(new DataInputStream(new ByteArrayInputStream(b)));
 				break;
 			case "logical-fileinfo":
 				logicStreamInfo = new LogicalStreamInfo(new DataInputStream(new ByteArrayInputStream(b)));
 				break;
 			case "audio/X-MP3-draft-00": /*dataLength must equals 0. In this case we do not have any codec info */ break;
 			case "audio/x-ralf-mpeg4" : 
 			case "audio/x-ralf-mpeg4-generic" :
 				losslessAudioInfo = new LosslessAudioInfo(new DataInputStream(new ByteArrayInputStream(b)));
 			case "audio/x-pn-multirate-realaudio" : /* ASM-compatible RealAudio stream. Not supported */				
 			default: throw new UnsupportedException("Unknow mime : " + mimeType); 
 		}
 	}
 }
