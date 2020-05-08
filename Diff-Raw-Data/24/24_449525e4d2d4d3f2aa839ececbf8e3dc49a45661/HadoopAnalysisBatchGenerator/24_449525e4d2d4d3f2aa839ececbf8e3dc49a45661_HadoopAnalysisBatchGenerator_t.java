 package misc.scripting;
 
 import java.io.*;
 import java.util.ArrayList;
 import java.util.Map;
 
 /**
  * Created with IntelliJ IDEA.
  * Date: 5/13/13
  * Time: 12:36 PM
  *
 * java -classpath CalculateTiTv.jar misc.scripting.HadoopAnalysisBatchGenerator
 *
  * @author Wai Lok Sibon Li
  */
 public class HadoopAnalysisBatchGenerator {
 
     private final static String fileNameIdentifier = "[FILENAME]";
 
     public static void main (String[] args){
 
         String templateFileName = "commands.txt";
         try {
             templateFileName=args[0];
         }catch(Exception e) {}
 
 
         String fileNameList = "file_list.txt";
         try {
             fileNameList=args[1];
         }catch(Exception e) {}
 
         String outfileName = "hadoop_script.sh";
         try {
             fileNameList=args[2];
         }catch(Exception e) {}
 
        String headerCommand = "header_commands.txt";
         try {
             templateFileName=args[3];
         }catch(Exception e) {}
 
         readWriteFiles(templateFileName, headerCommand, fileNameList, outfileName);
         runCommands(outfileName) ;
 
     }
 
     private static void readWriteFiles(String templateFileName, String headerCommand, String fileNameList, String outfileName) {
         ArrayList<String> filesList = new ArrayList<String>();
         try {
             String line;
             BufferedReader fileListReader = new BufferedReader(new FileReader(fileNameList));
             while((line = fileListReader.readLine())!=null) {
                 filesList.add(line);
             }
             fileListReader.close();
         }catch(IOException e) {
             e.printStackTrace();
         }
 
         ArrayList<String> templateLines = new ArrayList<String>();
         try {
             String line;
             BufferedReader templateFileReader = new BufferedReader(new FileReader(templateFileName));
             while((line = templateFileReader.readLine())!=null) {
                 templateLines.add(line);
             }
             templateFileReader.close();
         }catch(IOException e) {
             e.printStackTrace();
         }
 
         try {
             File outfile = new File(outfileName);
             PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(outfile)), true);
 
             String line;
            File headerFile = new File(headerCommand);
            if(headerFile.exists()) {
                BufferedReader headerFileReader = new BufferedReader(new FileReader(headerFile));
                 while((line = headerFileReader.readLine())!=null) {
                     pw.println(line);
                 }
                 headerFileReader.close();
 
             }
 
 
             for(int i=0; i<filesList.size(); i++) {
                 for(int j=0; j<templateLines.size(); j++) {
                    StringBuilder s2 = new StringBuilder(templateLines.get(j));
                    int index2;
                    while((index2=s2.indexOf(fileNameIdentifier))!=-1) {
                        s2.replace(index2, index2+fileNameIdentifier.length(), filesList.get(i));
                        //s2.replace(index2, index2+fileNameIdentifier.length(), geneName);
                    }
                    pw.println(s2);
                    System.out.println("hit it " + i + "\t" + j);
                 }
             }
             boolean isExecutable = outfile.setExecutable(true);
             if(!isExecutable) {
                 throw new RuntimeException("File not not be set to executable");
             }
         }catch(IOException e) {
             e.printStackTrace();
         }
     }
 
     public static void runCommands(String inputFile) {
         String so=null;
         try {
 //            String command = "java -Xmx1000m -classpath \"beast.jar\" dr.app.tools.TreeAnnotator -burnin 1000 \"" + inputFileName + "\" " + taOutputFileName;
             //System.out.println(command);
 
 //            ProcessBuilder pb = new ProcessBuilder(inputFile, "myArg1", "myArg2");
            ProcessBuilder pb = new ProcessBuilder("/bin/bash", inputFile);
             Map<String, String> env = pb.environment();
 
             Process p = pb.start();
 //            Process p = Runtime.getRuntime().exec(command);
             ByteArrayOutputStream protTestOutput = new ByteArrayOutputStream();
             StreamGobbler errorGobbler = new StreamGobbler(p.getErrorStream(), "ERR");
             StreamGobbler outputGobbler = new StreamGobbler(p.getInputStream(), "OUT", protTestOutput);
             errorGobbler.start();
             outputGobbler.start();
             int exitVal = p.waitFor();
             so = protTestOutput.toString();
             //System.out.println(so);
 
             //BufferedReader stdInput = new BufferedReader(new StringReader(so));
             //String s;
             //while ((s = stdInput.readLine()) != null) {
 
             //}
             //stdInput.close();
         } catch (IOException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates
         }catch (InterruptedException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates
         } /*catch (TreeParseException e) {
 			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates
 		}*/ catch (Exception e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates
         }
     }
 
     /* Runs BEAST as a process */
 //    public static void runBEAST(String geneName, int count, String beastLocation) {
 //        try {
 //            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(geneName+"."+count+".stdout")), true);
 //            Process p = Runtime.getRuntime().exec("java -classpath \""+beastLocation+"/beastdev.jar\" dr.app.beastdev.BeastMain "+geneName+"."+count+".xml");
 //
 //            BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
 //            BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
 //            String s;
 //            while ((s = stdInput.readLine()) != null) {
 //                out.println(s);
 //            }
 //            while ((s = stdError.readLine()) != null) {
 //                out.println(s);
 //            }
 //            p.waitFor();
 //
 //            stdInput.close();
 //            stdError.close();
 //            out.close();
 //
 //
 //        }catch(Exception e) {e.printStackTrace();}
 //    }
 
 
 }
