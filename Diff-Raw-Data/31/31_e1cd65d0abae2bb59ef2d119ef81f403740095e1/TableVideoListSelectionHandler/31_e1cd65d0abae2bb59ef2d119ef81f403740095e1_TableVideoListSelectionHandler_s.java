 package GUI.SelectionListeners;
 
 import javax.swing.JTable;
 import javax.swing.ListSelectionModel;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 
 import model.Video;
 import model.data.exceptions.RecordNotFoundException;
 import GUI.MainWindow;
 
 /**
  * 
  * @author Waldemar Smirnow
  * @author Volha Baranouskaya
  */
 public class TableVideoListSelectionHandler implements ListSelectionListener {
 
 	private MainWindow mainWindow;
 	private JTable videoTable;
 
 	public TableVideoListSelectionHandler(MainWindow mainWindow) {
 		this.mainWindow = mainWindow;
 		this.videoTable = mainWindow.getTablePanel().getTableVideo();
 	}
 
 	public void valueChanged(ListSelectionEvent e) {
 		ListSelectionModel lsm = (ListSelectionModel) e.getSource();
 		lsm.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 
 		if (lsm.getMinSelectionIndex() >= 0 && lsm.getMinSelectionIndex() < Video.findAll().size()) {
 			int vID = (Integer) videoTable.getValueAt(lsm
 					.getMinSelectionIndex(), 0);
 
 			try {
 				Video selectedVideo = Video.findByID(vID);
 				mainWindow.getDetailPanel().fillPanelDetailVideo(selectedVideo);
 			} catch (RecordNotFoundException e1) {
 //				JOptionPane.showMessageDialog(null, "Fehler: " + e1.getMessage(),
 //						"Fehler", JOptionPane.ERROR_MESSAGE);
 			}
 		}
 	}
 }
