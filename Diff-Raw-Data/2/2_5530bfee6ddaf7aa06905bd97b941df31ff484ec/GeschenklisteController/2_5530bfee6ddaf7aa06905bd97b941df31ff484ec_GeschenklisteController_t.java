 package de.giftbox.controller;
 
 import java.util.List;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 import de.giftbox.dao.GeschenklisteDAO;
 import de.giftbox.domain.Geschenkliste;
 import de.giftbox.helper.JSONStringToMap;
 
 @Controller
 @RequestMapping("/geschenkliste/*")
 public class GeschenklisteController {
 
 	GeschenklisteDAO geschenklisteDao;
 	JSONStringToMap jsonStringToMap;
 	
 	private static final Logger log = LoggerFactory
 			.getLogger(GeschenklisteController.class);
 	
 	@RequestMapping(value = "all", method = RequestMethod.GET)
 	private @ResponseBody
 	List<Geschenkliste> getAllGeschenkliste() {
 		log.debug("get all geschenklisten!");
 		List<Geschenkliste> listGeschenkliste = geschenklisteDao.listGeschenkliste();
 		return listGeschenkliste;
 	}
 
	public void setGeschenklisteDAO(GeschenklisteDAO geschenklisteDao) {
 		this.geschenklisteDao = geschenklisteDao;
 	}
 
 	public void setJsonStringToMap(JSONStringToMap jsonStringToMap) {
 		this.jsonStringToMap = jsonStringToMap;
 	}
 	
 	
 }
