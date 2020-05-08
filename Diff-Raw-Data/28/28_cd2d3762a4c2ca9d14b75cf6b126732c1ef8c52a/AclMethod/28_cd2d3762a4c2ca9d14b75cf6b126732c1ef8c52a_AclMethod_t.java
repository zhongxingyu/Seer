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
 
 import edu.rpi.cct.webdav.servlet.shared.WebdavBadRequest;
 import edu.rpi.cct.webdav.servlet.shared.WebdavException;
 import edu.rpi.cct.webdav.servlet.shared.WebdavNsIntf;
 import edu.rpi.cct.webdav.servlet.shared.WebdavServerError;
 import edu.rpi.cmt.access.AccessException;
 import edu.rpi.sss.util.xml.tagdefs.WebdavTags;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

 /** Class to handle WebDav ACLs
  *
  *  @author Mike Douglass   douglm@rpi.edu
  */
 public class AclMethod extends MethodBase {
   /** Called at each request
    */
  @Override
   public void init() {
   }
 
  @Override
  public void doMethod(final HttpServletRequest req,
                       final HttpServletResponse resp) throws WebdavException {
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
  private WebdavNsIntf.AclInfo processDoc(final Document doc, final String uri) throws WebdavException {
     try {
       WebdavNsIntf intf = getNsIntf();
 
       WebdavNsIntf.AclInfo ainfo = new WebdavNsIntf.AclInfo(uri);
 
       Element root = doc.getDocumentElement();
 
       AccessUtil autil = intf.getAccessUtil();
 
      ainfo.acl = autil.getAcl(root, true);
 
       if (autil.getErrorTag() != null) {
         ainfo.errorTag = autil.getErrorTag();
       }
 
       return ainfo;
     } catch (WebdavException wde) {
       throw wde;
     } catch (AccessException ae) {
       throw new WebdavBadRequest(ae.getMessage());
     } catch (Throwable t) {
       error(t.getMessage());
       if (debug) {
         t.printStackTrace();
       }
 
       throw new WebdavServerError();
     }
   }
 
  private void processResp(final HttpServletRequest req,
                          final HttpServletResponse resp,
                          final WebdavNsIntf.AclInfo ainfo) throws WebdavException {
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
 
