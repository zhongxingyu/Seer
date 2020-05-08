 package ru.xrm.app.domain;
 
import java.util.Comparator;

 public class VacancyPage extends DomainObject implements Comparable{
 
 	protected String href;
 	
 	public VacancyPage(){
 		
 	}
 	
 	public VacancyPage(String href){
 		this.href=href;
 	}
 
 	public String getHref() {
 		return href;
 	}
 
 	public void setHref(String href) {
 		this.href = href;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 	
 		if (this==obj){
 			return true;
 		}
 		
 		if (obj==null || !(this.getClass() != obj.getClass())){
 			return false;
 		}
 		VacancyPage vp=(VacancyPage)obj;
 		return this.href.equals(vp.getHref());
 	}
 
 	public int compareTo(Object obj) {
 		if (this==obj){
 			return 0;
 		}
 		
 		if (obj==null || !(this.getClass() != obj.getClass())){
 			return -1;
 		}
 		
 		VacancyPage vp=(VacancyPage)obj;
 		
 		return this.href.compareTo(vp.getHref());
 	}
 	
 }
