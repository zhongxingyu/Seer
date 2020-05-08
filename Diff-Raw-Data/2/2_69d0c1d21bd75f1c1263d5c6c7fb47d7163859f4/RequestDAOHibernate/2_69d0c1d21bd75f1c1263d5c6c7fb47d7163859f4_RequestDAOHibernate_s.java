 /**
  * Copyright (c) Members of the EGEE Collaboration. 2006-2009.
  * See http://www.eu-egee.org/partners/ for details on the copyright holders.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  * Authors:
  * 	Andrea Ceccanti (INFN)
  */
 package org.glite.security.voms.admin.persistence.dao.hibernate;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.UUID;
 
 import org.glite.security.voms.admin.error.VOMSException;
 import org.glite.security.voms.admin.persistence.dao.generic.RequestDAO;
 import org.glite.security.voms.admin.persistence.error.AlreadyExistsException;
 import org.glite.security.voms.admin.persistence.error.AlreadyMemberException;
 import org.glite.security.voms.admin.persistence.model.Certificate;
 import org.glite.security.voms.admin.persistence.model.VOMSGroup;
 import org.glite.security.voms.admin.persistence.model.VOMSRole;
 import org.glite.security.voms.admin.persistence.model.VOMSUser;
 import org.glite.security.voms.admin.persistence.model.request.CertificateRequest;
 import org.glite.security.voms.admin.persistence.model.request.GroupMembershipRequest;
 import org.glite.security.voms.admin.persistence.model.request.MembershipRemovalRequest;
 import org.glite.security.voms.admin.persistence.model.request.NewVOMembershipRequest;
 import org.glite.security.voms.admin.persistence.model.request.Request;
 import org.glite.security.voms.admin.persistence.model.request.RequesterInfo;
 import org.glite.security.voms.admin.persistence.model.request.RoleMembershipRequest;
 import org.glite.security.voms.admin.persistence.model.request.Request.STATUS;
 import org.hibernate.Criteria;
 import org.hibernate.criterion.Disjunction;
 import org.hibernate.criterion.Order;
 import org.hibernate.criterion.Restrictions;
 
 public class RequestDAOHibernate extends GenericHibernateDAO<Request, Long>
 		implements RequestDAO {
 
 	
 
 	public CertificateRequest createCertificateRequest(VOMSUser u,
 			String certificateSubject, String certificateIssuer,
 			Date expirationDate) {
 		
 		
 			if (userHasPendingCertificateRequest(u, certificateSubject, certificateIssuer))
 				throw new AlreadyExistsException("User '"+u+"' has a pending certificate request for '"+certificateSubject+","+certificateIssuer+"',");
 			
 		CertificateRequest req = new CertificateRequest();
 		req.setStatus(STATUS.SUBMITTED);
 		req.setRequesterInfo(RequesterInfo.fromVOUser(u));
 		req.setCreationDate(new Date());
 		req.setExpirationDate(expirationDate);
 		
 		req.setCertificateSubject(certificateSubject);
 		req.setCertificateIssuer(certificateIssuer);
 		
 		makePersistent(req);
 		
 		return req;
 	}
 
 	public GroupMembershipRequest createGroupMembershipRequest(VOMSUser usr,
 			VOMSGroup group, Date expirationDate) {
 		
 		if (usr.isMember(group))
 			throw new VOMSException("User '"+usr+"' is already member of group '"+group+"'!");
 		
 		if (userHasPendingGroupMembershipRequest(usr, group))
 			throw new AlreadyMemberException("User '"+usr+"' has a pending group membership request for group '"+group+"'!");
 		
 		GroupMembershipRequest req = new GroupMembershipRequest();
 		req.setStatus(STATUS.SUBMITTED);
 		req.setRequesterInfo(RequesterInfo.fromVOUser(usr));
 		req.setCreationDate(new Date());
 		req.setExpirationDate(expirationDate);
 		
 		req.setGroupName(group.getName());
 		
 		makePersistent(req);
 		
 		return req;
 	}
 
 	public MembershipRemovalRequest createMembershipRemovalRequest(
 			VOMSUser usr, String reason, Date expirationDate) {
 		
 		MembershipRemovalRequest req = new MembershipRemovalRequest();
 		req.setStatus(STATUS.SUBMITTED);
 		req.setRequesterInfo(RequesterInfo.fromVOUser(usr));
 		req.setCreationDate(new Date());
 		req.setExpirationDate(expirationDate);
 		req.setReason(reason);
 		
 		makePersistent(req);
 		
 		return req;
 	}
 
 	public RoleMembershipRequest createRoleMembershipRequest(VOMSUser usr,
 			VOMSGroup group, VOMSRole r, Date expirationDate) {
 		
 		if (usr.hasRole(group, r))
 			throw new AlreadyMemberException("User '"+usr+"' already has role '"+r.getName()+"' in group '"+group+"'!");
 		
 		RoleMembershipRequest req = new RoleMembershipRequest();
 		req.setStatus(STATUS.SUBMITTED);
 		req.setRequesterInfo(RequesterInfo.fromVOUser(usr));
 		req.setCreationDate(new Date());
 		req.setExpirationDate(expirationDate);
 		
 		req.setGroupName(group.getName());
 		req.setRoleName(r.getName());
 		
 		makePersistent(req);
 		
 		return req;
 	}
 	
 	public NewVOMembershipRequest createVOMembershipRequest(
 			RequesterInfo requester, Date expirationDate) {
 
 		NewVOMembershipRequest req = new NewVOMembershipRequest();
 
 		req.setStatus(STATUS.SUBMITTED);
 		req.setRequesterInfo(requester);
 		req.setCreationDate(new Date());
 		req.setExpirationDate(expirationDate);
 
 		req.setConfirmId(UUID.randomUUID().toString());
 		makePersistent(req);
 
 		return req;
 	}
 
 	public void deleteRequestFromUser(VOMSUser u) {
 		List<Request> userReqs = findRequestsFromUser(u);
 		
 		for (Request r: userReqs)	
 			makeTransient(r);
 		
 		
 	}
 	
 	public NewVOMembershipRequest findActiveVOMembershipRequest(
 			RequesterInfo requester) {
 		Criteria crit = getSession().createCriteria(
 				NewVOMembershipRequest.class);
 		
 		crit.add(Restrictions.ne("status", STATUS.APPROVED)).add(
 				Restrictions.ne("status", STATUS.REJECTED)).createCriteria(
 				"requesterInfo").add(
 				Restrictions.eq("certificateSubject", requester
 						.getCertificateSubject())).add(
 				Restrictions.eq("certificateIssuer", requester
 						.getCertificateIssuer()));
 
 		return (NewVOMembershipRequest) crit.uniqueResult();
 
 	}
 	
 	
 	public List<NewVOMembershipRequest> findConfirmedVOMembershipRequests() {
 
 		Criteria crit = getSession().createCriteria(
 				NewVOMembershipRequest.class);
 
 		crit.add(Restrictions.eq("status", STATUS.CONFIRMED));
 
 		return crit.list();
 
 	}
 	
 	public List<NewVOMembershipRequest> findPendingVOMembershipRequests() {
 
 		Criteria crit = getSession().createCriteria(
 				NewVOMembershipRequest.class);
 
 		crit.add(Restrictions.eq("status", STATUS.SUBMITTED));
 
 		return crit.list();
 
 	}
 	
 	
 
 	public List<NewVOMembershipRequest> findExpiredVOMembershipRequests() {
 		Criteria crit = getSession().createCriteria(NewVOMembershipRequest.class);
 		
 		 Date now = new Date();
 		 crit.add(Restrictions.lt("expirationDate", now));
 		 crit.add(Restrictions.eq("status", STATUS.SUBMITTED));
 		 
 		return crit.list();
 	}
 	
 	public List<CertificateRequest> findPendingCertificateRequests(){
 		Criteria crit = getSession().createCriteria(CertificateRequest.class);
 		crit.add(Restrictions.eq("status", STATUS.SUBMITTED));
 		
 		return crit.list();
 		
 	}
 
 	public List<GroupMembershipRequest> findPendingGroupMembershipRequests() {
 		
 		Criteria crit = getSession().createCriteria(GroupMembershipRequest.class);
 		
 		crit.add(Restrictions.eq("status", STATUS.SUBMITTED));
 		
 		return crit.list();
 	}
 
 	
 	public List<MembershipRemovalRequest> findPendingMembershipRemovalRequests() {
 		Criteria crit = getSession().createCriteria(MembershipRemovalRequest.class);
 		
 		crit.add(Restrictions.eq("status", STATUS.SUBMITTED));
 		
 		return crit.list();
 	}
 	
 	public List<Request> findPendingRequests() {
 		List<Request> result = new ArrayList<Request>();
 		
 		result.addAll(findPendingVOMembershipRequests());
 		result.addAll(findConfirmedVOMembershipRequests());
 		result.addAll(findPendingGroupMembershipRequests());
 		result.addAll(findPendingRoleMembershipRequests());
 		result.addAll(findPendingCertificateRequests());
 		result.addAll(findPendingMembershipRemovalRequests());
 		
 		return result;
 	}
 
 	public List<RoleMembershipRequest> findPendingRoleMembershipRequests() {
 		Criteria crit = getSession().createCriteria(RoleMembershipRequest.class);
 		
 		crit.add(Restrictions.eq("status", STATUS.SUBMITTED));
 		return crit.list();
 	}
 	
 
 	public List<CertificateRequest> findPendingUserCertificateRequests(VOMSUser u){
 		
 		Criteria crit = getSession().createCriteria(CertificateRequest.class);
 		
 		crit.add(Restrictions.disjunction().
 				add(Restrictions.eq("status", STATUS.SUBMITTED)).
 				add(Restrictions.eq("status", STATUS.PENDING))).createCriteria("requesterInfo")
 				.add(getDnEqualityCheckConstraints(u));
 		
 		return crit.list();
 	}
 	
 	public List<GroupMembershipRequest> findPendingUserGroupMembershipRequests(VOMSUser u){
 		
 		Criteria crit = getSession().createCriteria(
 				GroupMembershipRequest.class);
 		
 		
 		crit.add(Restrictions.disjunction().
 				add(Restrictions.eq("status", STATUS.SUBMITTED)).
 				add(Restrictions.eq("status", STATUS.PENDING))).createCriteria("requesterInfo")
 				.add(getDnEqualityCheckConstraints(u));
 				
 		
 		return crit.list();
 	}
 
 	public List<MembershipRemovalRequest> findPendingUserMembershipRemovalRequests(
 			VOMSUser u) {
 		
 		Criteria crit = getSession().createCriteria(MembershipRemovalRequest.class);
 		
 		crit.add(Restrictions.disjunction().
 				add(Restrictions.eq("status", STATUS.SUBMITTED)).
 				add(Restrictions.eq("status", STATUS.PENDING))).createCriteria("requesterInfo")
 				.add(getDnEqualityCheckConstraints(u));
 		
 		return crit.list();
 	}
 
 	public List<RoleMembershipRequest> findPendingUserRoleMembershipRequests(VOMSUser u){
 		Criteria crit = getSession().createCriteria(RoleMembershipRequest.class);
 		crit.add(Restrictions.disjunction().
 				add(Restrictions.eq("status", STATUS.SUBMITTED)).
 				add(Restrictions.eq("status", STATUS.PENDING))).createCriteria("requesterInfo")
 				.add(getDnEqualityCheckConstraints(u));
 		
 		return crit.list();
 	}
 
 	public List<NewVOMembershipRequest> findRejectedVOMembershipRequests() {
 		Criteria crit = getSession().createCriteria(NewVOMembershipRequest.class);
 		crit.add(Restrictions.eq("status", STATUS.REJECTED));
 		 
 		return crit.list();
 	}
 
 	public List<Request> findRequestsFromUser(VOMSUser u) {
 		
 		Criteria crit = getSession().createCriteria(Request.class);
 		crit.addOrder(Order.desc("creationDate"));
 		crit.createCriteria("requesterInfo").add(getDnEqualityCheckConstraints(u));
 		
 		return crit.list();
 	}
 
 	protected Disjunction getDnEqualityCheckConstraints(VOMSUser u){
 		
 		Disjunction dnEqualityChecks = Restrictions.disjunction();
 		
 		for (Certificate c: u.getCertificates())
 			dnEqualityChecks.add(Restrictions.eq("certificateSubject", c.getSubjectString()));
 		
 		return dnEqualityChecks;
 		
 	}
 
 	public boolean userHasPendingCertificateRequest(VOMSUser u,
 			String certificateSubject, String certificateIssuer) {
 		
 		
 		Criteria crit = getSession().createCriteria(CertificateRequest.class);
 		
 		crit.add(Restrictions.eq("certificateSubject", certificateSubject));
		crit.add(Restrictions.eq("caSubject", certificateIssuer));
 		
 		crit.add(Restrictions.disjunction().
 				add(Restrictions.eq("status", STATUS.SUBMITTED)).
 				add(Restrictions.eq("status", STATUS.PENDING))).createCriteria("requesterInfo").add(getDnEqualityCheckConstraints(u));		
 		
 		List<CertificateRequest> reqs = crit.list();
 		
 		if (reqs ==  null || reqs.isEmpty())
 			return false;
 		
 		return true;
 	}
 
 	public boolean userHasPendingGroupMembershipRequest(VOMSUser u, VOMSGroup g){
 		
 		
 		Criteria crit = getSession().createCriteria(
 				GroupMembershipRequest.class);
 		
 		
 		crit.add(Restrictions.eq("groupName", g.getName()));
 		
 		crit.add(Restrictions.disjunction().
 				add(Restrictions.eq("status", STATUS.SUBMITTED)).
 				add(Restrictions.eq("status", STATUS.PENDING))).createCriteria("requesterInfo").add(getDnEqualityCheckConstraints(u));
 				
 		List<GroupMembershipRequest> reqs= crit.list();
 		
 		if (reqs ==  null || reqs.isEmpty())
 			return false;
 		
 		
 		return true;
 	}
 
 	public boolean userHasPendingMembershipRemovalRequest(VOMSUser u) {
 		Criteria crit = getSession().createCriteria(MembershipRemovalRequest.class);
 		
 		crit.add(Restrictions.disjunction().
 				add(Restrictions.eq("status", STATUS.SUBMITTED)).
 				add(Restrictions.eq("status", STATUS.PENDING))).createCriteria("requesterInfo").add(getDnEqualityCheckConstraints(u));
 		
 		List<MembershipRemovalRequest> reqs = crit.list();
 		
 		if (reqs ==  null || reqs.isEmpty())
 			return false;
 		
 		return true;
 	}
 
 	public boolean userHasPendingRoleMembershipRequest(VOMSUser u, VOMSGroup g,
 			VOMSRole r) {
 		
 		Criteria crit = getSession().createCriteria(
 				RoleMembershipRequest.class);
 		
 		crit.add(Restrictions.eq("groupName", g.getName()));
 		crit.add(Restrictions.eq("roleName", r.getName()));
 		
 		crit.add(Restrictions.disjunction().
 				add(Restrictions.eq("status", STATUS.SUBMITTED)).
 				add(Restrictions.eq("status", STATUS.PENDING))).createCriteria("requesterInfo").add(getDnEqualityCheckConstraints(u));
 		
 		List<RoleMembershipRequest> reqs = crit.list();
 		
 		if (reqs ==  null || reqs.isEmpty())
 			return false;
 		
 		
 		return true;
 	}
 
 	
 	
 
 	
 
 }
