 package net.java.dev.cejug.classifieds.login.resource;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.security.GeneralSecurityException;
 import java.util.Map;
 
 import javax.ejb.EJB;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import net.java.dev.cejug.classifieds.login.entity.UserEntity;
 import net.java.dev.cejug.classifieds.login.entity.facade.client.RegistrationConstants;
 import net.java.dev.cejug.classifieds.login.entity.facade.client.URLDeobfuscator;
 import net.java.dev.cejug.classifieds.login.entity.facade.client.UserFacadeLocal;
 
 public class RegistrationConfirmation extends HttpServlet {
 	/** <code>serialVersionUID = {@value}</code>. */
 	private final static long serialVersionUID = -6026937020915831338L;
 
 	@EJB
 	private UserFacadeLocal userFacade;
 
 	@EJB
 	private URLDeobfuscator deobfuscator;
 
 	/** {@inheritDoc} */
 	@Override
 	protected void doGet(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 
 		String key = request
 				.getParameter(RegistrationConstants.CONFIRMATION_KEY.value());
 		if (key == null) {
 			response.sendError(HttpServletResponse.SC_BAD_REQUEST,
 					"invalid key - goodbye hacker...");
 		} else {
 			try {
 				Map<String, String> parameters = deobfuscator
 						.extractParameters(key);
 				userFacade.activate(parameters.get(UserEntity.SQL.PARAM_LOGIN),
 						parameters.get(UserEntity.SQL.PARAM_EMAIL));
 				PrintWriter out = response.getWriter();
 				out.print("<hr/>");
 				out
 						.print("<p>done... your account was successfully activated.</p><p><strong>TODO</todo>: To redirect the user to the login page...</p>");
 			} catch (GeneralSecurityException e) {
 				response.sendError(HttpServletResponse.SC_BAD_REQUEST, e
 						.getMessage());
 			}
 		}
 	}
 
 	/** {@inheritDoc} */
 	@Override
 	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 		resp.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);
 		super.doPost(req, resp);
 	}
 }
