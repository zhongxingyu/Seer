 /*
  * CalendarCG.java
  *
  * Created on 12. Juni 2006, 09:03
  *
  * To change this template, choose Tools | Template Manager
  * and open the template in the editor.
  */
 
 package org.wingx.plaf.css;
 
 import java.text.*;
 
 import org.wings.*;
 import java.util.*;
 import org.wings.event.SParentFrameEvent;
 import org.wings.event.SParentFrameListener;
 
 import org.wings.plaf.css.*;
 import org.wings.header.*;
 import org.wings.plaf.Update;
 import org.wings.plaf.css.script.OnHeadersLoadedScript;
 import org.wings.session.ScriptManager;
 import org.wings.util.SStringBuilder;
 import org.wingx.XCalendar;
 
 /**
  *
  *  * @author <a href="mailto:e.habicht@thiesen.com">Erik Habicht</a>
  */
 public class CalendarCG extends AbstractComponentCG implements org.wingx.plaf.CalendarCG {
 
     protected final List<Header> headers = new ArrayList<Header>();
 
     static {
         String[] images = new String [] {
             "org/wings/js/yui/calendar/assets/callt.gif",
             "org/wings/js/yui/calendar/assets/calrt.gif",
             "org/wings/js/yui/calendar/assets/calx.gif"
         };
 
         for ( int x = 0, y = images.length ; x < y ; x++ ) {
             SIcon icon = new SResourceIcon(images[x]);
             icon.getURL(); // hack to externalize
         }
     }
 
     public CalendarCG() {
         headers.add(Utils.createExternalizedCSSHeaderFromProperty(Utils.CSS_YUI_CALENDAR));
         headers.add(Utils.createExternalizedJSHeaderFromProperty(Utils.JS_YUI_YAHOO));
         headers.add(Utils.createExternalizedJSHeaderFromProperty(Utils.JS_YUI_EVENT));
         headers.add(Utils.createExternalizedJSHeaderFromProperty(Utils.JS_YUI_DOM));
         headers.add(Utils.createExternalizedJSHeader("org/wingx/calendar/yuicalendar.js"));
         headers.add(Utils.createExternalizedJSHeaderFromProperty(Utils.JS_YUI_CALENDAR));
     }
        
     public void installCG(final SComponent comp) {
         super.installCG(comp);
         SessionHeaders.getInstance().registerHeaders(headers);
     }
 
     public void writeInternal(org.wings.io.Device device, org.wings.SComponent _c )
     throws java.io.IOException {
 
         final XCalendar component = (org.wingx.XCalendar) _c;
         
         final String id_hidden = "hidden" + component.getName();
         final String id_button = "button" + component.getName();
         final String id_clear = "clear" + component.getName();
         final String id_cal = "cal"+component.getName();
 
         SFormattedTextField fTextField = component.getFormattedTextField();
 
         SimpleDateFormat dateFormat  = new SimpleDateFormat("MM/dd/yyyy");
         dateFormat.setTimeZone( component.getTimeZone() );
 
         device.print("<table");
         writeAllAttributes(device, component);
         device.print("><tr><td class=\"tf\"");
 
         int oversizePadding = Utils.calculateHorizontalOversize(fTextField, true);
         //oversizePadding += RenderHelper.getInstance(component).getHorizontalLayoutPadding();
 
         if (oversizePadding != 0)
             Utils.optAttribute(device, "oversize", oversizePadding);
         device.print(">");
 
         fTextField.setEnabled( component.isEnabled() );
         fTextField.write(device);
 
         device.print("\n</td><td class=\"b\">\n");
 
         device.print("<input type=\"hidden\" id=\""+id_hidden+"\" name=\""+id_hidden+"\" value=\""+ format(dateFormat, component.getDate() )+"\">\n");
         device.print("<img class=\"XCalendarButton\" id=\""+id_button+"\" src=\""+component.getEditIcon().getURL()+"\" />\n");
        device.print("<div style=\"display:none;position:absolute;z-index:1001\" id=\""+id_cal+"\"></div>");
 
         writeTableSuffix(device, component);
 
         SimpleDateFormat format_months_long     = new SimpleDateFormat("MMMMM");
         format_months_long.setTimeZone( component.getTimeZone() );
         
         SimpleDateFormat format_weekdays_short  = new SimpleDateFormat("EE");
         format_weekdays_short.setTimeZone( component.getTimeZone() );
 
         SStringBuilder newXCalendar = new SStringBuilder("new wingS.XCalendar(");
             newXCalendar.append("\"").append( component.getName() ).append("\",");
             newXCalendar.append("\"").append( id_cal ).append("\",");
             newXCalendar.append("\"").append( id_button ).append("\",");
             newXCalendar.append("\"").append( id_hidden ).append("\",");
             newXCalendar.append( createMonthsString( format_months_long ) ).append(",");
             newXCalendar.append( createWeekdaysString( format_weekdays_short ) ).append(",");
             newXCalendar.append( (Calendar.getInstance().getFirstDayOfWeek()-1) );
             newXCalendar.append( ");");
         
         ScriptManager.getInstance().addScriptListener(new OnHeadersLoadedScript( newXCalendar.toString(), true));
     }
 
     private String createMonthsString ( Format format ) {
         SStringBuilder stringBuilder = new SStringBuilder();
         stringBuilder.append( "[" );
         Calendar cal = new GregorianCalendar();
         cal.set( Calendar.MONTH, cal.JANUARY );
         for ( int x = 0, y = 12; x < y ; x++ ) {
             stringBuilder.append( "\"");
             stringBuilder.append( format.format( cal.getTime() ) );
             stringBuilder.append( "\",");
             cal.add( Calendar.MONTH, 1 );
         }
         stringBuilder.deleteCharAt( stringBuilder.length()-1 );
         stringBuilder.append( "]" );    
         return stringBuilder.toString();
     }
     
     private String createWeekdaysString ( Format format ) {
         SStringBuilder stringBuilder = new SStringBuilder();
         stringBuilder.append( "[" );
         Calendar cal = new GregorianCalendar();
         cal.set( Calendar.DAY_OF_WEEK, Calendar.SUNDAY );
         for ( int x = 0, y = 7; x < y ; x++ ) {
             stringBuilder.append( "\"");
             stringBuilder.append( format.format( cal.getTime() ) );
             stringBuilder.append( "\",");
             cal.add( Calendar.DAY_OF_WEEK, 1 );
         }
         stringBuilder.deleteCharAt( stringBuilder.length()-1 );
         stringBuilder.append( "]" );    
         return stringBuilder.toString();
     }
     
     private String format(DateFormat dateFormat, Date date) {
         if (date == null)
             date = new Date();
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
