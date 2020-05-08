 package civ;
 
 import java.util.Scanner;
 
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import java.awt.BorderLayout;
 import java.awt.Toolkit;
 import java.awt.Dimension;
 import javax.swing.Popup;
 import javax.swing.PopupFactory;
 
 import javax.swing.JMenuBar;
 import javax.swing.JMenu;
 import javax.swing.JMenuItem;
 
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 
 import proxy.Proxy;
 import proxy.Result;
 import proxy.FailedException;
 
 public class Window extends JFrame  implements ActionListener{
 
     //Menu bar
     private JMenuBar mainMenuBar;
     private JMenu mainMenu;
     private JMenuItem menuItem;
    // private PopupWindow puw;	
 
     //Only for testpurpose
     public static void main(String[] args){
         
    	//StartScreen st = new StartScreen();
     	
     	
     	/*String name, ip = "dvk.fishface.se";
         Proxy p;
         int port;
         Scanner getint = new Scanner(System.in);
         Scanner scan = new Scanner(System.in);
 
         System.out.println("Enter player name: ");
         name = scan.next();
 
         System.out.println("Enter a port to connect via: ");
         port = getint.nextInt();
 
         p = new Proxy(ip, port, new MyPacketListener());
 
         Result returned;
 
         try{
             returned = p.connect(name);
             System.out.println(returned.getOk() + "   " + returned.getOkMsg());
         }
         catch(FailedException fe){
             System.out.println(fe);
         }
 
         while(true){
             System.out.println("What you wanna do?\n1: connect\n2: list games\n3: host game\n4: start game\n5: join game\n0:quit");
             int what = getint.nextInt();
 
             switch(what){
                 case 0:
                     System.exit(0);
                 case 2:
                     try{
                         returned = p.listGames();
                         System.out.println(returned.getSessions());
                     }
                     catch(FailedException fe){
                         System.out.println(fe);
                     }
 
 
                     break;
 
                 case 3:
                     try{
                         returned = p.host();
                         System.out.println(returned.getName());
                     }
                     catch(FailedException fe){
                         System.out.println(fe);
                     }
 
                     break;
 
                 case 4:
                     try{
                         returned = p.startGame();
                         System.out.println(returned.getOk());
                     }
                     catch(FailedException fe){
                         System.out.println(fe);
                     }
                     break;
                 case 5:
                     try{
                         p.joinGame(scan.next());
                     }
                     catch(FailedException fe){
                         System.out.println(fe);
                     }
                     break;
                 default:
                     break;
             }
         }
    */
         Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
         new Window(size.width,size.height);
     }
 
     Window(int w,int h) {
         super ("Civ 6");
         Round.next(); // Round.next() has to be run atleast once before the player gets to play.
 
         //puw = new PopupWindow();
 
         Menu m = new Menu();
         ViewPort vp = new ViewPort();
 
         mainMenuBar = new JMenuBar();
         mainMenu = new JMenu("Meny");
 
         menuItem = new JMenuItem("Avsluta");
 
         mainMenu.add(menuItem);
         mainMenuBar.add(mainMenu);
 
         menuItem.addActionListener(this);
 
         setJMenuBar(mainMenuBar);
         ///add(mainMenu);
         ///add(mainMenuBar);
 
         setLayout(new BorderLayout());
         setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
         add(m, BorderLayout.SOUTH);
         add(vp, BorderLayout.CENTER);
         //setGlassPane(puw);
         setSize(w, h);
         setVisible(true);
     }
 
     public void actionPerformed(ActionEvent ae){
         if (menuItem == ae.getSource())
             System.exit(0);
     }
 
 }
 
