 package ihm;
 
 import java.awt.*;
 import java.util.Locale;
 import java.util.Observer;
 import java.util.ResourceBundle;
 
 import javax.swing.*;
 
 import world.WorldModel;
 import world.WorldModel.GameState;
 
 public class OptionDialog extends JDialog {
   
   WorldModel wm;
   ResourceBundle bIdat;
 
   JPanel panel        = new JPanel(new FlowLayout(FlowLayout.LEFT, 20,10));
   
   JLabel header       = new JLabel();
   JLabel lblOption    = new JLabel();
   JLabel lblLang      = new JLabel();
   JLabel lblSpeed     = new JLabel();
   JLabel lblColor     = new JLabel();
   
   JButton btGame      = new JButton("Start game");
   
   JComboBox boxLang = new JComboBox();
   JComboBox boxColor = new JComboBox();
   
   LanguagesComboModel comboModel;
   
   JSlider sliderSpeed;
   
   public OptionDialog(WorldModel wm){
     OptionDialogAction action = new OptionDialogAction(this);
     this.wm = wm;
     this.wm.addObserver((Observer) new OptionDialogObserver(this));
     
     comboModel= new LanguagesComboModel(wm);
        
     setTitle("Snake Game - Settings");
     setSize(600, 600);
     Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
     Dimension frameSize = this.getSize();
     //--- Place la fentre de l'application
     setLocation((screenSize.width - frameSize.width) / 2,
     (screenSize.height - frameSize.height) / 5);
     
     panel.setBackground(new Color(170, 189, 160));
     
     header = new JLabel( new ImageIcon("images/snake.png"));
     header.setText("Snake 1.0");
     header.setFont(new Font("Serif", Font.BOLD, 28));
     header.setForeground(new Color(18,189,51));
     
     lblOption.setFont(new Font("Serif", Font.BOLD, 18));
     //lblOption.setBorder(BorderFactory.createLineBorder(Color.BLACK));
     lblOption.setPreferredSize(new Dimension(this.getSize().width,30));
     lblOption.setHorizontalAlignment(JLabel.CENTER);
     
     lblLang.setPreferredSize(new Dimension(this.getSize().width/3,30));
     lblLang.setHorizontalAlignment(JLabel.RIGHT);
     lblSpeed.setPreferredSize(new Dimension(this.getSize().width/3,30));
     lblSpeed.setHorizontalAlignment(JLabel.RIGHT);
     lblColor.setPreferredSize(new Dimension(this.getSize().width/3,30));
     lblColor.setHorizontalAlignment(JLabel.RIGHT);
    
     boxLang.setModel(comboModel);
     boxLang.setPreferredSize(new Dimension(150,20));
     boxLang.setMaximumSize(boxLang.getPreferredSize());
     boxLang.addActionListener(action);
     boxLang.setActionCommand("lang");
        
     boxColor.setPreferredSize(new Dimension(150,20));
     boxColor.setMaximumSize(boxColor.getPreferredSize());
     boxColor.addActionListener(action);
     boxColor.setActionCommand("color");
 
     sliderSpeed = new JSlider();
     sliderSpeed.setPreferredSize(new Dimension(150, 60));
     sliderSpeed.setMinimumSize(new Dimension(150, 60));
     sliderSpeed.setMaximumSize(new Dimension(200, 60));
     sliderSpeed.setMinimum(wm.SPEED_MIN);
     sliderSpeed.setMaximum(wm.SPEED_MAX);
     sliderSpeed.setValue(wm.getSpeed());
     sliderSpeed.setMajorTickSpacing(2);
     sliderSpeed.setMinorTickSpacing(1);
     sliderSpeed.setPaintTicks(true);
     sliderSpeed.setPaintLabels(true);
     sliderSpeed.setSnapToTicks(true);
     sliderSpeed.addChangeListener(action);
     
     btGame.addActionListener(action);
     btGame.setActionCommand("start");
     
     panel.add(lblOption);
     panel.add(lblLang);
     panel.add(boxLang);
     panel.add(lblSpeed);
     panel.add(sliderSpeed);
     panel.add(lblColor);
     panel.add(boxColor);
     
     setLayout(new BorderLayout());
     add(header,BorderLayout.NORTH);
     add(panel, BorderLayout.CENTER);
     add(btGame, BorderLayout.SOUTH);
   }
   
   public void setText(){
     bIdat = ResourceBundle.getBundle("lang.Idat", wm.getLocale());
     
     lblOption.setText(bIdat.getString("option")+" : ");
     lblLang.setText(bIdat.getString("language")+" : ");
     lblSpeed.setText(bIdat.getString("speed")+" : ");
     lblColor.setText(bIdat.getString("color")+" : ");
     
     comboModel.update();
     boxLang.revalidate();
    
     boxColor.removeAllItems();
     boxColor.addItem(bIdat.getString("blue"));
     boxColor.addItem(bIdat.getString("red"));
     boxColor.addItem(bIdat.getString("pink"));    
     boxColor.revalidate();
   }
 
   public void changeLang(int i) {
     switch(i){
       case 0 :
         wm.setLocale(new Locale((Locale.FRENCH.toString())));
         break;
       case 1 :
         wm.setLocale(new Locale((Locale.ENGLISH.toString())));
         break;
       case 2 :
         wm.setLocale(new Locale((Locale.ITALIAN.toString())));
         break;
       default:
         break;
     }
   }
 
   public void changeSpeed(int value) {
     wm.setSpeed(value);    
   }
 
   public void startGame() {
     wm.setState(GameState.PLAY);
     setVisible(false);    
   }
 }
