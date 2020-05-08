 package org.oobium.build.esp.compiler;
 
 import static org.oobium.build.esp.dom.EspDom.DocType.ESS;
 
 import java.util.List;
 
 import org.oobium.build.esp.dom.EspResolver;
 import org.oobium.build.esp.dom.elements.StyleElement;
 import org.oobium.build.esp.dom.parts.MethodArg;
 import org.oobium.build.esp.dom.parts.StylePart;
 import org.oobium.build.esp.dom.parts.style.Property;
 import org.oobium.build.esp.dom.parts.style.Ruleset;
 import org.oobium.build.esp.dom.parts.style.Selector;
 
 public class StyleCompiler extends AssetCompiler {
 
 	private StyleElement element;
 	
 	public StyleCompiler(EspCompiler parent) {
 		super(parent);
 	}
 
 	public void buildStyle(StyleElement element) {
 		this.element = element;
 		buildStyle();
 	}
 	
 	private void buildStyle() {
 		if(element.hasJavaType()) {
 			if("true".equals(element.getEntry("inline"))) {
 				buildInlineEss(element);
 			} else {
 				buildExternalEss(element);
 			}
 		} else {
 			StringBuilder body = parent.getBody();
 			if(element.hasArgs()) {
 				parent.prepForJava(body);
 				for(MethodArg arg : element.getArgs()) {
 					body.append("addExternalStyle(");
 					parent.build(arg, body);
 					body.append(");\n");
 				}
 			}
 			if(element.hasAsset()) {
 				parent.prepForMarkup(body);
 				String oldSection = parent.prepFor(element);
 				if(!parent.getDom().is(ESS)) {
 					if(element.hasEntry("media")) {
 						body.append("<style media=");
 						parent.appendAttr("media", element.getEntry("media"));
 						body.append(">");
 					} else {
 						body.append("<style>");
 					}
 				}
 				buildStylePart(element.getAsset(), body);
 				if(!parent.getDom().is(ESS)) {
 					body.append("</style>");
 				}
 				parent.prepFor(oldSection);
 			}
 		}
 	}
 	
 	private void buildMethod(Property mixin) {
 		StringBuilder body = parent.getBody();
 		parent.prepFor(mixin);
 		parent.prepForJava(body);
 		body.append(parent.sbName(body)).append(".append(");
 		body.append(mixin.getMethodName()).append("(");
 		if(mixin.hasArgs()) {
 			List<MethodArg> args = mixin.getArgs();
 			for(int i = 0; i < args.size(); i++) {
 				if(i != 0) body.append(", ");
 				parent.build(args.get(i), body, true);
 			}
 		}
 		body.append("));\n");
 		parent.prepForMarkup(body);
 	}
 	
 	private void buildStylePart(StylePart style, StringBuilder sb) {
 		if(style.hasRules()) {
 			for(Ruleset rule : style.getRules()) {
 				if(rule.isParametric()) {
 					parent.buildMethodSignature(rule);
 				} else {
 					buildStyleRuleset(rule, sb);
 				}
 			}
 		}
 	}
 
 	void buildStyleProperties(Ruleset rule, StringBuilder sb) {
 		buildStyleProperties(rule, sb, true);
 	}
 	
 	boolean buildStyleProperties(Ruleset rule, StringBuilder sb, boolean first) {
 		if(rule.hasDeclaration()) {
 			for(Property property : rule.getDeclaration().getProperties()) {
 				first = buildStyleProperty(property, sb, first);
 			}
 		}
 		return first;
 	}
 	
 	private boolean buildStyleProperty(Property property, StringBuilder sb, boolean first) {
 		if(property.isParametric()) {
 			buildMethod(property);
 		}
		else if(property.isMixin()) {
 			EspResolver resolver = parent.getResolver();
 			String name = property.getName().getText();
 			Selector selector = (resolver != null) ? resolver.getCssSelector(name) : null;
 			if(selector == null) {
 				parent.logger.debug("could not find mixin with the name: '{}'", name);
 			} else {
 				buildStyleProperties(selector.getRuleset(), sb, first);
 			}
 		}
 		else {
 			if(first) { first = false; } else { sb.append(';'); }
 			sb.append(property.getName().getText());
 			if(property.hasValue()) {
 				sb.append(':');
 				parent.build(property.getValue(), sb);
 			}
 		}
 		return first;
 	}
 	
 	private void buildStyleRuleset(Ruleset ruleset, StringBuilder sb) {
 		buildStyleRuleset("", ruleset, sb);
 	}
 	
 	private void buildStyleRuleset(String path, Ruleset ruleset, StringBuilder sb) {
 		if(ruleset.hasDeclaration() && ruleset.hasSelectors()) {
 			List<Selector> selectors = ruleset.getSelectors();
 			for(int i = 0; i < selectors.size(); i++) {
 				Selector selector = selectors.get(i);
 				if(ruleset.hasProperties()) {
 					sb.append(path);
 					sb.append(selector.getText().trim());
 					sb.append('{');
 					buildStyleProperties(ruleset, sb);
 					sb.append('}');
 				}
 				if(ruleset.hasNestedRules()) {
 					String text = path + selector.getText().trim();
 					for(Ruleset nested : ruleset.getNestedRules()) {
 						String nestedPath = nested.isMerged() ? text : (text + " ");
 						buildStyleRuleset(nestedPath, nested, sb);
 					}
 				}
 			}
 		}
 	}
 	
 }
