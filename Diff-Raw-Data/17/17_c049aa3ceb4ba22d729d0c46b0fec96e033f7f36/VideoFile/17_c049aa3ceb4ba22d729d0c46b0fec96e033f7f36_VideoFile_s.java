 package fr.dush.mediamanager.dto.media.video;
 
 import static com.google.common.collect.Lists.*;
 
 import java.nio.file.Path;
 import java.util.List;
 
 import lombok.Data;
 import lombok.EqualsAndHashCode;
 import lombok.NoArgsConstructor;
 import fr.dush.mediamanager.dto.media.MediaFile;
 
 /**
  * Video file.
  *
  * @author Thomas Duchatelle
  */
 @SuppressWarnings("serial")
 @Data
 @EqualsAndHashCode(callSuper = true, of = {})
 @NoArgsConstructor
 public class VideoFile extends MediaFile {
 
 	/** Video quality */
 	private Quality quality;
 
 	/** Next files if video is split between multiple part. */
	private List<Path> nextParts = newArrayList();
 
 	public VideoFile(Path file) {
 		super(file);
 	}
 
 	/**
 	 * If file is cut between multiple files. If true, this instance
 	 *
 	 * @return
 	 */
 	public boolean isMultipart() {
 		return !nextParts.isEmpty();
 	}
 
 	@Override
 	public String toString() {
 		return "VideoFile [getFile()=" + getFile() + ", nextParts=" + nextParts + "]";
 	}
 }
