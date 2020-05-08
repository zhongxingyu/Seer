 package fmv.tools;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.Map;
 
 import fmv.Pair;
 
 public abstract class ExternalTool extends BasicTool {
 	@Override
 	public Pair run(File workDir, String... input) {
 		Pair ret;
 		try {
 			String[] args = getArgs(input);
 			ProcessBuilder pb = new ProcessBuilder(args)
 					.redirectErrorStream(true);
 			if (workDir != null && workDir.exists() && workDir.isDirectory()) {
 				pb.directory(workDir);
 			}
 			System.out.println(pb.command());
 			Map<String, String> env = pb.environment();
 			env.clear();
 			Process p = pb.start();
 			BufferedReader errReader = new BufferedReader(
 					new InputStreamReader(p.getErrorStream()));
 			String line;
 			while ((line = errReader.readLine()) != null) {
 				System.err.println(line);
 			}
			StringBuilder sb = new StringBuilder();
 			BufferedInputStream is = new BufferedInputStream(p.getInputStream());
			byte[] bytes = new byte[65536];
			while ((is.read(bytes)) != -1) {
				sb.append(new String(bytes));
 			}
 			int exit = p.waitFor();
			ret = new Pair(exit, sb.toString());
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 			ret = new Pair(1, e.getMessage());
 		} catch (IOException e) {
 			e.printStackTrace();
 			ret = new Pair(1, e.getMessage());
 		}
 		return ret;
 
 	}
 
 }
