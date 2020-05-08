 package edu.uic.cs.automatic_reviewer.input.parse;
 
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeMap;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.xml.transform.OutputKeys;
 import javax.xml.transform.TransformerConfigurationException;
 import javax.xml.transform.dom.DOMResult;
 import javax.xml.transform.sax.SAXTransformerFactory;
 import javax.xml.transform.sax.TransformerHandler;
 
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.lang3.StringUtils;
 import org.apache.log4j.Logger;
 import org.apache.tika.parser.ParseContext;
 import org.apache.tika.parser.pdf.PDFParser;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import edu.uic.cs.automatic_reviewer.input.Metadata;
 import edu.uic.cs.automatic_reviewer.input.Paper;
 import edu.uic.cs.automatic_reviewer.input.Paper.Paragraph;
 import edu.uic.cs.automatic_reviewer.misc.Assert;
 import edu.uic.cs.automatic_reviewer.misc.AutomaticReviewerException;
 import edu.uic.cs.automatic_reviewer.misc.LogHelper;
 
 public class PaperParser /* extends AbstractWordOperations */{
 
 	private static final Logger LOGGER = LogHelper.getLogger(PaperParser.class);
 
 	private static final int PARAGRAPH_MIN_CHAR_LENGTH = 300;
 
 	private static final int REFERENCE_MIN_CHAR_LENGTH = 30;
 
 	private static final Pattern REFERENCE_PATTERN = Pattern.compile(
 			"(references|reference)\\s*", Pattern.CASE_INSENSITIVE);
 
 	private static final Pattern FIGURE_PATTERN = Pattern.compile(
 			"^(fig\\.|figure)\\s*\\d+", Pattern.CASE_INSENSITIVE);
 	private static final Pattern TABLE_PATTERN = Pattern.compile(
 			"^(tab\\.|table)\\s*\\d+", Pattern.CASE_INSENSITIVE);
 
 	private static final double NONALPHA_THRESHOLD_RATIO = 0.3;
 
 	private static final String METADATA_NAME_CREATORTOOL = "xmp:CreatorTool";
 
 	private static final String[] IGNORING_PARAGRAPH_PREFIXES = readLines("ignoring_paragraph_prefixes.txt");
 
 	private PDFParser tikaParser = new PDFParser();
 	private TransformerHandler handler;
 
 	private AuthorInfoExtractor authorInfoExtractor = new AuthorInfoExtractorDBLP();
 
 	@SuppressWarnings("unchecked")
 	private static String[] readLines(String fileName) {
 		InputStream input = PaperParser.class.getResourceAsStream(fileName);
 
 		List<String> lines = null;
 		try {
 			lines = IOUtils.readLines(input);
 		} catch (IOException e) {
 			throw new AutomaticReviewerException(e);
 		} finally {
 			IOUtils.closeQuietly(input);
 		}
 
 		return lines.toArray(new String[lines.size()]);
 	}
 
 	public PaperParser() {
 
 		// SAX
 		SAXTransformerFactory saxTransformerFactory = (SAXTransformerFactory) SAXTransformerFactory
 				.newInstance();
 		try {
 			handler = saxTransformerFactory.newTransformerHandler();
 		} catch (TransformerConfigurationException e) {
 			throw new AutomaticReviewerException(e);
 		}
 		handler.getTransformer().setOutputProperty(OutputKeys.METHOD, "xml");
 	}
 
 	public Paper parse(File file) {
 
 		org.apache.tika.metadata.Metadata tikaMetadata = new org.apache.tika.metadata.Metadata();
 		DOMResult domResult = new DOMResult(); // use DOM
 
 		// start parsing using tika
 		parse(file, tikaMetadata, domResult);
 		// end parsing
 
 		Node document = domResult.getNode();
 		try {
 			Paper paper = parseDocument(document);
 			Metadata metadata = extractMetaInfo(paper, tikaMetadata, file);
 			paper.setMetadata(metadata);
 
 			return paper;
 		} catch (Exception e) {
 			throw new AutomaticReviewerException(e);
 		}
 	}
 
 	private Metadata extractMetaInfo(Paper paper,
 			org.apache.tika.metadata.Metadata tikaMetadata, File file) {
 
 		Metadata metadata = new Metadata();
 
 		// figure
 		Map<Integer, Integer> figureNumByPage = countFiguresUsingRegex(paper);
 		if (LOGGER.isDebugEnabled()) {
 			LOGGER.debug("{Page-Number_of_figures}: " + figureNumByPage);
 		}
 		metadata.setNumOfFiguresByPage(figureNumByPage);
 
 		// table
 		Map<Integer, Integer> tableNumByPage = countTablesUsingRegex(paper);
 		if (LOGGER.isDebugEnabled()) {
 			LOGGER.debug("{Page-Number_of_tables}: " + tableNumByPage);
 		}
 		metadata.setNumOfTablesByPage(tableNumByPage);
 
 		// formula
 		Map<Integer, Integer> formulaNumByPage = countFormulas(paper);
 		if (LOGGER.isDebugEnabled()) {
 			LOGGER.debug("{Page-Number_of_formulas}: " + formulaNumByPage);
 		}
 		metadata.setNumOfFormulasByPage(formulaNumByPage);
 
 		String creatorTool = tikaMetadata.get(METADATA_NAME_CREATORTOOL);
 		metadata.setCreatorTool(creatorTool);
 
 		return metadata;
 	}
 
 	private Map<Integer, Integer> countFormulas(Paper paper) {
 		Map<Integer, Integer> countByPage = new TreeMap<Integer, Integer>();
 
 		for (int pageIndex = 1; pageIndex <= paper.getNumOfPages(); pageIndex++) {
 			List<String> paragraphs = paper
 					.getContentParagraphsOnPage(pageIndex);
 			if (paragraphs == null) {
 				countByPage.put(pageIndex, 0);
 				continue;
 			}
 
 			int numOfFormula = 0;
 			boolean startFormula = false;
 			for (String paragraph : paragraphs) {
 				// remove all spaces
 				paragraph = paragraph.replaceAll("\\s+", "");
 
 				if (isFormula(paragraph)) {
 					if (!startFormula) {
 						startFormula = true;
 					}
 				} else if (startFormula) { // end of a formula
 					numOfFormula++;
 					startFormula = false;
 				}
 			}
 			if (startFormula) {
 				numOfFormula++;
 			}
 
 			countByPage.put(pageIndex, numOfFormula);
 		}
 
 		return countByPage;
 	}
 
 	private boolean isFormula(String paragraph) {
 		char[] chArray = paragraph.toCharArray();
 		double numOfNonAlpha = 0.0;
 		for (char ch : chArray) {
 			if (!Character.isLetter(ch)) {
 				numOfNonAlpha++;
 			}
 		}
 
 		// TODO may be optimized
 		if (numOfNonAlpha / chArray.length >= NONALPHA_THRESHOLD_RATIO) {
 			return true;
 		}
 
 		return false;
 	}
 
 	private Map<Integer, Integer> countTablesUsingRegex(Paper paper) {
 		return countUsingRegex(paper, TABLE_PATTERN);
 	}
 
 	private Map<Integer, Integer> countFiguresUsingRegex(Paper paper) {
 		return countUsingRegex(paper, FIGURE_PATTERN);
 	}
 
 	private Map<Integer, Integer> countUsingRegex(Paper paper, Pattern pattern) {
 
 		Map<Integer, Integer> countByPage = new TreeMap<Integer, Integer>();
 
 		for (int pageIndex = 1; pageIndex <= paper.getNumOfPages(); pageIndex++) {
 			List<String> paragraphs = paper
 					.getContentParagraphsOnPage(pageIndex);
 			if (paragraphs == null) {
 				countByPage.put(pageIndex, 0);
 				continue;
 			}
 
 			Set<Integer> numbers = new HashSet<Integer>();
 			for (String paragraph : paragraphs) {
 				Matcher matcher = pattern.matcher(paragraph);
 				while (matcher.find()) {
 					String matchedString = matcher.group();
 					String value = matchedString.replaceAll("\\D", "");
 					try {
 						Integer num = Integer.valueOf(value);
 						numbers.add(num);
 					} catch (Exception e) {
 						// ignore
 					}
 				}
 			}
 
 			countByPage.put(pageIndex, numbers.size());
 		}
 
 		return countByPage;
 	}
 
 	public Paper parse(String filePath) {
 
 		return parse(new File(filePath));
 	}
 
 	private Paper parseDocument(Node document) throws Exception {
 		// (XPath doesn't work! I don't know why...)
 		NodeList nodes = document.getChildNodes();
 		Assert.isTrue(nodes.getLength() == 1);
 
 		Node htmlNode = nodes.item(0);
 		nodes = htmlNode.getChildNodes();
 		Assert.isTrue(nodes.getLength() == 2);
 
 		Node bodyNode = nodes.item(1);
 		NodeList pageNodes = bodyNode.getChildNodes(); // page nodes
 
 		Paper resultPaper = new Paper();
 		resultPaper.setNumOfPages(pageNodes.getLength());
 
 		List<Paragraph> rawParagraphs = new ArrayList<Paragraph>();
 
 		for (int pageIndex = 0; pageIndex < pageNodes.getLength(); pageIndex++) {
 
 			Node pageNode = pageNodes.item(pageIndex);
 			Assert.isTrue("div".equalsIgnoreCase(pageNode.getLocalName()));
 			Node classAttr = pageNode.getAttributes().getNamedItem("class");
 			Assert.isTrue("page".equalsIgnoreCase(classAttr.getNodeValue()));
 
 			Integer pageNum = Integer.valueOf(pageIndex + 1); // based on "1"
 
 			NodeList paragraphNodes = pageNode.getChildNodes();
 			for (int paragraphIndex = 0; paragraphIndex < paragraphNodes
 					.getLength(); paragraphIndex++) {
 
 				Element paragraphNode = (Element) paragraphNodes
 						.item(paragraphIndex);
 				Assert.isTrue("p".equalsIgnoreCase(paragraphNode.getLocalName()));
 
 				// raw paragraph
 				String rawParagraph = paragraphNode.getTextContent();
 				rawParagraphs.add(new Paragraph(rawParagraph, pageNum));
 			}
 		}
 
 		parseRawParagraphs(rawParagraphs, resultPaper);
 
 		return resultPaper;
 	}
 
 	private void parseRawParagraphs(List<Paragraph> rawParagraphs,
 			Paper resultPaper) {
 
 		boolean paperStarted = false; // if entire paper started
 		boolean contentStarted = false; // if the content started
 
 		List<String> rawAuthorInfos = new ArrayList<String>();
 		List<Paragraph> contentParagraphs = new ArrayList<Paragraph>();
 
 		for (int index = 0; index < rawParagraphs.size(); index++) {
 
 			Paragraph paragraph = rawParagraphs.get(index);
 			String tidiedRawParagraph = tidyParagraph(paragraph.getContent());
 
 			if (tidiedRawParagraph.length() == 0 /* && !paperStarted */
 					|| StringUtils.startsWithAny(tidiedRawParagraph,
 							IGNORING_PARAGRAPH_PREFIXES)) {
 				continue;
 			}
 
 			if (!paperStarted) {
 
 				// first paragraph is the title
 				// tidiedRawParagraph = tidiedRawParagraph.split("\\r?\\n")[0];
 				tidiedRawParagraph = tidiedRawParagraph.replaceAll("\\s+", " ");
 				resultPaper.setTitle(tidiedRawParagraph);
 				paperStarted = true;
 
 				LOGGER.info("[TITLE]\t" + tidiedRawParagraph + "\n");
 
 			} else if (!contentStarted) {
 
 				if ("abstract".equalsIgnoreCase(tidiedRawParagraph)) {
 					continue; // ignore paragraph contains only "abstract"
 				} else if (tidiedRawParagraph.length() > PARAGRAPH_MIN_CHAR_LENGTH) {
 					resultPaper.setAbstract(tidiedRawParagraph);
 					contentStarted = true;
 
 					LOGGER.info("[ABSTRACT]\n" + tidiedRawParagraph + "\n");
 
 				} else { // authors and university
 					rawAuthorInfos.add(tidiedRawParagraph);
 				}
 
 			} else { // content
 				contentParagraphs.add(new Paragraph(tidiedRawParagraph,
 						paragraph.getPageNum()));
 			}
 
 		} // end rawParagraphs "for"
 
 		parseAndFillAuthorInfosAndRefillTitleIfNecessary(resultPaper,
 				rawAuthorInfos);
 
 		fillNormalContentAndRefereces(resultPaper, contentParagraphs);
 
 		// System.out.println("=============================================");
 		// if (resultPaper.getAuthors() != null) {
 		// for (Author author : resultPaper.getAuthors()) {
 		// System.out.println(author);
 		// }
 		// }
 	}
 
 	private void parseAndFillAuthorInfosAndRefillTitleIfNecessary(
 			Paper resultPaper, List<String> rawAuthorInfos) {
 		authorInfoExtractor
 				.parseAndFillAuthorInfos(resultPaper, rawAuthorInfos);
 	}
 
 	private void fillNormalContentAndRefereces(Paper resultPaper,
 			List<Paragraph> contentParagraphs) {
 
 		// match from the end of the list, in case there are "reference" key
 		// words within content
 		int index = contentParagraphs.size() - 1;
 		for (; index >= 0; index--) {
 
 			String tidiedRawParagraph = contentParagraphs.get(index)
 					.getContent();
 			if (StringUtils.startsWithIgnoreCase(tidiedRawParagraph,
 					"reference")) {
 				break;
 			}
 
 		}
 
 		fillNormalContentParagraphs(resultPaper, contentParagraphs, index);
 		fillReferences(resultPaper, contentParagraphs, index);
 	}
 
 	private void fillNormalContentParagraphs(Paper resultPaper,
 			List<Paragraph> contentParagraphs, int normalContentEndIndex) {
 
 		for (int index = 0; index < normalContentEndIndex; index++) {
 
 			Paragraph paragraph = contentParagraphs.get(index);
 
 			resultPaper.addContentParagraph(paragraph);
 			if (LOGGER.isDebugEnabled()) {
 				LOGGER.info("[PARAGRAPH]\tP" + paragraph.getPageNum() + " | "
 						+ paragraph.getContent());
 			}
 		}
 
 		removePageNumberOnEachPage(resultPaper);
 	}
 
 	private void removePageNumberOnEachPage(Paper resultPaper) {
 		for (int pageNum = 1; pageNum <= resultPaper.getNumOfPages(); pageNum++) {
 			List<String> paragraphs = resultPaper
 					.getContentParagraphsOnPage(pageNum);
 			if (paragraphs == null) {
 				continue; // reference page
 			}
 
 			String lastParagraph = paragraphs.get(paragraphs.size() - 1);
 			if (StringUtils.isNumeric(lastParagraph)) {
 				paragraphs.remove(paragraphs.size() - 1);
 			}
 		}
 	}
 
 	private void fillReferences(Paper resultPaper,
 			List<Paragraph> contentParagraphs, int referencesStartIndex) {
 
 		String firstReferencePrefix = null;
 		for (int refIndex = referencesStartIndex; refIndex < contentParagraphs
 				.size(); refIndex++) {
 
 			Paragraph paragraph = contentParagraphs.get(refIndex);
 			String tidiedRawParagraph = paragraph.getContent();
 
 			// first line of "reference" may contain some part of the real
 			// reference
 			if (refIndex == referencesStartIndex) {
 				Matcher matcher = REFERENCE_PATTERN.matcher(tidiedRawParagraph);
 				// remove literal "reference"
 				String possibleReference = matcher.replaceFirst("");
 
 				if (possibleReference.trim().length() > 0) {
 					firstReferencePrefix = possibleReference;
 				}
 
 				continue; // skip the first line
 			}
 
 			if (firstReferencePrefix != null) {
 				tidiedRawParagraph = firstReferencePrefix + " "
 						+ tidiedRawParagraph;
 				firstReferencePrefix = null;
 			}
 
 			if (tidiedRawParagraph.length() > REFERENCE_MIN_CHAR_LENGTH) {
 				resultPaper.addReference(tidiedRawParagraph);
 				LOGGER.info("[REFERENCE]\t" + tidiedRawParagraph);
 			}
 
 		}
 	}
 
 	private String tidyParagraph(String paragraphContent) {
 		// TODO maybe do more jobs
 		return paragraphContent.trim();
 	}
 
 	private void parse(File file, org.apache.tika.metadata.Metadata metadata,
 			DOMResult domResult) {
 
 		try {
 			BufferedInputStream inputStream = new BufferedInputStream(
 					new FileInputStream(file));
 
 			// store the result inside a DOM structure
 			handler.setResult(domResult);
 			tikaParser
 					.parse(inputStream, handler, metadata, new ParseContext());
 		} catch (Exception e) {
 			throw new AutomaticReviewerException(e);
 		}
 	}
 
 	// /**
 	// * Not accurate!
 	// *
 	// * @param filePath
 	// * @param countByPage
 	// * @return
 	// */
 	// private int countImagesUsingIText(String filePath,
 	// Map<Integer, Integer> countByPage) {
 	//
 	// int totalCount = 0;
 	// try {
 	// PdfReader iTextReader = new PdfReader(filePath);
 	// PdfReaderContentParser parser = new PdfReaderContentParser(
 	// iTextReader);
 	// for (int index = 1; index <= iTextReader.getNumberOfPages(); index++) {
 	//
 	// ImageCounter imageCounter = new ImageCounter();
 	// parser.processContent(index, imageCounter);
 	//
 	// countByPage.put(index, imageCounter.getCount());
 	// totalCount += imageCounter.getCount();
 	// }
 	// } catch (IOException e) {
 	// throw new AutomaticReviewerException(e);
 	// }
 	//
 	// return totalCount;
 	// }
 
 	public static void main(String[] args) {
 
 		File folder = new File(
 				"D:\\Data\\UIC Documents\\Computer Science\\CS_521\\Project\\papers_acl_2012\\");
 
 		PaperParser paperParser = new PaperParser();
 
 		// List<Paper> papers = new ArrayList<Paper>();
 		for (File file : folder.listFiles()) {
 			if (file.isDirectory()) {
 				continue;
 			}
 
 			Paper paper = paperParser.parse(file);
 			outputPaperInfo(paper);
 		}
 	}
 
 	private static void outputPaperInfo(Paper paper) {
 
 		System.out.println(paper.getTitle());
		// System.out.println("---------------------");
		// for (Author author : paper.getAuthors()) {
		// System.out.println(author);
		// }
 		// System.out.println("---------------------");
 		// System.out.println(paper.getAbstract());
 		// System.out.println("---------------------");
 		// for (String reference : paper.getReferences()) {
 		// System.out.println(reference);
 		// System.out.println("---------");
 		// }
 		System.out.println("---------------------");
 		System.out.println(paper.getNumOfPages() + " pages");
 
 		Metadata metadata = paper.getMetadata();
 		System.out.println("---------------------");
 		System.out.println("Figures:\t" + metadata.getNumOfFiguresByPage());
 		System.out.println("Tables:\t\t" + metadata.getNumOfTablesByPage());
 		System.out.println("Formulas:\t" + metadata.getNumOfFormulasByPage());
 		System.out.println("---------------------");
 		System.out.println(metadata.getCreatorTool());
 		// System.out.println("---------------------");
 		// for (String paragraph : paper.getContentParagraphsOnPage(1)) {
 		// System.out.println(paragraph);
 		// System.out.println("---------");
 		// }
 
 		System.out.println("===========================================");
 
 		System.out.println();
 
 	}
 }
