 package iu.texto.enums;
 
 public enum ItensMenuPrincipal {
 	STATUS(0, "Atualizar Status"),
 	ATUALIZACOES(1, "Ver o Feed de Notcias"),
	AMIGOS(1, "Lista de Amigos"),
	MENU_INICIAL(2, "Menu Inicial");
 	
 	private int id;
 	private String texto;
 	
 	private ItensMenuPrincipal(int id, String texto){
 		this.id = id;
 		this.texto = texto;
 	}
 
 	public int getId() {
 		return id;
 	}
 
 	public String getTexto() {
 		return texto;
 	}
 }
