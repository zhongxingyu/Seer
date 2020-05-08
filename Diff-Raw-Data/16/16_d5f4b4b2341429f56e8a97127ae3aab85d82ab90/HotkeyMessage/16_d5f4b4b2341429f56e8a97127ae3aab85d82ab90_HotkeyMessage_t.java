 package filter;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.image.BufferedImage;
 import java.util.ArrayList;
 
 import application.Main;
 
 public class HotkeyMessage extends InformationContent {
 
 	private ArrayList<String> messages;
 	private ArrayList<Integer> timeoutList;
 	private int x, y;
 
 	public HotkeyMessage( int timeout, ContentLayerCompatible parent ) {
 		super( timeout, parent );
 		messages = new ArrayList<String>();
 		timeoutList = new ArrayList<Integer>();
		x = Main.getFromConfig("HotkeyMsgTextX");
		y = Main.getFromConfig("HotkeyMsgTextY");
 	}
 
 	@Override
 	public BufferedImage paintContent( BufferedImage img ) {
 		if ( messages.size() == 0 ){
 			return img;
 		}
 		int i = 0;
 		Graphics g = img.getGraphics();
 		g.setFont( application.Main.getFont() );
 		g.setColor( Color.green );
 		for ( String s : messages ) {
 			g.drawString(s, x, y + i * application.Main.getFont().getSize() + 6 );
 			i++;
 		}
 		g.dispose();
 		for ( i = timeoutList.size() - 1; i >= 0; i-- ){
 			timeoutList.set( i, timeoutList.get( i ) -1 );
 			if ( timeoutList.get( i ) < 0 ){
 				messages.remove( i );
 				timeoutList.remove( i );
 			}
 		}		
 		return img;
 	}
 
 	public void addEvent( String s ) {
 		messages.add( s );
 		timeoutList.add( 45 );
 		if ( messages.size() > 3 ){
 			messages.remove( 0 );
 			timeoutList.remove( 0 );
 		}
 	}
 
 }
