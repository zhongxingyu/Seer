 package com.helper;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.Graphics2D;
 import java.awt.GridLayout;
 import java.awt.Image;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.image.BufferedImage;
 import java.io.File;
 
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JPanel;
 
 import com.physics.PhysicsEngine;
 
 /**
  * 
  * Custom JPanel class to create a sliding effect selection system that contains
  * the various McLuhan Media Objects.
  * 
  * @author Jozef
  *
  */
 public class TopSlider extends JPanel implements ActionListener {
 	
 	private File[] images, text;
 	private ImageIcon[] img, txt;
 	private Component[] list;
 	private JButton left, right;
 	private Dimension nav = new Dimension(45,45), btn = new Dimension(100,100);
 	private int index;
 	private PhysicsEngine eng;
 	private boolean type;
 	
 	public TopSlider(){
 		this.setBackground(Color.BLACK);
 		((FlowLayout)this.getLayout()).setAlignment(FlowLayout.LEFT);
 	}
 	
 	/*
 	 * Assigns the required file data for the selected themes images and text media
 	 */
 	public void letsFight(File[] images, File[] text){
 		this.invalidate();
 		this.text = text;
 		this.images = images;
 		index =0;
 		genLists();
 		setupPanel();
 		setupList();
 		this.validate();
 	}
 
 	/*
 	 * Assigns engine to recieve rendering calls
 	 */
 	public void setEngine(PhysicsEngine context){
 		this.eng = context;
 	}
 	
 	/*
 	 * Creates the 5 media 2 nav button objects and assigns their
 	 * motion directions 
 	 * 
 	 */
 	private void setupPanel() {
 		left = new JButton(new ImageIcon("First_Button1.jpg"));
 		left.setPreferredSize(nav);
 		left.setOpaque(false);
 		left.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent e) {
 				moveList(false);
 			}
 		});
 		
 		right = new JButton(new ImageIcon("Last_Button1.jpg"));
 		right.setPreferredSize(nav);
 		right.setOpaque(false);
 		right.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent e) {
 				moveList(true);
 			}
 		});
 
 		
 	}
 
 	/*
 	 * Creates the buttons for all the media files
 	 * and assigns them to the TopSlider panel. 
 	 * 
 	 */
 	private void setupList() {
 		JPanel grid = gridPan();
 		list = new Component[img.length+txt.length];
 		for (int i=0;i<img.length;i++){
 			list[i] = buttonIMG(img[i],i);
 		}
 		int j = img.length;
 		for(int i=0;i<txt.length;i++){
 			list[i+j] = buttonTXT(txt[i],i);
 		}
 		int count = 0;
 		while(count < 5 ){
 			if(index != list.length){
 				grid.add(list[index]);
 				index++;
 				count++;
 			}
 			else{
 				index=0;
 			}
 		}
 
 		this.invalidate();
 		this.removeAll();
 		this.add(left);
 		this.add(grid);
 		this.add(right);
 		this.validate();
 
 	}
 	
 	/*
 	 * Evaluates the button positions and 
 	 * if true moves the images ahead by 5
 	 * if false move the images back by 5 accounting
 	 * for out of bounds index and wrap around
 	 * 
 	 */
 	private void moveList(boolean dir){
 		JPanel grid = gridPan();
 		if(list.length > 5){
			if(!dir){
				index = index - 10;
				if(index<0){
					index= list.length + index;
				}
 			}
 			int count = 0;
 			while(count < 5 ){
 				if(index != list.length){
 					grid.add(list[index]);
 					index++;
 					count++;
 				}
 				else{
 					index=0;
 				}
 			}
 
 			this.invalidate();
 			this.removeAll();
 			this.add(left);
 			this.add(grid);
 			this.add(right);
 			this.validate();
 		}
 	}
 
 	/*
 	 * Initialize the button to launch a specified image media object
 	 * in the attached engine. 
 	 * 
 	 */
 	private Component buttonIMG(ImageIcon img,int index) {
 		JButton temp = new JButton(img);
 		temp.setName(index+"");
 		temp.setPreferredSize(btn);
 		temp.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent e) {
 				int i = Integer.parseInt(((JButton)e.getSource()).getName());
 				eng.pushTimerImg(i);
 			}
 		});
 
 
 		return temp;
 	}
 	
 	/*
 	 * Initialize the button to launch the specified text media
 	 * object in the attached engine.
 	 * 
 	 */
 	private Component buttonTXT(ImageIcon img,int index) {
 		JButton temp = new JButton(img);
 		temp.setName(index+"");
 		temp.setPreferredSize(btn);
 		temp.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent e) {
 				int i = Integer.parseInt(((JButton)e.getSource()).getName());
 				eng.pushTimerTxt(i);
 			}
 		});
 
 		
 		return temp;
 	}
 	
 	/*
 	 * creates the scrolling grid panel
 	 * 
 	 */
 	private JPanel gridPan(){
 		JPanel grid = new JPanel();
 		GridLayout scroll = new GridLayout(0,5);
 		scroll.setHgap(100);
 		scroll.setVgap(5);
 		grid.setLayout(scroll);
 		grid.setSize(750,200);
 		grid.setLocation(90,0);
 		grid.setBackground(Color.BLACK);
 		
 		return grid;
 	}
 
 	/*
 	 * create the image lists for the supplied media file objects
 	 */
 	private void genLists() {
 		img = new ImageIcon[images.length];
 		for(int i=0;i<images.length;i++){
 			img[i] = modImage(images[i]);
 
 		}
 		txt = new ImageIcon[text.length];
 		for(int i=0;i<text.length;i++){
 			txt[i] = modImage(text[i]);
 
 		}
 
 	}
 	
 	/*
 	 * resize the image to match the button sizes
 	 */
 	private ImageIcon modImage(File images2){
 		ImageIcon newIcon;
 		newIcon = new ImageIcon(images2.getPath());
 		Image img = newIcon.getImage();
 		Image newimg = img.getScaledInstance(100, 100,  java.awt.Image.SCALE_SMOOTH);
 		
 		return new ImageIcon(newimg);
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent arg0) {
 		
 		
 	}
 	
 
 
 }
