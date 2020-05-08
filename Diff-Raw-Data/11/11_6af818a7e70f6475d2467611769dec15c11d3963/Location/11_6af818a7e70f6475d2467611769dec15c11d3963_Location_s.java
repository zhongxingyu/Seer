 package org.atlasapi.media.entity.simple;
 /* Copyright 2010 Meta Broadcast Ltd
 
 Licensed under the Apache License, Version 2.0 (the "License"); you
 may not use this file except in compliance with the License. You may
 obtain a copy of the License at
 
 http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 implied. See the License for the specific language governing
 permissions and limitations under the License. */
 
 import java.util.Date;
 import java.util.Set;
 
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlElementWrapper;
 import javax.xml.bind.annotation.XmlRootElement;
 import javax.xml.bind.annotation.XmlType;
 
 import org.atlasapi.media.TransportType;
 import org.atlasapi.media.vocabulary.PLAY_SIMPLE_XML;
 
 import com.google.common.base.Strings;
 
 
 @XmlRootElement(namespace=PLAY_SIMPLE_XML.NS)
 @XmlType(name="location", namespace=PLAY_SIMPLE_XML.NS)
 public class Location extends Version {
 	
     private Integer advertisingDuration;
     private Integer audioBitRate;
     private Integer audioChannels;
     private String audioCoding;
     private Integer bitRate;
     private Boolean containsAdvertising;
     private String dataContainerFormat;
     private Long dataSize;
     private String distributor;
     private Boolean hasDOG;
     private String source;
     private String videoAspectRatio;
     private Integer videoBitRate;
     private String videoCoding;
     private Float videoFrameRate;
     private Integer videoHorizontalSize;
     private Boolean videoProgressiveScan;
     private Integer videoVerticalSize;
     
 	private Date availabilityStart;
 	private Date availabilityEnd;
     private Date drmPlayableFrom;
     private Set<String> availableCountries; 
     private String currency;
     private Integer price;
     private String revenueContract;
     
     private String restrictedBy;
     
     private Boolean transportIsLive;
     private String transportSubType;
     private String transportType;
     private String uri;
     private String embedCode;
     
     private boolean available = true;
 	
 	public String getUri() {
 		return uri;
 	}
 	
 	public void setUri(String uri) {
 		this.uri = uri;
 	}
 
 	public String getDataContainerFormat() {
 		return dataContainerFormat;
 	}
 	
 	public void setDataContainerFormat(String dataContainerFormat) {
 		this.dataContainerFormat = dataContainerFormat;
 	}
 
 	@XmlElementWrapper(namespace=PLAY_SIMPLE_XML.NS, name="availableCountries")
 	@XmlElement(name="code")
 	public Set<String> getAvailableCountries() {
 		return availableCountries;
 	}
 
 	public void setAvailableCountries(Set<String> availableCountries) {
 		this.availableCountries = availableCountries;
 	}
 
 	public String getCurrency() {
         return currency;
     }
 	
 	public void setCurrency(String currency) {
         this.currency = currency;
     }
 	
 	public Integer getPrice() {
         return price;
     }
 	
 	public void setPrice(Integer price) {
         this.price = price;
     }
 	
 	public String getRevenueContract() {
         return revenueContract;
     }
 	
 	public void setRevenueContract(String revenueContract) {
         this.revenueContract = revenueContract;
     }
 
 	public Integer getAdvertisingDuration() {
 		return advertisingDuration;
 	}
 
 	public void setAdvertisingDuration(Integer advertisingDuration) {
 		this.advertisingDuration = advertisingDuration;
 	}
 
 	public Integer getAudioBitRate() {
 		return audioBitRate;
 	}
 
 	public void setAudioBitRate(Integer audioBitRate) {
 		this.audioBitRate = audioBitRate;
 	}
 
 	public Integer getAudioChannels() {
 		return audioChannels;
 	}
 
 	public void setAudioChannels(Integer audioChannels) {
 		this.audioChannels = audioChannels;
 	}
 
 	public String getAudioCoding() {
 		return audioCoding;
 	}
 
 	public void setAudioCoding(String audioCoding) {
 		this.audioCoding = audioCoding;
 	}
 
 	public Integer getBitRate() {
 		return bitRate;
 	}
 
 	public void setBitRate(Integer bitRate) {
 		this.bitRate = bitRate;
 	}
 
 	public Boolean getContainsAdvertising() {
 		return containsAdvertising;
 	}
 
 	public void setContainsAdvertising(Boolean containsAdvertising) {
 		this.containsAdvertising = containsAdvertising;
 	}
 
 	public Long getDataSize() {
 		return dataSize;
 	}
 
 	public void setDataSize(Long dataSize) {
 		this.dataSize = dataSize;
 	}
 
 	public String getDistributor() {
 		return distributor;
 	}
 
 	public void setDistributor(String distributor) {
 		this.distributor = distributor;
 	}
 
 	public Boolean getHasDOG() {
 		return hasDOG;
 	}
 
 	public void setHasDOG(Boolean hasDOG) {
 		this.hasDOG = hasDOG;
 	}
 
 	public String getSource() {
 		return source;
 	}
 
 	public void setSource(String source) {
 		this.source = source;
 	}
 
 	public String getVideoAspectRatio() {
 		return videoAspectRatio;
 	}
 
 	public void setVideoAspectRatio(String videoAspectRatio) {
 		this.videoAspectRatio = videoAspectRatio;
 	}
 
 	public Integer getVideoBitRate() {
 		return videoBitRate;
 	}
 
 	public void setVideoBitRate(Integer videoBitRate) {
 		this.videoBitRate = videoBitRate;
 	}
 
 	public String getVideoCoding() {
 		return videoCoding;
 	}
 
 	public void setVideoCoding(String videoCoding) {
 		this.videoCoding = videoCoding;
 	}
 
 	public Float getVideoFrameRate() {
 		return videoFrameRate;
 	}
 
 	public void setVideoFrameRate(Float videoFrameRate) {
 		this.videoFrameRate = videoFrameRate;
 	}
 
 	public Integer getVideoHorizontalSize() {
 		return videoHorizontalSize;
 	}
 
 	public void setVideoHorizontalSize(Integer videoHorizontalSize) {
 		this.videoHorizontalSize = videoHorizontalSize;
 	}
 
 	public Boolean getVideoProgressiveScan() {
 		return videoProgressiveScan;
 	}
 
 	public void setVideoProgressiveScan(Boolean videoProgressiveScan) {
 		this.videoProgressiveScan = videoProgressiveScan;
 	}
 
 	public Integer getVideoVerticalSize() {
 		return videoVerticalSize;
 	}
 
 	public void setVideoVerticalSize(Integer videoVerticalSize) {
 		this.videoVerticalSize = videoVerticalSize;
 	}
 
 	public Date getAvailabilityStart() {
 		return availabilityStart;
 	}
 
 	public void setAvailabilityStart(Date availabilityStart) {
 		this.availabilityStart = availabilityStart;
 	}
 
 	public Date getDrmPlayableFrom() {
 		return drmPlayableFrom;
 	}
 
 	public void setDrmPlayableFrom(Date drmPlayableFrom) {
 		this.drmPlayableFrom = drmPlayableFrom;
 	}
 
 	public String getRestrictedBy() {
 		return restrictedBy;
 	}
 
 	public void setRestrictedBy(String restrictedBy) {
 		this.restrictedBy = restrictedBy;
 	}
 
 	public Boolean getTransportIsLive() {
 		return transportIsLive;
 	}
 
 	public void setTransportIsLive(Boolean transportIsLive) {
 		this.transportIsLive = transportIsLive;
 	}
 
 	public String getTransportSubType() {
 		return transportSubType;
 	}
 
 	public void setTransportSubType(String transportSubType) {
 		this.transportSubType = transportSubType;
 	}
 
 	public String getTransportType() {
 		return transportType;
 	}
 
 	public void setTransportType(String transportType) {
 		this.transportType = transportType;
 	}
 
 	public String getEmbedCode() {
 		return embedCode;
 	}
 
 	public void setEmbedCode(String embedCode) {
 		this.embedCode = embedCode;
 	}
 
 	public boolean isAvailable() {
 		return available;
 	}
 
 	public void setAvailable(boolean available) {
 		this.available = available;
 	}
 	
 	public void setAvailabilityEnd(Date availabilityEnd) {
 		this.availabilityEnd = availabilityEnd;
 	}
 	
 	public Date getAvailabilityEnd() {
 		return availabilityEnd;
 	}
 	
 	@Override
 	public boolean equals(Object obj) {
 	    if (obj instanceof Location) {
 	        Location target = (Location) obj;
 	        if (isEmbed()) {
 	            return transportType.equals(target.transportType) && embedCode.equals(target.embedCode);
	        } else {
 	            return transportType.equals(target.transportType) && uri.equals(target.uri);
 	        }
 	    }
 	    return false;
 	}
 	
 	@Override
 	public int hashCode() {
 	    if (isEmbed()) {
             return embedCode.hashCode();
        } else {
             return uri.hashCode();
         }
 	}
 	
 	@Override
 	public String toString() {
 	    if (isEmbed()) {
             return "embed location: "+embedCode;
         } else {
             return transportType+" location: "+uri;
         }
 	}
 	
 	private boolean isEmbed() {
 	    return TransportType.EMBED.toString().equals(transportType) && !Strings.isNullOrEmpty(embedCode);
 	}
 }
