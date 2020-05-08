 package xtremweb.role.examples.akratos;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.security.InvalidKeyException;
 import java.security.KeyPair;
 import java.security.KeyPairGenerator;
 import java.security.NoSuchAlgorithmException;
 import java.security.PrivateKey;
 import java.security.PublicKey;
 import java.security.SecureRandom;
 import java.security.spec.InvalidKeySpecException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Vector;
 import java.util.Hashtable;
 import javax.crypto.BadPaddingException;
 import javax.crypto.IllegalBlockSizeException;
 import javax.crypto.NoSuchPaddingException;
 import javax.xml.bind.JAXBException;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.apache.commons.codec.binary.Base64;
 import org.apache.commons.io.IOUtils;
 import org.xml.sax.SAXException;
 
 import xtremweb.role.examples.akratos.exception.AkratosException;
 import xtremweb.role.examples.akratos.pojo.Contact;
 import xtremweb.role.examples.akratos.pojo.Contacts;
 import xtremweb.role.examples.akratos.pojo.PersonalInfo;
 import xtremweb.role.examples.akratos.pojo.User;
 import xtremweb.role.examples.akratos.util.AkratosUtil;
 
 import xtremweb.api.activedata.ActiveData;
 import xtremweb.api.activedata.ActiveDataCallback;
 import xtremweb.api.activedata.ActiveDataException;
 import xtremweb.api.bitdew.BitDew;
 import xtremweb.api.bitdew.BitDewException;
 import xtremweb.api.transman.TransferManager;
 import xtremweb.api.transman.TransferManagerException;
 import xtremweb.core.com.idl.ComWorld;
 import xtremweb.core.com.idl.ModuleLoaderException;
 import xtremweb.core.iface.Interfacedc;
 import xtremweb.core.iface.Interfacedr;
 import xtremweb.core.iface.Interfaceds;
 import xtremweb.core.iface.Interfacedt;
 import xtremweb.core.log.Logger;
 import xtremweb.core.obj.dc.Data;
 import xtremweb.core.obj.ds.Attribute;
 import xtremweb.serv.dt.OOBTransfer;
 import xtremweb.core.conf.ConfigurationProperties;
 import xtremweb.core.conf.ConfigurationException;
 
 public class Akratos {
 	
 	/**
 	 * Class logger
 	 */
     Logger log = Logger.getLogger("akr.inria.fr.pojo");
     
     /**
      * Bootstrap node
      */
     private String bootstrap;
     
     /**
      * How many replicas of your public info do you want in the system ? 
      */
     private int REPLICA_PUBLICINFO = 2;
     
     /**
      * How many replcias of your ciphered-priviledged info do you want in the system ?
      */
     private int REPLICA_PRIVILEGED_INFO = 2;
     
     /**
      * Private info prefix
      */
     public static final String PRIVATE_INFO_PREFIX = "privateinfo_";
     
     /**
      * Postfix to recognize the aes key from the ciphered plain text
      */
     public static final String AES_CIPHERED_POSTFIX = "aeskey_ciphered";
     
     /**
      * Your public info file path
      */
     public static final String PUBLICINFO_XML = "data/publicinfo.xml";
     
     /**
      * Your personal info file path
      */
     public static final String PERSONAL_INFO_XML = "personalinfo.xml";
     
     /**
      * In this file are stored your friends public keys
      */
     public static final String PUBLICKEYS_XML = "data/publickeys.xml";
     
     /**
      * Your public key name
      */
     public static final String PUBLIC_KEY = "data/akratos.pub";
     
     /**
      * Your private key name
      */
     public static final String PRIVATE_KEY = "akratos.rsa";
     
     /**
      * Ciphered file prefix, each friend feed will have a different number
      */
     public static final String CIPHERED_PREFIX = "ciphered_file_";
     
     /**
      * Your contact XML file; here is stored your contacts
      */
     public static final String CONTACTS_XML = "contacts.xml";
     
     /**
      * BitDew API component
      */
     private BitDew bitdew;
     
     /**
      * ActiveData API component
      */
     private ActiveData activeData;
     
     /**
      * TransferManager API component
      */
     private TransferManager transferManager;
     
     /**
      * Akratos constructor, initialize dr and dt in localhost, dc and ds in bootstrap node (we are using a DHT).
      */
     public Akratos() {
 	
 	try {
 	    bootstrap = ConfigurationProperties.getProperties().getProperty("xtremweb.core.http.bootstrapNode");
 	    
 	    String localhost = InetAddress.getLocalHost().getHostAddress();
 	    Interfacedr dr = (Interfacedr) ComWorld.getComm(localhost, "rmi", 4325, "dr");
 	    Interfaceds ds = (Interfaceds) ComWorld.getComm(bootstrap, "rmi", 4325, "ds");
 	    Interfacedt dt = (Interfacedt)  ComWorld.getComm(localhost, "rmi", 4325, "dt");
 	    Interfacedc dc = (Interfacedc) ComWorld.getComm(bootstrap, "rmi", 4325, "dc");
 	    bitdew = new BitDew(dc, dr,  ds, true);
 	    transferManager = new TransferManager(dt);
 	} catch (ModuleLoaderException e) {
 	    e.printStackTrace();
 	} catch (ConfigurationException e) {
 	    e.printStackTrace();
 	} catch (UnknownHostException e){
 	    e.printStackTrace();
 	}
     }
     
     /**
      * This constructor adds two handlers to each client of BitDew.
      * @param mock
      */
     public Akratos(String mock) {
 	Vector comms;
 	try {
 	    bootstrap = ConfigurationProperties.getProperties().getProperty("xtremweb.core.http.bootstrapNode");
 	    String localhost = InetAddress.getLocalHost().getHostAddress();
 	    comms = ComWorld.getMultipleComms(localhost,"rmi",4325,"dr","dc","dt","ds");
 	    Interfacedr dr = (Interfacedr) ComWorld.getComm(localhost, "rmi", 4325, "dr");
 	    Interfacedt dt = (Interfacedt)  ComWorld.getComm(localhost, "rmi", 4325, "dt");
 	    Interfaceds ds = (Interfaceds) ComWorld.getComm(bootstrap, "rmi", 4325, "ds");
 	    Interfacedc ddc = (Interfacedc) ComWorld.getComm(bootstrap, "rmi", 4325, "dc");
 	    
 	    bitdew = new BitDew(ddc, dr, ds, true);
 	    transferManager = new TransferManager(dt);
 	    System.out.println(" In constructor boot is " + bootstrap);
 	    activeData = new ActiveData(ddc, ds);
 	    activeData.registerActiveDataCallback(new PublicInfoCallback());
 	    activeData.registerActiveDataCallback(new PriviledgedInfoCallback(bootstrap));
 	} catch (ModuleLoaderException e) {
 	    e.printStackTrace();
 	} catch (ConfigurationException e) {
 	    e.printStackTrace();
 	}catch (UnknownHostException e){
 	    e.printStackTrace();
 	}
     }
 
     public static void main(String[] args) {
 	try {
 	    System.setProperty("PROPERTIES_FILE", "properties_peer.json");
 
 	    if (args[0].equals("subscribe")){
 		Akratos akr = new Akratos("");
 		akr.subscribe(args[1]);
 	    }
 	    if (args[0].equals("fill"))
 		Akratos.fillFile(args[1]);
 	    if (args[0].equals("drop")){
 		Akratos akr = new Akratos("");
 		akr.dropNews(args[1]);
 	    }
 	    if (args[0].equals("get")){
 		Akratos akr = new Akratos("");
 		akr.getNews(args[1]);
 	    }
 	} catch (JAXBException e) {
 	    e.printStackTrace();
 	} catch (IOException e) {
 	    e.printStackTrace();
 	} catch (NoSuchAlgorithmException e) {
 	    e.printStackTrace();
 	}
     }
 
     /**
      * This method generates a user public key and an user uid in Akratos
      * @throws JAXBException if an exception occurs while parsing the XML document.
      * @throws IOException if a IO exception appears
      * @throws NoSuchAlgorithmException if an exception related to Checksum algorithm appears
      */
     public static void fillFile(String file_name) throws JAXBException,IOException, NoSuchAlgorithmException {
 	User us = (User) AkratosUtil.unmarshall(User.class, file_name);
 	SecureRandom sr = new SecureRandom();
 	FileOutputStream fos = new FileOutputStream(new File(PRIVATE_KEY));
 	KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
 	kpg.initialize(1024, sr);
 	KeyPair myPair = kpg.generateKeyPair();
 	PublicKey pukey = myPair.getPublic();
 	PrivateKey prkey = myPair.getPrivate();
 	byte[] bencPrivate = prkey.getEncoded();
 	byte[] pukeyblob = pukey.getEncoded();
 	String pukeystr = Base64.encodeBase64String(pukeyblob);
 	String prkeystr = Base64.encodeBase64String(bencPrivate);
 	IOUtils.write(prkeystr, fos);
 	us.setPublickey(pukeystr);
 	AkratosUtil.marshall(User.class, us, file_name);
     }
 
     /**
      * This method subscribes you to the network, this means that a user POJO is 
      * generated from your publicinfo.xml and is published in the DHT.
      * @param fileName the file name where you store your personal public information
      */
     public void subscribe(String fileName) {
 	File f;
 	if (fileName == null || fileName.equals(""))
 	    f = new File(PUBLICINFO_XML);
 	else
 	    f = new File(fileName);
 
 	log.setLevel("debug");
 	File privatef = new File(Akratos.PERSONAL_INFO_XML);
 	User us;
 	try {
 	    String localhost = InetAddress.getLocalHost().getHostAddress();
 	    Data data_public_info = bitdew.createData(f);
 	    Data data_private_info = bitdew.createData(privatef);
 	    System.out.println("Bootstrap node is " + bootstrap);
 	    data_public_info.setname("public_info");
 	    us = (User) AkratosUtil.unmarshall(User.class, fileName);
 
 	    us.setUid(data_public_info.getuid());
 	    us.setPrivateuid(data_private_info.getuid());
 	    AkratosUtil.marshall(User.class, us, fileName);
 	    OOBTransfer oobt = bitdew.put(f, data_public_info, "http");
 	    transferManager.start();
 	    transferManager.registerTransfer(oobt);
 
 	    transferManager.waitFor(data_public_info);
 	    transferManager.stop();
 
 	    bitdew.ddcPublish(us.getUid(), us);
 	    bitdew.ddcPublish(us.getPublicname(), us);
 	    bitdew.ddcPublish(us.getTag(), us);
 	    log.info("information published ");
 
 	    Attribute attr_public_info = activeData.createAttribute("{name: 'publicinfo', ft: true, replicat: "
 		    + REPLICA_PUBLICINFO + " }");
 	    activeData.schedule(data_public_info, attr_public_info);
 	    activeData.start();
 	} catch (JAXBException e) {
 	    e.printStackTrace();
 	} catch (FileNotFoundException e) {
 	    e.printStackTrace();
 	} catch (BitDewException e) {
 	    e.printStackTrace();
 	} catch (TransferManagerException e) {
 	    e.printStackTrace();
 	} catch (ActiveDataException e) {
 	    e.printStackTrace();
 	} catch (UnknownHostException e) {
 	    // TODO Auto-generated catch block
 	    e.printStackTrace();
 	}
 
     }
     
     /**
      * This method find you a contact according either its tag name (like in twitter), or
      * by its complete name.
      * @param tag
      * @param public_name
      * @return a list of people registered in the Network whose tag or complete name is equal to the 
      * one that you inserted.
      */
     public List findFriend(String tag, String public_name) {
 	List ret = null;
 	try {
 	    String localhost = InetAddress.getLocalHost().getHostAddress();
 	    String searchBy = null;
 	    if (tag != null && !tag.equals(""))
 		searchBy = tag;
 
 	    if (public_name != null && !public_name.equals(""))
 		searchBy = public_name;
 	    ret = bitdew.ddcSearch(searchBy);
 	} catch (BitDewException e) {
 	    // TODO Auto-generated catch block
 	    e.printStackTrace();
 	} catch (UnknownHostException e) {
 	    // TODO Auto-generated catch block
 	    e.printStackTrace();
 	}
 	return ret;
     }
     
     /**
      * Add a contact to your contacts.xml file
      * @param uid contact uid
      * @param privateuid contact private uid, 
      * @param publicname contact public complete name
      * @param tag contact tag name (like in twitter)
      * @param pu_key contact public key in base64
      * @param city contact city
      * @param country contact residence country
      * @param profession contact profession
      */
     public void addFriend(String uid, String privateuid, String publicname, String tag, String pu_key, String city,
 	    String country, String profession) {
 	try {
 	    Contact c = new Contact(uid, privateuid, pu_key);
 	    Contacts pks;
 	    pks = (Contacts) AkratosUtil.unmarshall(Contacts.class, PUBLICKEYS_XML);
 	    pks.addContact(c);
 	    AkratosUtil.marshall(Contacts.class, pks, PUBLICKEYS_XML);
 	} catch (FileNotFoundException e) {
 	    e.printStackTrace();
 	} catch (JAXBException e) {
 	    e.printStackTrace();
 	}
 
     }
     
     /**
      * This method send the user newsletter as an AES-ciphered XML file in the network. To achieve this,
      * a public key list is maintained in contacts.xml file, the current user ciphers its XML file using a generated AES key that is cipehred
      * with each of the public keys in the file system.
      * @param public_file_name The file where you have your public personal information.
      */
     public void dropNews(String public_file_name) {
 	try {
 	    Contacts contacts;
 	    User user = (User) AkratosUtil.unmarshall(User.class, public_file_name);
 	    String thisuserid = user.getUid();
 	    contacts = (Contacts) AkratosUtil.unmarshall(Contacts.class, CONTACTS_XML);
 	    FileInputStream file = new FileInputStream(new File(PERSONAL_INFO_XML));
 	    File personalinfo = new File(PERSONAL_INFO_XML);
 	    Data data_personal_info = bitdew.createData(personalinfo);
 	    String localhost = "127.0.0.1";
 	    transferManager.start();
 	    
 	    Interfacedr dr = (Interfacedr) ComWorld.getComm(localhost, "rmi", 4325, "dr");
 	    Interfaceds ds = (Interfaceds) ComWorld.getComm(localhost, "rmi", 4325, "ds");
 	    Interfacedc dc = (Interfacedc) ComWorld.getComm(localhost, "rmi", 4325, "dc");
 		
 	    BitDew bitdewlocal = new BitDew(dc, dr,  ds);
 	    
 	    for (int i = 0; i < contacts.getSize(); i++) {
 		Contact c = contacts.getContact(i);
 		// The personal info ciphered
 		String file_complete_name = thisuserid + "_" + c.getPrivateuid();
 		File personalinfociphered = new File(file_complete_name);
 		AkratosUtil.cipherAndWriteFile(file_complete_name, c.getPublickey(), file);
 		
 		
 		
 		Data data_personal_info_ciphered = bitdewlocal.createData(personalinfociphered);
 		data_personal_info_ciphered.setname(c.getPrivateuid());
 		bitdew.ddcPublish(c.getPrivateuid(), data_personal_info_ciphered);
 		bitdew.ddcPublish(data_personal_info_ciphered.getchecksum(), InetAddress.getLocalHost()
 			.getHostAddress());
 		System.out.println(" dropped checksum " + data_personal_info_ciphered.getchecksum());
 
 		// The AES key ciphered
 		File aeskeyciphered = new File(file_complete_name + Akratos.AES_CIPHERED_POSTFIX);
 		Data data_aeskeyciphered = bitdewlocal.createData(aeskeyciphered);
 		data_aeskeyciphered.setname(c.getPrivateuid() + Akratos.AES_CIPHERED_POSTFIX);
 		
 		bitdew.ddcPublish(data_aeskeyciphered.getchecksum(), InetAddress.getLocalHost().getHostAddress());
 
 		System.out.println(" dropped checksum " + data_aeskeyciphered.getchecksum());
 		// Put of the personal info file
 		OOBTransfer oob = bitdewlocal.put(personalinfociphered, data_personal_info_ciphered, "http");
 		transferManager.registerTransfer(oob);
 		transferManager.waitFor(data_personal_info_ciphered);
 
 		// Put of the AES key RSA-ciphered
 		oob = bitdewlocal.put(aeskeyciphered, data_aeskeyciphered, "http");
 		transferManager.registerTransfer(oob);
 		transferManager.waitFor(data_aeskeyciphered);
 
 		// scheduling
 		Attribute attr_personal_info = activeData.createAttribute("{ft: true, replicat: "
 			+ REPLICA_PRIVILEGED_INFO + " }");
 		Attribute attr_aes_key = activeData.createAttribute("{ft: true , affinity: '"
 			+ data_personal_info_ciphered.getuid() + "' }");
 		activeData.schedule(data_personal_info_ciphered, attr_personal_info);
 		activeData.schedule(data_aeskeyciphered, attr_aes_key);
 		bitdew.ddcPublish(c.getPrivateuid() + Akratos.AES_CIPHERED_POSTFIX, data_aeskeyciphered);
 	    }
 	    transferManager.stop();
 	} catch (FileNotFoundException e) {
 	    e.printStackTrace();
 	} catch (JAXBException e) {
 	    e.printStackTrace();
 	} catch (NoSuchAlgorithmException e) {
 	    e.printStackTrace();
 	} catch (InvalidKeySpecException e) {
 	    e.printStackTrace();
 	} catch (NoSuchPaddingException e) {
 	    e.printStackTrace();
 	} catch (InvalidKeyException e) {
 	    e.printStackTrace();
 	} catch (IllegalBlockSizeException e) {
 	    e.printStackTrace();
 	} catch (BadPaddingException e) {
 	    e.printStackTrace();
 	} catch (IOException e) {
 	    e.printStackTrace();
 	} catch (BitDewException e) {
 	    e.printStackTrace();
 	} catch (ActiveDataException e) {
 	    e.printStackTrace();
 	} catch (TransferManagerException e) {
 	    e.printStackTrace();
 	} catch (ModuleLoaderException e){
 	    e.printStackTrace();
 	}
     }
     
     /**
      * This method is called whenever you want to get news from your friends. To achieve this,
      * you must insert your private uid in the system, to get the files that has been directed
      * to you by your friends. Then by inserting your privateuid_<AES_CIPHERED_POSTFIX> you can get
      * the AES key associated to this ciphered file. You decrypt the AES key with your private key.
      * @param file_name
      */
     public void getNews(String file_name) {
 	User u;
 	PersonalInfo personal;
 	String plaintext = "";
 	Hashtable ht = new Hashtable();
 	String dataaff;
 	String ip="";
 	try {
 
 	    Contacts contacts = (Contacts) AkratosUtil.unmarshall(Contacts.class, "contacts.xml");
 	    u = (User) AkratosUtil.unmarshall(User.class, file_name);
 	    // datas contains feed and replicas of your friends, need depurate
 	    // to take only unique values
 	    List<Data> plaindatas = bitdew.ddcSearch(u.getPrivateuid());
 	    List<Data> aeskeys = bitdew.ddcSearch(u.getPrivateuid() + Akratos.AES_CIPHERED_POSTFIX);
 	    System.out.println(" Size of aes keys" + aeskeys.size());
 	    System.out.println(" Size of response : " + plaindatas.size());
 	    List<Data> uniqdatas = AkratosUtil.getUniques(plaindatas);
 	    List<Data> uniqaes = AkratosUtil.getUniques(aeskeys);
 	    System.out.println("Uniq aes " + uniqaes.size() + " Uniq datas " + uniqdatas.size());
 	    Hashtable<String, Data> hashdata = AkratosUtil.getDataHashTable(uniqdatas);
 	    Hashtable<String, Data> hashaes = AkratosUtil.getDataHashTable(uniqaes);
 	    // for each data name in the hash
 	    transferManager.start();
 	    for (int i = 0; i < uniqaes.size(); i++) {
 		System.out.println("entro al ciclo");
 		Data pre_aes_key = (Data) uniqaes.get(i);
 		System.out.println("data before is " + pre_aes_key + " data attrid " + pre_aes_key.getattruid());
 		Attribute attr = bitdew.getAttributeByUid(pre_aes_key.getattruid());
 		System.out.println("Attribute is " + attr);
 		dataaff = attr.getaffinity();
 
 		Data pre_ciphered_info = hashdata.get(dataaff);
 
 		String md5info = pre_ciphered_info.getchecksum();
 		String md5aes = pre_aes_key.getchecksum();
 		List<String> ips = bitdew.ddcSearch(md5info);
 		boolean done = false;
 		for (int ind_ips = 0 ; ind_ips < ips.size() && !done; ind_ips ++ ){
 		    
 		    try{
 		    ip = ips.get(0);
 		    System.out.println("ip to connect with " + ip);
		    Interfacedc dc = (Interfacedc) ComWorld.getComm(ip, "rmi", 4325, "dc");
		    Interfacedr dr = (Interfacedr) ComWorld.getComm(ip, "rmi", 4325, "dr");
		    Interfaceds ds = (Interfaceds) ComWorld.getComm(ip, "rmi", 4325, "ds");
 
 		    bitdew = new BitDew(dc, dr, ds);
 
 		    Data ciphered_info = bitdew.getDataFromMd5(md5info);
 		    Data aes_key = bitdew.getDataFromMd5(md5aes);
 		    System.out.println("Data to download " + ciphered_info.getuid());
 		    System.out.println("AES Key to download " + aes_key.getuid());
 		    File ciphered_file = new File("feed" + i);
 
 		    // Get ciphered file
 		    OOBTransfer oob = bitdew.get(ciphered_info, ciphered_file);
 		    transferManager.registerTransfer(oob);
 		    transferManager.waitFor(ciphered_info);
 
 		    // Get AES Key
 		    File aes = new File("feed"+i+Akratos.AES_CIPHERED_POSTFIX);
 		    oob = bitdew.get(aes_key, aes);
 		    transferManager.registerTransfer(oob);
 		    transferManager.waitFor(aes_key);
 		    done = true;
 		    }catch (ModuleLoaderException e) {
 			    e.printStackTrace();
 			} 
 		    catch(Exception e ){
 			
 			System.out.println("The host " + ip + "seems to be down, trying with next one");
 			e.printStackTrace();
 		    }
 		}
 		// At this point we have both files, we proceed to decryption
 
 		plaintext = AkratosUtil.uncipher("feed" + i );
 		System.out.println(" The plain Text is " + plaintext);
 		//String getFriendUid = AkratosUtil.getFriendUid(plaintext);
 		//System.out.println("Friend uid is " + getFriendUid);
 		BufferedWriter bw = new BufferedWriter(new FileWriter(new File("plaintext"+i+".xml")));
 		bw.write(plaintext);
 		bw.close();
 	    }
 
 	} catch (FileNotFoundException e) {
 	    e.printStackTrace();
 	} catch (BitDewException e) {
 	    e.printStackTrace();
 	} catch (JAXBException e) {
 	    e.printStackTrace();
 	} catch (InvalidKeyException e) {
 	    e.printStackTrace();
 	} catch (NoSuchAlgorithmException e) {
 	    e.printStackTrace();
 	} catch (NoSuchPaddingException e) {
 	    e.printStackTrace();
 	} catch (InvalidKeySpecException e) {
 	    e.printStackTrace();
 	} catch (IllegalBlockSizeException e) {
 	    e.printStackTrace();
 	} catch (BadPaddingException e) {
 	    e.printStackTrace();
 	} catch (IOException e) {
 	    e.printStackTrace();
 	}
     }
     
     /**
      * Callback to be called when public information is scheduled on a given machine.
      * @author jsaray
      *
      */
     public class PublicInfoCallback implements ActiveDataCallback {
     
     	/**
     	 * This method retrieves the data that has been inserted on the DHT and replicates it in the DHT.
     	 */
     	public void onDataScheduled(Data data, Attribute attribute) {
     	    if (data.getname().equals("public_info")) {
     		System.out.println("on scheduled called  on public! " + data.getuid() + " data name : "+ data.getname());
     		String uid = data.getuid();
     		List<User> users;
     		try {
 		    users = bitdew.ddcSearch(uid);
 		    User user = users.get(0);
 		    AkratosUtil.marshall(User.class, user, uid);
 		    bitdew.ddcPublish(user.getUid(), user);
 		    bitdew.ddcPublish(user.getPublicname(), user);
 		    bitdew.ddcPublish(user.getTag(), user);
 		} catch (BitDewException e) {
 		    e.printStackTrace();
 		} catch (JAXBException e) {
 		    e.printStackTrace();
 		} catch (ClassCastException e) {
 		    System.out.println("A controlled exception has occured, PublicInfoCallback was executed where another callback was needed (Private)");
 		    e.printStackTrace();
 		}
 	     }
     	}
 
 	/**
 	 * For now, this method is empty
 	 */
 	public void onDataDeleted(Data data, Attribute attr) {
 	}
 
     }
     
     /**
      *	This callback is used to store a copy of ciphered XML file representing the personal information
      *	of a peer.
      *
      *
      * @author jsaray
      */
     public class PriviledgedInfoCallback implements ActiveDataCallback {
     	
     	/**
     	 * Bootstrap node
     	 */
     	private String bootstrap;
     	
     	/**
     	 * Localhost address
     	 */
     	private String localhost;
     	
     	/**
     	 * BitDew distributed data catalog.
     	 */
     	private BitDew ddcbitdew;
     	
     	/**
     	 * Bitdew local catalog
     	 */
 	private BitDew bitdewlocal;
 		
 	/**
 	* Initialize fields in constructor
 	* @param bootstrap the bootstrap node running dc and ds.
 	*/
 	public PriviledgedInfoCallback(String bootstrap) {
 	    this.bootstrap = bootstrap;
 	    try{
 		String localhost = InetAddress.getLocalHost().getHostAddress();
 		Interfacedt dt = (Interfacedt )ComWorld.getComm(localhost, "rmi", 4325, "dt");
 		transferManager = new TransferManager(dt);
 	    
 		Interfacedr dr = (Interfacedr) ComWorld.getComm(localhost, "rmi", 4325, "dr");
 		Interfaceds ds = (Interfaceds) ComWorld.getComm(localhost, "rmi", 4325, "ds");
 		Interfacedc ddc = (Interfacedc) ComWorld.getComm(bootstrap, "rmi", 4325, "dc");
 	    
 		ddcbitdew = new BitDew(ddc,dr,ds,true);
 	    
 	    
 		Interfacedc dc = (Interfacedc) ComWorld.getComm(localhost, "rmi", 4325, "dc");
 		bitdewlocal = new BitDew(dc, dr,  ds);
 	    
 	    
 	    }catch(Exception e ){
 		e.printStackTrace();
 	    }
 	}
 	
 	/**
 	 * Called when a private data is scheduled to this machine, the handler replicates the data.
 	 */
 	public void onDataScheduled(Data data, Attribute attribute) {
 	    if (!data.getname().equals("public_info")) {
 		System.out.println("on scheduled called on private! " + data.getuid() + " data name : "+ data.getname() + " data checksum :" + data.getchecksum());
 		try {
 		    List<String> ips;
 		    ips = ddcbitdew.ddcSearch(data.getchecksum());
 
 		    ddcbitdew.ddcPublish(data.getname(), data);
 		    ddcbitdew.ddcPublish(data.getchecksum(), InetAddress.getLocalHost().getHostAddress());
 
 		    System.out.println(" Machine to contact " + ips.get(0) + " checksum to search "+ data.getchecksum());
 		    Interfacedr dr = (Interfacedr) ComWorld.getComm(ips.get(0), "rmi", 4325, "dr");
 		    Interfaceds ds = (Interfaceds) ComWorld.getComm(ips.get(0), "rmi", 4325, "ds");
 		    Interfacedc dc= (Interfacedc) ComWorld.getComm(ips.get(0), "rmi", 4325, "dc");
 
 		    BitDew bitdewrepo = new BitDew(dc, dr, ds);
 
 		    Data final_data = bitdewrepo.getDataFromMd5(data.getchecksum());
 		    File file_to_get = new File(final_data.getuid());
 		    // Get physically the file
 		    OOBTransfer oob = bitdewrepo.get(final_data, file_to_get);
 		    transferManager.start();
 		    transferManager.registerTransfer(oob);
 		    transferManager.waitFor(final_data);
 
 		    // Put locally the file to make it visible to bitdew
 		    File to_put_internally = new File(data.getuid());
 		    oob = bitdewlocal.put(to_put_internally, final_data, "http");
 		    transferManager.registerTransfer(oob);
 		    transferManager.waitFor(data);
 		    transferManager.stop();
 		} catch (UnknownHostException e) {
 		    e.printStackTrace();
 		} catch (BitDewException e) {
 		    e.printStackTrace();
 		} catch (TransferManagerException e) {
 		    e.printStackTrace();
 		} catch (ModuleLoaderException e) {
 		    e.printStackTrace();
 		} catch (ClassCastException e) {
 		    System.out.println(" A controlled exception has occured, private callback was call but it was not its turn");
 		    e.printStackTrace();
 		}
 	    }
 	}
 
 	/**
 	 * For now this method is not implemented
 	 */
 	public void onDataDeleted(Data data, Attribute attr) {
 	}
     }
 }
