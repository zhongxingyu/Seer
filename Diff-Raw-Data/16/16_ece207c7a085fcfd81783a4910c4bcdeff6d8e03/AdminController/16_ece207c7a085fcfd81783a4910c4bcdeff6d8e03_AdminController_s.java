 package is2.controller;
 
 import java.util.List;
 
 import javax.inject.Inject;
 import javax.validation.Valid;
 
 import org.springframework.stereotype.Controller;
 import org.springframework.validation.BindingResult;
 import org.springframework.validation.Validator;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.support.SessionStatus;
 import org.springframework.web.servlet.ModelAndView;
 
 import is2.domain.Admin;
 import is2.domain.Alumno;
 import is2.domain.Docente;
 import is2.domain.Periodo;
 import is2.service.AdminService;
 import is2.service.AlumnoService;
 import is2.service.DocenteService;
 import is2.service.PeriodoService;
 
 @Controller
 @RequestMapping("/admin")
 public class AdminController {
 
 	@Inject
 	AdminService adminService;
 	
 	@Inject
 	AlumnoService alumnoService;
 	
 	@Inject
 	DocenteService docenteService;
 	
 	@Inject
 	PeriodoService periodoService;
 
 	@Inject
 	Validator validator;
 
 	@RequestMapping("/list.html")
 	public ModelAndView list() {
 		return new ModelAndView("admin/list", "admins", adminService.findAll());
 	}
 	
 	@RequestMapping("/alumnos.html")
 	public ModelAndView alumnos() {
 		List<Alumno> alumnos = alumnoService.findAll();
 		ModelAndView model = new ModelAndView("admin/alumnos");
 		model.addObject("alumnos",alumnos);
 		return model;
 	}
 	
 	@RequestMapping("/docentes.html")
 	public ModelAndView docentes() {
 		List<Docente> docentes = docenteService.findAll();
 		ModelAndView model = new ModelAndView("admin/docentes");
 		model.addObject("docentes",docentes);
 		return model;
 	}
 	
 	@RequestMapping("/periodos.html")
 	public ModelAndView periodos() {
 		List<Periodo> periodos = periodoService.findAll();
 		ModelAndView model = new ModelAndView("admin/periodos");
 		model.addObject("periodos",periodos);
 		return model;
 	}
 
 	@RequestMapping("/{id}/details.html")
 	public ModelAndView details(@PathVariable Long id) {
 		ModelAndView view = new ModelAndView();
 		view.addObject("admin", adminService.find(id));
 		view.setViewName("admin/details");
 		return view;
 	}
 
 	@RequestMapping("/{id}/edit.html")
 	public ModelAndView edit(@PathVariable Long id) {
 		ModelAndView view = new ModelAndView();
 		view.addObject("admin", adminService.find(id));
 		view.setViewName("admin/edit");
 		return view;
 	}
 
 	@RequestMapping("/add.html")
 	public ModelAndView add() {
 		ModelAndView view = new ModelAndView();
 		view.addObject("admin", new Admin());
 		view.setViewName("admin/edit");
 		return view;
 	}
 
 	@RequestMapping(value = "/save.html", method = RequestMethod.POST)
 	public ModelAndView save(@ModelAttribute("admin") @Valid Admin Admin, BindingResult result, SessionStatus status) {
 		if (Admin.getId() == null) {
 			adminService.persist(Admin);
 			status.setComplete();
 		}
 		else {
 			adminService.merge(Admin);
 			status.setComplete();
 		}
 		return new ModelAndView(result.getErrorCount() > 0 ? "admin/edit" : "redirect:list.html");
 //		return new ModelAndView("Admin/save");
 	}
 }
