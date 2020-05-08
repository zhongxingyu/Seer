 package com.me.battleship;
 
 import com.badlogic.gdx.ApplicationListener;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.InputProcessor;
 import com.badlogic.gdx.graphics.GL10;
 
 import java.io.IOException;
 import java.util.Properties;
 
 public class Battleship implements ApplicationListener, InputProcessor {
     private Torpedo t;// test
 
     private Board player1, player2, setup;
 
     private Ship selectedShip;
     private float timeDragged;
     private boolean rotated;
     private Drawer drawer;
     private Properties props = new Properties();
     private Button rotateRegion;
 
     @Override
     public void create() {
         try {
             props.load(Gdx.files.internal("data/config.properties").read());
         } catch (IOException e) {
             e.printStackTrace();
             System.exit(10);
         }
 
         timeDragged = 0f;
         rotated = false;
         t = new Torpedo(50, 50, Globals.AttackStatus.HIT);
         player1 = new Board( Integer.valueOf(props.getProperty("grid.loc.x")), Integer.valueOf(props.getProperty("grid.loc.y")),
                 Integer.valueOf(props.getProperty("grid.dimensions.x")), Integer.valueOf(props.getProperty("grid.dimensions.y")),
                 "player1", Integer.parseInt(props.getProperty("grid.size")));
         rotateRegion = new Button(Integer.valueOf(props.getProperty("rotate.zone.loc.x")), Integer.valueOf(props.getProperty("rotate.zone.loc.y")),
                 Integer.valueOf(props.getProperty("rotate.zone.size.x")), Integer.valueOf(props.getProperty("rotate.zone.size.y")));
         drawer = new Drawer(player1, rotateRegion);
         //scaled to 80% of tilesize
         createShips(player1, drawer.getTileSize() * 4 / 5);
         Gdx.input.setInputProcessor(this);
     }
 
     @Override
     public void dispose() {
         drawer.dispose();
     }
 
     @Override
     public void render() {
         Gdx.gl.glClearColor(1, 1, 1, 1);
         Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
         drawer.drawSetup();
     }
 
     @Override
     public void resize(int width, int height) {
     }
 
     @Override
     public void pause() {
     }
 
     @Override
     public void resume() {
     }
 
     @Override
     public boolean keyDown(int keycode) {
         return false;
     }
 
     @Override
     public boolean keyUp(int keycode) {
         return false;
     }
 
     @Override
     public boolean keyTyped(char character) {
         return false;
     }
 
     @Override
     public boolean touchDown(int x, int y, int pointer, int button) {
         return false;
     }
 
     @Override
     public boolean touchUp(int x, int y, int pointer, int button) {
 
         if (selectedShip != null && timeDragged > .1f && Globals.isInside(selectedShip, player1)) {
             player1.centerShipOnSquare(selectedShip);
             player1.placeShipOnGrid(selectedShip);
             player1.deselectSquares();
         } else if (selectedShip != null) {
            player1.removeShipOnGrid(selectedShip);
             selectedShip.reset();
             player1.deselectSquares();
         } else {
             for (int i = 0; i < player1.ships.size(); i++)
                 if (touchedShip(player1.ships.get(i), x, y)) {
                     player1.ships.get(i).reset();
                     return true;
                 }
         }
         selectedShip = null;
         timeDragged = 0f;
         return true;
     }
 
     @Override
     public boolean touchDragged(int x, int y, int pointer) {
         if (selectedShip == null) {
             for (Ship ship : player1.getShips()) {
                 if (touchedShip(ship, x, y)) {
                     selectedShip = ship;
                     return true;
                 }
             }
         } else {
             timeDragged += Gdx.graphics.getDeltaTime();
             selectedShip.move(x, y);
             if (!rotated && Globals.isInside(x, y, rotateRegion)) {
                 rotated = true;
                 selectedShip.changeOrientation();
             } else if (rotated && !Globals.isInside(x, y, rotateRegion)) {
                 rotated = false;
             }
             player1.highlightSquares(x, y, selectedShip);
         }
         return true;
     }
 
     @Override
     public boolean scrolled(int amount) {
         return false;
     }
 
     @Override
     public boolean mouseMoved(int screenX, int screenY) {
         return false;
     }
 
     boolean touchedShip(Ship s, int x, int y) {
         return Globals.isInside(x, y, s);
     }
 
     private void createShips(Board player, int unitDimension) {
         String[] shipSizes = props.getProperty("grid.ships").split(",");
         for (int i = 0; i < shipSizes.length; i++) {
             player.addShip(new Ship(Integer.valueOf(props.getProperty("ship.zone.loc.x")),
                     i * unitDimension + Integer.valueOf(props.getProperty("ship.zone.loc.y")),
                     Ship.ShipClass.valueOf(shipSizes[i].trim()), Ship.Orientation.HORIZONTAL, unitDimension));
         }
     }
 
 }
