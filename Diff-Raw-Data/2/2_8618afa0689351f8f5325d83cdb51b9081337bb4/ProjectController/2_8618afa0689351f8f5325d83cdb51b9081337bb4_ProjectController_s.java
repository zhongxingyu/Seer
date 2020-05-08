 package controllers;
 
 import models.*;
 import play.Logger;
 import play.mvc.Controller;
 import play.mvc.With;
 
 import java.util.List;
 
 @With(Secure.class)
 public class ProjectController extends Controller {
 
 	/**
 	 * Create a new project and display the edit project page
 	 */
 	public static void newProject() {
 		Logger.info("Creating new project");
 
 		Project project = new Project();
 
 		// Retrieving user
 		// TODO Get the real user when authentication is enabled
 		Logger.info("Retrieving project owner %s", "admin");
 		User user = User.find("byUsername", "admin").first();
 		
 		// TODO Fix until better solution for initial null values in select
 		City city = City.get();
 
 		if (user != null) {
 			Logger.info("User %s retrieved successfully", user.getId());
 
 			// Assign owner
 			project.owner = user;
 			
 			// Assign city
 			project.city = city;
 
 			project.save();
 			Logger.info("New project created with id %s", project.getId());
 			
 			redirect("ProjectController.editProject", project.getId().toString());
 		} else {
 			flash.error("message", "project.creation.error");
 			projectList();
 		}
 	}
 
 	/**
 	 * Retrieve the projectId and display the edit project page
 	 * 
 	 * @param idProject
 	 */
 	public static void editProject(String idProject) {
 		Logger.info("Editing project %s", idProject);
 
 		Project project = Project.findById(idProject);
 
 		notFoundIfNull(project);
 		Logger.info("Project %s found", project.getId());
 
 		editProject(project);
 	}
 
 	/**
 	 * Retrieves the data needed to display the edit form
 	 * @param project
 	 */
 	private static void editProject(Project project) {
 		Logger.info("Retrieving images for project %s", project.getId()
 				.toString());
 		//List<Image> images = Image.find("project", project).asList();
 
 		Logger.info("%d images retrieved", project.images.size());
 
 		Logger.info("Retrieving all categories...");
 		List<Category> categories = Category.findAll();
 		Logger.info("%d categories retrieved", categories.size());
 		
 		Logger.info("Retrieving possible cities...");
 		List<City> cities = project.getPossibleCities();
 		Logger.info("%d cities retrieved", cities.size());
 				
 		render("project/editProject.html", project, categories, cities);
 	}
 
 	/**
 	 * Save the project and show the edit project page
 	 * 
 	 * @param project
 	 */
 	public static void saveProject(Project project) {
 		Logger.info("Updating project %s", project.getId());
 
 		project.save();
 		flash.success("project.updated.successfully");
 
 		redirect("ProjectController.editProject", project.getId().toString());
 	}
 
 
 	/**
 	 * Create new project from form
 	 * 
 	 * @param project
 	 */
 	public static void createProject(Project project) {
 		// Handle errors
 		if (validation.hasErrors()) {
 			render("project/editProject.html", project);
 		}
 
 		// Hack because checkbox binding issue
 		if (params.get("project.nonProfit") == null) {
 			project.nonProfit = false;
 		} else {
 			project.nonProfit = true;
 		}
 
 		// Create the project
 		project.save();
 
 		// flash.success("project successfully created");
 
 		// Go to the project list page
 		projectList();
 	}
 
 	/**
 	 * Shows a list of all the created projects in the system
 	 */
 	public static void projectList() {
 		Logger.info("Retrieving all the projects for user %s", session.get("username"));
 
         User user = User.find("email", session.get("username")).first();
         List<Project> projects = Project.find("owner", user).asList();
 
		render("project/projectList.html", projects);
 	}
 
 	/**
 	 * Displays the project profile page
 	 * 
 	 * @param idProject
 	 */
 	public static void showProject(String idProject) {
 		Logger.info("Showing project: ", idProject);
 		Project project = Project.findById(idProject);
 		// Redirect to the list if there's no project
 		if (project == null) {
 			projectList();
 		} else {
 			Logger.info("Project %s found", idProject);
 			render("project/project.html", project);
 		}
 	}
 
 	/**
 	 * Deletes a project based on the id
 	 * 
 	 * @param idProject
 	 */
 	public static void deleteProject(String idProject) {
 		Logger.info("Deleting project: %s", idProject);
 		Project project = Project.findById(idProject);
 		if (project != null) {
 			Logger.info("Project %s found!", idProject);
 			project.delete();
 			flash.success("project.deleted.successfully");
 		}
 		projectList();
 	}
 
     /**
      * Save an image of a project
      * @param imageUrl
      * @param idProject
      */
     public static void imageSaved(String imageUrl, String idProject){
         Logger.info("url received " + imageUrl + " for project " + idProject);
 
         notFoundIfNull(imageUrl);
 
         Project project = Project.findById(idProject);
 
         if (project != null){
             Logger.info("saving image...");
             Image projectImage = new Image();
             projectImage.url = imageUrl;
             //projectImage.project = project;
             projectImage.save();
 
             project.images.add(projectImage);
             project.save();
 
             flash.success("project.image.upload.success");
 
         }
 
         renderJSON("{\"imageUrl\": \"" + imageUrl +"\"}");
     }
 }
