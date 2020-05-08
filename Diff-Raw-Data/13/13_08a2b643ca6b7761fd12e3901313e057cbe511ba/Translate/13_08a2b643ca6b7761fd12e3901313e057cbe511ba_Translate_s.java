 package com.diwan.translation;
 
 import java.rmi.RemoteException;
 import java.util.*;
 import org.apache.axis2.AxisFault;
 import com.diwan.soap.SoapServiceStub;
 import com.diwan.soap.SoapServiceStub.*;
 import java.io.*;
 import java.lang.String;
 import javax.xml.stream.*;
 import javax.xml.stream.events.*;
 
 public class Translate {
 	private String appid;
 	private String contenttype;
 	private String category;
 	private String user;
 	private String uri;
 	private SoapServiceStub stub = null;
 	private TranslateOptions options = null;
 
 	public Translate(String id, String type, String catg, String use, String ur) {
 		appid = id;
 		contenttype = type;
 		category = catg;
 		user = use;
 		uri = ur;
 	}
 
 	public void init() throws TranslateFault {
 		try {
 			stub = new SoapServiceStub();
 			options = new SoapServiceStub.TranslateOptions();
 			options.setCategory("general");
 			options.setContentType("text/plain");
 			options.setUser("siteuser");
 
 		} catch (AxisFault e) {
 			e.printStackTrace();
 			throw new TranslateFault(e.getReason());
 		}
 	}
 
 	public void addTranslation(String original, String text, String from, String to, int rate) throws TranslateFault {
 		try {
 			SoapServiceStub.AddTranslation addTranslation = new SoapServiceStub.AddTranslation();
 			addTranslation.setAppId(appid);
 			addTranslation.setOriginalText(original);
 			addTranslation.setTranslatedText(text);
 			addTranslation.setFrom(from);
 			addTranslation.setTo(to);
 			addTranslation.setRating(rate);
 			addTranslation.setContentType(contenttype);
 			addTranslation.setCategory(category);
 			addTranslation.setUser(user);
 			addTranslation.setUri(uri);
 			stub.addTranslation(addTranslation);
 			System.out.println("Your translation has been added");
 		} catch (RemoteException e) {
 			throw new TranslateFault(e.getMessage());
 		}
 	}
 
 	public int[] breakSentences(String text, String language) throws TranslateFault {
 		if (stub == null)
 			init();
 
 		try {
 			SoapServiceStub.BreakSentences breakSentence = new SoapServiceStub.BreakSentences();
 			breakSentence.setLanguage(language);
 			breakSentence.setAppId(appid);
 			breakSentence.setText(text);
 
 			BreakSentencesResponse sentenceLen = stub.breakSentences(breakSentence);
 
 			return sentenceLen.getBreakSentencesResult().get_int();
 		} catch (RemoteException e) {
 			throw new TranslateFault(e.getMessage());
 		}
 	}
 
 	public String detect(String text) throws TranslateFault {
 		if (stub == null)
 			init();
 
 		try {
 			String detectString = "I have no idea what this language may be";
 			SoapServiceStub.Detect detect = new SoapServiceStub.Detect();
 			detect.setAppId(appid);
 			detect.setText(detectString);
 			DetectResponse detectResponse = stub.detect(detect);
 			return detectResponse.getDetectResult();
 		} catch (RemoteException e) {
 			throw new TranslateFault(e.getMessage());
 		}
 	}
 
 	public String[] detectArray(String[] texts) throws TranslateFault {
 
 		if (stub == null)
 			init();
 
 		try {
 			SoapServiceStub.DetectArray detectArray = new SoapServiceStub.DetectArray();
 			SoapServiceStub.ArrayOfstring detectText = new SoapServiceStub.ArrayOfstring();
 			detectText.setString(texts);
 			detectArray.setAppId(appid);
 			detectArray.setTexts(detectText);
 			DetectArrayResponse detectedText = stub.detectArray(detectArray);
 			return detectedText.getDetectArrayResult().getString();
 
 		} catch (RemoteException e) {
 			throw new TranslateFault(e.getMessage());
 		}
 
 	}
 
 	public String getAppIdToken(int minratingread, int maxratingwrite, int expireseconds) throws TranslateFault {
 		if (stub == null)
 			init();
 
 		try {
 			SoapServiceStub.GetAppIdToken appIdToken = new SoapServiceStub.GetAppIdToken();
 			appIdToken.setAppId(appid);
 			appIdToken.setMinRatingRead(minratingread);
 			appIdToken.setMaxRatingWrite(maxratingwrite);
 			appIdToken.setExpireSeconds(expireseconds);
 			GetAppIdTokenResponse appIdTokenRespons = stub
 					.getAppIdToken(appIdToken);
 			return appIdTokenRespons.getGetAppIdTokenResult();
 		} catch (RemoteException e) {
 			throw new TranslateFault(e.getMessage());
 		}
 	}
 
 	public String[] getLanguagesNames(String locale, String[] codeString)
 			throws TranslateFault {
 		if (stub == null)
 			init();
 
 		try {
 			SoapServiceStub.GetLanguageNames languageNames = new SoapServiceStub.GetLanguageNames();
 			languageNames.setAppId(appid);
 
 			SoapServiceStub.ArrayOfstring languageCodes = new SoapServiceStub.ArrayOfstring();
 			languageCodes.setString(codeString);
 			languageNames.setLanguageCodes(languageCodes);
 			languageNames.setLocale(locale);
 			SoapServiceStub.GetLanguageNamesResponse languageNamesResponse = stub
 					.getLanguageNames(languageNames);
 			return languageNamesResponse.getGetLanguageNamesResult()
 					.getString();
 		} catch (RemoteException e) {
 			throw new TranslateFault(e.getMessage());
 		}
 	}
 
 	public String[] getLanguageForSpeak() throws TranslateFault {
 		if (stub == null)
 			init();
 
 		try {
 			SoapServiceStub.GetLanguagesForSpeak languageSpeak = new SoapServiceStub.GetLanguagesForSpeak();
 			languageSpeak.setAppId(appid);
 			GetLanguagesForSpeakResponse languageSpeakA = stub
 					.getLanguagesForSpeak(languageSpeak);
 			return languageSpeakA.getGetLanguagesForSpeakResult().getString();
 		} catch (RemoteException e) {
 			throw new TranslateFault(e.getMessage());
 		}
 	}
 
 	public String[] getLanguagesForTranslate() throws TranslateFault {
 		if (stub == null)
 			init();
 
 		try {
 			SoapServiceStub.GetLanguagesForTranslate languagesTranslate = new SoapServiceStub.GetLanguagesForTranslate();
 			languagesTranslate.setAppId(appid);
 			GetLanguagesForTranslateResponse languagesForTranslate = stub
 					.getLanguagesForTranslate(languagesTranslate);
 			return languagesForTranslate.getGetLanguagesForTranslateResult()
 					.getString();
 		} catch (RemoteException e) {
 			throw new TranslateFault(e.getMessage());
 		}
 	}
 
 	public String[] getTranslations(String text, String from, String to, int maxtranslations)
 			throws TranslateFault {
 		String[] matches = null;
 		if (stub == null)
 			init();
 
 		try {
 			SoapServiceStub.GetTranslations getTranslation = new SoapServiceStub.GetTranslations();
 			getTranslation.setAppId(appid);
 			getTranslation.setText(text);
 			getTranslation.setFrom(from);
 			getTranslation.setTo(to);
 			getTranslation.setMaxTranslations(maxtranslations);
 			getTranslation.setOptions(options);
 			GetTranslationsResponse0 translationResponse = stub.getTranslations(getTranslation);
 
 			TranslationMatch[] translationMatches = translationResponse
 					.getGetTranslationsResult().getTranslations()
 					.getTranslationMatch();
 			matches = new String[translationMatches.length];
 			int i = 0;
 			for (TranslationMatch theTranslationMatch : translationMatches)
 				matches[i++] = theTranslationMatch.getTranslatedText();
 			return matches;
 
 		} catch (RemoteException e) {
 			throw new TranslateFault(e.getMessage());
 		}
 
 	}
 
 	public String[] getTranslationsArray(String[] texts, String from, String to, int maxtranslations)
 			throws TranslateFault {
 		String[] matches = null;
 		if (stub == null)
 			init();
 
 		try {
 			SoapServiceStub.GetTranslationsArray translationArray = new SoapServiceStub.GetTranslationsArray();
 			SoapServiceStub.ArrayOfstring translateArrayString = new SoapServiceStub.ArrayOfstring();
 			translateArrayString.setString(texts);
 			translationArray.setAppId(appid);
 			translationArray.setTexts(translateArrayString);
 			translationArray.setFrom(from);
 			translationArray.setTo(to);
 			translationArray.setMaxTranslations(maxtranslations);
 			translationArray.setOptions(options);
 			GetTranslationsArrayResponse getTranslationResponses = stub
 					.getTranslationsArray(translationArray);
 			GetTranslationsResponse[] translationMatches = getTranslationResponses
 					.getGetTranslationsArrayResult()
 					.getGetTranslationsResponse();
 			matches = new String[translationMatches.length];
 			int i = 0;
 			for (GetTranslationsResponse translationResponse : translationMatches) {
 				for (SoapServiceStub.TranslationMatch match : translationResponse
 						.getTranslations().getTranslationMatch()) {
 					matches[i++] = match.getTranslatedText();
 				}
 			}
 
 		} catch (RemoteException e) {
 			throw new TranslateFault(e.getMessage());
 		}
 
 		return matches;
 
 	}
 
 	public String speak(String text, String language, String format)
 			throws TranslateFault {
 		if (stub == null)
 			init();
 
 		try {
 			SoapServiceStub.Speak speak = new SoapServiceStub.Speak();
 			speak.setAppId(appid);
 			speak.setText(text);
 			speak.setLanguage(language);
 			speak.setFormat(format);
 			SpeakResponse speakOut = stub.speak(speak);
 			return speakOut.getSpeakResult();
 		} catch (RemoteException e) {
 			throw new TranslateFault(e.getMessage());
 		}
 	}
 
 	public String translateLine(String text, String from, String to)
 			throws TranslateFault {
 		if (stub == null)
 			init();
 
 		try {
 			SoapServiceStub.Translate translate = new SoapServiceStub.Translate();
 			translate.setAppId(appid);
 			translate.setText(text);
 			translate.setFrom(from);
 			translate.setTo(to);
 			TranslateResponse result;
 			result = stub.translate(translate);
 			return result.getTranslateResult();
 		} catch (RemoteException e) {
 			if (e.getMessage().contains("NoTranslationFound"))
 				return text;
 			else
 				throw new TranslateFault(e.getMessage());
 		}
 
 	}
 
 	public TranslateArrayResponse[] translateArray(String[] texts, String from, String to) throws TranslateFault {
 		if (stub == null)
 			init();
 
 		try {
 			SoapServiceStub.TranslateArray translateArray = new SoapServiceStub.TranslateArray();
 			SoapServiceStub.ArrayOfstring transArray1 = new SoapServiceStub.ArrayOfstring();
 			transArray1.setString(texts);
 			translateArray.setAppId(appid);
 			translateArray.setTexts(transArray1);
 			translateArray.setFrom(from);
 			translateArray.setTo(to);
 			translateArray.setOptions(options);
 			TranslateArrayResponse1 translateArrayResponse = stub.translateArray(translateArray);
 			return translateArrayResponse.getTranslateArrayResult().getTranslateArrayResponse();
 		} catch (RemoteException e) {
 			throw new TranslateFault(e.getMessage());
 		}
 	}
 
 	// Translate XML file
 	public byte[] TranslateXML(byte[] in) throws XMLStreamException {
 		ByteArrayOutputStream xmlbytesout = new ByteArrayOutputStream();
 		XMLEventWriter writer = null;
 		TreeMap<Integer, String> idOffsetMap = null;
 		StringBuffer blockText = null;
 		Boolean inTextBlock = false;
 		String currentTextBlockID = null;
 		String currentTextLineID = null;
 		String currentStringID = null;
 		XMLEventFactory m_eventFactory = XMLEventFactory.newInstance();
 
 		try {
 			Translate myTranslator = new Translate(
 					"6C9A92CF0DDDEF484F4C4ECEA2C82D8CE591A2AD", "text/plain",
 					"general", "username", null);
 			myTranslator.init();
 			EventProducerConsumer ms = new EventProducerConsumer();
 			XMLEventReader reader = XMLInputFactory.newInstance()
 					.createXMLEventReader(new ByteArrayInputStream(in));
 			writer = XMLOutputFactory.newInstance().createXMLEventWriter(
 					xmlbytesout);
 			while (reader.hasNext()) {
 				XMLEvent event = (XMLEvent) reader.next();
 				// write this event to Consumer (XMLOutputStream)
 
 				if (event.getEventType() == XMLEvent.START_ELEMENT) {
 					StartElement startEvent = event.asStartElement();
 					String startEventName = startEvent.getName().getLocalPart();
 
 					// detect start of a text block
 					if (startEventName.equalsIgnoreCase("textblock")) {
 						// initial state for a new text block
 						inTextBlock = true;
 						idOffsetMap = new TreeMap();
 						blockText = new StringBuffer();
 						currentTextBlockID = null;
 						currentTextLineID = null;
 						currentStringID = null;
 
 						// find the ID
 						currentTextBlockID = iterateAttibutes(startEvent, "ID");
 
 					}
 
 					// start of a text line
 					if (startEventName.equalsIgnoreCase("textline"))
 						currentTextLineID = iterateAttibutes(startEvent, "ID");
 
 					// the alto string
 					if (startEventName.equalsIgnoreCase("string")) {
 						currentStringID = iterateAttibutes(startEvent, "ID");
 						boolean inHyph = false;
 						String theString = null;
 						if (iterateAttibutes(startEvent, "subs_type") != null) {
 							inHyph = true;
 							theString = iterateAttibutes(startEvent,
 									"SUBS_CONTENT");
 						} else {
 							inHyph = false;
 							theString = iterateAttibutes(startEvent, "content");
 							idOffsetMap.put(new Integer(blockText.length()),
 									currentStringID);
 							blockText.append(theString);
 						}
 					}
 
 					if (startEventName.equalsIgnoreCase("sp")) {
 						blockText.append(" ");
 					}
 
 				} else if (currentStringID != null
 						&& event.getEventType() == XMLEvent.END_ELEMENT
 						&& event.asEndElement().getName().getLocalPart()
 								.equalsIgnoreCase("textblock")) {
 					int[] sentences = myTranslator.breakSentences(
 							blockText.toString(), "ar");
 					int sentenceStart = 0;
 					String sentenceStartID = idOffsetMap.firstEntry()
 							.getValue();
 					Integer currentKey = idOffsetMap.firstKey();
 					int sentenceBreakDelta = 0;
 					for (int sentenceLength : sentences) {
 						writer.add(ms.getNewSentenceEvent());
 
 						int sentenceEnd = sentenceStart + sentenceLength
 								- sentenceBreakDelta;
 
 						// calculate the next sentence
 						Integer previousKey = currentKey;
 						while (currentKey != null && currentKey < sentenceEnd) {
 							previousKey = currentKey;
 							currentKey = idOffsetMap.higherKey(currentKey);
 						}
 						if (currentKey == null)
 							currentKey = sentenceEnd;
 
 						String sentenceEndID = idOffsetMap.get(previousKey);
 						sentenceBreakDelta = currentKey - sentenceEnd;
 						if (currentKey > sentenceEnd)
 							sentenceEnd = currentKey;
 
 						writer.add(ms.getNewSentenceStartId(sentenceStartID));
 						writer.add(ms.getNewSentenceEndId(sentenceEndID));
 
 						// calculate translated sentence
 						writer.add(ms.getNewAltEvent());
 						writer.add(ms.getNewAltLang("ar"));
 
 						String translatedString = myTranslator.translateLine(
 								blockText.toString().substring(sentenceStart,
 										sentenceEnd), "en", "ar");
 						// System.out.println("Sentence Start ID: " +
 						// sentenceStartID);
 						// System.out.println(translatedString);
 						writer.add(ms.getNewCharactersEvent(translatedString));
 						writer.add(ms.getAltEndEvent());
 
 						writer.add(ms.getSentenceEndEvent());
 
 						// set ending state for next iteration
 						sentenceStart += sentenceLength;
 						sentenceStartID = idOffsetMap.get(currentKey);
 					}
 
 				}
 
 				writer.add(event);
 
 			}
 			writer.flush();
 		} catch (Exception ex) {
 			ex.printStackTrace();
 			if (writer != null)
 				writer.flush();
 		}
 		return xmlbytesout.toByteArray();
 	}
 
 	private static String iterateAttibutes(StartElement startEvent,
 			String theAttribute) {
 		String returnValue = null;
 		Iterator<Attribute> blockAttributes = startEvent.getAttributes();
 		while (blockAttributes.hasNext()) {
 			Attribute attr = blockAttributes.next();
 			if (attr.getName().getLocalPart().equalsIgnoreCase(theAttribute)) {
 				returnValue = attr.getValue();
 				break;
 			}
 		}
 
 		return returnValue;
 	}
 
 	/**
 	 * @return the appid
 	 */
 	public String getAppid() {
 		return appid;
 	}
 
 	/**
 	 * @param appid
 	 *            the appid to set
 	 */
 	public void setAppid(String appid) {
 		this.appid = appid;
 	}
 
 	/**
 	 * @return the contenttype
 	 */
 	public String getContenttype() {
 		return contenttype;
 	}
 
 	/**
 	 * @param contenttype
 	 *            the contenttype to set
 	 */
 	public void setContenttype(String contenttype) {
 		this.contenttype = contenttype;
 	}
 
 	/**
 	 * @return the category
 	 */
 	public String getCategory() {
 		return category;
 	}
 
 	/**
 	 * @param category
 	 *            the category to set
 	 */
 	public void setCategory(String category) {
 		this.category = category;
 	}
 
 	/**
 	 * @return the user
 	 */
 	public String getUser() {
 		return user;
 	}
 
 	/**
 	 * @param user
 	 *            the user to set
 	 */
 	public void setUser(String user) {
 		this.user = user;
 	}
 
 	/**
 	 * @return the uri
 	 */
 	public String getUri() {
 		return uri;
 	}
 
 	/**
 	 * @param uri
 	 *            the uri to set
 	 */
 	public void setUri(String uri) {
 		this.uri = uri;
 	}
 
 	/**
 	 * @return the stub
 	 */
 	public SoapServiceStub getStub() {
 		return stub;
 	}
 
 	/**
 	 * @param stub
 	 *            the stub to set
 	 */
 	public void setStub(SoapServiceStub stub) {
 		this.stub = stub;
 	}
 
 }
 
 class EventProducerConsumer {
 
 	XMLEventFactory m_eventFactory = XMLEventFactory.newInstance();
 
 	/** Creates a new instance of EventProducerConsumer */
 	public EventProducerConsumer() {
 	}
 
 	/**
 	 * @param args
 	 *            the command line arguments
 	 * @throws XMLStreamException
 	 * @throws IOException
 	 */
 	@SuppressWarnings({ "unchecked", "rawtypes" })
 	/**
 	 * New Character event (with text containing current time) is created using
 	 * XMLEventFactory in case the Characters event passed matches the criteria.
 	 * 
 	 * @param Characters
 	 *            Current character event. return Characters New Characters
 	 *            event.
 	 */
 	public StartElement getNewSentenceEvent() {
 		String name = "Sentence";
 		return m_eventFactory.createStartElement("", null, name);
 	}
 
 	public Attribute getNewSentenceStartId(String startId) {
 		Attribute start = m_eventFactory.createAttribute("Start", startId);
 		return start;
 	}
 
 	public Attribute getNewSentenceEndId(String endId) {
 		Attribute end = m_eventFactory.createAttribute("End", endId);
 		return end;
 	}
 
 	public Characters getNewCharactersEvent(String characters) {
 		return m_eventFactory.createCharacters(characters);
 	}
 
 	public EndElement getSentenceEndEvent() {
 		String name = "Sentence";
 		return m_eventFactory.createEndElement("", null, name);
 	}
 
 	public StartElement getNewAltEvent() {
 		String name = "Alt";
 		return m_eventFactory.createStartElement("", null, name);
 	}
 
 	public Attribute getNewAltLang(String language) {
 		Attribute lang = m_eventFactory.createAttribute("lang", language);
 		return lang;
 	}
 
 	public EndElement getAltEndEvent() {
 		String name = "Alt";
 		return m_eventFactory.createEndElement("", null, name);
 	}
 }
