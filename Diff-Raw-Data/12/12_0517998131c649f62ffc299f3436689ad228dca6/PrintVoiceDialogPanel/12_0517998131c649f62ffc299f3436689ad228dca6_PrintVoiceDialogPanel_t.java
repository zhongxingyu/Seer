 package com.t_oster.notenschrank.gui;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.IOException;
 import java.util.LinkedList;
 import java.util.Set;
 
 import javax.swing.BoxLayout;
 import javax.swing.JCheckBox;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.table.AbstractTableModel;
 
 import com.t_oster.notenschrank.data.Archive;
 import com.t_oster.notenschrank.data.SettingsManager;
 import com.t_oster.notenschrank.data.Sheet;
 import com.t_oster.notenschrank.data.Song;
 
 public class PrintVoiceDialogPanel extends JPanel{
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -2646439482396979485L;
 	private SelectVoiceBox bVoice;
 	private JTable tSongs;
 	private AbstractTableModel tSongsModel;
 	private JCheckBox cbPreview;
 	private Song[] availableSongs;
 	private boolean[] selectedNumbers;
 	
 	
 	public PrintVoiceDialogPanel(){
 		//this.setLayout(new GridLayout(0,2));
 		bVoice = new SelectVoiceBox(false);
 		availableSongs = Archive.getInstance().getAvailableSongs(bVoice.getSelectedVoice());
 		selectedNumbers = new boolean[availableSongs.length];
 		//do preselection
 		Set<String> defaultSongs = SettingsManager.getInstance().getDefaultSongs();
 		for (int i=0;i<selectedNumbers.length;i++){
 			selectedNumbers[i]=( defaultSongs.contains(availableSongs[i].toString()) );
 		}
 		bVoice.addActionListener(new ActionListener(){
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				availableSongs = Archive.getInstance().getAvailableSongs(bVoice.getSelectedVoice());
 				selectedNumbers = new boolean[availableSongs.length];
 				//do preselection
 				Set<String> defaultSongs = SettingsManager.getInstance().getDefaultSongs();
 				for (int i=0;i<selectedNumbers.length;i++){
 					selectedNumbers[i]=( defaultSongs.contains(availableSongs[i].toString()) );
 				}
 				tSongsModel.fireTableDataChanged();
 			}
 			
 		});
 		tSongsModel = new AbstractTableModel(){
 
 			/**
 			 * 
 			 */
 			private static final long serialVersionUID = -1328906876906673675L;
 
 			public String getColumnName(int column){
				return column==0?"Stück":"auswählen";
 			}
 			
 			public boolean isCellEditable(int r, int c){
 				return (c==1);
 			}
 			
 			 public void setValueAt(Object value, int row, int col) {
 				 	if (col==1 && value instanceof Boolean){
 				        selectedNumbers[row]=(Boolean) value;
 				        fireTableCellUpdated(row, col);
 				 	}
 			    }
 			
 			@Override
 			public int getColumnCount() {
 				return 2;
 			}
 
 			@SuppressWarnings({ "unchecked", "rawtypes" })
 			public Class getColumnClass(int c) {
 			        return getValueAt(0, c).getClass();
 			}
 			
 			@Override
 			public int getRowCount() {
 				return availableSongs==null?0:availableSongs.length;
 			}
 
 			@Override
 			public Object getValueAt(int rowIndex, int columnIndex) {
 				if (columnIndex==0){
 					return availableSongs[rowIndex];
 				}
 				else{
 					return selectedNumbers[rowIndex];
 				}
 			}
 			
 		};
 		tSongs = new JTable(tSongsModel);
 		tSongs.setFillsViewportHeight(true);
 		cbPreview = new JCheckBox("Druckvorschau");
 		
 		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
 		JPanel tmp = new JPanel();
		tmp.add(new JLabel("Stimme"));
 		tmp.add(bVoice);
 		add(tmp);
 		add(new JScrollPane(tSongs));
 		add(cbPreview);
 	}
 	
 	public boolean isPreviewSelected(){
 		return cbPreview.isSelected();
 	}
 	
 	public Sheet[] getSelectedSheets() throws IOException{
 		LinkedList<Sheet> result = new LinkedList<Sheet>();
 		for (int i=0;i<availableSongs.length;i++){
 			if (selectedNumbers[i]){
 				result.add(Archive.getInstance().getSheet(availableSongs[i], bVoice.getSelectedVoice()));
 			}
 		}
 		return result.toArray(new Sheet[0]);
 	}
 	
 }
