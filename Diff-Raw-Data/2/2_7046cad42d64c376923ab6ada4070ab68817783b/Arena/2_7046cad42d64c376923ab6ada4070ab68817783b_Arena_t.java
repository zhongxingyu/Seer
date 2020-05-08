 /*
  * Copyright (c) 2013 George Weller
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 
 package uk.co.zutty.glarena;
 
 import net.java.games.input.Controller;
 import net.java.games.input.ControllerEnvironment;
 import org.lwjgl.BufferUtils;
 import org.lwjgl.opengl.Util;
 import org.lwjgl.util.vector.Vector3f;
 import uk.co.zutty.glarena.gl.ElementArrayModel;
 import uk.co.zutty.glarena.gl.Model;
 
 import java.nio.FloatBuffer;
 import java.nio.ShortBuffer;
 
 /**
  * Concrete game class for the glArena game.
  */
 public class Arena extends Game {
 
     public static final Vector3f V = new Vector3f();
 
     private Gunship player;
     private Model ufoModel;
 
     private Gamepad gamepad;
 
     private Emitter playerBulletEmitter;
 
     private Vector3f arenaCentre;
 
     private int spawnTimer = 0;
     private int waveTimer = 0;
     private int waveSpawn = 0;
 
     @Override
     protected void init() {
         camera.setPosition(0f, 20f, -25f);
 
         for (Controller controller : ControllerEnvironment.getDefaultEnvironment().getControllers()) {
             if (controller.getType() == Controller.Type.GAMEPAD) {
                 gamepad = new Gamepad(controller);
             }
         }
         if (gamepad == null) {
             gamepad = new KeyboardGamepad();
         }
 
         Technique entityTechnique = new EntityTechnique();
         Model gunshipModel = createModel(entityTechnique, "/models/gunship.obj");
         ufoModel = createModel(entityTechnique, "/models/ufo.obj");
         Model ringModel = createModel(entityTechnique, "/models/circle.obj");
 
         playerBulletEmitter = new Emitter(new BulletTechnique(), TextureLoader.loadTexture("/textures/shot.png"), BulletParticle.class);
         add(playerBulletEmitter);
 
         Emitter explosionEmitter = new Emitter(new BillboardTechnique(), TextureLoader.loadTexture("/textures/cross.png"), BillboardParticle.class);
         add(explosionEmitter);
 
         player = new Gunship(new ModelInstance(gunshipModel, TextureLoader.loadTexture("/textures/gunship_diffuse.png")));
         player.setPosition(4.5f, 0, -1);
         player.setBulletEmitter(playerBulletEmitter);
         player.setGamepad(gamepad);
         add(player);
 
         arenaCentre = new Vector3f(0, 0, 0);
 
         Marker ringMarker = new Marker(new ModelInstance(ringModel, TextureLoader.loadTexture("/textures/circle.png")));
         ringMarker.position.y = -1;
         add(ringMarker);
 
         final double DEG_TO_RAD = Math.PI / 180.0;
 
         for (int i = 0; i < 360; i += 10) {
             float x = (float) Math.sin(i * DEG_TO_RAD) * 10f;
             float z = (float) Math.cos(i * DEG_TO_RAD) * 10f;
 
             explosionEmitter.emitFrom(new Vector3f(x, 0, z), new Vector3f(1, 0, 0), 0f);
         }
         explosionEmitter.update();
 
         Util.checkGLError();
     }
 
     private Model createModel(Technique technique, String meshFile) {
         Mesh mesh = new ObjLoader().loadMesh(meshFile);
 
         ElementArrayModel model = new ElementArrayModel(technique);
 
        FloatBuffer vertexData = BufferUtils.createFloatBuffer(mesh.getVertices().size() * technique.getFormat().getElements());
 
         for (Vertex vertex : mesh.getVertices()) {
             vertex.put(vertexData);
         }
 
         vertexData.flip();
         model.setVertexData(vertexData, mesh.getVertices().size());
 
         ShortBuffer indexData = BufferUtils.createShortBuffer(mesh.getIndices().size());
 
         for (short index : mesh.getIndices()) {
             indexData.put(index);
         }
 
         indexData.flip();
         model.setIndexData(indexData);
 
         return model;
     }
 
     public void spawnUfo() {
         Ufo ufo = new Ufo(new ModelInstance(ufoModel, TextureLoader.loadTexture("/textures/ufo.png")), playerBulletEmitter);
         ufo.setGame(this);
         ufo.setPosition(-4.5f, 0, -1);
         add(ufo);
     }
 
     @Override
     protected void update() {
         if (gamepad != null) {
             gamepad.update();
         }
 
         if (++waveTimer > 200) {
             waveSpawn = 0;
             waveTimer = 0;
         }
 
         if (++spawnTimer > 15 && waveSpawn < 6) {
             spawnTimer = 0;
             ++waveSpawn;
             //spawnUfo();
         }
 
         super.update();
 
         Vector3f.sub(arenaCentre, player.position, V);
         V.scale(0.9f);
 
         camera.setPosition(arenaCentre.x - V.x, 20f, arenaCentre.z - 25f - V.z);
         camera.setCenter(arenaCentre.x - V.x, 0f, arenaCentre.z - V.z);
         camera.update();
     }
 
     public static void main(String... args) {
         new Arena();
     }
 }
