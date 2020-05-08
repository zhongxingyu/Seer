 package kz.enu.epam.azimkhan.tour.command.admin;
 
 import kz.enu.epam.azimkhan.tour.builder.TourBuilder;
 import kz.enu.epam.azimkhan.tour.command.AdminCommand;
 import kz.enu.epam.azimkhan.tour.dao.TourDAO;
 import kz.enu.epam.azimkhan.tour.entity.Tour;
 import kz.enu.epam.azimkhan.tour.entity.TourType;
 import kz.enu.epam.azimkhan.tour.exception.BuildException;
 import kz.enu.epam.azimkhan.tour.exception.CommandException;
 import kz.enu.epam.azimkhan.tour.exception.DAOLogicalException;
 import kz.enu.epam.azimkhan.tour.exception.DAOTechnicalException;
 import kz.enu.epam.azimkhan.tour.notification.creator.NotificationCreator;
 import kz.enu.epam.azimkhan.tour.notification.entity.Notification;
 import kz.enu.epam.azimkhan.tour.notification.service.NotificationService;
 import kz.enu.epam.azimkhan.tour.resource.LocaleManager;
 import kz.enu.epam.azimkhan.tour.resource.PathManager;
 import org.apache.log4j.Logger;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.util.Locale;
 
 /**
  * Add tour command
  */
 public class AddTourCommand extends AdminCommand {
 
     @Override
 	public String execute(HttpServletRequest request, HttpServletResponse response) throws CommandException {
 
         request.setAttribute("tourTypes", TourType.values());
         Notification notification = null;
         Locale locale = LocaleManager.INSTANCE.resolveLocale(request);
         Tour tour = new Tour();
 
         if (request.getParameter("submit") != null){
             TourBuilder tourBuilder = new TourBuilder();
             try {
                 tourBuilder.build(request.getParameterMap(), tour);
                 TourDAO dao = TourDAO.getInstance();
                 if (dao.create(tour)){
                     notification = NotificationCreator.createFromProperty("info.db.create_success", locale);
                     return pathManager.getString("path.page.admin.manager");
                 }
 
             } catch (BuildException e) {
                 notification = NotificationCreator.createFromProperty("add_tour.invalid_form_data", Notification.Type.ERROR,  locale);
             } catch (DAOTechnicalException e) {
                 throw new CommandException(e);
 
             } catch (DAOLogicalException e) {
                 notification = new Notification(e.getMessage(), Notification.Type.ERROR);
             } finally {
                 if (notification != null){
                     NotificationService.push(request.getSession(), notification);
                 }
             }
         }
 
         request.setAttribute("tour", tour);
 
 		return pathManager.getString("path.page.admin.add_tour");
 	}
 }
