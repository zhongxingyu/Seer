 package pg13.presentation;
 
 import java.util.ArrayList;
 
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.FormLayout;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.FormData;
 import org.eclipse.swt.layout.FormAttachment;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableColumn;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.jface.viewers.IStructuredContentProvider;
 import org.eclipse.jface.viewers.TableViewerColumn;
 import org.eclipse.jface.viewers.Viewer;
 
 import pg13.business.AuthorFilter;
 import pg13.business.CategoryFilter;
 import pg13.business.DifficultyFilter;
 import pg13.business.PuzzleTableDriver;
 import pg13.business.TitleFilter;
 import pg13.models.Category;
 import pg13.models.Difficulty;
 import pg13.models.Puzzle;
 import pg13.org.eclipse.wb.swt.SWTResourceManager;
 
 public class FindScreen extends Composite
 {
 	private static class ContentProvider implements IStructuredContentProvider
 	{
 		public Object[] getElements(Object newElements)
 		{
 			@SuppressWarnings("unchecked")
 			ArrayList<Puzzle> puzzles = (ArrayList<Puzzle>) newElements;
 			return puzzles.toArray();
 		}
 
 		public void dispose()
 		{
 		}
 
 		public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
 		{
 		}
 	}
 
 	private Text txtTitle;
 	private Text txtAuthor;
 	private Combo cmbCategory;
 	private Table table;
 	private TableViewer tableViewer;
 	private PuzzleTableDriver tableDriver;
 	private ArrayList<Puzzle> puzzleResults;
 
 	private Button btnPlaySelectedPuzzle;
 	private Button btnAllDifficulties;
 	private Button btnEasy;
 	private Button btnDifficult;
 	private Button btnMedium;
 	private Button btnAllPuzzles;
 	private Button btnFriendsPuzzles;
 	private Button btnMyPuzzles;
 	private Puzzle selectedPuzzle;
 	private Composite cmpPuzzleFilter;
 
 	private AuthorFilter specialAuthorFilter;
 
 	public FindScreen(Composite parent, int style)
 	{
 		super(parent, style);
 		setLayout(new FormLayout());
 
 		// separates the left information from the results table
 		Label separator = new Label(this, SWT.SEPARATOR | SWT.VERTICAL);
 		FormData fd_separator = new FormData();
 		fd_separator.bottom = new FormAttachment(100);
 		fd_separator.top = new FormAttachment(0);
 		fd_separator.left = new FormAttachment(0, 200);
 		separator.setLayoutData(fd_separator);
 
 		// "Find Puzzles to Play"
 		Label lblFindAPuzzle = new Label(this, SWT.NONE);
 		lblFindAPuzzle.setFont(SWTResourceManager.getFont("Segoe UI", 16,
 				SWT.NORMAL));
 		FormData fd_lblFindAPuzzle = new FormData();
 		fd_lblFindAPuzzle.top = new FormAttachment(0, 10);
 		fd_lblFindAPuzzle.left = new FormAttachment(0, 10);
 		lblFindAPuzzle.setLayoutData(fd_lblFindAPuzzle);
 		lblFindAPuzzle.setText(Constants.FIND_PUZZLES);
 
 		// composite that contains the radio buttons to filter puzzle search
 		cmpPuzzleFilter = new Composite(this, SWT.BORDER);
 		cmpPuzzleFilter.setBackground(SWTResourceManager
 				.getColor(SWT.COLOR_WHITE));
 		cmpPuzzleFilter.setLayout(new FormLayout());
 		FormData fd_cmpPuzzleFilter = new FormData();
 		fd_cmpPuzzleFilter.bottom = new FormAttachment(lblFindAPuzzle, 84,
 				SWT.BOTTOM);
 		fd_cmpPuzzleFilter.top = new FormAttachment(lblFindAPuzzle, 6);
 		fd_cmpPuzzleFilter.right = new FormAttachment(separator, -10);
 		fd_cmpPuzzleFilter.left = new FormAttachment(0, 10);
 		cmpPuzzleFilter.setLayoutData(fd_cmpPuzzleFilter);
 
 		btnAllPuzzles = new Button(cmpPuzzleFilter, SWT.RADIO);
 		btnAllPuzzles.setSelection(true);
 		FormData fd_btnAllPuzzles = new FormData();
 		fd_btnAllPuzzles.right = new FormAttachment(100, -6);
 		fd_btnAllPuzzles.top = new FormAttachment(0, 6);
 		fd_btnAllPuzzles.left = new FormAttachment(0, 6);
 		btnAllPuzzles.setLayoutData(fd_btnAllPuzzles);
 		btnAllPuzzles.setText(Constants.ALL_PUZZLES);
 		btnAllPuzzles.setBackground(SWTResourceManager
 				.getColor(SWT.COLOR_WHITE));
 
 		btnFriendsPuzzles = new Button(cmpPuzzleFilter, SWT.RADIO);
 		btnFriendsPuzzles.setEnabled(false);
 		FormData fd_btnFriendsPuzzles = new FormData();
 		fd_btnFriendsPuzzles.top = new FormAttachment(0, 28);
 		fd_btnFriendsPuzzles.right = new FormAttachment(100, -6);
 		fd_btnFriendsPuzzles.left = new FormAttachment(0, 6);
 		btnFriendsPuzzles.setLayoutData(fd_btnFriendsPuzzles);
 		btnFriendsPuzzles.setText(Constants.FRIENDS_PUZZLES);
 		btnFriendsPuzzles.setBackground(SWTResourceManager
 				.getColor(SWT.COLOR_WHITE));
 
 		btnMyPuzzles = new Button(cmpPuzzleFilter, SWT.RADIO);
 		FormData fd_btnMyPuzzles = new FormData();
 		fd_btnMyPuzzles.right = new FormAttachment(100, -6);
 		fd_btnMyPuzzles.top = new FormAttachment(0, 50);
 		fd_btnMyPuzzles.left = new FormAttachment(0, 6);
 		btnMyPuzzles.setLayoutData(fd_btnMyPuzzles);
 		btnMyPuzzles.setText(Constants.MY_PUZZLES);
 		btnMyPuzzles
 				.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
 
 		// composite that contains additional search filters for the properties
 		// of the puzzle
 		Composite cmpPuzzleSearch = new Composite(this, SWT.BORDER);
 		cmpPuzzleSearch.setBackground(SWTResourceManager
 				.getColor(SWT.COLOR_WHITE));
 		cmpPuzzleSearch.setLayout(new FormLayout());
 		FormData fd_cmpPuzzleSearch = new FormData();
 		fd_cmpPuzzleSearch.bottom = new FormAttachment(100, -40);
 		fd_cmpPuzzleSearch.right = new FormAttachment(separator, -10);
 		fd_cmpPuzzleSearch.top = new FormAttachment(cmpPuzzleFilter, 8);
 		fd_cmpPuzzleSearch.left = new FormAttachment(0, 10);
 		cmpPuzzleSearch.setLayoutData(fd_cmpPuzzleSearch);
 
 		// search by title
 		Label lblTitle = new Label(cmpPuzzleSearch, SWT.NONE);
 		lblTitle.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
 		FormData fd_lblTitle = new FormData();
 		fd_lblTitle.top = new FormAttachment(0, 6);
 		fd_lblTitle.left = new FormAttachment(0, 6);
 		lblTitle.setLayoutData(fd_lblTitle);
 		lblTitle.setText(Constants.TITLE);
 
 		txtTitle = new Text(cmpPuzzleSearch, SWT.BORDER);
 		FormData fd_txtTitle = new FormData();
 		fd_txtTitle.right = new FormAttachment(100, -6);
 		fd_txtTitle.top = new FormAttachment(lblTitle, 2);
 		fd_txtTitle.left = new FormAttachment(0, 6);
 		txtTitle.setLayoutData(fd_txtTitle);
 
 		// search by author
 		Label lblAuthor = new Label(cmpPuzzleSearch, SWT.NONE);
 		lblAuthor.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
 		FormData fd_lblAuthor = new FormData();
 		fd_lblAuthor.top = new FormAttachment(txtTitle, 6);
 		fd_lblAuthor.left = new FormAttachment(0, 6);
 		lblAuthor.setLayoutData(fd_lblAuthor);
 		lblAuthor.setText(Constants.AUTHOR);
 
 		txtAuthor = new Text(cmpPuzzleSearch, SWT.BORDER);
 		FormData fd_txtAuthor = new FormData();
 		fd_txtAuthor.top = new FormAttachment(lblAuthor, 2);
 		fd_txtAuthor.right = new FormAttachment(100, -6);
 		fd_txtAuthor.left = new FormAttachment(0, 6);
 		txtAuthor.setLayoutData(fd_txtAuthor);
 
 		// filter by category
 		Label lblCategory = new Label(cmpPuzzleSearch, SWT.NONE);
 		lblCategory.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
 		FormData fd_lblCategory = new FormData();
 		fd_lblCategory.top = new FormAttachment(txtAuthor, 6);
 		fd_lblCategory.left = new FormAttachment(0, 6);
 		lblCategory.setLayoutData(fd_lblCategory);
 		lblCategory.setText(Constants.CATEGORY + ":");
 
 		cmbCategory = new Combo(cmpPuzzleSearch, SWT.NONE);
 		ArrayList<String> categories = Category.valuesAsStrings();
 		categories.add(0, "All Categories");
 		cmbCategory.setItems(categories.toArray(new String[categories.size()]));
 		FormData fd_cmbCategory = new FormData();
 		fd_cmbCategory.top = new FormAttachment(lblCategory, 2);
 		fd_cmbCategory.right = new FormAttachment(100, -6);
 		fd_cmbCategory.left = new FormAttachment(0, 6);
 		cmbCategory.setLayoutData(fd_cmbCategory);
 		cmbCategory.select(0);
 
 		// filter by difficulty
 		Label lblDifficulty = new Label(cmpPuzzleSearch, SWT.NONE);
 		lblDifficulty.setBackground(SWTResourceManager
 				.getColor(SWT.COLOR_WHITE));
 		FormData fd_lblDifficulty = new FormData();
 		fd_lblDifficulty.top = new FormAttachment(cmbCategory, 6);
 		fd_lblDifficulty.left = new FormAttachment(lblTitle, 0, SWT.LEFT);
 		lblDifficulty.setLayoutData(fd_lblDifficulty);
 		lblDifficulty.setText(Constants.DIFFICULTY + ":");
 
 		btnAllDifficulties = new Button(cmpPuzzleSearch, SWT.CHECK);
 		FormData fd_btnAllDifficulties = new FormData();
 		fd_btnAllDifficulties.top = new FormAttachment(lblDifficulty, 6);
 		fd_btnAllDifficulties.left = new FormAttachment(0, 6);
 		btnAllDifficulties.setLayoutData(fd_btnAllDifficulties);
 		btnAllDifficulties.setText(Constants.ALL_DIFFICULTIES);
 		btnAllDifficulties.setBackground(SWTResourceManager
 				.getColor(SWT.COLOR_WHITE));
 
 		btnEasy = new Button(cmpPuzzleSearch, SWT.CHECK);
 		FormData fd_btnEasy = new FormData();
 		fd_btnEasy.left = new FormAttachment(0, 6);
 		btnEasy.setLayoutData(fd_btnEasy);
 		btnEasy.setText(Constants.EASY);
 		btnEasy.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
 		fd_btnEasy.top = new FormAttachment(btnAllDifficulties, 4);
 
 		btnDifficult = new Button(cmpPuzzleSearch, SWT.CHECK);
 		FormData fd_btnDifficult = new FormData();
 		fd_btnDifficult.left = new FormAttachment(lblTitle, 0, SWT.LEFT);
 		btnDifficult.setLayoutData(fd_btnDifficult);
 		btnDifficult.setText(Constants.HARD);
 		btnDifficult
 				.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
 
 		btnMedium = new Button(cmpPuzzleSearch, SWT.CHECK);
 		fd_btnDifficult.top = new FormAttachment(btnMedium, 6);
 		FormData fd_btnAverage = new FormData();
 		fd_btnAverage.top = new FormAttachment(btnEasy, 4);
 		fd_btnAverage.left = new FormAttachment(0, 6);
 		btnMedium.setLayoutData(fd_btnAverage);
 		btnMedium.setText(Constants.MEDIUM);
 		btnMedium.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
 
 		// play the selected puzzle!
 		btnPlaySelectedPuzzle = new Button(this, SWT.NONE);
 		btnPlaySelectedPuzzle.addSelectionListener(new SelectionAdapter()
 		{
 			@Override
 			public void widgetSelected(SelectionEvent e)
 			{
 				playPuzzlePressed();
 			}
 
 			@Override
 			public void widgetDefaultSelected(SelectionEvent e)
 			{
 				playPuzzlePressed();
 			}
 		});
 
 		FormData fd_btnPlaySelectedPuzzle = new FormData();
 		fd_btnPlaySelectedPuzzle.right = new FormAttachment(separator, -10);
 		fd_btnPlaySelectedPuzzle.bottom = new FormAttachment(100, -10);
 		fd_btnPlaySelectedPuzzle.top = new FormAttachment(100, -36);
 		fd_btnPlaySelectedPuzzle.left = new FormAttachment(lblFindAPuzzle, 0,
 				SWT.LEFT);
 		btnPlaySelectedPuzzle.setLayoutData(fd_btnPlaySelectedPuzzle);
 		btnPlaySelectedPuzzle.setText(MessageConstants.SOLVE_SELECTED);
 
 		// create a table viewer to show puzzles
 		this.tableViewer = new TableViewer(this, SWT.BORDER
 				| SWT.FULL_SELECTION);
 		table = tableViewer.getTable();
 		table.addSelectionListener(new SelectionAdapter()
 		{
 			@Override
 			public void widgetSelected(SelectionEvent e)
 			{
 				updatePlayButtonStatus();
 			}
 		});
 		FormData fd_table = new FormData();
 		fd_table.bottom = new FormAttachment(100, -10);
 		fd_table.right = new FormAttachment(100, -10);
 		fd_table.top = new FormAttachment(separator, 10, SWT.TOP);
 		fd_table.left = new FormAttachment(separator, 6);
 		table.setLayoutData(fd_table);
 		table.setLinesVisible(true);
 		table.setHeaderVisible(true);
 		table.setBackground(SWTResourceManager
 				.getColor(SWT.COLOR_LIST_BACKGROUND));
 
 		// add business layer table driver
 		this.puzzleResults = new ArrayList<Puzzle>();
 		this.tableDriver = new PuzzleTableDriver(this.puzzleResults);
 
 		// make the columns
 		this.createColumns(this, this.tableViewer);
 
 		// add a content provider
 		this.tableViewer.setContentProvider(new ContentProvider());
 		this.tableViewer.setInput(this.puzzleResults);
 
 		// add a title filter to the table
 		final TitleFilter titleFilter = new TitleFilter();
 		this.txtTitle.addModifyListener(new ModifyListener()
 		{
 			@Override
 			public void modifyText(ModifyEvent e)
 			{
 				Text source = (Text) e.getSource();
 				titleFilter.setSearchString(source.getText());
 				tableViewer.refresh();
 			}
 		});
 		this.tableViewer.addFilter(titleFilter);
 
 		// add an author filter to the table
 		final AuthorFilter authorFilter = new AuthorFilter();
 		this.txtAuthor.addModifyListener(new ModifyListener()
 		{
 			@Override
 			public void modifyText(ModifyEvent e)
 			{
 				Text source = (Text) e.getSource();
 				authorFilter.setSearchString(source.getText());
 				tableViewer.refresh();
 			}
 		});
 		this.tableViewer.addFilter(authorFilter);
 
 		// add a special author filter to the table
 		specialAuthorFilter = new AuthorFilter();
 		btnMyPuzzles.addSelectionListener(new SelectionAdapter()
 		{
 			@Override
 			public void widgetSelected(SelectionEvent e)
 			{
 				filterByMyPuzzles();
 			}
 		});
 		btnAllPuzzles.addSelectionListener(new SelectionAdapter()
 		{
 			@Override
 			public void widgetSelected(SelectionEvent e)
 			{
 				specialAuthorFilter.setSearchString("");
 				tableViewer.refresh();
 			}
 		});
 		this.tableViewer.addFilter(specialAuthorFilter);
 
 		// add a category filter to the table
 		final CategoryFilter categoryFilter = new CategoryFilter();
 		this.cmbCategory.addSelectionListener(new SelectionAdapter()
 		{
 
 			@Override
 			public void widgetSelected(SelectionEvent e)
 			{
 				Combo source = (Combo) e.getSource();
 				int index = source.getSelectionIndex();
 				String selection = source.getItem(index);
 
 				Category category = null;
 				if (!selection.equalsIgnoreCase("All Categories"))
 				{
 					category = Category.valueOf(selection);
 				}
 				categoryFilter.setSearchValue(category);
 				tableViewer.refresh();
 			}
 		});
 		this.tableViewer.addFilter(categoryFilter);
 
 		// add filters to the difficulty
 		final DifficultyFilter difficultyFilter = new DifficultyFilter();
 		this.tableViewer.addFilter(difficultyFilter);
 		SelectionAdapter SLToggleAll = new SelectionAdapter()
 		{
 			@Override
 			public void widgetSelected(SelectionEvent e)
 			{
 				Button source = (Button) e.getSource();
 				boolean checked = source.getSelection();
 				for (Difficulty diff : Difficulty.values())
 				{
 					if (checked)
 					{
 						difficultyFilter.addValue(diff);
 					}
 					else
 					{
 						difficultyFilter.removeValue(diff);
 					}
 				}
 				tableViewer.refresh();
 			}
 		};
 		SelectionAdapter SLToggleOne = new SelectionAdapter()
 		{
 			@Override
 			public void widgetSelected(SelectionEvent e)
 			{
 				Button source = (Button) e.getSource();
 				boolean checked = source.getSelection();
 				Difficulty diff = Difficulty.valueOf(source.getText());
 				if (checked)
 				{
 					difficultyFilter.addValue(diff);
 				}
 				else
 				{
 					difficultyFilter.removeValue(diff);
 				}
 				tableViewer.refresh();
 			}
 		};
 		this.btnAllDifficulties.addSelectionListener(SLToggleAll);
 		this.btnEasy.addSelectionListener(SLToggleOne);
 		this.btnMedium.addSelectionListener(SLToggleOne);
 		this.btnDifficult.addSelectionListener(SLToggleOne);
 
 		this.tableViewer
 				.addSelectionChangedListener(new ISelectionChangedListener()
 				{
 					public void selectionChanged(
 							final SelectionChangedEvent event)
 					{
 						IStructuredSelection selection = (IStructuredSelection) event
 								.getSelection();
 						if (selection.size() == 1)
 						{
 							selectedPuzzle = (Puzzle) selection
 									.getFirstElement();
 						}
 						else
 						{
 							selectedPuzzle = null;
 						}
 					}
 				});
 	}
 
 	private void createColumns(final Composite parent, final TableViewer viewer)
 	{
 		String[] titles = { "Title", "Author", "Category", "Difficulty" };
 		int[] bounds = { 100, 100, 100, 100 };
 
 		// create each column
 		for (int i = 0; i < titles.length; i++)
 		{
 			TableViewerColumn col = createTableViewerColumn(titles[i],
 					bounds[i], i);
 			col.setLabelProvider(this.tableDriver
 					.getColumnLabelProvider(titles[i]));
 		}
 	}
 
 	private TableViewerColumn createTableViewerColumn(String title, int width,
 			final int colNumber)
 	{
 		final TableViewerColumn viewerColumn = new TableViewerColumn(
 				this.tableViewer, SWT.NONE);
 		final TableColumn column = viewerColumn.getColumn();
 		column.setText(title);
 		column.setWidth(width);
 		column.setResizable(true);
 		return viewerColumn;
 	}
 
 	@Override
 	protected void checkSubclass()
 	{
 		// Disable the check that prevents subclassing of SWT components
 	}
 
 	public void onLoad()
 	{
 		this.tableDriver.refresh();
 		this.tableViewer.setInput(this.puzzleResults);
 		updatePlayButtonStatus();
 	}
 
 	private void updatePlayButtonStatus()
 	{
 		int selected;
 
 		// get the selected puzzle from the table
 		selected = table.getSelectionIndex();
 
 		if (selected < 0)
 		{
 			btnPlaySelectedPuzzle.setEnabled(false);
 		}
 		else
 		{
 			btnPlaySelectedPuzzle.setEnabled(true);
 		}
 	}
 
 	private void playPuzzlePressed()
 	{
 		if (this.selectedPuzzle != null)
 		{
 			MainWindow.getInstance().playPuzzle(this.selectedPuzzle);
 		}
 	}
 
 	public void filterByMyPuzzles()
 	{
 		String loggedInUsername = MainWindow.getInstance().getLoggedInUser()
 				.getName();
 		specialAuthorFilter.setAbsoluteSearchString(loggedInUsername);
 		tableViewer.refresh();
 	}
 
 	public void selectMyPuzzles()
 	{
 		this.btnMyPuzzles.setSelection(true);
 		this.btnAllPuzzles.setSelection(false);
 		this.btnFriendsPuzzles.setSelection(false);
 	}
 }
