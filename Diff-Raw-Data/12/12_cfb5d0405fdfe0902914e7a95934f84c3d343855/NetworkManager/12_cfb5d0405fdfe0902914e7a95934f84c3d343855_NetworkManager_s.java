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
 
 	private NetworkManager() {
 		mPersonList = new ArrayList<Person>();
 		mCurrentTransfers = new ArrayList<FileTransferrer>();
 		mSharedByMeItems = new ArrayList<SharedByMeItem>();
 		
 		SharedPerson everybody = new SharedPerson("Everybody",String.valueOf(new Random().nextInt()),false,false);
 		mPersonList.add(everybody);
 		
 		mThisDeviceId = String.valueOf(new Random().nextInt());//TODO: Find the device ID. THIS IS A TEMPORARY FIX
 		
 		
 		/* TODO: Criar NetworkSender. */
		NetworkListener listener = new NetworkListener(32767);
 		listener.start();
 		
 		/*############ TODO: REMOVE! JUST FOR DEBUGGING. ############## */
 		SharedPerson james = new SharedPerson("James Stewart",String.valueOf(new Random().nextInt()), true, false);
 		SharedPerson john = new SharedPerson("John Noble", String.valueOf(new Random().nextInt()), false, true);
 		SharedPerson paul = new SharedPerson("Paul Tomas", String.valueOf(new Random().nextInt()), true, true);
 		
 		SharedByMeItem item1 = new SharedByMeItem("Music/");
 		item1.managePerson(james);
 		item1.managePerson(john);
 		item1.managePerson(paul);
 		
 		/*------------------------------------------------------------ */
 		
 		SharedPerson ana = new SharedPerson("Ana Simpson",String.valueOf(new Random().nextInt()), false, true);
 		SharedPerson maria = new SharedPerson("Maria Teresa",String.valueOf(new Random().nextInt()), true, true);
 		SharedPerson steve = new SharedPerson("Steve Timberlake",String.valueOf(new Random().nextInt()), true, false);
 		
 		SharedByMeItem item2 = new SharedByMeItem("file.pdf");
 		item2.managePerson(ana);
 		item2.managePerson(maria);
 		item2.managePerson(steve);
 		
 		mSharedByMeItems.add(item1);
 		mSharedByMeItems.add(item2);
 		
 		/*------------------------------------------------------------ */
 		mPersonList.add(james);
 		mPersonList.add(john);
 		mPersonList.add(paul);
 		mPersonList.add(ana);
 		mPersonList.add(maria);
 		mPersonList.add(steve);
 		
 		/* ########################################################### */
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
 	      if (tempPerson.getDeviceID() == person.getDeviceID()){
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
 	
 	
 }
