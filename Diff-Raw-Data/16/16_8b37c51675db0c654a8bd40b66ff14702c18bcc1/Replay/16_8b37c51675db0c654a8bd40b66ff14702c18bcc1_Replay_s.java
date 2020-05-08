 /**
  * vCpNX
  */
 import java.awt.Graphics;
 import java.awt.Image;
 import java.awt.Color;
 
 public class Replay extends Field{
 	
 	public Replay(){}
 	// Xe[WNÃvCpf[^
 	private int clearLine;
 	private int upNo;
 	
 	private int mode;
 	private boolean end;
 	
 	// vCׂ̍f[^indexƔz
 	private int scrollIndex;
 	private int swapIndex;
 	private int cursorIndex;
 	private long rseed;
 	private int[] rscrollFrame;
 	private int[] rswapFrame;
 	private int[] rswapX;
 	private int[] rswapY;
 	private int[] rcursorFrame;
 	private int[] rcursorX;
 	private int[] rcursorY;
 	
 	// vCIĂtrue
 	public boolean end(){
 		return end;
 	}
 	
 	// Q[I[o[p֐Be͊e[hɏ
 	protected void gameOver(){
 		switch(mode){
 		case Data.ENDLESS:
 			break;
 		case Data.SCORE_ATTACK:
 			if(startFrame + Data.TIME_LIMIT <= Data.frame){
 				if(gameOverFrame == 0){
 					gameOverFrame = Data.frame + 60;
 					Data.keyCansel = true;
 					Data.mouseCansel = true;
 				}
 				if(gameOverFrame != 0 && gameOverFrame <= Data.frame){
 					end = true;
 					Data.keyCansel = false;
 					Data.mouseCansel = false;
 				}
 			}
 			break;
 		case Data.STAGE_CLEAR:
 			boolean tmp = true;
 			if(clearLine >= 0){
 				for(int i = 0; i < clearLine; i++){
 					for(int j = 0; j < Data.COL; j++){
 						if(panel[i][j] != null) tmp = false;
 					}
 				}
 				if(tmp && !deletePanelExist()){
 					for(int i = 0; i < Data.ROW; i++){
 						for(int j = 0; j < Data.COL; j++){
 							if(panel[i][j] == null) continue;
 							if(isFallable(j,i)) tmp = false;
 						}
 					}
 					if(tmp){
 						if(gameOverFrame == 0){
 							gameOverFrame = Data.frame + 60;
 							Data.keyCansel = true;
 							Data.mouseCansel = true;
 						}
 						if(gameOverFrame != 0 && gameOverFrame <= Data.frame){
 							end = true;
 							Data.keyCansel = false;
 							Data.mouseCansel = false;
 						}
 					}
 				}
 			}
 			break;
 		}
 		if(topExist() && Data.scrollOffset != 0){
 			if(gameOverFrame == 0){
 				gameOverFrame = Data.frame + 60;
 				Data.keyCansel = true;
 				Data.mouseCansel = true;
 			}
 			if(gameOverFrame != 0 && gameOverFrame <= Data.frame){
 				end = true;
 				Data.keyCansel = false;
 				Data.mouseCansel = false;
 			}
 		}
 	}
 	
 	// Xe[WNÂ߂ɃI[o[Ch
 	protected void appearNewLine(){
 		if(rcursorFrame.length == 0) cursor.moveUp();
 		for(int i = 1; i < Data.ROW; i++){
 			for(int j = 0; j < Data.COL; j++){
 			panel[i-1][j] = panel[i][j];
 			}
 		}
 		for(int i = 0; i < Data.COL; i++){
 			panel[Data.ROW-1][i] = newLine[i];
 		}
 		createNewLine();
 		if(mode == Data.STAGE_CLEAR){
 			upNo++;
 			if(upNo == Data.CLEAR_LINE){
 				clearLine = Data.ROW;
 			}else{
 				if(clearLine > 0) clearLine--;
 			}
 		}
 	}
 	
 	// p֐
 	public void init(){
 		super.init();
 		end = false;
 		random.setSeed(rseed);
 		scrollIndex = 0;
 		swapIndex = 0;
 		cursorIndex = 0;
 		upNo = 0;
 		clearLine = -1;
 		Data.keyCansel = true;
 		Data.mouseCansel = true;
 		startFrame = Data.frame + Data.fps * 3;
 		for(int i = 0; i < Data.ROW; i++){
 			for(int j = 0; j < Data.COL; j++){
 				panel[i][j] = null;
 			}
 		}
 		for(int i = Data.ROW-1; i >= 0; i--){
 			for(int j = 0; j < Data.COL; j++){
 				if(i < Data.ROW/2+2) continue;
 				else{
 					do{
 						panel[i][j] = new Panel(random.nextInt(Data.PANEL_NUMBER*Data.hard));
 					}while(isDeletable(j,i));
 				}
 			}
 		}
 		createNewLine();
 	}
 	
 	// Ή閼OA[h̃vCf[^ǂ
 	public void setStatus(String name, int mode){
 		this.mode = mode;
 		ScoreIO io = new ScoreIO();
 		io.readReplayData(name);
 		rseed = io.getSeed(mode);
 		rscrollFrame = io.getScrollFrame(mode);
 		rswapFrame = io.getSwapFrame(mode);
 		rswapX = io.getSwapX(mode);
 		rswapY = io.getSwapY(mode);
 		rcursorFrame = io.getCursorFrame(mode);
 		rcursorX = io.getCursorX(mode);
 		rcursorY = io.getCursorY(mode);
 	}
 	
 	// update
 	public void update(){
		J[\ړ
 		if(cursorIndex < rcursorFrame.length){
 			if(Data.frame-startFrame == rcursorFrame[cursorIndex]){
 				int tx = rcursorX[cursorIndex];
 				int ty = rcursorY[cursorIndex];
 				cursor.set(tx,ty);
 				cursorIndex++;
 			}
 		}
 		// plړ
 		if(swapIndex < rswapFrame.length){
 			if(Data.frame-startFrame == rswapFrame[swapIndex]){
 				int tx = rswapX[swapIndex];
 				int ty = rswapY[swapIndex];
 				swapPanel(tx,ty);
 				swapIndex++;
 				if(rcursorFrame.length == 0) cursor.set(tx,ty);
 			}
 		}
 		super.update();
 	}
 	
 	// vCf[^ɏăXN[̂ŃI[o[Ch
 	protected void scroll(){
 		if(scrollIndex >= rscrollFrame.length){
 			return;
 		}
 		if(Data.frame-startFrame != rscrollFrame[scrollIndex]){
 			return;
 		}
 		Data.scrollOffset = (Data.scrollOffset + Data.SCROLL_UNIT*Data.hard) % Data.PANEL_SIZE;
 		if(Data.scrollOffset == 0){
 			appearNewLine();
 		}
 		scrollIndex++;
 	}
 	
 	// `
 	public void draw(Graphics g){
 		super.draw(g);
 		if(clearLine > 0){
 			g.setColor(Color.RED);
 			g.fillRect(Data.FIELD_START_X*Data.zoom,(Data.FIELD_START_Y+clearLine*Data.PANEL_SIZE-Data.scrollOffset-2)*Data.zoom,(Data.PANEL_SIZE*Data.COL)*Data.zoom,5*Data.zoom);
 		}
 		int space_x = 20;
 		int space_y = 7;
 		switch(mode){
 		case Data.SCORE_ATTACK:
 			if(startFrame <= Data.frame){
 				if(gameOverFrame == 0) Data.time = (startFrame+Data.TIME_LIMIT-Data.frame)/Data.fps;
 				g.drawString((Data.time/60)+":"+String.format("%1$02d",(Data.time%60)),(Data.REST_X+space_x)*Data.zoom,(Data.REST_Y-space_y)*Data.zoom);
 			}else{
 				g.drawString(((startFrame-Data.frame)/Data.fps+1)+"",Data.WIDTH/2*Data.zoom,Data.HEIGHT/2*Data.zoom);
 				Data.time = Data.TIME_LIMIT/Data.fps;
 				g.drawString((Data.time/60)+":"+String.format("%1$02d",(Data.time%60)),(Data.REST_X+space_x)*Data.zoom,(Data.REST_Y-space_y)*Data.zoom);
 			}
 			break;
 		case Data.ENDLESS:
 		case Data.STAGE_CLEAR:
 			Data.setFont(g,Data.SCORE_FONT);
 			if(startFrame <= Data.frame){
 				if(gameOverFrame == 0) Data.time = (Data.frame-startFrame)/Data.fps;
 				g.drawString((Data.time/60)+":"+String.format("%1$02d",(Data.time%60)),(Data.REST_X+space_x)*Data.zoom,(Data.REST_Y-space_y)*Data.zoom);
 			}else{
 				g.drawString(((startFrame-Data.frame)/Data.fps+1)+"",Data.WIDTH/2*Data.zoom,Data.HEIGHT/2*Data.zoom);
 				g.drawString("0:00",(Data.REST_X+space_x)*Data.zoom,(Data.REST_Y-space_y)*Data.zoom);
 			}
 			break;
 		}
 	}
 }
