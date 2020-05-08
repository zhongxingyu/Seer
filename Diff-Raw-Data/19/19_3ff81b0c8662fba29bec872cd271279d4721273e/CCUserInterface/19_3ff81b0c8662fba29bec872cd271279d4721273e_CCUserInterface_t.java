 package ch.jmnetwork.cookieclicker.ui;
 
import java.awt.Color;
 import java.awt.EventQueue;
 import java.awt.Font;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 
 import javax.swing.BorderFactory;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.UIManager;
 
 import ch.jmnetwork.cookieclicker.CookieManager;
 import ch.jmnetwork.cookieclicker.helper.Helper;
 import ch.jmnetwork.cookieclicker.helper.HelperClicker;
 import ch.jmnetwork.cookieclicker.util.SaveLoadHandler;
 
 public class CCUserInterface
 {
     // ======================================//
     // VARIABLES
     // ======================================//
     
     private static CookieManager cookiemanager;
     private static SaveLoadHandler slHandler;
     
     private final static String FONT = "Arial";
     
     private static JFrame jframe;
     private static JLabel infoLabel;
     private static JButton cookie_button;
     private static JLabel currentCookiesLabel;
     private static JLabel cookieRateLabel;
     private static JButton pointerBuyButton;
     private static JButton grandmaBuyButton;
     private static JButton farmBuyButton;
     private static JButton factoryBuyButton;
     private static JButton mineBuyButton;
     
     public CCUserInterface(CookieManager cookieManager, SaveLoadHandler slhandler)
     {
         cookiemanager = cookieManager;
         slHandler = slhandler;
         
         EventQueue.invokeLater(new Runnable()
         {
             @Override
             public void run()
             {
                 try
                 {
                     init();
                     jframe.setVisible(true);
                 }
                 catch (Exception e)
                 {
                 }
             }
         });
     }
     
     private void init()
     {
         // ======================================//
         // TRY TO SET SYSTEM LOOK AND FEEL
         // ======================================//
         
         try
         {
             UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
         }
         catch (Exception e)
         {
             System.out.println("FAIL: Unable to set system look'n'feel");
         }
         
         // ======================================//
         // DEFINE ALL VARIABLES
         // ======================================//
         
         jframe = new JFrame();
         infoLabel = new JLabel();
         cookie_button = new JButton();
         currentCookiesLabel = new JLabel();
         cookieRateLabel = new JLabel();
         pointerBuyButton = new JButton();
         grandmaBuyButton = new JButton();
         farmBuyButton = new JButton();
         factoryBuyButton = new JButton();
         mineBuyButton = new JButton();
         
         // ======================================//
         // COMPONENT SETTINGS
         // ======================================//
         
         jframe.setTitle("Java Cookie Clicker by TH3ON1YN00B and domi1819");
         jframe.setSize(1000, (20 + 265 + 60));
         jframe.getContentPane().setLayout(null);
         jframe.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
         jframe.setLocationRelativeTo(null);
         jframe.setResizable(false);
         jframe.setIconImage(Toolkit.getDefaultToolkit().getImage("cookie.png"));
         jframe.addWindowListener(new WindowAdapter()
         {
             @Override
             public void windowClosing(WindowEvent e)
             {
                 slHandler.saveToDisk();
                 System.exit(0);
             }
         });
         
         infoLabel.setText("");
         infoLabel.setFont(Font.getFont(FONT));
         infoLabel.setBounds(40 + 265, 270, 300, 50);
        infoLabel.setForeground(Color.RED);
         
         cookie_button.setBounds(20, 20, 265, 265);
         cookie_button.setBorder(BorderFactory.createEmptyBorder());
         cookie_button.setContentAreaFilled(false);
         cookie_button.setFocusPainted(false);
         cookie_button.setIcon(new ImageIcon("cookie.png", "This is a cookie"));
         cookie_button.addActionListener(new ActionListener()
         {
             @Override
             public void actionPerformed(ActionEvent arg0)
             {
                 cookiemanager.addCookies(HelperClicker.cookiesPerClick());
                 updateUI();
             }
         });
         
         currentCookiesLabel.setText("0");
         currentCookiesLabel.setFont(Font.getFont(FONT));
         currentCookiesLabel.setBounds(300, 20, 300, 50);
         
         cookieRateLabel.setText("");
         cookieRateLabel.setFont(Font.getFont(FONT));
         cookieRateLabel.setBounds(300, 30, 300, 50);
         
         pointerBuyButton.setText("POINTERS_HERE");
         pointerBuyButton.setFont(Font.getFont(FONT));
         pointerBuyButton.setBounds(700, 20, 250, 20);
         pointerBuyButton.setEnabled(false);
         pointerBuyButton.addActionListener(new ActionListener()
         {
             @Override
             public void actionPerformed(ActionEvent arg0)
             {
                 if (cookiemanager.buyPrice(Helper.getPriceForHelper(0)))
                 {
                     Helper.helpers[0].onBought();
                 }
             }
         });
         
         grandmaBuyButton.setText("GRANDMAS_HERE");
         grandmaBuyButton.setFont(Font.getFont(FONT));
         grandmaBuyButton.setBounds(700, 42, 250, 20);
         grandmaBuyButton.setEnabled(false);
         grandmaBuyButton.addActionListener(new ActionListener()
         {
             @Override
             public void actionPerformed(ActionEvent e)
             {
                 if (cookiemanager.buyPrice(Helper.getPriceForHelper(1)))
                 {
                     Helper.helpers[1].onBought();
                 }
             }
         });
         
         farmBuyButton.setText("FARMS_HERE");
         farmBuyButton.setFont(Font.getFont(FONT));
         farmBuyButton.setBounds(700, 64, 250, 20);
         farmBuyButton.setEnabled(false);
         farmBuyButton.addActionListener(new ActionListener()
         {
             @Override
             public void actionPerformed(ActionEvent e)
             {
                 if (cookiemanager.buyPrice(Helper.getPriceForHelper(2)))
                 {
                     Helper.helpers[2].onBought();
                 }
             }
         });
         
         factoryBuyButton.setText("FACTORYS_HERE");
         factoryBuyButton.setFont(Font.getFont(FONT));
         factoryBuyButton.setBounds(700, 86, 250, 20);
         factoryBuyButton.setEnabled(false);
         factoryBuyButton.addActionListener(new ActionListener()
         {
             @Override
             public void actionPerformed(ActionEvent arg0)
             {
                 if (cookiemanager.buyPrice(Helper.getPriceForHelper(3)))
                 {
                     Helper.helpers[3].onBought();
                 }
             }
         });
         
         mineBuyButton.setText("MINES_HERE");
         mineBuyButton.setFont(Font.getFont(FONT));
         mineBuyButton.setBounds(700, 108, 250, 20);
         mineBuyButton.setEnabled(false);
         mineBuyButton.addActionListener(new ActionListener()
         {
             @Override
             public void actionPerformed(ActionEvent arg0)
             {
                 if (cookiemanager.buyPrice(Helper.getPriceForHelper(4)))
                 {
                     Helper.helpers[4].onBought();
                 }
             }
         });
         
         // ======================================//
         // ADD COMPONENTS TO THE PANE
         // ======================================//
         
         jframe.getContentPane().add(cookie_button);
         jframe.getContentPane().add(currentCookiesLabel);
         jframe.getContentPane().add(cookieRateLabel);
         jframe.getContentPane().add(pointerBuyButton);
         jframe.getContentPane().add(grandmaBuyButton);
         jframe.getContentPane().add(farmBuyButton);
         jframe.getContentPane().add(factoryBuyButton);
         jframe.getContentPane().add(mineBuyButton);
         jframe.getContentPane().add(infoLabel);
     }
     
     public void updateUI()
     {
         // ======================================//
         // UPDATE THE UI CONTENT
         // ======================================//
         
         if (currentCookiesLabel != null && cookieRateLabel != null && pointerBuyButton != null && grandmaBuyButton != null && farmBuyButton != null && factoryBuyButton != null && mineBuyButton != null)
         {
             // ======================================//
             // SET BUTTON / LABEL TEXTS
             // ======================================//
             
             currentCookiesLabel.setText("Current Cookies: " + cookiemanager.getCurrentCookies());
             cookieRateLabel.setText("Current Cookie Rate: " + onlyOneAfterComma(Helper.getCookieRate()));
             pointerBuyButton.setText(Helper.owned[0] + " Pointers | Buy for " + Helper.getPriceForHelper(0));
             grandmaBuyButton.setText(Helper.owned[1] + " Grandmas | Buy for " + Helper.getPriceForHelper(1));
             farmBuyButton.setText(Helper.owned[2] + " Farms | Buy for " + Helper.getPriceForHelper(2));
             factoryBuyButton.setText(Helper.owned[3] + " Factorys | Buy for " + Helper.getPriceForHelper(3));
             mineBuyButton.setText(Helper.owned[4] + " Mines | Buy for " + Helper.getPriceForHelper(4));
             
             // ======================================//
             // ENABLE / DISABLE BUTTONS
             // ======================================//
             
             /* POINTERS */
             if (cookiemanager.getCurrentCookies() >= Helper.getPriceForHelper(0))
             {
                 pointerBuyButton.setEnabled(true);
             }
             else
             {
                 pointerBuyButton.setEnabled(false);
             }
             
             /* GRANDMAS */
             if (cookiemanager.getCurrentCookies() >= Helper.getPriceForHelper(1))
             {
                 grandmaBuyButton.setEnabled(true);
             }
             else
             {
                 grandmaBuyButton.setEnabled(false);
             }
             
             /* FARMS */
             if (cookiemanager.getCurrentCookies() >= Helper.getPriceForHelper(2))
             {
                 farmBuyButton.setEnabled(true);
             }
             else
             {
                 farmBuyButton.setEnabled(false);
             }
             
             /* FACTORYS */
             if (cookiemanager.getCurrentCookies() >= Helper.getPriceForHelper(3))
             {
                 factoryBuyButton.setEnabled(true);
             }
             else
             {
                 factoryBuyButton.setEnabled(false);
             }
             
             /* MINES */
             if (cookiemanager.getCurrentCookies() >= Helper.getPriceForHelper(4))
             {
                 mineBuyButton.setEnabled(true);
             }
             else
             {
                 mineBuyButton.setEnabled(false);
             }
         }
         
     }
     
     public void setInfoMessage(String message)
     {
        infoLabel.setText("<html><strong>" + message + "</strong></html>");
     }
     
     private float onlyOneAfterComma(float input)
     {
         float x = (float) (Math.floor(input * 10F) / 10F);
         
         return x;
     }
 }
