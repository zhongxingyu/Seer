 package com.alexecollins.releasemanager.web;
 
 import com.alexecollins.releasemanager.model.*;
 import com.mdimension.jchronic.Chronic;
 import org.pegdown.PegDownProcessor;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.data.domain.Sort;
 import org.springframework.stereotype.Controller;
 import org.springframework.transaction.annotation.Transactional;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 
import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.*;
 
 /**
  * @author alexec (alex.e.c@gmail.com)
  */
 @Controller
 public class ReleaseController {
 
 	@Autowired
 	ReleaseRepository releaseRepository;
 	@Autowired
 	ComponentRepository componentRepository;
 	@Autowired
 	UserRepository userRepository;
 
 	@RequestMapping("/releases")
 	public String index(Model model) {
 		model.addAttribute("releases", releaseRepository.findAll(new Sort(new Sort.Order("begin"))));
 
 		return "releases";
 	}
 	@RequestMapping("/releases/{id}")
 	public String edit(Model model, @PathVariable("id") String id, boolean edit) {
 		final Release r = releaseRepository.findOne(id);
 
 		model.addAttribute("release", r);
 
		model.addAttribute("when", newSimpleDateFormat().format(r.getWhen()));
		model.addAttribute("duration", TimeSpan.format(r.getDuration()));

 		final PegDownProcessor pegDownProcessor = new PegDownProcessor();
 		model.addAttribute("desc", pegDownProcessor.markdownToHtml(r.getDesc()));
 
 		model.addAttribute("included_components", new ArrayList<>(r.getComponents()));
 		final List<Component> components = new ArrayList<>(componentRepository.findAll());
 
 		for (ReleaseComponent releaseComponent : r.getComponents()) {
 			components.remove(releaseComponent.getComponent());
 		}
 
 		model.addAttribute("excluded_components", new ArrayList<>(components));
 
 		final Map<User, SignOff> signOffHashMap = new HashMap<>();
 		for (Map.Entry<String, SignOff> entry : r.getSignOffs().entrySet()) {
 			signOffHashMap.put(userRepository.findOne(entry.getKey()), entry.getValue());
 		}
 
 		model.addAttribute("sign_offs", signOffHashMap);
 
 		final List<User> users = userRepository.findAll();
 		users.removeAll(signOffHashMap.keySet());
 		model.addAttribute("excluded_users", users);
 
 		model.addAttribute("created", new SimpleDateFormat("dd MMM yy hh:MMaa").format(r.getCreated()));
 
 		return "releases/" + (!edit ?"view":"edit");
 	}
 
	private SimpleDateFormat newSimpleDateFormat() {
		return new SimpleDateFormat("dd MMM yyyy hh:ss z");
	}

 	@RequestMapping(value = "/releases/{id}", method = RequestMethod.POST)
 	public String post(String submit, @PathVariable("id") String id, String name, String when, String duration, String desc, String status) {
 
 		switch (submit) {
 			case "Update":
 				final Release release = releaseRepository.findOne(id);
 				release.setName(name);
 				release.setDesc(desc);
				try {
					release.setWhen(newSimpleDateFormat().parse(when));
				} catch (ParseException e) {
					release.setWhen(Chronic.parse(when).getBeginCalendar().getTime());
				}
 				release.setDuration(TimeSpan.parse(duration));
 				release.setStatus(ReleaseStatus.valueOf(status));
 				releaseRepository.save(release);
 				return redirectToRelease(id, false);
 			case "Remove":
 				releaseRepository.delete(id);
 				return "redirect:/releases.html";
 			default:
 				throw new IllegalArgumentException("unknown submit " + submit);
 		}
 	}
 
 	@RequestMapping(value = "/releases/{id}/components", method = RequestMethod.POST)
 	public String addComponent(@PathVariable("id") String id, @RequestParam("component_id") String componentId, String version) {
 
 		final Release release = releaseRepository.findOne(id);
 
 		release.getComponents().add(new ReleaseComponent(componentRepository.findOne(componentId), version));
 
 		releaseRepository.save(release);
 
 		return redirectToRelease(id, true);
 	}
 
 	@RequestMapping(value = "/releases/{id}/components/{component_id}", method = RequestMethod.POST)
 	@Transactional
 	public String updateComponent(@PathVariable("id") String id, @PathVariable("component_id") String componentId, String version) {
 
 		final Release release = releaseRepository.findOne(id);
 
 		final Iterator<ReleaseComponent> it = release.getComponents().iterator();
 		while (it.hasNext()) {
 			final ReleaseComponent next = it.next();
 			if (next.getComponent().getId().equals(componentId)) {
 				it.remove();
 			}
 		}
 
 		releaseRepository.save(release);
 
 		return redirectToRelease(id, true);
 	}
 
 	private String redirectToRelease(String id, boolean edit) {
 		return "redirect:/releases/" + id + ".html?edit=" + edit;
 	}
 
 	@RequestMapping(value = "/releases/{id}/sign-offs", method = RequestMethod.POST)
 	@Transactional
 	public String addSignOff(@PathVariable("id") String id, @RequestParam("user_id") String userId) {
 
 		final Release release = releaseRepository.findOne(id);
 		final User user = userRepository.findOne(userId);
 
 		release.getSignOffs().put(user.getId(), new SignOff());
 
 		releaseRepository.save(release);
 
 		return redirectToRelease(id, true);
 	}
 
 	@RequestMapping(value = "/releases/{id}/sign-offs/{user_id}", method = RequestMethod.POST)
 	@Transactional
 	public String updateSignOff(@PathVariable("id") String id, @PathVariable("user_id") String userId,
 	                            @RequestParam(value = "status", required = false) String status,
 	                            @RequestParam(value = "action", required = false) String action) {
 
 		final Release release = releaseRepository.findOne(id);
 		if ("REMOVE".equals(action)) {
 			release.getSignOffs().remove(userRepository.findOne(userId).getId());
 		} else {
 			release.getSignOffs().get(userRepository.findOne(userId).getId()).setStatus(SignOffStatus.valueOf(status));
 		}
 
 		releaseRepository.save(release);
 
 		return redirectToRelease(id, true);
 	}
 
 	@RequestMapping("/releases/create")
 	public String create() {
 		return "releases/create";
 	}
 
 	@RequestMapping(value = "/releases", method = RequestMethod.POST)
 	@Transactional
 	public String releases(String submit, String name, String desc, String when, String duration) {
 		final Release release = new Release();
 		release.setName(name);
 		release.setWhen(Chronic.parse(when).getBeginCalendar().getTime());
 		release.setDuration(TimeSpan.parse(duration));
 		release.setDesc(desc);
 		releaseRepository.save(release);
 		return redirectToRelease(release.getId(), false);
 	}
 }
