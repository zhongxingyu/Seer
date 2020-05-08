 package org.cagrid.gridgrouper.service.wsrf;
 
 import gov.nih.nci.cagrid.metadata.security.ServiceSecurityMetadata;
 import org.cagrid.core.common.JAXBUtils;
 import org.cagrid.gaards.authentication.WebServiceCallerId;
 import org.cagrid.gaards.security.servicesecurity.GetServiceSecurityMetadataRequest;
 import org.cagrid.gaards.security.servicesecurity.GetServiceSecurityMetadataResponse;
 import org.cagrid.gridgrouper.model.GroupDescriptor;
 import org.cagrid.gridgrouper.model.GroupPrivilege;
 import org.cagrid.gridgrouper.model.MemberDescriptor;
 import org.cagrid.gridgrouper.model.MembershipDescriptor;
 import org.cagrid.gridgrouper.model.MembershipRequestDescriptor;
 import org.cagrid.gridgrouper.model.StemDescriptor;
 import org.cagrid.gridgrouper.model.StemPrivilege;
 import org.cagrid.gridgrouper.service.GridGrouperService;
 import org.cagrid.gridgrouper.service.exception.GrantPrivilegeException;
 import org.cagrid.gridgrouper.service.exception.GridGrouperRuntimeException;
 import org.cagrid.gridgrouper.service.exception.GroupAddException;
 import org.cagrid.gridgrouper.service.exception.GroupDeleteException;
 import org.cagrid.gridgrouper.service.exception.GroupModifyException;
 import org.cagrid.gridgrouper.service.exception.GroupNotFoundException;
 import org.cagrid.gridgrouper.service.exception.InsufficientPrivilegeException;
 import org.cagrid.gridgrouper.service.exception.MemberAddException;
 import org.cagrid.gridgrouper.service.exception.MemberDeleteException;
 import org.cagrid.gridgrouper.service.exception.RevokePrivilegeException;
 import org.cagrid.gridgrouper.service.exception.SchemaException;
 import org.cagrid.gridgrouper.service.exception.StemAddException;
 import org.cagrid.gridgrouper.service.exception.StemDeleteException;
 import org.cagrid.gridgrouper.service.exception.StemModifyException;
 import org.cagrid.gridgrouper.service.exception.StemNotFoundException;
 import org.cagrid.gridgrouper.wsrf.stubs.AddChildGroupRequest;
 import org.cagrid.gridgrouper.wsrf.stubs.AddChildGroupResponse;
 import org.cagrid.gridgrouper.wsrf.stubs.AddChildStemRequest;
 import org.cagrid.gridgrouper.wsrf.stubs.AddChildStemResponse;
 import org.cagrid.gridgrouper.wsrf.stubs.AddCompositeMemberRequest;
 import org.cagrid.gridgrouper.wsrf.stubs.AddCompositeMemberResponse;
 import org.cagrid.gridgrouper.wsrf.stubs.AddMemberRequest;
 import org.cagrid.gridgrouper.wsrf.stubs.AddMemberResponse;
 import org.cagrid.gridgrouper.wsrf.stubs.AddMembershipRequestRequest;
 import org.cagrid.gridgrouper.wsrf.stubs.AddMembershipRequestResponse;
 import org.cagrid.gridgrouper.wsrf.stubs.DeleteCompositeMemberRequest;
 import org.cagrid.gridgrouper.wsrf.stubs.DeleteCompositeMemberResponse;
 import org.cagrid.gridgrouper.wsrf.stubs.DeleteGroupRequest;
 import org.cagrid.gridgrouper.wsrf.stubs.DeleteGroupResponse;
 import org.cagrid.gridgrouper.wsrf.stubs.DeleteMemberRequest;
 import org.cagrid.gridgrouper.wsrf.stubs.DeleteMemberResponse;
 import org.cagrid.gridgrouper.wsrf.stubs.DeleteStemRequest;
 import org.cagrid.gridgrouper.wsrf.stubs.DeleteStemResponse;
 import org.cagrid.gridgrouper.wsrf.stubs.DisableMembershipRequestsRequest;
 import org.cagrid.gridgrouper.wsrf.stubs.DisableMembershipRequestsResponse;
 import org.cagrid.gridgrouper.wsrf.stubs.EnableMembershipRequestsRequest;
 import org.cagrid.gridgrouper.wsrf.stubs.EnableMembershipRequestsResponse;
 import org.cagrid.gridgrouper.wsrf.stubs.GetChildGroupsRequest;
 import org.cagrid.gridgrouper.wsrf.stubs.GetChildGroupsResponse;
 import org.cagrid.gridgrouper.wsrf.stubs.GetChildStemsRequest;
 import org.cagrid.gridgrouper.wsrf.stubs.GetChildStemsResponse;
 import org.cagrid.gridgrouper.wsrf.stubs.GetGroupPrivilegesRequest;
 import org.cagrid.gridgrouper.wsrf.stubs.GetGroupPrivilegesResponse;
 import org.cagrid.gridgrouper.wsrf.stubs.GetGroupRequest;
 import org.cagrid.gridgrouper.wsrf.stubs.GetGroupResponse;
 import org.cagrid.gridgrouper.wsrf.stubs.GetMemberRequest;
 import org.cagrid.gridgrouper.wsrf.stubs.GetMemberResponse;
 import org.cagrid.gridgrouper.wsrf.stubs.GetMembersGroupsRequest;
 import org.cagrid.gridgrouper.wsrf.stubs.GetMembersGroupsResponse;
 import org.cagrid.gridgrouper.wsrf.stubs.GetMembersRequest;
 import org.cagrid.gridgrouper.wsrf.stubs.GetMembersResponse;
 import org.cagrid.gridgrouper.wsrf.stubs.GetMembershipRequestsRequest;
 import org.cagrid.gridgrouper.wsrf.stubs.GetMembershipRequestsResponse;
 import org.cagrid.gridgrouper.wsrf.stubs.GetMembershipsRequest;
 import org.cagrid.gridgrouper.wsrf.stubs.GetMembershipsResponse;
 import org.cagrid.gridgrouper.wsrf.stubs.GetParentStemRequest;
 import org.cagrid.gridgrouper.wsrf.stubs.GetParentStemResponse;
 import org.cagrid.gridgrouper.wsrf.stubs.GetStemPrivilegesRequest;
 import org.cagrid.gridgrouper.wsrf.stubs.GetStemPrivilegesResponse;
 import org.cagrid.gridgrouper.wsrf.stubs.GetStemRequest;
 import org.cagrid.gridgrouper.wsrf.stubs.GetStemResponse;
 import org.cagrid.gridgrouper.wsrf.stubs.GetSubjectsWithGroupPrivilegeRequest;
 import org.cagrid.gridgrouper.wsrf.stubs.GetSubjectsWithGroupPrivilegeResponse;
 import org.cagrid.gridgrouper.wsrf.stubs.GetSubjectsWithStemPrivilegeRequest;
 import org.cagrid.gridgrouper.wsrf.stubs.GetSubjectsWithStemPrivilegeResponse;
 import org.cagrid.gridgrouper.wsrf.stubs.GrantGroupPrivilegeRequest;
 import org.cagrid.gridgrouper.wsrf.stubs.GrantGroupPrivilegeResponse;
 import org.cagrid.gridgrouper.wsrf.stubs.GrantPrivilegeFaultFaultMessage;
 import org.cagrid.gridgrouper.wsrf.stubs.GrantStemPrivilegeRequest;
 import org.cagrid.gridgrouper.wsrf.stubs.GrantStemPrivilegeResponse;
 import org.cagrid.gridgrouper.wsrf.stubs.GridGrouperPortType;
 import org.cagrid.gridgrouper.wsrf.stubs.GridGrouperRuntimeFaultFaultMessage;
 import org.cagrid.gridgrouper.wsrf.stubs.GroupAddFaultFaultMessage;
 import org.cagrid.gridgrouper.wsrf.stubs.GroupDeleteFaultFaultMessage;
 import org.cagrid.gridgrouper.wsrf.stubs.GroupModifyFaultFaultMessage;
 import org.cagrid.gridgrouper.wsrf.stubs.GroupNotFoundFaultFaultMessage;
 import org.cagrid.gridgrouper.wsrf.stubs.HasGroupPrivilegeRequest;
 import org.cagrid.gridgrouper.wsrf.stubs.HasGroupPrivilegeResponse;
 import org.cagrid.gridgrouper.wsrf.stubs.HasStemPrivilegeRequest;
 import org.cagrid.gridgrouper.wsrf.stubs.HasStemPrivilegeResponse;
 import org.cagrid.gridgrouper.wsrf.stubs.InsufficientPrivilegeFaultFaultMessage;
 import org.cagrid.gridgrouper.wsrf.stubs.IsMemberOfRequest;
 import org.cagrid.gridgrouper.wsrf.stubs.IsMemberOfResponse;
 import org.cagrid.gridgrouper.wsrf.stubs.IsMemberRequest;
 import org.cagrid.gridgrouper.wsrf.stubs.IsMemberResponse;
 import org.cagrid.gridgrouper.wsrf.stubs.IsMembershipRequestEnabledRequest;
 import org.cagrid.gridgrouper.wsrf.stubs.IsMembershipRequestEnabledResponse;
 import org.cagrid.gridgrouper.wsrf.stubs.MemberAddFaultFaultMessage;
 import org.cagrid.gridgrouper.wsrf.stubs.MemberDeleteFaultFaultMessage;
 import org.cagrid.gridgrouper.wsrf.stubs.RevokeGroupPrivilegeRequest;
 import org.cagrid.gridgrouper.wsrf.stubs.RevokeGroupPrivilegeResponse;
 import org.cagrid.gridgrouper.wsrf.stubs.RevokePrivilegeFaultFaultMessage;
 import org.cagrid.gridgrouper.wsrf.stubs.RevokeStemPrivilegeRequest;
 import org.cagrid.gridgrouper.wsrf.stubs.RevokeStemPrivilegeResponse;
 import org.cagrid.gridgrouper.wsrf.stubs.SchemaFaultFaultMessage;
 import org.cagrid.gridgrouper.wsrf.stubs.StemAddFaultFaultMessage;
 import org.cagrid.gridgrouper.wsrf.stubs.StemDeleteFaultFaultMessage;
 import org.cagrid.gridgrouper.wsrf.stubs.StemModifyFaultFaultMessage;
 import org.cagrid.gridgrouper.wsrf.stubs.StemNotFoundFaultFaultMessage;
 import org.cagrid.gridgrouper.wsrf.stubs.UpdateGroupRequest;
 import org.cagrid.gridgrouper.wsrf.stubs.UpdateGroupResponse;
 import org.cagrid.gridgrouper.wsrf.stubs.UpdateMembershipRequestRequest;
 import org.cagrid.gridgrouper.wsrf.stubs.UpdateMembershipRequestResponse;
 import org.cagrid.gridgrouper.wsrf.stubs.UpdateStemRequest;
 import org.cagrid.gridgrouper.wsrf.stubs.UpdateStemResponse;
 import org.cagrid.wsrf.properties.InvalidResourceKeyException;
 import org.cagrid.wsrf.properties.NoSuchResourceException;
 import org.cagrid.wsrf.properties.Resource;
 import org.cagrid.wsrf.properties.ResourceException;
 import org.cagrid.wsrf.properties.ResourceHome;
 import org.cagrid.wsrf.properties.ResourceProperty;
 import org.cagrid.wsrf.properties.ResourcePropertySet;
 import org.oasis_open.docs.wsrf._2004._06.wsrf_ws_resourceproperties_1_2_draft_01.GetMultipleResourceProperties;
 import org.oasis_open.docs.wsrf._2004._06.wsrf_ws_resourceproperties_1_2_draft_01.GetMultipleResourcePropertiesResponse;
 import org.oasis_open.docs.wsrf._2004._06.wsrf_ws_resourceproperties_1_2_draft_01.GetResourcePropertyResponse;
 import org.oasis_open.docs.wsrf._2004._06.wsrf_ws_resourceproperties_1_2_draft_01.QueryResourceProperties;
 import org.oasis_open.docs.wsrf._2004._06.wsrf_ws_resourceproperties_1_2_draft_01.QueryResourcePropertiesResponse;
 import org.oasis_open.docs.wsrf._2004._06.wsrf_ws_resourceproperties_1_2_draft_01_wsdl.InvalidQueryExpressionFault;
 import org.oasis_open.docs.wsrf._2004._06.wsrf_ws_resourceproperties_1_2_draft_01_wsdl.InvalidResourcePropertyQNameFault;
 import org.oasis_open.docs.wsrf._2004._06.wsrf_ws_resourceproperties_1_2_draft_01_wsdl.QueryEvaluationErrorFault;
 import org.oasis_open.docs.wsrf._2004._06.wsrf_ws_resourceproperties_1_2_draft_01_wsdl.ResourceUnknownFault;
 import org.oasis_open.docs.wsrf._2004._06.wsrf_ws_resourceproperties_1_2_draft_01_wsdl.UnknownQueryExpressionDialectFault;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.w3c.dom.Node;
 
 import javax.xml.bind.JAXBElement;
 import javax.xml.namespace.QName;
 import javax.xml.ws.WebServiceContext;
 
 import java.util.Iterator;
 import java.util.List;
 
 public class GridGrouperWSRFImpl implements GridGrouperPortType {
 
     private GridGrouperService gridGrouper;
     private final Logger logger;
 
     @javax.annotation.Resource
     private WebServiceContext wsContext;
 
     private final ResourceHome resourceHome;
 
     public GridGrouperWSRFImpl(GridGrouperService service) {
         this.logger = LoggerFactory.getLogger(getClass());
         this.gridGrouper = service;
         this.resourceHome = service.getResourceHome();
     }
 
     @Override
     public GetMembershipsResponse getMemberships(GetMembershipsRequest parameters) throws GroupNotFoundFaultFaultMessage, GridGrouperRuntimeFaultFaultMessage {
         String message = "getMemberships";
         try {
             List<MembershipDescriptor> memberships = gridGrouper.getMemberships(getCallerId(), parameters.getGroup().getGroupIdentifier(), parameters.getFilter().getMemberFilter());
             GetMembershipsResponse response = new GetMembershipsResponse();
             response.getMembershipDescriptor().addAll(memberships);
             return response;
         } catch (GridGrouperRuntimeException e) {
             throw new GridGrouperRuntimeFaultFaultMessage(message + ":" + e.getMessage(), e.getFault());
         } catch (GroupNotFoundException e) {
             throw new GroupNotFoundFaultFaultMessage(message + ":" + e.getMessage(), e.getFault());
         }
     }
 
     @Override
     public GetSubjectsWithStemPrivilegeResponse getSubjectsWithStemPrivilege(GetSubjectsWithStemPrivilegeRequest parameters) throws GridGrouperRuntimeFaultFaultMessage, StemNotFoundFaultFaultMessage {
         String message = "getSubjectsWithStemPrivilege";
         try {
             List<String> subjects = gridGrouper.getSubjectsWithStemPrivilege(getCallerId(), parameters.getStem().getStemIdentifier(), parameters.getPrivilege().getStemPrivilegeType());
             GetSubjectsWithStemPrivilegeResponse response = new GetSubjectsWithStemPrivilegeResponse();
             response.getSubjectIdentifier().addAll(subjects);
             return response;
         } catch (GridGrouperRuntimeException e) {
             throw new GridGrouperRuntimeFaultFaultMessage(message + ":" + e.getMessage(), e.getFault());
         } catch (StemNotFoundException e) {
             throw new StemNotFoundFaultFaultMessage(message + ":" + e.getMessage(), e.getFault());
         }
     }
 
     @Override
     public AddMemberResponse addMember(AddMemberRequest parameters) throws InsufficientPrivilegeFaultFaultMessage, GroupNotFoundFaultFaultMessage, GridGrouperRuntimeFaultFaultMessage, MemberAddFaultFaultMessage {
         String message = "addMember";
         try {
             gridGrouper.addMember(getCallerId(), parameters.getGroup().getGroupIdentifier(), parameters.getSubject().getSubjectIdentifier());
             return new AddMemberResponse();
         } catch (InsufficientPrivilegeException e) {
             throw new InsufficientPrivilegeFaultFaultMessage(message + ":" + e.getMessage(), e.getFault());
         } catch (MemberAddException e) {
             throw new MemberAddFaultFaultMessage(message + ":" + e.getMessage(), e.getFault());
         } catch (GroupNotFoundException e) {
             throw new GroupNotFoundFaultFaultMessage(message + ":" + e.getMessage(), e.getFault());
         } catch (GridGrouperRuntimeException e) {
             throw new GridGrouperRuntimeFaultFaultMessage(message + ":" + e.getMessage(), e.getFault());
         }
     }
 
     @Override
     public GetServiceSecurityMetadataResponse getServiceSecurityMetadata(GetServiceSecurityMetadataRequest parameters) {
         ServiceSecurityMetadata serviceSecurityMetadata = gridGrouper.getServiceSecurityMetadata();
         GetServiceSecurityMetadataResponse response = new GetServiceSecurityMetadataResponse();
         response.setServiceSecurityMetadata(serviceSecurityMetadata);
         return response;
     }
 
     @Override
     public IsMemberOfResponse isMemberOf(IsMemberOfRequest parameters) throws GroupNotFoundFaultFaultMessage, GridGrouperRuntimeFaultFaultMessage {
         String message = "isMemberOf";
         try {
             boolean result = gridGrouper.isMemberOf(getCallerId(), parameters.getGroup().getGroupIdentifier(), parameters.getMember().getSubjectIdentifier(), parameters.getFilter().getMemberFilter());
             IsMemberOfResponse response = new IsMemberOfResponse();
             response.setResponse(result);
             return response;
         } catch (GridGrouperRuntimeException e) {
             throw new GridGrouperRuntimeFaultFaultMessage(message + ":" + e.getMessage(), e.getFault());
         } catch (GroupNotFoundException e) {
             throw new GroupNotFoundFaultFaultMessage(message + ":" + e.getMessage(), e.getFault());
         }
     }
 
     @Override
     public RevokeGroupPrivilegeResponse revokeGroupPrivilege(RevokeGroupPrivilegeRequest parameters) throws InsufficientPrivilegeFaultFaultMessage, GroupNotFoundFaultFaultMessage, GridGrouperRuntimeFaultFaultMessage, RevokePrivilegeFaultFaultMessage, SchemaFaultFaultMessage {
         String message = "revokeGroupPrivilege";
         try {
             gridGrouper.revokeGroupPrivilege(getCallerId(), parameters.getGroup().getGroupIdentifier(), parameters.getSubject().getSubjectIdentifier(), parameters.getPrivilege().getGroupPrivilegeType());
             return new RevokeGroupPrivilegeResponse();
         } catch (GroupNotFoundException e) {
             throw new GroupNotFoundFaultFaultMessage(message + ":" + e.getMessage(), e.getFault());
         } catch (RevokePrivilegeException e) {
             throw new RevokePrivilegeFaultFaultMessage(message + ":" + e.getMessage(), e.getFault());
         } catch (SchemaException e) {
             throw new SchemaFaultFaultMessage(message + ":" + e.getMessage(), e.getFault());
         } catch (InsufficientPrivilegeException e) {
             throw new InsufficientPrivilegeFaultFaultMessage(message + ":" + e.getMessage(), e.getFault());
         } catch (GridGrouperRuntimeException e) {
             throw new GridGrouperRuntimeFaultFaultMessage(message + ":" + e.getMessage(), e.getFault());
         }
     }
 
     @Override
     public UpdateStemResponse updateStem(UpdateStemRequest parameters) throws InsufficientPrivilegeFaultFaultMessage, GridGrouperRuntimeFaultFaultMessage, StemModifyFaultFaultMessage {
         String message = "updateStem";
         try {
             StemDescriptor desc = gridGrouper.updateStem(getCallerId(), parameters.getStem().getStemIdentifier(), parameters.getUpdate().getStemUpdate());
             UpdateStemResponse response = new UpdateStemResponse();
             response.setStemDescriptor(desc);
             return response;
         } catch (StemModifyException e) {
             throw new StemModifyFaultFaultMessage(message + ":" + e.getMessage(), e.getFault());
         } catch (GridGrouperRuntimeException e) {
             throw new GridGrouperRuntimeFaultFaultMessage(message + ":" + e.getMessage(), e.getFault());
         } catch (InsufficientPrivilegeException e) {
             throw new InsufficientPrivilegeFaultFaultMessage(message + ":" + e.getMessage(), e.getFault());
         }
     }
 
     @Override
     public GrantGroupPrivilegeResponse grantGroupPrivilege(GrantGroupPrivilegeRequest parameters) throws GrantPrivilegeFaultFaultMessage, InsufficientPrivilegeFaultFaultMessage, GroupNotFoundFaultFaultMessage, GridGrouperRuntimeFaultFaultMessage {
         String message = "grantGroupPrivilege";
         try {
             gridGrouper.grantGroupPrivilege(getCallerId(), parameters.getGroup().getGroupIdentifier(), parameters.getSubject().getSubjectIdentifier(), parameters.getPrivilege().getGroupPrivilegeType());
             return new GrantGroupPrivilegeResponse();
         } catch (GroupNotFoundException e) {
             throw new GroupNotFoundFaultFaultMessage(message + ":" + e.getMessage(), e.getFault());
         } catch (GrantPrivilegeException e) {
             throw new GrantPrivilegeFaultFaultMessage(message + ":" + e.getMessage(), e.getFault());
         } catch (InsufficientPrivilegeException e) {
             throw new InsufficientPrivilegeFaultFaultMessage(message + ":" + e.getMessage(), e.getFault());
         } catch (GridGrouperRuntimeException e) {
             throw new GridGrouperRuntimeFaultFaultMessage(message + ":" + e.getMessage(), e.getFault());
         }
     }
 
     @Override
     public GetMemberResponse getMember(GetMemberRequest parameters) throws InsufficientPrivilegeFaultFaultMessage, GridGrouperRuntimeFaultFaultMessage {
         String message = "getMember";
         try {
             MemberDescriptor member = gridGrouper.getMember(getCallerId(), parameters.getMember().getSubjectIdentifier());
             GetMemberResponse response = new GetMemberResponse();
             response.setMemberDescriptor(member);
             return response;
         } catch (InsufficientPrivilegeException e) {
             throw new InsufficientPrivilegeFaultFaultMessage(message + ":" + e.getMessage(), e.getFault());
         } catch (GridGrouperRuntimeException e) {
             throw new GridGrouperRuntimeFaultFaultMessage(message + ":" + e.getMessage(), e.getFault());
         }
     }
 
     @Override
     public HasStemPrivilegeResponse hasStemPrivilege(HasStemPrivilegeRequest parameters) throws GridGrouperRuntimeFaultFaultMessage, StemNotFoundFaultFaultMessage {
         String message = "hasStemPrivilege";
         try {
             boolean result = gridGrouper.hasStemPrivilege(getCallerId(), parameters.getStem().getStemIdentifier(), parameters.getSubject().getSubjectIdentifier(), parameters.getPrivilege().getStemPrivilegeType());
             HasStemPrivilegeResponse response = new HasStemPrivilegeResponse();
             response.setResponse(result);
             return response;
         } catch (StemNotFoundException e) {
             throw new StemNotFoundFaultFaultMessage(message + ":" + e.getMessage(), e.getFault());
         } catch (GridGrouperRuntimeException e) {
             throw new GridGrouperRuntimeFaultFaultMessage(message + ":" + e.getMessage(), e.getFault());
         }
     }
 
     @Override
     public GetMembershipRequestsResponse getMembershipRequests(GetMembershipRequestsRequest parameters) {
         String message = "getMembershipRequests";
         try {
             List<MembershipRequestDescriptor> memberships = gridGrouper.getMembershipRequests(getCallerId(), parameters.getGroup().getGroupIdentifier(), parameters.getStatus().getMembershipRequestStatus());
             GetMembershipRequestsResponse response = new GetMembershipRequestsResponse();
             response.getMembershipRequestDescriptor().addAll(memberships);
             return response;
         } catch (Exception e) {
             throw new RuntimeException(message + ":" + e.getMessage(), e);
         }
     }
 
     @Override
     public IsMemberResponse isMember(IsMemberRequest parameters) throws GridGrouperRuntimeFaultFaultMessage {
         String message = "isMember";
         try {
             boolean result = gridGrouper.isMember(getCallerId(), parameters.getMember(), parameters.getExpression().getMembershipExpression());
             IsMemberResponse response = new IsMemberResponse();
             response.setResponse(result);
             return response;
         } catch (GridGrouperRuntimeException e) {
             throw new GridGrouperRuntimeFaultFaultMessage(message + ":" + e.getMessage(), e.getFault());
         }
     }
 
     @Override
     public GetChildStemsResponse getChildStems(GetChildStemsRequest parameters) throws GridGrouperRuntimeFaultFaultMessage, StemNotFoundFaultFaultMessage {
         String message = "getChildStems";
         try {
             List<StemDescriptor> stems = gridGrouper.getChildStems(getCallerId(), parameters.getParentStem().getStemIdentifier());
             GetChildStemsResponse response = new GetChildStemsResponse();
             response.getStemDescriptor().addAll(stems);
             return response;
         } catch (GridGrouperRuntimeException e) {
             throw new GridGrouperRuntimeFaultFaultMessage(message + ":" + e.getMessage(), e.getFault());
         } catch (StemNotFoundException e) {
             throw new StemNotFoundFaultFaultMessage(message + ":" + e.getMessage(), e.getFault());
         }
     }
 
     @Override
     public AddChildGroupResponse addChildGroup(AddChildGroupRequest parameters) throws GroupAddFaultFaultMessage, InsufficientPrivilegeFaultFaultMessage, GridGrouperRuntimeFaultFaultMessage {
         String message = "addChildGroup";
         try {
             GroupDescriptor desc = gridGrouper.addChildGroup(getCallerId(), parameters.getStem().getStemIdentifier(), parameters.getExtension(), parameters.getDisplayExtension());
             AddChildGroupResponse response = new AddChildGroupResponse();
             response.setGroupDescriptor(desc);
             return response;
         } catch (GroupAddException e) {
             throw new GroupAddFaultFaultMessage(message + ":" + e.getMessage(), e.getFault());
         } catch (InsufficientPrivilegeException e) {
             throw new InsufficientPrivilegeFaultFaultMessage(message + ":" + e.getMessage(), e.getFault());
         } catch (StemNotFoundException e) {
             throw new GridGrouperRuntimeFaultFaultMessage(message + ":" + e.getMessage(), e);
         } catch (GridGrouperRuntimeException e) {
             throw new GridGrouperRuntimeFaultFaultMessage(message + ":" + e.getMessage(), e.getFault());
         }
     }
 
     @Override
     public GetMembersResponse getMembers(GetMembersRequest parameters) throws GroupNotFoundFaultFaultMessage, GridGrouperRuntimeFaultFaultMessage {
         String message = "getMembers";
         try {
             List<MemberDescriptor> members = gridGrouper.getMembers(getCallerId(), parameters.getGroup().getGroupIdentifier(), parameters.getFilter().getMemberFilter());
             GetMembersResponse response = new GetMembersResponse();
             response.getMemberDescriptor().addAll(members);
             return response;
         } catch (GridGrouperRuntimeException e) {
             throw new GridGrouperRuntimeFaultFaultMessage(message + ":" + e.getMessage(), e.getFault());
         } catch (GroupNotFoundException e) {
             throw new GroupNotFoundFaultFaultMessage(message + ":" + e.getMessage(), e.getFault());
         }
     }
 
     @Override
     public GetStemResponse getStem(GetStemRequest parameters) throws GridGrouperRuntimeFaultFaultMessage, StemNotFoundFaultFaultMessage {
         String message = "getStem";
         try {
             StemDescriptor desc = gridGrouper.getStem(getCallerId(), parameters.getStem().getStemIdentifier());
             GetStemResponse response = new GetStemResponse();
             response.setStemDescriptor(desc);
             return response;
         } catch (StemNotFoundException e) {
             throw new GridGrouperRuntimeFaultFaultMessage(message + ":" + e.getMessage(), e);
         } catch (GridGrouperRuntimeException e) {
             throw new GridGrouperRuntimeFaultFaultMessage(message + ":" + e.getMessage(), e.getFault());
         }
     }
 
     @Override
     public UpdateGroupResponse updateGroup(UpdateGroupRequest parameters) throws GroupModifyFaultFaultMessage, InsufficientPrivilegeFaultFaultMessage, GroupNotFoundFaultFaultMessage, GridGrouperRuntimeFaultFaultMessage {
         String message = "updateGroup";
         try{
             GroupDescriptor desc = gridGrouper.updateGroup(getCallerId(), parameters.getGroup().getGroupIdentifier(), parameters.getUpdate().getGroupUpdate());
             UpdateGroupResponse response = new UpdateGroupResponse();
             response.setGroupDescriptor(desc);
             return response;
         } catch (InsufficientPrivilegeException e) {
             throw new InsufficientPrivilegeFaultFaultMessage(message + ":" + e.getMessage(), e.getFault());
         } catch (GridGrouperRuntimeException e) {
             throw new GridGrouperRuntimeFaultFaultMessage(message + ":" + e.getMessage(), e.getFault());
         } catch (GroupModifyException e) {
             throw new GroupModifyFaultFaultMessage(message + ":" + e.getMessage(), e.getFault());
         } catch (GroupNotFoundException e) {
             throw new GroupNotFoundFaultFaultMessage(message + ":" + e.getMessage(), e.getFault());
         }
     }
 
     @Override
     public DeleteCompositeMemberResponse deleteCompositeMember(DeleteCompositeMemberRequest parameters) throws InsufficientPrivilegeFaultFaultMessage, GroupNotFoundFaultFaultMessage, GridGrouperRuntimeFaultFaultMessage, MemberDeleteFaultFaultMessage {
         String message = "deleteCompositeMember";
         try{
             GroupDescriptor desc = gridGrouper.deleteCompositeMember(getCallerId(), parameters.getGroup().getGroupIdentifier());
             DeleteCompositeMemberResponse response = new DeleteCompositeMemberResponse();
             response.setGroupDescriptor(desc);
             return response;
         } catch (InsufficientPrivilegeException e) {
             throw new InsufficientPrivilegeFaultFaultMessage(message + ":" + e.getMessage(), e.getFault());
         } catch (GridGrouperRuntimeException e) {
             throw new GridGrouperRuntimeFaultFaultMessage(message + ":" + e.getMessage(), e.getFault());
         } catch (GroupNotFoundException e) {
             throw new GroupNotFoundFaultFaultMessage(message + ":" + e.getMessage(), e.getFault());
         } catch (MemberDeleteException e) {
             throw new MemberDeleteFaultFaultMessage(message + ":" + e.getMessage(), e.getFault());
         }
     }
 
     @Override
     public DeleteStemResponse deleteStem(DeleteStemRequest parameters) throws InsufficientPrivilegeFaultFaultMessage, GridGrouperRuntimeFaultFaultMessage, StemNotFoundFaultFaultMessage, StemDeleteFaultFaultMessage {
         String message = "deleteStem";
         try{
             gridGrouper.deleteStem(getCallerId(), parameters.getStem().getStemIdentifier());
             return new DeleteStemResponse();
         } catch (InsufficientPrivilegeException e) {
             throw new InsufficientPrivilegeFaultFaultMessage(message + ":" + e.getMessage(), e.getFault());
         } catch (GridGrouperRuntimeException e) {
             throw new GridGrouperRuntimeFaultFaultMessage(message + ":" + e.getMessage(), e.getFault());
         } catch (StemDeleteException e) {
             throw new StemDeleteFaultFaultMessage(message + ":" + e.getMessage(), e.getFault());
         } catch (StemNotFoundException e) {
             throw new StemNotFoundFaultFaultMessage(message + ":" + e.getMessage(), e.getFault());
         }
     }
 
     @Override
     public GetParentStemResponse getParentStem(GetParentStemRequest parameters) throws GridGrouperRuntimeFaultFaultMessage, StemNotFoundFaultFaultMessage {
         String message = "getParentStem";
         try{
             StemDescriptor desc = gridGrouper.getParentStem(getCallerId(), parameters.getChildStem().getStemIdentifier());
             GetParentStemResponse response = new GetParentStemResponse();
             response.setStemDescriptor(desc);
             return response;
         } catch (GridGrouperRuntimeException e) {
             throw new GridGrouperRuntimeFaultFaultMessage(message + ":" + e.getMessage(), e.getFault());
         } catch (StemNotFoundException e) {
             throw new StemNotFoundFaultFaultMessage(message + ":" + e.getMessage(), e.getFault());
         }
     }
 
     @Override
     public GetResourcePropertyResponse getResourceProperty(QName resourcePropertyQName) throws ResourceUnknownFault, InvalidResourcePropertyQNameFault {
         Exception e = null;
         GetResourcePropertyResponse response = null;
         try {
             Resource resource = resourceHome.find(null);
             if (resource instanceof ResourcePropertySet) {
                 ResourcePropertySet resourcePropertySet = (ResourcePropertySet) resource;
                 ResourceProperty<?> resourceProperty = resourcePropertySet
                         .get(resourcePropertyQName);
                 if (resourceProperty != null) {
                     Object resourcePropertyValue = resourceProperty.get(0);
                     logger.info("getResourceProperty " + resourcePropertyQName
                             + " returning " + resourcePropertyValue);
                     if (!(resourcePropertyValue instanceof Node) && !(resourcePropertyValue instanceof JAXBElement<?>)) {
                         resourcePropertyValue = JAXBUtils
                                 .wrap(resourcePropertyValue);
                     }
                     response = new GetResourcePropertyResponse();
                     response.getAny().add(resourcePropertyValue);
                 }
             }
         } catch (NoSuchResourceException nsre) {
             e = nsre;
         } catch (InvalidResourceKeyException irke) {
             e = irke;
         } catch (ResourceException re) {
             e = re;
         }
         if ((response == null) || (e != null)) {
             throw new ResourceUnknownFault("No resource for '"
                     + resourcePropertyQName + "'", e);
         }
         return response;
     }
 
     @Override
     public GetStemPrivilegesResponse getStemPrivileges(GetStemPrivilegesRequest parameters) throws GridGrouperRuntimeFaultFaultMessage, StemNotFoundFaultFaultMessage {
         String message = "getStemPrivileges";
         try{
             List<StemPrivilege> privs = gridGrouper.getStemPrivileges(getCallerId(), parameters.getStem().getStemIdentifier(), parameters.getSubject().getSubjectIdentifier());
             GetStemPrivilegesResponse response = new GetStemPrivilegesResponse();
             response.getStemPrivilege().addAll(privs);
             return response;
         } catch (GridGrouperRuntimeException e) {
             throw new GridGrouperRuntimeFaultFaultMessage(message + ":" + e.getMessage(), e.getFault());
         } catch (StemNotFoundException e) {
             throw new StemNotFoundFaultFaultMessage(message + ":" + e.getMessage(), e.getFault());
         }
     }
 
     @Override
     public GetMembersGroupsResponse getMembersGroups(GetMembersGroupsRequest parameters) throws InsufficientPrivilegeFaultFaultMessage, GridGrouperRuntimeFaultFaultMessage {
         String message = "getMembersGroups";
         try{
             List<GroupDescriptor> groups = gridGrouper.getMembersGroups(getCallerId(), parameters.getMember().getSubjectIdentifier(), parameters.getType().getMembershipType());
             GetMembersGroupsResponse response = new GetMembersGroupsResponse();
             response.getGroupDescriptor().addAll(groups);
             return response;
         } catch (GridGrouperRuntimeException e) {
             throw new GridGrouperRuntimeFaultFaultMessage(message + ":" + e.getMessage(), e.getFault());
         } catch (InsufficientPrivilegeException e) {
             throw new InsufficientPrivilegeFaultFaultMessage(message + ":" + e.getMessage(), e.getFault());
         }
     }
 
     @Override
     public AddMembershipRequestResponse addMembershipRequest(AddMembershipRequestRequest parameters) {
         String message = "addMembershipRequest";
         try{
             gridGrouper.addMembershipRequest(getCallerId(), parameters.getGroup().getGroupIdentifier());
             return new AddMembershipRequestResponse();
         } catch (Exception e) {
             throw new RuntimeException(message + ":" + e.getMessage(), e);
         }
     }
 
     @Override
     public GetSubjectsWithGroupPrivilegeResponse getSubjectsWithGroupPrivilege(GetSubjectsWithGroupPrivilegeRequest parameters) throws GroupNotFoundFaultFaultMessage, GridGrouperRuntimeFaultFaultMessage {
         String message = "getSubjectsWithGroupPrivilege";
         try {
             List<String> subjects = gridGrouper.getSubjectsWithGroupPrivilege(getCallerId(), parameters.getGroup().getGroupIdentifier(), parameters.getPrivilege().getGroupPrivilegeType());
             GetSubjectsWithGroupPrivilegeResponse response = new GetSubjectsWithGroupPrivilegeResponse();
             response.getSubjectIdentifier().addAll(subjects);
             return response;
         } catch (GroupNotFoundException e) {
             throw new GroupNotFoundFaultFaultMessage(message + ":" + e.getMessage(), e.getFault());
         } catch (GridGrouperRuntimeException e) {
             throw new GridGrouperRuntimeFaultFaultMessage(message + ":" + e.getMessage(), e.getFault());
         }
     }
 
     @Override
     public GetGroupPrivilegesResponse getGroupPrivileges(GetGroupPrivilegesRequest parameters) throws GroupNotFoundFaultFaultMessage, GridGrouperRuntimeFaultFaultMessage {
         String message = "getGroupPrivileges";
         try {
             List<GroupPrivilege> privs = gridGrouper.getGroupPrivileges(getCallerId(), parameters.getGroup().getGroupIdentifier(), parameters.getSubject().getSubjectIdentifier());
             GetGroupPrivilegesResponse response = new GetGroupPrivilegesResponse();
             response.getGroupPrivilege().addAll(privs);
             return response;
         } catch (GroupNotFoundException e) {
             throw new GroupNotFoundFaultFaultMessage(message + ":" + e.getMessage(), e.getFault());
         } catch (GridGrouperRuntimeException e) {
             throw new GridGrouperRuntimeFaultFaultMessage(message + ":" + e.getMessage(), e.getFault());
         }
     }
 
     @Override
     public QueryResourcePropertiesResponse queryResourceProperties(QueryResourceProperties queryResourcePropertiesRequest) throws ResourceUnknownFault, InvalidQueryExpressionFault, QueryEvaluationErrorFault, InvalidResourcePropertyQNameFault, UnknownQueryExpressionDialectFault {
         // TODO
         QueryResourcePropertiesResponse response = null;
         response = new QueryResourcePropertiesResponse();
         return response;
     }
 
     @Override
     public DeleteMemberResponse deleteMember(DeleteMemberRequest parameters) throws InsufficientPrivilegeFaultFaultMessage, GroupNotFoundFaultFaultMessage, GridGrouperRuntimeFaultFaultMessage, MemberDeleteFaultFaultMessage {
         String message = "deleteMember";
         try {
             gridGrouper.deleteMember(getCallerId(), parameters.getGroup().getGroupIdentifier(), parameters.getMember().getSubjectIdentifier());
             return new DeleteMemberResponse();
         } catch (InsufficientPrivilegeException e) {
             throw new InsufficientPrivilegeFaultFaultMessage(message + ":" + e.getMessage(), e.getFault());
         } catch (GroupNotFoundException e) {
             throw new GroupNotFoundFaultFaultMessage(message + ":" + e.getMessage(), e.getFault());
         } catch (MemberDeleteException e) {
             throw new MemberDeleteFaultFaultMessage(message + ":" + e.getMessage(), e.getFault());
         } catch (GridGrouperRuntimeException e) {
             throw new GridGrouperRuntimeFaultFaultMessage(message + ":" + e.getMessage(), e.getFault());
         }
     }
 
     @Override
     public GrantStemPrivilegeResponse grantStemPrivilege(GrantStemPrivilegeRequest parameters) throws GrantPrivilegeFaultFaultMessage, InsufficientPrivilegeFaultFaultMessage, GridGrouperRuntimeFaultFaultMessage, StemNotFoundFaultFaultMessage, SchemaFaultFaultMessage {
         String message = "grantStemPrivilege";
         try {
             gridGrouper.grantStemPrivilege(getCallerId(), parameters.getStem().getStemIdentifier(), parameters.getSubject().getSubjectIdentifier(), parameters.getPrivilege().getStemPrivilegeType());
             return new GrantStemPrivilegeResponse();
         } catch (InsufficientPrivilegeException e) {
             throw new InsufficientPrivilegeFaultFaultMessage(message + ":" + e.getMessage(), e.getFault());
         } catch (GridGrouperRuntimeException e) {
             throw new GridGrouperRuntimeFaultFaultMessage(message + ":" + e.getMessage(), e.getFault());
         } catch (GrantPrivilegeException e) {
             throw new GrantPrivilegeFaultFaultMessage(message + ":" + e.getMessage(), e.getFault());
         } catch (SchemaException e) {
             throw new SchemaFaultFaultMessage(message + ":" + e.getMessage(), e.getFault());
         } catch (StemNotFoundException e) {
             throw new StemNotFoundFaultFaultMessage(message + ":" + e.getMessage(), e.getFault());
         }
     }
 
     @Override
     public DisableMembershipRequestsResponse disableMembershipRequests(DisableMembershipRequestsRequest parameters) {
         String message = "disableMembershipRequests";
         try {
             gridGrouper.disableMembershipRequests(getCallerId(), parameters.getGroup().getGroupIdentifier());
             return new DisableMembershipRequestsResponse();
         } catch (Exception e) {
             throw new RuntimeException(message + ":" + e.getMessage(), e);
         }
     }
 
     @Override
     public EnableMembershipRequestsResponse enableMembershipRequests(EnableMembershipRequestsRequest parameters) {
         String message = "enableMembershipRequests";
         try {
             gridGrouper.enableMembershipRequests(getCallerId(), parameters.getGroup().getGroupIdentifier());
             return new EnableMembershipRequestsResponse();
         } catch (Exception e) {
             throw new RuntimeException(message + ":" + e.getMessage(), e);
         }
     }
 
     @Override
     public DeleteGroupResponse deleteGroup(DeleteGroupRequest parameters) throws InsufficientPrivilegeFaultFaultMessage, GroupNotFoundFaultFaultMessage, GridGrouperRuntimeFaultFaultMessage, GroupDeleteFaultFaultMessage {
         String message = "deleteGroup";
         try {
             gridGrouper.deleteGroup(getCallerId(), parameters.getGroup().getGroupIdentifier());
             return new DeleteGroupResponse();
         } catch (InsufficientPrivilegeException e) {
             throw new InsufficientPrivilegeFaultFaultMessage(message + ":" + e.getMessage(), e.getFault());
         } catch (GridGrouperRuntimeException e) {
             throw new GridGrouperRuntimeFaultFaultMessage(message + ":" + e.getMessage(), e.getFault());
         } catch (GroupDeleteException e) {
             throw new GroupDeleteFaultFaultMessage(message + ":" + e.getMessage(), e.getFault());
         } catch (GroupNotFoundException e) {
             throw new GroupNotFoundFaultFaultMessage(message + ":" + e.getMessage(), e.getFault());
         }
     }
 
     @Override
     public IsMembershipRequestEnabledResponse isMembershipRequestEnabled(IsMembershipRequestEnabledRequest parameters) {
         String message = "isMembershipRequestEnabled";
         try {
             boolean result = gridGrouper.isMembershipRequestEnabled(getCallerId(), parameters.getGroup().getGroupIdentifier());
             IsMembershipRequestEnabledResponse response = new IsMembershipRequestEnabledResponse();
             response.setResponse(result);
             return response;
         } catch (Exception e) {
             throw new RuntimeException(message + ":" + e.getMessage(), e);
         }
     }
 
     @Override
     public RevokeStemPrivilegeResponse revokeStemPrivilege(RevokeStemPrivilegeRequest parameters) throws InsufficientPrivilegeFaultFaultMessage, GridGrouperRuntimeFaultFaultMessage, RevokePrivilegeFaultFaultMessage, StemNotFoundFaultFaultMessage, SchemaFaultFaultMessage {
         String message = "revokeStemPrivilege";
         try {
             gridGrouper.revokeStemPrivilege(getCallerId(), parameters.getStem().getStemIdentifier(), parameters.getSubject().getSubjectIdentifier(), parameters.getPrivilege().getStemPrivilegeType());
             return new RevokeStemPrivilegeResponse();
         } catch (InsufficientPrivilegeException e) {
             throw new InsufficientPrivilegeFaultFaultMessage(message + ":" + e.getMessage(), e.getFault());
         } catch (GridGrouperRuntimeException e) {
             throw new GridGrouperRuntimeFaultFaultMessage(message + ":" + e.getMessage(), e.getFault());
         } catch (RevokePrivilegeException e) {
             throw new RevokePrivilegeFaultFaultMessage(message + ":" + e.getMessage(), e.getFault());
         } catch (SchemaException e) {
             throw new SchemaFaultFaultMessage(message + ":" + e.getMessage(), e.getFault());
         } catch (StemNotFoundException e) {
             throw new StemNotFoundFaultFaultMessage(message + ":" + e.getMessage(), e.getFault());
         }
     }
 
     @Override
     public GetMultipleResourcePropertiesResponse getMultipleResourceProperties(GetMultipleResourceProperties getMultipleResourcePropertiesRequest) throws ResourceUnknownFault, InvalidResourcePropertyQNameFault {
     	logger.info("getMultipleResourceProperty "
 				+ getMultipleResourcePropertiesRequest);
 		System.out.println(getMultipleResourcePropertiesRequest);
 		GetMultipleResourcePropertiesResponse response = new GetMultipleResourcePropertiesResponse();
 		for (Iterator iterator = getMultipleResourcePropertiesRequest
 				.getResourceProperty().iterator(); iterator.hasNext();) {
 			QName qname = (QName) iterator.next();
 			Exception e;
 			try {
 				Resource resource = resourceHome.find(null);
 				if (resource instanceof ResourcePropertySet) {
 					ResourcePropertySet resourcePropertySet = (ResourcePropertySet) resource;
 					ResourceProperty<?> resourceProperty = resourcePropertySet
 							.get(qname);
 					if (resourceProperty != null) {
 						Object resourcePropertyValue = resourceProperty.get(0);
 						logger.info("getResourceProperty " + qname
 								+ " returning " + resourcePropertyValue);
 						if (!(resourcePropertyValue instanceof Node)
 								&& !(resourcePropertyValue instanceof JAXBElement<?>)) {
 							resourcePropertyValue = JAXBUtils
 									.wrap(resourcePropertyValue);
 						}
 						response.getAny().add(resourcePropertyValue);
 					}
 				}
 			} catch (NoSuchResourceException nsre) {
 				e = nsre;
 			} catch (InvalidResourceKeyException irke) {
 				e = irke;
 			} catch (ResourceException re) {
 				e = re;
 			}
 		}
 
 		return response;
     }
 
     @Override
     public GetChildGroupsResponse getChildGroups(GetChildGroupsRequest parameters) throws GridGrouperRuntimeFaultFaultMessage, StemNotFoundFaultFaultMessage {
         String message = "getChildGroups";
         try {
             List<GroupDescriptor> groups = gridGrouper.getChildGroups(getCallerId(), parameters.getStem().getStemIdentifier());
             GetChildGroupsResponse response = new GetChildGroupsResponse();
             response.getGroupDescriptor().addAll(groups);
             return response;
         } catch (GridGrouperRuntimeException e) {
             throw new GridGrouperRuntimeFaultFaultMessage(message + ":" + e.getMessage(), e.getFault());
         } catch (StemNotFoundException e) {
             throw new StemNotFoundFaultFaultMessage(message + ":" + e.getMessage(), e.getFault());
         }
     }
 
     @Override
     public UpdateMembershipRequestResponse updateMembershipRequest(UpdateMembershipRequestRequest parameters) {
         String message = "updateMembershipRequest";
         try {
             MembershipRequestDescriptor desc = gridGrouper.updateMembershipRequest(getCallerId(), parameters.getGroup().getGroupIdentifier(), parameters.getSubject().getSubjectIdentifier(), parameters.getUpdate().getMembershipRequestUpdate());
             UpdateMembershipRequestResponse response = new UpdateMembershipRequestResponse();
             response.setMembershipRequestDescriptor(desc);
             return response;
         } catch (Exception e) {
             throw new RuntimeException(message + ":" + e.getMessage(), e);
         }
     }
 
     @Override
     public AddChildStemResponse addChildStem(AddChildStemRequest parameters) throws StemAddFaultFaultMessage, InsufficientPrivilegeFaultFaultMessage, GridGrouperRuntimeFaultFaultMessage, StemNotFoundFaultFaultMessage {
         String message = "addChildStem";
         try {
             StemDescriptor desc = gridGrouper.addChildStem(getCallerId(), parameters.getStem().getStemIdentifier(), parameters.getExtension(), parameters.getDisplayExtension());
             AddChildStemResponse response = new AddChildStemResponse();
             response.setStemDescriptor(desc);
             return response;
         } catch (InsufficientPrivilegeException e) {
             throw new InsufficientPrivilegeFaultFaultMessage(message + ":" + e.getMessage(), e.getFault());
         } catch (GridGrouperRuntimeException e) {
             throw new GridGrouperRuntimeFaultFaultMessage(message + ":" + e.getMessage(), e.getFault());
         } catch (StemNotFoundException e) {
             throw new StemNotFoundFaultFaultMessage(message + ":" + e.getMessage(), e.getFault());
         } catch (StemAddException e) {
             throw new StemAddFaultFaultMessage(message + ":" + e.getMessage(), e.getFault());
         }
     }
 
     @Override
     public GetGroupResponse getGroup(GetGroupRequest parameters) throws GroupNotFoundFaultFaultMessage, GridGrouperRuntimeFaultFaultMessage {
         String message = "getGroup";
         try {
             GroupDescriptor desc = gridGrouper.getGroup(getCallerId(), parameters.getGroup().getGroupIdentifier());
             GetGroupResponse response = new GetGroupResponse();
             response.setGroupDescriptor(desc);
             return response;
         } catch (GridGrouperRuntimeException e) {
             throw new GridGrouperRuntimeFaultFaultMessage(message + ":" + e.getMessage(), e.getFault());
         } catch (GroupNotFoundException e) {
             throw new GroupNotFoundFaultFaultMessage(message + ":" + e.getMessage(), e.getFault());
         }
     }
 
     @Override
     public AddCompositeMemberResponse addCompositeMember(AddCompositeMemberRequest parameters) throws InsufficientPrivilegeFaultFaultMessage, GroupNotFoundFaultFaultMessage, GridGrouperRuntimeFaultFaultMessage, MemberAddFaultFaultMessage {
         String message = "addCompositeMember";
         try {
             GroupDescriptor desc = gridGrouper.addCompositeMember(getCallerId(), parameters.getType().getGroupCompositeType(), parameters.getComposite().getGroupIdentifier(), parameters.getLeft().getGroupIdentifier(), parameters.getRight().getGroupIdentifier());
             AddCompositeMemberResponse response = new AddCompositeMemberResponse();
             response.setGroupDescriptor(desc);
             return response;
         } catch (InsufficientPrivilegeException e) {
             throw new InsufficientPrivilegeFaultFaultMessage(message + ":" + e.getMessage(), e.getFault());
         } catch (GridGrouperRuntimeException e) {
             throw new GridGrouperRuntimeFaultFaultMessage(message + ":" + e.getMessage(), e.getFault());
         } catch (MemberAddException e) {
             throw new MemberAddFaultFaultMessage(message + ":" + e.getMessage(), e.getFault());
         } catch (GroupNotFoundException e) {
             throw new GroupNotFoundFaultFaultMessage(message + ":" + e.getMessage(), e.getFault());
         }
     }
 
     @Override
     public HasGroupPrivilegeResponse hasGroupPrivilege(HasGroupPrivilegeRequest parameters) throws GroupNotFoundFaultFaultMessage, GridGrouperRuntimeFaultFaultMessage {
         String message = "hasGroupPrivilege";
         try {
             boolean result = gridGrouper.hasGroupPrivilege(getCallerId(), parameters.getGroup().getGroupIdentifier(), parameters.getSubject().getSubjectIdentifier(), parameters.getPrivilege().getGroupPrivilegeType());
             HasGroupPrivilegeResponse response = new HasGroupPrivilegeResponse();
             response.setResponse(result);
             return response;
         } catch (GridGrouperRuntimeException e) {
             throw new GridGrouperRuntimeFaultFaultMessage(message + ":" + e.getMessage(), e.getFault());
         } catch (GroupNotFoundException e) {
             throw new GroupNotFoundFaultFaultMessage(message + ":" + e.getMessage(), e.getFault());
         }
     }
 
     private String getCallerId() {
         String callerId = WebServiceCallerId.getCallerId(wsContext);
         if (callerId == null)
            callerId = "anonymous";
         logger.info("CallerId = " + callerId);
         return callerId;
     }
 }
