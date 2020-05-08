 package nextmethod.web.razor.utils;
 
 import com.google.common.base.Joiner;
 import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
 import nextmethod.base.Debug;
 import nextmethod.base.Delegates;
 import nextmethod.web.razor.DebugArgs;
 
 import javax.annotation.Nonnull;
 import java.nio.file.FileSystem;
 import java.nio.file.FileSystems;
 import java.nio.file.Path;
 import java.util.List;
 import java.util.concurrent.TimeUnit;
 
 import static junit.framework.Assert.assertEquals;
 
 public final class MiscUtils {
 
 	private MiscUtils() {}
 
 	public static final int TimeoutInSeconds = 1;
 
 	@SuppressWarnings("ConstantConditions")
 	public static void DoWithTimeoutIfNotDebugging(@Nonnull Delegates.IFunc1<Long, Boolean> withTimeout) {
 		if (Debug.isDebugArgPresent(DebugArgs.DebuggerIsAttached)) {
 			withTimeout.invoke(Long.MAX_VALUE);
 		}
 		else {
 			assertEquals("Timeout Expired!", true, withTimeout.invoke(TimeUnit.SECONDS.toMillis(TimeoutInSeconds)).booleanValue());
 		}
 	}
 
 
 
 	public static String createTestFilePath(final String... parts) {
		final List<String> pathParts = Lists.newArrayList(parts);
 		final FileSystem fileSystem = FileSystems.getDefault();
		String first = pathAsString(Iterables.getFirst(fileSystem.getRootDirectories(), null));
		if (first != null && !first.startsWith(fileSystem.getSeparator())) {
			// Strip the last character if it equals the filesystem separator
			if (first.length() > 1 && first.endsWith(fileSystem.getSeparator())) {
				first = first.substring(0, first.length() - 1);
			}
			pathParts.add(0, first);
 		}
 		return Joiner.on(fileSystem.getSeparator()).join(pathParts);
 	}

	private static String pathAsString(final Path path) {
		return path != null ? path.toString() : null;
	}
 }
