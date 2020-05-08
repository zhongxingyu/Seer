 package ru.spbau.WhereIsMyMoney;
 
 import java.util.Date;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * User: Alexander Opeykin alexander.opeykin@gmail.com
  * Date: 11/12/12
  * Time: 3:32 PM
  */
 public class Transaction {
 	public static final int WITHDRAW = 0;
 	public static final int DEPOSIT = 1;
 	public static final int NONE = 2;
 
     private Date date;
     private String place;
     private String card;
     private String delta;
     private float balance;
     private int type;
     private String currency = null;
     private float amount = 0;
 
     private final Pattern currencyPattern = Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*(.*)\\s*");
     private static final int AMOUNT_GROUP = 1;
     private static final int CURRENCY_GROUP = 2;
 
     public Transaction() {
     }
     
     public Transaction(Date date, String place, String card, String delta, float balance, int type) {
         this.date = date;
         this.place = place == null ? "" : place;
         this.card = card;
         this.delta = delta;
         this.balance = balance;
         this.type = type;
 
        Matcher matcher = currencyPattern.matcher(delta);
 
         if (matcher.matches()) {
             this.amount = Float.valueOf(matcher.group(AMOUNT_GROUP));
             String currency = matcher.group(CURRENCY_GROUP);
             this.currency = currency == null ? "" : currency;
         }
     }
 
     public Date getDate() {
         return date;
     }
 
     public String getPlace() {
         return place;
     }
 
     public String getCard() {
         return card;
     }
 
     public String getDelta() {
         return delta;
     }
 
     public float getAmount() {
         return amount;
     }
     public String getCurrency() {
         return currency;
     }
 
     public float getBalance() {
         return balance;
     }
     
     public int getType() {
     	return type;
     }
 
     public void setDate(Date date) {
         this.date = date;
     }
 
     public void setPlace(String place) {
         this.place = place;
     }
 
     public void setCard(String card) {
         this.card = card;
     }
 
     public void setDelta(String delta) {
         this.delta = delta;
     }
 
     public void setBalance(float balance) {
         this.balance = balance;
     }
 
     public void setType(int type) {
         this.type = type;
     }
 }
