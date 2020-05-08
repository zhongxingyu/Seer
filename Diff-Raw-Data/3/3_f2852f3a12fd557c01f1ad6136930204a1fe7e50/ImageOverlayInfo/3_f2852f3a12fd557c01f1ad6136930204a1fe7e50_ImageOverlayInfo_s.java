 package com.parworks.androidlibrary.response;
 
 import java.io.IOException;
 import java.io.Serializable;
 import java.util.List;
 
 import com.fasterxml.jackson.core.JsonProcessingException;
 import com.fasterxml.jackson.databind.DeserializationFeature;
 import com.fasterxml.jackson.databind.ObjectMapper;
 
 @SuppressWarnings("serial")
 public class ImageOverlayInfo implements Serializable {
 
 	private String site;
 	private String content;
 	private String id;
 	private String imageId;
 	private String accuracy;
 	private String name;
 	private List<OverlayPoint> points;
 
 	/** 
 	 * The configuration object by parsing the content value.
 	 * 
 	 * The reason to not replace content String with this object
 	 * is to better handle old overlay content without the JSON
 	 * format.
 	 */
 	private OverlayConfiguration configuration;
 	
 	public String getSite() {
 		return site;
 	}
 
 	public void setSite(String site) {
 		this.site = site;
 	}
 
 	public String getContent() {
 		return content;
 	}
 
 	public void setContent(String content) {
 		this.content = content;
 		
 		// parse the content whenever this is set
 		parseOverlayContent();
 	}
 
 	public String getId() {
 		return id;
 	}
 
 	public void setId(String id) {
 		this.id = id;
 	}
 
 	public String getImageId() {
 		return imageId;
 	}
 
 	public void setImageId(String imageId) {
 		this.imageId = imageId;
 	}
 
 	public String getAccuracy() {
 		return accuracy;
 	}
 
 	public void setAccuracy(String accuracy) {
 		this.accuracy = accuracy;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public List<OverlayPoint> getPoints() {
 		return points;
 	}
 
 	public void setPoints(List<OverlayPoint> points) {
 		this.points = points;
 	}
 
 	public OverlayConfiguration getConfiguration() {
 		return configuration;
 	}
 
 	private void parseOverlayContent() {
 		ObjectMapper mapper = new ObjectMapper();
 		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
 		try {
			this.configuration = mapper.readValue(content, OverlayConfiguration.class);
 		} catch (IOException e) {
 			// when failing to parse the overlay content,
 			// generate an empty object and use default for everything
 			this.configuration = new OverlayConfiguration();			
 		}
 	}
 }
