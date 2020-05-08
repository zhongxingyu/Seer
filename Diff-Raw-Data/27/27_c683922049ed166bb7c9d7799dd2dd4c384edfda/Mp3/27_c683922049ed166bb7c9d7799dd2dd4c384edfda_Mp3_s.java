 package com.github.signed.mp3;
 
 import org.jaudiotagger.audio.exceptions.CannotReadException;
 import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
 import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
 import org.jaudiotagger.audio.mp3.MP3File;
 import org.jaudiotagger.tag.FieldDataInvalidException;
 import org.jaudiotagger.tag.FieldKey;
 import org.jaudiotagger.tag.TagException;
 import org.jaudiotagger.tag.id3.AbstractTagFrameBody;
 import org.jaudiotagger.tag.id3.ID3v24Frames;
 import org.jaudiotagger.tag.id3.framebody.AbstractFrameBodyTextInfo;
 import org.jaudiotagger.tag.id3.framebody.FrameBodyTALB;
 import org.jaudiotagger.tag.id3.framebody.FrameBodyTRCK;
 
 import java.io.File;
 import java.io.IOException;
 import java.nio.file.Path;
 
 import static org.jaudiotagger.tag.FieldKey.TITLE;
 import static org.jaudiotagger.tag.id3.ID3v24Frames.FRAME_ID_ACCOMPANIMENT;
 import static org.jaudiotagger.tag.id3.ID3v24Frames.FRAME_ID_ALBUM;
 import static org.jaudiotagger.tag.id3.ID3v24Frames.FRAME_ID_ARTIST;
 import static org.jaudiotagger.tag.id3.ID3v24Frames.FRAME_ID_TITLE;
 
 public class Mp3 {
 
     public static Mp3 From(Mp3 mp3){
         return From(mp3.mp3File.getFile().toPath());
     }
 
     public static Mp3 From(Path path) {
         try {
             File file = path.toFile();
             MP3File wrapped = new MP3File(file);
             return new Mp3(wrapped);
         } catch (IOException | TagException | ReadOnlyFileException | InvalidAudioFrameException e) {
             throw new RuntimeException(e);
         }
     }
 
     private MP3File mp3File;
     private final Tags tags;
 
     public Mp3(MP3File mp3File) {
         this.mp3File = mp3File;
         this.tags = Tags.ExtractFrom(mp3File);
     }
 
     public void saveChanges() {
         try {
             mp3File.save();
         } catch (IOException | TagException e) {
             throw new RuntimeException(e);
         }
     }
 
     public void isAuthorSet(){
         dumpNotExistingTag(FRAME_ID_ARTIST);
         dumpNotExistingTag(FRAME_ID_ACCOMPANIMENT);
     }
 
     private void dumpNotExistingTag(String tagId){
         boolean has = tags.hasFrameFor(tagId);
         if(!has){
             System.out.println(mp3File.getFile().getAbsolutePath());
             System.out.println(tagId + ": n.a.");
         }
     }
 
     public void setTitleTo(String newTitle) throws CannotReadException, IOException, TagException, ReadOnlyFileException, InvalidAudioFrameException {
         AbstractFrameBodyTextInfo body = getTitleTag();
         String oldTitle = body.getText();
         System.out.println("old title: " + oldTitle);
         System.out.println("new title: " + newTitle);
         body.setText(newTitle);
     }
 
     private AbstractFrameBodyTextInfo getTitleTag() {
         AbstractTagFrameBody body1 = tags.createFrameFor(FRAME_ID_TITLE, TITLE);
         return (AbstractFrameBodyTextInfo) body1;
     }
 



     public void provideTitleTo(CallbackWithFallback<String> callback) {
         if(tags.hasFrameFor(FRAME_ID_TITLE)){
             callback.call(getTitleTag().getText());
         }else{
             callback.fallback();
         }
     }
 
     public void setTrackNumberTo(Integer current, Integer total) throws FieldDataInvalidException {
         FrameBodyTRCK body = getTrackTag();
         System.out.println("before: " + body.getUserFriendlyValue());
         body.setTrackNo(current);
         body.setTrackTotal(total);
         System.out.println("after: " + body.getUserFriendlyValue());
         System.out.println("track number set to: " + body.getUserFriendlyValue());
     }
 
     private FrameBodyTRCK getTrackTag() {
         AbstractTagFrameBody box = tags.createFrameFor(ID3v24Frames.FRAME_ID_TRACK, FieldKey.TRACK);
         return (FrameBodyTRCK) box;
     }
 
     public void dumpAllTags() {
         tags.dumpTagsTo(System.out);
     }
 
     public void setAlbumName(String albumName) {
         FrameBodyTALB frameBodyTALB = getAlbumFrame();
         frameBodyTALB.setText(albumName);
     }
 
     private FrameBodyTALB getAlbumFrame() {
         AbstractTagFrameBody body = tags.createFrameFor(FRAME_ID_ALBUM, FieldKey.ALBUM);
         return (FrameBodyTALB) body;
     }
 
     public void drop(Tag tag){
         tags.dropFrameFor(tag);
     }
 }
