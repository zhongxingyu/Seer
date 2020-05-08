 package idv.web.controller;
 
 import idv.constant.Gender;
 import idv.model.Member;
 import idv.to.Page;
 import idv.util.WebUtil;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Locale;
 import java.util.UUID;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.data.domain.Pageable;
 import org.springframework.data.domain.Sort;
 import org.springframework.data.web.PageableDefaults;
 import org.springframework.http.HttpStatus;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.bind.annotation.ResponseStatus;
 
 @Controller
 public class MemberController {
 
 	private static final Logger log = LoggerFactory.getLogger(MemberController.class);
 	
 	@RequestMapping(value="/members", method = RequestMethod.GET)
 	@ResponseBody
 	public Page<Member> get(@PageableDefaults(pageNumber = 0, value = 10) Pageable pageable, HttpServletRequest request) {
 		log.debug("{} get members.", WebUtil.getUserAddress(request));
 		WebUtil.logParameters(request);
		/*try {
 			Thread.sleep(2000);
 		} catch (InterruptedException e) {
 			log.error(e.getMessage(), e);
		}*/
 		List<Member> members = getMembers(request);
 		if (pageable != null) {
 			return page(members, pageable);
 		}
 		return new Page<Member>(members);
 	}
 	
 	@RequestMapping(value="/members/edit", method = RequestMethod.POST)
 	@ResponseStatus(HttpStatus.OK)
 	public void edit(@ModelAttribute("member") Member member, HttpServletRequest request) {
 		log.debug("{} edit member.", WebUtil.getUserAddress(request));
 		WebUtil.logParameters(request);
 		save(getMembers(request), member);
 	}
 	
 	@RequestMapping(value="/members/delete", method = RequestMethod.POST)
 	@ResponseStatus(HttpStatus.OK)
 	public void remove(@RequestParam(value = "id", required = false) String id, HttpServletRequest request) {
 		log.debug("{} remove member {}.", WebUtil.getUserAddress(request), id);
 		WebUtil.logParameters(request);
 		String[] ids = id.split(",");
 		for (String idToDel: ids) {
 			delete(getMembers(request), idToDel);
 		}
 	}
 	
 	@RequestMapping(value="/members", method = RequestMethod.POST)
 	@ResponseStatus(HttpStatus.OK)
 	public void add(@ModelAttribute("member") Member member, HttpServletRequest request) {
 		log.debug("{} post member {}.", WebUtil.getUserAddress(request), member.getName());
 		WebUtil.logParameters(request);
 		save(getMembers(request), member);
 	}
 	
 	@RequestMapping(value="/members", method = RequestMethod.PUT)
 	@ResponseStatus(HttpStatus.OK)
 	public void update(@ModelAttribute("member") Member member, HttpServletRequest request) {
 		log.debug("{} put member {}.", WebUtil.getUserAddress(request), member.getId());
 		WebUtil.logParameters(request);
 		save(getMembers(request), member);
 	}
 	
 	@RequestMapping(value="/members/{id}", method = RequestMethod.PUT)
 	@ResponseStatus(HttpStatus.OK)
 	public void update(@PathVariable String id, @ModelAttribute("member") Member member, HttpServletRequest request) {
 		log.debug("{} put member {}.", WebUtil.getUserAddress(request), id);
 		WebUtil.logParameters(request);
 		member.setId(id);
 		save(getMembers(request), member);
 	}
 	
 	@RequestMapping(value="/members/{id}", method = RequestMethod.DELETE)
 	@ResponseStatus(HttpStatus.OK)
 	public void delete(@PathVariable String id, HttpServletRequest request) {
 		log.debug("{} delete member {}.", WebUtil.getUserAddress(request), id);
 		WebUtil.logParameters(request);
 		delete(getMembers(request), id);
 	}
 
 	protected List<Member> getMembers(HttpServletRequest request) {
 		HttpSession session = request.getSession();
 		
 		@SuppressWarnings("unchecked")
 		List<Member> members = (List<Member>) session.getAttribute("members");
 		
 		if (members == null || members.isEmpty()) {
 			members = createMembers();
 			session.setAttribute("members", members);
 		}
 		return members;
 	}
 	
 	protected Page<Member> page(List<Member> members, Pageable pageable) {
 		int pageNo = pageable.getPageNumber();
 		int pageSize = pageable.getPageSize();
 		Sort sort = pageable.getSort();
 		
 		if (sort != null) {
 			Iterator<Sort.Order> ite = sort.iterator();
 			while (ite.hasNext()) {
 				Sort.Order sortOrder = ite.next();
 				sort(members, sortOrder);
 				break;
 			}
 		}
 
 		List<Member> rtMembers = new ArrayList<Member>();
 		int total = members.size();
 		int i = pageNo * pageSize;
 		i = (i < 0 ? 0 : i);
 		i = (i >= total ? 0 : i);
 		int len = i + pageSize;
 		len = (len > total ? total : len);
 		for (; i < len; i++) {
 			Member member = members.get(i);
 			rtMembers.add(member);
 		}
 		
 		Page<Member> page = new Page<Member>(rtMembers);
 		page.setPage(pageNo);
 		page.setPageSize(pageSize);
 		page.setTotalRecords(total);
 		return page;
 	}
 	
 	protected Member save(List<Member> members, Member editedMember) {
 		String id = editedMember.getId();
 		if (id != null && !id.equals("") && !id.equals("0")) {
 			int idxToUpdate = -1;
 			for (int i = 0; i < members.size(); i++) {
 				Member member = members.get(i);
 				if (id.equals(member.getId())) {
 					idxToUpdate = i;
 					break;
 				}
 			}
 			
 			if (idxToUpdate >= 0) {
 				log.debug("Update member {}.", id);
 				members.remove(idxToUpdate);
 				members.add(idxToUpdate, editedMember);
 				return editedMember;
 			}
 		}
 		
 		log.debug("Add member {}.", editedMember.getName());
 		editedMember.setId(UUID.randomUUID().toString());
 		members.add(editedMember);
 		return editedMember;
 	}
 	
 	protected Member delete(List<Member> members, String id) {
 		if (id == null || id.equals("")) {
 			return null;
 		}
 		
 		int idxToDel = -1;
 		for (int i = 0; i < members.size(); i++) {
 			Member member = members.get(i);
 			if (id.equals(member.getId())) {
 				idxToDel = i;
 				break;
 			}
 		}
 		
 		if (idxToDel >= 0) {
 			log.debug("Delete member {}.", id);
 			Member member = members.remove(idxToDel);
 			return member;
 		}
 		return null;
 	}
 	
 	protected void sort(List<Member> members, Sort.Order sortOrder) {
 		final String prop = sortOrder.getProperty();
 		final int isDesc = sortOrder.getDirection() == Sort.Direction.DESC ? -1 : 1;
 		Collections.sort(members, new Comparator<Member>() {
 			@Override
 			public int compare(Member o1, Member o2) {
 				if ("name".equals(prop)) {
 					return o1.getName().compareTo(o2.getName()) * isDesc;
 				}
 				if ("birthday".equals(prop)) {
 					if (o1.getBirthday() == null) {
 						return -1 * isDesc;
 					}
 					if (o2.getBirthday() == null) {
 						return 1 * isDesc;
 					}
 					return (o1.getBirthday().after(o2.getBirthday()) ? 1 : -1) * isDesc;
 				}
 				if ("sex".equals(prop)) {
 					Gender g1 = Gender.get(o1.getSex());
 					Gender g2 = Gender.get(o2.getSex());
 					return g1.compareTo(g2) * isDesc;
 				}
 				if ("language".equals(prop)) {
 					String c1 = o1.getLanguage();
 					String c2 = o2.getLanguage();
 					if (c1 == null) {
 						return -1 * isDesc;
 					}
 					if (c2 == null) {
 						return -1 * isDesc;
 					}
 					Locale l1 = new Locale(c1);
 					Locale l2 = new Locale(c2);
 					// use Locale.US to display English not Chinese(default local)
 					return l1.getDisplayLanguage(Locale.US).compareTo(l2.getDisplayLanguage(Locale.US)) * isDesc;
 				}
 				return o1.getName().compareTo(o2.getName()) * isDesc;
 			}
 		});
 	}
 	
 	protected List<Member> createMembers() {
 		List<Member> members = new ArrayList<Member>();
 		Member member = new Member("David", "david@samples.com", Gender.Male.value(), Locale.FRANCE.getLanguage());
 		member.setId(UUID.randomUUID().toString());
 		members.add(member);
 		member = new Member("Alice", "alice@samples.com", Gender.Female.value(), Locale.US.getLanguage());
 		member.setId(UUID.randomUUID().toString());
 		members.add(member);
 		member = new Member("Anny", "anny@samples.com", Gender.Female.value(), Locale.US.getLanguage());
 		member.setId(UUID.randomUUID().toString());
 		members.add(member);
 		member = new Member("James", "james@samples.com", Gender.Male.value(), Locale.US.getLanguage());
 		member.setId(UUID.randomUUID().toString());
 		members.add(member);
 		member = new Member("John", "john@samples.com", Gender.Male.value(), Locale.US.getLanguage());
 		member.setId(UUID.randomUUID().toString());
 		members.add(member);
 		member = new Member("Bob", "bob@samples.com", Gender.Male.value(), Locale.US.getLanguage());
 		member.setId(UUID.randomUUID().toString());
 		members.add(member);
 		member = new Member("Jane", "jane@samples.com", Gender.Female.value(), Locale.CHINESE.getLanguage());
 		member.setId(UUID.randomUUID().toString());
 		members.add(member);
 		member = new Member("Jack", "jack@samples.com", Gender.Male.value(), Locale.US.getLanguage());
 		member.setId(UUID.randomUUID().toString());
 		members.add(member);
 		member = new Member("vivian", "vivian@samples.com", Gender.Female.value(), Locale.CHINESE.getLanguage());
 		member.setId(UUID.randomUUID().toString());
 		members.add(member);
 		member = new Member("Claire", "claire@samples.com", Gender.Female.value(), Locale.FRANCE.getLanguage());
 		member.setId(UUID.randomUUID().toString());
 		members.add(member);
 		member = new Member("Amy", "amy@samples.com", Gender.Female.value(), Locale.JAPAN.getLanguage());
 		member.setId(UUID.randomUUID().toString());
 		members.add(member);
 		member = new Member("Lisa", "lisa@samples.com", Gender.Female.value(), Locale.US.getLanguage());
 		member.setId(UUID.randomUUID().toString());
 		members.add(member);
 		return members;
 	}
 }
