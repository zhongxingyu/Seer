 package scstool.proc;
 
 import java.awt.BorderLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 
 import javax.swing.JComponent;
 import javax.swing.JFileChooser;
 
 import org.xml.sax.SAXException;
 
 import scstool.gui.MainMenu;
 import scstool.gui.MainView;
 import scstool.gui.StatusMessageEvent;
 import scstool.gui.StatusMessageEventMulticaster;
 import scstool.gui.TabbedPaneView;
 import scstool.gui.comp.StatusPane;
 import scstool.utils.IController;
 import scstool.utils.Repository;
 
 public class GuiController implements IController
 {
 	private static final String DATABASEXML = "/database.xml";
 	
 
 	private MainView view;
 	private TabbedPaneView tView;
 	private StatusPane sPane; 
 	private StatusMessageEventMulticaster multicaster;
 
 	
 	public GuiController()
 	{
 		init();
 	}
 	
 	private void init()
 	{
 		multicaster = StatusMessageEventMulticaster.getInstance();
 		sPane = new StatusPane();
 		view = new MainView();
 		view.setMenuListener(new MenuListener());
 		view.add(sPane,BorderLayout.PAGE_END);
 		view.setButtonListener(new ButtonListener());
 		try {
			URL url = this.getClass().getResource(DATABASEXML);
			ImportXmlController.readXml(new File(url.getFile()), DatabaseContentHandler.get());
 		} catch (SAXException | IOException e) {
 			throw new RuntimeException("Can't find Database.xml.");
 		}
 		
 		//int data containers
 		Repository.getInstance();
 		multicaster.add(sPane);
 	
 	}
 	
 	
 	@Override
 	public void addListener(ActionListener l) {
 		//not used
 		
 	}
 
 	@Override
 	public void removeListener(ActionListener l) {
 		//not used
 		
 	}
 
 	@Override
 	public void showView() 
 	{
 		this.view.setVisible(true);	
 	}
 	
 	public MainView getView()
 	{
 		return this.view;
 	}
 	
 	public void addContent(JComponent c)
 	{
 		
 		view.add(c);
 		view.invalidate();
 		view.validate();
 	}
 	
 	class MenuListener implements ActionListener
 	{
 
 		@Override
 		public void actionPerformed(ActionEvent e) 
 		{
 			StatusSingleton stat = StatusSingleton.get();
 			switch(e.getActionCommand())
 			{
 				case MainMenu.MENU_EXIT:
 					System.exit(0);
 					break;
 				case MainMenu.MENU_IMPORTXML:
 					ImportXmlController ico = new ImportXmlController(view);
 					ico.openDialog();
 
 					break;
 				case MainMenu.MENU_USER_INPUT:
 					multicaster.setStatusMessage(new StatusMessageEvent(this, ""));
 					if(stat.isInputXmlLoaded())
 					{
 						view.removePanel();
 						UserInputController pco = new UserInputController(view);
 						addContent(pco.getView());
 					}
 					break;
 			}
 		}
 		
 	}
 	class ButtonListener implements ActionListener
 	{
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			StatusSingleton stat = StatusSingleton.get();
 			switch(e.getActionCommand())
 			{
 				case "IMP":
 					ImportXmlController ico = new ImportXmlController(view);
 					int ret = ico.openDialog();
 					if(JFileChooser.APPROVE_OPTION == ret)
 					{
 						if(stat.isInputXmlLoaded())
 						{
 							view.removePanel();
 							UserInputController pco = new UserInputController(view);
 							tView = pco.getView();
 							tView.addBackListener(this);
 							addContent(tView);
 						}
 					}
 					break;
 				case "START":
 					view.remove(tView);
 					view.addPanel();
 					view.revalidate();
 					view.repaint();
 					multicaster.setStatusMessage(new StatusMessageEvent(this, ""));
 					stat.resetAll();
 					
 					break;
 					
 			}	
 		}
 	}
 }
