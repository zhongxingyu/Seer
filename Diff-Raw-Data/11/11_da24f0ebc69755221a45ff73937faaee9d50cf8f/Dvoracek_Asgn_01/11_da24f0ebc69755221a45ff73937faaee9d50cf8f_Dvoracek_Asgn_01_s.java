 
 package dvoracek_asgn_01;
 
 /**
  *SEIS 610: Software Engineering
  * @author Chad Dvoracek
  * Assignment #1
  * Due Date: March 2nd
  * 
  */
 
 import javax.swing.JOptionPane;
 public class Dvoracek_Asgn_01 {
 
      /**
      * @param args the command line arguments
      * Target Function(Fitness Function): Y = (5x^2-5)/4
      *
      * Random Functions are:  Y=(2x^2+2)/6 and Y=(8x^2-9)/9 
      * 
      */
     public static void main(String[] args) {
     // TODO code application logic here
     // assign variables 
      int n,x,nx,arraycount, r1score, r2score, beginx, lastx, arraysize;
      String testset1, testset2, t1,r1, r2;
      double target,random1,random2,test1,test2 ;
      
      //initialize variables
      r1score=0;
      r2score=0;
      x=-25;
      nx=25;
      beginx=x;
      arraycount=0;
      t1="Y=(5x^2-5)/4";
      r1="Y=(2x^2+2)/6";
      r2="Y=(8x^2-9)/9";
      arraysize=(Math.abs (beginx-nx))+1;
      
      /**Test arraysize
      * JOptionPane.showMessageDialog (null,"Array size = "+(arraysize));
      * name output arrays
      * */
     
      //String [] TestSetOutput1 = new String[arraysize]; 
      //String [] TestSetOutput2 = new String[arraysize];
      //String [] TestSetOutputTF= new String [arraysize];
     
     String [] FinalOutputTF = new String [arraysize];
     String [] FinalOutput1= new String[arraysize];
     String [] FinalOutput2= new String[arraysize];
   
      
      //Loop statement to test values.
      
      while (x<=nx)
      { 
      target= (5*(Math.pow(x,2))-5)/4;
      random1= (2*(Math.pow(x,2))+2)/6;
      random2= (2*(Math.pow(x,2))-9)/9;
      
      test1= Math.abs (target-random1);
      test2= Math.abs(target-random2);
      
      /** Testing Values Code 
      *  JOptionPane.showMessageDialog (null,"Value of test1 = "+test1);
      *  JOptionPane.showMessageDialog (null,"Value of test2 = "+test2);
      */  
      
      //Asign numerical values to the winner of each test set
           
      if (test1<test2) {
         r1score++;
             }
      else {
         r2score++;
             }
     
      /**test counting of rscore values
      * JOptionPane.showMessageDialog (null,"Value of r1score = "+r1score);
      * JOptionPane.showMessageDialog (null,"Value of r2score = "+r2score);
      */
     
      
      /** testing calculation comparison of formulas 
      *  System.out.println ("this is the value of target1 "+target);
      *  System.out.println ("this is the value of random1 "+random1);
      *  System.out.println ("this is the value of random2 "+random2);
      */
      
  
     //Capturing Test Set Data
     
     //Testing Data Set
     //System.out.print("Random 1: X"+(x+1)+"="+x+" ,Y"+x+"="+random1);
     
     //TestSetOutputTF[arraycount]="X"+(arraycount+1)+"="+x+" ,Y"+(arraycount+1)+"="+target;
     //TestSetOutput1[arraycount]="X"+(arraycount+1)+"="+x+" ,Y"+(arraycount+1)+"="+random1;
     //TestSetOutput2[arraycount]="X"+(arraycount+1)+"="+x+" ,Y"+(arraycount+1)+"="+random2;
     
     FinalOutputTF[arraycount]=x+", "+target;
     FinalOutput1[arraycount]=x+", "+random1;
     FinalOutput2[arraycount]=x+", "+random2;
     
     
     
     arraycount++;
     x++;
      
      }
      //Final Output for copy and paste
      if (r1score>r2score) {    
        System.out.println ("Target fuction "+t1); 
         System.out.println("Random 1 ("+r1+")score="+r1score);
         System.out.println("Random 2 ("+r2+")score="+r2score);
         System.out.println("X was tested from "+beginx+" to "+nx);
         JOptionPane.showMessageDialog (null,"X was tested from "+beginx+" to "+nx+".\nRandom 1 "+r1+" wins "+r1score+" to "+r2score+".\nIt is closer to the target function of \n"+t1);
         
             }
      else {
        System.out.println ("Target fuction "+t1); 
         System.out.println("Random 1 ("+r1+")score="+r1score);
         System.out.println("Random 2 ("+r2+")score="+r2score);
         System.out.println("X was tested from "+beginx+" to "+nx);
         JOptionPane.showMessageDialog (null,"X was tested from "+beginx+" to "+nx+".\nRandom 2 "+r2+" wins "+r2score+" to "+r1score+".\nIt is closer to the target function of \n"+t1);
             }
     
      System.out.println ("\nTarget function "+t1+" response to test set.\n");
     
        for (int tf=0; tf < arraysize; tf++)
     { 
         System.out.println (FinalOutputTF[tf]);
         //verify out put data System.out.println (TestSetOutputTF[tf]);
     } 
     System.out.println("\nTest set for Random 1 "+r1+":\n");
     
     for (int fnl1=0; fnl1 < arraysize; fnl1++)
     { 
         System.out.println (FinalOutput1[fnl1]);
         //verify output data System.out.println (TestSetOutput1[fnl1]);
     }
    System.out.println("\n\nTest set for Random 2 "+r2+":\n"); 
     
     for (int fnl2=0; fnl2 < arraysize; fnl2++)
     { 
         System.out.println (FinalOutput2[fnl2]);
         //verify output data System.out.println (TestSetOutput2[fnl2]);
     }
     
     System.out.println ("The End");
     }
 }
