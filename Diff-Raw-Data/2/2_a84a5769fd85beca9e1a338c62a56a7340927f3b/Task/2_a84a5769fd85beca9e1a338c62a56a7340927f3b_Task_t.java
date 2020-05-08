 package fr.eservice.todo;
 
 /**
  * Une tâche à réaliser.<br/>
  * Cette tâche devrait posséder :<ul>
  *   <li>une référence (identifiant unique)</li>
  *   <li>un titre court</li>
  *   <li>une description</li>
  *   <li>une date d'ajout</li>
  *   <li>une date limite de réalisation</li>
  *   <li>une propriété pour savoir si oui ou non elle a été réalisée</li>
  * </ul>
  * 
  * @author guillaume
  */
 public class Task {
 
 	
 	public int getReference() {
 		// TODO Complete this code
 		return 0;
 	}
 	
 	public String getTitle() {
 		// TODO Complete this code
 		return null;
 	}
 	
 	public String getDescription() {
 		// TODO Complete this code
 		return null;
 	}
 	
 	public long getAddedDate() {
 		// TODO Complete this code
 		return 0L;
 	}
 	
 	public long getTargetDate() {
 		// TODO Complete this code
 		return 0L;
 	}
 	
 	public boolean hasBeenCompleted() {
 		// TODO Complete this code
 		return true;
 	}
 	
 	/**
 	 * Une méthode pour créer une nouvelle tâche.
 	 * 
 	 * @param title l'intitullé de la tache.
 	 * @param description la description de la tache.
 	 * @param target la date limite de réalisation.
 	 * 
 	 * @return une nouvelle tâche à réaliser.
 	 * 
 	 * @throws ParameterException
 	 */
 	public static Task create(String title, String description, Long target) 
 	throws ParameterException
 	{
 		return null;
 	}
 
 
 	public void complete() {
		// return null;
 	}
 
 
 	
 }
