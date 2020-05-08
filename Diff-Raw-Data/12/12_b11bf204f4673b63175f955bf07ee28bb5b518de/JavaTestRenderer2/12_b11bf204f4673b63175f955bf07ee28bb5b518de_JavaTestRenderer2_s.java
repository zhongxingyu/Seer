 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 package org.apache.myfaces.javaloader.componentTest;
 
 import java.io.IOException;
 
 import javax.faces.component.UIComponent;
 import javax.faces.context.FacesContext;
 import javax.faces.context.ResponseWriter;
 import javax.faces.render.FacesRenderer;
 
 import org.apache.myfaces.extensions.scripting.core.util.ReflectUtil;
 import org.apache.myfaces.shared_impl.renderkit.html.HtmlTextareaRendererBase;
 
 /**
  * @author Werner Punz (latest modification by $Author$)
  * @version $Revision$ $Date$
  */
 
 public class JavaTestRenderer2 extends HtmlTextareaRendererBase {
 
    private static final String MSG = "<h2> Hello world from Renderer 2 </h2>";
    private static final String MSG2 = "<h3> hello world second var <h3>";
 
 
     @Override
     public void encodeBegin(FacesContext context, UIComponent component) throws IOException {
         super.encodeBegin(context, component);
         ResponseWriter writer = context.getResponseWriter();
         writer.write(((JavaTestComponent)component).getMyHello());
         writer.write(MSG2);
         writer.write((String) ReflectUtil.executeMethod(component, "getMarker"));
     }
 }
