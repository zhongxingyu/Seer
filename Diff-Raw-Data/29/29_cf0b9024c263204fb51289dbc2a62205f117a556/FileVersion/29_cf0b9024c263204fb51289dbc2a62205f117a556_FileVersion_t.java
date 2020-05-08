 package module.contents.domain;
 
 import myorg.applicationTier.Authenticate.UserView;
 
import org.joda.time.DateTime;

 public class FileVersion extends FileVersion_Base implements Comparable<FileVersion> {
 
     public FileVersion(VersionedFile file, String displayName, String filename, byte[] content, int revision) {
 	super();
 	setCurrentVersionFile(file);
 	setFile(file);
 	init(displayName, filename, content);
 	setRevision(revision);
 	setUploader(UserView.getCurrentUser());
 	setDate(new DateTime());
     }
 
     public FileVersion(VersionedFile file, int revision) {
 	setFile(file);
 	setCurrentVersionFile(file);
 	setRevision(revision);
 	setUploader(UserView.getCurrentUser());
 	setDate(new DateTime());
     }
 
     @Override
     public int compareTo(FileVersion fileVersion) {
 	return Integer.valueOf(getRevision()).compareTo(fileVersion.getRevision());
     }
 
 }
