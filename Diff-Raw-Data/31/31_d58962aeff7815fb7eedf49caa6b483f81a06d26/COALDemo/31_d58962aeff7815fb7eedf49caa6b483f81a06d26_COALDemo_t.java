 package eu.monnetproject.coal.demo;
 
 import java.io.File;
 import java.io.IOException;
 
 import com.vaadin.*;
 import com.vaadin.terminal.gwt.server.WebApplicationContext;
 import com.vaadin.ui.*;
 import eu.monnetproject.align.Aligner;
 
 import eu.monnetproject.align.Alignment;
 import eu.monnetproject.align.AlignmentSerializer;
 import eu.monnetproject.coal.CoalAligner;
 import eu.monnetproject.coal.CoalAlignment;
 import eu.monnetproject.data.FileDataSource;
 import eu.monnetproject.ontology.Ontology;
 import eu.monnetproject.ontology.OntologySerializer;
 import java.util.logging.Logger;
 import javax.servlet.ServletContext;
 
 public class COALDemo extends Application {
 
    private final Alignment alignment = new CoalAlignment(5,null,null);
     private final AlignmentSerializer alignmentSerializer;
     private VerticalLayout contentVerticalLayout;
     private final Logger log = Logger.getLogger(COALDemo.class.getName());
 
     public String getPath() {
         return "/coal";
     }
 
     public String getWidgetSet() {
         return null;
     }
     private Window mainWindow = new Window("Cross-Lingual Ontology Alignment Demo");
     final ProgressIndicator indicator =
             new ProgressIndicator(new Float(0.0));
     private final Aligner aligner;
 
     public COALDemo(AlignmentSerializer alignmentSerializer, Aligner aligner, OntologySerializer serializer) {
         this.alignmentSerializer = alignmentSerializer;
         this.aligner = aligner;
         this.serializer = serializer;
     }
 
     @Override
     public void init() {
         mainWindow.removeAllComponents();
         this.contentVerticalLayout = new VerticalLayout();
         if(aligner instanceof CoalAligner) {
             ServletContext context = ((WebApplicationContext)getContext()).getHttpSession().getServletContext();
             final String cfgPath = context.getRealPath("/WEB-INF/classes/load/matcher.cfg");
             File cfg = null;
             if(cfgPath == null || !(cfg = new File(cfgPath)).exists()) {
                 contentVerticalLayout.addComponent(new Label("Could not locate config @ " + cfgPath));
             } 
             final String classifyPath = context.getRealPath("/WEB-INF/classes/load/svm_rank_classify");
             File classify = null;
             if(classifyPath == null || !(classify = new File(classifyPath)).exists()) {
                 contentVerticalLayout.addComponent(new Label("Could not locate classifier @ " + classifyPath));
             } else if(!classify.canExecute()) {
                 classify.setExecutable(true);
             }
             final String learnPath = context.getRealPath("/WEB-INF/classes/load/svm_rank_learn");
             File learn = null;
             if(learnPath == null || !(learn = new File(learnPath)).exists()) {
                 contentVerticalLayout.addComponent(new Label("Could not locate learner @ " + learnPath));
             } else if(!learn.canExecute()) {
                 learn.setExecutable(true);
             }
             final String modelPath = context.getRealPath("/WEB-INF/classes/load/model.dat");
             File model = null;
             if(modelPath == null || !(model = new File(modelPath)).exists()) {
                 contentVerticalLayout.addComponent(new Label("Could not locate model @ " + modelPath));
             } 
             
             System.err.println("Reconfiguring aligner");
             ((CoalAligner)aligner).reconfigure(cfg, classify, learn,model);
         }
 
         setMainWindow(mainWindow);
 
 
         contentVerticalLayout.addComponent(UIFactory.getUploadComponent());
         mainWindow.setContent(contentVerticalLayout);
     }
     private final OntologySerializer serializer;
     private Panel progressPanel;
 
     public void startAlignment(File src, File tgt, Button button) {
 
         final Ontology srcOnto = serializer.read(new FileDataSource(src));
         final Ontology tgtOnto = serializer.read(new FileDataSource(tgt));
 
         progressPanel = new Panel();
 
         contentVerticalLayout.addComponent(UIFactory.getVSpacing());
         indicator.setCaption("Aligning");
 
         progressPanel.addComponent(indicator);
 
         contentVerticalLayout.addComponent(progressPanel);
 
         AlignmentThread alignmentThread = new AlignmentThread(srcOnto, tgtOnto, src, tgt, button);
         alignmentThread.start();
 
     }
 
     public void reset() {
 
         this.contentVerticalLayout.removeAllComponents();
         init();
 
     }
 
     class AlignmentThread extends Thread {
 
         private Ontology srcOnto;
         private Ontology tgtOnto;
         private File srcFile;
         private File tgtFile;
         private ProgressThread progressThread;
         private volatile boolean finished = false;
         private Button button;
 
         public AlignmentThread(Ontology src, Ontology tgt, File srcFile, File tgtFile, Button button) {
             super();
             this.srcOnto = src;
             this.tgtOnto = tgt;
             this.srcFile = srcFile;
             this.tgtFile = tgtFile;
             this.button = button;
         }
 
         public void run() {
 
             this.progressThread = new ProgressThread(this, srcOnto.getEntities().size(), "Aligning", new AlignerProgressWrapper());
             progressThread.start();
 
            alignment.setSourceOntology(srcOnto);
            alignment.setTargetOntology(tgtOnto);
            aligner.align(alignment);
 
             finished = true;
 
             File outputFile = new File(System.getProperty("java.io.tmpdir") + System.getProperty("file.separator") + srcFile.getName().substring(0, srcFile.getName().lastIndexOf(".")) + "_" + tgtFile.getName().substring(0, tgtFile.getName().lastIndexOf(".")) + "_alignment.rdf");
 
             try {
                 finished = false;
                 this.progressThread = new ProgressThread(this, alignment.getSourceEntities().size(), "Writing alignment to " + outputFile.getAbsolutePath(), new WriterProgressWrapper());
                 progressThread.start();
                 alignmentSerializer.writeAlignment(alignment, outputFile);
                 finished = true;
             } catch (IOException e) {
                 log.severe(e.getMessage());
             }
 
             contentVerticalLayout.addComponent(UIFactory.getVSpacing());
 
             progressPanel.removeAllComponents();
 
             progressPanel.addComponent(new Label("Wrote alignment to " + outputFile.getAbsolutePath()));
 
             contentVerticalLayout.addComponent(UIFactory.getVSpacing());
 
             contentVerticalLayout.addComponent(UIFactory.getResultPanel(outputFile));
 
             button.setEnabled(true);
 
         }
 
         public boolean isFinished() {
             return this.finished;
         }
     }
 
     class ProgressThread extends Thread {
 
         private int size;
         private AlignmentThread alignThread;
         private ProgressWrapper wrapper;
 
         public ProgressThread(AlignmentThread alignThread, int size, String caption, ProgressWrapper wrapper) {
             super();
             this.size = size;
             this.alignThread = alignThread;
             this.wrapper = wrapper;
             indicator.addStyleName("indented");
             indicator.addStyleName("bubble");
 
             indicator.setPollingInterval(100);
             indicator.setValue(0.0);
             indicator.setCaption(caption);
 
         }
 
         public void run() {
 
             Double value = 0.0;
 
             while (!alignThread.isFinished()) {
 
                 value = new Double(wrapper.getProgress()) / new Double(size);
 
                 if (value >= 1.0) {
                     indicator.setValue(new Float(1.0));
                 } else {
                     indicator.setValue(new Float(value));
                 }
 
                 try {
                     sleep(500);
                 } catch (InterruptedException e) {
                     log.severe(e.getMessage());
                 }
 
             }
 
             indicator.setValue(1.0);
 
         }
     }
 
     class AlignerProgressWrapper implements ProgressWrapper {
 
         public AlignerProgressWrapper() {
         }
 
         public int getProgress() {
             return aligner.getProgress();
         }
     }
 
     class WriterProgressWrapper implements ProgressWrapper {
 
         public WriterProgressWrapper() {
         }
 
         public int getProgress() {
             return alignmentSerializer.getProgress();
         }
     }
 
     interface ProgressWrapper {
 
         public int getProgress();
     }
 }
