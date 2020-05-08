 package net.intensicode.runme;
 
 import javax.swing.JFrame;
 import java.awt.*;
 
 public final class MIDletFrame extends JFrame implements DisplayContext
     {
     public MIDletFrame( final SystemContext aSystemContext )
         {
         mySystemContext = aSystemContext;
         setDefaultCloseOperation( EXIT_ON_CLOSE );
         }
 
     // From DisplayContext
 
     public final boolean isFullScreen()
         {
         final GraphicsDevice device = getGraphicsDevice();
         if ( device.isFullScreenSupported() ) return device.getFullScreenWindow() == this;
 
         final DisplayMode mode = device.getDisplayMode();
         return getX() == 0 && getY() == 0 && getWidth() == mode.getWidth() && getHeight() == mode.getHeight();
         }
 
     public final GraphicsDevice getGraphicsDevice()
         {
         final GraphicsDevice device = getGraphicsConfiguration().getDevice();
         if ( device == null ) throw new IllegalStateException();
         return device;
         }
 
     public final GraphicsConfiguration getGraphicsConfiguration()
         {
         final GraphicsConfiguration configuration = super.getGraphicsConfiguration();
         if ( configuration == null ) throw new IllegalStateException();
         return configuration;
         }
 
     public final void showTitleAndBorder()
         {
         setUndecorated( false );
         }
 
     public final void hideTitleAndBorder()
         {
         setUndecorated( true );
         }
 
     public final void showAndFocus()
         {
         setVisible( true );
         requestFocus();
         }
 
     public final void hideAndDispose()
         {
         setVisible( false );
         dispose();
         }
 
     public final void makeFullScreen()
         {
         final GraphicsDevice device = getGraphicsDevice();
         if ( device.isFullScreenSupported() ) device.setFullScreenWindow( this );
 
         final DisplayMode displayMode = device.getDisplayMode();
         setBounds( 0, 0, displayMode.getWidth(), displayMode.getHeight() );
         }
 
     public final void unmakeFullScreen()
         {
         final GraphicsDevice device = getGraphicsDevice();
         if ( device.isFullScreenSupported() ) device.setFullScreenWindow( null );
         }
 
     public final void centerAndResizeTo( final int aWidth, final int aHeight )
         {
        getContentPane().setPreferredSize( new Dimension( aWidth, aHeight ) );
        pack();

         final GraphicsDevice device = getGraphicsDevice();
         final DisplayMode mode = device.getDisplayMode();
        final Dimension frameSize = getSize();
        final int x = ( mode.getWidth() - frameSize.width ) / 2;
        final int y = ( mode.getHeight() - frameSize.height ) / 2;
        setBounds( x, y, frameSize.width, frameSize.height );
         }
 
     // From Window
 
     public final void setVisible( final boolean aVisibleFlag )
         {
         if ( aVisibleFlag ) updateTitle();
         super.setVisible( aVisibleFlag );
         }
 
     // From Container
 
     public final void paint( final Graphics aGraphics )
         {
         final DisplayTransformation transformation = mySystemContext.displayTransformation;
         updateDisplayTransformation( transformation );
 
         final Dimension size = getInnerScreenSize();
 
         final Insets insets = getInsets();
         final int x = transformation.xOffset - insets.left;
         final int y = transformation.yOffset - insets.top;
 
         aGraphics.translate( insets.left, insets.top );
 
         aGraphics.setColor( Color.DARK_GRAY );
         aGraphics.fillRect( 0, 0, x, size.height );
         aGraphics.fillRect( 0, 0, size.width, y );
         aGraphics.fillRect( x + transformation.scaledWidth, 0, size.width - x - transformation.scaledWidth, size.height );
         aGraphics.fillRect( 0, y + transformation.scaledHeight, size.width, size.height - y - transformation.scaledHeight );
 
         aGraphics.translate( -insets.left, -insets.top );
 
         final DisplayBuffer buffer = mySystemContext.displayBuffer;
         buffer.renderInto( aGraphics, transformation.xOffset, transformation.yOffset, transformation.scaledWidth, transformation.scaledHeight );
         }
 
     // Implementation
 
     private void updateTitle()
         {
         final StringBuilder builder = new StringBuilder();
         builder.append( mySystemContext.midlet.getClass().getSimpleName() );
         builder.append( " (IntensiCode RunME) - www.intensicode.net / www.berlinfactor.com" );
         setTitle( builder.toString() );
         }
 
     private Dimension getInnerScreenSize()
         {
         final Insets insets = getInsets();
         myInnerScreenSize.width = getWidth() - insets.left - insets.right;
         myInnerScreenSize.height = getHeight() - insets.top - insets.bottom;
         return myInnerScreenSize;
         }
 
     private void updateDisplayTransformation( final DisplayTransformation aDisplayTransformation )
         {
         final DisplayBuffer buffer = mySystemContext.displayBuffer;
         final int bufferWidth = buffer.width();
         final int bufferHeight = buffer.height();
 
         final Dimension screenSize = getInnerScreenSize();
 
         final float xScale = screenSize.width * 1.0f / bufferWidth;
         final float yScale = screenSize.height * 1.0f / bufferHeight;
         final float scale = Math.min( xScale, yScale );
 
         final int scaledWidth = (int) ( bufferWidth * scale );
         final int scaledHeight = (int) ( bufferHeight * scale );
         final int x = ( screenSize.width - scaledWidth ) / 2;
         final int y = ( screenSize.height - scaledHeight ) / 2;
 
         final Insets insets = getInsets();
         aDisplayTransformation.xOffset = x + insets.left;
         aDisplayTransformation.yOffset = y + insets.top;
         aDisplayTransformation.scaledWidth = scaledWidth;
         aDisplayTransformation.scaledHeight = scaledHeight;
         aDisplayTransformation.scale = scale;
         aDisplayTransformation.valid = true;
         }
 
 
     private MIDletContainer myContainer;
 
     private final SystemContext mySystemContext;
 
     private final Dimension myInnerScreenSize = new Dimension();
     }
