 /*
  *
  *  JMoney - A Personal Finance Manager
  *  Copyright (c) 2006 Nigel Westbury <westbury@users.sourceforge.net>
  *
  *
  *  This program is free software; you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation; either version 2 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program; if not, write to the Free Software
  *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
  *
  */
 
 package net.sf.jmoney.model2;
 
 
 /**
  * This is a helper class that makes it a little easier for a plug-in to extend
  * the CapitalAccount object.
  * <P>
  * To add fields and methods to a CapitalAccount object, one may derive a class
  * from CapitalAccountExtension. This mechanism allows multiple extensions to a
  * CapitalAccount object to be added and maintained at runtime.
  * <P>
  * All extensions to CapitalAccount objects implement the same methods that are
  * in the CapitalAccount object. This is for convenience so the consumer can get
  * a single object that supports both the original CapitalAccount methods and
  * the extension methods. All CapitalAccount methods are passed on to the
  * CapitalAccount object.
  * 
  * @author Nigel Westbury
  */
 public abstract class CapitalAccountExtension extends AccountExtension {
     
    public CapitalAccountExtension(ExtendableObject extendedObject) {
     	super(extendedObject);
     }
 
 	public String getAbbreviation() {
 		return getBaseObject().getAbbreviation();
 	}
 
 	/**
 	 * @return the comment of this account.
 	 */
 	public String getComment() {
 		return getBaseObject().getComment();
 	}
 
     @Override	
 	public ObjectCollection<CapitalAccount> getSubAccountCollection() {
 		return getBaseObject().getSubAccountCollection();
 	}
 
 	
 	public void setAbbreviation(String abbreviation) {
 		getBaseObject().setAbbreviation(abbreviation);
 	}
 
 	public void setComment(String comment) {
 		getBaseObject().setComment(comment);
 	}
 
 	public <A extends CapitalAccount> A createSubAccount(ExtendablePropertySet<A> propertySet) {
 		return getBaseObject().createSubAccount(propertySet);
 	}
         
 	boolean deleteSubAccount(CapitalAccount subAccount) {
 		return getBaseObject().deleteSubAccount(subAccount);
 	}
 
 	// This does some casting - perhaps this is not needed
 	// if generics are used????
     @Override	
 	public CapitalAccount getBaseObject() {
 		return (CapitalAccount)baseObject;
 	}
 }
