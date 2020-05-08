 import java.util.List;
 import java.util.Arrays;
 import dataHistographs.*;
 
 public class Util{
     public static boolean arrayEquals(double[] a1, double[] a2)
     {
         if (a1==null || a2==null)
             return false;
         if (a1.length!=a2.length)
             return false;
         for (int i=0; i<a1.length; i++)
         {
             if (a1[i]!=a2[i])
                 return false;
         }
         return true;
     }
     public static void writeRuns(String fileName, List<HistData> runs)
     {
         try
         {
             java.io.PrintWriter output = new java.io.PrintWriter(new java.io.FileWriter(fileName));
             output.print(stringRuns(runs));
             output.close();
         }
         catch(Exception e)
         {
             System.out.println(e.getMessage());
             System.exit(0);
         }
     }
     public static String stringRuns(List<HistData> runs)
     {
         if(runs == null)
         {
             System.out.println("shit! shit! shit! something is very wrong! why are you not giving me any runs?!");
             return "";
         }
         int timeLength = (runs.get(0)).size();
         
         String lineStr;
         String bigPrintStr="";
         for(int row = 0; row<timeLength;row++)
         {
             HistData runInCol;
             lineStr = "" + (row*-10);
             //this loops across all of the runs and selects one CO2 value from each
             for(int col = 0; col<runs.size(); col++)
             {
                 runInCol = runs.get(col);
                 lineStr+="\t"+(runInCol.getCO2(row));
             }
            System.out.println(lineStr);
             bigPrintStr+=lineStr+"\n";
         }
         return bigPrintStr;
     }
     
     
     
     
     public static void printDeclare(boolean staticV, String[] type, String[] name)
     {
         String s = "";
         if (staticV)
              s= " static ";
         String declare="private" +s+ type[0]+" "+name[0];
         for(int i = 1; i<type.length; i++)
         {
             if(type[i-1].equals(type[i]))
                 declare+=", "+name[i];
             else
                 declare+=";\n"+"private" +s+ type[i]+" "+name[i];
         }
         System.out.println(declare+";");
     }
     public static String makeGet(String type, String name)
     {
         return "    public "+type+" get"+(name.substring(0,1)).toUpperCase()+name.substring(1)+"()\n"+"    {\n"+"        return "+name+";\n"+"    }";
     }
     public static void printGetters(String type, String[] varNames)
     {
         String[] types=new String[varNames.length];
         Arrays.fill(types, type);
         printGetters(types, varNames);
     }
     public static void printGetters(String[] type, String[] varNames)
     {
         for (int i=0; i<type.length; i++)
         {
             System.out.println();
             System.out.print(makeGet(type[i], varNames[i]));
             System.out.println();
         }
     }
     public static String makeSet(String type, String name)
     {
         return "    public void set"+(name.substring(0,1)).toUpperCase()+name.substring(1)+"("+type+" newValue)\n"+"    {\n"+"        "+name+"=newValue;\n"+"    }";
     }
     public static void printSetters(String type, String[] varNames)
     {
         String[] types=new String[varNames.length];
         Arrays.fill(types, type);
         printSetters(types, varNames);
     }
     public static void printSetters(String[] type, String[] varNames)
     {
         for (int i=0; i<type.length; i++)
         {
             System.out.println();
             System.out.print(makeSet(type[i], varNames[i]));
             System.out.println();
         }
     }
     public static void constructHelper(String[] store, String[] varType, String[] params)
     {
         String pars=varType[0]+" "+params[0];
         String storestr=store[0]+"="+params[0]+";\n";
         for(int i = 1; i<store.length; i++)
         {
             
             pars+=", "+varType[i]+" "+params[i];
             storestr+="\n"+store[i]+"="+params[i]+";";
         }
         printDeclare(false, varType, store);
         System.out.println();
         System.out.println(pars);
         System.out.println();
         System.out.println(storestr);
     }
     public static void constructHelper(String[] store, String varType, String[] params)
     {
         String[] vt = new String[params.length];
         Arrays.fill(vt, varType);
         constructHelper(store, vt, params);
     }
     public static void constructVarMimic(String[] varType, String[] varsBase, String strPre, String strSuf, String parPre, String parSuf)
     {
         
         String[] parPass = new String[varType.length];
         String[] strPass= new String[varType.length];
         
         for(int i = 0; i<varType.length; i++)
         {
             parPass[i] = parPre+varsBase[i]+parSuf;
             strPass[i] = strPre+varsBase[i]+strSuf;
             
         }
         constructHelper(strPass, varType, parPass);
     }
     public static String[] newArray(int len, String filler)
     {
         String[] s=new String[len];
         Arrays.fill(s, filler);
         return s;
     }
 }
