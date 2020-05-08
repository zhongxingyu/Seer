 package eu32k.ludumdare.ld26.rendering;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.g2d.BitmapFont;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.math.Vector2;
 
 
 public class TextRenderer {
    
    private SpriteBatch textBatch;
    
    private BitmapFont font;
    
    private Map<String, Vector2> textPositions;
    private Map<String, String> textLines;
    
    public TextRenderer() {
       textBatch = new SpriteBatch();
       textLines = new HashMap<String, String>();
       textPositions = new HashMap<String, Vector2>();
      font = new BitmapFont(Gdx.files.internal("fonts/calibri.fnt"), Gdx.files.internal("fonts/calibri.png"), false);
    }
    
    public void render() {
       textBatch.begin();
       Set<String> keys = textLines.keySet();
       for(String text : keys) {
          Vector2 pos = textPositions.get(text);
          font.draw(textBatch, textLines.get(text), pos.x, pos.y);
       }
       textBatch.end();
    }
 
    public Map<String, String> getTextLines() {
       return textLines;
    }
 
    public Map<String, Vector2> getTextPositions() {
       return textPositions;
    }
    
    public void addText(String textKey, String text, float x, float y) {
       this.textLines.put(textKey, text);
       this.textPositions.put(textKey, new Vector2(x, y));
    }
 
 }
