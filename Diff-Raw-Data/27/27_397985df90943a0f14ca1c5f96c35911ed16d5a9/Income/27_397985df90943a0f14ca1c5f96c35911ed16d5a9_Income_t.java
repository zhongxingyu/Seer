 package com.joragupra.budinv.model;
 
import java.util.Date;

 /**
  * @author: jagudo
  * @since: 3/03/13
  */
 public class Income extends BookkeepingEntry {

    public Income(Date date, Double amount, String title) {
        super(date, amount, title);
    }

    public Income(Date date, Double amount, String title, Category category) {
        super(date, amount, title, category);
    }
 }
