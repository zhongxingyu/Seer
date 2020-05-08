 package lightbeam.editor.dialogs;
 
 import java.awt.BorderLayout;
 import java.awt.Cursor;
 import java.awt.Dimension;
 import java.awt.Rectangle;
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
import javax.swing.ListSelectionModel;
 import javax.swing.table.DefaultTableModel;
 import lightbeam.editor.MapArea;
 
 public class OpenDialog
 {
 	private final String[] mapCols		= { "Kartenname", "Kartenstatus", "Schwierigkeitsgrad" };
 	private final String extMap			= ".map";
 	
 	private JOptionPane pane			= null;
 	private SettingsDialog dSettings	= new SettingsDialog();
 	private JPanel panel				= new JPanel();
 	private JLabel lblOpen				= new JLabel( "Kartenauswahl:" );
 	private String[][] mapRows			= null;
 	private DefaultTableModel mapModel 	= new DefaultTableModel( mapRows, mapCols );
	private JTable mapTable				= new JTable( mapModel ) 
	{
		private static final long serialVersionUID = -1084225514626748678L;

		public boolean isCellEditable(int rowIndex, int vColIndex) { return false; }
    };
    
 	private JScrollPane scroll			= new JScrollPane( mapTable );
 	private MapArea mapArea				= null;
 	
 	public OpenDialog()	
 	{
 		this.panel.setLayout( null );
 		this.panel.setPreferredSize( new Dimension( 400, 400 ) );
 		
 		this.lblOpen.setBounds( new Rectangle( 10, 10, 120, 20 ) );
 		this.scroll.setBounds( new Rectangle( 10, 30, 350, 350 ) );
 		this.mapTable.setBounds( new Rectangle( 10, 30, 350, 350 ) );
 		
 		this.mapTable.setFillsViewportHeight( true );
		this.mapTable.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
 		this.mapTable.setCursor( new Cursor( Cursor.HAND_CURSOR ) );
//		this.mapTable.
 		
 		this.panel.add( this.lblOpen );
 		this.panel.add( this.scroll );
 	}
 	
 	public void showDialog()
 	{
 		this.resetTable();
 		this.fillRows();
 		
 		this.pane 		= new JOptionPane( this.panel, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION );		
 		this.pane.createDialog( "Karte ffnen" ).setVisible( true );
 
 //		if( this.pane.getValue() != null )
 //		{
 //			int selOption	= ( (Integer)this.pane.getValue() ).intValue();
 //			
 //			if( selOption == JOptionPane.OK_OPTION )
 //			{
 //				String mapName			= inpMap.getText();
 //				SettingsDialog dialog	= new SettingsDialog();
 //				String pathMaps			= dialog.getPath();
 //				
 //				if( !pathMaps.equals( "" ) && !mapName.equals( "" ) )
 //				{
 //					this.proceedSaving( pathMaps, mapName );
 //				} else
 //				{
 //					if( pathMaps.equals( "" ) )
 //					{
 //						JPanel panelNoPath	= new JPanel();
 //						
 //						panelNoPath.setLayout( new BorderLayout() );
 //						
 //						JLabel lblHint		= new JLabel( "Zur Speicherung Ihrer Karte mssen Sie einen Zielpfad festlegen!" );
 //						JLabel lblHint2		= new JLabel( "Klicken Sie auf \"Ok\" um diesen festzulegen!" );
 //						
 //						panelNoPath.add( lblHint, BorderLayout.NORTH );
 //						panelNoPath.add( lblHint2, BorderLayout.SOUTH );
 //						
 //						JOptionPane dSetPath	= new JOptionPane( panelNoPath, JOptionPane.INFORMATION_MESSAGE, JOptionPane.OK_CANCEL_OPTION );
 //						dSetPath.createDialog( "Speicherort hinterlegen!" ).setVisible( true );
 //						
 //						if( dSetPath.getValue() != null )
 //						{
 //							int selected	= ( (Integer)dSetPath.getValue() ).intValue();
 //							
 //							if( selected == 0 )
 //							{
 //								dialog.showDialog();
 //								this.showDialog();
 //							}
 //						}
 //					}
 //				}
 //			}
 //		}
 	}
 	
 	private String getMapName( String mapDest )
 	{
 		// ToDo: Prfen, ob typ in mapDest == .map!
 		try 
 		{
 			FileInputStream file	= new FileInputStream( mapDest );
 			BufferedInputStream buf	= new BufferedInputStream( file );
 	
 			try 
 			{
 				ObjectInputStream read	= new ObjectInputStream( buf );
 				try 
 				{
 					String fMapName		= (String) read.readObject();
 					
 					read.close();
 					
 					return fMapName;
 				} catch (ClassNotFoundException e) 
 				{
 					// TODO Fehler ausgeben, das map nicht gepsiechert werden kann,
 					// da Zugriff auf vorhandene Dateien nichtm glich ist, um zu berprfen, ob
 					// map bereits vorhanden!
 					read.close();
 					
 					return null;
 				}
 			} catch( IOException e )
 			{
 				return null;
 			}
 		} catch( FileNotFoundException e ) 
 		{
 			return null;
 		}		
 	}
 	
 	private void proceedSaving( String pathMaps, String mapName )
 	{
 		File dir		= new File( pathMaps );
 		String[] files	= dir.list();
 		int amount		= 0;
 		boolean mExists	= false;
 		
 		if( files != null )
 		{
 			amount		= files.length;
 			
 			for( int count = 0; count < amount; count++ )
 			{
 				String fileDest			= pathMaps + "/" + files[count];
 				String fMapName			= this.getMapName( fileDest );
 				
 				if( fMapName != null && fMapName.equals( mapName ) )
 				{
 					mExists	= true;
 					break;
 				}
 			}
 		}
 		
 		if( !mExists )
 		{
 			this.saveMap( pathMaps + "/map" + amount + this.extMap, mapName );
 		} else
 		{
 			JPanel panelQuestion	= new JPanel();
 			
 			panelQuestion.setLayout( new BorderLayout() );
 			
 			JLabel lblQuestion		= new JLabel( "Eine Karte mit dem Kartennamen \""+mapName+"\" ist bereits vorhanden!" );
 			JLabel lblQuestion2		= new JLabel( "Soll die bestehende Karte berschrieben werden?" );
 			
 			panelQuestion.add( lblQuestion, BorderLayout.NORTH );
 			panelQuestion.add( lblQuestion2, BorderLayout.SOUTH );
 			
 			JOptionPane dOverwrite	= new JOptionPane( panelQuestion, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION );
 			dOverwrite.createDialog( "Frage!" ).setVisible( true );
 			
 			if( dOverwrite.getValue() != null )
 			{
 				int selected	= ( (Integer)dOverwrite.getValue() ).intValue();
 
 				if( selected == 0 )
 				{
 					this.saveMap( pathMaps + "/map" + amount + this.extMap, mapName );
 				} else
 				{
 					this.showDialog();
 				}
 			}
 		}		
 	}
 	
 	private void saveMap( String mapDest, String mapName )
 	{
 		try 
 		{
 			FileOutputStream file		= new FileOutputStream( mapDest );
 			BufferedOutputStream buf	= new BufferedOutputStream( file );
 			
 			try 
 			{
 				ObjectOutputStream write	= new ObjectOutputStream( buf );
 				
 				write.writeObject( mapName );
 				write.writeObject( this.mapArea.getMap() );
 
 				write.close();
 			} catch( IOException e )
 			{
 				// TODO Auto-generated catch block
 			}
 		} catch( FileNotFoundException e )
 		{
 			// TODO Auto-generated catch block
 		}
 	}
 	
 	private void fillRows()
 	{
 		this.mapRows		= null;
 		int cntMap			= 0;
 		String[][] rows		= null;
 		String pathMaps		= this.dSettings.getPath();		
 		
 		if( !pathMaps.equals( "" ) )
 		{
 			File dir				= new File( pathMaps );
 			
 			if( dir.exists() && dir.isDirectory() )
 			{
 				String[] files	= dir.list();
 				int amount		= files.length;
 				rows			= new String[amount][];
 				
 				for( int count = 0; count < amount; count++ )
 				{
 					int strLen		= files[count].length() - this.extMap.length();
 					int strPos		= files[count].lastIndexOf( this.extMap );
 					
 					if( strLen == strPos )
 					{
 						String fDest			= pathMaps + "/" + files[count];
 						
 						try 
 						{
 							FileInputStream file	= new FileInputStream( fDest );
 							BufferedInputStream buf	= new BufferedInputStream( file );
 							
 							try 
 							{
 								ObjectInputStream read	= new ObjectInputStream( buf );
 								
 								try 
 								{
 									String 	mapName			= (String) read.readObject();
 									
 									rows[cntMap]			= new String[3];
 									rows[cntMap][0]			= mapName;
 									rows[cntMap][1]			= "kA";
 									rows[cntMap][2]			= "kA";
 									
 									cntMap++;
 									
 									read.close();
 								} catch( ClassNotFoundException e ) 
 								{
 									read.close();
 								}
 							} catch( IOException e )
 							{
 							}
 						} catch( FileNotFoundException e )
 						{
 						}
 					}
 				}
 			}
 		} 
 		
 		if( rows != null )
 		{
 			this.mapRows		= new String[cntMap][];
 			
 			for( int cntRow	= 0; cntRow < cntMap; cntRow++ )
 			{
 				this.mapRows[cntRow]	= new String[3];
 				this.mapRows[cntRow][0]	= rows[cntRow][0];
 				this.mapRows[cntRow][1]	= rows[cntRow][1];
 				this.mapRows[cntRow][2]	= rows[cntRow][2];
 			}
 		} else
 		{
 			this.mapRows		= new String[1][];
 			this.mapRows[0]		= new String[3];
 			
 			this.mapRows[0][0]	= "a"; 
 			this.mapRows[0][1]	= "a";
 			this.mapRows[0][2]	= "a";
 		}
 		
 		for( int i=0; i< this.mapRows.length; i++ )
 		{
 			this.mapModel.addRow( this.mapRows[i] );
 		}
 	}
 	
 	private void resetTable()
 	{
 		this.mapTable.removeAll();		
 	}
 }
