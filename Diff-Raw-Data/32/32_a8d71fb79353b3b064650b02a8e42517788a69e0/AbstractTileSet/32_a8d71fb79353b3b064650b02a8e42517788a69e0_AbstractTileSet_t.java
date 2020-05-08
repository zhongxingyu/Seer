 package core.tilefactory;
 
 import java.awt.image.BufferedImage;
 import core.tilestate.ITileState;
 
 public abstract class AbstractTileSet implements ITileState
 {
 	private String type						= null;
 	private BufferedImage image				= null;
	private boolean hidden					= false;
 
 	public AbstractTileSet( String type, BufferedImage image )
 	{
 		this.type	= type;		
 		this.image	= image;
 	}
 	
	public void hidden( boolean hidden )	{ this.hidden = hidden;	}
	
 	public String type()				{ return this.type;		}
 	public BufferedImage image()		{ return this.image;	}
	public boolean hidden()				{ return this.hidden;	}
 }
