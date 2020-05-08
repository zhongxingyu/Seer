 package org.pentaho.pac.client.datasources;
 
 import org.pentaho.pac.client.PentahoAdminConsole;
 import org.pentaho.pac.client.i18n.PacLocalizedMessages;
 import org.pentaho.pac.common.datasources.PentahoDataSource;
 
 import com.google.gwt.user.client.ui.KeyboardListener;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.user.client.ui.Widget;
 
 public class DataSourceAdvancePanel extends VerticalPanel implements KeyboardListener{
   private static final PacLocalizedMessages MSGS = PentahoAdminConsole.getLocalizedMessages();
   TextBox maxActiveConnTextBox = new TextBox();
   TextBox idleConnTextBox = new TextBox();
   TextBox validationQueryTextBox = new TextBox();
   TextBox waitTextBox = new TextBox();
   Label label = new Label(MSGS.datasourceAdvanceInfo());
   
   public DataSourceAdvancePanel() {
     add(new Label(MSGS.maxActiveDbConnections()));
     add(maxActiveConnTextBox);
     add(new Label(MSGS.numIdleDbConnnections()));
     add(idleConnTextBox);
     add(new Label(MSGS.dbValidationQuery()));
     add(validationQueryTextBox);
     add(new Label(MSGS.dbWaitTime()));
     add(waitTextBox);
     add(label);
     maxActiveConnTextBox.addKeyboardListener(this);
     idleConnTextBox.addKeyboardListener(this);
     waitTextBox.addKeyboardListener(this);
     
     maxActiveConnTextBox.setWidth("100%"); //$NON-NLS-1$
     idleConnTextBox.setWidth("100%"); //$NON-NLS-1$
     validationQueryTextBox.setWidth("100%"); //$NON-NLS-1$
     waitTextBox.setWidth("100%"); //$NON-NLS-1$
     label.setWidth("100%"); //$NON-NLS-1$
   }
 
   public int getMaxActiveConnections() {
     int count = -1;
     try {
       count = Integer.parseInt(maxActiveConnTextBox.getText());
     } catch (Exception ex) {
       // Do nothing.
     }
     return count;
   }
 
   public void setMaxActiveConnections(int count) {
     maxActiveConnTextBox.setText(count > 0 ? Integer.toString(count) : ""); //$NON-NLS-1$
   }
   
   public int getIdleConnections() {
     int count = -1;
     try {
       count = Integer.parseInt(idleConnTextBox.getText());
     } catch (Exception ex) {
       // Do nothing.
     }
     return count;
   }
   
   public void setIdleConnections(int count) {
     idleConnTextBox.setText(count > 0 ? Integer.toString(count) : ""); //$NON-NLS-1$
   }
   
   public String getValidationQuery() {
     return validationQueryTextBox.getText();
   }
 
   public void setValidationQuery(String query) {
     validationQueryTextBox.setText(query);
   }
 
   public long getWait() {
     int count = -1;
     try {
       count = Integer.parseInt(waitTextBox.getText());
     } catch (Exception ex) {
       // Do nothing.
     }
     return count;
   }
   
   public void setWait(long count) {
     waitTextBox.setText(count > 0 ? Long.toString(count) : ""); //$NON-NLS-1$
  }
   
   public TextBox getMaxActiveConnectionsTextBox() {
     return maxActiveConnTextBox;
   }
 
   public TextBox getIdleConnectionsTextBox() {
     return maxActiveConnTextBox;
   }
   
   public TextBox getValidationQueryTextBox() {
     return validationQueryTextBox;
   }
 
   public TextBox getWaitTextBox() {
     return waitTextBox;
   }
   
   public void setDataSource(PentahoDataSource dataSource) {
     if (dataSource == null) {
       setMaxActiveConnections(-1);
       setIdleConnections(-1);
       setValidationQuery(""); //$NON-NLS-1$
       setWait(-1);
     } else {
       setMaxActiveConnections(dataSource.getMaxActConn());
       setIdleConnections(dataSource.getIdleConn());
       setValidationQuery(dataSource.getQuery());
       setWait(dataSource.getWait());
     }
   }
   
   public PentahoDataSource getDataSource() {
     PentahoDataSource dataSource = new PentahoDataSource();
     dataSource.setMaxActConn(getMaxActiveConnections());
     dataSource.setIdleConn(getIdleConnections());
     dataSource.setQuery(getValidationQuery());
     dataSource.setWait(getWait());
     return dataSource;
   }
   
   public void setEnabled(boolean enabled) {
     maxActiveConnTextBox.setEnabled(enabled);
     idleConnTextBox.setEnabled(enabled);
     validationQueryTextBox.setEnabled(enabled);
     waitTextBox.setEnabled(enabled);
   }
 
   public void onKeyDown(Widget sender, char keyCode, int modifiers) {
   }
 
   public void onKeyPress(Widget sender, char keyCode, int modifiers) {
    if (!Character.isDigit(keyCode)) {
       TextBox textBox = (TextBox)sender;
       textBox.cancelKey();
    }    
   }
 
   public void onKeyUp(Widget sender, char keyCode, int modifiers) {
    }
   
    public void refresh() {
      
    }
  
 }
