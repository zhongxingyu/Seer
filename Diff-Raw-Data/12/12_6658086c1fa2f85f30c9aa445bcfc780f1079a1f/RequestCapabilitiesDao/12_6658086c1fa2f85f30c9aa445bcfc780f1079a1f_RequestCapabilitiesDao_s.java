 /**
  * SAHARA Scheduling Server
  *
  * Schedules and assigns local laboratory rigs.
  *
  * @license See LICENSE in the top level directory for complete license terms.
  *
  * Copyright (c) 2010, University of Technology, Sydney
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without 
  * modification, are permitted provided that the following conditions are met:
  *
  *  * Redistributions of source code must retain the above copyright notice, 
  *    this list of conditions and the following disclaimer.
  *  * Redistributions in binary form must reproduce the above copyright 
  *    notice, this list of conditions and the following disclaimer in the 
  *    documentation and/or other materials provided with the distribution.
  *  * Neither the name of the University of Technology, Sydney nor the names 
  *    of its contributors may be used to endorse or promote products derived from 
  *    this software without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE 
  * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
  * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, 
  * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE 
  * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *
  * @author Michael Diponio (mdiponio)
  * @date 28th March 2010
  */
 package au.edu.uts.eng.remotelabs.schedserver.dataaccess.dao;
 
 import java.io.Serializable;
 import java.util.List;
 
 import org.hibernate.Criteria;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
 import org.hibernate.Session;
 import org.hibernate.criterion.Restrictions;
 
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.entities.MatchingCapabilities;
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.entities.MatchingCapabilitiesId;
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.entities.RequestCapabilities;
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.entities.ResourcePermission;
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.entities.RigCapabilities;
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.impl.Capabilities;
 
 /**
  * Data access object for the {@link RequestCapabilities} entity.
  */
 public class RequestCapabilitiesDao extends GenericDao<RequestCapabilities>
 {
     public RequestCapabilitiesDao()
     {
         super(RequestCapabilities.class);
     }
     
     public RequestCapabilitiesDao(Session ses)
     {
         super(ses, RequestCapabilities.class);
     }
     
     /**
      * Find the request capabilities entity which match the specified capabilities.
      * <code>null</code> is returned if none is found.
      *  
      * @param capabilities capabilities to find 
      * @return capabilities or null
      */
     public RequestCapabilities findCapabilites(String capabilities)
     {
         Criteria cri = this.session.createCriteria(RequestCapabilities.class);
         cri.add(Restrictions.eq("capabilities", new Capabilities(capabilities).asCapabilitiesString()));
         return (RequestCapabilities) cri.uniqueResult();
     }
     
     /** 
      * Adds a new request capabilities string.  Adding a request rig capabilities string 
      * involves creating the request capabilities record and computing all
      * the matches to the currently stored request capabilities.
      * <br />
      * A match is defined as having each request capability token present
      * as a token of the rig capabilities string. That is, all matching
      * request capabilities strings are subsets of the rig capabililities
      * string.
      * <p />
      * To denote tokens in a capabilities string, the comma ('&#44;') symbol
      * is used.
      * 
      * @param capabilities rig capabilities string
      * @return rig capabilities persistent object
      */
     public RequestCapabilities addCapabilities(String capabilities)
     {
         Capabilities requestCaps = new Capabilities(capabilities);
         GenericDao<MatchingCapabilities> matchCapsDao = 
             new GenericDao<MatchingCapabilities>(this.session, MatchingCapabilities.class);
         List<String> capsList = requestCaps.asCapabilitiesList();
         
         /* Store the rig capabilities. */
         RequestCapabilities reqCaps = this.persist(new RequestCapabilities(requestCaps.asCapabilitiesString()));
         
         /* Iterate through all the rig capabilities and store matches. */
         Criteria cri = this.session.createCriteria(RigCapabilities.class);
        ScrollableResults results = cri.scroll(ScrollMode.FORWARD_ONLY);
         
         Capabilities caps = new Capabilities();
        while (results.next())
         {
            RigCapabilities rigCaps = (RigCapabilities) results.get(0);
             caps.setCapabilitiesString(rigCaps.getCapabilities());
 
             if (caps.asCapabilitiesList().containsAll(capsList))
             {
                 /* Rig - Request capabilities match. */
                 MatchingCapabilities match = new MatchingCapabilities();
                 match.setId(new MatchingCapabilitiesId(rigCaps.getId(), reqCaps.getId()));
                 match.setRequestCapabilities(reqCaps);
                 match.setRigCapabilities(rigCaps);
                 matchCapsDao.persist(match);
             }
         }
 
         this.session.refresh(reqCaps);
         return reqCaps;
     }
     
     /**
      * Deletes the request capabilities identified by its primary key, including
      * all matches with rig capabilities.
      * 
      * @param id primary key of request capabilities to delete
      */
     @Override
     public void delete(Serializable id)
     {
         this.delete(this.get(id));
     }
     
     /**
      * Deletes the request capabilities, including all matches with rig
      * capabilities.
      * 
      * @param caps request capabilities to delete.
      */
     @Override
     public void delete(RequestCapabilities caps)
     {
         this.session.beginTransaction();
         for (MatchingCapabilities match : caps.getMatchingCapabilitieses())
         {
             this.session.delete(match);
         }
         
         for (ResourcePermission perm : caps.getResourcePermissions())
         {
             perm.setRequestCapabilities(null);
         }
         
         this.session.delete(caps);
         this.session.getTransaction().commit();
     }
 }
