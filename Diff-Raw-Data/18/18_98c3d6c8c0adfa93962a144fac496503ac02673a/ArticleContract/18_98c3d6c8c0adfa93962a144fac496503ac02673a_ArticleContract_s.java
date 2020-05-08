 package se.alexanderblom.apk;
 
 import android.content.ContentUris;
 import android.net.Uri;
 import android.provider.BaseColumns;
 
 public class ArticleContract {
 	public static final String CONTENT_AUTHORITY = "se.alexanderblom.apk";
 	public static final Uri CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY + "/article");
 	
	public static final String DEFAULT_SORT_ORDER = Columns.APK + " ASC";
 	
 	public interface Columns extends BaseColumns {
 		static final String NAME = "name";
 		static final String NAME2 = "name2";
 		static final String PRICE = "price";
 		static final String VOLUME = "volume";
 		static final String PRICE_PER_LITER = "price_per_liter";
 		static final String PRODUCT_GROUP = "product_group";
 		static final String PACKAGING = "packaging";
 		static final String ORIGIN = "origin";
 		static final String ORIGIN_COUNTRY = "origin_country";
 		static final String PRODUCER = "producer";
 		static final String DISTRIBUTOR = "distributor";
 		static final String YEAR = "year";
 		static final String ALCOHOL_PERCENTAGE = "alcohol_percentage";
 		static final String INGREDIENTS = "ingredients";
 		static final String APK = "apk";
 	}
 	
 	public static Uri getUri(long id) {
 		return ContentUris.withAppendedId(CONTENT_URI, id);
 	}
 	
 	private ArticleContract() {}
 }
