 package com.nkhoang.gae.action;
 
 import com.nkhoang.gae.manager.GoldManager;
 import com.nkhoang.gae.model.GoldPrice;
 import com.nkhoang.gae.utils.WebUtils;
 import com.nkhoang.gae.view.constant.ViewConstant;
 import net.htmlparser.jericho.Element;
 import net.htmlparser.jericho.Source;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 
 import java.io.IOException;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 @Controller
 @RequestMapping("/service/gold")
 public class GoldINAction {
     private static final Logger LOGGER = LoggerFactory.getLogger(GoldINAction.class);
     @Autowired    
     private GoldManager goldService;
    @RequestMapping("/" + ViewConstant.GOLD_IN_UPDATE)
     public void update() {
         LOGGER.info("Starting to update Gold International...");
        GoldPrice goldPrice = getInternationalGoldPrice();
        LOGGER.info("Gold Price retrieved: " + goldPrice.toString());
         goldService.save(goldPrice);
     }
     private GoldPrice getInternationalGoldPrice() {
         GoldPrice price = new GoldPrice();
         Source source = null;
         try {
             source = WebUtils.retrieveWebContent("http://giavang.net/resources/pricetable2a.asp");
         } catch (IOException ioe) {
             LOGGER.error("Could not parse URL: [http://giavang.net/resources/pricetable2a.asp]", ioe);
         }
 
         if (source != null) {
             List<Element> trElements = source.getAllElementsByClass("row1");
             Element trElement = trElements.get(0);
 
             List<Element> tdElements = trElement.getChildElements();
             // get the 2nd td
             Element priceElement = tdElements.get(1);
 
             String priceString = priceElement.getTextExtractor().toString();
             LOGGER.info("International Gold Price: " + priceString);
             // process string
             Calendar calendar = GregorianCalendar.getInstance();
             Date currentDate = calendar.getTime();
             price.setTime(currentDate);
             price.setCurrency("USD");
             // Using regex to get price value.
             Pattern p = Pattern.compile("\\d+.\\d+");
             Matcher m = p.matcher(priceString.split("/")[0]);
             if (m.find()) {
                 price.setPriceSell(Float.parseFloat(m.group(0)));
             }
             m = p.matcher(priceString.split("/")[1]);
             if (m.find()) {
                 price.setPriceBuy(Float.parseFloat(m.group(0)));
             }
         }
         return price;
     }
 
     public GoldManager getGoldService() {
         return goldService;
     }
 
     public void setGoldService(GoldManager goldService) {
         this.goldService = goldService;
     }
 }
