 package org.offlike.server;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.offlike.server.data.Campaign;
 import org.offlike.server.data.QrCode;
 import org.offlike.server.service.MongoDbService;
 import org.offlike.server.service.QrCodeService;
 import org.offlike.server.service.UrlBuilder;
 import org.owasp.validator.html.AntiSamy;
 import org.owasp.validator.html.Policy;
 import org.owasp.validator.html.PolicyException;
 import org.owasp.validator.html.ScanException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.servlet.ModelAndView;
 
 import com.google.common.base.Predicate;
 import com.google.common.base.Strings;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.ImmutableMap;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Maps;
 
 @Controller
 public class CampaignController {
 
 	public static final Integer MAX_QR_CODES = 10;
 	public static final String QR_CODE_PAGE = "qrcodes";
 	public static final String CAMPAIGN_FIELD = "campaign";
 	public static final String CAMPAIGN_PAGE = "campaign";
 	public static final String QR_CODE_LIST = "qrcodeList";
 
 	@Autowired
 	private QrCodeService qrCodeService;
 
 	public Policy getPolicy() {
 		return policy;
 	}
 
 	public void setPolicy(Policy policy) {
 		this.policy = policy;
 	}
 
 	public MongoDbService getDbService() {
 		return dbService;
 	}
 
 	public void setDbService(MongoDbService dbService) {
 		this.dbService = dbService;
 	}
 
 	public static Logger log = LoggerFactory
 			.getLogger(CampaignController.class);
 
 	@Autowired
 	private Policy policy;
 
 	@Autowired
 	private MongoDbService dbService;
 
 	/**
 	 * Shows a new campaign page
 	 * 
 	 * @return
 	 */
 	@RequestMapping(method = { RequestMethod.GET }, value = "/createCampaign")
 	public ModelAndView newCampaign() {
 		return new ModelAndView("createCampaign");
 	}
 
 	@RequestMapping(method = { RequestMethod.POST }, value = "/createCampaign")
 	public ModelAndView createCampaign(
 			@RequestParam("title") String title,
 			@RequestParam("description") String description,
 			@RequestParam(value = "externalLink", required = false) String externalLink) {
 
 		Map<String, String> errorMap = new HashMap<String, String>();
 
 		String cleanTitle = cleanUpString(title);
 		if (cleanTitle == null) {
 			errorMap.put("title", "Dirty input!");
 		}
 		String cleanDescription = cleanUpString(description);
 		if (cleanDescription == null) {
 			errorMap.put("description", "Dirty input!");
 		}
 		if (!Strings.isNullOrEmpty(externalLink) && !isUrlValid(externalLink)) {
 			errorMap.put("refererUrl", "Not valid");
 		}
 
 		if (errorMap.isEmpty()) {
 
 			Campaign campaign = new Campaign();
 			campaign.setDescription(cleanDescription);
 			campaign.setExternalLink(externalLink);
 			campaign.setTitle(cleanTitle);
 
 			dbService.createCampaign(campaign);
 			return new ModelAndView("redirect:campaign/"+campaign.getId());
 			//getCampaign(campaign.getId());
 		}
 
 		return new ModelAndView("createCampaign", ImmutableMap.of("errorMap",
 				errorMap, "title", title, "description", description, "externalLink", externalLink));
 	}
 
 	@RequestMapping("/campaign/{id}")
 	public ModelAndView getCampaign(@PathVariable String id) {
 		if (!isIdValid(id)) {
 			return errorPage("Id is not valid");
 		}
 
 		Campaign camp = dbService.findCampaignById(id);
 
 		if (camp == null) {
 			return errorPage("No campaign with that id!");
 		}
 		List<QrCode> codesForCampaign = dbService.findQrCodesForCampaign(id);
 		List<Map<String, Object>> presentationCodes = new ArrayList<Map<String, Object>>(codesForCampaign.size());
 		for (QrCode code : codesForCampaign){
 			Map<String, Object> presentationCode = Maps.newHashMap();
 			presentationCode.put("qrCodeImageLink", QrCodeService.createUrl(code, camp).toExternalForm());
 			presentationCode.put("likeUrl", UrlBuilder.createLikeURL(code.getId()));
 			presentationCodes.add(presentationCode);
 		}
 		
 		Iterable<QrCode> registeredCodes = ImmutableList.copyOf(Iterables.filter(codesForCampaign, new Predicate<QrCode>() {
 			@Override
 			public boolean apply(QrCode arg0) {
 				
 				boolean r= arg0.getLongitude() != null && arg0.getLatitude() != null;
 				System.out.println("****************************");
 				System.out.println("r: " + r + ",  arg0 "+ arg0.getLongitude() + " " + arg0.getLatitude() + " " + arg0.getAccuracy() + " " + arg0.getCampaignId() + " " + arg0.getId() + " " );
 				System.out.println("****************************");
 			return r;
 			}
 		}));
 		System.out.println("registered codes: " + registeredCodes);
 		Iterator<QrCode> it = registeredCodes.iterator();
 		
 		StringBuilder mapUrl = new StringBuilder();
 		if (it.hasNext()) {
 			mapUrl.append("http://maps.google.com/maps/api/staticmap?zoom=15&size=330x230&maptype=roadmap&markers=color:red|");
 			appendPosition(mapUrl, it.next());
 			mapUrl.append("&markers=size:tiny|color:blue");
 			while(it.hasNext()) {
 				mapUrl.append("|");
 				appendPosition(mapUrl, it.next());
 			}
 			mapUrl.append("&mobile=true&sensor=true");
 		}
 		
 		return new ModelAndView("campaign", ImmutableMap.of("campaign", camp, "qrcodeList", presentationCodes, "mapUrl", mapUrl));
 	}
 
 	private void appendPosition(StringBuilder mapUrl, QrCode next) {
 		mapUrl.append(next.getLatitude()).append(",").append(next.getLongitude());
 	}
 
 	@RequestMapping(value="/createQrCodes", method=RequestMethod.POST)
 	public ModelAndView createQrCodes(@RequestParam("campaignid") String id,
 			@RequestParam("numberOfCodes") Integer ammount) {
 		if (!isIdValid(id)){
 			return errorPage("Bad campaign id!");
 		}
 		
 		Map<String, String> errorMap = new HashMap<String, String>();
 		
 		ammount = ammount == null ? 1 : ammount;
 		if (!isValidAmmountOfQrCodes(ammount)) {
 			errorMap.put("numberOfCodes", "Max " + MAX_QR_CODES + " per form submit");	
 		}
 
 		
 		Campaign campaign = dbService.findCampaignById(id);
 		if (campaign==null){
 			return errorPage("Unknown campaign id!");
 		}
 		
 		if (errorMap.isEmpty()) {
 			List<String> qrCodeList = new ArrayList<String>();
 			for (int i = 0; i < ammount ; i ++){
 				final URL url = qrCodeService.generateQrCode(id);
 				if (url != null) {
 					qrCodeList.add(url.toExternalForm());
 				}
 			}
 			return new ModelAndView(QR_CODE_PAGE, ImmutableMap.of(CAMPAIGN_FIELD, campaign, QR_CODE_LIST, qrCodeList));
 		}
 		
 		return new ModelAndView("campaign", ImmutableMap.of("campaign", campaign, "errorMap", errorMap));
 	}
 
 	private ModelAndView errorPage(String error) {
 		return new ModelAndView("errorPage", ImmutableMap.of("error", error));
 	}
 
 	private boolean isValidAmmountOfQrCodes(Integer ammount) {
 		if (ammount < 1 || ammount > MAX_QR_CODES) {
 			return false;
 		}
 
 		return true;
 	}
 
 	private boolean isIdValid(String id) {
 		if (id != null && id.matches("[0-9a-f]{24}")) {
 			return true;
 		}
 		return false;
 	}
 
 	private boolean isUrlValid(String refererUrl) {
 
 		try {
 			new URL(refererUrl);
 		} catch (MalformedURLException e) {
 
 			return false;
 		}
 
 		return true;
 	}
 
 	private String cleanUpString(String dirtyInput) {
         if (Strings.isNullOrEmpty(dirtyInput)) {
         	return null;
         }
 		AntiSamy as = new AntiSamy();
 		try {
 
 			String cleanHTML = as.scan(dirtyInput, policy, AntiSamy.SAX)
 					.getCleanHTML();
 			return cleanHTML == null || cleanHTML.length() == 0 ? null
 					: cleanHTML;
 
 		} catch (ScanException e) {
 			e.printStackTrace();
 			log.error(dirtyInput + " is not a valid input!", e);
 			return null;
 		} catch (PolicyException e) {
 			e.printStackTrace();
 			log.error(dirtyInput + " applies not for the policy!", e);
 			return null;
 		}
 
 	}
 
 	public QrCodeService getQrCodeService() {
 		return qrCodeService;
 	}
 
 	public void setQrCodeService(QrCodeService qrCodeService) {
 		this.qrCodeService = qrCodeService;
 	}
 }
