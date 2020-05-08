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
 package org.oobium.build.esp;
 
 import static org.oobium.build.esp.EspPart.Type.ConstructorElement;
 import static org.oobium.build.esp.EspPart.Type.DOM;
 import static org.oobium.build.esp.EspPart.Type.HtmlElement;
 import static org.oobium.build.esp.EspPart.Type.ImportElement;
 import static org.oobium.build.esp.EspPart.Type.InnerTextElement;
 import static org.oobium.build.esp.EspPart.Type.JavaElement;
 import static org.oobium.build.esp.EspPart.Type.StyleEntryPart;
 import static org.oobium.utils.StringUtils.blank;
 import static org.oobium.utils.StringUtils.getterName;
 import static org.oobium.utils.StringUtils.h;
 import static org.oobium.utils.StringUtils.hasserName;
 import static org.oobium.utils.StringUtils.plural;
 import static org.oobium.utils.StringUtils.titleize;
 import static org.oobium.utils.StringUtils.underscored;
 import static org.oobium.utils.StringUtils.varName;
 import static org.oobium.utils.literal.Set;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import org.oobium.app.server.controller.Action;
 import org.oobium.app.server.controller.Controller;
 import org.oobium.app.server.view.ScriptFile;
 import org.oobium.app.server.view.StyleSheet;
 import org.oobium.app.server.view.View;
 import org.oobium.build.esp.ESourceFile.EspLocation;
 import org.oobium.build.esp.ESourceFile.JavaSourcePart;
 import org.oobium.build.esp.elements.ConstructorElement;
 import org.oobium.build.esp.elements.HtmlElement;
 import org.oobium.build.esp.elements.ImportElement;
 import org.oobium.build.esp.elements.InnerTextElement;
 import org.oobium.build.esp.elements.JavaElement;
 import org.oobium.build.esp.elements.ScriptElement;
 import org.oobium.build.esp.elements.StyleChildElement;
 import org.oobium.build.esp.elements.StyleElement;
 import org.oobium.build.esp.parts.ConstructorArg;
 import org.oobium.build.esp.parts.EntryPart;
 import org.oobium.build.esp.parts.JavaPart;
 import org.oobium.build.esp.parts.StylePropertyPart;
 import org.oobium.mailer.MailerTemplate;
 import org.oobium.persist.Model;
 import org.oobium.utils.ArrayUtils;
 import org.oobium.utils.StringUtils;
 import org.oobium.utils.Utils;
 import org.oobium.utils.json.JsonUtils;
 
 public class EspCompiler {
 
 	private static final String SBNAME = "__sb__";
 
 	
 	static void appendEscaped(StringBuilder sb, String text) {
 		appendEscaped(sb, text, 0, text.length());
 	}
 	
 	static void appendEscaped(StringBuilder sb, String text, int s0, int s1) {
 		for(int j = s0; j < s1; j++) {
 			char c = text.charAt(j);
 			switch(c) {
 			case '"':	if(j == 0 || text.charAt(j-1) != '\\') { sb.append("\\\""); } break;
 			case '\t':	if(j == 0 || text.charAt(j-1) != '\\') { sb.append("\\t"); } break;
 			default:	sb.append(c); break;
 			}
 		}
 	}
 	
 	static void appendEscaped(StringBuilder sb, String text, Map<String, EntryPart> vars) {
 		if(vars == null) {
 			appendEscaped(sb, text);
 		} else {
 			StringBuilder sb0 = new StringBuilder();
 			appendEscaped(sb0, text);
 			String s = sb0.toString();
 			for(Entry<String, EntryPart> entry : vars.entrySet()) {
 				s = s.replaceAll("@"+entry.getKey(), "\").append("+entry.getValue().getValue().getText()+").append(\"");
 			}
 			sb.append(s);
 		}
 	}
 	
 	private static String getFormModelNameVar(int start) {
 		return "formModelName$" + start;
 	}
 	
 	private static String getFormModelVar(HtmlElement form) {
 		if(form.hasJavaType()) {
 			return "formModel$" + form.getStart();
 		} else {
 			String arg = form.getArg(0).getText().trim();
 			for(int i = 0; i < arg.length(); i++) {
 				if(!Character.isJavaIdentifierPart(arg.charAt(i))) {
 					return "formModel$" + form.getStart();
 				}
 			}
 			return form.getArg(0).getText().trim();
 		}
 	}
 	
 	
 	private final String pkg;
 	private final EspDom dom;
 	private ESourceFile esf;
 	private StringBuilder body;
 	private List<EspLocation> bodyLocations;
 	private StringBuilder script;
 	private List<EspLocation> scriptLocations;
 	private StringBuilder style;
 	private List<EspLocation> styleLocations;
 	private StringBuilder meta;
 	private StringBuilder title;
 	private List<EspLocation> titleLocations;
 	private boolean lastBodyIsJava;
 	private int javaBodyLevel;
 	private boolean lastScriptIsJava;
 	private int javaScriptLevel;
 	private boolean lastStyleIsJava;
 	private int javaStyleLevel;
 	private String sbName;
 	private int captureLevel;
 	
 	private String contentName;
 	
 	
 	public EspCompiler(String packageName, EspDom dom) {
 		this.pkg = packageName;
 		this.dom = dom;
 		this.sbName = SBNAME;
 		this.captureLevel = -1;
 	}
 
 	private void appendAttr(String attr, EspPart target) {
 		body.append(' ').append(attr).append('=');
 		int pos = body.length();
 		build(target, body);
 		if(pos < body.length()) {
 			if(body.charAt(pos) != '\\' || (pos+1 < body.length() && body.charAt(pos+1) != '"')) {
 				body.insert(pos, "\\\"");
 			}
 			if(body.charAt(body.length()-2) != '\\' || body.charAt(body.length()-1) != '"') {
 				body.append("\\\"");
 			}
 		} else {
 			body.append("\\\"\\\"");
 		}
 	}
 	
 	private void appendCreateJs(String target, Map<String, EntryPart> entries) {
 		String field = (entries != null && entries.containsKey("field")) ? entries.get("field").getValue().getText().trim() : null;
 		String value = (entries != null && entries.containsKey("value")) ? entries.get("value").getValue().getText().trim() : null;
 		body.append(" href=\\\"\").append(").append(target).append(").append(\"\\\"");
 		body.append(" onclick=\\\"");
 		if(entries != null && entries.containsKey("confirm")) {
			body.append("if(confirm('\").append(h(");
			build(entries.get("confirm").getValue(), body, true);
			body.append(")).append(\"')) {");
 		}
 		body.append("var f = document.createElement('form');");
 		body.append("f.style.display = 'none';");
 		body.append("this.parentNode.appendChild(f);");
 		body.append("f.method = 'POST';");
 		body.append("f.action = '\").append(").append(target).append(").append(\"';");
 		body.append("var m = document.createElement('input');");
 		body.append("m.setAttribute('type', 'hidden');");
 		body.append("m.setAttribute('name', '").append(field).append("');");
 		body.append("m.setAttribute('value', '").append(value).append("');");
 		body.append("f.appendChild(m);");
 //					sb.append("var s = document.createElement('input');");
 //					sb.append("s.setAttribute('type', 'hidden');");
 //					sb.append("s.setAttribute('name', 'authenticity_token');");
 //					sb.append("s.setAttribute('value', 'b9373bb3936620b9457ec2edc19f597b32faf6bf');");
 //					sb.append("f.appendChild(s);");
 		body.append("f.submit();");
 		if(entries != null && entries.containsKey("confirm")) {
 			body.append("}");
 		}
 		body.append("return false;\\\"");
 	}
 	
 	private void appendDeleteJs(String target, Map<String, EntryPart> entries) {
 		body.append(" href=\\\"\").append(").append(target).append(").append(\"\\\"");
 		body.append(" onclick=\\\"");
 		if(entries != null && entries.containsKey("confirm")) {
			body.append("if(confirm('\").append(h(");
			build(entries.get("confirm").getValue(), body, true);
			body.append(")).append(\"')) {");
 		}
 		body.append("var f = document.createElement('form');");
 		body.append("f.style.display = 'none';");
 		body.append("this.parentNode.appendChild(f);");
 		body.append("f.method = 'POST';");
 		body.append("f.action = '\").append(").append(target).append(").append(\"';");
 		body.append("var m = document.createElement('input');");
 		body.append("m.setAttribute('type', 'hidden');");
 		body.append("m.setAttribute('name', '_method');");
 		body.append("m.setAttribute('value', 'delete');");
 		body.append("f.appendChild(m);");
 //					sb.append("var s = document.createElement('input');");
 //					sb.append("s.setAttribute('type', 'hidden');");
 //					sb.append("s.setAttribute('name', 'authenticity_token');");
 //					sb.append("s.setAttribute('value', 'b9373bb3936620b9457ec2edc19f597b32faf6bf');");
 //					sb.append("f.appendChild(s);");
 		body.append("f.submit();");
 		if(entries != null && entries.containsKey("confirm")) {
 			body.append("}");
 		}
 		body.append("return false;\\\"");
 	}
 
 	private void appendFormFieldName(String name, List<EspPart> fields) {
 		body.append("\").append(").append(name).append(").append(\"");
 		for(EspPart field : fields) {
 			body.append('[');
 			build(field, body, true);
 			body.append(']');
 		}
 	}
 	
 	private void appendUpdateJs(String target, Map<String, EntryPart> entries) {
 		String field = (entries != null && entries.containsKey("field")) ? entries.get("field").getValue().getText().trim() : null;
 		String value = (entries != null && entries.containsKey("value")) ? entries.get("value").getValue().getText().trim() : null;
 		body.append(" href=\\\"\").append(").append(target).append(").append(\"\\\"");
 		body.append(" onclick=\\\"");
 		if(entries != null && entries.containsKey("confirm")) {
			body.append("if(confirm('\").append(h(");
			build(entries.get("confirm").getValue(), body, true);
			body.append(")).append(\"')) {");
 		}
 		body.append("var f = document.createElement('form');");
 		body.append("f.style.display = 'none';");
 		body.append("this.parentNode.appendChild(f);");
 		body.append("f.method = 'POST';");
 		body.append("f.action = '\").append(").append(target).append(").append(\"';");
 		body.append("var m = document.createElement('input');");
 		body.append("m.setAttribute('type', 'hidden');");
 		body.append("m.setAttribute('name', '_method');");
 		body.append("m.setAttribute('value', 'put');");
 		body.append("f.appendChild(m);");
 		body.append("var m = document.createElement('input');");
 		body.append("m.setAttribute('type', 'hidden');");
 		body.append("m.setAttribute('name', '").append(field).append("');");
 		body.append("m.setAttribute('value', '").append(value).append("');");
 		body.append("f.appendChild(m);");
 //					sb.append("var s = document.createElement('input');");
 //					sb.append("s.setAttribute('type', 'hidden');");
 //					sb.append("s.setAttribute('name', 'authenticity_token');");
 //					sb.append("s.setAttribute('value', 'b9373bb3936620b9457ec2edc19f597b32faf6bf');");
 //					sb.append("f.appendChild(s);");
 		body.append("f.submit();");
 		if(entries != null && entries.containsKey("confirm")) {
 			body.append("}");
 		}
 		body.append("return false;\\\"");
 	}
 	
 	private void appendValueGetter(String model, List<EspPart> fields) {
 		if(fields.size() == 1) {
 			body.append(model).append('.').append(getterName(fields.get(0).getText())).append('(').append(')');
 		} else {
 			int last = fields.size() - 1;
 			for(int i = 0; i < fields.size(); i++) {
 				body.append(model);
 				for(int j = 0; j <= i; j++) {
 					body.append('.').append((j == i && j != last) ? hasserName(fields.get(j).getText()) : getterName(fields.get(j).getText())).append("()");
 				}
 				if(i < last) {
 					body.append("?(");
 				}
 			}
 			for(int i = 0; i < last; i++) {
 				body.append("):\"\"");
 			}
 		}
 	}
 	
 	private void build(EspPart part, StringBuilder sb) {
 		build(part, sb, false);
 	}
 	
 	private void build(EspPart part, StringBuilder sb, boolean isArg) {
 		if(part == null) {
 			return; // occurs when part is supposed to be the value of an entry part, but it has not been created yet (key:)
 		}
 		String text = part.getText();
 //		if(!isArg && text.startsWith("\"") && text.endsWith("\"")) {
 //			text = text.substring(1, text.length()-1);
 //		}
 		if(part.isA(StyleEntryPart)) {
 			appendEscaped(sb, text);
 		} else if(part.hasParts()) {
 			List<EspPart> parts = part.getParts();
 			for(int i = 0; i < parts.size(); i++) {
 				if(parts.get(i) instanceof JavaPart) {
 					JavaPart jpart = (JavaPart) parts.get(i);
 					int s1 = jpart.getStart() - part.getStart();
 					int s2 = s1 + jpart.getLength();
 					if(i == 0) {
 						if(s1 > 0) {
 							appendEscaped(sb, text, 0, s1);
 						}
 					} else {
 						int s0 = parts.get(i-1).getEnd();
 						if(s0 < s1) {
 							appendEscaped(sb, text, s0, s1);
 						}
 					}
 					sb.append("\").append(");
 					if(jpart.isEscaped()) {
 						sb.append(jpart.getEscapeChar()).append('(');
 					}
 					if(sb == body) {
 						EspPart spart = jpart.getSourcePart();
 						if(spart != null) {
 							bodyLocations.add(new EspLocation(sb.length(), spart));
 						}
 					} else if(sb == title) {
 						EspPart spart = jpart.getSourcePart();
 						if(spart != null) {
 							titleLocations.add(new EspLocation(sb.length(), spart));
 						}
 					}
 					sb.append(jpart.getSource());
 					if(jpart.isEscaped()) {
 						sb.append(')');
 					}
 					sb.append(").append(\"");
 					if(i == parts.size() - 1) {
 						if(s2 < text.length()) {
 							appendEscaped(sb, text, s2, text.length());
 						}
 					}
 				} else {
 					build(parts.get(i), sb);
 				}
 			}
 		} else {
 			if(isArg) {
 				sb.append(text);
 			} else {
 				appendEscaped(sb, text);
 			}
 		}
 	}
 	
 	private void buildAttrs(HtmlElement element, String...skip) {
 		Set<String> skipSet = Set(skip);
 		if(element.hasEntries()) {
 			Map<String, EntryPart> entries = element.getEntries();
 			for(EntryPart entry : entries.values()) {
 				String key = entry.getKey().getText().trim();
 				if(!skipSet.contains(key)) {
 					EspPart value = entry.getValue();
 					if(value != null) {
 						body.append(' ');
 						body.append(key);
 						body.append("=");
 						int pos = body.length();
 						build(value, body);
 						if(pos < body.length()) {
 							if((pos < body.length() && body.charAt(pos) != '\\') || (pos+1 < body.length() && body.charAt(pos+1) != '"')) {
 								body.insert(pos, "\\\"");
 							}
 							if(element.isHidden() && "style".equals(key)) {
 								if(body.charAt(body.length()-1) == '"') {
 									body.delete(body.length()-2, body.length());
 								}
 								body.append(";display:none\\\"");
 							} else {
 								if(body.charAt(body.length()-1) != '"') {
 									body.append("\\\"");
 								}
 							}
 						} else {
 							if(element.isHidden() && "style".equals(key)) {
 								body.append(";display:none\\\"");
 							} else {
 								body.append("\\\"\\\"");
 							}
 						}
 					}
 				}
 			}
 			if(element.isHidden() && !entries.containsKey("style")) {
 				body.append(" style=\\\"display:none\\\"");
 			}
 		} else if(element.isHidden()) {
 			body.append(" style=\\\"display:none\\\"");
 		}
 	}
 
 	private void buildCheck(HtmlElement check) {
 		if(check.hasArgs()) {
 			List<EspPart> fields = check.getArgs();
 			String modelName = getFormModelName(check);
 			
 			body.append("<input type=\\\"hidden\\\" name=\\\"");
 			if(check.hasEntry("name")) {
 				build(check.getEntry("name").getValue(), body);
 			} else {
 				appendFormFieldName(modelName, fields);
 			}
 			body.append("\\\" value=\\\"false\\\" />");
 
 			body.append("<input type=\\\"checkbox\\\"");
 			if(!blank(modelName)) {
 				body.append(" id=\\\"");
 				if(check.hasId()) {
 					build(check.getId(), body);
 				} else if(check.hasEntry("id")) {
 					build(check.getEntry("id").getValue(), body);
 				} else {
 					appendFormFieldName(modelName, fields);
 				}
 				body.append("\\\"");
 				buildClasses(check);
 				body.append(" name=\\\"");
 				if(check.hasEntry("name")) {
 					build(check.getEntry("name").getValue(), body);
 				} else {
 					appendFormFieldName(modelName, fields);
 				}
 				body.append("\\\" value=\\\"true\\\"");
 				buildAttrs(check, "id", "name", "value", "type");
 				body.append("\");\n");
 				indent(body);
 				body.append("if(");
 				appendValueGetter(getFormModel(check), fields);
 				body.append(") {\n");
 				javaBodyLevel++;
 				indent(body);
 				body.append(sbName).append(".append(\" CHECKED\");\n");
 				javaBodyLevel--;
 				indent(body);
 				body.append("}\n");
 				indent(body);
 				body.append(sbName).append(".append(\"");
 			}
 		} else {
 			body.append("<input type=\\\"checkbox\\\"");
 			buildId(check);
 			buildClasses(check);
 			buildAttrs(check, "type");
 		}
 		body.append(" />");
 	}
 	
 	private void buildClasses(HtmlElement element) {
 		if(element.hasClassNames()) {
 			body.append(" class=\\\"");
 			for(Iterator<EspPart> ci = element.getClassNames().iterator(); ci.hasNext(); ) {
 				build(ci.next(), body);
 				if(ci.hasNext()) body.append(' ');
 			}
 			body.append("\\\"");
 		}
 	}
 	
 	private void buildConstructor(ConstructorElement element) {
 		StringBuilder sb;
 		List<ConstructorArg> args = element.hasArgs() ? element.getArgs() : new ArrayList<ConstructorArg>(0);
 		for(int i = 0; i < args.size(); i++) {
 			if(args.get(i).hasDefaultValue()) {
 				sb = new StringBuilder();
 				sb.append("\tpublic ").append(dom.getName()).append('(');
 				for(int j = 0; j < i; j++) {
 					ConstructorArg arg = args.get(j);
 					String declaration = arg.getVarType() + " " + arg.getVarName();
 					esf.addVariable(arg.getVarName(), declaration);
 					if(j != 0) sb.append(", ");
 					sb.append(declaration);
 				}
 				sb.append(") {\n");
 				for(int j = 0; j < args.size(); j++) {
 					ConstructorArg arg = args.get(j);
 					sb.append("\t\tthis.").append(arg.getVarName()).append(" = ");
 					if(j < i) {
 						sb.append(arg.getVarName());
 					} else {
 						sb.append(arg.getDefaultValue());
 					}
 					sb.append(";\n");
 				}
 				sb.append("\t}");
 				esf.addConstructor(new JavaSourcePart(sb.toString()));
 			}
 		}
 
 		List<EspLocation> locations = new ArrayList<EspLocation>();
 		sb = new StringBuilder();
 		sb.append("\tpublic ");
 		locations.add(new EspLocation(sb.length(), element));
 		sb.append(dom.getName()).append('(');
 		for(int j = 0; j < args.size(); j++) {
 			ConstructorArg arg = args.get(j);
 			if(j != 0) sb.append(", ");
 			int start = sb.length();
 			locations.add(new EspLocation(sb.length(), arg));
 			if(arg.hasVarType()) {
 				locations.add(new EspLocation(sb.length(), arg.getVarTypePart()));
 				sb.append(arg.getVarType());
 			}
 			if(arg.isVarArgs()) {
 				sb.append('.').append('.').append('.');
 			} else {
 				sb.append(' ');
 			}
 			if(arg.hasVarName()) {
 				locations.add(new EspLocation(sb.length(), arg.getVarNamePart()));
 				sb.append(arg.getVarName());
 				esf.addVariable(arg.getVarName(), new String(sb.substring(start, sb.length())));
 			}
 		}
 		sb.append(") {\n");
 		for(int j = 0; j < args.size(); j++) {
 			ConstructorArg arg = args.get(j);
 			sb.append("\t\tthis.").append(arg.getVarName()).append(" = ");
 			if(j <= args.size()) {
 				sb.append(arg.getVarName());
 			} else {
 				sb.append(arg.getDefaultValue());
 			}
 			sb.append(";\n");
 		}
 		sb.append("\t}");
 		esf.addConstructor(new JavaSourcePart(sb.toString(), locations));
 	}
 	
 	private void buildDateInputs(HtmlElement date) {
 		body.append("<span");
 		buildFormField(date, false);
 		body.append(">\");\n");
 		if(date.hasArgs()) {
 			String model = getFormModel(date);
 			if(!blank(model)) {
 				String var = "selection$" + date.getStart();
 				List<EspPart> fields = date.getArgs();
 				indent(body);
 				body.append("java.util.Date ").append(var).append(" = ");
 				appendValueGetter(model, fields);
 				body.append(";\n");
 				indent(body);
 				body.append(sbName).append(".append(dateTimeTags(\"");
 				appendFormFieldName(model, fields);
 				body.append("\", \"");
 				if(date.hasEntry("format")) {
 					build(date.getEntry("format"), body, true);
 				} else {
 					body.append("MM/dd/yyyy");
 				}
 				body.append("\", ").append(var).append("));\n");
 			}
 		} else {
 			indent(body);
 			body.append(sbName).append(".append(dateTimeTags(\"MMM/dd/yyyy\"));\n");
 		}
 		indent(body);
 		body.append(sbName).append(".append(\"</span>");
 	}
 	
 	private void buildElement(EspElement element) {
 		switch(element.getType()) {
 		case HtmlElement:
 			buildHtml((HtmlElement) element);
 			break;
 		case ScriptElement:
 			buildScript((ScriptElement) element);
 			break;
 		case StyleElement:
 			buildStyle((StyleElement) element);
 			break;
 		case YieldElement:
 			buildYield((HtmlElement) element);
 			lastBodyIsJava = true;
 			break;
 		}
 	}
 
 	private void buildEspMethods() {
 		buildMethod(
 				body,
 				"doRender",
 				"\t@Override\n\tpublic void doRenderBody(StringBuilder " + SBNAME + ") throws Exception {\n",
 				bodyLocations,
 				lastBodyIsJava
 		);
 
 		buildMethod(
 				script,
 				"doRenderScript",
 				"\t@Override\n\tprotected void doRenderScript(StringBuilder " + SBNAME + ") {\n",
 				scriptLocations,
 				lastScriptIsJava
 		);
 		if(script.length() > 0) {
 			esf.addMethod("hasScript", "\t@Override\n\tpublic boolean hasScript() {\n\t\treturn true;\n\t}");
 		}
 
 		buildMethod(
 				style,
 				"doRenderStyle",
 				"\t@Override\n\tprotected void doRenderStyle(StringBuilder " + SBNAME + ") {\n",
 				styleLocations,
 				lastStyleIsJava
 		);
 		if(style.length() > 0) {
 			esf.addMethod("hasStyle", "\t@Override\n\tpublic boolean hasStyle() {\n\t\treturn true;\n\t}");
 		}
 
 		if(meta.length() > 0) {
 			meta.insert(0, "\t@Override\n\tprotected void doRenderMeta(StringBuilder " + SBNAME + ") {\n\t\t" + SBNAME + ".append(\"");
 			meta.append("\");\n\t}");
 			esf.addMethod("doRenderMeta", meta.toString());
 			esf.addMethod("hasMeta", "\t@Override\n\tpublic boolean hasMeta() {\n\t\treturn true;\n\t}");
 		}
 
 		if(title.length() > 0) {
 			String s0 = "\t@Override\n\tprotected void doRenderTitle(StringBuilder " + SBNAME + ") {\n\t\t" + SBNAME + ".append(\"";
 			title.insert(0, s0);
 			title.append("\");\n\t}");
 			for(EspLocation location : titleLocations) {
 				location.offset += s0.length();
 			}
 			esf.addMethod("doRenderTitle", title.toString(), titleLocations);
 			esf.addMethod("hasTitle", "\t@Override\n\tpublic boolean hasTitle() {\n\t\treturn true;\n\t}");
 		}
 	}
 	
 	private void buildExternalEjs(StringBuilder sb, String type, ScriptElement element) {
 		prepForJava(sb);
 		int pos = element.getStart();
 		sb.append("String path$").append(pos).append(" = underscored(").append(type).append(".class.getName()).replaceAll(\"\\\\.\", \"/\");\n");
 		prepForMarkup(sb);
 		sb.append("<script src='/\").append(path$").append(pos).append(").append(\".js'></script>");
 	}
 	
 	private void buildExternalEss(StringBuilder sb, String type, StyleElement element) {
 		prepForJava(sb);
 		int pos = element.getStart();
 		sb.append("String path$").append(pos).append(" = underscored(").append(type).append(".class.getName()).replaceAll(\"\\\\.\", \"/\");\n");
 		prepForMarkup(sb);
 		sb.append("<link rel='stylesheet' type='text/css' href='/\").append(path$").append(pos).append(").append(\".css' />");
 	}
 	
 	private void buildFields(HtmlElement fields) {
 		List<EspPart> args = fields.getArgs();
 		if(args != null && args.size() == 1) {
 			String type = fields.hasJavaType() ? fields.getJavaType() : "Model";
 			String model = getFormModelVar(fields);
 			String modelVal = args.get(0).getText().trim();
 			String modelName = getFormModelNameVar(fields.getStart());
 
 			prepForJava(body);
 			if(!model.equals(modelVal)) {
 				if(type.equals("Model")) {
 					esf.addImport(Model.class.getCanonicalName());
 				}
 				body.append(type).append(' ').append(model).append(" = ").append(modelVal).append(";\n");
 				indent(body);
 			}
 			body.append("String ").append(modelName).append(" = ").append(getFormModelNameValue(fields)).append(";\n");
 			prepForMarkup(body);
 
 			body.append("<input type=\\\"hidden\\\" name=\\\"\").append(").append(modelName).append(").append(\"[id]\\\"");
 			body.append(" value=\\\"\").append(").append(model).append(".getId()).append(\"").append("\\\" />");
 		}
 	}
 	
 	private void buildForm(HtmlElement form) {
 		List<EspPart> args = form.getArgs();
 		if(args != null && (args.size() == 1 || args.size() == 2)) {
 			String type = form.hasJavaType() ? form.getJavaType() : "Model";
 			String model = getFormModelVar(form);
 			String modelVal = args.get(0).getText().trim();
 			String modelName = getFormModelNameVar(form.getStart());
 
 			prepForJava(body);
 			if(!model.equals(modelVal)) {
 				if(type.equals("Model")) {
 					esf.addImport(Model.class.getCanonicalName());
 				}
 				body.append(type).append(' ').append(model).append(" = ").append(modelVal).append(";\n");
 				indent(body);
 			}
 			body.append("String ").append(modelName).append(" = ").append(getFormModelNameValue(form)).append(";\n");
 			prepForMarkup(body);
 			
 			body.append('<').append(form.getTag());
 			buildId(form);
 			buildClasses(form);
 			buildAttrs(form, "action", "method");
 
 			String method = null;
 			
 			if(args.size() == 2) {
 				String action = args.get(1).getText().trim();
 				if("create".equalsIgnoreCase(action)) {
 					method = "post";
 					body.append(" action=\\\"\").append(pathTo(").append(model).append(", create)).append(\"\\\"");
 				} else if("update".equalsIgnoreCase(action)) {
 					method = "put";
 					body.append(" action=\\\"\").append(pathTo(").append(model).append(", update)).append(\"\\\"");
 				} else if("delete".equalsIgnoreCase(action)) {
 					method = "delete";
 					body.append(" action=\\\"\").append(pathTo(").append(model).append(", destroy)).append(\"\\\"");
 				} else {
 					method = "post";
 					body.append(" action=\\\"");
 					build(form.getEntry("action").getValue(), body);
 					body.append("\\\"");
 				}
 			}
 			if(method == null && form.hasEntry("method")) {
 				EspPart value = form.getEntry("method").getValue();
 				if(value != null) {
 					method = value.getText();
 					if(form.hasEntry("action")) {
 						body.append(" action=\\\"");
 						build(form.getEntry("action").getValue(), body);
 						body.append("\\\"");
 					} else {
 						if("post".equalsIgnoreCase(method)) {
 							body.append(" action=\\\"\").append(pathTo(").append(model).append(", create)).append(\"\\\"");
 						} else if("put".equalsIgnoreCase(method)) {
 							body.append(" action=\\\"\").append(pathTo(").append(model).append(", update)).append(\"\\\"");
 						} else if("delete".equalsIgnoreCase(method)) {
 							body.append(" action=\\\"\").append(pathTo(").append(model).append(", destroy)).append(\"\\\"");
 						} else {
 							body.append(" action=\\\"\").append(pathTo(").append(model).append(", create)).append(\"\\\"");
 						}
 					}
 				}
 			}
 			if(method == null && form.hasEntry("action")) {
 				EspPart value = form.getEntry("action").getValue();
 				if(value != null) {
 					String action = value.getText();
 					if("create".equalsIgnoreCase(action)) {
 						method = "post";
 						body.append(" action=\\\"\").append(pathTo(").append(model).append(", create)).append(\"\\\"");
 					} else if("update".equalsIgnoreCase(action)) {
 						method = "put";
 						body.append(" action=\\\"\").append(pathTo(").append(model).append(", update)).append(\"\\\"");
 					} else if("delete".equalsIgnoreCase(action)) {
 						method = "delete";
 						body.append(" action=\\\"\").append(pathTo(").append(model).append(", destroy)).append(\"\\\"");
 					} else {
 						method = "post";
 						body.append(" action=\\\"");
 						build(form.getEntry("action").getValue(), body);
 						body.append("\\\"");
 					}
 				}
 			}
 			if(method == null) {
 				body.append(" action=\\\"\").append(pathTo(").append(model).append(", ").append(model).append(".isNew() ? create : update)).append(\"\\\"");
 			}
 			body.append(" method=\\\"POST\\\"");
 			if(form.hasEntry("enctype")) {
 				body.append(" enctype=\\\"");
 				build(form.getEntry("enctype").getValue(), body);
 				body.append("\\\"");
 			} else {
 				if(containsFileInput(form)) {
 					body.append(" enctype=\\\"multipart/form-data\\\"");
 				}
 			}
 			body.append('>');
 			if(method == null) {
 				prepForJava(body);
 				body.append("if(!").append(model).append(".isNew()) {\n");
 				indent(body);
 				body.append('\t').append(sbName).append(".append(\"<input type=\\\"hidden\\\" name=\\\"_method\\\" value=\\\"PUT\\\" />\");\n");
 				indent(body);
 				body.append("}\n");
 				prepForMarkup(body);
 			} else if(method.equalsIgnoreCase("put")) {
 				body.append("<input type=\\\"hidden\\\" name=\\\"_method\\\" value=\\\"PUT\\\" />");
 			} else if(method.equalsIgnoreCase("delete")) {
 				body.append("<input type=\\\"hidden\\\" name=\\\"_method\\\" value=\\\"DELETE\\\" />");
 			}
 			body.append("<input type=\\\"hidden\\\" name=\\\"\").append(").append(modelName).append(").append(\"[id]\\\"");
 			body.append(" value=\\\"\").append(").append(model).append(".getId()).append(\"").append("\\\" />");
 		} else {
 			body.append('<').append(form.getTag());
 			buildId(form);
 			buildClasses(form);
 			buildAttrs(form);
 			body.append('>');
 		}
 	}
 	
 	private void buildFormField(HtmlElement input, boolean hasValue) {
 		if(input.hasArgs()) {
 			List<EspPart> fields = input.getArgs();
 			String modelName = getFormModelName(input);
 			if(!blank(modelName)) {
 				body.append(" id=\\\"");
 				if(input.hasId()) {
 					build(input.getId(), body);
 				} else if(input.hasEntry("id")) {
 					build(input.getEntry("id").getValue(), body);
 				} else {
 					appendFormFieldName(modelName, fields);
 				}
 				body.append("\\\"");
 				buildClasses(input);
 				body.append(" name=\\\"");
 				if(input.hasEntry("name")) {
 					build(input.getEntry("name").getValue(), body);
 				} else {
 					appendFormFieldName(modelName, fields);
 				}
 				body.append("\\\"");
 				if(hasValue) {
 					if(input.hasEntry("value")) {
 						body.append(" value=");
 						int pos = body.length();
 						build(input.getEntry("value").getValue(), body);
 						if(pos < body.length()) {
 							if(body.charAt(pos) != '\\' || (pos+1 < body.length() && body.charAt(pos+1) != '"')) {
 								body.insert(pos, "\\\"");
 							}
 							if(body.charAt(body.length()-1) != '"') {
 								body.append("\\\"");
 							}
 						} else {
 							body.append("\\\"\\\"");
 						}
 					} else {
 						body.append(" value=\\\"\").append(f(");
 						appendValueGetter(getFormModel(input), fields);
 						body.append(")).append(\"\\\"");
 					}
 				}
 				buildAttrs(input, "id", "name", "value", "type");
 			}
 		} else {
 			buildId(input);
 			buildClasses(input);
 			buildAttrs(input, "type");
 		}
 	}
 	
 	private void buildHtml(HtmlElement element) {
 		String tag = element.getTag();
 		if("view".equals(tag)) {
 			buildView(element);
 		} else if("options".equals(tag)) {
 			buildSelectOptions(element);
 		} else if("label".equals(tag)) {
 			buildLabel(element);
 		} else if("messages".equals(tag)) {
 			buildMessages(element);
 		} else if("capture".equals(tag)) {
 			startCapture(element);
 		} else if("contentFor".equals(tag)) {
 			startContent(element);
 		} else {
 			prepForMarkup(body);
 			if("a".equals(tag) || "link".equals(tag) || "button".equals(tag)) {
 				buildLink(element);
 			} else if("img".equals(tag)) {
 				buildImage(element);
 			} else if("number".equals(tag)) {
 				buildNumber(element);
 			} else if("form".equals(tag)) {
 				buildForm(element);
 			} else if("fields".equals(tag)) {
 				buildFields(element);
 			} else if("date".equals(tag)) {
 				buildDateInputs(element);
 			} else if("textarea".equals(tag)) {
 				buildTextArea(element);
 			} else if("check".equals(tag)) {
 				buildCheck(element);
 			} else if("radio".equals(tag) || "file".equals(tag) || "input".equals(tag) || "hidden".equals(tag) || "number".equals(tag) || "password".equals(tag) || "text".equals(tag) || "textarea".equals(tag)) {
 				buildInput(tag, element);
 			} else if("select".equals(tag)) {
 				buildSelect(element);
 			} else if("submit".equals(tag)) {
 				buildSubmit(element);
 			} else if("reset".equals(tag)) {
 				buildResetInput(element);
 			} else if("title".equals(tag)) {
 				buildTitle(element);
 			} else if(!"head".equals(tag)) {
 				body.append('<').append(tag);
 				buildId(element);
 				buildClasses(element);
 				buildAttrs(element);
 				body.append('>');
 				if(element.hasInnerText()) {
 					build(element.getInnerText(), body);
 				}
 			}
 			lastBodyIsJava = false;
 		}
 
 		if(element.hasChildren()) {
 			for(EspElement child : element.getChildren()) {
 				if(child.isElementA(JavaElement)) {
 					buildJava((JavaElement) child, true);
 				} else if(child.isElementA(InnerTextElement)) {
 					InnerTextElement innerText = (InnerTextElement) child;
 					if(innerText.hasInnerText()) {
 						if(innerText.isWordGroup()) {
 							if(body.length() > 0 && !Character.isWhitespace(body.charAt(body.length()-1))) {
 								body.append(' ');
 							}
 							appendEscaped(body, innerText.getInnerText().getText());
 						} else if(innerText.isPromptLine()) {
 							appendEscaped(body, h(innerText.getInnerText().getText()));
 							if(body.length() > 1 && (body.charAt(body.length()-2) != '\\' || body.charAt(body.length()-1) != 'n')) {
 								body.append('\\').append('n');
 							}
 						} else if(innerText.isLiteral()) {
 							appendEscaped(body, innerText.getInnerText().getText());
 						} else {
 							build(innerText.getInnerText(), body);
 						}
 					} else if(innerText.isPromptLine()) {
 						body.append('\\').append('n');
 					}
 				} else {
 					buildElement(child);
 				}
 			}
 		}
 		
 		if(element.hasClosingTag()) {
 			if(lastBodyIsJava) {
 				indent(body);
 				body.append(sbName).append(".append(\"");
 			}
 			body.append('<').append('/').append(element.getTag()).append('>');
 			lastBodyIsJava = false;
 		}
 	}
 
 	private void buildId(HtmlElement element) {
 		if(element.hasId()) {
 			body.append(" id=\\\"");
 			build(element.getId(), body);
 			body.append("\\\"");
 		}
 	}
 	
 	private void buildImage(HtmlElement image) {
 		body.append("<img");
 		buildId(image);
 		buildClasses(image);
 		buildAttrs(image, "src");
 		if(image.hasEntry("src")) {
 			appendAttr("src", image.getEntryValue("src"));
 		} else if(image.hasArgs()) {
 			appendAttr("src", image.getArgs().get(0));
 		}
 		body.append('>');
 		if(image.hasInnerText()) {
 			build(image.getInnerText(), body);
 		}
 	}
 	
 	private void buildImport(ImportElement element) {
 		if(element.hasImport()) {
 			if(element.isStatic()) {
 				esf.addStaticImport(new JavaSourcePart(element.getImport(), element.getImportPart()));
 			} else {
 				esf.addImport(new JavaSourcePart(element.getImport(), element.getImportPart()));
 			}
 		}
 	}
 
 	private void buildInlineDynamicAsset(StringBuilder sb, String type, int start, List<EspPart> args) {
 		String varName = varName(type) + "$" + start;
 		sb.append(type).append(' ').append(varName).append(" = new ").append(type);
 		if(args != null && !args.isEmpty()) {
 			sb.append("(");
 			for(Iterator<EspPart> iter = args.iterator(); iter.hasNext(); ) {
 				build(iter.next(), sb, true);
 				if(iter.hasNext()) sb.append(',').append(' ');
 			}
 			sb.append(");\n");
 		} else {
 			sb.append("();\n");
 		}
 		indent(sb);
 		sb.append(varName).append(".render(" + SBNAME + ");\n");
 	}
 
 	private void buildInlineEjs(StringBuilder sb, String type, ScriptElement element) {
 		prepForMarkup(sb);
 		sb.append("<script>");
 		prepForJava(sb);
 		buildInlineDynamicAsset(sb, type, element.getStart(), element.getArgs());
 		prepForMarkup(sb);
 		sb.append("</script>");
 	}
 
 	private void buildInlineEss(StringBuilder sb, String type, StyleElement element) {
 		prepForMarkup(sb);
 		sb.append("<style>");
 		prepForJava(sb);
 		buildInlineDynamicAsset(sb, type, element.getStart(), element.getArgs());
 		prepForMarkup(sb);
 		sb.append("</style>");
 	}
 	
 	private void buildInput(String tag, HtmlElement input) {
 		if(input.hasEntry("type")) {
 			body.append("<input type=\\\"");
 			build(input.getEntry("type").getValue(), body);
 			body.append("\\\"");
 		} else {
 			body.append("<input");
 			if("hidden".equals(tag))		body.append(" type=\\\"hidden\\\"");
 			else if("radio".equals(tag))	body.append(" type=\\\"radio\\\"");
 			else if("file".equals(tag))		body.append(" type=\\\"file\\\"");
 			else if("number".equals(tag))	body.append(" type=\\\"text\\\"");
 			else if("password".equals(tag))	body.append(" type=\\\"password\\\"");
 			else if("text".equals(tag))		body.append(" type=\\\"text\\\"");
 		}
 		buildFormField(input, true);
 		body.append(" />");
 	}
 	
 	private void buildJava(JavaElement element, boolean isStart) {
 		EspPart source = element.getSourcePart();
 		if(source != null) {
 			prepForJava(body);
 			bodyLocations.add(new EspLocation(body.length(), element.getSourcePart()));
 			body.append(source.getText()).append("\n");
 			lastBodyIsJava = true;
 		}
 
 		if(element.hasChildren()) {
 			javaBodyLevel++;
 			for(EspElement child : element.getChildren()) {
 				if(child.isElementA(JavaElement)) {
 					buildJava((JavaElement) child, true);
 				} else {
 					buildElement(child);
 				}
 			}
 			javaBodyLevel--;
 		}
 
 		if(!lastBodyIsJava) {
 			body.append("\");\n");
 			lastBodyIsJava = true;
 		}
 	}
 
 	private void buildLabel(HtmlElement label) {
 		prepForMarkup(body);
 		lastBodyIsJava = false; // set true below if necessary
 		body.append("<label");
 		buildId(label);
 		buildClasses(label);
 		if(label.hasArgs()) {
 			List<EspPart> fields = label.getArgs();
 			String modelName = getFormModelName(label);
 			if(!blank(modelName)) {
 				buildAttrs(label, "for", "text");
 				body.append(" for=\\\"");
 				if(label.hasEntry("for")) {
 					build(label.getEntry("for").getValue(), body);
 				} else {
 					appendFormFieldName(modelName, fields);
 				}
 				body.append("\\\"");
 				
 				body.append('>');
 				if(label.hasEntry("text")) {
 					int pos = body.length();
 					build(label.getEntry("text").getValue(), body);
 					if(pos < body.length()) {
 						if(body.charAt(pos) == '\\' && pos+1 < body.length() && body.charAt(pos+1) == '"') {
 							body.delete(pos, pos+2);
 						}
 						if(body.charAt(body.length()-2) == '\\' && body.charAt(body.length()-1) == '"') {
 							body.delete(body.length()-2, body.length());
 						}
 					}
 				} else {
 					body.append(titleize(fields.get(fields.size()-1).getText()));
 				}
 				if(label.hasInnerText()) {
 					build(label.getInnerText(), body);
 				}
 				
 				StringBuilder required = null;
 				if(label.hasEntry("required")) {
 					required = new StringBuilder();
 					build(label.getEntry("required").getValue(), required);
 				} else if(fields.size() == 1) {
 					required = new StringBuilder();
 					required.append(getFormModel(label)).append(".isRequired(\"");
 					build(fields.get(0), required);
 					required.append("\")");
 				}
 				if(required != null) {
 					String var = "required$" + label.getStart();
 					body.append("\");\n");
 					indent(body);
 					body.append("boolean ").append(var).append(" = ").append(required).append(";\n");
 					indent(body);
 					body.append("if(").append(var).append(") {\n");
 					if(label.hasEntry("requiredClass")) {
 						indent(body);
 						body.append('\t').append(SBNAME).append(".append(\"<span class=\\\"");
 						build(label.getEntry("requiredClass").getValue(), body);
 						body.append("\\\">*</span>\");\n");
 					} else {
 						indent(body);
 						body.append('\t').append(SBNAME).append(".append(\"<span class=\\\"required\\\">*</span>\");\n");
 					}
 					indent(body);
 					body.append("}\n");
 					lastBodyIsJava = true;
 				}
 			}
 		} else {
 			buildAttrs(label);
 			body.append('>');
 			if(label.hasInnerText()) {
 				build(label.getInnerText(), body);
 			}
 		}
 	}
 	
 	private void buildLink(HtmlElement link) {
 		body.append('<').append(link.getTag());
 		buildId(link);
 		buildClasses(link);
 		EspPart target = null;
 		String action = null;
 		if(link.hasArgs() && link.getArgs().size() <= 2) {
 			buildAttrs(link, "href", "action", "confirm", "update", "field", "value");
 			target = link.getArg(0);
 			if(link.getArgs().size() == 1) {
 				if(dom.isEsp()) {
 					action = link.hasEntry("action") ? link.getEntry("action").getValue().getText().trim() : null;
 					if("create".equals(action)) {
 						appendCreateJs(target.getText().trim(), link.getEntries());
 					} else if("update".equals(action)) {
 						appendUpdateJs(target.getText().trim(), link.getEntries());
 					} else if("destroy".equals(action)) {
 						appendDeleteJs(target.getText().trim(), link.getEntries());
 					} else {
 						String method = link.hasEntry("method") ? link.getEntry("method").getValue().getText().trim().toLowerCase() : null;
 						if("post".equals(method)) {
 							appendCreateJs(target.getText().trim(), link.getEntries());
 						} else if("put".equals(method)) {
 							appendUpdateJs(target.getText().trim(), link.getEntries());
 						} else if("delete".equals(method) || "destroy".equals(action)) {
 							appendDeleteJs(target.getText().trim(), link.getEntries());
 						} else {
 							appendAttr("href", target);
 						}
 					}
 				} else {
 					appendAttr("href", target);
 				}
 			} else { // size == 2
 				if(dom.isEsp()) {
 					action = dom.isEsp() ? link.getArg(1).getText().trim() : null;
 					if("create".equals(action)) {
 						appendCreateJs("pathTo(" + target.getText().trim() + ", create)", link.getEntries());
 					} else if("update".equals(action)) {
 						appendUpdateJs("pathTo(" + target.getText().trim() + ", update)", link.getEntries());
 					} else if("destroy".equals(action)) {
 						appendDeleteJs("pathTo(" + target.getText().trim() + ", destroy)", link.getEntries());
 					} else if("show".equals(action)) {
 						body.append(" href=\\\"\").append(").append("pathTo(").append(target.getText().trim()).append(", show)").append(").append(\"\\\"");
 					} else if("showAll".equals(action)) {
 						body.append(" href=\\\"\").append(").append("pathTo(").append(target.getText().trim()).append(", showAll)").append(").append(\"\\\"");
 					} else if("showEdit".equals(action)) {
 						body.append(" href=\\\"\").append(").append("pathTo(").append(target.getText().trim()).append(", showEdit)").append(").append(\"\\\"");
 					} else if("showNew".equals(action)) {
 						body.append(" href=\\\"\").append(").append("pathTo(").append(target.getText().trim()).append(", showNew)").append(").append(\"\\\"");
 					} else {
 						body.append(" href=\\\"\").append(").append("pathTo(").append(target.getText().trim()).append(", ").append(action).append(')').append(").append(\"\\\"");
 					}
 				}
 			}
 		} else {
 			buildAttrs(link);
 		}
 		body.append('>');
 
 		if(link.hasInnerText()) {
 			build(link.getInnerText(), body);
 		} else if(target != null && !link.hasChildren()) {
 			if(action != null) {
 				if(action.length() > 4 && action.startsWith("show")) {
 					body.append(titleize(action.substring(4))).append(' ');
 				} else {
 					body.append(titleize(action)).append(' ');
 				}
 				String txt = target.getText();
 				if(txt.endsWith(".class")) {
 					txt = plural(txt.substring(0, txt.length()-6));
 				}
 				body.append(titleize(txt));
 			} else {
 				String txt = target.getText();
 				if(txt.length() > 1 && txt.charAt(0) == '"' && txt.charAt(txt.length()-1) == '"') {
 					txt = txt.substring(1, txt.length()-1);
 				}
 				body.append(txt);
 			}
 		}
 	}
 	
 	private void buildMessages(HtmlElement element) {
 		prepForJava(body);
 
 		body.append("messagesBlock(").append(SBNAME).append(");\n");
 		
 		lastBodyIsJava = true;
 	}
 
 	private void buildMethod(StringBuilder sb, String name, String sig, List<EspLocation> locations, boolean lastIsJava) {
 		String s = sb.toString();
 		if(("\t\t" + SBNAME + ".append(\"").equals(s)) {
 			sb.delete(0, sb.length());
 		} else {
 			sb = new StringBuilder(sig.length() + s.length() + 15);
 			sb.append(sig).append(s);
 			if(lastIsJava) {
 				sb.append("\t}");
 			} else {
 				sb.append("\");\n\t}");
 			}
 			for(EspLocation location : locations) {
 				location.offset += sig.length();
 			}
 			esf.addMethod(name, sb.toString(), locations);
 		}
 	}
 	
 	private void buildNumber(HtmlElement input) {
 		body.append("<input type=\\\"text\\\"");
 		if(input.hasArgs()) {
 			List<EspPart> fields = input.getArgs();
 			String modelName = getFormModelName(input);
 			if(!blank(modelName)) {
 				body.append(" id=\\\"");
 				if(input.hasId()) {
 					build(input.getId(), body);
 				} else if(input.hasEntry("id")) {
 					build(input.getEntry("id").getValue(), body);
 				} else {
 					appendFormFieldName(modelName, fields);
 				}
 				body.append("\\\"");
 				buildClasses(input);
 				body.append(" name=\\\"");
 				if(input.hasEntry("name")) {
 					build(input.getEntry("name").getValue(), body);
 				} else {
 					appendFormFieldName(modelName, fields);
 				}
 				body.append("\\\"");
 				if(input.hasEntry("value")) {
 					body.append(" value=\\\"");
 					build(input.getEntry("value").getValue(), body);
 					body.append("\\\"");
 				} else {
 					body.append(" value=\\\"\").append(f(");
 					appendValueGetter(getFormModel(input), fields);
 					body.append(")).append(\"\\\"");
 				}
 				buildAttrs(input, "id", "name", "value", "onkeypress");
 			}
 		} else {
 			if(input.hasEntry("type")) {
 				body.append(" type=\\\"");
 				build(input.getEntry("type").getValue(), body);
 				body.append("\\\"");
 			}
 			buildId(input);
 			buildClasses(input);
 			buildAttrs(input, "type", "onkeypress");
 		}
 		if(input.hasEntry("onkeypress")) {
 			String f = input.getEntry("onkeypress").getValue().getText();
 			body.append(" onkeypress=\\\"").append(f);
 			if(!f.endsWith(";")) {
 				body.append(';');
 			}
 			body.append("var k=window.event?event.keyCode:event.which;return !(k>31&&(k<48||k>57));\\\"");
 		} else {
 			body.append(" onkeypress=\\\"var k=window.event?event.keyCode:event.which;return !(k>31&&(k<48||k>57));\\\"");
 		}
 		body.append(" />");
 	}
 
 	private void buildResetInput(HtmlElement element) {
 		body.append("<input");
 		body.append(" type=\\\"reset\\\"");
 		buildId(element);
 		buildClasses(element);
 		buildAttrs(element, "type");
 		if(!element.hasEntry("value")) {
 			body.append(" value=\\\"Reset\\\"");
 		}
 		body.append(" />");
 	}
 	
 	private void buildScript(ScriptElement element) {
 		StringBuilder sb = isInHead(element) ? script : body;
 		
 		String type = element.getJavaType();
 		if(type != null) {
 			if(sb == body) {
 				if("false".equals(element.getEntryText("inline"))) {
 					buildExternalEjs(sb, type, element);
 				} else {
 					buildInlineEjs(sb, type, element);
 				}
 			} else {
 				if("true".equals(element.getEntryText("inline"))) {
 					buildInlineEjs(sb, type, element);
 				} else {
 					buildExternalEjs(sb, type, element);
 				}
 			}
 		} else {
 			prepForMarkup(sb);
 			
 			if(element.hasArgs()) {
 				for(EspPart arg : element.getArgs()) {
 					String file = arg.getText();
 					if("defaults".equals(file)) {
 						sb.append("<script src='/jquery-1.4.2.min.js'></script>");
 						sb.append("<script src='/application.js'></script>");
 					} else {
 						sb.append("<script src='/");
 						sb.append(file);
 						if(!file.endsWith(".js")) {
 							sb.append(".js");
 						}
 						sb.append("'></script>");
 					}
 				}
 			}
 			if(element.hasLines()) {
 				List<EspPart> lines = element.getLines();
 				Map<String, EntryPart> vars = element.hasEntries() ? element.getEntries() : null;
 				if(!dom.isEjs()) {
 					sb.append("<script>");
 				}
 				for(int i = 0; i < lines.size(); i++) {
 					if(i != 0) sb.append("\\n");
 					appendEscaped(sb, trim(lines.get(i).getText()), vars);
 				}
 				if(!dom.isEjs()) {
 					sb.append("</script>");
 				}
 			}
 			
 			lastIsJava(sb, false);
 		}
 	}
 	
 	private void buildSelect(HtmlElement select) {
 		body.append("<select");
 		buildFormField(select, false);
 		body.append(">");
 	}
 	
 	// options<Member>(findAllMembers(), text:"option.getNameLF()", value:"option.getId(), sort:"option.getNameLF()")
 	private void buildSelectOptions(HtmlElement element) {
 		if(element.hasArgs()) {
 			prepForJava(body);
 
 			String options = element.getArgs().get(0).getText();
 			String selectionGetter = getSelectionGetter(element);
 			if(element.hasEntry("text") || element.hasEntry("value")) {
 				String type = element.hasJavaType() ? element.getJavaType() : "Object";
 				String text = element.hasEntry("text") ? element.getEntry("text").getText() : "String.valueOf(option)";
 				String value = element.hasEntry("value") ? element.getEntry("value").getText() : "option";
 				String title = element.hasEntry("title") ? element.getEntry("title").getText() : null;
 				if(blank(selectionGetter)) {
 					body.append("for(").append(type).append(" option : ").append(options).append(") {\n");
 					if(blank(title)) {
 						indent(body);
 						body.append('\t').append(SBNAME).append(".append(\"<option value=\\\"\"+f(");
 					} else {
 						indent(body);
 						body.append('\t').append(SBNAME).append(".append(\"<option title=\\\"\"+h(").append(title).append(")+\"\\\" value=\\\"\"+f(");
 					}
 					body.append(value).append(")+\"\\\" >\"+h(").append(text).append(")+\"</option>\");\n");
 					indent(body);
 					body.append("}\n");
 				} else {
 					String selectionVar = "selection$" + element.getStart();
 					String selectedVar = "selected$" + element.getStart();
 					
 					body.append("Object ").append(selectionVar).append(" = ").append(selectionGetter).append(";\n");
 					indent(body);
 					body.append("for(").append(type).append(" option : ").append(options).append(") {\n");
 					indent(body);
 					body.append("\tboolean ").append(selectedVar).append(" = isEqual(").append(value).append(", ").append(selectionVar).append(");\n");
 					if(blank(title)) {
 						indent(body);
 						body.append('\t').append(SBNAME).append(".append(\"<option value=\\\"\"+f(");
 					} else {
 						indent(body);
 						body.append('\t').append(SBNAME).append(".append(\"<option title=\\\"\"+h(").append(title).append(")+\"\\\" value=\\\"\"+f(");
 					}
 					body.append(value).append(")+\"\\\" \"+(").append(selectedVar).append(" ? \"selected >\" : \">\")+h(").append(text).append(")+\"</option>\");\n");
 					indent(body);
 					body.append("}\n");
 				}
 			} else {
 				if(blank(selectionGetter)) {
 					body.append(sbName).append(".append(optionTags(").append(options).append("));\n");
 				} else {
 					body.append(sbName).append(".append(optionTags(").append(options).append(", ").append(selectionGetter).append("));\n");
 				}
 			}
 
 			lastBodyIsJava = true;
 		}
 	}
 
 	private void buildStyle(StyleElement element) {
 		StringBuilder sb = isInHead(element) ? style : body;
 		
 		String type = element.getJavaType();
 		if(type != null) {
 			if(sb == body) {
 				if("false".equals(element.getEntryText("inline"))) {
 					buildExternalEss(sb, type, element);
 				} else {
 					buildInlineEss(sb, type, element);
 				}
 			} else {
 				if("true".equals(element.getEntryText("inline"))) {
 					buildInlineEss(sb, type, element);
 				} else {
 					buildExternalEss(sb, type, element);
 				}
 			}
 		} else {
 			prepForMarkup(sb);
 			
 			if(element.hasArgs()) {
 				for(EspPart arg : element.getArgs()) {
 					String file = arg.getText();
 					if("defaults".equals(file)) {
 						sb.append("<link rel='stylesheet' type='text/css' href='/application.css' />");
 					} else {
 						sb.append("<link rel='stylesheet' type='text/css' href='/").append(file);
 						if(file.endsWith(".css")) {
 							sb.append("' />");
 						} else {
 							sb.append(".css' />");
 						}
 					}
 				}
 			}
 			if(element.hasChildren()) {
 				boolean firstChild = true;
 				List<StyleChildElement> children = element.getChildren();
 				Map<String, EntryPart> vars = element.hasEntries() ? element.getEntries() : null;
 				if(!dom.isEss()) {
 					sb.append("<style>");
 				}
 				for(StyleChildElement child : children) {
 					if(child.hasSelectors() && child.hasProperties()) {
 						if(firstChild) firstChild = false;
 						else sb.append(' ');
 						List<EspPart> selectors = child.getSelectors();
 						for(int j = 0; j < selectors.size(); j++) {
 							if(j != 0) sb.append(' ');
 							sb.append(selectors.get(j).getText());
 						}
 						sb.append('{');
 						boolean firstProperty = true;
 						List<StylePropertyPart> properties = child.getProperties();
 						for(StylePropertyPart property : properties) {
 							if(property.hasName() && property.hasValue()) {
 								if(firstProperty) {
 									firstProperty = false;
 								} else {
 									sb.append(';');
 								}
 								sb.append(property.getName().getText());
 								EspPart value = property.getValue();
 								if(value != null) {
 									if(property.isValueJava()) {
 										sb.append(":\").append(").append(value.getText()).append(").append(\"");
 									} else {
 										sb.append(':');
 										appendEscaped(sb, value.getText(), vars);
 									}
 								}
 							}
 						}
 						sb.append('}');
 					}
 				}
 				if(!dom.isEss()) {
 					sb.append("</style>");
 				}
 			}
 			
 			lastIsJava(sb, false);
 		}
 	}
 	
 	private void buildSubmit(HtmlElement element) {
 		body.append("<input");
 		body.append(" type=\\\"submit\\\"");
 		buildId(element);
 		buildClasses(element);
 		buildAttrs(element, "type");
 		if(!element.hasEntry("value")) {
 			String model = getFormModel(element);
 			if(blank(model)) {
 				body.append(" value=\\\"Submit\\\"");
 			} else {
 				String modelName = getFormModelName(element);
 				String action = getFormAction(element);
 				if("create".equalsIgnoreCase(action)) {
 					body.append(" value=\\\"Create ");
 				} else if("update".equalsIgnoreCase(action)) {
 					body.append(" value=\\\"Update ");
 				} else {
					body.append(" value=\\\"\").append(").append(model).append(".isNew() ? \"Create \" : \"Update ");
 				}
 				body.append("\").append(titleize(").append(modelName).append(")).append(\"\\\"");
 			}
 		}
 		body.append(" />");
 	}
 	
 	private void buildTextArea(HtmlElement textarea) {
 		body.append("<textarea");
 		buildFormField(textarea, false);
 		body.append(">");
 		if(textarea.hasArgs()) {
 			body.append("\").append(f(");
 			appendValueGetter(getFormModel(textarea), textarea.getArgs());
 			body.append(")).append(\"");
 		} else {
 			if(textarea.hasInnerText()) {
 				build(textarea.getInnerText(), body);
 			}
 		}
 	}
 
 	private void buildTitle(HtmlElement element) {
 		if(element.hasInnerText()) {
 			EspPart part = element.getInnerText();
 			if(part.getText().startsWith("+= ")) {
 				int ix = title.length();
 				title.append(' ');
 				build(part, title);
 				title.delete(ix, ix+3);
 			} else {
 				if(title.length() > 0) {
 					title = new StringBuilder();
 				}
 				build(part, title);
 			}
 		}
 	}
 
 	private void buildView(HtmlElement view) {
 		String type = view.getJavaType();
 		if(type != null) {
 			
 			if(view.hasEntries()) {
 				Entry<String, EntryPart> entry = view.getEntries().entrySet().iterator().next();
 				EntryPart part = entry.getValue();
 				if(!blank(part)) {
 					String itype = entry.getKey();
 					String var = varName(itype) + "$" + view.getStart();
 					
 					prepForJava(body);
 
 					body.append("for(").append(itype).append(' ').append(var).append(" : ").append(part.getValue().getText()).append(") {\n");
 					indent(body);
 					body.append("\tyield(new ").append(type).append('(').append(var).append("), ").append(sbName).append(");\n");
 					indent(body);
 					body.append("}\n");
 					
 					lastBodyIsJava = true;
 				}
 			} else {
 				prepForJava(body);
 				body.append("yield(new ").append(type);
 				if(view.hasArgs()) {
 					body.append("(");
 					for(Iterator<EspPart> iter = view.getArgs().iterator(); iter.hasNext(); ) {
 						build(iter.next(), body, true);
 						if(iter.hasNext()) body.append(',').append(' ');
 					}
 					body.append("), ").append(sbName).append(");\n");
 				} else {
 					body.append("(), ").append(sbName).append(");\n");
 				}
 				lastBodyIsJava = true;
 			}
 		}
 	}
 
 	private void buildYield(HtmlElement element) {
 		prepForJava(body);
 
 		if(dom.isEsp()) {
 			if(element.hasArgs()) {
 				body.append("yield(");
 				build(element.getArg(0), body, true);
 				body.append(", ").append(sbName).append(");\n");
 			} else {
 				body.append("yield(").append(sbName).append(");\n");
 			}
 		} else {
 			body.append("yield(").append(sbName).append(");\n");
 		}
 
 		lastBodyIsJava = true;
 	}
 	
 	// TODO refactor compile methods (lots of overlap)
 	public ESourceFile compile() {
 		switch(dom.getDocType()) {
 		case ESP: return compileEsp();
 		case EMT: return compileEmt();
 		case ESS: return compileEss();
 		case EJS: return compileEjs();
 		default:
 			throw new IllegalArgumentException("don't know how to compile DocType: " + dom.getDocType());
 		}
 	}
 	
 	private ESourceFile compileEjs() {
 		esf = new ESourceFile();
 		body = new StringBuilder();
 		bodyLocations = new ArrayList<EspLocation>();
 
 		esf.setPackage(pkg);
 		esf.addImport(ScriptFile.class.getCanonicalName());
 		esf.setSimpleName(dom.getName());
 		esf.setSuperName(ScriptFile.class.getSimpleName());
 
 		if(dom.hasParts()) {
 			int ix = 0;
 			List<EspPart> parts = dom.getParts();
 			
 			while(ix < parts.size()) {
 				EspPart part = parts.get(ix);
 				if(part.isA(ImportElement)) {
 					buildImport((ImportElement) part);
 					ix++;
 				} else {
 					break;
 				}
 			}
 			
 			while(ix < parts.size()) {
 				EspPart part = parts.get(ix);
 				if(part.isA(ConstructorElement)) {
 					buildConstructor((ConstructorElement) part);
 					ix++;
 				} else {
 					break;
 				}
 			}
 
 			body.append("\t\t").append(SBNAME).append(".append(\"");
 			for( ; ix < parts.size(); ix++) {
 				EspElement element = (EspElement) parts.get(ix);
 				if(element.isA(JavaElement)) {
 					buildJava((JavaElement) element, true);
 				} else {
 					buildElement(element);
 				}
 			}
 		
 			buildMethod(
 					body,
 					"doRender",
 					"\t@Override\n\tpublic void doRender(StringBuilder " + SBNAME + ") throws Exception {\n",
 					bodyLocations,
 					lastBodyIsJava
 			);
 		}
 		
 		esf.finalizeSource();
 		
 		return esf;
 	}
 	
 	private ESourceFile compileEmt() {
 		esf = new ESourceFile();
 		body = new StringBuilder();
 		bodyLocations = new ArrayList<EspLocation>();
 
 		esf.setPackage(pkg);
 		esf.addStaticImport(Action.class.getCanonicalName() + ".*");
 		esf.addStaticImport(ArrayUtils.class.getCanonicalName() + ".*");
 		esf.addStaticImport(JsonUtils.class.getCanonicalName() + ".*");
 		esf.addStaticImport(StringUtils.class.getCanonicalName() + ".*");
 		esf.addStaticImport(Utils.class.getCanonicalName() + ".*");
 		esf.addImport(MailerTemplate.class.getCanonicalName());
 		esf.addClassAnnotation("@SuppressWarnings(\"unused\")");
 		esf.setSimpleName(dom.getName());
 		esf.setSuperName(MailerTemplate.class.getSimpleName());
 
 		if(dom.hasParts()) {
 			int ix = 0;
 			List<EspPart> parts = dom.getParts();
 			
 			while(ix < parts.size()) {
 				EspPart part = parts.get(ix);
 				if(part.isA(ImportElement)) {
 					buildImport((ImportElement) part);
 					ix++;
 				} else {
 					break;
 				}
 			}
 			
 			while(ix < parts.size()) {
 				EspPart part = parts.get(ix);
 				if(part.isA(ConstructorElement)) {
 					buildConstructor((ConstructorElement) part);
 					ix++;
 				} else {
 					break;
 				}
 			}
 
 			body.append("\t\t").append(SBNAME).append(".append(\"");
 			for( ; ix < parts.size(); ix++) {
 				EspElement element = (EspElement) parts.get(ix);
 				if(element.isA(JavaElement)) {
 					buildJava((JavaElement) element, true);
 				} else {
 					buildElement(element);
 				}
 			}
 		
 			buildMethod(
 					body,
 					"doRender",
 					"\t@Override\n\tpublic void doRender(StringBuilder " + SBNAME + ") throws Exception {\n",
 					bodyLocations,
 					lastBodyIsJava
 			);
 		}
 		
 		esf.finalizeSource();
 		
 		return esf;
 	}
 	
 	private ESourceFile compileEsp() {
 		esf = new ESourceFile();
 		body = new StringBuilder();
 		body.append("\t\t").append(SBNAME).append(".append(\"");
 		bodyLocations = new ArrayList<EspLocation>();
 		script = new StringBuilder();
 		script.append("\t\t").append(SBNAME).append(".append(\"");
 		scriptLocations = new ArrayList<EspLocation>();
 		style = new StringBuilder();
 		style.append("\t\t").append(SBNAME).append(".append(\"");
 		styleLocations = new ArrayList<EspLocation>();
 		meta = new StringBuilder();
 		title = new StringBuilder();
 		titleLocations = new ArrayList<EspLocation>();
 
 		esf.setPackage(pkg);
 		esf.addStaticImport(Action.class.getCanonicalName() + ".*");
 		esf.addStaticImport(ArrayUtils.class.getCanonicalName() + ".*");
 		esf.addStaticImport(JsonUtils.class.getCanonicalName() + ".*");
 		esf.addStaticImport(StringUtils.class.getCanonicalName() + ".*");
 		esf.addStaticImport(Utils.class.getCanonicalName() + ".*");
 		esf.addImport(View.class.getCanonicalName());
 		esf.addImport(Controller.class.getCanonicalName());
 		esf.addClassAnnotation("@SuppressWarnings(\"unused\")");
 		esf.setSimpleName(dom.getName());
 		esf.setSuperName(View.class.getSimpleName());
 
 		if(dom.hasParts()) {
 			int ix = 0;
 			List<EspPart> parts = dom.getParts();
 			
 			while(ix < parts.size()) {
 				EspPart part = parts.get(ix);
 				if(part.isA(ImportElement)) {
 					buildImport((ImportElement) part);
 					ix++;
 				} else {
 					break;
 				}
 			}
 			
 			while(ix < parts.size()) {
 				EspPart part = parts.get(ix);
 				if(part.isA(ConstructorElement)) {
 					buildConstructor((ConstructorElement) part);
 					ix++;
 				} else {
 					break;
 				}
 			}
 
 			for( ; ix < parts.size(); ix++) {
 				EspElement element = (EspElement) parts.get(ix);
 				if(captureLevel != -1 && element.getLevel() <= captureLevel) {
 					stopCapture();
 				}
 				if(element.isA(JavaElement)) {
 					buildJava((JavaElement) element, true);
 				} else {
 					buildElement(element);
 				}
 			}
 			if(captureLevel != -1) {
 				stopCapture();
 			}
 
 			buildEspMethods();
 		}
 		
 		esf.finalizeSource();
 		
 		return esf;
 	}
 	
 	private ESourceFile compileEss() {
 		esf = new ESourceFile();
 		body = new StringBuilder();
 		bodyLocations = new ArrayList<EspLocation>();
 
 		esf.setPackage(pkg);
 		esf.addImport(StyleSheet.class.getCanonicalName());
 		esf.setSimpleName(dom.getName());
 		esf.setSuperName(StyleSheet.class.getSimpleName());
 
 		if(dom.hasParts()) {
 			int ix = 0;
 			List<EspPart> parts = dom.getParts();
 			
 			while(ix < parts.size()) {
 				EspPart part = parts.get(ix);
 				if(part.isA(ImportElement)) {
 					buildImport((ImportElement) part);
 					ix++;
 				} else {
 					break;
 				}
 			}
 			
 			while(ix < parts.size()) {
 				EspPart part = parts.get(ix);
 				if(part.isA(ConstructorElement)) {
 					buildConstructor((ConstructorElement) part);
 					ix++;
 				} else {
 					break;
 				}
 			}
 
 			body.append("\t\t").append(SBNAME).append(".append(\"");
 			for( ; ix < parts.size(); ix++) {
 				EspElement element = (EspElement) parts.get(ix);
 				if(element.isA(JavaElement)) {
 					buildJava((JavaElement) element, true);
 				} else {
 					buildElement(element);
 				}
 			}
 		
 			buildMethod(
 					body,
 					"doRender",
 					"\t@Override\n\tpublic void doRender(StringBuilder " + SBNAME + ") throws Exception {\n",
 					bodyLocations,
 					lastBodyIsJava
 			);
 		}
 		
 		esf.finalizeSource();
 		
 		return esf;
 	}
 
 	private boolean containsFileInput(EspElement element) {
 		switch(element.getType()) {
 		case HtmlElement:
 			HtmlElement h = (HtmlElement) element;
 			if("file".equals(h.getTag())) {
 				return true;
 			}
 			if(h.hasChildren()) {
 				for(EspElement child : h.getChildren()) {
 					if(containsFileInput(child)) {
 						return true;
 					}
 				}
 			}
 			return false;
 		case JavaElement:
 			JavaElement j = (JavaElement) element;
 			if(j.hasChildren()) {
 				for(EspElement child : j.getChildren()) {
 					if(containsFileInput(child)) {
 						return true;
 					}
 				}
 			}
 			return false;
 		default:
 			return false;
 		}
 	}
 	
 	private HtmlElement getForm(HtmlElement formField) {
 		EspPart parent = formField.getParent();
 		while(parent != null) {
 			if(parent.isA(HtmlElement)) {
 				HtmlElement h = (HtmlElement) parent;
 				if("form".equals(h.getTag())) {
 					List<EspPart> args = h.getArgs();
 					if(args != null && (args.size() == 1 || args.size() == 2)) {
 						return h; // args.get(0).getText().trim();
 					}
 					break; // forms shouldn't be nested...
 				} else if("fields".equals(h.getTag())) {
 					List<EspPart> args = h.getArgs();
 					if(args != null && args.size() == 1) {
 						return h; // args.get(0).getText().trim();
 					}
 					// fields section may be nested...
 				}
 			}
 			parent = parent.getParent();
 		}
 		return null;
 	}
 	
 	private String getFormAction(HtmlElement formField) {
 		EspPart parent = formField.getParent();
 		while(parent != null) {
 			if(parent.isA(HtmlElement)) {
 				HtmlElement h = (HtmlElement) parent;
 				if("form".equals(h.getTag())) {
 					List<EspPart> args = h.getArgs();
 					if(args != null && args.size() == 2) {
 						return args.get(1).getText().trim();
 					}
 					if(h.hasEntry("method")) {
 						EspPart part = h.getEntry("method").getValue();
 						if(part != null) {
 							String method = part.getText().trim();
 							if("post".equalsIgnoreCase(method)) {
 								return "create";
 							} else if("put".equalsIgnoreCase(method)) {
 								return "update";
 							}
 						}
 						return null;
 					}
 					if(h.hasEntry("action")) {
 						EspPart part = h.getEntry("action").getValue();
 						if(part != null) {
 							return part.getText().trim();
 						}
 						return null;
 					}
 					break; // forms shouldn't be nested...
 				}
 			}
 			parent = parent.getParent();
 		}
 		return null;
 	}
 	
 	private String getFormModel(HtmlElement formField) {
 		HtmlElement form = getForm(formField);
 		if(form != null) {
 			return getFormModelVar(form);
 		}
 		return null;
 	}
 	
 	private String getFormModelName(HtmlElement formField) {
 		HtmlElement form = getForm(formField);
 		if(form != null) {
 			return getFormModelNameVar(form.getStart());
 		}
 		return null;
 	}
 	
 	private String getFormModelNameValue(HtmlElement form) {
 		if(form.hasEntry("as")) {
 			return form.getEntryValue("as").getText().trim();
 		}
 		String arg = form.getArg(0).getText().trim();
 		for(int i = 0; i < arg.length(); i++) {
 			if(!Character.isJavaIdentifierPart(arg.charAt(i))) {
 				return "varName((" + arg + ").getClass())";
 			}
 		}
 		return "\"" + arg + "\"";
 	}
 
 	private String getSelectionGetter(HtmlElement options) {
 		EspPart parent = options.getParent();
 		if(parent instanceof HtmlElement && "select".equals(((HtmlElement) parent).getTag())) {
 			HtmlElement select = (HtmlElement) parent;
 			String model = getFormModel(select);
 			if(!blank(model)) {
 				StringBuilder sb = new StringBuilder();
 				List<EspPart> fields = select.getArgs();
 				if(fields.size() == 1) {
 					sb.append(model).append('.').append(getterName(fields.get(0).getText())).append('(').append(')');
 				} else {
 					int last = fields.size() - 1;
 					for(int i = 0; i < fields.size(); i++) {
 						sb.append(model);
 						for(int j = 0; j <= i; j++) {
 							sb.append('.').append((j == i && j != last) ? hasserName(fields.get(j).getText()) : getterName(fields.get(j).getText())).append("()");
 						}
 						if(i < last) {
 							sb.append("?(");
 						}
 					}
 					for(int i = 0; i < last; i++) {
 						sb.append("):\"\"");
 					}
 				}
 				return sb.toString();
 			}
 		}
 		return null;
 	}
 	
 	private void indent(StringBuilder sb) {
 		for(int i = 0; i < level(sb)+2; i++) {
 			sb.append('\t');
 		}
 	}
 	
 	private boolean isInHead(EspElement element) {
 		EspPart parent = element.getParent();
 		while(parent != null && !parent.isA(DOM)) {
 			switch(parent.getType()) {
 			case DOM:
 				return false;
 			case HtmlElement:
 				if("head".equals(((HtmlElement) parent).getTag())) {
 					return true;
 				}
 			default:
 				parent = parent.getParent();
 			}
 		}
 		return false;
 	}
 	
 	private boolean lastIsJava(StringBuilder sb) {
 		if(sb == body) return lastBodyIsJava;
 		if(sb == script) return lastScriptIsJava;
 		if(sb == style) return lastStyleIsJava;
 		return false;
 	}
 	
 	private void lastIsJava(StringBuilder sb, boolean lastIsJava) {
 		if(sb == body) lastBodyIsJava = lastIsJava;
 		if(sb == script) lastScriptIsJava = lastIsJava;
 		if(sb == style) lastStyleIsJava = lastIsJava;
 	}
 
 	private int level(StringBuilder sb) {
 		if(sb == body) return javaBodyLevel;
 		if(sb == script) return javaScriptLevel;
 		if(sb == style) return javaStyleLevel;
 		return 0;
 	}
 	
 	private void prepForJava(StringBuilder sb) {
 		if(lastIsJava(sb)) {
 			indent(sb);
 		} else {
 			lastIsJava(sb, true);
 			String s = SBNAME + ".append(\"";
 			if(sb.length() < s.length()) {
 				sb.append("\");\n");
 				indent(sb);
 			} else {
 				for(int i = 0; i < s.length(); i++) {
 					if(sb.charAt(sb.length()-i-1) != s.charAt(s.length()-i-1)) {
 						sb.append("\");\n");
 						indent(sb);
 						return;
 					}
 				}
 				sb.delete(sb.length()-s.length(), sb.length());
 			}
 		}
 	}
 	
 	private void prepForMarkup(StringBuilder sb) {
 		if(lastIsJava(sb)) {
 			indent(sb);
 			sb.append(sbName(sb)).append(".append(\"");
 			lastIsJava(sb, false);
 		}
 	}
 	
 	private String sbName(StringBuilder sb) {
 		if(sb == body) return sbName;
 		return SBNAME;
 	}
 	
 	private void startCapture(HtmlElement capture) {
 		prepForJava(body);
 
 		if(capture.hasArgs()) {
 			sbName = capture.getArg(0).getText().trim();
 		}
 		sbName = sbName + "$" + capture.getStart();
 		body.append("StringBuilder ").append(sbName).append(" = new StringBuilder();\n");
 
 		captureLevel = capture.getLevel();
 		
 		lastBodyIsJava = true;
 	}
 	
 	private void startContent(HtmlElement content) {
 		prepForJava(body);
 
 		if(content.hasArgs()) {
 			sbName = content.getArg(0).getText().trim();
 		}
 		
 		contentName = sbName;
 
 		if(sbName.charAt(0) == '"' && sbName.charAt(sbName.length()-1) == '"') {
 			sbName = underscored(sbName.substring(1, sbName.length()-1));
 		}
 		
 		sbName = sbName + "$" + content.getStart();
 		body.append("StringBuilder ").append(sbName).append(" = new StringBuilder();\n");
 
 		captureLevel = content.getLevel();
 		
 		lastBodyIsJava = true;
 	}
 	
 	private void stopCapture() {
 		if(contentName != null) {
 			stopContent();
 		} else {
 			prepForJava(body);
 	
 			String var = sbName.substring(0, sbName.lastIndexOf('$'));
 			body.append("String ").append(var).append(" = ").append(sbName).append(".toString();\n");
 			indent(body);
 			body.append(sbName).append(" = null;\n");
 			
 			captureLevel = -1;
 			sbName = SBNAME;
 			
 			lastBodyIsJava = true;
 		}
 	}
 
 	private void stopContent() {
 		prepForJava(body);
 
 		body.append("putContent(").append(contentName).append(", ").append(sbName).append(".toString());\n");
 		indent(body);
 		body.append(sbName).append(" = null;\n");
 		
 		captureLevel = -1;
 		contentName = null;
 		sbName = SBNAME;
 		
 		lastBodyIsJava = true;
 	}
 	
 	/**
 	 * removes only spaces, leave other whitespace characters
 	 */
 	private String trim(String s) {
 		char[] ca = s.toCharArray();
 		for(int i1 = 0; i1 >= 0 && i1 < ca.length; i1++) {
 			if(ca[i1] != ' ') {
 				for(int i2 = ca.length-1; i2 >= i1; i2--) {
 					if(ca[i2] != ' ') {
 						return (i1 > 0 || i2 < ca.length-1) ? s.substring(i1, i2+1) : s;
 					}
 				}
 			}
 		}
 		return "";
 	}
 
 }
