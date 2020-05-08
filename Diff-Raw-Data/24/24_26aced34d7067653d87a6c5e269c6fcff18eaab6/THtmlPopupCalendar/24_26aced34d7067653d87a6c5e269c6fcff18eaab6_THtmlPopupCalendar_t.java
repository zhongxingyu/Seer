 /*
  * Copyright 2004-2008 the Seasar Foundation and the Others.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
  * either express or implied. See the License for the specific language
  * governing permissions and limitations under the License.
  */
 package org.seasar.teeda.extension.component.html;
 
 import java.io.IOException;
 import java.util.Locale;
 
 import javax.faces.context.FacesContext;
 import javax.faces.convert.DateTimeConverter;
 import javax.faces.internal.RenderPreparable;
 import javax.faces.internal.RenderPreparableUtil;
 
 import org.seasar.framework.util.DateConversionUtil;
 import org.seasar.teeda.extension.convert.TDateTimeConverter;
 
 /**
  * @author higa
  */
public class THtmlPopupCalendar extends THtmlInputText implements
         RenderPreparable {
 
     public static final String COMPONENT_FAMILY = "org.seasar.teeda.extension.HtmlPopupCalendar";
 
     public static final String COMPONENT_TYPE = "org.seasar.teeda.extension.HtmlPopupCalendar";
 
     public static final String DEFAULT_RENDERER_TYPE = "org.seasar.teeda.extension.HtmlPopupCalendar";
 
     private String datePattern = DateConversionUtil.getY4Pattern(Locale
             .getDefault());
 
     public THtmlPopupCalendar() {
         setRendererType(DEFAULT_RENDERER_TYPE);
         TDateTimeConverter converter = new TDateTimeConverter();
         converter.setPattern(datePattern);
         setConverter(converter);
     }
 
     public String getFamily() {
         return COMPONENT_FAMILY;
     }
 
     public String getDatePattern() {
         return datePattern;
     }
 
     public void setDatePattern(String datePattern) {
         this.datePattern = datePattern;
         ((DateTimeConverter) getConverter()).setPattern(datePattern);
     }
 
     public void preEncodeBegin(final FacesContext context) throws IOException {
         RenderPreparableUtil.encodeBeforeForRenderer(context, this);
     }
 
     public void postEncodeEnd(final FacesContext context) throws IOException {
     }
 
     public Object saveState(FacesContext context) {
         Object values[] = new Object[2];
         values[0] = super.saveState(context);
         values[1] = datePattern;
         return ((Object) (values));
     }
 
     public void restoreState(FacesContext context, Object state) {
         Object values[] = (Object[]) state;
         super.restoreState(context, values[0]);
         datePattern = (String) values[1];
     }
 }
