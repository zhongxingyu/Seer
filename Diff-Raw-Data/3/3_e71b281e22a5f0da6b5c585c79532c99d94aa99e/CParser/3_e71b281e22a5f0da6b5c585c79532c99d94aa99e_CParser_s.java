 // Copyright (C) 2013
 // Author: Vincenzo Lo Cicero.
 // e-mail: vincenzo.locicero@live.it
 //
 // This program is free software: you can redistribute it and/or modify it under the
 // terms of the GNU General Public License as published by the Free Software
 // Foundation, either version 3 of the License, or (at your option) any later version.
 // This program is distributed in the hope that it will be useful, but WITHOUT
 // ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 // or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 // Public License for more details.
 // You should have received a copy of the GNU General Public License along with
 // this program. If not, see http://www.gnu.org/licenses/.
 
 // Questo programma è software free: puoi redistribuirlo e/o modificarlo a piacere
 // sotto le condizioni della GNU General Public License così come pubblicata da Free Software
 // Foundation, o la versione 3 della licenza o (a propria scelta) una versione successiva. 
 // Questo programma è distribuito nella speranza che sia utile, ma SENZA ALCUNA GARANZIA;
 // senza nemmeno la garanzia implicita di commerciabilità o idoneità per uno SCOPO PARTICOLARE.
 // Vedi la GNU General Public License per maggiori dettagli. 
 // Dovresti aver ricevuto una copia della GNU General Public License insieme a questo programma.
 // Se questo non è avvenuto, vedi qui: http://www.gnu.org/licenses/.
 
 import java.lang.String;
 import java.lang.StringBuilder;
 
 /*
 expr  : expr1 {('+' | '-') expr1};
 expr1 : expr2 {('*' | '/') expr2};
 expr2 : ['-'] expr3;
 expr3 : T_NUMBER | '(' expr ')'
 */
 
 public class CParser
 {
 	public CParser()
 	{
 		m_Lexer = new CLexer();	
 		m_top = -1;
 		m_value = 0;
 		m_stack = new double[255];
 	}
 
 	public boolean Parse(String strExpr)
 	{
 		boolean ret = true;
 
 		m_strExpr = new String(strExpr);
 		m_top = -1;
 		m_value = 0;		
 
 		m_Lexer.SetExpr(strExpr);
 
 		m_Lexer.GetNextToken();
 
 		while ( ret && m_Lexer.m_currToken.Type != CLexer.TokenTypeEnum.T_EOL )
 		{
 			ret = expr();
 		}
 
 		if ( m_top >= 0 )
 			m_value = m_stack[m_top--];
 		m_top = -1;
 
 		return ret;	
 	}
 	
 	public double GetValue()
 	{
 		return m_value;
 	}
 
 	//expr  : expr1 {('+' | '-') expr1};
 	private boolean expr()
 	{
 		double right, left;
 		CLexer.TokenTypeEnum currToken;
 
 		if ( !expr1() )
 			return false;
 
 		while ( m_Lexer.m_currToken.Type == CLexer.TokenTypeEnum.T_PLUS ||
 				m_Lexer.m_currToken.Type == CLexer.TokenTypeEnum.T_MINUS )
 		{
 			currToken = m_Lexer.m_currToken.Type;
 			m_Lexer.GetNextToken();
 
 			if ( !expr1() )
 				return false;
 
 			right = m_stack[m_top--];
 			left  = m_stack[m_top--];
 
 			if ( currToken == CLexer.TokenTypeEnum.T_PLUS )
 				m_stack[++m_top] = left + right;
 			else if ( currToken == CLexer.TokenTypeEnum.T_MINUS )
 				m_stack[++m_top] = left - right;
 
 		}
 
 		return true;		
 	}
 	
 	//expr1 : expr2 {('*' | '/') expr2};
 	private boolean expr1()
 	{
 		double right, left;
 		CLexer.TokenTypeEnum currToken;
 
 		if ( !expr2() )
 			return false;
 
 		while ( m_Lexer.m_currToken.Type == CLexer.TokenTypeEnum.T_MULT ||
 				m_Lexer.m_currToken.Type == CLexer.TokenTypeEnum.T_DIV )
 		{
 			currToken = m_Lexer.m_currToken.Type;
 			m_Lexer.GetNextToken();
 
 			if ( !expr2() )
 				return false;
 
 			right = m_stack[m_top--];
 			left  = m_stack[m_top--];
 
 			if ( currToken == CLexer.TokenTypeEnum.T_MULT )
 				m_stack[++m_top] = left * right;
 			else if ( currToken == CLexer.TokenTypeEnum.T_DIV )
 			{
 				if ( right == 0 )
 				{
 					//System.out.println("Errore: divisione per zero.");
 					System.out.println("Error: division by zero.");					
 					return false;
 				}
 				m_stack[++m_top] = left / right;
 			}
 		}
 
 		return true;	
 	}
 	
 	//expr2 : ['-'] expr3;
 	private boolean expr2()
 	{
 		CLexer.TokenTypeEnum currToken;
 		double dblValue;
 		currToken = CLexer.TokenTypeEnum.T_EOL;
 
 		if ( m_Lexer.m_currToken.Type == CLexer.TokenTypeEnum.T_UMINUS )
 		{
			currToken = m_Lexer.m_currToken.Type;
 			m_Lexer.GetNextToken();
 		}
 
 		if ( !expr3() )
 			return false;
 
 		if ( currToken == CLexer.TokenTypeEnum.T_UMINUS )
 		{
 			dblValue = m_stack[m_top--];
 			dblValue *= -1;
 			m_stack[++m_top] = dblValue;
 		}
 
 		return true;	
 	}
 	
 	//expr3 : T_NUMBER
 	//		| '(' expr ')'
 	private boolean expr3()
 	{	
 		switch( m_Lexer.m_currToken.Type )
 		{
 		case T_NUMBER:
 			m_stack[++m_top] = m_Lexer.m_currToken.Value;
 			m_Lexer.GetNextToken();
 			break;
 		case T_OPAREN:
 			m_Lexer.GetNextToken();
 			if ( !expr() )
 				return false;
 			if ( !match(CLexer.TokenTypeEnum.T_CPAREN) )
 			{
 				//System.out.println("Errore: parentesi non bilanciate.");				
 				System.out.println("Error: unmatched parentheses.");								
 				return false;
 			}
 			break;
 		default:
 			//System.out.println("Errore: atteso numero, meno unario o parentesi aperta.");							
 			//System.out.print("Trovato invece ");										
 			System.out.println("Error: expected number, unary minus or opening parenthesis.");							
 			System.out.print("Found instead ");													
 			System.out.println(m_Lexer.m_currToken.str);
 			return false;
 		}
 
 		return true;	
 	}
 
 	private boolean match(CLexer.TokenTypeEnum ExpectedToken)
 	{
 		if ( m_Lexer.m_currToken.Type == ExpectedToken )
 		{
 			m_Lexer.GetNextToken();
 			return true;
 		}
 
 		return false;
 	}
 	
 	private CLexer m_Lexer;
 	private String m_strExpr;	
 	private int m_top;              
 	private double[] m_stack;
 	private double m_value;
 }
 
