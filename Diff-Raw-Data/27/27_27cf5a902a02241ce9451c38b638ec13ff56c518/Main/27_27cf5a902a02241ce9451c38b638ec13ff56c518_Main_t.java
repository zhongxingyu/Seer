 package ch.unibe.scg.cc;
 
 import java.io.File;
 import java.io.IOException;
 
 import org.apache.commons.lang3.time.StopWatch;
 import org.apache.commons.vfs2.FileObject;
 import org.apache.commons.vfs2.VFS;
 
 import ch.unibe.scg.cc.modules.CCModule;
import ch.unibe.scg.cc.modules.HBaseModule;
 import ch.unibe.scg.cc.modules.JavaModule;
 
 import com.google.inject.Guice;
 import com.google.inject.Injector;
 
 public class Main {
 	public static final byte TYPE_1_CLONE = 0x01;
 	public static final byte TYPE_2_CLONE = 0x02;
 	public static final byte TYPE_3_CLONE = 0x03;
 
 
 	public static void main(String[] args) throws  IOException {
 		Injector injector = Guice.createInjector(
 				new JavaModule(), 
				new CCModule(),
				new HBaseModule()
 		);
 		ProjectWalker walker = injector.getInstance(ProjectWalker.class);
 		stoppedWalk(walker);
 	}
 
 	static void stoppedWalk(ProjectWalker walker) throws  IOException {
 		StopWatch stopWatch = new StopWatch();
 		stopWatch.start();
 		walk(walker);
 		stopWatch.stop();
 		System.out.format("Time needed: %,8d msec%n", stopWatch.getTime());
 	}
 
 	public static void walk(ProjectWalker walker) throws  IOException {
 		crawl(walker, "eclipse-ant", "projects/eclipse-ant.zip");
 //		crawl(walker, "eclipse-jdtcore", "projects/eclipse-jdtcore.zip");
 //		crawl(walker, "j2sdk1.4.0-javax-swing", "projects/j2sdk1.4.0-javax-swing.zip");
 //		crawl(walker, "netbeans-javadoc", "projects/netbeans-javadoc.zip");
 	}
 	 
 	private static void crawl(ProjectWalker walker, String projectName, String filePath) throws IOException {
 		FileObject file = VFS.getManager().resolveFile(new File("."), filePath);
 		file = VFS.getManager().resolveFile("zip://" + file.getURL().getPath());
 		// FileObject file = VFS.getManager().resolveFile(new File("."),
 		// "projects/onefile/");
 		walker.crawl(file, projectName);
 	} 
 
 }
