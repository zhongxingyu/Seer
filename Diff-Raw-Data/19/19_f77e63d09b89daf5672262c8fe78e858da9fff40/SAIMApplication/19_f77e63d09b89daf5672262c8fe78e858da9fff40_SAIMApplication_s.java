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
 import com.vaadin.service.ApplicationContext.TransactionListener;
 import com.vaadin.terminal.FileResource;
 import com.vaadin.terminal.ParameterHandler;
 import com.vaadin.terminal.gwt.server.WebApplicationContext;
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
 import csplugins.layout.algorithms.circularLayout.CircularLayoutAlgorithm;
 import csplugins.layout.algorithms.force.ForceDirectedLayout;
 import csplugins.layout.algorithms.hierarchicalLayout.HierarchicalLayoutAlgorithm;
 import cytoscape.layout.algorithms.GridNodeLayout;
 import de.uni_leipzig.simba.saim.core.Configuration;
 import de.uni_leipzig.simba.saim.gui.widget.ConfigUploader;
 import de.uni_leipzig.simba.saim.gui.widget.form.EndPointUploader;
 import de.uni_leipzig.simba.saim.gui.widget.panel.MetricPanel;
 import de.uni_leipzig.simba.saim.gui.widget.window.EndpointWindow;
 /**
  * Central Application class.
  * Sets up main window and
  */
 @SuppressWarnings("serial")
 public class SAIMApplication extends Application implements TransactionListener
 {
 	public Messages messages;
 	private static final long serialVersionUID = -7665596682464881860L;
 	@Getter private  Window mainWindow;
 	private VerticalLayout mainLayout;
 	private Wizard wizard;
 	Window sub;
 	transient Configuration config = new Configuration();
 	Panel content;
 	static transient final Logger logger = Logger.getLogger(SAIMApplication.class);
 
 	private MenuBar menuBar = null;
 
 	private static ThreadLocal<SAIMApplication> currentApplication =
 			new ThreadLocal<SAIMApplication> ();
 
 	@Override
 	public void init()
 	{
 		 getContext ().addTransactionListener ( this );
 		// reproduce failure on headless environments
 //		System.setProperty("java.awt.headless", "true");
 		logger.debug("SAIMApplication()");
 //		messages = new Messages(Locale.getDefault());
 		messages = new Messages(Locale.ENGLISH);
 		mainWindow = new Window();
 		ParameterHandler parameterHandler = new ParameterHandler()
 		{
 			@Override
 			public void handleParameters(Map<String, String[]> parameters)
 			{
 				logger.setLevel(Level.DEBUG);
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
 		setMainWindow(mainWindow);
 	}
 
 	private void setLanguage(String language)
 	{
 		SAIMApplication.this.messages.setLanguage(language);
 		mainWindow.setCaption(messages.getString("title")); //$NON-NLS-1$
 		refresh();
 	}
 
 	/**
 	 * Builds the menu bar.
 	 * @return MenuBar.
 	 */
 	protected MenuBar buildMenuBar()
 	{
 		final MenuBar menuBar = new MenuBar();
 		menuBar.setWidth("100%"); //$NON-NLS-1$
 
 		MenuItem fileMenu = menuBar.addItem(messages.getString("file"), null, null); //$NON-NLS-1$
 		fileMenu.addItem(messages.getString("startnewconfig"), null, new StartCommand(this));
 
 //		fileMenu.addItem(messages.getString("open"), null, null).setEnabled(false); //$NON-NLS-1$
 //		fileMenu.addItem(messages.getString("save"), null, null).setEnabled(false); //$NON-NLS-1$
 
 		fileMenu.addItem(messages.getString("importlimes"), null, importLIMESCommand).setEnabled(true);		 //$NON-NLS-1$
 		fileMenu.addItem(messages.getString("exportlimes"), null, exportLIMESCommand).setEnabled(true); //$NON-NLS-1$
 
 		//TODO for testing to upload dumped endpoints.
 //		fileMenu.addItem("Upload Endpoint", null, uploadEndpointCommand);
 
 
 		MenuItem languageMenu = menuBar.addItem(messages.getString("language"), null, null); //$NON-NLS-1$
 		languageMenu.addItem(messages.getString("german"), null, new SetLanguageCommand("de"));		 //$NON-NLS-1$
 		languageMenu.addItem(messages.getString("english"), null, new SetLanguageCommand("en")).setEnabled(true); //$NON-NLS-1$
 
 		// zoom
 				menuBar.addItem(messages.getString("menubar_zoom_in"), null,new MenuBar.Command()	{
 					public void menuSelected(MenuItem selectedItem) {
 						if(selectedItem.getText().equals(messages.getString("menubar_zoom_in"))){//$NON-NLS-1$
 							((MetricPanel)content).getSaimcytopro().zoomIn(true);
 						}
 					}
 				});
 				menuBar.addItem(messages.getString("menubar_zoom_fit"), null,new MenuBar.Command()	{
 					public void menuSelected(MenuItem selectedItem) {
 						if(selectedItem.getText().equals(messages.getString("menubar_zoom_fit"))){//$NON-NLS-1$
 
 							((MetricPanel)content).getSaimcytopro().fitToView();
 						}
 					}
 				});
 				menuBar.addItem(messages.getString("menubar_zoom_out"), null,new MenuBar.Command()	{
 					public void menuSelected(MenuItem selectedItem) {
 						if(selectedItem.getText().equals(messages.getString("menubar_zoom_out"))){//$NON-NLS-1$
 							((MetricPanel)content).getSaimcytopro().zoomIn(false);
 						}
 					}
 				});
 
 				// layout algo.
 				MenuItem layoutalgo = menuBar.addItem(messages.getString("menubar_layout_algorithm"), null, null); //$NON-NLS-1$
 
 				layoutalgo.addItem(messages.getString("menubar_layout_algorithm_force_directed"), null, new MenuBar.Command()	{//$NON-NLS-1$
 					public void menuSelected(MenuItem selectedItem) {
 						if(selectedItem.getText().equals(messages.getString("menubar_layout_algorithm_force_directed"))){//$NON-NLS-1$
 							((MetricPanel)content).getSaimcytopro().applyLayoutAlgorithm(new ForceDirectedLayout(),true);
 						}
 					}
 				});
 				layoutalgo.addItem(messages.getString("menubar_layout_algorithm_hierarchical"), null, new MenuBar.Command()	{//$NON-NLS-1$
 					public void menuSelected(MenuItem selectedItem) {
 						if(selectedItem.getText().equals(messages.getString("menubar_layout_algorithm_hierarchical"))){//$NON-NLS-1$
 						((MetricPanel)content).getSaimcytopro().applyLayoutAlgorithm(new HierarchicalLayoutAlgorithm(),true);
 						}
 					}
 				});
 				layoutalgo.addItem(messages.getString("menubar_layout_algorithm_grid"), null, new MenuBar.Command()	{//$NON-NLS-1$
 					public void menuSelected(MenuItem selectedItem) {
 						if(selectedItem.getText().equals(messages.getString("menubar_layout_algorithm_grid"))){//$NON-NLS-1$
 							((MetricPanel)content).getSaimcytopro().applyLayoutAlgorithm(new GridNodeLayout(),true);
 						}
 					}
 				});
 				layoutalgo.addItem(messages.getString("menubar_layout_algorithm_circular"), null, new MenuBar.Command()	{//$NON-NLS-1$
 					public void menuSelected(MenuItem selectedItem) {
 						if(selectedItem.getText().equals(messages.getString("menubar_layout_algorithm_circular"))){//$NON-NLS-1$
 							((MetricPanel)content).getSaimcytopro().applyLayoutAlgorithm(new CircularLayoutAlgorithm(),true);
 						}
 					}
 				});
 
 		return menuBar;
 	}
 
 	protected VerticalLayout buildMainLayout() {
 		VerticalLayout layout = new VerticalLayout();
 		layout.setSpacing(true);
 		return layout;
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
 
 
 	MenuBar.Command uploadEndpointCommand = new MenuBar.Command() {
 		@Override
 		public void menuSelected(MenuItem selectedItem) {
 			sub = new Window("Endpoint Upload");
 			sub.setWidth("700px"); //$NON-NLS-1$
 			sub.setModal(true);
 			sub.addComponent(new EndPointUploader());
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
 
 
 	/**
 	 * Method is called if any action was taken in a subwindow that needs the main content to update.
 	 */
 	public void refresh()
 	{
 		//mainLayout.removeComponent(content);
 		mainWindow.removeComponent(menuBar);
 		//FIXME call refresh() method instead of constructing completely new?
 		//content = new MetricPanel(messages);
 		content.attach();
 		//mainLayout.addComponent(content);
 
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
 
 	/**
 	 * Get the WEB-INF Folder on runtime
 	 * @return
 	 */
 	public File getWebInfFolder() {
 		WebApplicationContext context = (WebApplicationContext)getContext();
 		File f = new File ( context.getHttpSession().getServletContext().getRealPath("/WEB-INF") );
 		System.out.println(f.getAbsolutePath());
 		return f;
 	}
 
 	@Override
 	public void transactionStart ( Application application, Object o )
     {
         if ( application == SAIMApplication.this )
         {
             currentApplication.set ( this );
         }
     }
 	@Override
     public void transactionEnd ( Application application, Object o )
     {
         if ( application == SAIMApplication.this )
         {
             currentApplication.set ( null );
             currentApplication.remove ();
         }
     }
 
 	/**
 	 * For access in non-UI classes.
 	 * @TODO Heavy testing
 	 * @return SAIMApplication instance
 	 */
     public static SAIMApplication getInstance()
     {
         return currentApplication.get ();
     }
 }
