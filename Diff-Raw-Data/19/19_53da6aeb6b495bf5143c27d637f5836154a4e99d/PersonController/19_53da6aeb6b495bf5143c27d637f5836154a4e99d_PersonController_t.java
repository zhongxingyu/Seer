 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package nl.sogeti.jdc.demo.jee6.banking.controller;
 
 import java.io.Serializable;
 import java.util.List;
 
 import javax.annotation.PostConstruct;
 import javax.ejb.EJB;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.ViewScoped;
 import javax.inject.Inject;
 
 import nl.sogeti.jdc.demo.jee6.banking.boundary.BankingServiceLocal;
 import nl.sogeti.jdc.demo.jee6.banking.constants.Constants;
 import nl.sogeti.jdc.demo.jee6.banking.controller.util.ControllerUtil;
 import nl.sogeti.jdc.demo.jee6.banking.entity.Person;
 import nl.sogeti.jdc.demo.jee6.banking.exception.TransactionRollbackException;
 import nl.sogeti.jdc.demo.jee6.banking.rs.client.SlowRestClient;
 
 import org.slf4j.Logger;
 
 /**
  * @author kanteriv
  */
 @ManagedBean
 @ViewScoped
 public class PersonController implements Serializable {
 
    private static final long serialVersionUID = 23452345234L;
    @Inject
    Logger logger;
    @EJB
    UserData userData;
    @EJB
    BankingServiceLocal bankingService;
    @EJB
    ControllerUtil controllerUtil;
    @EJB
    SlowRestClient slowRestClient;
    @Inject
    int numberOfRows;
 
    private List<Person> allPersons;
 
    private String timespend = "";
 
    @PostConstruct
    public void init() {
       this.logger.debug("init; PersonController.");
       this.controllerUtil.getSession(true).setAttribute("userData", this.userData);
 
       selectAll();
    }
 
    public void save() {
       this.logger.debug("save :: " + getSelected());
       try {
          if (getSelected().getId() == null) {
             this.bankingService.createPerson(getSelected());
          } else {
             this.bankingService.updatePerson(getSelected());
          }
 
          // call below can be very long. Make it asynchronized and wait for max 500 millis.
          Integer slowServiceResult = this.slowRestClient.callSlowMethod();
 
          setTimespend("" + slowServiceResult);
 
          selectAll();
          setSelected(getPersonFromList(getSelected().getClientId()));
          this.controllerUtil.addCallBackParam(Constants.CALLBACK_PARAM_SAVED_FAILED, Boolean.FALSE);
 
       } catch (TransactionRollbackException e) {
 
          this.controllerUtil.addMessage(e.getMessage());
          this.controllerUtil.addCallBackParam(Constants.CALLBACK_PARAM_SAVED_FAILED, Boolean.TRUE);
       }
    }
 
    public void newPerson() {
       this.logger.debug("newPerson()");
       setSelected(new Person());
    }
 
    public int getNumberOfRows() {
       return this.numberOfRows;
    }
 
    public Person getSelected() {
       return this.userData.getSelectedPerson();
    }
 
    public void setSelected(Person selected) {
       this.logger.debug("setSelected(" + selected + ")");
       this.userData.setSelectedPerson(selected);
    }
 
    public void rowEvent() {
       // Nothing to do
    }
 
    public List<Person> getAllPersons() {
       return this.allPersons;
    }
 
    private Person getPersonFromList(String clientId) {
       for (Person person : getAllPersons()) {
          if (person.getClientId().equals(clientId)) {
             return person;
          }
       }
       return null;
    }
 
    private void selectAll() {
       this.logger.debug("selectAll");
       this.allPersons = this.bankingService.findAllPersons();
    }
 
    public String getTimespend() {
       return this.timespend;
    }
 
    public void setTimespend(String timespend) {
       this.timespend = timespend;
    }
 }
