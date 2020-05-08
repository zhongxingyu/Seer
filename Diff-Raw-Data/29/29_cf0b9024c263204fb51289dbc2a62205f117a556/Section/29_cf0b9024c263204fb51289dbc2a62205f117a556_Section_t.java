 /*
  * @(#)Section.java
  *
  * Copyright 2009 Instituto Superior Tecnico
  * Founding Authors: João Figueiredo, Luis Cruz, Paulo Abrantes, Susana Fernandes
  * 
  *      https://fenix-ashes.ist.utl.pt/
  * 
  *   This file is part of the Contents Module for the MyOrg web application.
  *
  *   The Contents Module is free software: you can redistribute it and/or modify
  *   it under the terms of the GNU Lesser General Public License as published
  *   by the Free Software Foundation, either version 3 of the License, or
  *   (at your option) any later version.*
  *
  *   The Contents Module is distributed in the hope that it will be useful,
  *   but WITHOUT ANY WARRANTY; without even the implied warranty of
  *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  *   GNU Lesser General Public License for more details.
  *
  *   You should have received a copy of the GNU Lesser General Public License
  *   along with the Contents Module. If not, see <http://www.gnu.org/licenses/>.
  * 
  */
 
 package module.contents.domain;
 
 import java.io.Serializable;
 import java.util.Comparator;
 
 import myorg.domain.MyOrg;
 import pt.ist.fenixWebFramework.services.Service;
 import pt.ist.fenixWebFramework.util.DomainReference;
 import pt.utl.ist.fenix.tools.util.i18n.MultiLanguageString;
 
 public class Section extends Section_Base {
 
     public static final Comparator<Section> COMPARATOR_BY_ORDER = new Comparator<Section>() {
 
 	@Override
 	public int compare(final Section section1, final Section section2) {
 	    final int c = section1.getSectionOrder().compareTo(section2.getSectionOrder());
 	    return c == 0 ? section1.hashCode() - section2.hashCode() : c;
 	}
 
     };
 
     public static class SectionBean implements Serializable {
 
 	private static final long serialVersionUID = 1L;
 
 	MultiLanguageString title;
 	MultiLanguageString contents;
 	DomainReference<Page> page;
 
 	public SectionBean(final Page page) {
 	    setPage(page);
 	}
 
 	public MultiLanguageString getTitle() {
 	    return title;
 	}
 	public void setTitle(MultiLanguageString title) {
 	    this.title = title;
 	}
 	public MultiLanguageString getContents() {
 	    return contents;
 	}
 	public void setContents(MultiLanguageString contents) {
 	    this.contents = contents;
 	}
 	public Page getPage() {
 	    return page == null ? null : page.getObject();
 	}
 	public void setPage(Page page) {
 	    this.page = page == null ? null : new DomainReference<Page>(page);
 	}
     }
 
     public Section() {
         super();
         setMyOrg(MyOrg.getInstance());
     }
 
     public Section(final SectionBean sectionBean) {
 	this();
 	setTitle(sectionBean.getTitle());
 	setContents(sectionBean.getContents());
 	setPage(sectionBean.getPage());
     }
 
     @Service
     public static void createNewSection(final SectionBean sectionBean) {
 	new Section(sectionBean);
     }
 
     @Override
     public void setPage(final Page page) {
 	if (page != null) {
 	    final int order = page.getSectionsCount() + 1;
 	    setSectionOrder(Integer.valueOf(order));
 	}
 	super.setPage(page);
     }
 
     @Service
     public void delete() {
 	removePage();
 	removeMyOrg();
	deleteDomainObject();
     }
     
 }
