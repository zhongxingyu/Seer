 package gizmo385.librarymanager.ui;
 
 import gizmo385.datastructures.sll.SingleLinkedList;
 import gizmo385.librarymanager.ItemLibrary;
 import gizmo385.librarymanager.types.Album;
 import gizmo385.librarymanager.types.Book;
 import gizmo385.librarymanager.types.Item;
 import gizmo385.librarymanager.types.Movie;
 import gizmo385.librarymanager.types.VideoGame;
 import gizmo385.util.Logger;
 
 import java.awt.CardLayout;
 import java.awt.Dialog.ModalityType;
 import java.awt.FlowLayout;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 import java.util.Scanner;
 
 import javax.swing.JButton;
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.WindowConstants;
 
 /**
  * GUI Client for the ItemLibrary class
  * <p>Supports displaying library as well as the addition and deletion of new items
  */
 public class LibraryManagerClient
 {
 	private final static String MAINPANEL = "Library Display";
 	private final static String ADDBOOK = "Add Book";
 	private final static String ADDMOVIE = "Add Movie";
 	private final static String ADDGAME = "Add Game";
 	private final static String ADDALBUM = "Add Album";
 	
 	private ItemLibrary library = new ItemLibrary();
 	
 	private Logger actionLog = new Logger( "lmLog.txt" );
 	
 	// Window components
 	private JFrame frame;
 	private JDialog aboutDialog;
 	private JMenuBar jmb;
 	private JMenu file, help, addItem;
 	private JMenuItem save, load, exit, remove, about, book, movie, videoGame, album, display;
 	private JTextArea primaryDisplay;
 	private JScrollPane jsc;
 	private JTextField searchQuery;
 	private JTextField albumArtist, albumBarcodeNumber, albumLabel, albumSongList, albumYearReleased, albumName, albumGenre, albumCopies, albumTags;
 	private JTextField bookAuthor, bookIsbn, bookPublisher, bookYearPublished, bookName, bookGenre, bookCopies, bookTags;
 	private JTextField movieDirector, movieRating, movieStarring, movieYearDirected, movieBarcodeNumber, movieName, movieGenre, movieCopies, movieTags;
 	private JTextField gameBarcodeNumber, gameConsole, gameContentRating, gameDeveloper, gamePublisher, gameName, gameGenre, gameCopies, gameTags;
 	private JButton submitSearch, cancelNewBook, cancelNewMovie, cancelNewVideoGame, cancelNewAlbum,submitBook, submitVideoGame, submitMovie, submitAlbum, displayLib, closeAboutDialog;
 	private JPanel libraryDisplay, addBook, addMovie, addVideoGame, addAlbum, cards;
 	private CardLayout cardLayout = new CardLayout();
 	private ButtonHandler bh = new ButtonHandler();
 	private GridLayout layout = new GridLayout( 0, 2 );
 	
 	
 	/** Sets up the frame and calls methods to instantiate frame components */
 	public LibraryManagerClient()
 	{	
 		this.actionLog.publishToLog( "Initiallizing LibraryManagerClient" );
 		
 		/* Set layout gap */
 		layout.setVgap( 20 );
 		
 		/* Primary Window */
 		setupPrimary();
 		
 		/* Add Book */		
 		setupAddBookLayout();
 		
 		/* Add Movie */
 		setupAddMovieLayout();
 		
 		/* Add Video Game */
 		setupAddVideoGameLayout();
 		
 		/* Add Album */
 		setupAddAlbumLayout();
 		
 		/* About Dialog */
 		setupAboutDialog();
 		
 		/* Frame settings */
 		frame = new JFrame( "Library Manager" );
 		frame.setSize( 530, 450 );
 		frame.setResizable( false );
 		frame.setLocationRelativeTo( null );
 		frame.setVisible( true );
 		frame.setJMenuBar( jmb );
 		frame.setDefaultCloseOperation( WindowConstants.EXIT_ON_CLOSE );
 		
 		/* Set up cardLayout and add the cards to the frame */
 		cards = new JPanel( cardLayout );
 		cards.add( libraryDisplay, MAINPANEL );
 		cards.add( addBook, ADDBOOK );
 		cards.add( addMovie, ADDMOVIE );
 		cards.add( addVideoGame, ADDGAME );
 		cards.add( addAlbum, ADDALBUM );
 		frame.add( cards );
 		
 		/* Show the main panel */
 		cardLayout.show( cards, MAINPANEL );
 		
 		/* Load and display the library */
 		library.loadLibrary();
 		displayLibrary();
 	}
 	
 	/** Handles actions performed within the frame */
 	private class ButtonHandler implements ActionListener
 	{
 		public void actionPerformed( ActionEvent ae )
 		{
 			
 			if( ae.getSource() == book ) // Add a book
 			{
 				actionLog.publishToLog( "Switching display to ADDBOOK" );
 				cardLayout.show( cards, ADDBOOK );
 				frame.setTitle( "Library Manager - Add Book" );
 			}
 			else if( ae.getSource() == movie ) // Add a movie
 			{
 				actionLog.publishToLog( "Switching display to ADDMOVE" );
 				cardLayout.show( cards, ADDMOVIE );
 				frame.setTitle( "Library Manager - Add Movie" );
 			}
 			else if( ae.getSource() == videoGame ) // Add a video game
 			{
 				actionLog.publishToLog( "Switching display to ADDGAME" );
 				cardLayout.show( cards, ADDGAME );
 				frame.setTitle( "Library Manager - Add Video Game" );
 			}
 			else if( ae.getSource() == album ) // Add an album
 			{
 				actionLog.publishToLog( "Switching display to ADDALBUM" );
 				cardLayout.show( cards, ADDALBUM ); 
 				frame.setTitle( "Library Manager - Add Album" );
 			}
 			else if( ae.getSource() == cancelNewMovie || ae.getSource() == cancelNewBook || ae.getSource() == cancelNewVideoGame || ae.getSource() == cancelNewAlbum ) // Main panel
 			{
 				actionLog.publishToLog( "Switching display to MAINPANEL" );
 				cardLayout.show( cards, MAINPANEL );
 				frame.setTitle( "Library Manager" );
 			}
 			else if( ae.getSource() == display || ae.getSource() == displayLib) // Display library
 			{
 				displayLibrary();
 			}
 			else if( ae.getSource() == exit ) // Exit
 			{
 				actionLog.publishToLog( "Closing Library Manager application" );
 				System.exit( 1 );
 			}
 			else if( ae.getSource() == closeAboutDialog ) // close about dialog
 			{
 				changeAboutDialogVisibility( false );
 			}
 			else if( ae.getSource() == searchQuery || ae.getSource() == submitSearch ) // Search library
 			{
 				SingleLinkedList<Item> results = library.search( searchQuery.getText().toLowerCase() );
 				
 				displayResults( results );
 			} // end search
 			else if( ae.getSource() == submitBook ) //Submit a book
 			{
 				actionLog.publishToLog( "Adding new Book" );
 				
 				Scanner tagParser = new Scanner( bookTags.getText() );
 				tagParser.useDelimiter( ";" );
 				SingleLinkedList<String> tags = new SingleLinkedList<String>();
 				while( tagParser.hasNext() )
 					tags.insertInOrder( tagParser.next() );
 				tagParser.close();
 				
 				Item i = new Book( bookName.getText(), Integer.parseInt( bookCopies.getText() ), tags, 
 						bookIsbn.getText(), Integer.parseInt( bookYearPublished.getText() ), 
 						bookPublisher.getText(), bookAuthor.getText(), bookGenre.getText() );
 				
 				library.addItem( i );
 				if( library.saveLibrary() )
 					JOptionPane.showMessageDialog( frame, "Book successfully added to library!" );
 				else
 					JOptionPane.showMessageDialog( frame, "Book unsuccessfully added to library!" );
 				
 				cardLayout.show( cards, MAINPANEL );
 				frame.setTitle( "Library Manager" );
 				displayLibrary();
 				
 				bookName.setText("");
 				bookCopies.setText("");
 				bookIsbn.setText("");
 				bookYearPublished.setText("");
 				bookPublisher.setText("");
 				bookAuthor.setText("");
 				bookGenre.setText("");
 				bookTags.setText("");
 			}
 			else if( ae.getSource() == submitVideoGame ) // Submit a video game
 			{
 				actionLog.publishToLog( "Adding new VideoGame" );
 				
 				Scanner tagParser = new Scanner( gameTags.getText() );
 				tagParser.useDelimiter( ";" );
 				SingleLinkedList<String> tags = new SingleLinkedList<String>();
 				while( tagParser.hasNext() )
 					tags.insertInOrder( tagParser.next() );
 				tagParser.close();
 				
 				Item i = new VideoGame( gameName.getText(), Integer.parseInt( gameCopies.getText() ), 
 						tags, gameDeveloper.getText(), gamePublisher.getText(), gameGenre.getText(), 
 						gameConsole.getText(), gameContentRating.getText(), gameBarcodeNumber.getText() );
 				
 				library.addItem( i );
 				if( library.saveLibrary() )
 					JOptionPane.showMessageDialog( frame, "Video game successfully added to library!" );
 				else
 					JOptionPane.showMessageDialog( frame, "Video game unsuccessfully added to library!" );
 				
 				cardLayout.show( cards, MAINPANEL );
 				frame.setTitle( "Library Manager" );
 				displayLibrary();
 				
 				gameTags.setText("");
 				gameName.setText("");
 				gameCopies.setText("");
 				gameDeveloper.setText("");
 				gamePublisher.setText("");
 				gameGenre.setText("");
 				gameConsole.setText("");
 				gameContentRating.setText("");
 				gameBarcodeNumber.setText("");
 			}
 			else if( ae.getSource() == submitMovie ) // Submit a movie
 			{
 				actionLog.publishToLog( "Adding new Movie" );
 				
 				Scanner tagParser = new Scanner( movieTags.getText() );
 				tagParser.useDelimiter( ";" );
 				SingleLinkedList<String> tags = new SingleLinkedList<String>();
 				while( tagParser.hasNext() )
 					tags.insertInOrder( tagParser.next() );
 				tagParser.close();
 				
 				tagParser = new Scanner( movieStarring.getText() );
 				tagParser.useDelimiter( "," );
 				ArrayList<String> tempStarring = new ArrayList<String>();
 				while( tagParser.hasNext() )
 					tempStarring.add( tagParser.next() );
 				tagParser.close();
 				
 				String[] starring = new String[ tempStarring.size() ];
 				for( int i = 0; i < tempStarring.size(); i++ )
 					starring[i] = tempStarring.get(i);
 				
 				Item i = new Movie( movieName.getText(), Integer.parseInt( movieCopies.getText() ), 
 						tags, movieGenre.getText(), movieDirector.getText(), movieRating.getText(), starring, 
 						Integer.parseInt( movieYearDirected.getText() ), movieBarcodeNumber.getText() );
 				
 				library.addItem(i);
 				if( library.saveLibrary() )
 					JOptionPane.showMessageDialog( frame, "Movie successfully added to library!" );
 				else
 					JOptionPane.showMessageDialog( frame, "Movie unsuccessfully added to library!" );
 				
 				cardLayout.show( cards, MAINPANEL );
 				frame.setTitle( "Library Manager" );
 				displayLibrary();
 				
 				movieName.setText("");
 				movieCopies.setText("");
 				movieStarring.setText("");
 				movieTags.setText("");
 				movieGenre.setText("");
 				movieDirector.setText("");
 				movieRating.setText("");
 				movieYearDirected.setText("");
 				movieBarcodeNumber.setText("");
 			}
 			else if( ae.getSource() == submitAlbum ) // Submit an album
 			{
 				actionLog.publishToLog( "Adding new Album" );
 				
 				Scanner tagParser = new Scanner( albumTags.getText() );
 				tagParser.useDelimiter( ";" );
 				SingleLinkedList<String> tags = new SingleLinkedList<String>();
 				while( tagParser.hasNext() )
 					tags.insertInOrder( tagParser.next() );
 				tagParser.close();
 				
 				tagParser = new Scanner( albumSongList.getText() );
 				tagParser.useDelimiter( ", " );
 				ArrayList<String> tempSongList = new ArrayList<String>();
 				while( tagParser.hasNext() )
 					tempSongList.add( tagParser.next() );
 				tagParser.close();
 				
 				String[] songList = new String[ tempSongList.size() ];
 				for( int i = 0; i < tempSongList.size(); i++ )
 					songList[i] = tempSongList.get(i);
 					
 				Item i = new Album( albumName.getText(), Integer.parseInt( albumCopies.getText() ), tags, albumArtist.getText(),
 						albumGenre.getText(), albumLabel.getText(), songList, Integer.parseInt( albumYearReleased.getText() ), albumBarcodeNumber.getText() );
 				
 				library.addItem( i );
 				if( library.saveLibrary() )
 					JOptionPane.showMessageDialog( frame, "Album successfully added to library!" );
 				else
 					JOptionPane.showMessageDialog( frame, "Album unsuccessfully added to library!" );
 				
 				cardLayout.show( cards, MAINPANEL );
 				frame.setTitle( "Library Manager" );
 				displayLibrary();
 				
 				albumName.setText("");
 				albumCopies.setText("");
 				albumArtist.setText("");
 				albumGenre.setText("");
 				albumLabel.setText("");
 				albumYearReleased.setText("");
 				albumBarcodeNumber.setText("");
 				albumTags.setText("");
 			}
 			else if( ae.getSource() == save ) // save library
 			{
 				if( !library.saveLibrary() )
 					JOptionPane.showMessageDialog( frame, "Error saving library!" );
 			}
 			else if( ae.getSource() == load ) // load library
 			{
 				if( !library.loadLibrary() )
 					JOptionPane.showMessageDialog(frame, "Error loading library!" );
 				else
 					displayLibrary();
 			}
 			else if( ae.getSource() == about ) // about dialog
 			{
 				changeAboutDialogVisibility( true );
 			}
             else if( ae.getSource() == remove ) //remove item
             {
             	actionLog.publishToLog( "Performing Removal" );
             	
                 String nameOfItemToRemove = JOptionPane.showInputDialog( frame, "Enter item name: ", "Item Deletion", JOptionPane.QUESTION_MESSAGE );
 
                 for( Item i : library.getLibrary() )
                 {
                     if( i.getName().equalsIgnoreCase( nameOfItemToRemove ) )
                     {
                         int confirm = JOptionPane.showConfirmDialog( frame, "Delete this item?\n" + i.toString() );
 
                         if( confirm == JOptionPane.YES_OPTION )
                         {
                             library.removeItem( i );
                         }
                         else
                         {
                             JOptionPane.showMessageDialog( frame, "Canceling item deletion!", "Canceled!", JOptionPane.INFORMATION_MESSAGE );
                         }
                     }
                    else
                    {
                        JOptionPane.showMessageDialog( frame, "Item could not be found!", "Not found!", JOptionPane.ERROR_MESSAGE );
                    }
                 }
             } // end remove
 		} //end ActionPerformed()
 	} // end ButtonHandler
 
     /** Sets up components for the about dialog */
 	public void setupAboutDialog()
 	{
 		actionLog.publishToLog( "Setting up AboutDialog..." );
 		
 		closeAboutDialog = new JButton( "Close" );
 		closeAboutDialog.addActionListener( bh );
 		
 		aboutDialog = new JDialog( frame, "About" );
 		aboutDialog.setLayout( new FlowLayout() );
 		aboutDialog.setSize( 350, 120 );
 		aboutDialog.setResizable(false);
 		aboutDialog.setLocationRelativeTo( frame );
 		aboutDialog.setDefaultCloseOperation( JDialog.DISPOSE_ON_CLOSE );
 		aboutDialog.add( new JLabel( "Library Manager - Created by gizmo385" ) );
 		aboutDialog.add( new JLabel( "Source code available on Github repo" ) );
 		aboutDialog.add( new JLabel( "Programmed using the open source Eclipse IDE" ) );
 		aboutDialog.add( closeAboutDialog );
 		aboutDialog.setModalityType( ModalityType.APPLICATION_MODAL );
 		aboutDialog.setVisible( false );
 	}
 
     /** Changes the visibility constant on the aboutDialog */
 	public void changeAboutDialogVisibility( boolean visibility )
     {
 		actionLog.publishToLog( "Changing aboutDialog visibility from " + aboutDialog.isVisible() + " to " + visibility );
 		
         aboutDialog.setVisible( visibility );
     }
 
     /** Sets up components for the main frame */
 	public void setupPrimary()
 	{
 		actionLog.publishToLog( "Setting up primary JPanel..." );
 		
 		/* JMenuBar, JMenus, JMenuItems */
 		save = new JMenuItem( "Save Library" );
 		save.addActionListener( bh );
 		load = new JMenuItem( "Load Library" );
 		load.addActionListener( bh );
         remove = new JMenuItem( "Remove Item");
         remove.addActionListener( bh );
 		display = new JMenuItem( "Display Library" );
 		display.addActionListener( bh );
 		exit = new JMenuItem( "Exit" );
 		exit.addActionListener(bh);
 		about = new JMenuItem( "About" );
 		about.addActionListener( bh );
 		book = new JMenuItem( "Book" );
 		book.addActionListener( bh );
 		videoGame = new JMenuItem( "Video Game" );
 		videoGame.addActionListener( bh );
 		movie = new JMenuItem( "Movie" );
 		movie.addActionListener( bh );
 		album = new JMenuItem( "Album" );
 		album.addActionListener( bh );
 		
 		addItem = new JMenu( "Add item" );
 		addItem.add( album );
 		addItem.add( book );
 		addItem.add( movie );
 		addItem.add( videoGame );
 		
 		file = new JMenu( "File" );
 		file.add( save );
 		file.add( load );
 		file.addSeparator();
         file.add( remove );
 		file.add( display );
 		file.addSeparator();
 		file.add( exit );
 		
 		help = new JMenu( "Help" );
 		help.add( about );
 		
 		jmb = new JMenuBar();
 		jmb.add( file );
 		jmb.add( addItem );
 		jmb.add( help );
 		
 		/* Primary Library Display - Displays library contents & searches */
 		primaryDisplay = new JTextArea( 20, 43);
 		primaryDisplay.setToolTipText("Library contents displayed here");
 		primaryDisplay.setEditable(false);
 		jsc = new JScrollPane(primaryDisplay);
 		
 		searchQuery = new JTextField( 20 );
 		searchQuery.addActionListener( bh );
 		
 		
 		submitSearch = new JButton( "Search" );
 		submitSearch.addActionListener( bh );
 		displayLib = new JButton( "Display Library" );
 		displayLib.addActionListener( bh );
 		
 		libraryDisplay = new JPanel( new FlowLayout() );
 		libraryDisplay.setVisible( false );
 		libraryDisplay.add( searchQuery );
 		libraryDisplay.add( submitSearch );
 		libraryDisplay.add( jsc );
 		libraryDisplay.add( displayLib );
 	}
 	
 	/** Sets up the addBook JPanel */
 	public void setupAddBookLayout()
 	{
 		actionLog.publishToLog( "Setting up addBook..." );
 		
 		bookAuthor = new JTextField( 12 );
 		bookIsbn = new JTextField( 12 );
 		bookPublisher = new JTextField( 12 );
 		bookYearPublished = new JTextField( 12 );
 		bookName = new JTextField( 12 );
 		bookGenre = new JTextField( 12 );
 		bookCopies = new JTextField( 3 );
 		bookTags = new JTextField( 20 );
 		bookTags.setToolTipText( "Separate tags with semicolons" );
 		
 		submitBook = new JButton( "Add Book" );
 		submitBook.addActionListener( bh );
 		cancelNewBook = new JButton( "Return to main screen" );
 		cancelNewBook.addActionListener( bh );
 		
 		addBook = new JPanel( layout );
 		addBook.setVisible( false );
 		addBook.add( new JLabel( "Title: " ) );
 		addBook.add( bookName );
 		addBook.add( new JLabel( "Genre: " ) );
 		addBook.add( bookGenre );
 		addBook.add( new JLabel( "Number of copies: " ) );
 		addBook.add( bookCopies );
 		addBook.add( new JLabel( "Tags: " ) );
 		addBook.add( bookTags );
 		addBook.add( new JLabel( "Author: " ) );
 		addBook.add( bookAuthor );
 		addBook.add( new JLabel( "Publisher" ) );
 		addBook.add( bookPublisher );
 		addBook.add( new JLabel( "ISBN: ") );
 		addBook.add( bookIsbn );
 		addBook.add( new JLabel( "Year Published: " ) );
 		addBook.add( bookYearPublished );
 		addBook.add( submitBook );
 		addBook.add( cancelNewBook );
 	}
 	
 	/** Sets up the addMovie JPanel */
 	public void setupAddMovieLayout()
 	{
 		actionLog.publishToLog( "Setting up addMove..." );
 		
 		movieDirector = new JTextField( 12 );
 		movieRating = new JTextField( 12 );
 		movieStarring = new JTextField( 20 );
 		movieYearDirected = new JTextField( 4 );
 		movieBarcodeNumber = new JTextField( 12 );
 		movieName = new JTextField( 12 );
 		movieGenre = new JTextField( 12 );
 		movieCopies = new JTextField( 3 );
 		movieTags = new JTextField( 20 );
 		movieTags.setToolTipText( "Separate tags with semicolons" );
 		
 		submitMovie = new JButton( "Add Movie" );
 		submitMovie.addActionListener( bh );
 		cancelNewMovie = new JButton( "Return to main screen" );
 		cancelNewMovie.addActionListener( bh );
 		
 		addMovie = new JPanel( layout );
 		addMovie.setVisible( false );
 		addMovie.add( new JLabel( "Title: " ) );
 		addMovie.add( movieName );
 		addMovie.add( new JLabel( "Genre: " ) );
 		addMovie.add( movieGenre );
 		addMovie.add( new JLabel( "Number of copies: " ) );
 		addMovie.add( movieCopies );
 		addMovie.add( new JLabel( "Tags: " ) );
 		addMovie.add( movieTags );
 		addMovie.add( new JLabel( "Director: " ) );
 		addMovie.add( movieDirector );
 		addMovie.add( new JLabel( "Starring: " ) );
 		addMovie.add( movieStarring );
 		addMovie.add( new JLabel( "Rating: " ) );
 		addMovie.add( movieRating );
 		addMovie.add( new JLabel( "Year Directed: " ) );
 		addMovie.add( movieYearDirected );
 		addMovie.add( new JLabel( "Barcode: " ) );
 		addMovie.add( movieBarcodeNumber );
 		addMovie.add( submitMovie );
 		addMovie.add( cancelNewMovie );
 	}
 	
 	/** Sets up the layout of the addVideoGame JPanel */
 	public void setupAddVideoGameLayout()
 	{
 		actionLog.publishToLog( "Setting up addVideoGame..." );
 		
 		gameName = new JTextField( 12 );
 		gameGenre = new JTextField( 12 );
 		gameCopies = new JTextField( 3 );
 		gameTags = new JTextField( 20 );
 		gameTags.setToolTipText( "Separate tags with semicolons" );
 		gameConsole = new JTextField( 12 );
 		gameDeveloper = new JTextField( 12 );
 		gamePublisher = new JTextField( 12 );
 		gameBarcodeNumber = new JTextField( 12 );
 		gameContentRating = new JTextField( 12 );
 		
 		submitVideoGame = new JButton( "Add Video Game" );
 		submitVideoGame.addActionListener( bh );
 		cancelNewVideoGame = new JButton( "Return to main screen" );
 		cancelNewVideoGame.addActionListener( bh );
 		
 		addVideoGame = new JPanel( layout );
 		addVideoGame.setVisible( false );
 		addVideoGame.add( new JLabel( "Title: " ) );
 		addVideoGame.add( gameName );
 		addVideoGame.add( new JLabel( "Genre: " ) );
 		addVideoGame.add( gameGenre );
 		addVideoGame.add( new JLabel( "Number of copies: " ) );
 		addVideoGame.add( gameCopies );
 		addVideoGame.add( new JLabel( "Tags: " ) );
 		addVideoGame.add( gameTags );
 		addVideoGame.add( new JLabel( "Developer: " ) );
 		addVideoGame.add( gameDeveloper );
 		addVideoGame.add( new JLabel( "Publisher: " ) );
 		addVideoGame.add( gamePublisher );
 		addVideoGame.add( new JLabel( "Console: " ) );
 		addVideoGame.add( gameConsole );
 		addVideoGame.add( new JLabel( "Content Rating: " ) );
 		addVideoGame.add( gameContentRating );
 		addVideoGame.add( new JLabel( "Barcode: " ) );
 		addVideoGame.add( gameBarcodeNumber );
 		addVideoGame.add( submitVideoGame );
 		addVideoGame.add( cancelNewVideoGame );
 	}
 	
 	/** Sets up the layout of addAlbum JPanel */
 	public void setupAddAlbumLayout()
 	{
 		actionLog.publishToLog( "Setting up addAlbum..." );
 		
 		albumName = new JTextField( 12 );
 		albumGenre = new JTextField( 12 );
 		albumCopies = new JTextField( 3 );
 		albumTags = new JTextField( 20 );
 		albumTags.setToolTipText("Separate tags with a semicolon" );
 		albumSongList = new JTextField( 20 );
 		albumSongList.setToolTipText( "Separate song titles with commas" );
 		albumArtist = new JTextField( 12 );
 		albumLabel = new JTextField( 12 );
 		albumYearReleased = new JTextField( 4 );
 		albumBarcodeNumber = new JTextField( 12 );
 			
 		submitAlbum = new JButton( "Add Album" );
 		submitAlbum.addActionListener( bh );
 		cancelNewAlbum = new JButton( "Return to main screen" );
 		cancelNewAlbum.addActionListener( bh );
 		
 		addAlbum = new JPanel( layout );
 		addAlbum.setVisible( false );
 		addAlbum.add( new JLabel( "Title: " ) );
 		addAlbum.add( albumName );
 		addAlbum.add( new JLabel( "Genre: " ) );
 		addAlbum.add( albumGenre );
 		addAlbum.add( new JLabel( "Number of copies: " ) );
 		addAlbum.add( albumCopies );
 		addAlbum.add( new JLabel( "Tags: " ) );
 		addAlbum.add( albumTags );
 		addAlbum.add( new JLabel( "Artist: " ) );
 		addAlbum.add( albumArtist );
 		addAlbum.add( new JLabel( "Label: " ) );
 		addAlbum.add( albumLabel );
 		addAlbum.add( new JLabel( "Song list: " ) );
 		addAlbum.add( albumSongList );
 		addAlbum.add( new JLabel( "Year Released: " ) );
 		addAlbum.add( albumYearReleased );
 		addAlbum.add( new JLabel( "Barcode: " ) );
 		addAlbum.add( albumBarcodeNumber );
 		addAlbum.add( submitAlbum );
 		addAlbum.add( cancelNewAlbum );
 	}
 	
 	/** Displays all of the items within the library */
 	public void displayLibrary()
 	{
 		actionLog.publishToLog( "Updating library display" );
 		
 		primaryDisplay.setText("");
 		for( Item i : library.getLibrary() )
 			primaryDisplay.append( i.toString() + "\n\n" );
 		
 		primaryDisplay.setCaretPosition(0);
 		searchQuery.setText("");
 	}
 	
 	/** Displays a set of items within the JTextArea */
 	public void displayResults( SingleLinkedList<Item> results )
 	{
 		actionLog.publishToLog( "Displaying search results" );
 		
 		primaryDisplay.setText("");
 	
 		for( Item i : results )
 			primaryDisplay.append( i.toString() + "\n\n" );
 		
 		primaryDisplay.setCaretPosition(0);
 	}
 	
 	/** Creates an instance of LibraryManagerClient */
 	public static void main( String[] args )
 	{
 		@SuppressWarnings("unused")
 		LibraryManagerClient lmc = new LibraryManagerClient();
 	}
 }
