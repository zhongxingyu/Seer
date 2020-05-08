 package com.jjensen.exersises.format;
 
 public class Main {
 
     public static void main(String[] args) {
         Order order = new Order();
         order.addItem(new Product("Monitor", 2, 150.00));
         order.addItem(new Product("1TB HD", 2, 150.00));
         order.addItem(new Product("Tower", 1, 300.00));
        order.addItem(new Product("SomthingReally Long", 1, 12.00));
 
         Invoice invoice = order.generateInvoice();
         invoice.print();
 
 
     }
 }
