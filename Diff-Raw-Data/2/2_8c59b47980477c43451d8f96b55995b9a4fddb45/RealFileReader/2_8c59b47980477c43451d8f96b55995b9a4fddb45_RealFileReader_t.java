 package outag.formats.real;
 
 import outag.formats.exceptions.UnsupportedException;
 import outag.formats.generic.AudioFileReader;
 import outag.formats.real.io.ContentDescriptionChunk;
 import outag.formats.real.io.DataChunk;
 import outag.formats.real.io.FileHeaderChunk;
 import outag.formats.real.io.FilePropertiesChunk;
 import outag.formats.real.io.GenericChunk;
 import outag.formats.real.io.IndexChunk;
 import outag.formats.real.io.MediaPropertiesChunk;
 import outag.formats.EncodingInfo;
 import outag.formats.Tag;
 
 import java.io.RandomAccessFile;
 
 /** Real Media File Format: Major Chunks: .RMF PROP MDPR CONT DATA INDX */
 public class RealFileReader extends AudioFileReader {
     protected void fullTrack(RandomAccessFile raf) throws Exception {
         EncodingInfo rv = new EncodingInfo();
         GenericChunk chunk;
         boolean cycle = true;
         long fileSize = raf.getChannel().size();
         
         while(cycle) {
         	if (fileSize == raf.getFilePointer()) break;
         	chunk = new GenericChunk(raf);
         	
         	switch(chunk.obj_id) {
         		case ".RMF":
 //        			FileHeaderChunk fhc = new FileHeaderChunk(chunk.data);
         			break;
         		case "PROP":
 //        			FilePropertiesChunk fpc = new FilePropertiesChunk(chunk.data);       			
         			break;
         		case "MDPR":
 //        			MediaPropertiesChunk mpc = new MediaPropertiesChunk(chunk.data);
         			break;
         		case "CONT":
 //        			ContentDescriptionChunk cdc = new ContentDescriptionChunk(chunk.data);
         			break;
         		case "DATA":
 //        			DataChunk dc = new DataChunk(chunk.data);
         			break;
         		case "INDX":
 //        			IndexChunk ic = new IndexChunk(chunk.data);
         			break;
         		default: throw new UnsupportedException("Wrong chunk type : " + chunk.obj_id);
         	}
         }
     }
 	
     protected EncodingInfo getEncodingInfo(RandomAccessFile raf) throws Exception {
         EncodingInfo rv = new EncodingInfo();
         GenericChunk chunk;
         long fileSize = raf.getChannel().size();
         
         while(true) {
         	if (fileSize == raf.getFilePointer()) break;
         	chunk = new GenericChunk(raf);
         	
         	switch(chunk.obj_id) {
         		case "PROP":
         			FilePropertiesChunk fpc = new FilePropertiesChunk(chunk.data);
 	                  rv.setBitrate(fpc.averageBitrate / 1000);
 	                  rv.setLength(fpc.duration / 1000);
 	                  rv.setVbr(fpc.maxBitrate != fpc.averageBitrate);        			
         			break;
         		case "MDPR":
         			MediaPropertiesChunk mpc = new MediaPropertiesChunk(chunk.data);
         			if (mpc.audioInfo != null) {
         				rv.setChannelNumber(mpc.audioInfo.codecInfo.getchannels());
         				rv.setSamplingRate(mpc.audioInfo.codecInfo.getsampleRate());
         				rv.setEncodingType(mpc.mimeType);
        				return rv;
         			}
         			break; 			
         	}
         }
         return rv;
     }
 
     protected Tag getTag(RandomAccessFile raf) throws Exception {
         GenericChunk chunk;
         long fileSize = raf.getChannel().size();
         
         while(true) {
         	if (fileSize == raf.getFilePointer()) break;
         	chunk = new GenericChunk(raf);
         	
         	switch(chunk.obj_id) {
         		case "CONT":
         			ContentDescriptionChunk cdc = new ContentDescriptionChunk(chunk.data);
         			return cdc.tag;
         	}
         }
         
         return new RealTag();
     }
 }
