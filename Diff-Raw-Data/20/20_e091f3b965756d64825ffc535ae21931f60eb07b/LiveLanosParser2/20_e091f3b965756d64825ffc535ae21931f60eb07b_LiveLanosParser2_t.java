 package models.data.parser;
 
 import com.google.common.base.Function;
 import com.google.common.base.Predicate;
 import org.openqa.selenium.Dimension;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.WebElement;
 
 import javax.annotation.Nullable;
 import java.util.Collection;
 import java.util.List;
 
 import static com.google.common.base.Strings.isNullOrEmpty;
 import static com.google.common.collect.Collections2.filter;
 import static com.google.common.collect.Collections2.transform;
 import static com.google.common.collect.Lists.newArrayList;
 import static org.openqa.selenium.By.cssSelector;
 
 public class LiveLanosParser2
   implements BParser {
 
   private static final Predicate<ParsedEvent> NOT_NULL_FILTER = new Predicate<ParsedEvent>() {
     @Override
     public boolean apply(@Nullable ParsedEvent parsedEvent) {
       return null != parsedEvent;
     }
   };
 
   private final WebDriver webDriver;
 
   public LiveLanosParser2(String url, WebDriver webDriver) {
     this.webDriver = webDriver;
     this.webDriver.get(url);
     this.webDriver.manage().window().setSize(new Dimension(1800, 1000));
   }
 
   @Override
   public List<ParsedEvent> parse(byte[] input) {
     Collection<ParsedEvent> parsedEvents = newArrayList();
 
     List<WebElement> eventGroupEls = webDriver.findElements(cssSelector("#container_EVENTS > div.main-block-events"));
 
     for (WebElement eventGroupEl : eventGroupEls) {
      String sportDescr = findElTextOrNull(eventGroupEl, ".block-events-head > h2 > span", 0);
       if (null == sportDescr) continue;
 
      List<WebElement> eventEls = eventGroupEl.findElements(cssSelector("table > tbody > tr.event-header"));
       parsedEvents.addAll(transform(eventEls, createElToEventTransformer(sportDescr)));
     }
     return newArrayList(filter(parsedEvents, NOT_NULL_FILTER));
   }
 
   private static Function<WebElement, ParsedEvent> createElToEventTransformer(final String sportDescr) {
     return new Function<WebElement, ParsedEvent>() {
 
       @Nullable
       @Override
       public ParsedEvent apply(@Nullable WebElement eventEl) {
 
        String firstSide = findElTextOrNull(eventEl, ".first > table > tbody > tr:first-child > td > span.command > div", 0);
         if (null == firstSide) return null;
 
        String secondSide = findElTextOrNull(eventEl, ".first > table > tbody > tr:first-child > td > span.command > div", 1);
         if (null == secondSide) return null;
 
         String firstKof = findElTextOrNull(eventEl, "td.js-price > span", 0);
         if (null == firstKof) return null;
 
         String secondKof = findElTextOrNull(eventEl, "td.js-price > span", 1);
         if (null == secondKof) return null;
 
         return new ParsedEvent(sportDescr, firstSide, secondSide, null, firstKof, secondKof);
       }
 
     };
   }
 
   public static String findElTextOrNull(WebElement rootEl, String selector, int index) {
     List<WebElement> els = rootEl.findElements(cssSelector(selector));
     if (els.isEmpty() || els.size() <= index) return null;
 
     WebElement el = els.get(index);
     String text = el.getText();
 
     return isNullOrEmpty(text) ? null : text;
   }
 }
