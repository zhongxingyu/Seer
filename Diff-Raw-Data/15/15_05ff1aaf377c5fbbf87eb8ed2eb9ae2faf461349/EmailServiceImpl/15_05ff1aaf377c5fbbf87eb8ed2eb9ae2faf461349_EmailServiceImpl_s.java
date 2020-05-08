 package com.natepaulus.dailyemail.web.service.impl;
 
 import java.io.InputStream;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.annotation.Resource;
 import javax.mail.internet.MimeMessage;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.xpath.XPath;
 import javax.xml.xpath.XPathConstants;
 import javax.xml.xpath.XPathExpression;
 import javax.xml.xpath.XPathFactory;
 
 import org.apache.velocity.app.VelocityEngine;
 import org.joda.time.DateTime;
 import org.joda.time.DateTimeZone;
 import org.joda.time.LocalTime;
 import org.joda.time.format.DateTimeFormat;
 import org.joda.time.format.DateTimeFormatter;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.mail.javamail.JavaMailSender;
 import org.springframework.mail.javamail.MimeMessageHelper;
 import org.springframework.mail.javamail.MimeMessagePreparator;
 import org.springframework.scheduling.annotation.Scheduled;
 import org.springframework.stereotype.Service;
 import org.springframework.ui.velocity.VelocityEngineUtils;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 
 import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator;
 import com.luckycatlabs.sunrisesunset.dto.Location;
 import com.natepaulus.dailyemail.repository.DeliverySchedule;
 import com.natepaulus.dailyemail.repository.DeliveryScheduleRepository;
 import com.natepaulus.dailyemail.repository.NewsLink;
 import com.natepaulus.dailyemail.repository.User;
 import com.natepaulus.dailyemail.web.domain.EmailData;
 import com.natepaulus.dailyemail.web.domain.NewsFeed;
 import com.natepaulus.dailyemail.web.domain.NewsStory;
 import com.natepaulus.dailyemail.web.service.interfaces.EmailService;
 import com.sun.syndication.feed.synd.SyndEntry;
 import com.sun.syndication.feed.synd.SyndFeed;
 import com.sun.syndication.io.SyndFeedInput;
 import com.sun.syndication.io.XmlReader;
 
 @Service
 public class EmailServiceImpl implements EmailService {
 	
 	final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);
 	
 	@Resource
 	DeliveryScheduleRepository deliveryScheduleRepository;
 	
 	@Autowired
 	private JavaMailSender sender;
 	
 	@Autowired
 	private VelocityEngine velocityEngine;
 	
 	@Scheduled(cron = "0 0/1 * * * ?")
 	public void retrieveUserListForEmails(){
 		logger.info("Retrieving List of users to email");
 		int dayOfWeek = -1;
 		DateTime currentTime = new DateTime();
		if(currentTime.getDayOfWeek() != 7 || currentTime.getDayOfWeek() != 6){
			dayOfWeek = 0;			
 		} else {
			dayOfWeek = 1;
 		}
 		LocalTime currentLocalTime = currentTime.toLocalTime();
 		LocalTime lt = currentLocalTime.withSecondOfMinute(0).withMillisOfSecond(0);		
 		
 		List<DeliverySchedule> ds = deliveryScheduleRepository.findByTimeAndDeliveryDay(lt, dayOfWeek);
 		List<User> users = new ArrayList<User>();
 		for(DeliverySchedule d : ds){
 			users.add(d.getUser());
 			logger.info("Found a user");
 		}
 		
 		Iterator<User> userIterator = users.iterator();
 		while(userIterator.hasNext()){
 			User user = userIterator.next();
 			sendEmail(user);			
 		}		
 	}
 	
 	private void sendEmail(User user){
 		
 		EmailData emailData = new EmailData();
 		emailData.setToAddress(user.getEmail());
 		emailData.setToName(user.getFirstName() + " " + user.getLastName());
 		
 		emailData = getWeatherConditions(emailData, user);
 		emailData = getNewsStoriesForEmail(emailData, user);
 		final EmailData data = emailData;
 		
 		MimeMessagePreparator preparator = new MimeMessagePreparator(){
 			
 			@Override
 			public void prepare(MimeMessage mimeMessage) throws Exception {
 				MimeMessageHelper message = new MimeMessageHelper(mimeMessage, "UTF-8");
 				message.setTo(data.getToAddress());
 				message.setFrom("dailyemail@natepaulus.com");
 				message.setSubject("Daily News & Weather");
 				Map<String, Object> model = new HashMap<String, Object>();
 				model.put("data", data);
 				String messageText = VelocityEngineUtils.mergeTemplateIntoString(velocityEngine, "email.vm", model);				
 				message.setText(messageText, true);				
 			}
 		};
 		
 		this.sender.send(preparator);
 		logger.info("Message sent to: " + data.getToAddress());
 	}
 	
 	private EmailData getWeatherConditions(EmailData data, User user){
 		Set<DeliverySchedule> ds = user.getDeliveryTimes();
 		Iterator<DeliverySchedule> dsI = ds.iterator();
 		String tz = dsI.next().getTz();
 		try {
 
 			URL curCond_ForecastURL = new URL(
 					"http://forecast.weather.gov/MapClick.php?lat=" + user.getWeather().getLatitude()
 							+ "&lon=" + user.getWeather().getLongitude()
 							+ "&unit=0&lg=english&FcstType=dwml");					
 			InputStream inCC_Fx = curCond_ForecastURL.openStream();
 			DocumentBuilderFactory factoryCurCond_Fx = DocumentBuilderFactory
 					.newInstance();
 			DocumentBuilder builderCCFX = factoryCurCond_Fx
 					.newDocumentBuilder();
 			org.w3c.dom.Document docCFX = builderCCFX.parse(inCC_Fx);
 
 			XPathFactory xPathfactoryCCFX = XPathFactory.newInstance();
 			XPath xpathCCFX = xPathfactoryCCFX.newXPath();
 			XPathExpression exprCCFX = null;
 
 			if (user.getWeather().getDeliver_pref() == 1 || user.getWeather().getDeliver_pref() == 3) {
 				exprCCFX = xpathCCFX
 						.compile("/dwml/data[@type = 'current observations']/time-layout/start-valid-time[@period-name = 'current']/text()");
 				String temp = (String) exprCCFX.evaluate(docCFX,
 						XPathConstants.STRING);
 
 				DateTimeFormatter fmt = DateTimeFormat
 						.forPattern("yyyy-MM-dd'T'HH:mm:ssZ");
 				DateTime xmlTime = fmt.parseDateTime(temp);
 
 				DateTime localTime = xmlTime.withZone(DateTimeZone
 						.forID("America/New_York"));
 				logger.info("TZ: " + tz);
 				logger.info("Localtime: "+ localTime.toString());
 				DateTimeFormatter outFmt = DateTimeFormat
 						.forPattern("MMM dd',' yyyy h:mm a");
 				logger.info("Printing Local Time with printer: " + outFmt.print(localTime));
 				data.getWxCurCond().setLatestObDateTime(outFmt.print(localTime));
 				data.getWxCurCond().setCityState(user.getWeather().getLocation_name());
 				
 				exprCCFX = xpathCCFX
 						.compile("/dwml/data[@type = 'current observations']/location/area-description/text()");
 				data.getWxCurCond().setWeatherStation((String) exprCCFX.evaluate(docCFX,
 						XPathConstants.STRING));
 				exprCCFX = xpathCCFX
 						.compile("/dwml/data[@type = 'current observations']/parameters/wind-speed[@type = 'sustained']/value/text()");
 				double mphWindSpeed = 0;
 				boolean isNAWindSpeed = false;
 				try {
 					mphWindSpeed = (Double) exprCCFX.evaluate(docCFX,
 							XPathConstants.NUMBER);
 					mphWindSpeed = 1.15155 * mphWindSpeed;
 				} catch (NumberFormatException e) {
 					isNAWindSpeed = true;
 				}
 				if (isNAWindSpeed) {
 					data.getWxCurCond().setWindSpeed("NA");
 				} else {
 					int wSpeed = (int) Math.round(mphWindSpeed);
 					data.getWxCurCond().setWindSpeed(Integer.toString(wSpeed));
 				}
 				exprCCFX = xpathCCFX
 						.compile("/dwml/data[@type = 'current observations']/parameters/wind-speed[@type = 'gust']/value/text()");
 				double mphWindGust = 0;
 				boolean isNAWindGust = false;
 				try {
 					mphWindGust = (Double) exprCCFX.evaluate(docCFX,
 							XPathConstants.NUMBER);
 					mphWindGust = 1.15155 * mphWindSpeed;
 				} catch (NumberFormatException e) {
 					isNAWindGust = true;
 				}
 				if (isNAWindGust) {
 					data.getWxCurCond().setWindGust("NA");
 				} else {
 					int wGust = (int) Math.round(mphWindGust);
 					data.getWxCurCond().setWindGust(Integer.toString(wGust));
 				}
 				exprCCFX = xpathCCFX
 						.compile("/dwml/data[@type = 'current observations']/parameters/humidity[@type = 'relative']/value/text()");
 				data.getWxCurCond().setHumidity((String) exprCCFX.evaluate(docCFX,
 						XPathConstants.STRING));
 				exprCCFX = xpathCCFX
 						.compile("/dwml/data[@type = 'current observations']/parameters/temperature[@type = 'apparent']/value/text()");
 				data.getWxCurCond().setCurrentTemp((String) exprCCFX.evaluate(docCFX,
 						XPathConstants.STRING));
 				exprCCFX = xpathCCFX
 						.compile("/dwml/data[@type = 'current observations']/parameters/weather[@time-layout = 'k-p1h-n1-1']/weather-conditions/@weather-summary");
 				data.getWxCurCond().setCurWx((String) exprCCFX.evaluate(docCFX,
 						XPathConstants.STRING));
 
 				Location location = new Location(user.getWeather().getLatitude(), user.getWeather().getLongitude());
 				SunriseSunsetCalculator calculator = new SunriseSunsetCalculator(
 						location, tz);
 				Calendar officialSunrise = calculator
 						.getOfficialSunriseCalendarForDate(Calendar
 								.getInstance());
 				Calendar officialSunset = calculator
 						.getOfficialSunsetCalendarForDate(Calendar
 								.getInstance());
 				DateTimeFormatter dtfSunriseset = DateTimeFormat
 						.forPattern("h:mm a");
 				DateTime dtSunrise = new DateTime(officialSunrise);
 				DateTime dtSunset = new DateTime(officialSunset);
 				DateTime dtSunriseLocal = dtSunrise.withZone(DateTimeZone
 						.forID(tz));
 				DateTime dtSunsetLocal = dtSunset.withZone(DateTimeZone
 						.forID(tz));
 
 				data.getWxCurCond().setSunRise(dtfSunriseset.print(dtSunriseLocal));
 				data.getWxCurCond().setSunSet(dtfSunriseset.print(dtSunsetLocal));
 				
 			}
 			if (user.getWeather().getDeliver_pref() == 2 || user.getWeather().getDeliver_pref() == 3) {
 				exprCCFX = xpathCCFX
 						.compile("/dwml/data[@type='forecast']/time-layout[layout-key/text()=/dwml/data[@type='forecast']/parameters[@applicable-location='point1']/wordedForecast/@time-layout]/start-valid-time");
 				NodeList days = (NodeList) exprCCFX.evaluate(docCFX,
 						XPathConstants.NODESET);
 				exprCCFX = xpathCCFX
 						.compile("/dwml/data[@type='forecast']/parameters[@applicable-location='point1']/wordedForecast/text");
 				NodeList forecastText = (NodeList) exprCCFX.evaluate(docCFX,
 						XPathConstants.NODESET);				
 				
 				for (int i = 0; i < 3; i++) {
 					Element e = (Element) days.item(i);
 					Element e2 = (Element) forecastText.item(i);
 					data.getWeatherForecast().getPeriodForecast().put(e.getAttribute("period-name"), e2.getTextContent());
 					
 				}				
 			}
 			inCC_Fx.close();
 		} catch (Exception ex) {
 			logger.error("Get Weather Data had an issue.", ex);
 		}
 		
 		return data;
 						
 	}
 	
 	private EmailData getNewsStoriesForEmail(EmailData data, User user){
 		
 		for(NewsLink n : user.getNewsLink()){
 			if(n.getDeliver() == 1){
 				NewsFeed newsFeed = new NewsFeed();
 				newsFeed.setFeedTitle(n.getSource_name());
 				try {
 	                URL xmlUrl = new URL(n.getUrl());
 	                XmlReader reader = new XmlReader(xmlUrl);
 	                SyndFeed feed = new SyndFeedInput().build(reader);
 	                @SuppressWarnings("rawtypes")
 					Iterator iFeed = feed.getEntries().iterator();
 	                int count = 0;
 	                int max = 5;
 	                
 	                while(iFeed.hasNext() && count < max) {
 	                    
 	                	SyndEntry entry = (SyndEntry) iFeed.next();
 	                    String title = entry.getTitle();
 	                    String link = entry.getLink();
 	                    String desc = entry.getDescription().getValue().replaceAll("\\<.*?>", "");
 	                    NewsStory newsStory = new NewsStory(title, link, desc);
 	                    newsFeed.getNewsStories().add(newsStory);
 	                    count++;
 	                    
 	                }              
 	                
 	            } catch (Exception ex) {
 	                ex.printStackTrace();
 	            }
 				
 				data.getNewsFeeds().add(newsFeed);
 			}
 		}
 		
 		return data;
 	}
 
 }
