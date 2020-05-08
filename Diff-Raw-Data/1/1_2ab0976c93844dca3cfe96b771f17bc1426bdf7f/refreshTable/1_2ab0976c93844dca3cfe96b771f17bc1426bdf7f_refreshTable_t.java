 package Helpdesk.java.helpdesk.lib;
 /******************
  * Imports
  ******************/
 import Helpdesk.java.helpdesk.mvc.Model.Counter;
 import Helpdesk.java.helpdesk.mvc.Model.CustomerTable;
 import Helpdesk.java.helpdesk.mvc.Model.EmployeeTable;
 import Helpdesk.java.helpdesk.mvc.Model.FullticketTable;
 import Helpdesk.java.helpdesk.mvc.Model.HistoryTable;
 import Helpdesk.java.helpdesk.mvc.Model.ProductTable;
 import Helpdesk.java.helpdesk.mvc.View.Error_Frame;
 import Helpdesk.java.helpdesk.mvc.View.Main_Frame;
 
 public class refreshTable extends Thread {
     String c_model;
     String e_model;
     String f_model;
     String h_model;
     String p_model;
     Main_Frame _view;
     
     public refreshTable(String c_model, String e_model, String f_model, String h_model,
                                String p_model, Main_Frame _view) {
       this.c_model = c_model;
       this.e_model = e_model;
       this.f_model = f_model;
       this.h_model = h_model;
       this.p_model = p_model;
      this._view = _view;
     }
    
     /*****************
     *  JTable refresh
     ********************/
     @Override
     public void run() {
       try {  
             if (this.c_model.equals("Customer")) {
                 Thread t = new Thread (CustomerTable.getInstance());
                 t.start();
                 t.join(400);
             }
             if (this.e_model.equals("Employee")){
                 Thread t = new Thread (EmployeeTable.getInstance());
                 t.start();
                 t.join(400);
             }
             if (this.f_model.equals("Fullticket")) {
                 FullticketTable.getInstance().setStatus("");
                 Thread t = new Thread (FullticketTable.getInstance());
                 t.start();
                 t.join(400);
             }
             if (this.h_model.equals("History")) {
                 Thread t = new Thread (HistoryTable.getInstance());
                 t.start();
                 t.join(400);
             }
             if (this.p_model.equals("Product")) {
                 Thread t = new Thread (ProductTable.getInstance());
                 t.start();
                 t.join(400);
             }
             if (_view != null) {
                 Thread t = new Counter (_view);
                 t.start();
                 t.join(400);
             }
       } catch (InterruptedException e) {
           Error_Frame.Error(e.toString());
       }
     }
     
 }
