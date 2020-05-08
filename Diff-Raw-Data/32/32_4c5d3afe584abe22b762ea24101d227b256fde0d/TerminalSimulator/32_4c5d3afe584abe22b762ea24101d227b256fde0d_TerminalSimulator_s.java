 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package chocansimulator;
 
import chocansimulator.datamangement.Member;
 import chocansimulator.datamangement.Provider;
 import chocansimulator.datamangement.ServiceCode;
 import chocansimulator.reports.ServiceCodeDirectoryReport;
 
 /**
  *
  * @author debbj
  */
 public class TerminalSimulator {
 
     public int validateMember(int number) {
         Member m = (Member) ChocAnUserInterface.memberMan.search(number);
 
         if ( m == null )
             return(-1);
         else
             return m.getStatus();
     }
 
     public int validateProvider(int number) {
         Provider p = (Provider) ChocAnUserInterface.providerMan.search(number);
 
         if ( p == null )
             return(-1);
         else
             return p.getStatus();
     }
 
     public float validateServiceCode(int number) {
         ServiceCode s = (ServiceCode) ChocAnUserInterface.svcMan.search(number);
 
         if ( s == null ) {
             return(-1);
         } else {
             if (s.getStatus() == 0)
                 return 0;
             else
                 return s.getFee();
         }
     }
 
     public boolean requestServiceDirectory() {
         ServiceCodeDirectoryReport r = new ServiceCodeDirectoryReport();
         return r.createReport();
 
     }
 
 }
