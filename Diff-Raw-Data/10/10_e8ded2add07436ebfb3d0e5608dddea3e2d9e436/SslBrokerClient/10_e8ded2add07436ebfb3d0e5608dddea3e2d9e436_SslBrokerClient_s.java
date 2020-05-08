 package pt.com.broker.client;
 
 import java.util.Collection;
 
 import org.caudexorigo.text.RandomStringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import pt.com.broker.auth.AuthInfo;
 import pt.com.broker.auth.CredentialsProvider;
 import pt.com.broker.client.messaging.PendingAcceptRequestsManager;
 import pt.com.broker.client.utils.BlockingMessageAcceptedListener;
 import pt.com.broker.types.NetAction;
 import pt.com.broker.types.NetAuthentication;
 import pt.com.broker.types.NetMessage;
 import pt.com.broker.types.NetProtocolType;
 import pt.com.broker.types.NetAction.ActionType;
 
 public final class SslBrokerClient extends BaseBrokerClient
 {
 	private static final Logger log = LoggerFactory.getLogger(SslBrokerClient.class);
 
 	private boolean requiresAuthentication = false;
 
 	protected AuthInfo userCredentials;
 
 	private String keystoreLocation = null;
 	private char[] keystorePass = null;
 
 	private CredentialsProvider provider;
 
 	private AuthInfo providerCredentials;
 
 	public SslBrokerClient(String host, int portNumber) throws Throwable
 	{
 		this(host, portNumber, (String) null, (char[]) null);
 	}
 
 	public SslBrokerClient(String host, int portNumber, String keystoreLocation, char[] keystorePw) throws Throwable
 	{
 		this(host, portNumber, "BrokerClient", keystoreLocation, keystorePw);
 	}
 
 	public SslBrokerClient(String host, int portNumber, String appName) throws Throwable
 	{
 		this(host, portNumber, appName, null, null);
 	}
 
 	public SslBrokerClient(String host, int portNumber, String appName, String keystoreLocation, char[] keystorePw) throws Throwable
 	{
 		this(host, portNumber, appName, NetProtocolType.PROTOCOL_BUFFER, keystoreLocation, keystorePw);
 	}
 
 	public SslBrokerClient(String host, int portNumber, String appName, NetProtocolType ptype) throws Throwable
 	{
 		this(host, portNumber, appName, ptype, null, null);
 	}
 
 	public SslBrokerClient(String host, int portNumber, String appName, NetProtocolType ptype, String keystoreLocation, char[] keystorePw) throws Throwable
 	{
 		super(host, portNumber, appName, ptype);
 		this.keystoreLocation = keystoreLocation;
 		this.keystorePass = keystorePw;
 		init();
 	}
 
 	public SslBrokerClient(Collection<HostInfo> hosts) throws Throwable
 	{
 		this(hosts, (String) null, (char[]) null);
 	}
 
 	public SslBrokerClient(Collection<HostInfo> hosts, String keystoreLocation, char[] keystorePw) throws Throwable
 	{
 		this(hosts, "BrokerClient", keystoreLocation, keystorePw);
 	}
 
 	public SslBrokerClient(Collection<HostInfo> hosts, String appName) throws Throwable
 	{
 		this(hosts, appName, null, null);
 	}
 
 	public SslBrokerClient(Collection<HostInfo> hosts, String appName, String keystoreLocation, char[] keystorePw) throws Throwable
 	{
 		this(hosts, appName, NetProtocolType.PROTOCOL_BUFFER, keystoreLocation, keystorePw);
 	}
 
 	public SslBrokerClient(Collection<HostInfo> hosts, String appName, NetProtocolType ptype) throws Throwable
 	{
 		this(hosts, appName, ptype, null, null);
 	}
 
 	public SslBrokerClient(Collection<HostInfo> hosts, String appName, NetProtocolType ptype, String keystoreLocation, char[] keystorePw) throws Throwable
 	{
 		super(hosts, appName, ptype);
 		this.keystoreLocation = keystoreLocation;
 		this.keystorePass = keystorePw;
 		init();
 	}
 
 	public void setAuthenticationCredentials(AuthInfo userCredentials)
 	{
 		this.userCredentials = userCredentials;
 	}
 
 	public void setCredentialsProvider(CredentialsProvider provider, AuthInfo providerCredentials)
 	{
 		this.provider = provider;
 		this.providerCredentials = providerCredentials;
 	}
 
 	public boolean hasCredentialsProvider()
 	{
 		return provider != null;
 	}
 
 	public boolean renewCredentials()
 	{
 		if (provider != null)
 		{
 			try
 			{
 				setAuthenticationCredentials(provider.getCredentials(providerCredentials));
 			}
 			catch (Exception e)
 			{
 				log.error("Failed to renw credentials", e);
 				return false;
 			}
 			return true;
 		}
 		return false;
 	}
 
 	public boolean authenticateClient() throws Throwable
 	{
 		this.requiresAuthentication = true;
 
 		setState(BrokerClientState.AUTH);
 
 		NetAuthentication clientAuth = new NetAuthentication(userCredentials.getToken());
 		if (userCredentials.getRoles() != null && userCredentials.getRoles().size() != 0)
 			clientAuth.setRoles(userCredentials.getRoles());
 
 		if (userCredentials.getUserAuthenticationType() != null)
 			clientAuth.setAuthenticationType(userCredentials.getUserAuthenticationType());
 
 		if (userCredentials.getUserId() != null)
 			clientAuth.setUserId(userCredentials.getUserId());
 
 		NetAction action = new NetAction(ActionType.AUTH);
 		action.setAuthenticationMessage(clientAuth);
 
 		NetMessage msg = new NetMessage(action);
 
 		Object syncObj = new Object();
 		BlockingMessageAcceptedListener acceptedListener = new BlockingMessageAcceptedListener(syncObj);
 		String actionId = RandomStringUtils.random(25);
 		clientAuth.setActionId(actionId);
 
 		AcceptRequest acceptRequest = new AcceptRequest(actionId, acceptedListener, 5000);
 		PendingAcceptRequestsManager.addAcceptRequest(acceptRequest);
 
 		getNetHandler().sendMessage(msg);
 		synchronized (syncObj)
 		{
 			syncObj.wait();
 		}
 
 		if (acceptedListener.wasFailure())
 		{
 			log.error("Authentication failed.", acceptedListener.getFault().getMessage());
 			setState(BrokerClientState.OK);
 			return false;
 		}
 		else if (acceptedListener.wasTimeout())
 		{
 			log.warn("Authentication failed by timeout.");
 			setState(BrokerClientState.OK);
 			return false;
 		}
 		setState(BrokerClientState.OK);
 		return true;
 	}
 
 	public boolean isAuthenticationRequired()
 	{
 		return requiresAuthentication;
 	}
 
 	@Override
 	protected BrokerProtocolHandler getBrokerProtocolHandler() throws Throwable
 	{
 		BrokerProtocolHandler brokerProtocolHandler;
 
 		SslNetworkConnector networkConnector = new SslNetworkConnector(getHostInfo(), keystoreLocation, keystorePass);
 		brokerProtocolHandler = new BrokerProtocolHandler(this, getPortocolType(), networkConnector, this.isOldFramming());
 		networkConnector.setProtocolHandler(brokerProtocolHandler);
 
 		return brokerProtocolHandler;
 	}
 }
