 package kkckkc.syntaxpane.parse.grammar;
 
 import kkckkc.syntaxpane.model.Scope;
 import kkckkc.syntaxpane.model.ScopeWithAttributes;
 import kkckkc.syntaxpane.parse.grammar.SubPatternContext.Where;
 import kkckkc.syntaxpane.regex.Matcher;
 import kkckkc.syntaxpane.regex.Pattern;
 import kkckkc.syntaxpane.regex.PatternFactory;
 import kkckkc.utils.StringUtils;
 
 import java.util.Map;
 
 public class ContainerContext extends MatchableContext {
 	protected Pattern beginPattern;
 	protected Pattern endPattern;
 	
 	protected boolean styleInside;
 	protected boolean endAtLineEnd;
 
 	protected String previousEndPattern = "";
 	private PatternFactory factory;
     private boolean disabled;
 
     private boolean contentNameContext;
     private String endPatternExpression;
 
     public ContainerContext(PatternFactory factory) {
 		this.factory = factory;
 		this.beginPattern = factory.create("$^");
 	}
 
 	public ContainerContext(PatternFactory factory, String id) {
 		this.id = id;
 		this.factory = factory;
 		this.beginPattern = factory.create("$^");
 	}
 
 	public void setBegin(Pattern begin) {
 		this.beginPattern = begin;
 	}
 	
 	public void setEnd(Pattern end) {
 		this.endPattern = end;
         this.endPatternExpression = end.pattern();
 	}
 
 	public boolean isStyleInside() {
 		return styleInside;
 	}
 
 	public void setStyleInside(boolean styleInside) {
 		this.styleInside = styleInside;
 	}
 	
 	public boolean isEndAtLineEnd() {
 		return endAtLineEnd;
 	}
 
 	public void setEndAtLineEnd(boolean endAtLineEnd) {
 		this.endAtLineEnd = endAtLineEnd;
 	}
 
 	
 	public Scope createScope(Scope parent, Matcher matcher) {
 		Scope scope;
 
 		if (endPattern != null && !contentNameContext && endPatternExpression.indexOf("@start") >= 0) {
 			if (isStyleInside()) {
                 scope = new ScopeWithAttributes(matcher.end(), matcher.end(), this, parent);
             } else {
                 scope = new ScopeWithAttributes(matcher.start(), matcher.end(), this, parent);
             }
 
             for (int i = 0; i < matcher.groupCount(); i++) {
 				((ScopeWithAttributes) scope).addAttribute(i + "@start", matcher.group(i));
 			}
 		} else {
             if (isStyleInside()) {
                 scope = new Scope(matcher.end(), matcher.end(), this, parent);
             } else {
                 scope = new Scope(matcher.start(), matcher.end(), this, parent);
             }
         }
 		
 		buildSubPatternScopes(scope, matcher, Where.START);
 		return scope;
 	}
 	
 	public int close(Scope scope, Matcher matcher) {
 		if (isStyleInside()) {
 			scope.close(matcher.start());
 			buildSubPatternScopes(scope, matcher, Where.END);
 			return matcher.start();
 		} else {
 			scope.close(matcher.end());
 			buildSubPatternScopes(scope, matcher, Where.END);
 			return matcher.end();
 		}
 	}
 
 	public Matcher getEndMatcher(CharSequence segment, Scope scope) {
 		if (endPattern != null && endPatternExpression.indexOf("@start") >= 0) {
 			String p = endPatternExpression;
             if (contentNameContext) {
                 for (Map.Entry<String, String> entry : ((ScopeWithAttributes) scope.getParent()).getAttributes().entrySet()) {
                    p = StringUtils.replace(p, "\\%{" + entry.getKey() + "}", java.util.regex.Pattern.quote(entry.getValue()));
                 }
             } else if (scope instanceof ScopeWithAttributes && ((ScopeWithAttributes) scope).getAttributes() != null) {
                 for (Map.Entry<String, String> entry : ((ScopeWithAttributes) scope).getAttributes().entrySet()) {
                    p = StringUtils.replace(p, "\\%{" + entry.getKey() + "}", java.util.regex.Pattern.quote(entry.getValue()));
                 }
             }
 			
 			if (! previousEndPattern.equals(p)) {
 				previousEndPattern = p;
 				endPattern = factory.create(p);
 			}
		}

 		return endPattern == null ? factory.create("$^").matcher(segment) : endPattern.matcher(segment);
 	}
 
 	public Matcher getMatcher(CharSequence seq) {
         if (disabled) {
             return getDisabledMatcher(seq);
         } else {
 		    return beginPattern.matcher(seq);
         }
 	}
 
     private Matcher getDisabledMatcher(CharSequence seq) {
         return factory.create("a$b").matcher(seq);
     }
 
     public void setDisabled(boolean disabled) {
         this.disabled = disabled;
     }
 
     public void setContentNameContext(boolean contentNameContext) {
         this.contentNameContext = contentNameContext;
     }
 }
