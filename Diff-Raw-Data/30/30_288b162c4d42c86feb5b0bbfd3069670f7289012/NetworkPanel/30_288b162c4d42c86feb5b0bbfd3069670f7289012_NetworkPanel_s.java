 package risk_assessment;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Point;
 import java.text.DecimalFormat;
 
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 public class NetworkPanel extends JPanel{
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	float network[];
 	boolean paintPointFlag = false;
 
 	public NetworkPanel() {
 		// TODO Auto-generated constructor stub
 		this.setBackground(Color.white);
 		this.setLayout(null);
 //		this.setOpaque(false);
 
 		
 		JLabel yaxleLabel = new JLabel("Network Risk");
 		yaxleLabel.setBounds(0, 0, 100, 20);
 		
 		this.add(yaxleLabel);
 	}
 	
 	void setNetworkValue(float newNetwork[])
 	{
 		network = newNetwork;
 	}
 	
 	protected void paintComponent(Graphics g)
 	{
 		super.paintComponent(g);
 		super.paintComponent(g);
 //		Graphics2D g2d = (Graphics2D) g;
 		
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
 		
 		if(paintPointFlag == false)
 		{
 			return;
 		}
 		
 		for (int i = 0; i < 23; i++) {
 			Point ptFrom = convertNetworkValuetoPoint(network[i], i);
 			Point ptTo = convertNetworkValuetoPoint(network[i+1], i+1);
 			g.drawLine(ptFrom.x,ptFrom.y,ptTo.x,ptTo.y);
 			g.drawString(new DecimalFormat("##.##").format(network[i]), ptFrom.x, ptFrom.y);
 		}
 		
 		
 	}
 	
 	Point convertNetworkValuetoPoint(float networkValue, int index)
 	{
 		Point pt = new Point();
 		pt.x = 20 + 18 * index;
 		pt.y = (int)(224 - networkValue * 200);
 		return pt;
 	}
 	
 	JPanel getPanel()
 	{
 		return this;
 	}
 	
 	void notPaintPointFlag()
 	{
 		paintPointFlag = !paintPointFlag;
 	}
 }
