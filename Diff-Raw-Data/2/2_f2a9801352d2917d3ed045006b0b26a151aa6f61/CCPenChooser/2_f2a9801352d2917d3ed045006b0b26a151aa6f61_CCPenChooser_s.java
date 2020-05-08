 package org.concord.waba.extra.ui;
 
 public class CCPenChooser extends waba.ui.Control{
 public int penIndex = -1;
 int dh;
 int	nPens = 8;
 	public void setRect(int x,int y,int w,int h){
 		super.setRect(x,y,16,h);
		dh = (int)((float)height / nPens + 0.5);
 	}
 
 	public void destroy(){
 	}
 
 	public void onPaint(waba.fx.Graphics gr){
 		int y = 0;
 		gr.setColor(0,0,0);
 		for(int i = 1; i <= nPens; i++){
 			y = dh*(i - 1) + dh /2 - i/2;
 			if(penIndex == i){
 				gr.drawRect(2,y - 2,width - 3,i+4);
 			}
 			for(int j = 0; j < i; j++){
 				gr.drawLine(4,y,width - 4,y);
 				y++;
 			}
 		}
 	}
 	public void onEvent(waba.ui.Event event){
 		if (event.type == waba.ui.PenEvent.PEN_DOWN){
 			waba.ui.PenEvent penEvent = (waba.ui.PenEvent)event;
 			penIndex = penEvent.y / dh + 1;
 			if(penIndex > nPens) penIndex = nPens;
 			repaint();
 		}
 	}
 	public int getChosenPenSize(){
 		return penIndex;
 	}
 }
