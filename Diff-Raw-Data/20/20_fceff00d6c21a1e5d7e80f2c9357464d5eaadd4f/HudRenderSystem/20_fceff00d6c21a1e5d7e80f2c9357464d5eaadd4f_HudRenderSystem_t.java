 package com.github.slamm.morrigna.core.hud;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.InputMultiplexer;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.graphics.g2d.BitmapFont;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.reed.birdseye.Time;
 
 public class HudRenderSystem {
 
     private final SpriteBatch batch;
 
     private final OrthographicCamera camera;
 
     private final BitmapFont currentFont;
 
     private final HouseFurnaceRenderer houseFurnaceRenderer;
 
     private final InventoryRenderer inventoryRenderer;
 
     private final LevelRenderer levelRenderer;
 
     private final MessagesRenderer messagesRenderer;
 
     private final PointsRenderer pointsRenderer;
 
     private final SwordShopRenderer swordShopRenderer;
 
     private final ToolsListRenderer toolsListRenderer;
 
     private final TopMenuRenderer topMenuRenderer;
 
     private final TradeShopRenderer tradeShopRenderer;
 
     public HudRenderSystem(SpriteBatch batch, OrthographicCamera camera, BitmapFont currentFont) {
         this.batch = batch;
         this.camera = camera;
         this.currentFont = currentFont;
         pointsRenderer = new PointsRenderer();
         messagesRenderer = new MessagesRenderer();
         inventoryRenderer = new InventoryRenderer();
         swordShopRenderer = new SwordShopRenderer();
         tradeShopRenderer = new TradeShopRenderer();
         houseFurnaceRenderer = new HouseFurnaceRenderer();
         toolsListRenderer = new ToolsListRenderer();
         topMenuRenderer = new TopMenuRenderer();
         levelRenderer = new LevelRenderer();
         InputMultiplexer multiplexer;
         if (Gdx.input.getInputProcessor() instanceof InputMultiplexer) {
             multiplexer = (InputMultiplexer) Gdx.input.getInputProcessor();
         } else {
             multiplexer = new InputMultiplexer(Gdx.input.getInputProcessor());
         }
         multiplexer.addProcessor(topMenuRenderer);
         multiplexer.addProcessor(inventoryRenderer);
         multiplexer.addProcessor(swordShopRenderer);
         multiplexer.addProcessor(houseFurnaceRenderer);
         multiplexer.addProcessor(tradeShopRenderer);
         Gdx.input.setInputProcessor(multiplexer);
     }
 
     public void render(float delta) {
         batch.begin();
         batch.setProjectionMatrix(camera.combined);
         toolsListRenderer.render(batch);
         topMenuRenderer.render(batch);
         pointsRenderer.render(camera, batch);
         messagesRenderer.render(currentFont, batch);
         inventoryRenderer.render(currentFont, batch);
         swordShopRenderer.render(currentFont, batch);
         tradeShopRenderer.render(currentFont, batch);
         houseFurnaceRenderer.render(delta, currentFont, batch);
         batch.end();
     }
 
    public void renderLevel() {
        batch.begin();
        batch.setProjectionMatrix(camera.combined);
        if (Time.isOutdoors()) {
            levelRenderer.draw(batch);
        }
        batch.end();
    }

     public void update() {
         levelRenderer.update();
         messagesRenderer.update();
         swordShopRenderer.update();
         houseFurnaceRenderer.update();
         tradeShopRenderer.update();
         pointsRenderer.update();
     }
 }
