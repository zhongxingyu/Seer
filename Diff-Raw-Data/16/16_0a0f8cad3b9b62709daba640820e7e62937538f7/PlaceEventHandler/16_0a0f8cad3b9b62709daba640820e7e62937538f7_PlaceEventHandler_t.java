 package de.hswt.hrm.place.ui.event;
 
 import org.eclipse.e4.xwt.XWT;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Text;
 
 import de.hswt.hrm.place.ui.filter.PlaceFilter;
 
 public class PlaceEventHandler {
 	 
 	public void onFocusOut(Event event) {
 		Text text = (Text) event.widget;
 		text.setText("Suche (Location,Area,City)");
 		TableViewer tf = (TableViewer) XWT.findElementByName(text, "placeTable");
 		tf.refresh();
 	}
 
 	public void onFocusIn(Event event) {
 		Text text = (Text) event.widget;
 		text.setText("");
 	}
 
 	public void onSelection(Event event) {
//		Button b = (Button) event.widget;
//		WizardDialog dialog = new WizardDialog(b.getShell(), new PlaceWizard());
//		dialog.open();
 	}
 
 	public void onKeyUp(Event event) {
 		Text searchText = (Text) event.widget;
 		TableViewer tf = (TableViewer) XWT.findElementByName(searchText, "placeTable");
 		PlaceFilter f = (PlaceFilter) tf.getFilters()[0];
 		f.setSearchString(searchText.getText());
 		tf.refresh();
 	}
	public void onFocusOut_1(Event event) {
	}
 }
