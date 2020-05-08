 /*
  * Copyright (c) 2007-2009. Members of the EGEE Collaboration.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  * 
 * $Id: FunctionalPatternBuilder.java,v 1.11 2009/01/15 12:29:14 vtschopp Exp $
  */
 package org.glite.slcs.dn.impl;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeMap;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.glite.slcs.SLCSConfigurationException;
 import org.glite.slcs.SLCSException;
 import org.glite.slcs.ServiceException;
 import org.glite.slcs.config.SLCSServerConfiguration;
 import org.glite.slcs.util.Utils;
 
 /**
  * FunctionalPatternBuilder extends the SimplePatternBuilder by adding
  * functions. Your pattern can now contains the following functions:
  * <p>
  * Available pattern functions:<br>
  * <ul>
  * <li><code>mappedValue(attributeName)</code>: returns the defined mapped
  * value of the given attribute name, or simply the attribute value if no
  * mapping is defined.
  * <li><code>hashValue(attributeName)</code>: returns a hash of the
  * attribute value.
  * </ul>
  * TODO: document SLCSServerConfiguration parameters.
  * 
  * @author Valery Tschopp &lt;tschopp@switch.ch&gt;
 * @version $Revision: 1.11 $
  */
 public class FunctionalPatternBuilder extends SimplePatternBuilder {
 
     /** Logging */
     private static Log LOG = LogFactory.getLog(FunctionalPatternBuilder.class);
 
     /**
      * MappedValues Map(attributeName,Map(attributeValue,mappedValue))
      */
     private Map mappedValues_ = null;
 
     /*
      * (non-Javadoc)
      * 
      * @see org.glite.slcs.dn.impl.SimplePatternBuilder#init(org.glite.slcs.config.SLCSServerConfiguration)
      */
     public void init(SLCSServerConfiguration config)
             throws SLCSConfigurationException {
         super.init(config);
 
         // create the mapping map
         mappedValues_ = new HashMap();
 
         // look for all attributeNames mapped
         List mappedAttributeNames = config.getList(SLCSServerConfiguration.COMPONENTSCONFIGURATION_PREFIX
                 + ".DNBuilder.MappedValues[@attributeName]");
         LOG.info("DNBuilder.MappedValues[@attributeName]="
                 + mappedAttributeNames);
         Iterator attributeNames = mappedAttributeNames.iterator();
         for (int i = 0; attributeNames.hasNext(); i++) {
             String attributeName = (String) attributeNames.next();
             List mappedAttributeValues = config.getList(SLCSServerConfiguration.COMPONENTSCONFIGURATION_PREFIX
                     + ".DNBuilder.MappedValues("
                     + i
                     + ").MappedValue[@attributeValue]");
             if (LOG.isDebugEnabled()) {
                 LOG.debug("DNBuilder.MappedValues[" + attributeName
                         + "].MappedValue[@attributeValue]="
                         + mappedAttributeValues);
             }
             // create the mapping for these values
             Map attributeValueMappings = new TreeMap();
             // look for all attributeValues mapped
             Iterator attributeValues = mappedAttributeValues.iterator();
             for (int j = 0; attributeValues.hasNext(); j++) {
                 String attributeValue = (String) attributeValues.next();
                 String attributeValueMapping = config.getString(SLCSServerConfiguration.COMPONENTSCONFIGURATION_PREFIX
                         + ".DNBuilder.MappedValues("
                         + i
                         + ").MappedValue("
                         + j
                         + ")");
                 if (LOG.isDebugEnabled()) {
                     LOG.debug("DNBuilder.MappedValues[" + attributeName
                             + "].MappedValue[" + attributeValue + "]="
                             + attributeValueMapping);
                 }
                 attributeValueMappings.put(attributeValue,
                         attributeValueMapping);
             }
             LOG.info("DNBuilder.MappedValues[" + attributeName + "]="
                     + attributeValueMappings);
             mappedValues_.put(attributeName, attributeValueMappings);
         }
 
     }
 
     /**
      * Create a subject DN based on the string pattern configured in
      * SLCSServerConfiguration.
      * <p>
      * The resulting DN will be validated and normalized.
      * <p>
      * Available pattern functions:<br>
      * <ul>
      * <li>mappedValue(attributeName,attributeValue): returns the human
      * readable value of the given attribute name, or <code>null</code> if no
      * mapping is defined.
      * <li>hashValue(attributeName): returns a hash of the attribute value.
      * </ul>
      * 
      * @see org.glite.slcs.dn.DNBuilder#createDN(java.util.Map)
      */
     public String createDN(Map attributes) throws SLCSException {
         // parse pattern and match with attributes
         String dn = getPattern();
         Set attributeNames = attributes.keySet();
         Iterator names = attributeNames.iterator();
         while (names.hasNext()) {
             // get attribute name and value
             String name = (String) names.next();
             String value = (String) attributes.get(name);
 
             // BUG FIX: check multi-value and get only first one
             if (!name.equals("UserAgent") && value.indexOf(';') != -1) {
                 String multiValue = value;
                 int idx = multiValue.indexOf(';');
                 value = multiValue.substring(0, idx);
                 value = value.trim();
                 LOG.warn("Attribute " + name + " is multi-valued: "
                         + multiValue + ". Using only the first value: " + value);
                 if (value.length() <= 0) {
                     throw new ServiceException(
                             "Empty multi-valued attribute first value: " + name
                                     + "=" + value);
                 }
             }            
             // variable placeholder
             String placeholder = "${" + name + "}";
             // first look for functions hashValue(${attributeName}) and
             // mappedValue(${attributeName})
             String matchHashValueFunction = ".*hashValue\\( *\\$\\{" + name
                     + "\\} *\\).*";
             String matchMappedValueFunction = ".*mappedValue\\( *\\$\\{" + name
                     + "\\} *\\).*";
             if (getPattern().matches(matchHashValueFunction)) {
                 String replace = "hashValue\\( *\\$\\{" + name + "\\} *\\)";
                 // replace value with hash code
                 String hashValue = hashValue(value);
                 if (LOG.isDebugEnabled()) {
                     LOG.debug("DNPattern replace regex: " + replace + " by: "
                             + hashValue);
                 }
                 dn = dn.replaceAll(replace, hashValue);
             }
             else if (getPattern().matches(matchMappedValueFunction)) {
                 String replace = "mappedValue\\( *\\$\\{" + name + "\\} *\\)";
                 // replace value with mapped value
                 String mappedValue = mappedValue(name, value);
                 if (mappedValue == null) {
                     LOG.error("The name-value pair is not mapped: " + name
                             + " = " + value);
                     throw new ServiceException(
                             "Invalid Shibboleth attribute: The name-value pair is not mapped: "
                                     + name + " = " + value);
                 }
 
                 if (LOG.isDebugEnabled()) {
                     LOG.debug("DNPattern replace regex: " + replace + " by: "
                             + mappedValue);
                 }
                 dn = dn.replaceAll(replace, mappedValue);
             }
             else if (getPattern().indexOf(placeholder) != -1) {
                 // replace accentuated chars
                 String filteredValue = Utils.filterUnicodeAccentuedString(value);
                 // BUG FIX: escape value containing: ',', '=', '+', '<', '>', '#' or ';'
                 // to generate valid DN
                 String escapedValue = Utils.escapeAttributeValue(filteredValue);
                 // replace placeholder with attribute value: REGEX 1.4
                 String replace = "\\$\\{" + name + "\\}";
                 if (LOG.isDebugEnabled()) {
                     LOG.debug("DNPattern replace regex: " + replace + " by: "
                             + escapedValue);
                 }
                dn = dn.replaceAll(replace, escapedValue);
             }
         }
 
         if (dn.indexOf("${") != -1) {
             LOG.error("DN still contains not substitued placeholders: " + dn);
             throw new ServiceException(
                     "Missing or wrong Shibboleth attributes: DN still contains placeholders: "
                             + dn);
         }
         if (LOG.isDebugEnabled()) {
             LOG.debug("Raw DN: " + dn);
         }
         // try to validate and normalize the DN
         String normalizedDN = validateDN(dn);
         if (LOG.isDebugEnabled()) {
             LOG.debug("Normalized DN: " + normalizedDN);
         }
         return normalizedDN;
     }
 
     /**
      * Returns the mapping value for the given attribute name-value pair, or
      * <code>null</code> if the pair is not mapped.
      * 
      * @param attributeName
      *            The attribute name.
      * @param attributeValue
      *            The attribute value.
      * @return The mapped attribute value if the attribute name-value pair
      *         exists in the mapping table, <code>null</code> otherwise.
      */
     protected String mappedValue(String attributeName, String attributeValue) {
         if (mappedValues_.containsKey(attributeName)) {
             Map attributeValueMappings = (Map) mappedValues_.get(attributeName);
             if (attributeValueMappings != null
                     && attributeValueMappings.containsKey(attributeValue)) {
                 String attributeValueMapping = (String) attributeValueMappings.get(attributeValue);
                 if (attributeValueMapping != null) {
                     return attributeValueMapping;
                 }
             }
         }
         // return attributeValue;
         return null;
     }
 
     /**
      * Returns a hash value of the given string.
      * 
      * @param attributeValue
      *            The attributeValue to hash.
      * @return The hash code (HEX) of the string.
      * @see java.lang.String#hashCode()
      */
     protected String hashValue(String attributeValue) {
         int hashCode = attributeValue.hashCode();
         String hashValue = Integer.toHexString(hashCode);
         return hashValue.toUpperCase();
     }
 
 }
