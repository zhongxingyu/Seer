 /*
  * Copyright (c) 2010 Henrik Gustafsson <henrik.gustafsson@fnord.se>
  *
  * Permission to use, copy, modify, and distribute this software for any
  * purpose with or without fee is hereby granted, provided that the above
  * copyright notice and this permission notice appear in all copies.
  *
  * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
  * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
  * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
  * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
  * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
  * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
  * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
  */
 package se.fnord.rt.ui;
 
 import java.net.MalformedURLException;
 import java.net.URISyntaxException;
 import java.net.URL;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.mylyn.commons.net.AuthenticationCredentials;
 import org.eclipse.mylyn.commons.net.AuthenticationType;
 import org.eclipse.mylyn.internal.tasks.core.IRepositoryConstants;
 import org.eclipse.mylyn.tasks.core.TaskRepository;
 import org.eclipse.mylyn.tasks.ui.wizards.AbstractRepositorySettingsPage;
 import org.eclipse.mylyn.tasks.ui.wizards.ITaskRepositoryPage;
 import org.eclipse.swt.widgets.Composite;
 
 import se.fnord.rt.client.RTAPI;
 import se.fnord.rt.client.RTAPIFactory;
 import se.fnord.rt.client.RTException;
 import se.fnord.rt.core.RequestTrackerRepositoryConnector;
 
 @SuppressWarnings("restriction")
 public class RequestTrackerTaskRepositoryPage extends AbstractRepositorySettingsPage implements ITaskRepositoryPage {
     private static final String MSG_SETTINGS_TITLE = "Request Tracker Repository Settings";
 
     public RequestTrackerTaskRepositoryPage(TaskRepository repository) {
         super(MSG_SETTINGS_TITLE, "", repository);
         setNeedsValidation(true);
         setNeedsAnonymousLogin(false);
         setNeedsAdvanced(false);
         setNeedsProxy(false);
         setNeedsEncoding(false);
     }
 
     @Override
     public String getConnectorKind() {
         return RequestTrackerRepositoryConnector.REPOSITORY_CONNECTOR_KIND;
     }
 
     @Override
     public void applyTo(TaskRepository repository) {
         super.applyTo(repository);
         repository.setProperty(RequestTrackerRepositoryConnector.REPOSITORY_PROPERTY_QUEUE_PREFIX + "0", "PSMBugs");
         repository.setProperty(RequestTrackerRepositoryConnector.REPOSITORY_PROPERTY_QUEUE_PREFIX + "1", "PSMFeatures");
         repository.setProperty(IRepositoryConstants.PROPERTY_CATEGORY, IRepositoryConstants.CATEGORY_BUGS);
     }
 
     public boolean checkServerURI(String server) {
         if (server == null || server.length() == 0) {
             setErrorMessage("Enter a server address.");
             return false;
         }
         try {
             URL url = new URL(server);
             url.toURI();
         } catch (MalformedURLException e) {
             setErrorMessage("The provided server address is not a valid Request Tracker URI.");
             return false;
         } catch (URISyntaxException e) {
             setErrorMessage("The provided server address is not a valid Request Tracker URI.");
             return false;
         }
         setErrorMessage(null);
         return true;
     }
 
     @Override
     protected void createAdditionalControls(Composite parent) {
     }
 
     @Override
     protected boolean isValidUrl(String url) {
         return checkServerURI(url);
     }
 
     @Override
     protected Validator getValidator(final TaskRepository repository) {
         return new Validator() {
             @Override
             public void run(IProgressMonitor monitor) throws CoreException {
                 try {
                     AuthenticationCredentials credentials = repository.getCredentials(AuthenticationType.REPOSITORY);
                     RTAPI rtClient = new RTAPIFactory().getClient(repository.getRepositoryUrl(), credentials.getUserName(), credentials.getPassword());
                     rtClient.getUser(repository.getUserName());
                } catch (RTException e) {
                    if (e.getCode() == 401)
                        setStatus(new Status(IStatus.ERROR, RequestTrackerUIPlugin.PLUGIN_ID, INVALID_LOGIN));
                    else
                        setStatus(new Status(IStatus.ERROR, RequestTrackerUIPlugin.PLUGIN_ID, e.getMessage()));
                 } catch (Exception e) {
                    setStatus(new Status(IStatus.ERROR, RequestTrackerUIPlugin.PLUGIN_ID, e.getMessage()));
                 }
             }
         };
     }
 }
