 package is2.controller;
 
 import java.io.IOException;
 import java.util.List;
 
 import javax.inject.Inject;
 import javax.servlet.http.HttpServletResponse;
 import javax.validation.Valid;
 
 import org.springframework.security.core.context.SecurityContextHolder;
 import org.springframework.security.core.userdetails.User;
 import org.springframework.stereotype.Controller;
 import org.springframework.validation.BindingResult;
 import org.springframework.validation.Validator;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.support.SessionStatus;
 import org.springframework.web.servlet.ModelAndView;
 
 import is2.domain.Alumno;
 import is2.domain.Carrera;
 import is2.service.AlumnoService;
 import is2.service.CarreraService;
 import is2.service.HorarioAlumnoService;
 import is2.service.MatriculaAlumnoService;
 
 @Controller
 @RequestMapping("/alumno")
 public class AlumnoController {
 
         @Inject 
         AlumnoService alumnoService;
         
         @Inject
         HorarioAlumnoService horarioAlumnoService;
         
         @Inject
         MatriculaAlumnoService matriculaAlumnoService;
         
         @Inject
     	CarreraService carreraService;
       
 	    @Inject
         Validator validator;
 
 
 	@RequestMapping("/{id}/details.html")
 	public ModelAndView details(@PathVariable Long id) {
 		ModelAndView view = new ModelAndView();
 		view.addObject("alumno", alumnoService.find(id));
 		view.setViewName("alumno/details");
 		return view;
 	}
 	
 	@RequestMapping("/details.html")
 	public ModelAndView details2(HttpServletResponse response) throws IOException {
 		User user = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
 		Alumno alumno = alumnoService.findByUsername(user.getUsername());
 		ModelAndView view = new ModelAndView();
		view.addObject("alumno", alumnoService.find(alumno.getId()));
 		view.setViewName("alumno/details");
 		return view;
 	}
 
 	@RequestMapping("/{id}/edit.html")
 	public ModelAndView edit(@PathVariable Long id) {
 		ModelAndView view = new ModelAndView();
 		view.addObject("alumno", alumnoService.find(id));
 		view.setViewName("alumno/edit");
 		return view;
 	}
 	
 	@RequestMapping("/edit.html")
 	public ModelAndView edit2() {
 		User user = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
 		Alumno alumno = alumnoService.findByUsername(user.getUsername());
 		List<Carrera> carreras = carreraService.findAll();
 		ModelAndView model = new ModelAndView("alumno/edit2");
 		model.addObject("alumno", alumno);
 		model.addObject("carreras",carreras);
 		return model;
 	}
 	
 	@RequestMapping("/{id}/notas.html")
 	public ModelAndView notas(@PathVariable Long id)
 	{
 		ModelAndView view = new ModelAndView();
 		view.addObject("alumno", alumnoService.find(id));
 		view.setViewName("alumno/notas");
 		return view;
 	}
 	
 	@RequestMapping("/notas.html")
 	public ModelAndView notas2() {
 		User user = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
 		Alumno alumno = alumnoService.findByUsername(user.getUsername());
 		ModelAndView view = new ModelAndView();
		view.addObject("alumno", alumnoService.find(alumno.getId()));
 		view.setViewName("alumno/notas");
 		return view;
 	}
 	
 	@RequestMapping("/cursos.html")
 	public ModelAndView cursos() {
 		User user = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
 		Alumno alumno = alumnoService.findByUsername(user.getUsername());
 		ModelAndView view = new ModelAndView();
		view.addObject("alumno", alumnoService.find(alumno.getId()));
 		view.setViewName("alumno/cursos");
 		return view;
 	}
 	
 	@RequestMapping("/{id}/avance_curricular.html")
 	public ModelAndView avance_curricular(@PathVariable Long id)
 	{
 		ModelAndView view = new ModelAndView();
 		view.addObject("alumno", alumnoService.find(id));
 		view.setViewName("alumno/avance_curricular");
 		return view;
 	}
 	
 	@RequestMapping("/avance_curricular.html")
 	public ModelAndView avance_curricular2()
 	{
 		User user = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
 		Alumno alumno = alumnoService.findByUsername(user.getUsername());
 		ModelAndView view = new ModelAndView();
 		view.addObject("alumno", alumno);
 		view.setViewName("alumno/avance_curricular");
 		return view;
 	}
 
 	@RequestMapping("/{id}/horario.html")
     public ModelAndView horarios (@PathVariable Long id)
     {
             ModelAndView view = new ModelAndView();
             view.addObject("alumno",alumnoService.find(id));
             view.addObject("matriculas", horarioAlumnoService.getMatriculas(id));
             view.setViewName("alumno/horario");
             return view;
     }
 	
 	@RequestMapping("/horario.html")
 	public ModelAndView horarios2()
 	{
 		User user = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
 		Alumno alumno = alumnoService.findByUsername(user.getUsername());
 		ModelAndView view = new ModelAndView();
		view.addObject("alumno", alumnoService.find(alumno.getId()));
 		view.addObject("matriculas", horarioAlumnoService.getMatriculas(alumno.getId()));
 		view.setViewName("alumno/horario");
 		return view;
 	}
 	
 	@RequestMapping("/{id}/matricula.html")
     public ModelAndView matricula (@PathVariable Long id)
     {
             ModelAndView view = new ModelAndView();
             view.addObject("alumno",alumnoService.find(id));
             view.addObject("cursos", matriculaAlumnoService.getCursosDisponibles(id));
             view.setViewName("alumno/matricula");
             return view;
     }
 	
 	@RequestMapping("/matricula.html")
     public ModelAndView matricula2 ()
     {
 		User user = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
 		Alumno alumno = alumnoService.findByUsername(user.getUsername());
 		ModelAndView view = new ModelAndView();
		view.addObject("alumno", alumnoService.find(alumno.getId()));
 		view.addObject("cursos", matriculaAlumnoService.getCursosDisponibles(alumno.getId()));
 		view.setViewName("alumno/matricula");
 		return view;
     }
 	
     @RequestMapping("/add.html")
     public ModelAndView add() {
           ModelAndView view = new ModelAndView();
           view.addObject("alumno", new Alumno());
           view.setViewName("alumno/edit");
           return view;
         }
 
 
 	@RequestMapping(value = "/save.html", method = RequestMethod.POST)
 	public ModelAndView save(@ModelAttribute("alumno") @Valid Alumno Alumno, BindingResult result, SessionStatus status) {
 		if (Alumno.getId() == null) {
 			alumnoService.persist(Alumno);
 			status.setComplete();
 		}
 		else {
 			alumnoService.merge(Alumno);
 			status.setComplete();
 		}
 		return new ModelAndView(result.getErrorCount() > 0 ? "alumno/edit" : "redirect:list.html");
 //		return new ModelAndView("Alumno/save");
 	}
 	
 	@RequestMapping(value="/save2.html", method=RequestMethod.POST)
 	public ModelAndView save_alumno(@ModelAttribute("alumno") @Valid Alumno alumno, SessionStatus status){
 		if (alumno.getId() == null) {
 			alumnoService.encodePassword(alumno);
 			alumnoService.persist(alumno);
 			status.setComplete();
 		}
 		else {
 			if( alumno.getPassword() != "" ){
 				alumnoService.encodePassword(alumno);
 				alumnoService.merge(alumno);
 			}
 			else
 				alumnoService.merge_sin_password(alumno);
 			status.setComplete();
 		}
 		return new ModelAndView("redirect:details.html");
 	}
 }
