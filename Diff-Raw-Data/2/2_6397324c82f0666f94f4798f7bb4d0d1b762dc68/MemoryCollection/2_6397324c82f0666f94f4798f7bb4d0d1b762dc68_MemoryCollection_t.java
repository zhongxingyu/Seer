 package eu.europeana.uim.store.memory;
 
 import eu.europeana.uim.store.Collection;
 
 public class MemoryCollection extends AbstractMemoryEntity implements Collection {
 
 	private MemoryProvider provider;
 
 	private String oaiBaseUrl;
 	private String oaiPrefix;
 	private String oaiSet;
 	
 	public MemoryCollection(MemoryProvider provider) {
 		super();
 		this.provider = provider;
 	}
 
 	public MemoryCollection(long id, MemoryProvider provider) {
 		super(id);
 		this.provider = provider;
 	}
 
 
 	public MemoryProvider getProvider() {
 		return provider;
 	}
 
 	/**
 	 * @return the oaiBaseUrl
 	 */
 	public String getOaiBaseUrl() {
 		if (oaiBaseUrl != null) {
 			return oaiBaseUrl;
 		}
 		return provider.getOaiBaseUrl();
 	}
 
 	/**
 	 * @param oaiBaseUrl the oaiBaseUrl to set
 	 */
 	public void setOaiBaseUrl(String oaiBaseUrl) {
 		this.oaiBaseUrl = oaiBaseUrl;
 	}
 
 	/**
 	 * @return the oaiSet
 	 */
 	public String getOaiSet() {
 		return oaiSet;
 	}
 
 	/**
 	 * @param oaiSet the oaiSet to set
 	 */
 	public void setOaiSet(String oaiSet) {
 		this.oaiSet = oaiSet;
 	}
 
 	/**
 	 * @return the oaiPrefix
 	 */
 	public String getOaiPrefix() {
		if (oaiPrefix != null) {
 			return oaiPrefix;
 		}
 		return provider.getOaiPrefix();
 	}
 
 	/**
 	 * @param oaiPrefix the oaiPrefix to set
 	 */
 	public void setOaiPrefix(String oaiPrefix) {
 		this.oaiPrefix = oaiPrefix;
 	}
 	
 
 
 	
 	
 	/* (non-Javadoc)
 	 * @see eu.europeana.uim.store.memory.AbstractMemoryEntity#toString()
 	 */
 	@Override
 	public String toString() {
 		String string = super.toString();
 		string += " [";
 		string += getOaiBaseUrl() != null ? getOaiBaseUrl() : (getProvider().getOaiBaseUrl() != null ? getProvider().getOaiBaseUrl() : "undefined");
 
 		string += "?metadataPrefix=";
 		string += getOaiPrefix() != null ? getOaiPrefix() : getProvider().getOaiPrefix();
 		return string + "]";
 	}
 
 
 
 
 
 }
