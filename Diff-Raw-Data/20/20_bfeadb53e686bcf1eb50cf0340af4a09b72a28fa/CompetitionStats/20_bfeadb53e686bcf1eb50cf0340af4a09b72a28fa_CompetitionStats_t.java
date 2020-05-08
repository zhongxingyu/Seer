 package fi.valonia.pyorallatoihin.views.login;
 
 import java.util.List;
 
 import com.vaadin.ui.CssLayout;
 import com.vaadin.ui.GridLayout;
 import com.vaadin.ui.Label;
 import com.vaadin.ui.themes.ChameleonTheme;
 
 import fi.valonia.pyorallatoihin.Messages;
 import fi.valonia.pyorallatoihin.PyorallaToihinUI;
 import fi.valonia.pyorallatoihin.data.CompanyInfo;
 
 public class CompetitionStats extends CssLayout {
     private static final long serialVersionUID = 8524524551101097444L;
 
     public CompetitionStats(List<CompanyInfo> companies) {
         addStyleName("competition-stats");
 
         setWidth("600px");
         setHeight("150px");
 
        PyorallaToihinUI ui = PyorallaToihinUI.get();
 
         GridLayout layout = new GridLayout(6, 1);
        Label position = new Label(ui.getMessages().getString(
                 Messages.competition_company_position));
        Label name = new Label(ui.getMessages().getString(
                 Messages.competition_company_name));
        Label workers = new Label(ui.getMessages().getString(
                 Messages.competition_company_workers));
        Label participants = new Label(ui.getMessages().getString(
                 Messages.competition_company_participants));
        Label markers = new Label(ui.getMessages().getString(
                 Messages.competition_company_markers));
        Label ratio = new Label(ui.getMessages().getString(
         		Messages.competition_company_ratio));
 
         position.addStyleName(ChameleonTheme.LABEL_H4);
         name.addStyleName(ChameleonTheme.LABEL_H4);
         workers.addStyleName(ChameleonTheme.LABEL_H4);
         participants.addStyleName(ChameleonTheme.LABEL_H4);
         markers.addStyleName(ChameleonTheme.LABEL_H4);
         ratio.addStyleName(ChameleonTheme.LABEL_H4);
 
         layout.addComponent(position);
         layout.addComponent(name);
         layout.addComponent(workers);
         layout.addComponent(participants);
         layout.addComponent(markers);
         layout.addComponent(ratio);
         int i = 1;
         if (companies != null) {
             for (CompanyInfo companyInfo : companies) {
                 Label compPosition = new Label(i++ + "");
                 Label compName = new Label(companyInfo.getName());
                 Label compWorkers = new Label(companyInfo.getSize() + "");
                 Label compParticipants = new Label(companyInfo.getRegistered()
                         + "");
                 Label compMarkers = new Label(companyInfo.getTotalMarkers()
                         + "");
                 Label compRatio = new Label(companyInfo.getRatio() + "");
                 layout.addComponent(compPosition);
                 layout.addComponent(compName);
                 layout.addComponent(compWorkers);
                 layout.addComponent(compParticipants);
                 layout.addComponent(compMarkers);
                 layout.addComponent(compRatio);
             }
         }
         layout.setMargin(true);
         layout.setWidth("100%");
         layout.setHeight(null);
         addComponent(layout);
     }
 
 }
