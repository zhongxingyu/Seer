 package uk.co.unclealex.music.covers;
 
 import org.jaudiotagger.audio.mp3.MP3File;
 import org.jaudiotagger.tag.Tag;
 
 public class Mp3JAudioTaggerArtworkManager extends JAudioTaggerArtworkManager<MP3File> {
 
 	@Override
 	protected Tag getTag(MP3File audioFile) {
		return audioFile.getID3v2Tag();
 	}
 }
