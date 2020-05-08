 /**
  * 
  */
 package cz.cuni.mff.peckam.java.origamist.model;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Hashtable;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.ResourceBundle;
 import java.util.concurrent.Callable;
 
 import javax.xml.bind.annotation.XmlTransient;
 
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 
 import cz.cuni.mff.peckam.java.origamist.common.Author;
 import cz.cuni.mff.peckam.java.origamist.common.BinaryImage;
 import cz.cuni.mff.peckam.java.origamist.common.LangString;
 import cz.cuni.mff.peckam.java.origamist.common.License;
 import cz.cuni.mff.peckam.java.origamist.files.File;
 import cz.cuni.mff.peckam.java.origamist.modelstate.DefaultModelState;
 import cz.cuni.mff.peckam.java.origamist.utils.LangStringHashtableObserver;
 import cz.cuni.mff.peckam.java.origamist.utils.ObservableList;
 import cz.cuni.mff.peckam.java.origamist.utils.ObservablePropertyEvent;
 import cz.cuni.mff.peckam.java.origamist.utils.ObservablePropertyListener;
 
 /**
  * The origami diagram.
  * <p>
  * Provided property: src
  * <p>
  * See {@link cz.cuni.mff.peckam.java.origamist.model.jaxb.Origami} for other bound properties.
  * 
  * @author Martin Pecka
  */
 @XmlTransient
 public class Origami extends cz.cuni.mff.peckam.java.origamist.model.jaxb.Origami
 {
     /**
      * The hastable for more comfortable search in localized names.
      */
     protected final Hashtable<Locale, String> names             = new Hashtable<Locale, String>();
     /**
      * The hastable for more comfortable search in localized short descriptions.
      */
     protected final Hashtable<Locale, String> shortDescs        = new Hashtable<Locale, String>();
     /**
      * The hastable for more comfortable search in localized descriptions.
      */
     protected final Hashtable<Locale, String> descriptions      = new Hashtable<Locale, String>();
 
     /** If the origami is loaded without the model, then this task will be run the first time the model is read. */
     protected Callable<Model>                 loadModelCallable = null;
 
     /** The file in the listing containing this origami. */
     protected File                            file              = null;
 
     /**
      * The URL this origami was created from. Obviously this will be <code>null</code> for the just-being-created model.
      */
     protected URL                             src               = null;
 
     /** The cached number of pages required for this origami. */
     protected Integer                         pages             = null;
 
     /** The placement of steps in the page from key. */
     protected final Map<Integer, Integer[]>   stepsOnPages      = new HashMap<Integer, Integer[]>();
 
     /** The first step displayed on the page from key. */
     protected final Map<Integer, Step>        firstStepOnPages  = new HashMap<Integer, Step>();
 
     /**
      * Create a new origami diagram.
      */
     public Origami()
     {
         ((ObservableList<LangString>) getName()).addObserver(new LangStringHashtableObserver(names));
         ((ObservableList<LangString>) getShortdesc()).addObserver(new LangStringHashtableObserver(shortDescs));
         ((ObservableList<LangString>) getDescription()).addObserver(new LangStringHashtableObserver(descriptions));
         addObservablePropertyListener(new ObservablePropertyListener<Step>() {
             @Override
             public void changePerformed(ObservablePropertyEvent<? extends Step> evt)
             {
                 pages = null;
                 stepsOnPages.clear();
                 firstStepOnPages.clear();
             }
         }, MODEL_PROPERTY, Model.STEPS_PROPERTY, Steps.STEP_PROPERTY);
         PropertyChangeListener pagesCacheListener = new PropertyChangeListener() {
             @Override
             public void propertyChange(PropertyChangeEvent evt)
             {
                 pages = null;
                 stepsOnPages.clear();
                 firstStepOnPages.clear();
             }
         };
         addPropertyChangeListener(pagesCacheListener, PAPER_PROPERTY, DiagramPaper.COLS_PROPERTY);
         addPropertyChangeListener(pagesCacheListener, PAPER_PROPERTY, DiagramPaper.ROWS_PROPERTY);
         addPropertyChangeListener(pagesCacheListener, MODEL_PROPERTY, Model.STEPS_PROPERTY, Steps.STEP_PROPERTY,
                 Step.COLSPAN_PROPERTY);
         addPropertyChangeListener(pagesCacheListener, MODEL_PROPERTY, Model.STEPS_PROPERTY, Steps.STEP_PROPERTY,
                 Step.ROWSPAN_PROPERTY);
     }
 
     /**
      * Return the localized name of the model.
      * 
      * @param l The locale of the name. If null or not found, returns the
      *            content of the first &lt;name> element defined
      * @return The localized note
      */
     public String getName(Locale l)
     {
         if (names.size() == 0) {
             ResourceBundle b = ResourceBundle.getBundle("cz.cuni.mff.peckam.java.origamist.model.Origami", l);
             return b.getString("nameNotFound");
         }
 
         if (l == null || !names.containsKey(l))
             return names.elements().nextElement();
         return names.get(l);
     }
 
     /**
      * Add a name in the given locale.
      * 
      * @param l The locale of the name
      * @param name The name to add
      */
     public void addName(Locale l, String name)
     {
         LangString s = (LangString) new cz.cuni.mff.peckam.java.origamist.common.jaxb.ObjectFactory()
                 .createLangString();
         s.setLang(l);
         s.setValue(name);
         this.name.add(s);
     }
 
     /**
      * Return the localized short description of the model.
      * 
      * @param l The locale of the short descripton. If null or not found,
      *            returns the content of the first &lt;shortdesc> element
      *            defined
      * @return The localized note
      */
     public String getShortDesc(Locale l)
     {
         if (shortDescs.size() == 0) {
             ResourceBundle b = ResourceBundle.getBundle("cz.cuni.mff.peckam.java.origamist.model.Origami", l);
             return b.getString("shortDescNotFound");
         }
 
         if (l == null || !shortDescs.containsKey(l))
             return shortDescs.elements().nextElement();
         return shortDescs.get(l);
     }
 
     /**
      * Add a short description in the given locale.
      * 
      * @param l The locale of the short description
      * @param desc The short description to add to add
      */
     public void addShortDesc(Locale l, String desc)
     {
         LangString s = (LangString) new cz.cuni.mff.peckam.java.origamist.common.jaxb.ObjectFactory()
                 .createLangString();
         s.setLang(l);
         s.setValue(desc);
         this.shortdesc.add(s);
     }
 
     /**
      * Return the localized description of the model.
      * 
      * @param l The locale of the description. If null or not found, returns the
      *            content of the first &lt;description> element defined
      * @return The localized description
      */
     public String getDescription(Locale l)
     {
         if (descriptions.size() == 0) {
             // TODO possible fallback to short description
             ResourceBundle b = ResourceBundle.getBundle("cz.cuni.mff.peckam.java.origamist.model.Origami", l);
             return b.getString("descriptionNotFound");
         }
 
         if (l == null || !descriptions.containsKey(l))
             return descriptions.elements().nextElement();
         return descriptions.get(l);
     }
 
     /**
      * Add a description in the given locale.
      * 
      * @param l The locale of the description
      * @param name The description to add
      */
     public void addDescription(Locale l, String desc)
     {
         LangString s = (LangString) new cz.cuni.mff.peckam.java.origamist.common.jaxb.ObjectFactory()
                 .createLangString();
         s.setLang(l);
         s.setValue(desc);
         this.description.add(s);
     }
 
     @Override
     public Model getModel()
     {
         if (loadModelCallable != null) {
             try {
                 // TODO notify the user about loading
                 Callable<Model> callable = loadModelCallable;
                 loadModelCallable = null;
                 this.model = callable.call();
                 init();
             } catch (Exception e) {
                 this.model = (Model) new ObjectFactory().createModel();
                 Logger.getLogger("application").l7dlog(Level.ERROR, "modelLazyLoadException", e);
             }
         }
         return super.getModel();
     }
 
     /**
      * @param loadModelCallable the loadModelCallable to set
      */
     public void setLoadModelCallable(Callable<Model> loadModelCallable)
     {
         this.loadModelCallable = loadModelCallable;
     }
 
     /**
      * Return the number of pages needed for this origami.
      * <p>
      * This method requires the model to be completely loaded.
      * 
      * @return The number of pages needed for this origami.
      */
     public int getNumberOfPages()
     {
         if (pages == null)
             updatePagesCache();
 
         return pages;
     }
 
     /**
      * Return the placement of steps on the page as a list of linearized grid positions (sorted by the ordering of steps
      * on the page).
      * <p>
      * This method requires the model to be completely loaded.
      * 
      * @param page The number of the page (starting from 1).
      * @return The map of steps placement on the given page.
      */
     public Integer[] getStepsPlacement(int page)
     {
         if (stepsOnPages.get(page) == null)
             updatePagesCache();
 
         return stepsOnPages.get(page);
     }
 
     /**
      * Return the first step displayed on the given page.
      * 
      * @param page The page.
      * @return The step that is first on that page.
      */
     public Step getFirstStep(int page)
     {
         if (firstStepOnPages.get(page) == null)
             updatePagesCache();
 
         return firstStepOnPages.get(page);
     }
 
     /**
      * Return the number of the page the given step is displayed on.
      * 
      * @param step The step to search page for.
      * @return The page number (starting from 1).
      */
     public int getPage(Step step)
     {
         if (!getModel().getSteps().getStep().contains(step))
             return -1;
         Entry<Integer, Step> prev = null;
         for (Entry<Integer, Step> e : firstStepOnPages.entrySet()) {
             if (step == e.getValue())
                 return e.getKey();
 
             if (prev != null && prev.getValue().getId() < step.getId() && e.getValue().getId() > step.getId())
                 return prev.getKey();
 
             prev = e;
         }
         return getNumberOfPages();
     }
 
     /**
      * Recompute the number of pages and the number of steps to be placed on every page.
      */
     protected void updatePagesCache()
     {
         stepsOnPages.clear();
         firstStepOnPages.clear();
 
         if (getModel().getSteps().getStep().size() == 0) {
             pages = 0;
             return;
         }
 
         // a simple layout algoritm is implemented - try to place the step at cursor, and if it cannot be done, search
         // first free space - first looking to the right, then maybe going to another line, and finally mabye going to a
         // brand new page; the fitting is determined using a bitmap
 
         final int maxX = getPaper().getCols(), maxY = getPaper().getRows();
         int cursorX = 0, cursorY = 0;
         int pageNr = 1;
         final List<Integer> stepsPlacement = new LinkedList<Integer>();
 
         final boolean[] map = new boolean[maxX * maxY];
         Arrays.fill(map, false);
 
         for (Step step : getModel().getSteps().getStep()) {
             int width = step.getColspan() != null ? step.getColspan() - 1 : 0, height = step.getRowspan() != null ? step
                     .getRowspan() - 1 : 0;
             boolean fits;
             do {
                 fits = true;
                 if (cursorX + width > maxX) {
                     fits = false;
                 } else if (cursorY + height > maxY) {
                     fits = false;
                 } else {
                     fit: for (int i = cursorX; i <= cursorX + width; i++) {
                         for (int j = cursorY; j <= cursorY + height; j++) {
                             if (map[i + j * maxX]) {
                                 fits = false;
                                 break fit;
                             }
                         }
                     }
                 }
 
                 if (!fits) {
                     if (cursorX + width < maxX - 1) {
                         cursorX++;
                     } else if (cursorY + height < maxY - 1) {
                         cursorY++;
                         cursorX = 0;
                     } else {
                         cursorX = 0;
                         cursorY = 0;
                         stepsOnPages.put(pageNr, stepsPlacement.toArray(new Integer[] {}));
                         stepsPlacement.clear();
                         stepsPlacement.add(0);
                         pageNr++;
                         Arrays.fill(map, false);
                         for (int i = 0; i <= width; i++) {
                             for (int j = 0; j <= height; j++) {
                                 map[i + j * maxX] = true;
                             }
                         }
                         firstStepOnPages.put(pageNr, step);
                         break;
                     }
                     continue;
                 }
 
                 stepsPlacement.add(cursorX + cursorY * maxX);
                 for (int i = cursorX; i <= cursorX + width; i++) {
                     for (int j = cursorY; j <= cursorY + height; j++) {
                         map[i + j * maxX] = true;
                     }
                 }
                 if (firstStepOnPages.get(pageNr) == null)
                     firstStepOnPages.put(pageNr, step);
                 break;
             } while (!fits);
         }
 
         if (stepsOnPages.get(pageNr) == null)
             stepsOnPages.put(pageNr, stepsPlacement.toArray(new Integer[] {}));
 
         pages = stepsOnPages.size();
     }
 
     /**
      * @return the file
      */
     @XmlTransient
     public File getFile()
     {
         return file;
     }
 
     /**
      * @param file the file to set
      */
     public void setFile(File file)
     {
         this.file = file;
     }
 
     /**
      * @return the src
      */
     @XmlTransient
     public URL getSrc()
     {
         return src;
     }
 
     /**
      * Free all the memory held by model state information.
      */
     public void unloadModelStates()
     {
         for (Step s : getModel().getSteps().getStep())
             s.unloadModelState();
     }
 
     /**
      * @param value the new src
      */
     public void setSrc(URL value)
     {
         URL oldValue = this.src;
         this.src = value;
         if ((oldValue == null && value != null) || (oldValue != null && value == null)
                 || (oldValue != null && value != null && !oldValue.equals(value)))
             support.firePropertyChange("src", oldValue, value);
     }
 
     /**
      * Initializes the substructures, so that no structure that may contain other structures will be <code>null</code>.
      */
     public void initStructure(boolean preserveExisting)
     {
         ObjectFactory of = new ObjectFactory();
         cz.cuni.mff.peckam.java.origamist.common.jaxb.ObjectFactory cof = new cz.cuni.mff.peckam.java.origamist.common.jaxb.ObjectFactory();
 
         if (!preserveExisting || getAuthor() == null)
             this.setAuthor((Author) cof.createAuthor());
         if (!preserveExisting || getLicense() == null)
             this.setLicense((License) cof.createLicense());
         if (!preserveExisting || getThumbnail() == null)
             this.setThumbnail(cof.createThumbnail());
         if (!preserveExisting || getThumbnail().getImage() == null)
             this.getThumbnail().setImage((BinaryImage) cof.createBinaryImage());
         if (!preserveExisting || getModel() == null)
             this.setModel((Model) of.createModel());
         if (!preserveExisting || getModel().getPaper() == null)
             this.getModel().setPaper((ModelPaper) of.createModelPaper());
         if (!preserveExisting || getModel().getPaper().getSize() == null)
             this.getModel().getPaper().setSize((UnitDimension) of.createUnitDimension());
         if (!preserveExisting || getModel().getPaper().getColors() == null)
             this.getModel().getPaper().setColors(of.createModelColors());
         if (!preserveExisting || getModel().getSteps() == null)
             this.getModel().setSteps((cz.cuni.mff.peckam.java.origamist.model.Steps) of.createSteps());
         if (!preserveExisting || getPaper() == null)
             this.setPaper((DiagramPaper) of.createDiagramPaper());
         if (!preserveExisting || getPaper().getColor() == null)
             this.getPaper().setColor(of.createDiagramColors());
         if (!preserveExisting || getPaper().getSize() == null)
             this.getPaper().setSize((UnitDimension) of.createUnitDimension());
     }
 
     /**
      * Set the given origami's metadata to be this origami's metadata. The new metadata are another instance of the
      * <code>from</code>'s metadata, so it doesn't reflect any further changes to the <code>from</code>'s metadata.
      * 
      * @param from The origami the metadata should be loaded from.
      */
     public void getMetadataFrom(Origami from)
     {
         // reset the metadata to the empty ones
         initStructure(true);
 
         if (!getName().equals(from.getName())) {
             getName().clear();
             for (LangString name : from.getName())
                 getName().add(name.clone());
         }
         setCreationDate((Date) from.getCreationDate().clone());
         if (!getShortdesc().equals(from.getShortdesc())) {
             getShortdesc().clear();
             for (LangString shortDesc : from.getShortdesc())
                 getShortdesc().add(shortDesc.clone());
         }
         if (!getDescription().equals(from.getDescription())) {
             getDescription().clear();
             for (LangString desc : from.getDescription())
                 getDescription().add(desc.clone());
         }
         try {
             setOriginal(new URI(from.getOriginal().toString()));
         } catch (URISyntaxException e) {} catch (NullPointerException e) {}
 
         getAuthor().setName(from.getAuthor().getName());
         try {
             getAuthor().setHomepage(new URI(from.getAuthor().getHomepage().toString()));
         } catch (URISyntaxException e) {} catch (NullPointerException e) {}
 
         getLicense().setContent(from.getLicense().getContent());
         try {
             getLicense().setHomepage(new URI(from.getLicense().getHomepage().toString()));
         } catch (URISyntaxException e) {} catch (NullPointerException e) {}
         getLicense().setName(from.getLicense().getName());
         if (!getLicense().getPermission().equals(from.getLicense().getPermission())) {
             getLicense().getPermission().clear();
             getLicense().getPermission().addAll(from.getLicense().getPermission());
         }
 
        byte[] newImage = Arrays.copyOf(from.getThumbnail().getImage().getValue(), from.getThumbnail().getImage()
                .getValue().length);
         getThumbnail().getImage().setType(from.getThumbnail().getImage().getType());
         getThumbnail().setGenerated(from.getThumbnail().isGenerated());
        getThumbnail().getImage().setValue(newImage);
 
         getModel().getPaper().setWeight(from.getModel().getPaper().getWeight());
         getModel().getPaper().getColors().setBackground(from.getModel().getPaper().getColors().getBackground());
         getModel().getPaper().getColors().setForeground(from.getModel().getPaper().getColors().getForeground());
         getModel().getPaper().getSize().setWidth(from.getModel().getPaper().getSize().getWidth());
         getModel().getPaper().getSize().setHeight(from.getModel().getPaper().getSize().getHeight());
         getModel().getPaper().getSize().setUnit(from.getModel().getPaper().getSize().getUnit());
         getModel()
                 .getPaper()
                 .getSize()
                 .setReference(from.getModel().getPaper().getSize().getReferenceUnit(),
                         from.getModel().getPaper().getSize().getReferenceLength());
         if (!getModel().getPaper().getNote().equals(from.getModel().getPaper().getNote())) {
             getModel().getPaper().getNote().clear();
             for (LangString s : from.getModel().getPaper().getNote())
                 getModel().getPaper().addNote(s.getLang(), s.getValue());
         }
 
         getPaper().getColor().setBackground(from.getPaper().getColor().getBackground());
         getPaper().setCols(from.getPaper().getCols());
         getPaper().setRows(from.getPaper().getRows());
         getPaper().getSize().setWidth(from.getPaper().getSize().getWidth());
         getPaper().getSize().setHeight(from.getPaper().getSize().getHeight());
         getPaper().getSize().setUnit(from.getPaper().getSize().getUnit());
         getPaper().getSize().setReference(from.getPaper().getSize().getReferenceUnit(),
                 from.getPaper().getSize().getReferenceLength());
     }
 
     @Override
     protected void init()
     {
         super.init();
         initListeners();
     }
 
     /**
      * Create pointers to previous/next steps and setup model state invalidation callbacks.
      */
     public void initListeners()
     {
         PropertyChangeListener invalidateListener = new PropertyChangeListener() {
             @Override
             public void propertyChange(PropertyChangeEvent evt)
             {
                 if (getModel() != null && getModel().getSteps() != null)
                     getModel().getSteps().invalidateSteps();
             }
         };
 
         addPrefixedPropertyChangeListener(invalidateListener, MODEL_PROPERTY, Model.PAPER_PROPERTY,
                 ModelPaper.COLORS_PROPERTY);
         addPrefixedPropertyChangeListener(invalidateListener, MODEL_PROPERTY, Model.PAPER_PROPERTY,
                 ModelPaper.SIZE_PROPERTY);
 
         ObservablePropertyListener<Step> defaultStateListener = new ObservablePropertyListener<Step>() {
             @Override
             public void changePerformed(ObservablePropertyEvent<? extends Step> evt)
             {
                 if (evt.getEvent().getItem().getPrevious() == null
                         && (evt.getEvent().getItem().defaultModelState == null || evt.getEvent().getItem().defaultModelState
                                 .getOrigami() != Origami.this))
                     evt.getEvent().getItem().setDefaultModelState(new DefaultModelState(Origami.this));
             }
         };
 
         addObservablePropertyListener(defaultStateListener, MODEL_PROPERTY, Model.STEPS_PROPERTY, Steps.STEP_PROPERTY);
 
         PropertyChangeListener defaultStatePropertyListener = new PropertyChangeListener() {
             @Override
             public void propertyChange(PropertyChangeEvent evt)
             {
                 if (getModel() != null && getModel().getSteps() != null && getModel().getSteps().getStep().size() > 0) {
                     Step firstStep = getModel().getSteps().getStep().get(0);
                     if (firstStep.defaultModelState == null || firstStep.defaultModelState.getOrigami() != Origami.this)
                         firstStep.setDefaultModelState(new DefaultModelState(Origami.this));
                 }
             }
         };
 
         addPrefixedPropertyChangeListener(defaultStatePropertyListener, MODEL_PROPERTY);
     }
 
     @Override
     public int hashCode()
     {
         final int prime = 31;
         int result = super.hashCode();
         result = prime * result + ((src == null) ? 0 : src.hashCode());
         return result;
     }
 
     @Override
     public boolean equals(Object obj)
     {
         if (this == obj)
             return true;
         if (!super.equals(obj))
             return false;
         if (getClass() != obj.getClass())
             return false;
         Origami other = (Origami) obj;
         if (src == null) {
             if (other.src != null)
                 return false;
         } else if (!src.equals(other.src))
             return false;
         return true;
     }
 }
