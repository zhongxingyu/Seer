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
 
 package edu.rpi.cct.webdav.servlet.common;
 
 import org.bedework.davdefs.WebdavTags;
 
 import edu.rpi.cct.webdav.servlet.shared.WebdavBadRequest;
 import edu.rpi.cct.webdav.servlet.shared.WebdavException;
 import edu.rpi.cct.webdav.servlet.shared.WebdavNsIntf;
 import edu.rpi.cct.webdav.servlet.shared.WebdavServerError;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 
 /** Class to handle WebDav ACLs
  *
  *  @author Mike Douglass   douglm@rpi.edu
  */
 public class AclMethod extends MethodBase {
   /** Called at each request
    */
   public void init() {
   }
 
   public void doMethod(HttpServletRequest req,
                        HttpServletResponse resp) throws WebdavException {
     if (debug) {
       trace("AclMethod: doMethod");
     }
 
     Document doc = parseContent(req, resp);
 
     if (doc == null) {
       return;
     }
 
     WebdavNsIntf.AclInfo ainfo = processDoc(doc, getResourceUri(req));
 
     processResp(req, resp, ainfo);
   }
 
   /* ====================================================================
    *                   Private methods
    * ==================================================================== */
 
   /* We process the parsed document and produce a Collection of request
    * objects to process.
    */
   private WebdavNsIntf.AclInfo processDoc(Document doc, String uri) throws WebdavException {
     try {
       WebdavNsIntf intf = getNsIntf();
 
       WebdavNsIntf.AclInfo ainfo = intf.startAcl(uri);
 
       Element root = doc.getDocumentElement();
 
       /* We expect an acl root element containing 0 or more ace elemnts
          <!ELEMENT acl (ace)* >
        */
       if (!WebdavTags.acl.nodeMatches(root)) {
         throw new WebdavBadRequest();
       }
 
       Element[] aces = getChildrenArray(root);
 
       for (int i = 0; i < aces.length; i++) {
         Element curnode = aces[i];
 
         if (!WebdavTags.ace.nodeMatches(curnode)) {
           throw new WebdavBadRequest();
         }
 
         if (!processAcl(ainfo, curnode)) {
           break;
         }
       }
 
       return ainfo;
     } catch (Throwable t) {
       error(t.getMessage());
       if (debug) {
         t.printStackTrace();
       }
 
       throw new WebdavServerError();
     }
   }
 
   /* Process an acl<br/>
          <!ELEMENT ace ((principal | invert), (grant|deny), protected?,
                          inherited?)>
          <!ELEMENT grant (privilege+)>
          <!ELEMENT deny (privilege+)>
 
          protected and inherited are for acl display
    */
   private boolean processAcl(WebdavNsIntf.AclInfo ainfo, Node nd) throws WebdavException {
     WebdavNsIntf intf = getNsIntf();
 
     Element[] children = getChildrenArray(nd);
 
     if (children.length < 2) {
       throw new WebdavBadRequest();
     }
 
     Element curnode = children[0];
     boolean inverted = false;
 
     /* Require principal or invert */
 
     if (WebdavTags.principal.nodeMatches(curnode)) {
     } else if (WebdavTags.invert.nodeMatches(curnode)) {
       /*  <!ELEMENT invert principal>       */
 
       inverted = true;
       curnode = getOnlyChild(curnode);
     } else {
       throw new WebdavBadRequest();
     }
 
     if (!intf.parseAcePrincipal(ainfo, curnode, inverted)) {
       return false;
     }
 
     /* Recognize grant or deny */
     for (int i = 1; i < children.length; i++) {
       curnode = children[i];
 
       boolean denial = false;
 
       if (WebdavTags.deny.nodeMatches(curnode)) {
         denial = true;
       } else if (!WebdavTags.grant.nodeMatches(curnode)) {
         ainfo.errorTag = WebdavTags.noAceConflict;
         return false;
       }
 
       Element[] pchildren = getChildrenArray(curnode);
 
       for (int pi = 0; pi < pchildren.length; pi++) {
         Element pnode = pchildren[pi];
 
         if (!WebdavTags.privilege.nodeMatches(pnode)) {
           throw new WebdavBadRequest();
         }
 
         intf.parsePrivilege(ainfo, pnode, denial);
       }
     }
 
     return true;
   }
 
   private void processResp(HttpServletRequest req,
                           HttpServletResponse resp,
                           WebdavNsIntf.AclInfo ainfo) throws WebdavException {
     WebdavNsIntf intf = getNsIntf();
 
     if (ainfo.errorTag == null) {
       intf.updateAccess(ainfo);
       return;
     }
 
     startEmit(resp);
     resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
 
     openTag(WebdavTags.error);
     emptyTag(ainfo.errorTag);
     closeTag(WebdavTags.error);
 
     flush();
   }
 }
 
