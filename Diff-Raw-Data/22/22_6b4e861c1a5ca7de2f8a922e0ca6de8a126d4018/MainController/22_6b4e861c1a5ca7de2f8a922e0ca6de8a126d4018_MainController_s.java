 /**
  * 
  */
 package code;
 
 import java.awt.Graphics;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JOptionPane;
 import javax.swing.ListSelectionModel;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 
 
 /**
  * @author Eric
  *
  */
 public class MainController {
     private static MainController mainController;
 
     private Account account;
     //TODO
     public MainFrame view;
     private LessonController lessonController;
     private HistoryController historyController;
     private int currentEnableLessonIndex;
 
     public static MainController getMainController(){
 	if (mainController == null) {
 	    synchronized (MainController.class) {
 		if (mainController == null) {
 		    mainController = new MainController();
 		}
 	    }
 	}
 	return mainController;
     }
 
 
     private MainController(){
 	view = new MainFrame();
 	addListener();
 	addJtableListener();
 	//view.setVisible(true);
     }
 
     public void increaseEnabeLessonIndex(){
 	currentEnableLessonIndex++;
 	account.getLesson(currentEnableLessonIndex).setEnabled(true);
     }
 
 
     /**
      * @return the historyController
      */
     public HistoryController getHistoryController() {
 	return historyController;
     }
 
 
     /**
      * @return the lessonController
      */
     public LessonController getLessonController() {
 	return lessonController;
     }
 
 
     public void update(Graphics g){
 	view.update(g);
     }
 
 
     /**
      * @param account the account to set
      */
     public void setAccount(Account account) {
 	this.account = account;
 	lessonController = new LessonController(account.getLesson(0));
 	historyController = new HistoryController(account.getLesson(0));
 	account.getLesson(0).setEnabled(true);
 	initAllLessons();
     }
 
 
     private void initAllLessons(){
 	for(Lesson l : account.getAllLesson()){
 	    view.addLesson(l.getLessonName());
 	}
     }
 
     private void addListener(){
 	view.getExitButton().addActionListener(new ActionListener(){
 	    public void actionPerformed(ActionEvent e){
 		if(account.isModify()){
 		    int value = JOptionPane.showConfirmDialog(null,
 			    "Do you want to save you changes ?",
 			    "Log out",
 			    JOptionPane.YES_NO_CANCEL_OPTION);
 		    if(value == JOptionPane.CANCEL_OPTION){
 			return;
 		    } else if(value == JOptionPane.YES_OPTION){
 			account.writeToDatabase();
 		    }
 
 		}		
 		view.setVisible(false);
 		view.dispose();
 		System.exit(0);
 	    }
 	});
 
 	view.getLogoutButton().addActionListener(new ActionListener(){
 	    public void actionPerformed(ActionEvent e){
 		if(account.isModify()){
 		    int value = JOptionPane.showConfirmDialog(null,
 			    "Do you want to save you changes ?",
 			    "Log out",
 			    JOptionPane.YES_NO_CANCEL_OPTION);
 		    if(value == JOptionPane.CANCEL_OPTION){
 			return;
 		    } else if(value == JOptionPane.YES_OPTION){
 			account.writeToDatabase();
 		    }
 		} 		    
 		view.setVisible(false);
 		view.dispose();
 		new LoginController();
 	    }
 	});
 
 	view.getAddLessonButton().addActionListener(new ActionListener(){
 	    public void actionPerformed(ActionEvent ae){
 		String name = JOptionPane.showInputDialog("Please input a name");
 		try{
 		    if(name == null || name.equals("")) throw new Exception("not valid lesson name");
 		    account.createNewLesson(name);
 		    view.addLesson(name);
 		}catch(Exception e){
 		    JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
 		}
 	    }
 	});
 
 	view.getDeleteLessonButton().addActionListener(new ActionListener(){
 	    public void actionPerformed(ActionEvent e){
 		int row = view.getLessonTable().getSelectedRow();
 		if(row == -1) return;
 		account.deleteLesson(row);
 		view.deleteLesson(row);
 	    }
 	});
 
 	view.getTestAllButton().addActionListener(new ActionListener(){
 	    public void actionPerformed(ActionEvent e){
 		new TestController(account);
 		setVisible(false);
 	    }
 	});
 
 
     }
 
     private void addJtableListener(){
 	ListSelectionModel listSelectionModel = view.getLessonTable().getSelectionModel();
 	listSelectionModel.addListSelectionListener(new ListSelectionHandler());
     }
 
     public void setVisible(boolean visible){
 	view.setVisible(visible);
     }
 
     class ListSelectionHandler implements ListSelectionListener {
 	public void valueChanged(ListSelectionEvent e) { 
 	    ListSelectionModel lsm = (ListSelectionModel)e.getSource();
 
 	    boolean isAdjusting = e.getValueIsAdjusting();
 
 	    //System.out.println("("+isAdjusting+","+firstIndex+","+lastIndex+","+minIndex+","+maxIndex+")");
 	    if (!lsm.isSelectionEmpty() && !isAdjusting) {
 		int index = lsm.getMinSelectionIndex();
 		Lesson l = account.getLesson(index);
 		lessonController.setSelectedLesson(l);
 		historyController.setSelectedLesson(l);
 	    } 
 	}
     }
 
 }
