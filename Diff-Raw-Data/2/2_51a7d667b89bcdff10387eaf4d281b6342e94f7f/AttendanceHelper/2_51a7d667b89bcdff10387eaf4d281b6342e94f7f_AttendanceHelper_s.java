 package attendancehelper;
 import java.awt.*;
 import java.awt.event.*;
 import java.io.*;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.*;
 import javax.swing.*;
 public class AttendanceHelper implements ActionListener{
     private String version = "Beta v1.1";
     private JFrame frame;
     private JPanel contentPane;
     private JPanel presentPanel = new JPanel();
     private ArrayList<JCheckBox> presentBoxList = new ArrayList();
     private JPanel centerPanel = new JPanel();
     private JPanel absentPanel = new JPanel();
     private ArrayList<JCheckBox> absentBoxList = new ArrayList();
     private JPanel latePanel = new JPanel();
     private JPanel tutorPanel = new JPanel();
     private ArrayList<JCheckBox> lateBoxList = new ArrayList();
     private HashMap<String,Integer> nameMap = new HashMap();
     private HashMap<Integer,String> numberMap = new HashMap();
     private ArrayList nameList = new ArrayList();
     private JMenuItem classchange = new JMenuItem("Change Class");
     private String teacherRaw = System.getProperty("user.name");
     private String teacher;
     private HintTextField tutorNumberField = new HintTextField("Student Number");
     private HintTextField tutorLastField = new HintTextField("Last Name");
     private HintTextField tutorFirstField = new HintTextField("First Name");
     private String[] spinnerSetup = {"Absent","Late","Present"};
     private SpinnerListModel spinnerSetup2 = new SpinnerListModel(spinnerSetup);
     private JSpinner tutorSpinner = new JSpinner(spinnerSetup2);
     private String block = "?";
     private File courseFile;
     private File studentFile;
    private File classesStore = new File("\\classes");
     private File outFile = new File("\\\\ndss-ts1.ndss.sd68.bc.ca\\winapps\\Office PowerPoint\\Attendance\\Attendance_Helper_Output.txt");
     File dir = new File("S:\\Remark\\Attendance\\");
     File backupdir = new File("H:\\My Documents\\AttendanceHelper\\");
     File backupdir2 = new File("C:\\AttendanceHelper\\");
     File[] files;
     DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
     Date dateDate = new Date();
     String date = dateFormat.format(dateDate);
     @Override
     public void actionPerformed(ActionEvent e) {
         ListIterator<JCheckBox> pit = presentBoxList.listIterator();
         ListIterator<JCheckBox> ait = absentBoxList.listIterator();
         ListIterator<JCheckBox> lit = lateBoxList.listIterator();
         Object source = e.getSource();
         while(pit.hasNext()){
             JCheckBox check = pit.next();
             if (check == source){
                 check.setSelected(false);
                 pit.remove();
                 ait.add(check);
             }
         }
         while(ait.hasNext()){
             JCheckBox check = ait.next();
             if (check == source){
                 ait.remove();
                 check.setSelected(false);
                 lit.add(check);
             }
         }
         while(lit.hasNext()){
             JCheckBox check = lit.next();
             if (check == source){
                 lit.remove();
                 check.setSelected(false);
                 pit.add(check);
             }
         }
         presentPanel.removeAll();
         absentPanel.removeAll();
         latePanel.removeAll();
         for(JCheckBox cb:presentBoxList){
                 presentPanel.add(cb);
         }
         for(JCheckBox cb:absentBoxList){
                 absentPanel.add(cb);
         }
         for(JCheckBox cb:lateBoxList){
                 latePanel.add(cb);
         }
         if (absentBoxList.isEmpty()){
             absentPanel.add(Box.createRigidArea(new Dimension(100,100)));
         }
         if (lateBoxList.isEmpty()){
             latePanel.add(Box.createRigidArea(new Dimension(100,100)));
         }
         frame.pack();
     }
     public static void main(String[] args) {
         AttendanceHelper me = new AttendanceHelper();
         me.start();
     }
     @SuppressWarnings("ResultOfMethodCallIgnored")
     private void start(){
         frame = new JFrame("AttendanceHelper");
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         contentPane = (JPanel)frame.getContentPane();
         contentPane.setLayout(new BorderLayout(6,6));
         makeMenus();
         makeContent();
         frame.pack();
         frame.setResizable(false);
         frame.setVisible(true);
         teacher = teacherRaw.substring(1,2).toUpperCase() + teacherRaw.substring(2, teacherRaw.length()).toLowerCase() + ", " + teacherRaw.substring(0,1).toUpperCase();
         files = dir.listFiles(new WildCardFileFilter("*("+teacher+").*"));
         try{
             if(files.length==2){
                 for(File f:files){
                     if(f.getName().indexOf(".gc")!=-1){
                         studentFile = f;
                     }else{
                         courseFile = f;
                     }
                 }
             }
         }catch(Exception e){
             JOptionPane.showMessageDialog(frame,"Some kind of error occurred with S:\\Remark\\Attendance\nNow attempting to use H:\\My Documents\\AttendanceHelper","ERROR",JOptionPane.ERROR_MESSAGE);
             JOptionPane.showMessageDialog(frame,e,"ERROR",JOptionPane.ERROR_MESSAGE);
             files = backupdir.listFiles(new WildCardFileFilter("*("+teacher+").*"));
             try{
                 for(File f:files){
                     if(f.getName().indexOf(".gc")!=-1){
                         studentFile = f;
                     }else{
                         courseFile = f;
                     }
                 }
             }catch(Exception ex){
                 JOptionPane.showMessageDialog(frame,"Some kind of error occurred with H:\\My Documents\\AttendanceHelper\nNow attempting to use C:\\AttendanceHelper\\","ERROR",JOptionPane.ERROR_MESSAGE);
                 JOptionPane.showMessageDialog(frame,ex,"ERROR",JOptionPane.ERROR_MESSAGE);
                 files = backupdir.listFiles(new WildCardFileFilter("*("+teacher+").*"));
                 for(File f:files){
                     if(f.getName().indexOf(".gc")!=-1){
                         studentFile = f;
                     }else{
                         courseFile = f;
                     }
                 }
             }
         }
         File in = studentFile;
         Scanner sc;
             try {
                 sc = new Scanner(in);
                 HashMap<String,Integer> classListCombo = new HashMap();
                 HashMap<Integer,String> courseToBlock = new HashMap();
                 String classListA = "";
                 String classListB = "";
                 String classListC = "";
                 String classListD = "";
                 while (sc.hasNextLine()){
                     String line = sc.nextLine();
                     String inCourseS;
                     int inCourse;
                     int inNumber;
                     String inName;
                     String[] inNameArray;
                     String inLast;
                     String inFirst;
                     if (line.length() >= 30 && "X01".equals(line.substring(0,3))){
                         if (isInt(line.substring(6,12))){
                             inNumber = Integer.parseInt(line.substring(6,12));
                         }else{
                             inNumber = 999999;
                         }
                         inCourseS = line.substring(32,40);
                         inCourseS.replaceAll(" ","");
                         int loops = 0;
                         String cout;
                         String out = "";
                         while (loops < inCourseS.length()){
                             cout = inCourseS.substring(loops,loops+1);
                             if("1".equals(cout)||"2".equals(cout)||"3".equals(cout)||"4".equals(cout)||"5".equals(cout)||"6".equals(cout)||"7".equals(cout)||"8".equals(cout)||"9".equals(cout)||"0".equals(cout)){
                                out = out + cout;
                             }
                             loops++;
                         }
                         inCourseS = out;
                         if (isInt(inCourseS)){
                             inCourse = Integer.parseInt(inCourseS);
                         }else{
                             inCourse = 9;
                         }
                         inName = line.substring(12,32);
                         inNameArray = inName.split(", ");
                         inLast = inNameArray[0];
                         inFirst = inNameArray[1];
                         inFirst.replaceAll(" ","");
                         classListCombo.put(inNumber + "!" + inLast + "!" + inFirst,inCourse);
                     }
                 }
                 Integer blockInt;
                 String blockString;
                 Integer courseInt;
                 in = courseFile;
                 sc = new Scanner(in);
                 while (sc.hasNextLine()){
                     String line = sc.nextLine();
                     String course = line.substring(1,6);
                     int loops = 0;
                     String cout;
                     String out = "";
                     while (loops != course.length()){
                         cout = course.substring(loops,loops+1);
                         if("1".equals(cout)||"2".equals(cout)||"3".equals(cout)||"4".equals(cout)||"5".equals(cout)||"6".equals(cout)||"7".equals(cout)||"8".equals(cout)||"9".equals(cout)||"0".equals(cout)){
                             out = out + cout;
                         }
                         loops++;
                     }
                     courseInt = Integer.parseInt(out);
                     blockInt = Integer.parseInt(line.substring(83,84));
                     switch (blockInt){
                         case 1: blockString = "A";
                             break;
                         case 2: blockString = "B";
                             break;
                         case 3: blockString = "C";
                             break;
                         case 4: blockString = "D";
                             break;
                         default: blockString = "invalid";
                             break;
                     }
                     if (!"invalid".equals(blockString)){
                         courseToBlock.put(courseInt,blockString);
                     }
                 }
                 int countA = 0;
                 int countB = 0;
                 int countC = 0;
                 int countD = 0;
                 int course;
                 String block2;
                 for(String student:classListCombo.keySet()){
                     course = classListCombo.get(student);
                     block2 = courseToBlock.get(course);
                     if("A".equals(block2)){
                         classListA = classListA + student + "\n";
                         countA++;
                     }else if ("B".equals(block2)){
                         classListB = classListB + student + "\n";
                         countB++;
                     }else if ("C".equals(block2)){
                         classListC = classListC + student + "\n";
                         countC++;
                     }else if ("D".equals(block2)){
                         classListD = classListD + student + "\n";
                         countD++;
                     }
                 }
                 if("".equals(classListA)){
                     classListA = "NOCLASS";
                 }
                 if("".equals(classListB)){
                     classListB = "NOCLASS";
                 }
                 if("".equals(classListC)){
                     classListC = "NOCLASS";
                 }
                 if("".equals(classListD)){
                     classListD = "NOCLASS";
                 }
                 if (!classesStore.exists()){
                     classesStore.mkdir();
                 }
                 PrintStream oFile;
                 oFile = new PrintStream(new File("classes/A.txt"));
                 oFile.print(classListA);
                 oFile.close();
                 oFile = new PrintStream(new File("classes/B.txt"));
                 oFile.print(classListB);
                 oFile.close();
                 oFile = new PrintStream(new File("classes/C.txt"));
                 oFile.print(classListC);
                 oFile.close();
                 oFile = new PrintStream(new File("classes/D.txt"));
                 oFile.print(classListD);
                 oFile.close();
             }catch (Exception ex) {
                 JOptionPane.showMessageDialog(frame,ex,"ERROR",JOptionPane.ERROR_MESSAGE);
                 System.out.println(ex.getStackTrace());
                 System.exit(1);
             }
     }
     private void makeMenus(){
         JMenuBar menuBar;
         JMenu menu;
         JMenuItem menuItem;
         menuBar = new JMenuBar();
         frame.setJMenuBar(menuBar);
         menu = new JMenu("File");
         menu.setMnemonic(KeyEvent.VK_F);
         menuBar.add(menu);
         classchange.setMnemonic(KeyEvent.VK_C);
         classchange.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C,Event.CTRL_MASK));
         classchange.addActionListener(new changeClassListener());
         menu.add(classchange);
         menuItem = new JMenuItem("Send to Office");
         menuItem.setMnemonic(KeyEvent.VK_S);
         menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,Event.CTRL_MASK));
         menuItem.addActionListener(new sendListener());
         menu.add(menuItem);
         menu.add(menuItem);
         menu.addSeparator();
         menuItem = new JMenuItem("Exit");
         menuItem.setMnemonic(KeyEvent.VK_X);
         menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W,Event.CTRL_MASK));
         menuItem.addActionListener(new exitListener());
         menu.add(menuItem);
     }
     private class changeClassListener implements ActionListener{
         @Override
         public void actionPerformed(ActionEvent e){
             boolean loop = true;
             while (loop){
                 Object[] options = {"A","B","C","D"};
                 int blockInt = JOptionPane.showOptionDialog(frame,
                     "Which Block?",
                     "Choose A Block",
                     JOptionPane.YES_NO_CANCEL_OPTION,
                     JOptionPane.QUESTION_MESSAGE,
                     null,
                     options,
                 options[0]);
                 String blockString;
                 switch (blockInt){
                     case 0: blockString = "A";
                         break;
                     case 1: blockString = "B";
                         break;
                     case 2: blockString = "C";
                         break;
                     case 3: blockString = "D";
                         break;
                     default: blockString = "invalid";
                         break;
                 }
                 block = blockString;
                 blockString = "classes/" + blockString + ".txt";
                 File in = new File(blockString);
                 if (in.exists()){
                     try {
                         Scanner scan = new Scanner(in);
                         Integer studentNumber;
                         String lastName;
                         String firstName;
                         String Line;
                         String lineArray[];
                         nameMap.clear();
                         numberMap.clear();
                         nameList.clear();
                         while (scan.hasNext()){
                             Line = scan.nextLine();
                             if("NOCLASS".equals(Line)){
                                 JOptionPane.showMessageDialog(frame,"You don't have a class that block","ERROR",JOptionPane.ERROR_MESSAGE);
                                 presentPanel.add(Box.createRigidArea(new Dimension(100,100)));
                             }else{
                                 lineArray = Line.split("!");
                                 studentNumber = Integer.parseInt(lineArray[0]);
                                 lastName = lineArray[1];
                                 firstName = lineArray[2];
                                 nameMap.put(lastName+", "+firstName,studentNumber);
                                 numberMap.put(studentNumber,lastName+", "+firstName);
                                 nameList.add(lastName+", "+firstName);
                             }
                         }
                     } catch (Exception exception) {
                         JOptionPane.showMessageDialog(frame,exception,"ERROR",JOptionPane.ERROR_MESSAGE);
                         System.out.println(exception.getStackTrace());
                         System.exit(1);
                     }
                     loop = false;
                 }else{
                     JOptionPane.showMessageDialog(frame,"There is no valid class list for that block.\nPlease try again.","ERROR",JOptionPane.ERROR_MESSAGE);
                 }
             }
             makeCheckBoxes();
         }
     }
     private class sendListener implements ActionListener{
         @Override
         public void actionPerformed(ActionEvent e){
             PrintStream oFile = null;
             try {
                 String out = "GENERATED AT: "+date+"\nTEACHER: "+teacher+"\nBLOCK: "+block+"\nABSENT ("+absentBoxList.size()+" STUDENT";
                 if (absentBoxList.size() != 1){
                     out = out + "S";
                 }
                 out = out + "):\n";
                 String outName;
                 Integer outNumber;
                 java.util.List<Integer> outNumberArray = new ArrayList<Integer>();
                 for (JCheckBox cb:absentBoxList){
                     outName = cb.getText();
                     outNumber = nameMap.get(outName);
                     outNumberArray.add(outNumber);
                 }
                 Collections.sort(outNumberArray);
                 for (Integer num:outNumberArray){
                     outName = numberMap.get(num);
                     out = out + "    " + num + ": " + outName + "\n";
                 }
                 out = out + "LATE ("+lateBoxList.size()+" STUDENT";
                 if (lateBoxList.size() != 1){
                     out = out + "S";
                 }
                 out = out + "):\n";
                 for (JCheckBox cb:lateBoxList){
                     outName = cb.getText();
                     outNumber = nameMap.get(outName);
                     outNumberArray.add(outNumber);
                 }
                 Collections.sort(outNumberArray);
                 for (Integer num:outNumberArray){
                     outName = numberMap.get(num);
                     out = out + "    " + num + ": " + outName + "\n";
                 }
                 if (isInt(tutorNumberField.getText())){
                     out = out + "ADDITIONAL STUDENT:\n" + "    " + tutorNumberField.getText() + ": " + tutorLastField.getText() + ", " + tutorFirstField.getText() + " (" + (tutorSpinner.getValue() + "").toUpperCase() + ")\n";
                 }
                 out = out + "----------END ATTENDANCE REPORT----------\n";
                 oFile = new PrintStream(new BufferedOutputStream(new FileOutputStream(outFile,true)));
                 oFile.print(out);
                 oFile.close();
             }catch (Exception exception) {
                 JOptionPane.showMessageDialog(frame,exception,"ERROR",JOptionPane.ERROR_MESSAGE);
                 System.out.println(exception.getStackTrace());
                 System.exit(1);
             } finally {
                 oFile.close();
             }
         }
     }
     public boolean isInt(String in){
         try{
             Integer.parseInt(in);
             return true;
         }catch(Exception e){
             return false;
         }
     }
     private class exitListener implements ActionListener{
         @Override
         public void actionPerformed(ActionEvent e){
             System.exit(0);
             }
     }
     private void makeContent(){
         makeNorthRegion();
         makeWestRegion();
         makeCenterRegion();
         tutorPanel.add(new JLabel("Add another student?",JLabel.CENTER));
         tutorPanel.add(tutorNumberField);
         tutorPanel.add(tutorLastField);
         tutorPanel.add(tutorFirstField);
         tutorSpinner.setValue("Present");
         tutorPanel.add(tutorSpinner);
         contentPane.add(tutorPanel,BorderLayout.SOUTH);
     }
     private void makeNorthRegion(){
         JLabel titleLabel = new JLabel("Attendance Helper Client "+version,JLabel.CENTER);
         contentPane.add(titleLabel,BorderLayout.NORTH);
     }
     private void makeWestRegion(){
         presentPanel.setLayout(new BoxLayout(presentPanel,BoxLayout.Y_AXIS));
         presentPanel.setBorder(BorderFactory.createTitledBorder("Present Students"));
         contentPane.add(presentPanel,BorderLayout.WEST);
         presentPanel.add(Box.createRigidArea(new Dimension(100,100)));
     }
     private void makeCenterRegion(){
         centerPanel.setLayout(new BoxLayout(centerPanel,BoxLayout.Y_AXIS));
         absentPanel.setBorder(BorderFactory.createTitledBorder("Absent Students"));
         centerPanel.add(absentPanel);
         latePanel = new JPanel();
         latePanel.setBorder(BorderFactory.createTitledBorder("Late Students"));
         centerPanel.add(latePanel);
         contentPane.add(centerPanel,BorderLayout.CENTER);
         latePanel.add(Box.createRigidArea(new Dimension(100,100)));
         absentPanel.add(Box.createRigidArea(new Dimension(100,100)));
     }
     private void makeCheckBoxes(){
         presentPanel.removeAll();
         absentPanel.removeAll();
         latePanel.removeAll();
         presentBoxList.clear();
         absentBoxList.clear();
         lateBoxList.clear();
         tutorNumberField.setText("Student Number");
         tutorLastField.setText("Last Name");
         tutorFirstField.setText("First Name");
         tutorSpinner.setValue("Present");
         Component boxthing = Box.createRigidArea(new Dimension(100,100));
         presentPanel.add(boxthing);
         for(Object name:nameList){
             presentPanel.remove(boxthing);
             JCheckBox checkBox = new JCheckBox(name.toString());
             presentBoxList.add(checkBox);
             presentPanel.add(checkBox);
             checkBox.addActionListener(this);
         }
         frame.pack();
     }
 }
