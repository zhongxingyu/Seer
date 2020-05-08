 /*
 
 Copyright (C) 2010 Steffen Dienst
 
 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.
 
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
 package de.elatexam.editor.components.panels.tasks;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.Model;
 
 /**
  * @author Steffen Dienst
  *
  */
public class SortableIdModel extends Model<String> {
 
     private static final String STUPID_SEPARATOR = "_%sort%_";
 
     private IModel<String> nestedModel;
 
     public SortableIdModel(IModel<String> nestedModel) {
         this.nestedModel = nestedModel;
     }
     @Override
     public String getObject() {
         String id = nestedModel.getObject();
         return getRealId(id);
     }
 
     private static String getRealId(String id) {
         if (id != null && id.contains(STUPID_SEPARATOR))
             return id.substring(id.indexOf(STUPID_SEPARATOR) + STUPID_SEPARATOR.length());
         else
             return id;
     }
     @Override
     public void setObject(String object) {
         String id = nestedModel.getObject();
         String sortTag = getSortTag(id);
         if (StringUtils.isEmpty(sortTag)) {
            super.setObject(object);
         } else {
            super.setObject(sortTag + STUPID_SEPARATOR + object);
         }
     }
 
     private static String getSortTag(String id) {
        if (id.contains(STUPID_SEPARATOR))
             return id.substring(0, id.indexOf(STUPID_SEPARATOR));
         else
             return "";
     }
 
     /**
      * @param currentId
      * @param orderIdx
      * @return
      */
     public static String getTaggedId(String currentId, int orderIdx) {
         String value = getRealId(currentId);
         return orderIdx + STUPID_SEPARATOR + value;
     }
 }
