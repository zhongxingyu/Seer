 package com.photon.phresco.framework.commons;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.util.Map;
 import java.util.Set;
 
 import javax.servlet.Filter;
 import javax.servlet.FilterChain;
 import javax.servlet.FilterConfig;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.HttpServletRequest;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 
 import org.apache.commons.codec.binary.Base64;
 import org.apache.commons.io.IOUtils;
 import org.apache.log4j.Logger;
 import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
 import org.codehaus.plexus.util.StringUtils;
 import org.json.simple.JSONObject;
 import org.json.simple.parser.JSONParser;
 import org.json.simple.parser.ParseException;
 
 import com.google.gson.Gson;
 import com.photon.phresco.commons.model.Customer;
 import com.photon.phresco.exception.PhrescoException;
 import com.photon.phresco.framework.FrameworkConfiguration;
 import com.photon.phresco.framework.PhrescoFrameworkFactory;
 import com.photon.phresco.framework.actions.util.ClientIdentifyFilterConstants;
 import com.photon.phresco.framework.model.ClientIdentifyModel;
 import com.photon.phresco.framework.rest.api.util.ActionFunction;
 import com.photon.phresco.service.client.impl.ClientHelper;
 import com.sun.jersey.api.client.Client;
 import com.sun.jersey.api.client.ClientResponse;
 import com.sun.jersey.api.client.WebResource;
 import com.sun.jersey.api.client.config.ClientConfig;
 import com.sun.jersey.api.client.config.DefaultClientConfig;
 import com.sun.jersey.multipart.BodyPart;
 import com.sun.jersey.multipart.MultiPart;
 import com.sun.jersey.multipart.MultiPartMediaTypes;
 
 public class ClientIdentifyFilter implements Filter , ClientIdentifyFilterConstants {
 	
 	private static final Logger S_LOGGER= Logger.getLogger(ActionFunction.class);
 	private static boolean isDebugEnabled = S_LOGGER.isDebugEnabled();
 
 	public void init(FilterConfig filterConfig) throws ServletException {
 	}
 	
 	public void doFilter(ServletRequest request, ServletResponse response,
 			FilterChain chain) throws IOException, ServletException {
 		
 			try {
 				HttpServletRequest req = null;
 				String url="";
 				if (request instanceof HttpServletRequest) {
 					req = (HttpServletRequest)request;
 					url = req.getRequestURL().toString();
 
 					if (isDebugEnabled) {
 						S_LOGGER.debug("url : "+url);
 					}
 				}
 
 				if(	(url.contains(LOCALES)) || (url.contains(JSON)) || (url.contains(INDEX_HTML)) || (url.contains(REST_CALL)) || (url.contains(COMPONENTS)) || (url.contains(JS)) || (url.contains(LIB)) || (url.contains(THEMES))) {
 					chain.doFilter(req, response);
 				}
 				else {
 					
 					String forcompare = url;
 				 	int location = forcompare.lastIndexOf("/");
 				 	String cutomername = forcompare.substring(location);
 				 	cutomername = cutomername.substring(1);
 					try {
 						PrintWriter  out = response.getWriter();
 						out.print("<script type=\"text/javascript\"> localStorage.removeItem(\""+ THEME_KEY +"\"); </script> ");
 						out.print("<script type=\"text/javascript\"> localStorage.removeItem(\""+ LOGO_KEY +"\"); </script> ");
 						out.print("<script type=\"text/javascript\"> localStorage.removeItem(\""+ STATUS_MESSAGE +"\"); </script> ");
 						ClientIdentifyModel customerdetails = getCustomerProperties(cutomername);
 				    	if(customerdetails.getStatus()) {
 				    		JSONObject cutomer_theme = processCustomerProperties(customerdetails.getCustomer());
 				        	String customer_json_theme = new Gson().toJson(cutomer_theme);
 				        	String customer_logo= customerdetails.getCustomerlogo();
 				    		out.print("<script type=\"text/javascript\"> localStorage.setItem(\""+ THEME_KEY +"\",JSON.stringify("+ customer_json_theme +")); </script> ");
				    		out.print("<script type=\"text/javascript\"> localStorage.setItem(\""+ LOGO_KEY +"\",\""+ customer_logo +"\"); </script> ");	
 				    	}
 				    	else {
 				    			out.print("<script type=\"text/javascript\"> localStorage.setItem(\""+ STATUS_MESSAGE +"\",\""+ customerdetails.getMessage() +"\"); </script> ");	
 				    	}
 					} catch (PhrescoException e) {
 						S_LOGGER.error("Exception occured in the Client properties obatining process"+e.getMessage());
 					} catch (Exception e) {
 						S_LOGGER.error("Exception occured in the Client properties obatining process"+e.getMessage());
 						e.printStackTrace();
 					}
 					req.getRequestDispatcher(ROUTE_URL).include(req, response);
 				}
 			} catch (Exception e) {
 				S_LOGGER.error("A major exception occured in the Client identify filter "+e.getMessage());
 				chain.doFilter(request, response);
 			}
 	}
 	
 	public ClientIdentifyModel getCustomerProperties(String cutomername) throws PhrescoException, IOException
 	{
 		FrameworkConfiguration configuration = PhrescoFrameworkFactory.getFrameworkConfig();
 		ClientConfig config = new DefaultClientConfig();
         config.getClasses().add(JacksonJsonProvider.class);
 		Client c = Client.create(config);
 	    WebResource resource = c.resource(configuration.getServerPath() +REST_API_CUSTOMER_PROPERTIES);
 	    resource = resource.queryParam("context", cutomername);
 	    resource.accept(MultiPartMediaTypes.MULTIPART_MIXED);
 	    MultiPart result = null;
 		result = resource.get(MultiPart.class);
 		String status = result.getHeaders().getFirst("status");
 		if((Response.Status.OK.getReasonPhrase()).equals(status)) {
 				
 				BodyPart part1 = result.getBodyParts().get(0);
 			    BodyPart part2 = result.getBodyParts().get(1);
 		        Customer customer = part1.getEntityAs(Customer.class);
 		        if (isDebugEnabled) {
 					S_LOGGER.debug("Response Status -->  CustomerID : " + customer.getId());
 				}
 		        InputStream fileInputStream = part2.getEntityAs(InputStream.class);
 		        if(fileInputStream == null) {
 		        	throw new PhrescoException("exception occured while reading the customer image");
 		        }
 				byte[] imgByte = null;
 				imgByte = IOUtils.toByteArray(fileInputStream);
 			    byte[] encodedImage = Base64.encodeBase64(imgByte);
 		        String encodeImg = new String(encodedImage);
 		        ClientIdentifyModel data = new ClientIdentifyModel();
 		        data.setCustomer(customer);
 		        data.setCustomerlogo(encodeImg);
 		        data.setStatus(true);
 		        return data;
 		}
 		else if((Response.Status.NOT_FOUND.getReasonPhrase()).equals(status)) {
 		
 			if (isDebugEnabled) {
 					S_LOGGER.debug("Response Status :"+Response.Status.NOT_FOUND.getReasonPhrase());
 			}
 	       ClientIdentifyModel data = new ClientIdentifyModel();
 	       data.setStatus(false);
 	       data.setMessage(INVALID_CUSTOMER);
 	       return data;
 		}
 		else if((Response.Status.NO_CONTENT.getReasonPhrase()).equals(status)) {
 				
 			if (isDebugEnabled) {
 				S_LOGGER.debug("Response Status :"+Response.Status.NO_CONTENT.getReasonPhrase());
 			}
 		    ClientIdentifyModel data = new ClientIdentifyModel();
 		    data.setStatus(false);
 		    data.setMessage(NO_IMAGE);
 		    return data;
 		}
 		else if((Response.Status.PRECONDITION_FAILED.getReasonPhrase()).equals(status)) {
 			if (isDebugEnabled) {
 				S_LOGGER.debug("Response Status :"+Response.Status.PRECONDITION_FAILED.getReasonPhrase());
 			}
 			ClientIdentifyModel data = new ClientIdentifyModel();
 		    data.setStatus(false);
 		    data.setMessage(NO_ID);
 		    return data;
 		}
 		else {
 			if (isDebugEnabled) {
 				S_LOGGER.debug("Response Status :"+INVALID);
 			}
 			ClientIdentifyModel data = new ClientIdentifyModel();
 		    data.setStatus(false);
 		    data.setMessage(INVALID);
 			return null;
 			}
 	}
 	
 	public JSONObject processCustomerProperties(Customer customer) {
 		
 		String customerName = customer.getName();
 		try {
 	        if (!customerName.equalsIgnoreCase("Photon")) {
 		        
 	        	Map<String, String> theme = customer.getFrameworkTheme();
 				String brandingColor = theme.get("brandingColor");
 				String accordionBackGroundColor =  theme.get("accordionBackGroundColor");
 				String bodyBackGroundColor =  theme.get("bodyBackGroundColor");
 				String buttonColor =  theme.get("ButtonColor");
 				String pageHeaderColor =  theme.get("PageHeaderColor");
 				String copyRightColor =  theme.get("CopyRightColor");
 				String labelColor =  theme.get("LabelColor");
 				String menufontColor =  theme.get("MenufontColor"); 
 				String menuBackGround =  theme.get("MenuBackGround");
 //				String subMenuBackGround =  theme.get("SubMenuBackGround");
 				String copyRight =  theme.get("CopyRight");
 				String disabledLabelColor =  theme.get("DisabledLabelColor");
 				String loginLogo =  theme.get("loginlogo");
 				String logoPadding =  theme.get("logopadding");
 				String gradientTop = theme.get("gradientTop");
 				String gradientBottom = theme.get("gradientBottom");
 				String gradientvar = "linear-gradient(to bottom, gradientTop 1%,gradientBottom 100%) !important";
 				String filter = "progid:DXImageTransform.Microsoft.gradient( startColorstr=gradientTop, endColorstr=gradientBottom,GradientType=0 ) !important";
 			
 			    InputStream stream =   this.getClass().getClassLoader().getResourceAsStream("customercss.xml");
 			    String result = getStringFromInputStream(stream);
 				
 			    JSONParser parser = new JSONParser();
 				JSONObject jsonObject = (JSONObject) parser.parse(result);
 			
 				Set keySet = jsonObject.keySet();
 				for (Object object : keySet) {
 					JSONObject fields = (JSONObject)jsonObject.get(object.toString());
 					Set fieldSet = fields.keySet();
 					for (Object key : fieldSet) {
 						Object value = fields.get(key);
 						String customercolor = value.toString();
 						if (customercolor.contains("brandingColor") && StringUtils.isNotEmpty(brandingColor)) {
 							fields.put(key,  customercolor.replace("brandingColor", brandingColor) + " !important");
 						}
 						if (customercolor.equals("menuBackGround") && StringUtils.isNotEmpty(menuBackGround)) {
 							fields.put(key,  customercolor.replace("menuBackGround", menuBackGround) + " !important");
 						}
 						if (customercolor.contains("backgroundColor") && StringUtils.isNotEmpty(bodyBackGroundColor)) {
 							fields.put(key,  customercolor.replace("backgroundColor", bodyBackGroundColor) + " !important");
 						}
 						if (customercolor.contains("accordionBackGroundColor") && StringUtils.isNotEmpty(accordionBackGroundColor)) {
 							fields.put(key,  customercolor.replace("accordionBackGroundColor", accordionBackGroundColor) + " !important");
 						}
 						if (customercolor.contains("bodyBackGroundColor") && StringUtils.isNotEmpty(bodyBackGroundColor)) {
 							fields.put(key,  customercolor.replace("bodyBackGroundColor", bodyBackGroundColor) + " !important");
 						}
 						if (customercolor.contains("buttonColor") && StringUtils.isNotEmpty(buttonColor)) {
 							fields.put(key,  customercolor.replace("buttonColor", buttonColor) + " !important");
 						}
 						if (customercolor.contains("pageHeaderColor") && StringUtils.isNotEmpty(pageHeaderColor)) {
 							fields.put(key,  customercolor.replace("pageHeaderColor", pageHeaderColor) + " !important");
 						}
 						if (customercolor.contains("copyRightColor") && StringUtils.isNotEmpty(copyRightColor)) {
 							fields.put(key,  customercolor.replace("copyRightColor", copyRightColor) + " !important");
 						}
 						if (customercolor.contains("labelColor") && StringUtils.isNotEmpty(labelColor)) {
 							fields.put(key,  customercolor.replace("labelColor", labelColor) + " !important");
 						}
 						if (customercolor.contains("menufontColor") && StringUtils.isNotEmpty(menufontColor)) {
 							fields.put(key,  customercolor.replace("menufontColor", menufontColor) + " !important");
 						}
 						if (customercolor.contains("copyRight") && StringUtils.isNotEmpty(copyRight)) {
 							fields.put(key,  customercolor.replace("copyRight", copyRight) + " !important");
 						}
 						if (customercolor.contains("disabledLabelColor") && StringUtils.isNotEmpty(disabledLabelColor)) {
 							fields.put(key,  customercolor.replace("disabledLabelColor", disabledLabelColor) + " !important");
 						}
 						if (customercolor.contains("loginlogo") && StringUtils.isNotEmpty(loginLogo)) {
 							fields.put(key,  customercolor.replace("loginlogo", loginLogo) + " !important");
 						}
 						if (customercolor.contains("logopadding")&& StringUtils.isNotEmpty(logoPadding)) {
 							fields.put(key,  customercolor.replace("logopadding", logoPadding) + " !important");
 						}
 						if (StringUtils.isNotEmpty(gradientTop) && StringUtils.isNotEmpty(gradientBottom)) {
 							if (customercolor.contains("gradientBottom") && StringUtils.isNotEmpty(gradientBottom) && 
 									(customercolor.contains("gradientTop") && StringUtils.isNotEmpty(gradientTop))) {
 								fields.put(key, customercolor.replace("gradientBottom", gradientBottom).replaceAll("gradientTop", gradientTop)) ;
 							}
 						} else if (customercolor.contains(gradientvar)) {
 							fields.put(key,  gradientvar.replace(gradientvar, brandingColor + " !important"));
 						} else if (customercolor.contains(filter)) {
 							fields.put(key,  filter.replace(filter,""));
 						}
 					}
 				}
 				jsonObject.put("customerid", customer.getId());
 				return jsonObject;
 	        }
 		} catch (PhrescoException e) {
 			S_LOGGER.error(e.getMessage());
 		} catch (ParseException e) {
 			S_LOGGER.error(e.getMessage());
 		} catch (Exception e) {
 			S_LOGGER.error(e.getMessage());
 		}
 		return null;
 	}
 	
 	
 	private static String getStringFromInputStream(InputStream is) throws Exception {
 		BufferedReader br = null;
 		StringBuilder sb = new StringBuilder();
 		String line;
 		try {
 			br = new BufferedReader(new InputStreamReader(is));
 			while ((line = br.readLine()) != null) {
 				sb.append(line);
 			}
  
 		} catch (IOException e) {
 			e.printStackTrace();
 		} finally {
 			if (br != null) {
 				try {
 					br.close();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 		return sb.toString();
 	}
 	
 	private String getimage(String id) throws PhrescoException, IOException {
 		
 		Client client = ClientHelper.createClient();
 		FrameworkConfiguration configuration = PhrescoFrameworkFactory.getFrameworkConfig();
         WebResource resource = client.resource(configuration.getServerPath() + "/admin/icon");
         
         if (isDebugEnabled) {
 			S_LOGGER.debug("url hit is -->"+configuration.getServerPath() + "/admin/icon");
 		}
         
         resource = resource.queryParam("id", id);
         resource.accept(MediaType.MULTIPART_FORM_DATA);
         ClientResponse response = resource.get(ClientResponse.class);
         InputStream fileInputStream = response.getEntityInputStream();
 		byte[] imgByte = null;
 		imgByte = IOUtils.toByteArray(fileInputStream);
 	    byte[] encodedImage = Base64.encodeBase64(imgByte);
         String encodeImg = new String(encodedImage);
 		return encodeImg;
 	}
 	
 	
 	public void destroy() {
 	}
 	
 
 }
