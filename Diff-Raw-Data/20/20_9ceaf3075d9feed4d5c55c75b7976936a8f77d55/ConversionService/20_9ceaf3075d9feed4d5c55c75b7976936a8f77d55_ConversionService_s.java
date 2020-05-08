 package eionet.webq.service;
 
 import eionet.webq.dto.Conversion;
 import eionet.webq.dto.UploadedXmlFile;
 
import java.util.Collection;
 import java.util.List;
 
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
 
 /**
  * Service provides conversion information for xml schemas.
  */
 public interface ConversionService {
     /**
      * Convert xml to specified format.
      *
      * @param fileContent file content and name
      * @param conversionId conversion id for this file
      * @return conversion result as string
      */
     byte[] convert(UploadedXmlFile fileContent, int conversionId);
 
     /**
      * List all available conversions.
      *
      * @param schema xml schema
      * @return collection of available conversions
      */
     List<Conversion> conversionsFor(String schema);
 }
