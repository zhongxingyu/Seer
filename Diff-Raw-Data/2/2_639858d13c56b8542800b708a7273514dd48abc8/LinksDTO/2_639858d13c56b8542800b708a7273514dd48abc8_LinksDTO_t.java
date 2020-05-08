 package ro.iasi.communication.api;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 
 public class LinksDTO implements Serializable {
 
 	private static final long serialVersionUID = -5291716690774991189L;
 
 	private String urlRoot;
 	private List<String> urls = new ArrayList<>();
 
 	public LinksDTO(List<String> l) {
 		this.urls = l;
 	}
 
 	public LinksDTO() {

 	}
 
 	public String getUrlRoot() {
 		return urlRoot;
 	}
 
 	public void setUrlRoot(String urlRoot) {
 		this.urlRoot = urlRoot;
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + ((urlRoot == null) ? 0 : urlRoot.hashCode());
 		result = prime * result + ((urls == null) ? 0 : urls.hashCode());
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
 		LinksDTO other = (LinksDTO) obj;
 		if (urlRoot == null) {
 			if (other.urlRoot != null)
 				return false;
 		} else if (!urlRoot.equals(other.urlRoot))
 			return false;
 		if (urls == null) {
 			if (other.urls != null)
 				return false;
 		} else if (!urls.equals(other.urls))
 			return false;
 		return true;
 	}
 
 	@Override
 	public String toString() {
 		return "LinksDTO [urlRoot=" + urlRoot + ", urls=" + urls + "]";
 	}
 
 	public List<String> getUrls() {
 		return urls;
 	}
 
 	public void setUrls(List<String> urls) {
 		this.urls = urls;
 	}
 
 }
