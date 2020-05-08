 package sdl.ist.osaka_u.newmasu.Plugin.graph;
 
 import org.eclipse.jdt.core.dom.CompilationUnit;
 import sdl.ist.osaka_u.newmasu.Plugin.Plugin;
 import sdl.ist.osaka_u.newmasu.Settings;
 import sdl.ist.osaka_u.newmasu.data.BindingManager;
 
 import java.nio.file.Paths;
 import java.util.Set;
 
 public class GraphPlugin implements Plugin {
     @Override
     public void run() {
        System.out.println("Graph Plugin");
         Set<String> paths = Settings.getInstance().getListFiles();
         for(String path : paths){
            System.out.println(path.toString() + " processing...");
             CompilationUnit unit = BindingManager.getRel().getCallerMap().get(Paths.get(path));
             unit.accept(new GraphVisitor());
         }
     }
 }
