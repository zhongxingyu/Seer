 /*
  * This file is part of the OpenNMS(R) Application.
  *
  * OpenNMS(R) is Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
  * OpenNMS(R) is a derivative work, containing both original code, included code and modified
  * code that was published under the GNU General Public License. Copyrights for modified
  * and included code are below.
  *
  * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
  *
  * Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
  *
  * For more information contact:
  * OpenNMS Licensing       <license@opennms.org>
  *     http://www.opennms.org/
  *     http://www.opennms.com/
  */
 package org.opennms.protocols.rt;
 
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.apache.log4j.Category;
 import org.apache.log4j.Logger;
 
 /**
  * RequestLocatorImpl
  *
  * @author brozow
  */
 public class IDBasedRequestLocator<ReqIdT, ReqT extends Request<ReqIdT, ReqT, ReplyT>, ReplyT extends ResponseWithId<ReqIdT>> implements RequestLocator<ReqT, ReplyT> {
 
     private static final Logger s_log = Logger.getLogger(RequestTracker.class);
     
     private Map<ReqIdT, ReqT> m_pendingRequests = Collections.synchronizedMap(new HashMap<ReqIdT, ReqT>());
     
     public ReqT requestTimedOut(ReqT timedOutRequest) {
        ReqT pendingRequest = m_pendingRequests.remove(timedOutRequest.getId());
        return pendingRequest;
     }
     
     public void requestComplete(ReqT request) {
         m_pendingRequests.remove(request.getId());
     }
 
 
     public ReqT locateMatchingRequest(ReplyT reply) {
 
         ReqIdT id = reply.getRequestId();
         debugf("Looking for request with Id: %s in map %s", id, m_pendingRequests);
         ReqT request = m_pendingRequests.get(id);
         return request;
 
     }
     
     public boolean trackRequest(ReqT request) {
         synchronized(m_pendingRequests) {
             ReqT oldRequest = m_pendingRequests.get(request.getId());
             if (oldRequest != null) {
                 request.processError(new IllegalStateException("Duplicate request; keeping old request: "+oldRequest+"; removing new request: "+request));
                 return false;
             } else {
                 m_pendingRequests.put(request.getId(), request);
             }
         }
         return true;
     }
 
     private Category log() {
         return s_log;
     }
     
     private void debugf(String format, Object... args) {
         if (log().isDebugEnabled()) {
             log().debug(String.format(format, args));
         }
     }
 
    private void errorf(String format, Object... args) {
        log().error(String.format(format, args));
    }

    private void errorf(Throwable t, String format, Object... args) {
        log().error(String.format(format, args), t);
    }

 
 
 
 }
