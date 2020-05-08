 package gameLogic;
 
 import gui.MainClient;
 import gui.TableWindow;
 
 import java.util.Vector;
 
 import javax.swing.SwingUtilities;
 
 import aurelienribon.slidinglayout.SLAnimator;
 import aurelienribon.slidinglayout.SLConfig;
 import aurelienribon.slidinglayout.SLPanel;
 import aurelienribon.tweenengine.TweenManager;
 
 /**
  * @author Sameer
  * Client sided setTable does the following:
  * 		-Generates and manages cards given by server.
  * 		-Keeps track of selected cards
  * 		-Animates cards
  */
 public class SetTable{
 
 	private static Vector<SetCard> onTable = null;
 	public static  Vector<SetCard> selectedCards = null;
 	public SLPanel tableView = null;
 	private static TweenManager SLtweenManager = null;
 	
 	public SetTable() {
 		onTable = new Vector<SetCard>(15);
 		selectedCards = new Vector<SetCard>(3);
 		tableView = new SLPanel();
 		SLtweenManager = SLAnimator.createTweenManager();
 		tableView.setTweenManager(SLtweenManager);
 	}
 	
 
 	/*Adds deck card to ontable Vector removes from deck*/
 	public void addToTable(int cardNum){
 		onTable.add(new SetCard(cardNum));
 	}
 	
 	/*Getter Functions*/
 	public int tableSize() 	{ return onTable.size();}
 	public SetCard getTableCard(int pos){ 	return onTable.elementAt(pos);	}
 	public SetCard rmTableCard(int pos){	return onTable.remove(pos);}
 	
 	
 	/*
 	 * Returns the default layout of 3x4 grid. 
 	 * Sets the appropriate action for associated grid location. 
 	 */
 	public SLConfig defaultLayout(){
 		SLConfig mainCfg = new SLConfig(tableView)
 		.gap(20, 20)
 		.row(150).row(150).row(150).col(100).col(100).col(100).col(100).col(100);
 //		.row(1f).row(1f).row(1f).col(1f).col(1f).col(1f).col(1f);
 		for(int r = 0; r<3;r++){
 			for(int c = 0;c<4; c++ ){
 				mainCfg.place(r,c,onTable.elementAt(r*4+c));
 			}
 		}
 		return mainCfg;
 	}
 	
 	/* Selecting operations */
 	public static void addSelected(SetCard setCard){
 		if (selectedCards.size() == 2){
 			selectedCards.add(setCard);
 			if(isSet()){
 				TableWindow.sendSet(selectedCards.elementAt(0).getCardNum(), selectedCards.elementAt(1).getCardNum(),selectedCards.elementAt(2).getCardNum());
 			}
 			else{
 				MainClient.sendMessage("X");
 				// TODO: Notify player of incorrect selection
 				SwingUtilities.invokeLater(new Runnable() {
 					@Override
 					public void run() {
 						clearSelected();
 					};
 				});
 			}
 		}
 		else if (selectedCards.size() < 2 ){
 			selectedCards.add(setCard);
 		} 
 		else{
 			clearSelected();
 			selectedCards.add(setCard);
 		}
 	}
 			
 			
 	
 	public static void rmSelected(SetCard setCard){
 		selectedCards.remove(setCard);
 	}
 	
 	public static void clearSelected(){
//		for(int i = 0; i<selectedCards.size(); i++){
		while(selectedCards.size()!= 0 ){
			System.out.println(selectedCards.elementAt(0).toString());
			selectedCards.elementAt(0).unSelect();		
 		}
 	}
 
 	public static boolean isSet(){
 			
 		int colorCheck, numberCheck, shapeCheck, shadeCheck;
 		SetCard a = selectedCards.elementAt(0);
 		SetCard b = selectedCards.elementAt(1);
 		SetCard	c = selectedCards.elementAt(2);
 
 		colorCheck =  (a.attr(0) + b.attr(0) + c.attr(0)) % 3;
 		numberCheck = (a.attr(1) + b.attr(1) + c.attr(1)) % 3;
 		shapeCheck =  (a.attr(2) + b.attr(2) + c.attr(2)) % 3;
 		shadeCheck =  (a.attr(3) + b.attr(3) + c.attr(3)) % 3;
 		
 		
 		if (colorCheck == 0 && numberCheck == 0 && shapeCheck == 0 && shadeCheck == 0)
 		    return true;
 		else
 		    return false;
 	}
 	
 	
 	/*
 	 * Adds the last card in onTable vector (index= 12) to the new position. 
 	 * Unused at the moment
 	 */
 	@SuppressWarnings("unused")
 	private void tableReplace(int row, int col){
 		SetCard newCard = onTable.remove(12);
 		int vecPos = row*4+col;
 		onTable.setElementAt(newCard, vecPos);
 	}
 	
 	
 	/*Disables actions*/
 	@SuppressWarnings("unused")
 	private void disableActions() {
 		for(int i = 0; i<onTable.size(); i++){
 			onTable.elementAt(i).disableClick();
 		}
 	}
 	/*Enables Actions*/
 	@SuppressWarnings("unused")
 	private void enableActions(){
 		for(int i = 0; i<onTable.size(); i++){
 			onTable.elementAt(i).disableClick();
 		}
 	}
 
 	
 }
 
 
 
 /* OLD 
  * Deck Functions:
  * Creates New deck, clears on table vector
  * Get deckSize	
  * No need in client
  */
 //public void  newDeck(){
 //	deck = new HashMap<Integer,SetCard>(81);
 //	onTable = new Vector<SetCard>();
 //	SetCard tmpCard = null;
 //    for (int number=0; number<3; number++) {
 //        for (int symbol=0; symbol<3; symbol++) {
 //            for (int shading=0; shading<3; shading++) {
 //                for (int color=0; color<3; color++) {
 //                	tmpCard = new SetCard(number, symbol, shading, color);
 //                	deck.put(tmpCard.getCardNum(), tmpCard);
 //                }
 //            }
 //        }
 //    }
 ////    Collections.sort(deck);
 //}
 
 
 
 
 
 
 
 
 /*Maybe used some day*/
 
 
 //
 //private Runnable animationCreate( final int row, final int col){
 //	Runnable pAction = new Runnable() {
 //		
 //		@Override
 //		public void run() {
 //			disableActions();
 //			drawCard();
 //			//Animation	
 ////            Tween.to(super, cardAccessor.SCALE_XY, 0.5f)
 ////                .ease(Back.INOUT)
 ////                .target(2, 2)
 ////                .repeatYoyo(-1, 0.6f)
 ////                .start(tweenManager);
 //			
 //			tableReplace(row,col);
 //			enableActions();
 //			
 ////				.push(new SLKeyframe(createLayout(row, col), 1.6f)
 //////					.setEndSide(SLSide.BOTTOM, onTable.elementAt(1))
 //////					.setCallback(new SLKeyframe.Callback() {@Override public void done() {
 //////						onTable.elementAt(0).setAction(p1BackAction);
 //////						onTable.elementAt(0).enableAction();
 //////					}})
 ////					)
 ////				.play();	
 ////			defaultLayout();
 ////			tableView.createTransition()
 ////			.push(new SLKeyframe(defaultLayout(), 1.6f)
 //////				.setEndSide(SLSide.BOTTOM, onTable.elementAt(1))
 //////				.setCallback(new SLKeyframe.Callback() {@Override public void done() {
 //////					onTable.elementAt(0).setAction(p1BackAction);
 //////					onTable.elementAt(0).enableAction();
 //////				}})
 ////				)
 ////			.play();
 //		}
 //	};
 //	return pAction;
 //}
 
 
 
 /*
  * Generates "new" layout specific to each card clicked. 
  * Each card in the grid will have a different "final" layout. 
  */
 //private SLConfig createLayout(int row, int col){
 //	int rowNum = row/3;
 //	System.out.println(rowNum);
 //	SLConfig tempLayout = new SLConfig(tableView)
 //		.gap(10, 10)
 //		.row(150).row(150).row(150).col(100).col(100).col(100).col(100);
 //		for(int r = 0; r<3;r++){
 //			for(int c = 0;c<4; c++ ){
 //				SetCard tblCard = onTable.elementAt(r*4+c);
 //				if (rowNum == row){
 //					if(r == row && c == col){
 //						tblCard = onTable.elementAt(12);
 //					}
 //					else{
 //						tblCard = onTable.elementAt(r*4+c+1);
 //					}
 //				}
 //				tblCard.setAction(animationCreate(r, c));
 //				tempLayout.place(r,c,tblCard);
 //			}
 //		}
 //	
 //	return tempLayout;
 //}
 
 
