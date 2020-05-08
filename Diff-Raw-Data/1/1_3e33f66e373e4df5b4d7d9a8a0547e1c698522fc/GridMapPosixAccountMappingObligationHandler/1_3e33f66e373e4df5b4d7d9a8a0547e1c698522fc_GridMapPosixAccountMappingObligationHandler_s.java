 /*
  * Copyright 2008 EGEE Collaboration
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.glite.authz.common.obligation.provider.gridmap.posix;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import javax.security.auth.x500.X500Principal;
 
 import org.glite.authz.common.model.Attribute;
 import org.glite.authz.common.model.AttributeAssignment;
 import org.glite.authz.common.model.Obligation;
 import org.glite.authz.common.model.Request;
 import org.glite.authz.common.model.Result;
 import org.glite.authz.common.model.Subject;
 import org.glite.authz.common.obligation.AbstractObligationHandler;
 import org.glite.authz.common.obligation.ObligationProcessingException;
 import org.glite.authz.common.obligation.provider.gridmap.AccountMapper;
 import org.glite.authz.common.obligation.provider.gridmap.FQAN;
 import org.glite.authz.common.obligation.provider.gridmap.GridMapKey;
 import org.glite.authz.common.obligation.provider.gridmap.X509DistinguishedName;
 import org.glite.authz.common.pip.provider.X509PIP;
 import org.glite.authz.common.util.Strings;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * An obligation handler that creates a mapping between the subject ID of the request and a POSIX account (UID/GIDs).
  * This mapping information is provided in a gridmap file.
  */
 public class GridMapPosixAccountMappingObligationHandler extends AbstractObligationHandler {
 
     /** The URI, {@value}, of the obligation used to indicate that an grid map based account mapping should occur. */
     public static final String MAPPING_OB_ID = "x-posix-acount-map";
 
     /** The URI, {@value}, of the username obligation. */
     public static final String USERNAME_OB_ID = "http://authz-interop.org/xacml/obligation/username";
 
     /** The URI, {@value}, of the username obligation attribute. */
     public static final String USERNAME_ATTRIB_ID = "http://authz-interop.org/xacml/attribute/username";
 
     /** The URI, {@value}, of the UID/GID obligation. */
     public static final String UIDGID_OB_ID = "http://authz-interop.org/xacml/obligation/uidgid";
 
     /** The URI, {@value}, of the secondary GIDs obligation. */
     public static final String SECONDARY_GIDS_OB_ID = "http://authz-interop.org/xacml/obligation/secondary-gids";
 
     /** The URI, {@value}, of the UID obligation attribute. */
     public static final String UID_ATTRIB_ID = "http://authz-interop.org/xacml/attribute/posix-uid";
 
     /** The URI, {@value}, of the GID obligation attribute. */
     public static final String GID_ATTRIB_ID = "http://authz-interop.org/xacml/attribute/posix-gid";
     
     /** Class logger. */
     private final Logger log = LoggerFactory.getLogger(GridMapPosixAccountMappingObligationHandler.class);
 
     /** Service used to map subject information in to a {@link PosixAccount}. */
     private AccountMapper<PosixAccount> accountMapper;
 
     /**
      * Constructor. Obligation has the lowest precedence
      * 
      * @param obligationId ID of the handled obligation
      * @param mapper service used to map subject information in to a {@link PosixAccount}
      */
     public GridMapPosixAccountMappingObligationHandler(String obligationId, AccountMapper<PosixAccount> mapper) {
         this(obligationId, Integer.MIN_VALUE, mapper);
     }
 
     /**
      * Constructor.
      * 
      * @param obligationId ID of the handled obligation
      * @param handlerPrecedence precedence of this handler
      * @param mapper service used to map subject information in to a {@link PosixAccount}
      */
     public GridMapPosixAccountMappingObligationHandler(String obligationId, int handlerPrecedence,
             AccountMapper<PosixAccount> mapper) {
         super(obligationId, handlerPrecedence);
 
         if(mapper == null){
             throw new IllegalArgumentException("Account mapper may not be null");
         }
         accountMapper = mapper;
     }
 
     /** {@inheritDoc} */
     public void evaluateObligation(Request request, Result result) throws ObligationProcessingException {
         List<GridMapKey> mappingKeys = getMappingKeys(request);
         if(mappingKeys == null || mappingKeys.isEmpty()){
             log.warn("Unable to evaluate obligation, request did not contain appropriate information");
         }
         String subjectId = mappingKeys.get(0).toString();
         PosixAccount mappedAccount = accountMapper.mapToAccount(subjectId, mappingKeys);
         if(mappedAccount != null){
             addUIDGIDObligations(result, mappedAccount);
             
             // Remove the mapping obligation (even if it appears multiple times) 
             // since we've handled it and replaced it with the username and uid/gid obligations
             Iterator<Obligation> obligationItr = result.getObligations().iterator();
             Obligation obligation;
             List<Obligation> removedObligations = new ArrayList<Obligation>();
             while(obligationItr.hasNext()){
                 obligation = obligationItr.next();
                 if(obligation.getId().equals(GridMapPosixAccountMappingObligationHandler.MAPPING_OB_ID)){
                     removedObligations.add(obligation);
                 }
             }
             result.getObligations().removeAll(removedObligations);
         }
     }
 
     /**
      * Extracts the subject ID, primary FQAN, and second FQANs from the request's Subject.
      * 
      * @param request the current request
      * 
      * @return the list of keys to be mapped to a UID or GID
      */
     private List<GridMapKey> getMappingKeys(Request request) {
         ArrayList<GridMapKey> mappingKeys = new ArrayList<GridMapKey>();
         
         Set<Subject> subjects = request.getSubjects();
         if(subjects.size() != 1){
             log.warn("This obligation only operates on requests containing a single subject, this request contained {} subjects", subjects.size());
             return mappingKeys;
         }
         
         Attribute subjectId = null;
         Attribute primaryFQAN = null;
         Attribute secondaryFQANs = null;
         for(Attribute attribute : subjects.iterator().next().getAttributes()){
             if(attribute.getId().equals(Attribute.ID_SUB_ID)){
                 if(subjectId != null){
                     log.warn("More than one {} attribute present in Subject, only the first will be used", Attribute.ID_SUB_ID);
                     continue;
                 }
                 subjectId = attribute;
             }else if(attribute.getId().equals(X509PIP.VOMS_PRIMARY_FQAN)){
                 if(subjectId != null){
                     log.warn("More than one {} attribute present in Subject, only the first will be used", X509PIP.VOMS_PRIMARY_FQAN);
                     continue;
                 }
                 primaryFQAN = attribute;
             }else if(attribute.getId().equals(X509PIP.VOMS_FQAN)){
                 if(subjectId != null){
                     log.warn("More than one {} attribute present in Subject, only the first will be used", X509PIP.VOMS_FQAN);
                     continue;
                 }
                 secondaryFQANs = attribute;
             }
         }
         
         if(subjectId != null && subjectId.getValues().size() > 0){
             mappingKeys.add(new X509DistinguishedName(new X500Principal((String)subjectId.getValues().iterator().next())));
         }
         
         if(primaryFQAN != null && primaryFQAN.getValues().size() > 0){
             mappingKeys.add(FQAN.parseFQAN((String)primaryFQAN.getValues().iterator().next()));
         }
         
         if(secondaryFQANs != null && secondaryFQANs.getValues().size() > 0){
             for(Object value : secondaryFQANs.getValues()){
                 mappingKeys.add(FQAN.parseFQAN((String) value));
             }
         }
         
         return mappingKeys;
     }
     
     /**
      * Adds the UID/GID and username obligations to a result.
      * 
      * @param result current result
      * @param account account whose information will be added as obligations
      */
     protected void addUIDGIDObligations(Result result, PosixAccount account) {
         Obligation mappingOb = null;
         for (Obligation ob : result.getObligations()) {
             if (ob.getId().equals(MAPPING_OB_ID)) {
                 mappingOb = ob;
             }
         }
         result.getObligations().remove(mappingOb);
 
         Obligation usernameOb = buildUsernameObligation(account);
         if (usernameOb != null) {
             result.getObligations().add(usernameOb);
         }
 
         Obligation uidgidOb = buildUIDGIDObligation(account);
         result.getObligations().add(uidgidOb);
 
         Obligation secondaryGIDs = buildSecondaryGIDsObligation(account);
         if (secondaryGIDs != null) {
             result.getObligations().add(secondaryGIDs);
         }
     }
 
     /**
      * Creates an {@value #USERNAME_OB_ID} obligation if the given {@link PosixAccount} provides a username.
      * 
      * @param account the account used to populate the obligation
      * 
      * @return the created obligation or null if the {@link PosixAccount} did not contain a username
      */
     protected Obligation buildUsernameObligation(PosixAccount account) {
         String username = Strings.safeTrimOrNullString(account.getUsername());
         if (username == null) {
             return null;
         }
 
         Obligation obligation = new Obligation();
         obligation.setFulfillOn(Result.DECISION_PERMIT);
         obligation.setId(USERNAME_OB_ID);
 
         AttributeAssignment attributeAssignment = new AttributeAssignment();
         attributeAssignment.setAttributeId(USERNAME_ATTRIB_ID);
         attributeAssignment.getValues().add(username);
         obligation.getAttributeAssignments().add(attributeAssignment);
 
         return obligation;
     }
 
     /**
      * Creates an {@value #UIDGID_OB_ID} obligation with information in the given {@link PosixAccount}.
      * 
      * @param account the account used to populate the obligation
      * 
      * @return the created obligation
      */
     protected Obligation buildUIDGIDObligation(PosixAccount account) {
         Obligation obligation = new Obligation();
         obligation.setFulfillOn(Result.DECISION_PERMIT);
         obligation.setId(UIDGID_OB_ID);
 
         AttributeAssignment attributeAssignment = new AttributeAssignment();
         attributeAssignment.setAttributeId(UID_ATTRIB_ID);
         attributeAssignment.getValues().add(Long.toString(account.getUID()));
         obligation.getAttributeAssignments().add(attributeAssignment);
 
         if (!account.getGIDs().isEmpty()) {
             attributeAssignment = new AttributeAssignment();
             attributeAssignment.setAttributeId(GID_ATTRIB_ID);
             attributeAssignment.getValues().add(account.getGIDs().get(0).toString());
             obligation.getAttributeAssignments().add(attributeAssignment);
         }
 
         return obligation;
     }
 
     /**
      * Creates an {@value #SECONDARY_GIDS_OB_ID} obligation if the given {@link PosixAccount} has more than one GID.
      * 
      * @param account the account used to populate the obligation
      * 
      * @return the created obligation or null if the {@link PosixAccount} did not contain a username
      */
     protected Obligation buildSecondaryGIDsObligation(PosixAccount account) {
         if (account.getGIDs().size() < 2) {
             return null;
         }
         Obligation obligation = new Obligation();
         obligation.setFulfillOn(Result.DECISION_PERMIT);
         obligation.setId(SECONDARY_GIDS_OB_ID);
 
         AttributeAssignment attributeAssignment;
         for (int i = 1; i < account.getGIDs().size(); i++) {
             attributeAssignment = new AttributeAssignment();
             attributeAssignment.setAttributeId(GID_ATTRIB_ID);
             attributeAssignment.getValues().add(account.getGIDs().get(i).toString());
             obligation.getAttributeAssignments().add(attributeAssignment);
         }
 
         return obligation;
     }
 }
