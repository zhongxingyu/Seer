 package de.unisiegen.tpml.graphics.components;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Rectangle;
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
 
 public class CompoundExpression<S, E> extends JComponent {
 	
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -7653329118052555176L;
 
 	private PrettyStringRenderer				expressionRenderer;
 	
 	private Expression									expression;
 	
 	private Expression									underlineExpression;
 	
 	private Dimension										expressionSize;
 	
 	private Environment<S, E> 					environment;
 	
 	private EnvironmentRenderer<S, E>		environmentRenderer;
 	
 	private Dimension										environmentSize;
 	
 	private	int													braceSize;
 	
 	private boolean											noLineWrapping;
 	
 	private Color												alternativeColor;
 	
 	private static String								arrowStr = " \u22b3 ";
 	
 	public CompoundExpression () {
 		super ();
 		
 		this.alternativeColor 		= null;
 		this.braceSize 						= 10;
 		this.underlineExpression	= null;
 		
 		this.addMouseMotionListener(new MouseMotionAdapter() {
 			@Override
 			public void mouseMoved (MouseEvent event) {
 				handleMouseMoved (event);
 			}
 		});
 	}
 	
 	public void setAlternativeColor (Color color) {
 		this.alternativeColor = color;
 		
 		if (this.expressionRenderer != null) {
 			this.expressionRenderer.setAlternativeColor(color);
 		}
 		if (this.environmentRenderer != null) {
 			this.environmentRenderer.setAlternativeColor(color);
 		}
 	}
 	
 	public void setUnderlineExpression (Expression underlineExpression) {
 		boolean needsRepaint = this.underlineExpression != underlineExpression;
 		
 		
 		
 		this.underlineExpression = underlineExpression;
 		
 		if (this.expressionRenderer != null) {
 			this.expressionRenderer.setUndelinePrettyPrintable(this.underlineExpression);
 		}
 		
 		if (needsRepaint) {
 			repaint ();
 		}
 		
 	}
 	
 	private void handleMouseMoved (MouseEvent event) {
 		if (this.environmentRenderer != null && this.environmentRenderer.isCollapsed()) {
 			Rectangle r = this.environmentRenderer.getCollapsedArea();
 			if (event.getX () >= r.x && event.getX () <= r.x + r.width) {
 				setToolTipText(this.environmentRenderer.getCollapsedString());
 			}
 			else {
 				setToolTipText(null);
 			}
 		}
 		else {
 			setToolTipText(null);
 		}
 	}
 	
 	public void setNoLineWrapping (boolean noLineWrapping) {
 		this.noLineWrapping = noLineWrapping;
 	}
 	
 	public void setExpression (Expression expression) {
 		this.expression = expression;
 		if (this.expression == null) {
 			this.expressionRenderer = null;
 			return;
 		}
 		
 		if (this.expressionRenderer == null) {
 			this.expressionRenderer = new PrettyStringRenderer ();
 			this.expressionRenderer.setAlternativeColor(this.alternativeColor);
 		}
 		
 		this.expressionRenderer.setPrettyString(this.expression.toPrettyString());
 		
 		// reset the underlineExpression
 		setUnderlineExpression(this.underlineExpression);
 		
 	}
 	
 	public void setEnvironment (Environment<S, E> environment) {
 		this.environment = environment;
 		if (this.environment == null) {
 			this.environmentRenderer = null;
 			return;
 		}
 		
 		if (this.environmentRenderer == null) {
 			this.environmentRenderer = new EnvironmentRenderer<S,E> ();
 			this.environmentRenderer.setAlternativeColor(this.alternativeColor);
 		}
 		
 		this.environmentRenderer.setEnvironment(this.environment);
 	}
 	
 	
 	/**
 	 * Calculates the size needed to propperly render the compoundExpression<br>
 	 * <br>
 	 *   
 	 * @param maxWidth
 	 * @return
 	 */
 	public Dimension getNeededSize (int maxWidth) {
 		Dimension result = new Dimension (0, 0);
 		
 		if (this.noLineWrapping) {
 			// to guaranty that no line wrapping should be performed
 			// set the maxWidth = MAX_INT
 			maxWidth = Integer.MAX_VALUE;
 		}
 		
 		// check whether there is an environment. 
 		if (this.environment != null) {
 			this.environmentSize = this.environmentRenderer.getNeededSize();
 			
 			result.height = this.environmentSize.height;
 			
 			if (this.environment instanceof Store) {
 				/*
 				 * the store will be used within the smallstep and bigstep
 				 * interpreter
 				 * the rendering will appear like this:
 				 *      (expression [store])
 				 */
 
 				result.width += 2 * this.braceSize;
 				
 				// there will be a bit of free space between the environment
 				// and the expression
 				result.width += this.braceSize;
 				
 				// and the environment size will be missing asswell
 				result.width += this.environmentSize.width;
 				
 				
 				/*
 				 * XXX: Decision how the expression should be wrapped
 				 */
 				maxWidth -= result.width; // ???
 			}
 			else if (this.environment instanceof TypeEnvironment) {
 				/*
 				 * the typeenvironment will be used within the typechecker
 				 * the rendering will appear like this:
 				 * 		  [type] |> expression
 				 */
 
 				// the resulting size contains the environment 
 				result.width += this.environmentSize.width;
 				
 				// and the |> character 
 				result.width += AbstractRenderer.getTextFontMetrics().stringWidth(CompoundExpression.arrowStr);
 				
 				maxWidth -= result.width;
 			}
 
 		}
 		
 		if (this.expression != null && this.expressionRenderer != null) {
 			// now check the size still available for the expression
 			this.expressionSize = this.expressionRenderer.getNeededSize(maxWidth);
 			result.width += this.expressionSize.width;
 			
			result.height = Math.max(result.height, this.expressionSize.height);
 		}
 		return result;
 	}
 	
 	@Override
 	protected void paintComponent (Graphics gc) {
 		// assuming the size of the component will suffice, no testing
 		// of any sizes will happen.
 		
 		/*
 		 * just to get reminded:
 		 * 
 		 * no environment:       expression
 		 * storeenvironment:		(expression [env])
 		 * typeenvironment:			[env] |> expression
 		 */
 		
 		int posX = 0;
 		int posY = 0;
 		// if there is an environment and it is of type Store
 		// draw the braces around the entire expression with environment
 		if (this.environment instanceof Store) {
 			posX += this.braceSize;
 			gc.drawArc(0, 0, this.braceSize, getHeight (), 90, 180);
 			gc.drawArc(getWidth () - 1 - this.braceSize, 0, this.braceSize, getHeight (), 270, 180);
 		}
 		
 
 		// if there is no environment or the environment is of type
 		// Store, the entire expression (with environment) will begin
 		// with the expression
 		if (this.environment == null || this.environment instanceof Store) {
 			this.expressionRenderer.render(posX, posY, getHeight(), gc);
 			posX += this.expressionSize.width;
 			
 			// if there is an environment render it now
 			if (this.environment != null) {
				posX += this.braceSize;
 				
 				this.environmentRenderer.renderer(posX, posY, this.environmentSize.width, getHeight (), gc);
 			}
 		}
 		else if (this.environment instanceof TypeEnvironment) {
 			
 			// draw the environment first
 			this.environmentRenderer.renderer(posX, posY, this.environmentSize.width, getHeight (), gc);
 			posX += this.environmentSize.width;
 			
 			// draw the arrow character in the vertical center
 			int centerV = getHeight () / 2;
 			centerV += AbstractRenderer.getTextFontMetrics().getAscent() / 2;
 			gc.setFont(AbstractRenderer.getTextFont());
 			gc.setColor(AbstractRenderer.getTextColor());
 			gc.drawString(CompoundExpression.arrowStr, posX, centerV);
 			posX += AbstractRenderer.getTextFontMetrics().stringWidth(CompoundExpression.arrowStr);
 			
 			// draw the expression at the last position.
 			this.expressionRenderer.render(posX, posY, getHeight (), gc);
 		}
 		
 		
 		
 	}
 
 }
