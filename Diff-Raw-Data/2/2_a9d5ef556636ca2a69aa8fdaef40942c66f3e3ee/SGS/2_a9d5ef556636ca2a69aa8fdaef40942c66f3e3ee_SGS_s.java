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
 import org.accesointeligente.model.external.SGSListResult;
 import org.accesointeligente.server.ApplicationProperties;
 import org.accesointeligente.shared.RequestStatus;
 
 import com.google.gson.Gson;
 
 import org.apache.http.*;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.params.HttpProtocolParams;
 import org.apache.http.util.EntityUtils;
 import org.apache.log4j.Logger;
 import org.htmlcleaner.HtmlCleaner;
 import org.htmlcleaner.TagNode;
 
 import java.beans.ConstructorProperties;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class SGS extends Robot {
 	private static final Logger logger = Logger.getLogger(SGS.class);
 	private HttpClient client;
 	private HtmlCleaner cleaner;
 	private Boolean loggedIn = false;
 	private String characterEncoding = null;
 	private String baseUrl;
 	private String loginAction = "?accion=login";
 	private String homeAction = "?accion=Home";
 	private String exitAction = "?accion=Salir";
 	private String requestFormAction = "?accion=Solicitud-de-Informacion";
 	private String requestCreationAction = "?accion=solicitud-de-informacion&act=4";
 	private String requestConfirmAction = "?accion=solicitud-de-informacion&act=6";
 	private String requestCreatedAction = "?accion=solicitud-de-informacion&act=5";
 	private String requestViewAction = "?accion=mis-solicitudes&act=1";
 	private String requestListAction = "?accion=Mis-Solicitudes";
	private String requestAjaxOption = "&axj=1";
 	private String requestJsonListTotal = "&iDisplayStart=0&iDisplayLength=0";
 	private String requestJsonListStart = "&iDisplayStart=";
 	private String requestJsonListLength = "&iDisplayLength=";
 
 	public SGS() {
 		client = new DefaultHttpClient();
 		HttpProtocolParams.setUserAgent(client.getParams(), "Mozilla/5.0 (X11; U; Linux x86_64; es-CL; rv:1.9.2.12) Gecko/20101027 Ubuntu/10.10 (maverick) Firefox/3.6.12");
 		HttpProtocolParams.setVersion(client.getParams(), HttpVersion.HTTP_1_0);
 		cleaner = new HtmlCleaner();
 	}
 
 	@ConstructorProperties({"idEntidad", "baseUrl"})
 	public SGS(String idEntidad, String baseUrl) {
 		this();
 		setIdEntidad(idEntidad);
 		setBaseUrl(baseUrl);
 	}
 
 	@Override
 	public void login() throws Exception {
 		if (characterEncoding == null) {
 			detectCharacterEncoding();
 		}
 
 		List<NameValuePair> formParams;
 		HttpPost post;
 		HttpGet get;
 		HttpResponse response;
 		TagNode document;
 		Header location;
 
 		try {
 			formParams = new ArrayList<NameValuePair>();
 			formParams.add(new BasicNameValuePair("login", username));
 			formParams.add(new BasicNameValuePair("password", password));
 			formParams.add(new BasicNameValuePair("Ingresar", "Ingresar"));
 
 			post = new HttpPost(baseUrl + loginAction);
 			post.addHeader("Referer", baseUrl + homeAction);
 			post.setEntity(new UrlEncodedFormEntity(formParams, characterEncoding));
 			response = client.execute(post);
 			location = response.getFirstHeader("Location");
 
 			if (location == null) {
 				throw new RobotException("No redirect after login");
 			} else if (!"index.php".equals(location.getValue())) {
 				throw new RobotException("Invalid location after login");
 			}
 
 			EntityUtils.consume(response.getEntity());
 
 			get = new HttpGet(baseUrl + "");
 			get.addHeader("Referer", baseUrl + homeAction);
 			response = client.execute(get);
 			document = cleaner.clean(new InputStreamReader(response.getEntity().getContent(), characterEncoding));
 
 			if (document.getElementListByAttValue("href", "index.php" + exitAction, true, false).isEmpty()) {
 				throw new RobotException("Exit item not found in menu");
 			}
 
 			loggedIn = true;
 		} catch (Exception ex) {
 			logger.error(ex.getMessage(), ex);
 			throw ex;
 		}
 	}
 
 	@Override
 	public Request makeRequest(Request request) throws Exception {
 		if (!loggedIn) {
 			login();
 		}
 
 		List<NameValuePair> formParams;
 		HttpPost post;
 		HttpResponse response;
 		TagNode document, hidden;
 		Integer folio;
 		Header location;
 		Pattern pattern;
 		Matcher matcher;
 		String remoteIdentifier;
 
 		try {
 			EntityUtils.consume(client.execute(new HttpGet(baseUrl + requestFormAction)).getEntity());
 			formParams = new ArrayList<NameValuePair>();
 			formParams.add(new BasicNameValuePair("id_entidad", idEntidad));
 			formParams.add(new BasicNameValuePair("identificacion_documentos", request.getInformation() + "\n\n" + request.getContext() + "\n\n" + ApplicationProperties.getProperty("request.signature")));
 			formParams.add(new BasicNameValuePair("notificacion", "1"));
 			formParams.add(new BasicNameValuePair("id_forma_recepcion", "1")); // Email
 			formParams.add(new BasicNameValuePair("oficina", ""));
 			formParams.add(new BasicNameValuePair("id_formato_entrega", "2")); // Digital
 			formParams.add(new BasicNameValuePair("Continuar", "Continuar"));
 
 			post = new HttpPost(baseUrl + requestCreationAction);
 			post.addHeader("Referer", baseUrl + requestFormAction);
 			post.setEntity(new UrlEncodedFormEntity(formParams, characterEncoding));
 			response = client.execute(post);
 			document = cleaner.clean(new InputStreamReader(response.getEntity().getContent(), characterEncoding));
 
 			hidden = document.findElementByAttValue("name", "folio_hidden", true, true);
 
 			if (hidden == null) {
 				throw new RobotException("No hidden field for temporary id found");
 			} else if (!"input".equals(hidden.getName())) {
 				throw new RobotException("Expecting \"hidden\" for temporary id field but found \"" + hidden.getName() + "\"");
 			}
 
 			folio = Integer.parseInt(hidden.getAttributeByName("value"));
 
 			if (folio == null) {
 				throw new RobotException("Invalid value for temporary id");
 			}
 
 			formParams = new ArrayList<NameValuePair>();
 			formParams.add(new BasicNameValuePair("folio_hidden", folio.toString()));
 			formParams.add(new BasicNameValuePair("Aceptar", "Enviar Solicitud"));
 
 			post = new HttpPost(baseUrl + requestConfirmAction);
 			post.addHeader("Referer", baseUrl + requestCreationAction);
 			post.setEntity(new UrlEncodedFormEntity(formParams, characterEncoding));
 			response = client.execute(post);
 			request.setStatus(RequestStatus.PENDING);
 			request.setProcessDate(new Date());
 
 			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_MOVED_PERMANENTLY) {
 				location = response.getFirstHeader("Location");
 
 				if (location == null) {
 					throw new RobotException("Invalid redirection after confirmation");
 				}
 
 				pattern = Pattern.compile("^index.php\\" + requestCreatedAction + "&folio=(.+)$");
 				matcher = pattern.matcher(location.getValue());
 
 				if (!matcher.matches()) {
 					throw new RobotException("External id not found");
 				}
 
 				remoteIdentifier = matcher.group(1);
 
 				if (remoteIdentifier == null || remoteIdentifier.length() == 0) {
 					throw new Exception();
 				}
 
 				EntityUtils.consume(response.getEntity());
 
 				request.setRemoteIdentifier(remoteIdentifier);
 			} else {
 				// Something went wrong after creating the request, assuming that the request was saved,
 				// we must check for its code in the request list page
 				EntityUtils.consume(response.getEntity());
 				response = client.execute(new HttpGet(baseUrl + requestListAction));
 				document = cleaner.clean(new InputStreamReader(response.getEntity().getContent(), characterEncoding));
 				TagNode form = document.findElementByAttValue("id", "form1", true, true);
 
 				if (form == null) {
 					return request;
 				}
 
 				TagNode pager = form.findElementByName("strong", true);
 
 				if (pager != null) {
 					pattern = Pattern.compile("^(\\d+) Paginas$");
 					matcher = pattern.matcher(pager.getText());
 
 					if (matcher.matches()) {
 						// Go to the last page
 						response = client.execute(new HttpGet(baseUrl + requestListAction + "&act=0&p=" + matcher.group(1)));
 						document = cleaner.clean(new InputStreamReader(response.getEntity().getContent(), characterEncoding));
 					}
 				}
 
 				try {
 					// The last row of the table has the request
 					TagNode tableContainer = document.findElementByAttValue("id", "table-block", true, true);
 					TagNode[] tableRows = tableContainer.getChildTags()[0].getChildTags()[0].getChildTags();
 					TagNode lastRow = tableRows[tableRows.length - 1];
 
 					// FIXME: verify that the identifier found isn't already used
 					if (lastRow.getChildTags().length == 6) {
 						remoteIdentifier = lastRow.getChildTags()[0].getText().toString().trim();
 						request.setRemoteIdentifier(remoteIdentifier);
 					}
 				} catch (Exception ex) {
 					logger.info("Couldn't found remote identifier in SGS request list");
 				}
 
 				// If we couldn't get the remote identifier, it must be SGS 1.1. We'll try to get the identifier via JSON requests
 				if (request.getRemoteIdentifier() == null) {
 					try {
 						Gson gsonEncoder = new Gson();
 						String jsonResponse = null;
 						String jsonQueryUrl = baseUrl + requestListAction + requestAjaxOption + requestJsonListTotal;
 						logger.info(jsonQueryUrl);
 						SGSListResult sgsListResult = new SGSListResult();
 						Integer totalResults = 0;
 						response = client.execute(new HttpGet(jsonQueryUrl));
 						jsonResponse = response.getEntity().getContent().toString();
 						logger.info(jsonResponse);
 						sgsListResult = gsonEncoder.fromJson(jsonResponse, SGSListResult.class);
 
 						totalResults = sgsListResult.getTotalRecords();
 						totalResults--;
 						response = client.execute(new HttpGet(baseUrl + requestListAction + requestAjaxOption + requestJsonListStart + totalResults.toString() + requestJsonListLength + "1"));
 						jsonResponse = response.getEntity().getContent().toString();
 						logger.info(jsonResponse);
 						sgsListResult = gsonEncoder.fromJson(jsonResponse, SGSListResult.class);
 						remoteIdentifier = sgsListResult.getSgsRequests()[0].getIdentifier();
 						request.setRemoteIdentifier(remoteIdentifier);
 					} catch (Exception ex) {
 						logger.error(ex.getMessage(), ex);
 						throw ex;
 					}
 				}
 			}
 
 			return request;
 		} catch (Exception ex) {
 			logger.error(ex.getMessage(), ex);
 			throw ex;
 		}
 	}
 
 	@Override
 	public RequestStatus checkRequestStatus(Request request) throws Exception {
 		if (!loggedIn) {
 			login();
 		}
 
 		HttpGet get;
 		HttpResponse response;
 		TagNode document, statusCell;
 		String statusLabel;
 
 		try {
 			if (request.getRemoteIdentifier() == null || request.getRemoteIdentifier().length() == 0) {
 				throw new RobotException("Invalid remote identifier");
 			}
 
 			get = new HttpGet(baseUrl + requestViewAction + "&folio=" + request.getRemoteIdentifier());
 			get.addHeader("Referer", baseUrl + requestListAction);
 			response = client.execute(get);
 			document = cleaner.clean(new InputStreamReader(response.getEntity().getContent(), characterEncoding));
 			statusCell = document.findElementByAttValue("width", "36%", true, true);
 
 			if (statusCell == null) {
 				// If we couldn't get the remote identifier, it must be SGS 1.1. We'll try to get the new assigned cell
 				statusCell = document.findElementByAttValue("width", "28%", true, true);
 
 				if (statusCell == null) {
 					throw new RobotException("Invalid status text cell");
 				}
 			}
 
 			statusLabel = statusCell.getText().toString().trim();
 
 			// TODO: check if request expired
 			if (statusLabel.equals("En Proceso")) {
 				return RequestStatus.PENDING;
 			} else if (statusLabel.equals("Respondida")) {
 				return RequestStatus.CLOSED;
 			} else if (statusLabel.equals("Derivada")) {
 				return RequestStatus.DERIVED;
 			} else {
 				return null;
 			}
 		} catch (Exception ex) {
 			logger.error(ex.getMessage(), ex);
 			throw ex;
 		}
 	}
 
 	@Override
 	public Boolean checkInstitutionId() throws Exception {
 		if (!loggedIn) {
 			login();
 		}
 
 		HttpGet get;
 		HttpResponse response;
 		TagNode document, selector;
 
 		try {
 			get = new HttpGet(baseUrl + requestFormAction);
 			response = client.execute(get);
 			document = cleaner.clean(new InputStreamReader(response.getEntity().getContent(), characterEncoding));
 			selector = document.findElementByAttValue("name", "id_entidad", true, true);
 
 			if (selector == null) {
 				throw new RobotException("Institution selector not found");
 			}
 
 			for (TagNode option : selector.getChildTags()) {
 				if (option.hasAttribute("value") && option.getAttributeByName("value").equals(idEntidad)) {
 					return true;
 				}
 			}
 
 			return false;
 		} catch (Exception ex) {
 			logger.error(ex.getMessage(), ex);
 			throw ex;
 		}
 	}
 
 	public void detectCharacterEncoding() {
 		HttpGet get;
 		HttpResponse response;
 		Header contentType;
 		Pattern pattern;
 		Matcher matcher;
 
 		try {
 			get = new HttpGet(baseUrl + homeAction);
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
 
 	public String getLoginAction() {
 		return loginAction;
 	}
 
 	public void setLoginAction(String loginAction) {
 		this.loginAction = loginAction;
 	}
 
 	public String getHomeAction() {
 		return homeAction;
 	}
 
 	public void setHomeAction(String homeAction) {
 		this.homeAction = homeAction;
 	}
 
 	public String getExitAction() {
 		return exitAction;
 	}
 
 	public void setExitAction(String exitAction) {
 		this.exitAction = exitAction;
 	}
 
 	public String getRequestFormAction() {
 		return requestFormAction;
 	}
 
 	public void setRequestFormAction(String requestFormAction) {
 		this.requestFormAction = requestFormAction;
 	}
 
 	public String getRequestCreationAction() {
 		return requestCreationAction;
 	}
 
 	public void setRequestCreationAction(String requestCreationAction) {
 		this.requestCreationAction = requestCreationAction;
 	}
 
 	public String getRequestConfirmAction() {
 		return requestConfirmAction;
 	}
 
 	public void setRequestConfirmAction(String requestConfirmAction) {
 		this.requestConfirmAction = requestConfirmAction;
 	}
 
 	public String getRequestCreatedAction() {
 		return requestCreatedAction;
 	}
 
 	public void setRequestCreatedAction(String requestCreatedAction) {
 		this.requestCreatedAction = requestCreatedAction;
 	}
 
 	public String getRequestViewAction() {
 		return requestViewAction;
 	}
 
 	public void setRequestViewAction(String requestViewAction) {
 		this.requestViewAction = requestViewAction;
 	}
 
 	public String getRequestListAction() {
 		return requestListAction;
 	}
 
 	public void setRequestListAction(String requestListAction) {
 		this.requestListAction = requestListAction;
 	}
 
 	public String getRequestAjaxOption() {
 		return requestAjaxOption;
 	}
 
 	public void setRequestAjaxOption(String requestAjaxOption) {
 		this.requestAjaxOption = requestAjaxOption;
 	}
 
 	public String getRequestJsonListTotal() {
 		return requestJsonListTotal;
 	}
 
 	public void setRequestJsonListTotal(String requestJsonListTotal) {
 		this.requestJsonListTotal = requestJsonListTotal;
 	}
 
 	public String getRequestJsonListStart() {
 		return requestJsonListStart;
 	}
 
 	public void setRequestJsonListStart(String requestJsonListStart) {
 		this.requestJsonListStart = requestJsonListStart;
 	}
 
 	public String getRequestJsonListLength() {
 		return requestJsonListLength;
 	}
 
 	public void setRequestJsonListLength(String requestJsonListLength) {
 		this.requestJsonListLength = requestJsonListLength;
 	}
 }
