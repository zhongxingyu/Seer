 package eai.msejdf.jbpm.actions;
 
 
import java.math.BigDecimal;

 import org.jbpm.graph.def.ActionHandler;
 import org.jbpm.graph.exe.ExecutionContext;
 
 import eai.msejdf.esb.Company;
 import eai.msejdf.utils.SOAMessageConstants;
 
 public class ExtractCompanyName implements ActionHandler {
 
 	private static final long serialVersionUID = 1L;
 	
 	public void execute(ExecutionContext context) throws Exception {
 
 		Company company = (Company)context.getContextInstance().getVariable(SOAMessageConstants.JBPM_MSG_BODY);
		//Company company = null; //TODO: We should get this from body
 		
 		//TODO: Remove++ tmp for debug
 		if (null == company)
 		{
 			company = new Company();
 			company.setName("BES");
 		}
 		//TODO: Remove--
 		
 		context.getContextInstance().createVariable(SOAMessageConstants.JBPM_COMPANY_NAME, company.getName());
 	}
 
 }
