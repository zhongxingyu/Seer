 package no.uka.findmyapp.android.rest.contracts;
 
 import android.net.Uri;
 import android.provider.BaseColumns;
 
 /**
  * Defines a contract between the event content provider and its clients.
  * Clients should only use information given by this "contract" to fetch
  * data from the SensorProvider.
  */
 public final class UkaEvents implements BaseColumns{
 	private UkaEvents() {}
 
     /**
      * Defines the authority part of the URI
      */
	public static final String AUTHORITY = "no.uka.findmyapp.android.rest.providers"; 
 	
     /**
      * The scheme prefix
      */
 	public static final String SCHEME = "content://";
 
     /** 
      * Event table contract 
      */
 	public static class UkaEventContract implements BaseColumns {
 		
 		// This class cannot be instantiated
 		private UkaEventContract() {}
 		
 	    /**
 	     * The event table 
 	     */
 	    public static final String TABLE_NAME = "uka_events";
 	    
 		  /**
 		   * Create uka event table query
 		   */
 		  public static final String CREATE_TABLE_QUERY =
 			  "CREATE TABLE " + UkaEventContract.TABLE_NAME + " ("	
 			  + UkaEventContract.ID + " INTEGER PRIMARY KEY, "
 			  + UkaEventContract.EVENT_ID + " INTEGER, "
 			  + UkaEventContract.BILLING_ID + " INTEGER, "
 			  + UkaEventContract.ENTRANCE_ID + " INTEGER, "
 			  + UkaEventContract.TITLE + " varchar(255), "
 			  + UkaEventContract.LEAD + " varchar(255), " 
 			  + UkaEventContract.TEXT + " varchar(255), "
			  + UkaEventContract.PLACE + " varchar(30), "
 			  + UkaEventContract.EVENT_TYPE + " varchar(30), "
 			  + UkaEventContract.IMAGE + " varchar(100), "
 			  + UkaEventContract.THUMBNAIL + " varchar(100), "
 			  + UkaEventContract.HIDDEN_FROM_LISTING + " boolean, "
 			  + UkaEventContract.SLUG + " varchar(50), "
 			  + UkaEventContract.AGE_LIMIT + " INTEGER, "
 			  + UkaEventContract.DETAIL_PHOTO_ID + " INTEGER, "
 			  + UkaEventContract.SHOWING_TIME + " DATETIME, "
 			  + UkaEventContract.PUBLISH_TIME + " DATETIME, "
 			  + UkaEventContract.NETSALE_FROM + " DATETIME, "
 			  + UkaEventContract.NETSALE_TO + " DATETIME, "
 			  + UkaEventContract.FREE + " BOOLEAN, "
 			  + UkaEventContract.CANCELED + " BOOLEAN"
 			  + ");";
 		  
 		 /**
 		  * Drop uka events table
 		  */
 		public static final String DROP_TABLE_QUERY = 
 			"DROP TABLE IF EXISTS" + TABLE_NAME + ";";
 		
 	    /**
 	     * The default sort order for this table
 	     */
 	    public static final String DEFAULT_SORT_ORDER = "modified DESC";
 		
         /**
          * Path part for the event URI
          */
         public static final String EVENTS_PATH = "/events";
 
         /**
          * Path part for the event ID URI
          */
         public static final String EVENTS_ID_PATH = "/event_samples/";
 
         /**
          * 0-relative position of a event ID segment in the path part of a event ID URI
          */
         public static final int EVENTS_ID_PATH_POSITION = 1;
 
         /**
          * The content:// style URL for this table
          */
         public static final Uri EVENT_CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + EVENTS_PATH);
 
         /**
          * The content URI base for a single event. Callers must
          * append a numeric event id to this Uri to retrieve a 
          * event.
          */
         public static final Uri EVENT_CONTENT_ID_URI_BASE = Uri.parse(SCHEME + AUTHORITY + EVENTS_ID_PATH);
 
         /**
          * The content URI match pattern for a single event, specified by its ID. Use this to match
          * incoming URIs or to construct an Intent.
          */
         public static final Uri EVENT_CONTENT_ID_URI_PATTERN = Uri.parse(SCHEME + AUTHORITY + EVENTS_PATH + "/#");
 
 		/*
 		 * MIME-type definition 
 		 */
 	    /**
 	     * The MIME type of {@link #CONTENT_URI_EVENT} providing a directory of event.
 	     */
 	    public static final String CONTENT_TYPE_EVENT = "vnd.android.cursor.dir/vnd.no.uka.event";
 	    
 	    /**
 	     * The MIME type of a {@link #CONTENT_URI_EVENT} sub-directory of a single
 	     * event sample.
 	     */
 	    public static final String CONTENT_ITEM_EVENT = "vnd.android.cursor.item/vnd.no.uka.event";
 		
 		 /*
          * Database columns 
          */
         /**
          * Column name for the id
          * <P>Type: INTEGER</P>
          */
 		public static final String ID = "_id";
 		
         /**
          * Column name for the event id
          * <P>Type: INTEGER</P>
          */
 		public static final String EVENT_ID = "event_id";
 		
         /**
          * Column name for the billing id
          * <P>Type: INTEGER</P>
          */
 		public static final String BILLING_ID = "billing_id";
 		
         /**
          * Column name for the entrance id
          * <P>Type: INTEGER</P>
          */
 		public static final String ENTRANCE_ID = "entrance_id";
 		
         /**
          * Column name for the event title
          * <P>Type: VARCHAR(255)</P>
          */
 		public static final String TITLE = "title";
 		
 		/**
 		 * Column name for the events lead
 		 * text/discription.
 		 * <P>Type: VARCHAR(255)</P>
 		 */
 		public static final String LEAD = "lead"; 
 		
 		/**
 		 * Column name for the event description
 		 * <P>Type: VARCHAR(255)</P>
 		 */
 		public static final String TEXT = "text";
 		
         /**
          * Column name for the event place
          * <P>Type: VARCHAR(30)</P>
          */
 		public static final String PLACE = "place";
 		
         /**
          * Column name for the event type/
          * category field
          * <P>Type: VARCHAR(30)</P>
          */
 		public static final String EVENT_TYPE = "event_type";
 		
         /**
          * Column name for the image url
          * <P>Type: VARCHAR(100)</P>
          */
 		public static final String IMAGE = "image";
 	
         /**
          * Column name for the image thumbnail
          *  url
          * <P>Type: VARCHAR(100)</P>
          */
 		public static final String THUMBNAIL = "thumbnail";
 		
         /**
          * Column name for the 
          * hidden_from_listing flag
          * <P>Type: BOOLEAN</P>
          */
 		public static final String HIDDEN_FROM_LISTING = "hiddden_from_listing";
 		
         /**
          * Column name for the slug
          * <P>Type: VARCHAR(50)</P>
          */
 		public static final String SLUG = "slug";
 		
         /**
          * Column name for the age limit
          * <P>Type: SMALLINT</P>
          */
 		public static final String AGE_LIMIT = "age_limit";
 		
         /**
          * Column name for the detail photo id
          * <P>Type: INTEGER</P>
          */
 		public static final String DETAIL_PHOTO_ID = "detail_photo_id";
 		
 		/**
          * Column name for the showing time timestamp
          * <P>Type: DATETIME</P>
          */
         public static final String SHOWING_TIME = "showing_time";
         
         /**
          * Column name for the publish time timestamp
          * <P>Type: DATETIME</P>
          */
         public static final String PUBLISH_TIME = "publish_time";
         
         /**
          * Column name for thenetsaleFrom timestamp
          * <P>Type: DATETIME</P>
          */
         public static final String NETSALE_FROM = "netsale_from";
         
         /**
          * Column name for the netsaleTo timestamp
          * <P>Type: DATETIME</P>
          */
         public static final String NETSALE_TO = "netsale_to";
         
         /**
          * Column name for the free flag 
          * <P>Type: BOOLEAN</P>
          */
 		public static final String FREE = "free";
 		
 		 /**
          * Column name for the canceled flag
          * <P>Type: BOOLEAN</P>
          */
 		public static final String CANCELED = "canceled";
 	}
 }
