 package com.proyecto.bav.listeners;
 
 import com.octo.android.robospice.exception.NoNetworkException;
 import com.octo.android.robospice.persistence.exception.SpiceException;
 import com.octo.android.robospice.request.listener.RequestListener;
 import com.proyecto.bav.DatosPersonalesActivity;
 import com.proyecto.bav.models.Dialog;
 import com.proyecto.bav.results.UsuarioResult;
 
 public class UsuarioRequestListener implements RequestListener<UsuarioResult> {
 
 	private DatosPersonalesActivity activity;
 	
 	public UsuarioRequestListener(DatosPersonalesActivity datosPersonalesActivity) {
 		this.activity = datosPersonalesActivity;
 	}
 
 	@Override
 	public void onRequestFailure(SpiceException spiceException) {
 		
 		activity.myProgressDialog.dismiss();
 		
 		if (spiceException instanceof NoNetworkException)
 			Dialog.showDialog(activity, false, true, "No hay conexin. Intente nuevamente");
 		else 
 			Dialog.showDialog(activity, false, true, "Ha ocurrido un error con la conexin. Intente nuevamente");
 		
 	}
 
 	@Override
 	public void onRequestSuccess(UsuarioResult result) {		
 		result.getUser().save(activity.getApplicationContext());		
 		activity.myProgressDialog.dismiss();
		activity.fetchDatosPersonales();
 		Dialog.showDialog(activity, false, true, "Datos Personales sincronizados exitosamente");
 	}
 
 }
