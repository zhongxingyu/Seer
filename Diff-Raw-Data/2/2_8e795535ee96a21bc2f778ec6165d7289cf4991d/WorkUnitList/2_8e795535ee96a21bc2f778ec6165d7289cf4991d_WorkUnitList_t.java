 package war.webapp.action;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 
 import javax.faces.component.UIInput;
 import javax.faces.component.html.HtmlDataTable;
 import javax.faces.context.FacesContext;
 import javax.faces.event.ValueChangeEvent;
 import javax.faces.model.SelectItem;
 import javax.servlet.http.HttpServletRequest;
 
 //import war.webapp.dto.WorkUnitDto;
 import war.webapp.dto.WorkUnitDto;
 import war.webapp.model.DayDuty;
 import war.webapp.model.State;
 import war.webapp.model.User;
 import war.webapp.model.WorkUnit;
 import war.webapp.service.WorkUnitManager;
 
 public class WorkUnitList extends BasePage {
 
 
     private WorkUnitManager workUnitManager;
     private List<WorkUnitDto> unitsDto;
     
     public WorkUnitList() {
         setSortColumn("hoursAmount");
         setAscending(false);
     }
     
     public List<WorkUnitDto> getAllWorkUnits() {
         List<WorkUnit> units = workUnitManager.getAll();
         unitsDto = new ArrayList<WorkUnitDto>();
         if (units != null) {
             for(WorkUnit unit: units)
             {
                 WorkUnitDto unitDto = new WorkUnitDto();
                 unitDto.setDate(unit.getDate());
                 unitDto.setEmployee(unit.getEmployee());
                 unitDto.setEmployer(unit.getEmployer());
                 unitDto.setHoursAmount(unit.getHoursAmount());
                 unitDto.setId(unit.getId());
                 unitDto.setState(unit.getState());
                 unitDto.setWorkDescription(unit.getWorkDescription());
                 unitsDto.add(unitDto);               
             }
             return (List<WorkUnitDto>)sort(new ArrayList(unitsDto));
         } else {
             return unitsDto;
         }
     }
 
     public WorkUnitManager getWorkUnitManager() {
         return workUnitManager;
     }
 
    public void setWorkUnitManager(WorkUnitManager workUnitManager) {
         this.workUnitManager = workUnitManager;
     }
     
     public List<SelectItem> getStatesList() {
         List<SelectItem> statesList = new ArrayList<SelectItem>();
         for (State state: State.values()) {
             statesList.add(new SelectItem(state, state.toString()));
         }
         return statesList;
     }
     
     public String deleteSelected() {
         for (WorkUnitDto unit: unitsDto) {
             if (unit.getSelected()) {
                 WorkUnit unitToDelete = workUnitManager.get(unit.getId());
                 workUnitManager.deleteWorkUnit(unitToDelete);
             }
         }
         return null;
     }
     
     public String updateSelected() {
         for (WorkUnitDto unit: unitsDto) {
             if (unit.getSelected()) {
                 WorkUnit unitToUpdate = workUnitManager.get(unit.getId());
                 //update state
                 unitToUpdate.setState(unit.getState());
                 workUnitManager.save(unitToUpdate);
             }
         }
         return null;
     }
 
 }
