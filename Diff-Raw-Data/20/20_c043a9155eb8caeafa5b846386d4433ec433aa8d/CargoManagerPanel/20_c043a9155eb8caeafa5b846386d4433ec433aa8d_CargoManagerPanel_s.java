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
 import java.util.ArrayList;
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
     private JButton              loadBtn;
     private JButton              unloadBtn;
     private JTextArea            display;
     private JTextField           input;
     private final CargoInventory inventory;
     private ContainerLabel       currentContainer;
    // private Integer				 maxCargoStacks = 5;
 
     public CargoManagerPanel() throws IllegalArgumentException, CargoException,
             LabelException {
         initialiseComponents();
         inventory = new CargoInventory(5, 5, 20);
         /*
          * = new ContainerLabel(0,1,1,1); currentContainer = new
          * ContainerLabel(1,1,1,1);
          */
         inventory.loadContainer(new ContainerLabel(0, 1, 1, 1));
         inventory.loadContainer(new ContainerLabel(1,1,1,1));
         inventory.loadContainer(new ContainerLabel(2,1,1,1));
         inventory.loadContainer(new ContainerLabel(0,1,3,1));
         // inventory.loadContainer(new ContainerLabel(4,1,1,1));
         // reDraw();
         drawTopView();
     }
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
 	 */
 
 	// @Override
 	public void actionPerformed(ActionEvent event) {
 		Object source = event.getSource();
 
 		if ((currentContainer = getContainerFromInput()) != null) {
 			try {
 				System.out.println("loading container "
 						+ currentContainer.toString());
 				if (source == loadBtn) {
 					inventory.loadContainer(currentContainer);
 				} else if (source == unloadBtn) {
 					inventory.unloadContainer(currentContainer);
 				}
 				// reDraw();
 			} catch (CargoException e) {
 				message(e.getMessage());
 			}
 		} else {
 			input.setText("");
 			message("Invalid shipping container label");
 		}
 	}
 
 	private void message(String msg) {
 		JOptionPane.showMessageDialog(this, msg);
 	}
 
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
 
 	/*
 	 * This is where we will actually draw all our GUI elements
 	 */
 	private void initialiseComponents() {
 		GridBagLayout layout = new GridBagLayout();
 		setLayout(layout);
 
 		initialiseTextDisplay();
 		initialiseButtons();
 	}
 
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
 
 		repaint();
 	}
 
 	/**
 	 * 
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
 		addToPanel(display, constraints, 0, 0, 4, 1);
 
 		// input field
 		input = new JTextField();
 		input.addActionListener(this);
 		constraints.weighty = 1;
 		constraints.fill = GridBagConstraints.HORIZONTAL;
 		constraints.anchor = GridBagConstraints.SOUTH;
 		addToPanel(input, constraints, 0, 1, 4, 1);
 
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
 
     ArrayList<String> drawContainers(ContainerLabel[] containerLabels, int kind) {
         // ------------ //12 Dashes
         // | 01200432 | //
         // ------------
         // i.toString()
         // display.getColumns()
         // display.getRows()
         // display.insert(string, int pos)
         // display.setColumns(int)
         // display.append(string)
         // int asciiBoxWidth = 12;
         /*
          * for (ContainerLabel containerLabel : labels) { if (containerLabel !=
          * null) { display.append("------------"); display.append("\n| ");
          * display.append(containerLabel.toString()); display.append(" |\n");
          * display.append("------------"); //display.insert("8", 11); //
          * display.append(containerLabel.toString());
          * //display.append("\n-----------\n"); } }
          */
 
         ArrayList<String> stack = new ArrayList<String>();
 
         if (containerLabels.length > 0) {
             stack.add("----------");
 
             for (ContainerLabel label : containerLabels) {
                 if (label != null) {
                     stack.add(" " + label.toString() + " ");
                     stack.add("----------");
                 }
             }
         } else {
             stack.add("          ");
         }
 
         return stack;
 
     }
 
     void reDraw() {
         ContainerLabel[] localContainers;
         int kind = 0;
         int previousStackCount = 0;
         int maxLineCount = 0;
         ArrayList<String> viewLines = new ArrayList<String>();
         viewLines.add("");
         while (true) {
             try {
                 localContainers = inventory.toArray(kind);
                 ArrayList<String> stackStrings = drawContainers(
                         localContainers, kind);
 
                 if ((localContainers.length > 0) && (previousStackCount > 0)) {
                     ArrayList<String> divider = new ArrayList<String>();
                     divider.add("-");
                     for (int i = 0; i < Math.max(previousStackCount,
                             localContainers.length); i++) {
                         divider.add("|");
                         divider.add("-");
                     }
                     mergeArrayLists(viewLines, divider);
                 } else {
                     ArrayList<String> emptyDivider = new ArrayList<String>();
                     emptyDivider.add(" ");
                     mergeArrayLists(viewLines, emptyDivider);
                 }
 
                 if (localContainers.length > 0) {
                     mergeArrayLists(viewLines, stackStrings);
                 } else {
                     ArrayList<String> emptyLine = new ArrayList<String>();
                     emptyLine.add("          ");
                     mergeArrayLists(viewLines, emptyLine);
                 }
 
                 previousStackCount = localContainers.length;
 
                 if (numberOfRows(localContainers.length) > maxLineCount) {
                     maxLineCount = numberOfRows(localContainers.length);
                 }
 
                 kind++;
                 System.out.println("found a container");
             } catch (CargoException e) {
                 // we reached the end of the stacks
 
                 if (previousStackCount > 0) {
                     ArrayList<String> divider = new ArrayList<String>();
                     divider.add("-\n");
                     for (int i = 0; i < previousStackCount; i++) {
                         divider.add("|\n");
                         divider.add("-\n");
                     }
                     mergeArrayLists(viewLines, divider);
                 } else {
                     ArrayList<String> emptyDivider = new ArrayList<String>();
                     emptyDivider.add(" \n");
                     mergeArrayLists(viewLines, emptyDivider);
                 }
 
                 for (String line : viewLines) {
                     display.append(line);
                 }
 
                 System.out.println("ran out of containers bailing");
                 return;
             }
 
         }
         // "---------
 
     }
 
     int numberOfRows(int containers) {
         if (containers != 0) {
             return 0;
         } else {
             return 2 + containers * 3;
         }
     }
 
     ArrayList<String> mergeArrayLists(ArrayList<String> originalList,
             ArrayList<String> newList) {
         ArrayList<String> returnArrayList = new ArrayList<String>();
         int height = Math.max(originalList.size(), newList.size());
         int originalListWidth = originalList.get(0).length();
         int newListWidth = newList.get(0).length();
 
         for (int i = 0; i < height; i++) {
             String originalString;
             String newString;
 
             if (originalList.size() > (height - 1)) {
                 char[] spaces = new char[originalListWidth];
 
                 for (int j = 0; j < originalListWidth; j++) {
                     spaces[j] = ' ';
                 }
 
                 originalString = new String(spaces);
             } else {
                 originalString = originalList.get(i);
             }
 
             if (newList.size() > height - 1) {
                 char[] spaces = new char[newListWidth];
 
                 for (int j = 0; j < newListWidth; j++) {
                     spaces[j] = ' ';
                 }
 
                 newString = new String(spaces);
             } else {
                 newString = originalList.get(i);
             }
 
             returnArrayList.add(originalString + newString);
         }
 
         return returnArrayList;
     }
 
     void drawTopView() {
         ArrayList<ArrayList<String>> drawArray = new ArrayList<ArrayList<String>>();
         int kind = 0;
         ContainerLabel[] localStack;
         // current thinking for this view
         // An array list which will write the containerlabel(top one) to the
         // first index, and a string which will represent an int to be the count
 
         try {
             while (true) {
                 localStack = inventory.toArray(kind);
                 if (localStack.length > 0) {
                 	if (localStack[0] != null) {
                 	//need to catch empty stacks
                 	ArrayList<String> localArrayListDump = new ArrayList<String>();
                     int objectCount = 0;
                     while (localStack[objectCount] != null)
                     {
                     	objectCount++;
                     }
                     localArrayListDump.add(localStack[objectCount-1].toString());
                 	localArrayListDump.add(Integer.toString(objectCount));
                     drawArray.add(localArrayListDump);
                     
                 	} else
                 	{
                 		ArrayList<String> localArrayListDump = new ArrayList<String>();
                         localArrayListDump.add("        "); 
                         localArrayListDump.add("0");
                         drawArray.add(localArrayListDump);
                 	}
                 	
                 	kind++;
                 }
             }
         } catch (CargoException e) {
         	//catches when we run out of stacks
             drawTopViewHelper(drawArray);
         } catch (Exception e) {
             message("Drawing error: " + e);
         }
         
         
         
     }
     
 	void drawTopViewHelper(ArrayList<ArrayList<String>> drawArray) {
 		for (ArrayList<String> arrayList2 : drawArray) {
 			display.append("-----------");
 		}
 		display.append("\n");
 		for (ArrayList<String> arrayList : drawArray) {
 			display.append("| ");
 			display.append("  "+arrayList.get(0) + "  ");
 		}
 		display.append("\n");
 		for (ArrayList<String> arrayList2 : drawArray) {
 			display.append("-----------");
 		}
 		display.append("\n");
		for (ArrayList<String> arrayList : drawArray) {
 
 			display.append("| ");
 			display.append("     " + arrayList.get(1) + "      ");
 		}
 	}
 
 }
