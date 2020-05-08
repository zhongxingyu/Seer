 /**
  * Copyright (C) 2000 - 2009 Silverpeas
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as
  * published by the Free Software Foundation, either version 3 of the
  * License, or (at your option) any later version.
  *
  * As a special exception to the terms and conditions of version 3.0 of
  * the GPL, you may redistribute this Program in connection with Free/Libre
  * Open Source Software ("FLOSS") applications as described in Silverpeas's
  * FLOSS exception.  You should have received a copy of the text describing
  * the FLOSS exception, and it is also available here:
  * "http://repository.silverpeas.com/legal/licensing"
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.silverpeas.SilverpeasSettings.xml.transform;
 
 import java.util.ArrayList;
 import org.jdom.Element;
 import com.silverpeas.file.GestionVariables;
 import java.io.IOException;
 import java.util.Collections;
 import java.util.List;
 import static com.silverpeas.SilverpeasSettings.SilverpeasSettings.*;
 
 /**
  * @author ehugonnet
  */
 public class XmlConfiguration {
 
   private String fileName;
   private List<Parameter> parameters;
 
   public XmlConfiguration(String dir, Element eltConfigFile, GestionVariables gv)
       throws IOException {
     fileName = gv.resolveAndEvalString(dir + eltConfigFile.getAttributeValue(FILE_NAME_ATTRIB));
     parameters = new ArrayList<Parameter>();
     @SuppressWarnings("unchecked")
     List<Element> eltParameters = eltConfigFile.getChildren(PARAMETER_TAG);
     for (Element eltParameter : eltParameters) {
       Parameter parameter = new Parameter(gv.resolveAndEvalString(eltParameter.getAttributeValue(
           PARAMETER_KEY_ATTRIB)), getXmlMode(eltParameter.getAttributeValue(XPATH_MODE_ATTRIB)));
       if (eltParameter.getChildren() != null && !eltParameter.getChildren().isEmpty()) {
         @SuppressWarnings("unchecked")
         List<Element> eltValues = eltParameter.getChildren(VALUE_TAG);
         if (eltValues == null || eltValues.isEmpty()) {
           parameter.addValue(new Value(null, null, gv.resolveAndEvalString(
               eltParameter.getTextTrim()), parameter.getMode()));
         }
         for (Element eltValue : eltValues) {
           String relativePath = eltValue.getAttributeValue(RELATIVE_VALUE_ATTRIB);
           if (relativePath != null && !"".equals(relativePath)) {
             relativePath = gv.resolveAndEvalString(relativePath);
           }
           String location = eltValue.getAttributeValue(VALUE_LOCATION_ATTRIB);
           if (location != null && !"".equals(location)) {
             location = gv.resolveAndEvalString(location);
           }
           parameter.addValue(new Value(location, relativePath, gv.resolveAndEvalString(
              eltValue.getTextTrim()), parameter.getMode()));
         }
       } else {
         parameter.addValue(new Value(null, null, gv.resolveAndEvalString(
             eltParameter.getTextTrim()), parameter.getMode()));
       }
       parameters.add(parameter);
     }
   }
 
   public String getFileName() {
     return fileName;
   }
 
   public List<Parameter> getParameters() {
     return Collections.unmodifiableList(parameters);
   }
 }
