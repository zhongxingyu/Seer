 package org.offlike.server;
 
 import org.offlike.server.data.Campaign;
 import org.offlike.server.data.QrCode;
 import org.offlike.server.service.MongoDbService;
 import org.offlike.server.service.UrlBuilder;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.servlet.ModelAndView;
 
 import com.google.common.collect.ImmutableMap;
 
 @Controller
 public class LikeController {
 
 	// TODO count the number of requests,
 	private MongoDbService dbService;
 
 	@RequestMapping("/like/{id}")
 	public ModelAndView like(
 			@RequestParam(value = "campaign_name", required = false) String campaignName,
 			@RequestParam(value = "lat", required = false) Double lat,
			@RequestParam(value = "lng", required = false) Double lng,
 			@RequestParam(value = "accuracy", required = false) Integer accuracy,
 			@PathVariable("id") String id) {
 
 		
 		if (lat != null && lng != null && accuracy != null) {
 			getDbService().activateQrCode(id, lat, lng, accuracy);
 		}
 	
 		QrCode qrCode = dbService.findQrCodeById(id);
 		if (qrCode == null) {
 			return errorPage("Unknown qr code!");
 		}
 		Campaign campaign = dbService.findCampaignById(qrCode.getCampaignId());
 		if (campaign==null){
 			return errorPage("Unknown campaign id!");
 		}
 		
 		return new ModelAndView("like", ImmutableMap.<String, Object> of(
 				"campaign", campaign,
 				"url", UrlBuilder.createLikeURL(id)));
 	}
 
 	private ModelAndView errorPage(String error) {
 		return new ModelAndView("errorPage", ImmutableMap.of("error", error));
 	}
 	public MongoDbService getDbService() {
 		return dbService;
 	}
 
 	public void setDbService(MongoDbService dbService) {
 		this.dbService = dbService;
 	}
 
 }
