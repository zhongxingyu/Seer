 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package edu.syr.bytecast.fsys.test.implementation;
 
 import edu.syr.bytecast.fsys.ExeObjDependency;
 import edu.syr.bytecast.fsys.IBytecastFsys;
 import edu.syr.bytecast.fsys.elf.ElfExeObjParser;
 import edu.syr.bytecast.fsys.test.interfaces.ITestCase;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 /**
  *
  * @author dhrumin
  */
 public class FSysTestCase implements ITestCase {
 
     private TestResult m_result;
     private String m_inputFilePath;
     private Set <String> m_inputDependencies;
     private String m_testName;
     
     public FSysTestCase(String inputFilePath)
     {
         m_testName = "Default Test Name";
         m_inputFilePath = inputFilePath;
         m_inputDependencies = new HashSet<String>();        
         m_result = new TestResult();
     }
     
     public FSysTestCase(String inputFilePath, String testName)
     {
         m_testName = testName;
         m_inputFilePath = inputFilePath;
         m_inputDependencies = new HashSet<String>();        
         m_result = new TestResult();
     }
     
     public FSysTestCase(String inputFilePath, Set<String> dependencies)
     {
         m_testName = "DefaultName";
         m_inputFilePath = inputFilePath;
         this.m_inputDependencies = dependencies;        
         m_result = new TestResult();
     }
     
     public void addDependencyInput(String dependency)
     {
        m_inputDependencies.add(dependency);
     }
     
     @Override
     public TestResult getResult() {
         try {
             
             //Capturing Console Output
             ByteArrayOutputStream baos = new ByteArrayOutputStream();
             PrintStream ps = new PrintStream(baos);
             PrintStream old_ps = System.out;
             System.setOut(ps);
             
             IBytecastFsys testObject = new ElfExeObjParser();
             testObject.setFilepath(m_inputFilePath);
            List<ExeObjDependency> lists =   testObject.parse().getDependencies();
             for(ExeObjDependency dep : lists)
             {
                 if(!m_inputDependencies.contains(dep.getDependencyName()))
                 {
                     System.out.println("Dependency " + dep + "Not Found");
                     m_result.setPassed(false);
                 } else
                     m_inputDependencies.remove(dep.getDependencyName());
             }
             
             for(String notCheckedDependencies : m_inputDependencies)
             {
                 System.out.println("Dependency " + notCheckedDependencies + "not returned ");
                 m_result.setPassed(false);
             }
             
           System.setOut(old_ps);
        } catch (IOException ex) {
            ex.printStackTrace();
         }
         return m_result;
     }
 
     @Override
     public String getTestName() {
         return m_testName;
     }
     
     public void setTestName(String name)
     {
         this.m_testName = name;
     }
     
 }
