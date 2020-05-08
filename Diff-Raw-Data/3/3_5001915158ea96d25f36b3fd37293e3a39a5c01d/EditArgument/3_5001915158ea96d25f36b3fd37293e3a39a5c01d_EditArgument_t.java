 
 package edu.wpi.cs.jburge.SEURAT.editors;
 
 import java.io.Serializable;
 import java.util.Enumeration;
 import java.util.Vector;
 
 import org.eclipse.swt.*;
 import org.eclipse.swt.widgets.*;
 import org.eclipse.swt.layout.*;
 import org.eclipse.swt.events.*;
 import edu.wpi.cs.jburge.SEURAT.rationaleData.*;
 
 public class EditArgument extends NewRationaleElementGUI implements Serializable {
 	
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 3754043729197266131L;
 	private Display ourDisplay;
 	private Shell shell;
 	
 	private Argument ourArg;
 	
 	private Text nameField;
 	private Text argTypeField;
 	private Text descArea;
 	private Text argArea;
 	private Button addButton;
 	private Button cancelButton;
 	
 	private Button selArgButton;
 	
 	private Combo designerBox;
 	private Combo typeBox;
 	private Combo plausibilityBox;
 	private Combo importanceBox;
 	private Combo amountBox;
 	private Combo typeChoice;
 	
 	private Label argSelLabel;
 	private boolean argSet;
 	
 	
 	/**
 	 * The constructor for the editor for rationale arguments.
 	 * @param display - the pointer to the display
 	 * @param editArg - the argument being edited
 	 * @param newItem - indicates if this is a newly created argument
 	 */
 	public EditArgument(Display display, Argument editArg, boolean newItem)
 	{
 		super();
 		ourDisplay = display;
 		shell = new Shell(display, SWT.DIALOG_TRIM | SWT.PRIMARY_MODAL);
 		shell.setText("Argument Information");
 		GridLayout gridLayout = new GridLayout();
 		gridLayout.numColumns = 4;
 		gridLayout.marginHeight = 5;
 		gridLayout.makeColumnsEqualWidth = true;
 		shell.setLayout(gridLayout);
 		
 		ourArg = editArg;
 		
 		if ((newItem) && (editArg.getPtype() != RationaleElementType.ARGUMENT))
 		{
 			argSet = false;
 			ourArg.setImportance(Importance.DEFAULT);
 			ourArg.setPlausibility(Plausibility.HIGH);
 			ourArg.setAmount(10);
 		}
 		else
 		{
 			argSet = true;
 		}
 		/* - do we need to update our status first? probably not...
 		 else
 		 {
 		 ArgumentInferences inf = new ArgumentInferences();
 		 Vector newStat = inf.updateArgument(ourArg);
 		 } */
 		
 //		row 1
 		new Label(shell, SWT.NONE).setText("Name:");
 		
 		nameField =  new Text(shell, SWT.SINGLE | SWT.BORDER);
 		nameField.setText(ourArg.getName());
 		GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING);
 		gridData.horizontalSpan = 1;
 		DisplayUtilities.setTextDimensions(nameField, gridData, 150);
 		nameField.setLayoutData(gridData);
 		
 		new Label(shell, SWT.NONE).setText("Designer:");
 		
 		if (newItem)
 		{
 			RationaleDB db = RationaleDB.getHandle();
 			designerBox = new Combo(shell, SWT.NONE);
 			designerBox.select(0);
 			Vector ourDesigners = db.getNameList(RationaleElementType.DESIGNER);
 			if (ourDesigners != null)
 			{
 				Enumeration desEnum = ourDesigners.elements();
 				while (desEnum.hasMoreElements())
 				{
 					String des = (String) desEnum.nextElement();
 					if (des.compareTo("Designer-Profiles") != 0)
 					{
 						designerBox.add( des );					
 					}
 				}
 			}
 			gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
			gridData.horizontalSpan = 1;
 			DisplayUtilities.setComboDimensions(designerBox, gridData, 100);
 			designerBox.setLayoutData(gridData);
 			
 		}
 		else
 		{
 			if (ourArg.getDesigner() != null)
 			{
 				Text desField =  new Text(shell, SWT.SINGLE | SWT.BORDER | SWT.H_SCROLL | SWT.READ_ONLY);
 				desField.setText(ourArg.getDesigner().getName());
 				gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING);
 				gridData.horizontalSpan = 1;
 				DisplayUtilities.setTextDimensions(desField, gridData, 80);
 				desField.setLayoutData(gridData);	
 			}
 			else
 			{
 				new Label(shell, SWT.NONE).setText("Unknown");
 			}
 		}
 		
 		
 //		row 2
 		
 		new Label(shell, SWT.NONE).setText("Description:");
 		
 		descArea = new Text(shell, SWT.MULTI | SWT.WRAP | SWT.BORDER | SWT.V_SCROLL);
 		descArea.setText(ourArg.getDescription());
 		gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
 		DisplayUtilities.setTextDimensions(descArea, gridData,100, 3);
 		gridData.horizontalSpan = 3;
 		descArea.setLayoutData(gridData);
 		
 //		row 3
 		new Label(shell, SWT.NONE).setText("Type:");
 		
 		typeChoice = new Combo(shell, SWT.NONE);
 		refreshChoices();
 		if ((newItem) && (editArg.getPtype() != RationaleElementType.ARGUMENT))
 		{
 			typeChoice.select(0);
 		}
 		else
 		{
 			for (int h=0; h < typeChoice.getItemCount(); h++)
 			{
 				ArgType choiceArg = ArgType.fromString(typeChoice.getItem(h));
 				
 				if (choiceArg == ourArg.getType())
 				{
 					typeChoice.select(h);
 				}
 			}
 		}
 		gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
 		DisplayUtilities.setComboDimensions(typeChoice, gridData, 100);
 		typeChoice.setLayoutData(gridData);
 		
 		new Label(shell, SWT.NONE).setText("Plausibility:");
 		plausibilityBox = new Combo(shell, SWT.NONE);
 		Enumeration plausEnum = Plausibility.elements();
 		int j=0;
 		Plausibility stype;
 		while (plausEnum.hasMoreElements())
 		{
 			stype = (Plausibility) plausEnum.nextElement();
 			plausibilityBox.add( stype.toString() );
 			if (stype.toString().compareTo(ourArg.getPlausibility().toString()) == 0)
 			{
 				plausibilityBox.select(j);
 //				System.out.println(j);
 			}
 			j++;
 		}
 		gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
 		DisplayUtilities.setComboDimensions(plausibilityBox, gridData, 100);
 		plausibilityBox.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
 		
 		//row 4
 		new Label(shell, SWT.NONE).setText("Amount:");
 		amountBox = new Combo(shell, SWT.NONE);
 		int k;
 		for (k = 1;k < 11; k++)
 		{
 			amountBox.add(  new Integer(k).toString());
 			if (k == ourArg.getAmount())
 			{
 				amountBox.select(k-1);
 			}
 		}
 		gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
 		DisplayUtilities.setComboDimensions(amountBox, gridData, 100);
 		amountBox.setLayoutData(gridData);
 		
 		new Label(shell, SWT.NONE).setText("Importance:");
 		importanceBox = new Combo(shell, SWT.NONE);
 		Enumeration impEnum = Importance.elements();
 		int l=0;
 		Importance itype;
 		while (impEnum.hasMoreElements())
 		{
 			itype = (Importance) impEnum.nextElement();
 			importanceBox.add( itype.toString() );
 			if (itype.toString().compareTo(ourArg.getImportance().toString()) == 0)
 			{
 				importanceBox.select(l);
 			}
 			l++;
 		}
 		gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
 		DisplayUtilities.setComboDimensions(importanceBox, gridData, 100);
 		importanceBox.setLayoutData(gridData);
 		
 //		row 5
 		int argT;
 		new Label(shell, SWT.NONE).setText("Argues:");
 		
 		argArea = new Text(shell, SWT.READ_ONLY | SWT.MULTI | SWT.WRAP | SWT.BORDER | SWT.V_SCROLL);
 		argArea.setText(ourArg.getDescription());
 		
 		if ((!newItem) || (editArg.getPtype() == RationaleElementType.ARGUMENT))
 		{
 			
 			if (ourArg.getAlternative() != null)
 			{
 				argT = 0;
 				argArea.setText(ourArg.getAlternative().toString());
 			}
 			else if (ourArg.getAssumption() != null)
 			{
 				argT = 1;
 				argArea.setText(ourArg.getAssumption().toString());
 			}
 			else if (ourArg.getClaim() != null)
 			{
 				argT = 2;
 				argArea.setText(ourArg.getClaim().toString());
 			}
 			else if (ourArg.getRequirement() != null)
 			{
 				argT = 3;
 				argArea.setText(ourArg.getRequirement().toString());
 			}
 			else
 			{
 				argT = 0;
 				argArea.setText("Undefined");
 			}
 		}
 		else
 		{
 			argT = 0;
 			argArea.setText("Undefined");
 		}
 		gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
 		DisplayUtilities.setTextDimensions(argArea, gridData, 100);
 		gridData.horizontalSpan = 3;
 		gridData.heightHint = descArea.getLineHeight() * 3;
 		argArea.setLayoutData(gridData);
 		
 //		row 6
 		
 		new Label(shell, SWT.NONE).setText(" ");
 //		another type...
 		new Label(shell, SWT.NONE).setText("Argument Type:");
 		
 		typeBox = new Combo(shell, SWT.NONE);
 		typeBox.add("Alternative");
 		typeBox.add("Assumption");
 		typeBox.add("Claim");
 		typeBox.add("Requirement");
 		typeBox.select(argT);
 		
 		gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
 		DisplayUtilities.setComboDimensions(typeBox, gridData, 100);
 		typeBox.setLayoutData(gridData);
 		
 		
 		selArgButton = new Button(shell, SWT.PUSH); 
 		selArgButton.setText("Select");
 		gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING);
 		selArgButton.setLayoutData(gridData);
 		selArgButton.addSelectionListener(new SelectionAdapter() {
 			
 			public void widgetSelected(SelectionEvent event) 
 			{
 				RationaleElementType selType = RationaleElementType.fromString(typeBox.getItem(typeBox.getSelectionIndex()));
 				if (selType == RationaleElementType.REQUIREMENT)
 				{
 					Requirement newReq = null;
 					SelectItem sr = new SelectItem(ourDisplay, RationaleElementType.REQUIREMENT);
 					newReq = (Requirement) sr.getNewItem();
 					if (newReq != null)
 					{
 						ourArg.setRequirement(newReq);
 						argArea.setText(newReq.getName());
 						argSet = true;
 					}
 				}
 				else if (selType == RationaleElementType.ASSUMPTION)
 				{
 					Assumption newAssump = null;
 					SelectItem ar = new SelectItem(ourDisplay, RationaleElementType.ASSUMPTION);
 					newAssump = (Assumption) ar.getNewItem();
 					if (newAssump != null)
 					{
 						ourArg.setAssumption(newAssump);
 						argArea.setText(newAssump.getName());
 						if (importanceBox.getItem(importanceBox.getSelectionIndex()).compareTo(Importance.DEFAULT.toString()) == 0)
 						{
 							int n = importanceBox.getItemCount();
 							boolean found = false;
 							int i = 0;
 							while ((!found) && (i < n))
 							{
 								if (importanceBox.getItem(i).compareTo(Importance.MODERATE.toString()) == 0)
 								{
 									found = true;
 									importanceBox.select(i);
 								}
 								i++;
 								
 							}
 						}
 						argSet = true;
 					}
 				}
 				else if (selType == RationaleElementType.CLAIM)
 				{
 					Claim newClaim = null;
 					SelectItem ar = new SelectItem(ourDisplay, RationaleElementType.CLAIM);
 					newClaim = (Claim) ar.getNewItem();
 					if (newClaim != null)
 					{
 						ourArg.setClaim(newClaim);
 						argArea.setText(newClaim.getName());
 						argSet = true;
 					}
 				}
 				else if (selType == RationaleElementType.ALTERNATIVE)
 				{
 					Alternative newAlt = null;
 					SelectItem aa = new SelectItem(ourDisplay, RationaleElementType.ALTERNATIVE);
 					newAlt = (Alternative) aa.getNewItem();
 					if (newAlt != null)
 					{
 						ourArg.setAlternative(newAlt);
 						argArea.setText(newAlt.getName());
 						argSet = true;
 					}
 				}
 				if (argSet)
 					refreshChoices();
 			}
 		});
 		
 		
 		new Label(shell, SWT.NONE).setText(" ");
 		new Label(shell, SWT.NONE).setText(" ");
 		new Label(shell, SWT.NONE).setText(" ");
 		new Label(shell, SWT.NONE).setText(" ");
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
 					
 					if (!argSet)
 					{
 						MessageBox mbox = new MessageBox(shell, SWT.ICON_ERROR);
 						mbox.setMessage("Need to select an alternative, claim, requirement, or assumption!");
 						mbox.open();
 					}
 					else if (!nameField.getText().trim().equals(""))
 					{
 						ConsistencyChecker checker = new ConsistencyChecker(ourArg.getID(), nameField.getText(), "Arguments");
 						
 						if(ourArg.getName() == nameField.getText() || checker.check())
 						{
 							if ((designerBox.getItemCount() <= 0) || designerBox.getSelectionIndex() >= 0)
 							{
 								ourArg.setName(nameField.getText());
 								ourArg.setDescription(descArea.getText());
 								ourArg.setType(ArgType.fromString(typeChoice.getItem(typeChoice.getSelectionIndex())));
 								ourArg.setPlausibility( Plausibility.fromString(plausibilityBox.getItem(plausibilityBox.getSelectionIndex())));
 								ourArg.setImportance( Importance.fromString(importanceBox.getItem(importanceBox.getSelectionIndex())));
 								ourArg.setAmount( Integer.parseInt(amountBox.getItem(amountBox.getSelectionIndex())));
 								if (designerBox.getItemCount() > 0)
 								{
 									String designerName = designerBox.getItem(designerBox.getSelectionIndex());
 									Designer ourDes = new Designer();
 									ourDes.fromDatabase(designerName);
 									ourArg.setDesigner(ourDes);
 								}
 								
 								//			System.out.println("type " + ourArg.getType().toString());
 								
 //								comment before this made no sense...
 //								System.out.println("saving argument from edit");
 								ourArg.setID(ourArg.toDatabase(ourArg.getParent(), ourArg.getPtype()));
 								shell.close();
 								shell.dispose();	
 							}
 							else {
 								MessageBox mbox = new MessageBox(shell, SWT.ICON_ERROR);
 								mbox.setMessage("Need to provide the Designer name");
 								mbox.open();
 							}
 						}
 						
 					}
 					else
 					{
 						MessageBox mbox = new MessageBox(shell, SWT.ICON_ERROR);
 						mbox.setMessage("Need to provide the Argument name");
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
 					
 					ConsistencyChecker checker = new ConsistencyChecker(ourArg.getID(), nameField.getText(), "Arguments");
 					
 					if(ourArg.getName() == nameField.getText() || checker.check())
 					{
 						ourArg.setName(nameField.getText());
 						ourArg.setDescription(descArea.getText());
 						ourArg.setType(ArgType.fromString(typeChoice.getItem(typeChoice.getSelectionIndex())));
 						ourArg.setPlausibility( Plausibility.fromString(plausibilityBox.getItem(plausibilityBox.getSelectionIndex())));
 						ourArg.setImportance( Importance.fromString(importanceBox.getItem(importanceBox.getSelectionIndex())));
 						ourArg.setAmount( Integer.parseInt(amountBox.getItem(amountBox.getSelectionIndex())));
 						//since this is a save, not an add, the type and parent are ignored
 						//			System.out.println("saving argument from edit, ptype = " + ourArg.getPtype());
 						ourArg.setID(ourArg.toDatabase(ourArg.getParent(), ourArg.getPtype()));	
 						
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
 	
 	public void refreshChoices()
 	{
 		if (ourArg != null)
 		{
 			typeChoice.removeAll();
 			
 			if (ourArg.getClaim() != null)
 			{
 				typeChoice.add(ArgType.SUPPORTS.toString());
 				typeChoice.add(ArgType.DENIES.toString());
 			}
 			else if (ourArg.getAssumption() != null)
 			{
 				typeChoice.add(ArgType.SUPPORTS.toString());
 				typeChoice.add(ArgType.DENIES.toString());
 			}
 			else if (ourArg.getRequirement() != null)
 			{
 				typeChoice.add(ArgType.SATISFIES.toString());
 				typeChoice.add(ArgType.VIOLATES.toString());
 				typeChoice.add(ArgType.ADDRESSES.toString());
 			}
 			else if (ourArg.getAlternative() != null)
 			{
 				typeChoice.add(ArgType.PRESUPPOSES.toString());
 //				typeChoice.addItem(ArgType.PRESUPPOSEDBY);
 				typeChoice.add(ArgType.OPPOSES.toString());
 //				typeChoice.addItem(ArgType.OPPOSEDBY);
 			}
 			else
 			{
 				typeChoice.add("Select Argument");
 			}
 			
 			typeChoice.select(0);
 		}
 	}
 	
 	public Text getArgTypeField()
 	{
 		return argTypeField;
 	}
 	
 	public Label getArgSelLabel()
 	{
 		return argSelLabel;
 	}
 	
 	public RationaleElement getItem()
 	{
 		return ourArg;
 	}
 	
 	
 }
 
 
 
 
 
 
