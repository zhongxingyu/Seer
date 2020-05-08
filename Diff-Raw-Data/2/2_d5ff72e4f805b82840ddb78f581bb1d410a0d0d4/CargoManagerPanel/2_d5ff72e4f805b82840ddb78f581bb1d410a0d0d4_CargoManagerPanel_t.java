 
 /**
  * 
  */
 package solution.gui;
 
 import java.awt.Component;
 import java.awt.Font;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.swing.BorderFactory;
 import javax.swing.JButton;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 
 import solution.CargoException;
 import solution.CargoInventory;
 import solution.ContainerLabel;
 import solution.LabelException;
 
 /**
  * @author Bodaniel Jeanes and Jacob Evans
  */
 public class CargoManagerPanel extends JPanel implements ActionListener {
     private JButton        loadBtn, unloadBtn, createBtn, topViewBtn,
             sideViewBtn;
     private JTextArea      display;
     private JTextField     input, numStacksInput, maxHeightInput,
             maxContainersInput;
     private CargoInventory inventory;
     private ContainerLabel currentContainer;
     private CargoViewer    viewer;
 
     public CargoManagerPanel() throws IllegalArgumentException, CargoException,
             LabelException {
         initialiseComponents();
 
         createInventory(6, 6, 20);
 
         inventory.loadContainer(new ContainerLabel(0, 1, 1, 1));
         inventory.loadContainer(new ContainerLabel(1, 1, 1, 1));
         inventory.loadContainer(new ContainerLabel(2, 1, 1, 1));
         inventory.loadContainer(new ContainerLabel(3, 1, 1, 1));
         inventory.loadContainer(new ContainerLabel(4, 1, 1, 1));
         inventory.loadContainer(new ContainerLabel(4, 1, 2, 1));
         inventory.loadContainer(new ContainerLabel(4, 1, 3, 1));
         inventory.loadContainer(new ContainerLabel(4, 1, 4, 1));
         inventory.loadContainer(new ContainerLabel(4, 1, 5, 1));
//        inventory.loadContainer(new ContainerLabel(5, 1, 5, 1));
 
 
         viewer.draw();
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
      */
 
     // @Override
     public void actionPerformed(ActionEvent event) {
         Object source = event.getSource();
 
         if ((source == loadBtn) || (source == unloadBtn)) {
             if ((currentContainer = getContainerFromInput()) != null) {
                 try {
                     if (source == loadBtn) {
                         System.out.println("loading container "
                                 + currentContainer.toString());
                         inventory.loadContainer(currentContainer);
                     } else if (source == unloadBtn) {
                         System.out.println("unloading container "
                                 + currentContainer.toString());
                         inventory.unloadContainer(currentContainer);
                     }
 
                     viewer.draw();
                 } catch (CargoException e) {
                     message(e.getMessage());
                 }
             } else {
                 input.setText("");
                 message("Invalid shipping container label");
             }
         } else {
             if (source == createBtn) {
                 createInventory();
             } else if (source == topViewBtn) {
                 viewer.setType(CargoViewer.TYPE.TOP);
                 viewer.draw();
             } else if (source == sideViewBtn) {
                 viewer.setType(CargoViewer.TYPE.SIDE);
                 viewer.draw();
             }
         }
     }
 
     private void createInventory() {
         try {
             Integer numStacks = Integer.parseInt(numStacksInput.getText()
                     .trim());
             Integer maxHeight = Integer.parseInt(maxHeightInput.getText()
                     .trim());
             Integer maxContainers = Integer.parseInt(maxContainersInput
                     .getText().trim());
             createInventory(numStacks, maxHeight, maxContainers);
         } catch (Exception e) {
             message("Could not create inventory from given input: "
                     + e.getMessage());
         }
     }
 
     private void createInventory(Integer numStacks, Integer maxHeight,
             Integer maxContainers) throws IllegalArgumentException,
             CargoException {
         inventory = new CargoInventory(numStacks, maxHeight, maxContainers);
         viewer = new CargoViewer(inventory, display);
         viewer.draw();
     }
 
     private void message(String msg) {
         JOptionPane.showMessageDialog(this, msg);
     }
 
     /**
      * Get and parse our input to add a container
      * 
      * @return
      */
     private ContainerLabel getContainerFromInput() {
         Pattern pattern = Pattern.compile("\\d{8}");
         Matcher matcher = pattern.matcher(input.getText());
         if (matcher.matches()) {
             try {
                 Integer kind = Integer.parseInt(input.getText(0, 3));
                 Integer identifier = Integer.parseInt(input.getText(3, 5));
                 Integer kindLength = kind.toString().length();
                 Integer identifierLength = identifier.toString().length();
                 return new ContainerLabel(kind, kindLength, identifier,
                         identifierLength);
             } catch (Exception e) {
                 return null;
             } finally {
                 input.setText("");
             }
         }
         return null;
     }
 
     /**
      * This is where we will actually draw all our GUI elements
      */
     private void initialiseComponents() {
         GridBagLayout layout = new GridBagLayout();
         setLayout(layout);
 
         initialiseTextDisplay();
         initialiseButtons();
     }
 
     //
     /**
      * 
      */
     private void initialiseButtons() {
         GridBagConstraints constraints = new GridBagConstraints();
         loadBtn = new JButton("Load");
         loadBtn.addActionListener(this);
         loadBtn.setVisible(true);
         addToPanel(loadBtn, constraints, 0, 3, 1, 1);
 
         unloadBtn = new JButton("Unload");
         unloadBtn.addActionListener(this);
         unloadBtn.setVisible(true);
         addToPanel(unloadBtn, constraints, 1, 3, 1, 1);
 
         createBtn = new JButton("Create");
         createBtn.addActionListener(this);
         createBtn.setVisible(true);
         addToPanel(createBtn, constraints, 3, 5, 1, 1);
 
         topViewBtn = new JButton("Top View");
         topViewBtn.addActionListener(this);
         topViewBtn.setVisible(true);
         addToPanel(topViewBtn, constraints, 3, 8, 1, 1);
 
         sideViewBtn = new JButton("Side View");
         sideViewBtn.addActionListener(this);
         sideViewBtn.setVisible(true);
         constraints.anchor = GridBagConstraints.EAST;
         addToPanel(sideViewBtn, constraints, 3, 7, 1, 1);
 
         repaint();
     }
 
     /**
      * Initialise our textDisplay area
      */
     private void initialiseTextDisplay() {
         GridBagConstraints constraints = new GridBagConstraints();
         constraints.anchor = GridBagConstraints.CENTER;
         constraints.weightx = 100;
         constraints.weighty = 100;
         constraints.fill = GridBagConstraints.BOTH;
 
         // Text Area and Scroll Pane
         display = new JTextArea(" ", 500, 500);
         display.setEditable(false);
         display.setLineWrap(false);
         display.setFont(new Font("Courier New", Font.PLAIN, 12));
         display.setBorder(BorderFactory.createEtchedBorder());
         addToPanel(display, constraints, 0, 1, 4, 1);
 
         // input field
         input = new JTextField();
         input.addActionListener(this);
         constraints.weighty = 1;
         constraints.fill = GridBagConstraints.HORIZONTAL;
         constraints.anchor = GridBagConstraints.SOUTH;
         addToPanel(input, constraints, 0, 2, 4, 1);
 
         constraints = new GridBagConstraints();
 
         // numStacks input field
         numStacksInput = new JTextField();
         numStacksInput.addActionListener(this);
         constraints.fill = GridBagConstraints.HORIZONTAL;
         constraints.anchor = GridBagConstraints.LAST_LINE_START;
         numStacksInput.setText("Stack Count");
         addToPanel(numStacksInput, constraints, 0, 5, 1, 1);
 
         maxHeightInput = new JTextField();
         maxHeightInput.addActionListener(this);
         constraints.weightx = 1;
         constraints.fill = GridBagConstraints.HORIZONTAL;
         constraints.anchor = GridBagConstraints.LAST_LINE_START;
         maxHeightInput.setText("Max Height");
         addToPanel(maxHeightInput, constraints, 1, 5, 1, 1);
 
         maxContainersInput = new JTextField();
         maxContainersInput.addActionListener(this);
         constraints.fill = GridBagConstraints.HORIZONTAL;
         constraints.anchor = GridBagConstraints.LAST_LINE_START;
         maxContainersInput.setText("Max Containers");
         addToPanel(maxContainersInput, constraints, 2, 5, 1, 1);
 
         repaint();
     }
 
     /**
      * A convenience method to add a component to given grid bag layout
      * locations. Code due to Cay Horstmann
      * 
      * @author Cay Horstmann (via INB370)
      * @param c
      *            the component to add
      * @param constraints
      *            the grid bag constraints to use
      * @param x
      *            the x grid position
      * @param y
      *            the y grid position
      * @param w
      *            the grid width
      * @param h
      *            the grid height
      */
     private void addToPanel(Component c, GridBagConstraints constraints, int x,
             int y, int w, int h) {
         constraints.gridx = x;
         constraints.gridy = y;
         constraints.gridwidth = w;
         constraints.gridheight = h;
         add(c, constraints);
     }
 }
