 package Presentation;
 
 import Model.ExpenseType;
 import Controllers.RegisterExpenseController;
 import Model.PaymentMean;
 import eapli.exception.EmptyList;
 
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Scanner;
 
 /**
  *
  * @author nbento
  */
 class ExpenseRegisterUI {
 
     private static Scanner in = new Scanner(System.in);
    
     public void mainLoop() {
         try {
             System.out.println("* * *  REGISTER AN EXPENSE  * * *\n");
 
             System.out.print("Description:");
             String what = in.nextLine();
             System.out.print("When:");
             System.out.print("   ->Day:");
             int day, month, year;
             day = in.nextInt();
             System.out.print("   ->Month:");
             month = in.nextInt();
             System.out.print("   ->Year:");
             year = in.nextInt();
 
             Date date = new Date(year, month, day);
 
             System.out.print("Amount:");
             double value = in.nextDouble();
             BigDecimal amount = new BigDecimal(value);
 
             RegisterExpenseController controller = new RegisterExpenseController();
             ArrayList<ExpenseType> listExpenseType = new ArrayList<ExpenseType>(controller.getExpenseType());
             ArrayList<PaymentMean> listPaymentMean = new ArrayList<PaymentMean>(controller.getPaymentMean());
 
             System.out.println("List Expense Type:");
 
             for (ExpenseType obj : listExpenseType) {
                 System.out.println("ID:" + listExpenseType.indexOf(obj) + " | " + obj.getType());
             }
             System.out.println("Select Expense Type (ID-Number):");
             ExpenseType expenseTypeObj = listExpenseType.get(in.nextInt());
 
             System.out.println("List Payment Means:");
 
             for (PaymentMean obj : listPaymentMean) {
                 System.out.println("ID:" + listPaymentMean.indexOf(obj) + " | " + obj.getMean());
             }
 
             System.out.println("Select Payment Means (ID-Number):");
             PaymentMean paymentMeanObj = listPaymentMean.get(in.nextInt());
 
             controller.createExpense(what, date, amount, expenseTypeObj, paymentMeanObj);
 
             System.out.println("expense recorded.");
         } catch (IllegalArgumentException ex) {
             System.err.println("Illegal Argument!");
         } catch (EmptyList ex) {
             System.err.println("Expense Type List Empty");
         } catch (Exception ex){
             System.err.println("WARNING: OTHER ERROR! CONTACT ADMIN!");
         }
 
     }
 }
