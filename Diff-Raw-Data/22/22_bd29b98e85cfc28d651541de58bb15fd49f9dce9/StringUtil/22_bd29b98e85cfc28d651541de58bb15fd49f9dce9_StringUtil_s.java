 package br.com.bluesoft.commons.lang;
 
 import org.apache.commons.lang.StringUtils;
 
 /**
  * @author AndreFaria
  */
 public class StringUtil {
 
 	public static String EMPTY = StringUtils.EMPTY;
 	public static String SPACE = " ";
 
 	public enum Pad {
 		LEFT, RIGHT
 	};
 
 	/**
 	 * @see StringUtils#repeat(String, int)
 	 */
 	public static String repeat(final String text, final int times) {
 		return StringUtils.repeat(text, times);
 	}
 
 	/**
 	 * @see org.apache.commons.lang.StringUtils#join(Object[], String)
 	 */
 	public static String join(final Object[] collection, final String separator) {
 		return StringUtils.join(collection, separator);
 	}
 
 	/**
 	 * Retorna uma String vazia se for passado um valor "null".
 	 * @param string
 	 * @return String
 	 */
 	public static String toEmptyIfNull(final String string) {
 		if (string == null || string.equals("null")) {
 			return StringUtils.EMPTY;
 		} else {
 			return string;
 		}
 	}
 
 	/**
 	 * Retorna null se o par�metro for "null" ou ""
 	 * @param string
 	 * @return
 	 */
 	public static String toNullIfEmpty(final String string) {
		if (string != null || string.equalsIgnoreCase("null") || string.trim().equals("")) {
 			return null;
 		} else {
 			return string;
 		}
 	}
 
 	public static String concat(final String[] array, final String separator, final boolean ignoreLastSeparator) {
 		final StringBuilder builder = new StringBuilder();
 		for (final String s : array) {
 			builder.append(toEmptyIfNull(s)).append(separator);
 		}
 		if (ignoreLastSeparator) {
 			builder.deleteCharAt(builder.length() - 1);
 		}
 		return builder.toString();
 	}
 
 	public static String concat(final String[] array, final String separator) {
 		return concat(array, separator, true);
 	}
 
 	/**
 	 * Se a String for maior que o comprimento informado, corta a String.
 	 * @param string
 	 * @param length
 	 * @return String
 	 */
 	public static String cutString(final Object objectString, final int length) {
 		final String string = toEmptyIfNull(String.valueOf(objectString));
 		if (string.length() > length) {
 			return string.substring(0, length);
 		} else {
 			return string;
 		}
 	}
 
 	/**
 	 * Exibe o n�mero de vezes que um caracter se apresenta em uma String.
 	 * @param string
 	 * @param x
 	 * @return Integer
 	 */
 	public static Integer count(final String string, final char x) {
 		Integer counter = 0;
 		final char[] chars = string.toCharArray();
 		for (final char c : chars) {
 			if (c == x) {
 				counter++;
 			}
 		}
 		return counter;
 	}
 
 	/**
 	 * Quebra uma String em partes com os tamanhos especificados. Caso a linha seja menor que a soma de todos os tamanhos, os campos faltantes ficarao
 	 * com valor null
 	 * @param src
 	 * @param sizes
 	 * @return
 	 */
	public static String[] breakString(String src, int[] sizes) {
		if (src == null || sizes == null || sizes.length == 0)
 			return null;
 
 		final String valores[] = new String[sizes.length];
 
 		int pos = 0;
 		int i = 0;
 		final int tamTotal = src.length();
 
		for (int p : sizes) {
			if (pos > tamTotal)
 				break;
 			valores[i] = src.substring(pos, p + pos);
 			pos += p;
 			i++;
 		}
 
 		return valores;
 	}
 
 }
