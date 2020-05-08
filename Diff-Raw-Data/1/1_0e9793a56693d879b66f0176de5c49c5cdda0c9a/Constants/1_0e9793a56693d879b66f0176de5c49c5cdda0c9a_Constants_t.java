 package models;
 
 public class Constants {
 
 	public static class json_fields
 	{
 		public static final String INDEX_FIELD_INDEX = "index";
 		public static final String INDEX_FIELD_IMAGES = "images";
 		public static final String INDEX_FIELD_IMAGE_ID = "id";
 		public static final String INDEX_FIELD_IMAGE_URL = "url";
 	
 		public static final String QUERY_FIELD_SOURCE = "source";
 		public static final String QUERY_FIELD_FILEID = "fileidentifier";
 		public static final String QUERY_FIELD_FEATURE = "feature";
 		public static final String QUERY_FIELD_NUMOFRESULT = "numberofresults";
 	}
 
 	public static class db_fields 
 	{	
 		public static final int MEDIA_DOWNLOADED = 1;
 		public static final int MEDIA_INVALID = 2;
 		public static final int MEDIA_INDEXED = 4;
 		public static final int	MEDIA_QUEUEDFORINDEXING = 8;
 		public static final int MEDIA_INDEXINGEXCEPTION = 16;
		public static final int MEDIA_UNKNOWNFORMAT = 32;
 	}
 
 	public static final String FOLDER_INDICES = play.Play.application().getFile("/indices").getAbsolutePath();
 	public static final String FOLDER_PUBLIC = play.Play.application().getFile("/public").getAbsolutePath();
 
 	public static final String FOLDER_3CLASS = play.Play.application().getFile("/public/classifiers/english.all.3class.distsim.crf.ser.gz").getAbsolutePath();
 	public static final String FOLDER_7CLASS = play.Play.application().getFile("/public/classifiers/english.muc.7class.distsim.crf.ser.gz").getAbsolutePath();
 }
