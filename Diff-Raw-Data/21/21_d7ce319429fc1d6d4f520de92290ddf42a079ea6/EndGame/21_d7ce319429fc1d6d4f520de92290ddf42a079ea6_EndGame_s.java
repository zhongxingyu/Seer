 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.first.tribes.core;
 
 import com.first.tribes.core.util.Updatee;
 import java.util.ArrayList;
 import java.util.List;
 import playn.core.Canvas;
 import playn.core.CanvasImage;
 import playn.core.Color;
 import playn.core.Font;
 import playn.core.ImmediateLayer.Renderer;
 import playn.core.Keyboard;
 import playn.core.Mouse;
 import static playn.core.PlayN.*;
 import playn.core.Pointer;
 import playn.core.Surface;
 import playn.core.TextFormat;
 import playn.core.TextLayout;
 
 /**
  *
  * @author taylor
  */
 public class EndGame implements Updatee {
 
     private Tribes game;
 
     public EndGame(Tribes game) {
         this.game = game;
     }
 
     @Override
     public void update(float delta) {
        EndGameDialog dialog = null;
         if (game.world.villages().get(0).villagers().size() == 0) {
             dialog = new LoseDialog();
         } else if (game.world.villages().get(1).villagers().size() == 0) {
             dialog = new WinDialog();
         }
         if (dialog != null) {
             graphics().rootLayer().addAt(graphics().createImmediateLayer((int) dialog.width(), (int) dialog.height(), dialog), dialog.x(), dialog.y());
             pointer().setListener(dialog);
             keyboard().setListener(dialog);
             mouse().setListener(dialog);
             game.unregisterUpdatee(this);
 
         }
     }
 
     abstract class EndGameDialog implements Renderer, Pointer.Listener, Keyboard.Listener, Mouse.Listener {
 
         protected CanvasImage image;
 
         public float width() {
             return 500;
         }
 
         public final float x() {
             return (Tribes.SCREEN_WIDTH - width()) / 2;
         }
 
         public final float y() {
             return (Tribes.SCREEN_HEIGHT - height()) / 2;
         }
 
         public float height() {
             if (image == null) {
                 image = createImage();
             }
             return image.height();
         }
 
         public final void render(Surface surface) {
             if (image == null) {
                 image = createImage();
             }
             surface.drawImage(image, 0, 0);
         }
 
         @Override
         public void onPointerStart(Pointer.Event event) {
         }
 
         @Override
         public void onPointerEnd(Pointer.Event event) {
         }
 
         @Override
         public void onPointerDrag(Pointer.Event event) {
         }
 
         @Override
         public void onPointerCancel(Pointer.Event event) {
         }
 
         public void onKeyDown(Keyboard.Event event) {
         }
 
         public void onKeyTyped(Keyboard.TypedEvent event) {
         }
 
         @Override
         public void onKeyUp(Keyboard.Event event) {
         }
 
         @Override
         public void onMouseDown(Mouse.ButtonEvent event) {
         }
 
         @Override
         public void onMouseUp(Mouse.ButtonEvent event) {
         }
 
         @Override
         public void onMouseMove(Mouse.MotionEvent event) {
         }
 
         @Override
         public void onMouseWheelScroll(Mouse.WheelEvent event) {
         }
 
         public abstract CanvasImage createImage();
     }
 
     class StackLayout {
 
         List<LayoutObject> objects;
 
         public StackLayout() {
             objects = new ArrayList<LayoutObject>();
         }
 
         private float height() {
             float height = 0;
             for (LayoutObject object : objects) {
                 height += object.height();
             }
             return height;
         }
 
         private void draw(Canvas canvas) {
             float y = 0;
             for (LayoutObject object : objects) {
                 object.draw(canvas, (canvas.width() - object.width()) / 2, y);
                 y += object.height();
             }
         }
 
         private LayoutObject objectAt(float x, float y, float overallWidth) {
             float currY = 0;
             for (LayoutObject object : objects) {
                 float currX = (overallWidth - object.width()) / 2;
                 if (y > currY && y < currY + object.height() && x > currX && x < currX + object.width()) {
                     return object;
                 }
                 currY += object.height();
             }
             return null;
         }
     }
 
     abstract class LayoutObject {
 
         public static final float FRAME_PADDING = 5;
         public static final float PADDING_TOP = 10;
         public static final float PADDING_BOTTOM = 10;
         public static final float BUTTON_PADDING = 8;
         public static final float TEXT_PADDING = 7;
         public static final float INTERNAL_SPACING = 15;
         public static final float BETWEEN_BUTTON_SPACING = 5;
 
         abstract float width();
 
         abstract float height();
 
         public abstract void draw(Canvas canvas, float x, float y);
     }
 
     class Label extends LayoutObject {
 
         TextLayout labelLayout;
         int textColor;
 
         public Label(TextLayout layout, int textColor) {
             this.labelLayout = layout;
             this.textColor = textColor;
         }
 
         @Override
         float width() {
             return labelLayout.width();
         }
 
         @Override
         float height() {
             return labelLayout.height();
         }
 
         @Override
         public void draw(Canvas canvas, float x, float y) {
             canvas.setFillColor(textColor);
             canvas.fillText(labelLayout, x, y);
         }
 
         public String toString() {
             return labelLayout.toString();
         }
     }
 
     class Spacing extends LayoutObject {
 
         float width;
         float height;
 
         public Spacing(float width, float height) {
             this.width = width;
             this.height = height;
         }
 
         @Override
         float width() {
             return width;
         }
 
         @Override
         float height() {
             return height;
         }
 
         @Override
         public void draw(Canvas canvas, float x, float y) {
         }
 
         public String toString() {
             return width + "";
         }
     }
 
     class Button extends LayoutObject {
 
         int textColor;
         int frameColor;
         String text;
         int minWidth;
         TextLayout layout;
 
         public Button(String text, int textColor, float fontSize, int frameColor) {
             this(text, textColor, fontSize, frameColor, -1);
         }
 
         public Button(String text, int textColor, float fontSize, int frameColor, int minWidth) {
             this.textColor = textColor;
             this.frameColor = frameColor;
             this.text = text;
             this.minWidth = minWidth;
             Font buttonFont = graphics().createFont("Sans serif", Font.Style.PLAIN, fontSize);
             layout = graphics().layoutText(text, new TextFormat().withFont(buttonFont));
         }
 
         public float height() {
             return BUTTON_PADDING * 2 + layout.height();
         }
 
         public float width() {
             float width = BUTTON_PADDING * 2 + layout.width();
             if (width < minWidth) {
                 width = minWidth;
             }
             return width;
         }
 
         public void draw(Canvas canvas, float x, float y) {
             canvas.setFillColor(textColor);
             canvas.fillText(layout, x + (width() - layout.width()) / 2, y + BUTTON_PADDING);
             canvas.setStrokeColor(frameColor);
             canvas.setStrokeWidth(3);
             drawRect(canvas, x, y, width(), height());
         }
 
         protected final void drawRect(Canvas canvas, float x, float y, float width, float height) {
             canvas.drawLine(x, y, x + width, y);
             canvas.drawLine(x, y, x, y + height);
             canvas.drawLine(x + width, y, x + width, y + height);
             canvas.drawLine(x, y + height, x + width, y + height);
         }
 
         public String toString() {
             return text;
         }
     }
 
     class LoseDialog extends EndGameDialog {
 
         StackLayout layout;
 
         public CanvasImage createImage() {
             Font titleFont = graphics().createFont("Sans serif", Font.Style.BOLD, 36);
             TextLayout nameLayout = graphics().layoutText("You lose.", new TextFormat().withFont(titleFont));
 
             Font detailFont = graphics().createFont("Sans serif", Font.Style.PLAIN, 18);
             String text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. In adipiscing dui at dolor eleifend in convallis metus dictum. Donec at metus elit. Class aptent taciti sociosqu ad litora torquent per conubia nostra, per inceptos himenaeos.";
             TextLayout detailText = graphics().layoutText(text, new TextFormat().withFont(detailFont).withWrapWidth(width() - LayoutObject.FRAME_PADDING * 2 - LayoutObject.TEXT_PADDING * 2));
 
             layout = new StackLayout();
             layout.objects.add(new Spacing(LayoutObject.FRAME_PADDING, LayoutObject.FRAME_PADDING));
             layout.objects.add(new Spacing(LayoutObject.PADDING_TOP, LayoutObject.PADDING_TOP));
             layout.objects.add(new Label(nameLayout, Color.rgb(255, 255, 255)));
             layout.objects.add(new Spacing(LayoutObject.INTERNAL_SPACING, LayoutObject.INTERNAL_SPACING));
             layout.objects.add(new Label(detailText, Color.rgb(255, 255, 255)));
             layout.objects.add(new Spacing(LayoutObject.INTERNAL_SPACING, LayoutObject.INTERNAL_SPACING));
             layout.objects.add(new Button("Try again?", Color.rgb(255, 255, 255), 20, Color.rgb(200, 200, 200)));
             layout.objects.add(new Spacing(LayoutObject.PADDING_BOTTOM, LayoutObject.PADDING_BOTTOM));
             layout.objects.add(new Spacing(LayoutObject.FRAME_PADDING, LayoutObject.FRAME_PADDING));
 
 
             CanvasImage canvas = graphics().createImage(width(), layout.height());
             canvas.canvas().setFillColor(Color.rgb(255, 255, 255));
             canvas.canvas().fillRect(0, 0, width(), layout.height());
             canvas.canvas().setFillColor(Color.rgb(0, 0, 0));
             canvas.canvas().fillRect(LayoutObject.FRAME_PADDING, LayoutObject.FRAME_PADDING, width() - LayoutObject.FRAME_PADDING * 2, layout.height() - LayoutObject.FRAME_PADDING * 2);
             layout.draw(canvas.canvas());
 
             return canvas;
         }
 
         @Override
         public void onMouseMove(Mouse.MotionEvent event) {
             float x = event.x() - x();
             float y = event.y() - y();
             LayoutObject mousedObject = layout.objectAt(x, y, image.width());
         }
     }
 
     class WinDialog extends EndGameDialog {
 
         StackLayout layout;
 
         public CanvasImage createImage() {
             Font titleFont = graphics().createFont("Sans serif", Font.Style.BOLD, 36);
             TextLayout nameLayout = graphics().layoutText("You win.", new TextFormat().withFont(titleFont));
 
             Font detailFont = graphics().createFont("Sans serif", Font.Style.PLAIN, 18);
             String text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. In adipiscing dui at dolor eleifend in convallis metus dictum. Donec at metus elit. Class aptent taciti sociosqu ad litora torquent per conubia nostra, per inceptos himenaeos.";
             TextLayout detailText = graphics().layoutText(text, new TextFormat().withFont(detailFont).withWrapWidth(width() - LayoutObject.FRAME_PADDING * 2 - LayoutObject.TEXT_PADDING * 2));
 
             layout = new StackLayout();
             layout.objects.add(new Spacing(LayoutObject.FRAME_PADDING, LayoutObject.FRAME_PADDING));
             layout.objects.add(new Spacing(LayoutObject.PADDING_TOP, LayoutObject.PADDING_TOP));
             layout.objects.add(new Label(nameLayout, Color.rgb(0, 0, 0)));
             layout.objects.add(new Spacing(LayoutObject.INTERNAL_SPACING, LayoutObject.INTERNAL_SPACING));
             layout.objects.add(new Label(detailText, Color.rgb(0, 0, 0)));
             layout.objects.add(new Spacing(LayoutObject.INTERNAL_SPACING, LayoutObject.INTERNAL_SPACING));
             layout.objects.add(new Button("Continue with this world?", Color.rgb(0, 0, 0), 20, Color.rgb(50, 50, 50), 350));
             layout.objects.add(new Spacing(LayoutObject.BETWEEN_BUTTON_SPACING, LayoutObject.BETWEEN_BUTTON_SPACING));
             layout.objects.add(new Button("Start world from scratch?", Color.rgb(0, 0, 0), 20, Color.rgb(50, 50, 50), 350));
             layout.objects.add(new Spacing(LayoutObject.BETWEEN_BUTTON_SPACING, LayoutObject.BETWEEN_BUTTON_SPACING));
             layout.objects.add(new Button("Start world from sample?", Color.rgb(0, 0, 0), 20, Color.rgb(50, 50, 50), 350));
             layout.objects.add(new Spacing(LayoutObject.BETWEEN_BUTTON_SPACING, LayoutObject.BETWEEN_BUTTON_SPACING));
             layout.objects.add(new Spacing(LayoutObject.PADDING_BOTTOM, LayoutObject.PADDING_BOTTOM));
             layout.objects.add(new Spacing(LayoutObject.FRAME_PADDING, LayoutObject.FRAME_PADDING));
 
             CanvasImage canvas = graphics().createImage(width(), layout.height());
             canvas.canvas().setFillColor(Color.rgb(0, 0, 0));
             canvas.canvas().fillRect(0, 0, width(), layout.height());
             canvas.canvas().setFillColor(Color.rgb(255, 255, 255));
             canvas.canvas().fillRect(LayoutObject.FRAME_PADDING, LayoutObject.FRAME_PADDING, width() - LayoutObject.FRAME_PADDING * 2, layout.height() - LayoutObject.FRAME_PADDING * 2);
             layout.draw(canvas.canvas());
 
             return canvas;
         }
     }
 }
