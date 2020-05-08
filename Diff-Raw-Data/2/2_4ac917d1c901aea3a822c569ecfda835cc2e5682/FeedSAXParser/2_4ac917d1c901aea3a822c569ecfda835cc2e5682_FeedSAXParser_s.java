 package uk.org.catnip.eddie.parser;
 
 import org.xml.sax.SAXParseException;
 import org.xml.sax.SAXException;
 import org.apache.log4j.Logger;
 import uk.org.catnip.eddie.Entry;
 import uk.org.catnip.eddie.Generator;
 import uk.org.catnip.eddie.Author;
 import uk.org.catnip.eddie.Link;
 import uk.org.catnip.eddie.Date;
 
 public class FeedSAXParser extends BaseSAXParser {
     static Logger log = Logger.getLogger(FeedSAXParser.class);
 
     private Author author;
 
     private Generator generator;
 
     private Link link;
 
     public void endElement_author() throws SAXException {
         in_author = false;
         pop("author");
         getCurrentContext().setAuthor(author);
         if (author.getName() != null && author.getEmail() != null
                 && !author.getEmail().equals("")) {
             getCurrentContext().set("author",
                     author.getName() + " (" + author.getEmail() + ")");
         } else {
             getCurrentContext().set("author", author.getName());
         }
         author = null;
     }
 
     public void endElement_content() throws SAXException {
         pop("content");
         current_entry.addContent(detail);
     }
 
     public void endElement_contributor() throws SAXException {
         in_author = false;
         pop("contributor");
         getCurrentContext().addContributor(author);
         author = null;
     }
 
     public void endElement_email() throws SAXException {
         String value = pop("email");
         if (in_author) {
             author.setEmail(value);
         } else if (in_contributor) {
             save_contributor("email", value);
         }
     }
 
     public void endElement_entry() throws SAXException {
         pop("entry");
         in_entry = false;
         feed.addEntry(current_entry);
         current_entry = null;
     }
 
     public void endElement_generator() throws SAXException {
         String content = pop("generator");
         generator.setDetails(detail);
        if (generator.getName() != null && !content.equals("")) {
             generator.setName(content);
         }
         feed.setGenerator(generator);
         generator = null;
     }
 
     public void endElement_id() throws SAXException {
         in_author = false;
         String id = pop("id");
         getCurrentContext().set("guid", id);
         author = null;
     }
 
     public void endElement_link() throws SAXException {
         pop("link");
         link.setDetails(detail);
         getCurrentContext().addLink(link);
         if ((link.getType().equals("text/html") || link.getType().equals(
                 "application/xhtml+xml"))
                 && (link.getRel() != null && link.getRel().equals("alternate"))) {
             getCurrentContext().set("link", link.getHref());
         }
         link = null;
     }
 
     public void endElement_name() throws SAXException {
         String value = pop("name");
         if (in_author) {
             author.setName(value);
         } else if (in_contributor) {
             save_contributor("name", value);
             // } else if (in_textinput){
             // FeedContext context = getCurrentContext();
             // context.getTextInput().set("name", value);
         }
 
     }
 
     public void endElement_tagline() throws SAXException {
         String content = pop("tagline");
         feed.set("tagline", content);
         feed.setTagline(detail);
     }
 
     public void endElement_title() throws SAXException {
         String content = pop("title");
         getCurrentContext().set("title", content);
         getCurrentContext().setTitle(detail);
         in_content--;
     }
     public void endElement_info() throws SAXException {
         String content = pop("info");
         getCurrentContext().set("info", content);
         feed.setInfo(detail);
         in_content--;
     }
     public void endElement_summary() throws SAXException {
         String content = pop("summary");
         getCurrentContext().set("summary", content);
         getCurrentContext().setSummary(detail);
         in_content--;
     }
     public void endElement_copyright() throws SAXException {
         String content = pop("copyright");
         feed.set("copyright", content);
         feed.setCopyright(detail);
         in_content--;
     }
     public void endElement_issued() throws SAXException {
         String content = pop("issued");
         getCurrentContext().set("issued", content);
         getCurrentContext().setIssued(new Date(content,detail));
     }
     public void endElement_created() throws SAXException {
         String content = pop("created");
         getCurrentContext().set("created", content);
         getCurrentContext().setCreated(new Date(content,detail));
     }
     public void endElement_modified() throws SAXException {
         String content = pop("modified");
         getCurrentContext().set("modified", content);
         getCurrentContext().setModified(new Date(content,detail));
     }
     public void endElement_url() throws SAXException {
 
         String value = pop("url");
         if (in_author) {
             author.setUrl(value);
         } else if (in_contributor) {
             save_contributor("url", value);
         } else if (in_image) {
             // TODO
         } else if (in_contributor) {
             // TODO
         }
     }
 
     public void save_contributor(String key, String value) {
         log.debug("save_contributors: not implemented");
     }
 
     public void startElement_author(State state) throws SAXException {
         in_author = true;
         state.expectingText = true;
         author = new Author();
         push(state);
     }
 
     public void startElement_contributor(State state) throws SAXException {
         in_author = true;
         state.expectingText = true;
         author = new Author();
         push(state);
     }
 
     public void startElement_entry(State state) throws SAXException {
         in_entry = true;
         current_entry = new Entry();
         state.expectingText = false;
         push(state);
     }
 
     public void startElement_feed(State state) throws SAXException {
 
         this.in_feed = true;
         String attr_version = state.getAttr("version");
         String attr_namespace = state.getAttr("xmlns");
         if (!this.feed.has("format"))
             if (attr_version == null || attr_version.equals("")) {
                 if (attr_namespace != null && attr_namespace.equals("http://www.w3.org/2005/Atom")) {
                     this.feed.set("format", "atom10");
                 } else {
                     this.feed.set("format", "atom");
                 }
             } else {
                 if (attr_version.equals("0.1")) {
                     this.feed.set("format", "atom01");
                 } else if (attr_version.equals("0.2")) {
                     this.feed.set("format", "atom02");
                 } else if (attr_version.equals("0.3")) {
                     this.feed.set("format", "atom03");
                 } else {
                     this.feed.set("format", "atom");
                 }
             }
         // Do some sanity checking
         if (!this.feed.has("format")) {
             throw new SAXParseException("Failed to detect Atom format",
                     this.locator);
         }
     }
 
     public void startElement_generator(State state) throws SAXException {
         generator = new Generator();
         generator.setName(state.getAttr("name"));
         generator.setUrl(state.getAttr("url"));
         generator.setVersion(state.getAttr("version"));
         state.expectingText = true;
         push(state);
     }
 
     public void startElement_link(State state) throws SAXException {
         link = new Link();
         link.setHref(state.getAttr("href"));
         link.setTitle(state.getAttr("title"));
         link.setRel(state.getAttr("rel"));
         state.expectingText = false;
         push(state);
     }
 
     public void startElement_title(State state) throws SAXException {
         in_content++;
         state.mode = state.getAttr("mode", "escaped");
         state.type = state.getAttr("type", "text/plain");
         state.expectingText = (this.in_feed || this.in_entry);
         push(state);
 
     }
     public void startElement_info(State state) throws SAXException {
         in_content++;
         state.mode = state.getAttr("mode", "escaped");
         state.type = state.getAttr("type", "text/plain");
         state.expectingText = true;
         push(state);
 
     }
 
     public void startElement_summary(State state) throws SAXException {
         in_content++;
         state.mode = state.getAttr("mode", "escaped");
         state.type = state.getAttr("type", "text/plain");
         state.expectingText = true;
         push(state);
 
     }
     public void startElement_copyright(State state) throws SAXException {
         in_content++;
         state.mode = state.getAttr("mode", "escaped");
         state.type = state.getAttr("type", "text/plain");
         state.expectingText = true;
         push(state);
 
     }
     public void startElement_url(State state) throws SAXException {
         state.expectingText = true;
         push(state);
     }
     public void startElement_issued(State state) throws SAXException {
         state.expectingText = true;
         push(state);
     }
     public void startElement_created(State state) throws SAXException {
         state.expectingText = true;
         push(state);
     }
     public void startElement_modified(State state) throws SAXException {
         state.expectingText = true;
         push(state);
     }
 }
