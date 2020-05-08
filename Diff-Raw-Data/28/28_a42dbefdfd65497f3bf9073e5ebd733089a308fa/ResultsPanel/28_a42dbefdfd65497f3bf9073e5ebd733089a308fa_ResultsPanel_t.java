 package GUI;
 
 import java.awt.FlowLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.Set;
 
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 import boardnodes.BoardElt;
 import whiteboard.Backend;
 import whiteboard.SearchResult;
 
 public class ResultsPanel extends javax.swing.JPanel {
 	JLabel resNum;
 	JButton nextButton;
 	JButton prevButton;
 	JButton clearButton;
 	Backend backend;
 	public ResultsPanel() {
 		this.setLayout(new FlowLayout());
 		resNum = new JLabel("");
 		nextButton = new JButton("next");
 		prevButton = new JButton("prev");
 		clearButton = new JButton("clear search");
 		this.add(resNum);
 		this.add(prevButton);
 		this.add(nextButton);
 		this.add(clearButton);
 
 
 		prevButton.setEnabled(false);
 		nextButton.setEnabled(false);
 		clearButton.setEnabled(false);
 
 		nextButton.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent e) {
 				nextResult();
 			}
 		});
 		prevButton.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent e) {
 				prevResult();
 			}
 		});
 		clearButton.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent e) {
 				for(SearchResult s: results) {
 					s.elt.clearHighlight();
 				}
 				results.clear();
 				prevButton.setEnabled(false);
 				nextButton.setEnabled(false);
 				clearButton.setEnabled(false);
 				resNum.setText("");
 			}
 		});
 	}
 
 	private ArrayList<SearchResult> results;
 	private int index;
 	private String query;
 
 	private void nextResult() {
 		index++;
 		if(index>results.size()-1) {
 			index = 0;
 		}
 		showResult();
 	}
 
 	private void prevResult() {
 		index--;
 		if(index<0) {
 			index = results.size()-1;
 		}
 		showResult();
 	}
 
 	//highlights all results in a light color
 	private void highlightAll() {
 		for(SearchResult s: results) {
 			s.elt.highlightText(s.index, query.length(), false);
 		}
 	}
 	private void showResult() {
 		highlightAll();
 		if(results.size()>0) {
 			backend.centerNode(results.get(index).elt);
 			results.get(index).elt.highlightText(results.get(index).index, query.length(), true);
 			resNum.setText(((index%results.size())+1)+"/"+(results.size()));
 			prevButton.setEnabled(true);
 			nextButton.setEnabled(true);
 			clearButton.setEnabled(true);
 		} else {
 			resNum.setText("no results!");
 			prevButton.setEnabled(false);
 			nextButton.setEnabled(false);
 			clearButton.setEnabled(false);
 		}
 	}
 
 	public void setResults(ArrayList<SearchResult> set, String forQuery) {
		if(results!=null && !results.isEmpty()) {
			for(SearchResult s: results) {
				s.elt.clearHighlight();
			}
		}
 		query = forQuery;
 		results = set;
 		index = 0;
 		showResult();
 	}
 
 	public void setBackend(Backend b) {
 		backend = b;
 	}
 
 
 }
