 /*
  Copyright (c) 2000-2005 University of Washington.  All rights reserved.
 
  Redistribution and use of this distribution in source and binary forms,
  with or without modification, are permitted provided that:
 
    The above copyright notice and this permission notice appear in
    all copies and supporting documentation;
 
    The name, identifiers, and trademarks of the University of Washington
    are not used in advertising or publicity without the express prior
    written permission of the University of Washington;
 
    Recipients acknowledge that this distribution is made available as a
    research courtesy, "as is", potentially with defects, without
    any obligation on the part of the University of Washington to
    provide support, services, or repair;
 
    THE UNIVERSITY OF WASHINGTON DISCLAIMS ALL WARRANTIES, EXPRESS OR
    IMPLIED, WITH REGARD TO THIS SOFTWARE, INCLUDING WITHOUT LIMITATION
    ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
    PARTICULAR PURPOSE, AND IN NO EVENT SHALL THE UNIVERSITY OF
    WASHINGTON BE LIABLE FOR ANY SPECIAL, INDIRECT OR CONSEQUENTIAL
    DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR
    PROFITS, WHETHER IN AN ACTION OF CONTRACT, TORT (INCLUDING
    NEGLIGENCE) OR STRICT LIABILITY, ARISING OUT OF OR IN CONNECTION WITH
    THE USE OR PERFORMANCE OF THIS SOFTWARE.
  */
 /* **********************************************************************
     Copyright 2005 Rensselaer Polytechnic Institute. All worldwide rights reserved.
 
     Redistribution and use of this distribution in source and binary forms,
     with or without modification, are permitted provided that:
        The above copyright notice and this permission notice appear in all
         copies and supporting documentation;
 
         The name, identifiers, and trademarks of Rensselaer Polytechnic
         Institute are not used in advertising or publicity without the
         express prior written permission of Rensselaer Polytechnic Institute;
 
     DISCLAIMER: The software is distributed" AS IS" without any express or
     implied warranty, including but not limited to, any implied warranties
     of merchantability or fitness for a particular purpose or any warrant)'
     of non-infringement of any current or pending patent rights. The authors
     of the software make no representations about the suitability of this
     software for any particular purpose. The entire risk as to the quality
     and performance of the software is with the user. Should the software
     prove defective, the user assumes the cost of all necessary servicing,
     repair or correction. In particular, neither Rensselaer Polytechnic
     Institute, nor the authors of the software are liable for any indirect,
     special, consequential, or incidental damages related to the software,
     to the maximum extent the law permits.
 */
 
 package edu.rpi.cct.webdav.servlet.shared;
 
 import edu.rpi.cmt.access.AccessPrincipal;
 import edu.rpi.cmt.access.WhoDefs;
 import edu.rpi.cmt.access.Acl.CurrentAccess;
 import edu.rpi.sss.util.xml.XmlEmit;
 import edu.rpi.sss.util.xml.tagdefs.WebdavTags;
 
 import org.w3c.dom.Element;
 
 import java.io.Writer;
 import java.util.Collection;
 import java.util.HashMap;
 
 import javax.xml.namespace.QName;
 
 /** Class to represent a principal in webdav.
  *
  *
  *   @author Mike Douglass   douglm@rpi.edu
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
    * @param debug
    * @throws WebdavException
    */
   public WebdavPrincipalNode(final UrlHandler urlHandler, final String path,
                              final AccessPrincipal account,
                              final boolean collection,
                              final String uri, final boolean debug) throws WebdavException {
     super(urlHandler, path, collection, uri, debug);
     this.account = account;
     userPrincipal = account.getKind() == WhoDefs.whoTypeUser;
     groupPrincipal = account.getKind() == WhoDefs.whoTypeGroup;
 //    if (displayName.startsWith("/")) {
 //      debugMsg(displayName);
 //    }
   }
 
   /* (non-Javadoc)
    * @see edu.rpi.cct.webdav.servlet.shared.WebdavNsNode#getOwner()
    */
   @Override
   public AccessPrincipal getOwner() throws WebdavException {
     return account;
   }
 
   /* (non-Javadoc)
    * @see edu.rpi.cct.webdav.servlet.shared.WebdavNsNode#update()
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
    * @see edu.rpi.cct.webdav.servlet.shared.WebdavNsNode#trailSlash()
    */
   @Override
   public boolean trailSlash() {
     return true;
   }
 
   /* (non-Javadoc)
    * @see edu.rpi.cct.webdav.servlet.shared.WebdavNsNode#getChildren()
    */
   @Override
  public Collection<? extends WdEntity> getChildren() throws WebdavException {
     return null;
   }
 
   @Override
   public WdCollection getCollection(final boolean deref) throws WebdavException {
     return null;
   }
 
   /* ====================================================================
    *                   Required webdav properties
    * ==================================================================== */
 
   @Override
   public boolean writeContent(final XmlEmit xml,
                               final Writer wtr,
                               final String contentType) throws WebdavException {
     return false;
   }
 
   /* (non-Javadoc)
    * @see edu.rpi.cct.webdav.servlet.shared.WebdavNsNode#getContentBinary()
    */
   @Override
   public boolean getContentBinary() throws WebdavException {
     return false;
   }
 
   /* (non-Javadoc)
    * @see edu.rpi.cct.webdav.servlet.shared.WebdavNsNode#getContentLang()
    */
   @Override
   public String getContentLang() throws WebdavException {
     return null;
   }
 
   /* (non-Javadoc)
    * @see edu.rpi.cct.webdav.servlet.shared.WebdavNsNode#getContentLen()
    */
   @Override
   public long getContentLen() throws WebdavException {
     return 0;
   }
 
   /* (non-Javadoc)
    * @see edu.rpi.cct.webdav.servlet.shared.WebdavNsNode#getContentType()
    */
   @Override
   public String getContentType() throws WebdavException {
     return null;
   }
 
   /* (non-Javadoc)
    * @see edu.rpi.cct.webdav.servlet.shared.WebdavNsNode#getCreDate()
    */
   @Override
   public String getCreDate() throws WebdavException {
     return null;
   }
 
   /* (non-Javadoc)
    * @see edu.rpi.cct.webdav.servlet.shared.WebdavNsNode#getDisplayname()
    */
   @Override
   public String getDisplayname() throws WebdavException {
     return account.getAccount();
   }
 
   /* (non-Javadoc)
    * @see edu.rpi.cct.webdav.servlet.shared.WebdavNsNode#getLastmodDate()
    */
   @Override
   public String getLastmodDate() throws WebdavException {
     return null;
   }
 
   /* ====================================================================
    *                   Property methods
    * ==================================================================== */
 
   /* (non-Javadoc)
    * @see edu.rpi.cct.webdav.servlet.shared.WebdavNsNode#removeProperty(org.w3c.dom.Element)
    */
   @Override
   public boolean removeProperty(final Element val,
                                 final SetPropertyResult spr) throws WebdavException {
     warn("Unimplemented - removeProperty");
 
     return false;
   }
 
   /* (non-Javadoc)
    * @see edu.rpi.cct.webdav.servlet.shared.WebdavNsNode#setProperty(org.w3c.dom.Element)
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
    * @see edu.rpi.cct.webdav.servlet.shared.WebdavNsNode#knownProperty(edu.rpi.sss.util.xml.QName)
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
    * @see edu.rpi.cct.webdav.servlet.shared.WebdavNsNode#generatePropertyValue(edu.rpi.sss.util.xml.QName, edu.rpi.cct.webdav.servlet.shared.WebdavNsIntf, boolean)
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
