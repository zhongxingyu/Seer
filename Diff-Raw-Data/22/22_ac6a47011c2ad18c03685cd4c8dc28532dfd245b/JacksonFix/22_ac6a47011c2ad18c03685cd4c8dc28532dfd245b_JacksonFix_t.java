 package org.safaproject.safa.web.jackson;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.http.converter.HttpMessageConverter;
 import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter;
 import org.springframework.web.servlet.mvc.annotation.AnnotationMethodHandlerAdapter;
 
 public class JacksonFix {
	
	@Autowired
 	private AnnotationMethodHandlerAdapter annotationMethodHandlerAdapter;
	
	@Autowired
 	private HibernateAwareObjectMapper objectMapper;
 
 	public void init() {
 		HttpMessageConverter<?>[] messageConverters = annotationMethodHandlerAdapter
 				.getMessageConverters();
 		for (HttpMessageConverter<?> messageConverter : messageConverters) {
 			if (messageConverter instanceof MappingJacksonHttpMessageConverter) {
 				MappingJacksonHttpMessageConverter m = (MappingJacksonHttpMessageConverter) messageConverter;
 				m.setObjectMapper(objectMapper);
 			}
 		}
 	}
 
 	@Autowired
 	public void setAnnotationMethodHandlerAdapter(
 			AnnotationMethodHandlerAdapter annotationMethodHandlerAdapter) {
 		this.annotationMethodHandlerAdapter = annotationMethodHandlerAdapter;
 	}
 
 	@Autowired
 	public void setObjectMapper(HibernateAwareObjectMapper objectMapper) {
 		this.objectMapper = objectMapper;
 	}
 }
