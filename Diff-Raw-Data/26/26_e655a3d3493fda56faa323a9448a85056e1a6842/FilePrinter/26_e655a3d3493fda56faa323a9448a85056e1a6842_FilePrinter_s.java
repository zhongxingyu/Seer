 package server.operations;
 
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.FontMetrics;
 import java.awt.Graphics2D;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.imageio.ImageIO;
 
 import org.apache.pdfbox.pdmodel.PDDocument;
 import org.apache.pdfbox.pdmodel.PDPage;
 
 import server.entities.Day;
 import server.entities.Lesson;
 import server.entities.Timetable;
 import server.entities.TimetableLesson;
 import server.exceptions.ScheduleCreationException;
 import server.resources.DayResource;
 import server.resources.LessonResource;
 
 import com.itextpdf.text.Document;
 import com.itextpdf.text.DocumentException;
 import com.itextpdf.text.pdf.PdfPTable;
 import com.itextpdf.text.pdf.PdfWriter;
 
 /**
  * Printer class to create the schedule as PDF / PNG file.
  * 
  * @author dennis.markmann
  * @since JDK.1.7.0_25
  * @version 1.0
  */
 
 public class FilePrinter {
 	String path = System.getProperty("user.home") + System.getProperty("file.separator") + "timeTable.pdf";
 	String pngFileName = System.getProperty("user.home") + System.getProperty("file.separator") + "timeTable.png";
 
 	public final File printAsPDF(final Timetable timeTable) throws ScheduleCreationException {
 		final Document document = new Document();
 		try {
 
 			PdfWriter.getInstance(document, new FileOutputStream(this.path));
 			document.open();
 			this.createPdfTable(timeTable, document);
 			document.close();
 
 		} catch (final FileNotFoundException | DocumentException e) {
 			throw new ScheduleCreationException();
 		}
 		return new File(this.path);
 	}
 
 	public final File printAsPng(final Timetable timeTable) throws ScheduleCreationException {
 		try {
 			final int width = 200, height = 200;
 
 			final BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
 
 			final Graphics2D ig2 = image.createGraphics();
 
 			final Font font = new Font("TimesRoman", Font.BOLD, 20);
 			ig2.setFont(font);
 			final String message = this.createText(timeTable);
 			final FontMetrics fontMetrics = ig2.getFontMetrics();
 			final int stringWidth = fontMetrics.stringWidth(message);
 			final int stringHeight = fontMetrics.getAscent();
 			ig2.setPaint(Color.black);
 			ig2.drawString(message, (width - stringWidth) / 2, height / 2 + stringHeight / 4);
 
 			this.path = System.getProperty("user.home") + System.getProperty("file.separator") + "timeTable.png";
 
 			ImageIO.write(image, "PNG", new File(this.path));
 
 		} catch (final IOException e) {
 			throw new ScheduleCreationException();
 		}
 		return new File(this.path);
 
 	}
 
 	private String createText(final Timetable timeTable) {
 
 		final StringBuilder sb = new StringBuilder();
 
 		for (final TimetableLesson lesson : timeTable.getLessons()) {
 			sb.append(lesson.getSubject().getShortName());
 			sb.append("\n");
 			sb.append(lesson.getTeacher().getShortName());
 			sb.append("\n");
 			sb.append(lesson.getRoom().getDescription());
 			sb.append("\n");
 			sb.append("\n");
 			sb.append("\n");
 
 		}
 		return sb.toString();
 	}
 
 	private void createPdfTable(final Timetable timeTable, final Document document) throws DocumentException {
 
 		final PdfPTable table = new PdfPTable(6);
		table.addCell("Klasse XXX");
		table.addCell("Montag");
		table.addCell("Dienstag");
		table.addCell("Mittwoch");
		table.addCell("Donnerstag");
		table.addCell("Freitag");
 
 		table.setHeaderRows(1);
 		final Map<Integer, Map<Integer, String>> timeDayHashMap = new HashMap<Integer, Map<Integer, String>>();
 		String display = null;
 		// TODO --> Get times and Days not from Database but from a static
 		// Server Class
 		final List<Lesson> times = new LessonResource().getLessons();
 		final List<Day> days = new DayResource().getDays();
 		for (int x = 0; x < times.size(); x++) {
 			timeDayHashMap.put(times.get(x).getId(), new HashMap<Integer, String>());
 		}
 		for (final TimetableLesson lesson : timeTable.getLessons()) {
 			display = lesson.getSubject().getShortName() + "\n" + lesson.getTeacher().getShortName() + "\n" + lesson.getRoom().getDescription();
 			timeDayHashMap.get(lesson.getLesson().getId()).put(lesson.getDay().getId(), display);
 		}
 
 		for (int i = 0; i < times.size(); i++) {
 			table.addCell(times.get(i).getTimeFrom().toString().substring(0, 5) + "\n - \n" + times.get(i).getTimeTo().toString().substring(0, 5));
 			for (int j = 1; j < days.size() + 1; j++) {
 				if (timeDayHashMap.get(i + 1) == null) {
 					table.addCell("");
 				} else {
 					final String value = timeDayHashMap.get(i + 1).get(j);
 					if (value != null) {
 						table.addCell(value);
 					} else {
 						table.addCell("");
 					}
 				}
 			}
 		}
 		document.add(table);
 	}
 
 	public void createPng() {
 		final int i = 0;
 		PDDocument doc = null;
 		try {
 			doc = PDDocument.load(this.path);
 		} catch (final IOException e) {
 			e.printStackTrace();
 		}
 		@SuppressWarnings("unchecked")
 		final List<PDPage> pages = doc.getDocumentCatalog().getAllPages();
 		for (final PDPage page : pages) {
 			BufferedImage img = null;
 			try {
				img = page.convertToImage(BufferedImage.TYPE_INT_ARGB, 72);
 
 				ImageIO.write(img, "PNG", new File(this.pngFileName));
 			} catch (final IOException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 }
