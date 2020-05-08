 package pg13.presentation;
 
 import java.util.Enumeration;
 
 import org.eclipse.swt.widgets.Composite;
 import pg13.org.eclipse.wb.swt.SWTResourceManager;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.layout.FormLayout;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.swt.layout.FormData;
 import org.eclipse.swt.layout.FormAttachment;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.MessageBox;
 
 import pg13.business.PuzzleManager;
 import pg13.models.Category;
 import pg13.models.Cryptogram;
 import pg13.models.Difficulty;
 import pg13.models.Puzzle;
 import pg13.models.PuzzleValidationException;
 
 import acceptanceTests.Register;
 
 public class PuzzlePropertiesWidget extends Composite
 {
 	boolean editMode; // can we edit the properties of the puzzle?
 	private Text txtPuzzleName;
 	private Text txtDescription;
 	private Label lblCategory;
 	private Label lblCategoryFixedText;
 	private Label lblDifficulty;
 	private Label lblDifficultyFixedText;
 	private Combo cmbDificulty;
 	private Combo cmbCategory;
 	private Button btnSavePuzzle;
 	private Button btnCheckSolution;
 	private Puzzle displayingPuzzle;
 
 	public PuzzlePropertiesWidget(Composite parent, int style, Puzzle displayingPuzzle, boolean editMode)
 	{
 		super(parent, style);
		Register.newWindow(this, "PuzzlePropertiesWidget" + (editMode == true? "Edit" : "View" ));
 		
 		this.displayingPuzzle = displayingPuzzle;
 		setLayout(new FormLayout());
 
 		// puzzle name
 		txtPuzzleName = new Text(this, SWT.BORDER);
 		txtPuzzleName.setFont(SWTResourceManager.getFont("Segoe UI", 16, SWT.NORMAL));
 		FormData fd_txtPuzzleName = new FormData();
 		fd_txtPuzzleName.top = new FormAttachment(0, 10);
 		fd_txtPuzzleName.left = new FormAttachment(0, 10);
 		fd_txtPuzzleName.bottom = new FormAttachment(0, 49);
 		fd_txtPuzzleName.right = new FormAttachment(100, -10);
 		txtPuzzleName.setLayoutData(fd_txtPuzzleName);
 		txtPuzzleName.addModifyListener(new ModifyListener()
 		{
 			@Override
 			public void modifyText(ModifyEvent e)
 			{
 				updatePuzzleTitle();
 			}
 		});
 
 		// puzzle description label
 		Label lblDescription = new Label(this, SWT.NONE);
 		FormData fd_lblDescription = new FormData();
 		fd_lblDescription.top = new FormAttachment(txtPuzzleName, 10);
 		fd_lblDescription.left = new FormAttachment(0, 10);
 		lblDescription.setLayoutData(fd_lblDescription);
 		lblDescription.setText(Constants.DESCRIPTION);
 
 		// puzzle description text field
 		txtDescription = new Text(this, SWT.BORDER | SWT.WRAP);
 		FormData fd_txtDescription = new FormData();
 		fd_txtDescription.bottom = new FormAttachment(lblDescription, 134,
 				SWT.BOTTOM);
 		fd_txtDescription.right = new FormAttachment(100, -10);
 		fd_txtDescription.top = new FormAttachment(lblDescription, 4);
 		fd_txtDescription.left = new FormAttachment(0, 10);
 		txtDescription.setLayoutData(fd_txtDescription);
 		txtDescription.addModifyListener(new ModifyListener()
 		{
 			@Override
 			public void modifyText(ModifyEvent e)
 			{
 				updatePuzzleDescription();
 			}
 		});
 		txtDescription.setTextLimit(Constants.MAX_DESCRIPTION_CHARS);
 
 		// category label
 		lblCategory = new Label(this, SWT.NONE);
 		FormData fd_lblCategory = new FormData();
 		fd_lblCategory.left = new FormAttachment(0, 10);
 		fd_lblCategory.top = new FormAttachment(txtDescription, 10);
 		lblCategory.setLayoutData(fd_lblCategory);
 		lblCategory.setText(Constants.CATEGORY);
 
 		// category selection box
 		cmbCategory = new Combo(this, SWT.READ_ONLY);
 		cmbCategory.setFont(SWTResourceManager.getFont("Segoe UI", 9, SWT.NORMAL));
 		cmbCategory.setItems(getCategories(Category.values()));
 		FormData fd_cmbCategory = new FormData();
 		fd_cmbCategory.right = new FormAttachment(60);
 		fd_cmbCategory.top = new FormAttachment(lblCategory, 4);
 		fd_cmbCategory.left = new FormAttachment(0, 10);
 		cmbCategory.setLayoutData(fd_cmbCategory);
 		cmbCategory.select(0);
 		cmbCategory.addModifyListener(new ModifyListener()
 		{
 			@Override
 			public void modifyText(ModifyEvent e)
 			{
 				updatePuzzleCategory();
 			}
 		});
 
 		// category fixed label
 		lblCategoryFixedText = new Label(this, SWT.BORDER);
 		lblCategoryFixedText.setFont(SWTResourceManager.getFont("Segoe UI", 11,	SWT.NORMAL));
 		FormData fd_lblCategoryFixedText = new FormData();
 		fd_lblCategoryFixedText.height = 21;
 		fd_lblCategoryFixedText.right = new FormAttachment(60);
 		fd_lblCategoryFixedText.top = new FormAttachment(lblCategory, 4);
 		fd_lblCategoryFixedText.left = new FormAttachment(0, 10);
 		lblCategoryFixedText.setLayoutData(fd_lblCategoryFixedText);
 
 		// difficulty label
 		lblDifficulty = new Label(this, SWT.NONE);
 		FormData fd_lblDifficulty = new FormData();
 		fd_lblDifficulty.top = new FormAttachment(cmbCategory, 10);
 		fd_lblDifficulty.left = new FormAttachment(0, 10);
 		lblDifficulty.setLayoutData(fd_lblDifficulty);
 		lblDifficulty.setText(Constants.DIFFICULTY);
 
 		// difficulty selection box
 		cmbDificulty = new Combo(this, SWT.READ_ONLY);
 		cmbDificulty.setFont(SWTResourceManager.getFont("Segoe UI", 9, SWT.NORMAL));
 		cmbDificulty.setItems(getCategories(Difficulty.values()));
 		FormData fd_cmbDificulty = new FormData();
 		fd_cmbDificulty.right = new FormAttachment(60);
 		fd_cmbDificulty.top = new FormAttachment(lblDifficulty, 4);
 		fd_cmbDificulty.left = new FormAttachment(0, 10);
 		cmbDificulty.setLayoutData(fd_cmbDificulty);
 		cmbDificulty.select(0);
 		cmbDificulty.addModifyListener(new ModifyListener()
 		{
 			@Override
 			public void modifyText(ModifyEvent e)
 			{
 				updatePuzzleDifficulty();
 			}
 		});
 
 		// difficulty fixed label
 		lblDifficultyFixedText = new Label(this, SWT.BORDER);
 		lblDifficultyFixedText.setFont(SWTResourceManager.getFont("Segoe UI",
 				11, SWT.NORMAL));
 		FormData fd_lblDifficultyFixedText = new FormData();
 		fd_lblDifficultyFixedText.height = 21;
 		fd_lblDifficultyFixedText.right = new FormAttachment(60);
 		fd_lblDifficultyFixedText.top = new FormAttachment(lblDifficulty, 4);
 		fd_lblDifficultyFixedText.left = new FormAttachment(0, 10);
 		lblDifficultyFixedText.setLayoutData(fd_lblDifficultyFixedText);
 
 		// save puzzle button
 		this.btnSavePuzzle = new Button(this, SWT.NONE);
 		FormData fd_btnSavePuzzle = new FormData();
 		fd_btnSavePuzzle.top = new FormAttachment(100, -40);
 		fd_btnSavePuzzle.bottom = new FormAttachment(100, -10);
 		fd_btnSavePuzzle.left = new FormAttachment(50, -70);
 		fd_btnSavePuzzle.right = new FormAttachment(50, 70);
 		btnSavePuzzle.setLayoutData(fd_btnSavePuzzle);
 		btnSavePuzzle.setText(MessageConstants.SAVE_PUZZLE);
 		btnSavePuzzle.addSelectionListener(new SelectionListener()
 		{
 
 			@Override
 			public void widgetSelected(SelectionEvent e)
 			{
 				savePuzzle();
 			}
 
 			@Override
 			public void widgetDefaultSelected(SelectionEvent e)
 			{
 				savePuzzle();
 			}
 
 		});
 
 		// check solution button
 		this.btnCheckSolution = new Button(this, SWT.NONE);
 		btnCheckSolution.addSelectionListener(new SelectionListener()
 		{
 			@Override
 			public void widgetSelected(SelectionEvent e)
 			{
 				checkSolution();
 			}
 
 			@Override
 			public void widgetDefaultSelected(SelectionEvent e)
 			{
 				checkSolution();
 			}
 		});
 		FormData fd_btnCheckSolution = new FormData();
 		fd_btnCheckSolution.top = new FormAttachment(100, -40);
 		fd_btnCheckSolution.bottom = new FormAttachment(100, -10);
 		fd_btnCheckSolution.left = new FormAttachment(50, -70);
 		fd_btnCheckSolution.right = new FormAttachment(50, 70);
 		btnCheckSolution.setLayoutData(fd_btnCheckSolution);
 		btnCheckSolution.setText(MessageConstants.CHECK_SOLUTION);
 
 		this.setEditMode(editMode);
 	}
 
 	public void setPuzzle(Cryptogram newPuzzle)
 	{
 		this.displayingPuzzle = newPuzzle;
 		updateFields();
 	}
 
 	private void updateFields()
 	{
 		this.txtPuzzleName.setText((displayingPuzzle.getTitle() == null ? "" : displayingPuzzle.getTitle()));
 		this.txtDescription.setText((displayingPuzzle.getDescription() == null ? "" : displayingPuzzle.getDescription()));
 		this.cmbCategory.select(getComboIndex(displayingPuzzle.getCategory(), Category.values()));
 		this.lblCategoryFixedText.setText(cmbCategory.getText());
 		this.cmbDificulty.select(getComboIndex(displayingPuzzle.getDifficulty(), Difficulty.values()));
 		this.lblDifficultyFixedText.setText(cmbDificulty.getText());
 	}
 
 	@SuppressWarnings("rawtypes")
 	private String[] getCategories(Enumeration[] catEnum)
 	{
 		String[] categories = new String[catEnum.length];
 
 		for (int i = 0; i < catEnum.length; i++)
 		{
 			categories[i] = catEnum[i].toString();
 		}
 		return categories;
 	}
 
 	@SuppressWarnings("rawtypes")
 	private int getComboIndex(Enumeration key, Enumeration[] list)
 	{
 		int result = -1;
 
 		for (int i = 0; i < list.length; i++)
 		{
 			if (list[i].equals(key))
 			{
 				result = i;
 			}
 		}
 
 		return result;
 	}
 
 	private void setEditMode(boolean editMode)
 	{
 		this.editMode = editMode;
 
 		this.cmbCategory.setVisible(this.editMode);
 		this.lblCategoryFixedText.setVisible(!this.editMode);
 		this.cmbDificulty.setVisible(this.editMode);
 		this.lblDifficultyFixedText.setVisible(!this.editMode);
 
 		this.txtDescription.setEditable(this.editMode);
 		this.txtPuzzleName.setEditable(this.editMode);
 
 		this.btnSavePuzzle.setVisible(this.editMode);
 		this.btnCheckSolution.setVisible(!this.editMode);
 	}
 
 	private void savePuzzle()
 	{
 		MessageBox dialog;
 
 		try
 		{
 			// make sure the puzzle is valid to save
 			displayingPuzzle.validate();
 			displayingPuzzle.setUser(MainWindow.getInstance().getLoggedInUser());
 			displayingPuzzle.prepareForSave();
 			new PuzzleManager().save(displayingPuzzle);
 
 			dialog = new MessageBox(this.getShell(), SWT.ICON_INFORMATION | SWT.OK);
 			dialog.setText(MessageConstants.SAVE_SUCCESS);
 			dialog.setMessage(MessageConstants.SAVE_SUCCESS_MSG);
 
 			dialog.open();
 
 			MainWindow.getInstance().switchToWelcomeScreen();
 		}
 		catch (PuzzleValidationException e)
 		{
 			dialog = new MessageBox(this.getShell(), SWT.ICON_ERROR | SWT.OK);
 			dialog.setText(MessageConstants.SAVE_ERROR);
 			dialog.setMessage(e.getMessage());
 
 			dialog.open();
 		}
 	}
 
 	private void updatePuzzleTitle()
 	{
 		displayingPuzzle.setTitle(txtPuzzleName.getText());
 	}
 
 	private void updatePuzzleDescription()
 	{
 		displayingPuzzle.setDescription(txtDescription.getText());
 	}
 
 	private void updatePuzzleCategory()
 	{
 		displayingPuzzle.setCategory(Category.valueOf(cmbCategory.getText()));
 		lblCategoryFixedText.setText(cmbCategory.getText());
 	}
 
 	private void updatePuzzleDifficulty()
 	{
 		displayingPuzzle.setDifficulty(Difficulty.valueOf(cmbDificulty
 				.getText()));
 		lblDifficultyFixedText.setText(cmbDificulty.getText());
 	}
 
 	private void checkSolution()
 	{
 		String msg;
 		MessageBox dialog;
 
 		if (this.displayingPuzzle != null
 				&& this.displayingPuzzle.isCompleted())
 		{
 			msg = MessageConstants.PUZZLE_SOLVED;
 		}
 		else
 		{
 			msg = MessageConstants.PUZZLE_UNSOLVED;
 		}
 
 		dialog = new MessageBox(this.getShell(), SWT.ICON_QUESTION | SWT.OK);
 		dialog.setText(Constants.PUZZLE_SOLUTION);
 		dialog.setMessage(msg);
 
 		dialog.open();
 	}
 
 	@Override
 	protected void checkSubclass()
 	{
 		// Disable the check that prevents subclassing of SWT components
 	}
 }
