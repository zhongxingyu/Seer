 /* Ara - capture species and specimen data
  *
  * Copyright (C) 2009  INBio ( Instituto Naciona de Biodiversidad )
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.inbio.ara.web.specimen;
 
 import com.sun.data.provider.impl.ObjectListDataProvider;
 import com.sun.rave.web.ui.appbase.AbstractSessionBean;
 import com.sun.webui.jsf.component.DropDown;
 import com.sun.webui.jsf.model.Option;
 import com.sun.webui.jsf.model.SingleSelectOptionsList;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Locale;
 import javax.ejb.EJB;
 import javax.faces.FacesException;
 import javax.faces.context.FacesContext;
 import org.inbio.ara.dto.LifeStageSexDTO;
 import org.inbio.ara.facade.specimen.SpecimenRemote;
 import org.inbio.ara.persistence.specimen.Specimen;
 import org.inbio.ara.persistence.specimen.SpecimenType;
 import org.inbio.ara.web.AraApplicationBean;
 import org.inbio.ara.web.ApplicationBean1;
 import org.inbio.ara.web.util.MessageBean;
 import org.inbio.ara.web.util.SelectionListBean;
 
 /**
  * <p>Session scope data bean for your application.  Create properties
  *  here to represent cached data that should be made available across
  *  multiple HTTP requests for an individual user.</p>
  *
  * <p>An instance of this class will be created for you automatically,
  * the first time your application evaluates a value binding expression
  * or method binding expression that references a managed bean using
  * this class.</p>
  *
  * @version EditSpecimenSessionBean.java
  * @version Created on 12/03/2009, 02:31:24 PM
  * @author asanabria
  */
 public class EditSpecimenSessionBean extends AbstractSessionBean {
 
     //FIXME
 	//private LifeStageDataProvider lifeStageSexDataProvider = new LifeStageDataProvider();
     private ObjectListDataProvider lifeStageSexDTODataProvier = new ObjectListDataProvider(new ArrayList<LifeStageSexDTO>());
 
     private LifeStageSexDTO actualLifeStageSexDTO = new LifeStageSexDTO();
 
     private Option[] lifeStageOption;
 	//private Long selectedLifeStage = -1L;
     //private String selectedLifeStageName="";
 	private Option[] sexOption = null;
 	//private Long selectedSex = -1L;
     //private String selectedSexName="";
 	private DropDown typeDD = new DropDown();
 	private SingleSelectOptionsList typeDDDefaultOptions = new SingleSelectOptionsList();
 
     @EJB
     private SpecimenRemote specimenBean;
 
 
 	public void populateOptions() {
 
 		Locale l = FacesContext.getCurrentInstance().getViewRoot().getLocale();
 
 
 		Specimen s;
 		if (this.getspecimen$SpecimenSessionBean().isInMultipleEdition()) {
 			s = this.mergeSpecimens();
 		} else {
 			s = this.getspecimen$SpecimenSessionBean().getCurrentSpecimen();
 		}
 
         //FIXME
 		//this.lifeStageSexDataProvider.refreshList(s.getId());
         this.setLifeStageSexDTODataProvier(s.getId());
 
 		// Estas listas corresponden a la generaci�n de identificaciones
 		this.setLifeStageOption(this.getSelectionListBean().getLifeStage());
 		this.setSexOption(this.getSelectionListBean().getSex());
 
 		//this.typeDDDefaultOptions.setOptions(this.getutil$SelectionListBean().getOptions(SpecimenType.class));
 		typeDD.setSelected((s.getSpecimenType() != null) ? s.getSpecimenType().getId() : 0L);
 		typeDDDefaultOptions.setOptions(this.getSelectionListBean().getOptions(SpecimenType.class, l));
 	}
     
     public void setLifeStageSexDTODataProvier(Long specimenId){
 
         //specimenBean.getSpecimenLifeStageSex(specimenId);
         List<LifeStageSexDTO> lssDTOList = specimenBean.getLifeStageSexDTOList(specimenId);
 
         this.lifeStageSexDTODataProvier = new ObjectListDataProvider(lssDTOList);
     }
 
 	/**
 	 * <p>Return a reference to the scoped data bean.</p>
 	 */
 	protected SpecimenSessionBean getspecimen$SpecimenSessionBean() {
 		return (SpecimenSessionBean) getBean("specimen$SpecimenSessionBean");
 	}
 
     /**
      *
      */
    void saveActualLifeStageSexDTO() {
        this.specimenBean.saveSpecimenLifeStageSex(this.getspecimen$SpecimenSessionBean().getCurrentSpecimen().getId(), this.actualLifeStageSexDTO);
     }
 
     /**
      * 
      */
     void deleteActualLifeStageSexDTO() {
         this.specimenBean.deleteSpecimenLifeStageSex(this.getspecimen$SpecimenSessionBean().getCurrentSpecimen().getId(), this.actualLifeStageSexDTO.getLifeStageKey(), this.actualLifeStageSexDTO.getSexKey());
     }
 
 	/**
 	 * Takes specimenList from SpecimenSessionBean and merge them to a single
 	 * Specimen object with the common charateristics of all the Specimens
 	 * objects passed through the function.
 	 * Whenever the SpecimenSessionBean.multipleEdition is true, the GUI shows
 	 * the information shared by all of them and keeps deselected but editable
 	 * the non-shared information.
 	 *
 	 * @return Specimen the information shared by all the specimens.
 	 */
 	private Specimen mergeSpecimens() {
 		Specimen s = new Specimen();
 		boolean firstIteration = true;
 		List<Specimen> listSpecimen = getspecimen$SpecimenSessionBean().getSpecimenList();
 		for (Specimen elem : listSpecimen) {
 			System.out.println("NOT YET IMPLEMENTED");
 			if (firstIteration) {
 				s = elem;
 			}
 			firstIteration = false;
 		}
 		return s;
 	}
 
 	// <editor-fold defaultstate="collapsed" desc="Managed Component Definition">
 	/**
 	 * <p>Automatically managed component initialization.  <strong>WARNING:</strong>
 	 * This method is automatically generated, so any user-specified code inserted
 	 * here is subject to being replaced.</p>
 	 */
 	private void _init() throws Exception {
 	}
 	// </editor-fold>
 
 	/**
 	 * <p>Construct a new session data bean instance.</p>
 	 */
 	public EditSpecimenSessionBean() {
 	}
 
 	/**
 	 * <p>This method is called when this bean is initially added to
 	 * session scope.  Typically, this occurs as a result of evaluating
 	 * a value binding or method binding expression, which utilizes the
 	 * managed bean facility to instantiate this bean and store it into
 	 * session scope.</p>
 	 *
 	 * <p>You may customize this method to initialize and cache data values
 	 * or resources that are required for the lifetime of a particular
 	 * user session.</p>
 	 */
 	@Override
 	public void init() {
 		//this.populateOptions();
 		// Perform initializations inherited from our superclass
 		super.init();
 		// Perform application initialization that must complete
 		// *before* managed components are initialized
 		// TODO - add your own initialiation code here
 
 		// <editor-fold defaultstate="collapsed" desc="Managed Component Initialization">
 		// Initialize automatically managed components
 		// *Note* - this logic should NOT be modified
 		try {
 			_init();
 		} catch (Exception e) {
 			log("EditSpecimenSessionBean Initialization Failure", e);
 			throw e instanceof FacesException ? (FacesException) e : new FacesException(e);
 		}
 
 	// </editor-fold>
 	// Perform application initialization that must complete
 	// *after* managed components are initialized
 	// TODO - add your own initialization code here
 	}
 
 	/**
 	 * <p>This method is called when the session containing it is about to be
 	 * passivated.  Typically, this occurs in a distributed servlet container
 	 * when the session is about to be transferred to a different
 	 * container instance, after which the <code>activate()</code> method
 	 * will be called to indicate that the transfer is complete.</p>
 	 *
 	 * <p>You may customize this method to release references to session data
 	 * or resources that can not be serialized with the session itself.</p>
 	 */
 	@Override
 	public void passivate() {
 	}
 
 	/**
 	 * <p>This method is called when the session containing it was
 	 * reactivated.</p>
 	 *
 	 * <p>You may customize this method to reacquire references to session
 	 * data or resources that could not be serialized with the
 	 * session itself.</p>
 	 */
 	@Override
 	public void activate() {
 	}
 
 	/**
 	 * <p>This method is called when this bean is removed from
 	 * session scope.  Typically, this occurs as a result of
 	 * the session timing out or being terminated by the application.</p>
 	 *
 	 * <p>You may customize this method to clean up resources allocated
 	 * during the execution of the <code>init()</code> method, or
 	 * at any later time during the lifetime of the application.</p>
 	 */
 	@Override
 	public void destroy() {
 	}
 
 	/**
 	 * <p>Return a reference to the scoped data bean.</p>
 	 *
 	 * @return reference to the scoped data bean
 	 */
 	protected AraApplicationBean getAraApplicationBean() {
 		return (AraApplicationBean) getBean("AraApplicationBean");
 	}
 
 	/**
 	 * <p>Return a reference to the scoped data bean.</p>
 	 *
 	 * @return reference to the scoped data bean
 	 */
 	protected ApplicationBean1 getApplicationBean1() {
 		return (ApplicationBean1) getBean("ApplicationBean1");
 	}
 
 	/**
 	 * <p>Return a reference to the scoped data bean.</p>
 	 *
 	 * @return reference to the scoped data bean
 	 */
 	protected MessageBean getutil$MessageBean() {
 		return (MessageBean) getBean("util$MessageBean");
 	}
 
 	/**
 	 * <p>Return a reference to the scoped data bean.</p>
 	 */
 	protected SelectionListBean getSelectionListBean() {
 		return (SelectionListBean) getBean("util$SelectionListBean");
 	}
 
     /**
      * @return the lifeStageSexDTODataProvier
      */
     public ObjectListDataProvider getLifeStageSexDTODataProvier() {
         return lifeStageSexDTODataProvier;
     }
 
     /**
      * @param lifeStageSexDTODataProvier the lifeStageSexDTODataProvier to set
      */
     public void setLifeStageSexDTODataProvier(ObjectListDataProvider lifeStageSexDTODataProvier) {
         this.lifeStageSexDTODataProvier = lifeStageSexDTODataProvier;
     }
 
     public SingleSelectOptionsList getTypeDDDefaultOptions() {
 		return typeDDDefaultOptions;
 	}
 
 	public void setTypeDDDefaultOptions(SingleSelectOptionsList ssol) {
 		this.typeDDDefaultOptions = ssol;
 	}
 
 	public DropDown getTypeDD() {
 		return typeDD;
 	}
 
 	public void setTypeDD(DropDown dd) {
 		this.typeDD = dd;
 	}
 
 	public Option[] getLifeStageOption() {
 		return lifeStageOption;
 	}
 
 	public void setLifeStageOption(Option[] lifeStageOption) {
 		this.lifeStageOption = lifeStageOption;
 	}
 
     //FIXME
 	//public LifeStageDataProvider getLifeStageSexDataProvider() {
 	//	return lifeStageSexDataProvider;
 	//}
     //FIXME
 	//public void setLifeStageSexDataProvider(LifeStageDataProvider lifeStageSexDataProvider) {
 	//	this.lifeStageSexDataProvider = lifeStageSexDataProvider;
 	//}
 
 	
 	public Option[] getSexOption() {
 		return sexOption;
 	}
 
 	public void setSexOption(Option[] sexOption) {
 		this.sexOption = sexOption;
 	}
 
     /**
      * @return the specimenBean
      */
     public SpecimenRemote getSpecimenBean() {
         return specimenBean;
     }
 
     /**
      * @param specimenBean the specimenBean to set
      */
     public void setSpecimenBean(SpecimenRemote specimenBean) {
         this.specimenBean = specimenBean;
     }
 
     /**
      * @return the actualLifeStageSexDTO
      */
     public LifeStageSexDTO getActualLifeStageSexDTO() {
         return actualLifeStageSexDTO;
     }
 
     /**
      * @param actualLifeStageSexDTO the actualLifeStageSexDTO to set
      */
     public void setActualLifeStageSexDTO(LifeStageSexDTO actualLifeStageSexDTO) {
         this.actualLifeStageSexDTO = actualLifeStageSexDTO;
     }
 
 }
