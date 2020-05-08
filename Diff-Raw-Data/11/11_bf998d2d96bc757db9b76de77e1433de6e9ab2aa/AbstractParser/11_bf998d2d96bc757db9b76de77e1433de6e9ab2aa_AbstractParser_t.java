 package eu.amaxilatis.java.traceparser.parsers;
 
 import eu.amaxilatis.java.traceparser.TraceFile;
 
 import javax.swing.*;
 
 public abstract class AbstractParser extends JPanel {
 
     public static final String REMOVE = "Remove";
     public static final String PLOT = "Plot";
 
     public abstract void setTraceFile(TraceFile mytracefile);
 
 };
 
 
