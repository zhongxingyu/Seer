 package de.unisiegen.tpml.graphics.components ;
 
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Rectangle;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseMotionAdapter;
 
 import javax.swing.JComponent;
 
 import de.unisiegen.tpml.core.expressions.Expression;
 import de.unisiegen.tpml.core.interpreters.Store;
 import de.unisiegen.tpml.core.typechecker.TypeEnvironment;
 import de.unisiegen.tpml.core.util.Environment;
 import de.unisiegen.tpml.graphics.renderer.AbstractRenderer;
 import de.unisiegen.tpml.graphics.renderer.EnvironmentRenderer;
 import de.unisiegen.tpml.graphics.renderer.PrettyStringRenderer;
 import de.unisiegen.tpml.graphics.renderer.ToListenForMouseContainer;
 
 
 /**
  * this class renders the coumpoundexpression
  *
  *
  * @param <S>
  * @param <E>
  */
 public class CompoundExpression < S , E > extends JComponent
 {
   /**
    * 
    */
   private static final long serialVersionUID = - 7653329118052555176L ;
 
 
   /**
    * Renderer that is used to render the expression
    */
   private PrettyStringRenderer expressionRenderer ;
 
 
   /**
    * The current expression.
    */
   private Expression expression ;
 
 
   /**
    * The expression that will be underlined during the rendering.
    */
   private Expression underlineExpression ;
 
 
   /**
    * The size of the expression.
    */
   private Dimension expressionSize ;
 
 
   /**
    * The current environment. If the environment is <i>null</i> no environment
    * is rendered.
    */
   private Environment < S , E > environment ;
 
 
   /**
    * Renderer that is used to render the environment.
    */
   private EnvironmentRenderer < S , E > environmentRenderer ;
 
 
   /**
    * The size of the environment.
    */
   private Dimension environmentSize ;
 
 
   /**
    * The width of the braces around the expression and the environment.
    */
   private int braceSize ;
 
 
   /**
    * Whether the expression should be wrapped if there is not enough space to
    * render it in on line.<br>
    * Actualy this is only used with the result of the BigStepGUI.
    */
   private boolean noLineWrapping ;
 
 
   /**
    * If this color is given all colors of the {@link AbstractRenderer} are
    * ignored and only this color is used.
    */
   private Color alternativeColor ;
 
 
   /**
    * The arrow symbol that is used between the environment and the expression
    * when used within the
    * {@link de.unisiegen.tpml.graphics.typechecker.TypeCheckerNodeComponent}
    */
   private static String arrowStr = " \u22b3 " ;
 
 
   /**
    * Initialises the CompoundExpression with the default values.<br>
    * <br>
    * The braces have a size of 10 pixes, no underlining and the color of the
    * {@link AbstractRenderer} are ignored.
    */
   private ShowBonds bonds ;
 
   /**
    * the list of points where the mouseovereffect will be react
    */
   private ToListenForMouseContainer toListenForMouse ;
 
   /**
    * the constructor
    *
    */
   public CompoundExpression ( )
   {
     super ( ) ;
     this.bonds = new ShowBonds ( ) ;
     this.toListenForMouse = new ToListenForMouseContainer ( ) ;
     this.alternativeColor = null ;
     this.braceSize = 10 ;
     this.underlineExpression = null ;
     this.addMouseMotionListener ( new MouseMotionAdapter ( )
     {
       @ Override
       public void mouseMoved ( MouseEvent event )
       {
         handleMouseMoved ( event ) ;
       }
     } ) ;
     this.addMouseListener ( new MouseAdapter ( )
     {
       @ Override
       public void mouseExited ( MouseEvent e )
       {
         CompoundExpression.this.getToListenForMouse().reset();
         CompoundExpression.this.getToListenForMouse().setMark ( false ) ;
         CompoundExpression.this.repaint ( ) ;
       }
     } ) ;
   }
 
 
   /**
    * Sets an alternative color.<br>
    * <br>
    * Both renderers, the {@link PrettyStringRenderer} and the
    * {@link EnvironmentRenderer}, are updated with this color.
    * 
    * @param color The alternative color.
    */
   public void setAlternativeColor ( Color color )
   {
     this.alternativeColor = color ;
     if ( this.expressionRenderer != null )
     {
       this.expressionRenderer.setAlternativeColor ( color ) ;
     }
     if ( this.environmentRenderer != null )
     {
       this.environmentRenderer.setAlternativeColor ( color ) ;
     }
   }
 
 
   /**
    * Sets the expression, that should be underlined.
    * 
    * @param pUnderlineExpression
    */
   public void setUnderlineExpression ( Expression pUnderlineExpression )
   {
     boolean needsRepaint = this.underlineExpression != pUnderlineExpression ;
     this.underlineExpression = pUnderlineExpression ;
     if ( this.expressionRenderer != null )
     {
       this.expressionRenderer
           .setUndelinePrettyPrintable ( this.underlineExpression ) ;
     }
     if ( needsRepaint )
     {
       repaint ( ) ;
     }
   }
 
 
   /**
    * Causes the PrettyStringRenderer to recheck the linewraps
    */
   public void reset ( )
   {
     if ( this.expressionRenderer != null )
     {
       this.expressionRenderer.checkLinewraps ( ) ;
     }
   }
 
 
   /**
    * Handles whether a ToolTip should be displayed for the environment, if some
    * parts of it are collapsed.
    * 
    * @param event
    */
   void handleMouseMoved ( MouseEvent event )
   {
     
     //tell the PrettyStringRenderer where the mouse pointer is
     this.toListenForMouse.setHereIam ( event.getX ( ) , event.getY ( ) ) ;
 
     //first, we do not want to mark anything, we are waiting for mouse pointer is over one bounded id
     this.toListenForMouse.setMark ( false ) ;
     CompoundExpression.this.repaint ( ) ;
     
     //note if to mark or not to mark
     boolean mark = false;
     
     //walk throu the postions where to mark
     for ( int t = 0 ; t < this.toListenForMouse.size ( ) ; t++)
     {
       //get position of pointer, these are rectangles. These positions are made by the PrettyStringRenderer
       Rectangle r =  this.toListenForMouse.get ( t ) ;
       int pX = r.x;
       int pX1 = r.x+r.width ;
       int pY = r.y;
       int pY1 = r.y+r.height;
     
       // fnde out if pointer is on one of the chars to mark
       if ((event.getX() >= pX) && (event.getX() <= pX1) && (event.getY() >= pY) && (event.getY() <= pY1)) 
       //if ( ( event.getX ( ) >= pX ) && ( event.getX ( ) <= pX1 ) )
       {
         //just note it
         mark = true;
       }
     }
     
     //if the pointer is on one of the bounded chars
     if (mark)
     {
       //we want to habe marked
       this.toListenForMouse.setMark ( true ) ;
       CompoundExpression.this.repaint ( ) ;
     }
     else
     {
       //we do not want to see anything marked
      this.toListenForMouse.setMark ( false ) ;
      this.toListenForMouse.reset();
      CompoundExpression.this.repaint ( ) ;
     }
     
     if ( this.environmentRenderer != null
         && this.environmentRenderer.isCollapsed ( ) )
     {
       Rectangle r = this.environmentRenderer.getCollapsedArea ( ) ;
       if ( event.getX ( ) >= r.x && event.getX ( ) <= r.x + r.width )
       {
         setToolTipText ( this.environmentRenderer.getCollapsedString ( ) ) ;
       }
       else
       {
         setToolTipText ( null ) ;
       }
     }
     else
     {
       setToolTipText ( null ) ;
     }
   }
 
 
   /**
    * Sets whether the expression should be wrapped or not.
    * 
    * @param pNoLineWrapping
    */
   public void setNoLineWrapping ( boolean pNoLineWrapping )
   {
     this.noLineWrapping = pNoLineWrapping ;
   }
 
 
   /**
    * Sets the expression that should rendered.
    * 
    * @param pExpression
    */
   public void setExpression ( Expression pExpression )
   {
 		// check if we have a new expression
 		if (this.expression != pExpression)
 		{
 			// update to the new expression
 			this.expression = pExpression;
 
 			// because of the bounds are cached we need a new one. The expression
 			// might change by translating in coresyntax
 			this.bonds = new ShowBonds();
 			this.bonds.load(this.expression);
 
 			// check what to do with the renderer
 			if (this.expression == null)
 			{
 				this.expressionRenderer = null;
 			}
 			else
 			{
 				if (this.expressionRenderer == null)
 				{
 
 					this.expressionRenderer = new PrettyStringRenderer();
 					this.expressionRenderer.setAlternativeColor(this.alternativeColor);
 				}
 				this.expressionRenderer.setPrettyString(this.expression.toPrettyString());
 				// reset the underlineExpression
 				setUnderlineExpression(this.underlineExpression);
 			}
 			// be sure to schedule a repaint
 			repaint();
 		}
 	}
 
 
   /**
 	 * Sets the environment taht should be rendered.
 	 * 
 	 * @param pEnvironment
 	 */
   public void setEnvironment ( Environment < S , E > pEnvironment )
   {
     // check if we have a new environment
     if ( this.environment != pEnvironment )
     {
       // update to the new environment
       this.environment = pEnvironment ;
       // check what to do with the renderer
       if ( this.environment == null )
       {
         this.environmentRenderer = null ;
       }
       else
       {
         if ( this.environmentRenderer == null )
         {
           this.environmentRenderer = new EnvironmentRenderer < S , E > ( ) ;
           this.environmentRenderer.setAlternativeColor ( this.alternativeColor ) ;
         }
         this.environmentRenderer.setEnvironment ( this.environment ) ;
       }
       // be sure to schedule a repaint
       repaint ( ) ;
     }
   }
 
 
   /**
    * Calculates the size needed to propperly render the compoundExpression
    * 
    * @param pMaxWidth
    * @return
    */
   public Dimension getNeededSize ( int pMaxWidth )
   {
   	int maxWidth = pMaxWidth;

  	// TODO was ist, wenn der Platz zu klein wird...
 // 	if (maxWidth < 0)
 // 	{
 // 		return new Dimension(0,0);
 // 	}
   	
     Dimension result = new Dimension ( 0 , 0 ) ;
     if ( this.noLineWrapping )
     {
       // to guaranty that no line wrapping should be performed
       // set the maxWidth = MAX_INT
       maxWidth = Integer.MAX_VALUE ;
     }
     // check whether there is an environment.
     if ( this.environment != null )
     {
       this.environmentSize = this.environmentRenderer.getNeededSize ( ) ;
       result.height = this.environmentSize.height ;
       if ( this.environment instanceof Store )
       {
         /*
          * the store will be used within the smallstep and bigstep interpreter
          * the rendering will appear like this: (expression [store])
          */
         result.width += 2 * this.braceSize ;
         // there will be a bit of free space between the environment
         // and the expression
         result.width += this.braceSize ;
         // and the environment size will be missing asswell
         result.width += this.environmentSize.width ;
         /*
          * XXX: Decision how the expression should be wrapped
          */
         maxWidth -= result.width ; // ???
       }
       else if ( this.environment instanceof TypeEnvironment )
       {
         /*
          * the typeenvironment will be used within the typechecker the rendering
          * will appear like this: [type] |> expression
          */
         // the resulting size contains the environment
         result.width += this.environmentSize.width ;
         // and the |> character
         result.width += AbstractRenderer.getTextFontMetrics ( ).stringWidth (
             CompoundExpression.arrowStr ) ;
         maxWidth -= result.width ;
       }
     }
     if ( this.expression != null && this.expressionRenderer != null )
     {
       // now check the size still available for the expression
    		this.expressionSize = this.expressionRenderer.getNeededSizeAll_ ( maxWidth ) ;
   
       result.width += this.expressionSize.width ;
       result.height = Math.max ( result.height , this.expressionSize.height ) ;
     }
     return result ;
   }
 
 
   /**
    * The actualy rendering method.
    * 
    * @param gc The Graphics object that will be used to render the stuff.
    */
   @ Override
   protected void paintComponent ( Graphics gc )
   {
     //TODO Only for test to make yompoundexpression visible
   	//it also displays how often the exptresso is rednerd while srolling...
     //gc.setColor (Color.yellow);
   	//--------------------------------
     //Color [] test = new Color [10];
     //test[0] = Color.yellow;
     //test[1] = Color.red;
     //test[2] = Color.green;
     //test[3] = Color.cyan;
     //test[4] = Color.green;
     //test[5] = Color.lightGray;
     //test[6] = Color.blue;
     //test[7] = Color.gray;
     //test[8] = Color.magenta;
     //test[9] = Color.orange;
     
     //double get = Math.random();
     //int getR = (int) (get*10);
     //gc.setColor (test[getR]);
     //gc.fillRect(0, 0, getWidth () - 1, getHeight () - 1);
   	//--------------------------------
     
     // make sure that we have an expression renderer
     if ( this.expressionRenderer == null )
     {
       return ;
     }
     // assuming the size of the component will suffice, no testing
     // of any sizes will happen.
     /*
      * just to get reminded: no environment: expression storeenvironment:
      * (expression [env]) typeenvironment: [env] |> expression
      */
     int posX = 0 ;
     int posY = 0 ;
     // if there is an environment and it is of type Store
     // draw the braces around the entire expression with environment
     if ( this.environment instanceof Store )
     {
       posX += this.braceSize ;
       gc.drawArc ( 0 , 0 , this.braceSize , getHeight ( ) , 90 , 180 ) ;
       gc.drawArc ( getWidth ( ) - 1 - this.braceSize , 0 , this.braceSize ,
           getHeight ( ) , 270 , 180 ) ;
     }
     // if there is no environment or the environment is of type
     // Store, the entire expression (with environment) will begin
     // with the expression
     if ( this.environment == null || this.environment instanceof Store )
     {
       //this.expressionRenderer.render ( posX , posY , getHeight ( ) , gc ,
       //    bonds , toListenForMouse ) ;
     	
     	this.expressionRenderer.render ( posX , posY , getWidth(), getHeight () , gc , this.bonds , this.toListenForMouse ) ;
       posX += this.expressionSize.width ;
       // if there is an environment render it now
       if ( this.environment != null )
       {
         posX += this.braceSize ;
         this.environmentRenderer.renderer ( posX , posY ,
             this.environmentSize.width , getHeight ( ) , gc ) ;
       }
     }
     else if ( this.environment instanceof TypeEnvironment )
     {
       // draw the environment first
       this.environmentRenderer.renderer ( posX , posY , this.environmentSize.width , getHeight ( ) , gc ) ;
     	//this.environmentRenderer.renderer ( posX , posY , this.environmentSize.width , environmentSize.height , gc ) ;
       posX += this.environmentSize.width ;
 
       gc.setFont ( AbstractRenderer.getTextFont ( ) ) ;
       gc.setColor ( AbstractRenderer.getTextColor ( ) ) ;
       gc.drawString ( CompoundExpression.arrowStr , posX , posY+AbstractRenderer.getFontAscent() ) ;
       posX += AbstractRenderer.getTextFontMetrics ( ).stringWidth (
           CompoundExpression.arrowStr ) ;
       // draw the expression at the last position.
 
       this.expressionRenderer.render ( posX , posY , getWidth() ,getHeight ( ) , gc , this.bonds , this.toListenForMouse ) ;
       //this.expressionRenderer.render ( posX , posY , getWidth() ,AbstractRenderer.getAbsoluteHeight (), gc , bonds , toListenForMouse ) ;
     }
     
     //TODO DEbugging
     // gc.setColor (Color.YELLOW);
     // gc.drawRect(0, 0, getWidth () - 1, getHeight () - 1);
   }
 
 
   /**
    * returns the current expression.
    *
    * @return the expression
    */
   public Expression getExpression ( )
   {
     return this.expression ;
   }
 
 
 	/**
 	 * @return the toListenForMouse
 	 */
 	public ToListenForMouseContainer getToListenForMouse()
 	{
 		return this.toListenForMouse;
 	}
  
 }
