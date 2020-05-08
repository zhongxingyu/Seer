 /*
  * Copyright (C) 2013 XStream committers.
  * All rights reserved.
  *
  * Created on 01.05.2013 by Joerg Schaible
  */
 package org.codehaus.xsite.extractors;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import com.opensymphony.module.sitemesh.html.rules.PageBuilder;
 
 /**
  * A PageBuilder that supports custom attributes and can be used for DI.
  * 
  * @author J&ouml;rg Schaible
  */
 public class AttributedPageBuilder implements PageBuilder {
 
 	private final CharacterEscaper characterEscaper;
 	private final Map<String, String> properties;
 	
 	public AttributedPageBuilder(CharacterEscaper characterEscaper) {
 		this.characterEscaper = characterEscaper;
 		this.properties = new HashMap<String, String>();
 	}
 	
 	@Override
 	public void addProperty(String key, String value) {
         properties.put(key, characterEscaper.escape(value));
 	}
 	
	Map<String, String> getProperties() {
 		return properties;
 	}
 }
