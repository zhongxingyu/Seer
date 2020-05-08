 /**
  * 
  */
 package org.geworkbench.util;
 
 import java.awt.BorderLayout;
 import java.awt.Container;
 import java.awt.Frame;
 import java.awt.GridLayout;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.io.IOException;
 import java.lang.reflect.InvocationTargetException;
 
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JComboBox;
 import javax.swing.JDialog;
 import javax.swing.JEditorPane;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.SwingUtilities;
 import javax.swing.event.HyperlinkEvent;
 import javax.swing.event.HyperlinkListener;
 
 import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
 import org.geworkbench.bison.datastructure.bioobjects.markers.annotationparser.Affy3ExpressionAnnotationParser;
 import org.geworkbench.bison.datastructure.bioobjects.markers.annotationparser.AffyAnnotationParser;
 import org.geworkbench.bison.datastructure.bioobjects.markers.annotationparser.AffyGeneExonStAnnotationParser;
 import org.geworkbench.bison.datastructure.bioobjects.markers.annotationparser.AnnotationParser;
 import org.geworkbench.engine.preferences.PreferencesManager;
 import org.geworkbench.engine.properties.PropertiesManager;
 import org.geworkbench.parsers.InputFileFormatException;
 import org.geworkbench.util.AnnotationInformationManager.AnnotationType;
 
 import com.jgoodies.forms.builder.ButtonBarBuilder;
 
 /**
  * @author zji
  *
  */
 // TODO rename this class. there is a dangerous case of same name in other package
 public class AffyAnnotationUtil {
 
 	public static String matchAffyAnnotationFile(final DSMicroarraySet dataset)  throws InputFileFormatException {
 		PreferencesManager preferencesManager = PreferencesManager
 				.getPreferencesManager();
 		File prefDir = preferencesManager.getPrefDir();
 		final File annotFile = new File(prefDir, "annotations.prefs");
 		if (!annotFile.exists()) {
 			try {
 				SwingUtilities.invokeAndWait(new Runnable() {
 
 					public void run() {
 						boolean dontShowAgain = showAnnotationsMessage();
 						if (dontShowAgain) {
 							try {
 								annotFile.createNewFile();
 							} catch (IOException e) {
 								e.printStackTrace();
 							}
 						}
 					}
 
 				});
 			} catch (InterruptedException e1) {
 				e1.printStackTrace();
 			} catch (InvocationTargetException e1) {
 				e1.printStackTrace();
 			}
 		}
 
 		try {
 			SwingUtilities.invokeAndWait(new Runnable() {
 				public void run() {
 					userFile = selectAnnotationFile();
 					if(userFile!=null)
 						parser = selectAnnotationParser();
 				}
 			});
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		} catch (InvocationTargetException e) {
 			e.printStackTrace();
 		}
 
 		if (userFile != null) {
 			AnnotationParser.loadAnnotationFile(dataset, userFile, parser);
 			return userFile.getName();
 		} else {
 			return "Other";
 		}
 	}
 
 	// TODO these may be avoided by re-organize the EDT/non-EDT code
 	private volatile static File userFile = null;
 	private volatile static AffyAnnotationParser parser = null;
 
 	public static boolean showAnnotationsMessage() {
 		String message = "To process Affymetrix files many geWorkbench components require information from the associated chip annotation files. Annotation files can be downloaded from the Affymetrix web site, <a href='http://www.affymetrix.com/support/technical/byproduct.affx?cat=arrays' target='_blank'>http://www.affymetrix.com/support/technical/byproduct.affx?cat=arrays</a> (due to the Affymetrix license we are precluded from shipping these files with geWorkbench). Place downloaded files to a directory of your choice; when prompted by geWorkbench point to the appropriate annotation file to be associated with the microarray data you are about to load into the application. Your data will load even if you do not associate them with an annotation file; in that case, some geWorkbench components will not be fully functional.<br>\n"
 				+ "<br>\n"
 				+ "NOTE: Affymetrix requires users to register in order to download annotation files from its web site. Registration is a one time procedure. The credentials (user id and password) acquired via the registration process can then be used in subsequent interactions with the site.<br>\n"
 				+ "<br>\n"
 				+ "Each chip type in the Affymetrix site can have several associated annotation files (with names like \"...Annotations, BLAST\", \"...Annotations, MAGE-ML XML\", etc). Only annotation files named \"...Annotations, CSV\" need to be downloaded (these are the only files that geWorkbench can process).<br>";
 		final JDialog window = new JDialog((Frame) null,
 				"Annotations Information");
 		Container panel = window.getContentPane();
 		JEditorPane textarea = new JEditorPane("text/html", message);
 		textarea.setEditable(false);
 		textarea.addHyperlinkListener(new HyperlinkListener() {
 			public void hyperlinkUpdate(HyperlinkEvent e) {
 				if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
 					try {
 						BrowserLauncher.openURL("http://www.affymetrix.com/support/technical/byproduct.affx?cat=arrays");
 					} catch (IOException e1) { // ignore it
 						e1.printStackTrace();
 					}
 				}
 			}
 		});
 		panel.add(textarea, BorderLayout.CENTER);
 		ButtonBarBuilder builder = ButtonBarBuilder.createLeftToRightBuilder();
 		JCheckBox dontShow = new JCheckBox("Don't show this again");
 		builder.addFixed(dontShow);
 		builder.addGlue();
 		JButton jButton = new JButton("Continue");
 		builder.addFixed(jButton);
 		jButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				window.dispose();
 			}
 		});
 		panel.add(builder.getPanel(), BorderLayout.SOUTH);
 		int width = 500;
 		int height = 450;
 		window.pack();
 		window.setSize(width, height);
 		window
 				.setLocation(
 						(Toolkit.getDefaultToolkit().getScreenSize().width - width) / 2,
 						(Toolkit.getDefaultToolkit().getScreenSize().height - height) / 2);
 		window.setModal(true);
 		window.setVisible(true);
 		window.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 
 		return dontShow.isSelected();
 	}
 
 	private static AffyAnnotationParser selectAnnotationParser() {		
 
 		final JDialog dialog = new JDialog();
 		dialog.setTitle("Select Annotation File Type");
 
 		JPanel panel = new JPanel();
 		panel.setLayout(new BorderLayout());
 
 		JPanel panel1 = new JPanel();
 		panel1.add(new JLabel("Annotation file type:   "));
 
 		final JComboBox annotationFileTypeJcb = new JComboBox();
 		annotationFileTypeJcb.addItem("Please Select");
 		annotationFileTypeJcb.addItem(AnnotationType.AFFYMETRIX_3_EXPRESSION);
 		 
		annotationFileTypeJcb.addItem(AnnotationType.AFFY_GENE_EXON_10_ST);
 
 		panel1.add(annotationFileTypeJcb);
 
 		JPanel panel3 = new JPanel(new GridLayout(0, 3));
 
 		JButton continueButton = new JButton("Continue");
 		JButton cancelButton = new JButton("Cancel");
 
 		continueButton.addActionListener(new ActionListener() {
 
 			public void actionPerformed(ActionEvent e) {
 				AnnotationType parserType = (AnnotationType)annotationFileTypeJcb.getSelectedItem();
 						 
 				if (parserType.equals(AnnotationType.AFFYMETRIX_3_EXPRESSION)) {
 					parser = new Affy3ExpressionAnnotationParser();
 				} else if (parserType
						.equals(AnnotationType.AFFY_GENE_EXON_10_ST)) {
 					parser = new AffyGeneExonStAnnotationParser();
 				} else {
 					return;
 				}
 				dialog.dispose();
 			}
 		});
 		cancelButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				dialog.dispose();
 				parser = null;
 			}
 		});
 
 		panel3.add(continueButton);
 		panel3.add(new JLabel("  "));
 		panel3.add(cancelButton);
 
 		panel.add(panel1, BorderLayout.NORTH);
 		panel.add(panel3, BorderLayout.SOUTH);
 
 		dialog.add(panel);
 		dialog.setModal(true);
 		dialog.pack();
 		dialog.setLocationRelativeTo(null);
 		dialog.setVisible(true);
 
 		return parser;
 	}
 
 	private static File selectAnnotationFile() {
 		PropertiesManager properties = PropertiesManager.getInstance();
 		String annotationDir = System.getProperty("user.dir"); ;
 		try {
 			annotationDir = properties.getProperty(AnnotationParser.class,
 					ANNOT_DIR, annotationDir);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 		JFileChooser chooser = new JFileChooser(annotationDir);
 		chooser.setFileFilter(new CsvFileFilter());
 		chooser.setDialogTitle("Please select the annotation file");
 		int returnVal = chooser.showOpenDialog(null);
 		if (returnVal == JFileChooser.APPROVE_OPTION) {
 			File userAnnotations = chooser.getSelectedFile();
 			try {
 				properties.setProperty(AnnotationParser.class, ANNOT_DIR,
 						userAnnotations.getParent());
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 			return userAnnotations;
 		} else {
 			return null;
 		}
 	}
 
 	private static final String ANNOT_DIR = "annotDir";
 }
