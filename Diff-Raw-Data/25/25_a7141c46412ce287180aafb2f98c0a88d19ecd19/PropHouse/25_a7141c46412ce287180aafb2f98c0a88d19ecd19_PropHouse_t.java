 package AssistedScene;
 
 import Application.GameException;
 import Player.Movement;
 import Prop.PropManager;
 import UI.CommandLine;
 
 public class PropHouse implements Scene {
     private final PropSelectorFactory factory;
     private final CommandLine commandLine = new CommandLine();
 
     public PropHouse(PropManager propManager) {
         factory = new PropSelectorFactory(propManager);
     }
 
     public void handle(String roleName, Movement movement) {
         showPromptMessage();
         handleInputUntilQuit(roleName);
     }
 
     private void handleInputUntilQuit(String roleName) {
         while (handleInput(roleName));
     }
 
     private boolean handleInput(String roleName) {
         try {
             factory.get(commandLine.waitForInput("请输入您要购买的道具编号：").toLowerCase()).select(roleName);
             return true;
         } catch (GameException e) {
            commandLine.outputInNewline(e.toString());
             return e.isNeedRetry();
         }
     }
 
     private void showPromptMessage() {
         commandLine.outputInNewline("欢迎光临道具屋，请选择您所需要的道具：");
         commandLine.outputInNewline("道具\t\t编号\t价值（点数）\n路障\t\t1\t\t50\n机器娃娃\t2\t\t30\n炸弹\t\t3\t\t50");
     }
 }
