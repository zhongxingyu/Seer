 package de.unisiegen.gtitool.core.parser.style;
 
 
 import java.awt.Component;
 import java.awt.Font;
 import java.awt.FontMetrics;
 import java.awt.Graphics;
 import java.awt.event.MouseEvent;
 
 import javax.swing.JComponent;
 import javax.swing.JLabel;
 import javax.swing.border.EmptyBorder;
 
 
 /**
  * This class implements the {@link PrettyPrintable} {@link Component}.
  * 
  * @author Christian Fehler
  */
 public final class PrettyStringComponent extends JLabel
 {
 
   /**
    * The serial version uid.
    */
   private static final long serialVersionUID = 6078784158332697414L;
 
 
   /**
    * The used font.
    */
   private static final Font FONT = new Font ( "Dialog", Font.PLAIN, 12 ); //$NON-NLS-1$
 
 
   /**
    * The {@link PrettyString} of this component.
    */
   private PrettyString prettyString;
 
 
   /**
    * The center horizontal value.
    */
   private boolean centerHorizontal = false;
 
 
   /**
    * The center vertical value.
    */
   private boolean centerVertical = false;
 
 
   /**
    * Initializes the {@link PrettyStringComponent}.
    */
   public PrettyStringComponent ()
   {
     this ( null );
   }
 
 
   /**
    * Initializes the {@link PrettyStringComponent}.
    * 
    * @param prettyString The {@link PrettyString}.
    */
   public PrettyStringComponent ( PrettyString prettyString )
   {
     super ();
     this.prettyString = prettyString;
     setBorder ( new EmptyBorder ( 1, 1, 1, 1 ) );
 
     // used to calculate the preferered size
     setText ( "PrettyString" ); //$NON-NLS-1$
   }
 
 
   /**
    * Returns the prettyString.
    * 
    * @return The prettyString.
    * @see #prettyString
    */
   public final PrettyString getPrettyString ()
   {
     return this.prettyString;
   }
 
 
   /**
    * {@inheritDoc}
    * 
    * @see JComponent#getToolTipText(MouseEvent)
    */
   @Override
   public final String getToolTipText ( @SuppressWarnings ( "unused" )
   MouseEvent event )
   {
     if ( this.prettyString.toString ().equals ( "" ) ) //$NON-NLS-1$
     {
       return null;
     }
     return this.prettyString.toHTMLString ();
   }
 
 
   /**
    * Returns the center horizontal value.
    * 
    * @return The center horizontal value.
    * @see #centerHorizontal
    */
   public final boolean isCenterHorizontal ()
   {
     return this.centerHorizontal;
   }
 
 
   /**
    * Returns the center vertical value.
    * 
    * @return The center vertical value.
    * @see #centerVertical
    */
   public final boolean isCenterVertical ()
   {
     return this.centerVertical;
   }
 
 
   /**
    * {@inheritDoc}
    * 
    * @see JComponent#paintComponent(Graphics)
    */
   @Override
   protected final void paintComponent ( Graphics g )
   {
     g.setColor ( getBackground () );
     g.fillRect ( 0, 0, getWidth (), getHeight () );
     g.setFont ( FONT );
     FontMetrics metrics = g.getFontMetrics ();
 
     PrettyString usedPrettyString = new PrettyString ();
     usedPrettyString.addPrettyString ( this.prettyString );
 
     // short version
     if ( metrics.stringWidth ( this.prettyString.toString () ) > getWidth () )
     {
       String dots = " ..."; //$NON-NLS-1$
       PrettyToken lastPrettyToken = null;
       while ( ( !usedPrettyString.isEmpty () )
           && ( ( metrics.stringWidth ( usedPrettyString.toString () + dots ) ) > getWidth () ) )
       {
         lastPrettyToken = usedPrettyString.removeLastPrettyToken ();
       }
 
       if ( lastPrettyToken != null )
       {
         char [] chars = lastPrettyToken.getChar ();
         int i = 0;
         String addText = ""; //$NON-NLS-1$
         while ( i < chars.length
             && ( ( metrics.stringWidth ( usedPrettyString.toString () + addText
                 + dots ) ) <= getWidth () ) )
         {
           addText += chars [ i ];
           i++ ;
         }
        usedPrettyString.addPrettyToken ( new PrettyToken ( addText.substring (
            0, addText.length () - 1 ), lastPrettyToken.getStyle () ) );
       }
 
       // if empty do not use the first space
       if ( usedPrettyString.isEmpty () )
       {
         dots = "..."; //$NON-NLS-1$
       }
 
       usedPrettyString.addPrettyToken ( new PrettyToken ( dots, Style.NONE ) );
     }
 
     int dx = 0;
     if ( this.centerHorizontal )
     {
       dx = ( getWidth () - metrics.stringWidth ( usedPrettyString.toString () ) ) / 2;
     }
 
     int y = getHeight () - 3;
     if ( this.centerVertical )
     {
       y = y - ( ( getHeight () - metrics.getHeight () ) / 2 );
     }
 
     for ( PrettyToken currentToken : usedPrettyString.getPrettyToken () )
     {
       Font font = null;
 
       if ( !currentToken.isBold () && !currentToken.isItalic () )
       {
         font = FONT;
       }
       else if ( currentToken.isBold () && currentToken.isItalic () )
       {
         font = FONT.deriveFont ( Font.BOLD | Font.ITALIC );
       }
       else if ( currentToken.isBold () )
       {
         font = FONT.deriveFont ( Font.BOLD );
       }
       else if ( currentToken.isItalic () )
       {
         font = FONT.deriveFont ( Font.ITALIC );
       }
 
       g.setFont ( font );
       g.setColor ( currentToken.getColor () );
       char [] chars = currentToken.getChar ();
       for ( int i = 0 ; i < chars.length ; i++ )
       {
         g.drawChars ( chars, i, 1, dx, y );
         dx += metrics.charWidth ( chars [ i ] );
       }
     }
   }
 
 
   /**
    * Sets the center horizontal value.
    * 
    * @param centerHorizontal The center horizontal value to set.
    * @see #centerHorizontal
    */
   public final void setCenterHorizontal ( boolean centerHorizontal )
   {
     this.centerHorizontal = centerHorizontal;
   }
 
 
   /**
    * Sets the center vertical.
    * 
    * @param centerVertical The center vertical to set.
    * @see #centerVertical
    */
   public final void setCenterVertical ( boolean centerVertical )
   {
     this.centerVertical = centerVertical;
   }
 
 
   /**
    * Sets the prettyString.
    * 
    * @param prettyString The prettyString to set.
    * @see #prettyString
    */
   public final void setPrettyString ( PrettyString prettyString )
   {
     this.prettyString = prettyString;
   }
 }
