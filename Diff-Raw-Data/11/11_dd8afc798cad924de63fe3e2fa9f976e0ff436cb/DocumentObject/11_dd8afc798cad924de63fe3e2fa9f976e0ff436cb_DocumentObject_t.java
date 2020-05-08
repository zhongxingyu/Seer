 package document_object;
 
 import java.awt.AWTEvent;
 import java.awt.event.*;
 import java.util.Observable;
 import java.util.Observer;
 import java.util.ArrayList;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.lang.Math;
 
 import processing.core.*;
 
 public class DocumentObject extends Observable implements Observer,
 		Comparable<DocumentObject> {
 	
 	protected PApplet applet;
 	protected int width = 0;
 	protected int height = 0;
 	protected int pos_x = 0;
 	protected int pos_y = 0;
 	protected int z_index = 0;
 	protected ArrayList<DocumentObject> children;
 	protected DocumentObject parent = null;
 	protected Timer animation_timer = null;
 
 	public DocumentObject( PApplet p ) {
 		this.setApplet( p );
 		this.children = new ArrayList<DocumentObject>();
 	}
 
 	public PApplet getApplet() {
 		return applet;
 	}
 
 	public void setApplet( PApplet applet ) {
 		this.applet = applet;
 	}
 	
 	public void render() {
 		eachChildDo( new Lambda<DocumentObject>() {
 			public void run( DocumentObject child ) {
 				child.render();
 			}
 		} );
 	}
 
 	public void eachChildDo( Lambda<DocumentObject> block ) {
 		for ( int i = 0; i < getChildren().size(); ++i ) {
 			block.run( getChildren().get( i ) );
 		}
 	}
 
 	public void appendChild( DocumentObject child ) {
 		if ( !containsChild( child ) ) {
 			child.setParent( this );
 			children.add( child );
 		}
 	}
 
 	public void removeChild( DocumentObject child ) {
 		int index_of_child = children.indexOf( child );
 		if ( index_of_child != -1 ) {
 			child.noParent();
 			children.remove( index_of_child );
 		}
 	}
 
 	public Boolean containsChild( DocumentObject child ) {
 		return children.contains( child );
 	}
 
 	public ArrayList<DocumentObject> getChildren() {
 		return children;
 	}
 
 	public void truncateChildren() {
 		for ( int i = 0; i < children.size(); ++i ) {
 			children.get( i ).noParent();
 		}
 		children.clear();
 	}
 
 	public void noParent() {
 		this.parent = null;
 		this.setZIndex( 0 );
 	}
 	  
 	public void setParent( DocumentObject parent ) {
 		this.parent = parent;
 		this.setZIndex( 1 + parent.getZIndex() );
 	}
 
 	public DocumentObject getParent() {
 		return parent;
 	}
 
 	public Boolean hasParent() {
 		return !( this.parent == null );
 	}
 
 	public int getZIndex() {
 		return this.z_index;
 	}
 
 	public void setZIndex( int z_index) {
 		this.z_index = z_index;
 		final int child_z_index = 1 + z_index;
 		eachChildDo( new Lambda<DocumentObject>() {
 			public void run( DocumentObject child ) {
 				child.setZIndex( 1 + child_z_index );
 			}
 		});
 	}
 
 	public void update( Observable observable, Object arg ) {
 		if ( arg instanceof AWTEvent ) {
 			AWTEvent e = (AWTEvent)arg;
 			handle( e );
 			this.getParent().update( observable, arg );
 		}
 	}
 
 	public int compareTo( DocumentObject d ) {
 		final int z_index1 = this.getZIndex();
 	    final int z_index2 = d.getZIndex();
 	    return ( z_index1 == z_index2 ) ?
 	    		( 0 ) : 
 	    			( ( z_index1 > z_index2 ) ? -1 : 1 );
 	}
 
 	public Boolean intersect( AWTEvent e ) {
 		if ( e instanceof MouseEvent ) {
 			return this.intersectMouse( (MouseEvent)e );
 		}
 		return true;
 	}
 
 	public int getPosX() {
 		return this.pos_x;
 	}
 	
 	public void setPosX( int pos_x ) {
 		this.pos_x = pos_x;
 	}
 
 	public int getPosY() {
 		return this.pos_y;
 	}
 	
 	public void setPosY( int pos_y ) {
 		this.pos_y = pos_y;
 	}
 
 	public void setPos( int pos_x, int pos_y ) {
 		this.setPosX( pos_x );
 		this.setPosY( pos_y );
 	}
 	
 	public int getWidth() {
 		return this.width;
 	}
 
 	public void setWidth( int width ) {
 		this.width = width;
 	}
 	
 	public int getHeight() {
 		return this.height;
 	}
 	
 	public void setHeight( int height ) {
 		this.height = height;
 	}
 	
 	public void setDim( int width, int height ) {
 		this.setWidth( width );
 		this.setHeight( height );
 	}
 	
 	public void animate( final String event_type, float start, float end, int duration ) {
 		if ( this.animation_timer == null ) {
 			this.animation_timer = new Timer();
 		}
 		int fps = 60;
 		if ( start == end) {
 			return;
 		}
 		final double step = (( end - start ) / ( (float)duration * (float)fps / 1000.0 ));
 		int outstanding = 0;
 		int fps_step = Math.round( 1000 / fps );
 		float current_pos = start;
 		scheduleAnimationTask( event_type + AnimationEvent.ANIMATION_START_MASK,
 				current_pos, outstanding );
 		while( Math.abs( current_pos - end ) >= Math.abs( step ) ) {
 			current_pos += step;
 			outstanding += fps_step;
 			scheduleAnimationTask( event_type, current_pos, outstanding );
 		}
 		scheduleAnimationTask( event_type, end, fps_step + outstanding );
 		scheduleAnimationTask( event_type + AnimationEvent.ANIMATION_COMPLETE_MASK,
 				current_pos, 2 * fps_step + outstanding );
 	}
 	
 	public void animate( final String event_type, int start, int end, int duration ) {
 		if ( this.animation_timer == null ) {
 			this.animation_timer = new Timer();
 		}
 		int fps = 60;
 		if ( start == end) {
 			return;
 		}
 		final int step = (int)Math.round( (( end - start ) / ( (float)duration * (float)fps / 1000.0 ) ) );
 		int outstanding = 0;
 		int fps_step = Math.round( 1000 / fps );
 		int current_pos = start;
 		scheduleAnimationTask( event_type + AnimationEvent.ANIMATION_START_MASK,
 				current_pos, outstanding );
 		while( Math.abs( current_pos - end ) >= Math.abs( step ) ) {
 			current_pos += step;
 			outstanding += fps_step;
 			scheduleAnimationTask( event_type, current_pos, outstanding );
 		}
 		scheduleAnimationTask( event_type, end, fps_step + outstanding );
 		scheduleAnimationTask( event_type + AnimationEvent.ANIMATION_COMPLETE_MASK,
 				current_pos, 2 * fps_step + outstanding );
 	}
 	
 	protected void scheduleAnimationTask( final String event_type,
 			final float val, int delay ) {
		final DocumentObject self = this;
 		this.animation_timer.schedule( new TimerTask() {
 			public void run() {
				captureEvent( new AnimationEvent<Float>( self, event_type, val ) );
 			}
 		}, delay );
 	}
 	
 	protected void scheduleAnimationTask( final String event_type,
 			final int val, int delay ) {
		final DocumentObject self = this;
 		this.animation_timer.schedule( new TimerTask() {
 			public void run() {
				captureEvent( new AnimationEvent<Integer>( self, event_type, val ) );
 			}
 		}, delay );
 	}
 	
 	public void stopAnimation() {
 		if ( this.animation_timer != null ) {
 			this.animation_timer.cancel();
 			this.animation_timer = new Timer();
 		}
 	}
 	
 	protected Boolean intersectMouse( MouseEvent me ) {
 		return ( me.getX() > getPosX() && 
 				me.getY() > getPosY() && 
 				me.getX() < ( getPosX() + this.getWidth() ) && 
 				me.getY() < ( getPosY() + getHeight() ) );
 	}
 	
 	protected void onMouseMove( CatchableMouseEvent e ) {}
 	protected void onMouseDown( CatchableMouseEvent e ) {}
 	protected void onMouseUp( CatchableMouseEvent e ) {}
 	protected void onMouseDrag( CatchableMouseEvent e ) {}
 	protected void onMouseClick( CatchableMouseEvent e ) {}
 	
 	protected void handle( AWTEvent e ) {
 		switch ( e.getID() ) {
 		case MouseEvent.MOUSE_PRESSED:
 			this.onMouseDown( (CatchableMouseEvent)e );
 			break;
 		case MouseEvent.MOUSE_RELEASED:
 			this.onMouseUp( (CatchableMouseEvent)e );
 			break;
 		case MouseEvent.MOUSE_CLICKED:
 			this.onMouseClick( (CatchableMouseEvent)e );
 			break;
 		case MouseEvent.MOUSE_DRAGGED:
 			this.onMouseDrag( (CatchableMouseEvent)e );
 			break;
 		case MouseEvent.MOUSE_MOVED:
 			this.onMouseMove( (CatchableMouseEvent)e );
 			break;
 		}
 	}
 
 	protected int captureEvent( AWTEvent e ) {
 		int captured = -1;
 		if ( this.children.size() > 0 ) {
 			for ( int i = 0; i < children.size(); ++i ) {
 				if ( children.get( i ).intersect( e ) ) {
 					captured = 0;
 					children.get( i ).captureEvent( e );
 				}
 			}
 		}
 		if ( captured != 0 ) {
 			this.bubbleEvent( e );
 		}
 		return captured;
 	}
 	
 	protected int bubbleEvent( AWTEvent e ) {
 		int bubbled = -1;
 		this.handle( e );
 		if ( this.hasParent()) {
 			if ( e instanceof Catchable ) {
 				 if ( ( (Catchable)e ).isStopped() ) {
 					 return bubbled;
 				 } 
 			}
 			bubbled = 0; 
 			this.parent.bubbleEvent( e );
 		}
 		return bubbled;
 	}
 }
