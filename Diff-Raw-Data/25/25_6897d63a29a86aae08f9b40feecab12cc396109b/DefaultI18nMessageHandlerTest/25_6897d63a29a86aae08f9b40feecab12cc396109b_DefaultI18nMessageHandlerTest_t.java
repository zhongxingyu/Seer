 package org.dejava.component.i18n.test.message;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Locale;
 
 import junit.framework.Assert;
 
 import org.dejava.component.exception.localized.unchecked.EmptyParameterException;
 import org.dejava.component.exception.localized.unchecked.InvalidParameterException;
 import org.dejava.component.i18n.message.annotation.MessageBundle;
 import org.dejava.component.i18n.message.annotation.MessageBundles;
 import org.dejava.component.i18n.message.exception.MessageNotFoundException;
 import org.dejava.component.i18n.message.handler.MessageHandler;
 import org.dejava.component.i18n.message.handler.impl.DefaultI18nMessageHandler;
 import org.dejava.component.i18n.message.handler.model.ApplicationMessageType;
 import org.dejava.component.i18n.test.message.constant.InformationKeys;
 import org.dejava.component.reflection.ClassMirror;
 import org.dejava.component.reflection.FieldMirror;
 import org.dejava.component.test.annotation.ParametricTest;
 import org.dejava.component.test.runner.JUnitParametricRunner;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 
 /**
  * Tests for the default i18n message handler.
  */
 @MessageBundles(defaultType = "information", messageBundles = {
		@MessageBundle(type = "information", baseName = "org.dejava.component.i18n.test.message.properties.information"),
		@MessageBundle(type = "error", baseName = "org.dejava.component.i18n.test.message.properties.error") })
 @RunWith(value = JUnitParametricRunner.class)
 public class DefaultI18nMessageHandlerTest {
 	
 	/**
 	 * The information message bundle name.
 	 */
 	private static final String INFO_BUNDLE = "information";
 	
 	/**
 	 * The error message bundle name.
 	 */
 	private static final String ERROR_BUNDLE = "error";
 	
 	/**
 	 * Tests getting a message by key with a null key.
 	 * 
 	 * @throws MessageNotFoundException
 	 *             If the message cannot be found.
 	 * @throws InvalidParameterException
 	 *             If any of the parameters values are not valid.
 	 */
 	@Test(expected = EmptyParameterException.class)
 	public void testGetMessageByKeyNullKey() throws MessageNotFoundException, InvalidParameterException {
 		// Gets the message handler.
 		final MessageHandler messageHandler = DefaultI18nMessageHandler.getMessageHandler(new Locale("pt",
 				"BR"));
 		// Tries to get the message.
 		messageHandler.getMessage(null, null);
 	}
 	
 	/**
 	 * Tests getting a message by type and key with a null key.
 	 * 
 	 * @throws MessageNotFoundException
 	 *             If the message cannot be found.
 	 * @throws InvalidParameterException
 	 *             If any of the parameters values are not valid.
 	 */
 	@Test(expected = EmptyParameterException.class)
 	public void testGetMessageByTypeAndKeyNullKey() throws MessageNotFoundException,
 			InvalidParameterException {
 		// Gets the message handler.
 		final MessageHandler messageHandler = DefaultI18nMessageHandler.getMessageHandler(new Locale("pt",
 				"BR"));
 		// Tries to get the message.
 		messageHandler.getMessage(ApplicationMessageType.ERROR, null, null);
 	}
 	
 	/**
 	 * Tests getting a message by type (String) and key with a null key.
 	 * 
 	 * @throws MessageNotFoundException
 	 *             If the message cannot be found.
 	 * @throws InvalidParameterException
 	 *             If any of the parameters values are not valid.
 	 */
 	@Test(expected = EmptyParameterException.class)
 	public void testGetMessageByStringTypeAndKeyNullKey() throws MessageNotFoundException,
 			InvalidParameterException {
 		// Gets the message handler.
 		final MessageHandler messageHandler = DefaultI18nMessageHandler.getMessageHandler(new Locale("pt",
 				"BR"));
 		// Tries to get the message.
 		messageHandler.getMessage(ERROR_BUNDLE, null, null);
 	}
 	
 	/**
 	 * Tests getting a message by key that is not found.
 	 * 
 	 * @throws MessageNotFoundException
 	 *             If the message cannot be found.
 	 * @throws InvalidParameterException
 	 *             If any of the parameters values are not valid.
 	 */
 	@Test(expected = MessageNotFoundException.class)
 	public void testGetMessageByKeyNoKeyFound() throws MessageNotFoundException, InvalidParameterException {
 		// Gets the message handler.
 		final MessageHandler messageHandler = DefaultI18nMessageHandler.getMessageHandler(new Locale("pt",
 				"BR"));
 		// Tries to get the message.
 		messageHandler.getMessage("test", null);
 	}
 	
 	/**
 	 * Tests getting a message by type and key that is not found.
 	 * 
 	 * @throws MessageNotFoundException
 	 *             If the message cannot be found.
 	 * @throws InvalidParameterException
 	 *             If any of the parameters values are not valid.
 	 */
 	@Test(expected = MessageNotFoundException.class)
 	public void testGetMessageByTypeAndKeyKeyNoKeyFound() throws MessageNotFoundException,
 			InvalidParameterException {
 		// Gets the message handler.
 		final MessageHandler messageHandler = DefaultI18nMessageHandler.getMessageHandler(new Locale("pt",
 				"BR"));
 		// Tries to get the message.
 		messageHandler.getMessage(ApplicationMessageType.INFORMATION, "test", null);
 	}
 	
 	/**
 	 * Tests getting a message by type (String) and key that is not found.
 	 * 
 	 * @throws MessageNotFoundException
 	 *             If the message cannot be found.
 	 * @throws InvalidParameterException
 	 *             If any of the parameters values are not valid.
 	 */
 	@Test(expected = MessageNotFoundException.class)
 	public void testGetMessageByStringTypeAndKeyKeyNoKeyFound() throws MessageNotFoundException,
 			InvalidParameterException {
 		// Gets the message handler.
 		final MessageHandler messageHandler = DefaultI18nMessageHandler.getMessageHandler(new Locale("pt",
 				"BR"));
 		// Tries to get the message.
 		messageHandler.getMessage(INFO_BUNDLE, "test", null);
 	}
 	
 	/**
 	 * Tests getting a message by key for a not found locale.
 	 * 
 	 * @throws MessageNotFoundException
 	 *             If the message cannot be found.
 	 * @throws InvalidParameterException
 	 *             If any of the parameters values are not valid.
 	 */
 	@Test
 	public void testGetMessageByKeyNoLocaleFound() throws MessageNotFoundException, InvalidParameterException {
 		// Gets the message handler.
 		final MessageHandler messageHandler1 = DefaultI18nMessageHandler.getMessageHandler(Locale.CHINESE);
 		// Tries to get the message.
 		final String message1 = messageHandler1.getMessage(InformationKeys.SUCCESS1, null);
 		// Gets the message handler.
 		final MessageHandler messageHandler2 = DefaultI18nMessageHandler.getMessageHandler(Locale
 				.getDefault());
 		// Tries to get the message.
 		final String message2 = messageHandler2.getMessage(InformationKeys.SUCCESS1, null);
 		// Assert that the messages are the same.
 		Assert.assertEquals(message1, message2);
 	}
 	
 	/**
 	 * Tests getting a message by type and key for a not found locale.
 	 * 
 	 * @throws MessageNotFoundException
 	 *             If the message cannot be found.
 	 * @throws InvalidParameterException
 	 *             If any of the parameters values are not valid.
 	 */
 	@Test
 	public void testGetMessageByTypeAndKeyKeyNoLocaleFound() throws MessageNotFoundException,
 			InvalidParameterException {
 		// Gets the message handler.
 		final MessageHandler messageHandler1 = DefaultI18nMessageHandler.getMessageHandler(Locale.CHINESE);
 		// Tries to get the message.
 		final String message1 = messageHandler1.getMessage(ApplicationMessageType.INFORMATION,
 				InformationKeys.SUCCESS1, null);
 		// Gets the message handler.
 		final MessageHandler messageHandler2 = DefaultI18nMessageHandler.getMessageHandler(Locale
 				.getDefault());
 		// Tries to get the message.
 		final String message2 = messageHandler2.getMessage(ApplicationMessageType.INFORMATION,
 				InformationKeys.SUCCESS1, null);
 		// Assert that the messages are the same.
 		Assert.assertEquals(message1, message2);
 	}
 	
 	/**
 	 * Tests getting a message by type (String) and key for a not found locale.
 	 * 
 	 * @throws MessageNotFoundException
 	 *             If the message cannot be found.
 	 * @throws InvalidParameterException
 	 *             If any of the parameters values are not valid.
 	 */
 	@Test
 	public void testGetMessageByStringTypeAndKeyKeyNoLocaleFound() throws MessageNotFoundException,
 			InvalidParameterException {
 		// Gets the message handler.
 		final MessageHandler messageHandler1 = DefaultI18nMessageHandler.getMessageHandler(Locale.CHINESE);
 		// Tries to get the message.
 		final String message1 = messageHandler1.getMessage(INFO_BUNDLE, InformationKeys.SUCCESS1, null);
 		// Gets the message handler.
 		final MessageHandler messageHandler2 = DefaultI18nMessageHandler.getMessageHandler(Locale
 				.getDefault());
 		// Tries to get the message.
 		final String message2 = messageHandler2.getMessage(INFO_BUNDLE, InformationKeys.SUCCESS1, null);
 		// Assert that the messages are the same.
 		Assert.assertEquals(message1, message2);
 	}
 	
 	/**
 	 * Gets the keys defined in the information key class.
 	 * 
 	 * @return The keys defined in the information key class.
 	 */
 	public static Collection<String> testGetMessageSuccessKeys() {
 		// Creates the list of the keys.
 		final Collection<String> keys = new ArrayList<>();
 		// Gets the information key class.
 		final ClassMirror<InformationKeys> infoKeys = new ClassMirror<>(InformationKeys.class);
 		// For each field in the class.
 		for (final FieldMirror currentField : infoKeys.getFields()) {
 			// Tries to add the current key to the list.
 			try {
 				keys.add((String) currentField.getValue(null, true, false));
 			}
 			// If the field value cannot be retrieved.
 			catch (final Exception exception) {
 				// Ignores and continues the loop.
 			}
 		}
 		// Returns the retrieved keys.
 		return keys;
 	}
 	
 	/**
 	 * The en_US message for the success keys.
 	 */
 	public static final String SUCCESS_MESSAGE_EN_US = "success";
 	
 	/**
 	 * The pt_BR message for the success keys.
 	 */
 	public static final String SUCCESS_MESSAGE_PT_BR = "sucesso";
 	
 	/**
 	 * Tests getting a message by an existent key.
 	 * 
 	 * @param key
 	 *            A key that exists in the resource bundle.
 	 */
 	@ParametricTest(paramsValues = { "testGetMessageSuccessKeys" })
 	public void testGetMessageByKeySuccess(final String key) {
 		// Gets the message handler (en_US).
 		final MessageHandler messageHandlerUS = DefaultI18nMessageHandler.getMessageHandler(new Locale("en",
 				"US"));
 		// Tries to get the message (en_US).
 		final String messageUS = messageHandlerUS.getMessage(key, null);
 		// Assert that the message was retrieved correctly (en_US).
 		Assert.assertTrue(messageUS.startsWith(SUCCESS_MESSAGE_EN_US));
 		// Gets the message handler (pt_BR).
 		final MessageHandler messageHandlerBR = DefaultI18nMessageHandler.getMessageHandler(new Locale("pt",
 				"BR"));
 		// Tries to get the message (pt_BR).
 		final String messageBR = messageHandlerBR.getMessage(key, null);
 		// Assert that the message was retrieved correctly (pt_BR).
 		Assert.assertTrue(messageBR.startsWith(SUCCESS_MESSAGE_PT_BR));
 	}
 	
 	/**
 	 * Tests getting a message by type and an existent key.
 	 * 
 	 * @param key
 	 *            A key that exists in the resource bundle.
 	 */
 	@ParametricTest(paramsValues = { "testGetMessageSuccessKeys" })
 	public void testGetMessageByTypeAndKeySuccess(final String key) {
 		// Gets the message handler (en_US).
 		final MessageHandler messageHandlerUS = DefaultI18nMessageHandler.getMessageHandler(new Locale("en",
 				"US"));
 		// Tries to get the message (en_US).
 		final String messageUS = messageHandlerUS.getMessage(ApplicationMessageType.INFORMATION, key, null);
 		// Assert that the message was retrieved correctly (en_US).
 		Assert.assertTrue(messageUS.startsWith(SUCCESS_MESSAGE_EN_US));
 		// Gets the message handler (pt_BR).
 		final MessageHandler messageHandlerBR = DefaultI18nMessageHandler.getMessageHandler(new Locale("pt",
 				"BR"));
 		// Tries to get the message (pt_BR).
 		final String messageBR = messageHandlerBR.getMessage(ApplicationMessageType.INFORMATION, key, null);
 		// Assert that the message was retrieved correctly (pt_BR).
 		Assert.assertTrue(messageBR.startsWith(SUCCESS_MESSAGE_PT_BR));
 	}
 	
 	/**
 	 * Tests getting a message by type (String) and an existent key.
 	 * 
 	 * @param key
 	 *            A key that exists in the resource bundle.
 	 */
 	@ParametricTest(paramsValues = { "testGetMessageSuccessKeys" })
 	public void testGetMessageByStringTypeAndKeySuccess(final String key) {
 		// Gets the message handler (en_US).
 		final MessageHandler messageHandlerUS = DefaultI18nMessageHandler.getMessageHandler(new Locale("en",
 				"US"));
 		// Tries to get the message (en_US).
 		final String messageUS = messageHandlerUS.getMessage(INFO_BUNDLE, key, null);
 		// Assert that the message was retrieved correctly (en_US).
 		Assert.assertTrue(messageUS.startsWith(SUCCESS_MESSAGE_EN_US));
 		// Gets the message handler (pt_BR).
 		final MessageHandler messageHandlerBR = DefaultI18nMessageHandler.getMessageHandler(new Locale("pt",
 				"BR"));
 		// Tries to get the message (pt_BR).
 		final String messageBR = messageHandlerBR.getMessage(INFO_BUNDLE, key, null);
 		// Assert that the message was retrieved correctly (pt_BR).
 		Assert.assertTrue(messageBR.startsWith(SUCCESS_MESSAGE_PT_BR));
 	}
 	
 	/**
 	 * Gets some simple parameters to be tested in a parameterized message.
 	 * 
 	 * @return Some simple parameters to be tested in a parameterized message.
 	 */
 	public static Collection<String[]> testGetMessageSuccessSimpleParameters() {
 		// Creates a new parameters list.
 		final Collection<String[]> parameters = new ArrayList<>();
 		// Adds several parameters combinations...
 		parameters.add(new String[] { "dsa", "fre" });
 		parameters.add(new String[] { "t43", "b fg" });
 		parameters.add(new String[] { "54bsdg", "bzcv" });
 		parameters.add(new String[] { "cxvb", "4gfds" });
 		parameters.add(new String[] { "bsdgh", "o8uy67" });
 		parameters.add(new String[] { "lçi", "tiuk" });
 		// Returns the parameters.
 		return parameters;
 	}
 	
 	/**
 	 * The key for a parameterized message (with to parameters at the end of the message).
 	 */
 	public static final String PARAM_MESSAGE_KEY = "message.sample.success6";
 	
 	/**
 	 * Tests getting a message an existent key and some simple parameters.
 	 * 
 	 * @param simpleParams
 	 *            The parameters to be used in the message.
 	 */
 	@ParametricTest(paramsValues = { "testGetMessageSuccessSimpleParameters" })
 	public void testGetMessageByKeyWithSimpleParametersSuccess(final String[] simpleParams) {
 		// Gets the message handler.
 		final MessageHandler messageHandler = DefaultI18nMessageHandler.getMessageHandler(new Locale("pt",
 				"BR"));
 		// Tries to get the message.
 		final String messageUS = messageHandler.getMessage(PARAM_MESSAGE_KEY, simpleParams);
 		// Assert that the message was retrieved correctly.
 		Assert.assertEquals(SUCCESS_MESSAGE_PT_BR + simpleParams[0] + simpleParams[1], messageUS);
 	}
 	
 	/**
 	 * Tests getting a message by type and an existent key and some simple parameters.
 	 * 
 	 * @param simpleParams
 	 *            The parameters to be used in the message.
 	 */
 	@ParametricTest(paramsValues = { "testGetMessageSuccessSimpleParameters" })
 	public void testGetMessageByTypeAndKeyWithSimpleParametersSuccess(final String[] simpleParams) {
 		// Gets the message handler.
 		final MessageHandler messageHandler = DefaultI18nMessageHandler.getMessageHandler(new Locale("pt",
 				"BR"));
 		// Tries to get the message.
 		final String messageUS = messageHandler.getMessage(ApplicationMessageType.INFORMATION,
 				PARAM_MESSAGE_KEY, simpleParams);
 		// Assert that the message was retrieved correctly.
 		Assert.assertEquals(SUCCESS_MESSAGE_PT_BR + simpleParams[0] + simpleParams[1], messageUS);
 	}
 	
 	/**
 	 * Tests getting a message by type (String) and an existent key and some simple parameters.
 	 * 
 	 * @param simpleParams
 	 *            The parameters to be used in the message.
 	 */
 	@ParametricTest(paramsValues = { "testGetMessageSuccessSimpleParameters" })
 	public void testGetMessageByStringTypeAndKeyWithSimpleParametersSuccess(final String[] simpleParams) {
 		// Gets the message handler.
 		final MessageHandler messageHandler = DefaultI18nMessageHandler.getMessageHandler(new Locale("pt",
 				"BR"));
 		// Tries to get the message.
 		final String messageUS = messageHandler.getMessage(INFO_BUNDLE, PARAM_MESSAGE_KEY, simpleParams);
 		// Assert that the message was retrieved correctly.
 		Assert.assertEquals(SUCCESS_MESSAGE_PT_BR + simpleParams[0] + simpleParams[1], messageUS);
 	}
 	
 }
