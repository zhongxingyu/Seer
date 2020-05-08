 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.concurrent.TimeUnit;
 
 import javax.swing.*;
 import org.lwjgl.input.Keyboard;
 
 public class Menue extends Frame
 {
 	private WindowListener l;
 	private ActionLauscher a;
 	private JLabel schriftzug;
 	private ImageIcon icon;
 	private JFrame frame;
<<<<<<< HEAD
 	public static boolean conti = false;
=======
	public static boolean conti = true;
>>>>>>> d5be4a20f6f2d641fca711effa874842a7ae5089
 	
 	protected JButton[] hauptButtons;
 	protected JButton[] spielButtons;
 	private JButton optionButton;
 	private String[] headlines;
 	private JTextField[] felder;
 	private JCheckBox[] boxen;
 	
 	public Menue(String titel)
 	{
 		super(titel);
 		this.setResizable(true);
 		this.setLayout(null);
 		this.setBackground(Color.black);
 		this.setBounds(100, 100, 612, 792);
 		
 		//Listener
 		a = new ActionLauscher();
 		l = new WindowListener();
 		this.addWindowListener(l);
 		
 		//String-Array für Titel der Komponenten.
 		headlines = new String[13];
 		headlines[0] = "Einzelspieler";
 		headlines[1] = "Mehrspieler";
 		headlines[2] = "Optionen";
 		headlines[3] = "Spiel beenden";
 		headlines[4] = "Fortsetzen";
 		headlines[5] = "Spiel neu starten";
 		headlines[6] = "Optionen";
 		headlines[7] = "Hauptmenue";
 		headlines[8] = "Zureuck";
 		headlines[9] = "Power-Ups";
 		headlines[10] = "Anzahl der Leben";
 		headlines[11] = "Anzahl an Bomben";
 		headlines[12] = "Reichweite d. Bomben";
 		
 		//Buttons
 		hauptButtons = new JButton[4];
 		for(int i=0;i<hauptButtons.length;i++)
 		{
 			JButton b = new JButton(headlines[i]);
 			b.setBackground(Color.white);
 			b.setBounds(245, 160+(i*60), 150, 50);
 			b.addActionListener(a);
 			this.add(b);
 			hauptButtons[i] = b;
 		}
 		spielButtons = new JButton[4];
 		for(int i=0;i<spielButtons.length;i++)
 		{
 			JButton b = new JButton(headlines[i+4]);
 			b.setBackground(Color.white);
 			b.setBounds(245, 160+(i*60), 150, 50);
 			b.addActionListener(a);
 			b.setVisible(false);
 			this.add(b);
 			spielButtons[i] = b;
 		}
 		optionButton = new JButton("Zurueck");
 		optionButton.setBackground(Color.white);
 		optionButton.setBounds(245, 435, 150, 50);
 		optionButton.addActionListener(a);
 		optionButton.setVisible(false);
 		this.add(optionButton);
 		
 		//TextField
 		felder = new JTextField[5];
 		for(int i=0;i<felder.length-1;i++)
 		{
 			JTextField t = new JTextField(headlines[i+9]);
 			t.setBounds(125, 170+(i*60), 150, 50);
 			t.setBackground(Color.white);
 			t.setEditable(false);
 			t.setVisible(false);
 			this.add(t);
 			felder[i] = t;
 		}
 		felder[4]= new JTextField("Copyright by: Simon Thyßen, Marian Martini, Raphael Podoba, Martin von Arkel, Philip Höfges");
 		felder[4].setBounds(10, 742, 500, 50);
 		felder[4].setForeground(Color.white);
 		felder[4].setEditable(false);
 		felder[4].setFont(new Font("Arial",Font.PLAIN , 10));
 		felder[4].setBackground(this.getBackground());
 		this.add(felder[4]);
 		
 		//CheckBoxen
 		boxen = new JCheckBox[11];
 		for(int i=0;i<2;i++)
 		{
 			String s;
 			if(i==0)
 			{
 				s = "Ja";
 			}
 			else
 			{
 				s = "Nein";
 			}
 			JCheckBox c = new JCheckBox(s);
 			if(i==0)
 			{
 				c.setSelected(true);
 			}
 			c.setBounds(300+(i*90), 180, 80, 30);
 			c.setVisible(false);
 			this.add(c);
 			boxen[i] = c;
 		}
 		int j = 1;
 		int k = 0;
 		int l = 0;
 		for(int i=2;i<boxen.length;i++)
 		{
 			JCheckBox c = new JCheckBox(""+j);
 			c.setBounds(300+(l*60), 235+(k*60), 50, 30);
 			c.setVisible(false);
 			this.add(c);
 			j = j+2;
 			l++;
 			if(i==4 || i==7)
 			{
 				j = 1;
 				k = k+1;
 				l = 0;
 			}
 			if(i==2 || i==5 || i==8)
 			{
 				c.setSelected(true);
 			}
 			boxen[i] = c;
 		}
 		
 		//Image/Banner
 		schriftzug = new JLabel();
 		schriftzug.setBounds(0, 0, 500, 500);
 		this.add(schriftzug);
 		//Hier Pfad eurem PC anpassen.
 		icon = new ImageIcon(Thread.currentThread().getContextClassLoader().getResource("Bomberman_Vector_by_Haite_S_Regna.png"));
 		frame = new JFrame();
 		frame.getContentPane().add(schriftzug);
 		frame.setBounds(0, 0, 612, 792);
 		schriftzug.setIcon(icon);
 		frame.pack();
 		this.add(schriftzug);
 		
 		this.setVisible(true);
 	}
 	
 	private class ActionLauscher implements ActionListener
 	{
 		public void actionPerformed(ActionEvent e)
 		{
 			String[] string = new String[1];
 			if(e.getActionCommand().equals("Einzelspieler"))
 			{
 				for(int i=0;i<hauptButtons.length;i++)
 				{
 					hauptButtons[i].setVisible(false);
 				}
 				
 				Menue.this.setVisible(false);
 				
 				Main.t1 = new Thread(new Game());
 				Main.t1.start();
 				//Main.main2(string);
 				
 				for(int i=0;i<hauptButtons.length;i++)
 				{
 					hauptButtons[i].setVisible(true);
 					spielButtons[i].setVisible(false);
 				}
 			}
 			if(e.getActionCommand().equals("Mehrspieler"))
 			{
 				//Mehrspielermenue.
 				System.out.println("KNEEECHT klappt noch nit :D");
 			}
 			if(e.getActionCommand().equals("Optionen"))
 			{
 				for(int i=0;i<spielButtons.length;i++)
 				{
 					spielButtons[i].setVisible(false);
 					hauptButtons[i].setVisible(false);
 					optionButton.setVisible(true);
 					felder[i].setVisible(true);
 				}
 				for(int i=0;i<boxen.length;i++)
 				{
 					boxen[i].setVisible(true);
 				}
 			}
 			if(e.getActionCommand().equals("Spiel beenden"))
 			{
 				System.exit(0);
 			}
 			if(e.getActionCommand().equals("Fortsetzen"))
 			{
 				for(int i=0;i<spielButtons.length;i++)
 				{
 					spielButtons[i].setVisible(false);
 				}
 				Main.m.setVisible(false);
 				Main.t1.resume();
 			}
 			if(e.getActionCommand().equals("Neustarten"))
 			{
 				//aktuelle Spiel beenden, neue beginnen
 				
 				/*conti = false;
 				try {
 					TimeUnit.SECONDS.sleep(2);
 				} catch (InterruptedException f) {
 					
 					f.printStackTrace();
 				}
 				Main.t1 = new Thread(new Game());
 				Main.t1.start();*/
 			}
 			if(e.getActionCommand().equals("Hauptmenue"))
 			{
 				conti = false;
 				for(int i=0;i<spielButtons.length;i++)
 				{
 					spielButtons[i].setVisible(false);
 				}
 				Main.m.setVisible(false);
 				Main.t1.resume();
 				System.out.println("Spiel wurde erfolgreich beendet.");
 			}
 			if(e.getActionCommand().equals("Zurueck"))
 			{
 				optionButton.setVisible(false);
 				
 				if(conti == true){
 					for(int i=0;i<spielButtons.length;i++)
 					{
 						felder[i].setVisible(false);
 						spielButtons[i].setVisible(true);
 					}
 					for(int i=0;i<boxen.length;i++)
 					{
 						boxen[i].setVisible(false);
 					}
 				}
 				else{
 				for(int i=0;i<hauptButtons.length;i++)
 				{
 					felder[i].setVisible(false);
 					hauptButtons[i].setVisible(true);
 				}
 				for(int i=0;i<boxen.length;i++)
 				{
 					boxen[i].setVisible(false);
 				}
 				}
 			}
 		}
 	}
 }
