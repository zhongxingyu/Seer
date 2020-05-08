 package org.alljoyn.triumph.demo.ifaces;
 
 import org.alljoyn.bus.BusException;
 import org.alljoyn.bus.annotation.BusProperty;
 
 public interface PropertiesInterface {
 
 	
 	@BusProperty
     public int getMagicInt() throws BusException;
     
 	@BusProperty
     public void setMagicInt(int i) throws BusException;
 	
 	@BusProperty
 	public String getOS() throws BusException;
 	
 	@BusProperty
 	public String getOSArchitecture() throws BusException;
 	
 	@BusProperty
 	public String[] getFunnyWords() throws BusException;
 	
 	@BusProperty
 	public void setFunnyWords(String[] words) throws BusException;
 }
