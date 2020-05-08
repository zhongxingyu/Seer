 package code;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 
 import javax.swing.BorderFactory;
 import javax.swing.Icon;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JComponent;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.ListSelectionModel;
 import javax.swing.table.DefaultTableModel;
 import javax.swing.table.TableCellRenderer;
 
 import org.apache.commons.io.FilenameUtils;
 
 public class MainFrame extends AbstractFrame{
     private JTable lessonTable;
     private JScrollPane lessonScrollPanel;
     private JButton logoutButton, testAllButton, addLessonButton, deleteLessonButton;
     private JLabel titleLabel, historyLabel, listLabel;
     private HistoryPanel historyPanel;
     private LessonPanel lessonPanel;
     private DefaultTableModel tableModel;
     private int sum_lesson;
 
     public MainFrame(){
 	super(FilenameUtils.separatorsToSystem("resource/MainFrame.png"));
 	sum_lesson=0;
 	getExitButton().setBounds(getWidth()-90,17,30,30);
 	getShrinkButton().setBounds(getWidth()-140, 22, 50, 20);
 	initTitleLabel();
 	initLogoutButton();
 	initLessonScrollPanel();
 
 	initLessonTable();
 	initLessonButton();
 	initHistoryPanel();
 	initLessonPanel();
 	initTestAllButton();
 	initDefaultLabel();
     }
 
     /**
      * @return the historyPanel
      */
     public HistoryPanel getHistoryPanel() {
 	return historyPanel;
     }
 
     /**
      * @return the lessonPanel
      */
     public LessonPanel getLessonPanel() {
 	return lessonPanel;
     }
 
     /**
      * @return the lessonTable
      */
     public JTable getLessonTable() {
 	return lessonTable;
     }
 
     /**
      * @return the logoutButton
      */
     public JButton getLogoutButton() {
 	return logoutButton;
     }
 
     /**
      * @return the testAllButton
      */
     public JButton getTestAllButton() {
 	return testAllButton;
     }
 
     /**
      * @return the addLessonButton
      */
     public JButton getAddLessonButton() {
 	return addLessonButton;
     }
 
     /**
      * @return the deleteLessonButton
      */
     public JButton getDeleteLessonButton() {
 	return deleteLessonButton;
     }
 
 
     private void initLogoutButton(){
 	logoutButton = new JButton(new ImageIcon("resource/LogoutButton.png"));
 	logoutButton.setBounds(20,20,73,32);
 	logoutButton.setBorderPainted(false);
 	logoutButton.setToolTipText("logout");
 	logoutButton.setOpaque(false);
 	logoutButton.setBackground(new Color(0,0,0,0));
 	getContentPane().add(logoutButton);
     }
 
     private void initTitleLabel(){
 	titleLabel = new JLabel("English Tutor");
 	titleLabel.setBounds(200,10,80,100);
 	getContentPane().add(titleLabel);
     }
 
     private void initLessonScrollPanel(){
 	lessonScrollPanel = new JScrollPane();
 	
 	lessonScrollPanel.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
 	lessonScrollPanel.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
 	lessonScrollPanel.getViewport().setOpaque(false);
 	lessonScrollPanel.setOpaque(false);
 	lessonScrollPanel.setBounds(35, 80, 400, 500);
 	lessonScrollPanel.setBorder(BorderFactory.createMatteBorder(1,1,1,1,Color.white));
 	//lessonScrollPanel.setBorder(BorderFactory.createMatteBorder(1,1,1,1,Color.RED));
 	getContentPane().add(lessonScrollPanel);
     }
 
     private void initLessonTable(){
 	tableModel = new DefaultTableModel(0,3);
 
 	lessonTable = new JTable(){
 	    public Component prepareRenderer(
 		    TableCellRenderer renderer, int row, int column)
 	    {
 		Component c = super.prepareRenderer(renderer, row, column);
 		//renderer.setBorder(new MatteBorder(1, 0, 1, 0, Color.RED));
 		JComponent jc = (JComponent)c;
 		//  Color row based on a cell value
 		//  Alternate row color
 
 		if (isRowSelected(row)){
 		    jc.setBorder(BorderFactory.createMatteBorder(2,0,2,0,new Color(202, 76, 67)));
 		    jc.setBackground(new Color(0, true));
 		    jc.setForeground(Color.BLUE);
 		}
 		else{
 		    //jc.setBorder(BorderFactory.createMatteBorder(0,0,2,0,Color.white));
 		    jc.setForeground(Color.black);
 		}
 		return c;
 	    }
 	};
 
 
 	lessonTable.setModel(tableModel);
 	lessonTable.setBounds(0, 0, 380, 500);
 	lessonTable.setOpaque(false);
 	lessonTable.setBackground(new Color(0, true));
 	lessonTable.setIntercellSpacing(new Dimension(0,0));
 	lessonTable.setShowVerticalLines(false);
 	lessonTable.setShowHorizontalLines(false);
 	lessonTable.setRowHeight(30);
 	lessonTable.setBorder(null);
 	lessonTable.getTableHeader().setPreferredSize(new Dimension(0,0));
 	ListSelectionModel listSelectionModel = lessonTable.getSelectionModel();
 	listSelectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 	lessonTable.getTableHeader().setVisible(false);
 
 	//lessonTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
 	//lessonTable.getColumnModel().getColumn(0).setPreferredWidth(27);
 	//lessonTable.getColumnModel().getColumn(1).setMinWidth(350);
 	//lessonTable.getColumnModel().getColumn(2).setPreferredWidth(27);
 
 	lessonScrollPanel.setViewportView(lessonTable);	
     }
 
     private void initLessonButton(){
 	addLessonButton = new JButton("add");
 	addLessonButton.setBounds(40, 580, 60, 30);
 	addLessonButton.addMouseListener(new AddLessonButtonAdapter());
 
 	deleteLessonButton = new JButton("delete");
 	deleteLessonButton.setBounds(100, 580, 80, 30);
 
 	getContentPane().add(addLessonButton);
 	getContentPane().add(deleteLessonButton);
     }
 
     private void initTestAllButton(){
 	testAllButton = new JButton("Test All");
 	testAllButton.setBounds(185, 600, 80, 40);
 	getContentPane().add(testAllButton);
     }
 
     private void initDefaultLabel(){
 	historyLabel = new JLabel(new ImageIcon(FilenameUtils.separatorsToSystem("resource/History.png")));
 	historyLabel.setBounds(938, 60, 40, 80);
 	//historyLabel.setBorder(BorderFactory.createMatteBorder(1,1,1,1,Color.BLACK));
 	historyLabel.addMouseListener(new HistoryLabelAdapter());
 	getContentPane().add(historyLabel);
 
 	listLabel = new JLabel(new ImageIcon(FilenameUtils.separatorsToSystem("resource/Phrases.png")));
 	listLabel.setBounds(938, 210, 40, 80);
 	listLabel.addMouseListener(new ListLabelAdapter());
 	//listLabel.setBorder(BorderFactory.createMatteBorder(1,1,1,1,Color.BLACK));
 	getContentPane().add(listLabel);
     }
 
     private void initHistoryPanel(){
 	historyPanel = HistoryPanel.getHistoryPanel();
 	//historyPanel.setOpaque(false);
     }
 
     private void initLessonPanel(){
 	lessonPanel = LessonPanel.getLessonPanel();
 	getContentPane().add(lessonPanel);
     }
 
     public void addLesson(String lessonName){
 	sum_lesson++;
 
 	DefaultTableModel newTableModel = new DefaultTableModel(sum_lesson,3){
 	    @Override
 	    public boolean isCellEditable(int row, int column) {
 		return false;
 	    }
 	};
 	for(int i=0;i<sum_lesson-1;i++)
 	    for(int j=0;j<3;j++){
 		newTableModel.setValueAt(tableModel.getValueAt(i, j), i, j);
 	    }
 	lessonTable.setModel(newTableModel);
 	tableModel = newTableModel;
 
 	lessonTable.setValueAt(new ImageIcon(FilenameUtils.separatorsToSystem("resource/Lock.png")), sum_lesson-1, 0);
 	lessonTable.setValueAt(lessonName, sum_lesson-1, 1);
 	lessonTable.setValueAt("", sum_lesson-1, 2);
 
 	//lessonTable.getColumn("A").setCellEditor(lessonTable.getDefaultEditor(Icon.class));
 	lessonTable.getColumn("A").setCellRenderer(lessonTable.getDefaultRenderer(Icon.class));
 
 	//lessonTable.getColumn("C").setCellEditor(lessonTable.getDefaultEditor(Icon.class));
 	lessonTable.getColumn("C").setCellRenderer(lessonTable.getDefaultRenderer(Icon.class));
 
 
     
     }
 
     public void deleteLesson(int rowToRemove){
 	((DefaultTableModel)lessonTable.getModel()).removeRow(rowToRemove);
     }
 
     private class AddLessonButtonAdapter extends MouseAdapter{
 	public void mouseClicked(MouseEvent e){
 	}
 	public void mousePressed(MouseEvent e){
 	}
     }
    
    public void clearLessonTableContent(){
	DefaultTableModel mtl = (DefaultTableModel)lessonTable.getModel();
	int row = mtl.getRowCount();
	while(--row >= 0){
	    mtl.removeRow(row);
	}
	sum_lesson = 0;
    }
 
     public void setLearnedStatue(String lessonName){
 	for(int i=0; i<sum_lesson; i++){
 	    if (lessonTable.getValueAt(i, 1).equals(lessonName)){
 		lessonTable.setValueAt("", sum_lesson-1, 0);
 		lessonTable.setValueAt(lessonName, sum_lesson-1, 1);
 		lessonTable.setValueAt(new ImageIcon(FilenameUtils.separatorsToSystem("resource/Smile.png")), sum_lesson-1, 2);
 	    }
 	}
     }
 
     private class HistoryLabelAdapter extends MouseAdapter{
 
 	public void mouseClicked(MouseEvent e){
 	}
 	public void mousePressed(MouseEvent e){
 	    buildHistoryPanel();
 	}
     }
 
     private class ListLabelAdapter extends MouseAdapter{
 	public void mouseClicked(MouseEvent e){
 	}
 	public void mousePressed(MouseEvent e){
 	    buildLessonPanel();
 	}
     }
 
     private void buildHistoryPanel(){
 
 	historyPanel = HistoryPanel.getHistoryPanel();
 	if (lessonPanel != null) getContentPane().remove(lessonPanel);
 	repaint();
 	//historyPanel.setBounds(x, y, width, height)
 	getContentPane().add(historyPanel);
     }
 
     private void buildLessonPanel(){
 	lessonPanel = LessonPanel.getLessonPanel();
 	if (historyPanel != null) getContentPane().remove(historyPanel);
 	repaint();
 	getContentPane().add(lessonPanel);
     }
 
 
     public static void main(String args[]){
 	MainFrame test = new MainFrame();
 	test.setVisible(true);
 	test.addLesson("Lesson_1");
 	test.setLearnedStatue("Lesson_1");
 	test.addLesson("Lesson_2");
     }
 }
