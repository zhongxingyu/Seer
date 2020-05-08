 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 package de.cismet.cids.custom.objectrenderer.wunda_blau;
 
 import net.sf.jasperreports.engine.JRDefaultScriptlet;
 
 import org.apache.log4j.Logger;
 
 import java.io.InputStream;
 
 import java.net.URL;
 
 import java.util.Map;
 
 import de.cismet.cids.custom.objecteditors.wunda_blau.VermessungRissEditor;
 
 import de.cismet.security.WebAccessManager;
 
 import de.cismet.security.exceptions.AccessMethodIsNotSupportedException;
 import de.cismet.security.exceptions.MissingArgumentException;
 import de.cismet.security.exceptions.NoHandlerForURLException;
 import de.cismet.security.exceptions.RequestFailedException;
 
 /**
  * DOCUMENT ME!
  *
  * @author   jweintraut
  * @version  $Revision$, $Date$
  */
 public class VermessungRissReportScriptlet extends JRDefaultScriptlet {
 
     //~ Static fields/initializers ---------------------------------------------
 
     protected static final Logger LOG = Logger.getLogger(VermessungRissReportScriptlet.class);
 
     //~ Methods ----------------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      *
      * @param   host        DOCUMENT ME!
      * @param   schluessel  DOCUMENT ME!
      * @param   gemarkung   DOCUMENT ME!
      * @param   flur        DOCUMENT ME!
      * @param   blatt       DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public static Boolean isImageAvailable(final String host,
             final String schluessel,
             final Integer gemarkung,
             final String flur,
             final String blatt) {
         final Map<URL, URL> validURLs = VermessungRissEditor.getCorrespondingURLs(
                 host,
                 gemarkung,
                 flur,
                 schluessel,
                 blatt);
 
         InputStream streamToReadFrom = null;
         for (final Map.Entry<URL, URL> urls : validURLs.entrySet()) {
             final URL url = urls.getKey();
 
             try {
                 streamToReadFrom = WebAccessManager.getInstance().doRequest(url);
                 break;
             } catch (final MissingArgumentException ex) {
                 LOG.warn("Could not read document from URL '" + url.toExternalForm() + "'. Skipping this url.", ex);
             } catch (final AccessMethodIsNotSupportedException ex) {
                 LOG.warn("Can't access document URL '" + url.toExternalForm()
                             + "' with default access method. Skipping this url.",
                     ex);
             } catch (final RequestFailedException ex) {
                 LOG.warn("Requesting document from URL '" + url.toExternalForm() + "' failed. Skipping this url.",
                     ex);
             } catch (final NoHandlerForURLException ex) {
                 LOG.warn("Can't handle URL '" + url.toExternalForm() + "'. Skipping this url.", ex);
             } catch (final Exception ex) {
                 LOG.warn("An exception occurred while opening URL '" + url.toExternalForm()
                             + "'. Skipping this url.",
                     ex);
             }
         }
 
         return streamToReadFrom != null;
     }
 }
