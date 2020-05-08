 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.*;
 import java.util.*;
 
 public class LoginFrame extends JFrame implements ActionListener
 {
 	public final int WIDTH = 350;
 	public final int HEIGHT = 250;
 	private JLabel errorLbl;
 	private final JTextField idTxt;
 	private final JPasswordField passTxt;
 
 	public LoginFrame()
 	{
 		super("Just In Time Learning");
 		int y = 10;
 
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
 	
 		Container contentPane = getContentPane();
 
 		SpringLayout layout = new SpringLayout();
 		contentPane.setLayout(layout);
 
 		JLabel titleLbl = new JLabel("Login Screen");
 		contentPane.add(titleLbl);	
 
 		JLabel idLbl = new JLabel("Id:");
 		contentPane.add(idLbl);		
 
 		idTxt = new JTextField("", 10);
 		contentPane.add(idTxt);	
 
 		JLabel passLbl = new JLabel("Password:");
 		contentPane.add(passLbl);
 
 		passTxt = new JPasswordField("", 10);	
 		contentPane.add(passTxt);
 
 		errorLbl = new JLabel("");
 		contentPane.add(errorLbl);
 
 		JButton loginBtn = new JButton("Login");
 		loginBtn.setMnemonic(KeyEvent.VK_ENTER);
 		loginBtn.addActionListener(this);
 
 		contentPane.add(loginBtn);	
 
 		JButton closeBtn = new JButton("Close");
 		closeBtn.addActionListener(this);
 		contentPane.add(closeBtn);
 
 		layout.putConstraint(SpringLayout.WEST, titleLbl, ((WIDTH/2)-50), SpringLayout.WEST,
 			contentPane);
 		layout.putConstraint(SpringLayout.NORTH, titleLbl, y, SpringLayout.NORTH,
 			contentPane); 
 		y += 45;
 
 		layout.putConstraint(SpringLayout.WEST, idLbl, (((WIDTH/2)/2)-50), SpringLayout.WEST,
 			contentPane);
 		layout.putConstraint(SpringLayout.NORTH, idLbl, y, SpringLayout.NORTH,
 			contentPane);
 		layout.putConstraint(SpringLayout.NORTH, idTxt, y, SpringLayout.NORTH,
 			contentPane);
 		layout.putConstraint(SpringLayout.WEST, idTxt, 20, SpringLayout.EAST,
 			idLbl);
 
 		y += 35;
 
 		layout.putConstraint(SpringLayout.WEST, passLbl, (((WIDTH/2)/2)-50), SpringLayout.WEST,
 			contentPane);
 		layout.putConstraint(SpringLayout.NORTH, passLbl, y, SpringLayout.NORTH,
 			contentPane);
 		layout.putConstraint(SpringLayout.NORTH, passTxt, y, SpringLayout.NORTH,
 			contentPane);
 		layout.putConstraint(SpringLayout.WEST, passTxt, 20, SpringLayout.EAST,
 			passLbl);
 
 		y += 45;
 
 		layout.putConstraint(SpringLayout.WEST, errorLbl, ((WIDTH/2)-50), SpringLayout.WEST,
 			contentPane);
 		layout.putConstraint(SpringLayout.NORTH, errorLbl, y, SpringLayout.NORTH,
 			contentPane);
 
 		y += 25;
 
 		layout.putConstraint(SpringLayout.WEST, loginBtn, (WIDTH-75), SpringLayout.WEST, 
 			contentPane);
 		layout.putConstraint(SpringLayout.NORTH, loginBtn, (HEIGHT-75), SpringLayout.NORTH, 
 			contentPane); 
 		layout.putConstraint(SpringLayout.NORTH ,closeBtn, (HEIGHT-75), SpringLayout.NORTH, 
 			contentPane);
 		layout.putConstraint(SpringLayout.EAST, closeBtn, -5, SpringLayout.WEST, 
 			loginBtn);
 	}
 
 	public void actionPerformed(ActionEvent e)
 	{
 		String buttonString = e.getActionCommand();
 		User curr = null;
 		Vector<User> u = new Vector<User>();
 		User admin = new Admin(1234, "Jeremy", "password");
 		User teacher = new Teacher(1111, "Bob", "software");
 		User student = new Student(2222, "James", "progit"); 
 		//Class nClass = new Class(272111, "CSE 110 Intro to Java", "This class teaches you java", teacher);
 		u.add(admin);
 		u.add(student);
 		u.add(teacher);
 		//Vector<Answer> answers = new Vector<Answer>();
 		//Answer ans = new Answer("2009", false);
 		//answers.add(ans);
 		//ans = new Answer("2010", false);
 		//answers.add(ans);
 		//ans = new Answer("2011", true);
 		//answers.add(ans);
 		//Vector<Question> question = new Vector<Question>();
 		//Question q = new Question("What year is it?", answers);
 		//question.add(q);
 		//Quiz quiz1 = new Quiz("Quiz 1", question);
 		//nClass.addQuiz(quiz1);
 
 
 		if (buttonString.equals("Login"))
 		{
 
 
 			try {
 				int id = Integer.parseInt(idTxt.getText());
 				String pass = new String(passTxt.getPassword());
 				User temp;
 				/*/ With DB
 				jaklUtilities utility = new jaklUtilities();
 				
 				temp = utility.openUser(id);
 
 				if ((temp != null) && (pass.compareTo(temp.getPass()) == 0))
 					curr = temp;
 				else
 					errorLbl.setText("Id or Pass or incorrect");
 
 				*/// w/o DB
 				if (u != null)
 					for (int i = 0; i < u.size(); i++)
 					{
 						temp = u.get(i);
 						if ((temp.getId() == id) && (pass.compareTo(temp.getPass()) == 0))
 							curr = temp;
 					}
 			//*/	
 				if (curr != null)
 				{
 					dispose();
 					Menu gui = new Menu(curr);
 					gui.setVisible(true);
 				}
 				else
 					errorLbl.setText("User does not exist");
 			}
 			catch (Exception ex)
 			{
 				errorLbl.setText("Id or Pass or incorrect");	
 			}
 		}
 		else
 		if (buttonString.equals("Close"))
 		{
 			dispose();
 			System.exit(0);
 		}
 		else
 			System.out.println("Unexpected error.");	
 	}
 }
