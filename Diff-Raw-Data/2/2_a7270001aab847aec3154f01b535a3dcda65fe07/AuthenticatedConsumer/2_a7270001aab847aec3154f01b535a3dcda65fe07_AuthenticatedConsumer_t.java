package pt.com.broker.auth.saposts.samples;
 
 import java.util.concurrent.atomic.AtomicInteger;
 
 import org.caudexorigo.cli.CliFactory;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import pt.com.broker.auth.AuthInfo;
 import pt.com.broker.auth.CredentialsProvider;
 import pt.com.broker.auth.CredentialsProviderFactory;
 import pt.com.broker.auth.saposts.SapoSTSParameterProvider;
 import pt.com.broker.auth.saposts.SapoSTSProvider;
 import pt.com.broker.client.CliArgs;
 import pt.com.broker.client.SslBrokerClient;
 import pt.com.broker.client.messaging.BrokerListener;
 import pt.com.broker.types.NetNotification;
 import pt.com.broker.types.NetProtocolType;
 import pt.com.broker.types.NetSubscribe;
 import pt.com.broker.types.NetAction.DestinationType;
 
 public class AuthenticatedConsumer implements BrokerListener
 {
 
 	private static final Logger log = LoggerFactory.getLogger(AuthenticatedConsumer.class);
 	private final AtomicInteger counter = new AtomicInteger(0);
 
 	private String host;
 	private int port;
 	private DestinationType dtype;
 	private String dname;
 
 	private String stsLocation;
 	private String stsUsername;
 	private String stsPassword;
 
 	private String keystoreLocation;
 	private String keystorePassword;
 
 	private static void initSTSParams(String stsLocation)
 	{
 		SapoSTSParameterProvider.Parameters parameters = new SapoSTSParameterProvider.Parameters(stsLocation);
 		SapoSTSParameterProvider.setSTSParameters(parameters);
 		
 		CredentialsProviderFactory.addProvider("SapoSTS", new SapoSTSProvider() );
 	}
 
 	public static void main(String[] args) throws Throwable
 	{
 		final CliArgs cargs = CliFactory.parseArguments(CliArgs.class, args);
 
 		AuthenticatedConsumer consumer = new AuthenticatedConsumer();
 
 		consumer.host = cargs.getHost();
 		consumer.port = cargs.getPort();
 		consumer.dtype = DestinationType.valueOf(cargs.getDestinationType());
 		consumer.dname = cargs.getDestination();
 
 		consumer.stsLocation = cargs.getSTSLocation();
 		consumer.stsUsername = cargs.getUsername();
 		consumer.stsPassword = cargs.getUserPassword();
 		consumer.keystoreLocation = cargs.getKeystoreLocation();
 		consumer.keystorePassword = cargs.getKeystorePassword();
 
 		// Provider initialization
 		initSTSParams(consumer.stsLocation);
 		
 		
 		SslBrokerClient bk = new SslBrokerClient(consumer.host, consumer.port, "tcp://mycompany.com/mysniffer", NetProtocolType.PROTOCOL_BUFFER, consumer.keystoreLocation, consumer.keystorePassword.toCharArray());
 
 		AuthInfo clientAuthInfo = new AuthInfo(consumer.stsUsername, consumer.stsPassword);
 		clientAuthInfo.setUserAuthenticationType("SapoSTS");
 		
 		
 		CredentialsProvider credentialsProvider = CredentialsProviderFactory.getProvider("SapoSTS");
 		AuthInfo stsClientCredentials = credentialsProvider.getCredentials(clientAuthInfo);
 		
 
 		bk.setAuthenticationCredentials(stsClientCredentials);
 
 		/*
 		 * Note: Credential Provider should be set in the case of required automatic credentials access. E.g.  on agent fail, client failsover to another 
 		 * agent (eventually the same) and renewed STS credentials may be necessary to authenticate. This is not necessary in username-password scenarios.
 		 */
 		bk.setCredentialsProvider(credentialsProvider, clientAuthInfo);
 		
 		try
 		{
 			if(!bk.authenticateClient())
 			{
 				System.out.println("Authentication failed");
 				return;
 			}
 		}
 		catch (Throwable t)
 		{
 			System.out.println("Unable to authenticate client...");
 			System.out.println(t);
 			return;
 		}
 
 		System.out.println("subscribing");
 		NetSubscribe subscribe = new NetSubscribe(consumer.dname, consumer.dtype);
 
 		bk.addAsyncConsumer(subscribe, consumer, null);
 
 		System.out.println("listening...");
 	}
 
 	@Override
 	public boolean isAutoAck()
 	{
 		if (dtype == DestinationType.TOPIC)
 		{
 			return false;
 		}
 		else
 		{
 			return true;
 		}
 	}
 
 	@Override
 	public void onMessage(NetNotification notification)
 	{
 		log.info(String.format("%s -> Received Message Length: %s (%s)", counter.incrementAndGet(), notification.getMessage().getPayload().length, new String(notification.getMessage().getPayload())));
 	}
 
 }
