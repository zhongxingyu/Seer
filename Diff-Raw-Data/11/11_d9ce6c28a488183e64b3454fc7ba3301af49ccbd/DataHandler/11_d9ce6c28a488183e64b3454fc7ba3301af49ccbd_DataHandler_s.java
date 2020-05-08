 package game;
 
 /*
  * See levelSyntax.txt for explicit specifications of level syntax.
  */
 
 import java.awt.Color;
 import java.io.File;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.Scanner;
 
 import javax.swing.JOptionPane;
 
 import structures.*;
 
 /**
  * A DataHandler is an object that can read in level-information from text files
  * and create the appropriate in-game constructs. It provides static methods for
  * the handling of settings and control over a <code>String</code> to Color (and
  * vice versa) mapping is implemented.
  * @author Sean Lewis
  */
 public class DataHandler {
 
 	private PrintWriter pw;
 	private boolean errorFound;
 
 	/**
 	 * Reads in all level data from the file and returns an ArrayList of the
 	 * levels.
 	 * @param fileName the file all information is in
 	 * @return ArrayList of constructed levels
 	 * @throws IOException if any input error occurred
 	 */
 	public ArrayList<Level> getLevelData(String fileName) throws IOException {
 		pw = new PrintWriter(new File("logs/datalog.txt"));
 		ArrayList<Level> levels = new ArrayList<Level>();
 		long startTime = System.currentTimeMillis();
 		levels.addAll(readFile(new File(fileName)));
 		pw.println();
 		pw.println("Done reading all data. Took "
 				+ (System.currentTimeMillis() - startTime) + " ms.");
 		pw.close();
 		return levels;
 	}
 	 
 	/**
 	 * Reads in the settings from the basic settings file.
 	 * @return the int array of settings parameters.
 	 */
 	public static int[] getSettings() {
 		return getSettings("settings.txt");
 	}
 	
 	
 	/**
 	 * Reads in the settings from an arbitrary file path.
 	 * @param fileName the file to read settings from
 	 * @return the int array of settings parameters.
 	 */
 	public static int[] getSettings(String fileName) {
 
 		int[] settings = GamePanel.DEFAULT_SETTINGS;
 		try {
 			Scanner infile = new Scanner(new File(fileName));
 			int i = 0;
 			while (i < 5) {
 				settings[i++] = (infile.nextLine().contains("yes")) ? (1) : (0);
 			}
 			String l = infile.nextLine().toLowerCase().replaceAll(" ", "");
 			settings[5] = Integer.parseInt(l.substring(l.indexOf("=") + 1));
 			infile.close();
 		} catch (Exception e) {
 			System.out.println("Failed to read settings. " +
 					           "Using default settings.");
 			settings = GamePanel.DEFAULT_SETTINGS;
 			try {
 				DataHandler.printSettings(settings);
 			} catch (IOException io) {
 				io.printStackTrace();
 			}
 		}
 		return settings;
 	}
 
 	private ArrayList<Level> readFile(File file) throws IOException {
 		long t = System.currentTimeMillis();
 		ArrayList<Level> levels = new ArrayList<Level>();
 		Scanner infile = new Scanner(file);
 
 		Ball b = new Ball();
 		ArrayList<Body> bod = new ArrayList<Body>();
 		ArrayList<WarpPoint> warppts = new ArrayList<WarpPoint>();
 		ArrayList<GoalPost> gs = new ArrayList<GoalPost>();
 		ArrayList<Blockage> bs = new ArrayList<Blockage>();
 		double followF = 0;
 		double g = 0;
 		int currentLineN = 0;
 
 		try {
 			while (infile.hasNext()) {
 				currentLineN++;
 				String line = infile.nextLine().toLowerCase().trim()
 						.replaceAll(" ", "");
 
 				if (line.length() > 2 && !line.substring(0, 2).equals("//")) {
 					String[] data;
 					if (line.substring(0, 4).equals("null")) {
 						levels.add(null);						
 					} else if (line.substring(0, 4).equals("ball")) {
 						data = line.substring(5, line.length() - 1).split(",");
 						int x = Integer.parseInt(data[0]);
 						int y = Integer.parseInt(data[1]);
 						int r;
 						Color c;
 						if (data.length > 2) {
 							r = Integer.parseInt(data[2]);
 							c = readColor(data[3]);
 						} else {
 							r = 3;
 							c = Color.red;
 						}
 						b = new Ball(x, y, r, c);
 					} else if (line.substring(0, 4).equals("refl")) {
 
 						data = line.substring(5, line.length() - 1).split(",");
 						int x = Integer.parseInt(data[0]);
 						int y = Integer.parseInt(data[1]);
 						int r = Integer.parseInt(data[2]);
 						Color c = readColor(data[3]);
 						Body tempBod;
 						if (data.length > 4) {
 							tempBod = new Body(x, y, r, c,
 									Integer.parseInt(data[4]));
 						} else {
 							tempBod = new Body(x, y, r, c);
 						}
 						tempBod.setReflector(true);
 						bod.add(tempBod);
 					}
 					// Read planet data in
 					else if (line.substring(0, 4).equals("body")) {
 
 						data = line.substring(5, line.length() - 1).split(",");
 						int x = Integer.parseInt(data[0]);
 						int y = Integer.parseInt(data[1]);
 						int r = Integer.parseInt(data[2]);
 						Color c = readColor(data[3]);
 						if (data.length > 4) {
 							bod.add(new Body(x, y, r, c, Integer
 									.parseInt(data[4])));
 						} else {
 							bod.add(new Body(x, y, r, c));
 						}
 					}
 					// read moon data in - attach to previous body
 					else if (line.substring(0, 4).equals("moon")) {
 						data = line.substring(5, line.length() - 1).split(",");
 						int a = Integer.parseInt(data[0]);
 						int r = Integer.parseInt(data[2]);
 						// radius of ball and moon are added to the distance
 						// (moon parameter is the distance from center to
 						// center)
 						int d = Integer.parseInt(data[1])
 								+ bod.get((bod.size() - 1)).getRadius() + r;
 						Color c = readColor(data[3]);
 						Moon m = new Moon(a, d, r, c, bod.get(bod.size() - 1));
 						bod.get((bod.size() - 1)).addMoon(m); // attaches moon
 																// to last body
 																// in bod
 					}
 					// read warp data in
 					else if (line.substring(0, 4).equals("warp")) {
 						data = line.substring(5, line.length() - 1).split(",");
 						int x = Integer.parseInt(data[0]);
 						int y = Integer.parseInt(data[1]);
 						warppts.add(new WarpPoint(x, y));
 					}
 					// read goal data in
 					else if (line.substring(0, 4).equals("goal")) {
 						data = line.substring(5, line.length() - 1).split(",");
 						int x = Integer.parseInt(data[0]);
 						int y = Integer.parseInt(data[1]);
 						int r = Integer.parseInt(data[2]);
 						gs.add(new GoalPost(x, y, r));
 					}
 					// read top-left input blockage data in
 					else if (line.substring(0, 4).equals("rec2")) {
 						data = line.substring(5, line.length() - 1).split(",");
 						int leftX = Integer.parseInt(data[0]);
 						int topY = Integer.parseInt(data[1]);
 						int width = Integer.parseInt(data[2]);
 						int height = Integer.parseInt(data[3]);
 						int sX = width / 2;
 						int sY = height / 2;
 						int cX = leftX + sX;
 						int cY = topY + sY;
 						Color c = readColor(data[4]);
 						bs.add(new Blockage(cX, cY, sX, sY, c));
 					}
 					// read default blockage data in
 					else if (line.substring(0, 4).equals("rect")) {
 						data = line.substring(5, line.length() - 1).split(",");
 						int cX = Integer.parseInt(data[0]);
 						int cY = Integer.parseInt(data[1]);
 						int sX = Integer.parseInt(data[2]);
 						int sY = Integer.parseInt(data[3]);
 						Color c = readColor(data[4]);
 						bs.add(new Blockage(cX, cY, sX, sY, c));
 					}
 					// read final level data in
 					else if (line.substring(0, 5).equals("level")) {
 
 						data = line.substring(6, line.length() - 1).split(",");
 						if (data.length == 0) {
 							followF = 0;
 							g = 1;
 						} else if (data.length == 1) {
 							followF = 0;
 							g = Double.parseDouble(data[0]);
 						} else {
 							followF = Double.parseDouble(data[0]);
 							g = Double.parseDouble(data[1]);
 
 						}
 						levels.add(new Level(b, bod, warppts, gs, bs, followF,
 								g));
 
 						b = new Ball();
 						bod = new ArrayList<Body>();
 						warppts = new ArrayList<WarpPoint>();
 						gs = new ArrayList<GoalPost>();
 						bs = new ArrayList<Blockage>();
 					} else {
 						pw.println("Failed to read" + file
 								+ " - Invalid type at line " + currentLineN
 								+ ".");
 						pw.close();
 						JOptionPane.showMessageDialog(null,
 								"GravityGolf is unable to load " + file + ".",
 								"Error", JOptionPane.ERROR_MESSAGE);
 						errorFound = true;
 					}
 				}
 			}
 		} catch (Exception e) {
 			pw.println("Failed to read " + file + " - Invalid entry at line "
 					+ currentLineN + ".");
 			pw.println(e);
 			System.out.println("Failed to read " + file
 					+ " - Invalid entry at line " + currentLineN + ".");
 			System.out.println(e);
 			e.printStackTrace();
 			pw.close();
 			// System.exit(0);
 			if (!errorFound)
 				JOptionPane.showMessageDialog(null,
 						"GravityGolf was unable to load " + file + " at line "
 								+ currentLineN + ".", "Error",
 						JOptionPane.ERROR_MESSAGE);
 			errorFound = true;
 			infile.close();
 			return null;
 		}
 		pw.println("Done reading " + file + ". Took "
 				+ (System.currentTimeMillis() - t) + " ms.");
 		infile.close();
 		return levels;
 
 	}
 
 	/**
 	 * Returns if the last reading produced any error.
 	 * @return true of false
 	 */
 	public boolean wasErrorFound() {
 		return errorFound;
 	}
 
 	/**
 	 * Returns a String representation of this color.
 	 * @param c a Color
 	 * @return a String that contains the three pigment values
 	 */
 	public static String getColorDisplay(Color c) {
 		if (c.equals(Color.red))
 			return "red";
 		if (c.equals(Color.black))
 			return "black";
 		if (c.equals(Color.blue))
 			return "blue";
 		if (c.equals(Color.cyan))
 			return "cyan";
 		if (c.equals(Color.gray))
 			return "gray";
 		if (c.equals(Color.green))
 			return "green";
 		if (c.equals(Color.magenta))
 			return "magenta";
 		if (c.equals(Color.orange))
 			return "orange";
 		if (c.equals(Color.pink))
 			return "pink";
 		if (c.equals(Color.yellow))
 			return "yellow";
 		if (c.equals(new Color(128, 0, 128)))
 			return "purple";
 		if (c.equals(new Color(127, 0, 255)))
 			return "violet";
		return "Color(" + c.getRed() + ", " + c.getGreen() + ", " + c.getBlue()
 				+ ")";
 	}
 
 	/**
 	 * Reads in a Sting representation of a Color and creates the appropriate
 	 * Color object. If the color could not be read (i.e. the input string is
 	 * not a valid color representation) null is returned.
 	 * @param str a parameter
 	 * @return the Color object specified by the input String
 	 */
 	public static Color readColor(String str) {
 		if (str == null || str.length() == 0)
 			return null;	
 		String colorSpec = str.toLowerCase().replaceAll(" ", "");
 		if (colorSpec.equals("red"))
 			return Color.red;
 		if (colorSpec.equals("black"))
 			return Color.black;
 		if (colorSpec.equals("blue"))
 			return Color.blue;
 		if (colorSpec.equals("cyan"))
 			return Color.cyan;
 		if (colorSpec.equals("gray"))
 			return Color.gray;
 		if (colorSpec.equals("green"))
 			return Color.green;
 		if (colorSpec.equals("magenta"))
 			return Color.magenta;
 		if (colorSpec.equals("orange"))
 			return Color.orange;
 		if (colorSpec.equals("pink"))
 			return Color.pink;
 		if (colorSpec.equals("yellow"))
 			return Color.yellow;	
 		if (colorSpec.equals("white"))
 				return Color.white;
 		if (colorSpec.equals("purple"))
 			return new Color(128, 0, 128);
 		if (colorSpec.equals("violet"))
 			return new Color(127, 0, 255);
 		try {		
 			String r = colorSpec.substring(colorSpec.indexOf("(") + 1,
					colorSpec.indexOf(","));
			String g = colorSpec.substring(colorSpec.indexOf(",") + 1,
					colorSpec.lastIndexOf(","));
			String b = colorSpec.substring(colorSpec.lastIndexOf(",") + 1,
 					colorSpec.indexOf(")"));
 			return new Color(Integer.parseInt(r), Integer.parseInt(g),
 					Integer.parseInt(b));
 		} catch(Exception e) {
 			return null;			
 		}
 		
 	}
 
 	/**
 	 * Prints the current game settings to settings.txt.
 	 * @throws IOException if settings.txt cannot be written to
 	 */
 	public static void printSettings(boolean[] settings, int speed)
 			throws IOException {
 		PrintWriter settingsWriter = new PrintWriter("settings.txt");
 
 		settingsWriter.println("effects = "
 				+ (settings[GamePanel.EffectsNum] ? "yes" : "no"));
 		settingsWriter.println("vectors = "
 				+ (settings[GamePanel.VectorsNum] ? "yes" : "no"));
 		settingsWriter.println("resultant = "
 				+ (settings[GamePanel.ResultantNum] ? "yes" : "no"));
 		settingsWriter.println("trail = "
 				+ (settings[GamePanel.TrailNum] ? "yes" : "no"));
 		settingsWriter.println("warpArrows = "
 				+ (settings[GamePanel.WarpArrowsNum] ? "yes" : "no"));
 
 		settingsWriter.println("speed = " + speed);
 		settingsWriter.close();
 	}
 
 	/**
 	 * Prints the current game settings to settings.txt.
 	 * @throws IOException if settings.txt cannot be written to
 	 */
 	public static void printSettings(int[] settings) throws IOException {
 		PrintWriter settingsWriter = new PrintWriter("settings.txt");
 		settingsWriter.println("effects = "
 				+ (settings[GamePanel.EffectsNum] == 1 ? "yes" : "no"));
 		settingsWriter.println("vectors = "
 				+ (settings[GamePanel.VectorsNum] == 1 ? "yes" : "no"));
 		settingsWriter.println("resultant = "
 				+ (settings[GamePanel.ResultantNum] == 1 ? "yes" : "no"));
 		settingsWriter.println("trail = "
 				+ (settings[GamePanel.TrailNum] == 1 ? "yes" : "no"));
 		settingsWriter.println("warpArrows = "
 				+ (settings[GamePanel.WarpArrowsNum] == 1 ? "yes" : "no"));
 
 		settingsWriter.println("speed = " + settings[settings.length - 1]);
 		settingsWriter.close();
 	}
 
 }
