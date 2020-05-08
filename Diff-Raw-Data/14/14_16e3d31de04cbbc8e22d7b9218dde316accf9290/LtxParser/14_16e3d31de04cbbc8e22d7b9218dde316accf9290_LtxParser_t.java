 /*=============================================================================#
  # Copyright (c) 2009-2014 Stephan Wahlbrink (WalWare.de) and others.
  # All rights reserved. This program and the accompanying materials
  # are made available under the terms of the Eclipse Public License v1.0
  # which accompanies this distribution, and is available at
  # http://www.eclipse.org/legal/epl-v10.html
  # 
  # Contributors:
  #     Stephan Wahlbrink - initial API and implementation
  #=============================================================================*/
 
 package de.walware.docmlet.tex.core.ast;
 
 import static de.walware.docmlet.tex.core.ast.ITexAstStatusConstants.STATUS2_ENV_MISSING_NAME;
 import static de.walware.docmlet.tex.core.ast.ITexAstStatusConstants.STATUS2_ENV_NOT_OPENED;
 import static de.walware.docmlet.tex.core.ast.ITexAstStatusConstants.STATUS2_GROUP_NOT_CLOSED;
 import static de.walware.docmlet.tex.core.ast.ITexAstStatusConstants.STATUS2_GROUP_NOT_OPENED;
 import static de.walware.docmlet.tex.core.ast.ITexAstStatusConstants.STATUS2_VERBATIM_INLINE_C_MISSING;
 import static de.walware.docmlet.tex.core.ast.ITexAstStatusConstants.STATUS2_VERBATIM_INLINE_NOT_CLOSED;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import de.walware.ecommons.collections.ConstArrayList;
 import de.walware.ecommons.text.IStringCache;
 import de.walware.ecommons.text.InternStringCache;
 import de.walware.ecommons.text.NoStringCache;
 import de.walware.ecommons.text.SourceParseInput;
 
 import de.walware.docmlet.tex.core.ast.TexAst.NodeType;
 import de.walware.docmlet.tex.core.commands.Argument;
 import de.walware.docmlet.tex.core.commands.LtxCommandDefinitions;
 import de.walware.docmlet.tex.core.commands.TexCommand;
 import de.walware.docmlet.tex.core.commands.TexCommandSet;
 import de.walware.docmlet.tex.core.commands.TexEmbedCommand;
 import de.walware.docmlet.tex.core.parser.LtxLexer;
 
 
 public class LtxParser {
 	
 	
 	private static final byte ST_ROOT=                      0;
 	private static final byte ST_CURLY=                     1;
 	private static final byte ST_SQUARED=                   2;
 	private static final byte ST_BEGINEND=                  3;
 	private static final byte ST_MATHROUND=                 4;
 	private static final byte ST_MATHSQUARED=               5;
 	private static final byte ST_MATHDOLLAR=                6;
 	private static final byte ST_MATHDOLLARDOLLAR=          7;
 	
 	private static final String LABEL_TEXT= new String("LABEL"); //$NON-NLS-1$
 	private static final String NUM_TEXT= new String("NUM"); //$NON-NLS-1$
 	
 	
 	private final LtxLexer lexer;
 	
 	private TexCommandSet commandSet;
 	
 	private Map<String, TexCommand> customCommands;
 	private Map<String, TexCommand> customEnvs;
 	
 	private byte[] stackTypes= new byte[32];
 	private String[] stackEndKeys= new String[32];
 	private int stackPos= -1;
 	private int foundEndStackPos;
 	private int foundEndOffset;
 	private TexAstNode foundEndNode;
 	private boolean inMath;
 	private final Text whitespace= new Text(null, -1, -1);
 	private boolean wasLinebreak;
 	
 	private List<Embedded> embeddedList;
 	
 	private final IStringCache labelFactory;
 	private final IStringCache otherFactory;
 	
 	
 	public LtxParser(final LtxLexer lexer) {
 		this(lexer, null);
 	}
 	
 	public LtxParser(final LtxLexer lexer, final IStringCache labelFactory) {
 		this.lexer= (lexer != null) ? lexer : new LtxLexer();
 		this.lexer.setReportSquaredBrackets(true);
 		this.lexer.setCreateControlTexts(true);
 		this.labelFactory= (labelFactory != null) ? labelFactory : InternStringCache.INSTANCE;
 		this.otherFactory= (labelFactory != null) ? labelFactory : NoStringCache.INSTANCE;
 	}
 	
 	private TexCommand getCommand(final String controlWord) {
 		if (controlWord == "end") { //$NON-NLS-1$
 			return LtxCommandDefinitions.GENERICENV_end_COMMAND;
 		}
 		TexCommand command;
 		if (this.inMath) {
 			command= this.commandSet.getLtxMathCommandMap().get(controlWord);
 		}
 		else {
 			command= this.commandSet.getLtxPreambleCommandMap().get(controlWord);
 			if (command == null) {
 				command= this.commandSet.getLtxTextCommandMap().get(controlWord);
 			}
 		}
 		if (command != null) {
 			return command;
 		}
 		if (this.customCommands != null) {
 			return this.customCommands.get(controlWord);
 		}
 		return null;
 	}
 	
 	private TexCommand getEnv(final String name) {
 		TexCommand command= this.commandSet.getLtxInternEnvMap().get(name);
 		if (command != null) {
 			return command;
 		}
 		if (this.inMath) {
 			command= this.commandSet.getLtxMathEnvMap().get(name);
 		}
 		else {
 			command= this.commandSet.getLtxTextEnvMap().get(name);
 		}
 		if (command != null) {
 			return command;
 		}
 		if (this.customEnvs != null) {
 			return this.customEnvs.get(name);
 		}
 		return null;
 	}
 	
 	
 	public void setCollectEmebeddedNodes(final boolean enable) {
 		this.embeddedList= (enable) ? new ArrayList<Embedded>(32) : null;
 	}
 	
 	public List<Embedded> getEmbeddedNodes() {
 		return this.embeddedList;
 	}
 	
 	public Map<String, TexCommand> getCustomCommandMap() {
 		return this.customCommands;
 	}
 	
 	public Map<String, TexCommand> getCustomEnvMap() {
 		return this.customEnvs;
 	}
 	
 	
 	public SourceComponent parse(final SourceParseInput input, final TexCommandSet commandSet) {
 		if (this.embeddedList != null) {
 			this.embeddedList.clear();
 		}
 		this.customCommands= null;
 		this.customEnvs= null;
 		this.commandSet= commandSet;
 		this.lexer.setInput(input);
 		this.lexer.setFull();
 		this.foundEndStackPos= -1;
 		this.stackPos= -1;
 		this.inMath= false;
 		
 		this.lexer.setReport$$(true);
 		this.lexer.setReportAsterisk(false);
 		this.lexer.setReportSquaredBrackets(true);
 		
 		final SourceComponent node= new SourceComponent();
 		if (this.lexer.next() != LtxLexer.EOF) {
 			node.fStartOffset= this.lexer.getOffset();
 		}
 		putToStack(ST_ROOT, null);
 		parseGroup(node);
 		node.fStopOffset= this.lexer.getStopOffset();
 		
 		return node;
 	}
 	
 	
 	private void putToStack(final byte type, final String key) {
 		if (++this.stackPos >= this.stackTypes.length) {
 			final byte[] newTypes= new byte[this.stackEndKeys.length+16];
 			System.arraycopy(this.stackTypes, 0, newTypes, 0, this.stackPos);
 			this.stackTypes= newTypes;
 			final String[] newKeys= new String[this.stackEndKeys.length+16];
 			System.arraycopy(this.stackEndKeys, 0, newKeys, 0, this.stackPos);
 			this.stackEndKeys= newKeys;
 		}
 		this.stackTypes[this.stackPos]= type;
 		this.stackEndKeys[this.stackPos]= key;
 	}
 	
 	private void putToStack(final byte type, final byte argContent) {
 		switch (argContent & 0xf0) {
 		case Argument.CONTROLWORD:
 		case Argument.LABEL:
 			putToStack(type, LABEL_TEXT);
 			break;
 		case Argument.NUM:
 			putToStack(type, NUM_TEXT);
 			break;
 		default:
 			putToStack(type, null);
 			break;
 		}
 	}
 	
 	private void parseGroup(final ContainerNode group) {
 		final List<TexAstNode> children= new ArrayList<>();
 		Text textNode= null;
 		
 		GROUP: while (this.foundEndStackPos < 0) {
 			switch (this.lexer.pop()) {
 			case LtxLexer.EOF:
 				group.fStopOffset= this.lexer.getStopOffset();
 				group.fStatus= STATUS2_GROUP_NOT_CLOSED;
 				this.foundEndStackPos= 0;
 				this.foundEndNode= null;
 				break GROUP;
 			case LtxLexer.CONTROL_WORD:
 				{	final TexAstNode node= createAndParseWord();
 					if (node != null) {
 						node.fParent= group;
 						children.add(node);
 						textNode= null;
 						continue GROUP;
 					}
 					else {
 						break GROUP;
 					}
 				}
 			case LtxLexer.CONTROL_CHAR:
 				this.wasLinebreak= false;
 				this.lexer.consume();
 				{	final ControlNode.Char node= new ControlNode.Char(this.lexer.getText());
 					node.fStartOffset= this.lexer.getOffset();
 					node.fStopOffset= this.lexer.getStopOffset();
 					if (this.inMath) {
 						if (node.getText() == ")") { //$NON-NLS-1$
 							int endPos= this.stackPos;
 							while (endPos >= 0) {
 								if (this.stackTypes[endPos] == ST_MATHROUND) {
 									this.foundEndStackPos= endPos;
 									this.foundEndNode= node;
 									break GROUP;
 								}
 								endPos--;
 							}
 						}
 						else if (node.getText() == "]") { //$NON-NLS-1$
 							int endPos= this.stackPos;
 							while (endPos >= 0) {
 								if (this.stackTypes[endPos] == ST_MATHSQUARED) {
 									this.foundEndStackPos= endPos;
 									this.foundEndNode= node;
 									break GROUP;
 								}
 								endPos--;
 							}
 						}
 						node.fParent= group;
 						children.add(node);
 						textNode= null;
 						continue GROUP;
 					}
 					else {
 						if (node.getText() == "(") { //$NON-NLS-1$
 							final Environment env= new Environment();
 							env.fParent= group;
 							env.fStartOffset= node.getOffset();
 							env.fBegin= node;
 							node.fParent= env;
 							putToStack(ST_MATHROUND, null);
 							parseGroup(env);
 							textNode= null;
 							continue GROUP;
 						}
 						else if (node.getText() == "[") { //$NON-NLS-1$
 							final Environment env= new Environment();
 							env.fParent= group;
 							env.fStartOffset= node.getOffset();
 							env.fBegin= node;
 							node.fParent= env;
 							putToStack(ST_MATHSQUARED, null);
 							parseGroup(env);
 							textNode= null;
 							continue GROUP;
 						}
 						node.fParent= group;
 						children.add(node);
 						textNode= null;
 						continue GROUP;
 					}
 				}
 			case LtxLexer.CURLY_BRACKET_OPEN:
 				this.wasLinebreak= false;
 				this.lexer.consume();
 				{	final Group node= new Group.Bracket(group, this.lexer.getOffset(), this.lexer.getStopOffset());
 					node.fStartOffset= this.lexer.getOffset();
 					node.fStopOffset= this.lexer.getStopOffset();
 					putToStack(ST_CURLY, null);
 					parseGroup(node);
 					node.fParent= group;
 					children.add(node);
 					textNode= null;
 					continue GROUP;
 				}
 			case LtxLexer.CURLY_BRACKET_CLOSE:
 				this.wasLinebreak= false;
 				this.lexer.consume();
 				if (this.stackTypes[this.stackPos] == ST_CURLY) {
 					this.foundEndStackPos= this.stackPos;
 					this.foundEndOffset= this.lexer.getStopOffset();
 					textNode= null;
 					break GROUP;
 				}
 				else {
 					final Dummy node= new Dummy();
 					node.fStatus= STATUS2_GROUP_NOT_OPENED;
 					node.fStartOffset= this.lexer.getOffset();
 					node.fStopOffset= this.lexer.getStopOffset();
 					node.fParent= group;
 					children.add(node);
 					textNode= null;
 					continue GROUP;
 				}
 			case LtxLexer.SQUARED_BRACKET_CLOSE:
 				this.wasLinebreak= false;
 				this.lexer.consume();
 				if (this.stackTypes[this.stackPos] == ST_SQUARED) {
 					this.foundEndStackPos= this.stackPos;
 					this.foundEndOffset= this.lexer.getStopOffset();
 					break GROUP;
 				}
 				if (textNode == null) {
 					textNode= new Text(group, this.lexer.getOffset(), this.lexer.getStopOffset());
 					children.add(textNode);
 					continue GROUP;
 				}
 				else if (textNode == this.whitespace) {
 					textNode= new Text(group, textNode.fStartOffset, this.lexer.getStopOffset());
 					children.add(textNode);
 					continue GROUP;
 				}
 				else {
 					textNode.fStopOffset= this.lexer.getStopOffset();
 					continue GROUP;
 				}
 			case LtxLexer.WHITESPACE:
 				this.lexer.consume();
 				if (textNode == null) {
 					if (children.size() > 0) {
 						textNode= this.whitespace;
 						textNode.fStartOffset= this.lexer.getOffset();
 						continue GROUP;
 					}
 					continue GROUP;
 				}
 				else if (textNode == this.whitespace) {
 					continue GROUP;
 				}
 				else {
 					textNode.fStopOffset= this.lexer.getStopOffset();
 					continue GROUP;
 				}
 			case LtxLexer.DEFAULT_TEXT:
 				this.wasLinebreak= false;
 				this.lexer.consume();
 				if (this.stackEndKeys[this.stackPos] == LABEL_TEXT && children.isEmpty()) {
 					final Label node= new Label(group, this.lexer.getOffset(), this.lexer.getStopOffset(),
 							this.lexer.getFullText(this.labelFactory) );
 					node.fStartOffset= this.lexer.getOffset();
 					node.fStopOffset= this.lexer.getStopOffset();
 					node.fParent= group;
 					children.add(node);
 					continue GROUP;
 				}
 				if (this.stackEndKeys[this.stackPos] == NUM_TEXT && children.isEmpty()) {
 					final Text node= new Text.Num(group, this.lexer.getOffset(), this.lexer.getStopOffset(),
 							checkNum(this.lexer.getFullText(this.otherFactory)) );
 					node.fStartOffset= this.lexer.getOffset();
 					node.fStopOffset= this.lexer.getStopOffset();
 					node.fParent= group;
 					children.add(node);
 					continue GROUP;
 				}
 				if (textNode == null) {
 					textNode= new Text(group, this.lexer.getOffset(), this.lexer.getStopOffset());
 					children.add(textNode);
 					continue GROUP;
 				}
 				else if (textNode == this.whitespace) {
 					textNode= new Text(group, textNode.fStartOffset, this.lexer.getStopOffset());
 					children.add(textNode);
 					continue GROUP;
 				}
 				else {
 					textNode.fStopOffset= this.lexer.getStopOffset();
 					continue GROUP;
 				}
 			case LtxLexer.CONTROL_NONE:
 			case LtxLexer.SQUARED_BRACKET_OPEN:
 				this.wasLinebreak= false;
 				this.lexer.consume();
 				if (textNode == null) {
 					textNode= new Text(group, this.lexer.getOffset(), this.lexer.getStopOffset());
 					children.add(textNode);
 					continue GROUP;
 				}
 				else {
 					textNode.fStopOffset= this.lexer.getStopOffset();
 					continue GROUP;
 				}
 			case LtxLexer.LINEBREAK:
 				this.wasLinebreak= true;
 				this.lexer.consume();
 				textNode= null;
 				continue GROUP;
 			case LtxLexer.MATH_$:
 				this.wasLinebreak= false;
 				this.lexer.consume();
 				if (this.inMath) {
 					int endPos= this.stackPos;
 					while (endPos >= 0) {
 						if (this.stackTypes[endPos] == ST_MATHDOLLAR) {
 							this.foundEndStackPos= endPos;
 							this.foundEndOffset= this.lexer.getStopOffset();
 							break GROUP;
 						}
 						endPos--;
 					}
 					if (textNode == null || textNode == this.whitespace) {
 						textNode= new Text(group, this.lexer.getOffset(), this.lexer.getStopOffset());
 						children.add(textNode);
 						continue GROUP;
 					}
 					textNode.fStopOffset= this.lexer.getStopOffset();
 					continue GROUP;
 				}
 				else {
 					putToStack(ST_MATHDOLLAR, null);
 					this.inMath= true;
 					this.lexer.setReport$$(false);
 					final Math node= new Math.SingleDollar(group, this.lexer.getOffset(), this.lexer.getStopOffset());
 					parseGroup(node);
 					this.inMath= false;
 					this.lexer.setReport$$(true);
 					children.add(node);
 					textNode= null;
 					continue GROUP;
 				}
 			case LtxLexer.MATH_$$:
 				this.wasLinebreak= false;
 				this.lexer.consume();
 				if (this.inMath) {
 					int endPos= this.stackPos;
 					while (endPos >= 0) {
 						if (this.stackTypes[endPos] == ST_MATHDOLLARDOLLAR) {
 							this.foundEndStackPos= endPos;
 							this.foundEndOffset= this.lexer.getStopOffset();
 							break GROUP;
 						}
 						endPos--;
 					}
 					group.fStopOffset= this.lexer.getStopOffset();
 					if (textNode == null || textNode == this.whitespace) {
 						textNode= new Text(group, this.lexer.getOffset(), this.lexer.getStopOffset());
 						children.add(textNode);
 						continue GROUP;
 					}
 					textNode.fStopOffset= this.lexer.getStopOffset();
 					continue GROUP;
 				}
 				else {
 					putToStack(ST_MATHDOLLARDOLLAR, null);
 					this.inMath= true;
 					final Math node= new Math.DoubleDollar(group, this.lexer.getOffset(), this.lexer.getStopOffset());
 					parseGroup(node);
 					this.inMath= false;
 					children.add(node);
 					textNode= null;
 					continue GROUP;
 				}
 			case LtxLexer.LINE_COMMENT:
 				handleComment();
 				continue GROUP;
 			case LtxLexer.VERBATIM_TEXT:
 				this.wasLinebreak= false;
 				this.lexer.consume();
 				{	final Verbatim node= new Verbatim();
 					node.fParent= group;
 					node.fStartOffset= this.lexer.getOffset();
 					node.fStopOffset= this.lexer.getStopOffset();
 					children.add(node);
 					textNode= null;
 					continue GROUP;
 				}
 			case LtxLexer.EMBEDDED:
 				this.wasLinebreak= false;
 				this.lexer.consume();
 				{	final Embedded node= new Embedded(group, this.lexer.getOffset(), this.lexer.getStopOffset(),
 							this.lexer.getText().intern() );
 					children.add(node);
 					if (this.embeddedList != null) {
 						this.embeddedList.add(node);
 					}
 					textNode= null;
 					continue GROUP;
 				}
 			case LtxLexer.ASTERISK:
 			default:
 				throw new IllegalStateException("type= " + this.lexer.getType() + ", offset= "+this.lexer.getOffset()); //$NON-NLS-1$ //$NON-NLS-2$
 			}
 		}
 		
 		if (!children.isEmpty()) {
 			group.fChildren= children.toArray(new TexAstNode[children.size()]);
 		}
 		
 //		if (fFoundEndStackPos >= 0) {
 			if (this.foundEndStackPos == this.stackPos) {
 				group.setEndNode(this.foundEndOffset, this.foundEndNode);
 				this.foundEndStackPos= -1;
 			}
 			else {
 				group.setMissingEnd();
 			}
 //		}
 		this.stackPos--;
 	}
 	
 	private TexAstNode createAndParseWord() {
 		String label= null;
 		this.wasLinebreak= false;
 		this.lexer.consume();
 		final ControlNode.Word controlNode= new ControlNode.Word(label= this.lexer.getText());
 		controlNode.fStartOffset= this.lexer.getOffset();
 		controlNode.fStopOffset= this.lexer.getStopOffset();
 		
 		TexCommand command= getCommand(label);
 		if (command == null) {
 			if (this.lexer.pop() == LtxLexer.WHITESPACE) {
 				this.lexer.consume();
 			}
 			return controlNode;
 		}
 		
 		if (command.supportAsterisk()) {
 			this.lexer.setReportAsterisk(true);
 			ASTERISK: while (true) {
 				switch (this.lexer.pop()) {
 				case LtxLexer.WHITESPACE:
 					this.lexer.consume();
 					continue ASTERISK;
 				case LtxLexer.ASTERISK:
 					this.wasLinebreak= false;
 					this.lexer.consume();
 					break ASTERISK;
 				default:
 					break ASTERISK;
 				}
 			}
 			this.lexer.setReportAsterisk(false);
 		}
 		List<Argument> arguments;
 		if (!(arguments= command.getArguments()).isEmpty()) {
 			int nextArg= 0;
 			final List<TexAstNode> children= new ArrayList<>();
 			ARGUMENTS: while (this.foundEndStackPos < 0 && nextArg < arguments.size()) {
 				Argument argument= arguments.get(nextArg);
 				boolean optional= ((argument.getType() & Argument.OPTIONAL) != 0);
 				final ContainerNode argNode;
 				switch (this.lexer.pop()) {
 				case LtxLexer.WHITESPACE:
 					this.lexer.consume();
 					continue ARGUMENTS;
 				case LtxLexer.LINEBREAK:
 					if (this.wasLinebreak) {
 						break ARGUMENTS;
 					}
 					this.wasLinebreak= true;
 					this.lexer.consume();
 					continue ARGUMENTS;
 				case LtxLexer.SQUARED_BRACKET_OPEN:
 					if (!optional) {
 						break ARGUMENTS;
 					}
 					
 					this.wasLinebreak= false;
 					this.lexer.consume();
 					argNode= new Group.Square(controlNode, this.lexer.getOffset(), this.lexer.getStopOffset());
 					putToStack(ST_SQUARED, argument.getContent());
 					if (argument.getContent() == Argument.EMBEDDED) {
 						parseEmbedGroup(argNode, ((TexEmbedCommand) command).getEmbeddedType());
 					}
 					else {
 						parseGroup(argNode);
 					}
 					controlNode.fStopOffset= argNode.fStopOffset;
 					children.add(argNode);
 					
 					nextArg++;
 					continue ARGUMENTS;
 				case LtxLexer.CURLY_BRACKET_OPEN:
 					while (optional) {
 						if (++nextArg >= arguments.size()) {
 							break ARGUMENTS;
 						}
 						argument= arguments.get(nextArg);
 						optional= (argument.getType() == Argument.OPTIONAL);
 					}
 					if (argument.getType() != Argument.REQUIRED) {
 						break ARGUMENTS;
 					}
 					
 					this.wasLinebreak= false;
 					this.lexer.consume();
 					argNode= new Group.Bracket(controlNode, this.lexer.getOffset(), this.lexer.getStopOffset());
 					putToStack(ST_CURLY, argument.getContent());
 					if (argument.getContent() == Argument.EMBEDDED) {
 						parseEmbedGroup(argNode, ((TexEmbedCommand) command).getEmbeddedType());
 					}
 					else {
 						parseGroup(argNode);
 					}
 					controlNode.fStopOffset= argNode.fStopOffset;
 					children.add(argNode);
 					
 					if (command.getType() == TexCommand.C2_GENERICENV_BEGIN) {
 						if (argNode.fStatus == 0 && argNode.fChildren.length == 1
 								&& argNode.fChildren[0].getNodeType() == NodeType.LABEL) {
 							final TexCommand envCommand= getEnv(label= argNode.fChildren[0].getText());
 							if (envCommand != null) {
 								command= envCommand;
 								arguments= command.getArguments();
 							}
 						}
 						else {
 							break ARGUMENTS;
 						}
 					}
 					
 					nextArg++;
 					continue ARGUMENTS;
 				default:
 					break ARGUMENTS;
 				}
 			}
 			
 			controlNode.fArguments= children.toArray(new TexAstNode[children.size()]);
 		}
 		
 		controlNode.fCommand= command;
 		
 		if (this.foundEndStackPos >= 0) {
 			return controlNode;
 		}
 		
 		switch (command.getType() & TexCommand.MASK_C2) {
 		case TexCommand.C2_ENV_VERBATIM_BEGIN:
 		case TexCommand.C2_ENV_COMMENT_BEGIN:
 			if (this.lexer.getType() != 0) {
 				break;
 			}
 			this.lexer.setModeVerbatimEnv(("end{" + label + "}").toCharArray()); //$NON-NLS-1$ //$NON-NLS-2$
 			if (this.lexer.next() == LtxLexer.VERBATIM_TEXT) {
 				final Environment envNode= new Environment();
 				envNode.fBegin= controlNode;
 				envNode.fStartOffset= controlNode.fStartOffset;
 				putToStack(ST_BEGINEND, label);
 				parseGroup(envNode);
 				return envNode;
 			}
 			else {
 				throw new IllegalStateException();
 			}
 		case TexCommand.VERBATIM_INLINE:
 			if (this.lexer.getType() != 0) {
 				break;
 			}
 			this.lexer.setModeVerbatimLine();
 			if (this.lexer.next() == LtxLexer.VERBATIM_TEXT) {
 				this.wasLinebreak= false;
 				this.lexer.consume();
 				final TexAstNode verbatimNode;
 				switch (this.lexer.getSubtype()) {
 				case LtxLexer.SUB_OPEN_MISSING:
 //					verbatimNode= new Dummy();
 //					verbatimNode.fParent= controlNode;
 //					verbatimNode.fStatus= STATUS2_VERBATIM_INLINE_C_MISSING;
 //					verbatimNode.fStartOffset= verbatimNode.fStopOffset= controlNode.fStopOffset =
 //							fLexer.getStopOffset();
 					controlNode.fStatus= STATUS2_VERBATIM_INLINE_C_MISSING;
 					return controlNode;
 				case LtxLexer.SUB_CLOSE_MISSING:
 					verbatimNode= new Verbatim();
 					verbatimNode.fParent= controlNode;
 					verbatimNode.fStatus= STATUS2_VERBATIM_INLINE_NOT_CLOSED;
 					verbatimNode.fStartOffset= this.lexer.getOffset() + 1;
 					verbatimNode.fStopOffset= controlNode.fStopOffset =
 							this.lexer.getStopOffset();
 					break;
 				default:
 					verbatimNode= new Verbatim();
 					verbatimNode.fParent= controlNode;
 					verbatimNode.fStartOffset= this.lexer.getOffset() + 1;
 					verbatimNode.fStopOffset= controlNode.fStopOffset =
 							this.lexer.getStopOffset() - 1;
 					break;
 				}
 				controlNode.fArguments= new TexAstNode[] { verbatimNode };
 				
 				return controlNode;
 			}
 			else {
 				throw new IllegalStateException();
 			}
 		case TexCommand.C2_ENV_MATH_BEGIN:
 			{
 				final Environment envNode= new Environment();
 				controlNode.fParent= envNode;
 				envNode.fStartOffset= controlNode.fStartOffset;
 				envNode.fBegin= controlNode;
 				putToStack(ST_BEGINEND, label);
 				this.inMath= true;
 				parseGroup(envNode);
 				this.inMath= false;
 				return envNode;
 			}
 		case TexCommand.C2_GENERICENV_BEGIN:
 		case TexCommand.C2_ENV_DOCUMENT_BEGIN:
 		case TexCommand.C2_ENV_ELEMENT_BEGIN:
 		case TexCommand.C2_ENV_MATHCONTENT_BEGIN:
 		case TexCommand.C2_ENV_OTHER_BEGIN:
 			if (label != null && label != "begin") { //$NON-NLS-1$
 				final Environment envNode= new Environment();
 				controlNode.fParent= envNode;
 				envNode.fStartOffset= controlNode.fStartOffset;
 				envNode.fBegin= controlNode;
 				putToStack(ST_BEGINEND, label);
 				parseGroup(envNode);
 				return envNode;
 			}
 			else {
 				controlNode.fStatus= STATUS2_ENV_MISSING_NAME;
 			}
 			return controlNode;
 		case TexCommand.C2_GENERICENV_END:
 			if (controlNode.fArguments.length == 1) {
 				final Group argNode= (Group) controlNode.fArguments[0];
 				if (argNode.fStatus == 0 && argNode.fChildren.length == 1
 						&& argNode.fChildren[0].getNodeType() == NodeType.LABEL) {
 					label= argNode.fChildren[0].getText();
 					int endPos= this.stackPos;
 					while (endPos >= 0) {
 						if (this.stackEndKeys[endPos] == label) {
 							this.foundEndStackPos= endPos;
 							this.foundEndNode= controlNode;
 							return null;
 						}
 						endPos--;
 					}
 //					if (fStackEndKeys[fStackPos].length() > 1) {
 //						fFoundEndStackPos= fStackPos;
 //						fFoundEndNode= controlNode;
 //						return null;
 //					}
 					controlNode.fStatus= STATUS2_ENV_NOT_OPENED;
 				}
 			}
 			controlNode.fStatus= STATUS2_ENV_MISSING_NAME;
 			return controlNode;
 		case TexCommand.C2_PREAMBLE_CONTROLDEF:
 			if (controlNode.fArguments.length > 0) {
 				parseDef(controlNode, command);
 			}
 			return controlNode;
 		default:
 			break;
 		}
 		return controlNode;
 	}
 	
 	private String checkNum(final String text) {
 		if (text.isEmpty()) {
 			return null;
 		}
 		for (int i= 0; i < text.length(); i++) {
 			final char c= text.charAt(i);
 			if (c < '0' || c > '9') {
 				return null;
 			}
 		}
 		return text;
 	}
 	
 	private void parseDef(final ControlNode node, final TexCommand command) {
		final int type;
		switch ((command.getType() & TexCommand.MASK_C3)) {
		case TexCommand.C3_PREAMBLE_CONTROLDEF_COMMAND:
			type= 0;
			break;
		case TexCommand.C3_PREAMBLE_CONTROLDEF_ENV:
 			type= 1;
			break;
		default:
			return;
 		}
		
 		final String controlWord;
 		final TexAstNode[] argNodes= TexAst.resolveArguments(node);
 		TexAstNode argNode;
 		if (argNodes[0] == null || argNodes[0].fStatus != 0 || argNodes[0].getChildCount() != 1) {
 			return;
 		}
 		argNode= argNodes[0].getChild(0);
 		if (type == 1 && argNode.getNodeType() == NodeType.LABEL
 				&& argNode.getText().length() > 0) {
 			controlWord= argNode.getText();
 		}
 		else if (argNode.getNodeType() == NodeType.CONTROL
 				&& argNode.getText().length() > 0) {
 			controlWord= argNode.getText();
 		}
 		else {
 			return;
 		}
 		int optionalArgs= 0;
 		int requiredArgs= 0;
 		if (argNodes[1] != null && argNodes[1].fStatus == 0 && argNodes[1].getChildCount() == 1
 				&& (argNode= argNodes[1].getChild(0)).getNodeType() == NodeType.TEXT
 				&& argNode.getText() != null) {
 			try {
 				requiredArgs= Integer.parseInt(argNode.getText());
 			}
 			catch (final NumberFormatException e) {}
 		}
 		if (argNodes[2] != null && argNodes[2].fStatus == 0) {
 			optionalArgs= 1;
 			requiredArgs -= 1;
 		}
 		if (requiredArgs < 0) {
 			requiredArgs= 0;
 		}
 		final Argument[] args= new Argument[optionalArgs + requiredArgs];
 		{	int i= 0;
 			while (optionalArgs-- > 0) {
 				args[i++]= new Argument(Argument.OPTIONAL, Argument.NONE);
 			}
 			while (requiredArgs-- > 0) {
 				args[i++]= new Argument(Argument.REQUIRED, Argument.NONE);
 			}
 		}
 		{	final Map<String, TexCommand> map;
 			if (type == 1) {
 				if (this.customEnvs == null) {
 					this.customEnvs= new HashMap<>();
 				}
 				map= this.customEnvs;
 			}
 			else {
 				if (this.customCommands == null) {
 					this.customCommands= new HashMap<>();
 				}
 				map= this.customCommands;
 			}
 			map.put(controlWord, new TexCommand(0, controlWord, false,
 					new ConstArrayList<>(args), "(custom)"));
 		}
 	}
 	
 	private void handleComment() {
 		this.wasLinebreak= false;
 		this.lexer.consume();
 	}
 	
 	private void parseEmbedGroup(final ContainerNode group, final String type) {
 		final Embedded embedded= new Embedded.Inline(group, this.lexer.getStopOffset(), type);
 		GROUP: while (this.foundEndStackPos < 0) {
 			switch (this.lexer.pop()) {
 			case LtxLexer.EOF:
 				this.foundEndStackPos= 0;
 				this.foundEndNode= null;
 				break GROUP;
 			case LtxLexer.LINEBREAK:
 				break GROUP;
 			case LtxLexer.CURLY_BRACKET_CLOSE:
 				this.wasLinebreak= false;
 				this.lexer.consume();
 				if (this.stackTypes[this.stackPos] == ST_CURLY) {
 					this.foundEndStackPos= this.stackPos;
 					this.foundEndOffset= this.lexer.getStopOffset();
 					break GROUP;
 				}
 				continue GROUP;
 			case LtxLexer.SQUARED_BRACKET_CLOSE:
 				this.wasLinebreak= false;
 				this.lexer.consume();
 				if (this.stackTypes[this.stackPos] == ST_SQUARED) {
 					this.foundEndStackPos= this.stackPos;
 					this.foundEndOffset= this.lexer.getStopOffset();
 					break GROUP;
 				}
 				continue GROUP;
 			default:
 				this.wasLinebreak= false;
 				this.lexer.consume();
 				continue GROUP;
 			}
 		}
 		
 		embedded.fStopOffset= this.lexer.getOffset();
 		group.fChildren= new TexAstNode[] { embedded };
 		if (this.embeddedList != null) {
 			this.embeddedList.add(embedded);
 		}
 		
 		if (this.foundEndStackPos == this.stackPos) {
 			group.setEndNode(this.foundEndOffset, this.foundEndNode);
 			this.foundEndStackPos= -1;
 		}
 		else {
 			group.setMissingEnd();
 		}
 		this.stackPos--;
 	}
 	
 }
