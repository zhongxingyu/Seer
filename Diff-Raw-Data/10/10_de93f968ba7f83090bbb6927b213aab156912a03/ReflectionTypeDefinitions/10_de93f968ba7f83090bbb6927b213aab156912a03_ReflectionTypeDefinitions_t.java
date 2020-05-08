 /*
  *  Copyright (C) 2004  The Concord Consortium, Inc.,
  *  10 Concord Crossing, Concord, MA 01742
  *
  *  Web Site: http://www.concord.org
  *  Email: info@concord.org
  *
  *  This library is free software; you can redistribute it and/or
  *  modify it under the terms of the GNU Lesser General Public
  *  License as published by the Free Software Foundation; either
  *  version 2.1 of the License, or (at your option) any later version.
  *
  *  This library is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  *  Lesser General Public License for more details.
  *
  *  You should have received a copy of the GNU Lesser General Public
  *  License along with this library; if not, write to the Free Software
  *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  *
  * END LICENSE */
 
 /*
  * Last modification information:
  * $Revision: 1.26 $
  * $Date: 2007-10-03 21:44:16 $
  * $Author: scytacki $
  *
  * Licence Information
  * Copyright 2004 The Concord Consortium 
 */
 package org.concord.otrunk.xml;
 
 import java.util.List;
 import java.util.Vector;
 
 import org.concord.framework.otrunk.otcore.OTClass;
 import org.concord.otrunk.OTrunkImpl;
 import org.concord.otrunk.otcore.impl.OTClassImpl;
 import org.concord.otrunk.otcore.impl.ReflectiveOTClassFactory;
 
 
 /**
  * XMLTypeDefinitions
  * Class name and description
  *
  * Date created: Nov 17, 2004
  *
  * @author scott<p>
  *
  */
 public class ReflectionTypeDefinitions
 {
 	public static void registerTypes(List importedOTObjectClasses, TypeService typeService,
 			XMLDatabase xmlDB)
 	throws Exception
 	{
 	    registerTypes(importedOTObjectClasses, typeService, xmlDB, true);
 	}
     
 	public static void registerTypes(List importedOTObjectClasses, TypeService typeService,
 			XMLDatabase xmlDB, boolean addShortcuts)
 		throws Exception 
 	{
 		ClassLoader classloader = ReflectionTypeDefinitions.class.getClassLoader();
 		Vector typeClasses = new Vector();
 		
 		for(int i=0; i<importedOTObjectClasses.size(); i++) {
 	        String className = (String)importedOTObjectClasses.get(i);			
 		    try {
 		        Class typeClass = classloader.loadClass(className);
 		        typeClasses.add(typeClass);
		    } catch (ClassNotFoundException e) {
 		        System.err.println("Error importing class: " + className);
                 System.err.println("  this class was listed as an import in the otml file");
		    } catch (Throwable e){
		    	// if we get something other than a class not found exception 
		    	// then the problem is more complex so the whole stack trace is
		    	// useful. 
		        System.err.println("Error importing class: " + className);
                System.err.println("  this class was listed as an import in the otml file");
                e.printStackTrace();
 		    }
 		}		
 		
 		ReflectiveOTClassFactory.singleton.loadClasses(typeClasses);
 		
 		// This is a hack util the refactoring is more complete.  The current way that <object/> elements
 		// handled requires this.
 		OTClass baseObjectClass = OTrunkImpl.getOTClass("org.concord.framework.otrunk.OTObject");
 		typeService.registerUserType(baseObjectClass, typeService.getElementHandler("object"));
 		
 		for(int i=0; i<typeClasses.size(); i++) {
 			Class otObjectClass = (Class)typeClasses.get(i); 
 			String className = otObjectClass.getName();
 			
 			OTClassImpl otClass = (OTClassImpl) OTrunkImpl.getOTClass(className);
 			
 			if(otClass == null){
 				System.err.println("Warning cannot find valid OTClass for import: " + className);
 				continue;
 			}
 			
 			Class resourceSchemaClass = otClass.getSchemaInterface();
 
 			if(resourceSchemaClass == null) {
 				throw new RuntimeException("Can't find valid schema class for: " +
 						className);
 			}
 			
 			ObjectTypeHandler objectType = 
 				new ObjectTypeHandler(
 						otClass,
 						className,
 						className,
 						null,
 						typeService,
 						xmlDB);
 			
 			typeService.registerUserType(className, objectType);
 			typeService.registerUserType(otClass, objectType);
 			
 			if(addShortcuts) {
 			    int lastDot = className.lastIndexOf(".");
 			    String localClassName = className.substring(lastDot+1,className.length());
 			    typeService.registerUserType(localClassName, objectType);
 			    typeService.registerShortcutName(localClassName, otClass);
 			}
 		}		
 	}	
 }
