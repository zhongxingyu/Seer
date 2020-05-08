 package outag.formats.mp4.util.tag;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 
 import outag.file_presentation.JBBuffer;
 import outag.formats.generic.TagField;
 import outag.formats.generic.Utils;
 import outag.formats.mp4.Mp4Tag;
 import outag.formats.mp4.util.box.Mp4Box;
 
 public abstract class Mp4TagField implements TagField {
     protected String id;
     
     public static void parse(Mp4Tag tag, Mp4Box head, JBBuffer raw) throws Exception {    	
     	switch(head.getId()) {
     		case "ART":
     			tag.addArtist(new Mp4TagTextField(head.getId(), raw).getContent());
     			break;
     		case "alb":
     			tag.addAlbum(new Mp4TagTextField(head.getId(), raw).getContent());
     			break;
     		case "nam":
     			tag.addTitle(new Mp4TagTextField(head.getId(), raw).getContent());
     			break;
     		case "trkn":
     			tag.addTrack(new Mp4TrackField(head.getId(), raw).getContent());
     			break;
     		case "day":
     			tag.addYear(new Mp4TagTextField(head.getId(), raw).getContent());
     			break;
     		case "cmt":
     			tag.addComment(new Mp4TagTextField(head.getId(), raw).getContent());
     			break;
     		case "gnre":
    			//TODO: fix bug
     			tag.addGenre(new Mp4GenreField(head.getId(), raw).getContent());
     			break;
     		case Mp4TagReverseDnsField.IDENTIFIER: break;
     		default:
     			
     			break;
     	}
     	
     	
 //    	
 //    	if (head.getId().equals(Mp4TagReverseDnsField.IDENTIFIER)) {
 //            try  {
 //                TagField field = new Mp4TagReverseDnsField(header, raw);
 //                tag.addField(field);
 //            }
 //            catch (Exception e)
 //            {
 //                logger.warning(ErrorMessage.MP4_UNABLE_READ_REVERSE_DNS_FIELD.getMsg(e.getMessage()));
 //                TagField field = new Mp4TagRawBinaryField(header, raw);
 //                tag.addField(field);
 //            }
 //        }
 //        //Normal Parent with Data atom
 //        else
 //        {
 //            int currentPos = raw.position();
 //            boolean isDataIdentifier = Utils.getString(raw, Mp4BoxHeader.IDENTIFIER_POS, Mp4BoxHeader.IDENTIFIER_LENGTH, "ISO-8859-1").equals(Mp4DataBox.IDENTIFIER);
 //            raw.position(currentPos);
 //            if (isDataIdentifier)
 //            {
 //                //Need this to decide what type of Field to create
 //                int type = Utils.getIntBE(raw, Mp4DataBox.TYPE_POS_INCLUDING_HEADER, Mp4DataBox.TYPE_POS_INCLUDING_HEADER + Mp4DataBox.TYPE_LENGTH - 1);
 //                Mp4FieldType fieldType = Mp4FieldType.getFieldType(type);
 //                logger.config("Box Type id:" + header.getId() + ":type:" + fieldType);
 //
 //                //Special handling for some specific identifiers otherwise just base on class id
 //                if (header.getId().equals(Mp4FieldKey.TRACK.getFieldName()))
 //                {
 //                    TagField field = new Mp4TrackField(header.getId(), raw);
 //                    tag.addField(field);
 //                }
 //                else if (header.getId().equals(Mp4FieldKey.DISCNUMBER.getFieldName()))
 //                {
 //                    TagField field = new Mp4DiscNoField(header.getId(), raw);
 //                    tag.addField(field);
 //                }
 //                else if (header.getId().equals(Mp4FieldKey.GENRE.getFieldName()))
 //                {
 //                    TagField field = new Mp4GenreField(header.getId(), raw);
 //                    tag.addField(field);
 //                }
 //                else if (header.getId().equals(Mp4FieldKey.ARTWORK.getFieldName()) || Mp4FieldType.isCoverArtType(fieldType))
 //                {
 //                    int processedDataSize = 0;
 //                    int imageCount = 0;
 //                    //The loop should run for each image (each data atom)
 //                    while (processedDataSize < header.getDataLength())
 //                    {
 //                        //There maybe a mixture of PNG and JPEG images so have to check type
 //                        //for each subimage (if there are more than one image)
 //                        if (imageCount > 0)
 //                        {
 //                            type = Utils.getIntBE(raw, processedDataSize + Mp4DataBox.TYPE_POS_INCLUDING_HEADER,
 //                                    processedDataSize + Mp4DataBox.TYPE_POS_INCLUDING_HEADER + Mp4DataBox.TYPE_LENGTH - 1);
 //                            fieldType = Mp4FieldType.getFieldType(type);
 //                        }
 //                        Mp4TagCoverField field = new Mp4TagCoverField(raw,fieldType);
 //                        tag.addField(field);
 //                        processedDataSize += field.getDataAndHeaderSize();
 //                        imageCount++;
 //                    }
 //                }
 //                else if (fieldType == Mp4FieldType.TEXT)
 //                {
 //                    TagField field = new Mp4TagTextField(header.getId(), raw);
 //                    tag.addField(field);
 //                }
 //                else if (fieldType == Mp4FieldType.IMPLICIT)
 //                {
 //                    TagField field = new Mp4TagTextNumberField(header.getId(), raw);
 //                    tag.addField(field);
 //                }
 //                else if (fieldType == Mp4FieldType.INTEGER)
 //                {
 //                    TagField field = new Mp4TagByteField(header.getId(), raw);
 //                    tag.addField(field);
 //                }
 //                else
 //                {
 //                    boolean existingId = false;
 //                    for (Mp4FieldKey key : Mp4FieldKey.values())
 //                    {
 //                        if (key.getFieldName().equals(header.getId()))
 //                        {
 //                            //The parentHeader is a known id but its field type is not one of the expected types so
 //                            //this field is invalid. i.e I received a file with the TMPO set to 15 (Oxf) when it should
 //                            //be 21 (ox15) so looks like somebody got their decimal and hex numbering confused
 //                            //So in this case best to ignore this field and just write a warning
 //                            existingId = true;
 //                            logger.warning("Known Field:" + header.getId() + " with invalid field type of:" + type + " is ignored");
 //                            break;
 //                        }
 //                    }
 //
 //                    //Unknown field id with unknown type so just create as binary
 //                    if (!existingId)
 //                    {
 //                        logger.warning("UnKnown Field:" + header.getId() + " with invalid field type of:" + type + " created as binary");
 //                        TagField field = new Mp4TagBinaryField(header.getId(), raw);
 //                        tag.addField(field);
 //                    }
 //                }
 //            }
 //            //Special Cases
 //            else
 //            {
 //                //MediaMonkey 3 CoverArt Attributes field, does not have data items so just
 //                //copy parent and child as is without modification
 //                if (header.getId().equals(Mp4NonStandardFieldKey.AAPR.getFieldName()))
 //                {
 //                    TagField field = new Mp4TagRawBinaryField(header, raw);
 //                    tag.addField(field);
 //                }
 //                //Default case
 //                else
 //                {
 //                    TagField field = new Mp4TagRawBinaryField(header, raw);
 //                    tag.addField(field);
 //                }
 //            }
 //        }
 
     }
     
     public Mp4TagField(String id) { this.id = id; }
     
     public Mp4TagField(String id, JBBuffer raw) throws Exception {
         this(id);
         build(raw);
     }
     
     public String getId() { return id; }
 
     public void isBinary(boolean b) { /* One cannot choose if an arbitrary block can be binary or not */ }
 
     public boolean isCommon() {
         return getId().equals("ART")   ||
         		getId().equals("alb")  ||
         		getId().equals("nam")  ||
         		getId().equals("trkn") ||
         		getId().equals("day")  ||
         		getId().equals("cmt")  ||
         		getId().equals("gen");
     }
     
     protected byte[] getIdBytes() { return Utils.getDefaultBytes(getId()); }
     
     protected abstract void build(JBBuffer raw) throws UnsupportedEncodingException, IOException, Exception ;
 }
