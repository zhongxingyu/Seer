 package de.hswt.hrm.place.ui.part;
 
 import java.net.URL;
 import java.util.Collection;
 
 import javax.annotation.PostConstruct;
 
 import org.eclipse.e4.xwt.IConstants;
 import org.eclipse.e4.xwt.XWT;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.viewers.ArrayContentProvider;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.swt.widgets.Composite;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import de.hswt.hrm.common.database.exception.DatabaseException;
 import de.hswt.hrm.place.model.Place;
 import de.hswt.hrm.place.service.PlaceService;
 import de.hswt.hrm.place.ui.filter.PlaceFilter;
 import de.hswt.hrm.place.ui.part.util.PlaceComparator;
 import de.hswt.hrm.place.ui.part.util.PlacePartUtil;
 
 
 public class PlacePart {
 	
 	private final static Logger LOG = LoggerFactory.getLogger(PlacePart.class);
 
 	private TableViewer viewer;
    private PlaceFilter filter;
    private PlaceComparator p;
 	private Collection<Place> places = null;
 
 	@PostConstruct
 	public void postConstruct(Composite parent) {
 		/**
 		 * URL url = ContactPart.class.getClassLoader().getResource(
                 "de/hswt/hrm/contact/ui/xwt/ContactView" + IConstants.XWT_EXTENSION_SUFFIX);
 		 */
		URL url = PlacePart.class.getResource(
 				"de/hswt/hrm/place/ui/xwt/PlaceView" + IConstants.XWT_EXTENSION_SUFFIX);
 		
 		try {
 			final Composite composite = (Composite) XWT.load(url);
 			viewer = (TableViewer) XWT.findElementByName(composite, "placeTable");
 			initializeTable(parent, viewer);
 			refreshTable(parent);
 			
 		}
 		catch (Exception e) {
 			LOG.error("Could not load XWT file from resource", e);
 		}
 	}
 	
 	private void refreshTable(Composite parent) {
 		try {
 			places = PlaceService.findAll();
 			viewer.setInput(places);
 		} 
 		catch (DatabaseException e) {
 			LOG.error("Unable to retrieve list of places.", e);
 			
 			// TODO: übersetzen
 			MessageDialog.openError(parent.getShell(), "Connection Error",
 					"Could not load places from Database.");
 		}
 	}
 	
 	private void initializeTable(Composite parent, TableViewer viewer) {
 		PlacePartUtil.createColumns(parent, viewer, PlacePartUtil.getDefaultColumnHeaders(), p);
         viewer.setContentProvider(ArrayContentProvider.getInstance());
         viewer.addFilter(filter);
         viewer.setComparator(p);
 	}
 
 }
