 package de.mb.infinity;
 
 import javax.swing.JButton;
 import javax.swing.JProgressBar;
 
 import de.mb.swing.ClosableTabbedPane;
 import de.mb.swing.JAdvancedTextArea;
 
 public class IterateCalculator implements Runnable {
 
 	protected JProgressBar progress;
 	protected byte max_ew_a, max_ew_v, max_dices;
 	protected StringBuffer outfile_path, outfile_name;
 	protected boolean with_matrix;
 	protected JAdvancedTextArea result;
 	protected ClosableTabbedPane tab;
 	protected JButton invocator;
 
 	public IterateCalculator(JProgressBar progress, byte max_ew_a,
 			byte max_ew_v, byte max_dices, StringBuffer outfile_path,
 			StringBuffer outfile_name, boolean with_matrix,
 			JAdvancedTextArea result, ClosableTabbedPane tab, JButton invocator) {
 
 		this.progress = progress;
 		this.max_ew_a = max_ew_a;
 		this.max_ew_v = max_ew_v;
 		this.max_dices = max_dices;
 		this.outfile_path = outfile_path;
 		this.outfile_name = outfile_name;
 		this.with_matrix = with_matrix;
 		this.result = result;
 		this.tab = tab;
 		this.invocator = invocator;
 
 	}
 
 	public void run() {
 		progress.setMaximum(max_ew_a * max_ew_v * max_dices);
 		for (byte ew_a = 1; ew_a <= max_ew_a; ew_a++) {
 			for (byte ew_v = 1; ew_v <= max_ew_v; ew_v++) {
 				for (byte dices = 1; dices <= max_dices; dices++) {
 					progress.setValue(progress.getValue() + 1);
 					String outfile = outfile_path.toString() + ew_a + "_"
 							+ ew_v + "_" + dices + "_"
 							+ outfile_name.toString();
 					InfinityProcessor main = new InfinityProcessor(ew_a, ew_v,
 							dices, with_matrix, outfile);
 
 					double[] result_counter = main.process();
 
 					result.setText(result.getText() + "\nAngreifer\t" + ew_a
 							+ "\t" + result_counter[0] + "% (" + dices
 							+ " Würfel)\nVerteidiger\t" + ew_v + "\t"
 							+ result_counter[1] + "%\n");
 
 					if (with_matrix) {
 
 						result.setText(result.getText()
 								+ "\nDas Ergebnis der Analyse wurde nach "
 								+ outfile + " geschrieben.\n");
 					}
 				}
 			}
 		}
 
 		progress.setValue(0);
		tab.add(result.getScrollPane(), ">" + max_ew_a + "(>" + max_dices + ")/>" + max_ew_v);
 		invocator.setEnabled(true);
 	}
 
 }
