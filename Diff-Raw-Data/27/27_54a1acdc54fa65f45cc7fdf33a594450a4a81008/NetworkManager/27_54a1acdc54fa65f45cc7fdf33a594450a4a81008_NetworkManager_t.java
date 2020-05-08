 package org.AndroidShareApp.core;
 
 import java.util.ArrayList;
 import java.util.Random;
 
 public class NetworkManager {
 	
 	private static NetworkManager mSingleton=null;
 	private ArrayList<Person> mPersonList;
 	private ArrayList<FileTransferrer> mCurrentTransfers;
 	private ArrayList<SharedByMeItem> mSharedByMeItems;
 	private String mThisDeviceId;
 	private String mThisDevideName;
 	private NetworkSender mNetworkSender;
 	private NetworkListener mNetworkListener;
 
 	private NetworkManager() {
 		mPersonList = new ArrayList<Person>();
 		mCurrentTransfers = new ArrayList<FileTransferrer>();
 		mSharedByMeItems = new ArrayList<SharedByMeItem>();
 		
 		SharedPerson everybody = new SharedPerson("Everybody",String.valueOf(new Random().nextInt()),false,false);
 		addPerson(everybody);
 		
 		/* TODO: REMOVE!!!!!!!!!!!!! */
 		addPerson(new Person("teste", "123121"));
 		addPerson(new Person("teste1", "1231211"));
 		addPerson(new Person("teste2", "1231212"));
 		/*###########################*/
 		
 		mThisDeviceId = android.provider.Settings.Secure.ANDROID_ID;
 		mThisDevideName = android.os.Build.USER.concat("-"+android.os.Build.MODEL);
 		
 		/* TODO: Criar NetworkSender. */
 		mNetworkSender = new NetworkSender(9764);
 		mNetworkSender.start();
 		mNetworkListener = new NetworkListener(9226);
 		mNetworkListener.start();
 		
 		/* TODO: REMOVE!!!!!!!!!!!!! */
 		SharedPerson paul = new SharedPerson("Paul", "PaulID", true, false);
 		SharedPerson john = new SharedPerson("John", "JohnID", false, false);
 		SharedByMeItem s1 = new SharedByMeItem("/path/to/nothing");
 		s1.getSharedPersonList().add(paul);
 		s1.getSharedPersonList().add(john);
 		mSharedByMeItems.add(s1);
 		/*###########################*/
 	}
 
 	public static synchronized NetworkManager getInstance() {
 		if (mSingleton == null)
 			mSingleton = new NetworkManager();
 
 		return mSingleton;
 	}
 
 	public void addNewSharedByMe(SharedByMeItem newItem) {
 		mSharedByMeItems.add(newItem);
 	}
 	
 	public ArrayList<SharedByMeItem> getSharedByMeItems() {
 		return mSharedByMeItems;
 	}
 
 	public ArrayList<Person> getPersonList() {
 		return mPersonList;
 	}
 	
 	public void addPerson(Person person){
 		synchronized (mPersonList) {
 			/* If person exists, delete it */
 			deletePerson(person);
 			/* Add the new person, the last person(Everybody) will never be displayed */
			mPersonList.add(0, person);
 		}
 	}
 	
 	/* If person device ID does not exists, does nothing */
 	public void deletePerson(Person person){
 		synchronized (mPersonList) {
 			for(int i=0; i<mPersonList.size(); i++) {
 				if (mPersonList.get(i).getDeviceID().compareTo(person.getDeviceID()) == 0){
			    	  mPersonList.remove(i--);
 			      }
 			}
 		}
 	}
 
 	public void addNewTransfer(FileTransferrer newTransfer) {
 		mCurrentTransfers.add(newTransfer);
 	}
 
 	public void deleteTransfer(FileTransferrer item) {
 		mCurrentTransfers.remove(item);
 	}
 
 	public ArrayList<FileTransferrer> getTransfers() {
 		return mCurrentTransfers;
 	}
 	
 	public String getThisDeviceId(){
 		return mThisDeviceId;
 	}
 	
 	public String getThisDeviceName(){
 		return mThisDevideName;
 	}
 
 	public NetworkSender getNetworkSender() {
 		return mNetworkSender;
 	}
 
 	public NetworkListener getNetworkListener() {
 		return mNetworkListener;
 	}
 }
