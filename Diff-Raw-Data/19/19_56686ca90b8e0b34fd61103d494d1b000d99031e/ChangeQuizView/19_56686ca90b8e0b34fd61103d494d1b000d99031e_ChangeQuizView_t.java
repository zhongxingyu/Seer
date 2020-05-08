 package view;
 
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.Toolkit;
 import java.awt.event.ActionListener;
 import java.util.List;
 
 import javax.swing.BorderFactory;
 import javax.swing.DefaultListModel;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextField;
 import javax.swing.ListSelectionModel;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 
 import model.Exercise;
 import model.Exercise.ExerciseCategory;
 import model.Quiz;
 import model.QuizExercise;
 import model.QuizStatus;
 import model.Teacher;
 import controller.StartAppController;
 
 
 /**
  * 
  * @author Steve
  * @version 6/11/2013
  * 
  */
 
 public class ChangeQuizView extends JFrame {
 	
 		private static final long serialVersionUID = 1L;
 
 		private JTextField txt_01, txt_02;
 		private JLabel lb_01, lb_02, lb_03, lb_04, lb_05, lb_Quiz, lb_exInQuiz, lb_Ex;
 
 		private JButton btn_add, btn_update, btn_delete, btn_search_quiz;
 
 		private JList <Exercise> listExercise;
         private JList <Quiz> listQuiz;
         private JList <QuizExercise> listExercisesInQuiz;
         
         private JComboBox <QuizStatus> comboStatus;
         private JComboBox <Teacher> comboAuthor;
         private JComboBox<Integer> comboLeerjaar;
         
 		private DefaultListModel<Exercise> listModelEx;
 		private DefaultListModel<Quiz> listModelQuiz;	
 		private DefaultListModel<QuizExercise> listModelExercisesInQuiz;
 		
         private final JScrollPane paneExercise;
         private JScrollPane paneQuiz;
         private final JScrollPane paneExInQuiz;
 
 		public ChangeQuizView() {
 			super("Quiz wijzigen");
 			setResizable(true); //niet resizable
 			setLocationRelativeTo(null); //positie zetten
 			
 			Toolkit tk = Toolkit.getDefaultToolkit();
 			
 			Dimension dim = tk.getScreenSize();
 			
 			int x = (dim.width / 2) - (this.getWidth() / 2);
 			int y = (dim.height / 2) - (this.getHeight() / 2);
 						
 			this.setLocation(x, y);
 			
 			this.setSize(800,600);
 
 			this.setVisible(true);
 						
 			this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 			this.addWindowListener(new java.awt.event.WindowAdapter() {
 			    @Override
 			    public void windowClosing(java.awt.event.WindowEvent evt)
 			    {	
 			    	new StartAppController().startApp();
 			    }
 			});
 			
 			final JPanel mainPanel = new JPanel();
 
 			mainPanel.setBorder(BorderFactory.createTitledBorder("Quiz Control"));
 					
 			//Components
 			
 			//Zoekfunctie
 			
 			txt_01 = new JTextField("Zoek quiz", 20);			
 			btn_search_quiz = new JButton("Zoek");
 			
 			//Buttons
 			
 			btn_add = new JButton("Voeg toe");			
 			btn_delete = new JButton("Verwijder");			
 			btn_update = new JButton("Update");
 			
 			//Status
 
 			lb_01 = new JLabel("Status: ");
 			comboStatus = new JComboBox <QuizStatus>(QuizStatus.values());
 			
 			//Grades
 			
 			lb_05 = new JLabel("Leerjaar: ");
 			Integer[] grades = {1, 2, 3, 4};
 			comboLeerjaar = new JComboBox<Integer>(grades);
 			
 			//Title
 			
 			lb_04 = new JLabel("Quiztitel: ");
 			txt_02 = new JTextField();
 			
 			//Author
 			
 			lb_03 = new JLabel("Leraar: ");
 			comboAuthor = new JComboBox <Teacher>(Teacher.values());
 
 			
 			
 			//populate list quiz
 			
 			lb_Quiz = new JLabel("Beschikbare quizzes:");			
 			listModelQuiz = new DefaultListModel<Quiz>(); 			
 			listQuiz = new JList<>(listModelQuiz);
 			listQuiz.setVisibleRowCount(10);
 			listQuiz.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);			
 			paneQuiz = new JScrollPane(listQuiz);
 			
 			//populate ex & parameters in selected Quiz
 			
 			lb_exInQuiz = new JLabel("Vragen in quiz:");
 			
 			listQuiz.addListSelectionListener(new ListSelectionListener() {
 			    public void valueChanged(ListSelectionEvent event) {
 			        if (!event.getValueIsAdjusting()){
 			            JList<Quiz> source = (JList)event.getSource();
 			            
 			            Quiz selected = (Quiz)source.getSelectedValue();	            
 			            
 			            getExercisesInQuiz(selected.getQuizExercises());
 			            comboAuthor.setSelectedItem(selected.getTeacher());
 			            comboStatus.setSelectedItem(selected.getStatus());
 			            comboLeerjaar.setSelectedItem(selected.getLeerJaren());	
 			            txt_02.setText(selected.getSubject());
 			        }
 			    }
 			});
 			
 			listModelExercisesInQuiz = new DefaultListModel<QuizExercise>();			
 			listExercisesInQuiz = new JList<>(listModelExercisesInQuiz);			
 			listExercisesInQuiz.setVisibleRowCount(10);
 			listExercisesInQuiz.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);			
 			paneExInQuiz = new JScrollPane(listExercisesInQuiz);
 			
 			//populate list exercises
 			
 			lb_Ex = new JLabel("Beschikbare vragen:");			
 			listModelEx = new DefaultListModel<Exercise>();
 			listExercise = new JList<>(listModelEx);			
 			listExercise.setVisibleRowCount(10);
 			listExercise.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);			
 			paneExercise = new JScrollPane(listExercise);
 			
 			//Layout Manager
 			
 			mainPanel.setLayout(new GridBagLayout());
 			
 			GridBagConstraints gbc = new GridBagConstraints();
 
 			gbc.gridx = 1;
 			gbc.gridy = 1;
 			gbc.gridwidth = 4;
 			gbc.gridheight = 1;
 			gbc.weightx = 0;
 			gbc.weighty = 0;
 			gbc.insets = new Insets(2,2,2,2);
 			gbc.anchor = GridBagConstraints.CENTER;
 			gbc.fill = GridBagConstraints.BOTH;
 			
 			
 			
 			mainPanel.add(txt_01, gbc);
 			gbc.gridx = 5;
 			gbc.gridwidth = 2;
 			mainPanel.add(btn_search_quiz, gbc);
 			gbc.gridy = 2;
 			gbc.gridx = 1;
 			gbc.gridwidth = 1;
 			mainPanel.add(lb_05, gbc);
 			gbc.gridx = 2;
 			gbc.gridwidth = 2;
 			mainPanel.add(comboLeerjaar, gbc);
 			gbc.gridx = 4;
 			gbc.gridwidth = 1;
 			mainPanel.add(lb_04, gbc);
 			gbc.gridx = 5;
 			gbc.gridwidth = 2;
 			mainPanel.add(txt_02, gbc);
 			gbc.gridy = 3;
 			gbc.gridx = 1;
 			gbc.gridwidth = 1;
 			mainPanel.add(lb_03, gbc);
 			gbc.gridx = 2;
 			gbc.gridwidth = 2;
 			mainPanel.add(comboAuthor, gbc);
 			gbc.gridx = 4;
 			gbc.gridwidth = 1;
 			mainPanel.add(lb_01, gbc);
 			gbc.gridx = 5;
 			gbc.gridwidth = 2;
 			mainPanel.add(comboStatus, gbc);
 			gbc.gridy = 4;
 			gbc.gridx = 1;
 			gbc.gridwidth = 2;
 			mainPanel.add(btn_add, gbc);
 			gbc.gridx = 3;
 			mainPanel.add(btn_delete, gbc);
 			gbc.gridx = 5;
 			mainPanel.add(btn_update, gbc);
 			gbc.gridy = 5;
 			gbc.gridx = 1;
 			gbc.gridwidth = 1;
 			mainPanel.add(lb_Quiz, gbc);
 			gbc.gridx = 4;
 			mainPanel.add(lb_exInQuiz, gbc);
 			gbc.gridy = 6;
 			gbc.gridx = 1;
 			gbc.gridwidth = 3;
 			mainPanel.add(paneQuiz, gbc);
 			gbc.gridx = 4;
 			mainPanel.add(paneExInQuiz, gbc);
 			gbc.gridy = 7;
 			gbc.gridx = 1;
 			gbc.gridwidth = 1;
 			mainPanel.add(lb_Ex, gbc);
 			gbc.gridy = 8;
 			gbc.gridx = 1;
 			gbc.gridwidth = 6;
 			mainPanel.add(paneExercise, gbc);
 			gbc.gridwidth = 1;
 
 			add(mainPanel);
 
 			//get exercise from available ex list
 
 			listExercise.addListSelectionListener(new ListSelectionListener() {
 				public void valueChanged(ListSelectionEvent event) {
 					if (!event.getValueIsAdjusting()){
 						JList<Exercise> source = (JList)event.getSource();
 
 						Exercise selected = (Exercise)source.getSelectedValue();	
 
 						getExercise(selected);
 					}
 				}
 			});
 
 			//get exercise from  ex in quiz list
 
 			listExercisesInQuiz.addListSelectionListener(new ListSelectionListener() {
 				public void valueChanged(ListSelectionEvent event) {
 					if (!event.getValueIsAdjusting()){
 						JList<QuizExercise> source = (JList)event.getSource();
 
 						QuizExercise selected = (QuizExercise)source.getSelectedValue();
 
 						getExercise(selected.getExercise());
 					}
 				}
 			});
 						
 		}
 		
 		//Selectors
 		
 		/**
 		 * @return
 		 */
 		public Teacher getAuthor(){
 			return (Teacher) comboAuthor.getSelectedItem();
 		}
 
 		/**
 		 * @return
 		 */
 		public QuizStatus getStatus(){
 			return (QuizStatus) comboStatus.getSelectedItem();
 		}
 		
 		/**
 		 * @return
 		 */
 		public Integer getGrade(){
 			return Integer.parseInt(comboLeerjaar.getSelectedItem().toString());
 		}
 		
 		/**
 		 * @return
 		 */
 		public String getQuizTitle() {
 			return txt_02.getText();
 		}
 		
 		/**
 		 * @return
 		 */
 		public String getSearchText() {
 			if (txt_01 == null){
 				System.out.println(txt_01.getText() + "has null value");
 				return "";
 			}
 			else {
 				System.out.println(txt_01.getText() + "has value");
 				return txt_01.getText();
 			}
 		}
 		
 		/**
 		 * @return
 		 */
 		public String getSelectedValueToList(){
 			return String.valueOf(listExercise.getSelectedValue());
 		}
 		
 		/**
 		 * @return
 		 */
 		public Exercise getSelectedExerciseValueFromList(){
 			return listExercise.getSelectedValue(); 
 		}
 		
 		/**
 		 * @return
 		 */
 		public Quiz getSelectedQuizValueFromList(){
 			System.out.println(listQuiz.getSelectedValue().getSubject());
 			return listQuiz.getSelectedValue();
 		}
 
 		/**
 		 * @return
 		 */
 		public QuizExercise getSelectedQuizExerciseValueFromList(){
 			return listExercisesInQuiz.getSelectedValue();
 		}
 			
 		/**
 		 * @return
 		 */
 		public DefaultListModel<Quiz> getListQuizModel(){
 			return listModelQuiz;
 		}
 		
 		/**
 		 * @return
 		 */
 		public DefaultListModel<Exercise> getListExModel(){
 			return listModelEx;
 		}
 		
 		
 		//setExercises
 		
 		/**
 		 * @return
 		 */
 		public void setExercisesList(List<Exercise> exerciseList){
 			DefaultListModel listModel = new DefaultListModel();
 			
 			for (Exercise ex : exerciseList){
 
 				listModel.addElement(ex);
 			}
 			
 			this.listExercise.setModel(listModel);
 		}
 		
 		/**
 		 * @return
 		 */
 		public JList<Exercise> getExercisesList() {
 			return listExercise;
 		}
 		
 		//setQuizzes
 		
 		/**
 		 * @return
 		 */
 		public void setQuizList(List<Quiz> quizList){
 			DefaultListModel listModel = new DefaultListModel();
 			
 			for (Quiz q : quizList){
 				listModel.addElement(q);
 			}
 			this.listQuiz.setModel(listModel);
 		}
 		
 		//add exercises in quiz to list
 		
 		/**
 		 * @return
 		 */
 		public void getExercisesInQuiz(List<QuizExercise> exInQuizList){
 			DefaultListModel listModel = new DefaultListModel<>();
 			
 			for (QuizExercise ex : exInQuizList){
 
 				listModel.addElement(ex);
 			}
 			this.listExercisesInQuiz.setModel(listModel);
 		}
 		
 		/**
 		 * @return
 		 */
 		public void setListExercisesInQuiz(){
 			
 		}
 		
 		/**
 		 * @return
 		 */
 		public void getExercise(Exercise ex){
 			System.out.println(ex.getQuestion());
 			
 		}
 		
 		public void UpdateExerciseInQuiz(){
 			int index = listQuiz.getSelectedIndex();
 			listQuiz.setSelectedIndex(0);
 			listQuiz.setSelectedIndex(index);
 			JOptionPane.showMessageDialog(null, "De quiz werd geüpdatet.");
 		}
 		
 		
 
 		// events
 		
 		public void addAddListener(ActionListener listenForAddButton) {
 			
 			btn_add.addActionListener(listenForAddButton);
 		}
 		
 		public void addUpdateListener(ActionListener listenForUpdateButton) {
 
 			btn_update.addActionListener(listenForUpdateButton);
 		}
 		
 		public void addDeleteListener(ActionListener listenForDeleteButton) {
 			
 			btn_delete.addActionListener(listenForDeleteButton);
 		}
 		
 		public void addSearchListener(ActionListener listenForSearchButton) {
 	
 			btn_search_quiz.addActionListener(listenForSearchButton);
 		}
 	
 
 		// Open a popup that contains the error message passed
 
 		public void displayErrorMessage(String errorMessage) {
 
			JOptionPane.showMessageDialog(null, errorMessage);
 		}
 }
