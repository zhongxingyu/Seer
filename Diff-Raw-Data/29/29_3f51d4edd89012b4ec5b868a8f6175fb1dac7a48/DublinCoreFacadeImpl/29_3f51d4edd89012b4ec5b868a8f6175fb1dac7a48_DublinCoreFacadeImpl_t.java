 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package org.inbio.commons.dublincore.facade.ara.impl;
 
 
 import java.util.ArrayList;
 import java.util.GregorianCalendar;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import javax.annotation.PostConstruct;
 import javax.ejb.EJB;
 import javax.ejb.Stateless;
 
 
 import org.inbio.commons.dublincore.dto.DublinCoreDTO;
 
 
 import org.inbio.commons.dublincore.dto.ElementTypeDTO;
 
 import org.inbio.commons.dublincore.dto.ara.InterfaceDublinCoreDTO;
 import org.inbio.commons.dublincore.dto.ara.ReferenceDTO;
 import org.inbio.commons.dublincore.eao.ara.DublinCoreDescriptionEAOLocal;
 import org.inbio.commons.dublincore.eao.ara.DublinCoreElementEAOLocal;
 import org.inbio.commons.dublincore.facade.ara.DublinCoreFacadeRemote;
 import org.inbio.commons.dublincore.manager.impl.DublinCoreMetadataManagerImpl;
 import org.inbio.commons.dublincore.model.DublinCoreDescription;
 import org.inbio.commons.dublincore.model.DublinCoreElement;
 import org.inbio.commons.dublincore.model.DublinCoreElementEnum;
 import org.inbio.commons.dublincore.model.ResourceTypeEnum;
 import org.inbio.commons.dublincore.persistence.ara.DublinCoreDescriptionJPA;
 import org.inbio.commons.dublincore.persistence.ara.DublinCoreElementJPA;
 
 
 /**
  *
  * @author gsulca
  */
 //@Stateless
 @Stateless(name="DublinCoreFacadeImpl")
 public class DublinCoreFacadeImpl extends DublinCoreMetadataManagerImpl implements DublinCoreFacadeRemote{
 
 
         @EJB(beanName="DublinCoreElementEAOImpl")
         DublinCoreElementEAOLocal dublinCoreElementDAOImpl;
        
         @EJB(beanName="DublinCoreDescriptionEAOImpl")
 	DublinCoreDescriptionEAOLocal dublinCoreDescriptionDAOImpl;
 
         @PostConstruct
         public void DublinCoreFacadeImpl()
         {
             super.setDublinCoreDescriptionDAOImpl(dublinCoreDescriptionDAOImpl);
             super.setDublinCoreElementDAOImpl(dublinCoreElementDAOImpl);
         }
 
 
         
     public Long countSimpleSearch(String query) {
          Integer quantity = new Integer(unstructeredReferenceQuery(splitQuery(query)).size());
          return quantity.longValue();
     }
          
         private Set<Integer> unstructeredReferenceQuery(String[] parts)
         {
             Set<Integer> list = new HashSet();
             List<Integer> ids = null;
 
 
 
             for (int i = 0; i < parts.length; i++)
             {
                 //try to cast it
                 try
                 {
                     //find by title
                     ids = dublinCoreElementDAOImpl.findByValue(DublinCoreElementEnum.TITLE.getId(),parts[i]);
                     if(ids != null && !ids.isEmpty())
                         list.addAll(ids);
 
                     //find by creator
                     ids = dublinCoreElementDAOImpl.findByValue(DublinCoreElementEnum.CREATOR.getId(),parts[i]);;
                     if(ids != null && !ids.isEmpty())
                         list.addAll(ids);
 
                     //find by date
                     ids = dublinCoreElementDAOImpl.findByValue(DublinCoreElementEnum.DATE.getId(),parts[i]);
                     if(ids != null && !ids.isEmpty())
                         list.addAll(ids);
 
                     //find by animal code
                     ids = dublinCoreElementDAOImpl.findByValue(DublinCoreElementEnum.IDENTIFIER.getId(),parts[i]);
                     if(ids != null && !ids.isEmpty())
                         list.addAll(ids);
 
                 }
                 catch(Exception e){}
             }
             return list;
         }
 
 
        public List<DublinCoreDTO> getReferenceSimpleSearch(String query, int firstResult, int maxResult) {
 
         Set<Integer> resourceIds = unstructeredReferenceQuery(splitQuery(query));        
         List<DublinCoreDTO> result = new ArrayList();
 
         for(Integer resourceId: resourceIds)
         {
             result.add(this.getMetadataByResourceKey(resourceId.toString()));
         }
        System.out.println("Mata: "+result.size()+" Lista de entidades ");
        System.out.println("Mata: "+resourceIds.size()+" Lista de identificadores ");
        System.out.println(" ------- o -------");
         return result;
     }
 
        @Deprecated //use findAllDublinCorePaginated
        public List<DublinCoreDTO> getAllDublinCorePaginated(int firstResult, int maxResult) {
         
           
        List<DublinCoreDTO> result = new ArrayList();
        List<DublinCoreDescription> allReferences = new ArrayList();
        allReferences = dublinCoreDescriptionDAOImpl.findAllPaginated(ResourceTypeEnum.REFERENCE.getId(), firstResult, maxResult);
        
        for(DublinCoreDescription referenceEntity: allReferences)
        {
            result.add(this.getMetadataByResourceKey(referenceEntity.getResourceId().toString()));
        }
        return result;
     }
 
        public List<DublinCoreDTO> findAllDublinCorePaginated(int resourceTypeId, int firstResult, int maxResult) {
 
 
            List<DublinCoreDTO> result = new ArrayList();
            List<DublinCoreDescription> allReferences = new ArrayList();
            allReferences = dublinCoreDescriptionDAOImpl.findAllPaginated(resourceTypeId, firstResult, maxResult);
 
            for(DublinCoreDescription referenceEntity: allReferences)
            {
                result.add(this.getMetadataByResourceKey(referenceEntity.getResourceId().toString()));
            }
            return result;
         }
 
      /**
      * Split the query
      * @param query
      * @return
      */
     private String[] splitQuery(String query)
     {
             if(query == null || query.length() == 0)
                 return null;
             return query.split(" ");
     }
 
 
     public List<DublinCoreDTO> getDublinCoreAdvancedSearch(DublinCoreDTO dublinCoreDTO, int firstResult, int maxResult) {
         Set<Integer> resourceIds = getDublinCoreByCriteria(dublinCoreDTO);
 
         //Retrieve entities
         List<DublinCoreDTO> result = new ArrayList();
 
         for(Integer resourceId: resourceIds)
         {
             result.add(this.getMetadataByResourceKey(resourceId.toString()));
         }
         //return updateSementalDTOListValues(sementalDTOFactory.createDTOList(list));
         return result;
 
     }
 
     public Long countDublinCoreAdvancedSearch(DublinCoreDTO dublinCoreDTO) {
         Integer quantity = new Integer(getDublinCoreByCriteria(dublinCoreDTO).size());
         return quantity.longValue();
     }
 
     /**
      * Use this method for sementals advance search:
      * getElementsByCriteria: title, creator, identifier, date
      * @param dublinCoreDTO
      * @return  Set<Integer>
      */
     public Set<Integer> getDublinCoreByCriteria(DublinCoreDTO dublinCoreDTO)
     {
          Set<Integer> ids = new HashSet();
         boolean firstTime = true;
 
         List<Integer> query = new ArrayList<Integer>();
 
         //find by title
         if(dublinCoreDTO.getElementValues("title") != null)
         {
             query = dublinCoreElementDAOImpl.findByValue(DublinCoreElementEnum.TITLE.getId(),
                                                           dublinCoreDTO.getElementValues("title").get(0).getValue());
             if(query != null && !query.isEmpty())
             {
                 ids.addAll(query);
                 firstTime = false;
             }
         }
 
         //find by creator
         if(dublinCoreDTO.getElementValues("creator") != null)
         {
             query = dublinCoreElementDAOImpl.findByValue(DublinCoreElementEnum.CREATOR.getId(),
                                                           dublinCoreDTO.getElementValues("creator").get(0).getValue());
             if(query != null && !query.isEmpty())
             {
                 if(firstTime)
                 {
                     ids.addAll(query);
                     firstTime = false;
                 }
                 else
                     ids.retainAll(query);
             }
         }
 
         //find by identifier
         if(dublinCoreDTO.getElementValues("identifier") != null)
         {
             query = dublinCoreElementDAOImpl.findByValue(DublinCoreElementEnum.IDENTIFIER.getId(),
                                                           dublinCoreDTO.getElementValues("identifier").get(0).getValue());
             if(query != null && !query.isEmpty())
             {
                 if(firstTime)
                 {
                     ids.addAll(query);
                     firstTime = false;
                 }
                 else
                     ids.retainAll(query);
             }
         }
 
         //find by date
         if(dublinCoreDTO.getElementValues("date") != null)
         {
             query = dublinCoreElementDAOImpl.findByValue(DublinCoreElementEnum.DATE.getId(),
                                                           dublinCoreDTO.getElementValues("date").get(0).getValue());
             if(query != null && !query.isEmpty())
             {
                 if(firstTime)
                 {
                     ids.addAll(query);
                     firstTime = false;
                 }
                 else
                     ids.retainAll(query);
             }
         }
 
         return ids;
     }
 
 
     public List<ReferenceDTO> dublinCoreDTOsToReferenceDTOs (List<DublinCoreDTO> list)
     {
         List<ReferenceDTO> result = new ArrayList();
         for(DublinCoreDTO element: list)
         {
             result.add(dublinCoreDTOToReferenceDTO(element));
         }
         return result;
     }
 
     public ReferenceDTO dublinCoreDTOToReferenceDTO (DublinCoreDTO element)
     {
         
         ReferenceDTO result = new ReferenceDTO();
         
         result.setKey(element.getKey());
         
         /*If title exist*/
         if(element.getElementValues("title") != null && element.getElementValues("title").size() > 0)
         {
             result.setTitle(element.getElementValues("title").get(0).getValue());
         }
 
         /*If creator exist*/
         if(element.getElementValues("creator") != null && element.getElementValues("creator").size() > 0)
         {
             String creators = "";          
             for(ElementTypeDTO creatorName: element.getElementValues("creator"))
             {
                 creators += creatorName.getValue()+", ";
             }
             result.setCreator(creators);           
         }
 
         /*If date exist*/
         if(element.getElementValues("date") != null && element.getElementValues("date").size() > 0)
         {
             result.setDate(element.getElementValues("date").get(0).getValue());
         }
 
         /*If identifier exist*/        
         if(element.getElementValues("identifier") != null && element.getElementValues("identifier").size() > 0)
         {
             
             result.setIdentifier(element.getElementValues("identifier").get(0).getValue());
         }
         return result;
     }
 
 
 
     public List<ReferenceDTO> dublinCoreDTOsToFullReferenceDTOs (List<DublinCoreDTO> list)
     {
         List<ReferenceDTO> result = new ArrayList();
         for(DublinCoreDTO element: list)
         {
             result.add(dublinCoreDTOToFullReferenceDTO(element));
         }
         return result;
     }
 
     public ReferenceDTO dublinCoreDTOToFullReferenceDTO (DublinCoreDTO element)
     {
 
         ReferenceDTO result = new ReferenceDTO();
 
         result.setKey(element.getKey());
 
         /*If title exist*/
         if(element.getElementValues("title") != null)
         {
             String titles = "";
             for(ElementTypeDTO titlesName: element.getElementValues("title"))
             {
                 titles += titlesName.getValue()+"; ";
             }
             result.setTitle(titles);
         }
 
         /*If creator exist*/
         if(element.getElementValues("creator") != null)
         {
             String creators = "";
             for(ElementTypeDTO creatorName: element.getElementValues("creator"))
             {
                 creators += creatorName.getValue()+"; ";
             }
             result.setCreator(creators);
         }
 
         /*If date exist*/
         if(element.getElementValues("date") != null)
         {
             String dates = "";
             for(ElementTypeDTO dateName: element.getElementValues("date"))
             {
                 dates += dateName.getValue()+"; ";
             }
             result.setDate(dates);
         }
 
         /*If identifier exist*/
         if(element.getElementValues("identifier") != null)
         {
             String identifiers = "";
             for(ElementTypeDTO identifierName: element.getElementValues("identifier"))
             {
                 identifiers += identifierName.getValue()+"; ";
             }
             result.setIdentifier(identifiers);
         }
         return result;
     }
 
     public Long countResourceByTypeId(int typeId) {
         return dublinCoreDescriptionDAOImpl.countResourceByTypeId(typeId);
     }
 
     public void saveDublinCore(InterfaceDublinCoreDTO interfaceDublinCoreDTO) {
      
         DublinCoreDescriptionJPA dublinCoreDescriptionJPA = saveDublinCoreDescription(interfaceDublinCoreDTO);
 
         saveDublinCoreElements(dublinCoreDescriptionJPA.getResourceId(), interfaceDublinCoreDTO);
 
     }
 
     public DublinCoreDescriptionJPA saveDublinCoreDescription(InterfaceDublinCoreDTO interfaceDublinCoreDTO)
     {
         DublinCoreDescriptionJPA dublinCoreDescriptionJPA = new DublinCoreDescriptionJPA();
 
         dublinCoreDescriptionJPA.setDescription(interfaceDublinCoreDTO.getResourceTypeDescription());
         dublinCoreDescriptionJPA.setResourceTypeId(interfaceDublinCoreDTO.getResourceTypeId().intValue());
 
         dublinCoreDescriptionJPA.setCreatedBy(interfaceDublinCoreDTO.getUserName());
         dublinCoreDescriptionJPA.setCreationDate(new GregorianCalendar());
         dublinCoreDescriptionJPA.setLastModificationBy(interfaceDublinCoreDTO.getUserName());
         dublinCoreDescriptionJPA.setLastModificationDate(new GregorianCalendar());
 
         dublinCoreDescriptionDAOImpl.create(dublinCoreDescriptionJPA);
         return dublinCoreDescriptionJPA;
     }
 
     public void saveDublinCoreElements(Integer resourceId, InterfaceDublinCoreDTO interfaceDublinCoreDTO)
     {
         String[] elements = null;
 
         //luego los fors y las persistencias
         if(interfaceDublinCoreDTO.getTitle() != null)
         {
             elements = interfaceDublinCoreDTO.getTitle().split(";");
             for(int i = 0; i < elements.length; i++)
                 fillAndPersistDublinCoreelement(DublinCoreElementEnum.TITLE.getId(),elements[i],interfaceDublinCoreDTO,resourceId);
         }
 
         elements = null;
         if(interfaceDublinCoreDTO.getCreator() != null)
         {
             elements = interfaceDublinCoreDTO.getCreator().split(";");
             for(int i = 0; i < elements.length; i++)
                 fillAndPersistDublinCoreelement(DublinCoreElementEnum.CREATOR.getId(),elements[i],interfaceDublinCoreDTO,resourceId);
         }
 
         elements = null;
         if(interfaceDublinCoreDTO.getSubject() != null)
         {
             elements = interfaceDublinCoreDTO.getSubject().split(";");
             for(int i = 0; i < elements.length; i++)
                 fillAndPersistDublinCoreelement(DublinCoreElementEnum.SUBJECT.getId(),elements[i],interfaceDublinCoreDTO,resourceId);
         }
 
         elements = null;
         if(interfaceDublinCoreDTO.getDescription() != null)
         {
             elements = interfaceDublinCoreDTO.getDescription().split(";");
             for(int i = 0; i < elements.length; i++)
                 fillAndPersistDublinCoreelement(DublinCoreElementEnum.DESCRIPTION.getId(),elements[i],interfaceDublinCoreDTO,resourceId);
         }
 
         elements = null;
         if(interfaceDublinCoreDTO.getPublisher() != null)
         {
             elements = interfaceDublinCoreDTO.getPublisher().split(";");
             for(int i = 0; i < elements.length; i++)
                 fillAndPersistDublinCoreelement(DublinCoreElementEnum.PUBLISHER.getId(),elements[i],interfaceDublinCoreDTO,resourceId);
         }
 
         elements = null;
         if(interfaceDublinCoreDTO.getContributor() != null)
         {
             elements = interfaceDublinCoreDTO.getContributor().split(";");
             for(int i = 0; i < elements.length; i++)
                 fillAndPersistDublinCoreelement(DublinCoreElementEnum.CONTRIBUTOR.getId(),elements[i],interfaceDublinCoreDTO,resourceId);
         }
 
         elements = null;
         if(interfaceDublinCoreDTO.getDate() != null)
         {
             elements = interfaceDublinCoreDTO.getDate().split(";");
             for(int i = 0; i < elements.length; i++)
                 fillAndPersistDublinCoreelement(DublinCoreElementEnum.DATE.getId(),elements[i],interfaceDublinCoreDTO,resourceId);
         }
 
         elements = null;
         if(interfaceDublinCoreDTO.getType() != null)
         {
             elements = interfaceDublinCoreDTO.getType().split(";");
             for(int i = 0; i < elements.length; i++)
                 fillAndPersistDublinCoreelement(DublinCoreElementEnum.TYPE.getId(),elements[i],interfaceDublinCoreDTO,resourceId);
         }
 
         elements = null;
         if(interfaceDublinCoreDTO.getFormat() != null)
         {
             elements = interfaceDublinCoreDTO.getFormat().split(";");
             for(int i = 0; i < elements.length; i++)
                 fillAndPersistDublinCoreelement(DublinCoreElementEnum.FORMAT.getId(),elements[i],interfaceDublinCoreDTO,resourceId);
         }
 
         elements = null;
         if(interfaceDublinCoreDTO.getIdentifier() != null)
         {
             elements = interfaceDublinCoreDTO.getIdentifier().split(";");
             for(int i = 0; i < elements.length; i++)
                 fillAndPersistDublinCoreelement(DublinCoreElementEnum.IDENTIFIER.getId(),elements[i],interfaceDublinCoreDTO,resourceId);
         }
 
         elements = null;
         if(interfaceDublinCoreDTO.getSource() != null)
         {
             elements = interfaceDublinCoreDTO.getSource().split(";");
             for(int i = 0; i < elements.length; i++)
                 fillAndPersistDublinCoreelement(DublinCoreElementEnum.SOURCE.getId(),elements[i],interfaceDublinCoreDTO,resourceId);
         }
 
         elements = null;
         if(interfaceDublinCoreDTO.getLanguage() != null)
         {
             elements = interfaceDublinCoreDTO.getLanguage().split(";");
             for(int i = 0; i < elements.length; i++)
                 fillAndPersistDublinCoreelement(DublinCoreElementEnum.LANGUAGE.getId(),elements[i],interfaceDublinCoreDTO,resourceId);
         }
 
         elements = null;
         if(interfaceDublinCoreDTO.getRelation() != null)
         {
             elements = interfaceDublinCoreDTO.getRelation().split(";");
             for(int i = 0; i < elements.length; i++)
                 fillAndPersistDublinCoreelement(DublinCoreElementEnum.RELATION.getId(),elements[i],interfaceDublinCoreDTO,resourceId);
         }
 
         elements = null;
         if(interfaceDublinCoreDTO.getCoverage() != null)
         {
             elements = interfaceDublinCoreDTO.getCoverage().split(";");
             for(int i = 0; i < elements.length; i++)
                 fillAndPersistDublinCoreelement(DublinCoreElementEnum.COVERAGE.getId(),elements[i],interfaceDublinCoreDTO,resourceId);
         }
 
         elements = null;
         if(interfaceDublinCoreDTO.getRights() != null)
         {
             elements = interfaceDublinCoreDTO.getRights().split(";");
             for(int i = 0; i < elements.length; i++)
                 fillAndPersistDublinCoreelement(DublinCoreElementEnum.RIGHTS.getId(),elements[i],interfaceDublinCoreDTO,resourceId);
         }
        
     }
 
     private void fillAndPersistDublinCoreelement(int dbElementEnumId, String value,
             InterfaceDublinCoreDTO interfaceDublinCoreDTO,
             Integer resourceId)
     {
         if(!value.equals("") && !value.trim().equals("") && !value.equals(";"))
         {
             DublinCoreElementJPA dublinCoreElementJPA = new DublinCoreElementJPA();
 
             dublinCoreElementJPA.setCreatedBy(interfaceDublinCoreDTO.getUserName());
             dublinCoreElementJPA.setCreationDate(new GregorianCalendar());
             dublinCoreElementJPA.setLastModificationBy(interfaceDublinCoreDTO.getUserName());
             dublinCoreElementJPA.setLastModificationDate(new GregorianCalendar());
 
             dublinCoreElementJPA.setDublinCoreElementId(dbElementEnumId);
             
             dublinCoreElementJPA.setLanguage(null);
             dublinCoreElementJPA.setResourceId(resourceId);
             dublinCoreElementJPA.setValue(value);
 
             dublinCoreElementDAOImpl.create(dublinCoreElementJPA);
 
         }
     }
 
     public void updateDublinCore(InterfaceDublinCoreDTO interfaceDublinCoreDTO) {
 
         DublinCoreDescription dcd = dublinCoreDescriptionDAOImpl.findById(interfaceDublinCoreDTO.getResourceId().intValue());
         //update the values
         dcd.setResourceTypeId(interfaceDublinCoreDTO.getResourceTypeId().intValue());
         dcd.setDescription(interfaceDublinCoreDTO.getResourceTypeDescription());
 
         List<DublinCoreElement> listDBE = dublinCoreElementDAOImpl.findAllByResourceId(interfaceDublinCoreDTO.getResourceId().intValue());
         for (DublinCoreElement dublinCoreElement : listDBE) {
             dublinCoreElementDAOImpl.delete(dublinCoreElement);
         }
 
         saveDublinCoreElements(interfaceDublinCoreDTO.getResourceId().intValue(), interfaceDublinCoreDTO);
     }
 
     public InterfaceDublinCoreDTO findInterfaceDublincoreById(Long resourceId) {
 
         InterfaceDublinCoreDTO interfaceDublinCoreDTO = new InterfaceDublinCoreDTO();
 
         DublinCoreDescription dublinCoreDescription =
                 dublinCoreDescriptionDAOImpl.findById(resourceId.intValue());
 
         if(dublinCoreDescription != null)
         {
             interfaceDublinCoreDTO.setResourceId(resourceId);
             interfaceDublinCoreDTO.setResourceTypeId(new Long(dublinCoreDescription.getResourceTypeId()));
             interfaceDublinCoreDTO.setResourceTypeDescription(dublinCoreDescription.getDescription());
 
             List<DublinCoreElement> listDCElement;
 
             listDCElement = dublinCoreElementDAOImpl.findAllByResourceIdAndDCElementId(resourceId.intValue(), DublinCoreElementEnum.TITLE.getId());
             interfaceDublinCoreDTO.setTitle(groupDBElementString(listDCElement));
 
             listDCElement = null;
             listDCElement = dublinCoreElementDAOImpl.findAllByResourceIdAndDCElementId(resourceId.intValue(), DublinCoreElementEnum.CREATOR.getId());
             interfaceDublinCoreDTO.setCreator(groupDBElementString(listDCElement));
 
             listDCElement = null;
             listDCElement = dublinCoreElementDAOImpl.findAllByResourceIdAndDCElementId(resourceId.intValue(), DublinCoreElementEnum.SUBJECT.getId());
             interfaceDublinCoreDTO.setSubject(groupDBElementString(listDCElement));
 
             listDCElement = null;
             listDCElement = dublinCoreElementDAOImpl.findAllByResourceIdAndDCElementId(resourceId.intValue(), DublinCoreElementEnum.DESCRIPTION.getId());
             interfaceDublinCoreDTO.setDescription(groupDBElementString(listDCElement));
 
             listDCElement = null;
             listDCElement = dublinCoreElementDAOImpl.findAllByResourceIdAndDCElementId(resourceId.intValue(), DublinCoreElementEnum.PUBLISHER.getId());
             interfaceDublinCoreDTO.setPublisher(groupDBElementString(listDCElement));
 
             listDCElement = null;
             listDCElement = dublinCoreElementDAOImpl.findAllByResourceIdAndDCElementId(resourceId.intValue(), DublinCoreElementEnum.CONTRIBUTOR.getId());
             interfaceDublinCoreDTO.setContributor(groupDBElementString(listDCElement));
 
             listDCElement = null;
             listDCElement = dublinCoreElementDAOImpl.findAllByResourceIdAndDCElementId(resourceId.intValue(), DublinCoreElementEnum.DATE.getId());
             interfaceDublinCoreDTO.setDate(groupDBElementString(listDCElement));
 
             listDCElement = null;
             listDCElement = dublinCoreElementDAOImpl.findAllByResourceIdAndDCElementId(resourceId.intValue(), DublinCoreElementEnum.TYPE.getId());
             interfaceDublinCoreDTO.setType(groupDBElementString(listDCElement));
 
             listDCElement = null;
             listDCElement = dublinCoreElementDAOImpl.findAllByResourceIdAndDCElementId(resourceId.intValue(), DublinCoreElementEnum.FORMAT.getId());
             interfaceDublinCoreDTO.setFormat(groupDBElementString(listDCElement));
 
             listDCElement = null;
             listDCElement = dublinCoreElementDAOImpl.findAllByResourceIdAndDCElementId(resourceId.intValue(), DublinCoreElementEnum.IDENTIFIER.getId());
             interfaceDublinCoreDTO.setIdentifier(groupDBElementString(listDCElement));
 
             listDCElement = null;
             listDCElement = dublinCoreElementDAOImpl.findAllByResourceIdAndDCElementId(resourceId.intValue(), DublinCoreElementEnum.SOURCE.getId());
             interfaceDublinCoreDTO.setSource(groupDBElementString(listDCElement));
 
             listDCElement = null;
             listDCElement = dublinCoreElementDAOImpl.findAllByResourceIdAndDCElementId(resourceId.intValue(), DublinCoreElementEnum.LANGUAGE.getId());
             interfaceDublinCoreDTO.setLanguage(groupDBElementString(listDCElement));
 
             listDCElement = null;
             listDCElement = dublinCoreElementDAOImpl.findAllByResourceIdAndDCElementId(resourceId.intValue(), DublinCoreElementEnum.RELATION.getId());
             interfaceDublinCoreDTO.setRelation(groupDBElementString(listDCElement));
 
             listDCElement = null;
             listDCElement = dublinCoreElementDAOImpl.findAllByResourceIdAndDCElementId(resourceId.intValue(), DublinCoreElementEnum.COVERAGE.getId());
             interfaceDublinCoreDTO.setCoverage(groupDBElementString(listDCElement));
 
             listDCElement = null;
             listDCElement = dublinCoreElementDAOImpl.findAllByResourceIdAndDCElementId(resourceId.intValue(), DublinCoreElementEnum.RIGHTS.getId());
             interfaceDublinCoreDTO.setRights(groupDBElementString(listDCElement));
 
             return interfaceDublinCoreDTO;
             
         }
         else
             return interfaceDublinCoreDTO;
     }
 
 
     private String groupDBElementString(List<DublinCoreElement> listDCElement)
     {
         String text = "";
         for (DublinCoreElement dublinCoreElement : listDCElement)
             text += dublinCoreElement.getValue() + ";";
         
         return text;
     }
 
   
 
 }
