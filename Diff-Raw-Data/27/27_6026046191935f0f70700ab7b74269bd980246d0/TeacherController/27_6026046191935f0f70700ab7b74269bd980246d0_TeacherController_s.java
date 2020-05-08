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
 import java.io.Serializable;
 import java.util.ArrayList;
 
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 
 public class TeacherController implements ActionListener, Serializable {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	JFrame frame;
 	TeacherDB teachers;

	public TeacherController(JFrame f, TeacherDB t) {
 		frame = f;
 		teachers = t;
 	}
 
 	public void actionPerformed(ActionEvent event) {
 		Object obj = event.getSource();
 		JFileChooser chooser = new JFileChooser();
 		chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
 		chooser.setFileFilter(chooser.getAcceptAllFileFilter());
 		if (obj.equals(Menu.tSave)) {
 			if (chooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION)
 				writeFile(chooser.getSelectedFile());
 		}
 	}
 
 	void readFile(File filename) {
 		try {
 			FileInputStream fis = new FileInputStream(filename);
 			BufferedReader br = new BufferedReader(new InputStreamReader(fis));
 			
 			//Clean on a fresh open
 			teachers.removeAll();
 			
 			while (br.ready()) {
 				String line = br.readLine();
 				String[] params = line.split(",");
 				System.out.println(params.length);
 
 				Teachers t;
 				if (!Utilities.isBlank(params[0])) {
					t = new Teachers(params[0]);
 				} else {
 					return;
 				}
 				if (params.length == 1) {
 					teachers.addTeacher(t);
 					return;
 				}
 				String[] mClassList = params[1].split(";");
 				ArrayList<Integer> mClasses = new ArrayList<Integer>();
 				for (int i = 0; i < mClassList.length; i++) {
 					try {
 						int tmp = Integer.parseInt(mClassList[i]);
 						mClasses.add(tmp);
 					} catch (NumberFormatException e) {
 						// maybe an error here, but they probably wont be
 						// inputting integers so we'll need to do conversions
 					}
 				}
 				t.setPreference(mClasses, Teachers.Type.MATH);
 				if (params.length == 2) {
 					teachers.addTeacher(t);
 					return;
 				}
 
 				String[] rClassList = params[2].split(";");
 				ArrayList<Integer> rClasses = new ArrayList<Integer>();
 				for (int i = 0; i < rClassList.length; i++) {
 					try {
 						int tmp = Integer.parseInt(rClassList[i]);
 						rClasses.add(tmp);
 					} catch (NumberFormatException e) {
 						// maybe an error here, but they probably wont be
 						// inputting integers so we'll need to do conversions
 					}
 				}
 				t.setPreference(rClasses, Teachers.Type.READ);
 				if (params.length == 3) {
 					teachers.addTeacher(t);
 					return;
 				}
 
 				String[] lClassList = params[3].split(";");
 				ArrayList<Integer> lClasses = new ArrayList<Integer>();
 				for (int i = 0; i < lClassList.length; i++) {
 					try {
 						int tmp = Integer.parseInt(lClassList[i]);
 						lClasses.add(tmp);
 					} catch (NumberFormatException e) {
 						// maybe an error here, but they probably wont be
 						// inputting integers so we'll need to do conversions
 					}
 				}
 				t.setPreference(lClasses, Teachers.Type.LA);
 				teachers.addTeacher(t);
 
 			}
 
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (NumberFormatException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	private void writeFile(File filename) {
 		try {
 			FileOutputStream fos = new FileOutputStream(filename);
 			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
 			ArrayList<Teachers> tchrs = teachers.getTeachers();
 
 			for (int i = 0; i < teachers.getSize(); i++) {
 				Teachers t = tchrs.get(i);
 				System.out.println(t.getName());
 				StringBuilder line = new StringBuilder(t.getName() + ",");
 				ArrayList<Integer> mClasses = t
 						.getPreference(Teachers.Type.MATH);
 				for (int j = 0; j < mClasses.size(); j++) {
 					line.append(mClasses.get(j) + ";");
 				}
 				line.append(',');
 				ArrayList<Integer> rClasses = t
 						.getPreference(Teachers.Type.READ);
 				for (int j = 0; j < rClasses.size(); j++) {
 					line.append(rClasses.get(j) + ";");
 				}
 				line.append(',');
 				ArrayList<Integer> lClasses = t.getPreference(Teachers.Type.LA);
 				for (int j = 0; j < lClasses.size(); j++) {
 					line.append(lClasses.get(j) + ";");
 				}
 				line.append("\n");
 				bw.append(line.toString());
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
