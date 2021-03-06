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
 import is2.domain.Carrera;
 import is2.domain.CategoriaNota;
 import is2.domain.Docente;
 import is2.domain.Periodo;
 import is2.service.AdminService;
 import is2.service.AlumnoService;
 import is2.service.CarreraService;
 import is2.service.CategoriaNotaService;
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
 	CarreraService carreraService;
 	
 	@Inject
 	CategoriaNotaService categoriaNotaService;
 
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
 	
	@RequestMapping("/add_alumno.html")
	public ModelAndView add_alumno(){
		Alumno alumno = new Alumno();
		ModelAndView model = new ModelAndView("admin/add_alumno");
		model.addObject("alumno", alumno);
		return model;
	}
	
	@RequestMapping(value="/save_alumno.html", method=RequestMethod.POST)
	public ModelAndView save_alumno(@ModelAttribute("alumno") @Valid Alumno alumno, SessionStatus status){
		if (alumno.getId() == null) {
			alumnoService.persist(alumno);
			status.setComplete();
		}
		else {
			alumnoService.merge(alumno);
			status.setComplete();
		}
		return new ModelAndView("redirect:alumnos.html");
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
 	
 	@RequestMapping("/add_periodo.html")
 	public ModelAndView add_periodo(){
 		Periodo periodo = new Periodo();
 		ModelAndView model = new ModelAndView("admin/add_periodo");
 		model.addObject("periodo", periodo);
 		return model;
 	}
 	
 	@RequestMapping("/{id}/edit_periodo.html")
 	public ModelAndView edit_periodo(@PathVariable Long id){
 		Periodo periodo = periodoService.find(id);
 		ModelAndView model = new ModelAndView("admin/add_periodo");
 		model.addObject("periodo", periodo);
 		return model;
 	}
 	
 	@RequestMapping(value="/save_periodo.html", method=RequestMethod.POST)
 	public ModelAndView save_periodo(@ModelAttribute("periodo") @Valid Periodo periodo, SessionStatus status){
 		if (periodo.getId() == null) {
 			periodoService.persist(periodo);
 			status.setComplete();
 		}
 		else {
 			periodoService.merge(periodo);
 			status.setComplete();
 		}
 		return new ModelAndView("redirect:periodos.html");
 	}
 	
 	@RequestMapping(value="/{id}/remove_periodo.html")
 	public ModelAndView remove_periodo(@PathVariable Long id){
 		periodoService.removeById(id);
 		return new ModelAndView("redirect:../periodos.html");
 	}
 	
 	@RequestMapping("/categorias_notas.html")
 	public ModelAndView categorias_notas() {
 		List<CategoriaNota> categorias = categoriaNotaService.findAll();
 		ModelAndView model = new ModelAndView("admin/categorias_notas");
 		model.addObject("categorias",categorias);
 		return model;
 	}
 	
 	@RequestMapping("/add_categoria_nota.html")
 	public ModelAndView add_categoria_nota() {
 		CategoriaNota categoria = new CategoriaNota();
 		ModelAndView model = new ModelAndView("admin/add_categoria_nota");
 		model.addObject("categoria", categoria);
 		return model;
 	}
 	
 	@RequestMapping("/{id}/edit_categoria_nota.html")
 	public ModelAndView edit_categoria_nota(@PathVariable Long id) {
 		CategoriaNota categoria = categoriaNotaService.find(id);
 		ModelAndView model = new ModelAndView("admin/add_categoria_nota");
 		model.addObject("categoria", categoria);
 		return model;
 	}
 	
 	@RequestMapping(value="/save_categoria_nota.html", method=RequestMethod.POST)
 	public ModelAndView save_categoria_nota(@ModelAttribute("categoria") @Valid CategoriaNota categoria, SessionStatus status) {
 		if (categoria.getId() == null) {
 			categoriaNotaService.persist(categoria);
 			status.setComplete();
 		}
 		else {
 			categoriaNotaService.merge(categoria);
 			status.setComplete();
 		}
 		return new ModelAndView("redirect:categorias_notas.html");
 	}
 	
 	@RequestMapping("/{id}/remove_categoria_nota.html")
 	public ModelAndView remove_categoria_nota(@PathVariable Long id){
 		categoriaNotaService.removeById(id);
 		return new ModelAndView("redirect:../categorias_notas.html");
 	}
 	
 	@RequestMapping("/carreras.html")
 	public ModelAndView carreras() {
 		List<Carrera> carreras = carreraService.findAll();
 		ModelAndView model = new ModelAndView("admin/carreras");
 		model.addObject("carreras",carreras);
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
