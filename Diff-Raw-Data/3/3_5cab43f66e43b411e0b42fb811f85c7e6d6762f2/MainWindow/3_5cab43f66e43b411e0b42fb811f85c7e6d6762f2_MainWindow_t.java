 package org.effortless.ui.windows;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.effortless.core.GlobalContext;
 import org.effortless.core.ObjectUtils;
 import org.effortless.model.Entity;
 import org.effortless.ui.Message;
 import org.effortless.ui.Relocatable;
 import org.effortless.ui.Relocator;
 import org.effortless.ui.ViewContext;
import org.effortless.ui.impl.CteEvents;
 import org.effortless.ui.layouts.LayoutGrid;
 import org.effortless.ui.listeners.MainCtrl;
 import org.effortless.ui.widgets.AbstractComponent;
 import org.effortless.ui.widgets.AbstractField;
 import org.effortless.ui.widgets.Breadcrumb;
 import org.effortless.ui.widgets.BreadcrumbItem;
 import org.effortless.ui.widgets.Field;
 import org.effortless.ui.widgets.Menu;
 import org.zkoss.addon.fluidgrid.Rowchildren;
 import org.zkoss.zk.ui.Component;
 import org.zkoss.zk.ui.Session;
 import org.zkoss.zk.ui.Sessions;
 import org.zkoss.zk.ui.annotation.ComponentAnnotation;
 import org.zkoss.zk.ui.event.Event;
 import org.zkoss.zk.ui.event.EventListener;
 import org.zkoss.zk.ui.event.Events;
 import org.zkoss.zk.ui.select.annotation.Listen;
 import org.zkoss.zk.ui.select.annotation.Wire;
 import org.zkoss.zk.ui.util.Template;
 import org.zkoss.zul.Button;
 import org.zkoss.zul.Div;
 import org.zkoss.zul.Hlayout;
 import org.zkoss.zul.Image;
 import org.zkoss.zul.Include;
 import org.zkoss.zul.Label;
 import org.zkoss.zul.Style;
 import org.zkoss.zul.Vlayout;
 import org.zkoss.zul.West;
 import org.zkoss.zul.Window;
 
 public class MainWindow extends AbstractWindow implements Relocator {
 
 //    <hlayout height="100%" id="wHeader">
 	@Wire
 	protected Hlayout wHeader;
 
 	@Wire
 	protected Vlayout wLeft;
 	
 	@Wire
     protected Hlayout wTopMenu;
 
 	@Wire
 	protected Style wStyle;//	  <style id="wStyle" src="${resources}/styles/main.css.dsp" />
 	
 	@Wire
 	protected MsgWindow wMessage;//    <msg-window id="wMsg" message="@load(vm.msg)" />
 	
 	@Wire
 	protected ListWindows wListWindows;
 
 	@Wire
 	protected Breadcrumb wBreadcrumb;
 	
 	@Wire
 	protected Image wLogo;//    <image id="wLogo" src="${images}/main/main_logo.png" />
 
 	@Wire
 	protected West wWest;//	<west id="wWest" title="${i18n.app_title}" width="250px" border="0" splittable="true" collapsible="true" margins="0,5,0,0" autoscroll="true">
 
 	@Wire
 	protected Label wFooterMsg;//    <label id="wFooterMsg" value="@load(vm.footerMsg)" />
 	
 	@Wire
 	protected Div wContent;
 	
 	
 	public MainWindow () {
 		super();
 		ViewContext.setMainWindow(this);
 	}
 
 	protected void initiate () {
 		super.initiate();
 		
 		initiateLayoutMenu();
 		initiateMessage();
 	}
 	
     protected void initUi () {
     	super.initUi();
     	initUi_Wnd();
     }
     
     protected Message message;
     
     protected void initiateMessage () {
     	this.message = null;
     }
     
     public Message getMessage () {
     	return this.message;
     }
     
     public void setMessage (Message newValue) {
     	Message oldValue = this.message;
     	if (!ObjectUtils.equals(oldValue, newValue)) {
     		this.message = newValue;
     		_onChangeMessage();
     	}
     }
 
     protected void _onChangeMessage() {
     	this.wMessage.setMessage(getMessage());
 	}
     
     protected String footerMsg;
     
     protected void initiateFooterMsg () {
     	this.footerMsg = null;
     }
     
     public String getFooterMsg () {
     	return this.footerMsg;
     }
     
     public void setFooterMsg (String newValue) {
     	String oldValue = this.footerMsg;
     	if (!ObjectUtils.equals(oldValue, newValue)) {
     		this.footerMsg = newValue;
     		_onChangeFooterMsg();
     	}
     }
 
 	protected void _onChangeFooterMsg() {
 		String footerMsg = getFooterMsg();
 //		footerMsg = (footerMsg != null ? footerMsg : "");
 		if (footerMsg != null) {
 			this.wFooterMsg.setValue(footerMsg);
 		}
 		this.wFooterMsg.setVisible(footerMsg != null);
 	}
 
 	protected void initUi_Wnd () {
     	//<window id="wnd" width="100%" height="100%" contentStyle="overflow:auto" border="normal">
     	this.setWidth("100%");
     	this.setHeight("100%");
     	this.setContentStyle("overflow:auto");
     	this.setBorder("normal");
     	
     	this.wStyle.setSrc(_res("styles/main.css.dsp"));
     	
     	_onChangeMessage();
     	this.wLogo.setSrc(_images("main/main_logo.png"));
     	this.wWest.setTitle(ViewContext.i18n("app_title"));
     	_onChangeFooterMsg();
     }
     
 	
     protected boolean doInsertBefore (Component newChild, Component refChild) {
     	boolean result = false;
     	
     	if (newChild != null) {
     		result = this.wTopMenu.insertBefore(newChild, refChild);
     		if (newChild instanceof Relocatable) {
     			Relocatable relocatable = (Relocatable)newChild;
     			relocatable.setRelocator(this);
     			relocatable.setPosition(Integer.valueOf(1));
     		}
 //    		if (newChild instanceof Menu) {
 //    			Menu menu1 = (Menu)newChild;
 //    			menu1.setParentLevel2(this.wLeft);
 //    			menu1.setParentLevel3(this.wContent);
 //	    		List<Component> level2 = menu1.getSubitems();//(newChild != null ? newChild.getChildren() : null);
 //	    		if (level2 != null ) {
 //	    			for (Component childLevel2 : level2) {
 //	    				if (childLevel2 != null) {
 //	        				this.wLeft.appendChild(childLevel2);
 //	        				if (childLevel2 instanceof Menu) {
 //	        					Menu menu2 = (Menu)childLevel2;
 //		        				List<Component> level3 = menu2.getSubitems();//childLevel2.getChildren();
 //		        				if (level3 != null) {
 //		        					for (Component childLevel3 : level3) {
 //		        						this.wContent.appendChild(childLevel3);
 //		        					}
 //		        				}
 //	        				}
 //	    				}
 //	    			}
 //	    		}
 //    		}
     	}
 //    	if (newChild instanceof Menu) {
 //    		Menu menu = (Menu)newChild;
 //    		result = applyLayoutMenu(menu, refChild, 1);
 //    	}
 //    	else {
 //    		result = super.nativeInsertBefore(newChild, refChild);
 //    	}
 //    	return nativeInsertBefore(newChild, refChild);
     	return result;
     }
 
     protected boolean applyLayoutMenu(Menu menu, Component refChild, int level) {
     	boolean result = false;
     	if (menu != null) {
     		result = this.wTopMenu.insertBefore(menu, refChild);
     		
     		if (level == 2) {
 //    			this.wLeft.insertBefore(newChild, refChild);
     		}
     		List<Component> children = menu.getChildren();
     		if (children != null) {
     			for (Component itemNewChild : children) {
     				
         			this.wLeft.insertBefore(itemNewChild, null);
     			}
     		}
     		
     	}
 		return result;
 	}
     
     protected void processMenuOption (Menu menu) {
     	if (menu != null) {
     		menu.addEventListener(Events.ON_CLICK, _doGetClickMenuOptionListener());
     	}
     }
     
     protected EventListener _clickMenuOptionListener;
     
     protected EventListener _doGetClickMenuOptionListener () {
     	if (this._clickMenuOptionListener == null) {
     		final MainWindow _this = this;
     		this._clickMenuOptionListener = new EventListener () {
 
 				public void onEvent(Event event) throws Exception {
 					Menu menuOption = (Menu)event.getTarget();
 					_this.openOption(menuOption);
 				}
     			
     		};
     	}
     	return this._clickMenuOptionListener;
     }
 
 	public void openOption (Menu option) {
     	if (option != null) {
 	    	String src = option.getSrc();
 	    	Map<String, Object> args = option.getArgs();
 	    	openWindow(src, args);
 	    	addBreadcrumbItem(option);
     	}
     }
     
     protected void addBreadcrumbItem(Menu option) {
     	if (option != null && this.wBreadcrumb != null) {
     		String resumeLabel = option.getResumeLabel();
     		String resumeDescription = option.getResumeDescription();
     		String resumeImage = option.getResumeImage();
     		
     		BreadcrumbItem bi = new BreadcrumbItem();
     		bi.setLabel(resumeLabel);
     		bi.setDescription(resumeDescription);
     		bi.setImage(resumeImage);
     		bi.setValue(option);
 
     		this.wBreadcrumb.appendChild(bi);
     	}
 	}
 
 	public void addWindow (Component screen) {
 		if (this.wListWindows != null && screen != null) {
 			this.wListWindows.addScreen(screen);
 		}
 	}
 
 	public void removeWindow (Component screen) {
 		if (this.wListWindows != null && screen != null) {
 			this.wListWindows.removeScreen(screen);
 		}
 	}
 
 	public void removeAllWindows () {
 		if (this.wListWindows != null) {
 			this.wListWindows.removeAllScreens();
 		}
 	}
     
 	public void openWindow (String src, Map<String, Object> args) {
     	if (src != null) {
     		Include wInclude = new Include(src);
 			Set<String> keys = (args != null ? args.keySet() : null);
     		if (keys != null) {
     			for (String key : keys) {
     				Object keyValue = args.get(key);
     				wInclude.setDynamicProperty(key, keyValue);
     			}
     		}
     		this.wListWindows.addScreen(wInclude);
     	}
     }
 
     public void resetWindows () {
     	if (this.wListWindows != null) {
     		this.wListWindows.reset();
     	}
     }
 
     public static final String LAYOUT_MENU_THREE_LEVELS = "3levels";//level1=TOP,level2=LEFT,level3..=CONTENT
     public static final String LAYOUT_MENU_TWO_LEVELS = "2levels";//level1=TOP,level2=LEFT,level3..=POPUP
     public static final String LAYOUT_MENU_ONE_LEVEL = "1level";//level1=TOP,level2..=POPUP
 
     public static final String LAYOUT_MENU_DEFAULT = LAYOUT_MENU_THREE_LEVELS;
     
     protected String layoutMenu;
     
     protected void initiateLayoutMenu () {
     	this.layoutMenu = LAYOUT_MENU_DEFAULT;
     }
     
 	@ComponentAnnotation("@ZKBIND(ACCESS=both, SAVE_EVENT=onChangeLayoutMenu)")
     public String getLayoutMenu () {
     	return this.layoutMenu;
     }
     
     public void setLayoutMenu (String newValue) {
     	String oldValue = this.layoutMenu;
     	if (!ObjectUtils.equals(oldValue, newValue)) {
         	this.layoutMenu = newValue;
         	notifyChange("LayoutMenu");
         	_onChangeLayoutMenu();
     	}
     }
 
 	protected void _onChangeLayoutMenu() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	protected MainCtrl _mainMenuListener;
 	
 	protected MainCtrl doGetMainMenuListener () {
 		if (this._mainMenuListener == null) {
 			this._mainMenuListener = new MainCtrl();
 			this._mainMenuListener.setMainWindow(this);
 			ViewContext.setMainCtrl(this._mainMenuListener);
 		}
 		return this._mainMenuListener;
 	}
 	
 	@Override
 	public void relocate(Component cmp, Integer position) {
 		if (cmp != null && position != null) {
 			int level = position.intValue();
 			if (level < 2) {
 				this.wTopMenu.appendChild(cmp);
 			}
 			else if (level == 2) {
 //				cmp.detach();
 //				cmp.setParent(null);
 				this.wLeft.appendChild(cmp);
 //				this.wLeft.invalidate();
 //				this.wLeft.appendChild(new Label("TEXT" + this.wLeft.getChildren().size()));
 			}
 			else if (level > 2) {
 //				cmp.detach();
 //				cmp.setParent(null);
 				this.wContent.appendChild(cmp);
 //				this.wContent.invalidate();
 			}
 			if (cmp instanceof Menu) {
 //				cmp.addEventListener(Events.ON_SELECT, doGetMainMenuListener());
 				cmp.addEventListener(Events.ON_CLICK, doGetMainMenuListener());
 			}
 		}
 	}
 
 //	protected int loadLevel(Component cmp) {
 //		int result = 0;
 //		result = 2;
 //		// TODO Auto-generated method stub
 //		return result;
 //	}
 
 	@Override
 	public void beforeCompose() {
 		super.beforeCompose();
 		initLogin();
 	}
 
 	protected void initLogin() {
 		Session session = Sessions.getCurrent();
 
 		Object currentUser = (session != null ? session.getAttribute(GlobalContext.CURRENT_USER) : null);
 		if (currentUser != null) {
 //			String zul = "/" + appId + "/resources/main/main.zul";
 //			Component root = org.zkoss.zk.ui.Executions.createComponents(zul, null, null);
 //			root.setPage(page);
 		}
 		else {
 			LoginWindow loginWindow = new LoginWindow();
 			loginWindow.doEmbedded();
 			this.appendChild(loginWindow);
			loginWindow.addEventListener(CteEvents.ON_LOGIN, doGetMainMenuListener());
 			session.setAttribute(GlobalContext.CURRENT_USER, "LOGIN");
 		}
 	}
 	
 	
 }
