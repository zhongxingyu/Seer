 /**
 * My Net is free software, you can redistribute it and/or modify
 * it under the terms of GNU Affero General Public License
 * as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * You should have received a copy of the the GNU Affero
 * General Public License, along with My Net. If not, see
 *
 * http://www.gnu.org/licenses/agpl.txt
 */
 
 package com.perevillega.mynet.model;
 
 import java.io.IOException;
 import java.io.Serializable;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import javax.persistence.Basic;
 import javax.persistence.CascadeType;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.JoinTable;
 import javax.persistence.Lob;
 import javax.persistence.ManyToMany;
 import javax.persistence.ManyToOne;
 import javax.persistence.NamedQueries;
 import javax.persistence.NamedQuery;
 import javax.persistence.OneToMany;
 import javax.persistence.Temporal;
 import javax.persistence.TemporalType;
 import javax.persistence.Transient;
 import javax.persistence.Version;
 
 import org.hibernate.annotations.AccessType;
 import org.hibernate.annotations.Cache;
 import org.hibernate.annotations.CacheConcurrencyStrategy;
 import org.hibernate.validator.Length;
 import org.hibernate.validator.NotNull;
 import org.hibernate.validator.Pattern;
 
 import com.perevillega.mynet.action.validators.URI;
 
 @Entity
 @NamedQueries({	
 	@NamedQuery(
 		    name="findAllLinks",
 		    query="SELECT OBJECT(link) FROM Link link"
 		    	),	
 	})
 public class Link implements Serializable {
 
 	private Long id;
 	private Integer version;
 	private String name;
 	private Date date;
 	private String description;
 	private boolean hidden;
 	private String url;
 	private List<Tag> tags = new ArrayList<Tag>(0);
 	private User creator;
 	private String taglist;
 	private Category category;
 	private boolean validated;
 	private Date dateValidation;
 	private boolean selected;
 	private List<User> favorited = new ArrayList<User>(0);
 	private List<User> like = new ArrayList<User>(0);
 	private List<User> dislike = new ArrayList<User>(0);
 	private int valoration;
 	
 	
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + ((id == null) ? 0 : id.hashCode());
 		result = prime * result + ((version == null) ? 0 : version.hashCode());
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (!(obj instanceof Link))
 			return false;
 		Link other = (Link) obj;
 		if (id == null) {
 			if (other.id != null)
 				return false;
 		} else if (!id.equals(other.id))
 			return false;
 		if (version == null) {
 			if (other.version != null)
 				return false;
 		} else if (!version.equals(other.version))
 			return false;
 		return true;
 	}
 
 	@Id
 	@GeneratedValue
 	public Long getId() {
 		return id;
 	}
 
 	public void setId(Long id) {
 		this.id = id;
 	}
 
 	@Version
 	public Integer getVersion() {
 		return version;
 	}
 
 	private void setVersion(Integer version) {
 		this.version = version;
 	}
 
 	@Length(max = 20)
 	@NotNull
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	@Temporal(TemporalType.DATE)
 	public Date getDate() {
 		return date;
 	}
 
 	public void setDate(Date date) {
 		this.date = date;
 	}
 
 	@Lob
 	public String getDescription() {
 		return description;
 	}
 
 	public void setDescription(String description) {
 		this.description = description;
 	}
 
 	public String shortDescription() {
 		String tmp = "";
 		if (this.description != null) {
 			int endIndex = Math.min(30, this.description.length());
 			tmp = this.description.substring(0, endIndex);
 			if (endIndex >= 30) {
 				tmp += "[...]";
 			}
 		}
 		return tmp;
 	}
 
 	@Basic
 	@NotNull
 	public boolean isHidden() {
 		return hidden;
 	}
 
 	public void setHidden(boolean hidden) {
 		this.hidden = hidden;
 	}
 
 	@URI
 	@NotNull
 	public String getUrl() {
 		return url;
 	}
 
 	public void setUrl(String url) {
 		this.url = normalizeURL(url);		
 	}
 	
 	private String normalizeURL(String url) {
 		String result = url;
 		try {
 			java.net.URI uri = new java.net.URI(url);
 			uri = uri.normalize();
 			result = uri.toASCIIString();
 		} catch (URISyntaxException e) {
 			//we can't normalise teh URL, fail silently
 			e.printStackTrace();
 		}
 
 		return result;
 	}
 
 	@ManyToMany(fetch=FetchType.LAZY)
 	public List<Tag> getTags() {
 		return tags;
 	}
 
 	public void setTags(List<Tag> tags) {
 		this.tags = tags;
 	}
 	
 	public void removeTag(Tag tag) {
 		tags.remove(tag);
 	}
 	
 	public void addTag(Tag tag) {
 		tags.add(tag);
 	}
 	
 	public String formattedTags() {
 		StringBuffer bf = new StringBuffer();
 		for(Tag tag: tags) {
 			bf.append(tag.getName());
 			bf.append(", ");
 		}
 		if(bf.length() >= 2) bf.deleteCharAt(bf.length()-2);
 		return bf.toString().trim();
 	}
 
 	@Transient	
 	public String getTaglist() {
 		return taglist;
 	}
 
 	public void setTaglist(String taglist) {
 		this.taglist = taglist;
 	}
 
 	@ManyToOne
 	public Category getCategory() {
 		return category;
 	}
 
 	public void setCategory(Category category) {
 		this.category = category;
 	}
 
 	@NotNull
 	public boolean isValidated() {
 		return validated;
 	}
 
 	public void setValidated(boolean validated) {
 		this.validated = validated;
 	}
 	
 	//we can only validate http
 	public void validate(){		
 		if(url.startsWith("http")) {			
 			URL urltest;
 			try {
 				urltest = new URL(this.url);
 				HttpURLConnection conn = (HttpURLConnection) urltest.openConnection();
 				//according to W3C document, URL any content-length of 0 or greater is valid, unless a HTTP status code is sent and in a few other (rare?) cases.				
 				if(conn.getResponseCode() == HttpURLConnection.HTTP_ACCEPTED || conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
 					this.setValidated(true);
 					this.setDateValidation(new Date());
 				}				
 				conn.disconnect();
 			} catch (MalformedURLException e) {
 				//it should never come here, report on logs
 				e.printStackTrace();
 			} catch (IOException e) {
 				//we ignore connection errors, it means server is not available, so link not valid
 				e.printStackTrace();
 			}
 			
 		} else {
 			//by now, url that are not http are considered valid
 			this.setValidated(true);
 		}		
 	}
 		
 	
 	@Transient
 	public boolean isSelected() {
 		return selected;
 	}
 
 	public void setSelected(boolean selected) {
 		this.selected = selected;
 	}
 	
 	@Temporal(TemporalType.DATE)	
 	public Date getDateValidation() {
 		return dateValidation;
 	}
 
 	public void setDateValidation(Date dateValidation) {
 		this.dateValidation = dateValidation;
 	}
 
 	public void remove() {		
 		for (Tag t : tags) {
 			t.removeLink(this);
 		}
 		for(User u: favorited){
 			u.removeFavorite(this);
 		}
 		for(User u: like) {
 			u.removeLike(this);
 		}
 		for(User u: dislike) {
 			u.removeDislike(this);
 		}
 	}
 
 	@ManyToOne
 	public User getCreator() {
 		return creator;
 	}
 
 	public void setCreator(User creator) {
 		this.creator = creator;
 	}
 		
 	@ManyToMany(fetch=FetchType.LAZY)	
 	public List<User> getFavorited() {
 		return favorited;
 	}
 
 	public void setFavorited(List<User> favorites) {
 		this.favorited = favorites;
 	}
 	
 	public void removeFavorite(User user){
 		this.favorited.remove(user);	
 	}
 	
 	public void addFavorite(User user){
 		this.favorited.add(user);
 	}
 	
 	public boolean linkFavorited(User user) {		
 		return this.favorited.contains(user);
 	}
 
 	@ManyToMany(fetch=FetchType.LAZY)
 	@JoinTable(name="Links_Liked")
 	public List<User> getLike() {
 		return like;
 	}
 
 	public void setLike(List<User> like) {
 		this.like = like;
 	}
 	
 	public void removeLike(User user){
 		this.like.remove(user);	
 	}
 	
 	public void addLike(User user){
 		this.like.add(user);
 	}
 
 	@ManyToMany(fetch=FetchType.LAZY)
 	@JoinTable(name="Links_Disliked")
 	public List<User> getDislike() {
 		return dislike;
 	}
 
 	public void setDislike(List<User> dislike) {
 		this.dislike = dislike;
 	}
 	
 	public void removeDislike(User user){
 		this.dislike.remove(user);	
 	}
 	
 	public void addDislike(User user){
 		this.dislike.add(user);
 	}
 	
 	public int valoration() {				
 		return like.size() - dislike.size();
 	}
 }
