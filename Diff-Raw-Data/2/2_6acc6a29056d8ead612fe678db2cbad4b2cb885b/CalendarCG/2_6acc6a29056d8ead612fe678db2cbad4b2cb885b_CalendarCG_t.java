 /*
  * CalendarCG.java
  *
  * Created on 12. Juni 2006, 09:03
  *
  * To change this template, choose Tools | Template Manager
  * and open the template in the editor.
  */
 
 package org.wingx.plaf.css;
 
 import org.wings.*;
 import org.wings.header.Header;
 import org.wings.header.SessionHeaders;
 import org.wings.plaf.Update;
 import org.wings.plaf.css.AbstractComponentCG;
 import org.wings.plaf.css.AbstractUpdate;
 import org.wings.plaf.css.UpdateHandler;
 import org.wings.plaf.css.Utils;
 import org.wings.plaf.css.script.OnHeadersLoadedScript;
 import org.wings.session.Browser;
 import org.wings.session.BrowserType;
 import org.wings.session.ScriptManager;
 import org.wings.session.SessionManager;
 import org.wings.util.SStringBuilder;
 import org.wingx.XCalendar;
 
 import java.text.DateFormat;
 import java.text.Format;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.List;
 import java.util.Collections;
 
 /**
  *
  *  * @author <a href="mailto:e.habicht@thiesen.com">Erik Habicht</a>
  */
 public class CalendarCG extends AbstractComponentCG implements org.wingx.plaf.CalendarCG {
 
     protected final static List<Header> headers;
 
     static {
         String[] images = new String [] {
             "org/wingx/calendar/calcd.gif",
             "org/wingx/calendar/cally.gif",
             "org/wingx/calendar/calry.gif"
         };
 
         for ( int x = 0, y = images.length ; x < y ; x++ ) {
             new SResourceIcon(images[x]).getId(); // hack to externalize
         }
 
         List<Header> headerList = new ArrayList<Header>();
         headerList.add(Utils.createExternalizedCSSHeaderFromProperty(Utils.CSS_YUI_ASSETS_CALENDAR));   
         headerList.add(Utils.createExternalizedJSHeaderFromProperty(Utils.JS_YUI_CALENDAR));
         headerList.add(Utils.createExternalizedJSHeader("org/wingx/calendar/xcalendar.js"));
         headers = Collections.unmodifiableList(headerList);
     }
 
     public CalendarCG() {
     }
        
     public void installCG(final SComponent component) {
         super.installCG(component);
         SessionHeaders.getInstance().registerHeaders(headers);
     }
     
     public void uninstallCG(SComponent component) {
         super.uninstallCG(component);
         SessionHeaders.getInstance().deregisterHeaders(headers);
     }
 
     public void writeInternal(org.wings.io.Device device, org.wings.SComponent _c )
     throws java.io.IOException {
 
         final XCalendar component = (org.wingx.XCalendar) _c;
         
         final String id_hidden = "hidden" + component.getName();
         final String id_button = "button" + component.getName();
         final String id_cal = "cal"+component.getName();
 
         SFormattedTextField fTextField = component.getFormattedTextField();
 
         SimpleDateFormat dateFormat  = new SimpleDateFormat("MM/dd/yyyy");
         dateFormat.setTimeZone( component.getTimeZone() );
 
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
         fTextField.setEnabled( component.isEnabled() );
         fTextField.write(device);
 
         device.print("\n</td><td class=\"b\" width=\"1\">\n");
         
         device.print("<input type=\"hidden\" id=\"").print(id_hidden)
               .print("\" name=\"").print(id_hidden)
               .print("\" value=\"").print( format(dateFormat, component.getDate() ) )
               .print("\">\n");
         
         device.print("<div style=\"display:inline;position:absolute;\" id=\"r").print(id_cal)
               .print("\"></div>");
 
        device.print("<img id=\"").print(id_button)
               .print("\" src=\"").print( component.getEditIcon().getURL() )
               .print("\" />\n");
         
         String position = "fixed";
         Browser browser = SessionManager.getSession().getUserAgent();
         if (browser.getBrowserType() == BrowserType.IE && browser.getMajorVersion() < 7) {
             position = "absolute";
         }
      
         device.print("<div style=\"display:none;position:").print(position)
               .print(";z-index:1001\" id=\"").print(id_cal).print("\"></div>");
 
         writeTableSuffix(device, component);
 
         if ( component.isEnabled() ) {
             
             SimpleDateFormat format_months_long     = new SimpleDateFormat("MMMMM");
             format_months_long.setTimeZone( component.getTimeZone() );
             
             SimpleDateFormat format_weekdays_short  = new SimpleDateFormat("EE");
             format_weekdays_short.setTimeZone( component.getTimeZone() );
             
             SStringBuilder newXCalendar = new SStringBuilder("new YAHOO.widget.XCalendar(");
             newXCalendar.append('"').append( id_cal ).append("\",");
             newXCalendar.append("{close:true,");
             newXCalendar.append("months_long:").append(createMonthsString( format_months_long ) ).append(',');
             newXCalendar.append("weekdays_short:").append(createWeekdaysString( format_weekdays_short ) ).append(',');
             newXCalendar.append("start_weekday:").append( (Calendar.getInstance().getFirstDayOfWeek()-1) ).append("},");
             newXCalendar.append('"').append( component.getName() ).append("\",");
             newXCalendar.append('"').append( id_button ).append("\",");
             newXCalendar.append('"').append( id_hidden ).append('"');
             newXCalendar.append( ");");
             
             ScriptManager.getInstance().addScriptListener(new OnHeadersLoadedScript( newXCalendar.toString(), true));
             
         }
             
     }
 
     private String createMonthsString ( Format format ) {
         SStringBuilder stringBuilder = new SStringBuilder();
         stringBuilder.append('[');
         Calendar cal = new GregorianCalendar();
         cal.set( Calendar.MONTH, cal.JANUARY );
         for ( int x = 0, y = 12; x < y ; x++ ) {
             stringBuilder.append('"');
             stringBuilder.append( format.format( cal.getTime() ) );
             stringBuilder.append( "\",");
             cal.add( Calendar.MONTH, 1 );
         }
         stringBuilder.deleteCharAt( stringBuilder.length()-1 );
         stringBuilder.append(']');    
         return stringBuilder.toString();
     }
     
     private String createWeekdaysString ( Format format ) {
         SStringBuilder stringBuilder = new SStringBuilder();
         stringBuilder.append('[');
         Calendar cal = new GregorianCalendar();
         cal.set( Calendar.DAY_OF_WEEK, Calendar.SUNDAY );
         for ( int x = 0, y = 7; x < y ; x++ ) {
             stringBuilder.append('"');
             stringBuilder.append( format.format( cal.getTime() ) );
             stringBuilder.append( "\",");
             cal.add( Calendar.DAY_OF_WEEK, 1 );
         }
         stringBuilder.deleteCharAt( stringBuilder.length()-1 );
         stringBuilder.append(']');    
         return stringBuilder.toString();
     }
     
     private String format(DateFormat dateFormat, Date date) {
         return date != null ? dateFormat.format( date ) : "";
     }
 
     public Update getHiddenUpdate(XCalendar cal, Date date) {
     	return new HiddenUpdate(cal, date);
     }
 
     protected static class HiddenUpdate extends AbstractUpdate {
 
         private Date date;
 
         public HiddenUpdate(XCalendar cal, Date date) {
             super(cal);
             this.date = date;
         }
 
         public Handler getHandler() {
             UpdateHandler handler = new UpdateHandler("value");
             handler.addParameter("hidden"+component.getName());
             final SimpleDateFormat dateFormatForHidden  = new SimpleDateFormat("MM/dd/yyyy");
             handler.addParameter(date == null ? "" : dateFormatForHidden.format( date ) );
             return handler;
         }
 
     }
     
 }
