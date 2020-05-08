 package amu.licence.edt.view.main.table;
 
 public class ScheduleTable extends SpanTable {
     private static final long serialVersionUID = 1L;
 
     public ScheduleTable(ScheduleTableModel model) {
         super(model);
         setRowSelectionAllowed(false);
         setDefaultRenderer(Object.class, new ScheduleTableCellRenderer());
     }
 
 }
