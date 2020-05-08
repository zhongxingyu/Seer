 package org.gridlab.gridsphere.portlets.core.messaging;
 
 import org.gridlab.gridsphere.provider.portlet.ActionPortlet;
 import org.gridlab.gridsphere.provider.event.FormEvent;
 import org.gridlab.gridsphere.provider.portletui.beans.*;
 import org.gridlab.gridsphere.provider.portletui.model.DefaultTableModel;
 import org.gridlab.gridsphere.portlet.PortletConfig;
 import org.gridlab.gridsphere.portlet.PortletSettings;
 import org.gridlab.gridsphere.portlet.service.PortletServiceException;
 import org.gridlab.gridsphere.services.core.messaging.TextMessagingService;
 import org.gridlab.gridsphere.tmf.config.TmfService;
 import org.gridlab.gridsphere.tmf.config.TmfServiceConfig;
 import org.gridlab.gridsphere.tmf.config.ConfigParameter;
 import org.gridlab.gridsphere.tmf.TextMessagingException;
 
 import javax.servlet.UnavailableException;
 import java.util.List;
 import java.util.ArrayList;
 
 /*
  * @author <a href="mailto:oliver.wehrens@aei.mpg.de">Oliver Wehrens</a>
  * @version $Id$
  */
 
 public class MessagingServicesPortlet extends ActionPortlet {
 
     private TextMessagingService tms = null;
 
     public void init(PortletConfig config) throws UnavailableException {
         super.init(config);
         try {
             this.tms = (TextMessagingService) config.getContext().getService(TextMessagingService.class);
         } catch (PortletServiceException e) {
             log.error("Unable to initialize services!", e);
         }
 
     }
 
     public void initConcrete(PortletSettings settings) throws UnavailableException {
         super.initConcrete(settings);
         DEFAULT_VIEW_PAGE = "doView";
         DEFAULT_HELP_PAGE = "doHelp";
     }
 
 
     private DefaultTableModel getMessagingService(FormEvent event) {
         boolean zebra = false;
         DefaultTableModel dtm = new DefaultTableModel();
         List services = tms.getActiveServices();
 
         for (int i=0;i<services.size();i++) {
             TmfService service = (TmfService)services.get(i);
             TableRowBean trb = new TableRowBean();            
             TableCellBean tcbDescription = new TableCellBean();
             TableCellBean tcbConfiguration = new TableCellBean();
 
             if (!zebra) {
                 tcbDescription.setCssStyle("background: #BDBBB6");
                 tcbConfiguration.setCssStyle("background: #BDBBB6");
             }
             zebra = !zebra;
 
             // description
             CheckBoxBean restartBox = new CheckBoxBean();
             restartBox.setBeanId("restartBox");
             TextBean description = event.getTextBean("description"+service.getClassname());
             TextBean restart = event.getTextBean("restart"+service.getClassname());
 
 
             TmfServiceConfig config = service.getConfig();
             description.setValue(getLocalizedText(event.getPortletRequest(),service.getDescription()));
 
             if (config.isNeedRestart()) {
                 restartBox.setSelected(config.isConfigChanged());
                 restartBox.setValue(service.getClassname());
                 if (config.isConfigChanged()) {
                    restart.setValue("<p/>("+getLocalizedText(event.getPortletRequest(),"MESSAGING_SERVICE_CONFIG_CHANGED")+")");
                     restart.addCssStyle("color: red");
                 }
                 tcbDescription.addBean(restartBox);
             }
             tcbDescription.addBean(description);
             tcbDescription.addBean(restart);
 
             // configuration
             List params = config.getParams();
             DefaultTableModel configTable = new DefaultTableModel();
 
             for (int j=0;j<params.size();j++) {
 
                 TableRowBean configRow = new TableRowBean();
                 TableCellBean configDescription = new TableCellBean();
                 TableCellBean configValue = new TableCellBean();
 
                 ConfigParameter param = (ConfigParameter)params.get(j);
                 TextBean paramName = event.getTextBean(service.getClassname()+"paramKey"+param.getKey());
                 paramName.setValue(param.getKey());
                 configDescription.addBean(paramName);
 
                 TextFieldBean paramValue = event.getTextFieldBean(service.getClassname()+"paramValue"+param.getKey());
                 paramValue.setValue(param.getValue());
                 configValue.addBean(paramValue);
 
                 configRow.addBean(configDescription); configDescription.setAlign("top");
                 configRow.addBean(configValue);
 
                 configTable.addTableRowBean(configRow);
             }
 
             FrameBean frameConfig = new FrameBean();
             frameConfig.setTableModel(configTable);
             tcbConfiguration.addBean(frameConfig);
             trb.addBean(tcbDescription);
             trb.addBean(tcbConfiguration);
 
             dtm.addTableRowBean(trb);
 
         }
         return dtm;
 
     }
 
     public void doView(FormEvent event)  {
         FrameBean serviceFrame = event.getFrameBean("serviceframe");
         serviceFrame.setTableModel(getMessagingService(event));
         List services = tms.getActiveServices();
         event.getPortletRequest().setAttribute("services", new String()+services.size());
         setNextState(event.getPortletRequest(), "admin/messaging/view.jsp");
     }
 
     public void doSaveValues(FormEvent event) {
         List services = tms.getActiveServices();
         for (int i=0;i<services.size();i++) {
             TmfService service = (TmfService)services.get(i);
             TmfServiceConfig config = service.getConfig();
             List params = config.getParams();
             ArrayList newParamList = new ArrayList();
             boolean isDirty = false;
             for (int j=0;j<params.size();j++) {
                 ConfigParameter param = (ConfigParameter)params.get(j);
                 TextFieldBean value = event.getTextFieldBean(service.getClassname()+"paramValue"+param.getKey());
 
                 System.out.println("\n\n\n "+param.getKey()+" : " +value.getValue()+"("+service.getClassname()+"paramValue"+param.getKey()+")");
                 // if one parameter changed mark the service as changed values
                 if (!value.getValue().equals(param.getValue())) {
                     param.setValue(value.getValue());
                     isDirty = true;
                 }
                 newParamList.add(param);
 
             }
 
             config.setParams(newParamList);
             config.setConfigChanged(isDirty);
             if (isDirty)
                 try {
                     service.save();
                 } catch (TextMessagingException e) {
                     log.error("Problem saving config!");
                 }
 
         }
 
         setNextState(event.getPortletRequest(), "doView");
     }
 
     private TmfService getTmfServiceFromClassName(String classname) {
         List services = tms.getActiveServices();
         for (int i=0;i<services.size();i++) {
             TmfService s = (TmfService)services.get(i);
 
             //System.out.println("\n\n\nCHECKING: "+s.getClassname()+" "+classname);
             if (s.getClassname().equals(classname)) {
                 return s;
             }
         }
         return null;
     }
 
     public void restartServices(FormEvent event) {
 
         CheckBoxBean restartServices = event.getCheckBoxBean("restartBox");
         List services = restartServices.getSelectedValues();
         for (int i=0;i<services.size();i++) {
             TmfService s = getTmfServiceFromClassName((String)services.get(i));
             if (s!=null) {
                 s.shutdown();
                 s.startup();
                 s.getConfig().setConfigChanged(false);
             }
         }
 
         setNextState(event.getPortletRequest(), "doView");
 
 
 
     }
 }
