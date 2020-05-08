 package gov.nih.nci.nautilus.ui.struts.action;
 
 import gov.nih.nci.nautilus.constants.NautilusConstants;
 import gov.nih.nci.nautilus.de.GeneIdentifierDE;
 import gov.nih.nci.nautilus.de.SNPIdentifierDE;
 import gov.nih.nci.nautilus.resultset.kaplanMeierPlot.KMPlotManager;
 import gov.nih.nci.nautilus.resultset.kaplanMeierPlot.KaplanMeierPlotContainer;
 import gov.nih.nci.nautilus.ui.graph.geneExpression.GeneExpressionGraphGenerator;
 import gov.nih.nci.nautilus.ui.graph.kaplanMeier.KMGraphGenerator;
 import gov.nih.nci.nautilus.ui.graph.kaplanMeier.KMSampleInfo;
 import gov.nih.nci.nautilus.ui.helper.KMDataSetHelper;
 import gov.nih.nci.nautilus.ui.struts.form.KMDataSetForm;
 import gov.nih.nci.nautilus.ui.struts.form.QuickSearchForm;
 import gov.nih.nci.nautilus.ui.struts.form.UIFormValidator;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.log4j.Logger;
import org.apache.struts.action.ActionError;
 import org.apache.struts.action.ActionErrors;
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 import org.apache.struts.actions.DispatchAction;
 
 public class QuickSearchAction extends DispatchAction {
 	static Logger logger = Logger.getLogger(QuickSearchAction.class);
 	private KaplanMeierPlotContainer kmResultsContainer = null;
 	private String chartType;
 	private String quickSearchName;
     private String quickSearchType;
 	private String kmplotType;
 	/**
 	 * Method execute
 	 * 
 	 * @param ActionMapping
 	 *            mapping
 	 * @param ActionForm
 	 *            form
 	 * @param HttpServletRequest
 	 *            request
 	 * @param HttpServletResponse
 	 *            response
 	 * @return ActionForward
 	 * @throws Exception
 	 */
 	private ActionForward doGeneExpPlot(ActionMapping mapping, ActionForm form,
 			HttpServletRequest request, HttpServletResponse response)
 			throws Exception {
 		GeneExpressionGraphGenerator generator = new GeneExpressionGraphGenerator(
 				form);
 		if (generator.getErrors().size() > 0) {
 			this.saveErrors(request, generator.getErrors());
 			return mapping.findForward("badgraph");
 		}
         generator.setRequestAttributes(request);
         generator.setSessionAttributes(request.getSession(true));
         return mapping.findForward("histogram");
 
 	}
 
 	/***************************************************************************
 	 * This method gathers the input parameters from the quickSearchForm and
 	 * creates and forwards the request to the KMPlot jsp.
 	 * 
 	 * @param mapping
 	 * @param form
 	 * @param request
 	 * @param response
 	 * @return
 	 * @throws Exception
 	 */
 	public ActionForward doKMPlot(ActionMapping mapping, ActionForm form,
 			HttpServletRequest request, HttpServletResponse response)
 			throws Exception {
 
 		KMDataSetForm kmForm = (KMDataSetForm) form;
         quickSearchName = (String)request.getAttribute("quickSearchName");	
         quickSearchType = (String)request.getAttribute("quickSearchType");
 		kmplotType = (String)request.getAttribute("plotType");
 
         if(kmplotType.equals(NautilusConstants.GENE_EXP_KMPLOT)){
             performKMGeneExpressionQuery(quickSearchName);
             if(getKmResultsContainer() != null){
         		KMSampleInfo[] kmSampleInfos = getKmResultsContainer().getSummaryKMPlotSamples();
         		KMGraphGenerator generator = new KMGraphGenerator(kmForm.getUpFold(),
         				kmForm.getDownFold(), quickSearchName, kmSampleInfos, kmplotType);
         		if (generator.getMyActionErrors().size() > 0) {
         			this.saveErrors(request, generator.getMyActionErrors());
         			return mapping.findForward("badgraph");
         		}
                 kmForm.setGeneOrCytoband(getKmResultsContainer().getGeneSymbol().getValue().toString() );
         		kmForm = KMDataSetHelper.populateKMDataSetForm(generator,kmplotType, kmForm);
         		kmForm = KMDataSetHelper.populateReporters(getKmResultsContainer().getAssociatedReporters(),kmplotType, kmForm);
                 return mapping.findForward("kmplot");
             }
             return mapping.findForward("badgraph");
         }
         else if(kmplotType.equals(NautilusConstants.COPY_NUMBER_KMPLOT)){
             performKMCopyNumberQuery(quickSearchName, quickSearchType);
             //kmForm.setReporters(populateReporters());
             if(getKmResultsContainer() != null){
                 String cytobandGeneSymbol = getKmResultsContainer().getCytobandDE().getValue().toString();
                 kmForm.setGeneOrCytoband(quickSearchName +"("+cytobandGeneSymbol+")");
                 KMSampleInfo[] kmSampleInfos = {new KMSampleInfo(0,0,0)};
                 KMGraphGenerator generator = new KMGraphGenerator(kmForm.getUpFold(),
                         kmForm.getDownFold(), quickSearchName, kmSampleInfos, kmplotType);
             if (generator.getMyActionErrors().size() > 0) {
                 this.saveErrors(request, generator.getMyActionErrors());
                 return mapping.findForward("badgraph");
             }
         		kmForm = KMDataSetHelper.populateKMDataSetForm(generator,kmplotType, kmForm);
         		kmForm = KMDataSetHelper.populateReporters(getKmResultsContainer().getAssociatedReporters(),kmplotType, kmForm);
             return mapping.findForward("kmplot");
            }      
         }
        ActionErrors errors = new ActionErrors();
        errors
           .add(
                ActionErrors.GLOBAL_ERROR,
                new ActionError(
                        "gov.nih.nci.nautilus.ui.struts.form.quicksearch.noRecord",
                        quickSearchName));
        this.saveErrors(request, errors);
        return mapping.findForward("badgraph");
 	}
 
 	public ActionForward redrawKMPlot(ActionMapping mapping, ActionForm form,
 			HttpServletRequest request, HttpServletResponse response)
 			throws Exception {
 		KMDataSetForm kmForm = (KMDataSetForm) form;
 		KMSampleInfo[] kmSampleInfos = null;
 		//kmForm.setReporters(populateReporters());
 		if( getKmResultsContainer() != null && 
                 kmForm.getSelectedReporter() != null){
             if((kmForm.getSelectedReporter().trim().length() > 0)){
     	        if(kmplotType.equals(NautilusConstants.GENE_EXP_KMPLOT)){
     				if(kmForm.getSelectedReporter().equals(NautilusConstants.GRAPH_DEFAULT)){
     					kmSampleInfos = kmResultsContainer.getSummaryKMPlotSamples();
     				}
     				else{
     					kmSampleInfos = kmResultsContainer.getKMPlotSamplesForReporter(kmForm.getSelectedReporter());
     				}
     	        }
     	        else if(kmplotType.equals(NautilusConstants.COPY_NUMBER_KMPLOT)){
     	        		kmSampleInfos = kmResultsContainer.getKMPlotSamplesForReporter(kmForm.getSelectedReporter());
 	        	}
             }
             else{ //empty graph
                 KMSampleInfo[] km = {new KMSampleInfo(0,0,0)};
                 kmSampleInfos = km;
             }
 			KMGraphGenerator generator = new KMGraphGenerator(kmForm.getUpFold(),
 					kmForm.getDownFold(), kmForm.getGeneOrCytoband(),kmSampleInfos,kmplotType);
 			if (generator.getMyActionErrors().size() > 0) {
 				this.saveErrors(request, generator.getMyActionErrors());
 				return mapping.findForward("badgraph");
 			}
             //kmForm.setCensorDataset(generator.getCensorDataseries());
             //kmForm.setLineDataset(generator.getLineDataseries());
     		kmForm = KMDataSetHelper.populateKMDataSetForm(generator,kmplotType, kmForm);
     		kmForm = KMDataSetHelper.populateReporters(getKmResultsContainer().getAssociatedReporters(),kmplotType, kmForm);
 
 			return mapping.findForward("kmplot");
 		}
 		return mapping.findForward("badgraph");
 	}
 
 	public ActionForward quickSearch(ActionMapping mapping, ActionForm form,
 			HttpServletRequest request, HttpServletResponse response)
 			throws Exception {
 		QuickSearchForm qsForm = (QuickSearchForm) form;
 		ActionErrors errors = new ActionErrors();
 		errors = UIFormValidator.validateGeneSymbol(qsForm, errors);
 		if(errors.isEmpty()){
 		chartType = qsForm.getPlot();
 		
 			if (chartType.equalsIgnoreCase("kapMaiPlotGE")) {
 			    logger.debug("user requested geneExp kapMai w/ genesymbol");
 				request.setAttribute("quickSearchName", qsForm.getQuickSearchName());
 				request.setAttribute("plotType", NautilusConstants.GENE_EXP_KMPLOT);
 				return mapping.findForward("kmplot");
 			}if (chartType.equalsIgnoreCase("kapMaiPlotCN")) {
 			    logger.debug("user rquested SNP kapMaiPlotCN");
 			    request.setAttribute("quickSearchType",qsForm.getQuickSearchType());
 				request.setAttribute("quickSearchName", qsForm.getQuickSearchName());
 				request.setAttribute("plotType", NautilusConstants.COPY_NUMBER_KMPLOT);
 				return mapping.findForward("kmplot");
 			}
 			
 			else if (chartType.equalsIgnoreCase("geneExpPlot")) {
 				try {
 				    logger.debug("user has requested geneExpPlot");
 					return doGeneExpPlot(mapping, qsForm, request, response);
 				} catch (Exception e) {
 					logger.error("Gene Expression Plot Flopped");
 					logger.error(e);
 					return mapping.findForward("error");
 				}
 			} 
 			
 		}
 		this.saveErrors(request, errors);
 		return mapping.findForward("mismatch");
 	  }
 	/*private List populateReporters(){
 		List reporters = new ArrayList();
         reporters.add("");
 		if( getKmResultsContainer() != null){
 			reporters = kmResultsContainer.getAssociatedReporters();
 			if (kmplotType.equals(NautilusConstants.GENE_EXP_KMPLOT)){
 				reporters.add(0,NautilusConstants.GRAPH_DEFAULT);
 			}
             if (kmplotType.equals(NautilusConstants.COPY_NUMBER_KMPLOT)){
                 reporters.add(0," ");
             }
 		}
 		return reporters;
 	}*/
     /**
      * @return Returns the kmResultsContainer.
      * @throws Exception
      */
     private void performKMGeneExpressionQuery(String geneSymbol) throws Exception {
     		KMPlotManager kmPlotManager = new KMPlotManager();
     		this.kmResultsContainer = (KaplanMeierPlotContainer) kmPlotManager.performKMGeneExpressionQuery(geneSymbol);
     }
     /**
      * @return Returns the kmResultsContainer.
      * @throws Exception
      */
     private void performKMCopyNumberQuery(String name, String type) throws Exception {
         KMPlotManager kmPlotManager = new KMPlotManager();
         if(type.equals(NautilusConstants.GENE_SYMBOL)){
             GeneIdentifierDE.GeneSymbol genesymbolDE = new GeneIdentifierDE.GeneSymbol(name);
             this.kmResultsContainer = (KaplanMeierPlotContainer) kmPlotManager.performKMCopyNumberQuery(genesymbolDE);
 
         }
         /**TODO:FOr 1.0
          if(type.equals(NautilusConstants.CYTOBAND)){
             CytobandDE cytobandDE = new CytobandDE(name);
             this.kmResultsContainer = kmPlotManager.performKMCopyNumberQuery(cytobandDE);
 
         }
         **/
         if(type.equals(NautilusConstants.SNP_PROBESET_ID)){
             SNPIdentifierDE.SNPProbeSet snpDE = new SNPIdentifierDE.SNPProbeSet(name);
             this.kmResultsContainer = (KaplanMeierPlotContainer) kmPlotManager.performKMCopyNumberQuery(snpDE);
 
         }
         
     }
     /**
      * @return Returns the kmResultsContainer.
      */
     public KaplanMeierPlotContainer getKmResultsContainer() {
         return this.kmResultsContainer;
     }
 }
 
