 /*
  * NAME: era.foss.typeeditor.AttributesForm
  */
 
 package era.foss.typeeditor;
 
 import org.eclipse.emf.common.command.Command;
 import org.eclipse.emf.common.notify.AdapterFactory;
 import org.eclipse.emf.edit.command.AddCommand;
 import org.eclipse.emf.edit.provider.IChangeNotifier;
 import org.eclipse.emf.edit.ui.provider.AdapterFactoryContentProvider;
 import org.eclipse.jface.layout.TableColumnLayout;
 import org.eclipse.jface.viewers.ColumnWeightData;
 import org.eclipse.jface.viewers.ITableLabelProvider;
 import org.eclipse.jface.viewers.LabelProvider;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.jface.viewers.TableViewerColumn;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.ui.IEditorPart;
 
 import era.foss.rif.AttributeDefinition;
import era.foss.rif.DatatypeDefinition;
 import era.foss.rif.SpecType;
 import era.foss.rif.impl.RifFactoryImpl;
 
 
 /**
  * Editor form for manipulating attributes.
  * 
  * @author cpn
  */
 public class AttributesForm extends AbstractTypesForm {
 
  // for now the one and only spec type
     private SpecType specType;
 
     public AttributesForm( Composite parent, IEditorPart editor ) {
         super( parent, editor, SWT.NONE );
     }
 
     /**
      * @see era.foss.typeeditor.AbstractTypesForm#constructorPreHook()
      * @since 17.03.2010
      */
     @Override
     protected void constructorPreHook() {
         super.constructorPreHook();
         if (rifModel.getCoreContent().getSpecTypes().size() == 0)
         {
             Command addCommand = AddCommand.create( editingDomain,
                                                     rifModel.getCoreContent(),
                                                     null,
                                                    RifFactoryImpl.eINSTANCE.createSpecType());
             eraCommandStack.execute( addCommand );
         }
         specType=(SpecType)rifModel.getCoreContent().getSpecTypes().get(0);
     }
 
     
     /**
      * Provide data for table containing SpecTypes
      */
     public class AttributesAdapterFactoryContentProvider extends
             AdapterFactoryContentProvider {
         public AttributesAdapterFactoryContentProvider( AdapterFactory adapterFactory ) {
             super( adapterFactory );
             ((IChangeNotifier)adapterFactory).addListener( this );
         }
 
         public Object[] getElements( Object object ) {
 
             Object[] objects;
             try {
                 objects = specType.getSpecAttributes().toArray();
             } catch( NullPointerException e ) {
                 objects = new Object[0];
             }
             return objects;
         }
     }
 
     /**
      * Provide label for data of table containing SpecTypes
      */
     public class AttributesLabelProvider extends LabelProvider implements
             ITableLabelProvider {
 
         @Override
         public String getColumnText( Object element, int columnIndex ) {
             AttributeDefinition attribute = (AttributeDefinition)element;
 
             switch (columnIndex) {
             case 0:
                 return attribute.getLongName();
             case 1:
                DatatypeDefinition type = attribute.getType();
                if(type == null){
                    return "";
                }
                else {
                    return type.getLongName();
                }
                    
             default:
                 throw new RuntimeException( "Should not happen" );
             }
         }
 
         @Override
         public Image getColumnImage( Object element, int columnIndex ) {
             return null;
         }
     }
 
     /**
      * @see era.foss.typeeditor.AbstractTypesForm#setupLeftSide()
      * @since 17.03.2010
      */
     @Override
     protected TableViewer setupLeftSide() {
 
         final AddDeleteTableViewer tableViewer = new AddDeleteTableViewer(
             this,
             SWT.MULTI | SWT.V_SCROLL | SWT.BORDER | SWT.FULL_SELECTION );
         tableViewer.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );
 
         tableViewer.setEditingDomain( editingDomain );
         tableViewer.setAddCommandParameter( specType,
                                             RifFactoryImpl.eINSTANCE.createAttributeDefinitionSimple().eClass() );
         TableColumnLayout columnLayout = tableViewer.getTableColumnLayout();
         String[] colTitles = {"Name","Datatype"};
         int[] colMinWidth = {100,100};
         int[] colWeigth = {50,50};
         boolean[] colResize = {true,false};
         for( int colNr = 0; colNr < colTitles.length; colNr++ ) {
 
             TableViewerColumn column = new TableViewerColumn(
                 tableViewer,
                 SWT.NONE );
             column.getColumn().setText( colTitles[colNr] );
             column.getColumn().setResizable( colResize[colNr] );
             column.getColumn().setMoveable( false );
             columnLayout.setColumnData( column.getColumn(),
                                         new ColumnWeightData(
                                             colWeigth[colNr],
                                            colMinWidth[colNr] ) );
             // TODO: enable editing support
             //column.setEditingSupport( new DatatypesEditingSupport(
              //tableViewer,
              //colNr ) );
         }
 
         tableViewer.setContentProvider( new AttributesAdapterFactoryContentProvider(
             adapterFactory ) );
         tableViewer.setLabelProvider( new AttributesLabelProvider() );
 
         tableViewer.setInput( editingDomain.getResourceSet() );
 
         return tableViewer;
     }
 
 }
