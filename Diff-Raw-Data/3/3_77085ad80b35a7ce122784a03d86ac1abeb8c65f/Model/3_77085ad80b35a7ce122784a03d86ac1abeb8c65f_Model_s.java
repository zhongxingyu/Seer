 package model;
 
 import java.awt.Dimension;
 import java.util.TreeMap;
 import model.ParserException.Type;
 import model.expressions.Expression;
 
 
 /**
  * Evaluate an expression for each pixel in a image.
  * 
  * @author Robert C Duvall
  */
 public class Model
 {
     // single instance of model
     private static Model myModel;
 
     // state of the model
     public static final double DOMAIN_MIN = -1;
     public static final double DOMAIN_MAX = 1;
     public static final int NUM_FRAMES = 50;
     private double myCurrentTime = 0;
 
     private static TreeMap<String, RGBColor> variableMap;
 
     private Model ()
     {}
 
     public static Model getInstance ()
     {
         if (myModel == null)
         {
             myModel = new Model();
             variableMap = new TreeMap<String, RGBColor>();
         }
         return myModel;
     }
 
     /**
      * Evaluate an expression for each point in the image.
      */
     public Pixmap evaluate (String input, Dimension size)
     {
         Pixmap result = new Pixmap(size);
         // create expression to evaluate just once
         Expression toEval = Parser.getInstance().makeExpression(input);
         // evaluate at each pixel
         for (int imageY = 0; imageY < size.height; imageY++)
         {
             variableMap.put("y", new RGBColor(imageToDomainScale(imageY, size.height)));
             for (int imageX = 0; imageX < size.width; imageX++)
             {
                 variableMap.put("x", new RGBColor(imageToDomainScale(imageX, size.width)));
                variableMap.put("t", new RGBColor(myCurrentTime * 2 - 1));
                 result.setColor(imageX, imageY, toEval.evaluate().toJavaColor());
             }
         }
         return result;
     }
 
     /**
      * Advance to the next frame in the animation.
      */
     public void reset ()
     {
         myCurrentTime = 0;
     }
 
     /**
      * Advance to the next frame in the animation.
      */
     public void nextFrame ()
     {
         myCurrentTime += 1.0 / NUM_FRAMES;
     }
 
     public RGBColor getValue (String variable)
     {
         RGBColor value = variableMap.get(variable);
         if (value == null)
                           throw new ParserException("Undefined variable: " + variable,
                                                     Type.UNDEFINED_VARIABLE);
         return value;
     }
 
     public void storeMapping (String variable, RGBColor color)
     {
         variableMap.put(variable, color);
     }
 
     public void removeMapping (String variable)
     {
         variableMap.remove(variable);
     }
 
     /**
      * Convert from image space to domain space.
      */
     private double imageToDomainScale (int value, int bounds)
     {
         double range = DOMAIN_MAX - DOMAIN_MIN;
         return ((double) value / bounds) * range + DOMAIN_MIN;
     }
 
 }
