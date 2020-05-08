 /*
  *
  *  JMoney - A Personal Finance Manager
  *  Copyright (c) 2002 Johann Gyger <johann.gyger@switzerland.org>
  *  Copyright (c) 2004 Nigel Westbury <westbury@users.sourceforge.net>
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
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Vector;
 
 import net.sf.jmoney.JMoneyPlugin;
 import net.sf.jmoney.fields.AccountInfo;
 
 /**
  * An implementation of the Account interface
  */
 public abstract class Account extends ExtendableObject {
 	
 	protected String name;
 
 	protected IListManager subAccounts;
 	
 	protected Account(
 			IObjectKey objectKey, 
 			Map extensions, 
 			IObjectKey parentKey,
 			String name,
 			IListManager subAccounts) {
 		super(objectKey, extensions, parentKey);
 		this.name = name;
 		this.subAccounts = subAccounts;
 	}
 	
 	/**
 	 * @return the name of this account.
 	 */
 	public String getName() {
 		return name;
 	}
 
 	/**
 	 * @param aName the name of this account.
 	 */
 	
 	public void setName(String newName) {
 		String oldName = name;
 		name = newName;
 		
 		// Notify the change manager.
 		processPropertyChange(AccountInfo.getNameAccessor(), oldName, newName);
 	}
 
 	public String getFullAccountName() {
 		return getName();
 	}
 	
 	public Account getParent() {
 		ExtendableObject parent = parentKey.getObject();
 		if (parent instanceof Account) {
 			return (Account)parent;
 		} else {
 			return null;
 		}
 	}
 
 	/**
 	 * Returns the commodity that the amount in an entry
 	 * represents.  If the account for the entry is an account
 	 * that can store only one commodity (usually a currency)
 	 * then the commodity is a property of the account.  If,
 	 * however, the account can hold multiple commodities (such
 	 * as a stock account) then information from the entry is
 	 * required in order to get the commodity involved.
 	 * 
 	 * @return Commodity for the given entry
 	 */
 	public abstract Commodity getCommodity(Entry entry);
 
 	public abstract ObjectCollection getSubAccountCollection();
 
 	public Collection getAllSubAccounts() {
 	    Collection all = new Vector();
 	    Iterator it = getSubAccountCollection().iterator();
 	    while (it.hasNext()) {
 	        Account a = (Account) it.next();
 	        all.add(a);
 	        all.addAll(a.getAllSubAccounts());
 	    }
 		return all;
 	}
 
 	boolean deleteSubAccount(Account subAccount) {
		return getSubAccountCollection().remove(subAccount);
 	}
 	
 	/**
 	 * Get the entries in the account.
 	 * 
 	 * @return A read-only collection with elements of
 	 * 				type <code>Entry</code>
 	 */
 	public Collection getEntries() {
 		Collection accountEntries = getObjectKey().getSessionManager().getEntries(this);
 		return Collections.unmodifiableCollection(accountEntries);
 	}
 	
 	/**
 	 * @return true if there are any entries in this account,
 	 * 			false if no entries are in this account
 	 */
 	public boolean hasEntries() {
 		return getObjectKey().getSessionManager().hasEntries(this);
 	}
 	
     /**
 	 * This method is used for debugging purposes only.
 	 */
 	public String toString() {
 		return getName();
 	}
 	
 	public int compareTo(Object o) {
 		Account c = (Account) o;
 		return getName().compareTo(c.getName());
 	}
 
     public int getLevel () {
         int level;
         if (getParent() == null)
             level = 0;
         else 
             level = getParent().getLevel() + 1;
         if (JMoneyPlugin.DEBUG) System.out.println("Level from " + this.name + ", child of " + getParent() +" is " + level);
         return level;
     }
 }
