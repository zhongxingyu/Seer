 package se.atrosys.birds.model;
 
 import se.atrosys.birds.flickr.FlickrPhoto;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.Id;
 import javax.persistence.Table;
 
 /**
  * Created by IntelliJ IDEA.
  * User: ola
  * Date: 12/11/11
  * Time: 9:20 PM
  * To change this template use File | Settings | File Templates.
  */
 @Entity
 @Table(name = "BIRD_PHOTOS")
 public class BirdPhotoModel implements MediaModel, PhotoModel {
 	@Id
 	private String id;
 	@Column
 	private String owner;
 	@Column
 	private String secret;
 	@Column
 	private String server;
 	@Column
 	private String farm;
 	@Column
 	private String title;
	@Column
 	private boolean eligible;
//	@ManyToOne
//	private BirdModel bird;
 
 	public BirdPhotoModel() { }
 
 	public BirdPhotoModel(FlickrPhoto photo) {
 		this();
 		setFarm(photo.getFarm());
 		setId(photo.getId());
 		setOwner(photo.getOwner());
 		setSecret(photo.getSecret());
 		setServer(photo.getServer());
 		setTitle(photo.getTitle());
 	}
 
 	public String getId() {
 		return id;
 	}
 
 	public void setId(String id) {
 		this.id = id;
 	}
 
 	public String getOwner() {
 		return owner;
 	}
 
 	public void setOwner(String owner) {
 		this.owner = owner;
 	}
 
 	public String getSecret() {
 		return secret;
 	}
 
 	public void setSecret(String secret) {
 		this.secret = secret;
 	}
 
 	public String getServer() {
 		return server;
 	}
 
 	public void setServer(String server) {
 		this.server = server;
 	}
 
 	public String getFarm() {
 		return farm;
 	}
 
 	public void setFarm(String farm) {
 		this.farm = farm;
 	}
 
 	public String getTitle() {
 		return title;
 	}
 
 	public void setTitle(String title) {
 		this.title = title;
 	}
 
 	public String getUrl() {
 		return String.format("http://farm%s.staticflickr.com/%s/%s_%s.jpg", farm, server, id, secret);
 		/*
 								or
 							http://farm{farm-id}.staticflickr.com/{server-id}/{id}_{secret}_[mstzb].jpg
 								or
 							http://farm{farm-id}.staticflickr.com/{server-id}/{id}_{o-secret}_o.(jpg|gif|png)
 							 */
 	}
 
 	public void setBirdModel(BirdModel birdModel) {
 //		this.bird = birdModel;
 	}
 
 	@Override
 	public boolean equals(Object o) {
 		if (this == o) return true;
 		if (o == null || getClass() != o.getClass()) return false;
 
 		BirdPhotoModel that = (BirdPhotoModel) o;
 
 //		if (key != null ? !key.equals(that.key) : that.key != null) return false;
 		if (farm != null ? !farm.equals(that.farm) : that.farm != null) return false;
 		if (owner != null ? !owner.equals(that.owner) : that.owner != null) return false;
 		if (secret != null ? !secret.equals(that.secret) : that.secret != null) return false;
 		if (server != null ? !server.equals(that.server) : that.server != null) return false;
 		if (title != null ? !title.equals(that.title) : that.title != null) return false;
 
 		return true;
 	}
 
 	@Override
 	public int hashCode() {
 //		int result = key != null ? key.hashCode() : 0;
 		int result = id != null ? id.hashCode() : 0;
 		result = 31 * result + (owner != null ? owner.hashCode() : 0);
 		result = 31 * result + (secret != null ? secret.hashCode() : 0);
 		result = 31 * result + (server != null ? server.hashCode() : 0);
 		result = 31 * result + (farm != null ? farm.hashCode() : 0);
 		result = 31 * result + (title != null ? title.hashCode() : 0);
 
 		return result;
 	}
 	
 /*	@Embeddable
 	public static class CompositeKey implements Serializable{
 		private String id;
 		private String bird_id;
 
 		public String getId() {
 			return id;
 		}
 
 		public void setId(String id) {
 			this.id = id;
 		}
 
 		public String getBird_id() {
 			return bird_id;
 		}
 
 		public void setBird_id(String bird_id) {
 			this.bird_id = bird_id;
 		}
 
 		@Override
 		public boolean equals(Object o) {
 			if (this == o) return true;
 			if (o == null || getClass() != o.getClass()) return false;
 
 			CompositeKey that = (CompositeKey) o;
 
 			if (bird_id != null ? !bird_id.equals(that.bird_id) : that.bird_id != null) return false;
 			if (id != null ? !id.equals(that.id) : that.id != null) return false;
 
 			return true;
 		}
 
 		@Override
 		public int hashCode() {
 			int result = id != null ? id.hashCode() : 0;
 			result = 31 * result + (bird_id != null ? bird_id.hashCode() : 0);
 			return result;
 		}
 	}*/
 
	@Override
 	public boolean isSound() {
 		return false;  //To change body of implemented methods use File | Settings | File Templates.
 	}
 
	@Override
 	public boolean isPhoto() {
 		return true;  //To change body of implemented methods use File | Settings | File Templates.
 	}
 
	@Override
 	public boolean isEligible() {
 		return eligible;
 	}
 }
