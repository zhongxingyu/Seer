 package edu.ch.unifr.diuf.testing_tool;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.StringTokenizer;
 
 /**
  *
  * @author Teodor Macicas
  */
 public class Parser 
 {
     private File inputFile;
     private File outputFile;
     private int no_clients; 
     private int no_runs; 
     
     class ResultValuesRunning {
         public int no_threads;
         public int total_time; 
         public int no_trans; 
         public int no_conflicts; 
         public int no_aborted;
         public double trans_rate; 
         public int no_retrials; 
         
         public ResultValuesRunning() {
             no_threads = 0;
             total_time = 0;
             no_trans = 0;
             no_conflicts = 0;
             no_aborted = 0;
             trans_rate = 0.0;
             no_retrials = 0;
         }
     }
 
     class ResultValuesVersionsStats {
         public int no_entities;
         public int no_versions;
         public int no_conflicts;
         public int no_aborted;
         public double avg_time_lastCID;
         public double max_time_lastCID;
 
         public ResultValuesVersionsStats() {
             no_entities=0;
             no_versions=0;
             no_conflicts=0;
             no_aborted=0;
             avg_time_lastCID=0.0F;
             max_time_lastCID=0.0F;
         }
     }
     
     public Parser(String inputFilename, String outputFilename, 
             int no_clients, int no_runs) throws IOException { 
         this.inputFile = new File(inputFilename);
         this.outputFile = new File(outputFilename);
         if( outputFile.exists() ) {
             outputFile.delete();
             outputFile.createNewFile();
         }
         this.no_clients = no_clients;
         this.no_runs = no_runs;
     }
     
     public void parseFileRunningStats() throws FileNotFoundException, IOException {
         ResultValuesRunning values_per_config = new ResultValuesRunning();
         String line;
         int line_counter = 0;
         int entries_per_config = no_clients * no_runs;
         BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));
         // read line by line 
         BufferedReader br = new BufferedReader(new FileReader(inputFile));
         while ((line = br.readLine()) != null) {
             // process the line.
             StringTokenizer tok = new StringTokenizer(line, " ");
             values_per_config.no_threads += Integer.valueOf(tok.nextToken());
             values_per_config.total_time += Integer.valueOf(tok.nextToken());
             values_per_config.no_trans += Integer.valueOf(tok.nextToken());
             values_per_config.no_conflicts += Integer.valueOf(tok.nextToken());
             values_per_config.no_aborted += Integer.valueOf(tok.nextToken());
             values_per_config.trans_rate += Double.valueOf(tok.nextToken());
             values_per_config.no_retrials = Integer.valueOf(tok.nextToken());
             ++line_counter;
             if( line_counter == entries_per_config ) {
                 bw.write(String.valueOf(values_per_config.no_threads / no_runs));
                 bw.write(" ");
                 bw.write(String.valueOf(values_per_config.total_time / entries_per_config));
                 bw.write(" ");
                 bw.write(String.valueOf(values_per_config.no_trans / no_runs));
                 bw.write(" ");
                 bw.write(String.valueOf(values_per_config.no_conflicts / no_runs));
                 bw.write(" ");
                 bw.write(String.valueOf(values_per_config.no_aborted / no_runs));
                 bw.write(" ");
                 bw.write(String.valueOf(values_per_config.trans_rate / no_runs));
                 bw.write(" ");
                 bw.write(String.valueOf(values_per_config.no_retrials));
                 bw.newLine();
                 
                 // also reset 
                 values_per_config = new ResultValuesRunning();
                 line_counter = 0;
             }
         }
         bw.close();
         br.close();
     }
 
     public void parseFileVersionsStats() throws FileNotFoundException, IOException {
         ResultValuesVersionsStats values_per_config = new ResultValuesVersionsStats();
         String line;
         int line_counter = 0;
        int entries_per_config = no_clients * no_runs;
         BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));
         // read line by line
         BufferedReader br = new BufferedReader(new FileReader(inputFile));
         while ((line = br.readLine()) != null) {
             // process the line.
             StringTokenizer tok = new StringTokenizer(line, ", ");
             values_per_config.no_entities += Integer.valueOf(tok.nextToken());
             values_per_config.no_versions += Integer.valueOf(tok.nextToken());
             values_per_config.no_conflicts += Integer.valueOf(tok.nextToken());
             values_per_config.no_aborted += Integer.valueOf(tok.nextToken());
             values_per_config.avg_time_lastCID += Integer.valueOf(tok.nextToken().replace("ms", ""));
             values_per_config.max_time_lastCID += Integer.valueOf(tok.nextToken().replace("ms", ""));
             ++line_counter;
             if( line_counter == entries_per_config ) {
                 bw.write(String.valueOf(values_per_config.no_entities / no_runs));
                 bw.write(" ");
                 bw.write(String.valueOf(values_per_config.no_versions / entries_per_config));
                 bw.write(" ");
                 bw.write(String.valueOf(values_per_config.no_conflicts / no_runs));
                 bw.write(" ");
                 bw.write(String.valueOf(values_per_config.no_aborted / no_runs));
                 bw.write(" ");
                 bw.write(String.valueOf(values_per_config.avg_time_lastCID / no_runs));
                 bw.write(" ");
                 bw.write(String.valueOf(values_per_config.max_time_lastCID / no_runs));
                 bw.newLine();
 
                 // also reset
                 values_per_config = new ResultValuesVersionsStats();
                 line_counter = 0;
             }
         }
         bw.close();
         br.close();
     }
     
 }
