 package no.shhsoft.basus.language.eval;
 
 import java.awt.Graphics2D;
 import java.awt.RenderingHints;
 import java.awt.image.BufferedImage;
 
 import no.shhsoft.basus.language.eval.runtime.DrawingArea;
 import no.shhsoft.basus.utils.TextLocationHolder;
 import no.shhsoft.basus.value.BooleanValue;
 import no.shhsoft.basus.value.ImageValue;
 import no.shhsoft.basus.value.IntegerValue;
 import no.shhsoft.basus.value.NumericValue;
 import no.shhsoft.basus.value.SpriteValue;
 import no.shhsoft.basus.value.StringValue;
 import no.shhsoft.basus.value.Value;
 import no.shhsoft.basus.value.WidthAndHeightValue;
 
 /**
  * @author <a href="mailto:shh@thathost.com">Sverre H. Huseby</a>
  */
 final class GraphicFunctions {
 
     private static final Function WIDTH = new Width();
     private static final Function HEIGHT = new Height();
     private static final Function CLS = new Cls();
     private static final Function BACKGROUND = new Background();
     private static final Function COLOR = new Color();
     private static final Function PLOT = new Plot();
     private static final Function LINE = new Line();
     private static final Function RECT = new Rect();
     private static final Function FILL_RECT = new FillRect();
     private static final Function CIRCLE = new Circle();
     private static final Function FILL_CIRCLE = new FillCircle();
     private static final Function SHAPE = new Shape();
     private static final Function FILL_SHAPE = new FillShape();
     private static final Function AUTO_FLUSH = new AutoFlush();
     private static final Function FLUSH = new Flush();
     private static final Function GET_RED = new GetRed();
     private static final Function GET_GREEN = new GetGreen();
     private static final Function GET_BLUE = new GetBlue();
     private static final Function IS_BACKGROUND = new IsBackground();
     private static final Function CAPTURE_BACKGROUND = new CaptureBackground();
     private static final Function CLEAR_BACKGROUND = new ClearBackground();
     private static final Function LOAD_IMAGE = new LoadImage();
     private static final Function SCALE_IMAGE = new ScaleImage();
     private static final Function SUB_IMAGE = new SubImage();
     private static final Function DRAW_IMAGE = new DrawImage();
     private static final Function SPRITE_MOVE = new SpriteMove();
     private static final Function SPRITE_X = new SpriteX();
     private static final Function SPRITE_Y = new SpriteY();
     private static final Function SPRITE_VISIBLE = new SpriteVisible();
     private static final Function SPRITE_DEPTH = new SpriteDepth();
     private static final Function SPRITE_COLLISION = new SpriteCollision();
 
     private GraphicFunctions() {
     }
 
     private static final class Width
     extends BuiltinFunction {
 
         @Override
         protected Value implCall(final EvaluationContext context,
                                  final TextLocationHolder locationHolder, final Value[] args) {
             if (args.length == 0) {
                 return IntegerValue.get(context.getDrawingArea().getAreaWidth());
             }
             return IntegerValue.get(((WidthAndHeightValue) args[0]).getWidth());
         }
 
         public Width() {
             super("width", Function.NUM_ARGS_ZERO_OR_ONE,
                   new Class<?>[] { WidthAndHeightValue.class });
         }
 
     }
 
     private static final class Height
     extends BuiltinFunction {
 
         @Override
         protected Value implCall(final EvaluationContext context,
                                  final TextLocationHolder locationHolder, final Value[] args) {
             if (args.length == 0) {
                 return IntegerValue.get(context.getDrawingArea().getAreaHeight());
             }
             return IntegerValue.get(((WidthAndHeightValue) args[0]).getHeight());
         }
 
         public Height() {
             super("height", Function.NUM_ARGS_ZERO_OR_ONE,
                   new Class<?>[] { WidthAndHeightValue.class });
         }
 
     }
 
     private static final class Cls
     extends BuiltinFunction {
 
         @Override
         protected Value implCall(final EvaluationContext context,
                                  final TextLocationHolder locationHolder, final Value[] args) {
             context.getDrawingArea().clear();
             return IntegerValue.ZERO;
         }
 
         public Cls() {
             super("cls", 0, null);
         }
 
     }
 
     private static final class Background
     extends BuiltinFunction {
 
         @Override
         protected Value implCall(final EvaluationContext context,
                                  final TextLocationHolder locationHolder, final Value[] args) {
             context.getDrawingArea().setBackgroundColor(
                                            ((NumericValue) args[0]).getValueAsInteger(),
                                            ((NumericValue) args[1]).getValueAsInteger(),
                                            ((NumericValue) args[2]).getValueAsInteger());
             return IntegerValue.ZERO;
         }
 
         public Background() {
             super("background", 3, new Class<?>[] { NumericValue.class });
         }
 
     }
 
     private static final class Color
     extends BuiltinFunction {
 
         @Override
         protected Value implCall(final EvaluationContext context,
                                  final TextLocationHolder locationHolder, final Value[] args) {
             final int r = ((NumericValue) args[0]).getValueAsInteger();
             final int g = ((NumericValue) args[1]).getValueAsInteger();
             final int b = ((NumericValue) args[2]).getValueAsInteger();
             int a = 255;
             if (args.length > 3) {
                 a = ((NumericValue) args[3]).getValueAsInteger();
             }
             context.getDrawingArea().setForegroundColor(r, g, b, a);
             return IntegerValue.ZERO;
         }
 
         public Color() {
             super("color", Function.NUM_ARGS_THREE_OR_FOUR, new Class<?>[] { NumericValue.class });
         }
 
     }
 
     private static final class Plot
     extends BuiltinFunction {
 
         @Override
         protected Value implCall(final EvaluationContext context,
                                  final TextLocationHolder locationHolder, final Value[] args) {
             context.getDrawingArea().plot(((NumericValue) args[0]).getValueAsInteger(),
                                           ((NumericValue) args[1]).getValueAsInteger());
             return IntegerValue.ZERO;
         }
 
         public Plot() {
             super("plot", 2, new Class<?>[] { NumericValue.class });
         }
 
     }
 
     private static final class Line
     extends BuiltinFunction {
 
         @Override
         protected Value implCall(final EvaluationContext context,
                                  final TextLocationHolder locationHolder, final Value[] args) {
             context.getDrawingArea().line(((NumericValue) args[0]).getValueAsInteger(),
                                           ((NumericValue) args[1]).getValueAsInteger(),
                                           ((NumericValue) args[2]).getValueAsInteger(),
                                           ((NumericValue) args[3]).getValueAsInteger());
             return IntegerValue.ZERO;
         }
 
         public Line() {
             super("line", 4, new Class<?>[] { NumericValue.class });
         }
 
     }
 
     private static final class Rect
     extends BuiltinFunction {
 
         @Override
         protected Value implCall(final EvaluationContext context,
                                  final TextLocationHolder locationHolder, final Value[] args) {
             context.getDrawingArea().rect(((NumericValue) args[0]).getValueAsInteger(),
                                           ((NumericValue) args[1]).getValueAsInteger(),
                                           ((NumericValue) args[2]).getValueAsInteger(),
                                           ((NumericValue) args[3]).getValueAsInteger());
             return IntegerValue.ZERO;
         }
 
         public Rect() {
             super("rect", 4, new Class<?>[] { NumericValue.class });
         }
 
     }
 
     private static final class FillRect
     extends BuiltinFunction {
 
         @Override
         protected Value implCall(final EvaluationContext context,
                                  final TextLocationHolder locationHolder, final Value[] args) {
             context.getDrawingArea().fillRect(((NumericValue) args[0]).getValueAsInteger(),
                                               ((NumericValue) args[1]).getValueAsInteger(),
                                               ((NumericValue) args[2]).getValueAsInteger(),
                                               ((NumericValue) args[3]).getValueAsInteger());
             return IntegerValue.ZERO;
         }
 
         public FillRect() {
             super("fillRect", 4, new Class<?>[] { NumericValue.class });
         }
 
     }
 
     private static final class Circle
     extends BuiltinFunction {
 
         @Override
         protected Value implCall(final EvaluationContext context,
                                  final TextLocationHolder locationHolder, final Value[] args) {
             context.getDrawingArea().circle(((NumericValue) args[0]).getValueAsInteger(),
                                             ((NumericValue) args[1]).getValueAsInteger(),
                                             ((NumericValue) args[2]).getValueAsInteger());
             return IntegerValue.ZERO;
         }
 
         public Circle() {
             super("circle", 3, new Class<?>[] { NumericValue.class });
         }
 
     }
 
     private static final class FillCircle
     extends BuiltinFunction {
 
         @Override
         protected Value implCall(final EvaluationContext context,
                                  final TextLocationHolder locationHolder, final Value[] args) {
             context.getDrawingArea().fillCircle(((NumericValue) args[0]).getValueAsInteger(),
                                                 ((NumericValue) args[1]).getValueAsInteger(),
                                                 ((NumericValue) args[2]).getValueAsInteger());
             return IntegerValue.ZERO;
         }
 
         public FillCircle() {
             super("fillCircle", 3, new Class<?>[] { NumericValue.class });
         }
 
     }
 
     private abstract static class AbstractShape
     extends BuiltinFunction {
 
         private final boolean fill;
 
         @SuppressWarnings("boxing")
         @Override
         protected final Value implCall(final EvaluationContext context,
                                        final TextLocationHolder locationHolder,
                                        final Value[] args) {
             if (args.length < 6) {
                 error("err.funcArgCountEvenN", locationHolder, getName(), 6);
             }
             final int numPoints = args.length / 2;
             final int[] xPoints = new int[numPoints];
             final int[] yPoints = new int[numPoints];
             for (int q = 0; q < args.length; q++) {
                 if ((q & 1) == 0) {
                     xPoints[q / 2] = ((NumericValue) args[q]).getValueAsInteger();
                 } else {
                     yPoints[q / 2] = ((NumericValue) args[q]).getValueAsInteger();
                 }
             }
             if (fill) {
                 context.getDrawingArea().fillShape(xPoints, yPoints, numPoints);
             } else {
                 context.getDrawingArea().shape(xPoints, yPoints, numPoints);
             }
             return IntegerValue.ZERO;
         }
 
         public AbstractShape(final String name, final boolean fill) {
             super(name, Function.NUM_ARGS_ANY_EVEN,
                   new Class<?>[] { NumericValue.class });
             this.fill = fill;
         }
 
     }
 
     private static final class Shape
     extends AbstractShape {
 
         public Shape() {
             super("shape", false);
         }
 
     }
 
     private static final class FillShape
     extends AbstractShape {
 
         public FillShape() {
             super("fillShape", true);
         }
 
     }
 
     private static final class AutoFlush
     extends BuiltinFunction {
 
         @Override
         protected Value implCall(final EvaluationContext context,
                                  final TextLocationHolder locationHolder, final Value[] args) {
             context.getDrawingArea().setAutoFlush(((BooleanValue) args[0]).getValue());
             return IntegerValue.ZERO;
         }
 
         public AutoFlush() {
             super("autoFlush", 1, new Class<?>[] { BooleanValue.class });
         }
 
     }
 
     private static final class Flush
     extends BuiltinFunction {
 
         @Override
         protected Value implCall(final EvaluationContext context,
                                  final TextLocationHolder locationHolder, final Value[] args) {
             context.getDrawingArea().flush();
             return IntegerValue.ZERO;
         }
 
         public Flush() {
             super("flush", 0, null);
         }
 
     }
 
     private static final class GetRed
     extends BuiltinFunction {
 
         @Override
         protected Value implCall(final EvaluationContext context,
                                  final TextLocationHolder locationHolder, final Value[] args) {
             return IntegerValue.get(context.getDrawingArea().getRed(
                                             ((NumericValue) args[0]).getValueAsInteger(),
                                             ((NumericValue) args[1]).getValueAsInteger()));
         }
 
         public GetRed() {
             super("getRed", 2, new Class<?>[] { NumericValue.class });
         }
 
     }
 
     private static final class GetGreen
     extends BuiltinFunction {
 
         @Override
         protected Value implCall(final EvaluationContext context,
                                  final TextLocationHolder locationHolder, final Value[] args) {
             return IntegerValue.get(context.getDrawingArea().getGreen(
                                             ((NumericValue) args[0]).getValueAsInteger(),
                                             ((NumericValue) args[1]).getValueAsInteger()));
         }
 
         public GetGreen() {
             super("getGreen", 2, new Class<?>[] { NumericValue.class });
         }
 
     }
 
     private static final class GetBlue
     extends BuiltinFunction {
 
         @Override
         protected Value implCall(final EvaluationContext context,
                                  final TextLocationHolder locationHolder, final Value[] args) {
             return IntegerValue.get(context.getDrawingArea().getBlue(
                                             ((NumericValue) args[0]).getValueAsInteger(),
                                             ((NumericValue) args[1]).getValueAsInteger()));
         }
 
         public GetBlue() {
             super("getBlue", 2, new Class<?>[] { NumericValue.class });
         }
 
     }
 
     private static final class IsBackground
     extends BuiltinFunction {
 
         @Override
         protected Value implCall(final EvaluationContext context,
                                  final TextLocationHolder locationHolder, final Value[] args) {
             return BooleanValue.valueOf(context.getDrawingArea().isBackground(
                                                 ((NumericValue) args[0]).getValueAsInteger(),
                                                 ((NumericValue) args[1]).getValueAsInteger()));
         }
 
         public IsBackground() {
             super("isBackground", 2, new Class<?>[] { NumericValue.class });
         }
 
     }
 
     private static final class CaptureBackground
     extends BuiltinFunction {
 
         @Override
         protected Value implCall(final EvaluationContext context,
                                  final TextLocationHolder locationHolder, final Value[] args) {
             context.getDrawingArea().captureBackgroundImage();
             return IntegerValue.ZERO;
         }
 
         public CaptureBackground() {
             super("captureBackground", 0, null);
         }
 
     }
 
     private static final class ClearBackground
     extends BuiltinFunction {
 
         @Override
         protected Value implCall(final EvaluationContext context,
                                  final TextLocationHolder locationHolder, final Value[] args) {
             context.getDrawingArea().captureBackgroundImage();
             return IntegerValue.ZERO;
         }
 
         public ClearBackground() {
             super("clearBackground", 0, null);
         }
 
     }
 
     private static final class LoadImage
     extends BuiltinFunction {
 
         @Override
         protected Value implCall(final EvaluationContext context,
                                  final TextLocationHolder locationHolder, final Value[] args) {
             final String name = ((StringValue) args[0]).getValue();
             return context.loadImage(name, locationHolder);
         }
 
         public LoadImage() {
             super("loadImage", 1, new Class<?>[] { StringValue.class });
         }
 
     }
 
     private static final class ScaleImage
     extends BuiltinFunction {
 
         @Override
         protected Value implCall(final EvaluationContext context,
                                  final TextLocationHolder locationHolder, final Value[] args) {
             final BufferedImage image = ((ImageValue) args[0]).getValue();
             final int width = ((NumericValue) args[1]).getValueAsInteger();
             final int height = ((NumericValue) args[2]).getValueAsInteger();
            final BufferedImage scaledImage = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
             final Graphics2D g2 = scaledImage.createGraphics();
             g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
             g2.drawImage(image, 0, 0, width, height, 0, 0, image.getWidth(), image.getHeight(), null);
             g2.dispose();
             return new ImageValue(scaledImage);
         }
 
         public ScaleImage() {
             super("scaleImage", 3, new Class<?>[] { ImageValue.class, NumericValue.class });
         }
 
     }
 
     private static final class SubImage
     extends BuiltinFunction {
 
         @Override
         protected Value implCall(final EvaluationContext context,
                                  final TextLocationHolder locationHolder, final Value[] args) {
             final BufferedImage image = ((ImageValue) args[0]).getValue();
             final int x = ((NumericValue) args[1]).getValueAsInteger();
             int y = ((NumericValue) args[2]).getValueAsInteger();
             y = image.getHeight() - y - 1;
             final int width = ((NumericValue) args[3]).getValueAsInteger();
             final int height = ((NumericValue) args[4]).getValueAsInteger();
             return new ImageValue(image.getSubimage(x, y, width, height));
         }
 
         public SubImage() {
             super("subImage", 5, new Class<?>[] { ImageValue.class, NumericValue.class });
         }
 
     }
 
     private static final class DrawImage
     extends BuiltinFunction {
 
         @Override
         protected Value implCall(final EvaluationContext context,
                                  final TextLocationHolder locationHolder, final Value[] args) {
             context.getDrawingArea().drawImage(((ImageValue) args[0]).getValue(),
                                                ((NumericValue) args[1]).getValueAsInteger(),
                                                ((NumericValue) args[2]).getValueAsInteger());
             return IntegerValue.ZERO;
         }
 
         public DrawImage() {
             super("drawImage", 3, new Class<?>[] { ImageValue.class, NumericValue.class });
         }
 
     }
 
     private abstract static class AbstractSpriteFunction
     extends BuiltinFunction {
 
         protected abstract Value implImplCall(int spriteIndex, DrawingArea drawingArea,
                                               Value[] args);
 
         @Override
         protected final Value implCall(final EvaluationContext context,
                                        final TextLocationHolder locationHolder,
                                        final Value[] args) {
             final int spriteIndex = ((SpriteValue) args[0]).getValue();
             return implImplCall(spriteIndex, context.getDrawingArea(), args);
         }
 
         public AbstractSpriteFunction(final String name, final int numArgs,
                                       final Class<?>[] argTypes) {
             super(name, numArgs, argTypes);
         }
 
     }
 
     private static final class SpriteMove
     extends AbstractSpriteFunction {
 
         @Override
         protected Value implImplCall(final int spriteIndex, final DrawingArea drawingArea,
                                      final Value[] args) {
             drawingArea.setSpritePosition(spriteIndex,
                                           ((NumericValue) args[1]).getValueAsInteger(),
                                           ((NumericValue) args[2]).getValueAsInteger());
             return IntegerValue.ZERO;
         }
 
         public SpriteMove() {
             super("spriteMove", 3, new Class<?>[] { SpriteValue.class, NumericValue.class });
         }
 
     }
 
     private static final class SpriteX
     extends AbstractSpriteFunction {
 
         @Override
         protected Value implImplCall(final int spriteIndex, final DrawingArea drawingArea,
                                      final Value[] args) {
             return IntegerValue.get(drawingArea.getSpriteX(spriteIndex));
         }
 
         public SpriteX() {
             super("spriteX", 1, new Class<?>[] { SpriteValue.class });
         }
 
     }
 
     private static final class SpriteY
     extends AbstractSpriteFunction {
 
         @Override
         protected Value implImplCall(final int spriteIndex, final DrawingArea drawingArea,
                                      final Value[] args) {
             return IntegerValue.get(drawingArea.getSpriteY(spriteIndex));
         }
 
         public SpriteY() {
             super("spriteY", 1, new Class<?>[] { SpriteValue.class });
         }
 
     }
 
     private static final class SpriteVisible
     extends AbstractSpriteFunction {
 
         @Override
         protected Value implImplCall(final int spriteIndex, final DrawingArea drawingArea,
                                      final Value[] args) {
             if (args.length > 1) {
                 drawingArea.setSpriteVisible(spriteIndex, ((BooleanValue) args[1]).getValue());
             }
             return BooleanValue.valueOf(drawingArea.isSpriteVisible(spriteIndex));
         }
 
         public SpriteVisible() {
             super("spriteVisible", Function.NUM_ARGS_ONE_OR_TWO,
                   new Class<?>[] { SpriteValue.class, BooleanValue.class });
         }
 
     }
 
     private static final class SpriteDepth
     extends AbstractSpriteFunction {
 
         @Override
         protected Value implImplCall(final int spriteIndex, final DrawingArea drawingArea,
                                      final Value[] args) {
             if (args.length > 1) {
                 drawingArea.setSpriteDepth(spriteIndex,
                                            ((NumericValue) args[1]).getValueAsInteger());
             }
             return IntegerValue.get(drawingArea.getSpriteDepth(spriteIndex));
         }
 
         public SpriteDepth() {
             super("spriteDepth", Function.NUM_ARGS_ONE_OR_TWO,
                   new Class<?>[] { SpriteValue.class, NumericValue.class });
         }
 
     }
 
     private static final class SpriteCollision
     extends BuiltinFunction {
 
         @Override
         protected Value implCall(final EvaluationContext context,
                                  final TextLocationHolder locationHolder, final Value[] args) {
             return BooleanValue.valueOf(context.getDrawingArea().isSpriteCollision(
                                                  ((SpriteValue) args[0]).getValue(),
                                                  ((SpriteValue) args[1]).getValue()));
         }
 
         public SpriteCollision() {
             super("spriteCollision", 2, new Class<?>[] { SpriteValue.class });
         }
 
     }
 
 
     public static void register(final SimpleEvaluationContext context) {
         context.registerFunction(WIDTH);
         context.registerFunction(HEIGHT);
         context.registerFunction(CLS);
         context.registerFunction(BACKGROUND);
         context.registerFunction(COLOR);
         context.registerFunction(PLOT);
         context.registerFunction(LINE);
         context.registerFunction(RECT);
         context.registerFunction(FILL_RECT);
         context.registerFunction(CIRCLE);
         context.registerFunction(FILL_CIRCLE);
         context.registerFunction(SHAPE);
         context.registerFunction(FILL_SHAPE);
         context.registerFunction(AUTO_FLUSH);
         context.registerFunction(FLUSH);
         context.registerFunction(GET_RED);
         context.registerFunction(GET_GREEN);
         context.registerFunction(GET_BLUE);
         context.registerFunction(IS_BACKGROUND);
         context.registerFunction(CAPTURE_BACKGROUND);
         context.registerFunction(CLEAR_BACKGROUND);
         context.registerFunction(LOAD_IMAGE);
         context.registerFunction(SCALE_IMAGE);
         context.registerFunction(SUB_IMAGE);
         context.registerFunction(DRAW_IMAGE);
         context.registerFunction(SPRITE_MOVE);
         context.registerFunction(SPRITE_X);
         context.registerFunction(SPRITE_Y);
         context.registerFunction(SPRITE_VISIBLE);
         context.registerFunction(SPRITE_DEPTH);
         context.registerFunction(SPRITE_COLLISION);
     }
 
 }
