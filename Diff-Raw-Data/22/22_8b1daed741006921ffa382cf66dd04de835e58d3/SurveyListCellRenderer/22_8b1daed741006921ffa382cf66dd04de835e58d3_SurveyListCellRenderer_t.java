 package br.org.indt.ndg.lwuit.ui.renderers;
 
 import com.sun.lwuit.Component;
 import com.sun.lwuit.List;
 
 public class SurveyListCellRenderer extends SimpleListWithAnimatedTextCellRenderer {
 
     public Component getListCellRendererComponent(List list, Object value, int index, boolean isSelected) {
         if (index == 0) {
            m_label.setAlignment(Component.CENTER);
         } else {
            m_label.setAlignment(Component.LEFT);
         }
         return super.getListCellRendererComponent(list,value,index,isSelected);
     }
 
     protected String getText(Object value) {
         String surveyName = (String) value;
         return (String)surveyName;
     }
 
     protected String getTextToAnimate() {
         return m_label.getText();
     }
 }
