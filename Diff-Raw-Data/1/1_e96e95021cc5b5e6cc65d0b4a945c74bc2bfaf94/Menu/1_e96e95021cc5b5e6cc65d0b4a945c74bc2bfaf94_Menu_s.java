 package net.stuffrepos.tactics16.components.menu;
 
 import org.newdawn.slick.Color;
 import net.stuffrepos.tactics16.MyGame;
 import net.stuffrepos.tactics16.game.Coordinate;
 import net.stuffrepos.tactics16.util.cursors.ObjectCursor1D;
 import org.newdawn.slick.Graphics;
 import java.util.Collections;
 import net.stuffrepos.tactics16.GameKey;
 import net.stuffrepos.tactics16.components.Button;
 import net.stuffrepos.tactics16.components.Object2D;
 import net.stuffrepos.tactics16.util.cache.CacheableValue;
 import net.stuffrepos.tactics16.util.image.ColorUtil;
 import net.stuffrepos.tactics16.util.image.DrawerUtil;
 import net.stuffrepos.tactics16.util.javabasic.MathUtil;
 import net.stuffrepos.tactics16.util.listeners.Listener;
 import net.stuffrepos.tactics16.util.listeners.ListenerManager;
 import org.newdawn.slick.Font;
 
 /**
  *
  * @author Eduardo H. Bogoni <eduardobogoni@gmail.com>
  */
 public class Menu implements Object2D {
 
     private final static int TEXT_MARGIN = 5;
     private ObjectCursor1D<MenuOption> cursor = new ObjectCursor1D<MenuOption>();
     private Coordinate position = new Coordinate();
     private long elapsedTime = 0;
     private CacheableValue<Integer> width = new CacheableValue<Integer>() {
 
         @Override
         protected Integer calculate() {
             return getOptionWidth();
         }
     };
     private CacheableValue<Integer> height = new CacheableValue<Integer>() {
 
         @Override
         protected Integer calculate() {
             return cursor.getList().size() * (getOptionHeight() + Button.BUTTON_GAP);
         }
     };
     private ListenerManager<Object2D> listenerManager = new ListenerManager<Object2D>(this);
 
     public Menu(MenuOption... options) {
         cursor.getCursor().setKeys(GameKey.UP, GameKey.DOWN);
         Collections.addAll(this.cursor.getList(), options);
 
         width.addListener(new Listener<CacheableValue<Integer>>() {
 
             public void onChange(CacheableValue<Integer> source) {
                 listenerManager.fireChange();
             }
         });
 
         height.addListener(new Listener<CacheableValue<Integer>>() {
 
             public void onChange(CacheableValue<Integer> source) {
                 listenerManager.fireChange();
             }
         });
 
         position.addListener(new Listener<Coordinate>() {
 
             public void onChange(Coordinate source) {
                 listenerManager.fireChange();
             }
         });
     }
 
     public ObjectCursor1D getCursor() {
         return cursor;
     }
 
     public void render(Graphics g) {
         int h = this.getOptionHeight();
         int i = 0;
         for (MenuOption option : this.cursor.getList()) {
             renderOption(
                     option,
                     g,
                     cursor.getCursor().getCurrent() == i,
                     position.getX(),
                     position.getY() + i * (h + Button.BUTTON_GAP),
                     getWidth(), h);
             i++;
         }
     }
 
     public void update(long elapsedTime) {
         this.elapsedTime += elapsedTime;
         cursor.update(elapsedTime);
         while (!cursor.getSelected().isEnabled()) {
             if (cursor.getCursor().getLastMove() >= 0) {
                 cursor.getCursor().moveNext();
             } else {
                 cursor.getCursor().movePrevious();
             }
         }
 
         if (MyGame.getInstance().isKeyPressed(GameKey.CONFIRM)) {
             this.cursor.getSelected().executeAction();
         }
 
         for (MenuOption option : this.cursor.getList()) {
             if (option.getKey() != null) {
                 if (MyGame.getInstance().isKeyPressed(option.getKey())) {
                     option.executeAction();
                 }
             }
         }
     }
 
     public void addOption(MenuOption option) {
         this.cursor.getList().add(option);
         width.clear();
         height.clear();
     }
 
     protected void onChangeSelectedOption() {
     }
 
     public Coordinate getPosition() {
         return position;
     }
 
     private int getOptionWidth() {
         int w = 0;
 
         for (MenuOption option : this.cursor.getList()) {
             int optionW = getFont().getWidth(option.getText());
 
             if (optionW > w) {
                 w = optionW;
             }
         }
 
         return w + 2 * TEXT_MARGIN;
     }
 
     private int getOptionHeight() {
         return getFont().getLineHeight() + 2 * TEXT_MARGIN;
     }
 
     public void clear() {
         this.cursor.clear();
         this.height.clear();
         this.width.clear();
     }
 
     public int getTop() {
         return this.position.getY();
     }
 
     public int getLeft() {
         return this.position.getX();
     }
 
     public int getWidth() {
         return width.getValue();
     }
 
     public int getHeight() {
         return height.getValue();
     }
 
     private void renderOption(MenuOption option, Graphics g, boolean selected, int x, int y, int w, int h) {
         int optionW = getFont().getWidth(option.getText());
         Color backgroundColor;
         if (option.isEnabled()) {
             backgroundColor = selected
                     ? Colors.getSelectedBackgroundColor(elapsedTime)
                     : Colors.getEnabledBackgroundColor();
         } else {
             backgroundColor = Colors.getDisabledBackgroundColor();
         }
 
         g.setColor(backgroundColor);
         DrawerUtil.fill3dRect(g, x, y, w, h - 1, true);        
 
         if (option.isEnabled()) {
             g.setColor(Colors.getEnabledForegroundColor());
         } else {
             g.setColor(Colors.getDisabledForegroundColor());
         }
 
         g.drawString(
                 option.getText(),
                 x + (w - optionW) / 2,
                 y + TEXT_MARGIN);
     }
 
     public void addGeometryListener(Listener<Object2D> listener) {
         listenerManager.addListener(listener);
     }
 
     private Font getFont() {
         return MyGame.getInstance().getFont();
     }
 
     // <editor-fold defaultstate="collapsed" desc="class Colors">
     private static class Colors {
 
         private static final Color ENABLED_FOREGROUND_COLOR = Color.white;
         private static final Color ENABLED_BACKGROUND_COLOR = new Color(0x000077);
         private static final Color DISABLED_FOREGROUND_COLOR = Color.gray;
         private static final Color DISABLED_BACKGROUND_COLOR = Color.darkGray;
         private static final Color SELECTED_BACKGROUND_COLOR_BEGIN = new Color(0x0000AA);
         private static final Color SELECTED_BACKGROUND_COLOR_END = new Color(0x0000FF);
         private static final Color[] SELECTED_BACKGROUND_COLORS;
         private static final int SELECTED_BACKGROUND_COLORS_COUNT = 8;
         private static final int CHANGE_FRAME_INTERVAL = 100;
 
         static {
             SELECTED_BACKGROUND_COLORS = new Color[SELECTED_BACKGROUND_COLORS_COUNT];
 
             for (int i = 0; i < SELECTED_BACKGROUND_COLORS.length - 1; ++i) {
                 SELECTED_BACKGROUND_COLORS[i] = ColorUtil.getBetweenColor(
                         SELECTED_BACKGROUND_COLOR_BEGIN, SELECTED_BACKGROUND_COLOR_END,
                         (float) i / (SELECTED_BACKGROUND_COLORS.length - 1));
             }
 
             SELECTED_BACKGROUND_COLORS[SELECTED_BACKGROUND_COLORS.length - 1] = SELECTED_BACKGROUND_COLOR_END;
         }
 
         public static Color getEnabledForegroundColor() {
             return ENABLED_FOREGROUND_COLOR;
         }
 
         public static Color getEnabledBackgroundColor() {
             return ENABLED_BACKGROUND_COLOR;
         }
 
         public static Color getDisabledForegroundColor() {
             return DISABLED_FOREGROUND_COLOR;
         }
 
         public static Color getDisabledBackgroundColor() {
             return DISABLED_BACKGROUND_COLOR;
         }
 
         public static Color getSelectedBackgroundColor(long elapsedTime) {
             return SELECTED_BACKGROUND_COLORS[(int) MathUtil.getLoopCurrentIndex(
                     SELECTED_BACKGROUND_COLORS.length,
                     elapsedTime,
                     CHANGE_FRAME_INTERVAL)];
         }
     }// </editor-fold>
 }
