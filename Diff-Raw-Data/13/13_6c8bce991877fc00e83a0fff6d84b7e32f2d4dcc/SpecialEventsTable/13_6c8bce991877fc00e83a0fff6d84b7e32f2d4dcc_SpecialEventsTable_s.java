 package ho.module.specialEvents;
 
 import static ho.module.specialEvents.SpecialEventsTableModel.*;
 import ho.core.model.HOVerwaltung;
 import ho.core.util.HOLogger;
 
 import java.awt.Point;
 import java.awt.event.MouseEvent;
 import java.util.List;
 
 import javax.swing.JTable;
 import javax.swing.table.JTableHeader;
 
 public class SpecialEventsTable extends JTable {
 
 	private static final long serialVersionUID = 8656004206333977669L;
 	private List<String> highlightTexte;
 
 	@Override
 	protected JTableHeader createDefaultTableHeader() {
 		return new JTableHeader(columnModel) {
 
 			private static final long serialVersionUID = 203261496086729638L;
 
 			@Override
 			public String getToolTipText(MouseEvent e) {
 				String tip = null;
 				Point p = e.getPoint();
 				int index = columnModel.getColumnIndexAtX(p.x);
 				int modelIndex = convertColumnIndexToModel(columnModel.getColumnIndexAtX(p.x));
 				if (modelIndex == HOMEEVENTCOLUMN || modelIndex == AWAYEVENTCOLUMN) {
 					tip = HOVerwaltung.instance().getLanguageString("Tip4");
 				} else {
 					tip = getModel().getColumnName(modelIndex);
 
 				}
 				return tip;
 			}
 
 		};
 	}
 
 	public void setHighlightTexte(List<String> highlightTexte) {
 		this.highlightTexte = highlightTexte;
 	}
 
 	@Override
 	public String getToolTipText(MouseEvent e) {
 		String tip = null;
 		Point p = e.getPoint();
 		int rowIndex = rowAtPoint(p);
 		int colIndex = columnAtPoint(p);
 		int modelColumnIndex = convertColumnIndexToModel(colIndex);
 		int modelRowIndex = convertRowIndexToModel(rowIndex);
 		if (modelColumnIndex == HOMEEVENTCOLUMN || modelColumnIndex == AWAYEVENTCOLUMN) {
 			tip = HOVerwaltung.instance().getLanguageString("Tip4");
 		}
 		if (modelColumnIndex == NAMECOLUMN) {
 			tip = HOVerwaltung.instance().getLanguageString("TipName");
 		}
 		if (modelColumnIndex == EVENTTYPCOLUMN) {
			MatchRow row = ((SpecialEventsTableModel)getModel()).getMatchRow(modelRowIndex);
			String highlightText = "<table width='300'><tr><td>" + row.getMatchHighlight().getEventText()
					+ "</td></tr></table>";
			String text = "<html>" + highlightText + "</html>";
			tip = text;
 		}
 		return tip;
 	}
 
 	@Override
 	public boolean isCellEditable(int rowIndex, int mColIndex) {
 		return false;
 	}
 
 	private void showDebug(Exception exr) {
 		HOLogger.instance().error(this.getClass(), exr);
 	}
 }
