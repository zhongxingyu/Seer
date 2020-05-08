 package com.capgemini.igor.pusher;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import android.content.ContentProviderOperation;
 import android.content.OperationApplicationException;
 import android.content.ContentProviderOperation.Builder;
 import android.os.AsyncTask;
 import android.os.RemoteException;
 import android.provider.ContactsContract;
 import android.provider.ContactsContract.Data;
 import android.provider.ContactsContract.RawContacts;
 import android.provider.ContactsContract.CommonDataKinds.Nickname;
 import android.provider.ContactsContract.CommonDataKinds.Phone;
 import android.provider.ContactsContract.CommonDataKinds.StructuredName;
 import android.util.Log;
 
 import com.capgemini.igor.CreateContacts;
 
 public class ContactPusher extends AsyncTask<Void, Integer, Integer> {
 
 	private static final String tag = "Igor:ContactPusher";
 
 	public final CreateContacts parent;
 
 	public ContactPusher(CreateContacts parent) {
 		assert parent != null;
 
 		this.parent = parent;
 	}
 
 	@Override
 	protected Integer doInBackground(Void... params) {
 		Log.i(tag, "Starting contact push.");
 
 		try {
 			return createContacts();
 		} catch (RemoteException e) {
 			e.printStackTrace();
 		} catch (OperationApplicationException e) {
 			e.printStackTrace();
 		}
 
 		Log.i(tag, "Exiting contact push thread with error code.");
 		return -1;
 	}
 
 	@Override
 	protected void onPreExecute() {
 		super.onPreExecute();
 		parent.createContactsStartedCallback();
 	}
 
 	@Override
 	protected void onProgressUpdate(Integer... values) {
 		super.onProgressUpdate(values);
 		parent.createContactsUpdateCallback(values[0]);
 	}
 
 	@Override
 	protected void onPostExecute(Integer result) {
 		super.onPostExecute(result);
 		parent.createContactsFinishedCallback();
 	}
 
 	private int createContacts() throws RemoteException, OperationApplicationException {
 		int i = 0;
 
		for (; i < 20 && !Thread.interrupted(); i++) {
 			Log.v(tag, "creating contact #" + i);
 			createRawContact(i);
 			publishProgress(i);
 		}
 
 		return i;
 	}
 
 	private void createRawContact(int i) throws RemoteException, OperationApplicationException {
 		Builder accountBuilder = getAccountBuilder();
 
 		Builder rawContactBuilder = getStructuredNameBuilder(i);
 
 		Builder phoneBuilder = getPhoneBuilder(i);
 
 		Builder nicknameBuilder = getNicknameBuilder(i);
 
 		ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();
 
 		operations.add(accountBuilder.build());
 		operations.add(rawContactBuilder.build());
 		operations.add(phoneBuilder.build());
 		operations.add(nicknameBuilder.build());
 
 		parent.getContentResolver().applyBatch(ContactsContract.AUTHORITY, operations);
 	}
 
 	private Builder getAccountBuilder() {
 		Builder accountBuilder = ContentProviderOperation.newInsert(RawContacts.CONTENT_URI)
 				.withValue(RawContacts.ACCOUNT_NAME, null)
 				.withValue(RawContacts.ACCOUNT_TYPE, null);
 		return accountBuilder;
 	}
 
 	private Builder getNicknameBuilder(int i) {
 		Builder nicknameBuilder = ContentProviderOperation.newInsert(Data.CONTENT_URI)
 				.withValueBackReference(Data.RAW_CONTACT_ID, 0)
 				.withValue(Data.MIMETYPE, Nickname.CONTENT_ITEM_TYPE)
 				.withValue(Nickname.NAME, D.nickname(i))
 				.withValue(Nickname.TYPE, Nickname.TYPE_CUSTOM)
 				.withValue(Nickname.LABEL, "Superhero");
 		return nicknameBuilder;
 	}
 
 	private Builder getPhoneBuilder(int i) {
 		Builder phoneBuilder = ContentProviderOperation.newInsert(Data.CONTENT_URI)
 				.withValueBackReference(Data.RAW_CONTACT_ID, 0)
 				.withValue(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE)
 				.withValue(Phone.NUMBER, D.phonenumber(i))
 				.withValue(Phone.TYPE, Phone.TYPE_HOME)
 				.withValue(Phone.LABEL, "phoneLabel");
 		return phoneBuilder;
 	}
 
 	private Builder getStructuredNameBuilder(int i) {
 		Builder rawContactBuilder = ContentProviderOperation.newInsert(Data.CONTENT_URI)
 				.withValueBackReference(Data.RAW_CONTACT_ID, 0)
 				.withValue(Data.MIMETYPE, StructuredName.MIMETYPE)
 				.withValue(StructuredName.DISPLAY_NAME, D.displayname(i))
 				.withValue(StructuredName.FAMILY_NAME, D.familyname(i))
 				.withValue(StructuredName.GIVEN_NAME, D.givenname(i));
 		return rawContactBuilder;
 	}
 
 	private static class D {
 		private static List<String> nicknames = Arrays.asList("Navy Laser",
 				"Beauty Moon",
 				"Boiling Pet",
 				"Brave Heavy Prince",
 				"Colt Stony",
 				"Disco Crunchy Beast",
 				"DuckieDuckie",
 				"El Froglet",
 				"Elastic Yodelers",
 				"Fisty Volunteer",
 				"Heavy Demon",
 				"Misty Rough Crystal",
 				"Mutant Circus",
 				"Mysterious Villain",
 				"Needless Freaky Nymph",
 				"Pioneer Rotten",
 				"Thirsty Genius",
 				"Tough Pioneer",
 				"Tumbler Old",
 				"Stray Kid");
 
 		private static List<String> phoneNumbers = Arrays.asList("90909090",
 				"004790909091",
 				"90909092",
 				"90909093",
 				"+4790909094",
 				"+4790909095",
 				"004790909096",
 				"004790909097",
 				"90909098",
 				"90909099",
 				"+4790909100",
 				"+4790909101",
 				"004790909102",
 				"004790909103",
 				"90909104",
 				"90909105",
 				"+4790909106",
 				"+4790909107",
 				"004790909108",
 				"004790909109");
 
 		private static List<String> names = Arrays.asList("Selena Tonn",
 				"Alana Kratochvil",
 				"Allan Keas",
 				"Althea Kondo",
 				"Bohannan Temples",
 				"Clinton Smartt",
 				"Cody Cranfield",
 				"Kody Libbey",
 				"Codi Wickersham",
 				"Dollie Turlington",
 				"Javier Mcconnaughey",
 				"Kelly Neiman",
 				"Kelli Chamberland",
 				"Kurt Henne",
 				"Lance Cacciatore",
 				"Margery Abe",
 				"Melisa Ostrowski",
 				"Odessa Tallarico",
 				"Penelope Flore",
 				"Tameka Mccammon");
 
 		public static String phonenumber(int i) {
 			return phoneNumbers.get(i);
 		}
 
 		public static String nickname(int i) {
 			return nicknames.get(i);
 		}
 
 		public static String givenname(int i) {
 			return names.get(i).split("\\s")[0];
 		}
 
 		public static String familyname(int i) {
 			return names.get(i).split("\\s")[1];
 		}
 
 		public static String displayname(int i) {
 			return names.get(i);
 		}
 	}
 
 }
