 package no.niths.application.rest.misc;
 
 import java.io.IOException;
 import java.lang.reflect.Field;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import no.niths.application.rest.RESTConstants;
 import no.niths.application.rest.lists.RestResourceList;
 import no.niths.application.rest.misc.interfaces.DiscoverabilityController;
 import no.niths.common.constants.DomainConstantNames;
 
 import org.springframework.core.io.Resource;
 import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
 import org.springframework.core.io.support.ResourcePatternResolver;
 import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
 import org.springframework.core.type.classreading.MetadataReader;
 import org.springframework.core.type.classreading.MetadataReaderFactory;
 import org.springframework.http.MediaType;
 import org.springframework.security.access.prepost.PreAuthorize;
 import org.springframework.stereotype.Controller;
 import org.springframework.util.ClassUtils;
 import org.springframework.util.SystemPropertyUtils;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.bind.annotation.ResponseStatus;
 
 /**
  * Simple discover ability class that handles GET requests to the ROOT URI
  * <p>
  * Returns all valid domain in header
  * 
  */
 @Controller
 public class DiscoverabilityControllerImpl implements DiscoverabilityController {
 
	private RestResourceList list = new RestResourceList();
 	/**
      * 
      * Returns a list of all services in the API.
      * <p>
      * Contains path, method type, path variables,
      * status messages, status code, headers and
      * authorization
      * </p>
      * @param req the http header, specifies return format
      * @return a list of resources in the API
      */ 
 	@SuppressWarnings({ "unchecked", "rawtypes" })
 	@Override
 	@RequestMapping(value = "api", method = RequestMethod.GET, headers = RESTConstants.ACCEPT_HEADER)
 	@ResponseBody
 	public List<RestResource> getApi(HttpServletRequest req) {
 
		list.clear();
			
 		// Get all classes in given package with @controller
 		// Read class annotations and then read all method annotations
 		try {
 			List<Class> all = findMyTypes("no.niths.application.rest");
 			for (Class c : all) {
 				RestResource resource = null;
 				if (c.getAnnotation(RequestMapping.class) != null) {
 					RequestMapping mapClass = (RequestMapping) c
 							.getAnnotation(RequestMapping.class);
 					String url = req.getRequestURL().toString().replace("api", "");
 					url += mapClass.value()[0] + "/";
 					resource = new RestResource(url);
 
 					Method[] ms = c.getMethods();
 					for (int i = 0; i < ms.length; i++) {
 						String valuesS = "";
 						String methodS = "";
 						String headersS = "";
 						String reasonS = "";
 						String responseCodeS = "";
 						String resAuth = " ";
 						if (ms[i].getAnnotation(RequestMapping.class) != null) {
 							RequestMapping map = (RequestMapping) ms[i]
 									.getAnnotation(RequestMapping.class);
 							String[] head = map.headers();
 							for (String h : head) {
 								headersS += h + " ";
 							}
 							String[] values = map.value();
 							for (String v : values) {
 								valuesS +=  url + v + " ";
 							}
 							if(valuesS.equals("")) valuesS = url;
 							RequestMethod[] methods = map.method();
 							for (RequestMethod rm : methods) {
 								methodS += rm.name() + " ";
 							}
 							ResponseStatus status = ms[i]
 									.getAnnotation(ResponseStatus.class);
 							if (status != null) {
 								reasonS = status.reason();
 								responseCodeS = status.value().toString();
 							}
 
 							PreAuthorize pre = ms[i]
 									.getAnnotation(PreAuthorize.class);
 							if (pre != null) { //Extract the role names from EL
 								Pattern pattern = Pattern.compile("ROLE_([\\w]*)");
 							    Matcher matcher = pattern.matcher(pre.value());
 
 							    while (matcher.find()) {
 							      resAuth += matcher.group()  + " ";
 							    }
 							    
 							}
 							resource.getMethods().add(new MethodInfo(valuesS.trim(), methodS.trim(), headersS.trim(),
 									reasonS.trim(), responseCodeS.trim(), resAuth.trim()));
 						}
 					}
 					list.add(resource);
 				}
 			}
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
		list.setData(list);
 		return list;
 	}
 
 	@Override
 	@RequestMapping(value = "/", method = RequestMethod.GET)
 	@ResponseBody
 	public String root(HttpServletRequest req, HttpServletResponse res) {
 		res.setContentType(MediaType.APPLICATION_JSON_VALUE);
 
 		return buildURLs(req.getRequestURL().toString());
 
 	}
 
 	@SuppressWarnings("rawtypes")
 	private List<Class> findMyTypes(String basePackage) throws IOException,
 			ClassNotFoundException {
 		ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
 		MetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory(
 				resourcePatternResolver);
 
 		List<Class> candidates = new ArrayList<Class>();
 		String packageSearchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX
 				+ resolveBasePackage(basePackage) + "/" + "**/*.class";
 		Resource[] resources = resourcePatternResolver
 				.getResources(packageSearchPath);
 		for (Resource resource : resources) {
 			if (resource.isReadable()) {
 				MetadataReader metadataReader = metadataReaderFactory
 						.getMetadataReader(resource);
 				if (isCandidate(metadataReader)) {
 					candidates.add(Class.forName(metadataReader
 							.getClassMetadata().getClassName()));
 				}
 			}
 		}
 		return candidates;
 	}
 
 	private String resolveBasePackage(String basePackage) {
 		return ClassUtils.convertClassNameToResourcePath(SystemPropertyUtils
 				.resolvePlaceholders(basePackage));
 	}
 
 	@SuppressWarnings({ "rawtypes", "unchecked" })
 	private boolean isCandidate(MetadataReader metadataReader)
 			throws ClassNotFoundException {
 		try {
 			Class c = Class.forName(metadataReader.getClassMetadata()
 					.getClassName());
 			if (c.getAnnotation(Controller.class) != null) {
 				return true;
 			}
 		} catch (Throwable e) {
 		}
 		return false;
 	}
 
 	private String buildURLs(String rootURL) {
 		StringBuilder jsonBuilder = new StringBuilder("[");
 
 		try {
 			for (Field field : DomainConstantNames.class.getFields()) {
 
 				jsonBuilder.append(String.format("{ \"url\": \"%s%s\"},",
 						rootURL, field.get(null)));
 			}
 		} catch (IllegalArgumentException | IllegalAccessException e) {
 			e.printStackTrace();
 		}
 
 		return jsonBuilder.deleteCharAt(jsonBuilder.length() - 1).append(']')
 				.toString();
 	}
 }
