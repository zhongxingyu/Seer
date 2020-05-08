 /*******************************************************************************
  * Copyright 2008, 2009 Institute of Mathematics and Computer Science, University of Latvia; Author: Pēteris Paikens, Imants Borodkins
  *     This program is free software: you can redistribute it and/or modify
  *     it under the terms of the GNU General Public License as published by
  *     the Free Software Foundation, either version 3 of the License, or
  *     (at your option) any later version.
  * 
  *     This program is distributed in the hope that it will be useful,
  *     but WITHOUT ANY WARRANTY; without even the implied warranty of
  *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *     GNU General Public License for more details.
  * 
  *     You should have received a copy of the GNU General Public License
  *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
  *******************************************************************************/
 package lv.semti.annotator;
 
 import java.awt.Color;
 import java.util.Date;
 import java.util.Map.Entry;
 import javax.swing.table.AbstractTableModel;
 
 import lv.semti.morphology.analyzer.*;
 import lv.semti.morphology.attributes.AttributeNames;
 import lv.semti.morphology.corpus.Statistics;
 
 @SuppressWarnings("serial")
 public class VardinfoModel extends AbstractTableModel {
 
 	Word vārds = null;
 	MainFrame rāmis = null;
 	String[] columnNames = { "", "Vārds", "Marķējums", "Apraksts",
 			"Galotnes nr.", "Ticamība" };
 
	private Statistics statistics;

	// FIXME - hvz vai labākā vieta kur to statistiku linkot....

 	public void setVārds(Word vārds) {
 		if (this.vārds != null)
 			this.vārds.setTableModel(null);
 
 		if (vārds != null)
 			vārds.setTableModel(this);
 
 		this.vārds = vārds;
 		fireTableDataChanged();
 	}
 
 	public VardinfoModel(MainFrame rāmis) {
 		this.rāmis = rāmis;
		try {
			statistics = new Statistics(Statistics.DEFAULT_STATISTICS_FILE);
		} catch (Exception e) {
			e.printStackTrace();
		}
 	}
 
 	@Override
 	public String getColumnName(int col) {
 		return columnNames[col].toString();
 	}
 	
 	@Override
 	public Class<?> getColumnClass(int columnIndex) {
 		switch (columnIndex) {
 		case 0: return MButton.class;
 		case 1: return String.class;
 		case 2: return String.class;
 		case 3: return String.class;
 		case 4: return Integer.class;
 		case 5: return Integer.class;  
 		}
 		return Object.class;
 	}
 
 	public int getColumnCount() {
 		return columnNames.length;
 	}
 
 	public int getRowCount() {
 		if (vārds == null)
 			return 0;
 		return vārds.wordformsCount() + 4;
 	}
 
 	public Object getValueAt(int row, int col) {
 		if (vārds == null)
 			return null;
 		if (row < 0 || row >= this.getRowCount())
 			return null;
 
 		if (row == vārds.wordformsCount()) {
 			switch (col) {
 			case 0:
 				return new MButton(rāmis, row);
 			case 1:
 				return vārds.getToken();
 			case 2:
 				return "Es zinu labāk!";
 			default:
 				return null;
 			}
 		}
 		if (row > vārds.wordformsCount() ) {
 			switch (col) {
 			case 0:
 				return new MButton(rāmis, row);
 			case 1:
 				return vārds.getToken();
 			case 2:
 				switch (row-vārds.wordformsCount()) {
 				case 1: return "Bezmorfoloģijas elements";
 				case 2: return "Vārds svešvalodā";
 				case 3: return "Saīsinājums";
 				}
 				
 			default:
 				return null;
 			}
 		}
 
 		Wordform vārdforma = vārds.wordforms.get(row);
 		switch (col) {
 		case 0:
 			return new MButton(rāmis, row);
 		case 1:
 			return vārds.getToken();
 		case 2:
 			StringBuilder sb = new StringBuilder();
 			sb.append('<');
 			sb.append(MarkupConverter.charsToPrologList(vārdforma.getTag()));
 			sb.append(",'").append(vārdforma.getValue(AttributeNames.i_Lemma))
 					.append("'");
 			sb.append('>');
 			return sb.toString();
 		case 3:
 			return vārdforma.getDescription();
 		case 4:
 			String endingID = vārdforma.getValue(AttributeNames.i_EndingID);			
 			return (endingID == null) ? 0 :Integer.parseInt(endingID);
 		case 5:
			return new Double(statistics.getEstimate(vārdforma));
 
 		default:
 			return null;
 		}
 	}
 
 	public Color getColor(int row) {
 		if (vārds == null)
 			return Color.white;
 		if (!vārds.isRecognized() || row >= vārds.wordformsCount())
 			return Color.white;
 		if (row < 0 || row >= this.getRowCount())
 			return null;
 
 		if (vārds.wordforms.get(row).isMatchingStrong(AttributeNames.i_Tagged, AttributeNames.v_Yes))
 			return Color.green;
 
 		if (vārds.wordforms.get(row).isMatchingStrong(AttributeNames.i_Recommended, AttributeNames.v_Yes))
 			return Color.yellow; //TODO - prasīt Laurai vai šitā vajag 
 
 		return Color.white;
 	}
 	
 	/**
 	 * Which row is considered to be 'correct' and automatically focused when a new word is added
 	 * @return
 	 */
 	public int autofocusRow() {
 		if (vārds == null) return 0;
 		if (!vārds.isRecognized()) return 0;
 		for (int row = 0; row < vārds.wordforms.size(); row++) 		
 			if (vārds.wordforms.get(row).isMatchingStrong(AttributeNames.i_Tagged, AttributeNames.v_Yes))
 				return row;
 		return 0;		
 	}
 
 	public void pareizaisVariantsIr(int row) {
 		if (row < vārds.wordformsCount())
 			vārds.setCorrectWordform(vārds.wordforms.get(row));
 		else if (row == vārds.wordformsCount()) {
 			Wordform vārdforma = new Wordform(vārds.getToken());
 			vārdforma.addAttribute(AttributeNames.i_Source, String.format(
 					"Lietotājs zināja labāk %tF", new Date()));
 			vārdforma.addAttribute(AttributeNames.i_Lemma, vārds.getToken());
 			vārdforma.addAttribute(AttributeNames.i_PartOfSpeech, AttributeNames.v_Residual);
 			vārds.wordforms.add(vārdforma);
 			vārds.setCorrectWordform(vārdforma);
 		} else if (row > vārds.wordformsCount()) {
 			Wordform vārdforma = new Wordform(vārds.getToken());
 			switch (row-vārds.wordformsCount()) {
 			case 1: vārdforma.addAttribute(AttributeNames.i_PartOfSpeech, AttributeNames.v_Residual); break;
 			case 2: 
 				vārdforma.addAttribute(AttributeNames.i_PartOfSpeech, AttributeNames.v_Residual);
 				vārdforma.addAttribute(AttributeNames.i_ResidualType, AttributeNames.v_Foreign);
 				break;
 			case 3: vārdforma.addAttribute(AttributeNames.i_PartOfSpeech, AttributeNames.v_Abbreviation); break;
 			}
 			vārdforma.addAttribute(AttributeNames.i_Lemma, vārds.getToken());
 			vārds.wordforms.add(vārdforma);
 			vārds.setCorrectWordform(vārdforma);
 		}
 	}
 
 	public String getToolTip(int row) {
 		if (vārds == null)
 			return null;
 		if (row < 0 || row >= vārds.wordformsCount())
 			return null;
 
 		Wordform vārdforma = vārds.wordforms.get(row);
 
 		String īpašības = "";
 		for (Entry<String, String> pāris : vārdforma.entrySet())
 			īpašības = īpašības + pāris.getKey() + " = " + pāris.getValue()
 					+ "\n";
 
 		return īpašības;
 	}
 }
