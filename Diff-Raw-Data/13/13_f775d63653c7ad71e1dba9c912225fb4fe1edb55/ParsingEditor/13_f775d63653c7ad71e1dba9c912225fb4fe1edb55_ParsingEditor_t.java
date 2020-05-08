 package org.ggll.parser;
 
 import static org.apache.commons.lang3.StringEscapeUtils.escapeHtml4;
 import ggll.core.compile.ClassLoader;
 import ggll.core.compile.Compiler;
 import ggll.core.exceptions.ErrorRecoveryException;
 import ggll.core.exceptions.LexicalException;
 import ggll.core.exceptions.SemanticException;
 import ggll.core.exceptions.SintaticException;
 import ggll.core.lexical.YyFactory;
 import ggll.core.lexical.Yylex;
 import ggll.core.semantics.SemanticRoutineClass;
 import ggll.core.syntax.model.ParseNode;
 import ggll.core.syntax.model.ParseStack;
 import ggll.core.syntax.parser.GGLLTable;
 import ggll.core.syntax.parser.Parser;
 import ggll.core.syntax.parser.ParserOutput;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.StringReader;
 import java.util.Iterator;
 
 import javax.swing.JOptionPane;
 
 import org.ggll.director.GGLLDirector;
 import org.ggll.output.AppOutput;
 import org.ggll.output.HtmlViewer.TOPIC;
 import org.ggll.output.Output;
 import org.ggll.parser.syntax.SyntacticLoader;
 import org.ggll.util.Log;
 
 public class ParsingEditor
 {
 	private static ParsingEditor instance;
 	private SyntacticLoader syntacticLoader;
 	private Yylex yylex;
 	private Parser analyzer;
 
 	private final String rootPath;
 
 	public ParsingEditor(SyntacticLoader syntacticLoader, String rootPath)
 	{
 		instance = this;
 		this.rootPath = rootPath;
 		this.syntacticLoader = syntacticLoader;
 	}
 
 	public static ParsingEditor getInstance()
 	{
 		return instance;
 	}
 
 	private void endParser()
 	{
 		if (this.analyzer.isSucess())
 		{
 			AppOutput.displayText("<font color='green'>Expression Successfully recognized.</font>", TOPIC.Output);
 		}
 		else
 		{
 			AppOutput.displayText("<font color='red'>Expression can't be recognized.</font>", TOPIC.Output);
 			for (final Exception error : this.analyzer.getErrorList().getAll())
 			{
 				if (error instanceof SintaticException)
 				{
 					final SintaticException sintaticException = (SintaticException) error;
					AppOutput.displayText("<font color='red'>" + sintaticException.getMessage() + "</font>", TOPIC.Output);
 				}
 				else if (error instanceof ErrorRecoveryException)
 				{
 					final ErrorRecoveryException errorRecoveryException = (ErrorRecoveryException) error;
 					AppOutput.displayText("<font color='red'>Erro Recovery: " + errorRecoveryException.getMessage() + "</font>", TOPIC.Output);
 				}
 				else if (error instanceof LexicalException)
 				{
 					final LexicalException lexicalException = (LexicalException) error;
					AppOutput.displayText("<font color='red'>Lexical Error: \"" + lexicalException.getToken() + "\"</font>", TOPIC.Output);
 				}
 				else if (error instanceof SemanticException)
 				{
 					final SemanticException semanticException = (SemanticException) error;
 					AppOutput.displayText("<font color='red'>Semantic Error: " + semanticException.getMessage() + "</font>", TOPIC.Output);
 				}
 			}
 		}
 		this.analyzer = null;
 	}
 
 	private void startParse(boolean stepping, String text)
 	{
 		Output.getInstance().displayTextExt(escapeHtml4(text), TOPIC.Parser);
 		final StringReader stringReader = new StringReader(text);
 		try
 		{
 			this.yylex.yyreset(stringReader);
 		}
 		catch (final IOException e1)
 		{
 			Log.log(Log.ERROR, this, "An internal error has occurred!", e1);
 		}
 
 		try
 		{
 			final Compiler compiler = new Compiler();
 			compiler.compile(GGLLDirector.getProject().getSemanticFile().getPath());
 			final ClassLoader<SemanticRoutineClass> classLoader = new ClassLoader<SemanticRoutineClass>(GGLLDirector.getProject().getSemanticFile());
 			this.analyzer = new Parser(new GGLLTable(this.syntacticLoader.tabGraph(), this.syntacticLoader.tabNt(), this.syntacticLoader.tabT()), this.yylex, classLoader.getInstance(), stepping);
 			this.analyzer.setParserOutput(new ParserOutput()
 			{
 				@Override
 				public void Output()
 				{
 					printStack(ParsingEditor.this.analyzer.getParserStacks().getParseStack());
 				}
 			});
 		}
 		catch (final Exception e)
 		{
 			e.printStackTrace();
 		}
 
 	}
 
 	public ParsingEditor build()
 	{
 		try
 		{
 			YyFactory yyFactory = new YyFactory();
 			this.yylex = yyFactory.getYylex(new File(this.rootPath + "/export/Yylex.java"));
 			return instance;
 		}
 		catch (final Exception e)
 		{
 			e.printStackTrace();
 			return null;
 		}
 	}
 
 	public void printStack(ParseStack parseStackNode)
 	{
 		final Iterator<ParseNode> iterator = parseStackNode.iterator();
 		ParseNode parseStackNodeTemp = null;
 		String lineSyntax = "";
 		String lineSemantic = "";
 		while (iterator.hasNext())
 		{
 			parseStackNodeTemp = iterator.next();
 			lineSyntax += "<a style='color: #000000; font-weight: bold;' href='Id|" + parseStackNodeTemp.getFlag() + "'>" + parseStackNodeTemp.getType() + "</a>&nbsp;";
 			lineSemantic += parseStackNodeTemp.getSemanticSymbol() + "&nbsp;";
 		}
 
 		AppOutput.showAndSelectNode(parseStackNode.peek().getFlag());
 		AppOutput.printlnSyntaxStack(lineSyntax, true);
 		AppOutput.printlnSemanticStack(lineSemantic, true);
 	}
 
 	public void run(boolean stepping, String text)
 	{
 		try
 		{
 			if (this.analyzer != null)
 			{
 				this.analyzer.nextToEnd();
 				endParser();
 				return;
 			}
 			if (this.syntacticLoader == null)
 			{
 				return;
 			}
 			if (text.equals(""))
 			{
 				JOptionPane.showMessageDialog(null, "There is nothing to parse", "Can not parse", JOptionPane.WARNING_MESSAGE);
 				return;
 			}
 
 			startParse(stepping, text);
 
 			AppOutput.clearStacks();
 			this.analyzer.run();
 			if (!stepping)
 			{
 				endParser();
 			}
 		}
 		catch (final Exception e)
 		{
 			e.printStackTrace();
 		}
 	}
 
 	public void setSyntacticLoader(SyntacticLoader cs)
 	{
 		this.syntacticLoader = cs;
 	}
 
 	public void stepRun(String text)
 	{
 		try
 		{
 			if (this.analyzer == null)
 			{
 				run(true, text);
 			}
 			if (this.analyzer == null)
 			{
 				return;
 			}
 			if (!this.analyzer.next())
 			{
 				endParser();
 			}
 		}
 		catch (final Exception e)
 		{
 			e.printStackTrace();
 		}
 
 	}
 
 }
