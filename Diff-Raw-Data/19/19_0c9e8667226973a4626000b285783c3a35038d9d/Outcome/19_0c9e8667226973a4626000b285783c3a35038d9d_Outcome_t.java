 package linkagesviewer.models;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.List;
 
 import linkagesviewer.configuration.DBConnection;
 
 public class Outcome extends NNNObject {
 	//***** DATA MEMBERS *****//
 	private int parentID;
 	private List<Intervention> majorInterventions;
 	private List<Intervention> suggestedInterventions;
 	private List<Intervention> optionalInterventions;
 
         private List<Intervention> correlatedInterventions;
 	private List<Intervention> negativelyCorrelatedInterventions;
 	
 	//***** CONSTRUCTORS *****//
 	public Outcome(int id, int parentID, String name, String code, String definition)
 	{
 		this.id = id;
 		this.parentID = parentID;
 		this.name = name;
 		this.code = code;
 		this.definition = definition;
 		this.majorInterventions = getInterventionsFromDB(EdgeType.MAJOR_INTERVENTION);
 		this.suggestedInterventions = getInterventionsFromDB(EdgeType.SUGGESTED_INTERVENTION);
 		this.optionalInterventions = getInterventionsFromDB(EdgeType.OPTIONAL_INTERVENTION);
 	}
 	
 	
 	//***** METHODS *****//
 	public List<Intervention> getMajorInterventions()
 	{
 		return majorInterventions;
 	}
 	
 	public List<Intervention> getSuggestedInterventions()
 	{
 		return suggestedInterventions;
 	}
 	
 	public List<Intervention> getOptionalInterventions()
 	{
 		return optionalInterventions;
 	}
 	
 	public void addMajorIntervention(Intervention intervention)
 	{
 		majorInterventions.add(intervention);
 	}
 	
 	public void addSuggestedIntervention(Intervention intervention)
 	{
 		suggestedInterventions.add(intervention);
 	}
 	
 	public void addOptionalIntervention(Intervention intervention)
 	{
 		optionalInterventions.add(intervention);
 	}
 	
 	public List<Intervention> getInterventions(EdgeType type)
 	{
 		switch(type)
 		{
 			case MAJOR_INTERVENTION:
 				return getMajorInterventions();
 			case SUGGESTED_INTERVENTION:
 				return getSuggestedInterventions();
 			case OPTIONAL_INTERVENTION:
 				return getOptionalInterventions();
 			default:
 				throw new RuntimeException("Invalid intervention type: " + type.toString());
 		}
 	}
 	
 	private List<Intervention> getInterventionsFromDB(EdgeType type)
 	{
 		String typeString = "major";
 		switch(type)
 		{
 			case MAJOR_INTERVENTION:
 				typeString = "major";
 				break;
 			case SUGGESTED_INTERVENTION:
 				typeString = "suggested";
 				break;
 			case OPTIONAL_INTERVENTION:
 				typeString = "optional";
                                     break;
                         default:
                             throw new RuntimeException("Invalid intervention type: " + type.toString());
 		}
 		
 		// Get major interventions
 		List<Intervention> interventions = new ArrayList<Intervention>();
 		
 		String sql = new StringBuffer()
 			.append("SELECT id, name, nic_code, definition, type ")
 			.append("FROM interventions i JOIN diagnosis_outcome_interventions doi ")
 			.append("ON i.id = doi.intervention_id ")
 			.append("WHERE diagnosis_id = " + parentID + " and outcome_id = " + id + " and [type] = '" + typeString + "'")
 			.toString();
 		
 		Statement stmt;
 		try {
 			stmt = DBConnection.connection.createStatement();
 			ResultSet rs = stmt.executeQuery(sql);
 			
 			while (rs.next()) {
 				String id = rs.getString("id");
 				String name = rs.getString("name");
 				String code = rs.getString("nic_code");
 				String definition = rs.getString("definition");
 				
 				if (code == null) code = "-1";
 				
 				interventions.add(new Intervention(Integer.parseInt(id), name, code, definition));
 			}
 			rs.close();
 			stmt.close();
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		return interventions;
     }
 
    public List<Intervention> getCorrelatedInterventions() {
        List<Intervention> interventions = new ArrayList<Intervention>();
        String sql = new StringBuffer()
                .append("SELECT id, name, nic_code, definition ")
               .append("FROM interventions i JOIN diagnosis_outcome_intervention_correlations c ")
                .append("ON c.intervention_id = i.id ")
                .append("WHERE c.outcome_id = " + id + " ")
                .append("AND c.diagnosis_id = " + parentID + " ")
                .append("AND correlation > 0 ")
                .append("ORDER BY CORRELATION DESC").toString();
        System.out.println(sql);
 
 	try {
 		ResultSet rs = DBConnection.connection.createStatement().executeQuery(sql);
 		while (rs.next()) {
 			// Intervention(int id, String name, String code, String definition)
 			interventions.add(new Intervention(
 						rs.getInt("id"),
 						rs.getString("name"),
                                                 rs.getString("nic_code"),
                                                 rs.getString("definition")));
 		}
 	} catch (SQLException e) {
 		e.printStackTrace();
 	}
        return interventions;
 
    }
 
    public List<Intervention> getNegativelyCorrelatedInterventions() {
        List<Intervention> interventions = new ArrayList<Intervention>();
        String sql = new StringBuffer()
                .append("SELECT id, name, nic_code, definition ")
                .append("FROM interventions i JOIN diagnosis_outcome_intervention_correlations c ")
                .append("ON c.intervention_id = i.id ")
                .append("WHERE c.outcome_id = " + id + " ")
                .append("AND c.diagnosis_id = " + parentID + " ")
                .append("AND correlation < 0 ")
                .append("ORDER BY CORRELATION DESC").toString();
        System.out.println(sql);
 
 	try {
 		ResultSet rs = DBConnection.connection.createStatement().executeQuery(sql);
 		while (rs.next()) {
 			// Intervention(int id, String name, String code, String definition)
 			interventions.add(new Intervention(
 						rs.getInt("id"),
 						rs.getString("name"),
                                                 rs.getString("nic_code"),
                                                 rs.getString("definition")));
 		}
 	} catch (SQLException e) {
 		e.printStackTrace();
 	}
        return interventions;
 
    }
 
    @Override
    public String toString() {
        return this.getName();
    }
 
     @Override
     public List<NNNObject> getPositivelyCorrelatedObjects() {
         List<NNNObject> correlatedObjects = new ArrayList<NNNObject>();
         for (Intervention i : getCorrelatedInterventions()) {
             correlatedObjects.add((NNNObject) i);
         }
         return correlatedObjects;
     }
 
     @Override
     public List<NNNObject> getNegativelyCorrelatedObjects() {
         List<NNNObject> correlatedObjects = new ArrayList<NNNObject>();
         for (Intervention i : getNegativelyCorrelatedInterventions()) {
             correlatedObjects.add((NNNObject) i);
         }
         return correlatedObjects;
     }
 
 /*public List<Outcome> getCorrelatedOutcomes() {
 	List<Outcome> outcomes = new ArrayList<Outcome>();
 
 	String sql = new StringBuffer()
 		.append("SELECT id, noc_code, isnull(name_current,name_2005), definition ")
 		.append("FROM [dbo].[outcomes] o JOIN [dbo].[outcome_outcome_correlations] c")
 		.append("ON c.outcome_id_b = o.id ")
 		.append("WHERE c.outcome_id_a = " + id).toString();
 
 	try {
 		ResultSet rs = DBConnection.connection.createStatement().executeQuery(sql);
 		while (rs.next()) {
 			// public Outcome(int id, int parentID, String name, String code, String definition)
 			outcomes.add(new Outcome(
 						rs.getInteger("id"),
 						rs.get
 		}
 	} catch (SQLException e) {
 		e.printStackTrace();
 	}
 
 	return outcomes;
 }*/
 
 }
