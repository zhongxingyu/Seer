 package ru.shalicikkomarov;
 
 
 import java.awt.*;
 import java.util.Random;
 
 public class Mouse {
 	public int imx, imy, mx, my, k, ncadr;
 	private long nowTime = 0, lastTime = 0;
	private SnakeElem snHead;
 	private boolean flag;
 	public boolean mouseDead;
 	Random r = new Random();
 	Point points[] = new Point[16 * 12];
 	public Mouse() {
 		k = 0;
 		for(int i = 0; i < 16; i++) {
 			for(int j = 0; j < 12; j++) {
 				if(SnakeField.state[i][j] != 1) {
 					points[k] = new Point(i, j);
 					k++;
 				}
 			}
 		}
 		k = r.nextInt(k);
 		mx = points[k].x * Cnst.FCELL;
 		my = points[k].y * Cnst.FCELL;
 		imx = 0;
 		imy = 0;
 		ncadr = 0;
 		flag = true;
 		mouseDead = false;
 	}
 	public void render(Graphics g, SnakeField io) {
 		nowTime = System.currentTimeMillis();
 		SnakeField.snake.render(g, io, this);
 		if(!mouseDead) {
 			g.drawImage(io.imgmouse, mx, my, mx + Cnst.FCELL, my + Cnst.FCELL, imx, imy, imx + 51, imy + 51, io);
 			if(nowTime - lastTime > 100) {
 				if(flag) ncadr++; else ncadr--;
 				imx = Math.min(8 * 52, ncadr * 52);
 				if(ncadr == 0 || ncadr == 25) flag = !flag;
 				lastTime = nowTime;
 			}
 		}
 		
 	}
 }
