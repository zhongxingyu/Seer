 /**
  * Project : TowerDefense
  * By Aurélie Beauprez, Thomas Demenat, Keven Akyurek and Cecilia Lejeune
  * Copyright IMAC 2013 - All Rights Reserved
  *
  * File created on 9 mai 2013
  */
 package View;
 
 import java.awt.Color;
 import java.awt.Insets;
 import java.awt.Point;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.nio.file.Files;
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import javax.imageio.ImageIO;
 import javax.swing.JButton;
 import javax.swing.JFileChooser;
 import javax.swing.JOptionPane;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.filechooser.FileNameExtensionFilter;
 
 import GameEngine.Player.PlayerType;
 
 
 /**
  * Project - TowerDefense</br>
  * <b>Class - EditorToolBar</b></br>
  * <p>The EditorToolBar class represents the editor tool bar
  * </p> 
  * <b>Creation :</b> 10/05/2013</br>
  * @author K. Akyurek, A. Beauprez, T. Demenat, C. Lejeune - <b>IMAC</b></br>
  * @see MainViews
  * @see EditorScene
  * @see ViewManager
  */
 @SuppressWarnings("serial")
 public class EditorToolBar extends MainViews{
 	private EditorScene editorScene;
 	
 	static private final String newline = "\n";
 	
     private JButton jButtonOpen;
     private JButton jButtonSave;
     private JButton jButtonBack;
     private JButton jButtonPaint;
   
     private boolean mapChosen;
   
     private  JTextArea log;
     private  JScrollPane logScrollPane;
     
     private JFileChooser fileChooser;
     private FileNameExtensionFilter fileFilter;
     
     private File imageView;
     
     private ArrayList<Sprite> sprites;
 
 	/**
 	 * Constructor of the Editor Toolbar class
 	 * @param view
 	 * @param position
 	 * @param width
 	 * @param height
 	 */
 	public EditorToolBar(ViewManager view, Point position, int width, int height, EditorScene editorScene) {
 		super(view,position,width,height);
 		setLayout(null);
 		this.editorScene = editorScene;
 		mapChosen = false;
 		
 		//Creating the components
 		jButtonOpen = new javax.swing.JButton();
 		jButtonSave = new javax.swing.JButton();
 		jButtonBack = new javax.swing.JButton();
 		jButtonPaint = new javax.swing.JButton();
 	 
 		//Log : 5 lines, 20 column
         log = new JTextArea(5,200);
         log.setMargin(new Insets(5,5,5,5));
         log.setEditable(false);
         logScrollPane = new JScrollPane(log);
 		
 		//Setting the components parameters and theirs listeners        		
 		jButtonOpen.setText("Open");
 		jButtonOpen.addActionListener(new ActionListener() {
 		    public void actionPerformed(ActionEvent evt) {
 		        jButtonOpenPerformed(evt);
 		    }
 		});
 		
 		jButtonSave.setText("Save");
 		jButtonSave.addActionListener(new ActionListener() {
 		    public void actionPerformed(ActionEvent evt) {
 		        jButtonSavePerformed(evt);
 		    }
 		});
 		
 		jButtonBack.setText("Home");
 		jButtonBack.addActionListener(new ActionListener() {
 		    public void actionPerformed(ActionEvent evt) {
 		        jButtonBackPerformed(evt);
 		    }
 		});
 		
 		jButtonBack.setText("Home");
 		jButtonBack.addActionListener(new ActionListener() {
 		    public void actionPerformed(ActionEvent evt) {
 		        jButtonBackPerformed(evt);
 		    }
 		});
 		
 		jButtonPaint.setText("Paint Relief");
 		jButtonPaint.addActionListener(new ActionListener() {
 		    public void actionPerformed(ActionEvent evt) {
 		    	paintRelief();
 		    }
 		});
 		
 		setBackground(Color.DARK_GRAY); 
 		jButtonOpen.setBounds(10, 10, 100,25);
 		jButtonSave.setBounds(10, 50, 100,25);
 		jButtonBack.setBounds(10, 90, 100,25);
 		jButtonPaint.setBounds(140, 50, 150,25);
 		logScrollPane.setBounds(140,80, 650,85);
 		
 		sprites = new ArrayList<Sprite>();
 		//Add the AddTower Attack Sprite on the panel
 		addSprite(new EditorAddBaseSprite(editorScene, this, new Point(320,50),PlayerType.FIRE, 36, 36));
 		addSprite(new EditorAddBaseSprite(editorScene, this, new Point(360,50),PlayerType.NEUTRAL, 36, 36));
 		
 		add(jButtonOpen);
 		add(jButtonSave);
 		add(jButtonBack);
 		add(jButtonPaint);
 		add(logScrollPane);
 	}
 	
 	/**
 	 * Add a Sprite in the EditorToolBar ArrayList
 	 * @param sprite
 	 * @see
 	 */
 	public void addSprite(Sprite sprite){
 		sprites.add(sprite);
 		
 		//TO DO : retrieve the last element added...
 		Iterator<Sprite> it = sprites.iterator();
 		while (it.hasNext()) {
 			Sprite element = it.next();
 			element.setBounds(element.getPosition().x -(element.getWidth()/2), element.getPosition().y -(element.getHeight()/2), element.getWidth(),element.getHeight());
 			add(element);
 		}
 		
         //Repaint the panel
     	revalidate();
     	repaint();	
 	}
 	
 	/**
 	 * jButtonOpen Event handler - Launch the fileChooser to let the user choose a file !
 	 * @param evt - ActionEvent performed by the player
 	 */
     private void jButtonOpenPerformed(ActionEvent evt) {
     	if(!mapChosen) showChooseFile();
     	else {
         	mapChosen = false;
     		jButtonOpen.setText("Open");
     		
     		showChooseFile();
     		
     		//TODO else reinitialiser tout ! : Nouvelle map remettre  mapChosen à false et le texte  à Open
     	}
     }
     
 	/**
 	 * jButtonSave Event handler - Save the map and its mapView !
 	 * @param evt - ActionEvent performed by the player
 	 */
     private void jButtonSavePerformed(ActionEvent evt) {
     	if (editorScene.getPlayerBaseCount()==4){
     		//Ask the name of the newly created map
     		String fileName = JOptionPane.showInputDialog("How would you like to name your map ?");
     		if (fileName == null) return;
     		
     		while(fileName.length()<1){
     			fileName = JOptionPane.showInputDialog("Error : You have to give a name to your map");
     			if (fileName==null) return;
     		}
     			
     		File file = new File("img/map/"+fileName+"_hm.png");
     		
     		//Paint all the data on a image
 			BufferedImage newMap = new BufferedImage(800, 400, BufferedImage.TYPE_INT_RGB);
 			
 			//Paint Height infos
 			boolean heightGrid[] = editorScene.getHeightGrid();
 			for(int i=0;i<editorScene.getNbCaseInGrid();i++){
 				if (heightGrid[i]==false){
 					for(int y=((i-i%50)/50)*16;y<(16+((i-i%50)/50)*16);y++){
 						for (int x=(i%50)*16;x<(16+(i%50)*16);x++){
     						newMap.setRGB(x, y, Color.white.getRGB());
     					}
 					}
 				}
 			}
 			
 			//Paint Base infos
 			int nbPlayerBase=0;
 			for(Sprite sp:editorScene.getSprites()){
 				if (sp.getPlayerType()!=PlayerType.NEUTRAL){
 					Color colorPlayerBase = null;
 					switch(nbPlayerBase){
 					case 0:
 						colorPlayerBase=new Color(46,46,46);
 						break;
 					case 1:
 						colorPlayerBase=new Color(83,83,83);
 						break;
 					case 2:
 						colorPlayerBase=new Color(124,124,124);
 						break;
 					case 3:
 						colorPlayerBase=new Color(166,166,166);
 						break;
 					}
 					newMap.setRGB(sp.getPosition().x,sp.getPosition().y,colorPlayerBase.getRGB());
 					nbPlayerBase++;
 				}
 				else{
 					Color colorNeutralBase = new Color(208,208,208);
 					newMap.setRGB(sp.getPosition().x,sp.getPosition().y,colorNeutralBase.getRGB());
 				}
 			}
 			
 			try {
 				ImageIO.write(newMap, "png", file);
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 
 			//Copy the view image to the folder
 			File newImageView = new File("img/map/"+fileName+"_view.png");
 			
 			try {
				if (!newImageView.getAbsoluteFile().equals(imageView.getAbsoluteFile())){
 					if (newImageView.exists()) newImageView.delete();
 					Files.copy(imageView.toPath(), newImageView.toPath());
 				}
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 			
 			//Delete all tmp files
 			File tmpFolder = new File("tmp");
 			String[] files = tmpFolder.list();
 			for(String s:files){
 				File f = new File("tmp/"+s);
 				f.delete();
 			}
 			
 			//Maps have been successfully saved : quit the editor
 			JOptionPane.showMessageDialog(view,
 				    "Your map has been saved !");
 			view.homeMenu();
 			
     	}
     	else{
     		displayError("ERROR : You have to place the 4 player bases in order to save the Map !");
     	}
     }
 	
 	/**
 	 * jButtonBack Event handler - Back to the home menu !
 	 * @param evt - ActionEvent performed by the player
 	 */
     private void jButtonBackPerformed(ActionEvent evt) {
     	mapChosen = false;
 		jButtonOpen.setText("Open");
 		
     	editorScene.quitEditor();
     	view.homeMenu();
     }
     
 	/**
 	 * jButtonPaint Event handler - Tell the EditorScene that the user want to paint the relief !
 	 * @see #openImage(String, String)
 	 */
     public void paintRelief() {
     	if(mapChosen){
 	    	editorScene.setEditHeight(true);
 			log.append("_________________________________________________"+newline);
 			log.append("PAINT INSTRUCTIONS : "+newline);
 			log.append("Drag the mouse to paint the relief (mouse left button) or erase it (mouse right button) "+newline);
 			log.append("_________________________________________________"+newline);
     	}
     }
     
     public void addBaseClicked(){
     	log.append("_________________________________________________"+newline);
 		log.append("BASES INSTRUCTIONS : "+newline);
 		log.append("Left click on the map to add a base. Click on a base to delete it."+newline);
 		log.append("You can only add 4 player bases (red) and 5 neutral bases (gray)."+newline);
 		log.append("_________________________________________________"+newline);
     }
     
     public void displayError(String error){
     	log.append(error+newline);
     }
 
 	/**
 	 * Launch a file chooser to let the user choose a map
 	 * @param editorScene
 	 * @see #jButtonOpenPerformed(ActionEvent)
 	 * @see ViewManager#launchMapEditor()
 	 */
 	public void showChooseFile() {
 		//clearing the log contains
 		log.setText(newline+"Open a 800x400 jpg or png map"+newline);
 		fileChooser = new JFileChooser();
 		
 		//Create a filter to show png or jpg file only
 		fileFilter = new FileNameExtensionFilter("Images files","jpg", "jpeg", "png");
 		
 		fileChooser.setFileFilter(fileFilter);
 		
 		int choice=fileChooser.showOpenDialog(view);
 		//If a file have been choose 
 		if(choice==JFileChooser.APPROVE_OPTION){
 			imageView = fileChooser.getSelectedFile();
 			openImage(fileChooser.getSelectedFile().getName(),fileChooser.getSelectedFile().getAbsolutePath() );
 		}
 		else log.append("Open a damn map ! "+newline);
 	}
     
 	/**
 	 * Try to open the selected file
 	 * @param fileName
 	 * @param filePath
 	 * @see #showChooseFile()
 	 */
 	public void openImage(String fileName, String filePath){
 		
 		log.append("Checking the "+filePath+" map format..."+newline);
 		
 	
 		BufferedImage img = null;
 		
 		//Trying to load the image
 		try {
 			img = ImageIO.read(new File(filePath)); 
 		} catch (IOException e) {
 			e.printStackTrace();
 		}	
 		
 		if(img==null){
 			log.append ("ERROR : The "+filePath+" map can't be load. The file might be corrupted. Try an other map ! "+newline);
 		}
 		else{
 			//Checking the format
 			if((img.getWidth()!= 800)||(img.getHeight()!=400)){
 				log.append("ERROR : The image size need to be 800x400 instead of "+img.getWidth()+"x"+img.getHeight()+".Try an other map ! "+newline);
 			}
 			else{
 				log.append ("Loading the "+filePath+" map..."+newline);
 				
 				mapChosen = true;
 				jButtonOpen.setText("New");
 				
 				editorScene.initiate(img);	
 				paintRelief();
 			}
 		}
 		
 	}
     
 
 }
