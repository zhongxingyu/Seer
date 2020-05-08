 
 package All;
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.RenderingHints;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.util.ArrayList;
 import java.util.TreeMap;
 import java.util.TreeSet;
 
 import javax.swing.GroupLayout;
 import javax.swing.JButton;
 import javax.swing.JFileChooser;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JRadioButton;
 import javax.swing.JToggleButton;
 import javax.swing.SwingUtilities;
 
 import AttributePanel.AttributesPanel;
 import AttributePanel.MachineAttributePanel;
 import AttributePanel.NullAttributePanel;
 import AttributePanel.PrimaryAttributePanel;
 import AttributePanel.VisualizeAttributePanel;
 import ConfigurationMaker.ModuleMaker;
 import ConfigurationMaker.NodeMaker;
 import ConfigurationMaker.PrimaryMaker;
 import Data.Edge;
 import Data.Event;
 import Data.Interval;
 import Data.Machine;
 import Data.Node;
 import Data.Primary;
 
 
 public class DrawingPanel extends JPanel implements MouseMotionListener, MouseListener{
 	public UI ui;
 	public DrawingPanel drawingPanel;
 	public int selectedIndex = -1;
 	public int fromNode = -1, toNode = -1;
 	
 	public ArrayList<Node> listOfNodes;
 	public Node source, destination;
 	public int offX, offY;
 	
 	public JButton setSourceButton, setDestinationButton, generate;
 	public JToggleButton visualizeButton;
 	public StatisticsCollector sc;
 	
 	public int maxRange;
 	public DrawingPanel(UI ui_){
 		
 		this.drawingPanel = this;
 		this.ui = ui_;
 		
 		listOfNodes = new ArrayList<Node>();
 		
 		Node node1 = new Machine("1", 100, 100);
 		Node node2 = new Machine("2", 200, 200);
		Node node3 = new Machine("3", 300, 300);
 		
 		node1.WLS_HW.add("08:11:96:8B:84:F4");
 		node1.WLS_Name.add("wlan0");
 		node2.WLS_HW.add("08:11:96:8B:84:F3");
 		node2.WLS_Name.add("wlan1");
		node3.WLS_HW.add("08:11:96:8B:84:F2");
		node3.WLS_Name.add("wlan1");
 		
 		listOfNodes.add(node1);
 		listOfNodes.add(node2);
		listOfNodes.add(node3);
 		
 		addMouseListener(this);
 		addMouseMotionListener(this);
 		
 		setSourceButton = new JButton("Set Source");
 		setDestinationButton = new JButton("Set Dest.");
 		
 		setSourceButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				if(selectedIndex != -1 && listOfNodes.get(selectedIndex) instanceof Machine){
 					if(source != null)source.isSource = false;
 					
 					source = listOfNodes.get(selectedIndex);
 					source.isSource = true;
 					repaint();
 				}
 			}
 		});
 		add(setSourceButton);
 		
 		setDestinationButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				if(selectedIndex != -1 && listOfNodes.get(selectedIndex) instanceof Machine){
 					if(destination != null)destination.isDestination = false;
 					
 					destination = listOfNodes.get(selectedIndex);
 					destination.isDestination = true;
 					repaint();
 				}
 			}
 		});
 		add(setDestinationButton);
 		
 
 		generate = new JButton("Generate");
 		generate.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				if(source == null){
 					JOptionPane.showMessageDialog(drawingPanel, "Missing source node.");
 					return;
 				}
 				
 				if(destination == null){
 					JOptionPane.showMessageDialog(drawingPanel, "Missing destination node.");
 					return;
 				}
 				
 				final JFileChooser fc = new JFileChooser();
 				
 				int ret = fc.showDialog(drawingPanel, "Choose the source template");
 				if(ret != JFileChooser.APPROVE_OPTION){
 					JOptionPane.showMessageDialog(drawingPanel, "Missing source template.");
 					return;
 				}
 				String sourceTemplate = fc.getSelectedFile().getAbsolutePath();
 				
 				ret = fc.showDialog(drawingPanel, "Choose the destination template");
 				if(ret != JFileChooser.APPROVE_OPTION){
 					JOptionPane.showMessageDialog(drawingPanel, "Missing destination template.");
 					return;
 				}
 				String destinationTemplate = fc.getSelectedFile().getAbsolutePath();
 				
 				ret = fc.showDialog(drawingPanel, "Choose the hop template");
 				if(ret != JFileChooser.APPROVE_OPTION){
 					JOptionPane.showMessageDialog(drawingPanel, "Missing hop template.");
 					return;
 				}
 				String hopTemplate = fc.getSelectedFile().getAbsolutePath();
 				
 				ret = fc.showDialog(drawingPanel, "Choose the primary template");
 				if(ret != JFileChooser.APPROVE_OPTION){
 					JOptionPane.showMessageDialog(drawingPanel, "Missing primary template.");
 					return;
 				}
 				String primaryTemplate = fc.getSelectedFile().getAbsolutePath();
 				
 				NodeMaker nodeMaker = new NodeMaker();
 				PrimaryMaker primaryMaker = new PrimaryMaker();
 				ModuleMaker moduleMaker = new ModuleMaker();
 				
 				//Generate the files from templates.
 				try{
 					//Generate the files for all the nodes.
 					for(Node node : listOfNodes){
 						//Generate the configuration files for this node.
 						if(node instanceof Primary){
 							primaryMaker.parseTemplateFile(primaryTemplate, (Primary)node);
 						} else {
 							Node[] adjacentNodes = new Node[node.adjacent.size()];
 							for(int i = 0; i < node.adjacent.size(); i++)
 								adjacentNodes[i] = node.adjacent.get(i).to;
 							if(node == source){//Special treatment for the source node.
 								nodeMaker.parseTemplateFile(sourceTemplate, source, destination, node, adjacentNodes);
 							}else if(node == destination){//Special treatment for the destination node.
 								nodeMaker.parseTemplateFile(destinationTemplate, source, destination, node, adjacentNodes);
 							}else{//Rest of the nodes treated as hops.
 								nodeMaker.parseTemplateFile(hopTemplate, source, destination, node, adjacentNodes);
 							}
 						}
 						
 						//Generate
 						moduleMaker.generateModuleFile(node, listOfNodes, ui.ccc);
 					}
 				}catch(Exception ex){
 					ex.printStackTrace();
 					JOptionPane.showMessageDialog(drawingPanel, "Error generating nodes files.");
 					return;
 				}
 				
 			}
 		});
 		add(generate);
 		
 		visualizeButton = new JToggleButton("Visualize");
 		visualizeButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				if(!visualizeButton.isSelected()){
 					ui.attributesPanel.informationPanelCardLayout.show(ui.attributesPanel.informationPanel, Constants.nullAPCode);
 					ui.attributesPanel.deactiveVisualization();
 					return;
 				}
 				
 				ui.attributesPanel.informationPanelCardLayout.show(ui.attributesPanel.informationPanel, Constants.visualizeAPCode);
 				ui.attributesPanel.activeVisualization();
 				
 				sc = new StatisticsCollector(listOfNodes);
 				
 				for(Node node : listOfNodes){
 					JFileChooser fc = new JFileChooser();
 					int ret = fc.showDialog(drawingPanel, String.format("Choose Node:(%s) statistics file.", node.name));
 					if(ret != JFileChooser.APPROVE_OPTION){
 						JOptionPane.showMessageDialog(drawingPanel, "Missing statistics file.");
 						return;
 					}
 					
 					String statisticsFile = fc.getSelectedFile().getAbsolutePath();
 					
 					sc.parse(statisticsFile);
 				}
 				
 				sc.calculate();
 				maxRange = (int) (sc.maxTimestamp - sc.minTimestamp);
 				ui.attributesPanel.visualizationPanel.ticker.setMaximum(maxRange);
 				ui.attributesPanel.visualizationPanel.ticker.setValue(0);
 				
 				ui.attributesPanel.visualizationPanel.ticker.setMajorTickSpacing(maxRange/5);
 				ui.attributesPanel.visualizationPanel.ticker.setMinorTickSpacing(maxRange/50); 
 				ui.attributesPanel.visualizationPanel.ticker.setPaintTicks(true);    
 				ui.attributesPanel.visualizationPanel.ticker.setPaintLabels(true);   
 				
 				
 			}
 		});
 		add(visualizeButton);
 	}
 	
 	@Override
 	public void paint(Graphics g) {
 		super.paint(g);
 		
 		Graphics2D g2d = (Graphics2D) g;
 		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
 				RenderingHints.VALUE_ANTIALIAS_ON);
 		
 		float dash[] = { 10.0f };
 		g2d.setStroke(new BasicStroke(0.1f, BasicStroke.CAP_BUTT,
 			        BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f));
 		
 		//Vertical.
 		for(int d = 0; d < 19; d++)
 			g2d.drawLine(15 + d * 20, 85, 15 + d * 20, 620);
 		
 		//Horizental.
 		for(int d = 1; d < 28; d++)
 			g2d.drawLine(10, 70 + d * 20, 390, 70 + d * 20);
 		
 		//Legend box.
         g2d.drawString("Source", 45, 635);
         g2d.drawString("Destination", 175, 635);
         g2d.drawString("Selected", 315, 635);
         g2d.setColor(Constants.SOURCE_COLOR);
         g2d.fillRect(15, 621, 25, 20);
         g2d.setColor(Constants.DEST_COLOR);
         g2d.fillRect(145, 621, 25, 20);
         g2d.setColor(Constants.SELECTED_COLOR);
         g2d.fillRect(285, 621, 25, 20);
 
 		g2d.setStroke(new BasicStroke(1));
 		
 		String drawingOption = visualizeButton.isSelected() ? ui.attributesPanel.visualizationPanel.visualiztionOptions.getSelection().getActionCommand() : "Init";
 		
 		for(Node node : listOfNodes){
 			//Draw the Node itself.
 			node.draw(g2d, this, drawingOption);
 			
 			//Draw all the edges.
 			for(Edge edge : node.adjacent){
 				edge.draw(g2d, this, drawingOption);
 			}
 		}
 		
 		//Draw simulation of the packets.
 		if(!drawingOption.equals("Init")){
 			long currentTime = ui.attributesPanel.visualizationPanel.currentSimulationTime;
 			System.out.println("Current Simulation time: " + currentTime);
 			for(Interval interval : sc.timeline){
 				if(interval.fromTime <= currentTime && currentTime <= interval.toTime){
 					int fromX = interval.fromNode.x;
 					int fromY = interval.fromNode.y;
 					
 					int toX = interval.toNode.x;
 					int toY = interval.toNode.y;
 					
 					int dX = toX - fromX;
 					int dY = toY - fromY;
 					
 					long currentDisp = currentTime - interval.fromTime;
 					long totalTime = interval.toTime - interval.fromTime;
 	
 					int packetX = (int) (fromX + (currentDisp * 1.0 / totalTime) * dX);
 					int packetY = (int) (fromY + (currentDisp * 1.0 / totalTime) * dY);
 					
 					System.out.println("Current Displacement: " + currentDisp);
 					System.out.printf("Packet position: (%d, %d)\n", packetX, packetY);
 					
 					g2d.setColor(Constants.SELECTED_COLOR);
 					g2d.fillOval(packetX + 5, packetY + 5, 10, 10);
 					
 					g2d.setColor(Color.BLACK);
 					g2d.drawOval(packetX + 5, packetY + 5, 10, 10);
 				}
 			}
 		}
 	}
 
 	@Override
 	public void mouseDragged(MouseEvent mouse) {
 		if(visualizeButton.isSelected())return;
 		
 		if(selectedIndex != -1){
 			listOfNodes.get(selectedIndex).x = cap(mouse.getX() - offX, 70, 300);
 			listOfNodes.get(selectedIndex).y = cap(mouse.getY() - offY, 120, 550);
 		}
 		repaint();
 	}
 
 	private int cap(int i, int min, int max) {
 		return Math.max(min, Math.min(i, max));
 	}
 
 	@Override
 	public void mouseMoved(MouseEvent arg0) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void mouseClicked(MouseEvent e) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void mouseEntered(MouseEvent e) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void mouseExited(MouseEvent e) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void mousePressed(MouseEvent mouse) {
 //		if(visualizeButton.isSelected())return;
 		
 		selectedIndex = -1;
 		if(SwingUtilities.isRightMouseButton(mouse)){
 			fromNode = getNodeIndex(mouse);
 		}else{
 			if(mouse.getClickCount() == 1){
 				selectedIndex = getNodeIndex(mouse);
 				if(selectedIndex != -1){
 					offX = mouse.getX() - listOfNodes.get(selectedIndex).x;
 					offY = mouse.getY() - listOfNodes.get(selectedIndex).y;
 				}
 			}else if(mouse.getClickCount() == 2){
 				if(getNodeIndex(mouse) == -1){
 					String[] options = new String[]{"Machine", "Primary"};
 					int type = JOptionPane.showOptionDialog(null, "Choose type of the node", "", 1, 2, null, options, options[0]);
 					
 					System.out.println("Creating and adding a new node");
 
 					Node node;
 					if(type == 0){
 						//Create a node with a random name.
 						node = new Machine((int)(Math.random() * 100) + "", cap(mouse.getX() - 10, 70, 300), cap(mouse.getY() - 10, 120, 550));
 					}else if(type == 1){
 						//Create a primary user with a random name.
 						node = new Primary((int)(Math.random() * 100) + "", cap(mouse.getX() - 10, 70, 300), cap(mouse.getY() - 10, 120, 550));
 					}else{
 						JOptionPane.showMessageDialog(null, "Not a correct node type.");
 						return;
 					}
 					listOfNodes.add(node);
 					
 					System.out.println("Successfully created the node:\n" + node);
 				}else{
 					int result = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this node?");
 					
 					if(result == 0){//Yes delete the node.
 						Node toDeleteNode = listOfNodes.get(getNodeIndex(mouse));
 						
 						listOfNodes.remove(toDeleteNode);
 						
 						for(Node rest : listOfNodes)
 							rest.adjacent.remove(toDeleteNode);
 						
 						System.out.println("Successfully deleted the node:\n" + toDeleteNode);
 					}
 				}
 			}
 		}
 		repaint();
 	}
 
 	@Override
 	public void mouseReleased(MouseEvent mouse) {
 //		if(visualizeButton.isSelected())return;
 		
 		if(SwingUtilities.isRightMouseButton(mouse)){
 			toNode = getNodeIndex(mouse);
 			
 			if(fromNode != -1 && toNode != -1 && fromNode != toNode){
 				if(listOfNodes.get(fromNode).adjacent.contains(new Edge(listOfNodes.get(fromNode), listOfNodes.get(toNode)))){
 					int result = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this edge?");
 					
 					if(result == 0){//Yes delete the edge.
 						System.out.println("Removing the edge between " + fromNode + " -> " + toNode);
 						
 						listOfNodes.get(fromNode).adjacent.remove(new Edge(listOfNodes.get(fromNode), listOfNodes.get(toNode)));
 						listOfNodes.get(toNode).adjacent.remove(new Edge(listOfNodes.get(toNode), listOfNodes.get(fromNode)));
 					}
 					
 				}else{
 					System.out.println("Adding a new edge between " + fromNode + " -> " + toNode);
 					
 					Edge edge1 = new Edge(listOfNodes.get(fromNode), listOfNodes.get(toNode));
 					Edge edge2 = new Edge(listOfNodes.get(toNode), listOfNodes.get(fromNode));
 					
 					listOfNodes.get(fromNode).addAdjacentEdge(edge1);
 					listOfNodes.get(toNode).addAdjacentEdge(edge2);
 				}
 			}
 		}else{
 			if(selectedIndex != -1){
 				if(listOfNodes.get(selectedIndex) instanceof Machine){
 					ui.attributesPanel.machineAP.setInfo((Machine)listOfNodes.get(selectedIndex));
 					ui.attributesPanel.informationPanelCardLayout.show(ui.attributesPanel.informationPanel, Constants.machineAPCode);
 				}else if(listOfNodes.get(selectedIndex) instanceof Primary){
 					ui.attributesPanel.primaryAP.setInfo((Primary)listOfNodes.get(selectedIndex));
 					ui.attributesPanel.informationPanelCardLayout.show(ui.attributesPanel.informationPanel, Constants.primaryAPCode);
 				}else{
 					System.out.println("Unidentified node type.");
 				}
 			}else{
 				ui.attributesPanel.informationPanelCardLayout.show(ui.attributesPanel.informationPanel, Constants.nullAPCode);
 			}
 		}
 		repaint();
 	}
 	
 	private int getNodeIndex(MouseEvent mouse){
 		for(int i = 0; i < listOfNodes.size(); i++){
 			Node node = listOfNodes.get(i);
 			
 			if(node.x <= mouse.getX() && mouse.getX() <= node.x + 20 &&
 			   node.y <= mouse.getY() && mouse.getY() <= node.y + 20){
 				return i;
 			}
 		}
 		return -1;
 	}
 	
 }
