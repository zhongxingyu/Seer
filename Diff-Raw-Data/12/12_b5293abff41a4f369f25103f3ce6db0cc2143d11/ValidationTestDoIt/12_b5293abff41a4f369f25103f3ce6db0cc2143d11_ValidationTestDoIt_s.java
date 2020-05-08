 /*
  * Created on Feb 27, 2004
  *
  * To change the template for this generated file go to
  * Window - Preferences - Java - Code Generation - Code and Comments
  */
 package org.vfny.geoserver.action.validation;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 import javax.servlet.ServletContext;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 import org.geotools.data.DataStore;
 import org.geotools.data.FeatureSource;
 import org.geotools.validation.ValidationProcessor;
 import org.geotools.validation.ValidationResults;
 import org.vfny.geoserver.action.ConfigAction;
 import org.vfny.geoserver.config.DataConfig;
 import org.vfny.geoserver.config.DataStoreConfig;
 import org.vfny.geoserver.config.validation.TestSuiteConfig;
 import org.vfny.geoserver.config.validation.ValidationConfig;
 import org.vfny.geoserver.global.GeoValidator;
 import org.vfny.geoserver.global.UserContainer;
 
 import com.vividsolutions.jts.geom.Envelope;
 
 /**
  * ValidationTestDoIt purpose.
  * <p>
  * Description of ValidationTestDoIt ...
  * </p>
  * 
  * @author dzwiers, Refractions Research, Inc.
 * @author $Author: emperorkefka $ (last modification)
 * @version $Id: ValidationTestDoIt.java,v 1.6 2004/04/22 08:22:52 emperorkefka Exp $
  */
 public class ValidationTestDoIt extends ConfigAction {
 	public ActionForward execute(ActionMapping mapping,
             ActionForm incomingForm, UserContainer user, HttpServletRequest request,
             HttpServletResponse response) {
 
         ServletContext context = this.getServlet().getServletContext();
         ValidationConfig validationConfig = (ValidationConfig) context.getAttribute(ValidationConfig.CONFIG_KEY);
 		TestSuiteConfig suiteConfig = (TestSuiteConfig) request.getSession().getAttribute(TestSuiteConfig.CURRENTLY_SELECTED_KEY);
 		Map plugins = new HashMap();Map ts = new HashMap();
 		validationConfig.toDTO(plugins,ts); // return by ref.
 		
 		GeoValidator gv = new GeoValidator(ts,plugins);
         DataConfig dataConfig = (DataConfig) getDataConfig();
 		Map dataStores = dataConfig.getDataStores();
 		TestValidationResults vr = runTransactions(dataStores,gv,context);
 		
 		request.getSession().setAttribute(TestValidationResults.CURRENTLY_SELECTED_KEY,vr);
 		return  mapping.findForward("config.validation.suite");
 	}
 
     private TestValidationResults runTransactions(Map dsm, ValidationProcessor v, ServletContext sc) {
         if ((dsm == null) || (dsm.size() == 0)) {
             System.out.println("No Datastores were defined.");
 
             return null;
         }
 
         if (v == null) {
             System.err.println(
                 "An error occured: Cannot run without a ValidationProcessor.");
 
             return null;
         }
 
         TestValidationResults vr = new TestValidationResults();
         Iterator i = dsm.keySet().iterator();
 
         while (i.hasNext()) {
             Map sources = new HashMap();
             String key = i.next().toString();
             DataStoreConfig dsc = (DataStoreConfig) dsm.get(key);
             try {
             	DataStore ds = dsc.findDataStore(sc);
            
             	String[] ss = ds.getTypeNames();
             	for (int j = 0; j < ss.length; j++) {
                     FeatureSource fs = ds.getFeatureSource(ss[j]);
                    sources.put(ss, fs);
 
                     v.runFeatureTests(dsc.getId(),fs.getSchema(),
                         fs.getFeatures().collection(), (ValidationResults) vr);
                     System.out.println("Feature Test Results for " + key + ":"
                         + ss[j]);
                     System.out.println(vr.toString());
                 } 
             }catch (Exception e) {
                e.printStackTrace();
             }
 
             Envelope env = new Envelope(Integer.MIN_VALUE, Integer.MIN_VALUE,
                         Integer.MAX_VALUE, Integer.MAX_VALUE);
 
             try {
                 v.runIntegrityTests(sources.keySet(), sources, env, (ValidationResults) vr);
                 System.out.println("Feature Integrety Test Results");
                 System.out.println(vr.toString());
             } catch (Exception e) {
                 e.printStackTrace();
             }
         }
         return vr;
     }
 }
