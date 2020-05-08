 package ru.redsolution.rosyama;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.UnsupportedEncodingException;
 import java.nio.charset.Charset;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.mime.HttpMultipartMode;
 import org.apache.http.entity.mime.MIME;
 import org.apache.http.entity.mime.MultipartEntity;
 import org.apache.http.entity.mime.content.AbstractContentBody;
 import org.apache.http.entity.mime.content.ContentBody;
 import org.apache.http.entity.mime.content.FileBody;
 import org.apache.http.entity.mime.content.StringBody;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.protocol.HTTP;
 import org.apache.http.util.EntityUtils;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.json.JSONTokener;
 
 import android.app.Application;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.location.Location;
 import android.location.LocationManager;
import android.os.Environment;
 import android.preference.PreferenceManager;
 
 public class Rosyama extends Application {
 	private static final Pattern CSRF_PATTERN = Pattern.compile(
 			".*name=\'csrfmiddlewaretoken\' value=\'(.+?)\'.*", Pattern.DOTALL);
 	private static final Pattern LOGIN_PATTERN = Pattern.compile(
 			".*name=\"USER_LOGIN\" maxlength=\"255\" value=\"(.*?)\" />.*",
 			Pattern.DOTALL);
 	private static final Pattern ID_PATTERN = Pattern
 			.compile(".*/(\\d+?)/\\?.*");
 
 	private static final String PDF_PATTERN_TEMPLATE = ".*id=\"pdf_form_%1$s\" name=\"%1$s\">(.*?)</textarea>.*";
 	private static final String[] PDF_PATTERN_FIELDS = new String[] { "to",
 			"from", "postaddress", "address", "signature" };
 	private static final Pattern PDF_PATTERN_SIGNATURE = Pattern
 			.compile(
 					".*id=\"pdf_form_signature\" name=\"signature\" value=\"(.*?)\">.*",
 					Pattern.DOTALL);
 	private static final Map<String, Pattern> PDF_PATTERNS = new HashMap<String, Pattern>();
 
 	private static final String YANDEX_KEY = "ACG3DU0BAAAA6wkJGgQAgRY2ChXSECRoRN9yDmLrz_wSRYQAAAAAAAAAAABC1YjXH-X5KWN_NyvBCtLgrvoZSg==";
 	private static final String YANDEX_URL = "http://geocode-maps.yandex.ru/1.x/?format=json&key=%s&geocode=%s%%2C%s&results=1";
 
 	static {
 		PDF_PATTERNS.put("signature", PDF_PATTERN_SIGNATURE);
 		for (String field : PDF_PATTERN_FIELDS)
 			if (!PDF_PATTERNS.containsKey(field))
 				PDF_PATTERNS.put(field, Pattern.compile(
 						String.format(PDF_PATTERN_TEMPLATE, field),
 						Pattern.DOTALL));
 	}
 
 	private static final String LOGIN = "http://rosyama.ru/personal/holes.php";
 	// http://www.foryou-vip.ru:8000/journal/login/
 	private static final String ADD = "http://rosyama.ru/personal/add.php";
 	private static final String PDF = "http://rosyama.ru/%s/?pdf";
 
 	public final static SimpleDateFormat FILE_NAME_FORMAT = new SimpleDateFormat(
 			"yyyy-MM-dd-HH-mm-ss");
 
 	/**
 	 * CSRF for this session.
 	 */
 	private String csrf;
 
 	/**
 	 * Used client.
 	 */
 	private RedirctedHttpClient client;
 
 	/**
 	 * Used login.
 	 */
 	private String login;
 
 	/**
 	 * Used password.
 	 */
 	private String password;
 
 	/**
 	 * Used path to the photo.
 	 */
 	private String path;
 
 	/**
 	 * Used location.
 	 */
 	private Location location;
 
 	/**
 	 * Used address.
 	 */
 	private String address;
 
 	/**
 	 * Used city.
 	 */
 	private String locality;
 
 	/**
 	 * Used area.
 	 */
 	private String area;
 
 	/**
 	 * Used comment.
 	 */
 	private String comment;
 
 	/**
 	 * Used hole's id.
 	 */
 	private String id;
 
 	/**
 	 * Attributes used to get pdf.
 	 */
 	private Map<String, String> attrs;
 
 	/**
 	 * Settings.
 	 */
 	private SharedPreferences settings;
 
 	/**
 	 * Result pdf file.
 	 */
	private File pdf;
 
 	public Rosyama() {
 		csrf = null;
 		client = null;
 		login = null;
 		password = null;
 		path = null;
 		location = null;
 		id = null;
 		attrs = new HashMap<String, String>();
 		pdf = null;
 		address = "";
 		locality = "";
 		area = "";
 		comment = "";
 	}
 
 	@Override
 	public void onCreate() {
 		super.onCreate();
 		settings = PreferenceManager
 				.getDefaultSharedPreferences(getBaseContext());
 		login = settings.getString(getString(R.string.login_key), "");
 		password = settings.getString(getString(R.string.password_key), "");
 	}
 
 	/**
 	 * Whether there login was set.
 	 * 
 	 * @return
 	 */
 	public boolean hasLogin() {
 		return !"".equals(login);
 	}
 
 	/**
 	 * Sets path to the photo.
 	 * 
 	 * @param path
 	 * @return
 	 */
 	public boolean setPhoto(String path) {
 		this.path = path;
 		this.id = null;
 
 		Location gps = ((LocationManager) getSystemService(Context.LOCATION_SERVICE))
 				.getLastKnownLocation(android.location.LocationManager.GPS_PROVIDER);
 		Location net = ((LocationManager) getSystemService(Context.LOCATION_SERVICE))
 				.getLastKnownLocation(android.location.LocationManager.NETWORK_PROVIDER);
 		Location best = net;
 		if (gps != null && net != null) {
 			if (gps.getTime() > net.getTime())
 				best = gps;
 			else if (net.hasAccuracy()) {
 				float[] results = new float[1];
 				Location.distanceBetween(net.getLatitude(), net.getLongitude(),
 						gps.getLatitude(), gps.getLongitude(), results);
 				if (results[0] < net.getAccuracy())
 					best = gps;
 			}
 
 		} else if (net == null)
 			best = gps;
 		return setPosition(best);
 	}
 
 	/**
 	 * Whether there is selected photo.
 	 * 
 	 * @return
 	 */
 	public boolean hasPhoto() {
 		return path != null;
 	}
 
 	public String getArea() {
 		return area;
 	}
 
 	public String getLocality() {
 		return locality;
 	}
 
 	public String getAddress() {
 		return address;
 	}
 
 	public String getComment() {
 		return comment;
 	}
 
 	/**
 	 * Sets location.
 	 * 
 	 * @param location
 	 * @return
 	 */
 	public boolean setPosition(Location location) {
 		this.location = location;
 		if (location == null) {
 			this.location = new Location("GPS");
 			this.location.setLatitude(55.693163);
 			this.location.setLongitude(37.707086);
 			area = "";
 			locality = "";
 			address = "";
 			comment = "";
 			// return false;
 		}
 		RedirctedHttpClient client = new RedirctedHttpClient();
 		HttpGet get;
 		HttpResponse response;
 		HttpEntity entity;
 		String content;
 		boolean done = false;
 		try {
 			get = new HttpGet(String.format(YANDEX_URL, YANDEX_KEY,
 					getPoint(this.location.getLongitude()),
 					getPoint(this.location.getLatitude())));
 			response = client.execute(get);
 			System.out
 					.println("location form get: " + response.getStatusLine());
 			entity = response.getEntity();
 			if (entity != null) {
 				content = EntityUtils.toString(entity);
 				FileOutputStream stream = openFileOutput(
 						FILE_NAME_FORMAT.format(new Date()) + ".json",
 						Context.MODE_WORLD_WRITEABLE);
 				stream.write(content.getBytes());
 				Object result = new JSONTokener(content).nextValue();
 				JSONArray array = (JSONArray) ((JSONObject) ((JSONObject) ((JSONObject) result)
 						.get("response")).get("GeoObjectCollection"))
 						.get("featureMember");
 				if (array.length() == 1) {
 					JSONObject object = (JSONObject) (JSONObject) ((JSONObject) array
 							.get(0)).get("GeoObject");
 					this.address = object.getString("name");
 					JSONObject country = (JSONObject) ((JSONObject) ((JSONObject) ((JSONObject) object
 							.get("metaDataProperty")).get("GeocoderMetaData"))
 							.get("AddressDetails")).get("Country");
 					JSONObject area;
 					try {
 						area = (JSONObject) country.get("AdministrativeArea");
 						this.area = area.getString("AdministrativeAreaName");
 					} catch (JSONException e) {
 						area = country;
 						this.area = "";
 					}
 					try {
 						JSONObject locality = (JSONObject) area.get("Locality");
 						this.locality = locality.getString("LocalityName");
 					} catch (JSONException e) {
 						this.locality = "";
 					}
 					done = true;
 				}
 			}
 			if (entity != null)
 				entity.consumeContent();
 		} catch (ClientProtocolException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 		if (done) {
 			return true;
 		} else {
 			location = null;
 			area = "";
 			locality = "";
 			address = "";
 			comment = "";
 			return false;
 		}
 	}
 
 	/**
 	 * Whether there is selected photo.
 	 * 
 	 * @return
 	 */
 	public boolean hasId() {
 		return id != null;
 	}
 
 	/**
 	 * Returns result pdf file.
 	 * 
 	 * @return
 	 */
	public File getPdf() {
 		return pdf;
 	}
 
 	/**
 	 * Logins with current login and password.
 	 * 
 	 * @return
 	 */
 	public boolean login() {
 		return login(login, password);
 	}
 
 	/**
 	 * Logins with new login and password. Save login and password on success.
 	 * 
 	 * @param login
 	 * @param password
 	 * @return
 	 */
 	public boolean login(String login, String password) {
 		client = new RedirctedHttpClient();
 		HttpGet get;
 		HttpPost post;
 		HttpResponse response;
 		HttpEntity entity;
 		Matcher matcher;
 		String content;
 		boolean done = false;
 		try {
 			get = new HttpGet(LOGIN);
 			response = client.execute(get);
 			System.out.println("Login form get: " + response.getStatusLine());
 			entity = response.getEntity();
 			matcher = CSRF_PATTERN.matcher(EntityUtils.toString(entity));
 			if (matcher.matches())
 				csrf = matcher.group(1);
 			else
 				csrf = null;
 			if (entity != null)
 				entity.consumeContent();
 
 			post = new HttpPost(LOGIN);
 			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
 			if (csrf == null) {
 				nameValuePairs.add(new BasicNameValuePair("AUTH_FORM", "Y"));
 				nameValuePairs.add(new BasicNameValuePair("TYPE", "AUTH"));
 				nameValuePairs.add(new BasicNameValuePair("backurl",
 						"/personal/holes.php"));
 				nameValuePairs.add(new BasicNameValuePair("USER_LOGIN", login));
 				nameValuePairs.add(new BasicNameValuePair("USER_PASSWORD",
 						password));
 				nameValuePairs.add(new BasicNameValuePair("Login", "Войти"));
 			} else {
 				nameValuePairs.add(new BasicNameValuePair(
 						"csrfmiddlewaretoken", csrf));
 				nameValuePairs.add(new BasicNameValuePair("USERNAME", login));
 				nameValuePairs
 						.add(new BasicNameValuePair("PASSWORD", password));
 			}
 			UrlEncodedFormEntity encodedFormEntity = new UrlEncodedFormEntity(
 					nameValuePairs, HTTP.UTF_8);
 			FileOutputStream stream = openFileOutput(
 					FILE_NAME_FORMAT.format(new Date()) + ".post",
 					Context.MODE_WORLD_WRITEABLE);
 			encodedFormEntity.writeTo(stream);
 			post.setEntity(encodedFormEntity);
 			response = client.execute(post);
 			System.out.println("Login form post: " + response.getStatusLine());
 			entity = response.getEntity();
 			if (entity != null) {
 				content = EntityUtils.toString(entity);
 				stream = openFileOutput(FILE_NAME_FORMAT.format(new Date())
 						+ ".html", Context.MODE_WORLD_WRITEABLE);
 				stream.write(content.getBytes());
 				if (!LOGIN_PATTERN.matcher(content).matches())
 					done = true;
 			}
 			if (entity != null)
 				entity.consumeContent();
 		} catch (ClientProtocolException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		if (done) {
 			this.login = login;
 			this.password = password;
 			SharedPreferences.Editor editor = settings.edit();
 			editor.putString(getString(R.string.login_key), login);
 			editor.putString(getString(R.string.password_key), password);
 			editor.commit();
 			return true;
 		} else {
 			shutdown();
 			return false;
 		}
 	}
 
 	private static class EmptyFileBody extends AbstractContentBody {
 		public EmptyFileBody() {
 			super("application/octet-stream");
 		}
 
 		@Override
 		public void writeTo(OutputStream out) throws IOException {
 			// Do noting
 		}
 
 		@Override
 		public String getCharset() {
 			return null;
 		}
 
 		@Override
 		public long getContentLength() {
 			return 0;
 		}
 
 		@Override
 		public String getTransferEncoding() {
 			return MIME.ENC_BINARY;
 		}
 
 		@Override
 		public String getFilename() {
 			return "";
 		}
 	}
 
 	public boolean add(String area, String locality, String address,
 			String comment) {
 		this.area = area;
 		this.locality = locality;
 		this.address = address;
 		this.comment = comment;
 		HttpPost post;
 		HttpResponse response;
 		HttpEntity entity;
 		Matcher matcher;
 		String content;
 		boolean done = false;
 		try {
 			post = new HttpPost(ADD);
 			File file = new File(path);
 			MultipartEntity multipartEntity = new MultipartEntity(
 					HttpMultipartMode.BROWSER_COMPATIBLE);
 			// , null, Charset.forName(HTTP.UTF_8));
 			ContentBody contentBody = new FileBody(file, "image/jpeg");
 			String ID = "0";
 			String type = "holeonroad";
 			String mapaddress = locality + address;
 			String coordinates = getPoint(location.getLongitude()) + ","
 					+ getPoint(location.getLatitude());
 			multipartEntity.addPart("ID",
 					new StringBody(ID, Charset.forName(HTTP.UTF_8)));
 			multipartEntity.addPart("TYPE",
 					new StringBody(type, Charset.forName(HTTP.UTF_8)));
 			multipartEntity.addPart("ADDRESS",
 					new StringBody(address, Charset.forName(HTTP.UTF_8)));
 			multipartEntity.addPart("PHOTOS_0", contentBody);
 			multipartEntity.addPart("PHOTOS_1", new EmptyFileBody());
 			multipartEntity.addPart("PHOTOS_2", new EmptyFileBody());
 			multipartEntity.addPart("PHOTOS_3", new EmptyFileBody());
 			multipartEntity.addPart("PHOTOS_4", new EmptyFileBody());
 			multipartEntity.addPart("PHOTOS_5", new EmptyFileBody());
 			multipartEntity.addPart("PHOTOS_6", new EmptyFileBody());
 			multipartEntity.addPart("PHOTOS_7", new EmptyFileBody());
 			multipartEntity.addPart("PHOTOS_8", new EmptyFileBody());
 			multipartEntity.addPart("PHOTOS_9", new EmptyFileBody());
 			multipartEntity.addPart("COMMENT",
 					new StringBody(comment, Charset.forName(HTTP.UTF_8)));
 			multipartEntity.addPart("mapaddress", new StringBody(mapaddress,
 					Charset.forName(HTTP.UTF_8)));
 			multipartEntity.addPart("adr_subjectrf", new StringBody(area,
 					Charset.forName(HTTP.UTF_8)));
 			multipartEntity.addPart("adr_city", new StringBody(locality,
 					Charset.forName(HTTP.UTF_8)));
 			multipartEntity.addPart("COORDINATES", new StringBody(coordinates,
 					Charset.forName(HTTP.UTF_8)));
 			FileOutputStream stream = openFileOutput(
 					FILE_NAME_FORMAT.format(new Date()) + ".post",
 					Context.MODE_WORLD_WRITEABLE);
 			multipartEntity.writeTo(stream);
 			post.setEntity(multipartEntity);
 			response = client.execute(post);
 			System.out.println("File form post: " + post.getRequestLine());
 			if (client.getRedirects().size() != 1)
 				throw new IOException("Redirect missed.");
 			matcher = ID_PATTERN.matcher(client.getRedirects().iterator()
 					.next());
 			if (!matcher.matches())
 				throw new IOException("Illegal redirect.");
 			id = matcher.group(1);
 			System.out.println("ID: " + id);
 			entity = response.getEntity();
 			if (entity != null) {
 				content = EntityUtils.toString(entity);
 				stream = openFileOutput(FILE_NAME_FORMAT.format(new Date())
 						+ ".html", Context.MODE_WORLD_WRITEABLE);
 				stream.write(content.getBytes());
 				done = true;
 				attrs.clear();
 				for (Entry<String, Pattern> entry : PDF_PATTERNS.entrySet()) {
 					matcher = entry.getValue().matcher(content);
 					if (matcher.matches()) {
 						attrs.put(entry.getKey(), matcher.group(1));
 						System.out.println(entry.getKey() + ": "
 								+ matcher.group(1));
 					} else {
 						System.out.println(entry.getKey() + ": not found");
 						done = false;
 					}
 				}
 			}
 			if (entity != null)
 				entity.consumeContent();
 		} catch (ClientProtocolException e) {
 			e.printStackTrace();
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		if (done) {
 			return true;
 		} else {
 			shutdown();
 			return false;
 		}
 	}
 
 	public boolean pdf() {
 		HttpPost post;
 		HttpResponse response;
 		HttpEntity entity;
 		boolean done = false;
 		try {
 			post = new HttpPost(String.format(PDF, id));
 			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
 			for (String field : PDF_PATTERN_FIELDS)
 				nameValuePairs.add(new BasicNameValuePair(field, attrs
 						.get(field)));
 			UrlEncodedFormEntity encodedFormEntity = new UrlEncodedFormEntity(
 					nameValuePairs, HTTP.UTF_8);
 			FileOutputStream stream = openFileOutput(
 					FILE_NAME_FORMAT.format(new Date()) + ".post",
 					Context.MODE_WORLD_WRITEABLE);
 			encodedFormEntity.writeTo(stream);
 			post.setEntity(encodedFormEntity);
 			response = client.execute(post);
 			System.out.println("PDF form post: " + response.getStatusLine());
 			entity = response.getEntity();
 			if (entity != null) {
				pdf = new File(Environment.getExternalStorageDirectory(), id
						+ ".pdf");
				stream = new FileOutputStream(pdf);
 				entity.writeTo(stream);
 				done = true;
 			}
 			if (entity != null)
 				entity.consumeContent();
 		} catch (ClientProtocolException e) {
 			e.printStackTrace();
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		if (done) {
 			return true;
 		} else {
 			shutdown();
 			return false;
 		}
 	}
 
 	private String getPoint(double value) {
 		return Location.convert(value, Location.FORMAT_DEGREES).replace(',',
 				'.');
 	}
 
 	/**
 	 * Close connection.
 	 */
 	private void shutdown() {
 		if (client != null) {
 			client.getConnectionManager().shutdown();
 			client = null;
 			csrf = null;
 			id = null;
 			attrs.clear();
 			pdf = null;
 		}
 	}
 }
