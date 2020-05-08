 package com.redhat.topicindex.syntaxchecker;
 
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import javax.ws.rs.core.PathSegment;
 
 import net.htmlparser.jericho.Source;
 
 import org.codehaus.jackson.map.ObjectMapper;
 import org.jboss.resteasy.client.ProxyFactory;
 import org.jboss.resteasy.plugins.providers.RegisterBuiltin;
 import org.jboss.resteasy.specimpl.PathSegmentImpl;
 import org.jboss.resteasy.spi.ResteasyProviderFactory;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.w3c.dom.Text;
 
 import com.redhat.ecs.commonutils.CollectionUtilities;
 import com.redhat.ecs.commonutils.ExceptionUtilities;
 import com.redhat.ecs.commonutils.XMLUtilities;
 import com.redhat.ecs.servicepojo.ServiceStarter;
 import com.redhat.ecs.services.docbookcompiling.xmlprocessing.XMLPreProcessor;
 import com.redhat.topicindex.rest.collections.BaseRestCollectionV1;
 import com.redhat.topicindex.rest.entities.PropertyTagV1;
 import com.redhat.topicindex.rest.entities.StringConstantV1;
 import com.redhat.topicindex.rest.entities.TagV1;
 import com.redhat.topicindex.rest.entities.TopicV1;
 import com.redhat.topicindex.rest.expand.ExpandDataDetails;
 import com.redhat.topicindex.rest.expand.ExpandDataTrunk;
 import com.redhat.topicindex.rest.sharedinterface.RESTInterfaceV1;
 import com.redhat.topicindex.syntaxchecker.data.SpellingErrorData;
 
 import dk.dren.hunspell.Hunspell;
 import dk.dren.hunspell.Hunspell.Dictionary;
 
 public class Main
 {
 	/**
 	 * The system property that defines the query against which to run the
 	 * content checks
 	 */
 	private static final String SPELL_CHECK_QUERY_SYSTEM_PROPERTY = "topicIndex.spellCheckQuery";
 	/** The string constant that defines the Docbook elements to ignore */
 	private static final Integer DOCBOOK_IGNORE_ELEMENTS_STRING_CONSTANT_ID = 30;
 	/** The property tag that holds the gammar errors */
 	private static final Integer GRAMMAR_ERRORS_PROPERTY_TAG_ID = 27;
 	/** The property tag that holds the spelling errors */
 	private static final Integer SPELLING_ERRORS_PROPERTY_TAG_ID = 26;
 	/** The tag that indicates that a topic has a spelling error */
 	private static final Integer SPELLING_ERRORS_TAG_ID = 456;
 	/** The tag that indicates that a topic has a grammar error */
 	private static final Integer GRAMMAR_ERRORS_TAG_ID = 457;
 	/** http://en.wikipedia.org/wiki/Regular_expression#POSIX_character_classes **/
 	private static final String PUNCTUATION_CHARACTERS_RE = "[\\]\\[!\"#$%&'()*+,./:;<=>?@\\^`{|}~\\s]";
 	/** A regex that matches an xref */
 	private static final String XREF_RE = "<xref*.?/\\s*>";
 	/** A regex that matches the opening tag of an entry */
 	private static final String ENTRY_RE = "<entry>";
 	/** A regext that matches a closing tag of an entry */
 	private static final String ENTRY_CLOSE_RE = "</entry>";
 	/**
 	 * A string that is used to replace ignored elements, to indicate that there
 	 * was a break between words before the element was removed
 	 */
 	private static final String ELEMENT_PUNCTUATION_MARKER = "#";
 	/** The Jackson mapper that converts POJOs to JSON */
 	private final ObjectMapper mapper = new ObjectMapper();
 
 	/** Entry point */
 	public static void main(final String[] args)
 	{
 		System.out.println("-> Main.main()");
 		
 		final ServiceStarter starter = new ServiceStarter();
 		if (starter.isValid())
 		{
 			RegisterBuiltin.register(ResteasyProviderFactory.getInstance());
 			new Main(starter);
 		}
 		
 		System.out.println("<- Main.main()");
 	}
 
 	public Main(final ServiceStarter serviceStarter)
 	{
 		System.out.println("-> Main.Main()");
 		
 		final String query = System.getProperty(SPELL_CHECK_QUERY_SYSTEM_PROPERTY);
 
 		try
 		{
 			/* Get the topics */
 			
 			System.out.println("Main.Main() - Getting topics from query " + query);
 			
 			final RESTInterfaceV1 restClient = ProxyFactory.create(RESTInterfaceV1.class, serviceStarter.getSkynetServer());
 
 			final PathSegment pathSegment = new PathSegmentImpl(query, false);
 
 			final ExpandDataTrunk expand = new ExpandDataTrunk();
 
 			final ExpandDataTrunk topicsExpand = new ExpandDataTrunk(new ExpandDataDetails("topics"));
 			expand.setBranches(CollectionUtilities.toArrayList(topicsExpand));
 
 			final ExpandDataTrunk tagsExpand = new ExpandDataTrunk(new ExpandDataDetails("tags"));
 			final ExpandDataTrunk propertiesExpand = new ExpandDataTrunk(new ExpandDataDetails("properties"));
 			topicsExpand.setBranches(CollectionUtilities.toArrayList(tagsExpand, propertiesExpand));
 
 			final String expandString = mapper.writeValueAsString(expand);
 			final String expandEncodedStrnig = URLEncoder.encode(expandString, "UTF-8");
 
 			final BaseRestCollectionV1<TopicV1> topics = restClient.getJSONTopicsWithQuery(pathSegment, expandEncodedStrnig);
 			
 			/* Get the tags to ignore */
 			final StringConstantV1 ignoreTags = restClient.getJSONStringConstant(DOCBOOK_IGNORE_ELEMENTS_STRING_CONSTANT_ID, "");
 			final List<String> ignoreTagsList = CollectionUtilities.toArrayList(ignoreTags.getValue().split("\r\n"));
 
 			/* Create the dictionaries */
 			final Dictionary standardDict = Hunspell.getInstance().getDictionary("target/classes/dict/en_US/en_US");
 			final Dictionary customDict = Hunspell.getInstance().getDictionary("target/classes/customdict/en_US/en_US");
 
 			/* Process the topics */
 			for (final TopicV1 topic : topics.getItems())
 			{
 				processDocument(restClient, topic, ignoreTagsList, standardDict, customDict);
 			}
 		}
 		catch (final Exception ex)
 		{
 			ExceptionUtilities.handleException(ex);
 		}
 		
 		System.out.println("<- Main.Main()");
 	}
 
 	/**
 	 * Process the topic for spelling and grammar issues
 	 * 
 	 * @param topic
 	 *            The topic to process
 	 * @param ignoreElements
 	 *            The XML elements to ignore
 	 * @param standardDict
 	 *            The standard dictionary
 	 * @param customDict
 	 *            The custom dictionary
 	 */
 	private void processDocument(final RESTInterfaceV1 restClient, final TopicV1 topic, final List<String> ignoreElements, final Dictionary standardDict, final Dictionary customDict)
 	{
 		/* Run the content checks */
 		final List<SpellingErrorData> spellingErrors = checkSpelling(topic, ignoreElements, standardDict, customDict);
 		final List<String> doubleWords = checkGrammar(topic, ignoreElements);
 
 		/*
 		 * The topic will be updated to remove the tags and property tags
 		 * regardless of the results of the content checks. If errors are found,
 		 * the property tags will be added back with the new details.
 		 */
 		boolean topicIsUpdated = false;
 
 		final TopicV1 updateTopic = new TopicV1();
 		updateTopic.setId(topic.getId());
 		updateTopic.setPropertiesExplicit(new BaseRestCollectionV1<PropertyTagV1>());
 		updateTopic.setTagsExplicit(new BaseRestCollectionV1<TagV1>());
 
 		/* remove any old spelling error details */
 		for (final PropertyTagV1 tag : topic.getProperties().getItems())
 		{
 			if (tag.getId().equals(GRAMMAR_ERRORS_PROPERTY_TAG_ID))
 			{
 				final PropertyTagV1 removeGrammarErrorPropertyTag = new PropertyTagV1();
 				removeGrammarErrorPropertyTag.setId(GRAMMAR_ERRORS_PROPERTY_TAG_ID);
 				removeGrammarErrorPropertyTag.setValue(tag.getValue());
 				removeGrammarErrorPropertyTag.setRemoveItem(true);
 				updateTopic.getProperties().addItem(removeGrammarErrorPropertyTag);
 				topicIsUpdated = true;
 			}
 		}
 
 		/* remove any old grammar error details */
 		for (final PropertyTagV1 tag : topic.getProperties().getItems())
 		{
 			if (tag.getId().equals(SPELLING_ERRORS_PROPERTY_TAG_ID))
 			{
 				final PropertyTagV1 removeSpellingErrorPropertyTag = new PropertyTagV1();
 				removeSpellingErrorPropertyTag.setId(SPELLING_ERRORS_PROPERTY_TAG_ID);
 				removeSpellingErrorPropertyTag.setValue(tag.getValue());
 				removeSpellingErrorPropertyTag.setRemoveItem(true);
 				updateTopic.getProperties().addItem(removeSpellingErrorPropertyTag);
 				topicIsUpdated = true;
 			}
 		}
 
 		/* Add or remove the spelling tags as needed */
 		boolean foundSpellingTag = false;
 		for (final TagV1 tag : topic.getTags().getItems())
 		{
 			if (tag.getId().equals(SPELLING_ERRORS_TAG_ID))
 			{
 				foundSpellingTag = true;
 				break;
 			}
 		}
 
 		if (spellingErrors.size() == 0 && foundSpellingTag)
 		{
 			final TagV1 removeSpellingErrorTag = new TagV1();
 			removeSpellingErrorTag.setRemoveItem(true);
 			removeSpellingErrorTag.setId(SPELLING_ERRORS_TAG_ID);
 			updateTopic.getTags().addItem(removeSpellingErrorTag);
 			topicIsUpdated = true;
 		}
 		else if (spellingErrors.size() != 0 && !foundSpellingTag)
 		{
 			final TagV1 removeSpellingErrorTag = new TagV1();
 			removeSpellingErrorTag.setAddItem(true);
 			removeSpellingErrorTag.setId(SPELLING_ERRORS_TAG_ID);
 			updateTopic.getTags().addItem(removeSpellingErrorTag);
 			topicIsUpdated = true;
 		}
 
 		/* Add or remove the grammar tags as needed */
 		boolean foundGrammarTag = false;
 		for (final TagV1 tag : topic.getTags().getItems())
 		{
 			if (tag.getId().equals(GRAMMAR_ERRORS_TAG_ID))
 			{
 				foundGrammarTag = true;
 				break;
 			}
 		}
 
 		if (doubleWords.size() == 0 && foundGrammarTag)
 		{
 			final TagV1 removeGrammarErrorTag = new TagV1();
 			removeGrammarErrorTag.setRemoveItem(true);
 			removeGrammarErrorTag.setId(GRAMMAR_ERRORS_TAG_ID);
 			updateTopic.getTags().addItem(removeGrammarErrorTag);
 			topicIsUpdated = true;
 		}
 		else if (doubleWords.size() != 0 && !foundGrammarTag)
 		{
 			final TagV1 grammarErrorTag = new TagV1();
 			grammarErrorTag.setAddItem(true);
 			grammarErrorTag.setId(GRAMMAR_ERRORS_TAG_ID);
 			updateTopic.getTags().addItem(grammarErrorTag);
 			topicIsUpdated = true;
 		}
 
 		/* build up the property tags */
 		if (spellingErrors.size() != 0 || doubleWords.size() != 0)
 		{
 			topicIsUpdated = true;
 
 			System.out.println("Topic ID: " + topic.getId());
 			System.out.println("Topic Title: " + topic.getTitle());
 
 			if (doubleWords.size() != 0)
 			{
 				final StringBuilder doubleWordErrors = new StringBuilder();
 
 				if (doubleWords.size() != 0)
 				{
 					doubleWordErrors.append("Repeated Words: " + CollectionUtilities.toSeperatedString(doubleWords, ", "));
 					System.out.println(doubleWordErrors.toString());
 				}
 
 				final PropertyTagV1 addGrammarErrorTag = new PropertyTagV1();
 				addGrammarErrorTag.setId(GRAMMAR_ERRORS_PROPERTY_TAG_ID);
 				addGrammarErrorTag.setAddItem(true);
 				addGrammarErrorTag.setValueExplicit(doubleWordErrors.toString());
 
 				updateTopic.getProperties().addItem(addGrammarErrorTag);
 			}
 
 			if (spellingErrors.size() != 0)
 			{
 				final StringBuilder spellingErrorsMessage = new StringBuilder();
 
 				int longestWord = 0;
 				for (final SpellingErrorData error : spellingErrors)
 				{
 					final int wordLength = error.getMisspelledWord().length() + (error.getMispellCount() != 1 ? 5 : 0);
 					longestWord = wordLength > longestWord ? wordLength : longestWord;
 				}
 
 				for (final SpellingErrorData error : spellingErrors)
 				{
 					final StringBuilder spaces = new StringBuilder();
 					for (int i = error.getMisspelledWord().length() + (error.getMispellCount() != 1 ? 5 : 0); i < longestWord; ++i)
 					{
 						spaces.append(" ");
 					}
 
 					spellingErrorsMessage.append(error.getMisspelledWord());
 					if (error.getMispellCount() != 1)
 					{
 						spellingErrorsMessage.append(" [x" + error.getMispellCount() + "]");
 					}
 					spellingErrorsMessage.append(":" + spaces.toString() + " ");
 					spellingErrorsMessage.append(CollectionUtilities.toSeperatedString(error.getSuggestions(), ", "));
 					spellingErrorsMessage.append("\n");
 				}
 
 				System.out.println(spellingErrorsMessage.toString());
 
 				/* Update the database */
 				final PropertyTagV1 addSpellingErrorTag = new PropertyTagV1();
 				addSpellingErrorTag.setId(SPELLING_ERRORS_PROPERTY_TAG_ID);
 				addSpellingErrorTag.setAddItem(true);
 				addSpellingErrorTag.setValueExplicit(spellingErrorsMessage.toString());
 
 				updateTopic.getProperties().addItem(addSpellingErrorTag);
 			}
 			else
 			{
 				System.out.println();
 			}
 		}
 
 		/*
 		 * Update the topic in the database if there are changes that need to be
 		 * persisted
 		 */
 		if (topicIsUpdated)
 		{
 			try
 			{
 				/*
 				 * final ExpandDataTrunk expand = new ExpandDataTrunk();
 				 * 
 				 * final ExpandDataTrunk tagsExpand = new ExpandDataTrunk(new
 				 * ExpandDataDetails("tags")); final ExpandDataTrunk
 				 * propertyTagsExpand = new ExpandDataTrunk(new
 				 * ExpandDataDetails("properties"));
 				 * 
 				 * expand.setBranches(CollectionUtilities.toArrayList(tagsExpand,
 				 * propertyTagsExpand));
 				 * 
 				 * final String expandString =
 				 * mapper.writeValueAsString(expand); final String
 				 * expandEncodedStrnig = URLEncoder.encode(expandString,
 				 * "UTF-8");
 				 * 
 				 * final TopicV1 updatedTopic =
 				 * restClient.updateJSONTopic(expandEncodedStrnig, updateTopic);
 				 * System.out.println(updatedTopic.getId());
 				 */
 
 				restClient.updateJSONTopic("", updateTopic);
 			}
 			catch (final Exception ex)
 			{
 				ExceptionUtilities.handleException(ex);
 			}
 		}
 	}
 
 	/**
 	 * Checks the topic for spelling errors
 	 * 
 	 * @param topic
 	 *            The topic to process
 	 * @param ignoreElements
 	 *            The XML elements to ignore
 	 * @param standardDict
 	 *            The standard dictionary
 	 * @param customDict
 	 *            The custom dictionary
 	 * @return A collection of spelling errors, their frequency, and suggested
 	 *         replacements
 	 */
 	private List<SpellingErrorData> checkSpelling(final TopicV1 topic, final List<String> ignoreElements, final Dictionary standarddict, final Dictionary customDict)
 	{
 		/*
 		 * prepare the topic xml for a spell check
 		 */
 		final Document doc = XMLUtilities.convertStringToDocument(topic.getXml());
 		stripOutIgnoredElements(doc, ignoreElements);
 		final String cleanedXML = XMLUtilities.convertDocumentToString(doc, "UTF-8").replaceAll("\n", " ");
 
 		final Source source = new Source(cleanedXML);
 		final String xmlText = source.getRenderer().toString();
 
 		/* Get the word list */
 		final List<String> xmlTextWords = CollectionUtilities.toArrayList(xmlText.split(PUNCTUATION_CHARACTERS_RE + "+"));
 
 		/* Some collections to hold the spelling error details */
 		final Map<String, SpellingErrorData> misspelledWords = new HashMap<String, SpellingErrorData>();
 
 		/* Check for spelling */
 		for (int i = 0; i < xmlTextWords.size(); ++i)
 		{
 			final String word = xmlTextWords.get(i);
 			final String trimmedWord = word.trim();
 
 			/* make sure we are not looking at a blank string, or a combination of underscores and dashes */
 			if (!trimmedWord.isEmpty() &&
				!trimmedWord.matches("[_\\-]+)"))
 			{
 				/* Check spelling */
 				final boolean standardDictMispelled = standarddict.misspelled(word);
 				final boolean customDictMispelled = customDict.misspelled(word);
 
 				if (standardDictMispelled && customDictMispelled)
 				{
 					if (misspelledWords.containsKey(word))
 					{
 						misspelledWords.get(word).incMispellCount();
 					}
 					else
 					{
 						final List<String> suggestions = standarddict.suggest(word);
 						CollectionUtilities.addAllThatDontExist(customDict.suggest(word), suggestions);
 						Collections.sort(suggestions);
 
 						misspelledWords.put(word, new SpellingErrorData(word, suggestions));
 					}
 				}
 			}
 		}
 
 		return CollectionUtilities.toArrayList(misspelledWords.values());
 	}
 
 	/**
 	 * Checks the Docbook XML for common grammar errors
 	 * 
 	 * @param topic
 	 *            The topic to process
 	 * @param ignoreElements
 	 *            The list of XML elements to ignore
 	 * @return A list of grammar errors that were found
 	 */
 	private List<String> checkGrammar(final TopicV1 topic, final List<String> ignoreElements)
 	{
 		/*
 		 * prepare the topic xml for a grammar check
 		 */
 		final Document grammarDoc = XMLUtilities.convertStringToDocument(topic.getXml());
 		replaceIgnoredElements(grammarDoc, ignoreElements);
 		final String grammarCleanedXML = XMLUtilities.convertDocumentToString(grammarDoc, "UTF-8").replaceAll("\n", " ");
 
 		final Source grammarSource = new Source(replaceElementsWithMarkers(grammarCleanedXML));
 		final String grammarXmlText = grammarSource.getRenderer().toString();
 
 		/* Get the grammar word list */
 		final List<String> xmlTextWordsForDoubleChecking = CollectionUtilities.toArrayList(grammarXmlText.split("\\s+"));
 
 		final List<String> doubleWords = new ArrayList<String>();
 
 		/* Check for double words */
 		for (int i = 0; i < xmlTextWordsForDoubleChecking.size(); ++i)
 		{
 			final String word = xmlTextWordsForDoubleChecking.get(i);
 
 			if (!word.trim().isEmpty())
 			{
 				/* Check for doubled words */
 				if (i != 0)
 				{
 					/* don't detected numbers */
 					try
 					{
 						Double.parseDouble(word);
 						continue;
 					}
 					catch (final Exception ex)
 					{
 
 					}
 
 					/* make sure the "word" is not just punctuation */
 					if (word.matches(PUNCTUATION_CHARACTERS_RE + "+"))
 						continue;
 
 					if (word.toLowerCase().equals(xmlTextWordsForDoubleChecking.get(i - 1)))
 					{
 						if (!doubleWords.contains(word + " " + word))
 							doubleWords.add(word + " " + word);
 					}
 				}
 			}
 		}
 
 		return doubleWords;
 	}
 
 	/**
 	 * When converting XML to plain text, the loss of some elements causes
 	 * unintended side effects for the grammar checks. A sentence such as
 	 * "Refer to <xref linkend="something"/> to find out more information" will
 	 * appear to have repeated the word "to" when the xref is removed.
 	 * 
 	 * This method will replace these elements with a punctuation marker, which
 	 * is then used to break up the sequence of words to prevent these false
 	 * positivies.
 	 * 
 	 * @param input
 	 *            The XML to be processed
 	 * @return The XML with certain tags replaced with a punctuation marker
 	 */
 	private String replaceElementsWithMarkers(final String input)
 	{
 		return input.replaceAll(XREF_RE, ELEMENT_PUNCTUATION_MARKER).replaceAll(ENTRY_RE, ELEMENT_PUNCTUATION_MARKER).replaceAll(ENTRY_CLOSE_RE, ELEMENT_PUNCTUATION_MARKER).replaceAll("<!--" + XMLPreProcessor.CUSTOM_INJECTION_SEQUENCE_RE + "-->", ELEMENT_PUNCTUATION_MARKER)
 				.replaceAll("<!--" + XMLPreProcessor.CUSTOM_INJECTION_LIST_RE + "-->", ELEMENT_PUNCTUATION_MARKER).replaceAll("<!--" + XMLPreProcessor.CUSTOM_INJECTION_LISTITEMS_RE + "-->", ELEMENT_PUNCTUATION_MARKER)
 				.replaceAll("<!--" + XMLPreProcessor.CUSTOM_ALPHA_SORT_INJECTION_LIST_RE + "-->", ELEMENT_PUNCTUATION_MARKER).replaceAll("<!--" + XMLPreProcessor.CUSTOM_INJECTION_SINGLE_RE + "-->", ELEMENT_PUNCTUATION_MARKER)
 				.replaceAll("<!--" + XMLPreProcessor.INJECT_CONTENT_FRAGMENT_RE + "-->", ELEMENT_PUNCTUATION_MARKER).replaceAll("<!--" + XMLPreProcessor.INJECT_TITLE_FRAGMENT_RE + "-->", ELEMENT_PUNCTUATION_MARKER);
 	}
 
 	/**
 	 * Here we remove any nodes that we don't want to include in the spell check
 	 * 
 	 * @param node
 	 *            The node to process
 	 * @param ignoreElements
 	 *            The list of elements that are to be ignored
 	 */
 	private void stripOutIgnoredElements(final Node node, final List<String> ignoreElements)
 	{
 		final List<Node> removeNodes = new ArrayList<Node>();
 
 		for (int i = 0; i < node.getChildNodes().getLength(); ++i)
 		{
 			final Node childNode = node.getChildNodes().item(i);
 
 			for (final String ignoreElement : ignoreElements)
 			{
 				if (childNode.getNodeName().toLowerCase().equals(ignoreElement.toLowerCase()))
 				{
 					removeNodes.add(childNode);
 				}
 			}
 		}
 
 		for (final Node removeNode : removeNodes)
 		{
 			node.removeChild(removeNode);
 		}
 
 		for (int i = 0; i < node.getChildNodes().getLength(); ++i)
 		{
 			final Node childNode = node.getChildNodes().item(i);
 			stripOutIgnoredElements(childNode, ignoreElements);
 		}
 	}
 
 	/**
 	 * Here we replace any nodes that we don't want to include in the grammar
 	 * checks with punctuation marks
 	 * 
 	 * @param node
 	 *            The node to process
 	 * @param ignoreElements
 	 *            The list of elements that are to be ignored
 	 */
 	private void replaceIgnoredElements(final Node node, final List<String> ignoreElements)
 	{
 		final List<Node> removeNodes = new ArrayList<Node>();
 
 		for (int i = 0; i < node.getChildNodes().getLength(); ++i)
 		{
 			final Node childNode = node.getChildNodes().item(i);
 
 			for (final String ignoreElement : ignoreElements)
 			{
 				if (childNode.getNodeName().toLowerCase().equals(ignoreElement.toLowerCase()))
 				{
 					removeNodes.add(childNode);
 				}
 			}
 		}
 
 		/*
 		 * Loop through the nodes we found for removal, and insert an
 		 * "innocuous" punctuation mark that is used to prevent unintended
 		 * run-ons when the ignored node is removed.
 		 */
 		for (final Node removeNode : removeNodes)
 		{
 			final Text textnode = node.getOwnerDocument().createTextNode(" " + ELEMENT_PUNCTUATION_MARKER + " ");
 			node.insertBefore(textnode, removeNode);
 			node.removeChild(removeNode);
 		}
 
 		for (int i = 0; i < node.getChildNodes().getLength(); ++i)
 		{
 			final Node childNode = node.getChildNodes().item(i);
 			replaceIgnoredElements(childNode, ignoreElements);
 		}
 	}
 }
