 /*
  * Jamm
  * Copyright (C) 2002 Dave Dribin and Keith Garner
  *  
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  */
 
 package jamm.webapp;
 
 import java.util.Set;
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.struts.action.ActionMapping;
 
 
 /**
  * Holds the check boxes for the account list of the domain admin
  * form.  This is a list of check boxes for Delete, Action, and Admin.
  * Used by both DomainAccountAction and DomainAliasAction.
  *
  * @see jamm.webapp.DomainAccountAction
  * @see jamm.webapp.DomainAliasAction
 * @struts.form name="domainAccountForm"
 * @struts.form name="domainAliasForm"
  */
 public class DomainConfigForm extends JammForm
 {
     /**
      * Sets the items to delete.
      *
      * @param itemsToDelete an array of strings containing the items
      *                      to delete.
      */
     public void setItemsToDelete(String[] itemsToDelete)
     {
         mItemsToDelete = itemsToDelete;
     }
 
     /**
      * Sets the original list of items to delete.
      *
      * @param originalItemsToDelete original items to delete
      */
     public void setOriginalItemsToDelete(String[] originalItemsToDelete)
     {
         mOriginalItemsToDelete = originalItemsToDelete;
     }
 
     /**
      * Returns the items to delete.
      *
      * @return a string array containing the items to delete.
      */
     public String[] getItemsToDelete()
     {
         return mItemsToDelete;
     }
 
     /**
      * returns the original list of items to delete
      *
      * @return a string array
      */
     public String[] getOriginalItemsToDelete()
     {
         return mOriginalItemsToDelete;
     }
 
     /**
      * Returns newly checked items to delete.
      *
      * @return a set of accounts
      */
     public Set getCheckedDeleteItems()
     {
         return getDifference(mItemsToDelete, mOriginalItemsToDelete);
     }
 
     /**
      * Returns newly unchecked items to delete.
      *
      * @return a set of accounts
      */
     public Set getUncheckedDeleteItems()
     {
         return getDifference(mOriginalItemsToDelete, mItemsToDelete);
     }
     
 
     /**
      * The original list of active items.
      *
      * @param originalActiveItems an array of strings containing the
      *                            originally active items.
      */
     public void setOriginalActiveItems(String[] originalActiveItems)
     {
         mOriginalActiveItems = originalActiveItems;
     }
 
     /**
      * Returns the original list of active items.
      *
      * @return a string array containing the originally active items.
      */
     public String[] getOriginalActiveItems()
     {
         return mOriginalActiveItems;
     }
 
     /**
      * Sets the active items.
      *
      * @param activeItems an array of strings containing the active items.
      */
     public void setActiveItems(String[] activeItems)
     {
         mActiveItems = activeItems;
     }
 
     /**
      * Returns the active items.
      *
      * @return a string array of the active items.
      */
     public String[] getActiveItems()
     {
         return mActiveItems;
     }
 
     /**
      * Returns the newly checked active items from the form.
      *
      * @return a Set of the newly checked active items.
      */
     public Set getCheckedActiveItems()
     {
         return getDifference(mActiveItems, mOriginalActiveItems);
     }
 
     /**
      * Returns the newly unchecked active items from the form.
      *
      * @return a Set of the newly unchecked active items.
      */
     public Set getUncheckedActiveItems()
     {
         return getDifference(mOriginalActiveItems, mActiveItems);
     }
 
     /**
      * Set the originally checked admin items.
      *
      * @param originalAdminItems a string array of the checked items.
      */
     public void setOriginalAdminItems(String[] originalAdminItems)
     {
         mOriginalAdminItems = originalAdminItems;
     }
 
     /**
      * Returns the originally checked admin items.
      *
      * @return a string array of the originally unchecked items.
      */
     public String[] getOriginalAdminItems()
     {
         return mOriginalAdminItems;
     }
 
     /**
      * Sets the list of checked admin items.
      *
      * @param adminItems a string array of the checked admin items.
      */
     public void setAdminItems(String[] adminItems)
     {
         mAdminItems = adminItems;
     }
 
     /**
      * Returns the list of checked admin items.
      *
      * @return a string array of the checked admin items.
      */
     public String[] getAdminItems()
     {
         return mAdminItems;
     }
 
     /**
      * Returns the newly checked admin items.
      *
      * @return a Set of the newly checked admin items.
      */
     public Set getCheckedAdminItems()
     {
         return getDifference(mAdminItems, mOriginalAdminItems);
     }
 
     /**
      * Returns the newly unchecked items.
      *
      * @return a Set of the newly unchecked admin items.
      */
     public Set getUncheckedAdminItems()
     {
         return getDifference(mOriginalAdminItems, mAdminItems);
     }
 
     /**
      * Sets the domain name this form is for.
      *
      * @param domain Domain name
      */
     public void setDomain(String domain)
     {
         mDomain = domain;
     }
 
     /**
      * Returns the domain name this form is for.
      *
      * @return The domain name this form is for
      */
     public String getDomain()
     {
         return mDomain;
     }
 
     /**
      * Resets the form to the default values.  In this case, all of
      * the lists become empty.
      *
      * @param mapping The mapping used to select this instance.
      * @param request The servlet request we are processing.
      */
     public void reset(ActionMapping mapping, HttpServletRequest request)
     {
         mItems = new String[0];
         mItemsToDelete = new String[0];
         mOriginalItemsToDelete = new String[0];
         mOriginalActiveItems = new String[0];
         mActiveItems = new String[0];
         mOriginalAdminItems = new String[0];
         mAdminItems = new String[0];
         mDomain = request.getParameter("domain");
     }
 
     /** The list of items. */
     private String[] mItems;
     /** The checked items to delete */
     private String[] mItemsToDelete;
     /** The original list of items to delete */
     private String[] mOriginalItemsToDelete;
     /** The original list of active items */
     private String[] mOriginalActiveItems;
     /** The checked active items */
     private String[] mActiveItems;
     /** The original list of admin items */
     private String[] mOriginalAdminItems;
     /** The checked list of admin items. */
     private String[] mAdminItems;
     /** The domain name this data pertains to. */
     private String mDomain;
 }
