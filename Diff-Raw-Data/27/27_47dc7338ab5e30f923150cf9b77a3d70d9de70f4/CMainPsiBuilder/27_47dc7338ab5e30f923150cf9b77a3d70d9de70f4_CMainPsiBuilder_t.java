 /*
  * Copyright 2010-2012 napile.org
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.napile.cpp4idea.lang.parser.parsingMain.builder;
 
 import java.lang.reflect.Field;
 import java.util.AbstractList;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import org.jetbrains.annotations.NonNls;
 import org.jetbrains.annotations.NotNull;
 import org.jetbrains.annotations.Nullable;
 import org.napile.cpp4idea.CLanguage;
 import org.napile.cpp4idea.lang.psi.CPsiTokens;
 import com.intellij.lang.*;
 import com.intellij.openapi.diagnostic.Logger;
 import com.intellij.openapi.progress.ProgressIndicator;
 import com.intellij.openapi.progress.ProgressIndicatorProvider;
 import com.intellij.openapi.project.Project;
 import com.intellij.openapi.util.Comparing;
 import com.intellij.openapi.util.Key;
 import com.intellij.openapi.util.Ref;
 import com.intellij.openapi.util.TextRange;
 import com.intellij.openapi.util.UserDataHolderBase;
 import com.intellij.psi.PsiElement;
 import com.intellij.psi.PsiErrorElement;
 import com.intellij.psi.PsiFile;
 import com.intellij.psi.TokenType;
 import com.intellij.psi.impl.source.resolve.FileContextUtil;
 import com.intellij.psi.impl.source.text.BlockSupportImpl;
 import com.intellij.psi.impl.source.text.DiffLog;
 import com.intellij.psi.impl.source.tree.CompositeElement;
 import com.intellij.psi.impl.source.tree.Factory;
 import com.intellij.psi.impl.source.tree.FileElement;
 import com.intellij.psi.impl.source.tree.ForeignLeafPsiElement;
 import com.intellij.psi.impl.source.tree.LeafElement;
 import com.intellij.psi.impl.source.tree.PsiWhiteSpaceImpl;
 import com.intellij.psi.impl.source.tree.TreeElement;
 import com.intellij.psi.impl.source.tree.TreeUtil;
 import com.intellij.psi.text.BlockSupport;
 import com.intellij.psi.tree.CustomParsingType;
 import com.intellij.psi.tree.IElementType;
 import com.intellij.psi.tree.IFileElementType;
 import com.intellij.psi.tree.ILazyParseableElementType;
 import com.intellij.psi.tree.ILeafElementType;
 import com.intellij.psi.tree.ILightLazyParseableElementType;
 import com.intellij.psi.tree.TokenSet;
 import com.intellij.util.CharTable;
 import com.intellij.util.ExceptionUtil;
 import com.intellij.util.ThreeState;
 import com.intellij.util.TripleFunction;
 import com.intellij.util.containers.ContainerUtil;
 import com.intellij.util.containers.Convertor;
 import com.intellij.util.containers.LimitedPool;
 import com.intellij.util.containers.Stack;
 import com.intellij.util.diff.DiffTreeChangeBuilder;
 import com.intellij.util.diff.FlyweightCapableTreeStructure;
 import com.intellij.util.diff.ShallowNodeComparator;
 
 /**
  * Base source getter from Jetbrains Intellij IDEA CE. Apache 2.0 License
  * Modifications by VISTALL
  */
 public class CMainPsiBuilder extends UserDataHolderBase implements PsiBuilder
 {
 	public static final Key<TextRange> ORIGINAL_TEXT_RANGE = Key.create("c-original-text-range");
 	public static final Key<PsiElement> ORIGINAL_SINGLE_ELEMENT = Key.create("c-original-single-element");
 
 	private static final Logger LOG = Logger.getInstance(CMainPsiBuilder.class);
 
 	// function stored in PsiBuilderImpl' user data which called during reparse when merge algorithm is not sure what to merge
 	public static final Key<TripleFunction<ASTNode, LighterASTNode, FlyweightCapableTreeStructure<LighterASTNode>, ThreeState>> CUSTOM_COMPARATOR = Key.create("CUSTOM_COMPARATOR");
 
 	private final Project myProject;
 	private PsiFile myFile = null;
 
 	private PsiElement[] elements;
 
 	private IElementType[] myLexTypes;
 	private int myCurrentLexeme;
 
 	private final MyList myProduction = new MyList();
 
 	private final TokenSet myWhitespaces;
 	private TokenSet myComments;
 
 
 	private boolean myDebugMode = false;
 	private int myLexemeCount = 0;
 	private boolean myTokenTypeChecked;
 
 
 	private Map<Key, Object> myUserData = null;
 
 	private final LimitedPool<StartMarker> START_MARKERS = new LimitedPool<StartMarker>(2000, new LimitedPool.ObjectFactory<StartMarker>()
 	{
 		@Override
 		public StartMarker create()
 		{
 			return new StartMarker();
 		}
 
 		@Override
 		public void cleanup(final StartMarker startMarker)
 		{
 			startMarker.clean();
 		}
 	});
 
 	private final LimitedPool<DoneMarker> DONE_MARKERS = new LimitedPool<DoneMarker>(2000, new LimitedPool.ObjectFactory<DoneMarker>()
 	{
 		@Override
 		public DoneMarker create()
 		{
 			return new DoneMarker();
 		}
 
 		@Override
 		public void cleanup(final DoneMarker doneMarker)
 		{
 			doneMarker.clean();
 		}
 	});
 
 	private static final WhitespacesAndCommentsBinder DEFAULT_LEFT_EDGE_TOKEN_BINDER = new WhitespacesAndCommentsBinder()
 	{
 		@Override
 		public int getEdgePosition(final List<IElementType> tokens, final boolean atStreamEdge, final TokenTextGetter getter)
 		{
 			return tokens.size();
 		}
 	};
 
 	private static final WhitespacesAndCommentsBinder DEFAULT_RIGHT_EDGE_TOKEN_BINDER = new WhitespacesAndCommentsBinder()
 	{
 		@Override
 		public int getEdgePosition(final List<IElementType> tokens, final boolean atStreamEdge, final TokenTextGetter getter)
 		{
 			return 0;
 		}
 	};
 
	public CMainPsiBuilder(Project project, LanguageVersion languageVersion, List<PsiElement> list)
 	{
 		myProject = project;
 
 
 		ParserDefinition parserDefinition = LanguageParserDefinitions.INSTANCE.forLanguage(CLanguage.INSTANCE);
		myWhitespaces = parserDefinition.getWhitespaceTokens(languageVersion);
		myComments = parserDefinition.getCommentTokens(languageVersion);
 
 		myLexemeCount = list.size();
 		myLexTypes = new IElementType[list.size()];
 		elements = new PsiElement[list.size()];
 
 		for(int i = 0; i < list.size(); i++)
 		{
 			PsiElement element = list.get(i);
 
 			elements[i] = element;
 			myLexTypes[i] = element.getNode().getElementType();
 		}
 	}
 
 	@Override
 	public Project getProject()
 	{
 		return myProject;
 	}
 
 	@Override
 	public void enforceCommentTokens(TokenSet tokens)
 	{
 		myComments = tokens;
 	}
 
 	@Override
 	@Nullable
 	public LighterASTNode getLatestDoneMarker()
 	{
 		int index = myProduction.size() - 1;
 		while(index >= 0)
 		{
 			ProductionMarker marker = myProduction.get(index);
 			if(marker instanceof DoneMarker)
 				return ((DoneMarker) marker).myStart;
 			--index;
 		}
 		return null;
 	}
 
 	private abstract static class Node implements LighterASTNode
 	{
 		public abstract int hc();
 	}
 
 	public IElementType getElementType(int lexemeIndex)
 	{
 		return lexemeIndex < myLexemeCount && lexemeIndex >= 0 ? myLexTypes[lexemeIndex] : null;
 	}
 
 	public abstract static class ProductionMarker extends Node
 	{
 		protected int myLexemeIndex;
 		protected WhitespacesAndCommentsBinder myEdgeTokenBinder;
 		protected ProductionMarker myParent;
 		protected ProductionMarker myNext;
 
 		public void clean()
 		{
 			myLexemeIndex = 0;
 			myParent = myNext = null;
 		}
 
 		public void remapTokenType(IElementType type)
 		{
 			throw new UnsupportedOperationException("Shall not be called on this kind of markers");
 		}
 	}
 
 	private static class StartMarker extends ProductionMarker implements Marker
 	{
 		private CMainPsiBuilder myBuilder;
 		private IElementType myType;
 		private DoneMarker myDoneMarker;
 		private Throwable myDebugAllocationPosition;
 		private ProductionMarker myFirstChild;
 		private ProductionMarker myLastChild;
 		private int myHC = -1;
 
 		private StartMarker()
 		{
 			myEdgeTokenBinder = DEFAULT_LEFT_EDGE_TOKEN_BINDER;
 		}
 
 		@Override
 		public void clean()
 		{
 			super.clean();
 			myBuilder = null;
 			myType = null;
 			myDoneMarker = null;
 			myDebugAllocationPosition = null;
 			myFirstChild = myLastChild = null;
 			myHC = -1;
 			myEdgeTokenBinder = DEFAULT_LEFT_EDGE_TOKEN_BINDER;
 		}
 
 		@Override
 		public int hc()
 		{
 			if(myHC == -1)
 			{
 				CMainPsiBuilder builder = myBuilder;
 				int hc = 0;
 
 				ProductionMarker child = myFirstChild;
 				int lexIdx = myLexemeIndex;
 
 				while(child != null)
 				{
 					int lastLeaf = child.myLexemeIndex;
 
 					for(char c : myBuilder.getTextFromTo(lexIdx, lastLeaf).toCharArray())
 						hc += c;
 
 					lexIdx = lastLeaf;
 					hc += child.hc();
 					if(child instanceof StartMarker)
 					{
 						lexIdx = ((StartMarker) child).myDoneMarker.myLexemeIndex;
 					}
 					child = child.myNext;
 				}
 
 				for(char c : myBuilder.getTextFromTo(lexIdx, myDoneMarker.myLexemeIndex).toCharArray())
 					hc += c;
 				myHC = hc;
 			}
 
 			return myHC;
 		}
 
 		@Override
 		public int getStartOffset()
 		{
 			return myBuilder.elements[myLexemeIndex].getNode().getStartOffset();
 		}
 
 		@Override
 		public int getEndOffset()
 		{
 			return myBuilder.elements[myDoneMarker.myLexemeIndex].getNode().getStartOffset();
 		}
 
 		public void addChild(ProductionMarker node)
 		{
 			if(myFirstChild == null)
 			{
 				myFirstChild = node;
 				myLastChild = node;
 			}
 			else
 			{
 				myLastChild.myNext = node;
 				myLastChild = node;
 			}
 		}
 
 		@Override
 		public Marker precede()
 		{
 			return myBuilder.precede(this);
 		}
 
 		@Override
 		public void drop()
 		{
 			myBuilder.drop(this);
 		}
 
 		@Override
 		public void rollbackTo()
 		{
 			myBuilder.rollbackTo(this);
 		}
 
 		@Override
 		public void done(IElementType type)
 		{
 			myType = type;
 			myBuilder.done(this);
 		}
 
 		@Override
 		public void collapse(IElementType type)
 		{
 			myType = type;
 			myBuilder.collapse(this);
 		}
 
 		@Override
 		public void doneBefore(IElementType type, Marker before)
 		{
 			myType = type;
 			myBuilder.doneBefore(this, before);
 		}
 
 		@Override
 		public void doneBefore(final IElementType type, final Marker before, final String errorMessage)
 		{
 			final StartMarker marker = (StartMarker) before;
 			myBuilder.myProduction.add(myBuilder.myProduction.lastIndexOf(marker), new ErrorItem(myBuilder, errorMessage, marker.myLexemeIndex));
 			doneBefore(type, before);
 		}
 
 		@Override
 		public void error(String message)
 		{
 			myType = TokenType.ERROR_ELEMENT;
 			myBuilder.error(this, message);
 		}
 
 		@Override
 		public void errorBefore(final String message, final Marker before)
 		{
 			myType = TokenType.ERROR_ELEMENT;
 			myBuilder.errorBefore(this, message, before);
 		}
 
 		@Override
 		public IElementType getTokenType()
 		{
 			return myType;
 		}
 
 		@Override
 		public void remapTokenType(IElementType type)
 		{
 			//assert myType != null && type != null;
 			myType = type;
 		}
 
 		@Override
 		public void setCustomEdgeTokenBinders(final WhitespacesAndCommentsBinder left, final WhitespacesAndCommentsBinder right)
 		{
 			if(left != null)
 			{
 				myEdgeTokenBinder = left;
 			}
 
 			if(right != null)
 			{
 				if(myDoneMarker == null)
 					throw new IllegalArgumentException("Cannot set right-edge processor for unclosed marker");
 				myDoneMarker.myEdgeTokenBinder = right;
 			}
 		}
 	}
 
 	private Marker precede(final StartMarker marker)
 	{
 		int idx = myProduction.lastIndexOf(marker);
 		if(idx < 0)
 		{
 			LOG.error("Cannot precede dropped or rolled-back marker");
 		}
 		StartMarker pre = createMarker(marker.myLexemeIndex);
 		myProduction.add(idx, pre);
 		return pre;
 	}
 
 	private abstract static class Token extends Node
 	{
 		protected CMainPsiBuilder myBuilder;
 		private IElementType myTokenType;
 		private int myTokenStart;
 		private int myTokenEnd;
 		private int myHC = -1;
 
 		public void clean()
 		{
 			myBuilder = null;
 			myHC = -1;
 		}
 
 		@Override
 		public int hc()
 		{
 			if(myHC == -1)
 			{
 				int hc = 0;
 				if(myTokenType instanceof TokenWrapper)
 				{
 					final String value = ((TokenWrapper) myTokenType).getValue();
 					for(int i = 0; i < value.length(); i++)
 					{
 						hc += value.charAt(i);
 					}
 				}
 				else
 				{
 					final String text = myBuilder.getTextFromTo(myTokenStart, myTokenEnd);
 
 					for(char c : text.toCharArray())
 						hc += c;
 				}
 
 				myHC = hc;
 			}
 
 			return myHC;
 		}
 
 		@Override
 		public int getEndOffset()
 		{
 			return myTokenEnd;
 		}
 
 		@Override
 		public int getStartOffset()
 		{
 			return myTokenStart;
 		}
 
 		public CharSequence getText()
 		{
 			if(myTokenType instanceof TokenWrapper)
 			{
 				return ((TokenWrapper) myTokenType).getValue();
 			}
 
 			return myBuilder.getTextFromTo(myTokenStart, myTokenEnd);
 		}
 
 		@Override
 		public IElementType getTokenType()
 		{
 			return myTokenType;
 		}
 	}
 
 	private static class TokenNode extends Token implements LighterASTTokenNode
 	{
 	}
 
 	private static class LazyParseableToken extends Token implements LighterLazyParseableNode
 	{
 		private MyTreeStructure myParent;
 		private FlyweightCapableTreeStructure<LighterASTNode> myParsed;
 		private int myStartIndex;
 		private int myEndIndex;
 
 		@Override
 		public void clean()
 		{
 			super.clean();
 			myParent = null;
 			myParsed = null;
 		}
 
 		@Override
 		public PsiFile getContainingFile()
 		{
 			return myBuilder.myFile;
 		}
 
 		@Override
 		public CharTable getCharTable()
 		{
 			throw new UnsupportedOperationException();
 		}
 
 		@Override
 		public boolean accept(@NotNull Visitor visitor)
 		{
 			for(int i = myStartIndex; i < myEndIndex; i++)
 			{
 				IElementType type = myBuilder.myLexTypes[i];
 				if(!visitor.visit(type))
 				{
 					return false;
 				}
 			}
 
 			return true;
 		}
 
 		public FlyweightCapableTreeStructure<LighterASTNode> parseContents()
 		{
 			if(myParsed == null)
 			{
 				myParsed = ((ILightLazyParseableElementType) getTokenType()).parseContents(this);
 			}
 			return myParsed;
 		}
 	}
 
 	private static class DoneMarker extends ProductionMarker
 	{
 		private StartMarker myStart;
 		private boolean myCollapse = false;
 
 		public DoneMarker()
 		{
 			myEdgeTokenBinder = DEFAULT_RIGHT_EDGE_TOKEN_BINDER;
 		}
 
 		public DoneMarker(final StartMarker marker, final int currentLexeme)
 		{
 			this();
 			myLexemeIndex = currentLexeme;
 			myStart = marker;
 		}
 
 		@Override
 		public void clean()
 		{
 			super.clean();
 			myStart = null;
 			myEdgeTokenBinder = DEFAULT_RIGHT_EDGE_TOKEN_BINDER;
 		}
 
 		@Override
 		public int hc()
 		{
 			throw new UnsupportedOperationException("Shall not be called on this kind of markers");
 		}
 
 		@Override
 		public IElementType getTokenType()
 		{
 			throw new UnsupportedOperationException("Shall not be called on this kind of markers");
 		}
 
 		@Override
 		public int getEndOffset()
 		{
 			throw new UnsupportedOperationException("Shall not be called on this kind of markers");
 		}
 
 		@Override
 		public int getStartOffset()
 		{
 			throw new UnsupportedOperationException("Shall not be called on this kind of markers");
 		}
 	}
 
 	private static class DoneWithErrorMarker extends DoneMarker
 	{
 		private String myMessage;
 
 		public DoneWithErrorMarker(final StartMarker marker, final int currentLexeme, final String message)
 		{
 			super(marker, currentLexeme);
 			myMessage = message;
 		}
 
 		@Override
 		public void clean()
 		{
 			super.clean();
 			myMessage = null;
 		}
 	}
 
 	private static class ErrorItem extends ProductionMarker
 	{
 		private final CMainPsiBuilder myBuilder;
 		private String myMessage;
 
 		public ErrorItem(final CMainPsiBuilder builder, final String message, final int idx)
 		{
 			myBuilder = builder;
 			myMessage = message;
 			myLexemeIndex = idx;
 			myEdgeTokenBinder = DEFAULT_RIGHT_EDGE_TOKEN_BINDER;
 		}
 
 		@Override
 		public void clean()
 		{
 			super.clean();
 			myMessage = null;
 		}
 
 		@Override
 		public int hc()
 		{
 			return 0;
 		}
 
 		@Override
 		public int getEndOffset()
 		{
 			return getStartOffset();
 		}
 
 		@Override
 		public int getStartOffset()
 		{
 			return myBuilder.elements[myLexemeIndex == myBuilder.myLexTypes.length ? myLexemeIndex - 1 : myLexemeIndex].getNode().getStartOffset();
 		}
 
 		@Override
 		public IElementType getTokenType()
 		{
 			return TokenType.ERROR_ELEMENT;
 		}
 	}
 
 	@Override
 	public CharSequence getOriginalText()
 	{
 		return null;
 	}
 
 	@Override
 	@Nullable
 	public IElementType getTokenType()
 	{
 		if(eof())
 			return null;
 
 		return myLexTypes[myCurrentLexeme];
 	}
 
 	@Override
 	public void setTokenTypeRemapper(final ITokenTypeRemapper remapper)
 	{
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public void remapCurrentToken(IElementType type)
 	{
 		myLexTypes[myCurrentLexeme] = type;
 	}
 
 	@Nullable
 	@Override
 	public IElementType lookAhead(int steps)
 	{
 		if(eof())
 		{    // ensure we skip over whitespace if it's needed
 			return null;
 		}
 		int cur = myCurrentLexeme;
 
 		while(steps > 0)
 		{
 			++cur;
 			while(cur < myLexemeCount && whitespaceOrComment(myLexTypes[cur]))
 			{
 				cur++;
 			}
 
 			steps--;
 		}
 
 		return cur < myLexemeCount ? myLexTypes[cur] : null;
 	}
 
 	@Override
 	public IElementType rawLookup(int steps)
 	{
 		int cur = myCurrentLexeme + steps;
 		return cur < myLexemeCount && cur >= 0 ? myLexTypes[cur] : null;
 	}
 
 	@Override
 	public int rawTokenTypeStart(int steps)
 	{
 		int cur = myCurrentLexeme + steps;
 		if(cur < 0)
 			return -1;
 		if(cur >= myLexemeCount)
 			return getOriginalText().length();
 		return elements[cur].getNode().getStartOffset();
 	}
 
 	@Override
 	public void setWhitespaceSkippedCallback(WhitespaceSkippedCallback callback)
 	{
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public void advanceLexer()
 	{
 		if(eof())
 			return;
 
 		if(!myTokenTypeChecked)
 			LOG.assertTrue(eof(), "Probably a bug: eating token without its type checking");
 
 		myTokenTypeChecked = false;
 
 		myCurrentLexeme++;
 		//if(myCurrentLexeme >= myLexTypes.length)
 		//	myCurrentLexeme = myLexTypes.length  - 1;
 		ProgressIndicatorProvider.checkCanceled();
 	}
 
 	private void skipWhitespace()
 	{
 		while(myCurrentLexeme < myLexemeCount && whitespaceOrComment(myLexTypes[myCurrentLexeme]))
 			myCurrentLexeme++;
 	}
 
 
 	@Override
 	public int getCurrentOffset()
 	{
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	@Nullable
 	public String getTokenText()
 	{
 		if(eof())
 			return null;
 		final IElementType type = getTokenType();
 		if(type instanceof TokenWrapper)
 			return ((TokenWrapper) type).getValue();
 
 		return elements[myCurrentLexeme].getText();
 	}
 
 	private boolean whitespaceOrComment(IElementType token)
 	{
 		return myWhitespaces.contains(token) || myComments.contains(token) || token == CPsiTokens.NEW_LINE;
 	}
 
 	@Override
 	public Marker mark()
 	{
 		if(!myProduction.isEmpty())
 		{
 			skipWhitespace();
 		}
 		StartMarker marker = createMarker(myCurrentLexeme);
 
 		myProduction.add(marker);
 		return marker;
 	}
 
 	private StartMarker createMarker(final int lexemeIndex)
 	{
 		StartMarker marker = START_MARKERS.alloc();
 		marker.myLexemeIndex = lexemeIndex;
 		marker.myBuilder = this;
 
 		if(myDebugMode)
 		{
 			marker.myDebugAllocationPosition = new Throwable("Created at the following trace.");
 		}
 		return marker;
 	}
 
 	@Override
 	public final boolean eof()
 	{
 		if(!myTokenTypeChecked)
 		{
 			myTokenTypeChecked = true;
 			skipWhitespace();
 		}
 		return myCurrentLexeme >= myLexemeCount;
 	}
 
 	@SuppressWarnings({"SuspiciousMethodCalls"})
 	private void rollbackTo(Marker marker)
 	{
 		myCurrentLexeme = ((StartMarker) marker).myLexemeIndex;
 		myTokenTypeChecked = true;
 		int idx = myProduction.lastIndexOf(marker);
 		if(idx < 0)
 		{
 			LOG.error("The marker must be added before rolled back to.");
 		}
 		myProduction.removeRange(idx, myProduction.size());
 		START_MARKERS.recycle((StartMarker) marker);
 	}
 
 	@SuppressWarnings({"SuspiciousMethodCalls"})
 	public void drop(Marker marker)
 	{
 		final DoneMarker doneMarker = ((StartMarker) marker).myDoneMarker;
 		if(doneMarker != null)
 		{
 			myProduction.remove(myProduction.lastIndexOf(doneMarker));
 			DONE_MARKERS.recycle(doneMarker);
 		}
 		final boolean removed = myProduction.remove(myProduction.lastIndexOf(marker)) == marker;
 		if(!removed)
 		{
 			LOG.error("The marker must be added before it is dropped.");
 		}
 		START_MARKERS.recycle((StartMarker) marker);
 	}
 
 	public void error(Marker marker, String message)
 	{
 		doValidityChecks(marker, null);
 
 		DoneWithErrorMarker doneMarker = new DoneWithErrorMarker((StartMarker) marker, myCurrentLexeme, message);
 		boolean tieToTheLeft = isEmpty(((StartMarker) marker).myLexemeIndex, myCurrentLexeme);
 		if(tieToTheLeft)
 			((StartMarker) marker).myEdgeTokenBinder = DEFAULT_RIGHT_EDGE_TOKEN_BINDER;
 
 		((StartMarker) marker).myDoneMarker = doneMarker;
 		myProduction.add(doneMarker);
 	}
 
 	@SuppressWarnings({"SuspiciousMethodCalls"})
 	public void errorBefore(Marker marker, String message, Marker before)
 	{
 		doValidityChecks(marker, before);
 
 		int beforeIndex = myProduction.lastIndexOf(before);
 
 		DoneWithErrorMarker doneMarker = new DoneWithErrorMarker((StartMarker) marker, ((StartMarker) before).myLexemeIndex, message);
 		boolean tieToTheLeft = isEmpty(((StartMarker) marker).myLexemeIndex, ((StartMarker) before).myLexemeIndex);
 		if(tieToTheLeft)
 			((StartMarker) marker).myEdgeTokenBinder = DEFAULT_RIGHT_EDGE_TOKEN_BINDER;
 
 		((StartMarker) marker).myDoneMarker = doneMarker;
 		myProduction.add(beforeIndex, doneMarker);
 	}
 
 	public void done(final Marker marker)
 	{
 		doValidityChecks(marker, null);
 
 		DoneMarker doneMarker = DONE_MARKERS.alloc();
 		doneMarker.myStart = (StartMarker) marker;
 		doneMarker.myLexemeIndex = myCurrentLexeme;
 		boolean tieToTheLeft = doneMarker.myStart.myType.isLeftBound() && isEmpty(((StartMarker) marker).myLexemeIndex, myCurrentLexeme);
 		if(tieToTheLeft)
 			((StartMarker) marker).myEdgeTokenBinder = DEFAULT_RIGHT_EDGE_TOKEN_BINDER;
 
 		((StartMarker) marker).myDoneMarker = doneMarker;
 		myProduction.add(doneMarker);
 	}
 
 	@SuppressWarnings({"SuspiciousMethodCalls"})
 	public void doneBefore(Marker marker, Marker before)
 	{
 		doValidityChecks(marker, before);
 
 		int beforeIndex = myProduction.lastIndexOf(before);
 
 		DoneMarker doneMarker = DONE_MARKERS.alloc();
 		doneMarker.myLexemeIndex = ((StartMarker) before).myLexemeIndex;
 		doneMarker.myStart = (StartMarker) marker;
 		boolean tieToTheLeft = doneMarker.myStart.myType.isLeftBound() && isEmpty(((StartMarker) marker).myLexemeIndex, ((StartMarker) before).myLexemeIndex);
 		if(tieToTheLeft)
 			((StartMarker) marker).myEdgeTokenBinder = DEFAULT_RIGHT_EDGE_TOKEN_BINDER;
 
 		((StartMarker) marker).myDoneMarker = doneMarker;
 		myProduction.add(beforeIndex, doneMarker);
 	}
 
 	private boolean isEmpty(final int startIdx, final int endIdx)
 	{
 		for(int i = startIdx; i < endIdx; i++)
 		{
 			final IElementType token = myLexTypes[i];
 			if(!whitespaceOrComment(token))
 				return false;
 		}
 		return true;
 	}
 
 	public void collapse(final Marker marker)
 	{
 		done(marker);
 		((StartMarker) marker).myDoneMarker.myCollapse = true;
 	}
 
 	@SuppressWarnings({
 			"UseOfSystemOutOrSystemErr",
 			"SuspiciousMethodCalls",
 			"ThrowableResultOfMethodCallIgnored"
 	})
 	private void doValidityChecks(final Marker marker, @Nullable final Marker before)
 	{
 		final DoneMarker doneMarker = ((StartMarker) marker).myDoneMarker;
 		if(doneMarker != null)
 		{
 			LOG.error("Marker already done.");
 		}
 
 		if(!myDebugMode)
 			return;
 
 		int idx = myProduction.lastIndexOf(marker);
 		if(idx < 0)
 		{
 			LOG.error("Marker has never been added.");
 		}
 
 		int endIdx = myProduction.size();
 		if(before != null)
 		{
 			endIdx = myProduction.lastIndexOf(before);
 			if(endIdx < 0)
 			{
 				LOG.error("'Before' marker has never been added.");
 			}
 			if(idx > endIdx)
 			{
 				LOG.error("'Before' marker precedes this one.");
 			}
 		}
 
 		for(int i = endIdx - 1; i > idx; i--)
 		{
 			Object item = myProduction.get(i);
 			if(item instanceof StartMarker)
 			{
 				StartMarker otherMarker = (StartMarker) item;
 				if(otherMarker.myDoneMarker == null)
 				{
 					final Throwable debugAllocOther = otherMarker.myDebugAllocationPosition;
 					final Throwable debugAllocThis = ((StartMarker) marker).myDebugAllocationPosition;
 					if(debugAllocOther != null)
 					{
 						Throwable currentTrace = new Throwable();
 						ExceptionUtil.makeStackTraceRelative(debugAllocThis, currentTrace).printStackTrace(System.err);
 						ExceptionUtil.makeStackTraceRelative(debugAllocOther, currentTrace).printStackTrace(System.err);
 					}
 					LOG.error("Another not done marker added after this one. Must be done before this.");
 				}
 			}
 		}
 	}
 
 	@Override
 	public void error(String messageText)
 	{
 		final ProductionMarker lastMarker = myProduction.get(myProduction.size() - 1);
 		if(lastMarker instanceof ErrorItem && lastMarker.myLexemeIndex == myCurrentLexeme)
 		{
 			return;
 		}
 		myProduction.add(new ErrorItem(this, messageText, myCurrentLexeme));
 	}
 
 	@Override
 	public ASTNode getTreeBuilt()
 	{
 		try
 		{
 			return buildTree();
 		}
 		finally
 		{
 			for(ProductionMarker marker : myProduction)
 			{
 				if(marker instanceof StartMarker)
 				{
 					START_MARKERS.recycle((StartMarker) marker);
 				}
 				else if(marker instanceof DoneMarker)
 				{
 					DONE_MARKERS.recycle((DoneMarker) marker);
 				}
 			}
 		}
 	}
 
 	private ASTNode buildTree()
 	{
 		final StartMarker rootMarker = prepareLightTree();
 		final boolean isTooDeep = myFile != null && BlockSupport.isTooDeep(myFile.getOriginalFile());
 
 
 		final ASTNode rootNode = createRootAST(rootMarker);
 		bind(rootMarker, (CompositeElement) rootNode);
 
 		if(isTooDeep && !(rootNode instanceof FileElement))
 		{
 			final ASTNode childNode = rootNode.getFirstChildNode();
 			childNode.putUserData(BlockSupport.TREE_DEPTH_LIMIT_EXCEEDED, Boolean.TRUE);
 		}
 
 		return rootNode;
 	}
 
 	@Override
 	public FlyweightCapableTreeStructure<LighterASTNode> getLightTree()
 	{
 		final StartMarker rootMarker = prepareLightTree();
 		return new MyTreeStructure(rootMarker);
 	}
 
 	private ASTNode createRootAST(final StartMarker rootMarker)
 	{
 		final IElementType type = rootMarker.getTokenType();
 
 		return type instanceof ILazyParseableElementType ? ASTFactory.lazy((ILazyParseableElementType) type, null) : createComposite(rootMarker);
 	}
 
 	private static class ConvertFromTokensToASTBuilder implements DiffTreeChangeBuilder<ASTNode, LighterASTNode>
 	{
 		private final DiffTreeChangeBuilder<ASTNode, ASTNode> myDelegate;
 		private final ASTConverter myConverter;
 
 		public ConvertFromTokensToASTBuilder(StartMarker rootNode, DiffTreeChangeBuilder<ASTNode, ASTNode> delegate)
 		{
 			myDelegate = delegate;
 			myConverter = new ASTConverter(rootNode);
 		}
 
 		@Override
 		public void nodeDeleted(@NotNull final ASTNode oldParent, @NotNull final ASTNode oldNode)
 		{
 			myDelegate.nodeDeleted(oldParent, oldNode);
 		}
 
 		@Override
 		public void nodeInserted(@NotNull final ASTNode oldParent, @NotNull final LighterASTNode newNode, final int pos)
 		{
 			myDelegate.nodeInserted(oldParent, myConverter.convert((Node) newNode), pos);
 		}
 
 		@Override
 		public void nodeReplaced(@NotNull final ASTNode oldChild, @NotNull final LighterASTNode newChild)
 		{
 			ASTNode converted = myConverter.convert((Node) newChild);
 			myDelegate.nodeReplaced(oldChild, converted);
 		}
 	}
 
 	@NonNls
 	private static final String UNBALANCED_MESSAGE = "Unbalanced tree. Most probably caused by unbalanced markers. " + "Try calling setDebugMode(true) against PsiBuilder passed to identify exact location of the problem";
 
 	@NotNull
 	private DiffLog merge(@NotNull final ASTNode oldRoot, @NotNull StartMarker newRoot)
 	{
 		DiffLog diffLog = new DiffLog();
 		final ConvertFromTokensToASTBuilder builder = new ConvertFromTokensToASTBuilder(newRoot, diffLog);
 		final MyTreeStructure treeStructure = new MyTreeStructure(newRoot);
 		final MyComparator comparator = new MyComparator(getUserDataUnprotected(CUSTOM_COMPARATOR), treeStructure);
 
 		final ProgressIndicator indicator = ProgressIndicatorProvider.getGlobalProgressIndicator();
 		BlockSupportImpl.diffTrees(oldRoot, builder, comparator, treeStructure, indicator);
 		return diffLog;
 	}
 
 	@NotNull
 	private StartMarker prepareLightTree()
 	{
 		myTokenTypeChecked = true;
 		balanceWhiteSpaces();
 
 		final StartMarker rootMarker = (StartMarker) myProduction.get(0);
 		rootMarker.myParent = rootMarker.myFirstChild = rootMarker.myLastChild = rootMarker.myNext = null;
 		StartMarker curNode = rootMarker;
 		final Stack<StartMarker> nodes = ContainerUtil.newStack();
 		nodes.push(rootMarker);
 
 		@SuppressWarnings({"MultipleVariablesInDeclaration"}) int lastErrorIndex = -1, maxDepth = 0, curDepth = 0;
 		for(int i = 1; i < myProduction.size(); i++)
 		{
 			final ProductionMarker item = myProduction.get(i);
 
 			if(curNode == null)
 				LOG.error("Unexpected end of the production");
 
 			item.myParent = curNode;
 			if(item instanceof StartMarker)
 			{
 				final StartMarker marker = (StartMarker) item;
 				marker.myFirstChild = marker.myLastChild = marker.myNext = null;
 				curNode.addChild(marker);
 				nodes.push(curNode);
 				curNode = marker;
 				curDepth++;
 				if(curDepth > maxDepth)
 					maxDepth = curDepth;
 			}
 			else if(item instanceof DoneMarker)
 			{
 				if(((DoneMarker) item).myStart != curNode)
 					LOG.error(UNBALANCED_MESSAGE);
 				curNode = nodes.pop();
 				curDepth--;
 			}
 			else if(item instanceof ErrorItem)
 			{
 				int curToken = item.myLexemeIndex;
 				if(curToken == lastErrorIndex)
 					continue;
 				lastErrorIndex = curToken;
 				curNode.addChild(item);
 			}
 		}
 
 		if(myCurrentLexeme < myLexemeCount)
 		{
 			final List<IElementType> missed = ContainerUtil.newArrayList(myLexTypes, myCurrentLexeme, myLexemeCount);
 			LOG.error("Tokens " + missed + " were not inserted into the tree. " + (myFile != null ? myFile.getLanguage() + ", " : ""));
 		}
 
 		if(rootMarker.myDoneMarker.myLexemeIndex < myLexemeCount)
 		{
 			final List<IElementType> missed = ContainerUtil.newArrayList(myLexTypes, rootMarker.myDoneMarker.myLexemeIndex, myLexemeCount);
 			LOG.error("Tokens " + missed + " are outside of root element \"" + rootMarker.myType);
 		}
 
 		LOG.assertTrue(curNode == rootMarker, UNBALANCED_MESSAGE);
 
 		checkTreeDepth(maxDepth, rootMarker.getTokenType() instanceof IFileElementType);
 
 		return rootMarker;
 	}
 
 	private void balanceWhiteSpaces()
 	{
 		RelativeTokenTypesView wsTokens = null;
 		RelativeTokenTextView tokenTextGetter = null;
 
 		for(int i = 1, size = myProduction.size() - 1; i < size; i++)
 		{
 			final ProductionMarker item = myProduction.get(i);
 
 			if(item instanceof StartMarker && ((StartMarker) item).myDoneMarker == null)
 			{
 				LOG.error(UNBALANCED_MESSAGE);
 			}
 
 			final int prevProductionLexIndex = myProduction.get(i - 1).myLexemeIndex;
 			int idx = item.myLexemeIndex;
 			while(idx > prevProductionLexIndex && whitespaceOrComment(myLexTypes[idx - 1]))
 				idx--;
 			final int wsStartIndex = idx;
 
 			int wsEndIndex = item.myLexemeIndex;
 			while(wsEndIndex < myLexemeCount && whitespaceOrComment(myLexTypes[wsEndIndex]))
 				wsEndIndex++;
 
 			if(wsTokens == null)
 				wsTokens = new RelativeTokenTypesView();
 			wsTokens.configure(wsStartIndex, wsEndIndex);
 			final boolean atEnd = wsStartIndex == 0 || wsEndIndex == myLexemeCount;
 			if(tokenTextGetter == null)
 				tokenTextGetter = new RelativeTokenTextView();
 			tokenTextGetter.configure(wsStartIndex);
 
 			item.myLexemeIndex = wsStartIndex + item.myEdgeTokenBinder.getEdgePosition(wsTokens, atEnd, tokenTextGetter);
 		}
 	}
 
 	private final class RelativeTokenTypesView extends AbstractList<IElementType>
 	{
 		private int start;
 		private int size;
 
 		private void configure(int _start, int _end)
 		{
 			size = _end - _start;
 			start = _start;
 		}
 
 		@Override
 		public IElementType get(final int index)
 		{
 			return myLexTypes[start + index];
 		}
 
 		@Override
 		public int size()
 		{
 			return size;
 		}
 	}
 
 	private final class RelativeTokenTextView implements WhitespacesAndCommentsBinder.TokenTextGetter
 	{
 		private int start;
 
 		private void configure(int _start)
 		{
 			start = _start;
 		}
 
 		@Override
 		public CharSequence get(final int i)
 		{
 			return getTextFromTo(start + i, start + i + 1);
 		}
 	}
 
 	private void checkTreeDepth(final int maxDepth, final boolean isFileRoot)
 	{
 		if(myFile == null)
 			return;
 		final PsiFile file = myFile.getOriginalFile();
 		final Boolean flag = file.getUserData(BlockSupport.TREE_DEPTH_LIMIT_EXCEEDED);
 		if(maxDepth > BlockSupport.INCREMENTAL_REPARSE_DEPTH_LIMIT)
 		{
 			if(!Boolean.TRUE.equals(flag))
 			{
 				file.putUserData(BlockSupport.TREE_DEPTH_LIMIT_EXCEEDED, Boolean.TRUE);
 			}
 		}
 		else if(isFileRoot && flag != null)
 		{
 			file.putUserData(BlockSupport.TREE_DEPTH_LIMIT_EXCEEDED, null);
 		}
 	}
 
 	private void bind(final StartMarker rootMarker, final CompositeElement rootNode)
 	{
 		StartMarker curMarker = rootMarker;
 		CompositeElement curNode = rootNode;
 
 		int lexIndex = rootMarker.myLexemeIndex;
 		ProductionMarker item = rootMarker.myFirstChild != null ? rootMarker.myFirstChild : rootMarker.myDoneMarker;
 		while(true)
 		{
 			lexIndex = insertLeaves(lexIndex, item.myLexemeIndex, curNode);
 
 			if(item == rootMarker.myDoneMarker)
 				break;
 
 			if(item instanceof StartMarker)
 			{
 				final StartMarker marker = (StartMarker) item;
 				if(!marker.myDoneMarker.myCollapse)
 				{
 					curMarker = marker;
 
 					final CompositeElement childNode = createComposite(marker);
 					curNode.rawAddChildrenWithoutNotifications(childNode);
 					curNode = childNode;
 
 					item = marker.myFirstChild != null ? marker.myFirstChild : marker.myDoneMarker;
 					continue;
 				}
 				else
 				{
 					lexIndex = collapseLeaves(curNode, marker);
 				}
 			}
 			else if(item instanceof ErrorItem)
 			{
 				final CompositeElement errorElement = Factory.createErrorElement(((ErrorItem) item).myMessage);
 				errorElement.putUserData(ORIGINAL_TEXT_RANGE, new TextRange(item.getStartOffset(), item.getEndOffset()));
 				curNode.rawAddChildrenWithoutNotifications(errorElement);
 			}
 			else if(item instanceof DoneMarker)
 			{
 				curMarker = (StartMarker) ((DoneMarker) item).myStart.myParent;
 				curNode = curNode.getTreeParent();
 				item = ((DoneMarker) item).myStart;
 			}
 
 			item = item.myNext != null ? item.myNext : curMarker.myDoneMarker;
 		}
 	}
 
 	private int insertLeaves(int curToken, int lastIdx, final CompositeElement curNode)
 	{
 		lastIdx = Math.min(lastIdx, myLexemeCount);
 		while(curToken < lastIdx)
 		{
 			ProgressIndicatorProvider.checkCanceled();
 			final int start = curToken;
 			final int end = curToken + 1;
 			if(start < end || myLexTypes[curToken] instanceof ILeafElementType)
 			{ // Empty token. Most probably a parser directive like indent/dedent in Python
 				final IElementType type = myLexTypes[curToken];
 				final TreeElement leaf = createLeaf(type, start, end);
 				curNode.rawAddChildrenWithoutNotifications(leaf);
 			}
 			curToken++;
 		}
 
 		return curToken;
 	}
 
 	private int collapseLeaves(final CompositeElement ast, final StartMarker startMarker)
 	{
 		final TreeElement leaf = createLeaf(startMarker.myType, startMarker.myLexemeIndex, startMarker.myDoneMarker.myLexemeIndex);
 		ast.rawAddChildrenWithoutNotifications(leaf);
 		return startMarker.myDoneMarker.myLexemeIndex;
 	}
 
 	private static CompositeElement createComposite(final StartMarker marker)
 	{
 		final IElementType type = marker.myType;
 		if(type == TokenType.ERROR_ELEMENT)
 		{
 			String message = marker.myDoneMarker instanceof DoneWithErrorMarker ? ((DoneWithErrorMarker) marker.myDoneMarker).myMessage : null;
 			return Factory.createErrorElement(message);
 		}
 
 		if(type == null)
 		{
 			throw new RuntimeException(UNBALANCED_MESSAGE);
 		}
 
 		return ASTFactory.composite(type);
 	}
 
 	@Nullable
 	public static String getErrorMessage(final LighterASTNode node)
 	{
 		if(node instanceof ErrorItem)
 			return ((ErrorItem) node).myMessage;
 		if(node instanceof StartMarker)
 		{
 			final StartMarker marker = (StartMarker) node;
 			if(marker.myType == TokenType.ERROR_ELEMENT && marker.myDoneMarker instanceof DoneWithErrorMarker)
 			{
 				return ((DoneWithErrorMarker) marker.myDoneMarker).myMessage;
 			}
 		}
 
 		return null;
 	}
 
 	private static class MyComparator implements ShallowNodeComparator<ASTNode, LighterASTNode>
 	{
 		private final TripleFunction<ASTNode, LighterASTNode, FlyweightCapableTreeStructure<LighterASTNode>, ThreeState> custom;
 		private final MyTreeStructure myTreeStructure;
 
 		private MyComparator(TripleFunction<ASTNode, LighterASTNode, FlyweightCapableTreeStructure<LighterASTNode>, ThreeState> custom, MyTreeStructure treeStructure)
 		{
 			this.custom = custom;
 			myTreeStructure = treeStructure;
 		}
 
 		@Override
 		public ThreeState deepEqual(final ASTNode oldNode, final LighterASTNode newNode)
 		{
 			ProgressIndicatorProvider.checkCanceled();
 
 			boolean oldIsErrorElement = oldNode instanceof PsiErrorElement;
 			boolean newIsErrorElement = newNode.getTokenType() == TokenType.ERROR_ELEMENT;
 			if(oldIsErrorElement != newIsErrorElement)
 				return ThreeState.NO;
 			if(oldIsErrorElement && newIsErrorElement)
 			{
 				final PsiErrorElement e1 = (PsiErrorElement) oldNode;
 				return Comparing.equal(e1.getErrorDescription(), getErrorMessage(newNode)) ? ThreeState.UNSURE : ThreeState.NO;
 			}
 
 			if(newNode instanceof Token)
 			{
 				final IElementType type = newNode.getTokenType();
 				final Token token = (Token) newNode;
 
 				if(oldNode instanceof ForeignLeafPsiElement)
 				{
 					return type instanceof ForeignLeafType && ((ForeignLeafType) type).getValue().equals(oldNode.getText()) ? ThreeState.YES : ThreeState.NO;
 				}
 
 				if(oldNode instanceof LeafElement)
 				{
 					if(type instanceof ForeignLeafType)
 						return ThreeState.NO;
 
 					return ((LeafElement) oldNode).textMatches(token.getText()) ? ThreeState.YES : ThreeState.NO;
 				}
 
 				if(type instanceof ILightLazyParseableElementType)
 				{
 					return ((TreeElement) oldNode).textMatches(token.getText()) ? ThreeState.YES : TreeUtil.isCollapsedChameleon(oldNode) ? ThreeState.NO  // do not dive into collapsed nodes
 							: ThreeState.UNSURE;
 				}
 
 				if(oldNode.getElementType() instanceof ILazyParseableElementType && type instanceof ILazyParseableElementType || oldNode.getElementType() instanceof CustomParsingType && type instanceof CustomParsingType)
 				{
 					return ((TreeElement) oldNode).textMatches(token.getText()) ? ThreeState.YES : ThreeState.NO;
 				}
 			}
 			if(custom != null)
 			{
 				return custom.fun(oldNode, newNode, myTreeStructure);
 			}
 
 			return ThreeState.UNSURE;
 		}
 
 		@Override
 		public boolean typesEqual(final ASTNode n1, final LighterASTNode n2)
 		{
 			if(n1 instanceof PsiWhiteSpaceImpl)
 			{
 				return n2 instanceof Token && ((Token) n2).myBuilder.myWhitespaces.contains(n2.getTokenType());
 			}
 			IElementType n1t;
 			IElementType n2t;
 			if(n1 instanceof ForeignLeafPsiElement)
 			{
 				n1t = ((ForeignLeafPsiElement) n1).getForeignType();
 				n2t = n2.getTokenType();
 			}
 			else
 			{
 				n1t = dereferenceToken(n1.getElementType());
 				n2t = dereferenceToken(n2.getTokenType());
 			}
 
 			return Comparing.equal(n1t, n2t);
 		}
 
 		public static IElementType dereferenceToken(IElementType probablyWrapper)
 		{
 			if(probablyWrapper instanceof TokenWrapper)
 			{
 				return dereferenceToken(((TokenWrapper) probablyWrapper).getDelegate());
 			}
 			return probablyWrapper;
 		}
 
 
 		@Override
 		public boolean hashCodesEqual(final ASTNode n1, final LighterASTNode n2)
 		{
 			if(n1 instanceof LeafElement && n2 instanceof Token)
 			{
 				boolean isForeign1 = n1 instanceof ForeignLeafPsiElement;
 				boolean isForeign2 = n2.getTokenType() instanceof ForeignLeafType;
 				if(isForeign1 != isForeign2)
 					return false;
 
 				if(isForeign1 && isForeign2)
 				{
 					return n1.getText().equals(((ForeignLeafType) n2.getTokenType()).getValue());
 				}
 
 				return ((LeafElement) n1).textMatches(((Token) n2).getText());
 			}
 
 			if(n1 instanceof PsiErrorElement && n2.getTokenType() == TokenType.ERROR_ELEMENT)
 			{
 				final PsiErrorElement e1 = (PsiErrorElement) n1;
 				if(!Comparing.equal(e1.getErrorDescription(), getErrorMessage(n2)))
 					return false;
 			}
 
 			return ((TreeElement) n1).hc() == ((Node) n2).hc();
 		}
 	}
 
 	private static class MyTreeStructure implements FlyweightCapableTreeStructure<LighterASTNode>
 	{
 		private final LimitedPool<Token> myPool;
 		private final LimitedPool<LazyParseableToken> myLazyPool;
 		private final StartMarker myRoot;
 
 		public MyTreeStructure(@NotNull StartMarker root)
 		{
 			myPool = new LimitedPool<Token>(1000, new LimitedPool.ObjectFactory<Token>()
 			{
 				@Override
 				public void cleanup(final Token token)
 				{
 					token.clean();
 				}
 
 				@Override
 				public Token create()
 				{
 					return new TokenNode();
 				}
 			});
 			myLazyPool = new LimitedPool<LazyParseableToken>(200, new LimitedPool.ObjectFactory<LazyParseableToken>()
 			{
 				@Override
 				public void cleanup(final LazyParseableToken token)
 				{
 					token.clean();
 				}
 
 				@Override
 				public LazyParseableToken create()
 				{
 					return new LazyParseableToken();
 				}
 			});
 
 			myRoot = root;
 		}
 
 		@Override
 		@NotNull
 		public LighterASTNode getRoot()
 		{
 			return myRoot;
 		}
 
 		@Override
 		public LighterASTNode getParent(@NotNull final LighterASTNode node)
 		{
 			if(node instanceof StartMarker)
 			{
 				return ((StartMarker) node).myParent;
 			}
 			throw new UnsupportedOperationException("Unknown node type: " + node);
 		}
 
 		@Override
 		@NotNull
 		public LighterASTNode prepareForGetChildren(@NotNull final LighterASTNode node)
 		{
 			return node;
 		}
 
 		private int count;
 		private LighterASTNode[] nodes;
 
 		@Override
 		public int getChildren(@NotNull final LighterASTNode item, @NotNull final Ref<LighterASTNode[]> into)
 		{
 			if(item instanceof LazyParseableToken)
 			{
 				final FlyweightCapableTreeStructure<LighterASTNode> tree = ((LazyParseableToken) item).parseContents();
 				final LighterASTNode root = tree.getRoot();
 				return tree.getChildren(tree.prepareForGetChildren(root), into);  // todo: set offset shift for kids?
 			}
 
 			if(item instanceof Token || item instanceof ErrorItem)
 				return 0;
 			StartMarker marker = (StartMarker) item;
 
 			count = 0;
 			ProductionMarker child = marker.myFirstChild;
 			int lexIndex = marker.myLexemeIndex;
 			while(child != null)
 			{
 				lexIndex = insertLeaves(lexIndex, child.myLexemeIndex, marker.myBuilder);
 
 				if(child instanceof StartMarker && ((StartMarker) child).myDoneMarker.myCollapse)
 				{
 					int lastIndex = ((StartMarker) child).myDoneMarker.myLexemeIndex;
 					insertLeaf(child.getTokenType(), marker.myBuilder, child.myLexemeIndex, lastIndex);
 				}
 				else
 				{
 					ensureCapacity();
 					nodes[count++] = child;
 				}
 
 				if(child instanceof StartMarker)
 				{
 					lexIndex = ((StartMarker) child).myDoneMarker.myLexemeIndex;
 				}
 				child = child.myNext;
 			}
 
 			insertLeaves(lexIndex, marker.myDoneMarker.myLexemeIndex, marker.myBuilder);
 			into.set(nodes);
 			nodes = null;
 
 			return count;
 		}
 
 		@Override
 		public void disposeChildren(final LighterASTNode[] nodes, final int count)
 		{
 			if(nodes == null)
 				return;
 			for(int i = 0; i < count; i++)
 			{
 				final LighterASTNode node = nodes[i];
 				if(node instanceof LazyParseableToken)
 				{
 					myLazyPool.recycle((LazyParseableToken) node);
 				}
 				else if(node instanceof Token)
 				{
 					myPool.recycle((Token) node);
 				}
 			}
 		}
 
 		private void ensureCapacity()
 		{
 			LighterASTNode[] old = nodes;
 			if(old == null)
 			{
 				old = new LighterASTNode[10];
 				nodes = old;
 			}
 			else if(count >= old.length)
 			{
 				LighterASTNode[] newStore = new LighterASTNode[count * 3 / 2];
 				System.arraycopy(old, 0, newStore, 0, count);
 				nodes = newStore;
 			}
 		}
 
 		private int insertLeaves(int curToken, int lastIdx, CMainPsiBuilder builder)
 		{
 			lastIdx = Math.min(lastIdx, builder.myLexemeCount);
 			while(curToken < lastIdx)
 			{
 				insertLeaf(builder.myLexTypes[curToken], builder, curToken, curToken + 1);
 
 				curToken++;
 			}
 			return curToken;
 		}
 
 		private void insertLeaf(IElementType type, CMainPsiBuilder builder, int startLexemeIndex, int endLexemeIndex)
 		{
 			if(startLexemeIndex > endLexemeIndex || ((startLexemeIndex == endLexemeIndex) && !(type instanceof ILeafElementType)))
 				return;
 
 			final Token lexeme;
 			if(type instanceof ILightLazyParseableElementType)
 			{
 				lexeme = myLazyPool.alloc();
 				LazyParseableToken lazyParseableToken = (LazyParseableToken) lexeme;
 				lazyParseableToken.myParent = this;
 				lazyParseableToken.myStartIndex = startLexemeIndex;
 				lazyParseableToken.myEndIndex = endLexemeIndex;
 			}
 			else
 			{
 				lexeme = myPool.alloc();
 			}
 			lexeme.myBuilder = builder;
 			lexeme.myTokenType = type;
 			lexeme.myTokenStart = startLexemeIndex;
 			lexeme.myTokenEnd = endLexemeIndex;
 			ensureCapacity();
 			nodes[count++] = lexeme;
 		}
 	}
 
 	private static class ASTConverter implements Convertor<Node, ASTNode>
 	{
 		private final StartMarker myRoot;
 
 		public ASTConverter(final StartMarker root)
 		{
 			myRoot = root;
 		}
 
 		@Override
 		public ASTNode convert(final Node n)
 		{
 			if(n instanceof Token)
 			{
 				final Token token = (Token) n;
 				return token.myBuilder.createLeaf(token.getTokenType(), token.myTokenStart, token.myTokenEnd);
 			}
 			else if(n instanceof ErrorItem)
 			{
 				return Factory.createErrorElement(((ErrorItem) n).myMessage);
 			}
 			else
 			{
 				final StartMarker startMarker = (StartMarker) n;
 				final CompositeElement composite = n == myRoot ? (CompositeElement) myRoot.myBuilder.createRootAST(myRoot) : createComposite(startMarker);
 				startMarker.myBuilder.bind(startMarker, composite);
 				return composite;
 			}
 		}
 	}
 
 	@Override
 	public void setDebugMode(boolean dbgMode)
 	{
 		myDebugMode = dbgMode;
 	}
 
 	@NotNull
 	private TreeElement createLeaf(final IElementType type, final int start, final int end)
 	{
 		CharSequence text = getTextFromTo(start, end);
 
 		TreeElement element;
 		if(myWhitespaces.contains(type))
 			element = new PsiWhiteSpaceImpl(text);
 		else if(type instanceof ILazyParseableElementType)
 			element = ASTFactory.lazy((ILazyParseableElementType) type, text);
 		else
 			element = ASTFactory.leaf(type, text);
 
 		element.putUserData(ORIGINAL_SINGLE_ELEMENT, elements[start]);
 		return element;
 	}
 
 	/**
 	 * just to make removeRange method available.
 	 */
 	private static class MyList extends ArrayList<ProductionMarker>
 	{
 		private static final Field ourElementDataField;
 
 		static
 		{
 			Field f;
 			try
 			{
 				f = ArrayList.class.getDeclaredField("elementData");
 				f.setAccessible(true);
 			}
 			catch(NoSuchFieldException e)
 			{
 				// IBM J9 does not have the field
 				f = null;
 
 			}
 			ourElementDataField = f;
 		}
 
 		private Object[] cachedElementData;
 
 		@Override
 		public void removeRange(final int fromIndex, final int toIndex)
 		{
 			super.removeRange(fromIndex, toIndex);
 		}
 
 		MyList()
 		{
 			super(256);
 		}
 
 		@Override
 		public int lastIndexOf(final Object o)
 		{
 			if(cachedElementData == null)
 				return super.lastIndexOf(o);
 			for(int i = size() - 1; i >= 0; i--)
 			{
 				if(cachedElementData[i] == o)
 					return i;
 			}
 			return -1;
 		}
 
 		@Override
 		public void ensureCapacity(final int minCapacity)
 		{
 			if(cachedElementData == null || minCapacity >= cachedElementData.length)
 			{
 				super.ensureCapacity(minCapacity);
 				initCachedField();
 			}
 		}
 
 		private void initCachedField()
 		{
 			if(ourElementDataField == null)
 				return;
 			try
 			{
 				cachedElementData = (Object[]) ourElementDataField.get(this);
 			}
 			catch(Exception e)
 			{
 				LOG.error(e);
 			}
 		}
 	}
 
 	private String getTextFromTo(int from, int to)
 	{
 		StringBuilder b = new StringBuilder();
 		for(int i = from; i < to; i++)
 			b.append(elements[i].getText());
 		return b.toString();
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public <T> T getUserDataUnprotected(@NotNull final Key<T> key)
 	{
 		if(key == FileContextUtil.CONTAINING_FILE_KEY)
 			return (T) myFile;
 		return myUserData != null ? (T) myUserData.get(key) : null;
 	}
 
 	@Override
 	public <T> void putUserDataUnprotected(@NotNull final Key<T> key, @Nullable final T value)
 	{
 		if(key == FileContextUtil.CONTAINING_FILE_KEY)
 		{
 			myFile = (PsiFile) value;
 			return;
 		}
 		if(myUserData == null)
 			myUserData = ContainerUtil.newHashMap();
 		myUserData.put(key, value);
 	}
 }
