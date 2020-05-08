 /* Copyright 2009 British Broadcasting Corporation
    Copyright 2009 Meta Broadcast Ltd
 
 Licensed under the Apache License, Version 2.0 (the "License"); you
 may not use this file except in compliance with the License. You may
 obtain a copy of the License at
 
 http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 implied. See the License for the specific language governing
 permissions and limitations under the License. */
 
 package org.atlasapi.media.entity;
 
 import java.util.List;
 import java.util.Set;
 
 import org.atlasapi.content.rdf.annotations.RdfClass;
 import org.atlasapi.content.rdf.annotations.RdfProperty;
 import org.atlasapi.media.TransportType;
 import org.atlasapi.media.vocabulary.DC;
 import org.atlasapi.media.vocabulary.PO;
 
 import com.google.common.base.Function;
 import com.google.common.base.Predicate;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Sets;
 
 /**
  * @author Robert Chatley (robert@metabroadcast.com)
  * @author Lee Denison (lee@metabroadcast.com)
  * @author John Ayres (john@metabroadcast.com)
  */
 @RdfClass(namespace = PO.NS)
 public class Item extends Content {
 	
 	private Container<?> container;
 	
 	private Set<Version> versions = Sets.newHashSet();
 	private Set<CrewMember> people = Sets.newHashSet();
 	
 	private boolean isLongForm = false;
 	
 	public Item(String uri, String curie, Publisher publisher) {
 		super(uri, curie, publisher);
 	}
 	
 	public Item() { }
 	
 	public void setContainer(Container<?> container) {
         this.container = container;
     }
     
     public Container<?> getContainer() {
 		if (container == null) {
 			return null;
 		}
         return this.container.toSummary();
     }
     
     public Container<?> getFullContainer() {
 		return container;
     }
 	
 	@RdfProperty(namespace = DC.NS)
 	public boolean getIsLongForm() {
 		return isLongForm;
 	}
 
 	public void setIsLongForm(boolean isLongForm) {
 		this.isLongForm = isLongForm;
 	}
 
 	public void addVersion(Version version) {
 		if (version.getProvider() == null) {
 			version.setProvider(publisher);
 		}
 		versions.add(version);
 	}
 
 	@RdfProperty(relation = true, uri="version")
 	public Set<Version> getVersions() {
 		return versions;
 	}
 	
 	public Set<Version> nativeVersions() {
 		return Sets.filter(versions, new Predicate<Version>() {
 			@Override
 			public boolean apply(Version v) {
 				return publisher.equals(v.getProvider());
 			}
 		});
 	}
 	
 	public void setVersions(Set<Version> versions) {
 		this.versions = Sets.newHashSet();
 		addVersions(versions);
 	}
 
 	public void addVersions(Set<Version> versions) {
 		for (Version version : versions) {
 			addVersion(version);
 		}
 	}
 
 	public boolean removeVersion(Version version) {
 		return versions.remove(version);
 	}
 	
 	public Set<CrewMember> people() {
 	    return people;
 	}
 	
 	public Set<Actor> actors() {
 	    return Sets.<Actor>newHashSet(Iterables.filter(people, Actor.class));
 	}
 	
 	public void addPerson(CrewMember person) {
 	    people.add(person);
 	}
 	
 	public void setPeople(Set<CrewMember> people) {
 	    this.people = people;
 	}
 
 	public boolean isAvailable() {
 		for (Location location : locations()) {
 			if (location.getAvailable()) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public boolean isEmbeddable() {
 		for (Location location : locations()) {
 			if (location.getTransportType() != null && TransportType.EMBED.equals(location.getTransportType())) {
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	private List<Location> locations() {
 		List<Location> locations = Lists.newArrayList();
 		for (Version version : getVersions()) {
 			for (Encoding encoding : version.getManifestedAs()) {
 				for (Location location : encoding.getAvailableAt()) {
 					locations.add(location);
 				}
 			}
 		}
 		
 		return locations;
 	}
 	
 	@Override
 	public Content copy() {
 	    Item copy = new Item();
 	    
 	    Item.copyTo(this, copy);
 	    
 	    return copy;
 	}
 	
 	public static void copyTo(Item from, Item to) {
 	    Content.copyTo(from, to);
 	    if (from.container != null) {
 	        to.container = from.container.toSummary();
 	    }
 	    to.isLongForm = from.isLongForm;
 	    to.people = Sets.newHashSet(Iterables.transform(from.people, CrewMember.COPY));
 	    to.versions = Sets.newHashSet(Iterables.transform(from.versions, Version.COPY));
 	}
 	
 	public static final Function<Item, Item> COPY = new Function<Item, Item>() {
         @Override
         public Item apply(Item input) {
             return (Item) input.copy();
         }
     };
 }
