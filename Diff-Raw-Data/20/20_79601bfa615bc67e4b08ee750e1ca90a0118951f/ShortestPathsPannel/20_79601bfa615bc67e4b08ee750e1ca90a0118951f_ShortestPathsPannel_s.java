 package ch.hszt.sp.views;
 
 import javax.swing.*;
 
 
 import ch.hszt.sp.models.CEdge;
 import ch.hszt.sp.models.CNode;
 
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.ArrayList;
 import java.util.Map;
 
 /**
  * @author Mirković Miroslav
  * @class ShortestPathsPannel
  * @since 27.04.11
  * @version 1.0
  * ShortestPathsPannel dient zur Darstellung der Nodes und der Edges in dieser Applikation.
  * 
  **/
 public class ShortestPathsPannel extends JPanel{
 
 	//private Image img;
 	private static final long serialVersionUID = 1L;
 	private ArrayList<CNode> cnlist;
 	//private ArrayList<CEdge> cEdge;
 	private LinkedList<CNode> uNode;
 	private Map<Integer, CEdge> ledg;
 	private Map<Integer, CNode> lnod;
 	
 	
 	public ShortestPathsPannel(List<CNode> cnlist, List<CEdge> cEdge, List<CNode> uNode, Map<Integer, CNode> lnode, Map<Integer, CEdge>ledge){
 		this.cnlist = (ArrayList<CNode>) cnlist;		
 		//this.cEdge = (ArrayList<CEdge>) cEdge;
 		this.uNode = (LinkedList<CNode>) uNode;
 		this.ledg = ledge;
 		this.lnod = lnode;
 	}
 
 	//paintComponent stellt die Komponenten auf dem Pannel dar.
 	public void paintComponent(Graphics g){
 		try {
 			for (CNode cnode : this.cnlist) {
 				ShowNode shn = new ShowNode(cnode.getxCoordinate(), cnode.getyCoordinate(), cnode.getId(), cnode.getName());
 				shn.paintNode(g);
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		ShowEdge she = new ShowEdge(lnod, ledg, uNode);
 		she.paintComponent(g);
 		}
 		
 	}
 	
 	/**
 	 * 
 	 * @author miro
 	 * @version 1.0
 	 * Darstellung der Nodes im Pannel
 	 **/
 	class ShowNode extends JPanel
 	{
 		private static final long serialVersionUID = 1L;
 		private int x, y, sizx, sizy;
 		private String nodeName;
 		
 		//Konstruktor erwartet die jeweilige Position der Knoten. Soll auch die Namen der Nodes anzeigen.
 		public ShowNode(int x, int y, int id, String nodeName){
 			this.x = x;
 			this.y = y;
 			sizx = 10; 
 			sizy = 10;
 			this.nodeName = nodeName;
 		}
 		//Hier werden die Knoten gezeichnet momentan Rot
 		public void paintNode(Graphics g){
 			g.setColor(Color.RED);
 			g.fillOval(x, y, sizx, sizy);
 			g.setColor(Color.black);
 			g.drawString(nodeName + ": ", x-25, y+11);
 		}
 	}
 	
 	class ShowEdge extends JPanel
 	{
 		/**
 		 * @author miro
 		 * @version 1.0
 		 * Die Klasse ShowEdge ist fuer die Dartsellung der Kanten zustaendig.
 		 * 
 		 * */
 		private static final long serialVersionUID = 1L;
 		private final Stroke stroke = new BasicStroke(2.0F);
 		//private int ax, ay, bx, by, weight;
 		private Map<Integer,CNode> lnod;
 		private Map<Integer,CEdge> ledg;
 		private LinkedList<CNode> unod;
 		
 		public ShowEdge(Map<Integer, CNode> lnod, Map<Integer, CEdge> ledg, LinkedList<CNode> unod){
 			this.lnod = lnod;
 			this.ledg = ledg;
 			this.unod = unod;
 		}
 		
 		public void paintComponent(Graphics g){
 			//System.out.println(unod.get(1));
 			Graphics2D g2d = (Graphics2D) g;
 			g2d.setStroke(stroke);
 	        g2d.setColor(Color.BLUE);
 	        Iterator<Integer> ite = ledg.keySet().iterator();
 	        while(ite.hasNext()) { 
 	        	Object ekey = ite.next(); 
 	        	CEdge eval = ledg.get(ekey);
 	        	g2d.drawLine(lnod.get(eval.getStartNode()).getxCoordinate()+5, lnod.get(eval.getStartNode()).getyCoordinate()+5, lnod.get(eval.getTargetNode()).getxCoordinate()+5,lnod.get(eval.getTargetNode()).getyCoordinate()+5);
 	        	}
 	        if(!unod.isEmpty()){
 	        	showUsedEdges(g);
 	        }
         	
 		}
 	    public void showUsedEdges(Graphics g){	
 	    	Graphics2D g2d = (Graphics2D) g;
 	    	g2d.setStroke(stroke);
 	    	g2d.setColor(Color.GREEN);
 	    	Iterator<Integer> ite = ledg.keySet().iterator();
 	    	CNode start;
 	    	CNode target;
 	    	while(ite.hasNext()){
 	    		Object key = ite.next();
 	    		CEdge eval = ledg.get(key);
 	    		
 	    		for(int i = 0; i < unod.size()-1; i++){
	    			for(int j = 1; j<unod.size(); j++){
	    				start = lnod.get(unod.get(i).getId());
			        	target = lnod.get(unod.get(j).getId());
	    				if(eval.getStartNode() == start.getId() && eval.getTargetNode() == target.getId()){
				        	g2d.drawLine(lnod.get(unod.get(i).getId()).getxCoordinate()+5, lnod.get(unod.get(i).getId()).getyCoordinate()+5, lnod.get(unod.get(j).getId()).getxCoordinate()+5, lnod.get(unod.get(j).getId()).getyCoordinate()+5);
			        	}
	    			}
 	    		}
 	    	}
         }
 }
 		
 
