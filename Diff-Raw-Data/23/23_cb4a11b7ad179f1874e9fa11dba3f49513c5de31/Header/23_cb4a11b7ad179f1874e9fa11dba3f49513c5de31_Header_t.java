 package gui.objects.headers;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import gui.objects.FontStyles;
 
 import org.mt4j.components.visibleComponents.shapes.MTRectangle;
 import org.mt4j.components.visibleComponents.widgets.MTTextArea;
 import org.mt4j.util.MTColor;
 
 import processing.core.PApplet;
 
 public class Header extends MTRectangle {
 
 	protected MTColor sHeaderBgColor;
 	protected MTTextArea txtLog;
 	protected List<String> messages = new ArrayList<String>();
 	protected int maxLines = 4;
	protected String txtBuffer = "";
 	
 	/**
 	 * Constructor
 	 * @param pApplet
 	 * @param x
 	 * @param y
 	 * @param width
 	 * @param height
 	 */
 	public Header(PApplet pApplet, float x, float y, float width, float height) {
 		super(pApplet, x, y, width, height);
 		
 		sHeaderBgColor = new MTColor( 170, 50, 70, 255 );
 		setFillColor( sHeaderBgColor );
 		
 		txtLog = new MTTextArea(pApplet, x, y, width, height);
 		txtLog.setFont( FontStyles.getFontMedium(pApplet) );
 		txtLog.setFillColor( new MTColor(0, 0, 0, 0) );
 		
 		updateLog( "Log:" );
 		
 		txtLog.unregisterAllInputProcessors();
 		txtLog.removeAllGestureEventListeners();
 		
 		addChild(txtLog);		
 		
 		unregisterAllInputProcessors();
 		removeAllGestureEventListeners();
 		
 	}
 	
 	
 	/**
 	 * Updates the log messages
 	 * @param msg
 	 */
 	public void updateLog(String msg){
 		
 		// add message to log list
 		messages.add( msg );
 		
 		// remove all old messages
 		while( messages.size() > maxLines ){
 			messages.remove(0);
		}		
 				
 		// update txtLog
		txtBuffer = "";
 		for( String s : messages ){
			txtBuffer += s + "\n";
 		}
 		
 	}
	
	@Override
	public void updateComponent(long timeDelta) {
		super.updateComponent(timeDelta);
		
		txtLog.setText(txtBuffer);
	}
 
 }
