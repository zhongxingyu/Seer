 
 /*
  *
  *  MatchInformationFrame
  *  Copyright (C) 2012 Gaurav Vaidya
  *
  *  This file is part of TaxRef.
  *
  *  TaxRef is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  TaxRef is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with TaxRef.  If not, see <http://www.gnu.org/licenses/>.
  *
  */
 
 package com.ggvaidya.TaxRef.UI;
 
 import com.ggvaidya.TaxRef.Model.*;
 import java.util.List;
 import java.awt.*;
 import java.awt.event.*;
 import javax.swing.*;
 import javax.swing.event.*;
 
 /**
  * A MatchInformationFrame gathers information about a particular match and
  * displays it to the user.
  * 
  * @author Gaurav Vaidya <gaurav@ggvaidya.com>
  */
 public class MatchInformationPanel extends JPanel implements ActionListener, FocusListener, TableModelListener {
 	/** The row index being matched (and displayed). */
 	private RowIndex from;
 	
 	/** The row index match currently in progress; contains links to information
 	 * about matching columns as well as individual matches. Some of the code in
 	 * this class should eventually be moved there.
 	 */
 	private RowIndexMatch match;
 	
 	/* The selected cell (i.e. the one we are displaying) in row/col. */
 	private MainFrame mainFrame;
 	private int currentRow = -1;
 	private int currentCol = -1;
 	
 	/* UI elements */
 	private JTextField tf_name_to_match = new JTextField();
 	private JTextField tf_matched_name = new JTextField("", 20);
 	private JTextField tf_matched_taxonid = new JTextField("", 8);
 	private JTextField tf_accepted_name = new JTextField("", 20);
 	private JTextField tf_accepted_taxonid = new JTextField("", 8);
 	private JTextField tf_match_summary = new JTextField("Zero match problems!");
 	private JButton btn_prev = new JButton("<< Previous");
 	private JButton btn_next = new JButton("Next >>");
 	
 	/**
 	 * Creates a new match information panel.
 	 */
 	public MatchInformationPanel(MainFrame mf) {
 		super();
 		mainFrame = mf;
 		
 		tf_match_summary.setBackground(Color.GREEN);
 		tf_match_summary.setHorizontalAlignment(SwingConstants.CENTER);
 		
 		initPanel();
 	}
 	
 	/**
 	 * Actually constructs the panel with UI elements. Lots of RightLayout 
 	 * madness in here.
 	 */
 	private void initPanel() {
 		RightLayout rl = new RightLayout(this);
 		rl.add(new JLabel("Name to match: "), RightLayout.NONE);
 		
 		// Set up the text fields: only one is editable!
 		tf_name_to_match.addFocusListener(this);
 		tf_name_to_match.addActionListener(this);
 		tf_matched_name.setEditable(false);
 		tf_matched_taxonid.setEditable(false);
 		tf_accepted_name.setEditable(false);
 		tf_accepted_taxonid.setEditable(false);
 		
 		// Add all the elements into the panel.
 		rl.add(tf_name_to_match, RightLayout.BESIDE | RightLayout.STRETCH_X | RightLayout.FILL_3);
 		rl.add(new JButton(new AbstractAction("Search") {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				String nameToMatch = tf_name_to_match.getText();
 				
 				if(nameToMatch.equals(""))
 					return;
 				
 				mainFrame.searchName(nameToMatch);
 			}
 		}), RightLayout.BESIDE);
 		
 		rl.add(new JLabel("Matched name: "), RightLayout.NEXTLINE);
 		rl.add(tf_matched_name, RightLayout.BESIDE | RightLayout.STRETCH_X);
 		rl.add(new JLabel("TaxonID"), RightLayout.BESIDE);
 		rl.add(tf_matched_taxonid, RightLayout.BESIDE);
 		rl.add(new JButton(new AbstractAction("Look up") {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				String taxonID = tf_matched_taxonid.getText();
 				
 				if(taxonID.equals("(none)") || taxonID.equals(""))
 					return;
 				
 				mainFrame.lookUpTaxonID(taxonID);
 			}
 		}), RightLayout.BESIDE);
 		
 		rl.add(new JLabel("Accepted name: "), RightLayout.NEXTLINE);
 		rl.add(tf_accepted_name, RightLayout.BESIDE | RightLayout.STRETCH_X);
 		rl.add(new JLabel("TaxonID"), RightLayout.BESIDE);
 		rl.add(tf_accepted_taxonid, RightLayout.BESIDE);
 		rl.add(new JButton(new AbstractAction("Look up") {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				String taxonID = tf_accepted_taxonid.getText();
 				
 				if(taxonID.equals("(none)") || taxonID.equals(""))
 					return;
 				
 				mainFrame.lookUpTaxonID(taxonID);
 			}
 		}), RightLayout.BESIDE);
 		
 		btn_prev.addActionListener(this);
 		btn_next.addActionListener(this);
 		
 		rl.add(btn_prev, RightLayout.NEXTLINE);
 		rl.add(tf_match_summary, RightLayout.BESIDE | RightLayout.FILL_3);
 		rl.add(btn_next, RightLayout.BESIDE);
 	}
 	
 	/**
 	 * The match changed. Blank all the fields so that the user can select a
 	 * new cell.
 	 * 
 	 * @param match 
 	 */
 	public void matchChanged(RowIndexMatch match) {
 		this.match = match;
 		currentCol = -1;
 		currentRow = -1;
 		
 		blankAllFields();
 	}
 	
 	/**
 	 * A little helper method to blank all the cells.
 	 */
 	private void blankAllFields() {
 		tf_name_to_match.setText("");
 		tf_matched_name.setText("");
 		tf_matched_taxonid.setText("");
 		tf_accepted_name.setText("");
 		tf_accepted_taxonid.setText("");
 		tf_match_summary.setText("");
 	}
 	
 	/**
 	 * A name was selected. Note that this may be called before 
 	 * {@link #matchChanged} -- if a file is opened and a Name in that
 	 * file is selected before a match is made, for instance.
 	 * 
 	 * @param name The name which was selected. 
 	 */
 	public void nameSelected(RowIndex newFrom, Name name, int currentRow, int currentCol) {
 		this.currentRow = currentRow;
 		this.currentCol = currentCol;
 		
 		// If there's a new from, let's add ourselves as TableModelListeners,
 		// so we know when somebody edits a cell we're currently displaying.
 		if(from != newFrom) {
 			if(from != null)
 				from.removeTableModelListener(this);
 			from = newFrom;
 			from.addTableModelListener(this);
 		}
 		
 		blankAllFields();
 		
 		// If no Name is currently selected, don't do anything.
 		if(name == null)
 			return;
 		
 		// If there is a name selected, display it.
 		tf_name_to_match.setText(name.toString());
 		
 		if(match == null) {
 			// No match in progress? No need to do anything but display the 
 			// name!
 			return;
 		} else {
 			RowIndex against = match.getAgainst();
 			List<Object[]> rows;
 			
 			// If there isn't a real match going on, say so and exit.
 			if(!against.hasName(name) && !against.hasName(name.getGenus())) {
 				tf_matched_name.setText("(no match)");
 				tf_matched_taxonid.setText("(none)");
 				tf_accepted_name.setText("(no match)");
 				tf_accepted_taxonid.setText("(none)");
 				
 				return;
 			}
 			
 			// Figure out the matched name based on the kind of match.
 			// This logic should definitely be elsewhere, but where?
 			// ColumnMatch? RowIndex?
 			if(against.hasName(name)) {
 				rows = against.getNameRows(name);
 				tf_matched_name.setText(name.toString());
 			} else if(against.hasName(name.getGenus())) {
 				rows = against.getNameRows(name.getGenus());
 				tf_matched_name.setText(name.getGenus().toString());
 			} else
 				throw new RuntimeException("logical error in MatchInformationPanel.java");
 			
 			// Can't deal with multiple information yet. Eventually, another module
 			// should be able to figure this out by summarizing the rows returned.
 			// Remember that all this needs to happen at render-speeds, but it's
 			// computationally really simple, so it should be totally doable, just
 			// complicated.
 			if(rows.size() > 1) {
 				tf_matched_taxonid.setText("(many)");
 				tf_accepted_name.setText("(multiple)");
 				tf_accepted_taxonid.setText("(many)");
 				return;
 			}
 			
 			// Get the (single) row we have.
 			Object[] row = rows.get(0);
 			
 			// Figure out the taxonid.
 			if(against.hasColumn("taxonid")) {
 				tf_matched_taxonid.setText(row[against.getColumnIndex("taxonid")].toString());
 			} else if(against.hasColumn("id")) {
 				tf_matched_taxonid.setText(row[against.getColumnIndex("id")].toString());
 			}
 			
 			// Figure out the accepted name column to use.
 			int acceptedname_col = -1;
 			int acceptednameid_col = -1;
 			
 			if(against.hasColumn("acceptedname")) {
 				acceptedname_col = against.getColumnIndex("acceptedname");
 				acceptednameid_col = against.getColumnIndex("acceptednameid");
 			} else if(against.hasColumn("acceptednameusage")) {
 				acceptedname_col = against.getColumnIndex("acceptednameusage");
 				acceptednameid_col = against.getColumnIndex("acceptednameusageid");
 			} else if(against.hasColumn("acceptednameusageid")) {
 				acceptednameid_col = against.getColumnIndex("acceptednameusageid");
 				// TODO: figure out accepted name from acceptednameusageid.
 			} else {
 				return;
 			}
 			
 			// Figure out which accepted name to use.
 			String acceptedName = null;
 			String acceptedNameID = null;
 			
 			if(acceptedname_col != -1)		acceptedName =   row[acceptedname_col].toString();
 			if(acceptednameid_col != -1) {
 				acceptedNameID = row[acceptednameid_col].toString();
 			
 				// If we have an acceptedNameID and it's equal to either blank
 				// or matched_taxonid, then it's the same as the matched name.
 				if(acceptedNameID.equals("") || acceptedNameID.equals(tf_matched_taxonid.getText())) {
 					acceptedName = tf_matched_name.getText();
 					acceptedNameID = tf_matched_taxonid.getText();
 				}
 			}
 			
 			// Set the text fields!
 			tf_accepted_name.setText(acceptedName);
 			tf_accepted_taxonid.setText(acceptedNameID);
 		}
 	}
 
 	/*
 	 * Action listener: if the user hits enter in an editable text field,
 	 * update the value.
 	 */
 	
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		if(JTextField.class.isAssignableFrom(e.getSource().getClass())) {
 			textUpdated((JTextField)e.getSource());
 		} else {
 			if(e.getSource().equals(btn_prev)) {
 				mainFrame.goToRow(-1);
 			} else {
 				mainFrame.goToRow(+1);
 			}
 		}
 	}
 
 	/* 
 	 * Focus listener: when the editable text fields lose value, update their
 	 * value wherever.
 	 * 
 	 */
 	
 	@Override
 	public void focusGained(FocusEvent e) {
 		// Do nothing.
 	}
 
 	@Override
 	public void focusLost(FocusEvent e) {
 		textUpdated((JTextField)e.getComponent());
 	}
 
 	/**
 	 * A text field was updated. Do something!
 	 * 
 	 * @param textField The text field which was updated.
 	 */
 	private void textUpdated(JTextField textField) {
 		System.err.println("Text field changed: (" + from + ", " + currentRow + ", " + currentCol + "): " + textField);
 		
 		// only tf_name_to_match may be reasonably changed, so only accept changes there.
 		if(!textField.equals(tf_name_to_match))
 			return;
 		
 		// Is a "Name" cell actually selected?
 		if(from == null || currentRow == -1 || currentCol == -1)
 			return;
 		
 		// Update the value!
 		from.setValueAt(tf_name_to_match.getText(), currentRow, currentCol);
 	}
 
 	/**
 	 * The table was changed. Do nothing unless it's the row and column we're
 	 * displaying, in which case recalculate our display.
 	 * 
 	 * @param e A TableModelEvent describing which rows and columns have changed. 
 	 */
 	@Override
 	public void tableChanged(TableModelEvent e) {
 		// If the currently selected value changes, redisplay it.
 		System.err.println("Table changed! (" + e.getColumn() + ", " + e.getFirstRow() + " to " + e.getLastRow() + "), I am at (" + currentCol + ", " + currentCol + ")");
 		if(e.getColumn() == currentCol && e.getFirstRow() >= currentRow && e.getLastRow() <= currentRow) {
 			nameSelected(from, (Name)from.getValueAt(currentRow, currentCol), currentRow, currentCol);
 		}
 	}
 
 
 
 }
