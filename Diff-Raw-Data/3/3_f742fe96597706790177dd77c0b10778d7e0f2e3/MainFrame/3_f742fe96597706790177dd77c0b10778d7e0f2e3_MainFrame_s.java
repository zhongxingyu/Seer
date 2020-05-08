 package main.java.shopsardine.view;
 
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.Image;
 import java.net.MalformedURLException;
 
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTable;
 
 import main.java.shopsardine.main.SSApplication;
 import main.java.shopsardine.model.qrcode.GUIRunner;
 import main.java.shopsardine.model.stats.Stats;
 
 import org.jdesktop.application.ApplicationContext;
 
 public class MainFrame extends JFrame {
 	
 	ApplicationContext context;
 
 	public Navbar navbar;
 	public Searchbar searchbar;
 	public Sidebar sidebar;
 	public JPanel content;
 	public GUIRunner scan;
 	public JTable statsTable;
 	public JLabel help;
 	public Key1 keyboard;
 	public Image loadingImage;
 	
 	public ProductView catalog, search;
 	
 	public Component current_content, current_top;
 	
 	public Stats stats;
 	
 	public MainFrame() {
 		context = SSApplication.getInstance().getContext();
 		setName("mainFrame");
 		getContentPane().setLayout(new BorderLayout());
 		
 		stats = new Stats(context);
 		stats.loadStats();
 		
 		initComponents();
 		setSize(880, 600);
 		setMaximumSize(getSize());
 		setMinimumSize(getSize());
 		
 	}
 	
 	private void initComponents() {
 		
 		sidebar = new Sidebar();
 		add(sidebar, BorderLayout.EAST);
 
 		catalog = new ProductView();
 		add(current_content = catalog);
 		
 		navbar = new Navbar();
 		add(current_top = navbar, BorderLayout.BEFORE_FIRST_LINE);
 		
 		search = new ProductView();
 		searchbar = new Searchbar();
 		
 		keyboard = new Key1(searchbar.tsearch);
 		
 		statsTable = new JTable(); // No se que hacer con esto, que vamos a guardar?
 		statsTable.setName("stats");
 		
 		help = new JLabel(context.getResourceMap().getString("help.text"));
 		help.setName("help");
 		
 		scan = new GUIRunner();
 		scan.setName("scan");
 		
 		pack();
 		
 		
 	}
 	
 	public void showSearch() {
 		if (current_content != search) {
 			System.out.println("search loaded");
 			sidebar.showLogo();
 			remove(current_top);
 			remove(current_content);
 		
 			add(current_top = searchbar, BorderLayout.BEFORE_FIRST_LINE);
 			add(current_content = keyboard);
 		
 			validate();
 			//setSize(880, 600);
 			repaint();
 			
 		}
 	}
 	
 	public void showCatalog() {
 		if (current_content != catalog) {
 			System.out.println("catalog loaded");
 			sidebar.showLogo();
 			remove(current_top);
 			remove(current_content);
 		
 			add(current_top = navbar, BorderLayout.BEFORE_FIRST_LINE);
 			add(current_content = catalog);
 		
 			validate();
 			//setSize(880, 600);
 			
 			repaint();
 		}
 	}
 	
 	public void showStats() {
 		if (current_content != statsTable) {
 			System.out.println("stats loaded");
 			sidebar.showLogo();
 			remove(current_top);
 			remove(current_content);
 			
 			statsTable = new JTable(stats.getTableModel());
 		
 			add(current_top = new JLabel(), BorderLayout.BEFORE_FIRST_LINE);
 			add(current_content = statsTable, BorderLayout.CENTER);
 		
 			validate();
 			//setSize(880, 600);
 			
 			repaint();
 		}
 	}
 	
 	public void showScan() {
 		if (current_content != scan) {
 			System.out.println("scan loaded");
 			sidebar.showLogo();
 			remove(current_top);
 			remove(current_content);
 		
 			add(current_top = new JLabel(), BorderLayout.BEFORE_FIRST_LINE);
 			add(current_content = scan, BorderLayout.CENTER);
 			
 			try {
 				scan.chooseImage();
 			} catch (MalformedURLException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			
 			validate();
 			//setSize(880, 600);
 			
 			repaint();
 		}
 	}
 	
 	public void showHelp() {
 		if (current_content != help) {
 			System.out.println("help loaded");
 			sidebar.showLogo();
 			remove(current_top);
 			remove(current_content);
 		
 			add(current_top = new JLabel(), BorderLayout.BEFORE_FIRST_LINE);
 			add(current_content = help, BorderLayout.CENTER);
 		
 			validate();
 			//setSize(880, 600);
 			
 			repaint();
 		}
 	}
 	/*        categories = new LinkedList<Category>();
         
         new CatalogRequest("GetCategoryList", "language_id=1").make(
         new RequestCallback() {
         	public void handle(Document response) {
         		NodeList cats = response.getElementsByTagName("category");
         		
 				for (int i = 0; i < cats.getLength(); i++)
 					categories.add(new Category((Element) cats.item(i)));
 				
 				splashFrame.pbar.setValue(splashFrame.pbar.getValue() + 1);
         	}
         });*/
 
 	public void showSearchContent() {
 		remove(current_content);
 		add(current_content = search);
 		validate();
 		//setSize(880, 600);
 		repaint();
 	}
 	
 	public void showKeyboard() {
 		remove(current_content);
 		add(current_content = keyboard);
 		validate();
 		//setSize(880, 600);
 		repaint();
 	}
 }
