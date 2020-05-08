 package org.bxmy.shiftclock.shiftduty;
 
 import java.util.ArrayList;
 
 public class ShiftDuty {
 
     private ArrayList<Duty> mDuties = new ArrayList<Duty>();
 
     public ShiftDuty() {
         addTestDuties();
     }

     public String[] getDutyNames() {
         ArrayList<String> names = new ArrayList<String>();
         for (Duty duty : mDuties) {
             names.add(duty.getName());
         }
 
         return (String[]) names.toArray();
     }

     private void addTestDuties() {
         Duty duty = new Duty();
         duty.setName("白班");
         mDuties.add(duty);
     }
 }
