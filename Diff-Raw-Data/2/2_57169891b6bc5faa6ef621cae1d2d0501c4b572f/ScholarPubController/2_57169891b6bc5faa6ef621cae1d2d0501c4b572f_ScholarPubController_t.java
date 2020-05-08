 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.IOException;
 import java.util.ArrayList;
 import javax.swing.JOptionPane;
 
 
 
 public class ScholarPubController {
 	
 	private ScholarshipModel model;
 	private SelectionView inputView;
 
 	/**
 	 * Creates new controller
 	 */
 	public ScholarPubController(){
 	}
 
 	/**
 	 * @param model to set as the model
 	 */
 	public void setModel(ScholarshipModel model){
 		this.model = model;
 	}
 
 	/**
 	 * @return model
 	 */
 	public ScholarshipModel getModel(){ 
 		return model;
 	}
 	
 	/**
 	 * AddScholarListener provides a method to create a new scholar.
 	 */
 	private class AddScholarListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 		}
 	}
 	
 	/**
 	 * DeleteScholarListener provides a method to delete a scholar.
 	 */
 	private class DeleteScholarListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 		}
 	}
 	
 	/**
 	 * DeleteAllScholarsListener provides a method to delete all scholars.
 	 */
 	private class DeleteAllScholarsListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 		}
 	}
 	
 	/**
 	 * AddSerialListener provides a method to create a new serial.
 	 */
 	private class AddSerialListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 		}
 	}
 	
 	/**
 	 * DeleteSerialListener provides a method to delete a serial.
 	 */
 	private class DeleteSerialListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 		}
 	}
 	
 	/**
 	 * DeleteAllSerialsListener provides a method to delete all serials.
 	 */
 	private class DeleteAllSerialsListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 		}
 	}
 	
 	/**
 	 * AddPaperListener provides a method to create a new paper.
 	 */
 	private class AddPaperListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 		}
 	}
 	
 	/**
 	 * DeletePaperListener provides a method to delete a paper.
 	 */
 	private class DeletePaperListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 		}
 	}
 	
 	/**
 	 * DeleteAllPapersListener provides a method to delete all papers.
 	 */
 	private class DeleteAllPapersListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 		}
 	}
 	
 
 
 
 	/**
	 * @param view on which the listeners should be set
 	 */
 	public void setInputWindow(SelectionView view) {
 		inputView = view;
 		//listeners here
 	}
 
 	/**
 	 * @return the window (in case it needs to be sent messages from elsewhere)
 	 */
 	public SelectionView getInputWindow() {
 		return inputView;
 	}
 	
 	
 	
 }
