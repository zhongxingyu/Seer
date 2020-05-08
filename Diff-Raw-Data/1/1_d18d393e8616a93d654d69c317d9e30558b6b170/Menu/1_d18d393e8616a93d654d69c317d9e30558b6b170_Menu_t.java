 import java.awt.Font;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.util.ArrayList;
 
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JPanel;
 import javax.swing.JTextArea;
 import javax.swing.JToolBar;
 
 public class Menu extends JFrame implements ActionListener {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	JMenuBar bar;
 	JToolBar tb;
 	JFrame frame;
 	JMenu file, edit, exit;
 	JMenuItem open, save, copy, paste, exit1;
 	JButton openb, saveb, exitb, scheduleb;
 	JTextArea textArea;
 	JFileChooser chooser;
 	FileOutputStream fos;
 	BufferedWriter bwriter;
 	StudentDB students;
 
 	public Menu(StudentDB s, JFrame f) {
 		frame = f;
 		students = s;
 		bar = new JMenuBar();
 		bar.setFont(new Font("Arial", Font.BOLD, 14));
 		file = new JMenu(" File ");
 		open = new JMenuItem(" Open... ");
 		open.addActionListener(this);
 		save = new JMenuItem(" Save... ");
 		save.addActionListener(this);
 		file.add(open);
 		file.add(save);
 		bar.add(file);
 		edit = new JMenu(" Edit ");
 		copy = new JMenuItem(" Copy ");
 		paste = new JMenuItem(" Paste ");
 		edit.add(copy);
 		edit.add(paste);
 		bar.add(edit);
 		exit = new JMenu(" Exit ");
 		exit1 = new JMenuItem("Exit Application");
 		exit1.addActionListener(this);
 
 		exit.add(exit1);
 		bar.add(exit);
 
 		// Create Toolbar
 		tb = new JToolBar();
 		ImageIcon openi = new ImageIcon(getClass().getResource("open.png"));
 		ImageIcon savei = new ImageIcon(getClass().getResource("save.png"));
 		ImageIcon exiti = new ImageIcon(getClass().getResource("exit.png"));
 
 		openb = new JButton(openi);
 		openb.addActionListener(this);
 		saveb = new JButton(savei);
 		saveb.addActionListener(this);
 		exitb = new JButton(exiti);
 		exitb.addActionListener(this);
 		scheduleb = new JButton();
 		scheduleb.setText("<html>Generate<br>Schedule</html>");
 		scheduleb.addActionListener(this);
 		JPanel flowNorth = new JPanel(); // defaults to centered FlowLayout
 		flowNorth.add(scheduleb);
 
 		tb.add(openb);
 		tb.add(saveb);
 		tb.add(exitb);
 		tb.add(flowNorth);
 		tb.setAlignmentX(0);
 
 	}
 
 	public void actionPerformed(ActionEvent event) {
 		Object obj = event.getSource();
 		chooser = new JFileChooser();
 		chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
 		chooser.setFileFilter(chooser.getAcceptAllFileFilter());
 		if (obj.equals(open) || obj.equals(openb)) {
 			// use chooser.getSelectedFile() to get file
 			// Some code here to parse or call a parser
 			if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
 				readFile(chooser.getSelectedFile());
 
 		} else if (obj.equals(save) || obj.equals(saveb)) {
 			if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION)
 				writeFile(chooser.getSelectedFile());
 		} else if (obj.equals(copy)) {
 
 		} else if (obj.equals(paste)) {
 
 		} else if (obj.equals(exit1) || obj.equals(exitb)) {
 			// Probably want to do checking here if user wants to save
 			System.exit(0);
 		} else if (obj.equals(scheduleb)) {
 			// Code here to call schedule algorithm and display schedules
 			Schedulizer.genSchedule(students);
 		}
 
 	}
 
 	public JMenuBar getMenu() {
 		return bar;
 	}
 
 	public JToolBar getToolBar() {
 		return tb;
 	}
 
 	private void readFile(File filename) {
 		try {
 			FileInputStream fis = new FileInputStream(filename);
 			BufferedReader br = new BufferedReader(new InputStreamReader(fis));
 			while (br.ready()) {
 				String line = br.readLine();
 				String[] params = line.split(",");
 				if (params.length == 2) {
 					Students s = new Students(params[0],
 							Integer.parseInt(params[1]), 0, 0, 0);
 					students.addStudent(s);
 				} else if (params.length == 5) {
 					Students s = new Students(params[0],
 							Integer.parseInt(params[1]),
 							Integer.parseInt(params[2]),
 							Integer.parseInt(params[4]),
 							Integer.parseInt(params[3]));
 					students.addStudent(s);
 				} else {
 					// error
 				}
 			}
 			new NewFrame(frame, students);
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	private void writeFile(File filename) {
 		try {
 			FileOutputStream fos = new FileOutputStream(filename);
 			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
 			ArrayList<Students> stds = students.getStudents();
 
 			for (int i = 0; i < students.getSize(); i++) {
 				Students s = stds.get(i);
 				String line;
 				line = s.getName() + "," + s.getAge() + "," + s.getMath() + ","
 						+ s.getRead() + "," + s.getLA() + "\n";
 				bw.append(line);
 
 			}
 			bw.close();
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 }
