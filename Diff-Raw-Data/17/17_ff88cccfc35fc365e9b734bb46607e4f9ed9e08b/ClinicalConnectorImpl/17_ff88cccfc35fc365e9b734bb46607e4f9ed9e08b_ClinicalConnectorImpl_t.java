 package gov.nih.nci.clinicalconnector.service;
 
 import gov.nih.nci.cagrid.common.Utils;
 import gov.nih.nci.clinicalconnector.client.ClinicalConnectorClient;
 import gov.nih.nci.clinicalconnector.common.ClinicalConnectorI;
 import gov.nih.nci.clinicalconnector.manager.EnrollPatientManager;
 import gov.nih.nci.clinicalconnector.manager.LoadLabsManager;
 import clinicalconnector.nci.nih.gov.RegisterSubjectResponse;
 import clinicalconnector.nci.nih.gov.RegisterSubjectRequest;
 import clinicalconnector.nci.nih.gov.LoadLabsRequest;
 import clinicalconnector.nci.nih.gov.LoadLabsResponse;
 
 import java.io.InputStream;
 import java.io.StringWriter;
 import java.rmi.RemoteException;
 
 import javax.xml.namespace.QName;
 
 import org.apache.log4j.Logger;
 import org.oasis.wsrf.properties.GetMultipleResourcePropertiesResponse;
 import org.oasis.wsrf.properties.GetMultipleResourceProperties_Element;
 import org.oasis.wsrf.properties.GetResourcePropertyResponse;
 import org.oasis.wsrf.properties.QueryResourcePropertiesResponse;
 import org.oasis.wsrf.properties.QueryResourceProperties_Element;
 
 /** 
  * TODO:I am the service side implementation class.  IMPLEMENT AND DOCUMENT ME
  * 
  * @created by Introduce Toolkit version 1.3
  * 
  */
 public class ClinicalConnectorImpl extends ClinicalConnectorImplBase implements ClinicalConnectorI{
 
 	private final static Logger log = Logger.getLogger(ClinicalConnectorImpl.class.getName());
 
 	protected EnrollPatientManager enrollPatientManager;
 	protected LoadLabsManager loadLabsManager;
 
 	public ClinicalConnectorImpl() throws RemoteException {
 		super();
 	}
 
   public clinicalconnector.nci.nih.gov.LoadLabsResponse loadLabs(clinicalconnector.nci.nih.gov.LoadLabsRequest loadLabsRequest) throws RemoteException {
 
 		LoadLabsResponse response = null;
 		StringWriter writer = new StringWriter();
 		try {
 			InputStream seralizeStream = ClinicalConnectorClient.class.getResourceAsStream(
 			"client-config.wsdd");
 			Utils.serializeObject(loadLabsRequest,
 					new QName("LoadLabsRequest"), writer, seralizeStream);
 		} catch (Exception e) {
 			e.printStackTrace();
 			throw new RuntimeException(e);
 		}
 		log.debug(writer.getBuffer().toString());
 
 		try {
 			response = (LoadLabsResponse) loadLabsManager
 			.loadLabs(loadLabsRequest);
 
 			try {
 				InputStream seralizeStream = ClinicalConnectorClient.class.getResourceAsStream(
 				"client-config.wsdd");
 				Utils.serializeObject(response, new QName("LoadLabsResponse"),
 						writer, seralizeStream);
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 			log.debug("The response is: ");
 			log.debug(writer.getBuffer().toString());
 
 		} catch (Exception e) {
 			e.printStackTrace();
 			throw new RemoteException(e.getMessage());
 		}
 
 		return response;
 	}
 
  public clinicalconnector.nci.nih.gov.RegisterSubjectResponse registerSubject(clinicalconnector.nci.nih.gov.RegisterSubjectRequest enrollPatientRequest) throws RemoteException {
 
         RegisterSubjectResponse enrollPatientResponse = null;
         StringWriter writer = new StringWriter();
         //StringWriter anotherWriter = new StringWriter();
         try {
              InputStream seralizeStream = ClinicalConnectorClient.class.getResourceAsStream(
                         "client-config.wsdd");
 
              Utils.serializeObject(enrollPatientRequest, new QName(
                         "EnrollPatientRequest"), writer, seralizeStream);
         } catch (Exception e) {
              e.printStackTrace();
              throw new RuntimeException(e);
         }
 		
 		System.out.println("Received EnrollPatientRequest");
 		log.debug("Received EnrollPatientRequest");
 		//System.out.println("Request Buffer String: '" + writer.getBuffer().toString() +"'");
 		//System.out.println("Request Buffer AnotherString: '" + anotherWriter.getBuffer().toString() +"'");
 		log.debug(writer.getBuffer().toString());
 
 		try {
 			enrollPatientResponse = (RegisterSubjectResponse) enrollPatientManager
 			.enrollPatient(enrollPatientRequest);
 		} catch (Exception e) {
 			e.printStackTrace();
 			throw new RemoteException(e.getMessage());
 		}
 
 		writer = new StringWriter();
 		try {
 			InputStream seralizeStream = ClinicalConnectorClient.class.getResourceAsStream(
 			"client-config.wsdd");
 			Utils.serializeObject(enrollPatientResponse, new QName(
 			"EnrollPatientResponse"), writer,seralizeStream);
 		} catch (Exception e) {
 			e.printStackTrace();
 			throw new RuntimeException(e);
 		}
 		log.debug("Sending enrollPatientResponse");
 		log.debug(writer.getBuffer().toString());
 		return enrollPatientResponse;
 	}
 
 	public GetMultipleResourcePropertiesResponse getMultipleResourceProperties(
 			GetMultipleResourceProperties_Element params)
 	throws RemoteException {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public GetResourcePropertyResponse getResourceProperty(QName arg0)
 	throws RemoteException {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public QueryResourcePropertiesResponse queryResourceProperties(
 			QueryResourceProperties_Element arg0) throws RemoteException {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public EnrollPatientManager getEnrollPatientManager() {
 		return enrollPatientManager;
 	}
 
 	public void setEnrollPatientManager(
 			EnrollPatientManager enrollPatientManager) {
 		this.enrollPatientManager = enrollPatientManager;
 	}
 
 	public LoadLabsManager getLoadLabsManager() {
 		return loadLabsManager;
 	}
 
 	public void setLoadLabsManager(LoadLabsManager loadLabsManager) {
 		this.loadLabsManager = loadLabsManager;
 	}
 
 }
 
