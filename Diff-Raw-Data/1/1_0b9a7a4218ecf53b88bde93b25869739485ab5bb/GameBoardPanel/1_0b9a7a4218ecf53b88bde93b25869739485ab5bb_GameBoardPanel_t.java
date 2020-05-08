 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.Polygon;
 import java.util.ArrayList;
 
 import javax.swing.JPanel;
 
 
 public class GameBoardPanel extends JPanel {
 	public GameBoardPanel(GameLogic gl) {
 		this.gl = gl;
 		this.hexRolls = gl.hexRolls;
 		this.resDistr = gl.resDistr;
 		this.portOrder = gl.portOrder;
		roadList = new ArrayList<GamePiece>();
 	}
 	
 	//Keep these in a 30 60 90 ratio
 	//Also try to make them ints
 	private int a = 22;
 	private int b = 38;
 	private int c = 44;
 	
 	private int startX = 350;
 	private int startY = 250;
 	
 	private GameLogic gl;
 	
 	private ArrayList<Integer> hexRolls;
 	private ArrayList<Resource> resDistr;
 	private ArrayList<Resource> portOrder;
 	
 	private ArrayList<GamePiece> roadList;
 	
 	
 	public void paint(Graphics g){
 		
 		Polygon bigHex = new Polygon();
 		int bigX = (int) (startX - 5.90*b);
 		int bigY = (int) (startY - 2.75*(a+c));
 		bigHex.addPoint(bigX , bigY + b*6);
 		bigHex.addPoint(bigX + 6*a, bigY );
 		bigHex.addPoint(bigX + 6*(a+c), bigY );
 		bigHex.addPoint(bigX + 6*2*c, bigY + 6*b);
 		bigHex.addPoint(bigX + 6*(a+c), bigY + 6*2*b);
 		bigHex.addPoint(bigX + 6*a, bigY + 6*2*b);
 		g.setColor(Color.blue);
 		g.fillPolygon(bigHex);
 		
 		
 		//Set up board
 		// ordered from left to right, top to bottom.
 		g.setColor(resDistr.get(0).getColor());
 		g.fillPolygon(makeHex(startX - 2*b,startY - 2*(a + c)));
 		g.setColor(resDistr.get(1).getColor());
 		g.fillPolygon(makeHex(startX, startY - 2*(a + c) ));
 		g.setColor(resDistr.get(2).getColor());
 		g.fillPolygon(makeHex(startX + 2*b,startY - 2*(a + c)));
 		g.setColor(resDistr.get(3).getColor());
 		g.fillPolygon(makeHex(startX - 3*b,startY - 1*(a + c)));
 		g.setColor(resDistr.get(4).getColor());
 		g.fillPolygon(makeHex(startX - 1*b,startY - 1*(a + c)));
 		g.setColor(resDistr.get(5).getColor());
 		g.fillPolygon(makeHex(startX + 1*b,startY - 1*(a + c)));
 		g.setColor(resDistr.get(6).getColor());
 		g.fillPolygon(makeHex(startX + b*3, startY - (a + c) ));
 		g.setColor(resDistr.get(7).getColor());
 		g.fillPolygon(makeHex(startX - 4*b,startY));
 		g.setColor(resDistr.get(8).getColor());
 		g.fillPolygon(makeHex(startX - 2*b,startY ));
 		g.setColor(resDistr.get(9).getColor());
 		g.fillPolygon(makeHex(startX + 0*b,startY + 0*(a + c)));
 		g.setColor(resDistr.get(10).getColor());
 		g.fillPolygon(makeHex(startX + 2*b,startY ));
 		g.setColor(resDistr.get(11).getColor());
 		g.fillPolygon(makeHex(startX + 4*b,startY + 0*(a + c)));
 		g.setColor(resDistr.get(12).getColor());
 		g.fillPolygon(makeHex(startX - 3*b,startY + 1*(a + c)));
 		g.setColor(resDistr.get(13).getColor());
 		g.fillPolygon(makeHex(startX - b, startY + a + c));
 		g.setColor(resDistr.get(14).getColor());
 		g.fillPolygon(makeHex(startX + b,startY + a + c));
 		g.setColor(resDistr.get(15).getColor());
 		g.fillPolygon(makeHex(startX + 3*b,startY + 1*(a + c)));
 		g.setColor(resDistr.get(16).getColor());
 		g.fillPolygon(makeHex(startX - 2*b,startY + 2*(a + c)));
 		g.setColor(resDistr.get(17).getColor());
 		g.fillPolygon(makeHex(startX + 0*b,startY + 2*(a + c)));
 		g.setColor(resDistr.get(18).getColor());
 		g.fillPolygon(makeHex(startX + 2*b,startY + 2*(a + c)));
 		
 		//Draw border lines
 		g.setColor(Color.black);
 		g.drawPolygon(makeHex(startX - 4*b,startY));
 		g.drawPolygon(makeHex(startX + b,startY + a + c));
 		g.drawPolygon(makeHex(startX - b, startY + a + c));
 		g.drawPolygon(makeHex(startX + b*3, startY - (a + c) ));
 		g.drawPolygon(makeHex(startX, startY - 2*(a + c) ));
 		g.drawPolygon(makeHex(startX + 2*b,startY ));
 		g.drawPolygon(makeHex(startX + 2*b,startY + 2*(a + c)));
 		g.drawPolygon(makeHex(startX - 2*b,startY ));
 		g.drawPolygon(makeHex(startX - 2*b,startY - 2*(a + c)));
 		g.drawPolygon(makeHex(startX + 0*b,startY + 0*(a + c)));
 		g.drawPolygon(makeHex(startX + 2*b,startY - 2*(a + c)));
 		g.drawPolygon(makeHex(startX + 4*b,startY + 0*(a + c)));
 		g.drawPolygon(makeHex(startX + 0*b,startY + 2*(a + c)));
 		g.drawPolygon(makeHex(startX + 1*b,startY - 1*(a + c)));
 		g.drawPolygon(makeHex(startX - 3*b,startY - 1*(a + c)));
 		g.drawPolygon(makeHex(startX - 3*b,startY + 1*(a + c)));
 		g.drawPolygon(makeHex(startX - 1*b,startY - 1*(a + c)));
 		g.drawPolygon(makeHex(startX + 3*b,startY + 1*(a + c)));
 		g.drawPolygon(makeHex(startX - 2*b,startY + 2*(a + c)));
 		
 		
 		//Draw Roll Thingies
 		g.setColor(Color.LIGHT_GRAY);
 		g.fillOval(214 + 2*b, 272, c, c);
 		g.fillOval(214 + 4*b, 272, c, c);
 		g.fillOval(214 + 6*b, 272, c, c);
 		g.fillOval(214 + 8*b, 272, c, c);
 		g.fillOval(214 + 1*b, 272 - (a + c), c, c);
 		g.fillOval(214 + 3*b, 272 - (a + c), c, c);
 		g.fillOval(214 + 5*b, 272 - (a + c), c, c);
 		g.fillOval(214 + 7*b, 272 - (a + c), c, c);
 		g.fillOval(214 + 2*b, 272 - 2*(a + c), c, c);
 		g.fillOval(214 + 4*b, 272 - 2*(a + c), c, c);
 		g.fillOval(214 + 6*b, 272 - 2*(a + c), c, c);
 		g.fillOval(214 + 1*b, 272 + (a + c), c, c);
 		g.fillOval(214 + 3*b, 272 + (a + c), c, c);
 		g.fillOval(214 + 5*b, 272 + (a + c), c, c);
 		g.fillOval(214 + 7*b, 272 + (a + c), c, c);
 		g.fillOval(214 + 2*b, 272 + 2*(a + c), c, c);
 		g.fillOval(214 + 4*b, 272 + 2*(a + c), c, c);
 		g.fillOval(214 + 6*b, 272 + 2*(a + c), c, c);
 		//Docks
 		// ordered clockwise starting from the top left corner next to Resource #1
 		g.setColor(portOrder.get(0).getColor());
 		g.fillOval(214 + 10*b, 272 - 0*(a + c), c, c); //? top left
 		g.setColor(portOrder.get(1).getColor());
 		g.fillOval(214 + 5*b, 272 - 3*(a + c), c, c); //S
 		g.setColor(portOrder.get(2).getColor());
 		g.fillOval(214 + 8*b, 272 - 2*(a + c), c, c); //? top right edge middle
 		g.setColor(portOrder.get(3).getColor());
 		g.fillOval(214 + 1*b, 272 - 3*(a + c), c, c); //? middle right corner
 		g.setColor(portOrder.get(4).getColor());
 		g.fillOval(214 + 8*b, 272 + 2*(a + c), c, c); //B
 		g.setColor(portOrder.get(5).getColor());
 		g.fillOval(214 + 5*b, 272 + 3*(a + c), c, c); //L
 		g.setColor(portOrder.get(6).getColor());
 		g.fillOval(214 + 1*b, 272 + 3*(a + c), c, c); //? bottom left
 		g.setColor(portOrder.get(7).getColor());
 		g.fillOval(214 - 1*b, 272 + (a + c), c, c); //W
 		g.setColor(portOrder.get(8).getColor());
 		g.fillOval(214 - 1*b, 272 - (a + c), c, c); //O
 		
 		g.setColor(Color.black);
 		g.drawOval(214 + 2*b, 272, c, c);
 		g.drawOval(214 + 4*b, 272, c, c);
 		g.drawOval(214 + 6*b, 272, c, c);
 		g.drawOval(214 + 8*b, 272, c, c);
 		g.drawOval(214 + 1*b, 272 - (a + c), c, c);
 		g.drawOval(214 + 3*b, 272 - (a + c), c, c);
 		g.drawOval(214 + 5*b, 272 - (a + c), c, c);
 		g.drawOval(214 + 7*b, 272 - (a + c), c, c);
 		g.drawOval(214 + 2*b, 272 - 2*(a + c), c, c);
 		g.drawOval(214 + 4*b, 272 - 2*(a + c), c, c);
 		g.drawOval(214 + 6*b, 272 - 2*(a + c), c, c);
 		g.drawOval(214 + 1*b, 272 + (a + c), c, c);
 		g.drawOval(214 + 3*b, 272 + (a + c), c, c);
 		g.drawOval(214 + 5*b, 272 + (a + c), c, c);
 		g.drawOval(214 + 7*b, 272 + (a + c), c, c);
 		g.drawOval(214 + 2*b, 272 + 2*(a + c), c, c);
 		g.drawOval(214 + 4*b, 272 + 2*(a + c), c, c);
 		g.drawOval(214 + 6*b, 272 + 2*(a + c), c, c);
 		g.drawOval(214 + 5*b, 272 + 3*(a + c), c, c);
 		g.drawOval(214 + 8*b, 272 + 2*(a + c), c, c);
 		g.drawOval(214 + 1*b, 272 + 3*(a + c), c, c);
 		g.drawOval(214 - 1*b, 272 + (a + c), c, c);
 		g.drawOval(214 - 1*b, 272 - (a + c), c, c);
 		g.drawOval(214 + 5*b, 272 - 3*(a + c), c, c);
 		g.drawOval(214 + 8*b, 272 - 2*(a + c), c, c);
 		g.drawOval(214 + 1*b, 272 - 3*(a + c), c, c);
 		g.drawOval(214 + 10*b, 272 - 0*(a + c), c, c);
 		
 		g.setColor(Color.black);
 		g.setFont(new Font("Times New Roman", Font.PLAIN, 20));
 		g.drawString(hexRolls.get(0).toString(), 301, 168);
 		g.drawString(hexRolls.get(1).toString(), 301 + 2*b, 168);
 		g.drawString(hexRolls.get(2).toString(), 301 + 4*b, 168);
 		g.drawString(hexRolls.get(3).toString(), 301 - 1*b, 168 + (a+c));
 		g.drawString(hexRolls.get(4).toString(), 301 + 1*b, 168 + (a+c));
 		g.drawString(hexRolls.get(5).toString(), 301 + 3*b, 168 + (a+c));
 		g.drawString(hexRolls.get(6).toString(), 301 + 5*b, 168 + (a+c));
 		g.drawString(hexRolls.get(8).toString(), 301 + 0*b, 168 + 2*(a+c));
 		g.drawString(hexRolls.get(9).toString(), 301 + 2*b, 168 + 2*(a+c));
 		g.drawString(hexRolls.get(10).toString(), 301 + 4*b, 168 + 2*(a+c));
 		g.drawString(hexRolls.get(11).toString(), 301 + 6*b, 168 + 2*(a+c));
 		g.drawString(hexRolls.get(12).toString(), 301 - 1*b, 168 + 3*(a+c));
 		g.drawString(hexRolls.get(13).toString(), 301 + 1*b, 168 + 3*(a+c));
 		g.drawString(hexRolls.get(14).toString(), 301 + 3*b, 168 + 3*(a+c));
 		g.drawString(hexRolls.get(15).toString(), 301 + 5*b, 168 + 3*(a+c));
 		g.drawString(hexRolls.get(16).toString(), 301 + 0*b, 168 + 4*(a+c));
 		g.drawString(hexRolls.get(17).toString(), 301 + 2*b, 168 + 4*(a+c));
 		g.drawString(hexRolls.get(18).toString(), 301 + 4*b, 168 + 4*(a+c));
 		// ordered clockwise starting from the top left corner next to Resource #1
 		g.drawString(" "+portOrder.get(0).toPortString(), 301 - 1*b, 168 - 1*(a+c));
 		g.drawString(" "+portOrder.get(1).toPortString(), 301 + 3*b, 168 - 1*(a+c));
 		g.drawString(" "+portOrder.get(2).toPortString(), 301 + 6*b, 168 + 0*(a+c));
 		g.drawString(" "+portOrder.get(3).toPortString(), 301 + 8*b, 168 + 2*(a+c));
 		g.drawString(" "+portOrder.get(4).toPortString(), 301 + 6*b, 168 + 4*(a+c)); 
 		g.drawString(" "+portOrder.get(5).toPortString(), 301 + 3*b, 168 + 5*(a+c));
 		g.drawString(" "+portOrder.get(6).toPortString(), 301 - 1*b, 168 + 5*(a+c));
 		g.drawString(" "+portOrder.get(7).toPortString(), 301 - 3*b, 168 + 3*(a+c));
 		g.drawString(" "+portOrder.get(8).toPortString(), 301 - 3*b, 168 + 1*(a+c));
 		
 		for(GamePiece p : roadList){
 			if(!(p == null)) drawRoad(p.player, p.location, g);
 		}
 		
 		
 	}
 	
 	public Polygon makeHex(int x, int y){
 		Polygon p = new Polygon();
 		p.addPoint(x, y + a + c);
 		p.addPoint(x , y + a);
 		p.addPoint(x + b, y);
 		p.addPoint(x + b + b, y + a);
 		p.addPoint(x + b + b, y + a + c);
 		p.addPoint(x + b, y + c + c);
 		
 		return p;
 	}
 	
 	public void drawRoad(int player, int location, Graphics g){
 		Color col = gl.playerList.get(player).color;
 		Polygon road = new Polygon();
 		switch(location){
 		case 0:
 			road.addPoint(277, 134);
 			road.addPoint(307, 117);
 			road.addPoint(311, 124);
 			road.addPoint(281, 141);
 			break;
 		case 1:
 			road.addPoint(313, 124);
 			road.addPoint(317, 117);
 			road.addPoint(347, 134);
 			road.addPoint(343, 141);
 			break;
 		case 2:
 			road.addPoint(277 + 2*b, 134);
 			road.addPoint(307 + 2*b, 117);
 			road.addPoint(311 + 2*b, 124);
 			road.addPoint(281 + 2*b, 141);
 			break;
 		case 3:
 			road.addPoint(313 + 2*b, 124);
 			road.addPoint(317 + 2*b, 117);
 			road.addPoint(347 + 2*b, 134);
 			road.addPoint(343 + 2*b, 141);
 			break;
 		case 4:
 			road.addPoint(277 + 4*b, 134);
 			road.addPoint(307 + 4*b, 117);
 			road.addPoint(311 + 4*b, 124);
 			road.addPoint(281 + 4*b, 141);
 			break;
 		case 5:
 			road.addPoint(313 + 4*b, 124);
 			road.addPoint(317 + 4*b, 117);
 			road.addPoint(347 + 4*b, 134);
 			road.addPoint(343 + 4*b, 141);
 			break;
 		case 6:
 			road.addPoint(278 + 0*b, 144);
 			road.addPoint(278 + 0*b, 180);
 			road.addPoint(270 + 0*b, 180);
 			road.addPoint(270 + 0*b, 144);
 			break;
 		case 7:
 			road.addPoint(278 + 2*b, 144);
 			road.addPoint(278 + 2*b, 180);
 			road.addPoint(270 + 2*b, 180);
 			road.addPoint(270 + 2*b, 144);
 			break;
 		case 8:
 			road.addPoint(278 + 4*b, 144);
 			road.addPoint(278 + 4*b, 180);
 			road.addPoint(270 + 4*b, 180);
 			road.addPoint(270 + 4*b, 144);
 			break;
 		case 9:
 			road.addPoint(278 + 6*b, 144);
 			road.addPoint(278 + 6*b, 180);
 			road.addPoint(270 + 6*b, 180);
 			road.addPoint(270 + 6*b, 144);
 			break;
 		case 10:
 			road.addPoint(277 + -1*b, 134 + 1*(a+c));
 			road.addPoint(307 + -1*b, 117 + 1*(a+c));
 			road.addPoint(311 + -1*b, 124 + 1*(a+c));
 			road.addPoint(281 + -1*b, 141 + 1*(a+c));
 			break;
 		case 11:
 			road.addPoint(313 + -1*b, 124 + 1*(a+c));
 			road.addPoint(317 + -1*b, 117 + 1*(a+c));
 			road.addPoint(347 + -1*b, 134 + 1*(a+c));
 			road.addPoint(343 + -1*b, 141 + 1*(a+c));
 			break;
 		case 12:
 			road.addPoint(277 + 1*b, 134 + 1*(a+c));
 			road.addPoint(307 + 1*b, 117 + 1*(a+c));
 			road.addPoint(311 + 1*b, 124 + 1*(a+c));
 			road.addPoint(281 + 1*b, 141 + 1*(a+c));
 			break;
 		case 13:
 			road.addPoint(313 + 1*b, 124 + 1*(a+c));
 			road.addPoint(317 + 1*b, 117 + 1*(a+c));
 			road.addPoint(347 + 1*b, 134 + 1*(a+c));
 			road.addPoint(343 + 1*b, 141 + 1*(a+c));
 			break;
 		case 14:
 			road.addPoint(277 + 3*b, 134 + 1*(a+c));
 			road.addPoint(307 + 3*b, 117 + 1*(a+c));
 			road.addPoint(311 + 3*b, 124 + 1*(a+c));
 			road.addPoint(281 + 3*b, 141 + 1*(a+c));
 			break;
 		case 15:
 			road.addPoint(313 + 3*b, 124 + 1*(a+c));
 			road.addPoint(317 + 3*b, 117 + 1*(a+c));
 			road.addPoint(347 + 3*b, 134 + 1*(a+c));
 			road.addPoint(343 + 3*b, 141 + 1*(a+c));
 			break;
 		case 16:
 			road.addPoint(277 + 5*b, 134 + 1*(a+c));
 			road.addPoint(307 + 5*b, 117 + 1*(a+c));
 			road.addPoint(311 + 5*b, 124 + 1*(a+c));
 			road.addPoint(281 + 5*b, 141 + 1*(a+c));
 			break;
 		case 17:
 			road.addPoint(313 + 5*b, 124 + 1*(a+c));
 			road.addPoint(317 + 5*b, 117 + 1*(a+c));
 			road.addPoint(347 + 5*b, 134 + 1*(a+c));
 			road.addPoint(343 + 5*b, 141 + 1*(a+c));
 			break;
 		case 18:
 			road.addPoint(278 + -1*b, 144 + 1*(a+c));
 			road.addPoint(278 + -1*b, 180 + 1*(a+c));
 			road.addPoint(270 + -1*b, 180 + 1*(a+c));
 			road.addPoint(270 + -1*b, 144 + 1*(a+c));
 			break;
 		case 19:
 			road.addPoint(278 + 1*b, 144 + 1*(a+c));
 			road.addPoint(278 + 1*b, 180 + 1*(a+c));
 			road.addPoint(270 + 1*b, 180 + 1*(a+c));
 			road.addPoint(270 + 1*b, 144 + 1*(a+c));
 			break;
 		case 20:
 			road.addPoint(278 + 3*b, 144 + 1*(a+c));
 			road.addPoint(278 + 3*b, 180 + 1*(a+c));
 			road.addPoint(270 + 3*b, 180 + 1*(a+c));
 			road.addPoint(270 + 3*b, 144 + 1*(a+c));
 			break;
 		case 21:
 			road.addPoint(278 + 5*b, 144 + 1*(a+c));
 			road.addPoint(278 + 5*b, 180 + 1*(a+c));
 			road.addPoint(270 + 5*b, 180 + 1*(a+c));
 			road.addPoint(270 + 5*b, 144 + 1*(a+c));
 			break;
 		case 22:
 			road.addPoint(278 + 7*b, 144 + 1*(a+c));
 			road.addPoint(278 + 7*b, 180 + 1*(a+c));
 			road.addPoint(270 + 7*b, 180 + 1*(a+c));
 			road.addPoint(270 + 7*b, 144 + 1*(a+c));
 			break;
 		case 23:
 			road.addPoint(277 + -2*b, 134 + 2*(a+c));
 			road.addPoint(307 + -2*b, 117 + 2*(a+c));
 			road.addPoint(311 + -2*b, 124 + 2*(a+c));
 			road.addPoint(281 + -2*b, 141 + 2*(a+c));
 			break;
 		case 24:
 			road.addPoint(313 + -2*b, 124 + 2*(a+c));
 			road.addPoint(317 + -2*b, 117 + 2*(a+c));
 			road.addPoint(347 + -2*b, 134 + 2*(a+c));
 			road.addPoint(343 + -2*b, 141 + 2*(a+c));
 			break;
 		case 25:
 			road.addPoint(277 + 0*b, 134 + 2*(a+c));
 			road.addPoint(307 + 0*b, 117 + 2*(a+c));
 			road.addPoint(311 + 0*b, 124 + 2*(a+c));
 			road.addPoint(281 + 0*b, 141 + 2*(a+c));
 			break;
 		case 26:
 			road.addPoint(313 + 0*b, 124 + 2*(a+c));
 			road.addPoint(317 + 0*b, 117 + 2*(a+c));
 			road.addPoint(347 + 0*b, 134 + 2*(a+c));
 			road.addPoint(343 + 0*b, 141 + 2*(a+c));
 			break;
 		case 27:
 			road.addPoint(277 + 2*b, 134 + 2*(a+c));
 			road.addPoint(307 + 2*b, 117 + 2*(a+c));
 			road.addPoint(311 + 2*b, 124 + 2*(a+c));
 			road.addPoint(281 + 2*b, 141 + 2*(a+c));
 			break;
 		case 28:
 			road.addPoint(313 + 2*b, 124 + 2*(a+c));
 			road.addPoint(317 + 2*b, 117 + 2*(a+c));
 			road.addPoint(347 + 2*b, 134 + 2*(a+c));
 			road.addPoint(343 + 2*b, 141 + 2*(a+c));
 			break;
 		case 29:
 			road.addPoint(277 + 4*b, 134 + 2*(a+c));
 			road.addPoint(307 + 4*b, 117 + 2*(a+c));
 			road.addPoint(311 + 4*b, 124 + 2*(a+c));
 			road.addPoint(281 + 4*b, 141 + 2*(a+c));
 			break;
 		case 30:
 			road.addPoint(313 + 4*b, 124 + 2*(a+c));
 			road.addPoint(317 + 4*b, 117 + 2*(a+c));
 			road.addPoint(347 + 4*b, 134 + 2*(a+c));
 			road.addPoint(343 + 4*b, 141 + 2*(a+c));
 			break;
 		case 31:
 			road.addPoint(277 + 6*b, 134 + 2*(a+c));
 			road.addPoint(307 + 6*b, 117 + 2*(a+c));
 			road.addPoint(311 + 6*b, 124 + 2*(a+c));
 			road.addPoint(281 + 6*b, 141 + 2*(a+c));
 			break;
 		case 32:
 			road.addPoint(313 + 6*b, 124 + 2*(a+c));
 			road.addPoint(317 + 6*b, 117 + 2*(a+c));
 			road.addPoint(347 + 6*b, 134 + 2*(a+c));
 			road.addPoint(343 + 6*b, 141 + 2*(a+c));
 			break;
 		case 33:
 			road.addPoint(278 + -2*b, 144 + 2*(a+c));
 			road.addPoint(278 + -2*b, 180 + 2*(a+c));
 			road.addPoint(270 + -2*b, 180 + 2*(a+c));
 			road.addPoint(270 + -2*b, 144 + 2*(a+c));
 			break;
 		case 34:
 			road.addPoint(278 + 0*b, 144 + 2*(a+c));
 			road.addPoint(278 + 0*b, 180 + 2*(a+c));
 			road.addPoint(270 + 0*b, 180 + 2*(a+c));
 			road.addPoint(270 + 0*b, 144 + 2*(a+c));
 			break;
 		case 35:
 			road.addPoint(278 + 2*b, 144 + 2*(a+c));
 			road.addPoint(278 + 2*b, 180 + 2*(a+c));
 			road.addPoint(270 + 2*b, 180 + 2*(a+c));
 			road.addPoint(270 + 2*b, 144 + 2*(a+c));
 			break;
 		case 36:
 			road.addPoint(278 + 4*b, 144 + 2*(a+c));
 			road.addPoint(278 + 4*b, 180 + 2*(a+c));
 			road.addPoint(270 + 4*b, 180 + 2*(a+c));
 			road.addPoint(270 + 4*b, 144 + 2*(a+c));
 			break;
 		case 37:
 			road.addPoint(278 + 6*b, 144 + 2*(a+c));
 			road.addPoint(278 + 6*b, 180 + 2*(a+c));
 			road.addPoint(270 + 6*b, 180 + 2*(a+c));
 			road.addPoint(270 + 6*b, 144 + 2*(a+c));
 			break;
 		case 38:
 			road.addPoint(278 + 6*b, 144 + 4*(a+c));
 			road.addPoint(278 + 6*b, 180 + 4*(a+c));
 			road.addPoint(270 + 6*b, 180 + 4*(a+c));
 			road.addPoint(270 + 6*b, 144 + 4*(a+c));
 			break;
 		case 39:
 			road.addPoint(313 + -3*b, 124 + 3*(a+c));
 			road.addPoint(317 + -3*b, 117 + 3*(a+c));
 			road.addPoint(347 + -3*b, 134 + 3*(a+c));
 			road.addPoint(343 + -3*b, 141 + 3*(a+c));
 			break;
 		case 40:
 			road.addPoint(277 + -1*b, 134 + 3*(a+c));
 			road.addPoint(307 + -1*b, 117 + 3*(a+c));
 			road.addPoint(311 + -1*b, 124 + 3*(a+c));
 			road.addPoint(281 + -1*b, 141 + 3*(a+c));
 			break;
 		case 41:
 			road.addPoint(313 + -1*b, 124 + 3*(a+c));
 			road.addPoint(317 + -1*b, 117 + 3*(a+c));
 			road.addPoint(347 + -1*b, 134 + 3*(a+c));
 			road.addPoint(343 + -1*b, 141 + 3*(a+c));
 			break;
 		case 42:
 			road.addPoint(277 + 1*b, 134 + 3*(a+c));
 			road.addPoint(307 + 1*b, 117 + 3*(a+c));
 			road.addPoint(311 + 1*b, 124 + 3*(a+c));
 			road.addPoint(281 + 1*b, 141 + 3*(a+c));
 			break;
 		case 43:
 			road.addPoint(313 + 1*b, 124 + 3*(a+c));
 			road.addPoint(317 + 1*b, 117 + 3*(a+c));
 			road.addPoint(347 + 1*b, 134 + 3*(a+c));
 			road.addPoint(343 + 1*b, 141 + 3*(a+c));
 			break;
 		case 44:
 			road.addPoint(277 + 3*b, 134 + 3*(a+c));
 			road.addPoint(307 + 3*b, 117 + 3*(a+c));
 			road.addPoint(311 + 3*b, 124 + 3*(a+c));
 			road.addPoint(281 + 3*b, 141 + 3*(a+c));
 			break;
 		case 45:
 			road.addPoint(313 + 3*b, 124 + 3*(a+c));
 			road.addPoint(317 + 3*b, 117 + 3*(a+c));
 			road.addPoint(347 + 3*b, 134 + 3*(a+c));
 			road.addPoint(343 + 3*b, 141 + 3*(a+c));
 			break;
 		case 46:
 			road.addPoint(277 + 5*b, 134 + 3*(a+c));
 			road.addPoint(307 + 5*b, 117 + 3*(a+c));
 			road.addPoint(311 + 5*b, 124 + 3*(a+c));
 			road.addPoint(281 + 5*b, 141 + 3*(a+c));
 			break;
 		case 47:
 			road.addPoint(313 + 5*b, 124 + 3*(a+c));
 			road.addPoint(317 + 5*b, 117 + 3*(a+c));
 			road.addPoint(347 + 5*b, 134 + 3*(a+c));
 			road.addPoint(343 + 5*b, 141 + 3*(a+c));
 			break;
 		case 48:
 			road.addPoint(277 + 7*b, 134 + 3*(a+c));
 			road.addPoint(307 + 7*b, 117 + 3*(a+c));
 			road.addPoint(311 + 7*b, 124 + 3*(a+c));
 			road.addPoint(281 + 7*b, 141 + 3*(a+c));
 			break;
 		case 49:
 			road.addPoint(278 + -1*b, 144 + 3*(a+c));
 			road.addPoint(278 + -1*b, 180 + 3*(a+c));
 			road.addPoint(270 + -1*b, 180 + 3*(a+c));
 			road.addPoint(270 + -1*b, 144 + 3*(a+c));
 			break;
 		case 50:
 			road.addPoint(278 + 1*b, 144 + 3*(a+c));
 			road.addPoint(278 + 1*b, 180 + 3*(a+c));
 			road.addPoint(270 + 1*b, 180 + 3*(a+c));
 			road.addPoint(270 + 1*b, 144 + 3*(a+c));
 			break;
 		case 51:
 			road.addPoint(278 + 3*b, 144 + 3*(a+c));
 			road.addPoint(278 + 3*b, 180 + 3*(a+c));
 			road.addPoint(270 + 3*b, 180 + 3*(a+c));
 			road.addPoint(270 + 3*b, 144 + 3*(a+c));
 			break;
 		case 52:
 			road.addPoint(278 + 5*b, 144 + 3*(a+c));
 			road.addPoint(278 + 5*b, 180 + 3*(a+c));
 			road.addPoint(270 + 5*b, 180 + 3*(a+c));
 			road.addPoint(270 + 5*b, 144 + 3*(a+c));
 			break;
 		case 53:
 			road.addPoint(278 + 7*b, 144 + 3*(a+c));
 			road.addPoint(278 + 7*b, 180 + 3*(a+c));
 			road.addPoint(270 + 7*b, 180 + 3*(a+c));
 			road.addPoint(270 + 7*b, 144 + 3*(a+c));
 			break;
 		case 54:
 			road.addPoint(313 + -2*b, 124 + 4*(a+c));
 			road.addPoint(317 + -2*b, 117 + 4*(a+c));
 			road.addPoint(347 + -2*b, 134 + 4*(a+c));
 			road.addPoint(343 + -2*b, 141 + 4*(a+c));
 			break;
 		case 55:
 			road.addPoint(277 + 0*b, 134 + 4*(a+c));
 			road.addPoint(307 + 0*b, 117 + 4*(a+c));
 			road.addPoint(311 + 0*b, 124 + 4*(a+c));
 			road.addPoint(281 + 0*b, 141 + 4*(a+c));
 			break;
 		case 56:
 			road.addPoint(313 + 0*b, 124 + 4*(a+c));
 			road.addPoint(317 + 0*b, 117 + 4*(a+c));
 			road.addPoint(347 + 0*b, 134 + 4*(a+c));
 			road.addPoint(343 + 0*b, 141 + 4*(a+c));
 			break;
 		case 57:
 			road.addPoint(277 + 2*b, 134 + 4*(a+c));
 			road.addPoint(307 + 2*b, 117 + 4*(a+c));
 			road.addPoint(311 + 2*b, 124 + 4*(a+c));
 			road.addPoint(281 + 2*b, 141 + 4*(a+c));
 			break;
 		case 58:
 			road.addPoint(313 + 2*b, 124 + 4*(a+c));
 			road.addPoint(317 + 2*b, 117 + 4*(a+c));
 			road.addPoint(347 + 2*b, 134 + 4*(a+c));
 			road.addPoint(343 + 2*b, 141 + 4*(a+c));
 			break;
 		case 59:
 			road.addPoint(277 + 4*b, 134 + 4*(a+c));
 			road.addPoint(307 + 4*b, 117 + 4*(a+c));
 			road.addPoint(311 + 4*b, 124 + 4*(a+c));
 			road.addPoint(281 + 4*b, 141 + 4*(a+c));
 			break;
 		case 60:
 			road.addPoint(313 + 4*b, 124 + 4*(a+c));
 			road.addPoint(317 + 4*b, 117 + 4*(a+c));
 			road.addPoint(347 + 4*b, 134 + 4*(a+c));
 			road.addPoint(343 + 4*b, 141 + 4*(a+c));
 			break;
 		case 61:
 			road.addPoint(277 + 6*b, 134 + 4*(a+c));
 			road.addPoint(307 + 6*b, 117 + 4*(a+c));
 			road.addPoint(311 + 6*b, 124 + 4*(a+c));
 			road.addPoint(281 + 6*b, 141 + 4*(a+c));
 			break;
 		case 62:
 			road.addPoint(278 + 0*b, 144 + 4*(a+c));
 			road.addPoint(278 + 0*b, 180 + 4*(a+c));
 			road.addPoint(270 + 0*b, 180 + 4*(a+c));
 			road.addPoint(270 + 0*b, 144 + 4*(a+c));
 			break;
 		case 63:
 			road.addPoint(278 + 2*b, 144 + 4*(a+c));
 			road.addPoint(278 + 2*b, 180 + 4*(a+c));
 			road.addPoint(270 + 2*b, 180 + 4*(a+c));
 			road.addPoint(270 + 2*b, 144 + 4*(a+c));
 			break;
 		case 64:
 			road.addPoint(278 + 4*b, 144 + 4*(a+c));
 			road.addPoint(278 + 4*b, 180 + 4*(a+c));
 			road.addPoint(270 + 4*b, 180 + 4*(a+c));
 			road.addPoint(270 + 4*b, 144 + 4*(a+c));
 			break;
 		case 65:
 			road.addPoint(278 + 6*b, 144 + 4*(a+c));
 			road.addPoint(278 + 6*b, 180 + 4*(a+c));
 			road.addPoint(270 + 6*b, 180 + 4*(a+c));
 			road.addPoint(270 + 6*b, 144 + 4*(a+c));
 			break;
 		case 66:
 			road.addPoint(313 + -1*b, 124 + 5*(a+c));
 			road.addPoint(317 + -1*b, 117 + 5*(a+c));
 			road.addPoint(347 + -1*b, 134 + 5*(a+c));
 			road.addPoint(343 + -1*b, 141 + 5*(a+c));
 			break;
 		case 67:
 			road.addPoint(277 + 1*b, 134 + 5*(a+c));
 			road.addPoint(307 + 1*b, 117 + 5*(a+c));
 			road.addPoint(311 + 1*b, 124 + 5*(a+c));
 			road.addPoint(281 + 1*b, 141 + 5*(a+c));
 			break;
 		case 68:
 			road.addPoint(313 + 1*b, 124 + 5*(a+c));
 			road.addPoint(317 + 1*b, 117 + 5*(a+c));
 			road.addPoint(347 + 1*b, 134 + 5*(a+c));
 			road.addPoint(343 + 1*b, 141 + 5*(a+c));
 			break;
 		case 69:
 			road.addPoint(277 + 3*b, 134 + 5*(a+c));
 			road.addPoint(307 + 3*b, 117 + 5*(a+c));
 			road.addPoint(311 + 3*b, 124 + 5*(a+c));
 			road.addPoint(281 + 3*b, 141 + 5*(a+c));
 			break;
 		case 70:
 			road.addPoint(313 + 3*b, 124 + 5*(a+c));
 			road.addPoint(317 + 3*b, 117 + 5*(a+c));
 			road.addPoint(347 + 3*b, 134 + 5*(a+c));
 			road.addPoint(343 + 3*b, 141 + 5*(a+c));
 			break;
 		case 71:
 			road.addPoint(277 + 5*b, 134 + 5*(a+c));
 			road.addPoint(307 + 5*b, 117 + 5*(a+c));
 			road.addPoint(311 + 5*b, 124 + 5*(a+c));
 			road.addPoint(281 + 5*b, 141 + 5*(a+c));
 			break;
 		}
 		g.setColor(col);
 		g.fillPolygon(road);
 		g.setColor(Color.BLACK);
 		g.drawPolygon(road);
 				
 	}
 
 	public void updateLogic(GameLogic l) {
 		this.gl = l;
 		this.roadList = gl.road;  
 	}
 	
 	
 }
