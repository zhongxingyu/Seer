 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package badm;
 
 import cc.test.bridge.BridgeConstants;
 import cc.test.bridge.BridgeConstants.Side;
 import cc.test.bridge.BudgetInterface;
 import cc.test.bridge.LineInterface;
 import cc.test.bridge.NoteInterface;
 import com.mongodb.BasicDBObject;
 import java.util.ArrayList;
 import org.workplicity.util.Helper;
 import org.workplicity.util.MongoHelper;
 import org.workplicity.worklet.WorkletContext;
 
 /**
  * Budget
  * The base of the database. Everything is in some way a part of a Budget.
  * @author Trescenzi
  */
 public class Budget extends BaseModel implements BudgetInterface {
     protected String description;
     protected Double total;
     
     /**
      * 
      * @return The total value of the budget
      */
     public Double getTotal() {
         return total;
     }
 
     /**
      * 
      * @param total The new total value of the budget
      */
     public void setTotal(Double total) {
         this.total = total;
         dirty();
     }
     
     
     /**
      * 
      * @return The budget's description
      */
     @Override
     public String getDescription() {
         BridgeHelper.getHamper().put(this,BridgeConstants.State.UPDATE);
         return description;
     }
     
     /**
      * 
      * @param d The new description for the budget
      */
     @Override
     public void setDescription(String d) {
         description = d;
         dirty();
     }
     
     /**
      * Gets all of the lines associated with the budget.
      * Does not take into account side yet.
      * @param side Expenditures/Income
      * @return The lines associated with the budget
      */
     @Override
     public ArrayList<LineInterface> fetchLines(Side side) {
         BasicDBObject query = new BasicDBObject();
         query.put("entry.budgetId", id);
         try{
             return MongoHelper.query(query,BaseModel.getStoreName(),new Line().getRepositoryName());
         }catch(Exception e){
                 System.out.println("couldn't fetch lines"+e);
         }
         return null;
     }
     
     /**
      * Gets all of the notes associated with the budget.
      * @return The Notes associated with the budget
      */
     @Override
     public ArrayList<NoteInterface> fetchNotes() {
         BasicDBObject query = new BasicDBObject();
         query.put("entry.budgetId", id);
         try{
             return MongoHelper.query(query,BaseModel.getStoreName(),new Note().getRepositoryName());
         }catch(Exception e){
                 System.out.println("couldn't fetch notes"+e);
         }
         return null;
     }
     
     /**
      * Makes a new Line, associates the line with the calling budget
      * then returns the Line.
      * @return The new Line
      */
     @Override
     public LineInterface createLine() {
 	Line ln = new Line();
 	ln.setBudgetId(id);
         return ln;
     }
     
     /**
      * Makes a new Note, associates the Note with the calling budget
      * then returns the Note.
      * @return The new Note
      */
     @Override
     public NoteInterface createNote() {
 	Note n = new Note();
 	n.setBudgetId(id);
         return n;
     }
     
     /**
      * Associates a Note with the calling Budget.
      * @param ni The Note to associate
      */
     @Override
     public void add(NoteInterface ni) {
         Note note = (Note) ni;
         note.setBudgetId(this.id);
     }
     
     /**
      * Deletes a Note.
      * @param ni The Note to delete
      */
     @Override
     public void delete(NoteInterface ni) {
         Note note = (Note) ni;
         note.delete();
     }
     
     /**
      * Associates a Line with the calling Budget.
      * @param li The Line to associate
      */
     @Override
     public void add(LineInterface li) {
         Line line = (Line) li;
         line.setBudgetId(this.id);
     }
     
     /**
      * Deletes a Line.
      * @param li The Line to delete
      */
     @Override
     public void delete(LineInterface li) {
         Line line = (Line) li;
         line.delete();
     }
     
     /**
      * Updates a Line. Commits upon call.
      * @param li The Line to be updated
      */
     @Override
     public void update(LineInterface li) {
         Line line = (Line) li;
         line.commit();
     }
     
     /**
      * Updates a Note. Commits upon call.
      * @param ni The Note to be updated
      */
     @Override
     public void update(NoteInterface ni) {
         Note note = (Note) ni;
         note.commit();
     }
 
     /**
      * 
      * @return A WorkDate?
      */
     @Override
     public org.workplicity.util.WorkDate getUpdateDate() {
         return null;
     }
 
     /**
      * 
      * @param audit The audit to be added
      */
     private void addAudit(Audit audit){
         audit.setBudgetId(id);
     }
     
     /**
      * Updates the Budget then stashes the Audit in AuditLand.
      * @param audit The Audit to use as a refrence for the update
      */
     public void update(Audit audit){
         super.update(audit);
         total += audit.getValue();
         dirty();
         addAudit(audit);
     }
     
     /**
      * Finds a budget with a specific id.
      * @param id The id of the Budget to be found
      * @return The Budget, of null if not found.
      */
     public static Budget find(Integer id) {
 	    BasicDBObject query = new BasicDBObject();
 	    query.put("entry.id", id);
 	    System.out.println(id);
 	    try{
 		return (Budget) MongoHelper.query(query,BaseModel.getStoreName(),new Budget().getRepositoryName()).get(0);
 	    }catch(Exception e){
 		    System.out.println("couldnt find budget #"+id+" "+e);
 	    }
 	    return null;
     }
     
     public static ArrayList<Budget> all(){
         BasicDBObject query = new BasicDBObject();
        BasicDBObject gt = new BasicDBObject("$gt", 1);
         query.put("entry.id", gt);
         try{
             return MongoHelper.query(query,BaseModel.getStoreName(),new Budget().getRepositoryName());
         }catch(Exception e){
                 System.out.println("couldnt find budgets");
         }
         return null;
     }
 
 }
