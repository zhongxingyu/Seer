 package de.uni.stuttgart.informatik.ToureNPlaner.UI.Overlays;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.drawable.Drawable;
 import android.location.Location;
 import android.location.LocationListener;
 import android.os.Bundle;
 import android.text.InputType;
 import android.view.HapticFeedbackConstants;
 import android.widget.EditText;
 import de.uni.stuttgart.informatik.ToureNPlaner.Data.Constraint;
 import de.uni.stuttgart.informatik.ToureNPlaner.Data.Edits.AddNodeEdit;
 import de.uni.stuttgart.informatik.ToureNPlaner.Data.Edits.Edit;
 import de.uni.stuttgart.informatik.ToureNPlaner.Data.Edits.UpdateNNSEdit;
 import de.uni.stuttgart.informatik.ToureNPlaner.Data.Node;
 import de.uni.stuttgart.informatik.ToureNPlaner.Net.Session;
 import de.uni.stuttgart.informatik.ToureNPlaner.R;
 import de.uni.stuttgart.informatik.ToureNPlaner.UI.Activities.EditNodeScreen;
 import de.uni.stuttgart.informatik.ToureNPlaner.UI.Activities.MapScreen;
 import org.mapsforge.android.maps.MapView;
 import org.mapsforge.android.maps.overlay.ItemizedOverlay;
 import org.mapsforge.android.maps.overlay.OverlayItem;
 import org.mapsforge.core.GeoPoint;
 
 import java.util.ArrayList;
 
 public class NodeOverlay extends ItemizedOverlay<OverlayItem> implements LocationListener, Session.Listener {
 	private ArrayList<OverlayItem> list = new ArrayList<OverlayItem>();
 
 	private final Context context;
 	private Session session;
 
 	private static final int GPS_RADIUS = 10;
 	private OverlayItem gpsMarker;
 	private String constraintValue;
 	private boolean useGps = false;
 
 	private GpsDrawable gpsDrawable;
 	private Drawable defaultDrawable;
 
 	public NodeOverlay(Context context, Session session, GeoPoint gpsPoint, Drawable defaultDrawable) {
 		// ColorDrawable is just a workaround until the icons are loaded
 		super(boundCenterBottom(defaultDrawable));
 		this.session = session;
 		this.context = context;
 		this.defaultDrawable = defaultDrawable;
 		setupGpsDrawable();
 
 		loadFromModel();
 		updateGpsMarker(gpsPoint);
 	}
 
 	private void setupGpsDrawable() {
 		Paint p = new Paint();
 		p.setAntiAlias(true);
 		p.setColor(Color.YELLOW);
 		p.setAlpha(128);
 		gpsDrawable = new GpsDrawable(p);
 		gpsDrawable.setBounds(-GPS_RADIUS, -GPS_RADIUS, GPS_RADIUS, GPS_RADIUS);
 	}
 
	synchronized public GeoPoint getGpsPosition() {
 		return gpsMarker == null ? null : gpsMarker.getPoint();
 	}
 
 	synchronized private void loadFromModel() {
 		list.clear();
 		for (int i = 0; i < session.getNodeModel().size(); i++) {
 			addMarkerToMap(session.getNodeModel().get(i));
 		}
 		updateIcons();
 	}
 
 	@Override
 	public boolean onLongPress(GeoPoint geoPoint, MapView mapView) {
 		String markerName = String.valueOf(session.getNodeModel().size() + 1);
 
 
 		final ArrayList<Constraint> cl = new ArrayList<Constraint>();
 		ArrayList<Constraint> tempcl = session.getSelectedAlgorithm().getPointConstraints();
 		for (int i = 0; i < session.getSelectedAlgorithm().getPointConstraints().size(); i++) {
 			cl.add(new Constraint(session.getSelectedAlgorithm().getPointConstraints().get(i)));
 		}
 
 		final Node node = new Node(markerName, geoPoint, cl);
 
 		mapView.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
 
 
 		// Important! Try to perform the NNS Search before, notifying any one else about the change.
 		// else through HTTP pipelining the NNS might be performed after the request!
 		((MapScreen) context).runOnUiThread(new Runnable() {
 			@Override
 			public void run() {
 				((MapScreen) context).performNNSearch(node);
 			}
 		});
 
 		((MapScreen) context).runOnUiThread(new Runnable() {
 			@Override
 			public void run() {
 				for (int i = 0; i < cl.size(); i++) {
 					ConstraintDialog(cl.get(i).getName(), cl.get(i).getMinimumValue(), cl.get(i).getMaximumValue(), i, cl.get(i).getType());
 				}
 				session.getNodeModel().getNodeVector().set(session.getNodeModel().size() - 1, session.getNodeModel().getNodeVector().get(session.getNodeModel().size() - 1));
 			}
 		});
 
 		Edit edit = new AddNodeEdit(session, node, AddNodeEdit.Position.END);
 		edit.perform();
 		return true;
 	}
 
 	@Override
 	public int size() {
 		return list.size() + (gpsMarker == null ? 0 : 1);
 	}
 
 	@Override
 	protected OverlayItem createItem(int i) {
 		if (i == list.size())
 			return gpsMarker;
 		else
 			return list.get(i);
 	}
 
 	@Override
 	public boolean onTap(int i) {
 
 		if (i == list.size()) {
 			final GeoPoint gpsPoint = this.gpsMarker.getPoint();
 			AlertDialog.Builder builder = new AlertDialog.Builder(context);
 			builder.setMessage("Wollen Sie ihren aktuellen Standort als Startpunkt setzen?")
 					.setCancelable(true)
 					.setPositiveButton("Ja", new DialogInterface.OnClickListener() {
 						public void onClick(DialogInterface dialog, int id) {
 							// transform the GpsMarker into a regular mapMarker on Position 0
 							Node gpsStartnode = new Node("gpsLocation", gpsPoint);
 							useGps = true;
 							Edit edit = new AddNodeEdit(session, gpsStartnode, AddNodeEdit.Position.BEGINNING);
 							edit.perform();
 						}
 					})
 					.setNegativeButton("Nein", new DialogInterface.OnClickListener() {
 						public void onClick(DialogInterface dialog, int id) {
 							dialog.cancel();
 						}
 					}).create().show();
 		} else {
 			Intent intent = new Intent(context, EditNodeScreen.class);
 			intent.putExtra("node", session.getNodeModel().get(i));
 			intent.putExtra(Session.IDENTIFIER, session);
 			intent.putExtra("index", i);
 			((Activity) context).startActivityForResult(intent, MapScreen.REQUEST_NODE);
 		}
 
 		return true;
 	}
 
 	public void addMarkerToMap(Node node) {
 		//	NodeDrawable dm = new NodeDrawable(nodeParameters, node.getName());
 		OverlayItem overlayitem = new OverlayItem(node.getGeoPoint(), null, null, defaultDrawable);
 		list.add(overlayitem);
 		requestRedraw();
 	}
 
	synchronized private void updateIcons() {
		if (!session.getSelectedAlgorithm().sourceIsTarget() && list.size() > 0) {
 			for (int i = 1; i < list.size() - 1; i++) {
 				Drawable d = boundCenterBottom(context.getResources().getDrawable(R.drawable.markericon));
 				list.get(i).setMarker(d);
 			}
 
 			Drawable dend = boundCenterBottom(context.getResources().getDrawable(R.drawable.markerendger));
 			list.get(list.size() - 1).setMarker(dend);
 			Drawable dstart = boundCenterBottom(context.getResources().getDrawable(R.drawable.markerstart));
 			list.get(0).setMarker(dstart);
 		}
 
 		requestRedraw();
 	}
 
 	private void updateGpsMarker(GeoPoint geoPoint) {
 		// If we have a valid point
 		if (geoPoint != null) {
 			// Then create a new Overlayitem or update the old one
 			if (gpsMarker == null)
 				gpsMarker = new OverlayItem(geoPoint, null, null, gpsDrawable);
 			else
 				gpsMarker.setPoint(geoPoint);
 
 		} else {
 			gpsMarker = null;
 		}
 		requestRedraw();
 	}
 
 	@Override
 	public void onLocationChanged(Location location) {
 		GeoPoint geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
		gpsMarker.setPoint(geoPoint);
 		if (useGps && !list.isEmpty()) {
 			Edit edit = new UpdateNNSEdit(session, session.getNodeModel().get(0), geoPoint);
 			edit.perform();
 		}
 	}
 
 	@Override
 	public void onStatusChanged(String s, int i, Bundle bundle) {
 	}
 
 	@Override
 	public void onProviderEnabled(String s) {
 	}
 
 	@Override
 	public void onProviderDisabled(String s) {
 		gpsMarker = null;
 	}
 
 
 	@Override
 	public void onChange(int change) {
 		if (0 < ((Session.MODEL_CHANGE | Session.NNS_CHANGE) & change))
 			loadFromModel();
 	}
 
 	public void ConstraintDialog(String title, final Object min, final Object max, final int constraintid, final String constraintType) {
 		AlertDialog.Builder builder = new AlertDialog.Builder(context);
 		builder.setMessage("enter a value: ");
 		builder.setCancelable(true);
 		builder.setTitle(title);
 		// Set an EditText view to get user input
 		final EditText input = new EditText(context);
 		String hintmessage = String.valueOf(min) + " .. " + String.valueOf(max);
 		input.setHint(hintmessage);
 		if (constraintType.equals("integer") || constraintType.equals("boolean")) {
 			input.setInputType(InputType.TYPE_CLASS_NUMBER);
 		}
 		if (constraintType.equals("float") || constraintType.equals("meter") || constraintType.equals("price")) {
 			input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
 
 		}
 		builder.setView(input);
 		builder.setPositiveButton("Ja", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int id) {
 				constraintValue = input.getEditableText().toString();
 				Boolean isNumeric = checkForDigits(constraintValue);
 				if (constraintValue.equals("") || constraintValue == null) {
 					constraintValue = String.valueOf(min);
 				}
 
 				if (isNumeric) {
 					session.getNodeModel().get(session.getNodeModel().size() - 1).getConstraintList().get(constraintid).setValue(constraintValue);
 				}
 			}
 		});
 		builder.setNegativeButton("Nein", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int id) {
 				dialog.cancel();
 			}
 		}).create().show();
 	}
 
 	public boolean checkForDigits(String str) {
 		char c;
 		for (int i = 0; i < str.length(); i++) {
 			c = str.charAt(i);
 
 			if (!Character.isDigit(c) && '.' != c)
 				return false;
 		}
 
 		return true;
 	}
 }
