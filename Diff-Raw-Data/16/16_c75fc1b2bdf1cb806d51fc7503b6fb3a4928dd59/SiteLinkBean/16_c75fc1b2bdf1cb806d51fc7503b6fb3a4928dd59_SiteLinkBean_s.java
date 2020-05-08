 package com.opensajux.view;
 
 import java.io.Serializable;
 import java.util.Collection;
 import java.util.List;
 import java.util.Map;
 
 import javax.enterprise.context.RequestScoped;
 import javax.inject.Inject;
 import javax.inject.Named;
 
 import org.primefaces.model.LazyDataModel;
 import org.primefaces.model.SortOrder;
 
 import com.opensajux.common.PaginationParameters;
 import com.opensajux.entity.SiteLink;
 import com.opensajux.service.SiteLinkService;
 
 @RequestScoped
 @Named
 public class SiteLinkBean implements Serializable {
 	private static final long serialVersionUID = 6547830425414566882L;
 
 	@Inject
 	private SiteLinkService siteLinkService;
 
 	private LazyDataModel<SiteLink> siteLinkModel;
 	private SiteLink[] selectedSiteLinks;
 	private SiteLink[] filteredSiteLinks;
 	private String siteTitle = "";
 	private String siteUrl = "";
 
 	public LazyDataModel<SiteLink> getSiteLinks() {
 		if (siteLinkModel == null) {
 			siteLinkModel = new LazyDataModel<SiteLink>() {
 				private static final long serialVersionUID = 5823556446396883931L;
 
 				@Override
 				public int getRowCount() {
 					return siteLinkService.getCount().intValue();
 				}
 
 				@Override
 				public List<SiteLink> load(int first, int pageSize, String sortField, SortOrder sortOrder,
 						Map<String, String> filters) {
 					PaginationParameters param = new PaginationParameters();
 					param.setFirst(first);
 					param.setPageSize(pageSize);
 					param.setSortField(sortField);
 					param.setSortOrder(sortOrder == null ? "" : sortOrder == SortOrder.ASCENDING ? "asc" : "desc");
 
 					return siteLinkService.getLinks(param);
 				}
 
 				@Override
 				public Object getRowKey(SiteLink siteLink) {
 					return siteLink.getKey().getId() + "";
 				}
 
 				@Override
 				public SiteLink getRowData(String rowKey) {
 					return siteLinkService.getById(rowKey);
 				}
 
 			};
 			siteLinkModel.setPageSize(5);
 		}
 		return siteLinkModel;
 	}
 
 	public Collection<SiteLink> getLinks() {
 		return siteLinkService.getLinks(null);
 	}
 
 	/**
 	 * @return the selectedSiteLinks
 	 */
 	public SiteLink[] getSelectedSiteLinks() {
 		return selectedSiteLinks;
 	}
 
 	/**
 	 * @param selectedSiteLinks
 	 *            the selectedSiteLinks to set
 	 */
 	public void setSelectedSiteLinks(SiteLink[] selectedSiteLinks) {
 		this.selectedSiteLinks = selectedSiteLinks;
 	}
 
 	/**
 	 * @return the filteredSiteLinks
 	 */
 	public SiteLink[] getFilteredSiteLinks() {
 		return filteredSiteLinks;
 	}
 
 	/**
 	 * @param filteredSiteLinks
 	 *            the filteredSiteLinks to set
 	 */
 	public void setFilteredSiteLinks(SiteLink[] filteredSiteLinks) {
 		this.filteredSiteLinks = filteredSiteLinks;
 	}
 
 	/**
 	 * @return the siteTitle
 	 */
 	public String getSiteTitle() {
 		return siteTitle;
 	}
 
 	/**
 	 * @param siteTitle
 	 *            the siteTitle to set
 	 */
 	public void setSiteTitle(String siteTitle) {
 		this.siteTitle = siteTitle;
 	}
 
 	/**
 	 * @return the siteUrl
 	 */
 	public String getSiteUrl() {
 		return siteUrl;
 	}
 
 	/**
 	 * @param siteUrl
 	 *            the siteUrl to set
 	 */
 	public void setSiteUrl(String siteUrl) {
 		this.siteUrl = siteUrl;
 	}
 
 	public void saveSiteLink() {
 		siteLinkService.saveSiteLink(siteTitle, siteUrl);
 	}
 
 	public void removeSiteLinks() {
 		for (SiteLink b : selectedSiteLinks) {
			siteLinkService.removeSiteLink	(b.getUrl().getValue());
 		}
 	}
 }
