 /*
  * If you intend to use, modify or redistribute this file contact kreisel.sebastian@gmail.com
  */
 
 package com.elfeck.ephemeral.glContext;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 
 import com.elfeck.ephemeral.glContext.uniform.EPHUniformLookup;
 
 
 public class EPHShaderProgramPool {
 
 	private Map<String, EPHShaderProgram> programs;
 
 	public EPHShaderProgramPool(String parentPath) {
 		programs = new HashMap<String, EPHShaderProgram>();
 		initShaderProgramPool(parentPath);
 	}
 
 	private void initShaderProgramPool(String parentPath) {
 		Map<String, String[]> shaderSrcPairs = new HashMap<String, String[]>();
 		loadShaderFiles(new File(parentPath), shaderSrcPairs);
 		for (String key : shaderSrcPairs.keySet()) {
 			programs.put(
 					key,
 					new EPHShaderProgram(shaderSrcPairs.get(key)[0], shaderSrcPairs.get(key)[1], uniformStringToShaderUniforms(shaderSrcPairs.get(key)[2],
 							shaderSrcPairs.get(key)[3])));
 		}
 	}
 
 	private void loadShaderFiles(File folder, Map<String, String[]> shaderSrcPairs) {
 		for (File fileEntry : folder.listFiles()) {
 			if (fileEntry.isDirectory()) {
 				loadShaderFiles(fileEntry, shaderSrcPairs);
 			} else
 				if (fileEntry.getName().contains("_frag.glsl") || fileEntry.getName().contains("_vert.glsl")) {
 					String type = fileEntry.getName().substring(fileEntry.getName().length() - 9, fileEntry.getName().length());
 					String name = fileEntry.getName().substring(0, fileEntry.getName().length() - 10);
 					if (type.equals("vert.glsl")) {
 						if (shaderSrcPairs.containsKey(name)) {
 							shaderSrcPairs.get(name)[0] = loadShaderSource(fileEntry);
 						} else {
 							shaderSrcPairs.put(name, new String[] { loadShaderSource(fileEntry), null, null, null });
 						}
 						shaderSrcPairs.get(name)[2] = extractUniforms(shaderSrcPairs.get(name)[0]);
 					} else {
 						if (type.equals("frag.glsl")) {
 							if (shaderSrcPairs.containsKey(name)) {
 								shaderSrcPairs.get(name)[1] = loadShaderSource(fileEntry);
 							} else {
 								shaderSrcPairs.put(name, new String[] { null, loadShaderSource(fileEntry), null, null });
 							}
 						}
 						shaderSrcPairs.get(name)[3] = extractUniforms(shaderSrcPairs.get(name)[1]);
 					}
 				}
 		}
 	}
 
 	private String extractUniforms(String src) {
 		String result = "";
 		String[] rawTokens = src.split(";");
 		for (String s : rawTokens) {
 			s = s.replaceAll("\n", "");
 			if (s.startsWith("uniform")) {
 				String[] lineTokens = s.split(" ");
 				result += lineTokens[1] + "#" + lineTokens[2] + "%";
 			}
 		}
 		return result;
 	}
 
 	private EPHShaderUniformCollection uniformStringToShaderUniforms(String vertString, String fragString) {
 		EPHShaderUniformCollection shaderUniforms = new EPHShaderUniformCollection();
 		String[] uniforms;
 		if (!vertString.equals("")) {
 			uniforms = vertString.split("%");
 			for (String s : uniforms) {
 				String[] cut = s.split("#");
 				shaderUniforms.addUniformLookup(new EPHUniformLookup(cut[1]));
 			}
 		}
 		if (!fragString.equals("")) {
 			uniforms = fragString.split("%");
 			for (String s : uniforms) {
 				String[] cut = s.split("#");
 				shaderUniforms.addUniformLookup(new EPHUniformLookup(cut[1]));
 			}
 		}
 		return shaderUniforms;
 	}
 
 	private String loadShaderSource(File file) {
 		StringBuilder source = new StringBuilder();
 		try {
 			BufferedReader br = new BufferedReader(new FileReader(file));
 			String line = null;
 			while ((line = br.readLine()) != null) {
 				if (line.contains("//@insert")) {
 					source.append(loadNestedFile(line, file.getParent()));
 				} else {
 					source.append(line).append('\n');
 				}
 			}
 			br.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return source.toString();
 	}
 
 	private String loadNestedFile(String insert, String parentPath) {
 		StringBuilder source = new StringBuilder();
		insert = insert.replaceAll("//@insert", "").replaceAll(" ", "").replaceAll("\t", "");
 		String type = insert.substring(insert.indexOf(".") + 1);
 		try {
 			BufferedReader br = new BufferedReader(new FileReader(parentPath + "/" + insert.substring(0, insert.indexOf(".")) + ".glsl"));
 			String line = null;
 			boolean found = false;
 			while ((line = br.readLine()) != null) {
 				if (line.contains("//@section " + type)) {
 					found = true;
 					continue;
 				}
 				if (found && line.contains("//@section")) found = false;
 				if (found) source.append(line).append('\n');
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return source.toString();
 	}
 
 	protected void glInit() {
 		for (String key : programs.keySet()) {
 			programs.get(key).glCompileShaderSrc();
 		}
 	}
 
 	protected EPHShaderProgram getShaderProgram(String key) {
 		return programs.get(key);
 	}
 
 	public void glDisposeShaderPrograms() {
 		for (String key : programs.keySet()) {
 			programs.get(key).glDispose();
 		}
 	}
 
 }
