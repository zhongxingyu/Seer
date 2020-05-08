 package br.com.developer.redu.http;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URLConnection;
 import java.util.Map;
 
 import org.scribe.builder.ServiceBuilder;
 import org.scribe.exceptions.OAuthConnectionException;
 import org.scribe.model.OAuthRequest;
 import org.scribe.model.Response;
 import org.scribe.model.Token;
 import org.scribe.model.Verb;
 import org.scribe.model.Verifier;
 import org.scribe.oauth.OAuthService;
 
 import android.util.Log;
 import br.com.developer.redu.models.Lecture;
 
 /**
  * Created with IntelliJ IDEA. User: igor Date: 8/30/12 Time: 12:08 PM To change
  * this template use File | Settings | File Templates.
  */
 public class ScribeHttpClient extends HttpClient {
 	private OAuthService service;
 	private Token accesToken;
 
 	public ScribeHttpClient(String consumerKey, String consumerSecret) {
 		super(consumerKey, consumerSecret);
 		this.initOauth();
 
 	}
 
 	public ScribeHttpClient(String consumerKey, String consumerSecret, String pin) {
 		super(consumerKey, consumerSecret);
 		this.initOauth();
 		this.accesToken = this.service.getAccessToken(null, new Verifier(pin));
 	}
 
 	private void initOauth() {
 		this.service = new ServiceBuilder().provider(ReduOauth2.class).apiKey(this.consumerKey).apiSecret(this.consumerSecret).callback("").build();
 
 	}
 
 	private void addUrlParams(OAuthRequest request, Map.Entry<String, String>... params) {
 		for (Map.Entry<String, String> pair : params) {
 			if (pair != null) {
 				request.addQuerystringParameter(pair.getKey(), pair.getValue());
 			}
 		}
 	}
 
 	private void addBodyParams(OAuthRequest request, Map.Entry<String, String>... params) {
 		for (Map.Entry<String, String> pair : params) {
 			request.addBodyParameter(pair.getKey(), pair.getValue());
 		}
 	}
 
 	@Override
 	public void initClient(String pin) {
 		Verifier v = new Verifier(pin);
 		this.accesToken = this.service.getAccessToken(null, v);
 	}
 
 	@Override
 	public String getAuthUrl() {
 		return this.service.getAuthorizationUrl(null);
 	}
 
 	@Override
 	public String get(String url, Map.Entry<String, String>... params) throws OAuthConnectionException {
 		OAuthRequest request = new OAuthRequest(Verb.GET, url);
 		if (params != null) {
 			this.addUrlParams(request, params);
 		}
 		this.service.signRequest(this.accesToken, request);
 		Response r = request.send();
 		return r.getBody();
 	}
 
 	@Override
 	public String post(String url, Map.Entry<String, String>... params) {
 		OAuthRequest request = new OAuthRequest(Verb.POST, url);
 		if (params != null) {
 			this.addBodyParams(request, params);
 		}
 		this.service.signRequest(this.accesToken, request);
 		Response r = request.send();
 		return r.getBody(); // To change body of implemented methods use File |
 							// Settings | File Templates.
 	}
 
 	@Override
 	public String post(String url, byte[] payload, Map.Entry<String, String>... params) {
 		OAuthRequest request = new OAuthRequest(Verb.POST, url);
 		if (params != null) {
 			this.addBodyParams(request, params);
 		}
 
 		request.addPayload(payload);
 
 		request.addHeader("Content-Type", "application/json");
 		this.service.signRequest(this.accesToken, request);
 		Response r = request.send();
 		return r.getBody();
 	}
 
 	@Override
 	public String postMedia(String url, Lecture lecture, java.io.File file, Map.Entry<String, String>... params) throws IOException {
 		Entity entity = new Entity();
 		Log.i("FileNAME", file.getName());
 		Log.i("FileNAME", file.getAbsolutePath());
 		Log.i("FileNAME", file.getCanonicalPath());
 		Log.i("FileNAME", file.getParent());
		Log.i("TOKEN", this.accesToken.getToken());
 
 		entity.addPart("lecture[media]", file, URLConnection.guessContentTypeFromName(file.getName()));
 		entity.addPart("lecture[name]", lecture.name);
 		entity.addPart("lecture[type]", lecture.type);
 		MultipartRequest request = new MultipartRequest(url, this.accesToken.getToken());
 		request.setEntity(entity);
 		Response r = request.send();
 		if (r.getCode() != 201){
 			throw new IOException();
 		}
 		Log.i("RESPONSE", Integer.toString(r.getCode()));
 		return r.getBody();
 	}
 
 	@Override
 	public String postMedia(String url, java.io.File file, Map.Entry<String, String>... params) throws IOException {
 		Entity entity = new Entity();
 		entity.addPart("file[content]", file, URLConnection.guessContentTypeFromName(file.getName()));
 		MultipartRequest request = new MultipartRequest(url, this.accesToken.getToken());
 		request.setEntity(entity);
 		Response r = request.send();
 		if (r.getCode() != 201){
 			throw new IOException();
 		}
 		Log.i("RESPONSE", Integer.toString(r.getCode()));
 		return r.getBody();
 	}
 
 	@Override
 	public void delete(String url, Map.Entry<String, String>... params) {
 		OAuthRequest request = new OAuthRequest(Verb.DELETE, url);
 		if (params != null) {
 			this.addUrlParams(request, params);
 		}
 		this.service.signRequest(this.accesToken, request);
 		Response r = request.send();
 		if (!(r.getCode() == 204)) {
 			throw new DeleteException("Invalid return code", r.getCode());
 		}
 	}
 
 	@Override
 	public void put(String url, byte[] payload, Map.Entry<String, String>... params) {
 		OAuthRequest request = new OAuthRequest(Verb.PUT, url);
 		if (params != null) {
 			this.addBodyParams(request, params);
 		}
 		request.addPayload(payload);
 		request.addHeader("Content-Type", "application/json");
 		this.service.signRequest(this.accesToken, request);
 		Response r = request.send();
 		if (!(r.getCode() == 204)) {
 			throw new PutException("Invalid return code", r.getCode());
 		}
 
 	}
 
 	public byte[] read(File file) throws IOException {
 
 		/*
 		 * if ( file.length() > MAX_FILE_SIZE ) { throw new
 		 * FileTooBigException(file); }
 		 */
 
 		byte[] buffer = new byte[(int) file.length()];
 		InputStream ios = null;
 		try {
 			ios = new FileInputStream(file);
 			if (ios.read(buffer) == -1) {
 				throw new IOException("EOF reached while trying to read the whole file");
 			}
 		} finally {
 			try {
 				if (ios != null)
 					ios.close();
 			} catch (IOException e) {
 			}
 		}
 
 		return buffer;
 	}
 
 }
