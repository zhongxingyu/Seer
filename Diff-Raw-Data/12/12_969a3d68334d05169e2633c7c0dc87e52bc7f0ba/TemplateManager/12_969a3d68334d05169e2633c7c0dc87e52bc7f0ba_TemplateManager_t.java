 package engine;
 
import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import engine.uid.OutOfUidsException;
 
public class TemplateManager implements Serializable {

	private static final long serialVersionUID = 9045605830987464993L;
 
 	public List<TemplateDayImpl> allDayTemplates = null;
 	
 	EngineController engineController = null;
 	
 	public TemplateManager(EngineController engineController) {
 		this.engineController = engineController;
 		allDayTemplates = new ArrayList<TemplateDayImpl>();
 	}
 	
 	public TemplateDay createNewDayTemplate(String name, List<Position> positions) throws OutOfUidsException {
 		int uid = engineController.getUidManager().generateTemplateDayUid();
 		TemplateDayImpl templateDayImpl = new TemplateDayImpl(uid, name, positions);
 		allDayTemplates.add(templateDayImpl);
 		return (TemplateDay)templateDayImpl;
 	}
 	
 	public Position createPosition(int requiredRoleUid, Date startTime, Date endTime) throws OutOfUidsException {
 		Role requiredRole = engineController.getRoleDirectory().getRole(requiredRoleUid);
 		int uid = engineController.getUidManager().generatePositionUid();
 		Position position = new Position(requiredRole, uid, startTime, endTime);
 		return position;
 	}
 	
 	public TemplateDay updateDayTemplate(int uid, String name, List<Position> positions) {
 		// TODO: implement
 		return null;
 	}
 	
 	public TemplateDay getTemplateDay(int uid) {
 		TemplateDay retval = null;
 		for (TemplateDay template: allDayTemplates) {
 			if (template.getUid() == uid) {
 				retval = template;
 				break;
 			}
 		}
 		return retval;
 	}
  
 	class TemplateDayImpl implements TemplateDay {
 
 		private static final long serialVersionUID = 3046419887197749536L;
 		
 		private int uid;
 		private String name = null;
 		private List<Position> positions = null;
 		
 		public TemplateDayImpl(int uid, String name, List<Position> positions) {
 			this.uid = uid;
 			this.name = name;
 			this.positions = positions;
 		}
 		
 		@Override
 		public String getName() {
 			return name;
 		}
 		
 		public void setName(String name) {
 			this.name = name;
 		}
 
 		@Override
 		public int getUid() {
 			return uid;
 		}
 
 		@Override
 		public List<Position> getPositions() {
 			ArrayList<Position> returnList = new ArrayList<Position>();
 			for (Position pos : positions) {
 				returnList.add(pos);
 			}
 			return returnList;
 		}
 		
 		public void addPosition(Position position) {
 			positions.add(position);
 		}
 		
 		public void updatePositions(List<Position> newPositions) {
 			positions.clear();
 			for (Position pos : newPositions) {
 				positions.add(pos);
 			}
 		}
 	}
 	
 }
