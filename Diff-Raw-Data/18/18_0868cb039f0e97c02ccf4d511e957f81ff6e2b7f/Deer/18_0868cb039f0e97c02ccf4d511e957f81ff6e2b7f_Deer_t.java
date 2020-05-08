 package com.cegeka.nocturne.godgame;
 
 public class Deer implements Creature {
     private int age;
 
     @Override
     public int getAge() {
         return age;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
    public void setAge(int age) {
        this.age = age;
    }

    @Override
     public void age() {
         age++;
     }
 
     @Override
     public boolean shouldDie() {
         return age == 30;
     }

    @Override
    public String render() {
        return "D";
    }
 }
