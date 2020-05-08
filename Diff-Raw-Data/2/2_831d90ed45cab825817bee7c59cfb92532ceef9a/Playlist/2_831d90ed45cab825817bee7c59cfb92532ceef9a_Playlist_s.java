 package org.atlasapi.media.entity.simple;
 
 import java.util.List;
 
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlElementWrapper;
 import javax.xml.bind.annotation.XmlElements;
 import javax.xml.bind.annotation.XmlRootElement;
 import javax.xml.bind.annotation.XmlType;
 
 import org.atlasapi.media.vocabulary.PLAY_SIMPLE_XML;
 
 import com.google.common.base.Function;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Lists;
 
 @XmlRootElement(namespace=PLAY_SIMPLE_XML.NS)
 @XmlType(name="list", namespace=PLAY_SIMPLE_XML.NS)
 public class Playlist extends Description {
 
 	private List<Description> content = Lists.newArrayList();
 
 	public void add(Description c) {
 		content.add(c);
 	}
 	
 	@XmlElementWrapper(namespace=PLAY_SIMPLE_XML.NS, name="content")
 	@XmlElements({ 
 		@XmlElement(name = "item", type = Item.class, namespace=PLAY_SIMPLE_XML.NS),
 		@XmlElement(name = "playlist", type = Playlist.class, namespace=PLAY_SIMPLE_XML.NS) 
 	})
 	public List<Description> getContent() {
 		return content;
 	}
 	
	public void setContent(Iterable<Description> items) {
 		this.content = Lists.newArrayList(items);
 	}
 	
 	public Playlist copy() {
 	    Playlist copy = new Playlist();
 	    copyTo(copy);
 	    copy.setContent(Iterables.transform(getContent(), new Function<Description, Description>() {
 			@Override
 			public Description apply(Description input) {
 				return input.copy();
 			}
 	    }));
 	    return copy;
 	}
 }
