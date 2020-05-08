 package org.AndroidShareApp.core;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.PrintWriter;
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.Random;
 
 public class NetworkManager {
 
 	private static NetworkManager mSingleton = null;
 	private ArrayList<Person> mPersonList;
 	private ArrayList<SharedByMeItem> mSharedByMeItems;
 	private String mThisDeviceId;
 	private String mThisDeviceName;
 	private InetAddress mHostAddress;
 	private InetAddress mBroadcastAddress;
 	private NetworkSender mNetworkSender;
 	private NetworkListener mNetworkListener;
 	private FileServer mFileServer;
 	private ArrayList<FileTransferrer> mCurrentTransfers;
 
 	private NetworkManager() {
 		mPersonList = new ArrayList<Person>();
 		mSharedByMeItems = new ArrayList<SharedByMeItem>();
 		mCurrentTransfers = new ArrayList<FileTransferrer>();
 
 		SharedPerson everybody = new SharedPerson("Everybody",
 				String.valueOf(new Random().nextInt()), null, false, false);
 		addPerson(everybody);
 
 		/* TODO: REMOVE!!!!!!!!!!!!! */
 		/*addPerson(new Person("teste", "123121", null));
 		addPerson(new Person("teste1", "1231211", null));
 		Person teste2 = new Person("teste2", "1231212", null); 
 		try {
 			teste2.setIP(InetAddress.getByName("10.0.2.2"));
 		} catch (UnknownHostException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 		SharedWithMeItem sharedWithMeItem1 = new SharedWithMeItem("/Documents/", true, false,0);
 		SharedWithMeItem sharedWithMeItem2 = new SharedWithMeItem("/cool music.mp3", true, true,0);
 		SharedWithMeItem sharedWithMeItem3 = new SharedWithMeItem("/picture.png", true, true,0);
 		SharedWithMeItem sharedWithMeItem4 = new SharedWithMeItem("/Movies/", true, true,0);
 		SharedWithMeItem sharedWithMeItem5 = new SharedWithMeItem("/Movies/alternative movie.mpg", true, true,0);
 		SharedWithMeItem sharedWithMeItem6 = new SharedWithMeItem("/Movies/unknown.mpg", true, true,0);
 		SharedWithMeItem sharedWithMeItem7 = new SharedWithMeItem("/Movies/Bad movie/unknown.mpg", true, true,0);
 		teste2.addSharedWithMeItem(sharedWithMeItem1);
 		teste2.addSharedWithMeItem(sharedWithMeItem2);
 		teste2.addSharedWithMeItem(sharedWithMeItem3);
 		teste2.addSharedWithMeItem(sharedWithMeItem4);
 		teste2.addSharedWithMeItem(sharedWithMeItem5);
 		teste2.addSharedWithMeItem(sharedWithMeItem6);
 		teste2.addSharedWithMeItem(sharedWithMeItem7);
 		addPerson(teste2);*/
 		/* ########################### */
 
 		mNetworkSender = new NetworkSender(NetworkProtocol.UDP_PORT);
 		
 		mNetworkListener = new NetworkListener(NetworkProtocol.UDP_PORT);
 		
 		mFileServer = new FileServer(NetworkProtocol.FILE_PORT);
 		
 
 		/* TODO: REMOVE!!!!!!!!!!!!! */
 
 		InetAddress address = null;
 		try {
 			address = InetAddress.getByName("10.0.2.2");
 		} catch (UnknownHostException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		/*SharedPerson paul = new SharedPerson("Paul", "PaulID", address, true,
 				false);
 		SharedPerson john = new SharedPerson("John", "JohnID", address, false,
 				false);*/
 		
		SharedByMeItem s1 = new SharedByMeItem("/mnt/sdcard/Example.txt");
 		/*s1.getSharedPersonList().add(paul);
 		s1.getSharedPersonList().add(john);*/
		mSharedByMeItems.add(s1);
 		//createFile();
 		/* ########################### */
 	}
 	
 	private static void createFile () {
 		File f = new File ("/mnt/sdcard/teste.txt");
 		FileOutputStream fos = null;
 		try {
 			fos = new FileOutputStream(f);
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		PrintWriter out = new PrintWriter(fos);
 		
 		out.print("This is a sample file.");
 		out.close();
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
 
 	public void addPerson(Person person) {
 		synchronized (mPersonList) {
 			/* If person exists, delete it */
 			deletePerson(person);
 			/*
 			 * Add the new person, the last person(Everybody) will never be
 			 * displayed
 			 */
 			mPersonList.add(0, person);
 		}
 	}
 
 	/* If person device ID does not exists, does nothing */
 	public void deletePerson(Person person) {
 		synchronized (mPersonList) {
 			Iterator<Person> itr = mPersonList.iterator();
 			while (itr.hasNext()) {
 				Person tempPerson = itr.next();
 				if (tempPerson.getDeviceID().compareTo(person.getDeviceID()) == 0) {
 					itr.remove();
 				}
 			}
 		}
 	}
 
 	public Person getPersonByDeviceID(String deviceID) {
 		synchronized (mPersonList) {
 			Iterator<Person> itr = mPersonList.iterator();
 			while (itr.hasNext()) {
 				Person temp = itr.next();
 				if (temp.getDeviceID().compareTo(deviceID) == 0)
 					return temp;
 			}
 		}
 		return null;
 	}
 
 	public void addTransfer(FileTransferrer transfer) {
 		mCurrentTransfers.add(transfer);
 	}
 
 	public void deleteTransfer(FileTransferrer transfer) {
 		mCurrentTransfers.remove(transfer);
 	}
 	
 	public void deleteCompletedTransfers() {
 		synchronized (mCurrentTransfers) {
 			Iterator<FileTransferrer> itr = mCurrentTransfers.iterator();
 			while (itr.hasNext()) {
 				FileTransferrer currClient = itr.next();
 				if (!currClient.isAlive()) {
 					itr.remove();
 				}
 			}
 		}
 	}
 
 	public ArrayList<FileTransferrer> getTransfers() {
 		return mCurrentTransfers;
 	}
 
 	public void setThisDeviceId(String deviceId) {
 		mThisDeviceId = deviceId;
 	}
 	
 	public String getThisDeviceId() {
 		return mThisDeviceId;
 	}
 	
 	public void setThisDeviceName(String deviceName) {
 		mThisDeviceName = deviceName;
 	}
 
 	public String getThisDeviceName() {
 		return mThisDeviceName;
 	}
 
 	public NetworkSender getNetworkSender() {
 		return mNetworkSender;
 	}
 
 	public NetworkListener getNetworkListener() {
 		return mNetworkListener;
 	}
 
 	public FileServer getFileServer() {
 		return mFileServer;
 	}
 
 	public InetAddress getHostAddress() {
 		return mHostAddress;
 	}
 
 	public void setHostAddress(InetAddress hostAddress) {
 		mHostAddress = hostAddress;
 	}
 
 	public InetAddress getBroadcastAddress() {
 		return mBroadcastAddress;
 	}
 
 	public void setBroadcastAddress(InetAddress broadcastAddress) {
 		mBroadcastAddress = broadcastAddress;
 	}
 	
 	/** Return fullPath using a relativePath
 	 *  if there is no item with relativePath, returns null **/
 	public String getSharedByMeItemFullPath(String relativePath){
 		Iterator<SharedByMeItem> itr = mSharedByMeItems.iterator();
 		while (itr.hasNext()) {
 			SharedByMeItem sharedByMeItem = itr.next();
 			if (sharedByMeItem.getSharedPath().compareTo(relativePath) == 0) {
 				return sharedByMeItem.getFullPath();
 			}
 		}
 		return null;
 	}
 	
 	/** Start servers **/
 	public void startServers(){
 		if (!mNetworkSender.isAlive()){
 			mNetworkSender.start();
 		}
 		if (!mNetworkListener.isAlive()){
 			mNetworkListener.start();
 		}
 		if (!mFileServer.isAlive()){
 			mFileServer.start();
 		}
 	}
 }
