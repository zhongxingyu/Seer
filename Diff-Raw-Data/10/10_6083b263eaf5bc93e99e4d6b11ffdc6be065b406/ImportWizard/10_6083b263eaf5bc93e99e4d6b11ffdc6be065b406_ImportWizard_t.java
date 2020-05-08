 package sia.ui.importui;
 
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.jface.dialogs.IPageChangedListener;
 import org.eclipse.jface.dialogs.IPageChangingListener;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.dialogs.PageChangedEvent;
 import org.eclipse.jface.dialogs.PageChangingEvent;
 import org.eclipse.jface.wizard.IWizardPage;
 import org.eclipse.jface.wizard.Wizard;
 import org.eclipse.jface.wizard.WizardDialog;
 import org.eclipse.jface.wizard.WizardPage;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.sormula.SormulaException;
 
 import sia.datasources.DataSource;
 import sia.models.Contact;
 import sia.utils.Dictionaries;
 
 /**
  * 
  * @author Agnieszka Glabala
  */
 public class ImportWizard extends Wizard implements IPageChangingListener, IPageChangedListener, SelectionListener,
 		ModifyListener {
 	private String[] imNames;
 	private int im;
 	private boolean wasSetAccounts = false;
 	DataSource datasource;
 	ImportChooseIM chooseIM;
 	ImportChooseFiles chooseFiles;
 	ImportSetPasswords setPasswords;
 	ImportChooseAccounts chooseAccounts;
 	ImportSetAccounts setAccounts;
 	ImportMapContacts mapContacts;
 	ImportLoading accountsLoading;
 	ImportLoading messageLoading;
 	ImportLoading saveLoading;
 	ImportSetContacts setContacts;
 	List<Contact> contacts;
 
 	public ImportWizard() {
 		setWindowTitle("Import messages");
 		imNames = Dictionaries.getInstance().getDataSources().keySet().toArray(new String[] {});
 		chooseIM = new ImportChooseIM(imNames);
 		chooseFiles = new ImportChooseFiles();
 		setPasswords = new ImportSetPasswords();
 		chooseAccounts = new ImportChooseAccounts();
 		setAccounts = new ImportSetAccounts();
 		mapContacts = new ImportMapContacts();
 		accountsLoading = new ImportLoading("accountsLoading");
 		accountsLoading.setTitle("Accounts loading");
 		messageLoading = new ImportLoading("messagesLoading");
 		messageLoading.setTitle("Messages loading");
 		saveLoading = new ImportLoading("saveLoading");
 		saveLoading.setTitle("Saving");
 		setContacts = new ImportSetContacts();
 		im = -1;
 		List<Contact> tmpContacts = Dictionaries.getInstance().getContacts();
 		contacts = new ArrayList<Contact>();
 		for (int i = 0; i < tmpContacts.size(); i++) {
 			contacts.add(tmpContacts.get(i).clone());
 		}
 		mapContacts.setNextPage(setContacts);
 	}
 
 	@Override
 	public void addPages() {
 		addPage(chooseIM);
 		addPage(chooseFiles);
 		addPage(setPasswords);
 		addPage(accountsLoading);
 		addPage(setAccounts);
 		addPage(chooseAccounts);
 		addPage(messageLoading);
 		addPage(mapContacts);
 		addPage(setContacts);
 		addPage(saveLoading);
 		chooseIM.setPageComplete(false);
 	}
 
 	public boolean validatePage(IWizardPage page) {
 		try {
 			((WizardPage) page).setErrorMessage(null);
 			if (page == chooseIM) {
 				if (chooseIM.getSelected() == -1) {
 					chooseIM.setErrorMessage("Choose an application");
 					return false;
 				}
 			} else if (page == chooseFiles) {
 				String msg = datasource.validateFiles(chooseFiles.getFiles());
 				if (msg != null) {
 					chooseFiles.setErrorMessage(msg);
 					return false;
 				}
 			} else if (page == setPasswords) {
 				String msg = datasource.validatePasswords(setPasswords.getPasswords());
 				if (msg != null) {
 					setPasswords.setErrorMessage(msg);
 					return false;
 				}
 			} else if (page == accountsLoading) {
 				if (datasource.getProgress(DataSource.Progress.USER_ACCOUNTS_PROGRESS) != 100) {
 					accountsLoading.setErrorMessage("You can't go to the next page until accounts are loaded.");
 					return false;
 				}
 			} else if (page == setAccounts) {
 				String msg = datasource.validateUid(setAccounts.getUserAccounts().get(0).getUid());
 				if (msg != null) {
 					setAccounts.setErrorMessage(msg);
 					return false;
 				}
 			} else if (page == chooseAccounts) {
 				if (chooseAccounts.getUserAccounts().isEmpty()) {
 					chooseAccounts.setErrorMessage("You have to select at least one account.");
 					return false;
 				}
 			} else if (page == messageLoading) {
 				if (datasource.getProgress(DataSource.Progress.CONTACTS_PROGRESS) != 100) {
 					// TODO: [Marek] try to disable next button
 					messageLoading
 							.setErrorMessage("You can't go to the next page until contacts and messages are loaded.");
 					return false;
 				}
 				// } else if (page == mapContacts) {
 			} else if (page == setContacts) {
 				List<ImportSetContacts.Controls> controls = setContacts.getControls();
 				String uid;
 				for (ImportSetContacts.Controls ctls : controls) {
 					if (ctls.getVisible()) {
 						// TODO [Marek] check this
 						for (Contact c : contacts) {
 							if (ctls.name.getVisible() && ctls.name.getEnabled()
 									&& ctls.name.getText().toLowerCase().trim().equals(c.getName().toLowerCase())) {
 								setContacts
 										.setErrorMessage("A contact with name \""
 												+ ctls.name.getText()
 												+ "\" already exists. Connect one to another on previous page or change its name.");
 								return false;
 							}
 						}
 						for (ImportSetContacts.Controls ctls2 : controls) {
 							if (ctls != ctls2 && ctls2.getVisible() && ctls2.name.getEnabled()
 									&& ctls.name.getEnabled() && ctls.name.getText().equals(ctls2.name.getText())) {
 								setContacts.setErrorMessage("Two contacts can't have the same name ("
 										+ ctls.name.getText() + "). Connect one to another or change name.");
 								return false;
 							}
 						}
 						uid = ctls.getSelectedItem();
 						if (uid != null) {
 							for (int i = 0; i < datasource.getContacts().size(); i++) {
 								if (datasource.getContacts().get(i).getContactAccounts().get(0).getUid().equals(uid)
 										&& controls.get(i).getSelectedItem() != null) {
 									setContacts.setErrorMessage("Chained contact merging disallowed. Contact with UID "
 											+ ctls.uids[0].getText() + " can't be attached to contact with UID " + uid
 											+ ", because it's already attached to another contact.");
 									return false;
 								}
 							}
 						}
 					}
 				}
 			}
 		} finally {
 			((WizardPage) page).setPageComplete(((WizardPage) page).getErrorMessage() == null);
 			page.canFlipToNextPage();
 		}
 		return true;
 	}
 
 	@Override
 	public boolean performFinish() {
 		if (datasource == null || datasource.getProgress(DataSource.Progress.SAVE_PROGRESS) != 100) {
 			return false;
 		}
 		try {
 			Dictionaries.getInstance().loadContacts();
 		} catch (SormulaException e) {
 			// TODO: [Marek] logger (Issue #5)
 			e.printStackTrace();
 		}
 		return true;
 	}
 
 	@Override
 	public void handlePageChanging(PageChangingEvent event) {
 		WizardDialog dialog = (WizardDialog) event.getSource();
 		WizardPage current = (WizardPage) event.getCurrentPage();
 		WizardPage target = (WizardPage) event.getTargetPage();
 		if (current.getNextPage().equals(event.getTargetPage()) && !validatePage(current)) {
 			event.doit = false;
 			return;
 		}
 		if (event.getTargetPage() == chooseFiles) {
 			int previousIm = im;
 			im = chooseIM.getSelected();
 			this.datasource = Dictionaries.getInstance().getDataSource(imNames[im]);
 			if (datasource.getFileExtensions() == null || datasource.getFileExtensions().length == 0) {
 				dialog.showPage(setPasswords);
 				event.doit = false;
 			} else if (previousIm != im) {
 				chooseFiles.setFileExtensions(datasource.getFileExtensions());
 				chooseFiles.setDescriptions(datasource.getFileDescriptions());
 				chooseFiles.setControls();
 				chooseFiles.setPageComplete(false);
 				chooseFiles.canFlipToNextPage();
 			}
 			// TODO: [Aga] fix this (Issue #10)
 			this.getShell().setSize(this.getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT));
 		} else if (event.getTargetPage() == setPasswords) {
 			setPasswords.setPasswordDescpriptions(datasource.getRequiredPassword());
 			if (datasource.getRequiredPassword() != null && datasource.getRequiredPassword().length > 0) {
 				setPasswords.setPasswordDescpriptions(datasource.getRequiredPassword());
 				setPasswords.setControls();
 				setPasswords.setPageComplete(false);
 				setPasswords.canFlipToNextPage();
 			} else {
 				dialog.showPage(accountsLoading);
 				event.doit = false;
 			}
 		} else if (event.getTargetPage() == accountsLoading) {
 		} else if (event.getTargetPage() == setAccounts) {
 			if (!wasSetAccounts) {
 				if (datasource.getUserAccounts() != null && datasource.getUserAccounts().size() > 0
 						&& datasource.getUserAccounts().get(0).getUid().length() > 0) {
 					chooseAccounts.setUserAccounts(datasource.getUserAccounts());
 					chooseAccounts.setControls();
 					chooseAccounts.setPageComplete(false);
 					chooseAccounts.canFlipToNextPage();
 					dialog.showPage(chooseAccounts);
 					event.doit = false;
 				} else {
 					setAccounts.setUserAccounts(datasource.getUserAccounts());
 					setAccounts.setControls();
 					setAccounts.setPageComplete(false);
 					setAccounts.canFlipToNextPage();
 					wasSetAccounts = true;
 				}
 			}
 		} else if (event.getTargetPage() == chooseAccounts) {
 			if (wasSetAccounts) {
 				dialog.showPage(event.getCurrentPage() == messageLoading ? setAccounts : messageLoading);
 				event.doit = false;
 			}
 		} else if (event.getTargetPage() == messageLoading) {
 		} else if (event.getTargetPage() == mapContacts) {
 			if (datasource.getContacts().size() == 0) {
 				dialog.showPage(setContacts);
 				event.doit = false;
 			}
 		} else if (event.getTargetPage() == setContacts) {
 			validatePage(setContacts);
 			setContacts.layout();
 		} else if (event.getTargetPage() == saveLoading) {
 		}
 	}
 
 	@Override
 	public void pageChanged(PageChangedEvent event) {
 		if (event.getSelectedPage() == accountsLoading) {
 			if (datasource.getRequiredPassword() != null && datasource.getRequiredPassword().length > 0) {
 				datasource.setPasswords(setPasswords.getPasswords());
 			}
 			new Thread(new Runnable() {
 				@Override
 				public void run() {
 					datasource.loadFiles(chooseFiles.getFiles());
 					datasource.getUserAccounts();
 				}
 			}).start();
 			new Thread(new Loader(DataSource.Progress.USER_ACCOUNTS_PROGRESS, accountsLoading)).start();
 		} else if (event.getSelectedPage() == messageLoading) {
 			if (wasSetAccounts) {
 				datasource.setUserAccounts(setAccounts.getUserAccounts());
 			} else {
 				datasource.setUserAccounts(chooseAccounts.getUserAccounts());
 			}
 			new Thread(new Runnable() {
 				@Override
 				public void run() {
 					datasource.getContacts();
 					datasource.mapContacts(contacts);
 					mapContacts.setParsedContacts(datasource.getContacts());
 					mapContacts.setContacts(contacts);
 					setContacts.setParsedContacts(datasource.getContacts());
 					setContacts.setContacts(contacts);
 					getShell().getDisplay().asyncExec(new Runnable() {
 						public void run() {
 							mapContacts.setControls();
 							setContacts.setControls();
 						}
 					});
 				}
 			}).start();
 			new Thread(new Loader(DataSource.Progress.CONTACTS_PROGRESS, messageLoading)).start();
 		} else if (event.getSelectedPage() == saveLoading) {
 			mapContacts.addContactAccounts(); // merge new ContactAccounts with
 												// existing Contacts
 			setContacts.addNewContacts();
 			new Thread(new Runnable() {
 				@Override
 				public void run() {
 					try {
 						datasource.save(contacts);
 					} catch (SQLException e) {
 						// TODO: [Marek] catch block
 						e.printStackTrace();
 					} catch (SormulaException e) {
 						// TODO: [Marek] catch block
 						e.printStackTrace();
 					}
 				}
 			}).start();
 			new Thread(new Loader(DataSource.Progress.SAVE_PROGRESS, saveLoading)).start();
 		}
 	}
 
 	@Override
 	public void widgetDefaultSelected(SelectionEvent arg0) {
 	}
 
 	@Override
 	public void widgetSelected(SelectionEvent e) {
 		validatePage(this.getContainer().getCurrentPage());
 	}
 
 	@Override
 	public void modifyText(ModifyEvent e) {
 		validatePage(this.getContainer().getCurrentPage());
 	}
 
 	/**
 	 * Loader.
 	 * 
 	 * Updates specified operation progress
 	 * 
 	 * @author jumper
 	 */
 	class Loader implements Runnable {
 		private DataSource.Progress progress;
 		private ImportLoading page;
 
 		/**
 		 * Default and only constructor
 		 * 
 		 * @param progress
 		 *            progress percent
 		 * @param page
 		 *            loading page
 		 */
 		public Loader(DataSource.Progress progress, ImportLoading page) {
 			this.progress = progress;
 			this.page = page;
 		}
 
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void run() {
 			int value = 0;
			getShell().getDisplay().asyncExec(new Runnable() {
				public void run() {
					page.setPageComplete(false);
					page.canFlipToNextPage();
				}
			});
 			while (value < 100) {
 				value = datasource.getProgress(progress);
 				final int valueToSet = value;
 				getShell().getDisplay().asyncExec(new Runnable() {
 					public void run() {
 						page.setProgress(valueToSet);
 					}
 				});
 				try {
 					Thread.sleep(100);
 				} catch (InterruptedException e) {/* TODO: [Marek] logger */
 				}
 			}
 			getShell().getDisplay().asyncExec(new Runnable() {
 				public void run() {
 					page.setPageComplete(true);
 					page.canFlipToNextPage();
 				}
 			});
 		}
 	}
 }
