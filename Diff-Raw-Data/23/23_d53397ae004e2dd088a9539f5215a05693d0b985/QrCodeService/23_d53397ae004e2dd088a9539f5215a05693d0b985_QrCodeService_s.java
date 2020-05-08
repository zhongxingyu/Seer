 package org.offlike.server.service;
 
 import java.io.UnsupportedEncodingException;
 import java.net.URL;
 import java.net.URLEncoder;
 
 import org.offlike.server.data.Campaign;
 import org.offlike.server.data.QrCode;
 
 public class QrCodeService {
 
 	private static final String URL_ENCODING = "UTF-8";
 	private static final String GOOGLE_QR_API = "http://chart.apis.google.com/chart?cht=qr&chs=350x350&chld=L&choe=UTF-8&chl=";
 	private static final String OFFLIKE_DOMAIN = "http://offlike.org/like/";
 	
 	private final MongoDbService mongoDbService;
 	
 	public QrCodeService(MongoDbService mongoDbService) {
 		this.mongoDbService = mongoDbService;
 	}
 	
	public URL generateQrCode(String campaignId) throws Exception {
 		QrCode qrCode = new QrCode();
 		Campaign campaign = mongoDbService.findCampaignById(campaignId);
 		mongoDbService.createQrCode(campaign, qrCode);
 		
		return new URL(GOOGLE_QR_API + parseUrl(qrCode.getId(), campaign.getTitle()));
 	}
 	
 	protected static String parseUrl(String qrCodeId, String campaignTitle) throws UnsupportedEncodingException {
 		String URLdomain = URLEncoder.encode(OFFLIKE_DOMAIN+qrCodeId,URL_ENCODING);
 		String URLcamapignTitle = URLEncoder.encode(campaignTitle, URL_ENCODING);
 		
 		return URLdomain + "?campaign_name=" + URLcamapignTitle;
 	}
 }
