 package ch.bergturbenthal.raoa.provider;
 
 import java.io.IOException;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Date;
 import java.util.List;
 
 import org.codehaus.jackson.map.ObjectMapper;
 import org.codehaus.jackson.map.type.CollectionType;
 import org.codehaus.jackson.map.type.SimpleType;
 
 import android.content.ContentResolver;
 import android.net.Uri;
 import android.net.Uri.Builder;
 import android.os.Bundle;
 
 public class Client {
 	public static class Album {
 		public static final String ALBUM_CAPTURE_DATE = "albumCaptureDate";
 		public static final String ALBUM_ENTRIES_URI = "albumEntriesUri";
 		public static final String ARCHIVE_NAME = "archiveName";
 		public static final String AUTOADD_DATE = "autoAddDate";
 		public static final String ENTRY_COUNT = "entryCount";
 		public static final String ENTRY_URI = "entryUri";
 		public static final String ID = "id";
 		public static final String NAME = "name";
 		public static final String ORIGINALS_SIZE = "originalsSize";
 		public static final String REPOSITORY_SIZE = "repositorySize";
 		public static final String SHOULD_SYNC = "shouldSync";
 		public static final String SYNCED = "synced";
 		public static final String THUMBNAIL = "thumbnail";
 		public static final String THUMBNAILS_SIZE = "thumbnailsSize";
 		public static final String TITLE = "title";
 	}
 
 	public static class AlbumEntry {
 		public static final String CAMERA_MAKE = "cameraMake";
 		public static final String CAMERA_MODEL = "cameraModel";
 		public static final String CAPTURE_DATE = "captureDate";
 		public static final String ENTRY_TYPE = "entryType";
 		public static final String ENTRY_URI = "entryUri";
 		public static final String EXPOSURE_TIME = "exposureTime";
 		public static final String F_NUMBER = "fNumber";
 		public static final String FOCAL_LENGTH = "focalLength";
 		public static final String ID = "id";
 		public static final String ISO = "iso";
 		public static final String LAST_MODIFIED = "lastModified";
 		public static final String META_CAPTION = "metaCaption";
 		public static final String META_KEYWORDS = "metaKeywords";
 		public static final String META_RATING = "metaRating";
 		public static final String NAME = "fileName";
 		public static final String ORIGINAL_SIZE = "originalSize";
 		public static final String THUMBNAIL = "thumbnail";
 		public static final String THUMBNAIL_SIZE = "thumbnailSize";
 
 		public static Collection<String> decodeKeywords(final String keywordValue) {
 			try {
 				if (keywordValue == null) {
 					return Collections.emptyList();
 				}
 				return mapper.readValue(keywordValue, stringListType);
 			} catch (final IOException e) {
 				throw new RuntimeException("Cannot decode value " + keywordValue, e);
 			}
 		}
 
 		public static String encodeKeywords(final Collection<String> keywords) {
 			try {
 				return mapper.writeValueAsString(keywords);
 			} catch (final IOException e) {
 				throw new RuntimeException("Cannot encode value " + keywords, e);
 			}
 		}
 	}
 
 	public static class IssueEntry {
 		public static final String ALBUM_ENTRY_NAME = "fileName";
 		public static final String ALBUM_NAME = "albumName";
 		public static final String CAN_ACK = "canAck";
 		public static final String ID = "_id";
 		public static final String ISSUE_TIME = "issueTime";
 		public static final String ISSUE_TYPE = "issueType";
 		public static final String STACK_TRACE = "stackTrace";
 	}
 
 	public static class ProgressEntry {
 		public static final String CURRENT_STATE_DESCRIPTION = "currentStateDescription";
 		public static final String CURRENT_STEP_NR = "currentStepNr";
 		public static final String ID = "_id";
 		public static final String PROGRESS_DESCRIPTION = "progressDescription";
 		public static final String PROGRESS_ID = "progressId";
 		public static final String PROGRESS_TYPE = "progressType";
 		public static final String STEP_COUNT = "stepCount";
 	}
 
 	public static class ServerEntry {
 		public static final String ARCHIVE_NAME = "archiveName";
 		public static final String ID = "_id";
 		public static final String SERVER_ID = "serverId";
 		public static final String SERVER_NAME = "serverName";
 	}
 
 	public static final Uri ALBUM_URI;
 	public static final String AUTHORITY = "ch.bergturbenthal.raoa.provider";
 	public static final String METHOD_CREATE_ALBUM_ON_SERVER = "createAlbumOnServer";
 	public static final String PARAMETER_AUTOADD_DATE = "autoaddDate";
 	public static final String PARAMETER_FULL_ALBUM_NAME = "fullAlbumName";
 	public static final Uri SERVER_URI;
 	private static final ObjectMapper mapper = new ObjectMapper();
 	private static final CollectionType stringListType = CollectionType.construct(List.class, SimpleType.construct(String.class));
 	private final ContentResolver provider;
 
 	static {
 		ALBUM_URI = Uri.parse("content://" + AUTHORITY + "/albums");
 		SERVER_URI = Uri.parse("content://" + AUTHORITY + "/servers");
 	}
 
 	public static Uri makeAlbumEntriesUri(final String archiveName, final String albumId) {
 		final Builder builder = ALBUM_URI.buildUpon();
 		builder.appendPath(archiveName);
 		builder.appendPath(albumId);
 		builder.appendPath("entries");
 		return builder.build();
 	}
 
 	public static Uri makeAlbumEntryUri(final String archiveName, final String albumId, final String albumEntryId) {
 		final Builder builder = ALBUM_URI.buildUpon();
 		builder.appendPath(archiveName);
 		builder.appendPath(albumId);
 		builder.appendPath("entries");
 		builder.appendPath(albumEntryId);
 		return builder.build();
 	}
 
 	public static Uri makeAlbumUri(final String archiveName, final String albumId) {
 		final Builder builder = ALBUM_URI.buildUpon();
 		builder.appendPath(archiveName);
 		builder.appendPath(albumId);
 		return builder.build();
 	}
 
 	public static Uri makeServerIssueUri(final String serverId) {
 		final Builder builder = SERVER_URI.buildUpon();
 		builder.appendPath(serverId);
 		builder.appendPath("issues");
 		return builder.build();
 	}
 
 	public static Uri makeServerProgressUri(final String serverId) {
 		final Builder builder = SERVER_URI.buildUpon();
 		builder.appendPath(serverId);
 		builder.appendPath("progress");
 		return builder.build();
 	}
 
 	/**
 	 * Build a Content-Provider-URI for reading a given Thumbnail from Content-Provider
 	 * 
 	 * @param albumId
 	 *          id of album
 	 * @param entryId
 	 *          id of image
 	 * @return built URI
 	 */
 	public static Uri makeThumbnailUri(final String archiveName, final String albumId, final String albumEntryId) {
 		final Builder builder = ALBUM_URI.buildUpon();
 		builder.appendPath(archiveName);
 		builder.appendPath(albumId);
 		builder.appendPath("entries");
 		builder.appendPath(albumEntryId);
 		builder.appendPath("thumbnail");
 		return builder.build();
 	}
 
 	public Client(final ContentResolver provider) {
 		this.provider = provider;
 	}
 
 	public void createAlbumOnServer(final String serverId, final String fullAlbumName, final Date autoAddDate) {
 		assert fullAlbumName != null;
 		final Bundle extras = new Bundle();
 		extras.putString(PARAMETER_FULL_ALBUM_NAME, fullAlbumName);
 		if (autoAddDate != null) {
 			extras.putLong(PARAMETER_AUTOADD_DATE, autoAddDate.getTime());
 		}
 		provider.call(SERVER_URI, METHOD_CREATE_ALBUM_ON_SERVER, serverId, extras);
 	}
 }
