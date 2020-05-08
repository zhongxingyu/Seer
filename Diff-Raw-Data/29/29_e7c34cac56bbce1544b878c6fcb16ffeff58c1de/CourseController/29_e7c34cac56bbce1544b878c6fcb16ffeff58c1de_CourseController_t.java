 package org.jojen.wikistudy.controller;
 
 import org.jojen.wikistudy.entity.Course;
 import org.jojen.wikistudy.entity.Lesson;
 import org.jojen.wikistudy.service.CourseService;
 import org.jojen.wikistudy.service.LessonService;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.*;
 
 import javax.inject.Inject;
 import javax.validation.Valid;
 
 @Controller
 @RequestMapping("/course")
 public class CourseController {
 	protected static final int DEFAULT_PAGE_NUM = 0;
 	protected static final int DEFAULT_PAGE_SIZE = 5;
 
 	@Inject
 	protected CourseService courseService;
 
 	@Inject
 	protected LessonService lessonService;
 
 
 	protected static final Logger log = LoggerFactory
 												.getLogger(CourseController.class);
 
 
 	@RequestMapping(value = "/{courseId}/lesson/{id}", method = RequestMethod.GET)
 	public String listLesson(
 									@PathVariable("courseId") Integer cid,
 									@PathVariable("id") Integer id,
 									@RequestParam(value = "page", required = false) Integer page,
 									Model model) {
 		Course c = courseService.findById(cid);
 		Lesson l = lessonService.findById(id);
 
 		model.addAttribute("course", c);
 		model.addAttribute("lesson", l);
 
 		return "/course/course";
 	}
 
 
 	@RequestMapping(value = "/{courseId}/lesson/delete/{id}")
 	public String deleteLesson(
 									  @PathVariable("courseId") Integer cid,
 									  @RequestParam(value = "page", required = false) Integer page,
 									  @PathVariable("id") Integer id,
 									  Model model) {
 		log.debug("delete id={}", id);
 		lessonService.deleteById(id);
 
 		Course c = courseService.findById(cid);
 		model.addAttribute("course", c);
 
 		return "/course/" + cid;
 	}
 
 
 	@RequestMapping(value = "/{id}")
 	public String show(
 							  @PathVariable("id") Integer id,
 							  @RequestParam(value = "page", required = false) Integer page,
 							  Model model) {

		Integer lesson;
        Course c = courseService.findById(id);

        if(c.getLessons().isEmpty()){
            Lesson l = new Lesson();
            l = lessonService.insert(l);
            c.addLessons(l);
            courseService.update(c);
            lesson = l.getId();

        }else{
            lesson = c.getLessons().iterator().next().getId();
        }
        // TODO aktuelle lesson des benutzers

 		return "redirect:/course/" + id + "/lesson/" + lesson;
 	}
 
 	@RequestMapping(value = "/{id}/add-lesson")
 	public String addLesson(
 								   @PathVariable("id") Integer id,
 								   Model model) {
 		Course c = courseService.findById(id);
 		Lesson l = new Lesson();
 		lessonService.insert(l);
 		c.addLessons(l);
 		courseService.update(c);
 
 		return "redirect:/course/" + id + "/lesson/" + l.getId();
 	}
 
 
 	@RequestMapping(value = "/edit", method = RequestMethod.GET)
 	public String edit(@RequestParam(value = "id", required = false) Integer id, Model model) {
 		if (id == null) {
 			model.addAttribute(new Course());
 		} else {
 			model.addAttribute(courseService.findById(id));
 		}
 
 		return "/course/form";
 	}
 
 	@RequestMapping(value = "/form", method = RequestMethod.GET)
 	public
 	@ModelAttribute
 	Course create(Model model) {
 		Course course = new Course();
 		return course;
 	}
 
 	@RequestMapping(value = "/form", method = RequestMethod.POST)
 	public String createOnSubmit(@Valid Course course,
 								 BindingResult bindingResult, Model model) {
 		log.debug("create course={}", course);
 		if (bindingResult.hasErrors()) {
 			log.warn("validation error={}", bindingResult.getModel());
 			model.addAllAttributes(bindingResult.getModel());
 			return "/course/form";
 		}
 		courseService.insert(course);
 		return "redirect:/";
 	}
 
 
 	@RequestMapping(value = "/edit", method = RequestMethod.POST)
 	public String editOnSubmit(@Valid Course course,
 							   BindingResult bindingResult,
 							   @RequestParam(value = "id", required = false) Integer id
 									  , Model model) {
 		log.debug("edit course={}", course);
 		if (bindingResult.hasErrors()) {
 			log.warn("validation error={}", bindingResult.getModel());
 			model.addAllAttributes(bindingResult.getModel());
 			return "/course/form";
 		}
 		Course modelCourse;
 		if (id == null) {
 			modelCourse = new Course();
 		} else {
 			modelCourse = courseService.findById(id);
 		}
 
 		// TODO hier vielleicht noch ein bisschen reflections
 		modelCourse.setName(course.getName());
 		modelCourse.setDescription(course.getDescription());
 		courseService.update(modelCourse);
 		return "redirect:/";
 	}
 
 	@RequestMapping(value = "/delete/{id}")
 	public String delete(
 								@RequestParam(value = "page", required = false) Integer page,
 								@PathVariable("id") Integer id) {
 		log.debug("delete id={}", id);
 		courseService.deleteById(id);
 
 		return "redirect:/";
 	}
 
 
 }
