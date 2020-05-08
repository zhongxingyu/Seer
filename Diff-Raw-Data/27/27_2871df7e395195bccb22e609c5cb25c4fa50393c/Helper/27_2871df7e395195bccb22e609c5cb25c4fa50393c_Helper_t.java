 package gvhd;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import graphDB.explore.NodeHelper;
 
 import org.neo4j.graphdb.*;
 import org.neo4j.kernel.EmbeddedGraphDatabase;
 
 public class Helper {
 
 	public static List<Double> NormalizeArray(List<Double> values)
 	{
 		List<Double> results = new ArrayList<Double>();
 		double somme = 0;
 		for(int i = 0; i < values.size(); i++)
 		{
 			somme += values.get(i);
 		}
 		if(somme > 0)
 		{
 			for(int i = 0; i < values.size(); i++)
 				results.add(values.get(i) / somme);
 		}
 		else
 			for(int i = 0; i < values.size(); i++)
 				results.add(0.0);
 		return results;			
 	}
 	
 	public static List<Pair<Double,Double>> NormalizeArrayPair(List<Pair<Double,Double>> values)
 	{
 		List<Pair<Double,Double>> results = new ArrayList<Pair<Double,Double>>();
 		double somme = 0;
 		for(int i = 0; i < values.size(); i++)
 		{
 			somme += values.get(i).second;
 		}
 		if(somme > 0)
 		{
 			for(int i = 0; i < values.size(); i++)
 				results.add(new Pair<Double,Double>(values.get(i).first, values.get(i).second / somme));
 		}
 		else
 			for(int i = 0; i < values.size(); i++)
 				results.add(new Pair<Double,Double>(0.0, 0.0));
 		return results;			
 	}
 	
 	public static String BuildGraphValues(List<List<Pair<Double,Double>>> array, List<List<Pair<Double,Double>>> arrayGV, List<String> names, List<String> namesGV, long averageGVHDDate)
 	{
 	   	String json = "";	
 		for(int i = 0; i < array.size(); i++)
 		{
 			List<Pair<Double,Double>> tmpArray = Helper.NormalizeArrayPair(array.get(i));
 			if(tmpArray.size() > 0)
 			{
 				Double dateY = (double)averageGVHDDate;
 				String strDES = "";		
 				for(int j = 0; j < tmpArray.size(); j++)
 					if(tmpArray.get(j).first <= averageGVHDDate)
 						strDES += ",{x:" + (tmpArray.get(j).first / dateY) + ", y: " + tmpArray.get(j).second + "}";
 				if(!strDES.isEmpty())
 					json += ",{values:[" + strDES.substring(1) + "], key: '" + names.get(i) + "'}";
 			}
 		}		
 		for(int i = 0; i < arrayGV.size(); i++)
 		{
 			List<Pair<Double,Double>> tmpArray = Helper.NormalizeArrayPair(arrayGV.get(i));
 			if(tmpArray.size() > 0)
 			{
 				Double dateY = 0.0;//tmpArray.get(tmpArray.size() - 1).first;
 				for(int j = 0; j < tmpArray.size(); j++)
 					if(tmpArray.get(j).first > dateY)
 						dateY = tmpArray.get(j).first;
 				if(dateY > 0)
 				{
 					String strDESGv = "";		
 					for(int j = 0; j < tmpArray.size(); j++)
 						if(tmpArray.get(j).first <= dateY)
 							strDESGv += ",{x:" + (tmpArray.get(j).first / dateY) + ", y: " + tmpArray.get(j).second + "}";
 					if(!strDESGv.isEmpty())
 						json += ",{values:[" + strDESGv.substring(1) + "], key: '" + namesGV.get(i) + " (GV)'}";
 				}
 			}
 		}
 		return "[" + json.substring(1) + "]";	
 	}
 	public static String BuildGraphAxis(List<String> dates, List<Double> values, List<String> comments)
 	{
 		String output = "";
 		for(int i = 0 ; i < dates.size(); i++)
 		{
 			output += ",{x:" + dates.get(i) + ", y: " + values.get(i).toString() + comments.get(i) + "}";
 		}
		if(output.length() > 0)
			return output.substring(1);
		else
			return output;
 		//arrayDESLibre  += ",{x:" + Long.toString(daySince) + ", y: " + sample.getProperty("Ratio DES Libre") + strToAdd + "}";	
 	}
 	public static Node CreatePatient(EmbeddedGraphDatabase graphDb, Node project, String name)
 	{
 		RelationshipType newNodeRel = DynamicRelationshipType.withName("Link");
 		Node patient = graphDb.createNode();
 		project.createRelationshipTo(patient, newNodeRel);
 		patient.setProperty("Name", name);
 		patient.setProperty("type", "Patient");
 		return patient;
 	}
 	
 	public static Node CreateControlCD(EmbeddedGraphDatabase graphDb, Node project, String name)
 	{
 		RelationshipType newNodeRel = DynamicRelationshipType.withName("Link");
 		Node control = graphDb.createNode();
 		project.createRelationshipTo(control, newNodeRel);
 		control.setProperty("Name", name);
 		control.setProperty("type", "ControlCD");
 		return control;
 	}
 
 	public static Node CreateControl(EmbeddedGraphDatabase graphDb, Node project, String name)
 	{
 		RelationshipType newNodeRel = DynamicRelationshipType.withName("Link");
 		Node control = graphDb.createNode();
 		project.createRelationshipTo(control, newNodeRel);
 		control.setProperty("Name", name);
 		if(NodeHelper.isNumeric(name))
 			control.setProperty("Control", Double.parseDouble(name));
 		else
 			control.setProperty("Control", name);
 		control.setProperty("type", "Control");
 		return control;
 	}
 	
 	public static Node CreateSample(EmbeddedGraphDatabase graphDb, Node patient, String moment, String date, String creatinine, String comment)
 	{
 		RelationshipType newNodeRel = DynamicRelationshipType.withName("Link");
 		Node sample = graphDb.createNode();
 		patient.createRelationshipTo(sample, newNodeRel);
 
 		sample.setProperty("type", "Sample");
 		if(!moment.isEmpty())
 			sample.setProperty("Moment", moment);
 		if(!date.isEmpty())
 			sample.setProperty("Date", date);
 		if(!creatinine.isEmpty())
 			sample.setProperty("Creatinine", Double.parseDouble(creatinine));
 		if(!comment.isEmpty())
 			sample.setProperty("Comment", comment);
 		return sample;
 	}
 	
 	public static Node CreateControlDuplicate(EmbeddedGraphDatabase graphDb, Node patient)
 	{
 		RelationshipType newNodeRel = DynamicRelationshipType.withName("Link");
 		Node sample = graphDb.createNode();
 		patient.createRelationshipTo(sample, newNodeRel);
 
 		sample.setProperty("type", "ControlDuplicate");
 		return sample;
 	}
 }
