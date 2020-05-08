 package de.splashfish.EvenLinux;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Font;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Hashtable;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import javax.swing.BorderFactory;
 import javax.swing.DefaultListCellRenderer;
 import javax.swing.DefaultListModel;
 import javax.swing.ImageIcon;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import org.lesscss.LessCompiler;
 import de.splashfish.util.FileShift;
 import de.splashfish.util.ModificationEvent;
 import de.splashfish.util.ModificationListener;
 import de.splashfish.util.ModificationWatcher;
 
 public class OverviewPane extends JPanel {
 	private static final long serialVersionUID = -4127456742787322239L;
 
 	private Font 				font;
 	private ImageIcon			loadingIco	= new ImageIcon(this.getClass().getResource("/res/loading.gif"));
 	private ImageIcon			emptyIco	= null;
 	
 	private DefaultListModel 	model 		= new DefaultListModel();
 	private JList				list		= new JList(model);
 	private JScrollPane			listscroll	= new JScrollPane(list);
 	private JLabel				process		= new JLabel(emptyIco, JLabel.CENTER);
 	
 	private ModificationWatcher modlistener	= new ModificationWatcher(3);
 	private Hashtable
 			<File, Integer>		itemId 		= new Hashtable<File, Integer>();
 	
 	private CompilationHandler 	compiler;
 	private File				ignore_file;
 	
 	public OverviewPane() {
 		LessCompiler comp = new LessCompiler();
 					 comp.setEncoding("UTF-8");
 					 comp.setCompress(true);
 					 
 		compiler = 	new CompilationHandler(comp, this);
 		
 		list.setCellRenderer(new CustomRenderer());
 		try {
 			font = Font.createFont(Font.TRUETYPE_FONT, OverviewItem.class.getResourceAsStream("/font/arvo.ttf")).deriveFont(Font.PLAIN, 24f);
 		} catch (Exception e) {
 			font = new Font(Font.SERIF, Font.PLAIN, 24);
 		}
 		
 		list.addMouseListener(new MouseAdapter() {
 			@Override public void mousePressed(MouseEvent e) {
 				if (e.getButton() == MouseEvent.BUTTON3) {
 					final int index = list.locationToIndex(e.getPoint());
 					final ItemDefinition current = ((ItemDefinition)model.get(index));
 					OptionPopup popup = new OptionPopup(current, new Runnable() {
 																	@Override public void run() {
 																		model.remove(index);
 																		list.repaint();
 																		
 																		try {
 																			if(!ignore_file.exists()) {
 																				ignore_file.createNewFile();
 																			}
 																			
 																			BufferedWriter bw = new BufferedWriter(new FileWriter(ignore_file, true));
 																				bw.append(current.getShift().get(FileShift.FROM_FILE).getAbsolutePath());
 																				bw.newLine();
 																			bw.close();
 																		} catch (IOException e) {
 																			// too bad.. :p
 																		}
 																	}
 																 }, new Runnable() {
 																	@Override public void run() {
 																		compiler.addToQueue(current);
 																	}
 																 });
 					popup.show(list, e.getPoint().x, e.getPoint().y);
 				}
 			}
 		});
 		
 		modlistener.addModificationListener(new ModificationListener() {
 
 			@Override public void fileModified(ModificationEvent e) {
 				
 				if(e.getFiles().length > 0)
 					for(File file : compile_list) {
 						if(model.size() >= itemId.get(file))
 							compiler.addToQueue((ItemDefinition) model.get(itemId.get(file)));
 					}
 				
 			}
 			
 		});
 		
 		this.setLayout(new BorderLayout());
 		this.add(process, BorderLayout.NORTH);
 		this.add(listscroll, BorderLayout.CENTER);
 		
 		process.setFont(font);
 		process.setOpaque(true);
 		process.setBackground(new Color(225, 225, 225));
 		process.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(180, 180, 180)));
 		
 		new Timer().schedule(new TimerTask() {
 
 			@Override public void run() {
 				list.repaint();
 			}
 			
 		}, 5000, 5000);
 	}
 	
 	
 	private ArrayList<File> ignore_list = new ArrayList<File>();
 	private void listIgnores() {
 		ignore_list.clear();
 		if(ignore_file.exists()) {
 			String line;
 			try {
 				BufferedReader br = new BufferedReader(new FileReader(ignore_file));
 				while((line = br.readLine()) != null) {
 					ignore_list.add(new File(line).getAbsoluteFile());
 				}
 				br.close();
 			} catch (FileNotFoundException e) {
 				// checked with ".exists()"
 			} catch (IOException e) {
 				// too bad :p
 			}
 		}
 	}
 	
 	private ArrayList<File> compile_list = new ArrayList<File>();
 	public boolean listFiles(File[] projectfolder) {
 		this.ignore_file = new File(projectfolder[0] + File.separator + ".lecss_ignore");
 		listIgnores();
 		
 		model.clear();
 		itemId.clear();
 		
 		toggleProcess(true);
 		
 		for(File folder : projectfolder) {
 			addToList(folder);
 		}
 		
 		toggleProcess(false);
 		
 		for(int i = 0; i < model.getSize(); i++) {
 			itemId.put(((ItemDefinition)model.get(i)).getShift().get(FileShift.FROM_FILE), i);
 		}
 		
 		return model.getSize() > 0;
 	}
 	
 	
 	private void addToList(File dir){
 		if(dir instanceof File) {
 			dir = dir.getAbsoluteFile();
 			
 	        if(dir.isFile()) {
 	            if(dir.getName().endsWith(".less") ) {
 	            	if(!ignore_list.contains(dir)) {
 		            	model.addElement(new ItemDefinition(dir, new File(
 		            				dir.getAbsolutePath().substring(
 		            						0, dir.getAbsolutePath().lastIndexOf(".less")
 		            					)
 		            				+ ".css"
 		            			)));
 		            	compile_list.add(dir);
 	            	}
 	            	modlistener.addWatcher(dir);
 	            }
 	        } else {
 	            File[] subcontent = dir.listFiles();
 	            if(subcontent != null && subcontent.length > 0)
 		            for(File current : subcontent) {
 		            	addToList(current);
 		            }
 	        }
 		}
     }
 	
 	public void toggleProcess(boolean on) {
 		if(on)
 			process.setIcon(loadingIco);
 		else
 			process.setIcon(emptyIco);
 	}
 }
 
 class CustomRenderer extends DefaultListCellRenderer{
 	private static final long serialVersionUID = 7051960293208419093L;
 	
 	private final ImageIcon TRUE 	= new ImageIcon(this.getClass().getResource("/res/true.png"));
 	private final ImageIcon FALSE 	= new ImageIcon(this.getClass().getResource("/res/false.png"));
 	private final ImageIcon WAIT 	= new ImageIcon(this.getClass().getResource("/res/wait.png"));
 	
 	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean hasFocus) {
 		ItemDefinition definition = (ItemDefinition) value;
 		ImageIcon state;
 		
 		String additional = null;
 		
 		switch(definition.getState()) {
 			case ItemDefinition.TRUE: state = TRUE; 	break;
 			case ItemDefinition.FALSE: state = FALSE; 
 				 additional = definition.getError();	break;
 			default:
 			case ItemDefinition.WAIT: state = WAIT; 	break;
 		}
 		
 		if(definition.getState() != ItemDefinition.FALSE) {
 			if(System.currentTimeMillis() - definition.getUpdate() < 10000) {
 				additional = "Just now!";
 			} else if(System.currentTimeMillis() - definition.getUpdate() < 300000) {
 				additional = "Not more than 5 min. ago!";
 			} else {
 				additional = "Some time ago!";
 			}
 		}
 		
 		return new OverviewItem(state, definition.getLessName(), additional);
 	}
 	
 }
