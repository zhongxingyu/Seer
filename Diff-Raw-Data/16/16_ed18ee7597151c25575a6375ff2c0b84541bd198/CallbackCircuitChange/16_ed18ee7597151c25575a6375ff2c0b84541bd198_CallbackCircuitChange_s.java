 package trains;
 

//dans JGroups, on doit pouvoir sur unobjet particulier, appeler l'une ou l'autred es callbacks
// donc les callbacks doivent pouvoir tre appeles en tant que mthodes sur un pbjet
// tout en restant un objet pour passer en JNI
 public interface CallbackCircuitChange {
	//message --> crer une classe message
	//sender
	public void run();
 }
