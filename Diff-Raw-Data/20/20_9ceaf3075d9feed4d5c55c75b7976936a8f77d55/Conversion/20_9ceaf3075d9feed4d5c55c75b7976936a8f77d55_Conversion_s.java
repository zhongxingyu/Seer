 package eionet.webq.dto;
 
 /*
  * The contents of this file are subject to the Mozilla Public
  * License Version 1.1 (the "License"); you may not use this file
  * except in compliance with the License. You may obtain a copy of
  * the License at http://www.mozilla.org/MPL/
  *
  * Software distributed under the License is distributed on an "AS
  * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
  * implied. See the License for the specific language governing
  * rights and limitations under the License.
  *
  * The Original Code is Web Questionnaires 2
  *
  * The Initial Owner of the Original Code is European Environment
  * Agency. Portions created by TripleDev are Copyright
  * (C) European Environment Agency.  All Rights Reserved.
  *
  * Contributor(s):
  *        Anton Dmitrijev
  */
 
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlRootElement;
 
 /**
  * Class represents conversion for xml schema.
  */
 @XmlRootElement(name = "conversion")
 public class Conversion {
     /**
      * Conversion id.
      */
     @XmlElement(name = "convert_id")
     private int id;
     /**
      * Conversion output type.
      */
     @XmlElement(name = "content_type_out")
     private String outputType;
     /**
      * Xml schema for which this conversion is valid.
      */
     @XmlElement(name = "xml_schema")
     private String xmlSchema;
     /**
      * Xsl file name.
      */
     @XmlElement(name = "xsl")
     private String xslName;
     /**
     * Resulting type (e.g. HTML, RDF)
      */
     @XmlElement(name = "result_type")
     private String resultType;
     /**
     * Description of this conversion
      */
     @XmlElement(name = "description")
     private String description;
 
     public int getId() {
         return id;
     }
 
     public String getOutputType() {
         return outputType;
     }
 
     public String getXmlSchema() {
         return xmlSchema;
     }
 
     public String getXslName() {
         return xslName;
     }
 
     public String getResultType() {
         return resultType;
     }
 
     public String getDescription() {
         return description;
     }
 }
