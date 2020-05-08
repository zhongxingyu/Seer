 package org.computer.knauss.reqtDiscussion.ui.ctrl;
 
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.List;
 import java.util.Vector;
 
 import org.computer.knauss.reqtDiscussion.io.Util;
 import org.computer.knauss.reqtDiscussion.model.Discussion;
 import org.computer.knauss.reqtDiscussion.ui.uiModel.DiscussionTableModel;
 
 public class HighlightRelatedDiscussions {
 
 	private List<int[]> ids = new Vector<int[]>();
 
 	public HighlightRelatedDiscussions() throws IOException {
 		BufferedReader r = new BufferedReader(new FileReader(
 				"related-work-items.txt"));
 
 		String line = r.readLine();
 		while (line != null) {
 			if (line.startsWith("=")) {
 				int[] tmp = Util.stringToID(line);
 				this.ids.add(tmp);
 			}
 			line = r.readLine();
 		}
 		r.close();
 	}
 
	public int[] getRelatedDiscussionIDs(int id) {
 		for (int[] a : this.ids) {
 			for (int i : a) {
 				if (i == id) {
 					return a;
 				}
 			}
 		}
 		return new int[] { id };
 	}
 
 	public void highlightRelatedWorkitems(DiscussionTableModel tm) {
 		Discussion[] selectedItems = tm.getSelectedWorkitems();
 
 		// Do not interfere when user does multiple selections...
 		if (selectedItems.length == 1) {
			int[] ids = getRelatedDiscussionIDs(selectedItems[0].getID());
 
 			if (ids.length > 1) {
 				Discussion[] wis = tm.getDiscussions();
 				for (int j = 0; j < wis.length; j++) {
 					Discussion wi = wis[j];
 					for (int i : ids) {
 						if (wi.getID() == i) {
 							int convertedRow = tm.getTable()
 									.convertRowIndexToView(j);
 							tm.getTable()
 									.getSelectionModel()
 									.addSelectionInterval(convertedRow,
 											convertedRow);
 						}
 					}
 				}
 
 			}
 		}
 	}
 
 }
