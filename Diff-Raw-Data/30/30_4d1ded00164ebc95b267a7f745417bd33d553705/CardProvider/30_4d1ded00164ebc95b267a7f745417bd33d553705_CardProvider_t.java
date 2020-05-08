 package edu.mit.mobile.android.livingpostcards.data;
 
 import android.content.ContentValues;
 import android.content.Context;
import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.net.Uri;
 import android.util.Log;
 import edu.mit.mobile.android.content.ForeignKeyDBHelper;
 import edu.mit.mobile.android.content.GenericDBHelper;
 import edu.mit.mobile.android.content.ProviderUtils;
 import edu.mit.mobile.android.content.QuerystringWrapper;
 import edu.mit.mobile.android.locast.Constants;
 import edu.mit.mobile.android.locast.data.JsonSyncableItem;
 import edu.mit.mobile.android.locast.data.NoPublicPath;
 import edu.mit.mobile.android.locast.net.NetworkClient;
 import edu.mit.mobile.android.locast.sync.SyncableSimpleContentProvider;
 
 public class CardProvider extends SyncableSimpleContentProvider {
 
     public static final String AUTHORITY = "edu.mit.mobile.android.livingpostcards";
 
     public static final int VERSION = 9;
 
     protected static final String TAG = CardProvider.class.getSimpleName();
 
     public CardProvider() {
         super(AUTHORITY, VERSION);
 
         final QuerystringWrapper cards = new QuerystringWrapper(new GenericDBHelper(Card.class) {
             @Override
             public void upgradeTables(SQLiteDatabase db, int oldVersion, int newVersion) {
                 db.beginTransaction();
                 if (Constants.DEBUG) {
                     Log.d(TAG, "upgrading " + getTable() + " from " + oldVersion + " to "
                             + newVersion);
                 }
 
                 try {
                     // we started managing upgrades with version 6
                     if (oldVersion >= 6) {
 
                         // NOTE always use string literals here and not constants
                         if (oldVersion == 6) {
                             // adding the web url
                             db.execSQL("ALTER TABLE card ADD COLUMN web_url TEXT");
                             invalidateLocalCards(db);
                         }
 
                         if (oldVersion <= 7) {
                             db.execSQL("ALTER TABLE card ADD COLUMN deleted BOOLEAN");
                         }
 
                     } else {
                         if (Constants.DEBUG) {
                             Log.d(TAG, "upgrading tables by dropping / recreating them");
                         }
                         // this deletes everything
                         super.upgradeTables(db, oldVersion, newVersion);
                     }
                     db.setTransactionSuccessful();
                 } finally {
                     db.endTransaction();
                 }
             }
 
             private void invalidateLocalCards(SQLiteDatabase db) {
                 final ContentValues cv = new ContentValues();
                 // invalidate all cards so they'll get updated.
                 cv.put("modified", 0);
                 cv.put("server_modified", 0);
                 db.update(getTable(), cv, null, null);
             }
         });
 
         // unused ATM
         // final GenericDBHelper users = new GenericDBHelper(User.class);
 
         // content://authority/card
         // content://authority/card/1
         addDirAndItemUri(cards, Card.PATH);
 
         // addDirAndItemUri(users, User.PATH);
 
         final ForeignKeyDBHelper cardmedia = new ForeignKeyDBHelper(Card.class, CardMedia.class,
                 CardMedia.COL_CARD) {
             @Override
             public void upgradeTables(SQLiteDatabase db, int oldVersion, int newVersion) {
                 if (Constants.DEBUG) {
                     Log.d(TAG, "upgrading " + getTable() + " from " + oldVersion + " to "
                             + newVersion);
                 }
                 db.beginTransaction();
                 try {
                     // started managing upgrades at version 6
                     if (oldVersion >= 6) {
                         // no changes between v6-7
                         // no changes between v7-8

                        // forgot to add the deleted column in rev 8
                        if (oldVersion <= 7
                                || (oldVersion <= 8 && !columnExists(db, "cardmedia", "deleted"))) {
                             db.execSQL("ALTER TABLE cardmedia ADD COLUMN deleted BOOLEAN");
                         }
 
                     } else {
                         if (Constants.DEBUG) {
                             Log.d(TAG, "upgrading tables by dropping / recreating them");
                         }
                         // this deletes everything
                         super.upgradeTables(db, oldVersion, newVersion);
                     }
                     db.setTransactionSuccessful();
                 } finally {
                     db.endTransaction();
                 }
 
             }
         };
 
         // content://authority/card/1/media
         // content://authority/card/1/media/1
         addChildDirAndItemUri(cardmedia, Card.PATH, CardMedia.PATH);
 
     }
 
    private boolean tableExists(SQLiteDatabase db, String table) {
        final Cursor c = db.rawQuery("SELECT COUNT() FROM sqlite_master WHERE name=?",
                new String[] { table });
        boolean tableExists = false;
        try {
            final int count = c.getInt(0);
            tableExists = count == 1;
        } finally {
            c.close();
        }
        return tableExists;
    }

    private boolean columnExists(SQLiteDatabase db, String table, String column) {
        final Cursor c = db.query(table, null, null, null, null, null, "_id LIMIT 1");
        try {
            return c.getColumnIndex(column) != -1;
        } finally {
            c.close();
        }
    }

     @Override
     public boolean canSync(Uri uri) {
         // TODO Auto-generated method stub
         Log.d(TAG, uri + " can sync");
         return true;
     }
 
     @Override
     public String getPostPath(Context context, Uri uri) throws NoPublicPath {
         // item post paths are the public path of the dir
         if (getType(uri).startsWith(ProviderUtils.TYPE_ITEM_PREFIX)) {
             uri = ProviderUtils.removeLastPathSegment(uri);
         }
         return getPublicPath(context, uri);
     }
 
     @Override
     public String getPublicPath(Context context, Uri uri) throws NoPublicPath {
         Log.d(TAG, "getPublicPath " + uri);
         final String type = getType(uri);
 
         // TODO this is the only hard-coded URL. This should be removed eventually.
         if (Card.TYPE_DIR.equals(type)) {
             return NetworkClient.getBaseUrlFromManifest(context) + "postcard/";
 
             // TODO find a way to make this generic. Inspect the SYNC_MAP somehow?
         } else if (CardMedia.TYPE_DIR.equals(type)) {
             return JsonSyncableItem.SyncChildRelation.getPathFromField(context,
                     CardMedia.getCard(uri), Card.COL_MEDIA_URL);
         } else {
             return super.getPublicPath(context, uri);
         }
     }
 
     @Override
     public String getAuthority() {
         return AUTHORITY;
     }
 }
