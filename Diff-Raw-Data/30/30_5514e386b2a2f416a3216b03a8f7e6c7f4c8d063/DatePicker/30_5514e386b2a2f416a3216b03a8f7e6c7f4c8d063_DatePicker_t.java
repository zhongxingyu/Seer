 /*
  * DatePicker.java
  *
  * Created on October 31, 2007, 10:04 PM
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
  */
 
 package com.totsp.gwittir.client.ui.calendar;
 
 import com.google.gwt.user.client.ui.ClickListener;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.user.client.ui.Widget;
import com.totsp.gwittir.client.beans.Converter;
 import com.totsp.gwittir.client.log.Level;
 import com.totsp.gwittir.client.ui.AbstractBoundWidget;
 import com.totsp.gwittir.client.ui.Label;
 import com.totsp.gwittir.client.ui.ListBox;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.util.ArrayList;
import java.util.Collection;
 import java.util.Date;
 import java.util.List;
 
 /**
  * A wrapper around a Calendar that provides Month/Year navigation for selection.
  * @author cooper
  */
 public class DatePicker extends AbstractBoundWidget<Date> implements
         SourcesCalendarDrawEvents, SourcesCalendarEvents {
     private static final String VALUE_PROPERTY_NAME = "value";
     
     private Calendar calendar = new Calendar();
     private HorizontalPanel hp = new HorizontalPanel();
     private List years = new ArrayList();
     private ListBox year = new ListBox();
     private ListBox month;
     /** Creates a new instance of DatePicker */
     public DatePicker() {
         VerticalPanel vp = new VerticalPanel();
         Label back = new Label("<<");
         back.addClickListener( new ClickListener(){
             public void onClick(Widget sender) {
                 Date current = calendar.getRenderDate();
                 if( current.getMonth() -1 >= 0 ){
                     current = new Date( current.getYear(), current.getMonth() - 1 , 1);
                 } else {
                     LOG.log( Level.SPAM, "current Year"+ current.getYear(), null );
                     current = new Date(current.getYear() - 1, 12 , 0 );
                     LOG.log( Level.SPAM, "new date"+ current.getYear(), null );
                 }
                 LOG.log(Level.SPAM, current.toString(), null);
                 calendar.setRenderDate( current );
             }
         });
         hp.add( back );
         month = new ListBox();
         final ArrayList months = new ArrayList();
        for( String monthString : Calendar.MONTHS_OF_YEAR_SHORT ){
            months.add(monthString);
         }
         month.setOptions(months);
         month.addPropertyChangeListener(DatePicker.VALUE_PROPERTY_NAME, new PropertyChangeListener(){
             public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                 Date current = calendar.getRenderDate();
                 current = new Date( current.getYear(),
                        indexOf( Calendar.MONTHS_OF_YEAR_SHORT, ""+ Converter.FROM_COLLECTION_CONVERTER.convert((Collection)propertyChangeEvent.getNewValue()) )
                         ,1);
                 calendar.setRenderDate( current );
             }
             
         });
         
         this.updateMonth();
         hp.add( this.month );
         this.updateYears();
         this.year.setValue( year.single(Integer.toString( calendar.getRenderDate().getYear() + 1900 )) );
         hp.add( this.year );
         this.year.addPropertyChangeListener(VALUE_PROPERTY_NAME, new PropertyChangeListener(){
             public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                 if( propertyChangeEvent.getNewValue() == null ){
                     return;
                 }
                 Date current = calendar.getRenderDate();
                current = new Date( Integer.parseInt( ((Collection)propertyChangeEvent.getNewValue()).iterator().next().toString() ) -1900, current.getMonth(), 1 );
                 LOG.log(Level.SPAM, current.toString(), null );
                 calendar.setRenderDate( current );
             }
             
         });
         final Label next = new Label(">>");
         next.addClickListener( new ClickListener(){
             public void onClick(Widget sender) {
                 Date current = calendar.getRenderDate();
                 if( current.getMonth() + 1 < 12 ){
                     current = new Date( current.getYear(), current.getMonth() + 1 , 1);
                 } else {
                     LOG.log( Level.SPAM, "current Year"+ current.getYear(), null );
                     current = new Date(current.getYear() + 1, 1, 0 );
                     LOG.log( Level.SPAM, "new date"+ current.getYear(), null );
                 }
                 LOG.log(Level.SPAM, current.toString(), null);
                 calendar.setRenderDate( current );
             }
         });
         hp.add( next );
         vp.add( hp );
         vp.add( this.calendar );
         this.calendar.addPropertyChangeListener("renderDate", new PropertyChangeListener(){
             public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                 updateYears();
                 updateMonth();
             }
             
         });
         this.calendar.addPropertyChangeListener(VALUE_PROPERTY_NAME, new PropertyChangeListener(){
             public void propertyChange(PropertyChangeEvent evt) {
                 changes.firePropertyChange(VALUE_PROPERTY_NAME, evt.getOldValue(), evt.getNewValue()  );
             }
             
         });
         super.initWidget( vp );
         this.setStyleName("gwittir-DatePicker");
         
     }
     
     /**
      * Returns the Date value of this Calendar.
      * @return Date value of this Calendar.
      */
     public Date getValue() {
         return this.calendar.getValue();
     }
     
     /**
      * Sets Date value of this Calendar.
      * @param value Date value of this Calendar.
      */
     public void setValue(Date value) {
         this.calendar.setValue( value );
     }
     
     /**
      * 
      * @param cdl 
      */
     public void addCalendarDrawListener(CalendarDrawListener cdl) {
         this.calendar.addCalendarDrawListener( cdl );
     }
     
     /**
      * 
      * @param cdl 
      */
     public void removeCalendarDrawListener(CalendarDrawListener cdl) {
         this.calendar.removeCalendarDrawListener( cdl );
     }
     
     /**
      * 
      * @return 
      */
     public CalendarDrawListener[] getCalendarDrawListeners() {
         return this.calendar.getCalendarDrawListeners();
     }
     
     /**
      * 
      * @param l 
      */
     public void addCalendarListener(CalendarListener l) {
         this.calendar.addCalendarListener( l );
     }
     
     /**
      * 
      * @param l 
      */
     public void removeCalendarListener(CalendarListener l) {
         this.calendar.removeCalendarListener(l);
     }
     
     /**
      * 
      * @return 
      */
     public CalendarListener[] getCalendarListeners() {
         return this.calendar.getCalendarListeners();
     }
     
     private void updateYears(){
         this.years.clear();
         int year = this.calendar.getRenderDate().getYear() + 1900;
         for( int i = year - 20; i < year + 20; i++){
             this.years.add( Integer.toString( i ) );
         }
         this.year.setOptions(this.years);
         this.year.setValue( this.year.single(Integer.toString( this.calendar.getRenderDate().getYear() + 1900 )) );
         LOG.log( Level.SPAM, Integer.toString( this.calendar.getRenderDate().getYear() + 1900 ), null );
         
     }
     private void updateMonth(){
         this.month.setValue( this.month.single(Calendar.MONTHS_OF_YEAR_SHORT[ this.calendar.getRenderDate().getMonth() ]));
     }
     private int indexOf( final String[] values, final String check ){
         int index = -1;
         for( int i=0; i < values.length; i++ ){
             if( values[i].equals( check ) ){
                 index = i;
                 break;
             }
             
         }
         return index;
     }
     
     /**
      * Gets the Month/Year date the calendar should be rendering.
      * @return Date currently rendered around.
      */
     public Date getRenderDate() {
         return this.calendar.getRenderDate();
     }
 
     /**
      * Sets the Month/Year date the calendar should be rendering.
      * @param renderDate Month/Year date the calendar should be rendering.
      */
     public void setRenderDate(Date renderDate) {
         this.calendar.setRenderDate( renderDate );
     }
     
 }
