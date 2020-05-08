 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package tilt;
 
 import java.awt.BorderLayout;
 import java.net.URL;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JToolBar;
 import javax.swing.SwingConstants;
 import javax.swing.JPanel;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import tilt.link.Links;
 import java.awt.image.BufferedImage;
 import java.awt.Dimension;
 /**
  *
  * @author desmond
  */
 public class TiltGui extends JPanel implements ActionListener
 {
     ImagePanel image;
     TextPanel text;
     JButton rectButton, regionButton,bAndWButton,wordButton,linesButton,
         lineButton,pageButton;
     JToolBar topToolBar;
     /**
      * Create a TILT GUI instance 
      * @param plainText the plain text to link to
      * @param bi the image on the LHS
      */
     public TiltGui( String plainText, BufferedImage bi ) throws Exception
     {
         setLayout( new BorderLayout() );
         topToolBar = new JToolBar("File Actions");
         topToolBar.add(makeButton("Save24","SAVE","Save to disk or server","SAVE"));
         topToolBar.add(makeButton("Properties24","PERFS","Set Preferences","PREFS"));
         topToolBar.add(makeButton("Open24","OPEN","Open text file","OPEN"));
         topToolBar.add(makeButton("Import24","IMAGE","Open image file","IMAGE"));
         topToolBar.addSeparator();
         rectButton = makeButton("Rectangle24","RECTANGLE","Rectangle","RECTANGLE");
         topToolBar.add(rectButton);
         regionButton = makeButton("Region24","REGION","Region","REGION");
         topToolBar.add(regionButton);
         wordButton = makeButton("WordSelect24","RECOGNISE_WORD","Word select","WORD");
         topToolBar.add(wordButton);
         lineButton = makeButton("LineSelect24","RECOGNISE_LINE","Line select","LINE");
         topToolBar.add(lineButton);
         pageButton = makeButton("Page24","PAGE","Recognise page","PAGE");
         topToolBar.add(pageButton);
         bAndWButton = makeButton("BlackWhite24","B&W","Switch to black and white","B&W");
         topToolBar.add(makeButton("Link24","LINK","Link selected region to selected text","LINK"));
         topToolBar.add(bAndWButton);
         linesButton = makeButton("AlignJustify24","LINES","Show lines","LINES");
         topToolBar.add(linesButton);
         regionButton.setSelected(true);
         add( topToolBar, BorderLayout.NORTH );
         Links links = new Links();
         image = new ImagePanel( bi, links );
         add(image, BorderLayout.WEST);
         text = new TextPanel( image.width, image.height, plainText, links);
         add(text, BorderLayout.EAST);
         image.setMode( Mode.REGION );
         requestFocus();
     }
     /**
      * Make a single button for the toolbar
      * @param imageName the name of the image in the jar
      * @param actionCommand the string action
      * @param toolTipText the tooltip displayed when mousing over it
      * @param altText alternatieve text if no image
      * @return the button
      */
     protected final JButton makeButton(String imageName, 
         String actionCommand,String toolTipText, String altText) 
     {
         //Look for the image.
         String imgLocation = "/graphics/"+imageName+".gif";
         URL imageURL = TILT.class.getResource(imgLocation);
 
         //Create and initialize the button.
         JButton button = new JButton();
         button.setActionCommand(actionCommand);
         button.setToolTipText(toolTipText);
         button.addActionListener(this);
 
         if (imageURL != null) 
             button.setIcon(new ImageIcon(imageURL, altText));
         else    
         {
             button.setText(altText);
             System.err.println("Resource not found: " + imgLocation);
         }
         return button;
     }
     /**
      * Set the size of the gui
      * @param width the suggested width
      * @param height the suggested height
      */
     @Override
     public void setSize( int width, int height )
     {
        image.setSize( width/2, height );
         Dimension dim = image.getPreferredSize();
         text.setSize( dim.width, dim.height );
     }
     @Override
     public Dimension getPreferredSize()
     {
         Dimension iSize = image.getPreferredSize();
         Dimension tSize = text.getPreferredSize();
         return new Dimension(iSize.width+tSize.width,iSize.height);
     }
     public void actionPerformed(ActionEvent e) 
     {
         String command = e.getActionCommand();
         if ( command != null )
         {
             try
             {
                 Mode mode = Mode.valueOf(command);
                 image.setMode( mode );
                 if ( rectButton.isSelected() )
                     rectButton.setSelected( false );
                 if ( regionButton.isSelected() )
                     regionButton.setSelected( false );
                 if ( wordButton.isSelected() )
                     wordButton.setSelected( false );
                 if ( lineButton.isSelected() )
                     lineButton.setSelected( false );
                 switch ( mode )
                 {
                     case REGION:
                         regionButton.setSelected( true );
                         break;
                     case RECTANGLE:
                         rectButton.setSelected( true );
                         break;
                     case RECOGNISE_WORD:
                         wordButton.setSelected( true );
                         break;
                     case RECOGNISE_LINE:
                         lineButton.setSelected( true );
                         break;
                 }
             }
             catch ( IllegalArgumentException iae )
             {
                 if ( command.equals("B&W") )
                 {
                     linesButton.setSelected( false );
                     bAndWButton.setSelected( !bAndWButton.isSelected() );
                     if ( bAndWButton.isSelected() )
                         image.switchToBW();
                     else
                         image.switchToColor();
                 }
                 else if ( command.equals("LINES") )
                 {
                     bAndWButton.setSelected( false );
                     linesButton.setSelected( !linesButton.isSelected() );
                     if ( linesButton.isSelected() )
                         image.switchToLines();
                     else
                         image.switchToColor();
                 }
                 else if ( command.equals("PAGE") )
                 {
                     image.recognisePage();
                 }
             }
         }
     }
 }
