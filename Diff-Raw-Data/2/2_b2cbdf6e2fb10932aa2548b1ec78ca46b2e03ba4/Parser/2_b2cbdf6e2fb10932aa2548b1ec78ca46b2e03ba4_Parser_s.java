 /*
  * JBoss, Home of Professional Open Source.
  * Copyright 2008, Red Hat Middleware LLC, and individual contributors
  * as indicated by the @author tags. See the copyright.txt file in the
  * distribution for a full listing of individual contributors.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 package  org.jboss.jmx.adaptor.snmp.generator;
 
 import java.io.*;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import javax.management.ObjectName;
 
 
 
 import org.jboss.jmx.adaptor.snmp.generator.metrics.MappedAttribute;
 import org.jboss.jmx.adaptor.snmp.generator.metrics.AttributeMappings;
 import org.jboss.jmx.adaptor.snmp.generator.metrics.ManagedBean;
 import org.jboss.jmx.adaptor.snmp.generator.metrics.Mapping;
 
 import org.jboss.xb.binding.ObjectModelFactory;
 import org.jboss.xb.binding.Unmarshaller;
 import org.jboss.xb.binding.UnmarshallerFactory;
 
 /**
  * This class is used by the MIBGenerator to get the initial list of MappedAttributes out of an input file. 
  * 
  * TODO: allow for the parser to have a list of files and implement an object that can hold all needed data for the generator
  * inside it for each file. This will allow the generator to amalgate attribute definitions from deployed webapps once that 
  * feature has been implemented. 
  * 
  * @author<a href="mailto:tom.hauser@gmail.com"> or <a href="mailto:thauser@redhat.com">Tom Hauser
  *
  */
 
 public class Parser {
 	String inputAttrFile;
 	String inputNotificationFile;
 	ArrayList<MappedAttribute> maList; 
 	ArrayList<Mapping> nmList;//list of notifications that we care about.
 	AttributeMappings mbList; 
 	
 	public Parser(){}
 
 	public Parser(String inputAttrFile, String inputNotificationFile){
 		this.inputAttrFile = inputAttrFile;
 		this.inputNotificationFile = inputNotificationFile;
 		this.maList = new ArrayList<MappedAttribute>(1);
 		this.mbList = new AttributeMappings();
 		this.nmList = new ArrayList<Mapping>(1);
 	}
 	 
 	public String getNotiFile(){
 		return this.inputNotificationFile;
 	}
 	
 	public void setNotiFile(String inputNotiFile){
 		this.inputNotificationFile = inputNotiFile;		
 	}
 	
 	public String getAttrFile(){
 		return this.inputAttrFile;
 	}
 	
 	public void setAttrFile(String inputAttrFile){
 		this.inputAttrFile = inputAttrFile;		
 	}
 	
 	public ArrayList<MappedAttribute> getMaList(){
 		return this.maList;
 	}
 	
 	public void setMaList(ArrayList<MappedAttribute> maList){
 		this.maList = maList;
 	}
 	
 	public AttributeMappings getMbList(){
 		return this.mbList;
 	}
 	
 	public void setMbList(AttributeMappings mbList){
 		this.mbList = mbList;
 	}
 	
 	public ArrayList<Mapping> getNmList(){
 		return this.nmList;
 	}
 	
 	public void setNmList(ArrayList<Mapping> nmList){
 		this.nmList = nmList;
 	}
 
 	/**
 	 * 
 	 * */
 	
 	public void parse(){
 		// TODO: refine the checks here. they are not enough 
 		try {
 			if (this.inputAttrFile != null){
 				parseAttributesXml();
 			}
 			else
 				System.err.println("No attributes file indicated, skipping.");
 			
 			if (this.inputNotificationFile != null){
 				parseNotificationsXml();
 			}
 			else 
 				System.err.println("No notifications file indicated, skipping;");
 		}
 		catch (Exception e){
 			e.printStackTrace();
 			System.exit(1);				
 		}		
 	}
 	
 	
 	private void parseNotificationsXml() throws Exception{
 		ObjectModelFactory omf = new ParserNotificationBindings();
 		ArrayList<Mapping> mappings = null;
 		FileInputStream is = null;
 		try{
 
 			is = new FileInputStream(this.inputNotificationFile);
 
 			
 			Unmarshaller unmarshaller = UnmarshallerFactory.newInstance().newUnmarshaller();
 			
 			mappings = (ArrayList)unmarshaller.unmarshal(is,omf,null);
 		}
 		catch (NoClassDefFoundError e){
 			System.err.println("The notifications file is not formatted correctly: '"+this.inputNotificationFile+"'");
 			System.exit(1);
 		}
 		catch (FileNotFoundException e){
 			System.err.println("Filename given for notifications does not exist: '"+this.inputNotificationFile+"'");
 			System.exit(1);
 		}
 		catch (Exception e){
 			throw e;
 		}
 		finally{
 			if (is!=null){
 				try{
 					is.close();
 				}
 				catch (Exception e){
 					throw e;
 				}
 			}
 		}
 		if (mappings == null){
 			return;
 		}
 		
 		// mappings recieved! add them to our nmList
 		Iterator<Mapping> mIt = mappings.iterator();
 		while (mIt.hasNext()){
 			nmList.add(mIt.next());
 		}
 	}//end parseNotificationXml
 	/**
 	 * All work is done here, using ObjectModelFactory. ParseBindings is used to define how the given xml is parsed, 
 	 * and creates a AttributeMappings object, which is then used to add all ManagedAttributes along with their associated
 	 * data to the member maList. 
 	 *
 	 * @throws Exception
 	 */
 	private void parseAttributesXml() throws Exception{
 		ObjectModelFactory omf = new ParserAttributeBindings();
 		AttributeMappings mappings = null;
 		FileInputStream is = null;
 		try{
 		   
 		   is = new FileInputStream(this.inputAttrFile);
 		   
 		   
 		   // create unmarshaller
 		   Unmarshaller unmarshaller = UnmarshallerFactory.newInstance().newUnmarshaller();
 
 		   // let JBossXB do it's magic using the AttributeMappingsBinding
 		   mappings = (AttributeMappings)unmarshaller.unmarshal(is, omf, null);
 		   setMbList(mappings);
 		}
 		catch (NoClassDefFoundError e){
 			System.err.println("The attributes file is not formatted correctly: '"+this.inputAttrFile+"'");
 			System.exit(1);
 		}
 		catch (FileNotFoundException e){
 			System.err.println("Filename given for attributes.xml does not exist: '"+this.inputAttrFile+"'");
 			System.exit(1);
 		}
 		catch (Exception e){
 		   throw e;
 		}
 		finally{
 		   if (is != null){
 			   try{
 			   is.close();
 			   }
 			   catch (Exception e){
 				   throw e;
 			   }
 		   }
 		}
 		if (mappings == null){
 		   return;         
 		}
 			/**
 			 * We have the MBeans now. Put them into the bindungs.
 			 */
 
 		 Iterator it = mappings.iterator();
 			while (it.hasNext()){
 			   ManagedBean mmb = (ManagedBean)it.next();
 			   String mbeanName = mmb.getName();
 			   ObjectName test = null;
 			   try {
 				   test = new ObjectName(mbeanName);
 			   } catch (Exception e) {}
 			   
 			   if (test == null){
 				   System.err.println("The mbeanName '"+mbeanName+"' could not be converted to an ObjectName. MIB generation failed.");
 				   System.exit(1);
 			   }
 			   
 			   if(!test.isPattern()){
 				   String oidPrefix = mmb.getOidPrefix();
 				   String oidDefName = mmb.getOidDefinition();
 				   List attrs = mmb.getAttributes();
 				   Iterator aIt = attrs.iterator();
 				   while (aIt.hasNext()){
 					  Object check = aIt.next();
 					  
 					  MappedAttribute ma = (MappedAttribute)check;
 	
 					  if (oidPrefix != null)
 						  ma.setOidPrefix(oidPrefix);
 					  else{
 						  ma.setOidPrefix(removeLast(ma.getOid()));
						  ma.setOid(getLast(ma.getOid()));
 					  }
 					  
 					  ma.setOidDefName(oidDefName);
 					  			  
 					  // for the MIB Generator
 					  ma.setMbean(mmb.getName());
 					  if(!maList.contains(ma)){
 						  maList.add(ma); 
 					  }
 					  else{
 						  System.err.println("The attribute '"+ma.getName()+"' is defined using a duplicated OID. MIB generation failed.");
 						  System.exit(1);
 					  }
 			  }
 		   }
 		}
 	}//end parseXml
 	
 	/**
 	 * This method returns the last element of a dotted string representing an OID
 	 */
 	private String getLast(String oid){
 		String [] split = oid.split("\\.");
 		return split[split.length-1];
 	}
 	
 	/**
 	 * This private method removes the last element of a dotted string like
 	 * 1.3.6.1.2.1.1, and returns the rest of the string.
 	 */
 	private String removeLast(String oid){
 		String [] split = oid.split("\\.");
 		String retVal = "";
 		for (int i = 0; i < split.length-1; i++){
 			if (i == split.length-2)
 				retVal+=split[i];
 			else
 				retVal+=split[i]+".";
 		}
 		return retVal;		
 	}
 }// end Parser
