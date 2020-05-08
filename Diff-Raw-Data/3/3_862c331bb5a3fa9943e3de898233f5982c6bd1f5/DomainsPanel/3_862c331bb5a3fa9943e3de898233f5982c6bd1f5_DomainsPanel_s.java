 package edu.ucsf.rbvi.cddApp.internal.ui;
 
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 
 import javax.swing.Icon;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 
 import org.cytoscape.application.CyApplicationManager;
 import org.cytoscape.application.swing.CytoPanelComponent;
 import org.cytoscape.application.swing.CytoPanelName;
 import org.cytoscape.model.CyNetwork;
 import org.cytoscape.model.CyTable;
 import org.cytoscape.model.events.RowSetRecord;
 import org.cytoscape.model.events.RowsSetEvent;
 import org.cytoscape.model.events.RowsSetListener;
 
 public class DomainsPanel extends JPanel implements CytoPanelComponent,
 		RowsSetListener {
 	
 	private JTextArea textArea;
 	private JScrollPane scrollPane;
 	private HashMap<Long, Boolean> selectedNodes;
 	private CyApplicationManager manager;
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 4255348824636450908L;
 
 	public DomainsPanel(CyApplicationManager manager) {
 		setLayout(new BorderLayout());
 		textArea = new JTextArea(50, 100);
 		textArea.setEditable(false);
 		scrollPane = new JScrollPane(textArea);
 		add(BorderLayout.CENTER, scrollPane);
 		selectedNodes = new HashMap<Long, Boolean>();
 		this.manager = manager;
 	}
 	
 	public void handleEvent(RowsSetEvent arg0) {
 		CyTable table = manager.getCurrentNetwork().getDefaultNodeTable();
 		String message = "";
 		Collection<RowSetRecord> record = arg0.getPayloadCollection();
 		for (RowSetRecord r: record) {
 			Long suid = r.getRow().get(CyNetwork.SUID, Long.class);
 			if (suid != null && table.getRow(suid) != null &&
 					table.getRow(suid).getList("PDB-Chain", String.class) != null &&
 					table.getRow(suid).getList("PDB-Chain", String.class).size() > 0 &&
 					table.getRow(suid).getList("PDB-Chain-Features", String.class) != null &&
 					table.getRow(suid).getList("PDB-Chain-Features", String.class).size() > 0)
 				selectedNodes.put(suid,r.getRow().get(CyNetwork.SELECTED, Boolean.class));
 		}
 		for (long node: selectedNodes.keySet()) {
 			if (table.getRow(node) != null && selectedNodes.get(node)) {
 				List<String> pdbChains = table.getRow(node).getList("PDB-Chain", String.class),
 						pdbChainFeatures = table.getRow(node).getList("PDB-Chain-Features", String.class),
 						cddAccession = table.getRow(node).getList("CDD-Accession", String.class),
 						domainType = table.getRow(node).getList("CDD-Hit-Type", String.class),
 						cddFeatureType = table.getRow(node).getList("CDD-Feature-Type", String.class),
 						cddFeatureSite = table.getRow(node).getList("CDD-Feature-Site", String.class);
 				List<Long> cddFrom = table.getRow(node).getList("CDD-From", Long.class),
 						cddTo = table.getRow(node).getList("CDD-To", Long.class); 
				
 				HashSet<String> chains = new HashSet<String>();
 				for (String s: pdbChains) chains.add(s);
 				for (String s: pdbChainFeatures) chains.add(s);
 				
 				HashMap<String, List<Integer>> pdbChainPos = new HashMap<String, List<Integer>>(),
 						pdbChainFeaturePos = new HashMap<String, List<Integer>>();
 				for (int i = 0; i < pdbChains.size(); i++) {
 					String n = pdbChains.get(i);
 					if (!pdbChainPos.containsKey(n))
 						pdbChainPos.put(n, new ArrayList<Integer>());
 					pdbChainPos.get(n).add(i);
 				}
 				for (int i = 0; i < pdbChainFeatures.size(); i++) {
 					String n = pdbChainFeatures.get(i);
 					if (!pdbChainFeaturePos.containsKey(n))
 						pdbChainFeaturePos.put(n, new ArrayList<Integer>());
 					pdbChainFeaturePos.get(n).add(i);
 				}
 				message = message + "Node Name:\n" + 
 						table.getRow(node).get(CyNetwork.NAME, String.class) + "\n\n";
 				for (String chain: chains) {
 					message = message + "Protein: " + chain + "\n";
 					for (int i: pdbChainPos.get(chain)) {
 						message = message + "CDD Accession: " + cddAccession.get(i) + "\n" +
 								"CDD Domain Type: " + domainType.get(i) + "\n" +
 								"Domain Range: " + cddFrom.get(i) + "-" + cddTo.get(i) + "\n\n";
 					}
 					for (int i: pdbChainFeaturePos.get(chain)) {
 						message = message + "CDD Feature Type: " + cddFeatureType.get(i) + "\n" +
 								"CDD Feature Site: " + cddFeatureSite.get(i) + "\n\n";
 					}
 				}
 			}
 		}
 		textArea.setText(message);
 	}
 
 	public Component getComponent() {
 		// TODO Auto-generated method stub
 		return this;
 	}
 
 	public CytoPanelName getCytoPanelName() {
 		// TODO Auto-generated method stub
 		return CytoPanelName.EAST;
 	}
 
 	public Icon getIcon() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public String getTitle() {
 		// TODO Auto-generated method stub
 		return "CDD Domains";
 	}
 
 }
