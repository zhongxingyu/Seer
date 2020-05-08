 package cytoscape.graph.layout.algorithm;
 
 // This import statement implies that this package depends on
 // packages outside of cytoscape.graph.*.  I kind of hesitate to do this.
 // At least, put this class in a package with as little code as possible so
 // that other code wouldn't necessitate this dependency also; dependencies are
 // a per-package concept.
 import cytoscape.process.Task;
 
 /**
  * This class encapsulates the logic needed to perform a layout on a graph.
  * One of the goals of this class is to limit the set
  * of dependent classes and packages to an absolute minimum.  This class
  * should be so concise that it would be a simple effort to
  * provide layout algorithms as web services using this layout framework.
 */
 public abstract class LayoutAlgorithm implements Task
 {
 
   /**
    * The graph that this algorithm will lay out.
    * By agreement, methods on <code>m_graph</code> will be called only from
    * the thread that invokes <code>run()</code>.
    **/
   protected final MutableGraphLayout m_graph;
 
   /**
    * Simply sets <code>m_graph</code> to reference the input parameter.
    **/
   protected LayoutAlgorithm(MutableGraphLayout graph)
   {
     if (graph == null) throw new NullPointerException("graph is null");
     this.m_graph = graph;
   }
 
   /**
    * <code>run()</code> is called externally, not by subclasses.
    * Subclasses must implement this method - layout logic should be
    * executed in this method, by the same thread that calls this method.
    * The layout logic should act on
    * the <code>MutableGraphLayout</code> stored in <code>this.m_graph</code>.
    * <code>run()</code> shall only be called once for a given instance.
    * If an asynchronous call to <code>halt()</code> is made while
    * <code>run()</code> is executing, <code>run()</code> should make an effort
    * to abort its operations and exit as soon as possible.
   */
   public abstract void run();
 
   /**
    * <code>halt()</code> is called externally, not by subclasses.
    * Subclasses must implement this method.  <code>halt()</code> should
    * not block; it should return quickly.  If [an asynchronous] thread is
    * executing <code>run()</code> when <code>halt()</code> is called,
    * a signal should be sent to the thread that is executing <code>run()</code>
    * to abort and exit <code>run()</code>.  If <code>run()</code> has not
    * been called at the time that <code>halt()</code> is invoked,
    * a later call to <code>run()</code> should not actually
    * &quot;run&quot; anything.  If <code>run()</code> has already been run
    * and has exited by the time <code>halt()</code> is called,
    * <code>halt()</code> should do nothing.
    * There is no guarantee that <code>halt()</code> will be called on
    * and instance of this class.
   */
   public abstract void halt();
 
 }
