 package de.htwg.mgse.formular.model;
 
 public class Input extends Element {
 
 	private InputType type;
 	private String defaultValue;
 
 	public Input(String id) {
 		this.id = id;
 	}
 
 	public InputType getType() {
 		return type;
 	}
 
 	public String getDefaultValue() {
 		return defaultValue;
 	}
 
 	public void setType(InputType type) {
 		this.type = type;
 	}
 
 	public void setDefaultValue(String defaultValue) {
 		this.defaultValue = defaultValue;
 	}
 	
 	public String toHtml() {
 		String prefix = "\t" + label + ":<br />\n";
		String intCheck = "onkeypress=\"return (function(e){var c = (e.which) ? e.which : event.keyCode; return !(c != 0 && c != 47 && c != 37 && c != 39 && c > 31 && (c < 48 || c > 57)); })(event)\"";
 		switch (type) {
 		case PASSWORD:
 			return prefix + "\t<input name=\"" + id + "\" value=\"" + defaultValue + "\" type=\"password\" /><br />\n";
 		case INT:
 			return prefix + "\t<input name=\"" + id + "\" value=\"" + defaultValue + "\" type=\"text\" " + intCheck + " /><br />\n";
 		default:
 			return prefix + "\t<input name=\"" + id + "\" value=\"" + defaultValue + "\" type=\"text\" /><br />\n";
 		}
 	}
 
 }
