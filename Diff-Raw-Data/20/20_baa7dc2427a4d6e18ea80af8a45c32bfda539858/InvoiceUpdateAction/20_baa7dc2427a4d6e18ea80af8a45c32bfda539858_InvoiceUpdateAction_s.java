 package net.vexelon.myglob.actions;
 
 import java.util.List;
 import java.util.Map;
 
 import android.content.Context;
 import net.vexelon.mobileops.HttpClientException;
 import net.vexelon.mobileops.IClient;
 import net.vexelon.myglob.users.User;
 
 public class InvoiceUpdateAction extends BaseAction {
 	
 	public InvoiceUpdateAction(Context context, User user) {
 		super(context, user);
 	}
 
 	@Override
 	public ActionResult execute() throws ActionExecuteException {
 		ActionResult result = new ActionResult();
		IClient client = this.newClient();
		
 		clientLogin(client);
 		
 		try {
 			List<Map<String, String>> invoiceInfo = client.getInvoiceInfo();
 			result.setResult(invoiceInfo);
 		} catch (HttpClientException e) {
 			throw new ActionExecuteException(e);
 		} finally {
 			// Make sure we (attempt to) logout.
 			clientLogout(client);
 		}
 		
 		return result;
 	}
 
 }
