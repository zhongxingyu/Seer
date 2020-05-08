 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.io.PrintWriter;
 import java.text.SimpleDateFormat;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Properties;
 import java.util.Scanner;
 
 public class Main {
 	// final public static long FOLDER_DATE = 0;
 	/** The name of the CSS file to be included. */
 	final public static String CSS_FILE = "_jandexer.css";
 	/** A list of JavaScript files to be included. */
 	final public static String[] JS_FILES = { "_sorttable.js", "_dateDiff.js" };
 	/** The name of the file that stores the path to the public directory. */
 	final public static String DIR_FILE_NAME = "public_path.txt";
 	/** The name of the package of resources. */
 	final public static String RES_PACKAGE_NAME = "resources";
 	/** The size of the file/folder icons. */
 	final public static String IMG_SIZE = "16";
 	/** The name of the generated file list page. */
 	final public static String PAGE_NAME = "_jandexer.html";
 	/** The relative path of the folder from which resources will be copied. */
 	final public static String RES_IN_PATH = "resources";
 	/** The name of the folder to which resources will be copied. */
 	final public static String RES_OUT_PATH = "_jandexerRes";
 	/** The date format to be used in the generated file list. */
 	final public static SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm");
 	final public static String EXT_FILE = "extensions.txt";
 	/** A map from file extensions to icons. */
 	final protected static Properties extImg = getExtImg();
 
 	public static void main(String[] args) throws IOException {
 		String dir = null;
 		try {
 			if (args.length > 0 && args[0].equals("-changedir")) {
 				// User has relocated their Dropbox location
 				throw new IOException();
 			}
 			final Scanner snFile = new Scanner(new File(DIR_FILE_NAME));
 			if (!snFile.hasNextLine()) {
 				// File is empty even though it should contain the path to the
 				// Dropbox Public folder
 				snFile.close();
 				throw new IOException();
 			}
 			dir = snFile.nextLine();
 			snFile.close();
 			if (!new File(dir).exists()) {
 				// The stored path is no longer valid
 				throw new IOException();
 			}
 		} catch (IOException e) {
 			// First time running or unable to find the Dropbox Public folder
 			System.out.println("Enter the path of the Dropbox *Public* folder:");
 			final Scanner snInput = new Scanner(System.in);
 			do {
 				if (dir != null) {
 					System.out.println("Could not locate the path entered. Try again:");
 				}
 				dir = snInput.nextLine().replaceAll("\"", "");
 			} while (!new File(dir).exists());
 			snInput.close();
 
 			try {
 				final FileWriter fw = new FileWriter(DIR_FILE_NAME);
 				final BufferedWriter bw = new BufferedWriter(fw);
 				bw.write(dir);
 				bw.close();
 			} catch (FileNotFoundException fnfe) {
 				fnfe.printStackTrace();
 			}
 		}
 
 		outputResources(dir, new Scanner(System.in));
 		final long t0 = System.currentTimeMillis();
 		jandex(dir, 0);
		System.out.printf("Finished in %d ms.%n", System.currentTimeMillis() - t0);
 	}
 
 	/**
 	 * Generates a file list at the given directory.
 	 *
 	 * @param dir
 	 *            The directory for the file list
 	 * @param depth
 	 *            The distance from the root of the Dropbox Public folder
 	 * @return A record object to be used for ancestor file lists
 	 * @throws IOException
 	 *             If the file data couldn't be read
 	 */
 	public static Row jandex(String dir, int depth) throws IOException {
 		final String folderTitle = folderTitle(dir, depth);
 
 		final File file = new File(dir, PAGE_NAME);
 		final FileOutputStream fso = new FileOutputStream(file);
 		final OutputStreamWriter osw = new OutputStreamWriter(fso, "UTF-8");
 		final BufferedWriter bw = new BufferedWriter(osw);
 		final PrintWriter pw = new PrintWriter(bw);
 
 		pw.println("<!DOCTYPE HTML>");
 		pw.println("<html>");
 		pw.println("<head>");
 		pw.println("<meta charset=\"UTF-8\" />");
 		pw.println("<title>" + folderTitle + " :: Dropbox :: jandexer</title>");
 		pw.println("<link href=\"http://www.dropbox.com/static/1238803391/images/favicon.ico\" rel=\"shortcut icon\" />");
 		pw.printf("<link rel=\"stylesheet\" type=\"text/css\" href=\"%s%s/%s\" />%n", pathToRoot(depth), RES_OUT_PATH,
 				CSS_FILE);
 		for (final String jsFile : JS_FILES) {
 			pw.printf("<script type=\"text/javascript\" src=\"%s%s/%s\"></script>%n", pathToRoot(depth), RES_OUT_PATH,
 					jsFile);
 		}
 		pw.println("</head>");
 		pw.println("<body>");
 		pw.println("<table class=\"sortable\">");
 		pw.println("<caption>Contents of <strong>" + folderTitle + "</strong></caption>");
 		pw.println("<tr>");
 
 		// Headings
 		final String[] headings = { "", "Name", "Size", "Ext.", "Date" };
 		for (final String heading : headings) {
 			pw.println("<th><a href=\"javascript:void(0)\">" + heading + "</a></th>");
 		}
 		pw.println("</tr>");
 
 		long length = 0; // Cumulative size of the current directory
 		final File folder = new File(dir);
 		final List<Row> rows = new LinkedList<Row>();
 
 		if (depth > 0) {
 			rows.add(new Row("/..", -1024, 0));
 		}
 
 		for (final File f : folder.listFiles()) {
 			// Ignore hidden files
 			if (!f.isHidden()) {
 				if (f.isFile() && !f.getName().equals(PAGE_NAME) && (depth > 0 || !f.getName().equals("Downloads.lnk"))) {
 					// Ignore Jandexer file lists and the "Downloads" shortcut
 					rows.add(new Row(f));
 				} else if (f.isDirectory() && (depth > 0 || !f.getName().equals(RES_OUT_PATH))) {
 					rows.add(jandex(dir + "/" + f.getName(), depth + 1));
 				}
 			}
 		}
 
 		Collections.sort(rows, new Comparator<Row>() {
 			public int compare(Row o1, Row o2) {
 				return o1.name.compareToIgnoreCase(o2.name);
 			}
 		});
 
 		// Print to file
 		for (final Row tr : rows) {
 			pw.println("<tr>");
 			pw.printf("<td><img src=\"%s%s/%s.png\" alt=\"\" width=\"%s\" height=\"%s\" /></td>%n", pathToRoot(depth),
 					RES_OUT_PATH, getIcon(tr), IMG_SIZE, IMG_SIZE);
 			pw.println("<td><a href=\"" + getLink(tr.name) + "\">" + tr.name + "</a></td>");
 
 			if (tr.size.contains("-")) {
 				// "Negative" size; omit details
 				// Occurs with "Up one level"
 				pw.println("<td></td><td></td><td></td>");
 			} else {
 				pw.println("<td>" + tr.size + "</td>");
 				pw.println("<td>" + tr.extension + "</td>");
 				pw.println("<td>" + SDF.format(tr.date) + "</td>");
 			}
 
 			pw.println("</tr>");
 			if (tr.length > 0) {
 				length += tr.length;
 			}
 		}
 
 		pw.println("</table>");
 		pw.printf(
 				"<p>Generated by jandexer on %s<script type=\"text/javascript\">document.write(' (' + dateDiff(%d) + ')');</script>.</p>%n",
 				new Date(), System.currentTimeMillis());
 		pw.printf("<p><a href=\"%s\" title=\"%s\"><img src=\"%s\" alt=\"%s\" /></a></p>%n", "http://www.dropbox.com",
 				"dropbox.com", "http://www.dropbox.com/static/images/main_logo.png", "Dropbox logo");
 		pw.println("</body>");
 		pw.println("</html>");
 		pw.close();
 		return new Row(folderTitle(dir, 1), length, folder.lastModified());
 	}
 
 	/**
 	 * Returns the name of the image associated with the given file/folder.
 	 *
 	 * @param tr
 	 *            the object for the file/folder
 	 * @return the name of the image
 	 */
 	public static String getIcon(Row tr) {
 		if (tr.name.startsWith("/")) {
 			if (tr.length > 0) {
 				// Non-empty folder
 				return "folder";
 			}
 			return "folder_empty";
 		}
 
 		final String value = extImg.getProperty(tr.extension.toLowerCase());
 		return value == null ? "blank" : value;
 	}
 
 	/**
 	 * Returns the relative link to the file/folder with the given name.
 	 *
 	 * @param name
 	 *            the name of the file/folder
 	 * @return a href value
 	 */
 	public static String getLink(String name) {
 		if (name.startsWith("/")) {
 			return name.substring(1) + "/" + PAGE_NAME;
 		}
 		return name;
 	}
 
 	/**
 	 * Copies resources to a subfolder in the given directory.
 	 *
 	 * @param dir
 	 *            the directory to contain the subfolder of resources
 	 * @throws IOException
 	 *             If a folder could not be created or files could not be copied
 	 * @throws NullPointerException
 	 */
 	public static void outputResources(String dir, Scanner sn) throws IOException, NullPointerException {
 		final File outFolder = new File(dir, RES_OUT_PATH);
 		if (!outFolder.exists() && !outFolder.mkdir()) {
 			System.err.println("Failed to create the folder \"" + RES_OUT_PATH + "\".");
 			return;
 		}
 
 		final File inFolder = new File(RES_IN_PATH);
 		if (!inFolder.exists()) {
 			System.err.println("Failed to find \"" + inFolder.getAbsolutePath() + "\".");
 			return;
 		}
 
 		for (final File fin : inFolder.listFiles()) {
 			final File fout = new File(dir + "/" + RES_OUT_PATH, fin.getName());
 
 			final boolean replace;
 			if (fout.exists()) {
 				if (MD5Checksum.getMD5Checksum(new FileInputStream(fin)).equals(
 						MD5Checksum.getMD5Checksum(new FileInputStream(fout)))) {
 					replace = false;
 				} else {
 					String input;
 					do {
 						System.out.print("Overwrite " + fout.getName() + "? (y/n): ");
 					} while (!(input = sn.nextLine()).toLowerCase().trim().matches("[ynYN]"));
 					replace = input.matches("[yY]");
 				}
 			} else {
 				replace = true;
 			}
 
 			if (replace) {
 				copyFile(new FileInputStream(fin), new FileOutputStream(fout));
 			}
 		}
 	}
 
 	/**
 	 * Copies a file in the resources package to the designated path.
 	 *
 	 * @param resource
 	 *            the name of the file to copy
 	 * @param destFolder
 	 *            the destination folder
 	 * @param destFile
 	 *            the destination file name
 	 * @throws IOException
 	 *             If the resource could not be copied
 	 */
 	public static void copyFile(FileInputStream in, FileOutputStream out) throws IOException {
 		final byte[] buffer = new byte[1024];
 		int len;
 		while ((len = in.read(buffer)) > 0) {
 			out.write(buffer, 0, len);
 		}
 		in.close();
 		out.close();
 	}
 
 	/**
 	 * Returns the value of <title> for the given directory.
 	 *
 	 * @param dir
 	 *            the name of the directory
 	 * @param depth
 	 *            the distance from the root
 	 * @return the value of <title> in the generated HTML file list
 	 */
 	public static String folderTitle(String dir, int depth) {
 		if (depth == 0) {
 			return "root";
 		}
 
 		final String[] tokens = dir.split("[/\\\\]+");
 		String output = "";
 		for (int i = tokens.length - depth; i < tokens.length; i++) {
 			output += "/" + tokens[i];
 		}
 		return output;
 	}
 
 	/**
 	 * Returns a map from file extensions to icons.
 	 *
 	 * @return a map from file extensions to icons
 	 */
 	protected static Properties getExtImg() {
 		final Properties ppt = new Properties();
 		try {
 			ppt.load(new FileInputStream(EXT_FILE));
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return ppt;
 	}
 
 	/**
 	 * Returns relative path to return to the root from the given depth.
 	 *
 	 * @param depth
 	 *            the distance from the root
 	 * @return the relative path to the root
 	 */
 	public static String pathToRoot(int depth) {
 		String output = "";
 		for (int i = 0; i < depth; i++) {
 			output += "../";
 		}
 		return output;
 	}
 
 	/**
 	 * Remove all existing Jandexer file lists, and optionally resources.
 	 *
 	 * @param dir
 	 *            the path of the Dropbox Public folder
 	 * @param searchForRes
 	 *            true iff the resources folder is to be removed
 	 */
 	public static void clean(String dir, boolean searchForRes) {
 		final File jandexerPage = new File(dir, PAGE_NAME);
 		if (jandexerPage.exists() && !jandexerPage.delete()) {
 			System.err.printf("Failed to delete \"%s/%s\".%n", dir, PAGE_NAME);
 		}
 
 		for (final File f : new File(dir).listFiles()) {
 			if (f.isDirectory()) {
 				clean(f.getPath(), false);
 			}
 		}
 
 		if (searchForRes) {
 			// Remove resource folder
 			final File res = new File(dir, RES_OUT_PATH);
 			if (res.exists()) {
 				clean(res.getPath(), false);
 			}
 			if (!res.delete()) {
 				System.err.printf("Failed to delete the folder \"%s\".%n", RES_OUT_PATH);
 			}
 		}
 	}
}
