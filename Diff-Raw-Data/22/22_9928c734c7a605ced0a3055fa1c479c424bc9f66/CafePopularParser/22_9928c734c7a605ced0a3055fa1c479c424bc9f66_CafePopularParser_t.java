 package webmenu.crawler;
 
 import java.io.*;
 import java.util.*;
 import java.util.logging.Logger;
 import java.util.regex.*;
 
 import com.itextpdf.text.pdf.parser.*;
 import com.itextpdf.text.pdf.PdfReader;
 
 import webmenu.model.*;
 import static webmenu.crawler.ParserUtil.normalizeText;
 
 public class CafePopularParser implements Parser {
     private static final Logger log = Logger.getLogger(CafePopularParser.class.getName());
 
     final static Pattern StartDatePattern = Pattern.compile("MENU V OBDOBÍ OD\\s+(\\d+)\\.(\\d+)\\.\\s+-\\s+(\\d+)\\.(\\d+)\\.(\\d+)");
 
     final static int PizzaFee = 8;
     final static Pattern Pizza1Pattern = Pattern.compile("PIZZA\\s+No.1\\s+(.+)\\s+Kč *(\\d+),-");
     final static Pattern Pizza2Pattern = Pattern.compile("PIZZA\\s+No.2\\s+(.+)\\s+Kč *(\\d+),-");
 
     final static int SaladFee = 0; // TODO packaging & shipping in CZK
     final static Pattern SaladPattern = Pattern.compile(
             "S *T *Á *L *Á *T *Ý *D *E *N *N *Í *N *A *B *Í *D *K *A *: *S *A *L *Á *T */ */ *S *A *L *A *D\\s+●\\s*(.+\\s?.*)\\s+Kč *(\\d+),-");
 
     final static int WeekMealFee = 20;
     final static Pattern ItalyPattern = Pattern.compile(
             "S *T *Á *L *Á *T *Ý *D *E *N *N *Í *N *A *B *Í *D *K *A *: *I *T *A *L *S *K *Á *K *U *C *H *Y *N *Ě */ */ *I *TA *L *I *A *N *C *O *U *S *I *N *E\\s+●\\s*(.+\\s?.*)\\s+Kč *(\\d+),-");
     final static Pattern CzechPattern = Pattern.compile(
             "S *T *Á *L *Á *T *Ý *D *E *N *N *Í *N *A *B *Í *D *K *A *: *Č *E *S *K *Á *K *U *C *H *Y *N *Ě */ */ *C *Z *E *C *H *C *O *U *S *I *N *E\\s+●\\s*(.+\\s?.*)\\s+Kč *(\\d+),-");
     final static Pattern VegetarianPattern = Pattern.compile(
             "S *T *Á *L *Á *T *Ý *D *E *N *N *Í *N *A *B *Í *D *K *A *: *B *E *Z *M *A *S *Á *K *U *C *H *Y *N *Ě */ */ *W *I *T *H *O *U *T *M *E *AT *C *O *U *S *I *N *E\\s+●\\s*(.+\\s?.*)\\s+Kč *(\\d+),-");
 
     final static Pattern DayMenuPricePattern = Pattern.compile("KOMPLETNÍ MENU S POLÉVKOU\\s*\\d+,- Kč\\s+(\\d+),- Kč");
 
     final static Pattern DayMenuPattern = Pattern.compile("^\\s*●?\\s*(.+\\s?.*)\\s+●\\s*(.+)\\s$");
     final static Pattern MondayPattern = Pattern.compile("P *O *N *D *Ě *L *Í((?s).+)Ú *T *E *R *Ý");
     final static Pattern TuesdayPattern = Pattern.compile("Ú *T *E *R *Ý((?s).+)S *T *Ř *E *D *A");
     final static Pattern WednesdayPattern = Pattern.compile("S *T *Ř *E *D *A((?s).+)Č *T *V *R *T *E *K");
     final static Pattern ThursdayPattern = Pattern.compile("Č *T *V *R *T *E *K((?s).+)P *Á *T *E *K");
     final static Pattern FridayPattern = Pattern.compile("P *Á *T *E *K((?s).+)KOMPLETNÍ(?s).+S *DOVOZEM");
 
     static String extractText(InputStream source) throws IOException, CrawlException {
         PdfReader reader = new PdfReader(source);
         if (reader.getNumberOfPages() != 1)
             throw new CrawlException("The PDF document has " + reader.getNumberOfPages() + " pages.");
 
         PdfTextExtractor extractor = new PdfTextExtractor(reader, new PdfTextListener());
         return extractor.getTextFromPage(1);
     }
 
     static Calendar parseStartDate(String pdfText) throws CrawlException {
         Matcher m = StartDatePattern.matcher(pdfText);
 
         if (!m.find())
             throw new CrawlException("Malformed date text: '" + pdfText + "'");
 
         try {
             int year = Integer.parseInt(m.group(5));
             int month = Integer.parseInt(m.group(2));
             int day = Integer.parseInt(m.group(1));
             if (year < 2008 || year > 2100)
                 throw new CrawlException("Suspicious date: '" + pdfText + "', the year " + year + " is out of range");
            Calendar result = new GregorianCalendar(year, month - 1, day);
            // Make sure the first day is Monday
            int dayInWeek = result.get(Calendar.DAY_OF_WEEK);
            result.add(Calendar.DAY_OF_MONTH, -(dayInWeek - Calendar.MONDAY + 7) % 7);
            return result;
         } catch (NumberFormatException e) {
             throw new CrawlException("Canot parse date from text '" + pdfText + "'", e);
         }
     }
 
     static MenuItem parseMenuItem(String pdfText, Pattern pattern, String name, int fee) throws CrawlException {
         Matcher m = pattern.matcher(pdfText);
         if (!m.find()) {
             log.warning(name + " not found\n" + pdfText);
             return null;
         }
     
         try {
             String meal = normalizeText(m.group(1));
             int price = Integer.parseInt(m.group(2));
             price += fee;
            log.fine("Parsed '"+ name + "': '" + meal + "' for " + price + ",-");
             return new MenuItem(name, meal, price);
         } catch (NumberFormatException e) {
             throw new AssertionError("price is not a number");
         }
     }
 
     static MenuItem parsePizza1(String pdfText) throws CrawlException {
         return parseMenuItem(pdfText, Pizza1Pattern, "Pizza 1", PizzaFee);
     }
 
     static MenuItem parsePizza2(String pdfText) throws CrawlException {
         return parseMenuItem(pdfText, Pizza2Pattern, "Pizza 2", PizzaFee);
     }
 
     static MenuItem parseSalad(String pdfText) throws CrawlException {
         return parseMenuItem(pdfText, SaladPattern, "Salát", SaladFee);
     }
 
     static MenuItem parseItaly(String pdfText) throws CrawlException {
         return parseMenuItem(pdfText, ItalyPattern, "Italská", WeekMealFee);
     }
 
     static MenuItem parseCzech(String pdfText) throws CrawlException {
         return parseMenuItem(pdfText, CzechPattern, "Česká", WeekMealFee);
     }
 
     static MenuItem parseVegetarian(String pdfText) throws CrawlException {
         return parseMenuItem(pdfText, VegetarianPattern, "Bezmasá", WeekMealFee);
     }
 
     static int parseDayMenuPrice(String pdfText) throws CrawlException {
         Matcher m = DayMenuPricePattern.matcher(pdfText);
         if (!m.find())
             throw new CrawlException("DayMenuPrice not found\n" + pdfText);
 
         try {
             return Integer.parseInt(m.group(1));
         } catch (NumberFormatException e) {
             throw new AssertionError("price is not a number");
         }
     }
 
     static class MenuWithSoup {
         MenuItem menu;
         SoupItem soup;
         
         MenuWithSoup(MenuItem menu, SoupItem soup) {
             this.menu = menu;
             this.soup = soup;
         }
     };
 
     static MenuWithSoup parseDayMenu(String pdfText, int menuPrice, Pattern pattern, String day) throws CrawlException {
         Matcher m = pattern.matcher(pdfText);
         if (!m.find())
             throw new CrawlException("day-menu not found\n" + pdfText);
         String inner = m.group(1);
 
         m = DayMenuPattern.matcher(inner);
         if (!m.find()) {
             log.warning("Skipping malformed day-menu " + day + ":\n" + inner);
             return null;
         }
 
        log.fine("DayMenu - Soup '" + m.group(2) + "', Meal + '" + m.group(1) + "'.");
         return new MenuWithSoup(
                 new MenuItem("Dnešní", normalizeText(m.group(1)), menuPrice),
                 new SoupItem("Polévka", normalizeText(m.group(2)))
                 );
     }
 
     static MenuWithSoup parseMonday(String pdfText, int menuPrice) throws CrawlException {
         return parseDayMenu(pdfText, menuPrice, MondayPattern, "Pondělí");
     }
 
     static MenuWithSoup parseTuesday(String pdfText, int menuPrice) throws CrawlException {
         return parseDayMenu(pdfText, menuPrice, TuesdayPattern, "Úterý");
     }
 
     static MenuWithSoup parseWednesday(String pdfText, int menuPrice) throws CrawlException {
         return parseDayMenu(pdfText, menuPrice, WednesdayPattern, "Středa");
     }
 
     static MenuWithSoup parseThursday(String pdfText, int menuPrice) throws CrawlException {
         return parseDayMenu(pdfText, menuPrice, ThursdayPattern, "Čtvrtek");
     }
 
     static MenuWithSoup parseFriday(String pdfText, int menuPrice) throws CrawlException {
         return parseDayMenu(pdfText, menuPrice, FridayPattern, "Pátek");
     }
 
     public OneDayMenu[] parse(InputStream source) throws CrawlException {
         try {
             String pdfText = extractText(source);
             Calendar start = parseStartDate(pdfText);
             int dayMenuPrice = parseDayMenuPrice(pdfText);
 
             MenuItem pizza1 = parsePizza1(pdfText);
             MenuItem pizza2 = parsePizza2(pdfText);
             MenuItem salad = parseSalad(pdfText);
             MenuItem italy = parseItaly(pdfText);
             MenuItem czech = parseCzech(pdfText);
             MenuItem vegetarian = parseVegetarian(pdfText);
             MenuWithSoup[] days = new MenuWithSoup[] {
                 parseMonday(pdfText, dayMenuPrice),
                 parseTuesday(pdfText, dayMenuPrice),
                 parseWednesday(pdfText, dayMenuPrice),
                 parseThursday(pdfText, dayMenuPrice),
                 parseFriday(pdfText, dayMenuPrice)
             };
 
             OneDayMenu[] retval = new OneDayMenu[5];
             for (int d=0; d<5; d++, start.add(Calendar.DAY_OF_MONTH, 1))
             {
                 if (days[d] == null)
                 {
                     retval[d] = new OneDayMenu(start.getTime(), null, null);
                     continue;
                 }
 
                 List<SoupItem> soups = new ArrayList<SoupItem>(1);
                 soups.add(days[d].soup);
 
                 List<MenuItem> meals = new ArrayList<MenuItem>(7);
                 if (pizza1 != null) meals.add(new MenuItem(pizza1));
                 if (pizza2 != null) meals.add(new MenuItem(pizza2));
                 if (salad != null) meals.add(new MenuItem(salad));
                 if (italy != null) meals.add(new MenuItem(italy));
                 if (czech != null) meals.add(new MenuItem(czech));
                 if (vegetarian != null) meals.add(new MenuItem(vegetarian));
                 meals.add(days[d].menu);
 
                 retval[d] = new OneDayMenu(start.getTime(), soups, meals);
             }
 
             return retval;
         } catch (IOException e) {
             throw new CrawlException("Cannot read PDF document", e);
         }
     }
 }
