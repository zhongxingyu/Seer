 package com.twistlet.falcon.controller;
 
 import static org.junit.Assert.*;
 
 import java.util.ArrayList;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.easymock.EasyMock;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.springframework.security.core.GrantedAuthority;
 import org.springframework.security.core.authority.SimpleGrantedAuthority;
 import org.springframework.web.servlet.ModelAndView;
 
 import com.twistlet.falcon.security.service.SecurityContextService;
 
 public class MainControllerTest {
 
 	private MainController unit;
 	private SecurityContextService securityContextService;
 	private Map<String, String> map;
 
 	@Before
 	public void init() {
 		securityContextService = EasyMock
 				.createStrictMock(SecurityContextService.class);
 		createMap();
 		unit = new MainController(securityContextService, map);
 	}
 
 	private void createMap() {
 		map = new LinkedHashMap<>();
 		map.put("ROLE_ADMIN", "redirect:/admin_page");
 		map.put("ROLE_STAFF", "redirect:/staff_page");
 		map.put("ROLE_PATIENT", "redirect:/patient_page");
 	}
 
 	@After
 	public void verify() {
 		EasyMock.verify(securityContextService);
 	}
 
 	@Test
 	public void testMainNoHits() throws Exception {
 		final List<GrantedAuthority> list = new ArrayList<GrantedAuthority>();
 		list.add(new SimpleGrantedAuthority("ROLE_SUPERVISOR"));
 		EasyMock.expect(securityContextService.getAuthorities())
 				.andReturn(list);
 		EasyMock.replay(securityContextService);
		final ModelAndView mav = unit.handleRequest(null, null);
 		assertNull(mav);
 	}
 
 	@Test
 	public void testMainSingleHitAdmin() throws Exception {
 		final List<GrantedAuthority> list = new ArrayList<GrantedAuthority>();
 		list.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
 		EasyMock.expect(securityContextService.getAuthorities())
 				.andReturn(list);
 		EasyMock.replay(securityContextService);
		final ModelAndView mav = unit.handleRequest(null, null);
 		assertEquals("redirect:/admin_page", mav.getViewName());
 	}
 
 	@Test
 	public void testMainSingleHitStaff() throws Exception {
 		final List<GrantedAuthority> list = new ArrayList<GrantedAuthority>();
 		list.add(new SimpleGrantedAuthority("ROLE_STAFF"));
 		EasyMock.expect(securityContextService.getAuthorities())
 				.andReturn(list);
 		EasyMock.replay(securityContextService);
		final ModelAndView mav = unit.handleRequest(null, null);
 		assertEquals("redirect:/staff_page", mav.getViewName());
 	}
 
 	@Test
 	public void testMainMultiHitAdminStaff() throws Exception {
 		final List<GrantedAuthority> list = new ArrayList<GrantedAuthority>();
 		list.add(new SimpleGrantedAuthority("ROLE_STAFF"));
 		list.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
 		EasyMock.expect(securityContextService.getAuthorities())
 				.andReturn(list);
 		EasyMock.replay(securityContextService);
		final ModelAndView mav = unit.handleRequest(null, null);
 		assertEquals("redirect:/admin_page", mav.getViewName());
 	}
 }
