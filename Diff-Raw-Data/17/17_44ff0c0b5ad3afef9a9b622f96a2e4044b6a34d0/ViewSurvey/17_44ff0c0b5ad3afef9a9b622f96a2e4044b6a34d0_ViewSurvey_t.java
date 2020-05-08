 package gui;
 
 import java.awt.Color;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.GridLayout;
 import java.awt.Insets;
 
 import javax.swing.border.Border;
 import javax.swing.BorderFactory;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 import ctrl.Controller;
 
 import obj.Group;
 import obj.Survey;
 import gui.custom.RoundPanel;
 import dao.JobsDAO;
 import obj.Job;
 
 public class ViewSurvey extends JPanel implements GUI {
 
 	private Group group = Controller.getInstance().getGroup();
 
 	public ViewSurvey(Survey survey) {
 
 		/*********************************
 		 * Configurations
 		 *********************************/
 		setBackground(FRAME_BACKGROUND);
 		setSize(SURVEY_WIDTH, SURVEY_HEIGHT);
 
 		/*********************************
 		 * Configure Components
 		 *********************************/
 		setBackground(FRAME_BACKGROUND);
 
 		/** Assemble Panel **/
 		GridBagLayout gridBagLayout = new GridBagLayout();
 		gridBagLayout.columnWidths = new int[] { 46, 0, 0, 0 };
 		gridBagLayout.rowHeights = new int[] { 16, 16, 0, 439, 0 };
 		gridBagLayout.columnWeights = new double[] { 0.0, 0.0, 0.0, Double.MIN_VALUE };
 		gridBagLayout.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0,
 				Double.MIN_VALUE };
 		setLayout(gridBagLayout);
 		JLabel lblSingle = new JLabel("Single");
 		
 		//Sub-panel borders
 		Border blue = BorderFactory.createLineBorder(BORDER_COLOR, 1);
 		
 		//Main panel
 		RoundPanel pnlMain = new RoundPanel();
 		pnlMain.setBackground(PANEL_BACKGROUND);
 				GridBagLayout gbl_pnlMain = new GridBagLayout();
 				gbl_pnlMain.columnWidths = new int[]{589, 0};
 				gbl_pnlMain.rowHeights = new int[]{70, 70, 70, 70, 70, 0, 0};
 				gbl_pnlMain.columnWeights = new double[]{0.0, Double.MIN_VALUE};
 				gbl_pnlMain.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
 				pnlMain.setLayout(gbl_pnlMain);
 		
 		
 		
 		
 						
 						//Sub-panels
 						JPanel pnlClassInfo = new JPanel();
 						//pnlClassInfo.setForeground(BORDER_COLOR);
 						pnlClassInfo.setBorder(BorderFactory.createTitledBorder( blue, "Class Information", 0, 0, BORDER_FONT,BORDER_COLOR));
 						GridBagLayout gbl_pnlClassInfo = new GridBagLayout();
 						gbl_pnlClassInfo.columnWidths = new int[]{103, 92, 53, 118, 73, 109, 0};
 						gbl_pnlClassInfo.rowHeights = new int[]{25, 0};
 						gbl_pnlClassInfo.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
 						gbl_pnlClassInfo.rowWeights = new double[]{0.0, Double.MIN_VALUE};
 						pnlClassInfo.setLayout(gbl_pnlClassInfo);
 						
 								// Labels
 								
 								//Class Info labels
 								JLabel lblGroup = new JLabel("RealityU Group: ");
 								
 								
 								//Assemble sub-panels
 								GridBagConstraints gbc_lblGroup = new GridBagConstraints();
 								gbc_lblGroup.fill = GridBagConstraints.VERTICAL;
 								gbc_lblGroup.insets = new Insets(0, 0, 0, 5);
 								gbc_lblGroup.gridx = 0;
 								gbc_lblGroup.gridy = 0;
 								pnlClassInfo.add(lblGroup, gbc_lblGroup);
 						JLabel lblGroupInfo = new JLabel(group.getName());
 						GridBagConstraints gbc_lblGroupInfo = new GridBagConstraints();
 						gbc_lblGroupInfo.fill = GridBagConstraints.BOTH;
 						gbc_lblGroupInfo.insets = new Insets(0, 0, 0, 5);
 						gbc_lblGroupInfo.gridx = 1;
 						gbc_lblGroupInfo.gridy = 0;
 						pnlClassInfo.add(lblGroupInfo, gbc_lblGroupInfo);
 						
 
 						JLabel lblTeacher = new JLabel("Teacher: ");
 						GridBagConstraints gbc_lblTeacher = new GridBagConstraints();
 						gbc_lblTeacher.fill = GridBagConstraints.BOTH;
 						gbc_lblTeacher.insets = new Insets(0, 0, 0, 5);
 						gbc_lblTeacher.gridx = 2;
 						gbc_lblTeacher.gridy = 0;
 						pnlClassInfo.add(lblTeacher, gbc_lblTeacher);
 						JLabel lblTeacherInfo = new JLabel(survey.getTeacher());
 						GridBagConstraints gbc_lblTeacherInfo = new GridBagConstraints();
 						gbc_lblTeacherInfo.fill = GridBagConstraints.BOTH;
 						gbc_lblTeacherInfo.insets = new Insets(0, 0, 0, 5);
 						gbc_lblTeacherInfo.gridx = 3;
 						gbc_lblTeacherInfo.gridy = 0;
 						pnlClassInfo.add(lblTeacherInfo, gbc_lblTeacherInfo);
 						
 
 						JLabel lblClassPeriod = new JLabel("Class Period: ");
 						GridBagConstraints gbc_lblClassPeriod = new GridBagConstraints();
 						gbc_lblClassPeriod.fill = GridBagConstraints.BOTH;
 						gbc_lblClassPeriod.insets = new Insets(0, 0, 0, 5);
 						gbc_lblClassPeriod.gridx = 4;
 						gbc_lblClassPeriod.gridy = 0;
 						pnlClassInfo.add(lblClassPeriod, gbc_lblClassPeriod);
 						JLabel lblDClassPeriod = new JLabel("" + survey.getCPeriod());
 						GridBagConstraints gbc_lblDClassPeriod = new GridBagConstraints();
 						gbc_lblDClassPeriod.fill = GridBagConstraints.BOTH;
 						gbc_lblDClassPeriod.gridx = 5;
 						gbc_lblDClassPeriod.gridy = 0;
 						pnlClassInfo.add(lblDClassPeriod, gbc_lblDClassPeriod);
 						
 						
 						
 						
 						//Assemble Main Panel
 						GridBagConstraints gbc_pnlClassInfo = new GridBagConstraints();
 						gbc_pnlClassInfo.fill = GridBagConstraints.BOTH;
 						gbc_pnlClassInfo.insets = new Insets(0, 0, 5, 0);
 						gbc_pnlClassInfo.gridx = 0;
 						gbc_pnlClassInfo.gridy = 0;
 						pnlMain.add(pnlClassInfo, gbc_pnlClassInfo);
 						JPanel pnlStudentInfo = new JPanel();
 						pnlStudentInfo.setBorder(BorderFactory.createTitledBorder(blue, "Student Information", 0, 0,  BORDER_FONT,BORDER_COLOR));
 						GridBagLayout gbl_pnlStudentInfo = new GridBagLayout();
 						gbl_pnlStudentInfo.columnWidths = new int[]{111, 86, 58, 55, 48, 66, 106, 47, 0, 0};
 						gbl_pnlStudentInfo.rowHeights = new int[]{25, 25, 0};
 						gbl_pnlStudentInfo.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
 						gbl_pnlStudentInfo.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
 						pnlStudentInfo.setLayout(gbl_pnlStudentInfo);
 								
 								
 								
 								//Student info labels
 								JLabel lblStuId = new JLabel("Student Identifier: ");
 								
 								GridBagConstraints gbc_lblStuId = new GridBagConstraints();
 								gbc_lblStuId.insets = new Insets(0, 0, 5, 5);
 								gbc_lblStuId.gridx = 0;
 								gbc_lblStuId.gridy = 0;
 								pnlStudentInfo.add(lblStuId, gbc_lblStuId);
 								JLabel lblStuIdinfo = new JLabel(Integer.toString(survey.getID()));
 								GridBagConstraints gbc_lblStuIdinfo = new GridBagConstraints();
 								gbc_lblStuIdinfo.anchor = GridBagConstraints.WEST;
 								gbc_lblStuIdinfo.insets = new Insets(0, 0, 5, 5);
 								gbc_lblStuIdinfo.gridx = 1;
 								gbc_lblStuIdinfo.gridy = 0;
 								pnlStudentInfo.add(lblStuIdinfo, gbc_lblStuIdinfo);
 						
 								JLabel lblFName = new JLabel("First Name: ");
 								GridBagConstraints gbc_lblFName = new GridBagConstraints();
 								gbc_lblFName.anchor = GridBagConstraints.WEST;
 								gbc_lblFName.insets = new Insets(0, 0, 5, 5);
 								gbc_lblFName.gridx = 2;
 								gbc_lblFName.gridy = 0;
 								pnlStudentInfo.add(lblFName, gbc_lblFName);
 						JLabel lblFNameInfo = new JLabel(survey.getFName());
 						GridBagConstraints gbc_lblFNameInfo = new GridBagConstraints();
 						gbc_lblFNameInfo.anchor = GridBagConstraints.WEST;
 						gbc_lblFNameInfo.insets = new Insets(0, 0, 5, 5);
 						gbc_lblFNameInfo.gridx = 3;
 						gbc_lblFNameInfo.gridy = 0;
 						pnlStudentInfo.add(lblFNameInfo, gbc_lblFNameInfo);
 						
 						JLabel lblLName = new JLabel("Last Name: ");
 						GridBagConstraints gbc_lblLName = new GridBagConstraints();
 						gbc_lblLName.anchor = GridBagConstraints.WEST;
 						gbc_lblLName.insets = new Insets(0, 0, 5, 5);
 						gbc_lblLName.gridx = 5;
 						gbc_lblLName.gridy = 0;
 						pnlStudentInfo.add(lblLName, gbc_lblLName);
 						JLabel lblLNameInfo = new JLabel(survey.getLName());
 						GridBagConstraints gbc_lblLNameInfo = new GridBagConstraints();
 						gbc_lblLNameInfo.anchor = GridBagConstraints.WEST;
 						gbc_lblLNameInfo.insets = new Insets(0, 0, 5, 5);
 						gbc_lblLNameInfo.gridx = 6;
 						gbc_lblLNameInfo.gridy = 0;
 						pnlStudentInfo.add(lblLNameInfo, gbc_lblLNameInfo);
 						
 						JLabel lblGPA = new JLabel("Current GPA: ");
 						GridBagConstraints gbc_lblGPA = new GridBagConstraints();
 						gbc_lblGPA.insets = new Insets(0, 0, 0, 5);
 						gbc_lblGPA.gridx = 0;
 						gbc_lblGPA.gridy = 1;
 						pnlStudentInfo.add(lblGPA, gbc_lblGPA);
 						GridBagConstraints gbc_pnlStudentInfo = new GridBagConstraints();
 						gbc_pnlStudentInfo.fill = GridBagConstraints.BOTH;
 						gbc_pnlStudentInfo.insets = new Insets(0, 0, 5, 0);
 						gbc_pnlStudentInfo.gridx = 0;
 						gbc_pnlStudentInfo.gridy = 1;
 						pnlMain.add(pnlStudentInfo, gbc_pnlStudentInfo);
 						JLabel lblGPAinfo = new JLabel("" + ARR_GPA[survey.getGPA()]);
 						GridBagConstraints gbc_lblGPAinfo = new GridBagConstraints();
 						gbc_lblGPAinfo.anchor = GridBagConstraints.WEST;
 						gbc_lblGPAinfo.insets = new Insets(0, 0, 0, 5);
 						gbc_lblGPAinfo.gridx = 1;
 						gbc_lblGPAinfo.gridy = 1;
 						pnlStudentInfo.add(lblGPAinfo, gbc_lblGPAinfo);
 		JPanel pnlFamilyInfo = new JPanel();
 		pnlFamilyInfo.setBorder(BorderFactory.createTitledBorder(blue, "Family Information", 0, 0,  BORDER_FONT,BORDER_COLOR));
 		GridBagLayout gbl_pnlFamilyInfo = new GridBagLayout();
 		gbl_pnlFamilyInfo.columnWidths = new int[]{94, 70, 36, 49, 77, 38, 0, 70, 37, 7, 0};
 		gbl_pnlFamilyInfo.rowHeights = new int[]{20, 0};
 		gbl_pnlFamilyInfo.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
 		gbl_pnlFamilyInfo.rowWeights = new double[]{0.0, Double.MIN_VALUE};
 		pnlFamilyInfo.setLayout(gbl_pnlFamilyInfo);
 		
 		//Family info Labels
 		JLabel lblMaritalStatus = new JLabel("MaritalStatus: ");
 		
 		GridBagConstraints gbc_lblMaritalStatus = new GridBagConstraints();
 		gbc_lblMaritalStatus.insets = new Insets(0, 0, 0, 5);
 		gbc_lblMaritalStatus.gridx = 0;
 		gbc_lblMaritalStatus.gridy = 0;
 		pnlFamilyInfo.add(lblMaritalStatus, gbc_lblMaritalStatus);
 		
 		JLabel lblMarried = new JLabel("Married");
		//TODO:
		//Comment out this if stmt if you need to edit in Window Builder
		if (survey.getMarried()==(1)) {
			pnlFamilyInfo.add(lblSingle);
			
		}
 		
		else {
 			GridBagConstraints gbc_lblMarried = new GridBagConstraints();
 			gbc_lblMarried.anchor = GridBagConstraints.WEST;
 			gbc_lblMarried.insets = new Insets(0, 0, 0, 5);
 			gbc_lblMarried.gridx = 1;
 			gbc_lblMarried.gridy = 0;
 			pnlFamilyInfo.add(lblMarried, gbc_lblMarried);
		}
 		JLabel lblSpouse = new JLabel("Spouse: ");
 		GridBagConstraints gbc_lblSpouse = new GridBagConstraints();
 		gbc_lblSpouse.anchor = GridBagConstraints.WEST;
 		gbc_lblSpouse.insets = new Insets(0, 0, 0, 5);
 		gbc_lblSpouse.gridx = 3;
 		gbc_lblSpouse.gridy = 0;
 		pnlFamilyInfo.add(lblSpouse, gbc_lblSpouse);
 		JLabel lblSpouseInfo = new JLabel("toBeDetermined");
 		GridBagConstraints gbc_lblSpouseInfo = new GridBagConstraints();
 		gbc_lblSpouseInfo.anchor = GridBagConstraints.WEST;
 		gbc_lblSpouseInfo.insets = new Insets(0, 0, 0, 5);
 		gbc_lblSpouseInfo.gridx = 4;
 		gbc_lblSpouseInfo.gridy = 0;
 		pnlFamilyInfo.add(lblSpouseInfo, gbc_lblSpouseInfo);
 		
 		JLabel lblChildren = new JLabel("No. of Children: ");
 		GridBagConstraints gbc_lblChildren = new GridBagConstraints();
 		gbc_lblChildren.anchor = GridBagConstraints.WEST;
 		gbc_lblChildren.insets = new Insets(0, 0, 0, 5);
 		gbc_lblChildren.gridx = 6;
 		gbc_lblChildren.gridy = 0;
 		pnlFamilyInfo.add(lblChildren, gbc_lblChildren);
 			GridBagConstraints gbc_pnlFamilyInfo = new GridBagConstraints();
 			gbc_pnlFamilyInfo.fill = GridBagConstraints.BOTH;
 			gbc_pnlFamilyInfo.insets = new Insets(0, 0, 5, 0);
 			gbc_pnlFamilyInfo.gridx = 0;
 			gbc_pnlFamilyInfo.gridy = 2;
 			pnlMain.add(pnlFamilyInfo, gbc_pnlFamilyInfo);
 			//probably yes/no not #
 			JLabel lblChildrenInfo = new JLabel(Integer.toString(survey.getChildren()));
 			GridBagConstraints gbc_lblChildrenInfo = new GridBagConstraints();
 			gbc_lblChildrenInfo.anchor = GridBagConstraints.WEST;
 			gbc_lblChildrenInfo.insets = new Insets(0, 0, 0, 5);
 			gbc_lblChildrenInfo.gridx = 7;
 			gbc_lblChildrenInfo.gridy = 0;
 			pnlFamilyInfo.add(lblChildrenInfo, gbc_lblChildrenInfo);
 				JPanel pnlOccupation = new JPanel();
 				pnlOccupation.setBorder(BorderFactory.createTitledBorder(blue, "Occupation and Income", 0, 0,  BORDER_FONT,BORDER_COLOR));
 				
 					
 					GridBagLayout gbl_pnlOccupation = new GridBagLayout();
 					gbl_pnlOccupation.columnWidths = new int[]{579, 0};
 					gbl_pnlOccupation.rowHeights = new int[]{24, 24, 0};
 					gbl_pnlOccupation.columnWeights = new double[]{0.0, Double.MIN_VALUE};
 					gbl_pnlOccupation.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
 					pnlOccupation.setLayout(gbl_pnlOccupation);
 					JPanel pnlOccupationA = new JPanel();
 					
 						GridBagLayout gbl_pnlOccupationA = new GridBagLayout();
 						gbl_pnlOccupationA.columnWidths = new int[]{94, 106, 40, 89, 93, 64, 20, 0};
 						gbl_pnlOccupationA.rowHeights = new int[]{25, 25, 0};
 						gbl_pnlOccupationA.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
 						gbl_pnlOccupationA.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
 						pnlOccupationA.setLayout(gbl_pnlOccupationA);
 						
 						
 						//Occupation and income Labels
 						//TODO: job needs to be translated from # to job text	
 						JLabel lblOccupation = new JLabel("Occupation: ");
 						GridBagConstraints gbc_lblOccupation = new GridBagConstraints();
 							gbc_lblOccupation.insets = new Insets(0, 0, 5, 5);
 							gbc_lblOccupation.gridx = 0;
 							gbc_lblOccupation.gridy = 0;
 							pnlOccupationA.add(lblOccupation, gbc_lblOccupation);
 						JLabel lblOccupationInfo = new JLabel(Integer.toString(survey.getJob()));
 						GridBagConstraints gbc_lblOccupationInfo = new GridBagConstraints();
 						gbc_lblOccupationInfo.anchor = GridBagConstraints.WEST;
 						gbc_lblOccupationInfo.insets = new Insets(0, 0, 5, 5);
 						gbc_lblOccupationInfo.gridx = 1;
 						gbc_lblOccupationInfo.gridy = 0;
 						pnlOccupationA.add(lblOccupationInfo, gbc_lblOccupationInfo);
 						
 						//TODO: salary info needs to pull from jobs table
 						JLabel lblAnnualSalary = new JLabel("Annual Salary: ");
 						GridBagConstraints gbc_lblAnnualSalary = new GridBagConstraints();
 						gbc_lblAnnualSalary.anchor = GridBagConstraints.WEST;
 						gbc_lblAnnualSalary.insets = new Insets(0, 0, 5, 5);
 						gbc_lblAnnualSalary.gridx = 2;
 						gbc_lblAnnualSalary.gridy = 0;
 						pnlOccupationA.add(lblAnnualSalary, gbc_lblAnnualSalary);
 						JLabel lblAnnualSalaryInfo = new JLabel("00,000");//Double.toString(Job.getAnnGrossSal()));
 						GridBagConstraints gbc_lblAnnualSalaryInfo = new GridBagConstraints();
 						gbc_lblAnnualSalaryInfo.anchor = GridBagConstraints.WEST;
 						gbc_lblAnnualSalaryInfo.insets = new Insets(0, 0, 5, 5);
 						gbc_lblAnnualSalaryInfo.gridx = 3;
 						gbc_lblAnnualSalaryInfo.gridy = 0;
 						pnlOccupationA.add(lblAnnualSalaryInfo, gbc_lblAnnualSalaryInfo);
 						
 						JLabel lblMonthlySalary = new JLabel("Monthly Salary: ");
 						GridBagConstraints gbc_lblMonthlySalary = new GridBagConstraints();
 						gbc_lblMonthlySalary.anchor = GridBagConstraints.WEST;
 						gbc_lblMonthlySalary.insets = new Insets(0, 0, 5, 5);
 						gbc_lblMonthlySalary.gridx = 4;
 						gbc_lblMonthlySalary.gridy = 0;
 						pnlOccupationA.add(lblMonthlySalary, gbc_lblMonthlySalary);
 						JLabel lblMonthlySalaryInfo = new JLabel("$00,000");
 						GridBagConstraints gbc_lblMonthlySalaryInfo = new GridBagConstraints();
 						gbc_lblMonthlySalaryInfo.anchor = GridBagConstraints.WEST;
 						gbc_lblMonthlySalaryInfo.insets = new Insets(0, 0, 5, 5);
 						gbc_lblMonthlySalaryInfo.gridx = 5;
 						gbc_lblMonthlySalaryInfo.gridy = 0;
 						pnlOccupationA.add(lblMonthlySalaryInfo, gbc_lblMonthlySalaryInfo);
 						
 						//TODO: will need to provide for married or single taxes to pull from db
 						JLabel lblAnnualTaxes = new JLabel("Annual Taxes : ");
 						GridBagConstraints gbc_lblAnnualTaxes = new GridBagConstraints();
 						gbc_lblAnnualTaxes.anchor = GridBagConstraints.WEST;
 						gbc_lblAnnualTaxes.insets = new Insets(0, 0, 0, 5);
 						gbc_lblAnnualTaxes.gridx = 2;
 						gbc_lblAnnualTaxes.gridy = 1;
 						pnlOccupationA.add(lblAnnualTaxes, gbc_lblAnnualTaxes);
 						JLabel lblAnnualTaxesInfo = new JLabel("$00,000");
 						GridBagConstraints gbc_lblAnnualTaxesInfo = new GridBagConstraints();
 						gbc_lblAnnualTaxesInfo.anchor = GridBagConstraints.WEST;
 						gbc_lblAnnualTaxesInfo.insets = new Insets(0, 0, 0, 5);
 						gbc_lblAnnualTaxesInfo.gridx = 3;
 						gbc_lblAnnualTaxesInfo.gridy = 1;
 						pnlOccupationA.add(lblAnnualTaxesInfo, gbc_lblAnnualTaxesInfo);
 						
 						JLabel lblMonthlyTaxes = new JLabel("Monthly Taxes: ");
 						GridBagConstraints gbc_lblMonthlyTaxes = new GridBagConstraints();
 						gbc_lblMonthlyTaxes.anchor = GridBagConstraints.WEST;
 						gbc_lblMonthlyTaxes.insets = new Insets(0, 0, 0, 5);
 						gbc_lblMonthlyTaxes.gridx = 4;
 						gbc_lblMonthlyTaxes.gridy = 1;
 						pnlOccupationA.add(lblMonthlyTaxes, gbc_lblMonthlyTaxes);
 						
 						GridBagConstraints gbc_pnlOccupationA = new GridBagConstraints();
 						gbc_pnlOccupationA.fill = GridBagConstraints.BOTH;
 						gbc_pnlOccupationA.insets = new Insets(0, 0, 5, 0);
 						gbc_pnlOccupationA.gridx = 0;
 						gbc_pnlOccupationA.gridy = 0;
 						pnlOccupation.add(pnlOccupationA, gbc_pnlOccupationA);
 						JLabel lblMonthlyTaxesInfo = new JLabel("$0,000");
 						GridBagConstraints gbc_lblMonthlyTaxesInfo = new GridBagConstraints();
 						gbc_lblMonthlyTaxesInfo.insets = new Insets(0, 0, 0, 5);
 						gbc_lblMonthlyTaxesInfo.anchor = GridBagConstraints.WEST;
 						gbc_lblMonthlyTaxesInfo.gridx = 5;
 						gbc_lblMonthlyTaxesInfo.gridy = 1;
 						pnlOccupationA.add(lblMonthlyTaxesInfo, gbc_lblMonthlyTaxesInfo);
 					JPanel pnlOccupationB = new JPanel();
 					GridBagLayout gbl_pnlOccupationB = new GridBagLayout();
 					gbl_pnlOccupationB.columnWidths = new int[]{56, 71, 60, 104, 86, 95, 138, 0};
 					gbl_pnlOccupationB.rowHeights = new int[]{25, 25, 25, 25, 0};
 					gbl_pnlOccupationB.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
 					gbl_pnlOccupationB.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
 					pnlOccupationB.setLayout(gbl_pnlOccupationB);
 					
 					JLabel lblChildSupport = new JLabel("Child Support: ");
 					
 					GridBagConstraints gbc_lblChildSupport = new GridBagConstraints();
 					gbc_lblChildSupport.anchor = GridBagConstraints.NORTHWEST;
 					gbc_lblChildSupport.insets = new Insets(0, 0, 5, 5);
 					gbc_lblChildSupport.gridx = 5;
 					gbc_lblChildSupport.gridy = 0;
 					pnlOccupationB.add(lblChildSupport, gbc_lblChildSupport);
 					JLabel lblChildSupportInfo = new JLabel("$000 ");
 					GridBagConstraints gbc_lblChildSupportInfo = new GridBagConstraints();
 					gbc_lblChildSupportInfo.insets = new Insets(0, 0, 5, 0);
 					gbc_lblChildSupportInfo.anchor = GridBagConstraints.NORTHWEST;
 					gbc_lblChildSupportInfo.gridx = 6;
 					gbc_lblChildSupportInfo.gridy = 0;
 					pnlOccupationB.add(lblChildSupportInfo, gbc_lblChildSupportInfo);
 					
 					JLabel lblNetIncome = new JLabel("Net income: ");
 					GridBagConstraints gbc_lblNetIncome = new GridBagConstraints();
 					gbc_lblNetIncome.anchor = GridBagConstraints.NORTHWEST;
 					gbc_lblNetIncome.insets = new Insets(0, 0, 5, 5);
 					gbc_lblNetIncome.gridx = 5;
 					gbc_lblNetIncome.gridy = 1;
 					pnlOccupationB.add(lblNetIncome, gbc_lblNetIncome);
 					JLabel lblNetIncomeInfo = new JLabel("$00,000");
 					GridBagConstraints gbc_lblNetIncomeInfo = new GridBagConstraints();
 					gbc_lblNetIncomeInfo.insets = new Insets(0, 0, 5, 0);
 					gbc_lblNetIncomeInfo.anchor = GridBagConstraints.NORTHWEST;
 					gbc_lblNetIncomeInfo.gridx = 6;
 					gbc_lblNetIncomeInfo.gridy = 1;
 					pnlOccupationB.add(lblNetIncomeInfo, gbc_lblNetIncomeInfo);
 					
 					JLabel lblSpousalIncome = new JLabel("Spousal Income: ");
 					GridBagConstraints gbc_lblSpousalIncome = new GridBagConstraints();
 					gbc_lblSpousalIncome.anchor = GridBagConstraints.NORTHWEST;
 					gbc_lblSpousalIncome.insets = new Insets(0, 0, 5, 5);
 					gbc_lblSpousalIncome.gridx = 5;
 					gbc_lblSpousalIncome.gridy = 2;
 					pnlOccupationB.add(lblSpousalIncome, gbc_lblSpousalIncome);
 					JLabel lblSpousalIncomeInfo = new JLabel("$0,000");
 					GridBagConstraints gbc_lblSpousalIncomeInfo = new GridBagConstraints();
 					gbc_lblSpousalIncomeInfo.insets = new Insets(0, 0, 5, 0);
 					gbc_lblSpousalIncomeInfo.anchor = GridBagConstraints.NORTHWEST;
 					gbc_lblSpousalIncomeInfo.gridx = 6;
 					gbc_lblSpousalIncomeInfo.gridy = 2;
 					pnlOccupationB.add(lblSpousalIncomeInfo, gbc_lblSpousalIncomeInfo);
 					
 					JLabel lblCheckbookEntry = new JLabel("Checkbook Entry: ");
 					GridBagConstraints gbc_lblCheckbookEntry = new GridBagConstraints();
 					gbc_lblCheckbookEntry.anchor = GridBagConstraints.NORTHWEST;
 					gbc_lblCheckbookEntry.insets = new Insets(0, 0, 0, 5);
 					gbc_lblCheckbookEntry.gridx = 5;
 					gbc_lblCheckbookEntry.gridy = 3;
 					pnlOccupationB.add(lblCheckbookEntry, gbc_lblCheckbookEntry);
 					JLabel lblCheckbookEntryInfo = new JLabel("$00,000");
 					GridBagConstraints gbc_lblCheckbookEntryInfo = new GridBagConstraints();
 					gbc_lblCheckbookEntryInfo.anchor = GridBagConstraints.NORTHWEST;
 					gbc_lblCheckbookEntryInfo.gridx = 6;
 					gbc_lblCheckbookEntryInfo.gridy = 3;
 					pnlOccupationB.add(lblCheckbookEntryInfo, gbc_lblCheckbookEntryInfo);
 					GridBagConstraints gbc_pnlOccupationB = new GridBagConstraints();
 					gbc_pnlOccupationB.anchor = GridBagConstraints.EAST;
 					gbc_pnlOccupationB.fill = GridBagConstraints.VERTICAL;
 					gbc_pnlOccupationB.gridx = 0;
 					gbc_pnlOccupationB.gridy = 1;
 					pnlOccupation.add(pnlOccupationB, gbc_pnlOccupationB);
 					GridBagConstraints gbc_pnlOccupation = new GridBagConstraints();
 					gbc_pnlOccupation.fill = GridBagConstraints.BOTH;
 					gbc_pnlOccupation.insets = new Insets(0, 0, 5, 0);
 					gbc_pnlOccupation.gridx = 0;
 					gbc_pnlOccupation.gridy = 3;
 					pnlMain.add(pnlOccupation, gbc_pnlOccupation);
 				
 				GridBagConstraints gbc_pnlMain = new GridBagConstraints();
 				gbc_pnlMain.gridheight = 2;
 				gbc_pnlMain.insets = new Insets(0, 0, 0, 5);
 				gbc_pnlMain.gridx = 1;
 				gbc_pnlMain.gridy = 2;
 				this.add(pnlMain, gbc_pnlMain);
 				JPanel pnlFinConsid = new JPanel();
 				pnlFinConsid.setBorder(BorderFactory.createTitledBorder(blue, "Financial Considerations", 0, 0,  BORDER_FONT,BORDER_COLOR));
 				GridBagLayout gbl_pnlFinConsid = new GridBagLayout();
 				gbl_pnlFinConsid.columnWidths = new int[]{97, 73, 24, 66, 65, 0};
 				gbl_pnlFinConsid.rowHeights = new int[]{25, 0};
 				gbl_pnlFinConsid.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
 				gbl_pnlFinConsid.rowWeights = new double[]{0.0, Double.MIN_VALUE};
 				pnlFinConsid.setLayout(gbl_pnlFinConsid);
 				
 				JLabel lblCollegeLoans = new JLabel("College Loans: ");
 				
 				GridBagConstraints gbc_lblCollegeLoans = new GridBagConstraints();
 				gbc_lblCollegeLoans.insets = new Insets(0, 0, 0, 5);
 				gbc_lblCollegeLoans.gridx = 0;
 				gbc_lblCollegeLoans.gridy = 0;
 				pnlFinConsid.add(lblCollegeLoans, gbc_lblCollegeLoans);
 				JLabel lblCollegeLoansInfo = new JLabel("$0,000");
 				GridBagConstraints gbc_lblCollegeLoansInfo = new GridBagConstraints();
 				gbc_lblCollegeLoansInfo.anchor = GridBagConstraints.WEST;
 				gbc_lblCollegeLoansInfo.insets = new Insets(0, 0, 0, 5);
 				gbc_lblCollegeLoansInfo.gridx = 1;
 				gbc_lblCollegeLoansInfo.gridy = 0;
 				pnlFinConsid.add(lblCollegeLoansInfo, gbc_lblCollegeLoansInfo);
 				
 				//TODO: get credit score from processed survey
 				JLabel lblCreditScore = new JLabel("Credit Score: ");
 				GridBagConstraints gbc_lblCreditScore = new GridBagConstraints();
 				gbc_lblCreditScore.anchor = GridBagConstraints.WEST;
 				gbc_lblCreditScore.insets = new Insets(0, 0, 0, 5);
 				gbc_lblCreditScore.gridx = 3;
 				gbc_lblCreditScore.gridy = 0;
 				pnlFinConsid.add(lblCreditScore, gbc_lblCreditScore);
 				JLabel lblCreditScoreInfo = new JLabel("GPA based");
 				GridBagConstraints gbc_lblCreditScoreInfo = new GridBagConstraints();
 				gbc_lblCreditScoreInfo.gridx = 4;
 				gbc_lblCreditScoreInfo.gridy = 0;
 				pnlFinConsid.add(lblCreditScoreInfo, gbc_lblCreditScoreInfo);
 				GridBagConstraints gbc_pnlFinConsid = new GridBagConstraints();
 				gbc_pnlFinConsid.insets = new Insets(0, 0, 5, 0);
 				gbc_pnlFinConsid.fill = GridBagConstraints.BOTH;
 				gbc_pnlFinConsid.gridx = 0;
 				gbc_pnlFinConsid.gridy = 4;
 				pnlMain.add(pnlFinConsid, gbc_pnlFinConsid);
 				
 		//Bottom/button panel
 		
 		JPanel pnlButtons = new JPanel();
 		pnlButtons.setBackground(PANEL_BACKGROUND);
 		GridBagLayout gbl_pnlButtons = new GridBagLayout();
 		gbl_pnlButtons.columnWidths = new int[]{314, 67, 50, 0, 46, 63, 0};
 		gbl_pnlButtons.rowHeights = new int[]{23, 0};
 		gbl_pnlButtons.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
 		gbl_pnlButtons.rowWeights = new double[]{0.0, Double.MIN_VALUE};
 		pnlButtons.setLayout(gbl_pnlButtons);
 		GridBagConstraints gbc_pnlButtons = new GridBagConstraints();
 		gbc_pnlButtons.gridx = 0;
 		gbc_pnlButtons.gridy = 5;
 		pnlMain.add(pnlButtons, gbc_pnlButtons);
 		JButton btnPrintPrev = new JButton("Print Preview");
 		GridBagConstraints gbc_btnPrintPrev = new GridBagConstraints();
 		gbc_btnPrintPrev.anchor = GridBagConstraints.NORTHWEST;
 		gbc_btnPrintPrev.insets = new Insets(0, 0, 0, 5);
 		gbc_btnPrintPrev.gridx = 4;
 		gbc_btnPrintPrev.gridy = 0;
 		pnlButtons.add(btnPrintPrev, gbc_btnPrintPrev);
 		JButton btnClose = new JButton("Close");
 		GridBagConstraints gbc_btnClose = new GridBagConstraints();
 		gbc_btnClose.anchor = GridBagConstraints.NORTHEAST;
 		gbc_btnClose.gridx = 5;
 		gbc_btnClose.gridy = 0;
 		pnlButtons.add(btnClose, gbc_btnClose);
 		
 	
 		
 		
 		
 		
 		
 	}
 
 }
