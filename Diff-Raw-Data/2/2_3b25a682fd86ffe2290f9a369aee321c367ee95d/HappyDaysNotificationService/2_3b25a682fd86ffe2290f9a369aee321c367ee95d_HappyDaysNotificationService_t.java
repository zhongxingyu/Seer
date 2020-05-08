 package uk.co.cameronhunter.happydays;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import android.app.IntentService;
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.ContentResolver;
 import android.content.Context;
 import android.content.Intent;
 import android.database.Cursor;
 import android.net.Uri;
 import android.provider.ContactsContract;
 import android.util.Pair;
 
 public class HappyDaysNotificationService extends IntentService {
 
 	private static final SimpleDateFormat FULL_DATE_FORMAT = new SimpleDateFormat( "yyyy-MM-dd" );
 	private static final SimpleDateFormat NO_YEAR_DATE_FORMAT = new SimpleDateFormat( "--MM-dd" );
 
 	public HappyDaysNotificationService() {
 		super( "Birthday Notification Service" );
 	}
 
 	@Override
 	protected void onHandleIntent( Intent intent ) {
 		Date now = new Date( System.currentTimeMillis() );
 		notifyBirthdays( getApplicationContext(), now.getDate(), now.getMonth() + 1, now.getYear() );
 	}
 
 	private void notifyBirthdays( Context context, int day, int month, int year ) {
 		String dateFormat = String.format( "%02d-%02d", month, day );
 
 		String[] selection = { ContactsContract.Contacts.DISPLAY_NAME, ContactsContract.CommonDataKinds.Event.START_DATE, ContactsContract.CommonDataKinds.Event.CONTACT_ID,
 				ContactsContract.CommonDataKinds.Event.TYPE };
 		String where = ContactsContract.Data.MIMETYPE + "= ? " + "AND " + ContactsContract.CommonDataKinds.Event.TYPE + " IN (" + ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY + ", "
 				+ ContactsContract.CommonDataKinds.Event.TYPE_ANNIVERSARY + ") " + "AND substr(" + ContactsContract.CommonDataKinds.Event.START_DATE + ", -5, 5)= ?";
 		String[] args = { ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE, dateFormat };
 		String order = ContactsContract.Contacts.LAST_TIME_CONTACTED + " ASC";
 
 		ContentResolver cr = context.getContentResolver();
 		Cursor cursor = cr.query( ContactsContract.Data.CONTENT_URI, selection, where, args, order );
 
 		if ( cursor.getCount() > 0 ) {
 			while ( cursor.moveToNext() ) {
 				try {
 					String contactName = cursor.getString( 0 );
 					String dateText = cursor.getString( 1 );
 					String id = cursor.getString( 2 );
 					int type = cursor.getInt( 3 );
 
 					boolean hasYear = hasYear( dateText );
 
 					SimpleDateFormat format = hasYear ? FULL_DATE_FORMAT : NO_YEAR_DATE_FORMAT;
 					Date happyDate = format.parse( dateText );
 					
 					Pair<String, String> message = getNotificationMessage( contactName, type, hasYear ? (year - happyDate.getYear()) : 0 );
 
 					notification( Uri.withAppendedPath( ContactsContract.Contacts.CONTENT_URI, id ), message.hashCode(), message.first, message.second, context );
 				}
 				catch ( ParseException ignore ) {}
 			}
 		}
 
 		cursor.close();
 	}
 
 	private Pair<String, String> getNotificationMessage( String contact, int type, int years ) {
 
 		int title;
 		int message;
 
 		switch ( type ) {
 			case ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY:
 				title = R.string.notification_birthday_title;
 				message = R.plurals.notification_birthday_message;
 				break;
 
 			case ContactsContract.CommonDataKinds.Event.TYPE_ANNIVERSARY:
 				title = R.string.notification_anniversary_title;
 				message = R.plurals.notification_anniversary_message;
 				break;
 
 			default:
 				return null;
 		}
 
		return Pair.create( getString( title, contact ), getResources().getQuantityString( message, years, years ) );
 	}
 
 	private static boolean hasYear( String birthday ) {
 		return !birthday.startsWith( "-" );
 	}
 
 	private void notification( Uri uri, int id, String title, String message, Context context ) {
 
 		NotificationManager notificationManager = (NotificationManager) context.getSystemService( NOTIFICATION_SERVICE );
 
 		Notification notification = new Notification( android.R.drawable.ic_menu_my_calendar, title, System.currentTimeMillis() );
 
 		Intent intent = new Intent( Intent.ACTION_VIEW, uri );
 
 		PendingIntent contentIntent = PendingIntent.getActivity( context, 0, intent, 0 );
 		notification.setLatestEventInfo( context, title, message, contentIntent );
 		notificationManager.notify( id, notification );
 	}
 
 }
