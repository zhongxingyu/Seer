 package pro.trousev.cleer.commands;
 
 import java.io.File;
 import java.io.PrintStream;
 import java.util.List;
 
 import pro.trousev.cleer.Plugin.Interface;
 
 public class Library extends Command {
 
 	@Override
 	public String name() {
 		return "library";
 	}
 
 	@Override
 	public String help(boolean is_short) {
 		if(is_short) return "Media Library management tool";
 		return 
 				"Media Library management tool\n" +
 				"You may use these subcommands:\n" +
 				"    library list             -- list all folders in library\n" +
 				"    library add <folder>     -- add new folder to library\n" +
 				"    library remove <folder>  -- remove folder from library\n" +
 				"    library scan <folder>    -- scan/rescan selected folder\n" +
 				"    library scan             -- scan/rescan all library\n";
 	}

 	@Override
 	public boolean main(List<String> args, final PrintStream stdout, Interface iface) {
 		String command;
 		try
 		{
 			command = args.remove(0);
 		}
 		catch(Exception e)
 		{
 			stdout.println("Need subcommand. ");
 			return false;
 		}
 		if(command.equals("list"))
 		{
 			for(File f: iface.library().folders())
 			{
 				stdout.println(f.getAbsolutePath());
 			}
 		}
 		if(command.equals("add"))
 		{
 			String folder;
 			try
 			{
				folder = args.remove(0);
 			}
 			catch (Exception e)
 			{
 				stdout.println("Folder required.");
 				return false;
 			}
 			iface.library().folder_add(new File(folder));
 		}
 		if(command.equals("remove"))
 		{
 			String folder;
 			try
 			{
				folder = args.remove(0);
 			}
 			catch (Exception e)
 			{
 				stdout.println("Folder required.");
 				return false;
 			}
 			iface.library().folder_remove(new File(folder));
 		}
 		if(command.equals("scan"))
 		{
 			String folder;
 			try
 			{
				folder = args.remove(0);
 			}
 			catch (Exception e)
 			{
 				folder = null;
 			}
 			
 			pro.trousev.cleer.Library.FolderScanCallback callback = new pro.trousev.cleer.Library.FolderScanCallback() {
 				int c=0;
 				int m=0;
 				@Override
 				public void started() {
 					// TODO Auto-generated method stub
 					
 				}
 				
 				@Override
 				public void progress(int current, int maximum) {
 					c = current;
 					m = maximum;
 					if(current % 100 == 0)
 						stdout.println(String.format("Overall progress : "+current+"/"+maximum));
 				}
 				
 				@Override
 				public void message(String message) {
 					stdout.println(String.format("%d/%d %s",c,m,message));
 				}
 				
 				@Override
 				public void finished() {
 					stdout.println("Scanning complete.");
 				}
 			};
 			if(folder == null) iface.library().folder_scan(callback);
 			else iface.library().folder_scan(new File(folder), callback);
 		}
		
 		return true;
 	}
 
 }
