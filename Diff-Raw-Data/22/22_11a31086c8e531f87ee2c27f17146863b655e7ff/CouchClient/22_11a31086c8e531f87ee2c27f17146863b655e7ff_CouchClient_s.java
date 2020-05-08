 package churndb.couch;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpDelete;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPut;
 import org.apache.http.client.methods.HttpUriRequest;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 import churndb.couch.response.CouchResponse;
 import churndb.couch.response.CouchResponseHandler;
 import churndb.couch.response.CouchResponseView;
 
 import com.google.gson.JsonElement;
 import com.google.gson.JsonObject;
 
 public class CouchClient {
 
 	private String couchdbHost;
 
 	private String database;
 
 	public CouchClient(String couchdbHost, String database) {
 		this.couchdbHost = normalizePath(couchdbHost);
 		this.database = normalizePath(database);
 	}
 
 	public CouchResponse welcome() {
 		return executeRequest(new HttpGet(couchdbHost));
 	}
 
 	public void create() {
 		executeRequest(new HttpPut(fullRequestUrl("")));
 	}
 
 	public void drop() {
 		delete("");
 	}
 
 	public CouchResponse get() {
 		return get("");
 	}
 
 	public CouchResponse get(String url) {
 		return executeRequest(new HttpGet(fullRequestUrl(url)));
 	}
 
 	public CouchResponse put(String url, String body) {
 		HttpPut request = new HttpPut(fullRequestUrl(url));
 
 		try {
 			request.setEntity(new StringEntity(body));
 		} catch (UnsupportedEncodingException e) {
 			throw new RuntimeException(e);
 		}
 
 		return executeRequest(request);
 	}
 
 	public CouchResponse delete(String url) {
 		return executeRequest(new HttpDelete(fullRequestUrl(url)));
 	}
 
 	private CouchResponse executeRequest(HttpUriRequest request) {
 		return executeRequest(request, new CouchResponseHandler(this));
 	}
 
 	private CouchResponse executeRequest(HttpUriRequest request, CouchResponseHandler handler) {
 		HttpClient httpclient = new DefaultHttpClient();
 
 		try {
 			return httpclient.execute(request, handler);
 
 		} catch (ClientProtocolException e) {
 			throw new RuntimeException(e);
 		} catch (IOException e) {
 			throw new RuntimeException(e);
 		} finally {
 			httpclient.getConnectionManager().shutdown();
 		}
 	}
 
 	private String fullRequestUrl(String url) {
 		StringBuilder sb = new StringBuilder(couchdbHost);
 
 		if (database != null) {
 			sb.append(database);
 		}
 
 		if (url != null) {
 			sb.append(url);
 		}
 
 		return sb.toString();
 	}
 
 	private String normalizePath(String host) {
 		if (host.charAt(host.length() - 1) != '/') {
 			return host + "/";
 		}
 		return host;
 	}
 
 	public void dropIfExists() {
 		CouchResponse response = get();
 		if (!response.objectNotFound()) {
 			drop();
 		}
 	}
 	
 	public CouchResponseView view(String viewUri, String... keys) {
 		HttpGet request = new HttpGet(fullRequestUrl(viewRequestUrl(viewUri, false, keys)));
 		return (CouchResponseView) executeRequest(request, new CouchResponseHandler(this, CouchResponseView.class));
 	}
 
 	private String viewRequestUrl(String viewUri, boolean reduce, String... keys) {
 		String[] split = viewUri.split("/");
 		String module = split[0];
 		String view = split[1];
 
 		StringBuilder normalizedKeys = new StringBuilder("?");
 		if (keys.length > 0) {
 			normalizedKeys.append(CouchUtils.keys(keys));
 		}
 		normalizedKeys.append("&reduce=" + reduce);
 
 		String viewRequestUrl = "_design/" + module + "/_view/" + view + normalizedKeys;
 		return viewRequestUrl;
 	}
 
 	public void put(DesignDocument designDocument) {
 		put(designDocument.getId(), designDocument.json());
 	}
 
 	public CouchResponse get(JsonElement jsonElement) {
 		return get(jsonElement.getAsString());
 	}
 
 	public String id() {
 		CouchResponse request = executeRequest(new HttpGet(couchdbHost + "_uuids"));
 		return request.json().get("uuids").getAsJsonArray().get(0).getAsString();
 	}
 
 	public CouchResponse viewGetFirst(String viewUri, String... keys) {
 		CouchResponseView response = view(viewUri, keys);
 		return get(response.first().get("id"));
 	}
 
 	public void viewDelete(String viewUri, String... keys) {
 		CouchResponseView response = view(viewUri, keys);
 		for (int i = 0; i < response.size(); i++) {
 			JsonObject row = response.json(i);
			// the view must emit doc._rev as value to be able use viewDelete
 			delete(row.get("id").getAsString() + "?rev=" + extractRevision(row.get("value")));
 		}
 	}
 
 	private String extractRevision(JsonElement value) {
 		if (value.isJsonArray()) {
 			return value.getAsJsonArray().get(0).getAsString();
 		}
 
 		if (value.isJsonObject()) {
 			return value.getAsJsonObject().get("_rev").getAsString();
 		}
 
 		return value.getAsString();
 	}
 
 	public CouchResponse viewGetAt(String viewUri, int index, String... keys) {
 		CouchResponseView response = view(viewUri, keys);
 		return get(response.json(index).get("id"));
 	}
 
 	public CouchResponseReduce reduce(String viewUri, String... keys) {
 		HttpGet request = new HttpGet(fullRequestUrl(viewRequestUrl(viewUri, true, keys)));
 		return (CouchResponseReduce) executeRequest(request, new CouchResponseHandler(this, CouchResponseReduce.class));
 	}
 
 	public CouchBean put(CouchBean bean) {
 		if (bean.get_id() == null) {
 			bean.set_id(id());
 		}
 		CouchResponse response = put(bean.get_id(), bean.json());
 		bean.set_rev(response.json().get("rev").getAsString());
 		
 		return bean;
 	}
 
 	public CouchBean put(String id, CouchBean bean) {
 		if(bean.get_id() == null) {
 			bean.set_id(id);
 		}
 		return put(bean);
 	}
 
 }
