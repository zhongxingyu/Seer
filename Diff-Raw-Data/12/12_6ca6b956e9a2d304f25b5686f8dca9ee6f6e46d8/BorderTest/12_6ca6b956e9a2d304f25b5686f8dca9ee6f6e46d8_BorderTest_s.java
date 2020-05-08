 package html2windows.css;
 
 import html2windows.css.BorderPainter;
 import html2windows.css.Style;
 import html2windows.dom.ElementInter;
 import html2windows.dom.Element;
 import html2windows.dom.Document;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Color;
 import java.awt.Dimension;
 
 import javax.swing.JPanel;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JButton;
 
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.awt.GridLayout;
 import java.awt.FlowLayout;
 
 import org.junit.AfterClass;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import static org.junit.Assert.*;
 import static java.lang.Thread.sleep;
 
 /**
  * Test draw border with element with user defined 
  * style. 
  *  
  * @author Jason Kuo
  */
 
 public class BorderTest {
 	private JFrame frame = new JFrame();
 	private JPanel panel=new JPanel();
 
 	@BeforeClass
     public static void setUpBeforeClass() throws Exception {
     }
 
     @AfterClass
     public static void tearDownAfterClass() throws Exception {
     }
     
     /**
      * Test custom usage of BorderPainter
      */
     @Test
     public void testCustomBorder(){
     	
     	/**
     	 * Setup
     	 * 
     	 * Create a JFrame frame and JPanel and create document
     	 * with defined getPainter() with BorderPainter(). Create
     	 * element with custom style setting and add to frame.  
     	 */
     	
     	//Create JFrame and JPanel 
     	JFrame frame = new JFrame("Button Frame");
         JPanel customPanel = new JPanel( new CustomLayoutManager());
 
         //Create document with BorderPainter()
         Document document = new Document(){
             @Override
             public CSSPainter getPainter(){
                 return new BorderPainter();
             }
         };
 
         //Create element with defined test properties
         String tagName = "div";
         ElementInter elementInter = new ElementInter(tagName);
         elementInter.setOwnerDocument(document);
 
         Element elementNode = elementInter;
         elementNode.setPreferredSize(new Dimension(100, 100));
 
         Style style = elementNode.getStyle();
         style.setProperty("border-width","2");
         style.setProperty("width","50");
         style.setProperty("height","5");
         style.setProperty("top","5");
         style.setProperty("left","10");
         style.setProperty("bottom","50");
         style.setProperty("border-style","dashed");
        style.setProperty("border-color","black");
         
         /**
          * Test
          * 
          * Add panel to Jframe and show
          * 
          * expect border with defined feature
          */
         customPanel.add(elementNode);
 
         frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
         frame.setSize(400,400);
         frame.setLocationRelativeTo(null);
         frame.add(customPanel);
         frame.setVisible(true);
         
         try{
         	Thread.sleep(2000);
         }catch(Exception e){}
     }
     
     @Test
     public void testIllegalProperty() throws Exception{
     	
     	/**
     	 * Setup
     	 * 
     	 * Create Jframe and panel with customLayoutManager.
     	 * Create document with BorderPainter.
     	 * Create element with illegal style properties in it.
     	 */
     	JFrame frame = new JFrame("Button Frame");
         JPanel customPanel = new JPanel( new CustomLayoutManager());
 
         Document document = new Document(){
             @Override
             public CSSPainter getPainter(){
                 return new BorderPainter();
             }
         };
         
         //create div
         String tagName = "div";
         ElementInter elementInter = new ElementInter(tagName);
         elementInter.setOwnerDocument(document);
 
         Element elementNode = elementInter;
         elementNode.setPreferredSize(new Dimension(100, 100));
 
         /*
          * Create style with width "a", top "b",
          * border-style "hello", and color "color" 
          */
         Style style = elementNode.getStyle();
         style.setProperty("border-width","2");
         style.setProperty("width","a");
         style.setProperty("height","20");
         style.setProperty("top","b");
         style.setProperty("left","10");
         style.setProperty("bottom","50");
         style.setProperty("border-style","hello");
        style.setProperty("border-color","black");
         
         /**
          * Test
          * 
          * Add panel to JFrame with element with illegal
          * style properties.
          * 
          * Expect non crashed JFrame with border.
          */
         customPanel.add(elementNode);
 
         frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
         frame.setSize(400,400);
         frame.setLocationRelativeTo(null);
         frame.add(customPanel);
         frame.setVisible(true);
         
         try{
         	Thread.sleep(2000);
         }catch(Exception e){}
     }
 }
