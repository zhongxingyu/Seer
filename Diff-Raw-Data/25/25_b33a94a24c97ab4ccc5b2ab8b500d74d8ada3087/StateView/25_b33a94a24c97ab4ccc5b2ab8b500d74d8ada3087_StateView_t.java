 package de.unisiegen.gtitool.ui.jgraph;
 
 
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.FontMetrics;
 import java.awt.GradientPaint;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.geom.Point2D;
 import java.awt.geom.Rectangle2D;
 
 import javax.swing.JComponent;
 
 import org.jgraph.graph.CellViewRenderer;
 import org.jgraph.graph.EdgeView;
 import org.jgraph.graph.VertexRenderer;
 import org.jgraph.graph.VertexView;
 
 import de.unisiegen.gtitool.core.entities.State;
 import de.unisiegen.gtitool.core.entities.Transition;
 import de.unisiegen.gtitool.core.parser.style.PrettyString;
 import de.unisiegen.gtitool.core.parser.style.PrettyToken;
 import de.unisiegen.gtitool.core.parser.style.Style;
 import de.unisiegen.gtitool.core.preferences.listener.ColorChangedAdapter;
 import de.unisiegen.gtitool.ui.preferences.PreferenceManager;
 
 
 /**
  * The state view class.
  * 
  * @author Benjamin Mies
  * @author Christian Fehler
  * @version $Id:StateView.java 910 2008-05-16 00:31:21Z fehler $
  */
 public final class StateView extends VertexView
 {
 
   /**
    * The {@link JGTIGraph} ellipse renderer.
    * 
    * @author Benjamin Mies
    * @author Christian Fehler
    */
   public static class JGraphEllipseRenderer extends VertexRenderer
   {
 
     /**
      * The serial version uid.
      */
     private static final long serialVersionUID = 264864659062743923L;
 
 
     /**
      * The {@link State} color.
      */
     private Color preferenceState;
 
 
     /**
      * The active {@link State} color.
      */
     private Color preferenceStateActive;
 
 
     /**
      * The {@link State} background color.
      */
     private Color preferenceStateBackground;
 
 
     /**
      * The error {@link State} color.
      */
     private Color preferenceStateError;
 
 
     /**
      * The final {@link State} color.
      */
     private Color preferenceStateFinal;
 
 
     /**
      * The slected {@link State} color.
      */
     private Color preferenceStateSelected;
 
 
     /**
      * The start {@link State} color.
      */
     private Color preferenceStateStart;
 
 
     /**
      * The normal {@link Transition} color.
      */
     private Color preferenceTransition;
 
 
     /**
      * The {@link StateView}.
      */
     private StateView stateView;
 
 
     /**
      * Allocates a new {@link JGraphEllipseRenderer}.
      * 
      * @param stateView The {@link StateView}.
      */
     public JGraphEllipseRenderer ( StateView stateView )
     {
       super ();
       this.stateView = stateView;
 
       this.preferenceState = PreferenceManager.getInstance ()
           .getColorItemState ().getColor ();
       this.preferenceStateBackground = PreferenceManager.getInstance ()
           .getColorItemStateBackground ().getColor ();
       this.preferenceStateError = PreferenceManager.getInstance ()
           .getColorItemStateError ().getColor ();
       this.preferenceStateActive = PreferenceManager.getInstance ()
           .getColorItemStateActive ().getColor ();
       this.preferenceStateSelected = PreferenceManager.getInstance ()
           .getColorItemStateSelected ().getColor ();
       this.preferenceStateStart = PreferenceManager.getInstance ()
           .getColorItemStateStart ().getColor ();
       this.preferenceStateFinal = PreferenceManager.getInstance ()
           .getColorItemStateFinal ().getColor ();
       this.preferenceTransition = PreferenceManager.getInstance ()
           .getColorItemTransition ().getColor ();
 
       PreferenceManager.getInstance ().addColorChangedListener (
           new ColorChangedAdapter ()
           {
 
             @SuppressWarnings ( "synthetic-access" )
             @Override
             public void colorChangedState ( Color newColor )
             {
               JGraphEllipseRenderer.this.preferenceState = newColor;
             }
 
 
             @SuppressWarnings ( "synthetic-access" )
             @Override
             public void colorChangedStateActive ( Color newColor )
             {
               JGraphEllipseRenderer.this.preferenceStateActive = newColor;
             }
 
 
             @SuppressWarnings ( "synthetic-access" )
             @Override
             public void colorChangedStateBackground ( Color newColor )
             {
               JGraphEllipseRenderer.this.preferenceStateBackground = newColor;
             }
 
 
             @SuppressWarnings ( "synthetic-access" )
             @Override
             public void colorChangedStateError ( Color newColor )
             {
               JGraphEllipseRenderer.this.preferenceStateError = newColor;
             }
 
 
             @SuppressWarnings ( "synthetic-access" )
             @Override
             public void colorChangedStateFinal ( Color newColor )
             {
               JGraphEllipseRenderer.this.preferenceStateFinal = newColor;
             }
 
 
             @SuppressWarnings ( "synthetic-access" )
             @Override
             public void colorChangedStateSelected ( Color newColor )
             {
               JGraphEllipseRenderer.this.preferenceStateSelected = newColor;
             }
 
 
             @SuppressWarnings ( "synthetic-access" )
             @Override
             public void colorChangedStateStart ( Color newColor )
             {
               JGraphEllipseRenderer.this.preferenceStateStart = newColor;
             }
 
 
             @SuppressWarnings ( "synthetic-access" )
             @Override
             public void colorChangedTransition ( Color newColor )
             {
               JGraphEllipseRenderer.this.preferenceTransition = newColor;
             }
           } );
     }
 
 
     /**
      * {@inheritDoc}
      * 
      * @see JComponent#getPreferredSize()
      */
     @Override
     public final Dimension getPreferredSize ()
     {
       Dimension d = super.getPreferredSize ();
       d.width += d.width / 8;
       d.height += d.height / 2;
       return d;
     }
 
 
     /**
      * {@inheritDoc}
      * 
      * @see VertexRenderer#paint(Graphics)
      */
     @SuppressWarnings ( "synthetic-access" )
     @Override
     public final void paint ( Graphics g )
     {
       State state = null;
       DefaultStateView defaultStateView = null;
       if ( this.stateView.getCell () instanceof DefaultStateView )
       {
         defaultStateView = ( DefaultStateView ) this.stateView.getCell ();
         state = defaultStateView.getState ();
       }
       else
       {
         return;
       }
       int b = this.borderWidth;
       Graphics2D g2 = ( Graphics2D ) g;
       Dimension d = getSize ();
       boolean tmp = this.selected;
       if ( super.isOpaque () )
       {
         Color background = null;
         // Group
         if ( defaultStateView.getGroupColor () != null )
         {
           background = defaultStateView.getGroupColor ();
         }
         // Error
         else if ( state.isError () )
         {
           background = this.preferenceStateError;
         }
         // Active
         else if ( state.isActive () )
         {
           background = this.preferenceStateActive;
         }
         // Start
         else if ( state.isStartState () )
         {
           background = this.preferenceStateStart;
         }
         // Final
         else if ( state.isFinalState () )
         {
           background = this.preferenceStateFinal;
         }
         // Normal
         else
         {
           background = this.preferenceStateBackground;
         }
 
         g.setColor ( background );
 
         if ( ( this.gradientColor != null ) && !this.preview )
         {
           setOpaque ( false );
           g2.setPaint ( new GradientPaint ( 0, 0, background, getWidth (),
               getHeight (), this.gradientColor, true ) );
         }
 
         // final state
         if ( state.isPowerState () )
         {
           if ( state.isFinalState () )
           {
             g.fillRoundRect ( b + 3, b + 3, d.width - b - 8, d.height - b - 8,
                 50, 50 );
           }
           else
           {
             g.fillRoundRect ( b - 1, b - 1, d.width - b, d.height - b, 50, 50 );
           }
         }
         else
         {
           if ( state.isFinalState () )
           {
             g.fillOval ( b + 3, b + 3, d.width - b - 8, d.height - b - 8 );
           }
           else
           {
             g.fillOval ( b - 1, b - 1, d.width - b, d.height - b );
           }
         }
       }
       try
       {
         setBorder ( null );
         setOpaque ( false );
         this.selected = false;
 
         g.setColor ( this.preferenceState );
         g.setFont ( getFont () );
         FontMetrics metrics = g.getFontMetrics ();
 
         int dx = ( d.width / 2 )
             - ( metrics.stringWidth ( state.toString () ) / 2 ) - 1;
         int dy = ( d.height / 2 ) + ( metrics.getHeight () / 2 ) - 3;
 
         PrettyString prettyString = new PrettyString ();
         prettyString.addPrettyString ( state.toPrettyString () );
 
         int insets = state.isFinalState () ? 20 : 10;
         // short version
         if ( ( metrics.stringWidth ( state.toString () ) + insets ) > d.width )
         {
           state.setShortNameUsed ( true );
 
           dx = insets / 2;
           String dots = " ..."; //$NON-NLS-1$
           PrettyToken lastPrettyToken = null;
           while ( ( !prettyString.isEmpty () )
               && ( ( metrics.stringWidth ( prettyString.toString () + dots ) + 2 * dx ) > d.width ) )
           {
             lastPrettyToken = prettyString.removeLastPrettyToken ();
           }
 
           if ( lastPrettyToken != null )
           {
             char [] chars = lastPrettyToken.getChar ();
             int i = 0;
             String addText = ""; //$NON-NLS-1$
             while ( i < chars.length
                 && ( ( metrics.stringWidth ( prettyString.toString () + addText
                     + dots ) + 2 * dx ) <= d.width ) )
             {
               addText += chars [ i ];
               i++ ;
             }

            if ( addText.length () > 0 )
            {
              addText = addText.substring ( 0, addText.length () - 1 );
            }

            prettyString.addPrettyToken ( new PrettyToken ( addText,
                lastPrettyToken.getStyle () ) );
           }
 
           // center the dots if there are no pretty tokens
           if ( prettyString.isEmpty () )
           {
             dots = "..."; //$NON-NLS-1$
             dx = ( d.width / 2 ) - ( metrics.stringWidth ( dots ) / 2 ) - 1;
           }
 
           prettyString.addPrettyToken ( new PrettyToken ( dots, Style.NONE ) );
         }
         // normal version
         else
         {
           state.setShortNameUsed ( false );
         }
 
         for ( PrettyToken currentToken : prettyString )
         {
           Font font = null;
 
           if ( !currentToken.isBold () && !currentToken.isItalic () )
           {
             font = g.getFont ().deriveFont ( Font.PLAIN );
           }
           else if ( currentToken.isBold () && currentToken.isItalic () )
           {
             font = g.getFont ().deriveFont ( Font.BOLD | Font.ITALIC );
           }
           else if ( currentToken.isBold () )
           {
             font = g.getFont ().deriveFont ( Font.BOLD );
           }
           else if ( currentToken.isItalic () )
           {
             font = g.getFont ().deriveFont ( Font.ITALIC );
           }
 
           g.setFont ( font );
           g.setColor ( currentToken.getColor () );
 
           char [] chars = currentToken.getChar ();
           for ( int i = 0 ; i < chars.length ; i++ )
           {
             g.drawChars ( chars, i, 1, dx, dy );
             dx += metrics.charWidth ( chars [ i ] );
           }
         }
       }
       finally
       {
         this.selected = tmp;
       }
       if ( this.bordercolor != null )
       {
         g.setColor ( this.bordercolor );
         g2.setStroke ( new BasicStroke ( b ) );
 
         if ( state.isPowerState () )
         {
           g.drawRoundRect ( b - 1, b - 1, d.width - b, d.height - b, 50, 50 );
           if ( state.isFinalState () )
           {
             g.drawRoundRect ( b + 3, b + 3, d.width - b - 8, d.height - b - 8,
                 50, 50 );
           }
         }
         else
         {
           g.drawOval ( b - 1, b - 1, d.width - b, d.height - b );
           if ( state.isFinalState () )
           {
             g.drawOval ( b + 3, b + 3, d.width - b - 8, d.height - b - 8 );
           }
         }
       }
       if ( this.selected )
       {
         g.setColor ( this.preferenceStateSelected );
 
         if ( state.isPowerState () )
         {
           g.drawRoundRect ( b - 1, b - 1, d.width - b, d.height - b, 50, 50 );
           if ( state.isFinalState () )
           {
             g.drawRoundRect ( b + 3, b + 3, d.width - b - 8, d.height - b - 8,
                 50, 50 );
           }
         }
         else
         {
           g.drawOval ( b - 1, b - 1, d.width - b, d.height - b );
           if ( state.isFinalState () )
           {
             g.drawOval ( b + 3, b + 3, d.width - b - 8, d.height - b - 8 );
           }
         }
       }
       if ( state.isStartState () )
       {
         g.setFont ( getFont ().deriveFont ( Font.BOLD ) );
         g.setColor ( this.preferenceTransition );
         // Manipulate the clipping area
         g2.setClip ( -100, 0, 150, 70 );
 
         // Paint an arrow and a string start if state is start state
         g.drawLine ( -50, 35, 0, 35 );
         g.fillPolygon ( new int []
         { -6, -6, 0 }, new int []
         { 30, 40, 35 }, 3 );
         g.drawString ( "Start", -40, 30 ); //$NON-NLS-1$
       }
     }
   }
 
 
   /**
    * The serial version uid.
    */
   private static final long serialVersionUID = -8873631550630271091L;
 
 
   /**
    * Returns the height.
    * 
    * @param state The {@link State}.
    * @return The height.
    */
   public static final int getHeight ( State state )
   {
     if ( state.isPowerState () )
     {
       return 70;
     }
     return 70;
   }
 
 
   /**
    * Returns the width.
    * 
    * @param state The {@link State}.
    * @return The width.
    */
   public static final int getWidth ( State state )
   {
     if ( state.isPowerState () )
     {
       return 120;
     }
     return 70;
   }
 
 
   /**
    * The {@link JGraphEllipseRenderer} for this view.
    */
   public transient JGraphEllipseRenderer ellipseRenderer;
 
 
   /**
    * Allocates a new {@link StateView}.
    */
   public StateView ()
   {
     super ();
     this.ellipseRenderer = new JGraphEllipseRenderer ( this );
   }
 
 
   /**
    * Create a new {@link StateView}.
    * 
    * @param cell The cell {@link Object} for this view.
    */
   public StateView ( Object cell )
   {
     super ( cell );
     this.ellipseRenderer = new JGraphEllipseRenderer ( this );
   }
 
 
   /**
    * {@inheritDoc}
    * 
    * @see VertexView#getPerimeterPoint(EdgeView, Point2D, Point2D)
    */
   @Override
   public final Point2D getPerimeterPoint ( @SuppressWarnings ( "unused" )
   EdgeView edge, @SuppressWarnings ( "unused" )
   Point2D source, Point2D p )
   {
     Rectangle2D r = getBounds ();
 
     double x = r.getX ();
     double y = r.getY ();
     double a = ( r.getWidth () + 1 ) / 2;
     double b = ( r.getHeight () + 1 ) / 2;
 
     // x0,y0 - center of ellipse
     double x0 = x + a;
     double y0 = y + b;
 
     // x1, y1 - point
     double x1 = p.getX ();
     double y1 = p.getY ();
 
     // calculate straight line equation through point and ellipse center
     // y = d * x + h
     double dx = x1 - x0;
     double dy = y1 - y0;
 
     if ( dx == 0 )
     {
       return new Point ( ( int ) x0, ( int ) ( y0 + b * dy / Math.abs ( dy ) ) );
     }
 
     double d = dy / dx;
     double h = y0 - d * x0;
 
     // calculate intersection
     double e = a * a * d * d + b * b;
     double f = -2 * x0 * e;
     double g = a * a * d * d * x0 * x0 + b * b * x0 * x0 - a * a * b * b;
 
     double det = Math.sqrt ( f * f - 4 * e * g );
 
     // two solutions (perimeter points)
     double xout1 = ( -f + det ) / ( 2 * e );
     double xout2 = ( -f - det ) / ( 2 * e );
     double yout1 = d * xout1 + h;
     double yout2 = d * xout2 + h;
 
     double dist1Squared = Math.pow ( ( xout1 - x1 ), 2 )
         + Math.pow ( ( yout1 - y1 ), 2 );
     double dist2Squared = Math.pow ( ( xout2 - x1 ), 2 )
         + Math.pow ( ( yout2 - y1 ), 2 );
 
     // correct solution
     double xout, yout;
 
     if ( dist1Squared < dist2Squared )
     {
       xout = xout1;
       yout = yout1;
     }
     else
     {
       xout = xout2;
       yout = yout2;
     }
 
     return getAttributes ().createPoint ( xout, yout );
   }
 
 
   /**
    * {@inheritDoc}
    * 
    * @see VertexView#getRenderer()
    */
   @Override
   public final CellViewRenderer getRenderer ()
   {
     return this.ellipseRenderer;
   }
 }
