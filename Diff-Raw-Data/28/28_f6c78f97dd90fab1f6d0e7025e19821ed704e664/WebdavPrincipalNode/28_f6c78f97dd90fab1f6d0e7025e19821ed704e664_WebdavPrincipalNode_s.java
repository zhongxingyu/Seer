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
 
 package org.bedework.webdav.servlet.shared;
 
 import org.bedework.access.AccessPrincipal;
 import org.bedework.access.Acl.CurrentAccess;
 import org.bedework.access.WhoDefs;
 import org.bedework.util.xml.XmlEmit;
 import org.bedework.util.xml.tagdefs.WebdavTags;
 
 import org.w3c.dom.Element;
 
 import java.io.Writer;
 import java.util.Collection;
 import java.util.HashMap;
 
 import javax.xml.namespace.QName;
 
 /** Class to represent a principal in webdav.
  *
  *
  *   @author Mike Douglass   douglm@bedework.edu
  */
 public class WebdavPrincipalNode extends WebdavNsNode {
   private AccessPrincipal account;
 
   private final static HashMap<QName, PropertyTagEntry> propertyNames =
     new HashMap<QName, PropertyTagEntry>();
 
   static {
     addPropEntry(propertyNames, WebdavTags.groupMemberSet);
     addPropEntry(propertyNames, WebdavTags.groupMembership);
   }
 
   /**
    * @param urlHandler - needed for building hrefs.
    * @param path - resource path
    * @param account
    * @param collection - true if this is a collection
    * @param uri
    * @throws WebdavException
    */
   public WebdavPrincipalNode(final UrlHandler urlHandler, final String path,
                              final AccessPrincipal account,
                              final boolean collection,
                              final String uri) throws WebdavException {
     super(urlHandler, path, collection, uri);
     this.account = account;
     userPrincipal = account.getKind() == WhoDefs.whoTypeUser;
     groupPrincipal = account.getKind() == WhoDefs.whoTypeGroup;
 //    if (displayName.startsWith("/")) {
 //      debugMsg(displayName);
 //    }
   }
 
   /* (non-Javadoc)
    * @see edu.bedework.cct.webdav.servlet.shared.WebdavNsNode#getOwner()
    */
   @Override
   public AccessPrincipal getOwner() throws WebdavException {
     return account;
   }
 
   /* (non-Javadoc)
    * @see edu.bedework.cct.webdav.servlet.shared.WebdavNsNode#update()
    */
   @Override
   public void update() throws WebdavException {
   }
 
   /* ====================================================================
    *                   Abstract methods
    * ==================================================================== */
 
   @Override
   public CurrentAccess getCurrentAccess() throws WebdavException {
     return null;
   }
 
   @Override
   public String getEtagValue(final boolean strong) throws WebdavException {
     String val = "1234567890";
 
     if (strong) {
       return "\"" + val + "\"";
     }
 
     return "W/\"" + val + "\"";
   }
 
   /* (non-Javadoc)
    * @see edu.bedework.cct.webdav.servlet.shared.WebdavNsNode#trailSlash()
    */
   @Override
   public boolean trailSlash() {
     return true;
   }
 
   /* (non-Javadoc)
    * @see edu.bedework.cct.webdav.servlet.shared.WebdavNsNode#getChildren()
    */
   @Override
   public Collection<? extends WdEntity> getChildren() throws WebdavException {
     return null;
   }
 
   @Override
   public WdCollection getCollection(final boolean deref) throws WebdavException {
     return null;
   }
 
   @Override
   public boolean allowsSyncReport() throws WebdavException {
     return false;
   }
 
   @Override
   public boolean getDeleted() throws WebdavException {
     return false;
   }
 
   @Override
   public String getSyncToken() throws WebdavException {
     return null;
   }
 
   /* ====================================================================
    *                   Required webdav properties
    * ==================================================================== */
 
   @Override
   public String writeContent(final XmlEmit xml,
                              final Writer wtr,
                              final String contentType) throws WebdavException {
     return null;
   }
 
   /* (non-Javadoc)
    * @see edu.bedework.cct.webdav.servlet.shared.WebdavNsNode#getContentBinary()
    */
   @Override
   public boolean getContentBinary() throws WebdavException {
     return false;
   }
 
   /* (non-Javadoc)
    * @see edu.bedework.cct.webdav.servlet.shared.WebdavNsNode#getContentLang()
    */
   @Override
   public String getContentLang() throws WebdavException {
     return null;
   }
 
   /* (non-Javadoc)
    * @see edu.bedework.cct.webdav.servlet.shared.WebdavNsNode#getContentLen()
    */
   @Override
  public long getContentLen() throws WebdavException {
     return 0;
   }
 
   /* (non-Javadoc)
    * @see edu.bedework.cct.webdav.servlet.shared.WebdavNsNode#getContentType()
    */
   @Override
   public String getContentType() throws WebdavException {
     return null;
   }
 
   /* (non-Javadoc)
    * @see edu.bedework.cct.webdav.servlet.shared.WebdavNsNode#getCreDate()
    */
   @Override
   public String getCreDate() throws WebdavException {
     return null;
   }
 
   /* (non-Javadoc)
    * @see edu.bedework.cct.webdav.servlet.shared.WebdavNsNode#getDisplayname()
    */
   @Override
   public String getDisplayname() throws WebdavException {
     return account.getAccount();
   }
 
   /* (non-Javadoc)
    * @see edu.bedework.cct.webdav.servlet.shared.WebdavNsNode#getLastmodDate()
    */
   @Override
   public String getLastmodDate() throws WebdavException {
     return null;
   }
 
   /* ====================================================================
    *                   Property methods
    * ==================================================================== */
 
   /* (non-Javadoc)
    * @see edu.bedework.cct.webdav.servlet.shared.WebdavNsNode#removeProperty(org.w3c.dom.Element)
    */
   @Override
   public boolean removeProperty(final Element val,
                                 final SetPropertyResult spr) throws WebdavException {
     warn("Unimplemented - removeProperty");
 
     return false;
   }
 
   /* (non-Javadoc)
    * @see edu.bedework.cct.webdav.servlet.shared.WebdavNsNode#setProperty(org.w3c.dom.Element)
    */
   @Override
   public boolean setProperty(final Element val,
                              final SetPropertyResult spr) throws WebdavException {
     if (super.setProperty(val, spr)) {
       return true;
     }
 
     return false;
   }
 
   /* (non-Javadoc)
    * @see edu.bedework.cct.webdav.servlet.shared.WebdavNsNode#knownProperty(edu.bedework.sss.util.xml.QName)
    */
   @Override
   public boolean knownProperty(final QName tag) {
     if (propertyNames.get(tag) != null) {
       return true;
     }
 
     // Not ours
     return super.knownProperty(tag);
   }
 
   /* (non-Javadoc)
    * @see edu.bedework.cct.webdav.servlet.shared.WebdavNsNode#generatePropertyValue(edu.bedework.sss.util.xml.QName, edu.bedework.cct.webdav.servlet.shared.WebdavNsIntf, boolean)
    */
   @Override
   public boolean generatePropertyValue(final QName tag,
                                        final WebdavNsIntf intf,
                                        final boolean allProp) throws WebdavException {
     String ns = tag.getNamespaceURI();
     XmlEmit xml = intf.getXmlEmit();
 
     /* Deal with webdav properties */
     if (!ns.equals(WebdavTags.namespace)) {
       // Not ours
       return super.generatePropertyValue(tag, intf, allProp);
     }
 
     try {
       if (tag.equals(WebdavTags.groupMemberSet)) {
         // PROPTODO
         xml.emptyTag(tag);
         return true;
       }
 
       if (tag.equals(WebdavTags.groupMembership)) {
         // PROPTODO
         xml.emptyTag(tag);
         return true;
       }
 
       // Not known - try higher
       return super.generatePropertyValue(tag, intf, allProp);
     } catch (Throwable t) {
       throw new WebdavException(t);
     }
   }
 
   /* ====================================================================
    *                   Private methods
    * ==================================================================== */
 }
