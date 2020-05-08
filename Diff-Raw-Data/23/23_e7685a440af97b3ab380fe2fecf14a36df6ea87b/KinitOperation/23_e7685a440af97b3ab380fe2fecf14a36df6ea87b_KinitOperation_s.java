 package net.brainvitamins.kerberos;
 
 /*
  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.
 
  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 import java.io.File;
 import java.io.IOException;
 
 import android.os.Handler;
 import android.util.Log;
 import edu.mit.kerberos.KerberosOperations;
 
 public class KinitOperation extends KerberosOperation {
 
 	public static final String LOG_TAG = "KinitOperation";
 
 	public static KinitOperationNativeWrapper wrapper;
 
 	public synchronized static void execute(String principalName,
 			File configurationFile, Handler messageHandler) {
 		execute(principalName, Utilities.getDefaultCredentialsCache(),
 				configurationFile, messageHandler);
 	}
 
 	public synchronized static void execute(String principalName,
 			final CredentialsCacheFile credentialsCache,
 			final File configurationFile, final Handler messageHandler) {
 
		// TODO: more validation (null checks, configuration file existence,
		// etc)
 		if (!credentialsCache.getParentFile().exists()) {
 			log(messageHandler,
 					"ERROR: Credentials cache directory does not exist.\n");
 			messageHandler.sendEmptyMessage(FAILURE_MESSAGE);
 			return;
 		}
 
 		final String kinitArguments = "-V " + principalName;
 
 		operation = new Thread() {
 			public void run() {
 				if (operationLock.tryLock()) {
 					try {
 						wrapper = new KinitOperationNativeWrapper(
 								messageHandler);
 						wrapper.nativeSetEnv("KRB5_CONFIG", configurationFile
 								.getCanonicalPath().toString());
 						wrapper.nativeSetEnv("KRB5CCNAME", credentialsCache
 								.getCanonicalPath().toString());
 
 						Log.d(LOG_TAG, "Going native...");
 						int authenticationResult = wrapper.nativeKinit(
 								kinitArguments,
 								KerberosOperations.countWords(kinitArguments));
 
 						Log.d(LOG_TAG, "Native return code: "
 								+ authenticationResult);
 						if (authenticationResult == SUCCESS_MESSAGE
 								|| authenticationResult == CANCEL_MESSAGE) {
 							wrapper.messageHandler
 									.sendEmptyMessage(authenticationResult);
 						} else {
 							wrapper.messageHandler
 									.sendEmptyMessage(FAILURE_MESSAGE);
 						}
 
 					} catch (Error e) {
 						wrapper.log("ERROR: " + e.getMessage());
 						wrapper.messageHandler
 								.sendEmptyMessage(FAILURE_MESSAGE);
 					} catch (IOException io) {
 						wrapper.log("ERROR: " + io.getMessage());
 						wrapper.messageHandler
 								.sendEmptyMessage(FAILURE_MESSAGE);
 					} finally {
 						// we'll maintain a reference to this thread object
 						// until the next operation...
 						operationLock.unlock();
 					}
 				} else {
 					Log.e(LOG_TAG,
 							"Attempted to launch multiple concurrent kinit calls.");
 				}
 			}
 		};
 
 		operation.start();
 	}
 
 	public synchronized static void cancel() {
 		if (operation != null) {
 			operation.interrupt();
 		} else {
 			Log.d(LOG_TAG, "Cancel attempt with no running operations.");
 		}
 	}
 	// public static void execute(String principalName, KeytabFile keytab,
 	// File configurationFile, Handler logMessageHandler) {
 	// execute(principalName, keytab, Utilities.getDefaultCredentialsCache(),
 	// configurationFile, logMessageHandler);
 	// }
 	//
 	// public static void execute(String principalName, KeytabFile keytab,
 	// CredentialsCacheFile credentialsCache, File configurationFile,
 	// final Handler messageHandler) {
 	// // so much copy pasta...
 	// if (!credentialsCache.getParentFile().exists()) {
 	// logMessage(messageHandler,
 	// "ERROR: Credentials cache directory does not exist.\n");
 	// messageHandler.sendEmptyMessage(AUTHENTICATION_FAILURE_MESSAGE);
 	// return;
 	// }
 	//
 	// final String kinitArguments = "-V -k -t " + keytab.getAbsolutePath()
 	// + " " + principalName;
 	//
 	// KinitOperation operation = new KinitOperation(new Thread() {
 	// public void run() {
 	// launchNativeKinit(kinitArguments, messageHandler);
 	// }
 	//
 	// }, messageHandler);
 	//
 	// try {
 	// operation.nativeSetEnv("KRB5_CONFIG", configurationFile
 	// .getCanonicalPath().toString(), messageHandler);
 	// operation.nativeSetEnv("KRB5CCNAME", credentialsCache
 	// .getCanonicalPath().toString(), messageHandler);
 	//
 	// } catch (Error e) {
 	// operation.logMessage("ERROR: " + e.getMessage());
 	// operation.messageHandler
 	// .sendEmptyMessage(AUTHENTICATION_FAILURE_MESSAGE);
 	// } catch (IOException io) {
 	// operation.logMessage("ERROR: " + io.getMessage());
 	// operation.messageHandler
 	// .sendEmptyMessage(AUTHENTICATION_FAILURE_MESSAGE);
 	// }
 	//
 	// operation.operation.start();
 	// }
 
 	// private void launchNativeKinit(final String kinitArguments,
 	// KinitOperationNativeWrapper wrapper) {
 	// if (operationLock.tryLock()) {
 	// Log.d(LOG_TAG, "Going native...");
 	// try {
 	// int authenticationResult = wrapper.nativeKinit(kinitArguments,
 	// KerberosOperations.countWords(kinitArguments));
 	//
 	// if (authenticationResult == 0) {
 	// wrapper.messageHandler
 	// .sendEmptyMessage(AUTHENTICATION_SUCCESS_MESSAGE);
 	// } else {
 	// wrapper.messageHandler
 	// .sendEmptyMessage(AUTHENTICATION_FAILURE_MESSAGE);
 	// }
 	// operationLock.unlock();
 	// } catch (Error e) {
 	// Log.i(LOG_TAG, e.getMessage());
 	// operationLock.unlock();
 	// return;
 	// }
 	// } else {
 	// Log.e(LOG_TAG,
 	// "Attempted to launch multiple concurrent kinit calls.");
 	// }
 	// }
 }
