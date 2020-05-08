 package com.globant.gaetraining.addsincgae.services;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 
 import com.globant.gaetraining.addsincgae.daos.CampaignDao;
 import com.globant.gaetraining.addsincgae.daos.CampaignSummaryDao;
 import com.globant.gaetraining.addsincgae.daos.ProductDao;
 import com.globant.gaetraining.addsincgae.daos.ProductSummaryDao;
 import com.globant.gaetraining.addsincgae.model.Campaign;
 import com.globant.gaetraining.addsincgae.model.CampaignSummary;
 import com.globant.gaetraining.addsincgae.model.Product;
 import com.globant.gaetraining.addsincgae.model.ProductSummary;
 import com.google.appengine.api.datastore.Key;
 import com.google.appengine.api.datastore.KeyFactory;
 
 @Service
 public class CampaignService {
 
 	@Autowired
 	private CampaignDao campaignDao;
 
 	@Autowired
 	private CampaignSummaryDao campaignSummaryDao;
 
 	@Autowired
 	private ProductDao productDao;
 
 	@Autowired
 	private ProductSummaryDao productSummaryDao;
 
 	/**
 	 * Find the active {@link Campaign}s for a specific customer
 	 * 
 	 * @param customerKey
 	 *            {@link Key} of the customer
 	 * @return {@link Campaign} found or null
 	 */
 	public List<Campaign> findActiveCampaignsByCustomerKey(Key customerKey) {
 		return campaignDao.findActiveByCustomerKey(customerKey);
 	}
 
 	/**
 	 * Find the {@link Campaign} associadte with the campaignId. This id is the
 	 * equivalent to Key.id
 	 * 
 	 * @param campaignId
 	 *            Id of the campaign
 	 * @return {@link Campaign} with his {@link CampaignSummary} found or null
 	 *         if one of both is null. The first position of the list is the
 	 *         {@link Campaign}, the second is the {@link CampaignSummary}, the
 	 *         third is the {@link Map} of {@link ProductsSummaryCountryDTO}
 	 */
 	public List<Object> findCampaignWithStatisticsById(Object campaignId) {
 
 		// Find entities
 		Campaign campaign = this.campaignDao.findByIdWithProducts(campaignId);
 		CampaignSummary campaignSummary = this.campaignSummaryDao
 				.findByCampaignKeyWithProductsAndDistrChannelsSummaries(campaign
 						.getKey());
 		List<String> countries = this.campaignDao
				.findCountriesByCampaignKey(campaign.getKey());
 
 		// Creation of countries map
 		Map<String, List<ProductsSummaryCountryDTO>> productCountryMap = new HashMap<String, List<ProductsSummaryCountryDTO>>();
 		for (String countryName : countries) {
 			productCountryMap.put(countryName,
 					new ArrayList<ProductsSummaryCountryDTO>());
 		}
 
 		Map<Key, ProductSummary> map = this.productSummaryDao
 				.convertToMapWithProductKey(campaignSummary.getProductSummary());
 
 		// Creation of countryProductSumamryDTO
 		ProductSummary productSummary;
 		ProductsSummaryCountryDTO dto;
 		for (Product product : campaign.getProduct()) {
 
 			productSummary = map.get(product.getKey());
 
 			if (productSummary != null) {
 
 				dto = new ProductsSummaryCountryDTO(product.getCountry(),
 						product.getName(), productSummary.getTotalHits(),
 						productSummary.getTotalViews());
 			} else {
 				dto = new ProductsSummaryCountryDTO(product.getCountry(),
 						product.getName(), 0, 0);
 			}
 
 			List<ProductsSummaryCountryDTO> list = productCountryMap
 					.get(product.getCountry());
 			list.add(dto);
 
 			productCountryMap.put(product.getCountry(), list);
 		}
 
 		List<Object> results = new ArrayList<>();
 
 		if (campaign == null || campaignSummary == null) {
 			return null;
 		}
 
 		results.add(campaign);
 		results.add(campaignSummary);
 		results.add(productCountryMap);
 
 		return results;
 	}
 
 	public List<Campaign> getCampaigns() {
 		return campaignDao.findAll(Campaign.class);
 	}
 
 	public void addCampaign(Campaign campaign) {
 		campaignDao.persist(campaign);
 	}
 
 	public Campaign getCampaign(Long campaignId) {
 		Key key = KeyFactory.createKey(Campaign.class.getSimpleName(),
 				campaignId);
 		return campaignDao.findByKey(key, Campaign.class, null);
 	}
 
 	public Campaign getCampaignByKey(String keyString){
 		Key key = KeyFactory.stringToKey(keyString);
 		return campaignDao.findByKey(key, Campaign.class, null);
 	}
 }
