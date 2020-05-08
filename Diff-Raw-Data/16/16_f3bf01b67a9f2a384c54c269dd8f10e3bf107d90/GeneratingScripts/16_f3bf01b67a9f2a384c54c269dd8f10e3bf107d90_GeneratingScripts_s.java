 /**odel= atas
  * 
  */
 package no.whg.mini;
 
 import java.util.Vector;
 
 
 /**
  * @author Peer Andreas Stange
  *
  */
 public class GeneratingScripts {
 	/**
 	 * Class that generates the string that is printed to a file
 	 * 
 	 */
 	private Vector<TableData> generatingVector;
 	public String longAssString;
 	private String className;
 	
 	/**
 	 * Empty constructor
 	 */
 	public GeneratingScripts()
 	{
 		
 	}
 	
 	/**
 	 * 
 	 * @param genVec the vector that holds the items in a table
 	 * @param className holds the name of the class
 	 */
 	public GeneratingScripts(Vector<TableData> genVec, String className)
 	{
 		this.className = className;
 		this.generatingVector = genVec;
 		longAssString = new String("import javax.swing.*; \n import java.awt.*; \n public class  " + this.className
 		+ " extends JPanel{");
 	}
 	
 	/**
 	 *  function that generates the entire string from the vector of table data
 	 * @return the string that will be written to file
 	 */
 	public String generate()
 	{
 		for(int i = 0; i < generatingVector.size(); i++)
 		 {	//get the info from the vectors current element that is relevant for writing the code
 			TableData currentRow = generatingVector.elementAt(i);
 			String currentType = currentRow.getType();
 			String varName = currentRow.getVarName();
 			String text = currentRow.getText();
 			String[] spinnerListArray = currentRow.getOptionsSpinnerListArray();
 			String optionsColumns = currentRow.getOptionsColumns();
 			String optionsHeight = currentRow.getOptionsHeight();
 			String optionsWidth = currentRow.getOptionsWidth();
 			String optionsRows = currentRow.getOptionsRows();
 			float optionsMaxValue = currentRow.getOptionsMaxValue();
 			float optionsMinValue = currentRow.getOptionsMinValue();
 			float optionsStartValue = currentRow.getOptionsStartValue();
 			float optionsStepValue = currentRow.getOptionsStepValue();
 			
 			//checks type classification
 			if(currentType == "JLabel")
 			{
 				longAssString += "JLabel " + varName + " = new JLabel(\"" + text +"\");\n";  
 			}
 			else if(currentType == "JButton")
 			{
 				longAssString += "JButton " + varName + " = new JButton(" + text + ");\n";
 			}
 			else if(currentType == "JTextField")
 			{
 				
 				if(optionsRows == null)
 				{
 					longAssString += "JTextField " + varName + " = new JTextField(\"" + text + "\");\n";
 				}
 				else
 				{
 					longAssString += "JTextField " + varName + " = new JTextField(\"" + text + "\", " + optionsRows +");\n";
 				}
 			}
 			else if(currentType == "JTextArea")
 			{
 				if(optionsColumns == null)
 				{
 					longAssString += "JTextArea " + varName + " = new JTextArea(\"" + text + "\");\n";
 				}
 				else
 				{
 					longAssString += "JTextArea " + varName + " = new JTextArea(\"" + text + "\","+ optionsRows
 							+ "," + optionsColumns + ");\n";
 				}
 			}
 			else if(currentType == "JCheckBox")
 			{
 				longAssString += "JCheckBox " + varName + " = new JCheckBox(\"" + text + "\");\n";
 			}
 			else if(currentType == "JList")
 			{
 				longAssString += "JList " + varName + " = new JList(\"" + text + "\");\n";
  			}
 			else if(currentType == "JComboBox")
 			{
 				if(optionsHeight == null)
 				{
 					longAssString += "JComboBox" + varName + " = new JComboBox(\"" + text + "\");\n";
 				}
 				else
 				{
 					longAssString += "String " + varName + "Data = " + "\"" + text + "\";\n"
 							+ "DefaultComboBoxModel " +varName+ "Model" + " = new DefaultComboBoxModel(" +varName +
 							"Data.split(\"[\\\\p{Punct}\\\\s]+\"));\n"
 							+ "JComboBox" + varName + " = new JComboBox(" + varName +"Model);\n";
 				}
 			}
 			else if(currentType == "JSpinnerList")
 			{
 				longAssString += "JSpinnerList " + varName + " = new JSpinnerList(\"" + text + "\");\n";
 			}
 			else if(currentType == "JSpinnerNumber")
 			{
 				longAssString += "JSpinnerNumber " + varName + " = new JSpinnerNumber(\"" + text + "\");\n";
 			}
 		}
 		
 		longAssString += "public " + this.className + "(){\n GridBagLayout layout  = new GridBagLayout();\n"
 				+ "GridBagConstraints gbc = new  GridBagConstraints();\n"
 				+ "setLayout(layout);\n";
 		
 		for(int j = 0; j < generatingVector.size(); j++)
 		{
 			TableData currentRow = generatingVector.elementAt(j);
 			
 			String type = currentRow.getType();
 			String name = currentRow.getVarName();
 			String row = currentRow.getRow();
 			String column = currentRow.getColumn();
 			String rows = currentRow.getRows();
 			String columns = currentRow.getColumns();
 			String fill = currentRow.getFill();
 			String anchor = currentRow.getAnchor();
 			String optionsWidth = currentRow.getOptionsWidth();
 			String optionsHeight = currentRow.getOptionsHeight();
 			boolean wordWrap = currentRow.isOptionsWordWrapping();
 			boolean scrollPane = currentRow.isOptionsScrollPane();
 			
 			if(type == "JTextArea")
 			{
 				if(wordWrap)
 				{
 					longAssString += name + ".setLineWrap(true);\n" +
 							name + ".setWordWrap(true);\n";
 				}
 				if(scrollPane)
 				{
 					longAssString += "JScrollPane " + name + "ScrollPane = new ScrollPane(" + name + ");\n"
 							+ name + "ScrollPane.setPreferredSize(new java.awt.Dimension(" + optionsWidth + "," + optionsHeight + "));\n";
 				}
 			}
 			else if(type == "JTextField")
 			{
 				if(optionsWidth != null)
 				{
 					longAssString += name + ".setPreferredSize(new java.awt.Dimension("+ optionsWidth +"," + optionsHeight + "));\n";
 				}
 			}
 			else if(type == "JComboBox")
 			{
 				if(optionsWidth != null)
 				{
 					longAssString +=  name + ".setPreferredSize(new java.awt.Dimension(" + optionsWidth + "," + optionsHeight +"));\n";
 				}
 			}
 			longAssString += "gbc.gridx = " + row + ";\n";
 			longAssString += "gbc.gridy = " + column + ";\n";
 			longAssString += "gbc.gridwidth = " + rows + ";\n";
 			longAssString += "gbc.gridheight = " + columns + ";\n";
			longAssString += "gbc.anchor = java.awt.GridBagConstraints." + anchor + ";\n";
			longAssString += "gbc.fill = java.awt.GridBagConstraints." + fill + ";\n";
 			
 			if(scrollPane)
 			{
 				longAssString += "layout.setConstraints(" + name + "ScrollPane, gbc);\n";
 				longAssString += "add(" + name + "ScrollPane);\n";
 				
 			}
 			else
 			{
 				longAssString += "layout.setConstraints(" + name + ", gbc);\n";
 				longAssString += "add(" + name + ");\n";
 			}
 		}
 		
 		longAssString += "}}";
 		return longAssString;
 	}
 }
