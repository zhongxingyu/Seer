 package org.offlike.server;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.sound.midi.Patch;
 
 import org.offlike.server.data.Campaign;
 import org.offlike.server.service.MongoDbService;
 import org.offlike.server.service.QrCodeService;
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
 
 import com.google.common.base.Strings;
 import com.google.common.collect.ImmutableMap;
 
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
 	Policy policy;
 
 	@Autowired
 	MongoDbService dbService;
 
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
 		return new ModelAndView("campaign", ImmutableMap.of("campaign", camp));
 	}
 
 	@RequestMapping("/qr/{id}")
 	public ModelAndView createQrCodes(@PathVariable("id") String id,
 			@RequestParam("ammount") Integer ammount) {
 
 		ammount = ammount == null ? 1 : ammount;
 		if (!isValidAmmountOfQrCodes(ammount)) {
 			return errorPage("Maxium are "+MAX_QR_CODES+" Qr Codes per request");
 		}
 
 		if (!isIdValid(id)){
 			return errorPage("Bad campaign id!");
 		}
 		
 		Campaign campaign = dbService.findCampaignById(id);
 		if (campaign==null){
 			return errorPage("Unknown campaign id!");
 		}
 		
 		List<String> qrCodeList = new ArrayList<String>();
 		for (int i = 0; i < ammount ; i ++){
			final URL url = qrCodeService.generateQrCode(id);
			if (url != null) {
				qrCodeList.add(url.toExternalForm());
			}
 		}
 		
 		
 		return new ModelAndView(QR_CODE_PAGE, ImmutableMap.of(CAMPAIGN_FIELD, campaign, QR_CODE_LIST, qrCodeList));
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
