 package com.github.pageallocation.simulation;
 
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.JTable;
 import javax.swing.JViewport;
 import javax.swing.table.DefaultTableModel;
 
 import com.github.pageallocation.algorithms.model.Reference;
 
 public class PageAllocationSimulation implements Simulation {
 
 	private JTable table;
 	private DefaultTableModel model;
 	private static Reference EMPTY = new Reference(-1,0);
 	private int i, k;
 	private int frames;
 	private int numberOfSteps;
 	private List<Reference> references;
 	
 	public PageAllocationSimulation(JTable table, DefaultTableModel model){
 		this.table = table;
 		this.model = model;
 	}
 	@Override
 	public boolean hasMoreSteps() {
 		return numberOfSteps > 0;
 	}
 
 	@Override
 	public void step() {
 		if (hasMoreSteps()) {
 
 			scrollToVisible(table, k, i);
 			int val = getValue(i,k);
 			if ( val == -1)
 				table.getModel().setValueAt("X", k, i);
 			else
 				table.getModel().setValueAt("" + val, k, i);
 
 			if (k == frames - 1) {
 				k = 0;
 				i++;
 			} else {
 				k++;
 			}
 
 			numberOfSteps--;
 			model.fireTableDataChanged();
 		}
 		
 	}
 	
 	
 	public void setParams(List<Reference> references, int frames, int columns) {
 		this.references = new ArrayList<>(references.size() + 1);//defensive
 		this.references.add(0,EMPTY);
 		this.references.addAll(references);
 		i = 1;
 		k = 0;
 		numberOfSteps = (columns - 1) * frames;
 		this.frames = frames;
 	}
 
 	public void clearParams() {
 		this.references = null;
 	    numberOfSteps= i = k= -1;
 	    frames = 0;
 	
 		
 	}
 	
 	private int getValue(int i, int k){
 		return references.get(i).getFrames().get(k);
 	}
 
 	/*
 	 * Moves the table's scrollbar to show the current column and row having
 	 * data added to it.
 	 */
 	private static void scrollToVisible(JTable table, int row, int col) {
 		if (!(table.getParent() instanceof JViewport))
 			return;
 	
 		JViewport viewport = (JViewport) table.getParent();
 		Rectangle rect = table.getCellRect(row, col, true);
 		Point p = viewport.getViewPosition();
 	
 		rect.setLocation(rect.x - p.x, rect.y - p.y);
 		viewport.scrollRectToVisible(rect);
 	}
 
 }
