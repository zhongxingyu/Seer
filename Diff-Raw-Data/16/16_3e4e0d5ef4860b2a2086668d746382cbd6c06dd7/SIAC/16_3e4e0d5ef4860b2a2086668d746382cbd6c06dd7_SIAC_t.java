 /**
  * Acceso Inteligente
  *
  * Copyright (C) 2010-2011 Fundación Ciudadano Inteligente
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as
  * published by the Free Software Foundation, either version 3 of the
  * License.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.accesointeligente.server.robots;
 
 import org.accesointeligente.model.Request;
 import org.accesointeligente.server.ApplicationProperties;
 import org.accesointeligente.shared.RequestStatus;
 
 import org.apache.http.Header;
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.params.HttpProtocolParams;
 import org.apache.http.util.EntityUtils;
 import org.htmlcleaner.HtmlCleaner;
 import org.htmlcleaner.TagNode;
 
 import java.beans.ConstructorProperties;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class SIAC extends Robot {
 	private HttpClient client;
 	private HtmlCleaner cleaner;
 	private Boolean loggedIn = false;
 	private String characterEncoding = null;
 	private String baseUrl;
 	private String userId;
 
 	public SIAC() {
 		client = new DefaultHttpClient();
 		HttpProtocolParams.setUserAgent(client.getParams(), "Mozilla/5.0 (X11; U; Linux x86_64; es-CL; rv:1.9.2.12) Gecko/20101027 Ubuntu/10.10 (maverick) Firefox/3.6.12");
 		cleaner = new HtmlCleaner();
 	}
 
 	@ConstructorProperties({"idEntidad", "baseUrl"})
 	public SIAC(String idEntidad, String baseUrl) {
 		this();
 		setIdEntidad(idEntidad);
 		setBaseUrl(baseUrl);
 	}
 
 	@Override
 	public void login() throws RobotException {
 		if (characterEncoding == null) {
 			detectCharacterEncoding();
 		}
 
 		List<NameValuePair> formParams;
 		HttpPost post;
 		HttpResponse response;
 		TagNode document, hiddenUser;
 
 		try {
 			formParams = new ArrayList<NameValuePair>();
 			formParams.add(new BasicNameValuePair("usuario", username));
 			formParams.add(new BasicNameValuePair("clave", password));
 			formParams.add(new BasicNameValuePair("accion", "login"));
 
 			post = new HttpPost(baseUrl);
 			post.addHeader("Referer", baseUrl + "?accion=ingresa");
 			post.setEntity(new UrlEncodedFormEntity(formParams, characterEncoding));
 			response = client.execute(post);
 			document = cleaner.clean(new InputStreamReader(response.getEntity().getContent(), characterEncoding));
 			hiddenUser = document.findElementByAttValue("id", "user", true, true);
 
 			if (hiddenUser == null || !hiddenUser.hasAttribute("value") || hiddenUser.getAttributeByName("value").equals("0")) {
 				throw new Exception();
 			}
 
 			userId = hiddenUser.getAttributeByName("value");
 			loggedIn = true;
 		} catch (Throwable ex) {
 			throw new RobotException();
 		}
 	}
 
 	@Override
 	public Request makeRequest(Request request) throws RobotException {
 		if (!loggedIn) {
 			login();
 		}
 
 		List<NameValuePair> formParams;
 		HttpPost post;
 		HttpResponse response;
 		TagNode document;
		Pattern pattern = Pattern.compile("^Solicitud ingresada exitosamente, el número de su solicitud es: ([A-Z]{2}[0-9]{3}[A-Z])([0-9]{7})$");
 		Matcher matcher;
 
 		try {
 			formParams = new ArrayList<NameValuePair>();
 			formParams.add(new BasicNameValuePair("user", userId));
 			formParams.add(new BasicNameValuePair("accion", "registrar"));
 			formParams.add(new BasicNameValuePair("tipo", "7"));
 			formParams.add(new BasicNameValuePair("dirigido", idEntidad));
 			formParams.add(new BasicNameValuePair("ley_email", "S"));
 			formParams.add(new BasicNameValuePair("detalle", request.getInformation() + "\n\n" + request.getContext() + "\n\n" + ApplicationProperties.getProperty("request.signature")));
 			formParams.add(new BasicNameValuePair("remLen1", "" + (1996 - request.getInformation().length() - request.getContext().length() - ApplicationProperties.getProperty("request.signature").length())));
 			formParams.add(new BasicNameValuePair("nombre", "Felipe"));
 			formParams.add(new BasicNameValuePair("ap_paterno", "Heusser"));
 			formParams.add(new BasicNameValuePair("ap_materno", "Ferres"));
 			formParams.add(new BasicNameValuePair("rut", ""));
 			formParams.add(new BasicNameValuePair("rut_dv", ""));
 			formParams.add(new BasicNameValuePair("pasaporte", ""));
 			formParams.add(new BasicNameValuePair("nacionalidad", "320"));
 			formParams.add(new BasicNameValuePair("tipo_zona", "U"));
 			formParams.add(new BasicNameValuePair("educacion", "0"));
 			formParams.add(new BasicNameValuePair("edad", "0"));
 			formParams.add(new BasicNameValuePair("ocupacion", "0"));
 			formParams.add(new BasicNameValuePair("sexo", "M"));
 			formParams.add(new BasicNameValuePair("rb_organizacion", "S"));
 			formParams.add(new BasicNameValuePair("razon_social", "Fundación Ciudadano Inteligente"));
 			formParams.add(new BasicNameValuePair("rb_apoderado", "N"));
 			formParams.add(new BasicNameValuePair("nombre_apoderado", ""));
 			formParams.add(new BasicNameValuePair("residencia", "CHI"));
 			formParams.add(new BasicNameValuePair("direccion", "Holanda"));
 			formParams.add(new BasicNameValuePair("direccion_numero", "895"));
 			formParams.add(new BasicNameValuePair("direccion_villa", ""));
 			formParams.add(new BasicNameValuePair("fono_area", ""));
 			formParams.add(new BasicNameValuePair("fono", ""));
 			formParams.add(new BasicNameValuePair("comuna", "13123"));
 			formParams.add(new BasicNameValuePair("email", "info@accesointeligente.org"));
 			formParams.add(new BasicNameValuePair("email_verif", "info@accesointeligente.org"));
 			formParams.add(new BasicNameValuePair("bt_modificar", "Enviar Datos"));
 
 			post = new HttpPost(baseUrl);
 			post.addHeader("Referer", baseUrl);
 			post.setEntity(new UrlEncodedFormEntity(formParams, characterEncoding));
 			response = client.execute(post);
 			document = cleaner.clean(new InputStreamReader(response.getEntity().getContent(), characterEncoding));
 
			for (TagNode node : document.getElementsByName("li", true)) {
				matcher = pattern.matcher(node.getText().toString().trim());
 
				if (matcher.matches()) {
					request.setRemoteIdentifier(matcher.group(1) + "-" + matcher.group(2));
					break;
 				}
 			}
 
 			if (request.getRemoteIdentifier() != null) {
 				request.setStatus(RequestStatus.PENDING);
 				request.setProcessDate(new Date());
 				return request;
 			} else {
 				throw new Exception();
 			}
 		} catch (Throwable ex) {
 			throw new RobotException();
 		}
 	}
 
 	@Override
 	public RequestStatus checkRequestStatus(Request request) throws RobotException {
 		if (characterEncoding == null) {
 			detectCharacterEncoding();
 		}
 
 		List<NameValuePair> formParams;
 		HttpPost post;
 		HttpResponse response;
 		TagNode document, statusCell;
 		String statusLabel;
 
 		try {
 			if (request.getRemoteIdentifier() == null || request.getRemoteIdentifier().length() == 0) {
 				throw new Exception();
 			}
 
 			formParams = new ArrayList<NameValuePair>();
 			formParams.add(new BasicNameValuePair("nsolicitud", request.getRemoteIdentifier()));
 			post = new HttpPost(baseUrl);
 			post.addHeader("Referer", baseUrl + "?accion=consulta");
 			post.addHeader("X-Requested-With", "XMLHttpRequest");
 			post.setEntity(new UrlEncodedFormEntity(formParams, characterEncoding));
 			response = client.execute(post);
 			document = cleaner.clean(new InputStreamReader(response.getEntity().getContent(), characterEncoding));
 			statusCell = document.getElementsByName("td", true)[5];
 
 			if (statusCell == null) {
 				throw new Exception();
 			}
 
 			statusLabel = statusCell.getText().toString().trim();
 
 			// TODO: we don't know the rest of the status strings
 			if (statusLabel.equals("Ingreso de Solicitud")) {
 				return RequestStatus.PENDING;
 			} else {
 				return null;
 			}
 		} catch (Throwable ex) {
 			throw new RobotException();
 		}
 	}
 
 	@Override
 	public Boolean checkInstitutionId() throws RobotException {
 		if (!loggedIn) {
 			login();
 		}
 
 		HttpGet get;
 		HttpResponse response;
 		TagNode document, selector;
 
 		try {
 			get = new HttpGet(baseUrl + "?accion=ingresa");
 			response = client.execute(get);
 			document = cleaner.clean(new InputStreamReader(response.getEntity().getContent(), characterEncoding));
 			selector = document.findElementByAttValue("name", "dirigido", true, true);
 
 			if (selector == null) {
 				throw new Exception();
 			}
 
 			for (TagNode option : selector.getElementsByName("option", true)) {
 				if (option.hasAttribute("value") && option.getAttributeByName("value").equals(idEntidad)) {
 					return true;
 				}
 			}
 
 			return false;
 		} catch (Throwable ex) {
 			throw new RobotException();
 		}
 	}
 
 	public void detectCharacterEncoding() {
 		HttpGet get;
 		HttpResponse response;
 		Header contentType;
 		Pattern pattern;
 		Matcher matcher;
 
 		try {
 			get = new HttpGet(baseUrl + "?accion=ingresa");
 			response = client.execute(get);
 			contentType = response.getFirstHeader("Content-Type");
 			EntityUtils.consume(response.getEntity());
 
 			if (contentType == null || contentType.getValue() == null) {
 				characterEncoding = "ISO-8859-1";
 			}
 
 			pattern = Pattern.compile(".*charset=(.+)");
 			matcher = pattern.matcher(contentType.getValue());
 
 			if (!matcher.matches()) {
 				characterEncoding = "ISO-8859-1";
 			}
 
 			characterEncoding = matcher.group(1);
 		} catch (Exception e) {
 			characterEncoding = "ISO-8859-1";
 		}
 	}
 
 	public String getCharacterEncoding() {
 		return characterEncoding;
 	}
 
 	public void setCharacterEncoding(String characterEncoding) {
 		this.characterEncoding = characterEncoding;
 	}
 
 	public String getBaseUrl() {
 		return baseUrl;
 	}
 
 	public void setBaseUrl(String baseUrl) {
 		this.baseUrl = baseUrl;
 	}
 
 	public String getUserId() {
 		return userId;
 	}
 
 	public void setUserId(String userId) {
 		this.userId = userId;
 	}
 }
