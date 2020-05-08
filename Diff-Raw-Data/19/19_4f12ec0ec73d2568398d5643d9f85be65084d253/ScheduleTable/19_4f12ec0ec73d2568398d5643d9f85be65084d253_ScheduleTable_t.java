 package amu.licence.edt.view.main.table;
 
 public class ScheduleTable extends SpanTable {
     private static final long serialVersionUID = 1L;
 
    private final int ROW_HEIGHT = 100;

     public ScheduleTable(ScheduleTableModel model) {
         super(model);
         setRowSelectionAllowed(false);
         setDefaultRenderer(Object.class, new ScheduleTableCellRenderer());
        model.getRowHeader().setFixedCellHeight(ROW_HEIGHT);
        setRowHeight(ROW_HEIGHT);
     }
 
 }
