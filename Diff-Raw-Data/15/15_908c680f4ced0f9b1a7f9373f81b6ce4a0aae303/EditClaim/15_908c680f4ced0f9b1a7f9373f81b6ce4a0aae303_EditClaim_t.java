 
 package edu.wpi.cs.jburge.SEURAT.editors;
 
 import java.io.Serializable;
 import java.util.Enumeration;
 
 import org.eclipse.swt.*;
 import org.eclipse.swt.widgets.*;
 import org.eclipse.swt.layout.*;
 import org.eclipse.swt.events.*;
 
 import edu.wpi.cs.jburge.SEURAT.rationaleData.*;
 
 /**
  * Used as the editor to enter and modify claim information
  * @author burgeje
  *
  */
 public class EditClaim extends NewRationaleElementGUI implements Serializable {
 	
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	
 	/**
 	 * The display
 	 */
 	private Display ourDisplay;
 	/**
 	 * The GUI shell
 	 */
 	private Shell shell;
 	/**
 	 * The claim being edited
 	 */
 	private Claim ourClaim;
 	/**
 	 * The name of the claim
 	 */  
 	private Text nameField;
 	/**
 	 * The description
 	 */
 	private Text descArea;
 //	private boolean newItem;
 	
 	/**
 	 * Button to add the claim
 	 */
 	private Button addButton;
 	/**
 	 * Button to cancel edits
 	 */
 	private Button cancelButton;
 	/**
 	 * Button to select an argument ontology entry
 	 */
 	private Button selOntButton;
 	
 	/**
 	 * Combo box to choose the direction (IS, NOT)
 	 */  
 	private Combo directionBox;
 	/**
 	 * Combo box to select importance
 	 */
 	private Combo importanceBox;
 	/**
 	 * The ontology entry description
 	 */
 	private Label ontDesc;
 	// private MessageBox errorBox; 		//dialog for the error message
 	
 	
 	
 	/**
 	 * Constructor for editing claim information
 	 * @param display - points to the display
 	 * @param editClaim - the claim being edited
 	 * @param newItem - true if this is a new claim being created
 	 */
 	public EditClaim(Display display, Claim editClaim, boolean newItem)
 	{
 		super();
 		ourDisplay = display;
 		shell = new Shell(display, SWT.DIALOG_TRIM | SWT.PRIMARY_MODAL);
 		shell.setText("Claim Information");
 		GridLayout gridLayout = new GridLayout();
 		gridLayout.numColumns = 4;
 		gridLayout.marginHeight = 5;
 		gridLayout.makeColumnsEqualWidth = true;
 		shell.setLayout(gridLayout);
 		
 		ourClaim = editClaim;
 		
 		if (newItem)
 		{
 			ourClaim.setDirection(Direction.IS);
 			ourClaim.setImportance(Importance.DEFAULT);
 		}
 		else
 		{
 		}
 		/* - do we need to update our status first? probably not...
 		 else
 		 {
 		 ClaimInferences inf = new ClaimInferences();
 		 Vector newStat = inf.updateClaim(ourClaim);
 		 } */
 		
 //		row 1
 		new Label(shell, SWT.NONE).setText("Name:");
 		
 		nameField =  new Text(shell, SWT.SINGLE | SWT.BORDER);
 		nameField.setText(ourClaim.getName());
 		GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING);
 		gridData.horizontalSpan = 3;
 		DisplayUtilities.setTextDimensions(nameField, gridData, 150);
 		nameField.setLayoutData(gridData);
 		
 //		row 2
 		
 		new Label(shell, SWT.NONE).setText("Description:");
 		
 		descArea = new Text(shell, SWT.MULTI | SWT.WRAP | SWT.BORDER | SWT.V_SCROLL);
 		descArea.setText(ourClaim.getDescription());
 		gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
 		DisplayUtilities.setTextDimensions(descArea, gridData,100, 2);
 		gridData.horizontalSpan = 3;
 		descArea.setLayoutData(gridData);
 		
 //		row 3
 		new Label(shell, SWT.NONE).setText("Direction:");
 		
 		
		directionBox = new Combo(shell, SWT.DROP_DOWN | SWT.READ_ONLY);
 		Enumeration directionEnum = Direction.elements();
 		int j=0;
 		Direction stype;
 		while (directionEnum.hasMoreElements())
 		{
 			stype = (Direction) directionEnum.nextElement();
 			directionBox.add( stype.toString() );
 			if (stype.toString().compareTo(ourClaim.getDirection().toString()) == 0)
 			{
 				directionBox.select(j);
 //				System.out.println(j);
 			}
 			j++;
 		}
 		gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
 		DisplayUtilities.setComboDimensions(directionBox, gridData, 100);
 		directionBox.setLayoutData(gridData);
 		
 		new Label(shell, SWT.NONE).setText("Importance:");
		importanceBox = new Combo(shell, SWT.DROP_DOWN | SWT.READ_ONLY);
 		Enumeration impEnum = Importance.elements();
 		int l=0;
 		Importance itype;
 		while (impEnum.hasMoreElements())
 		{
 			itype = (Importance) impEnum.nextElement();
 			importanceBox.add( itype.toString() );
 			if (itype.toString().compareTo(ourClaim.getImportance().toString()) == 0)
 			{
 				importanceBox.select(l);
 			}
 			l++;
 		}
 		gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
 		DisplayUtilities.setComboDimensions(importanceBox, gridData, 100);
 		importanceBox.setLayoutData(gridData);
 		
 //		row 4
 		new Label(shell, SWT.NONE).setText("Ontology Entry:");
 		ontDesc = new Label(shell, SWT.WRAP);
 		
 		if (!newItem)
 		{
 			ontDesc.setText(ourClaim.getOntology().toString());
 		}
 		else
 		{
 			ontDesc.setText("Undefined");
 		}
 		gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
 		gridData.horizontalSpan = 2;
 		ontDesc.setLayoutData(gridData);
 		
 //		new Label(shell, SWT.NONE).setText(" ");
 		
 		selOntButton = new Button(shell, SWT.PUSH); 
 		selOntButton.setText("Select");
 		gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING);
 		selOntButton.setLayoutData(gridData);
 		selOntButton.addSelectionListener(new SelectionAdapter() {
 			
 			public void widgetSelected(SelectionEvent event) 
 			{
 				OntEntry newOnt = null;
 				SelectOntEntry ar = new SelectOntEntry(ourDisplay, true);
 				/*
 				 SelectOntEntryGUI ar = new SelectOntEntryGUI(lf, true);
 				 ar.show();
 				 */
 				newOnt = ar.getSelOntEntry();
 				if (newOnt != null)
 				{
 					ourClaim.setOntology(newOnt);
 					ontDesc.setText(newOnt.toString());
 				}
 				
 			}
 		});
 		
 		
 		new Label(shell, SWT.NONE).setText(" ");
 		new Label(shell, SWT.NONE).setText(" ");
 //		new Label(shell, SWT.NONE).setText(" ");
 //		new Label(shell, SWT.NONE).setText(" ");
 		addButton = new Button(shell, SWT.PUSH); 
 		gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING);
 		addButton.setLayoutData(gridData);
 		if (newItem)
 		{
 			addButton.setText("Add");
 			addButton.addSelectionListener(new SelectionAdapter() {
 				
 				public void widgetSelected(SelectionEvent event) 
 				{
 					canceled = false;
 					if (ourClaim.getOntology()!= null)
 					{
 						if (!nameField.getText().trim().equals(""))
 						{
 							ConsistencyChecker checker = new ConsistencyChecker(ourClaim.getID(), nameField.getText(), "Claims");
 							
 							if(ourClaim.getName() == nameField.getText() || checker.check())
 							{
 								ourClaim.setName(nameField.getText());
 								ourClaim.setDescription(descArea.getText());
 								ourClaim.setDirection(Direction.fromString(directionBox.getItem(directionBox.getSelectionIndex())));
 								ourClaim.setImportance( Importance.fromString(importanceBox.getItem(importanceBox.getSelectionIndex())));
 								
 //								comment before this made no sense...
 								ourClaim.setID(ourClaim.toDatabase());
 								shell.close();
 								shell.dispose();
 							}
 						}
 						else
 						{
 							MessageBox mbox = new MessageBox(shell, SWT.ICON_ERROR);
 							mbox.setMessage("Need to provide the Claim name");
 							mbox.open();
 						}
 					}
 					else
 					{
 						MessageBox mbox = new MessageBox(shell, SWT.ICON_ERROR);
 						mbox.setMessage("Need to select an Ontology Entry");
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
 					canceled = false;
 					
 					ConsistencyChecker checker = new ConsistencyChecker(ourClaim.getID(), nameField.getText(), "Claims");
 					
 					if(ourClaim.getName() == nameField.getText() || checker.check())
 					{
 						ourClaim.setName(nameField.getText());
 						ourClaim.setDescription(descArea.getText());
 						ourClaim.setDirection(Direction.fromString(directionBox.getItem(directionBox.getSelectionIndex())));
 						ourClaim.setImportance( Importance.fromString(importanceBox.getItem(importanceBox.getSelectionIndex())));
 						//since this is a save, not an add, the type and parent are ignored
 						ourClaim.setID(ourClaim.toDatabase());
 						
 						//			RationaleDB db = RationaleDB.getHandle();
 						//			db.addClaim(ourClaim);
 						
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
 	 * Get the claim information
 	 */
 	public RationaleElement getItem()
 	{
 		return ourClaim;
 	}
 	
 	
 }
 
 
 
 
 
 
