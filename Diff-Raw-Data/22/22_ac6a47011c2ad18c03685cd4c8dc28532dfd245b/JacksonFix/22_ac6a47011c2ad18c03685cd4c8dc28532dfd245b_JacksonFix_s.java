 package org.safaproject.safa.web.jackson;
 
import javax.annotation.PostConstruct;

 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.http.converter.HttpMessageConverter;
 import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter;
import org.springframework.stereotype.Component;
 import org.springframework.web.servlet.mvc.annotation.AnnotationMethodHandlerAdapter;
 
@Component
 public class JacksonFix {
 	private AnnotationMethodHandlerAdapter annotationMethodHandlerAdapter;
 	private HibernateAwareObjectMapper objectMapper;
 
	@PostConstruct
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
