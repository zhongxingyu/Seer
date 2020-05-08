 package com.intellij.lang.stylus.parser;
 
 import java.util.ArrayDeque;
 import java.util.Deque;
 import java.util.Iterator;
 
 import org.jetbrains.annotations.NotNull;
 import org.jetbrains.annotations.Nullable;
 import com.intellij.lang.ASTNode;
 import com.intellij.lang.PsiBuilder;
 import com.intellij.lang.PsiParser;
 import com.intellij.lang.stylus.psi.StylusNodeTypes;
 import com.intellij.lang.stylus.psi.StylusTokenTypes;
 import com.intellij.psi.tree.IElementType;
 
 /**
  * @author VISTALL
  * @since 20:35/26.06.13
  */
 public class StylusParser implements PsiParser, StylusTokenTypes, StylusNodeTypes
 {
 	private static class Part
 	{
 		@Nullable
 		private PsiBuilder.Marker myMarker;
 
 		private IElementType myDoneElement = PROPERTY;
 
 		private int myIndent;
 	}
 
 	private Deque<Part> myParts = new ArrayDeque<Part>();
 
 	@NotNull
 	@Override
 	public ASTNode parse(IElementType root, PsiBuilder builder)
 	{
 		builder.setDebugMode(true);
 
 		final PsiBuilder.Marker mark = builder.mark();
 
 		while(!builder.eof())
 		{
 			parseLine(builder, false);
 
 			if(builder.getTokenType() == NEWLINE)
 			{
 				builder.advanceLexer();
 			}
 		}
 
 		final Iterator<Part> iterator = myParts.descendingIterator();
 		while(iterator.hasNext())
 		{
 			doneMarker(iterator.next());
 		}
 
 		mark.done(root);
 		return builder.getTreeBuilt();
 	}
 
 	private void parseLine(PsiBuilder builder, boolean cssStyle)
 	{
 		int indent = 0;
 		while(builder.getTokenType() == INDENT)
 		{
 			builder.advanceLexer();
 			indent++;
 		}
 
 		// if line is ended return
 		if(builder.getTokenType() == NEWLINE || cssStyle && builder.getTokenType() == RBRACE)
 		{
 			return;
 		}
 
 		if(!cssStyle)
 		{
 			final Part prevPart = myParts.peekLast();
 			if(prevPart != null)
 			{
 				if(prevPart.myIndent == indent) // new property - done prev property
 				{
 					doneMarker(prevPart);
 				}
 				else if(prevPart.myIndent < indent) // child
 				{
 					prevPart.myDoneElement = RULE;
 				}
 				else // new child of parent
 				{
 					// A
 					//  B
 					// C - this is current
 
 					final Iterator<Part> partIterator = myParts.descendingIterator();
 					while(partIterator.hasNext())
 					{
 						Part next = partIterator.next();
 
 						doneMarker(next);
 
 						if(next.myIndent == 0)
 						{
 							break;
 						}
 					}
 				}
 			}
 		}
 
 		final PsiBuilder.Marker mark = builder.mark();
 		Part part = new Part();
 		part.myMarker = mark;
 		part.myIndent = indent;
 
 		myParts.add(part);
 
 		while(!builder.eof())
 		{
 			if(builder.getTokenType() == NEWLINE)
 			{
 				break;
 			}
 			else if(builder.getTokenType() == LBRACE)
 			{
 				builder.advanceLexer();
 
 				part.myDoneElement = RULE;
 
 				parseLinesWithCssStyle(builder, part);
 			}
 			else
 			{
 				builder.advanceLexer();
 			}
 		}
 	}
 
 	private void parseLinesWithCssStyle(PsiBuilder builder, Part part)
 	{
		boolean found = false;

 		while(!builder.eof())
 		{
 			parseLine(builder, true);
 
 			if(builder.getTokenType() == RBRACE)
 			{
 				doneMarker(myParts.peekLast());
 
 				builder.advanceLexer();
 
 				doneMarker(part);
				found = true;
 				break;
 			}
 			else
 			{
 				builder.advanceLexer();
 			}
 		}

		if(!found)
		{
			builder.error("'}' expected");
		}
 	}
 
 	private static void doneMarker(Part part)
 	{
 		if(part == null)
 		{
 			return;
 		}
 		final PsiBuilder.Marker marker = part.myMarker;
 		if(marker != null)
 		{
 			marker.done(part.myDoneElement);
 			part.myMarker = null;
 		}
 	}
 }
