 package org.andrill.conop.analysis;
 
import java.awt.*;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 import java.util.Map;
 
 import javax.imageio.ImageIO;
 
 public class SolutionImage {
 	protected static Color[] COLORS = new Color[] { Color.red, Color.blue, Color.green, Color.magenta, Color.cyan,
 			Color.yellow };
 	protected static int HPAD = 4;
 	protected static int VPAD = 4;
 
 	public static void main(final String[] args) {
 		// parse our solutions
 		Solution[] solutions = new Solution[args.length - 1];
		for (int i = 0; i < (args.length - 1); i++) {
 			File file = new File(args[i]);
 			if (file.isDirectory()) {
 				solutions[i] = new CONOP9Solution(new File(args[i]));
 			} else {
 				solutions[i] = new CONOP4JSolution(new File(args[i]));
 			}
 		}
 
 		SolutionImage image = new SolutionImage();
 		image.generate(new File(args[args.length - 1]), solutions);
 	}
 
 	protected int count = 0;
 	protected int firstWidth = 0;
 	protected Graphics2D graphics;
 	protected int otherWidth = 0;
 	protected int rowHeight = 0;
 
 	protected int col(final int i) {
 		if (i == 0) {
 			return 0;
 		} else {
 			return (firstWidth + HPAD) + ((i - 1) * (otherWidth + HPAD));
 		}
 	}
 
 	protected void drawBar(final int row, final int start, final int end, final int run) {
		System.out.println(row + ": " + start + "-" + end);
 		Stroke oldStroke = graphics.getStroke();
 		Color oldColor = graphics.getColor();
 
 		int height = (rowHeight + VPAD) / count;
 		graphics.setStroke(new BasicStroke(height));
 		graphics.setColor(COLORS[run]);
		graphics.drawLine(col(start + 1) + (height / 2), row(row - 1) + (run * height) + (height / 2), col(end + 2)
				- (height / 2), row(row - 1) + (run * height) + (height / 2));
 
 		graphics.setStroke(oldStroke);
 		graphics.setColor(oldColor);
 	}
 
 	protected void drawString(final String str, final int row, final int col) {
 		int x = col(col);
 		if (col == 0) {
			x += ((firstWidth + HPAD) - graphics.getFontMetrics().stringWidth(str.trim())) - (HPAD / 2);
 		} else {
			x += ((otherWidth + HPAD) - graphics.getFontMetrics().stringWidth(str.trim())) / 2;
 		}
 		int y = row(row) - VPAD;
 		graphics.drawString(str.trim(), x, y);
 	}
 
 	protected Map<String, String> findEvent(final String name, final String type, final List<Map<String, String>> events) {
 		for (Map<String, String> e : events) {
 			if (e.get("name").equals(name) && e.get("type").equals(type)) {
 				return e;
 			}
 		}
 		return null;
 	}
 
 	public void generate(final File file, final Solution... solutions) {
 		count = solutions.length;
 
 		// get our events sorted in solution order
 		List<Map<String, String>> events = solutions[0].getEvents();
 		Collections.sort(events, new Comparator<Map<String, String>>() {
			@Override
 			public int compare(final Map<String, String> o1, final Map<String, String> o2) {
 				return new Integer(o2.get("solution")).compareTo(new Integer(o1.get("solution")));
 			}
 		});
 
 		// find our longest event name
 		String longest = "";
 		for (int i = 0; i < events.size(); i++) {
 			Map<String, String> event = events.get(i);
 			String name = event.get("name");
 			if (name.length() > longest.length()) {
 				longest = name;
 			}
 		}
 
 		// calculate metrics
 		FontMetrics metrics = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB).createGraphics().getFontMetrics();
 		rowHeight = metrics.getHeight();
 		firstWidth = metrics.stringWidth(longest);
 		otherWidth = metrics.stringWidth("0") * (int) Math.ceil(Math.log10(events.size()));
 
 		// calculate image bounds
 		int totalHeight = (events.size() + 1) * (rowHeight + VPAD);
 		int totalWidth = (firstWidth + HPAD) + (events.size() * (otherWidth + HPAD));
 
 		// create our image
 		BufferedImage image = new BufferedImage(totalWidth, totalHeight, BufferedImage.TYPE_INT_RGB);
 		graphics = image.createGraphics();
 		graphics.setBackground(Color.white);
 		graphics.fillRect(0, 0, totalWidth, totalHeight);
 		graphics.setColor(Color.black);
 
 		// draw our labels
 		drawString("Event", 0, 0);
 		for (int i = 1; i <= events.size(); i++) {
 			String name = events.get(i - 1).get("name");
 			String type = events.get(i - 1).get("type");
 			drawString("" + i, 0, i);
 			drawString(name, i, 0);
 			for (int j = 0; j < solutions.length; j++) {
 				Solution solution = solutions[j];
 				Map<String, String> e = findEvent(name, type, solution.getEvents());
 				if (e == null) {
 					System.out.println(name + ":" + type);
 				} else {
					int min = (events.size() - Integer.parseInt(e.get("rankmin"))) + 1;
					int max = (events.size() - Integer.parseInt(e.get("rankmax"))) + 1;
 					drawBar(i, max, min, j);
 				}
 			}
 		}
 
 		// draw our borders
 		for (int i = 0; i < events.size(); i++) {
 			graphics.drawLine(0, row(i), totalWidth, row(i));
 			graphics.drawLine(col(i + 1), 0, col(i + 1), totalHeight);
 		}
 		graphics.drawLine(0, row(events.size()), totalWidth, row(events.size()));
 		graphics.drawRect(0, 0, totalWidth - 1, totalHeight - 1);
 
 		try {
 			ImageIO.write(image, "png", file);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	protected int row(final int i) {
 		return (i + 1) * (rowHeight + VPAD);
 	}
 }
