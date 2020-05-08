 package de.devboost.natspec.library.documentation;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.apache.commons.lang.StringUtils;
 import org.eclipse.emf.ecore.EObject;
 
 import de.devboost.natspec.annotations.Many;
 import de.devboost.natspec.annotations.TextSyntax;
 
 public class DocumentationSupport {
 
 	private boolean withinTable;
 	private Documentation documentation;
 	private DocumentationFactory factory;
 
 	public DocumentationSupport(Class<?> testClass) {
 		super();
 		this.factory = DocumentationFactory.eINSTANCE;
 	}
 
 	public Documentation getDocumentation() {
 		return this.documentation;
 	}
 
 	public String flattenList(List<String> name) {
 		String result = "";
 		for (String string : name) {
 			result += string + " ";
 		}
 		return result;
 	}
 
 	@TextSyntax("Documentation - #1")
 	public Documentation initDocumentation(List<String> name) {
 		documentation = factory.createDocumentation();
 		documentation.setTitle(flattenList(name));
 		return documentation;
 	}
 
 	@TextSyntax("Reference to #1 with caption #2")
 	public Reference addReference(String label, List<String> caption,
 			TextContainer container, Documentation documentation) {
 		
 		Reference reference = factory.createReference();
 		reference.setName(flattenList(caption));
 		reference.setReferredLabel(label);
 
 		container.getTexts().add(reference);
 		return reference;
 	}
 
 	@TextSyntax("Section - #1")
 	public Section addSection(List<String> name, Documentation d) {
 		Section section = factory.createSection();
 		section.setName(flattenList(name));
 		d.getSections().add(section);
 		return section;
 	}
 
 	@TextSyntax("Section (#1) - #2")
 	public Section addSection(String label, List<String> name, Documentation d) {
 		Section section = addSection(name, d);
 		section.setLabel(label);
 		return section;
 	}
 
 	@TextSyntax("Subsection - #1")
 	public Subsection addSubsection(List<String> name, Section section) {
 		Subsection subsection = factory.createSubsection();
 		String subsectionName = flattenList(name);
 		subsection.setName(subsectionName);
 		section.getFragments().add(subsection);
 		return subsection;
 	}
 
 	@TextSyntax("Subsection (#1) - #2")
 	public Subsection addLabeledSubsection(String label, List<String> name,
 			Section section) {
 		Subsection result = addSubsection(name, section);
 		result.setLabel(label);
 		return result;
 	}
 
 	@TextSyntax("Subsubsection - #1")
 	public Subsubsection addSubsubsection(List<String> name, Subsection s) {
 		Subsubsection subsubsection = factory.createSubsubsection();
 		String subsubsectionName = flattenList(name);
 		subsubsection.setName(subsubsectionName);
 		s.getFragments().add(subsubsection);
 		return subsubsection;
 
 	}
 
 	@TextSyntax("Subsubsection (#1) - #2")
 	public Subsubsection addLabeledSubsubsection(String label, List<String> name, Subsection s) {
 		Subsubsection subsubsection = addSubsubsection(name, s);
 		subsubsection.setLabel(label);
 		return subsubsection;
 	}
 
 	@TextSyntax("Insert page break")
 	public void addPageBreak(FragmentContainer container) {
 		PageBreak fragment = factory.createPageBreak();
 		container.getFragments().add(fragment);
 	}
 
 	@TextSyntax("#1")
 	public Line createPlainContents(List<String> fullSentence,
 			TextContainer container) {
 		String text = flattenList(fullSentence);
 		if (text.startsWith("##")) {
 			text = "//" + text.substring(2);
 		}
 		return addLine(container, text);
 	}
 
 	private Line addLine(TextContainer container, String text) {
 		Line line = factory.createLine();
 		line.setText(text);
 		if (container instanceof FragmentContainer) {
 			FragmentContainer fragmentContainer = (FragmentContainer) container;
 			fragmentContainer.getFragments().add(line);
 		} else {
 			container.getTexts().add(line);
 		}
 		return line;
 	}
 
 	@TextSyntax("|---- #1 ----|")
 	public Table createOrEndTable(List<String> tableDescription,
 			FragmentContainer container) {
 		if (!withinTable) {
 			withinTable = true;
 			Table table = factory.createTable();
 			container.getFragments().add(table);
 			return table;
 		} else {
 			withinTable = false;
 			return null;
 		}
 	}
 
 	@TextSyntax("|- #1 -|")
 	public void createTableHeader(List<String> headerContents, Table table) {
 		List<String> removeSeparators = removeSeparators(headerContents, "-|-");
 		TableHeader tableHeader = factory.createTableHeader();
 		table.setTableHeader(tableHeader);
 		for (String cell : removeSeparators) {
 			tableHeader.getHeaderCells().add(cell);
 		}
 	}
 
 	@TextSyntax("| #1 |")
 	public void createTableRow(List<String> rowContents, Table table) {
 		List<String> removeSeparators = removeSeparators(rowContents, "|");
 		TableRow tableRow = factory.createTableRow();
 		table.getTableRows().add(tableRow);
 		for (String cell : removeSeparators) {
 			tableRow.getRowCells().add(cell);
 		}
 	}
 
 	@TextSyntax("#todo #1")
 	public HtmlCode createTodo(List<String> fullSentence,
 			TextContainer container) {
 		
 		String text = flattenList(fullSentence);
 		return DocumentationElementFactory.INSTANCE.createTodo(text, container);
 	}
 
 	private List<String> removeSeparators(List<String> rowContents,
 			String separator) {
 		List<String> filtered = new LinkedList<String>();
 		String compound = "";
 		for (String content : rowContents) {
 			if (content.equals(separator)) {
 				filtered.add(compound.trim());
 				compound = "";
 				continue;
 			}
 			compound += " " + content;
 		}
 		if (!compound.isEmpty()) {
 			filtered.add(compound);
 		}
 		return filtered;
 	}
 
 	@TextSyntax("Paragraph #1")
 	public Paragraph createParagraphWithHeading(List<String> heading,
 			FragmentContainer container) {
 
 		if (container instanceof Listing) {
 			Listing listing = (Listing) container;
 			EObject parent = listing.eContainer();
 			if (parent instanceof FragmentContainer) {
 				container = (FragmentContainer) parent;
 			}
 		}
 
 		Paragraph paragraph = factory.createParagraph();
 		container = locateProperContainer(container);
 		container.getFragments().add(paragraph);
 		if (!heading.isEmpty()) {
 			HtmlCode headingLine = factory.createHtmlCode();
 			headingLine.setText("<strong>" + StringUtils.join(heading, " ")
 					+ " </strong>");
 			paragraph.getTexts().add(headingLine);
 		}
 		return paragraph;
 	}
 
 	// TODO This can be removed if the metamodel structure is correct
 	private FragmentContainer locateProperContainer(
 			FragmentContainer container) {
 		while (container instanceof ListItem) {
 			EObject parentContainer = container.eContainer();
 			while (parentContainer != null
 					&& !(parentContainer instanceof FragmentContainer)) {
 				parentContainer = parentContainer.eContainer();
 			}
 			if (parentContainer instanceof FragmentContainer) {
 				container = (FragmentContainer) parentContainer;
 			} else {
 				break;
 			}
 		}
 		return container;
 	}
 
 	@TextSyntax("List")
 	public de.devboost.natspec.library.documentation.List addList(
 			FragmentContainer container) {
 		de.devboost.natspec.library.documentation.List list = factory
 				.createList();
 		container.getFragments().add(list);
 		return list;
 	}
 
 	@TextSyntax("* #1")
 	public ListItem addListItem(List<String> item,
 			de.devboost.natspec.library.documentation.List list) {
 		ListItem listItem = factory.createListItem();
 		list.getItems().add(listItem);
 		listItem.setText(flattenList(item));
 		return listItem;
 	}
 
 	@TextSyntax("\\* #1")
 	public ListItem continueListItem(List<String> item, ListItem listItem) {
 		listItem.setText(listItem.getText() + flattenList(item));
 		return listItem;
 	}
 
 	@TextSyntax("Image of #1 at #2")
 	public Image image(List<String> name, String externalPath,
 			FragmentContainer container) {
 		Image image = factory.createImage();
 		container.getFragments().add(image);
 		image.setName(StringUtils.join(name, " "));
 		image.setOriginalSource(externalPath);
 		return image;
 	}
 
 	@TextSyntax("Image of #1 at #2 width #3 #4")
 	public Image image(List<String> name, String externalPath,
 			String widthPercent, String stringUnit,
 			FragmentContainer container) {
 		Image image = image(name, externalPath, container);
 		try {
 			Width width = factory.createWidth();
 			width.setWidth(Integer.parseInt(widthPercent));
 			Unit unit = Unit.get(stringUnit);
 			width.setUnit(unit);
 			image.setWidth(width);
 		} catch (NumberFormatException e) {
 		}
 		return image;
 	}
 
 	private String insertCamelCaseWhitespaces(String ccs) {
 		String result = "";
 		String[] individualWords = ccs
 				.split("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])");
 
 		for (String word : individualWords) {
 			result += " " + word;
 		}
 		return result;
 	}
 
 	@TextSyntax("Story - #1")
 	public void addStory(String path, FragmentContainer c) throws Exception {
 		addNatSpecFile(path, c, "Story", true);
 	}
 
 	private void addNatSpecFile(String path, FragmentContainer c,
 			String contentKind, boolean showLineNumbers)
 			throws FileNotFoundException, IOException {
 		File f = new File(path);
 		if (f.exists()) {
 			HtmlCode code = factory.createHtmlCode();
 			c.getFragments().add(code);
 			String nameWithoutExtension = f.getName().substring(0,
 					f.getName().lastIndexOf('.'));
 			code.setText("<h3 class =\"scenario\">" + contentKind + ": "
 					+ insertCamelCaseWhitespaces(nameWithoutExtension)
 					+ "</h3>");
 			FileInputStream inputStream = new FileInputStream(f);
 			BufferedReader br = new BufferedReader(new InputStreamReader(
 					inputStream));
 
 			String line;
 			int lineNumber = 1;
 			// TODO handle comments as plain documentation
 			List<String> codeFragments = new LinkedList<String>();
 			String codeFragment = "";
 			while ((line = br.readLine()) != null) {
 				if (isComment(line)) {
 					if (!codeFragment.isEmpty()) {
 						codeFragments.add(codeFragment);
 						codeFragment = "";
 					}
 					codeFragments.add(line);
 				} else {
 
 					if (showLineNumbers) {
 						codeFragment += "<span class=\"linenumber\">"
 								+ lineNumber
 								+ "</span><span class=\"codeline\">" + line
 								+ "&nbsp;</span>\n";
 						lineNumber++;
 					} else {
 						codeFragment += "<span class=\"codeline\">" + line
 								+ "&nbsp;</span>\n";
 
 					}
 				}
 			}
 
 			br.close();
 
 			if (!codeFragment.isEmpty()) {
 				codeFragments.add(codeFragment);
 			}
 			for (String fragment : codeFragments) {
 				HtmlCode contents = factory.createHtmlCode();
 				c.getFragments().add(contents);
 				if (isComment(fragment)) {
 					contents.setText(fragment.substring(2));
 				} else {
 					contents.setText("<div class=\"code\"><code class=\"natspec_code\">\n"
 							+ fragment + "\n</code></div>\n");
 				}
 			}
 
 		} else {
 			System.out.println("Can't find " + contentKind + " at: "
 					+ f.getAbsolutePath());
 		}
 	}
 
 	private boolean isComment(String line) {
 		return line.trim().startsWith("//");
 	}
 
 	@TextSyntax("Rules - #1")
 	public void addRules(String path, FragmentContainer c) throws Exception {
 		addNatSpecFile(path, c, "Rules", false);
 	}
 
 	@TextSyntax("Define #1 : #2")
 	public void addTerminoligyEntry(List<String> entryName,
 			List<String> entryDescription, Documentation documentation) {
 		TermEntry termEntry = factory.createTermEntry();
 		termEntry.setName(StringUtils.join(entryName, " "));
 		termEntry.setDescription(StringUtils.join(entryDescription, " "));
 		documentation.getTerminology().add(termEntry);
 	}
 
 	@TextSyntax("Author #1")
 	public void addAuthor(List<String> authors, FragmentContainer container) {
 		HtmlCode html = factory.createHtmlCode();
 		html.setText("<div class=\"author_tag\">" + StringUtils.join(authors, " ")
 				+ "</div>");
 		container.getFragments().add(html);
 	}
 
 	@TextSyntax("XML of #1 from resource #2 at #3")
 	public void codeFromFile(List<String> nameParts, String path, String className,
 			FragmentContainer container) {
 
 		String name = StringUtils.join(nameParts, " ");
 		DocumentationElementFactory.INSTANCE.createXML(container, path,
 				className, name);
 	}
 
 	@TextSyntax("Listing")
 	public Listing beginListing(FragmentContainer container) {
 		Listing createListing = factory.createListing();
 		container.getFragments().add(createListing);
 		return createListing;
 	}
 
 	@TextSyntax("Code #1")
 	public Code code(@Many String text, TextContainer container) {
 		Code code = factory.createCode();
 		code.setText(text);
 		if (container instanceof FragmentContainer) {
 			FragmentContainer fragmentContainer = (FragmentContainer) container;
 			fragmentContainer.getFragments().add(code);
 		} else {
 			container.getTexts().add(code);
 		}
 		return code;
 	}
 
 	@TextSyntax("Link to #1")
 	public Link link(String uri, FragmentContainer container) {
 		Link link = factory.createLink();
 		container.getFragments().add(link);
 		link.setName(uri);
 		link.setUri(uri);
 		return link;
 	}
 
 	@TextSyntax("Link to #2 with caption #1")
 	public Link link(List<String> caption, String uri,
 			FragmentContainer container) {
 		Link link = link(uri, container);
 		link.setName(StringUtils.join(caption, " "));
 		return link;
 	}
 }
