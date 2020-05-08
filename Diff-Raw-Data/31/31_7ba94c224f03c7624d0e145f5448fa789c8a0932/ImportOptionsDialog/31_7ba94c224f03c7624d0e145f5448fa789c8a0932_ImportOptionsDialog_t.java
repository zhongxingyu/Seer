 /*
  *
  *  JMoney - A Personal Finance Manager
  *  Copyright (c) 2006 Nigel Westbury <westbury@users.sourceforge.net>
  *
  *
  *  This program is free software; you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation; either version 2 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program; if not, write to the Free Software
  *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
  *
  */
 
 package net.sf.jmoney.reconciliation.reconcilePage;
 
 import java.net.URL;
 import java.util.Iterator;
 import java.util.regex.Pattern;
 import java.util.regex.PatternSyntaxException;
 
 import net.sf.jmoney.fields.AccountControl;
 import net.sf.jmoney.isolation.TransactionManager;
 import net.sf.jmoney.model2.Account;
 import net.sf.jmoney.model2.AccountCellEditor;
 import net.sf.jmoney.model2.ExtendableObject;
 import net.sf.jmoney.model2.IncomeExpenseAccount;
 import net.sf.jmoney.model2.ObjectCollection;
import net.sf.jmoney.model2.ReferenceViolationException;
 import net.sf.jmoney.model2.ScalarPropertyAccessor;
 import net.sf.jmoney.reconciliation.MemoPattern;
 import net.sf.jmoney.reconciliation.MemoPatternInfo;
 import net.sf.jmoney.reconciliation.ReconciliationAccount;
 import net.sf.jmoney.reconciliation.ReconciliationAccountInfo;
 import net.sf.jmoney.reconciliation.ReconciliationPlugin;
 
 import org.eclipse.jface.dialogs.Dialog;
 import org.eclipse.jface.dialogs.DialogMessageArea;
 import org.eclipse.jface.dialogs.IDialogConstants;
 import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.jface.viewers.CellEditor;
 import org.eclipse.jface.viewers.CellLabelProvider;
 import org.eclipse.jface.viewers.ColumnLabelProvider;
 import org.eclipse.jface.viewers.ColumnViewerEditor;
 import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
 import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
 import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
 import org.eclipse.jface.viewers.EditingSupport;
 import org.eclipse.jface.viewers.FocusCellOwnerDrawHighlighter;
 import org.eclipse.jface.viewers.IStructuredContentProvider;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.jface.viewers.TableViewerColumn;
 import org.eclipse.jface.viewers.TableViewerEditor;
 import org.eclipse.jface.viewers.TableViewerFocusCellManager;
 import org.eclipse.jface.viewers.TextCellEditor;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.jface.viewers.ViewerCell;
 import org.eclipse.jface.viewers.ViewerSorter;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.StackLayout;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Table;
 
 /**
  * An input dialog that allows the user to configure the methods for importing statement data
  * for a particular account.
  * 
  * @author Nigel Westbury
  */
 public class ImportOptionsDialog extends Dialog {
 	
 	private TransactionManager transactionManager;
 	
 	/**
 	 * The account for which we are configuring, which is in our own transaction.
 	 */
 	ReconciliationAccount account;
 
 	private DialogMessageArea messageArea;
 
 	Button reconcilableButton;
 
 	TableViewer viewer;
 
 	AccountControl<IncomeExpenseAccount> defaultAccountControl;
 
 	Image errorImage;
 	
 	/**
 	 * When adding new patterns, we add to the end by default.
 	 * We must set an index that is more than all prior ordering
 	 * indexes.  This field contains the lowest integer that is
 	 * more than all existing values (or 0 if no patterns
 	 * currently exist). 
 	 */
 	int nextOrderingIndex;
 
 	/**
 	 * Creates an input dialog with OK and Cancel buttons. Note that the dialog
 	 * will have no visual representation (no widgets) until it is told to open.
 	 * <p>
 	 * Note that the <code>open</code> method blocks for input dialogs.
 	 * </p>
 	 * 
 	 * @param parentShell
 	 *            the parent shell
 	 */
 	public ImportOptionsDialog(Shell parentShell, ReconciliationAccount account) {
 		super(parentShell);
 		/*
 		 * All changes within this dialog are made within a transaction, so canceling
 		 * is trivial (the transaction is simply not committed).
 		 */
 		transactionManager = new TransactionManager(account.getDataManager());
 		ExtendableObject accountInTransaction = transactionManager.getCopyInTransaction(account.getBaseObject()); 
 		this.account = accountInTransaction.getExtension(ReconciliationAccountInfo.getPropertySet(), true);
 
 		// Load the error indicator
 		URL installURL = ReconciliationPlugin.getDefault().getBundle().getEntry("/icons/error.gif");
 		errorImage = ImageDescriptor.createFromURL(installURL).createImage();
 
 		// Find an ordering index that is greater than all existing ordering indexes,
 		// so new patterns can be added after all others.
 		nextOrderingIndex = 0;
 		for (MemoPattern pattern: account.getPatternCollection()) {
 			if (nextOrderingIndex <= pattern.getOrderingIndex()) {
 				nextOrderingIndex = pattern.getOrderingIndex() + 1;
 			}
 		}
 	}
 
 	@Override
 	protected void buttonPressed(int buttonId) {
 		if (buttonId == IDialogConstants.OK_ID) {
 			// All edits are transferred to the model as they are made,
 			// so we just need to commit them.
 			transactionManager.commit("Change Import Options");
 		}
 		super.buttonPressed(buttonId);
 	}
 
 	@Override
 	protected void configureShell(Shell shell) {
 		super.configureShell(shell);
 		shell.setText("Import Options for " + account.getName());
 	}
 	
 	@Override
 	public boolean close() {
 		boolean closed = super.close();
 		
 		// Dispose the image
 		if (closed) {
 			errorImage.dispose();
 		}
 		
 		return closed;
 	}
 	
 	@Override
 	protected void createButtonsForButtonBar(Composite parent) {
 		// create OK and Cancel buttons by default
 		createButton(parent, IDialogConstants.OK_ID,
 				IDialogConstants.OK_LABEL, true);
 		createButton(parent, IDialogConstants.CANCEL_ID,
 				IDialogConstants.CANCEL_LABEL, false);
 	}
 
 	@Override
 	protected Control createDialogArea(Composite parent) {
 		Composite composite = (Composite) super.createDialogArea(parent);
 		composite.setLayout(new GridLayout(1, false));
 
 		// Message label
 		messageArea = new DialogMessageArea();
 		messageArea.createContents(composite);
 		
 		// Ensure the message area is shown and fills the space
 		messageArea.setTitleLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
 		messageArea.setMessageLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
 
 		messageArea.showTitle("Options for Statement Import", null);
 		/*
 		 * It should not be possible for there to be errors when the dialog box is first
 		 * opened, because we don't allow the user to save the data when there are errors.
 		 * However, just in case, we ensure that any errors are shown. 
 		 */
 		updateErrorMessage();
 		
 		Label label = new Label(composite, SWT.WRAP);
 		label.setText("JMoney allows you to import bank account statements from the bank's servers. " +
 				"Before these records can be imported into JMoney, you must specify categories that are to be assigned to each entry " +
 				"because a requirement of JMoney is that all entries have an account or category assigned. " +
 				"Categories can be assigned based on regex pattern matching.");
 
 		GridData messageData = new GridData();
 		Rectangle rect = getShell().getMonitor().getClientArea();
 		messageData.widthHint = rect.width/2;
 		label.setLayoutData(messageData);
 
 		reconcilableButton = new Button(composite, SWT.CHECK);
 		reconcilableButton.setText("Statements can be imported?");
 
 		final Composite stackContainer = new Composite(composite, 0);
 
 		final StackLayout stackLayout = new StackLayout();
 		stackContainer.setLayout(stackLayout);
 
 		GridData containerData = new GridData(SWT.FILL, SWT.FILL, true, true);
 		containerData.grabExcessHorizontalSpace = true;
 		containerData.grabExcessVerticalSpace = true;
 		stackContainer.setLayoutData(containerData);
 
 		// Create the control containing the controls to be shown when 'is reconcilable'
 		final Control whenIsReconcilableControl = createCategoryControls(stackContainer);
 
 		reconcilableButton.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				boolean isReconcilable = reconcilableButton.getSelection();
 				account.setReconcilable(isReconcilable);
 
 				if (reconcilableButton.getSelection()) {
 					stackLayout.topControl = whenIsReconcilableControl;
 				} else {
 					stackLayout.topControl = null;
 				}
 				stackContainer.layout(false);
 
 				updateErrorMessage();
 			}
 		});
 
 		reconcilableButton.setSelection(account.isReconcilable());
 		if (account.isReconcilable()) {
 			stackLayout.topControl = whenIsReconcilableControl;
 			defaultAccountControl.setAccount(account.getDefaultCategory());
 		}
 
 		defaultAccountControl.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				IncomeExpenseAccount defaultCategory = defaultAccountControl.getAccount();
 				account.setDefaultCategory(defaultCategory);
 				updateErrorMessage();
 			}
 		});
 		applyDialogFont(composite);
 		return composite;
 	}
 
 	private Control createCategoryControls(Composite parent) {
 		Composite composite = new Composite(parent, SWT.NONE);
 		composite.setLayout(new GridLayout(2, false));
 
 		// Create the table of patterns
 		Control patternMatchingTableControl = createPatternMatchingTableControl(composite);
 		GridData tableData = new GridData(SWT.FILL, SWT.FILL, true, true);
 		tableData.horizontalSpan = 2;
 		tableData.grabExcessHorizontalSpace = true;
 		tableData.grabExcessVerticalSpace = false;  //?????
 		patternMatchingTableControl.setLayoutData(tableData);
 
 		// The default category, if no rule matches
 
 		new Label(composite, SWT.NONE).setText("Default category:");
 		defaultAccountControl = new AccountControl<IncomeExpenseAccount>(composite, account.getSession(), IncomeExpenseAccount.class);
 		GridData accountData = new GridData();
 		accountData.widthHint = 200;
 		defaultAccountControl.setLayoutData(accountData);
 
 		return composite;
 	}
 
 	private Control createPatternMatchingTableControl(Composite parent) {
 		Composite composite = new Composite(parent, SWT.NONE);
 		composite.setLayout(new GridLayout(2, false));
 
 		// Create the table viewer to display the pattern matching rules
 		viewer = new TableViewer(composite, SWT.BORDER | SWT.FULL_SELECTION);
 
 		// Set up the table
 		final Table table = viewer.getTable();
 		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
 		
 		TableViewerFocusCellManager focusCellManager = new TableViewerFocusCellManager(viewer, new FocusCellOwnerDrawHighlighter(viewer));
 		ColumnViewerEditorActivationStrategy actSupport = new ColumnViewerEditorActivationStrategy(viewer) {
 			@Override
 			protected boolean isEditorActivationEvent(
 					ColumnViewerEditorActivationEvent event) {
 				return event.eventType == ColumnViewerEditorActivationEvent.TRAVERSAL
 						|| event.eventType == ColumnViewerEditorActivationEvent.MOUSE_CLICK_SELECTION
 						|| (event.eventType == ColumnViewerEditorActivationEvent.KEY_PRESSED && event.keyCode == SWT.CR)
 						|| event.eventType == ColumnViewerEditorActivationEvent.PROGRAMMATIC;
 			}
 		};
 
 		TableViewerEditor.create(viewer, focusCellManager, actSupport, ColumnViewerEditor.TABBING_HORIZONTAL
 				| ColumnViewerEditor.TABBING_MOVE_TO_ROW_NEIGHBOR
 				| ColumnViewerEditor.TABBING_VERTICAL 
 				| ColumnViewerEditor.KEYBOARD_ACTIVATION);
 
 		// Set the content and label providers
 		viewer.setContentProvider(new PatternContentProvider());
 		viewer.setSorter(new PatternSorter());
 
 		ColumnViewerToolTipSupport.enableFor(viewer);
 
 		// Add the columns
 		TableViewerColumn column1 = new TableViewerColumn(viewer, SWT.LEFT);
 		column1.getColumn().setWidth(40);
 		column1.getColumn().setText("");
 		column1.setLabelProvider(new CellLabelProvider() {
 			@Override
 			public void update(ViewerCell cell) {
 				MemoPattern pattern = (MemoPattern)cell.getElement();
 				if (isMemoPatternValid(pattern) != null) {
 					cell.setImage(errorImage);
 				} else {
 					cell.setImage(null);
 				}
 			}
 
 			@Override
 			public String getToolTipText(Object element) {
 				MemoPattern pattern = (MemoPattern)element;
 				return isMemoPatternValid(pattern);
 			}
 		});
 
 		column1.setEditingSupport(new EditingSupport(viewer) {
 			@Override
 			protected boolean canEdit(Object element) {
 				return false;
 			}
 
 			@Override
 			protected CellEditor getCellEditor(Object element) {
 				return null;
 			}
 
 			@Override
 			protected Object getValue(Object element) {
 				return null;
 			}
 
 			@Override
 			protected void setValue(Object element, Object value) {
 			}
 		});
 
 		addColumn(MemoPatternInfo.getPatternAccessor(), "<html>The pattern is a Java regular expression that is matched against the memo in the downloadable file.<br>For each record from the bank, the first row in this table with a matching pattern is used.</html>");
 		addColumn(MemoPatternInfo.getCheckAccessor(), "The value to be put in the check field.  The values in this table may contain {0}, [1} etc. where the number matches the group number in the Java regular expression.");
 		addColumn(MemoPatternInfo.getMemoAccessor(), "The value to be put in the memo field.  The values in this table may contain {0}, [1} etc. where the number matches the group number in the Java regular expression.");
 		addColumn(MemoPatternInfo.getAccountAccessor(), "The account to be used for entries that match this pattern.");
 		addColumn(MemoPatternInfo.getDescriptionAccessor(), "The value to be put in the description field.  The values in this table may contain {0}, [1} etc. where the number matches the group number in the Java regular expression.");
 
 		/*
 		 * Set the account as the input object that contains the list of pattern
 		 * matching rules.
 		 */
 		viewer.setInput(account);
 
 		// Pack the columns
 		for (int i = 0, n = table.getColumnCount(); i < n; i++) {
 			table.getColumn(i).pack();
 		}
 
 		// Turn on the header and the lines
 		table.setHeaderVisible(true);
 		table.setLinesVisible(true);
 
 		// Create the button area
 		Control buttonAreaControl = createButtonArea(composite);
 		buttonAreaControl.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, true));
 
 		return composite;
 	}
 
 	/**
 	 * 
 	 * @param pattern
 	 * @return null if the pattern is valid, or a string describing the error
 	 * 		if the pattern is not valid 
 	 */
 	protected String isMemoPatternValid(MemoPattern pattern) {
 		if (pattern.getPattern() == null) {
 			return "No regex pattern has been entered"; 
 		} 
 
 		try {
 			Pattern.compile(pattern.getPattern());
 		} catch (PatternSyntaxException e) {
 			return "The regex pattern is not valid: " + e.getDescription();
 		}
 		
 		if (pattern.getAccount() == null) {
 			return "No account has been entered";
 		}
 
 		return null;
 	}
 
 	private void addColumn(final ScalarPropertyAccessor<?> propertyAccessor, String tooltip) {
 		TableViewerColumn column = new TableViewerColumn(viewer, SWT.LEFT);
 		column.getColumn().setWidth(propertyAccessor.getMinimumWidth());
 		column.getColumn().setText(propertyAccessor.getDisplayName());
 		column.getColumn().setToolTipText(tooltip);
 
 		column.setLabelProvider(new ColumnLabelProvider() {
 			@Override
 			public String getText(Object element) {
 				MemoPattern pattern = (MemoPattern)element;
 				return propertyAccessor.formatValueForTable(pattern);
 			}
 
 		});
 		column.setEditingSupport(new EditingSupport(viewer) {
 			@Override
 			protected boolean canEdit(Object element) {
 				return true;
 			}
 
 			@Override
 			protected CellEditor getCellEditor(Object element) {
 				if (propertyAccessor == MemoPatternInfo.getAccountAccessor()) {
 					return new AccountCellEditor<Account>(viewer.getTable(), account.getSession(), Account.class);
 				} else {
 					return new TextCellEditor(viewer.getTable());
 				}
 			}
 
 			@Override
 			protected Object getValue(Object element) {
 				// The text cell editor requires that null is never returned
 				// by this method.
 				MemoPattern pattern = (MemoPattern)element;
 				Object value = pattern.getPropertyValue(propertyAccessor);
 				if (value == null && propertyAccessor.getClassOfValueObject() == String.class) {
 					value = "";
 				}
 				return value;
 			}
 
 			@Override
 			protected void setValue(Object element, Object value) {
 				MemoPattern pattern = (MemoPattern)element;
 				setValue(pattern, propertyAccessor, value);
 				viewer.update(element, null);
 				updateErrorMessage();
 			}
 
 			private <V> void setValue(MemoPattern pattern, ScalarPropertyAccessor<V> property, Object value) {
 				V typedValue = property.getClassOfValueObject().cast(value);
 				pattern.setPropertyValue(property, typedValue);
 			}
 		});
 	}
 
 	private Composite createButtonArea(Composite parent) {
 		Composite container = new Composite(parent, SWT.NONE);
 		GridLayout layout = new GridLayout();
 		layout.marginWidth = 0;
 		layout.marginHeight = 30;
 		container.setLayout(layout);
 
 		Button button;
 
 		button = new Button(container, SWT.PUSH);
 		button.setText("Add Row");
 		button.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
 		button.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				ObjectCollection<MemoPattern> patterns = account.getPatternCollection();
 				MemoPattern newPattern = patterns.createNewElement(MemoPatternInfo.getPropertySet());
 
 				newPattern.setOrderingIndex(nextOrderingIndex++);
 
 				/*
 				 * Add the new pattern to the end of the table.
 				 */
 				viewer.add(newPattern);
 			}
 		});
 
 		button = new Button(container, SWT.PUSH);
 		button.setText("Remove Row");
 		button.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
 		button.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				IStructuredSelection ssel = (IStructuredSelection)viewer.getSelection();
 				if (ssel.size() > 0) {
					try {
						ObjectCollection<MemoPattern> patterns = account.getPatternCollection();
						for (Iterator<?> iter = ssel.iterator(); iter.hasNext(); ) {
							MemoPattern pattern = (MemoPattern) iter.next();
							patterns.deleteElement(pattern);
						}
 
						/*
						 * We have deleted patterns but remaining patterns are
						 * not affected so labels for the remaining patterns do not
						 * need updating.
						 */
						viewer.refresh(false);
					} catch (ReferenceViolationException e1) {
						MessageDialog.openError(getShell(), "Pattern in Use", "The pattern cannot be removed because it is in use else where.  " + e1.getLocalizedMessage() + "  The object referencing is " + e1.getPropertySet().getObjectDescription());
					}
 				}		
 			}
 		});
 
 		button = new Button(container, SWT.PUSH);
 		button.setText("Up");
 		button.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
 		button.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				IStructuredSelection ssel = (IStructuredSelection)viewer.getSelection();
 				if (ssel.size() == 1) {
 					MemoPattern thisPattern = (MemoPattern) ssel.getFirstElement();
 					
 					// Find the previous MemoPattern in the order.
 					MemoPattern abovePattern = null;
 					ObjectCollection<MemoPattern> patterns = account.getPatternCollection();
 					for (MemoPattern pattern: patterns) {
 						if (pattern.getOrderingIndex() < thisPattern.getOrderingIndex()) {
 							if (abovePattern == null || pattern.getOrderingIndex() > abovePattern.getOrderingIndex()) {
 								abovePattern = pattern;
 							}
 						}
 					}
 
 					if (abovePattern != null) {
 						swapOrderOfPatterns(thisPattern, abovePattern);
 					}
 
 					/*
 					 * The patterns are re-ordered but the labels are not
 					 * affected so do not request a refresh of the labels.
 					 */
 					viewer.refresh(false);
 				}		
 			}
 		});
 
 		button = new Button(container, SWT.PUSH);
 		button.setText("Down");
 		button.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
 		button.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				IStructuredSelection ssel = (IStructuredSelection)viewer.getSelection();
 				if (ssel.size() == 1) {
 					MemoPattern thisPattern = (MemoPattern) ssel.getFirstElement();
 					
 					// Find the next MemoPattern in the order.
 					MemoPattern belowPattern = null;
 					ObjectCollection<MemoPattern> patterns = account.getPatternCollection();
 					for (MemoPattern pattern: patterns) {
 						if (pattern.getOrderingIndex() > thisPattern.getOrderingIndex()) {
 							if (belowPattern == null || pattern.getOrderingIndex() < belowPattern.getOrderingIndex()) {
 								belowPattern = pattern;
 							}
 						}
 					}
 
 					if (belowPattern != null) {
 						swapOrderOfPatterns(thisPattern, belowPattern);
 					}
 
 					/*
 					 * The patterns are re-ordered but the labels are not
 					 * affected so do not request a refresh of the labels.
 					 */
 					viewer.refresh(false);
 				}
 			}
 		});
 
 		return container;
 	}
 
 	/**
 	 * Sets or clears the error message.
 	 * If not <code>null</code>, the OK button is disabled.
 	 * 
 	 * @param errorMessage
 	 *            the error message, or <code>null</code> to clear
 	 */
 	public void updateErrorMessage() {
 		String errorMessage = null;
 
 		if (account.isReconcilable()) {
 			if (account.getDefaultCategory() == null) {
 				errorMessage = "All reconcilable accounts must have a default category set.";
 			} else {
 				// Check the patterns
 				for (MemoPattern pattern: account.getPatternCollection()) {
 					if (isMemoPatternValid(pattern) != null) {
 						errorMessage = "There are errors in the patterns table.  Hover over the error image to the left of the row to see details.";
 						break;
 					}
 				}
 			}
 		}
 	
 		if (errorMessage == null) {
 //			messageArea.clearErrorMessage();    ?????
 			messageArea.restoreTitle();
 		} else {
 			messageArea.updateText(errorMessage, IMessageProvider.ERROR);
 		}
 		
 		// If called during createDialogArea, the okButton
 		// will not have been created yet.
 		Button okButton = getButton(IDialogConstants.OK_ID);
 		if (okButton != null) {
 			okButton.setEnabled(errorMessage == null);
 		}
 	}
 
 	void swapOrderOfPatterns(MemoPattern thisPattern,
 			MemoPattern abovePattern) {
 		// Swap the ordering indexes
 		int thisIndex = thisPattern.getOrderingIndex();
 		int aboveIndex = abovePattern.getOrderingIndex();
 		abovePattern.setOrderingIndex(thisIndex);
 		thisPattern.setOrderingIndex(aboveIndex);
 	}
 
 	/**
 	 * This class provides the content for the table
 	 */
 	class PatternContentProvider implements IStructuredContentProvider {
 
 		/**
 		 * Gets the elements for the table.  The elements are the MemoPattern
 		 * objects for the account.
 		 */
 		public Object[] getElements(Object input) {
 			ReconciliationAccount account = (ReconciliationAccount)input;
 			return account.getPatternCollection().toArray(new MemoPattern[0]);
 		}
 
 		/**
 		 * Disposes any resources
 		 */
 		public void dispose() {
 			// We don't create any resources, so we don't dispose any
 		}
 
 		/**
 		 * Called when the input changes
 		 */
 		public void inputChanged(Viewer arg0, Object arg1, Object arg2) {
 			// Nothing to do
 		}
 	}
 
 	/**
 	 * This class implements the sorting for the type catalog table.
 	 */
 	class PatternSorter extends ViewerSorter {
 		@Override
 		public int compare(Viewer viewer, Object e1, Object e2) {
 			MemoPattern pattern1 = (MemoPattern) e1;
 			MemoPattern pattern2 = (MemoPattern) e2;
 			
 			return pattern1.getOrderingIndex() - pattern2.getOrderingIndex();
 		}
 	}
 }
