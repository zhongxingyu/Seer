 package com.github.tclem.arduinocli;
 
 import java.io.File;
 import java.io.IOException;
 import java.lang.reflect.Array;
 import java.lang.reflect.Field;
 
 import processing.app.Base;
 import processing.app.Editor;
 import processing.app.SerialException;
 import processing.app.Sketch;
 import processing.app.SketchCode;
 import processing.app.debug.Compiler;
 import processing.app.debug.RunnerException;
 
 public class MySketch extends Sketch {
 
 	String primaryClassName;
 	String tempBuildFolder;
 
 	public MySketch(Editor editor, String path) throws IOException {
 		super(editor, path);
 	}
 
 	public void compile() throws RunnerException {
 		tempBuildFolder = Base.getBuildFolder().getAbsolutePath();
 //		System.out.println(tempBuildFolder);
 
 		MyPdePreprocessor pp = new MyPdePreprocessor();
 		primaryClassName = preprocess(tempBuildFolder, pp);
 		System.out.println(pp.getExtraImports());
 
 		Compiler compiler = new MyCompiler();
 		Boolean res = compiler.compile(this, tempBuildFolder, primaryClassName, false);
 		System.out.printf("Compilation %s.\n", res ? "succeeded" : "failed");
 	}
 
 	public void compileAndDeploy() throws RunnerException, SerialException {
 		compile();
		this.upload(tempBuildFolder, primaryClassName, false);
 	}
 
 	private void setField(String fieldName, Object value) {
 		try {
 			Field field = Sketch.class.getDeclaredField(fieldName);
 			field.setAccessible(true);
 			field.set(this, value);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void setCompilingProgress(int percent) {
 		System.out.printf("%d\r", percent);
 	}
 
 	protected void load() {
 		File folder = getFolder();
 		File codeFolder = getCodeFolder();
 		File dataFolder = getDataFolder();
 		int codeCount = getCodeCount();
 		SketchCode[] code = getCode();
 		File primaryFile = getPrimaryFile();
 
 		codeFolder = new File(folder, "code");
 		dataFolder = new File(folder, "data");
 
 		// get list of files in the sketch folder
 		String list[] = folder.list();
 
 		// reset these because load() may be called after an
 		// external editor event. (fix for 0099)
 		codeCount = 0;
 
 		code = new SketchCode[list.length];
 
 		String[] extensions = getExtensions();
 
 		for (String filename : list) {
 			// Ignoring the dot prefix files is especially important to avoid
 			// files
 			// with the ._ prefix on Mac OS X. (You'll see this with Mac files
 			// on
 			// non-HFS drives, i.e. a thumb drive formatted FAT32.)
 			if (filename.startsWith("."))
 				continue;
 
 			// Don't let some wacko name a directory blah.pde or bling.java.
 			if (new File(folder, filename).isDirectory())
 				continue;
 
 			// figure out the name without any extension
 			String base = filename;
 			// now strip off the .pde and .java extensions
 			for (String extension : extensions) {
 				if (base.toLowerCase().endsWith("." + extension)) {
 					base = base.substring(0,
 							base.length() - (extension.length() + 1));
 
 					// Don't allow people to use files with invalid names, since
 					// on load,
 					// it would be otherwise possible to sneak in nasty
 					// filenames. [0116]
 					if (Sketch.isSanitaryName(base)) {
 						code[codeCount++] = new MySketchCode(new File(folder,
 								filename), extension);
 					}
 				}
 			}
 		}
 		// Remove any code that wasn't proper
 		code = (SketchCode[]) subset(code, 0, codeCount);
 
 		// move the main class to the first tab
 		// start at 1, if it's at zero, don't bother
 		for (int i = 1; i < codeCount; i++) {
 			// if (code[i].file.getName().equals(mainFilename)) {
 			if (code[i].getFile().equals(primaryFile)) {
 				SketchCode temp = code[0];
 				code[0] = code[i];
 				code[i] = temp;
 				break;
 			}
 		}
 
 		setField("code", code);
 
 		// sort the entries at the top
 		sortCode();
 	}
 
 	static public Object subset(Object list, int start, int count) {
 		Class<?> type = list.getClass().getComponentType();
 		Object outgoing = Array.newInstance(type, count);
 		System.arraycopy(list, start, outgoing, 0, count);
 		return outgoing;
 	}
 }
