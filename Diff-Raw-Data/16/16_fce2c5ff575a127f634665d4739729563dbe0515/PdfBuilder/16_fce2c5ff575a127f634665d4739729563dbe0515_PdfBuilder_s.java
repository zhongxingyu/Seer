 package model.report.builder;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import com.itextpdf.text.Chunk;
 import com.itextpdf.text.Document;
 import com.itextpdf.text.DocumentException;
 import com.itextpdf.text.Element;
 import com.itextpdf.text.Font;
 import com.itextpdf.text.Paragraph;
 import com.itextpdf.text.pdf.PdfPCell;
 import com.itextpdf.text.pdf.PdfPTable;
 import com.itextpdf.text.pdf.PdfWriter;
 
 public class PdfBuilder implements ReportBuilder {
 
 	private PdfPTable table;
 	private Paragraph list;
 	private List<Element> elements;
 	private String EXTENSION = ".pdf";
 
 	public PdfBuilder() {
 		elements = new ArrayList<Element>();
 	}
 
 	@Override
 	public void addDocumentTitle(String title) {
 		endPreviousElement();
 
 		Paragraph titleParagraph = new Paragraph();
 		titleParagraph.setSpacingAfter(25);
 		titleParagraph.setAlignment(Element.ALIGN_CENTER);
 		titleParagraph.getFont().setSize(19);
 		titleParagraph.getFont().setStyle(Font.BOLD);
 		titleParagraph.add(new Chunk(title));
 		elements.add(titleParagraph);
 	}
 
 	@Override
 	public void addListItem(String content) {
 		if (list == null)
 			throw new IllegalStateException("List must be opened before adding content");
 
 		Paragraph newLine = new Paragraph(" - " + content);
 		list.add(newLine);
 	}
 
 	@Override
 	public void addSectionTitle(String title) {
 		endPreviousElement();
 
 		Paragraph titleParagraph = new Paragraph();
 		titleParagraph.setSpacingAfter(10);
 		titleParagraph.getFont().setSize(15);
 		titleParagraph.add(new Chunk(title));
 		elements.add(titleParagraph);
 	}
 
 	@Override
 	public void addTableRow(List<String> row) {
 		addRow(row, Font.NORMAL, 8);
 	}
 
 	@Override
 	public File print(String filename) throws IOException {
 		filename += EXTENSION;
 		endPreviousElement();
 		Document document = new Document();
 		try {
 			PdfWriter.getInstance(document, new FileOutputStream(filename.toString()));
 		} catch (DocumentException e) {
 			throw new IOException("Unable to write pdf content to file " + filename);
 		}
 		document.open();
 		for (Element element : elements) {
 			try {
 				document.add(element);
 			} catch (DocumentException e) {
 				e.printStackTrace();
 			}
 		}
 		document.close();
 		return new File(filename);
 	}
 
 	@Override
 	public void startList(String title) {
 		endPreviousElement();
 
 		list = new Paragraph();
 		Paragraph firstLine = new Paragraph(title);
 		list.add(firstLine);
 	}
 
 	@Override
 	public void startTable(List<String> headers) {
 		endPreviousElement();
 
 		table = new PdfPTable(headers.size());
 		table.setWidthPercentage(100);
 		table.setSpacingAfter(20);
		addRow(headers, Font.BOLD, 10);
 	}
 
 	private void addRow(List<String> rowValues, int style, float fontSize) {
 		if (table == null)
 			throw new IllegalStateException("Cannot write row before opening table");
 
 		if (rowValues.size() != table.getNumberOfColumns())
 			throw new IllegalArgumentException(
 					"Number of strings in list must be equal to number of columns in table");
 
 		for (String value : rowValues) {
 			PdfPCell cell = new PdfPCell();
 			Chunk content = new Chunk(value);
 			content.getFont().setSize(fontSize);
 			content.getFont().setStyle(style);
 			cell.addElement(content);
 			table.addCell(cell);
 		}
 	}
 
 	private void endList() {
 		if (list != null)
 			elements.add(list);
 		list = null;
 	}
 
 	private void endPreviousElement() {
 		if (table != null)
 			endTable();
 
 		if (list != null)
 			endList();
 
 	}
 
 	private void endTable() {
 		if (table != null)
 			elements.add(table);
 		table = null;
 	}
 }
