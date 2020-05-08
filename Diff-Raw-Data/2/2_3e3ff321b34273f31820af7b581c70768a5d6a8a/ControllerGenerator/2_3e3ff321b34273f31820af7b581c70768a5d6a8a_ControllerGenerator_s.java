 /*******************************************************************************
  * Copyright (c) 2010 Oobium, Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Jeremy Dowdall <jeremy@oobium.com> - initial API and implementation
  ******************************************************************************/
 package org.oobium.build.gen;
 
 import static org.oobium.build.util.SourceFile.closer;
 import static org.oobium.build.util.SourceFile.find;
 import static org.oobium.utils.FileUtils.writeFile;
 import static org.oobium.utils.StringUtils.camelCase;
 import static org.oobium.utils.StringUtils.packageName;
 import static org.oobium.utils.StringUtils.plural;
 import static org.oobium.utils.StringUtils.repeat;
 import static org.oobium.utils.StringUtils.simpleName;
 import static org.oobium.utils.StringUtils.titleize;
 import static org.oobium.utils.StringUtils.varName;
 
 import java.io.File;
 import java.sql.SQLException;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.TreeSet;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.oobium.app.server.controller.Action;
 import org.oobium.app.server.controller.Controller;
 import org.oobium.build.model.ModelDefinition;
 import org.oobium.build.util.SourceFile;
 import org.oobium.build.workspace.Module;
 import org.oobium.http.HttpRequest;
 
 public class ControllerGenerator {
 
 	private static final char[] regen = {'@','R','e','g','e','n'};
 	private static final char[] CLASS = {'c','l','a','s','s'};
 	private static final char[] IMPORT = {'i','m','p','o','r','t'};
 	private static final char[] PACKAGE = {'p','a','c','k','a','g','e'};
 
 	private static final Matcher[] sigs = new Matcher[7];
 	{
 		sigs[0] = Pattern.compile(".*(public\\s+void\\s+create\\().*", Pattern.DOTALL).matcher("");
 		sigs[2] = Pattern.compile(".*(public\\s+void\\s+update\\().*", Pattern.DOTALL).matcher("");
 		sigs[1] = Pattern.compile(".*(public\\s+void\\s+destroy\\().*", Pattern.DOTALL).matcher("");
 		sigs[3] = Pattern.compile(".*(public\\s+void\\s+showAll\\().*", Pattern.DOTALL).matcher("");
 		sigs[4] = Pattern.compile(".*(public\\s+void\\s+showEdit\\().*", Pattern.DOTALL).matcher("");
 		sigs[5] = Pattern.compile(".*(public\\s+void\\s+showNew\\().*", Pattern.DOTALL).matcher("");
 		sigs[6] = Pattern.compile(".*(public\\s+void\\s+show\\().*", Pattern.DOTALL).matcher("");
 	}
 	
 	public static File createController(Module module, String name) {
 		String controller = camelCase((name.endsWith("Controller")) ? name : (name + "Controller"));
 		String canonicalName = module.packageName(module.controllers) + "." + controller;
		File appController = new File(module.controllers, "ApplicationController");
 		String src = generate(canonicalName, appController.isFile());
 		return writeFile(module.controllers, controller + ".java", src);
 	}
 
 	public static String generate(Module module, ModelDefinition model) {
 		ControllerGenerator gen = new ControllerGenerator(module, model);
 		return gen.doGenerate();
 	}
  	
 	private static String generate(String canonicalName, boolean extendAppController) {
 		SourceFile src = new SourceFile();
 
 		src.packageName = packageName(canonicalName);
 		src.simpleName = simpleName(canonicalName);
 		if(extendAppController) {
 			src.superName = "ApplicationController";
 		} else {
 			src.superName = Controller.class.getSimpleName();
 			src.imports.add(Controller.class.getCanonicalName());
 		}
 		src.imports.add(SQLException.class.getCanonicalName());
 		
 		StringBuilder sb = new StringBuilder();
 		sb.append("\t@Override\n");
 		sb.append("\tpublic void handleRequest() throws SQLException {\n");
 		sb.append("\t\t// TODO handle the request\n");
 		sb.append("\t}");
 		src.methods.put("show", sb.toString());
 		
 		return src.toSource();
 	}
 
 	private final Module module;
 	private final ModelDefinition model;
 	private final boolean extendAppController;
 	private final boolean withViews;
 	private final String mType;
 	private final String mTypePlural;
 	private final String varName;
 	private final String varNamePlural;
 
 	private ControllerGenerator(Module module, ModelDefinition model) {
 		this.module = module;
 		this.model = model;
 		extendAppController = module.getController("ApplicationController").isFile();
 		withViews = !module.hasNature(Module.NATURE_WEBSERVICE);
 		mType = model.getSimpleName();
 		mTypePlural = plural(mType);
 		varName = varName(mType);
 		varNamePlural = varName(mType, true);
 	}
 
 	private void addActionCreateImports(TreeSet<String> imports) {
 		// nothing to do...
 	}
 
 	private void addActionDestroyImports(TreeSet<String> imports) {
 		addImports(imports);
 	}
 	
 	private void addActionUpdateImports(TreeSet<String> imports) {
 		addImports(imports);
 	}
 	
 	private void addImports(int i, TreeSet<String> imports) {
 		switch(i) {
 		case 0: addActionCreateImports(imports); break;
 		case 1: addActionDestroyImports(imports); break;
 		case 2: addActionUpdateImports(imports); break;
 		case 3: addShowAllViewImports(imports); break;
 		case 4: addShowEditViewImports(imports); break;
 		case 5: addShowNewViewImports(imports); break;
 		case 6: addShowViewImports(imports); break;
 		}
 	}
 	
 	private void addImports(TreeSet<String> imports) {
 		imports.add("static " + Action.class.getCanonicalName() + ".*");
 		imports.add(HttpRequest.class.getCanonicalName());
 		imports.add(SQLException.class.getCanonicalName());
 		imports.add(model.getCanonicalName());
 	}
 	
 	private void addShowAllViewImports(TreeSet<String> imports) {
 		addImports(imports);
 		imports.add(List.class.getCanonicalName());
 		if(withViews) {
 			imports.add(module.packageName(module.getViewsFolder(mType))+".ShowAll"+mTypePlural);
 		}
 	}
 
 	private void addShowEditViewImports(TreeSet<String> imports) {
 		addImports(imports);
 		if(withViews) {
 			imports.add(module.packageName(module.getViewsFolder(mType))+".ShowEdit"+mType);
 		}
 	}
 
 	private void addShowNewViewImports(TreeSet<String> imports) {
 		addImports(imports);
 		if(withViews) {
 			imports.add(module.packageName(module.getViewsFolder(mType))+".ShowNew"+mType);
 		}
 	}
 
 	private void addShowViewImports(TreeSet<String> imports) {
 		addImports(imports);
 		if(withViews) {
 			imports.add(module.packageName(module.getViewsFolder(mType))+".Show"+mType);
 		}
 	}
 
 	private String doGenerate() {
 		SourceFile src = new SourceFile();
 
 		String controllerName = model.getControllerName();
 		
 		src.packageName = module.packageName(module.getController(controllerName).getParentFile());
 		src.simpleName = controllerName;
 		if(extendAppController) {
 			src.superName = "ApplicationController";
 		} else {
 			src.superName = Controller.class.getSimpleName();
 			src.imports.add(Controller.class.getCanonicalName());
 		}
 		src.imports.add(SQLException.class.getCanonicalName());
 
 		for(int i = 0; i < 7; i++) {
 			addImports(i, src.imports);
 		}
 		
 		src.methods.put("create", genCreate(false));
 		src.methods.put("update", genUpdate(false));
 		src.methods.put("destroy", genDestroy(false));
 		src.methods.put("showAll", genShowAll(false));
 		if(withViews) {
 			src.methods.put("showEdit", genShowEdit(false));
 			src.methods.put("showNew", genShowNew(false));
 		}
 		src.methods.put("show", genShow(false));
 		
 		return src.toSource();
 	}
 	
 	private String doGenerate(StringBuilder sb) {
 		int start = find(sb, 0, sb.length(), regen);
 		if(start != -1) {
 			TreeSet<String> imports = loadImports(sb);
 			while(start != -1) {
 				int end = find(sb, start+regen.length, sb.length(), '{');
 				for(int i = 0; i < sigs.length; i++) {
 					sigs[i].reset(sb.substring(start, end+1));
 					if(sigs[i].matches()) {
 						end = closer(sb, end);
 						if(end != -1) {
 							addImports(i, imports);
 							sb.replace(start, end+1, genMethod(i, true));
 						}
 						break;
 					}
 				}
 				start = find(sb, end+1, sb.length(), regen);
 			}
 			writeImports(sb, imports);
 		}
 		return sb.toString();
 	}
 	
 	private String genCreate(boolean regen) {
 		StringBuilder sb = new StringBuilder();
 		sb.append(regen ? "@Regen\n\t" : "\t");
 		sb.append("@Override // POST/URL/[models]\n");
 		sb.append("\tpublic void create() throws SQLException {\n");
 		sb.append("\t\t").append(mType).append(" ").append(varName).append(" = ").append("param(\"").append(varName).append("\", new ").append(mType).append("());\n");
 		sb.append("\t\tif(").append(varName).append(".create()) {\n");
 		if(!withViews) {
 			sb.append("\t\t\trenderCreated(").append(varName).append(");\n");
 		} else {
 			sb.append("\t\t\tswitch(wants()) {\n");
 			sb.append("\t\t\t\tcase JS:\n");
 			sb.append("\t\t\t\tcase JSON: renderCreated(").append(varName).append("); break;\n");
 			sb.append("\t\t\t\tcase HTML: redirectTo(").append(varName).append(", show, \"").append(titleize(mType)).append(" was successfully created.\"); break;\n");
 			sb.append("\t\t\t}\n");
 		}
 		sb.append("\t\t} else {\n");
 		if(!withViews) {
 			sb.append("\t\t\trenderErrors(").append(varName).append(");\n");
 		} else {
 			sb.append("\t\t\tswitch(wants()) {\n");
 			sb.append("\t\t\t\tcase JS:\n");
 			sb.append("\t\t\t\tcase JSON: renderErrors(").append(varName).append("); break;\n");
 			sb.append("\t\t\t\tcase HTML: render(new ShowNew").append(mType).append('(').append(varName).append(")); break;\n");
 			sb.append("\t\t\t}\n");
 		}
 		sb.append("\t\t}\n");
 		sb.append("\t}");
 		return sb.toString();
 	}
 	
 	private String genDestroy(boolean regen) {
 		StringBuilder sb = new StringBuilder();
 		sb.append(regen ? "@Regen\n\t" : "\t");
 		sb.append("@Override // DELETE/URL/[models]/id\n");
 		sb.append("\tpublic void destroy() throws SQLException {\n");
 		sb.append("\t\t").append(mType).append(" ").append(varName).append(" = ").append(mType).append(".newInstance(getId());\n");
 		sb.append("\t\tif(").append(varName).append(".destroy()) {\n");
 		if(!withViews) {
 			sb.append("\t\t\trenderDestroyed(").append(varName).append(");\n");
 		} else {
 			sb.append("\t\t\tswitch(wants()) {\n");
 			sb.append("\t\t\t\tcase JS:\n");
 			sb.append("\t\t\t\tcase JSON: renderDestroyed(").append(varName).append(");     break;\n");
 			sb.append("\t\t\t\tcase HTML: redirectTo(").append(varName).append(", showAll); break;\n");
 			sb.append("\t\t\t}\n");
 		}
 		sb.append("\t\t} else {\n");
 		sb.append("\t\t\trenderErrors(").append(varName).append(");\n");
 		sb.append("\t\t}\n");
 		sb.append("\t}");
 
 		return sb.toString();
 	}
 	
 	private String genMethod(int i, boolean regen) {
 		switch(i) {
 		case 0: return genCreate(regen);
 		case 1: return genDestroy(regen);
 		case 2: return genUpdate(regen);
 		case 3: return genShowAll(regen);
 		case 4: return genShowEdit(regen);
 		case 5: return genShowNew(regen);
 		case 6: return genShow(regen);
 		}
 		return null;
 	}
 	
 	private String genShow(boolean regen) {
 		StringBuilder sb = new StringBuilder();
 		sb.append(regen ? "@Regen\n\t" : "\t");
 		sb.append("@Override // GET/URL/[models]/id\n");
 		sb.append("\tpublic void show() throws SQLException {\n");
 		sb.append("\t\t").append(mType).append(" ").append(varName).append(" = ").append(mType).append(".find(getId());\n");
 		sb.append("\t\tif(").append(varName).append(" != null) {\n");
 		if(!withViews) {
 			sb.append("\t\t\trender(").append(varName).append(");\n");
 		} else {
 			sb.append("\t\t\tswitch(wants()) {\n");
 			sb.append("\t\t\t\tcase JS:\n");
 			sb.append("\t\t\t\tcase JSON: render(").append(varName).append(");           ").append(repeat(' ', mType.length())).append("break;\n");
 			sb.append("\t\t\t\tcase HTML: render(new Show").append(mType).append("(").append(varName).append(")); break;\n");
 			sb.append("\t\t\t}\n");
 		}
 		sb.append("\t\t}\n");
 		sb.append("\t}");
 		return sb.toString();
 	}
 
 	private String genShowAll(boolean regen) {
 		StringBuilder sb = new StringBuilder();
 		sb.append(regen ? "@Regen\n\t" : "\t");
 		sb.append("@Override // GET/URL/[models]\n");
 		sb.append("\tpublic void showAll() throws SQLException {\n");
 		sb.append("\t\tList<").append(mType).append("> ").append(varNamePlural).append(" = ").append(mType).append(".findAll();\n");
 		sb.append('\n');
 		if(!withViews) {
 			sb.append("\t\trender(").append(varNamePlural).append(");\n");
 		} else {
 			sb.append("\t\tswitch(wants()) {\n");
 			sb.append("\t\t\tcase JS:\n");
 			sb.append("\t\t\tcase JSON: render(").append(varNamePlural).append("); break;\n");;
 			sb.append("\t\t\tcase HTML: render(new ShowAll").append(mTypePlural).append("(").append(varNamePlural).append(")); break;\n");
 			sb.append("\t\t}\n");
 		}
 		sb.append("\t}");
 		return sb.toString();
 	}
 
 	private String genShowEdit(boolean regen) {
 		StringBuilder sb = new StringBuilder();
 		sb.append(regen ? "@Regen\n\t" : "\t");
 		sb.append("@Override // GET/URL/[models]/id/edit\n");
 		sb.append("\tpublic void showEdit() throws SQLException {\n");
 		sb.append("\t\t").append(mType).append(" ").append(varName).append(" = ").append(mType).append(".find(getId());\n");
 		sb.append("\t\tif(").append(varName).append(" != null) {\n");
 		sb.append("\t\t\trender(new ShowEdit").append(mType).append("(").append(varName).append("));\n");
 		sb.append("\t\t}\n");
 		sb.append("\t}");
 		return sb.toString();
 	}
 	
 	private String genShowNew(boolean regen) {
 		StringBuilder sb = new StringBuilder();
 		sb.append(regen ? "@Regen\n\t" : "\t");
 		sb.append("@Override // GET/URL/[models]/new\n");
 		sb.append("\tpublic void showNew() throws SQLException {\n");
 		sb.append("\t\t").append(mType).append(" ").append(varName).append(" = new ").append(mType).append("();\n");
 		sb.append("\t\trender(new ShowNew").append(mType).append('(').append(varName).append("));\n");
 		sb.append("\t}");
 		return sb.toString();
 	}
 	
 	private String genUpdate(boolean regen) {
 		StringBuilder sb = new StringBuilder();
 		sb.append(regen ? "@Regen\n\t" : "\t");
 		sb.append("@Override // PUT/URL/[models]/id\n");
 		sb.append("\tpublic void update() throws SQLException {\n");
 		sb.append("\t\t").append(mType).append(" ").append(varName).append(" = ").append("param(\"").append(varName).append("\", ").append(mType).append(".class).setId(getId());\n");
 		sb.append("\t\tif(").append(varName).append(".update()) {\n");
 		if(!withViews) {
 			sb.append("\t\t\trenderCreated(").append(varName).append(");\n");
 		} else {
 			sb.append("\t\t\tswitch(wants()) {\n");
 			sb.append("\t\t\t\tcase JS:\n");
 			sb.append("\t\t\t\tcase JSON: renderOK(); break;\n");
 			sb.append("\t\t\t\tcase HTML: redirectTo(").append(varName).append(", show, \"").append(titleize(mType)).append(" was successfully updated.\"); break;\n");
 			sb.append("\t\t\t}\n");
 		}
 		sb.append("\t\t} else {\n");
 		if(!withViews) {
 			sb.append("\t\t\trenderErrors(").append(varName).append(");\n");
 		} else {
 			sb.append("\t\t\tswitch(wants()) {\n");
 			sb.append("\t\t\t\tcase JS:\n");
 			sb.append("\t\t\t\tcase JSON: renderErrors(").append(varName).append("); break;\n");
 			sb.append("\t\t\t\tcase HTML: render(new ShowEdit").append(mType).append('(').append(varName).append(")); break;\n");
 			sb.append("\t\t\t}\n");
 		}
 		sb.append("\t\t}\n");
 		sb.append("\t}");
 		return sb.toString();
 	}
 	
 	private TreeSet<String> loadImports(StringBuilder sb) {
 		TreeSet<String> imports = new TreeSet<String>();
 		int stop = find(sb, 0, sb.length(), CLASS);
 		int start = find(sb, 0, stop, IMPORT);
 		while(start != -1) {
 			start += IMPORT.length;
 			while(Character.isWhitespace(sb.charAt(start)) && start < stop) {
 				start++;
 			}
 			int end = find(sb, start, stop, ';');
 			imports.add(sb.substring(start, end));
 			start = find(sb, end, stop, IMPORT);
 		}
 		return imports;
 	}
 
 	private void writeImports(StringBuilder sb, TreeSet<String> imports) {
 		if(imports.isEmpty()) {
 			return;
 		}
 		
 		int stop = find(sb, 0, sb.length(), CLASS);
 		if(stop == -1) {
 			return;
 		}
 		
 		int start = find(sb, 0, stop, PACKAGE);
 		if(start == -1) {
 			start = 0;
 		} else {
 			start = find(sb, start+1, stop, '\n') + 1;
 		}
 
 		int end = find(sb, start + IMPORT.length, stop, IMPORT);
 		if(end != -1) {
 			while(true) {
 				int i = find(sb, end + IMPORT.length, stop, IMPORT);
 				if(i != -1) {
 					end = i;
 				} else {
 					end = find(sb, end, stop, '\n') + 1;
 					break;
 				}
 			}
 		}
 		
 		TreeSet<String> statics = new TreeSet<String>();
 		for(Iterator<String> iter = imports.iterator(); iter.hasNext(); ) {
 			String imp = iter.next();
 			if(imp.startsWith("static ")) {
 				statics.add(imp);
 				iter.remove();
 			}
 		}
 		for(String imp1 : new HashSet<String>(statics)) {
 			if(imp1.endsWith(".*")) {
 				String s1 = imp1.substring(0, imp1.lastIndexOf('.'));
 				for(Iterator<String> iter = statics.iterator(); iter.hasNext(); ) {
 					String imp2 = iter.next();
 					if(imp1 != imp2) {
 						String s2 = imp2.substring(0, imp2.lastIndexOf('.'));
 						if(s1.equals(s2) || (s1 + "Model").equals(s2)) {
 							iter.remove();
 						}
 					}
 				}
 			}
 		}
 		
 		StringBuilder sb2 = new StringBuilder();
 		sb2.append('\n');
 		for(String imp : statics) {
 			sb2.append("import ").append(imp).append(";\n");
 		}
 		if(!statics.isEmpty() && !imports.isEmpty()) {
 			sb2.append('\n');
 		}
 		for(String imp : imports) {
 			sb2.append("import ").append(imp).append(";\n");
 		}
 		
 		if(end == -1) {
 			sb.insert(start, sb2.toString());
 		} else {
 			sb.replace(start, end, sb2.toString());
 		}
 	}
 
 }
