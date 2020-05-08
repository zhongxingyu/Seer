 package outag.formats.mp4.util.box;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import outag.file_presentation.JBBuffer;
 import outag.formats.mp4.util.tag.Mp4FieldType;
 
 /** This box is used within both normal metadata boxes and ---- boxes to hold the actual data. <br>
  * Format is as follows:
  * :length          (4 bytes)
  * :name 'Data'     (4 bytes)
  * :atom version    (1 byte)
  * :atom type flags (3 bytes)
  * :locale field    (4 bytes) //Currently always zero
  * :data */
 public class Mp4DataBox {
     public static final String IDENTIFIER = "data";
 
     public static final int VERSION_LENGTH = 1;
     public static final int TYPE_LENGTH = 3;
     public static final int NULL_LENGTH = 4;
     public static final int PRE_DATA_LENGTH = VERSION_LENGTH + TYPE_LENGTH + NULL_LENGTH;
     public static final int DATA_HEADER_LENGTH = 8 + PRE_DATA_LENGTH;
 
     public static final int TYPE_POS = VERSION_LENGTH;
 
     //For use externally
     public static final int TYPE_POS_INCLUDING_HEADER = 8 + TYPE_POS;
 
     private int type;
     private String content;
 
     public static final int NUMBER_LENGTH = 2;
 
     //Holds byte data for byte fields as is not clear for multi byte fields exactly these should be wrttien
     private byte[] bytedata;
     
     ArrayList<Short> numbers;
 
     /** @param header     parentHeader info
      * @param dataBuffer data of box (doesn't include parentHeader data)
      * @throws IOException  */
     public Mp4DataBox(Mp4Box box, JBBuffer dataBuffer) throws IOException {
         //Make slice so operations here don't effect position of main buffer
     	JBBuffer data = dataBuffer.slice();
 
         //Type
         type = data.move(Mp4DataBox.TYPE_POS).UInt();
         
     	if (type == Mp4FieldType.TEXT.getFileClassId()) 
     		content = data.move(PRE_DATA_LENGTH).Str(box.getLength() - PRE_DATA_LENGTH, box.getEncoding());
     	else if (type == Mp4FieldType.IMPLICIT.getFileClassId()) {
         	data.pos(PRE_DATA_LENGTH);
         	content = "";
            while(data.pos() < box.getFullLength())
            	content += "/" + data.UShort();

            if (content.length() > 0) content.substring(1);
     	} 
     	else if (type == Mp4FieldType.INTEGER.getFileClassId()) {
             //TODO byte data length seems to be 1 for pgap and cpil but 2 for tmpo ?
             //Create String representation for display
     		data.pos(PRE_DATA_LENGTH);
    		content = data.UInt() + "";	        	
 //                content = Utils.getIntBE(this.dataBuffer, PRE_DATA_LENGTH, header.getDataLength() - 1) + "";
 
             //Songbird uses this type for trkn atom (normally implicit type) is used so just added this code so can be used
             //by the Mp4TrackField atom
     		
         	data.pos(PRE_DATA_LENGTH);
             while(data.pos() < box.getLength())
             	numbers.add((short)data.UShort());
             
 //                //But store data for safer writing back to file
 //                bytedata = new byte[box.getLength() - PRE_DATA_LENGTH];
 //                data.move(PRE_DATA_LENGTH).get(bytedata);                
     	}
     	else if (type == Mp4FieldType.COVERART_JPEG.getFileClassId()) {
     		content = data.move(PRE_DATA_LENGTH)
     			.Str(box.getLength() - PRE_DATA_LENGTH, box.getEncoding());        		
     	}
     }
 
     public String getContent() { return content; }
 
     public int getType() { return type; }
 
     /** Return numbers, only valid for numeric fields
      * @return numbers */
     //TODO this is only applicable for numeric databoxes, should we subclass dont know type until start
     //constructing and we also have Mp4tagTextNumericField class as well
     public List<Short> getNumbers() { return numbers; }
 
     /** Return raw byte data only vaid for byte fields
      * @return byte data */
     public byte[] getByteData() { return bytedata; }
 }
