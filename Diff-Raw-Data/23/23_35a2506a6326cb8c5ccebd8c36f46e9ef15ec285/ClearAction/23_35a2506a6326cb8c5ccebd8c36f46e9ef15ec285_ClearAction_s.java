 /**
  * 
  */
 package mesh;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 final class ClearAction implements ActionListener {
 	/**
 	 * 
 	 */
 	private final MainForm mForm;
 
 	/**
 	 * @param mainForm
 	 */
 	ClearAction(MainForm mainForm) {
 		mForm = mainForm;
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		int mod = e.getModifiers() & ActionEvent.CTRL_MASK;
		System.out.println (mod);
 		int size = mForm.getDataModel().getSize();
 
 		for (int i = 0; i < size; i++) {
 			for (int j = 0; j < size; j++) {
 				if (mod > 0) {
 					String str = mForm.getDataModel().matrix[i][j];
					if ((str.indexOf("%") >= 0)) {
 						mForm.getDataModel().matrix[i][j] = "";
					} 
 
 				} else {
 					mForm.getDataModel().matrix[i][j] = "";
 				}
 
 			}
 		}
 		mForm.getDataModel().fireTableDataChanged();
 
 	}
 }
