 package blender;
 import org.lwjgl.opengl.GL11; 
 
 public class Mesh {
   private String _name;
   private KeyFrame[] _action;
   private int _remainder = 0;
   private int _currentFrame = 0;
   private int _interval = 0;
   private int _fps = 0;
  private int rot=10; // temp used to play animation
 
   public Mesh(String name) {
     _name = name;
   }
 
   public String getName() {
     return _name;
   }
 
   public void addAction(String actionName, int fps, KeyFrame[] frames) {
     _action = frames;
     _fps = fps;
     // Divide 1000ms by fps from new action to obtain the ms 
     //  interval for the animation
     _interval = 1000/_fps;
     System.out.printf("Added action: %s", actionName);
   }
 
   /* render the mesh using arrays */
   public void render(GameWindow window, int delta) {
     GL11.glTranslatef(0.0f, -2.0f, -10.0f);//position in front of camera 
    GL11.glRotatef(rot++, 0.0f, 1.0f, 0.0f);//rotate 
     /* 
      * Enable gl arrays and set the pointer states with the FloatBuffers 
      * We change the state of the pointers as we proceed through the 
      * animation... that is, we get them to point to the data stored 
      * in the next animations array. 
      */ 
     GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY); 
     GL11.glEnableClientState(GL11.GL_NORMAL_ARRAY); 
     GL11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY); 
     GL11.glEnableClientState(GL11.GL_COLOR_ARRAY); 
     GL11.glVertexPointer(3, 0, _action[_currentFrame].getVerts()); 
     GL11.glNormalPointer(0, _action[_currentFrame].getNorms()); 
     GL11.glTexCoordPointer(2, 0, _action[_currentFrame].getCoords()); 
     GL11.glColorPointer(3, 0, _action[_currentFrame].getCols()); 
     //System.out.println(currentFrame);
     /* 
      * This single line of code does the work of all the 
      * glBegin(TRIANGLES)... glEnd() function calls normally used. 
      * It works because the state of the pointers has been pointed to 
      * the correct arrays. Given this state, glDrawArrays steps 
      * through the arrays using pointer math. 
      */ 
     GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 
         _action[_currentFrame].getVertCount()); 
   }
 
   public void update(GameWindow window, int delta) {
     // _remainder keeps track of the ms that build up just in case not
     // enough time passes to warrant a frame change (i.e. if time passed
     // is less than _interval
    _currentFrame = _currentFrame + (_remainder + delta)/_interval;
     _remainder = ((delta + _remainder) % _interval);
     if(_currentFrame >= _action.length) {
       _currentFrame = 0; 
     } 
   }
 
 }
