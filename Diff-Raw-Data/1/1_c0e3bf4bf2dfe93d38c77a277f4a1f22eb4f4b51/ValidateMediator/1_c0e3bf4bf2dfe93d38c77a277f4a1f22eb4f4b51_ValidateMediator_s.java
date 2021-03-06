 /*
  *  Licensed to the Apache Software Foundation (ASF) under one
  *  or more contributor license agreements.  See the NOTICE file
  *  distributed with this work for additional information
  *  regarding copyright ownership.  The ASF licenses this file
  *  to you under the Apache License, Version 2.0 (the
  *  "License"); you may not use this file except in compliance
  *  with the License.  You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing,
  *  software distributed under the License is distributed on an
  *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  *  KIND, either express or implied.  See the License for the
  *  specific language governing permissions and limitations
  *  under the License.
  */
 
 package org.apache.synapse.mediators.builtin;
 
 import org.apache.axiom.om.OMNode;
 import org.apache.axiom.om.xpath.AXIOMXPath;
 import org.apache.axiom.soap.SOAP11Constants;
 import org.apache.axiom.soap.SOAP12Constants;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.synapse.MessageContext;
 import org.apache.synapse.SynapseException;
 import org.apache.synapse.SynapseConstants;
 import org.apache.synapse.config.Util;
 import org.apache.synapse.config.Entry;
 import org.apache.synapse.mediators.AbstractListMediator;
 import org.apache.synapse.mediators.MediatorProperty;
 import org.jaxen.JaxenException;
 import org.xml.sax.*;
 import org.xml.sax.helpers.DefaultHandler;
 import org.xml.sax.helpers.XMLReaderFactory;
 
 import javax.xml.stream.XMLOutputFactory;
 import javax.xml.stream.XMLStreamWriter;
 import javax.xml.transform.sax.SAXSource;
 import javax.xml.transform.stream.StreamSource;
 import javax.xml.transform.Source;
 import javax.xml.validation.Schema;
 import javax.xml.validation.SchemaFactory;
 import javax.xml.validation.Validator;
 import javax.xml.XMLConstants;
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.util.*;
 
 /**
  * Validate a message or an element against a schema
  * <p/>
  * This internally uses the Xerces2-j parser, which cautions a lot about thread-safety and
  * memory leaks. Hence this initial implementation will create a single parser instance
  * for each unique mediator instance, and re-use it to validate multiple messages - even
  * concurrently - by synchronizing access
  */
 public class ValidateMediator extends AbstractListMediator {
 
     private static final Log log = LogFactory.getLog(ValidateMediator.class);
     private static final Log trace = LogFactory.getLog(SynapseConstants.TRACE_LOGGER);
 
     /**
      * A list of property keys, referring to the schemas to be used for the validation
      */
     private List schemaKeys = new ArrayList();
 
     /**
      * An XPath expression to be evaluated against the message to find the element to be validated.
      * If this is not specified, the validation will occur against the first child element of the
      * SOAP body
      */
     private AXIOMXPath source = null;
 
     /**
      * A Map containing features to be passed to the actual validator (Xerces)
      */
     private List explicityFeatures = new ArrayList();
 
     /**
      * This is the actual schema instance used to create a new schema
      * This is a thred-safe instance.
      */
     private Schema cachedSchema;
 
     /**
      * Lock used to ensure thread-safe creation and use of the above Validator
      */
     private final Object validatorLock = new Object();
 
     /**
      * The SchemaFactory for whcih used to create new schema instance
      */
     private  SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
 
     private static final String DEFAULT_XPATH = "//s11:Envelope/s11:Body/child::*[position()=1] | " +
         "//s12:Envelope/s12:Body/child::*[position()=1]";
 
     public ValidateMediator() {
         // create the default XPath
         try {
             this.source = new AXIOMXPath(DEFAULT_XPATH);
             this.source.addNamespace("s11", SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI);
             this.source.addNamespace("s12", SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI);
         } catch (JaxenException e) {
             handleException("Error creating source XPath expression", e);
         }
     }
 
     public boolean mediate(MessageContext synCtx) {
 
         if (log.isDebugEnabled()) {
             log.debug("ValidateMediator - Validate mediator mediate()");
         }
         boolean shouldTrace = shouldTrace(synCtx.getTracingState());
         if (shouldTrace) {
             trace.trace("Start : Validate mediator");
         }
         // Input source for the validation
         Source validateSrc = null;
         try {
             // create a byte array output stream and serialize the source node into it
             ByteArrayOutputStream baosForSource = new ByteArrayOutputStream();
             XMLStreamWriter xsWriterForSource =
                     XMLOutputFactory.newInstance().createXMLStreamWriter(baosForSource);
 
             // serialize the validation target and get an input stream into it
             OMNode validateSource = getValidateSource(synCtx);
             if (shouldTrace) {
                 trace.trace("Validate Source : " + validateSource.toString());
             }
             validateSource.serialize(xsWriterForSource);
             ByteArrayInputStream baisFromSource = new ByteArrayInputStream(
                     baosForSource.toByteArray());
             XMLReader reader = XMLReaderFactory.createXMLReader();
             validateSrc = new SAXSource(reader, new InputSource(baisFromSource));
         } catch (Exception e) {
             handleException("Error accessing source element for validation : " + source, e);
         }
 
         // flag to check if we need to initialize/re-initialize the schema
         boolean reCreate = false;
         // if any of the schemas are not loaded or expired, load or re-load them
         for (Iterator iter = schemaKeys.iterator(); iter.hasNext();) {
             String propKey = (String) iter.next();
             Entry dp = synCtx.getConfiguration().getEntryDefinition(propKey);
             if (dp != null && dp.isDynamic()) {
                 if (!dp.isCached() || dp.isExpired()) {
                     reCreate = true;       // request re-initialization of Validator
                 }
             }
         }
 
         // This is the reference to the DefaultHandler instance
         MyErrorHandler errorHandler = new MyErrorHandler();
 
         // do not re-initialize schema unless required
         synchronized (validatorLock) {
             if (reCreate || cachedSchema == null) {
                 try {
                     factory.setErrorHandler(errorHandler);
                     StreamSource[] sources = new StreamSource[schemaKeys.size()];
                     int i = 0;
                     for (Iterator iterator = schemaKeys.iterator(); iterator.hasNext();) {
                         String propName = (String) iterator.next();
                         sources[i++] = Util.getStreamSource(synCtx.getEntry(propName));
                     }
                     cachedSchema = factory.newSchema(sources);
                     if (errorHandler.isValidationError()) {
                         //reset the errorhandler state
                         errorHandler.setValidationError(false);
                         if (log.isDebugEnabled()) {
                             log.debug("Error occured during creating a new schema ");
                         }
                     }
                 } catch (SAXNotSupportedException e) {
                     handleException("Error when setting a feature", e);
                 } catch (SAXException e) {
                     handleException("Error when creating a new schema", e);
                 }
             }
         }
         // no need to synchronized ,schema instance is thread-safe
         try {
             Validator validator = cachedSchema.newValidator();
             validator.setErrorHandler(errorHandler);
             // perform actual validation
             validator.validate(validateSrc);
             if (errorHandler.isValidationError()) {
                 if (log.isDebugEnabled()) {
                     log.debug(
                             "Validation of element returned by XPath : " + source +
                                     " failed against the given schemas with Message : " +
                                     errorHandler.getSaxParseException().getMessage() +
                                     " Executing 'on-fail' sequence");
                     log.debug("Failed message envelope : " + synCtx.getEnvelope());
                 }
                 // super.mediate() invokes the "on-fail" sequence of mediators
                 if (shouldTrace) {
                     trace.trace("Validation failed. Invoking the \"on-fail\" " +
                             "sequence of mediators");
                 }
                 return super.mediate(synCtx);
             }
         } catch (SAXException e) {
             handleException("Error validating " + source + " element" + e.getMessage(), e);
         } catch (IOException e) {
             handleException("Error validating " + source + " element" + e.getMessage(), e);
         }
         if (log.isDebugEnabled()) {
             log.debug("validation of element returned by the XPath expression : " + source +
                     " succeeded against the given schemas and the current message");
         }
         if (shouldTrace) {
             trace.trace("End : Validate mediator");
         }
         return true;
     }
 
     /**
      * This class handles validation errors to be used for the error reporting
      */
     private class MyErrorHandler extends DefaultHandler {
 
         private boolean validationError = false;
         private SAXParseException saxParseException = null;
 
         public void error(SAXParseException exception) throws SAXException {
             validationError = true;
             saxParseException = exception;
         }
 
         public void fatalError(SAXParseException exception) throws SAXException {
             validationError = true;
             saxParseException = exception;
         }
 
         public void warning(SAXParseException exception) throws SAXException {
         }
 
         public boolean isValidationError() {
             return validationError;
         }
 
         public SAXParseException getSaxParseException() {
             return saxParseException;
         }
         
         /**
          * To set explicitly validation error condition
          * @param validationError  is occur validation error?
          */
         public void setValidationError(boolean validationError) {
             this.validationError = validationError;
         }
     }
 
     private void handleException(String msg) {
         log.error(msg);
         throw new SynapseException(msg);
     }
 
     private void handleException(String msg, Exception e) {
         log.error(msg, e);
         throw new SynapseException(msg, e);
     }
 
     /**
      * Return the OMNode to be validated. If a source XPath is not specified, this will
      * default to the first child of the SOAP body i.e. - //*:Envelope/*:Body/child::*
      *
      * @param synCtx the message context
      * @return the OMNode against which validation should be performed
      */
     private OMNode getValidateSource(MessageContext synCtx) {
 
         try {
             Object o = source.evaluate(synCtx.getEnvelope());
             if (o instanceof OMNode) {
                 return (OMNode) o;
             } else if (o instanceof List && !((List) o).isEmpty()) {
                 return (OMNode) ((List) o).get(0);  // Always fetches *only* the first
             } else {
                 handleException("The evaluation of the XPath expression "
                         + source + " must result in an OMNode");
             }
         } catch (JaxenException e) {
             handleException("Error evaluating XPath " + source + " on message");
         }
         return null;
     }
 
     // setters and getters
 
     /**
      * Get a mediator feature. The common use case is a feature for the
      * underlying Xerces validator
      *
      * @param key property key / feature name
      * @return property string value (usually true|false)
      */
     public Object getFeature(String key) {
         for (Iterator iter = explicityFeatures.iterator(); iter.hasNext();) {
             MediatorProperty prop = (MediatorProperty) iter.next();
             if (key.equals(prop.getName())) {
                 return prop.getValue();
             }
         }
         return null;
     }
 
     /**
      * add a feature which need to set for the Schema Factory
      *
      * @param  featureName The name of the feature
      * @param isFeatureEnable should this feature enable?(true|false)
      * @see #getFeature(String)
      */
    public void addFeature(String featureName, boolean isFeatureEnable) {
         try {
             MediatorProperty mp = new MediatorProperty();
             mp.setName(featureName);
             if (isFeatureEnable) {
                 mp.setValue("true");
             } else {
                 mp.setValue("false");
             }
             explicityFeatures.add(mp);
             factory.setFeature(featureName, isFeatureEnable);             
         } catch (SAXNotSupportedException e) {
            handleException("Error setting a feature to the Schema Factory " + e.getMessage(),e);
         } catch (SAXNotRecognizedException e) {
            handleException("Error setting a feature to the Schema Factory " + e.getMessage(),e);
         }
     }
 
     /**
      * Set a list of local property names which refer to a list of schemas to be
      * used for validation
      *
      * @param schemaKeys list of local property names
      */
     public void setSchemaKeys(List schemaKeys) {
         this.schemaKeys = schemaKeys;
     }
 
     /**
      * Set the given XPath as the source XPath
      * @param source an XPath to be set as the source
      */
     public void setSource(AXIOMXPath source) {
        this.source = source;
     }
 
     /**
      * Get the source XPath which yeilds the source element for validation
      * @return the XPath which yeilds the source element for validation
      */
     public AXIOMXPath getSource() {
         return source;
     }
 
     /**
      * The keys for the schema resources used for validation
      * @return schema registry keys
      */
     public List getSchemaKeys() {
         return schemaKeys;
     }
 
     /**
      * Features for the actual Xerces validator
      * @return explicityFeatures to be passed to the Xerces validator
      */
     public List getFeatures() {
         return explicityFeatures;
     }
 }
