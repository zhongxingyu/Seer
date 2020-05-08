 package edu.tufts.cs.gv.view;
 
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.FontMetrics;
 import java.awt.Graphics;
 import java.awt.Color;
 import java.awt.Rectangle;
 import java.awt.event.MouseEvent;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Set;
 
 import edu.tufts.cs.gv.controller.VizEventType;
 import edu.tufts.cs.gv.controller.VizState;
 import edu.tufts.cs.gv.model.Dataset;
 import edu.tufts.cs.gv.model.TestCase;
 
 //This class will be a bar chart of the witnesses.
 
 public class ResultsView extends VizView {
 	private static final long serialVersionUID = 1L;
 	
 	private static final int maxBars = 5; //The maximum number of bars for a particular test case
 	private static final float barSpacing = 2; //Number of pixels between bars of a test case
 	private static final int barWidth   = 14;
 	private static final float testCaseSpacing = 20; //Spacing between test cases
 	
 	private ArrayList<HashMap<String, Integer>> witnesses;
 	private ArrayList<String> testcases;
 	private int maxBarChartHeight;
 	private HashMap<Rectangle, String> bars;
 
 	public ResultsView() {
 		VizState.getState().addVizUpdateListener(this);
 		maxBarChartHeight = 0;
 		this.setToolTipText("");
 	}
 	
 	@Override
 	public void vizUpdated(VizEventType eventType) {
 		if(eventType == VizEventType.NEW_DATA_SOURCE) {
 			int screenWidth = 0;
 			Dataset dataset = VizState.getState().getDataset();
 			Set<String> testNames = dataset.getAllTestNames();
 			witnesses = new ArrayList<>(testNames.size());
 			testcases = new ArrayList<>(testNames.size());
 			for(String testName : testNames) {
 				Set<TestCase> testCases = dataset.getTestCasesForTest(testName);
 				testcases.add(testName);
 				HashMap<String, Integer> witnessMap = new HashMap<>();
 				witnesses.add(witnessMap);
 				int numUniqueWitnesses = 0;
 				for(TestCase t : testCases) {
 					if(!t.didPass()) {
 						String witness = t.getWitness();
 						int count = 1;
 						if(witnessMap.containsKey(witness)) {
 							count += witnessMap.get(witness);
 						} else {
 							numUniqueWitnesses++;
 							if(numUniqueWitnesses <= maxBars) {
 								screenWidth += barSpacing + barWidth;
 							}
 						}
 						maxBarChartHeight = Math.max(maxBarChartHeight, count);
 						witnessMap.put(witness, count);
 					}
 				}
 				//TODO: if there are more than 5, limit to 5
 				screenWidth += testCaseSpacing;
 			}
 			this.setPreferredSize(new Dimension(screenWidth, this.getHeight()));
 			bars = null;
 		}
 	}
 	
 	private void updateRectangles(Graphics g) {
 		if(witnesses == null)
 			return;
 		FontMetrics metrics = g.getFontMetrics();
 		bars = new HashMap<>();
 		int height = this.getHeight();
 		//Find the height that the text will take up
 		int maxTextHeight = 0;
 		for(String testname : testcases) {
 			maxTextHeight = Math.max(maxTextHeight, getTextHeight(metrics, testname));
 		}
 		float heightFactor = height/(float)maxBarChartHeight;
 		float x = 0;
 		int y = height - 1 - maxTextHeight;
 		for(int i=0; i<witnesses.size(); i++) {
 			HashMap<String, Integer> testcase = witnesses.get(i);
 			int textWidth = getTextWidth(metrics, testcases.get(i));
 			int testcaseWidth = 0;
 			for(String witness : testcase.keySet()) {
 				int count = ((Integer)testcase.get(witness)).intValue();
 				int barHeight = (int)(count * heightFactor);
 				bars.put(new Rectangle((int)x + testcaseWidth, y - barHeight, barWidth, barHeight), witness);
 				testcaseWidth += barWidth + barSpacing;
 			}
 			x += Math.max(testcaseWidth, textWidth) + testCaseSpacing;
 		}
 	}
 	
 	private int getTextHeight(FontMetrics metrics, String text) {
		return metrics.getHeight();
 	}
 	
 	private int getTextWidth(FontMetrics metrics, String text) {
 		return metrics.stringWidth(text);
 	}
 	
 	private int getTestcaseWidth(FontMetrics metrics, int testIndex) {
 		int numBars = Math.min(maxBars, witnesses.get(testIndex).size());
 		return (int) Math.max(numBars * (barSpacing + barWidth), getTextWidth(metrics, testcases.get(testIndex)));
 	}
 
 	public void paint(Graphics g) {
 		if(bars == null) {
 			updateRectangles(g);
 		}
 		if(bars == null) {
 			return;
 		}
 		Color[] colors = {Color.BLUE, Color.GREEN, Color.ORANGE};
 		int colorIndex = 0;
 		for(Rectangle bar : bars.keySet()) {
 			g.setColor(colors[colorIndex]);
 			colorIndex = (colorIndex + 1) % colors.length;
 			g.fillRect(bar.x, bar.y, bar.width, bar.height);
 		}
 		int x = 0;
 		for(int i=0; i<testcases.size(); i++) {
 			String text = testcases.get(i);
			g.drawString(text, x, this.getHeight());
 			x += getTestcaseWidth(g.getFontMetrics(), i) + testCaseSpacing;
 		}
 	}
 	
 	public String getToolTipText(MouseEvent e) {
 		if (bars != null) {
 			for (Rectangle bar : bars.keySet()) {
 				if(bar.contains(e.getX(), e.getY())) {
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
 		//System.out.println("Results update");
 	}
 }
