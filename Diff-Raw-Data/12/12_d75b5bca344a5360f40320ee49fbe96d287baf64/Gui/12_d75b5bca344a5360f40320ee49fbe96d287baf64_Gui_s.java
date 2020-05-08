 package de.mb.infinity;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.FileDialog;
 import java.awt.FlowLayout;
 import java.awt.GridLayout;
 import java.awt.Image;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.BorderFactory;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JProgressBar;
 import javax.swing.JScrollPane;
 import javax.swing.JSpinner;
 import javax.swing.SpinnerNumberModel;
 
 import de.mb.swing.ClosableTabbedPane;
 import de.mb.swing.CloseAction;
 import de.mb.swing.JAdvancedTextArea;
 import de.mb.swing.JDefaultFrame;
 
 public class Gui extends JDefaultFrame implements ActionListener {
 
 
 	public static void main(String[] args) {
 
 		Gui gui = new Gui();
 		gui.setVisible(true);
 
 	}
 	
 	public Gui() {
 		initialize();
 	}
 
 	protected ClosableTabbedPane tab = new ClosableTabbedPane();
 	protected JSpinner ew_a_text = new JSpinner(new SpinnerNumberModel(10, 1,
 			20, 1));
 	protected JSpinner ew_v_text = new JSpinner(new SpinnerNumberModel(10, 1,
 			20, 1));
 	protected JSpinner num_dices_text = new JSpinner(new SpinnerNumberModel(2,
 			1, 5, 1));
 	protected JCheckBox with_matrix = new JCheckBox();
 
 	protected JProgressBar progress = new JProgressBar(0, 100);
 
 	protected JButton getActionButton(String text, String actionCommand) {
 		JButton temp = new JButton(text);
 		temp.setActionCommand(actionCommand);
 		temp.addActionListener(this);
 		return temp;
 	}
 
 	protected Image createImage(String name) {
 		return Toolkit.getDefaultToolkit().createImage(
 				this.getClass().getResource(name));
 	}
 
 	protected JMenuBar getMenu() {
 		JMenuBar menu = new JMenuBar();
 		
 		JMenu file = new JMenu("File");
 		JMenuItem file_exit = new JMenuItem("Exit");
 		file_exit.setActionCommand("file_exit");
 		file_exit.addActionListener(this);
 		
 		file.add(file_exit);
 
 		
 		JMenu info = new JMenu("Info");
 		JMenuItem info_about = new JMenuItem("About");
 		info_about.setActionCommand("info_about");
 		info_about.addActionListener(this);
 		
 		info.add(info_about);
 
 		
 		menu.add(file);
 		menu.add(info);
 		return menu;
 	}
 	
 	protected void initialize() {
 		
 		setJMenuBar(getMenu());
 		
 		// final JDefaultFrame gui = new JDefaultFrame();
 		this.setIconImage(createImage("/resource/images/ilogo.png"));
 		this.setTitle("Infinity Berechner");
 		this.setLayout(new BorderLayout());
 		this.setBounds(50, 50, 640, 480);
 
 		JPanel output = new JPanel(new GridLayout(1, 1));
 
 		JPanel settings = new JPanel(new FlowLayout());
 
 		progress.setOrientation(JProgressBar.VERTICAL);
 		progress.setBorder(BorderFactory.createLineBorder(Color.gray));
 		progress.setForeground(Color.blue);
 		progress.setValue(0);
 
 		JButton start = getActionButton("Berechnung starten", "single");
 		JButton iterStart = getActionButton("iterative Berechnung starten",
 				"iterate");
 
 		JPanel top = new JPanel(new GridLayout(2, 1));
 
 		settings.add(new JLabel("EW Angreifer"));
 		settings.add(ew_a_text);
 		settings.add(new JLabel("EW Verteidiger"));
 		settings.add(ew_v_text);
 		settings.add(new JLabel("Anzahl Würfel Angreifer"));
 		settings.add(num_dices_text);
 		settings.add(new JLabel("Matrixausgabe?"));
 		settings.add(with_matrix);
 
 		ImageIcon logo = new ImageIcon(
 				createImage("/resource/images/fulllogo.png"));
 
 		top.add(new JLabel(logo));
 		top.add(settings);
 
 		this.add(top, BorderLayout.NORTH);
 
		output.add(new JScrollPane(tab));
 
 		this.add(output, BorderLayout.CENTER);
 		this.add(progress, BorderLayout.EAST);
 
 		JPanel bottom = new JPanel(new GridLayout(1, 2));
 		bottom.add(start);
 		bottom.add(iterStart);
 
 		this.add(bottom, BorderLayout.SOUTH);
 	}
 
 	public void actionPerformed(ActionEvent e) {
 		if (e.getActionCommand().equalsIgnoreCase("single")) {
 			((JButton)e.getSource()).setEnabled(false);
 			single(new Byte(ew_a_text.getValue().toString()).byteValue(),
 					new Byte(ew_v_text.getValue().toString()).byteValue(),
 					new Byte(num_dices_text.getValue().toString()).byteValue(),
 					with_matrix.isSelected()
 				);
 			((JButton)e.getSource()).setEnabled(true);
 		}
 		else if (e.getActionCommand().equalsIgnoreCase("iterate")) {
 			((JButton)e.getSource()).setEnabled(false);
 			iterate(new Byte(ew_a_text.getValue().toString()).byteValue(),
 					new Byte(ew_v_text.getValue().toString()).byteValue(),
 					new Byte(num_dices_text.getValue().toString()).byteValue(),
 					with_matrix, ((JButton)e.getSource()));
 		}
 		else if (e.getActionCommand().equalsIgnoreCase("info_about")) {
 			JOptionPane.showMessageDialog(this, "(c) 2009 by Schattenschwinge & firegate666", "About", JOptionPane.OK_OPTION, new ImageIcon(createImage("/resource/images/ilogo.png")));
 		} else if (e.getActionCommand().equalsIgnoreCase("file_exit"))
 			new CloseAction(e);
 			
 	}
 
 	protected boolean confirm(String title, String message) {
 		return JOptionPane.showConfirmDialog(this, message, title,
 				JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
 	}
 
 	protected void iterate(byte ew_a, byte ew_v, byte dices, JCheckBox with_matrix, JButton invocator) {
 
 
 		if (dices > 3
 				&& !confirm("Achtung!",
 						"Die Berechnung mit mehr als 3 Würfeln dauert länger. Wirklich fortfahren?"))
 			return;
 
 		if (confirm(
 				"Iterative Berechnung",
 				"Die iterative Berechnung startet jeweils bei EW=0 bis zum gewählten Limit ("
 						+ ew_a
 						+ "/"
 						+ ew_v
 						+ ")\nund wählt die Würfel von Anzahl=1 bis zum gewählten Limit ("
 						+ dices + ").\n\nBerechnung jetzt starten?")) {
 
 			final StringBuffer outfile_name = new StringBuffer();
 			final StringBuffer outfile_path = new StringBuffer();
 
 			final JAdvancedTextArea result = new JAdvancedTextArea("");
 			result.setEditable(false);
 			if (with_matrix.isSelected()) {
 				FileDialog fd = new FileDialog(this);
 				fd.setAlwaysOnTop(true);
 				fd.setMode(FileDialog.SAVE);
 				fd.setFile("output.csv");
 				fd.setVisible(true);
 
 				outfile_path.append(fd.getDirectory());
 				outfile_name.append(fd.getFile());
 				if (fd.getDirectory() == null || fd.getFile() == null) {
 					result
 							.setText("Da keine Datei ausgewählt wurde, wurde die Matrixausgabe abgeschaltet.\n\n");
 					with_matrix.setSelected(false);
 				}
 			}
 
 			final byte max_ew_a = ew_a;
 			final byte max_ew_v = ew_v;
 			final byte max_dices = dices;
 
 			result.setText(result.getText() + "Berechne Angreifer EW bis "
 					+ ew_a + " und " + dices
 					+ " Würfel und Verteidiger EW bis " + ew_v + "\n\n");
 			Thread t = new Thread(new IterateCalculator(progress, max_ew_a,
 					max_ew_v, max_dices, outfile_path, outfile_name,
 					with_matrix.isSelected(), result, tab, invocator));
 			t.start();
 		}
 	}
 
 	protected void single(byte ew_attacker, byte ew_defender, byte num_dices, boolean output_to_file) {
 		try {
 
 			if (num_dices > 3) {
 				int ret = JOptionPane
 						.showConfirmDialog(
 								this,
 								"Die Berechnung mit mehr als 3 Würfeln dauert länger. Wirklich fortfahren?",
 								"Achtung!", JOptionPane.YES_NO_OPTION);
 				if (ret == JOptionPane.NO_OPTION)
 					return;
 			}
 			JAdvancedTextArea result = new JAdvancedTextArea("");
 			result.setEditable(false);
 			String outfile = "";
 			if (output_to_file) {
 				FileDialog fd = new FileDialog(this);
 				fd.setAlwaysOnTop(true);
 				fd.setMode(FileDialog.SAVE);
 				fd.setFile("output.csv");
 				fd.setVisible(true);
 				outfile = fd.getDirectory() + fd.getFile();
 			}
 
 			InfinityProcessor main = new InfinityProcessor(ew_attacker, ew_defender, num_dices, output_to_file,
 					outfile);
 			double[] result_counter = main.process();
 			Thread.sleep(100);
 			if (result_counter[0] > 50.0
 					&& result_counter[0] > result_counter[1] * 1.5) {
 				result.setBorder(BorderFactory.createLineBorder(Color.GREEN));
 			} else {
 				result.setBorder(BorderFactory.createLineBorder(Color.RED));
 			}
 			result.setText("Angreifer " + result_counter[0] + "%\nVerteidiger "
 					+ result_counter[1] + "%");
 
 			if (output_to_file) {
 				result.setText(result.getText()
 						+ "\n\nDas Ergebnis der Analyse wurde nach " + outfile
 						+ " geschrieben.");
 			}
			tab.add(result, ew_attacker + "(" + num_dices + ")/" + ew_defender);
 		} catch (Exception ex) {
 			JOptionPane.showMessageDialog(null,
 					"Es ist ein Fehler aufgetreten: " + ex.getMessage());
 		}
 		// result.setVisible(true);
 	}
 
 }
