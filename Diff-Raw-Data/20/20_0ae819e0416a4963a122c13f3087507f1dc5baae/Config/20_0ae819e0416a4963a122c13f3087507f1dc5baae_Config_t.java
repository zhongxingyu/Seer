 package ggit;
 
 import java.io.File;
 import java.io.IOException;
 
 import org.eclipse.jface.preference.IPreferenceStore;
 
 public class Config {
 
 	public static String getGitExecutable()
 	{
 		IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
 		return  preferenceStore.getString(Activator.GIT_LOCATION);
 		//return "C:\\git\\bin\\git.exe";
 	}
 
 	public static File getWorkDir() {
 
 		IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
 		String string = preferenceStore.getString(Activator.WORKDIR_LOCATION);
 		return new File(string);
 	}
 
 	public static String execGit(String ...args)
 	{
 		String[] cmdLine =  new String[args.length+1];
 		cmdLine[0]=getGitExecutable();
 		System.arraycopy(args, 0, cmdLine, 1, args.length);
 		Process exec;
 		try {
 			exec = Runtime.getRuntime().exec(cmdLine,null,getWorkDir());
 			StringBuilder stringBuilder = new StringBuilder();
 			for(;;)
 			{
 				int read = exec.getInputStream().read();
 				if( read<0)
 				{
 					break;
 				}
 				stringBuilder.append((char)read);
 			}
 			int result;
 			try {
 				result = exec.waitFor();
 			} catch (InterruptedException e) {
 				throw new RuntimeException(e);
 			}
 			// what to do with bad result ?
 			if( result != 0){
				throw new RuntimeException("Process"+cmdLine[0]+" return not 0 "+result+stringBuilder.toString());
 			}
 			return stringBuilder.toString();
 		} catch (IOException e) {
 			throw new RuntimeException(e);
 		}
 	}
 }
