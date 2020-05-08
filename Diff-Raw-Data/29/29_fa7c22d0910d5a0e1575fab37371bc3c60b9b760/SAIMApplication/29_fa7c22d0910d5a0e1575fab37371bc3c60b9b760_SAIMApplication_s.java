 package de.uni_leipzig.simba.saim;
 
 import java.io.File;
 import java.util.Locale;
 import java.util.Map;
 import lombok.Getter;
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 import org.vaadin.teemu.wizards.Wizard;
 import cern.colt.Arrays;
 import com.vaadin.Application;
 import com.vaadin.terminal.FileResource;
 import com.vaadin.terminal.ParameterHandler;
 import com.vaadin.ui.Button;
 import com.vaadin.ui.Button.ClickEvent;
 import com.vaadin.ui.Button.ClickListener;
 import com.vaadin.ui.Link;
 import com.vaadin.ui.MenuBar;
 import com.vaadin.ui.MenuBar.Command;
 import com.vaadin.ui.MenuBar.MenuItem;
 import com.vaadin.ui.Panel;
 import com.vaadin.ui.VerticalLayout;
 import com.vaadin.ui.Window;
 
 import de.uni_leipzig.simba.saim.core.Configuration;
 import de.uni_leipzig.simba.saim.gui.widget.ConfigUploader;
 import de.uni_leipzig.simba.saim.gui.widget.panel.MetricPanel;
 import de.uni_leipzig.simba.saim.gui.widget.window.EndpointWindow;
 
 @SuppressWarnings("serial")
 public class SAIMApplication extends Application
 {
 	public final Messages messages;
 	private static final long	serialVersionUID	= -7665596682464881860L;
 	private static SAIMApplication application = null; 
 	@Getter private final Window mainWindow;
 	private VerticalLayout mainLayout;
 	private Wizard wizard;
 	Window sub;
 	Configuration config = new Configuration();
 	Panel content;	
 	static final Logger logger = Logger.getLogger(SAIMApplication.class);
 	public static Application getInstance() {return application;}
 	private MenuBar menuBar = null;
 
 	public SAIMApplication()
 	{	
 		logger.debug("SAIMApplication()");
 		messages = new Messages(Locale.getDefault());
 		mainWindow = new Window();
 		ParameterHandler parameterHandler = new ParameterHandler()
 		{			
 			@Override
 			public void handleParameters(Map<String, String[]> parameters)
 			{
 				if(logger.getEffectiveLevel().isGreaterOrEqual(Level.INFO))
 				{logger.info("SAIMApplication was called with url parameters "+parametersToString(parameters));}
 
 				String[] languages=parameters.get("language");				
 				String language = languages==null?null:languages[0];				
 				if((language!=null))
 				{
 					setLanguage(language);
 				}
 			}
 
 			private String parametersToString(Map<String, String[]> parameters)
 			{
 				StringBuilder sb = new StringBuilder();
 				for(String key:parameters.keySet()) {sb.append(Arrays.toString(parameters.get(key)));}
 				return sb.toString();
 			}
 		};		
 
 		mainWindow.addParameterHandler(parameterHandler);
 		mainLayout = buildMainLayout();
 		mainWindow.setContent(mainLayout);
 		mainWindow.addComponent(menuBar=buildMenuBar());
 		content = new MetricPanel(messages);
 		mainLayout.addComponent(content);
 		wizard = new Wizard();	
 		setTheme("saim");
 		application=this;
 	}
 
 	private void setLanguage(String language)
 	{
 		SAIMApplication.this.messages.setLanguage(language);
 		mainWindow.setCaption(messages.getString("title")); //$NON-NLS-1$
 		refresh();
 	}
 
 	protected MenuBar buildMenuBar()
 	{
 		final MenuBar menuBar = new MenuBar();
 		menuBar.setWidth("100%"); //$NON-NLS-1$
 		//menuBar.setStyleName("margin1em");
 
 		MenuItem fileMenu = menuBar.addItem(messages.getString("file"), null, null); //$NON-NLS-1$
 		fileMenu.addItem(messages.getString("startnewconfig"), null, new StartCommand(this));
 		
 //		fileMenu.addItem(messages.getString("open"), null, null).setEnabled(false); //$NON-NLS-1$
 //		fileMenu.addItem(messages.getString("save"), null, null).setEnabled(false); //$NON-NLS-1$
 
 		fileMenu.addItem(messages.getString("importlimes"), null, importLIMESCommand).setEnabled(true);		 //$NON-NLS-1$
 		fileMenu.addItem(messages.getString("exportlimes"), null, exportLIMESCommand).setEnabled(true); //$NON-NLS-1$
 
 		
 
 		MenuItem languageMenu = menuBar.addItem(messages.getString("language"), null, null); //$NON-NLS-1$
 		languageMenu.addItem(messages.getString("german"), null, new SetLanguageCommand("de"));		 //$NON-NLS-1$
 		languageMenu.addItem(messages.getString("english"), null, new SetLanguageCommand("en")).setEnabled(true); //$NON-NLS-1$
 
 		return menuBar;
 	}
 
 	protected VerticalLayout buildMainLayout() {
 		VerticalLayout layout = new VerticalLayout();
 		layout.setSpacing(true);
 		return layout;
 	}
 
 	@Override
 	public void init() {		
 		setMainWindow(mainWindow);		
 	}
 
 	MenuBar.Command importLIMESCommand = new MenuBar.Command()
 	{
 		public void menuSelected(MenuItem selectedItem) {
 			sub = new Window(messages.getString("limesupload")); //$NON-NLS-1$
 			sub.setWidth("700px"); //$NON-NLS-1$
 			sub.setModal(true);
 			final ConfigUploader upload = new ConfigUploader(messages);
 			sub.addComponent(upload);
 			Button ok = new Button("ok"); //$NON-NLS-1$
 			sub.addComponent(ok);
 			ok.addListener(new ClickListener() {				
 				@Override
 				public void buttonClick(ClickEvent event) {
 					getMainWindow().removeWindow(sub);
 				}
 			});
 			getMainWindow().addWindow(sub);
 		}  
 	};
 
 	MenuBar.Command exportLIMESCommand = new MenuBar.Command() {
 		public void menuSelected(MenuItem selectedItem) {
 			sub = new Window(messages.getString("limesdownload")); //$NON-NLS-1$
 			sub.setWidth("700px"); //$NON-NLS-1$
 			sub.setModal(true);
 			config.saveToXML("linkspec.xml"); //$NON-NLS-1$
 
 			sub.addComponent(new Link(messages.getString("SAIMApplication.menudownloadlinkspec"),new FileResource(new File("linkspec.xml"),SAIMApplication.this))); //$NON-NLS-1$ //$NON-NLS-2$
 			getMainWindow().addWindow(sub);
 		}  
 	};
 
 	private class SetLanguageCommand implements MenuBar.Command
 	{
 		private final String language;
 		public SetLanguageCommand(String language) {this.language=language;}
 
 		public void menuSelected(MenuItem selectedItem) {setLanguage(language);}  
 	};
 
 	public Configuration getConfig()
 	{
 		if(config == null)
 			config = new Configuration();
 		return config;
 	}
 
 	//	/**
 	//	 * Show a component instead of the wizard. Finishes the Wizard.
 	//	 * @param c
 	//	 * @deprecated
 	//	 */
 	//	public void showComponent(Component c) {
 	//		mainWindow.removeWindow(sub);
 	//		wizard.finish();
 	//		mainWindow.removeAllComponents();
 	//		mainWindow.addComponent(buildMainMenu());
 	//		mainWindow.addComponent(c);
 	//	}
 	//	/**
 	//	 * Return view to the beginning: showing wizard.
 	//	 * @deprecated
 	//	 */
 	//	public void returnToBegin() {
 	//		mainWindow.removeAllComponents();
 	//		mainWindow.addComponent(buildMainMenu());
 	//
 	//		
 	//		mainLayout.addComponent(new StartPanel());
 	//		mainLayout.addComponent(wizard);
 	//
 	//	}
 	//
 	//	/**
 	//	 * @deprecated
 	//	 * @param oldStep
 	//	 * @param newStep
 	//	 */
 	//	public void setStep(WizardStep oldStep, WizardStep newStep) {
 	//		List<WizardStep> olderSteps = new LinkedList<WizardStep>();
 	//		boolean found = false;
 	//		for(WizardStep s:wizard.getSteps()) {
 	//			if(s == oldStep) {
 	//				found = true;
 	//				continue;
 	//			}
 	//			if(found) {
 	//				olderSteps.add(s);
 	//			}			
 	//		}
 	//		wizard.addStep(newStep);
 	//		wizard.activateStep(newStep);
 	//		for(WizardStep os : olderSteps) {
 	//			wizard.removeStep(os);
 	//		}
 	//	}
 	//	/**
 	//	 * @deprecated
 	//	 * @param oldStep
 	//	 */
 	//	public void removeOlderSteps(WizardStep oldStep) {
 	//		List<WizardStep> olderSteps = new LinkedList<WizardStep>();
 	//		boolean found = false;
 	//		for(WizardStep s:wizard.getSteps()) {
 	//			if(s == oldStep) {
 	//				found = true;
 	//				continue;
 	//			}
 	//			if(found) {
 	//				olderSteps.add(s);
 	//			}			
 	//		}
 	//		for(WizardStep os : olderSteps) {
 	//			wizard.removeStep(os);
 	//		}
 	//	}
 	//	
 	/**
 	 * Method is called if any action was taken in a subwindow that needs the main content to update.
 	 */
 	public void refresh()
 	{
 		mainLayout.removeComponent(content);
 		mainWindow.removeComponent(menuBar);
 		//FIXME call refresh() method instead of constructing completely new?
 		content = new MetricPanel(messages);
 		mainLayout.addComponent(content);
 
 		mainLayout.removeComponent(menuBar);
 		mainLayout.addComponent(menuBar=buildMenuBar(),0);		
 	}
 
 	public class StartCommand implements Command
 	{
 		SAIMApplication app;
 		public StartCommand(SAIMApplication app) {this.app=app;}
 
 		@Override
 		public void menuSelected(MenuItem selectedItem) {
 			config = new Configuration();
 			EndpointWindow endpointWindow = new EndpointWindow(app);
 			endpointWindow.setModal(true);
 			endpointWindow.setVisible(true);
 			app.getMainWindow().addWindow(endpointWindow);
 		}
 	}
 }
