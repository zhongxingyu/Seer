 /*
  * ProteinTableFormat.java
  *
  * Created on October 10, 2006, 3:57 PM
  *
  * To change this template, choose Tools | Template Manager
  * and open the template in the editor.
  */
 
 package gov.nih.nimh.mass_sieve.gui;
 
 import ca.odell.glazedlists.EventList;
 import ca.odell.glazedlists.GlazedLists;
 import ca.odell.glazedlists.SeparatorList;
 import ca.odell.glazedlists.gui.AdvancedTableFormat;
 import ca.odell.glazedlists.gui.WritableTableFormat;
 import gov.nih.nimh.mass_sieve.*;
 import java.util.ArrayList;
 import java.util.Comparator;
 import java.util.HashSet;
 
 /**
  *
  * @author slotta
  */
 public class ProteinTableFormat implements WritableTableFormat, AdvancedTableFormat {
     private EventList evList;
     private ArrayList<String> experiments;
     private ArrayList<String> columnNames;
     private ArrayList<Class> columnClasses;
     private int upStart, upStop, phStart, phStop, covStart, covStop;
     private boolean parsimonyView;
     
     /** Creates a new instance of ProteinTableFormat */
     public ProteinTableFormat(HashSet<String> exp, EventList list, boolean pView) {
         parsimonyView = pView;
         evList = list;
         experiments = new ArrayList<String>();
         experiments.addAll(exp);
         columnNames = new ArrayList<String>();  columnClasses =  new ArrayList<Class>();
         columnNames.add("Protein Name");        columnClasses.add(String.class);
         if (parsimonyView) {
             columnNames.add("Preferred");         columnClasses.add(Boolean.class);
         }
         upStart = columnNames.size();
         if (experiments.size() > 1) {
             for (String e:experiments) {
                 columnNames.add(e + " Peptides"); columnClasses.add(Integer.class);
             }
             for (String e:experiments) {
                 columnNames.add(e + " PepHits"); columnClasses.add(Integer.class);
             }
         } else {
             columnNames.add("Peptides");    columnClasses.add(Integer.class);
             columnNames.add("PepHits");     columnClasses.add(Integer.class);
         }
         columnNames.add("Parsimony Type");  columnClasses.add(String.class);
         columnNames.add("Cluster");         columnClasses.add(Integer.class);
         columnNames.add("Equiv. Proteins"); columnClasses.add(String.class);
         columnNames.add("Seq Len");         columnClasses.add(Integer.class);
         if (experiments.size() > 1) {
             for (String e:experiments) {
                 columnNames.add(e + " %cover"); columnClasses.add(Double.class);
             }
         } else {
             columnNames.add("%Cover");      columnClasses.add(Double.class);
         }
         columnNames.add(" Mass ");          columnClasses.add(Double.class);
         columnNames.add(" pI ");            columnClasses.add(Double.class);
         columnNames.add("Description");     columnClasses.add(String.class);
         columnNames.add("Files found in");  columnClasses.add(String.class);
         //upStart = 2;
         upStop =  upStart + experiments.size()-1;
         phStart = upStop + 1;
         phStop = phStart + experiments.size()-1;
         covStart = phStop + 5;
         covStop = covStart + experiments.size()-1;
     }
     
     public Class getColumnClass(int column) {
         return columnClasses.get(column);
     }
     
     public int getColumnCount() {
         return columnNames.size();
     }
     
     public String getColumnName(int column) {
         return columnNames.get(column);
         //throw new IllegalStateException();
     }
     public Comparator getColumnComparator(int column) {
         return GlazedLists.comparableComparator();
     }
     
     
     public boolean isEditable(Object baseObject, int column) {
         return true;
         //return baseObject instanceof SeparatorList.Separator;
     }
     
     public Object setColumnValue(Object baseObject, Object editedValue, int column) {
        if (parsimonyView && (column == 1)) {
             Protein pro = (Protein)baseObject;
             Boolean val = (Boolean)editedValue;
             if (val) {
                 pro.setMostEquivalent(val, evList);
             }
         }
         return null;
     }
     
     public Object getColumnValue(Object baseObject, int column) {
         if (baseObject == null) return null;
         if (baseObject instanceof SeparatorList.Separator) {
             SeparatorList.Separator<Protein> separator = (SeparatorList.Separator<Protein>)baseObject;
             return separator.first().getCluster();
         }
         Protein p = (Protein)baseObject;
         if (column == 0) return p.getName();
         if (parsimonyView && (column == 1)) return p.isMostEquivalent();
         else if ((column >= upStart) && (column <= upStop)) {
             if (experiments.size() > 1) return p.getNumUniquePeptides(experiments.get(column-upStart));
             else return p.getNumUniquePeptides();
         } else if ((column >= phStart) && (column <= phStop)) {
             if (experiments.size() > 1) return p.getNumPeptideHits(experiments.get(column-phStart));
             else return p.getNumPeptideHits();
         } else if (column == (phStop+1)) return p.getParsimonyType();
         else if (column == (phStop+2)) return p.getCluster();
         else if (column == (phStop+3)) return p.getEquivalentList();
         else if (column == (phStop+4)) return p.getLength();
         else if ((column >= covStart) && (column <= covStop)) {
             if (experiments.size() > 1) return p.getCoveragePercent(experiments.get(column-covStart));
             else return p.getCoveragePercent();
         } else if (column == (covStop+1)) return p.getMass();
         else if (column == (covStop+2)) return p.getIsoelectricPoint();
         else if (column == (covStop+3)) return p.getDescription();
         else if (column == (covStop+4)) return p.getFileList();
         throw new IllegalStateException();
     }
     
     
 }
