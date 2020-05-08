 package com.example.glttt;
 
 import com.example.glttt.pulser.PulseManager;
 import com.example.glttt.shader.IShader;
 import com.example.glttt.shapes.ShapeFactory;
 import com.example.glttt.shapes.Triangle;
 
 public class SceneFactory {
 
     private static final float ORIGINAL_TRI_VERTEX_DIVISOR = 500.0f;
     private static final float BOARD_VERTEX_DIVISOR = 50.0f;
 
     private static final float[] PEG_COLOUR_NORMAL = {0.55f, 0.55f, 0.48f, 0.3f};
 
     private enum PegLabel {
         PEG_A,
         PEG_B,
         PEG_C,
         PEG_D,
         PEG_E,
         PEG_F,
         PEG_G,
         PEG_H,
         PEG_NONE
     }
 
     private static final int[][] PEG_POS = {
             {-50,50},{0,50},{50,50},
             {-25,0},{25,0},
             {-50,-50},{0,-50},{50,-50}
     };
 
     private static final int PEG_THICK = 1;
 
     private ShapeFactory mShapeFactory;
     private PulseManager mPulseManager;
     private GestureManager mGestureManager;
 
     public SceneFactory( PulseManager pulseManager, GestureManager gestureManager, boolean includeNormalData ) {
         mShapeFactory = new ShapeFactory(includeNormalData);
         mPulseManager = pulseManager;
         mGestureManager = gestureManager;
     }
 
     public static enum TYPE {
         NO_SCENE,
         NEW_GAME_SCENE,
         GAME_BOARD_SCENE
     }
 
     public Scene create( TYPE type, IShader shader ) {
         Scene scene;
 
         switch (type) {
             case NEW_GAME_SCENE:
                 scene = createNewGameScene(shader);
                 break;
 
             case GAME_BOARD_SCENE:
                 scene = createGameBoardScene(shader);
                 break;
 
             case NO_SCENE:
                 scene = createEmptyScene(shader);
                 break;
 
             default:
                 throw new RuntimeException("unknown scene type: " + type);
         }
 
         return scene;
     }
 
     private Scene createGameBoardScene( IShader shader ) {
         GameBoardInputReceiver inputReceiver = new GameBoardInputReceiver();
         Scene scene = new Scene(shader, new GameBoardSceneChangeHandler(), inputReceiver);
         mPulseManager.setPulseReceiver(inputReceiver);
         mGestureManager.setGestureListener(inputReceiver);
 
         float boardVertices[] = {
                 -100.0f, 0.0f, -100.0f,
                 -100.0f, 0.0f, 100.0f,
                 100.0f, 0.0f, 100.0f,
                 100.0f, 0.0f, -100.0f
         };
 
         for (int x=0; x<8; ++x) {
             float[] pegVerticesFront = {
                     PEG_POS[x][0] - PEG_THICK, 0.0f, PEG_POS[x][1] - PEG_THICK,
                     PEG_POS[x][0] - PEG_THICK, 75.0f, PEG_POS[x][1] - PEG_THICK,
                     PEG_POS[x][0] + PEG_THICK, 75.0f, PEG_POS[x][1] - PEG_THICK,
                     PEG_POS[x][0] + PEG_THICK, 0.0f, PEG_POS[x][1] - PEG_THICK
             };
 
             float[] pegVerticesBack = {
                     PEG_POS[x][0] - PEG_THICK, 0.0f, PEG_POS[x][1] + PEG_THICK,
                     PEG_POS[x][0] + PEG_THICK, 0.0f, PEG_POS[x][1] + PEG_THICK,
                     PEG_POS[x][0] + PEG_THICK, 75.0f, PEG_POS[x][1] + PEG_THICK,
                     PEG_POS[x][0] - PEG_THICK, 75.0f, PEG_POS[x][1] + PEG_THICK
             };
 
             float[] pegVerticesLeft = {
                     PEG_POS[x][0] - PEG_THICK, 0.0f, PEG_POS[x][1] + PEG_THICK,
                     PEG_POS[x][0] - PEG_THICK, 75.0f, PEG_POS[x][1] + PEG_THICK,
                     PEG_POS[x][0] - PEG_THICK, 75.0f, PEG_POS[x][1] - PEG_THICK,
                     PEG_POS[x][0] - PEG_THICK, 0.0f, PEG_POS[x][1] - PEG_THICK
             };
 
             float[] pegVerticesRight = {
                     PEG_POS[x][0] + PEG_THICK, 0.0f, PEG_POS[x][1] + PEG_THICK,
                     PEG_POS[x][0] + PEG_THICK, 0.0f, PEG_POS[x][1] - PEG_THICK,
                     PEG_POS[x][0] + PEG_THICK, 75.0f, PEG_POS[x][1] - PEG_THICK,
                     PEG_POS[x][0] + PEG_THICK, 75.0f, PEG_POS[x][1] + PEG_THICK
             };
 
             float[] pegVerticesTop = {
                     PEG_POS[x][0] - PEG_THICK, 75.0f, PEG_POS[x][1] - PEG_THICK,
                     PEG_POS[x][0] - PEG_THICK, 75.0f, PEG_POS[x][1] + PEG_THICK,
                     PEG_POS[x][0] + PEG_THICK, 75.0f, PEG_POS[x][1] + PEG_THICK,
                     PEG_POS[x][0] + PEG_THICK, 75.0f, PEG_POS[x][1] - PEG_THICK
             };
 
             Triangle[] pegTris;
            ModelObject peg = new ModelObject("peg"+x);
 
             pegTris = mShapeFactory.createRectangle(pegVerticesFront, PEG_COLOUR_NORMAL, BOARD_VERTEX_DIVISOR, "peg"+x+"_front");
             peg.add(pegTris);
 
             pegTris = mShapeFactory.createRectangle(pegVerticesBack, PEG_COLOUR_NORMAL, BOARD_VERTEX_DIVISOR, "peg"+x+"_back");
             peg.add(pegTris);
 
             pegTris = mShapeFactory.createRectangle(pegVerticesLeft, PEG_COLOUR_NORMAL, BOARD_VERTEX_DIVISOR, "peg"+x+"_left");
             peg.add(pegTris);
 
             pegTris = mShapeFactory.createRectangle(pegVerticesRight, PEG_COLOUR_NORMAL, BOARD_VERTEX_DIVISOR, "peg"+x+"_right");
             peg.add(pegTris);
 
             pegTris = mShapeFactory.createRectangle(pegVerticesTop, PEG_COLOUR_NORMAL, BOARD_VERTEX_DIVISOR, "peg"+x+"_top");
             peg.add(pegTris);

             scene.add(peg);
         }
 
         Triangle[] boardTris = mShapeFactory.createRectangle(boardVertices, new float[]{0.1f, 0.2f, 0.5f, 1.0f}, BOARD_VERTEX_DIVISOR, "board");
         ModelObject obj = new ModelObject("board");
         obj.add(boardTris);
         scene.add(obj);
 
         Triangle[] sphereTris = mShapeFactory.createSphere("sphere", 10.0f, 10, 10, new float[]{0.4f, 0.0f, 0.0f, 1.0f}, BOARD_VERTEX_DIVISOR);
         obj = new ModelObject("sphere");
         obj.setTranslation(0.0f, 1.0f, 0.0f);
         obj.add(sphereTris);
         scene.add(obj);
 
        Triangle[] touchSphereTris = mShapeFactory.createSphere("touchsphere1", 4.0f, 10, 10, new float[]{0.6f, 0.6f, 0.0f, 1.0f}, BOARD_VERTEX_DIVISOR);
         obj = new ModelObject("touchsphere1");
         obj.setTranslation(-2.0f, 0.0f, -2.0f);
         obj.add(touchSphereTris);
         scene.add(obj);
 
        touchSphereTris = mShapeFactory.createSphere("touchsphere2", 4.0f, 10, 10, new float[]{0.0f, 0.6f, 0.6f, 1.0f}, BOARD_VERTEX_DIVISOR);
         obj = new ModelObject("touchsphere2");
         obj.setTranslation(2.0f, 0.0f, 2.0f);
         obj.add(touchSphereTris);
         scene.add(obj);
 
        touchSphereTris = mShapeFactory.createSphere("touchsphere3", 4.0f, 10, 10, new float[]{0.6f, 0.0f, 0.6f, 1.0f}, BOARD_VERTEX_DIVISOR);
         obj = new ModelObject("touchsphere3");
         obj.setTranslation(-2.0f, 0.0f, 2.0f);
         obj.add(touchSphereTris);
         scene.add(obj);
 
         // Position the eye behind the origin.
         final float eyeX = 0.0f;
         final float eyeY = 0.0f;
         final float eyeZ = 1.5f;
 
         // We are looking toward the distance
         final float lookX = 0.0f;
         final float lookY = 0.0f;
         final float lookZ = -5.0f;
 
         // Set our up vector. This is where our head would be pointing were we holding the camera.
         final float upX = 0.0f;
         final float upY = 1.0f;
         final float upZ = 0.0f;
 
         scene.setLookAt(eyeX, eyeY + 5.0f, eyeZ + 15.0f, lookX, lookY, lookZ - 10.0f, upX, upY, upZ);
 
         return scene;
     }
 
     private Scene createNewGameScene( IShader shader )
     {
         Scene scene = new Scene(shader, new NewGameSceneChangeHandler());
 
         float whiteVertices[] = {
                 250f, 300f, 0f,
                 450f, 300f, 0f,
                 450f, 500f, 0f,
                 250f, 500f, 0f
         };
 
         float redVertices[] = {
                 500f, 300f, 0f,
                 700f, 300f, 0f,
                 700f, 500f, 0f,
                 500f, 500f, 0f
         };
 
         //gl.glColor4f( 1.0f, 0.0f, 0.0f, 1.0f );
         //draw_message( 0.5, 200, 200, "Select your colour:" );
 
         Triangle[] whiteSquareTris = mShapeFactory.createRectangle(whiteVertices, new float[]{0.9f, 0.9f, 0.9f, 1.0f}, ORIGINAL_TRI_VERTEX_DIVISOR, "white");
         Triangle[] redSquareTris = mShapeFactory.createRectangle(redVertices, new float[]{1.0f, 0.0f, 0.0f, 1.0f}, ORIGINAL_TRI_VERTEX_DIVISOR, "red");
 
         ModelObject redSquare = new ModelObject("redSquare");
         redSquare.add( redSquareTris );
 
         ModelObject whiteSquare = new ModelObject("whiteSquare");
         whiteSquare.add( whiteSquareTris );
 
         scene.add(redSquare);
         scene.add(whiteSquare);
 
         // Position the eye behind the origin.
         final float eyeX = 1.0f;
         final float eyeY = 0.0f;
         final float eyeZ = 1.5f;
 
         // We are looking toward the distance
         final float lookX = 1.0f;
         final float lookY = 0.0f;
         final float lookZ = -5.0f;
 
         // Set our up vector. This is where our head would be pointing were we holding the camera.
         final float upX = 0.0f;
         final float upY = 1.0f;
         final float upZ = 0.0f;
 
         scene.setLookAt(eyeX, eyeY, eyeZ, lookX, lookY, lookZ, upX, upY, upZ);
 
         return scene;
     }
 
     private Scene createEmptyScene( IShader shader ) {
         return new Scene(shader, new NewGameSceneChangeHandler());
     }
 }
