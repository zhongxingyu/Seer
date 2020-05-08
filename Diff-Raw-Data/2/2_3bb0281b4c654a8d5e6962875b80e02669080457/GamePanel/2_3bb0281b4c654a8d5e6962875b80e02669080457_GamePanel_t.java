 package sikla;
 
 import java.io.DataInputStream;
 import java.awt.*;
 import java.awt.event.*;
 import javax.swing.*;
 
 public class GamePanel extends JPanel implements KeyListener {
 
 //-------------STATIC FIELDS---------------
 private static final int USIZE=32;
 static final byte SPACE=0, GOAL=1, BLOCK=2, GBLOCK=3, WALL=4,
 	PLAYS=0, WIN=1;
 SiklaMain par;
 
 //---------------STAGE VARIABLES----------------
 byte map[][], posx=0, posy=0; //map and position
 int moves=0; //moves made
 byte gnum=1, status=0, t1=0,t2=0;
 
 
 
 static Image im1 = (new ImageIcon("./sikla/data/sikla.png")).getImage();
 static Image im2 = (new ImageIcon("./sikla/data/wall.png")).getImage();
 static Image im3 = (new ImageIcon("./sikla/data/block.png")).getImage();
 static Image im4 = (new ImageIcon("./sikla/data/goal.png")).getImage();
 
 
 GamePanel(SiklaMain pr) {
 	super();
 	this.par = pr;
 	setBackground(new Color(230,230,230));
 	addKeyListener(this);
 }//Constructor
 
 
 
 public void paintComponent(Graphics g) {
 	super.paintComponent(g);
 	
 	if (status==PLAYS && map!=null) {
 	for (int i=0;i<map.length;++i) {
 	for (int j=0;j<map[0].length;++j) {
 	switch (map[i][j]) {
 	
 	case WALL:
 	g.drawImage(im2,USIZE*i,USIZE*j,this);
 	break;
 	
 	case BLOCK: case GBLOCK:
 	g.drawImage(im3,USIZE*i,USIZE*j,this);
 	break;
 	
 	case GOAL:
 	g.drawImage(im4,USIZE*i,USIZE*j,this);
 	break;
 	}//switch
 	}//for j
 	}//for i
 	
 	g.drawImage(im1,USIZE*posx,USIZE*posy,this);
 	}//if PLAYS
 	
 	else if (status==WIN) {
 	if (par.cstage<SiklaMain.stages || par.pmode) {
 	g.drawString("ΣΥΓΧΑΡΗΤΗΡΙΑ!! ΛΥΣΑΤΕ ΤΟ "+par.cstage+"ο ΕΠΙΠΕΔΟ!!!!",100,250);
 	g.drawString("ΚΑΝΑΤΕ "+moves+" ΚΙΝΗΣΕΙΣ!",100,270);
 	g.drawString("Πιέστε \"ENTER\" για να αποθηκεύσετε και να συνεχίσετε...",100,290);
 	}//if level
 	
 	else {
 	g.drawString("ΣΥΓΧΑΡΗΤΗΡΙΑ!! ΛΥΣΑΤΕ ΤΟ "+par.cstage+"ο ΕΠΙΠΕΔΟ!!!!",100,250);
 	g.drawString("ΚΑΝΑΤΕ "+moves+" ΚΙΝΗΣΕΙΣ!",100,270);
 	g.drawString("ΟΛΟΚΛΗΡΩΣΑΤΕ ΤΟ ΠΑΙΧΝΙΔΙ! ΕΥΧΑΡΙΣΤΟΥΜΕ ΠΟΥ ΠΑΙΞΑΤΕ ΜΕ ΤΟ Sikla!!!",100,290);
 	
 	}//else
 	}//else if WIN
 	
 	else if (map==null) {g.drawString("Διαλέξτε επίπεδο από το μενού \"Παιχνίδι\"!",100,250);}
 }//PAINT_COMPONENT
 
 
 
 private void moveSikla(int dir) {
 
 	//-------MOVE DOWN---------
 	if(dir==KeyEvent.VK_DOWN) {
 	t1=map[posx][posy+1];
 	
 	if (t1==SPACE || t1==GOAL) {posy++; moves++;} //1. just move
 	else if (t1==WALL) {} //2. just stay
 	
 	else if (t1==BLOCK) { //3. move block
 	t2=map[posx][posy+2];
 	
 	if (t2==SPACE) { //3a. move block to space
 	map[posx][posy+2]= BLOCK;
 	map[posx][posy+1] = SPACE;
 	posy++; moves++;
 	}//if t2=SPACE
 	else if (t2==GOAL) { //3b. move block to goal
 	map[posx][posy+2] = GBLOCK; gnum--;
 	map[posx][posy+1] = SPACE;
 	posy++; moves++;
 	}//else if t2=GOAL
 	}//else if t1=BLOCK
 	
 	else if (t1==GBLOCK) { //4. move gblock
 	t2=map[posx][posy+2];
 	
 	if (t2==SPACE) { //4a. move gblock to space
 	map[posx][posy+2] = BLOCK; gnum++;
 	map[posx][posy+1] = GOAL;
 	posy++; moves++;
 	}//if t2=SPACE
 	else if (t2==GOAL) { //4b. move gblock to goal
 	map[posx][posy+2] = GBLOCK;
 	map[posx][posy+1] = GOAL;
 	posy++; moves++;
 	}//else if t2=GOAL
 	}//else if t1=GBLOCK
 	}//if DOWN
 	
 	
 	//-------MOVE UP---------
 	else if(dir==KeyEvent.VK_UP) {
 	t1=map[posx][posy-1];
 	
 	if (t1==SPACE || t1==GOAL) {posy--; moves++;} //1. just move
 	else if (t1==WALL) {} //2. just stay
 	
 	else if (t1==BLOCK) { //3. move block
 	t2=map[posx][posy-2];
 	
 	if (t2==SPACE) { //3a. move block to space
 	map[posx][posy-2]= BLOCK;
 	map[posx][posy-1] = SPACE;
 	posy--; moves++;
 	}//if t2=SPACE
 	else if (t2==GOAL) { //3b. move block to goal
 	map[posx][posy-2] = GBLOCK; gnum--;
 	map[posx][posy-1] = SPACE;
 	posy--; moves++;
 	}//else if t2=GOAL
 	}//else if t1=BLOCK
 	
 	else if (t1==GBLOCK) { //4. move gblock
 	t2=map[posx][posy-2];
 	
 	if (t2==SPACE) { //4a. move gblock to space
 	map[posx][posy-2] = BLOCK; gnum++;
 	map[posx][posy-1] = GOAL;
 	posy--; moves++;
 	}//if t2=SPACE
 	else if (t2==GOAL) { //4b. move gblock to goal
 	map[posx][posy-2] = GBLOCK;
 	map[posx][posy-1] = GOAL;
 	posy--; moves++;
 	}//else if t2=GOAL
 	}//else if t1=GBLOCK
 	}//if UP
 	
 	
 	//-------MOVE LEFT---------
 	else if(dir==KeyEvent.VK_LEFT) {
 	t1=map[posx-1][posy];
 	
 	if (t1==SPACE || t1==GOAL) {posx--; moves++;} //1. just move
 	else if (t1==WALL) {} //2. just stay
 	
 	else if (t1==BLOCK) { //3. move block
 	t2=map[posx-2][posy];
 	
 	if (t2==SPACE) { //3a. move block to space
 	map[posx-2][posy]= BLOCK;
 	map[posx-1][posy] = SPACE;
 	posx--; moves++;
 	}//if t2=SPACE
 	else if (t2==GOAL) { //3b. move block to goal
 	map[posx-2][posy] = GBLOCK; gnum--;
 	map[posx-1][posy] = SPACE;
 	posx--; moves++;
 	}//else if t2=GOAL
 	}//else if t1=BLOCK
 	
 	else if (t1==GBLOCK) { //4. move gblock
 	t2=map[posx-2][posy];
 	
 	if (t2==SPACE) { //4a. move gblock to space
 	map[posx-2][posy] = BLOCK; gnum++;
 	map[posx-1][posy] = GOAL;
 	posx--; moves++;
 	}//if t2=SPACE
 	else if (t2==GOAL) { //4b. move gblock to goal
 	map[posx-2][posy] = GBLOCK;
 	map[posx-1][posy] = GOAL;
 	posx--; moves++;
 	}//else if t2=GOAL
 	}//else if t1=GBLOCK
 	}//if LEFT
 	
 	
 	//-------MOVE RIGHT---------
 	else if(dir==KeyEvent.VK_RIGHT) {
 	t1=map[posx+1][posy];
 	
 	if (t1==SPACE || t1==GOAL) {posx++; moves++;} //1. just move
 	else if (t1==WALL) {} //2. just stay
 	
 	else if (t1==BLOCK) { //3. move block
 	t2=map[posx+2][posy];
 	
 	if (t2==SPACE) { //3a. move block to space
 	map[posx+2][posy]= BLOCK;
 	map[posx+1][posy] = SPACE;
 	posx++; moves++;
 	}//if t2=SPACE
 	else if (t2==GOAL) { //3b. move block to goal
 	map[posx+2][posy] = GBLOCK; gnum--;
 	map[posx+1][posy] = SPACE;
 	posx++; moves++;
 	}//else if t2=GOAL
 	}//else if t1=BLOCK
 	
 	else if (t1==GBLOCK) { //4. move gblock
 	t2=map[posx+2][posy];
 	
 	if (t2==SPACE) { //4a. move gblock to space
 	map[posx+2][posy] = BLOCK; gnum++;
 	map[posx+1][posy] = GOAL;
 	posx++; moves++;
 	}//if t2=SPACE
 	else if (t2==GOAL) { //4b. move gblock to goal
 	map[posx+2][posy] = GBLOCK;
 	map[posx+1][posy] = GOAL;
 	posx++; moves++;
 	}//else if t2=GOAL
 	}//else if t1=GBLOCK
 	}//if RIGHT
 	
 }//MOVE_SIKLA
 
 
 int setStage(int i, boolean prs, Object[] ob) {
 	if (ob==null) return 1;
 	
 	par.cstage=i;
 	par.pmode = prs;
 	par.loadf = (prs)? par.pf : SiklaMain.fmain;
 	
 	map = (byte[][]) ob[0];
 	byte tpos[] = (byte[]) ob[1];
 	posx = tpos[0]; posy = tpos[1];
 	gnum = (Byte) ob[2];
 	moves=0;
 	
 	//Put the label
 	if(par.lb!=null)
 	 if (!par.pmode) {par.lb.setText("Επίπεδο "+i);}
 	 else {par.lb.setText("Προσωπικό επίπεδο "+i);}
 	
 	par.setMode(SiklaMain.PLAY);
 	status=GamePanel.PLAYS;
 	repaint();
 	return 0;
 }//SET_STAGE
 
 public void keyPressed(KeyEvent e) {
 	if (status==PLAYS) {
 	moveSikla(e.getKeyCode());
 	
 	if (gnum==0) {
 	status=WIN;
 	if (par.cstage!=SiklaMain.stages) par.avstages++;
 	}//gnum=0
 	
 	repaint();
 	}//if plays
 	
 	
 	
 	else if(status==WIN) {
 	int k = e.getKeyCode();
 	
 	if (k==KeyEvent.VK_ENTER) {
 		if (!par.pmode && par.cstage<SiklaMain.stages) FileManager.loadStage(par,par.cstage+1,par.pmode,true);
 		else if (par.pmode && par.cstage<par.pers) FileManager.loadStage(par,par.cstage+1,par.pmode,true);
 		else FileManager.loadStage(par,1,par.pmode,true);
 	 
		if (!par.pmode) FileManager.saveGame(par, false, true);
 	}//if enter
 	
 	}//else if win
 	
 }//KEY_PRESSED
 public void keyReleased(KeyEvent e) {}
 public void keyTyped(KeyEvent e) {}
 
 }//class
