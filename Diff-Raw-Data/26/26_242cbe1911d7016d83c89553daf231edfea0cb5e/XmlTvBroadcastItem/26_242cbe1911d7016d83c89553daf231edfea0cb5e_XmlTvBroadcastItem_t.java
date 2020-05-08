 package org.atlasapi.feeds.xmltv;
 
 import org.atlasapi.media.entity.Broadcast;
 import org.atlasapi.media.entity.Container;
 import org.atlasapi.media.entity.Item;
 import org.atlasapi.media.entity.Series;
 import org.atlasapi.media.entity.Version;
 
 public class XmlTvBroadcastItem implements Comparable<XmlTvBroadcastItem> {
 
     private final Item item;
     private final Version version;
     private final Broadcast broadcast;
     private Container container;
     private Series series;
 
     public XmlTvBroadcastItem(Item item, Version version, Broadcast broadcast) {
         this.item = item;
         this.version = version;
         this.broadcast = broadcast;
     }
 
     public Item getItem() {
         return item;
     }
 
     public Version getVersion() {
         return version;
     }
 
     public Broadcast getBroadcast() {
         return broadcast;
     }
 
     @Override
     public int compareTo(XmlTvBroadcastItem that) {
         return this.broadcast.getTransmissionTime().compareTo(that.getBroadcast().getTransmissionTime());
     }
 
     public XmlTvBroadcastItem withContainer(Container container) {
         this.container = container;
         return this;
     }
     
     public Container getContainer() {
         return this.container;
     }
 
     public boolean hasContainer() {
         return container != null;
     }
     
     public XmlTvBroadcastItem withSeries(Series series) {
         this.series = series;
         return this;
     }
 
     public Series getSeries() {
         return series;
     }

    public boolean hasSeries() {
        return series != null;
    }
     
 }
