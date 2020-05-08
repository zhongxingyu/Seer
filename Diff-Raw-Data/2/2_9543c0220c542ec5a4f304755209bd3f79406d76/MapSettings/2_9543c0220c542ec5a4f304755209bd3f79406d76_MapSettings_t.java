 package lightbeam.editor;
 
 
 import java.awt.Cursor;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 import javax.swing.border.TitledBorder;
 
 import core.GameObjects;
 import core.tilestate.ITileState;
 
 
 public class MapSettings extends GameObjects
 {
 	private JPanel panelSettings	= new JPanel();
 	
 	private JTextField txtRows		= new JTextField();
 	private JTextField txtCols		= new JTextField();
 	
 	private JButton btnRowsUp		= new JButton();
 	private JButton btnRowsDown		= new JButton();
 	private JButton btnColsUp		= new JButton();
 	private JButton btnColsDown		= new JButton();
 	
 	private ITileState oldTileState	= null;
 	private ITileState curTileState	= null;
 	
 	private MapArea maparea			= null;
 
 	final static Cursor CURSOR_HAND				= new Cursor( Cursor.HAND_CURSOR );
 	final static Cursor CURSOR_DEFAULT			= new Cursor( Cursor.DEFAULT_CURSOR );
 	
 	private int lblWidth			= 59;
 	private int txtWidth			= 30;
 	private int btnWidth			= 16;
 	private int btnHeight			= 16;
 	
 	private int margin_left			= 15;
 	private int margin_right		= 8;
 	private int margin_top			= 15;
 	private int margin_bottom		= 8;
 	private int ctrlHeight			= 32;
 	
 	public MapSettings( MapArea maparea, int rows, int cols )
 	{
 		this.maparea		= maparea;
 		
 		panelSettings.setBorder( new TitledBorder( "Einstellungen:" ) );
 		panelSettings.setLayout( null );
 		
 		JPanel pnlRows		= new JPanel();		
 		JPanel pnlCols		= new JPanel();
 		
 		pnlRows.setLayout( null );
 		pnlCols.setLayout( null );
 		
 		JLabel lblRows	= new JLabel( "Zeilen:" );
 		lblRows.setBounds( margin_left, margin_top, lblWidth, ctrlHeight );
 		
 		JLabel lblCols	= new JLabel( "Spalten:" );
 		lblCols.setBounds( margin_left, lblRows.getBounds().y + lblRows.getBounds().height + 5, lblWidth, ctrlHeight );
 
 		this.txtRows.setText( rows + "" );
 		this.txtRows.setBounds( margin_left + lblWidth, margin_top, txtWidth, ctrlHeight );
 		this.txtRows.setHorizontalAlignment( JTextField.CENTER );
 		this.txtRows.setEditable( false );
 		
 		this.txtCols.setText( cols + "" );
 		this.txtCols.setBounds( margin_left + lblWidth, lblCols.getBounds().y , txtWidth, ctrlHeight );
 		this.txtCols.setHorizontalAlignment( JTextField.CENTER );
 		this.txtCols.setEditable( false );
 
 		// Spielfeldzeile hinzufgen: 
 		this.btnRowsUp.setIcon( new ImageIcon( "./src/fx/Lightbeam/editor/palette/btnUp.png" ) );		
 		this.btnRowsUp.setBounds( 2 * margin_left + txtRows.getBounds().x, txtRows.getBounds().y, btnWidth, btnHeight );
 		this.btnRowsUp.addMouseListener( new MouseAdapter(){public void mouseReleased(MouseEvent e){
 			oldTileState	= MapSettings.this.eTileset.getSelected();
 			curTileState	= MapSettings.this.eTileset.tile( 1 );
 
 			MapSettings.this.eTileset.setSelected( curTileState );
 			MapSettings.this.maparea.addRow();
 			MapSettings.this.eTileset.setSelected( oldTileState );
 			
 			int amountRows	= Integer.parseInt( MapSettings.this.txtRows.getText() ) + 1;
 			MapSettings.this.txtRows.setText( amountRows + "" );
 		}});
 		
 		this.btnRowsUp.addMouseListener(new MouseAdapter(){public void mouseEntered( MouseEvent e ) 
 		{
 			MapSettings.this.btnRowsUp.setCursor( MapSettings.CURSOR_HAND );
 		}});
 		this.btnRowsUp.addMouseListener(new MouseAdapter(){public void mouseExited( MouseEvent e ) 
 		{
 			MapSettings.this.btnRowsUp.setCursor( MapSettings.CURSOR_DEFAULT );
 		}});
 		
 		// Spielfeldzeile entfernen:
 		this.btnRowsDown.setIcon( new ImageIcon( "./src/fx/Lightbeam/editor/palette/btnDown.png" ) );
 		this.btnRowsDown.setBounds( 2 * margin_left + txtRows.getBounds().x, txtRows.getBounds().y + txtRows.getBounds().height - btnHeight, btnWidth, btnHeight );
 		this.btnRowsDown.addMouseListener( new MouseAdapter(){public void mouseClicked(MouseEvent e){
 			if( MapSettings.this.maparea.delRow() == true )
 			{
 				int amountRows	= Integer.parseInt( MapSettings.this.txtRows.getText() ) - 1;
 				MapSettings.this.txtRows.setText( amountRows + "" );
 			}
 		}});
 		this.btnRowsDown.addMouseListener(new MouseAdapter(){public void mouseEntered( MouseEvent e ) 
 		{
 			MapSettings.this.btnRowsDown.setCursor( MapSettings.CURSOR_HAND );
 		}});
 		this.btnRowsDown.addMouseListener(new MouseAdapter(){public void mouseExited( MouseEvent e ) 
 		{
 			MapSettings.this.btnRowsDown.setCursor( MapSettings.CURSOR_DEFAULT );
 		}});
 
 		
 		this.btnColsUp.setIcon( new ImageIcon( "./src/fx/Lightbeam/editor/palette/btnUp.png" ) );		
 		this.btnColsUp.setBounds( 2 * margin_left + txtCols.getBounds().x, txtCols.getBounds().y, btnWidth, btnHeight );
 		this.btnColsUp.addMouseListener( new MouseAdapter(){public void mouseClicked(MouseEvent e){
 			oldTileState	= MapSettings.this.eTileset.getSelected();
 			curTileState	= MapSettings.this.eTileset.tile( 1 );
 			
 			MapSettings.this.eTileset.setSelected( curTileState );
 			MapSettings.this.maparea.addCol();
 			MapSettings.this.eTileset.setSelected( oldTileState );
 			
 			int amountCols	= Integer.parseInt( MapSettings.this.txtCols.getText() ) + 1;
 			MapSettings.this.txtCols.setText( amountCols + "" );
 		}});
 		this.btnColsUp.addMouseListener(new MouseAdapter(){public void mouseEntered( MouseEvent e ) 
 		{
 			MapSettings.this.btnColsUp.setCursor( MapSettings.CURSOR_HAND );
 		}});
 		this.btnColsUp.addMouseListener(new MouseAdapter(){public void mouseExited( MouseEvent e ) 
 		{
 			MapSettings.this.btnColsUp.setCursor( MapSettings.CURSOR_DEFAULT );
 		}});
 
 
 		this.btnColsDown.setIcon( new ImageIcon( "./src/fx/Lightbeam/editor/palette/btnDown.png" ) );
 		this.btnColsDown.setBounds( 2 * margin_left + txtCols.getBounds().x, txtCols.getBounds().y + txtRows.getBounds().height - btnHeight, btnWidth, btnHeight );
 		this.btnColsDown.addMouseListener( new MouseAdapter(){public void mouseClicked(MouseEvent e){
 			if( MapSettings.this.maparea.delCol() == true )
 			{
 				int amountCols	= Integer.parseInt( MapSettings.this.txtCols.getText() ) - 1;
 				MapSettings.this.txtCols.setText( amountCols + "" );
 			}
 		}});
 		this.btnColsDown.addMouseListener(new MouseAdapter(){public void mouseEntered( MouseEvent e ) 
 		{
 			MapSettings.this.btnColsDown.setCursor( MapSettings.CURSOR_HAND );
 		}});
 		this.btnColsDown.addMouseListener(new MouseAdapter(){public void mouseExited( MouseEvent e ) 
 		{
 			MapSettings.this.btnColsDown.setCursor( MapSettings.CURSOR_DEFAULT );
 		}});
 		
 		int iWidth	= margin_left + lblWidth + txtWidth + btnWidth + margin_right;
 		int iHeight	= txtCols.getBounds().y + txtCols.getBounds().height + margin_bottom;
 		
 		this.panelSettings.add( lblRows );
 		this.panelSettings.add( this.txtRows );
 		this.panelSettings.add( this.btnRowsUp );
 		this.panelSettings.add( this.btnRowsDown );
 		
 		this.panelSettings.add( lblCols );
 		this.panelSettings.add( this.txtCols );
 		this.panelSettings.add( this.btnColsUp );
 		this.panelSettings.add( this.btnColsDown );
 		
		this.panelSettings.setBounds( 5, 85, iWidth, iHeight );	
 	}
 	
 	public JPanel panel() { return this.panelSettings; }
 	
 	public void resetSettings( int rows, int cols )	
 	{
 		this.oldTileState	= null;
 		this.curTileState	= null;
 
 		this.txtRows.setText( rows + "" );
 		this.txtCols.setText( cols + "" );
 	}
 }
