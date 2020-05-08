 /***************************************************************
  *  This file is part of the [fleXive](R) framework.
  *
  *  Copyright (c) 1999-2012
  *  UCS - unique computing solutions gmbh (http://www.ucs.at)
  *  All rights reserved
  *
  *  The [fleXive](R) project is free software; you can redistribute
  *  it and/or modify it under the terms of the GNU Lesser General Public
  *  License version 2.1 or higher as published by the Free Software Foundation.
  *
  *  The GNU Lesser General Public License can be found at
  *  http://www.gnu.org/licenses/lgpl.html.
  *  A copy is found in the textfile LGPL.txt and important notices to the
  *  license from the author are found in LICENSE.txt distributed with
  *  these libraries.
  *
  *  This library is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  For further information about UCS - unique computing solutions gmbh,
  *  please see the company website: http://www.ucs.at
  *
  *  For further information about [fleXive](R), please see the
  *  project website: http://www.flexive.org
  *
  *
  *  This copyright notice MUST APPEAR in all copies of the file!
  ***************************************************************/
 package com.flexive.shared;
 
 import com.flexive.shared.exceptions.FxNoAccessException;
 import com.flexive.shared.value.FxString;
 import org.apache.commons.lang.StringUtils;
 
 import java.io.Serializable;
 
 /**
  * Phrase implementation
  *
  * @author Markus Plesser (markus.plesser@ucs.at), UCS - unique computing solutions gmbh (http://www.ucs.at)
  */
 @SuppressWarnings("UnusedDeclaration")
 public class FxPhrase implements Serializable {
     private long id = -1L;
     private long mandator;
     private String key;
     private String value; 
     private FxString fxValue;
     private String tag;
     private FxString fxTag;
 
     public FxPhrase(String key, FxString fxValue, String tag) {
         this.mandator = FxContext.getUserTicket().getMandatorId();
         this.key = key;
         this.value = null;
         this.fxValue = fxValue;
         this.tag = tag;
         this.fxTag = null;
     }
 
     public FxPhrase(String key, FxString fxValue, FxString fxTag) {
         this.mandator = FxContext.getUserTicket().getMandatorId();
         this.key = key;
         this.value = null;
         this.fxValue = fxValue;
         this.tag = null;
         this.fxTag = fxTag;
     }
 
     public FxPhrase(long mandator, String key, FxString fxValue, String tag) {
         this.mandator = mandator;
         this.key = key;
         this.value = null;
         this.fxValue = fxValue;
         this.tag = tag;
         this.fxTag = null;
     }
 
     public FxPhrase(long mandator, String key, FxString fxValue, FxString fxTag) {
         this.mandator = mandator;
         this.key = key;
         this.value = null;
         this.fxValue = fxValue;
         this.tag = null;
         this.fxTag = fxTag;
     }
 
     public FxPhrase(long mandator, String key, String value, String tag) {
         this.mandator = mandator;
         this.key = key;
         this.value = value;
         this.fxValue = null;
         this.tag = tag;
         this.fxTag = null;
     }
 
     public long getMandator() {
         return mandator;
     }
 
     public String getKey() {
         return key;
     }
 
     public void setKey(String key) {
         this.key = key;
     }
 
     public FxString getValue() {
         if(fxValue == null)
             return new FxString(false, value);
         return fxValue;
     }
     
     public String getSingleValue() {
         if(value != null)
             return value;
        return fxValue.getBestTranslation();
     }
 
     public void setValue(FxString value) {
         this.fxValue = value;
     }
 
     public String getTag() {
         if (tag != null)
             return tag;
         else
            return fxTag.getBestTranslation();
     }
 
     public FxString getFxTag() {
         if (fxTag != null)
             return fxTag;
         return new FxString(false, tag);
     }
 
     public void setTag(String tag) {
         this.tag = tag;
         this.fxTag = null;
     }
 
     public void setFxTag(FxString fxTag) {
         this.fxTag = fxTag;
         this.tag = null;
     }
 
     public boolean hasTag() {
         return (fxTag != null && !fxTag.isEmpty()) || (tag != null && !StringUtils.isBlank(tag));
     }
 
     public long getId() {
         return id;
     }
 
     public FxPhrase setId(long id) {
         this.id = id;
         return this;
     }
 
     public boolean hasId() {
         return this.id >= 0;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public boolean equals(Object obj) {
         if(!(obj instanceof FxPhrase))
             return false;
         FxPhrase other = (FxPhrase)obj;
         try {
             return !(this.hasId() && other.hasId() && this.getId() != other.getId()) &&
                     other.getKey().equals(this.getKey()) && other.getMandator() == this.getMandator() &&
                     other.hasTag() == this.hasTag() && other.getFxTag().equals(this.getFxTag());
         } catch (Exception e) {
             return false;
         }
     }
 
     @Override
     public String toString() {
         return "{" + getSingleValue() + " (id:" + getId() + ",mandator:" + getMandator() + ")" + "}";
     }
 
     public FxPhrase save() throws FxNoAccessException {
         EJBLookup.getPhraseEngine().savePhrase(this.getKey(), this.getValue(), tag != null ? tag : fxTag != null ? fxTag : null, this.mandator);
         return this;
     }
 }
