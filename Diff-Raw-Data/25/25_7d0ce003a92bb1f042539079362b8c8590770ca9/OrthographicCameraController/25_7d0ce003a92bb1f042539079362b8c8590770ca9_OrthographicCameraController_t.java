 package com.someclock.java;
 
 import com.badlogic.gdx.ApplicationListener;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Input;
 import com.badlogic.gdx.files.FileHandle;
 import com.badlogic.gdx.graphics.*;
 import com.badlogic.gdx.graphics.g3d.loaders.obj.ObjLoader;
 import com.badlogic.gdx.math.Rectangle;
 import static java.lang.Math.*;
 /**
  * Created with IntelliJ IDEA.
  * User: ko3a4ok
  * Date: 7/14/12
  * Time: 1:50 PM
  * To change this template use File | Settings | File Templates.
  */
 public class OrthographicCameraController implements ApplicationListener, PositionDataInterface {
 
     static final int WIDTH  = 50;
     static final int HEIGHT = 50;
 
     private OrthographicCamera camera;
     private Texture texture;
     private Mesh mesh;
     private Rectangle glViewport;
     private float                           rotationSpeed;
 
     Mesh model;
 
 
     @Override
     public void create() {
         rotationSpeed = 0.5f;
         mesh = new Mesh(true, 4, 6,
                 new VertexAttribute(VertexAttributes.Usage.Position, 3,"attr_Position"),
                 new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, "attr_texCoords"));
 
         System.err.println("LOCAL path: " + Gdx.files.getExternalStoragePath());
 //        texture = new Texture(Gdx.files.external("sc_map.png"));
         float max = 100;
         float z = 0;
         mesh.setVertices(new float[] {
                 0, 0, z, 0, 1,
                 max, 0, z, 1, 1,
                 0, max, z, 1, 0,
                 -max,  max, z, 0, 0
         });
         mesh.setIndices(new short[] { 0, 1, 2});
 
         camera = new OrthographicCamera(WIDTH, HEIGHT);
         camera.zoom  = .3f;
         //camera.position.set(WIDTH / 2, HEIGHT / 2, 0);
         glViewport = new Rectangle(0, 0, WIDTH, HEIGHT);
 
         for (FileHandle fh: Gdx.files.internal("./").list())
             System.err.println(fh.name());
         model = ObjLoader.loadObj(Gdx.files.internal("sun_clock.obj").read(), true);
         texture = new Texture(Gdx.files.internal("1853.jpg"));
         Gdx.gl.glEnable(GL10.GL_DEPTH_TEST);
         Gdx.gl10.glTranslatef(0.0f,0.0f,-3.0f);
 
     }
 
     protected int lastTouchX;
     protected int lastTouchY;
     protected float rotateZ=0.01f;
     protected float increment=0.01f;
 
     @Override
     public void render() {
         Gdx.gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
         Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
        camera.rotate(prevZ, 0, 0, 1);
        camera.rotate(prevX, 1, 0, 0);
        camera.rotate(prevY, 0, 1, 0);

         camera.update();
         camera.apply(Gdx.gl10);
        // Texturing - -------------------- /
         Gdx.gl.glActiveTexture(GL10.GL_TEXTURE0);
         Gdx.gl.glEnable(GL10.GL_TEXTURE_2D);
         texture.bind();
         model.render(GL10.GL_TRIANGLES);
 
         if (Gdx.input.justTouched())  {
             setAngle(Gdx.input.getX());
         }
        camera.rotate(-prevY, 0, 1, 0);
        camera.rotate(-prevX, 1, 0, 0);
        camera.rotate(-prevZ, 0, 0, 1);

         if (true) return;
         if (Gdx.input.justTouched()) {
             lastTouchX = Gdx.input.getX();
             lastTouchY = Gdx.input.getY();
         } else if (Gdx.input.isTouched()) {
             camera.rotate(0.2f * (lastTouchX - Gdx.input.getX()), 0, 1.0f, 0);
             camera.rotate(0.2f * (lastTouchY - Gdx.input.getY()), 1.0f, 0, 0);
 
             lastTouchX = Gdx.input.getX();
             lastTouchY = Gdx.input.getY();
         }
         rotateZ+=increment;
     }
 //
 //    @Override
 //    public void resize(int arg0, int arg1) {
 //        float aspectRatio = (float) arg0 / (float) arg1;
 //        camera = new PerspectiveCamera(67, 2f * aspectRatio, 2f);
 //        camera.near=0.1f;
 //        camera.translate(0, 0, 0);
 //    }
 
 
 
 
     private float angle = Float.MAX_VALUE;
 
     public void setAngle(float a) {
         camera.rotate(a-prevZ);
         prevZ = a;
     }
 
 
     @Override
     public void setAsimut(float a) {
 
     }
 
     @Override
     public void setChord(float x, float y, float z) {
         System.err.printf("%.2f   %.2f   %.2f\n", x, y, z);
         if (camera == null) return;
         //camera.lookAt(-(float)cos(cast(x)), -(float)cos(cast(y)), -(float)(sin(cast(x))+sin(cast(y))));
//        camera.rotate(z - prevZ, 0, 0, 1);
//        camera.rotate(x - prevX, 1, 0, 0);
//        camera.rotate(y-prevY, 0, 1, 0);
        prevZ=-z-360;
        prevX=-x-360;
        prevY = -y-360;
     }
 
     float prevZ;
     float prevX;
     float prevY;
 
     double cast(float a) {
         return a*Math.PI/180.;
     }
     //    @Override
     public void render2() {
         handleInput();
         //camera.rotate(angle, 0, 1, 0);
         if (angle != Float.MAX_VALUE)
             camera.direction.set((float) Math.sqrt(1-angle*angle), 0f, angle);
         else
             camera.direction.set((float)(Math.sin(al)),0f, -(float) Math.cos(al));
         System.err.println(camera.direction + " | " + camera.position + " " + camera.zoom);
 
 
         GL10 gl = Gdx.graphics.getGL10();
         // Camera --------------------- /
         gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
         gl.glViewport((int) glViewport.x, (int) glViewport.y,
                 (int) glViewport.width, (int) glViewport.height);
 
         camera.update();
         camera.apply(gl);
 
 //        // Texturing --------------------- /
 //        gl.glActiveTexture(GL10.GL_TEXTURE0);
 //        gl.glEnable(GL10.GL_TEXTURE_2D);
 //        texture.bind();
 
         mesh.render(GL10.GL_TRIANGLES);
 
     }
 
     private void handleInput() {
         if(Gdx.input.isKeyPressed(Input.Keys.A)) {
             camera.zoom += 0.02;
         }
         if(Gdx.input.isKeyPressed(Input.Keys.Q)) {
             camera.zoom -= 0.02;
         }
         if(Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
             if (camera.position.x > 0)
                 camera.translate(-3, 0, 0);
         }
         if(Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
             if (camera.position.x < 1024)
                 camera.translate(3, 0, 0);
         }
         if(Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
             if (camera.position.y > 0)
                 camera.translate(0, -3, 0);
         }
         if(Gdx.input.isKeyPressed(Input.Keys.UP)) {
             if (camera.position.y < 1024)
                 camera.translate(0, 3, 0);
         }
         if(Gdx.input.isKeyPressed(Input.Keys.W)) {
             camera.rotate(-rotationSpeed, 0, 0, 1);
         }
         if(Gdx.input.isKeyPressed(Input.Keys.E)) {
             camera.rotate(rotationSpeed, 0, 0, 1);
         }
         if (Gdx.input.isKeyPressed(Input.Keys.SPACE)){
             //camera.rotate(rotationSpeed, 0, 1, 0);
             al += .01f;
 
         }
         if (Gdx.input.isKeyPressed(Input.Keys.BACKSPACE)){
 //            camera.rotate(-rotationSpeed, 0, 1, 0);
             al -= .01f;
         }
 
         if (Gdx.input.isKeyPressed(Input.Keys.O)){
             camera.position.z -= 3;
         }
         if (Gdx.input.isKeyPressed(Input.Keys.P)){
             camera.position.z += 3;
         }
 
     }
 
     private float al;
 
     @Override
     public void resize(int width, int height) {
         // TODO Auto-generated method stub
     }
 
     @Override
     public void resume() {
         // TODO Auto-generated method stub
     }
 
     @Override
     public void dispose() {
         // TODO Auto-generated method stub
     }
 
     @Override
     public void pause() {
         // TODO Auto-generated method stub
     }
 }
