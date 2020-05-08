 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package evopaint;
 
 import evopaint.pixel.ColorDimensions;
 import evopaint.pixel.Pixel;
 import evopaint.pixel.PixelColor;
 import evopaint.pixel.rulebased.Rule;
 import evopaint.pixel.rulebased.RuleBasedPixel;
 import evopaint.pixel.rulebased.RuleSet;
 import evopaint.pixel.rulebased.actions.AssimilationAction;
 import evopaint.pixel.rulebased.actions.NoAction;
 import evopaint.pixel.rulebased.actions.RewardAction;
 import evopaint.pixel.rulebased.conditions.ColorLikenessCondition;
 import evopaint.pixel.rulebased.conditions.EnergyCondition;
 import evopaint.pixel.rulebased.conditions.NoCondition;
 import evopaint.pixel.rulebased.interfaces.IAction;
 import evopaint.pixel.rulebased.interfaces.ICondition;
 import evopaint.pixel.rulebased.interfaces.IRule;
 import evopaint.pixel.rulebased.util.NumberComparisonOperator;
 import evopaint.util.mapping.AbsoluteCoordinate;
 import evopaint.util.mapping.RelativeCoordinate;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  *
  * @author tam
  */
 public class Brush {
     public static final int COLOR = 0;
     public static final int FAIRY_DUST = 1;
     public static final int USE_EXISTING = 2;
 
     private Configuration configuration;
 
     private int brushSize;
     private int mode;
     
     private PixelColor color;
     // we don't know the location yet
     // private AbsoluteCoordinate location;
     private int energy;
     private RuleSet ruleSet;
 
     public int getBrushSize() {
         return brushSize;
     }
 
     public void setBrushSize(int radius) {
         this.brushSize = radius;
     }
 
     public int getMode() {
         return mode;
     }
 
     public void setMode(int mode) {
         this.mode = mode;
     }
 
     public int getEnergy() {
         return energy;
     }
 
     public void setEnergy(int energy) {
         this.energy = energy;
     }
 
     public PixelColor getColor() {
         return color;
     }
 
     public void setColor(PixelColor color) {
         this.color = color;
     }
 
     public RuleSet getRuleSet() {
         return ruleSet;
     }
 
     public void setRuleSet(RuleSet ruleSet) {
         this.ruleSet = ruleSet;
     }
 
     private RuleSet noRuleSet() {
         List<IRule> rules = new ArrayList<IRule>();
 
         List<ICondition> conditions = new ArrayList<ICondition>();
         List<RelativeCoordinate> directions = new ArrayList<RelativeCoordinate>();
         directions.add(RelativeCoordinate.SELF);
         conditions.add(new NoCondition());
         IAction action = new NoAction();
         rules.add(new Rule(conditions, action));
 
         return new RuleSet("No RuleSet", "You should not be able to read this, well okay by digging the source you should. In that case: Welcome to the EvoPaint source code, enjoy your stay and copy all you like!", rules);
     }
 
     public void paint(int x, int y) {
         
         for (int i = 0 - brushSize / 2 ; i < (int)Math.ceil((double)brushSize / 2); i++) {
             for (int j = 0 - brushSize / 2; j < (int)Math.ceil((double)brushSize / 2); j++) {
                PixelColor newColor = new PixelColor(color);
                 switch (mode) {
                     case COLOR:
                         break;
                     case FAIRY_DUST:
                        newColor.setInteger(configuration.rng.nextPositiveInt(), configuration.rng);
                         break;
                     case USE_EXISTING:
                         Pixel pixie = configuration.world.get(x + j, y + i);
                         if (pixie == null) {
                             continue;
                         }
                        newColor.setHSB(pixie.getPixelColor().getHSB());
                         break;
                     default:
                         assert(false);
                 }
 
                 Pixel newPixel = null;
                 switch (configuration.pixelType) {
                     case Pixel.RULESET:
                        newPixel = new RuleBasedPixel(newColor, new AbsoluteCoordinate(x + j, y + i, configuration.world), energy, ruleSet);
                         break;
                     default: assert(false);
                 }
                 configuration.world.set(newPixel);
             }
         }
     }
 
     public Brush(Configuration configuration) {
         this.configuration = configuration;
         this.energy = configuration.startingEnergy;
         this.brushSize = 10;
         this.mode = COLOR;
         this.color = new PixelColor(0xFF0000, configuration.rng);
         this.ruleSet = noRuleSet();
     }
 }
