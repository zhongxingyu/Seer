 package eu.amaxilatis.java.traceparser.parsers;
 
 import eu.amaxilatis.java.traceparser.TraceFile;
 
 import javax.swing.*;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;
import java.awt.event.*;


/**
 * Created by IntelliJ IDEA.
 * User: amaxilatis
 * Date: 7/2/11
 * Time: 12:33 PM
 */
 
 public abstract class AbstractParser extends JPanel {
 
     public static final String REMOVE = "Remove";
     public static final String PLOT = "Plot";
 
     public abstract void setTraceFile(TraceFile mytracefile);
 
 };
 
 
