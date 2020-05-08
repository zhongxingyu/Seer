 package haw.po.la.cliff;
 
 /**
  * 
  * @author Fenja Harbke
  *
  */
 
 import java.awt.Color;
 import java.awt.Frame;
 import java.awt.Graphics;
 import java.applet.Applet;
 import java.util.List;
 
 @SuppressWarnings("serial")
 public class GuiImpl extends Applet implements Gui{
 	
 	private int startx = 30;
 	private int starty = 10;
 	private int fieldSize = 30;
	private static Graphics g;
 	private Frame frame;
 	
 
 	//interface environment
 	public int width;
 	public int height;
 	public Position startField;
 	public Position endField;
 	public List<Position> cliffList;
 	public Position agentPos;
 	
 	public GuiImpl(EnvironmentImpl env){
 		g = getGraphics();
 		this.startField = env.getStartPosition();
 		this.endField = env.getFinishPosition();
 		this.cliffList = env.getCliffPositions();
 		this.width = env.getWidth();
 		this.height = env.getHeigth();
 		this.agentPos = startField;
 		frame = new Frame();
 		frame.setResizable(true);
 		frame.add(this);
 		frame.pack();
 		frame.setSize(400,400);
 		init();
 		frame.setVisible(true);	
 	}
 	
 	public void init() {
 		g = getGraphics();
 	}
 	
 	public void paint (Graphics g){
 		this.g= g; //Damits bei MAC funzt?
 		
 		//Grid
 		g.setColor(Color.black);
 		g.drawLine(startx, starty, startx, starty+height*(fieldSize+1)); //waagerecht
 		g.drawLine(startx, starty, startx+width*(fieldSize+1), starty); //senkrecht
 		for(int i = 1; i <= height; i++){
 			g.drawLine(startx, starty+((fieldSize+1)*i), startx+width*(fieldSize+1), starty+((fieldSize+1)*i));
 		}
 		for(int i = 1; i <= width; i++){
 			g.drawLine(startx+((fieldSize+1)*i), starty, startx+((fieldSize+1)*i), starty+height*((fieldSize+1)));
 		}
 		
 		//start & end field
 		fillField(startField, Color.lightGray);
 		fillField(endField, Color.lightGray);
 		
 		//cliff
 		if(cliffList!=null){
 			for(Position p : cliffList){
 				fillField(p,Color.black);
 			}
 		}
 		
 		drawAgent();
 	}
 
 	private void drawAgent(){
 		g.setColor(Color.red);
 		g.fillOval(startx+((fieldSize+1)*agentPos.x()+1), starty+((fieldSize+1)*agentPos.y()+1), fieldSize, fieldSize);
 	}
 	
 	private void drawClear(){
 		Color c;
 		if(agentPos == startField || agentPos == endField){
 			c = Color.lightGray;
 		}else if(cliffList.contains(agentPos)){
 			c = Color.black;
 		}else{
 			c = Color.white;
 		}
 		fillField(agentPos, c);
 	}
 	
 	private void fillField(Position pos, Color c){
 		g.setColor(c);
 		g.fillRect(startx+((fieldSize+1)*pos.x()+1), starty+((fieldSize+1)*pos.y()+1), fieldSize, fieldSize);
 	}
 	
     public void render(Position agentPos) {
     	if (this.agentPos != agentPos){
     		drawClear();
     		this.agentPos = agentPos;
     		drawAgent();
     	}
     }
 }
