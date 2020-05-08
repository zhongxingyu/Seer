 package org.synyx.minos.core.web;
 
 import java.util.Locale;
 
import javax.servlet.http.HttpServletRequest;
 import javax.servlet.jsp.PageContext;
 
 import org.displaytag.decorator.DisplaytagColumnDecorator;
 import org.displaytag.exception.DecoratorException;
 import org.displaytag.properties.MediaTypeEnum;
 import org.joda.time.DateMidnight;
 import org.joda.time.ReadableInstant;
 import org.joda.time.format.DateTimeFormat;
 import org.joda.time.format.DateTimeFormatter;
import org.springframework.web.servlet.support.RequestContextUtils;
 import org.synyx.minos.core.domain.User;
 
 
 /**
 * Basic column decorator that automatically decorates instances of very basic types to String values. Currently the
 * following types are decorated automatically:
  * <ul>
  * <li>org.joda.time.DateTime</li>
  * </ul>
  * 
  * @author Oliver Gierke - gierke@synyx.de
  */
 public class MinosColumnDecorator implements DisplaytagColumnDecorator {
 
     /*
      * (non-Javadoc)
      * 
     * @see org.displaytag.decorator.DisplaytagColumnDecorator#decorate(java.lang .Object,
     * javax.servlet.jsp.PageContext, org.displaytag.properties.MediaTypeEnum)
      */
    public Object decorate(Object columnValue, PageContext pageContext, MediaTypeEnum media) throws DecoratorException {
 
         if (columnValue instanceof ReadableInstant) {
             ReadableInstant date = (ReadableInstant) columnValue;
            // Locale locale = pageContext.getRequest().getLocale();
            Locale locale = RequestContextUtils.getLocale((HttpServletRequest) pageContext.getRequest());
             DateTimeFormatter formatter = getFormatterForInstant(date, locale);
             return formatter.print(date);
         }
 
         if (columnValue instanceof User) {
 
             User user = (User) columnValue;
             return user.getFullName();
         }
 
         return columnValue;
     }
 
 
     /**
      * Returns a {@link DateTimeFormatter} for a given {@link ReadableInstant}
      * 
     * @param instant the {@link ReadableInstant} to get the {@link DateTimeFormatter} for
      * @param locale the Locale to get the {@link DateTimeFormatter} for
      * @return {@link DateTimeFormatter}
      */
    private DateTimeFormatter getFormatterForInstant(ReadableInstant instant, Locale locale) {
 
         // if its a datemidnight we use shortdateformat for Date, otherwise
         // for DateTime
 
         if (instant instanceof DateMidnight) {
             return DateTimeFormat.shortDate().withLocale(locale);
         }
 
         return DateTimeFormat.shortDateTime().withLocale(locale);
 
     }
 }
