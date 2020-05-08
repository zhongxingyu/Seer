 package com.piusvelte.hydra.client;
 
 import java.io.ByteArrayOutputStream;
import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.UnsupportedEncodingException;
 import java.math.BigInteger;
 import java.net.URI;
 import java.net.URLEncoder;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Properties;
 import java.util.Scanner;
 import java.util.Set;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpDelete;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.methods.HttpPut;
 import org.apache.http.client.methods.HttpUriRequest;
 import org.apache.http.client.utils.URIBuilder;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.json.simple.JSONArray;
 import org.json.simple.JSONObject;
 import org.json.simple.parser.JSONParser;
 import org.json.simple.parser.ParseException;
 
 public class HydraClient {
 
 	public static final String PARAM_DATABASE = "database";
 	public static final String PARAM_TOKEN = "token";
 	public static final String PARAM_VALUES = "values";
 	public static final String PARAM_COLUMNS = "columns";
 	public static final String PARAM_SELECTION = "selection";
 	public static final String PARAM_ARGUMENTS = "arguments";
 	public static final String PARAM_COMMAND = "command";
 	public static final String PARAM_QUEUEABLE = "queueable";
 	public static final String ACTION_ABOUT = "about";
 	public static final String ACTION_QUERY = "query";
 	public static final String ACTION_INSERT = "insert";
 	public static final String ACTION_UPDATE = "update";
 	public static final String ACTION_EXECUTE = "execute";
 	public static final String ACTION_SUBROUTINE = "subroutine";
 	public static final String ACTION_DELETE = "delete";
 
 	private static final String Sresult = "result";
 	private static final String Salias = "alias";
 	private static final String Stype = "type";
 	private static final String Shost = "host";
 	private static final String Sport = "port";
 	public static final String[] DATABASE_ATTRS = new String[]{Salias, Stype, Shost, Sport, PARAM_DATABASE};
 
 	private String host = "";
 	private static final int INVALID_PORT = 0;
 	private int port = INVALID_PORT;
 	private static final String CONTEXT = "/Hydra";
 	private static final String PATH_AUTH = "/auth";
 	private static final String PATH_API = "/api";
 
 	private String scheme = "";
 	private String token = "";
 	private String passphrase = null;
 
 	public HydraClient(String scheme, String host, int port, String passphrase, String token) {
 		this.scheme = scheme;
 		this.host = host;
 		this.port = port;
 		this.passphrase = passphrase;
		this.token = token;
 	}
 
 	// this can be run as a command line client
 	@SuppressWarnings("unchecked")
 	public static void main(String[] args) {
 		Scanner user_input = new Scanner(System.in);
 		String action = null;
 		String database = null;
 		String target = null;
 		ArrayList<String> columns = new ArrayList<String>();
 		ArrayList<String> values = new ArrayList<String>();
 		String selection = null;
 		boolean queueable = false;
 		String scheme = null;
 		String host = null;
 		String portStr = null;
 		int port = INVALID_PORT;
 		String passphrase = null;
 		String token = null;
 		// try to load a properties file
 		System.out.println("looking for properties file...");
 		String clientRoot = HydraClient.class.getProtectionDomain().getCodeSource().getLocation().getPath().substring(1);
 		String propsFile = clientRoot + ".properties";
 		Properties properties = new Properties();
 		FileInputStream in = null;
 		try {
 			in = new FileInputStream(propsFile);
 			properties.load(in);
 		} catch (FileNotFoundException e) {
 			properties = null;
 			e.printStackTrace();
 		} catch (IOException e) {
 			properties = null;
 			e.printStackTrace();
 		} finally {
 			if (in != null) {
 				try {
 					in.close();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 		if (properties != null) {
 			if (properties.containsKey("scheme"))
 				scheme = properties.getProperty("scheme");
 			if (properties.containsKey("host"))
 				host = properties.getProperty("host");
 			if (properties.containsKey("port")) {
 				try {
 					port = Integer.parseInt(properties.getProperty("port"));
 				} catch (NumberFormatException e) {
 				}
 			}
 			if (properties.containsKey("passphrase"))
 				passphrase = properties.getProperty("passphrase");
			if (properties.containsKey("token"))
 				token = properties.getProperty("token");
 		}
		String save = null;
 		if ((host == null) || (port == INVALID_PORT) || (passphrase == null)) {
 			System.out.println("Unable to load properties.");
 			System.out.print("Scheme[http]: ");
 			scheme = user_input.nextLine();
 			if ((scheme == null) || (scheme.length() == 0))
 				scheme = "http";
 			System.out.print("Host[localhost]: ");
 			host = user_input.nextLine();
 			if ((host == null) || (host.length() == 0))
 				host = "localhost";			
 			System.out.print("Port[80]:");
 			portStr = user_input.nextLine();
 			port = INVALID_PORT;
 			if (portStr != null) {
 				try {
 					port = Integer.parseInt(portStr);
 				} catch (NumberFormatException e) {
 					port = INVALID_PORT;
 				}
 			} else
 				port = 80;
 			System.out.print("Passphrase:");
 			passphrase = user_input.nextLine();
 			System.out.print("Attempt to save properties? null to skip:");
 			save = user_input.nextLine();
 			if (save != null) {
 				if (properties == null)
 					properties = new Properties();
 				properties.setProperty("scheme", scheme);
 				properties.setProperty("host", host);
 				properties.setProperty("port", Integer.toString(port));
 				properties.setProperty("passphrase", passphrase);
 				properties.setProperty("token", "");
 				FileOutputStream out = null;
 				try {
 					out = new FileOutputStream(propsFile, false);
 					properties.store(out, "");
 				} catch (FileNotFoundException e) {
 					e.printStackTrace();
 				} catch (IOException e) {
 					e.printStackTrace();
 				} finally {
 					if (out != null) {
 						try {
 							out.close();
 						} catch (IOException e) {
 							e.printStackTrace();
 						}
 					}
 				}
 			}
 		}
 		HydraClient hydraClient = new HydraClient(scheme, host, port, passphrase, token);
 		if (token == null) {
 			System.out.println("getting a token");
 			try {
 				token = hydraClient.getUnauthorizedToken();
				if (hydraClient.authorizeToken(token));
 				System.out.println("token: " + token);
 				hydraClient.setToken(token);
 			} catch (Exception e) {
 				e.printStackTrace();
 				return;
 			}
 			if (save != null) {
 				System.out.println("saving the token");
 				properties.setProperty("token", token);
 				FileOutputStream out = null;
 				try {
 					out = new FileOutputStream(propsFile, false);
 					properties.store(out, "");
 				} catch (FileNotFoundException e) {
 					e.printStackTrace();
 				} catch (IOException e) {
 					e.printStackTrace();
 				} finally {
 					if (out != null) {
 						try {
 							out.close();
 						} catch (IOException e) {
 							e.printStackTrace();
 						}
 					}
 				}
 			}
 		}
 		if (hydraClient != null) {
 			System.out.print("Action, or null to exit:");
 			action = user_input.nextLine();
 			while ((action != null) && (action.length() > 0)) {
 				System.out.print("Database:");
 				database = user_input.nextLine();
 				if ((database != null) && (database.length() > 0)) {
 					System.out.print("Target:");
 					target = user_input.nextLine();
 					if ((target != null) && (target.length() > 0)) {
 						System.out.println("Enter columns...");
 						System.out.print("column:");
 						String column = user_input.nextLine();
 						while ((column != null) && (column.length() > 0)) {
 							if (column.equals("\"\""))
 								columns.add("");
 							else
 								columns.add(column);
 							System.out.print("column:");
 							column = user_input.nextLine();
 						}
 						System.out.println("Enter values...");
 						System.out.print("value:");
 						String value = user_input.nextLine();
 						while ((value != null) && (value.length() > 0)) {
 							if (value.equals("\"\""))
 								values.add("");
 							else
 								values.add(value);
 							System.out.print("value:");
 							value = user_input.nextLine();
 						}
 						System.out.print("Selection:");
 						selection = user_input.nextLine();
 						System.out.print("Queueable (true,false):");
 						String queueableStr = user_input.nextLine();
 						queueable = Boolean.parseBoolean(queueableStr);
 					}
 				}
 				if (ACTION_ABOUT.equals(action)) {
 					if ((database == null) || (database.length() == 0)) {
 						try {
 							String[] databases = hydraClient.getDatabases();
 							for (String db : databases)
 								System.out.println("database: " + db);
 						} catch (Exception e) {
 							e.printStackTrace();
 						}
 					} else {
 						try {
 							HashMap<String, String> databaseInfo = hydraClient.getDatabase(database);
 							Set<String> keys = databaseInfo.keySet();
 							for (String key : keys)
 								System.out.println(key + ": " + databaseInfo.get(key));
 						} catch (Exception e) {
 							e.printStackTrace();
 						}
 					}
 				} else if (ACTION_EXECUTE.equals(action)) {
 					try {
 						JSONObject response = hydraClient.execute(database, target, queueable);
 						if (response.containsKey("result")) {
 							JSONArray vmValues = (JSONArray) response.get("result");
 							for (int i = 0, l = vmValues.size(); i < l; i++) {
 								JSONArray svmValues = (JSONArray) vmValues.get(i);
 								for (int i2 = 0, l2 = svmValues.size(); i2 < l2; i2++)
 									System.out.println(svmValues.get(i2));
 							}
 						} else
 							System.out.println("no result");
 					} catch (Exception e) {
 						e.printStackTrace();
 					}
 				} else if (ACTION_SUBROUTINE.equals(action)) {
 					String[] valuesArr = new String[values.size()];
 					for (int i = 0, l = valuesArr.length; i < l; i++)
 						valuesArr[i] = values.get(i);
 					try {
 						JSONObject response = hydraClient.subroutine(database, target, valuesArr, queueable);
 						if (response.containsKey("result")) {
 							JSONArray vmValues = (JSONArray) response.get("result");
 							for (int i = 0, l = vmValues.size(); i < l; i++) {
 								JSONArray svmValues = (JSONArray) vmValues.get(i);
 								for (int i2 = 0, l2 = svmValues.size(); i2 < l2; i2++)
 									System.out.println(svmValues.get(i2));
 							}
 						} else
 							System.out.println("no result");
 					} catch (Exception e) {
 						e.printStackTrace();
 					}
 				} else if (ACTION_QUERY.equals(action)) {
 					String[] columnsArr = new String[columns.size()];
 					for (int i = 0, l = columnsArr.length; i < l; i++)
 						columnsArr[i] = columns.get(i);
 					try {
 						JSONObject response = hydraClient.query(database, target, columnsArr, selection, queueable);
 						if (response.containsKey("result")) {
 							JSONArray rowsJArr = (JSONArray) response.get("result");
 							for (int r = 0, rl = rowsJArr.size(); r < rl; r++) {
 								JSONObject columnsObj = (JSONObject) rowsJArr.get(r);
 								Set<String> columnsNames = columnsObj.keySet();
 								for (String columnName : columnsNames)
 									System.out.println(columnName + "= " + ((String) columnsObj.get(columnName)));
 							}
 						}
 					} catch (Exception e) {
 						e.printStackTrace();
 					}
 				} else if (ACTION_INSERT.equals(action)) {
 					String[] columnsArr = new String[columns.size()];
 					for (int i = 0, l = columnsArr.length; i < l; i++)
 						columnsArr[i] = columns.get(i);
 					String[] valuesArr = new String[values.size()];
 					for (int i = 0, l = valuesArr.length; i < l; i++)
 						valuesArr[i] = values.get(i);
 					try {
 						JSONObject response = hydraClient.insert(database, target, columnsArr, valuesArr, queueable);
 						if (response.containsKey("result"))
 							System.out.println(response.get("result"));
 					} catch (Exception e) {
 						e.printStackTrace();
 					}
 				} else if (ACTION_UPDATE.equals(action)) {
 					String[] columnsArr = new String[columns.size()];
 					for (int i = 0, l = columnsArr.length; i < l; i++)
 						columnsArr[i] = columns.get(i);
 					String[] valuesArr = new String[values.size()];
 					for (int i = 0, l = valuesArr.length; i < l; i++)
 						valuesArr[i] = values.get(i);
 					try {
 						JSONObject response = hydraClient.update(database, target, columnsArr, valuesArr, selection, queueable);
 						if (response.containsKey("result"))
 							System.out.println(response.get("result"));
 					} catch (Exception e) {
 						e.printStackTrace();
 					}
 				}
 				System.out.println("Action, or null to exit:");
 				action = user_input.nextLine();
 			}
 		}
 	}
 
 	private HttpClient httpClient = new DefaultHttpClient();
 	private JSONParser jsonParser = new JSONParser();
 
 	private String getHttpEntity(HttpUriRequest request) throws Exception {
 		HttpResponse response = httpClient.execute(request);
 		HttpEntity entity = response.getEntity();
 		if (entity != null) {
 			InputStream is = entity.getContent();
 			ByteArrayOutputStream content = new ByteArrayOutputStream();
 			byte[] sBuffer = new byte[512];
 			int readBytes = 0;
 			while ((readBytes = is.read(sBuffer)) != -1)
 				content.write(sBuffer, 0, readBytes);
			//TODO: remove the following line
			System.out.println(new String(content.toByteArray()));
 			return new String(content.toByteArray());
 		} else
 			throw new Exception("response is empty");
 	}
 
 	public void setToken(String token) {
		this.token = token;
	}

	public String getToken() {
		return token;
 	}
 
 	public String getUnauthorizedToken() throws Exception {
 		URIBuilder builder = new URIBuilder();
 		builder.setScheme(scheme).setHost(host).setPath(CONTEXT + PATH_AUTH);
 		if (port > INVALID_PORT)
 			builder.setPort(port);
 		URI uri = builder.build();
 		HttpGet httpGet = new HttpGet(uri);
 		JSONObject result = (JSONObject) jsonParser.parse(getHttpEntity(httpGet));
 		return (String) result.get(Sresult);
 	}
 
 	public boolean authorizeToken(String token) throws Exception {
 		URIBuilder builder = new URIBuilder();
 		builder.setScheme(scheme).setHost(host).setPath(CONTEXT + PATH_AUTH).setParameter(PARAM_TOKEN, getHash64(token + passphrase));
 		if (port > INVALID_PORT)
 			builder.setPort(port);
 		URI uri = builder.build();
 		HttpGet httpGet = new HttpGet(uri);
 		JSONObject result = (JSONObject) jsonParser.parse(getHttpEntity(httpGet));
 		return !result.containsKey("errors");
 	}
 
 	private static String getHash64(String in) {
 		String out = null;
 		MessageDigest md;
 		try {
 			md = MessageDigest.getInstance("SHA-256");
 			md.update(in.getBytes("UTF-8"));
 			out = new BigInteger(1, md.digest()).toString(16);
 			StringBuffer hexString = new StringBuffer();
 			byte[] hash = md.digest();
 			for (byte b : hash) {
 				if ((0xFF & b) < 0x10)
 					hexString.append("0" + Integer.toHexString((0xFF & b)));
 				else
 					hexString.append(Integer.toHexString(0xFF & b));
 			}
 			out = hexString.toString();
 			if (out.length() > 64)
 				return out.substring(0, 64);
 		} catch (NoSuchAlgorithmException e) {
 			e.printStackTrace();
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 		}
 		return out;
 	}
 
 	public URI buildURI(String database, String entity, String[] columns, String[] values, String selection, boolean queueable) throws ParseException, Exception {
 		URIBuilder builder = new URIBuilder();
 		builder
 		.setScheme(scheme)
 		.setHost(host)
 		.setPath(CONTEXT + PATH_API + (database != null ? "/" + database + (entity != null ? "/" + entity : "") : ""))
 		.setParameter(PARAM_TOKEN, token)
 		.setParameter(PARAM_QUEUEABLE, Boolean.toString(queueable));
 		if (port > INVALID_PORT)
 			builder.setPort(port);
 		if (columns != null) {
 			for (int i = 0; i < columns.length; i++)
 				builder.setParameter("columns[" + i + "]", URLEncoder.encode(columns[i], "UTF-8"));
 		}
 		if (values != null) {
 			for (int i = 0; i < values.length; i++)
 				builder.setParameter("values[" + i + "]", URLEncoder.encode(values[i], "UTF-8"));
 		}
 		if (selection != null)
 			builder.setParameter(PARAM_SELECTION, URLEncoder.encode(selection, "UTF-8"));
 		return builder.build();
 	}
 
 	public URI buildURI(String database, String entity, String[] arguments, boolean queueable) throws ParseException, Exception {
 		URIBuilder builder = new URIBuilder();
 		builder
 		.setScheme(scheme)
 		.setHost(host)
 		.setPath(CONTEXT + PATH_API + (database != null ? "/" + database + (entity != null ? "/" + entity : "") : ""))
 		.setParameter(PARAM_TOKEN, token)
 		.setParameter(PARAM_QUEUEABLE, Boolean.toString(queueable));
 		if (port > INVALID_PORT)
 			builder.setPort(port);
 		if (arguments != null) {
 			for (int i = 0; i < arguments.length; i++)
 				builder.setParameter("arguments[" + i + "]", URLEncoder.encode(arguments[i], "UTF-8"));
 		}
 		return builder.build();
 	}
 
 	public URI buildURI(String database, String command, boolean queueable) throws ParseException, Exception {
 		URIBuilder builder = new URIBuilder();
 		builder
 		.setScheme(scheme)
 		.setHost(host)
 		.setPath(CONTEXT + PATH_API + (database != null ? "/" + database : ""))
 		.setParameter(PARAM_TOKEN, token)
 		.setParameter(PARAM_COMMAND, URLEncoder.encode(command, "UTF-8"))
 		.setParameter(PARAM_QUEUEABLE, Boolean.toString(queueable));
 		if (port > INVALID_PORT)
 			builder.setPort(port);
 		return builder.build();
 	}
 
 	@SuppressWarnings("unchecked")
 	public String[] getDatabases() throws Exception {
 		JSONObject jobj = query(null, null, null, null, false);
 		JSONArray arr = (JSONArray) jobj.get(Sresult);
 		String[] databases;
 		if (arr != null) {
 			databases = new String[arr.size()];
 			for (int i = 0, l = databases.length; i < l; i++)
 				databases[i] = (String) arr.get(i);
 		} else
 			databases = new String[0];
 		return databases;
 	}
 
 	@SuppressWarnings("unchecked")
 	public HashMap<String, String> getDatabase(String database) throws Exception {
 		JSONObject jobj = query(database, null, null, null, false);
 		HashMap<String, String> db = new HashMap<String, String>();
 		JSONObject dbObj = (JSONObject) jobj.get(Sresult);
 		for (String attr : DATABASE_ATTRS) {
 			if (dbObj.containsKey(attr))
 				db.put(attr, (String) dbObj.get(attr));
 			else
 				db.put(attr, null);
 		}
 		return db;
 	}
 
 	@SuppressWarnings("unchecked")
 	public JSONObject execute(String database, String command, boolean queueable) throws Exception {
 		return (JSONObject) jsonParser.parse(getHttpEntity(new HttpPost(buildURI(database, command, queueable))));
 	}
 
 	@SuppressWarnings("unchecked")
 	public JSONObject query(String database, String entity, String[] columns, String selection, boolean queueable) throws Exception {
 		return (JSONObject) jsonParser.parse(getHttpEntity(new HttpGet(buildURI(database, entity, columns, null, selection, queueable))));
 	}
 
 	@SuppressWarnings("unchecked")
 	public JSONObject update(String database, String entity, String[] columns, String[] values, String selection, boolean queueable) throws Exception {
 		return (JSONObject) jsonParser.parse(getHttpEntity(new HttpPut(buildURI(database, entity, columns, values, selection, queueable))));
 	}
 
 	@SuppressWarnings("unchecked")
 	public JSONObject insert(String database, String entity, String[] columns, String[] values, boolean queueable) throws Exception {
 		return (JSONObject) jsonParser.parse(getHttpEntity(new HttpPost(buildURI(database, entity, columns, values, null, queueable))));
 	}
 
 	@SuppressWarnings("unchecked")
 	public JSONObject delete(String database, String entity, String selection, boolean queueable) throws Exception {
 		return (JSONObject) jsonParser.parse(getHttpEntity(new HttpDelete(buildURI(database, entity, null, null, selection, queueable))));
 	}
 
 	@SuppressWarnings("unchecked")
 	public JSONObject subroutine(String database, String entity, String[] values, boolean queueable) throws Exception {
 		return (JSONObject) jsonParser.parse(getHttpEntity(new HttpPost(buildURI(database, entity, values, queueable))));
 	}
 
 	String[] parseArray(JSONArray jarr) {
 		String[] sArr;
 		if (jarr == null)
 			sArr = new String[0];
 		else
 			sArr = new String[jarr.size()];
 		for (int i = 0, l = sArr.length; i < l; i++)
 			sArr[i] = (String) jarr.get(i);
 		return sArr;
 	}
 
 }
