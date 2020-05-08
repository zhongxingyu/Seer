 package com.patrick_vane.unrealscript.editor.parser;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import org.eclipse.jface.text.IDocument;
 import com.patrick_vane.unrealscript.editor.UnrealScriptEditor;
 import com.patrick_vane.unrealscript.editor.class_hierarchy.TypeHierarchyView;
 import com.patrick_vane.unrealscript.editor.class_hierarchy.parser.UnrealScriptClass;
 import com.patrick_vane.unrealscript.editor.constants.WordConstant;
 import com.patrick_vane.unrealscript.editor.parser.CodeWord.CodeWordData;
 
 
 public class UnrealScriptAdvancedParser
 {
 	public static class ClassOrAttribute
 	{
 		/** The word that was found within the document with the given offset */
 		public final CodeWord			word;
 		
 		/** Found class */
 		public final UnrealScriptClass	unrealscriptClass;
 		/** Found attribute */
 		public final CodeAttribute		attribute;
 		
 		public ClassOrAttribute( CodeWord word, UnrealScriptClass unrealscriptClass )
 		{
 			this.word = word;
 			this.unrealscriptClass = unrealscriptClass;
 			this.attribute = null;
 		}
 		public ClassOrAttribute( CodeWord word, CodeAttribute attribute )
 		{
 			this.word = word;
 			this.unrealscriptClass = null;
 			this.attribute = attribute;
 		}
 		
 		public boolean isClass()
 		{
 			return (unrealscriptClass != null);
 		}
 		public boolean isAttribute()
 		{
 			return (attribute != null);
 		}
 	}
 	
 	
 	private final static HashMap<String,UnrealScriptAttributes>		lastAttributes 	= new HashMap<String,UnrealScriptAttributes>();
 	private final static HashMap<String,Long>						lastTime 		= new HashMap<String,Long>();
 	
 	
 	// parse line >>
 		/** Returns null if it can't find anything */
 		public static CodeWordData getWordAt( IDocument document, int offset )
 		{
 			return getWordAt( document, offset, false );
 		}
 		/** Returns null if it can't find anything */
 		public static CodeWordData getWordAt( IDocument document, int offset, boolean cutWordOffTillOffset )
 		{
 			if( document == null )
 				return null;
 			
 			try
 			{
 				int min = 0;
 				int max = (cutWordOffTillOffset ? Math.min(offset,document.getLength()) : document.getLength());
 				
 				int start  = clamp( offset-40, min, max );
 				int length = clamp( 80, 0, max-start );
 				CodeWord[] words = UnrealScriptParser.parsePartWords( document.get(start, length), start );
 				CodeWord[] line  = UnrealScriptParser.parseLine( document.get(), offset );
 				
 				int pos = offset;
 				int lastPos = 0;
 				
 				CodeWord word = null;
 				for( int i=0; i<words.length; i++ )
 				{
 					word = words[i];
 					
 					if( isBetween(pos, word.getFirstCharacterPosition(), word.getLastCharacterPosition()) )
 					{
 						int inLineArrayPos = -1;
 						CodeWord[] currentLine = line;
 						for( int j=0; j<currentLine.length; j++ )
 						{
 							if( currentLine[j].getFirstCharacterPosition() == word.getFirstCharacterPosition() )
 							{
 								inLineArrayPos = j;
 								break;
 							}
 						}
 						
 						if( inLineArrayPos >= 0 )
 						{
 							boolean function = false;
 							if( inLineArrayPos+1 < currentLine.length )
 							{
 								CodeWord after = currentLine[inLineArrayPos+1];
 								if( after != null )
 								{
 									function = "(".equals( after.getWord() );
 								}
 							}
 							
 							return new CodeWordData( word, currentLine, inLineArrayPos, function );
 						}
 					}
 					
 					if( isBetween(pos, lastPos, word.getFirstCharacterPosition()) )
 					{
 						int inLineArrayPos = -1;
 						CodeWord[] currentLine = line;
 						for( int j=0; j<currentLine.length; j++ )
 						{
 							if( currentLine[j].getFirstCharacterPosition() == word.getFirstCharacterPosition() )
 							{
 								inLineArrayPos = j;
 								break;
 							}
 						}
 						
 						if( inLineArrayPos >= 0 )
 						{
 							CodeWord newWord = new CodeWord( pos );
 							newWord.close( pos );
 							
 							boolean added = false;
 							ArrayList<CodeWord> newLine = new ArrayList<CodeWord>();
 							for( CodeWord lineWord : currentLine )
 							{
 								if( !added )
 								{
 									if( newWord.getFirstCharacterPosition() < lineWord.getFirstCharacterPosition() )
 									{
 										inLineArrayPos = newLine.size();
 										newLine.add( newWord );
 										added = true;
 									}
 								}
 								newLine.add( lineWord );
 							}
 							if( !added )
 							{
 								inLineArrayPos = newLine.size();
 								newLine.add( newWord );
 								added = true;
 							}
 							
 							return new CodeWordData( newWord, newLine.toArray(new CodeWord[0]), inLineArrayPos, false );
 						}
 					}
 					
 					lastPos = word.getLastCharacterPosition();
 				}
 				
 				if( word != null )
 				{
 					int inLineArrayPos = -1;
 					CodeWord[] currentLine = line;
 					for( int j=0; j<currentLine.length; j++ )
 					{
 						if( currentLine[j].getFirstCharacterPosition() == word.getFirstCharacterPosition() )
 						{
 							inLineArrayPos = j;
 							break;
 						}
 					}
 					
 					if( inLineArrayPos >= 0 )
 					{
 						CodeWord newWord = new CodeWord( pos );
 						newWord.close( pos );
 						
 						boolean added = false;
 						ArrayList<CodeWord> newLine = new ArrayList<CodeWord>();
 						for( CodeWord lineWord : currentLine )
 						{
 							if( !added )
 							{
 								if( newWord.getFirstCharacterPosition() < lineWord.getFirstCharacterPosition() )
 								{
 									inLineArrayPos = newLine.size();
 									newLine.add( newWord );
 									added = true;
 								}
 							}
 							newLine.add( lineWord );
 						}
 						if( !added )
 						{
 							inLineArrayPos = newLine.size();
 							newLine.add( newWord );
 							added = true;
 						}
 						
 						return new CodeWordData( newWord, newLine.toArray(new CodeWord[0]), inLineArrayPos, false );
 					}
 				}
 			}
 			catch( Exception e )
 			{
 				e.printStackTrace();
 			}
 			
 			CodeWord newWord = new CodeWord( offset );
 			newWord.close( offset );
 			return new CodeWordData( newWord, new CodeWord[]{newWord}, 0, false );
 		}
 	// parse line <<
 	
 	
 	// get class or attribute >>
 		/** Returns null if it can't find anything */
 		public static ClassOrAttribute getClassOrAttributeAt( IDocument document, int offset )
 		{
 			return getClassOrAttributeAt( getWordAt(document, offset) );
 		}
 		/** Returns null if it can't find anything */
 		public static ClassOrAttribute getClassOrAttributeAt( CodeWordData word )
 		{
 			if( word == null )
 				return null;
 			return getClassOrAttributeAt( word.word, word.line, word.inLineArrayPos, word.function );
 		}
 		/** Returns null if it can't find anything */
 		public static ClassOrAttribute getClassOrAttributeAt( CodeWord word, CodeWord[] line, int inLineArrayPos )
 		{
 			if( line != null )
 			{
 				boolean function = false;
 				if( inLineArrayPos+1 < line.length )
 				{
 					CodeWord after = line[inLineArrayPos+1];
 					if( after != null )
 					{
 						function = "(".equals( after.getWord() );
 					}
 				}
 				return getClassOrAttributeAt( word, line, inLineArrayPos, function );
 			}
 			return null;
 		}
 		/** Returns null if it can't find anything */
 		public static ClassOrAttribute getClassOrAttributeAt( CodeWord word, CodeWord[] line, int inLineArrayPos, boolean function )
 		{
 			UnrealScriptClass usingClass = UnrealScriptEditor.getActiveUnrealScriptClass();
 			boolean canBeClass = true;
 			
 			ArrayList<CodeWordData> parents = new ArrayList<CodeWordData>();
 			CodeWordData parent = word.getParentWord( line, inLineArrayPos );
 			while( parent != null )
 			{
 				parents.add( parent );
 				parent = parent.getParentWord();
 			}
 			
 			for( int i=parents.size()-1; i>=0; i-- )
 			{
 				parent = parents.get( i );
 				
 				if( i == parents.size()-1 )
 				{
 					CodeAttributeVariable localVariable = getLocalVariable( usingClass.getName(), parent.word.getWord(), parent.inLineArrayPos, parent.line, parent.function );
 					UnrealScriptClass newLocalVariableClass = ((localVariable == null) ? null : getClass(localVariable.getType()));
 					if( newLocalVariableClass != null )
 					{
 						usingClass = newLocalVariableClass;
 						canBeClass = false;
 						continue;
 					}
 				}
 				
 				boolean startWithClass = canBeClass;
 				if( canBeClass )
 				{
 					if( parent.word.getWord().length() > 0 )
 					{
 						char firstChar = parent.word.getWord().charAt( 0 );
 						if( firstChar == Character.toLowerCase(firstChar) ) // firstChar is lower case
 						{
 							startWithClass = false;
 						}
 					}
 				}
 				
 				if( startWithClass )
 				{
 					UnrealScriptClass newClass = getClass( parent.word.getWord() );
 					if( newClass != null )
 					{
 						usingClass = newClass;
 						canBeClass = false;
 						continue;
 					}
 					else
 					{
 						CodeAttribute newAttribute = getAttribute( usingClass.getName(), parent.word.getWord(), parent.inLineArrayPos, parent.line, parent.function );
 						UnrealScriptClass newAttributeClass = ((newAttribute == null) ? null : getClass(newAttribute.getType()));
 						if( newAttributeClass != null )
 						{
 							usingClass = newAttributeClass;
 							canBeClass = false;
 							continue;
 						}
 						else
 						{
 							break;
 						}
 					}
 				}
 				else
 				{
 					CodeAttribute newAttribute = getAttribute( usingClass.getName(), parent.word.getWord(), parent.inLineArrayPos, parent.line, parent.function );
 					UnrealScriptClass newAttributeClass = ((newAttribute == null) ? null : getClass(newAttribute.getType()));
 					if( newAttributeClass != null )
 					{
 						usingClass = newAttributeClass;
 						canBeClass = false;
 						continue;
 					}
 					else if( canBeClass )
 					{
 						UnrealScriptClass newClass = getClass( parent.word.getWord() );
 						if( newClass != null )
 						{
 							usingClass = newClass;
 							canBeClass = false;
 							continue;
 						}
 						else
 						{
 							break;
 						}
 					}
 					else
 					{
 						break;
 					}
 				}
 			}
 			
 			
 			if( usingClass == null )
 				return null;
 			
 			
			if( canBeClass )
			{
				CodeAttributeVariable localVariable = getLocalVariable( usingClass.getName(), word.getWord(), inLineArrayPos, line, function );
				if( localVariable != null )
					return new ClassOrAttribute( word, localVariable );
			}
			
 			boolean startWithClass = canBeClass;
 			if( canBeClass )
 			{
 				if( word.getWord().length() > 0 )
 				{
 					char firstChar = word.getWord().charAt( 0 );
 					if( firstChar == Character.toLowerCase(firstChar) ) // firstChar is lower case
 					{
 						startWithClass = false;
 					}
 				}
 			}
 			
 			if( startWithClass )
 			{
 				UnrealScriptClass unrealscriptClass = getClass( word.getWord() );
 				if( unrealscriptClass != null )
 					return new ClassOrAttribute( word, unrealscriptClass );
 				
 				CodeAttribute attribute = getAttribute( usingClass.getName(), word.getWord(), inLineArrayPos, line, function );
 				if( attribute != null )
 					return new ClassOrAttribute( word, attribute );
 			}
 			else
 			{
 				CodeAttribute attribute = getAttribute( usingClass.getName(), word.getWord(), inLineArrayPos, line, function );
 				if( attribute != null )
 					return new ClassOrAttribute( word, attribute );
 				
 				if( canBeClass )
 				{
 					UnrealScriptClass unrealscriptClass = getClass( word.getWord() );
 					if( unrealscriptClass != null )
 						return new ClassOrAttribute( word, unrealscriptClass );
 				}
 			}
 			
 			return null;
 		}
 		
 		
 		public static HashMap<String,UnrealScriptClass> getClasses()
 		{
 			if( TypeHierarchyView.isRootFromThisProject() )
 				return TypeHierarchyView.getClasses();
 			return new HashMap<String,UnrealScriptClass>();
 		}
 		public static UnrealScriptAttributes getAttributes( String className )
 		{
 			if( className == null )
 				return new UnrealScriptAttributes();
 			className = className.toLowerCase();
 			
 			Long newLastTime = lastTime.get( className );
 			if( (newLastTime == null) || (System.currentTimeMillis()-newLastTime >= 3000) )
 			{
 				lastTime.put( className, newLastTime );
 				lastAttributes.put( className, UnrealScriptEditor.getUnrealScriptAttributesWithParents(className) );
 			}
 			return lastAttributes.get( className );
 		}
 		
 		public static HashMap<String,CodeAttributeVariable> getLocalVariables( String className, int positionInsideFunction )
 		{
 			return UnrealScriptParser.parseFunctionParametersAndLocalVariables( className, positionInsideFunction );
 		}
 		
 		
 		public static UnrealScriptClass getClass( String word )
 		{
 			return UnrealScriptEditor.getUnrealScriptClass( word );
 		}
 		
 		public static CodeAttribute getAttribute( String className, String word, int inLineArrayPos, CodeWord[] line, boolean function )
 		{
 			UnrealScriptAttributes attributes = getAttributes( className );
 			if( function )
 				return getParentAttributeIfNeeded( attributes.getAttributeFunction(word), word, inLineArrayPos, line );
 			else
 				return getParentAttributeIfNeeded( attributes.getAttributeVariable(word), word, inLineArrayPos, line );
 		}
 		
 		public static CodeAttributeVariable getLocalVariable( String className, String word, int inLineArrayPos, CodeWord[] line, boolean function )
 		{
 			if( inLineArrayPos < 0 )
 				return null;
 			HashMap<String,CodeAttributeVariable> attributes = getLocalVariables( className, line[inLineArrayPos].getFirstCharacterPosition() );
 			return attributes.get( word.toLowerCase() );
 		}
 		
 		public static CodeAttribute getParentAttributeIfNeeded( CodeAttribute attribute, String word, int inLineArrayPos, CodeWord[] line )
 		{
 			if( attribute != null )
 			{
 				boolean skipOne = false;
 				
 				if( !skipOne )
 				{
 					if( inLineArrayPos >= 2 )
 					{
 						skipOne = (".".equals(line[inLineArrayPos-1].getWord()) && "super".equals(line[inLineArrayPos-2].getWord()));
 					}
 				}
 				
 				if( !skipOne )
 				{
 					if( attribute.isFunction() )
 					{
 						boolean inString = false;
 						boolean inName = false;
 						
 						for( int i=0; i<inLineArrayPos; i++ )
 						{
 							String wordWord = line[i].getWord();
 							
 							if( !inString && "'".equals(wordWord) )
 								inName = !inName;
 							if( !inName && "\"".equals(wordWord) )
 								inString = !inString;
 							
 							if( inString || inName )
 								continue;
 							
 							if( WordConstant.FUNCTION_KEYWORDS_HASHSET.contains(wordWord) )
 							{
 								skipOne = true;
 								break;
 							}
 						}
 					}
 				}
 				
 				if( skipOne )
 				{
 					if( !attribute.isFunction() )
 					{
 						CodeAttribute newAttribute = getAttributes( attribute.getClassName() ).getAttributeVariable( word, 1 );
 						if( newAttribute != null )
 						{
 							return newAttribute;
 						}
 					}
 					else
 					{
 						CodeAttribute newAttribute = getAttributes( attribute.getClassName() ).getAttributeFunction( word, 1 );
 						if( newAttribute != null )
 						{
 							return newAttribute;
 						}
 					}
 				}
 			}
 			return attribute;
 		}
 	// get class or attribute <<
 	
 	
 	// other >>
 		private static int clamp( int x, int min, int max )
 		{
 			return Math.max( min, Math.min(max, x) );
 		}
 		private static boolean isBetween( int x, int min, int max )
 		{
 			return ((x >= min) && (x <= max));
 		}
 	// other <<
 }
