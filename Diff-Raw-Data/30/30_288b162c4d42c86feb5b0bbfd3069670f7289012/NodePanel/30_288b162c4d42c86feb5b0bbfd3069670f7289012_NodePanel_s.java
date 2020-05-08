 package risk_assessment;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Point;
 
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 public class NodePanel extends JPanel{
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	float node_1[];
 	float node_2[];
 	float node_3[];
 	float node_4[];
 	float node_5[];
 	
 	boolean paintPointFlag = false;
 	
 	public NodePanel() {
 		// TODO Auto-generated constructor stub
 		this.setLayout(null);
 		this.setBackground(Color.white);
 //		this.setOpaque(false);
 
 		
 		JLabel yaxleLabel = new JLabel("Risk Value");
 		yaxleLabel.setBounds(0, 0, 70, 20);
 		
 		JLabel node1 = new JLabel("Node 1");
 		node1.setBounds(101, 0, 50, 20);
 		
 		JLabel node2 = new JLabel("Node 2");
 		node2.setBounds(181, 0, 50, 20);
 		
 		JLabel node3 = new JLabel("Node 3");
 		node3.setBounds(261, 0, 50, 20);
 		
 		JLabel node4 = new JLabel("Node 4");
 		node4.setBounds(341, 0, 50, 20);
 		
 		JLabel node5 = new JLabel("Node 5");
 		node5.setBounds(421, 0, 50, 20);
 		
 		this.add(yaxleLabel);
 		this.add(node1);
 		this.add(node2);
 		this.add(node3);
 		this.add(node4);
 		this.add(node5);
 	}
 	
 	void setNodesValue(float newNode_1[], float newNode_2[], float newNode_3[], float newNode_4[], float newNode_5[])
 	{
 		node_1 = newNode_1;
 		node_2 = newNode_2;
 		node_3 = newNode_3;
 		node_4 = newNode_4;
 		node_5 = newNode_5;
 	}
 	
 	protected void paintComponent(Graphics g)
 	{
 		super.paintComponent(g);
 		Graphics2D g2d = (Graphics2D) g;
 		
 		g.setColor(Color.green);
 		g2d.drawRect(80, 8, 20, 4);
 		g2d.fillRect(80, 8, 20, 4);
 
 		g.setColor(Color.blue);
 		g2d.drawRect(160, 8, 20, 4);
 		g2d.fillRect(160, 8, 20, 4);
 		
 		g.setColor(Color.red);
 		g2d.drawRect(240, 8, 20, 4);
 		g2d.fillRect(240, 8, 20, 4);
 		
 		g.setColor(Color.orange);
 		g2d.drawRect(320, 8, 20, 4);
 		g2d.fillRect(320, 8, 20, 4);
 		
 		g.setColor(Color.darkGray);
 		g2d.drawRect(400, 8, 20, 4);
 		g2d.fillRect(400, 8, 20, 4);
 		
 		//draw y axle
 		int panelHeight = this.getHeight();
 		g.setColor(Color.black);
 		g.drawLine(20, 20, 20, panelHeight-20);
 		//draw x axle
 		g.drawLine(20, panelHeight-20, 459, panelHeight-20);
 		
 		//draw y axle point
 		int x_ori = 20;
 		int y_ori = panelHeight-20;
 		for (int i = 0; i <= 10; i++) {
 			g.drawLine(x_ori, y_ori, x_ori+5, y_ori);
 			g.drawString(Float.toString(i/10f), 0, y_ori+5);
 			y_ori -= 20;
 		}
 		
 		//draw x axle point
 		x_ori = 20;
 		y_ori = panelHeight-20;
 		for (int i = 0; i < 24; i++) {
 			g.drawLine(x_ori, y_ori, x_ori, y_ori-5);
 			g.drawString(Integer.toString(i), x_ori-5, y_ori+14);
 			x_ori += 18;
 		}
 		g.drawString("h", x_ori-5, y_ori+14);
 		
 		//depict point
 		if(paintPointFlag == false)
 		{
 			return;
 		}
 		
 		g.setColor(Color.green);
 		for (int i = 0; i < 23; i++) {
 			Point ptFrom = convertNodetoPoint(node_1[i], i);
 			Point ptTo = convertNodetoPoint(node_1[i+1], i+1);
 			g.drawLine(ptFrom.x,ptFrom.y,ptTo.x,ptTo.y);
 		}
 		
 		g.setColor(Color.blue);
 		for (int i = 0; i < 23; i++) {
 			Point ptFrom = convertNodetoPoint(node_2[i], i);
 			Point ptTo = convertNodetoPoint(node_2[i+1], i+1);
 			g.drawLine(ptFrom.x,ptFrom.y,ptTo.x,ptTo.y);
 		}
 		
 		g.setColor(Color.red);
 		for (int i = 0; i < 23; i++) {
 			Point ptFrom = convertNodetoPoint(node_3[i], i);
 			Point ptTo = convertNodetoPoint(node_3[i+1], i+1);
 			g.drawLine(ptFrom.x,ptFrom.y,ptTo.x,ptTo.y);
 		}
 		
 		g.setColor(Color.orange);
 		for (int i = 0; i < 23; i++) {
 			Point ptFrom = convertNodetoPoint(node_4[i], i);
 			Point ptTo = convertNodetoPoint(node_4[i+1], i+1);
 			g.drawLine(ptFrom.x,ptFrom.y,ptTo.x,ptTo.y);
 		}
 		
 		g.setColor(Color.darkGray);
 		for (int i = 0; i < 23; i++) {
 			Point ptFrom = convertNodetoPoint(node_5[i], i);
 			Point ptTo = convertNodetoPoint(node_5[i+1], i+1);
 			g.drawLine(ptFrom.x,ptFrom.y,ptTo.x,ptTo.y);
 		}
 		
 	}
 	
 	Point convertNodetoPoint(float node_value, int index)
 	{
 		Point pt = new Point();
 //		pt.setLocation(20+18*index, node_value * 200 + 20);
 		pt.x = 20 + 18 * index;
 		pt.y = (int)(224 - node_value * 200);
 		return pt;
 	}
 	
 	JPanel getPanel()
 	{
 		return this.getPanel();
 	}
 	
 	void notPaintPointFlag()
 	{
 		paintPointFlag = !paintPointFlag;
 	}
 }
