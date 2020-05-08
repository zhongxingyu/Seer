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
         
         System.out.println("Selecione o tipo de despesa");
         ExpenseRepository eR = new ExpenseRepository();
         List<TypeOfExpense> lista = new ArrayList<TypeOfExpense>();
         lista = eR.getListTExpense();
         int i;
        for(i = 0; i < lista.size(); i++)
         {
             System.out.println(i+1+":"+lista.get(i));
         }
         int type = Console.readInteger("Indice:");
         
         Date date = Console.readDate("Data da Despesa:");
         //PayMode pM = new PayMode();//Input tipo de pagamento
         //Input detalhes de pagamento
         
         String what = Console.readLine("Comentário:");
         
         
         ExpenseRegisterController controller = new ExpenseRegisterController();
         controller.registerExpense(amount,lista.get(type),date,null,what);
         
         System.out.println("expense recorded.");
     }
 }
