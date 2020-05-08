 /*
  * NAME: era.foss.typeeditor.AbstractErfTypesForm
  */
 
 package era.foss.typeeditor;
 
 import org.eclipse.emf.common.command.CommandStack;
 import org.eclipse.emf.common.notify.AdapterFactory;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.xmi.XMIResource;
 import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
 import org.eclipse.emf.edit.domain.IEditingDomainProvider;
 import org.eclipse.emf.edit.ui.util.EditUIUtil;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Widget;
 import org.eclipse.ui.IEditorPart;
 
 import era.foss.erf.ERF;
 import era.foss.objecteditor.EraCommandStack;
 
 /**
  * An abstract form, that contains members and constructor code that is common to all editing forms for ERA types.
  * <p>
  * Contains a reference to the {@link ERF} model.
  * <p>
  * Contains a reference to the {@link EraCommandStack}.
  * 
  * @author cpn
  */
 public abstract class AbstractErfTypesForm extends Composite {
 
     protected IEditorPart editor = null;
     protected AdapterFactoryEditingDomain editingDomain = null;
     protected Resource erfResource = null;
     protected ERF erfModel = null;
     protected EraCommandStack eraCommandStack = null;
     protected AdapterFactory adapterFactory = null;
     protected Activator typeEditorActivator = null;
     
     /**
      * Constructs a new instance of this class - defaulting the style to {@link SWT#NONE}.
      * 
      * @param parent a widget which will be the parent of the new instance (cannot be null)
      * @param editor the editor from which the and {@link CommandStack} can be derived
      * 
      * @see #AbstractErfTypesForm(Composite, IEditorPart, int)
      */
     public AbstractErfTypesForm( Composite parent, IEditorPart editor ) {
         this( parent, editor, SWT.NONE );
     }
 
     /**
      * Constructs a new instance of this class given its parent, the editor and a style value describing its behavior
      * and appearance.
      * 
      * @param parent a widget which will be the parent of the new instance (cannot be null)
      * @param editor the editor from which the and {@link CommandStack} can be derived
      * @param style the style of widget to construct
      * 
      * @see SWT#NO_BACKGROUND
      * @see SWT#NO_FOCUS
      * @see SWT#NO_MERGE_PAINTS
      * @see SWT#NO_REDRAW_RESIZE
      * @see SWT#NO_RADIO_GROUP
      * @see SWT#EMBEDDED
      * @see SWT#DOUBLE_BUFFERED
      * @see Widget#getStyle
      */
     public AbstractErfTypesForm( Composite parent, IEditorPart editor, int style ) {
         super( parent, style );
 
         this.editor = editor;
         this.editingDomain = (AdapterFactoryEditingDomain)((IEditingDomainProvider)editor).getEditingDomain();
         this.erfResource = (XMIResource)editingDomain.getResourceSet()
                                                      .getResource( EditUIUtil.getURI( editor.getEditorInput() ), true );
        this.erfModel = (ERF)(erfResource).getContents().get( 0 );
 
         this.eraCommandStack = (EraCommandStack)editingDomain.getCommandStack();
         this.adapterFactory = ((AdapterFactoryEditingDomain)editingDomain).getAdapterFactory();
 
         this.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );
         this.typeEditorActivator = era.foss.typeeditor.Activator.INSTANCE;
     }
 
 }
