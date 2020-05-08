 package net.bioclipse.brunn.ui.editors.plateEditor;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.regex.Pattern;
 
 import net.bioclipse.brunn.Springcontact;
 import net.bioclipse.brunn.business.plate.IPlateManager;
 import net.bioclipse.brunn.pojos.AbstractSample;
 import net.bioclipse.brunn.pojos.DrugSample;
 import net.bioclipse.brunn.pojos.Plate;
 import net.bioclipse.brunn.pojos.PlateFunction;
 import net.bioclipse.brunn.pojos.Well;
 import net.bioclipse.brunn.pojos.WellFunction;
 import net.bioclipse.brunn.results.PlateResults;
 import net.bioclipse.brunn.ui.editors.plateEditor.model.JasperDiagram;
 import net.bioclipse.brunn.ui.editors.plateEditor.model.JasperFunction;
 import net.bioclipse.brunn.ui.editors.plateEditor.model.JasperPoint;
 import net.bioclipse.brunn.ui.editors.plateEditor.model.JasperRootBean;
 import net.bioclipse.brunn.ui.editors.plateEditor.model.ReplicateTableModel;
 import net.bioclipse.core.util.FileUtil;
 import net.bioclipse.core.util.LogUtils;
 import net.bioclipse.jasper.editor.ReportEditor;
 
 import org.apache.log4j.Logger;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.IExtension;
 import org.eclipse.core.runtime.IExtensionPoint;
 import org.eclipse.core.runtime.IExtensionRegistry;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IEditorSite;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.part.MultiPageEditorPart;
 
 public class PlateMultiPageEditor extends MultiPageEditorPart {
 
     private static final Logger logger 
         = Logger.getLogger( PlateMultiPageEditor.class );
    private static List<IPlateExportAction> exportActions;
     
     static {
         IExtensionRegistry registry = Platform.getExtensionRegistry();
         IExtensionPoint extensionPoint 
             = registry.getExtensionPoint(
                            "net.bioclipse.brunn.plateExportAction" );
         IExtension[] extensions = extensionPoint.getExtensions();
         for ( IExtension extension: extensions ) {
             for ( IConfigurationElement element 
                             : extension.getConfigurationElements() ) {
                 IPlateExportAction action = null;
                 try {
                     action = (IPlateExportAction) 
                              element.createExecutableExtension( "action" );
                 }
                 catch (CoreException e) {
                     LogUtils.handleException( e, 
                                               logger, 
                                               "net.bioclipse.brunn.ui" );
                 }
                 exportActions.add( action );
             }
         }
     }
     
     private static final Pattern siPattern 
         = Pattern.compile( "si?%", Pattern.CASE_INSENSITIVE );
 	private PlateEditor plateEditor;
 	private Summary summary;
 	private Replicates replicates;
 	private IC50 ic50;
 	private ReportEditor plateReport;
 	private List<OutlierChangedListener> outLierListeners 
 	    = new ArrayList<OutlierChangedListener>();
 	private Plate toBeSaved;
 
 	
 	public final static String ID 
 	    = "net.bioclipse.brunn.ui.editors.plateEditor.PlateMultiPageEditor"; 
 	
 	public void fireOutliersChanged() {
 		for( OutlierChangedListener l : outLierListeners ) {
 			l.onOutLierChange();
 		}
 	}
 	
 	public void addListener(OutlierChangedListener listener) {
 		outLierListeners.add(listener);
 	}
 	
 	public void removeListener( OutlierChangedListener listener ) {
 		outLierListeners.remove(listener);
 	}
 	
 	@Override
 	protected void createPages() {
 		
 		net.bioclipse.brunn.ui.explorer.model.nonFolders.Plate plate =
 			(net.bioclipse.brunn.ui.explorer.model.nonFolders.Plate)
 			getEditorInput();
 		PlateResults plateResults = plate.getPlateResults();
 		this.setPartName(plate.getName()); 
 		
 		toBeSaved = ( (Plate)plate.getPOJO() ).deepCopy(); 
 		
 		plateEditor = new PlateEditor( plateResults, 
 		                               this, 
 		                               toBeSaved,
 		                               exportActions );
 		replicates  = new Replicates( plateResults, 
 		                              this, 
 		                              toBeSaved,
 		                              exportActions );
 		summary     = new Summary( plateResults, 
 		                           this, 
 		                           toBeSaved, 
 		                           replicates,
 		                           exportActions );
 		ic50        = new IC50( this, replicates, exportActions );
 		plateReport = new ReportEditor();
 		
 		try {
 			int index = this.addPage( (IEditorPart) plateEditor, 
 			                          getEditorInput() );
 			setPageText(index, "Overview");
 			this.setActivePage(index);
 		} 
 		catch (PartInitException e) {
 			e.printStackTrace();
 		}
 
 		try {
 			int index = this.addPage((IEditorPart) summary, getEditorInput());
 			setPageText(index, "Summary");
 		} 
 		catch (PartInitException e) {
 			e.printStackTrace();			
 		}
 
 		try {
 			int index = this.addPage( (IEditorPart) replicates, 
 			                          getEditorInput() );
 			setPageText(index, "Average");
 		} 
 		catch (PartInitException e) {
 			e.printStackTrace();			
 		}
 
 		try {
 			int index = this.addPage((IEditorPart) ic50, getEditorInput());
 			setPageText(index, "IC50");
 		} 
 		catch (PartInitException e) {
 			e.printStackTrace();			
 		}
 		
 		try {
 			int index = this.addPage( (IEditorPart) plateReport, 
 			                          getEditorInput() );
 			setPageText(index, "Report");
 		} 
 		catch (PartInitException e) {
 			e.printStackTrace();			
 		}
 	}
 
 	@Override
 	public void doSave(IProgressMonitor monitor) {
 		plateEditor.doSave(monitor);
 		summary.doSave(monitor);
 	}
 
 	@Override
 	public void doSaveAs() {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	@Override
 	protected void pageChange(int newPageIndex){
 		if(newPageIndex == 4) {
             String reportPath;
             try {
                 reportPath = FileUtil.getFilePath( 
                                "reports/384Plate.jasper", 
                                net.bioclipse.brunn.ui.Activator.PLUGIN_ID );
 			plateReport.openReport( reportPath, 
 			                        createParamaters(), 
 			                        createBeanCollection() );
             }
             catch ( Exception e ) {
                 LogUtils.handleException( e, 
                                           logger , 
                                           "net.bioclipse.brunn.ui" );
             }
 		}
 	}
 
 	/**
      * @return
      */
     private Collection<JasperRootBean> createBeanCollection() {
 
         final JasperRootBean bean = new JasperRootBean();
         
         List<JasperFunction> functions = new ArrayList<JasperFunction>();
         PlateResults plateResults = plateEditor.getPlateResults();
         for ( PlateFunction pf : toBeSaved.getPlateFunctions() ) {
             JasperFunction function = new JasperFunction();
             String name = pf.getName();
             function.setName( name );
             function.setValue( String.format( "%.2f", 
                                               plateResults.getValue(name) ) );
             functions.add( function );
         }
         bean.setFunctions( functions );
         
         List<List<Well>> values = ReplicateTableModel.groupWells( 
                                                         toBeSaved.getWells() );
         
         Map<String, JasperDiagram> diagrams 
             = new HashMap<String, JasperDiagram>();
         for ( List<Well> wells : values ) {
             
             double sum = 0;
             int numOf  = 0;
             double concentration = 0;
             
             for ( Well well : wells ) {
                 for ( WellFunction wf : well.getWellFunctions() ) {
                     if ( siPattern.matcher( wf.getName() ).matches() ) {
                         sum += plateResults.getValue( well.getCol(), 
                                                       well.getRow(), 
                                                       wf.getName() );
                         numOf++;
                         
                         break;
                     }
                 }
             }
             StringBuffer substanceNameBuilder = new StringBuffer();
             int i = 0;
             String unit = "??";
             for ( AbstractSample s : wells.get( 0 ).getSampleContainer()
                                                    .getSamples() ) {
 
                 if (s instanceof DrugSample) {
                     if ( i++ != 0 ) {
                         substanceNameBuilder.append( ',' ).append( ' ' );
                     }
                     substanceNameBuilder.append( ((DrugSample)s).getDrugOrigin()
                                                                 .getName() );
                     unit = ((DrugSample) s).getConcUnit().toString();
                     concentration = ((DrugSample) s).getConcentration();
                 }
             }
             String substanceName = substanceNameBuilder.toString();
             if ( substanceName.equals( "" ) ) {
                 continue;
             }
             JasperDiagram diagram = diagrams.get(substanceName);
             
             if ( diagram == null ) {
                 diagram = new JasperDiagram();
                 diagram.setIc50( ic50.getIC50(substanceName) );
                 diagram.setName( substanceName );
                 diagram.setUnit( unit );
                 diagrams.put( substanceName, diagram );
             }
             
             diagram.getPoints().add( 
                 new JasperPoint( concentration == 0 ? 1 : concentration, 
                                  sum/numOf ) );
             
         }
             
         bean.setDiagrams( new ArrayList<JasperDiagram>( diagrams.values() ) );
         
         return new ArrayList<JasperRootBean>() {
             private static final long serialVersionUID = 1L;
             { add( bean ); }
         };
     }
 
     /**
      * @return
      */
     private Map<String, String> createParamaters() {
 
         Map<String, String> parameters = new HashMap<String, String>();
         String basePath;
         try {
             basePath = FileUtil.getFilePath( 
                            "reports/", 
                            net.bioclipse.brunn.ui.Activator.PLUGIN_ID );
         }
         catch ( Exception e ) {
             throw new IllegalStateException("Failed to get file path", e);
         }
         parameters.put("DS_BASE_PATH",basePath);
         parameters.put( "NAME", toBeSaved.getName() );
         return parameters;
     }
 
     @Override
 	public boolean isSaveAsAllowed() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 }
