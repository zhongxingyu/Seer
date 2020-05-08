 package com.natepaulus.dailyemail.web.service.impl;
 
 import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator;
 import com.luckycatlabs.sunrisesunset.dto.Location;
 import com.natepaulus.dailyemail.repository.*;
 import com.natepaulus.dailyemail.repository.entity.*;
 import com.natepaulus.dailyemail.web.domain.EmailData;
 import com.natepaulus.dailyemail.web.domain.NewsFeed;
 import com.natepaulus.dailyemail.web.domain.NewsStory;
 import com.natepaulus.dailyemail.web.service.interfaces.EmailService;
 import com.sun.syndication.feed.synd.SyndEntry;
 import com.sun.syndication.feed.synd.SyndFeed;
 import com.sun.syndication.io.SyndFeedInput;
 import org.apache.velocity.app.VelocityEngine;
 import org.joda.time.DateTime;
 import org.joda.time.DateTimeZone;
 import org.joda.time.LocalTime;
 import org.joda.time.format.DateTimeFormat;
 import org.joda.time.format.DateTimeFormatter;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.annotation.PropertySource;
 import org.springframework.core.env.Environment;
 import org.springframework.data.domain.PageRequest;
 import org.springframework.data.domain.Pageable;
 import org.springframework.mail.MailException;
 import org.springframework.mail.javamail.JavaMailSender;
 import org.springframework.mail.javamail.MimeMessageHelper;
 import org.springframework.mail.javamail.MimeMessagePreparator;
 import org.springframework.scheduling.annotation.Scheduled;
 import org.springframework.stereotype.Service;
 import org.springframework.ui.velocity.VelocityEngineUtils;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 import org.xml.sax.InputSource;
 
 import javax.annotation.Resource;
 import javax.mail.internet.MimeMessage;
 import javax.transaction.Transactional;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.xpath.XPath;
 import javax.xml.xpath.XPathConstants;
 import javax.xml.xpath.XPathExpression;
 import javax.xml.xpath.XPathFactory;
 import java.io.InputStream;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.*;
 
 // TODO: Auto-generated Javadoc
 /**
  * The Class EmailServiceImpl checks every minute to see if any users have requested their email at that time. If it finds any users it
  * then builds and sends them an email with their requested data.
  */
 @Service
 @PropertySource("classpath:emailConfig.properties")
 public class EmailServiceImpl implements EmailService {
 
 	@Resource
 	private Environment environment;
 
 	/** The email addresses to use: */
 	private final static String ADMINISTRATOR = "administrator.email";
 	private final static String DAILY_EMAIL_ADDRESS = "dailyemail.from.email";
 
 	/** The logger. */
 	final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);
 
 	/** The delivery schedule repository. */
 	@Resource
 	DeliveryScheduleRepository deliveryScheduleRepository;
 
 	/** The Rss Feeds Repository. */
 	@Resource
 	RssFeedsRepository rssFeedsRepository;
 
 	/** The User Rss Feeds Repository. */
 	@Resource
 	RssNewsLinksRepository rssNewsLinksRepository;
 
 	/** The user repository. */
 	@Resource
 	UserRepository userRepository;
 
 	/** The failed messages repository. */
 	@Resource
 	FailedMessagesRepository failedMessagesRepository;
 
 	/** The java mail sender for sending email. */
 	@Autowired
 	private JavaMailSender sender;
 
 	/** The velocity engine. */
 	@Autowired
 	private VelocityEngine velocityEngine;
 
 	/*
 	 * (non-Javadoc)
 	 *
 	 * @see com.natepaulus.dailyemail.web.service.interfaces.EmailService# generateQuickView(java.lang.String)
 	 */
 	@Override
 	public EmailData generateQuickView(final String code) {
 
 		final User user = this.userRepository.findByUrlCode(code);
 
 		EmailData data = new EmailData();
 		data = this.getWeatherConditions(data, user);
 		data = this.getNewsStoriesForEmail(data, user);
 
 		return data;
 	}
 
 	/**
 	 * Gets the news stories for email from the users defined RSS feeds.
 	 *
 	 * @param data the EmailData object to attach the news story information to
 	 * @param user the user
 	 * @return the news stories for email
 	 */
 	private EmailData getNewsStoriesForEmail(final EmailData data, final User user) {
 
 		final Set<UserRssFeeds> userRssFeeds = user.getUserRssFeeds();
 		final Pageable topFiveArticlesByDate = new PageRequest(0, 5);
 
 		for (final UserRssFeeds userRssFeed : userRssFeeds) {
 			if (userRssFeed.getDeliver() == 1) {
 				final NewsFeed newsFeed = new NewsFeed();
 				newsFeed.setFeedTitle(userRssFeed.getFeedName());
 				final List<RssNewsLinks> articles =
 						this.rssNewsLinksRepository.findByFeedIdOrderByPubDateDesc(userRssFeed.getFeedId(), topFiveArticlesByDate);
 				for (final RssNewsLinks r : articles) {
 					final NewsStory newsStory = new NewsStory(r.getTitle(), r.getLink(), r.getDescription());
 					newsFeed.getNewsStories().add(newsStory);
 				}
 
 				data.getNewsFeeds().add(newsFeed);
 			}
 		}
 
 		return data;
 	}
 
 	/**
 	 * Gets the weather conditions from the National Weather Service experimental forecast feed.
 	 *
 	 * @param data the EmailData object to add the weather conditions to
 	 * @param user the user
 	 * @return the weather conditions
 	 */
 	private EmailData getWeatherConditions(final EmailData data, final User user) {
 
 		try {
 
 			final URL curCond_ForecastURL =
 					new URL("http://forecast.weather.gov/MapClick.php?lat=" + user.getWeather().getLatitude() + "&lon="
 							+ user.getWeather().getLongitude() + "&unit=0&lg=english&FcstType=dwml");
 			final InputStream inCC_Fx = curCond_ForecastURL.openStream();
 			final DocumentBuilderFactory factoryCurCond_Fx = DocumentBuilderFactory.newInstance();
 			final DocumentBuilder builderCCFX = factoryCurCond_Fx.newDocumentBuilder();
 			final org.w3c.dom.Document docCFX = builderCCFX.parse(inCC_Fx);
 
 			final XPathFactory xPathfactoryCCFX = XPathFactory.newInstance();
 			final XPath xpathCCFX = xPathfactoryCCFX.newXPath();
 			XPathExpression exprCCFX = null;
 
 			if (user.getWeather().getDeliver_pref() == 1 || user.getWeather().getDeliver_pref() == 3) {
 				exprCCFX =
 						xpathCCFX
 						.compile("/dwml/data[@type = 'current observations']/time-layout/start-valid-time[@period-name = 'current']/text()");
 				final String temp = (String) exprCCFX.evaluate(docCFX, XPathConstants.STRING);
 
 				final DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZ");
 				final DateTime xmlTime = fmt.withOffsetParsed().parseDateTime(temp);
 
 				final DateTime localTime = xmlTime;
 
 				this.logger.info("Localtime: " + localTime.toString());
 				final DateTimeFormatter outFmt = DateTimeFormat.forPattern("MMM dd',' yyyy h:mm a");
 				this.logger.info("Printing Local Time with printer: " + outFmt.print(xmlTime));
 				data.getWxCurCond().setLatestObDateTime(outFmt.print(xmlTime));
 				data.getWxCurCond().setCityState(user.getWeather().getLocation_name());
 
 				exprCCFX = xpathCCFX.compile("/dwml/data[@type = 'current observations']/location/area-description/text()");
 				data.getWxCurCond().setWeatherStation((String) exprCCFX.evaluate(docCFX, XPathConstants.STRING));
 				exprCCFX =
 						xpathCCFX
 						.compile("/dwml/data[@type = 'current observations']/parameters/wind-speed[@type = 'sustained']/value/text()");
 				double mphWindSpeed = 0;
 				boolean isNAWindSpeed = false;
 				try {
 					mphWindSpeed = (Double) exprCCFX.evaluate(docCFX, XPathConstants.NUMBER);
 					mphWindSpeed = 1.15155 * mphWindSpeed;
 				} catch (final NumberFormatException e) {
 					isNAWindSpeed = true;
 				}
 				if (isNAWindSpeed) {
 					data.getWxCurCond().setWindSpeed("NA");
 				} else {
 					final int wSpeed = (int) Math.round(mphWindSpeed);
 					data.getWxCurCond().setWindSpeed(Integer.toString(wSpeed));
 				}
 				exprCCFX =
 						xpathCCFX
 						.compile("/dwml/data[@type = 'current observations']/parameters/wind-speed[@type = 'gust']/value/text()");
 				double mphWindGust = 0;
 				boolean isNAWindGust = false;
 				try {
 					mphWindGust = (Double) exprCCFX.evaluate(docCFX, XPathConstants.NUMBER);
 					mphWindGust = 1.15155 * mphWindSpeed;
 				} catch (final NumberFormatException e) {
 					isNAWindGust = true;
 				}
 				if (isNAWindGust) {
 					data.getWxCurCond().setWindGust("NA");
 				} else {
 					final int wGust = (int) Math.round(mphWindGust);
 					data.getWxCurCond().setWindGust(Integer.toString(wGust));
 				}
 				exprCCFX =
 						xpathCCFX
 						.compile("/dwml/data[@type = 'current observations']/parameters/humidity[@type = 'relative']/value/text()");
 				data.getWxCurCond().setHumidity((String) exprCCFX.evaluate(docCFX, XPathConstants.STRING));
 				exprCCFX =
 						xpathCCFX
 						.compile("/dwml/data[@type = 'current observations']/parameters/temperature[@type = 'apparent']/value/text()");
 				data.getWxCurCond().setCurrentTemp((String) exprCCFX.evaluate(docCFX, XPathConstants.STRING));
 				exprCCFX =
 						xpathCCFX
 						.compile("/dwml/data[@type = 'current observations']/parameters/temperature[@type = 'dew point']/value/text()");
 				data.getWxCurCond().setDewPoint((String) exprCCFX.evaluate(docCFX, XPathConstants.STRING));
 				exprCCFX =
 						xpathCCFX
 						.compile("/dwml/data[@type = 'current observations']/parameters/weather[@time-layout = 'k-p1h-n1-1']/weather-conditions/@weather-summary");
 				data.getWxCurCond().setCurWx((String) exprCCFX.evaluate(docCFX, XPathConstants.STRING));
 				final DateTimeZone dtz = xmlTime.getZone();
 				this.logger.info("TimeZone: " + dtz.toString());
 				final Location location = new Location(user.getWeather().getLatitude(), user.getWeather().getLongitude());
 				final SunriseSunsetCalculator calculator = new SunriseSunsetCalculator(location, dtz.toString());
 				final Calendar officialSunrise = calculator.getOfficialSunriseCalendarForDate(Calendar.getInstance());
 				final Calendar officialSunset = calculator.getOfficialSunsetCalendarForDate(Calendar.getInstance());
 				final DateTimeFormatter dtfSunriseset = DateTimeFormat.forPattern("h:mm a");
 				final DateTime dtSunrise = new DateTime(officialSunrise);
 				final DateTime dtSunset = new DateTime(officialSunset);
 				final DateTime dtSunriseLocal = dtSunrise.withZone(DateTimeZone.forID(dtz.toString()));
 				final DateTime dtSunsetLocal = dtSunset.withZone(DateTimeZone.forID(dtz.toString()));
 
 				data.getWxCurCond().setSunRise(dtfSunriseset.print(dtSunriseLocal));
 				data.getWxCurCond().setSunSet(dtfSunriseset.print(dtSunsetLocal));
 
 			}
 			if (user.getWeather().getDeliver_pref() == 2 || user.getWeather().getDeliver_pref() == 3) {
 				exprCCFX =
 						xpathCCFX
 						.compile("/dwml/data[@type='forecast']/time-layout[layout-key/text()=/dwml/data[@type='forecast']/parameters[@applicable-location='point1']/wordedForecast/@time-layout]/start-valid-time");
 				final NodeList days = (NodeList) exprCCFX.evaluate(docCFX, XPathConstants.NODESET);
 				exprCCFX =
 						xpathCCFX
 						.compile("/dwml/data[@type='forecast']/parameters[@applicable-location='point1']/wordedForecast/text");
 				final NodeList forecastText = (NodeList) exprCCFX.evaluate(docCFX, XPathConstants.NODESET);
 
 				for (int i = 0; i < 3; i++) {
 					final Element e = (Element) days.item(i);
 					final Element e2 = (Element) forecastText.item(i);
					data.getWeatherForecast().getPeriodForecast().put(e.getAttribute("period-name"), e2.getNodeValue());
 
 				}
 			}
 			inCC_Fx.close();
 		} catch (final Exception ex) {
 			this.logger.error("Get Weather Data had an issue.", ex);
 		}
 
 		return data;
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 *
 	 * @see com.natepaulus.dailyemail.web.service.interfaces.EmailService# retrieveUserListForEmails()
 	 */
 	@Override
 	@Scheduled(cron = "0 0/1 * * * ?")
 	public void retrieveUserListForEmails() {
 		// logger.info("Retrieving List of users to email");
 		int dayOfWeek = -1;
 		final DateTime currentTime = new DateTime();
 
 		final List<DeliverySchedule> allDeliverySchedules = this.deliveryScheduleRepository.findAll();
 		final List<User> users = new ArrayList<User>();
 
 		for (final DeliverySchedule d : allDeliverySchedules) {
 			// get current time in user's local date & time
 			final DateTime currentLocalTime =
 					currentTime.withSecondOfMinute(0).withMillisOfSecond(0).withZone(DateTimeZone.forID(d.getTz()));
 
 			final int currentDayOfWeek = currentLocalTime.getDayOfWeek();
 
 			if (currentDayOfWeek == 7 || currentDayOfWeek == 6) {
 				dayOfWeek = 1;
 			} else {
 				dayOfWeek = 0;
 			}
 
 			final LocalTime userSetTime = d.getTime(); // user's set time
 
 			// convert local time to today's time & date and user's local time &
 			// date
 			final DateTime userSetTimeDateTime = userSetTime.toDateTimeToday().withZone(DateTimeZone.UTC);
 			final DateTime userLocalSetTime = userSetTimeDateTime.withZone(DateTimeZone.forID(d.getTz()));
 
 			/*
 			 * logger.info("before IF userLocalSetTime: " + userLocalSetTime.toString()); logger.info("before IF currentLocalTime: " +
 			 * currentLocalTime.toString());
 			 */
 
 			// check if current time equals user set time for today's date
 			if (userLocalSetTime.equals(currentLocalTime)) {
 				// logger.info("User's local set time is equal to current local time.");
 				// if the delivery day (weekend or weekday) equals the delivery
 				// day in the schedule add user to list
 				if (d.getDeliveryDay() == dayOfWeek) {
 					users.add(d.getUser());
 					// logger.info("Added user: " + d.getUser().getEmail());
 				}
 			}
 		}
 
 		final Iterator<User> userIterator = users.iterator();
 		while (userIterator.hasNext()) {
 			final User user = userIterator.next();
 			this.sendEmail(user);
 		}
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 *
 	 * @see com.natepaulus.dailyemail.web.service.interfaces.EmailService# retrySendingFailedMessages()
 	 */
 	@Override
 	@Scheduled(cron = "0 0/5 * * * ?")
 	public void retrySendingFailedMessages() {
 		// logger.info("retry sending failed messages");
 		final List<FailedMessages> failedMessages = this.failedMessagesRepository.findAll();
 
 		for (final FailedMessages failedMsg : failedMessages) {
 			final MimeMessagePreparator preparator = new MimeMessagePreparator() {
 
 				@Override
 				public void prepare(final MimeMessage mimeMessage) throws Exception {
 					final MimeMessageHelper message = new MimeMessageHelper(mimeMessage, "UTF-8");
 					message.setTo(failedMsg.getToAddress());
 					message.setFrom(EmailServiceImpl.this.environment.getRequiredProperty(DAILY_EMAIL_ADDRESS));
 					message.setSubject("Daily News & Weather");
 
 					message.setText(failedMsg.getMessage(), true);
 				}
 			};
 			try {
 				this.sender.send(preparator);
 				this.failedMessagesRepository.delete(failedMsg);
 				// logger.info("Successfully resent");
 			} catch (final MailException e) {
 				final int failedAttempts = failedMsg.getNumberFailedAttempts() + 1;
 				failedMsg.setNumberFailedAttempts(failedAttempts);
 				this.failedMessagesRepository.save(failedMsg);
 				// logger.info("Failed again and incremented counter");
 			}
 		}
 
 	}
 
 	/**
 	 * Send email to the user with the data they have requested.
 	 *
 	 * @param user the user
 	 */
 	private void sendEmail(final User user) {
 
 		EmailData emailData = new EmailData();
 		emailData.setToAddress(user.getEmail());
 		emailData.setToName(user.getFirstName() + " " + user.getLastName());
 
 		emailData = this.getWeatherConditions(emailData, user);
 		emailData = this.getNewsStoriesForEmail(emailData, user);
 		final EmailData data = emailData;
 		final Map<String, Object> model = new HashMap<String, Object>();
 		model.put("data", data);
 		@SuppressWarnings("deprecation")
 		final String messageText = VelocityEngineUtils.mergeTemplateIntoString(this.velocityEngine, "email.vm", model);
 		final MimeMessagePreparator preparator = new MimeMessagePreparator() {
 
 			@Override
 			public void prepare(final MimeMessage mimeMessage) throws Exception {
 				final MimeMessageHelper message = new MimeMessageHelper(mimeMessage, "UTF-8");
 				message.setTo(data.getToAddress());
 				message.setFrom(EmailServiceImpl.this.environment.getRequiredProperty(DAILY_EMAIL_ADDRESS));
 				message.setSubject("Daily News & Weather");
 				message.setText(messageText, true);
 			}
 		};
 		try {
 			this.sender.send(preparator);
 			this.logger.info("Message sent to: " + data.getToAddress());
 		} catch (final MailException e) {
 			final FailedMessages fm = new FailedMessages();
 			fm.setToAddress(data.getToAddress());
 			fm.setToName(data.getToName());
 			fm.setMessage(messageText);
 			fm.setErrorMessage(e.getMessage());
 			fm.setNumberFailedAttempts(1);
 			this.failedMessagesRepository.save(fm);
 			this.logger.info("Email errored occured. Saved to Database.");
 		}
 	}
 
 	/**
 	 * Send error email.
 	 *
 	 * @param errorMessage the error message
 	 */
 	private void sendErrorEmail(final String errorMessage) {
 
 		final MimeMessagePreparator preparator = new MimeMessagePreparator() {
 
 			@Override
 			public void prepare(final MimeMessage mimeMessage) throws Exception {
 				final MimeMessageHelper message = new MimeMessageHelper(mimeMessage, "UTF-8");
 				message.setTo(EmailServiceImpl.this.environment.getRequiredProperty(ADMINISTRATOR));
 				message.setFrom(EmailServiceImpl.this.environment.getRequiredProperty(DAILY_EMAIL_ADDRESS));
 				message.setSubject("Error - Daily News & Weather");
 
 				message.setText(errorMessage, true);
 			}
 		};
 		this.sender.send(preparator);
 		this.logger.info("Error Message sent!");
 	}
 
 	/*
 	 * (non-Javadoc)
 	 *
 	 * @see com.natepaulus.dailyemail.web.service.interfaces.EmailService# updateRssFeedLinks()
 	 */
 	@Override
 	@Scheduled(cron = "0 0/30 * * * ?")
 	@Transactional
 	public void updateRssFeedLinks() {
 		final List<RssFeeds> rssFeeds = this.rssFeedsRepository.findByDisabled(false);
 		this.logger.info("Processing rss feeds");
 
 
 
 		for (final RssFeeds rssFeed : rssFeeds) {
 			System.out.println("Feed Name: " + rssFeed.getUrl());
 			try {
 				Set<RssNewsLinks> rssLinks = new HashSet<RssNewsLinks>();
 				
 				final URLConnection connection = new URL(rssFeed.getUrl()).openConnection();
 				connection
 				.setRequestProperty("User-Agent",
 						"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
 				connection.connect();
 				final InputStream is = connection.getInputStream();
 				final InputSource source = new InputSource(is);
 				final SyndFeedInput input = new SyndFeedInput();
 				final SyndFeed feed = input.build(source);
 
 				@SuppressWarnings("rawtypes")
 				final Iterator iFeed = feed.getEntries().iterator();
 
 				while (iFeed.hasNext()) {
 					RssNewsLinks link = new RssNewsLinks();
 					link.setFeedId(rssFeed.getId());
 					link.setRssFeed(rssFeed);
 					SyndEntry entry = (SyndEntry) iFeed.next();
 					link.setTitle(entry.getTitle());
 					link.setLink(entry.getLink());
 					link.setDescription(entry.getDescription().getValue()
 							.replaceAll("\\<.*?>", ""));
 					link.setGuid(entry.getUri().toString());
 					Date publicationDate = entry.getPublishedDate();
 					if (publicationDate == null) { // feed doesn't have
 						// published date
 						publicationDate = new Date();
 					}
 					DateTime publishedDate = new DateTime(publicationDate);
 					link.setPubDate(publishedDate);
 
 					rssLinks.add(link);
 
 				}
 				Set<RssNewsLinks> currentLinksInFeed = rssFeed
 						.getRssNewsLinks();
 				currentLinksInFeed.addAll(rssLinks);
 				rssFeed.setRssNewsLinks(currentLinksInFeed);
 				rssFeedsRepository.save(rssFeed);
 
 				logger.info("Saved " + rssFeed.getId());
 
 			} catch (final Exception ex) {
 				int rssFeedConnectFailures = rssFeed.getConnectFailures();
 				rssFeedConnectFailures += 1;
 				rssFeed.setConnectFailures(rssFeedConnectFailures);
 				this.rssFeedsRepository.save(rssFeed);
 				if (rssFeed.getConnectFailures() >= 24) { // change this number
 					// to adjust number
 					// of tries before
 					// disabling
 					rssFeed.setDisabled(true);
 					this.rssFeedsRepository.save(rssFeed);
 					this.logger.info("The following feed ID was just disabled for too many failed connect attempts: "
 							+ rssFeed.getId());
 					this.sendErrorEmail("The following feed ID was just disabled for too many failed connect attempts: "
 							+ rssFeed.getId());
 				}
 				this.logger.error("There was a parsing issue for: " + rssFeed.getId(), ex);
 			}
 
 		}
 
 	}
 
 }
