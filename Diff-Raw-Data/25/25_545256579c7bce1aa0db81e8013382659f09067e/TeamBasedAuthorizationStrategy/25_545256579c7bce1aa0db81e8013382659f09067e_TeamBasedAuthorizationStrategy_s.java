 /*
  * Copyright (c) 2013 Oracle Corporation.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Winston Prakash
  */
 package org.eclipse.hudson.security.team;
 
 import com.thoughtworks.xstream.converters.Converter;
 import com.thoughtworks.xstream.converters.MarshallingContext;
 import com.thoughtworks.xstream.converters.UnmarshallingContext;
 import com.thoughtworks.xstream.io.HierarchicalStreamReader;
 import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
 import hudson.Extension;
 import hudson.model.Descriptor;
 import hudson.model.Hudson;
 import hudson.model.Job;
 import hudson.security.ACL;
 import hudson.security.AuthorizationStrategy;
 import hudson.security.Permission;
 import hudson.util.FormValidation;
 import java.io.IOException;
 import java.util.Collection;
 import java.util.Collections;
 import org.kohsuke.stapler.DataBoundConstructor;
 import org.kohsuke.stapler.HttpResponse;
 import org.kohsuke.stapler.HttpResponses;
 import org.kohsuke.stapler.QueryParameter;
 
 /**
  * Team based authorization strategy
  *
  * @since 3.1.0
  * @author Winston Prakash
  */
 public class TeamBasedAuthorizationStrategy extends AuthorizationStrategy {
 
     @DataBoundConstructor
     public TeamBasedAuthorizationStrategy() {
     }
 
     /**
      * Get the root ACL which has grand authority over all model level ACLs
      *
      * @return root ACL, obtained from Team nager
      */
     @Override
     public ACL getRootACL() {
         return getTeamManager().getRoolACL();
     }
 
     /**
      * Get the specific ACL for jobs.
      *
      * @param job The access-controlled job
      * @return The job specific ACL
      */
     @Override
     public ACL getACL(Job<?, ?> job) {
         return getTeamManager().getACL(job);
     }
 
     public ACL getACL(Team team) {
         return getTeamManager().getACL(team);
     }
 
     /**
      * Used by the container realm.
      *
      * @return empty List
      */
     @Override
     public Collection<String> getGroups() {
         return Collections.EMPTY_LIST;
     }
 
     @Extension
     public static final class TeamBasedAuthorizationStrategyDescriptor extends Descriptor<AuthorizationStrategy> {
 
         @Override
         public String getDisplayName() {
             return Messages.TeamBasedAuthorizationStrategy_DisplayName();
         }
 
         public HttpResponse doAddSysAdmin(@QueryParameter String sysAdminSid) throws IOException {
             if (!Hudson.getInstance().getSecurityManager().hasPermission(Permission.HUDSON_ADMINISTER)) {
                 return HttpResponses.forbidden();
             }
 
             if ((sysAdminSid == null) || "".equals(sysAdminSid.trim())) {
                 return new TeamUtils.ErrorHttpResponse("Sys admin name required");
             }
 
            TeamManager teamManager = Hudson.getInstance().getTeamManager(this);
             if (teamManager.getSysAdmins().contains(sysAdminSid)) {
                 return new TeamUtils.ErrorHttpResponse(sysAdminSid + " is already a System Administrator.");
             }
 
             teamManager.addSysAdmin(sysAdminSid);
 
             return FormValidation.respond(FormValidation.Kind.OK, TeamUtils.getIcon(sysAdminSid));
         }
         
         public HttpResponse doRemoveSysAdmin(@QueryParameter String sysAdminSid) throws IOException {
             if (!Hudson.getInstance().getSecurityManager().hasPermission(Permission.HUDSON_ADMINISTER)) {
                 return HttpResponses.forbidden();
             }
 
             if ((sysAdminSid == null) || "".equals(sysAdminSid.trim())) {
                 return new TeamUtils.ErrorHttpResponse("Sys admin name required");
             }
 
            TeamManager teamManager = Hudson.getInstance().getTeamManager(this);
             if (teamManager.getSysAdmins().contains(sysAdminSid)) {
                 teamManager.removeSysAdmin(sysAdminSid);
                 return HttpResponses.ok();
             }else{
                 return new TeamUtils.ErrorHttpResponse(sysAdminSid + " is not a System Administrator.");
             }
         }
 
         public HttpResponse doCheckSid(@QueryParameter String sid) throws IOException {
             return FormValidation.respond(FormValidation.Kind.OK, TeamUtils.getIcon(sid));
         }
     }
 
     public static class ConverterImpl implements Converter {
 
         @Override
         public boolean canConvert(Class type) {
             return type == TeamBasedAuthorizationStrategy.class;
         }
 
         @Override
         public void marshal(Object o, HierarchicalStreamWriter writer, MarshallingContext mc) {
         }
 
         @Override
         public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext uc) {
             return new TeamBasedAuthorizationStrategy();
         }
     }
 
     private TeamManager getTeamManager() {
         return Hudson.getInstance().getTeamManager();
     }
 }
