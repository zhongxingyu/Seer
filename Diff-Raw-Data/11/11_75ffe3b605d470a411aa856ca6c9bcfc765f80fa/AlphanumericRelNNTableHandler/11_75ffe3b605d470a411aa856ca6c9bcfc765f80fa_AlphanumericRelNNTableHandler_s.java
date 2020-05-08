 package es.icarto.gvsig.navtableforms.gui.tables;
 
 import java.awt.event.MouseListener;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import javax.swing.JComponent;
 import javax.swing.JTable;
 
 import com.iver.cit.gvsig.fmap.edition.IEditableSource;
 import com.iver.cit.gvsig.project.documents.table.ProjectTable;
 
 import es.udc.cartolab.gvsig.users.utils.DBSession;
 
 public class AlphanumericRelNNTableHandler {
 
     private IEditableSource source;
     private AbstractSubForm form;
     private JTable jtable;
     private String originKey;
     private String relTable;
     private String dbSchema;
     private String destinyKey;
     private String[] colNames;
     private String[] colAliases;
     private MouseListener listener;
 
     public AlphanumericRelNNTableHandler(ProjectTable sourceTable,
 	    HashMap<String, JComponent> widgets, String dbSchema,
 	    String originKey, String relTable, String destinyKey,
 	    String[] colNames, String[] colAliases) {
 	this.source = sourceTable.getModelo();
 	jtable = (JTable) widgets.get(sourceTable.getName());
 	jtable.setAutoCreateRowSorter(true);
 	this.originKey = originKey;
 	this.dbSchema = dbSchema;
 	this.relTable = relTable;
 	this.destinyKey = destinyKey;
 	this.colNames = colNames;
 	this.colAliases = colAliases;
     }
 
     public void reload(AbstractSubForm form) {
 	this.form = form;
 	listener = new JTableOnlyUpdateContextualMenu(form);
 	jtable.addMouseListener(listener);
 	// for the popUp to work on empty tables
 	jtable.setFillsViewportHeight(true);
     }
 
     public void removeListeners() {
 	jtable.removeMouseListener(listener);
     }
 
     public void fillValues(String value) {
 	try {
 	    DBSession session = DBSession.getCurrentSession();
 	    if (session != null) {
 		String[] secPkValues = session.getDistinctValues(relTable,
 			dbSchema, destinyKey, false, false, "WHERE "
 				+ originKey + "='" + value + "'");
 		int foreignKeyColumn = source.getRecordset()
 			.getFieldIndexByName(destinyKey);
 		List<IRowFilter> filters = new ArrayList<IRowFilter>();
 		for (String val : secPkValues) {
 		    filters.add(new IRowFilterImplementer(foreignKeyColumn, val));
 		}
 		TableModelAlphanumeric model = new TableModelAlphanumeric(
 			source, new IRowMultipleOrFilterImplementer(filters),
 			colNames, colAliases);
 		jtable.setModel(model);
		form.setModel(model);
 	    }
 	} catch (Exception e) {
 	    e.printStackTrace();
 	}
 
     }
 
 }
