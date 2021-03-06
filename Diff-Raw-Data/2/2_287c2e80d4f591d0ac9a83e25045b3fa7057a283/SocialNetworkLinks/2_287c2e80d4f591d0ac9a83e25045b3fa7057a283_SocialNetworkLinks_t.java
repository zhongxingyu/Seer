 package com.zestia.rest.capsule.restapi;
 
 import com.google.common.base.CharMatcher;
 import com.google.common.collect.Sets;
 import com.zestia.capsule.restapi.*;
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 import org.junit.Test;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import play.libs.WS;
 
 import java.io.IOException;
 import java.nio.charset.IllegalCharsetNameException;
 import java.util.Set;
 import java.util.concurrent.TimeUnit;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import static play.test.Helpers.running;
 
 /**
  * Integration tests or demo's that serve a greater purpose.
  * <ul>
  *     <li>Skype: creates Skype social network links from phone numbers of your contacts (ignores fax numbers and deletes duplicate numbers)</li>
  *     <li>Twitter: creates Twitter social network lniks by visiting your contacts' websites and looking for a screen name</li>
  * </ul>
  *
  * @author Mathias Bogaert
  */
 public class SocialNetworkLinks extends CapsuleTest {
     // TODO logging isn't working in Play 2.0 unit tests, find a solution
     private static final Logger logger = LoggerFactory.getLogger("application");
 
     @Test
     public void addSkypeLinks() throws Exception {
         running(fakeApplication(), new Runnable() {
             public void run() {
                 logger.info("Listing all parties...");
                 CParties parties = CParty.listAll(5, TimeUnit.SECONDS).get(50l, TimeUnit.SECONDS);
 
                 System.out.println("Found " + parties.size + " parties, adding Skype links from phone numbers...");
 
                 for (CParty party : parties) {
                     Set<String> phoneNumbers = Sets.newHashSet();
                     Set<String> skypeNumbers = Sets.newHashSet();
 
                     for (CContact contact : party.contacts) {
                         if (contact instanceof CPhone) {
                             CPhone phone = (CPhone) contact;
 
                             if (!"Fax".equals(phone.type)) {
                                 String phoneNumber = CharMatcher.WHITESPACE.removeFrom(((CPhone) contact).phoneNumber);
 
                                 if (phoneNumbers.contains(phoneNumber)) {
                                     System.out.println("CParty " + party + " has duplicate phone number (" + ((CPhone) contact).phoneNumber + "). Deleting.");
                                     party.deleteContact(contact); // remove dup
                                 } else {
                                     phoneNumbers.add(CharMatcher.WHITESPACE.removeFrom(((CPhone) contact).phoneNumber));
                                 }
                             }
                         }
 
                         if (contact instanceof CWebsite) {
                             if ("SKYPE".equals(((CWebsite) contact).webService)) {
                                 String skypeNumber = ((CWebsite) contact).webAddress;
 
                                 if (skypeNumbers.contains(skypeNumber)) {
                                     System.out.println("CParty " + party + " has duplicate Skype number (" + ((CWebsite) contact).webAddress + "). Deleting.");
                                     party.deleteContact(contact); // remove dup
                                 } else {
                                     skypeNumbers.add(skypeNumber);
                                 }
                             }
                         }
                     }
 
                     boolean save = false;
                     for (String phoneNumber : phoneNumbers) {
                         if (!skypeNumbers.contains(phoneNumber)) {
                             CWebsite website = new CWebsite(null, phoneNumber, "SKYPE");
                             party.addContact(website);
 
                             save = true;
                         }
                     }
 
                     if (save) {
                         System.out.println("Saving " + party);
 
                         WS.Response response = party.save().get();
                         if (response.getStatus() < 200 || response.getStatus() > 206) {
                             System.out.println("Failure saving party " + party + ", response " + response.getStatus() + " " + response.getStatusText());
                         }
                         else {
                             System.out.println("Success saving party " + party + ", response " + response.getStatus() + " " + response.getStatusText());
                         }
                     }
                 }
             }
         });
     }
 
     @Test
     public void addTwitterLinks() throws InterruptedException {
         running(fakeApplication(), new Runnable() {
             public void run() {
                 logger.info("Listing all parties...");
                 CParties parties = CParty.listAll(5, TimeUnit.SECONDS).get(50l, TimeUnit.SECONDS);
 
                 logger.info("Found " + parties.size + " parties, finding and adding Twitter links...");
                 for (CParty party : parties) {
                     boolean hasTwitterLink = false;
 
                     for (CContact contact : party.contacts) {
                         if (contact instanceof CWebsite) {
                             CWebsite website = (CWebsite) contact;
 
                             if ("TWITTER".equals(website.webService)) {
                                 logger.info("Skipping " + party + " since it already has a twitter link.");
 
                                 hasTwitterLink = true;
                             }
                         }
                     }
 
                     if (!hasTwitterLink) {
                         Set<String> twitterUsers = Sets.newTreeSet(String.CASE_INSENSITIVE_ORDER);
 
                         for (CContact contact : party.contacts) {
                             if (contact instanceof CWebsite) {
                                 CWebsite website = (CWebsite) contact;
 
                                 if ("URL".equals(website.webService) && !website.url.contains("google")) {
                                     System.out.println("Visiting website of " + party.getName() + " at " + website.url);
 
                                     try {
                                         Document doc = Jsoup.connect(website.url)
                                                 .ignoreHttpErrors(true)
                                                 .get();
 
                                         Elements links = doc.select("a[href]");
                                         for (Element link : links) {
                                             String href = link.attr("href");
 
                                             if (!href.contains("/search")
                                                     && !href.contains("/share")
                                                     && !href.contains("/home")
                                                     && !href.contains("/intent")) {
 
                                                 Matcher matcher = Pattern.compile("(www\\.)?twitter\\.com/(#!/)?@?([^/]*)").matcher(href);
                                                 while (matcher.find()) {
                                                     String twitterUser = CharMatcher.WHITESPACE.trimFrom(href.substring(matcher.start(3), matcher.end(3)));
 
                                                     if (!"".equals(twitterUser)) {
                                                         twitterUsers.add(twitterUser);
                                                     }
                                                 }
                                             }
                                         }
                                     } catch (IllegalCharsetNameException e) { // see https://github.com/jhy/jsoup/commit/2714d6be6cbe465b522a724c2796ddf74df06482#-P0
                                         logger.info("Illegal charset name for " + website + " of " + party);
                                     } catch (IOException e) {
                                         logger.info("Unable to GET " + website + " of " + party);
                                     }
                                 }
                             }
                         }
 
                         for (String twitterUser : twitterUsers) {
                             logger.info("Found twitter user @" + twitterUser + ", adding it to " + party.getName() + " and saving...");
 
                             CWebsite twitterLink = new CWebsite(null, twitterUser, "TWITTER");
                             party.addContact(twitterLink);
 
                             WS.Response response = party.save().get();
                            if (response.getStatus() < 200 || response.getStatus() > 206) {
                                 logger.info("Failure saving party " + party + ", response " + response.getStatusText());
                             }
                         }
                     }
                 }
             }
         });
     }
 }
