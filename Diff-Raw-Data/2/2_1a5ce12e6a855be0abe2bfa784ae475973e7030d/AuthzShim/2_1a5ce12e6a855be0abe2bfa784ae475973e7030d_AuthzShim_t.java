 /**********************************************************************************
  * $URL$
  * $Id$
  ***********************************************************************************
  *
  * Copyright (c) 2004, 2005 The Regents of the University of Michigan, Trustees of Indiana University,
  *                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
  *
  * Licensed under the Educational Community License Version 1.0 (the "License");
  * By obtaining, using and/or copying this Original Work, you agree that you have read,
  * understand, and will comply with the terms and conditions of the Educational Community License.
  * You may obtain a copy of the License at:
  *
  *      http://cvs.sakaiproject.org/licenses/license_1_0.html
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
  * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
  * AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
  * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
  * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
  *
  **********************************************************************************/
 package org.sakaiproject.metaobj.security.impl;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.sakaiproject.authz.api.AuthzGroup;
 import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.authz.api.AuthzGroupService;
 import org.sakaiproject.metaobj.security.AuthorizationFacade;
 import org.sakaiproject.metaobj.security.AuthorizationFailedException;
 import org.sakaiproject.metaobj.shared.model.Agent;
 import org.sakaiproject.metaobj.shared.model.Id;
 import org.sakaiproject.thread_local.cover.ThreadLocalManager;
 import org.sakaiproject.user.api.UserDirectoryService;
 import org.sakaiproject.webapp.cover.ToolManager;
 
 /**
  * Created by IntelliJ IDEA.
  * User: John Ellis
  * Date: Jun 30, 2005
  * Time: 4:57:18 PM
  * To change this template use File | Settings | File Templates.
  */
 public class AuthzShim implements AuthorizationFacade {
    protected final transient Log logger = LogFactory.getLog(getClass());
 
    private static final String AUTHZ_GROUPS_LIST =
       "org.sakaiproject.metaobj.security.impl.AuthzShim.groups";
 
    private AuthzGroupService realmService;
    private UserDirectoryService userDirectoryService;
 
    public void checkPermission(String function, Id id) throws AuthorizationFailedException {
       if (!isAuthorized(function, id)) {
          throw new AuthorizationFailedException(function, id);
       }
    }
 
    public void checkPermission(Agent agent, String function, Id id) throws AuthorizationFailedException {
       if (!isAuthorized(agent, function, id)) {
          throw new AuthorizationFailedException(agent, function, id);
       }
    }
 
    public boolean isAuthorized(String function, Id id) {
       return isAuthorized(null, function, id);
    }
 
    public boolean isAuthorized(Agent agent, String function, Id id) {
       String agentId = null;
       if (agent == null) {
          agentId = getUserDirectoryService().getCurrentUser().getId();
       }
       if (function.equals("maintain")) {
          return checkMaintain(agentId);
       }
       return getRealmService().isAllowed(agentId, function, getCurrentRealm());
    }
 
    protected boolean checkMaintain(String agentId) {
       AuthzGroup siteRealm = null;
       try {
          siteRealm = getRealmService().getAuthzGroup(getCurrentRealm());
       }
       catch (GroupNotDefinedException e) {
          logger.warn("unkown realm", e);
       }
       String maintain = siteRealm.getMaintainRole();
 
       return siteRealm.hasRole(agentId, maintain);
    }
 
    protected String getCurrentRealm() {
       if (getAuthzGroupsList().size() == 0) {
          return "/site/" + ToolManager.getCurrentPlacement().getContext();
       }
       else {
          return "/site/" + getAuthzGroupsList().get(0);
       }
    }
 
    protected String getReference(Id id) {
       return null;
    }
 
    public List getAuthorizations(Agent agent, String function, Id id) {
       return new ArrayList();
    }
 
    public void createAuthorization(Agent agent, String function, Id id) {
    }
 
    public void deleteAuthorization(Agent agent, String function, Id id) {
    }
 
    public void deleteAuthorizations(Id qualifier) {
    }
 
    public void pushAuthzGroups(Collection authzGroups) {
       List authzGroupList = getAuthzGroupsList();
       authzGroupList.addAll(authzGroups);
    }
 
    public void pushAuthzGroups(String siteId) {
       getAuthzGroupsList().add(siteId);
    }
 
    public AuthzGroupService getRealmService() {
       return realmService;
    }
 
    public void setRealmService(AuthzGroupService realmService) {
       this.realmService = realmService;
    }
 
    public UserDirectoryService getUserDirectoryService() {
       return userDirectoryService;
    }
 
    public void setUserDirectoryService(UserDirectoryService userDirectoryService) {
       this.userDirectoryService = userDirectoryService;
    }
 
    protected List getAuthzGroupsList() {
       List returned = (List) ThreadLocalManager.get(AUTHZ_GROUPS_LIST);
 
       if (returned == null) {
          returned = new ArrayList();
          ThreadLocalManager.set(AUTHZ_GROUPS_LIST, returned);
       }
       return returned;
    }
 
 }
