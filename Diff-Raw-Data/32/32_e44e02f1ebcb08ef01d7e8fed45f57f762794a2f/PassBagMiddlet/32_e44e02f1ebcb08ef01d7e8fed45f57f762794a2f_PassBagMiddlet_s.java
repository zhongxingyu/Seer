 package midbase;
 
 import java.util.Date;
 import java.util.Vector;
 
 import javax.microedition.lcdui.Alert;
 import javax.microedition.lcdui.AlertType;
 import javax.microedition.lcdui.Command;
 import javax.microedition.lcdui.CommandListener;
 import javax.microedition.lcdui.Display;
 import javax.microedition.lcdui.Displayable;
 import javax.microedition.lcdui.Item;
 import javax.microedition.lcdui.List;
 import javax.microedition.midlet.MIDlet;
 import javax.microedition.midlet.MIDletStateChangeException;
 
 import midbase.component.CustomChoiceGroup;
 import midbase.component.CustomTextField;
 import midbase.data.CategoryRepository;
 import midbase.data.EntryRepository;
 import midbase.data.TitleRepository;
 import midbase.data.UserSettingsRepository;
 import midbase.domain.Category;
 import midbase.domain.Entry;
 import midbase.domain.Field;
 import midbase.domain.Title;
 import midbase.domain.UserSettings;
 import midbase.localization.L10nConstants;
 import midbase.localization.L10nResources;
 import midbase.security.ChriptoHelper;
 import midbase.view.CategoryList;
 import midbase.view.EntryForm;
 import midbase.view.EntryList;
 import midbase.view.LoginForm;
 import net.sourceforge.floggy.persistence.PersistableManager;
 
 public class PassBagMiddlet extends MIDlet implements CommandListener {
 
 	String locale = System.getProperty("microedition.locale");
 	
 	private DisplayManager manager;
 	L10nResources resources = L10nResources.getL10nResources(L10nConstants.locales.EN_US);
 	
 	private Command exitCommand = new Command(resources.getString(L10nConstants.keys.EXIT), Command.EXIT, 60);
 	
 	private Command loginFormOKCommand;
 		
 	private midbase.view.CategoryList categoryListView;
 	private midbase.view.EntryList entryListView;
 	private midbase.view.EntryForm entryFormView;
 		
 	private Command	 back;
 	
 	private Command selectCategoryCommand;
 	private Command newCategoryCommand; //TODO
 	private Command editCategoryCommand; //TODO 
 	private Command deleteCategoryCommand; //TODO
 		
 	private Command settingsCommand;
 	
 	private Command selectEntryFormCommand;
 	private Command createEntryFormCommand;
 	
 	public Command entryFormSaveCommand;
 	public Command entryFormCancelCommand;
 	public Command entryFormAddFieldCommand;
 	public Command entryFormDeleteEntryCommand;
 			
 	PersistableManager persistableManager = PersistableManager.getInstance();
 	
 	EntryRepository entryRepository = new EntryRepository(this.persistableManager);
 	CategoryRepository categoryRepository = new CategoryRepository();
 	TitleRepository titleRepository = new TitleRepository();
 	UserSettingsRepository userSettingsRepository = new UserSettingsRepository(this.persistableManager);
 		
 	int ActiveCategoryId;
 	String ActiveCategoryName;
 	
 	ChriptoHelper chriptoHelper = new ChriptoHelper();
 	private byte[] cipher;	
 			
 	String backKey = resources.getString(L10nConstants.keys.BACK);
 	
 	public PassBagMiddlet() {
 		printMemoryDetails("Const top");
 		
 		this.manager = new DisplayManager(Display.getDisplay(this));
 		
 		printMemoryDetails("Const after new DisplayManager");
 		
 		this.back = new Command(backKey, Command.BACK, 1);
 	}
 	
 	/* (non-Javadoc)
 	 * @see javax.microedition.midlet.MIDlet#startApp()
 	 */
 	protected void startApp() throws MIDletStateChangeException {
 		printMemoryDetails("Start app top.");
 		
 		this.categoryListView = this.getCategoryListScreen();
 		
 		//TODO:
 		//this.cipher = "mytestppassword0".getBytes();
 		
 		//this.manager.next(this.categoryListView);
 		
 		//TODO : icerde kayit olup olmama durumuna g�re
 		UserSettings userSettings = this.userSettingsRepository.get();
 		if (userSettings == null)
 			this.manager.next(getLoginFormScreen(true));
 		else
 			this.manager.next(getLoginFormScreen(false));
 		
 		printMemoryDetails("Start app bottom.");
 	}
 	
 	/* (non-Javadoc)
 	 * @see javax.microedition.lcdui.CommandListener#commandAction(javax.microedition.lcdui.Command, javax.microedition.lcdui.Displayable)
 	 */
 	public void commandAction(Command command, Displayable displayable) {
 
 		if (command == this.exitCommand){
 			this.destroyApp(false);
 	        this.notifyDestroyed();
 		}
 		
 		if (command == this.back) {
 			if (displayable.getClass() == EntryList.class)
 				this.manager.next(this.categoryListView);
 		}
 		
 		//** Login Form commands		
 		if (displayable.getClass() == midbase.view.LoginForm.class){
 			if (command == this.loginFormOKCommand){
 				LoginForm loginForm = (LoginForm)displayable;
 				String password = loginForm.passwordTextField.getString();
 				//TODO  if shorter then 6 return.				
 				String cipherTemp = password;
 				while (cipherTemp.length() <16 )
 			    {
 					cipherTemp = cipherTemp.concat(" ");
 			    }
 				
 				if (loginForm.firstTime){
 					if (!password.equals(loginForm.password2TextField.getString())){
 						Alert alert = new Alert("Alert", "Re-enter password does not some with the old one.", null, AlertType.WARNING);
 						this.manager.getDisplay().setCurrent(alert, loginForm);
 						return;
 					}
 										
 					UserSettings userSettings = new UserSettings();
 					userSettings.setApplicationLanguage(L10nConstants.locales.TR_TR);
 					userSettings.setRunDate(new Date());
 					byte[] encryptedValidationData = this.chriptoHelper.Enc(cipherTemp.getBytes(), new Date().toString().getBytes());
 					userSettings.setValidationData(encryptedValidationData);
 					this.userSettingsRepository.save(userSettings);
 				}
 				else{
 					UserSettings userSettings = this.userSettingsRepository
 							.get();
 					try {
 						this.chriptoHelper.Dec(
 								userSettings.getValidationData(),
 								cipherTemp.getBytes());						
 					} catch (Exception e) {
 						Alert alert = new Alert("Alert",
 								"Password is wrong", null,
 								AlertType.WARNING);
 						// alert.setTimeout(Alert.FOREVER);
 						this.manager.getDisplay().setCurrent(alert,
 								getLoginFormScreen(false));
 					}
 				}
 				this.cipher = cipherTemp.getBytes();
 				this.manager.next(this.categoryListView);	
 			}	
 			
 		}
 		
 		//** Category List Commands
 		if (displayable.getClass() == midbase.view.CategoryList.class){
 			if (command == List.SELECT_COMMAND || command == this.selectCategoryCommand){			
 				printMemoryDetails("Category list command-before");				
 				SetActiveCategoryId();
 				Vector entries = this.entryRepository.getEntriesByCategory(Entry.class, this.ActiveCategoryId);			
 				this.manager.next(getEntryListScreen(this.ActiveCategoryName, entries));		
 				printMemoryDetails("Category list command-after");		
 			}
 			
 			if (command == this.settingsCommand){
 				//TODO Dil ayarlari
 			}
 		}
 		
 		//** Entry List Commands
 		if (displayable.getClass() == EntryList.class){
 			if (command == this.createEntryFormCommand){
 				SetActiveCategoryId();
 				this.manager.next(getEntryFormScreen(this.ActiveCategoryId));
 			}
 			
 			if (command == List.SELECT_COMMAND || command == this.selectEntryFormCommand){
 				int selectedEntryIndex = ((EntryList)displayable).getSelectedIndex();
 				if (selectedEntryIndex == -1){
 					this.manager.next(new Alert("Alert", "You should choose account !", null, AlertType.WARNING));
 				}
 				Entry entry = ((EntryList)displayable).getSelectedEntry(selectedEntryIndex);			
 				EntryForm entryForm = getEntryFormScreen(this.ActiveCategoryId, entry);				
 				this.manager.next(entryForm);
 			}
 		}
 		//** End Entry List Commands
 			
 		//** Entry Form Commands
 		if (displayable.getClass() == EntryForm.class){
 			if (command == entryFormSaveCommand){
 				SaveEntry((EntryForm)displayable);			
 				Alert alert = new Alert("Info", "You account has been saved.", null, AlertType.INFO);
 				//alert.setTimeout(Alert.FOREVER);
 				Vector entries = this.entryRepository.getEntriesByCategory(Entry.class, this.ActiveCategoryId);			
 				this.manager.getDisplay().setCurrent(alert, getEntryListScreen(this.ActiveCategoryName, entries));
 			}
 			
 			if (command == this.entryFormCancelCommand){				
 				Alert alert = new Alert("Alert", "Are you sure ?", null, AlertType.CONFIRMATION);
 				alert.setTimeout(Alert.FOREVER);
 				alert.addCommand(deleteEntryOkCommand);
				alert.addCommand(deleteEntryCancelCommand);
 				this.manager.getDisplay().setCurrent(alert);
 			}
 			
 			if (command == entryFormAddFieldCommand){
 				((EntryForm)displayable).addField();
 			}
		}
 		
		if (command == deleteEntryCancelCommand)
		{
			Vector entries = this.entryRepository.getEntriesByCategory(Entry.class, this.ActiveCategoryId);				
			this.manager.getDisplay().setCurrent(getEntryListScreen(this.ActiveCategoryName, entries));
		}
 		if (command == deleteEntryOkCommand)
 		{
 			//Entry entry = this.entryFormView.getOriginalEntry();
 			//this.entryRepository.delete(entry);
			Alert alert = new Alert("Alert", "Record has been deleted.", null, AlertType.WARNING);
 			Vector entries = this.entryRepository.getEntriesByCategory(Entry.class, this.ActiveCategoryId);				
			this.manager.getDisplay().setCurrent(alert, getEntryListScreen(this.ActiveCategoryName, entries));
 		}
		
 	}	
 
 	 Command deleteEntryOkCommand = new Command("Yes", Command.CANCEL, 0);
	 Command deleteEntryCancelCommand = new Command("Cancel", Command.SCREEN, 0);
 	
 	/* (non-Javadoc)
 	 * @see javax.microedition.midlet.MIDlet#destroyApp(boolean)
 	 */
 	protected void destroyApp(boolean unconditional) {
 		System.out.println("Middlet is destroyed.");		
 	} //throws MIDletStateChangeException {}
 
 	/* (non-Javadoc)
 	 * @see javax.microedition.midlet.MIDlet#pauseApp()
 	 */
 	protected void pauseApp() {}
 	
 	private LoginForm getLoginFormScreen(boolean firsTime){
 		LoginForm loginForm = new LoginForm(this.resources, firsTime);
 		this.loginFormOKCommand = new Command("OK", Command.OK, 1);
 		
 		loginForm.setCommandListener(this);
 		loginForm.addCommand(this.loginFormOKCommand);		
 		loginForm.addCommand(this.exitCommand);
 		
 		return loginForm;
 	}
 	
 	private CategoryList getCategoryListScreen(){
 		CategoryList categoryList = new CategoryList(this.categoryRepository.GetCategories(), this.resources);
 		
 		this.selectCategoryCommand = new Command(this.resources.getString(L10nConstants.keys.ACCOUNTS), Command.OK, 1);
 		this.settingsCommand = new Command("Ayarlar", Command.ITEM, 1);
 		this.newCategoryCommand = new Command("Yeni", Command.ITEM, 1);
 		this.editCategoryCommand = new Command("Düzenle", Command.ITEM, 1);
 		this.deleteCategoryCommand = new Command("Sil", Command.ITEM, 1);
 		
 		categoryList.setCommandListener(this);
 		categoryList.addCommand(this.exitCommand);		
 		categoryList.addCommand(this.selectCategoryCommand);
 		categoryList.addCommand(this.settingsCommand);
 		categoryList.addCommand(this.newCategoryCommand);
 		categoryList.addCommand(this.editCategoryCommand);
 		categoryList.addCommand(this.deleteCategoryCommand);
 		
 		return categoryList;
 	}
 	
 	private EntryList getEntryListScreen(String categoryName, Vector v) {
 		printMemoryDetails("Get Entry List first");
 		
 		if (this.entryListView != null){
 			this.entryListView.ChangeList(v);
 			
 			return this.entryListView;
 		}
 		
 		EntryList entryListView = new EntryList(categoryName, v);
 		entryListView.setCommandListener(this);
 		//entryListView.setSelectCommand(this.selectEntryFormCommand);//Orta tus
 		this.createEntryFormCommand = new Command(this.resources.getString(L10nConstants.keys.NEW_ACCOUNT), Command.ITEM, 1);
 		this.selectEntryFormCommand = new Command(this.resources.getString(L10nConstants.keys.ACCOUNT_DETAILS), Command.OK, 1);	
 		entryListView.addCommand(this.back);
 		entryListView.addCommand(this.selectEntryFormCommand);
 		entryListView.addCommand(this.createEntryFormCommand);
 		
 		this.entryListView = entryListView;
 		
 		printMemoryDetails("Get Entry List after");
 				
 		return this.entryListView;
 	}
 	
 	private EntryForm getEntryFormScreen(int categoryId){
 		return getEntryFormScreen(categoryId, null);
 	}
 	
 	private EntryForm getEntryFormScreen(int categoryId, Entry entry){
 		printMemoryDetails("form create before");
 				
 		EntryForm form = null;
 		if (entry == null){
 			form = new EntryForm(categoryId, this.titleRepository.GetFieldTitles(), this.chriptoHelper, this.cipher, this.resources);
 			form.addField();
 		}
 		else
 			form = new EntryForm(categoryId, entry, this.titleRepository.GetFieldTitles(), this.chriptoHelper, this.cipher, this.resources);
 		
 		this.entryFormCancelCommand = new Command(this.resources.getString(L10nConstants.keys.CANCEL), Command.BACK, 1);
 	    this.entryFormSaveCommand = new Command(this.resources.getString(L10nConstants.keys.SAVE), Command.ITEM, 4);
 		this.entryFormAddFieldCommand = new Command(this.resources.getString(L10nConstants.keys.ADD_NEW_FIELD), Command.ITEM, 3);    
 	    this.entryFormDeleteEntryCommand = new Command(this.resources.getString(L10nConstants.keys.DELETE), Command.ITEM, 2);
 		
 	    form.addCommand(this.entryFormCancelCommand);
 	    form.addCommand(this.entryFormSaveCommand);
 	    form.addCommand(this.entryFormAddFieldCommand);
 	    form.addCommand(this.entryFormDeleteEntryCommand);
 	    form.setCommandListener(this);
 	    
 	    this.entryFormView = form;
 	    
 	    printMemoryDetails("form create After ");
 	    
 	    return this.entryFormView;
 	}
 		
 	private void SaveEntry(EntryForm form){
 		Entry e = new Entry();
 	
 		if (form.getOriginalEntry() != null){
 			e = (Entry)this.entryRepository.get(e, form.getOriginalEntry().getId());
 		}
 		
 		e.setName(form.AccountNameTextField.getString());
 		e.setCategory(new Category(form.CategoryId));
 		e.setNote(form.NoteTextField.getString());		
 									
 		Field[] fields = new Field[form.size() - 3];
 				
 		int fieldCounter = 0;
 		for (int i=0, formSize = form.size(); i < formSize; i++){
 			Item item = form.get(i);
 			if (item.getClass() == CustomChoiceGroup.class){
 				Title title = null;
 				String fieldValue = null;
 				CustomChoiceGroup choiceGroup = (CustomChoiceGroup)item;
 				title = this.titleRepository.getFieldTitle(choiceGroup.getSelectedIndex());
 				i++;
 				item = form.get(i);
 				CustomTextField textField = (CustomTextField)item;
 				fieldValue = textField.getString();							
 				//
 				byte[] encVal = this.chriptoHelper.Enc(this.cipher, fieldValue.getBytes());
 				fields[fieldCounter] = new Field(title, encVal);				
 				//
 				fieldCounter++;
 			}	
 						
 		}		
 		
 		e.setFields(fields);
 		
 		this.entryRepository.save(e);
 	}
 	
 	private void SetActiveCategoryId() {
 		int selectedCategoryIndex = ((List)this.categoryListView).getSelectedIndex();
 		this.ActiveCategoryId = this.categoryRepository.GetCategories()[selectedCategoryIndex].GetId();
 		this.ActiveCategoryName = this.categoryRepository.GetCategories()[selectedCategoryIndex].GetName();
 	}	
 	
 	private void printMemoryDetails(String place){
 		System.out.println(place);
 		System.out.println("Total Memory:" + Runtime.getRuntime().totalMemory());
 		System.out.println("Free Memory:" + Runtime.getRuntime().freeMemory());
 		System.out.println("");
 	}
 	
 }
