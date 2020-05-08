 package net.thumbtack.updateNotifierBackend.database.entities;
 
 import java.io.Serializable;
 import java.util.List;
 
 public class Resource implements Serializable {
 	
 	private static final long serialVersionUID = 1L;
 	
 	private Long id;
 	private transient Long userId;
 
 	private String url;
 	private byte sheduleCode;
 	private transient int hash;
 	private List<Long> tagIds;
 	
 	public Resource() {
 	}
 
 	public String getUrl() {
 		return url;
 	}
 
 	public void setUrl(String url) {
 		this.url = url;
 	}
 
 	public byte getSheduleCode() {
 		return sheduleCode;
 	}
 
 	public Long getId() {
 		return id;
 	}
 
 	public Long getUserId() {
 		return userId;
 	}
 
 	public void setUserId(Long userId) {
 		this.userId = userId;
 	}
 
 	public List<Long> getTagIds() {
 		return tagIds;
 	}
 
 
 	public void setTagIds(List<Long> list) {
 		this.tagIds = list;
 	}
 
 	public int getHash() {
 		return hash;
 	}
 
 	public void setHash(int hash) {
 		this.hash = hash;
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + ((id == null) ? 0 : id.hashCode());
 		result = prime * result + ((url == null) ? 0 : url.hashCode());
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		Resource other = (Resource) obj;
 		if (id == null) {
 			if (other.id != null)
 				return false;
 		} else if (!id.equals(other.id))
 			return false;
 		if (url == null) {
 			if (other.url != null)
 				return false;
 		} else if (!url.equals(other.url))
 			return false;
 		return true;
 	}
 
 	public String getDomPath() {
		return "/0/1/1/1/0/2/0/1/0";
 	}
 	
 	public String getFilter() {
		return null;
 	}
 
 }
