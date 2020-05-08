 package lorian.graph.function;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class Term {
 	
 	private boolean parsed = false;
 	
 	private int index = 0;
 	private char argumentChar = 'x';
 	List<Factor> factors;
 	
 	public Term()
 	{
 		factors = new ArrayList<Factor>();
 	}
 	public Term(char argumentChar)
 	{
 		this.argumentChar = argumentChar;
 		factors = new ArrayList<Factor>();
 	}
 	
 	
 	
 	private String GetEverythingBetweenParentheses(String s)
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
 					return result;
 				}
 			}
 			result += ch;
 			index++;
 		}
 		return result;
 	}
 	
 	private String exponentTimesMinusOne(String s)
 	{
 		/*
 		if(!Util.StringContains(s, '('))
 		{
 			String base = s.substring(0, s.indexOf('^'));
 			String exponent = s.substring(s.indexOf('^')+1);
 			String result = String.format("%s^(-1(%s))", base, exponent);
 			return result;
 		}
 		return "a";
 		*/
 		return String.format("(%s)^-1", s);
 	}
 	private List<String> SplitIntoFactors(String s)
 	{
 		List<String> factors = new ArrayList<String>();
 		char ch;
 		String newfactor = "";
 		boolean ignoreminplus = true;
 		boolean inexponent = false;
 		boolean division = false;
 		while(index < s.length())
 		{
 			ch = s.charAt(index);
 
 				if(!((ch >= '0' && ch <= '9') || ch == '.'))
 				{
 					if(ch=='*' || ch == '/')
 					{
 						if(division) newfactor = exponentTimesMinusOne(newfactor);
 						if(newfactor.length() > 0) factors.add(newfactor);
 						newfactor = "";
 						ignoreminplus = true;
 						if(ch == '/') division = true;
 						else division = false;
 					}
 					
 					else if(ch==argumentChar)
 					{
 						if(inexponent)
 						{
 							newfactor += ch;
 						}
 						else if(newfactor.length() > 0)
 						{
 							if(!ignoreminplus)
 							{
 								if(division) newfactor = exponentTimesMinusOne(newfactor);
 								if(newfactor.length() > 0) factors.add(newfactor);
 								newfactor = "";
 								division = false;
 								ignoreminplus = true;
 								inexponent = false;
 								continue;
 							}
 							else
 							{
 								boolean neg = false;
 								int i=0;
 								String tmp = "";
 								while(i < newfactor.length())
 								{
 									if(newfactor.charAt(i) == '-') neg = !neg;
 									i++;
 								}
 								if(neg) tmp += "-";
 								tmp += "1";
 								if(division) tmp = exponentTimesMinusOne(tmp);
 								factors.add(tmp);
 								newfactor = "";
 								division = false;
 								ignoreminplus = true;
 								continue;
 							}
 						}
 						
 						else
 						{
 							newfactor += ch;
 							if(index+1 < s.length())
 							{
 								if(s.charAt(index+1) != '^')
 								{
 									if(division) newfactor = exponentTimesMinusOne(newfactor);
 									if(newfactor.length() > 0) factors.add(newfactor);
 									division = false;
 									newfactor = "";
 									ignoreminplus = true;
 								}
 								
 							}
 							
 							
 							
 						}
 					}
 					
 					
 					else if(ch=='+' || ch=='-')
 					{
 						if(!ignoreminplus)
 						{
 							if(division) newfactor = exponentTimesMinusOne(newfactor);
 							if(newfactor.length() > 0) factors.add(newfactor);
 							division = false;
 							newfactor = "";
 							ignoreminplus = true;
 							inexponent = false;
 							continue;
 						}
 						else newfactor += ch;
 					}
 					else if(ch == '(')
 					{
						if(!Util.StringContains(newfactor, Util.LowercaseAlphabethWithout(argumentChar)))
 						{
 							
 							if(newfactor.length() > 0) 
 							{
 								if(division) newfactor = exponentTimesMinusOne(newfactor);
 								factors.add(newfactor);
 								division = false;
 								newfactor = "";
 							}
 						}
 						newfactor += GetEverythingBetweenParentheses(s);
 						if(index+1 < s.length())
 						{
 							if(s.charAt(index+1) == '^')
 							{
 								index++;
 								continue;
 							}
 						}
 						if(division) newfactor = exponentTimesMinusOne(newfactor);
 						if(newfactor.length() > 0) factors.add(newfactor);
 						division = false;
 						newfactor = "";
 						ignoreminplus = true;
 						inexponent = false;
 						
 					}
 					else if(ch == '^')
 					{
 						ignoreminplus = true;
 						inexponent = true;
 						newfactor += ch;
 						index++;
 						continue;
 					}
 
 					else if(Util.StringContains("" + ch, Util.LowercaseAlphabethWithout(argumentChar) + "()+=^"))
 					{
 						if(division) newfactor = exponentTimesMinusOne(newfactor);
 						if(newfactor.length() > 0) factors.add(newfactor);
 						int tmpindex = s.indexOf('(', index);
 						newfactor = s.substring(index, tmpindex);
 						index = tmpindex;
 						newfactor += GetEverythingBetweenParentheses(s);
 						if(index+1 < s.length())
 						{
 							if(s.charAt(index+1) == '^')
 							{
 								index++;
 								continue;
 							}
 						}
 						if(division) newfactor = exponentTimesMinusOne(newfactor);
 						if(newfactor.length() > 0) factors.add(newfactor);
 						division = false;
 						newfactor = "";
 						ignoreminplus = true;
 						inexponent = false;
 						
 					}
 					
 					else
 						newfactor += ch;
 				}
 				else
 				{
 					newfactor += ch;
 				}
 				if(ignoreminplus && newfactor.length() > 0 && !(ch == '+' || ch == '-')) ignoreminplus = false;
 			
 			index++;
 		}
 		if(division) newfactor = exponentTimesMinusOne(newfactor);
 		if(newfactor.length() > 0) factors.add(newfactor);
 		return factors;
 	}
 	
 	public boolean Parse(String s)
 	{
 		s = Util.removeWhiteSpace(s).toLowerCase();
 		boolean result = true;
 		List<String> factorstrs = SplitIntoFactors(s);
 		//System.out.println(factorstrs);
 		for(String factorstr: factorstrs)
 		{
 			Factor factor = new Factor(this.argumentChar);
 			if(!factor.Parse(factorstr)) { 
 				result = false;
 				factors.add(null);
 			}
 			else
 				factors.add(factor);
 		}
 		parsed = result;
 		return result;
 	}
 	
 
 	public double Calc(double arg)
 	{
 		if(!parsed) return 0;
 		double product = 1;
 		for(Factor fac: factors)
 		{
 			product *= fac.Calc(arg);
 		}
 		return product;
 		
 	}
 }
 
 /*
  *
   private boolean coefficientnegative = false;
 	private boolean exponentnegative = false;
 	private boolean coefficienthasfloatpart = false;
 	private boolean exponenthasfloatpart = false;
 	private boolean isconstant = false;
 
 	double coefficient = 0;
 	double exponent = 0;
 	
  	private double OldCalc(double arg)
 	{
 		if(!parsed) return 0;
 		return (coefficient * Math.pow(arg, exponent));
 	}
 	private boolean OldParse(String s)
 	{
 		s = Util.removeWhiteSpace(s).toLowerCase();
 		coefficientnegative = false;
 		exponentnegative = false;
 		coefficienthasfloatpart = false;
 		exponenthasfloatpart = false;
 		isconstant = false;
 		coefficient = 0;
 		exponent = 0;
 		
 		
 		if(!Util.StringContains(s, argumentChar))
 		{
 			isconstant = true;
 			exponent = 0;
 			return ParseConstant(s);
 		}
 		
 		return ParseAxN(s);
 	}
 	
 	private boolean ParseAxN(String s)
 	{
 		boolean skipNext = false;
 
 		if(s.charAt(index)=='-')
 		{
 			coefficientnegative = true;
 			index++;
 		}
 		else if(s.charAt(index)=='+')
 		{
 			coefficientnegative = false;
 			index++;
 		}
 		
 		if(!(s.charAt(index) >= '0' && s.charAt(index) <= '9'))
 		{
 			if(s.charAt(index) == argumentChar)
 			{
 				skipNext = true;
 				coefficient = 1;
 				index++;
 				if(index < s.length())
 				{
 					if(s.charAt(index)!='^')
 					{
 						//System.err.printf("Error: Unknown char '%c' in term '%s'\n", s.charAt(index), s);
 						return false;
 					}
 					index++;
 				}
 					
 			}
 			else
 			{
 				//System.err.printf("Error: Unknown char '%c' in term '%s'\n", s.charAt(index), s);
 				return false;
 			}
 		}
 		
 		
 	    double i=1;  
 	    if(index == s.length()) skipNext = true;
 		while(true)
 		{
 			if(skipNext) 
 			{
 				if(index < s.length()) skipNext = false;
 				else exponent = 1;
 				break;
 			}
 			
 			if(!(s.charAt(index) >= '0' && s.charAt(index) <= '9'))
 			{
 				if(s.charAt(index)==argumentChar)
 				{
 
 					index++;
 					
 					if(s.length()==index)
 					{						
 						skipNext = true;
 						exponent = 1;
 						break;
 						
 					}
 					
 					if(s.charAt(index)=='^')
 					{
 						index++;
 						break;
 					}
 					else
 					{
 						//System.err.printf("Error: Unknown char '%c' in term '%s'\n", s.charAt(index), s);
 						return false;
 					}
 				}
 				else if(s.charAt(index)==',')
 				{
 					index++;
 				}
 				else if(s.charAt(index)=='.')
 				{
 					if(this.coefficienthasfloatpart)
 					{
 						//System.err.printf("Error: Unknown char '%c' in term '%s'\n", s.charAt(index), s);
 						return false;
 					}
 					this.coefficienthasfloatpart = true;
 					index++;
 				}
 				else 
 				{
 					//System.err.printf("Error: Unknown char '%c' in term '%s'\n", s.charAt(index), s);
 					return false;
 				}
 			}
 			else
 			{
 				if(coefficienthasfloatpart)
 				{
 					coefficient += ((s.charAt(index) - '0')) / Math.pow(10, i++);
 				}
 				else
 				{
 					coefficient *= 10;
 					coefficient += ((s.charAt(index) - '0'));
 				}
 				index++;
 			}
 
 		}
 	
 		if(index < s.length())
 		{
 			if(s.charAt(index)=='-')
 			{
 				exponentnegative = true;
 				index++;
 			}
 			else if(s.charAt(index)=='+')
 			{
 				exponentnegative = false;
 				index++;
 			}
 		}
 		i=1;
 		while(index < s.length())
 		{
 			if(skipNext) 
 			{
 				skipNext = false;
 				break;
 			}
 			
 			if(!(s.charAt(index) >= '0' && s.charAt(index) <= '9'))
 			{
 				if(s.charAt(index)==',')
 				{
 					index++;
 				}
 				else if(s.charAt(index)=='.')
 				{
 					if(this.exponenthasfloatpart)
 					{
 						//System.err.printf("Error: Unknown char '%c' in term '%s'\n", s.charAt(index), s);
 						return false;
 					}
 					this.exponenthasfloatpart = true;
 					index++;
 				}
 				else 
 				{
 					//System.err.printf("Error: Unknown char '%c' in term '%s'\n", s.charAt(index), s);
 					return false;
 				}
 			}
 			else
 			{
 				if(exponenthasfloatpart)
 				{
 					exponent += ((s.charAt(index) - '0')) / Math.pow(10, i++);
 				}
 				else
 				{
 					exponent *= 10;
 					exponent += ((s.charAt(index) - '0'));
 				}
 				index++;
 			}
 			
 		}
 		if(coefficientnegative) coefficient *= -1;
 		if(exponentnegative) exponent *= -1;
 		
 	
 	
 		parsed = true;
 		return true;
 	}
 
 	private boolean ParseTerm(String s)
 	{
 		int constantstart=-1, functionend = -1;
 		int state = 0; //0 = constant, 1 = x, 2 = function
 		int state2 = 0; //0 = normal, 1 = exponent
 		double constant;
 		String tmp;
 		boolean neg=false;
 		char ch;
 		while(index<s.length())
 		{
 			ch = s.charAt(index);
 			if(state==0)
 			{
 				if((ch >= '0' && ch <= '9') || ch == '.')
 				{
 					if(constantstart == -1)
 					{
 						constantstart = index;
 					}
 				}
 				else if(ch == '-')
 				{
 					neg = !neg;
 				}
 				else if(ch == '+')
 				{
 					
 				}
 				else if(ch == '^')
 				{
 					
 				}
 				else if(ch == '*')
 				{
 					tmp = s.substring(constantstart, index);
 					constant = Double.parseDouble(tmp);
 					if(neg) constant *= -1;
 					constantstart = -1;
 					neg = false;
 					System.out.printf("Constant product: %f\n", constant);
 					coefficient *= constant;
 					
 				}
 				else if(ch == argumentChar)
 				{
 					state = 1;
 				}
 				else //function
 				{
 					tmp = s.substring(index);
 					functionend = tmp.indexOf('(');
 					tmp = s.substring(index, functionend+index);
 					System.out.printf("Function: %s\n", tmp);
 					
 				}
 			}
 			else if(state == 1)
 			{
 				if(ch == '^')
 				{
 					state2 = 1;
 				}
 				else if(ch == '*')
 				{
 					
 				}
 				else if(ch == argumentChar)
 				{
 					
 				}
 				else //function
 				{
 					tmp = s.substring(index);
 					functionend = tmp.indexOf('(');
 					tmp = s.substring(index, functionend+index);
 					System.out.printf("Function: %s\n", tmp);
 				}
 			
 			}
 			index++;
 		}
 		return true;
 	}
 
 	
 	private boolean ParseConstant(String s)
 	{
 		int index = 0;
 		if(s.charAt(index)=='-')
 		{
 			coefficientnegative = true;
 			index++;
 		}
 		else if(s.charAt(index)=='+')
 		{
 			coefficientnegative = false;
 			index++;
 		}
 		int i=1;
 		for( ;index<s.length();index++)
 		{
 			if(!(s.charAt(index) >= '0' && s.charAt(index) <= '9'))
 			{
 				if(s.charAt(index)==',')
 				{
 					continue;
 				}
 				else if(s.charAt(index)=='.')
 				{
 					this.coefficienthasfloatpart = true;
 					continue;
 				}
 				else
 				{
 					//System.err.printf("Error: Unknown char '%c' in term '%s'\n", s.charAt(index), s);
 					return false;
 				}
 				
 			}
 			else
 			{
 				if(coefficienthasfloatpart)
 				{
 					coefficient += ((s.charAt(index) - '0')) / Math.pow(10, i++);
 				}
 				else
 				{
 					coefficient *= 10;
 					coefficient += ((s.charAt(index) - '0'));
 				}
 			}
 		}
 		if(coefficientnegative) coefficient *= -1;
 		
 		parsed = true;
 		return true;
 	}
 	*/
