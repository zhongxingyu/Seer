 package ch.jmnetwork.cookieclicker.ui;
 
 import java.awt.Color;
 import java.awt.EventQueue;
 import java.awt.Font;
 import java.awt.Image;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.File;
 import java.io.IOException;
 
 import javax.imageio.ImageIO;
 import javax.swing.BorderFactory;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.UIManager;
 
 import ch.jmnetwork.cookieclicker.CookieClickerMain;
 import ch.jmnetwork.cookieclicker.CookieManager;
 import ch.jmnetwork.cookieclicker.helper.Helper;
 import ch.jmnetwork.cookieclicker.helper.HelperClicker;
 import ch.jmnetwork.cookieclicker.lib.Reference;
 import ch.jmnetwork.cookieclicker.util.CryptedSLHandler;
import ch.jmnetwork.cookieclicker.util.NumberHelper;
 import ch.jmnetwork.cookieclicker.util.SaveLoadHandler;
 
 public class CCUserInterface
 {
     // ======================================//
     // VARIABLES
     // ======================================//
     
     private boolean run = false;
     private NumberHelper nh = new NumberHelper();
     
     private static CookieManager cookiemanager;
     private CryptedSLHandler cslhandler;
     
     private final static String FONT = "Arial";
     public final static Image cookie = Toolkit.getDefaultToolkit().getImage("cookie.png");
     private final static Image cookie_small = Toolkit.getDefaultToolkit().getImage("cookie_small.png");
     private final static ImageIcon cookieImageIcon = new ImageIcon(cookie);
     private final static ImageIcon cookie_smallImageIcon = new ImageIcon(cookie_small);
     private static String currentInfoMessage = "";
     
     public static JFrame jframe;
     private static JLabel infoLabel;
     private static JButton cookie_button;
     private static JLabel currentCookiesLabel;
     private static JLabel cookieRateLabel;
     private static JLabel cookiesHandmade;
     private static JLabel cookiesHandmadePerSec;
     private static JButton pointerBuyButton;
     private static JButton grandmaBuyButton;
     private static JButton farmBuyButton;
     private static JButton factoryBuyButton;
     private static JButton mineBuyButton;
     private static JButton shipmentBuyButton;
     private static JButton alchemyLabBuyButton;
     private static JButton portalBuyButton;
     private static JButton timeMachineBuyButton;
     private static JButton condenserBuyButton;
     private static JLabel totalCookies;
     
     public CCUserInterface(CookieManager cookieManager, SaveLoadHandler slhandler, CryptedSLHandler cslhandler)
     {
         cookiemanager = cookieManager;
         this.cslhandler = cslhandler;
         
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
         cookiesHandmade = new JLabel();
         cookiesHandmadePerSec = new JLabel();
         pointerBuyButton = new JButton();
         grandmaBuyButton = new JButton();
         farmBuyButton = new JButton();
         factoryBuyButton = new JButton();
         mineBuyButton = new JButton();
         shipmentBuyButton = new JButton();
         alchemyLabBuyButton = new JButton();
         portalBuyButton = new JButton();
         timeMachineBuyButton = new JButton();
         condenserBuyButton = new JButton();
         totalCookies = new JLabel();
         
         // ======================================//
         // COMPONENT SETTINGS
         // ======================================//
         
         jframe.setTitle("Java Cookie Clicker by TH3ON1YN00B and domi1819 - Version " + Reference.VERSION);
         jframe.setSize(1000, (265 + 80));
         jframe.getContentPane().setLayout(null);
         jframe.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
         jframe.setLocationRelativeTo(null);
         jframe.setResizable(false);
         jframe.setIconImage(cookie);
         if (new File("BackgroundNew.png").exists())
         {
             try
             {
                 jframe.setContentPane(new ImagePanel(ImageIO.read(new File("BackgroundNew.png"))));
             }
             catch (IOException e1)
             {
                 e1.printStackTrace();
             }
         }
         jframe.addWindowListener(new WindowAdapter()
         {
             @Override
             public void windowClosing(WindowEvent e)
             {
                 cslhandler.save();
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
                 cookiemanager.addHandmadeCookies(HelperClicker.cookiesPerClick());
                 updateUI();
                 
                 cslhandler.save();
             }
         });
         cookie_button.addMouseListener(new MouseListener()
         {
             
             @Override
             public void mouseReleased(MouseEvent arg0)
             {
                 if (arg0.getButton() == MouseEvent.BUTTON1) cookie_button.setIcon(cookieImageIcon);
             }
             
             @Override
             public void mousePressed(MouseEvent arg0)
             {
                 if (arg0.getButton() == MouseEvent.BUTTON1) cookie_button.setIcon(cookie_smallImageIcon);
             }
             
             @Override
             public void mouseExited(MouseEvent arg0)
             {
             }
             
             public void mouseEntered(MouseEvent arg0)
             {
             }
             
             public void mouseClicked(MouseEvent arg0)
             {
             }
             
         });
         
         currentCookiesLabel.setText("0");
         currentCookiesLabel.setFont(Font.getFont(FONT));
         currentCookiesLabel.setBounds(300, 20, 300, 50);
         
         cookieRateLabel.setText("");
         cookieRateLabel.setFont(Font.getFont(FONT));
         cookieRateLabel.setBounds(300, 30, 300, 50);
         
         cookiesHandmade.setText("HANDMADE_HERE");
         cookiesHandmade.setFont(Font.getFont(FONT));
         cookiesHandmade.setBounds(300, 40, 300, 50);
         
         cookiesHandmadePerSec.setText("CPS_H");
         cookiesHandmadePerSec.setFont(Font.getFont(FONT));
         cookiesHandmadePerSec.setBounds(300, 50, 300, 50);
         
         pointerBuyButton.setText("POINTERS_HERE");
         pointerBuyButton.setFont(Font.getFont(FONT));
         pointerBuyButton.setBounds(600, 20, 350, 20);
         pointerBuyButton.setEnabled(false);
         pointerBuyButton.setToolTipText(Helper.helpers[0].productivity + " Cookies / s");
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
         grandmaBuyButton.setBounds(600, 42, 350, 20);
         grandmaBuyButton.setEnabled(false);
         grandmaBuyButton.setToolTipText(Helper.helpers[1].productivity + " Cookies / s");
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
         farmBuyButton.setBounds(600, 64, 350, 20);
         farmBuyButton.setEnabled(false);
         farmBuyButton.setToolTipText(Helper.helpers[2].productivity + " Cookies / s");
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
         factoryBuyButton.setBounds(600, 86, 350, 20);
         factoryBuyButton.setEnabled(false);
         factoryBuyButton.setToolTipText(Helper.helpers[3].productivity + " Cookies / s");
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
         mineBuyButton.setBounds(600, 108, 350, 20);
         mineBuyButton.setEnabled(false);
         mineBuyButton.setToolTipText(Helper.helpers[4].productivity + " Cookies / s");
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
         
         shipmentBuyButton.setText("SHIPMENTS_HERE");
         shipmentBuyButton.setFont(Font.getFont(FONT));
         shipmentBuyButton.setBounds(600, 130, 350, 20);
         shipmentBuyButton.setEnabled(false);
         shipmentBuyButton.setToolTipText(Helper.helpers[5].productivity + " Cookies / s");
         shipmentBuyButton.addActionListener(new ActionListener()
         {
             @Override
             public void actionPerformed(ActionEvent arg0)
             {
                 if (cookiemanager.buyPrice(Helper.getPriceForHelper(5)))
                 {
                     Helper.helpers[5].onBought();
                 }
             }
         });
         
         alchemyLabBuyButton.setText("ALCHEMYLABS_HERE");
         alchemyLabBuyButton.setFont(Font.getFont(FONT));
         alchemyLabBuyButton.setBounds(600, 152, 350, 20);
         alchemyLabBuyButton.setEnabled(false);
         alchemyLabBuyButton.setToolTipText(Helper.helpers[6].productivity + " Cookies / s");
         alchemyLabBuyButton.addActionListener(new ActionListener()
         {
             @Override
             public void actionPerformed(ActionEvent arg0)
             {
                 if (cookiemanager.buyPrice(Helper.getPriceForHelper(6)))
                 {
                     Helper.helpers[6].onBought();
                 }
             }
         });
         
         portalBuyButton.setText("PORTALS_HERE");
         portalBuyButton.setFont(Font.getFont(FONT));
         portalBuyButton.setBounds(600, 174, 350, 20);
         portalBuyButton.setEnabled(false);
         portalBuyButton.setToolTipText(Helper.helpers[7].productivity + " Cookies / s");
         portalBuyButton.addActionListener(new ActionListener()
         {
             @Override
             public void actionPerformed(ActionEvent arg0)
             {
                 if (cookiemanager.buyPrice(Helper.getPriceForHelper(7)))
                 {
                     Helper.helpers[7].onBought();
                 }
             }
         });
         
         timeMachineBuyButton.setText("TIMEMACHINES_HERE");
         timeMachineBuyButton.setFont(Font.getFont(FONT));
         timeMachineBuyButton.setBounds(600, 196, 350, 20);
         timeMachineBuyButton.setEnabled(false);
         timeMachineBuyButton.setToolTipText(Helper.helpers[8].productivity + " Cookies / s");
         timeMachineBuyButton.addActionListener(new ActionListener()
         {
             @Override
             public void actionPerformed(ActionEvent arg0)
             {
                 if (cookiemanager.buyPrice(Helper.getPriceForHelper(8)))
                 {
                     Helper.helpers[8].onBought();
                 }
             }
         });
         
         condenserBuyButton.setText("CONDENSER");
         condenserBuyButton.setFont(Font.getFont(FONT));
         condenserBuyButton.setBounds(600, 218, 350, 20);
         condenserBuyButton.setEnabled(false);
         condenserBuyButton.setToolTipText(Helper.helpers[9].productivity + " Cookies / s");
         condenserBuyButton.addActionListener(new ActionListener()
         {
             @Override
             public void actionPerformed(ActionEvent event)
             {
                 if (cookiemanager.buyPrice(Helper.getPriceForHelper(9)))
                 {
                     Helper.helpers[9].onBought();
                 }
             }
         });
         
         totalCookies.setText("TOTAL_HERE");
         totalCookies.setFont(Font.getFont(FONT));
         totalCookies.setBounds(300, 70, 300, 40);
         
         // ======================================//
         // ADD COMPONENTS TO THE PANE
         // ======================================//
         
         jframe.getContentPane().add(cookie_button);
         jframe.getContentPane().add(currentCookiesLabel);
         jframe.getContentPane().add(cookieRateLabel);
         jframe.getContentPane().add(cookiesHandmade);
         jframe.getContentPane().add(cookiesHandmadePerSec);
         jframe.getContentPane().add(pointerBuyButton);
         jframe.getContentPane().add(grandmaBuyButton);
         jframe.getContentPane().add(farmBuyButton);
         jframe.getContentPane().add(factoryBuyButton);
         jframe.getContentPane().add(mineBuyButton);
         jframe.getContentPane().add(infoLabel);
         jframe.getContentPane().add(shipmentBuyButton);
         jframe.getContentPane().add(alchemyLabBuyButton);
         jframe.getContentPane().add(portalBuyButton);
         jframe.getContentPane().add(timeMachineBuyButton);
         jframe.getContentPane().add(condenserBuyButton);
         jframe.getContentPane().add(totalCookies);
     }
     
     public void updateUI()
     {
         // ======================================//
         // UPDATE THE UI CONTENT
         // ======================================//
         
         if (run == true || (totalCookies != null && cookiesHandmadePerSec != null && currentCookiesLabel != null && cookieRateLabel != null && pointerBuyButton != null && grandmaBuyButton != null && farmBuyButton != null && factoryBuyButton != null && mineBuyButton != null && shipmentBuyButton != null && alchemyLabBuyButton != null && portalBuyButton != null && timeMachineBuyButton != null && condenserBuyButton != null && cookiesHandmade != null))
         {
             run = true;
             
             // ======================================//
             // SET BUTTON / LABEL TEXTS
             // ======================================//
             
             currentCookiesLabel.setText("Current Cookies: " + nh.addApostropheToNumber(cookiemanager.getCurrentCookies()));
             cookieRateLabel.setText("Current Cookie Rate: " + onlyOneAfterComma(Helper.getCookieRate()));
             cookiesHandmade.setText("Handmade Cookies: " + nh.addApostropheToNumber(cookiemanager.getHandmadeCookies()));
             cookiesHandmadePerSec.setText("Handmade Cookies / s: " + CookieClickerMain.handmadePerSec);
             pointerBuyButton.setText(Helper.owned[0] + " Pointers | Buy for " + nh.addApostropheToNumber(Helper.getPriceForHelper(0)));
             grandmaBuyButton.setText(Helper.owned[1] + " Grandmas | Buy for " + nh.addApostropheToNumber(Helper.getPriceForHelper(1)));
             farmBuyButton.setText(Helper.owned[2] + " Farms | Buy for " + nh.addApostropheToNumber(Helper.getPriceForHelper(2)));
             factoryBuyButton.setText(Helper.owned[3] + " Factorys | Buy for " + nh.addApostropheToNumber(Helper.getPriceForHelper(3)));
             mineBuyButton.setText(Helper.owned[4] + " Mines | Buy for " + nh.addApostropheToNumber(Helper.getPriceForHelper(4)));
             shipmentBuyButton.setText(Helper.owned[5] + " Shipments | Buy for " + nh.addApostropheToNumber(Helper.getPriceForHelper(5)));
             alchemyLabBuyButton.setText(Helper.owned[6] + " Alchemy Labs | Buy for " + nh.addApostropheToNumber(Helper.getPriceForHelper(6)));
             portalBuyButton.setText(Helper.owned[7] + " Portals | Buy for " + nh.addApostropheToNumber(Helper.getPriceForHelper(7)));
             timeMachineBuyButton.setText(Helper.owned[8] + " Time Machines | Buy for " + nh.addApostropheToNumber(Helper.getPriceForHelper(8)));
             condenserBuyButton.setText(Helper.owned[9] + " Condensers | Buy for " + nh.addApostropheToNumber(Helper.getPriceForHelper(9)));
             totalCookies.setText("All-Time Cookies: " + nh.addApostropheToNumber(cookiemanager.getTotalCookies()));
             
             // ======================================//
             // ENABLE / DISABLE BUTTONS
             // ======================================//
             
             /* POINTERS */
             pointerBuyButton.setEnabled(cookiemanager.getCurrentCookies() >= Helper.getPriceForHelper(0));
             
             /* GRANDMAS */
             grandmaBuyButton.setEnabled(cookiemanager.getCurrentCookies() >= Helper.getPriceForHelper(1));
             
             /* FARMS */
             farmBuyButton.setEnabled(cookiemanager.getCurrentCookies() >= Helper.getPriceForHelper(2));
             
             /* FACTORYS */
             factoryBuyButton.setEnabled(cookiemanager.getCurrentCookies() >= Helper.getPriceForHelper(3));
             
             /* MINES */
             mineBuyButton.setEnabled(cookiemanager.getCurrentCookies() >= Helper.getPriceForHelper(4));
             
             /* SHIPMENTS */
             shipmentBuyButton.setEnabled(cookiemanager.getCurrentCookies() >= Helper.getPriceForHelper(5));
             
             /* ALCHEMYLAB */
             alchemyLabBuyButton.setEnabled(cookiemanager.getCurrentCookies() >= Helper.getPriceForHelper(6));
             
             /* PORTAL */
             portalBuyButton.setEnabled(cookiemanager.getCurrentCookies() >= Helper.getPriceForHelper(7));
             
             /* TIMEMACHINE */
             timeMachineBuyButton.setEnabled(cookiemanager.getCurrentCookies() >= Helper.getPriceForHelper(8));
             
             /* CONDENSER */
             condenserBuyButton.setEnabled(cookiemanager.getCurrentCookies() >= Helper.getPriceForHelper(9));
         }
         
     }
     
     public void setInfoMessage(String message)
     {
         infoLabel.setText("<html><strong>" + message + "</strong></html>");
         currentInfoMessage = message;
     }
     
     public String getInfoMessage()
     {
         return currentInfoMessage;
     }
     
     private float onlyOneAfterComma(float input)
     {
         return (float) (Math.floor(input * 10F) / 10F);
     }
 }
