 package uk.org.catnip.eddie;
 
 import java.io.*;
 import java.util.regex.*;
 import java.util.Iterator;
 import java.util.Calendar;
 import org.apache.log4j.Logger;
 import uk.org.catnip.eddie.parser.Parser;
 import org.python.util.PythonInterpreter;
 import org.python.core.*;
 
 public class Test {
     public class EddieDict extends PyDictionary {
         //Logger log = Logger.getLogger(EddieDict.class);
         //private PyObject map(PyObject key) {
            // PyString str_key = (PyString) key;
            // if (str_key.__cmp__(new PyString("url")) == 0) {
            //     key = new PyString("href");
            // }
             //log.debug("key = '"+key+"'");
         //   return key;
         //}
         
         //public PyObject __finditem__(PyObject key) {
         //    return super.__finditem__(map(key));
         //}
         //public void __setitem__(PyObject key, PyObject value) {
         //    super.__setitem__(map(key),value);
         //}
         //public void __delitem__(PyObject key) {
         //    super.__delitem__(map(key));
         //}
     }
     
     static Logger log = Logger.getLogger(Test.class);
 
     private PythonInterpreter interp = new PythonInterpreter();
 
     public int total_tests = 0;
 
     public int failed_tests = 0;
 
     public int passed_tests = 0;
 
     public void parse_dir(String dirname) {
 
         File file_or_dir = new File(dirname);
         if (file_or_dir.isFile()) {
             test(dirname);
 
         } else {
             String[] dir = file_or_dir.list();
             java.util.Arrays.sort(dir);
             for (int i = 0; i < dir.length; i++) {
                 if (!dir[i].equals(".svn")) {
 
                     File dir_entry = new File(dirname + "/" + dir[i]);
                     if (dir_entry.isDirectory()) {
                         System.out.println("descending into " + dir[i]);
                         parse_dir(dirname + "/" + dir[i]);
                         System.out.println();
                     } else if (dir_entry.isFile()) {
 
                         test(dirname + "/" + dir[i]);
                     }
                 }
             }
         }
 
     }
 
     public boolean test(String filename) {
         total_tests++;
 
         boolean ok = false;
 
         try {
 
             BufferedReader in = new BufferedReader(new FileReader(filename));
             String line;
             String description = "";
             String test = "";
             Pattern desc_pattern = Pattern.compile("^Description: (.*)$");
             Pattern test_pattern = Pattern.compile("^Expect: (.*)$");
 
             while ((line = in.readLine()) != null) {
                 Matcher desc_matcher = desc_pattern.matcher(line);
                 Matcher test_matcher = test_pattern.matcher(line);
                 if (desc_matcher.matches()) {
                     description = desc_matcher.group(1);
                 } else if (test_matcher.matches()) {
                     test = test_matcher.group(1);
                 }
             }
             in.close();
             if (log.isDebugEnabled()) {
                 log.debug(filename);
                 log.debug("Description: " + description);
                 log.debug("Test: " + test);
             }
             Parser parser = new Parser();
             Feed feed = parser.parse(filename);
             if (log.isDebugEnabled()) {
                 log.debug(feed);
             }
             runPython(feed, test);
             if (log.isInfoEnabled()) {
                 log.info("passed: "+ filename);
             } else {
                 System.out.print(".");
             }
             ok = true;
             passed_tests++;
         } catch (Exception ex) {
             if (log.isDebugEnabled()) {
                 log.debug("****************************************");
                 log.debug("parse failed: ", ex);
             } else if (log.isInfoEnabled()){
                 log.info("failed: " + filename);
             } else {
                 System.out.print("x");
             }
             
             failed_tests++;
         }
         return ok;
 
     }
 
     public void runPython(Feed feed, String test) throws Exception {
 
         if (feed.error) {
             interp.set("bozo", new PyInteger(1));
         } else {
             interp.set("bozo", new PyInteger(0));
         }
 
         PyList entries_list = new PyList();
         Iterator entries = feed.entries();
         while (entries.hasNext()) {
             entries_list.append(convertEntry((Entry) entries.next()));
         }
         if (feed.get("format") != null) {
         interp.set("version", new PyString(feed.get("format")));
         }
         interp.set("feed", convertFeed(feed));
         interp.set("entries", entries_list);
         interp.exec("ret ="+ test);
         PyInteger ret = (PyInteger)interp.get("ret");
         if (ret.getValue() == 0) { 
             throw new Exception("test failed");
           
         }
 
     }
     public PyDictionary convertFeed(Feed feed) {
         PyDictionary feed_dict = new PyDictionary();
         Iterator it = feed.keys();
         while (it.hasNext()) {
             String key = (String) it.next();
             feed_dict.__setitem__(key, new PyString(feed.get(key)));
         }
         if (feed.getAuthor() != null) {
             feed_dict.__setitem__("author_detail", convertAuthor(feed.getAuthor()));
         }
         if (feed.getPublisher() != null) {
             feed_dict.__setitem__("publisher_detail", convertAuthor(feed.getPublisher()));
         }
         if (feed.getTitle() != null) {
             feed_dict.__setitem__("title_detail",convertDetail(feed.getTitle()));
         }
         if (feed.getInfo() != null) {
             feed_dict.__setitem__("info_detail",convertDetail(feed.getInfo()));
         }
         if (feed.getTagline() != null) {
             feed_dict.__setitem__("tagline_detail",convertDetail(feed.getTagline()));
         }
         if (feed.getGenerator() != null) {
             feed_dict.__setitem__("generator_detail",convertGenerator(feed.getGenerator()));
         }
         if (feed.getCopyright() != null) {
             feed_dict.__setitem__("copyright_detail",convertDetail(feed.getCopyright()));
         }
         if (feed.getCreated() != null) {
             feed_dict.__setitem__("created_parsed",convertDate(feed.getCreated()));
         }
         if (feed.getIssued() != null) {
             feed_dict.__setitem__("issued_parsed",convertDate(feed.getIssued()));
         }
         if (feed.getModified() != null) {
             feed_dict.__setitem__("modified_parsed",convertDate(feed.getModified()));
             feed_dict.__setitem__("date_parsed",convertDate(feed.getModified()));
         }
         if (feed.getImage() != null) {
             feed_dict.__setitem__("image",convertImage(feed.getImage()));
         }
         if (feed.getTextinput() != null) {
             feed_dict.__setitem__("textinput",convertTextInput(feed.getTextinput()));
         }
         PyList contributors_list = new PyList();
         Iterator contributors = feed.contributors();
         while (contributors.hasNext()) {
             contributors_list.append(convertAuthor((Author)contributors.next()));
         }
         feed_dict.__setitem__("contributors",contributors_list);
         
         
         PyList links_list = new PyList();
         Iterator links = feed.links();
         while (links.hasNext()) {
             links_list.append(convertLink((Link)links.next()));
         }
         feed_dict.__setitem__("links",links_list);
         
         PyList category_list = new PyList();
         Iterator categories = feed.categories();
         while (categories.hasNext()) {
             category_list.append(convertCategoryTuple((Category)categories.next()));
         }
         feed_dict.__setitem__("categories",category_list);
       
         log.debug(feed_dict);
         
         return feed_dict;
         
         
         
     }
     public PyDictionary convertAuthor(Author author) {
         PyDictionary author_detail = new EddieDict();
         author_detail.__setitem__("name", new PyString(author.getName()));
         author_detail.__setitem__("email", new PyString(author.getEmail()));
         author_detail.__setitem__("url", new PyString(author.getHref()));
         return author_detail;
     }
     
 
     
     
     public PyDictionary convertEntry(Entry entry) {
         PyDictionary entry_dict = new PyDictionary();
         Iterator entry_it = entry.keys();
         while (entry_it.hasNext()) {
             String key = (String) entry_it.next();
             entry_dict.__setitem__(key, new PyString(entry.get(key)));
         }
         if (entry.getAuthor() != null) {
             entry_dict.__setitem__("author_detail", convertAuthor(entry.getAuthor()));
         }
         if (entry.getPublisher() != null) {
             entry_dict.__setitem__("publisher_detail", convertAuthor(entry.getPublisher()));
         }
         if (entry.getTitle() != null) {
             entry_dict.__setitem__("title_detail",convertDetail(entry.getTitle()));
         }
         if (entry.getSummary() != null) {
             entry_dict.__setitem__("summary_detail",convertDetail(entry.getSummary()));
         }
         if (entry.getCreated() != null) {
             entry_dict.__setitem__("created_parsed",convertDate(entry.getCreated()));
         }
         if (entry.getIssued() != null) {
             entry_dict.__setitem__("issued_parsed",convertDate(entry.getIssued()));
         }
         if (entry.getModified() != null) {
             entry_dict.__setitem__("modified_parsed",convertDate(entry.getModified()));
             entry_dict.__setitem__("date_parsed",convertDate(entry.getModified()));
         }
         
         PyList contents_list = new PyList();
         Iterator contents = entry.contents();
         while (contents.hasNext()) { 
             contents_list.append(convertDetail((Detail)contents.next()));
         }
         entry_dict.__setitem__("content",contents_list);
         
         PyList contributors_list = new PyList();
         Iterator contributors = entry.contributors();
         while (contributors.hasNext()) {
             contributors_list.append(convertAuthor((Author)contributors.next()));
         }
         entry_dict.__setitem__("contributors",contributors_list);
         
         PyList links_list = new PyList();
         Iterator links = entry.links();
         while (links.hasNext()) {
             links_list.append(convertLink((Link)links.next()));
         }
         entry_dict.__setitem__("links",links_list);
         
         // Tuple style categories
         PyList category_list = new PyList();
         Iterator categories = entry.categories();
         while (categories.hasNext()) {
             category_list.append(convertCategoryTuple((Category)categories.next()));
         }
         entry_dict.__setitem__("categories",category_list);
         
         // Dict style tags
         category_list = new PyList();
         categories = entry.categories();
         while (categories.hasNext()) {
             category_list.append(convertCategory((Category)categories.next()));
         }
         entry_dict.__setitem__("tags",category_list);
         
         
         
         PyList enclosure_list = new PyList();
         Iterator enclosures = entry.enclosures();
         while (enclosures.hasNext()) {
             enclosure_list.append(convertEnclosure((Enclosure)enclosures.next()));
         }
         entry_dict.__setitem__("enclosures",enclosure_list);
         
         if (entry.isGuidIsLink()) {
             entry_dict.__setitem__("guidislink", new PyInteger(1));
         } else {
             entry_dict.__setitem__("guidislink", new PyInteger(0));
         }
         
         log.debug(entry_dict);
         
         return entry_dict;
     }
     
     public PyDictionary convertDetail(Detail detail) {
 
         PyDictionary detail_dict = new PyDictionary();
         if (detail != null) {
             if (detail.getLanguage() != null) {
                 detail_dict.__setitem__("language", new PyString(detail
                         .getLanguage()));
             }
             if (detail.getType() != null) {
                 detail_dict.__setitem__("type", new PyString(detail.getType()));
             }
             if (detail.getValue() != null) {
                 detail_dict.__setitem__("value",
                         new PyString(detail.getValue()));
             }
             if (detail.getSrc() != null) {
                 detail_dict.__setitem__("src",
                         new PyString(detail.getSrc()));
             }
         }
         return detail_dict;
     }
     public PyDictionary convertLink(Link link) {
         PyDictionary link_dict =  convertDetail(link);
         if (link.getHref() != null) {
         link_dict.__setitem__("href", new PyString(link.getHref()));
         }
         if (link.getTitle() != null) {
         link_dict.__setitem__("title", new PyString(link.getTitle()));
         }
         if (link.getRel() != null) {
         link_dict.__setitem__("rel", new PyString(link.getRel()));
         }
         if (link.getHreflang() != null) {
             link_dict.__setitem__("hreflang", new PyString(link.getHreflang()));
             }
         if (link.getLength() != null) {
             link_dict.__setitem__("length", new PyString(link.getLength()));
             }
         return link_dict;
     }
     public PyDictionary convertGenerator(Generator generator) {
         PyDictionary link_dict = convertDetail(generator);
     
     if (generator.getName() != null) {
         link_dict.__setitem__("name", new PyString(generator.getName()));
     }
     if (generator.getUrl() != null) {
         link_dict.__setitem__("url", new PyString(generator.getUrl()));
     }
     if (generator.getVersion() != null) {
         link_dict.__setitem__("version", new PyString(generator.getVersion()));
     }
         return link_dict;
     }
     public PyTuple convertDate(Date date) {
         PyTuple date_tuple;;
     
     if (date.getDate() != null) {
         Calendar cal = date.getDate();
         PyObject [] fields = {
                 new PyInteger(cal.get(Calendar.YEAR)),
                 new PyInteger(cal.get(Calendar.MONTH)+1),
                 new PyInteger(cal.get(Calendar.DAY_OF_MONTH)),
                 new PyInteger(cal.get(Calendar.HOUR_OF_DAY)),
                 new PyInteger(cal.get(Calendar.MINUTE)),
                 new PyInteger(cal.get(Calendar.SECOND)),
                 new PyInteger(Date.normaliseDayOfWeek(cal.get(Calendar.DAY_OF_WEEK))),
                 new PyInteger(cal.get(Calendar.DAY_OF_YEAR)),
                 new PyInteger(0)
         };
         date_tuple = new PyTuple(fields);
 
     } else {
         date_tuple = new PyTuple();
     }
 
         return date_tuple;
     }
     public PyDictionary convertCategory(Category category) {
         PyDictionary tags_dict = new PyDictionary();
 
         if (category.getTerm() != null) {
             tags_dict.__setitem__("term", new PyString(category.getTerm()));
         }
         if (category.getSchedule() != null) {
             tags_dict.__setitem__("scheme", new PyString(category.getSchedule()));
         }
         if (category.getLabel() != null) {
             tags_dict.__setitem__("label", new PyString(category.getLabel()));
         }
 
         return tags_dict;
 
     }
     public PyTuple convertCategoryTuple(Category category) {
         String term = category.getTerm();
         String schedule = category.getSchedule();
         String label = category.getLabel();
         
         if (term == null) { term = ""; }
         if (schedule == null) { schedule = ""; }
         if (label == null) { label = ""; }
         
         PyObject[] fields = { new PyString(schedule),
                 new PyString(term),
                 new PyString(label) };
         return new PyTuple(fields);
 
     }
     
     public PyDictionary convertImage(Image image) {
         PyDictionary image_dict = new PyDictionary();
 
         if (image.getTitle() != null) {
             image_dict.__setitem__("title", new PyString(image.getTitle()));
         }
         if (image.getUrl() != null) {
             image_dict.__setitem__("url", new PyString(image.getUrl()));
         }
         if (image.getLink() != null) {
             image_dict.__setitem__("link", new PyString(image.getLink()));
         }
         if (image.getWidth() != null) {
             image_dict.__setitem__("width", new PyInteger(Integer.parseInt(image.getWidth())));
         }
         if (image.getHeight() != null) {
             image_dict.__setitem__("height", new PyInteger(Integer.parseInt(image.getHeight())));
         }
         if (image.getDescription() != null) {
             image_dict.__setitem__("description", new PyString(image
                     .getDescription()));
         }
 
         return image_dict;
     }
     public PyDictionary convertTextInput(TextInput textinput) {
         PyDictionary textinput_dict = new PyDictionary();
 
         if (textinput.getTitle() != null) {
             textinput_dict.__setitem__("title", new PyString(textinput.getTitle()));
         }
         if (textinput.getLink() != null) {
             textinput_dict.__setitem__("link", new PyString(textinput.getLink()));
         }
         if (textinput.getName() != null) {
             textinput_dict.__setitem__("name", new PyString(textinput.getName()));
         }
         if (textinput.getDescription() != null) {
             textinput_dict.__setitem__("description", new PyString(textinput
                     .getDescription()));
         }
 
         return textinput_dict;
     }
     
     public PyDictionary convertEnclosure(Enclosure enclosure) {
         PyDictionary enclosure_dict = new PyDictionary();
 
         if (enclosure.getUrl() != null) {
             enclosure_dict.__setitem__("url", new PyString(enclosure.getUrl()));
            enclosure_dict.__setitem__("href", new PyString(enclosure.getUrl()));
         }
         if (enclosure.getLength() != null) {
             enclosure_dict.__setitem__("length", new PyString(enclosure.getLength()));
         }
         if (enclosure.getType() != null) {
             enclosure_dict.__setitem__("type", new PyString(enclosure.getType()));
         }
 
         return enclosure_dict;
     }
 }
