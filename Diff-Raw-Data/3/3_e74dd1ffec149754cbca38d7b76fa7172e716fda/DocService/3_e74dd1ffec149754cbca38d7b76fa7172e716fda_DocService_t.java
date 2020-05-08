 package com.meadowhawk.homepi.util.service;
 
 import java.io.UnsupportedEncodingException;
 import java.lang.annotation.Annotation;
 import java.lang.reflect.Method;
 import java.net.URLDecoder;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.regex.Pattern;
 
 import javax.ws.rs.Consumes;
 import javax.ws.rs.DELETE;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.PUT;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.beans.factory.config.BeanDefinition;
 import org.springframework.beans.factory.support.BeanDefinitionRegistry;
 import org.springframework.beans.factory.support.SimpleBeanDefinitionRegistry;
 import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
 import org.springframework.core.type.filter.AnnotationTypeFilter;
 import org.springframework.core.type.filter.RegexPatternTypeFilter;
 import org.springframework.core.type.filter.TypeFilter;
 import org.springframework.http.HttpMethod;
 import org.springframework.stereotype.Component;
 
 import com.meadowhawk.homepi.util.StringUtil;
 import com.meadowhawk.homepi.util.model.PublicRESTDoc;
 import com.meadowhawk.homepi.util.model.PublicRESTDocMethod;
 import com.meadowhawk.homepi.util.model.ServiceDocMethodTO;
 import com.meadowhawk.homepi.util.model.ServiceDocTO;
 
 /**
  * Service searches the code base and renders REST API documentation for any services marked with the PublicRESTDoc annotation.
  * @author Lee Clarke
  */
 @Component
 public class DocService {
 	private static final String UNDEFINED = "Undefined";
 	private static Logger log = Logger.getLogger(DocService.class);
 	
 	@Value("#{'${apidocs.scan_packages}'.split(',')}") 
 	private List<String> packagesToScan;
 	
 
 	/**
 	 * Retrieves Service Level Documentation
 	 * @return 
 	 */
 	public List<ServiceDocTO> getEndpointDocs() {
 		List<ServiceDocTO> serviceDocs = new ArrayList<ServiceDocTO>();
 		BeanDefinitionRegistry bdr = new SimpleBeanDefinitionRegistry();
 		ClassPathBeanDefinitionScanner s = new ClassPathBeanDefinitionScanner(bdr);
 
 		TypeFilter tf = new AnnotationTypeFilter(PublicRESTDoc.class);
 		// tf.match(arg0, arg1); ??
 		s.addIncludeFilter(tf);
		//Filter out Spring stuff
		
 		s.scan(packagesToScan.toArray(new String[packagesToScan.size()]));
 		String[] beans = bdr.getBeanDefinitionNames();
 		log.debug("Scanning packages for Docs: " + packagesToScan.size());
 		log.debug("Beans: " + beans.length);
 		for (String beanName : beans) {
 			log.debug(beanName);
 		}
 		for (String bean : beans) {
 			BeanDefinition def = bdr.getBeanDefinition(bean);
 			String packageString = def.getBeanClassName().substring(0,
 					def.getBeanClassName().lastIndexOf('.'));
 			if (packagesToScan.contains(packageString)) {
 				try {
 					boolean isRestDoc = false;
 					Class<?> servClass = Class.forName(def.getBeanClassName());
 
 					Annotation[] classAnnotations = servClass.getAnnotations();
 					ServiceDocTO servDoc = new ServiceDocTO();
 					servDoc.setServiceClass(servClass);
 
 					for (Annotation annotation : classAnnotations) {
 						if (annotation instanceof PublicRESTDoc) {
 							isRestDoc = true;
 							servDoc
 									.setServiceName(((PublicRESTDoc) annotation).serviceName());
 							if (StringUtil.isNullOrEmpty(servDoc.getServiceName())) {
 								servDoc.setServiceName(UNDEFINED); // can't allow a null here.
 							}
 							servDoc.setServiceDescription(((PublicRESTDoc) annotation)
 									.description());
 
 						} else if (annotation instanceof Path) {
 							servDoc.setServicePath(((Path) annotation).value());
 						}
 					}
 
 					// Process Methods.
 					Method[] methods = servClass.getMethods();
 					for (Method method : methods) {
 						List<Annotation> annos = Arrays.asList(method.getAnnotations());
 
 						if (annos.size() > 0) {
 							ServiceDocMethodTO methodDoc = new ServiceDocMethodTO();
 							boolean isDocMethod = false;
 							for (Annotation anno : annos) {
 								if (anno instanceof PublicRESTDocMethod) {
 									isDocMethod = true;
 									methodDoc.setEndPointName(((PublicRESTDocMethod) anno).endPointName());
 									methodDoc.setEndPointMethodName(method.getName());
 									methodDoc.setEndPointDescription(((PublicRESTDocMethod) anno)
 											.description());
 									methodDoc.setSampleLinks(((PublicRESTDocMethod) anno)
 											.sampleLinks());
 									methodDoc
 											.setErrors(((PublicRESTDocMethod) anno).errorCodes());
 								} else if (anno instanceof Path) {
 									methodDoc.setEndPointPath(((Path) anno).value());
 								} else if (anno instanceof Produces) {
 									methodDoc.setEndPointProvides(((Produces) anno).value());
 								} else if (anno instanceof Consumes) {
 									methodDoc.setConsumes(((Consumes) anno).value());
 								} else if (anno instanceof GET) {
 									methodDoc.setEndPointRequestType(HttpMethod.GET.name());
 								} else if (anno instanceof POST) {
 									methodDoc.setEndPointRequestType(HttpMethod.POST.name());
 								} else if (anno instanceof PUT) {
 									methodDoc.setEndPointRequestType(HttpMethod.PUT.name());
 								} else if (anno instanceof DELETE) {
 									methodDoc.setEndPointRequestType(HttpMethod.DELETE.name());
 								}
 							}
 							if (isDocMethod) // Don't add methods that don't have docs.
 								servDoc.getMethodDocs().add(methodDoc);
 						}
 					}
 
 					if (isRestDoc) // Don't add services that don't have docs.
 						serviceDocs.add(servDoc);
 				} catch (ClassNotFoundException e) {
 					// log it, eat it, yum.
 					log.debug(e);
 				}
 			}
 		}
 
 		return serviceDocs;
 	}
 
 	/**
 	 * Returns a single ServiceDoc that matchs the given name.
 	 * @param endPointName - name of Service specified in its Doc Annotation.
 	 * @return ServiceDocTO
 	 */
 	public ServiceDocTO getEndpointDocsByName(String endPointName){
 		List<ServiceDocTO> servDocs = this.getEndpointDocs();
 		for (ServiceDocTO serviceDocTO : servDocs) {
 			String name;
 			try {
 				name = URLDecoder.decode(endPointName,"UTF-8");
 				if(name != null && serviceDocTO.getServiceName().equalsIgnoreCase(name)){
 					return serviceDocTO;
 				}
 			} catch (UnsupportedEncodingException e) {
 				log.debug("Couldn't Decode name:" + endPointName + " " + e);
 			}
 		}
 		
 		ServiceDocTO notFund = new ServiceDocTO();
 		notFund.setServiceName("Service Not found by that name.");
 		return notFund;
 	}
 
 }
