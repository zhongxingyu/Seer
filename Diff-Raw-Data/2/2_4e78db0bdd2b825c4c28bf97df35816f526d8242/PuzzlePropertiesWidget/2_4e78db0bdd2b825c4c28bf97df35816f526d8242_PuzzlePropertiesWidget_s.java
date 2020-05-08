 package pg13.presentation;
 
 import java.util.ArrayList;
 
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.wb.swt.SWTResourceManager;
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
 
 import pg13.business.create.PuzzleCreator;
 import pg13.models.Cryptogram;
 import pg13.models.Puzzle;
 
 public class PuzzlePropertiesWidget extends Composite
 {
 	boolean editMode;						// can we edit the properties of the puzzle?
 	private Text txtPuzzleName;
 	private Text txtDescription;
 	private Label lblCategory;
 	private Label lblDifficulty;
 	private Combo cmbDificulty;
 	private Combo cmbCategory;
 	private Button btnSavePuzzle;
 	/**
 	 * Creates and populates the properties widget.
 	 * @author Eric
 	 * @param parent
 	 * @param style
 	 * @date May 29 2013
 	 */
 	public PuzzlePropertiesWidget(Composite parent, int style, Cryptogram displayingPuzzle, boolean editMode)
 	{
 		super(parent, style);

		this.displayingPuzzle = displayingPuzzle;
 		final Puzzle puzzle = displayingPuzzle;
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
 				puzzle.setTitle(txtPuzzleName.getText());
 			}
 		});
 
 		// puzzle description label
 		Label lblDescription = new Label(this, SWT.NONE);
 		FormData fd_lblDescription = new FormData();
 		fd_lblDescription.top = new FormAttachment(txtPuzzleName, 10);
 		fd_lblDescription.left = new FormAttachment(0, 10);
 		lblDescription.setLayoutData(fd_lblDescription);
 		lblDescription.setText("Description");
 
 
 		// puzzle description text field
 		txtDescription = new Text(this, SWT.BORDER | SWT.WRAP);
 		FormData fd_txtDescription = new FormData();
 		fd_txtDescription.bottom = new FormAttachment(lblDescription, 134, SWT.BOTTOM);
 		fd_txtDescription.right = new FormAttachment(100, -10);
 		fd_txtDescription.top = new FormAttachment(lblDescription, 4);
 		fd_txtDescription.left = new FormAttachment(0, 10);
 		txtDescription.setLayoutData(fd_txtDescription);
 		txtDescription.addModifyListener(new ModifyListener()
 		{
 			@Override
 			public void modifyText(ModifyEvent e)
 			{
 				puzzle.setDescription(txtDescription.getText());
 			}
 		});
 
 		// category label
 		lblCategory = new Label(this, SWT.NONE);
 		FormData fd_lblCategory = new FormData();
 		fd_lblCategory.left = new FormAttachment(0, 10);
 		fd_lblCategory.top = new FormAttachment(txtDescription, 10);
 		lblCategory.setLayoutData(fd_lblCategory);
 		lblCategory.setText("Category");
 
 		// category selection box
 		cmbCategory = new Combo(this, SWT.NONE);
 		cmbCategory.setItems(new String[] {"", "Animals", "Biology", "Computers", "Games", "General Trivia", "Geography", "History", "Miscellaneous", "Politics", "Science", "Space", "Sports"});
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
 				puzzle.setCategory(cmbCategory.getText());
 			}
 		});
 
 
 		// difficulty label
 		lblDifficulty = new Label(this, SWT.NONE);
 		FormData fd_lblDifficulty = new FormData();
 		fd_lblDifficulty.top = new FormAttachment(cmbCategory, 10);
 		fd_lblDifficulty.left = new FormAttachment(0, 10);
 		lblDifficulty.setLayoutData(fd_lblDifficulty);
 		lblDifficulty.setText("Difficulty");
 
 		// difficulty selection box
 		cmbDificulty = new Combo(this, SWT.NONE);
 		cmbDificulty.setItems(new String[] {"", "Easy", "Average", "Difficult", "Expert"});
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
 				puzzle.setDifficulty(cmbDificulty.getText());
 			}
 		});
 
 
 		// save puzzle button
 		this.btnSavePuzzle = new Button(this, SWT.NONE);
 		FormData fd_btnSavePuzzle = new FormData();
 		fd_btnSavePuzzle.top = new FormAttachment(100, -40);
 		fd_btnSavePuzzle.bottom = new FormAttachment(100, -10);
 		fd_btnSavePuzzle.left = new FormAttachment(50, -70);
 		fd_btnSavePuzzle.right = new FormAttachment(50, 70);
 		btnSavePuzzle.setLayoutData(fd_btnSavePuzzle);
 		btnSavePuzzle.setText("Save this Puzzle");
 		btnSavePuzzle.addSelectionListener(new SelectionListener()
 		{
 
 			@Override
 			public void widgetSelected(SelectionEvent e)
 			{
 				new PuzzleCreator().save(puzzle);
 			}
 
 			@Override
 			public void widgetDefaultSelected(SelectionEvent e)
 			{
 				new PuzzleCreator().save(puzzle);
 			}
 
 		});
 		this.setEditMode(editMode);
 	}
 
 	private void setEditMode(boolean editMode){
 		this.editMode = editMode;
 		this.cmbCategory.setEnabled(this.editMode);
 		this.cmbDificulty.setEnabled(this.editMode);
 		this.txtDescription.setEnabled(this.editMode);
 		this.txtPuzzleName.setEnabled(this.editMode);
 		this.btnSavePuzzle.setVisible(this.editMode);
 	}
 
 	@Override
 	protected void checkSubclass()
 	{
 		// Disable the check that prevents subclassing of SWT components
 	}
 }
