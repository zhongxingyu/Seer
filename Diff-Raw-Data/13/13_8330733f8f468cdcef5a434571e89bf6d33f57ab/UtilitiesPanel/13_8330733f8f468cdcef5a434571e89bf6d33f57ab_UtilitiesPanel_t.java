 /*
  * Program Name: Recipe Application
 * author: Vairon M. 
  * date: Feb/06/2012 
  * Computer used: HP Compaq 6000 Pro Microtower
  * IDE used: Eclipse IDE
  * purpose: Provide the user with a simple/flexible recipe organizer application 
  */
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.Scanner;
 import javax.swing.*;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import javax.swing.filechooser.FileNameExtensionFilter;
 
 public class UtilitiesPanel extends JPanel
 {
 
 	private static final long serialVersionUID = 1L;
 
 	// Set Font color, size, and attributes.
 	private Color fontColor = new Color(0, 0, 0);
 	private Font currentFont = new Font("Times New Roman", Font.PLAIN, 14);;
 	JFrame styleFrame = new JFrame();
 
 	private JMenuBar barMenu = new JMenuBar();
 	private JMenu fileMenu = new JMenu("File"), formatMenu = new JMenu("Format"), helpMenu = new JMenu("Help");
 
 	private JColorChooser colorChooser = new JColorChooser();
 
 	/*
 	 * Items for all menus
 	 */
 
 	private JMenuItem openMenuItem = new JMenuItem("Open"), closeMenuItem = new JMenuItem("Exit"),
 	        aboutMenuItem = new JMenuItem("About"), saveMenuItem = new JMenuItem("Save"),
 	        addServingValueMenuItem = new JMenuItem("Add Serving Value"),
 	        startNewMenuItem = new JMenuItem("New Recipe"), styleMenuItem = new JMenuItem("Font Color");
 
 	// Input fields
 	// Mastery factor # 1 Arrays
 	private JTextField recipeField, authorField, ingredientField[] = new JTextField[40],
 	        amountField[] = new JTextField[40], unitField[] = new JTextField[40];
 
 	/*
 	 * Drop down list holding multiple values representing the serving size and
 	 * rating values of the recipe
 	 */
 	private JComboBox<Integer> ratingBox = new JComboBox<Integer>(), servingBox = new JComboBox<Integer>();
 
 	/*
 	 * The JEditorPane holds user input, and the JScrollPane allows the user to
 	 * scroll through the user inputed data which is off screen.
 	 */
 	private JEditorPane descriptionEditor = new JEditorPane();
 	private JScrollPane descriptionHolder = new JScrollPane(descriptionEditor);
 
 	/*
 	 * The subPanel holds ingredients information such as name, amount, and unit
 	 * of the ingredient.
 	 */
 	private JPanel subPanel = new JPanel();
 	private JScrollPane ingredientScroll = new JScrollPane(subPanel);
 
 	// Mastery factors # 2 User-defined objects
 	private Recipe recipe = new Recipe();
 
 	public UtilitiesPanel()
 	{
 		setLayout(new GridBagLayout());
 
 		// Constraints for the UtilitiesPanel
 		GridBagConstraints c = new GridBagConstraints();
 		// Mastery factor # 14 Use of additional libraries
 		setBackground(Color.decode("#666699"));
 
 		// Add all menu items to the menus
 		barMenu.add(fileMenu);
 		barMenu.add(formatMenu);
 		barMenu.add(helpMenu);
 		helpMenu.add(aboutMenuItem);
 		formatMenu.add(addServingValueMenuItem);
 		formatMenu.add(styleMenuItem);
 		fileMenu.add(openMenuItem);
 		fileMenu.add(saveMenuItem);
 		fileMenu.add(startNewMenuItem);
 		fileMenu.addSeparator();
 		fileMenu.add(closeMenuItem);
 
 		// Listeners to actions which the user makes
 		styleMenuItem.addActionListener(new formatColor());
 		closeMenuItem.addActionListener(new closeAction());
 		saveMenuItem.addActionListener(new saveAction());
 		openMenuItem.addActionListener(new openAction());
 		aboutMenuItem.addActionListener(new aboutAction());
 		addServingValueMenuItem.addActionListener(new valueAction());
 		startNewMenuItem.addActionListener(new newAction());
 
 		descriptionEditor.setFont(currentFont);
 		descriptionEditor.setForeground(fontColor);
 
 		c.gridx = 0;
 		c.gridy = 0;
 		c.ipady = 20;
 		c.ipadx = 100;
 		c.fill = GridBagConstraints.BOTH;
 
 		add(barMenu, c);
 		c.insets = new Insets(0, 20, 8, 70);
 		c.ipady = 0;
 		c.ipadx = 0;
 
 		/*
 		 * Add the recipe's name, author's name, serving size, rating value, and
 		 * description labels and text fields to the screen
 		 */
 		// Mastery factor # 6 Loops
 		for (int set = 0; set < 40; set++)
 		{
 			c.gridy++;
 
 			if (set == 0)
 			{
 				add(new JLabel("Recipe's Name:"), c);
 				c.ipady = 10;
 				recipeField = new JTextField(20);
 				recipeField.setForeground(fontColor);
 				c.gridx = 1;
 				add(recipeField, c);
 				c.gridx = 0;
 			}
 
 			else if (set == 1)
 			{
 				add(new JLabel("Author's Name:"), c);
 				c.gridx = 1;
 				authorField = new JTextField(20);
 				authorField.setForeground(fontColor);
 				add(authorField, c);
 				c.gridx = 0;
 			}
 
 			else if (set == 2)
 			{
 				add(new JLabel("Serving Size:"), c);
 				c.gridx = 1;
 				for (int items = 0; items < 60; items++)
 					servingBox.addItem(items + 1);
 				add(servingBox, c);
 				servingBox.setForeground(fontColor);
 				c.gridx = 0;
 			}
 
 			else if (set == 3)
 			{
 				add(new JLabel("Rating:"), c);
 				c.gridx = 1;
 				for (int item = 0; item < 5; item++)
 				{
 					ratingBox.addItem(item + 1);
 				}
 				add(ratingBox, c);
 				c.gridx = 0;
 			}
 
 			else if (set == 4)
 			{
 				add(new JLabel("Description:"), c);
 				c.ipady = 170;
 				c.ipadx = 500;
 				c.gridx = 1;
 				add(descriptionHolder, c);
 			}
 
 		}
 
 		c.gridy++;
 		c.gridx = 1;
 
 		// Sub panel which contains the ingredients
 		subPanel.setLayout(new GridBagLayout());
 		subPanel.setMaximumSize(new Dimension(50, 100));
 
 		GridBagConstraints c2 = new GridBagConstraints();
 
 		c2.gridx = 0;
 		c2.gridy++;
 		c2.insets = new Insets(5, 5, 5, 5);
 
 		// Allows the subpanel to be scrolled
 		ingredientScroll.setPreferredSize(new Dimension(50, 100));
 		ingredientScroll.setMaximumSize(new Dimension(50, 100));
 		add(ingredientScroll, c);
 
 		// Add all of the ingredients attributes
 		// Mastery factor # 6 Loops
 		for (int set = 0; set < 40; set++)
 		{
 			// Mastery factor # 1 Arrays
 			ingredientField[set] = new JTextField(15);
 			amountField[set] = new JTextField(4);
 			unitField[set] = new JTextField(4);
 
 			subPanel.add(new JLabel("Ingredient " + (set + 1) + ":"), c2);
 			c2.gridx++;
 			subPanel.add(ingredientField[set], c2);
 			c2.gridx++;
 
 			subPanel.add(new JLabel("Amount:"), c2);
 			c2.gridx++;
 			subPanel.add(amountField[set], c2);
 			c2.gridx++;
 
 			subPanel.add(new JLabel("Unit:"), c2);
 			c2.gridx++;
 			subPanel.add(unitField[set], c2);
 			c2.gridx++;
 
 			c2.gridx = 0;
 			c2.gridy++;
 
 		}
 
 	}
 
 	// Closes the entire application
 	private class closeAction implements ActionListener
 	{
 		public void actionPerformed(ActionEvent e)
 		{
 			System.exit(0);
 		}
 	}
 
 	private class saveAction implements ActionListener
 	{
 		// Mastery factors # 8 User-defined methods
 		public void actionPerformed(ActionEvent e)
 		{
 
 			// Decides whether to save the file or not
 			boolean allowable = true;
 			allowable = saveRecipeVariables(recipe);
 
 			// Mastery factor # 5 Complex selection
 			if (allowable == true)
 			{
 				JFileChooser sc = new JFileChooser();
 
 				// Only allow text files to be viewed
 				FileNameExtensionFilter filter = new FileNameExtensionFilter("Text Files", "txt");
 				sc.setFileFilter(filter);
 
 				int returnValue = sc.showSaveDialog(null);
 
 				// If the user did not press cancel
 				// Mastery factor # 5 Complex selection
 				if (returnValue == JFileChooser.APPROVE_OPTION)
 				{
 					try
 					{
 						// Mastery factor # 13 File i/o
 						FileWriter writer;
 
 						// String which allows the FileWriter to enter an empty
 						// line
 						String blankLine = System.getProperty("line.separator");
 
 						/*
 						 * if the file contains a text file extention do not add
 						 * .txt at the end. Otherwise add a .txt extension at
 						 * the end
 						 */
 
 						// Mastery factors # 4 Simple selection (ifelse)
 						if (String.valueOf(sc.getSelectedFile()).contains(".txt"))
 						{
 							writer = new FileWriter(sc.getSelectedFile());
 						} else
 						{
 							writer = new FileWriter(sc.getSelectedFile() + ".txt");
 						}
 
 						writer.write(recipe.getRecipeName() + "%delimeter%" + recipe.getAuthor() + "%delimeter%"
 						        + recipe.getServing() + "%delimeter%" + recipe.getRating() + "%delimeter%");
 
 						/*
 						 * Check the positions and according to the current
 						 * position write the format needed.
 						 */
 
 						// Mastery factor # 6 Loops
 						// Mastery factors # 11 Sorting
 						for (int current = 0; current < recipe.getIngredients().length; current++)
 						{
 
 							// Mastery factors # 4 Simple selection (ifelse)
 							if (current == 0)
 							{
 								// Mastery factor # 3 Objects as data records
 								writer.write(recipe.getIngredients()[current]);
 							} else if (current == recipe.getIngredients().length - 1)
 							{
 								if (recipe.getIngredients()[current].isEmpty() == false)
 								{
 									// Mastery factor # 3 Objects as data
 									// records
 									writer.write("," + recipe.getIngredients()[current] + "%delimeter%");
 								} else
 								{
 									writer.write("%delimeter%");
 								}
 							}
 
 							else
 							{
 								if (recipe.getIngredients()[current].isEmpty() == false)
 								{
 									writer.write("," + recipe.getIngredients()[current]);
 								}
 							}
 
 						}
 						// Mastery factor # 6 Loops
 						// Mastery factors # 11 Sorting
 						for (int current = 0; current < recipe.getAmounts().length; current++)
 						{
 
 							if (current == 0)
 							{
 								writer.write(recipe.getAmounts()[current].toString());
 							} else if (current == recipe.getAmounts().length - 1)
 							{
 								// Mastery factors # 4 Simple selection
 								// (ifelse)
 								if (recipe.getAmounts()[current].equals(0.0) == false)
 								{
 									writer.write("," + recipe.getAmounts()[current] + "%delimeter%");
 								} else
 								{
 									writer.write("%delimeter%");
 								}
 							}
 
 							else
 							{
 								if (recipe.getAmounts()[current].equals(0.0) == false)
 								{
 									writer.write("," + recipe.getAmounts()[current]);
 								}
 							}
 
 						}
 						// Mastery factor # 6 Loops
 						// Mastery factors # 11 Sorting
 						for (int current = 0; current < recipe.getUnits().length; current++)
 						{
 
 							/*
 							 * Assessment nested if else loops
 							 */
 							if (current == 0)
 							{
 								writer.write(recipe.getUnits()[current].toString());
 							} else if (current == recipe.getUnits().length - 1)
 							{
 								// Mastery factors # 4 Simple selection
 								// (ifelse)
 								if (recipe.getUnits()[current].isEmpty() == false)
 								{
 									writer.write("," + blankLine + recipe.getUnits()[current] + "%delimeter%");
 								} else
 								{
 									writer.write("%delimeter%");
 								}
 							}
 
 							else
 							{
 								if (recipe.getUnits()[current].isEmpty() == false)
 								{
 									writer.write("," + blankLine + recipe.getUnits()[current]);
 								}
 							}
 
 						}
 
 						writer.write(recipe.getDescription() + "%delimeter%");
 
 						writer.close();
 					} catch (IOException e1)
 					{
 						JOptionPane.showMessageDialog(null, "There was an error saving the file");
 					}
 					// Erase all values in the panel interface, and clear the
 					// Recipe object
 					newRecipe();
 				}
 
 			}
 
 		}
 	}
 
 	// Set the inputed values to the recipe class, if the conditions are met.
 	// Mastery factors # 10 User-defined methods with appropriate return values
 	private boolean saveRecipeVariables(Recipe recipe)
 	{
 		boolean allowed = true;
 
 		// Check to see the required fields are not empty
 		if (recipeField.getText().isEmpty() || authorField.getText().isEmpty() || descriptionEditor.getText().isEmpty()
 		        || ingredientField[0].getText().isEmpty() || amountField[0].getText().isEmpty()
 		        || unitField[0].getText().isEmpty())
 		{
 			JOptionPane.showMessageDialog(null, "One or more of the required fields are empty\n"
 			        + "The first ingredient and its attributes must be filled");
 			allowed = false;
 		} else
 		{
 
 			recipe.setRecipeName(recipeField.getText());
 
 			recipe.setAuthorName(authorField.getText());
 
 			recipe.setServing((Integer) servingBox.getSelectedItem());
 
 			recipe.setRating(Integer.valueOf(String.valueOf(ratingBox.getSelectedItem())));
 
 			recipe.setRecipeDescription(descriptionEditor.getText());
 
 			for (int check = 0; check < ingredientField.length; check++)
 			{
 				if (!ingredientField[check].getText().isEmpty() && unitField[check].getText().isEmpty()
 				        || !ingredientField[check].getText().isEmpty() && amountField[check].getText().isEmpty())
 				{
 					allowed = false;
 					JOptionPane.showMessageDialog(null,
 					        "If one Ingredient name is filled the unit and amount field must also be completed", null,
 					        JOptionPane.ERROR_MESSAGE);
 					break;
 				}
 
 			}
 			for (int set = 0; set < ingredientField.length; set++)
 			{
 				if (ingredientField[set].getText().isEmpty() == false)
 				{
 					recipe.setIngredients(ingredientField[set].getText(), set);
 				} else
 				{
 					recipe.setIngredients("", set);
 				}
 			}
 
 			for (int check = 0; check < amountField.length; check++)
 			{
 				if (!amountField[check].getText().isEmpty() && unitField[check].getText().isEmpty()
 				        || !amountField[check].getText().isEmpty() && ingredientField[check].getText().isEmpty())
 				{
 					allowed = false;
 					JOptionPane.showMessageDialog(null,
 					        "If one amount value is filled the ingredient name and unit field must also be completed",
 					        null, JOptionPane.ERROR_MESSAGE);
 					break;
 				}
 
 			}
 
 			for (int set = 0; set < recipe.getAmounts().length; set++)
 			{
 				if (amountField[set].getText().isEmpty() == false)
 				{
 					try
 					{
 						recipe.setIngredientAmounts(Double.valueOf(amountField[set].getText()), set);
 					} catch (NumberFormatException ex)
 					{
 						JOptionPane.showMessageDialog(null,
 						        "The amount fields must be numbers, decimals are\n accepted");
 						allowed = false;
 						break;
 					}
 				} else
 				{
 					recipe.setIngredientAmounts(0.0, set);
 				}
 
 			}
 
 			for (int check = 0; check < ingredientField.length; check++)
 			{
 				if (!unitField[check].getText().isEmpty() && ingredientField[check].getText().isEmpty()
 				        || !unitField[check].getText().isEmpty() && amountField[check].getText().isEmpty())
 				{
 					allowed = false;
 					JOptionPane.showMessageDialog(null,
 					        "If one unit field is filled the igredient's name and amount field must also be completed",
 					        null, JOptionPane.ERROR_MESSAGE);
 					break;
 				}
 
 			}
 
 			for (int set = 0; set < recipe.getUnits().length; set++)
 			{
 
 				if (unitField[set].getText().isEmpty() == false)
 				{
 					recipe.setIngredientUnits(unitField[set].getText(), set);
 				} else
 				{
 					recipe.setIngredientUnits("", set);
 				}
 
 			}
 
 		}
 
 		return allowed;
 	}
 
 	// Checks if the recipe text files was accepted, and if so open it.
 	private class openAction implements ActionListener
 	{
 		// Mastery factors # 8 User-defined methods
 		public void actionPerformed(ActionEvent e)
 		{
 			JFileChooser sc = new JFileChooser();
 			FileNameExtensionFilter filter = new FileNameExtensionFilter("Text Files", "txt");
 			sc.setFileFilter(filter);
 
 			int returnValue = sc.showOpenDialog(null);
 
 			// Set the values from an opened text file if they were approved
 			// Mastery factor # 5 Complex selection
 			if (returnValue == JFileChooser.APPROVE_OPTION)
 			{
 				try
 				{
 					boolean allowable = true;
 					Scanner scanner = new Scanner(new File(String.valueOf(sc.getSelectedFile())));
 					allowable = getTheRecipe(scanner);
 
 					// Mastery factor # 5 Complex selection
 					if (allowable == true)
 					{
 						recipeField.setText(recipe.getRecipeName());
 						authorField.setText(recipe.getAuthor());
 						servingBox.setSelectedItem(recipe.getServing());
 						ratingBox.setSelectedItem(recipe.getRating());
 						descriptionEditor.setText(recipe.getDescription());
 
 						// Mastery factor # 7 Nested loops
 						for (int set = 0; set < recipe.getIngredients().length; set++)
 						{
 							if (recipe.getIngredients()[set].isEmpty() == false)
 							{
 								ingredientField[set].setText(recipe.getIngredients()[set]);
 							}
 							for (int set2 = 0; set2 < recipe.getAmounts().length; set2++)
 							{
 
 								if (recipe.getAmounts()[set2].equals(0.0) == false)
 								{
 									amountField[set2].setText(recipe.getAmounts()[set2].toString());
 								}
 							}
 							for (int set3 = 0; set3 < recipe.getUnits().length; set3++)
 							{
 								if (recipe.getUnits()[set3].isEmpty() == false)
 								{
 									unitField[set3].setText(recipe.getUnits()[set3]);
 								}
 							}
 						}
 
 					}
 
 				} catch (IOException ex)
 				{
 					JOptionPane.showMessageDialog(null, "Erroneous data found");
 				}
 			}
 		}
 	}
 
 	// Reads a recipe text file, also validates data
 	// Mastery factors # 10 User-defined methods with appropriate return values
 	private boolean getTheRecipe(Scanner scanner)
 	{
 		boolean allowed = true;
 		scanner.useDelimiter("%delimeter%");
 
 		String recipeName = "", authorName = "", recipeDescription = "";
 		String[] ingredientName = new String[40], ingredientUnits = new String[40];
 		String[] amountConversion = new String[40];
 		Double[] ingredientAmounts = new Double[40];
 		Integer ratingValue = 0, servingSize = 0;
 		try
 		{
 			recipeName = scanner.next().trim();
 			authorName = scanner.next().trim();
 			servingSize = scanner.nextInt();
 			ratingValue = scanner.nextInt();
 			ingredientName = scanner.next().split(",");
 			amountConversion = scanner.next().split(",");
 
 			for (int clean = 0; clean < ingredientName.length; clean++)
 			{
 				ingredientName[clean] = ingredientName[clean].trim();
 			}
 			for (int init = 0; init < ingredientAmounts.length; init++)
 			{
 				ingredientAmounts[init] = 0.0;
 			}
 			for (int convert = 0; convert < amountConversion.length; convert++)
 			{
 				if (amountConversion[convert].isEmpty() == false)
 				{
 					ingredientAmounts[convert] = Double.valueOf(amountConversion[convert].trim());
 				}
 			}
 
 			ingredientUnits = scanner.next().split(",");
 
 			for (int clean = 0; clean < ingredientName.length; clean++)
 			{
 				ingredientUnits[clean] = ingredientUnits[clean].trim();
 			}
 			recipeDescription = scanner.next().trim();
 
 		} catch (Exception ex)
 		{
 			JOptionPane.showMessageDialog(null, "There was an error reading the file");
 			allowed = false;
 		}
 		if (recipeName.isEmpty() || authorName.isEmpty() || ingredientName[0].isEmpty() || ingredientUnits[0].isEmpty()
 		        || ingredientAmounts[0].toString().equals("0.0") || recipeDescription.isEmpty()
 		        || ratingValue.toString().equals("0") || servingSize.toString().equals("0"))
 		{
 			allowed = false;
 			JOptionPane.showMessageDialog(null, "There was an error reading the file");
 		} else
 		{
 			newRecipe();
 			recipe.setRecipeName(recipeName);
 			recipe.setAuthorName(authorName);
 			recipe.setRating(ratingValue);
 			recipe.setServing(servingSize);
 			recipe.setRecipeDescription(recipeDescription);
 			recipe.setIngredientAmountArray(ingredientAmounts);
 			for (int set = 0; set < ingredientName.length; set++)
 			{
 				recipe.setIngredients(ingredientName[set], set);
 			}
 			for (int set = 0; set < ingredientUnits.length; set++)
 			{
 				recipe.setIngredientUnits(ingredientUnits[set], set);
 			}
 
 		}
 
 		return allowed;
 	}
 
 	/*
 	 * Action to add a new value at the end of the serving size JComboBox
 	 */
 	private class valueAction implements ActionListener
 	{
 		// Mastery factors # 8 User-defined methods
 		public void actionPerformed(ActionEvent e)
 		{
 
 			String newValString = JOptionPane.showInputDialog(null, "Add a new serving size value");
 			if (newValString == null)
 			{
 			} else
 			{
 				try
 				{
 					Integer newVal = Integer.valueOf(newValString);
 					if (newVal < 1)
 					{
 						JOptionPane.showMessageDialog(null, "The number cannot be less than 1", "Value",
 						        JOptionPane.WARNING_MESSAGE);
 					} else
 					{
 						servingBox.addItem(newVal);
 					}
 				} catch (NumberFormatException ex)
 				{
 					JOptionPane.showMessageDialog(null, "The value cannot be accepted", "Error",
 					        JOptionPane.WARNING_MESSAGE);
 				}
 			}
 		}
 	}
 
 	private class formatColor implements ActionListener
 	{
 		// Mastery factors # 8 User-defined methods
 		public void actionPerformed(ActionEvent e)
 		{
 
 			styleFrame = new JFrame();
 			JPanel stylePanel = new JPanel();
 			styleFrame.setSize(600, 600);
 
 			styleFrame.setTitle("Choose a font color");
 			styleFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 
 			colorChooser.setColor(fontColor);
 			colorChooser.getSelectionModel().addChangeListener(new colorSelected());
 			JButton styleExit = new JButton("Exit");
 
 			styleExit.addActionListener(new disposeAction());
 
 			stylePanel.setLayout(new GridBagLayout());
 			GridBagConstraints styleConstraints = new GridBagConstraints();
 			styleConstraints.gridy = 0;
 			styleConstraints.gridx = 0;
 
 			stylePanel.add(colorChooser, styleConstraints);
 			styleConstraints.gridy++;
 
 			styleConstraints.gridy++;
 			styleConstraints.ipady = 20;
 			styleConstraints.insets = new Insets(10, 0, 10, 0);
 			stylePanel.add(styleExit, styleConstraints);
 
 			styleFrame.add(stylePanel);
 			styleFrame.setLocationRelativeTo(null);
 			styleFrame.setVisible(true);
 			styleFrame.setResizable(false);
 		}
 	}
 
 	// Change the current font color, to the new color
 	public class colorSelected implements ChangeListener
 	{
 		// Mastery factors # 8 User-defined methods
 		public void stateChanged(ChangeEvent e)
 		{
 			// Mastery factor # 14 Use of additional libraries
 			Color newColor = colorChooser.getColor();
 			descriptionEditor.setForeground(newColor);
 			recipeField.setForeground(newColor);
 			authorField.setForeground(newColor);
 			for (int changeColor = 0; changeColor < recipe.getIngredients().length; changeColor++)
 			{
 				ingredientField[changeColor].setForeground(newColor);
 				amountField[changeColor].setForeground(newColor);
 				unitField[changeColor].setForeground(newColor);
 
 			}
 
 		}
 	}
 
 	// Close the color frame
 	private class disposeAction implements ActionListener
 	{
 		// Mastery factors # 8 User-defined methods
 		public void actionPerformed(ActionEvent e)
 		{
 			styleFrame.dispose();
 
 		}
 	}
 
 	private class newAction implements ActionListener
 	{
 		// Mastery factors # 8 User-defined methods
 		public void actionPerformed(ActionEvent e)
 		{
 
 			newRecipe();
 		}
 	}
 
 	private class aboutAction implements ActionListener
 	{
 		// Mastery factors # 8 User-defined methods
 		public void actionPerformed(ActionEvent e)
 		{
 			JOptionPane.showMessageDialog(null, "Program Name: Recipe Application\n" + "Author: Vairon Mendoza\n"
 			        + "Date: Feb/06/2012\n" + "Computer used: HP Compaq 6000 Pro Microtower\n"
 			        + "IDE used: Eclipse IDE\n"
 			        + "Purpose: Provide the user with a simple/flexible recipe organizer application ", "About",
 			        JOptionPane.PLAIN_MESSAGE, null);
 
 		}
 	}
 
 	// Erase all text fields, set drop down list to 1, and initialize the recipe
 	// variable to a new recipe
 	// Mastery factors # 8 User-defined methods
 	public void newRecipe()
 	{
 		recipeField.setText(null);
 		authorField.setText(null);
 		servingBox.setSelectedIndex(0);
 		ratingBox.setSelectedIndex(0);
 		descriptionEditor.setText(null);
 		for (int empty = 0; empty < 40; empty++)
 		{
 			ingredientField[empty].setText(null);
 			amountField[empty].setText(null);
 			unitField[empty].setText(null);
 		}
 		recipe.setRecipeName("");
 		recipe.setAuthorName("");
 		recipe.setRating(0);
 		recipe.setServing(0);
 		recipe.setRecipeDescription("");
 		recipe.emptyIngredients();
 
 		// Mastery factors # 2 User-defined objects
 		recipe = new Recipe();
 	}
 
 }
