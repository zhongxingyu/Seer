 /**
  * Q[vCʂ̊NX
  * Q[vC̊{̊֐Ƃ
  * ȃ[hɋʂ̕ϐ`Ă
  * ̃NXp邱ƂŔrIȒPɂȃ[hł
  */
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Image;
 import java.awt.Toolkit;
 import java.util.ArrayList;
 
 import java.util.Random;
 
 import javax.swing.ImageIcon;
 
 public class Field{
 	
 	public Field(){}
 	/**
 	 * Q[I[o[ɂȂuԂɐݒ肳ϐ
 	 * O͂Ƃ^Cgɖ߂Ƃ̃t[ɂȂ
 	 */
 	protected int gameOverFrame;
 	// GtFNgXg
 	protected ArrayList<Effect> effect = new ArrayList<Effect>();
 	// Q[Jn̏uԂ̃t[
 	protected int startFrame;
 	// XN[uԂ̃t[
 	protected int scrollFrame;
 	// }EXłłplAxAyW
 	protected int pressingPanelX;
 	protected int pressingPanelY;
 	private Panel pressingPanel;
 	// plւL[spaceĂ邩ǂ
 	private boolean cursorReleased;
 	// pϐ
 	protected Random random = new Random();
 	// tB[h̃plz
 	protected Panel[][] panel = new Panel[Data.ROW][Data.COL];
 	// J[\
 	protected Cursor cursor;
 	// VɏoẴpl
 	protected Panel[] newLine = new Panel[Data.COL];
 	// }EX̃{^Ă邩ǂ
 	private boolean mouseReleased;
 	// gC{^Ă邩ǂ
 	private boolean retryReleased;
 	// ^Cgɖ߂{^Ă邩ǂ
 	private boolean toTitleReleased;
 	// gC֐
 	public void retry(){
 		KeyStatus.setAll(false);
 		init();
 	}
 	// t[Ƃupdate֐
 	public void update(){
 		// Q[JnO牽Ȃ
 		if(startFrame > Data.frame) return;
 		// gCL[Ă΃tOZbg
 		if(!KeyStatus.retry) retryReleased = true;
 		// gCL[Ot[ł͗ĂāAt[ŉꂽꍇgC
 		if(retryReleased && KeyStatus.retry){
 			retryReleased = false;
 			retry();
 			return;
 		}
 		// ^CgɂĂgCl
 		if(!KeyStatus.toTitle) toTitleReleased = true;
 		if(toTitleReleased && KeyStatus.toTitle){
 			toTitleReleased = false;
 			Data.gameStatus = Data.TITLE;
 			Data.keyCansel = false;
 			Data.mouseCansel = false;
 			return;
 		}
 		// Q[I[o[茓Q[I[o[֐Ă
 		gameOver();
 		// Q[I[o[t[0oȂꍇQ[I[o[ƂɂȂ̂ňȍ~̏͂Ȃ
 		if(gameOverFrame != 0) return;
 		// J[\xAyWێĂ
 		int ty = cursor.getY();
 		int tx = cursor.getX();
 		// グx̓XRA/1000(ȉ؂̂)Ƃ
 		Data.lv = Data.score/1000;
 		// グx̍ő9
 		if(Data.lv > 9) Data.lv = 9;
 		if(!Data.mousePressed){
 			// }EX̃{^ĂȂ΃L[{[hɂJ[\쏈
 			
 			// }EXĂpl΂𗣂
 			if(pressingPanel != null) releasePanel();
 			// }EX{^ƂĂ
 			mouseReleased = true;
 			// J[\ĂL[ɑΉē
 			cursor.move();
 			// ւL[𗣂tO
 			if(!KeyStatus.change) cursorReleased = true;
 			// Ot[ɓւL[Ăčt[œւL[Ă΃plւ
 			if(KeyStatus.change && cursorReleased) swapping();
 		}else{
 			// }EX̃{^Ăꍇ̓}EXɂJ[\쏈
 			
 			// Ot[Ƀ}EX{^Ăplޏ
 			if(mouseReleased){
 				pressPanel();
 				mouseReleased = false;
 				cursor.set(Data.pressedX,Data.pressedY);
 			}
 			cursor.setX(Data.pressedX);
 			// łplAꂪĂ邩Apl炻𗣂
 			if(pressingPanel != null && (pressingPanel.isDeleting() || pressingPanel != panel[pressingPanelY][pressingPanelX])) releasePanel();
 			// ȊO̓pl𓮂
 			else panelMove();
 		}
 		// グ
 		scroll();
 		// tB[hɂ邷ׂẴplupdate
 		for(int i = 0; i < Data.ROW; i++){
 			for(int j = 0; j < Data.COL; j++){
 				if(panel[i][j] != null) panel[i][j].update();
 			}
 		}
 		// GtFNgupdate
 		for(int i = 0; i < effect.size(); i++){
 			effect.get(i).update();
 		}
 		// plƏpl̃tOZbg
 		setFallingFlag();
 		setDeleteFlag();
 		// ۂɃpl𗎂ƂA肷鏈
 		fallPanels();
 		deletePanels();
 		// plSɏ
 		endPanels();
 		// GtFNg
 		deleteEffect();
 		// SĂ̏̃J[\Ɉʒu𓾂
 		int cx = cursor.getX();
 		int cy = cursor.getY();
 		// J[\̈ʒuĂreplaypɋL^
 		if(ty != cy || tx != cx){
 			Data.replayCursorFrame.add(new Integer(Data.frame-startFrame));
 			Data.replayCursorX.add(new Integer(cx));
 			Data.replayCursorY.add(new Integer(cy));
 		}
 		// őAXV
 		if(Data.maxChain < Data.chain) Data.maxChain = Data.chain;
 		// A؂ꂽA0ɂ
 		if(chainResetable()) Data.chain = 0;
 	}
 	
 	// }EXɂăpl𓮂֐
 	private void panelMove(){
 		// ǂ̃tOƂǂɓ̃tO
 		boolean movable = true;
 		boolean left = true;
 		int x = -1;
 		int y = -1;
 		int newOffset = 0;
 		// łplȂAړA}EX̎wxɕωȂ珈I
 		if(pressingPanel == null) return;
 		if(pressingPanel.cMoving()) return;
 		if(pressingPanelX == Data.pressedX) return;
 		// łplEɓꍇ
 		if(pressingPanelX < Data.pressedX){
 			left = false;
 			x = pressingPanelX + 1;
 			y = pressingPanelY;
 			// newOffset͈ړplɐݒ肷鉡ړ̃ItZbg
 			newOffset = -Data.PANEL_SIZE;
 			// ړpl݂ÃplړĂŒȂړsƂ
 			if(panel[y][x] != null && (panel[y][x].cMoving() || panel[y][x].isDeleting())) movable = false;
 		}
 		// łplɓꍇ
 		if(pressingPanelX > Data.pressedX){
 			left = true;
 			x = pressingPanelX - 1;
 			y = pressingPanelY;
 			// newOffset͈ړplɐݒ肷鉡ړ̃ItZbg
 			newOffset = Data.PANEL_SIZE;
 			// ړpl݂ÃplړĂŒȂړsƂ
 			if(panel[y][x] != null && (panel[y][x].cMoving() || panel[y][x].isDeleting())) movable = false;
 		}
 		// ړ\Ȃړ
 		if(movable){
 			// ړƉEړŕς
 			if(left) swapPanel(x,y);
 			else swapPanel(pressingPanelX,pressingPanelY);
 			// łplxAyWXV
 			pressingPanelX = x;
 			pressingPanelY = y;
 		}
 	}
 	
 	// }EXŃpl𗣂֐
 	private void releasePanel(){
 		pressingPanel = null;
 		pressingPanelX = -1;
 		pressingPanelY = -1;
 	}
 	
 	// }EXŃplފ֐
 	private void pressPanel(){
 		int x = Data.pressedX;
 		int y = Data.pressedY;
 		if(x < 0 || x >= Data.COL) return;
 		if(y < 0 || y >= Data.ROW) return;
 		// plȂAړAĂŒȂ߂Ȃ
 		if(panel[y][x] == null || panel[y][x].cMoving() || panel[y][x].isDeleting()) return;
 		pressingPanel = panel[Data.pressedY][Data.pressedX];
 		pressingPanelX = x;
 		pressingPanelY = y;		
 	}
 
 	// GtFNg폜֐
 	private void deleteEffect(){
 		int i = 0;
 		while(i < effect.size()){
 			if(effect.get(i).ended()) effect.remove(i);
 			else i++;
 		}
 	}
 	
 	// gameoverp֐Ae[hŎ
	protected void gameOver(){assert(0);}
 	
 	// AZbg邩ǂ
 	private boolean chainResetable(){
 		// ĂplAA1ȉȂA1Ƃ
 		if(deletePanelExist()){
 			if(Data.chain <= 1){
 				Data.chain = 1;
 				return false;
 			}
 		}
 		// ÃplȂΘA̓Zbg
 		for(int i = 0; i < Data.ROW; i++){
 			for(int j = 0; j < Data.COL; j++){
 				if(panel[i][j] == null) continue;
 				if(panel[i][j].getConnected() != 0) return false;
 			}
 		}
 		return true;
 	}
 
 	// Vɏopl̃C
 	protected void createNewLine(){
 		int tmp;
 		int count;
 		for(int i = 0; i < Data.COL; i++){
 			while(true){
 				count = 1;
 				tmp = random.nextInt(Data.PANEL_NUMBER*Data.hard);
 				for(int j = i-1; j >= 0; j--){
 					if(tmp == newLine[j].getKind()) count++;
 					else break;
 				}
 				for(int j = Data.ROW-1; j >= 0; j--){
 					if(panel[j][i] != null && panel[j][i].getKind() == tmp) count++;
 					else break;
 				}
 				if(count < 3) break;
 			}
 			newLine[i] = new Panel(tmp);
 		}
 	}
 
 	// tB[ḧԏɃpl邩
 	protected boolean topExist(){
 		for(int i = 0; i < Data.COL; i++){
 			if(panel[0][i] != null) return true;
 		}
 		return false;
 	}
 	
 	// pl𗎂Ƃ֐
 	private void fallPanels(){
 		for(int i = Data.ROW-1; i >= 0; i--){
 			for(int j = 0; j < Data.COL; j++){
 				if(panel[i][j] == null) continue;
 				// tOĂāÃplcړ̃ItZbg0ŏĂȂAȂΗ(ItZbgݒ)
 				if(panel[i][j].isFalling()){
 					if(!panel[i][j].rMoving() && !panel[i][j].isDeleting() && panel[i+1][j] == null){
 						panel[i+1][j] = panel[i][j];
 						panel[i][j].setOffset(0,-Data.PANEL_SIZE);
 						panel[i][j] = null;
 					}
 				}
 			}
 		}
 	}
 
 	// J[\̈ʒũplւ֐
 	private void swapping(){
 		cursorReleased = false;
 		int x = cursor.getX();
 		int y = cursor.getY();
 		boolean leftMovable = true;
 		boolean rightMovable = true;
 		// J[\݂̍ړĂAĂ肵瓮Ȃ
 		if(panel[y][x] != null && (panel[y][x].cMoving() || panel[y][x].isDeleting())) leftMovable = false;
 		// J[\̉E݂ړĂAĂ肵瓮Ȃ
 		if(panel[y][x+1] != null && (panel[y][x+1].cMoving() || panel[y][x+1].isDeleting())) rightMovable = false;
 		// J[\̍AE̗𓮂ꍇ
 		if(leftMovable && rightMovable){
 			swapPanel(x,y);
 		}
 	}
 	
 	// ւpl̍̍WɂƂplۂɓւ֐
 	protected void swapPanel(int x, int y){
 		// ւƂL
 		Data.replaySwapFrame.add(new Integer(Data.frame-startFrame));
 		Data.replaySwapX.add(new Integer(x));
 		Data.replaySwapY.add(new Integer(y));
 		// ɃplȂΏI
 		if(panel[y][x] == null && panel[y][x+1] == null) return;
 		// ȂΉE𓮂
 		if(panel[y][x] == null){
 			panel[y][x+1].setOffset(Data.PANEL_SIZE,0);
 			panel[y][x] = panel[y][x+1];
 			panel[y][x+1] = null;
 			return;
 		}
 		// EȂ΍𓮂
 		if(panel[y][x+1] == null){
 			panel[y][x].setOffset(-Data.PANEL_SIZE,0);
 			panel[y][x+1] = panel[y][x];
 			panel[y][x] = null;
 			return;
 		}
 		// ȊO͓ւ
 		panel[y][x].setOffset(-Data.PANEL_SIZE,0);
 		panel[y][x+1].setOffset(Data.PANEL_SIZE,0);
 		Panel tmp = panel[y][x];
 		panel[y][x] = panel[y][x+1];
 		panel[y][x+1] = tmp;
 	}
 
 	// ͗tO̗Ăpl邩ǂ
 	private boolean fallingPanelExist(){
 		for(int i = 0; i < Data.ROW; i++){
 			for(int j = 0; j < Data.COL; j++){
 				if(panel[i][j] == null) continue;
 				if(panel[i][j].isFalling()) return true;
 			}
 		}
 	return false;
 	}
 
 	// Ãpl邩ǂ
 	private boolean connectPanelExist(){
 		for(int i = 0; i < Data.ROW; i++){
 			for(int j = 0; j < Data.COL; j++){
 				if(panel[i][j] == null) continue;
 				if(panel[i][j].getConnected() != 0) return true;
 			}
 		}
 		return false;
 	}
 	
 	// ړ̃pl邩ǂ
 	private boolean movingPanelExist(){
 		for(int i = 0; i < Data.ROW; i++){
 			for(int j = 0; j < Data.COL; j++){
 				if(panel[i][j] == null) continue;
 				if(panel[i][j].cMoving()) return true;
 			}
 		}
 		return false;
 	}
 	
 	// XN[~܂true
 	private boolean scrollStop(){
 		// Q[I[o[Ȃ~܂
 		if(gameOverFrame != 0) return true;
 		// ŏ̃plXN[ItZbg0łȂȂ~܂(Q[I[o[ɂȂ)
 		if(topExist() && Data.scrollOffset != 0) return true;
 		// XN[{^Ăꍇ
 		if(KeyStatus.scroll){
 			// ĂplĂplƂɍŏ̃pl݂Ύ~܂
 			if((fallingPanelExist() || connectPanelExist() || deletePanelExist()) && topExist()) return true;
 			// ȊO͓
 			else return false;
 		}
 		//XN[{^ĂȂ
 		for(int i = 0; i < Data.ROW; i++){
 			for(int j = 0; j < Data.COL; j++){
 				if(panel[i][j] == null) continue;
 				// Ăpl藎plƎ~܂
 				if(panel[i][j].isDeleting()) return true;
 				if(isFallable(j,i)) return true;
 				if(panel[i][j].isFalling()) return true;
 			}
 		}
 		return false;
 	}
 	
 	
 	// Ăpl邩ǂ
 	protected boolean deletePanelExist(){
 		for(int i = 0; i < Data.ROW; i++){
 			for(int j = 0; j < Data.COL; j++){
 				if(panel[i][j] == null) continue;
 				if(panel[i][j].isDeleting()) return true;
 			}
 		}
 		return false;
 	}
 	
 	// VCtB[hɏo֐(܂ʏ̐Fɂ֐)
 	protected void appearNewLine(){
 		// XN[ɍ킹ăJ[\グ
 		cursor.moveUp();
 		pressingPanelY -= 1;
 		for(int i = 1; i < Data.ROW; i++){
 			for(int j = 0; j < Data.COL; j++){
 			panel[i-1][j] = panel[i][j];
 			}
 		}
 		for(int i = 0; i < Data.COL; i++){
 			panel[Data.ROW-1][i] = newLine[i];
 		}
 		// VȃC
 		createNewLine();
 	}
 
 	// XN[֐
 	protected void scroll(){
 		// XN[~܂Ȃ炱̃t[ŃXN[ƂɂAԂZbg
 		if(scrollStop()){
 			scrollFrame = Data.frame;
 			return;
 		}
 		// ÕXN[莞Ԍo班ɂ
 		if(scrollFrame + (10 - Data.lv) * 2 <= Data.frame || KeyStatus.scroll){
 			// vCpL^
 			Data.replayScrollFrame.add(new Integer(Data.frame-startFrame));
 			// ItZbg
 			Data.scrollOffset = (Data.scrollOffset + Data.SCROLL_UNIT*Data.hard) % Data.PANEL_SIZE;
 			// 񕪋VȃCo
 			if(Data.scrollOffset == 0){
 				appearNewLine();
 			}
 			// XN[t[ۑ
 			scrollFrame = Data.frame;
 		}
 	}
 	
 	// plSɏ
 	private boolean endPanels(){
 		boolean connection;
 		for(int j = 0; j < Data.COL;j++){
 			connection = false;
 			for(int i = Data.ROW-1; i >= 0; i--){
 				if(panel[i][j] == null){
 					connection = false;
 					continue;
 				}
 				// plAtO𗧂Ẵpl
 				if(panel[i][j].end()){
 					connection = true;
 					panel[i][j] = null;
 				}else{
 					// plĘAtOΘAݒ
 					if(connection) panel[i][j].setConnected(Data.frame);
 				}
 			}
 		}
 		// ȂtrueԂĂ邩oĂȂAdlύXɂ́B͎gĂȂ͂
 		return true;
 	}
 	
 	// pl̕\(ۂ͈ړłȂ̃pl͗ȂԂɂ)
 	private void deletePanels(){
 		for(int i = 0; i < Data.ROW; i++){
 			for(int j = 0; j < Data.COL; j++){
 				if(panel[i][j] == null) continue;
 				if(panel[i][j].isDeleted()){
 					if(panel[i][j].getKind() != -1){
 						panel[i][j].setKind(-1);
 						Data.score += 10;
 					}
 				}
 			}
 		}
 	}
 	
 	// 
 	public void init(){
 		Data.replayScrollFrame.clear();
 		Data.replaySwapFrame.clear();
 		Data.replaySwapX.clear();
 		Data.replaySwapY.clear();
 		Data.replayCursorFrame.clear();
 		Data.replayCursorX.clear();
 		Data.replayCursorY.clear();
 		gameOverFrame = 0;
 		cursor = new Cursor(Data.INIT_CURSOR_X,Data.INIT_CURSOR_Y);
 		cursor.setLoopAble(false);
 		Data.cursorMaxX = Data.COL-2;
 		Data.cursorMaxY = Data.ROW-1;
 		pressingPanel = null;
 		pressingPanelX = -1;
 		pressingPanelY = -1;
 		mouseReleased = true;
 		Data.score = 0;
 		Data.chain = 0;
 		Data.maxDelete = 0;
 		Data.maxChain = 0;
 		scrollFrame = Data.frame;
 		cursorReleased = true;
 		toTitleReleased = false;
 		retryReleased = false;
 		Data.scrollOffset = 0;
 	}
 	
 	// ΉxAyW̃plplǂ
 	protected boolean isDeletable(int x, int y){
 		if(panel[y][x].isFalling() || isFallable(x,y) || panel[y][x].isDeleting() || panel[y][x].cMoving()) return false;
 		int count_row = 1;
 		int count_col = 1;
 		int myKind = panel[y][x].getKind();
 		for(int u = y-1; u >= 0; u--){
 			if(panel[u][x] == null) break;
 			if(panel[u][x].isFalling() || isFallable(x,u) || panel[u][x].isDeleting() || panel[u][x].cMoving()) break;
 			if(panel[u][x].getKind() != myKind) break;
 			else count_row++;
 		}
 		for(int d = y+1; d < Data.ROW; d++){
 			if(panel[d][x] == null) break;
 			if(panel[d][x].isFalling() || isFallable(x,d) || panel[d][x].isDeleting() || panel[d][x].cMoving()) break;
 			if(panel[d][x].getKind() != myKind) break;
 			else count_row++;
 		}
 		for(int l = x-1; l >= 0; l--){
 			if(panel[y][l] == null) break;
 			if(panel[y][l].isFalling() || isFallable(l,y) || panel[y][l].isDeleting() || panel[y][l].cMoving()) break;
 			if(panel[y][l].getKind() != myKind) break;
 			else count_col++;
 		}
 		for(int r = x+1; r < Data.COL; r++){
 			if(panel[y][r] == null) break;
 			if(panel[y][r].isFalling() || isFallable(r,y) || panel[y][r].isDeleting() || panel[y][r].cMoving()) break;
 			if(panel[y][r].getKind() != myKind) break;
 			else count_col++;
 		}
 		// 3ȏȂĂΏplȂ̂true
 		if(count_row>=3 || count_col>=3) return true;
 		else return false;
 	}
 	
 	// tO𗧂Ă
 	private void setDeleteFlag(){
 		boolean first = true;
 		boolean firstChain = true;
 		int minI = Data.ROW;
 		int minJ = Data.COL;
 		int count = 0;
 		int max = 0;
 		int tmp = 0;
 		boolean[][] flag = new boolean[Data.ROW][Data.COL];
 		for(int i = 0; i < Data.ROW; i++){
 			for(int j = 0; j < Data.COL; j++){
 				if(panel[i][j] == null){
 					flag[i][j] = false;
 					continue;
 				}
 				if(isDeletable(j,i)){
 					if(minI > i) minI = i;
 					if(minJ > j) minJ = j;
 					flag[i][j] = true;
 					max++;
 				}else{
 					flag[i][j] = false;
 				}
 			}
 		}
 		// ̍XV
 		if(Data.maxDelete < max) Data.maxDelete = max;
 		count = max-1;
 		for(int i = Data.ROW-1; i >= 0; i--){
 			for(int j = Data.COL-1; j >= 0; j--){
 				// ztO
 				if(flag[i][j]){
 					// GtFNgZbg
 					effect.add(new Effect(Data.DELETE_EFFECT,j,i,panel[i][j].getKind()));
 					// ̃GtFNgƃ|Cgǉ
 					if(first && max > 3){
 						effect.add(new Effect(Data.SAME_EFFECT,minJ,minI,max));
 						Data.score += max*max*5;
 					}
 					// ÃGtFNgƃ|Cgǉ
 					if(firstChain && panel[i][j].getConnected() != 0 && Data.chain > 0){
 						firstChain = false;
 						Data.chain++;
 						effect.add(new Effect(Data.CHAIN_EFFECT,minJ,minI,Data.chain));
 						Data.score += Data.chain*Data.chain*10;
 					}
 					// ŏ̃tO(|CgǉƂGtFNgdȂ)
 					first = false;
 					if(Data.chain == 0) Data.chain = 1;
 					// t[Zbg(Ԃɏ悤ɂ邽countƂmaxƂg)
 					panel[i][j].setDeleteFrame(count,max-1);
 					count--;
 				}else{
 					// Ȃpl̏ꍇŘAtOĂꍇ
 					if(panel[i][j] != null && panel[i][j].getConnected() < 0){
 						// ړĂȂplł
 						if(!panel[i][j].cMoving() && !panel[i][j].rMoving()){
 							// ŉɂ艺̃pl̘AtOȂAĂŒĂȂĂŒłȂΘAtOZbg
 							if((i+1==Data.ROW || (panel[i+1][j] != null && (panel[i+1][j].getConnected() == 0 || panel[i+1][j].isDeleting()))) && !panel[i][j].isDeleting()){
 								panel[i][j].setConnected(0);
 							}
 						}
 					}
 				}
 			}
 		}
 	}
 	
 	// xAyWplǂ
 	protected boolean isFallable(int x, int y){
 		int tmp = panel[y][x].getConnected();
 		// ԉ̃plɂ
 		if(y == Data.ROW-1){
 			// ĂŒȂtrue
 			if(panel[Data.ROW-1][x].rMoving()) return true;
 			else{
 				// AtOĂΒnuԂ̃t[}CiXŃZbg
 				if(tmp != 0) panel[y][x].setConnected(-Data.frame);
 				return false;
 			}
 		}else{
 			// ̃Q[ł̓pl͎΂߂ɓƂ͖̂ŁAɓĂΗĂȂ
 			if(panel[y][x].cMoving()){
 				// AtOĂΒnuԂ̃t[}CiXŃZbg
 				if(tmp != 0) panel[y][x].setConnected(-Data.frame);
 				return false;
 			}
 			// ĂŒȂtrue
 			if(panel[y][x].rMoving()){
 				return true;
 			}
 			// ̃pl݂ȂΗ̂true
 			if(panel[y+1][x] == null){
 				return true;
 			}
 			boolean ret = panel[y+1][x].isFalling();
 			// ̃plplŘAtOĂΒnuԂ̃t[}CiXŃZbg
 			if(!ret && tmp != 0) panel[y][x].setConnected(-Data.frame);
 			// ̃pl̗锻ƓɂȂ
 			return ret;
 		}
 	}
 	
 	// tOZbg
 	public void setFallingFlag(){
 		int tmp = 0;
 		for(int i = Data.ROW-1; i>=0; i--){
 			for(int j = 0; j < Data.COL; j++){
 				if(panel[i][j]==null) continue;
 				// ɓtmp͉̃plSɏƂ̃t[(ĂȂ0)
 				tmp = panel[i][j].getConnected();
 				// plł̃plSɏĂ莞Ԍo߂ĂΗtO𗧂Ă
 				panel[i][j].setFalling(isFallable(j,i) && (tmp == 0 || (tmp+Data.DELETE_RAG/Data.hard<=Data.frame)));
 			}
 		}
 	}
 	
 	// 낢`
 	public void draw(Graphics g){
 		if(gameOverFrame == 0){
 			for(int i = 0; i < Data.ROW; i++){
 				for(int j = 0; j < Data.COL; j++){
 					if(panel[i][j] == null) continue;
 					panel[i][j].draw(g,j,i);
 				}
 			}
 			for(int i = 0; i < Data.COL; i++){
 				newLine[i].draw(g,i,Data.ROW);
 			}
 			cursor.draw(g,Data.ENDLESS);
 		}
 		if(Data.hard==1){
 			g.drawImage(Data.image.fieldImage,0,0,Data.WIDTH*Data.zoom,Data.HEIGHT*Data.zoom,0,0,Data.WIDTH,Data.HEIGHT,null);
 		}else if(Data.hard==2){
 			g.drawImage(Data.image.hardFieldImage,0,0,Data.WIDTH*Data.zoom,Data.HEIGHT*Data.zoom,0,0,Data.WIDTH,Data.HEIGHT,null);
 		}
 		for(int i = 0; i < effect.size(); i++){
 			effect.get(i).draw(g);
 		}
 		int space_x = 20;
 		int space_y = 7;
 		Data.setFont(g,Data.SCORE_FONT);
 		g.drawString(Data.score+"",(Data.SCORE_X+space_x)*Data.zoom,(Data.SCORE_Y-space_y)*Data.zoom);
 		g.drawString(Data.lv+"",(Data.LV_X+space_x)*Data.zoom,(Data.LV_Y-space_y)*Data.zoom);
 		g.drawString(Data.maxChain+"",(Data.MAX_CHAIN_X+space_x)*Data.zoom,(Data.MAX_CHAIN_Y-space_y)*Data.zoom);
 		g.drawString(Data.maxDelete+"",(Data.MAX_DELETE_X+space_x)*Data.zoom,(Data.MAX_DELETE_Y-space_y)*Data.zoom);
 		if(gameOverFrame != 0){
 			g.drawString("Game Over",(Data.WIDTH/2-100)*Data.zoom,(Data.HEIGHT/2)*Data.zoom);
 		}
 	}
 }
