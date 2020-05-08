 /**
 *   CellularAutomaton.java
 *
 *   Copyright (c) 2013 Vladimir Brigant
 *   This software is distributed under the terms of the GNU General Public License.
 */
 
 package cassp.ca;
 
 
 import java.util.*;
 
 import cassp.*;
 import cassp.ca.*;
 import cassp.data.*;
 import cassp.config.*;
 import cassp.ca.rules.*;
 
 
 
 public class CellularAutomaton {
 
     private CACell[] cells;
     private int cellsLength;
     private DataItem dataItem;
     private CARule rule;
     private SimConfig config;
 
     private String predSeq;
     private ArrayList<Integer> reliabIndexes;
     private int meanReliabIndex;
     private double propsMeanDiff;
 
 
     public CellularAutomaton(DataItem dataItem, SimConfig config) {
         this.dataItem = dataItem;
         this.cells = new CACell[this.dataItem.length()];
         this.cellsLength = this.cells.length;
         this.config = config;
         this.predSeq = "";
         this.reliabIndexes = new ArrayList<Integer>();
         this.meanReliabIndex = -1;
         this.propsMeanDiff = -1;
     }
 
     public CellularAutomaton(DataItem dataItem){
         this.dataItem = dataItem;
         this.cells = new CACell[this.dataItem.length()];
         this.cellsLength = this.cells.length;
         this.config = new SimConfig();
         this.predSeq = "";
         this.reliabIndexes = new ArrayList<Integer>();
         this.meanReliabIndex = -1;
         this.propsMeanDiff = -1;
     }
 
     /**
     * Run one step of cellular automaton (next configuration of all cells).
     *
     * @param rule Transition rule of cellular automaton.
     * @param data Data object needed for amino acids properties.
     */
     public String run(CARule rule){
         this.rule = rule;
         int diLength = this.dataItem.length();
 
         // cells initialization
         for (int i = 0; i < diLength; i++){
             this.cells[i] = new CACell(rule.getAminoAcid(this.dataItem.getAaAt(i)));
         }
 
         int ruleSteps = this.rule.getSteps();
 
         for (int s = 0; s < ruleSteps; s++) {
             // cells array copy
             CACell[] tmpCells = new CACell[this.cellsLength];
             for (int u = 0; u < this.cellsLength; u++) {
                 tmpCells[u] = new CACell(this.cells[u]);
             }
             // next states computation
             for (int c = 0; c < this.cellsLength; c++ ) {
                 rule.nextState(tmpCells, this.cells[c], c);
             }
         }
 
         for (int i = 0; i < this.cellsLength; i++ ) {
             this.predSeq += this.cells[i].getMotif();
         }
         return this.predSeq;
     }
 
     /**
     * Returns predicted sequence taken from cells in final CA configuration.
     */
     public String getPredictedSeq(){
         return this.predSeq;
     }
 
     public CACell getCell(int index){
         return this.cells[index];
     }
 
 
     /**
     * Compute mean diffs between max and second highest props.
     */
     public void computePropsMeanDiff(){
 
         double diffSum = 0.0;
 
         for (int i=0; i < this.cells.length; i++) {
             double[] sortedProps = this.sortProps(this.cells[i]);
             diffSum += sortedProps[2] - sortedProps[1];
         }
         this.propsMeanDiff = diffSum/this.cells.length;
         this.dataItem.setPropsMeanDiff(diffSum/this.cells.length);
     }
 
     public double getPropsMeanDiff(){
         return this.propsMeanDiff;
     }
 
     private double[] sortProps(CACell cell){
         double[] props = {cell.getHelixProps(), cell.getSheetProps(), cell.getCoilProps()};
         Arrays.sort(props);
         return props;
     }
 
 
     public void computeReliabIndexes(double maxProps){
         double diff = 0.0;
         int index = 0;
 
         for (int i = 0; i < this.cells.length; i++) {
             double[] sortedProps = this.sortProps(this.cells[i]);
             diff = sortedProps[2] - sortedProps[1];
             index = (int) Math.round(diff/maxProps * this.config.getReliabClasses());
             this.reliabIndexes.add(index);
         }
 
         this.meanReliabIndex = (int) Math.round(this.dataItem.getPropsMeanDiff()/
             maxProps*this.config.getReliabClasses());
         this.dataItem.setMeanReliabIndex(this.meanReliabIndex);
     }
 
    public void computePsipredMeanReliabIndex(){
        System.out.println(this.reliabIndexes);
    }

 
     public String getPredSeq(){
         return this.predSeq;
     }
 
     public ArrayList<Integer> getReliabIndexes(){
         return this.reliabIndexes;
     }
 }
