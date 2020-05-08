 package org.wingx.plaf.css;
 
 import java.text.DateFormat;
 import java.text.Format;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.List;
 
 import org.wings.SDimension;
 import org.wings.SFormattedTextField;
 import org.wings.SResourceIcon;
 import org.wings.header.Header;
 import org.wings.header.SessionHeaders;
 import org.wings.io.Device;
 import org.wings.plaf.Update;
 import org.wings.plaf.css.AbstractComponentCG;
 import org.wings.plaf.css.AbstractUpdate;
 import org.wings.plaf.css.UpdateHandler;
 import org.wings.plaf.css.Utils;
 import org.wings.plaf.css.script.OnHeadersLoadedScript;
 import org.wings.session.ScriptManager;
 import org.wings.util.SStringBuilder;
 import org.wingx.XCalendar;
 
 /**
  * @author <a href="mailto:e.habicht@thiesen.com">Erik Habicht</a>
  * @author Stephan Schuster
  */
 public class CalendarCG extends AbstractComponentCG<XCalendar> implements org.wingx.plaf.CalendarCG<XCalendar> {
 
     private static final long serialVersionUID = 1L;
     
     protected final static List<Header> headers;
 
     static {
         String[] images = new String [] {
             "org/wingx/calendar/calcd.gif",
             "org/wingx/calendar/cally.gif",
             "org/wingx/calendar/calry.gif"
         };
 
         for ( int x = 0, y = images.length ; x < y ; x++ ) {
             new SResourceIcon(images[x]).getId();
         }
 
         List<Header> headerList = new ArrayList<Header>();
         headerList.add(Utils.createExternalizedCSSHeaderFromProperty(Utils.CSS_YUI_ASSETS_CALENDAR));   
         headerList.add(Utils.createExternalizedJSHeaderFromProperty(Utils.JS_YUI_CALENDAR));
         headerList.add(Utils.createExternalizedJSHeader("org/wingx/calendar/calendar.js"));
         headers = Collections.unmodifiableList(headerList);
     }
 
     public CalendarCG() {
     }
        
     public void installCG(XCalendar component) {
         super.installCG(component);
         SessionHeaders.getInstance().registerHeaders(headers);
     }
     
     public void uninstallCG(XCalendar component) {
         super.uninstallCG(component);
         SessionHeaders.getInstance().deregisterHeaders(headers);
     }
 
     public void writeInternal(Device device, XCalendar component) throws java.io.IOException {
         final String idComponent = component.getName();
        final String idValue = idComponent + "val";
        final String idButton = idComponent + "btn";
        final String idContainer = idComponent + "con";
        final String idCalendar = idComponent + "cal";
 
         SFormattedTextField fTextField = component.getFormattedTextField();
         SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
         dateFormat.setTimeZone(component.getTimeZone());
 
         device.print("<table");
         Utils.writeAllAttributes(device, component);
         device.print("><tr><td class=\"tf\" width=\"*\"");
         int oversizePadding = Utils.calculateHorizontalOversize(fTextField, true);
         if (oversizePadding != 0)
             Utils.optAttribute(device, "oversize", oversizePadding);
         device.print('>');
 
         SDimension preferredSize = component.getPreferredSize();
         if (preferredSize != null && preferredSize.getWidth() != null && "auto".equals(preferredSize.getWidth()))
             fTextField.setPreferredSize(SDimension.FULLWIDTH);
         fTextField.setEnabled(component.isEnabled());
         fTextField.write(device);
 
         device.print("\n</td><td class=\"bu\" width=\"1\">\n");
         
         device.print("<input type=\"hidden\" id=\"").print(idValue)
               .print("\" name=\"").print(idValue)
               .print("\" value=\"").print(format(dateFormat, component.getDate()))
               .print("\">\n");
         device.print("<img id=\"").print(idButton)
               .print("\" src=\"").print( component.getEditIcon().getURL() )
               .print("\" />\n");
         device.print("<div class=\"container\" id=\"").print(idContainer)
               .print("\"><div class=\"hd\"></div><div class=\"bd\"><div class=\"calendar\" id=\"")
               .print(idCalendar).print("\"></div></div></div></td>");
 
         writeTableSuffix(device, component);
 
         if (component.isEnabled()) {
             SimpleDateFormat format_months_long = new SimpleDateFormat("MMMMM");
             format_months_long.setTimeZone(component.getTimeZone());
             SimpleDateFormat format_weekdays_short = new SimpleDateFormat("EE");
             format_weekdays_short.setTimeZone(component.getTimeZone());
             
             SStringBuilder script = new SStringBuilder("new wingS.calendar.XCalendar(")
                 .append('"').append(component.getName()).append("\",\"")
                 .append(component.getParentFrame().getName()).append("\", {iframe:false,")
                 .append("months_long:").append(createMonthsString( format_months_long)).append(',')
                 .append("weekdays_short:").append(createWeekdaysString( format_weekdays_short)).append(',')
                 .append("start_weekday:").append((Calendar.getInstance().getFirstDayOfWeek() - 1)).append("}")
                 .append(");");
             
             ScriptManager.getInstance().addScriptListener(new OnHeadersLoadedScript(script.toString(), true));
         }
             
     }
 
     private String createMonthsString(Format format) {
         SStringBuilder stringBuilder = new SStringBuilder();
         stringBuilder.append('[');
         Calendar cal = new GregorianCalendar();
         cal.set(Calendar.MONTH, Calendar.JANUARY);
         for (int x = 0, y = 12; x < y; x++) {
             stringBuilder.append('"');
             stringBuilder.append(format.format(cal.getTime()));
             stringBuilder.append("\",");
             cal.add(Calendar.MONTH, 1);
         }
         stringBuilder.deleteCharAt(stringBuilder.length() - 1);
         stringBuilder.append(']');
         return stringBuilder.toString();
     }
     
     private String createWeekdaysString(Format format) {
         SStringBuilder stringBuilder = new SStringBuilder();
         stringBuilder.append('[');
         Calendar cal = new GregorianCalendar();
         cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
         for (int x = 0, y = 7; x < y; x++) {
             stringBuilder.append('"');
             stringBuilder.append(format.format(cal.getTime()));
             stringBuilder.append("\",");
             cal.add(Calendar.DAY_OF_WEEK, 1);
         }
         stringBuilder.deleteCharAt(stringBuilder.length() - 1);
         stringBuilder.append(']');
         return stringBuilder.toString();
     }
     
     private String format(DateFormat dateFormat, Date date) {
         return date != null ? dateFormat.format(date) : "";
     }
 
     public Update getHiddenUpdate(XCalendar cal, Date date) {
         return new HiddenUpdate(cal, date);
     }
 
     protected static class HiddenUpdate extends AbstractUpdate<XCalendar> {
 
         private Date date;
         private SimpleDateFormat format;
 
         public HiddenUpdate(XCalendar cal, Date date) {
             super(cal);
             this.date = date;
             this.format = new SimpleDateFormat("MM/dd/yyyy");
         }
 
         public Handler getHandler() {
             UpdateHandler handler = new UpdateHandler("value");
             handler.addParameter(component.getName() + "val");
             handler.addParameter(date == null ? "" : format.format(date));
             return handler;
         }
     }
     
 }
