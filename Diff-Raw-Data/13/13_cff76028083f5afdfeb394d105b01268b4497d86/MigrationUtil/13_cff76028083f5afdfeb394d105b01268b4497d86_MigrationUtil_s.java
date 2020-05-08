 /**
  * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, version 2.1, dated February 1999.
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the latest version of the GNU Lesser General
  * Public License as published by the Free Software Foundation;
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with this program (LICENSE.txt); if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
  */
 package org.jamwiki.migrate;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import org.apache.commons.lang.StringUtils;
 import org.jamwiki.DataAccessException;
 import org.jamwiki.WikiBase;
 import org.jamwiki.WikiException;
 import org.jamwiki.WikiMessage;
 import org.jamwiki.model.Topic;
 import org.jamwiki.model.TopicType;
 import org.jamwiki.model.TopicVersion;
 import org.jamwiki.model.WikiUser;
 import org.jamwiki.parser.ParserException;
 import org.jamwiki.parser.ParserOutput;
 import org.jamwiki.parser.ParserUtil;
 import org.jamwiki.utils.Utilities;
 import org.jamwiki.utils.WikiLogger;
 
 /**
  * This class provides utility methods helpful when importing and exporting
  * file data.
  */
 public class MigrationUtil {
 
 	private static final WikiLogger logger = WikiLogger.getLogger(MigrationUtil.class.getName());
 
 	/**
 	 * Given a file and a list of topic names, export the topics to the file.
 	 *
 	 * @param file The file that contains topic data to be parsed.
 	 * @param virtualWiki The virtual wiki to which the topic name list belongs.
 	 * @param topicNames A list of topic names to be exported.
 	 * @param excludeHistory Set to <code>true</code> if only the most recent topic
 	 *  version, not the full topic history, should be exported.
 	 * @throws MigrationException Thrown if a parsing error or data update error is
 	 *  thrown while trying to parse and commit topic data.
 	 * @throws WikiException Thrown if there is no topic data available.
 	 */
 	public static void exportToFile(File file, String virtualWiki, List<String> topicNames, boolean excludeHistory) throws MigrationException, WikiException {
 		TopicExporter exporter = new MediaWikiXmlExporter();
 		exporter.exportToFile(file, virtualWiki, topicNames, excludeHistory);
 	}
 
 	/**
 	 * Given a file containing import information, parse the file and commit all
 	 * topic information within it.
 	 *
 	 * @param file The file that contains topic data to be parsed.
 	 * @param virtualWiki The virtual wiki to write the topic data to.
 	 * @param user The user (if any) that is performing the import.
 	 * @param authorDisplay The display value for the user that is performing the
 	 *  import.  This value is typically the user's IP address.
 	 * @param locale The locale for the user that is performing the import.
 	 * @param errors A list of errors that will be updating if errors are encountered
 	 *  during parsing.
 	 * @return A list of topic names that are successfully parsed and committed to
 	 *  the database.
 	 * @throws MigrationException Thrown if a parsing error or data update error is
 	 *  thrown while trying to parse and commit topic data.
 	 * @throws WikiException Thrown if there is no topic data available.
 	 */
 	public static List<String> importFromFile(File file, String virtualWiki, WikiUser user, String authorDisplay, Locale locale) throws MigrationException, WikiException {
 		TopicImporter importer = new MediaWikiXmlImporter();
 		long start = System.currentTimeMillis();
 		Map<Topic, List<Integer>> parsedTopics = null;
 		try {
 			parsedTopics = importer.importFromFile(file, virtualWiki);
 		} catch (MigrationException e) {
 			if (e.getCause() instanceof WikiException) {
				throw (MigrationException)(e.getCause());
 			}
 			throw e;
 		}
 		logger.fine("Parsed XML " + file.getAbsolutePath() + " in " + ((System.currentTimeMillis() - start) / 1000.000) + " s.");
 		if (parsedTopics.isEmpty()) {
 			throw new WikiException(new WikiMessage("import.error.notopic"));
 		}
 		List<String> successfulImports = new ArrayList<String>();
 		for (Topic topic : parsedTopics.keySet()) {
 			// create a dummy version to indicate that the topic was imported
 			String importedBy = (user != null && user.getUserId() > 0) ? user.getUsername() : authorDisplay;
 			String editComment = Utilities.formatMessage("import.message.importedby", locale, new Object[]{importedBy});
 			TopicVersion topicVersion = new TopicVersion(user, authorDisplay, editComment, topic.getTopicContent(), 0);
 			topicVersion.setEditType(TopicVersion.EDIT_IMPORT);
 			ParserOutput parserOutput = null;
 			try {
 				parserOutput = ParserUtil.parserOutput(topicVersion.getVersionContent(), virtualWiki, topic.getName());
 			} catch (ParserException e) {
 				throw new MigrationException("Failure while parsing topic version of topic: " + topic.getName(), e);
 			}
 			if (!StringUtils.isBlank(parserOutput.getRedirect())) {
 				// set up a redirect
 				topic.setRedirectTo(parserOutput.getRedirect());
 				topic.setTopicType(TopicType.REDIRECT);
 			}
 			try {
 				WikiBase.getDataHandler().writeTopic(topic, topicVersion, parserOutput.getCategories(), parserOutput.getLinks());
 			} catch (DataAccessException e) {
 				throw new MigrationException("Data access exception while processing topic " + topic.getName(), e);
 			}
 			successfulImports.add(topic.getName());
 		}
 		return successfulImports;
 	}
 }
