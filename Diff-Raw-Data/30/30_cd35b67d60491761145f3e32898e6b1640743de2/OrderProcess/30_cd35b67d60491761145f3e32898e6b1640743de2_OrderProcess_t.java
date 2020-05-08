 package pl.edu.agh.security.order.process;
 
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.xml.ws.BindingProvider;
 import javax.xml.ws.handler.Handler;
 import javax.xml.ws.handler.MessageContext;
 
 import org.apache.http.auth.AuthScope;
 import org.apache.http.auth.UsernamePasswordCredentials;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.jboss.resteasy.client.ClientRequest;
 import org.jboss.resteasy.client.ClientResponse;
 import org.jboss.resteasy.client.ProxyFactory;
 import org.jboss.resteasy.client.core.executors.ApacheHttpClient4Executor;
 import org.jboss.resteasy.plugins.providers.RegisterBuiltin;
 import org.jboss.resteasy.spi.ResteasyProviderFactory;
 import org.picketlink.identity.federation.core.exceptions.ConfigurationException;
 import org.picketlink.identity.federation.core.exceptions.ParsingException;
 import org.picketlink.identity.federation.core.exceptions.ProcessingException;
 import org.picketlink.identity.federation.core.saml.v2.util.DocumentUtil;
 import org.picketlink.trust.jbossws.SAML2Constants;
 import org.picketlink.trust.jbossws.handler.SAML2Handler;
 import org.w3c.dom.Element;
 
 import pl.edu.agh.security.common.Utils;
 import pl.edu.agh.security.common.services.DeliveryState;
 import pl.edu.agh.security.common.services.IDeliveryService;
 import pl.edu.agh.security.store.state.service.client.IStoreState;
 import pl.edu.agh.security.store.state.service.client.Store;
 
 public class OrderProcess {
 
     private static final String STORES_REQUEST_PATH = "http://stores-states.security.agh.edu.pl:8080/stores-state-service/state";
     private static final String DELIVERY_REQUEST_PATH = "http://delivery.security.agh.edu.pl:8080/delivery";
     private static final String STORES_HOST = "stores-states.security.agh.edu.pl";
     private static final String DELIVERY_HOST = "delivery.security.agh.edu.pl";
 	private static final int PORT = 8080;
 	private static final String STS_SERVICE_NAME = "PicketLinkSTS";
 	private static final String STS_PORT = "PicketLinkSTSPort";
 	private static final String STS_ENDPOINT_URI = "http://localhost:8080/picketlink-sts/PicketLinkSTS";
 
 	private Element samlAssertion;
 	private String samlAssertionString;
 	private final String userName;
 	private final String password;
 	private final String orderedProduct;
 	private final int count;
 	private final boolean invoiceRequested;
 
 	public OrderProcess(String userName, String password,
 			String orderedProduct, int count, boolean invoiceRequested) {
 		super();
 		this.userName = userName;
 		this.password = password;
 		this.orderedProduct = orderedProduct;
 		this.count = count;
 		this.invoiceRequested = invoiceRequested;
 	}
 
 	public void execute() throws ConfigurationException, ProcessingException,
 			ParsingException {
 		/**
 		 * Retrieves SAML assertion
 		 */
 		authenticate();
 
 		/**
 		 * Looks for store with requested count of the product
 		 */
 		Store store = prepareStoreStateServiceClient().getStore(orderedProduct,
 				count);
 		if (store != null) {
 			// TODO: store service + shipments
 
            DeliveryState deliveryState = prepareDeliveryServiceClient().putDelivery("Middle of nowhere",
                    store.getLocation(), 1.0);
             System.out.println(deliveryState);
 
 			/**
 			 * Financial service execution
 			 */
 			FinancialOperations financialOperations = prepareFinancialServiceClient();
 			TransactionRequest transactionRequest = new TransactionRequest();
 			transactionRequest.setCount(count);
 			Product product = new Product();
 			product.setName(orderedProduct);
 			transactionRequest.setProduct(product);
 			transactionRequest.setInvoiceRequested(invoiceRequested);
 
 			TransactionResponse transactionResponse = financialOperations
 					.registerTransaction(transactionRequest);
 			if (transactionResponse != null) {
 				System.out.println("Transaction successful. Due date: "
 						+ transactionResponse.getDueDate()
 						+ ", invoice number: "
 						+ transactionResponse.getInvoiceIdentifier()
 						+ ", store: " + store.getLocation());
 			}
 		} else {
 			System.out.println("Requested count of the product not available");
 		}
 	}
 
 	public void authenticate() throws ConfigurationException,
 			ProcessingException, ParsingException {
 		samlAssertion = Utils.retrieveSamlAssertion(STS_SERVICE_NAME, STS_PORT,
 				STS_ENDPOINT_URI, userName, password);
 		samlAssertionString = DocumentUtil.getNodeAsString(samlAssertion);
 	}
 
 	public IStoreState prepareStoreStateServiceClient()
 			throws ConfigurationException, ProcessingException,
 			ParsingException {
 		DefaultHttpClient defaultHttpClient = new DefaultHttpClient();
 		defaultHttpClient.getCredentialsProvider().setCredentials(
 new AuthScope(STORES_HOST, PORT),
 				new UsernamePasswordCredentials(userName, samlAssertionString));
 		ApacheHttpClient4Executor executor = new ApacheHttpClient4Executor(
 				defaultHttpClient) {
 
 			@SuppressWarnings("rawtypes")
 			@Override
 			public ClientResponse execute(ClientRequest request)
 					throws Exception {
 				request.header("samlAssertion", samlAssertionString);
 				return super.execute(request);
 			}
 		};
 
 		// This initialization only needs to be done once per VM
 		RegisterBuiltin.register(ResteasyProviderFactory.getInstance());
 
         return ProxyFactory.create(IStoreState.class, STORES_REQUEST_PATH, executor);
 	}
 
     public IDeliveryService prepareDeliveryServiceClient() throws ConfigurationException, ProcessingException,
             ParsingException {
         DefaultHttpClient defaultHttpClient = new DefaultHttpClient();
         defaultHttpClient.getCredentialsProvider().setCredentials(new AuthScope(DELIVERY_HOST, PORT),
                 new UsernamePasswordCredentials(userName, samlAssertionString));
         ApacheHttpClient4Executor executor = new ApacheHttpClient4Executor(defaultHttpClient) {
 
             @SuppressWarnings("rawtypes")
             @Override
             public ClientResponse execute(ClientRequest request) throws Exception {
                 request.header("samlAssertion", samlAssertionString);
                 return super.execute(request);
             }
         };
 
         // This initialization only needs to be done once per VM
         RegisterBuiltin.register(ResteasyProviderFactory.getInstance());
 
         return ProxyFactory.create(IDeliveryService.class, DELIVERY_REQUEST_PATH, executor);
     }
 
 	public FinancialOperations prepareFinancialServiceClient() {
 		FinancialOperations financialOperations = new FinancialOperationsService()
 				.getFinancialOperationsPort();
 
 		BindingProvider bindingProvider = (BindingProvider) financialOperations;
 		Map<String, List<String>> headers = new HashMap<String, List<String>>();
 		headers.put("samlAssertion",
 				Collections.singletonList(samlAssertionString));
 		bindingProvider.getRequestContext().put(
 				MessageContext.HTTP_REQUEST_HEADERS, headers);
 
 		bindingProvider.getRequestContext().put(
 				SAML2Constants.SAML2_ASSERTION_PROPERTY, samlAssertion);
 
 		List<Handler> handlers = bindingProvider.getBinding().getHandlerChain();
 		handlers.add(new SAML2Handler());
 		bindingProvider.getBinding().setHandlerChain(handlers);
 		return financialOperations;
 	}
 
 }
