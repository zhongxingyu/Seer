 package cc.warlock.core.script.javascript;
 
 import java.io.Serializable;
 
 import org.mozilla.javascript.Function;
 import org.mozilla.javascript.NativeArray;
 import org.mozilla.javascript.Scriptable;
 
 import cc.warlock.core.script.IScriptCommands;
 import cc.warlock.core.script.Match;
 import cc.warlock.core.script.internal.RegexMatch;
 import cc.warlock.core.script.internal.TextMatch;
 
 public class JavascriptCommands {
 
 	protected IScriptCommands commands;
 	protected JavascriptScript script;
 	
 	private static final String CALLBACK = "callback";
 	private static final String USER_OBJECT = "userobject";
 	
 	public class JavascriptStopException extends Exception implements Serializable {
 		private static final long serialVersionUID = 7226391328268718796L;
 	}
 	
 	public JavascriptCommands(IScriptCommands commands, JavascriptScript script) {
 		this.commands = commands;
 		this.script = script;
 	}
 
 	public void echo(String text) {
 		commands.echo(text);
 	}
 
 	public Match matchWait(NativeArray matches) {
 		/*
 		int len = (int) matches.getLength(); 
 		for (int i = 0; i < len; i++) {
 			Object obj = matches.get(i, matches);
 			engine.content.toObject(obj, matches);
 			
 		}
 		*/
 		for(Object o : matches.getIds()) {
			if (o instanceof Match) continue; // TODO: Throw a friendly error
			commands.addMatch((Match) matches.getAssociatedValue(o));
 		}
 		commands.echo("Sending off to matching");
 		Match match = commands.matchWait();
 		commands.echo("Got a match!");
 		Function function = (Function)match.getAttribute(CALLBACK);
 		try {
 			if (match.getAttribute(USER_OBJECT) == null) {
 				function.call(script.getContext(), script.getScope(), null, new Object[] {});
 			} else {
 				function.call(script.getContext(), script.getScope(), null, new Object[] {match.getAttribute(USER_OBJECT)});
 			}
 		}
 		catch (Exception e) {
 			e.printStackTrace();
 		}
 		return match;
 	}
 
 	public void move(String direction) {
 		commands.move(direction);
 	}
 
 	public void pause(int seconds) {
 		commands.pause(seconds);
 	}
 
 	public void put(String text) {
 		commands.put(text);
 	}
 	
 	public void waitFor(String string)
 	{
 		waitFor(new TextMatch(string, true));
 	}
 	
 	// Default to case sensitivity
 	public void waitForRe(String string) {
 		waitForRe(string, false);
 	}
 	
 	public void waitForRe(String string, Boolean ignoreCase)
 	{
 		waitFor(new RegexMatch(string, ignoreCase));
 	}
 
 	public void waitFor(Match match) {
 		commands.waitFor(match);
 	}
 
 	public void waitForPrompt() {
 		commands.waitForPrompt();
 	}
 
 	public void exit() throws JavascriptStopException {
 		script.stop();
 		
 		throw new JavascriptStopException();
 	}
 	
 	// Match match(String text, Function function[, Scriptable object])
 	
 	public Match match(String text, Function function) {
 		return match(text, function, null);
 	}
 	
 	public Match match(String text, Function function, Scriptable object) {
 		Match m = new TextMatch(text);
 		m.setAttribute(CALLBACK, function);
 		m.setAttribute(USER_OBJECT, object);
 		
 		return m;
 	}
 	
 	// Match matchre(String text, Function function[, Boolean ignoreCase[, Scriptable object]])
 	public Match matchRe(String text, Function function) {
 		return matchRe(text, function, false);
 	}
 	
 	public Match matchRe(String text, Function function, Boolean ignoreCase) {
 		return matchRe(text, function, ignoreCase, null);
 	}
 	
 	public Match matchRe(String text, Function function, Boolean ignoreCase, Scriptable object) {
 		Match m = new RegexMatch(text, ignoreCase);
 		m.setAttribute(CALLBACK, function);
 		m.setAttribute(USER_OBJECT, object);
 		
 		return m;
 	}
 	
 	public void stop() {
 		commands.stop();
 	}
 }
