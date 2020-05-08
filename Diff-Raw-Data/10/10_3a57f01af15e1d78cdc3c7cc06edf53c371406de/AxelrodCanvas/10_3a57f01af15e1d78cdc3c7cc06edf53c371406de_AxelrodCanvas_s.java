 /**
  * 
  */
 package br.edu.axelrod;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Insets;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import javax.swing.JPanel;
 
 /**
  * @author muggler
  * 
  */
 
 public class AxelrodCanvas extends JPanel {
 
 	private static final long serialVersionUID = -5262909296764901151L;
 
 	private final int siteWidth;
 	private final int canvasWidth;
 	private final RgbPartitioner rp; // O(335.5) MB - see RgbPartitioner.java
 	private final AxelrodNetwork nw;
 
 	protected Insets thickness;
 	protected Color lineColor;
 	protected Insets gap;
 	private final boolean borders;
 	
 	public int[] stateStroke;
 	
 	public final List<int[]> dirtySites = Collections.synchronizedList(new ArrayList<int[]>());
 
 	public AxelrodCanvas(int canvasWidth, AxelrodNetwork nw, boolean borders) {
 		super();
 		this.nw = nw;
 		this.canvasWidth = canvasWidth - (canvasWidth % this.nw.size);
 		this.siteWidth = (int) canvasWidth / this.nw.size;
 		
 		this.rp = new RgbPartitioner(nw.features, nw.traits);
 		this.borders = borders;
 		
 		this.stateStroke = new int[nw.features];
 		for (int i = 0; i < stateStroke.length; i++) {
 			stateStroke[i] = 0;
 		}
 		
 		this.addMouseListener(stateInspector);
 		this.addMouseListener(statePen);
 		this.addMouseListener(stateRect);
 		
 		this.setPreferredSize(new Dimension(this.canvasWidth, this.canvasWidth));
 		
 		lineColor = new Color(0,0,0);
 		this.thickness = new Insets(1, 1, 1, 1);
 		this.gap = new Insets(1, 1, 1, 1);
 
 	}
 	
 	public void paintComponent(Graphics g){
 		for (int nd = 0; nd < nw.n_nodes; nd++) {
 			int[] site = {nd / nw.size, nd % nw.size};
 			int node = site[0] * nw.size + site[1];
 			int y = site[0] * this.siteWidth;
 			int x = site[1] * this.siteWidth;
 			int color = this.rp.color(this.nw.states[node]);
 			g.setColor(new Color(color));
 			g.fillRect(x, y, this.siteWidth, this.siteWidth);
 			if(borders){
 				this.paintBorder(g, x, y, this.siteWidth, this.siteWidth);
 			}
 		}
 	}
 
 	public void paintBorder( Graphics g, int x, int y, int width,
 			int height) {
 		Color oldColor = g.getColor();
 
 		g.setColor(lineColor);
 		// top
 		for (int i = 0; i < thickness.top; i++) {
 			g.drawLine(x, y + i, x + width, y + i);
 		}
 		// bottom
 		for (int i = 0; i < thickness.bottom; i++) {
 			g.drawLine(x, y + height - i - 1, x + width, y + height - i - 1);
 		}
 		// right
 		for (int i = 0; i < thickness.right; i++) {
 			g.drawLine(x + width - i - 1, y, x + width - i - 1, y + height);
 		}
 		// left
 		for (int i = 0; i < thickness.left; i++) {
 			g.drawLine(x + i, y, x + i, y + height);
 		}
 		g.setColor(oldColor);
 	}
 
 	/**
 	 * @return the canvasWidth
 	 */
 	public int getCanvasWidth() {
 		return canvasWidth;
 	}
 	
 	private MouseListener stateInspector = new MouseListener() {
 		public void mouseClicked(MouseEvent e) {
 			if(e.getButton() == MouseEvent.BUTTON1){
 				int node = getClickedNode(e);
 				System.arraycopy(AxelrodCanvas.this.nw.states[node], 0, stateStroke, 0, nw.features);
 				int[] coords = networkCoordsForClick(e);
 				String out = String.format("node: %d, %d, state %s", coords[0], coords[1], State.toString(AxelrodCanvas.this.nw.states[node]));
 				System.out.println(out);
 	//			AxelrodCanvas.this.setToolTipText(out);
 			}
 		}
 		public void mouseEntered(MouseEvent e) {}
 		public void mouseExited(MouseEvent e) {}
 		public void mousePressed(MouseEvent e) {}
 		public void mouseReleased(MouseEvent e) {}
 	};
 	
 	private MouseListener statePen = new MouseListener() {
 		public void mouseClicked(MouseEvent e) {
 			if(e.getButton() == MouseEvent.BUTTON3){
 				int node = getClickedNode(e);
 				setNwState(node, stateStroke);
 			}
 		}
 		public void mouseEntered(MouseEvent e) {}
 		public void mouseExited(MouseEvent e) {}
 		public void mousePressed(MouseEvent e) {}
 		public void mouseReleased(MouseEvent e) {}
 	};
 	
 	private MouseListener stateRect = new MouseListener() {
 		int[] dragStarted;
 		int[] dragEnded;
 		public void mouseClicked(MouseEvent e) {}
 		public void mouseEntered(MouseEvent e) {}
 		public void mouseExited(MouseEvent e) {}
 		public void mousePressed(MouseEvent e) {
 			dragStarted = networkCoordsForClick(e);
 		}
 		public void mouseReleased(MouseEvent e) {
 			dragEnded = networkCoordsForClick(e);
 			int startX, startY, endX, endY;
 			if(dragStarted[0] < dragEnded[0]){
 				startX = dragStarted[0];
 				endX = dragEnded[0];
 			}else{
 				endX = dragStarted[0];
 				startX = dragEnded[0];
 			}
 			if(dragStarted[1] < dragEnded[1]){
 				startY = dragStarted[1];
 				endY = dragEnded[1];
 			}else{
 				endY = dragStarted[1];
 				startY = dragEnded[1];
 			}
			for (int i = startX; i <= endX; i++) {
				for (int j = startY; j <= endY; j++) {
					if(i < nw.size && j < nw.size){
						setNwState(i*nw.size + j, stateStroke);
 					}
 				}
 			}
 		}
 	};
 	
 	private void setNwState(int node, int[] state){
 		System.arraycopy(state, 0, nw.states[node], 0, nw.features);
 		nw.update_representations(node);
 		AxelrodCanvas.this.repaint();
 	}
 	
 	private int getClickedNode(MouseEvent e) {
 		int mouseX = e.getX();
 		int mouseY = e.getY();
 		int i = (mouseY / AxelrodCanvas.this.siteWidth);
 		int j = (mouseX / AxelrodCanvas.this.siteWidth);
 		int node = i*AxelrodCanvas.this.nw.size+ j;
 		return node;
 	}
 	
 	private int[] networkCoordsForClick(MouseEvent e){
 		int[] coords = {e.getY() / AxelrodCanvas.this.siteWidth, e.getX() / AxelrodCanvas.this.siteWidth};
 		return coords;
 	}
 }
