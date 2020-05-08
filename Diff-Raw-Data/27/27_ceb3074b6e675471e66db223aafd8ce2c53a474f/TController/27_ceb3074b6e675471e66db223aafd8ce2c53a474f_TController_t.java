 package mvc.Controller;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.sql.Timestamp;
 import java.util.Calendar;
 import java.util.Timer;
 import java.util.TimerTask;
 import lib.refreshTable;
 import mvc.Model.Counter;
 import mvc.Model.FullticketTable;
 import mvc.Model.HistoryTable;
 import mvc.Model.Ticket;
 import mvc.View.Ticket_Frame;
 import mvc.View.Error_Frame;
 import mvc.View.Main_Frame;
 
 public class TController implements Runnable{
     private Integer ID;
     private FullticketTable f_model;
     private HistoryTable h_model;
     private Main_Frame main;
     private Ticket_Frame _view;
     
     public TController(Integer ID, FullticketTable f_model,HistoryTable h_model, Main_Frame main, Ticket_Frame frame) {
       this.ID = ID;  
       this.f_model = f_model;
       this.h_model = h_model;
       this.main = main;
       this._view = frame;
       addListener();
     }
     
     @Override
     public void run() {
         if (this.ID != null) {
             searching (this.ID);
         }
         this._view.init();
     }
     
     private void addListener() {
         this._view.setbtn_cancelListener(new btn_cancelListener());
         this._view.setbtn_searchListener(new btn_csearchListener());
         this._view.setbtn_saveListener(new btn_saveListener());
         this._view.setchb_newListener(new chb_newListener());
         init();
     }
     
     private void init() {
         _view.cmb_status.setModel(new javax.swing.DefaultComboBoxModel(mvc.Model.Ticket.Ticket_ComboBox(4).toArray(
                                   new Object[mvc.Model.Ticket.Ticket_ComboBox(4).size()])));
         _view.cmb_category.setModel(new javax.swing.DefaultComboBoxModel(mvc.Model.Ticket.Ticket_ComboBox(3).toArray(
                                     new Object[mvc.Model.Ticket.Ticket_ComboBox(3).size()])));
         _view.cmb_eID.setModel(new javax.swing.DefaultComboBoxModel(mvc.Model.Ticket.Ticket_ComboBox(2).toArray(
                                new Object[mvc.Model.Ticket.Ticket_ComboBox(2).size()])));
         _view.cmb_cID.setModel(new javax.swing.DefaultComboBoxModel(mvc.Model.Ticket.Ticket_ComboBox(1).toArray(
                                new Object[mvc.Model.Ticket.Ticket_ComboBox(1).size()])));
 
     }
     
       /*************************************
       * 
       *     ButtonListener
       * 
       **************************************/
 
     class btn_cancelListener implements ActionListener{
         @Override
         public void actionPerformed(ActionEvent e) {  
             _view.dispose();
         }
     }
     
     class btn_csearchListener implements ActionListener{
         @Override
         public void actionPerformed(ActionEvent e) {  
           try {
             Integer ini = null;
             String Str = _view.edt_ID.getText();
             if (!"".equals(Str)) { 
                 ini = Integer.parseInt (Str);
                 searching(ini);  
             }
           } catch (NullPointerException E){
             Error_Frame.Error("ID not found");
           } catch (NumberFormatException E) {
             Error_Frame.Error("Please use only number for ID");
           }
         }
     }
     
     
     class btn_saveListener implements ActionListener{
         @Override
         public void actionPerformed(ActionEvent e) {  
            try {
             //set timestamp for "create tickets" and "update tickets"
             Timestamp currentTimestamp = new java.sql.Timestamp(Calendar.getInstance().getTime().getTime());
             //send Error frame if one of these textfield are empty
             if ("".equals(_view.edt_topic.getText()) || "".equals(_view.edt_problem.getText())){
                 Error_Frame.Error("Please fill out: Topic and Problem"); 
             } else {
                    String sol = null,note = null;
                    Integer noEm = null;
                    ID = null;
                    if ("".equals(_view.edt_solution.getText())) {
                        sol = _view.edt_solution.getText();
                    }
                    if ("".equals(_view.edt_note.getText())) {
                        note = _view.edt_note.getText();
                    }
                
                 //check checkbox "new Ticket"
                 if (_view.chb_new.getSelectedObjects() == null) {
                     String Str = _view.edt_ID.getText();
                     ID = Integer.parseInt (Str);  
                     if (_view.cmb_eID.getSelectedItem() != "") {
                         noEm = (Integer)_view.cmb_eID.getSelectedItem();
                     }
                     //get timestamp and string from textfield and update ticket
                    if ("".equals(_view.edt_solution.getText())) {
                        sol = _view.edt_solution.getText();
                    }
                    if ("".equals(_view.edt_note.getText())) {
                        note = _view.edt_note.getText();
                    }
                     Ticket updateTicket = new Ticket (ID,
                     (Integer)_view.cmb_cID.getSelectedItem(),
                     noEm,
                     (Integer)_view.cmb_category.getSelectedItem(),
                     (Integer)_view.cmb_status.getSelectedItem(),
                     _view.edt_topic.getText(),
                     _view.edt_problem.getText(),
                     _view.edt_note.getText(),
                     _view.edt_solution.getText(),
                     _view.edt_created.getText(),
                     currentTimestamp.toString());
                     updateTicket.updateTicket(ID);
                 } else {
                 if (_view.cmb_eID.getSelectedItem() != "") {
                     noEm = (Integer)_view.cmb_eID.getSelectedItem();
                 }
                 //get timestamp and string from textfield and create ticket
                 Ticket newTicket = new Ticket (ID,
                 (Integer)_view.cmb_cID.getSelectedItem(),
                 noEm,
                 (Integer)_view.cmb_category.getSelectedItem(),
                 (Integer)_view.cmb_status.getSelectedItem(),
                 _view.edt_topic.getText(),
                 _view.edt_problem.getText(),
                note,
                sol,
                 currentTimestamp.toString(),
                 currentTimestamp.toString());
                 newTicket.newTicket();
                 }                     
                 //refresh jtable
                 refreshTable A1;
                 A1 = new refreshTable(null, null, f_model, h_model, null);
                 A1.start();
                 //count ticket status for fullticket control buttons
                 //timer to prevent connection link lost
                 Timer timer = new Timer();
                 timer.schedule  (new Task(), 500);
                 _view.dispose();
             }
         } catch (NumberFormatException ev) {
               Error_Frame.Error("Please use only number for ID");
         } catch (Exception ev) {
               Error_Frame.Error(e.toString()); 
         }
         }
     }
     
     
       /*************************************
       * 
       *     Timer
       * 
       **************************************/
     class Task extends TimerTask {
         @Override
         public void run() {
                 Counter A2;
                 A2 = new Counter(main);
                 A2.start();
         }
     }
     
     
       /*************************************
       * 
       *     Checkbox - ItemListener
       * 
       **************************************/
      
     class chb_newListener implements ItemListener{
         @Override
         public void itemStateChanged(ItemEvent e) {
          if (e.getStateChange() == ItemEvent.DESELECTED){
             _view.edt_ID.setVisible(true);
          } else {
             _view.edt_ID.setVisible(false);
             _view.edt_ID.setText("");
             _view.edt_problem.setText("");
             _view.edt_topic.setText("");
             _view.edt_note.setText("");
             _view.edt_solution.setText("");
             _view.edt_created.setText("");
             _view.edt_update.setText("");
         }
         }
     }
     
     
      /**************************
      *  
      *  User defined functions
      *  
      ***************************/
     
     /*
     *  search ticket and fill textfield/combobox
     */
     public void searching (Integer ID) {
         try {
             String [] Array = Ticket.searchTicket(ID);
             _view.edt_ID.setText(ID.toString());
             _view.edt_topic.setText(Array[4]);
             _view.edt_problem.setText(Array[5]);
             _view.edt_note.setText(Array[6]);
             _view.edt_solution.setText(Array[7]);
             _view.edt_created.setText(Array[8]);
             _view.edt_update.setText(Array[9]);
       
             for (int i=0; i<= _view.cmb_cID.getItemCount()-1;i++) {
                 if (Array[0].equals(_view.cmb_cID.getItemAt(i).toString())) {
                     _view.cmb_cID.setSelectedIndex(i);
                  }
             }
             if (Array[1]!=null) {
                 for (int i=0; i<= _view.cmb_eID.getItemCount()-1;i++) {
                     if (Array[1].equals(_view.cmb_eID.getItemAt(i).toString())) {
                         _view.cmb_eID.setSelectedIndex(i);
                     }
                 }
             }
             
             for (int i=0; i<= _view.cmb_category.getItemCount()-1;i++) {
                 if (Array[2].equals(_view.cmb_category.getItemAt(i).toString())) {
                     _view.cmb_category.setSelectedIndex(i);
                  }
             }
             
             for (int i=0; i<= _view.cmb_status.getItemCount()-1;i++) {
                 if (Array[3].equals(_view.cmb_status.getItemAt(i).toString())) {
                     _view.cmb_status.setSelectedIndex(i);
                  }
             }
         } catch (NullPointerException E){
             Error_Frame.Error("ID not found");
         } catch (NumberFormatException E) {
             Error_Frame.Error(E.toString());
         }
     }
     
     
       /*****************************************************************
      *  Count ticket status like open,in process and closed in fulltickettable
      *  and set counts in fullticket control buttons
      ********************************************************************/
     public void getStatusCount () {
         Integer openCount=0,processCount=0,closedCount=0;
         Object [] A2 = Counter.getCount();
                for (int i=0;i<=A2.length-1;i++) {
                 if ("Open".equals(A2[i])) {
                     openCount++;
                 } else if ("In process".equals(A2[i])) {
                     processCount++;
                 } else if ("Closed".equals(A2[i])) {
                     closedCount++;
                 }
                }
         main.btn_setopen.setText("Open [" + openCount+"]");
         main.btn_setprocess.setText("In Process [" + processCount +"]");
         main.btn_setclosed.setText("Closed [" + closedCount +"]");
     }
 }
