 package webmenu.crawler;
 
 import java.io.*;
 import java.util.*;
 import java.util.logging.Logger;
 import java.util.regex.*;
 import nu.validator.htmlparser.common.*;
 import nu.validator.htmlparser.xom.*;
 import nu.xom.*;
 import webmenu.model.*;
 import static webmenu.crawler.ParserUtil.normalizeText;
 
 public class SportCafeParser implements Parser {
     final static HtmlBuilder builder = new HtmlBuilder(XmlViolationPolicy.ALTER_INFOSET);
     final static XPathContext xpathContext = new XPathContext("x", "http://www.w3.org/1999/xhtml");
 
     private static final Logger log = Logger.getLogger(SportCafeParser.class.getName());
 
    final static Pattern DayNamePattern = Pattern.compile("^images/menu_pro_tento_tyden/([^/]+).png$");
     final static Pattern MealNamePattern = Pattern.compile("^\\d+\\)\\s+(.*[^,])[,]?$");
     final static Pattern PricePattern = Pattern.compile("^(\\d+),-$");
 
     static Calendar getStartDate(Calendar now) {
         int dayInWeek = now.get(Calendar.DAY_OF_WEEK);
         now.add(Calendar.DAY_OF_MONTH, -(dayInWeek - Calendar.MONDAY + 7) % 7);
         return new GregorianCalendar(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH));
     }
 
     Nodes parseMenuNodes(Document doc) throws CrawlException {
         String xpath = "/x:html/x:body/x:table/x:tbody/x:tr[1]/x:td[1]/x:div";
         Nodes nodes = doc.query(xpath, xpathContext);
         if (nodes.size() != 5)
             throw new CrawlException("Cannot parse menu nodes: nodes.size() is " + nodes.size());
         return nodes;
     }
 
     public String parseDayName(Node menuDiv) throws CrawlException
     {
         Nodes nodes = menuDiv.query("x:p[1]/x:img/@src", xpathContext);
         if (nodes.size() != 1)
         {
             StringBuffer msg = new StringBuffer("Malformed day div: p[1] has " + nodes.size() + " images.");
             for (int nix = 0; nix < nodes.size(); nix++)
                 msg.append("\t\t--image" + nix + ":src--\n" + nodes.get(nix).toString());
             log.warning(msg.toString());
             throw new CrawlException("Malformed day div.");
         }
 
         String src = nodes.get(0).getValue();
         Matcher m = DayNamePattern.matcher(src);
         if (!m.matches())
             throw new CrawlException("Malformed day-name img-src: '" + src + "'");
 
         return m.group(1);
     }
 
     public String parseSoupName(Node menuDiv) throws CrawlException
     {
         Nodes nodes = menuDiv.query("x:p[2]", xpathContext);
         if (nodes.size() != 1)
         {
             throw new CrawlException("Cannot parse soup name - there is no p[2]");
         }
 
         return normalizeText(nodes.get(0).getValue());
     }
 
     public int parseMenuItemCount(Node menuDiv)
     {
         Nodes paras = menuDiv.query("x:p", xpathContext);
         return paras.size() < 2 ? 0 : paras.size() - 2;
     }
 
     public MenuItem parseMenuItem(Node menuDiv, int item) throws CrawlException
     {
         Nodes itemNodes = menuDiv.query("x:p[" + (item+3) + "]", xpathContext);
         if (itemNodes.size() != 1)
         {
             log.warning("\tCannot parse menu item " + (item+1) + " - no para found.\n\t\t--menuDiv--\n" + menuDiv.toXML() + "\n\t\t--end--");
             return null;
         }
         Node para = itemNodes.get(0);
         if (para.getChildCount() != 3)
         {
             log.warning("\tCannot parse menu item " + (item+1) + " - para has wrong number of children: " + para.getChildCount()
                     + "\n\t\t--para--\n" + para.toXML() + "\n\t\t--end--");
             return null;
         }
 
         Node br = para.getChild(1);
         if (!(br instanceof Element))
         {
             log.warning("\tCannot parse menu item " + (item+1) + " - para/*[2] is not an element.\n\t\t--para--\n" + para.toXML() + "\n\t\t--end--");
             return null;
         }
 
         String name = normalizeText(para.getChild(0).getValue());
         String priceString = normalizeText(para.getChild(2).getValue());
         
         Matcher m = MealNamePattern.matcher(name);
         if (!m.matches())
             throw new CrawlException("Malformed meal name: '" + name + "'");
         name = m.group(1);
 
         m = PricePattern.matcher(priceString);
         if (!m.matches())
             throw new CrawlException("Malformed menu-item price: '" + priceString + "'");
         int price = -1;
         try {
             price = Integer.parseInt(m.group(1));
         } catch (NumberFormatException e) {
             throw new CrawlException("Cannot parse price '" + m.group(1) + "' as integer");
         }
 
         return new MenuItem("Menu " + (item +1), name, price);
     }
 
     public OneDayMenu[] parse(InputStream source) throws CrawlException {
         try {
             Document doc = builder.build(source);
             Nodes days = parseMenuNodes(doc);
             Calendar start = getStartDate(Calendar.getInstance());
 
             OneDayMenu[] retval = new OneDayMenu[days.size()];
             for (int d=0; d<days.size(); d++, start.add(Calendar.DAY_OF_MONTH, 1))
             {
                 Node div = days.get(d);
                 String dayName = parseDayName(div);
                 log.fine("Parsing day '" + dayName + "'");
 
                 int itemCount = parseMenuItemCount(div);
                 if (itemCount == 0)
                 {
                     retval[d] = new OneDayMenu(start.getTime(), null, null);
                     continue;
                 }
 
                 List<SoupItem> soups = new ArrayList<SoupItem>(1);
                 String soupName = parseSoupName(div);
                 log.fine("\tsoup: " + soupName);
                 soups.add(new SoupItem("Polévka", soupName));
     
                 List<MenuItem> meals = new ArrayList<MenuItem>(itemCount);
                 for (int i=0; i<itemCount; i++) {
                     MenuItem item = parseMenuItem(div, i);
                     log.fine("\t" + item.getName() + ": " + item.getMeal());
                     meals.add(item);
                 }
 
                 retval[d] = new OneDayMenu(start.getTime(), soups, meals);
             }
 
             return retval;
         } catch (XPathException e) {
             throw new CrawlException("Problem in parsing XPath", e);
         } catch (ParsingException e) {
             throw new CrawlException("Cannot parse HTML document", e);
         } catch (IOException e) {
             throw new CrawlException("Cannot read HTML document", e);
         }
     }
 }
