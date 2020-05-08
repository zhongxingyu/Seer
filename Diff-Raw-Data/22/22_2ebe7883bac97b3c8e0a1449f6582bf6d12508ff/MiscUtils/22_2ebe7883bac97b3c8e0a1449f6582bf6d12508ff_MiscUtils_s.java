 package nextmethod.web.razor.utils;
 
 import com.google.common.base.Joiner;
 import com.google.common.collect.Iterables;
 import nextmethod.base.Debug;
 import nextmethod.base.Delegates;
 import nextmethod.web.razor.DebugArgs;
 
 import javax.annotation.Nonnull;
 import java.nio.file.FileSystem;
 import java.nio.file.FileSystems;
 import java.nio.file.Path;
import java.util.Arrays;
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
		final List<String> pathParts = Arrays.asList(parts);
 		final FileSystem fileSystem = FileSystems.getDefault();
		Path first = Iterables.getFirst(fileSystem.getRootDirectories(), null);
		if (first != null && !first.toString().equalsIgnoreCase(fileSystem.getSeparator())) {
			pathParts.add(first.toString());
 		}
 		return Joiner.on(fileSystem.getSeparator()).join(pathParts);
 	}
 }
