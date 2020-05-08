 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package org.hibu.atek.tordf.building;
 
 import org.hibu.atek.tordf.mock.DummyBuilder;
 import java.io.File;
 
 /**
  *
  * @author Tord
  */
 public class BuilderFactory
 {
 
     public static AntBuilder GetDefaultBuilder(File dir, String NextFree)
     {
        AntBuilder ab = new DummyBuilder();//new IntegratedAntBuilder(dir, NextFree);
         return ab;
     }
 
 }
