 package ar.com.restba;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.ArrayList;
 import java.util.List;
 
 import ar.com.restba.json.JsonArray;
 import ar.com.restba.json.JsonObject;
 import ar.com.restba.types.ObrasRegistradas;
 import ar.com.restba.utils.RestBAUtils;
 import au.com.bytecode.opencsv.CSVReader;
 
 public class DefaultRestBAClient implements RestBAClient {
 
 	private RestFbConnector restFbConnector;
 
 	public DefaultRestBAClient() {
 		restFbConnector = new RestFbConnector();
 		;
 	}
 
 	@Override
 	public List<ObrasRegistradas> fetchObrasRegistradas() {
 
		com.restfb.json.JsonObject fetchObjectRestFb = restFbConnector
				.fetchObject("obras-registradas",
						com.restfb.json.JsonObject.class);

		JsonObject fetchObject = new JsonObject(fetchObjectRestFb.toString());
 		JsonArray resources = fetchObject.getJsonArray("resources");
 
 		List<ObrasRegistradas> obrasRegistradas = new ArrayList<ObrasRegistradas>();
 
 		JsonObject resource = RestBAUtils.findResourceById(resources,
 				ObrasRegistradas.OBRAS_REGISTRADAS_ID);
 
 		String url = resource.getString("url").replaceFirst("https", "http");
 		System.out.println(url);
 		InputStream inputStream = null;
 		URL urlconnect;
 		try {
 			urlconnect = new URL(url);
 			URLConnection uc = urlconnect.openConnection();
 			inputStream = uc.getInputStream();
 		} catch (MalformedURLException eURL) {
 			throw new RuntimeException(eURL);
 		} catch (IOException e) {
 			throw new RuntimeException(e);
 		}
 
 		CSVReader reader = null;
 		reader = new CSVReader(new InputStreamReader(inputStream));
 		String[] nextLine;
 		boolean firstTime = true;
 		try {
 			while ((nextLine = reader.readNext()) != null) {
 
 				// To Remove Header of CSV
 				if (firstTime) {
 					firstTime = false;
 					continue;
 				}
 				ObrasRegistradas entity = new ObrasRegistradas();
 				entity.setnExpediente(nextLine[0]);
 				entity.setDireccion(nextLine[1]);
 				entity.setSmp(nextLine[2]);
 				entity.setEstadoTramite(nextLine[3]);
 				entity.setFechaEstado(nextLine[4]);
 				entity.setTipoObra(nextLine[5]);
 				entity.setNombreProfesional(nextLine[6]);
 				obrasRegistradas.add(entity);
 			}
 		} catch (IOException e) {
 			throw new RuntimeException(e);
 		} finally {
 			try {
 				reader.close();
 			} catch (IOException e) {
 				throw new RuntimeException(e);
 			}
 		}
 
 		return obrasRegistradas;
 	}
 
 	@Override
 	public JsonObject fetchDataset(String dataset) {
		com.restfb.json.JsonObject fetchObject = restFbConnector.fetchObject(
				dataset, com.restfb.json.JsonObject.class);
		return new JsonObject(fetchObject.toString());
 	}
 
 }
