 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package GUI;
 
 import java.awt.CardLayout;
 import java.awt.Color;
 import java.awt.Desktop;
 import java.awt.HeadlessException;
 import java.io.IOException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.sql.SQLException;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.UIManager;
 import javax.swing.table.DefaultTableModel;
 import users.Controller;
 import javax.swing.JSplitPane;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.GregorianCalendar;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Vector;
 import tables.Book;
 import tables.BookCopy;
 import tables.BookCopyEvilTwinException;
 import tables.Borrower;
 import tables.Borrowing;
 import tables.Fine;
 import tables.NoPaymentException;
 import tables.NoSuchCopyException;
 import tables.Table;
 import users.FineRequiredException;
 
 /*
  * ViewFrame.java
  *
  * Created on 16-Nov-2011, 4:09:38 PM
  * 
  * TODO maybe changed DefaultTableModel to DefaultTableModel
  */
 
 /**
  * 
  * @author Mitch
  */
 public class ViewFrame extends javax.swing.JFrame {
 	/** Creates new form ViewFrame */
 	public ViewFrame() {
 		this.controller = new Controller(this);
 		statemap = new HashMap<String, State>();
 		statemap.put(START_STRING, State.START);
 		statemap.put(TABLES, State.TABLES);
 		statemap.put(SEARCH, State.SEARCH);
 		statemap.put(CHECK_ACCOUNT, State.CHECK_ACCOUNT);
 		statemap.put(HOLD_REQUEST, State.HOLD_REQUEST);
 		statemap.put(PAY_FINE, State.PAY_FINE);
 		statemap.put(ADD_BORROWER, State.ADD_BORROWER);
 		statemap.put(CHECK_OUT, State.CHECK_OUT);
 		statemap.put(CHECK_OVERDUE, State.CHECK_OVERDUE);
 		statemap.put(PROCESS_RETURN, State.PROCESS_RETURN);
 		statemap.put(ADD_BOOK, State.ADD_BOOK);
 		statemap.put(ADD_COPY, State.ADD_COPY);
 		statemap.put(REMOVE_BOOK, State.REMOVE_BOOK);
 		statemap.put(REMOVE_BORROWER, State.REMOVE_BORROWER);
 		statemap.put(REPORT_POPULAR, State.REPORT_POPULAR);
 		statemap.put(REPORT_CHECKED_OUT, State.REPORT_CHECKED_OUT);
 		initComponents();
 		CardLayout cl_cardPanel = (CardLayout) cardPanel.getLayout();
 		state = State.START;
 		cl_cardPanel.show(cardPanel, START_STRING);
 		doButton.setText("Go");
                 setSize(800,600);
 	}
 
   private boolean addBook() throws NumberFormatException {
     // check user input using a regular expression
     abOpStatus.setForeground(Color.BLACK);
     abOpStatus.setBackground(Color.WHITE);
     abOpStatus.setText("...");
     String regex1 = "([0-9]+|[a-z]+|[A-Z]+|[\\s]+)+";
     if (!abCN.getText().matches(regex1)
                     || !abCN.getText().matches(regex1)
                     || !abISBN.getText().matches(regex1)
                     || !abTitle.getText().matches(regex1)
                     || !abMA.getText().matches(regex1)
                     || !abPub.getText().matches(regex1)
                     || !abYear.getText().matches(regex1)
                     //|| !abAA.getText().matches(regex1)
                     || !abSubs.getText().matches(regex1)
                     || !abSpinner.getValue().toString().matches(regex1)) {
       abOpStatus.setForeground(Color.RED);
       abOpStatus.setBackground(Color.WHITE);
       abOpStatus
                       .setText("Looks like you have bad input, please check again");
       return true;
     }
     String onlyNumberRegex = "[0-9]+";
     if (!abYear.getText().matches(onlyNumberRegex)) {
       abOpStatus.setText("Year Must be A number");
       abOpStatus.setBackground(Color.RED);
       return true;
     }
     Book bTest = new Book();
     bTest.setCallNumber(abCN.getText());
     try {
       if (bTest.checkExists()) {
         abOpStatus.setText("Book Exists");
         abOpStatus.setForeground(Color.red);
         return true;
       }
     } catch (SQLException ex) {
       Logger.getLogger(ViewFrame.class.getName()).log(Level.SEVERE,
                       null, ex);
       abOpStatus.setText("Sql exception");
       abOpStatus.setForeground(Color.red);
       return true;
     }
     // create the bopk object with user input
     Book b = new Book();
     b.setCallNumber(abCN.getText().trim().toUpperCase());
     b.setIsbn(abISBN.getText());
     b.setMainAuthor(abMA.getText());
     b.setTitle(abTitle.getText());
     b.setPublisher(abPub.getText());
     b.setYear(Integer.parseInt(abYear.getText()));
     // add additional authors
     String aa = abAA.getText();
     ArrayList<String> aLAA = new ArrayList<String>();
     aLAA.add(b.getMainAuthor());
     if (aa.length() >= 1)
     {
       // deliniate the string into sep. objects
       String[] aaArray = aa.split(",", 0);
       // create the array list from string[]\
       for (int i = 0; i < aaArray.length; i++) {
               aLAA.add(aaArray[i]);
       }
       // set the book objects array list of additional authors
     }
     b.setAuthors(aLAA);
     // add additional subjects
     String subs = abSubs.getText();
     // deliniateString
     String[] subsArray = subs.split(",");
     ArrayList<String> aLSubs = new ArrayList<String>();
     // create the array list from string[]
     for (int i = 0; i < subsArray.length; i++) {
             aLSubs.add(subsArray[i]);
     }
     // set the book objects array list of subjects
     b.setSubjects(aLSubs);
     // try inserting the book into the database ( which also inserts the
     // subjects and authors)
     try {
       b.insert();
     } catch (SQLException ex) {
       Logger.getLogger(ViewFrame.class.getName()).log(Level.SEVERE,
                       null, ex);
       abOpStatus.setForeground(Color.red);
       abOpStatus.setText("error: " + ex.getErrorCode());
       return true;
     }
     // add book copies
     Object copiesAmount = abSpinner.getValue();
     int test = Integer.parseInt(copiesAmount.toString());
     for (int i = 0; i < test; i++) {
             try {
                     BookCopy bC = new BookCopy("C" + Integer.toString(i), b,
                                     "in");
                     bC.insert();
             } catch (SQLException ex) {
                     Logger.getLogger(ViewFrame.class.getName()).log(
                                     Level.SEVERE, null, ex);
                     abOpStatus.setForeground(Color.red);
                     abOpStatus.setText("error: " + ex.getErrorCode());
                     break;
             }
     }
     // clean up UI
     String clear = "";
     abOpStatus.setBackground(Color.green);
     abOpStatus.setText(abSpinner.getValue().toString()
                     + " copies of the Book with callnumber " + abCN.getText()
                     + " have been added! ");
     abCN.setText(clear);
     abISBN.setText(clear);
     abTitle.setText(clear);
     abMA.setText(clear);
     abPub.setText(clear);
     abYear.setText(clear);
     abAA.setText(clear);
     abSubs.setText(clear);
     abSpinner.setValue(1);
     return false;
   }
 
   private boolean addBorrower() throws HeadlessException {
     try {
       Borrower borr = new Borrower();
       borr.setName(addBorrowerTextName.getText().trim());
       borr.setAddress(addBorrowerTextAddress.getText().trim());
       borr.setPhone(addBorrowerTextPhoneNo.getText().trim());
       borr.setEmailAddress(addBorrowerTextEmail.getText().trim());
       try {
         borr.setSinOrStNum(Integer.parseInt(addBorrowerTextSinOrStNo
                         .getText().trim()));
         if (borr.getSinOrStNum() < 0)
         {
           throw new NumberFormatException();
         }
       } catch (NumberFormatException e) {
         JOptionPane.showMessageDialog(this, "Not a valid SIN or Student Number", "Error", JOptionPane.ERROR_MESSAGE);
         return true;
       }
       borr.setPassword(addBorrowerTextPassword.getText().trim());
       int expiryDate = Integer.parseInt(expiryDateTextField.getText().trim());
       int expiryMonth = Integer.parseInt(expiryMonthTextField.getText().trim());
       expiryMonth--; // 0 is JANUARY
       int expiryYear = Integer.parseInt(expiryYearTextField.getText().trim());
       if (expiryDate <= 0 || expiryMonth <0 || expiryYear <=0 || expiryMonth>11 ||expiryDate > 31)
       {
         throw new NumberFormatException("Date must be positive numbers");
       }
       Calendar expiryDateCalendar = new GregorianCalendar(expiryYear, expiryMonth, expiryDate, 23, 59);
       DateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
       borr.setExpiryDate(expiryDateCalendar);
       borr.setType((String) addBorrowerComboBoxType.getSelectedItem());
       if (!borr.insert())
       {
         throw new SQLException("Insert failed.");
       }
       JOptionPane.showMessageDialog(this, "Borrower successfully added.\nBorrower ID "+borr.getBid()+" generated.", "Success", JOptionPane.INFORMATION_MESSAGE);
       clearButtonActionPerformed(null);
     }catch (SQLException e1) {
            JOptionPane.showMessageDialog(this, "Borrower could not be added.\n"+e1.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
    }catch (NumberFormatException expiryNfe)
     {
       JOptionPane.showMessageDialog(this, "Date entered incorrectly.", "Error", JOptionPane.ERROR_MESSAGE);
     }
     return false;
   }
 
   private boolean addCopy() throws HeadlessException, NumberFormatException {
     Book b1 = new Book();
     b1.setCallNumber(abcCN.getText());
     BookCopy bC1 = new BookCopy();
     bC1.setB(b1);
     bC1.setStatus("in");
     // fetch the last copy number given the above call number
     String lastCopyNumber;
     try {
       lastCopyNumber = bC1.getLatestCopyNo();
     } catch (SQLException ex) {
       Logger.getLogger(ViewFrame.class.getName()).log(Level.SEVERE,
                       null, ex);
       // prompt the user with popup box
       String admin = "It seems there is something wrong with the Database. :( Contact your administrator";
       JOptionPane.showMessageDialog(this, admin, "Manual",
                       JOptionPane.INFORMATION_MESSAGE);
       return true;
     }
     if (lastCopyNumber == null) {
       // promptuser
       String admin = "It looks like this book doesn't exist in database. Please add book first";
       JOptionPane.showMessageDialog(this, admin, "Manual",
                       JOptionPane.INFORMATION_MESSAGE);
       return true;
     }
     int lastCopyNum = Integer.parseInt(lastCopyNumber.substring(1));
     int numCopiesToAdd = Integer.parseInt(abcSpinner.getValue()
                     .toString());
     for (int i = 0; i < numCopiesToAdd; i++) {
             int newCopyNum = lastCopyNum + i + 1;
             bC1.setCopyNo("C" + newCopyNum);
             try {
                     bC1.insert();
             } catch (SQLException ex) {
                     Logger.getLogger(ViewFrame.class.getName()).log(
                                     Level.SEVERE, null, ex);
                     // prompt user of catastrophic error
                     String admin1 = "It seems there is something wrong with the Database. :( Contact your administrator";
                     JOptionPane.showMessageDialog(this, admin1, "Manual",
                                     JOptionPane.INFORMATION_MESSAGE);
                     break;
             }
     }
     // prompt user of success
     String success = "Success!";
     JOptionPane.showMessageDialog(this, success, "Manual",
                     JOptionPane.INFORMATION_MESSAGE);
     return false;
   }
 
   private void checkAccount() throws NumberFormatException, HeadlessException {
     try {
             String searchIdField = SearchIdField.getText();
 
             Borrower b = new Borrower();
             b.setBid(Integer.parseInt(searchIdField));
             b = b.get();
             ArrayList<String[][]> loi = b.checkAccount();
 
             String[][] lob2D = loi.get(0);
             String[][] lof2D = loi.get(1);
             String[][] loh2D = loi.get(2);
 
             String[] borHeader = lob2D[0];
             String[][] bor2DMinusHeader = new String[lob2D.length - 1][lob2D[0].length];
             for (int i = 0; i < lob2D.length - 1; i++) {
                     bor2DMinusHeader[i] = lob2D[i + 1];
             }
 
             String[] fineHeader = lof2D[0];
             String[][] fine2DMinusHeader = new String[lof2D.length - 1][lof2D[0].length];
             for (int i = 0; i < lof2D.length - 1; i++) {
                     fine2DMinusHeader[i] = lof2D[i + 1];
             }
 
             String[] holdHeader = loh2D[0];
             String[][] hold2DMinusHeader = new String[loh2D.length - 1][loh2D[0].length];
             for (int i = 0; i < loh2D.length - 1; i++) {
                     hold2DMinusHeader[i] = loh2D[i + 1];
             }
 
             // print 2d array
             DefaultTableModel uTMBorrowing = new DefaultTableModel(
                             bor2DMinusHeader, borHeader);
             checkedOutBooksTable.setModel(uTMBorrowing);
             checkedOutBooksTable.repaint();
 
             DefaultTableModel uTMFine = new DefaultTableModel(
                             fine2DMinusHeader, fineHeader);
             finesTable.setModel(uTMFine);
             finesTable.repaint();
 
             DefaultTableModel uTMHold = new DefaultTableModel(
                             hold2DMinusHeader, holdHeader);
             currentHoldsTable.setModel(uTMHold);
             currentHoldsTable.repaint();
 
             TabbedPane.repaint();
     }
     catch (NullPointerException npe)
     {
       JOptionPane.showMessageDialog(this, "Borrower does not exist.", "Error", JOptionPane.ERROR_MESSAGE);
     }
     catch (SQLException S) {
             JOptionPane.showMessageDialog(this, "Could not complete transaction.\n"+S.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
     }
   }
 
   private boolean checkOut() throws HeadlessException {
     //get a string representation of the table
     String[] callNos = new String[checkOutTableReceiptTable.getRowCount()];
     String[] copyNos = new String[checkOutTableReceiptTable.getRowCount()];
     for (int i = 0; i < callNos.length; i++) {
             callNos[i] = (String) checkOutTableReceiptTable.getValueAt(i, 1);
             copyNos[i] = (String) checkOutTableReceiptTable.getValueAt(i, 2);
     }
     String[][] results = null;
     try {
       results = controller.getSystemClerk().checkOutItems(Integer.parseInt((String) checkOutTableReceiptTable.getValueAt(0, 0)), callNos, copyNos);
     } catch (Exception e2) {
       // TODO Auto-generated catch block
       //TODO Need to fix this
      JOptionPane.showMessageDialog(this, e2.toString(), "Have a good day",
               JOptionPane.ERROR_MESSAGE);
       return true;
     }
     //"ITEM", "CALLNUMBER","COPYNO","TITLE","OUTDATE","DUEDATE"
     DefaultTableModel checkOutmodel = new DefaultTableModel();
     results = get2DArrayMinusHeader(results);
     String[] item = new String[results.length];
     String[] callNo = new String[results.length];
     String[] copyNo = new String[results.length];
     String[] title = new String[results.length];
     String[] outDate = new String[results.length];
     String[] dueDate = new String[results.length];
     for (int i = 0; i < results.length; i++) {
         int n = 0;
         item[i] = results[i][n++];
         callNo[i] = results[i][n++];
         copyNo[i] = results[i][n++];
         title[i] = results[i][n++];
         outDate[i] = results[i][n++];
         dueDate[i] = results[i][n++];
     }
     //redraw table to reflect receipt, set add button to inactive
     checkOutmodel.addColumn("Item", item);
     checkOutmodel.addColumn("Call Number", callNo);
     checkOutmodel.addColumn("Copy Number", copyNo);
     checkOutmodel.addColumn("Title", title);
     checkOutmodel.addColumn("Out Date", outDate);
     checkOutmodel.addColumn("Due Date", dueDate);
     checkOutTableReceiptTable.setModel(checkOutmodel);
     checkOutTableReceiptTable.repaint();
     JOptionPane.showMessageDialog(this, "The Borrower checked out.", "Have a good day",
                     JOptionPane.INFORMATION_MESSAGE);
     return false;
   }
 
   private void checkOverdue() throws HeadlessException {
     try {
             Borrower borr = new Borrower();
             Borrowing bwing = new Borrowing();
             BookCopy bc = new BookCopy();
             Collection<Table> lbw = bwing.getOverdue();
             HashMap<Borrower, BookCopy> overdue = new HashMap<Borrower, BookCopy>();
 
             String[] borrStr = new String[lbw.size()];
             String[] bcpyStr = new String[lbw.size()];
             String[] bcalStr = new String[lbw.size()];
             int i = 0;
 
             if (lbw.size() > 0) {
                     Iterator<Table> bwItr = lbw.iterator();
                     while (bwItr.hasNext()) {
                             bwing = (Borrowing) bwItr.next();
 
                             borr = new Borrower();
                             borr.setBid(bwing.getBorid());
                             borr = (Borrower) borr.get();
 
                             bc = bwing.getBookCopy();
                             bc.setStatus("overdue");
                             bc.update();
 
                             overdue.put(borr, bc);
                             borrStr[i] = borr.getName();
                             bcpyStr[i] = bc.getCopyNo();
                             bcalStr[i] = bc.getB().getCallNumber();
                             i++;
                     }
             }
 
             DefaultTableModel model = new DefaultTableModel();
             model.addColumn("Borrower Name", borrStr);
             model.addColumn("Call Number", bcalStr);
             model.addColumn("Copy Number", bcpyStr);
             
             checkOverdueTable.setModel(model);
 checkOverdueTable.repaint();
 
     } catch (SQLException e1) {
             JOptionPane.showMessageDialog(this, "Could not complete transaction.\n"+e1.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
     }
   }
 
 	private String[][] get2DArrayMinusHeader(String[][] TwoDArrayToPrint) {
 		String[][] TwoDMinusHeader = new String[TwoDArrayToPrint.length - 1][TwoDArrayToPrint[0].length];
 		for (int i = 0; i < TwoDArrayToPrint.length - 1; i++) {
 			TwoDMinusHeader[i] = TwoDArrayToPrint[i + 1];
 		}
 		return TwoDMinusHeader;
 	}
 
   private void holdRequest() throws HeadlessException {
     try {
             controller.getSystemBorrower()
                             .setBid(holdRequestPanel.getBid());
             controller.getSystemBorrower().placeHoldRequest(
                             holdRequestPanel.getCallNumber());
             String holdRequestSuccessMessage = "Your hold request has been placed.  You will be informed when the book is ready.";
             JOptionPane.showMessageDialog(this, holdRequestSuccessMessage,
                             "Success", JOptionPane.INFORMATION_MESSAGE);
             holdRequestPanel.clear();
     } catch (NumberFormatException nfe) {
             String holdRequestBidErrorMessage = "Invalid account ID.\nYour account ID is the number found on your library card.";
             JOptionPane.showMessageDialog(this, holdRequestBidErrorMessage,
                             "Error", JOptionPane.ERROR_MESSAGE);
     } catch (SQLException e) {
             String holdRequestErrorMessage = "Cannot place hold request at this time. Please try again later.\n"
                             + e.getMessage();
             JOptionPane.showMessageDialog(this, holdRequestErrorMessage,
                             "Error", JOptionPane.ERROR_MESSAGE);
     } finally {
             controller.getSystemBorrower().setBid(-1);
     }
   }
 
 	/**
 	 * This method is called from within the constructor to initialize the form.
 	 * WARNING: Do NOT modify this code. The content of this method is always
 	 * regenerated by the Form Editor.
 	 */
 	@SuppressWarnings("unchecked")
 	// <editor-fold defaultstate="collapsed"
 	// <editor-fold defaultstate="collapsed"
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
         java.awt.GridBagConstraints gridBagConstraints;
 
         removeBookButtonGroup = new javax.swing.ButtonGroup();
         mainPanel = new javax.swing.JPanel();
         cardPanel = new javax.swing.JPanel();
         startPanel = new javax.swing.JPanel();
         startComboBoxPanel = new javax.swing.JPanel();
         navigationComboBox = new javax.swing.JComboBox();
         tablesPanel = new javax.swing.JPanel();
         tableNamePanel = new javax.swing.JPanel();
         tablesComboBox = new javax.swing.JComboBox();
         viewTablePane = new javax.swing.JScrollPane();
         entitiesTable = new javax.swing.JTable();
         searchPanel = new javax.swing.JPanel();
         SearchTopPanel = new javax.swing.JPanel();
         SearchComboBox = new javax.swing.JComboBox();
         SearchTextField = new javax.swing.JTextField();
         jScrollPane1 = new javax.swing.JScrollPane();
         SearchTable = new javax.swing.JTable();
         checkAccountPanel = new javax.swing.JPanel();
         searchAccountPanel = new javax.swing.JPanel();
         SearchIdField = new javax.swing.JTextField();
         TabbedPane = new javax.swing.JTabbedPane();
         checkOutBooksPane = new javax.swing.JScrollPane();
         checkedOutBooksTable = new javax.swing.JTable();
         finesPane = new javax.swing.JScrollPane();
         finesTable = new javax.swing.JTable();
         currentHoldsPane = new javax.swing.JScrollPane();
         currentHoldsTable = new javax.swing.JTable();
         payFinePanel = new javax.swing.JPanel();
         payFineMsgLabel = new javax.swing.JLabel();
         payFineInputPanel = new javax.swing.JPanel();
         payFineFidLabel = new javax.swing.JLabel();
         payFineAmtLabel = new javax.swing.JLabel();
         payFineInstructionLabel = new javax.swing.JLabel();
         jLabel1 = new javax.swing.JLabel();
         paddingLabel = new javax.swing.JLabel();
         payFineFidTextField = new javax.swing.JTextField();
         payFineAmountTextField = new javax.swing.JTextField();
         checkOutPanel = new javax.swing.JPanel();
         checkOutFieldsPanel = new javax.swing.JPanel();
         checkOutLabelBorid = new javax.swing.JLabel();
         checkOutTextBorid = new javax.swing.JTextField();
         checkOutLabelCallNo = new javax.swing.JLabel();
         checkOutTextCallNo = new javax.swing.JTextField();
         checkOutLabelCopyNo = new javax.swing.JLabel();
         checkOutTextCopyNo = new javax.swing.JTextField();
         checkOutDummyLabel = new javax.swing.JLabel();
         //TODO add to checkout
         checkOutButtonAdd = new javax.swing.JButton();
         checkOutFieldsPanelReceipt = new javax.swing.JPanel();
         checkOutTableReceipt = new javax.swing.JScrollPane();
         checkOutTableReceiptTable = new javax.swing.JTable();
         addBorrowerPanel = new javax.swing.JPanel();
         addBorrowerLabelName = new javax.swing.JLabel();
         addBorrowerTextName = new javax.swing.JTextField();
         addBorrowerLabelAddress = new javax.swing.JLabel();
         addBorrowerTextAddress = new javax.swing.JTextField();
         addBorrowerLabelPhoneNo = new javax.swing.JLabel();
         addBorrowerTextPhoneNo = new javax.swing.JTextField();
         addBorrowerLabelEmail = new javax.swing.JLabel();
         addBorrowerTextEmail = new javax.swing.JTextField();
         addBorrowerLabelPassword = new javax.swing.JLabel();
         addBorrowerTextPassword = new javax.swing.JTextField();
         addBorrowerLabelSinOrStNo = new javax.swing.JLabel();
         addBorrowerTextSinOrStNo = new javax.swing.JTextField();
         addBorrowerLabelType = new javax.swing.JLabel();
         addBorrowerComboBoxType = new javax.swing.JComboBox();
         expiryDateLabel = new javax.swing.JLabel();
         expiryDateTextField = new javax.swing.JTextField();
         expiryMonthTextField = new javax.swing.JTextField();
         expiryYearTextField = new javax.swing.JTextField();
         checkOverduePanel = new javax.swing.JPanel();
         checkOverdueLabelBorrInfo = new javax.swing.JLabel();
         checkOverdueScrollPaneInfo = new javax.swing.JScrollPane();
         checkOverdueTable = new javax.swing.JTable();
         checkOverduePanelMessage = new javax.swing.JPanel();
         checkOverdueButtonMessage = new javax.swing.JButton();
         addNewBookPanel = new javax.swing.JPanel();
         abMainLabel = new javax.swing.JLabel();
         abMainPanel = new javax.swing.JPanel();
         abCNLabel = new javax.swing.JLabel();
         abCN = new javax.swing.JTextField();
         abISBNLabel = new javax.swing.JLabel();
         abISBN = new javax.swing.JTextField();
         abTitleLabel = new javax.swing.JLabel();
         abTitle = new javax.swing.JTextField();
         abMainAuthorLabel = new javax.swing.JLabel();
         abMA = new javax.swing.JTextField();
         abPubLabel = new javax.swing.JLabel();
         abPub = new javax.swing.JTextField();
         abYearLabel = new javax.swing.JLabel();
         abYear = new javax.swing.JTextField();
         abAALabel = new javax.swing.JLabel();
         abAA = new javax.swing.JTextField();
         abSubsLabel = new javax.swing.JLabel();
         abSubs = new javax.swing.JTextField();
         abSpinnerLabel = new javax.swing.JLabel();
         abSpinner = new javax.swing.JSpinner();
         abStatusPanel = new javax.swing.JPanel();
         abOpStatusLabel = new javax.swing.JLabel();
         abOpStatus = new javax.swing.JTextField();
         addNewCopyPanel = new javax.swing.JPanel();
         abcMainPanel = new javax.swing.JPanel();
         abcCNLabel = new javax.swing.JLabel();
         abcSpinnerLabel = new javax.swing.JLabel();
         abcCN = new javax.swing.JTextField();
         abcSpinner = new javax.swing.JSpinner();
         abcMainPanelLabel = new javax.swing.JLabel();
         removeBorrowerPanel = new javax.swing.JPanel();
         removeBorrowerInputPanel = new javax.swing.JPanel();
         removeBorrowerLabel = new javax.swing.JLabel();
         removeBorrowerTextField = new javax.swing.JTextField();
         removeBookPanel = new javax.swing.JPanel();
         removeBookCallNumberPanel = new javax.swing.JPanel();
         removeBookCallNumberLabel = new javax.swing.JLabel();
         removeBookPrimaryNoTextField = new javax.swing.JTextField();
         removeBookSecondaryNoTextField = new javax.swing.JTextField();
         removeBookYearTextField = new javax.swing.JTextField();
         removeBookRadioButtonPanel = new javax.swing.JPanel();
         removeBookBookRadioButton = new javax.swing.JRadioButton();
         removeBookWhichCopiesTextField = new javax.swing.JTextField();
         removeBookOnlyTheseCopiesRadioButton = new javax.swing.JRadioButton();
         popularReportPanel = new javax.swing.JPanel();
         popularReportOptionsPanel = new javax.swing.JPanel();
         popularReportYearSelectLabel = new javax.swing.JLabel();
         popularReportYearTextField = new javax.swing.JTextField();
         popularReportNSelectLabel = new javax.swing.JLabel();
         popularReportNTextField = new javax.swing.JTextField();
         popularReportTablePanel = new javax.swing.JPanel();
         popularReportTablePane = new javax.swing.JScrollPane();
         popularReportTable = new javax.swing.JTable();
         checkedOutReportPanel = new javax.swing.JPanel();
         checkedOutReportFilterPanel = new javax.swing.JPanel();
         checkedOutReportFilterCheckBox = new javax.swing.JCheckBox();
         checkedOutReportTextField = new javax.swing.JTextField();
         checkedOutReportTablePanel = new javax.swing.JPanel();
         checkedOutReportTablePane = new javax.swing.JScrollPane();
         checkedOutReportTable = new javax.swing.JTable();
         holdRequestPanel = new GUI.PlaceHoldRequestPanel();
         processReturnPanel = new GUI.ProcessReturnPanel();
         buttonPanel = new javax.swing.JPanel();
         doButton = new javax.swing.JButton();
         clearButton = new javax.swing.JButton();
         menuBar = new javax.swing.JMenuBar();
         fileMenu = new javax.swing.JMenu();
         reconnectMenuItem = new javax.swing.JMenuItem();
         quitMenuItem = new javax.swing.JMenuItem();
         navigationMenu = new javax.swing.JMenu();
         borrowerMenu = new javax.swing.JMenu();
         searchMenuItem = new javax.swing.JMenuItem();
         checkAccountMenuItem = new javax.swing.JMenuItem();
         holdRequestMenuItem = new javax.swing.JMenuItem();
         payFineMenuItem = new javax.swing.JMenuItem();
         clerkMenu = new javax.swing.JMenu();
         addBorrowerMenuItem = new javax.swing.JMenuItem();
         checkOutMenuItem = new javax.swing.JMenuItem();
         processReturnMenuItem = new javax.swing.JMenuItem();
         checkOverdueMenuItem = new javax.swing.JMenuItem();
         librarianMenu = new javax.swing.JMenu();
         addMenu = new javax.swing.JMenu();
         addBookMenuItem = new javax.swing.JMenuItem();
         addBookCopyMenuItem = new javax.swing.JMenuItem();
         removeMenu = new javax.swing.JMenu();
         removeBorrowerMenuItem = new javax.swing.JMenuItem();
         removeBookMenuItem = new javax.swing.JMenuItem();
         reportMenu = new javax.swing.JMenu();
         checkedOutReportMenuItem = new javax.swing.JMenuItem();
         popularReportMenuItem = new javax.swing.JMenuItem();
         navigationSeparator = new javax.swing.JPopupMenu.Separator();
         tableMenuItem = new javax.swing.JMenuItem();
         startMenuItem = new javax.swing.JMenuItem();
         helpMenu = new javax.swing.JMenu();
         manualMenuItem = new javax.swing.JMenuItem();
 
         setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
         setTitle("Library");
         setMinimumSize(new java.awt.Dimension(800, 600));
         addWindowListener(new java.awt.event.WindowAdapter() {
             public void windowClosing(java.awt.event.WindowEvent evt) {
                 formWindowClosing(evt);
             }
         });
 
         mainPanel.setLayout(new java.awt.BorderLayout());
 
         cardPanel.setLayout(new java.awt.CardLayout());
 
         startPanel.setLayout(new java.awt.BorderLayout());
 
         startComboBoxPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Choose a transaction"));
 
         navigationComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] {
             START_STRING,
             TABLES,
             SEARCH,
             CHECK_ACCOUNT,
             HOLD_REQUEST,
             PAY_FINE,
             ADD_BORROWER,
             CHECK_OUT,
             CHECK_OVERDUE,
             PROCESS_RETURN,
             ADD_BOOK,
             ADD_COPY,
             REMOVE_BOOK,
             REMOVE_BORROWER,
             REPORT_POPULAR,
             REPORT_CHECKED_OUT
         }));
         startComboBoxPanel.add(navigationComboBox);
 
         startPanel.add(startComboBoxPanel, java.awt.BorderLayout.NORTH);
 
         cardPanel.add(startPanel, "Start");
 
         tablesPanel.setLayout(new java.awt.BorderLayout());
 
         tableNamePanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Choose table to view"));
 
         tablesComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] {
             "Borrower",
             "BorrowerType",
             "Book",
             "HasAuthor",
             "HasSubject",
             "BookCopy",
             "HoldRequest",
             "Borrowing",
             "Fine"
         }));
         tableNamePanel.add(tablesComboBox);
 
         tablesPanel.add(tableNamePanel, java.awt.BorderLayout.NORTH);
 
         entitiesTable.setModel(new javax.swing.table.DefaultTableModel(
             new Object [0][],
             new String [0]
 
         ) {
             boolean[] canEdit = new boolean [] {
                 false, false, false, false
             };
 
             public boolean isCellEditable(int rowIndex, int columnIndex) {
                 return canEdit [columnIndex];
             }
         });
         entitiesTable.setRowSelectionAllowed(false);
         viewTablePane.setViewportView(entitiesTable);
 
         tablesPanel.add(viewTablePane, java.awt.BorderLayout.CENTER);
 
         cardPanel.add(tablesPanel, "View tables");
 
         searchPanel.setLayout(new java.awt.BorderLayout());
 
         SearchTopPanel.setLayout(new java.awt.GridBagLayout());
 
         SearchComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Title", "Subject", "Author"}));
         SearchTopPanel.add(SearchComboBox, new java.awt.GridBagConstraints());
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.ipadx = 200;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         SearchTopPanel.add(SearchTextField, gridBagConstraints);
 
         searchPanel.add(SearchTopPanel, java.awt.BorderLayout.PAGE_START);
 
         SearchTable.setModel(new javax.swing.table.DefaultTableModel());
         jScrollPane1.setViewportView(SearchTable);
 
         searchPanel.add(jScrollPane1, java.awt.BorderLayout.CENTER);
 
         cardPanel.add(searchPanel, "Search for book");
 
         checkAccountPanel.setLayout(new java.awt.BorderLayout());
 
         searchAccountPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Borrower ID"));
         searchAccountPanel.setLayout(new java.awt.GridBagLayout());
 
         SearchIdField.setText("");
         SearchIdField.setPreferredSize(new java.awt.Dimension(150, 20));
         searchAccountPanel.add(SearchIdField, new java.awt.GridBagConstraints());
 
         checkAccountPanel.add(searchAccountPanel, java.awt.BorderLayout.PAGE_START);
 
         checkedOutBooksTable.setModel(new javax.swing.table.DefaultTableModel());
         checkOutBooksPane.setViewportView(checkedOutBooksTable);
 
         TabbedPane.addTab("Currently checked out books", checkOutBooksPane);
 
         finesTable.setModel(new javax.swing.table.DefaultTableModel());
         finesPane.setViewportView(finesTable);
 
         TabbedPane.addTab("Outstanding fines", finesPane);
 
         currentHoldsTable.setModel(new javax.swing.table.DefaultTableModel());
         currentHoldsPane.setViewportView(currentHoldsTable);
 
         TabbedPane.addTab("Placed hold requests", currentHoldsPane);
 
         checkAccountPanel.add(TabbedPane, java.awt.BorderLayout.CENTER);
 
         cardPanel.add(checkAccountPanel, "Check account");
 
         payFinePanel.setLayout(new java.awt.BorderLayout());
         payFinePanel.add(payFineMsgLabel, java.awt.BorderLayout.CENTER);
 
         payFineInputPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Pay a fine"));
         payFineInputPanel.setLayout(new java.awt.GridBagLayout());
 
         payFineFidLabel.setText("Fine ID");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         payFineInputPanel.add(payFineFidLabel, gridBagConstraints);
 
         payFineAmtLabel.setText("Amount");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 3;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         payFineInputPanel.add(payFineAmtLabel, gridBagConstraints);
 
         payFineInstructionLabel.setFont(new java.awt.Font("Tahoma", 0, 12));
         payFineInstructionLabel.setText("Obtain the Fine ID by checking your account for outstanding fines.");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 3, 5);
         payFineInputPanel.add(payFineInstructionLabel, gridBagConstraints);
 
         jLabel1.setFont(new java.awt.Font("Tahoma", 0, 12));
         jLabel1.setText("The Fine ID will be in the first column of the Fine tab.");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
         payFineInputPanel.add(jLabel1, gridBagConstraints);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 2;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.ipadx = 25;
         payFineInputPanel.add(paddingLabel, gridBagConstraints);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.ipadx = 60;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         payFineInputPanel.add(payFineFidTextField, gridBagConstraints);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 4;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.ipadx = 60;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         payFineInputPanel.add(payFineAmountTextField, gridBagConstraints);
 
         payFinePanel.add(payFineInputPanel, java.awt.BorderLayout.PAGE_START);
 
         cardPanel.add(payFinePanel, "Pay a fine");
 
         checkOutPanel.setLayout(new java.awt.BorderLayout());
 
         checkOutFieldsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Check out books"));
         checkOutFieldsPanel.setLayout(new java.awt.GridBagLayout());
 
         checkOutLabelBorid.setText("Borrower's Card Number");
         checkOutFieldsPanel.add(checkOutLabelBorid, new java.awt.GridBagConstraints());
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.ipadx = 120;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         checkOutFieldsPanel.add(checkOutTextBorid, gridBagConstraints);
 
         checkOutLabelCallNo.setText("Call Number");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         checkOutFieldsPanel.add(checkOutLabelCallNo, gridBagConstraints);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.ipadx = 120;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         checkOutFieldsPanel.add(checkOutTextCallNo, gridBagConstraints);
 
         checkOutLabelCopyNo.setText("Copy Number");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 2;
         checkOutFieldsPanel.add(checkOutLabelCopyNo, gridBagConstraints);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.ipadx = 120;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         checkOutFieldsPanel.add(checkOutTextCopyNo, gridBagConstraints);
         checkOutFieldsPanel.add(checkOutDummyLabel, new java.awt.GridBagConstraints());
 
         checkOutButtonAdd.setText("Add to Checkout Queue");
         checkOutButtonAdd.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 checkOutButtonAddActionPerformed(evt);
             }
         });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 3;
         gridBagConstraints.gridwidth = 2;
         checkOutFieldsPanel.add(checkOutButtonAdd, gridBagConstraints);
 
         checkOutPanel.add(checkOutFieldsPanel, java.awt.BorderLayout.NORTH);
 
         checkOutFieldsPanelReceipt.setBorder(javax.swing.BorderFactory.createTitledBorder("Receipt"));
         checkOutFieldsPanelReceipt.setLayout(new java.awt.BorderLayout());
 
         checkOutTableReceiptTable.setModel(new javax.swing.table.DefaultTableModel(
             new Object [][] {
 
             },
             new String [] {
                 "Borrower ID", "Call Number", "Copy Number"
             }
         ));
         checkOutTableReceipt.setViewportView(checkOutTableReceiptTable);
 
         checkOutFieldsPanelReceipt.add(checkOutTableReceipt, java.awt.BorderLayout.CENTER);
 
         checkOutPanel.add(checkOutFieldsPanelReceipt, java.awt.BorderLayout.CENTER);
 
         cardPanel.add(checkOutPanel, "Check-out books");
 
         addBorrowerPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Add a new borrower"));
         addBorrowerPanel.setAutoscrolls(true);
         addBorrowerPanel.setPreferredSize(new java.awt.Dimension(250, 180));
         addBorrowerPanel.setRequestFocusEnabled(false);
         addBorrowerPanel.setLayout(new java.awt.GridBagLayout());
 
         addBorrowerLabelName.setText("Name");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         addBorrowerPanel.add(addBorrowerLabelName, gridBagConstraints);
 
         addBorrowerTextName.setName(""); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 2;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
         gridBagConstraints.ipadx = 120;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         addBorrowerPanel.add(addBorrowerTextName, gridBagConstraints);
 
         addBorrowerLabelAddress.setText("Address");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         addBorrowerPanel.add(addBorrowerLabelAddress, gridBagConstraints);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 2;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
         gridBagConstraints.ipadx = 120;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         addBorrowerPanel.add(addBorrowerTextAddress, gridBagConstraints);
 
         addBorrowerLabelPhoneNo.setText("Phone No");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 3;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         addBorrowerPanel.add(addBorrowerLabelPhoneNo, gridBagConstraints);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 2;
         gridBagConstraints.gridy = 3;
         gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
         gridBagConstraints.ipadx = 120;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         addBorrowerPanel.add(addBorrowerTextPhoneNo, gridBagConstraints);
 
         addBorrowerLabelEmail.setText("Email");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 4;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         addBorrowerPanel.add(addBorrowerLabelEmail, gridBagConstraints);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 2;
         gridBagConstraints.gridy = 4;
         gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
         gridBagConstraints.ipadx = 120;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         addBorrowerPanel.add(addBorrowerTextEmail, gridBagConstraints);
 
         addBorrowerLabelPassword.setText("Password");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 5;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         addBorrowerPanel.add(addBorrowerLabelPassword, gridBagConstraints);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 2;
         gridBagConstraints.gridy = 5;
         gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
         gridBagConstraints.ipadx = 120;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         addBorrowerPanel.add(addBorrowerTextPassword, gridBagConstraints);
 
         addBorrowerLabelSinOrStNo.setText("SIN No");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 6;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         addBorrowerPanel.add(addBorrowerLabelSinOrStNo, gridBagConstraints);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 2;
         gridBagConstraints.gridy = 6;
         gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
         gridBagConstraints.ipadx = 120;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         addBorrowerPanel.add(addBorrowerTextSinOrStNo, gridBagConstraints);
 
         addBorrowerLabelType.setText("BorrowerType");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 7;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         addBorrowerPanel.add(addBorrowerLabelType, gridBagConstraints);
 
         addBorrowerComboBoxType.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Student", "Faculty", "Staff" }));
         addBorrowerComboBoxType.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 addBorrowerComboBoxTypeActionPerformed(evt);
             }
         });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 2;
         gridBagConstraints.gridy = 7;
         gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
         gridBagConstraints.ipadx = 50;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         addBorrowerPanel.add(addBorrowerComboBoxType, gridBagConstraints);
 
         expiryDateLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
         expiryDateLabel.setText("ExpiryDate (dd/mm/yyyy)");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 8;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         addBorrowerPanel.add(expiryDateLabel, gridBagConstraints);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 2;
         gridBagConstraints.gridy = 8;
         gridBagConstraints.ipadx = 30;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         addBorrowerPanel.add(expiryDateTextField, gridBagConstraints);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 3;
         gridBagConstraints.gridy = 8;
         gridBagConstraints.ipadx = 30;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         addBorrowerPanel.add(expiryMonthTextField, gridBagConstraints);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 4;
         gridBagConstraints.gridy = 8;
         gridBagConstraints.ipadx = 60;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         addBorrowerPanel.add(expiryYearTextField, gridBagConstraints);
 
         cardPanel.add(addBorrowerPanel, "Add a new borrower");
 
         checkOverduePanel.setLayout(new java.awt.BorderLayout());
 
         checkOverdueLabelBorrInfo.setText("Overdue Info");
         checkOverduePanel.add(checkOverdueLabelBorrInfo, java.awt.BorderLayout.NORTH);
 
         checkOverdueTable.setModel(new javax.swing.table.DefaultTableModel(
             new Object [][] {
 
             },
             new String [] {
 
             }
         ));
         checkOverdueTable.setFillsViewportHeight(true);
         checkOverdueScrollPaneInfo.setViewportView(checkOverdueTable);
 
         checkOverduePanel.add(checkOverdueScrollPaneInfo, java.awt.BorderLayout.CENTER);
 
         checkOverdueButtonMessage.setText("Message Borrower");
         checkOverdueButtonMessage.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 checkOverdueButtonMessageActionPerformed(evt);
             }
         });
         checkOverduePanelMessage.add(checkOverdueButtonMessage);
 
         checkOverduePanel.add(checkOverduePanelMessage, java.awt.BorderLayout.PAGE_END);
 
         cardPanel.add(checkOverduePanel, "Check overdue books");
 
         addNewBookPanel.setLayout(new java.awt.BorderLayout(0, 30));
 
         abMainLabel.setText("Add New Book:  * = required, & = deliniate with commas");
         addNewBookPanel.add(abMainLabel, java.awt.BorderLayout.PAGE_START);
 
         abMainPanel.setPreferredSize(new java.awt.Dimension(500, 252));
         abMainPanel.setLayout(new java.awt.GridBagLayout());
 
         abCNLabel.setText("CallNumber  (*)");
         abMainPanel.add(abCNLabel, new java.awt.GridBagConstraints());
 
         abCN.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 abCNActionPerformed(evt);
             }
         });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.ipadx = 300;
         abMainPanel.add(abCN, gridBagConstraints);
 
         abISBNLabel.setText("ISBN (*)");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         abMainPanel.add(abISBNLabel, gridBagConstraints);
 
         abISBN.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 abISBNActionPerformed(evt);
             }
         });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         abMainPanel.add(abISBN, gridBagConstraints);
 
         abTitleLabel.setText("Title");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 2;
         abMainPanel.add(abTitleLabel, gridBagConstraints);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         abMainPanel.add(abTitle, gridBagConstraints);
 
         abMainAuthorLabel.setText("Main Author");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 3;
         abMainPanel.add(abMainAuthorLabel, gridBagConstraints);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 3;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         abMainPanel.add(abMA, gridBagConstraints);
 
         abPubLabel.setText("Publisher");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 4;
         abMainPanel.add(abPubLabel, gridBagConstraints);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 4;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         abMainPanel.add(abPub, gridBagConstraints);
 
         abYearLabel.setText("Year");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 5;
         abMainPanel.add(abYearLabel, gridBagConstraints);
 
         abYear.setCursor(new java.awt.Cursor(java.awt.Cursor.TEXT_CURSOR));
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 5;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         abMainPanel.add(abYear, gridBagConstraints);
 
         abAALabel.setText("Additional Authors (&)");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 6;
         abMainPanel.add(abAALabel, gridBagConstraints);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 6;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         abMainPanel.add(abAA, gridBagConstraints);
 
         abSubsLabel.setText("Subjects (*) (&)");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 7;
         abMainPanel.add(abSubsLabel, gridBagConstraints);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 7;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         abMainPanel.add(abSubs, gridBagConstraints);
 
         abSpinnerLabel.setText("Create Copies");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 8;
         abMainPanel.add(abSpinnerLabel, gridBagConstraints);
 
         abSpinner.setModel(new javax.swing.SpinnerNumberModel(1, 1, 1000, 1));
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 8;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         abMainPanel.add(abSpinner, gridBagConstraints);
 
         addNewBookPanel.add(abMainPanel, java.awt.BorderLayout.LINE_START);
 
         abStatusPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
 
         abOpStatusLabel.setText("Operation Status");
         abStatusPanel.add(abOpStatusLabel);
 
         abOpStatus.setText("Waiting for Input...");
         abOpStatus.setPreferredSize(new java.awt.Dimension(500, 28));
         abStatusPanel.add(abOpStatus);
 
         addNewBookPanel.add(abStatusPanel, java.awt.BorderLayout.PAGE_END);
 
         cardPanel.add(addNewBookPanel, "Add new book");
 
         addNewCopyPanel.setLayout(new java.awt.BorderLayout());
 
         abcMainPanel.setLayout(new java.awt.GridBagLayout());
 
         abcCNLabel.setText("Call Number");
         abcMainPanel.add(abcCNLabel, new java.awt.GridBagConstraints());
 
         abcSpinnerLabel.setText("Number of Copies");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         abcMainPanel.add(abcSpinnerLabel, gridBagConstraints);
 
         abcCN.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 abcCNActionPerformed(evt);
             }
         });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         abcMainPanel.add(abcCN, gridBagConstraints);
 
         abcSpinner.setModel(new javax.swing.SpinnerNumberModel(1, 1, 1000, 1));
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 1;
         abcMainPanel.add(abcSpinner, gridBagConstraints);
 
         addNewCopyPanel.add(abcMainPanel, java.awt.BorderLayout.CENTER);
 
         abcMainPanelLabel.setText("Add New Book Copy");
         addNewCopyPanel.add(abcMainPanelLabel, java.awt.BorderLayout.PAGE_START);
 
         cardPanel.add(addNewCopyPanel, "Add new book copy");
 
         removeBorrowerPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Remove borrower"));
         removeBorrowerPanel.setLayout(new java.awt.BorderLayout());
 
         removeBorrowerInputPanel.setLayout(new java.awt.GridBagLayout());
 
         removeBorrowerLabel.setText("Borrower ID:");
         removeBorrowerLabel.setVerticalAlignment(javax.swing.SwingConstants.TOP);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.anchor = java.awt.GridBagConstraints.PAGE_START;
         gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
         removeBorrowerInputPanel.add(removeBorrowerLabel, gridBagConstraints);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.ipadx = 100;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.PAGE_START;
         gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
         removeBorrowerInputPanel.add(removeBorrowerTextField, gridBagConstraints);
 
         removeBorrowerPanel.add(removeBorrowerInputPanel, java.awt.BorderLayout.NORTH);
 
         cardPanel.add(removeBorrowerPanel, "Remove borrower");
 
         removeBookPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Remove books and copies"));
         removeBookPanel.setLayout(new java.awt.BorderLayout());
 
         removeBookCallNumberPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Book"));
         removeBookCallNumberPanel.setLayout(new java.awt.GridBagLayout());
 
         removeBookCallNumberLabel.setText("Call number: ");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         removeBookCallNumberPanel.add(removeBookCallNumberLabel, gridBagConstraints);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.ipadx = 60;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         removeBookCallNumberPanel.add(removeBookPrimaryNoTextField, gridBagConstraints);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.ipadx = 60;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         removeBookCallNumberPanel.add(removeBookSecondaryNoTextField, gridBagConstraints);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.ipadx = 60;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         removeBookCallNumberPanel.add(removeBookYearTextField, gridBagConstraints);
 
         removeBookPanel.add(removeBookCallNumberPanel, java.awt.BorderLayout.PAGE_START);
 
         removeBookRadioButtonPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Remove"));
         removeBookRadioButtonPanel.setLayout(new java.awt.GridBagLayout());
 
         removeBookButtonGroup.add(removeBookBookRadioButton);
         removeBookBookRadioButton.setSelected(true);
         removeBookBookRadioButton.setText("Remove book and all copies");
         removeBookBookRadioButton.setName("all"); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         removeBookRadioButtonPanel.add(removeBookBookRadioButton, gridBagConstraints);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.ipadx = 100;
         removeBookRadioButtonPanel.add(removeBookWhichCopiesTextField, gridBagConstraints);
 
         removeBookButtonGroup.add(removeBookOnlyTheseCopiesRadioButton);
         removeBookOnlyTheseCopiesRadioButton.setText("Only these copies: ");
         removeBookOnlyTheseCopiesRadioButton.setName("select"); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         removeBookRadioButtonPanel.add(removeBookOnlyTheseCopiesRadioButton, gridBagConstraints);
 
         removeBookPanel.add(removeBookRadioButtonPanel, java.awt.BorderLayout.CENTER);
 
         cardPanel.add(removeBookPanel, "Remove books and copies");
 
         popularReportPanel.setLayout(new java.awt.BorderLayout());
 
         popularReportOptionsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Options"));
         popularReportOptionsPanel.setLayout(new java.awt.GridBagLayout());
 
         popularReportYearSelectLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
         popularReportYearSelectLabel.setText("Year to report on:");
         popularReportYearSelectLabel.setName("Year to report on"); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         popularReportOptionsPanel.add(popularReportYearSelectLabel, gridBagConstraints);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.ipadx = 50;
         popularReportOptionsPanel.add(popularReportYearTextField, gridBagConstraints);
 
         popularReportNSelectLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
         popularReportNSelectLabel.setText("Number of books in report:");
         popularReportNSelectLabel.setName("Number of books in report"); // NOI18N
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         popularReportOptionsPanel.add(popularReportNSelectLabel, gridBagConstraints);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.ipadx = 50;
         popularReportOptionsPanel.add(popularReportNTextField, gridBagConstraints);
 
         popularReportPanel.add(popularReportOptionsPanel, java.awt.BorderLayout.PAGE_START);
 
         popularReportTablePanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Report"));
         popularReportTablePanel.setLayout(new java.awt.BorderLayout());
 
         popularReportTable.setModel(new javax.swing.table.DefaultTableModel(
 
         ));
         popularReportTablePane.setViewportView(popularReportTable);
 
         popularReportTablePanel.add(popularReportTablePane, java.awt.BorderLayout.CENTER);
 
         popularReportPanel.add(popularReportTablePanel, java.awt.BorderLayout.CENTER);
 
         cardPanel.add(popularReportPanel, "Popular books report");
 
         checkedOutReportPanel.setLayout(new java.awt.BorderLayout());
 
         checkedOutReportFilterPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Filter by subject"));
         checkedOutReportFilterPanel.setLayout(new java.awt.GridBagLayout());
 
         checkedOutReportFilterCheckBox.setText("Filter by subject: ");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
         checkedOutReportFilterPanel.add(checkedOutReportFilterCheckBox, gridBagConstraints);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.ipadx = 100;
         gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
         checkedOutReportFilterPanel.add(checkedOutReportTextField, gridBagConstraints);
 
         checkedOutReportPanel.add(checkedOutReportFilterPanel, java.awt.BorderLayout.PAGE_START);
 
         checkedOutReportTablePanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Report"));
         checkedOutReportTablePanel.setLayout(new java.awt.BorderLayout());
 
         checkedOutReportTable.setModel(new javax.swing.table.DefaultTableModel());
         checkedOutReportTablePane.setViewportView(checkedOutReportTable);
 
         checkedOutReportTablePanel.add(checkedOutReportTablePane, java.awt.BorderLayout.CENTER);
 
         checkedOutReportPanel.add(checkedOutReportTablePanel, java.awt.BorderLayout.CENTER);
 
         cardPanel.add(checkedOutReportPanel, "Checked-out report");
         cardPanel.add(holdRequestPanel, "Place hold request");
         cardPanel.add(processReturnPanel, "Process a return");
 
         mainPanel.add(cardPanel, java.awt.BorderLayout.CENTER);
 
         buttonPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));
 
         doButton.setText("Do something");
         doButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 doButtonActionPerformed(evt);
             }
         });
         buttonPanel.add(doButton);
 
         clearButton.setText("Clear");
         clearButton.setHideActionText(true);
         clearButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 clearButtonActionPerformed(evt);
             }
         });
         buttonPanel.add(clearButton);
 
         mainPanel.add(buttonPanel, java.awt.BorderLayout.SOUTH);
 
         getContentPane().add(mainPanel, java.awt.BorderLayout.CENTER);
 
         fileMenu.setText("File");
 
         reconnectMenuItem.setText("Reconnect");
         reconnectMenuItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 reconnectMenuItemActionPerformed(evt);
             }
         });
         fileMenu.add(reconnectMenuItem);
 
         quitMenuItem.setText("Quit");
         quitMenuItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 quitMenuItemActionPerformed(evt);
             }
         });
         fileMenu.add(quitMenuItem);
 
         menuBar.add(fileMenu);
 
         navigationMenu.setText("Navigation");
 
         borrowerMenu.setText("Borrower");
 
         searchMenuItem.setText(SEARCH);
         searchMenuItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 navigationMenuItemActionPerformed(evt);
             }
         });
         borrowerMenu.add(searchMenuItem);
 
         checkAccountMenuItem.setText(CHECK_ACCOUNT);
         checkAccountMenuItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 navigationMenuItemActionPerformed(evt);
             }
         });
         borrowerMenu.add(checkAccountMenuItem);
 
         holdRequestMenuItem.setText(HOLD_REQUEST);
         holdRequestMenuItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 navigationMenuItemActionPerformed(evt);
             }
         });
         borrowerMenu.add(holdRequestMenuItem);
 
         payFineMenuItem.setText(PAY_FINE);
         payFineMenuItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 navigationMenuItemActionPerformed(evt);
             }
         });
         borrowerMenu.add(payFineMenuItem);
 
         navigationMenu.add(borrowerMenu);
 
         clerkMenu.setText("Clerk");
 
         addBorrowerMenuItem.setText(ADD_BORROWER);
         addBorrowerMenuItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 navigationMenuItemActionPerformed(evt);
             }
         });
         clerkMenu.add(addBorrowerMenuItem);
 
         checkOutMenuItem.setText(CHECK_OUT);
         checkOutMenuItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 navigationMenuItemActionPerformed(evt);
             }
         });
         clerkMenu.add(checkOutMenuItem);
 
         processReturnMenuItem.setText(PROCESS_RETURN);
         processReturnMenuItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 navigationMenuItemActionPerformed(evt);
             }
         });
         clerkMenu.add(processReturnMenuItem);
 
         checkOverdueMenuItem.setText(CHECK_OVERDUE);
         checkOverdueMenuItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 navigationMenuItemActionPerformed(evt);
             }
         });
         clerkMenu.add(checkOverdueMenuItem);
 
         navigationMenu.add(clerkMenu);
 
         librarianMenu.setText("Librarian");
 
         addMenu.setText("Add");
 
         addBookMenuItem.setText(ADD_BOOK);
         addBookMenuItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 navigationMenuItemActionPerformed(evt);
             }
         });
         addMenu.add(addBookMenuItem);
 
         addBookCopyMenuItem.setText(ADD_COPY);
         addBookCopyMenuItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 navigationMenuItemActionPerformed(evt);
             }
         });
         addMenu.add(addBookCopyMenuItem);
 
         librarianMenu.add(addMenu);
 
         removeMenu.setText("Remove");
 
         removeBorrowerMenuItem.setText(REMOVE_BORROWER);
         removeBorrowerMenuItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 navigationMenuItemActionPerformed(evt);
             }
         });
         removeMenu.add(removeBorrowerMenuItem);
 
         removeBookMenuItem.setText(REMOVE_BOOK);
         removeBookMenuItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 navigationMenuItemActionPerformed(evt);
             }
         });
         removeMenu.add(removeBookMenuItem);
 
         librarianMenu.add(removeMenu);
 
         reportMenu.setText("Report");
 
         checkedOutReportMenuItem.setText(REPORT_CHECKED_OUT);
         checkedOutReportMenuItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 navigationMenuItemActionPerformed(evt);
             }
         });
         reportMenu.add(checkedOutReportMenuItem);
 
         popularReportMenuItem.setText(REPORT_POPULAR);
         popularReportMenuItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 navigationMenuItemActionPerformed(evt);
             }
         });
         reportMenu.add(popularReportMenuItem);
 
         librarianMenu.add(reportMenu);
 
         navigationMenu.add(librarianMenu);
         navigationMenu.add(navigationSeparator);
 
         tableMenuItem.setText(TABLES);
         tableMenuItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 navigationMenuItemActionPerformed(evt);
             }
         });
         navigationMenu.add(tableMenuItem);
 
         startMenuItem.setText(START_STRING);
         startMenuItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 navigationMenuItemActionPerformed(evt);
             }
         });
         navigationMenu.add(startMenuItem);
 
         menuBar.add(navigationMenu);
 
         helpMenu.setText("Help");
 
         manualMenuItem.setText("User manual");
         manualMenuItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 manualMenuItemActionPerformed(evt);
             }
         });
         helpMenu.add(manualMenuItem);
 
         menuBar.add(helpMenu);
 
         setJMenuBar(menuBar);
 
         pack();
     }// </editor-fold>//GEN-END:initComponents
 
         /** 
          * Simulates sending an email
          * @param evt 
          */
   private void checkOverdueButtonMessageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkOverdueButtonMessageActionPerformed
     JOptionPane.showMessageDialog(this, "Email sent.");
   }//GEN-LAST:event_checkOverdueButtonMessageActionPerformed
 
 	/*
 	 * Button that adds a Borrower ID, Card Number and Copy Number to a
 	 * CheckOutTable before checking out all items.
 	 */
 	private void checkOutButtonAddActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_checkOutButtonAddActionPerformed
 	// TODO add your handling code here:
 		checkOutTextBorid.setEditable(false);
 		
 		String borid = checkOutTextBorid.getText();
 		String copyNum = checkOutTextCopyNo.getText();
 		String callNum = checkOutTextCallNo.getText();
 
 		Vector<String> r = new Vector<String>();
 		r.addElement(borid);
 		r.addElement(callNum);
 		r.addElement(copyNum);
 
 		DefaultTableModel model = (DefaultTableModel) checkOutTableReceiptTable
 				.getModel();
 
 		model.addRow(r);
 
 		checkOutTableReceiptTable.setModel(model);
 	}// GEN-LAST:event_checkOutButtonAddActionPerformed
 
 	/**
 	 * Opens the default browser to a web site hosting the user manual TODO:
 	 * replace with actual manual URL
 	 * 
 	 * @param evt
 	 *            reference:
 	 *            http://docs.oracle.com/javase/tutorial/uiswing/misc/
 	 *            desktop.html
 	 */
 	private void manualMenuItemActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_manualMenuItemActionPerformed
 		// http://docs.oracle.com/javase/tutorial/uiswing/misc/desktop.html
 		final String manualUrl = "http://www.ugrad.cs.ubc.ca/~c7e8/";
 		try {
 			Desktop desktop = null;
 			if (Desktop.isDesktopSupported()) {
 				desktop = Desktop.getDesktop();
 				desktop.browse(new URI(manualUrl));
 			} else {
 				throw new IOException();
 			}
 		} catch (IOException ex) {
 			String message = "The user manual is located at " + manualUrl;
 			JOptionPane.showMessageDialog(this, message, "Manual",
 					JOptionPane.INFORMATION_MESSAGE);
 		} catch (URISyntaxException ex) {
 			String msg = "URI syntax is incorrect";
 			JOptionPane.showMessageDialog(this, msg, "Error",
 					JOptionPane.ERROR_MESSAGE);
 		}
 	}// GEN-LAST:event_manualMenuItemActionPerformed
 
 	/**
 	 * Do this when the window is being closed Takes care of any clean up
 	 * operations
 	 * 
 	 * @param evt
 	 */
 	private void formWindowClosing(java.awt.event.WindowEvent evt) {// GEN-FIRST:event_formWindowClosing
 		if (controller != null) {
 			controller.shutdown();
 		}
 	}// GEN-LAST:event_formWindowClosing
 
 	/**
 	 * Do this when one of the menu items is clicked Displays the panel
 	 * corresponding to the user's selection
 	 * 
 	 * @param evt
 	 */
 	private void navigationMenuItemActionPerformed(
 			java.awt.event.ActionEvent evt) {// GEN-FIRST:event_navigationMenuItemActionPerformed
 		CardLayout cl = (CardLayout) (cardPanel.getLayout());
 		String key = ((JMenuItem) evt.getSource()).getText();
 		cl.show(cardPanel, key);
 		state = statemap.get(key);
 		doButton.setText(key);
 	}// GEN-LAST:event_navigationMenuItemActionPerformed
 
 	/**
 	 * Chooses an action based on the current state of the GUI Executes the
 	 * functionality that the state is supposed to embody i.e. In the SEARCH
 	 * state, this should send a message to the Controller to search for a book
 	 * 
 	 * @param evt
 	 */
 	private void doButtonActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_doButtonActionPerformed
               String[][] tableWithHeader = null;
               String[][] tableWithoutHeader = null;
               String[] header = null;
               int numRows, numCols;
               try
               {
 		switch (state) {
 		case TABLES:
                         if (viewTables()) {
                           return;
                         }
 			break;
 		case START:
 			CardLayout cl = (CardLayout) (cardPanel.getLayout());
 			String key = ((String) navigationComboBox.getSelectedItem());
 			State state = statemap.get(key);
 			cl.show(cardPanel, key);
 			this.state = state;
 			String buttonText = generateButtonText();
 			doButton.setText(buttonText);
 			break;
 		case SEARCH:
                         search();
 			break;
 		case CHECK_ACCOUNT:
                         checkAccount();
 			break;
 		case HOLD_REQUEST:
                         holdRequest();
 			break;
 		case PAY_FINE:
                         payFine();
 			break;
 		case ADD_BORROWER:
                         if (addBorrower()) {
                           return;
                         }
 			break;
 		case CHECK_OUT:
                         if (checkOut()) {
                           return;
                         }
 			
 			break;
 		case CHECK_OVERDUE:
                        checkOverdue();
 
 			break;
 		case PROCESS_RETURN:
                   if (processReturn()) {
                     return;
                   }
                   
                   
                   break;
 		case ADD_BOOK:
                         if (addBook()) {
                           return;
                         }
 
 			break;
 		case ADD_COPY:
                         if (addCopy()) {
                           return;
                         }
 			break;
 		case REMOVE_BOOK:
                         if (removeBook()) {
                           return;
                         }
 
 			break;
 		case REMOVE_BORROWER:
                         if (removeBorrower()) {
                           return;
                         }
 			break;
 
 		case REPORT_POPULAR:
                         if (reportPopular()) {
                           return;
                         }
 			break; // END CASE POPULAR REPORT
 
 		case REPORT_CHECKED_OUT:
                         if (reportCheckedOut()) {
                           return;
                         }
 			break;
 		default:
               }// end switch
                 
           }
           catch (Exception e)
           {
             JOptionPane.showMessageDialog(this, "Could not complete transaction.\n"+e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
           }
 	}// GEN-LAST:event_doButtonActionPerformed
 
   private boolean reportCheckedOut() throws HeadlessException {
     int numRows;
     String[][] tableWithoutHeader;
     int numCols;
     String[][] checkedOutReportWithHeader = null;
     try {
       if (checkedOutReportFilterCheckBox.isSelected()) {
               String subjectToFilterBy = checkedOutReportTextField
                               .getText().trim();
               checkedOutReportWithHeader = controller
                               .getSystemLibrarian().getCheckedOutBooksReport(
                                               subjectToFilterBy);
       } else {
               checkedOutReportWithHeader = controller
                               .getSystemLibrarian().getCheckedOutBooksReport();
       }
     } catch (SQLException e) {
       String msg = "Could not retrieve data\n" + e.getMessage();
       JOptionPane.showMessageDialog(this, msg, "Error",
                       JOptionPane.ERROR_MESSAGE);
       return true;
     }
     String[] checkedOutReportHeader = checkedOutReportWithHeader[0];
     numRows = checkedOutReportWithHeader.length;
     numRows--;
     numCols = checkedOutReportWithHeader[0].length;
     tableWithoutHeader = new String[numRows][];
     for (int i = 0; i < numRows; i++) {
             tableWithoutHeader[i] = new String[numCols];
     }
     for (int i = 0; i < numRows; i++) {
             System.arraycopy(checkedOutReportWithHeader[i + 1], 0,
                             tableWithoutHeader[i], 0, numCols);
     }
     checkedOutReportTable.setModel(new DefaultTableModel(
                     tableWithoutHeader, checkedOutReportHeader));
     checkedOutReportTable.repaint();
     return false;
   }
 
   private boolean reportPopular() throws HeadlessException {
     String[][] tableWithHeader;
     int numRows;
     String[][] tableWithoutHeader;
     int numCols;
     String[] header;
     int reportYear = -1;
     int reportN = -1;
     try {
       // begin error checking text fields
       boolean reportYearError = true;
       boolean reportNError = true;
       try {
               reportYear = Integer.parseInt(popularReportYearTextField
                               .getText());
               reportYearError = false;
       } catch (NumberFormatException e) {
               // do nothing
       }
       try {
               reportN = Integer.parseInt(popularReportNTextField
                               .getText());
               reportNError = false;
       } catch (NumberFormatException e) {
               // do nothing
       }
       if (reportYearError || reportNError) {
         String msg = "There are errors in the following fields:\n"
                         + ((reportYearError) ? '-' + popularReportYearSelectLabel
                                         .getName() + '\n' : "")
                         + ((reportNError) ? '-' + popularReportNSelectLabel
                                         .getName() + '\n' : "")
                         + "Please correct them and resubmit.";
         JOptionPane.showMessageDialog(this, msg, "Input error",
                         JOptionPane.ERROR_MESSAGE);
         return true;
       }
       // end error checking text fields
       tableWithHeader = controller.getSystemLibrarian()
                       .getPopularBooks(reportYear, reportN);
     } catch (SQLException e) {
       String msg = "Could not retrieve the table:\n" + e.getMessage();
       JOptionPane.showMessageDialog(this, msg, "Error",
                       JOptionPane.ERROR_MESSAGE);
       return true;
     }
     header = tableWithHeader[0];
     numRows = tableWithHeader.length;
     numRows--;
     numCols = tableWithHeader[0].length;
     tableWithoutHeader = new String[numRows][];
     for (int i = 0; i < numRows; i++) {
             tableWithoutHeader[i] = new String[numCols];
     }
     for (int i = 0; i < numRows; i++) {
             System.arraycopy(tableWithHeader[i + 1], 0,
                             tableWithoutHeader[i], 0, numCols);
     }
     popularReportTable.setModel(new DefaultTableModel(
                     tableWithoutHeader, header));
     popularReportTable.repaint();
     return false;
   }
 
   private boolean removeBorrower() throws HeadlessException {
     int bid = -1;
     try {
       bid = Integer.parseInt(removeBorrowerTextField.getText());
       // confirm
       String confirmRemoveBorrowerMessage = "Are you sure you want to remove borrower "
                       + bid + "?";
       int confirmRemoveBorrowerResult = JOptionPane
                       .showConfirmDialog(this, confirmRemoveBorrowerMessage,
                                       "Please confirm", JOptionPane.YES_NO_OPTION,
                                       JOptionPane.WARNING_MESSAGE);
       if (confirmRemoveBorrowerResult != JOptionPane.YES_OPTION) {
         return true;
       }
       Borrower removeBorrowerBorrower = new Borrower();
       removeBorrowerBorrower.setBid(bid);
       // TODO delete does not return false if the Borrower is not
       // deleted.
       // Coordinate with the author of Borrower and get this fixed.
       if (removeBorrowerBorrower.delete()) {
               String msg = "Borrower with an ID of " + bid
                               + " successfully removed from the database.";
               JOptionPane.showMessageDialog(this, msg, "Success",
                               JOptionPane.PLAIN_MESSAGE);
       } else {
               String msg = "Failed to remove borrower with an ID of "
                               + bid + ".";
               JOptionPane.showMessageDialog(this, msg, "Error",
                               JOptionPane.ERROR_MESSAGE);
 
       }
     }catch (NumberFormatException nfe) {
            String msg = "Borrower ID must be a whole number.";
            JOptionPane.showMessageDialog(this, msg, "Error",
                            JOptionPane.ERROR_MESSAGE);
    }catch (SQLException se) {
           String msg = "Could not remove borrower with an ID of " + bid
                           + " from the database:\n" + se.getMessage();
           JOptionPane.showMessageDialog(this, msg, "Error",
                           JOptionPane.ERROR_MESSAGE);
   } finally {
       clearButtonActionPerformed(null);
     }
     return false;
   }
 
   private boolean removeBook() throws HeadlessException {
     String removeBookCallNumber = removeBookPrimaryNoTextField
                     .getText().trim().toUpperCase()
                     + ' '
                     + removeBookSecondaryNoTextField.getText().trim()
                                     .toUpperCase()
                     + ' '
                     + removeBookYearTextField.getText().trim().toUpperCase();
     try {
       if (removeBookBookRadioButton.isSelected()) {
         // confirm
         String removeBookConfirmMessage = "Are you sure you want to remove book "
                         + removeBookCallNumber
                         + "?\nAll information such as copies, requests, and borrowings"
                         + " will also be deleted.";
         int removeBookConfirm = JOptionPane.showConfirmDialog(this,
                         removeBookConfirmMessage, "Please confirm",
                         JOptionPane.YES_NO_OPTION,
                         JOptionPane.WARNING_MESSAGE);
         if (removeBookConfirm != JOptionPane.YES_OPTION) {
           return true;
         }
         if (controller.getSystemLibrarian().removeBook(
                         removeBookCallNumber)) {
           String msg = "Book " + removeBookCallNumber
                           + " successfully removed.";
           JOptionPane.showMessageDialog(this, msg, "Success",
                           JOptionPane.PLAIN_MESSAGE);
         } else {
           String msg = "Failed to remove book "
                           + removeBookCallNumber + ".";
           JOptionPane.showMessageDialog(this, msg, "Error",
                           JOptionPane.ERROR_MESSAGE);
           return true;
         }
       } else // if remove copies radio button is selected
       {
         int[] copyNumbersToRemove = parseBookCopyCopyNumbers(removeBookWhichCopiesTextField
                         .getText());
         String removeBookCopiesConfirmMessage = "Are you sure you want to remove the copies of book "
                         + removeBookCallNumber + " with copy numbers:";
         int length = copyNumbersToRemove.length;
         for (int i = 0; i < length; i++) {
                 removeBookCopiesConfirmMessage += "C"
                                 + copyNumbersToRemove[i];
                 if (i != length - 1) {
                         removeBookCopiesConfirmMessage += ", ";
                 }
         }
         removeBookCopiesConfirmMessage += "?\nAll information such as hold requests and borrowings"
                         + " for these copies" + " will also be deleted.";
         int removeBookConfirm = JOptionPane.showConfirmDialog(this,
                         removeBookCopiesConfirmMessage, "Please confirm",
                         JOptionPane.YES_NO_OPTION,
                         JOptionPane.WARNING_MESSAGE);
         if (removeBookConfirm != JOptionPane.YES_OPTION) {
           return true;
         }
         if (controller
                         .getSystemLibrarian()
                         .removeBookCopy(
                                         removeBookCallNumber,
                                         mapCopyNumberIntsToCopyNumberStrings(copyNumbersToRemove))) {
           String msg = "Copies successfully removed.";
           JOptionPane.showMessageDialog(this, msg, "Success",
                           JOptionPane.PLAIN_MESSAGE);
         } else {
           String msg = "Failed to remove copies.  None were removed.";
           JOptionPane.showMessageDialog(this, msg, "Error",
                           JOptionPane.ERROR_MESSAGE);
           return true;
         }
         clearButtonActionPerformed(null);
       }
     } // end try
     catch (SQLException e) {
             // handle
 
             String msg = "Deletion failed.\n";
             msg += e.getMessage();
             JOptionPane.showMessageDialog(this, msg, "Error",
                             JOptionPane.ERROR_MESSAGE);
     }
     return false;
   }
 
   private boolean processReturn() throws HeadlessException {
     String processReturnCallNumber = processReturnPanel.getCallNumber();
     String processReturnCopyNo = processReturnPanel.getCopyNo();
     try {
       try {
         controller.getSystemClerk().processReturn(processReturnCallNumber, processReturnCopyNo);
       } catch (FineRequiredException processReturnFineRequiredException) {
         boolean fineError = true;
         int fineAmountInCents = 0;
         int errorCounter = 0;
         while (fineError && errorCounter > 3) 
         {
           try 
           {
 
             String fineRequiredMessage = "This book is overdue and requires a fine\n"
                     + "Please enter the dollars to charge the borrower:";
             String processReturnFineString = JOptionPane.showInputDialog(this, fineRequiredMessage, "Error", JOptionPane.ERROR_MESSAGE);
             double fineInDollars = Double.parseDouble(processReturnFineString);
             if (fineInDollars < 0) 
             {
               throw new NoPaymentException("Fines must be greater than $0");
             }
             final int CENTS_IN_DOLLAR = 100;
             fineAmountInCents = (int) (fineInDollars * CENTS_IN_DOLLAR);
             fineError = false;
           } 
           catch (NumberFormatException fineRequiredNfe) 
           {
             String processReturnNfeMessage = "Fine amount must be a number.";
             JOptionPane.showMessageDialog(this, processReturnNfeMessage, "Error", JOptionPane.ERROR_MESSAGE);
             errorCounter++;
           } 
           catch (NoPaymentException fineRequiredNpe) 
           {
             JOptionPane.showMessageDialog(this, fineRequiredNpe.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
             errorCounter++;
           }
         } // end while error in entering the fine amount
         if (errorCounter >= 3) {
           JOptionPane.showMessageDialog(this, "Too many attempts.\nTransaction cancelled.", "Error", JOptionPane.ERROR_MESSAGE);
           processReturnPanel.append(processReturnCallNumber + ' ' + processReturnCopyNo + " - Failed");
           return true;
         }
         controller.getSystemClerk().processReturn(processReturnCallNumber, processReturnCopyNo, fineAmountInCents);
       } // end handling of fine
       // inform clerk of success
       processReturnPanel.append(processReturnCallNumber + ' ' + processReturnCopyNo + " - Success");
       processReturnPanel.clearCatalogNumber();
     }catch (FineRequiredException neverThrownFineRequiredException) 
      {
        // already handled, do nothing
      }catch (BookCopyEvilTwinException processReturnEvilTwinException) 
       {
         JOptionPane.showMessageDialog(this, processReturnEvilTwinException.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
         processReturnPanel.append(processReturnCallNumber + ' ' + processReturnCopyNo + " - Failed");
       }catch (NoSuchCopyException processReturnNsce) 
        {
          JOptionPane.showMessageDialog(this, processReturnNsce.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
          processReturnPanel.append(processReturnCallNumber + ' ' + processReturnCopyNo + " - Failed");
        }catch (SQLException processReturnsSqlException) 
         {
           String processReturnsSqlExceptionMessage = 
                   "Return could not be processed.\n" + processReturnsSqlException.getMessage();
           JOptionPane.showMessageDialog(this, processReturnsSqlExceptionMessage, "Error", JOptionPane.ERROR_MESSAGE);
           processReturnPanel.append(processReturnCallNumber + ' ' + processReturnCopyNo + " - Failed");
         }
     return false;
   }
 
   private void search() throws HeadlessException {
     try {
             String searchTextField = SearchTextField.getText();
             String searchGetSelection = (String) SearchComboBox
                             .getSelectedItem();
             // List<Book> lob = new ArrayList<Book>();
 
             String[][] TwoDArrayToPrint = null;
 
             Borrower bor = new Borrower();
 
             if (searchGetSelection.equals("Subject")) {
                     TwoDArrayToPrint = bor.searchBookBySubject(searchTextField);
             }
             if (searchGetSelection.equals("Title")) {
                     TwoDArrayToPrint = bor.searchBookByTitle(searchTextField);
             }
             if (searchGetSelection.equals("Author")) {
                     TwoDArrayToPrint = bor.searchBookByAuthor(searchTextField);
             }
 
             String[] header1 = TwoDArrayToPrint[0];
             String[][] TwoDMinusHeader = get2DArrayMinusHeader(TwoDArrayToPrint);
 
             // print 2d array
             DefaultTableModel uTM = new DefaultTableModel(TwoDMinusHeader,
                             header1);
             SearchTable.setModel(uTM);
             SearchTable.repaint();
     } catch (SQLException S) {
             JOptionPane.showMessageDialog(this, "Could not complete transaction.\n"+S.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
 
     }
   }
 
   private boolean viewTables() throws HeadlessException {
     
     String[][] tableWithHeader;
     int numRows;
     String[][] tableWithoutHeader;
     int numCols;
     String[] header;
     try {
       tableWithHeader = controller
                       .displayTable((String) tablesComboBox.getSelectedItem());
     } catch (SQLException e) {
       String msg = "Could not retrieve the table:\n" + e.getMessage();
       JOptionPane.showMessageDialog(this, msg, "Error",
                       JOptionPane.ERROR_MESSAGE);
       return true;
     }
     header = tableWithHeader[0];
     numRows = tableWithHeader.length;
     numRows--;
     numCols = tableWithHeader[0].length;
     tableWithoutHeader = new String[numRows][];
     for (int i = 0; i < numRows; i++) {
             tableWithoutHeader[i] = new String[numCols];
     }
     for (int i = 0; i < numRows; i++) {
             System.arraycopy(tableWithHeader[i + 1], 0,
                             tableWithoutHeader[i], 0, numCols);
     }
     entitiesTable.setModel(new DefaultTableModel(tableWithoutHeader,
                     header));
     entitiesTable.repaint();
     return false;
   }
 
   private void payFine() throws HeadlessException {
     Fine f = new Fine();
     Borrower bor = new Borrower();
     Integer fid = 0;
     double amount = 0;
     String msgFine = "";
 
     String fineIDField = payFineFidTextField.getText();
     String amountField = payFineAmountTextField.getText();
     boolean okayToContinue = true;
     try {
       fid = Integer.parseInt(fineIDField);
     } catch (NumberFormatException nfe) {
       okayToContinue = false;
       JOptionPane.showMessageDialog(this, "Fine ID must be a whole number.",
               "Error", JOptionPane.ERROR_MESSAGE);
     }
     try {
       amount = Double.parseDouble(amountField);
     } catch (NumberFormatException nfe) {
       okayToContinue = false;
       JOptionPane.showMessageDialog(this, "Amount must be a dollar value.",
               "Error", JOptionPane.ERROR_MESSAGE);
     }
 
     if (okayToContinue == true) {
 
       try {
         try {
           f.setFid(fid);
           f = (Fine) f.get();
           bor = f.getBorrowing().getBorrower();
         } catch (NullPointerException npe) {
           okayToContinue = false;
           JOptionPane.showMessageDialog(this, "The fine and/or its associated borrower do not exist.",
                   "Error", JOptionPane.ERROR_MESSAGE);
         }
         if (okayToContinue == true) {
           try {
             msgFine = bor.payFine(fid, (int)(amount*100));
           } catch (SQLException e) {
             okayToContinue = false;
             JOptionPane.showMessageDialog(this, "Payment refused.",
                     "Error", JOptionPane.ERROR_MESSAGE);
           } catch (NoPaymentException npe) {
             okayToContinue = false;
             JOptionPane.showMessageDialog(this, "Amount must be a positive amount.",
                     "Error", JOptionPane.ERROR_MESSAGE);
           }
           payFineMsgLabel.setText(msgFine);
           payFineMsgLabel.repaint();
         }
       } catch (SQLException e) {
         okayToContinue = false;
         JOptionPane.showMessageDialog(this, e.getMessage(),
                 "Error", JOptionPane.ERROR_MESSAGE);
       }
     }
   }
 
 	/**
 	 * Generates text for the doButton based on GUI state.
 	 * 
 	 * @return
 	 */
 	private String generateButtonText() {
 		String buttonText = null;
 		switch (this.state) {
 		case TABLES:
 			buttonText = "View";
 			break;
 		case START:
 			buttonText = "Go";
 			break;
 		case SEARCH:
 			buttonText = "Search";
 			break;
 		case CHECK_ACCOUNT:
 			buttonText = "Check account";
 			break;
 		case HOLD_REQUEST:
 			buttonText = "Place request";
 			break;
 		case PAY_FINE:
 			buttonText = "Pay";
 			break;
 		case ADD_BORROWER:
 			buttonText = "Add";
 			break;
 		case CHECK_OUT:
 			buttonText = "Check out";
 			break;
 		case CHECK_OVERDUE:
 			buttonText = "Check";
 			break;
 		case PROCESS_RETURN:
 			buttonText = "Process return";
 			break;
 		case ADD_BOOK:
 		case ADD_COPY:
 			buttonText = "Add";
 			break;
 		case REMOVE_BOOK:
 		case REMOVE_BORROWER:
 			buttonText = "Remove";
 			break;
 		case REPORT_POPULAR:
 		case REPORT_CHECKED_OUT:
 			buttonText = "Generate report";
 			break;
 		default:
 		}
 		return buttonText;
 	}
 
 	/**
 	 * Tells the GUI to clear the current panel
 	 * 
 	 * @param evt
 	 */
 	private void clearButtonActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_clearButtonActionPerformed
 
 		switch (state) {
 		case TABLES:
 			entitiesTable.setModel(new DefaultTableModel());
 			entitiesTable.repaint();
 			break;
 		case START:
 			navigationComboBox.setSelectedItem(START_STRING);
 			break;
 		case SEARCH:
                         SearchTextField.setText("");
                         SearchTable.setModel(new DefaultTableModel());
                         SearchTable.repaint();
 			break;
 		case CHECK_ACCOUNT:
                         SearchIdField.setText("");
                         checkedOutBooksTable.setModel(new DefaultTableModel());
                         checkedOutBooksTable.repaint();
                         finesTable.setModel(new DefaultTableModel());
                         finesTable.repaint();
                         currentHoldsTable.setModel(new DefaultTableModel());
                         currentHoldsTable.repaint();
 			break;
 		case HOLD_REQUEST:
 			holdRequestPanel.clear();
 			break;
 		case PAY_FINE:
                         payFineFidTextField.setText("");
                         payFineAmountTextField.setText("");
                         payFineMsgLabel.setText("");
 			break;
 		case ADD_BORROWER:
                         addBorrowerTextAddress.setText("");
                         addBorrowerTextEmail.setText("");
                         addBorrowerTextName.setText("");
                         addBorrowerTextPassword.setText("");
                         addBorrowerTextPhoneNo.setText("");
                         addBorrowerTextSinOrStNo.setText("");
                         expiryDateTextField.setText("");
                         expiryMonthTextField.setText("");
                         expiryYearTextField.setText("");
 			break;
 		case CHECK_OUT:
                         checkOutTextBorid.setText("");
                         checkOutTextBorid.setEditable(true);
                         checkOutTextCallNo.setText("");
                         checkOutTextCopyNo.setText("");
                         checkOutTableReceiptTable.setModel
                                 (new DefaultTableModel
                                         (new Object [][] {}, 
                                         new String [] {"Borrower ID", "Call Number", "Copy Number"}));
 			break;
 		case CHECK_OVERDUE:
                         checkOverdueTable.setModel(new DefaultTableModel());
                         checkOverdueTable.repaint();
                                 
 			break;
 		case PROCESS_RETURN:
                         processReturnPanel.clear();
 			break;
 		case ADD_BOOK:
                         abOpStatus.setBackground(Color.WHITE);
                         abOpStatus.setForeground(Color.BLACK);
                         abOpStatus.setText("Waiting for Input...");
                         abPub.setText("");
                         abSpinner.setValue(1);
                         abAA.setText("");
                         abCN.setText("");
                         abISBN.setText("");
                         abMA.setText("");
                         abSubs.setText("");
                         abTitle.setText("");
                         abYear.setText("");
 			break;
 		case ADD_COPY:
                         abcCN.setText("");
                         abcSpinner.setValue(1);
 			break;
 		case REMOVE_BOOK:
 			removeBookPrimaryNoTextField.setText("");
 			removeBookSecondaryNoTextField.setText("");
 			removeBookYearTextField.setText("");
 			removeBookWhichCopiesTextField.setText("");
 			break;
 		case REMOVE_BORROWER:
 			removeBorrowerTextField.setText("");
 			break;
 		case REPORT_POPULAR:
 			popularReportYearTextField.setText("");
 			popularReportNTextField.setText("");
 			popularReportTable.setModel(new DefaultTableModel());
 			popularReportTable.repaint();
 			break;
 		case REPORT_CHECKED_OUT:
 			checkedOutReportFilterCheckBox.setSelected(false);
 			checkedOutReportTextField.setText("");
 			checkedOutReportTable.setModel(new DefaultTableModel());
 			checkedOutReportTable.repaint();
 			break;
 		default:
 		}
 	}// GEN-LAST:event_clearButtonActionPerformed
 
 	/**
 	 * Quit is selected from the menu
 	 * 
 	 * @param evt
 	 */
 	private void quitMenuItemActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_quitMenuItemActionPerformed
 		formWindowClosing(null);
 		System.exit(0);
 	}// GEN-LAST:event_quitMenuItemActionPerformed
 
 	private void reconnectMenuItemActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_reconnectMenuItemActionPerformed
 		controller.reconnect();
 	}// GEN-LAST:event_reconnectMenuItemActionPerformed
 
 	private void abCNActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_abCNActionPerformed
 		// TODO add your handling code here:
 	}// GEN-LAST:event_abCNActionPerformed
 
 	private void abISBNActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_abISBNActionPerformed
 		// TODO add your handling code here:
 	}// GEN-LAST:event_abISBNActionPerformed
 
 	private void abcCNActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_abcCNActionPerformed
 		// TODO add your handling code here:
 	}// GEN-LAST:event_abcCNActionPerformed
 
 	private void addBorrowerComboBoxTypeActionPerformed(
 			java.awt.event.ActionEvent evt) {// GEN-FIRST:event_addBorrowerComboBoxTypeActionPerformed
 		// TODO add your handling code here:
 	}// GEN-LAST:event_addBorrowerComboBoxTypeActionPerformed
 
 	/**
 	 * Parses a string of copy numbers, delimited by the comma, ','. You can
 	 * specify a range using '-' It changes it to an int.
 	 * 
 	 * @param copyNumbers
 	 * @return
 	 */
 	private int[] parseBookCopyCopyNumbers(String copyNumbers) {
 		// filter out C
 		String copyNumbersFilteredForC = "";
 		int length;
 		length = copyNumbers.length();
 		for (int i = 0; i < length; i++) {
 			char charAt = copyNumbers.charAt(i);
 			if (charAt != 'c' && charAt != 'C') {
 				copyNumbersFilteredForC += charAt;
 			}
 			// else leave it out
 		}
 
 		// tokenize by ','
 		String[] copyNumbersSplitOnComma = copyNumbersFilteredForC.split(",");
 		HashSet<Integer> copyNumbersSet = new HashSet();
 		length = copyNumbersSplitOnComma.length;
 		for (int i = 0; i < length; i++) {
 			String trimmedCopyNumber = copyNumbersSplitOnComma[i].trim();
 			try {
 				if (trimmedCopyNumber.contains("-")) {
 					String[] splitRange = trimmedCopyNumber.split("-");
 					int start = Integer.parseInt(splitRange[0].trim());
 					int end = Integer.parseInt(splitRange[1].trim());
 					for (int j = start; j <= end; j++) {
 						copyNumbersSet.add(j);
 					}
 				} else {
 					copyNumbersSet.add(Integer.parseInt(trimmedCopyNumber));
 				}
 			} catch (NumberFormatException e) {
 				// it's not a copy number - ignore it
 			}
 		} // end for
 
 		length = copyNumbersSet.size();
 		int[] copyNumbersArray = new int[length];
 		Iterator<Integer> setIterator = copyNumbersSet.iterator();
 		int copyNumbersIndex = 0;
 		while (setIterator.hasNext()) {
 			copyNumbersArray[copyNumbersIndex] = setIterator.next();
 			copyNumbersIndex++;
 		}
 		Arrays.sort(copyNumbersArray);
 		return copyNumbersArray;
 
 	}
 
 	/**
 	 * @param args
 	 *            the command line arguments
 	 */
 	public static void main(String args[]) {
 		/* Set the Nimbus look and feel */
 		// <editor-fold defaultstate="collapsed"
 		// desc=" Look and feel setting code (optional) ">
 		/*
 		 * If Nimbus (introduced in Java SE 6) is not available, stay with the
 		 * default look and feel. For details see
 		 * http://download.oracle.com/javase
 		 * /tutorial/uiswing/lookandfeel/plaf.html
 		 */
 		try {/*
 			 * for (javax.swing.UIManager.LookAndFeelInfo info :
 			 * javax.swing.UIManager.getInstalledLookAndFeels()) { if
 			 * ("Nimbus".equals(info.getName())) {
 			 * javax.swing.UIManager.setLookAndFeel(info.getClassName()); break;
 			 * } }
 			 */
 			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 
 		} catch (ClassNotFoundException ex) {
 			java.util.logging.Logger.getLogger(ViewFrame.class.getName()).log(
 					java.util.logging.Level.SEVERE, null, ex);
 		} catch (InstantiationException ex) {
 			java.util.logging.Logger.getLogger(ViewFrame.class.getName()).log(
 					java.util.logging.Level.SEVERE, null, ex);
 		} catch (IllegalAccessException ex) {
 			java.util.logging.Logger.getLogger(ViewFrame.class.getName()).log(
 					java.util.logging.Level.SEVERE, null, ex);
 		} catch (javax.swing.UnsupportedLookAndFeelException ex) {
 			java.util.logging.Logger.getLogger(ViewFrame.class.getName()).log(
 					java.util.logging.Level.SEVERE, null, ex);
 		}
 		// </editor-fold>
 
 		/* Create and display the form */
 		java.awt.EventQueue.invokeLater(new Runnable() {
 
                         @Override
 			public void run() {
 				new ViewFrame().setVisible(true);
 			}
 		});
 	}
 
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JComboBox SearchComboBox;
     private javax.swing.JTextField SearchIdField;
     private javax.swing.JTable SearchTable;
     private javax.swing.JTextField SearchTextField;
     private javax.swing.JPanel SearchTopPanel;
     private javax.swing.JTabbedPane TabbedPane;
     private javax.swing.JTextField abAA;
     private javax.swing.JLabel abAALabel;
     private javax.swing.JTextField abCN;
     private javax.swing.JLabel abCNLabel;
     private javax.swing.JTextField abISBN;
     private javax.swing.JLabel abISBNLabel;
     private javax.swing.JTextField abMA;
     private javax.swing.JLabel abMainAuthorLabel;
     private javax.swing.JLabel abMainLabel;
     private javax.swing.JPanel abMainPanel;
     private javax.swing.JTextField abOpStatus;
     private javax.swing.JLabel abOpStatusLabel;
     private javax.swing.JTextField abPub;
     private javax.swing.JLabel abPubLabel;
     private javax.swing.JSpinner abSpinner;
     private javax.swing.JLabel abSpinnerLabel;
     private javax.swing.JPanel abStatusPanel;
     private javax.swing.JTextField abSubs;
     private javax.swing.JLabel abSubsLabel;
     private javax.swing.JTextField abTitle;
     private javax.swing.JLabel abTitleLabel;
     private javax.swing.JTextField abYear;
     private javax.swing.JLabel abYearLabel;
     private javax.swing.JTextField abcCN;
     private javax.swing.JLabel abcCNLabel;
     private javax.swing.JPanel abcMainPanel;
     private javax.swing.JLabel abcMainPanelLabel;
     private javax.swing.JSpinner abcSpinner;
     private javax.swing.JLabel abcSpinnerLabel;
     private javax.swing.JMenuItem addBookCopyMenuItem;
     private javax.swing.JMenuItem addBookMenuItem;
     private javax.swing.JComboBox addBorrowerComboBoxType;
     private javax.swing.JLabel addBorrowerLabelAddress;
     private javax.swing.JLabel addBorrowerLabelEmail;
     private javax.swing.JLabel addBorrowerLabelName;
     private javax.swing.JLabel addBorrowerLabelPassword;
     private javax.swing.JLabel addBorrowerLabelPhoneNo;
     private javax.swing.JLabel addBorrowerLabelSinOrStNo;
     private javax.swing.JLabel addBorrowerLabelType;
     private javax.swing.JMenuItem addBorrowerMenuItem;
     private javax.swing.JPanel addBorrowerPanel;
     private javax.swing.JTextField addBorrowerTextAddress;
     private javax.swing.JTextField addBorrowerTextEmail;
     private javax.swing.JTextField addBorrowerTextName;
     private javax.swing.JTextField addBorrowerTextPassword;
     private javax.swing.JTextField addBorrowerTextPhoneNo;
     private javax.swing.JTextField addBorrowerTextSinOrStNo;
     private javax.swing.JMenu addMenu;
     private javax.swing.JPanel addNewBookPanel;
     private javax.swing.JPanel addNewCopyPanel;
     private javax.swing.JMenu borrowerMenu;
     private javax.swing.JPanel buttonPanel;
     private javax.swing.JPanel cardPanel;
     private javax.swing.JMenuItem checkAccountMenuItem;
     private javax.swing.JPanel checkAccountPanel;
     private javax.swing.JScrollPane checkOutBooksPane;
     private javax.swing.JButton checkOutButtonAdd;
     private javax.swing.JLabel checkOutDummyLabel;
     private javax.swing.JPanel checkOutFieldsPanel;
     private javax.swing.JPanel checkOutFieldsPanelReceipt;
     private javax.swing.JLabel checkOutLabelBorid;
     private javax.swing.JLabel checkOutLabelCallNo;
     private javax.swing.JLabel checkOutLabelCopyNo;
     private javax.swing.JMenuItem checkOutMenuItem;
     private javax.swing.JPanel checkOutPanel;
     private javax.swing.JScrollPane checkOutTableReceipt;
     private javax.swing.JTable checkOutTableReceiptTable;
     private javax.swing.JTextField checkOutTextBorid;
     private javax.swing.JTextField checkOutTextCallNo;
     private javax.swing.JTextField checkOutTextCopyNo;
     private javax.swing.JButton checkOverdueButtonMessage;
     private javax.swing.JLabel checkOverdueLabelBorrInfo;
     private javax.swing.JMenuItem checkOverdueMenuItem;
     private javax.swing.JPanel checkOverduePanel;
     private javax.swing.JPanel checkOverduePanelMessage;
     private javax.swing.JScrollPane checkOverdueScrollPaneInfo;
     private javax.swing.JTable checkOverdueTable;
     private javax.swing.JTable checkedOutBooksTable;
     private javax.swing.JCheckBox checkedOutReportFilterCheckBox;
     private javax.swing.JPanel checkedOutReportFilterPanel;
     private javax.swing.JMenuItem checkedOutReportMenuItem;
     private javax.swing.JPanel checkedOutReportPanel;
     private javax.swing.JTable checkedOutReportTable;
     private javax.swing.JScrollPane checkedOutReportTablePane;
     private javax.swing.JPanel checkedOutReportTablePanel;
     private javax.swing.JTextField checkedOutReportTextField;
     private javax.swing.JButton clearButton;
     private javax.swing.JMenu clerkMenu;
     private javax.swing.JScrollPane currentHoldsPane;
     private javax.swing.JTable currentHoldsTable;
     private javax.swing.JButton doButton;
     private javax.swing.JTable entitiesTable;
     private javax.swing.JLabel expiryDateLabel;
     private javax.swing.JTextField expiryDateTextField;
     private javax.swing.JTextField expiryMonthTextField;
     private javax.swing.JTextField expiryYearTextField;
     private javax.swing.JMenu fileMenu;
     private javax.swing.JScrollPane finesPane;
     private javax.swing.JTable finesTable;
     private javax.swing.JMenu helpMenu;
     private javax.swing.JMenuItem holdRequestMenuItem;
     private GUI.PlaceHoldRequestPanel holdRequestPanel;
     private javax.swing.JLabel jLabel1;
     private javax.swing.JScrollPane jScrollPane1;
     private javax.swing.JMenu librarianMenu;
     private javax.swing.JPanel mainPanel;
     private javax.swing.JMenuItem manualMenuItem;
     private javax.swing.JMenuBar menuBar;
     private javax.swing.JComboBox navigationComboBox;
     private javax.swing.JMenu navigationMenu;
     private javax.swing.JPopupMenu.Separator navigationSeparator;
     private javax.swing.JLabel paddingLabel;
     private javax.swing.JTextField payFineAmountTextField;
     private javax.swing.JLabel payFineAmtLabel;
     private javax.swing.JLabel payFineFidLabel;
     private javax.swing.JTextField payFineFidTextField;
     private javax.swing.JPanel payFineInputPanel;
     private javax.swing.JLabel payFineInstructionLabel;
     private javax.swing.JMenuItem payFineMenuItem;
     private javax.swing.JLabel payFineMsgLabel;
     private javax.swing.JPanel payFinePanel;
     private javax.swing.JMenuItem popularReportMenuItem;
     private javax.swing.JLabel popularReportNSelectLabel;
     private javax.swing.JTextField popularReportNTextField;
     private javax.swing.JPanel popularReportOptionsPanel;
     private javax.swing.JPanel popularReportPanel;
     private javax.swing.JTable popularReportTable;
     private javax.swing.JScrollPane popularReportTablePane;
     private javax.swing.JPanel popularReportTablePanel;
     private javax.swing.JLabel popularReportYearSelectLabel;
     private javax.swing.JTextField popularReportYearTextField;
     private javax.swing.JMenuItem processReturnMenuItem;
     private GUI.ProcessReturnPanel processReturnPanel;
     private javax.swing.JMenuItem quitMenuItem;
     private javax.swing.JMenuItem reconnectMenuItem;
     private javax.swing.JRadioButton removeBookBookRadioButton;
     private javax.swing.ButtonGroup removeBookButtonGroup;
     private javax.swing.JLabel removeBookCallNumberLabel;
     private javax.swing.JPanel removeBookCallNumberPanel;
     private javax.swing.JMenuItem removeBookMenuItem;
     private javax.swing.JRadioButton removeBookOnlyTheseCopiesRadioButton;
     private javax.swing.JPanel removeBookPanel;
     private javax.swing.JTextField removeBookPrimaryNoTextField;
     private javax.swing.JPanel removeBookRadioButtonPanel;
     private javax.swing.JTextField removeBookSecondaryNoTextField;
     private javax.swing.JTextField removeBookWhichCopiesTextField;
     private javax.swing.JTextField removeBookYearTextField;
     private javax.swing.JPanel removeBorrowerInputPanel;
     private javax.swing.JLabel removeBorrowerLabel;
     private javax.swing.JMenuItem removeBorrowerMenuItem;
     private javax.swing.JPanel removeBorrowerPanel;
     private javax.swing.JTextField removeBorrowerTextField;
     private javax.swing.JMenu removeMenu;
     private javax.swing.JMenu reportMenu;
     private javax.swing.JPanel searchAccountPanel;
     private javax.swing.JMenuItem searchMenuItem;
     private javax.swing.JPanel searchPanel;
     private javax.swing.JPanel startComboBoxPanel;
     private javax.swing.JMenuItem startMenuItem;
     private javax.swing.JPanel startPanel;
     private javax.swing.JMenuItem tableMenuItem;
     private javax.swing.JPanel tableNamePanel;
     private javax.swing.JComboBox tablesComboBox;
     private javax.swing.JPanel tablesPanel;
     private javax.swing.JScrollPane viewTablePane;
     // End of variables declaration//GEN-END:variables
 
 	private Controller controller;
 	private State state;
 	private HashMap<String, State> statemap;
 
 	/**
 	 * The possible states that the GUI can be in. One state for every panel/
 	 * functional requirement. There are 16 states in total.
 	 */
 	private enum State {
 		// System functionality
 		TABLES, START,
 		// Borrower functionality
 		SEARCH, CHECK_ACCOUNT, HOLD_REQUEST, PAY_FINE,
 		// Clerk functionality
 		ADD_BORROWER, CHECK_OUT, CHECK_OVERDUE, PROCESS_RETURN,
 		// Librarian functionality
 		ADD_BOOK, ADD_COPY, REMOVE_BOOK, REMOVE_BORROWER, REPORT_POPULAR, REPORT_CHECKED_OUT
 	}
 
 	private static final String TABLES = "View tables";
 	private static final String START_STRING = "Start";
 	private static final String SEARCH = "Search for book";
 	private static final String CHECK_ACCOUNT = "Check account";
 	private static final String HOLD_REQUEST = "Place hold request";
 	private static final String PAY_FINE = "Pay a fine";
 	private static final String ADD_BORROWER = "Add a new borrower";
 	private static final String CHECK_OUT = "Check-out books";
 	private static final String CHECK_OVERDUE = "Check overdue books";
 	private static final String PROCESS_RETURN = "Process a return";
 	private static final String ADD_BOOK = "Add new book";
 	private static final String ADD_COPY = "Add new book copy";
 	private static final String REMOVE_BOOK = "Remove books and copies";
 	private static final String REMOVE_BORROWER = "Remove borrower";
 	private static final String REPORT_POPULAR = "Popular books report";
 	private static final String REPORT_CHECKED_OUT = "Checked-out report";
 	private JSplitPane splitPane;
 
 	/**
 	 * Changes a an integer representation of a copy number to a string
 	 * representation i.e. 1 becomes C1
 	 * 
 	 * @param copyNumbersAsInts
 	 * @return
 	 */
 	private static String[] mapCopyNumberIntsToCopyNumberStrings(
 			int[] copyNumbersAsInts) {
 		int numCopyNumbers = copyNumbersAsInts.length;
 		String[] copyNumbersAsStrings = new String[numCopyNumbers];
 		for (int i = 0; i < numCopyNumbers; i++) {
 			copyNumbersAsStrings[i] = "C" + copyNumbersAsInts[i];
 		}
 		return copyNumbersAsStrings;
 	}
 }
