 /*
  *
  *  JMoney - A Personal Finance Manager
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
 
 package net.sf.jmoney.fields;
 
 import org.eclipse.swt.widgets.Composite;
 
 import net.sf.jmoney.JMoneyPlugin;
 import net.sf.jmoney.model2.Commodity;
 import net.sf.jmoney.model2.Entry;
 import net.sf.jmoney.model2.ExtendableObject;
 import net.sf.jmoney.model2.IPropertyControl;
 import net.sf.jmoney.model2.IPropertyControlFactory;
 import net.sf.jmoney.model2.IPropertyRegistrar;
 import net.sf.jmoney.model2.IPropertySetInfo;
 import net.sf.jmoney.model2.PropertyAccessor;
 import net.sf.jmoney.model2.PropertySet;
 import net.sf.jmoney.model2.SessionChangeAdapter;
 
 /**
  * This class is a listener class to the net.sf.jmoney.fields
  * extension point.  It implements an extension.
  * <P>
  * This extension registers the Entry properties.  By registering
  * the properties, every one can know how to display, edit, and store
  * the properties.
  * <P>
  * These properties are supported in the JMoney base code, so everyone
  * including plug-ins will know about these properties.  However, to
  * follow the Eclipse paradigm (every one should be treated equal,
  * including oneself), these are registered through the same extension
  * point that plug-ins must also use to register their properties.
  * 
  * @author Nigel Westbury
  * @author Johann Gyger
  */
 public class EntryInfo implements IPropertySetInfo {
 
 	private static PropertySet propertySet = null;
 	private static PropertyAccessor checkAccessor = null;
 	private static PropertyAccessor descriptionAccessor = null;
 	private static PropertyAccessor accountAccessor = null;
 	private static PropertyAccessor valutaAccessor = null;
 	private static PropertyAccessor memoAccessor = null;
 	private static PropertyAccessor amountAccessor = null;
 	private static PropertyAccessor creationAccessor = null;
 
 	public EntryInfo() {
     }
 
 	public Class getImplementationClass() {
 		return Entry.class;
 	}
 	
 	public void registerProperties(PropertySet propertySet, IPropertyRegistrar propertyRegistrar) {
 		EntryInfo.propertySet = propertySet;
 		
 		IPropertyControlFactory textControlFactory = new TextControlFactory();
        IPropertyControlFactory dateControlFactory = new DateControlFactory();
 		
 		IPropertyControlFactory amountControlFactory = new AmountControlFactory() {
 
 			protected Commodity getCommodity(ExtendableObject object) {
 	    	        return ((Entry) object).getCommodity();
 			}
 
 			public IPropertyControl createPropertyControl(Composite parent, PropertyAccessor propertyAccessor) {
 		    	final AmountEditor editor = new AmountEditor(parent, propertyAccessor, this);
 		        
 		    	// The format of the amount will change if either
 		    	// the account property of the entry changes or if
 		    	// the commodity property of the account changes.
 		        editor.setListener(new SessionChangeAdapter() {
 		        		public void objectChanged(ExtendableObject changedObject, PropertyAccessor changedProperty, Object oldValue, Object newValue) {
 		        			Entry entry = (Entry)editor.getObject();
		        			if (entry == null) {
		        			    return;
		        			}
 		        			// Has the account property changed?
 		        			if (changedObject == entry && changedProperty == EntryInfo.getAccountAccessor()) {
 		        				editor.updateCommodity(entry.getCommodity());	
 		        			}
 		        			// Has the commodity property of the account changed?
 		        			if (changedObject ==  entry.getAccount() && changedProperty == CurrencyAccountInfo.getCurrencyAccessor()) {
 		        				editor.updateCommodity(entry.getCommodity());	
 		        			}
 		        			// If any property in the commodity object changed then
 		        			// the format of the amount might also change.
 		        			if (changedObject ==  entry.getCommodity()) {
 		        				editor.updateCommodity(entry.getCommodity());	
 		        			}
 		        			// TODO: All the above tests are still not complete.
 		        			// If the account for the entry can contain multiple
 		        			// commodities then the commodity may depend on properties
 		        			// in the entry object.  We really need a special listener
 		        			// that listens for any changes that would affect the
 		        			// Entry.getCommodity() value.
 		        		}
 		        	});   	
 		        
 		        return editor;
 			}};
 
 		checkAccessor       = propertyRegistrar.addProperty("check",       JMoneyPlugin.getResourceString("Entry.check"),        8.0, textControlFactory, null, null);
 		descriptionAccessor = propertyRegistrar.addProperty("description", JMoneyPlugin.getResourceString("Entry.description"), 30.0, textControlFactory, null, null);
		accountAccessor     = propertyRegistrar.addProperty("account",     JMoneyPlugin.getResourceString("Entry.account"),     30.0, null, null, null);  // TODO Control factory
		valutaAccessor      = propertyRegistrar.addProperty("valuta",      JMoneyPlugin.getResourceString("Entry.valuta"),      10.0, dateControlFactory, null, null);
 		memoAccessor        = propertyRegistrar.addProperty("memo",        JMoneyPlugin.getResourceString("Entry.memo"),        30.0, textControlFactory, null, null);
 		amountAccessor      = propertyRegistrar.addProperty("amount",      JMoneyPlugin.getResourceString("Entry.amount"),      10.0, amountControlFactory, null, null);
		creationAccessor    = propertyRegistrar.addProperty("creation",    JMoneyPlugin.getResourceString("Entry.creation"),    10.0, null, null, null);
 		
 		propertyRegistrar.setObjectDescription("Accounting Entry");
 	}
 
 	/**
 	 * @return
 	 */
 	public static PropertySet getPropertySet() {
 		return propertySet;
 	}
 
 	/**
 	 * @return
 	 */
 	public static PropertyAccessor getCheckAccessor() {
 		return checkAccessor;
 	}	
 
 	/**
 	 * @return
 	 */
 	public static PropertyAccessor getDescriptionAccessor() {
 		return descriptionAccessor;
 	}	
 
 	/**
 	 * @return
 	 */
 	public static PropertyAccessor getAccountAccessor() {
 		return accountAccessor;
 	}	
 
 	/**
 	 * @return
 	 */
 	public static PropertyAccessor getValutaAccessor() {
 		return valutaAccessor;
 	}	
 
 	/**
 	 * @return
 	 */
 	public static PropertyAccessor getMemoAccessor() {
 		return memoAccessor;
 	}	
 
 	/**
 	 * @return
 	 */
 	public static PropertyAccessor getAmountAccessor() {
 		return amountAccessor;
 	}	
 
 	/**
 	 * @return
 	 */
 	public static PropertyAccessor getCreationAccessor() {
 		return creationAccessor;
 	}	
 }
