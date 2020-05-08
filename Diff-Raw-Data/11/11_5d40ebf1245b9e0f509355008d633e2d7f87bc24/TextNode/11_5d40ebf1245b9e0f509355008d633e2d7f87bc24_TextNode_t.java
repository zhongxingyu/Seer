 package com.tysanclan.site.projectewok.util.bbcode.ast;
 
 import com.tysanclan.site.projectewok.util.bbcode.BBAstNode;
 import com.tysanclan.site.projectewok.util.bbcode.DefaultNode;
 
 public class TextNode extends DefaultNode {
 
 	private final String text;
 
 	public TextNode(BBAstNode parent, String text) {
 		super(parent);
 
		this.text = text;
 	}
 
 	@Override
 	public void renderTo(StringBuilder builder) {
 		builder.append(text);
 	}
 
 }
