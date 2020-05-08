 
 package leen.meij.controllers;
 
 import leen.meij.dataAccess.VoertuigDataAccess;
 import leen.meij.utilities.*;
 import leen.meij.*;
 import leen.meij.views.VoertuigDetailsView;
 import leen.meij.views.VoertuigView;
 
 public class VoertuigController extends Controller
 {
 
 	private VoertuigDataAccess voertuigDataAccess = new VoertuigDataAccess();
 	
 	public View voertuigOverzichtRaadplegenTask()
 	{
 		// TODO - implement {class}.{operation}
 		return new VoertuigView(voertuigDataAccess.selectAll());
 	}
 
 	public View voertuigToevoegenTask()
 	{
 		// TODO - implement {class}.{operation}
 		return new VoertuigDetailsView(new Voertuig());
 	}
 
 	/**
 	 * 
 	 * @param voertuig
 	 */
 	public View voertuigToevoegenTask(Voertuig voertuig)
 	{
 		// validate user data
 		voertuig.validateFields();
                 
 		if (voertuig.isValid())
 		{
 
 			voertuig = voertuigDataAccess.add(voertuig);
 			                 
 			// redirect back to the overview
 			return voertuigOverzichtRaadplegenTask();
 		}
 		
 		// show the same view with error messages
 		return new VoertuigDetailsView(voertuig);
 	}
 
 	/**
 	 * 
 	 * @param voertuigID
 	 */
 	public View voertuigWijzigenTask(Integer voertuigID)
 	{
 		Voertuig voertuig = voertuigDataAccess.select(voertuigID);
 
 		return new VoertuigDetailsView(voertuig);
 	}
 
 	/**
 	 * 
 	 * @param voertuig
 	 */
 	public View voertuigWijzigenTask(Voertuig voertuig)
 	{
 		voertuig.validateFields();
 		
 		if(voertuig.isValid())
 		{
 			voertuig = voertuigDataAccess.edit(voertuig);
 			
 			// redirect back to the overview
 			return voertuigOverzichtRaadplegenTask();
 		}
 		
 		// show the same view with error messages
 		return new VoertuigDetailsView(voertuig);
 	}
 
 	/**
 	 * 
 	 * @param voertuigID
 	 */
 	public View voertuigVerwijderenTask(Integer voertuigID)
 	{
		// TODO - implement {class}.{operation}
		throw new UnsupportedOperationException();
 	}
 
 	/**
 	 * 
 	 * @param voertuigID
 	 */
 	public View onderhoudToevoegenTask(Integer voertuigID)
 	{
 		// TODO - implement {class}.{operation}
 		throw new UnsupportedOperationException();
 	}
 
 	/**
 	 * 
 	 * @param onderhoud
 	 */
 	public View onderhoudToevoegenTask(Onderhoud onderhoud)
 	{
 		// TODO - implement {class}.{operation}
 		throw new UnsupportedOperationException();
 	}
 
 }
