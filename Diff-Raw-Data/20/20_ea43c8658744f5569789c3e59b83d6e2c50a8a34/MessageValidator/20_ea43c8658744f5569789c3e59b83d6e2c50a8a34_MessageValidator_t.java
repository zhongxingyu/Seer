 /**
  * Copyright 2009-2010 the original author or authors.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package net.javacrumbs.springws.test.helper;
 
 import java.io.IOException;
 import java.util.Map;
 import java.util.regex.Pattern;
 
 import net.javacrumbs.springws.test.WsTestException;
 import net.javacrumbs.springws.test.common.DefaultMessageComparator;
 import net.javacrumbs.springws.test.common.ExpressionEvaluator;
 import net.javacrumbs.springws.test.common.MessageComparator;
 import net.javacrumbs.springws.test.common.SchemaValidator;
 import net.javacrumbs.springws.test.common.XPathExpressionEvaluator;
 import net.javacrumbs.springws.test.template.FreeMarkerTemplateProcessor;
 import net.javacrumbs.springws.test.template.TemplateProcessor;
 import net.javacrumbs.springws.test.template.XsltTemplateProcessor;
 import net.javacrumbs.springws.test.util.DefaultXmlUtil;
 import net.javacrumbs.springws.test.util.XmlUtil;
 
 import org.springframework.core.io.DefaultResourceLoader;
 import org.springframework.core.io.Resource;
 import org.springframework.core.io.ResourceLoader;
 import org.springframework.ws.WebServiceMessage;
 import org.springframework.ws.soap.SoapEnvelopeException;
 import org.springframework.ws.soap.SoapFault;
 import org.springframework.ws.soap.SoapMessage;
 import org.springframework.xml.namespace.SimpleNamespaceContext;
 import org.springframework.xml.validation.XmlValidator;
 import org.springframework.xml.validation.XmlValidatorFactory;
 
 
 /**
  * Contains methods simplifying message validation.
  * @author Lukas Krecan
  *
  */
 public class MessageValidator {
 
 	private static final String TRUE = Boolean.TRUE.toString();
 
 	private final WebServiceMessage message;
 	
     private ResourceLoader resourceLoader = new DefaultResourceLoader();
 
 	private TemplateProcessor templateProcessor = new XsltTemplateProcessor();
 
 	private SimpleNamespaceContext namespaceContext = new SimpleNamespaceContext();
 	
 	private MessageComparator messageComparator = new DefaultMessageComparator();
 	
 	private SchemaValidator schemaValidator = new SchemaValidator();
 
 	private String schemaLanguage = XmlValidatorFactory.SCHEMA_W3C_XML;
 	
 	private ExpressionEvaluator expressionEvaluator = new XPathExpressionEvaluator();
 	
 	private XmlUtil xmlUtil = DefaultXmlUtil.getInstance();
 	
 	public MessageValidator(WebServiceMessage message) {
 		this.message = message;
 	}
 	
 	public MessageValidator compare(String controlResourcePath) {
 		return compare(getResource(controlResourcePath));
 	}
 
 	public MessageValidator compare(Resource controlResource) {
 		try {
 			messageComparator.compareMessage(message, preprocessResource(controlResource));
 		} catch (IOException e) {
 			processIOException(e);
 		}
 		return this;
 	}
 
 	protected void processIOException(IOException e) {
 		throw new WsTestException("Error when comparing messages", e);
 	}
 		
 	/**
 	 * Validates if the message corresponds to given XSDs.
 	 * @param message
 	 */
 	public MessageValidator validate(String schemaPath, String... schemaPaths) throws IOException {
 		Resource[] schemas = new Resource[schemaPaths.length];
 		for (int i = 0; i < schemas.length; i++) {
 			schemas[i] = resourceLoader.getResource(schemaPaths[i]);
 		}
 		return validate(resourceLoader.getResource(schemaPath), schemas);
 	}
 	
 	/**
 	 * Validates if the message corresponds to given XSD.
 	 * @param message
 	 */
 	public MessageValidator validate(Resource schema, Resource... schemas) throws IOException {
 		
 		Resource[] joinedSchemas = new Resource[schemas.length+1];
 		joinedSchemas[0] = schema;
 		System.arraycopy(schemas, 0, joinedSchemas, 1, schemas.length);
 		
 		validate(schemaValidator.createValidatorFromSchemas(joinedSchemas, schemaLanguage ));
 		return this;
 	}
 	
 	
 	
 	
 	/**
 	 * Validates message using generic {@link XmlValidator}. See {@link XmlValidatorFactory} for more details.
 	 * @param validator
 	 */
 	public MessageValidator validate(XmlValidator xmlValidator) {
 		try {
 			schemaValidator.validate(message, xmlValidator);		
 		} catch (IOException e) {
 			processIOException(e);
 		}
 		return this;
 	}
 	
 	/**
 	 * Sets namespace mapping for all XPath validations like {@link #assertXPath(String)}
 	 * @param namespaceMapping
 	 * @return
 	 */
 	public MessageValidator useNamespaceMapping(Map<String, String> namespaceMapping) {
 		namespaceContext = new SimpleNamespaceContext();
 		namespaceContext.setBindings(namespaceMapping);
 		return this;
 	}
 	
 	/**
 	 * Adds mapping between prefix and namespace.
 	 * @param prefix
 	 * @param namespace
 	 * @return
 	 */
 	public MessageValidator addNamespaceMapping(String prefix, String namespace)
 	{
 		namespaceContext.bindNamespaceUri(prefix, namespace);
 		return this;
 	}
 	
 	/**
 	 * Asserts XPath expression. If the expression is not evaluated to true, throws {@link WsTestException}.
 	 * It is possible to set namespace mapping using {@link #useNamespaceMapping(Map)}.
 	 * @param xpath
 	 * @return
 	 */
 	public MessageValidator assertXPath(String xpath) {
		return assertXPath(xpath, "Expression \""+xpath+"\" does not evaluate to true.");
	}
	/**
	 * Asserts XPath expression. If the expression is not evaluated to true, throws {@link WsTestException}.
	 * It is possible to set namespace mapping using {@link #useNamespaceMapping(Map)}.
	 * @param xpath
	 * @return
	 */
	public MessageValidator assertXPath(String xpath, String errorMessage) {
 		if (!TRUE.equals(evaluateExpression(xpath)))
 		{
			throw new WsTestException(errorMessage);
 		}
 		return this;
 	}
 
 	/**
 	 * Evaluates expression
 	 * @param expression
 	 * @return
 	 */
 	protected String evaluateExpression(String expression) {
 		return expressionEvaluator.evaluateExpression(xmlUtil.loadDocument(message), expression, null, namespaceContext);
 	}
 	
 
 	/**
 	 * Asserts that message is not SOAP fault.
 	 * @return
 	 */
 	public MessageValidator assertNotSoapFault() {
 		if (isSoapFault())
 		{
 			throw new WsTestException("Message is SOAP fault");
 		}
 		return this;
 	}
 	
 	
 	
 	/**
 	 * Assert that message is SOAP message (wrapped in SOAP envelope).
 	 */
 	public MessageValidator assertSoapMessage() {
 		SoapMessage soapMessage = getSoapMessage();
 		try
 		{
 			soapMessage.getEnvelope();
 		}
 		catch(SoapEnvelopeException e)
 		{
 			throw new WsTestException("Message is not a SOAP message",e);
 		}
 		return this;
 	}
 	/**
 	 * Assert that message is SOAP fault.
 	 */
 	public MessageValidator assertSoapFault() {
 		if (!isSoapFault())
 		{
 			throw new WsTestException("Message is not SOAP fault");
 		}
 		return this;
 	}
 
 	/**
 	 * Returns true if message is SOAP fault.
 	 * @return
 	 */
 	protected boolean isSoapFault() {
 		if (getSoapMessage().hasFault()) 
 		{
 			return true;
 		}
 		else
 		{
 			return false;
 		}
 	}
 
 	/**
 	 * Returns soap message.
 	 * @return
 	 */
 	public SoapMessage getSoapMessage() {
 		if (message instanceof SoapMessage)
 		{
 			return (SoapMessage)message;
 		}
 		else
 		{
 			throw new WsTestException("The message is not SOAP message");
 		}
 	}
 	
 	/**
 	 * Returns SOAP fault.
 	 * @return
 	 */
 	public SoapFault getSoapFault() {
 		assertSoapFault();	
 		return getSoapMessage().getSoapBody().getFault();
 	}
 	
 	/**
 	 * Compares fault code.
 	 * @param expectedFaultCode
 	 * @return
 	 */
 	public MessageValidator assertFaultCode(String expectedFaultCode) {
 		String faultCode = getSoapFault().getFaultCode().getLocalPart();
 		if (!expectedFaultCode.equals(faultCode))
 		{
 			throw new WsTestException("Expected fault code \""+expectedFaultCode+"\", get \""+faultCode+"\"");
 		}
 		return this;
 	}
 
 	/**
 	 * Compares fault string or reason. See {@link SoapFault#getFaultStringOrReason()}.
 	 * @param expectedStringOrReason
 	 * @return 
 	 */
 	public MessageValidator assertFaultStringOrReason(String expectedStringOrReason) {
 		String faultStringOrReason = getSoapFault().getFaultStringOrReason();
 		if (!expectedStringOrReason.equals(faultStringOrReason))
 		{
 			throw new WsTestException("Expected fault string or reason \""+expectedStringOrReason+"\", get \""+faultStringOrReason+"\"");
 		}
 		return this;
 	}
 	/**
 	 * Compares fault actor or role. See {@link SoapFault#getFaultActorOrRole()}.
 	 * @param expectedActorOrRole
 	 * @return 
 	 */
 	public MessageValidator assertFaultActorOrRole(String expectedActorOrRole) {
 		String faultActorOrRole = getSoapFault().getFaultActorOrRole();
 		if (!expectedActorOrRole.equals(faultActorOrRole))
 		{
 			throw new WsTestException("Expected fault actor or role \""+expectedActorOrRole+"\", get \""+faultActorOrRole+"\"");
 		}
 		return this;
 	}
 	
 	/**
 	 * Asserts that the message contains element with given name. Namespace is ignored.
 	 * @param elementName
 	 * @return 
 	 */
 	public MessageValidator assertContainElement(String elementName) {
		return assertXPath("count(//*[local-name()='"+elementName+"'])>0", "Element \""+elementName+"\" not found.");
 	}
 
 	/**
 	 * Asserts that message does not contain given element. Namespace is ignored.
 	 * @param string
 	 * @return 
 	 */
 	public MessageValidator assertNotContainElement(String elementName) {
		return assertXPath("count(//*[local-name()='"+elementName+"'])=0", "Element \""+elementName+"\" found.");
 	}
 	
 
 	/**
 	 * Asserts that the message contains given reqular expression. The message is processed as text, so whitespaces are taken into account
 	 * @param string
 	 * @return 
 	 */
 	public MessageValidator assertContain(String regexp) {
 		if (!contains(regexp))
 		{
 			throw new WsTestException("Message does not contain regular expression \""+regexp+"\"");
 		}
 		return this;
 	}
 	
 	/**
 	 * Asserts that the message does not contain given reqular expression. The message is processed as text, so whitespaces are taken into account
 	 * @param string
 	 * @return 
 	 * @return 
 	 */
 	public MessageValidator assertNotContain(String regexp) {
 		if (contains(regexp))
 		{
 			throw new WsTestException("Message contains regular expression \""+regexp+"\"");
 		}
 		return this;
 	}
 	/**
 	 * Returns true if message contains regexp.
 	 * @param regexp
 	 * @return 
 	 */
 	private boolean contains(String regexp) {
 		String messageAsText = xmlUtil.serializeDocument(message);
 		Pattern pattern = Pattern.compile(regexp);
 		return pattern.matcher(messageAsText).find();
 	}
 
 
 	
 	/**
 	 * Does resource preprocessing. In default implementation just evaluates a template.
 	 * @param resource
 	 * @return
 	 * @throws IOException
 	 */
 	protected Resource preprocessResource(Resource resource) throws IOException {
 		return templateProcessor.processTemplate(resource, null);
 	}
 
 
 	protected Resource getResource(String resourcePath) {
 		return resourceLoader.getResource(resourcePath);
 	}
 	
 	public ResourceLoader getResourceLoader() {
 		return resourceLoader;
 	}
 
 	public void setResourceLoader(ResourceLoader resourceLoader) {
 		this.resourceLoader = resourceLoader;
 	}
 
 	public TemplateProcessor getTemplateProcessor() {
 		return templateProcessor;
 	}
 
 	public void setTemplateProcessor(TemplateProcessor templateProcessor) {
 		this.templateProcessor = templateProcessor;
 	}
 	
 	public MessageValidator useTemplateProcessor(TemplateProcessor templateProcessor) {
 		setTemplateProcessor(templateProcessor);
 		return this;
 	}
 	
 	/**
 	 * From now on use FreeMarker for templates.
 	 * @return
 	 */
 	public MessageValidator useFreeMarkerTemplateProcessor() {
 		FreeMarkerTemplateProcessor freemarkerTemplateProcessor = new FreeMarkerTemplateProcessor();
 		freemarkerTemplateProcessor.setResourceLoader(resourceLoader);
 		freemarkerTemplateProcessor.afterPropertiesSet();
 		return useTemplateProcessor(freemarkerTemplateProcessor);
 	}
 	
 	
 	
 	/**
 	 * From now on use XSLT for templates.
 	 * @return
 	 */
 	public MessageValidator useXsltTemplateProcessor() {
 		return useTemplateProcessor(new XsltTemplateProcessor());
 	}
 
 	public WebServiceMessage getMessage() {
 		return message;
 	}
 
 	public MessageComparator getMessageComparator() {
 		return messageComparator;
 	}
 
 	public void setMessageComparator(MessageComparator messageComparator) {
 		this.messageComparator = messageComparator;
 	}
 
 	public MessageValidator useMessageComparator(MessageComparator messageComparator) {
 		setMessageComparator(messageComparator);
 		return this;
 	}
 
 	public SchemaValidator getSchemaValidator() {
 		return schemaValidator;
 	}
 
 	public void setSchemaValidator(SchemaValidator schemaValidator) {
 		this.schemaValidator = schemaValidator;
 	}
 
 	public MessageValidator useSchemaValidator(SchemaValidator schemaValidator) {
 		setSchemaValidator(schemaValidator);
 		return this;
 	}
 
 	public String getSchemaLanguage() {
 		return schemaLanguage;
 	}
 
 	public void setSchemaLanguage(String schemaLanguage) {
 		this.schemaLanguage = schemaLanguage;
 	}
 	public MessageValidator useSchemaLanguage(String schemaLanguage) {
 		setSchemaLanguage(schemaLanguage);
 		return this;		
 	}
 
 	public ExpressionEvaluator getExpressionEvaluator() {
 		return expressionEvaluator;
 	}
 
 	public void setExpressionEvaluator(ExpressionEvaluator expressionEvaluator) {
 		this.expressionEvaluator = expressionEvaluator;
 	}
 
 	public MessageValidator useExpressionEvaluator(ExpressionEvaluator expressionEvaluator) {
 		setExpressionEvaluator(expressionEvaluator);
 		return this;
 	}
 }
