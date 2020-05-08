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
 
 import edu.rpi.cct.webdav.servlet.shared.WebdavException;
 import edu.rpi.cct.webdav.servlet.shared.WebdavNotFound;
 import edu.rpi.cct.webdav.servlet.shared.WebdavNsIntf;
 import edu.rpi.cct.webdav.servlet.shared.WebdavNsNode;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 /** Class to handle COPY
  *
  *   @author Mike Douglass   douglm@rpi.edu
  */
 public class CopyMethod extends MethodBase {
   /** Called at each request
    */
   public void init() {
   }
 
   /* (non-Javadoc)
    * @see edu.rpi.cct.webdav.servlet.common.MethodBase#doMethod(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
    */
   public void doMethod(HttpServletRequest req,
                        HttpServletResponse resp) throws WebdavException {
     process(req, resp, true);
   }
 
   protected void process(HttpServletRequest req,
                          HttpServletResponse resp,
                          boolean copy) throws WebdavException {
     if (debug) {
       if (copy) {
         trace("CopyMethod: doMethod");
       } else {
         trace("MoveMethod: doMethod");
       }
     }
 
     try {
       String dest = req.getHeader("Destination");
       if (dest == null) {
         if (debug) {
           debugMsg("No Destination");
         }
         throw new WebdavNotFound("No Destination");
       }
 
       int depth = Headers.depth(req);
       /*
       if (depth == Headers.depthNone) {
         depth = Headers.depthInfinity;
       }
       */
 
       String ow = req.getHeader("Overwrite");
       boolean overwrite;
       if (ow == null) {
         overwrite = true;
       } else if ("T".equals(ow)) {
         overwrite = true;
       } else if ("F".equals(ow)) {
         overwrite = false;
       } else {
         resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
         return;
       }
 
       WebdavNsIntf intf = getNsIntf();
       WebdavNsNode from = intf.getNode(getResourceUri(req),
                                        WebdavNsIntf.existanceMust,
                                        WebdavNsIntf.nodeTypeUnknown);
 
       int toNodeType;
       if (from.getCollection()) {
         toNodeType = WebdavNsIntf.nodeTypeCollection;
       } else {
         toNodeType = WebdavNsIntf.nodeTypeEntity;
       }
 
      WebdavNsNode to = intf.getNode(intf.getUri(dest),
                                      WebdavNsIntf.existanceMay, toNodeType);
 
       intf.copyMove(req, resp, from, to, copy, overwrite, depth);
     } catch (WebdavException we) {
       throw we;
     } catch (Throwable t) {
       throw new WebdavException(t);
     }
   }
 }
