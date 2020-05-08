 /* ********************************************************************
     Licensed to Jasig under one or more contributor license
     agreements. See the NOTICE file distributed with this work
     for additional information regarding copyright ownership.
     Jasig licenses this file to you under the Apache License,
     Version 2.0 (the "License"); you may not use this file
     except in compliance with the License. You may obtain a
     copy of the License at:
 
     http://www.apache.org/licenses/LICENSE-2.0
 
     Unless required by applicable law or agreed to in writing,
     software distributed under the License is distributed on
     an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
     KIND, either express or implied. See the License for the
     specific language governing permissions and limitations
     under the License.
 */
 package edu.rpi.cct.webdav.servlet.shared;
 
 import edu.rpi.cmt.access.AccessPrincipal;
 import edu.rpi.sss.util.DateTimeUtil;
 import edu.rpi.sss.util.Util;
 
 import java.util.Date;
 
 /** Class to represent an entity in WebDAV
  *
  * @author douglm
  *
  */
 public abstract class WdEntity implements Comparable<WdEntity> {
   /** The internal name of the entity
    */
   private String name;
 
   private String displayName;
 
   /* The path up to and including this object */
   private String path;
 
   /* The path up to this object */
   private String parentPath;
 
   private AccessPrincipal owner;
 
   /** UTC datetime */
   private String created;
 
   /** UTC datetime */
   private String lastmod;
 
   private String description;
 
   /** Constructor
    *
    * @throws WebdavException
    */
   public WdEntity() throws WebdavException {
     super();
 
     Date dt = new Date();
     setLastmod(DateTimeUtil.isoDateTimeUTC(dt));
     setCreated(DateTimeUtil.isoDateTimeUTC(dt));
   }
 
   /* ====================================================================
    *                      Abstract methods
    * ==================================================================== */
 
   /**
    * @return true if this is an alias for another entity.
    * @throws WebdavException
    */
   public abstract boolean isAlias() throws WebdavException;
 
  /** If isAlias() resolves the alias. Otherwise just returns the parameter.
    *
    * @return WdEntity or null.
   * @param resolveSubAlias - if true and the alias points to an alias, resolve
   *                  down to a non-alias.
    * @throws WebdavException
    */
  public abstract WdEntity resolveAlias(final boolean resolveSubAlias) throws WebdavException;
 
   /* ====================================================================
    *                      Bean methods
    * ==================================================================== */
 
   /** Set the name
    *
    * @param val    String name
    * @throws WebdavException
    */
   public void setName(final String val) throws WebdavException {
     name = val;
   }
 
   /** Get the name
    *
    * @return String   name
    * @throws WebdavException
    */
   public String getName() throws WebdavException {
     return name;
   }
 
   /** Set the display name
    *
    * @param val    String display name
    * @throws WebdavException
    */
   public void setDisplayName(final String val) throws WebdavException {
     displayName = val;
   }
 
   /** Get the display name
    *
    * @return String   display name
    * @throws WebdavException
    */
   public String getDisplayName() throws WebdavException {
     return displayName;
   }
 
   /** Set the path to this collection
    *
    * @param val    String path
    * @throws WebdavException
    */
   public void setPath(final String val) throws WebdavException {
     path = val;
   }
 
   /** Get the path
    *
    * @return String   path
    * @throws WebdavException
    */
   public String getPath() throws WebdavException {
     return path;
   }
 
   /** Set the path to this collection
    *
    * @param val    String path
    * @throws WebdavException
    */
   public void setParentPath(final String val) throws WebdavException {
     parentPath = val;
   }
 
   /** Get the path
    *
    * @return String   path
    * @throws WebdavException
    */
   public String getParentPath() throws WebdavException {
     return parentPath;
   }
 
   /**
    * @param val
    * @throws WebdavException
    */
   public void setOwner(final AccessPrincipal val) throws WebdavException {
     owner = val;
   }
 
   /**
    * @return AccessPrincipal
    * @throws WebdavException
    */
   public AccessPrincipal getOwner() throws WebdavException {
     return owner;
   }
 
   /**
    * @param val
    * @throws WebdavException
    */
   public void setCreated(final String val) throws WebdavException {
     created = val;
   }
 
   /**
    * @return String created
    * @throws WebdavException
    */
   public String getCreated() throws WebdavException {
     return created;
   }
 
   /**
    * @param val
    * @throws WebdavException
    */
   public void setLastmod(final String val) throws WebdavException {
     lastmod = val;
   }
 
   /**
    * @return String lastmod
    * @throws WebdavException
    */
   public String getLastmod() throws WebdavException {
     return lastmod;
   }
 
   /** Get the current etag value
    *
    * @return String    the etag
    * @throws WebdavException
    */
   public abstract String getEtag() throws WebdavException;
 
   /** Get the etag value before any changes were applied
    *
    * @return String    the etag
    * @throws WebdavException
    */
   public abstract String getPreviousEtag() throws WebdavException;
 
   /** Set the description
    *
    * @param val    String description
    * @throws WebdavException
    */
   public void setDescription(final String val) throws WebdavException {
     description = val;
   }
 
   /** Get the description
    *
    * @return String   description
    * @throws WebdavException
    */
   public String getDescription() throws WebdavException {
     return description;
   }
 
   /**
    * @param sb
    */
   public void toStringSegment(final StringBuilder sb) {
     try {
       addStringSegmentNoComma(sb, "name", getName());
 
       addStringSegment(sb, "displayName", getDisplayName());
 
       addStringSegment(sb, "path", getPath());
 
       addStringSegment(sb, "parentPath", getParentPath());
 
       addStringSegment(sb, "owner", getOwner());
 
       addStringSegment(sb, "created", getCreated());
 
       addStringSegment(sb, "lastmod", getLastmod());
 
       addStringSegment(sb, "etag", getEtag());
 
       addStringSegment(sb, "previousEtag", getPreviousEtag());
 
       addStringSegment(sb, "description", getDescription());
     } catch (Throwable t) {
       sb.append(t);
     }
   }
 
   /**
    * @param sb
    * @param name
    * @param val
    * @throws Throwable
    */
   public void addStringSegment(final StringBuilder sb,
                                final String name,
                                final Object val) throws Throwable {
     sb.append(", ");
     addStringSegmentNoComma(sb, name, val);
   }
 
   /**
    * @param sb
    * @param name
    * @param val
    * @throws Throwable
    */
   public void addStringSegmentNoComma(final StringBuilder sb,
                                       final String name,
                                       final Object val) throws Throwable {
     sb.append(name);
     sb.append("=");
     if (val == null) {
       sb.append("<<null>>");
     } else {
       sb.append(String.valueOf(val));
     }
   }
 
   /* ====================================================================
    *                      Object methods
    * ==================================================================== */
 
   @Override
   public int hashCode() {
     try {
       return getPath().hashCode() * getName().hashCode();
     } catch (Throwable t) {
       throw new RuntimeException(t);
     }
   }
 
  @Override
   public int compareTo(final WdEntity that)  {
     try {
       if (this == that) {
         return 0;
       }
 
       return Util.cmpObjval(getPath(), that.getPath());
     } catch (Throwable t) {
       throw new RuntimeException(t);
     }
   }
 
   @Override
   public String toString() {
     StringBuilder sb = new StringBuilder("WdEntity{");
 
     toStringSegment(sb);
 
     sb.append("}");
 
     return sb.toString();
   }
 }
