 //this panel is the center panel that contains the info of the current track playing.
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.*;
 import javax.swing.event.*;
 
 class CenterConsole extends JPanel {
   JLabel artistLabel; //artist 
   JLabel titleLabel; //song name
   JLabel albumLabel; //album title
   JButton setTagButton; //button to set tag
   JLabel timeNowLabel; //shows current time in track
   JLabel totTimeLabel; //shows total time in track
   JProgressBar trackTimeBar; //shows current time position in song
   //trackTime can maybe be a JSlider
 	
   JPanel tagButtonPanel; //holds buttons for tags
 	JPanel tagPanel; //holds the visual tags
   JButton tagButton1;
   JButton tagButton2;
   JButton tagButton3;
   JButton tagButton4;
   JButton tagButton5;
   JButton tagButton6;
 	JButton tabRemoveButton;
 
  
   CenterConsole() {
     setBorder(BorderFactory.createLineBorder(Color.BLACK,1));
     
 		//set layout
 		setLayout(new GridBagLayout());
 	
 		//initialize Components
 		initComponents();
 	
     //set size
     Dimension size = new Dimension(400,200); //dimension of panel
     this.setPreferredSize(size); //set size of panel
 		this.setBackground(Color.BLACK);	
 
   }
 
 	public void initComponents(){
 		artistLabel = new JLabel("Artist");
 		titleLabel = new JLabel("Title");
 		albumLabel = new JLabel("Album");
 		setTagButton = new JButton("Tag");
 		timeNowLabel = new JLabel("0:00");
 		totTimeLabel = new JLabel("0:00");
 		trackTimeBar = new JProgressBar(0,0); //initializes JProgressBar to 0 until a track is linked up 	    
 		tagPanel = new JPanel();
 		
 
 		GridBagConstraints c = new GridBagConstraints();
 		
 	//add artistLabel
 		c.gridx = 3;
 		c.gridy = 0;
 		c.gridwidth = 1;
 		c.gridheight = 1;
 		
 		this.add(artistLabel,c);
 
 	//add titleArtist
 		c.gridx = 3;
 		c.gridy = 1;
 		c.gridwidth = 1;
 		c.gridheight = 1;
 		this.add(titleLabel,c);
 
 	//add albumLabel
 		c.gridx = 3;
 		c.gridy = 2;
 		c.gridwidth = 1;
 		c.gridheight = 1;
 		this.add(albumLabel,c);
 
 	//add setTagButton
 		c.gridx = 4;
 		c.gridy = 2;
 		c.gridwidth = 1;
 		c.gridheight = 1;
 		this.add(setTagButton,c);
 
 
 	//add timeNowLabel
 		c.gridx = 0;
 		c.gridy = 3;
 		c.gridwidth = 1;
 		c.gridheight = 1;
 		c.weightx = .2;
 		this.add(timeNowLabel,c);
 
 	//add trackTimeBar
 		c.gridx = 1;
 		c.gridy = 3;
 		c.gridwidth = 3;
 		c.gridheight = 1;
 		c.weightx = .6;
 		c.fill = GridBagConstraints.HORIZONTAL;
 		this.add(trackTimeBar,c);
 
 	//add totTimeLabel
 		c.gridx = 4;
 		c.gridy = 3;
 		c.gridwidth = 1;
 		c.gridheight = 1;
 		c.weightx = .2;
 		this.add(totTimeLabel,c);
 
 	//add tagPanel
 		c.gridx = 0;
 		c.gridy = 4;
 		c.weightx = 1;
 		c.gridwidth = 6;
 		c.fill = GridBagConstraints.HORIZONTAL;
 		this.add(tagPanel,c);
 
 	//adds listeners to CenterContent
 
 		setTagButton.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent e){
 				//tag the current point in the track
 				postTag();
 			}
 		});
 	
 		trackTimeBar.addChangeListener(new ChangeListener(){
 			public void stateChanged(ChangeEvent e){
 				//updates timeNowLabel as track progresses
				timeNowLabel.setText(trackTimeBar.getValue().toString());
 			}
 		});		
 
 
 	}
 
 	public void changeTrackInfo(Track t){
 		titleLabel.setText(t.title);
 		
 		if(t.artist != null){
 			artistLabel.setText(t.artist);
 		}else{
 			artistLabel.setText("");
 		}
 	
 		if(t.album != null){
 			albumLabel.setText(t.album);
 		}else{
 			albumLabel.setText("");
 		}
 
 		//set totTimeLabel with the length of the track
 		
 	}
 
 	public void postTag(){
 		//post tag at current time position of the track
 	}
 	
 }
 
 /*
 NEED LISTENERS FOR
 -JButton setTag
 -JLabel timeNow needs to always update with current time position
 -JProgressBar trackTime should always update with current time position
 -JButton setTag needs actionListener
 **JPanel tagPanel should update whenever a new tag is created and should load a tag when a tag is chosen
    --also this 'tagPanel' should hold buttons for the tags
      --do we want to generate a new button anytime a setTag is pushed and a new tag is made or
 
      have a static amount of buttons?
 
 -tagPanel has a static number of buttons. unused ones are grayed out or invisible
 
 -JLabel totTime is a static label...shouldn't change
 */
