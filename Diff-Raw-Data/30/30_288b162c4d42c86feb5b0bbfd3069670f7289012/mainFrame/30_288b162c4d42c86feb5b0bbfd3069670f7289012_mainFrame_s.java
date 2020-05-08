 package risk_assessment;
 
 import java.awt.Color;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.Random;
 
 import javax.swing.*;
 import javax.swing.border.Border;
 
 public class mainFrame {
 	static SubPanel sp1 = new SubPanel();
 	static SubPanel sp2 = new SubPanel();
 	static SubPanel sp3 = new SubPanel();
 	static SubPanel sp4 = new SubPanel();
 	static SubPanel sp5 = new SubPanel();
 	static NodePanel np = new NodePanel();
 	static NetworkPanel netp = new NetworkPanel();
 	static JCheckBox use_guard = new JCheckBox();
 	static JTextArea console = new JTextArea();
 	
 	static float node_1[] = new float[24];
 	static float node_2[] = new float[24];
 	static float node_3[] = new float[24];
 	static float node_4[] = new float[24];
 	static float node_5[] = new float[24];
 	static float network[] = new float[24];
 
 
 	public static void main(String[] args) {
 		final mainFrame mf = new mainFrame();
 		
 		//Preset value
 		sp1.node_weight_value.setText("3");
 		sp2.node_weight_value.setText("2");
 		sp3.node_weight_value.setText("4");
 		sp4.node_weight_value.setText("3");
 		sp5.node_weight_value.setText("5");
 		
 		sp1.FW.setText("0.25");
 		sp2.FW.setText("0.38");
 		sp3.FW.setText("0.33");
 		sp4.FW.setText("0.26");
 		sp5.FW.setText("0.47");
 		
 		sp1.IP.setText("0.24");
 		sp2.IP.setText("0.31");
 		sp3.IP.setText("0.35");
 		sp4.IP.setText("0.44");
 		sp5.IP.setText("0.24");
 		
 		sp1.AV.setText("0.22");
 		sp2.AV.setText("0.32");
 		sp3.AV.setText("0.55");
 		sp4.AV.setText("0.25");
 		sp5.AV.setText("0.23");
 		
 		sp1.SIT.setText("0.28");
 		sp2.SIT.setText("0.39");
 		sp3.SIT.setText("0.26");
 		sp4.SIT.setText("0.43");
 		sp5.SIT.setText("0.32");
 		
 		sp1.IMP.setText("3");
 		sp2.IMP.setText("3.5");
 		sp3.IMP.setText("2");
 		sp4.IMP.setText("3");
 		sp5.IMP.setText("4");
 		
 		sp1.SG.setText("1.6");
 		sp2.SG.setText("3.1");
 		sp3.SG.setText("1.3");
 		sp4.SG.setText("3.3");
 		sp5.SG.setText("5.6");
 		
 		SwingUtilities.invokeLater(new Runnable() {
 			public void run() {
 				mf.createAndShowGUI();
 			}
 		});
 
 	}
 
 	void createAndShowGUI() {
 		JFrame mainFrame = new JFrame();
 		mainFrame
 				.setTitle("LEO satellite communication network Risk Assessment");
 		mainFrame.setBounds(200, 100, 500, 620);
 		mainFrame.setResizable(false);
 		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		mainFrame.setVisible(true);
 		mainFrame.setLayout(null);
 
 		JTabbedPane tabPane = new JTabbedPane();
 		tabPane.setBounds(0, 0, mainFrame.getWidth(), 250);
 
 		tabPane.addTab("Node 1", sp1.getPanel());
 		tabPane.addTab("Node 2", sp2.getPanel());
 		tabPane.addTab("Node 3", sp3.getPanel());
 		tabPane.addTab("Node 4", sp4.getPanel());
 		tabPane.addTab("Node 5", sp5.getPanel());
 
 		JLabel guard_label = new JLabel("Security Guard");
 		guard_label.setBounds(20, 270, 100, 20);
 
 		use_guard.setBounds(110, 270, 25, 20);
 
 		JButton calculate = new JButton("Assess");
 		calculate.setBounds(300, 270, 100, 20);
 		calculate.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				Assess();
				np.notPaintPointFlag();
				netp.notPaintPointFlag();
 				drawNode();
				np.notPaintPointFlag();
 				drawNetwork();
				netp.notPaintPointFlag();
 			}
 		});
 		
 		JTabbedPane tabPanelBottom = new JTabbedPane();
 		tabPanelBottom.setBounds(0, 310, mainFrame.getWidth(), 290);
 		
 		console.setBounds(0, 310, 394, 260);
 		Border border = BorderFactory.createLineBorder(Color.black);
 		console.setBorder(border);
 		console.setEditable(false);
 
 		JScrollPane scroll = new JScrollPane(console);
 //		scroll.setBounds(0, 310, 394, 260);
 		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
 		scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
 		
 		tabPanelBottom.addTab("Risk Values", scroll);
 		tabPanelBottom.addTab("Node", np);
 		tabPanelBottom.addTab("Network", netp.getPanel());
 		
 		mainFrame.add(tabPane);
 		mainFrame.add(tabPanelBottom);
 		mainFrame.add(guard_label);
 		mainFrame.add(use_guard);
 		mainFrame.add(calculate);
 
 	}
 
 	void Assess() {
 		console.setText("");
 
 		float node1_weight = Float.parseFloat(sp1.node_weight_value.getText());
 		float node2_weight = Float.parseFloat(sp2.node_weight_value.getText());
 		float node3_weight = Float.parseFloat(sp3.node_weight_value.getText());
 		float node4_weight = Float.parseFloat(sp4.node_weight_value.getText());
 		float node5_weight = Float.parseFloat(sp5.node_weight_value.getText());
 
 		float node1_fw = Float.parseFloat(sp1.FW.getText());
 		float node2_fw = Float.parseFloat(sp2.FW.getText());
 		float node3_fw = Float.parseFloat(sp3.FW.getText());
 		float node4_fw = Float.parseFloat(sp4.FW.getText());
 		float node5_fw = Float.parseFloat(sp5.FW.getText());
 
 		float node1_ips = Float.parseFloat(sp1.IP.getText());
 		float node2_ips = Float.parseFloat(sp2.IP.getText());
 		float node3_ips = Float.parseFloat(sp3.IP.getText());
 		float node4_ips = Float.parseFloat(sp4.IP.getText());
 		float node5_ips = Float.parseFloat(sp5.IP.getText());
 
 		float node1_av = Float.parseFloat(sp1.AV.getText());
 		float node2_av = Float.parseFloat(sp2.AV.getText());
 		float node3_av = Float.parseFloat(sp3.AV.getText());
 		float node4_av = Float.parseFloat(sp4.AV.getText());
 		float node5_av = Float.parseFloat(sp5.AV.getText());
 
 		float node1_sit = Float.parseFloat(sp1.SIT.getText());
 		float node2_sit = Float.parseFloat(sp2.SIT.getText());
 		float node3_sit = Float.parseFloat(sp3.SIT.getText());
 		float node4_sit = Float.parseFloat(sp4.SIT.getText());
 		float node5_sit = Float.parseFloat(sp5.SIT.getText());
 
 		float node1_impact = Float.parseFloat(sp1.IMP.getText());
 		float node2_impact = Float.parseFloat(sp2.IMP.getText());
 		float node3_impact = Float.parseFloat(sp3.IMP.getText());
 		float node4_impact = Float.parseFloat(sp4.IMP.getText());
 		float node5_impact = Float.parseFloat(sp5.IMP.getText());
 
 		float node1_security = Float.parseFloat(sp1.SG.getText());
 		float node2_security = Float.parseFloat(sp2.SG.getText());
 		float node3_security = Float.parseFloat(sp3.SG.getText());
 		float node4_security = Float.parseFloat(sp4.SG.getText());
 		float node5_security = Float.parseFloat(sp5.SG.getText());
 
 		/*
 		//test value
 		float node1_weight = 0.3f;
 		float node2_weight = 0.4f;
 		float node3_weight = 0.34f;
 		float node4_weight = 0.34f;
 		float node5_weight = 0.23f;
 
 		float node1_fw = 0.3f;
 		float node2_fw = 0.5f;
 		float node3_fw = 0.3f;
 		float node4_fw = 0.4f;
 		float node5_fw = 0.3f;
 
 		float node1_ips = 0.4f;
 		float node2_ips = 0.45f;
 		float node3_ips = 0.34f;
 		float node4_ips = 0.45f;
 		float node5_ips = 0.34f;
 
 		float node1_av = 0.45f;
 		float node2_av = 0.23f;
 		float node3_av = 0.45f;
 		float node4_av = 0.34f;
 		float node5_av = 0.45f;
 
 		float node1_sit = 0.45f;
 		float node2_sit = 0.45f;
 		float node3_sit = 0.45f;
 		float node4_sit = 0.45f;
 		float node5_sit = 0.45f;
 
 		float node1_impact = 2f;
 		float node2_impact = 2f;
 		float node3_impact = 2f;
 		float node4_impact = 2f;
 		float node5_impact = 2f;
 
 		float node1_security = 1.5f;
 		float node2_security = 1.5f;
 		float node3_security = 1.5f;
 		float node4_security = 1.5f;
 		float node5_security = 1.5f;
 		
 		//
 		*/
 		
 		console.append("On the service layer:\n");
 		console.append("Calculating believe value...\n");
 		// Believe Function
 		float node1_m = believe(node1_fw, node1_ips, node1_av, node1_sit);
 		float node2_m = believe(node2_fw, node2_ips, node2_av, node2_sit);
 		float node3_m = believe(node3_fw, node3_ips, node3_av, node3_sit);
 		float node4_m = believe(node4_fw, node4_ips, node4_av, node4_sit);
 		float node5_m = believe(node5_fw, node5_ips, node5_av, node5_sit);
 	
 
 		
 		console.append("Believe value of service on satellite node 1 is: "
 				+ Float.toString(node1_m) + "\n");
 		console.append("Believe value of service on satellite node 2 is: "
 				+ Float.toString(node2_m) + "\n");
 		console.append("Believe value of service on satellite node 3 is: "
 				+ Float.toString(node3_m) + "\n");
 		console.append("Believe value of service on satellite node 4 is: "
 				+ Float.toString(node4_m) + "\n");
 		console.append("Believe value of service on satellite node 5 is: "
 				+ Float.toString(node5_m) + "\n");
 		console.append("\n");
 
 		console.append("On the node layer:\n");
 		console.append("Calculating risk value of service...\n");
 		boolean check = use_guard.isSelected();
 
 		float node1_service_val;
 		float node2_service_val;
 		float node3_service_val;
 		float node4_service_val;
 		float node5_service_val;
 		if (check) {
 			console.append("Security Guard enabled!\n");
 			node1_service_val = node1_m * node1_impact / node1_security;
 			node2_service_val = node2_m * node2_impact / node2_security;
 			node3_service_val = node3_m * node3_impact / node3_security;
 			node4_service_val = node4_m * node4_impact / node4_security;
 			node5_service_val = node5_m * node5_impact / node5_security;
 			
 			node_1[0] = node1_service_val;
 			node_2[0] = node2_service_val;
 			node_3[0] = node3_service_val;
 			node_4[0] = node4_service_val;
 			node_5[0] = node5_service_val;
 			
 			//Generate another 23 value
 			for(int index = 1; index < 24; index++)
 			{
 				node_1[index] = beliefValueFromRandomThread(node1_fw, node1_ips, node1_av, node1_sit) * node1_impact / node1_security;
 				node_2[index] = beliefValueFromRandomThread(node2_fw, node2_ips, node2_av, node2_sit) * node2_impact / node1_security;
 				node_3[index] = beliefValueFromRandomThread(node3_fw, node3_ips, node3_av, node3_sit) * node3_impact / node1_security;
 				node_4[index] = beliefValueFromRandomThread(node4_fw, node4_ips, node4_av, node4_sit) * node4_impact / node1_security;
 				node_5[index] = beliefValueFromRandomThread(node5_fw, node5_ips, node5_av, node5_sit) * node5_impact / node1_security;
 			}
 			
 		} else {
 			console.append("Security Guard disabled!\n");
 			node1_service_val = node1_m * node1_impact;
 			node2_service_val = node2_m * node2_impact;
 			node3_service_val = node3_m * node3_impact;
 			node4_service_val = node4_m * node4_impact;
 			node5_service_val = node5_m * node5_impact;
 			
 			node_1[0] = node1_service_val;
 			node_2[0] = node2_service_val;
 			node_3[0] = node3_service_val;
 			node_4[0] = node4_service_val;
 			node_5[0] = node5_service_val;
 			
 			//Generate another 23 value
 			for(int index = 1; index < 24; index++)
 			{
 				node_1[index] = beliefValueFromRandomThread(node1_fw, node1_ips, node1_av, node1_sit) * node1_impact;
 				node_2[index] = beliefValueFromRandomThread(node2_fw, node2_ips, node2_av, node2_sit) * node2_impact;
 				node_3[index] = beliefValueFromRandomThread(node3_fw, node3_ips, node3_av, node3_sit) * node3_impact;
 				node_4[index] = beliefValueFromRandomThread(node4_fw, node4_ips, node4_av, node4_sit) * node4_impact;
 				node_5[index] = beliefValueFromRandomThread(node5_fw, node5_ips, node5_av, node5_sit) * node5_impact;
 			}
 		}
 		
 		addTimeWeight(node_1);
 		addTimeWeight(node_2);
 		addTimeWeight(node_3);
 		addTimeWeight(node_4);
 		addTimeWeight(node_5);
 
 		console.append("Risk value of satellite node 1 is: "
 				+ Float.toString(node1_service_val) + "\n");
 		console.append("Risk value of satellite node 2 is: "
 				+ Float.toString(node2_service_val) + "\n");
 		console.append("Risk value of satellite node 3 is: "
 				+ Float.toString(node3_service_val) + "\n");
 		console.append("Risk value of satellite node 4 is: "
 				+ Float.toString(node4_service_val) + "\n");
 		console.append("Risk value of satellite node 5 is: "
 				+ Float.toString(node5_service_val) + "\n");
 		console.append("\n");
 
 		console.append("On the network layer:\n");
 		console.append("Normalizing...\n");
 		float base = normailize_base(node1_weight, node2_weight, node3_weight,
 				node4_weight, node5_weight);
 		float node1_w = node1_weight / base;
 		float node2_w = node2_weight / base;
 		float node3_w = node3_weight / base;
 		float node4_w = node4_weight / base;
 		float node5_w = node5_weight / base;
 		console.append("Normalized weight of node 1 is: "
 				+ Float.toString(node1_w) + "\n");
 		console.append("Normalized weight of node 2 is: "
 				+ Float.toString(node2_w) + "\n");
 		console.append("Normalized weight of node 3 is: "
 				+ Float.toString(node3_w) + "\n");
 		console.append("Normalized weight of node 4 is: "
 				+ Float.toString(node4_w) + "\n");
 		console.append("Normalized weight of node 5 is: "
 				+ Float.toString(node5_w) + "\n");
 
 		float risk_value = node1_w * node1_service_val + node2_w
 				* node2_service_val + node3_w * node3_service_val + node4_w
 				* node4_service_val + node5_w * node5_service_val;
 		console.append("Risk value of the whole network is: "
 				+ Float.toString(risk_value) + "\n");
 
 		float probability = 1 - (1 - node1_service_val)
 				* (1 - node2_service_val) * (1 - node3_service_val)
 				* (1 - node4_service_val) * (1 - node5_service_val);
 		console.append("Risk probability is: " + Float.toString(probability)
 				+ "\n");
 		console.append("************END*************");
 		
 		
 		//Generate random number of node risk values;
 		
 		network[0] = probability;
 		for(int index = 1; index < 24; index++)
 		{
 //			float riskv = node1_w * node_1[index] + node2_w * node_2[index] + node3_w * node_3[index] + node4_w * node_4[index] + node5_w * node_5[index];
 			network[index] = 1-(1-node_1[index])*(1-node_2[index])*(1-node_3[index])*(1-node_3[index])*(1-node_4[index]);
 		}
 	}
 	
 	void drawNode()
 	{
 		
 		np.setNodesValue(node_1, node_2, node_3, node_4, node_5);
 		np.repaint();
 		
 //		System.out.println(np.getWidth());
 //		System.out.println(np.getHeight());
 
 	}
 	
 	void drawNetwork()
 	{
 		netp.setNetworkValue(network);
 		netp.repaint();
 	}
 
 	float believe(float fw, float ips, float av, float sit) {
 		float multi = fw * ips * av * sit;
 		float result = multi
 				/ (multi + (1 - fw) * (1 - ips) * (1 - av) * (1 - sit));
 		return result;
 	}
 
 	float normailize_base(float node1, float node2, float node3, float node4,
 			float node5) {
 		float base = (float) Math.sqrt(node1 * node1 + node2 * node2 + node3
 				* node3 + node4 * node4 + node5 * node5);
 		return base;
 	}
 	
 	float beliefValueFromRandomThread(float fw, float ips, float av, float sit)
 	{
 		Random rand = new Random();
 		fw = (rand.nextFloat()-0.5f)/5.0f + fw;
 		ips = (rand.nextFloat()-0.5f)/5.0f + ips;
 		av = (rand.nextFloat()-0.5f)/5.0f + av;
 		sit = (rand.nextFloat()-0.5f)/5.0f + sit;
 		
 		return believe(fw, ips, av, sit); 
 	}
 	
 	void addTimeWeight(float node[])
 	{
 		for (int i = 0; i < 24; i++) {
 			if(i < 9 || i > 18)
 			{
 				node[i] = node[i] * 0.4f;
 			}
 		}
 	}
 }
