 package com.gmail.at.zhuikov.aleksandr.view.json;
 
 import java.util.Map;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.data.domain.Page;
import org.springframework.web.servlet.view.json.MappingJacksonJsonView;
 
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.gmail.at.zhuikov.aleksandr.root.domain.xml.XmlFriendlyPage;
 
 public abstract class AbstractPageJsonView<T> extends MappingJackson2JsonView {
 
 	@Override
 	protected Object filterModel(Map<String, Object> model) {
 		return new XmlFriendlyPage<T>((Page<T>) model.get("page"));
 	}
 	
 	@Autowired
 	@Override
 	public void setObjectMapper(ObjectMapper objectMapper) {
 		super.setObjectMapper(objectMapper);
 	}
 }
