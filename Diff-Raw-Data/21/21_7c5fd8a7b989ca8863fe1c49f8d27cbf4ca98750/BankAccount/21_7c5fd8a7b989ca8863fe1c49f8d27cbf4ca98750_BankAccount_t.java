 /*
  * Copyright(C) 2013 Yutaka Kato
  */
 package local.JPL.ch05.ex05_02;
 
 public class BankAccount {
     private long number; // 口座番号
     private long balance; // 現在の残高
     private Action lastAct; // 最後に行われた処理
     private History history;
 
     public class Action {
         private String act;
         private long amount;
 
         Action(String act, long amount) {
             this.act = act;
             this.amount = amount;
         }
 
         @Override
         public String toString() {
             return number + ": " + act + " " + amount;
         }
     }
 
     public void deposit(long amount) {
         balance += amount;
         lastAct = new Action("deposit", amount);
         history.add(lastAct);
     }
 
     public void withdraw(long amount) {
         balance -= amount;
         lastAct = new Action("withdraw", amount);
         history.add(lastAct);
     }
 
     public void transfer(BankAccount other, long amount) {
         other.withdraw(amount);
         deposit(amount);
         lastAct = this.new Action("transfer", amount);
         history.add(lastAct);
         other.lastAct = other.new Action("transfer", amount);
         other.history().add(other.lastAct);
     }
     
     public long getBalance() {
         return balance;
     }
     
     public Action getLastAction() {
         return lastAct;
     }
 
     public History history() {
        return history.clone();
     }
 
    public static class History implements Cloneable {
         private static final int MAX = 10;
         Action[] actions = new Action[MAX];
         int cursor = 0;
        
        private History() {
        }
 
         public Action next() {
             return actions[cursor];
         }
         
        private void add(Action act) {
             if (++cursor == MAX) {
                 cursor = 0;
             }
             actions[cursor] = act;
         }
        
        @Override
        public History clone() {
        	History history = new History();
        	history.actions = this.actions.clone();
        	history.cursor = this.cursor;
        	return history;
        }
     }
 }
