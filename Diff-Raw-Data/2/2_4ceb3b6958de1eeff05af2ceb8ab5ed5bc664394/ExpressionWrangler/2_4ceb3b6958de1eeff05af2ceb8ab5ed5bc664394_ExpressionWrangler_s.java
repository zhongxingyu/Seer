 package uk.org.smithfamily.utils.normaliser;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * Converts INI visibility expressions into something Java can compile
  *
  */
 public class ExpressionWrangler
 {
 	//Cheat translation table.  See usage in code below.
 	static Map<String,String> translations = new HashMap<String,String>();
 	static
 	{
 	    // User defined
 		translations.put("0", "false");
 		translations.put("RevLimCLTbased & 1", "(RevLimCLTbased & 1) != 0");
 		translations.put("RevLimOption & 4", "(RevLimOption & 4) != 0"); 
 		translations.put("RevLimOption & 1", "(RevLimOption & 1) != 0"); 
		translations.put("!(status3 & 8)", "(status3 & 8) != 0");
 		translations.put("status3 & 8", "(status3 & 8) != 0");
 		translations.put("secondtrigopts & 0x1", "(secondtrigopts & 0x1) != 0"); 
 		translations.put("(launchlimopt & 1) && (launch_opt_on >0)", "(launchlimopt & 1) !=0 && (launch_opt_on >0)"); 
 		translations.put("spk_config_trig2 & 0x2", "(spk_config_trig2 & 0x2) != 0"); 
 		translations.put("((dualfuel_opt_out || dualfuel_opt_mode) && staged_extended_opts_use_v3) || ((nCylinders > 4) && (dualfuel_opt_out || dualfuel_opt_mode) && hardware_fuel && !(sequential & 0x1))", "((dualfuel_opt_out !=0 || dualfuel_opt_mode !=0) && staged_extended_opts_use_v3 != 0) || ((nCylinders > 4) && (dualfuel_opt_out !=0 || dualfuel_opt_mode !=0) && hardware_fuel !=0 && !((sequential & 0x1)!=0))");	
 		translations.put("(staged_extended_opts_use_v3 && staged_first_param) || ((nCylinders > 4) && (staged_first_param) && (hardware_fuel) && !(sequential & 0x1))", "(staged_extended_opts_use_v3!=0 && staged_first_param!=0) || ((nCylinders > 4) && (staged_first_param!=0) && (hardware_fuel!=0) && !((sequential & 0x1)!=0))");
 		translations.put("(spk_config_trig2 & 0x2) && spk_mode0 == 4", "(spk_config_trig2 & 0x2)!=0 && spk_mode0 == 4");
 		
 		// Menus
 	    translations.put("AE_options & 0x1", "(AE_options & 0x1) != 0");
 	    translations.put("!(AE_options & 0x1)", "!((AE_options & 0x1) != 0)");
 	    translations.put("NoiseFilterOpts & 1", "(NoiseFilterOpts & 1) != 0");
 	    translations.put("(RevLimCLTbased & 1)", "(RevLimCLTbased & 1) != 0");
 	}
 
 	/**
 	 * Entry point.  Take an INI visibility expression and attempt to convert it to Java
 	 * @param e
 	 * @return
 	 */
 	public static String convertExpr(String e)
 	{
 		//Normalise space
 		e = e.trim();
 		e = e.replaceAll("  ", " ");
 		
 		//Some expressions are just too much hard work, so use a cheat translation table to do the conversion
 		if (translations.containsKey(e))
 		{
 			return translations.get(e);
 		}
 
 		StringBuffer b = new StringBuffer();
 
 		//Are we processing something that looks like a variable?
 		boolean inIdentifier = false;
 		//Are we processing something that looks like a numeric constant (dec or hex)
 		boolean inConstant = false;
 		//Do we need to possibly add a comparison with zero to convert int to bool?
 		boolean pendingEqualityTest = false;
 		//Have we seen ==, >, <, >=, <=
 		boolean seenComparator = false;
 		//Have we seen && or || on our travels along the expression
 		boolean seenLogical = false;
 		//Did the identifier start with ! so that we need to change the sense of the comparison
 		boolean negate = false;
 		//Is there some shonky usage of bitwise & when it should be logical &&
 		boolean needAnAmp = false;
 		
 		//Walk the string
 		for (int i = 0; i < e.length(); i++)
 		{
 			char c = e.charAt(i);
 
 			if (c == '!' && e.charAt(i + 1) != '=' && e.charAt(i + 1)!='(')
 			{
 				negate = true;
 				continue;
 			}
 			if (!seenLogical && (c == '&' || c == '|') && c == e.charAt(i + 1))
 			{
 				seenLogical = true;
 			}
 
 			//Now fixup some dodgy bitwise vs logical anding in the INIs
 			//where it is doing a bitwise & against a non constant.  All constants appear
 			//to start with a leading zero
 			if (!seenLogical && c == '&' && e.charAt(i + 1) != '&' && e.charAt(i + 2) != '0')
 			{
 				needAnAmp = true;
 				seenLogical = true;
 			}
 			if (c == '=' || c == '>' || c == '<' || (!seenLogical && (c == '&' || c == '|') && c != e.charAt(i + 1)))
 			{
 				seenComparator = true;
 			}
 			if (!inConstant && Character.isDigit(c))
 			{
 				//Start of a constant
 				inConstant = true;
 			}
 			if (inConstant && Character.isDigit(c) || c == 'x' || (c>='a' && c<='f'))
 			{
 				//We are still processing a potentially hex constant
 				inConstant = true;
 			}
 			if (inConstant && !(Character.isDigit(c) || c == 'x'|| (c>='a' && c<='f')))
 			{
 				//And now we've fallen off the end.
 				inConstant = false;
 			}
 			if (!inIdentifier && !inConstant && (Character.isLetterOrDigit(c) || c == '_'))
 			{
 				//Start of an identifier
 				inIdentifier = true;
 				pendingEqualityTest = true;
 				seenLogical = false;
 				seenComparator = false;
 			}
 			if (inIdentifier && !Character.isJavaIdentifierPart(c))
 			{
 				//Fallen off the end of the identifier
 				inIdentifier = false;
 			}
 			if (pendingEqualityTest && !inIdentifier && !seenComparator && (seenLogical || c == ')'))
 			{
 				//Convert the implicit to the explicit
 				if (negate)
 				{
 					b.append("==0 ");
 				}
 				else
 				{
 					b.append("!=0 ");
 				}
 				pendingEqualityTest = false;
 				negate = false;
 				if (needAnAmp)
 				{
 					b.append('&');
 				}
 				needAnAmp = false;
 			}
 			b.append(c);
 		}
 		if (pendingEqualityTest && !seenComparator)
 		{
 			//We've fallen off the end of the string, but it looks like we still need a comparison
 			if (negate)
 			{
 				b.append("==0 ");
 			}
 			else
 			{
 				b.append("!=0 ");
 			}
 		}
 		String result = b.toString();
 
 		return result;
 	}
 
 	/*
 	 * Test expressions
 	 */
 	static String[] exprs = { 
 		"!(als_in_pin && als_opt_pwmout)",
 		"v == 0x0e",
 		"RevLimCLTbased & 1",
 		" pwmIdle & (pwmidlewhen >1) ",
 		"ax &  (bx >1)","ay && !by",
 		"(a & 0x7) != 5", 
 		"staged_first_param && (staged_first_param  & 0x7) != 5",
 		"staged_first_param && staged_transition_on && ((staged_first_param & 0x7) != 5)", "feature3_3 ",
 		"injctl && extrainj", "(spk_mode0 > 3) && seq_inj",
 		"(spk_mode0 > 3) && (seq_inj == 3)", 
 		"(spk_mode0 > 3) && (seq_inj == 3) && (injusetable == 0) && (injdualvalue)", 
 		"f5_0_tsf && f5_0_tsf_opt == 3",
 		"testmode == 2 && testop_pwm && !extrainj" 
 	};
 	
 	/**
 	 * Run tests from the command line, output before and after
 	 * @param args
 	 */
 	public static void main(String[] args)
 	{
 
 		for (String e : exprs)
 		{
 			String result = convertExpr(e);
 			System.out.println(String.format("%s  :  %s", e, result));
 		}
 	}
 
 }
