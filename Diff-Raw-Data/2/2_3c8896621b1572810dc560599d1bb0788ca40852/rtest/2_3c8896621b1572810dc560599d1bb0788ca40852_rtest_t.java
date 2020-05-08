package org.rosuda.JRI;

 import java.io.*;
 
 import java.awt.*;
 import javax.swing.*;
 
 import org.rosuda.JRI.Rengine;
 import org.rosuda.JRI.REXP;
 import org.rosuda.JRI.RMainLoopCallbacks;
 
 class TextConsole implements RMainLoopCallbacks
 {
 	JFrame f;
 	
     public JTextArea textarea = new JTextArea();
 
     public TextConsole() {
         f = new JFrame();
         f.getContentPane().add(new JScrollPane(textarea));
         f.setSize(new Dimension(800,600));
         f.show();
     }
 
     public void rWriteConsole(Rengine re, String text) {
         textarea.append(text);
         //System.out.print(text);
     }
     
     public void rBusy(Rengine re, int which) {
         System.out.println("rBusy("+which+")");
     }
     
     public String rReadConsole(Rengine re, String prompt, int addToHistory) {
         System.out.print(prompt);
         try {
             BufferedReader br=new BufferedReader(new InputStreamReader(System.in));
             String s=br.readLine();
             return (s==null||s.length()==0)?s:s+"\n";
         } catch (Exception e) {
             System.out.println("jriReadConsole exception: "+e.getMessage());
         }
         return null;
     }
     
     public void rShowMessage(Rengine re, String message) {
         System.out.println("rShowMessage \""+message+"\"");
     }
 	
 	public String rChooseFile(Rengine re, int newFile) {
 		FileDialog fd = new FileDialog(f, (newFile==0)?"Select a file":"Select a new file", (newFile==0)?FileDialog.LOAD:FileDialog.SAVE);
 		fd.show();
 		String res=null;
 		if (fd.getDirectory()!=null) res=fd.getDirectory();
 		if (fd.getFile()!=null) res=(res==null)?fd.getFile():(res+fd.getFile());
 		return res;
 	}
 	
     public void   rFlushConsole (Rengine re) {
 	}
 	
     public void   rLoadHistory  (Rengine re, String filename) {
 	}			
 
     public void   rSaveHistory  (Rengine re, String filename) {
 	}			
 }
 
 public class rtest {
     public static void main(String[] args) {
         System.out.println("Press <Enter> to continue (time to attach the debugger if necessary)");
         try { System.in.read(); } catch(Exception e) {};
         System.out.println("Creating Rengine (with arguments)");
 	Rengine re=new Rengine(args, true, new TextConsole());
         System.out.println("Rengine created, waiting for R");
         if (!re.waitForR()) {
             System.out.println("Cannot load R");
             return;
         }
 
         //java.awt.Frame f=new java.awt.Frame("hello");
         //f.setVisible(true);
 
         // simple assignment (env=0 means use R_GlobalEnv)
         long xp1 = re.rniPutString("hello");
         re.rniAssign("a", xp1, 0);
 
         // Example: how to create a named list or data.frame
         double da[] = {1.2, 2.3, 4.5};
         double db[] = {1.4, 2.6, 4.2};
         long xp3 = re.rniPutDoubleArray(da);
         long xp4 = re.rniPutDoubleArray(db);
         
         // now build a list
         long la[] = {xp3, xp4};
         long xp5 = re.rniPutVector(la);
 
         // now let's add names
         String sa[] = {"a","b"};
         long xp2 = re.rniPutStringArray(sa);
         re.rniSetAttr(xp5, "names", xp2);
 
         // ok, we have a proper list now
         // we could use assign and then eval "b<-data.frame(b)", but for now let's build it by hand:       
         String rn[] = {"1", "2", "3"};
         long xp7 = re.rniPutStringArray(rn);
         re.rniSetAttr(xp5, "row.names", xp7);
         
         long xp6 = re.rniPutString("data.frame");
         re.rniSetAttr(xp5, "class", xp6);
         
         // assign the whole thing to the "b" variable
         re.rniAssign("b", xp5, 0);
         
         if (true) {
             System.out.println("Letting go; use main loop from now on");
             return;
         }
         
         {
             System.out.println("Parsing");
             long e=re.rniParse("data(iris)", 1);
             System.out.println("Result = "+e+", running eval");
             long r=re.rniEval(e, 0);
             System.out.println("Result = "+r+", building REXP");
             REXP x=new REXP(re, r);
             System.out.println("REXP result = "+x);
         }
         {
             System.out.println("Parsing");
             long e=re.rniParse("iris", 1);
             System.out.println("Result = "+e+", running eval");
             long r=re.rniEval(e, 0);
             System.out.println("Result = "+r+", building REXP");
             REXP x=new REXP(re, r);
             System.out.println("REXP result = "+x);
         }
         {
             System.out.println("Parsing");
             long e=re.rniParse("names(iris)", 1);
             System.out.println("Result = "+e+", running eval");
             long r=re.rniEval(e, 0);
             System.out.println("Result = "+r+", building REXP");
             REXP x=new REXP(re, r);
             System.out.println("REXP result = "+x);
             String s[]=x.asStringArray();
             if (s!=null) {
                 int i=0; while (i<s.length) { System.out.println("["+i+"] \""+s[i]+"\""); i++; }
             }
         }
         {
             System.out.println("Parsing");
             long e=re.rniParse("rnorm(10)", 1);
             System.out.println("Result = "+e+", running eval");
             long r=re.rniEval(e, 0);
             System.out.println("Result = "+r+", building REXP");
             REXP x=new REXP(re, r);
             System.out.println("REXP result = "+x);
             double d[]=x.asDoubleArray();
             if (d!=null) {
                 int i=0; while (i<d.length) { System.out.print(((i==0)?"":", ")+d[i]); i++; }
                 System.out.println("");
             }
             System.out.println("");
         }
         {
             REXP x=re.eval("1:10");
             System.out.println("REXP result = "+x);
             int d[]=x.asIntArray();
             if (d!=null) {
                 int i=0; while (i<d.length) { System.out.print(((i==0)?"":", ")+d[i]); i++; }
                 System.out.println("");
             }
         }
 
         re.eval("print(1:10/3)");
         
         if (false) {
             re.eval("X11()");
             re.eval("plot(rnorm(1000))");
             re.eval("x<-rnorm(100000)");
             re.eval("y<-rnorm(100000)");
             re.eval("for(i in 1:10) lm(y~x,subset=sample(100000,10000))");
         }
         System.out.println("R is ready, press <Enter> to continue (time to attach the debugger is necessary)");
         try { System.in.read(); } catch(Exception e2) {};
 
 	//f.dispose();
 
         re.end();
         System.out.println("end");
     }
 }
