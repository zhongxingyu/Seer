 package edu.gatech.arktos;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.Point;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ComponentEvent;
 import java.awt.event.ComponentListener;
 import java.awt.event.FocusEvent;
 import java.awt.event.FocusListener;
 import java.awt.font.TextAttribute;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Hashtable;
 import java.util.Map;
 import java.util.Set;
 
 import javax.swing.BorderFactory;
 import javax.swing.ComboBoxModel;
 import javax.swing.DefaultListCellRenderer;
 import javax.swing.DefaultListModel;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JList;
 import javax.swing.JOptionPane;
 import javax.swing.JScrollPane;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 
 import com.google.gdata.util.ServiceException;
 
 public class GradesTool {
 	
 	private static JComboBoxThemed<Student> _comboBoxStudent;
 	private static JComboBoxThemed<String> _comboBoxProject;
 	private static JComboBoxThemed<String> _comboBoxTeam;
 	private static JComboBoxThemed<Assignment> _comboBoxAssignments;
 	private static JComboBoxThemed<ProjectTeam> _comboBoxProjects;
 	
 	private static LabelExtended labelGTIDvalue;
 	private static LabelExtended labelEmailValue;
 	private static LabelExtended labelAttendanceValue;
 	private static LabelExtended labelAssignmentsAverageGrade;
 	private static LabelExtended labelAssignmentGradeValue;
 	private static LabelExtended labelAssignmentAverageGrade;
 	private static LabelExtended labelProjectAverageGrade;
 	private static LabelExtended labelProjectGradeTotalValue;
 	private static JComboBoxThemed<String> _comboBoxProjectContributionGrade;
 	private static JComboBoxThemed<String> _comboBoxProjectAverageGrade;
 	private static JComboBoxThemed<String> _comboBoxTeamContributionMembers;
 	private static JList<String> _listMembers;
 	private static JList<String> _listGrades;
 	private static JList<String> _listContributions;
 	
 	private static GradesDB gdb;
 	
 	private static ArrayList<ArrayList<ProjectTeam>> projectsTeams;
 	private static DefaultListModel<String> membersModel;
 	private static DefaultListModel<String> gradesModel;
 	private static DefaultListModel<String> contributionsModel;
 	
 	private static boolean updateRightSide = true;
 	
 	
 	@SuppressWarnings({ "unchecked", "rawtypes", "serial" })
 	public static void main(String [] args) throws IOException, ServiceException {
 		
 		final JFrame frame = new JFrame("GradesTool");
 		frame.setContentPane(new Container() {
 			public void paint(Graphics g) {
 				super.paint(g);
 				
 				g.setColor(Color.getHSBColor(0, 0, 0.75f));
 				g.drawLine(325, 25, 325, 465);
 				
 				g.drawLine(25, 82, 620, 82);
 			}
 		});
 		frame.getContentPane().setPreferredSize(new Dimension(640, 480));
 		frame.pack();
 		frame.setResizable(false);
 		frame.setLayout(null);
 		frame.setLocationRelativeTo(null);
 		frame.addComponentListener(new ComponentListener() {
 			@Override
 			public void componentResized(ComponentEvent e) {
 			}
 			@Override
 			public void componentMoved(ComponentEvent e) {
 			}
 			@Override
 			public void componentShown(ComponentEvent e) {
 			}
 			@Override
 			public void componentHidden(ComponentEvent e) {
 				System.exit(0);
 			}
 		});
 		
 		Font font = null;
 		try {
 			String pathSeparator = System.getProperty("file.separator");
 			font = ResourcesDispatcher.getFont("resources" + pathSeparator + "project.ttf", Font.TRUETYPE_FONT);
 			font = font.deriveFont(15.0f);
 		}
 		catch (ResourceException e1) {
 			e1.printStackTrace();
 		}
 		
 		_comboBoxStudent = new JComboBoxThemed<Student>();
 		frame.add(_comboBoxStudent);
 		_comboBoxStudent.setLocation(new Point(15, 35));
 		_comboBoxStudent.setSize(210, 35);
 		_comboBoxStudent.setFont(font);
 		_comboBoxStudent.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				Student s = _comboBoxStudent.getSelectedItem();
 				
 				if (s != null) {
 					labelGTIDvalue.setText(s.getGtid());
 					labelEmailValue.setText(s.getEmail());
 					labelAttendanceValue.setText(String.valueOf(s.getAttendance()));
 					
 					labelAssignmentsAverageGrade.setText("average " + String.valueOf(s.getAverageAssignmentGrade()));
 					
 					_comboBoxAssignments.removeAllItems();
 					ArrayList<Assignment> assigns = s.getAssignments();
 					for (Assignment assign: assigns) {
 						_comboBoxAssignments.addItem(assign);
 					}
 					
 					_comboBoxProjects.removeAllItems();
 					ArrayList<ProjectTeam> projects = s.getProjects();
 					for (ProjectTeam project: projects) {
 						_comboBoxProjects.addItem(project);
 					}
 				}
 			}
 		});
 		ButtonExtended save = new ButtonExtended("Save ->", font);
 		save.setLocation(237, 35);
 		save.setSize(65, 35);
 		frame.add(save);
 		save.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				JFileChooser fileChooser = new JFileChooser();
 		        fileChooser.setDialogTitle("Select directory to save the user information to");
 		        fileChooser.setApproveButtonText("Choose");
 		        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
 		        if (fileChooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
 		        	File f = fileChooser.getSelectedFile();
 		        	
 		        	try{
 		        		if (!f.createNewFile()) {
 			        		if (!f.canWrite()) {
 			        			JOptionPane.showMessageDialog(frame, "The filename in that directory can't be written. Please, check permissions and try again.");
 			        			return;
 			        		}
 			        	}
 		        		
 		        		Student s = _comboBoxStudent.getSelectedItem();
 		        		FileWriter fstream = new FileWriter(f.getAbsoluteFile() + System.getProperty("file.separator") + s.getName() + ".txt");
 		        		BufferedWriter out = new BufferedWriter(fstream);
 		        		out.write(s.getName() + System.lineSeparator());
 		        		for (int i = 0; i < s.getName().length(); ++i) {
 		        			out.write("-");
 		        		}
 		        		out.write(System.lineSeparator());
 		        		out.write("\tGTID: " + s.getGtid() + System.lineSeparator());
 		        		out.write("\te-mail: " + s.getEmail() + System.lineSeparator());
 		        		out.write("\tAttendance: " + s.getAttendance() + System.lineSeparator());
 		        		out.write(System.lineSeparator());
 		        		out.write("Grades" + System.lineSeparator());
 		        		
 		        		out.write("\tAssignments " + System.lineSeparator());
 		        		ComboBoxModel assignments = _comboBoxAssignments.getModel();
 		        		for (int i = 0; i < assignments.getSize(); ++i) {
 		        			Assignment a = (Assignment)assignments.getElementAt(i);
 		        			
 		        			out.write("\t\t" + a.getName() + ": " + a.getDesc() + " - score " + a.getGrade() + System.lineSeparator());
 		        		}
 		        		out.write("\t\tAverage score: " + s.getAverageAssignmentGrade() + System.lineSeparator());
 		        		out.write(System.lineSeparator());
 		        		
 		        		out.write("\tProjects " + System.lineSeparator());
 		        		ComboBoxModel projects = _comboBoxProjects.getModel();
 		        		for (int i = 0; i < projects.getSize(); ++i) {
 		        			ProjectTeam p = (ProjectTeam)projects.getElementAt(i);
 		        			
 		        			out.write("\t\t" + "Project " + p + System.lineSeparator());
 		        			
 		        			out.write("\t\t\tTeam grades:" + System.lineSeparator());
 		        			HashMap<String, Integer> scores = p.getAllTeamScores();
 		        			Set<String> keys = scores.keySet();
 		        			for (String key: keys) {
 		        				if (key.equals("TOTAL")) {
 		        					continue;
 		        				}
 		        				
 		        				int grade = scores.get(key);
 		        				out.write("\t\t\t\t" + ((grade < 10) ? "0" : "") + grade + ": " + key + System.lineSeparator());
 		        			}
 		        			out.write("\t\t\t\t" + scores.get("TOTAL") + ": TOTAL" + System.lineSeparator());
 		        			out.write(System.lineSeparator());
 		        			
 		        			out.write("\t\t\tContribution grades received" + System.lineSeparator());
 		            		ArrayList<Integer> contributions = p.getPeerScores(s.getName());
 		            		ArrayList<String> teamMembers = p.getTeamMembers();
 		            		int j = 0;
 		            		for (String member: teamMembers) {
 		            			if (member.equals(s.getName())) {
 		            				continue;
 		            			}
 		            			
 		            			int grade = contributions.get(j);
 		            			out.write("\t\t\t\t" + ((grade < 10) ? "0" : "") + grade + ": " + member + System.lineSeparator());
 		            			++j;
 		            		}
 		            		out.write(System.lineSeparator());
 		        		}
 		        		
 		        		out.flush();
 		        		out.close();
 		    		}
 		        	catch (Exception ex) {
 		        		JOptionPane.showMessageDialog(frame, "There was some error while saving the file. The system said: " + ex.getMessage());
 		    		}
 		        }
 			}
 		});
 		
 		_comboBoxProject = new JComboBoxThemed();
 		frame.add(_comboBoxProject);
 		_comboBoxProject.setLocation(new Point(350, 35));
 		_comboBoxProject.setSize(new Dimension(65, 35));
 		_comboBoxProject.setFont(font);
 		_comboBoxProject.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				int projectNumber = _comboBoxProject.getSelectedIndex();
 				
 				if (projectNumber >= 0) {
 					ArrayList<ProjectTeam> projectTeams = projectsTeams.get(projectNumber);
 					
 					_comboBoxTeam.removeAllItems();
 					for (int i = 0; i < projectTeams.size(); ++i) {
 						_comboBoxTeam.addItem(((i < 10) ? "0" : "") + (i + 1));
 					}
 				}
 			}
 		});
 		
 		_comboBoxTeam = new JComboBoxThemed();
 		frame.add(_comboBoxTeam);
 		_comboBoxTeam.setLocation(new Point(430, 35));
 		_comboBoxTeam.setSize(new Dimension(80, 35));
 		_comboBoxTeam.setFont(font);
 		_comboBoxTeam.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				int projectNumber = _comboBoxProject.getSelectedIndex();
 				int selectedTeam = _comboBoxTeam.getSelectedIndex();
 				
 				if (selectedTeam >= 0) {
 					ProjectTeam p = projectsTeams.get(projectNumber).get(selectedTeam);
 					
 					membersModel.clear();
 					_comboBoxTeamContributionMembers.removeAllItems();
 					ArrayList<String> teamMembers = p.getTeamMembers();
 					for (String member: teamMembers) {
 						membersModel.addElement(member);
 						_comboBoxTeamContributionMembers.addItem(member);
 					}
 					
 					gradesModel.clear();
 					HashMap<String, Integer> teamGrades = p.getAllTeamScores();
 					Set<String> keys = teamGrades.keySet();
 					for (String key: keys) {
 						if (!key.equals("TOTAL")) {
 							gradesModel.addElement(key + " got " + teamGrades.get(key));							
 						}
 						else {
 							labelProjectGradeTotalValue.setText(String.valueOf(teamGrades.get(key)) + " (avg. " + gdb.getAverageProjectGrade(p.getProjectNumber() - 1) + ")");
 						}
 					}
 				}
 			}
 		});
 		
 		LabelExtended labelStudent = new LabelExtended("Student:");
 		labelStudent.setFont(font);
 		labelStudent.setLocation(15, 12);
 		frame.add(labelStudent);
 		
 		LabelExtended labelProjectNumber = new LabelExtended("Project #:");
 		labelProjectNumber.setFont(font);
 		labelProjectNumber.setLocation(350, 12);
 		frame.add(labelProjectNumber);
 		
 		LabelExtended labelProjectGroup = new LabelExtended("Group #:");
 		labelProjectGroup.setFont(font);
 		labelProjectGroup.setLocation(430, 12);
 		frame.add(labelProjectGroup);
 		
 		LabelExtended labelGTID = new LabelExtended("GTID:", font);
 		labelGTID.setLocation(25, 90);
 		frame.add(labelGTID);
 		labelGTIDvalue = new LabelExtended("<Undefined>", font);
 		labelGTIDvalue.setBold(true);
 		labelGTIDvalue.setSize(185, labelGTIDvalue.getSize().height);
 		labelGTIDvalue.setLocation(115, 90);
 		labelGTIDvalue.setHorizontalAlignment(LabelExtended.ALIGN_RIGHT);
 		frame.add(labelGTIDvalue);
 		
 		LabelExtended labelEmail = new LabelExtended("e-mail:", font);
 		labelEmail.setLocation(25, 110);
 		frame.add(labelEmail);
 		labelEmailValue = new LabelExtended("<Undefined>", font);
 		labelEmailValue.setBold(true);
 		labelEmailValue.setSize(185, labelEmailValue.getSize().height);
 		labelEmailValue.setLocation(115, 110);
 		labelEmailValue.setHorizontalAlignment(LabelExtended.ALIGN_RIGHT);
 		frame.add(labelEmailValue);
 		
 		LabelExtended labelAttendance = new LabelExtended("Attendance:", font);
 		labelAttendance.setLocation(25, 130);
 		frame.add(labelAttendance);
 		labelAttendanceValue = new LabelExtended("<Undefined>", font);
 		labelAttendanceValue.setBold(true);
 		labelAttendanceValue.setSize(185, labelAttendanceValue.getSize().height);
 		labelAttendanceValue.setLocation(115, 130);
 		labelAttendanceValue.setHorizontalAlignment(LabelExtended.ALIGN_RIGHT);
 		frame.add(labelAttendanceValue);
 		
 		
 		Map<TextAttribute,Object> map = new Hashtable<TextAttribute,Object>();
 		map.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
 		Font fontUnderline = font.deriveFont(map).deriveFont(18.0f);
 		LabelExtended labelGrades = new LabelExtended("Grades", fontUnderline);
 		labelGrades.setLocation(15, 170);
 		labelGrades.setBold(true);
 		frame.add(labelGrades);
 		
 		LabelExtended labelAssignment = new LabelExtended("Assignments:", font);
 		labelAssignment.setLocation(25, 205);
 		frame.add(labelAssignment);
 		labelAssignmentsAverageGrade = new LabelExtended("<Undefined>", font);
 		labelAssignmentsAverageGrade.setBold(true);
 		labelAssignmentsAverageGrade.setSize(185, labelAssignmentsAverageGrade.getSize().height);
 		labelAssignmentsAverageGrade.setLocation(115, 205);
 		labelAssignmentsAverageGrade.setHorizontalAlignment(LabelExtended.ALIGN_RIGHT);
 		frame.add(labelAssignmentsAverageGrade);
 		
 		_comboBoxAssignments = new JComboBoxThemed<Assignment>();
 		frame.add(_comboBoxAssignments);
 		_comboBoxAssignments.setLocation(new Point(25, 235));
 		_comboBoxAssignments.setSize(new Dimension(275, 29));
 		_comboBoxAssignments.setFont(font);
 		_comboBoxAssignments.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				Assignment a = _comboBoxAssignments.getSelectedItem();
 				
 				if (a != null) {
 					labelAssignmentGradeValue.setText(a.getGrade());
 					
 					int avgGrade = gdb.getAssignmentGrade(a.getNumber());
 					labelAssignmentAverageGrade.setText(String.valueOf(avgGrade));
 				}
 			}
 		});
 		
 		LabelExtended labelAssignmentGrade = new LabelExtended("Student grade:", font);
 		labelAssignmentGrade.setLocation(55, 265);
 		frame.add(labelAssignmentGrade);
 		labelAssignmentGradeValue = new LabelExtended("<Undefined>", font);
 		labelAssignmentGradeValue.setBold(true);
 		labelAssignmentGradeValue.setSize(185, labelAssignmentGradeValue.getSize().height);
 		labelAssignmentGradeValue.setLocation(115, 265);
 		labelAssignmentGradeValue.setHorizontalAlignment(LabelExtended.ALIGN_RIGHT);
 		frame.add(labelAssignmentGradeValue);
 		
 		LabelExtended labelAssignmentAverage = new LabelExtended("Average class grade:", font);
 		labelAssignmentAverage.setLocation(55, 285);
 		frame.add(labelAssignmentAverage);
 		labelAssignmentAverageGrade = new LabelExtended("<Undefined>", font);
 		labelAssignmentAverageGrade.setBold(true);
 		labelAssignmentAverageGrade.setSize(185, labelAssignmentAverageGrade.getSize().height);
 		labelAssignmentAverageGrade.setLocation(115, 285);
 		labelAssignmentAverageGrade.setHorizontalAlignment(LabelExtended.ALIGN_RIGHT);
 		frame.add(labelAssignmentAverageGrade);
 		
 		
 		LabelExtended labelProject = new LabelExtended("Projects:", font);
 		labelProject.setLocation(25, 325);
 		frame.add(labelProject);
 		
 		_comboBoxProjects = new JComboBoxThemed<ProjectTeam>();
 		frame.add(_comboBoxProjects);
 		_comboBoxProjects.setLocation(new Point(25, 355));
 		_comboBoxProjects.setSize(new Dimension(275, 29));
 		_comboBoxProjects.setFont(font);
 		_comboBoxProjects.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				ProjectTeam p = _comboBoxProjects.getSelectedItem();
 				
 				if (p != null) {
 					if (!updateRightSide) {
 						updateRightSide = true;
 						return;
 					}
 					
 					_comboBoxProjectAverageGrade.removeAllItems();
 					HashMap<String, Integer> teamScores = p.getAllTeamScores();
 					Set<String> keys = teamScores.keySet();
 					
 					for (String score: keys) {
 						if (score.equals("TOTAL")) {
 							labelProjectAverageGrade.setText(String.valueOf(teamScores.get(score)));
 							continue;
 						}
 						int grade = teamScores.get(score);
 						_comboBoxProjectAverageGrade.addItem(((grade < 10) ? "0" : "") + grade + " for " + score);
 					}
 					
 					_comboBoxProjectContributionGrade.removeAllItems();
 					String student = _comboBoxStudent.getSelectedItem().getName();
 					ArrayList<Integer> contributionScores = p.getPeerScores(student);
 					ArrayList<String> peerNames = p.getTeamMembers();
 					
 					int i = 0;
 					for (String peer: peerNames) {
 						if (peer.equals(student)) continue;
 						
 						int grade = contributionScores.get(i);
 						_comboBoxProjectContributionGrade.addItem(((grade < 10) ? "0" : "") + contributionScores.get(i) + " from " + peer);
 						++i;
 					}
 					
 					_comboBoxProject.setSelectedIndex(p.getProjectNumber() - 1);
 					_comboBoxTeam.setSelectedIndex(Integer.parseInt(p.getTeamNumber()) - 1);
 				}
 			}
 		});
 		
 		LabelExtended labelProjectGrade = new LabelExtended("Team:", font);
 		labelProjectGrade.setLocation(55, 387);
 		frame.add(labelProjectGrade);
 		_comboBoxProjectAverageGrade = new JComboBoxThemed<String>(true);
 		frame.add(_comboBoxProjectAverageGrade);
 		_comboBoxProjectAverageGrade.setLocation(150, 390);
 		_comboBoxProjectAverageGrade.setSize(150, 25);
 		_comboBoxProjectAverageGrade.setFont(font);
 		labelProjectAverageGrade = new LabelExtended("<Undefined>", font);
 		labelProjectAverageGrade.setBold(true);
 		labelProjectAverageGrade.setSize(40, labelProjectAverageGrade.getSize().height);
 		labelProjectAverageGrade.setLocation(100, 387);
 		labelProjectAverageGrade.setHorizontalAlignment(LabelExtended.ALIGN_RIGHT);
 		frame.add(labelProjectAverageGrade);
 		
 		LabelExtended labelProjectContribution = new LabelExtended("Contribution:", font);
 		labelProjectContribution.setLocation(55, 415);
 		frame.add(labelProjectContribution);
 		_comboBoxProjectContributionGrade = new JComboBoxThemed<String>(true);
 		frame.add(_comboBoxProjectContributionGrade);
 		_comboBoxProjectContributionGrade.setLocation(150, 420);
 		_comboBoxProjectContributionGrade.setSize(150, 25);
 		_comboBoxProjectContributionGrade.setFont(font);
 		
 		DefaultListCellRenderer cellRenderer = new DefaultListCellRenderer() {
 		     @Override
 		     public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
 		         Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
 		         if (isSelected) {
 		             c.setBackground(Color.getHSBColor(0, 0, 0.88f));
		             c.setForeground(Color.BLACK);
 		         }
 		         return c;
 		     }
 		};
 		
 		LabelExtended labelProjectMembers = new LabelExtended("Team members:", font);
 		labelProjectMembers.setLocation(350, 88);
 		frame.add(labelProjectMembers);
 		membersModel = new DefaultListModel();
 		_listMembers = new JList();
 		_listMembers.setLocation(350, 115);
 		_listMembers.setSize(275, 71);
 		_listMembers.setFont(font);
 		_listMembers.setBackground(Color.getHSBColor(0, 0, 0.88f));
 		_listMembers.setBorder(BorderFactory.createLineBorder(Color.getHSBColor(0, 0, 0.75f)));
 		_listMembers.setModel(membersModel);
 		_listMembers.addFocusListener(new FocusListener() {
 			@Override
 			public void focusGained(FocusEvent e) {
 			}
 
 			@Override
 			public void focusLost(FocusEvent e) {
 				_listMembers.clearSelection();
 			}
 		});
 		JScrollPane acrossScrollBar = new JScrollPane(_listMembers);
 		frame.add(acrossScrollBar);
 		acrossScrollBar.setLocation(350, 115);
 		acrossScrollBar.setSize(275, 71);
 		_listMembers.addListSelectionListener(new ListSelectionListener() {
 			@Override
 			public void valueChanged(ListSelectionEvent e) {
 				if (_listMembers.getSelectedIndex() >= 0) {
 					updateRightSide = false;
 					_comboBoxStudent.setSelectedItem(gdb.getStudentByName(_listMembers.getSelectedValue()));
 				}
 			}
 		});
 		
 		LabelExtended labelProjectGrades = new LabelExtended("Team project grades:", font);
 		labelProjectGrades.setLocation(350, 202);
 		frame.add(labelProjectGrades);
 		gradesModel = new DefaultListModel();
 		_listGrades = new JList();
 		_listGrades.setLocation(350, 230);
 		_listGrades.setSize(275, 93);
 		_listGrades.setFont(font);
 		_listGrades.setBackground(Color.getHSBColor(0, 0, 0.88f));
 		_listGrades.setBorder(BorderFactory.createLineBorder(Color.getHSBColor(0, 0, 0.75f)));
 		_listGrades.setModel(gradesModel);
 		_listGrades.setFocusable(false);
 		_listGrades.setCellRenderer(cellRenderer);
 		acrossScrollBar = new JScrollPane(_listGrades);
 		frame.add(acrossScrollBar);
 		acrossScrollBar.setLocation(350, 230);
 		acrossScrollBar.setSize(275, 93);
 		LabelExtended labelProjectGradeTotal = new LabelExtended("Total:", font);
 		labelProjectGradeTotal.setLocation(495, 320);
 		frame.add(labelProjectGradeTotal);
 		labelProjectGradeTotalValue = new LabelExtended("<Undefined>", font);
 		labelProjectGradeTotalValue.setLocation(545, 320);
 		labelProjectGradeTotalValue.setSize(75, labelProjectGradeTotalValue.getHeight());
 		labelProjectGradeTotalValue.setHorizontalAlignment(LabelExtended.ALIGN_RIGHT);
 		labelProjectGradeTotalValue.setBold(true);
 		frame.add(labelProjectGradeTotalValue);
 		
 		
 		LabelExtended labelTeamContribution = new LabelExtended("Contribution grades:", font);
 		labelTeamContribution.setLocation(350, 340);
 		frame.add(labelTeamContribution);
 		_comboBoxTeamContributionMembers = new JComboBoxThemed<String>();
 		frame.add(_comboBoxTeamContributionMembers);
 		_comboBoxTeamContributionMembers.setLocation(350, 370);
 		_comboBoxTeamContributionMembers.setSize(275, 25);
 		_comboBoxTeamContributionMembers.setFont(font);
 		_comboBoxTeamContributionMembers.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				if (_comboBoxTeamContributionMembers.getSelectedIndex() >= 0) {
 					int projectNumber = _comboBoxProject.getSelectedIndex();
 					int selectedTeam = _comboBoxTeam.getSelectedIndex();
 					ProjectTeam p = projectsTeams.get(projectNumber).get(selectedTeam);
 					
 					String student = _comboBoxTeamContributionMembers.getSelectedItem();
 					ArrayList<Integer> contributionScores = p.getPeerScores(student);
 					ArrayList<String> peerNames = p.getTeamMembers();
 					
 					int i = 0;
 					contributionsModel.clear();
 					for (String peer: peerNames) {
 						if (peer.equals(student)) continue;
 						
 						int grade = contributionScores.get(i);
 						contributionsModel.addElement(((grade < 10) ? "0" : "") + contributionScores.get(i) + " from " + peer);
 						++i;
 					}
 				}
 			}
 		});
 		contributionsModel = new DefaultListModel();
 		_listContributions = new JList();
 		_listContributions.setLocation(350, 400);
 		_listContributions.setSize(275, 71);
 		_listContributions.setFont(font);
 		_listContributions.setBackground(Color.getHSBColor(0, 0, 0.88f));
 		_listContributions.setBorder(BorderFactory.createLineBorder(Color.getHSBColor(0, 0, 0.75f)));
 		_listContributions.setModel(contributionsModel);
 		_listContributions.setFocusable(false);
 		_listContributions.setCellRenderer(cellRenderer);
 		acrossScrollBar = new JScrollPane(_listContributions);
 		frame.add(acrossScrollBar);
 		acrossScrollBar.setLocation(350, 400);
 		acrossScrollBar.setSize(275, 71);
 		
 		
 		fillData();
 		frame.setVisible(true);
 	}
 	
 	private static void fillData() throws IOException, ServiceException {
 		Session sess = new Session();
 		sess.login(Constants.USERNAME, Constants.PASSWORD);
 		
 		gdb = sess.getDBByName(Constants.GRADES_DB);
 		HashSet<Student> stds = gdb.getStudents();
 		
 		projectsTeams = gdb.getProjectsTeams();
 		for (int i = 0; i < projectsTeams.size(); ++i) {
 			_comboBoxProject.addItem(String.valueOf(i + 1));
 		}
 		
 		for (Student std: stds) {
 			_comboBoxStudent.addItem(std);
 		}
 	}
 }
