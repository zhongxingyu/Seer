 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package br.gov.saudecaruaru.bpai.util;
 
 import br.gov.saudecaruaru.bpai.business.model.ProcedimentoRealizado;
 import br.gov.saudecaruaru.bpai.business.model.ProcedimentoRealizadoPK;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.table.AbstractTableModel;
 
 /**
  *
  * @author Albuquerque
  */
 public abstract class AbstractProcedimentoRealizadoTableModel extends AbstractTableModel{
 
     
     private String[] columns;
     
     private List<ProcedimentoRealizado> list;
 
     public AbstractProcedimentoRealizadoTableModel(String[] columns, List<ProcedimentoRealizado> list) {
         this.columns = columns;
         this.list = list;
     }
 
     protected AbstractProcedimentoRealizadoTableModel(List<ProcedimentoRealizado> list) {
         this.list = list;
     }
     
     @Override
     public int getRowCount() {
         return this.list.size();
     }
 
     @Override
     public int getColumnCount() {
         return this.columns.length;
     }
 
     @Override
     public abstract Object getValueAt(int rowIndex, int columnIndex);
     
     public ProcedimentoRealizado getCloneElementList(int rowIndex){
           ProcedimentoRealizado p=this.list.get(rowIndex);
         try {
             ProcedimentoRealizado pClone =(ProcedimentoRealizado) p.clone();
             pClone.setProcedimentoRealizadoPK((ProcedimentoRealizadoPK) p.getProcedimentoRealizadoPK().clone());
             return pClone;
         } catch (CloneNotSupportedException ex) {
             Logger.getLogger(ProcedimentoRealizadoTableModel.class.getName()).log(Level.SEVERE, null, ex);
             return null;
         }
         
     }
 
     public List<ProcedimentoRealizado> getList() {
         return list;
     }
     
     public List<ProcedimentoRealizado> getListWithOutEmptyElements(){
         List<ProcedimentoRealizado> listNotEmpty = new ArrayList<ProcedimentoRealizado>();
          for(ProcedimentoRealizado p : this.list){
             if(p.getProcedimentoRealizadoPK().getCnesUnidade()!=null){
                listNotEmpty.add(p);
             }
         }
           return listNotEmpty;
     }
     public ProcedimentoRealizado getCloneElementListEmpty() {
         for(ProcedimentoRealizado p : this.list){
             if(p.getProcedimentoRealizadoPK().getCnesUnidade()==null){
                 try {
                     ProcedimentoRealizado pClone =(ProcedimentoRealizado) p.clone();
                      pClone.setProcedimentoRealizadoPK((ProcedimentoRealizadoPK) p.getProcedimentoRealizadoPK().clone());
                     return pClone;
                 } catch (CloneNotSupportedException ex) {
                     Logger.getLogger(ProcedimentoRealizadoTableModel.class.getName()).log(Level.SEVERE, null, ex);
                     
                 }
             }
         }
         return null;
        
     }
     
     
     @Override
     public String getColumnName(int column) {
         
         return this.columns[column];
     }
  
     @Override
     public Class getColumnClass(int columnIndex) {
         //retorna a classe que representa a coluna
         return String.class;
     }
     
      @Override
     public abstract void setValueAt(Object aValue, int rowIndex, int columnIndex); 
     
     public abstract void setValueAt(ProcedimentoRealizado aValue, int rowIndex);
  
     @Override
     public boolean isCellEditable(int rowIndex, int columnIndex) {
         //no nosso caso todas vão ser editáveis, entao retorna true pra todas
         return false;
     } 
     
     public void removeProcedimentoRealizado(int indexRow){
         this.list.remove(indexRow);
         fireTableRowsDeleted(indexRow, indexRow); 
     }
     
     public void addProcedimentoRealizado(ProcedimentoRealizado model){
         this.list.add(model);
         
         int ultimoIndex=this.getRowCount()-1;
         
         
         this.fireTableRowsInserted(ultimoIndex, ultimoIndex);
         
     }
     
     public void addAllProcedimentoRealizado(List<ProcedimentoRealizado> list){
         int primeiroIndex=list.size()-1;
         primeiroIndex=primeiroIndex<0?0:primeiroIndex;
         this.list.addAll(list);
         int ultimoIndex=this.getRowCount()-1;
         this.fireTableRowsInserted(primeiroIndex, ultimoIndex);
         
     }
     
    public void replaceAllProcedimentoRealizado(List<ProcedimentoRealizado> list){
        this.list.clear();
        int primeiroIndex=list.size()-1;
        //primeiroIndex= primeiroIndex < 0 ? 0 : primeiroIndex;
         
         this.list.addAll(list);
         int ultimoIndex=this.getRowCount()-1;
         this.fireTableRowsDeleted(primeiroIndex, ultimoIndex);
         this.fireTableRowsInserted(primeiroIndex, ultimoIndex);
    }
     
     public ProcedimentoRealizado getProcedimentoRealizado(int index){
         return this.list.get(index);
     }
     
     public void clean(){
         this.list.clear();
         fireTableDataChanged();
     }
     
     public boolean isEmpty(){
         return this.list.isEmpty();
     }
 
     public String[] getColumns() {
         return columns;
     }
 
     public void setColumns(String[] columns) {
         this.columns = columns;
     }
     
 }
