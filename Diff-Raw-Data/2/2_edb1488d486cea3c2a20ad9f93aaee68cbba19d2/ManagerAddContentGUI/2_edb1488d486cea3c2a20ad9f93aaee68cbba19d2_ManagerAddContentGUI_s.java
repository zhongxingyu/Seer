 package mediastore;
 
 import java.awt.*;
 import java.awt.event.*;
 import java.io.IOException;
 import java.sql.SQLException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.*;
 import javax.swing.border.EmptyBorder;
 
 public class ManagerAddContentGUI extends JFrame implements ItemListener, ActionListener {
 
     final int PADDING = 16;
     JPanel mainPanel;
     JPanel topPanel;
     JPanel moviePanel;
     JPanel albumPanel;
     JPanel audiobookPanel;
     String MOVIE = "Movie";
     String ALBUM = "Music Album";
     String AUDIOBOOK = "Audiobook";
     String[] mediaTypeChoices = { MOVIE, ALBUM, AUDIOBOOK };
     //
     JLabel mediaTypeLabel;
     JComboBox mediaTypeComboBox;
     //
     JLabel movieDirectorLabel;
     JTextField movieDirectorTextField;
     JLabel movieTitleLabel;
     JTextField movieTitleTextField;
     JLabel movieDurationLabel;
     JTextField movieDurationTextField;
     JLabel movieGenreLabel;
     JTextField movieGenreTextField;
     JLabel movieRatingLabel;
     JTextField movieRatingTextField;
     JLabel movieTotalReviewsLabel;
     JTextField movieTotalReviewsTextField;
     JLabel moviePriceLabel;
     JTextField moviePriceTextField;
     JLabel movieReleaseYearLabel;
     JTextField movieReleaseYearTextField;
     //
     JLabel albumAuthorLabel;
     JTextField albumAuthorTextField;
     JLabel albumTitleLabel;
     JTextField albumTitleTextField;
     JLabel albumDurationLabel;
     JTextField albumDurationTextField;
     JLabel albumGenreLabel;
     JTextField albumGenreTextField;
     JLabel albumRatingLabel;
     JTextField albumRatingTextField;
     JLabel albumTotalReviewsLabel;
     JTextField albumTotalReviewsTextField;
     JLabel albumPriceLabel;
     JTextField albumPriceTextField;
     //
     JLabel audiobookAuthorLabel;
     JTextField audiobookAuthorTextField;
     JLabel audiobookTitleLabel;
     JTextField audiobookTitleTextField;
     JLabel audiobookDurationLabel;
     JTextField audiobookDurationTextField;
     JLabel audiobookGenreLabel;
     JTextField audiobookGenreTextField;
     JLabel audiobookRatingLabel;
     JTextField audiobookRatingTextField;
     JLabel audiobookTotalReviewsLabel;
     JTextField audiobookTotalReviewsTextField;
     JLabel audiobookPriceLabel;
     JTextField audiobookPriceTextField;
     //
     JButton okButton;
     JButton cancelButton;
     //
     Database db;
 
     public ManagerAddContentGUI() {
         db = MediaStoreGUI.db;
 
         addWindowListener( new ManagerAddContentGUIExitHandler() );
 
         setLayout( new BorderLayout() );
         mainPanel = new JPanel();
         mainPanel.setLayout( new CardLayout() );
 
         topPanel = new JPanel();
         mediaTypeLabel = new JLabel( "Media type: " );
         topPanel.add( mediaTypeLabel );
 
         mediaTypeComboBox = new JComboBox( mediaTypeChoices );
         mediaTypeComboBox.setSelectedIndex( 0 );
         mediaTypeComboBox.addItemListener( this );
         topPanel.add( mediaTypeComboBox );
 
         add( topPanel, BorderLayout.NORTH );
 
         //<editor-fold defaultstate="collapsed" desc="set up the movie GUI">
         moviePanel = new JPanel();
         moviePanel.setLayout( new GridLayout( 16, 0 ) );
         moviePanel.setBorder( new EmptyBorder( new Insets( PADDING, PADDING, PADDING, PADDING ) ) );
 
         movieDirectorLabel = new JLabel( "Director: " );
         movieDirectorTextField = new JTextField( 15 );
         movieTitleLabel = new JLabel( "Title: " );
         movieTitleTextField = new JTextField( 15 );
         movieDurationLabel = new JLabel( "Duration (in minutes): " );
         movieDurationTextField = new JTextField( 15 );
         movieGenreLabel = new JLabel( "Genre: " );
         movieGenreTextField = new JTextField( 15 );
         movieRatingLabel = new JLabel( "Rating: " );
         movieRatingTextField = new JTextField( 15 );
         movieTotalReviewsLabel = new JLabel( "Total reviews: " );
         movieTotalReviewsTextField = new JTextField( 15 );
         moviePriceLabel = new JLabel( "Price (in USD): " );
         moviePriceTextField = new JTextField( 15 );
         movieReleaseYearLabel = new JLabel( "Release year: " );
         movieReleaseYearTextField = new JTextField( 15 );
 
         moviePanel.add( movieDirectorLabel );
         moviePanel.add( movieDirectorTextField );
         moviePanel.add( movieTitleLabel );
         moviePanel.add( movieTitleTextField );
         moviePanel.add( movieDurationLabel );
         moviePanel.add( movieDurationTextField );
         moviePanel.add( movieGenreLabel );
         moviePanel.add( movieGenreTextField );
         moviePanel.add( movieRatingLabel );
         moviePanel.add( movieRatingTextField );
         moviePanel.add( movieTotalReviewsLabel );
         moviePanel.add( movieTotalReviewsTextField );
         moviePanel.add( moviePriceLabel );
         moviePanel.add( moviePriceTextField );
         moviePanel.add( movieReleaseYearLabel );
         moviePanel.add( movieReleaseYearTextField );
 
 
         //</editor-fold>
 
         //<editor-fold defaultstate="collapsed" desc="set up the album GUI">
 
         albumPanel = new JPanel();
         albumPanel.setLayout( new GridLayout( 14, 0 ) );
         albumPanel.setBorder( new EmptyBorder( new Insets( PADDING, PADDING, PADDING, PADDING ) ) );
 
         albumAuthorLabel = new JLabel( "Artist: " );
         albumAuthorTextField = new JTextField( 15 );
         albumTitleLabel = new JLabel( "Title: " );
         albumTitleTextField = new JTextField( 15 );
         albumDurationLabel = new JLabel( "Duration (in minutes): " );
         albumDurationTextField = new JTextField( 15 );
         albumGenreLabel = new JLabel( "Genre: " );
         albumGenreTextField = new JTextField( 15 );
         albumRatingLabel = new JLabel( "Rating: " );
         albumRatingTextField = new JTextField( 15 );
         albumTotalReviewsLabel = new JLabel( "Total reviews: " );
         albumTotalReviewsTextField = new JTextField( 15 );
         albumPriceLabel = new JLabel( "Price (in USD): " );
         albumPriceTextField = new JTextField( 15 );
 
         albumPanel.add( albumAuthorLabel );
         albumPanel.add( albumAuthorTextField );
         albumPanel.add( albumTitleLabel );
         albumPanel.add( albumTitleTextField );
         albumPanel.add( albumDurationLabel );
         albumPanel.add( albumDurationTextField );
         albumPanel.add( albumGenreLabel );
         albumPanel.add( albumGenreTextField );
         albumPanel.add( albumRatingLabel );
         albumPanel.add( albumRatingTextField );
         albumPanel.add( albumTotalReviewsLabel );
         albumPanel.add( albumTotalReviewsTextField );
         albumPanel.add( albumPriceLabel );
         albumPanel.add( albumPriceTextField );
 
         //</editor-fold>
 
         //<editor-fold defaultstate="collapsed" desc="set up the audiobook GUI">
 
         audiobookPanel = new JPanel();
         audiobookPanel.setLayout( new GridLayout( 14, 0 ) );
         audiobookPanel.setBorder( new EmptyBorder( new Insets( PADDING, PADDING, PADDING, PADDING ) ) );
 
         audiobookAuthorLabel = new JLabel( "Author: " );
         audiobookAuthorTextField = new JTextField( 15 );
         audiobookTitleLabel = new JLabel( "Title: " );
         audiobookTitleTextField = new JTextField( 15 );
         audiobookDurationLabel = new JLabel( "Duration (in minutes): " );
         audiobookDurationTextField = new JTextField( 15 );
         audiobookGenreLabel = new JLabel( "Genre: " );
         audiobookGenreTextField = new JTextField( 15 );
         audiobookRatingLabel = new JLabel( "Rating: " );
         audiobookRatingTextField = new JTextField( 15 );
         audiobookTotalReviewsLabel = new JLabel( "Total reviews: " );
         audiobookTotalReviewsTextField = new JTextField( 15 );
         audiobookPriceLabel = new JLabel( "Price (in USD): " );
         audiobookPriceTextField = new JTextField( 15 );
 
         audiobookPanel.add( audiobookAuthorLabel );
         audiobookPanel.add( audiobookAuthorTextField );
         audiobookPanel.add( audiobookTitleLabel );
         audiobookPanel.add( audiobookTitleTextField );
         audiobookPanel.add( audiobookDurationLabel );
         audiobookPanel.add( audiobookDurationTextField );
         audiobookPanel.add( audiobookGenreLabel );
         audiobookPanel.add( audiobookGenreTextField );
         audiobookPanel.add( audiobookRatingLabel );
         audiobookPanel.add( audiobookRatingTextField );
         audiobookPanel.add( audiobookTotalReviewsLabel );
         audiobookPanel.add( audiobookTotalReviewsTextField );
         audiobookPanel.add( audiobookPriceLabel );
         audiobookPanel.add( audiobookPriceTextField );
 
         //</editor-fold>
 
         mainPanel.add( moviePanel, MOVIE );
         mainPanel.add( albumPanel, ALBUM );
         mainPanel.add( audiobookPanel, AUDIOBOOK );
 
         add( mainPanel, BorderLayout.CENTER );
 
         okButton = new JButton( "OK" );
         cancelButton = new JButton( "Cancel" );
 
         okButton.addActionListener( this );
         cancelButton.addActionListener( this );
 
         JPanel buttonPanel = new JPanel();
         buttonPanel.add( okButton );
         buttonPanel.add( cancelButton );
 
         add( buttonPanel, BorderLayout.SOUTH );
 
 
     }
 
     @Override
     public void itemStateChanged( ItemEvent e ) {
         if ( e.getSource() == mediaTypeComboBox ) {
             CardLayout cl = (CardLayout) ( mainPanel.getLayout() );
             cl.show( mainPanel, (String) e.getItem() );
         }
     }
 
     @Override
     public void actionPerformed( ActionEvent e ) {
         if ( e.getSource() == cancelButton ) {
             MediaStoreGUI.managerScreen();
         }
         if ( e.getSource() == okButton ) {
             Media newItem = null;
             try {
                 if ( mediaTypeComboBox.getSelectedItem().equals( MOVIE ) ) {
                     newItem = new Movie( 0, movieDirectorTextField.getText(), movieTitleTextField.getText(), Integer.parseInt( movieDurationTextField.getText() ),
                             movieGenreTextField.getText(), Integer.parseInt( movieRatingTextField.getText() ), Integer.parseInt( movieTotalReviewsTextField.getText() ), Double.parseDouble( moviePriceTextField.getText() ),
                            Integer.parseInt( movieReleaseYearTextField.getText() ), 0 );
                 }
                 if ( mediaTypeComboBox.getSelectedItem().equals( ALBUM ) ) {
                     newItem = new Album( 0, albumAuthorTextField.getText(), albumTitleTextField.getText(), Integer.parseInt( albumDurationTextField.getText() ),
                             albumGenreTextField.getText(), Integer.parseInt( albumRatingTextField.getText() ), Integer.parseInt( albumTotalReviewsTextField.getText() ), Double.parseDouble( albumPriceTextField.getText() ), 0 );
                 }
                 if ( mediaTypeComboBox.getSelectedItem().equals( AUDIOBOOK ) ) {
                     newItem = new Audiobook( 0, audiobookAuthorTextField.getText(), audiobookTitleTextField.getText(), Integer.parseInt( audiobookDurationTextField.getText() ),
                             audiobookGenreTextField.getText(), Integer.parseInt( audiobookRatingTextField.getText() ), Integer.parseInt( audiobookTotalReviewsTextField.getText() ), Double.parseDouble( audiobookPriceTextField.getText() ), 0 );
                 }
             } catch ( NumberFormatException ex ) {
                 JOptionPane.showMessageDialog( null, "One or more fields contain invalid input, please correct this and try again.", "", JOptionPane.ERROR_MESSAGE );
             }
             db.media.add( newItem );
             try {
                 db.writeNewMediaItem( newItem );
             } catch ( IOException ex ) {
                 Logger.getLogger( ManagerAddContentGUI.class.getName() ).log( Level.SEVERE, null, ex );
             } catch ( SQLException ex ) {
                 Logger.getLogger( ManagerAddContentGUI.class.getName() ).log( Level.SEVERE, null, ex );
             }
 
             MediaStoreGUI.reloadDB();
             MediaStoreGUI.managerScreen();
             JOptionPane.showMessageDialog( null, "Added new item successfully." );
         }
     }
 
     private class ManagerAddContentGUIExitHandler extends WindowAdapter {
 
         @Override
         public void windowClosing( WindowEvent e ) {
             MediaStoreGUI.managerScreen();
         }
     }
 }
