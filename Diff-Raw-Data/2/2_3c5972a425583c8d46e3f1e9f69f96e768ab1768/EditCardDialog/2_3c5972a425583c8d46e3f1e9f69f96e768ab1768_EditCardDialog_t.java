 package ru.terra.activitystore.gui.swt;
 
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Dialog;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.swt.widgets.MenuItem;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableColumn;
 import org.eclipse.swt.widgets.TableItem;
 import org.eclipse.swt.widgets.Text;
 
 import ru.terra.activitystore.db.entity.Card;
 import ru.terra.activitystore.db.entity.Cell;
 
 public class EditCardDialog extends Dialog
 {
 	private String cardName;
 	private String name;
 	private Card ret, card;
 	private Table cellsTable;
 	private Text cardNameInput;
 
 	public EditCardDialog(Shell arg0)
 	{
 		super(arg0, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
 	}
 
 	public Card open(Card card)
 	{
 		Shell shell = new Shell(getParent(), getStyle());
 		shell.setText(getText());
 		this.card = card;
 		createContents(shell);
		shell.pack();
 		shell.open();
 		Display display = getParent().getDisplay();
 		while (!shell.isDisposed())
 		{
 			if (!display.readAndDispatch())
 			{
 				display.sleep();
 			}
 		}
 		return ret;
 	}
 
 	private void createContents(final Shell shell)
 	{
 		shell.setLayout(new GridLayout(2, true));
 		Label label = new Label(shell, SWT.NONE);
 		label.setText("Новая карточка");
 		GridData data = new GridData();
 		data.horizontalSpan = 2;
 		label.setLayoutData(data);
 
 		cardNameInput = new Text(shell, SWT.BORDER);
 		if (name != null)
 			cardNameInput.setText(name);
 		data = new GridData(GridData.FILL_HORIZONTAL);
 		data.horizontalSpan = 2;
 		cardNameInput.setLayoutData(data);
 
 		cellsTable = new Table(shell,  SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION);
 		//cellsTable.setHeaderVisible(true);
 		cellsTable.setLinesVisible(true);
 //		TableColumn column = new TableColumn(cellsTable, SWT.NONE);
 //		column.setText("Ячейка");
 //		
 //		cellsTable.setHeaderVisible(true);
 		data = new GridData(GridData.FILL_HORIZONTAL);
 		data.horizontalSpan = 2;
 		cellsTable.setLayoutData(data);
 
 		if (card != null)
 		{
 			loadCard();
 		}
 		else
 		{
 			card = new Card();
 		}
 
 		Menu cellsMenu = new Menu(cellsTable);
 		MenuItem miDelete = new MenuItem(cellsMenu, SWT.POP_UP);
 		miDelete.setText("Удалить");
 		miDelete.addSelectionListener(new SelectionListener()
 		{
 
 			@Override
 			public void widgetSelected(SelectionEvent arg0)
 			{
 				TableItem selected = cellsTable.getSelection()[0];
 				if (selected != null)
 				{
 					Cell c = (Cell) selected.getData();
 					card.getCells().remove(c);
 					cellsTable.remove(cellsTable.getSelectionIndex());
 					cellsTable.setRedraw(true);
 				}
 			}
 
 			@Override
 			public void widgetDefaultSelected(SelectionEvent arg0)
 			{
 			}
 		});
 
 		MenuItem miAdd = new MenuItem(cellsMenu, SWT.POP_UP);
 		miAdd.setText("Добавить");
 		miAdd.addSelectionListener(new SelectionListener()
 		{
 
 			@Override
 			public void widgetSelected(SelectionEvent arg0)
 			{
 				Cell newCell = new EditCellDialog(shell).open(null);
 				if (newCell != null)
 				{
 					card.getCells().add(newCell);
 					TableItem newItem = new TableItem(cellsTable, SWT.NONE);
 					newCell.setCard(card);
 					newItem.setText(0, newCell.getComment());
 					newItem.setData(newCell);
 				}
 			}
 
 			@Override
 			public void widgetDefaultSelected(SelectionEvent arg0)
 			{
 			}
 		});
 
 		cellsTable.setMenu(cellsMenu);
 
 		Button ok = new Button(shell, SWT.PUSH);
 		ok.setText("OK");
 		data = new GridData(GridData.FILL_HORIZONTAL);
 		ok.setLayoutData(data);
 		ok.addSelectionListener(new SelectionAdapter()
 		{
 			public void widgetSelected(SelectionEvent event)
 			{
 				cardName = cardNameInput.getText();
 				card.setName(cardName);
 				ret = card;
 				shell.close();
 			}
 		});
 
 		Button cancel = new Button(shell, SWT.PUSH);
 		cancel.setText("Отмена");
 		data = new GridData(GridData.FILL_HORIZONTAL);
 		cancel.setLayoutData(data);
 		cancel.addSelectionListener(new SelectionAdapter()
 		{
 			public void widgetSelected(SelectionEvent event)
 			{
 				cardName = null;
 				card = null;
 				ret = card;
 				shell.close();
 			}
 		});
 
 		shell.setDefaultButton(ok);
 	}
 
 	private void loadCard()
 	{
 		cardNameInput.setText(card.getName());
 		cellsTable.setRedraw(false);
 		for (Cell c : card.getCells())
 		{
 			TableItem ti = new TableItem(cellsTable, SWT.NONE);
 			ti.setText(0, c.getComment());
 			ti.setData(c);
 			System.out.println("Loading cell to table " + c.getComment());
 		}
 		cellsTable.setRedraw(true);
 	}
 }
