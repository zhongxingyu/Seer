 package com.pingidentity.efazendin.pingpong.sp.model;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.log4j.Logger;
 
 
 /**
  * This class pages through a list of Identity Providers.
  * 
  * @author efazendin
  *
  */
 public class IdentityProviderPager implements Serializable {
 	
 	private static final Logger _logger = Logger.getLogger(IdentityProviderPager.class);
 	
 	public static final String IDP_PAGER = "idp.pager";
 	
 	private int pageSize;
 	private int currentPage = 0;
 	private long pageExpirationPeriod = 5000L;
 	private Date pageRequestedAt;
 	private List<IdentityProvider> prioritizedIdps;
 	private List<IdentityProvider> usersIdps = new ArrayList<IdentityProvider>();
 	
 
 	public IdentityProviderPager(List<IdentityProvider> prioritizedIdps, int pageSize, long pageExpirationPeriod) {
 		this.prioritizedIdps = prioritizedIdps;
 		this.pageSize = pageSize;
 		this.pageExpirationPeriod = pageExpirationPeriod;
 	}
 
 	public List<IdentityProvider> getNextPage() {
 		pageRequestedAt = new Date();
 		
 		List<IdentityProvider> idps = getPageOfIdPs(currentPage);
 		
 		currentPage++;
 		
 		_logger.debug("This page's idp count: " + idps.size());
 		
 		return idps;
 	}
 	
 	public void updateHasAuthenticatedUser(String entityId, boolean hasAuthenticatedUser) {
 
 		IdentityProvider idp = getIdentityProvider(entityId);
 		if (idp != null) {
 			idp.setHasAuthenticatedUser(hasAuthenticatedUser);
 			
 			if (hasAuthenticatedUser)
 				usersIdps.add(idp);
 		}
 		
 	}
 	
 	public boolean haveAllPagedPonged() {
 		boolean result = true;
 		
 		for (IdentityProvider idp : getPageOfIdPs(currentPage - 1)) {
 			if (!idp.getHasPonged()) {
 				result = false;
 				break;
 			}
 		}
 		
 		return result;
 	}
 	
 	public boolean haveAnyAuthnedUser() {
 		return usersIdps.size() > 0;
 	}
 
 	public boolean hasNextPage() {
 		return (currentPage * pageSize) < prioritizedIdps.size();
 	}
 
 	public boolean hasPageExpired() {
 		Date now = new Date();
 		return (now.getTime() - pageRequestedAt.getTime()) > pageExpirationPeriod;
 	}
 	
 	public boolean contiansIdp(String entityId) {
 		boolean result = false;
 		
 		IdentityProvider idp = getIdentityProvider(entityId);
 		if (idp != null)
 			result = true;
 		
 		return result;
 	}
 	
 	public String getFileName(String entityId) {
 		String result = "";
 		
 		IdentityProvider idp = getIdentityProvider(entityId);
 		if (idp != null)
 			result = idp.getFileName();
 		
 		return result;
 	}
 	
 	public IdentityProvider getIdentityProvider(String entityId) {
 		IdentityProvider result = null;
 		for (IdentityProvider idp : prioritizedIdps) {
 			if (idp.getEntityId().equals(entityId)) {
 				result = idp;
 				break;
 			}
 		}
 		return result;
 	}
 	
 		
 	public int getPageSize() {
 		return pageSize;
 	}
 	public void setPageSize(int pageSize) {
 		this.pageSize = pageSize;
 	}
 	
 	public int getCurrentPage() {
 		return currentPage;
 	}
 	public void setCurrentPage(int currentPage) {
 		this.currentPage = currentPage;
 	}
 	
 	public long getPageExpirationPeriod() {
 		return pageExpirationPeriod;
 	}
 	public void setPageExpirationPeriod(long pageExpirationPeriod) {
 		this.pageExpirationPeriod = pageExpirationPeriod;
 	}
 	
 	public void setPrioritizedIdentityProviders(List<IdentityProvider> prioritizedIdentityProviders) {
 		this.prioritizedIdps = prioritizedIdentityProviders;
 	}
 	public List<IdentityProvider> getPrioritizedIdentityProviders() {
 		return prioritizedIdps;
 	}
 
 	public Date getPageRequestedAt() {
 		return pageRequestedAt;
 	}
 
 	public List<IdentityProvider> getUsersIdps() {
 		return usersIdps;
 	}
 
 	public Set<IdentityProvider> getAllIdentityProviders() {
 		return new HashSet<IdentityProvider>(prioritizedIdps);
 	}
 	
 	
 	private List<IdentityProvider> getPageOfIdPs(int pageNumber) {
 		List<IdentityProvider> thePage = new ArrayList<IdentityProvider>();
 		
 		int potentialFullPageIndex = ((pageNumber * pageSize) + pageSize) - 1;
 		int firstIndexOfPage = (pageNumber * pageSize);
		int lastIndexOfPage = Math.min(potentialFullPageIndex, prioritizedIdps.size());
		/*
		int lastIndexOfPage = potentialFullPageIndex > prioritizedIdps.size() - 1
								? prioritizedIdps.size() - 1 : potentialFullPageIndex;
								*/
 
		//for (int i = firstIndexOfPage; i <= lastIndexOfPage; i++)
		for (int i = firstIndexOfPage; i < lastIndexOfPage; i++)
 			thePage.add(prioritizedIdps.get(i));
 
 		return thePage;
 	}
 
 }
