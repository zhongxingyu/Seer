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
 
 package net.sf.jmoney.pages.accountProperties;
 
 import java.util.Iterator;
 import java.util.Vector;
 
 import net.sf.jmoney.IBookkeepingPage;
 import net.sf.jmoney.IBookkeepingPageFactory;
 import net.sf.jmoney.JMoneyPlugin;
 import net.sf.jmoney.fields.AccountInfo;
 import net.sf.jmoney.model2.CapitalAccount;
 import net.sf.jmoney.model2.ExtendableObject;
 import net.sf.jmoney.model2.IPropertyControl;
 import net.sf.jmoney.model2.PropertyAccessor;
 import net.sf.jmoney.model2.PropertySet;
 import net.sf.jmoney.model2.Session;
 import net.sf.jmoney.model2.SessionChangeAdapter;
 import net.sf.jmoney.model2.SessionChangeListener;
 import net.sf.jmoney.views.NodeEditor;
 import net.sf.jmoney.views.SectionlessPage;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.FocusAdapter;
 import org.eclipse.swt.events.FocusEvent;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.ui.IMemento;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.forms.widgets.FormToolkit;
 
 /**
  * @author Nigel Westbury
  */
 public class AccountPropertiesPage implements IBookkeepingPageFactory {
 	
     private static final String PAGE_ID = "net.sf.jmoney.accountProperties";
     
 	/**
 	 * The implementation for the composite control that contains
 	 * the account property controls.
 	 */
 	private class AccountPropertiesControl extends Composite {
 		CapitalAccount account;
 		Session session;
 		/**
 		 * List of the IPropertyControl objects for the
 		 * properties that can be edited in this panel.
 		 */
 		Vector propertyControlList = new Vector();
 	
 		// Listen for changes to the account properties.
 		// If anyone else changes any of the account properties
 		// then we update the values in the edit controls.
 		// Note that we do not need to deal with the case where
 		// the account is deleted or the session is closed.  
 		// If the account is deleted then
 		// the navigation view will destroy the node and destroy
 		// this view.
 		
 		private SessionChangeListener listener =
 			new SessionChangeAdapter() {
         	public void objectChanged(ExtendableObject extendableObject, PropertyAccessor changedProperty, Object oldValue, Object newValue) {
 				if (extendableObject.equals(AccountPropertiesControl.this.account)) {
 					// Find the control for this property.
 					IPropertyControl propertyControl = (IPropertyControl)propertyControlList.get(changedProperty.getIndexIntoScalarProperties());
 					propertyControl.load(account);
 				}
 			}
 		};
 		
 		/**
 		 * @param parent
 		 */
 		public AccountPropertiesControl(Composite parent, CapitalAccount account, FormToolkit toolkit) {
 			super(parent, SWT.NULL);
 
 			GridLayout layout = new GridLayout();
 			layout.numColumns = 2;
 			setLayout(layout);
			GridData data = new GridData();
			data.verticalAlignment = GridData.FILL;
			data.horizontalAlignment = GridData.FILL;
			setLayoutData(data);
 			
 			// TODO: what is this?
 			pack();
 			
 			JMoneyPlugin.getDefault().addSessionChangeListener(listener, this);
 
 			this.account = account;
 			
 			session = account.getSession();
 			
 			// Create the controls to edit the properties.
 			
 			// Add the properties for the Account objects.
 			PropertySet extendablePropertySet = PropertySet.getPropertySet(account.getClass());
 			for (Iterator iter = extendablePropertySet.getPropertyIterator3(); iter.hasNext(); ) {
 				final PropertyAccessor propertyAccessor = (PropertyAccessor)iter.next();
 				if (propertyAccessor.isScalar()) {
 					Label propertyLabel = new Label(this, 0);
 					propertyLabel.setText(propertyAccessor.getShortDescription() + ':');
 					final IPropertyControl propertyControl = propertyAccessor.createPropertyControl(this);
 					propertyControl.getControl().addFocusListener(
 							new FocusAdapter() {
 
 								// When a control gets the focus, save the old value here.
 								// This value is used in the change message.
 								String oldValueText;
 								
 								public void focusLost(FocusEvent e) {
 									if (AccountPropertiesControl.this.session.isSessionFiring()) {
 										return;
 									}
 									
 									propertyControl.save();
 									String newValueText = propertyAccessor.formatValueForMessage(
 											AccountPropertiesControl.this.account);
 									
 									String description;
 									if (propertyAccessor == AccountInfo.getNameAccessor()) {
 										description = 
 											"rename account from " + oldValueText
 											+ " to " + newValueText;
 									} else {
 										description = 
 											"change " + propertyAccessor.getShortDescription() + " property"
 											+ " in '" + AccountPropertiesControl.this.account.getName() + "' account"
 											+ " from " + oldValueText
 											+ " to " + newValueText;
 									}
 									AccountPropertiesControl.this.session.registerUndoableChange(description);
 								}
 								public void focusGained(FocusEvent e) {
 									// Save the old value of this property for use in our 'undo' message.
 									oldValueText = propertyAccessor.formatValueForMessage(
 											AccountPropertiesControl.this.account);
 								}
 							});
 					
 					// Add to our list of controls.
 					propertyControlList.add(propertyControl);
 
 					toolkit.adapt(propertyLabel, false, false);
 					toolkit.adapt(propertyControl.getControl(), true, true);
 				}
 				
 			}
 			
 			// Set the values from the account object into the control fields.
 			for (Iterator iter = propertyControlList.iterator(); iter.hasNext(); ) {
 				IPropertyControl propertyControl = (IPropertyControl)iter.next();
 				propertyControl.load(account);
 			}
 		}
 	}
 
 
 	/* (non-Javadoc)
 	 * @see net.sf.jmoney.IBookkeepingPage#createPages(java.lang.Object, org.eclipse.swt.widgets.Composite)
 	 */
 	public IBookkeepingPage createFormPage(NodeEditor editor, IMemento memento)
 	{
 		SectionlessPage formPage = new SectionlessPage(
 				editor,
 				PAGE_ID, 
 				"Properties", 
 				"Account Properties") {
 			
			AccountPropertiesControl propertiesControl;
			
 			public Composite createControl(Object nodeObject, Composite parent, FormToolkit toolkit, IMemento memento) {
 				CapitalAccount account = (CapitalAccount)nodeObject;
				propertiesControl = new AccountPropertiesControl(parent, account, toolkit);
				return propertiesControl;
 			}
 
 			public void saveState(IMemento memento) {
 				// The user cannot change the view of the account properties
 				// so there is no 'view state' to save.
 			}
 		};
 
 		try {
 			editor.addPage(formPage);
 		} catch (PartInitException e) {
 			JMoneyPlugin.log(e);
 			// TODO: cleanly leave out this page.
 		}
 		
 		return formPage;
 	}
 }
