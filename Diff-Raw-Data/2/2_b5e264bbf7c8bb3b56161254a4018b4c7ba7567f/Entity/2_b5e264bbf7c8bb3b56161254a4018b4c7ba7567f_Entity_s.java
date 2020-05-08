 import java.awt.*;
 import java.io.File;
 import java.io.IOException;
 
 import javax.imageio.*;
 
 //Fri, Jan 4th
 //TODO: NPCs Collide with each other
 
 public class Entity{
 	static NPC root = null;
 	static int nNpc = -1; //The last NPC index 
 	public static Player p;
 	public static Weapon[] w = new Weapon[30];
 	public static int wIndex;
 	
 	public static Teleport[] t = new Teleport[30];
 	public static int tIndex;
 
 	
 	public Entity(Player p){
 		this.p = p;
 		wIndex = 0;
 		tIndex = 0;
 		root = null;
 		nNpc = -1;
		w = new Weapon[50];
 		for (int i = 0; i < w.length; i++)
 			w[i] = new Weapon();
 		t = new Teleport[30];
 	}
 	
 	public double getDist(NPC from){
 		int tX = (int)p.x - (int)from.x;
 		int tY = (int)p.y - (int)from.y;
 		return Math.sqrt((tX)*(tX)+(tY)*(tY));
 	}
 	
 	public void addNPC(int type, int x, int y, int diff){
 		if (this.root == null)	//The first npc, the added npc should become the root
 			this.root = new NPC(0, type, diff, x, y);
 		else{	//This should link the previous npc to the added one
 			NPC temp = new NPC(nNpc+1, type, diff, x, y);
 			NPC prev = getEne(this.root, nNpc);
 			prev.next = temp;
 		}
 		nNpc++;
 	}
 	
 	public void moveNormal(NPC at){
 		if ((System.currentTimeMillis() - at.hit) > 750){
 			if (at.range*24 >= getDist(at)){
 				at.hitPlayer();
 			}else{
 				double angle = (Math.tanh((p.y-at.y)/(p.x-at.x)));
 				double mxd = p.x - at.x;
 				double myd = p.y - at.y;
 				if (mxd == 0)
 					angle = myd >= 0? Math.PI/2: -Math.PI/2;
 				else {
 					angle = Math.atan(myd/mxd);
 					if (mxd < 0)
 						angle += Math.PI;
 				}
 				if (angle < 0)
 					angle += 2*Math.PI;
 				double vy = at.speed*(Math.sin(angle));
 				double vx = at.speed*(Math.cos(angle));
 				if (!((npcBlocked(at, at.x1, at.y1)||npcBlocked(at, at.x2, at.y2)||npcBlocked(at, at.x3, at.y3)||npcBlocked(at, at.x4, at.y4)) || (tilesBlocked(at.x1,at.y1)||tilesBlocked(at.x2, at.y2)||tilesBlocked(at.x3, at.y3)||tilesBlocked(at.x4, at.y4)))){
 					at.x += vx;
 					at.y += vy;
 				}
 				collideTile(at, vx, vy);
 				npcCollision(at, vx, vy);
 			}
 		}
 	}
 	
 	public static void fillArray(NPC[] n, NPC at, int i){
 		if (at != null){
 			n[i] = at;
 			if (at.next != null)
 				fillArray(n, at.next, i+1);
 		}
 			
 	}
 	
 	public static void resetArray(NPC[] n, NPC at, int i){
 		if (i < n.length){
 			at = n[i];
 			if (i+1 < n.length)
 				resetArray(n, at.next, i+1);
 		}
 	}
 
 	public static boolean inNpc(NPC rec, double x, double y){
 		if ((x >= rec.x1 && x < rec.x4) && (y >= rec.y1 && y <= rec.y4)){	//Top left corner is inside other npc
 			return true;
 		}
 		return false;
 	}
 	
 	public static boolean npcBlocked(NPC from, double x, double y){
 		NPC[] n = new NPC[nNpc+1];
 		fillArray(n, root, 0);
 		for (int j = 0; j < n.length; j++){
 			if (n[j] != null){
 				NPC rec = n[j];
 				if (n[j] != from){
 					if ((x >= rec.x1 && x <= rec.x4) && (y >= rec.y1 && y <= rec.y4)){	//Top left corner is inside other npc
 						return true;
 					}
 				}
 			}
 		}
 		return false;
 	}
 	
 	public void npcCollision(NPC at, double vx, double vy){
 		try{
 			if (npcBlocked(at, at.x1, at.y1)||npcBlocked(at, at.x2, at.y2)||npcBlocked(at, at.x3, at.y3)||npcBlocked(at, at.x4, at.y4)){
 				int k = 0;
 				if (vx < 0){
 					//If one of their left side corners are inside an npc
 					while(k < at.speed && (npcBlocked(at, at.x1, at.y1)||npcBlocked(at, at.x2, at.y2))){
 						//Slowly move them out
 						at.x1++;
 						at.x2++;
 						k++;
 					}
 					if (npcBlocked(at, at.x1, at.y1) || npcBlocked(at, at.x2, at.y2) || tilesBlocked((int)(at.x1), (int)(at.y1)) || tilesBlocked((int)(at.x2), (int)(at.y2)))
 						at.resetHitBox();
 					at.resetCoords();
 				}else if (vx > 0){
 					k = 0;
 					while(k < at.speed && (npcBlocked(at, at.x3, at.y3) || npcBlocked(at, at.x4, at.y4))){
 						at.x3--;
 						at.x4--;
 						k++;
 					}
 					if (npcBlocked(at, at.x3, at.y3) || npcBlocked(at, at.x4, at.y4) || tilesBlocked((int)(at.x3), (int)(at.y3)) || tilesBlocked((int)(at.x4), (int)(at.y4)))
 						at.resetHitBox();
 					at.resetCoords();
 				}
 				if (vy < 0){
 					k = 0;
 					//Top left, top right
 					while(k < at.speed && (npcBlocked(at, at.x1, at.y1) || npcBlocked(at, at.x3, at.y3))){
 						at.y1++;
 						at.y3++;
 						k++;
 					}
 					if (npcBlocked(at, at.x1, at.y1) || npcBlocked(at, at.x3, at.y3) || tilesBlocked((int)(at.x1), (int)(at.y1)) || tilesBlocked((int)(at.x3), (int)(at.y3)))
 						at.resetHitBox();
 					at.resetCoords();
 				}else if (vy > 0){
 					k = 0;
 					while(k < at.speed && (npcBlocked(at, at.x2, at.y2) || npcBlocked(at, at.x4, at.y4))){
 						at.y2--;
 						at.y4--;
 						k++;
 					}
 					if (npcBlocked(at, at.x2, at.y2) || npcBlocked(at, at.x4, at.y4) || tilesBlocked((int)(at.x4), (int)(at.y4)) || tilesBlocked((int)(at.x2), (int)(at.y2)))
 						at.resetHitBox();
 					at.resetCoords();
 				}
 				try{ //TODO: failed collisions -> try/catch doesn't move entity back
 					if (vy > 0 && (!npcBlocked(at, (int)(at.x2), (int)(at.y2+at.speed)) && !npcBlocked(at, (int)(at.x4), (int)(at.y4+at.speed)) && (!tilesBlocked((int)(at.x2), (int)(at.y2+at.speed)) && !tilesBlocked((int)(at.x4), (int)(at.y4+at.speed)))))
 						at.y += at.speed;	//Its unblocked, move them down
 					else if (vy < 0 && (!npcBlocked(at, (int)(at.x1), (int)(at.y1-at.speed)) && !npcBlocked(at, (int)(at.x3), (int)(at.y3-at.speed)) && (!tilesBlocked((int)(at.x1), (int)(at.y1+at.speed)) && !tilesBlocked((int)(at.x3), (int)(at.y3+at.speed)))))
 						at.y -= at.speed;
 					//If they're moving right, check to their right
 					if (vx > 0 && (!npcBlocked(at, (int)(at.x3+at.speed), (int)(at.y3)) && !npcBlocked(at, (int)(at.x4+at.speed), (int)(at.y4)) && (!tilesBlocked((int)(at.x3), (int)(at.y3+at.speed)) && !tilesBlocked((int)(at.x4), (int)(at.y4+at.speed)))))
 						at.x += at.speed;	//It's open, move them there
 					else if (vx < 0 && (!npcBlocked(at, (int)(at.x1-at.speed), (int)(at.y1)) && !npcBlocked(at, (int)(at.x2-at.speed), (int)(at.y2)) && (!tilesBlocked((int)(at.x1), (int)(at.y1+at.speed)) && !tilesBlocked((int)(at.x2), (int)(at.y2+at.speed)))))
 						at.x -= at.speed;
 				}catch (Exception e){	e.printStackTrace();	}
 			}else{
 				double xc, yc, change = Math.PI/16;
 				for (double ang = change; ang < 2*Math.PI; ang += change) { //circular collisions
 					if (ang != 0 && ang != Math.PI/2 && ang != Math.PI && ang != 3*Math.PI/2 && ang != 2*Math.PI) {
 						xc = (21*Math.cos(ang));
 						yc = (21*Math.sin(ang));
 						if (inNpc(at, p.x+xc, p.y+yc)) {
 							at.x -= vx;
 							at.y -= vy;
 							if (System.currentTimeMillis() - at.attack > at.rate*1000){
 								Display.p.takeHealth(at.damage);
 								at.attack = System.currentTimeMillis();
 							}
 						}
 					}
 				}
 			}
 		}catch(Exception e){	e.printStackTrace();	}
 		
 	}
 	
 	public static void collideTile(NPC at, double vx, double vy){
 		try{
 			at.x1 = (int)at.x;	//top left
 			at.y1 = (int)at.y;
 			
 			at.x2 = at.x1;				//bottom left
 			at.y2 = at.y1 + at.height;
 			
 			at.x3 = at.x1 + at.width;			//top right
 			at.y3 = at.y1;
 			
 			at.x4 = at.x1 + at.width;			//bottom right
 			at.y4 = at.y1 + at.height;
 			//The tile they're attempting to move to is blocked
 			//if (Display.map[(int)(at.y1/48)][(int)(at.x1/48)] >= 40 || Display.map[(int)(at.y2/48)][(int)(at.x2/48)] >= 40){
 			//If any corner of the npc is on a blocked tile
 			if (tilesBlocked(at.x1,at.y1)||tilesBlocked(at.x2, at.y2)||tilesBlocked(at.x3, at.y3)||tilesBlocked(at.x4, at.y4)){
 				//For some reason, the npcs will move slightly left/right inside blocked tiles, this stops that.
 				try{
 					//If they're moving left
 					int k = 0;
 					if (vx < 0){
 						//If one of their left side corners are inside a blocked tile
 						while(k < Math.abs(vx) && (tilesBlocked((int)(at.x1), (int)(at.y1)) || tilesBlocked((int)(at.x2), (int)(at.y2)))){
 							//Slowly move them out
 							at.x1++;
 							at.x2++;
 							k++;
 						}
 						if (tilesBlocked((int)(at.x1), (int)(at.y1)) || tilesBlocked((int)(at.x2), (int)(at.y2)))
 							at.resetHitBox();
 						at.resetCoords();
 					}else if (vx > 0){
 						k = 0;
 						while(k < Math.abs(vx) && (tilesBlocked((int)(at.x3), (int)(at.y3)) || tilesBlocked((int)(at.x4), (int)(at.y4)))){
 							at.x3--;
 							at.x4--;
 							k++;
 						}
 						if (tilesBlocked((int)(at.x3), (int)(at.y3)) || tilesBlocked((int)(at.x4), (int)(at.y4)))
 							at.resetHitBox();
 						at.resetCoords();
 					}
 					if (vy < 0){
 						k = 0;
 						//Top left, top right
 						while(k < Math.abs(vy) && (tilesBlocked((int)(at.x1), (int)(at.y1)) || tilesBlocked((int)(at.x3), (int)(at.y3)))){
 							at.y1++;
 							at.y3++;
 							k++;
 						}
 						if (tilesBlocked((int)(at.x1), (int)(at.y1)) || tilesBlocked((int)(at.x3), (int)(at.y3)))
 							at.resetHitBox();
 						at.resetCoords();
 					}else if (vy > 0){
 						k = 0;
 						while(k < Math.abs(vy) && (tilesBlocked((int)(at.x2), (int)(at.y2)) || tilesBlocked((int)(at.x4), (int)(at.y4)))){
 							at.y2--;
 							at.y4--;
 							k++;
 						}
 						if (tilesBlocked((int)(at.x2), (int)(at.y2)) || tilesBlocked((int)(at.x4), (int)(at.y4)))
 							at.resetHitBox();
 						at.resetCoords();
 					}
 					
 				}catch (Exception e){	e.printStackTrace();	}
 				//If they're moving down, check the tile below them
 				try{ //TODO: failed collisions -> try/catch doesn't move entity back
 					if (vy > 0 && (!npcBlocked(at, (int)(at.x2), (int)(at.y2+at.speed)) && !npcBlocked(at, (int)(at.x4), (int)(at.y4+at.speed)) && (!tilesBlocked((int)(at.x2), (int)(at.y2+at.speed)) && !tilesBlocked((int)(at.x4), (int)(at.y4+at.speed)))))
 						at.y += at.speed;	//Its unblocked, move them down
 					else if (vy < 0 && (!npcBlocked(at, (int)(at.x1), (int)(at.y1-at.speed)) && !npcBlocked(at, (int)(at.x3), (int)(at.y3-at.speed)) && (!tilesBlocked((int)(at.x1), (int)(at.y1+at.speed)) && !tilesBlocked((int)(at.x3), (int)(at.y3+at.speed)))))
 						at.y -= at.speed;
 					//If they're moving right, check to their right
 					if (vx > 0 && (!npcBlocked(at, (int)(at.x3+at.speed), (int)(at.y3)) && !npcBlocked(at, (int)(at.x4+at.speed), (int)(at.y4)) && (!tilesBlocked((int)(at.x3), (int)(at.y3+at.speed)) && !tilesBlocked((int)(at.x4), (int)(at.y4+at.speed)))))
 						at.x += at.speed;	//It's open, move them there
 					else if (vx < 0 && (!npcBlocked(at, (int)(at.x1-at.speed), (int)(at.y1)) && !npcBlocked(at, (int)(at.x2-at.speed), (int)(at.y2)) && (!tilesBlocked((int)(at.x1), (int)(at.y1+at.speed)) && !tilesBlocked((int)(at.x2), (int)(at.y2+at.speed)))))
 						at.x -= at.speed;
 				}catch (Exception e){	e.printStackTrace();	}
 				at.resetHitBox();
 			}
 		}catch(Exception e){
 			e.printStackTrace();
 		}
 	}
 	
 	public static boolean tilesBlocked(double x, double y){
 		if ((int)(y/48) < Display.maph || (int)(x/48) < Display.mapw){
 			if (Display.map[(int)(y/48)][(int)(x/48)] >= 40 || Display.map[(int)(y/48)][(int)(x/48)] == -1)
 				return true;
 			return false;
 		}else
 			return true;
 		/*if (Display.map[(int)(y/48)][(int)(x/48)] >= 40 || Display.map[(int)((y+30)/48)][(int)(x/48)] >= 40 || Display.map[(int)(y/48)][(int)((x+30)/48)] >= 40 || Display.map[(int)((y+30)/48)][(int)((x+30)/48)] >= 40)
 			return true;
 		else
 			return false;*/
 	}
 	
 	public void drawModel(NPC at, Display m, Graphics g, boolean move){
 		if (getDist(at) <= Math.sqrt((m.w/2)*(m.w/2)+(m.h/2)*(m.h/2))){	//Within the screen of the user
 			at.dX = (m.w/2)-(int)(p.x-at.x);
 			at.dY = (m.h/2)-(int)(p.y-at.y);
 			Image img;
 			if (move)
 				moveNormal(at);
 			else {
 				try {
 					img = ImageIO.read(new File("npc/" + at.type + "/model.png"));
 					at.width = img.getWidth(m);
 					at.height = img.getHeight(m);
 					g.drawImage(img, at.dX, at.dY, at.dX+30, at.dY+30, 0, 0, 30, 30, m);
 				} catch (IOException e) {	e.printStackTrace();	}
 			}
 			if (System.currentTimeMillis() - at.update < 5000)
 				at.showHealth(g);
 		}
 		if (at.next != null)
 			drawModel(at.next, m, g, move);
 		else
 			return;
 	}
 	
 	public void drawWeapons(Display m, Graphics g){
 		for (int i = 0; i < wIndex; i++){
 			if (w[i].wid != -1){
 				try{
 					int x = (m.w/2)-(int)(p.x-w[i].x);
 					int y = (m.h/2)-(int)(p.y-w[i].y);
 					w[i].drawWeapon(g, m, x, y, true);
 				}catch(Exception e){	System.out.println(e);	}
 			}
 		}
 	}
 	
 	public static NPC getEne(NPC at, int index){
 		try{
 			if (index <= nNpc){
 				if (at.index != index){
 					return getEne(at.next, index);
 				}else
 					return at;
 			}else
 				return null;
 		}catch (Exception e){	return null;	}
 	}
 }
