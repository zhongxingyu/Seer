 package webmenu.crawler;
 
 import org.junit.*;
 import static org.junit.Assert.*;
 import static org.hamcrest.Matchers.*;
 
 import java.io.*;
 import java.util.*;
 import nu.validator.htmlparser.common.*;
 import nu.validator.htmlparser.xom.*;
 import nu.xom.*;
 import org.hamcrest.Matcher;
 import webmenu.model.*;
 
 public class SportCafeParserTest {
     SportCafeParser parser;
     HtmlBuilder builder;
 
     @Before public void setUp() {
         parser = new SportCafeParser();
         builder = new HtmlBuilder(XmlViolationPolicy.ALTER_INFOSET);
         TestUtil.setupLoggingToConsole();
     }
 
     InputStream getShortMenuStream() throws FileNotFoundException {
         File f = TestUtil.getTestData("sport-cafe-menu-short.html");
         // System.out.println(f.getCanonicalPath());
         return new FileInputStream(f);
     }
 
     Document loadDocument(InputStream source) throws ParsingException, IOException {
         return builder.build(source);
     }
 
     Node extractDay(int day, InputStream source) throws CrawlException, IOException, ParsingException {
         Nodes days = parser.parseMenuNodes(loadDocument(source));
         return days.get(day);
     }
 
     @Test public void getStartDate_NowIsMonday_ReturnsLastMonday() {
         Calendar start = SportCafeParser.getStartDate(new GregorianCalendar(2010, 01-1, 11, 10, 20, 30));
         assertEquals(new GregorianCalendar(2010, 01-1, 11).getTime(), start.getTime());
     }
 
     @Test public void getStartDate_NowIsSunday_ReturnsLastMonday() {
         Calendar start = SportCafeParser.getStartDate(new GregorianCalendar(2010, 01-1, 03));
         assertEquals(new GregorianCalendar(2009, 12-1, 28).getTime(), start.getTime());
     }
 
     @Test public void parseMenuNodes_ShortHtml_ReturnsFiveItems() throws CrawlException, IOException, ParsingException {
         Nodes nodes = parser.parseMenuNodes(loadDocument(getShortMenuStream()));
         assertEquals(5, nodes.size());
     }
 
     @Test public void parseDayName_ShortHtmlDayOne_ReturnsMonday() throws CrawlException, IOException, ParsingException {
         Node node = extractDay(0, getShortMenuStream());
         String dayName = parser.parseDayName(node);
         assertEquals("pondeli", dayName);
     }
 
     @Test public void parseDayName_ShortHtmlDayTwo_ReturnsTuesday() throws CrawlException, IOException, ParsingException {
         Node node = extractDay(1, getShortMenuStream());
         String dayName = parser.parseDayName(node);
         assertEquals("utery", dayName);
     }
 
     @Test public void parseSoupName_ShortHtmlDayOne_ReturnsCorrectName() throws CrawlException, IOException, ParsingException {
         Node node = extractDay(0, getShortMenuStream());
         String soupName = parser.parseSoupName(node);
         assertEquals("Krupicová s vejcem", soupName);
     }
 
     @Test public void parseSoupName_ShortHtmlDayTwo_ReturnsCorrectName() throws CrawlException, IOException, ParsingException {
         Node node = extractDay(1, getShortMenuStream());
         String soupName = parser.parseSoupName(node);
         // Note: we cannot tell whether the text is soup name or a message
         // These two states are handled elsewhere
         assertEquals("Státní svátek otevřeno od 11:30hod- 24:00 hod", soupName);
     }
 
     @Test public void parseMenuItemCount_ShortHtmlDayOne_Returns5() throws CrawlException, IOException, ParsingException {
         Node node = extractDay(0, getShortMenuStream());
         int count = parser.parseMenuItemCount(node);
         assertEquals(5, count);
     }
 
     @Test public void parseMenuItemCount_ShortHtmlDayTwo_Returns0() throws CrawlException, IOException, ParsingException {
         Node node = extractDay(1, getShortMenuStream());
         int count = parser.parseMenuItemCount(node);
         assertEquals(0, count);
     }
 
     @Test public void parseMenuItem_ShortHtmlDayOneItemOne_ReturnsCorrectMenuItem() throws CrawlException, IOException, ParsingException {
         Node node = extractDay(0, getShortMenuStream());
         MenuItem item = parser.parseMenuItem(node, 0);
         assertEquals("Menu 1", item.getName());
         assertEquals("Smažený sýr po pastýřsku (sýr eidam, šunka), smažené hranolky, tatarská omáčka", item.getMeal());
         assertEquals(70 + 8, item.getPrice());
     }
 
     @Test public void parseMenuItem_MenuItemWithTrailingComma_ReturnsCorrectMenuItem() throws CrawlException, IOException, ParsingException {
         Node node = extractDay(0, getShortMenuStream());
         MenuItem item = parser.parseMenuItem(node, 3);
         assertEquals("Menu 4", item.getName());
         assertEquals("Krůtí špíz ,,KARIBIK? (krůtí marinované maso,šunka,čerstvý ananas) podlitý sýrovou omáčkou", item.getMeal());
         assertEquals(90 + 8, item.getPrice());
     }
     @Test public void parseMenuItem_ShortHtmlDayOneItemFive_ReturnsCorrectMenuItem() throws CrawlException, IOException, ParsingException {
         Node node = extractDay(0, getShortMenuStream());
         MenuItem item = parser.parseMenuItem(node, 4);
         assertEquals("Menu 5", item.getName());
         assertEquals("Pizza ,,Bacon? (tomatový základ, sýr, anglická slanina, kukuřice)", item.getMeal());
         assertEquals(99 + 8, item.getPrice());
     }
 
     @Test public void parse_ShortHtml_ReturnsCorrectMenus() throws CrawlException, IOException, ParsingException {
         InputStream html = getShortMenuStream();
         OneDayMenu[] menus = parser.parse(html);
 
         assertThat(menus.length, greaterThan(2));
 
         Calendar day = SportCafeParser.getStartDate(Calendar.getInstance());
 
         OneDayMenu m1 = menus[0];
         assertEquals(day .getTime(), m1.getDay());
 
         List<SoupItem> soupItems = m1.getSoupItems();
         assertThat(soupItems.size(), equalTo(1));
         assertThat(soupItems.get(0).getName(), equalTo("Polévka"));
         assertThat(soupItems.get(0).getMeal(), equalTo("Krupicová s vejcem"));
 
         List<MenuItem> menuItems = m1.getMenuItems();
         assertThat(menuItems.size(), equalTo(5));
 
         assertThat(menuItems.get(0).getName(), equalTo("Menu 1"));
         assertThat(menuItems.get(0).getMeal(), equalTo("Smažený sýr po pastýřsku (sýr eidam, šunka), smažené hranolky, tatarská omáčka"));
         assertThat(menuItems.get(0).getPrice(), equalTo(70+8));
 
         assertThat(menuItems.get(1).getName(), equalTo("Menu 2"));
         assertThat(menuItems.get(1).getMeal(), equalTo("Domácí ovocné kynuté knedlíky sypané grankem, máslo"));
         assertThat(menuItems.get(1).getPrice(), equalTo(70+8));
 
         assertThat(menuItems.get(2).getName(), equalTo("Menu 3"));
         assertThat(menuItems.get(2).getMeal(), equalTo("Pečený pstruh na másle a kmíně, vařený brambor s petrželkou, zeleninová obloha"));
         assertThat(menuItems.get(2).getPrice(), equalTo(90+8));
 
         assertThat(menuItems.get(3).getName(), equalTo("Menu 4"));
         assertThat(menuItems.get(3).getMeal(), equalTo("Krůtí špíz ,,KARIBIK? (krůtí marinované maso,šunka,čerstvý ananas) podlitý sýrovou omáčkou"));
         assertThat(menuItems.get(3).getPrice(), equalTo(90+8));
 
         assertThat(menuItems.get(4).getName(), equalTo("Menu 5"));
         assertThat(menuItems.get(4).getMeal(), equalTo("Pizza ,,Bacon? (tomatový základ, sýr, anglická slanina, kukuřice)"));
         assertThat(menuItems.get(4).getPrice(), equalTo(99+8));
 
         // tuesday was skipped
         day.add(Calendar.DAY_OF_MONTH, 1);
         assertTrue(menus[1].isEmpty());
         assertEquals(day.getTime(), menus[1].getDay());
 
         // verify that the third menu has correct date
         day.add(Calendar.DAY_OF_MONTH, 1);
         assertEquals(day.getTime(), menus[2].getDay());
     }
 }
