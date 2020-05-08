 package webmenu.crawler;
 
 import org.junit.*;
 import static org.junit.Assert.*;
 import static org.hamcrest.Matchers.*;
 import static webmenu.crawler.TestRegexMatcher.matches;
 
 import java.io.*;
 import java.util.*;
 import java.util.regex.Pattern;
 import org.hamcrest.Matcher;
 import webmenu.model.*;
 
 public class CafePopularParserTest {
     @Before public void setUp() {
         TestUtil.setupLoggingToConsole();
     }
 
     InputStream getShortMenuStream() throws Exception {
         File f = TestUtil.getTestData("cafe-popular-short.pdf");
         return new FileInputStream(f);
     }
 
     InputStream getFullMenuStream() throws Exception {
         File f = TestUtil.getTestData("cafe-popular-full.pdf");
         return new FileInputStream(f);
     }
 
     String loadShortMenu() throws Exception {
         return CafePopularParser.extractText(getShortMenuStream());
     }
 
     @Test public void loadPdf_ShortMenu_ReturnsSensibleContent() throws Exception {
         String pdfText = loadShortMenu();
 
         assertThat(pdfText, allOf(
                     matches("(?s).*S *P *E *C *I *A *L *O *F *F *E *R(?s).*"),
                     matches("(?s).*S *T *Á *L *Á *T *Ý *D *E *N *N *Í *N *A *B *Í *D *K *A *:(?s).*")
                     ));
     }
 
     @Test public void parseStartDate_ShortMenu_Returns16_11_2009() throws Exception {
         String pdfText = loadShortMenu();
         Calendar start = CafePopularParser.parseStartDate(pdfText);
         assertEquals(new GregorianCalendar(2009, 11-1, 16).getTime(), start.getTime());
     }
 
     @Test public void parsePizza1_ShortMenu_ReturnsCorrectItem() throws Exception {
         String pdfText = loadShortMenu();
         MenuItem item = CafePopularParser.parsePizza1(pdfText);
         assertThat(item.getName(), equalTo("Pizza 1"));
         assertThat(item.getMeal(), equalTo("„SMETANA, SÝR MOZZARELLA, BURGUNDSKÁ ŠUNKA, ŽAMPIONY“"));
         assertThat(item.getPrice(), equalTo(99 + CafePopularParser.PizzaFee));
     }
 
     @Test public void parsePizza2_ShortMenu_ReturnsCorrectItem() throws Exception {
         String pdfText = loadShortMenu();
         MenuItem item = CafePopularParser.parsePizza2(pdfText);
         assertThat(item.getName(), equalTo("Pizza 2"));
         assertThat(item.getMeal(), equalTo("„SUGO, ČERTVÝ SÝR MOZZARELLA, PAPRIKOVÝ SALÁM, FEFERONY“"));
         assertThat(item.getPrice(), equalTo(99 + CafePopularParser.PizzaFee));
     }
 
     @Test public void parseSalad_ShortMenu_ReturnsCorrectItem() throws Exception {
         String pdfText = loadShortMenu();
         MenuItem item = CafePopularParser.parseSalad(pdfText);
         assertThat(item.getName(), equalTo("Salát"));
         assertThat(item.getMeal(), equalTo("„ARABSKÝ SALÁT“ - restovaná kuřecí prsíčka marinovaná v limetkách a čerstvých bylinkách, na lůžku list. salátů, zeleniny a kous-kousem, olivami, bylinkový dresink, pita chléb"));
         assertThat(item.getPrice(), equalTo(99 + CafePopularParser.SaladFee));
     }
 
     @Test public void parseItaly_ShortMenu_ReturnsCorrectItem() throws Exception {
         String pdfText = loadShortMenu();
         MenuItem item = CafePopularParser.parseItaly(pdfText);
         assertThat(item.getName(), equalTo("Italská"));
         assertThat(item.getMeal(), equalTo("„PASTA CON POLLO E BROCOLLI“ - italské spaghetti s restovaným kuřecím masem marinovaným v bylinkách, na cibulce s brokolicí a smetanou, sypané parmezanem, cherry rajče, rucola"));
         assertThat(item.getPrice(), equalTo(99 + CafePopularParser.WeekMealFee));
     }
 
     @Test public void parseCzech_ShortMenu_ReturnsCorrectItem() throws Exception {
         String pdfText = loadShortMenu();
         MenuItem item = CafePopularParser.parseCzech(pdfText);
         assertThat(item.getName(), equalTo("Česká"));
         assertThat(item.getMeal(), equalTo("„STAROČESKÝ GULÁŠ POPULAR“ - domácí guláš z hovězí kližky, servírovaný s variací knedlíků (2+2+2) bramborový, houskový karlovarský, feferonka, pečená vídeňská cibulka"));
         assertThat(item.getPrice(), equalTo(79 + CafePopularParser.WeekMealFee));
     }
 
     @Test public void parseVegetarian_ShortMenu_ReturnsCorrectItem() throws Exception {
         String pdfText = loadShortMenu();
         MenuItem item = CafePopularParser.parseVegetarian(pdfText);
         assertThat(item.getName(), equalTo("Bezmasá"));
         assertThat(item.getMeal(), equalTo("„VEGETARIANSKÉ ASIJSKÉ NUDLE – restovaný tofu sýr, bambus, sojové výhonky, cibule, mrkev vejce, sojová omáčka, ořechy, servírované s čínskými nudlemi a zelným salátkem"));
         assertThat(item.getPrice(), equalTo(79 + CafePopularParser.WeekMealFee));
     }
 
     @Test public void parseDayMenuPrice_ShortMenu_ReturnsCorrectValue() throws Exception {
         String pdfText = loadShortMenu();
         int price = CafePopularParser.parseDayMenuPrice(pdfText);
         assertThat(price, equalTo(99));
     }
 
     @Test public void parseMonday_ShortMenu_ReturnsCorrectItem() throws Exception {
         String pdfText = loadShortMenu();
         CafePopularParser.MenuWithSoup result = CafePopularParser.parseMonday(pdfText, 42);
 
         assertThat(result.menu.getName(), equalTo("Dnešní"));
         assertThat(result.menu.getMeal(), equalTo("„PLNĚNÉ DOMÁCÍ PALAČINKY“ - restovaná krůtí prsíčka se smetanovo-karotkovým špenátem, servírovaná v tenké palačince se sýrovým přelivem a čerstvou sezóní zeleninou"));
         assertThat(result.menu.getPrice(), equalTo(42));
 
         assertThat(result.soup.getName(), equalTo("Polévka"));
         assertThat(result.soup.getMeal(), equalTo("Drůbeží polévka s masem zeleninou a knedlíčky"));
     }
 
     @Test public void parseTuesday_ShortMenu_ReturnsNull() throws Exception {
         String pdfText = loadShortMenu();
         CafePopularParser.MenuWithSoup result = CafePopularParser.parseTuesday(pdfText, 42);
         assertNull(result);
     }
 
     @Test public void parseWednesday_ShortMenu_ReturnsCorrectItem() throws Exception {
         String pdfText = loadShortMenu();
         CafePopularParser.MenuWithSoup result = CafePopularParser.parseWednesday(pdfText, 42);
 
         assertThat(result.menu.getName(), equalTo("Dnešní"));
         assertThat(result.menu.getMeal(), equalTo("„KUŘECÍ GRILOVANÉ MEDAILONKY PEČENÉ V BBQ MARINÁDĚ“ - servírujeme s jemným výpekem, hranolkami Jullien, máslovým kukuřičným klasem a zelným salátkem Colleslaw"));
         assertThat(result.menu.getPrice(), equalTo(42));
 
         assertThat(result.soup.getName(), equalTo("Polévka"));
         assertThat(result.soup.getMeal(), equalTo("Brokolicová polévka sypaná sýrem"));
     }
 
     @Test public void parseThursday_ShortMenu_ReturnsCorrectItem() throws Exception {
         String pdfText = loadShortMenu();
         CafePopularParser.MenuWithSoup result = CafePopularParser.parseThursday(pdfText, 42);
 
         assertThat(result.menu.getName(), equalTo("Dnešní"));
         assertThat(result.menu.getMeal(), equalTo("„PANGASIUS NA ROZMARÝNU“ - grilovaný pangasius na rozmarýnu a olivovém oleji, servírujeme s tomatovým přelivem, domácí bramborovou kaší zjemněnou máslem a čerstvou zeleninou"));
         assertThat(result.menu.getPrice(), equalTo(42));
 
         assertThat(result.soup.getName(), equalTo("Polévka"));
         assertThat(result.soup.getMeal(), equalTo("Italská čočková polévka s uzeným masem"));
     }
 
     @Test public void parseFriday_ShortMenu_ReturnsCorrectItem() throws Exception {
         String pdfText = loadShortMenu();
         CafePopularParser.MenuWithSoup result = CafePopularParser.parseFriday(pdfText, 42);
 
         assertThat(result.menu.getName(), equalTo("Dnešní"));
         assertThat(result.menu.getMeal(), equalTo("„SÝROVÝ ŠPÍZ POPULAR“ - (mozzarella, eidam, uzený sýr, blue cheese), servírovaný s dušeným petrželovým bramborem zjemněným máslem, domácí tatarská omáčka, zelný salátek"));
         assertThat(result.menu.getPrice(), equalTo(42));
 
         assertThat(result.soup.getName(), equalTo("Polévka"));
         assertThat(result.soup.getMeal(), equalTo("Gulášová polévka s bramborem"));
     }
     
 
     @Test public void parse_ShortMenu_ReturnsCorrectMenus() throws Exception {
         OneDayMenu[] menus = new CafePopularParser().parse(getShortMenuStream());
 
         assertThat(menus.length, greaterThan(2));
 
         OneDayMenu m1 = menus[0];
         assertEquals(new GregorianCalendar(2009, 11-1, 16).getTime(), m1.getDay());
 
         List<SoupItem> soupItems = m1.getSoupItems();
         assertThat(soupItems.size(), equalTo(1));
         assertThat(soupItems.get(0).getMeal(), equalTo("Drůbeží polévka s masem zeleninou a knedlíčky"));
 
         List<MenuItem> menuItems = m1.getMenuItems();
         assertThat(menuItems.size(), equalTo(7));
 
         assertThat(menuItems.get(0).getName(), equalTo("Pizza 1"));
         assertThat(menuItems.get(1).getName(), equalTo("Pizza 2"));
         assertThat(menuItems.get(2).getName(), equalTo("Salát"));
         assertThat(menuItems.get(3).getName(), equalTo("Italská"));
         assertThat(menuItems.get(4).getName(), equalTo("Česká"));
         assertThat(menuItems.get(5).getName(), equalTo("Bezmasá"));
         assertThat(menuItems.get(6).getName(), equalTo("Dnešní"));
 
         // tuesday was skipped
         assertTrue(menus[1].isEmpty());
         assertEquals(new GregorianCalendar(2009, 11-1, 17).getTime(), menus[1].getDay());
 
         // verify that the third menu has correct date
         assertFalse(menus[2].isEmpty());
         assertEquals(new GregorianCalendar(2009, 11-1, 18).getTime(), menus[2].getDay());
     }
 
     @Test public void parse_FullMenu_SmokeTest() throws Exception {
         OneDayMenu[] menus = new CafePopularParser().parse(getFullMenuStream());
         assertThat(menus.length, greaterThan(2));
 
         assertFalse(menus[0].isEmpty());
         assertEquals(new GregorianCalendar(2010, 03-1, 01).getTime(), menus[0].getDay());
 
         assertFalse(menus[1].isEmpty());
         assertEquals(new GregorianCalendar(2010, 03-1, 02).getTime(), menus[1].getDay());
 
         assertFalse(menus[2].isEmpty());
         assertEquals(new GregorianCalendar(2010, 03-1, 03).getTime(), menus[2].getDay());
 
         assertFalse(menus[3].isEmpty());
         assertEquals(new GregorianCalendar(2010, 03-1, 04).getTime(), menus[3].getDay());
 
         assertFalse(menus[4].isEmpty());
         assertEquals(new GregorianCalendar(2010, 03-1, 05).getTime(), menus[4].getDay());
     }
 
     @Test public void parse_Pizza2Problem_Works() throws Exception {
         File f = TestUtil.getTestData("cafe-popular-pizza2.pdf");
         FileInputStream s = new FileInputStream(f);
         String pdfText = CafePopularParser.extractText(s);
         MenuItem item = CafePopularParser.parsePizza2(pdfText);
         assertNotNull(item);
         assertThat(item.getName(), equalTo("Pizza 2"));
         assertThat(item.getMeal(), equalTo("„PIZZA VEGETARIANA“ - sugo, mozzarella, kukuřice, parika, cuketa, lilek, pesto"));
         assertThat(item.getPrice(), equalTo(99 + CafePopularParser.PizzaFee));
     }
 
     @Test public void parse_MissingDotInDayMenu_Works() throws Exception {
         File f = TestUtil.getTestData("cafe-popular-pizza2.pdf");
         FileInputStream s = new FileInputStream(f);
         String pdfText = CafePopularParser.extractText(s);
 
 
         CafePopularParser.MenuWithSoup result = CafePopularParser.parseTuesday(pdfText, 42);
         assertNotNull(result);
         assertNotNull(result.menu);
 
         assertThat(result.menu.getName(), equalTo("Dnešní"));
         assertThat(result.menu.getMeal(), equalTo("„GRILOVANÁ MARINOVANÁ VEPŘOVÁ KOTLETKA“ - vepřová kotletka na grilu s rozmarýnem, servírovaná s restovanou máslovou kukuřicí, bramborovými kroketami a bazalkovým přelivem"));
         assertThat(result.menu.getPrice(), equalTo(42));
 
         assertThat(result.soup.getName(), equalTo("Polévka"));
         assertThat(result.soup.getMeal(), equalTo("Italská z čerstvých rajčat s parmezanem"));
     }
 
     @Test public void parse_SpacingProblem_SmokeTest() throws Exception {
         InputStream stream = new FileInputStream(TestUtil.getTestData("cafe-popular-spaces.pdf"));
         OneDayMenu[] menus = new CafePopularParser().parse(stream);
     }

    @Test public void parse_BadDate_MenuHasCorrectDates() throws Exception {
        InputStream stream = new FileInputStream(TestUtil.getTestData("cafe-popular-baddate.pdf"));
        OneDayMenu[] menus = new CafePopularParser().parse(stream);

        assertThat(menus[0].getDay(), equalTo(new GregorianCalendar(2010, 07-1, 05).getTime()));
    }
 }
