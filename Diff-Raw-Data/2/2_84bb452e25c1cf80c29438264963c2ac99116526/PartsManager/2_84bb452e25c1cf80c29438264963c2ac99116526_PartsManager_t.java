 package factory.client.partManager; 
 import javax.swing.*;
 import java.util.*;
 import java.awt.*;
 import java.awt.event.*;
 import java.io.*;
 import factory.global.data.*;
 import factory.global.network.*;
 public class PartsManager extends JFrame implements ActionListener, NetworkManager
 {
 	protected TreeMap<Integer, Parts> currentParts;
 	private PartsInfoPanel pip;
 	private PartsIconPanel picp;
 	private SelectPartPanel spp;
 	private PartsCreatorPanel pcp;
 	private JPanel panel = new JPanel();
 	private ArrayList<JButton> newButtons;
 	private ArrayList<JButton> editButtons;
 	private ArrayList<JButton> deleteButtons;
 	private int index = -1;
 	NetworkBridge nb1;
 	@SuppressWarnings("unchecked")
 	public PartsManager()
 	{
 		nb1 = new NetworkBridge(this, "localhost", 8465, 0);
 		/*FileInputStream fin;
 		ObjectInputStream oin;
 		try
 		{
 			fin = new FileInputStream("Parts");
 			oin = new ObjectInputStream(fin);
 			currentParts = (TreeMap<Integer, Parts>) oin.readObject();
 		}catch(Exception e)
 		{
 			currentParts = new TreeMap<Integer, Parts>();
 		}*/
 	}
 	
 	public void initialize()
 	{
 		panel.removeAll();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
 		pip = new PartsInfoPanel(currentParts);
 		picp = new PartsIconPanel();
 		spp = new SelectPartPanel(this);
 		pcp = new PartsCreatorPanel(this);
 		//pcp.setVisible(false);
 		newButtons = spp.getNewButtons();
 		editButtons = spp.getEditButtons();
 		deleteButtons = spp.getDeleteButtons();
 		panel.add(pip);
 		panel.add(picp);
 		panel.add(spp);
 		panel.add(pcp);
 		add(panel);
 	}
 
 	public static void main(String[] args)
 	{
 		PartsManager frame = new PartsManager();
 		//frame.setSize(1000, 350);
 		//frame.setVisible(true);
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent e) 
 	{
 		for (int i = 0; i < newButtons.size(); i++)
 			if (e.getSource() == newButtons.get(i))
 			{
 				if (currentParts.get(i) == null)
 				{
 					index = i;
 					spp.newButtons.get(i).setEnabled(false);
 					spp.editButtons.get(i).setEnabled(true);
 					spp.deleteButtons.get(i).setEnabled(true);
 					spp.setVisible(false);
 					pcp.setVisible(true);
 				}
 			}
 				
 		for (int i = 0; i < editButtons.size(); i++)
 			if (e.getSource() == editButtons.get(i))
 			{
 				if (currentParts.get(i) != null)
 				{
 					index = i;
 					Parts t = currentParts.get(i);
 					pcp.partName.setText(t.getName());
 					pcp.partID.setText("" + t.getPartNumber());
 					pcp.partDes.setText(t.getDesc());
 					spp.setVisible(false);
 					pcp.setVisible(true);
 				}
 			}		
 		for (int i = 0; i < newButtons.size(); i++)
 			if (e.getSource() == deleteButtons.get(i))
 			{
 				if (currentParts.get(i) != null)
 				{
 					currentParts.remove(i);
 					spp.newButtons.get(i).setEnabled(true);
 					spp.editButtons.get(i).setEnabled(false);
 					spp.deleteButtons.get(i).setEnabled(false);
 				}
 			}
 		if (e.getSource() == pcp.CreatePart)
 		{
 			int ID;
 			try
 			{
 				ID = Integer.parseInt(pcp.partID.getText());
 			}catch (Exception ex)
 			{
 				ID = -1;
 			}
 			if (ID >= 0)
 			{
 				Parts p = new Parts(ID, pcp.partName.getText(), pcp.partDes.getText(), index + 1);
 				currentParts.put(index, p);
 				pcp.partName.setText("Please enter part name");
 				pcp.partID.setText("Please enter part ID");
 				pcp.partDes.setText("Please enter part description");
 				pcp.setVisible(false);
 				spp.setVisible(true);
 			}
 		}
 		nb1.sendPartData(currentParts);
 		pip.setCurrentParts(currentParts);
 		pip.drawLabels();
 		pip.revalidate();
 		pip.repaint();
 	}
 
 	// -------------------------------------------------------------------------------------- //
 	// ----------------------------------- Network Manager ---------------------------------- //
 	// -------------------------------------------------------------------------------------- //
 		
 	// server specific
 	public void registerClientListener(NetworkBridge newBridge, int cID){}
 	public void syncFrame(){}
 	public void updatePartData(TreeMap<Integer, Parts>partData){
 		if (partData != null)
 			currentParts = partData;
 		else
 			currentParts = new TreeMap<Integer, Parts>();
 		initialize();
 		pcp.setVisible(false);
 		setSize(1000, 350);
 		setVisible(true);
 	}
 	public void updateKitData(TreeMap<Integer, Kits>kitData){}
 		
 	// client specific
 	public void mergeChanges(ArrayList<TreeMap<Integer, Boolean>> mapArray, ArrayList<TreeMap<Integer, FactoryObject>> dataArray){}
 	
 	public void syncChanges(ArrayList<TreeMap<Integer,FactoryObject>> dataArray){}
 		
 	// global
 	public void closeNetworkBridge(int bridgeID)
 	{
 				nb1.close();
 	}
 		
 	// -------------------------------------------------------------------------------------- //
 	// ----------------------------------- End Network Manager ------------------------------ //
 	// -------------------------------------------------------------------------------------- //
 }
