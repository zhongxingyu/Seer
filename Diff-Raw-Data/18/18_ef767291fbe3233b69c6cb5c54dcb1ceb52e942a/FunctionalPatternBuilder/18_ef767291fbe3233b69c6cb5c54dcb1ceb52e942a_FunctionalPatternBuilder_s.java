 /*
 * $Id: FunctionalPatternBuilder.java,v 1.4 2007/03/19 15:45:55 vtschopp Exp $
  *
  * Copyright (c) Members of the EGEE Collaboration. 2004.
  * See http://eu-egee.org/partners/ for details on the copyright holders.
  * For license conditions see the license file or http://eu-egee.org/license.html 
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
 * @version $Revision: 1.4 $
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
                 attributeValueMappings.put(attributeValue, attributeValueMapping);
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
      * Available pattern functions:<br>
      * <ul>
      * <li>mappedValue(attributeName): returns the human readable value of the
      * given attribute name, or simply the attribute value if no mapping is
      * defined.
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
                 value = hashValue(value);
                 if (LOG.isDebugEnabled()) {
                     LOG.debug("DNPattern replace regex: " + replace + " by: "
                             + value);
                 }
                 dn = dn.replaceAll(replace, value);
             }
             else if (getPattern().matches(matchMappedValueFunction)) {
                 String replace = "mappedValue\\( *\\$\\{" + name + "\\} *\\)";
                 // replace value with mapped value
                 value = mappedValue(name, value);
                 if (LOG.isDebugEnabled()) {
                     LOG.debug("DNPattern replace regex: " + replace + " by: "
                             + value);
                 }
                 dn = dn.replaceAll(replace, value);
             }
             else if (getPattern().indexOf(placeholder) != -1) {
                 // replace accentuated chars
                 value = Utils.filterUnicodeAccentuedString(value);
                 // replace placeholder with attribute value: REGEX 1.4
                 String replace = "\\$\\{" + name + "\\}";
                 if (LOG.isDebugEnabled()) {
                     LOG.debug("DNPattern replace regex: " + replace + " by: "
                             + value);
                 }
                 dn = dn.replaceAll(replace, value);
             }
         }
 
         if (dn.indexOf("${") != -1) {
             LOG.error("DN still contains not substitued placeholders: " + dn);
             throw new ServiceException("Missing or wrong Shibboleth attributes: DN still contains placeholders: "
                     + dn);
         }
         if (LOG.isDebugEnabled()) {
             LOG.debug("DN: " + dn);
         }
 
         return dn;
     }
 
     /**
      * Returns the mapping value for the given attribute name-value pair.
      * 
      * @param attributeName
      *            The attribute name.
      * @param attributeValue
      *            The attribute value.
      * @return The mapped attribute value if the attribute name-value pair
      *         exists in the mapping table, otherwise returns the given
      *         attributeValue.
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
         return attributeValue;
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
