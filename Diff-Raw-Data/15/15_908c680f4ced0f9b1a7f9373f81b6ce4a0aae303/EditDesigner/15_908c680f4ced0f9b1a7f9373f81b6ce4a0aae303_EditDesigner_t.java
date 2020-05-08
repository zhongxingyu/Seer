 
 package edu.wpi.cs.jburge.SEURAT.editors;
 
 import java.io.Serializable;
 import java.util.Enumeration;
 import java.util.Iterator;
 
 import org.eclipse.swt.*;
 import org.eclipse.swt.widgets.*;
 import org.eclipse.swt.layout.*;
 import org.eclipse.swt.events.*;
 import org.eclipse.swt.graphics.Rectangle;
 
 import edu.wpi.cs.jburge.SEURAT.rationaleData.*;
 
 /**
  * Displays the editor for a designer
  * @author burgeje
  *
  */
 public class EditDesigner extends NewRationaleElementGUI implements Serializable {
 	
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -2113489005792790176L;
 	
 	private Shell shell;
 	/**
 	 * The designer we are editing
 	 */	
 	private Designer ourDesigner;
 	/**
 	 * The designer name
 	 */
 	private Text nameField;
 	/**
 	 * Their experience at the current job
 	 */
 	private Text expHereField;
 	/**
 	 * Their total work experience
 	 */
 	private Text expTotalField;
 	/**
 	 * Their position in the corporation
 	 */
 	private Combo corpPosBox;
 	/**
 	 * Their position in the organization (on this project)
 	 */
 	private Combo orgPosBox;
 	
 	/**
 	 * Button to add the designer
 	 */
 	private Button addButton;
 	/**
 	 * Button to cancel edits
 	 */
 	private Button cancelButton;
 	
 	/**
 	 * Edit a designer or create a new one
 	 * @param display - points back to the display
 	 * @param editDesigner - our designer being created/edited
 	 * @param newItem - true if this is a new designer
 	 */
 	public EditDesigner(Display display, Designer editDesigner, boolean newItem)
 	{
 		super();
 		
 		shell = new Shell(display, SWT.DIALOG_TRIM | SWT.PRIMARY_MODAL);
 		shell.setText("Designer Information");
 		GridLayout gridLayout = new GridLayout();
 		gridLayout.numColumns = 2;
 		gridLayout.marginHeight = 5;
 		gridLayout.makeColumnsEqualWidth = true;
 		shell.setLayout(gridLayout);
 		
 		ourDesigner = editDesigner;
 		
 		
 		//row 1: just the name
 		new Label(shell, SWT.NONE).setText("Name:");
 		
 		nameField =  new Text(shell, SWT.SINGLE | SWT.BORDER);
 		nameField.setText(ourDesigner.getName());
 		GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING);
 		gridData.horizontalSpan = 1;
 		DisplayUtilities.setTextDimensions(nameField, gridData, 100);
 		nameField.setLayoutData(gridData);
 		
 		//row 2: position in company, position on project
 		new Label(shell, SWT.NONE).setText("Position (Company):");
		corpPosBox = new Combo(shell, SWT.DROP_DOWN | SWT.READ_ONLY);
 		gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING);
 		
 		gridData.horizontalSpan = 1;
 		corpPosBox.setLayoutData(gridData);	
 		Enumeration typeEnum = CorpPosType.elements();
 //		System.out.println("got enum");
 		int i = 0;
 		CorpPosType rtype;
 		while (typeEnum.hasMoreElements())
 		{
 			rtype = (CorpPosType) typeEnum.nextElement();
 //			System.out.println("got next element");
 			corpPosBox.add( rtype.toString());
 			if (rtype.toString().compareTo(ourDesigner.getCorpPosition()) == 0)
 			{
 //				System.out.println(ourDec.getType().toString());
 				corpPosBox.select(i);
 //				System.out.println(i);
 			}
 			i++;
 		}
 		
 		new Label(shell, SWT.NONE).setText("Position (Project):");
 		
		orgPosBox = new Combo(shell, SWT.DROP_DOWN | SWT.READ_ONLY);
 		gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING);
 		gridData.horizontalSpan = 1;
 		orgPosBox.setLayoutData(gridData);
 		Enumeration otypeEnum = ProjPosType.elements();
 //		System.out.println("got enum");
 		i = 0;
 		ProjPosType optype;
 		while (otypeEnum.hasMoreElements())
 		{
 			optype = (ProjPosType) otypeEnum.nextElement();
 //			System.out.println("got next element");
 			orgPosBox.add( optype.toString());
 			if (optype.toString().compareTo(ourDesigner.getProjPosition()) == 0)
 			{
 //				System.out.println(ourDec.getType().toString());
 				orgPosBox.select(i);
 //				System.out.println(i);
 			}
 			i++;
 		}
 		
 		//Row 3 -- experience levels
 		
 		new Label(shell, SWT.NONE).setText("Experience (Company):");
 		
 		expHereField =  new Text(shell, SWT.SINGLE | SWT.BORDER);
 		expHereField.setText(new Integer(ourDesigner.getExprHere()).toString());
 		gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING);
 		gridData.horizontalSpan = 1;
 		expHereField.setLayoutData(gridData);		
 		
 		new Label(shell, SWT.NONE).setText("Experience (Total):");
 		
 		expTotalField =  new Text(shell, SWT.SINGLE | SWT.BORDER);
 		expTotalField.setText(new Integer(ourDesigner.getExprTotal()).toString());
 		gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING);
 		gridData.horizontalSpan = 1;
 		expTotalField.setLayoutData(gridData);
 		
 		//Row 4 -- show the expertise levels
 		new Label(shell, SWT.NONE).setText("Area:");
 		new Label(shell, SWT.NONE).setText("Level");
 		
 		List areaModel = new List(shell, SWT.SINGLE | SWT.V_SCROLL);
 		
 		gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
 		gridData.horizontalSpan = 1;
 		int listHeight = areaModel.getItemHeight() * 4;
 		Rectangle trim = areaModel.computeTrim(0, 0, 0, listHeight);
 		gridData.heightHint = trim.height;
 		Iterator altI = ourDesigner.getExpertise().iterator();
 		while (altI.hasNext())
 		{
 			AreaExp area = (AreaExp) altI.next();
 			String altDesc = area.getComponent().getName();
 			areaModel.add( altDesc);
 		}    
 		// add a list of arguments against to the right side
 		areaModel.setLayoutData(gridData);
 		
 		List exprModel = new List(shell, SWT.SINGLE | SWT.V_SCROLL);
 		
 //		gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
 		//		gridData.horizontalSpan = 2;
 		//	int list2Height = exprModel.getItemHeight() * 4;
 //		Rectangle trim2 = exprModel.computeTrim(0, 0, 0, list2Height);
 //		gridData.heightHint = trim.height;
 		Iterator alt2I = ourDesigner.getExpertise().iterator();
 		while (alt2I.hasNext())
 		{
 			AreaExp area = (AreaExp) alt2I.next();
 			String altDesc = new Integer(area.getLevel()).toString();
 			exprModel.add( altDesc);
 		}    
 		// add a list of arguments against to the right side
 		exprModel.setLayoutData(gridData);
 		
 		addButton = new Button(shell, SWT.PUSH); 
 		gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING);
 		addButton.setLayoutData(gridData);
 		if (newItem)
 		{
 			addButton.setText("Add");
 			addButton.addSelectionListener(new SelectionAdapter() {
 				
 				public void widgetSelected(SelectionEvent event) 
 				{
 					//int typeIndex;
 					//int statusIndex;
 					canceled = false;
 					if (!nameField.getText().trim().equals(""))
 					{
 						ConsistencyChecker checker = new ConsistencyChecker(ourDesigner.getID(), nameField.getText(), "designerprofiles");
 						
 						if(ourDesigner.getName() == nameField.getText() || checker.check())
 						{
 							ourDesigner.setName(nameField.getText());
 							ourDesigner.setCorpPosition((String) corpPosBox.getItem(corpPosBox.getSelectionIndex()));
 							ourDesigner.setProjPosition((String) orgPosBox.getItem(orgPosBox.getSelectionIndex()));
 							ourDesigner.setExprHere((new Integer(expHereField.getText())).intValue());
 							ourDesigner.setExprTotal((new Integer(expTotalField.getText())).intValue());
 							
 							//comment before this made no sense...
 							ourDesigner.toDatabase();
 							
 							shell.close();
 							shell.dispose();	
 						}
 						
 					}
 					else
 					{
 						MessageBox mbox = new MessageBox(shell, SWT.ICON_ERROR);
 						mbox.setMessage("Need to provide the Contingency name");
 						mbox.open();
 					}
 				}
 			});
 			
 		}
 		else
 		{
 			addButton.setText("Save");
 			addButton.addSelectionListener(new SelectionAdapter() {
 				
 				public void widgetSelected(SelectionEvent event) 
 				{
 					//int typeIndex;
 					canceled = false;
 					//int statusIndex;
 					
 					ConsistencyChecker checker = new ConsistencyChecker(ourDesigner.getID(), nameField.getText(), "designerprofiles");
 					
 					if(ourDesigner.getName() == nameField.getText() || checker.check())
 					{
 						ourDesigner.setName(nameField.getText());
 						ourDesigner.setCorpPosition((String) corpPosBox.getItem(corpPosBox.getSelectionIndex()));
 						ourDesigner.setProjPosition((String) orgPosBox.getItem(orgPosBox.getSelectionIndex()));
 						ourDesigner.setExprHere((new Integer(expHereField.getText())).intValue());
 						ourDesigner.setExprTotal((new Integer(expTotalField.getText())).intValue());
 						
 						//since this is a save, not an add, the type and parent are ignored
 						ourDesigner.toDatabase();
 						
 						//			RationaleDB db = RationaleDB.getHandle();
 						//			db.addAssumption(ourAssump);
 						
 						shell.close();
 						shell.dispose();	
 					}
 				}
 			});
 		}
 		
 		
 		
 		cancelButton = new Button(shell, SWT.PUSH); 
 		cancelButton.setText("Cancel");
 		gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING);
 		cancelButton.setLayoutData(gridData);
 		cancelButton.addSelectionListener(new SelectionAdapter() {
 			
 			public void widgetSelected(SelectionEvent event) 
 			{
 				canceled = true;
 				shell.close();
 				shell.dispose();
 			}
 		});
 		
 		shell.pack();
 		shell.open();
 		while (!shell.isDisposed()) {
 			if (!display.readAndDispatch()) display.sleep();
 		}
 	}
 	
 	
 	/**
 	 * Get our designer
 	 */  
 	public RationaleElement getItem()
 	{
 		return ourDesigner;
 	}
 	
 	
 }
 
 
 
 
 
 
