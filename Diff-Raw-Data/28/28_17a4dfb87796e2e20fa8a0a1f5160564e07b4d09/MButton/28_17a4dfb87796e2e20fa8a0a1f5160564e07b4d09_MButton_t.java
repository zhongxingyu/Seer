 package de.miij.ui.comp;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.FocusEvent;
 import java.awt.event.FocusListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 
 import javax.swing.JButton;
 
 import de.miij.language.ILanguageSupport;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 
 public class MButton extends JButton implements ILanguageSupport
 {
 
 	public Connector clicked = null;
 	public Connector focusGained = null;
 	public Connector focusLost = null;
 	public Connector mousePressed = null;
 	public Connector mouseReleased = null;
 	public Connector mouseEntered = null;
 	public Connector mouseClicked = null;
 	public Connector mouseExited = null;
 	public Connector mouseDragged = null;
 	public Connector mouseMoved = null;
 	public Connector enterPressed = null;
 	public Connector escapePressed = null;
 	
 	public MButton()
 	{
 		super();
 		init();
 	}
 	
 	public MButton( String text )
 	{
 		super( text );
 		init();
 	}
 	
 	public MButton clicked( Connector c )
 	{
 		clicked = c;
 		return this;
 	}
 	
 	public MButton focusGained( Connector c )
 	{
 		focusGained = c;
 		return this;
 	}
 	
 	public MButton focusLost( Connector c )
 	{
 		focusLost = c;
 		return this;
 	}
 	
 	public MButton mousePressed( Connector c )
 	{
 		mousePressed = c;
 		return this;
 	}
 	
 	public MButton mouseEntered( Connector c )
 	{
 		mouseEntered = c;
 		return this;
 	}
 	
 	public MButton mouseClicked( Connector c )
 	{
 		mouseClicked = c;
 		return this;
 	}
 	
 	public MButton mouseExited( Connector c )
 	{
 		mouseExited = c;
 		return this;
 	}
 	
 	public MButton mouseDragged( Connector c )
 	{
 		mouseDragged = c;
 		return this;
 	}
 	
 	public MButton mouseMoved( Connector c )
 	{
 		mouseMoved = c;
 		return this;
 	}
 	
 	public MButton mouseReleased( Connector c )
 	{
 		mouseReleased = c;
 		return this;
 	}
 	
 	public MButton enterPressed( Connector c )
 	{
 		enterPressed = c;
 		return this;
 	}
 	
 	public MButton escapePressed( Connector c )
 	{
 		escapePressed = c;
 		return this;
 	}
 	
 	private void init()
 	{
 		addActionListener( new ActionListener() {
 			public void actionPerformed( ActionEvent e )
 			{
 				// Hier die in clicked gespeicherte Methode aufrufen
 				if( clicked != null )
 					clicked.action( e );
 			}
 		});
 		addFocusListener( new FocusListener() {
 
 			public void focusGained( FocusEvent e )
 			{
 				if( focusGained != null )
 					focusGained.action( e );
 			}
 
 			public void focusLost( FocusEvent e )
 			{
 				if( focusLost != null )
 					focusLost.action( e );
 			}
 			
 		});
 		addMouseListener( new MouseListener() {
 
 			public void mouseClicked( MouseEvent e )
 			{
 				if( mouseClicked != null )
 					mouseClicked.action( e );
 			}
 
 			public void mouseEntered( MouseEvent e )
 			{
 				if( mouseEntered != null )
 					mouseEntered.action( e );
 			}
 
 			public void mouseExited( MouseEvent e )
 			{
 				if( mouseExited != null )
 					mouseExited.action( e );
 			}
 
 			public void mousePressed( MouseEvent e )
 			{
 				if( mousePressed != null )
 					mousePressed.action( e );
 			}
 
 			public void mouseReleased( MouseEvent e )
 			{
 				if( mouseReleased != null )
 					mouseReleased.action( e );
 			}
 			
 		});
 		addMouseMotionListener( new MouseMotionListener() {
 
 			public void mouseDragged( MouseEvent e )
 			{
 				if( mouseDragged != null )
 					mouseDragged.action( e );
 			}
 
 			public void mouseMoved( MouseEvent e )
 			{
 				if( mouseMoved != null )
 					mouseMoved.action( e );
 			}
 			
 		});
 		
 		addKeyListener(new KeyAdapter() {
 
 			public void keyPressed( KeyEvent e )
 			{
				if(e.getKeyCode() == KeyEvent.VK_ENTER && enterPressed != null)
 					enterPressed.action( e );
				else if(e.getKeyCode() == KeyEvent.VK_ESCAPE && escapePressed != null)
 					escapePressed.action( e );
 			}
 		});
 	}
 	
 	public String text()
 	{
 		return getText();
 	}
 	
 	public MButton text( String text )
 	{
 		setText( text );
 		return this;
 	}
 
 }
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
