 /**
  * ArtGenerator - Package: syam.artgenerator.command
  * Created: 2012/11/21 19:41:06
  */
 package syam.artgenerator.command;
 
 import java.io.File;
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import syam.artgenerator.Perms;
 import syam.artgenerator.exception.CommandException;
 import syam.artgenerator.generator.Direction;
 import syam.artgenerator.generator.GeneratorTask;
 import syam.artgenerator.generator.Timer;
 import syam.artgenerator.util.Actions;
 
 /**
  * GenerateCommand (GenerateCommand.java)
  * @author syam(syamn)
  */
 public class GenerateCommand extends BaseCommand{
     public GenerateCommand(){
         bePlayer = true;
         name = "generate";
         argLength = 1;
         usage = "<URL,FileName> (up/down) <- generate dot art";
     }
 
     @Override
     public void execute() throws CommandException {
         // check source: URL
         URL url = null;
         try{
             url = new URL(args.get(0));
         }catch(MalformedURLException ex){
             // through this exception
         }
 
         // check source: File
         File file = null;
         if (url == null){
             file = new File(plugin.getDataFolder().getPath() + System.getProperty("file.separator") + "image", args.get(0));
             if (!file.exists()){
                 throw new CommandException(msgPrefix+ "&cファイルが見つかりません！ " + file.getPath());
             }
             if (!file.canRead()){
                 throw new CommandException(msgPrefix+ "&cファイルが読み込めません！ " + file.getPath());
             }
         }
 
         // check direction
        Direction dir = null;
         if (args.size() >= 2){
            final String str1 = args.get(1);
             for(Direction d : Direction.values()){
                 if (d.name().equalsIgnoreCase(str1)){
                     dir = d;
                 }
             }
            if (dir == null)
                throw new CommandException(msgPrefix+ "&c無効な方向指定です！ (UP/DOWN)");
        }else{
            dir = Direction.FACE;
         }
 
         // check already running, synchronized check
         synchronized (GenerateCommand.class){
             if (Timer.isRunning(sender.getName())){
                 throw new CommandException(msgPrefix+ "&c既に生成タスクを実行中です！");
             }
             final GeneratorTask task = new GeneratorTask(plugin, sender, dir);
             if (url != null) task.setSource(url);
             else task.setSource(file);
             final int taskID = plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, task, 0L);
             Timer.putTask(sender.getName(), taskID);
         }
 
         Actions.message(sender, msgPrefix + "&aドットアートの生成を開始しました..");
     }
 
     @Override
     public boolean permission() {
         return Perms.GENERATE.has(sender);
     }
 }
