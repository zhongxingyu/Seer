 package ui.swing.jira;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.util.List;
 
 import javax.swing.JTable;
 import javax.swing.event.TableModelListener;
 import javax.swing.table.AbstractTableModel;
 import javax.swing.table.TableCellRenderer;
 
 import jira.JiraIssueStatusManagement;
 import jira.issue.JiraAction;
 import jira.issue.RemoteJiraIssue;
 
 @SuppressWarnings("serial")
 public class JiraIssueTableModel extends AbstractTableModel {
 
 	protected static final int COLUMN_ACTIONS = 3;
 	protected static final int COLUMN_ASSIGN_TO = 4;
 	protected static final int COLUMN_COMMENT = 5;
 	private final List<RemoteJiraIssue> issues;
 	private Object[][] datas;
 	private String[] columns;
 	
 	protected static final int PENDENCY_WORKFLOW_PROCESS = 0;
 	protected static final int PENDENCY_ASSIGNMNET = 1;
 	private boolean[][] pendencies;
 	private final JiraIssueStatusManagement jiraStatusManagement;
 	private boolean[][] failures;
 
 	public JiraIssueTableModel(List<RemoteJiraIssue> issues, JiraIssueStatusManagement jiraStatusManagement) {
 		this.issues = issues;
 		this.jiraStatusManagement = jiraStatusManagement;
 		defineColumns();
 		defineDatas();
 		definePendingActions();
 	}
 
 	private void definePendingActions() {
 		pendencies = new boolean[issues.size()][columns.length];
 		for (int row = 0; row < issues.size(); row++){ 
 			for(int column = 0; column < columns.length; column++)
 				pendencies[row][column] = true;
 			
 			pendencies[row][COLUMN_ACTIONS] = false;
 			pendencies[row][COLUMN_ASSIGN_TO] = false;
 		}
 		
 		failures = new boolean[issues.size()][columns.length];
 	}
 
 	private void defineDatas() {
 		datas = new Object[issues.size()][columns.length];
 		for (int x = 0; x < issues.size(); x++) {
 			RemoteJiraIssue issue = issues.get(x);
 			datas[x][0] = issue.getKey();
 			datas[x][1] = issue.getSummary();
 			datas[x][2] = issue.getStatus();
			datas[x][COLUMN_ACTIONS] = "";
 			datas[x][COLUMN_ASSIGN_TO] = issue.getAssignee();
 			datas[x][COLUMN_COMMENT] = "";
 		}
 	}
 
 	private void defineColumns() {
 		columns = new String[] { "Issue", "Summary", "Status", "Actions", "Assignee", "Comment" };
 	}
 
 	@Override
 	public void addTableModelListener(TableModelListener l) {
 	}
 
 	@Override
 	public int getColumnCount() {
 		return columns.length;
 	}
 
 	@Override
 	public int getRowCount() {
 		return issues.size();
 	}
 
 	@Override
 	public Object getValueAt(int rowIndex, int columnIndex) {
 		return datas[rowIndex][columnIndex];
 	}
 
 	@Override
 	public String getColumnName(int column) {
 		return columns[column];
 	}
 
 	@Override
 	public boolean isCellEditable(int rowIndex, int columnIndex) {
 		return columnIndex == COLUMN_ACTIONS 
 				|| columnIndex == COLUMN_ASSIGN_TO 
 				|| columnIndex == COLUMN_COMMENT;
 	}
 
 	@Override
 	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
 		datas[rowIndex][columnIndex] = aValue;
 	}
 
 	public List<JiraAction> getActionsForRow(int row) {
 		return issues.get(row).getAvaiableActions();
 	}
 
 	public RemoteJiraIssue getIssueAt(int row) {
 		return issues.get(row);
 	}
 
 	public JiraAction getActionAt(int row) {
 		return (JiraAction) getValueAt(row, COLUMN_ACTIONS);
 	}
 
 	public String getCommentAt(int row) {
 		return (String) getValueAt(row, COLUMN_COMMENT);
 	}
 	
 	public String getUserNameAt(int row){
 		return (String) getValueAt(row, COLUMN_ASSIGN_TO);
 	}
 
 	public void setProgressDone(int row) {
 		pendencies[row][COLUMN_ACTIONS] = true;
 	}
 
 	public boolean isProgressDone(int row) {
 		return pendencies[row][COLUMN_ACTIONS];
 	}
 
 	public void setAssignmentDone(int row) {
 		pendencies[row][COLUMN_ASSIGN_TO] = true;
 	}
 	
 	public boolean isAssignmentDone(int row) {
 		return pendencies[row][COLUMN_ASSIGN_TO];
 	}
 
 	public boolean hasPendencies(int row) {
 		for(boolean isDone : pendencies[row])
 			if(!isDone)
 				return true;
 		return false;
 	}
 
 	public void processRow(int row) {
 		
 		// TODO temporary (eu juro!!!)
 		if(!isProgressDone(row)){
 			failures[row][COLUMN_ACTIONS] = true;
 			progressIssueWorkflow(row);
 			failures[row][COLUMN_ACTIONS] = false;
 		}
 		
 		if(!isAssignmentDone(row)){
 			failures[row][COLUMN_ASSIGN_TO] = true;
 			assignIssue(row);
 			failures[row][COLUMN_ASSIGN_TO] = false;
 		}
 	}
 
 	private void progressIssueWorkflow(int row) {
 		RemoteJiraIssue issue = getIssueAt(row);
 		JiraAction action = getActionAt(row);
 		String comment = getCommentAt(row);
 		if (action != null) {
 			jiraStatusManagement.progressIssue(issue, action, comment);
 			setProgressDone(row);
 		}
 		fireTableDataChanged();
 	}
 
 	private void assignIssue(int row) {
 		RemoteJiraIssue issue = getIssueAt(row);
 		String username = getUserNameAt(row);
 		if (username != null && !username.isEmpty()){
 			issue.assignTo(username);
 			setAssignmentDone(row);
 		}
 		fireTableDataChanged();
 	}
 	
 	public TableCellRenderer decorateCellRenderer(TableCellRenderer rederer){
 		return new CellRendererDecorator(rederer);
 	}
 	
 	public class CellRendererDecorator implements TableCellRenderer{
 		
 		private final TableCellRenderer decorated;
 
 		public CellRendererDecorator(TableCellRenderer decorated) {
 			this.decorated = decorated;
 		}
 		
 		@Override
 		public Component getTableCellRendererComponent(JTable table,
 				Object value, boolean isSelected, boolean hasFocus, int row,
 				int column) {
 
 			Component cell = decorated.getTableCellRendererComponent(table,
 					value, isSelected, hasFocus, row, column);
 			
 			cell.setBackground(Color.WHITE);
 			
 			if(failures[row][column])
 				cell.setBackground(new Color(0xFFAAAA));
 			else if(pendencies[row][column])
 				cell.setBackground(new Color(0xAAFFAA));
 			
 			return cell;
 		}
 	}
 	
 }
