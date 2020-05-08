 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class Spider {
 	static final Pattern HREF_REGEX = Pattern.compile(
 			"href=['\"]?([^'\" <>]*)['\"]?",
 			Pattern.CASE_INSENSITIVE);
 
 	/**
 	 * Obtém todos os links em uma página HTML, passada como argumento através
 	 * de um {@link InputStream}.
 	 *
 	 * @param in Página HTML
 	 * @return Lista de links encontrados
 	 * @throws IOException se ocorrer um erro de E/S
 	 */
 	private static List<String> findLinks(final InputStream in) throws IOException {
 		final BufferedReader reader = new BufferedReader(new InputStreamReader(in));
 		final List<String> foundLinks = new ArrayList<>();
 
 		String line;
 		while ((line=reader.readLine()) != null) {
 			final Matcher matcher = HREF_REGEX.matcher(line);
			if (matcher.find()){
 				foundLinks.add(matcher.group(1));
 			}
 		}
 
 		return foundLinks;
 	}
 
 }
