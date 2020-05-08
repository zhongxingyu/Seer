 package org.atlasapi.remotesite.seesaw;
 
 import java.util.Currency;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.atlasapi.genres.GenreMap;
 import org.atlasapi.media.TransportType;
 import org.atlasapi.media.entity.Countries;
 import org.atlasapi.media.entity.Encoding;
 import org.atlasapi.media.entity.Episode;
 import org.atlasapi.media.entity.Location;
 import org.atlasapi.media.entity.Policy;
 import org.atlasapi.media.entity.Publisher;
 import org.atlasapi.media.entity.Version;
 import org.atlasapi.media.entity.Policy.RevenueContract;
 import org.atlasapi.remotesite.ContentExtractor;
 import org.atlasapi.remotesite.html.HtmlNavigator;
 import org.jaxen.JaxenException;
 import org.jdom.Element;
 
 import com.google.common.collect.Sets;
 import com.metabroadcast.common.currency.Price;
 
 public class SeesawItemContentExtractor implements ContentExtractor<HtmlNavigator, Episode> {
     static final Log LOG = LogFactory.getLog(SeesawItemContentExtractor.class);
     private final Pattern poundsPricePattern = Pattern.compile(".*\\u00A3([0-9]+)\\.([0-9]{2})");
     private final Pattern pencePricePattern = Pattern.compile(".*([0-9]{2})p");
     private final Pattern seriesPattern = Pattern.compile("^.*Series (\\d+).*$");
     private final Pattern imagePattern = Pattern.compile("(/i/ccp/\\d+/\\d+.JPG)", Pattern.CASE_INSENSITIVE);
     private final GenreMap genreMap = new SeesawGenreMap();
     
     @Override
     public Episode extract(HtmlNavigator source) {
         try {
             Episode episode = new Episode();
             episode.setPublisher(Publisher.SEESAW);
             
             Version version = new Version();
             version.setProvider(Publisher.SEESAW);
             Encoding encoding = new Encoding();
             Location linkLocation = new Location();
             linkLocation.setTransportType(TransportType.LINK);
             linkLocation.setAvailable(true);
             linkLocation.setPolicy(ukPolicy());
             encoding.addAvailableAt(linkLocation);
             version.addManifestedAs(encoding);
             
             episode.addVersion(version);
             
             Element infoElem = source.firstElementOrNull("//div[@class='information']");
             List<Element> headers = source.allElementsMatching("h3", infoElem);
             
             String seriesText = null;
             String episodeText;
             String title = headers.get(0).getText();
             if (headers.size() > 1) {
                 if (headers.size() > 2) {
                     seriesText = headers.get(1).getText();
                     episodeText = headers.get(2).getText();
                 }
                 else {
                     episodeText = headers.get(1).getText();
                 }
                 
                 if (seriesText != null) {
                     Matcher matcher = seriesPattern.matcher(seriesText);
                     if (matcher.find()) {
                         episode.setSeriesNumber(Integer.valueOf(matcher.group(1)));
                     } else {
                         LOG.warn("Unable to parse series number: "+seriesText);
                     }
                     
                 }
                 if (episodeText.startsWith("Episode ")) {
                     try {
                         String numberString = episodeText.substring("Episode ".length());
                         
                         if (numberString.contains(":")) {
                             numberString = numberString.substring(0, numberString.indexOf(""));
                         }
                         
                         if (numberString.contains(" - ")) {
                             numberString = numberString.substring(0, numberString.indexOf(" - "));
                         }
                         
                         int episodeNumber = Integer.parseInt(episodeText.substring("Episode ".length(), 
                             episodeText.contains(":") ? episodeText.indexOf(":") : episodeText.length()));
                         episode.setEpisodeNumber(episodeNumber);
                     }
                     catch (NumberFormatException e) {
                         LOG.warn("Unable to parse episode number: "+episodeText, e);
                     }
                 }
                 
                 if (episodeText.contains(": ")) {
                     String episodeTitle = episodeText.substring(episodeText.indexOf(": ") + 2, episodeText.length());
                     episode.setTitle(episodeTitle);
                 } else if (episode.getEpisodeNumber() != null){
                     episode.setTitle("Episode "+episode.getEpisodeNumber());
                } else {
                    episode.setTitle(episodeText);
                 }
             }
             
             if (episode.getTitle() == null) {
                 episode.setTitle(title);
             }
             
             Element playerInfoElem = source.firstElementOrNull("//*[@class='programInfo']");
             if (playerInfoElem != null) {
                 String info = SeesawHelper.getAllTextContent(playerInfoElem);
                 Pattern pattern = Pattern.compile(".*\\((\\d+) mins\\).*", Pattern.DOTALL);
                 Matcher matcher = pattern.matcher(info);
                 if (matcher.matches()) {
                     try {
                         Integer duration = Integer.valueOf(matcher.group(1)) * 60;
                         version.setPublishedDuration(duration);
                     }
                     catch (NumberFormatException e) {
                         LOG.debug("Exception when trying to parse duration: ", e);
                     }
                 }
                 
             }
             
             Element programmeInfoElem = source.firstElementOrNull("//*[text()='About this programme:']/following-sibling::*", infoElem);
             if (programmeInfoElem != null) {
                 String progDesc = SeesawHelper.getFirstTextContent(programmeInfoElem).trim();
                 episode.setDescription(progDesc);
             }
             
             Element dateElem = source.firstElementOrNull("//*[text()='Date: ']/following-sibling::*", infoElem);
             if (dateElem != null) {
                 @SuppressWarnings("unused")
                 String date = SeesawHelper.getFirstTextContent(dateElem).trim();
             }
             
             Element categoryElem = source.firstElementOrNull("//*[text()='Categories: ']/following-sibling::*", infoElem);
             if (categoryElem != null) {
                 //String category = SeesawHelper.getFirstTextContent(categoryElem).trim();
                 String categoryLink = SeesawHelper.getFirstLinkUri(categoryElem);
                 
                 episode.setGenres(genreMap.map(Sets.newHashSet(categoryLink)));
             }
             
             Element externalLinksElem = source.firstElementOrNull("//*[text()='External Links']/following-sibling::*", infoElem);
             if (externalLinksElem != null) {
                 // TODO: use external links as aliases
                 @SuppressWarnings("unused")
                 List<String> links = SeesawHelper.getAllLinkUris(externalLinksElem);
             }
             
             List<Element> scriptElements = source.allElementsMatching("//script");
             for (Element scriptElement: scriptElements) {
                 Matcher matcher = imagePattern.matcher(scriptElement.getValue());
                 if (matcher.find()) {
                     episode.setImage("http://www.seesaw.com"+matcher.group(1));
                 }
             }
             
             Element priceElem = source.firstElementOrNull("//*[@id='episodePriceSpan']");
             if (priceElem != null) {
                 linkLocation.getPolicy().setRevenueContract(RevenueContract.PAY_TO_RENT);
                 
                 Integer amount = null;
                 Matcher poundsMatcher = poundsPricePattern.matcher(priceElem.getText());
                 Matcher penceMatcher = pencePricePattern.matcher(priceElem.getText());
                 if (poundsMatcher.matches()) {
                     amount = (Integer.valueOf(poundsMatcher.group(1)) * 100) + Integer.valueOf(poundsMatcher.group(2));
                 }
                 else if (penceMatcher.matches()) {
                     amount = Integer.valueOf(penceMatcher.group(1));
                 }
                 if (amount != null) {
                     linkLocation.getPolicy().setPrice(new Price(Currency.getInstance("GBP"), amount));
                 }
                 else {
                     LOG.debug("Could not find price of rentable content");
                 }
             }
             else {
                 linkLocation.getPolicy().setRevenueContract(RevenueContract.FREE_TO_VIEW);
             }
             
             return episode;
         } catch (JaxenException e) {
             LOG.warn("Error extracting seesaw item", e);
         }
         
         return null;
     }
     
     private Policy ukPolicy() {
         Policy policy = new Policy();
         policy.addAvailableCountry(Countries.GB);
         return policy;
     }
 }
