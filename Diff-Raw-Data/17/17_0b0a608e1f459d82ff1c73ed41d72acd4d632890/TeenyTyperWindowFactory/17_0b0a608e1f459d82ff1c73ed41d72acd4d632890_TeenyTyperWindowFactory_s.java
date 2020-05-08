 /**
  * TeenyTyper by Lindsay Bradford is licensed under a 
  * Creative Commons Attribution-ShareAlike 3.0 Unported License.
  *
  * Year: 2012 
  *
  */
 
 package blacksmyth.teenytyper;
 
 import java.awt.BorderLayout;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Container;
 import java.awt.FocusTraversalPolicy;
 import java.awt.Font;
 import java.awt.Toolkit;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.util.Hashtable;
 
 import javax.swing.AbstractAction;
 import javax.swing.AbstractButton;
 import javax.swing.Box;
 import javax.swing.ButtonGroup;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JToggleButton;
 import javax.swing.KeyStroke;
 import javax.swing.WindowConstants;
 import javax.swing.border.MatteBorder;
 
 import blacksmyth.general.swing.ActionBinder;
 import blacksmyth.general.swing.ColourIcon;
 
 /**
  * A factory class that produces a fully-formed, ready to run TeenyTyper JFrame.
  * The creation of a new frame is thread-safe in that all instance state for 
  * frame construction is placed on the stack.
  */
 
 public class TeenyTyperWindowFactory {
 
   private static final long serialVersionUID = 1L;
   
   private static Font EDITOR_FONT = new Font("Serif", Font.BOLD, 42);
   
   private static int ICON_PIXELS = 55;
   
   private static Color TEXT_COLOURS[] = {Color.RED, Color.GREEN, Color.BLUE};
   
   private static Color DEFAULT_TEXT_COLOUR = TEXT_COLOURS[0]; 
   
   public static JFrame createJFrame() {
     
     InteractiveComponents components = new InteractiveComponents(new JFrame());
     
     components.window.setUndecorated(true);
     
     components.window.setDefaultCloseOperation(
         WindowConstants.DO_NOTHING_ON_CLOSE
     );
     
     createRootPaneContent(components);
     
     components.window.setBounds(
         0, 0,    // x,y coords
         Toolkit.getDefaultToolkit().getScreenSize().width, 
         Toolkit.getDefaultToolkit().getScreenSize().height
     );
 
     components.window.setFocusTraversalPolicy(
         new EditorFocusTraversalPolicy(components.editor)    
     );
 
     return components.window;
   }
   
   private static void createRootPaneContent(InteractiveComponents components) {
 
     JPanel mainPanel = new JPanel(new BorderLayout(5,5));    
     mainPanel.setBackground(Color.DARK_GRAY);
 
 
     mainPanel.add(
         Box.createHorizontalStrut(5),
         BorderLayout.EAST
     );
 
     mainPanel.add(
         createScrollableEditorPane(components), 
         BorderLayout.CENTER
     );
     
     mainPanel.add(
         Box.createHorizontalStrut(5),
         BorderLayout.WEST
     );
     
     mainPanel.add(
         createMenuButtonPanel(components), 
         BorderLayout.PAGE_START
     );
     
     mainPanel.add(
         Box.createVerticalStrut(5),
         BorderLayout.PAGE_END
     );
    
     components.window.getContentPane().add(
         mainPanel, 
         BorderLayout.CENTER
     );
     
     createEventHandlers(components);
   }
   
   private static JPanel createMenuButtonPanel(InteractiveComponents components) {
     JPanel buttonPanel = new JPanel();
     buttonPanel.setBackground(Color.DARK_GRAY);
 
     final int HORIZONTAL_STRUT_WIDTH = 34;
     
     final JLabel editorLabel = new JLabel("TeenyTyper");
     editorLabel.setFont(EDITOR_FONT);
     editorLabel.setForeground(Color.WHITE);
     
     buttonPanel.add(
         editorLabel
     );
 
     buttonPanel.add(
         Box.createHorizontalStrut(HORIZONTAL_STRUT_WIDTH)
     );
     
     buttonPanel.add(
         createClearButton(components)
     );
     
     buttonPanel.add(
         Box.createHorizontalStrut(HORIZONTAL_STRUT_WIDTH)
     );
     
     createTextColourButtons(buttonPanel, components);
     
     return buttonPanel;
   }
 
   private static void setColourButtonSizeAndBorder(AbstractButton theButton) {
     theButton.setRequestFocusEnabled(false);
     theButton.setSize(ICON_PIXELS, ICON_PIXELS);
     theButton.setBorder(
         new MatteBorder(5, 5, 5, 5, Color.LIGHT_GRAY)
     );
   }
   
   private static JButton createClearButton(InteractiveComponents components) {
      JButton clearButton = new JButton();
 
      setColourButtonSizeAndBorder(clearButton);
      
      clearButton .setIcon(
          new ColourIcon(
              ICON_PIXELS, 
              Color.WHITE
          )
      );
 
      clearButton .setSelectedIcon(
          new ColourIcon(
              ICON_PIXELS, 
              Color.LIGHT_GRAY
          )
      );
 
      components.clearButton = clearButton;
      
     return clearButton;
   }
   
   private static void createTextColourButtons(
                           JPanel buttonPanel, 
                           InteractiveComponents components) {
 
     ButtonGroup colourButtonGroup = new ButtonGroup();
     
     for (Color textColour : TEXT_COLOURS) {
       
       JToggleButton colourButton = createColourButton(
           textColour, 
           components
       );  
       
       buttonPanel.add(colourButton);
 
       // Ensure only one colour button is toggled active at any one time by
       // adding the button to colourButtonGroup.
 
       colourButtonGroup.add(colourButton);
       
       if (textColour == DEFAULT_TEXT_COLOUR) {
         colourButtonGroup.setSelected(
             colourButton.getModel(), 
             true
         );
       }
       
     }
   }
  
   private static JToggleButton createColourButton(
                                   Color colour, 
                                   InteractiveComponents components) {
     
     JToggleButton theButton = new JToggleButton();
 
     setColourButtonSizeAndBorder(theButton);
     
     theButton.setIcon(
         new ColourIcon(
             ICON_PIXELS, 
             colour.darker().darker()
         )
     );
 
     theButton.setSelectedIcon(
         new ColourIcon(
             ICON_PIXELS, 
             colour
         )
     );
     
     components.colorButtons.put(colour, theButton);
 
     return theButton;
   }
   
   private static JScrollPane createScrollableEditorPane(InteractiveComponents components) {
     TeenyTyperEditorPane editor = new TeenyTyperEditorPane(EDITOR_FONT, TEXT_COLOURS, DEFAULT_TEXT_COLOUR);
 
     components.editor = editor;
     
     return new JScrollPane(editor);
   }
 
   private static void createEventHandlers(final InteractiveComponents components) {
 
     ActionBinder.bindKeyStrokeToAction(
         components.editor, 
         KeyStroke.getKeyStroke(KeyEvent.VK_D, KeyEvent.CTRL_DOWN_MASK), // <CTRL-D>
         new AbstractAction() {
           private static final long serialVersionUID = 1L;
 
           public void actionPerformed(ActionEvent arg0) {
             System.exit(0);
           }
         }
     );
     
     components.clearButton.addActionListener(
         new ActionListener() {
           public void actionPerformed(ActionEvent arg0) {
             components.editor.setText("");
             components.editor.requestFocusInWindow();
           }
         }
     );
     
     for (Color textColour : TEXT_COLOURS) {
       
       JToggleButton colourButton = components.colorButtons.get(textColour);
       
       colourButton.addActionListener(
           new ColourActionListener(components.editor, textColour) 
       );
 
     }
   }
   
 }
 
 /**
  * An actionListener that we can bind to colour buttons to trigger changes in the 
  * foreground colour style currently set for the given TeenyTyperEditorPane.
 * @author linds
 *
  */
 class ColourActionListener implements ActionListener {
   private Color colour;
   private TeenyTyperEditorPane editor;
   
   public ColourActionListener(TeenyTyperEditorPane editor, Color colour) {
     this.editor = editor;
     this.colour = colour;
   }
 
   public void actionPerformed(ActionEvent arg0) {
     editor.setTextColour(colour);
     editor.requestFocusInWindow();
   }
 }
 
 /** 
  * A simple container (modelled loosely off .NET structures) to keep references
  * to all interactive components as the factory creates, and then wires together
  * components via various event handlers.
  *
  */
 class InteractiveComponents {
   public JFrame window;
   public TeenyTyperEditorPane editor;
   public JButton clearButton;
   public Hashtable<Color, JToggleButton> colorButtons;
   
   public InteractiveComponents(JFrame window) {
     this.colorButtons = new Hashtable<Color, JToggleButton>();
     this.window = window;
   }
 }
 
 /**
  * A restrictive editor focus traversal policy where all attempts
  * at focus traversal bring the user back to the TeenyTyperEditorPane.
  */
 
 class EditorFocusTraversalPolicy extends FocusTraversalPolicy {
   private TeenyTyperEditorPane editor;
 
   public EditorFocusTraversalPolicy(TeenyTyperEditorPane editor) {
     this.editor = editor;
   }
 
   @Override
   public Component getComponentAfter(Container arg0, Component arg1) {
     return editor;
   }
 
   @Override
   public Component getComponentBefore(Container arg0, Component arg1) {
     return editor;
   }
 
   @Override
   public Component getDefaultComponent(Container arg0) {
     return editor;
   }
 
   @Override
   public Component getFirstComponent(Container arg0) {
     return editor;
   }
 
   @Override
   public Component getLastComponent(Container arg0) {
     return editor;
   }
 
 }
