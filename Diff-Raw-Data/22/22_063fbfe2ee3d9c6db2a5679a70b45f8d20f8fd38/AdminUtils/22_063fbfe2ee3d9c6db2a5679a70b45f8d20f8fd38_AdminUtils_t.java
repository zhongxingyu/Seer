 /*
  * $Source$
  * $Revision$
  *
  * Part of Melati (http://melati.org), a framework for the rapid
  * development of clean, maintainable web applications.
  *
  * -------------------------------------
  *  Copyright (C) 2000 William Chesters
  * -------------------------------------
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
  *
  * A copy of the GPL should be in the file org/melati/COPYING in this tree.
  * Or see http://melati.org/License.html.
  *
  * Contact details for copyright holder:
  *
  *     William Chesters <williamc@paneris.org>
  *     http://paneris.org/~williamc
  *     Obrechtstraat 114, 2517VX Den Haag, The Netherlands
  *
  *
  * ------
  *  Note
  * ------
  *
  * I will assign copyright to PanEris (http://paneris.org) as soon as
  * we have sorted out what sort of legal existence we need to have for
  * that to make sense.  When WebMacro's "Simple Public License" is
  * finalised, we'll offer it as an alternative license for Melati.
  * In the meantime, if you want to use Melati on non-GPL terms,
  * contact me!
  */
 
 package org.melati.admin;
 
 import org.webmacro.WebMacroException;
 import org.melati.poem.*;
 import org.melati.*;
 
 public class AdminUtils {
 
   private String adminURL;
   private String adminStaticURL;
   private String logicalDatabase;
 
   public AdminUtils(String adminURL, String adminStaticURL,
 		    String logicalDatabase) {
     this.adminURL = adminURL;
     this.adminStaticURL = adminStaticURL;
     this.logicalDatabase = logicalDatabase;
   }
 
   public String TopURL() {
     return adminURL + "/" + logicalDatabase + "/Top";
   }
   
   public String BottomURL(Table table) {
     return adminURL + "/" + logicalDatabase + "/" + table.getName() +
            "/Bottom";
   }
   
  public String LeftURL(Table table) {
    return adminURL + "/" + logicalDatabase + "/" + table.getName() +
           "/Left";
  }
  
   public String PrimarySelectURL(Table table) {
     return adminURL + "/" + logicalDatabase + "/" + table.getName() +
            "/PrimarySelect";
   }
 
   public String SelectionURL(Table table) {
     return adminURL + "/" + logicalDatabase + "/" + table.getName() +
            "/Selection";
   }
 
   public String NavigationURL(Table table) {
     return adminURL + "/" + logicalDatabase + "/" + table.getName() +
            "/Navigation";
   }
 
   public String editURL(Persistent object) throws AccessPoemException {
     return
         adminURL + "/" + logicalDatabase + "/" +
         object.getTable().getName() + "/" + object.troid() + "/Edit";
   }
 
   public String addURL(Table table) throws AccessPoemException {
     return
         adminURL + "/" + logicalDatabase + "/" +
         table.getAddURL();
   }
 
   public String PopupURL(Table table) {
     return adminURL + "/" + logicalDatabase + "/" + table.getName() + "/PopUp";
   }
 
   public String SelectionWindowURL(Table table) {
     return adminURL + "/" + logicalDatabase + "/" + table.getName() + "/SelectionWindow";
   }
   
   public String SelectionWindowPrimarySelectURL(Table table) {
     return adminURL + "/" + logicalDatabase + "/" + table.getName() + "/SelectionWindowPrimarySelect";
   }
   
   public String SelectionWindowSelectionURL(Table table) {
     return adminURL + "/" + logicalDatabase + "/" + table.getName() + "/SelectionWindowSelection";
   }
   
   // establish if this is a reference poem type field
   public boolean isReferencePoemType(Field field) {
     return field.getType() instanceof ReferencePoemType;
   }
 
   public String getStaticURL() {
     return adminStaticURL;
   }
 
   public String specialFacilities(Melati melati, MarkupLanguage ml,
 				  Persistent object)
       throws WebMacroException {
     if (!(object instanceof AdminSpecialised))
       return "";
     else
       return ml.templetExpansion(
           ((AdminSpecialised)object).adminSpecialFacilities(melati, ml));
   }
 }
