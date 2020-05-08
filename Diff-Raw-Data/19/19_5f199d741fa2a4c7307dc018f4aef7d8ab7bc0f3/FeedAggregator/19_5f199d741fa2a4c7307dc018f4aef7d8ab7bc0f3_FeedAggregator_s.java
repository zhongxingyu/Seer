 /*
  * Created on Saturday, May 14 2010 17:08
  */
 package com.jogamp.hungryharry;
 
 import com.jogamp.hungryharry.Config.Feed;
 import com.jogamp.hungryharry.Config.Planet;
 import com.sun.syndication.feed.synd.SyndContent;
 import com.sun.syndication.io.SyndFeedOutput;
 import com.sun.syndication.feed.synd.SyndEntry;
 import com.sun.syndication.io.FeedException;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 
 import com.sun.syndication.feed.synd.SyndFeedImpl;
 import com.sun.syndication.feed.synd.SyndFeed;
 import com.sun.syndication.fetcher.FeedFetcher;
 import com.sun.syndication.fetcher.impl.FeedFetcherCache;
 import com.sun.syndication.fetcher.impl.HashMapFeedInfoCache;
 import com.sun.syndication.fetcher.impl.HttpURLFeedFetcher;
 import freemarker.template.Configuration;
 import freemarker.template.ObjectWrapper;
 import freemarker.template.Template;
 import freemarker.template.TemplateException;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileWriter;
 import java.io.Writer;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.logging.Logger;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Unmarshaller;
 
 import static java.util.Collections.*;
 import static java.util.logging.Level.*;
 import static java.io.File.*;
 
 /**
  * Always hungry, always.
  *
  * @author Michael Bien
  *
  */
 public class FeedAggregator {
 
     private static final Logger LOG = Logger.getLogger(FeedAggregator.class.getName());
     private final String configFile;
 
     private FeedAggregator(String configFile) {
         this.configFile = configFile;
     }
 
     private void aggregate() {
 
         Config config = null;
         try {
             config = readConfiguration();
         } catch (JAXBException ex) {
             throw new RuntimeException("can not read configuration", ex);
         } catch (FileNotFoundException ex) {
             throw new RuntimeException("can not read configuration", ex);
         }
 
         List<Config.Feed> feeds = config.feed;
         List<SyndFeed> syndFeeds = new ArrayList<SyndFeed>();
         List<SyndEntry> entries = downloadFeeds(feeds, syndFeeds);
 
         Planet planet = config.planet;
         new File(planet.outputFolder).mkdirs();
 
         createAggregatedFeed(planet, entries);
 
         List<Map<String, Object>> aggregatedEntries = new ArrayList<Map<String, Object>>(entries.size());
         int n = 0;
 
         LOG.info(entries.size()+" entries found");
 
         for (SyndEntry entry : entries) {
             if(n++ >= planet.maxEntries) {
                 break;
             }
            SyndContent description = entry.getDescription();
             Map<String, Object> map = new HashMap<String, Object>();
             map.put("title", entry.getTitle());
             map.put("link", entry.getLink());
             map.put("author", entry.getAuthor());
             map.put("published", entry.getPublishedDate());
             map.put("updated", entry.getUpdatedDate());
            map.put("description", description);
             
             StringBuilder sb = new StringBuilder();
             List contents = entry.getContents();
             for (Object content : contents) {
                 sb.append(((SyndContent)content).getValue());
             }
            if(sb.length() == 0) {
                map.put("content", description.getValue());
             }else{
                 map.put("content", sb);
             }
 
             String link = entry.getLink();
             for (Config.Template template : config.template) {
                 if(link.contains(template.keyword)) {
                     Matcher matcher = Pattern.compile(template.idpattern).matcher(link);
                     matcher.find();
                     String playercode = template.text.replaceAll("#id#", matcher.group(1));
 
                     map.put("player", playercode);
 
                     String filteredDescription = null;
                     //TODO sometimes there is only content
                    if(description == null) {
                         filteredDescription = sb.toString();
                     }else{
                        filteredDescription = description.getValue();
                         if(template.descriptionfilter != null) {
                             Pattern descPattern = Pattern.compile(template.descriptionfilter);
                             Matcher filter = descPattern.matcher(filteredDescription);
                             filter.find();
                             filteredDescription = filter.group(1);
                         }
                     }
 
                     map.put("filteredDescription", filteredDescription);
 
                     break;
                 }
             }
             aggregatedEntries.add(map);
 
         }
 
         int max = planet.maxEntriesPerPage;
         int pages = (int) Math.ceil(aggregatedEntries.size() / (float)max);
         System.out.println(pages);
         for(int i = 0; i < pages; i++) {
             List<Map<String, Object>> subList = aggregatedEntries.subList(i * max, Math.min(i * max + max, aggregatedEntries.size()-1));
             generatePage(i, pages, syndFeeds, subList, planet);
         }
 
     }
 
     private void generatePage(int index, int pages, List<SyndFeed> syndFeeds, List<Map<String, Object>> entries, Planet planet) {
 
         String name = pageName(planet, index, pages);
 
         Map<String, Object> root = new HashMap<String, Object>();
         root.put("entries", entries);
         root.put("planet", planet);
         root.put("feedlinks", planet.feeds);
         root.put("feeds", syndFeeds);
         root.put("page", index+1);
         root.put("pages", pages);
         root.put("filename", name);
         root.put("updated", new Date());
         root.put("prev", pageName(planet, index-1, pages));
         root.put("next", pageName(planet, index+1, pages));
 
         try {
             String templateFolder = cutoffTail(planet.templatePath, '/');
             String templateName = planet.templatePath.substring(templateFolder.length());
 
             Configuration cfg = new Configuration();
             // Specify the data source where the template files come from.
             // Here I set a file directory for it:
             cfg.setDirectoryForTemplateLoading(new File(templateFolder));
             // Specify how templates will see the data-model. This is an advanced topic...
             // but just use this:
             cfg.setObjectWrapper(ObjectWrapper.DEFAULT_WRAPPER);
            cfg.setDefaultEncoding("UTF8");
 
             Template template = cfg.getTemplate(templateName);
             Writer writer = new FileWriter(new File(planet.outputFolder + separator + name));
             template.process(root, writer);
             writer.close();
         } catch (IOException ex) {
             LOG.log(SEVERE, null, ex);
         } catch (TemplateException ex) {
             LOG.log(SEVERE, null, ex);
         }
     }
 
     private String pageName(Planet planet, int index, int pages) {
         if(index < 0 || index >= pages) {
             return null;
         }
         return planet.name + (index == 0 ? "" : index) + ".html";
     }
 
     private void createAggregatedFeed(Planet planet, List<SyndEntry> entries) {
 
         for (Planet.PlanetFeed planetFeed : planet.feeds) {
             try {
                 SyndFeed feed = new SyndFeedImpl();
 
                 feed.setFeedType(planetFeed.type);
                 feed.setTitle(planet.title);
                 feed.setDescription(planet.description);
                 feed.setAuthor(planet.author);
                 feed.setLink(planet.link + separatorChar + planetFeed.getFileName());
                 feed.setEntries(entries);
 
                 SyndFeedOutput output = new SyndFeedOutput();
                 output.output(feed, new File(planet.outputFolder + separatorChar + planetFeed.getFileName()));
 
             } catch (IOException ex) {
                 LOG.log(SEVERE, null, ex);
             } catch (FeedException ex) {
                 LOG.log(SEVERE, null, ex);
             }
         }
     }
 
     private List<SyndEntry> downloadFeeds(List<Feed> feeds, List<SyndFeed> list) throws IllegalArgumentException {
 
         FeedFetcherCache feedInfoCache = HashMapFeedInfoCache.getInstance();
         FeedFetcher feedFetcher = new HttpURLFeedFetcher(feedInfoCache);
         List<SyndEntry> collectedEntries = new ArrayList<SyndEntry>();
 
         for (Config.Feed feed : feeds) {
             LOG.info("downloading "+feed);
             try {
                 SyndFeed inFeed = feedFetcher.retrieveFeed(new URL(feed.url));
                 list.add(inFeed);
                 List entries = inFeed.getEntries();
 
                 LOG.info("downloaded "+entries.size()+ " entries");
                 collectedEntries.addAll(entries);
             } catch (Exception ex) {
                 LOG.log(WARNING, "skipping feed", ex);
             }
         }
 
         sort(collectedEntries, new Comparator<SyndEntry>() {
             @Override
             public int compare(SyndEntry o1, SyndEntry o2) {
                 return o2.getPublishedDate().compareTo(o1.getPublishedDate());
             }
         });
         return collectedEntries;
     }
 
     private Config readConfiguration() throws JAXBException, FileNotFoundException {
         Unmarshaller unmarshaller = JAXBContext.newInstance(Config.class).createUnmarshaller();
         LOG.info("reading config file: " + configFile);
         InputStream is = getClass().getResourceAsStream(configFile);
         if(is == null) {
             is = new FileInputStream(configFile);
         }
 
         Object obj = unmarshaller.unmarshal(is);
         return (Config) obj;
     }
 
     private String cutoffTail(String text, char cut) {
         return text.substring(0, text.lastIndexOf(cut));
     }
 
     private StringBuilder replace(StringBuilder sb, String token, String replacement) {
         int start = sb.indexOf(token);
         sb.replace(start, start+token.length(), replacement);
         return sb;
     }
 
     public static void main(String... args) {
 
         if(args.length < 1) {
             System.out.println("args must contain a path to the configuration file");
             return;
         }
 
         new FeedAggregator(args[0]).aggregate();
     }
 
 }
