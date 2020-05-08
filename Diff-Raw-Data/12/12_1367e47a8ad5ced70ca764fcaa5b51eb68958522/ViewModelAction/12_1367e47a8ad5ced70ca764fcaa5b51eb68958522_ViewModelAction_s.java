 /**
  *  @author sguruswami
  *  
 *  $Id: ViewModelAction.java,v 1.15 2005-10-19 18:56:00 guruswas Exp $
  *  
  *  $Log: not supported by cvs2svn $
  *  Revision 1.14  2005/10/11 18:15:25  georgeda
  *  More comment changes
  *
  *  Revision 1.13  2005/10/10 14:12:24  georgeda
  *  Changes for comment curation
  *
  *  Revision 1.12  2005/10/07 21:15:03  georgeda
  *  Added caarray variables
  *
  *  Revision 1.11  2005/10/06 13:37:01  georgeda
  *  Removed informational message
  *
  *  Revision 1.10  2005/09/30 18:42:24  guruswas
  *  intial implementation of drug screening search and display page
  *
  *  Revision 1.9  2005/09/22 21:34:51  guruswas
  *  First stab at carcinogenic intervention pages
  *
  *  Revision 1.8  2005/09/22 15:23:41  georgeda
  *  Cleaned up warnings
  *
  *  Revision 1.7  2005/09/21 21:02:24  guruswas
  *  Display the organ, disease names from NCI Thesaurus
  *
  *  Revision 1.6  2005/09/21 20:47:16  georgeda
  *  Cleaned up
  *
  *  Revision 1.5  2005/09/16 19:30:00  guruswas
  *  Display invivo data (from DTP) in the therapuetic approaches page
  *
  *  Revision 1.4  2005/09/16 15:52:56  georgeda
  *  Changes due to manager re-write
  *
  *  
  */
 package gov.nih.nci.camod.webapp.action;
 
 import gov.nih.nci.cabio.domain.Gene;
 import gov.nih.nci.cabio.domain.impl.GeneImpl;
 import gov.nih.nci.camod.Constants;
 import gov.nih.nci.camod.domain.*;
 import gov.nih.nci.camod.service.*;
 import gov.nih.nci.camod.service.impl.QueryManagerSingleton;
 import gov.nih.nci.camod.util.EvsTreeUtil;
 import gov.nih.nci.common.domain.DatabaseCrossReference;
 import gov.nih.nci.common.domain.impl.DatabaseCrossReferenceImpl;
 import gov.nih.nci.system.applicationservice.ApplicationService;
 
 import java.util.*;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.struts.action.*;
 
 public class ViewModelAction extends BaseAction {
 
     /**
      * sets the cancer model object in the session
      * 
      * @param request
      *            the httpRequest
      */
     private void setCancerModel(HttpServletRequest request) {
         String modelID = request.getParameter("aModelID");
         System.out.println("<setCancerModel> modelID" + modelID);
         AnimalModelManager animalModelManager = (AnimalModelManager) getBean("animalModelManager");
         AnimalModel am = null;
         try {
             am = animalModelManager.get(modelID);
         } catch (Exception e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
         request.getSession().setAttribute(Constants.ANIMALMODEL, am);
     }
 
     /**
      * sets the cancer model object in the session
      * 
      * @param request
      *            the httpRequest
      * @throws Exception
      */
     private void setComments(HttpServletRequest request, String inSection) throws Exception {
 
         String theCommentsId = request.getParameter(Constants.Parameters.COMMENTSID);
 
         CommentsManager theCommentsManager = (CommentsManager) getBean("commentsManager");
 
         System.out.println("Comments id: " + theCommentsId);
         List theCommentsList = new ArrayList();
         if (theCommentsId != null && theCommentsId.length() > 0) {
             Comments theComments = theCommentsManager.get(theCommentsId);
             if (theComments != null) {
                 System.out.println("Found a comment: " + theComments.getRemark());
                 theCommentsList.add(theComments);
             }
         }
 
         // Get all comments that are either approved or owned by this user
         else {
             PersonManager thePersonManager = (PersonManager) getBean("personManager");
             Person theCurrentUser = thePersonManager.getByUsername((String) request.getSession().getAttribute(
                     Constants.CURRENTUSER));
 
             AnimalModel theAnimalModel = (AnimalModel) request.getSession().getAttribute(Constants.ANIMALMODEL);
 
             theCommentsList = theCommentsManager.getAllBySection(inSection, theCurrentUser, theAnimalModel);
         }
 
         request.setAttribute(Constants.Parameters.COMMENTSLIST, theCommentsList);
     }
 
     /**
      * @param mapping
      * @param form
      * @param request
      * @param response
      * @return
      * @throws Exception
      */
     public ActionForward populateModelCharacteristics(ActionMapping mapping, ActionForm form,
             HttpServletRequest request, HttpServletResponse response) throws Exception {
         setCancerModel(request);
         setComments(request, Constants.Pages.MODEL_CHARACTERISTICS);
         return mapping.findForward("viewModelCharacteristics");
     }
 
     /**
      * 
      */
     public ActionForward populateEngineeredGene(ActionMapping mapping, ActionForm form, HttpServletRequest request,
             HttpServletResponse response) throws Exception {
         System.out.println("<populateEngineeredGene> modelID" + request.getParameter("aModelID"));
         String modelID = request.getParameter("aModelID");
 
         AnimalModelManager animalModelManager = (AnimalModelManager) getBean("animalModelManager");
         AnimalModel am = animalModelManager.get(modelID);
 
         final List egc = am.getEngineeredGeneCollection();
         final int egcCnt = (egc != null) ? egc.size() : 0;
         final List tgc = new ArrayList();
         int tgCnt = 0;// Transgene
         final List gsc = new ArrayList();
         int gsCnt = 0;// GenomicSegment
         final List tmc = new ArrayList();
         int tmCnt = 0;// TargetedModification
         final Map tmGeneMap = new HashMap();
         final List imc = new ArrayList();
         int imCnt = 0;// InducedMutation
         for (int i = 0; i < egcCnt; i++) {
             EngineeredGene eg = (EngineeredGene) egc.get(i);
             if (eg instanceof Transgene) {
                 tgc.add(eg);
                 tgCnt++;
             } else if (eg instanceof GenomicSegment) {
                 gsc.add(eg);
                 gsCnt++;
             } else if (eg instanceof TargetedModification) {
                 tmc.add(eg);
                 tmCnt++;
                 // now go to caBIO and query the gene object....
                 TargetedModification tm = (TargetedModification) eg;
                 String geneId = tm.getGeneId();
                 if (geneId != null) {
                     log.info("Connecting to caBIO to look up gene " + geneId);
                     // the geneId is available
                     ApplicationService appService = EvsTreeUtil.getApplicationService();
                     DatabaseCrossReference dcr = new DatabaseCrossReferenceImpl();
                     dcr.setCrossReferenceId(geneId);
                     dcr.setType("gov.nih.nci.cabio.domain.Gene");
                     dcr.setDataSourceName("LOCUS_LINK_ID");
                     List resultList = appService.search(DatabaseCrossReference.class, dcr);
                     final int resultCount = (resultList != null) ? resultList.size() : 0;
                     log.info("Got " + resultCount + " dataCrossReferences....");
                     if (resultCount > 0) {
                         dcr = (DatabaseCrossReference) resultList.get(0);
                         Gene myGene = new GeneImpl();
                         List cfcoll = new ArrayList();
                         cfcoll.add(dcr);
                         myGene.setDatabaseCrossReferenceCollection(cfcoll);
                         resultList = appService.search(Gene.class, myGene);
                         final int geneCount = (resultList != null) ? resultList.size() : 0;
                         log.info("Got " + geneCount + " Gene Objects");
                         if (geneCount > 0) {
                             myGene = (Gene) resultList.get(0);
                             log.info("Gene:" + geneId + " ==>" + myGene);
                             tmGeneMap.put(tm.getId(), myGene);
                         }
                     }
                 }
             } else if (eg instanceof InducedMutation) {
                 imc.add(eg);
                 imCnt++;
             }
         }
         System.out.println("<populateEngineeredGene> " + "egcCnt=" + egcCnt + "tgc=" + tgCnt + "gsc=" + gsCnt + "tmc="
                 + tmCnt + "imc=" + imCnt);
         request.getSession().setAttribute(Constants.ANIMALMODEL, am);
         request.getSession().setAttribute(Constants.TRANSGENE_COLL, tgc);
         request.getSession().setAttribute(Constants.GENOMIC_SEG_COLL, gsc);
         request.getSession().setAttribute(Constants.TARGETED_MOD_COLL, tmc);
         request.getSession().setAttribute(Constants.TARGETED_MOD_GENE_MAP, tmGeneMap);
         request.getSession().setAttribute(Constants.INDUCED_MUT_COLL, imc);
         System.out.println("<populateEngineeredGene> set attributes done.");
 
         setComments(request, Constants.Pages.GENETIC_DESCRIPTION);
         
         return mapping.findForward("viewGeneticDescription");
     }
 
     /**
      * Populate the session and/or request with the objects necessary to display
      * the page.
      * 
      * @param mapping
      *            the struts action mapping
      * @param form
      *            the web form
      * @param request
      *            HTTPRequest
      * @param response
      *            HTTPResponse
      * @return
      * @throws Exception
      */
     public ActionForward populateCarcinogenicInterventions(ActionMapping mapping, ActionForm form,
             HttpServletRequest request, HttpServletResponse response) throws Exception {
         setCancerModel(request);
         String modelID = request.getParameter(Constants.Parameters.MODELID);
         AnimalModelManager animalModelManager = (AnimalModelManager) getBean("animalModelManager");
         AnimalModel am = null;
         try {
             am = animalModelManager.get(modelID);
         } catch (Exception e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
         final List therapyColl = am.getTherapyCollection();
         final Map interventionTypeMap = new HashMap();
         final int cc = (therapyColl != null) ? therapyColl.size() : 0;
         for (int i = 0; i < cc; i++) {
             Therapy t = (Therapy) therapyColl.get(i);
             Boolean isTE = t.getTherapeuticExperiment();
             if (isTE != null && !isTE.booleanValue()) {
                 log.info("Checking agent:" + t.getAgent().getNscNumber());
                 String myType = t.getAgent().getType();
                 if (myType == null || myType.length() == 0) {
                     myType = t.getAgent().getTypeUnctrlVocab();
                     if (myType == null || myType.length() == 0) {
                         myType = "Not specified";
                     }
                 }
                 List myTypeColl = (List) interventionTypeMap.get(myType);
                 if (myTypeColl == null) {
                     myTypeColl = new ArrayList();
                     interventionTypeMap.put(myType, myTypeColl);
                 }
                 myTypeColl.add(t);
             }
         }
         request.getSession().setAttribute(Constants.CARCINOGENIC_INTERVENTIONS_COLL, interventionTypeMap);
         
         setComments(request, Constants.Pages.CARCINOGENIC_INTERVENTION);
         
         return mapping.findForward("viewCarcinogenicInterventions");
     }
 
     /**
      * Populate the session and/or request with the objects necessary to display
      * the page.
      * 
      * @param mapping
      *            the struts action mapping
      * @param form
      *            the web form
      * @param request
      *            HTTPRequest
      * @param response
      *            HTTPResponse
      * @return
      * @throws Exception
      */
     public ActionForward populatePublications(ActionMapping mapping, ActionForm form, HttpServletRequest request,
             HttpServletResponse response) throws Exception {
         setCancerModel(request);
         
         setComments(request, Constants.Pages.PUBLICATIONS);
         
         return mapping.findForward("viewPublications");
     }
 
     /**
      * Populate the session and/or request with the objects necessary to display
      * the page.
      * 
      * @param mapping
      *            the struts action mapping
      * @param form
      *            the web form
      * @param request
      *            HTTPRequest
      * @param response
      *            HTTPResponse
      * @return
      * @throws Exception
      */
     public ActionForward populateHistopathology(ActionMapping mapping, ActionForm form, HttpServletRequest request,
             HttpServletResponse response) throws Exception {
         setCancerModel(request);
         
         setComments(request, Constants.Pages.HISTOPATHOLOGY);
         
         return mapping.findForward("viewHistopathology");
     }
 
     /**
      * Populate the session and/or request with the objects necessary to display
      * the page.
      * 
      * @param mapping
      *            the struts action mapping
      * @param form
      *            the web form
      * @param request
      *            HTTPRequest
      * @param response
      *            HTTPResponse
      * @return
      * @throws Exception
      */
     public ActionForward populateTherapeuticApproaches(ActionMapping mapping, ActionForm form,
             HttpServletRequest request, HttpServletResponse response) throws Exception {
         setCancerModel(request);
         //
         // query caBIO and load clinical protocols information
         // store clinicalProtocol info in a hashmap keyed by NSC#
         //
         final HashMap clinProtocols = new HashMap();
         final HashMap yeastResults = new HashMap();
         final HashMap invivoResults = new HashMap();
         final List therapeuticApprochesColl = new ArrayList();
 
         String modelID = request.getParameter(Constants.Parameters.MODELID);
         AnimalModelManager animalModelManager = (AnimalModelManager) getBean("animalModelManager");
         AnimalModel am = animalModelManager.get(modelID);
         final List therapyColl = am.getTherapyCollection();
         final int cc = (therapyColl != null) ? therapyColl.size() : 0;
         log.info("Looking up clinical protocols for " + cc + " agents...");
 
         for (int i = 0; i < cc; i++) {
             Therapy t = (Therapy) therapyColl.get(i);
             final boolean isTherapy = (t.getTherapeuticExperiment() != null) ? t.getTherapeuticExperiment()
                     .booleanValue() : false;
             if (isTherapy) {
                 therapeuticApprochesColl.add(t);
             }
             Agent a = t.getAgent();
             AgentManager myAgentManager = (AgentManager) getBean("agentManager");
             if (a != null) {
                 Long nscNumber = a.getNscNumber();
                 if (nscNumber != null) {
                     Collection protocols = myAgentManager.getClinicalProtocols(a);
                     clinProtocols.put(nscNumber, protocols);
                     // get the yeast data
                     List yeastStages = myAgentManager.getYeastResults(a);
                     yeastResults.put(nscNumber, yeastStages);
                     // now get invivo/Xenograft data
                     List xenograftResults = QueryManagerSingleton.instance().getInvivoResults(a);
                     invivoResults.put(nscNumber, xenograftResults);
                 }
             }
         }
         request.getSession().setAttribute(Constants.THERAPEUTIC_APPROACHES_COLL, therapeuticApprochesColl);
         request.getSession().setAttribute(Constants.CLINICAL_PROTOCOLS, clinProtocols);
         request.getSession().setAttribute(Constants.YEAST_DATA, yeastResults);
         request.getSession().setAttribute(Constants.INVIVO_DATA, invivoResults);
 
         setComments(request, Constants.Pages.THERAPEUTIC_APPROACHES);
         
         return mapping.findForward("viewTherapeuticApproaches");
     }
 
     /**
      * Populate the session and/or request with the objects necessary to display
      * the page.
      * 
      * @param mapping
      *            the struts action mapping
      * @param form
      *            the web form
      * @param request
      *            HTTPRequest
      * @param response
      *            HTTPResponse
      * @return
      * @throws Exception
      */
     public ActionForward populateCellLines(ActionMapping mapping, ActionForm form, HttpServletRequest request,
             HttpServletResponse response) throws Exception {
         setCancerModel(request);
         
         setComments(request, Constants.Pages.CELL_LINES);
         
         return mapping.findForward("viewCellLines");
     }
 
     /**
      * Populate the session and/or request with the objects necessary to display
      * the page.
      * 
      * @param mapping
      *            the struts action mapping
      * @param form
      *            the web form
      * @param request
      *            HTTPRequest
      * @param response
      *            HTTPResponse
      * @return
      * @throws Exception
      */
     public ActionForward populateImages(ActionMapping mapping, ActionForm form, HttpServletRequest request,
             HttpServletResponse response) throws Exception {
         setCancerModel(request);
         
         setComments(request, Constants.Pages.IMAGES);
         
         return mapping.findForward("viewImages");
     }
 
     /**
      * Populate the session and/or request with the objects necessary to display
      * the page.
      * 
      * @param mapping
      *            the struts action mapping
      * @param form
      *            the web form
      * @param request
      *            HTTPRequest
      * @param response
      *            HTTPResponse
      * @return
      * @throws Exception
      */
     public ActionForward populateMicroarrays(ActionMapping mapping, ActionForm form, HttpServletRequest request,
             HttpServletResponse response) throws Exception {
         setCancerModel(request);
 
         ResourceBundle theBundle = ResourceBundle.getBundle(Constants.CAMOD_BUNDLE);
 
         request.setAttribute("uri_start", theBundle.getString(Constants.CaArray.URI_START));
         request.setAttribute("uri_end", theBundle.getString(Constants.CaArray.URI_END));
 
         setComments(request, Constants.Pages.MICROARRAY);
         
         return mapping.findForward("viewMicroarrays");
     }
 
     /**
      * Populate the session and/or request with the objects necessary to display
      * the page.
      * 
      * @param mapping
      *            the struts action mapping
      * @param form
      *            the web form
      * @param request
      *            HTTPRequest
      * @param response
      *            HTTPResponse
      * @return
      * @throws Exception
      */
     public ActionForward populateTransplantXenograft(ActionMapping mapping, ActionForm form,
             HttpServletRequest request, HttpServletResponse response) throws Exception {
         setCancerModel(request);
         
         setComments(request, Constants.Pages.XENOGRAFT);
         
         return mapping.findForward("viewTransplantXenograft");
     }
 
     /**
      * Populate the session and/or request with the objects necessary to display
      * the page.
      * 
      * @param mapping
      *            the struts action mapping
      * @param form
      *            the web form
      * @param request
      *            HTTPRequest
      * @param response
      *            HTTPResponse
      * @return
      * @throws Exception
      */
     public ActionForward populateXenograftDetails(ActionMapping mapping, ActionForm form, HttpServletRequest request,
             HttpServletResponse response) throws Exception {
         String modelID = request.getParameter("xModelID");
         String nsc = request.getParameter("nsc");
 		if(nsc != null && nsc.length()==0) return mapping.findForward("viewModelCharacteristics");
         log.info("<populateXenograftDetails> modelID:" + modelID);
         log.info("<populateXenograftDetails> nsc:" + nsc);
 		XenograftManager mgr = (XenograftManager) getBean("xenograftManager");
         Xenograft x = null;
         try {
             x = mgr.get(modelID);
         } catch (Exception e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
        request.getSession().setAttribute(Constants.ANIMALMODEL, x);
         request.getSession().setAttribute(Constants.NSC_NUMBER, nsc);
         return mapping.findForward("viewInvivoDetails");
     }
 }
