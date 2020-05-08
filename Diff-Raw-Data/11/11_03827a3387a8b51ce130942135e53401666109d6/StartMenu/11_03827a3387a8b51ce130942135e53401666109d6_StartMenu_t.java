 package org.promasi.client.gui.startmenu;
 
 import java.io.IOException;
 
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Decorations;
 import org.promasi.client.gui.company.Project;
 import org.promasi.client.gui.marketplace.MarketPlaceComposite;
 import org.promasi.client.gui.scheduler.SchedulerComposite;
 import org.promasi.game.IGame;
 import org.promasi.game.company.Company;
 import org.promasi.game.company.MarketPlace;
 import org.promasi.utilities.exceptions.NullArgumentException;
 import org.promasi.utilities.file.RootDirectory;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.browser.Browser;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.ProgressBar;
 
 public class StartMenu extends Composite {
 
 	/**
 	 * 
 	 */
 	private ProgressBar _progressBar;
 	
 	/**
 	 * 
 	 */
 	private MarketPlaceComposite _marketPlaceComposite;
 	
 	/**
 	 * 
 	 */
 	private Decorations _marketPlaceDecorations;
 	
 	/**
 	 * 
 	 */
 	private SchedulerComposite _schedulerComposite;
 	
 	/**
 	 * 
 	 */
 	private Decorations _schedulerDecorations;
 	
 	/**
 	 * 
 	 */
 	private Project _currentProject;
 	
 	/**
 	 * 
 	 */
 	private Company _company;
 	
 	/**
 	 * 
 	 */
 	private MarketPlace _marketPlace;
 	
 	/**
 	 * 
 	 */
 	private IGame _game;
 	
 	/**
 	 * 
 	 */
 	private Composite _desktop;
 	
 	/**
 	 * Create the composite.
 	 * @param parent
 	 * @param style
 	 * @throws NullArgumentException 
 	 */
 	public StartMenu(Composite parent, int style, Composite desktop, Project project, MarketPlace marketPlace, Company company) throws NullArgumentException {
 		super(parent, SWT.BORDER);
 		
 		if(desktop==null){
 			throw new NullArgumentException("Wrong argument desktop==null");
 		}
 		
 		if(project==null){
 			throw new NullArgumentException("Wrong argument project==null");
 		}
 		
 		if(marketPlace==null){
 			throw new NullArgumentException("Wrong argument marketPlace==null");
 		}
 		
 		if(company==null){
 			throw new NullArgumentException("Wrong argument company==null");
 		}
 		
 		Button schedulerButton = new Button(this, SWT.TOGGLE);
 		schedulerButton.setBounds(10, 336, 267, 34);
 		schedulerButton.setText("Scheduler");
 		try{
 			Image img=new Image(getParent().getDisplay(),RootDirectory.getInstance().getImagesDirectory()+"planner.png");
 			schedulerButton.setImage(img);
 		}catch(IOException e){
 			e.printStackTrace();
 		}
 
 		schedulerButton.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent arg0) {
 				_schedulerComposite.setVisible(true);
 				setVisible(false);
 			}
 		});
 
 		
 		Button marketPlaceButton = new Button(this, SWT.TOGGLE);
 		marketPlaceButton.setAlignment(SWT.LEFT);
 		marketPlaceButton.setBounds(10, 376, 267, 34);
 		marketPlaceButton.setText("MarketPlace");
 		try{
 			Image img=new Image(getParent().getDisplay(),RootDirectory.getInstance().getImagesDirectory()+"marketplace.png");
 			marketPlaceButton.setImage(img);
 		}catch(IOException e){
 			e.printStackTrace();
 		}
 		
 		marketPlaceButton.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent arg0) {
 				_marketPlaceComposite.setVisible(true);
 				setVisible(false);
 			}
 		});
 
 		
 		Browser browser = new Browser(this, SWT.NONE);
 		browser.setBounds(10, 133, 267, 174);
 		
 		Label lblCurrentProject = new Label(this, SWT.NONE);
 		lblCurrentProject.setBounds(10, 81, 267, 19);
 		lblCurrentProject.setText("Current project");
 		
 		Label lblPromasi = new Label(this, SWT.NONE);
 		lblPromasi.setBounds(0, 0, 283, 19);
 		lblPromasi.setText("Pro.Ma.Si");
 		
 		_schedulerDecorations = new Decorations(_desktop, SWT.TITLE | SWT.MAX | SWT.RESIZE | SWT.MIN);
 		_schedulerDecorations.setLayoutData(new GridData(GridData.FILL_BOTH));
 		_schedulerDecorations.setLayout(new FillLayout());
 		_schedulerDecorations.setBounds(new Rectangle(0,0,400,400));
 	    _schedulerComposite=new SchedulerComposite(_schedulerDecorations,SWT.FILL,_game, _company, _currentProject);
 	    _schedulerComposite.setVisible(false);
 	    _schedulerDecorations.setVisible(true);
 	    _schedulerDecorations.setBounds(new Rectangle(0,0,600,400));
 	    _schedulerDecorations.setText("Scheduler");
 	    _schedulerDecorations.setFocus();
 	    _schedulerDecorations.layout();
 		
 	    _marketPlaceDecorations = new Decorations(_desktop, SWT.TITLE);
 	    _marketPlaceDecorations.setLayoutData(new GridData(GridData.FILL_BOTH));
 	    _marketPlaceDecorations.setLayout(new FillLayout());
 	    _marketPlaceDecorations.setBounds(new Rectangle(0,0,400,400));
 	    _marketPlaceComposite=new MarketPlaceComposite(_marketPlaceDecorations,SWT.FILL,_company,_marketPlace, _game);
 		_marketPlaceComposite.setVisible(false);
 		_marketPlaceDecorations.setVisible(true);
 		_marketPlaceDecorations.setBounds(new Rectangle(0,0,650,500));
 		_marketPlaceDecorations.setText("MarketPlace");
 		_marketPlaceDecorations.setFocus();
 		_marketPlaceDecorations.layout();
 	    
 		_progressBar = new ProgressBar(this, SWT.NONE);
 		_progressBar.setBounds(10, 110, 170, 17);
 		_desktop=desktop;
 		_currentProject=project;
 		_company=company;
 		_marketPlace=marketPlace;
 	}
 
 	@Override
 	public void dispose(){
 		_marketPlaceComposite.dispose();
 		_schedulerComposite.dispose();
 		super.dispose();
 	}
 	
 	@Override
 	protected void checkSubclass() {
 		// Disable the check that prevents subclassing of SWT components
 	}
 	
 	public void updateProgressBar(int progress){
 		_progressBar.setSelection(progress);
 	}
 	
	/**
	 * 
	 */
 	public void close(){
 		_marketPlaceComposite.dispose();
 		_schedulerComposite.dispose();
 	}
 }
