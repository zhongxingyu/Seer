 /*
  * Part of Fresco software under GPL licence
  * http://www.gnu.org/licenses/gpl-3.0.txt
  */
 package support.regmarks;
 
 import fresco.CData;
 import java.awt.BorderLayout;
 import java.awt.FlowLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.geom.Point2D;
 import java.awt.image.BufferedImage;
 import java.util.Arrays;
 import java.util.Comparator;
 import java.util.LinkedList;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.JButton;
 import javax.swing.JComponent;
 import javax.swing.JDialog;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.JTextField;
 import support.CSupportWorker;
 import utils.metrics.CAreaSimilarityMetric;
 import utils.metrics.CCrossCorrelationMetric;
 import workers.registration.CPointPairs;
 
 /**
  *
  * @author gimli
  */
 public class CPointPairsOverview extends CSupportWorker<JDialog, Void> {
 
 	private final LinkedList<CAreaSimilarityMetric> metrics = new LinkedList<CAreaSimilarityMetric>();
 	private CPointPairs pairs;
 	private double[][] values;
 	private final String[] columnNames;
 	private Double[][] rows;
 	JDialog dialog;
 	private static final Logger logger = Logger.getLogger(CPointPairsOverview.class.getName());
 
 	public CPointPairsOverview(CPointPairs pairs, BufferedImage imageA, BufferedImage imageB) {
 		this.pairs = pairs;
 		metrics.add(new CCrossCorrelationMetric(imageA, imageB, 25, CAreaSimilarityMetric.Shape.CIRCULAR));
 		columnNames = new String[]{"No.", "cross corelation"};
 
 		values = new double[pairs.size()][metrics.size()];
 	}
 
 	private void countValues() {
 		for(int i=0; i< pairs.size(); i++) {
 			int j=0;
 			for(CAreaSimilarityMetric metric: metrics) {
 				values[i][j] = metric.getDistance(pairs.getOrigin(i), pairs.getProjected(i));
 				j++;
 				setProgress((i*metrics.size()+j)*100/pairs.size()/metrics.size());
 			}
 		}
 	}
 
 	public JTable createGui() {
 		rows = new Double[pairs.size()][metrics.size() + 1];
 
 		for(int i=0; i< pairs.size(); i++) {
 			int j = 0;
 			rows[i][j++] = (double)(i+1);
 			for(int k=0; k< metrics.size(); k++) {
 				rows[i][j++] = values[i][k];
 			}
 		}
 
 		sortRows(1);
 
 		JTable gui = new JTable(rows, columnNames);
 
 		return gui;
 	}
 
 	private void sortRows(int field) {
 		Arrays.sort(rows, new CRowComparator(field));
 	}
 
 	@Override
 	public String getWorkerName() {
 		return "Registration marks metrics overview";
 	}
 
 	@Override
 	protected JDialog doInBackground() throws Exception {
 		dialog = new JDialog(CData.mainFrame, "Registration marks quality.", false);
 
 		countValues();
 		JScrollPane scroll = new JScrollPane(createGui());
 
 		final JComponent[] threshRow = new JComponent[metrics.size()+1];
 
 		JButton confirm = new JButton("remove under");
 		threshRow[0] = confirm;
 		for(int i=1; i< metrics.size()+1; i++) {
 			threshRow[i] = new JTextField("0", 8);
 		}
 
 		confirm.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				double[] thresholds = new double[metrics.size()];
 
 				for(int i=0; i< metrics.size(); i++) {
 					thresholds[i] = Double.valueOf(((JTextField)threshRow[i+1]).getText());
 				}
 
 				removeMarks(thresholds);
 			}
 		});
 
 		JPanel threshold = new JPanel(new FlowLayout());
 		for(JComponent cmp: threshRow) {
 			threshold.add(cmp);
 		}
 		dialog.add(threshold, BorderLayout.NORTH);
 		dialog.add(scroll);
 		dialog.pack();
 
 		return dialog;
 	}
 
 	private void removeMarks(double[] thresholds) {
 		Arrays.sort(rows, new CRowComparator(0));
 
 		boolean[] keep = new boolean[rows.length];
 
 		for(int i = 0; i < rows.length; i++ ) {
 			keep[(int)(double)rows[i][0]-1] = true;
 			for(int j=1; j< thresholds.length+1; j++) {
 				if ((Double)rows[i][j] < thresholds[j-1]) {
 					keep[(int)(double)rows[i][0]-1] = false;
 					break;
 				}
 			}
 		}
 
 		LinkedList<Point2D.Double> copyOfMarks0 = new LinkedList<Point2D.Double>(CData.getImage(CData.showImage[0]).getMarks());
 		LinkedList<Point2D.Double> copyOfMarks2 = new LinkedList<Point2D.Double>(CData.getImage(CData.showImage[2]).getMarks());
 
 		int removed = 0;
 		for(int i=rows.length-1; i >= 0; i--) {
 			if (!keep[i]) {
 				copyOfMarks0.remove(i);
 				copyOfMarks2.remove(i);
 				removed++;
 			}
 		}
 
 		logger.log(Level.INFO, "{0} marks removed.", removed);
 
 		CData.getImage(CData.showImage[0]).setMarks(copyOfMarks0);
 		CData.getImage(CData.showImage[2]).setMarks(copyOfMarks2);
 
 		dialog.setVisible(false);
 	}
 
 	private class CRowComparator implements Comparator<Double[]> {
 
 		int field = 0;
 
 		public CRowComparator(int fieldToComapre) {
 			field = fieldToComapre;
 		}
 
 		@Override
 		public int compare(Double[] o1, Double[] o2) {
 			if (o1[field] > o2[field])
 				return -1;
 			else if ((Double)o1[field] == (Double)o2[field])
 				return 0;
 			else
 				return 1;
 		}
 
 	}
 
 }
