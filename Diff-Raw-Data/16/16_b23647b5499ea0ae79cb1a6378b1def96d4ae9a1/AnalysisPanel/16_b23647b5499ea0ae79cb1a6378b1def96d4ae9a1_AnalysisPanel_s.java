 package org.andrill.conop.ui;
 
 import java.awt.event.ActionEvent;
 import java.io.File;
 import java.util.List;
 
 import javax.swing.AbstractAction;
 import javax.swing.DefaultListModel;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JFileChooser;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 
 import net.miginfocom.swing.MigLayout;
 
 import org.andrill.conop.analysis.AgeAndPlacements;
 import org.andrill.conop.analysis.CONOP4JSolution;
 import org.andrill.conop.analysis.CONOP9Solution;
 import org.andrill.conop.analysis.CompareSolutions;
 import org.andrill.conop.analysis.Solution;
 import org.andrill.conop.analysis.SummarySpreadsheet;
 import org.andrill.conop.analysis.SummarySpreadsheet.Summary;
 
 import com.google.common.collect.Lists;
 
 /**
  * A simple GUI for performing post processing.
  * 
  * @author Josh Reed (jareed@andrill.org)
  */
 public class AnalysisPanel extends JPanel {
 	private static final long serialVersionUID = 1L;
 	private static final Class<?>[] SUMMARIES = new Class<?>[] { AgeAndPlacements.class, CompareSolutions.class };
 	private final List<Class<?>> summaries = Lists.newArrayList(SUMMARIES);
 
 	public AnalysisPanel() {
 		initComponents();
 	}
 
 	private String getLabelFor(final Class<?> summary) {
 		StringBuilder s = new StringBuilder();
 		for (char c : summary.getSimpleName().toCharArray()) {
 			int i = c;
 			if ((i >= 65) && (i <= 90) && (s.length() > 0)) {
 				s.append(" ");
 			}
 			s.append(c);
 		}
 		return s.toString();
 	}
 
 	private void initComponents() {
 		setLayout(new MigLayout("fill", "", "[][grow][]"));
 
 		final JFileChooser fileChooser = new JFileChooser(new File("."));
		fileChooser.setAcceptAllFileFilterUsed(false);
 		final JButton processButton = new JButton();
 		processButton.setEnabled(false);
 		final JButton addButton = new JButton();
 		final JButton removeButton = new JButton();
 		removeButton.setEnabled(false);
 
 		// list of files
 		add(new JLabel("Runs:"), "wrap");
 		final DefaultListModel runs = new DefaultListModel();
 		final JList list = new JList(runs);
 		add(new JScrollPane(list), "grow, span, wrap");
 
 		addButton.setAction(new AbstractAction("+") {
 			private static final long serialVersionUID = 1L;
 
 			public void actionPerformed(final ActionEvent arg0) {
 				fileChooser.setDialogTitle("Choose Run File or Directory");
 				if (fileChooser.showOpenDialog(AnalysisPanel.this) == JFileChooser.APPROVE_OPTION) {
					runs.addElement(fileChooser.getSelectedFile());
 				}
 				processButton.setEnabled((runs.size() > 0) && (summaries.size() > 0));
 				removeButton.setEnabled(runs.size() > 0);
 			}
 		});
 		addButton.putClientProperty("JButton.buttonType", "gradient");
 		add(addButton, "split, align right");
 
 		removeButton.setAction(new AbstractAction("-") {
 			private static final long serialVersionUID = 1L;
 
 			public void actionPerformed(final ActionEvent e) {
 				Object selected = list.getSelectedValue();
 				if (selected != null) {
 					runs.removeElement(selected);
 				}
 				processButton.setEnabled((runs.size() > 0) && (summaries.size() > 0));
 				removeButton.setEnabled(runs.size() > 0);
 			}
 		});
 		removeButton.putClientProperty("JButton.buttonType", "gradient");
 		removeButton.setEnabled(false);
 		add(removeButton, "wrap");
 
 		add(new JLabel("Analyses:"), "span, wrap");
 		for (final Class<?> summary : SUMMARIES) {
 			final JCheckBox checkbox = new JCheckBox(getLabelFor(summary), true);
 			checkbox.setAction(new AbstractAction(getLabelFor(summary)) {
 				private static final long serialVersionUID = 1L;
 
 				public void actionPerformed(final ActionEvent e) {
 					if (checkbox.isSelected()) {
 						summaries.add(summary);
 					} else {
 						summaries.remove(summary);
 					}
 					processButton.setEnabled((runs.size() > 0) && (summaries.size() > 0));
 				}
 			});
 			add(checkbox, "span, wrap");
 		}
 
 		processButton.setAction(new AbstractAction("Process") {
 			private static final long serialVersionUID = 1L;
 
 			public void actionPerformed(final ActionEvent e) {
				fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
 				fileChooser.setDialogTitle("Save");
 				if (fileChooser.showSaveDialog(AnalysisPanel.this) == JFileChooser.APPROVE_OPTION) {
 					File out = fileChooser.getSelectedFile();
 
 					// create a list of summaries
 					List<Summary> list = Lists.newArrayList();
 					for (Class<?> c : summaries) {
 						try {
 							list.add((Summary) c.newInstance());
 						} catch (InstantiationException e1) {
 							// should never happen
 						} catch (IllegalAccessException e1) {
 							// should never happen
 						}
 					}
 
 					// create a list of runs
 					List<Solution> solutions = Lists.newArrayList();
 					for (int i = 0; i < runs.size(); i++) {
 						File file = (File) runs.getElementAt(i);
 						if (file.isDirectory()) {
 							solutions.add(new CONOP9Solution(file));
 						} else {
 							solutions.add(new CONOP4JSolution(file));
 						}
 					}
 
 					// write our summary spreadsheet
 					SummarySpreadsheet spreadsheet = new SummarySpreadsheet(list.toArray(new Summary[list.size()]));
					spreadsheet.write(out, solutions.toArray(new CONOP9Solution[0]));
 
 					JOptionPane.showMessageDialog(AnalysisPanel.this,
 							"Summary spreadsheet written to: " + out.getName());
 				}
 			}
 		});
 		add(processButton, "span, align right");
 	}
 }
