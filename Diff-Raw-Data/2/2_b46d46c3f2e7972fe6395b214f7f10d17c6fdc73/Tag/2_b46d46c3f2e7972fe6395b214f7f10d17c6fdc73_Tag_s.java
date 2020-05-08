 package com.github.signed.mp3;
 
 import org.jaudiotagger.tag.id3.ID3v24FieldKey;
 import org.jaudiotagger.tag.id3.ID3v24Frames;
 
 public enum Tag {
     Title(ID3v24FieldKey.TITLE),
     Album(ID3v24FieldKey.ALBUM),
     Track(ID3v24FieldKey.TRACK),
     Artist(ID3v24FieldKey.ARTIST),
     Comment(ID3v24FieldKey.COMMENT),
     EncodedBy(ID3v24FieldKey.ENCODER),
     Publisher(ID3v24FieldKey.RECORD_LABEL),
     Genre(ID3v24FieldKey.GENRE),
     OriginalArtist(ID3v24FieldKey.ORIGINAL_ARTIST),
     Year(ID3v24FieldKey.YEAR),
 
     Copyright(ID3v24Frames.FRAME_ID_COPYRIGHTINFO),
     UserDefinedInformation(ID3v24Frames.FRAME_ID_USER_DEFINED_INFO),
     Private(ID3v24Frames.FRAME_ID_PRIVATE),
     MusicCdIdentifier(ID3v24Frames.FRAME_ID_MUSIC_CD_ID),
     EncoderSettings(ID3v24Frames.FRAME_ID_HW_SW_SETTINGS),
     MusicCdiIdentifier(ID3v24Frames.FRAME_ID_MUSIC_CD_ID),
     LengthInMilliseconds(ID3v24Frames.FRAME_ID_LENGTH),
     APIC(ID3v24Frames.FRAME_ID_ATTACHED_PICTURE),
     TXXX("TXXX")
     ;
 
     private final String frameId;
     private ID3v24FieldKey key = null;
 
     Tag(String frameId) {
         this.frameId = frameId;
     }
 
     Tag(ID3v24FieldKey key) {
         this(key.getFrameId());
        this.key = key;Tag
     }
 
     public String frameId() {
         return frameId;
     }
 
     public ID3v24FieldKey getFieldKey() {
         if( null == key) {
             throw new RuntimeException("fix it!");
         }
         return key;
     }
 
     public static Tag lookup(String tagAsString) {
         for (Tag tag : Tag.values()) {
             if(tagAsString.equals(tag.frameId)) {
                 return tag;
             }
         }
         throw new RuntimeException("there is no tag for " + tagAsString);
     }
 }
