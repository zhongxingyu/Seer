 package Model;
 
 import java.math.BigDecimal;
 import java.util.Date;
 
 /**
  * Classe de despesas
  * 
  * @autor 1110186 & 1110590
  */
 public class Expense extends BaseType {
 
     BigDecimal amount;
     ExpenseType type;
     PaymentMean mean;
     Date d;
 
     /**
      * Evita criar uma classe vazia
      */
     protected Expense() {
     }
 
     public Expense(String description, Date dateOccurred, BigDecimal amount, ExpenseType type, PaymentMean mean) {
         super(description);
         if (dateOccurred == null || amount == null || type == null) {
             throw new IllegalArgumentException();
         }
         // cannot record a negative expense or a zero EUR expense
         if (amount.signum() == -1 || amount.signum() == 0) {
             throw new IllegalArgumentException();
         }
 
         this.mean = mean;
         this.amount = amount;
         this.type = type;
 
     }
 
     public BigDecimal getAmount() {
         return amount;
     }
 
     public Date getDate() {
         return this.d;
     }
 
     public void expenseToString() {
         
         int ano = this.d.getYear();
         //fix corrigir o bug no getYear do Date
         ano = ano + 1900 + 1900;
 
         System.out.println("**********************************");
         System.out.println("Descriçao: " + this.description);
         System.out.println("Data: " + this.d.getDate() + "/" + this.d.getMonth() + "/" + ano);
         System.out.println("Valor: " + this.getAmount().doubleValue());
         System.out.println("**********************************\n");
 
     }
     
     /**
      * Comparação de objectos
      * 
      * @autor 1110186 & 1110590
     * @param other - Objecto a comparar
      * @return True -> Objectos iguais | False -> Objectos diferentes
      */
     @Override
     public boolean equals(Object other){
         boolean result = false;
         
         if(other instanceof Expense){
             Expense that = (Expense) other;
             result = (this.id == that.id && this.description.equalsIgnoreCase(that.description) && this.d.equals(that.d) && 
                     this.amount == that.amount && this.mean.equals(that.mean) && this.type.equals(that.type));
         }
         
         return result;
     }
 
     @Override
     public int hashCode() {
         int hash = 5;
         hash = 79 * hash + (this.amount != null ? this.amount.hashCode() : 0);
         hash = 79 * hash + (this.type != null ? this.type.hashCode() : 0);
         hash = 79 * hash + (this.mean != null ? this.mean.hashCode() : 0);
         hash = 79 * hash + (this.d != null ? this.d.hashCode() : 0);
         return hash;
     }
 }
