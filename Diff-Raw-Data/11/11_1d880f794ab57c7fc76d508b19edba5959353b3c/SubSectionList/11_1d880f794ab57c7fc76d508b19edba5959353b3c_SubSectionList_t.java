 package edu.cs319.client.customcomponents;
 
 import java.util.Collection;
 
 import javax.swing.JList;
 
 import edu.cs319.dataobjects.DocumentSubSection;
 import edu.cs319.dataobjects.impl.DocumentSubSectionImpl;
 
 public class SubSectionList extends JList {
 
 	private JFlexibleListModel<DocumentSubSection> model;
 
 	public SubSectionList() {
 		model = new JFlexibleListModel<DocumentSubSection>();
		this.setModel(model);
 	}
 
 	public void addSubSection(DocumentSubSection sec) {
 		if (model.contains(sec))
 			return;
 		model.add(sec);
 	}
 
 	public void subSectionUpdated(DocumentSubSection sec) {
 		int idx = model.indexOf(sec);
 		if (idx == -1)
 			return;
 		DocumentSubSection oldSec = (DocumentSubSection) model.getElementAt(idx);
 		oldSec.setText(oldSec.lockedByUser(), sec.getText());
 		model.update();
 	}
 
 	public void subSectionRemoved(String secName) {
 		model.remove(new DocumentSubSectionImpl(secName));
 	}
 	
 	public void fullyRefreshList(Collection<DocumentSubSection> newSet){
 		model.clearList();
 		model.addAll(newSet);
 	}
 }
