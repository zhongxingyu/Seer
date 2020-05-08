 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Presentation;
 
 import Controllers.BaseController;
 import Controllers.DefPaymentMeansController;
 import eapli.util.Console;
 import java.math.BigDecimal;
 import java.util.Date;
 import Controllers.ExpenseRegisterController;
 import Controllers.ExpenseTypeController;
 import Model.ExpenseType;
 import Model.PaymentMeans;
 import Persistence.PaymentMeansRepository;
 import java.util.List;
 
 /**
  *
  * @author
  */
 public class ExpenseRegisterUI extends BaseUI{
 
     public ExpenseRegisterUI(){}
       
       ExpenseRegisterController controller = new ExpenseRegisterController();
       
       public BaseController controller(){
           return controller;
       }
 
       @Override
       public void run() {
 
             String desc = Console.readLine("\nDescription:");
             Date date = Console.readDate("\nWhen (dd-MM-yyyy):");
             double value = Console.readDouble("\nAmount:");
             BigDecimal amount = new BigDecimal(value);
             System.out.println("\nExpense Type:");
 
             List<ExpenseType> list=controller.ListAllTypes();
 
            
            displayList(list);
             
             int op=Console.readInteger("\nChoose an option");
             
            ExpenseType exptype=new ExpenseType(list.get(op-1));
             
             System.out.println("\nPayment mean:");
 
             List<PaymentMeans> listMeans = controller.ListAllMeans();
             
             displayListPM(listMeans);
             
             int opPM = Console.readInteger("\nChoose an option");
             
            PaymentMeans pay=new PaymentMeansImpl(listMeans.get(opPM-1));
                         
             System.out.println("\nDo you wish to leave a comment?");
             String opComment=Console.readLine("\n(y/n)");
             
             String comment="";
             if("y".equals(opComment))
             {
                 comment=Console.readLine("");
             }
             
             controller.registerExpense(desc, date, amount, exptype, comment, pay);
 
             System.out.println("expense recorded.");
       }
       
       @Override
       public void header()
       {
           System.out.println("*** REGISTER AN EXPENSE ***");
       }

    private static class PaymentMeansImpl extends PaymentMeans {

        public PaymentMeansImpl(PaymentMeans get) {
            super();
        }
    }    
     public void displayList(List<ExpenseType> list)
     {
         int i=0;
         System.out.println("List of Types\n");
         for (ExpenseType exptype : list) {
             i=i+1;
             System.out.println("Type "+i+"\n" + exptype);
         }
     }
     
       public void displayListPM(List<PaymentMeans> listMeans) {
         int i = 0;
         System.out.println("List of Payment Means\n");
         for (PaymentMeans payMean : listMeans) {
             i = i + 1;
             System.out.println("Mean " + i + "\n" + payMean);
         }
     }
      
     
 }
