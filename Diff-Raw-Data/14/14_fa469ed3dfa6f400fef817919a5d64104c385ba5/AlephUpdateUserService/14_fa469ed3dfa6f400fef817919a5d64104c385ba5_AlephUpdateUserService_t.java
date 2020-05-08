 package org.extensiblecatalog.ncip.v2.aleph;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.extensiblecatalog.ncip.v2.aleph.AlephXServices.AlephException;
 import org.extensiblecatalog.ncip.v2.aleph.AlephXServices.user.AlephUser;
 import org.extensiblecatalog.ncip.v2.aleph.util.AlephRemoteServiceManager;
 import org.extensiblecatalog.ncip.v2.service.*;
 import org.xml.sax.SAXException;
 
 public class AlephUpdateUserService implements NCIPService<UpdateUserInitiationData, UpdateUserResponseData> {
 
 	public UpdateUserResponseData performService(UpdateUserInitiationData initData, ServiceContext serviceContext, RemoteServiceManager serviceManager) throws ServiceException {
 		// TODO: Think about forwarding password in encrypted format ({@link Version1AuthenticationDataFormatType.APPLICATION_AUTH_POLICY_XML})
 
 		final UpdateUserResponseData responseData = new UpdateUserResponseData();
 		AlephRemoteServiceManager alephRemoteServiceManager = (AlephRemoteServiceManager) serviceManager;
 
 		String patronId = null;
 		String password = null;
 
 		for (AuthenticationInput authInput : initData.getAuthenticationInputs()) {
 			if (authInput.getAuthenticationInputType() == Version1AuthenticationInputType.USER_ID) {
 				patronId = authInput.getAuthenticationInputData();
 			} else if (authInput.getAuthenticationInputType() == Version1AuthenticationInputType.PASSWORD) {
 				password = authInput.getAuthenticationInputData();
 			}
 		}
 
 		if (patronId == null || password == null) {
 			String details = patronId == null ? "Username" : "Password" + " is missing.";
 			throw new ServiceException(ServiceError.UNSUPPORTED_REQUEST, "Please authenticate user. " + details);
 		}
 
 		InitiationHeader initiationHeader = initData.getInitiationHeader();
 		if (initiationHeader != null) {
 			ResponseHeader responseHeader = new ResponseHeader();
 			if (initiationHeader.getFromAgencyId() != null && initiationHeader.getToAgencyId() != null) {
 				responseHeader.setFromAgencyId(initiationHeader.getFromAgencyId());
 				responseHeader.setToAgencyId(initiationHeader.getToAgencyId());
 			}
 			if (initiationHeader.getFromSystemId() != null && initiationHeader.getToSystemId() != null) {
 				responseHeader.setFromSystemId(initiationHeader.getFromSystemId());
 				responseHeader.setToSystemId(initiationHeader.getToSystemId());
 				if (initiationHeader.getFromAgencyAuthentication() != null && !initiationHeader.getFromAgencyAuthentication().isEmpty())
 					responseHeader.setFromSystemAuthentication(initiationHeader.getFromAgencyAuthentication());
 			}
 			responseData.setResponseHeader(responseHeader);
 		}
 
 		AlephUser alephUser = null;
 
 		try {
 			alephUser = alephRemoteServiceManager.updateUser(patronId, password, initData);
 			if (alephUser == null)
 				throw new AlephException("alephUser returned by responder is null");
 			else {
 				UserId id = new UserId();
				String username = alephUser.getUsername(); 
				id.setUserIdentifierValue(username);
 				responseData.setUserId(id);
 			}
 		} catch (IOException ie) {
 			Problem p = new Problem();
 			p.setProblemType(new ProblemType("Procesing IOException error"));
 			p.setProblemDetail(ie.getMessage());
 			List<Problem> problems = new ArrayList<Problem>();
 			problems.add(p);
 			responseData.setProblems(problems);
 		} catch (SAXException se) {
 			Problem p = new Problem();
 			p.setProblemType(new ProblemType("Procesing SAXException error"));
 			p.setProblemDetail(se.getMessage());
 			List<Problem> problems = new ArrayList<Problem>();
 			problems.add(p);
 			responseData.setProblems(problems);
 		} catch (AlephException ae) {
 			Problem p = new Problem();
 			p.setProblemType(new ProblemType("Procesing AlephException error"));
 			p.setProblemDetail(ae.getMessage());
 			List<Problem> problems = new ArrayList<Problem>();
 			problems.add(p);
 			responseData.setProblems(problems);
 		} catch (ParserConfigurationException pce) {
 			Problem p = new Problem();
 			p.setProblemType(new ProblemType("Procesing ParserConfigurationException error"));
 			p.setProblemDetail(pce.getMessage());
 			List<Problem> problems = new ArrayList<Problem>();
 			problems.add(p);
 			responseData.setProblems(problems);
 		} catch (Exception e) {
 			Problem p = new Problem();
 			p.setProblemType(new ProblemType("Unknown procesing exception error"));
 			p.setProblemDetail(e.getMessage());
 			List<Problem> problems = new ArrayList<Problem>();
 			problems.add(p);
 			responseData.setProblems(problems);
 		}
 
 		return responseData;
 	}
 
 }
