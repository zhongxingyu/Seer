 package gameLogic;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 
 import javax.swing.JPanel;
 import javax.swing.JTextArea;
 import javax.swing.SwingUtilities;
 
 import aurelienribon.slidinglayout.SLAnimator;
 import aurelienribon.tweenengine.Tween;
 import aurelienribon.tweenengine.TweenManager;
 import aurelienribon.tweenengine.equations.Quad;
 
 /**
  * @author Sameer
  * Holds the properties of each card
  *
  */
 @SuppressWarnings("serial")
 public class SetCard extends JPanel{
 	
 	/*
 	 * Panel Properties
 	 */
 	private static final Color FG_COLOR = new Color(0xFFFFFF);
 	private static final Color BG_COLOR = new Color(0x3B5998);
 	private static final Color BORDER_COLOR = new Color(0x000000);
 	private static final float baseScale = 1f;
 	private static final float bigScale = 1.1f;
 	private int borderThickness = 2;
 	private int height = 150;
 	private int width = 100;
 	private float scaleXY = baseScale;
 	private float opacity = 100;
 	private boolean hover = false;
 	private boolean hoverEnabled = true;
 	private boolean clickEnabled = true;
 //	private Runnable selectAdd, selectRemove;
 	private final JTextArea cardInfo = new JTextArea();
 	private static TweenManager tweenManager = null;
 	
 	
 	
 	/*
 	 * Card Properties
 	 * cardLock is associated with the positioning In the deck
 	 * all private
 	 */
 	private int color, number, shade, shape;
 	private boolean selected = false;
 
 	// Probably only for testing ////////
     public static final String colorNames [] = {"red", "green", "blue"};
     public static final String numberNames [] = {"one", "two", "three"};
     public static final String shapeNames [] = {"diamond", "oval", "squiggle"};
     public static final String shadeNames [] = {"hollow", "shaded", "solid"};
     public static final String attributeNames [][] = {colorNames, numberNames, shapeNames, shadeNames};
     public static final String attributes [] = {"color", "number", "shape", "shade"};
     /////////////////
 
     
     //Alternate Constructor
     public SetCard ( int cardNum){
     	tweenManager = SLAnimator.createTweenManager();
     	this.color = getNthDigit(cardNum,3,4);
     	this.number = getNthDigit(cardNum,3,3);
     	this.shape = getNthDigit(cardNum,3,2);
     	this.shade = getNthDigit(cardNum,3,1);
     	selected = false;
     	setBackground(BG_COLOR);
     	
     	//Only for testing  //////////////////
     	String cardString = "Color: " + colorNames[color] + "\nNumber: " + numberNames[number] + "\nShape: " + shapeNames[shape]+ "\nShade: " +shadeNames[shade];
     	cardInfo.setText(cardString);
 		cardInfo.setEditable(false);
 		cardInfo.setEnabled(false);
 		cardInfo.setBackground(null);
 		cardInfo.setForeground(FG_COLOR);
 		add(cardInfo);
 		/////////////
 		
 		addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseEntered(MouseEvent e) {
 				hover = true;
 				if (hoverEnabled) showBorder();
 			}
 
 			@Override
 			public void mouseExited(MouseEvent e) {
 				hover = false;
 				if(!selected)
 					hideBorder();
 			}
 
 			@Override
 			public void mouseReleased(MouseEvent e) {
 				if (clickEnabled) {
 					if(selected){
 						SwingUtilities.invokeLater(new Runnable() {
 							@Override
 							public void run() {
 								selectRemove();
 								shrink();
 							}
 						});
 					} else{
 						SwingUtilities.invokeLater(new Runnable() {
 							@Override
 							public void run() {
 								selectAdd();
 								grow();
 							}
 						});
 					}	
 				}
 			}
 		}); //End Mouse Listener
     }//End Alt Constructor
     
     public int getNthDigit(int number, int base, int n) {    
     	  return (int) ((number / Math.pow(base, n - 1)) % base);
     }
     public int getCardNum() {
 		return (27*color + 9*number + 3*shape + shade);
 	}
     
 
     /*      getFunctions     */
     public String getColor(){	return colorNames[color];	}
     public String getNumber(){ 	return numberNames[number]; }
     public String getShape(){  	return shapeNames[shape];   }
     public String getShade(){  	return shadeNames[shade];   }
     public String toString(){
     	return "SetCard (" + color + ", " + number + ", " + shape + ", " + shade + ")";
     }
     
     /*Allows for easy checking of attributes*/
     public int attr (int i) {
     	switch (i) {
     	case 0:
     	    return color;
     	case 1:
     	    return number;
     	case 2:
     	    return shape;
     	case 3:
     	    return shade;
     	default:
     	    return -1;
     	}
     }
     
 	public void enableHover() {hoverEnabled = true; if (hover) showBorder();}
 	public void disableHover() {hoverEnabled = false;}
 	public void enableClick() {clickEnabled = true;}
 	public void disableClick(){clickEnabled = false;}
 
     public void unSelect(){
      	hideBorder();
    	shrink();
     	selectRemove();
     }
     
     /* Add self to selected cards vector if selected*/
     public void selectAdd(){
     	SetTable.addSelected(this);
     }
     public void selectRemove(){
     	SetTable.rmSelected(this);
     }
    
 	private void showBorder() {
 		tweenManager.killTarget(borderThickness);
 		Tween.to(SetCard.this, Accessor.BORDER_THICKNESS, 0.4f)
 			.target(10)
 			.start(tweenManager);
 	}
 
 	private void hideBorder() {
 		tweenManager.killTarget(borderThickness);
 		Tween.to(SetCard.this, Accessor.BORDER_THICKNESS, 0.4f)
 			.target(2)
 			.start(tweenManager);
 	}
 	
 	private void grow(){
 		selected = true;
 		showBorder();
 		disableClick();
 		scaleXY = bigScale;
 		Tween.to(SetCard.this, Accessor.XYWH, 0.1f)
 					.targetRelative((1-scaleXY)*width/2, (1-scaleXY)*height/2, (scaleXY-1)*width, (scaleXY-1)*height)
 					.ease(Quad.OUT) 
 					.start(tweenManager);
 		enableClick();
 	}
 	
 	private void shrink(){
 		selected = false;
 		disableClick();
 		Tween.to(SetCard.this, Accessor.XYWH, 0.1f)
 			.targetRelative(-(1-scaleXY)*width/2, -(1-scaleXY)*height/2, -(scaleXY-1)*width, -(scaleXY-1)*height)
 			.ease(Quad.OUT)
 			.start(tweenManager);
 		scaleXY = baseScale;
 		enableClick();
 	}	
 	    
     /*
      * Drawing SetCard
      */
     @Override
 	protected void paintComponent(Graphics g) {
 		super.paintComponent(g);
 
 		Graphics2D gg = (Graphics2D) g;
 
 		int w = getWidth();
 		int h = getHeight();
 		
 		int t = borderThickness;
 		gg.setColor(BORDER_COLOR);
 		gg.fillRect(0, 0, t, h-1);
 		gg.fillRect(0, 0, w-1, t);
 		gg.fillRect(0, h-1-t, w-1, t);
 		gg.fillRect(w-1-t, 0, t, h-1);
 	}
     
     private float getOpacity(){
     	return opacity;
     }
     private void setOpacity(float newVal){
     	opacity = newVal;
     }
    
     /*Animation Accessors*/
 	public static class Accessor extends SLAnimator.ComponentAccessor {
 		public static final int BORDER_THICKNESS = 100;
 		public static final int OPACITY = 101;
 
 		@Override
 		public int getValues(Component target, int tweenType, float[] returnValues) {
 			SetCard tp = (SetCard) target;
 
 			int ret = super.getValues(target, tweenType, returnValues);
 			if (ret >= 0) return ret;
 
 			switch (tweenType) {
 				case BORDER_THICKNESS: 
 					returnValues[0] = tp.borderThickness; 
 					return 1;
 				case OPACITY:
 					returnValues[0] = tp.getOpacity();
 					return 1;					
 				default: return -1;
 			}
 		}
 
 		@Override
 		public void setValues(Component target, int tweenType, float[] newValues) {
 			SetCard tp = (SetCard) target;
 
 			super.setValues(target, tweenType, newValues);
 
 			switch (tweenType) {
 				case BORDER_THICKNESS:
 					tp.borderThickness = Math.round(newValues[0]);
 					tp.repaint();
 					break;
 				case OPACITY:
 					tp.setOpacity(newValues[0]);
 				default: assert false;
 					
 			}
 		}
 	}   
     
 }
 
 //
 ///*
 //* Old Constructor 
 //* Sets cardLoc to a random float value
 //*/ 
 //public SetCard (int color, int number, int shape, int shade) {                                  
 //	tweenManager =  SLAnimator.createTweenManager();                                                          
 //	this.color = color;                                                                         
 //	this.number = number;                                                                       
 //	this.shape = shape;                                                                         
 //	this.shade = shade;                                                                         
 //	selected = false;
 //	cardLoc = (int)( random.nextInt());
 //	setBackground(BG_COLOR);
 //		
 //	String cardString = "Color: " + colorNames[color] + "\nNumber: " + numberNames[number] + "\nShape: " + shapeNames[shape]+ "\nShade: " +shadeNames[shade];
 //	cardInfo.setText(cardString);
 //	cardInfo.setEditable(false);
 //	cardInfo.setEnabled(false);
 //	cardInfo.setBackground(null);
 //	cardInfo.setForeground(FG_COLOR);
 //	add(cardInfo);
 //	
 //	addMouseListener(new MouseAdapter() {
 //		@Override
 //		public void mouseEntered(MouseEvent e) {
 //			hover = true;
 //			if (hoverEnabled) showBorder();
 //		}
 //
 //		@Override
 //		public void mouseExited(MouseEvent e) {
 //			hover = false;
 //			if(!selected)
 //				hideBorder();
 //		}
 //
 //		@Override
 //		public void mouseReleased(MouseEvent e) {
 //			if (clickEnabled) {
 //				if(selected){
 //					selectRemove();
 //					shrink();
 //				}
 //				else{
 //					selectAdd();
 //					grow();
 //				}
 //				
 //			}
 //		}
 //		
 //	}); //End Mouse Listener
 //}//End Constructor
 //
 //
 ///*
 // * Allows us to use the Collections.sort method. 
 // * @see java.lang.Comparable#compareTo(java.lang.Object)
 // */
 //public int compareTo(SetCard otherCard) {
 //	 if (this.cardLoc == ( otherCard).cardLoc)
 //         return 0;
 //     else if ((this.cardNum) > (otherCard).cardNum)
 //         return 1;
 //     else
 //         return -1;
 //}
     
 
 
