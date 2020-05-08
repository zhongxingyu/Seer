 package com.github.gabrielmoreira.propriete.source;
 
 import com.github.gabrielmoreira.propriete.transformer.MapTransformer;
 
 public class SystemPropertiesConfigSource extends PropertiesConfigSource {
 
 	public SystemPropertiesConfigSource() {
		this(null);
 	}
 
 	public SystemPropertiesConfigSource(MapTransformer mapTransformer) {
 		super(System.getProperties(), mapTransformer);
 	}
 
 }
