 package cx.ath.jbzdak.jpaGui.ui.form;
 
 import cx.ath.jbzdak.common.famfamicons.IconManager;
 import cx.ath.jbzdak.jpaGui.ClassHandler;
 import cx.ath.jbzdak.jpaGui.Utils;
 import cx.ath.jbzdak.jpaGui.ui.error.ErrorHandlers;
 import cx.ath.jbzdak.jpaGui.ui.error.ErrorHandlers.Formatter;
 import net.miginfocom.swing.MigLayout;
 import org.apache.commons.lang.StringUtils;
 import org.slf4j.Logger;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.util.Map;
 import java.util.ResourceBundle;
 
 /**
  * @author Jacek Bzdak jbzdak@gmail.com
  *         Date: 2009-04-20
  */
 public class FormPanelMock<T extends Component,FE extends FormElement<T>> extends JPanel {
 
    private static final Logger LOGGER = Utils.makeLogger();
 
    //LOWPRIO zmienić na cx.ath.jbzdak.jpaGui.beanFormatter.PatternBeanFormatter
    private static final String LABEL_PATTERN = "<html><strong>LABEL_NAME</strong></html>";
 
    protected final FE formElement;
 
    @SuppressWarnings({"WeakerAccess"})
    protected Map<String, String> constraints;
 
    private boolean helpButtonVisible;
 
    //Komponenty wyświetlane tworone przez tą instancję
 
    @SuppressWarnings({"WeakerAccess"})
    protected JLabel nameLabel;
 
    @SuppressWarnings({"WeakerAccess"})
    protected JButton errorBtn;
 
    protected JButton helpButton;
 
    @SuppressWarnings({"WeakerAccess"})
    protected final JLabel errorLabel = new JLabel();
 
    private Object message;
 
    private Icon noErrorIcon;
 
    private Icon errorIcon;
 
    private final ResourceBundle bundle;
 
    //TODO uporządkować - tj. dodać statyczną zmienną z domyślnymi i tutaj na początek apodawać nulla
    @SuppressWarnings({"WeakerAccess"})
    protected final ClassHandler<Formatter> errorHandlers = ErrorHandlers.createShortHandlers();
 
 
    public FormPanelMock(FE formElement) {
       this(formElement, FormPanelConstraints.createDefaultConstraints(), null);
    }
 
     public FormPanelMock(FE formElement, Map<String, String> constraints){
        this(formElement, constraints, null);
     }
    //TODO Wyseparować kiedyś same
    public FormPanelMock(FE formElement, Map<String, String> constraints, ResourceBundle bundle) {
       this.formElement = formElement;
       this.constraints = constraints;
       this.bundle = bundle;
       this.formElement.addPropertyChangeListener("error", new PropertyChangeListener() {
          @Override
          public void propertyChange(PropertyChangeEvent evt) {
            if((Boolean) evt.getNewValue()){
               errorBtn.setIcon(getErrorIcon());
               errorBtn.setFocusable(true);
            }else{
               errorBtn.setIcon(getNoErrorIcon());
               errorBtn.setFocusable(false);
            }
          }
       });
       this.formElement.addPropertyChangeListener("shortDescription", new PropertyChangeListener(){
          @Override
          public void propertyChange(PropertyChangeEvent evt) {
             setToolTipText((String) evt.getNewValue());
             helpButton.setToolTipText(getString((String) evt.getNewValue()));
          }
       });
       this.formElement.addPropertyChangeListener("longDescription", new PropertyChangeListener(){
          @Override
          public void propertyChange(PropertyChangeEvent evt) {
             if(isHelpButtonVisible()){
                getHelpButton().setEnabled(!StringUtils.isEmpty(getString((String) evt.getNewValue())));
             }
          }
       });
       this.formElement.addPropertyChangeListener("errorMessage", new PropertyChangeListener() {
          @Override
          public void propertyChange(PropertyChangeEvent evt) {
             setMessage(evt.getNewValue());
          }
       });
       helpButtonVisible = formElement.getLongDescription()!=null;
       initLayout();
    }
 
    void initLayout(){
       removeAll();
       setLayout(new MigLayout(constraints.get("layout"), constraints.get("columns"), constraints.get("rows")));
       add(getNameLabel(), constraints.get("nameLabel"));
       add(formElement.getRenderer(), constraints.get("renderer"));
       add(getErrorBtn(), constraints.get("errorBtn"));
       add(errorLabel, constraints.get("errorLabel"));
       errorLabel.setVisible(false);
       if(isHelpButtonVisible()){
          add(getHelpButton(), constraints.get("helpBtn"));
       }
    }
 
    protected String getString(String key){
       if(bundle==null || key==null || !bundle.containsKey(key)){
          return key;
       }
       return  bundle.getString(key);
    }
 
    public JButton getHelpButton() {
       if (helpButton == null) {
          helpButton = Utils.createIconButton(IconManager.getScaled("help", 1.5));
          helpButton.setFocusable(false);
          helpButton.addActionListener(new ActionListener(){
             @Override
             public void actionPerformed(ActionEvent e) {
                if(StringUtils.isEmpty((formElement.getLongDescription()))){
                   if(!StringUtils.isEmpty(formElement.getShortDescription())){
                      ToolTipManager.sharedInstance().mouseMoved(
                              new MouseEvent(helpButton, 0, 0, 0,
                                             helpButton.getWidth()/2,
                                             helpButton.getHeight()/2,
                                             0, true));
                   }
                }else{
                   JOptionPane.showMessageDialog(helpButton,
                                                 getString(formElement.getLongDescription()),
                                                 "Pomoc",
                                                 JOptionPane.INFORMATION_MESSAGE);
                }
             }
          });
       }
       return helpButton;
    }
 
    JButton getErrorBtn() {
       if(errorBtn==null){
          errorBtn = Utils.createIconButton(getNoErrorIcon());
          errorBtn.setFocusable(false);
          errorBtn.addActionListener(new ActionListener(){
             @Override
             public void actionPerformed(ActionEvent e) {
                if(!errorLabel.isVisible()){
                   if(message!=null){
                      String erroTxt = errorHandlers.getHandler(message).getMessage(message);
                      errorLabel.setText(erroTxt);
                      errorLabel.setToolTipText(erroTxt);
                      errorLabel.setVisible(true);
                      if (message instanceof Throwable) {
                         LOGGER.info("Error message in FormPanel is", (Throwable)message);                     
                      }
                }
                }else{
                   errorLabel.setVisible(false);
                }
             }
          });
       }
       return errorBtn;
    }
 
    public boolean isHelpButtonVisible() {
       return helpButtonVisible;
    }
 
    public void setHelpButtonVisible(boolean helpButtonVisible) {
       if(this.helpButtonVisible != helpButtonVisible){
          this.helpButtonVisible = helpButtonVisible;
          if(!helpButtonVisible){
             helpButton = null; //Coby trafił do GC
          }
          initLayout();
       }
    }
 
    public Object getMessage() {
       return message;
    }
 
    public void setMessage(Object message) {
       this.message = message;
       if(message==null){
          errorLabel.setVisible(false);
       }
    }
 
    JLabel getNameLabel() {
       if(nameLabel==null){
          nameLabel = new JLabel();
          nameLabel.setText(LABEL_PATTERN.replaceAll("LABEL_NAME", getString(formElement.getName())));
       }
       return nameLabel;
    }
 
    public Map<String, String> getConstraints() {
       return constraints;
    }
 
    public void setConstraints(Map<String, String> constraints) {
       this.constraints = constraints;
       initLayout();
    }
 
    Icon getNoErrorIcon() {
       if (noErrorIcon == null) {
          return IconManager.getScaled("no_error_icon", 1.5);
       }
       return noErrorIcon;
    }
 
    public void setNoErrorIcon(Icon noErrorIcon) {
       this.noErrorIcon = noErrorIcon;
    }
 
    Icon getErrorIcon() {
       if (errorIcon == null) {
          return IconManager.getScaled("error_icon", 1.5);
       }
       return errorIcon;
    }
 
    public FE getFormElement() {
 		return formElement;
 	}
 
    public void setErrorIcon(Icon errorIcon) {
       this.errorIcon = errorIcon;
    }
 }
