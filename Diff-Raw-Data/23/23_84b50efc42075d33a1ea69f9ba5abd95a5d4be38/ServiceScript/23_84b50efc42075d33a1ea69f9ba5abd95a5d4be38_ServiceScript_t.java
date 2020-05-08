 package eu.trentorise.smartcampus.services.semantic.impl;
 
 import it.unitn.disi.sweb.webapi.client.WebApiException;
 import it.unitn.disi.sweb.webapi.client.smartcampus.SCWebApiClient;
 import it.unitn.disi.sweb.webapi.model.entity.Entity;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Locale;
 import java.util.Properties;
 
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

 import eu.trentorise.smartcampus.common.Concept;
 import eu.trentorise.smartcampus.common.SemanticHelper;
 import eu.trentorise.smartcampus.services.semantic.data.message.Semantic.Data;
 import eu.trentorise.smartcampus.services.semantic.data.message.Semantic.Result;
 import eu.trentorise.smartcampus.services.semantic.data.message.Semantic.Tag;
 
 public class ServiceScript {
 
 	private static final String SE_HOST = "213.21.154.85";
 	private static final int SE_PORT = 8080;
 	private static final String KEY_HOST = "host";
 	private static final String KEY_PORT = "port";
 
	static Log logger = LogFactory.getLog(ServiceScript.class);
	
 	private static SCWebApiClient client = null;
 	static {
 		Properties props = new Properties();
 		ClassLoader original = Thread.currentThread().getContextClassLoader();
 		try {
 			props.load(ServiceScript.class.getResourceAsStream("server.properties"));
 			Thread.currentThread().setContextClassLoader(SCWebApiClient.class.getClassLoader());
 			client = SCWebApiClient.getInstance(Locale.ENGLISH, props.getProperty(KEY_HOST), Integer.parseInt(props.getProperty(KEY_PORT)));
 		} catch (Throwable e) {
			logger.error(e);
 			client = SCWebApiClient.getInstance(Locale.ENGLISH,SE_HOST, SE_PORT);
 		}
 		try {
			SemanticHelper.getSCCommunityEntityBase(client);
 		} catch (WebApiException e) {
			logger.error(e);
 		} finally {
 			Thread.currentThread().setContextClassLoader(original);
 		}
 	}
 	
 	public static Result create(Long actorId, Data data) {
 		try {
 			Entity e = SemanticHelper.createEntity(client, actorId, data.getType(), data.getName(), data.getDescription(), convertConcepts(data.getTagList()), data.getRelationList());
 			return Result.newBuilder().setId(e.getId()).setStatus(true).build();
 		} catch (WebApiException e) {
			logger.error(e);
 			return Result.newBuilder().setStatus(false).build();
 		}
 	}
 
 	public static Result createSC(Data data) {
 		try {
 			Entity e = SemanticHelper.createSCEntity(client, data.getType(), data.getName(), data.getDescription(), convertConcepts(data.getTagList()), data.getRelationList());
 			return Result.newBuilder().setId(e.getId()).setStatus(true).build();
 		} catch (WebApiException e) {
			logger.error(e);
 			return Result.newBuilder().setStatus(false).build();
 		}
 	}
 	
 	private static List<Concept> convertConcepts(List<Tag> tagList) {
 		List<Concept> list = new ArrayList<Concept>();
 		if (tagList != null && !tagList.isEmpty()) {
 			for (Tag tag : tagList) {
 				Concept c = new Concept();
 				c.setId(tag.getId());
 				c.setName(tag.getName());
 				c.setSummary(tag.getSummary());
 				c.setDescription(tag.getDescription());
 				list.add(c);
 			}
 		}
 		return list;
 	}
 	
 	public static Result update(Long id, Data data){
 		try {
 			SemanticHelper.updateEntity(client, id, data.getName(), data.getDescription(), convertConcepts(data.getTagList()), data.getRelationList());
 			return Result.newBuilder().setId(id).setStatus(true).build();
 		} catch (WebApiException e) {
			logger.error(e);
 			return Result.newBuilder().setId(id).setStatus(false).build();
 		}
 	}
 	public static Result delete(Long id){
 		try {
 			boolean result = SemanticHelper.deleteEntity(client, id);
 			return Result.newBuilder().setId(id).setStatus(result).build();
 		} catch (WebApiException e) {
			logger.error(e);
 			return Result.newBuilder().setId(id).setStatus(false).build();
 		}
 	}
 }
