 package org.github.dx88968.monitor.restlet.resources;
 
 import java.io.IOException;
 import java.util.Iterator;
 import java.util.List;
 
 import org.github.dx88968.monitor.logger.DirectOutputTracker;
 import org.github.dx88968.monitor.logger.Pipeline;
 import org.github.dx88968.monitor.logger.Session;
 import org.github.dx88968.monitor.utils.AddressUtil;
 import org.github.dx88968.monitor.utils.Constants;
 import org.json.simple.JSONArray;
 import org.json.simple.JSONObject;
 
 import org.restlet.data.Form;
 import org.restlet.data.MediaType;
 import org.restlet.data.Reference;
 import org.restlet.representation.Representation;
 import org.restlet.representation.StringRepresentation;
 import org.restlet.resource.Delete;
 import org.restlet.resource.Get;
 import org.restlet.resource.Options;
 import org.restlet.resource.Post;
 import org.restlet.resource.ServerResource;
 
 public class PipelineResource extends ServerResource{
 	
 	@Options
     public void doOptions(Representation entity) {
         Form responseHeaders = (Form) getResponse().getAttributes().get("org.restlet.http.headers");
         if (responseHeaders == null) {
             responseHeaders = new Form();
             getResponse().getAttributes().put("org.restlet.http.headers", responseHeaders);
         }
         responseHeaders.add("Access-Control-Allow-Origin", "*");
         responseHeaders.add("Access-Control-Allow-Methods", "GET,POST,OPTIONS,DELETE");
         responseHeaders.add("Access-Control-Allow-Headers", "Content-Type");
         responseHeaders.add("Access-Control-Allow-Credentials", "false");
         responseHeaders.add("Access-Control-Max-Age", "60");
     }
 	
 	@SuppressWarnings("unchecked")
 	@Get
 	public Representation handleGet(){
 		try{
 			 Form responseHeaders = (Form) getResponse().getAttributes().get("org.restlet.http.headers");
 			 if (responseHeaders == null) {
 	            responseHeaders = new Form();
 	            getResponse().getAttributes().put("org.restlet.http.headers", responseHeaders);
 	        }
 	        responseHeaders.add("Access-Control-Allow-Origin", "*");
 	        
 			//System.out.println(Request.getCurrent().getClientInfo().getAddress()+":"+Request.getCurrent().getClientInfo().getPort());
 	        String hash = extractIDFromURL(getReference());
 			Pipeline pipeline=DirectOutputTracker.instance.getPipeline(hash);
 			JSONObject returnMessage=new JSONObject();
 			returnMessage.put("title", "Tracer");
 			
 			if (pipeline==null) {
 				returnMessage.put("error", "This pipeline does not exist or is no longer active");
 			}else{
 				Session session;
 				Form queryParams = getRequest().getResourceRef().getQueryAsForm();
 				String key = queryParams.getFirstValue("key");
 				String timestamp=queryParams.getFirstValue("timestamp");
 				//System.out.println("key received is "+key);
 				//System.out.println("timestamp is "+timestamp);
 				session=pipeline.getSession(Integer.parseInt(key),timestamp);
 				List<String> lines = session.getBuffer();
 				JSONArray lineArray=new JSONArray();
 				Iterator<String> iter = lines.iterator();
 				while (iter.hasNext()) {
 					lineArray.add(iter.next());
 				}
 				returnMessage.put("append", lineArray);
 			}
 			//System.out.println(returnMessage.toString());
 			Representation  rep = new StringRepresentation(returnMessage.toJSONString(),MediaType.APPLICATION_JSON);
 			return rep;
 		} catch (Exception e) {
 			//e.printStackTrace();
 			JSONObject returnMessage=new JSONObject();
			returnMessage.put("error", "session expires");
 			Representation  rep = new StringRepresentation(returnMessage.toJSONString(),MediaType.APPLICATION_JSON);
 			return rep;
 		}
 	}
 	
 	@SuppressWarnings("unchecked")
 	@Post
 	public Representation handlePost(Representation entity){
 		try{
 			 Form responseHeaders = (Form) getResponse().getAttributes().get("org.restlet.http.headers");
 			 if (responseHeaders == null) {
 	            responseHeaders = new Form();
 	            getResponse().getAttributes().put("org.restlet.http.headers", responseHeaders);
 	        }
 	        responseHeaders.add("Access-Control-Allow-Origin", "*");
 	        
 			JSONObject returnMessage=new JSONObject();
 			String hash = extractIDFromURL(getReference());
 			Pipeline pipeline=DirectOutputTracker.instance.getPipeline(hash);
 			if (pipeline==null) {
 				returnMessage.put("error", "This pipeline does not exist or is no longer active");
 			}else{
 				returnMessage.put("info", AddressUtil.constructJsonObject(getReference(), pipeline));
 				Session session;
 				session=pipeline.createSession();
 				returnMessage.put("sessionkey", session.getKey());
 				returnMessage.put("timestamp", session.getTimestamp());
 			}
 			Representation  rep = new StringRepresentation(returnMessage.toJSONString(),MediaType.APPLICATION_JSON);
 			return rep;
 		} catch (Exception e) {
 			JSONObject returnMessage=new JSONObject();
 			returnMessage.put("error", "This pipeline does not exist or is no longer active");
 			Representation  rep = new StringRepresentation(returnMessage.toJSONString(),MediaType.APPLICATION_JSON);
 			return rep;
 		}
 	}
 	
 	@SuppressWarnings("unchecked")
 	@Delete
 	public Representation handleDelete(){
 		try{
 			 Form responseHeaders = (Form) getResponse().getAttributes().get("org.restlet.http.headers");
 			 if (responseHeaders == null) {
 	            responseHeaders = new Form();
 	            getResponse().getAttributes().put("org.restlet.http.headers", responseHeaders);
 	        }
 	        responseHeaders.add("Access-Control-Allow-Origin", "*");
 	        String hash=extractIDFromURL(getReference());
 	        Pipeline pipeline=DirectOutputTracker.instance.getPipeline(hash);
 	        if (pipeline!=null) {
 		        pipeline.stop();
 			}
 			JSONObject returnMessage=new JSONObject();
 			returnMessage.put("message", "success");
 			returnMessage.put("info", AddressUtil.constructJsonObject(getReference(), pipeline));
 			Representation  rep = new StringRepresentation(returnMessage.toJSONString(),MediaType.APPLICATION_JSON);
 			return rep;
 		} catch (Exception e) {
 			e.printStackTrace();
 			return new StringRepresentation("{message:error}");
 		}
 	}
 	
 	private String extractIDFromURL(Reference ref) throws IOException{
 		String host=ref.getHostDomain()+":"+ref.getHostPort();
 		try{
 			return ref.toString().split(host)[1].split("\\?")[0].split(Constants.IDInterval)[1];
 		}catch(Exception ex){
 			throw new IOException("Illegal url for extracting id");
 		}
 	}
 
 }
