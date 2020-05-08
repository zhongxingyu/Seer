 package lightbeam.editor;
 
 import java.awt.Dimension;
 import java.awt.Rectangle;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 
 import javax.swing.JButton;
 import javax.swing.JFileChooser;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 
 import core.tilestate.TileArray;
 
 public class SettingsDialog 
 {
 	private JOptionPane pane			= null;
 	private JPanel panel				= new JPanel();
 	private JLabel lblPath				= new JLabel( "Speicherort:" );
 	private JTextField inpPath			= new JTextField();
 	private JButton btnPath				= new JButton( "..." );
 	private JFileChooser fc				= new JFileChooser();
 	
 	private String setPath				= "";
 	private final String setFile		= "settings.cnf";
 	
 	public SettingsDialog()	
 	{
 		this.loadSettings();
 		this.inpPath.setText( this.setPath ); 
 		
 		this.panel.setLayout( null );
		this.panel.setPreferredSize( new Dimension( 400, 200 ) );
 		
 		this.lblPath.setBounds( new Rectangle( 10, 10, 70, 20 ) );
 		this.inpPath.setBounds( new Rectangle( 95, 10, 270, 20 ) );
 		this.btnPath.setBounds( new Rectangle( 370, 10, 20, 20 ) );
 		
 		this.panel.add( this.lblPath );
 		this.panel.add( this.inpPath );
 		this.panel.add( this.btnPath );
 		
 		this.fc.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY );
         
 		this.pane 		= new JOptionPane( this.panel, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION );
         
 		this.btnPath.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent e) {
 				SettingsDialog.this.fc.showOpenDialog( SettingsDialog.this.pane );
 				
 				String path	= SettingsDialog.this.fc.getSelectedFile() + "";
 
 				SettingsDialog.this.inpPath.setText( ( !path.equals( "null"  ) )? path : "" );
 			}
 		});
 
 	}
 	
 	public void showDialog()
 	{
 		this.pane.createDialog( "Einstellungen" ).setVisible( true );
 		int selOption	= ( (Integer)this.pane.getValue() ).intValue();
 		
 		if( selOption == JOptionPane.OK_OPTION )
 		{
 			String path		= this.inpPath.getText();
 			File stat 		= new File( path );
 			
 			if( stat.exists() && stat.isDirectory() )
 			{
 				this.setPath	= path;
 				
 				this.saveSettings();
 			} else if( stat.exists() == false )
 			{
 				// ToDo: Fragen, ob Verzeichnis erstellt werden soll, da noch nicht existiert!
 				this.setPath	= path;
 				
 				stat.mkdir();
 				this.saveSettings();
 			} else
 			{
 				// ToDo: Fehlermeldung, das angegebenes Ziel zwar existiert, aber kein Ordner ist!
 			}
 		}
 	}
 	
 	public String getPath() { return this.setPath; }
 	
 	private void saveSettings()
 	{
 		File f	= new File( this.setFile );
 		
 		if( !f.exists() )
 		{
 			try { f.createNewFile(); }
 			catch (IOException e) 
 			{
 				// ToDo: Meldung, das Fehler beim erstellen der Datei!
 				e.printStackTrace();
 			}
 		} else if( f.exists() && !f.isFile() )
 		{
 			return;
 			//ToDo: Fehlermeldung, das angegebenes Ziel zwar existiert, aber keine Datei ist!
 		}
 		
 		FileOutputStream file;
 		try 
 		{
 			file = new FileOutputStream( this.setFile );
 			BufferedOutputStream buf	= new BufferedOutputStream( file );
 			ObjectOutputStream write;
 			
 			try 
 			{
 				write = new ObjectOutputStream( buf );
 				write.writeObject( this.setPath );
 
 				write.close();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	private void loadSettings()
 	{
 		File f	= new File( this.setFile );
 		
 		if( f.exists() && f.isFile() )
 		{
 			FileInputStream file;
 			try 
 			{
 				file = new FileInputStream( this.setFile );
 				BufferedInputStream buf	= new BufferedInputStream( file );
 				ObjectInputStream read;
 				try 
 				{
 					read 					= new ObjectInputStream( buf );
 					
 					try 
 					{
 						this.setPath		= (String) read.readObject();
 					} catch (ClassNotFoundException e) 
 					{
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 					
 					read.close();
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			} catch (FileNotFoundException e) 
 			{
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		} else if( f.exists() && !f.isFile() )
 		{
 			// ToDo: Fehlermeldung, dass Ziel zwar existiert, aber keine Datei ist!
 		}
 	}
 }
