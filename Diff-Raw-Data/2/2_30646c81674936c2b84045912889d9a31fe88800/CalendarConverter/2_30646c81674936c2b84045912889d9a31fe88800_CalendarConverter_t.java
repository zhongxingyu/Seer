 /*
  * TnT - Things and tasks to do
  * Licenced under General Public Licence v3 (GPLv3)
  * newInstance.org, 2012
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.newinstance.tnt.utility;
 
 import javax.faces.component.UIComponent;
 import javax.faces.context.FacesContext;
 import javax.faces.convert.Converter;
 import javax.faces.convert.FacesConverter;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 
 /**
  * Converts a calendar object to string value and vice versa.
  *
  * @author mwalter
  */
 @FacesConverter(value = "calendarConverter")
 public final class CalendarConverter implements Converter {
 
     public Object getAsObject(final FacesContext facesContext, final UIComponent uiComponent, final String value) {
         if (value == null || value.isEmpty()) {
             return null;
         }
 
        final String[] date = value.split("\\.");
         final Calendar calendar = Calendar.getInstance();
         calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(date[0]));
         // month starts with index 0
         calendar.set(Calendar.MONTH, Integer.parseInt(date[1]) - 1);
         calendar.set(Calendar.YEAR, Integer.parseInt(date[2]));
         calendar.set(Calendar.HOUR, 0);
         calendar.set(Calendar.MINUTE, 0);
         calendar.set(Calendar.SECOND, 0);
         calendar.set(Calendar.MILLISECOND, 0);
         return calendar;
     }
 
     public String getAsString(final FacesContext facesContext, final UIComponent uiComponent, final Object value) {
         if (value != null && value instanceof Calendar) {
             final Calendar calendar = (Calendar) value;
             return new SimpleDateFormat("dd.MM.yyyy").format(calendar.getTime());
         }
         return "";
     }
 }
