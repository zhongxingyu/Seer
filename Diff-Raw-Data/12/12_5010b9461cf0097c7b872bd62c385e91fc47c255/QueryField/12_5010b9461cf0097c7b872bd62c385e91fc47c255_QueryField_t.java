 package org.vamdc.validator.gui.mainframe;
 
 
 import javax.swing.JComboBox;
 import javax.swing.text.JTextComponent;
 
 import org.vamdc.validator.gui.GuiSettings;
 import org.vamdc.validator.interfaces.XSAMSIOModel;
 
 public class QueryField extends JComboBox implements ComponentUpdateInterface{
 	
 	private static final long serialVersionUID = -9123198309241131479L;
 	private String[] queries;
 	
 	private XSAMSIOModel data;
 	
 	public QueryField(){
 		super();
 		this.setEditable(true);
 		loadQueries();
 	}
 
 	private void loadQueries(){
 		String allqueries = GuiSettings.get(GuiSettings.QUERY_HISTORY, "Select * where AtomSymbol='Fe';");
 		queries = allqueries.split(";");
 		for (String query:queries){
 			if (query.length()>1)
 				this.addItem(query+";");
 		}
 		if (data!=null && data.getSampleQueries()!=null)
 			for (String query:data.getSampleQueries()){
 				if (query.length()>1)
 					this.addItem(query+";");
 			}
 	}
 
 	/**
 	 * Add query to history. If query is already present, move it up the list.
 	 * @param query query string
 	 */
 	private void saveQuery(String query) {
 		if (query == null || query.length() == 0)
 			return;
 		query=query.trim();
 		
 		if (query.endsWith(";"))
 			query = query.substring(0, query.length()-1).trim();
 		String newQuery = query;
 			newQuery+=";";
 		int i=0;
 		for (String oldquery:queries){
 			if (oldquery.length()>0 && !oldquery.equalsIgnoreCase(query))
 				newQuery+=oldquery+";";
 			if (i++>10)
 				break;
 		}
 		GuiSettings.put(GuiSettings.QUERY_HISTORY, newQuery);
 		this.removeAllItems();
 		
 		this.loadQueries();
 	}
 
 	public String getText() {
 		return this.getEditor().getItem().toString();
 	}
 	
 	public JTextComponent getTextComponent() {
 		return (JTextComponent)this.getEditor().getEditorComponent();
 	}
 
 	@Override
 	public void resetComponent() {
		loadQueries();
 	}
 
 	@Override
 	public void setModel(XSAMSIOModel data) {
 		this.data = data;
 		loadQueries();
 	}
 
 	@Override
 	public void updateFromModel(boolean isFinal) {
		if (isFinal && data!=null){
			if (data.getLineCount() > 10)
				saveQuery(data.getQuery());
			else 
				loadQueries(); 
			
 		}
 	}
 
 }
