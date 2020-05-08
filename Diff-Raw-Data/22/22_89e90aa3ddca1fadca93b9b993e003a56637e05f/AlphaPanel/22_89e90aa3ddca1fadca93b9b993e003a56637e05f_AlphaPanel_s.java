 package edu.cudenver.bios.powercalculator.client.panels;
 
 import java.util.ArrayList;
 
 import com.google.gwt.event.dom.client.ChangeEvent;
 import com.google.gwt.event.dom.client.ChangeHandler;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.Grid;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 
 import edu.cudenver.bios.powercalculator.client.PowerCalculatorConstants;
 import edu.cudenver.bios.powercalculator.client.PowerCalculatorGUI;
 import edu.cudenver.bios.powercalculator.client.TextValidation;
 import edu.cudenver.bios.powercalculator.client.listener.AlphaListener;
 
 public class AlphaPanel extends Composite
 {
     protected TextBox alphaTextBox = new TextBox();
     protected HTML alphaErrorHTML = new HTML();
     protected ArrayList<AlphaListener> listeners = new ArrayList<AlphaListener>();
 
     public AlphaPanel()
     {
         Grid grid = new Grid(1, 3);
         grid.setWidget(0, 0, new HTML(PowerCalculatorGUI.constants.textLabelAlpha()));
         grid.setWidget(0, 1, alphaTextBox);
         grid.setWidget(0, 2, alphaErrorHTML);
         alphaErrorHTML.setStyleName(PowerCalculatorConstants.STYLE_MESSAGE);
        alphaErrorHTML.setStyleName(PowerCalculatorConstants.STYLE_MESSAGE_ERROR);
 
         alphaTextBox.addChangeHandler(new ChangeHandler() {
             public void onChange(ChangeEvent e)
             {
                 try
                 {
                     double alpha = TextValidation.parseDouble(alphaTextBox.getText(), 0, 1);
                     for(AlphaListener listener : listeners) listener.onAlpha(alpha);
                     TextValidation.displayOkay(alphaErrorHTML, PowerCalculatorGUI.constants.okay());
                 }
                 catch (NumberFormatException nfe)
                 {
                     TextValidation.displayError(alphaErrorHTML, PowerCalculatorGUI.constants.errorAlphaInvalid());
                     alphaTextBox.setText("");
                     for(AlphaListener listener : listeners) listener.onAlphaInvalid();
 
                 }
             }
         }); 
         
         initWidget(grid);
     }
 
     public String getAlpha()
     {
         return alphaTextBox.getText();
     }
     
     public void reset()
     {
         alphaTextBox.setText("");
         alphaErrorHTML.setText("");
     }
     
     public void setAlpha(String alphaStr)
     {
         try
         {
             double alpha = TextValidation.parseDouble(alphaStr, 0, 1);
             for(AlphaListener listener : listeners) listener.onAlpha(alpha);
             TextValidation.displayOkay(alphaErrorHTML, PowerCalculatorGUI.constants.okay());
         }
         catch (NumberFormatException nfe)
         {
             alphaTextBox.setText("");
             for(AlphaListener listener : listeners) listener.onAlphaInvalid();
         }
     }
 }
