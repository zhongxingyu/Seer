 import java.awt.Color;
 
 import javax.swing.JPanel;
 
 public class HistogrammValue extends JPanel {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 3324681790517352634L;
 	private int width = 3;
 	private int maxheight = 100;
 	
 	private int x;
 	private double val;
 	
 	public int getX() {
 		return x;
 	}
 
 	public void setX(int x) {
 		this.x = x;
 		this.setBounds(x*width,0,width,(int)val*maxheight);
 	}
 
 	public double getVal() {
 		return val;
 	}
 
 	public void setVal(double val) {
 		this.val = val;
 		this.setBounds(x*width,0,width,(int)val*maxheight);
 	}
 
 	public HistogrammValue(int x, double val){
 		this.x = x;
 		this.val = val;
 		
 		this.setBounds(x*width,(100-(int)(val*maxheight)),width,(int)(val*maxheight));
		if(x%2==0)
			this.setBackground(new Color(1f, 1f, 1f, 0.3f));
		else
			this.setBackground(new Color(0f, 1f, 0f, 0.3f));
 		this.setBorder(javax.swing.BorderFactory.createLineBorder(new Color(0.1f, 1f, 1f, 0.9f)));
 	}
 }
