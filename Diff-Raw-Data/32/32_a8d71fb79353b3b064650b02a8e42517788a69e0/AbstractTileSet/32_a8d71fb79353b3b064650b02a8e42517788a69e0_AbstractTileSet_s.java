 package core.tilefactory;
 
 import java.awt.image.BufferedImage;
 import core.tilestate.ITileState;
 
 public abstract class AbstractTileSet implements ITileState
 {
 	private String type						= null;
 	private BufferedImage image				= null;
 
 	public AbstractTileSet( String type, BufferedImage image )
 	{
 		this.type	= type;		
 		this.image	= image;
 	}
 	
 	public String type()				{ return this.type;		}
 	public BufferedImage image()		{ return this.image;	}
 }
