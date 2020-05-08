 package amp.rabbit;
 
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.util.EntityUtils;
 
 import amp.anubis.core.NamedToken;
 import amp.rabbit.topology.Exchange;
 import amp.utility.http.HttpClientProvider;
 import amp.utility.serialization.ISerializer;
 
 import com.rabbitmq.client.ConnectionFactory;
 
 
 public class TokenChannelFactory extends BaseChannelFactory {
 
     private final HttpClientProvider _httpClientFactory;
     private final String _anubisUri;
     private final ISerializer _serializer;
     private final BaseChannelFactory _secureChannelFactory;
 
     public TokenChannelFactory(
             HttpClientProvider httpClientFactory,
             String anubisUri,
             ISerializer serializer) {
     	this(httpClientFactory, anubisUri, serializer, null);
     }
 
     public TokenChannelFactory(
             HttpClientProvider httpClientFactory,
             String anubisUri,
             ISerializer serializer,
             BaseChannelFactory secureChannelFactory) {
        _httpClientFactory = httpClientFactory;
        _anubisUri = anubisUri;
        _serializer = serializer;
        _secureChannelFactory = secureChannelFactory;
     }
 
 
     @Override
 	public void configureConnectionFactory(ConnectionFactory factory, Exchange exchange) throws Exception {
         try {
 
             NamedToken token = this.getNamedToken();
 
         	super.configureConnectionFactory(factory, exchange);
 
         	if(_secureChannelFactory != null){
         		_secureChannelFactory.configureConnectionFactory(factory, exchange);
             }
             
             // set the username and password from the token
             factory.setUsername(token.getIdentity());
             factory.setPassword(token.getToken());
 
         }
         catch(Exception ex) {
         	log.error("Failed to get token from Anubis.", ex);
         }
     }
 
     public NamedToken getNamedToken() throws Exception {
 
         // create the client and the GET method
         HttpClient client = _httpClientFactory.getClient();
         HttpGet getMethod = new HttpGet(_anubisUri);
 
         // get a response by executing the GET method
         HttpResponse response = client.execute(getMethod);
 
         // extract the string content from the response
         String content = EntityUtils.toString(response.getEntity());
         log.debug("Received the following content from Anubis: {}", content);
 
         // deserialize the named token and return it
         return _serializer.stringDeserialize(content, NamedToken.class);
     }
 
 
     @Override
     public void dispose() {
     }
 }
