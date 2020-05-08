 package bazik.android.sms;
 
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.database.Cursor;
 import android.net.Uri;
 
 public class Model
 {
 	/* column names for sms provider:
     0: _id
     1: thread_id
     2: address
     3: person
     4: date
     5: protocol
     6: read   
     7: status
     8: type
     9: reply_path_present
     10: subject
     11: body
     12: service_center
     13: locked
     */
 	private String columns[] = new String[] {"_id", "thread_id", "address", "person", "date", "protocol", "read", "status", "type", "reply_path_present", "subject", "body", "service_center", "locked"};
 	private static String inbox_url = "content://sms/inbox";
 	private static String sent_url = "content://sms/sent";
 	private Activity context;
 	private Uri uri;
 	
 	public Model(Activity context, String type)
 	{
 		this.context = context;
 		String url = "";
     	if(type == "received") url = inbox_url;
     	if(type == "sent") url = sent_url;	
         
     	this.uri = Uri.parse(url);
 	}
 	
     public ArrayList<Message> getMessages(String type)
     {
     	String url = "";
     	if(type == "received") url = inbox_url;
     	if(type == "sent") url = sent_url;	
         
     	Uri uri = Uri.parse(url);
     	
         Cursor cur = context.managedQuery(uri, 
         this.columns, // Which columns to return
         null, // WHERE clause; which rows to return(all rows)
         null, // WHERE clause selection arguments (none)
         null); // Order-by clause (ascending by name)
         
         int i = 0;
         ArrayList<Message> messages = new ArrayList<Message>();
         //Message messages[] = {};
         try{
         	for (i = 0; i < cur.getCount() - 1; i++) { 
             	cur.move(1);
             	
             	String id = cur.getString(cur.getColumnIndex("_id"));
             	String thread_id = cur.getString(cur.getColumnIndex("thread_id"));
             	String address = cur.getString(cur.getColumnIndex("address"));
             	String person = cur.getString(cur.getColumnIndex("person"));
             	String date = cur.getString(cur.getColumnIndex("date"));
             	String protocol = cur.getString(cur.getColumnIndex("protocol"));
             	String read = cur.getString(cur.getColumnIndex("read"));
             	String status = cur.getString(cur.getColumnIndex("status"));
             	String reply_path_present = cur.getString(cur.getColumnIndex("reply_path_present"));
             	String subject = cur.getString(cur.getColumnIndex("subject"));
             	String body = cur.getString(cur.getColumnIndex("body"));
             	String service_center = cur.getString(cur.getColumnIndex("service_center"));
             	String locked = cur.getString(cur.getColumnIndex("locked"));
             	
            	ContactsManager contactsManager = new ContactsManager();
             	//contactsManager.getPerson(this.context, address);
             	
            	Message message = new Message(id, thread_id, address, person, date, protocol, read, type, reply_path_present, subject, body, service_center, locked);
             	messages.add(message);
             }
             cur.close();
         }
         catch(ArrayIndexOutOfBoundsException e)
         {
         	System.out.println(e.toString() + " " + Integer.toString(i));
         }
         return messages;
     }
 	
 	/**
 	 * @return the columns
 	 */
 	public String[] getColumns() {
 		return this.columns;
 	}
 
 	/**
 	 * @return the uri
 	 */
 	public Uri getUri() {
 		return this.uri;
 	}
 }
