 /*
     TrayRSS - simply notification of feed information
     (c) 2009-2011 TrayRSS Developement Team
     visit the project at http://trayrss.nullpointer.at/
 
     This program is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with this program. If not, see <http://www.gnu.org/licenses/>.
 
  */
 package at.nullpointer.trayrss.gui;
 
 import java.awt.BorderLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 import java.util.Set;
import java.util.TreeSet;
 
 import javax.swing.AbstractAction;
 import javax.swing.Action;
 import javax.swing.BoxLayout;
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JComboBox;
 import javax.swing.JComponent;
 import javax.swing.JDialog;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 import javax.swing.table.TableModel;
 
 import at.nullpointer.trayrss.error.ErrorListener;
 import at.nullpointer.trayrss.error.ErrorType;
 import at.nullpointer.trayrss.gui.tablemodel.FeedTableValidator;
 import at.nullpointer.trayrss.gui.tablemodel.TableColumnUtil;
import at.nullpointer.trayrss.messages.MessageResolverImpl;
 import at.nullpointer.trayrss.messages.Messages;
 
 public class FeedEditorInternalFrame extends JDialog implements WindowListener{
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	private int selectedRow = -1;
 	private Long selectedID = 0L;
 	private JTextField txtFeedName;
 	private JTextField txtFeedUrl;
 	private TrayRssConfigWindow motherFrame;
 	private final Action cancelAction;
 	private final Action saveAction;
 	public JComboBox cbbMonitorIntervall;
 	private JCheckBox chckbxMonitoringEnabled;
 	
	private Set<ErrorListener> errorListener = new TreeSet<ErrorListener>();
 
 	/**
 	 * Create the frame.
 	 * @param window 
 	 * @wbp.parser.constructor
 	 */
 	public FeedEditorInternalFrame(TrayRssConfigWindow window) {
 		this.motherFrame = window;
 		
 		cancelAction = new CancelAction(this);
 		saveAction = new SaveAction(this, this.motherFrame);
 		
 		setModalityType(ModalityType.APPLICATION_MODAL);
 		setTitle(Messages.getMessageResolver(Messages.CONFIG).getString("config.feedinfo.title", "TrayRSS - Feed Info")); //$NON-NLS-1$ //$NON-NLS-2$
 		setBounds(100, 100, 468, 265);
 		
 		JPanel feedInfoPanel = new JPanel();
 		getContentPane().add(feedInfoPanel, BorderLayout.CENTER);
 		feedInfoPanel.setLayout(new BoxLayout(feedInfoPanel, BoxLayout.PAGE_AXIS));
 		
 		JPanel pnlPosOne = new JPanel();
 		feedInfoPanel.add(pnlPosOne);
 		
 		JLabel lblFeedName = new JLabel(Messages.getMessageResolver(Messages.CONFIG).getString("config.feedinfo.feedName.label", "Feed Name")); //$NON-NLS-1$ //$NON-NLS-2$
 		pnlPosOne.add(lblFeedName);
 		
 		JPanel pnlPosTwo = new JPanel();
 		feedInfoPanel.add(pnlPosTwo);
 		
 		txtFeedName = new JTextField();
 		pnlPosTwo.add(txtFeedName);
 		txtFeedName.setColumns(35);
 		
 		JPanel pnlPosThree = new JPanel();
 		feedInfoPanel.add(pnlPosThree);
 		
 		JLabel lblFeedUrl = new JLabel(Messages.getMessageResolver(Messages.CONFIG).getString("config.feedinfo.feedUrl.label", "Feed URL")); //$NON-NLS-1$ //$NON-NLS-2$
 		pnlPosThree.add(lblFeedUrl);
 		
 		JPanel pnlPosFour = new JPanel();
 		feedInfoPanel.add(pnlPosFour);
 		
 		txtFeedUrl = new JTextField();
 		pnlPosFour.add(txtFeedUrl);
 		txtFeedUrl.setColumns(35);
 		
 		JPanel pnlPosFive = new JPanel();
 		feedInfoPanel.add(pnlPosFive);
 		
 		JLabel lblMonitorIntervall = new JLabel(Messages.getMessageResolver(Messages.CONFIG).getString("config.feedinfo.monitorIntervall.label", "Monitor Intervall")); //$NON-NLS-1$ //$NON-NLS-2$
 		pnlPosFive.add(lblMonitorIntervall);
 		
 		cbbMonitorIntervall = new JComboBox();
 		cbbMonitorIntervall.setModel(new DefaultComboBoxModel(new String[] {"1", "2", "3", "5", "10", "15", "30", "60", "120", "240", "300"}));
 		pnlPosFive.add(cbbMonitorIntervall);
 		
 		JPanel pnlPosSix = new JPanel();
 		feedInfoPanel.add(pnlPosSix);
 		
 		chckbxMonitoringEnabled = new JCheckBox(Messages.getMessageResolver(Messages.CONFIG).getString("config.feedinfo.monitoringEnabled.label", (String) null));
 		pnlPosSix.add(chckbxMonitoringEnabled);
 		
 		JPanel pnlPosSeven = new JPanel();
 		feedInfoPanel.add(pnlPosSeven);
 		
 		JButton btnSaveFeedInfo = new JButton(Messages.getMessageResolver(Messages.CONFIG).getString("config.feedinfo.save.button.save", "Save")); //$NON-NLS-1$ //$NON-NLS-2$
 		btnSaveFeedInfo.setAction(saveAction);
 		pnlPosSeven.add(btnSaveFeedInfo);
 		
 		JButton btnCancelFeedInfo = new JButton(Messages.getMessageResolver(Messages.CONFIG).getString("config.feedinfo.cancel.button", "Cancel")); //$NON-NLS-1$ //$NON-NLS-2$
 		btnCancelFeedInfo.setAction(cancelAction);
 		pnlPosSeven.add(btnCancelFeedInfo);
 
 	}
 
 	public FeedEditorInternalFrame(TrayRssConfigWindow frmTrayrss, int selectedRow,
 			TableModel model) {
 		this(frmTrayrss);
 		this.selectedRow = selectedRow;
 		this.selectedID = (Long)model.getValueAt(selectedRow, TableColumnUtil.ID);
 		this.txtFeedName.setText(String.valueOf(model.getValueAt(selectedRow, TableColumnUtil.FEED_NAME)));
 		this.txtFeedUrl.setText(String.valueOf(model.getValueAt(selectedRow, TableColumnUtil.FEED_URL)));
 		this.cbbMonitorIntervall.setSelectedItem(((Long)model.getValueAt(selectedRow, TableColumnUtil.INTERVALL)).toString());
 		this.chckbxMonitoringEnabled.setSelected((Boolean) model.getValueAt(selectedRow, TableColumnUtil.MONITORED));
 	}
 
 	public void windowOpened(WindowEvent e) {
 		// not used
 		
 	}
 
 	public void windowClosing(WindowEvent e) {
 		//this.motherFrame.setFocusable(true);
 		this.dispose();
 		
 	}
 
 	public void windowClosed(WindowEvent e) {
 		// not used
 		
 	}
 
 	public void windowIconified(WindowEvent e) {
 		// not used
 		
 	}
 
 	public void windowDeiconified(WindowEvent e) {
 		// not used
 		
 	}
 
 	public void windowActivated(WindowEvent e) {
 		// not used
 		
 	}
 
 	public void windowDeactivated(WindowEvent e) {
 		// not used
 		
 	}
 	
 	private class SaveAction extends AbstractAction {
 		/**
 		 * 
 		 */
 		private static final long serialVersionUID = 1L;
 		private FeedEditorInternalFrame window;
 		private TrayRssConfigWindow motherFrame;
 		
 		public SaveAction(FeedEditorInternalFrame frame, TrayRssConfigWindow motherFrame) {
 			super(Messages.getMessageResolver(Messages.CONFIG).getString("config.feedinfo.save.button.save", "Save"));
 			this.window = frame;
 			this.motherFrame = motherFrame;
 		}
 		
 		public void actionPerformed(ActionEvent e) {
 			
 			//Check URL
 			if(FeedTableValidator.checkURL(this.window.txtFeedUrl.getText())){
 				if(FeedTableValidator.checkName(this.window.txtFeedName.getText())){
 				this.motherFrame.addFeedRow(this.window.selectedRow,
 						this.window.selectedID, 
 		                this.window.txtFeedName.getText(),
 		                this.window.txtFeedUrl.getText(),
 		                Long.valueOf((String)this.window.cbbMonitorIntervall.getSelectedItem()),
 		                this.window.chckbxMonitoringEnabled.isSelected());
 
 				window.dispose();
 				} else {
 					notifyAllErrorListener(this.window.rootPane, Messages.getMessageResolver(Messages.CONFIG).getString("error.feedinfo.name", "Feed Name Error"), 
 							Messages.getMessageResolver(Messages.CONFIG).getString("error.feedinfo.name.text", "No empty feed name allowed"), ErrorType.ERROR_MESSAGE);
 				}
 			} else {
 				notifyAllErrorListener(this.window.rootPane, Messages.getMessageResolver(Messages.CONFIG).getString("error.feedinfo.url", "URL Error"), 
 						Messages.getMessageResolver(Messages.CONFIG).getString("error.feedinfo.url.text", "Not a valid feed url"), ErrorType.ERROR_MESSAGE);
 			}
 		}
 
 		private void notifyAllErrorListener(JComponent where,
 				String title, String text, int errorType) {
 			for(ErrorListener e : errorListener){
 				e.addError(where, title, text, errorType);
 			}
 			
 		}
 	}
 
 	private class CancelAction extends AbstractAction {
 		/**
 		 * 
 		 */
 		private static final long serialVersionUID = 1L;
 		private JDialog window;
 		
 		public CancelAction(JDialog frame) {
 			super(Messages.getMessageResolver(Messages.CONFIG).getString("config.feedinfo.cancel.button", "Cancel"));
 			this.window = frame;
 		}
 		
 		public void actionPerformed(ActionEvent e) {
 			window.dispose();
 		}
 	}
 	
 	/*
 	 * getter / setter
 	 */
 	
 	public void registerErrorListener(ErrorListener e){
 		this.errorListener.add(e);
 	}
 }
