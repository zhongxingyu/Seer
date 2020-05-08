 package guiAntGame;
 
 import java.awt.Desktop;
 import java.io.File;
 import java.io.IOException;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 
 import javax.swing.JFileChooser;
 
 import antgame.core.AntBrain;
 import antgame.core.AntColour;
 import antgame.core.Cell;
 import antgame.core.Map;
 import antgame.core.World;
 import processing.core.*;
 
 import org.gicentre.utils.move.*;
 import controlP5.*;
 import antgame.core.*;
 
 public class MainMenu extends PApplet {
 
 	private ControlP5 cp5;
 	private PImage bg, bgMain;
 	controlP5.Button buttonHelp, buttonTournament, createMap, bgBrain;
 	controlP5.Button selectRedBrain, selectBlackBrain, selectMap, startButton;
 	private PFont arialMain, arialB;
 	private File redBrain, blackBrain, mapFile;
 	private Map mapMap;
 
 	public void setup() {
 
 		size(600, 500);
 
 		arialMain = createFont("ArialBold", 32);
 		arialB = createFont("Arial", 16);
 
 		cp5 = new ControlP5(this);
 
 		// Main Menu text
 		cp5.addTextlabel("mainText").setPosition(width / 2 - 85, 30)
 				.setText("Main Menu").setFont(arialMain).setColor(200);
 
 		// Help Button
 		buttonHelp = createButton("buttonHelp", "Help", 55, 85, 60);
 
 		// Tournament Button
 		buttonTournament = createButton("buttonTournament", "Tournament", 220,
 				85, 27);
 
 		// Quit Button
 		createMap = createButton("createMap", "Random Map", 385, 85, 22);
 
 		Group antBrains = cp5.addGroup("antBrains")
 				.setBackgroundColor(color(0, 64)).setBackgroundHeight(270)
 				.setWidth(490).setTitle("Ant brain, you choose.")
 				.setColorBackground(color(0, 100)).setPosition(55, 125)
 				.hideArrow().hideBar();
 		PImage bg = loadImage("brainsbg.png");
 
 		bgBrain = cp5.addButton("bgBrain").setPosition(0, 0)
 				.setImages(bg, bg, bg).updateSize().moveTo(antBrains).setOff();
 
 		// uploadBrain Button
 		selectRedBrain = createSelectButton("selectRedBrain", "Select Brain",
 				490 / 4 - 50, 140).moveTo(antBrains);
 
 		// uploadBrain Button
 		selectBlackBrain = createSelectButton("selectBlackBrain",
 				"Select Brain", 490 / 4 * 3 - 50, 140).moveTo(antBrains);
 
 		selectMap = cp5.addButton("selectMap").setPosition(0, 232)
 				.setSize(490, 35).setColorBackground(color(0, 200))
 				.setColorActive(color(0, 200))
 				.setColorForeground(color(0, 150)).moveTo(antBrains);
 
 		selectMap.getCaptionLabel().setFont(arialB).setText("Select Map")
 				.toUpperCase(false).setSize(18).setColor(color(140)).getStyle()
 				.setMarginLeft(190);
 
 		startButton = cp5.addButton("startButton").setPosition(55, 400)
 				.setSize(490, 65).setColorBackground(color(115, 73, 73))
 				.setColorActive(color(215, 73, 73))
 				.setColorForeground(color(115, 73, 73)).lock();
 
 		startButton.getCaptionLabel().setFont(arialB).setText("START")
 				.toUpperCase(false).setSize(38).setColor(color(140)).getStyle()
 				.setMarginLeft(180);
 
 		bgMain = loadImage("bg.jpg");
 
 	}
 
 	public void draw() {
 		background(200);
 		image(bgMain,0,0);
 	}
 
 	private controlP5.Button createButton(String name, String text, int x,
 			int y, int m) {
 		controlP5.Button b = cp5.addButton(name).setPosition(x, y)
 				.setSize(160, 35).setColorBackground(color(0, 50))
 				.setColorActive(color(0, 200))
 				.setColorForeground(color(0, 150));
 
 		b.getCaptionLabel().setFont(arialB).setText(text).toUpperCase(false)
 				.setSize(18).setColor(color(200));
 		b.captionLabel().getStyle().marginLeft = m;
 
 		return b;
 	}
 
 	private controlP5.Button createSelectButton(String name, String text,
 			int x, int y) {
 		controlP5.Button b = cp5.addButton(name).setPosition(x, y)
 				.setSize(100, 20).setColorBackground(color(0, 200))
 				.setColorActive(color(0, 200))
 				.setColorForeground(color(0, 150));
 
 		b.getCaptionLabel().setFont(arialMain).toUpperCase(false).setSize(12)
 				.setText(text).setColor(color(140));
 
 		return b;
 	}
 
 	public void selectRedBrain() {
 		JFileChooser fc = new JFileChooser();
 		int returnVal = fc.showOpenDialog(null);
 
 		if (returnVal == JFileChooser.APPROVE_OPTION) {
 			File file = fc.getSelectedFile();
 			if (AntBrainInterpreterCoryn.antBrainChecker(file
 					.getAbsolutePath())) {
 				selectRedBrain.getCaptionLabel().set(
 						shortText(file.getName(), 13));
 				setButtonGreen(selectRedBrain);
 				blackBrain = file;
 			} else {
 				selectRedBrain.getCaptionLabel().set("Wrong Brain");
 				setButtonRed(selectRedBrain);
 			}
 			checkStart();
 		}
 	}
 
 	public void selectBlackBrain() {
 		JFileChooser fc = new JFileChooser();
 		int returnVal = fc.showOpenDialog(null);
 
 		if (returnVal == JFileChooser.APPROVE_OPTION) {
 			File file = fc.getSelectedFile();
 			if (AntBrainInterpreterCoryn.antBrainChecker(file
 					.getAbsolutePath())) {
 				selectBlackBrain.getCaptionLabel().set(
 						shortText(file.getName(), 13));
 				setButtonGreen(selectBlackBrain);
 				redBrain = file;
 			} else {
 				selectBlackBrain.getCaptionLabel().set("Wrong Brain");
 				setButtonRed(selectBlackBrain);
 			}
 			
 			checkStart();
 		}
 	}
 
 	public void selectMap() {
 		JFileChooser fc = new JFileChooser();
		;
 		int returnVal = fc.showOpenDialog(null);
 
 		if (returnVal == JFileChooser.APPROVE_OPTION) {
 			File file = fc.getSelectedFile();
			
			if (MapInterpreter.mapchecker(file.getAbsolutePath())) {
 				selectMap.getCaptionLabel().set(shortText(file.getName(), 30));
 				selectMap.getCaptionLabel().getStyle().setMarginLeft(10);
 				setButtonGreen(selectMap);
 				mapFile = file;
 				mapMap = null;
 				createMap.unlock();
 				setButtonGrey(createMap);
 				createMap.getCaptionLabel().setText("Random Map").getStyle().setMarginLeft(22);
 				
 			} else {
 				selectMap.getCaptionLabel().set("Wrong Map");
 				setButtonRed(selectMap);
 			}
 			checkStart();
 		}
 	}
 
 	public void createMap(){
 		mapMap = MapCreator.getRandomMap();
 		setButtonGreen(createMap);
 		createMap.getCaptionLabel().setText("Map Generated").getStyle().setMarginLeft(15);
 		createMap.lock();
 		mapFile = null;
 		setButtonGrey(selectMap);
 		checkStart();
 		
 	}
 	public void setButtonRed(controlP5.Button darthMaul) {
 		darthMaul.setColorBackground(color(115, 73, 74))
 				.setColorActive(color(175, 73, 74))
 				.setColorForeground(color(215, 73, 74));
 	}
 
 	public void setButtonGreen(controlP5.Button yoda) {
 		yoda.setColorBackground(color(80, 113, 60))
 				.setColorActive(color(73, 113, 74))
 				.setColorForeground(color(73, 113, 24));
 	}
 	
 	private void setButtonGrey(controlP5.Button darthVader){
 		darthVader.setColorBackground(color(0, 50))
 		.setColorActive(color(0, 200))
 		.setColorForeground(color(0, 150));
 	}
 
 	public String shortText(String toBeShort, int len) {
 		if (toBeShort.length() > len) {
 			return toBeShort.substring(0, len);
 		} else {
 			return toBeShort;
 		}
 	}
 	
 	public void startButton(){
 		if(mapFile!= null && redBrain != null && blackBrain!=null){
 			if((MapInterpreter.mapchecker(mapFile.getAbsolutePath())) && 
 					AntBrainInterpreterCoryn.antBrainChecker(blackBrain
 							.getAbsolutePath()) && AntBrainInterpreterCoryn.antBrainChecker(redBrain
 									.getAbsolutePath())){
 				try {
 					
 					new DisplayFrame(new World(mapFile.getAbsolutePath(),redBrain
 								.getAbsolutePath(),blackBrain
 								.getAbsolutePath()));
 				} catch (Exception e) {
 
 				}
 			}
 		}else if(mapMap != null && redBrain != null && blackBrain!=null){
 			if(AntBrainInterpreterCoryn.antBrainChecker(blackBrain
 							.getAbsolutePath()) && AntBrainInterpreterCoryn.antBrainChecker(redBrain
 									.getAbsolutePath())){
 				try {
 					
 					new DisplayFrame(new World(mapMap,
 							new AntBrain(redBrain.getAbsolutePath(), AntColour.RED),
 							new AntBrain(blackBrain.getAbsolutePath(), AntColour.BLACK)));
 				} catch (Exception e) {
 
 				}
 			}
 		}
 	}
 	
 	public void checkStart(){
 		if(mapFile!= null && redBrain != null && blackBrain!=null){
 			if((MapInterpreter.mapchecker(mapFile.getAbsolutePath())) && 
 					AntBrainInterpreterCoryn.antBrainChecker(blackBrain
 							.getAbsolutePath()) && AntBrainInterpreterCoryn.antBrainChecker(redBrain
 									.getAbsolutePath())){
 				startButton.unlock();
 				setButtonGreen(startButton);
 			}
 			else{
 				setButtonRed(startButton);
 				startButton.lock();
 			}
 		}else if(mapMap!= null && redBrain != null && blackBrain!=null){
 			if(	AntBrainInterpreterCoryn.antBrainChecker(blackBrain
 							.getAbsolutePath()) && AntBrainInterpreterCoryn.antBrainChecker(redBrain
 									.getAbsolutePath())){
 				startButton.unlock();
 				setButtonGreen(startButton);
 			}
 			else{
 				setButtonRed(startButton);
 				startButton.lock();
 			}
 		}
 
 	}
 	public void buttonHelp(){
 
 			try {
 				Desktop.getDesktop().open(new File("\\userGuide\\index.html"));
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 
 	}
 	public void buttonTournament(){
 		try {
 			new DisplayFrame(1);
 		} catch (Exception e) {
 		}
 	}
 
 }
