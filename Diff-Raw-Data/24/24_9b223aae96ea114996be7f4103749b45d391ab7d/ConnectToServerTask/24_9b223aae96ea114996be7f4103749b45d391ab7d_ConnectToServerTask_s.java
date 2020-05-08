 /* Copyright 2009 Sean Hodges <seanhodges@bluebottle.com>
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package tesla.app.task;
 
 import tesla.app.service.business.ICommandController;
 import tesla.app.service.business.IErrorHandler;
 import android.os.AsyncTask;
 import android.os.RemoteException;
 
 public class ConnectToServerTask extends AsyncTask<ICommandController, Boolean, Object> {
 
 	private OnConnectionListener listener = null;
	ICommandController commandService = null;
 	
 	// Error messages need to be passed back to main UI thread
 	private String errorTitle = null;
 	private String errorMessage = null;
 	
 	private IErrorHandler errorHandler = new IErrorHandler.Stub() {
 		public void onServiceError(String title, String message, boolean fatal) throws RemoteException {
 			// Pass the error data back to the main UI thread
 			errorTitle = title;
 			errorMessage = message;
 		}
 	};
 	
 	public interface OnConnectionListener {
 		void onConnectionCancelled();
 		void onConnectionComplete();
 		void onConnectionFailed(String title, String message);
 	}
 	
 	protected Object doInBackground(ICommandController... args) {
 		boolean success = true;
		commandService = args[0];
 		try {
			connect();
 		} catch (RemoteException e) {
 			e.printStackTrace();
 		}
 		return success;
 	}

	private boolean connect() throws RemoteException {
		boolean success = false;
		commandService.registerErrorHandler(errorHandler);
		success = commandService.connect();
		commandService.unregisterErrorHandler(errorHandler);
		return success;
	}
 	
 	protected void onPostExecute(Object result) {
 		// Pass error message back to UI thread if there is one
 		if (errorTitle != null && errorMessage != null) {
 			if (listener != null) listener.onConnectionFailed(errorTitle, errorMessage);
 		}
 		else {
 			if (listener != null) listener.onConnectionComplete();
 		}
 	}
 
 	public void registerConnectionListener(OnConnectionListener listener) {
 		this.listener = listener;
 	}
 }
