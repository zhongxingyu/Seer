 package hyde.megakill.shapes;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Input;
 import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
 import hyde.megakill.core.Font;
 import hyde.megakill.core.GlobalValues;
 import hyde.megakill.input.Event;
 import hyde.megakill.input.KeyboardAndMouse;
 import hyde.megakill.util.Random;
 
 import java.util.ArrayList;
 
 public class PlayerShape extends Shape {
     private float rotateSpeed = 1.5f;
 
     protected double speedIncrement = 0.05;
 
     private boolean isRotateingLeft = false,
                     isRotateingRight = false,
                     isMovingForward = false,
                     isMovingBackwards = false,
                     isShooting = false;
 
     private long weaponHeat = 0;
 
     private ArrayList<Missile> missiles = new ArrayList<Missile>();
     private int maxWeaponHeat = 50;
 
     public PlayerShape() {
         pos.x = GlobalValues.screenWidth / 2;
         pos.y = GlobalValues.screenHeight / 2;
 
         KeyboardAndMouse keyboardAndMouse = ((KeyboardAndMouse) Gdx.input.getInputProcessor());
         keyboardAndMouse.addEvent(Input.Keys.LEFT, new Event() {
             @Override
             public void action(boolean keyDown) {
                 isRotateingLeft = keyDown;
             }
         });
         keyboardAndMouse.addEvent(Input.Keys.RIGHT, new Event() {
             @Override
             public void action(boolean keyDown) {
                 isRotateingRight = keyDown;
             }
         });
         keyboardAndMouse.addEvent(Input.Keys.UP, new Event() {
             @Override
             public void action(boolean keyDown) {
                 isMovingForward = keyDown;
             }
         });
         keyboardAndMouse.addEvent(Input.Keys.DOWN, new Event() {
             @Override
             public void action(boolean keyDown) {
                 isMovingBackwards = keyDown;
             }
         });
         keyboardAndMouse.addEvent(Input.Keys.SPACE, new Event() {
             @Override
             public void action(boolean keyDown) {
                 shoot();
             }
         });
         angle = Random.getRandomInteger(360);
         size = 25.0f;
     }
 
     private void moveBackwards() {
         if (speed > 0) {
            speed-=speedIncrement * Gdx.graphics.getDeltaTime();
             calc(false);
         }
     }
 
     private void moveForward() {
        speed+=speedIncrement  * Gdx.graphics.getDeltaTime();
         calc(true);
     }
 
     public void rotateLeft() {
        angle -= rotateSpeed  * Gdx.graphics.getDeltaTime();
     }
     public void rotateRight() {
        angle += rotateSpeed  * Gdx.graphics.getDeltaTime();
     }
     public void shoot() {
         if ( weaponHeat <= 0 ) {
             missiles.add(new Missile(pos.x, pos.y, angle, speed));
             weaponHeat += maxWeaponHeat;
         }
     }
 
     @Override
     public String toString() {
         return "PlayerShape{" +
                 "size=" + size +
                 ", rotateSpeed=" + rotateSpeed +
                 ", speedIncrement=" + speedIncrement +
                 ", isRotateingLeft=" + isRotateingLeft +
                 ", isRotateingRight=" + isRotateingRight +
                 ", isMovingForward=" + isMovingForward +
                 ", isMovingBackwards=" + isMovingBackwards +
                 ", isShooting=" + isShooting +
                 '}';
     }
 
     @Override
     public void render(ShapeRenderer shapeRenderer) {
         ifOnEdgeMoveToOppositeEdge();
         moveAndRotateShip();
 
         pos.x += xForwardMomentum;
         pos.y += yForwardMomentum;
 
         shapeRenderer.identity();
         shapeRenderer.translate(pos.x, pos.y, 0);
         shapeRenderer.rotate(0, 0, 1, angle+270);
 
         shapeRenderer.translate(-(size / 2),-(size / 2), 0);
         shapeRenderer.begin(ShapeRenderer.ShapeType.Triangle);
         shapeRenderer.setColor(1, 1, 1, 1);
         shapeRenderer.triangle(0,0, size,0, size /2, size);
         shapeRenderer.end();
 
         for (Missile missile : missiles)
             missile.render(shapeRenderer);
 
         if ( weaponHeat > 0)
             weaponHeat -= Gdx.graphics.getDeltaTime();
 
         Font.drawText("WeaponHeat: " + weaponHeat, 400, 20);
     }
 
     private void moveAndRotateShip() {
         if (isRotateingLeft)
             rotateLeft();
         if (isRotateingRight)
             rotateRight();
         if (isMovingBackwards)
             moveBackwards();
         if (isMovingForward)
             moveForward();
     }
 
     public ArrayList<Missile> getMissiles() {
         return missiles;
     }
 
 }
