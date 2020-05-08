 /*
  * Last modification information:
 * $Revision: 1.3 $
 * $Date: 2005-02-09 06:15:09 $
  * $Author: scytacki $
  *
  * Licence Information
  * Copyright 2004 The Concord Consortium 
 */
 package org.concord.otrunk.datamodel;
 
 import org.concord.framework.otrunk.OTID;
 import org.doomdark.uuid.EthernetAddress;
 import org.doomdark.uuid.NativeInterfaces;
 import org.doomdark.uuid.UUID;
 import org.doomdark.uuid.UUIDGenerator;
 ;
 
 
 /**
  * OTUUID
  * Class name and description
  *
  * Date created: Dec 5, 2004
  *
  * @author scott<p>
  *
  */
 public class OTUUID extends UUID
 	implements OTID 
 {
 	private static boolean noEthernetInterfaces;
 	private static EthernetAddress hwAddress = null;
 	
 	static {
 		// tell the UUID code to use the standard paths 
 		// to find native library
 		NativeInterfaces.setUseStdLibDir(true);
 	}
 	
 	protected OTUUID(String id)
 	{
 		super(id);
 	}
 	
 	public static EthernetAddress getHWAddress()	
 	{
 		if(noEthernetInterfaces) {
 			return null;
 		}
 		
 		if(hwAddress != null) {
 			return hwAddress;
 		}
 		
		try {
			hwAddress = NativeInterfaces.getPrimaryInterface();
		} catch (Throwable t) {
			// can't get the hardware address for some reason
			System.err.println("Unable to find hardware address for unique ids because: \r\t" + t.getMessage());
			noEthernetInterfaces = true;
			return null;
		}
 
     	if(hwAddress == null) {
     		System.err.println("primary interface is null");
     		EthernetAddress [] addressArray = NativeInterfaces.getAllInterfaces();
     		for(int i=0; i<addressArray.length; i++) {
     			if(addressArray[i] != null) {
     				hwAddress = addressArray[i];
     			}  			
     		}
     		if(hwAddress == null) {
     			System.err.println("Can't find a native interface");
     		}
     	}
     	
 		if(hwAddress == null) {
 			noEthernetInterfaces = true;
 		}
 		
 		return hwAddress;
 	}
 	
 	public static OTID createOTUUID()
 	{
 		EthernetAddress hwAddress = getHWAddress();
 				
 		UUIDGenerator generator = UUIDGenerator.getInstance();
     	// FIXME: there has to be a better way to do this without
     	// making a throw away object.
     	UUID id;   	
     	if(hwAddress != null) {
     		id = generator.generateTimeBasedUUID(hwAddress);
     	} else {
     		id = generator.generateTimeBasedUUID();
     	}
     	return new OTUUID(id.toString());
 	}
 }
