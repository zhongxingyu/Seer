 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package edu.syr.bytecast.fsys.test.implementation;
 
 import edu.syr.bytecast.fsys.ExeObj;
 import edu.syr.bytecast.fsys.ExeObjDependency;
 import edu.syr.bytecast.fsys.IBytecastFsys;
 import edu.syr.bytecast.fsys.elf.ElfExeObjParser;
 import java.io.IOException;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *
  * @author dhrumin
  */
 public class TestParser {
     
    public static void main(String[] args)
    {
         try {
             IBytecastFsys testObject = new ElfExeObjParser();
             testObject.setFilepath("test_input/a.out");
             List<ExeObjDependency> lists =   testObject.parse().getDependencies();
             for(ExeObjDependency dep : lists)
             {
                 System.out.println(dep.getDependencyName());
             }
        } catch (IOException ex) {
             Logger.getLogger(TestParser.class.getName()).log(Level.SEVERE, null, ex);
         }
    }
 }
