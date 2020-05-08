 package com.thingtrack.konekti.view.web.workbench;
 
 import java.util.List;
 
 import org.vaadin.addon.formbinder.ViewBoundForm;
 
 import com.thingtrack.konekti.domain.User;
 import com.thingtrack.konekti.service.security.SecurityService;
 import com.thingtrack.konekti.view.addon.ui.AbstractView;
 import com.thingtrack.konekti.view.addon.ui.SliderView;
 import com.thingtrack.konekti.view.kernel.ui.layout.IPanelView;
 import com.thingtrack.konekti.view.kernel.ui.layout.IToolbar;
 import com.thingtrack.konekti.view.security.LoginViewForm;
 import com.vaadin.data.util.BeanItem;
 import com.vaadin.event.ShortcutAction.KeyCode;
 import com.vaadin.terminal.UserError;
 import com.vaadin.ui.Alignment;
 import com.vaadin.ui.Button;
 import com.vaadin.ui.HorizontalLayout;
 import com.vaadin.ui.VerticalLayout;
 import com.vaadin.ui.Window;
 
 @SuppressWarnings("serial")
 public class SecurityAccessView extends AbstractView {
 
 	private SecurityService securityService;
 	
 	private SliderView sliderView;
 	
 	private ViewBoundForm viewBoundForm;
 	private Window loginWindow;
 	private LoginViewForm loginViewForm;
 	private VerticalLayout mainLayout;
 	
 	private User grantedUser; 
 
 	private String version;
 	private String logo;
 	private boolean demo;
 	
 	private static final String DEMO_USERNAME = "demo";
 	private static final String DEMO_PASSWORD = "demo";
 	
 	public User getGrantedUser() {
 		return grantedUser;
 	}
 
 	public SecurityAccessView(SecurityService securityService,
 			SliderView sliderView, String version, String logo, boolean demo) {
 		
 		this.securityService = securityService;
 		this.sliderView = sliderView;
 		this.version = version;
 		this.logo = logo;
 		this.demo = demo;
 		
 		mainLayout = new VerticalLayout();
 		setCompositionRoot(mainLayout);
 	}
 
 	@Override
 	public void attach() {
 
 		super.attach();
 
 		loginWindow = new Window();
 		
 		// create view bound form
 		VerticalLayout content = buildMainLayout();
 		content.setMargin(false);
 		loginWindow.setContent(content);
 		loginWindow.center();
 		loginWindow.setCaption("Entrar al sistema");
 		loginWindow.setModal(true);
 		loginWindow.setResizable(false);
 		loginWindow.setClosable(false);
 
 		User loginUser = new User();
 		BeanItem<User> userBean = new BeanItem<User>(loginUser);
 		viewBoundForm.setItemDataSource(userBean);
 		
 		getWindow().addWindow(loginWindow);
 	}
 
 	private VerticalLayout buildMainLayout() {
 
 		// common part: create layout
 		VerticalLayout mainLayout = new VerticalLayout();
 		mainLayout.setImmediate(false);
 		mainLayout.setSizeUndefined();
 		mainLayout.setMargin(false);
 
 		viewBoundForm = buildLoginLayout();
 
 		mainLayout.addComponent(viewBoundForm);
 		mainLayout.setExpandRatio(viewBoundForm, 1.0f);
 
 		return mainLayout;
 
 	}
 
 	private ViewBoundForm buildLoginLayout() {
 
 		viewBoundForm = new ViewBoundForm();
 		viewBoundForm.setImmediate(false);
 		viewBoundForm.setWidth("100%");
 		viewBoundForm.setHeight("100%");
 		viewBoundForm.setWriteThrough(false);
 		viewBoundForm.setInvalidCommitted(false);
 
 		loginViewForm = new LoginViewForm(version, logo, demo);
 		viewBoundForm.setContent(loginViewForm);
 
 		viewBoundForm.getFooter().addComponent(buildFooterWindow());
 		viewBoundForm.getFooter().addStyleName("windowdialog-footer");
 
 		return viewBoundForm;
 	}
 
 	private HorizontalLayout buildFooterWindow() {
 
 		HorizontalLayout footerWindow = new HorizontalLayout();
 		footerWindow.setImmediate(false);
 		footerWindow.setWidth(loginViewForm.getWidth(), UNITS_PIXELS);
 		footerWindow.setHeight("-1px");
 		footerWindow.setMargin(true);
 		footerWindow.setSpacing(true);
 
 		Button loginBtn = new Button("Entrar", this, "loginBtn_Click");
 		loginBtn.setImmediate(true);
 		loginBtn.setWidth("-1px");
 		loginBtn.setHeight("-1px");
 		loginBtn.setClickShortcut(KeyCode.ENTER);
 		loginBtn.addStyleName("primary");
 		footerWindow.addComponent(loginBtn);
 		footerWindow.setComponentAlignment(loginBtn, new Alignment(34));
 
 		return footerWindow;
 	}
 
 	/**
 	 * Login result button
 	 */
 	public void loginBtn_Click(Button.ClickEvent event) {
 		String userName ;
 		String password;
 		
 		try{
 			viewBoundForm.commit();
 			BeanItem<User> userBean = (BeanItem<User>) viewBoundForm.getItemDataSource();
 			
 			if (demo) {
 				userName = DEMO_USERNAME;
 				password = DEMO_PASSWORD;
 			}
 			else {
 				userName = userBean.getBean().getUsername();
 				password = userBean.getBean().getPassword();
 			}
 			
 			grantedUser = securityService.authenticate(userName, password);
 			getWindow().removeWindow(loginWindow);
			
 			sliderView.rollNext();
 			
 		}catch(Exception e){
 			
 			viewBoundForm.setComponentError(new UserError("credenciales erróneos"));
 		}
 
 	}
 
 	@Override
 	public void addPanelView(IPanelView arg0) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void addToolbar(IToolbar arg0) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public List<IPanelView> getPanelView() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public List<IToolbar> getToolbars() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public void removeAllPanelView() {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void removeAllToolbar() {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void removePanelView(IPanelView arg0) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void removeToolbar(IToolbar arg0) {
 		// TODO Auto-generated method stub
 
 	}
 
 }
