 package org.atlasapi.media.entity.simple;
 
 import java.util.List;
 import java.util.Set;
 
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlElementWrapper;
 import javax.xml.bind.annotation.XmlElements;
 import javax.xml.bind.annotation.XmlRootElement;
 import javax.xml.bind.annotation.XmlType;
 
 import org.atlasapi.media.entity.simple.ContentIdentifier.BrandIdentifier;
 import org.atlasapi.media.entity.simple.ContentIdentifier.EpisodeIdentifier;
 import org.atlasapi.media.entity.simple.ContentIdentifier.FilmIdentifier;
 import org.atlasapi.media.entity.simple.ContentIdentifier.ItemIdentifier;
 import org.atlasapi.media.entity.simple.ContentIdentifier.PersonIdentifier;
 import org.atlasapi.media.entity.simple.ContentIdentifier.SeriesIdentifier;
 import org.atlasapi.media.vocabulary.PLAY_SIMPLE_XML;
 
 import com.google.common.base.Function;
 import com.google.common.collect.ImmutableSet;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Sets;
 
 @XmlRootElement(namespace=PLAY_SIMPLE_XML.NS)
 @XmlType(name="playlist", namespace=PLAY_SIMPLE_XML.NS)
 public class Playlist extends Description {
 
 	private List<ContentIdentifier> content = Lists.newArrayList();
     private Set<ContentIdentifier> upcomingContent = Sets.newHashSet();
     private Set<ContentIdentifier> availableContent = Sets.newHashSet();
     private Set<ContentIdentifier> recentContent = Sets.newHashSet();
     private List<SeriesIdentifier> series = Lists.newArrayList();
     private Integer totalEpisodes;
     private Integer seriesNumber;
 
 	public void add(ContentIdentifier c) {
 		content.add(c);
 	}
 	
 	@XmlElementWrapper(namespace=PLAY_SIMPLE_XML.NS, name="content")
 	@XmlElements({ 
 		@XmlElement(name = "item", type = ItemIdentifier.class, namespace=PLAY_SIMPLE_XML.NS),
 		@XmlElement(name = "episode", type = EpisodeIdentifier.class, namespace=PLAY_SIMPLE_XML.NS),
 		@XmlElement(name = "film", type = FilmIdentifier.class, namespace=PLAY_SIMPLE_XML.NS),
 		@XmlElement(name = "person", type = PersonIdentifier.class, namespace=PLAY_SIMPLE_XML.NS),
 		@XmlElement(name = "series", type = SeriesIdentifier.class, namespace=PLAY_SIMPLE_XML.NS),
 		@XmlElement(name = "brand", type = BrandIdentifier.class, namespace=PLAY_SIMPLE_XML.NS) 
 	})
 	public List<ContentIdentifier> getContent() {
 		return content;
 	}
 	
 	public void setContent(Iterable<? extends ContentIdentifier> items) {
 		this.content = Lists.newArrayList(items);
 	}
 	
     @XmlElementWrapper(namespace=PLAY_SIMPLE_XML.NS, name="seriesList")
     @XmlElements({ 
         @XmlElement(name = "series", type = SeriesIdentifier.class, namespace=PLAY_SIMPLE_XML.NS),
 	})
     public List<SeriesIdentifier> getSeriesList() {
         return series;
     }
     
     public void setSeriesList(Iterable<? extends SeriesIdentifier> series) {
         this.series = Lists.newArrayList(series);
     }
 	
 	public Playlist copy() {
 	    Playlist copy = new Playlist();
 	    copyTo(copy);
 	    copy.setContent(Iterables.transform(getContent(), ContentIdentifier.COPY));
         copy.upcomingContent = upcomingContent == null ? null : ImmutableSet.copyOf(Iterables.transform(getUpcomingContent(), ContentIdentifier.COPY));
         copy.availableContent = availableContent == null ? null : ImmutableSet.copyOf(Iterables.transform(getAvailableContent(), ContentIdentifier.COPY));
 	    return copy;
 	}
 	
 	public static final Function<Playlist, List<ContentIdentifier>> TO_CONTENTS = new Function<Playlist, List<ContentIdentifier>>() {
 		@Override
 		public List<ContentIdentifier> apply(Playlist input) {
 			return input.getContent();
 		}
 	};
 
 	public Integer getTotalEpisodes() {
 	    return totalEpisodes;
 	}
 	
 	public void setTotalEpisodes(Integer totalEpisodes) {
 	    this.totalEpisodes = totalEpisodes;
 	}
 	
 	public Integer getSeriesNumber() {
 	    return seriesNumber;
 	}
 	
 	public void setSeriesNumber(Integer seriesNumber) {
 	    this.seriesNumber = seriesNumber;
 	}
 
     public void setUpcomingContent(Iterable<ContentIdentifier> filteredRefs) {
         this.upcomingContent = ImmutableSet.copyOf(filteredRefs);
     }
 
     @XmlElementWrapper(namespace=PLAY_SIMPLE_XML.NS, name="upcoming_content")
     @XmlElements({ 
         @XmlElement(name = "item", type = ItemIdentifier.class, namespace=PLAY_SIMPLE_XML.NS),
         @XmlElement(name = "episode", type = EpisodeIdentifier.class, namespace=PLAY_SIMPLE_XML.NS),
         @XmlElement(name = "film", type = FilmIdentifier.class, namespace=PLAY_SIMPLE_XML.NS),
         @XmlElement(name = "person", type = PersonIdentifier.class, namespace=PLAY_SIMPLE_XML.NS),
         @XmlElement(name = "series", type = SeriesIdentifier.class, namespace=PLAY_SIMPLE_XML.NS),
         @XmlElement(name = "brand", type = BrandIdentifier.class, namespace=PLAY_SIMPLE_XML.NS) 
     })
     public Set<ContentIdentifier> getUpcomingContent() {
         return upcomingContent;
     }
     
     public void setAvailableContent(Iterable<ContentIdentifier> filteredRefs) {
         this.availableContent = ImmutableSet.copyOf(filteredRefs);
     }
 
     @XmlElementWrapper(namespace=PLAY_SIMPLE_XML.NS, name="available_content")
     @XmlElements({ 
         @XmlElement(name = "item", type = ItemIdentifier.class, namespace=PLAY_SIMPLE_XML.NS),
         @XmlElement(name = "episode", type = EpisodeIdentifier.class, namespace=PLAY_SIMPLE_XML.NS),
         @XmlElement(name = "film", type = FilmIdentifier.class, namespace=PLAY_SIMPLE_XML.NS),
         @XmlElement(name = "person", type = PersonIdentifier.class, namespace=PLAY_SIMPLE_XML.NS),
         @XmlElement(name = "series", type = SeriesIdentifier.class, namespace=PLAY_SIMPLE_XML.NS),
         @XmlElement(name = "brand", type = BrandIdentifier.class, namespace=PLAY_SIMPLE_XML.NS) 
     })
     public Set<ContentIdentifier> getAvailableContent() {
         return availableContent;
     }
 
     public void setRecentContent(Iterable<ContentIdentifier> filteredRefs) {
         this.recentContent = ImmutableSet.copyOf(filteredRefs);
     }
     
    @XmlElementWrapper(namespace=PLAY_SIMPLE_XML.NS, name="available_content")
     @XmlElements({ 
         @XmlElement(name = "item", type = ItemIdentifier.class, namespace=PLAY_SIMPLE_XML.NS),
         @XmlElement(name = "episode", type = EpisodeIdentifier.class, namespace=PLAY_SIMPLE_XML.NS),
         @XmlElement(name = "film", type = FilmIdentifier.class, namespace=PLAY_SIMPLE_XML.NS),
         @XmlElement(name = "person", type = PersonIdentifier.class, namespace=PLAY_SIMPLE_XML.NS),
         @XmlElement(name = "series", type = SeriesIdentifier.class, namespace=PLAY_SIMPLE_XML.NS),
         @XmlElement(name = "brand", type = BrandIdentifier.class, namespace=PLAY_SIMPLE_XML.NS) 
     })
     public Set<ContentIdentifier> getRecentContent() {
         return recentContent;
     }
 }
