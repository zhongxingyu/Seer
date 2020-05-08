 package il.ac.huji.chores;
 
 import android.content.ContentResolver;
 import android.content.Context;
 import android.database.Cursor;
 import android.provider.ContactsContract;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.CursorAdapter;
 import android.widget.Filterable;
 import android.widget.TextView;
 
 /**
 * User: yoav
 * Email: ${EMAIL}
 * Date: 06/09/13
 */
 class ContactsCursorAdapter extends CursorAdapter implements Filterable {
 
     private ContentResolver content;
     final String[] PEOPLE_PROJECTION = new String[] {
             ContactsContract.Contacts._ID,
             ContactsContract.Contacts.DISPLAY_NAME,
             ContactsContract.Contacts.HAS_PHONE_NUMBER,
             ContactsContract.Contacts.LOOKUP_KEY,
     };
 
     public ContactsCursorAdapter(Context context, Cursor c) {
         super(context, c);
         content = context.getContentResolver();
     }
 
     @Override
     public View newView(Context context, Cursor cursor, ViewGroup parent) {
         final LayoutInflater inflater = LayoutInflater.from(context);
         final View view;
         if (cursor.getInt(2) == 1) {
             view = inflater.inflate(R.layout.contacts_dropdown_layout, parent, false);
         } else {
            view =  inflater.inflate(R.layout.simple_dropdown_item, parent, false);
         }
         populateView(view, cursor);
         Log.d("ContactsCursorAdapter", "new view " + cursor.getString(1));
         return view;
     }
 
     private void populateView(View view, Cursor cursor) {
         if (cursor.getInt(2) == 1) {
             String phones = getPhones(cursor);
             ((TextView) view.findViewById(R.id.contactsDropdownLayoutName)).setText(cursor.getString(1));
             ((TextView) view.findViewById(R.id.contactsDropdownLayoutNumber)).setText(phones);
         } else {
        	((TextView) view.findViewById(R.id.text_item)).setText(cursor.getString(1));
         }
     }
 
     public String getPhones(Cursor cursor) {
         if (cursor.isClosed() || cursor.isBeforeFirst() || cursor.isAfterLast()) {
             Log.w("ContactsCursorAdapter", "Cursor invalid " + cursor.getPosition());
             return "";
         }
         Cursor phoneCursor = content.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                 ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY + " = ?", new String[]{cursor.getString(3)}, null);
         StringBuilder phones = new StringBuilder();
         while (phoneCursor.moveToNext()) {
             phones.append(phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
             phones.append("; ");
         }
         phoneCursor.close();
         return phones.toString();
     }
 
     @Override
     public void bindView(View view, Context context, Cursor cursor) {
         Log.d("ContactsCursorAdapter", "Binding view " + cursor.getString(1));
         populateView(view, cursor);
     }
 
     @Override
     public Cursor getCursor() {
         return super.getCursor();
     }
 
     @Override
     public String convertToString(Cursor cursor) {
         StringBuilder displayString = new StringBuilder();
         displayString.append(cursor.getString(1));
 /*        if (cursor.getInt(2) == 1) {
             displayString.append(" <").append(getPhones(cursor)).append(">");
         }*/
         return displayString.toString();
     }
 
     @Override
     public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
         if (getFilterQueryProvider() != null) {
             return getFilterQueryProvider().runQuery(constraint);
         }
 
         StringBuilder buffer = null;
         String[] args = null;
         if (constraint != null) {
             buffer = new StringBuilder();
             buffer.append("UPPER(");
             buffer.append(ContactsContract.Contacts.DISPLAY_NAME);
             buffer.append(") GLOB ?");
             args = new String[] { constraint.toString().toUpperCase() + "*" };
         }
 
         return content.query(ContactsContract.Contacts.CONTENT_URI, PEOPLE_PROJECTION,
                 buffer == null ? null : buffer.toString(), args,
                 ContactsContract.Contacts.SORT_KEY_PRIMARY);
     }
 }
