 package service.impl;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.primefaces.model.map.LatLng;
 import org.springframework.security.core.context.SecurityContextHolder;
 import org.springframework.security.ldap.userdetails.LdapUserDetailsImpl;
 import org.springframework.stereotype.Service;
 
 import service.ICadreService;
 import beans.Address;
 import beans.CadreBean;
 import beans.Personn;
 import beans.SectorBean;
 import beans.Visite;
 
 @Service("defaultCadreService")
 public class DefaultCadreService implements ICadreService {
 
 	private static List<CadreBean> cadres = new ArrayList<CadreBean>();
 
 	public DefaultCadreService(){
 		createCadre1();
 		createCadre2();
 	}
 
 	private void createCadre1() {
 		CadreBean cadreBean = new CadreBean();
 		cadreBean.setName("Pradiera");
 		cadreBean.setSurname("Paul");
 		cadreBean.setSsid("12345");
		cadreBean.setLogin("pradiera");
 		cadreBean.setBirthdate(new Date());
 		cadreBean.setAddress(new Address("42, avenue du nonsens", 101010,
 				"Pandemonium"));
 		cadreBean.setPhone("0667854630");
 		cadreBean.setReferPersonn(new Personn("Pradier", "Paulette", "0623456988",
 				new Address("14bis rue de la paix", 75001, "Paris")));
 		cadreBean.setLatLng(new LatLng(48.839399, 2.587119));
 		
 		List<SectorBean> sectors = new ArrayList<SectorBean>();
 		sectors.add(new SectorBean("Gagny"));
 		sectors.add(new SectorBean("Champs"));
 		
 		cadreBean.setSectors(sectors);
 		
 		Set<Integer> workDay = new HashSet<Integer>();
 		workDay.add(Calendar.MONDAY);
 		workDay.add(Calendar.TUESDAY);
 		workDay.add(Calendar.WEDNESDAY);
 		workDay.add(Calendar.THURSDAY);
 		workDay.add(Calendar.FRIDAY);
 		
 		cadreBean.setDaysOfWeek(workDay);
 		
 		cadres.add(cadreBean);
 	}
 
 	private void createCadre2() {
 		CadreBean cadreBean = new CadreBean();
 		cadreBean.setName("Petiot");
 		cadreBean.setSurname("Anthonyo");
		cadreBean.setSsid("54321");
		cadreBean.setLogin("petiot");
 		cadreBean.setBirthdate(new Date());
 		cadreBean.setAddress(new Address("42, avenue du nonsens", 101010,
 				"Pandemonium"));
 		cadreBean.setPhone("0667854631");
 		cadreBean.setReferPersonn(new Personn("Petiot", "Anthonette", "0623456989",
 				new Address("14 rue de la paix", 75001, "Paris")));
 		cadreBean.setLatLng(new LatLng(48.839399, 2.587119));
 		
 		List<SectorBean> sectors = new ArrayList<SectorBean>();
 		sectors.add(new SectorBean("Gagny"));
 		sectors.add(new SectorBean("Champs"));
 		
 		cadreBean.setSectors(sectors);
 		
 		Set<Integer> workDay = new HashSet<Integer>();
 		workDay.add(Calendar.MONDAY);
 		workDay.add(Calendar.TUESDAY);
 		workDay.add(Calendar.WEDNESDAY);
 		workDay.add(Calendar.THURSDAY);
 		workDay.add(Calendar.FRIDAY);
 		
 		cadreBean.setDaysOfWeek(workDay);
 		
 		cadres.add(cadreBean);
 	}
 
 	@Override
 	public List<CadreBean> getAllCadre() {
 		return cadres;
 	}
 
 	@Override
 	public void removeCadre(CadreBean cadre) {
 		cadres.remove(cadre);
 	}
 
 	@Override
 	public void addCadre(CadreBean cadre) {
 		cadres.add(cadre);
 	}
 
 	@Override
 	public void editCadre(CadreBean oldCadre, CadreBean newCadre) {
 		System.out.println(oldCadre);
 		System.out.println(newCadre);
 		int index = 0;
 		for (CadreBean cadre : cadres) {
 			if (cadre.getSsid().equals(oldCadre.getSsid())) {
 				break;
 			}
 			index++;
 		}
 		// int index = patients.indexOf(oldPatient);
 		cadres.remove(index);
 		cadres.add(index, newCadre);
 	}
 	
 	@Override
 	public CadreBean clone(CadreBean cadre) {
 		return new CadreBean(cadre);
 	}
 
 	@Override
 	public CadreBean getConnectedCadre() {
 		LdapUserDetailsImpl principal = (LdapUserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
 		String username = principal.getUsername();
 		for(CadreBean cb : cadres){
 			if(username.equals(cb.getLogin())){
 				return cb;
 			}
 		}
 		return null;
 	}
 
 	@Override
 	public List<Visite> getVisites(CadreBean cb) {
 		return cb.getVisitList();
 	}
 
 }
