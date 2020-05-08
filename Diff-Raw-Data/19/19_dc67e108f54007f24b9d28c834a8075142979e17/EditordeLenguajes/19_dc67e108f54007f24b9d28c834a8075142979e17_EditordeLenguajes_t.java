 package lector.client.admin.langedit;
 
 import lector.client.admin.langedit.tabs.AnnotationEditor;
 import lector.client.admin.langedit.tabs.BrowserEditor;
 import lector.client.admin.langedit.tabs.FilterEditor;
 import lector.client.admin.langedit.tabs.MainWindowEditor;
 import lector.client.admin.langedit.tabs.SpecificationsEditor;
 import lector.client.admin.langedit.tabs.VariosEditor;
 import lector.client.controler.Controlador;
 import lector.share.model.Language;
 
 import com.google.gwt.core.client.EntryPoint;
 import com.google.gwt.user.client.ui.RootPanel;
 import com.google.gwt.user.client.ui.MenuBar;
 import com.google.gwt.user.client.ui.DecoratedTabPanel;
 import com.google.gwt.user.client.ui.MenuItem;
 import com.google.gwt.user.client.ui.Widget;
 import com.google.gwt.user.client.Command;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.MenuItemSeparator;
 import com.google.gwt.event.logical.shared.BeforeSelectionHandler;
 import com.google.gwt.event.logical.shared.BeforeSelectionEvent;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.user.client.ui.HasHorizontalAlignment;
 import com.google.gwt.user.client.ui.HasVerticalAlignment;
 import com.google.gwt.user.client.ui.DockPanel;
 import com.google.gwt.user.client.ui.DockLayoutPanel;
 import com.google.gwt.dom.client.Style.Unit;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 
 public class EditordeLenguajes implements EntryPoint {
 
 	//welcome
 
 	
 	
 	private static Language LenguajeActual;
 	private DecoratedTabPanel decoratedTabPanel;
 	private MenuItem mntmNewItem;
 	
 	
 	public void onModuleLoad() {
 		RootPanel RootTXOriginal = RootPanel.get();
 		RootPanel RootMenu = RootPanel.get("Menu");
 		RootTXOriginal.setSize("100%", "100%");
 		RootTXOriginal.setStyleName("Root");
 								
 								MenuBar menuBar = new MenuBar(false);
 								RootMenu.add(menuBar);
 								menuBar.setSize("100%", "25px");
 								
 								mntmNewItem = new MenuItem("New item", false, (Command) null);
 								mntmNewItem.setEnabled(false);
 								mntmNewItem.setHTML("Language Editor : " + LenguajeActual.getName());
 								menuBar.addItem(mntmNewItem);
 								
 								MenuItemSeparator separator = new MenuItemSeparator();
 								menuBar.addSeparator(separator);
 								
								MenuItem mntmNewItem_2 = new MenuItem("Main Window", false, new Command() {
 									public void execute() {
 										int actual_widget=decoratedTabPanel.getDeckPanel().getVisibleWidget();
 										Widget WidgetActual=decoratedTabPanel.getDeckPanel().getWidget(actual_widget);
 										{
 											((PanelDecorador)WidgetActual).saveAll();
 											
 										}
 										
 									}
 
 
 								});
 								mntmNewItem_2.setHTML("Save");
 								menuBar.addItem(mntmNewItem_2);
 								
 								MenuItemSeparator separator_1 = new MenuItemSeparator();
 								menuBar.addSeparator(separator_1);
 								
 								MenuItem mntmNewItem_1 = new MenuItem("New item", false, new Command() {
 									public void execute() {
 										int actual_widget=decoratedTabPanel.getDeckPanel().getVisibleWidget();
 										//	Window.alert(Integer.toString(actual_widget2));
 										//	int actual_widget=event.getItem();
 											//Window.alert(Integer.toString(actual_widget));
 										if (actual_widget>-1){
 											Widget WidgetActual=((HorizontalPanel)decoratedTabPanel.getDeckPanel().getWidget(actual_widget)).getWidget(0);														
 											if (!((PanelDecorador)WidgetActual).isSaved())
 												if (Window.confirm("The tab is modificated and not saved, Do you want to save the modification"))
 													{
 													((PanelDecorador)WidgetActual).saveAll();
 													}
 											Controlador.change2AdminLenguaje();
 									}
 									}
 								});
 								mntmNewItem_1.setHTML("Back");
 								menuBar.addItem(mntmNewItem_1);
 								
 								VerticalPanel verticalPanel = new VerticalPanel();
 								RootTXOriginal.add(verticalPanel,0,30);
 								verticalPanel.setStyleName("fondoLogo");
 								verticalPanel.setSize("100%", "95%");
 										
 										decoratedTabPanel = new DecoratedTabPanel();
 										verticalPanel.add(decoratedTabPanel);
 										decoratedTabPanel.setSize("100%", "100%");
 										
 													
 													
 													
 													//Ollente para pasar de pestaa
 													decoratedTabPanel.addBeforeSelectionHandler(new BeforeSelectionHandler<Integer>() {
 														public void onBeforeSelection(BeforeSelectionEvent<Integer> event) {
 															int actual_widget=decoratedTabPanel.getDeckPanel().getVisibleWidget();
 														//	Window.alert(Integer.toString(actual_widget2));
 														//	int actual_widget=event.getItem();
 														//	Window.alert(Integer.toString(actual_widget));
 															if (actual_widget>-1){
 															Widget WidgetActual=((HorizontalPanel)decoratedTabPanel.getDeckPanel().getWidget(actual_widget)).getWidget(0);														
 															if (!((PanelDecorador)WidgetActual).isSaved())
 																if (Window.confirm("The tab is modificated and not saved, Do you want to save the modification"))
 																	((PanelDecorador)WidgetActual).saveAll();
 															}
 																
 														}
 													});
 													
 													HorizontalPanel horizontalPanel = new HorizontalPanel();
 													horizontalPanel.setSpacing(10);
 													horizontalPanel.setStyleName("AzulTransparente");
													decoratedTabPanel.add(horizontalPanel, "Main Window", false);
 													horizontalPanel.setSize("100%", "");
 													
 													//Esta encima porque sino salta error al setear la tab del decorador.
 													MainWindowEditor MWE = new MainWindowEditor(LenguajeActual);
 													horizontalPanel.add(MWE);
 													MWE.setStyleName("BlancoTransparente");
 													
 													
 													
 													HorizontalPanel horizontalPanel_1 = new HorizontalPanel();
 													horizontalPanel_1.setStyleName("AzulTransparente");
 													horizontalPanel_1.setSpacing(10);
 													decoratedTabPanel.add(horizontalPanel_1, "Specifications", false);
 													horizontalPanel_1.setSize("100%", "");
 													
 													SpecificationsEditor Specifications= new SpecificationsEditor(LenguajeActual);
 													horizontalPanel_1.add(Specifications);
 													Specifications.setStyleName("BlancoTransparente");
 													
 													HorizontalPanel horizontalPanel_2 = new HorizontalPanel();
 													horizontalPanel_2.setStyleName("AzulTransparente");
 													decoratedTabPanel.add(horizontalPanel_2, "Filter", false);
 													horizontalPanel_2.setSize("100%", "");
 													
 													FilterEditor Filter= new FilterEditor(LenguajeActual);
 													horizontalPanel_2.add(Filter);
 													Filter.setStyleName("BlancoTransparente");
 													
 													HorizontalPanel horizontalPanel_3 = new HorizontalPanel();
 													horizontalPanel_3.setStyleName("AzulTransparente");
 													decoratedTabPanel.add(horizontalPanel_3, "Annotation", false);
 													horizontalPanel_3.setSize("100%", "");
 													
 													AnnotationEditor Anotation= new AnnotationEditor(LenguajeActual);
 													horizontalPanel_3.add(Anotation);
 													Anotation.setStyleName("BlancoTransparente");
 															
 															HorizontalPanel horizontalPanel_4 = new HorizontalPanel();
 															horizontalPanel_4.setStyleName("AzulTransparente");
 															horizontalPanel_4.setSpacing(10);
 															decoratedTabPanel.add(horizontalPanel_4, "Browser", false);
 															horizontalPanel_4.setSize("100%", "");
 													
 															BrowserEditor Browser= new BrowserEditor(LenguajeActual);
 															horizontalPanel_4.add(Browser);
 															Browser.setStyleName("BlancoTransparente");
 															
 															HorizontalPanel horizontalPanel_5 = new HorizontalPanel();
 															horizontalPanel_5.setStyleName("AzulTransparente");
 															horizontalPanel_5.setSpacing(10);
 															decoratedTabPanel.add(horizontalPanel_5, "Others", false);
 															horizontalPanel_5.setSize("100%", "");
 															
 															VariosEditor varios= new VariosEditor(LenguajeActual);
 															horizontalPanel_5.add(varios);
 															varios.setStyleName("BlancoTransparente");
 		
 		
 															//Debajo de la declaracion sino da IndexOutOfBounds
 															decoratedTabPanel.selectTab(0);
 }
 	public static Language getLenguajeActual() {
 		return LenguajeActual;
 	}
 	
 	public static void setLenguajeActual(Language lenguajeActual) {
 		LenguajeActual = lenguajeActual;
 	}
 	
 	
 	
 	
 	
 }
