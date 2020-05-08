 package fr.dush.mediamanager.dto.media;
 
 import java.io.Serializable;
 import java.nio.file.Path;
 
 import lombok.Data;
 import lombok.EqualsAndHashCode;
 import lombok.NoArgsConstructor;
 
 /**
  * Media file of any type.
  *
  * @author Thomas Duchatelle
  */
 @SuppressWarnings("serial")
 @Data
 @EqualsAndHashCode(of = { "hash", "file" })
 @NoArgsConstructor
 public class MediaFile implements Serializable {
 
 	/** File sha1 hash, used to identify file and find duplicates */
 	private String hash;
 
 	/** Full path file */
	private String file;
 
	public MediaFile(String file) {
 		this.file = file;
 	}

	public MediaFile(Path file) {
		this.file = file.toAbsolutePath().normalize().toString();
	}
 }
