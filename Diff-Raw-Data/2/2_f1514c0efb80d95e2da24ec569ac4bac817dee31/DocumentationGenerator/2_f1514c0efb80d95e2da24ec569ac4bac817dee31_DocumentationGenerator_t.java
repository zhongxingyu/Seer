 package de.devboost.natspec.library.documentation;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.StringWriter;
 
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.lang.StringEscapeUtils;
 
 import de.devboost.natspec.library.documentation.util.DocumentationSwitch;
 
 public class DocumentationGenerator extends DocumentationSwitch<String> {
 
 	public static final String DOC_PATH = "./doc/";
 	public static final String DOC_FRAGMENT_PATH = "./doc/fragment/";
	public static final String DOC_IMAGE_PATH = "./images/";
 	private static final String DEFAULT_CSS_FILENAME = "css.css";
 
 	private int sectionCount;
 	private int subsectionCount;
 	private int subsubsectionCount;
 	private int figureCounter = 1;
 	private int entryCounter;
 	private int xmlCounter = 1;
 	
 	public DocumentationGenerator() {
 	}
 
 	@Override
 	public String caseDocumentation(Documentation documentation) {
 		String result = "<h1 class=\"title\">" + documentation.getTitle()
 				+ "</h1>\n";
 		result += "<div class=\"divFooter\">UNCLASSIFIED</div>";
 		result = insertPageBreak(result);
 		result += "<h2>Outline</h2>";
 		for (Section s : documentation.getSections()) {
 			sectionCount++;
 			subsectionCount = 0;
 			s.setId(sectionCount + "");
 			result += "<a class=\"outline_section_reference\" href=\"#"
 					+ s.getId() + "\">" + s.getId() + " " + s.getName()
 					+ "</a></br>\n";
 
 			for (Fragment f : s.getFragments()) {
 				if (f instanceof Subsection) {
 					Subsection subsection = (Subsection) f;
 					subsectionCount++;
 					subsubsectionCount = 0;
 					String id = sectionCount + "." + subsectionCount;
 					subsection.setId(id);
 					result += "<a class=\"outline_subsection_reference\" href=\"#"
 							+ subsection.getId()
 							+ "\">"
 							+ subsection.getId()
 							+ " " + subsection.getName() + "</a></br>\n";
 
 					for (Fragment f2 : subsection.getFragments()) {
 						if (f2 instanceof Subsubsection) {
 							Subsubsection subsubsection = (Subsubsection) f2;
 							subsubsectionCount++;
 							String subsubid = sectionCount + "."
 									+ subsectionCount + "."
 									+ subsubsectionCount;
 							subsubsection.setId(subsubid);
 							result += "<a class=\"outline_subsubsection_reference\" href=\"#"
 									+ subsubsection.getId()
 									+ "\">"
 									+ subsubsection.getId()
 									+ " "
 									+ subsubsection.getName() + "</a></br>\n";
 
 						}
 
 					}
 
 				}
 			}
 		}
 
 		result = insertPageBreak(result);
 		
 		
 		for (Section s : documentation.getSections()) {
 			result += doSwitch(s);
 		}
 		String glossary = "";
 		for (TermEntry entry : documentation.getTerminology()) {
 			result += doSwitch(entry);
 			//result = weaveTerminologyReferences(entry, result);
 			
 		}
 		return result + glossary;
 
 	}
 
 
 
 	private String insertPageBreak(String result) {
 		result += "\n<div class=\"page-break\"></div>\n";
 		return result;
 	}
 
 	@Override
 	public String caseSection(Section section) {
 		String result = insertPageBreak("");
 		
 		result += "<a name=\"" + section.getId()
 				+ "\"><h2 class=\"section\">" + section.getId() + " "
 				+ section.getName() + "</h2></a>\n";
 		for (Fragment f : section.getFragments()) {
 			result += doSwitch(f);
 		}
 		return result;
 
 	}
 
 	@Override
 	public String caseSubsection(Subsection subsection) {
 		String result = "<a name=\"" + subsection.getId()
 				+ "\"><h3 class=\"subsection\">" + subsection.getId() + " "
 				+ subsection.getName() + "</h3></a>\n";
 		for (Fragment f : subsection.getFragments()) {
 			result += doSwitch(f);
 		}
 
 		return result;
 
 	}
 
 	@Override
 	public String caseSubsubsection(Subsubsection subsubsection) {
 		String result = "<a name=\"" + subsubsection.getId()
 				+ "\"><h3 class=\"subsubsection\">" + subsubsection.getId()
 				+ " " + subsubsection.getName() + "</h3></a>\n";
 		for (Fragment f : subsubsection.getFragments()) {
 			result += doSwitch(f);
 		}
 
 		return result;
 	}
 
 	@Override
 	public String caseParagraph(Paragraph paragraph) {
 		String result = "<p>\n";
 		for (Fragment f : paragraph.getFragments()) {
 			result += doSwitch(f);
 		}
 		result += "</p>\n";
 		return result;
 	}
 
 	@Override
 	public String caseLine(Line line) {
 		return line.getText();
 	}
 
 	@Override
 	public String caseTable(Table object) {
 		String result = "<table>";
 		result += doSwitch(object.getTableHeader());
 		for (TableRow rows : object.getTableRows()) {
 			result += doSwitch(rows);
 		}
 		result += "</table>\n";
 		return result;
 	}
 
 	@Override
 	public String caseTableHeader(TableHeader object) {
 		String result = "<tr>";
 		for (String cell : object.getHeaderCells()) {
 			result += "<th>" + cell + "</th>";
 		}
 		result += "</tr>\n";
 		return result;
 	}
 
 	@Override
 	public String caseTableRow(TableRow object) {
 		String result = "<tr>";
 		for (String cell : object.getRowCells()) {
 			result += "<td>" + cell + "</td>";
 		}
 		result += "</tr>\n";
 		return result;
 	}
 
 	@Override
 	public String caseList(List list) {
 		String result = "<ul>\n";
 		for (ListItem item : list.getItems()) {
 			result += doSwitch(item);
 		}
 		result += "</ul>\n";
 		return result;
 	}
 
 	@Override
 	public String caseListItem(ListItem item) {
 		return "<li>" + item.getText() + "</li>\n";
 	}
 
 	@Override
 	public String caseImage(Image image) {
 		String result = "<br/>";
 		if (image.getWidth() != null) {
 			result += "<img class=\"manStyled\" src=\"../"
 					+ image.getOriginalSource() + "\" width=\""
 					+ image.getWidth() + "%\" />";
 		} else {
 			result += "<img src=\"../" + image.getOriginalSource()
 					+ "\" width=\"100%\" />";
 		}
 		result += "<div class=\"figure_description\">Figure " + figureCounter++
 				+ " - " + image.getName() + "</div>";
 		return result;
 	}
 	
 	@Override
 	public String caseXML(XML xml) {
 		StringBuffer result = new StringBuffer();
 		result.append("<br/><br/>");
 		result.append("<div class=\"figure_description\">XML Listing " + xmlCounter ++
 				+ " - " + xml.getName() + "</div>");
 		result.append("<pre>");
 		
 		String content;
 		try {
 			Class<?> clazz = Class.forName(xml.getContextClassName());
 			InputStream inputStream = clazz.getResourceAsStream(xml.getResource());
 			
 			StringWriter writer = new StringWriter();
 			IOUtils.copy(inputStream, writer, "UTF-8");
 			content = StringEscapeUtils.escapeXml(writer.toString());
 			result.append(content);
 			//result.append(writer.toString());
 			
 		} catch (ClassNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		result.append("</pre>");
 		return result.toString();
 	}
 
 
 //	private String weaveTerminologyReferences(TermEntry entry, String result) {
 //		return result.replaceAll(entry.getName(), "<a href=\"#" + entry.getId()
 //				+ "\">" + entry.getName() + "</a>");
 //	}
 	
 	@Override
 	public String caseTermEntry(TermEntry entry) {
 		entry.setId("entry_"+entryCounter++);
 		String result = "<a name=\""+entry.getId()+"\"><strong>" + entry.getName() + "</strong></a>: "
 				+ entry.getDescription() + "</br>";
 		return result;
 	}
 
 	public void saveDocumentationToFile(Documentation documentation)
 			throws Exception {
 		String completeDocumentation = getDocumentationAsString(documentation, DEFAULT_CSS_FILENAME);
 		
 		File file = new File(DOC_PATH + "Documentation.html");
 
 		// if file doesn't exists, then create it
 		if (!file.exists()) {
 			File parentFile = file.getParentFile();
 			if (!parentFile.exists()) {
 				parentFile.mkdirs();
 			}
 			file.createNewFile();
 		}
 		FileOutputStream fop = new FileOutputStream(file);
 
 		// get the content in bytes
 		byte[] contentInBytes = completeDocumentation.getBytes();
 
 		fop.write(contentInBytes);
 		fop.flush();
 		fop.close();
 		System.out.println("Saved documentation to: " + file.getAbsolutePath());
 
 	}
 	
 	public String getDocumentationAsString(Documentation documentation, String cssPath) {
 		StringBuffer completeFile = new StringBuffer();
 		initHTMLHeader(completeFile, cssPath);
 		completeFile.append(doSwitch(documentation));
 		return completeFile.toString();
 	}
 
 	public void saveFragmentToFile(Fragment documentation, String filename)
 			throws Exception {
 		StringBuffer completeFile = new StringBuffer();
 		initHTMLHeader(completeFile, DEFAULT_CSS_FILENAME);
 		completeFile.append(doSwitch(documentation));
 		File file = new File(DOC_FRAGMENT_PATH + filename + ".html");
 
 		// if file doesn't exists, then create it
 		if (!file.exists()) {
 			File parentFile = file.getParentFile();
 			if (!parentFile.exists()) {
 				parentFile.mkdirs();
 			}
 			file.createNewFile();
 		}
 		FileOutputStream fop = new FileOutputStream(file);
 
 		// get the content in bytes
 		byte[] contentInBytes = completeFile.toString().getBytes();
 
 		fop.write(contentInBytes);
 		fop.flush();
 		fop.close();
 		System.out.println("Saved documentation to: " + file.getAbsolutePath());
 
 	}
 
 	private void initHTMLHeader(StringBuffer buffer, String cssPath) {
 		buffer.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n");
 		buffer.append("<html xmlns=\"http://www.w3.org/1999/xhtml\">\n");
 		buffer.append("<head>\n");
 		buffer.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />\n");
 		buffer.append("<link href='http://fonts.googleapis.com/css?family=Titillium+Web:200,400' rel='stylesheet' type='text/css'/>\n");
 		buffer.append("<link rel=\"stylesheet\" href=\"" + cssPath + "\" />\n");
 		buffer.append("</head>\n");
 		buffer.append("<body>\n");
 	}
 
 	public String getDocumentationFragmentContents(String fragmentFilenname)
 			throws IOException {
 		File file = new File(DOC_FRAGMENT_PATH + fragmentFilenname.trim()
 				+ ".html");
 		if (!file.exists()) {
 			return "<div class=\"error\">ERROR: could not find documentation fragment at:</br> " + file.getAbsolutePath() + "</div>";
 		}
 		InputStream stream = new FileInputStream(file);
 		BufferedReader br = new BufferedReader(new InputStreamReader(stream));
 
 		StringBuilder sb = new StringBuilder();
 		String line;
 		while ((line = br.readLine()) != null) {
 			sb.append(line);
 		}
 
 		br.close();
 		return sb.toString();
 	}
 
 }
