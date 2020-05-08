 /**
  * @author Clément Sipieter <csipieter@gmail.com>
  */
 package pi.interpreter.commands;
 
 import java.io.File;
 
 import pi.interpreter.Environment;
 
 public class Ls implements Command
 {
 
   private static final String LABEL = "ls";
   private static final String SYNTAX = "";
   private static final String SHORT_DESC = "display current directory";
 
   @Override
   public String getLabel()
   {
     return LABEL;
   }
 
   @Override
   public int exec(String[] args, Environment env)
   {
     File dir;
     File[] list;
    String dir_name = env.get(Environment.PWD_KEY).toString();
     String file_name;
     boolean opt_all = false;
 
     for (int i = 1; i < args.length; ++i)
       if (args[i].length() > 0)
         {
           if (args[i].charAt(0) != '-')
             {
               if(args[i].charAt(0) == '/')
                 dir_name = args[i];
               else
                 dir_name += args[i];
             }
           else if (args[i].equals("-a"))
             opt_all = true;
         }
 
     dir = new File(dir_name);
     list = dir.listFiles();
     if (list != null)
       for (File file : list)
         {
           file_name = file.getName();
           if(opt_all || file_name.charAt(0) != '.')
             env.out.println(file_name);
         }
 
     return Command.EXIT_SUCCESS;
   }
 
   @Override
   public String manual()
   {
     return syntax();
   }
   
 	public String syntax() {
 		return SYNTAX_KEYWORD + getLabel() + " " + SYNTAX;
 	}
 	public String shortDescription() {
 		return SHORT_DESC;
 	}
 }
