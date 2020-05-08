 package proglang.daphne.pointex;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.nio.charset.Charset;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Scanner;
 
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 
 public class PointExtractor {
 
 	private static final int COL_USERNAME = 0;
	private static final int COL_TUTOR = 1;
 //	private static final int COL_POINTS = 2;
 //	private static final int COL_MARK = 3;
 	private static final int COL_LATEST_EX = 4;
 
 	private static org.slf4j.Logger log = org.slf4j.LoggerFactory
 			.getLogger(PointExtractor.class);
 
 	public static List<Student> extract(final InputStream in)
 			throws IOException {
 		String[] tableHeaders = null;
 		String[][] table = null;
 
 		List<Student> students = new ArrayList<>();
 
 		Document doc = Jsoup.parse(in, Charset.defaultCharset().name(), "");
 		Elements tables = doc.select("table");
 		log.debug("Number of tables found: " + tables.size());
 		for (Element element : tables) {
 
 			// Extract header
 			Elements headers = element.select("thead > tr > th");
 			tableHeaders = new String[headers.size()];
 			for (int i = 0; i < headers.size(); i++) {
 				Element header = headers.get(i);
 				tableHeaders[i] = header.text().trim();
 			}
 
 			log.debug("Headers: " + Arrays.toString(tableHeaders));
 
 			// Extract content
 			Elements rows = tables.select("tbody > tr.even, tbody > tr.odd");
 
 			table = new String[rows.size()][tableHeaders.length];
 
 			for (int currRow = 0; currRow < rows.size(); currRow++) {
 				Elements cols = rows.get(currRow).select("td");
 
 				for (int currCol = 0; currCol < cols.size(); currCol++) {
 					Element col = cols.get(currCol);
 					table[currRow][currCol] = col.text();
 				}
 
 				String[] points = Arrays.copyOfRange(table[currRow],
 						COL_LATEST_EX, table[currRow].length);
 				ArrayList<ExPoint> parsedPoints = parsePoints(points);
 
 				Student student = new Student(table[currRow][COL_USERNAME],
 						parseTutor(table[currRow][COL_TUTOR]), parsedPoints);
 				students.add(student);
 			}
 		}
 
 		log.debug("Entries: " + table.length);
 
 		// System.out.println(Arrays.toString(tableHeaders));
 		// for (int i = 0; i < table.length; i++) {
 		// for (int j = 0; j < table[i].length; j++) {
 		// System.out.print(table[i][j] + "|");
 		// }
 		// System.out.println();
 		// }
 
 		return students;
 	}
 
 	private static ArrayList<ExPoint> parsePoints(String[] points) {
 		ArrayList<ExPoint> coll = new ArrayList<>();
 
 		for (String entry : points) {
 			// Decimal conversion
 			entry = entry.replace(",", ".");
 
 			double received = 0;
 			double max = -1;
 
 			try (Scanner sc = new Scanner(entry);) {
 				
 				if (sc.hasNextDouble()) {
 					received = sc.nextDouble();
 				}
 				sc.skip(".*/");
 				max = sc.nextDouble();
 			}
 
 			coll.add(new ExPoint(received, max));
 		}
 
 		return coll;
 	}
 
 	private static String parseTutor(String tutor) {
 		if (tutor.equals("*")) {
 			return null;
 		} else {
 			String[] f = tutor.split(" ");
 			if (f.length == 2) {
 				return f[1];
 			} else {
 				return null; // TODO Or throw exception?!
 			}
 		}
 	}
 }
