 package features.mouse;
 
 import java.math.BigDecimal;
 import java.text.DecimalFormat;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 
 
 public class Calculator implements features.Calculator {
 
 	@Override
 	public String execute(String formula) {
 		Integer	answer = 0 ;
 		int	idx = 0 ;
 
 		// トークン分割
 		String	valueA = "" ;
 		String	valueB = "" ;
 		String	expression = "" ;
 
 		// １つめの値
 		for( idx=0; idx < formula.length(); ++idx)
 		{
 			String letter = formula.substring( idx, idx+1);
 
 			// 数字なら積む、それ以外は終了
 			String regex = "[0-9]";
 			Pattern p = Pattern.compile(regex);
 			Matcher m = p.matcher(letter);
 			if(m.find()) {
 				valueA = valueA + letter ;
 			}else {
 				break ;
 			}
 		}
 		if(valueA.isEmpty()) {
 			throw new java.lang.IllegalArgumentException();
 		}
 		if(Integer.parseInt(valueA) == 0) {
 			throw new java.lang.IllegalArgumentException("左辺が０");
 		}
 
 		// 演算子
 		{
 			String letter = formula.substring( idx, idx+1);
 
 			// 数字なら積む、それ以外は終了
 			String regex = "[-\\+\\*\\/]";
 			Pattern p = Pattern.compile(regex);
 			Matcher m = p.matcher(letter);
 			if(m.find()) {
 				expression = letter ;
 			}
 			++idx ;
 		}
 		if(expression.isEmpty()) {
 			throw new java.lang.IllegalArgumentException();
 		}
 
 		// ２つめの値
 		for( ; idx < formula.length(); ++idx)
 		{
 			String letter = formula.substring( idx, idx+1);
 
 			// 数字なら積む、それ以外は終了
 			String regex = "[0-9]";
 			Pattern p = Pattern.compile(regex);
 			Matcher m = p.matcher(letter);
 			if(m.find()) {
 				valueB = valueB + letter ;
 			}else {
 				break ;
 			}
 		}
 		if(valueB.isEmpty()) {
 			throw new java.lang.IllegalArgumentException();
 		}
 		if(Integer.parseInt(valueB) == 0) {
 			throw new java.lang.IllegalArgumentException("右辺が０");
 		}
 
 		// まだ文字列に残りがある？
 		if( idx < formula.length()) {
 			throw new java.lang.IllegalArgumentException();
 		}
 
 		// まず割り算
 		if(expression.equals("/")) {
 			double	a = Double.parseDouble(valueA) ;
 			double	b = Double.parseDouble(valueB) ;
 			BigDecimal bi = new BigDecimal(String.valueOf(a / b));
 
			double k3= bi.setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue();
			DecimalFormat exFormat1 = new DecimalFormat("#.###");
 			return exFormat1.format(k3) ;
 		}
 
 
 
 		if(expression.equals("+")) {
 			answer = (Integer.parseInt(valueA) + Integer.parseInt(valueB));
 		}else
 		if(expression.equals("-")) {
 			answer = (Integer.parseInt(valueA) - Integer.parseInt(valueB));
 		}else
 		if(expression.equals("*")) {
 			answer = (Integer.parseInt(valueA) * Integer.parseInt(valueB));
 		}
 		return answer.toString();
 	}
 
 }
