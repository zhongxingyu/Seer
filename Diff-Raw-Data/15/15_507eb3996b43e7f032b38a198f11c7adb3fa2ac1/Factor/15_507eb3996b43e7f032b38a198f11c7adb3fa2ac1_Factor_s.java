 package lorian.graph.function;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class Factor {
 	public enum Type
 	{
 		CONSTANT, ARGUMENT, PARENTHESES, SPECIAL, FUNCTION
 	}
 	
 	
 	private Type type;
 	private char argumentChar = 'x';
 	private double value = 0;
 	private boolean parsed = false;
 	private int index = 0;
 	
 	Function basefunc, exponentfunc;
 	Factor specialfac;
 	String functionname;
 	List<String> functionargs;
 	
 	public Factor()
 	{
 		basefunc = new Function();
 		exponentfunc = new Function();
 	}
 	public Factor(char argumentChar)
 	{
 		this.argumentChar = argumentChar;
 		basefunc = new Function();
 		exponentfunc = new Function();
 	}
 	public Factor(char argumentChar, String s)
 	{
 		this.argumentChar = argumentChar;
 		basefunc = new Function();
 		exponentfunc = new Function();
 		Parse(s);
 		
 	}
 	public Factor(String s)
 	{
 		basefunc = new Function();
 		exponentfunc = new Function();
 		Parse(s);
 	}
 	
 	private double ParseConstantBetweenParentheses(String s)
 	{
 		Function tmpfunc = new Function();
 		String forparse = "";
 		boolean neg = false;
 		while(s.charAt(index) == '+' || s.charAt(index) == '-')
 		{
 			if(s.charAt(index) == '-') neg = !neg;
 			index++;
 		}
 		if(neg) forparse += '-';
 		forparse += s.substring(index+1, s.length()-2);
 		if(!tmpfunc.Parse(forparse)) return 0;
 		double tmpvalue = tmpfunc.Calc(0);
 		return tmpvalue;
 	}
 	private int getFirstPower(String s)
 	{
 		char ch;
 		int funcdepth = 0;
 		index++;
 		while(index < s.length())
 		{
 			ch = s.charAt(index);
 			if(ch == '(')
 			{
 				funcdepth++;
 			}
 			else if(ch == ')')
 			{
 				if(funcdepth > 0 ) funcdepth--;
 				else if(funcdepth==0)
 				{
 					//result += ch;
 					return -1;
 				}
 			}
 			else if(ch == '^' && funcdepth == 0)
 			{
 				return index;
 			}
 			index++;
 		}
 		return -1;
 	}
 	private List<String> SplitArgs(String s)
 	{
 		char ch;
 		int funcdepth = 0;
 		List<String> args = new ArrayList<String>();
 		int i = 0;
 		String arg = "";
 		while(i < s.length())
 		{
 			ch = s.charAt(i);
 			if(ch == '(')
 			{
 				funcdepth++;
 			}
 			else if(ch == ')')
 			{
 				if(funcdepth > 0 ) funcdepth--;
 				else if(funcdepth==0)
 				{
 					return null;
 				}
 			}
 			else if(ch == ',' && funcdepth == 0)
 			{
 				if(arg.length() > 0) args.add(arg);
 				else args.add(" ");
 				arg = "";
 				i++;
 				continue;
 			}
 			arg += ch;
 			i++;
 		}
 		if(arg.length() > 0) args.add(arg);
 		return args;
 	}
 	private boolean ParseConstant(String s)
 	{
 
 		if(!Util.StringContains(s, '^'))
 		{
 			if(!Util.StringContains(s, "0123456789"))
 			{
 				value = 1;
 				for(int i=0;i<s.length();i++)
 				{
 					if(s.charAt(i) == '-') value *= -1;
 					else if(s.charAt(i) != '+')
 						return false;
 				}
 				return true;
 			}
 			else if(Util.StringContains(s, '('))
 			{
 				value = ParseConstantBetweenParentheses(s);
 				return true;
 			}
 			else
 			{
 				String forparse = "";
 				boolean neg = false;
 				while(s.charAt(index) == '+' || s.charAt(index) == '-')
 				{
 					if(s.charAt(index) == '-') neg = !neg;
 					index++;
 				}
 				if(neg) forparse += '-';
 				forparse += s.substring(index);
 				value = Double.parseDouble(forparse);
 				return true;
 			}
 		}
 		
 		String forparse = "";
 		boolean neg = false;
 		while(s.charAt(index) == '+' || s.charAt(index) == '-')
 		{
 			if(s.charAt(index) == '-') neg = !neg;
 			index++;
 		}
 		if(neg) forparse += '-';
 		forparse += s.substring(index, s.indexOf('^')); 
 		value = Double.parseDouble(forparse);
 		
 		
 		String exponentstr = s.substring(s.indexOf('^')+1);
 		index = 0;
 		double exponent;
 		if(Util.StringContains(exponentstr, '('))
 		{
 			exponent = ParseConstantBetweenParentheses(exponentstr);
 		}
 		else
 		{
 			forparse = "";
 			neg = false;
 			
 			while(exponentstr.charAt(index) == '+' || exponentstr.charAt(index) == '-')
 			{
 				if(exponentstr.charAt(index) == '-') neg = !neg;
 				index++;
 			}
 			if(neg) forparse += '-';
 			forparse += exponentstr.substring(index);
 			exponent = Double.parseDouble(forparse);
 		}
 		value = Math.pow(value, exponent);
 		return true;
 	}
 	private String ParseParanthesesBase(String s)
 	{
 		char ch;
 		int funcdepth = 0;
 		String result = "(";
 		index++;
 		while(index < s.length())
 		{
 			ch = s.charAt(index);
 			if(ch == '(')
 			{
 				funcdepth++;
 			}
 			else if(ch == ')')
 			{
 				if(funcdepth > 0 ) funcdepth--;
 				else if(funcdepth==0)
 				{
 					result += ch;
 					break;
 				}
 			}
 			result += ch;
 			index++;
 		}
 		return result.substring(1, result.length()-1);
 	}
 	private String ParseParanthesesExponent(String s)
 	{
 		index += 2;
 		return s.substring(index);
 	}
 	private boolean ParseParentheses(String s)
 	{
 		String basestr = ParseParanthesesBase(s); 
 		if(index == s.length()-1)
 		{
 			exponentfunc.Parse("1");
 			return basefunc.Parse(basestr);
 		}
 		String exponentstr = ParseParanthesesExponent(s);
 		if(!basefunc.Parse(basestr) || !exponentfunc.Parse(exponentstr)) 
 		{
 			return false;
 		}
 		else return true;
 	}
 	private boolean ParseExponentX(String s)
 	{
 		int tmpindex = getFirstPower(s);//s.indexOf('^');
 		String basestr, exponentstr;
 		if(tmpindex == -1)
 		{
 			basestr = s;
 			exponentstr = "1";
 		}
 		else
 		{
 			basestr = s.substring(0, tmpindex);
 			exponentstr = s.substring(tmpindex + 1);
 		}
 
 		if(!basefunc.Parse(basestr) || !exponentfunc.Parse(exponentstr)) 
 		{
 			return false;
 		}
 		else return true;
 	}
 	private boolean ParseX(String s)
 	{
 		if(!Util.StringContains(s, '^'))
 		{
			exponentfunc.Parse("1");
 			return true;
 		} 
 		String exponentstr = s.substring(s.indexOf('^')+1);
 		return exponentfunc.Parse(exponentstr);
 		//return true;
 	}
 	private boolean ParseFunction(String s)
 	{
 		functionname = s.substring(0, s.indexOf('('));
 		if(!MathFunctions.functionExists(functionname)) return false;
 		String functionargsstr = s.substring(s.indexOf('(') + 1, s.length()-1);
 		functionargs = SplitArgs(functionargsstr);
 		return true;
 	}
 	
 	private boolean ParseOther(String s)
 	{
 		int ii = Util.StringIndexNotOf(s, "+-");
 		if(s.charAt(0) == '-' || s.charAt(0) == '+')
 		{
 			boolean neg = false;
 			for(int i = 0; i< ii; i++)
 			{
 				if(s.charAt(i) == '+' || s.charAt(i) == '-')
 				{
 					if(s.charAt(i) == '-') neg = !neg;
 				}
 				else return false;
 			}
 			if(neg) value = -1;
 			else value = 1;
 			type = Type.SPECIAL;
 			specialfac = new Factor(this.argumentChar);
 			
 			System.out.println("SPECIAL!!!!!!!!");
 			return specialfac.Parse(s.substring(ii));
 		
 			/*
 			type = Type.PARENTHESES;
 			return ParseParentheses(s.substring(ii));
 			*/
 		}
 		else
 		{
 			type = Type.PARENTHESES;
 			value = 1;
 			return ParseExponentX(s);
 		}
 	}
 	public boolean Parse(String s)
 	{
		
 		if(s.charAt(0) == argumentChar)
 		{
 			type = Type.ARGUMENT;
 			parsed = ParseX(s);
 			return parsed;				
 		}
 		else if(s.charAt(s.length() - 1) == ')')
 		{
 			if(s.charAt(0) == '(')
 			{
 				type = Type.PARENTHESES;
 				value = 1;
 				parsed = ParseParentheses(s);
 			}
 			else if(Util.StringContains(s, Util.LowercaseAlphabethWithout(argumentChar)))
 			{
 				type = Type.FUNCTION; 
 				parsed = ParseFunction(s);
 			}
 			else
 			{
 				System.out.println(s);
 				parsed = ParseOther(s);
 			}
 			return parsed;
 		}
 		else if(s.charAt(0) == '(')
 		{
 			type = Type.PARENTHESES;
 			value = 1;
 			parsed =  ParseParentheses(s);
 			return parsed;
 		}
		else if(!Util.StringContains(s, argumentChar))
 		{
 			type = Type.CONSTANT;
 			parsed = ParseConstant(s);
 			return parsed;
 		}
 		else
 		{
 			parsed = ParseOther(s);
 			return parsed;
 		}
 		
 	}
 	public double Calc(double arg)
 	{
 		if(!parsed )return Double.NaN;
 		
 		if(type == Type.CONSTANT)
 		{
 			return value;
 		}
 		else if(type == Type.ARGUMENT)
 		{
			return Math.pow(arg, exponentfunc.Calc(arg)); 
 		}
 		else if(type == Type.PARENTHESES)
 		{
 			return value * Math.pow(basefunc.Calc(arg), exponentfunc.Calc(arg));
 		}
 		else if(type == Type.SPECIAL)
 		{
 			return value * specialfac.Calc(arg);
 		}
 		else if(type == Type.FUNCTION)
 		{
 			return MathFunctions.Calculate(functionname, functionargs, arg);
 		}
 		else return 0;
 	}
 	
 	public Type getType()
 	{
 		return this.type;
 	}
 	public Function getBaseFunction()
 	{
 		return this.basefunc;
 	}
 	public Function getExponentFunction()
 	{
 		return this.exponentfunc;
 	}
 	public double getValue()
 	{
 		return this.value;
 	}
 	public Factor getSpecialFac()
 	{
 		return this.specialfac;
 	}
 	public String getFunctionName()
 	{
 		return this.functionname;
 	}
 	public List<String> getFunctionArgs()
 	{
 		return this.functionargs;
 	}
 }
 
