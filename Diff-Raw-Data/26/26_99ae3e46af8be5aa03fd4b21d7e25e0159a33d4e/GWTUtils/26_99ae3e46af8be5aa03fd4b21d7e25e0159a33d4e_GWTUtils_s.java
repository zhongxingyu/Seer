 /* 
  * Copyright 2012 Matthias Huebner
  * 
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 package com.gwtao.ui.util.client;
 
 import com.google.gwt.dom.client.DivElement;
 import com.google.gwt.dom.client.Document;
 import com.google.gwt.dom.client.NativeEvent;
 import com.google.gwt.dom.client.ParagraphElement;
 import com.google.gwt.dom.client.Style.Overflow;
 import com.google.gwt.dom.client.Style.Position;
 import com.google.gwt.dom.client.Style.Unit;
 import com.google.gwt.dom.client.Style.Visibility;
 
 public class GWTUtils {
   /**
    * This function calculates scrollbarWidth and scrollbarHeight
    * 
    * 
    * @see http://www.alexandre-gomes.com/?p=115
    */
   public static int[] calcScrollbarSize() {
     ParagraphElement p = Document.get().createPElement();
     p.getStyle().setWidth(100, Unit.PCT);
     p.getStyle().setHeight(200, Unit.PX);
 
     DivElement div = Document.get().createDivElement();
     div.getStyle().setPosition(Position.ABSOLUTE);
     div.getStyle().setLeft(0, Unit.PX);
     div.getStyle().setTop(0, Unit.PX);
     div.getStyle().setWidth(200, Unit.PX);
     div.getStyle().setHeight(150, Unit.PX);
     div.getStyle().setVisibility(Visibility.HIDDEN);
     div.getStyle().setOverflow(Overflow.HIDDEN);
     div.appendChild(p);
 
     Document.get().getBody().appendChild(div);
 
     int w1 = p.getOffsetWidth();
     int h1 = p.getOffsetHeight();
     div.getStyle().setOverflow(Overflow.SCROLL);
     int w2 = p.getOffsetWidth();
     int h2 = p.getOffsetHeight();
     if (w1 == w2)
       w2 = div.getClientWidth();
     if (h1 == h2)
       h2 = div.getClientWidth();
 
     Document.get().getBody().removeChild(div);
 
     int sw = w1 - w2;
     int sh = h1 - h2;
 
     return new int[] {
         sw,
         sh };
   };
 
   public static native void closeBrowser()
   /*-{
 		$wnd.close();
   }-*/;
 
   public static KeyInfo createKeyInfo(NativeEvent nativeEvent) {
     if (nativeEvent != null)
       return new KeyInfo(nativeEvent.getKeyCode(), nativeEvent.getShiftKey(), nativeEvent.getCtrlKey(), nativeEvent.getAltKey());
     else
       return null;
   }
 
   public static EventInfo createEventInfo(NativeEvent nativeEvent) {
     if (nativeEvent != null)
       return new EventInfo(nativeEvent.getClientX(), nativeEvent.getClientY(), nativeEvent.getShiftKey(), nativeEvent.getCtrlKey(), nativeEvent.getAltKey());
     else
       return null;
   }
 }
