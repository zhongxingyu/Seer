 package org.AndroidShareApp.core;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.Random;
 
 public class NetworkManager {
 	
 	private static NetworkManager mSingleton=null;
 	private ArrayList<Person> mPersonList;
 	private ArrayList<FileTransferrer> mCurrentTransfers;
 	private ArrayList<SharedByMeItem> mSharedByMeItems;
 	private String mThisDeviceId;
 	private String mThisDevideName;
 
 	private NetworkManager() {
 		mPersonList = new ArrayList<Person>();
 		mCurrentTransfers = new ArrayList<FileTransferrer>();
 		mSharedByMeItems = new ArrayList<SharedByMeItem>();
 		
 		SharedPerson everybody = new SharedPerson("Everybody",String.valueOf(new Random().nextInt()),false,false);
 		mPersonList.add(everybody);
 		
 		mThisDeviceId = String.valueOf(new Random().nextInt());//TODO: Find the device ID. THIS IS A TEMPORARY FIX
 		mThisDevideName = "jonaias";
 		
 		/* TODO: Criar NetworkSender. */
 		NetworkSender sender = new NetworkSender();
 		sender.start();
 		NetworkListener listener = new NetworkListener(9226);
 		listener.start();
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
 		/* If person exists, delete it */
 		deletePerson(person);
 		/* Add the new person */
 		mPersonList.add(person);
 	}
 	
 	/* If person device ID does not exists, does nothing */
 	public void deletePerson(Person person){
 		Iterator<Person> itr = mPersonList.iterator();
 		/* Search for Person with same device ID */
 	    while (itr.hasNext()) {
 	      Person tempPerson = itr.next();
 	      /* If has the same ID, delete it */
	      if (tempPerson.getDeviceID().compareTo(person.getDeviceID()) == 0){
 	    	  mPersonList.remove(tempPerson);
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
 	
 	
 }
