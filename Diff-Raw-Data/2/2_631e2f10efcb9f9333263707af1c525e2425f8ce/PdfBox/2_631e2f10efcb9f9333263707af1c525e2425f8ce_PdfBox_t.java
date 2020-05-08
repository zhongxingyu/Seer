 package util;
 
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.math.BigInteger;
 import java.security.SecureRandom;
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.imageio.ImageIO;
 
 import org.apache.pdfbox.exceptions.COSVisitorException;
 import org.apache.pdfbox.pdmodel.PDDocument;
 import org.apache.pdfbox.pdmodel.PDPage;
 import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;
 import org.apache.pdfbox.pdmodel.font.PDType1Font;
 import org.apache.pdfbox.pdmodel.graphics.xobject.PDJpeg;
 import org.apache.pdfbox.pdmodel.graphics.xobject.PDXObjectImage;
 
 import ctrl.DBField;
 import ctrl.DBSubject;
 import data.Field;
 import data.Subject;
 
 public class PdfBox {
 
 	public static int headX = 50;
 	public static int headY = 660;
 	public static PDXObjectImage ximage;
 
 	public static void main(String[] args) {
 		processSubjects(DBSubject.loadSubjects());
 	}
 
 	/**
 	 * Function that takes a subjectlist that prints all files to PDF into the
 	 * ./pdf directory.
 	 * 
 	 * @param list
 	 */
 	private static void processSubjects(List<Subject> list) {
 		// LOAD FIELDS FROM DB
 		List<Field> fields;
 		for (Subject s : list) {
 			fields = DBField.loadFieldList(s.getModTitle(), s.getVersion(),
 					s.getSubTitle());
 			System.out.println(fields.toString());
 
 			List<String> strings = new LinkedList<String>();
 
 			strings.add("Fach: " + s.getSubTitle());
 			strings.add("Aus: " + s.getModTitle());
 			strings.add("Version: " + s.getVersion());
 			strings.add("Beschreibung: " + s.getDescription());
 
 			try {
 				printPDF(strings, fields);
 
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 
 	}
 
 	/**
 	 * Print the processed List of Strings - it contains all variables in
 	 * strings, then is overwritten
 	 * 
 	 * @param strings
 	 * @throws IOException
 	 */
 	private static void printPDF(List<String> strings, List<Field> fields)
 			throws IOException {
 		File logo = new File("D:/logo_50_sw.jpg");
 		PDDocument doc = null;
 		PDPage page = null;
 
 		BufferedImage buff = ImageIO.read(logo);
 		try {
 			doc = new PDDocument();
 			page = new PDPage();
 
 			doc.addPage(page);
 			PDXObjectImage ximage = new PDJpeg(doc, buff, 1f);
 			PDPageContentStream content = new PDPageContentStream(doc, page);
 
 			content.drawXObject(ximage, 180, 700, 411, 80);
 			System.out.println("height/width: "
 					+ page.getMediaBox().getHeight() + " / "
 					+ page.getMediaBox().getWidth());
			printLine(content, "Universität Ulm - MMS: ", headX, headY, 16);
 
 			content.drawLine(headX, headY - 5,
 					page.getMediaBox().getWidth() - 50, headY - 5);
 			printLine(content, strings.get(0), headX + 150, headY - 20, 14);
 			content.drawLine(headX, headY - 25,
 					page.getMediaBox().getWidth() - 50, headY - 25);
 			// draw Subject
 			printMultipleLines(content, strings, headX + 10, headY - 40, 12);
 			// draw Fields of Subject
 			content.drawLine(headX, headY - 95,
 					page.getMediaBox().getWidth() - 50, headY - 95);
 			printLine(content, "Weitere Informationen:", headX + 150,
 					headY - 110, 14);
 			content.drawLine(headX, headY - 115,
 					page.getMediaBox().getWidth() - 50, headY - 115);
 
 			printFields(content, page, doc, fields, false);
 
 			content.close();
 			String name = strings.get(0).split(": ")[1];
 			String version = strings.get(2).split(": ")[1];
 
 			doc.save("pdf/" + name + "_" + version + ".pdf");
 			doc.close();
 		} catch (IOException | COSVisitorException e) {
 			System.out.println(e);
 		}
 	}
 
 	/**
 	 * Print the fields of the specific subjects - needs the content until now,
 	 * the page and document of course it needs all the fields in a list and has
 	 * a boolean to either print or not print the university logo at the bottom
 	 * of the document.
 	 * 
 	 * @param content
 	 * @param page
 	 * @param doc
 	 * @param fields
 	 * @param printLogo
 	 * @throws IOException
 	 */
 	private static void printFields(PDPageContentStream content, PDPage page,
 			PDDocument doc, List<Field> fields, boolean printLogo)
 			throws IOException {
 		LinkedList<String> strings = new LinkedList<String>();
 		int offset = 0;
 		int tmpsize = 0;
 		int bottomborder = 60;
 		int tmpY = headY - 80 + offset;
 		for (Field f : fields) {
 			tmpY -= 55;
 			System.out.println(f.getFieldTitle());
 			if (tmpY < bottomborder) {
 				if (printLogo)
 					bottomborder = 115;
 				tmpY = headY + 100;
 				content.close();
 				page = new PDPage();
 				doc.addPage(page);
 				content = new PDPageContentStream(doc, page);
 				// optional - draw university logo on page 2++
 				if (printLogo)
 					content.drawXObject(ximage, 180, -20, 411, 80);
 			}
 
 			strings = new LinkedList<String>();
 			for (String s : f.getDescription().split("\r\n")) {
 				strings.add(s);
 			}
 
 			printLine(content, f.getFieldTitle() + ": ", headX + 20, tmpY, 12);
 			tmpsize = printMultipleLines(content, strings, headX + 50,
 					tmpY - 14, 12) * 12;
 			tmpY = tmpY - tmpsize + 20;
 			System.out.println("location y: " + tmpY);
 		}
 		content.close();
 	}
 
 	/**
 	 * prints a message to the specified content, at location (x,y) with
 	 * specified font size.
 	 * 
 	 * @param content
 	 * @param message
 	 * @param x
 	 * @param y
 	 * @param size
 	 * @throws IOException
 	 */
 	private static void printLine(PDPageContentStream content, String message,
 			int x, int y, float size) throws IOException {
 		content.beginText();
 		content.setFont(PDType1Font.HELVETICA, size);
 		content.moveTextPositionByAmount(x, y);
 		content.drawString(message);
 
 		content.endText();
 	}
 
 	/**
 	 * prints multiple lines to the specified content, starts at location (x,y)
 	 * with specified fontsize.
 	 * 
 	 * @param contentStream
 	 * @param lines
 	 * @param x
 	 * @param y
 	 * @param size
 	 * @return
 	 * @throws IOException
 	 */
 	private static int printMultipleLines(PDPageContentStream contentStream,
 			List<String> lines, float x, float y, float size)
 			throws IOException {
 		if (lines.size() == 0) {
 			return 0;
 		}
 		final int numberOfLines = lines.size();
 		final float fontHeight = 12;
 		contentStream.beginText();
 		contentStream.setFont(PDType1Font.HELVETICA, size);
 
 		contentStream.appendRawCommands(fontHeight + " TL\n");
 		contentStream.moveTextPositionByAmount(x, y);
 		for (int i = 0; i < numberOfLines; i++) {
 			contentStream.drawString(lines.get(i));
 			if (i < numberOfLines - 1) {
 				contentStream.appendRawCommands("T*\n");
 			}
 		}
 		contentStream.endText();
 		System.out.println("no of lines: " + numberOfLines);
 		return numberOfLines;
 	}
 }
