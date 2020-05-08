 package edu.uic.cs.automatic_reviewer.input.parse;
 
 import java.io.IOException;
 import java.io.StringReader;
 import java.text.Normalizer;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.xpath.XPath;
 import javax.xml.xpath.XPathConstants;
 import javax.xml.xpath.XPathFactory;
 
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.ResponseHandler;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.BasicResponseHandler;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.log4j.Logger;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.InputSource;
 
 import edu.uic.cs.automatic_reviewer.input.Paper;
 import edu.uic.cs.automatic_reviewer.input.Paper.Author;
 import edu.uic.cs.automatic_reviewer.misc.Assert;
 import edu.uic.cs.automatic_reviewer.misc.AutomaticReviewerException;
 import edu.uic.cs.automatic_reviewer.misc.LogHelper;
 
 public class AuthorInfoExtractorDBLP implements AuthorInfoExtractor {
 
 	private static final Logger LOGGER = LogHelper
 			.getLogger(AuthorInfoExtractorDBLP.class);
 
 	private static final Pattern COMPOUND_EMAIL_PATTERN = Pattern
 			.compile("\\{([_a-z0-9-]+(\\.[_a-z0-9-]+)*\\s*,\\s*)*[_a-z0-9-]+(\\.[_a-z0-9-]+)*}@[a-z0-9-]+(\\.[a-z0-9-]+)*(\\.[a-z]{2,4})");
 	private static final Pattern EMAIL_PATTERN = Pattern
 			.compile("[_a-z0-9-]+(\\.[_a-z0-9-]+)*@[a-z0-9-]+(\\.[a-z0-9-]+)*(\\.[a-z]{2,4})");
 
 	private DocumentBuilder documentBuilder = null;
 	private XPath xPath = null;
 
 	public AuthorInfoExtractorDBLP() {
 		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
 		try {
 			documentBuilder = dbFactory.newDocumentBuilder();
 		} catch (ParserConfigurationException e) {
 			throw new AutomaticReviewerException(e);
 		}
 
 		XPathFactory factory = XPathFactory.newInstance();
 		xPath = factory.newXPath();
 	}
 
 	@Override
 	public void parseAndFillAuthorInfos(Paper paper, List<String> rawAuthorInfos) {
 
 		// retrieve names, and re-fill title if we matched one record in DBLP
 		List<String> authorNames = gatherAuthorNamesAndReFillTitle(paper);
 
 		List<Author> authors = gatherEmailAndOrganizationInfos(authorNames,
 				rawAuthorInfos);
 
 		paper.setAuthors(authors);
 
 	}
 
 	private List<Author> gatherEmailAndOrganizationInfos(
 			List<String> authorNames, List<String> rawAuthorInfos) {
 
 		Map<String, String> infoByName = gatherInfosByAuthorName(authorNames,
 				rawAuthorInfos);
 
 		List<Author> result = new ArrayList<Author>(infoByName.size());
 		for (Entry<String, String> entry : infoByName.entrySet()) {
 
 			String name = entry.getKey();
 			String info = entry.getValue();
 
 			Author author = new Author();
 			author.setName(name);
 
 			String organization = info;
 			String email = "";
 
 			Matcher matcher = EMAIL_PATTERN.matcher(info);
 			if (matcher.find()) {
 				email = matcher.group();
 				organization = info.replace(email, "");
 			} else {
 				matcher = COMPOUND_EMAIL_PATTERN.matcher(info);
 				if (matcher.find()) {
 					email = matcher.group();
 					organization = info.replace(email, "");
 				}
 			}
 
 			author.setOrganization(organization.trim());
 			author.setEmail(email);
 
 			result.add(author);
 		}
 
 		return result;
 	}
 
 	private Map<String, String> gatherInfosByAuthorName(
 			List<String> authorNames, List<String> rawAuthorInfos) {
 
 		rawAuthorInfos = splitByLine(rawAuthorInfos);
 
 		List<String> replacedRawInfos = new ArrayList<String>();
 		Map<String, Integer> usedMaxIndexForTerm = new HashMap<String, Integer>();
 
 		for (String rawInfo : rawAuthorInfos) {
 			String[] terms = rawInfo.split("\\s+");
 
 			for (String term : terms) {
 				int nameIndex = findNameIndex(term, authorNames,
 						usedMaxIndexForTerm);
 				if (nameIndex == -1) {
 					continue;
 				}
 
 				rawInfo = rawInfo.replaceFirst(term, "{" + nameIndex + "}");
 			}
 
 			replacedRawInfos.add(rawInfo);
 		}
 
 		boolean[] lineContainsNameOrNot = new boolean[replacedRawInfos.size()];
 		for (int lineIndex = 0; lineIndex < replacedRawInfos.size(); lineIndex++) {
 			String replacedRawInfo = replacedRawInfos.get(lineIndex);
 			if (replacedRawInfo.matches(".*\\{\\d}.*")) {
 				lineContainsNameOrNot[lineIndex] = true;
 			} else {
 				lineContainsNameOrNot[lineIndex] = false;
 			}
 		}
 
 		Map<String, Integer> appearingLineIndexByName = new HashMap<String, Integer>();
 		for (int currentNameIndex = 0; currentNameIndex < authorNames.size(); currentNameIndex++) {
 			String nameSymbol = "{" + currentNameIndex + "}";
 
 			for (int lineIndex = 0; lineIndex < replacedRawInfos.size(); lineIndex++) {
 				String replacedRawInfo = replacedRawInfos.get(lineIndex);
 				if (replacedRawInfo.contains(nameSymbol)) {
 					appearingLineIndexByName.put(
 							authorNames.get(currentNameIndex), lineIndex);
 				}
 			}
 		}
 
 		// use linkedHashMap to maintain the order of authors
 		Map<String, String> infoByName = new LinkedHashMap<String, String>();
 		for (String name : authorNames) {
 			Integer appearingIndex = appearingLineIndexByName.get(name);
 			if (appearingIndex == null) {
 				// use the entire raw info
 				infoByName.put(name, concatenate(rawAuthorInfos));
 				continue;
 			}
 
 			StringBuilder info = new StringBuilder();
 			boolean infoStart = false;
 			for (int index = appearingIndex.intValue() + 1; index < lineContainsNameOrNot.length; index++) {
 
 				if (infoStart) {
 					if (lineContainsNameOrNot[index]) {
 						break; // another name start
 					} else {
 						info.append(replacedRawInfos.get(index)).append("\n");
 					}
 				} else { // we haven't met any info
 					if (lineContainsNameOrNot[index]) {
 						continue;
 					} else { // not name, means it is the beginning of info
 						info.append(replacedRawInfos.get(index)).append("\n");
 						infoStart = true;
 					}
 				}
 			}
 
 			String authorInfo = info.toString().trim();
 			infoByName.put(name, authorInfo);
 		}
 		return infoByName;
 	}
 
 	private String concatenate(List<String> rawAuthorInfos) {
 
 		StringBuilder info = new StringBuilder();
 		for (String str : rawAuthorInfos) {
 			info.append(str).append("\n");
 		}
 		return info.toString().trim();
 	}
 
 	private List<String> splitByLine(List<String> rawAuthorInfos) {
 		List<String> result = new ArrayList<String>();
 		for (String rawLine : rawAuthorInfos) {
 			String[] lines = rawLine.split("\n");
 			result.addAll(Arrays.asList(lines));
 		}
 		return result;
 	}
 
 	private String removeDiacritics(String input) {
 
 		String nrml = Normalizer.normalize(input, Normalizer.Form.NFD);
 		StringBuilder stripped = new StringBuilder();
 		for (int i = 0; i < nrml.length(); ++i) {
 			if (Character.getType(nrml.charAt(i)) != Character.NON_SPACING_MARK) {
 				stripped.append(nrml.charAt(i));
 			}
 		}
 		return stripped.toString();
 	}
 
 	private int findNameIndex(String term, List<String> authorNames,
 			Map<String, Integer> usedMaxIndexForTerm) {
 
 		term = term.replaceAll("\\W", ""); // TODO may have problem here
 		if (term.length() == 0) { // none character
 			return -1;
 		}
 
 		for (int index = 0; index < authorNames.size(); index++) {
 			String name = removeDiacritics(authorNames.get(index));
 			if (name.contains(term)) {
 
 				Integer usedMaxIndex = usedMaxIndexForTerm.get(term);
 				if (usedMaxIndex == null || usedMaxIndex.intValue() < index) {
 					usedMaxIndexForTerm.put(term, index);
 				} else {
 					continue;
 				}
 			}
 		}
 
 		Integer currentUsedMaxIndex = usedMaxIndexForTerm.get(term);
 		return currentUsedMaxIndex == null ? -1 : currentUsedMaxIndex
 				.intValue();
 	}
 
 	private List<String> gatherAuthorNamesAndReFillTitle(Paper paper) {
 
 		List<String> authorNames = new ArrayList<String>();
 
 		String possibleTitle = paper.getTitle();
 		Assert.notEmpty(possibleTitle);
 
 		String title = null;
 		try {
 			while (true) {
 				String retrievedXmlString = retrievePaperInfoFromDBLP(possibleTitle);
 				Document document = documentBuilder.parse(new InputSource(
 						new StringReader(retrievedXmlString)));
 				title = parseAuthorNames(document, authorNames);
 
 				if (title != null) { // matched or match too many
 					break;
 				}
 
 				// noting matched, try shorter title
 				possibleTitle = shrinkPossibleTitle(possibleTitle);
 				if (possibleTitle == null) { // nothing more can be shrunk
 					break;
 				}
 			}
 		} catch (Exception e) {
 			throw new AutomaticReviewerException(e);
 		}
 
 		if (title == null || title.equals("")) {
 			// really nothing matched, or too many matched.
 			// keep the original title
 			// we should use super class's method
 			System.err.println("NO NAME!!");
 			// TODO
 		} else {
 			paper.setTitle(title); // update the title
 			Assert.notEmpty(authorNames);
 			// paper.setAuthors(authorNames);
 		}
 
 		return authorNames;
 	}
 
 	private String shrinkPossibleTitle(String possibleTitle) {
 
 		String shrunkTitle = possibleTitle.substring(0,
 				possibleTitle.lastIndexOf(" "));
 		if (shrunkTitle.equals(possibleTitle)) {
 			return null;
 		}
 
 		return shrunkTitle;
 	}
 
 	// private String parseAuthorNames(Document document, List<String> names)
 	// throws Exception {
 	//
 	// String matchedNumString = xPath.evaluate("/result/hits/@total",
 	// document).trim();
 	// if (matchedNumString.length() == 0) { // nothing matched
 	// return null;
 	// }
 	//
 	// int matchedNum = Integer.parseInt(matchedNumString);
 	// if (matchedNum == 0) { // nothing matched
 	// return null;
 	// } else if (matchedNum > 1) { // should only one matched
 	// return "";
 	// }
 	//
 	// String titleNodeString = xPath.evaluate("//title", document);
 	// System.out.println(titleNodeString);
 	//
 	// String titleNodeXmlString = "<title>" + titleNodeString + "</title>";
 	//
 	// Document titleDocument = documentBuilder.parse(new InputSource(
 	// new StringReader(titleNodeXmlString)));
 	// NodeList authorNodes = (NodeList) xPath.evaluate(
 	// "/title/authors/author", titleDocument, XPathConstants.NODESET);
 	//
 	// Assert.isTrue(names.isEmpty());
 	// for (int index = 0; index < authorNodes.getLength(); index++) {
 	// String name = authorNodes.item(index).getFirstChild()
 	// .getNodeValue();
 	// names.add(name.trim());
 	// }
 	//
 	// String title = xPath.evaluate("/title/title", titleDocument).trim();
 	// if (title.endsWith(".")) {
 	// title = title.substring(0, title.length() - 1);
 	// }
 	//
 	// Assert.notEmpty(title);
 	// Assert.notEmpty(names);
 	// return title;
 	// }
 
 	private String parseAuthorNames(Document document, List<String> names)
 			throws Exception {
 
 		String matchedNumString = xPath.evaluate("/result/hits/@total",
 				document).trim();
 		if (matchedNumString.length() == 0) { // nothing matched
 			return null;
 		}
 
 		int matchedNum = Integer.parseInt(matchedNumString);
 		if (matchedNum == 0) { // nothing matched
 			return null;
 		} else if (matchedNum > 1) { // should only one matched
 			return "";
 		}
 
 		NodeList infoNodeList = (NodeList) xPath.evaluate("//hit/info",
 				document, XPathConstants.NODESET);
 		Assert.isTrue(infoNodeList.getLength() == 1);
 
 		Node infoNode = infoNodeList.item(0);
 
 		NodeList authorNodes = (NodeList) xPath.evaluate("//authors/author",
 				infoNode, XPathConstants.NODESET);
 
 		Assert.isTrue(names.isEmpty());
 		for (int index = 0; index < authorNodes.getLength(); index++) {
 			String name = authorNodes.item(index).getFirstChild()
 					.getNodeValue();
 			names.add(name.trim());
 		}
 
 		String title = xPath.evaluate("//title", infoNode).trim();
 		if (title.endsWith(".")) {
 			title = title.substring(0, title.length() - 1);
 		}
 
 		Assert.notEmpty(title);
 		Assert.notEmpty(names);
 		return title;
 	}
 
 	private String retrievePaperInfoFromDBLP(String title) {
 
 		if (LOGGER.isDebugEnabled()) {
 			LOGGER.debug(LogHelper.LOG_LAYER_ONE_BEGIN
 					+ "retrieve info from DBLP for title [" + title + "]");
 		}
 
 		title = title.replace("-", " ").replace(" ", "*%20")
				.replaceAll(":", "").toLowerCase(Locale.US);
 		title = title + "*"; // add the last "*"
 		String queryUrl = "http://www.dblp.org/search/api/?q=" + title
 				+ "&format=xml";
 
 		HttpClient httpclient = new DefaultHttpClient();
 		try {
 			HttpGet httpget = new HttpGet(queryUrl);
 
 			// Create a response handler
 			ResponseHandler<String> responseHandler = new BasicResponseHandler();
 			try {
 				String responseBody = httpclient.execute(httpget,
 						responseHandler);
 
 				if (LOGGER.isDebugEnabled()) {
 					LOGGER.debug(LogHelper.LOG_LAYER_ONE_END
 							+ "retrieved info from DBLP, response ["
 							+ responseBody + "]");
 				}
 
 				return responseBody;
 			} catch (IOException e) {
 				throw new AutomaticReviewerException(e);
 			}
 		} finally {
 			httpclient.getConnectionManager().shutdown();
 		}
 
 	}
 
 }
