 package org.twuni.money.treasury.client;
 
 import java.io.IOException;
 import java.lang.reflect.Type;
 import java.util.HashSet;
 import java.util.Set;
 
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.methods.HttpUriRequest;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.client.BasicResponseHandler;
 import org.twuni.common.crypto.rsa.PrivateKey;
 import org.twuni.common.crypto.rsa.Transformer;
 import org.twuni.money.common.ShareableToken;
 import org.twuni.money.common.SimpleToken;
 import org.twuni.money.common.Token;
 import org.twuni.money.common.Treasury;
 import org.twuni.money.common.exception.NetworkException;
 
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 import com.google.gson.reflect.TypeToken;
 
 public class TreasuryClient implements Treasury {
 
 	private static final String CREATE_URI = "/create";
 	private static final String VALUE_URI = "/value";
 	private static final String MERGE_URI = "/merge";
 	private static final String SPLIT_URI = "/split";
 
 	private final Gson gson = new GsonBuilder().disableHtmlEscaping().create();
 
 	private final String baseUrl;
 	private final HttpClient client;
 
 	public TreasuryClient( HttpClient client, String baseUrl ) {
 		this.client = client;
 		this.baseUrl = baseUrl;
 	}
 
 	private String getUrl( String uri ) {
 		return String.format( "%s%s", baseUrl, uri );
 	}
 
 	@Override
 	public Token create( int amount ) {
 		HttpPost post = new HttpPost( getUrl( CREATE_URI ) );
 		try {
 			post.setEntity( new StringEntity( Integer.toString( amount ) ) );
 			ShareableToken token = execute( post, new TypeToken<ShareableToken>() {
 			}.getType() );
 			return adapt( token );
 		} catch( IOException exception ) {
 			throw new NetworkException( exception );
 		}
 	}
 
 	@Override
 	public Set<Token> split( Token token, int amount ) {
 
 		HttpPost post = new HttpPost( getUrl( SPLIT_URI ) );
 
 		try {
 			post.setEntity( new StringEntity( encrypt( token, Integer.toString( amount ) ) ) );
 			Set<ShareableToken> tokens = execute( post, new TypeToken<Set<ShareableToken>>() {
 			}.getType(), token );
 			return adapt( tokens );
 		} catch( IOException exception ) {
 			throw new NetworkException( exception );
 		}
 
 	}
 
 	@Override
 	public Token merge( Token a, Token b ) {
 
 		HttpPost post = new HttpPost( getUrl( MERGE_URI ) );
 
 		try {
 			post.setEntity( new StringEntity( encrypt( a, encrypt( b, a.getActionKey().getPublicKey().serialize() ) ) ) );
 			ShareableToken token = execute( post, new TypeToken<ShareableToken>() {
 			}.getType(), a, b );
 			return adapt( token );
 		} catch( IOException exception ) {
 			throw new NetworkException( exception );
 		}
 
 	}
 
 	@Override
 	public int getValue( Token token ) {
 
 		HttpPost post = new HttpPost( getUrl( VALUE_URI ) );
 
 		try {
 			post.setEntity( new StringEntity( encrypt( token, baseUrl ) ) );
 			Integer response = execute( post, new TypeToken<Integer>() {
 			}.getType(), token );
 			return response.intValue();
 		} catch( IOException exception ) {
 			throw new NetworkException( exception );
		} catch( Exception exception ) {
			return 0;
 		}
 
 	}
 
 	@SuppressWarnings( "unchecked" )
 	private <T> T execute( HttpUriRequest request, Type responseType, Token... tokens ) throws ClientProtocolException, IOException {
 		BasicResponseHandler responseHandler = new BasicResponseHandler();
 		String response = client.execute( request, responseHandler );
 		for( Token token : tokens ) {
 			response = decrypt( token, response );
 		}
 		return (T) gson.fromJson( response, responseType );
 	}
 
 	private String encrypt( Token token, String message ) throws IOException {
 
 		Transformer action = new Transformer( token.getActionKey().getPublicKey() );
 		Transformer owner = new Transformer( token.getOwnerKey().getPublicKey() );
 
 		StringBuilder encrypted = new StringBuilder();
 
 		encrypted.append( token.getActionKey().getPublicKey() );
 		encrypted.append( "\n" );
 		encrypted.append( action.encrypt( owner.encrypt( message ) ) );
 
 		return encrypted.toString();
 
 	}
 
 	private String decrypt( Token token, String message ) throws IOException {
 		Transformer action = new Transformer( token.getActionKey() );
 		Transformer owner = new Transformer( token.getOwnerKey() );
 		return owner.decrypt( action.decrypt( message ) );
 	}
 
 	private Token adapt( ShareableToken token ) {
 		return new SimpleToken( token.getTreasury(), PrivateKey.deserialize( token.getActionKey() ), PrivateKey.deserialize( token.getOwnerKey() ), token.getValue() );
 	}
 
 	private Set<Token> adapt( Set<ShareableToken> tokens ) {
 		Set<Token> result = new HashSet<Token>();
 		for( ShareableToken token : tokens ) {
 			result.add( adapt( token ) );
 		}
 		return result;
 	}
 
 	@Override
 	public boolean equals( Object object ) {
 		if( object == null ) {
 			return false;
 		}
 		if( object instanceof TreasuryClient ) {
 			TreasuryClient treasury = (TreasuryClient) object;
 			return baseUrl.equals( treasury.baseUrl );
 		}
 		return super.equals( object );
 	}
 
 	@Override
 	public String toString() {
 		return baseUrl;
 	}
 
 }
