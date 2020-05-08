 /*
  * Copyright 2004-2012 ICEsoft Technologies Canada Corp.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the
  * License. You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an "AS
  * IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
  * express or implied. See the License for the specific language
  * governing permissions and limitations under the License.
  */
 
 package org.icefaces.ace.component.columngroup;
 
 import org.icefaces.ace.meta.annotation.Component;
 import org.icefaces.ace.meta.annotation.Property;
 import org.icefaces.ace.meta.baseMeta.UIComponentBaseMeta;
 
 
 @Component(
     tagName = "columnGroup",
     componentClass = "org.icefaces.ace.component.columngroup.ColumnGroup",
     generatedClass = "org.icefaces.ace.component.columngroup.ColumnGroupBase",
     extendsClass = "javax.faces.component.UIComponentBase",
     componentType = "org.icefaces.ace.component.ColumnGroup",
     componentFamily = "org.icefaces.ace.ColumnGroup",
     tlddoc = "<p>A grouping of column and row components to furnish table segments specified by the 'type' attribute.</p>" +
              "<p>For more information, see the <a href=\"http://wiki.icefaces.org/display/ICE/ColumnGroup\">ColumnGroup Wiki Documentation</a>.</p>"
 )
 public class ColumnGroupMeta extends UIComponentBaseMeta {
    @Property(tlddoc= "Defines which segment of the table this component and its child columns will be used to render. Valid values are 'header' and 'footer'.")
     private String type;
 }
