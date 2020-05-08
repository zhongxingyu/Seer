 package amu.licence.edt.view.main.table;
 
 import java.awt.Component;
 import java.util.Calendar;
 import java.util.List;
 
 import javax.swing.AbstractListModel;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JTable;
 import javax.swing.ListCellRenderer;
 import javax.swing.UIManager;
 import javax.swing.table.AbstractTableModel;
 import javax.swing.table.JTableHeader;
 
 import amu.licence.edt.model.beans.Session;
 import amu.licence.edt.model.beans.TU;
 
 class RowHeaderRenderer extends JLabel implements ListCellRenderer {
     private static final long serialVersionUID = 1L;
 
     RowHeaderRenderer() {
         JTableHeader header = new JTable().getTableHeader();
         setOpaque(true);
         setBorder(UIManager.getBorder("TableHeader.cellBorder"));
         setHorizontalAlignment(CENTER);
         setForeground(header.getForeground());
         setBackground(header.getBackground());
         setFont(header.getFont());
     }
 
     @Override
     public Component getListCellRendererComponent(JList list, Object value,
             int index, boolean isSelected, boolean cellHasFocus) {
         setText((value == null) ? "" : value.toString());
         return this;
     }
 }
 
 public class ScheduleTableModel extends AbstractTableModel implements SpanTableModel {
     private static final long serialVersionUID = 1L;
 
     private SpanModel spanModel = new SpanModel();
     private Session[][] data;
     private final String[] entete = { "8h", "9h", "10h", "11h", "12h", "13h", "14h", "15h", "16h", "17h", "18h", "19h" };
     private JList rowHeader;
 
     public ScheduleTableModel() {
         super();
         this.data = new Session[5][entete.length];
 
         this.rowHeader = new JList(new AbstractListModel() {
             private static final long serialVersionUID = 1L;
 
             String semaine[] = { "Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi" };
 
             @Override
             public int getSize() {
                 return semaine.length;
             }
 
             @Override
             public Object getElementAt(int index) {
                 return semaine[index];
             }
         });
         rowHeader.setFixedCellWidth(100);
//        rowHeader.setFixedCellHeight(table.getRowHeight()/*+ table.getRowMargin()*/);
         rowHeader.setCellRenderer(new RowHeaderRenderer());
     }
 
     public void fillData(List<Session> sessions) {
         if (sessions == null) return;
         Calendar c = Calendar.getInstance();
         for (Session s : sessions) {
             int row, col;
             c.setTime(s.getStartDate());
             row = c.get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY;
             col = c.get(Calendar.HOUR_OF_DAY) - 8;
             if (s.getDuration() > 1) {
                 spanModel.addSpan(new Span(row, col, s.getDuration()));
             }
             data[row][col] = s;
         }
         fireTableDataChanged();
     }
 
     public void clear() {
         for (int i = 0; i < 5; ++i) {
             for (int j = 0; j < entete.length; ++j) {
                 data[i][j] = null;
             }
         }
         spanModel.clear();
     }
 
     @Override
     public Class<TU> getColumnClass(int columnIndex) {
         return TU.class;
     }
 
     @Override
     public int getColumnCount() {
         return entete.length;
     }
 
     @Override
     public int getRowCount() {
         return data.length;
     }
 
     @Override
     public String getColumnName(int columnIndex) {
         return entete[columnIndex];
     }
 
     @Override
     public Object getValueAt(int arg0, int arg1) {
         return data[arg0][arg1];
     }
 
     @Override
     public SpanModel getSpanModel() {
         return spanModel;
     }
 
     public JList getRowHeader() {
         return rowHeader;
     }
 
 }
