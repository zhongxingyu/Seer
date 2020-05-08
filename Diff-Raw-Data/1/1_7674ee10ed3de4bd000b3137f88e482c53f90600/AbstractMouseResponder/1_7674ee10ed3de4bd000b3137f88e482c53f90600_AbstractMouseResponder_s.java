 package de.sciss.swingosc;
 
 import java.lang.reflect.InvocationTargetException;
 
 public abstract class AbstractMouseResponder
 extends AbstractResponder
 {
 	private final Frame		f;
 	protected boolean		acceptsMouseOver;
 	
 	protected AbstractMouseResponder( Object objectID, int numReplyArgs, Object frameID  )
 	throws IllegalAccessException, NoSuchMethodException, InvocationTargetException
 	{
 		super( objectID, numReplyArgs );
 
 		this.f	= frameID == null ? null : (Frame) client.getObject( frameID );
 		if( f != null ) {
 			f.registerMouseResponder( this );
 			acceptsMouseOver = f.getAcceptMouseOver();
 		} else {
 			acceptsMouseOver = true;
 		}
 	}
 
 	public void setAcceptMouseOver( boolean onOff ) {
 		acceptsMouseOver = onOff;
 	}
 	
 	public boolean getAcceptMouseOver()
 	{
 		return acceptsMouseOver;
 	}
 
 	public void remove()
 	throws IllegalAccessException, InvocationTargetException
 	{
 		if( f != null ) f.unregisterMouseResponder( this );
 		super.remove();
 	}
 }
