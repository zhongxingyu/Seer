 package cubetech.client;
 
 import cubetech.gfx.Sprite;
 import cubetech.gfx.SpriteManager.Type;
 import cubetech.gfx.TextManager.Align;
 import cubetech.input.Key;
 import cubetech.input.KeyEvent;
 import cubetech.input.KeyEventListener;
 import cubetech.misc.Ref;
 import org.lwjgl.input.Keyboard;
 import org.lwjgl.util.vector.Vector2f;
 
 /**
  *
  * @author mads
  */
 public class MessageField implements KeyEventListener {
     private static final int MAX_CHAR_SIZE = 256;
     public int widthInChars = MAX_CHAR_SIZE;
     public StringBuilder buffer = new StringBuilder(MAX_CHAR_SIZE);
     public Vector2f position = new Vector2f(10, 200);
 
     public MessageField(Vector2f position) {
         if(position != null)
             this.position = position;
     }
 
     public MessageField(Vector2f position, String text) {
         if(position != null)
             this.position = position;
         buffer.append(text);
     }
 
     public void Clear() {
         buffer = new StringBuilder(MAX_CHAR_SIZE);
     }
 
     private boolean isActive() {
         return (Ref.client.message.getCurrentMessage() == this);
     }
 
     public void Render() {
         String extraHax = "";
         if(this == Ref.client.message.chatField)
             extraHax = "say: "; // hot-wire chat capability
         boolean cardi = isActive() && (int)(Ref.client.realtime / 250f)%2 == 1;
        String str = extraHax + buffer.toString() + (cardi?"_":" ");
         float bgwidth = 300;
         Vector2f maxSize = Ref.glRef.GetResolution();
        maxSize = new Vector2f(maxSize.x -  position.x - 50, maxSize.y);
         Vector2f size = Ref.textMan.GetStringSize(str, maxSize, null,1, Type.HUD);
         bgwidth = size.x ;
         float bgheight = size.y ;
         Sprite spr = Ref.SpriteMan.GetSprite(Type.HUD);
        spr.Set(new Vector2f(position.x,  (position.y -size.y)), new Vector2f(bgwidth, bgheight), null, null, null);
         spr.SetColor(0, 0, 0, 80);
        Ref.textMan.AddText(new Vector2f(position.x, Ref.glRef.GetResolution().y - position.y ), str, Align.LEFT, null, maxSize, Type.GAME.HUD, 1);
     }
 
     public void KeyPressed(KeyEvent evt) {
         Key key = (Key)evt.getSource();
 
         if(!key.Pressed)
             return;
 
         if(key.key == Keyboard.KEY_BACK) {
             if(buffer.length() > 0)
                 buffer.deleteCharAt(buffer.length()-1);
         }
         else if(key.Char != Keyboard.CHAR_NONE && buffer.length() < widthInChars) {
             buffer.append(key.Char);
         }
     }
 
 
 }
