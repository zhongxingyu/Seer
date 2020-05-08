 /*
  * @(#)CreateRegulationDispatchAction.java
  *
  * Copyright 2011 Instituto Superior Tecnico
  * Founding Authors: Anil Kassamali
  * 
  *      https://fenix-ashes.ist.utl.pt/
  * 
  *   This file is part of the Dispatch Registry Module.
  *
  *   The Dispatch Registry Module is free software: you can
  *   redistribute it and/or modify it under the terms of the GNU Lesser General
  *   Public License as published by the Free Software Foundation, either version 
  *   3 of the License, or (at your option) any later version.
  *
  *   The Dispatch Registry Module is distributed in the hope that it will be useful,
  *   but WITHOUT ANY WARRANTY; without even the implied warranty of
  *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  *   GNU Lesser General Public License for more details.
  *
  *   You should have received a copy of the GNU Lesser General Public License
  *   along with the Dispatch Registry Module. If not, see <http://www.gnu.org/licenses/>.
  * 
  */
 package module.regulation.dispatch.presentationTier;
 
 import java.io.IOException;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import module.regulation.dispatch.domain.RegulationDispatchProcessFile;
 import module.regulation.dispatch.domain.RegulationDispatchQueue;
 import module.regulation.dispatch.domain.RegulationDispatchWorkflowMetaProcess;
 import module.regulation.dispatch.domain.activities.AbstractWorkflowActivity;
 import module.regulation.dispatch.domain.activities.CreateRegulationDispatchBean;
 import module.regulation.dispatch.domain.activities.RegulationDispatchActivityInformation;
 import module.regulation.dispatch.domain.exceptions.RegulationDispatchException;
 import module.workflow.activities.WorkflowActivity;
 import module.workflow.presentationTier.WorkflowLayoutContext;
 
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 
 import pt.ist.bennu.core.applicationTier.Authenticate.UserView;
 import pt.ist.bennu.core.domain.User;
 import pt.ist.bennu.core.presentationTier.Context;
 import pt.ist.bennu.core.presentationTier.actions.ContextBaseAction;
 import pt.ist.fenixWebFramework.renderers.utils.RenderUtils;
 import pt.ist.fenixWebFramework.servlets.filters.contentRewrite.GenericChecksumRewriter;
 import pt.ist.fenixWebFramework.struts.annotations.Mapping;
