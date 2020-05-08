 package net.bioclipse.brunn.ui.editors.masterPlateEditor;
 
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.List;
 
 import net.bioclipse.brunn.pojos.AuditLog;
 import net.bioclipse.brunn.pojos.AuditType;
 import net.bioclipse.brunn.pojos.DrugSample;
 import net.bioclipse.brunn.pojos.MasterPlate;
 import net.bioclipse.brunn.pojos.SampleContainer;
 import net.bioclipse.brunn.pojos.SampleMarker;
 import net.bioclipse.brunn.pojos.Well;
 import net.bioclipse.brunn.ui.editors.masterPlateEditor.model.JasperCell;
 import net.bioclipse.brunn.ui.editors.masterPlateEditor.model.SampleSetCreater;
 import net.bioclipse.core.business.BioclipseException;
 import net.bioclipse.core.util.FileUtil;
 import net.bioclipse.core.util.LogUtils;
 import net.bioclipse.jasper.editor.ReportEditor;
 
 import org.apache.log4j.Logger;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.part.MultiPageEditorPart;
 
 public class MasterPlateMultiPageEditor extends MultiPageEditorPart {
 
 	private MasterPlateEditor masterPlateEditor;
 	private ReportEditor masterPlateReport;
 	private MasterPlate toBeSaved;
     private static Logger logger 
         = Logger.getLogger(MasterPlateMultiPageEditor.class);
 	
 	public final static String ID 
 	    = "net.bioclipse.brunn.ui.editors.masterPlateEditor." +
 	    		"MasterPlateMultiPageEditor";
     public static final SimpleDateFormat dateFormatter = 
         new SimpleDateFormat("yyyy-MM-dd"); 
 	
 	@Override
 	protected void createPages() {
 		
 		net.bioclipse.brunn.ui.explorer.model.nonFolders.MasterPlate masterPlate =
 			(net.bioclipse.brunn.ui.explorer.model.nonFolders.MasterPlate)getEditorInput();
 		this.setPartName(masterPlate.getName()); 
 		
 		toBeSaved = ( (MasterPlate)masterPlate.getPOJO() ).deepCopy(); 
 		
 		masterPlateEditor = new MasterPlateEditor(toBeSaved);
 		
 		try {
 			int index = this.addPage((IEditorPart) masterPlateEditor, getEditorInput());
 			setPageText(index, "Overview");
 			this.setActivePage(index);
 		} 
 		catch (PartInitException e) {
 			e.printStackTrace();
 		}
 
 		masterPlateReport = new ReportEditor();
 
 		try {
 			int index = this.addPage((IEditorPart) masterPlateReport, getEditorInput());
 			setPageText(index, "Report");
 		} 
 		catch (PartInitException e) {
 			e.printStackTrace();			
 		}
 	}
 
 	@Override
 	public void doSave(IProgressMonitor monitor) {
 		masterPlateEditor.doSave(monitor);
 	}
 
 	@Override
 	public void doSaveAs() {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	@Override
 	protected void pageChange(int newPageIndex){
 	    try {
 	        if(newPageIndex == 1) {
 	            String reportPath = null;
 	            int cols = masterPlateEditor.getCurrentMasterPlate().getCols();
 	            int rows = masterPlateEditor.getCurrentMasterPlate().getRows();
 	            if ( cols == 24 && rows == 16 ) {
 	                reportPath 
 	                    = FileUtil.getFilePath( 
 	                          "reports/masterplate384.jasper", 
                               net.bioclipse.brunn.ui.Activator.PLUGIN_ID );
 	            }
 	            else if ( cols == 12  && rows == 8 ) {
 	                reportPath
 	                    = FileUtil.getFilePath( 
 	                          "reports/masterplate96.jasper",
 	                          net.bioclipse.brunn.ui.Activator.PLUGIN_ID );
 	            }
 	            else {
 	                LogUtils.handleException( 
 	                    new IllegalStateException( 
 	                            "Report generation for plates with " + cols + 
 	                            " cols and " + rows + 
 	                            " rows is not supported." ), 
 	                    logger, 
 	                    "net.bioclipse.brunn.ui" );
 	            }
 	            
 	            String basePath 
 	                = FileUtil.getFilePath( 
 	                      "reports/", 
 	                      net.bioclipse.brunn.ui.Activator.PLUGIN_ID );
 	            HashMap<String, String> parameters 
 	                = getParameters();
 	            parameters.put("DS_BASE_PATH",basePath);
 
 	            masterPlateReport.openReport( 
 	                                  reportPath, 
 	                                  parameters, 
 	                                  getCellsCollection() );
 	        }
 	    }
         catch ( Exception e ) {
             LogUtils.handleException( e, logger , "net.bioclipse.brunn.ui" );
         }
 	}
 	
 	public HashMap<String, String> getParameters() {
 	    HashMap<String, String> params = new HashMap<String, String>();
 	    params.put( "MASTERPLATE_NAME", 
 	                masterPlateEditor.getCurrentMasterPlate().getName() );
 	    String creationDate = "[UNKNOWN DATE]";
 	    String creator = masterPlateEditor.getCurrentMasterPlate()
 	                                      .getCreator().getName();
 	    String changeDate = "[UNKNOWN DATE]";
 	    String changer = "[UNKNOWN]";
 	    
 	    List<AuditLog> changes = new ArrayList<AuditLog>();
 	    
 	    for ( AuditLog log : masterPlateEditor.getCurrentMasterPlate()
 	                                          .getAuditLogs() ) {
 	        if ( log.getAuditType() == AuditType.CREATE_EVENT ) {
 	            creationDate = dateFormatter.format( log.getTimeStamp() );
 	        }
 	        if ( log.getAuditType() == AuditType.UPDATE_EVENT ) {
 	            changes.add( log );
 	        }
 	    }
 	    Collections.sort( changes, new Comparator<AuditLog>() {
             @Override
             public int compare( AuditLog l1, AuditLog l2 ) {
                 return l1.getTimeStamp().compareTo( l2.getTimeStamp() );
             }
 	    });
 	    
 	    if ( changes.size() > 0 ) {
 	        changer = changes.get( 0 ).getUser().getName();
 	        changeDate = dateFormatter.format( changes.get( 0 )
 	                                                  .getTimeStamp() );
 	    }
 	    
 	    params.put( "CREATION_DATE", creationDate );
 	    params.put( "CREATOR",       creator      );
 	    params.put( "CHANGE_DATE",   changeDate   );
 	    params.put( "CHANGER",       changer      );
         
 	    return params;
     }
 
     @SuppressWarnings({ "rawtypes" })
     public Collection<JasperCell> getCellsCollection() {
         List<Well> wells = new ArrayList<Well>( 
                                    masterPlateEditor.getCurrentMasterPlate()
                                                     .getWells() );
         Collections.sort( wells, new Comparator<Well>() {
 
             @Override
             public int compare( Well w1, Well w2 ) {
 
                 if ( w1.getCol() != w2.getCol() ) {
                     return w1.getCol() - w2.getCol();
                 }
                 return w1.getRow() - w2.getRow();
             }
             
         });
         Collection<JasperCell> result = new ArrayList<JasperCell>();
         for ( char row = 'A' ; 
               row < 'A' + masterPlateEditor.getCurrentMasterPlate().getRows() ;
               row++ ) {
             JasperCell c = new JasperCell();
             c.setCol( "0" );
             c.setConcentrations( row + "" );
             c.setSubstances( "" );
             c.setUnits( "" );
             result.add( c );
         }
         int maxLength = 0, newLength = 0;
           
         switch ( wells.size() ) {
             case 384 : 
                 maxLength = 9;
                 newLength = 7;
                 break;
             case 96 :
                 maxLength = 14;
                 newLength = 12;
         }        
         for ( Well w : wells ) {
             JasperCell c = new JasperCell();
             c.setCol( w.getCol() + "" );
             StringBuilder substances     = new StringBuilder();
             StringBuilder concentrations = new StringBuilder();
             StringBuilder units          = new StringBuilder();
             StringBuilder markers        = new StringBuilder();
             int i = 0, 
                 j = 0;
             for ( SampleMarker sm : w.getSampleMarkers() ) {
                 markers.append( sm.getName() );
                 if ( sm.getSample() instanceof DrugSample ) {
                     substances.append( 
                         sm.getSample().getName() );
                     concentrations.append( 
                         ((DrugSample)sm.getSample()).getConcentration() );
                     units.append( 
                         ((DrugSample)sm.getSample()).getConcUnit().toString() );
                     
                     if ( i++ > 0 ) {
                         substances.    append( ',' ).append( ' ' );
                         concentrations.append( ',' ).append( ' ' );
                         units.         append( ',' ).append( ' ' );
                     }
                 }
                 if ( j++ > 0 ) {
                     markers.append( ',' ).append( ' ' );
                 }
             }
             c.setSubstances( substances.length() > maxLength 
                                  ? substances.substring( 0, newLength ) + "..."
                                  : substances.toString() );
             c.setConcentrations( concentrations.length() > maxLength 
                                      ? concentrations.substring( 
                                                           0, 
                                                           newLength ) + "..."
                                      : concentrations.toString() );
             c.setUnits( units.length() > maxLength 
                             ? substances.substring( 0, newLength ) + "..."
                             : units.toString() );
             if ( "".equals( c.getSubstances() ) ) {
                 c.setConcentrations( markers.toString() );
             }
             result.add( c );
         }
         return result;
     }
 
 	@Override
 	public boolean isSaveAsAllowed() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 	
 //	public String[][] getSubstanceNames() {
 //		return masterPlateEditor.getSubstanceNames();
 //	}
 	
 //	public String[][] getMasterPlateLayout() {
 //		return masterPlateEditor.getMasterPlateLayout();
 //	}
 }
