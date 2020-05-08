 package pt.up.fe.pt.lpoo.bombermen;
 
 import pt.up.fe.pt.lpoo.utils.Direction;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.math.Vector3;
 import com.badlogic.gdx.utils.Disposable;
 
 public class ControlPad implements Disposable
 {
     public static final int PLACE_BOMB = 4;
     private Texture _padTexture;
     private Texture _bombButtonTexture;
     private Sprite _padSprite;
     private Sprite _bombButtonSprite;
 
     public ControlPad()
     {
         _padTexture = new Texture(Gdx.files.internal("data/dpad.png"));
         _padSprite = new Sprite(_padTexture);
         _padSprite.setOrigin(0, 0);
         _padSprite.setPosition(0, 0);
 
         _bombButtonTexture = new Texture(Gdx.files.internal("data/bombButton.png"));
         _bombButtonSprite = new Sprite(_bombButtonTexture);
         _bombButtonSprite.setOrigin(_bombButtonTexture.getWidth(), 0);
         _bombButtonSprite.setPosition(Gdx.graphics.getWidth() - _bombButtonTexture.getWidth(), 0);
     }
 
     public float GetWidth()
     {
         return _padSprite.getWidth();
     }
 
     public float GetHeight()
     {
         return _padSprite.getHeight();
     }
 
     public void SetSize(float width, float height)
     {
         _padSprite.setSize(width, height);
         _bombButtonSprite.setSize(width * Constants.DEFAULT_PAD_BUTTON_SIZE_MULT, height * Constants.DEFAULT_PAD_BUTTON_SIZE_MULT);
         _bombButtonSprite.setPosition(Gdx.graphics.getWidth() - _bombButtonSprite.getWidth(), 0);
     }
 
     public void ChangeSize(float dW, float dH)
     {
         SetSize(_padSprite.getWidth() + dW, _padSprite.getHeight() + dH);
     }
 
     public int Click(Vector3 pos)
     {
         if (_padSprite.getBoundingRectangle().contains(pos.x, pos.y))
         {
             float x1 = pos.x - _padSprite.getX();
             float y1 = pos.y - _padSprite.getY();
 
             float y2 = _padSprite.getHeight() / 3;
             float x2 = _padSprite.getWidth() / 3;
 
             if (x1 > x2 && x1 < (2 * x2))
             {
                 if (y1 < y2)
                    return Direction.SOUTH;
                else if (y1 > 2 * y2)
                    return Direction.NORTH;
             }
            else if (y1 > y2 && y1 < (2 * y2))
                return x1 > x2 ? Direction.EAST : Direction.WEST;
         }
         else if (_bombButtonSprite.getBoundingRectangle().contains(pos.x, pos.y))
         {
             return PLACE_BOMB;
         }
 
         return Direction.NONE;
     }
 
     public void draw(SpriteBatch spriteBatch)
     {
        _padSprite.draw(spriteBatch);
        _bombButtonSprite.draw(spriteBatch);
     }
 
     @Override
     public void dispose()
     {
        _padTexture.dispose();
        _bombButtonTexture.dispose();
     }
 }
