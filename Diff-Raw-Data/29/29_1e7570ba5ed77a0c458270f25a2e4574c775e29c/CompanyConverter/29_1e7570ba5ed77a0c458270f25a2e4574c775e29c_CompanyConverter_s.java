 package presentationLayer;
 
 import javax.ejb.EJB;
 import javax.enterprise.context.RequestScoped;
 import javax.faces.component.UIComponent;
 import javax.faces.context.FacesContext;
 import javax.faces.convert.Converter;
 import javax.inject.Named;
 
 import businessLayer.Company;
 import daoLayer.CompanyDaoBean;
 
 @Named
 @RequestScoped
 public class CompanyConverter implements Converter {
	
	@EJB private CompanyDaoBean companyDao;
 
 	public CompanyConverter() {}
 
 	@Override
	public Object getAsObject(FacesContext context, UIComponent component,
			String value) {
		return companyDao.getBySocialNumber(value);
 	}
 
 	@Override
	public String getAsString(FacesContext context, UIComponent component,
			Object value) {
		return ((Company) value).getSocialNumber();
 	}
 
 }
