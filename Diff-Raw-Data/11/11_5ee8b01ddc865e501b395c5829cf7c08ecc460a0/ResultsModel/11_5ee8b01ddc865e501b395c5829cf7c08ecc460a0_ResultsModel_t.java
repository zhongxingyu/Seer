 import java.util.ArrayList;
 import java.util.concurrent.ConcurrentHashMap;
 
 import java.util.regex.Pattern;
 import java.util.regex.Matcher;
 
 import java.net.URL;
 import java.net.URLEncoder;
 
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 
 import org.apache.commons.lang3.StringEscapeUtils;
 
 import java.awt.Component;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.Desktop;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.awt.event.MouseEvent;
 
 import javax.swing.JLabel;
 import javax.swing.JComboBox;
 import javax.swing.JTable;
 import javax.swing.JOptionPane;
 import javax.swing.ToolTipManager;
 import javax.swing.table.DefaultTableModel;
 import javax.swing.table.DefaultTableCellRenderer;
 
 public class ResultsModel extends DefaultTableModel
 {
     public static final int SHARES_COL  = 0;
     public static final int NEW_TAG_COL = 1;
     public static final int TAGS_COL    = 2;
     public static final int DOMAIN_COL  = 3;
     public static final int LINK_COL    = 4;
 
     private static final ColumnContext[] columnArray =
     {
         new ColumnContext( "Shares",  Rating.class,    false ),
         new ColumnContext( "New Tag", String.class,    true ),
         new ColumnContext( "Tags",    Tags.class,      false ),
         new ColumnContext( "Site",    String.class,    false ),
         new ColumnContext( "Link",    Hyperlink.class, false )
     };
 
     private LinkProcessor linkProcessor = null;
     private JLabel fetchStatusLabel     = null;
     private Tags allTags                = null;
     private TagsPanel tagsPanel         = null;
     private JComboBox newTagInput       = null;
 
     private String dbFile = null;
 
     private static final long serialVersionUID = 101;
 
     public ResultsModel(
         JLabel fetchStatusLabel, String dbFile )
     {
         this.fetchStatusLabel = fetchStatusLabel;
         this.dbFile    = dbFile;
         linkProcessor = new LinkProcessor( this, dbFile );
         allTags = linkProcessor.dbGetAllTags();
         linkProcessor.start();
 
         ToolTipManager.sharedInstance().setDismissDelay( Integer.MAX_VALUE );
     }
 
     public void setTagsPanel( TagsPanel tagsPanel )
     {
         this.tagsPanel = tagsPanel;
     }
 
     public String fetchTitle( String url )
     {
         String value = null;
         try
         {
             String dataUrl =
                 "http://query.yahooapis.com/v1/public/yql?q=" +
                 "select%20*%20from%20html%20where%20url%3D%22" + 
                 URLEncoder.encode( url, "UTF-8" ) + 
                 "%22%20and%20xpath%3D'%2F%2Ftitle'&format=xml";
             String content = Stome.getHTMLContent( dataUrl, 3000, null );
             if( content != null )
             {
                 String regex = "<title>([^<]+)</title>";
 
                 Pattern pattern = Pattern.compile( regex, Pattern.CASE_INSENSITIVE );
                 Matcher matcher = pattern.matcher( content );
                 if( matcher.find() )
                     value = matcher.group( 1 );
             }
         }
         catch( java.io.UnsupportedEncodingException ex ) {}
         return value;
     }
 
     public void addNewTagInput( JComboBox newTagInput )
     {
         this.newTagInput = newTagInput;
     }
 
     public Tags getAllTags()
     {
         return allTags;
     }
 
     public ArrayList<TagCount> getTagCounts( Tags selectedTags )
     {
         return linkProcessor.dbGetTagCounts( selectedTags );
     }
 
     public Integer getTagCount( String tagName, Tags selectedTags )
     {
         return linkProcessor.dbGetTagCount( tagName, selectedTags );
     }
 
     public ArrayList<String> getTagLinks( Tags tags )
     {
         return linkProcessor.dbGetTagLinks( tags );
     }
 
     public void updateTitle( String url, String title )
     {
         if( title.equals( "" ) )
             title = null;
 
         String linkKey = linkKey( url );
         linkProcessor.dbUpdateTitle( linkKey, title );
         for( int i = 0; i < getRowCount(); i++ )
         {
             Hyperlink rh = (Hyperlink) getValueAt( i, ResultsModel.LINK_COL );
             if( linkKey.equals( rh.getLinkKey() ) )
             {
                 rh.setTitle( title );
                 setValueAt( rh, i, ResultsModel.LINK_COL );
                 break;
             }
         }
     }
 
     public void deleteLinkTag( String tagName, int rowIndex )
     {
         Tags tags = (Tags) getValueAt( rowIndex, ResultsModel.TAGS_COL );
         if( tags.contains( tagName ) )
         {
             // Update database
             Hyperlink link = 
                 (Hyperlink) getValueAt( rowIndex, ResultsModel.LINK_COL );
             String linkKey = link.getLinkKey();
             linkProcessor.dbDeleteLinkTag( linkKey, tagName );
 
             // Update GUI
             tags.remove( tagName );
             setValueAt( tags, rowIndex, ResultsModel.TAGS_COL );
         }
     }
 
     public void addLinkTag( String tagName, int rowIndex )
     {
         tagName = tagName.trim().toLowerCase().replaceAll( "\\s+", "-" );
         if( ! tagName.equals( "" ) )
         {
             if( ! allTags.contains( tagName ) )
             {
                 int reply = JOptionPane.showConfirmDialog( null, 
                     "Do you want to create new tag \"" + tagName + "\"?",
                     "Create new tag", JOptionPane.YES_NO_OPTION );
                 if( reply == JOptionPane.YES_OPTION )
                     allTags.add( tagName );
                 else
                     return;
             }
 
             Tags tags = (Tags) getValueAt( rowIndex, ResultsModel.TAGS_COL );
             if( ! tags.contains( tagName ) )
             {
                 // Update database
                 Hyperlink link = 
                     (Hyperlink) getValueAt( rowIndex, ResultsModel.LINK_COL );
                 String linkKey = link.getLinkKey();
                 linkProcessor.dbAddLink( linkKey, link.getTitle() );
                 linkProcessor.dbAddLinkTag( linkKey, tagName );
 
                 // Update GUI
                 tags.add( tagName );
                 setValueAt( tags, rowIndex, ResultsModel.TAGS_COL );
             }
         }
     }
 
     @Override public void setValueAt( Object value, int rowIndex, int columnIndex )
     {
         if( columnIndex == ResultsModel.NEW_TAG_COL )
             addLinkTag( (String) value, rowIndex );
         else
             super.setValueAt( value, rowIndex, columnIndex );
     }
 
     public void clearLinks()
     {
         // Restart link processor
 
         linkProcessor.interrupt();

        // Wait up to 5 seconds for threads to stop
        int waits = 0;
        while( linkProcessor.isAlive() && waits++ < 25 )
         {
             try { Thread.sleep( 200 ); } catch( InterruptedException ex ) {}
         }
 
         linkProcessor = new LinkProcessor( this, dbFile );
         linkProcessor.start();
 
         setFetchStatus( "" );
         setRowCount( 0 );
 
         Stome.buttonsSetEnabled( true );
     }
 
     public void stopFetch()
     {
         linkProcessor.interrupt();

        // Wait up to 5 seconds for threads to stop
        int waits = 0;
        while( linkProcessor.isAlive() && waits++ < 25 )
         {
             try { Thread.sleep( 200 ); } catch( InterruptedException ex ) {}
         }
 
         ConcurrentHashMap<String,String> allUrls = linkProcessor.allUrls;
         linkProcessor = new LinkProcessor( this, dbFile );
 
         // Remove rows where share count was not fetched
         for( int i = getRowCount() - 1; i >= 0; i-- )
         {
             Rating rating = (Rating) getValueAt( i, ResultsModel.SHARES_COL );
             String linkKey = rating.getLinkKey();
             if( rating.getShareCount() == -1 )
             {
                 removeRow( i );
                 allUrls.remove( linkKey );
             }
         }
 
         linkProcessor.allUrls = allUrls;
         linkProcessor.completedCount = getRowCount();
         linkProcessor.setFetchStatus( 0, 0 );
         linkProcessor.start();
 
         Stome.buttonsSetEnabled( true );
     }
 
     public void setFetchStatus( String status )
     {
         fetchStatusLabel.setText( status );
     }
 
     public void startUrls()
     {
         linkProcessor.setUrlsLoadedState( false );
     }
 
     public void stopUrls()
     {
         linkProcessor.setUrlsLoadedState( true );
     }
 
     // Generate SHA1 hashing key
 
     private static String linkKey( String url )
     {
         MessageDigest md = null;
         try { md = MessageDigest.getInstance( "SHA-1" ); }
         catch( NoSuchAlgorithmException e ) { e.printStackTrace(); } 
         return byteArrayToHexString( md.digest( url.getBytes() ) );
     }
 
     private static String byteArrayToHexString( byte[] b )
     {
         String result = "";
         for( int i = 0; i < b.length; i++ )
         {
             result += 
                 Integer.toString( ( b[ i ] & 0xff ) + 0x100, 16 ).substring( 1 );
         }
         return result;
     }
 
     public boolean addUrl( String url )
     {
         if( url != null )
             return linkProcessor.addLinkKey( linkKey( url ), url );
         return false;
     }
 
     @Override public boolean isCellEditable( int row, int col )
     {
         return columnArray[ col ].isEditable;
     }
 
     @Override public Class<?> getColumnClass( int modelIndex )
     {
         return columnArray[ modelIndex ].columnClass;
     }
 
     @Override public int getColumnCount()
     {
         return columnArray.length;
     }
 
     @Override public String getColumnName( int modelIndex )
     {
         return columnArray[ modelIndex ].columnName;
     }
 
     private static class ColumnContext
     {
         public final String  columnName;
         public final Class<?>   columnClass;
         public final boolean isEditable;
         public ColumnContext( String columnName, Class<?> columnClass,
                               boolean isEditable )
         {
             this.columnName  = columnName;
             this.columnClass = columnClass;
             this.isEditable  = isEditable;
         }
     }
 }
 
 class HyperlinkRenderer extends DefaultTableCellRenderer 
                   implements MouseListener, MouseMotionListener
 {
     private static final long serialVersionUID = 104;
 
     private int row = -1;
     private int col = -1;
     private boolean isRollover = false;
     JLabel hoverUrlLabel = null;
 
     public HyperlinkRenderer( JLabel hoverUrlLabel )
     {
         super();
         this.hoverUrlLabel = hoverUrlLabel;
     }
 
     @Override public Component getTableCellRendererComponent(
         JTable table, Object value, 
         boolean isSelected, boolean hasFocus,
         int row, int column )
     {
         JLabel c = (JLabel) super.getTableCellRendererComponent(
             table, value, isSelected, false, row, column);
 
         Hyperlink link = (Hyperlink) value;
 
         String url = link.getURL() != null ? link.getURL().toString() : "";
         String title = url;
         if( link.getTitle() != null )
         {
             title = link.getTitle();
         }
 
         if( ! table.isEditing() && this.row == row && 
             this.col == column && this.isRollover )
         {
             setText( "<html><u><font color='blue'>" + title + "</font></u></html>" );
             hoverUrlLabel.setText( url );
         }
         else if( hasFocus )
         {
             setText( "<html><font color='blue'>" + title + "</font></u></html>" );
             hoverUrlLabel.setText( url );
         }
         else
         {
             setText( title );
         }
 
         ArrayList<String> matchList = new ArrayList<String>();
         Pattern regex = Pattern.compile( ".{1,100}(?:\\s|$)", Pattern.DOTALL );
         Matcher regexMatcher = regex.matcher( title );
         while( regexMatcher.find() )
             matchList.add( regexMatcher.group() );
 
         String htmlTitle = "<html>";
         for( String s : matchList )
             htmlTitle += s + "<br/>";
         htmlTitle += "</html>";
 
         c.setToolTipText( htmlTitle );
 
         return this;
     }
 
     private static boolean isHyperlinkColumn( JTable table, int column )
     {
         return column >= 0 && 
                table.getColumnClass( column ).equals( Hyperlink.class );
     }
 
     @Override public void mouseMoved( MouseEvent e )
     {
         JTable table = ( JTable ) e.getSource();
         Point pt = e.getPoint();
         int prev_row = row;
         int prev_col = col;
         boolean prev_ro = isRollover;
         row = table.rowAtPoint( pt );
         col = table.columnAtPoint( pt );
         isRollover = isHyperlinkColumn( table, col );
 
         Rectangle repaintRect;
         if( isRollover )
         {
             Rectangle r = table.getCellRect( row, col, false );
             repaintRect = prev_ro ? r.union( 
                 table.getCellRect( prev_row, prev_col, false ) ) : r;
         }
         else
         {
             repaintRect = table.getCellRect( prev_row, prev_col, false );
             hoverUrlLabel.setText( "" );
         }
         table.repaint( repaintRect );
     }
 
     @Override public void mouseExited( MouseEvent e )
     {
         JTable table = ( JTable ) e.getSource();
         if( isHyperlinkColumn( table, col ) )
         {
             table.repaint( table.getCellRect( row, col, false ) );
             row = -1;
             col = -1;
             isRollover = false;
             hoverUrlLabel.setText( "" );
         }
     }
 
     @Override public void mouseClicked( MouseEvent e )
     {
         JTable table = ( JTable ) e.getSource();
         Point pt = e.getPoint();
         int ccol = table.columnAtPoint( pt );
         if( isHyperlinkColumn( table, ccol ) )
         {
             int crow = table.rowAtPoint( pt );
             if( crow == -1 )
                 return;
             Hyperlink link = ( Hyperlink ) table.getValueAt( crow, ccol );
             URL url = link.getURL();
 //            System.out.println( url );
             try
             {
                 // JDK 1.6.0
                 if( Desktop.isDesktopSupported() )
                 {
                     Desktop.getDesktop().browse( url.toURI() );
                 }
             }
             catch( Exception ex )
             {
               ex.printStackTrace();
             }
         }
     }
 
     @Override public void mouseDragged( MouseEvent e ) {}
     @Override public void mouseEntered( MouseEvent e ) {}
     @Override public void mousePressed( MouseEvent e ) {}
     @Override public void mouseReleased( MouseEvent e ) {}
 }
 
 class Hyperlink implements Comparable<Hyperlink>
 {
     private String linkKey;
     private String title;
     private URL url;
 
     public Hyperlink( String linkKey, String title, URL url )
     {
         this.linkKey = linkKey;
         setTitle( title );
         this.url     = url;
     }
 
     public void setLinkKey( String linkKey )
     {
         this.linkKey = linkKey;
     }
 
     public void setTitle( String title )
     {
         this.title = StringEscapeUtils.unescapeHtml4( title );
     }
 
     public void setURL( URL url )
     {
         this.url = url;
     }
 
     public String getLinkKey()
     {
         return linkKey;
     }
 
     public String getTitle()
     {
         return title;
     }
 
     public URL getURL()
     {
         return url;
     }
 
     public int compareTo( Hyperlink o )
     {
         if( this != null && getTitle() != null &&
             o    != null && o.getTitle() != null )
         {
             return getTitle().compareTo( o.getTitle() );
         }
         return 0;
     }
 }
 
 class Rating implements Comparable<Rating>
 {
     private String linkKey;
     private int rating;
     private int shareCount;
 
     public Rating( String linkKey, int rating, int shareCount )
     {
         this.linkKey    = linkKey;
         this.rating     = rating;
         this.shareCount = shareCount;
     }
 
     public void setRating( int rating )
     {
         this.rating = rating;
     }
 
     public void setShareCount( int shareCount )
     {
         this.shareCount = shareCount;
     }
 
     public String getLinkKey()
     {
         return linkKey;
     }
 
     public int getRating()
     {
         return rating;
     }
 
     public int getShareCount()
     {
         return shareCount;
     }
 
     public int compareTo( Rating o )
     {
         if( getShareCount() > o.getShareCount() )
             return 1;
         else if( getShareCount() < o.getShareCount() )
             return -1;
         return 0;
     }
 
     public String toString()
     {
         if( shareCount == -1 )
             return "";
         return
             Integer.toString( shareCount );
     }
 }
