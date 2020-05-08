 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Presentation;
 
 import Controllers.ExpenseRegisterController;
 import Model.PayMode;
 import Model.TypeOfExpense;
 import Persistence.ExpenseRepository;
 
 import eapli.util.Console;
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 /**
  *
  * @author Paulo Gandra Sousa
  */
 class ExpenseRegisterUI {
     public void mainLoop() {
         System.out.println("* * *  REGISTER AN EXPENSE  * * *\n");
         
         double value = Console.readDouble("Amount:");
         BigDecimal amount = new BigDecimal(value);
         
         System.out.println("Select type of expense");
         ExpenseRepository eR = new ExpenseRepository();
         List<TypeOfExpense> lista = new ArrayList<TypeOfExpense>();
         lista = eR.getListTExpense();
         int type; /* Index of type expense */
         if(lista.size() > 0)
         {
             for(int i = 0; i < lista.size(); i++)
             {
                 System.out.println(i+1+":"+lista.get(i));
             }
            type = Console.readInteger("Select index:"); 
            type--;
         }else{
             String op;
             op = Console.readLine("Do not have any type of expense created, want to creat? [y]/[n]");
             while (!op.equalsIgnoreCase("y") && !op.equalsIgnoreCase("n"))
             {
                 op = Console.readLine("Select correct option [y]/[n]");
             }
             if(op.equalsIgnoreCase("y"))
             {
                 TypeOfExpenseUI uiTE = new TypeOfExpenseUI();
                 uiTE.mainLoop();
                 type = eR.getListTExpense().size() - 1;
             }else{
                 System.out.println("Do not possible insert a expense without first create a type of expense");
                 System.out.println("This option will be exit!\n\n");
                 return;
             }
         }
         
         
         
         Date date = Console.readDate("Data da Despesa: \"dd-MM-yyyy\"");
         //PayMode pM = new PayMode();//Input tipo de pagamento
         //Input detalhes de pagamento
         
         String what = Console.readLine("Comentário:");
         
         
         ExpenseRegisterController controller = new ExpenseRegisterController();
 
         controller.registerExpense(amount,lista.get(type),date,null,what);
      
         System.out.println("Despesa guardada com sucesso");
     }
 }
