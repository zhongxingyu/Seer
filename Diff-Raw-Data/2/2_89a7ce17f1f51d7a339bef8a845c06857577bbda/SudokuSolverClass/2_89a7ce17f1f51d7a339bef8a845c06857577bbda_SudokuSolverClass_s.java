 import javax.swing.*;
 import javax.swing.filechooser.FileFilter;
 import java.awt.event.*;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 
 public class SudokuSolverClass extends JDialog {
     private JPanel contentPane;
     private JButton buttonOK;
     private JButton buttonCancel;
     private JTextField textField1;
     private JButton selFileBut;
     private JTextArea textArea1;
     private JTextArea textArea2;
     private JTextArea textArea3;
     private JTextArea textArea4;
     private JTextArea textArea5;
     private JTextArea textArea6;
     private JTextArea textArea7;
     private JTextArea textArea8;
     private JTextArea textArea9;
     private final int[] field = new int[81];
 
     private boolean checkHLine (int lineNum, int[] tmpField) {
         ArrayList<Integer> lst = new ArrayList<Integer>();
         for (int i = 0; i < 9; i ++) {
             if (tmpField[lineNum * 9 + i] == 0) continue;
             if (lst.contains(tmpField[lineNum * 9 + i])) {
                 return false;
             } else {
                 lst.add(tmpField[lineNum * 9 + i]);
             }
         }
         return true;
     }
 
     private boolean checkVLine (int lineNum, int[] tmpField) {
         ArrayList<Integer> lst = new ArrayList<Integer>();
         for (int i = 0; i < 9; i ++) {
             if (tmpField[9 * i + lineNum] == 0) continue;
             if (lst.contains(tmpField[9 * i + lineNum])) {
                 return false;
             } else {
                 lst.add(tmpField[9 * i + lineNum]);
             }
         }
         return true;
     }
 
     @SuppressWarnings("BooleanMethodIsAlwaysInverted")
     private boolean checkSquare (int pos, int[] tmpField) {
         ArrayList<Integer> lst = new ArrayList<Integer>();
         int squareNum = getSquareByAbsPos(pos);
         for (int i = 0; i < 9; i ++) {
             if (tmpField[getAbsPosBySquare(squareNum, i)] == 0) continue;
             if (lst.contains(tmpField[getAbsPosBySquare(squareNum, i)])) {
                 return false;
             } else {
                 lst.add(tmpField[getAbsPosBySquare(squareNum, i)]);
             }
         }
         return true;
     }
 
     private int getAbsPosBySquare (int squareNum, int pos) {
         int retVal;
         int auxY = ((squareNum / 3) * 3);
         int auxX = ((squareNum % 3) * 3);
         int position = (pos / 3) * 9 + (pos % 3);
         retVal = auxX + (auxY * 9) + position;
         return retVal;
     }
 
     private int getSquareByAbsPos (int pos) {
         int retVal;
         int auxX = (pos % 9) / 3;
         int auxY = (pos / 9) / 3;
         retVal = auxY * 3 + auxX;
         return retVal;
     }
 
     @SuppressWarnings("BooleanMethodIsAlwaysInverted")
     private boolean sudokuSolve () {
         int[] fieldTmp = field.clone();
         ArrayList<Integer> al = initValues();
         if (al == null) return false;
         tryValues(0, al, 0, fieldTmp);
 
         return fieldFilled();
     }
 
     private ArrayList<Integer> initValues() {
         ArrayList<Integer> retVal = new ArrayList<Integer>();
         for (int i = 0; i < 9; i ++) {
             for (int j = 0; j < 9; j ++) {
                 retVal.add(i+1);
             }
         }
 
         for (int i = 0; i < 81; i ++) {
             if (field[i] == 0) continue;
             if (!retVal.contains(field[i])) return null;
             retVal.remove(retVal.indexOf(field[i]));
         }
         return retVal;
     }
 
     private boolean fieldFilled() {
 
         for (int i = 0; i < 81; i ++) {
             if (field[i] == 0) return false;
         }
         return true;
     }
 
     @SuppressWarnings("ConstantConditions")
     private void sudokuSolutionShow () {
         ArrayList<ArrayList<Integer>> squares = new ArrayList<ArrayList<Integer>>();
         for (int i = 0; i < 9; i ++) {
             squares.add(new ArrayList<Integer>());
         }
         for (int i = 0; i < 81; i ++) {
             int squareNum = getSquareByAbsPos(i);
             squares.get(squareNum).add(field[i]);
         }
         for (int i = 0; i < 9; i ++) {
             StringBuilder sb = new StringBuilder();
             for (int j = 0; j < 9; j ++) {
                 if (j == 3 || j == 6) {
                     sb.append('\n');
                 }
                 int val = squares.get(i).get(j);
                 sb.append(val == 0 ? "*" : val);
                 sb.append(' ');
             }
             switch (i) {
                 case 0: {
                     textArea1.setText(sb.toString());
                     continue;
                 }
                 case 1: {
                     textArea2.setText(sb.toString());
                     continue;
                 }
                 case 2: {
                     textArea3.setText(sb.toString());
                     continue;
                 }
                 case 3: {
                     textArea4.setText(sb.toString());
                     continue;
                 }
                 case 4: {
                     textArea5.setText(sb.toString());
                     continue;
                 }
                 case 5: {
                     textArea6.setText(sb.toString());
                     continue;
                 }
                 case 6: {
                     textArea7.setText(sb.toString());
                     continue;
                 }
                 case 7: {
                     textArea8.setText(sb.toString());
                     continue;
                 }
                 case 8: {
                     textArea9.setText(sb.toString());
                 }
             }
 
         }
     }
 
     private void fillField () throws IOException {
         File inp = new File(textField1.getText());
         FileReader inpStream = null;
         try {
             inpStream = new FileReader(inp);
             int i = 0;
             int buf;
             char[] tmp = new char[1];
             while ((buf = inpStream.read()) >= 0) {
                 if (buf != '\n' && i < 81) {
                     if (buf != '*') {
                         tmp[0] = (char)buf;
                         field[i ++] = Integer.parseInt(new String(tmp));
                     } else {
                         field[i ++] = 0;
                     }
                 }
             }
         } finally {
             if (inpStream != null) {
                 inpStream.close();
             }
 
         }
     }
 
     public SudokuSolverClass() {
         setContentPane(contentPane);
         setModal(true);
         getRootPane().setDefaultButton(selFileBut);
 
         buttonOK.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 onOK();
             }
         });
 
         buttonCancel.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 onCancel();
             }
         });
 
 // call onCancel() when cross is clicked
         setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
         addWindowListener(new WindowAdapter() {
             public void windowClosing(WindowEvent e) {
                 onCancel();
             }
         });
 
 // call onCancel() on ESCAPE
         contentPane.registerKeyboardAction(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 onCancel();
             }
         }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
 
         this.setResizable(false);
 
         selFileBut.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent actionEvent) {
                 JFileChooser fc = new JFileChooser();
                 fc.setFileFilter(new FileFilter() {
                     @Override
                     public boolean accept(File file) {
                         if (file.isDirectory()) return true;
                         String[] filenameParts = file.getName().split("\\.");
                         return filenameParts[filenameParts.length - 1].toLowerCase().equals("sud");
                     }
 
                     @Override
                     public String getDescription() {
                         return "Sudoku files";
                     }
                 });
 
                 if (fc.showDialog(selFileBut, "") == JFileChooser.APPROVE_OPTION) {
                     textField1.setText(fc.getSelectedFile().getAbsolutePath());
 
                     try{
                         fillField();
                     } catch (Exception e) {
                        JOptionPane.showMessageDialog(getRootPane(), "Somewhat went wrong", "Error", JOptionPane.ERROR_MESSAGE);
                     }
                     sudokuSolutionShow();
                     getRootPane().setDefaultButton(buttonOK);
                 }
             }
         });
     }
 
     private void onOK() {
         if (!(textField1.getText().equals(""))) {
             if (!sudokuSolve()) {
                 JOptionPane.showMessageDialog(getRootPane(), "Seems like this is not solvable...", "Can not solve", JOptionPane.WARNING_MESSAGE);
             }
             sudokuSolutionShow();
 
         }
     }
 
     @SuppressWarnings({"unchecked"})
     private static synchronized <ArrayList> ArrayList somethingToArLInt (Object obj) {
         return (ArrayList) obj;
     }
 
     private boolean tryValues (int pos, ArrayList<Integer> values, int iteration, int[] fieldTmp) {
         if (values.size() == 1) {
             int[] field2Tmp = fieldTmp.clone();
             field2Tmp[pos] = values.get(0);
             if (checkHLine(pos / 9, field2Tmp) && checkVLine(pos % 9, field2Tmp)) {
                 System.arraycopy(field2Tmp, 0, field, 0, 81);
                 return true;
             } else {
                 return false;
             }
         } else {
             if (iteration > values.size() - 1) {
                 return false;
             }
             if (field[pos] != 0) {
                 return tryValues(pos + 1, values, 0, fieldTmp);
             }
             int[] field2Tmp = fieldTmp.clone();
             field2Tmp[pos] = values.get(iteration);
             ArrayList<Integer> ar2;
             ar2 = somethingToArLInt(values.clone());
             if (ar2 != null) ar2.remove(iteration);
             if (!checkHLine(pos / 9, field2Tmp) || !checkVLine(pos % 9, field2Tmp) || !checkSquare(pos, field2Tmp)) {
                 int currentValue = values.get(iteration);
                 int i = 1;
                 while (!values.contains(currentValue + i) && (currentValue + i) < 10) {
                     i ++;
                 }
                 if (currentValue + i > 9) return false;
                 int newIteration = values.indexOf(currentValue + i);
                 return tryValues(pos, values, newIteration, fieldTmp);
             }
             boolean resolved = tryValues(pos + 1, ar2, 0, field2Tmp);
             if (resolved) return true;
             //noinspection ConstantConditions
             if (!resolved) {
                 int currentValue = values.get(iteration);
                 int i = 1;
                 while (!values.contains(currentValue + i) && (currentValue + i) < 10) {
                     i ++;
                 }
                 if (currentValue + i > 9) return false;
                 int newIteration = values.indexOf(currentValue + i);
                 return tryValues(pos, values, newIteration, fieldTmp);
             }
         }
         return false;
     }
 
     private void onCancel() {
 // add your code here if necessary
         dispose();
     }
 
     public static void sscMain() {
         SudokuSolverClass dialog = new SudokuSolverClass();
         dialog.pack();
         dialog.setVisible(true);
         System.exit(0);
     }
 }
