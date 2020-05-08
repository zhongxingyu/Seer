 package tschumacher.rally.el.engine;
 
 import java.util.Stack;
 import tschumacher.rally.el.Context;
 import tschumacher.rally.el.Template;
 
 /**
  *
  * @author Timothy Schumacher, Ph.D. <schumact@gmail.com>
  */
 public class DefaultEngine implements tschumacher.rally.el.Engine {
 
 	public String evaluate(Template template, Context context) {
 		if(template == null) {
 			throw new tschumacher.rally.el.Exception("The template was null.");
 		}
 
 		if(context == null) {
 			throw new tschumacher.rally.el.Exception("The context was null.");
 		}
 
 		final Stack<StringBuilder> stack = new Stack<StringBuilder>();
 		stack.push(new StringBuilder());
 
 		for(int i=0;i<template.length();i++) {
 			char c = template.charAt(i);
 			switch(c) {
 				case '}': 
 					if(stack.size() > 1) {
 						StringBuilder top = stack.pop();
 						final String KEY = top.toString().trim();
 						Object value = context.getAttribute(KEY);
 						if(value != null) {
 							stack.peek().append(value.toString());
 						} else {
 							throw new tschumacher.rally.el.Exception("property " + KEY + " could not be located in any context");
 						}
 					} else {
 						stack.peek().append(c);
 					}
 					break;
 
 				case '$': // eat the control character and process the next one:
 					try {
 						c = template.charAt(++i);
 					} catch(IndexOutOfBoundsException oobe) {
 						throw new tschumacher.rally.el.Exception("error parsing expression, ended with $", oobe);
 					}
 					switch(c) {
 						case '{': stack.push(new StringBuilder()); break;
 						default : stack.peek().append(c); break;
 					}
 					break;
 					
 				default: 
 					stack.peek().append(c);
 					break;
 			}
 		}
		if(stack.size() > 1) {
			throw new tschumacher.rally.el.Exception("error parsing input.  malformed expression [unclosed '${'?]");
		}
 		return stack.pop().toString();
 	}
 }
