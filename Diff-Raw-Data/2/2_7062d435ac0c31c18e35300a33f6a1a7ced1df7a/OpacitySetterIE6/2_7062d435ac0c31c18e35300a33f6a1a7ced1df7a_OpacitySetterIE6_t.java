 /*
  * OpacitySetter.java
  *
  * Created on August 3, 2007, 8:06 PM
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
 
 package com.totsp.gwittir.client.fx.rebind;
 
 import com.google.gwt.user.client.DOM;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.UIObject;
 
 /**
  *
  * @author cooper
  */
public class OpacitySetterIE6 extends OpacitySetter {
     protected static final String ALPHA="alpha(";
     protected static final String OPACITY="opacity=";
     protected static final String NUMBERS="1234567890.";
     
     /** Creates a new instance of OpacitySetter */
     public OpacitySetterIE6() {
     }
     
     public Double getOpacity(UIObject o){
         String str = DOM.getStyleAttribute( o.getElement(), "filter");
         str = parseOrReplace( str, null );
         if( str == null || str.length() == 0 ){
             return new Double( 1.0 );
         } else {
             return new Double( Integer.parseInt( str ) / 100 );
         }
     }
     
     public void setOpacity(UIObject o, Double opacity){
         if( opacity  != null ){
             String filter = DOM.getStyleAttribute( o.getElement(), "filter" );
             if( filter == null ){ 
                 filter = "";
             }
             filter = parseOrReplace( filter, ""+Math.round( opacity.doubleValue() * 100d) );
             DOM.setStyleAttribute( o.getElement(), "filter", filter );
         } else {
             String filter = DOM.getStyleAttribute( o.getElement(), "filter" );
             if( filter == null ){ 
                 filter = "";
             }
             filter = parseOrReplace( filter, "100" );
             Window.alert( "Serting filter "+ filter);
             DOM.setStyleAttribute( o.getElement(), "filter", filter );
         }
     }
     
     static String parseOrReplace(String filter, String replace){
         int start = filter.indexOf( ALPHA );
         if(  start == -1 ){
             if( replace != null ){
                 if( filter == null ){
                     return ALPHA+OPACITY+replace+")";
                 } else {
                     return filter+ALPHA+OPACITY+replace+")";
                 }
             }
             return null;
         }
         int alphaEnd = filter.indexOf( ")", start+ALPHA.length());
         String alphaBlock = filter.substring( start + ALPHA.length(),
                 alphaEnd );
         int opacityStart = alphaBlock.indexOf( OPACITY );
         if( opacityStart == -1 ){
             if( replace != null ){
                 return filter.substring( 0, start + ALPHA.length()) + OPACITY
                 + replace + ","+ alphaBlock + filter.substring(alphaEnd, filter.length() );
             } else {
                 return null;
             }
         }
         opacityStart +=start + ALPHA.length();
         int opacityEnd = opacityStart + OPACITY.length() +1;
         for( ; NUMBERS.indexOf( filter.charAt( opacityEnd ) ) != -1; opacityEnd++ );
         if( replace == null ){
             return filter.substring( opacityStart + OPACITY.length(), opacityEnd );
         } else {
             return filter.substring(0, opacityStart + OPACITY.length() ) + replace + filter.substring( opacityEnd, filter.length() );
         }
         
         
     }
     
     
 }
