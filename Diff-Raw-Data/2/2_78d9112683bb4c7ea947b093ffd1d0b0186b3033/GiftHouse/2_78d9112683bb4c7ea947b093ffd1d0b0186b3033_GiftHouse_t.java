 package AssistedScene;
 
 import Application.GameException;
 import Estate.EstateManager;
 import Player.Movement;
 import Prop.PropManager;
 import UI.CommandLine;
 
 public class GiftHouse implements Scene {
     private final GiftSelectorFactory factory;
     private final CommandLine commandLine = new CommandLine();
 
     public GiftHouse(PropManager propManager, EstateManager estateManager) {
         factory = new GiftSelectorFactory(propManager, estateManager);
     }
 
     public void handle(String roleName, Movement movement) {
         showPromptMessage();
         handleInput(roleName);
     }
 
     private void handleInput(String roleName) {
         try {
             factory.get(commandLine.waitForInput("请输入您要选择的礼品编号：")).select(roleName);
         } catch (GameException e) {
            commandLine.outputInNewline(e.toString());
         }
     }
 
     private void showPromptMessage() {
         commandLine.outputInNewline("欢迎光临礼品屋，请选择一件您 喜欢的礼品：");
         commandLine.outputInNewline("礼品\t编号\n奖 金\t1\n点数卡\t2\n福 神\t3\n");
     }
 }
