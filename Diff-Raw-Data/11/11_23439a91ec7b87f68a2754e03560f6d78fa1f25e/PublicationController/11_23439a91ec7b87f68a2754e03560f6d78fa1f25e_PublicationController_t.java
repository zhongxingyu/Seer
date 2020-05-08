 package zonaProp.web;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.validation.Errors;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.servlet.ModelAndView;
 
 import zonaProp.mailing.MailingService;
 import zonaProp.model.repo.PublicationRepo;
 import zonaProp.model.repo.UserRepo;
 import zonaProp.transfer.bussiness.Environment;
 import zonaProp.transfer.bussiness.EnvironmentType;
 import zonaProp.transfer.bussiness.Photo;
 import zonaProp.transfer.bussiness.PropertyServices;
 import zonaProp.transfer.bussiness.Publication;
 import zonaProp.transfer.bussiness.User;
 import zonaProp.web.command.CommentForm;
 import zonaProp.web.command.EnvironmentForm;
 import zonaProp.web.command.PhotoForm;
 import zonaProp.web.command.PublicationForm;
 import zonaProp.web.command.SearchForm;
 import zonaProp.web.command.validator.CommentFormValidator;
 import zonaProp.web.command.validator.EnvironmentFormValidator;
 import zonaProp.web.command.validator.PhotoFormValidator;
 import zonaProp.web.command.validator.PublicationFormValidator;
 import zonaProp.web.command.validator.SearchFormValidator;
 
 @Controller
 public class PublicationController {
 
 	PublicationFormValidator pfv;
 	PhotoFormValidator photofv;
 	CommentFormValidator cfv;
 	SearchFormValidator sfv;
 	EnvironmentFormValidator efv;
 	PublicationRepo publications;
 	UserRepo users;
 	MailingService mailingService;
 
 	@Autowired
 	public PublicationController(PublicationFormValidator pfv,
 			PhotoFormValidator photofv, CommentFormValidator cfv,
 			SearchFormValidator sfv, EnvironmentFormValidator efv,
 			PublicationRepo publications, UserRepo users,
 			MailingService mailingService) {
 
 		this.photofv = photofv;
 		this.pfv = pfv;
 		this.sfv = sfv;
 		this.cfv = cfv;
 		this.efv = efv;
 		this.publications = publications;
 		this.users = users;
 		this.mailingService = mailingService;
 	}
 
 	@RequestMapping(method = RequestMethod.GET)
 	public ModelAndView search() {
 
 		ModelAndView mav = new ModelAndView();
 		mav.addObject("searchForm", new SearchForm());
 		return mav;
 
 	}
 
 	@RequestMapping(method = RequestMethod.GET)
 	public ModelAndView searchResults(SearchForm sf, Errors errors) {
 		sfv.validate(sf, errors);
 
 		ModelAndView mav = new ModelAndView();
 
 		if (errors.hasErrors()) {
 			mav.addObject("searchForm", sf);
 			mav.setViewName("publication/search");
 			return mav;
 		} else {
 			List<Publication> pList = publications.getSome(sf.build());
 
 			int fromIndex = (sf.getPageValue() - 1) * sf.getPageSizeValue();
 			int toIndex = sf.getPageValue() * sf.getPageSizeValue();
 
 			fromIndex = (fromIndex > 0 && fromIndex <= toIndex && fromIndex < pList
 					.size()) ? fromIndex : 0;
 			toIndex = (toIndex > 0 && toIndex < pList.size() && toIndex >= fromIndex) ? toIndex
 					: pList.size();
 
 			List<SearchForm> sFList = new ArrayList<SearchForm>();
 
 			for (int i = 0; i < pList.size() / sf.getPageSizeValue()
 					+ (pList.size() % sf.getPageSizeValue() == 0 ? 0 : 1); i++) {
 				SearchForm s = new SearchForm(sf.getMax(), sf.getMin(),
 						sf.getOperationType(), sf.getPropertyType(),
 						sf.getPublisher(), sf.isAscending(),
 						String.valueOf(i + 1), sf.getPageSize());
 				sFList.add(s);
 			}
 			mav.addObject("sf", sf);
 			mav.addObject("sFList", sFList);
 			mav.addObject("pList", pList.subList(fromIndex, toIndex));
 			return mav;
 		}
 
 	}
 
 	@RequestMapping(method = RequestMethod.GET)
 	public ModelAndView view(@RequestParam("publicationId") Publication p) {
 
 		ModelAndView mav = new ModelAndView();
 		mav.addObject("publication", p);
 		mav.addObject("commentForm", new CommentForm());
 		mav.addObject("searchForm", new SearchForm());
 		mav.addObject("showPublisher", false);
 		p.access();
 		return mav;
 	}
 
 	@RequestMapping(method = RequestMethod.POST)
 	public ModelAndView comment(@RequestParam("publicationId") Publication p,
 			CommentForm cf, Errors errors) {
 
 		cfv.validate(cf, errors);
 
 		ModelAndView mav = new ModelAndView();
 
 		if (errors.hasErrors()) {
 			mav.addObject("showPublisher", false);
 		} else {
 			mav.addObject("showPublisher", true);
 			mailingService.contact(cf.build(), p.getPublisher());
 		}
 
 		mav.addObject("publication", p);
 		mav.addObject("searchForm", new SearchForm());
 		mav.setViewName("publication/view");
 		return mav;
 	}
 
 	@RequestMapping(method = RequestMethod.POST)
 	protected ModelAndView uploadPhoto(
 			@RequestParam("publicationId") Publication p, PhotoForm pF,
 			Errors errors, HttpSession s) {
 
 		User u = users.get((Integer) s.getAttribute("userId"));
 
		if (!p.belongsTo(u)) {
 			return null;
 		}
 		photofv.validate(pF, errors);
 
 		if (!errors.hasErrors()) {
 			Photo image = pF.build();
 			p.addPhoto(image);
 			ModelAndView mav = new ModelAndView("redirect:editPhotos");
 			mav.addObject("publicationId", p.getId());
 			return mav;
 		}
 		ModelAndView mav = new ModelAndView();
 		mav.setViewName("publication/editPhotos");
 		mav.addObject("publication", p);
 		return mav;
 	}
 
 	@RequestMapping(method = RequestMethod.GET)
 	public void viewPhoto(@RequestParam("imageId") Photo photo,
 			HttpServletResponse resp) {
 		if (photo == null) {
 			return;
 		}
 		resp.setContentType("image/jpeg");
 		resp.setHeader("Content-Disposition", "inline; filename=\"imagen"
 				+ photo.getId() + "\"");
 		OutputStream output = null;
 		try {
 			output = resp.getOutputStream();
 			resp.setContentLength(photo.getSize());
 			output.write(photo.getData());
 		} catch (IOException e) {
 			throw new RuntimeException(e);
 		} finally {
 			try {
 				output.close();
 			} catch (Exception e) {
 				throw new RuntimeException(e);
 			}
 		}
 	}
 
 	@RequestMapping(method = RequestMethod.GET)
 	public ModelAndView editPhotos(@RequestParam("publicationId") Publication p) {
 		ModelAndView mav = new ModelAndView();
 		mav.addObject("publication", p);
 		mav.addObject("photoForm", new PhotoForm());
 		return mav;
 	}
 
 	@RequestMapping(method = RequestMethod.POST)
 	protected ModelAndView deletePhoto(
 			@RequestParam("publicationId") Publication p,
 			@RequestParam("imageId") Photo photo, HttpSession s) {
 
 		User u = users.get((Integer) s.getAttribute("userId"));
 
 		ModelAndView mav = new ModelAndView();
 
		if (!p.belongsTo(u)) {
 			return null;
 		}
 		p.deletePhoto(photo);
 		mav.addObject("publication", p);
 		mav.addObject("photoForm", new PhotoForm());
 		mav.setViewName("publication/editPhotos");
 		return mav;
 	}
 
 	@RequestMapping(method = RequestMethod.GET)
 	public ModelAndView modify(@RequestParam("publicationId") Publication p) {
 		ModelAndView mav = new ModelAndView();
 
 		mav.addObject("publicationForm", new PublicationForm(p));
 		mav.addObject("services", PropertyServices.values());
 		mav.setViewName("publication/ABM");
 		return mav;
 	}
 
 	@RequestMapping(method = RequestMethod.GET)
 	public ModelAndView create() {
 		ModelAndView mav = new ModelAndView();
 
 		mav.addObject("publicationForm", new PublicationForm());
 		mav.addObject("services", PropertyServices.values());
 		mav.setViewName("publication/ABM");
 		return mav;
 	}
 
 	@RequestMapping(method = RequestMethod.POST)
 	public ModelAndView saveChanges(PublicationForm pf, Errors errors,
 			HttpSession s) {
 
 		pfv.validate(pf, errors);
 
 		ModelAndView mav = new ModelAndView();
 
 		User publisher = users.get((Integer) s.getAttribute("userId"));
 
 		if (errors.hasErrors()) {
 			mav.addObject("publicationForm", pf);
 			mav.addObject("services", PropertyServices.values());
 			mav.setViewName("publication/ABM");
 			return mav;
 		}
 
 		Publication p = pf.build();
 		p.setPublisher(publisher);
 		if (p.isNew()) {
 			publications.add(p);
 		} else {
 			Publication old = publications.get(p.getId());
 			old.update(p);
 		}
 
 		return new ModelAndView("redirect:../user/publications");
 
 	}
 
 	@RequestMapping(method = RequestMethod.GET)
 	public ModelAndView addEnvironment(
 			@RequestParam("publicationId") Publication p) {
 		ModelAndView mav = new ModelAndView();
 
 		mav.addObject("publication", p);
 		mav.addObject("environmentForm", new EnvironmentForm());
 		mav.addObject("environmentTypes", EnvironmentType.values());
 		return mav;
 	}
 
 	@RequestMapping(method = RequestMethod.GET)
 	protected ModelAndView deleteEnv(
 			@RequestParam("publicationId") Publication p,
 			@RequestParam("envId") Environment e, HttpSession s) {
 
 		User u = users.get((Integer) s.getAttribute("userId"));
 
		if (!p.belongsTo(u)) {
 			return null;
 		}
 
 		ModelAndView mav = new ModelAndView();
 		p.deleteEnvironment(e);
 		mav.addObject("publicationForm", new PublicationForm(p));
 		mav.addObject("services", PropertyServices.values());
 		mav.setViewName("publication/ABM");
 		return mav;
 	}
 
 	@RequestMapping(method = RequestMethod.POST)
 	protected ModelAndView addEnvironment(
 			@RequestParam("publicationId") Publication p, EnvironmentForm ef,
 			Errors errors, HttpSession s) {
 
 		User u = users.get((Integer) s.getAttribute("userId"));
 
		if (!p.belongsTo(u)) {
 			return null;
 		}
 		efv.validate(ef, errors);
 
 		if (!errors.hasErrors()) {
 			Environment env = ef.build();
 			p.addEnvironment(env);
 			ModelAndView mav = new ModelAndView("redirect:modify");
 			mav.addObject("publicationId", p.getId());
 			return mav;
 		}
 		ModelAndView mav = new ModelAndView();
 		mav.addObject("publication", p);
 		mav.addObject("environmentForm", ef);
 		mav.addObject("environmentTypes", EnvironmentType.values());
 		return mav;
 
 	}
 
 }
