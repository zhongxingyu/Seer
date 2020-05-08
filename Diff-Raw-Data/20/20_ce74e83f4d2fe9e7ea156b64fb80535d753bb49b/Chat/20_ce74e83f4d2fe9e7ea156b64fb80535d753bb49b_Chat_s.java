 package br.model.logic;
 
 import br.common.UpdateArguments;
 import br.model.ModelFacade;
 
 public class Chat {
 	private ModelFacade observable;
 	private String message;
 
 	public Chat(ModelFacade modelFacade) {
 		this.observable = modelFacade;
 		this.message = "";
 	}
 
	//Pas trop cool si c'est entre deux update mais bon. tampax
 	public void ajoutMessage(String nomJoueur, String message) {
		message = nomJoueur + " : " + message + "\n";
 		observable.notifyView(UpdateArguments.CHAT_UPDATE);
 	}
 
 	public String getMessage() {
 		return this.message;
 	}
 }
