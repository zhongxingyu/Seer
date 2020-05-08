 package edu.tufts.cs.gv.view;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.FontMetrics;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Rectangle;
 import java.awt.RenderingHints;
 import java.awt.event.ComponentEvent;
 import java.awt.event.ComponentListener;
 import java.awt.event.MouseEvent;
 import java.awt.geom.AffineTransform;
 import java.awt.geom.Rectangle2D;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Set;
 
 import edu.tufts.cs.gv.controller.VizEventType;
 import edu.tufts.cs.gv.controller.VizState;
 import edu.tufts.cs.gv.model.Dataset;
 import edu.tufts.cs.gv.model.TestCase;
 import edu.tufts.cs.gv.util.DrawingHelp;
 
 //This class will be a bar chart of the witnesses.
 
 public class ResultsView extends VizView {
 	private static final long serialVersionUID = 1L;
 
 	private static final int maxBars = 5; // The maximum number of bars for a
 											// particular test case
 	private static final float barSpacing = 2; // Number of pixels between bars
 												// of a test case
 	private static final int barWidth = 14;
 	private static final float testCaseSpacing = 20; // Spacing between test
 														// cases
 	private static final List<String> helpString = Arrays.asList("This view shows the distribution of witnesses for each test case.\n",
 																 "For each witness, the top " + maxBars + " witnesses are shown along with their\n",
 																 "respective counts. Mouse over various bars to see their witness names.\n",
 																 "The bar height is directly proportional to the frequency of that particular\n",
 																 "witness.");
 
 	private ArrayList<HashMap<String, Integer>> witnesses;
 	private ArrayList<String> testcases;
 	private int maxBarChartHeight;
 	private LinkedHashMap<Rectangle, String> bars;
 
 	public ResultsView() {
 		VizState.getState().addVizUpdateListener(this);
 		maxBarChartHeight = 0;
 		this.setToolTipText("");
 		
 		this.addComponentListener(new ComponentListener() {
 			
 			@Override
 			public void componentShown(ComponentEvent arg0) {	
 			}
 			
 			@Override
 			public void componentResized(ComponentEvent arg0) {
 				bars = null;
 			}
 			
 			@Override
 			public void componentMoved(ComponentEvent arg0) {
 			}
 			
 			@Override
 			public void componentHidden(ComponentEvent arg0) {
 			}
 		});
 	}
 
 	@Override
 	public void vizUpdated(VizEventType eventType) {
 		if (eventType == VizEventType.NEW_DATA_SOURCE) {
 			int screenWidth = 0;
 			Dataset dataset = VizState.getState().getDataset();
 			Set<String> testNames = dataset.getAllTestNames();
 			witnesses = new ArrayList<>(testNames.size());
 			testcases = new ArrayList<>(testNames.size());
 			for (String testName : testNames) {
 				Set<TestCase> testCases = dataset.getTestCasesForTest(testName);
 				testcases.add(testName);
 				HashMap<String, Integer> witnessMap = new HashMap<>();
 				witnesses.add(witnessMap);
 				int numUniqueWitnesses = 0;
 				for (TestCase t : testCases) {
 					if (!t.didPass()) {
 						String witness = t.getWitness();
 						int count = 1;
 						if (witnessMap.containsKey(witness)) {
 							count += witnessMap.get(witness);
 						} else {
 							numUniqueWitnesses++;
 							if (numUniqueWitnesses <= maxBars) {
 								screenWidth += barSpacing + barWidth;
 							}
 						}
 						maxBarChartHeight = Math.max(maxBarChartHeight, count);
 						witnessMap.put(witness, count);
 					}
 				}
 				// TODO: if there are more than 5, limit to 5
 				screenWidth += testCaseSpacing;
 			}
 			this.setPreferredSize(new Dimension(screenWidth, this.getHeight()));
			this.getParent().revalidate();
 			bars = null;
 		}
 	}
 
 	private void updateRectangles(Graphics g) {
 		if (witnesses == null)
 			return;
 		FontMetrics metrics = g.getFontMetrics();
 		bars = new LinkedHashMap<>();
 		int height = this.getHeight();
 		// Find the height that the text will take up
 		int maxTextHeight = 0;
 		for (String testname : testcases) {
 			maxTextHeight = Math.max(maxTextHeight,	getTextHeight(metrics, testname));
 		}
 		float heightFactor = height / (float) maxBarChartHeight;
 		float x = 0;
 		int y = height - 1 - maxTextHeight;
 		for (int i = 0; i < witnesses.size(); i++) {
 			HashMap<String, Integer> testcase = witnesses.get(i);
 			for (String witness : testcase.keySet()) {
 				int count = ((Integer) testcase.get(witness)).intValue();
 				int barHeight = (int) (count * heightFactor);
 				bars.put(new Rectangle((int)x, y - barHeight,
 						barWidth, barHeight), witness);
 				x += barWidth + barSpacing;
 			}
 			x += testCaseSpacing;
 		}
 	}
 
 	private int getTextHeight(FontMetrics metrics, String text) {
 		return (int) (Math.sqrt(3) / 2 * metrics.stringWidth(text));
 	}
 
 	private int getTestcaseWidth(FontMetrics metrics, int testIndex) {
 		int numBars = Math.min(maxBars, witnesses.get(testIndex).size());
 		return (int)(numBars * (barSpacing + barWidth));
 	}
 	
 	private void paintBarGraph(Graphics g) {
 		if (bars == null) {
 			updateRectangles(g);
 		}
 		if (bars == null) {
 			return;
 		}
 		Color[] colors = { Color.BLUE, Color.GREEN, Color.ORANGE };
 		int colorIndex = 0;
 		for (Rectangle bar : bars.keySet()) {
 			g.setColor(colors[colorIndex]);
 			colorIndex = (colorIndex + 1) % colors.length;
 			g.fillRect(bar.x, bar.y, bar.width, bar.height);
 		}
 		int x = 0;
 		Graphics2D g2 = (Graphics2D) g;
 		int maxTextHeight = 0;
 		for(String testname : testcases) {
 			maxTextHeight = Math.max(maxTextHeight, getTextHeight(g.getFontMetrics(), testname));
 		}
 		g.setColor(Color.BLACK);
 		int y = this.getHeight() - maxTextHeight;
 		for (int i = 0; i < testcases.size(); i++) {
 			String text = testcases.get(i);
 			AffineTransform orig = g2.getTransform();
 			AffineTransform rotation = new AffineTransform(orig);
 			rotation.translate(x + getTestcaseWidth(g.getFontMetrics(), i) / 2, y + 5);
 			rotation.rotate(Math.PI / 3);
 			g2.setTransform(rotation);
 			g2.drawString(text, 0, 0);
 			x += getTestcaseWidth(g.getFontMetrics(), i) + testCaseSpacing;
 			g2.setTransform(orig);
 		}
 	}
 	
 	private void paintHelp(Graphics g) {
 		DrawingHelp.renderHelpText(this.getParent(), helpString, g);
 	}
 	
 	public void paint(Graphics g) {
 		Graphics2D g2 = (Graphics2D) g;
 		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
 				RenderingHints.VALUE_ANTIALIAS_ON);
 		paintBarGraph(g);
 		if(VizState.getState().isShowingHelp()) {
 			paintHelp(g);
 		}
 	}
 
 	public String getToolTipText(MouseEvent e) {
 		if (bars != null) {
 			for (Rectangle bar : bars.keySet()) {
 				if (bar.contains(e.getX(), e.getY())) {
 					return bars.get(bar);
 				}
 			}
 			return null;
 		}
 		return null;
 	}
 
 	@Override
 	public void update() {
 		// TODO Auto-generated method stub
 		// System.out.println("Results update");
 	}
 }
