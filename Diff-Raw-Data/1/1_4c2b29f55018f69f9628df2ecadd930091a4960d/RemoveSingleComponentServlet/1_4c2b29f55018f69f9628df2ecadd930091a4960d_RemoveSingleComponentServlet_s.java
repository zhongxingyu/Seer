 package ua.edu.ChaliyLukyanov.laba3.controller.servlets;
 
 import java.io.IOException;
 import java.util.Collection;
 import java.util.Enumeration;
 
 import javax.ejb.FinderException;
 import javax.ejb.RemoveException;
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.log4j.Logger;
 
 import ua.edu.ChaliyLukyanov.laba3.model.Application;
 import ua.edu.ChaliyLukyanov.laba3.model.Consts;
 import ua.edu.ChaliyLukyanov.laba3.model.component.Component;
 import ua.edu.ChaliyLukyanov.laba3.model.component.ComponentHome;
 import ua.edu.ChaliyLukyanov.laba3.model.device.Device;
 import ua.edu.ChaliyLukyanov.laba3.model.device.DeviceHome;
 import ua.edu.ChaliyLukyanov.laba3.model.exception.ShopException;
 
 
 public class RemoveSingleComponentServlet extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 	private static Logger logger = Logger.getLogger(Consts.LOGGER_NAME);
        
 
 	protected void doPost(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 		ComponentHome model = (ComponentHome) request.getAttribute(Application.COMPONENT);
 		
 		try {
 			Enumeration<String> names = (Enumeration<String>) request.getParameterNames();
 			while (names.hasMoreElements()) {
 				int id = Integer.parseInt(names.nextElement());
 				model.remove(id);
 				logger.info("Component " + id + " removed");
 			}
             Collection<Component> components = model.findAll();
             request.setAttribute(Consts.COMPONENTS, components);
             RequestDispatcher dispatcher = request.getRequestDispatcher("/show_components.jsp");
             dispatcher.forward(request, response);
 		} catch (ShopException e) {
 			logger.error(e);
 			throw new ShopException(e.getMessage());
 		} catch (NumberFormatException e) {
 			logger.info(e);
 			throw new NumberFormatException(e.getMessage());
 		} catch (RemoveException e) {
 			logger.error(e);
 			throw new ShopException(e.getMessage());
 		} catch (FinderException e) {
 			logger.error(e);
 			throw new ShopException(e);
 		}
 	}
 
 }
