 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.*;
 import java.*;
 import javax.swing.event.*;
 
 public class Menu extends JFrame
 	implements ActionListener
 {
 	public static int WIDTH = 600;
 	public static int HEIGHT = 400;
 	private int x;
 	private int y;
 	public static jaklUtilities utility;
 	private final User curr;
 	private final Class currClass;
 
 	// Used for changed Password
 	private JLabel aErrorLbl;
 	private JPasswordField cPassTxt;
 	private JPasswordField nPassTxt;
 
 
 	// Used for create Accounts
 	private JTextField idTxt;
 	private JTextField nameTxt;
 	private JComboBox userList;
 	private JTextField passTxt;
 	private JLabel createAErrorLbl;
 
 	// Used for create Classes
 	private JTextField courseTxt;
 	private JTextField titleTxt;
 	private JTextArea descTxt;
 	private JLabel teacherLbl;
 	private JTextField teacherTxt;
 	private JLabel createCErrorLbl;
 
 	// Used for create Quiz
 	private JTextField qTitleTxt;
 	private JComboBox numList;
 
 	public Menu(User _curr)
 	{
		super("Just In Time Teaching");
 		currClass = null;
 		curr = _curr;
 		utility = new jaklUtilities();
 
 		try
 		{
 			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 		}
 
 		setSize(WIDTH, HEIGHT);
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		setResizable(false);
 		setLocationRelativeTo(null);
 
 		final JTabbedPane jTab = new JTabbedPane();
 		getContentPane().add(jTab);
 
 		jTab.addChangeListener(new ChangeListener() {
 		public void stateChanged(ChangeEvent e) {
 			//tabChanged();
 		}});
 
 		JPanel hPanel = home("Main Menu");
 		JPanel cPanel = view("Class Menu");
 		JPanel aPanel = account();
 
 
 		jTab.add("Home", hPanel);
 		jTab.add("Class View", cPanel);
 		jTab.add("Account View", aPanel);
 
 		if (curr.getStatus() == 'a')
 		{
 			JPanel cAccountPanel = create("Create Account");
 			JPanel cClassPanel = create("Create Class");
 			jTab.add("Create Account", cAccountPanel);
 			jTab.add("Create Class", cClassPanel);
 		}
 
 		jTab.addChangeListener(new ChangeListener() {
 		public void stateChanged(ChangeEvent e) {
 			tabChanged();
 		}});
 	}
 
 	public Menu(Class _currClass, User _curr)
 	{
		super("Just In Time Teaching");
 		currClass = _currClass;
 		curr = _curr;
 
 		try
 		{
 			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 		}
 
 		setSize(WIDTH, HEIGHT);
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		setLocationRelativeTo(null);
 
 		JTabbedPane jTab = new JTabbedPane();
 		getContentPane().add(jTab);
 
 
 		JPanel hPanel = home(currClass.getTitle());
 		JPanel qPanel = view("Quiz Menu");
 		JPanel gPanel = grades();
 		//JScrollPane gPane = grades();
 
 		jTab.add("Home", hPanel);
 		jTab.add("Quiz View", qPanel);
 
 
 		if (curr.getStatus() != 's')
 		{
 			JPanel cQuizPanel = create("Create Quiz");
 			jTab.add("Create Quiz", cQuizPanel);
 		}
 
 		jTab.add("Grade View", gPanel);
 
 		jTab.addChangeListener(new ChangeListener() {
 		public void stateChanged(ChangeEvent e) {
 			tabChanged();
 		}});
 
 
 	}
 
 	public JPanel create(String title)
 	{
 		JPanel createPanel = new JPanel();
 		SpringLayout layout = new SpringLayout();
 		createPanel.setLayout(layout);
 
 		x = 25;
 		y = 25;
 		int inc = 45;
 
 		JLabel createLbl = new JLabel(title);
 		createPanel.add(createLbl);
 
 
 		layout.putConstraint(SpringLayout.WEST, createLbl, x, SpringLayout.WEST,
 			createPanel);
 		layout.putConstraint(SpringLayout.NORTH, createLbl, y, SpringLayout.NORTH,
 			createPanel);
 
 		y += inc;
 
 
 		if (title.equals("Create Class"))
 		{
 			JLabel courseLbl = new JLabel("Course ID:");
 			createPanel.add(courseLbl);
 
 			courseTxt = new JTextField("", 10);
 			createPanel.add(courseTxt);
 
 			JLabel titleLbl = new JLabel("Course Title:");
 			createPanel.add(titleLbl);
 
 			titleTxt = new JTextField("", 10);
 			createPanel.add(titleTxt);
 
 			JLabel descLbl = new JLabel("Course Description:");
 			createPanel.add(descLbl);
 
 			descTxt = new JTextArea(5, 20);
 
 			descTxt.setWrapStyleWord(true);
 			descTxt.setBorder(BorderFactory.createLineBorder(Color.BLACK));
 			JScrollPane pane = new JScrollPane(descTxt, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                        JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
 			pane.setPreferredSize(new Dimension(200, 50));
 			createPanel.add(pane);
 
 			teacherLbl = new JLabel("Enter a Teacher ID:");
 			createPanel.add(teacherLbl);
 
 			teacherTxt = new JTextField("", 10);
 			createPanel.add(teacherTxt);
 
 			JButton createBtn = new JButton("Create Class");
 			createBtn.addActionListener(this);
 			createPanel.add(createBtn);
 
 			JButton logoutBtn = new JButton("Logout");
 			logoutBtn.addActionListener(this);
 			createPanel.add(logoutBtn);
 ;
 
 			createCErrorLbl = new JLabel("");
 			createPanel.add(createCErrorLbl);
 
 			layout.putConstraint(SpringLayout.WEST, courseLbl, (((WIDTH/2)/2)-50), SpringLayout.WEST,
 				createPanel);
 			layout.putConstraint(SpringLayout.NORTH, courseLbl, y, SpringLayout.NORTH,
 				createPanel);
 			layout.putConstraint(SpringLayout.NORTH, courseTxt, y, SpringLayout.NORTH,
 				createPanel);
 			layout.putConstraint(SpringLayout.WEST, courseTxt, 20, SpringLayout.EAST,
 				courseLbl);
 
 			y += inc;
 
 			layout.putConstraint(SpringLayout.WEST, titleLbl, (((WIDTH/2)/2)-50), SpringLayout.WEST,
 				createPanel);
 			layout.putConstraint(SpringLayout.NORTH, titleLbl, y, SpringLayout.NORTH,
 				createPanel);
 			layout.putConstraint(SpringLayout.NORTH, titleTxt, y, SpringLayout.NORTH,
 				createPanel);
 			layout.putConstraint(SpringLayout.WEST, titleTxt, 20, SpringLayout.EAST,
 				titleLbl);
 
 			y += inc;
 
 			layout.putConstraint(SpringLayout.WEST, descLbl, (((WIDTH/2)/2)-50), SpringLayout.WEST,
 				createPanel);
 			layout.putConstraint(SpringLayout.NORTH, descLbl, y, SpringLayout.NORTH,
 				createPanel);
 			layout.putConstraint(SpringLayout.NORTH, pane, y, SpringLayout.NORTH,
 				createPanel);
 			layout.putConstraint(SpringLayout.WEST, pane, 20, SpringLayout.EAST,
 				descLbl);
 
 			y += inc + 25;
 
 			layout.putConstraint(SpringLayout.WEST, teacherLbl, (((WIDTH/2)/2)-50), SpringLayout.WEST,
 				createPanel);
 			layout.putConstraint(SpringLayout.NORTH, teacherLbl, y, SpringLayout.NORTH,
 				createPanel);
 			layout.putConstraint(SpringLayout.NORTH, teacherTxt, y, SpringLayout.NORTH,
 				createPanel);
 			layout.putConstraint(SpringLayout.WEST, teacherTxt, 20, SpringLayout.EAST,
 				teacherLbl);
 
 			y += inc;
 
 			layout.putConstraint(SpringLayout.NORTH ,createBtn, (HEIGHT-110), SpringLayout.NORTH,
 				createPanel);
 			layout.putConstraint(SpringLayout.EAST, createBtn, -5, SpringLayout.WEST,
 				logoutBtn);
 
 			layout.putConstraint(SpringLayout.WEST, createCErrorLbl, ((WIDTH/2)-50), SpringLayout.WEST,
 				createPanel);
 			layout.putConstraint(SpringLayout.NORTH, createCErrorLbl, y, SpringLayout.NORTH,
 				createPanel);
 
 			layout.putConstraint(SpringLayout.WEST, logoutBtn, (WIDTH-90), SpringLayout.WEST,
 				createPanel);
 			layout.putConstraint(SpringLayout.NORTH, logoutBtn, (HEIGHT-110), SpringLayout.NORTH,
 				createPanel);
 
 		}
 		else if (title.equals("Create Account"))
 		{
 
 			JLabel idLbl = new JLabel("ID:");
 			createPanel.add(idLbl);
 
 			idTxt = new JTextField("", 10);
 			createPanel.add(idTxt);
 
 			JLabel nameLbl = new JLabel("Name:");
 			createPanel.add(nameLbl);
 
 			nameTxt = new JTextField("", 10);
 			createPanel.add(nameTxt);
 
 			String[] users = { "Admin", "Teacher", "Student"};
 
 			userList = new JComboBox(users);
 			userList.setSelectedIndex(0);
 			userList.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent ev)
 				{
 					comboBoxActionPerformed(ev);
 				}
 			});
 
 			createPanel.add(userList);
 
 			JLabel passLbl = new JLabel("Password:");
 			createPanel.add(passLbl);
 
 			passTxt = new JTextField("", 10);
 			createPanel.add(passTxt);
 
 			createAErrorLbl = new JLabel("");
 			createPanel.add(createAErrorLbl);
 
 			JButton createBtn = new JButton("Create Account");
 			createBtn.addActionListener(this);
 			createPanel.add(createBtn);
 
 			JButton logoutBtn = new JButton("Logout");
 			logoutBtn.addActionListener(this);
 			createPanel.add(logoutBtn);
 
 			layout.putConstraint(SpringLayout.WEST, idLbl, (((WIDTH/2)/2)-50), SpringLayout.WEST,
 				createPanel);
 			layout.putConstraint(SpringLayout.NORTH, idLbl, y, SpringLayout.NORTH,
 				createPanel);
 			layout.putConstraint(SpringLayout.NORTH, idTxt, y, SpringLayout.NORTH,
 				createPanel);
 			layout.putConstraint(SpringLayout.WEST, idTxt, 20, SpringLayout.EAST,
 				nameLbl);
 			layout.putConstraint(SpringLayout.NORTH, userList, y, SpringLayout.NORTH,
 				createPanel);
 			layout.putConstraint(SpringLayout.WEST, userList, 20, SpringLayout.EAST,
 				idTxt);
 
 			y += inc;
 
 			layout.putConstraint(SpringLayout.WEST, nameLbl, (((WIDTH/2)/2)-50), SpringLayout.WEST,
 				createPanel);
 			layout.putConstraint(SpringLayout.NORTH, nameLbl, y, SpringLayout.NORTH,
 				createPanel);
 			layout.putConstraint(SpringLayout.NORTH, nameTxt, y, SpringLayout.NORTH,
 				createPanel);
 			layout.putConstraint(SpringLayout.WEST, nameTxt, 20, SpringLayout.EAST,
 				nameLbl);
 
 			y += inc;
 
 			layout.putConstraint(SpringLayout.WEST, passLbl, (((WIDTH/2/2)-50)), SpringLayout.WEST,
 				createPanel);
 			layout.putConstraint(SpringLayout.NORTH, passLbl, y, SpringLayout.NORTH,
 				createPanel);
 			layout.putConstraint(SpringLayout.NORTH, passTxt, y, SpringLayout.NORTH,
 				createPanel);
 			layout.putConstraint(SpringLayout.WEST, passTxt, 20, SpringLayout.EAST,
 				passLbl);
 
 			y += inc;
 
 			layout.putConstraint(SpringLayout.NORTH ,createBtn, (HEIGHT-110), SpringLayout.NORTH,
 				createPanel);
 			layout.putConstraint(SpringLayout.EAST, createBtn, -5, SpringLayout.WEST,
 				logoutBtn);
 
 			layout.putConstraint(SpringLayout.WEST, logoutBtn, (WIDTH-90), SpringLayout.WEST,
 				createPanel);
 			layout.putConstraint(SpringLayout.NORTH, logoutBtn, (HEIGHT-110), SpringLayout.NORTH,
 				createPanel);
 
 			layout.putConstraint(SpringLayout.WEST, createAErrorLbl, ((WIDTH/2)-50), SpringLayout.WEST,
 				createPanel);
 			layout.putConstraint(SpringLayout.NORTH, createAErrorLbl, y, SpringLayout.NORTH,
 				createPanel);
 		}
 		else
 		{
 			JLabel titleLbl = new JLabel("Quiz Title:");
 			createPanel.add(titleLbl);
 
 			qTitleTxt = new JTextField("", 15);
 			createPanel.add(qTitleTxt);
 
 			JLabel questionLbl = new JLabel("How many questions:");
 			createPanel.add(questionLbl);
 
 			int i = 1;
 			String [] num = new String[99];
 
 			while ((i-1) < num.length)
 			{
 				num[(i-1)] = Integer.toString(i);
 				i++;
 			}
 
 			numList = new JComboBox(num);
 			numList.setSelectedIndex(0);
 
 			createPanel.add(numList);
 
 			JButton contBtn = new JButton("Continue");
 			contBtn.addActionListener(this);
 			createPanel.add(contBtn);
 
 			JButton backBtn = new JButton("Main Menu");
 			backBtn.addActionListener(this);
 			createPanel.add(backBtn);
 
 
 
 			layout.putConstraint(SpringLayout.WEST, titleLbl, (((WIDTH/2/2)-50)), SpringLayout.WEST,
 				createPanel);
 			layout.putConstraint(SpringLayout.NORTH, titleLbl, y, SpringLayout.NORTH,
 				createPanel);
 			layout.putConstraint(SpringLayout.NORTH, qTitleTxt, y, SpringLayout.NORTH,
 				createPanel);
 			layout.putConstraint(SpringLayout.WEST, qTitleTxt, 20, SpringLayout.EAST,
 				titleLbl);
 
 			y += inc;
 
 			layout.putConstraint(SpringLayout.WEST, questionLbl, (((WIDTH/2/2)-50)), SpringLayout.WEST,
 				createPanel);
 			layout.putConstraint(SpringLayout.NORTH, questionLbl, y, SpringLayout.NORTH,
 				createPanel);
 			layout.putConstraint(SpringLayout.NORTH, numList, y, SpringLayout.NORTH,
 				createPanel);
 			layout.putConstraint(SpringLayout.WEST, numList, 20, SpringLayout.EAST,
 				questionLbl);
 
 			y += inc;
 
 			layout.putConstraint(SpringLayout.NORTH , contBtn, (HEIGHT-110), SpringLayout.NORTH,
 				createPanel);
 			layout.putConstraint(SpringLayout.EAST, contBtn, -5, SpringLayout.WEST,
 				backBtn);
 
 			layout.putConstraint(SpringLayout.WEST, backBtn, (WIDTH-115), SpringLayout.WEST,
 				createPanel);
 			layout.putConstraint(SpringLayout.NORTH, backBtn, (HEIGHT-110), SpringLayout.NORTH,
 				createPanel);
 		}
 
 		return createPanel;
 
 	}
 
 	public JPanel account()
 	{
 		JPanel aPanel = new JPanel();
 		SpringLayout layout = new SpringLayout();
 		aPanel.setLayout(layout);
 
 		x = 25;
 		y = 25;
 		int inc = 45;
 
 		JLabel accountLbl = new JLabel("Account Mangement");
 		aPanel.add(accountLbl);
 
 		JLabel titleLbl = new JLabel("Change Pass");
 		aPanel.add(titleLbl);
 
 		JLabel cPassLbl = new JLabel("Current Password:");
 		aPanel.add(cPassLbl);
 
 		cPassTxt = new JPasswordField("", 10);
 		aPanel.add(cPassTxt);
 
 		JLabel nPassLbl = new JLabel("New Password:");
 		aPanel.add(nPassLbl);
 
 		nPassTxt = new JPasswordField("", 10);
 		aPanel.add(nPassTxt);
 
 		aErrorLbl = new JLabel("");
 		aPanel.add(aErrorLbl);
 
 		JButton submitBtn = new JButton("Submit");
 		submitBtn.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent ev)
 			{
 				passwordChangeActionPerformed(ev);
 			}
 		});
 
 		aPanel.add(submitBtn);
 
 		JButton logoutBtn = new JButton("Logout");
 		logoutBtn.addActionListener(this);
 		aPanel.add(logoutBtn);
 
 		layout.putConstraint(SpringLayout.WEST, accountLbl, x, SpringLayout.WEST,
 			aPanel);
 		layout.putConstraint(SpringLayout.NORTH, accountLbl, y, SpringLayout.NORTH,
 			aPanel);
 
 		y += inc;
 
 		layout.putConstraint(SpringLayout.WEST, titleLbl, ((WIDTH/2)-50), SpringLayout.WEST,
 			aPanel);
 		layout.putConstraint(SpringLayout.NORTH, titleLbl, y, SpringLayout.NORTH,
 			aPanel);
 
 		y += inc;
 
 		layout.putConstraint(SpringLayout.WEST, cPassLbl, (((WIDTH/2)/2)-50), SpringLayout.WEST,
 			aPanel);
 		layout.putConstraint(SpringLayout.NORTH, cPassLbl, y, SpringLayout.NORTH,
 			aPanel);
 		layout.putConstraint(SpringLayout.NORTH, cPassTxt, y, SpringLayout.NORTH,
 			aPanel);
 		layout.putConstraint(SpringLayout.WEST, cPassTxt, 20, SpringLayout.EAST,
 			cPassLbl);
 
 		y += inc;
 
 		layout.putConstraint(SpringLayout.WEST, nPassLbl, (((WIDTH/2/2)-50)), SpringLayout.WEST,
 			aPanel);
 		layout.putConstraint(SpringLayout.NORTH, nPassLbl, y, SpringLayout.NORTH,
 			aPanel);
 		layout.putConstraint(SpringLayout.NORTH, nPassTxt, y, SpringLayout.NORTH,
 			aPanel);
 		layout.putConstraint(SpringLayout.WEST, nPassTxt, 20, SpringLayout.EAST,
 			nPassLbl);
 
 		y += inc;
 
 		layout.putConstraint(SpringLayout.WEST, aErrorLbl, ((WIDTH/2)-50), SpringLayout.WEST,
 			aPanel);
 		layout.putConstraint(SpringLayout.NORTH, aErrorLbl, y, SpringLayout.NORTH,
 			aPanel);
 
 		layout.putConstraint(SpringLayout.WEST, logoutBtn, (WIDTH-90), SpringLayout.WEST,
 			aPanel);
 		layout.putConstraint(SpringLayout.NORTH, logoutBtn, (HEIGHT-110), SpringLayout.NORTH,
 			aPanel);
 		layout.putConstraint(SpringLayout.NORTH ,submitBtn, (HEIGHT-110), SpringLayout.NORTH,
 			aPanel);
 		layout.putConstraint(SpringLayout.EAST, submitBtn, -5, SpringLayout.WEST,
 			logoutBtn);
 
 
 		return aPanel;
 
 	}
 
 	public JPanel view(String title)
 	{
 		JPanel cPanel = new JPanel();
 		SpringLayout layout = new SpringLayout();
 		cPanel.setLayout(layout);
 
 		x = 25;
 		y = 25;
 		int inc = 45;
 
 		JLabel titleLbl = new JLabel(title);
 		cPanel.add(titleLbl);
 
 		JLabel insLbl = new JLabel("Double click to view");
 		cPanel.add(insLbl);
 
 		JScrollPane pane = new JScrollPane();
 		pane.setPreferredSize(new Dimension(300, 200));
 		cPanel.add(pane);
 
 		if (title.equals("Class Menu"))
 		{
 			/* //WITH DB
 				int tempSize = curr.getNumClasses();
 				int list[] = null;
 
 				for(int j = 0; j < tempSize; j++)
 				{
 					list[j] = curr.showClassId(j);
 				}
 			*/
 
 			String list[] = {"22722 CSE 340 Intro to Software Engineering",
 				"232424 CSE 205 Java Programming",
 				"235353 CSE 240 Into to PL",
 				"345454 CSE 310 Data Structs",
 				"334433 CSE 205 Java Programming",
 				"553355 CSE 120 Digital Design"};
 
 			JList classList = new JList(list);
 			classList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 			classList.setBorder(BorderFactory.createLineBorder(Color.BLACK));
 			classList.setLayoutOrientation(classList.VERTICAL);
 			classList.setVisibleRowCount(4);
 			classList.setSelectedIndex(-1);
 			pane.setViewportView(classList);
 
 
 			classList.addMouseListener( new MouseAdapter() {
 			      public void mouseClicked(MouseEvent mouseEvent) {
 				JList theList = (JList) mouseEvent.getSource();
 				if (mouseEvent.getClickCount() == 2) {
 				  int index = theList.locationToIndex(mouseEvent.getPoint());
 				  if (index >= 0) {
 				    Object o = theList.getModel().getElementAt(index);
 				    char [] input = o.toString().toCharArray();
 				    int i = 0;
 				    char c = input[i];
 				    String s = "";
 
 				    while (Character.isDigit(c))
 				    {
 					s += c;
 					i++;
 					c = input[i];
 				    }
 
 				    int id = Integer.parseInt(s);
 				    System.out.println("Double-clicked on: " + id);
 
 				    User teacher = new Teacher(1111, "Bob", "software");
 				    Class tempClass = new Class(272111, "CSE 110 Intro to Java", "This class teaches you java", teacher.getId());
 				    // Class tempClass = utility.openClass(option);
 				    dispose();
 				    Menu classView = new Menu(tempClass, curr);
 				    classView.setVisible(true);
 				  }
 				}
 			      }
 			});
 		}
 		else
 		{
 			String list[] = {"Quiz 1", "Quiz 2", "Quiz 3"};
 
 			JList quizList = new JList(list);
 			quizList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 			quizList.setBorder(BorderFactory.createLineBorder(Color.BLACK));
 			quizList.setLayoutOrientation(quizList.VERTICAL);
 			quizList.setVisibleRowCount(4);
 			quizList.setSelectedIndex(-1);
 			pane.setViewportView(quizList);
 
 			quizList.addMouseListener( new MouseAdapter() {
 			      public void mouseClicked(MouseEvent mouseEvent) {
 				JList theList = (JList) mouseEvent.getSource();
 				if (mouseEvent.getClickCount() == 2) {
 				  int index = theList.locationToIndex(mouseEvent.getPoint());
 				  if (index >= 0) {
 				        Object o = theList.getModel().getElementAt(index);
 				        String s = o.toString();
 			
 				        //System.out.println("Double-clicked on: " + s);
 					dispose();
 					String [] title = {"Hello", "GoodBye"};
 					String [] ans = {"ans", "ans"};
 					Quiz quiz = new Quiz(123,  title, ans, ans, ans, ans, ans);
 					QuizFrame gui = new QuizFrame(quiz, currClass, curr);
 					gui.setVisible(true);					  
 				    }
 				}
 			      }
 			});
 
 		}
 
 		JButton backBtn = new JButton("Main Menu");
 		backBtn.addActionListener(this);
 		cPanel.add(backBtn);
 
 		layout.putConstraint(SpringLayout.WEST, titleLbl, x, SpringLayout.WEST,
 			cPanel);
 		layout.putConstraint(SpringLayout.NORTH, titleLbl, y, SpringLayout.NORTH,
 			cPanel);
 
 		y += inc;
 
 		layout.putConstraint(SpringLayout.WEST, insLbl, (((WIDTH/2)/2)-50), SpringLayout.WEST,
 			cPanel);
 		layout.putConstraint(SpringLayout.NORTH, insLbl, y, SpringLayout.NORTH,
 			cPanel);
 
 		y += 25;
 
 		layout.putConstraint(SpringLayout.WEST, pane, (((WIDTH/2)/2)-50), SpringLayout.WEST,
 			cPanel);
 		layout.putConstraint(SpringLayout.NORTH, pane, y, SpringLayout.NORTH,
 			cPanel);
 
 		layout.putConstraint(SpringLayout.WEST, backBtn, (WIDTH-115), SpringLayout.WEST,
 			cPanel);
 		layout.putConstraint(SpringLayout.NORTH, backBtn, (HEIGHT-110), SpringLayout.NORTH,
 			cPanel);
 
 		return cPanel;
 	}
 
 	public JPanel home(String home)
 	{
 		JPanel hPanel = new JPanel();
 		SpringLayout layout = new SpringLayout();
 		hPanel.setLayout(layout);
 
 		x = 25;
 		y = 25;
 		int inc = 45;
 
 		JLabel homeLbl = new JLabel(home);
 		hPanel.add(homeLbl);
 
 		JLabel helloLbl = new JLabel("Hello, " + curr.getName());
 		hPanel.add(helloLbl);
 
 		JLabel announceLbl = new JLabel("Announcements:");
 		hPanel.add(announceLbl);
 
 		if (home.equals("Main Menu"))
 		{
 			JButton logoutBtn = new JButton("Logout");
 			logoutBtn.addActionListener(this);
 			hPanel.add(logoutBtn);
 
 			layout.putConstraint(SpringLayout.WEST, logoutBtn, (WIDTH-90), SpringLayout.WEST,
 				hPanel);
 			layout.putConstraint(SpringLayout.NORTH, logoutBtn, (HEIGHT-110), SpringLayout.NORTH,
 				hPanel);
 		}
 		else
 		{
 			JButton backBtn = new JButton("Main Menu");
 			backBtn.addActionListener(this);
 			hPanel.add(backBtn);
 
 			layout.putConstraint(SpringLayout.WEST, backBtn, (WIDTH-115), SpringLayout.WEST,
 				hPanel);
 			layout.putConstraint(SpringLayout.NORTH, backBtn, (HEIGHT-110), SpringLayout.NORTH,
 				hPanel);
 		}
 
 		layout.putConstraint(SpringLayout.WEST, homeLbl, x, SpringLayout.WEST,
 			hPanel);
 		layout.putConstraint(SpringLayout.NORTH, homeLbl, y, SpringLayout.NORTH,
 			hPanel);
 
 		y += inc;
 
 		layout.putConstraint(SpringLayout.WEST, helloLbl, x, SpringLayout.WEST,
 			hPanel);
 		layout.putConstraint(SpringLayout.NORTH, helloLbl, y, SpringLayout.NORTH,
 			hPanel);
 		y += inc;
 
 		layout.putConstraint(SpringLayout.WEST, announceLbl, x, SpringLayout.WEST,
 			hPanel);
 		layout.putConstraint(SpringLayout.NORTH, announceLbl, y, SpringLayout.NORTH,
 			hPanel);
 
 
 
 		return hPanel;
 	}
 
 	public JPanel grades()
 	{
 		JPanel gradesPanel = new JPanel();
 		SpringLayout layout = new SpringLayout();
 		gradesPanel.setLayout(layout);
 
 		/*JScrollPane pane = new JScrollPane(gradesPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                        JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
 			//pane.setPreferredSize(new Dimension(200, 50));
 */
 		x = 25;
 		y = 25;
 		int inc = 45;
 
 		JLabel titleLbl = new JLabel("Grades");
 		gradesPanel.add(titleLbl);
 
 
 
 		JButton backBtn = new JButton("Main Menu");
 		backBtn.addActionListener(this);
 		gradesPanel.add(backBtn);
 
 
 		layout.putConstraint(SpringLayout.WEST, titleLbl, x, SpringLayout.WEST,
 			gradesPanel);
 		layout.putConstraint(SpringLayout.NORTH, titleLbl, y, SpringLayout.NORTH,
 			gradesPanel);
 
 		y += inc;
 		x = 100;
 
 
 
 		if (curr.getStatus() == 's')
 		{
 			JLabel qLbl = new JLabel("Quiz");
 			gradesPanel.add(qLbl);
 
 			JLabel gLbl = new JLabel("Grades");
 			gradesPanel.add(gLbl);
 
 			layout.putConstraint(SpringLayout.WEST, qLbl, x , SpringLayout.WEST,
 				gradesPanel);
 			layout.putConstraint(SpringLayout.NORTH, qLbl, y, SpringLayout.NORTH,
 				gradesPanel);
 			layout.putConstraint(SpringLayout.NORTH, gLbl, y, SpringLayout.NORTH,
 				gradesPanel);
 			layout.putConstraint(SpringLayout.WEST, gLbl, 100, SpringLayout.EAST,
 				qLbl);
 			y += inc;
 
 			grades(gradesPanel, layout, null);
 		}
 		else
 		{
 			User [] student = new Student[3];
 			student[0] = new Student(2222, "James", "progit"); 
 			student[1] = new Student(3333, "Alex", "fuck");
 			student[2] = new Student(4444, "Kevin", "Cock");
 
 			for (int i = 0; i < student.length; i++)
 			{
 				JLabel nLbl = new JLabel(student[i].getName());
 				gradesPanel.add(nLbl);
 
 				layout.putConstraint(SpringLayout.WEST, nLbl, x , SpringLayout.WEST,
 					gradesPanel);
 				layout.putConstraint(SpringLayout.NORTH, nLbl, y, SpringLayout.NORTH,
 					gradesPanel);
 				
 				y += inc;
 
 				gradesPanel = grades(gradesPanel, layout, student[i]);
 			}
 		}
 
 		layout.putConstraint(SpringLayout.WEST, backBtn, (WIDTH-115), SpringLayout.WEST,
 			gradesPanel);
 		layout.putConstraint(SpringLayout.NORTH, backBtn, (HEIGHT-110), SpringLayout.NORTH,
 			gradesPanel);
 
 		return gradesPanel;
 	//	return pane;
 	}
 
 	public JPanel grades(JPanel panel, SpringLayout layout, User student)
 	{
 
 		// Title = Quiz Title: "Quiz 1"
 		// Grade =
 
 		/*//with DB
 		int i = 0;
 		int size = currClass.getNumQuizes();
 		JLabel[] qTitleLbl = new JLable[size];
 		JLabel [] qGradeLbl = new JLabel[size];
 
 		*/
 
 		int i = 0;
 		int size = 4;
 		JLabel [] qTitleLbl = new JLabel[size];
 		JLabel [] qGradeLbl = new JLabel[size];
 
 
 		/* Layout for Admins and Teachers:
 
 		Jeremy:
 			100%
 			67%
 		Kevin
 			543%
 			0%
 		Alex
 			-23%
 			2%
 		*/
 
 		while (i < size)
 		{
 			qTitleLbl[i] = new JLabel("Quiz");
 			panel.add(qTitleLbl[i]);
 
 			qGradeLbl[i] = new JLabel("100%");
 			panel.add(qGradeLbl[i]);
 
 			layout.putConstraint(SpringLayout.WEST, qTitleLbl[i], (x+50) , SpringLayout.WEST,
 				panel);
 			layout.putConstraint(SpringLayout.NORTH, qTitleLbl[i], y, SpringLayout.NORTH,
 				panel);
 			layout.putConstraint(SpringLayout.NORTH, qGradeLbl[i], y, SpringLayout.NORTH,
 				panel);
 			layout.putConstraint(SpringLayout.WEST, qGradeLbl[i], 100, SpringLayout.EAST,
 				qTitleLbl[i]);
 			y += 45;   //CHANGE
 			i++;
 		}
 
 		return panel;
 	}
 
 	public void actionPerformed(ActionEvent e)
 	{
 		String buttonString = e.getActionCommand();
 
 		if (buttonString.equals("Logout"))
 		{
 			dispose();
 			LoginFrame gui = new LoginFrame();
 			gui.setVisible(true);
 		}
 		else if (buttonString.equals("Create Class"))
 		{
 			try
 			{
 				int id = Integer.parseInt(courseTxt.getText());
 				String title = titleTxt.getText();
 				String desc = descTxt.getText();
 				int  nUser = Integer.parseInt(teacherTxt.getText());
 
 				//User tUser = utiliity.openUser(nUser);
 			/*	if (tempU != null)
 				{
 					utility.writeClass(id, title, desc, nUser);*/
 					createCErrorLbl.setText("Class Created");
 			/*	}
 				else
 					createCErrorLbl.setText("Class failed to be created");
 			*/
 			}
 			catch(Exception ex)
 			{
 				createCErrorLbl.setText("Incorrect Input");
 			}
 		}
 		else if (buttonString.equals("Create Account"))
 		{
 			Object selected = userList.getSelectedItem();
 			String combo = selected.toString();
 
 			try {
 				int id = Integer.parseInt(idTxt.getText());
 				String name = nameTxt.getText();
 				String pass;
 
 				if (combo.equals("Admin"))
 				{
 					pass = passTxt.getText();
 					Admin admin = new Admin(id, name, pass);
 					//utility.writeAdmin(id, name, pass, 'a');
 					createAErrorLbl.setText("Admin Created");
 
 				}
 				else
 				{
 					pass = Long.toHexString(Double.doubleToLongBits(Math.random())); // Our Random pass generator*
 					pass = pass.substring(0, 8);
 
 					passTxt.setText(pass);
 
 					if (combo.equals("Teacher"))
 					{
 						Teacher teacher = new Teacher(id, name, pass);
 						//utility.writeTeacher(id, name, pass, 't');
 						createAErrorLbl.setText("Teacher Created");
 					}
 					else
 					{
 						Student student = new Student(id, name, pass);
 						//utility.writeStudent(id, name, pass, 's'); //Write student should change
 						createAErrorLbl.setText("Student Created");
 					}
 				}
 			}
 			catch (Exception ex)
 			{
 				createAErrorLbl.setText("Incorrect Input");
 			}
 		}
 		else if (buttonString.equals("Main Menu"))
 		{
 			dispose();
 			Menu menu = new Menu(curr);
 			menu.setVisible(true);
 		}
 		else if (buttonString.equals("Continue"))
 		{
 			Object selected = numList.getSelectedItem();
 			int num = Integer.parseInt(selected.toString());
 
 			String quiz = qTitleTxt.getText();
 
 			dispose();
 			QuizFrame q = new  QuizFrame(quiz, num, currClass, curr);
 			q.setVisible(true);
 		}
 		else
 			System.out.println("Unexpected error.");
 	}
 
 	private void passwordChangeActionPerformed(ActionEvent event)
 	{
 		try
 		{
 			String oldPass = new String(cPassTxt.getPassword());
 			String newPass = new String(nPassTxt.getPassword());
 
 			// DB
 			// utility.changePass(curr.getId(), curr.getName(), oldPass, newPass);
 
 			// Needs a boolean to notify if change was successful or not
 			// if (flag)
 			aErrorLbl.setText("Password Change Successful!");
 			//else
 				//aErrorLbl.setText("Incorrect Password");
 		}
 		catch (Exception ex)
 		{
 			aErrorLbl.setText("Incorrect Input");
 		}
 	}
 
 	private void comboBoxActionPerformed(ActionEvent event)
 	{
 		Object selected = userList.getSelectedItem();
 		String s = selected.toString();
 
 		if ((s.compareTo("Admin")) == 0)
 		{
 			passTxt.setEditable(true);
 		}
 		else
 		{
 			passTxt.setText("");
 			passTxt.setEditable(false);
 		}
 	}
 
 	private void tabChanged()
 	{
 		if (currClass == null)
 		{
 			aErrorLbl.setText("");
 			cPassTxt.setText("");
 			nPassTxt.setText("");
 
 			if (curr.getStatus() == 'a')
 			{
 				idTxt.setText("");
 				nameTxt.setText("");
 				userList.setSelectedIndex(0);
 				passTxt.setText("");
 				createAErrorLbl.setText("");
 				courseTxt.setText("");
 				titleTxt.setText("");
 				descTxt.setText("");
 				teacherTxt.setText("");
 				createCErrorLbl.setText("");
 			}
 		}
 		else
 		{
 			qTitleTxt.setText("");
 			numList.setSelectedIndex(-1);	
 		}
 	}
 }