import pt.ist.fenixframework.Atomic;
 
 @Mapping(path = "/createRegulationDispatch")
 /**
  * 
  * @author Anil Kassamali
  * 
  */
 public class CreateRegulationDispatchAction extends ContextBaseAction {
 
     @Override
     public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)
             throws Exception {
 
         RegulationDispatchQueue queue = readQueue(request);
         request.setAttribute("queue", queue);
 
         return super.execute(mapping, form, request, response);
     }
 
     public ActionForward prepare(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
             final HttpServletResponse response) {
 
         RegulationDispatchQueue queue = readQueue(request);
         CreateRegulationDispatchBean bean = new CreateRegulationDispatchBean(queue);
 
         request.setAttribute("bean", bean);
 
         return forward(request, "/module/regulation/dispatch/domain/RegulationDispatchWorkflowMetaProcess/create.jsp");
     }
 
     public ActionForward createInvalid(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
             final HttpServletResponse response) {
         request.setAttribute("bean", getRenderedObject("bean"));
 
         return forward(request, "/module/regulation/dispatch/domain/RegulationDispatchWorkflowMetaProcess/create.jsp");
     }
 
    @Atomic
     public ActionForward create(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
             final HttpServletResponse response) {
         CreateRegulationDispatchBean bean = getRenderedObject("bean");
         RegulationDispatchQueue queue = readQueue(request);
         User user = UserView.getCurrentUser();
 
         try {
             RegulationDispatchWorkflowMetaProcess.createNewProcess(bean, user);
             return forwardToViewQueue(request, queue);
         } catch (final RegulationDispatchException e) {
             addMessage(request, "error", e.getMessage(), e.getArgs());
             return createInvalid(mapping, form, request, response);
         }
     }
 
     private ActionForward forwardToViewQueue(final HttpServletRequest request, RegulationDispatchQueue queue) {
         String contextPath = request.getContextPath();
         String realLink = contextPath + "/regulationDispatch.do?method=viewQueue&queueId=" + queue.getExternalId();
         String checksum =
                 String.format("&%s=%s", GenericChecksumRewriter.CHECKSUM_ATTRIBUTE_NAME,
                         GenericChecksumRewriter.calculateChecksum(realLink));
         return new ActionForward("/regulationDispatch.do?method=viewQueue&queueId=" + queue.getExternalId() + checksum, true);
     }
 
     public ActionForward prepareEdit(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
             final HttpServletResponse response) {
         RegulationDispatchWorkflowMetaProcess process = readProcess(request);
         WorkflowActivity<RegulationDispatchWorkflowMetaProcess, RegulationDispatchActivityInformation> activity =
                 process.getActivity("EditDispatch");
         RegulationDispatchActivityInformation bean =
                 new RegulationDispatchActivityInformation(process, (AbstractWorkflowActivity) activity);
 
         request.setAttribute("dispatch", process);
         request.setAttribute("bean", bean);
 
         return forward(request, "/module/regulation/dispatch/domain/RegulationDispatchWorkflowMetaProcess/edit.jsp");
     }
 
     public ActionForward editInvalid(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
             final HttpServletResponse response) {
         RegulationDispatchActivityInformation bean = getRenderedObject("bean");
         request.setAttribute("bean", bean);
 
         return forward(request, "/module/regulation/dispatch/domain/RegulationDispatchWorkflowMetaProcess/edit.jsp");
     }
 
     public ActionForward edit(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
             final HttpServletResponse response) {
         try {
             RegulationDispatchActivityInformation bean = getRenderedObject("bean");
             RegulationDispatchWorkflowMetaProcess process = bean.getProcess();
             WorkflowActivity<RegulationDispatchWorkflowMetaProcess, RegulationDispatchActivityInformation> activity =
                     process.getActivity("EditDispatch");
 
             activity.execute(bean);
 
             RegulationDispatchQueue queue = readQueue(request);
             return forwardToViewQueue(request, queue);
         } catch (RegulationDispatchException e) {
             addMessage(request, "error", e.getKey(), e.getArgs());
             return editInvalid(mapping, form, request, response);
         }
     }
 
     public ActionForward upload(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
             final HttpServletResponse response) {
         try {
             RegulationDispatchActivityInformation bean = getRenderedObject("bean");
             RegulationDispatchWorkflowMetaProcess process = bean.getProcess();
             WorkflowActivity<RegulationDispatchWorkflowMetaProcess, RegulationDispatchActivityInformation> activity =
                     process.getActivity("UploadFile");
 
             activity.execute(bean);
             RenderUtils.invalidateViewState();
 
             return prepareEdit(mapping, form, request, response);
         } catch (RegulationDispatchException e) {
             addMessage(request, "error", e.getKey(), e.getArgs());
             return editInvalid(mapping, form, request, response);
         }
     }
 
     public ActionForward uploadInvalid(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
             final HttpServletResponse response) {
         return prepareEdit(mapping, form, request, response);
     }
 
     public ActionForward download(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
             final HttpServletResponse response) throws IOException {
         RegulationDispatchProcessFile file = readFile(request);
         return download(response, file.getFilename(), file.getStream(), file.getContentType());
     }
 
     public ActionForward removeFile(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
             final HttpServletResponse response) {
 
         try {
             RegulationDispatchWorkflowMetaProcess process = readProcess(request);
             RegulationDispatchProcessFile file = readFile(request);
             WorkflowActivity<RegulationDispatchWorkflowMetaProcess, RegulationDispatchActivityInformation> activity =
                     process.getActivity("RemoveFile");
 
             RegulationDispatchActivityInformation bean =
                     new RegulationDispatchActivityInformation(process, file, (AbstractWorkflowActivity) activity);
 
             activity.execute(bean);
 
             return prepareEdit(mapping, form, request, response);
 
         } catch (RegulationDispatchException e) {
             addMessage(request, "error", e.getKey(), e.getArgs());
             return editInvalid(mapping, form, request, response);
         }
     }
 
     public ActionForward putFileAsMainDocument(final ActionMapping mapping, final ActionForm form,
             final HttpServletRequest request, final HttpServletResponse response) {
         try {
             RegulationDispatchWorkflowMetaProcess process = readProcess(request);
             RegulationDispatchProcessFile file = readFile(request);
             WorkflowActivity<RegulationDispatchWorkflowMetaProcess, RegulationDispatchActivityInformation> activity =
                     process.getActivity("SetFileAsMainDocument");
 
             RegulationDispatchActivityInformation bean =
                     new RegulationDispatchActivityInformation(process, file, (AbstractWorkflowActivity) activity);
 
             activity.execute(bean);
 
             return prepareEdit(mapping, form, request, response);
 
         } catch (RegulationDispatchException e) {
             addMessage(request, "error", e.getKey(), e.getArgs());
             return editInvalid(mapping, form, request, response);
         }
     }
 
     public ActionForward prepareRemoveDispatch(final ActionMapping mapping, final ActionForm form,
             final HttpServletRequest request, final HttpServletResponse response) {
         RegulationDispatchWorkflowMetaProcess process = readProcess(request);
 
         WorkflowActivity<RegulationDispatchWorkflowMetaProcess, RegulationDispatchActivityInformation> activity =
                 process.getActivity("RemoveDispatch");
         RegulationDispatchActivityInformation bean =
                 new RegulationDispatchActivityInformation(process, (AbstractWorkflowActivity) activity);
 
         request.setAttribute("dispatch", process);
         request.setAttribute("bean", bean);
 
         return forward(request, "/module/regulation/dispatch/domain/RegulationDispatchWorkflowMetaProcess/remove.jsp");
     }
 
     public ActionForward removeDispatch(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
             final HttpServletResponse response) {
         try {
             RegulationDispatchActivityInformation bean = getRenderedObject("bean");
             WorkflowActivity<RegulationDispatchWorkflowMetaProcess, RegulationDispatchActivityInformation> activity =
                     bean.getProcess().getActivity("RemoveDispatch");
 
             activity.execute(bean);
 
             return forwardToViewQueue(request, readQueue(request));
 
         } catch (RegulationDispatchException e) {
             addMessage(request, "error", e.getKey(), e.getArgs());
             return editInvalid(mapping, form, request, response);
         }
     }
 
     private RegulationDispatchProcessFile readFile(final HttpServletRequest request) {
         return getDomainObject(request, "fileId");
     }
 
     private RegulationDispatchWorkflowMetaProcess readProcess(final HttpServletRequest request) {
         return getDomainObject(request, "dispatchId");
     }
 
     private RegulationDispatchQueue readQueue(final HttpServletRequest request) {
         return getDomainObject(request, "queueId");
     }
 
     @Override
     public Context createContext(String contextPathString, HttpServletRequest request) {
         WorkflowLayoutContext context =
                 WorkflowLayoutContext.getDefaultWorkflowLayoutContext(RegulationDispatchWorkflowMetaProcess.class);
         context.setElements(contextPathString);
 
         return context;
     }
 
 }
