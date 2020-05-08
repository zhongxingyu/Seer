 import java.util.Scanner;
 
 public class recursiveDrivenGrammer
 {
     public static String grammer = "";
 
     public static void main(String args[])
     {
         Scanner in = new Scanner(System.in);
         System.out.print("Grammer: ");
         grammer = in.nextLine();
         system_goal();
 
         if (grammer.trim().length() == 0)
             System.out.println("Text is grammer compatible!");
         else
             System.out.println("Text is not grammer compatible!");
     }
 
     public static boolean start(String token)
     {
         if (grammer.trim().startsWith(token.trim()))
             return true;
         else
             return false;
     }
 
     public static void remove(String token)
     {
         if (start(token))
         {
             grammer = grammer.trim().substring(token.trim().length(),
                     grammer.trim().length());
         }
         else
         {
             System.out.println("Text is not grammer compatible!");
             System.exit(1);
         }
     }
 
     public static void system_goal()
     {
         if (start("begin"))
         {
             program();
             remove("$");
         }
     }
 
     public static void program()
     {
         if (start("begin"))
         {
             remove("begin");
             statement_list();
             remove("end");
         }
     }
 
     public static void statement_list()
     {
         if (start("Id") || start("read") || start("write"))
         {
             statement();
             statement_tail();
         }
        else
            grammer = "$" + grammer;
        return;
     }
 
     public static void statement_tail()
     {
         if (start("Id") || start("read") || start("write"))
         {
             statement();
             statement_tail();
         }
         else
             return;
     }
     
     public static void statement()
     {
         if (start("Id"))
         {
             remove("Id");
             remove(":");
             remove("=");
             expression();
             remove(";");
         }
         else if (start("read"))
         {
             remove("read");
             remove("(");
             id_list();
             remove(")");
             remove(";");
         }
         else if (start("write"))
         {
             remove("write");
             remove("(");
             expr_list();
             remove(")");
             remove(";");
         }
     }
 
     public static void id_list()
     {
         if (start("Id"))
         {
             remove("Id");
             id_tail();
         }
     }
 
     public static void id_tail()
     {
         if (start(","))
         {
             remove(",");
             remove("Id");
             id_tail();
         }
         else
             return;
     }
 
     public static void expr_list()
     {
         if (start("Id") || start("INTLIT"))
         {
             expression();
             expr_tail();
         }
     }
 
     public static void expr_tail()
     {
         if (start(","))
         {
             remove(",");
             expression();
             expr_tail();
         }
         else
             return;
     }
 
     public static void expression()
     {
         if (start("Id") || start("INTLIT"))
         {
             primary();
             primary_tail();
         }
     }
 
     public static void primary_tail()
     {
         if (start("+") || start("-"))
         {
             add_op();
             primary();
             primary_tail();
         }
         else if (start("("))
         {
             remove("(");
             expression();
             remove(")");
         }
         else
             return;
     }
 
     public static void primary()
     {
         if (start("Id"))
         {
             remove("Id");
         }
         else if (start("INTLIT"))
             remove("INTLIT");
     } 
 
     public static void add_op()
     {
         if (start("+"))
             remove("+");
         else if (start("-"))
             remove("-");
     }
 }
