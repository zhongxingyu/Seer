 // PathVisio,
 // a tool for data visualization and analysis using Biological Pathways
 // Copyright 2006-2009 BiGCaT Bioinformatics
 //
 // Licensed under the Apache License, Version 2.0 (the "License"); 
 // you may not use this file except in compliance with the License. 
 // You may obtain a copy of the License at 
 // 
 // http://www.apache.org/licenses/LICENSE-2.0 
 //  
 // Unless required by applicable law or agreed to in writing, software 
 // distributed under the License is distributed on an "AS IS" BASIS, 
 // WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 // See the License for the specific language governing permissions and 
 // limitations under the License.
 //
 package org.pathvisio.sbml;
 
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.Font;
 import java.awt.event.ActionEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.io.File;
 import java.rmi.RemoteException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Vector;
 import java.util.concurrent.ExecutionException;
 
 import javax.swing.AbstractAction;
 import javax.swing.Action;
 import javax.swing.BorderFactory;
 import javax.swing.DefaultListCellRenderer;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.JTextField;
 import javax.swing.SwingWorker;
 import javax.swing.border.Border;
 import javax.swing.table.AbstractTableModel;
 import javax.swing.table.TableRowSorter;
 
 import org.pathvisio.core.debug.Logger;
 import org.pathvisio.core.util.ProgressKeeper;
 import org.pathvisio.gui.ProgressDialog;
 
 import uk.ac.ebi.biomodels.ws.BioModelsWSClient;
 
 import com.jgoodies.forms.layout.CellConstraints;
 import com.jgoodies.forms.layout.FormLayout;
 
 import org.pathvisio.sbml.SBMLPlugin;
 /**
  * This class creates the content in the Dialog of the Search
  */
 public class BioModelPanel extends JPanel {
 	
 	SBMLPlugin plugin;
 	public static Border etch = BorderFactory.createEtchedBorder();
 	JComboBox clientDropdown;
 
 	JTable resultTable;
 	int i = 0;
 
 	private JTextField txtId;
 
 	private JScrollPane resultspane;
 
 	public int flag = 0;
 	private JTextField sbmlName;
 	private JTextField chebiId;
 	//private JLabel tipLabel;
 	private JTextField uniprotId;
 	private JTextField pubTitId;
 	private JButton search;
 	private JTextField person;
 
 	public BioModelPanel(final SBMLPlugin plugin) {
 
 		this.plugin = plugin;
 
 		setLayout(new BorderLayout());
 
 		sbmlName = new JTextField();
 		chebiId = new JTextField();
 		uniprotId = new JTextField();
 		pubTitId = new JTextField();
 		person = new JTextField();
 		sbmlName.setToolTipText("Tip:Use Biomodel name (e.g.:'Tyson1991 - Cell Cycle 6 var')");
 		pubTitId.setToolTipText("Tip:Use publication name(e.g.:'sbml')");
 		chebiId.setToolTipText("Tip:Use Chebi id (e.g.:'24996')");
 		person.setToolTipText("Tip:Use person/encoder name (e.g.:'Rainer','Nicolas')");
 		uniprotId.setToolTipText("Tip:Use Uniprot id (e.g.:'P04637','P10113')");
 		//tipLabel = new JLabel(
 			//	"Tip: use Biomodel name (e.g.:'Tyson1991 - Cell Cycle 6 var')");
 		
 		//tipLabel.setFont(new Font("SansSerif", Font.ITALIC, 11));
 		
 		Action searchLiteratureAction = new AbstractAction("searchlit") {
 			public void actionPerformed(ActionEvent e) {
 				try {
 					resultspane.setBorder(BorderFactory.createTitledBorder(
 							etch, "BioModels"));
 					search();
 				} catch (Exception ex) {
 					JOptionPane
 							.showMessageDialog(BioModelPanel.this,
 									ex.getMessage(), "Error",
 									JOptionPane.ERROR_MESSAGE);
 					Logger.log.error("Error searching Biomodels", ex);
 				}
 			}
 
 		};
 
 	
 		
 		JPanel searchBox = new JPanel();
 		FormLayout layoutf = new FormLayout(
 				"p,3dlu,120px,2dlu,30px,fill:pref:grow,3dlu,fill:pref:grow,3dlu",
 				"pref, pref, 14dlu, pref, 4dlu, pref");
 		CellConstraints ccf = new CellConstraints();
 
 		searchBox.setLayout(layoutf);
 		searchBox.setBorder(BorderFactory.createTitledBorder(etch));
 
 		JPanel searchOptBox = new JPanel();
 		FormLayout layout = new FormLayout(
 				"3dlu,p,3dlu,2dlu,30px,fill:pref:grow,2dlu",
 				"pref, pref, 14dlu, 14dlu, 14dlu, pref,pref,pref");
 		CellConstraints cc = new CellConstraints();
 
 		searchOptBox.setLayout(layout);
 		searchOptBox.setBorder(BorderFactory.createTitledBorder(etch,
 				"Search options"));
 
 		searchOptBox.add(new JLabel("Biomodel name:"), cc.xy(2, 1));
 		//searchOptBox.add(tipLabel, cc.xyw(2, 2, 5));					
 		searchOptBox.add(new JLabel("Publication Title/ID:"), cc.xy(2, 3));
 		//searchOptBox.add(tipLabel,cc.xyw(2, 3, 5));
 		searchOptBox.add(new JLabel("Chebi ID:"),cc.xy(2, 4));
 		searchOptBox.add(new JLabel("Person:"),cc.xy(2, 5));
 		searchOptBox.add(new JLabel("Uniprot ID:"),cc.xy(2,6));
 		searchOptBox.add(sbmlName, cc.xyw(4, 1, 3));
 		searchOptBox.add(pubTitId,cc.xyw(4, 3,3));
 		searchOptBox.add(chebiId,cc.xyw(4, 4, 3));
 		searchOptBox.add(person,cc.xyw(4, 5, 3));
 		searchOptBox.add(uniprotId,cc.xyw(4, 6,3));
  search= new JButton("search");
  search.addActionListener(searchLiteratureAction);
  searchOptBox.add(search,cc.xyw(4,7,3));
 		Vector<String> clients = new Vector<String>(plugin.getClients()
 				.keySet());
 		Collections.sort(clients);
 
 		clientDropdown = new JComboBox(clients);
 		clientDropdown.setSelectedIndex(0);
 		clientDropdown.setRenderer(new DefaultListCellRenderer() {
 			public Component getListCellRendererComponent(final JList list,
 					final Object value, final int index,
 					final boolean isSelected, final boolean cellHasFocus) {
 				String strValue = SBMLPlugin.shortClientName(value.toString());
 				return super.getListCellRendererComponent(list, strValue,
 						index, isSelected, cellHasFocus);
 			}
 		});
 
 		searchOptBox.add(clientDropdown, cc.xy(6, 1));
 
 		if (plugin.getClients().size() < 2)
 			clientDropdown.setVisible(false);
 		searchBox.add(searchOptBox, ccf.xyw(1, 1, 8));
 
 		add(searchBox, BorderLayout.NORTH);
 
 		// Center contains table model for results
 		resultTable = new JTable();
 		resultspane = new JScrollPane(resultTable);
 
 		add(resultspane, BorderLayout.CENTER);
 
 		resultTable.addMouseListener(new MouseAdapter() {
 			public void mouseClicked(MouseEvent e) {
 				if (e.getClickCount() == 2) {
 					JTable target = (JTable) e.getSource();
 					int row = target.getSelectedRow();
 
 					try {
 
 						ResultTableModel model = (ResultTableModel) target
 								.getModel();
 						File tmpDir = new File(plugin.getTmpDir(), SBMLPlugin
 								.shortClientName(model.clientName));
 						tmpDir.mkdirs();
 						plugin.openPathwayWithProgress(
 								plugin.getClients().get(model.clientName),
 								model.getValueAt(row, 0).toString(), 0, tmpDir);
 
 					} catch (Exception ex) {
 						JOptionPane.showMessageDialog(BioModelPanel.this,
 								ex.getMessage(), "Error",
 								JOptionPane.ERROR_MESSAGE);
 						Logger.log.error("Error", ex);
 					}
 				}
 			}
 		});
 	}
 
 	private void search() throws RemoteException, InterruptedException,
 			ExecutionException {
		final String sbmlname = sbmlName.getText();
		final String sbmlpub = pubTitId.getText();
		final String sbmlchebi = chebiId.getText();
		final String sbmlperson = person.getText();
		final String sbmluniprot = uniprotId.getText();
 		if (!(sbmlpub.isEmpty()&&sbmlname.isEmpty()&&sbmlchebi.isEmpty()&&sbmlperson.isEmpty()&&sbmluniprot.isEmpty())) {
 			String clientName = clientDropdown.getSelectedItem().toString();
 			final BioModelsWSClient client = plugin.getClients()
 					.get(clientName);
 
 			final ProgressKeeper pk = new ProgressKeeper();
 			final ProgressDialog d = new ProgressDialog(
 					JOptionPane.getFrameForComponent(this), "", pk, true, true);
 			final ArrayList<String> results = new ArrayList<String>();
 			SwingWorker<String[], Void> sw = new SwingWorker<String[], Void>() {
 				
 				protected String[] doInBackground() throws Exception {
 					pk.setTaskName("Searching Biomodels");
 					String[] results1 = null;
 					String[] results2 = null;
 					String[] results3 = null;
 					String[] results4 = null;
 					String[] results5 = null;
 					try {
 						// getting the models id by name
 						if(!sbmlName.getText().equalsIgnoreCase(""))
 						{
 						results1 = client.getModelsIdByName(sbmlname);
 						if(results1!=null){
 						 for (int i = 0; i < results1.length; i++) {
 								
 								results.add(results1[i]);
 							}
 						}
 						}
 						if(!pubTitId.getText().equalsIgnoreCase(""))
 						{
 						results2= client.getModelsIdByPublication(sbmlpub);
 						if(results2!=null){ 
 						for (int i = 0; i < results2.length; i++) {
 								
 								results.add(results2[i]);
 							}
 						}
 						}
 						if(!chebiId.getText().equalsIgnoreCase(""))
 						{
 						results3= client.getModelsIdByChEBIId(sbmlchebi);
 						if(results3!=null){
 						for (int i = 0; i < results3.length; i++) {
 								
 								results.add(results3[i]);
 							}
 						}
 						}
 						if(!person.getText().equalsIgnoreCase(""))
 						{
 						results4= client.getModelsIdByPerson(sbmlperson);
 						if(results4!=null){ 
 						for (int i = 0; i < results4.length; i++) {
 								
 								results.add(results4[i]);
 							}
 						}
 						}
 						if(!uniprotId.getText().equalsIgnoreCase(""))
 						{
 						results5= client.getModelsIdByUniprot(sbmluniprot);
 						if(results5!=null){ 
 						for (int i = 0; i < results5.length; i++) {
 								
 								results.add(results5[i]);
 							}
 						}
 						}
 
 					} catch (Exception e) {
 						throw e;
 					} finally {
 						pk.finished();
 					}
 					
 					
 					 String[] finalresults = new String[results.size()];
 					 
 						results.toArray(finalresults);
 						
 					return finalresults;
 					
 					
 				}
 				  
 				 protected void done() {
 			           if(!pk.isCancelled())
 			             {
 			              if(results.size()==0)
 			              {
 			                 JOptionPane.showMessageDialog(null,"0 results found");
 			              }
 			             }
 			             else if(pk.isCancelled())
 			             {
 			               pk.finished();
 			             }
 			           }          
 			};
 
 			sw.execute();
 			d.setVisible(true);
 
 			resultTable.setModel(new ResultTableModel(sw.get(), clientName));
 			resultTable
 					.setRowSorter(new TableRowSorter(resultTable.getModel()));
 		} else {
 			JOptionPane.showMessageDialog(null, "Please Enter a Search Query",
 					"ERROR", JOptionPane.ERROR_MESSAGE);
 		}
 	}
 
 	private class ResultTableModel extends AbstractTableModel {
 		String[] results;
 		String[] columnNames = new String[] { "Name" };
 		String clientName;
 
 		public ResultTableModel(String[] results, String clientName) {
 			this.clientName = clientName;
 			this.results = results;
 
 		}
 
 		public int getColumnCount() {
 			return 1;
 		}
 
 		public int getRowCount() {
 			return results.length;
 		}
 
 		public Object getValueAt(int rowIndex, int columnIndex) {
 			String r = results[rowIndex];
 
 			return r;
 		}
 
 		public String getColumnName(int column) {
 			return columnNames[column];
 		}
 	}
 
 }
