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
 
 public class BorderTest {
 	private JFrame frame = new JFrame();
 	private JPanel panel=new JPanel();
 
 	@BeforeClass
     public static void setUpBeforeClass() throws Exception {
     }
 
     @AfterClass
     public static void tearDownAfterClass() throws Exception {
     }
     
     @Test
     public void test() throws Exception{
     	JFrame frame = new JFrame("Button Frame");
         JPanel customPanel = new JPanel( new CustomLayoutManager());
 
         Document document = new Document(){
             @Override
             public CSSPainter getPainter(){
                 return new BorderPainter();
             }
         };
        
 
         String tagName = "div";
         ElementInter elementInter = new ElementInter(tagName);
         elementInter.setOwnerDocument(document);
 
         Element elementNode = elementInter;
         elementNode.setPreferredSize(new Dimension(100, 100));
 
         Style style = elementNode.getStyle();
 
         
         style.setProperty("border-width","2");
         style.setProperty("width","50");
         style.setProperty("height","50");
         style.setProperty("top","5");
         style.setProperty("left","5");
         style.setProperty("bottom","50");
         style.setProperty("border-style","dashed");
         style.setProperty("border-color","black");
         
 
         //customPanel.add(btn);
         customPanel.add(elementNode);
 
         frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
         frame.setSize(400,400);
         frame.setLocationRelativeTo(null);
         frame.add(customPanel);
         frame.setVisible(true);
         
         Thread.sleep(2000);
     }
 }
