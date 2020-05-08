 package enzet.moire.core;
 
 import java.util.Arrays;
 import java.util.List;
 
 import enzet.moire.util.Util;
 
 /**
  * Language preprocessor
  *
  * @see LanguagePreprocessor#preprocess(String, List, String)
  *
  * @author Sergey Vartanov (me@enzet.ru)
  */
 public class LanguagePreprocessor
 {
 	private static char BEGIN = '[';
 	private static char END = ']';
 	private static char SCREEN = '\\';
	private static String CODE_BEGIN = "\\code";
 
 	/**
 	 * Preprocessor detects character sequences with syntax
 	 * <code>BEGIN language_name non-letter_symbol text END</code>,
 	 * removes sequences where <code>language_name</code> is not included in
 	 * <code>languages</code> and is not equals <code>format</code> and remains
 	 * <code>text</code> otherwise. <code>non-letter_symbol</code> will be
 	 * deleted.
 	 *
 	 * @param text input text
 	 * @param languages list of language names
 	 * @param format current format name (e. g. HTML)
 	 * @return preprocessed text
 	 */
 	public String preprocess(String text, List<String> languages, String format)
 	{
 		System.out.print("Language preprocessing... ");
 
 		char[] newText = new char[text.length()];
 		int k = 0;
		boolean inCodeSection = false;
		int tagLevel = 0;
 
 		for (int i = 0; i < text.length(); i++)
 		{
 			char cp = i > 0 ? text.charAt(i - 1) : ' ';
 			char c = text.charAt(i);
 
			if (text.substring(i).startsWith(CODE_BEGIN))
			{
				inCodeSection = true;
				tagLevel = 0;
			}
			if (c == '{' && cp != SCREEN)
			{
				tagLevel++;
			}
			if (c == '}' && cp != SCREEN)
			{
				tagLevel--;

				if (tagLevel == 0 && inCodeSection)
				{
					inCodeSection = false;
				}
			}
			if (c == BEGIN && cp != SCREEN && !inCodeSection)
 			{
 				String begin = text.substring(i + 1);
 
 				if (begin.startsWith(format) &&
 					!Util.isLetter(text.charAt(i + 1 + format.length())))
 				{
 					i += format.length() + 1;
 					continue;
 				}
 				boolean matched = false;
 
 				if (languages != null)
 				{
 					for (String language : languages)
 					{
 						if (begin.startsWith(language) &&
 							!Util.isLetter(text.charAt(i + 1 + language.length())))
 						{
 							i += language.length() + 1;
 							matched = true;
 							break;
 						}
 					}
 				}
 				if (!matched)
 				{
 					while (!(text.charAt(i) == END && text.charAt(i - 1) != SCREEN))
 					i++;
 				}
 				continue;
 			}
			if (c == END && cp != SCREEN && !inCodeSection) continue;
 
 			newText[k++] = c;
 		}
 		char[] newNewText;
 		newNewText = Arrays.copyOf(newText, k);
 
 		System.out.println("done.");
 
 		return new String(newNewText);
 	}
 }
