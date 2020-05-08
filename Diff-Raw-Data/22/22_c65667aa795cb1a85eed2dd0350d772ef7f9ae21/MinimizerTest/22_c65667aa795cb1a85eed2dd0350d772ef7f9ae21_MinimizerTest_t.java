 package net.ayld.facade.api;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 import java.util.Enumeration;
 import java.util.jar.JarEntry;
 import java.util.jar.JarFile;
 
 import junit.framework.Assert;
 import net.ayld.facade.model.ClassName;
 import net.ayld.facade.util.Settings;
 import net.ayld.facade.util.Tokenizer;
 
 import org.junit.Test;
 
import com.google.common.base.Joiner;
 import com.google.common.io.Resources;
 
 public class MinimizerTest {
 	
 	@Test
 	public void minimize() throws IOException {
 		final JarFile outJar = Minimizer
 				.sources(toPath(Resources.getResource("test-classes/test-src-dir")))
 				.libs(toPath(Resources.getResource("test-classes/test-lib-dir")))
 				.getJar();
 		
 		Assert.assertTrue(outJar != null);
 		
 		final String outJarName = Tokenizer.delimiter(File.separator).tokenize(outJar.getName()).lastToken();
 		
 		Assert.assertTrue(outJarName.equals(Settings.DEFAULT_FACADE_JAR_NAME.getValue()));
 	}
 	
 	@Test
 	public void mandatoryInclude() throws IOException {
 		final JarFile mandatoryJar = new JarFile(new File(toPath(Resources.getResource("test-classes/test-lib-dir/commons-lang3-3.1.jar"))));
 		
 		final JarFile outJar = Minimizer
 				
 			.sources(toPath(Resources.getResource("test-classes/test-src-dir")))
 			.libs(toPath(Resources.getResource("test-classes/test-lib-dir")))
 			
 			.forceInclude(new ClassName("org.primefaces.json.JSONArray"))
 			.forceInclude(mandatoryJar)
 			
 			.getJar();
 		
 		System.out.println(outJar.getName());
 		final Enumeration<JarEntry> entries = outJar.entries();
 		while (entries.hasMoreElements()) {
 			
 			System.out.println(entries.nextElement().getName());
 		}
 		
 		Assert.assertTrue("out jar is null", outJar != null);
 		
 		final String outJarName = Tokenizer.delimiter(File.separator).tokenize(outJar.getName()).lastToken();
 		Assert.assertTrue(outJarName.equals(Settings.DEFAULT_FACADE_JAR_NAME.getValue()));
 		
		Assert.assertTrue("org.primefaces.json.JSONArray, mandatory include not found", outJar.getEntry(Joiner.on(File.separator).join("org", "primefaces", "json", "JSONArray.class")) != null);
 		
 		final Enumeration<JarEntry> mandatoryEntries = mandatoryJar.entries();
 		
 		while (mandatoryEntries.hasMoreElements()) {
 			final JarEntry mandatoryEntry = mandatoryEntries.nextElement();
 			
 			// skip non-class entries
 			if (mandatoryEntry.getName().contains("META-INF") || mandatoryEntry.getName().contains("templates")) {
 				continue;
 			}
 			
 			final Enumeration<JarEntry> actualEntries = outJar.entries();
 			boolean found = false;
 			while (actualEntries.hasMoreElements()) {
 				final JarEntry actualEntry = actualEntries.nextElement();
 				
				final String mandatoryEntryName = normalize(mandatoryEntry.getName());
				final String actualEntryName = normalize(actualEntry.getName());
				if (mandatoryEntryName.equals(actualEntryName)) {
 					found = true;
 					break;
 				}
 			}
 			if (!found) {
 				Assert.fail("entry: " + mandatoryEntry.getName() + ", not found");
 			}
 		}
 	}
	
	// Here be dragons.
	// A.K.A. The-Magic-Method-Of-Many-Slashes
	private String normalize(String jarEntryName) {
		if (jarEntryName.contains("/")) {
			return jarEntryName.replace("/", File.separator);
		}
		if (jarEntryName.contains("\\")) {
			return jarEntryName.replace("\\", File.separator); // because Windows is awesome !
		}
		return jarEntryName;
	}
 
 	private String toPath(URL uri) {
 		return Tokenizer.delimiter(":").tokenize(uri.toString()).lastToken();
 	}
 }
