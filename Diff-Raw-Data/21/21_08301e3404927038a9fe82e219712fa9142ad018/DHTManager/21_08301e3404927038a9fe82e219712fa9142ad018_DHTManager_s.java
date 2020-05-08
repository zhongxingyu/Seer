 package com.mymed.controller.core.manager.dht;
 
 import java.io.UnsupportedEncodingException;
 
 import com.mymed.controller.core.exception.IOBackEndException;
 import com.mymed.controller.core.exception.InternalBackEndException;
 import com.mymed.controller.core.manager.AbstractManager;
 import com.mymed.controller.core.manager.storage.StorageManager;
 
 /**
  * 
  * @author lvanni
  *
  */
 public class DHTManager extends AbstractManager implements IDHTManager {
 	/* --------------------------------------------------------- */
	/* Attributes */
	/* --------------------------------------------------------- */
	/** DAO pattern */
	private StorageManager storageManager;
	
	
	/* --------------------------------------------------------- */
 	/* Constructors */
 	/* --------------------------------------------------------- */
 	public DHTManager() throws InternalBackEndException {
 		this(new StorageManager());
 	}
 	
 	public DHTManager(StorageManager storageManager) throws InternalBackEndException {
 		super(storageManager);
 	}
 	
 
 	/* --------------------------------------------------------- */
 	/* Put_Get */
 	/* --------------------------------------------------------- */
 
 	/**
 	 * 
 	 * @param key
 	 * @param value
 	 * @throws IOBackEndException
 	 * @throws InternalBackEndException
 	 */
 	public void put(String key, String value) throws IOBackEndException, InternalBackEndException {
 		try {
 			System.out.println("\nINFO: put value = " + value );
 			storageManager.put(key, value.getBytes("UTF8"));
 		} catch (UnsupportedEncodingException e) {
 			throw new InternalBackEndException(e.getMessage());
 		}
 	}
 	
 	/**
 	 * 
 	 * @param key
 	 * @return
 	 * @throws IOBackEndException
 	 * @throws InternalBackEndException
 	 */
 	public String get(String key) throws IOBackEndException, InternalBackEndException {
 		try {
 			String value = new String(storageManager.get(key), "UTF8");
 			System.out.println("\nINFO: get value = " + value );
 			return value;
 		} catch (UnsupportedEncodingException e) {
 			throw new InternalBackEndException(e.getMessage());
 		}
 	}
 	
 }
