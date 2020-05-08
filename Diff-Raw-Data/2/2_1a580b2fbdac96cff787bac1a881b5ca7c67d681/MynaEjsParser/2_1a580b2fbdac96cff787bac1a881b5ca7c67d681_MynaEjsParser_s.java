 // $ANTLR 3.0.1 /home/mark/myna/antlr/MynaEjs.g 2007-09-21 11:54:17
 
 /**
 * This class parses a supplied string to translate 
 * Embedded JavaScript to server-side JavaScript. 
 *
 
 */
 package info.emptybrain.myna;
 import java.util.Stack;
 import java.util.Properties;
 import java.util.regex.*;
 import org.mozilla.javascript.*;
 
 
 public class MynaEjsParser {
 	/* text modes */
 	public final int EJS=1;
 	public final int SJS=2;
 	public final int EVAL=3;
 	public final int COMMENT=4;
 	public final int TICK_LITERAL=5;
 	public final int QUOTE_LITERAL=6;
 	
 	public Stack 		textMode = new java.util.Stack(); // for appendText
 	public StringBuffer text = new java.lang.StringBuffer();
 	public StringBuffer script = new java.lang.StringBuffer();
 	public String scriptPath = null;
 	
 	/* these are for dealing with @tag macros (@loop, @if, etc..)*/
 	public Stack 		loopStack = new java.util.Stack(); // for @loop
 	/* for @if. Each node is at type of  "IF" or ""ELSE" */
 	public Stack 		ifStack = new java.util.Stack();  
 	//boolean inElse=false; //true if in @else block. Used to check for @elseif after @else
 	
 	
 	public MynaEjsParser() {;}
 	
 /** parseString
 * This is the main entry point. Takes a string and the path to the file the 
 * string was loaded from and returns a plain SJS string with all EJS and macros 
 * translated. If the path ends in ".ejs", then the file is assumed to be EJS text
 *
 * @param  jsText Javascript Text to parse
 * @param  path of of file that produced the supplied text. Used in error messages, and to determine type of text
 * @return translated SJS only text
 */
 	public String  parseString(String jsText, String path) throws EvaluatorException{
 		this.scriptPath = path;
 		
 		String  	jsString = new java.lang.String(jsText);
 		String 		tag=null; 
 		int 		length = jsString.length();
 		int 		pointer=-1;
 		String 		c1 = null;
 		String 		c2 = null;
 		String 		c3 = null;
 		String 		c4 = null;
 		
 		int startTextMode = SJS;
 		if (scriptPath.matches(".*.ejs")){
 			startTextMode = EJS;
 		} 
 		textMode.push(new Integer(startTextMode));
 		
 		while ( ++pointer < length){
 			c1 = jsString.substring(pointer,pointer+1);
 			
 			/* tag begin or end '<' */
 			if (c1.equals("<")){
 				c2 = jsString.substring(++pointer,pointer+1);
 				/* block, eval or comment '<%(=|--)' */
 				if (c2.equals("%")){
 					c3 = jsString.substring(++pointer,pointer+1);
 					/* eval '<%=' */
 					if (c3.equals("=")){
 						evalBegin(c1+c2+c3);
 					/* comment or text */
 					} else if (c3.equals("-") ) {
 						c4 = jsString.substring(++pointer,pointer+1);
 						/* comment '<%--' */
 						if  (c4.equals("-") ) {
 							commentBegin(c1+c2+c3+c4);
 							++pointer;
 						/* no match */
 						} else {
 							pointer-=3;
 						}
 					/* SJS begin '<%' */
 					} else {
 						sjsBegin(c1+c2);
 						--pointer;
 					}
 				/* tag end '</' */
 				} else if (c2.equals("/")){
 					/* ejsEnd '</ejs>' */
 					if (pointer + 5 < length && jsString.substring(pointer+1,pointer + 5).toLowerCase().equals("ejs>")){
 						ejsEnd(c1+c2);
 						pointer += 4;
 					} else {
 						c3 = jsString.substring(++pointer,pointer+1);
 						/* @macro end */
 						if (c3 .equals("@")){
 							/* @loop end '</@loop>' */
 							if (pointer + 5 < length && jsString.substring(pointer+1,pointer + 5).toLowerCase().equals("loop")){
 								tag = jsString.substring(pointer-2,(pointer=jsString.indexOf(">",pointer) ));
 								atLoopEnd(tag);
 							/* @if end '</@if>' */
 							} else if (pointer + 3 < length && jsString.substring(pointer+1,pointer + 3).toLowerCase().equals("if")){
 								tag = jsString.substring(pointer-2,(pointer=jsString.indexOf(">",pointer) ));
 								atIfEnd(tag);
 							} else {
 								pointer-=2;
 								text.append(c1);
 							}
 						/* no match */
 						} else {
 							pointer-=2;
 							text.append(c1);
 						}
 					}
 				/* ejs begin */
 				} else if (c2.equals("e")){
 					if (pointer + 4 < length && jsString.substring(pointer+1,pointer + 4).toLowerCase().equals("js>")){
 						ejsBegin(c1+c2);
 						pointer += 3;
 					/* no match */
 					} else {
 						pointer-=1;
 						text.append(c1);
 					}
 				/* @macro begin */
 				} else if (c2.equals("@")){
 					/* @loop */
 					if (pointer + 5 < length && jsString.substring(pointer+1,pointer + 5).toLowerCase().equals("loop")){
 						tag = jsString.substring(pointer-1,(pointer=jsString.indexOf(">",pointer) ));
 						atLoopBegin(tag);
 					/* @if */
 					} else if (pointer + 3 < length && jsString.substring(pointer+1,pointer + 3).toLowerCase().equals("if")){
 						tag = jsString.substring(pointer-1,(pointer=jsString.indexOf(">",pointer) ));
 						atIfBegin(tag);
 					/* @else(if) */
 					} else if (pointer + 5 < length && jsString.substring(pointer+1,pointer + 5).toLowerCase().equals("else")){
 						/* @elseif */
 						if (pointer + 7 < length && jsString.substring(pointer+5,pointer + 7).toLowerCase().equals("if")){
 							tag = jsString.substring(pointer-1,(pointer=jsString.indexOf(">",pointer) ));
 							atElseIf(tag);
 						/* @else */
 						} else {
 							tag = jsString.substring(pointer-1,(pointer=jsString.indexOf(">",pointer) ));
 							atElse(tag);	
 						}
 					/* @set '<@set' */
 					} else if (pointer + 4 < length && jsString.substring(pointer+1,pointer + 4).toLowerCase().equals("set")){
 						tag = jsString.substring(pointer,(pointer=jsString.indexOf(">",pointer) ));
 						atSet(tag);
 					/* @include '<@includeOnce' */
 					} else if (pointer + 12 < length && jsString.substring(pointer+1,pointer + 12).toLowerCase().equals("includeonce")){
 						tag = jsString.substring(pointer+9,(pointer=jsString.indexOf(">",pointer) ));
 						atIncludeOnce(tag);
 					/* @include '<@include' */
 					} else if (pointer + 8 < length && jsString.substring(pointer+1,pointer + 8).toLowerCase().equals("include")){
 						tag = jsString.substring(pointer+5,(pointer=jsString.indexOf(">",pointer) ));
 						atInclude(tag);
 					/* no match */	
 					} else {
 						pointer-=1;
 						text.append(c1);
 					}
 				/* no match */
 				} else {
 					pointer-=1;
 					text.append(c1);
 				}
 			
 			} else if (c1.equals("%")){
 				c2 = jsString.substring(++pointer,pointer+1);
 				/* end sjs */
 				if (c2.equals(">")){
					if (pointer+2 < length && jsString.substring(pointer+1,pointer+2).equals("\n")){
 						pointer++;
 						text.append("\n");
 						
 					} 
 					sjsEnd(c1+c2);
 				} else {
 					pointer--;
 					text.append(c1);
 				}
 			} else if (c1.equals("-")){
 				c2 = jsString.substring(++pointer,pointer+1);
 				if (c2.equals("-")){
 					c3 = jsString.substring(++pointer,pointer+1);
 					if (c3.equals("%")){
 						c4 = jsString.substring(++pointer,pointer+1);
 						if (c4.equals(">")){
 							commentEnd(c1+c2+c3+c4);
 						} else {
 							pointer-=3;
 							text.append(c1);
 						}
 					} else {
 						pointer-=2;
 						text.append(c1);		
 					}
 				} else {
 					pointer--;
 					text.append(c1);	
 				}
 			} else  {
 				////System.err.println(c1+"<hr>");
 				text.append(c1);
 				
 			}
 		}
 		appendText();
 		
 		/* some extra error handling I'm thinking about */
 		/* if (mode.size() > 1){
 			if (curMode() == SJS){
 				if (textMode.size() == 0){
 					throw new EvaluatorException(
 						"Missing closing '%>'",
 						scriptPath,
 						curLine()
 					 );
 				}	
 			}
 		} */
 		
 		return new String(script.toString());	
 	}
 	
 	
 /** appendText
 *	handles text tokens. Basically any charachters between the known tokens
 */
 	public void appendText(){
 		 String [] lines  = text.toString().split("\n",-1);
 		 int i;
 		
 		
 		switch (curTextMode()){
 			case COMMENT: 		
 				//script.append("/* " + text.toString() + " */\n");
 				for (i =0;i<lines.length;++i){
 					if (i < lines.length -1) {
 						script.append("\n");	
 					} 
 				}
 			break;
 			case EJS: 		
 				for (i =0;i<lines.length;++i){
 					if (textMode.size() ==1){
 						script.append("$res.print('" + jsEscape(lines[i]));
 					}else {
 						script.append("___EJS_BUFFER___.push('" + jsEscape(lines[i]));
 					}
 					if (i < lines.length -1) {
 						script.append( "\\n');\n");	
 					} else {
 						script.append( "');");
 					}
 				}
 			break;
 			case SJS: 		
 				script.append(text);
 			break;
 			case EVAL:
 				if (textMode.size() ==2 && ((Integer)(textMode.elementAt(0))).equals(EJS)){
 					script.append("$res.print(String(" + text + "));");
 				} else {
 					script.append("___EJS_BUFFER___.push(String(" + text + "));");
 				}
 			break;
 			
 		}
 		text.setLength(0);
 	}
 	
 /** curLine
 *	 returns the current line of the script we are processing. Usefull for errors
 * @return  the current line of the script
 */
 	public int curLine(){
 		return script.toString().split("\n").length;
 	}
 	
 /** sjsBegin
 *	handles SJS begin tags
 * @param tag text of the begin sjs tag
 */
 	public void sjsBegin(String tag){
 		if (curTextMode() != EJS) return;
 		appendText();
 		textMode.push(new Integer(SJS));
 		//System.err.println("sjsBegin " + tag );	
 	}
 /** evalBegin
 *	handles eval begin tags
 * @param tag text of the begin eval tag
 */
 	public void evalBegin(String tag){
 		if (curTextMode() != EJS) return;
 		appendText();
 		textMode.push(new Integer(EVAL));
 		//System.err.println("evalBegin " + tag );	
 	}
 /** sjsEnd
 *	handles SJS end tags
 * @param tag text of the end sjs tag
 */
 	public void sjsEnd(String tag){
 		if ((curTextMode() != SJS && curTextMode() != EVAL) || textMode.size() == 1) return;
 		//if (nextChar.equals("\n")) text.append("\n");
 		appendText();
 		
 		textMode.pop();
 		if (textMode.size() == 0){
 			throw new EvaluatorException(
 				"Closing '%>'  without opening '<%' ",
 				scriptPath,
 				curLine()
 			 );
 		}
 		//System.err.println("sjsEnd " + tag );	
 	}
 	
 /** commentBegin
 *	handles comment begin tags
 * @param tag text of the begin comment tag
 */
 	public void commentBegin(String tag){
 		if (curTextMode() != EJS) return;
 		appendText();
 		textMode.push(new Integer(COMMENT));
 		//System.err.println("commentBegin " + tag );	
 	}
 	
 /** commentEnd
 *	handles comment end tags
 * @param tag text of the end comment tag
 */
 	public void commentEnd(String tag){
 		if (curTextMode() != COMMENT) return;
 		appendText();
 		textMode.pop();
 		//System.err.println("commentEnd " + tag );	
 	}
 	
 /** ejsBegin
 *	handles EJS  begin tags
 * @param tag text of the begin EJS tag
 */
 	public void ejsBegin(String tag){
 		if (curTextMode() != SJS) return;
 		appendText();
 		
 		textMode.push(new Integer(EJS));
 		//script.append("function(){var originalContent=$res.clear();");
 		script.append("function(__EJS_RES__,__EJS_MYNA__){var ___EJS_BUFFER___=[];var $res = __EJS_RES__.applyTo({print:function(text){___EJS_BUFFER___.push(text)}});var Myna = __EJS_MYNA__.applyTo({res:$res});");
 			
 		//System.err.println("ejsBegin <br>");	
 	}
 /** ejsEnd
 *	handles EJS  end tags
 * @param tag text of the end EJS tag
 */
 	public void ejsEnd(String tag){
 		if (curTextMode() != EJS) return;
 		appendText();
 		
 		//script.append("var newContent=$res.clear();$res.print(originalContent);return newContent;}.apply(this)");
 		script.append("return ___EJS_BUFFER___.join('').trimIndent().trim();}.call(this,$res,Myna)");
 		textMode.pop();
 		if (textMode.size() == 0){
 			throw new EvaluatorException(
 				"Closing ejs tag without opening ejs tag",
 				scriptPath,
 				curLine()
 			 );
 		}
 		//System.err.println("ejsEnd <br>");	
 	}
 /** atSet
 *	handles @set tags
 * @param tag text of the @set tag
 */
 	public void atSet(String tag) throws EvaluatorException{
 		if (curTextMode() != EJS) return;
 		appendText();
 		
 		//clear any semicolons
 		tag =tag.substring(4).replaceAll(";","");
 		
 		script.append("var " + tag + ";");
 			
 		
 		//System.err.println("atSet " + tag );	
 	}
 	
 /** atInclude
 *	handles @include tags
 * @param tag text of the @include tag
 */
 	public void atInclude(String tag) throws EvaluatorException{
 		if (curTextMode() != EJS) return;
 		appendText();
 		
 		//clear any semicolons
 		tag =tag.substring(4).replaceAll("\"","").replaceAll("'","").trim();
 		
 		script.append("Myna.include(\"" + tag + "\");");
 	}
 /** atIncludeOnce
 *	handles @include tags
 * @param tag text of the @includeOnce tag
 */
 	public void atIncludeOnce(String tag) throws EvaluatorException{
 		if (curTextMode() != EJS) return;
 		appendText();
 		
 		//clear any semicolons
 		tag =tag.substring(4).replaceAll("\"","").replaceAll("'","").trim();
 		
 		script.append("Myna.includeOnce(\"" + tag + "\");");
 	}
 	
 /** atLoopBegin
 *	handles @loop  begin tags
 * @param tag text of the begin @loop tag
 */
 	public void atLoopBegin(String tag) throws EvaluatorException{
 		if (curTextMode() != EJS) return;
 		appendText();
 		
 		Properties attributes = getTagAttributes(tag);
 		
 		if (attributes.getProperty("array") == null){
 			throw new EvaluatorException(
 				"@loop tag requires attribute 'array' ",
 				scriptPath,
 				curLine()
 			 );	
 		}
 		if (attributes.getProperty("element") == null){
 			throw new EvaluatorException(
 				"@loop tag requires attribute 'element' ",
 				scriptPath,
 				curLine()
 			 );	
 		}
 		
 		script.append(attributes.getProperty("array") + ".forEach(function(" + attributes.getProperty("element"));
 		if (attributes.getProperty("index") != null){
 			script.append("," + attributes.getProperty("index"));
 		}
 		script.append("){");
 		loopStack.push(new Integer(curLine()));	
 		
 		//System.err.println("atLoopBegin " + tag );	
 	}
 	
 /** atLoopEnd
 *	handles @loop  end tags
 * @param tag text of the end @loop tag
 */
 	public void atLoopEnd(String tag){
 		if (curTextMode() != EJS) return;
 		appendText();
 		
 		if (loopStack.size() ==0){
 			throw new EvaluatorException(
 				"Closing @loop tag without opening @loop tag",
 				scriptPath,
 				curLine()
 			 );	
 		}
 		
 		script.append("});/* end @loop */");
 		loopStack.pop();
 		//System.err.println("atLoopEnd " + tag );	
 	}
 
 	
 /** atIfBegin
 *	handles @if  begin tags
 * @param tag text of the begin @if tag
 */
 	public void atIfBegin(String tag) throws EvaluatorException{
 		if (curTextMode() != EJS) return;
 		appendText();
 		tag = tag
 			.substring(4)
 			.replaceAll("\\s[gG][tT]\\s"," > ")
 			.replaceAll("\\s[gG][tT][eE]\\s"," >= ")
 			.replaceAll("\\s[lL][tT]\\s"," < ")
 			.replaceAll("\\s[lL][tT][eE]\\s"," <= ");
 		script.append("if (" + tag +"){");
 		
 		ifStack.push("IF");	
 		
 		//System.err.println("atIfBegin " + tag );	
 	}
 
 
 
 /** atElseIf
 *	handles @elseif  tags
 * @param tag text of the @elseif tag
 */
 	public void atElseIf(String tag) throws EvaluatorException{
 		if (curTextMode() != EJS) return;
 		appendText();
 		tag = tag.substring(8)
 			.replaceAll("\\s[gG][tT]\\s"," > ")
 			.replaceAll("\\s[gG][tT][eE]\\s"," >= ")
 			.replaceAll("\\s[lL][tT]\\s"," < ")
 			.replaceAll("\\s[lL][tT][eE]\\s"," <= ");
 		
 		if (ifStack.size() ==0){
 			throw new EvaluatorException(
 				"@elseif tag without opening @if tag",
 				scriptPath,
 				curLine()
 			 );	
 		}
 		if (ifStack.peek().toString().equals("ELSE")){
 			throw new EvaluatorException(
 				"@elseif tag cannot be declared after @else",
 				scriptPath,
 				curLine()
 			 );
 		}
 		
 		
 		script.append("}else if (" + tag +"){");
 		
 		ifStack.push(new Integer(curLine()));	
 		
 		//System.err.println("atElseIf " + tag );	
 	}
 
 
 
 /** atElse
 *	handles @else  begin tags
 * @param tag text of the  @else tag
 */
 	public void atElse(String tag) throws EvaluatorException{
 		if (curTextMode() != EJS) return;
 		appendText();
 		
 		if (ifStack.size() ==0){
 			throw new EvaluatorException(
 				"@else tag without opening @if tag",
 				scriptPath,
 				curLine()
 			 );	
 		}
 		
 		script.append("} else {");
 		
 		ifStack.pop();	
 		ifStack.push("ELSE");		
 		
 		//System.err.println("atElse " + tag );	
 	}
 
 
 /** atIfEnd
 *	handles @if  end tags
 * @param tag text of the end @if tag
 */
 	public void atIfEnd(String tag){
 		if (curTextMode() != EJS) return;
 		appendText();
 		
 		if (ifStack.size() ==0){
 			throw new EvaluatorException(
 				"Closing @if tag without opening @if tag",
 				scriptPath,
 				curLine()
 			 );	
 		}
 		
 		script.append("}/* end @if */");
 		ifStack.pop();
 		//System.err.println("atIfEnd " + tag );	
 	}
 	
 /** curTextMode
 * @return the int constant that represents the current Text Mode
 */	
 	public int curTextMode(){
 		return ((Integer)textMode.peek()).intValue(); 
 	}
 
 /** jsEscape
 * @param text String that may contain characters such as \n or " that are not allowed in JS literals
 * @return supplied text with invalid characters escaped
 */
 	public String jsEscape(String  text) {
 		return text
 			.replaceAll("(\\\\)","$1$1")
 			.replaceAll("\n","\\\\n")
 			.replaceAll("\r","\\\\r")
 			.replaceAll("'","\\\\'")
 			.replaceAll("\"","\\\\\\\"");
 			
 	}
 	
 /** getTagAttributes
 * @param text tag text, such as <@loop array="data" element="curRow" index="i"> 
 * @return Properties file that representes each attribute key and value as Strings
 */
 	public Properties getTagAttributes(String text){
 		Matcher attributeRegEx=Pattern.compile("\\s*(\\w*)\\s*=[\"|']([^\"|']+)[\"|'][\\s|>]*",Pattern.CASE_INSENSITIVE).matcher(text);
 		Properties result = new Properties();
 		while (attributeRegEx.find()){
 			result.setProperty(attributeRegEx.group(1).toLowerCase(),attributeRegEx.group(2));
 		}
 		return result;
 	}
 
 }
