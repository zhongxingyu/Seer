 package is.idega.idegaweb.pheidippides.presentation;
 
 import is.idega.idegaweb.pheidippides.PheidippidesConstants;
 import is.idega.idegaweb.pheidippides.bean.PheidippidesBean;
 import is.idega.idegaweb.pheidippides.business.RegistrationStatus;
 import is.idega.idegaweb.pheidippides.dao.PheidippidesDao;
 import is.idega.idegaweb.pheidippides.output.ReceiptWriter;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.faces.context.FacesContext;
 
 import org.springframework.beans.factory.annotation.Autowired;
 
 import com.idega.facelets.ui.FaceletComponent;
 import com.idega.idegaweb.IWBundle;
 import com.idega.presentation.IWBaseComponent;
 import com.idega.presentation.IWContext;
 import com.idega.user.data.User;
 import com.idega.util.PresentationUtil;
 import com.idega.util.expression.ELUtil;
 
 public class ParticipantRegistrationsList extends IWBaseComponent {
 
 	@Autowired
 	private PheidippidesDao dao;
 	
 	private IWBundle iwb;
 
 	@Override
 	protected void initializeComponent(FacesContext context) {
 		IWContext iwc = IWContext.getIWContext(context);
 		if (iwc.isLoggedOn()) {
 			User user = iwc.getCurrentUser();
 			iwb = getBundle(context, getBundleIdentifier());
 			
 			List<RegistrationStatus> statuses = new ArrayList<RegistrationStatus>();
 			statuses.add(RegistrationStatus.OK);
 	
 			PresentationUtil.addStyleSheetToHeader(iwc, iwb.getVirtualPathWithFileNameString("style/pheidippides.css"));
 	
 			PheidippidesBean bean = getBeanInstance("pheidippidesBean");
 			bean.setLocale(iwc.getCurrentLocale());
 			bean.setRegistrations(getDao().getRegistrations(user.getUniqueId(), statuses));
 			bean.setDownloadWriter(ReceiptWriter.class);
 	
 			FaceletComponent facelet = (FaceletComponent) iwc.getApplication().createComponent(FaceletComponent.COMPONENT_TYPE);
 			facelet.setFaceletURI(iwb.getFaceletURI("participantRegistrationsList/view.xhtml"));
 			add(facelet);
 		}
 	}
 
 	private String getBundleIdentifier() {
 		return PheidippidesConstants.IW_BUNDLE_IDENTIFIER;
 	}
 	
 	private PheidippidesDao getDao() {
 		if (dao == null) {
 			ELUtil.getInstance().autowire(this);
 		}
 		
 		return dao;
 	}
 }
