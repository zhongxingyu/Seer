 /*
  * This file is part of the Lokalizator grobów project.
  *
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation v3; 
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301 USA
  */
 package pl.itiner.fetch;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.lang.reflect.Type;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.StatusLine;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 import pl.itiner.model.Departed;
 import pl.itiner.model.DepartedDeserializer;
 import pl.itiner.model.DepartedListDeserializer;
 import android.annotation.SuppressLint;
 import android.net.Uri;
 import android.net.http.AndroidHttpClient;
 import android.os.Build;
 
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 import com.google.gson.reflect.TypeToken;
 
 /**
  * 
  */
 
 /**
  * TODO złap gdzieś wyjątek parsowania
  * 
  */
 public class GeoJSON {
 
 	private static final String DATE_FORMAT = "yyyy-MM-dd";
 	private static List<Departed> dList = new ArrayList<Departed>();
 	public static final String TAG = "GeoJSON";
 
 	private static final String USER_AGENT = "Grave-finder (www.itiner.pl)";
 	private static final int MAX_FETCH_SIZE = 200;
 	private static final Type COLLECTION_TYPE = new TypeToken<List<Departed>>() {
 	}.getType();
 
 	private static GsonBuilder g;
 
 	static {
 		g = new GsonBuilder();
 		g.setDateFormat(DATE_FORMAT);
 		g.registerTypeAdapter(Departed.class, new DepartedDeserializer());
 		g.registerTypeAdapter(COLLECTION_TYPE, new DepartedListDeserializer());
 	}
 
 	public static List<Departed> getResults() {
 		return Collections.unmodifiableList(dList);
 	}
 
 	public GeoJSON() {
 	}
 
 	public static void executeQuery(Long cmId, String name, String surname,
 			Date deathDate, Date birthDate, Date burialDate) throws IOException {
 		Uri uri = prepeareURL(cmId, name, surname, deathDate, birthDate,
 				burialDate);
 		String resp = getResponse(uri);
 		parseJSON(resp);
 	}
 
 	private static Uri prepeareURL(Long cmId, String name, String surname,
 			Date deathDate, Date birthDate, Date burialDate) {
 		Map<String, String> paramsMap = createQueryParamsMap(cmId, name,
 				surname, deathDate, birthDate, burialDate);
 		Uri.Builder b = Uri.parse(
 				"http://www.poznan.pl/featureserver/featureserver.cgi/groby")
 				.buildUpon();
 		b.appendQueryParameter("maxFeatures", MAX_FETCH_SIZE + "");
 		StringBuilder queryableParam = new StringBuilder();
 		for (String p : paramsMap.keySet()) {
 			queryableParam.append(p).append(",");
 			b.appendQueryParameter(p, paramsMap.get(p));
 		}
		if(queryableParam.length() > 0)
			queryableParam.deleteCharAt(queryableParam.length() - 1);
 		b.appendQueryParameter("queryable", queryableParam.toString());
 		return b.build();
 	}
 
 	@SuppressLint("NewApi")
 	private static String getResponse(Uri uri) throws IOException {
 		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
 			AndroidHttpClient client = AndroidHttpClient
 					.newInstance(USER_AGENT);
 			HttpResponse resp = client.execute(new HttpGet(uri.toString()));
 			OutputStream os = new ByteArrayOutputStream();
 			resp.getEntity().writeTo(os);
 			client.close();
 			os.close();
 			return os.toString();
 		} else {
 			HttpClient client = new DefaultHttpClient();
 			HttpGet request = new HttpGet(uri.toString());
 			request.setHeader("User-Agent", USER_AGENT);
 			HttpResponse response = client.execute(request);
 			StatusLine status = response.getStatusLine();
 			if (status.getStatusCode() != 200) {
 				throw new IOException("Invalid response from server: "
 						+ status.toString());
 			}
 			HttpEntity entity = response.getEntity();
 			InputStream inputStream = entity.getContent();
 			ByteArrayOutputStream content = new ByteArrayOutputStream();
 			int readBytes = 0;
 			byte[] sBuffer = new byte[512];
 			while ((readBytes = inputStream.read(sBuffer)) != -1) {
 				content.write(sBuffer, 0, readBytes);
 			}
 			return new String(content.toByteArray());
 		}
 	}
 
 	private static void parseJSON(String JSON) {
 		Gson gson = g.create();
 		dList = gson.fromJson(JSON, COLLECTION_TYPE);
 	}
 
 	private static Map<String, String> createQueryParamsMap(Long cmId,
 			String name, String surname, Date deathDate, Date birthDate,
 			Date burialDate) {
 		SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT);
 		Map<String, String> map = new HashMap<String, String>();
 		if (cmId != null) {
 			map.put("cm_id", cmId.toString());
 		}
 		if (filledStr(name)) {
 			map.put("g_name", name);
 		}
 		if (filledStr(surname)) {
 			map.put("g_surname", surname);
 		}
 		if (deathDate != null) {
 			map.put("g_date_death", formatter.format(deathDate));
 		}
 		if (burialDate != null) {
 			map.put("g_date_burial", formatter.format(burialDate));
 		}
 		if (birthDate != null) {
 			map.put("g_date_birth", formatter.format(birthDate));
 		}
 		return map;
 	}
 
 	private static boolean filledStr(String str) {
 		return str != null && !str.equals("");
 	}
 
 }
