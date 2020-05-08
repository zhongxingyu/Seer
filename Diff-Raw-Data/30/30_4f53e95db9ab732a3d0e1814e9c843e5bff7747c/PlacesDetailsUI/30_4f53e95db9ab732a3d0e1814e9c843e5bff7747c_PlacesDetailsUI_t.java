 package il.ac.tau.team3.uiutils;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 
 import il.ac.tau.team3.common.GeneralPlace;
 import il.ac.tau.team3.shareaprayer.FindPrayer;
 import il.ac.tau.team3.shareaprayer.PlaceArrayItemizedOverlay;
 import il.ac.tau.team3.shareaprayer.ServiceConnector;
 import il.ac.tau.team3.shareaprayer.ServiceNotConnected;
 import il.ac.tau.team3.shareaprayer.UserNotFoundException;
 import il.ac.tau.team3.spcomm.ACommHandler;
 import il.ac.tau.team3.spcomm.ICommHandler;
 import il.ac.tau.team3.spcomm.SPComm;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
 import android.content.DialogInterface;
import android.os.Build;
 
 public class PlacesDetailsUI {
 	public enum Actions	{
 		JOINER	{
 			public void excute(SPComm comm, long id, final PlacesDetailsUI placesDetails)	{
 
 					
 					comm.requestGetJoinerPlaces(id, new ACommHandler<GeneralPlace[]> (){
 						
 						public void onRecv(final GeneralPlace[] places) {
 							placesDetails.run(places);
 						}
 					});
 			}
 					
 		},
 		OWNER	{
 			public void excute(SPComm comm, long id, final PlacesDetailsUI placesDetails)	{
 
 				
 				comm.requestGetOwnerPlaces(id, new ACommHandler<GeneralPlace[]> (){
 					
 					public void onRecv(final GeneralPlace[] places) {
 						placesDetails.run(places);
 					}
 				});
 		}
 		};
 		
 		
 		public abstract void excute(SPComm comm, long id, PlacesDetailsUI placesDetails);
 	};
 	
 	private ProgressDialog progress;
 	private String message;
 	private PlaceArrayItemizedOverlay publicPlaces;
 	private Activity activity;
 	
 	public PlacesDetailsUI(final Activity activity, ServiceConnector svcGetter, SPComm comm, 
 			final String message, Actions action, final PlaceArrayItemizedOverlay publicPlaces)	{
 	
 		this.message = message;
 		this.publicPlaces = publicPlaces;
 		this.activity = activity;
 		
 		progress = ProgressDialog.show(activity, "", 
                 "Fetching from Server, Please wait...", true);
     	
     	try {
     		
     		action.excute(comm, svcGetter.getService().getUser().getId(), this);
     		
     	} catch (UserNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (ServiceNotConnected e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} finally	{
     		progress.dismiss();
     	}
 	}
 
 	public void run(final GeneralPlace[] places) {
 		// TODO Auto-generated method stub
 		progress.dismiss();
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setTitle(message);
 		String[] names = new String[places.length];
 		if(places.length == 0){
 			names = new String[1];
 			names[0]= "No places listed yet.";
			builder.setItems(names, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int item) {
					dialog.dismiss();
				}
			});
			
 		}else{
 			for (int i = 0; i < places.length; names[i]=places[i].toString(), i++);
			builder.setItems(names, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int item) {
					UIUtils.createRegisterDialog(places[item], publicPlaces);
					
				}
			});
 		}
		
 
 		AlertDialog alert = builder.create();
 		alert.show();
 
 	}
     
 }
