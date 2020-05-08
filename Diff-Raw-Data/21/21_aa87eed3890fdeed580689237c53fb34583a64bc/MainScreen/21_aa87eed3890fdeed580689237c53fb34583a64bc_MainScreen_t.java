 package virtualcity3d.screens;
 
 import framework.core.architecture.BaseScreen;
 import framework.core.architecture.Program;
 
 /**
  * Created with IntelliJ IDEA.
  * User: yan
  * Date: 03/09/13
  * Time: 10:27
  * To change this template use File | Settings | File Templates.
  */
 public class MainScreen extends BaseScreen {
 
     public MainScreen(Program program) {
         super(program);
     }
 
     @Override
     public void init() {
         //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public void onDraw() {
         //go to Test screen
        getProgram().setScreen(new ObjLoadingTestScreen(getProgram()));
     }
 
     @Override
     public void onUpdate(long delta) {
         //To change body of implemented methods use File | Settings | File Templates.
     }
 }
