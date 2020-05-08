 package com.xored.fmaven;
 
 import java.io.File;
 
 import com.google.common.base.Joiner;
 
 import fan.fmaven.Compiler;
 import fan.fmaven.FanPod;
 import fan.sys.List;
 
 public class FantomCompiler {
 	public static int ERROR = 0;
 	public static int SUCCESSFUL = 1;
 
 	public static class CompileStatus {
 
 		public final int code;
 		public final String msg;
 
 		public CompileStatus(int code) {
 			this(code, null);
 		}
 
 		private CompileStatus(int code, String msg) {
 			this.code = code;
 			this.msg = msg;
 		}
 	}
 
 	@SuppressWarnings("static-access")
 	public CompileStatus compile(FanPod pod, File fanOutputDir) {
 		if (!fanOutputDir.exists()) {
 			fanOutputDir.mkdirs();
 		}
 
		// TODO: use normal path
		String path = "file:/" + fanOutputDir.getPath().replaceAll("\\\\", "/");
		Compiler compiler = new Compiler().make(path.endsWith("/") ? path
				: path + "/");
 		List errors = compiler.compileManifest(pod);
 
 		if (errors.isEmpty()) {
 			return status(SUCCESSFUL);
 		} else {
 			status(ERROR, Joiner.on("; ").join(errors.toArray()));
 		}
 		return status(SUCCESSFUL);
 	}
 
 	private static CompileStatus status(int code, String msg) {
 		return new CompileStatus(code, msg);
 	}
 
 	private static CompileStatus status(int code) {
 		return new CompileStatus(code);
 	}
 }
