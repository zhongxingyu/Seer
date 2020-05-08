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
 
 public class MamHladHkParser implements Parser
 {
     final static Pattern MenuPricePattern = Pattern.compile("^Menu (\\d): (\\d+),- Kč$");
    final static Pattern StartDatePattern = Pattern.compile("^(\\d+)\\.(\\d+)\\. - (\\d+)\\.(\\d+)\\.(\\d+) od .*$");
     final static Pattern MenuNumberPattern = Pattern.compile("^(\\d)\\.\\s*$");
 
     final static HtmlBuilder builder = new HtmlBuilder(XmlViolationPolicy.ALTER_INFOSET);
     final static XPathContext xpathContext = new XPathContext("x", "http://www.w3.org/1999/xhtml");
 
     private static final Logger log = Logger.getLogger(MamHladHkParser.class.getName());
 
     int[] parsePrices(Document doc) throws CrawlException
     {
         int[] prices = new int[3];
 
         for (int ix = 0; ix < 3; ix++)
         {
             String menuNumber = String.valueOf(ix+1);
             String xpath = "//x:div[@id='main']/x:div[@class='columnright']/x:div[@class='graybox']//x:span[@class='menu" + menuNumber + "']";
             Nodes nodes = doc.query(xpath, xpathContext);
 
             if (nodes.size() != 1)
             {
                 log.info("Cannot parse price for class menu" + menuNumber + " : nodes.size() is " + nodes.size());
                 continue;
             }
 
             String text = nodes.get(0).getValue();
 
             Matcher m = MenuPricePattern.matcher(text);
             if (!m.matches())
                 throw new CrawlException("Malformed price text: '" + text + "'");
 
             int priceIndex = ix;
             String num = m.group(1);
             if (!num.equals(menuNumber))
             {
                 log.info("Class menu" + menuNumber + " contains Menu " + num + ". Using later value.");
                 try {
                     priceIndex = Integer.parseInt(num) - 1;
                 } catch (NumberFormatException e) {
                     throw new CrawlException("Cannot parse menu number '" + num + "' in text '" + text + "'", e);
                 }
             }
 
             String priceString = m.group(2);
 
             try
             {
                 prices[priceIndex] = Integer.parseInt(priceString);
             }
             catch (NumberFormatException e)
             {
                 throw new CrawlException("Canot parse price string '" + priceString + "' in text '" + text + "'", e);
             }
         }
         return prices;
     }
 
     Calendar parseStartDate(Document doc) throws CrawlException
     {
         String xpath = "//x:div[@id='main']/x:div[@class='columnleft3r']/x:p[1]/x:em";
         Nodes nodes = doc.query(xpath, xpathContext);
 
         if (nodes.size() != 1)
             throw new CrawlException("Cannot parse start date: nodes.size() is " + nodes.size());
 
         String text = nodes.get(0).getValue();
 
         Matcher m = StartDatePattern.matcher(text);
 
         if (!m.matches())
             throw new CrawlException("Malformed date text: '" + text + "'");
 
         try
         {
             int year = Integer.parseInt(m.group(5));
             int month = Integer.parseInt(m.group(2));
             int day = Integer.parseInt(m.group(1));
             if (year < 2008 || year > 2100)
                 throw new CrawlException("Suspicious date: '" + text + "', the year " + year + " is out of range");
             return new GregorianCalendar(year, month - 1, day);
         }
         catch (NumberFormatException e)
         {
             throw new CrawlException("Canot parse date from text '" + text + "'", e);
         }
     }
 
     Nodes parseMenuNodes(Document doc) throws CrawlException
     {
         String xpath = "//x:div[@id='main']/x:div[@class='columnleft3r']/x:h4/following-sibling::x:p";
         Nodes nodes = doc.query(xpath, xpathContext);
         if (nodes.size() != 5)
             throw new CrawlException("Cannot parse menu nodes: nodes.size() is " + nodes.size());
         return nodes;
     }
 
     /// @return { soup, menu1, menu2, menu3 } or null
     String[] parseDay(Node menuPara) throws CrawlException
     {
         ParentNode parent = menuPara.getParent();
         String day = "(unknown)";
         for (int ix = parent.indexOf(menuPara)-1; ix > 0; ix--)
         {
             Node n = parent.getChild(ix);
             if (n instanceof Element && ((Element)n).getLocalName() == "h4")
             {
                 day = n.getValue();
                 break;
             }
         }
         log.fine("Parsing day '" + day + "'");
 
         if (menuPara.getChildCount() < 3)
         {
             log.warning("Skipping possibly empty menu node:\n" + menuPara.toXML());
             return null;
         }
 
         if (!(menuPara.getChild(0) instanceof Element 
                     && normalizeText(menuPara.getChild(0).getValue()).equals("Polévka:") 
                     && menuPara.getChild(1) instanceof Text))
         {
             log.warning("Skipping menu node without soup:\n" + menuPara.toXML());
             return null;
         }
 
         String[] meals = new String[4];
         meals[0] = normalizeText(menuPara.getChild(1).getValue());
         log.fine("\tsoup: " + meals[0]);
 
         for (int ix=1; ix<=3; ix++)
         {
             // extract menu number
             Nodes nodes = menuPara.query("x:span[@class='menu" + ix +"']", xpathContext);
             if (nodes.size() != 1)
             {
                 StringBuffer msg = new StringBuffer("\tskipping menu" + ix + ": span[@class='menu" + ix + "'] has " + nodes.size() + " nodes.\n");
                 for (int nix = 0; nix < nodes.size(); nix++)
                     msg.append("\t\t--node" + nix + "--\n" + nodes.get(nix).toXML());
                 log.warning(msg.toString());
                 continue;
             }
 
             String numberText = nodes.get(0).getValue();
             Matcher m = MenuNumberPattern.matcher(numberText);
             if (!m.matches())
                 throw new CrawlException("Malformed menu number: '" + numberText + "'");
 
             int menuIndex = ix;
             String num = m.group(1);
             if (!num.equals(String.valueOf(ix))) {
                 log.info("\tclass menu" + ix + " contains Menu " + num + ". Using later value.");
                 try {
                     menuIndex = Integer.parseInt(num);
                 } catch (NumberFormatException e) {
                     log.warning("cannot parse '" + num + "' as integer, skipping class menu" + ix);
                     continue;
                 }
             }
                 
             // extract meal name
             nodes = menuPara.query("x:span[@class='menu" + ix +"']/following-sibling::text()[position()=1]", xpathContext);
             if (nodes.size() != 1)
             {
                 StringBuffer msg = new StringBuffer("\tskipping menu" + ix + ": nodes.size() is " + nodes.size() + "\n");
                 for (int nix = 0; nix < nodes.size(); nix++)
                     msg.append("\t\t--node" + nix + "--\n" + nodes.get(nix).toXML());
                 log.warning(msg.toString());
                 continue;
             }
 
             meals[menuIndex] = normalizeText(nodes.get(0).getValue());
             log.fine("\tMenu " + menuIndex + ": " + meals[menuIndex]);
         }
 
         return meals;
     }
 
     public OneDayMenu[] parse(InputStream source) throws CrawlException
     {
         try
         {
             Document doc = builder.build(source);
             int prices[] = parsePrices(doc);
             Calendar start = parseStartDate(doc);
             
             Nodes days = parseMenuNodes(doc);
             if (days.size() != 5)
                 log.warning("There is menu for " + days.size() + " days.");
 
             OneDayMenu[] retval = new OneDayMenu[days.size()];
 
             for (int d=0; d<days.size(); d++, start.add(Calendar.DAY_OF_MONTH, 1))
             {
                 String[] names = parseDay(days.get(d));
 
                 if (names == null)
                 { 
                     retval[d] = new OneDayMenu(start.getTime(), null, null);
                     continue;
                 }
 
                 List<SoupItem> soups = new ArrayList<SoupItem>(1);
                 soups.add(new SoupItem("Polévka", names[0]));
 
                 List<MenuItem> meals = new ArrayList<MenuItem>(3);
                 if (names[1] != null)
                     meals.add(new MenuItem("Menu 1", names[1], prices[0]));
                 if (names[2] != null)
                     meals.add(new MenuItem("Menu 2", names[2], prices[1]));
                 if (names[3] != null)
                     meals.add(new MenuItem("Menu 3", names[3], prices[2]));
 
                 retval[d] = new OneDayMenu(start.getTime(), soups, meals);
             }
 
             return retval;
         }
         catch (XPathException e)
         {
             throw new CrawlException("Problem in parsing XPath", e);
         }
         catch (ParsingException e)
         {
             throw new CrawlException("Cannot parse HTML document", e);
         }
         catch (IOException e)
         {
             throw new CrawlException("Cannot read HTML document", e);
         }
     }
 }
 
