 package com.pace.base.migration;
 
 import java.util.List;
 import java.util.Map;
 
 import org.apache.log4j.Logger;
 
 import com.pace.base.PafBaseConstants;
 import com.pace.base.ViewPrintState;
 import com.pace.base.project.ProjectElementId;
 import com.pace.base.project.ProjectSaveException;
 import com.pace.base.project.XMLPaceProject;
 import com.pace.base.ui.DefaultPrintSettings;
 import com.pace.base.ui.PrintStyle;
 import com.pace.base.ui.PrintStyles;
 import com.pace.base.utility.GUIDUtil;
 import com.pace.base.view.PafView;
 
 public class PafViewPrintStylesMigrationAction extends MigrationAction {
 
 	private static Logger logger = Logger.getLogger(PafViewPrintStylesMigrationAction.class);
 	private int count = 0;
 	private Map<String, PrintStyle> printStylesMap = null;
	private PrintStyle defaultPrintStyle = DefaultPrintSettings.getInstance().getDefaultPrintSettings();
 	
 	public PafViewPrintStylesMigrationAction() {
 		// TODO Auto-generated constructor stub
 	}
 
 	public PafViewPrintStylesMigrationAction(XMLPaceProject xmlPaceProject) {
 		this.xmlPaceProject = xmlPaceProject;
 		if ( this.xmlPaceProject != null ) {
 			boolean currentUpgradeProject = this.xmlPaceProject.isUpgradeProject();
 			this.xmlPaceProject.setUpgradeProject(true);
 			this.printStylesMap = this.xmlPaceProject.getPrintStyles();
 			this.xmlPaceProject.setUpgradeProject(currentUpgradeProject);
 		}
 	}	
 	
 	@Override
 	public String getActionName() {
 		// TODO Auto-generated method stub
		return "Converting view's old print settings to com.pase.base.ui.PrintStyle.";
 	}
 
 	@Override
 	public MigrationActionStatus getStatus() {
 		// TODO Auto-generated method stub
 		MigrationActionStatus status = MigrationActionStatus.NotStarted;
 		List<PafView> pafViews = xmlPaceProject.getViews();
 		if( pafViews.size() == 0 )
 			status = MigrationActionStatus.Failed;
 		for( PafView pafView : pafViews ) {
 			if( pafView.getViewPrintState() != null && pafView.getPrintStyle() != null 
 				&& pafView.getPageOrientation() == null && pafView.getPagesTall() == null && pafView.getPagesWide() == null ) {
 				status = MigrationActionStatus.Completed;
 			}
 			else if( pafView.getViewPrintState() == null && pafView.getPrintStyle() == null ) {
 				status = MigrationActionStatus.NotStarted;
 			}
 		}
 		return status;
 	}
 
 	@Override
 	public void run() {
 		// TODO Auto-generated method stub
 		//1. get views
 		//2. get default print settings
 		//3. construct the global style used by all those views by copying the 3 print settings in addition to the default settings.
 		//4. set old print setting fields to null
 		//5. assign the global styles to those views
 		//6. add the global print style to the global print style collection 
 		boolean currentUpgradeProject = this.xmlPaceProject.isUpgradeProject();
 		xmlPaceProject.setUpgradeProject(false);
 		List<PafView> pafViews = xmlPaceProject.getViews();
 		xmlPaceProject.setUpgradeProject(currentUpgradeProject);
 		
 		for( PafView pafView : pafViews ) {
 			constructMigrationGlobalPrintStyleForView( pafView );
 		}
 		
 		xmlPaceProject.setViews(pafViews);
 		xmlPaceProject.setPrintStyles(printStylesMap);
 		try {
 			xmlPaceProject.save(ProjectElementId.Views);
 			xmlPaceProject.save(ProjectElementId.PrintStyles);
 		} catch (ProjectSaveException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 	}
 	
 	public void constructMigrationGlobalPrintStyleForView( PafView view ) {
		PrintStyle migratePrintStyle = defaultPrintStyle.clone();
 		if( view.getPageOrientation() == null && view.getPagesTall() == null && view.getPagesWide() == null ) {
 			if( view.getPrintStyle() == null && view.getViewPrintState() == null ) {
 				migratePrintStyle.setGUID(GUIDUtil.getGUID());
 				migratePrintStyle.setDefaultStyle(false);
				migratePrintStyle.setName(PafBaseConstants.EMBEDED_PRINT_SETTINGS);
 				view.setGlobalPrintStyleGUID(null);
 				view.setPrintStyle(migratePrintStyle);
 				view.setViewPrintState(ViewPrintState.LOCAL);
 			}
 		}
 		else {
 			if ( view.getPageOrientation() != null ) { 
 				if( view.getPageOrientation().equalsIgnoreCase(PafBaseConstants.PRINTSTYLE_PAGE_ORIENTATION_PORTRAIT) ) {
 					migratePrintStyle.setPortrait(true);
 					migratePrintStyle.setLandscape(false);
 				}
 				else if( view.getPageOrientation().equalsIgnoreCase(PafBaseConstants.PRINTSTYLE_PAGE_ORIENTATION_LANDSCAPE) ) {
 					migratePrintStyle.setLandscape(true);
 					migratePrintStyle.setPortrait(false);
 				}
 			}
 			if( view.getPagesTall() != null ) {
 				migratePrintStyle.setPageTall(view.getPagesTall());
 			}
 			if( view.getPagesWide() != null ) {
 				migratePrintStyle.setPageWide(view.getPagesWide());
 			}
 			boolean found = false;
 			for( String key : printStylesMap.keySet() ) {
 				PrintStyle ps = (PrintStyle)printStylesMap.get(key);
 				if( migratePrintStyle.equals(ps) ) {
 					found = true;
 					if( ps.getDefaultStyle() ) {
 						view.setGlobalPrintStyleGUID(null);
 						view.setViewPrintState(ViewPrintState.DEFAULT);
 					}
 					else {
 						view.setGlobalPrintStyleGUID(ps.getGUID());
 						view.setViewPrintState(ViewPrintState.GLOBAL);
 					}
 					view.setPrintStyle(ps);
 					break;
 				}
 			}
 			if( ! found ) {
 				String guid = GUIDUtil.getGUID();
 				migratePrintStyle.setGUID(guid);
 				migratePrintStyle.setDefaultStyle(false);
 				migratePrintStyle.setName("Migration Print Style #" + ++count);
 				printStylesMap.put(guid, migratePrintStyle);
				view.setViewPrintState(ViewPrintState.GLOBAL);
 				view.setGlobalPrintStyleGUID(guid);
 				view.setPrintStyle(migratePrintStyle);
 			}
 			view.setPageOrientation(null);
 			view.setPagesTall(null);
 			view.setPagesWide(null);
 		}
 	}
 }
