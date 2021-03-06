 package com.badlogic.gdx.jnigen.test;
 
 import com.badlogic.gdx.jnigen.AntScriptGenerator;
 import com.badlogic.gdx.jnigen.BuildConfig;
 import com.badlogic.gdx.jnigen.BuildTarget;
 import com.badlogic.gdx.jnigen.BuildTarget.TargetOs;
 import com.badlogic.gdx.jnigen.NativeCodeGenerator;
 
 public class TestBuild {
 	public static void main(String[] args) throws Exception {
 		// generate C/C++ code
 		new NativeCodeGenerator().generate("src", "bin", "jni");
 		
 		// generate build scripts, for win32 only
 		BuildConfig buildConfig = new BuildConfig("test");
 		BuildTarget win32 = BuildTarget.newDefaultTarget(TargetOs.Windows, false);
 		BuildTarget win64 = BuildTarget.newDefaultTarget(TargetOs.Windows, true);
 		BuildTarget lin32 = BuildTarget.newDefaultTarget(TargetOs.Linux, false);
 		BuildTarget lin64 = BuildTarget.newDefaultTarget(TargetOs.Linux, true);
 		BuildTarget android = BuildTarget.newDefaultTarget(TargetOs.Android, false);
		new AntScriptGenerator().generate(buildConfig, win32, win64, lin32, lin64, android);
 		
 		// build natives
 //		BuildExecutor.executeAnt("jni/build.xml", "-v");
 	}
 }
