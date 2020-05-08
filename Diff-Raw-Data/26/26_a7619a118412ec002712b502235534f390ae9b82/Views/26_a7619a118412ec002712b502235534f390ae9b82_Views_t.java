 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.abada.cleia.entity.user;
 
 /**
  *
  * @author katsu
  */
 public class Views {
     public static class Public {}
     public static class Level1 extends Public{}
     public static class Level2 extends Level1{}
    public static class Level3 extends Level2{}
    public static class Level4 extends Level3{}
     public static class Hide extends Public{}
 }
