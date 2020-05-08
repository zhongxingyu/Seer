 package org.vaadin.mideaas.frontend;
 
 import java.util.List;
 
 import org.vaadin.aceeditor.SuggestionExtension;
 import org.vaadin.chatbox.ChatBox;
 import org.vaadin.chatbox.client.ChatUser;
 import org.vaadin.mideaas.editor.MultiUserEditor;
 import org.vaadin.mideaas.frontend.ProjectItemList.Listener;
 import org.vaadin.mideaas.java.JavaErrorChecker;
 import org.vaadin.mideaas.java.JavaSuggester;
 import org.vaadin.mideaas.model.ProjectFile;
 import org.vaadin.mideaas.model.SharedProject;
 import org.vaadin.mideaas.model.SharedView;
 import org.vaadin.mideaas.model.User;
 import org.vaadin.mideaas.model.UserSettings;
 
 import com.vaadin.ui.Component;
 import com.vaadin.ui.CustomComponent;
 import com.vaadin.ui.HorizontalSplitPanel;
 import com.vaadin.ui.MenuBar;
 import com.vaadin.ui.MenuBar.Command;
 import com.vaadin.ui.MenuBar.MenuItem;
 import com.vaadin.ui.Panel;
 import com.vaadin.ui.VerticalLayout;
 
 @SuppressWarnings("serial")
 public class MideaasEditor extends CustomComponent implements Listener {
 
 	public interface CloseHandler {
 		public void closeRequested(SharedProject project);
 	}
 
 	
 
 	private final SharedProject project;
 
 	private final User user;
 	
 	private final UserSettings settings;
 
 	private final VerticalLayout layout = new VerticalLayout();
 
 	private com.vaadin.ui.MenuBar menu;
 
 	private HorizontalSplitPanel split;
 
 	private VerticalLayout sideBar;
 
 	private BuildComponent buildComponent;
 
 	private String activeComponentName;
 
 	private Component activeEditor;
 
 	private ProjectItemList componentTree;
 
 	private final Builder builder;
 
 	private List<MideaasEditorPlugin> plugins;
 
 	private CloseHandler closeHandler;
 
 	private boolean testingEnabled;
 
 	public MideaasEditor(User user, SharedProject project, UserSettings settings, List<MideaasEditorPlugin> plugins) {
 		super();
 
 		this.project = project;
 		this.user = user;
 		this.settings = settings;
 		this.plugins = plugins;
 
 		builder = new Builder(project,settings);
 		componentTree = new ProjectItemList(project, user);
 		
 		layout.setSizeFull();
 		setCompositionRoot(layout);
 	}
 	
 	public void setCloseHandler(CloseHandler ha) {
 		if (menu!=null && closeHandler==null) {
 			addCloseMenu();
 		}
 		this.closeHandler = ha;
 	}
 
 	@Override
 	public void attach() {
 		super.attach();
 		createLayout();
 
 		componentTree.addComponentListener(this);
 
 	}
 	
 	@Override
 	public void detach() {
 		project.removeDiffering(user);
 		
 		super.detach();
 	}
 
 	private void createLayout() {
 		setSizeFull();
 
 		buildComponent = new BuildComponent(builder, user, settings);
 
 		initMenuBar();
 
 		layout.addComponent(menu);
 
 		split = new HorizontalSplitPanel();
 		split.setSizeFull();
 		layout.addComponent(split);
 		layout.setExpandRatio(split, 1);
 
 		sideBar = new VerticalLayout();
 		sideBar.setMargin(true);
 		split.setFirstComponent(sideBar);
 
 		sideBar.addComponent(componentTree);
 		sideBar.addComponent(buildComponent);
 		//adds delpoy component to view
 		if (settings.paasDeployTurnedOn){
			sideBar.addComponent(new DeployComponent(project, settings));
 		}
 		
 		sideBar.addComponent(new JettyComponent(project, user));
 
 		ChatBox cb = new ChatBox(project.getChat());
 		cb.setUser(new ChatUser(user.getUserId(), user.getName(), "user1"));
 		cb.setWidth("100%");
 		cb.setShowSendButton(false);
 		sideBar.addComponent(new Panel("Chat", cb));
 
 		split.setSplitPosition(20.0f);
 
 		setActiveFile(project.getFile("App.java"));
 
 		//adds new menuitem (that does not do anything)
 		/*if (settings.easiCloudsFeaturesTurnedOn){
 			MenuItem root = this.menu.addItem("Deploy to...",null);
 			MenuItem item = root.addItem("Jetty", null);
 			item.setCheckable(true);
 			item.setChecked(true);
 			item = root.addItem("Gae", null);
 			item.setCheckable(true);
 			item.setChecked(false);
 		}*/
 		
 	}
 
 	private void initMenuBar() {
 		menu = new MenuBar();
 		String buildDir = MavenUtil.targetDirFor(user);
 //		MenuBarUtil.addBuildMenu(menu, builder, buildDir);
 		MenuBarUtil.addAddonMenu(menu, project);
 		MenuBarUtil.addPanicMenu(menu, project, builder, buildDir);
 		for (MideaasEditorPlugin plugin : plugins) {
 			plugin.extendMenu(menu);
 		}
 		
 		if (closeHandler!=null) {
 			addCloseMenu();
 		}
 	}
 	
 	private void addCloseMenu() {
 		menu.addItemBefore("Close", null, new Command() {
 			@Override
 			public void menuSelected(MenuItem selectedItem) {
 				closeHandler.closeRequested(project);
 			}}, menu.getItems().get(0) /* assuming at least one item */);
 	}
 
 	private void setActiveComponent(SharedView component) {
 		activeEditor = new MideaasComponentEditor(user, component, project);
 		((MideaasComponentEditor) activeEditor).setTestingEnabled(testingEnabled);
 		split.setSecondComponent(activeEditor);
 		activeComponentName = component.getName();
 	}
 
 	@Override
 	public void componentSelected(String name) {
 		if (!name.equals(activeComponentName)) {
 			setActiveComponent(project.getView(name));
 		}
 
 	}
 
 	@Override
 	public void javaFileSelected(String name) {
 		if (!name.equals(activeComponentName)) {
 			ProjectFile f = project.getFile(name);
 			if (f!=null) {
 				setActiveFile(f);
 			}
 			else {
 				System.err.println("WARNING: " + name + " doesn't exist!");
 			}
 		}
 	}
 
 
 	private void setActiveFile(ProjectFile f) {
 		MultiUserEditor ed = f.createEditor(user);
 		if (f.getName().endsWith(".java")) {
 			String cls = f.getName().substring(0, f.getName().length()-5);
 			String fullCls = project.getPackageName() + "." + cls;
 			JavaErrorChecker checker = new JavaErrorChecker(fullCls, project.getCompiler());
 			ed.setErrorChecker(checker);
 			JavaSuggester sugger = new JavaSuggester(project.getCompiler().getInMemoryCompiler(), fullCls, f.getMud());
 			ed.setSuggestionExtension(new SuggestionExtension(sugger));
 		}
 
 		ed.setSizeFull();
 		activeEditor = ed;
 		split.setSecondComponent(activeEditor);
 		activeComponentName = f.getName();
 	}
 
 	public void setTestingEnabled(boolean enabled) {
 		testingEnabled = enabled;
 	}
 
 }
 
