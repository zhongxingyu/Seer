 /*
  * Created on Dec 31, 2004
  *
  * TODO To change the template for this generated file go to
  * Window - Preferences - Java - Code Style - Code Templates
  */
 package cc.warlock.rcp.wizards;
 
 import java.lang.reflect.InvocationTargetException;
 import java.util.Collection;
 import java.util.Map;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.jface.operation.IRunnableWithProgress;
 import org.eclipse.jface.viewers.DoubleClickEvent;
 import org.eclipse.jface.viewers.IDoubleClickListener;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.IStructuredContentProvider;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.LabelProvider;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.jface.wizard.IWizardPage;
 import org.eclipse.jface.wizard.WizardPage;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Table;
 
 import cc.warlock.configuration.Account;
 import cc.warlock.configuration.Profile;
 import cc.warlock.configuration.SavedProfiles;
 import cc.warlock.network.SGEConnection;
 import cc.warlock.network.SGEConnectionListener;
 import cc.warlock.rcp.ui.WarlockSharedImages;
 import cc.warlock.rcp.ui.network.SWTSGEConnectionListenerAdapter;
 
 /**
  * @author Marshall
  *
  * TODO To change the template for this generated type comment go to
  * Window - Preferences - Java - Code Style - Code Templates
  */
 public class GameSelectWizardPage extends WizardPage {
 
 	private Table games;
 	private Map<String, String> gameMap;
 	private SGEConnection connection;
 	private TableViewer gamesViewer;
 	private String selectedGameCode;
 	private Listener listener;
 	
 	private static String[] gameFilterCodes = new String[] {
 		"CS", "DRDT", "GS4D", "HXD", "MOD"
 	};
 	
 	public GameSelectWizardPage (SGEConnection connection)
 	{
 		super ("Select a game.", "Please select a game.", WarlockSharedImages.getImageDescriptor(WarlockSharedImages.IMG_WIZBAN_WARLOCK));
 		this.connection = connection;
 		
 		listener = new Listener();
 		connection.addSGEConnectionListener(new SWTSGEConnectionListenerAdapter(listener));
 		
 		System.out.println("GameSelectWizardPage constructor");
 	}
 	
 	public void createControl(Composite parent) {
 		// TODO Auto-generated method stub
 
 		Composite controls = new Composite(parent, SWT.NONE);
 		FillLayout layout = new FillLayout();
 		controls.setLayout(layout);
 		
 		games = new Table(controls, SWT.BORDER | SWT.SINGLE);
 		games.setEnabled(false);
 		
 		gamesViewer = new TableViewer (games);
 		gamesViewer.setContentProvider(new GameContentProvider());
 		gamesViewer.setLabelProvider(new GameLabelProvider());
 		gamesViewer.addSelectionChangedListener(new ISelectionChangedListener() {
 			public void selectionChanged(SelectionChangedEvent event) {
 				gameSelected(event.getSelection());
 			}
 		});
 		gamesViewer.addDoubleClickListener(new IDoubleClickListener() {
 			public void doubleClick(DoubleClickEvent event) {
 				gameSelected(gamesViewer.getSelection());
 				
 				IWizardPage nextPage = getWizard().getNextPage(GameSelectWizardPage.this);
 
 				nextPage.setVisible(true);
 				setVisible(false);
 			}
 		});
 		setControl(controls);
 	}
 
 	private class GameContentProvider implements IStructuredContentProvider
 	{
 		
 		public Object[] getElements(Object inputElement) {
 			Map<String,String> gameMap = (Map<String,String>) inputElement;
 			return gameMap.keySet().toArray();
 		}
 		
 		public void dispose() {
 			// TODO Auto-generated method stub
 		}
 		
 		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
 			// TODO Auto-generated method stub
 		}
 }
 	
 	private class GameLabelProvider extends LabelProvider
 	{
 		public Image getImage(Object element) {
 			return WarlockSharedImages.getImage(WarlockSharedImages.IMG_GAME);
 		}
 		
 		public String getText(Object element) {
 			return (String) gameMap.get(element);
 		}
 	}
 	
 	private void gameSelected (ISelection selection)
 	{
 		if (selection instanceof IStructuredSelection)
 		{
 			IStructuredSelection selection2 = (IStructuredSelection) selection;
 			selectedGameCode = (String) selection2.getFirstElement();
 		}
 	}
 	
 	public void setVisible (boolean visible) {
 		super.setVisible(visible);
 		
		if (!visible && gameMap != null) {
 			try {
 				getContainer().run(true, true, new IRunnableWithProgress() {
 					public void run(IProgressMonitor monitor)
 						throws InvocationTargetException, InterruptedException
 					{
 						listener.setProgressMonitor(monitor);
 						
 						monitor.beginTask("Finding characters in \"" + gameMap.get(selectedGameCode) + "\"...", 2);
 						connection.selectGame(selectedGameCode);
 						monitor.worked(1);
 					}
 				});
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 		
 		setPageComplete(true);
 	}
 	
 	private class Listener extends SGEConnectionListener {
 		private IProgressMonitor monitor;
 		
 		public void setProgressMonitor (IProgressMonitor monitor)
 		{
 			this.monitor = monitor;
 		}
 		
 		public void gamesReady(SGEConnection connection, Map games) {
 			GameSelectWizardPage.this.gameMap = games;
 			
 			for (String gameCode : gameFilterCodes)
 			{
 				GameSelectWizardPage.this.gameMap.remove(gameCode);
 			}
 			
 			GameSelectWizardPage.this.games.setEnabled(true);
 			gamesViewer.setInput(gameMap);
 			
 			Account savedAccount = ((AccountWizardPage)getPreviousPage()).getSavedAccount();
 			if (savedAccount != null)
 			{
 				Collection<Profile> savedProfiles = SavedProfiles.getProfiles(savedAccount);
 			}
 		}
 
 		public void charactersReady(SGEConnection connection, Map characters) {
 			if (monitor != null)
 			{
 				monitor.worked(1);
 				monitor.done();
 			}
 		}
 	}
 
 	public String getSelectedGameCode() {
 		return selectedGameCode;
 	}
 	
 	public String getSelectedGameName() {
 		return gameMap.get(selectedGameCode);
 	}
 	
 }
