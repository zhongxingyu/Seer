 /**
  * 
  * Copyright 2006-2007 Istituto Nazionale di Fisica Nucleare (INFN)
  * 
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
  * compliance with the License. You may obtain a copy of the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software distributed under the License is
  * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and limitations under the License.
  * 
  * File : ServicesUtils.java
  * 
  * Authors: Valerio Venturi <valerio.venturi@cnaf.infn.it>
  * 
  */
 
 package org.glite.authz.pap.services;
 
 import java.util.Iterator;
 import java.util.List;
 import java.util.UUID;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.glite.authz.pap.common.PAPConfiguration;
 import org.glite.authz.pap.common.Pap;
 import org.glite.authz.pap.common.xacml.utils.PolicySetHelper;
 import org.glite.authz.pap.papmanagement.PapManager;
 import org.glite.authz.pap.repository.RepositoryManager;
 import org.glite.authz.pap.services.provisioning.exceptions.MissingIssuerException;
 import org.glite.authz.pap.services.provisioning.exceptions.VersionMismatchException;
 import org.glite.authz.pap.services.provisioning.exceptions.WrongFormatIssuerException;
 import org.joda.time.DateTime;
 import org.opensaml.Configuration;
 import org.opensaml.common.SAMLVersion;
 import org.opensaml.saml2.core.Assertion;
 import org.opensaml.saml2.core.Issuer;
 import org.opensaml.saml2.core.NameID;
 import org.opensaml.saml2.core.Response;
 import org.opensaml.saml2.core.Statement;
 import org.opensaml.saml2.core.Status;
 import org.opensaml.saml2.core.StatusCode;
 import org.opensaml.saml2.core.StatusMessage;
 import org.opensaml.saml2.core.impl.AssertionBuilder;
 import org.opensaml.saml2.core.impl.IssuerBuilder;
 import org.opensaml.saml2.core.impl.ResponseBuilder;
 import org.opensaml.saml2.core.impl.StatusBuilder;
 import org.opensaml.saml2.core.impl.StatusCodeBuilder;
 import org.opensaml.saml2.core.impl.StatusMessageBuilder;
 import org.opensaml.xacml.XACMLObject;
 import org.opensaml.xacml.policy.PolicySetType;
 import org.opensaml.xacml.policy.PolicyType;
 import org.opensaml.xacml.profile.saml.XACMLPolicyQueryType;
 import org.opensaml.xacml.profile.saml.XACMLPolicyStatementType;
 import org.opensaml.xacml.profile.saml.impl.XACMLPolicyStatementTypeImplBuilder;
 import org.opensaml.xml.XMLObjectBuilderFactory;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * @author Valerio Venturi <valerio.venturi@cnaf.infn.it>
  * 
  */
 public class ServicesUtils {
 
     @SuppressWarnings("unused")
     private static Logger logger = LoggerFactory.getLogger(ServicesUtils.class);
     public static final Object highLevelOperationLock = new Object();
 
     public static void checkQuery(XACMLPolicyQueryType query) throws VersionMismatchException,
             MissingIssuerException, WrongFormatIssuerException {
 
         /* check the version attribute is for a SAML V2.0 query */
 
         if (query.getVersion() != SAMLVersion.VERSION_20) {
             throw new VersionMismatchException();
         }
 
         /* TODO check issue instant */
 
         /* check the issuer is present and has the expected format */
 
         Issuer issuer = query.getIssuer();
 
         if (issuer == null) {
             throw new MissingIssuerException();
         }
 
         String issuerFormat = issuer.getFormat();
 
         if (issuerFormat != null && !issuerFormat.equals(NameID.ENTITY))
             throw new WrongFormatIssuerException(issuerFormat);
 
         // TODO Check that the issuer is the same as in the transport
 
     }
 
     public static Response createErrorResponse(XACMLPolicyQueryType inResponseTo, Exception e) {
 
         // get a builder factory
         XMLObjectBuilderFactory builderFactory = Configuration.getBuilderFactory();
 
         /* prepare the response */
 
         ResponseBuilder responseBuilder = (ResponseBuilder) builderFactory.getBuilder(Response.DEFAULT_ELEMENT_NAME);
         Response response = responseBuilder.buildObject();
 
         // set a few attributes for the response
         response.setID("_" + UUID.randomUUID().toString());
         response.setVersion(SAMLVersion.VERSION_20);
         response.setIssueInstant(new DateTime());
         response.setInResponseTo(inResponseTo.getID());
 
         /* add the Status element */
 
         // build a status object
         StatusBuilder statusBuilder = (StatusBuilder) builderFactory.getBuilder(Status.DEFAULT_ELEMENT_NAME);
         Status status = statusBuilder.buildObject();
 
         // build a status code object
         StatusCodeBuilder statusCodeBuilder = (StatusCodeBuilder) builderFactory.getBuilder(StatusCode.DEFAULT_ELEMENT_NAME);
         StatusCode statusCode = statusCodeBuilder.buildObject();
 
         // TODO now discriminates by exception but the code must be improved
 
         if (e instanceof VersionMismatchException) {
 
             statusCode.setValue(StatusCode.VERSION_MISMATCH_URI);
 
         } else if (e instanceof MissingIssuerException || e instanceof WrongFormatIssuerException) {
 
             // set the status code
 
             statusCode.setValue(StatusCode.REQUESTER_URI);
 
             // set status message with some details, when provided
 
             if (e.getMessage() != null) {
 
                 StatusMessageBuilder statusMessageBuilder = (StatusMessageBuilder) builderFactory.getBuilder(StatusMessage.DEFAULT_ELEMENT_NAME);
 
                 StatusMessage statusMessage = statusMessageBuilder.buildObject();
 
                 statusMessage.setMessage(e.getMessage());
 
                 // add StatusMessage to Status
                 status.setStatusMessage(statusMessage);
 
             }
 
         } else {
 
             /* set status code */
 
             statusCode.setValue(StatusCode.RESPONDER_URI);
 
         }
 
         // add StatusCode to Status
         status.setStatusCode(statusCode);
 
         response.setStatus(status);
 
         return response;
     }
 
     // TODO this method is too long, should be split
     public static Response createResponse(XACMLPolicyQueryType inResponseTo, List<XACMLObject> policyObjects,
             HttpServletRequest request) {
 
         // get a builder factory
         XMLObjectBuilderFactory builderFactory = Configuration.getBuilderFactory();
 
         /* prepare the Response object to return */
 
         // build a response object
         ResponseBuilder responseBuilder = (ResponseBuilder) builderFactory.getBuilder(Response.DEFAULT_ELEMENT_NAME);
         Response response = responseBuilder.buildObject();
 
         // set a few attributes for the response
         response.setID("_" + UUID.randomUUID().toString());
         response.setVersion(SAMLVersion.VERSION_20);
         response.setIssueInstant(new DateTime());
         response.setInResponseTo(inResponseTo.getID());
 
         /* add the Assertion element */
 
         // build an assertion object
         AssertionBuilder assertionBuilder = (AssertionBuilder) builderFactory.getBuilder(Assertion.DEFAULT_ELEMENT_NAME);
         Assertion assertion = assertionBuilder.buildObject();
 
         // set a few attributes for the assertion
         assertion.setID("_" + UUID.randomUUID().toString());
         assertion.setVersion(SAMLVersion.VERSION_20);
         assertion.setIssueInstant(new DateTime());
 
         // build an issuer object
         IssuerBuilder issuerBuilder = (IssuerBuilder) builderFactory.getBuilder(Issuer.DEFAULT_ELEMENT_NAME);
         Issuer issuer = issuerBuilder.buildObject();
 
         
         String defaultEntityId = String.format("%s://%s:%s/%s/services/ProvisioningService",
                 request.getScheme(),
                 request.getServerName(),
                 request.getServerPort(),
                 PAPConfiguration.DEFAULT_WEBAPP_CONTEXT); 
         
         PAPConfiguration conf = PAPConfiguration.instance();
         
        String issuerValue = conf.getString(PAPConfiguration.STANDALONE_SERVICE_STANZA+".entity_id", defaultEntityId);
 
         issuer.setValue(issuerValue);
 
         assertion.setIssuer(issuer);
 
         /* build policy statements objects */
 
         XACMLPolicyStatementTypeImplBuilder policyStatementBuilder = (XACMLPolicyStatementTypeImplBuilder) builderFactory.getBuilder(XACMLPolicyStatementType.TYPE_NAME_XACML20);
 
         XACMLPolicyStatementType policyStatement = policyStatementBuilder.buildObject(Statement.DEFAULT_ELEMENT_NAME,
                                                                                       XACMLPolicyStatementType.TYPE_NAME_XACML20);
 
         Iterator<XACMLObject> iterator = policyObjects.iterator();
 
         while (iterator.hasNext()) {
 
             XACMLObject xacmlObject = iterator.next();
 
             if (xacmlObject instanceof PolicySetType) {
 
                 policyStatement.getPolicySets().add((PolicySetType) xacmlObject);
 
                 // if (xacmlObject instanceof PolicySetTypeString) {
                 // ((PolicySetTypeString) xacmlObject).releasePolicySetType();
                 // }
 
             } else {
 
                 policyStatement.getPolicies().add((PolicyType) xacmlObject);
 
                 // if (xacmlObject instanceof PolicyTypeString) {
                 // ((PolicyTypeString) xacmlObject).releasePolicyType();
                 // }
 
             }
 
             // add the statement to the assertion
             assertion.getStatements().add(policyStatement);
         }
 
         // add the assertion to the response
         response.getAssertions().add(assertion);
 
         /* add the Status element */
 
         // build a status object
         StatusBuilder statusBuilder = (StatusBuilder) builderFactory.getBuilder(Status.DEFAULT_ELEMENT_NAME);
         Status status = statusBuilder.buildObject();
 
         // build a status code object
         StatusCodeBuilder statusCodeBuilder = (StatusCodeBuilder) builderFactory.getBuilder(StatusCode.DEFAULT_ELEMENT_NAME);
         StatusCode statusCode = statusCodeBuilder.buildObject();
 
         statusCode.setValue(StatusCode.SUCCESS_URI);
 
         status.setStatusCode(statusCode);
 
         response.setStatus(status);
 
         return response;
     }
 
     public static PolicySetType makeRootPolicySet() {
         
         String rootPolicySetId = "root-" + PapManager.getInstance().getPap(Pap.DEFAULT_PAP_ALIAS).getId();
         
         PolicySetType rootPolicySet = PolicySetHelper.buildWithAnyTarget(rootPolicySetId,
                                                                          PolicySetHelper.COMB_ALG_FIRST_APPLICABLE);
         rootPolicySet.setVersion(RepositoryManager.REPOSITORY_MANAGER_VERSION);
         
         return rootPolicySet;
     }
 
 }
