 package webserviceclient;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 
 import javax.xml.namespace.QName;
 
 import mediator.MediatorWebServiceClient;
 import model.service.Service;
 import model.user.User;
 import model.webservice.WebServiceJson;
 import model.webservice.WebServiceJsonImpl;
 
 import org.apache.axis.client.Call;
 import org.apache.log4j.Logger;
 
 import constants.Constants;
 
 /**
  * Implements {@link WebServiceClient}.
  *
  * @author cmihail, radu-tutueanu
  */
 public class WebServiceClientImpl implements WebServiceClient {
 
 	private static final Logger logger = Logger.getLogger(WebServiceClientImpl.class);
 	private final MediatorWebServiceClient mediator;
 	private final WebServiceJson json = new WebServiceJsonImpl();
 
 	private URL endpoint;
 	private org.apache.axis.client.Service service;
 	private HashMap<String, Call> operationCalls;
 
 	public WebServiceClientImpl(MediatorWebServiceClient mediator) {
 		this.mediator = mediator;
 
 		try {
 			endpoint = new URL(Constants.WEBSERVICE_ADDRESS);
 		} catch (MalformedURLException e) {
 			logger.error(e.getMessage());
 			mediator.webServiceError("Problem at connecting to web service");
 		}
 		service = new org.apache.axis.client.Service();
 		operationCalls=new HashMap<String, Call>();
 	}
 
 	@Override
 	public Map<Service, Set<User>> login(User user, String password) {
 		Map<Service, Set<User>> services = null;
 		try {
 			Object[] params = new Object[] { json.userAsJson(user), password };
 			Object obj = invoke("loginUser", params);
 			logger.info(obj);
 			services = json.jsonAsMapServiceUsers(obj.toString());
 		} catch (Exception e) {
 			e.printStackTrace();
 			logger.error(e.getMessage());
 			mediator.webServiceError("Problem at connecting to web service");
 		}
 
 		return services;
 	}
 
 	@Override
 	public void logout(User user) {
 		try {
 			Object[] params = new Object[] { json.userAsJson(user) };
			Object obj = invoke("logout", params);
 			logger.info(obj);
 		} catch (Exception e) {
 			e.printStackTrace();
 			logger.error(e.getMessage());
 			mediator.webServiceError("Problem at connecting to web service");
 		}
 	}
 
 	private Object invoke(String operationName, Object[] params) throws Exception {
 		Call call;
 
 		if (operationCalls.containsKey(operationName)) {
 			call=operationCalls.get(operationName);
 		}
 		else {
 			call = (Call)service.createCall();
 			call.setTargetEndpointAddress(endpoint);
 			call.setOperationName(new QName(operationName)); // operation name
 			operationCalls.put(operationName, call);
 		}
 
 		return call.invoke(params);
 	}
 }
