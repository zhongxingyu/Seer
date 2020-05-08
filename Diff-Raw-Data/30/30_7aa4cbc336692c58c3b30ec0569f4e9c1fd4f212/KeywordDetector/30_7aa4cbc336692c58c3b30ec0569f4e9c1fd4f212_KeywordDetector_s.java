 package com.patrick_vane.unrealscript.editor.default_classes;
 
 import org.eclipse.jface.text.rules.IWordDetector;
 
 
 public class KeywordDetector implements IWordDetector
 {
 	protected static KeywordDetector instance = new KeywordDetector();
 	
 	
 	protected KeywordDetector()
 	{
 	}
 	
 	public static KeywordDetector getSharedInstance()
 	{
 		return instance;
 	}
 	
 	
 	@Override
	public boolean isWordPart( char c )
 	{
		return ((c == '_') || Character.isLetterOrDigit(c));
 	}
 	
 	@Override
	public boolean isWordStart( char c )
 	{
		return ((c == '_') || Character.isLetter(c));
 	}
 }
