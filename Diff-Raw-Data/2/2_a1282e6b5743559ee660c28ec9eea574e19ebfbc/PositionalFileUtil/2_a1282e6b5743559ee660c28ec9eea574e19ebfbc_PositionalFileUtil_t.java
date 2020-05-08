 package br.com.bluesoft.commons.lang;
 
 import java.text.DecimalFormat;
 import java.text.DecimalFormatSymbols;
 
 import org.apache.commons.lang.StringUtils;
 
 import br.com.bluesoft.commons.lang.StringUtil.Pad;
 
 /**
  * Funes eis para formatao de Arquivos Posicionais.
  * @author AndreFaria
  */
 public class PositionalFileUtil {
 
 	/**
 	 * Mtodo auxiliar para formatao de Strings.
 	 * @param string string fonte
 	 * @param padDirection Left or Right
 	 * @param padCharacter Caractere de preenchimento
 	 * @param size tamanho mximo da String
 	 * @return String
 	 */
 	public static String format(final Object text, final Pad padDirection, final char padCharacter, final int size) {
 		final String string = String.valueOf(text);
 		// Se nulo retorna uma string preenchida com 'padCaracter' do tamanho 'size'
 		if (string == null || string.equals("null")) {
 			return StringUtils.leftPad("", size, padCharacter);
 		}
 
 		// Se for maior que o tamanho permitido corta e retorna
 		if (string.length() > size) {
 			return StringUtil.cutString(string, size);
 		}
 
 		if (padDirection.equals(Pad.LEFT)) {
 			return StringUtils.leftPad(string, size, padCharacter);
 		} else if (padDirection.equals(Pad.RIGHT)) {
 			return StringUtils.rightPad(string, size, padCharacter);
 		}
 
 		return StringUtils.leftPad("", size, padCharacter);
 
 	}
 
 	/**
 	 * Formata uma String, cortando se utrapassar o tamanho e mximo e preendo com espaos em branco ( direita) se no atingir o tamanho mximo.
 	 * Tambm converte os caracteres para maiusculos. Retira acentuaao da palavra.  a formatao padro utilizada para campos Alpha-Numricos.
 	 * @param string
 	 * @param size
 	 * @return String
 	 */
 	public static String formatAlpha(final Object string, final int size) {
 		return format(string, Pad.RIGHT, ' ', size);
 	}
 
 	/**
 	 * Formata uma String, cortando se utrapassar o tamanho e mximo e preendo com zeros ( esquerda) se no atingir o tamanho mximo.  a formatao
 	 * padro utilizada para campos Numricos.
 	 * @param string
 	 * @param size
 	 * @return String
 	 */
 	public static String formatNumber(final Object string, final int size) {
 		return format(String.valueOf(string), Pad.LEFT, '0', size);
 	}
 
 	/**
 	 * Formata um nmero decimal, cortando se utrapassar o tamanho e mximo e preendo com zeros ( esquerda) se no atingir o tamanho mximo.  a
 	 * formatao padro utilizada para campos Numricos.
 	 * @return String
 	 */
 	public static String formatNumber(Object number, final int digits, final int size) {
 		if (number == null) {
 			return StringUtil.repeat("0", size);
 		}
 		final DecimalFormat decimalFormat = new DecimalFormat();
 		decimalFormat.setMinimumFractionDigits(digits);
 		decimalFormat.setMaximumFractionDigits(digits);
 		decimalFormat.setDecimalSeparatorAlwaysShown(false);
 		number = decimalFormat.format(number).replace(".", "").replace(",", "");
 		return format(String.valueOf(number), Pad.LEFT, '0', size);
 	}
 
 	/**
 	 * Formata um nmero decimal, cortando se utrapassar o tamanho e mximo e preendo com zeros ( esquerda) se no atingir o tamanho mximo.  a
 	 * formatao padro utilizada para campos Numricos.
 	 * @return String
 	 */
 	public static String formatNumberWithDecimalSeparator(Object number, final int digits, final int size, final char decimalSeparator) {
		return formatNumberWithDecimalSeparator(number, digits, size, decimalSeparator, '0');
 	}
 
 	/**
 	 * Formata um nmero decimal, cortando se utrapassar o tamanho e mximo e preendo com zeros ( esquerda) se no atingir o tamanho mximo.  a
 	 * formatao padro utilizada para campos Numricos.
 	 * @return String
 	 */
 	public static String formatNumberWithDecimalSeparator(Object number, final int digits, final int size, final char decimalSeparator, final char padding) {
 		if (number == null) {
 			return StringUtil.repeat("0", size);
 		}
 
 		final DecimalFormat decimalFormat = new DecimalFormat();
 		decimalFormat.setMinimumFractionDigits(digits);
 		decimalFormat.setMaximumFractionDigits(digits);
 		decimalFormat.setDecimalSeparatorAlwaysShown(true);
 		final DecimalFormatSymbols newSymbols = new DecimalFormatSymbols();
 		newSymbols.setDecimalSeparator(decimalSeparator);
 		decimalFormat.setDecimalFormatSymbols(newSymbols);
 		decimalFormat.setGroupingUsed(false);
 		number = decimalFormat.format(number);
 		return format(String.valueOf(number), Pad.LEFT, padding, size);
 	}
 
 }
