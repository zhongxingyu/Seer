 package com.imaginea.qctree;
 
 import static com.imaginea.qctree.Cell.DIMENSION_VALUE_ANY;
 
 import java.util.Set;
 
 import com.imaginea.qctree.measures.Aggregable;
 import com.imaginea.qctree.measures.Average;
 
 public class Class implements Comparable<Class> {
   private int clsID;
   private int chdID;
   private Cell ub;
   private Cell lb;
   private Double aggregateVal;
 
   private Partition partition;
   private Aggregable measure = new Average();
 
   public Class(Partition partition) {
     this.partition = partition;
   }
 
   public Class() {
   }
 
   public void setClassID(int clsID) {
     this.clsID = clsID;
   }
 
   public int getClassID() {
     return this.clsID;
   }
 
   public void setChildID(int chdID) {
     this.chdID = chdID;
   }
 
   public void setLowerBound(Cell lb) {
     this.lb = lb;
   }
 
   public void setUpperBound(Cell ub) {
     this.ub = ub;
   }
  
   public Cell getUpperBound() {
     return this.ub;
   }
 
   public void setAggregate(double aggr) {
     this.aggregateVal = aggr;
   }
  
   public double getAggregate() {
     return this.aggregateVal;
   }
 
   public double computeAggregateAndGet() {
     if (aggregateVal != null) {
       return aggregateVal;
     }
     aggregateVal = measure.aggregate(partition.getBaseCells());
     return aggregateVal;
   }
 
   private String getDimensionValueAt(int colIndex) {
     Set<String> values = partition.getUniqueColumnValues(colIndex);
     return values.size() == 1 ? values.iterator().next() : DIMENSION_VALUE_ANY;
   }
 
   /**
    * Upper bound of a partition will be computed as follows.
    * 
    * 1. For any dimension, if the value of that dimension is non-*, upper bound
    * also will have the same dimension value as that of the input cell.
    * 2.Otherwise, for all the dimensions, for which input cell has value *, we
    * scan all the base cells in the partition, and if there is any value (x)
    * repeated in the partition, we replace the value of the ith dimension of
    * upper bound by repeated value (x).
    * 
    * Ex: For the partition {(a2,b1,c1),(a2,b2,c1),(a1,b1,c2)}, upper bound of
    * the cell (a2, *, *) is (a2, *, c1). Here c1 has appeared in all the tuples
    * but not b1.
    * 
    * @param cell
    *          input cell
    * @return Upper bound of the partition.
    * @throws CloneNotSupportedException
    */
   public Cell upperBoundOf(Cell cell) {
     if (ub != null) {
       return ub;
     }
     ub = new Cell(cell);
     for (int colIndex = 0; colIndex < ub.getDimensions().length; ++colIndex) {
       if (ub.getDimensions()[colIndex] != DIMENSION_VALUE_ANY) {
         continue;
       }
       ub.setDimensionAt(colIndex, getDimensionValueAt(colIndex));
     }
     return ub;
   }
 
   @Override
   public int hashCode() {
     final int prime = 31;
     int hashcode = 1;
     hashcode = hashcode * prime + clsID;
     hashcode = hashcode * prime + chdID;
     hashcode = hashcode * prime + aggregateVal.hashCode();
     hashcode = hashcode * prime + ub.hashCode();
     hashcode = hashcode * prime + lb.hashCode();
     return hashcode;
   }
 
   @Override
   public boolean equals(Object obj) {
     if (this == obj) {
       return true;
     }
     if (obj == null) {
       return false;
     }
     if (obj.getClass() != this.getClass()) {
       return false;
     }
     Class that = (Class) obj;
 
    if (that.clsID != this.clsID || that.chdID != this.chdID) {
       return false;
     }
    if (Double.compare(this.aggregateVal, that.aggregateVal) != 0) {
       return false;
     }
    if (!that.ub.equals(this.ub) || !that.lb.equals(this.lb)) {
       return false;
     }
     return true;
   }
 
   @Override
   public String toString() {
     StringBuilder sb = new StringBuilder();
     sb.append("class ID : ").append(clsID).append(' ');
     sb.append("Upper Bound : ").append(ub).append(' ');
     sb.append("Lower Bound : ").append(lb).append(' ');
     sb.append("Lattice Child : ").append(chdID).append(' ');
     sb.append("Agg : ").append(aggregateVal);
     return sb.toString();
   }
 
   @Override
   public int compareTo(Class that) {
    return this.ub.compareTo(that.ub);
   }
 
 }
