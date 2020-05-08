 package com.imaginea.qctree;
 
 import java.util.Collections;
 import java.util.Set;
import java.util.TreeSet;
 
 public class QCCube {
 
   private Set<Class> classes;
   private int classId;
 
   public QCCube() {
    classes = new TreeSet<Class>();
   }
 
   public Set<Class> getClasses() {
     return Collections.unmodifiableSet(classes);
   }
 
   public static QCCube construct() {
     Table table = Table.getTable();
     String[] rootStr = new String[table.getDimensionHeaders().size()];
     for (int i = 0; i < rootStr.length; ++i) {
       rootStr[i] = Cell.DIMENSION_VALUE_ANY;
     }
     Cell root = new Cell(rootStr);
     Partition base = new Partition(table.getRows(), table.getColumns());
     QCCube cube = new QCCube();
     cube.DFS(root, base, 0, -1);
     return cube;
   }
 
   private void DFS(Cell cell, Partition partition, int k, int chdID) {
     Class clazz = new Class(partition);
     clazz.computeAggregateAndGet();
     Cell ub = clazz.upperBoundOf(cell);
     clazz.setLowerBound(new Cell(cell));
     clazz.setClassID(classId);
     clazz.setChildID(chdID);
     ++classId;
     classes.add(clazz);
 
     for (int j = 0; j < k; ++j) {
       if (cell.getDimensionAt(j) == Cell.DIMENSION_VALUE_ANY
           && ub.getDimensionAt(j) != Cell.DIMENSION_VALUE_ANY) {
         return;
       }
     }
 
     for (int j = k; j < Table.getTable().getDimensionHeaders().size(); ++j) {
       Cell c = new Cell(ub);
       if (c.getDimensionAt(j) != Cell.DIMENSION_VALUE_ANY) {
         continue;
       }
       for (String column : partition.getUniqueColumnValues(j)) {
         c.setDimensionAt(j, column);
         Partition part = Partition.inducedBy(c);
         if (!part.isEmpty()) {
           DFS(c, part, j, clazz.getClassID());
         }
       }
     }
   }
 }
