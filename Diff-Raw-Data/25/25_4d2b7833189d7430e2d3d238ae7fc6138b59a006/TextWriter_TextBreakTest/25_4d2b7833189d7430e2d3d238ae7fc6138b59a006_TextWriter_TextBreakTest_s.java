 package net.sf.anathema.platform.tree.view.draw;
 
 import net.sf.anathema.framework.ui.Area;
 import net.sf.anathema.platform.tree.swing.SwingGraphicsCanvas;
 import org.junit.Before;
 import org.junit.Test;
 
 import java.awt.Font;
 import java.awt.Graphics2D;
import java.awt.Polygon;
 
 import static org.mockito.Matchers.isA;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.times;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 
 public class TextWriter_TextBreakTest {
 
   private LineSuggestion lineSuggestion = mock(LineSuggestion.class);
  private TextWriter writer = new TextWriter(new Polygon(new int[]{0, 100}, new int[]{0, 100}, 2), lineSuggestion);
   private Font font = new Font("SansSerif", Font.PLAIN, 15);
   private Graphics2D graphics = GraphicsMother.createForFont(font);
   private Canvas canvas = new SwingGraphicsCanvas(graphics);
 
   @Before
   public void setUp() throws Exception {
     writer.setText("Forceful Arrow");
   }
 
   @Test
   public void breaksText() throws Exception {
     when(lineSuggestion.suggestNumberOfLines(isA(Area.class))).thenReturn(2);
     writer.write(canvas);
     verify(graphics).drawString("Forceful", 50, 38);
   }
 
   @Test
   public void keepsBreaksOnceEstablished() throws Exception {
     when(lineSuggestion.suggestNumberOfLines(isA(Area.class))).thenReturn(2, 1);
     writer.write(canvas);
     writer.write(canvas);
     verify(graphics, times(2)).drawString("Forceful", 50, 38);
   }
 }
