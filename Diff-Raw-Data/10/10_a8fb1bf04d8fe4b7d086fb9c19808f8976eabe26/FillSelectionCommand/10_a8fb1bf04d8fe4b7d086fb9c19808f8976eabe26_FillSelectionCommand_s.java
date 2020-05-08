 package evopaint.commands;
 
 import evopaint.Brush;
 import evopaint.Configuration;
 import evopaint.Selection;
 import evopaint.gui.Showcase;
 import evopaint.pixel.Pixel;
 import evopaint.pixel.PixelColor;
import evopaint.pixel.RuleSet;
 import evopaint.util.mapping.AbsoluteCoordinate;
 
 import java.awt.geom.AffineTransform;
 
 /**
  * Created by IntelliJ IDEA.
  * User: daniel
  * Date: 25.03.2010
  * Time: 23:23:20
  * To change this template use File | Settings | File Templates.
  */
 public class FillSelectionCommand extends AbstractCommand {
     private Configuration configuration;
     private double scale;
     private AffineTransform affineTransform;
     private Selection selection;
     private PixelColor color;
     private Brush brush;
     private int energy;
     private RuleSet ruleSet;
     private Showcase showcase;
     protected int density = 1;
 
     public FillSelectionCommand(Showcase showcase) {
         this.showcase = showcase;
         this.configuration = showcase.getConfiguration();
         this.scale = showcase.getScale();
         this.affineTransform = showcase.getAffineTransform();
 
         this.energy = configuration.startingEnergy;
         this.ruleSet = configuration.createDefaultRuleSet();
     }
 
     public void execute() {
         this.color = configuration.brush.getColor();
         this.selection = showcase.getActiveSelection();
         for (int x = selection.getStartPoint().x; x < selection.getEndPoint().x; x++){
             for (int y = selection.getStartPoint().y; y < selection.getEndPoint().y; y++){
                if ((x % density) == 0) continue;
                if ((y % density) == 0) continue;
                Pixel newPixel = new Pixel(new PixelColor(color), new AbsoluteCoordinate(x, y, configuration.world), energy, ruleSet);
                 configuration.world.set(newPixel);
             }
         }
 
     }
 }
