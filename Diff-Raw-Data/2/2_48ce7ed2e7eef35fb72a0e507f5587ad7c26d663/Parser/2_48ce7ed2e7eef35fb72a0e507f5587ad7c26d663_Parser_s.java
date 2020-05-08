 package lambda.ast.parser;
 
 import java.util.Scanner;
 
 import lambda.ast.ASTAbstract;
 import lambda.ast.ASTApply;
 import lambda.ast.ASTLiteral;
 import lambda.ast.ASTMacro;
 import lambda.ast.Lambda;
 
 public class Parser
 {
 	private Lexer lexer;
 	private Token token;
 
 	public Parser(Lexer lexer)
 	{
 		this.lexer = lexer;
 	}
 
 	public Lambda parse() throws ParserException
 	{
 		next();
 		Lambda ast = abstraction();
 		if (token.type == TokenType.END)
 		{
 			return ast;
 		}
 		throw new ParserException("Syntax error, extra tokens.", token);
 	}
 
 	private Lambda abstraction() throws ParserException
 	{
 		if (token.type == TokenType.LAMBDA)
 		{
 			next();
 			if (token.type == TokenType.ID)
 			{
 				String id = token.text;
 				next();
 				return new ASTAbstract(id, id, absList());
 			}
 			if (token.type == TokenType.DOT)
 			{
 				throw new ParserException("Syntax error, empty formals.", token);
 			}
 			throw new ParserException("Syntax error, expected variable names.", token);
 		}
 		return appList();
 	}
 
 	private Lambda absList() throws ParserException
 	{
 		if (token.type == TokenType.ID)
 		{
 			String id = token.text;
 			next();
 			return new ASTAbstract(id, id, absList());
 		}
 		if (token.type == TokenType.DOT)
 		{
 			next();
 			return abstraction();
 		}
 		throw new ParserException("Syntax error, unexpected token.", token);
 	}
 
 	private Lambda appList() throws ParserException
 	{
 		Lambda e = atomic();
 		TokenType t = token.type;
 		while (t == TokenType.ID || t == TokenType.LPAR || t == TokenType.MACRONAME)
 		{
 			e = new ASTApply(e, atomic());
 			t = token.type;
 		}
 		return e;
 	}
 
 	private Lambda atomic() throws ParserException
 	{
 		switch (token.type)
 		{
 		case ID:
 		{
 			String name = token.text;
 			next();
 			return new ASTLiteral(name);
 		}
 		case MACRONAME:
 		{
 			String name = token.text;
 			next();
 			return new ASTMacro(name);
 		}
 		case LPAR:
 			next();
 			Lambda e = abstraction();
 			if (token.type == TokenType.RPAR)
 			{
 				next();
 			}
 			else
 			{
 				throw new ParserException("Syntax error, missing ')'.", token);
 			}
 			return e;
 		}
 		throw new ParserException("Syntax error, unexpected token.", token);
 	}
 
 	private void next() throws ParserException
 	{
 		try
 		{
 			token = lexer.nextToken();
 		}
 		catch (LexerException e)
 		{
 			throw new ParserException("Lexical error, " + e.getMessage(), e.column);
 		}
 	}
 
 	public static void main(String[] args)
 	{
 		System.out.println("Type :q to quit.");
 		Scanner sc = new Scanner(System.in);
 		while (true)
 		{
 			System.out.print("> ");
 			String line = sc.nextLine();
 			if ((line == null) || (line.equals(":q")))
 				break;
 			Parser parser = new Parser(new Lexer(line));
 			try
 			{
 				Lambda ast = parser.parse();
 				System.out.println("parsed: " + ast);
 			}
 			catch (ParserException e)
 			{
 				System.out.println(line);
 				for (int i = 0; i < e.column; i++)
 				{
 					System.out.print(' ');
 				}
 				System.out.println('^');
 				System.out.println(e.getMessage());
 			}
 		}
 	}
 }
